package jdos.cpu.core_normal;

import jdos.cpu.*;
import jdos.hardware.Memory;
import jdos.misc.Log;
import jdos.types.LogSeverities;
import jdos.types.LogTypes;
import jdos.util.IntRef;
import jdos.util.LongRef;

public class Prefix_66_0f extends Prefix_66 {

    private static final IntRef int_ref_1 = new IntRef(0);

    private static final LongRef long_ref_1 = new LongRef(0);

    static {
        ops[0x300] = new OP() {

            public final int call() {
                if ((CPU_Regs.flags & CPU_Regs.VM) != 0 || (!CPU.cpu.pmode)) return ILLEGAL_OPCODE;
                short rm = Fetchb();
                int which = (rm >> 3) & 7;
                switch(which) {
                    case 0x00:
                    case 0x01:
                        {
                            int saveval;
                            if (which == 0) saveval = CPU.CPU_SLDT(); else saveval = CPU.CPU_STR();
                            if (rm >= 0xc0) {
                                Modrm.GetEArw[rm].word(saveval);
                            } else {
                                int eaa = getEaa(rm);
                                Memory.mem_writew(eaa, saveval);
                            }
                        }
                        break;
                    case 0x02:
                    case 0x03:
                    case 0x04:
                    case 0x05:
                        {
                            int loadval;
                            if (rm >= 0xc0) {
                                loadval = Modrm.GetEArw[rm].word();
                            } else {
                                int eaa = getEaa(rm);
                                loadval = Memory.mem_readw(eaa);
                            }
                            switch(which) {
                                case 0x02:
                                    if (CPU.cpu.cpl != 0) return EXCEPTION(CPU.EXCEPTION_GP);
                                    if (CPU.CPU_LLDT(loadval)) return RUNEXCEPTION();
                                    break;
                                case 0x03:
                                    if (CPU.cpu.cpl != 0) return EXCEPTION(CPU.EXCEPTION_GP);
                                    if (CPU.CPU_LTR(loadval)) return RUNEXCEPTION();
                                    break;
                                case 0x04:
                                    CPU.CPU_VERR(loadval);
                                    break;
                                case 0x05:
                                    CPU.CPU_VERW(loadval);
                                    break;
                            }
                        }
                        break;
                    default:
                        if (Log.level <= LogSeverities.LOG_ERROR) Log.log(LogTypes.LOG_CPU, LogSeverities.LOG_ERROR, "GRP6:Illegal call " + Integer.toString(which, 16));
                        return ILLEGAL_OPCODE;
                }
                return HANDLED;
            }
        };
        ops[0x301] = new OP() {

            public final int call() {
                short rm = Fetchb();
                int which = (rm >> 3) & 7;
                if (rm < 0xc0) {
                    int eaa = getEaa(rm);
                    int limit;
                    switch(which) {
                        case 0x00:
                            Memory.mem_writew(eaa, CPU.CPU_SGDT_limit());
                            Memory.mem_writed(eaa + 2, CPU.CPU_SGDT_base());
                            break;
                        case 0x01:
                            Memory.mem_writew(eaa, CPU.CPU_SIDT_limit());
                            Memory.mem_writed(eaa + 2, CPU.CPU_SIDT_base());
                            break;
                        case 0x02:
                            if (CPU.cpu.pmode && CPU.cpu.cpl != 0) return EXCEPTION(CPU.EXCEPTION_GP);
                            CPU.CPU_LGDT(Memory.mem_readw(eaa), Memory.mem_readd(eaa + 2));
                            break;
                        case 0x03:
                            if (CPU.cpu.pmode && CPU.cpu.cpl != 0) return EXCEPTION(CPU.EXCEPTION_GP);
                            CPU.CPU_LIDT(Memory.mem_readw(eaa), Memory.mem_readd(eaa + 2));
                            break;
                        case 0x04:
                            Memory.mem_writew(eaa, CPU.CPU_SMSW() & 0xFFFF);
                            break;
                        case 0x06:
                            limit = Memory.mem_readw(eaa);
                            if (CPU.CPU_LMSW(limit)) return RUNEXCEPTION();
                            break;
                        case 0x07:
                            if (CPU.cpu.pmode && CPU.cpu.cpl != 0) return EXCEPTION(CPU.EXCEPTION_GP);
                            Paging.PAGING_ClearTLB();
                            break;
                    }
                } else {
                    switch(which) {
                        case 0x02:
                            if (CPU.cpu.pmode && CPU.cpu.cpl != 0) return EXCEPTION(CPU.EXCEPTION_GP);
                            return ILLEGAL_OPCODE;
                        case 0x03:
                            if (CPU.cpu.pmode && CPU.cpu.cpl != 0) return EXCEPTION(CPU.EXCEPTION_GP);
                            return ILLEGAL_OPCODE;
                        case 0x04:
                            Modrm.GetEArd[rm].dword = CPU.CPU_SMSW();
                            break;
                        case 0x06:
                            if (CPU.CPU_LMSW(Modrm.GetEArd[rm].dword)) return RUNEXCEPTION();
                            break;
                        default:
                            if (Log.level <= LogSeverities.LOG_ERROR) Log.log(LogTypes.LOG_CPU, LogSeverities.LOG_ERROR, "Illegal group 7 RM subfunction " + which);
                            return ILLEGAL_OPCODE;
                    }
                }
                return HANDLED;
            }
        };
        ops[0x302] = new OP() {

            public final int call() {
                if ((CPU_Regs.flags & CPU_Regs.VM) != 0 || (!CPU.cpu.pmode)) return ILLEGAL_OPCODE;
                short rm = Fetchb();
                int_ref_1.value = Modrm.Getrd[rm].dword;
                if (rm >= 0xc0) {
                    CPU.CPU_LAR(Modrm.GetEArw[rm].word(), int_ref_1);
                } else {
                    int eaa = getEaa(rm);
                    CPU.CPU_LAR(Memory.mem_readw(eaa), int_ref_1);
                }
                Modrm.Getrd[rm].dword = int_ref_1.value;
                return HANDLED;
            }
        };
        ops[0x303] = new OP() {

            public final int call() {
                if ((CPU_Regs.flags & CPU_Regs.VM) != 0 || (!CPU.cpu.pmode)) return ILLEGAL_OPCODE;
                short rm = Fetchb();
                int_ref_1.value = Modrm.Getrd[rm].dword;
                if (rm >= 0xc0) {
                    CPU.CPU_LSL(Modrm.GetEArw[rm].word(), int_ref_1);
                } else {
                    int eaa = getEaa(rm);
                    CPU.CPU_LSL(Memory.mem_readw(eaa), int_ref_1);
                }
                Modrm.Getrd[rm].dword = int_ref_1.value;
                return HANDLED;
            }
        };
        ops[0x380] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_O());
                return CONTINUE;
            }
        };
        ops[0x381] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_NO());
                return CONTINUE;
            }
        };
        ops[0x382] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_B());
                return CONTINUE;
            }
        };
        ops[0x383] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_NB());
                return CONTINUE;
            }
        };
        ops[0x384] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_Z());
                return CONTINUE;
            }
        };
        ops[0x385] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_NZ());
                return CONTINUE;
            }
        };
        ops[0x386] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_BE());
                return CONTINUE;
            }
        };
        ops[0x387] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_NBE());
                return CONTINUE;
            }
        };
        ops[0x388] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_S());
                return CONTINUE;
            }
        };
        ops[0x389] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_NS());
                return CONTINUE;
            }
        };
        ops[0x38a] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_P());
                return CONTINUE;
            }
        };
        ops[0x38b] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_NP());
                return CONTINUE;
            }
        };
        ops[0x38c] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_L());
                return CONTINUE;
            }
        };
        ops[0x38d] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_NL());
                return CONTINUE;
            }
        };
        ops[0x38e] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_LE());
                return CONTINUE;
            }
        };
        ops[0x38f] = new OP() {

            public final int call() {
                JumpCond32_d(Flags.TFLG_NLE());
                return CONTINUE;
            }
        };
        ops[0x3a0] = new OP() {

            public final int call() {
                CPU.CPU_Push32(CPU.Segs_FSval);
                return HANDLED;
            }
        };
        ops[0x3a1] = new OP() {

            public final int call() {
                if (CPU.CPU_PopSegFS(true)) return RUNEXCEPTION();
                return HANDLED;
            }
        };
        ops[0x3a3] = new OP() {

            public final int call() {
                FillFlags();
                short rm = Fetchb();
                int mask = 1 << (Modrm.Getrd[rm].dword & 31);
                if (rm >= 0xc0) {
                    SETFLAGBIT(CF, (Modrm.GetEArd[rm].dword & mask) != 0);
                } else {
                    int eaa = getEaa(rm);
                    eaa += (Modrm.Getrd[rm].dword >> 5) * 4;
                    int old = Memory.mem_readd(eaa);
                    SETFLAGBIT(CF, (old & mask) != 0);
                }
                return HANDLED;
            }
        };
        ops[0x3a4] = new OP() {

            public final int call() {
                final short rm = Fetchb();
                if (rm >= 0xc0) {
                    int op3 = Fetchb() & 0x1F;
                    if (op3 != 0) {
                        Reg r = Modrm.GetEArd[rm];
                        r.dword = DSHLD(Modrm.Getrd[rm].dword, op3, r.dword);
                    }
                } else {
                    int eaa = getEaa(rm);
                    int op3 = Fetchb() & 0x1F;
                    if (op3 != 0) Memory.mem_writed(eaa, DSHLD(Modrm.Getrd[rm].dword, op3, Memory.mem_readd(eaa)));
                }
                return HANDLED;
            }
        };
        ops[0x3a5] = new OP() {

            public final int call() {
                final short rm = Fetchb();
                int val = reg_ecx.dword & 0x1f;
                if (rm >= 0xc0) {
                    if (val != 0) {
                        Reg r = Modrm.GetEArd[rm];
                        r.dword = DSHLD(Modrm.Getrd[rm].dword, val, r.dword);
                    }
                } else {
                    int eaa = getEaa(rm);
                    if (val != 0) Memory.mem_writed(eaa, DSHLD(Modrm.Getrd[rm].dword, val, Memory.mem_readd(eaa)));
                }
                return HANDLED;
            }
        };
        ops[0x3a8] = new OP() {

            public final int call() {
                CPU.CPU_Push32(CPU.Segs_GSval);
                return HANDLED;
            }
        };
        ops[0x3a9] = new OP() {

            public final int call() {
                if (CPU.CPU_PopSegGS(true)) return RUNEXCEPTION();
                return HANDLED;
            }
        };
        ops[0x3ab] = new OP() {

            public final int call() {
                FillFlags();
                short rm = Fetchb();
                Reg rd = Modrm.Getrd[rm];
                int mask = 1 << (rd.dword & 31);
                if (rm >= 0xc0) {
                    Reg eard = Modrm.GetEArd[rm];
                    SETFLAGBIT(CF, (eard.dword & mask) != 0);
                    eard.dword |= mask;
                } else {
                    int eaa = getEaa(rm);
                    eaa += (rd.dword >> 5) * 4;
                    int old = Memory.mem_readd(eaa);
                    Memory.mem_writed(eaa, old | mask);
                    SETFLAGBIT(CF, (old & mask) != 0);
                }
                return HANDLED;
            }
        };
        ops[0x3ac] = new OP() {

            public final int call() {
                final short rm = Fetchb();
                if (rm >= 0xc0) {
                    int op3 = Fetchb() & 0x1F;
                    if (op3 != 0) {
                        Reg r = Modrm.GetEArd[rm];
                        r.dword = DSHRD(Modrm.Getrd[rm].dword, op3, r.dword);
                    }
                } else {
                    int eaa = getEaa(rm);
                    int op3 = Fetchb() & 0x1F;
                    if (op3 != 0) Memory.mem_writed(eaa, DSHRD(Modrm.Getrd[rm].dword, op3, Memory.mem_readd(eaa)));
                }
                return HANDLED;
            }
        };
        ops[0x3ad] = new OP() {

            public final int call() {
                final short rm = Fetchb();
                int val = reg_ecx.dword & 0x1f;
                if (rm >= 0xc0) {
                    if (val != 0) {
                        Reg r = Modrm.GetEArd[rm];
                        r.dword = DSHRD(Modrm.Getrd[rm].dword, val, r.dword);
                    }
                } else {
                    int eaa = getEaa(rm);
                    if (val != 0) Memory.mem_writed(eaa, DSHRD(Modrm.Getrd[rm].dword, val, Memory.mem_readd(eaa)));
                }
                return HANDLED;
            }
        };
        ops[0x3af] = new OP() {

            public final int call() {
                final short rm = Fetchb();
                Reg r = Modrm.Getrd[rm];
                if (rm >= 0xc0) {
                    r.dword = DIMULD(Modrm.GetEArd[rm].dword, r.dword);
                } else {
                    r.dword = DIMULD(Memory.mem_readd(getEaa(rm)), r.dword);
                }
                return HANDLED;
            }
        };
        ops[0x3b1] = new OP() {

            public final int call() {
                if (CPU.CPU_ArchitectureType < CPU.CPU_ARCHTYPE_486NEW) return ILLEGAL_OPCODE;
                FillFlags();
                short rm = Fetchb();
                if (rm >= 0xc0) {
                    Reg eard = Modrm.GetEArd[rm];
                    Instructions.CMPD(eard.dword, reg_eax.dword);
                    if (eard.dword == reg_eax.dword) {
                        eard.dword = Modrm.Getrd[rm].dword;
                        SETFLAGBIT(ZF, true);
                    } else {
                        reg_eax.dword = eard.dword;
                        SETFLAGBIT(ZF, false);
                    }
                } else {
                    int eaa = getEaa(rm);
                    int val = Memory.mem_readd(eaa);
                    Instructions.CMPD(val, reg_eax.dword);
                    if (val == reg_eax.dword) {
                        Memory.mem_writed(eaa, Modrm.Getrd[rm].dword);
                        SETFLAGBIT(ZF, true);
                    } else {
                        Memory.mem_writed(eaa, val);
                        reg_eax.dword = val;
                        SETFLAGBIT(ZF, false);
                    }
                }
                return HANDLED;
            }
        };
        ops[0x3b2] = new OP() {

            public final int call() {
                short rm = Fetchb();
                if (rm >= 0xc0) return ILLEGAL_OPCODE;
                int eaa = getEaa(rm);
                if (CPU.CPU_SetSegGeneralSS(Memory.mem_readw(eaa + 4))) return RUNEXCEPTION();
                Modrm.Getrd[rm].dword = Memory.mem_readd(eaa);
                return HANDLED;
            }
        };
        ops[0x3b3] = new OP() {

            public final int call() {
                FillFlags();
                short rm = Fetchb();
                Reg rd = Modrm.Getrd[rm];
                int mask = 1 << (rd.dword & 31);
                if (rm >= 0xc0) {
                    Reg eard = Modrm.GetEArd[rm];
                    SETFLAGBIT(CF, (eard.dword & mask) != 0);
                    eard.dword &= ~mask;
                } else {
                    int eaa = getEaa(rm);
                    eaa += (rd.dword >> 5) * 4;
                    int old = Memory.mem_readd(eaa);
                    Memory.mem_writed(eaa, old & ~mask);
                    SETFLAGBIT(CF, (old & mask) != 0);
                }
                return HANDLED;
            }
        };
        ops[0x3b4] = new OP() {

            public final int call() {
                short rm = Fetchb();
                if (rm >= 0xc0) return ILLEGAL_OPCODE;
                int eaa = getEaa(rm);
                if (CPU.CPU_SetSegGeneralFS(Memory.mem_readw(eaa + 4))) return RUNEXCEPTION();
                Modrm.Getrd[rm].dword = Memory.mem_readd(eaa);
                return HANDLED;
            }
        };
        ops[0x3b5] = new OP() {

            public final int call() {
                short rm = Fetchb();
                if (rm >= 0xc0) return ILLEGAL_OPCODE;
                int eaa = getEaa(rm);
                if (CPU.CPU_SetSegGeneralGS(Memory.mem_readw(eaa + 4))) return RUNEXCEPTION();
                Modrm.Getrd[rm].dword = Memory.mem_readd(eaa);
                return HANDLED;
            }
        };
        ops[0x3b6] = new OP() {

            public final int call() {
                short rm = Fetchb();
                if (rm >= 0xc0) {
                    Modrm.Getrd[rm].dword = Modrm.GetEArb[rm].get();
                } else {
                    int eaa = getEaa(rm);
                    Modrm.Getrd[rm].dword = Memory.mem_readb(eaa);
                }
                return HANDLED;
            }
        };
        ops[0x3b7] = new OP() {

            public final int call() {
                short rm = Fetchb();
                if (rm >= 0xc0) {
                    Modrm.Getrd[rm].dword = Modrm.GetEArw[rm].word();
                } else {
                    int eaa = getEaa(rm);
                    Modrm.Getrd[rm].dword = Memory.mem_readw(eaa);
                }
                return HANDLED;
            }
        };
        ops[0x3ba] = new OP() {

            public final int call() {
                FillFlags();
                short rm = Fetchb();
                if (rm >= 0xc0) {
                    int mask = 1 << (Fetchb() & 31);
                    Reg eard = Modrm.GetEArd[rm];
                    SETFLAGBIT(CF, (eard.dword & mask) != 0);
                    switch(rm & 0x38) {
                        case 0x20:
                            break;
                        case 0x28:
                            eard.dword |= mask;
                            break;
                        case 0x30:
                            eard.dword &= ~mask;
                            break;
                        case 0x38:
                            if (GETFLAG(CF) != 0) eard.dword &= ~mask; else eard.dword |= mask;
                            break;
                        default:
                            Log.exit("CPU:66:0F:BA:Illegal subfunction " + Integer.toString(rm & 0x38, 16));
                    }
                } else {
                    int eaa = getEaa(rm);
                    int old = Memory.mem_readd(eaa);
                    int mask = 1 << (Fetchb() & 31);
                    switch(rm & 0x38) {
                        case 0x20:
                            break;
                        case 0x28:
                            Memory.mem_writed(eaa, old | mask);
                            break;
                        case 0x30:
                            Memory.mem_writed(eaa, old & ~mask);
                            break;
                        case 0x38:
                            if (GETFLAG(CF) != 0) old &= ~mask; else old |= mask;
                            Memory.mem_writed(eaa, old);
                            break;
                        default:
                            Log.exit("CPU:66:0F:BA:Illegal subfunction " + Integer.toString(rm & 0x38, 16));
                    }
                    SETFLAGBIT(CF, (old & mask) != 0);
                }
                return HANDLED;
            }
        };
        ops[0x3bb] = new OP() {

            public final int call() {
                FillFlags();
                short rm = Fetchb();
                int mask = 1 << (Modrm.Getrd[rm].dword & 31);
                if (rm >= 0xc0) {
                    Reg eard = Modrm.GetEArd[rm];
                    SETFLAGBIT(CF, (eard.dword & mask) != 0);
                    eard.dword ^= mask;
                } else {
                    int eaa = getEaa(rm);
                    eaa += (Modrm.Getrd[rm].dword >> 5) * 4;
                    int old = Memory.mem_readd(eaa);
                    Memory.mem_writed(eaa, old ^ mask);
                    SETFLAGBIT(CF, (old & mask) != 0);
                }
                return HANDLED;
            }
        };
        ops[0x3bc] = new OP() {

            public final int call() {
                short rm = Fetchb();
                int result, value;
                if (rm >= 0xc0) {
                    value = Modrm.GetEArd[rm].dword;
                } else {
                    int eaa = getEaa(rm);
                    value = Memory.mem_readd(eaa);
                }
                if (value == 0) {
                    SETFLAGBIT(ZF, true);
                } else {
                    result = 0;
                    while ((value & 0x01) == 0) {
                        result++;
                        value >>>= 1;
                    }
                    SETFLAGBIT(ZF, false);
                    Modrm.Getrd[rm].dword = result;
                }
                lflags.type = t_UNKNOWN;
                return HANDLED;
            }
        };
        ops[0x3bd] = new OP() {

            public final int call() {
                short rm = Fetchb();
                int result, value;
                if (rm >= 0xc0) {
                    value = Modrm.GetEArd[rm].dword;
                } else {
                    int eaa = getEaa(rm);
                    value = Memory.mem_readd(eaa);
                }
                if (value == 0) {
                    SETFLAGBIT(ZF, true);
                } else {
                    result = 31;
                    while ((value & 0x80000000) == 0) {
                        result--;
                        value <<= 1;
                    }
                    SETFLAGBIT(ZF, false);
                    Modrm.Getrd[rm].dword = result;
                }
                lflags.type = t_UNKNOWN;
                return HANDLED;
            }
        };
        ops[0x3be] = new OP() {

            public final int call() {
                short rm = Fetchb();
                if (rm >= 0xc0) {
                    Modrm.Getrd[rm].dword = (byte) (Modrm.GetEArb[rm].get());
                } else {
                    int eaa = getEaa(rm);
                    Modrm.Getrd[rm].dword = (byte) Memory.mem_readb(eaa);
                }
                return HANDLED;
            }
        };
        ops[0x3bf] = new OP() {

            public final int call() {
                short rm = Fetchb();
                if (rm >= 0xc0) {
                    Modrm.Getrd[rm].dword = (short) (Modrm.GetEArw[rm].word());
                } else {
                    int eaa = getEaa(rm);
                    Modrm.Getrd[rm].dword = (short) Memory.mem_readw(eaa);
                }
                return HANDLED;
            }
        };
        ops[0x3c1] = new OP() {

            public final int call() {
                if (CPU.CPU_ArchitectureType < CPU.CPU_ARCHTYPE_486OLD) return ILLEGAL_OPCODE;
                short rm = Fetchb();
                if (rm >= 0xc0) {
                    int result = Instructions.ADDD(Modrm.Getrd[rm].dword, Modrm.GetEArd[rm].dword);
                    Modrm.Getrd[rm].dword = Modrm.GetEArd[rm].dword;
                    Modrm.GetEArd[rm].dword = result;
                } else {
                    int eaa = getEaa(rm);
                    int value = Memory.mem_readd(eaa);
                    int result = Instructions.ADDD(Modrm.Getrd[rm].dword, value);
                    Memory.mem_writed(eaa, result);
                    Modrm.Getrd[rm].dword = value;
                }
                return HANDLED;
            }
        };
        ops[0x3c8] = new OP() {

            public final int call() {
                if (CPU.CPU_ArchitectureType < CPU.CPU_ARCHTYPE_486OLD) return ILLEGAL_OPCODE;
                reg_eax.dword = BSWAPD(reg_eax.dword);
                return HANDLED;
            }
        };
        ops[0x3c9] = new OP() {

            public final int call() {
                if (CPU.CPU_ArchitectureType < CPU.CPU_ARCHTYPE_486OLD) return ILLEGAL_OPCODE;
                reg_ecx.dword = BSWAPD(reg_ecx.dword);
                return HANDLED;
            }
        };
        ops[0x3ca] = new OP() {

            public final int call() {
                if (CPU.CPU_ArchitectureType < CPU.CPU_ARCHTYPE_486OLD) return ILLEGAL_OPCODE;
                reg_edx.dword = BSWAPD(reg_edx.dword);
                return HANDLED;
            }
        };
        ops[0x3cb] = new OP() {

            public final int call() {
                if (CPU.CPU_ArchitectureType < CPU.CPU_ARCHTYPE_486OLD) return ILLEGAL_OPCODE;
                reg_ebx.dword = BSWAPD(reg_ebx.dword);
                return HANDLED;
            }
        };
        ops[0x3cc] = new OP() {

            public final int call() {
                if (CPU.CPU_ArchitectureType < CPU.CPU_ARCHTYPE_486OLD) return ILLEGAL_OPCODE;
                reg_esp.dword = BSWAPD(reg_esp.dword);
                return HANDLED;
            }
        };
        ops[0x3cd] = new OP() {

            public final int call() {
                if (CPU.CPU_ArchitectureType < CPU.CPU_ARCHTYPE_486OLD) return ILLEGAL_OPCODE;
                reg_ebp.dword = BSWAPD(reg_ebp.dword);
                return HANDLED;
            }
        };
        ops[0x3ce] = new OP() {

            public final int call() {
                if (CPU.CPU_ArchitectureType < CPU.CPU_ARCHTYPE_486OLD) return ILLEGAL_OPCODE;
                reg_esi.dword = BSWAPD(reg_esi.dword);
                return HANDLED;
            }
        };
        ops[0x3cf] = new OP() {

            public final int call() {
                if (CPU.CPU_ArchitectureType < CPU.CPU_ARCHTYPE_486OLD) return ILLEGAL_OPCODE;
                reg_edi.dword = BSWAPD(reg_edi.dword);
                return HANDLED;
            }
        };
    }
}
