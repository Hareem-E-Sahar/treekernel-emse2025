public class Test {        public static void doString() {
            int add_index = CPU.cpu.direction;
            int si_base = base_ds;
            int di_base = CPU.Segs_ESphys;
            Memory.mem_writeb(di_base + reg_edi.word(), Memory.mem_readb(si_base + reg_esi.word()));
            reg_edi.word(reg_edi.word() + add_index);
            reg_esi.word(reg_esi.word() + add_index);
        }
}