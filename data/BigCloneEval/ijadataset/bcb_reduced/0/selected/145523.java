package org.binarytranslator.generic.memory;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import org.binarytranslator.DBT_Options;
import org.binarytranslator.generic.fault.SegmentationFault;

/**
 * ByteAddressedMemory:
 * 
 * Memory is arrays of bytes, no endian conversion is performed.
 * 
 * The string helo followed by the int of 0xcafebabe appear as:
 * 
 * <pre>
 * Byte Address|    
 * -----------------
 * .........07 | ca|
 * .........06 | fe|
 * .........05 | ba|
 * .........04 | be|
 * .........03 |'o'|
 * .........02 |'l'|
 * .........01 |'e'|
 * .........00 |'H'|
 * </pre>
 */
public class ByteAddressedBigEndianMemory extends CallBasedMemory {

    /** The size of a single page in bytes. */
    private static final int PAGE_SIZE = 4096;

    /** Bits in offset */
    private static final int OFFSET_BITS = 12;

    /** The number of pages */
    private static final int NUM_PAGES = 0x100000;

    /** The maximum amount of RAM available */
    protected static final long MAX_RAM = (long) PAGE_SIZE * (long) NUM_PAGES;

    /** The memory backing store */
    private byte readableMemory[][];

    private byte writableMemory[][];

    private byte executableMemory[][];

    /** Do we have more optimal nio mmap operation? */
    private boolean HAVE_java_nio_FileChannelImpl_nio_mmap_file = false;

    /**
   * Constructor - used when this is the instatiated class
   */
    public ByteAddressedBigEndianMemory() {
        this(null);
    }

    /**
   * Constructor - used when deriving a class
   * 
   * @param classType
   *          the name of the over-riding class
   */
    protected ByteAddressedBigEndianMemory(Class classType) {
        super(classType != null ? classType : ByteAddressedBigEndianMemory.class);
        readableMemory = new byte[NUM_PAGES][];
        writableMemory = new byte[NUM_PAGES][];
        executableMemory = new byte[NUM_PAGES][];
    }

    /**
   * Return the offset part of the address
   */
    private static final int getOffset(int address) {
        return address & (PAGE_SIZE - 1);
    }

    /**
   * Return the page table entry part of the address
   */
    private static final int getPTE(int address) {
        return address >>> OFFSET_BITS;
    }

    /**
   * Find free consecutive pages
   * 
   * @param pages
   *          the number of pages required
   * @return the address found
   */
    private final int findFreePages(int pages) {
        starting_page_search: for (int i = 0; i < NUM_PAGES; i++) {
            if (getPage(i) == null) {
                int start = i;
                int end = i + pages;
                for (; i <= end; i++) {
                    if (getPage(i) != null) {
                        continue starting_page_search;
                    }
                }
                return start << OFFSET_BITS;
            }
        }
        throw new Error("No mappable consecutive pages found for an anonymous map of size" + (pages * PAGE_SIZE));
    }

    /**
   * Map an anonymous page of memory
   * 
   * @param addr
   *          the address to map or NULL if don't care
   * @param len
   *          the amount of memory to map
   * @param read
   *          is the page readable
   * @param write
   *          is the page writable
   * @param exec
   *          is the page executable
   */
    public int map(int addr, int len, boolean read, boolean write, boolean exec) throws MemoryMapException {
        if ((addr % PAGE_SIZE) != 0) {
            MemoryMapException.unalignedAddress(addr);
        }
        int num_pages = (len + PAGE_SIZE - 1) / PAGE_SIZE;
        byte pages[][] = new byte[num_pages][PAGE_SIZE];
        if (addr == 0) {
            addr = findFreePages(num_pages);
        }
        if (DBT_Options.debugMemory) {
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

    /**
   * Map a page of memory from file
   * 
   * @param file
   *          the file map in from
   * @param addr
   *          the address to map or NULL if don't care
   * @param len
   *          the amount of memory to map
   * @param read
   *          is the page readable
   * @param write
   *          is the page writable
   * @param exec
   *          is the page executable
   */
    public int map(RandomAccessFile file, long offset, int addr, int len, boolean read, boolean write, boolean exec) throws MemoryMapException {
        if ((addr % PAGE_SIZE) != 0) {
            MemoryMapException.unalignedAddress(addr);
        }
        int num_pages = (len + PAGE_SIZE - 1) / PAGE_SIZE;
        if (addr == 0) {
            addr = findFreePages(num_pages);
        }
        if (DBT_Options.debugMemory) {
            System.out.println("Mapping file " + file + " offset=" + offset + " addr=0x" + Integer.toHexString(addr) + " len=" + len + (read ? " r" : " -") + (write ? "w" : "-") + (exec ? "x" : "-"));
        }
        try {
            int pte = getPTE(addr);
            if (!HAVE_java_nio_FileChannelImpl_nio_mmap_file) {
                file.seek(offset);
                for (int i = 0; i < num_pages; i++) {
                    if (getPage(pte + i) != null) {
                        throw new Error("Memory map of already mapped location addr=0x" + Integer.toHexString(addr) + " len=" + len);
                    }
                    byte page[] = new byte[PAGE_SIZE];
                    if (i == 0) {
                        file.read(page, getOffset(addr), PAGE_SIZE - getOffset(addr));
                    } else if (i == (num_pages - 1)) {
                        file.read(page, 0, ((len - getOffset(addr)) % PAGE_SIZE));
                    } else {
                        file.read(page);
                    }
                    readableMemory[pte + i] = read ? page : null;
                    writableMemory[pte + i] = write ? page : null;
                    executableMemory[pte + i] = exec ? page : null;
                }
            } else {
                for (int i = 0; i < num_pages; i++) {
                    if (getPage(pte + i) != null) {
                        throw new Error("Memory map of already mapped location addr=0x" + Integer.toHexString(addr) + " len=" + len);
                    }
                    if (read && write) {
                        readableMemory[pte + i] = file.getChannel().map(FileChannel.MapMode.READ_WRITE, offset + (i * PAGE_SIZE), PAGE_SIZE).array();
                        writableMemory[pte + i] = readableMemory[pte + i];
                        if (exec) {
                            executableMemory[pte + i] = readableMemory[pte + i];
                        }
                    } else if (read) {
                        readableMemory[pte + i] = file.getChannel().map(FileChannel.MapMode.READ_ONLY, offset + (i * PAGE_SIZE), PAGE_SIZE).array();
                        if (exec) {
                            executableMemory[pte + i] = readableMemory[pte + i];
                        }
                    } else if (exec) {
                        executableMemory[pte + i] = file.getChannel().map(FileChannel.MapMode.READ_ONLY, offset + (i * PAGE_SIZE), PAGE_SIZE).array();
                    } else {
                        throw new Error("Unable to map address 0x" + Integer.toHexString(addr) + " with permissions " + (read ? "r" : "-") + (write ? "w" : "-") + (exec ? "x" : "-"));
                    }
                }
            }
            return addr;
        } catch (java.io.IOException e) {
            throw new Error(e);
        }
    }

    /**
   * Returns the page currently mapped at the given page table entry.
   * 
   * @param pte
   *  The page table entry, for which a page is to be retrieved.
   * @return
   *  The page mapped at the given page table entry or null, if no page is currently mapped
   *  to that entry.
   */
    private byte[] getPage(int pte) {
        if (readableMemory[pte] != null) return readableMemory[pte];
        if (writableMemory[pte] != null) return writableMemory[pte];
        if (executableMemory[pte] != null) return executableMemory[pte];
        return null;
    }

    /**
   * Unmap a page of memory
   * 
   * @param addr
   *          the address to unmap
   * @param len
   *          the amount of memory to unmap
   */
    public void unmap(int addr, int len) {
        for (int i = 0; i < len; i += PAGE_SIZE) {
            int pte = getPTE(addr + i);
            if (getPage(pte) != null) {
                readableMemory[pte] = null;
                writableMemory[pte] = null;
                executableMemory[pte] = null;
            } else {
                throw new Error("Unmapping memory that's not mapped addr=0x" + Integer.toHexString(addr) + " len=" + len);
            }
        }
    }

    /**
   * Is the given address mapped into memory?
   * @param addr to check
   * @return true => memory is mapped
   */
    public boolean isMapped(int addr) {
        return getPage(getPTE(addr)) != null;
    }

    /**
   * @return the size of a page
   */
    public int getPageSize() {
        return PAGE_SIZE;
    }

    /**
   * Is the given address aligned on a page boundary?
   * 
   * @param addr
   *          the address to check
   * @return whether the address is aligned
   */
    public boolean isPageAligned(int addr) {
        return (addr % PAGE_SIZE) == 0;
    }

    /**
   * Make the given address page aligned to the page beneath it
   * 
   * @param addr
   *          the address to truncate
   * @return the truncated address
   */
    public int truncateToPage(int addr) {
        return (addr >> OFFSET_BITS) << OFFSET_BITS;
    }

    /**
   * Make the given address page aligned to the page above it
   * 
   * @param addr
   *          the address to truncate
   * @return the truncated address
   */
    public int truncateToNextPage(int addr) {
        return ((addr + PAGE_SIZE - 1) >> OFFSET_BITS) << OFFSET_BITS;
    }

    /**
   * Perform a byte load where the sign extended result fills the return value
   * 
   * @param addr
   *          the address of the value to load
   * @return the sign extended result
   */
    public final int loadSigned8(int addr) {
        try {
            if (DBT_Options.debugMemory) System.err.println("LoadS8 address: 0x" + Integer.toHexString(addr) + " val: " + readableMemory[getPTE(addr)][getOffset(addr)]);
            return readableMemory[getPTE(addr)][getOffset(addr)];
        } catch (NullPointerException e) {
            System.err.println("Null pointer exception at address: 0x" + Integer.toHexString(addr));
            throw e;
        }
    }

    /**
   * Perform a byte load where the zero extended result fills the return value
   * 
   * @param addr
   *          the address of the value to load
   * @return the zero extended result
   */
    public final int loadUnsigned8(int addr) {
        try {
            if (DBT_Options.debugMemory) System.err.println("LoadU8 address: 0x" + Integer.toHexString(addr) + " val: " + readableMemory[getPTE(addr)][getOffset(addr)]);
            return readableMemory[getPTE(addr)][getOffset(addr)] & 0xFF;
        } catch (NullPointerException e) {
            System.err.println("Null pointer exception at address: 0x" + Integer.toHexString(addr));
            throw e;
        }
    }

    /**
   * Perform a 16bit load where the sign extended result fills the return value
   * 
   * @param addr
   *          the address of the value to load
   * @return the sign extended result
   */
    public int loadSigned16(int addr) {
        return (loadSigned8(addr) << 8) | loadUnsigned8(addr + 1);
    }

    /**
   * Perform a 16bit load where the zero extended result fills the return value
   * 
   * @param addr
   *          the address of the value to load
   * @return the zero extended result
   */
    public int loadUnsigned16(int addr) {
        return (loadUnsigned8(addr) << 8) | loadUnsigned8(addr + 1);
    }

    /**
   * Perform a 32bit load
   * 
   * @param addr
   *          the address of the value to load
   * @return the result
   */
    public int load32(int addr) {
        try {
            return (loadSigned8(addr) << 24) | (loadUnsigned8(addr + 1) << 16) | (loadUnsigned8(addr + 2) << 8) | loadUnsigned8(addr + 3);
        } catch (Exception e) {
            throw new SegmentationFault(addr);
        }
    }

    /**
   * Perform a 8bit load from memory that must be executable
   * 
   * @param addr
   *          the address of the value to load
   * @return the result
   */
    public int loadInstruction8(int addr) {
        if (DBT_Options.debugMemory) System.err.println("LoadI8 address: 0x" + Integer.toHexString(addr) + " val: " + executableMemory[getPTE(addr)][getOffset(addr)]);
        return executableMemory[getPTE(addr)][getOffset(addr)] & 0xFF;
    }

    /**
   * Perform a 32bit load from memory that must be executable
   * 
   * @param addr
   *          the address of the value to load
   * @return the result
   */
    public int loadInstruction32(int addr) {
        return (loadInstruction8(addr) << 24) | (loadInstruction8(addr + 1) << 16) | (loadInstruction8(addr + 2) << 8) | loadInstruction8(addr + 3);
    }

    /**
   * Perform a byte store
   * 
   * @param value
   *          the value to store
   * @param addr
   *          the address of where to store
   */
    public final void store8(int addr, int value) {
        if (DBT_Options.debugMemory) System.err.println("Store8 address: 0x" + Integer.toHexString(addr) + " val: 0x" + Integer.toHexString(value & 0xFF));
        writableMemory[getPTE(addr)][getOffset(addr)] = (byte) value;
    }

    /**
   * Perform a 16bit store
   * 
   * @param value
   *          the value to store
   * @param addr
   *          the address of where to store
   */
    public void store16(int addr, int value) {
        store8(addr, value >> 8);
        store8(addr + 1, value);
    }

    /**
   * Perform a 32bit store
   * 
   * @param value
   *          the value to store
   * @param addr
   *          the address of where to store
   */
    public void store32(int addr, int value) {
        try {
            store8(addr, value >> 24);
            store8(addr + 1, value >> 16);
            store8(addr + 2, value >> 8);
            store8(addr + 3, value);
        } catch (Exception e) {
            throw new SegmentationFault(addr);
        }
    }

    @Override
    public void changeProtection(int address, int len, boolean newRead, boolean newWrite, boolean newExec) {
        while (len > 0) {
            int pte = getPTE(address);
            byte[] page = getPage(pte);
            if (page == null) throw new SegmentationFault(address);
            readableMemory[pte] = newRead ? page : null;
            writableMemory[pte] = newWrite ? page : null;
            executableMemory[pte] = newExec ? page : null;
            address += PAGE_SIZE;
            len -= PAGE_SIZE;
        }
    }

    @Override
    public int loadInstruction16(int addr) {
        return (loadInstruction8(addr) << 8) | loadInstruction8(addr + 1);
    }
}
