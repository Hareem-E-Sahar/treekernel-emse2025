package jpcsp;

import static jpcsp.format.Elf32SectionHeader.SHF_ALLOCATE;
import static jpcsp.format.Elf32SectionHeader.SHF_EXECUTE;
import static jpcsp.format.Elf32SectionHeader.SHF_NONE;
import static jpcsp.format.Elf32SectionHeader.SHF_WRITE;
import static jpcsp.util.Utilities.readUnaligned32;
import static jpcsp.util.Utilities.writeUnaligned32;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import jpcsp.Debugger.ElfHeaderInfo;
import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.Managers;
import jpcsp.HLE.kernel.types.SceModule;
import jpcsp.HLE.modules.SysMemUserForUser;
import jpcsp.HLE.modules150.SysMemUserForUser.SysMemInfo;
import jpcsp.format.DeferredStub;
import jpcsp.format.Elf32;
import jpcsp.format.Elf32EntHeader;
import jpcsp.format.Elf32ProgramHeader;
import jpcsp.format.Elf32Relocate;
import jpcsp.format.Elf32SectionHeader;
import jpcsp.format.Elf32StubHeader;
import jpcsp.format.PBP;
import jpcsp.format.PSF;
import jpcsp.format.PSP;
import jpcsp.format.PSPModuleInfo;
import jpcsp.memory.IMemoryReader;
import jpcsp.memory.MemoryReader;
import jpcsp.memory.MemorySection;
import jpcsp.memory.MemorySections;
import jpcsp.settings.Settings;
import jpcsp.util.Utilities;

public class Loader {

    private static Loader instance;

    private boolean loadedFirstModule;

    private static Logger log = Logger.getLogger("loader");

    public static final int SCE_MAGIC = 0x4543537E;

    public static final int PSP_MAGIC = 0x50535000;

    public static final int EDAT_MAGIC = 0x54414445;

    private static final int FIRMWAREVERSION_HOMEBREW = 999;

    public static final int FORMAT_UNKNOWN = 0x00;

    public static final int FORMAT_ELF = 0x01;

    public static final int FORMAT_PRX = 0x02;

    public static final int FORMAT_PBP = 0x04;

    public static final int FORMAT_SCE = 0x08;

    public static final int FORMAT_PSP = 0x10;

    public static Loader getInstance() {
        if (instance == null) instance = new Loader();
        return instance;
    }

    private Loader() {
    }

    public void reset() {
        loadedFirstModule = false;
    }

    /**
     * @param pspfilename   Example:
     *                      ms0:/PSP/GAME/xxx/EBOOT.PBP
     *                      disc0:/PSP_GAME/SYSDIR/BOOT.BIN
     *                      disc0:/PSP_GAME/SYSDIR/EBOOT.BIN
     *                      xxx:/yyy/zzz.prx
     * @param f             the module file contents
     * @param baseAddress   should be at least 64-byte aligned,
     *                      or how ever much is the default alignment in pspsysmem.
     * @param analyzeOnly   true, if the module is not really loaded, but only
     *                            the SceModule object is returned;
     *                      false, if the module is really loaded in memory.
     * @return              Always a SceModule object, you should check the
     *                      fileFormat member against the FORMAT_* bits.
     *                      Example: (fileFormat & FORMAT_ELF) == FORMAT_ELF
     **/
    public SceModule LoadModule(String pspfilename, ByteBuffer f, int baseAddress, boolean analyzeOnly) throws IOException {
        SceModule module = new SceModule(false);
        int currentOffset = f.position();
        module.fileFormat = FORMAT_UNKNOWN;
        module.pspfilename = pspfilename;
        if (f.capacity() - f.position() == 0) {
            log.error("LoadModule: no data.");
            return module;
        }
        do {
            f.position(currentOffset);
            if (LoadPBP(f, module, baseAddress, analyzeOnly)) {
                currentOffset = f.position();
                if (currentOffset == f.limit()) break;
            } else if (!loadedFirstModule) {
                loadPSF(module, analyzeOnly);
            }
            if (module.psf != null) {
                log.info("PBP meta data :\n" + module.psf);
                if (!loadedFirstModule) {
                    if (module.psf.isLikelyHomebrew()) {
                        Emulator.getInstance().setFirmwareVersion(FIRMWAREVERSION_HOMEBREW);
                    } else {
                        Emulator.getInstance().setFirmwareVersion(module.psf.getString("PSP_SYSTEM_VER"));
                    }
                    Modules.SysMemUserForUserModule.setMemory64MB(module.psf.getNumeric("MEMSIZE") == 1);
                }
            }
            f.position(currentOffset);
            if (LoadSPRX(f, module, baseAddress, analyzeOnly)) break;
            f.position(currentOffset);
            if (LoadSCE(f, module, baseAddress, analyzeOnly)) break;
            f.position(currentOffset);
            if (LoadPSP(f, module, baseAddress, analyzeOnly)) break;
            f.position(currentOffset);
            if (LoadELF(f, module, baseAddress, analyzeOnly)) break;
            f.position(currentOffset);
            LoadUNK(f, module, baseAddress, analyzeOnly);
        } while (false);
        return module;
    }

    private void loadPSF(SceModule module, boolean analyzeOnly) {
        if (module.psf != null) return;
        String filetoload = module.pspfilename;
        if (filetoload.startsWith("ms0:")) filetoload = filetoload.replace("ms0:", "ms0");
        File metapbp = null;
        File pbpfile = new File(filetoload);
        if (pbpfile.getParentFile() == null || pbpfile.getParentFile().getParentFile() == null) {
            return;
        }
        File metadir = new File(pbpfile.getParentFile().getParentFile().getPath() + File.separatorChar + "%" + pbpfile.getParentFile().getName());
        if (metadir.exists()) {
            File[] eboot = metadir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File arg0) {
                    return arg0.getName().equalsIgnoreCase("eboot.pbp");
                }
            });
            if (eboot.length > 0) metapbp = eboot[0];
        }
        metadir = new File(pbpfile.getParentFile().getParentFile().getPath() + File.separatorChar + pbpfile.getParentFile().getName() + "%");
        if (metadir.exists()) {
            File[] eboot = metadir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File arg0) {
                    return arg0.getName().equalsIgnoreCase("eboot.pbp");
                }
            });
            if (eboot.length > 0) metapbp = eboot[0];
        }
        if (metapbp != null) {
            FileChannel roChannel;
            try {
                roChannel = new RandomAccessFile(metapbp, "r").getChannel();
                ByteBuffer readbuffer = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) roChannel.size());
                PBP meta = new PBP(readbuffer);
                module.psf = meta.readPSF(readbuffer);
                roChannel.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File[] psffile = pbpfile.getParentFile().listFiles(new FileFilter() {

                @Override
                public boolean accept(File arg0) {
                    return arg0.getName().equalsIgnoreCase("param.sfo");
                }
            });
            if (psffile != null && psffile.length > 0) {
                try {
                    FileChannel roChannel = new RandomAccessFile(psffile[0], "r").getChannel();
                    ByteBuffer readbuffer = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) roChannel.size());
                    module.psf = new PSF();
                    module.psf.read(readbuffer);
                    roChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** @return true on success */
    private boolean LoadPBP(ByteBuffer f, SceModule module, int baseAddress, boolean analyzeOnly) throws IOException {
        PBP pbp = new PBP(f);
        if (pbp.isValid()) {
            module.fileFormat |= FORMAT_PBP;
            if (pbp.getOffsetParam() > 0) {
                module.psf = pbp.readPSF(f);
            }
            if (Settings.getInstance().readBool("emu.pbpunpack")) {
                PBP.unpackPBP(f);
            }
            ElfHeaderInfo.PbpInfo = pbp.toString();
            f.position((int) pbp.getOffsetPspData());
            return true;
        }
        return false;
    }

    /** @return true on success */
    private boolean LoadSPRX(ByteBuffer f, SceModule module, int baseAddress, boolean analyzeOnly) throws IOException {
        int magicPSP = Utilities.readWord(f);
        int magicEDAT = Utilities.readWord(f);
        if ((magicPSP == PSP_MAGIC) && (magicEDAT == EDAT_MAGIC)) {
            log.warn("Encrypted file detected! (.PSPEDAT)");
            f.position(0x90);
            LoadPSP(f.slice(), module, baseAddress, analyzeOnly);
            return true;
        }
        return false;
    }

    /** @return true on success */
    private boolean LoadSCE(ByteBuffer f, SceModule module, int baseAddress, boolean analyzeOnly) throws IOException {
        int magic = Utilities.readWord(f);
        if (magic == SCE_MAGIC) {
            module.fileFormat |= FORMAT_SCE;
            log.warn("Encrypted file not supported! (~SCE)");
            return true;
        }
        return false;
    }

    /** @return true on success */
    private boolean LoadPSP(ByteBuffer f, SceModule module, int baseAddress, boolean analyzeOnly) throws IOException {
        PSP psp = new PSP(f);
        if (psp.isValid()) {
            module.fileFormat |= FORMAT_PSP;
            log.warn("Encrypted file detected! (~PSP)");
            if (!loadedFirstModule) {
                log.info("Calling crypto engine for PRX.");
                LoadELF(psp.decrypt(f), module, baseAddress, analyzeOnly);
            }
            return true;
        }
        return false;
    }

    /** @return true on success */
    private boolean LoadELF(ByteBuffer f, SceModule module, int baseAddress, boolean analyzeOnly) throws IOException {
        int elfOffset = f.position();
        Elf32 elf = new Elf32(f);
        if (elf.getHeader().isValid()) {
            module.fileFormat |= FORMAT_ELF;
            if (!elf.getHeader().isMIPSExecutable()) {
                log.error("Loader NOT a MIPS executable");
                return false;
            }
            if (elf.getHeader().isPRXDetected()) {
                log.debug("Loader: Relocation required (PRX)");
                module.fileFormat |= FORMAT_PRX;
            } else if (elf.getHeader().requiresRelocation()) {
                log.info("Loader: Relocation required (ELF)");
            } else {
                if (baseAddress > 0x08900000) log.warn("Loader: Probably trying to load PBP ELF while another PBP ELF is already loaded");
                baseAddress = 0;
            }
            module.baseAddress = baseAddress;
            if (elf.getHeader().getE_entry() == 0xFFFFFFFFL) {
                module.entry_addr = -1;
            } else {
                module.entry_addr = baseAddress + (int) elf.getHeader().getE_entry();
            }
            module.loadAddressLow = (baseAddress != 0) ? baseAddress : MemoryMap.END_USERSPACE;
            module.loadAddressHigh = baseAddress;
            LoadELFProgram(f, module, baseAddress, elf, elfOffset, analyzeOnly);
            LoadELFSections(f, module, baseAddress, elf, elfOffset, analyzeOnly);
            if (module.loadAddressLow > module.loadAddressHigh) {
                log.error(String.format("Incorrect ELF module address: loadAddressLow=0x%08X, loadAddressHigh=0x%08X", module.loadAddressLow, module.loadAddressHigh));
                module.loadAddressHigh = module.loadAddressLow;
            }
            if (!analyzeOnly) {
                if (elf.getHeader().requiresRelocation()) {
                    relocateFromHeaders(f, module, baseAddress, elf, elfOffset);
                }
                LoadELFModuleInfo(f, module, baseAddress, elf, elfOffset);
                LoadELFReserveMemory(module);
                LoadELFImports(module);
                LoadELFExports(module);
                Managers.modules.addModule(module);
                ProcessUnresolvedImports();
                LoadELFDebuggerInfo(f, module, baseAddress, elf, elfOffset);
                module.write(Memory.getInstance(), module.address);
                loadedFirstModule = true;
            }
            return true;
        }
        log.debug("Loader: Not a ELF");
        return false;
    }

    /** Dummy loader for unrecognized file formats, put at the end of a loader chain.
     * @return true on success */
    private boolean LoadUNK(ByteBuffer f, SceModule module, int baseAddress, boolean analyzeOnly) throws IOException {
        byte m0 = f.get();
        byte m1 = f.get();
        byte m2 = f.get();
        byte m3 = f.get();
        if (m0 == 0x43 && m1 == 0x49 && m2 == 0x53 && m3 == 0x4F) {
            log.info("This is not an executable file!");
            log.info("Try using the Load UMD menu item");
        } else if ((m0 == 0 && m1 == 0x50 && m2 == 0x53 && m3 == 0x46)) {
            log.info("This is not an executable file!");
        } else {
            boolean handled = false;
            if (f.limit() >= 16 * 2048 + 6) {
                f.position(16 * 2048);
                byte[] id = new byte[6];
                f.get(id);
                if ((((char) id[1]) == 'C') && (((char) id[2]) == 'D') && (((char) id[3]) == '0') && (((char) id[4]) == '0') && (((char) id[5]) == '1')) {
                    log.info("This is not an executable file!");
                    log.info("Try using the Load UMD menu item");
                    handled = true;
                }
            }
            if (!handled) {
                log.info("Unrecognized file format");
                log.info(String.format("File magic %02X %02X %02X %02X", m0, m1, m2, m3));
            }
        }
        return false;
    }

    /** Load some programs into memory */
    private void LoadELFProgram(ByteBuffer f, SceModule module, int baseAddress, Elf32 elf, int elfOffset, boolean analyzeOnly) throws IOException {
        List<Elf32ProgramHeader> programHeaderList = elf.getProgramHeaderList();
        Memory mem = Memory.getInstance();
        int i = 0;
        module.bss_size = 0;
        for (Elf32ProgramHeader phdr : programHeaderList) {
            if (log.isTraceEnabled()) {
                log.trace(String.format("ELF Program Header: %s", phdr.toString()));
            }
            if (phdr.getP_type() == 0x00000001L) {
                int fileOffset = (int) phdr.getP_offset();
                int memOffset = baseAddress + (int) phdr.getP_vaddr();
                if (!Memory.isAddressGood(memOffset)) {
                    memOffset = (int) phdr.getP_vaddr();
                    if (!Memory.isAddressGood(memOffset)) {
                        log.warn(String.format("Program header has invalid memory offset 0x%08X!", memOffset));
                    }
                }
                int fileLen = (int) phdr.getP_filesz();
                int memLen = (int) phdr.getP_memsz();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("PH#%d: loading program %08X - %08X - %08X", i, memOffset, memOffset + fileLen, memOffset + memLen));
                }
                f.position(elfOffset + fileOffset);
                if (f.position() + fileLen > f.limit()) {
                    int newLen = f.limit() - f.position();
                    log.warn(String.format("PH#%d: program overflow clamping len %08X to %08X", i, fileLen, newLen));
                    fileLen = newLen;
                }
                if (!analyzeOnly) {
                    mem.copyToMemory(memOffset, f, fileLen);
                }
                if (memOffset < module.loadAddressLow) {
                    module.loadAddressLow = memOffset;
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("PH#%d: new loadAddressLow %08X", i, module.loadAddressLow));
                    }
                }
                if (memOffset + memLen > module.loadAddressHigh) {
                    module.loadAddressHigh = memOffset + memLen;
                    if (log.isTraceEnabled()) {
                        log.trace(String.format("PH#%d: new loadAddressHigh %08X", i, module.loadAddressHigh));
                    }
                }
                if (log.isTraceEnabled()) {
                    log.trace(String.format("PH#%d: contributes %08X to bss size", i, (int) (phdr.getP_memsz() - phdr.getP_filesz())));
                }
                module.bss_size += (int) (phdr.getP_memsz() - phdr.getP_filesz());
            }
            i++;
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("PH alloc consumption %08X (mem %08X)", (module.loadAddressHigh - module.loadAddressLow), module.bss_size));
        }
    }

    /** Load some sections into memory */
    private void LoadELFSections(ByteBuffer f, SceModule module, int baseAddress, Elf32 elf, int elfOffset, boolean analyzeOnly) throws IOException {
        List<Elf32SectionHeader> sectionHeaderList = elf.getSectionHeaderList();
        Memory mem = Memory.getInstance();
        for (Elf32SectionHeader shdr : sectionHeaderList) {
            if (log.isTraceEnabled()) {
                log.trace(String.format("ELF Section Header: %s", shdr.toString()));
            }
            int memOffset = baseAddress + (int) shdr.getSh_addr();
            if (!Memory.isAddressGood(memOffset)) {
                memOffset = (int) shdr.getSh_addr();
            }
            int len = (int) shdr.getSh_size();
            int flags = shdr.getSh_flags();
            if (flags != SHF_NONE && Memory.isAddressGood(memOffset)) {
                boolean read = (flags & SHF_ALLOCATE) != 0;
                boolean write = (flags & SHF_WRITE) != 0;
                boolean execute = (flags & SHF_EXECUTE) != 0;
                MemorySection memorySection = new MemorySection(memOffset, len, read, write, execute);
                MemorySections.getInstance().addMemorySection(memorySection);
            }
            if ((flags & SHF_ALLOCATE) != 0) {
                switch(shdr.getSh_type()) {
                    case Elf32SectionHeader.SHT_PROGBITS:
                        {
                            if (!Memory.isAddressGood(memOffset)) {
                                log.warn(String.format("Section header (type 1) has invalid memory offset 0x%08X!", memOffset));
                            }
                            if (memOffset < module.loadAddressLow) {
                                log.warn(String.format("%s: section allocates more than program %08X - %08X", shdr.getSh_namez(), memOffset, (memOffset + len)));
                                module.loadAddressLow = memOffset;
                            }
                            if (memOffset + len > module.loadAddressHigh) {
                                log.warn(String.format("%s: section allocates more than program %08X - %08X", shdr.getSh_namez(), memOffset, (memOffset + len)));
                                module.loadAddressHigh = memOffset + len;
                            }
                            break;
                        }
                    case Elf32SectionHeader.SHT_NOBITS:
                        {
                            if (!Memory.isAddressGood(memOffset)) {
                                log.warn(String.format("Section header (type 8) has invalid memory offset 0x%08X!", memOffset));
                            }
                            if (len == 0) {
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("%s: ignoring zero-length type 8 section %08X", shdr.getSh_namez(), memOffset));
                                }
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("%s: clearing section %08X - %08X (len %08X)", shdr.getSh_namez(), memOffset, (memOffset + len), len));
                                }
                                if (!analyzeOnly) {
                                    mem.memset(memOffset, (byte) 0x0, len);
                                }
                                if (memOffset < module.loadAddressLow) {
                                    module.loadAddressLow = memOffset;
                                    if (log.isDebugEnabled()) {
                                        log.debug(String.format("%s: new loadAddressLow %08X (+%08X)", shdr.getSh_namez(), module.loadAddressLow, len));
                                    }
                                }
                                if (memOffset + len > module.loadAddressHigh) {
                                    module.loadAddressHigh = memOffset + len;
                                    if (log.isDebugEnabled()) {
                                        log.debug(String.format("%s: new loadAddressHigh %08X (+%08X)", shdr.getSh_namez(), module.loadAddressHigh, len));
                                    }
                                }
                            }
                            break;
                        }
                }
            }
        }
        Elf32SectionHeader shdr = elf.getSectionHeader(".text");
        if (shdr != null) {
            if (log.isTraceEnabled()) {
                log.trace(String.format("SH: Storing text size %08X %d", shdr.getSh_size(), shdr.getSh_size()));
            }
            module.text_addr = (int) (baseAddress + shdr.getSh_addr());
            module.text_size = (int) shdr.getSh_size();
        }
        shdr = elf.getSectionHeader(".data");
        if (shdr != null) {
            if (log.isTraceEnabled()) {
                log.trace(String.format("SH: Storing data size %08X %d", shdr.getSh_size(), shdr.getSh_size()));
            }
            module.data_size = (int) shdr.getSh_size();
        }
        shdr = elf.getSectionHeader(".bss");
        if (shdr != null && shdr.getSh_size() != 0) {
            if (log.isTraceEnabled()) {
                log.trace(String.format("SH: Storing bss size %08X %d", shdr.getSh_size(), shdr.getSh_size()));
            }
            if (module.bss_size == (int) shdr.getSh_size()) {
                if (log.isTraceEnabled()) {
                    log.trace("SH: Same bss size already set");
                }
            } else if (module.bss_size > (int) shdr.getSh_size()) {
                if (log.isTraceEnabled()) {
                    log.trace(String.format("SH: Larger bss size already set (%08X > %08X)", module.bss_size, shdr.getSh_size()));
                }
            } else if (module.bss_size != 0) {
                log.warn(String.format("SH: Overwriting bss size %08X with %08X", module.bss_size, shdr.getSh_size()));
                module.bss_size = (int) shdr.getSh_size();
            } else {
                log.info("SH: bss size not already set");
                module.bss_size = (int) shdr.getSh_size();
            }
        }
        module.nsegment += 1;
        module.segmentaddr[0] = module.loadAddressLow;
        module.segmentsize[0] = module.loadAddressHigh - module.loadAddressLow;
    }

    private void LoadELFReserveMemory(SceModule module) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Reserving 0x%X bytes at 0x%08X for module '%s'", module.loadAddressHigh - module.loadAddressLow, module.loadAddressLow, module.pspfilename));
        }
        int address = module.loadAddressLow & ~(SysMemUserForUser.defaultSizeAlignment - 1);
        int size = module.loadAddressHigh - address;
        SysMemInfo info = Modules.SysMemUserForUserModule.malloc(SysMemUserForUser.USER_PARTITION_ID, module.modname, SysMemUserForUser.PSP_SMEM_Addr, size, address);
        if (info == null || info.addr != address) {
            log.warn(String.format("Failed to properly reserve memory consumed by module %s at address 0x%08X, size 0x%X: allocated %s", module.modname, address, size, info));
        }
        module.addAllocatedMemory(info);
    }

    /** Loads from memory */
    private void LoadELFModuleInfo(ByteBuffer f, SceModule module, int baseAddress, Elf32 elf, int elfOffset) throws IOException {
        Elf32ProgramHeader phdr = elf.getProgramHeader(0);
        Elf32SectionHeader shdr = elf.getSectionHeader(".rodata.sceModuleInfo");
        if (!elf.getHeader().isPRXDetected() && shdr == null) {
            log.warn("ELF is not PRX, but has no section headers!");
            int memOffset = (int) (phdr.getP_vaddr() + (phdr.getP_paddr() & 0x7FFFFFFFL) - phdr.getP_offset());
            log.warn("Manually locating ModuleInfo at address: 0x" + Integer.toHexString(memOffset));
            PSPModuleInfo moduleInfo = new PSPModuleInfo();
            moduleInfo.read(Memory.getInstance(), memOffset);
            module.copy(moduleInfo);
        } else if (elf.getHeader().isPRXDetected()) {
            int memOffset = (int) (baseAddress + (phdr.getP_paddr() & 0x7FFFFFFFL) - phdr.getP_offset());
            PSPModuleInfo moduleInfo = new PSPModuleInfo();
            moduleInfo.read(Memory.getInstance(), memOffset);
            module.copy(moduleInfo);
        } else if (shdr != null) {
            int memOffset = (int) (baseAddress + shdr.getSh_addr());
            PSPModuleInfo moduleInfo = new PSPModuleInfo();
            moduleInfo.read(Memory.getInstance(), memOffset);
            module.copy(moduleInfo);
        } else {
            log.error("ModuleInfo not found!");
            return;
        }
        log.info("Found ModuleInfo name:'" + module.modname + "' version:" + String.format("%02x%02x", module.version[1], module.version[0]) + " attr:" + String.format("%08x", module.attribute) + " gp:" + String.format("%08x", module.gp_value));
        if ((module.attribute & 0x1000) != 0) {
            log.warn("Kernel mode module detected");
        }
        if ((module.attribute & 0x0800) != 0) {
            log.warn("VSH mode module detected");
        }
    }

    /**
     * @param f        The position of this buffer must be at the start of a
     *                 list of Elf32Rel structs.
     * @param RelCount The number of Elf32Rel structs to read and process.
     */
    private void relocateFromBuffer(ByteBuffer f, SceModule module, int baseAddress, Elf32 elf, int RelCount) throws IOException {
        Elf32Relocate rel = new Elf32Relocate();
        int AHL = 0;
        List<Integer> deferredHi16 = new LinkedList<Integer>();
        Memory mem = Memory.getInstance();
        for (int i = 0; i < RelCount; i++) {
            rel.read(f);
            int R_TYPE = (int) (rel.getR_info() & 0xFF);
            int OFS_BASE = (int) ((rel.getR_info() >> 8) & 0xFF);
            int ADDR_BASE = (int) ((rel.getR_info() >> 16) & 0xFF);
            long R_OFFSET = rel.getR_offset();
            if (log.isTraceEnabled()) {
                log.trace(String.format("Relocation #%d type=%d, Offset PH#%d, Base Offset PH#%d, Offset 0x%08X", i, R_TYPE, OFS_BASE, ADDR_BASE, R_OFFSET));
            }
            if (R_TYPE == 0xFF) {
                log.warn("Special relocation code 0xFF detected!");
                break;
            }
            int phOffset = (int) elf.getProgramHeader(OFS_BASE).getP_vaddr();
            int phBaseOffset = (int) elf.getProgramHeader(ADDR_BASE).getP_vaddr();
            int data_addr = (int) (baseAddress + R_OFFSET + phOffset);
            int data = readUnaligned32(mem, data_addr);
            long result = 0;
            int word32 = data & 0xFFFFFFFF;
            int targ26 = data & 0x03FFFFFF;
            int hi16 = data & 0x0000FFFF;
            int lo16 = data & 0x0000FFFF;
            int rel16 = data & 0x0000FFFF;
            int A = 0;
            int S = (int) baseAddress + phBaseOffset;
            int GP_ADDR = (int) baseAddress + (int) R_OFFSET;
            int GP_OFFSET = GP_ADDR - ((int) baseAddress & 0xFFFF0000);
            switch(R_TYPE) {
                case 0:
                    if (log.isTraceEnabled()) {
                        log.trace(String.format("R_MIPS_NONE addr=%08X", data_addr));
                    }
                    break;
                case 1:
                    data = (data & 0xFFFF0000) | ((data + S) & 0x0000FFFF);
                    if (log.isTraceEnabled()) {
                        log.trace(String.format("R_MIPS_16 addr=%08X before=%08X after=%08X", data_addr, word32, data));
                    }
                    break;
                case 2:
                    data += S;
                    if (log.isTraceEnabled()) {
                        log.trace(String.format("R_MIPS_32 addr=%08X before=%08X after=%08X", data_addr, word32, data));
                    }
                    break;
                case 4:
                    A = targ26;
                    result = ((A << 2) + S) >> 2;
                    data &= ~0x03FFFFFF;
                    data |= (int) (result & 0x03FFFFFF);
                    if (log.isTraceEnabled()) {
                        log.trace(String.format("R_MIPS_26 addr=%08X before=%08X after=%08X", data_addr, word32, data));
                    }
                    break;
                case 5:
                    A = hi16;
                    AHL = A << 16;
                    deferredHi16.add(data_addr);
                    if (log.isTraceEnabled()) {
                        log.trace(String.format("R_MIPS_HI16 addr=%08X", data_addr));
                    }
                    break;
                case 6:
                    A = lo16;
                    AHL &= ~0x0000FFFF;
                    AHL |= A & 0x0000FFFF;
                    result = AHL + S;
                    data &= ~0x0000FFFF;
                    data |= result & 0x0000FFFF;
                    for (Iterator<Integer> it = deferredHi16.iterator(); it.hasNext(); ) {
                        int data_addr2 = it.next();
                        int data2 = readUnaligned32(mem, data_addr2);
                        result = ((data2 & 0x0000FFFF) << 16) + A + S;
                        if ((A & 0x8000) != 0) {
                            result -= 0x10000;
                        }
                        if ((result & 0x8000) != 0) {
                            result += 0x10000;
                        }
                        data2 &= ~0x0000FFFF;
                        data2 |= (result >> 16) & 0x0000FFFF;
                        if (log.isTraceEnabled()) {
                            log.trace(String.format("R_MIPS_HILO16 addr=%08X before=%08X after=%08X", data_addr2, readUnaligned32(mem, data_addr2), data2));
                        }
                        writeUnaligned32(mem, data_addr2, data2);
                        it.remove();
                    }
                    if (log.isTraceEnabled()) {
                        log.trace(String.format("R_MIPS_LO16 addr=%08X before=%08X after=%08X", data_addr, word32, data));
                    }
                    break;
                case 7:
                    A = rel16;
                    if (A == 0) {
                        result = S - GP_ADDR;
                    } else {
                        result = S + GP_OFFSET + (((A & 0x00008000) != 0) ? (((A & 0x00003FFF) + 0x4000) | 0xFFFF0000) : A) - GP_ADDR;
                    }
                    if ((result > 32768) || (result < -32768)) {
                        log.warn("Relocation overflow (R_MIPS_GPREL16)");
                    }
                    data &= ~0x0000FFFF;
                    data |= (int) (result & 0x0000FFFF);
                    if (log.isTraceEnabled()) {
                        log.trace(String.format("R_MIPS_GPREL16 addr=%08X before=%08X after=%08X", data_addr, word32, data));
                    }
                    break;
                default:
                    log.warn(String.format("Unhandled relocation type %d at %08X", R_TYPE, data_addr));
                    break;
            }
            writeUnaligned32(mem, data_addr, data);
        }
    }

    /** Uses info from the elf program headers and elf section headers to
     * relocate a PRX. */
    private void relocateFromHeaders(ByteBuffer f, SceModule module, int baseAddress, Elf32 elf, int elfOffset) throws IOException {
        int i = 0;
        for (Elf32ProgramHeader phdr : elf.getProgramHeaderList()) {
            if (phdr.getP_type() == 0x700000A0L) {
                int RelCount = (int) phdr.getP_filesz() / Elf32Relocate.sizeof();
                if (log.isDebugEnabled()) {
                    log.debug("PH#" + i + ": relocating " + RelCount + " entries");
                }
                f.position((int) (elfOffset + phdr.getP_offset()));
                relocateFromBuffer(f, module, baseAddress, elf, RelCount);
                return;
            } else if (phdr.getP_type() == 0x700000A1L) {
                log.warn("Unimplemented:PH#" + i + ": relocate type 0x700000A1");
            }
            i++;
        }
        for (Elf32SectionHeader shdr : elf.getSectionHeaderList()) {
            if (shdr.getSh_type() == Elf32SectionHeader.SHT_REL) {
                log.warn(shdr.getSh_namez() + ": not relocating section");
            }
            if (shdr.getSh_type() == Elf32SectionHeader.SHT_PRXREL) {
                int RelCount = (int) shdr.getSh_size() / Elf32Relocate.sizeof();
                if (log.isDebugEnabled()) {
                    log.debug(shdr.getSh_namez() + ": relocating " + RelCount + " entries");
                }
                f.position((int) (elfOffset + shdr.getSh_offset()));
                relocateFromBuffer(f, module, baseAddress, elf, RelCount);
            }
        }
    }

    private void ProcessUnresolvedImports() {
        Memory mem = Memory.getInstance();
        NIDMapper nidMapper = NIDMapper.getInstance();
        int numberoffailedNIDS = 0;
        int numberofmappedNIDS = 0;
        for (SceModule module : Managers.modules.values()) {
            module.importFixupAttempts++;
            for (Iterator<DeferredStub> it = module.unresolvedImports.iterator(); it.hasNext(); ) {
                DeferredStub deferredStub = it.next();
                String moduleName = deferredStub.getModuleName();
                int nid = deferredStub.getNid();
                int importAddress = deferredStub.getImportAddress();
                int exportAddress;
                exportAddress = nidMapper.moduleNidToAddress(moduleName, nid);
                if (exportAddress != -1) {
                    int instruction = ((jpcsp.AllegrexOpcodes.J & 0x3f) << 26) | ((exportAddress >>> 2) & 0x03ffffff);
                    mem.write32(importAddress, instruction);
                    mem.write32(importAddress + 4, 0);
                    it.remove();
                    numberofmappedNIDS++;
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Mapped import at 0x%08X to export at 0x%08X [0x%08X] (attempt %d)", importAddress, exportAddress, nid, module.importFixupAttempts));
                    }
                } else if (nid == 0) {
                    log.warn(String.format("Ignoring import at 0x%08X [0x%08X] (attempt %d)", importAddress, nid, module.importFixupAttempts));
                    it.remove();
                    mem.write32(importAddress + 4, AllegrexOpcodes.ADDU | (2 << 11) | (0 << 16) | (0 << 21));
                } else {
                    int code = nidMapper.nidToSyscall(nid);
                    if (code != -1) {
                        int instruction = ((jpcsp.AllegrexOpcodes.SPECIAL & 0x3f) << 26) | (jpcsp.AllegrexOpcodes.SYSCALL & 0x3f) | ((code & 0x000fffff) << 6);
                        mem.write32(importAddress + 4, instruction);
                        it.remove();
                        numberofmappedNIDS++;
                        if (loadedFirstModule && log.isDebugEnabled()) {
                            log.debug(String.format("Mapped import at 0x%08X to syscall 0x%05X [0x%08X] (attempt %d)", importAddress, code, nid, module.importFixupAttempts));
                        }
                    } else {
                        log.warn(String.format("Failed to map import at 0x%08X [0x%08X] Module '%s'(attempt %d)", importAddress, nid, moduleName, module.importFixupAttempts));
                        numberoffailedNIDS++;
                    }
                }
            }
        }
        log.info(numberofmappedNIDS + " NIDS mapped");
        if (numberoffailedNIDS > 0) {
            log.info(numberoffailedNIDS + " remaining unmapped NIDS");
        }
    }

    private void LoadELFImports(SceModule module) throws IOException {
        Memory mem = Memory.getInstance();
        int stubHeadersAddress = module.stub_top;
        int stubHeadersEndAddress = module.stub_top + module.stub_size;
        String moduleName;
        for (int i = 0; stubHeadersAddress < stubHeadersEndAddress; i++) {
            Elf32StubHeader stubHeader = new Elf32StubHeader(mem, stubHeadersAddress);
            if (stubHeader.getSize() <= 0) {
                log.warn("Skipping dummy entry with size " + stubHeader.getSize());
                stubHeadersAddress += Elf32StubHeader.sizeof() / 2;
            } else {
                if (Memory.isAddressGood((int) stubHeader.getOffsetModuleName())) {
                    moduleName = Utilities.readStringNZ((int) stubHeader.getOffsetModuleName(), 64);
                } else {
                    moduleName = module.modname;
                }
                stubHeader.setModuleNamez(moduleName);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Processing Import #%d: %s", i, stubHeader.toString()));
                }
                if (stubHeader.getSize() > 5) {
                    stubHeadersAddress += stubHeader.getSize() * 4;
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("'%s' has size %d", stubHeader.getModuleNamez(), stubHeader.getSize()));
                    }
                } else {
                    stubHeadersAddress += Elf32StubHeader.sizeof();
                }
                if (!Memory.isAddressGood((int) stubHeader.getOffsetNid()) || !Memory.isAddressGood((int) stubHeader.getOffsetText())) {
                    log.warn(String.format("Incorrect s_nid or s_text address in StubHeader #%d: %s", i, stubHeader.toString()));
                } else {
                    IMemoryReader nidReader = MemoryReader.getMemoryReader((int) stubHeader.getOffsetNid(), stubHeader.getImports() * 4, 4);
                    for (int j = 0; j < stubHeader.getImports(); j++) {
                        int nid = nidReader.readNext();
                        int importAddress = (int) (stubHeader.getOffsetText() + j * 8);
                        DeferredStub deferredStub = new DeferredStub(stubHeader.getModuleNamez(), importAddress, nid);
                        module.unresolvedImports.add(deferredStub);
                        int instruction = ((jpcsp.AllegrexOpcodes.SPECIAL & 0x3f) << 26) | (jpcsp.AllegrexOpcodes.SYSCALL & 0x3f) | ((0xfffff & 0x000fffff) << 6);
                        mem.write32(importAddress + 4, instruction);
                    }
                }
            }
        }
        if (module.unresolvedImports.size() > 0) {
            if (log.isInfoEnabled()) {
                log.info(String.format("Found %d unresolved imports", module.unresolvedImports.size()));
            }
        }
    }

    private void LoadELFExports(SceModule module) throws IOException {
        NIDMapper nidMapper = NIDMapper.getInstance();
        Memory mem = Memory.getInstance();
        int entHeadersAddress = module.ent_top;
        int entHeadersEndAddress = module.ent_top + module.ent_size;
        int entCount = 0;
        String moduleName;
        for (int i = 0; entHeadersAddress < entHeadersEndAddress; i++) {
            Elf32EntHeader entHeader = new Elf32EntHeader(mem, entHeadersAddress);
            if ((entHeader.getSize() <= 0)) {
                log.warn("Skipping dummy entry with size " + entHeader.getSize());
                entHeadersAddress += Elf32EntHeader.sizeof() / 2;
            } else {
                if (Memory.isAddressGood((int) entHeader.getOffsetModuleName())) {
                    moduleName = Utilities.readStringNZ((int) entHeader.getOffsetModuleName(), 64);
                } else {
                    moduleName = module.modname;
                }
                entHeader.setModuleNamez(moduleName);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Processing header #%d at 0x%08X: %s", i, entHeadersAddress, entHeader.toString()));
                }
                if (entHeader.getSize() > 4) {
                    entHeadersAddress += entHeader.getSize() * 4;
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("'%s' has size %d", entHeader.getModuleNamez(), entHeader.getSize()));
                    }
                } else {
                    entHeadersAddress += Elf32EntHeader.sizeof();
                }
                int functionCount = entHeader.getFunctionCount();
                int variableCount = entHeader.getVariableCount();
                int nidAddr = (int) entHeader.getOffsetResident();
                IMemoryReader nidReader = MemoryReader.getMemoryReader(nidAddr, 4);
                int exportAddr = nidAddr + (functionCount + variableCount) * 4;
                IMemoryReader exportReader = MemoryReader.getMemoryReader(exportAddr, 4);
                if ((entHeader.getAttr() & 0x8000) == 0) {
                    for (int j = 0; j < functionCount; j++) {
                        int nid = nidReader.readNext();
                        int exportAddress = exportReader.readNext();
                        if (Memory.isAddressGood(exportAddress) && ((entHeader.getAttr() & 0x4000) != 0x4000)) {
                            nidMapper.addModuleNid(moduleName, nid, exportAddress);
                            entCount++;
                            if (log.isDebugEnabled()) {
                                log.debug(String.format("Export found at 0x%08X [0x%08X]", exportAddress, nid));
                            }
                        }
                    }
                } else {
                    for (int j = 0; j < functionCount; j++) {
                        int nid = nidReader.readNext();
                        int exportAddress = exportReader.readNext();
                        switch(nid) {
                            case 0xD632ACDB:
                                module.module_start_func = exportAddress;
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("module_start found: nid=0x%08X, function=0x%08X", nid, exportAddress));
                                }
                                break;
                            case 0xCEE8593C:
                                module.module_stop_func = exportAddress;
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("module_stop found: nid=0x%08X, function=0x%08X", nid, exportAddress));
                                }
                                break;
                            case 0x2F064FA6:
                                module.module_reboot_before_func = exportAddress;
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("module_reboot_before found: nid=0x%08X, function=0x%08X", nid, exportAddress));
                                }
                                break;
                            case 0xADF12745:
                                module.module_reboot_phase_func = exportAddress;
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("module_reboot_phase found: nid=0x%08X, function=0x%08X", nid, exportAddress));
                                }
                                break;
                            case 0xD3744BE0:
                                module.module_bootstart_func = exportAddress;
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("module_bootstart found: nid=0x%08X, function=0x%08X", nid, exportAddress));
                                }
                                break;
                            default:
                                if (Memory.isAddressGood(exportAddress) && ((entHeader.getAttr() & 0x4000) != 0x4000)) {
                                    nidMapper.addModuleNid(moduleName, nid, exportAddress);
                                    entCount++;
                                    if (log.isDebugEnabled()) {
                                        log.debug(String.format("Export found at 0x%08X [0x%08X]", exportAddress, nid));
                                    }
                                }
                                break;
                        }
                    }
                    int variableTableAddr = exportAddr + functionCount * 4;
                    IMemoryReader variableReader = MemoryReader.getMemoryReader(variableTableAddr, 4);
                    for (int j = 0; j < variableCount; j++) {
                        int nid = nidReader.readNext();
                        int variableAddr = variableReader.readNext();
                        switch(nid) {
                            case 0xF01D73A7:
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("module_info found: nid=0x%08X, addr=0x%08X", nid, variableAddr));
                                }
                                break;
                            case 0x0F7C276C:
                                module.module_start_thread_priority = mem.read32(variableAddr + 4);
                                module.module_start_thread_stacksize = mem.read32(variableAddr + 8);
                                module.module_start_thread_attr = mem.read32(variableAddr + 12);
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("module_start_thread_parameter found: nid=0x%08X, priority=%d, stacksize=%d, attr=0x%08X", nid, module.module_start_thread_priority, module.module_start_thread_stacksize, module.module_start_thread_attr));
                                }
                                break;
                            case 0xCF0CC697:
                                module.module_stop_thread_priority = mem.read32(variableAddr + 4);
                                module.module_stop_thread_stacksize = mem.read32(variableAddr + 8);
                                module.module_stop_thread_attr = mem.read32(variableAddr + 12);
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("module_stop_thread_parameter found: nid=0x%08X, priority=%d, stacksize=%d, attr=0x%08X", nid, module.module_stop_thread_priority, module.module_stop_thread_stacksize, module.module_stop_thread_attr));
                                }
                                break;
                            case 0xF4F4299D:
                                module.module_reboot_before_thread_priority = mem.read32(variableAddr + 4);
                                module.module_reboot_before_thread_stacksize = mem.read32(variableAddr + 8);
                                module.module_reboot_before_thread_attr = mem.read32(variableAddr + 12);
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("module_reboot_before_thread_parameter found: nid=0x%08X, priority=%d, stacksize=%d, attr=0x%08X", nid, module.module_reboot_before_thread_priority, module.module_reboot_before_thread_stacksize, module.module_reboot_before_thread_attr));
                                }
                                break;
                            case 0x11B97506:
                                int sdk_version = mem.read32(variableAddr);
                                if (log.isDebugEnabled()) {
                                    log.warn(String.format("module_sdk_version found: nid=0x%08X, sdk_version=0x%08X", nid, sdk_version));
                                }
                                break;
                            default:
                                log.warn(String.format("Unknown variable entry found: nid=0x%08X, addr=0x%08X", nid, variableAddr));
                                break;
                        }
                    }
                }
            }
        }
        if (entCount > 0) {
            if (log.isInfoEnabled()) {
                log.info(String.format("Found %d exports", entCount));
            }
        }
    }

    private void LoadELFDebuggerInfo(ByteBuffer f, SceModule module, int baseAddress, Elf32 elf, int elfOffset) throws IOException {
        Elf32SectionHeader shdr;
        shdr = elf.getSectionHeader(".init");
        if (shdr != null) {
            module.initsection[0] = (int) (baseAddress + shdr.getSh_addr());
            module.initsection[1] = (int) shdr.getSh_size();
        }
        shdr = elf.getSectionHeader(".fini");
        if (shdr != null) {
            module.finisection[0] = (int) (baseAddress + shdr.getSh_addr());
            module.finisection[1] = (int) shdr.getSh_size();
        }
        shdr = elf.getSectionHeader(".sceStub.text");
        if (shdr != null) {
            module.stubtextsection[0] = (int) (baseAddress + shdr.getSh_addr());
            module.stubtextsection[1] = (int) shdr.getSh_size();
        }
        if (!loadedFirstModule) {
            ElfHeaderInfo.ElfInfo = elf.getElfInfo();
            ElfHeaderInfo.ProgInfo = elf.getProgInfo();
            ElfHeaderInfo.SectInfo = elf.getSectInfo();
        }
    }
}
