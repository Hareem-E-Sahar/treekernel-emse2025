package edumips64.core.is;

import edumips64.core.*;
import edumips64.utils.*;
import java.util.*;
import java.io.IOException;
import javax.swing.JOptionPane;

/** SYSCALL instruction, used to issue system calls.
 *
 * @author Andrea Spadaccini
 */
public class SYSCALL extends Instruction {

    String OPCODE_VALUE = "000000";

    String FINAL_VALUE = "001100";

    private java.util.List<String> syscall_params = new java.util.LinkedList<String>();

    private int syscall_n;

    private int return_value;

    private long address;

    private Dinero din;

    public void IF() {
        edumips64.Main.logger.log("SYSCALL (" + this.hashCode() + ") -> IF");
        try {
            CPU cpu = CPU.getInstance();
            din.IF(Converter.binToHex(Converter.intToBin(64, cpu.getLastPC().getValue())));
        } catch (IrregularStringOfBitsException e) {
            e.printStackTrace();
        }
    }

    public SYSCALL() {
        this.syntax = "%U";
        this.paramCount = 1;
        this.name = "SYSCALL";
        din = Dinero.getInstance();
    }

    public void ID() throws RAWException, IrregularWriteOperationException, IrregularStringOfBitsException {
        syscall_n = params.get(0);
        edumips64.Main.logger.log("SYSCALL (" + this.hashCode() + ") n = " + syscall_n);
        if (syscall_n == 0) {
            edumips64.Main.logger.debug("Stopping CPU due to SYSCALL (" + this.hashCode() + ")");
            CPU.getInstance().setStatus(CPU.CPUStatus.STOPPING);
        } else if ((syscall_n > 0) && (syscall_n <= 5)) {
            CPU cpu = CPU.getInstance();
            Register r14 = cpu.getRegister(14);
            if (r14.getWriteSemaphore() > 0) throw new RAWException();
            Register r1 = cpu.getRegister(1);
            r1.incrWriteSemaphore();
            address = r14.getValue();
            edumips64.Main.logger.debug("SYSCALL (" + this.hashCode() + "): locked register R14. Value = " + address);
        } else {
            edumips64.Main.logger.debug("INVALID SYSCALL (" + this.hashCode() + ")");
        }
    }

    public void EX() throws IrregularStringOfBitsException, IntegerOverflowException, TwosComplementSumException, IrregularWriteOperationException {
        edumips64.Main.logger.log("SYSCALL (" + this.hashCode() + ") -> EX");
    }

    public void MEM() throws IrregularStringOfBitsException, MemoryElementNotFoundException {
        edumips64.Main.logger.log("SYSCALL (" + this.hashCode() + ") -> MEM");
        if (syscall_n == 1) {
            String filename = fetchString(address);
            int flags_address = (int) address + filename.length();
            flags_address += 8 - (flags_address % 8);
            MemoryElement flags_m = Memory.getInstance().getCell((int) flags_address);
            int flags = (int) flags_m.getValue();
            for (int i = (int) address; i <= flags_address; i += 8) din.Load(Converter.binToHex(Converter.positiveIntToBin(64, i)), 8);
            edumips64.Main.logger.debug("We must open " + filename + " with flags " + flags);
            return_value = -1;
            try {
                return_value = edumips64.Main.iom.open(filename, flags);
            } catch (java.io.FileNotFoundException e) {
                JOptionPane.showMessageDialog(edumips64.Main.ioFrame, CurrentLocale.getString("FILE_NOT_FOUND"), "EduMIPS64 - " + CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            } catch (IOManagerException e) {
                JOptionPane.showMessageDialog(edumips64.Main.ioFrame, CurrentLocale.getString(e.getMessage()), "EduMIPS64 - " + CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(edumips64.Main.ioFrame, CurrentLocale.getString("IOEXCEPTION"), "EduMIPS64 - " + CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            }
        } else if (syscall_n == 2) {
            MemoryElement fd_cell = Memory.getInstance().getCell((int) address);
            int fd = (int) fd_cell.getValue();
            edumips64.Main.logger.debug("Closing fd " + fd);
            return_value = -1;
            try {
                return_value = edumips64.Main.iom.close(fd);
            } catch (IOException e1) {
                edumips64.Main.logger.debug("Error in closing " + fd);
            }
        } else if ((syscall_n == 3) || (syscall_n == 4)) {
            int fd, count;
            long buf_addr;
            MemoryElement temp = Memory.getInstance().getCell((int) address);
            fd = (int) temp.getValue();
            address += 8;
            temp = Memory.getInstance().getCell((int) address);
            buf_addr = temp.getValue();
            address += 8;
            temp = Memory.getInstance().getCell((int) address);
            count = (int) temp.getValue();
            address += 8;
            return_value = -1;
            try {
                if (syscall_n == 3) {
                    edumips64.Main.logger.debug("SYSCALL (" + this.hashCode() + "): trying to read from fd " + fd + " " + count + " bytes, writing them to address " + buf_addr);
                    return_value = edumips64.Main.iom.read(fd, buf_addr, count);
                } else {
                    edumips64.Main.logger.debug("SYSCALL (" + this.hashCode() + "): trying to write to fd " + fd + " " + count + " bytes, reading them from address " + buf_addr);
                    return_value = edumips64.Main.iom.write(fd, buf_addr, count);
                }
            } catch (java.io.FileNotFoundException e) {
                JOptionPane.showMessageDialog(edumips64.Main.ioFrame, CurrentLocale.getString("FILE_NOT_FOUND"), "EduMIPS64 - " + CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            } catch (IOManagerException e) {
                JOptionPane.showMessageDialog(edumips64.Main.ioFrame, CurrentLocale.getString(e.getMessage()), "EduMIPS64 - " + CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(edumips64.Main.ioFrame, CurrentLocale.getString("IOEXCEPTION"), "EduMIPS64 - " + CurrentLocale.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
            }
        } else if (syscall_n == 5) {
            StringBuffer temp = new StringBuffer();
            edumips64.Main.logger.debug("Reading memory cell at address " + address + ", searching for the address of the format string");
            MemoryElement tempMemCell = memory.getCell((int) address);
            int format_string_address = (int) tempMemCell.getValue();
            din.Load(Converter.binToHex(Converter.positiveIntToBin(64, address)), 8);
            String format_string = fetchString(format_string_address);
            edumips64.Main.logger.debug("Read " + format_string);
            int next_param_address = (int) address + 8;
            int t1 = (int) format_string_address + format_string.length();
            t1 += 8 - (t1 % 8);
            for (int i = (int) format_string_address; i < t1; i += 8) din.Load(Converter.binToHex(Converter.positiveIntToBin(64, i)), 8);
            int oldIndex = 0, newIndex = 0;
            while ((newIndex = format_string.indexOf('%', oldIndex)) > 0) {
                char type = format_string.charAt(newIndex + 1);
                temp.append(format_string.substring(oldIndex, newIndex));
                switch(type) {
                    case 's':
                        tempMemCell = memory.getCell(next_param_address);
                        int str_address = (int) tempMemCell.getValue();
                        edumips64.Main.logger.debug("Retrieving the string @ " + str_address + "...");
                        String param = fetchString(str_address);
                        next_param_address += 8;
                        int t2 = str_address + param.length();
                        t2 += 8 - (t2 % 8);
                        for (int i = str_address; i < t2; i += 8) din.Load(Converter.binToHex(Converter.positiveIntToBin(64, i)), 8);
                        edumips64.Main.logger.debug("Got " + param);
                        temp.append(param);
                        break;
                    case 'i':
                    case 'd':
                        edumips64.Main.logger.debug("Retrieving the integer @ " + next_param_address + "...");
                        MemoryElement memCell = memory.getCell((int) next_param_address);
                        din.Load(Converter.binToHex(Converter.positiveIntToBin(64, next_param_address)), 8);
                        Long val = memCell.getValue();
                        next_param_address += 8;
                        temp.append(val.toString());
                        edumips64.Main.logger.debug("Got " + val);
                        break;
                    case '%':
                        edumips64.Main.logger.debug("Literal %...");
                        temp.append('%');
                        break;
                    default:
                        edumips64.Main.logger.debug("Unknown placeholder");
                        break;
                }
                oldIndex = newIndex + 2;
            }
            temp.append(format_string.substring(oldIndex));
            edumips64.Main.logger.debug("That became " + temp.toString());
            edumips64.Main.ioFrame.write(temp.toString());
            return_value = temp.length();
        }
    }

    private String fetchString(long address) throws MemoryElementNotFoundException {
        StringBuffer temp = new StringBuffer();
        boolean end_of_string = false;
        while (!end_of_string) {
            MemoryElement memEl = memory.getCell((int) address);
            for (int i = 0; i < 8; ++i) {
                int tempInt = memEl.readByte(i);
                if (tempInt == 0) {
                    end_of_string = true;
                    break;
                }
                char c = (char) (tempInt);
                temp.append(c);
            }
            address += 8;
        }
        return temp.toString();
    }

    public void WB() throws IrregularStringOfBitsException, HaltException {
        edumips64.Main.logger.debug("SYSCALL (" + this.hashCode() + ") -> WB. n = " + syscall_n);
        if (syscall_n == 0) {
            edumips64.Main.logger.debug("Stopped CPU due to SYSCALL (" + this.hashCode() + ")");
            CPU.getInstance().setStatus(CPU.CPUStatus.HALTED);
            throw new HaltException();
        } else if (syscall_n > 0 && syscall_n <= 5) {
            edumips64.Main.logger.debug("SYSCALL (" + this.hashCode() + "): setting R1 to " + return_value);
            Register r1 = CPU.getInstance().getRegister(1);
            edumips64.Main.logger.debug("SYSCALL (" + this.hashCode() + "): got R1");
            r1.setBits(Converter.intToBin(64, return_value), 0);
            edumips64.Main.logger.debug("SYSCALL (" + this.hashCode() + "): set R1 to " + return_value);
            r1.decrWriteSemaphore();
            edumips64.Main.logger.debug("SYSCALL (" + this.hashCode() + "): decremented write semaphore");
        }
        edumips64.Main.logger.debug("SYSCALL (" + this.hashCode() + ") exiting from WB. n = " + syscall_n);
    }

    public void pack() throws IrregularStringOfBitsException {
        repr.setBits(OPCODE_VALUE, 0);
        repr.setBits(Converter.intToBin(20, params.get(0)), 6);
        repr.setBits(FINAL_VALUE, 26);
    }
}
