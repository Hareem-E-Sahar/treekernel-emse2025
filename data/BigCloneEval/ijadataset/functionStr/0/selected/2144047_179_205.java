public class Test {    public int map(int addr, int len, boolean read, boolean write, boolean exec) throws MemoryMapException {
        if ((addr % PAGE_SIZE) != 0) {
            int validPageCount = addr / PAGE_SIZE;
            int oldStartAddress = addr;
            addr = validPageCount * PAGE_SIZE;
            if (DBT.VerifyAssertions) DBT._assert(oldStartAddress > addr);
            len += (oldStartAddress - addr);
        }
        int num_pages = (len + PAGE_SIZE - 1) / PAGE_SIZE;
        int pages[][] = new int[num_pages][PAGE_SIZE / 4];
        if (addr == 0) {
            addr = findFreePages(num_pages);
        }
        if (DBT_Options.debugRuntime) {
            System.out.println("Anonymous mapping: addr=0x" + Integer.toHexString(addr) + " len=" + len + (read ? " r" : " -") + (write ? "w" : "-") + (exec ? "x" : "-"));
        }
        int pte = getPTE(addr);
        for (int i = 0; i < num_pages; i++) {
            if (getPage(pte + i) != null) {
                throw new Error("Memory map of already mapped location addr=0x" + Integer.toHexString(addr) + " len=" + len);
            }
            readableMemory[pte + i] = read ? pages[i] : null;
            writableMemory[pte + i] = write ? pages[i] : null;
            executableMemory[pte + i] = exec ? pages[i] : null;
        }
        return addr;
    }
}