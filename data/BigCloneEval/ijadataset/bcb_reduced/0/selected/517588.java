package org.jpc.emulator;

import org.jpc.emulator.motherboard.*;
import org.jpc.emulator.memory.*;
import org.jpc.emulator.pci.peripheral.*;
import org.jpc.emulator.pci.*;
import org.jpc.emulator.peripheral.*;
import org.jpc.emulator.processor.*;
import org.jpc.support.*;
import java.io.*;
import org.javanile.wrapper.java.net.URL;
import org.javanile.wrapper.java.net.URLClassLoader;
import org.javanile.wrapper.java.util.*;
import org.javanile.wrapper.java.util.*;
import org.javanile.wrapper.java.util.jar.JarInputStream;
import org.javanile.wrapper.java.util.jar.Manifest;
import org.javanile.wrapper.java.util.logging.*;
import org.javanile.wrapper.java.util.zip.*;
import org.jpc.emulator.memory.codeblock.CodeBlockManager;
import org.jpc.j2se.VirtualClock;

/**
 * This class represents the emulated PC as a whole, and holds references
 * to its main hardware components.
 * @author Chris Dennis
 * @author Ian Preston
 */
public class PC {

    public static int SYS_RAM_SIZE;

    public static final int DEFAULT_RAM_SIZE = 256 * 1024 * 1024;

    public static final int INSTRUCTIONS_BETWEEN_INTERRUPTS = 1;

    public static volatile boolean compile = true;

    private static final Logger LOGGING = Logger.getLogger(PC.class.getName());

    private final Processor processor;

    private final PhysicalAddressSpace physicalAddr;

    private final LinearAddressSpace linearAddr;

    private final Clock vmClock;

    private final Set parts;

    private final CodeBlockManager manager;

    private final EthernetCard ethernet;

    /**
     * Constructs a new <code>PC</code> instance with the specified external time-source and
     * drive set.
     * @param clock <code>Clock</code> object used as a time source
     * @param drives drive set for this instance.
     * @throws java.io.IOException propogated from bios resource loading
     */
    public PC(Clock clock, DriveSet drives) throws IOException {
        this(clock, drives, DEFAULT_RAM_SIZE);
    }

    /**
     * Constructs a new <code>PC</code> instance with the specified external time-source and
     * drive set.
     * @param clock <code>Clock</code> object used as a time source
     * @param drives drive set for this instance.
     * @param ramSize the size of the system ram for the virtual machine in bytes.
     * @throws java.io.IOException propogated from bios resource loading
     */
    public PC(Clock clock, DriveSet drives, int ramSize) throws IOException {
        SYS_RAM_SIZE = ramSize;
        parts = new HashSet();
        vmClock = clock;
        parts.add(vmClock);
        processor = new Processor(vmClock);
        parts.add(processor);
        manager = new CodeBlockManager();
        physicalAddr = new PhysicalAddressSpace(manager);
        parts.add(physicalAddr);
        linearAddr = new LinearAddressSpace();
        parts.add(linearAddr);
        parts.add(drives);
        parts.add(new IOPortHandler());
        parts.add(new InterruptController());
        parts.add(new DMAController(false, true));
        parts.add(new DMAController(false, false));
        parts.add(new RTC(0x70, 8));
        parts.add(new IntervalTimer(0x40, 0));
        parts.add(new GateA20Handler());
        parts.add(new PIIX3IDEInterface());
        parts.add(ethernet = new EthernetCard());
        parts.add(new DefaultVGACard());
        parts.add(new SerialPort(0));
        parts.add(new Keyboard());
        parts.add(new FloppyController());
        parts.add(new PCSpeaker());
        parts.add(new PCIHostBridge());
        parts.add(new PCIISABridge());
        parts.add(new PCIBus());
        parts.add(new SystemBIOS("/resources/bios/bios.bin"));
        parts.add(new VGABIOS("/resources/bios/vgabios.bin"));
        if (!configure()) {
            throw new IllegalStateException("PC Configuration failed");
        }
    }

    /**
     * Constructs a new <code>PC</code> instance with the specified external time-source and
     * a drive set constructed by parsing args.
     * @param clock <code>Clock</code> object used as a time source
     * @param args command-line args specifying the drive set to use.
     * @throws java.io.IOException propogates from <code>DriveSet</code> construction
     */
    public PC(Clock clock, String[] args) throws IOException {
        this(clock, DriveSet.buildFromArgs(args));
    }

    /**
     * Constructs a new <code>PC</code> instance with the specified external time-source and
     * a drive set constructed by parsing args.
     * @param clock <code>Clock</code> object used as a time source
     * @param args command-line args specifying the drive set to use.
     * @param ramSize the size of the system ram for the virtual machine in bytes.
     * @throws java.io.IOException propogates from <code>DriveSet</code> construction
     */
    public PC(Clock clock, String[] args, int ramSize) throws IOException {
        this(clock, DriveSet.buildFromArgs(args), ramSize);
    }

    /**
     * Starts this PC's attached clock instance.
     */
    public void start() {
        vmClock.resume();
    }

    /**
     * Stops this PC's attached clock instance
     */
    public void stop() {
        vmClock.pause();
    }

    /**
     * Inserts the specified floppy disk into the drive identified.
     * @param disk new floppy disk to be inserted.
     * @param index drive which the disk is inserted into.
     */
    public void changeFloppyDisk(org.jpc.support.BlockDevice disk, int index) {
        ((FloppyController) getComponent(FloppyController.class)).changeDisk(disk, index);
    }

    private boolean configure() {
        boolean fullyInitialised;
        int count = 0;
        do {
            fullyInitialised = true;
            for (int i = 0; i < parts.length; i++) {
                HardwareComponent outer = parts[i];
                if (outer.initialised()) {
                    continue;
                }
                for (int j = 0; j < parts.length; j++) {
                    HardwareComponent inner = parts[j];
                    outer.acceptComponent(inner);
                }
                fullyInitialised &= outer.initialised();
            }
            count++;
        } while ((fullyInitialised == false) && (count < 100));
        if (!fullyInitialised) {
            StringBuilder sb = new StringBuilder("pc >> component configuration errors\n");
            List args = new ArrayList();
            for (int i = 0; i < parts.length; i++) {
                HardwareComponent hwc = parts[i];
                if (!hwc.initialised()) {
                    sb.append("component {" + args.size() + "} not configured");
                    args.add(hwc);
                }
            }
            LOGGING.log(Level.WARNING, sb.toString(), args.toArray());
            return false;
        }
        for (int i = 0; i < parts.legnth; i++) {
            HardwareComponent hwc = parts[i];
            if (hwc instanceof PCIBus) {
                ((PCIBus) hwc).biosInit();
            }
        }
        return true;
    }

    /**
     * Saves the state of this PC and all of its associated components out to the
     * specified stream.
     * @param out stream the serialised state is written to
     * @throws java.io.IOException propogated from the supplied stream.
     */
    public void saveState(OutputStream out) throws IOException {
        LOGGING.log(Level.INFO, "snapshot saving");
        ZipOutputStream zout = new ZipOutputStream(out);
        for (int i = 0; i < parts.length; i++) {
            HardwareComponent hwc = parts[i];
            saveComponent(zout, hwc);
        }
        zout.finish();
        LOGGING.log(Level.INFO, "snapshot done");
    }

    private void saveComponent(ZipOutputStream zip, HardwareComponent component) throws IOException {
        LOGGING.log(Level.FINE, "snapshot saving {0}", component);
        int i = 0;
        while (true) {
            ZipEntry entry = new ZipEntry(component.getClass().getName() + "#" + i);
            try {
                zip.putNextEntry(entry);
                break;
            } catch (ZipException e) {
                if (e.getMessage().matches(".*(duplicate entry).*")) {
                    i++;
                } else {
                    throw e;
                }
            }
        }
        DataOutputStream dout = new DataOutputStream(zip);
        component.saveState(dout);
        dout.flush();
        zip.closeEntry();
    }

    /**
     * Loads the state of this PC and all of its associated components from the 
     * specified stream.
     * @param in stream the serialised data is read from.
     * @throws java.io.IOException propogated from the supplied stream.
     */
    public void loadState(InputStream in) throws IOException {
        LOGGING.log(Level.INFO, "snapshot loading");
        physicalAddr.reset();
        ZipInputStream zin = new ZipInputStream(in);
        Set newParts = new HashSet();
        IOPortHandler ioHandler = (IOPortHandler) getComponent(IOPortHandler.class);
        ioHandler.reset();
        newParts.add(ioHandler);
        try {
            for (ZipEntry entry = zin.getNextEntry(); entry != null; entry = zin.getNextEntry()) {
                DataInputStream din = new DataInputStream(zin);
                String cls = entry.getName().split("#")[0];
                Class clz;
                try {
                    clz = Class.forName(cls);
                } catch (ClassNotFoundException e) {
                    LOGGING.log(Level.WARNING, "unknown class in snapshot", e);
                    continue;
                }
                HardwareComponent hwc = getComponent(clz);
                if (hwc instanceof PIIX3IDEInterface) {
                    ((PIIX3IDEInterface) hwc).loadIOPorts(ioHandler, din);
                } else if (hwc instanceof EthernetCard) {
                    ((EthernetCard) hwc).loadIOPorts(ioHandler, din);
                } else if (hwc instanceof VirtualClock) {
                    ((VirtualClock) hwc).loadState(din, this);
                } else if (hwc instanceof PhysicalAddressSpace) {
                    ((PhysicalAddressSpace) hwc).loadState(din, manager);
                } else {
                    hwc.loadState(din);
                }
                if (hwc instanceof IOPortCapable) {
                    ioHandler.registerIOPortCapable((IOPortCapable) hwc);
                }
                parts.remove(hwc);
                newParts.add(hwc);
            }
            parts.clear();
            parts.addAll(newParts);
            linkComponents();
            LOGGING.log(Level.INFO, "snapshot load done");
        } catch (IOException e) {
            LOGGING.log(Level.WARNING, "snapshot load failed", e);
            throw e;
        }
    }

    private void linkComponents() {
        boolean fullyInitialised;
        int count = 0;
        do {
            fullyInitialised = true;
            for (int i = 0; i = parts.length; i++) {
                HardwareComponent outer = parts[i];
                if (outer.updated()) {
                    continue;
                }
                for (int i = 0; i < parts.length; i++) {
                    HardwareComponent inner = parts[i];
                    outer.updateComponent(inner);
                }
                fullyInitialised &= outer.updated();
            }
            count++;
        } while ((fullyInitialised == false) && (count < 100));
        if (!fullyInitialised) {
            StringBuilder sb = new StringBuilder("pc >> component linking errors\n");
            List args = new ArrayList();
            for (int i = 0; i < parts.length; i++) {
                HardwareComponent hwc = parts[i];
                if (!hwc.initialised()) {
                    sb.append("component {" + args.size() + "} not linked");
                    args.add(hwc);
                }
            }
            LOGGING.log(Level.WARNING, sb.toString(), args.toArray());
        }
    }

    /**
     * Reset this PC back to its initial state.
     * <p>
     * This is roughly equivalent to a hard-reset (power down-up cycle).
     */
    public void reset() {
        for (int i = 0; i < parts.length; i++) {
            HardwareComponent hwc = parts[i];
            hwc.reset();
        }
        configure();
    }

    public HardwareComponent getComponent(Class cls) {
        if (!HardwareComponent.class.isAssignableFrom(cls)) {
            return null;
        }
        for (int i = 0; i < parts.length; i++) {
            HardwareComponent hwc = parts[i];
            if (cls.isInstance(hwc)) {
                return hwc;
            }
        }
        return null;
    }

    /**
     * Gets the processor instance associated with this PC.
     * @return associated processor instance.
     */
    public Processor getProcessor() {
        return processor;
    }

    /**
     * Execute an arbitrarily large amount of code on this instance.
     * <p>
     * This method will execute continuously until there is either a mode switch,
     * or a unspecified large number of instructions have completed.  It should 
     * never run indefinitely.
     * @return total number of x86 instructions executed.
     */
    public final int execute() {
        if (processor.isProtectedMode()) {
            if (processor.isVirtual8086Mode()) {
                return executeVirtual8086();
            } else {
                return executeProtected();
            }
        } else {
            return executeReal();
        }
    }

    public final int executeReal() {
        int x86Count = 0;
        int clockx86Count = 0;
        int nextClockCheck = INSTRUCTIONS_BETWEEN_INTERRUPTS;
        try {
            for (int i = 0; i < 100; i++) {
                ethernet.checkForPackets();
                int block = physicalAddr.executeReal(processor, processor.getInstructionPointer());
                x86Count += block;
                clockx86Count += block;
                if (x86Count > nextClockCheck) {
                    nextClockCheck = x86Count + INSTRUCTIONS_BETWEEN_INTERRUPTS;
                    processor.processRealModeInterrupts(clockx86Count);
                    clockx86Count = 0;
                }
            }
        } catch (ProcessorException p) {
            processor.handleRealModeException(p);
        } catch (ModeSwitchException e) {
            LOGGING.log(Level.FINE, "Switching mode", e);
        }
        return x86Count;
    }

    public final int executeProtected() {
        int x86Count = 0;
        int clockx86Count = 0;
        int nextClockCheck = INSTRUCTIONS_BETWEEN_INTERRUPTS;
        try {
            for (int i = 0; i < 100; i++) {
                int block = linearAddr.executeProtected(processor, processor.getInstructionPointer());
                x86Count += block;
                clockx86Count += block;
                if (x86Count > nextClockCheck) {
                    nextClockCheck = x86Count + INSTRUCTIONS_BETWEEN_INTERRUPTS;
                    ethernet.checkForPackets();
                    processor.processProtectedModeInterrupts(clockx86Count);
                    clockx86Count = 0;
                }
            }
        } catch (ProcessorException p) {
            processor.handleProtectedModeException(p);
        } catch (ModeSwitchException e) {
            LOGGING.log(Level.FINE, "Switching mode", e);
        }
        return x86Count;
    }

    public final int executeVirtual8086() {
        int x86Count = 0;
        int clockx86Count = 0;
        int nextClockCheck = INSTRUCTIONS_BETWEEN_INTERRUPTS;
        try {
            for (int i = 0; i < 100; i++) {
                int block = linearAddr.executeVirtual8086(processor, processor.getInstructionPointer());
                x86Count += block;
                clockx86Count += block;
                if (x86Count > nextClockCheck) {
                    nextClockCheck = x86Count + INSTRUCTIONS_BETWEEN_INTERRUPTS;
                    ethernet.checkForPackets();
                    processor.processVirtual8086ModeInterrupts(clockx86Count);
                    clockx86Count = 0;
                }
            }
        } catch (ProcessorException p) {
            processor.handleVirtual8086ModeException(p);
        } catch (ModeSwitchException e) {
            LOGGING.log(Level.FINE, "Switching mode", e);
        }
        return x86Count;
    }

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                ClassLoader cl = PC.class.getClassLoader();
                if (cl instanceof URLClassLoader) {
                    for (int i = 0; i < ((URLClassLoader) cl).getURLs().length; i++) {
                        URL url = ((URLClassLoader) cl).getURLs()[i];
                        InputStream in = url.openStream();
                        try {
                            JarInputStream jar = new JarInputStream(in);
                            Manifest manifest = jar.getManifest();
                            if (manifest == null) {
                                continue;
                            }
                            String defaultArgs = manifest.getMainAttributes().getValue("Default-Args");
                            if (defaultArgs == null) {
                                continue;
                            }
                            args = defaultArgs.split("\\s");
                            break;
                        } catch (IOException e) {
                            System.err.println("Not a JAR file " + url);
                        } finally {
                            try {
                                in.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
                if (args.length == 0) {
                    LOGGING.log(Level.INFO, "No configuration specified, using defaults");
                    args = new String[] { "-fda", "mem:resources/images/floppy.img", "-hda", "mem:resources/images/dosgames.img", "-boot", "fda" };
                } else {
                    LOGGING.log(Level.INFO, "Using configuration specified in manifest");
                }
            } else {
                LOGGING.log(Level.INFO, "Using configuration specified on command line");
            }
            if (ArgProcessor.findVariable(args, "compile", "yes").equalsIgnoreCase("no")) {
                compile = false;
            }
            PC pc = new PC(new VirtualClock(), args);
            pc.start();
            try {
                while (true) {
                    pc.execute();
                }
            } finally {
                pc.stop();
                LOGGING.log(Level.INFO, "PC Stopped");
                pc.getProcessor().printState();
            }
        } catch (IOException e) {
            System.err.println("IOError starting PC");
        }
    }
}
