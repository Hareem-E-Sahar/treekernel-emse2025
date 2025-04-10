package net.sourceforge.jspcemulator.client.emulator.peripheral;

import net.sourceforge.jspcemulator.client.emulator.*;
import net.sourceforge.jspcemulator.client.emulator.motherboard.*;
import net.sourceforge.jspcemulator.client.support.*;

/**
 * 
 * @author Chris Dennis
 * @author Kevin O'Dwyer (for JsPCEmulator)
 */
public class FloppyController implements IOPortCapable, DMATransferCapable, HardwareComponent, TimerResponsive {

    public static enum DriveType {

        DRIVE_144, DRIVE_288, DRIVE_120, DRIVE_NONE
    }

    private static final int SECTOR_LENGTH = 512;

    private static final int SECTOR_SIZE_CODE = 2;

    private static final int CONTROL_ACTIVE = 0x01;

    private static final int CONTROL_RESET = 0x02;

    private static final int CONTROL_SLEEP = 0x04;

    private static final int CONTROL_BUSY = 0x08;

    private static final int CONTROL_INTERRUPT = 0x10;

    private static final int DIRECTION_WRITE = 0;

    private static final int DIRECTION_READ = 1;

    private static final int DIRECTION_SCANE = 2;

    private static final int DIRECTION_SCANL = 3;

    private static final int DIRECTION_SCANH = 4;

    private static final int STATE_COMMAND = 0x00;

    private static final int STATE_STATUS = 0x01;

    private static final int STATE_DATA = 0x02;

    private static final int STATE_STATE = 0x03;

    private static final int STATE_MULTI = 0x10;

    private static final int STATE_SEEK = 0x20;

    private static final int STATE_FORMAT = 0x40;

    private static final byte CONTROLLER_VERSION = (byte) 0x90;

    private static final int INTERRUPT_LEVEL = 6;

    private static final int DMA_CHANNEL = 2;

    private static final int IOPORT_BASE = 0x3f0;

    private boolean drivesUpdated;

    private Timer resultTimer;

    private Clock clock;

    private int state;

    private boolean dmaEnabled;

    private int currentDrive;

    private int bootSelect;

    private byte[] fifo;

    private int dataOffset;

    private int dataLength;

    private int dataState;

    private int dataDirection;

    private int interruptStatus;

    private byte eot;

    private byte timer0;

    private byte timer1;

    private byte preCompensationTrack;

    private byte config;

    private byte lock;

    private byte pwrd;

    private FloppyDrive[] drives;

    private InterruptController irqDevice;

    private DMAController dma;

    public FloppyController() {
        ioportRegistered = false;
        drives = new FloppyDrive[2];
        config = (byte) 0x60;
        state = CONTROL_ACTIVE;
        fifo = new byte[SECTOR_LENGTH];
    }

    public int getType() {
        return 1;
    }

    public void callback() {
        stopTransfer((byte) 0x00, (byte) 0x00, (byte) 0x00);
    }

    public DriveType getDriveType(int number) {
        return drives[number].drive;
    }

    public int[] ioPortsRequested() {
        return new int[] { IOPORT_BASE + 1, IOPORT_BASE + 2, IOPORT_BASE + 3, IOPORT_BASE + 4, IOPORT_BASE + 5, IOPORT_BASE + 7 };
    }

    public int ioPortReadByte(int address) {
        switch(address & 0x07) {
            case 0x01:
                return readStatusB();
            case 0x02:
                return readDOR();
            case 0x03:
                return readTape();
            case 0x04:
                return readMainStatus();
            case 0x05:
                return readData();
            case 0x07:
                return readDirection();
            default:
                return 0xff;
        }
    }

    public int ioPortReadWord(int address) {
        return (ioPortReadByte(address) & 0xff) | ((ioPortReadByte(address + 1) << 8) & 0xff00);
    }

    public int ioPortReadLong(int address) {
        return (ioPortReadWord(address) & 0xffff) | ((ioPortReadWord(address + 2) << 16) & 0xffff0000);
    }

    public void ioPortWriteByte(int address, int data) {
        switch(address & 0x07) {
            case 0x02:
                writeDOR(data);
                break;
            case 0x03:
                writeTape(data);
                break;
            case 0x04:
                writeRate(data);
                break;
            case 0x05:
                writeData(data);
                break;
            default:
                break;
        }
    }

    public void ioPortWriteWord(int address, int data) {
        ioPortWriteByte(address, data & 0xff);
        ioPortWriteByte(address + 1, (data >>> 8) & 0xff);
    }

    public void ioPortWriteLong(int address, int data) {
        ioPortWriteWord(address, data & 0xffff);
        ioPortWriteWord(address + 2, (data >>> 16) & 0xffff);
    }

    private void reset(boolean doIRQ) {
        resetIRQ();
        currentDrive = 0;
        dataOffset = 0;
        dataLength = 0;
        dataState = STATE_COMMAND;
        dataDirection = DIRECTION_WRITE;
        drives[0].reset();
        drives[1].reset();
        resetFIFO();
        if (doIRQ) raiseIRQ(0xc0);
    }

    private void raiseIRQ(int status) {
        if (~(state & CONTROL_INTERRUPT) != 0) {
            irqDevice.setIRQ(INTERRUPT_LEVEL, 1);
            state |= CONTROL_INTERRUPT;
        }
        interruptStatus = status;
    }

    private void resetFIFO() {
        dataDirection = DIRECTION_WRITE;
        dataOffset = 0;
        dataState = (dataState & ~STATE_STATE) | STATE_COMMAND;
    }

    private void resetIRQ() {
        irqDevice.setIRQ(INTERRUPT_LEVEL, 0);
        state &= ~CONTROL_INTERRUPT;
    }

    private int readStatusB() {
        return 0;
    }

    private int readDOR() {
        int retval = 0;
        if ((getDrive(0).driveFlags & FloppyDrive.MOTOR_ON) != 0) retval |= 1 << 5;
        if ((getDrive(1).driveFlags & FloppyDrive.MOTOR_ON) != 0) retval |= 1 << 4;
        retval |= dmaEnabled ? 1 << 3 : 0;
        retval |= (state & CONTROL_RESET) == 0 ? 1 << 2 : 0;
        retval |= currentDrive;
        return retval;
    }

    private int readTape() {
        return bootSelect << 2;
    }

    private int readMainStatus() {
        int retval = 0;
        state &= ~(CONTROL_SLEEP | CONTROL_RESET);
        if ((state & CONTROL_BUSY) == 0) {
            retval |= 0x80;
            if (dataDirection == DIRECTION_READ) retval |= 0x40;
        }
        if ((dataState & STATE_STATE) == STATE_DATA || (dataState & STATE_STATE) == STATE_STATUS) retval |= 0x10;
        return retval;
    }

    private int readData() {
        FloppyDrive drive;
        drive = getCurrentDrive();
        state &= ~CONTROL_SLEEP;
        if ((dataState & STATE_STATE) == STATE_COMMAND) {
            return 0;
        }
        int offset = dataOffset;
        if ((dataState & STATE_STATE) == STATE_DATA) {
            offset %= SECTOR_LENGTH;
            if (offset == 0) {
                int length = Math.min(dataLength - dataOffset, SECTOR_LENGTH);
                drive.read(drive.currentSector(), fifo, length);
            }
        }
        int retval = fifo[offset];
        if (++dataOffset == dataLength) {
            dataOffset = 0;
            if ((dataState & STATE_STATE) == STATE_DATA) stopTransfer((byte) 0x20, (byte) 0x00, (byte) 0x00); else {
                resetFIFO();
                resetIRQ();
            }
        }
        return retval;
    }

    private int readDirection() {
        int retval = 0;
        if (((getDrive(0).driveFlags & FloppyDrive.REVALIDATE) != 0) || ((getDrive(1).driveFlags & FloppyDrive.REVALIDATE) != 0)) retval |= 0x80;
        getDrive(0).driveFlags &= ~FloppyDrive.REVALIDATE;
        getDrive(1).driveFlags &= ~FloppyDrive.REVALIDATE;
        return retval;
    }

    private void writeDOR(int data) {
        if (((state & CONTROL_RESET) != 0) && ((data & 0x04) == 0)) return;
        if ((data & 0x20) != 0) getDrive(1).start(); else getDrive(1).stop();
        if ((data & 0x10) != 0) getDrive(0).start(); else getDrive(0).stop();
        if ((data & 0x04) == 0) if ((state & CONTROL_RESET) == 0) state |= CONTROL_RESET; else if ((state & CONTROL_RESET) != 0) {
            reset(true);
            state &= ~(CONTROL_RESET | CONTROL_SLEEP);
        }
        currentDrive = data & 1;
    }

    private void writeTape(int data) {
        if ((state & CONTROL_RESET) != 0) return;
        bootSelect = (data >>> 2) & 1;
    }

    private void writeRate(int data) {
        if ((state & CONTROL_RESET) != 0) return;
        if ((data & 0x80) != 0) {
            state |= CONTROL_RESET;
            reset(true);
            state &= ~CONTROL_RESET;
        }
        if ((data & 0x40) != 0) {
            state |= CONTROL_SLEEP;
            reset(true);
        }
    }

    private void writeData(int data) {
        FloppyDrive drive = getCurrentDrive();
        if ((state & CONTROL_RESET) != 0) {
            return;
        }
        state &= ~CONTROL_SLEEP;
        if ((dataState & STATE_STATE) == STATE_STATUS) {
            return;
        }
        if ((dataState & STATE_STATE) == STATE_DATA) {
            fifo[dataOffset++] = (byte) data;
            if (dataOffset % SECTOR_LENGTH == (SECTOR_LENGTH - 1) || dataOffset == dataLength) drive.write(drive.currentSector(), fifo, SECTOR_LENGTH);
            if ((dataState & STATE_STATE) == STATE_DATA) stopTransfer((byte) 0x20, (byte) 0x00, (byte) 0x00);
            return;
        }
        if (dataOffset == 0) {
            switch(data & 0x5f) {
                case 0x46:
                case 0x4c:
                case 0x50:
                case 0x56:
                case 0x59:
                case 0x5d:
                    dataLength = 9;
                    enqueue(drive, data);
                    return;
                default:
                    break;
            }
            switch(data & 0x7f) {
                case 0x45:
                case 0x49:
                    dataLength = 9;
                    enqueue(drive, data);
                    return;
                default:
                    break;
            }
            switch(data) {
                case 0x03:
                case 0x0f:
                    dataLength = 3;
                    enqueue(drive, data);
                    return;
                case 0x04:
                case 0x07:
                case 0x12:
                case 0x33:
                case 0x4a:
                    dataLength = 2;
                    enqueue(drive, data);
                    return;
                case 0x08:
                    fifo[0] = (byte) (0x20 | (drive.head << 2) | currentDrive);
                    fifo[1] = (byte) drive.track;
                    setFIFO(2, false);
                    resetIRQ();
                    interruptStatus = 0xc0;
                    return;
                case 0x0e:
                    fifo[0] = (byte) getDrive(0).track;
                    fifo[1] = (byte) getDrive(1).track;
                    fifo[2] = 0;
                    fifo[3] = 0;
                    fifo[4] = timer0;
                    fifo[5] = dmaEnabled ? (byte) (timer1 << 1) : (byte) 0;
                    fifo[6] = (byte) drive.sectorCount;
                    fifo[7] = (byte) ((lock << 7) | (drive.perpendicular << 2));
                    fifo[8] = config;
                    fifo[9] = preCompensationTrack;
                    setFIFO(10, false);
                    return;
                case 0x10:
                    fifo[0] = CONTROLLER_VERSION;
                    setFIFO(1, true);
                    return;
                case 0x13:
                    dataLength = 4;
                    enqueue(drive, data);
                    return;
                case 0x14:
                    lock = 0;
                    fifo[0] = 0;
                    setFIFO(1, false);
                    return;
                case 0x17:
                case 0x8f:
                case 0xcf:
                    dataLength = 3;
                    enqueue(drive, data);
                    return;
                case 0x18:
                    fifo[0] = 0x41;
                    setFIFO(1, false);
                    return;
                case 0x2c:
                    fifo[0] = 0;
                    fifo[1] = 0;
                    fifo[2] = (byte) getDrive(0).track;
                    fifo[3] = (byte) getDrive(1).track;
                    fifo[4] = 0;
                    fifo[5] = 0;
                    fifo[6] = timer0;
                    fifo[7] = timer1;
                    fifo[8] = (byte) drive.sectorCount;
                    fifo[9] = (byte) ((lock << 7) | (drive.perpendicular << 2));
                    fifo[10] = config;
                    fifo[11] = preCompensationTrack;
                    fifo[12] = pwrd;
                    fifo[13] = 0;
                    fifo[14] = 0;
                    setFIFO(15, true);
                    return;
                case 0x42:
                    dataLength = 9;
                    enqueue(drive, data);
                    return;
                case 0x4c:
                    dataLength = 18;
                    enqueue(drive, data);
                    return;
                case 0x4d:
                case 0x8e:
                    dataLength = 6;
                    enqueue(drive, data);
                    return;
                case 0x94:
                    lock = 1;
                    fifo[0] = 0x10;
                    setFIFO(1, true);
                    return;
                case 0xcd:
                    dataLength = 11;
                    enqueue(drive, data);
                    return;
                default:
                    unimplemented();
                    return;
            }
        }
        enqueue(drive, data);
    }

    private void enqueue(FloppyDrive drive, int data) {
        fifo[dataOffset] = (byte) data;
        if (++dataOffset == dataLength) {
            if ((dataState & STATE_FORMAT) != 0) {
                formatSector();
                return;
            }
            switch(fifo[0] & 0x1f) {
                case 0x06:
                    startTransfer(DIRECTION_READ);
                    return;
                case 0x0c:
                    startTransferDelete(DIRECTION_READ);
                    return;
                case 0x16:
                    stopTransfer((byte) 0x20, (byte) 0x00, (byte) 0x00);
                    return;
                case 0x10:
                    startTransfer(DIRECTION_SCANE);
                    return;
                case 0x19:
                    startTransfer(DIRECTION_SCANL);
                    return;
                case 0x1d:
                    startTransfer(DIRECTION_SCANH);
                    return;
                default:
                    break;
            }
            switch(fifo[0] & 0x3f) {
                case 0x05:
                    startTransfer(DIRECTION_WRITE);
                    return;
                case 0x09:
                    startTransferDelete(DIRECTION_WRITE);
                    return;
                default:
                    break;
            }
            switch(fifo[0]) {
                case 0x03:
                    timer0 = (byte) ((fifo[1] >>> 4) & 0xf);
                    timer1 = (byte) (fifo[2] >>> 1);
                    dmaEnabled = ((fifo[2] & 1) != 1);
                    resetFIFO();
                    break;
                case 0x04:
                    currentDrive = fifo[1] & 1;
                    drive = getCurrentDrive();
                    drive.head = ((fifo[1] >>> 2) & 1);
                    fifo[0] = (byte) ((drive.readOnly << 6) | (drive.track == 0 ? 0x10 : 0x00) | (drive.head << 2) | currentDrive | 0x28);
                    setFIFO(1, false);
                    break;
                case 0x07:
                    currentDrive = fifo[1] & 1;
                    drive = getCurrentDrive();
                    drive.recalibrate();
                    resetFIFO();
                    raiseIRQ(0x20);
                    break;
                case 0x0f:
                    currentDrive = fifo[1] & 1;
                    drive = getCurrentDrive();
                    drive.start();
                    if (fifo[2] <= drive.track) drive.direction = 1; else drive.direction = 0;
                    resetFIFO();
                    if (fifo[2] > drive.maxTrack) raiseIRQ(0x60); else {
                        drive.track = fifo[2];
                        raiseIRQ(0x20);
                    }
                    break;
                case 0x12:
                    if ((fifo[1] & 0x80) != 0) drive.perpendicular = fifo[1] & 0x7;
                    resetFIFO();
                    break;
                case 0x13:
                    config = fifo[2];
                    preCompensationTrack = fifo[3];
                    resetFIFO();
                    break;
                case 0x17:
                    pwrd = fifo[1];
                    fifo[0] = fifo[1];
                    setFIFO(1, true);
                    break;
                case 0x33:
                    resetFIFO();
                    break;
                case 0x42:
                    startTransfer(DIRECTION_READ);
                    break;
                case 0x4A:
                    drive.head = (fifo[1] >>> 2) & 1;
                    resultTimer.setExpiry(clock.getTime() + (clock.getTickRate() / 50));
                    break;
                case 0x4C:
                    getDrive(0).track = fifo[3];
                    getDrive(1).track = fifo[4];
                    timer0 = fifo[7];
                    timer1 = fifo[8];
                    drive.sectorCount = fifo[9];
                    lock = (byte) (fifo[10] >>> 7);
                    drive.perpendicular = (fifo[10] >>> 2) & 0xf;
                    config = fifo[11];
                    preCompensationTrack = fifo[12];
                    pwrd = fifo[13];
                    resetFIFO();
                    break;
                case 0x4D:
                    currentDrive = fifo[1] & 1;
                    drive = getCurrentDrive();
                    dataState |= STATE_FORMAT;
                    if ((fifo[0] & 0x80) != 0) dataState |= STATE_MULTI; else dataState &= ~STATE_MULTI;
                    dataState &= ~STATE_SEEK;
                    drive.bps = fifo[2] > 7 ? 0x4000 : (0x80 << fifo[2]);
                    drive.sectorCount = fifo[3];
                    dataState &= ~STATE_FORMAT;
                    stopTransfer((byte) 0x00, (byte) 0x00, (byte) 0x00);
                    break;
                case (byte) 0x8E:
                    if ((fifo[dataOffset - 1] & 0x80) != 0) if ((fifo[dataOffset - 1] & 0x40) != 0) {
                        fifo[0] = fifo[1];
                        fifo[2] = 0;
                        fifo[3] = 0;
                        setFIFO(4, true);
                    } else resetFIFO(); else if (dataLength > 7) {
                        fifo[0] = (byte) (0x80 | (drive.head << 2) | currentDrive);
                        setFIFO(1, true);
                    }
                    break;
                case (byte) 0x8F:
                    currentDrive = fifo[1] & 1;
                    drive = getCurrentDrive();
                    drive.start();
                    drive.direction = 0;
                    if (fifo[2] + drive.track >= drive.maxTrack) drive.track = drive.maxTrack - 1; else drive.track += fifo[2];
                    resetFIFO();
                    raiseIRQ(0x20);
                    break;
                case (byte) 0xCD:
                    unimplemented();
                    break;
                case (byte) 0xCF:
                    currentDrive = fifo[1] & 1;
                    drive = getCurrentDrive();
                    drive.start();
                    drive.direction = 1;
                    if (fifo[2] > drive.track) drive.track = 0; else drive.track -= fifo[2];
                    resetFIFO();
                    raiseIRQ(0x20);
                    break;
            }
        }
    }

    private void setFIFO(int fifoLength, boolean doIRQ) {
        dataDirection = DIRECTION_READ;
        dataLength = fifoLength;
        dataOffset = 0;
        dataState = (dataState & ~STATE_STATE) | STATE_STATUS;
        if (doIRQ) raiseIRQ(0x00);
    }

    private FloppyDrive getCurrentDrive() {
        return getDrive(currentDrive);
    }

    private FloppyDrive getDrive(int driveNumber) {
        return drives[driveNumber - bootSelect];
    }

    private void unimplemented() {
        fifo[0] = (byte) 0x80;
        setFIFO(1, false);
    }

    private void startTransfer(int direction) {
        currentDrive = fifo[1] & 1;
        FloppyDrive drive = getCurrentDrive();
        byte kt = fifo[2];
        byte kh = fifo[3];
        byte ks = fifo[4];
        boolean didSeek = false;
        switch(drive.seek(0xff & kh, 0xff & kt, 0xff & ks, drive.sectorCount)) {
            case 2:
                stopTransfer((byte) 0x40, (byte) 0x00, (byte) 0x00);
                fifo[3] = kt;
                fifo[4] = kh;
                fifo[5] = ks;
                return;
            case 3:
                stopTransfer((byte) 0x40, (byte) 0x80, (byte) 0x00);
                fifo[3] = kt;
                fifo[4] = kh;
                fifo[5] = ks;
                return;
            case 4:
                stopTransfer((byte) 0x40, (byte) 0x00, (byte) 0x00);
                fifo[3] = kt;
                fifo[4] = kh;
                fifo[5] = ks;
                return;
            case 1:
                didSeek = true;
                break;
            default:
                break;
        }
        dataDirection = direction;
        dataOffset = 0;
        dataState = (dataState & ~STATE_STATE) | STATE_DATA;
        if ((fifo[0] & 0x80) != 0) dataState |= STATE_MULTI; else dataState &= ~STATE_MULTI;
        if (didSeek) dataState |= STATE_SEEK; else dataState &= ~STATE_SEEK;
        if (fifo[5] == 0x00) dataLength = fifo[8]; else {
            dataLength = 128 << fifo[5];
            int temp = drive.sectorCount - ks + 1;
            if ((fifo[0] & 0x80) != 0) temp += drive.sectorCount;
            dataLength *= temp;
        }
        eot = fifo[6];
        if (dmaEnabled) {
            int dmaMode = dma.getChannelMode(DMA_CHANNEL & 3);
            dmaMode = (dmaMode >>> 2) & 3;
            if (((direction == DIRECTION_SCANE || direction == DIRECTION_SCANL || direction == DIRECTION_SCANH) && dmaMode == 0) || (direction == DIRECTION_WRITE && dmaMode == 2) || (direction == DIRECTION_READ && dmaMode == 1)) {
                state |= CONTROL_BUSY;
                dma.holdDmaRequest(DMA_CHANNEL & 3);
                return;
            }
        }
        raiseIRQ(0x00);
    }

    private void stopTransfer(byte status0, byte status1, byte status2) {
        FloppyDrive drive = getCurrentDrive();
        fifo[0] = (byte) (status0 | (drive.head << 2) | currentDrive);
        fifo[1] = status1;
        fifo[2] = status2;
        fifo[3] = (byte) drive.track;
        fifo[4] = (byte) drive.head;
        fifo[5] = (byte) drive.sector;
        fifo[6] = SECTOR_SIZE_CODE;
        dataDirection = DIRECTION_READ;
        if ((state & CONTROL_BUSY) != 0) {
            dma.releaseDmaRequest(DMA_CHANNEL & 3);
            state &= ~CONTROL_BUSY;
        }
        setFIFO(7, true);
    }

    private void startTransferDelete(int direction) {
        stopTransfer((byte) 0x60, (byte) 0x00, (byte) 0x00);
    }

    private void formatSector() {
    }

    private static int memcmp(byte[] a1, byte[] a2, int offset, int length) {
        for (int i = 0; i < length; i++) if (a1[i] != a2[i + offset]) return a1[i] - a2[i + offset];
        return 0;
    }

    public int handleTransfer(DMAController.DMAChannel channel, int pos, int size) {
        byte status0 = 0x00, status1 = 0x00, status2 = 0x00;
        if ((state & CONTROL_BUSY) == 0) return 0;
        FloppyDrive drive = getCurrentDrive();
        if ((dataDirection == DIRECTION_SCANE) || (dataDirection == DIRECTION_SCANL) || (dataDirection == DIRECTION_SCANH)) status2 = 0x04;
        size = Math.min(size, dataLength);
        if (drive.device == null) {
            if (dataDirection == DIRECTION_WRITE) stopTransfer((byte) 0x60, (byte) 0x00, (byte) 0x00); else stopTransfer((byte) 0x40, (byte) 0x00, (byte) 0x00);
            return 0;
        }
        int relativeOffset = dataOffset % SECTOR_LENGTH;
        int startOffset;
        for (startOffset = dataOffset; dataOffset < size; ) {
            int length = Math.min(size - dataOffset, SECTOR_LENGTH - relativeOffset);
            if ((dataDirection != DIRECTION_WRITE) || (length < SECTOR_LENGTH) || (relativeOffset != 0)) if (drive.read(drive.currentSector(), fifo, 1) < 0) for (int i = 0; i < Math.min(fifo.length, SECTOR_LENGTH); i++) fifo[i] = (byte) 0x00;
            switch(dataDirection) {
                case DIRECTION_READ:
                    channel.writeMemory(fifo, relativeOffset, dataOffset, length);
                    break;
                case DIRECTION_WRITE:
                    channel.readMemory(fifo, relativeOffset, dataOffset, length);
                    if (drive.write(drive.currentSector(), fifo, 1) < 0) {
                        stopTransfer((byte) 0x60, (byte) 0x00, (byte) 0x00);
                        return length;
                    }
                    break;
                default:
                    {
                        byte[] tempBuffer = new byte[SECTOR_LENGTH];
                        channel.readMemory(tempBuffer, 0, dataOffset, length);
                        int ret = memcmp(tempBuffer, fifo, relativeOffset, length);
                        if (ret == 0) {
                            status2 = 0x08;
                            length = dataOffset - startOffset;
                            if (dataDirection == DIRECTION_SCANE || dataDirection == DIRECTION_SCANL || dataDirection == DIRECTION_SCANH) status2 = 0x08;
                            if ((dataState & STATE_SEEK) != 0) status0 |= 0x20;
                            dataLength -= length;
                            stopTransfer(status0, status1, status2);
                            return length;
                        }
                        if ((ret < 0 && dataDirection == DIRECTION_SCANL) || (ret > 0 && dataDirection == DIRECTION_SCANH)) {
                            status2 = 0x00;
                            length = dataOffset - startOffset;
                            if (dataDirection == DIRECTION_SCANE || dataDirection == DIRECTION_SCANL || dataDirection == DIRECTION_SCANH) status2 = 0x08;
                            if ((dataState & STATE_SEEK) != 0) status0 |= 0x20;
                            dataLength -= length;
                            stopTransfer(status0, status1, status2);
                            return length;
                        }
                    }
                    break;
            }
            dataOffset += length;
            relativeOffset = dataOffset % SECTOR_LENGTH;
            if (relativeOffset == 0) if ((drive.sector >= drive.sectorCount) || (drive.sector == eot)) {
                drive.sector = 1;
                if ((dataState & STATE_MULTI) != 0) if ((drive.head == 0) && (drive.headCount > 0)) drive.head = 1; else {
                    drive.head = 0;
                    drive.track++;
                } else drive.track++;
            } else drive.sector++;
        }
        int length = dataOffset - startOffset;
        if (dataDirection == DIRECTION_SCANE || dataDirection == DIRECTION_SCANL || dataDirection == DIRECTION_SCANH) status2 = 0x08;
        if ((dataState & STATE_SEEK) != 0) status0 |= 0x20;
        dataLength -= length;
        stopTransfer(status0, status1, status2);
        return length;
    }

    static class FloppyDrive {

        static final int MOTOR_ON = 0x01;

        static final int REVALIDATE = 0x02;

        static final int DOUBLE_SIDES = 0x01;

        BlockDevice device;

        DriveType drive;

        int driveFlags;

        int perpendicular;

        int head;

        int headCount;

        int track;

        int sector;

        int sectorCount;

        int direction;

        int readWrite;

        int flags;

        int maxTrack;

        int bps;

        int readOnly;

        FloppyFormat format;

        FloppyDrive(BlockDevice device) {
            this.device = device;
            drive = DriveType.DRIVE_NONE;
            driveFlags = 0;
            perpendicular = 0;
            sectorCount = 0;
            maxTrack = 0;
        }

        private void start() {
            driveFlags |= MOTOR_ON;
        }

        private void stop() {
            driveFlags &= ~MOTOR_ON;
        }

        private int seek(int seekHead, int seekTrack, int seekSector, int enableSeek) {
            if ((seekTrack > maxTrack) || (seekHead != 0 && (headCount == 0))) return 2;
            if (seekSector > sectorCount) return 3;
            int fileSector = calculateSector(seekTrack, seekHead, headCount, seekSector, sectorCount);
            if (fileSector != currentSector()) {
                if (enableSeek == 0) return 4;
                head = seekHead;
                if (track != seekTrack) {
                    track = seekTrack;
                    sector = seekSector;
                    return 1;
                }
                sector = seekSector;
            }
            return 0;
        }

        private int currentSector() {
            return calculateSector(track, head, headCount, sector, sectorCount);
        }

        private int calculateSector(int track, int head, int headCount, int sector, int sectorCount) {
            return ((((0xff & track) * headCount) + (0xff & head)) * (0xff & sectorCount)) + (0xff & sector) - 1;
        }

        private void recalibrate() {
            head = 0;
            track = 0;
            sector = 1;
            direction = 1;
            readWrite = 0;
        }

        private int read(int sector, byte[] buffer, int length) {
            return device.read(0xffffffffl & sector, buffer, length);
        }

        private int write(int sector, byte[] buffer, int length) {
            return device.write(0xffffffffl & sector, buffer, length);
        }

        private void reset() {
            stop();
            recalibrate();
        }

        private void revalidate() {
            driveFlags &= ~REVALIDATE;
            if (device != null && device.isInserted()) {
                format = FloppyFormat.findFormat(device.getTotalSectors(), drive);
                headCount = format.heads();
                if (headCount == 1) flags &= ~DOUBLE_SIDES; else flags |= DOUBLE_SIDES;
                maxTrack = format.tracks();
                sectorCount = (byte) format.sectors();
                readOnly = device.isReadOnly() ? 0x1 : 0x0;
                drive = format.drive();
            } else {
                sectorCount = 0;
                maxTrack = 0;
                flags &= ~DOUBLE_SIDES;
            }
            driveFlags |= REVALIDATE;
        }

        public String toString() {
            return (device == null) ? "<none>" : format.toString();
        }
    }

    private boolean ioportRegistered;

    public void reset() {
        irqDevice = null;
        clock = null;
        resultTimer = null;
        dma = null;
        ioportRegistered = false;
        fifo = new byte[SECTOR_LENGTH];
        config = (byte) 0x60;
        drives = new FloppyDrive[2];
        state = CONTROL_ACTIVE;
    }

    public boolean initialised() {
        return ((irqDevice != null) && (clock != null) && (dma != null) && (drives[0] != null) && ioportRegistered);
    }

    public void acceptComponent(HardwareComponent component) {
        if ((component instanceof InterruptController) && component.initialised()) irqDevice = (InterruptController) component;
        if ((component instanceof Clock) && component.initialised()) {
            clock = (Clock) component;
            resultTimer = clock.newTimer(this);
        }
        if ((component instanceof IOPortHandler) && component.initialised()) {
            ((IOPortHandler) component).registerIOPortCapable(this);
            ioportRegistered = true;
        }
        if ((component instanceof DMAController) && component.initialised()) if (((DMAController) component).isPrimary()) if (DMA_CHANNEL != -1) {
            dma = (DMAController) component;
            dmaEnabled = true;
            dma.registerChannel(DMA_CHANNEL & 3, this);
        }
        if ((component instanceof DriveSet) && component.initialised()) {
            drives[0] = new FloppyDrive(((DriveSet) component).getFloppyDrive(0));
            drives[1] = new FloppyDrive(((DriveSet) component).getFloppyDrive(1));
        }
        if (initialised()) {
            reset(false);
            for (int i = 0; i < 2; i++) if (drives[i] != null) drives[i].revalidate();
        }
    }

    public boolean updated() {
        return (irqDevice.updated() && clock.updated() && dma.updated() && drivesUpdated && ioportRegistered);
    }

    public void updateComponent(HardwareComponent component) {
        if ((component instanceof IOPortHandler) && component.updated()) {
            ((IOPortHandler) component).registerIOPortCapable(this);
            ioportRegistered = true;
        }
        if ((component instanceof DMAController) && component.updated()) if (((DMAController) component).isPrimary()) if (DMA_CHANNEL != -1) {
            dma = (DMAController) component;
            dmaEnabled = true;
            dma.registerChannel(DMA_CHANNEL & 3, this);
        }
        if ((component instanceof Clock) && component.initialised()) {
            clock = (Clock) component;
            clock.update(resultTimer);
        }
    }

    public String toString() {
        return "Intel 82078 Floppy Controller [" + drives[0].toString() + ", " + drives[1].toString() + "]";
    }
}
