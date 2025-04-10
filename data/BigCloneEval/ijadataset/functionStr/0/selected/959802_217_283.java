public class Test {        public static void doString() {
            int add_index = CPU.cpu.direction << 2;
            int count = CPU_Regs.reg_ecx.dword;
            int si_base = base_ds;
            int di_base = CPU.Segs_ESphys;
            if (Config.FAST_STRINGS) {
                while (count > 1) {
                    int dst = di_base + reg_edi.dword;
                    int src = si_base + reg_esi.dword;
                    int dst_index = Paging.getDirectIndex(dst);
                    int src_index = Paging.getDirectIndexRO(src);
                    int src_len;
                    int dst_len;
                    if (dst_index < 0 || src_index < 0) {
                        break;
                    }
                    int len = count << 2;
                    if (add_index < 0) {
                        src_len = (src & 0xFFF) + 1;
                        dst_len = (dst & 0xFFF) + 1;
                    } else {
                        src_len = 0x1000 - (src & 0xFFF);
                        dst_len = 0x1000 - (dst & 0xFFF);
                    }
                    if (len > src_len) len = src_len;
                    if (len > dst_len) len = dst_len;
                    len = len & ~3;
                    if (len <= 0) {
                        Memory.mem_writed(di_base + reg_edi.dword, Memory.mem_readd(si_base + reg_esi.dword));
                        reg_edi.dword += add_index;
                        reg_esi.dword += add_index;
                        reg_ecx.dword--;
                        count--;
                    } else {
                        int thisCount = (len >> 2);
                        if (Math.abs(src_index - dst_index) < len) {
                            for (int i = 0; i < thisCount; i++) {
                                Memory.host_writed(dst_index, Memory.host_readd(src_index));
                                dst_index += add_index;
                                src_index += add_index;
                            }
                        } else {
                            if (add_index < 0) {
                                src_index -= len - 4;
                                dst_index -= len - 4;
                            }
                            Memory.host_memcpy(dst_index, src_index, len);
                        }
                        if (add_index < 0) {
                            reg_edi.dword -= len;
                            reg_esi.dword -= len;
                        } else {
                            reg_edi.dword += len;
                            reg_esi.dword += len;
                        }
                        reg_ecx.dword -= thisCount;
                        count -= thisCount;
                    }
                }
            }
            for (; count > 0; count--) {
                Memory.mem_writed(di_base + reg_edi.dword, Memory.mem_readd(si_base + reg_esi.dword));
                reg_edi.dword += add_index;
                reg_esi.dword += add_index;
                reg_ecx.dword--;
            }
        }
}