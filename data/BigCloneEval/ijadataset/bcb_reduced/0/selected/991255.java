package ti.targetinfo.symtable.ti;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import ti.elfutil.ELFFile;
import ti.elfutil.ELFUtil;
import ti.elfutil.ELFFile.EType;
import ti.elfutil.ELFFile.ShFlags;
import ti.elfutil.ELFFile.ShType;
import ti.io.UDataInputStream;
import ti.io.UDataOutputStream;
import ti.io.VersionedExternalizable;
import ti.mcore.u.FileUtil;
import ti.mcore.u.log.PlatoLogger;
import ti.targetinfo.symtable.Symbol;
import ti.targetinfo.symtable.SymbolTable;
import ti.targetinfo.symtable.SymbolTableFactory;

class TISymbolTable extends SymbolTable {

    private static final long serialVersionUID = 8384705429692865728L;

    private static final PlatoLogger LOGGER = PlatoLogger.getLogger(TISymbolTable.class);

    private static final boolean DEBUG = LOGGER.isDebugEnabled();

    private static final int VERSION = 3;

    private static String lock = "lock";

    private Section[] textSections = new TISection[0];

    /**
	 * name -> TISymbol or TISymbol[]
	 */
    private Map<String, Object> varNameMap = new HashMap<String, Object>();

    /**
	 * symbols sorted by address.
	 */
    private TISymbol[] symbols = new TISymbol[0];

    private transient LinkedList<Filter> inputFilters = new LinkedList<Filter>();

    private transient LinkedList<URL> nestedDatabaseNames = new LinkedList<URL>();

    private TILineNumberTable lineNumberTable;

    private int e_type;

    public TISymbolTable() {
    }

    /** helper class for deciding which symbols to keep */
    private static class Filter {

        private final Pattern pattern;

        private final boolean invert;

        Filter(String pattern, boolean invert) {
            this.pattern = Pattern.compile(pattern);
            this.invert = invert;
        }

        boolean allowed(String symbolName) {
            return pattern.matcher(symbolName).matches() ^ invert;
        }
    }

    void prepare2LoadImage(String extension) {
        if (extension.equals("axf") || extension.equals("<exe>") || extension.equals("so")) {
            inputFilters.add(new Filter("[a-zA-Z_][a-zA-Z_0-9]*", false));
        } else {
            throw new IllegalArgumentException("unexpected extension: " + extension);
        }
    }

    private static final long align(long x, long y) {
        return (y * ((x / y) + ((x % y == 0) ? 0 : 1)));
    }

    private long[] layoutLkmSections(ELFFile.Section[] sections) {
        long[] alt_addr = new long[sections.length];
        long[] sh_flags = new long[sections.length];
        long addr = 0;
        final long[][] masks = { { ShFlags.SHF_EXECINSTR | ShFlags.SHF_ALLOC, 0 }, { ShFlags.SHF_ALLOC, ShFlags.SHF_WRITE }, { ShFlags.SHF_WRITE | ShFlags.SHF_ALLOC, 0 }, { ShFlags.SHF_ALLOC, 0 } };
        int symindex = -1;
        for (int i = 0; i < sections.length; i++) {
            ELFFile.Section s = sections[i];
            alt_addr[i] = -1;
            sh_flags[i] |= sections[i].sh_flags;
            if (s.sh_name.equals(".modinfo") || s.sh_name.equals("__versions")) {
                sh_flags[i] &= ~ShFlags.SHF_ALLOC;
            }
            if (s.sh_type == ShType.SHT_SYMTAB) {
                symindex = i;
            }
        }
        if (symindex != -1) {
            sh_flags[symindex] |= ShFlags.SHF_ALLOC;
            sh_flags[(int) sections[symindex].sh_link] |= ShFlags.SHF_ALLOC;
        }
        for (int m = 0; m < masks.length; m++) {
            for (int i = 0; i < sections.length; i++) {
                ELFFile.Section s = sections[i];
                if (((sh_flags[i] & masks[m][0]) != masks[m][0]) || ((sh_flags[i] & masks[m][1]) != 0) || (alt_addr[i] != -1) || s.sh_name.startsWith(".init")) {
                    continue;
                }
                long align = (s.sh_addralign == 0) ? 1 : s.sh_addralign;
                addr = align(addr, align);
                alt_addr[i] = addr;
                addr += s.sh_size;
                LOGGER.dbg("%2d:%d: 0x%08x: allocated %s\t(actual sh_flags=%x)\t(%d)", i, m, alt_addr[i], s, sh_flags[i], alt_addr[i]);
            }
        }
        for (int i = 0; i < alt_addr.length; i++) if (alt_addr[i] == -1) alt_addr[i] = -0;
        return alt_addr;
    }

    public boolean loadImage(String basefile) {
        synchronized (lock) {
            boolean success = true;
            try {
                LinkedList<Section> textSectList = new LinkedList<Section>();
                String[] filenames = getFilenames(basefile);
                for (String filename : filenames) {
                    ELFFile elfFile = new ELFFile(filename);
                    e_type = elfFile.e_type;
                    ELFFile.Section symTableSection = null;
                    ELFFile.Section lineTableSection = null;
                    long[] alt_addr;
                    if (elfFile.e_type == EType.ET_REL) {
                        if (filename.endsWith(".ko")) {
                            LOGGER.dbg("calculating section addresses");
                            alt_addr = layoutLkmSections(elfFile.sections);
                        } else {
                            LOGGER.logError("unknown relocatable type, don't know how to calculate section addresses");
                            return false;
                        }
                    } else {
                        LOGGER.dbg("using section addresses from ELF file");
                        alt_addr = new long[elfFile.sections.length];
                        for (int i = 0; i < elfFile.sections.length; i++) {
                            alt_addr[i] = elfFile.sections[i].sh_addr;
                        }
                    }
                    for (int i = 0; i < elfFile.sections.length; i++) {
                        ELFFile.Section sect = elfFile.sections[i];
                        LOGGER.dbg("*** " + sect + ", alt_addr=" + alt_addr[i]);
                        String temp = sect.sh_name.toLowerCase();
                        if (sect.sh_name.equals(ELFUtil.SYM_TABLE_NAME)) {
                            symTableSection = sect;
                        } else if ((temp.indexOf("text") >= 0) || (temp.indexOf("rom") >= 0) || (temp.indexOf("rodata") >= 0)) {
                            if (filename == filenames[0]) {
                                textSectList.add(new TISection(sect.sh_name, alt_addr[i], sect.sh_size, getSectionContent(elfFile, sect)));
                            }
                        } else if (sect.sh_name.equals(".debug_line")) {
                            lineTableSection = sect;
                        } else if ((sect.sh_type == 0x80000001L) && sect.sh_name.startsWith(".tidbg_")) {
                            LOGGER.dbg("found nested db: " + sect.sh_name + ", " + sect.sh_offset + "#" + sect.sh_size);
                            String dbName = sect.sh_name.substring(".tidbg_".length());
                            nestedDatabaseNames.add(new URL("x-elf:" + FileUtil.getURL(new File(filename)).toExternalForm() + "!" + dbName));
                        }
                    }
                    if ((symTableSection != null) && (symbols.length == 0)) {
                        parseSymTable(elfFile, symTableSection.sh_offset, symTableSection.sh_size, symTableSection.sh_entsize);
                    }
                    if ((lineTableSection != null) && (lineNumberTable == null)) {
                        try {
                            lineNumberTable = new TILineNumberTable(getSectionContent(elfFile, lineTableSection), elfFile.isBigEndian());
                        } catch (Throwable t) {
                            LOGGER.logError(t);
                        }
                    }
                    elfFile.close();
                }
                textSections = textSectList.toArray(new Section[textSectList.size()]);
            } catch (Exception e) {
                LOGGER.logError(e);
                success = false;
            }
            dumpContents();
            return success;
        }
    }

    private static byte[] getSectionContent(ELFFile elfFile, ELFFile.Section sect) throws IOException {
        byte[] content = new byte[(int) sect.sh_size];
        elfFile.read(sect.sh_offset, content);
        return content;
    }

    /**
	 * in some cases, debug symbols may be split out into a seperate file...
	 * figure this out, and return the set of all files to look in..
	 */
    private static String[] getFilenames(String basefile) {
        String basename = FileUtil.basename(basefile);
        String dirname = FileUtil.dirname(basefile);
        if ((new File(dirname + "/.debug/" + basename)).exists()) {
            return new String[] { basefile, dirname + "/.debug/" + basename };
        } else {
            return new String[] { basefile };
        }
    }

    public Collection<Filter> getInputFilters() {
        return inputFilters;
    }

    public Symbol findByName(String name) {
        Object val = varNameMap.get(name);
        if (val == null) return null;
        if (val instanceof TISymbol) {
            return (TISymbol) val;
        } else {
            TISymbol[] arr = (TISymbol[]) val;
            if (arr.length > 0) return arr[0];
            return null;
        }
    }

    public Symbol findByAddress(long addr) {
        int idx = findIndex(symbols, addr);
        if (idx < 0) return null;
        TISymbol sym = symbols[idx];
        long saddr = sym.getAddress();
        if (saddr > addr) return null;
        long sz = sym.getSize();
        if (sz == 0) {
            if (((idx + 1) >= symbols.length) || (addr < symbols[idx + 1].getAddress())) return sym;
        } else {
            if (addr < (saddr + sz)) return sym;
        }
        return null;
    }

    private static final int findIndex(TISymbol[] varAddrMap, long addr) {
        int idx = 0;
        int end = varAddrMap.length;
        int pivot = (idx + end) / 2;
        while (idx < end) {
            long paddr = varAddrMap[pivot].getAddress();
            if (addr < paddr) end = pivot - 1; else if (addr > paddr) idx = pivot + 1; else break;
            pivot = (idx + end) / 2;
        }
        if (pivot >= varAddrMap.length) pivot = varAddrMap.length - 1;
        if (pivot > 0) while (varAddrMap[pivot].getAddress() > addr) pivot--;
        return pivot;
    }

    private void dumpContents() {
        if (DEBUG) {
            try {
                File f = new File("symbols.txt");
                LOGGER.dbg("*** output to: " + f.getAbsolutePath());
                java.io.FileWriter fw = new java.io.FileWriter(f);
                outputContents(fw);
                fw.flush();
                fw.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void outputContents(Writer w) {
        try {
            for (int i = 0; i < textSections.length; i++) {
                w.write("==========================================\n");
                w.write("TEXT SECTION: " + i + "\n");
                w.write("==========================================\n");
                w.write(textSections[i].toString() + "\n");
            }
            w.write("==========================================\n");
            w.write("VARIABLE NAMES\n");
            w.write("==========================================\n");
            outputContents(w, varNameMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void outputContents(Writer w, Map<String, Object> map) throws IOException {
        for (Map.Entry<String, Object> e : map.entrySet()) {
            w.write(e.getKey().toString());
            w.write(" ");
            Object obj = e.getValue();
            if (obj instanceof TISymbol) {
                outputContents(w, (TISymbol) obj);
            } else {
                TISymbol[] symbols = (TISymbol[]) obj;
                for (int i = 0; i < symbols.length; i++) outputContents(w, symbols[i]);
            }
        }
    }

    protected void outputContents(Writer w, TISymbol sym) throws IOException {
        w.write("name=" + sym.getFullName() + ", 0x" + Long.toHexString(sym.getAddress()) + "\n");
    }

    public Set<String> getSymbolNames() {
        return varNameMap.keySet();
    }

    /**
	 * Access the version number of the TypeInfo that is currently being de-
	 * serialized.  This is only intended to be used by {@link TISymbol} class 
	 * whose format may depend on the current
	 * file version.
	 * 
	 * @return tdt file version
	 */
    @Override
    protected int getCurrentVersion() {
        return VERSION;
    }

    public Section[] getSections(SectionType type) {
        if (type != SECT_TEXT) throw new IllegalArgumentException("bad type");
        return textSections;
    }

    public SymbolTableFactory getSymbolTableFactory() {
        return TISymbolTableFactory.getDefault();
    }

    private void parseSymTable(ELFFile elfFile, long symTableOffset, long symTableSize, long symEntrySize) throws IOException {
        long numEntries = symTableSize / symEntrySize;
        LinkedList<TISymbol> symbolList = new LinkedList<TISymbol>();
        for (int i = 0; i < numEntries; i++) {
            long offset = symTableOffset + i * 16;
            String symbolName = elfFile.getSymbolName(elfFile.readU4(offset));
            boolean wantedSymbol = false;
            for (Filter filter : inputFilters) {
                if (filter.allowed(symbolName)) {
                    wantedSymbol = true;
                    break;
                }
            }
            if (wantedSymbol) {
                long address = elfFile.readU4(offset + 4 * 1);
                if (address != 0) {
                    long size = elfFile.readU4(offset + 4 * 2);
                    TISymbol symbol = new TISymbol(symbolName, address, size);
                    add(varNameMap, symbol.getName(), symbol);
                    symbolList.add(symbol);
                }
            }
        }
        TISymbol[] varAddrMap = new TISymbol[symbolList.size()];
        varAddrMap = symbolList.toArray(varAddrMap);
        sort(varAddrMap);
        this.symbols = varAddrMap;
    }

    private static final void sort(TISymbol[] symbols) {
        Arrays.sort(symbols, new Comparator<TISymbol>() {

            public int compare(TISymbol s1, TISymbol s2) {
                long diff = s1.getAddress() - s2.getAddress();
                if (diff < 0) return -1; else if (diff > 0) return 1; else return 0;
            }
        });
    }

    protected static final void add(Map<String, Object> map, String key, TISymbol symbol) {
        Object val = map.get(key);
        if (val == null) {
            map.put(key, symbol);
        } else if (val instanceof TISymbol) {
            map.put(key, new TISymbol[] { (TISymbol) val, symbol });
        } else {
            int len = ((TISymbol[]) val).length;
            TISymbol[] newVal = new TISymbol[len + 1];
            System.arraycopy(val, 0, newVal, 0, len);
            newVal[len] = symbol;
            map.put(key, newVal);
        }
    }

    @Override
    protected void readVersioned(int version, DataInput in) throws IOException {
        try {
            if (version == 0) {
                VersionedExternalizable.setLegacyVersion(0);
            }
            if (!(in instanceof UDataInputStream)) {
                in = new UDataInputStream((InputStream) in);
            }
            if (version > VERSION) {
                throw new IOException("Incompatible TISymbolTable version!  (" + version + ")");
            }
            if (version >= 0) {
                int numOfSymbols = (int) in.readLong();
                TISymbol[] varAddrMap = new TISymbol[numOfSymbols];
                varNameMap.clear();
                for (int i = 0; i < numOfSymbols; i++) {
                    TISymbol symbol = new TISymbol();
                    symbol.readExternal(in);
                    add(varNameMap, symbol.getName(), symbol);
                    varAddrMap[i] = symbol;
                }
                sort(varAddrMap);
                this.symbols = varAddrMap;
                int numOfSections = in.readInt();
                textSections = new TISection[numOfSections];
                for (int i = 0; i < numOfSections; i++) {
                    TISection section = new TISection();
                    section.readExternal(in);
                    textSections[i] = section;
                }
            }
            if (version >= 2) {
                if (in.readBoolean()) {
                    lineNumberTable = new TILineNumberTable();
                    lineNumberTable.readExternal(in);
                }
            }
            if (version >= 3) {
                e_type = in.readInt();
            }
            dumpContents();
        } finally {
            VersionedExternalizable.clearLegacyVersion();
        }
    }

    @Override
    protected void writeVersioned(DataOutput out) throws IOException {
        if (!(out instanceof UDataOutputStream)) {
            out = new UDataOutputStream((OutputStream) out);
        }
        out.writeLong(getNumOfValidSymbols());
        writeSymbols(out);
        out.writeInt(textSections.length);
        for (int i = 0; i < textSections.length; i++) {
            TISection section = (TISection) textSections[i];
            section.writeExternal(out);
        }
        out.writeBoolean(lineNumberTable != null);
        if (lineNumberTable != null) {
            lineNumberTable.writeExternal(out);
        }
        out.writeInt(e_type);
    }

    private void writeSymbols(DataOutput out) throws IOException {
        for (Object val : varNameMap.values()) {
            if (val instanceof TISymbol) {
                ((TISymbol) val).writeExternal(out);
            } else {
                TISymbol[] vals = (TISymbol[]) val;
                for (int i = 0; i < vals.length; i++) vals[i].writeExternal(out);
            }
        }
    }

    @Override
    public boolean isAbsolute() {
        return e_type == ELFFile.EType.ET_EXEC;
    }

    public static void main(String[] args) {
        if (args.length == 0) args = new String[] { "/Users/robclark/local-src/tmp/target/ttiftest.ko" };
        TISymbolTable mytable = new TISymbolTable();
        mytable.prepare2LoadImage("axf");
        mytable.loadImage(args[0]);
        long addr = mytable.symbols[0].getAddress();
        while (true) {
            TILineNumberTable.TILineNumberProgram.TILineNumberInfo lni = (TILineNumberTable.TILineNumberProgram.TILineNumberInfo) mytable.getLineNumberInfo(addr);
            if (lni == null) break;
            Symbol sym = mytable.findByAddress(lni.address);
            if (sym != null) System.err.printf("0x%08x:%s %s:%d\n", lni.address, sym.getName(), lni.getFile(), lni.getLine()); else System.err.printf("null symbol\n");
            addr = lni.address + 1;
        }
    }

    private long getNumOfValidSymbols() {
        long sum = 0;
        for (Object val : varNameMap.values()) {
            if (val instanceof TISymbol) sum += 1; else sum += ((TISymbol[]) val).length;
        }
        return sum;
    }

    public URL[] getNestedDatabases() {
        return nestedDatabaseNames.toArray(new URL[nestedDatabaseNames.size()]);
    }

    @Override
    public LineNumberInfo getLineNumberInfo(long addr) {
        if (lineNumberTable != null) return lineNumberTable.getLineNumberInfo(addr);
        return null;
    }
}
