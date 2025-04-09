package sun.java2d;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.peer.ComponentPeer;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.awt.AppContext;
import sun.awt.DisplayChangedListener;
import sun.awt.FontConfiguration;
import sun.awt.SunDisplayChanger;
import sun.font.CompositeFontDescriptor;
import sun.font.Font2D;
import sun.font.FontManager;
import sun.font.FontManager.FamilyDescription;
import sun.font.NativeFont;

/**
 * This is an implementation of a GraphicsEnvironment object for the
 * default local GraphicsEnvironment.
 *
 * @see GraphicsDevice
 * @see GraphicsConfiguration
 * @version %I% %G%
 */
public abstract class SunGraphicsEnvironment extends GraphicsEnvironment implements FontSupport, DisplayChangedListener {

    public static boolean isLinux;

    public static boolean isSolaris;

    public static boolean isOpenSolaris;

    public static boolean noType1Font;

    private static Font defaultFont;

    private static String lucidaSansFileName;

    public static final String lucidaFontName = "Lucida Sans Regular";

    public static boolean debugFonts = false;

    protected static Logger logger = null;

    private static ArrayList badFonts;

    public static String jreLibDirName;

    public static String jreFontDirName;

    private static HashSet<String> missingFontFiles = null;

    private FontConfiguration fontConfig;

    protected String fontPath;

    private boolean discoveredAllFonts = false;

    private boolean loadedAllFontFiles = false;

    protected HashSet registeredFontFiles = new HashSet();

    public static String eudcFontFileName;

    static {
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

            public Object run() {
                String debugLevel = System.getProperty("sun.java2d.debugfonts");
                if (debugLevel != null && !debugLevel.equals("false")) {
                    debugFonts = true;
                    logger = Logger.getLogger("sun.java2d");
                    if (debugLevel.equals("warning")) {
                        logger.setLevel(Level.WARNING);
                    } else if (debugLevel.equals("severe")) {
                        logger.setLevel(Level.SEVERE);
                    }
                }
                return null;
            }
        });
    }

    public SunGraphicsEnvironment() {
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

            public Object run() {
                String osName = System.getProperty("os.name");
                if ("Linux".equals(osName)) {
                    isLinux = true;
                } else if ("SunOS".equals(osName)) {
                    isSolaris = true;
                    String version = System.getProperty("os.version", "0.0");
                    try {
                        float ver = Float.parseFloat(version);
                        if (ver > 5.10f) {
                            File f = new File("/etc/release");
                            FileInputStream fis = new FileInputStream(f);
                            InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
                            BufferedReader br = new BufferedReader(isr);
                            String line = br.readLine();
                            if (line.indexOf("OpenSolaris") >= 0) {
                                isOpenSolaris = true;
                            } else {
                                String courierNew = "/usr/openwin/lib/X11/fonts/TrueType/CourierNew.ttf";
                                File courierFile = new File(courierNew);
                                isOpenSolaris = !courierFile.exists();
                            }
                            fis.close();
                        }
                    } catch (Exception e) {
                    }
                }
                noType1Font = "true".equals(System.getProperty("sun.java2d.noType1Font"));
                jreLibDirName = System.getProperty("java.home", "") + File.separator + "lib";
                jreFontDirName = jreLibDirName + File.separator + "fonts";
                if (useAbsoluteFontFileNames()) {
                    lucidaSansFileName = jreFontDirName + File.separator + "LucidaSansRegular.ttf";
                } else {
                    lucidaSansFileName = "LucidaSansRegular.ttf";
                }
                File badFontFile = new File(jreFontDirName + File.separator + "badfonts.txt");
                if (badFontFile.exists()) {
                    FileInputStream fis = null;
                    try {
                        badFonts = new ArrayList();
                        fis = new FileInputStream(badFontFile);
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader br = new BufferedReader(isr);
                        while (true) {
                            String name = br.readLine();
                            if (name == null) {
                                break;
                            } else {
                                if (debugFonts) {
                                    logger.warning("read bad font: " + name);
                                }
                                badFonts.add(name);
                            }
                        }
                    } catch (IOException e) {
                        try {
                            if (fis != null) {
                                fis.close();
                            }
                        } catch (IOException ioe) {
                        }
                    }
                }
                if (isLinux) {
                    registerFontDir(jreFontDirName);
                }
                registerFontsInDir(jreFontDirName, true, Font2D.JRE_RANK, true, false);
                registerJREFontsWithPlatform(jreFontDirName);
                fontConfig = createFontConfiguration();
                getPlatformFontPathFromFontConfig();
                String extraFontPath = fontConfig.getExtraFontPath();
                boolean prependToPath = false;
                boolean appendToPath = false;
                String dbgFontPath = System.getProperty("sun.java2d.fontpath");
                if (dbgFontPath != null) {
                    if (dbgFontPath.startsWith("prepend:")) {
                        prependToPath = true;
                        dbgFontPath = dbgFontPath.substring("prepend:".length());
                    } else if (dbgFontPath.startsWith("append:")) {
                        appendToPath = true;
                        dbgFontPath = dbgFontPath.substring("append:".length());
                    }
                }
                if (debugFonts) {
                    logger.info("JRE font directory: " + jreFontDirName);
                    logger.info("Extra font path: " + extraFontPath);
                    logger.info("Debug font path: " + dbgFontPath);
                }
                if (dbgFontPath != null) {
                    fontPath = getPlatformFontPath(noType1Font);
                    if (extraFontPath != null) {
                        fontPath = extraFontPath + File.pathSeparator + fontPath;
                    }
                    if (appendToPath) {
                        fontPath = fontPath + File.pathSeparator + dbgFontPath;
                    } else if (prependToPath) {
                        fontPath = dbgFontPath + File.pathSeparator + fontPath;
                    } else {
                        fontPath = dbgFontPath;
                    }
                    registerFontDirs(fontPath);
                } else if (extraFontPath != null) {
                    registerFontDirs(extraFontPath);
                }
                if (isSolaris && Locale.JAPAN.equals(Locale.getDefault())) {
                    registerFontDir("/usr/openwin/lib/locale/ja/X11/fonts/TT");
                }
                initCompositeFonts(fontConfig, null);
                defaultFont = new Font(Font.DIALOG, Font.PLAIN, 12);
                return null;
            }
        });
    }

    protected GraphicsDevice[] screens;

    /**
     * Returns an array of all of the screen devices.
     */
    public synchronized GraphicsDevice[] getScreenDevices() {
        GraphicsDevice[] ret = screens;
        if (ret == null) {
            int num = getNumScreens();
            ret = new GraphicsDevice[num];
            for (int i = 0; i < num; i++) {
                ret[i] = makeScreenDevice(i);
            }
            screens = ret;
        }
        return ret;
    }

    protected abstract int getNumScreens();

    protected abstract GraphicsDevice makeScreenDevice(int screennum);

    /**
     * Returns the default screen graphics device.
     */
    public GraphicsDevice getDefaultScreenDevice() {
        return getScreenDevices()[0];
    }

    /**
     * Returns a Graphics2D object for rendering into the
     * given BufferedImage.
     * @throws NullPointerException if BufferedImage argument is null
     */
    public Graphics2D createGraphics(BufferedImage img) {
        if (img == null) {
            throw new NullPointerException("BufferedImage cannot be null");
        }
        SurfaceData sd = SurfaceData.getDestSurfaceData(img);
        return new SunGraphics2D(sd, Color.white, Color.black, defaultFont);
    }

    protected String getPlatformFontPath(boolean noType1Font) {
        return FontManager.getFontPath(noType1Font);
    }

    /**
     * Whether registerFontFile expects absolute or relative
     * font file names.
     */
    protected boolean useAbsoluteFontFileNames() {
        return true;
    }

    /**
     * Returns file name for default font, either absolute
     * or relative as needed by registerFontFile.
     */
    public String getDefaultFontFile() {
        return lucidaSansFileName;
    }

    /**
     * Returns face name for default font, or null if
     * no face names are used for CompositeFontDescriptors
     * for this platform.
     */
    public String getDefaultFontFaceName() {
        return lucidaFontName;
    }

    public void loadFonts() {
        if (discoveredAllFonts) {
            return;
        }
        synchronized (lucidaFontName) {
            if (debugFonts) {
                Thread.dumpStack();
                logger.info("SunGraphicsEnvironment.loadFonts() called");
            }
            FontManager.initialiseDeferredFonts();
            java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

                public Object run() {
                    if (fontPath == null) {
                        fontPath = getPlatformFontPath(noType1Font);
                        registerFontDirs(fontPath);
                    }
                    if (fontPath != null) {
                        if (!FontManager.gotFontsFromPlatform()) {
                            registerFontsOnPath(fontPath, false, Font2D.UNKNOWN_RANK, false, true);
                            loadedAllFontFiles = true;
                        }
                    }
                    FontManager.registerOtherFontFiles(registeredFontFiles);
                    discoveredAllFonts = true;
                    return null;
                }
            });
        }
    }

    public void loadFontFiles() {
        loadFonts();
        if (loadedAllFontFiles) {
            return;
        }
        synchronized (lucidaFontName) {
            if (debugFonts) {
                Thread.dumpStack();
                logger.info("loadAllFontFiles() called");
            }
            java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

                public Object run() {
                    if (fontPath == null) {
                        fontPath = getPlatformFontPath(noType1Font);
                    }
                    if (fontPath != null) {
                        registerFontsOnPath(fontPath, false, Font2D.UNKNOWN_RANK, false, true);
                    }
                    loadedAllFontFiles = true;
                    return null;
                }
            });
        }
    }

    private boolean isNameForRegisteredFile(String fontName) {
        String fileName = FontManager.getFileNameForFontName(fontName);
        if (fileName == null) {
            return false;
        }
        return registeredFontFiles.contains(fileName);
    }

    private Font[] allFonts;

    /**
     * Returns all fonts installed in this environment.
     */
    public Font[] getAllInstalledFonts() {
        if (allFonts == null) {
            loadFonts();
            TreeMap fontMapNames = new TreeMap();
            Font2D[] allfonts = FontManager.getRegisteredFonts();
            for (int i = 0; i < allfonts.length; i++) {
                if (!(allfonts[i] instanceof NativeFont)) {
                    fontMapNames.put(allfonts[i].getFontName(null), allfonts[i]);
                }
            }
            String[] platformNames = FontManager.getFontNamesFromPlatform();
            if (platformNames != null) {
                for (int i = 0; i < platformNames.length; i++) {
                    if (!isNameForRegisteredFile(platformNames[i])) {
                        fontMapNames.put(platformNames[i], null);
                    }
                }
            }
            String[] fontNames = null;
            if (fontMapNames.size() > 0) {
                fontNames = new String[fontMapNames.size()];
                Object[] keyNames = fontMapNames.keySet().toArray();
                for (int i = 0; i < keyNames.length; i++) {
                    fontNames[i] = (String) keyNames[i];
                }
            }
            Font[] fonts = new Font[fontNames.length];
            for (int i = 0; i < fontNames.length; i++) {
                fonts[i] = new Font(fontNames[i], Font.PLAIN, 1);
                Font2D f2d = (Font2D) fontMapNames.get(fontNames[i]);
                if (f2d != null) {
                    FontManager.setFont2D(fonts[i], f2d.handle);
                }
            }
            allFonts = fonts;
        }
        Font[] copyFonts = new Font[allFonts.length];
        System.arraycopy(allFonts, 0, copyFonts, 0, allFonts.length);
        return copyFonts;
    }

    /**
     * Returns all fonts available in this environment.
     */
    public Font[] getAllFonts() {
        Font[] installedFonts = getAllInstalledFonts();
        Font[] created = FontManager.getCreatedFonts();
        if (created == null || created.length == 0) {
            return installedFonts;
        } else {
            int newlen = installedFonts.length + created.length;
            Font[] fonts = java.util.Arrays.copyOf(installedFonts, newlen);
            System.arraycopy(created, 0, fonts, installedFonts.length, created.length);
            return fonts;
        }
    }

    /**
     * Default locale can be changed but we need to know the initial locale
     * as that is what is used by native code. Changing Java default locale
     * doesn't affect that.
     * Returns the locale in use when using native code to communicate
     * with platform APIs. On windows this is known as the "system" locale,
     * and it is usually the same as the platform locale, but not always,
     * so this method also checks an implementation property used only
     * on windows and uses that if set.
     */
    private static Locale systemLocale = null;

    public static Locale getSystemStartupLocale() {
        if (systemLocale == null) {
            systemLocale = (Locale) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

                public Object run() {
                    String fileEncoding = System.getProperty("file.encoding", "");
                    String sysEncoding = System.getProperty("sun.jnu.encoding");
                    if (sysEncoding != null && !sysEncoding.equals(fileEncoding)) {
                        return Locale.ROOT;
                    }
                    String language = System.getProperty("user.language", "en");
                    String country = System.getProperty("user.country", "");
                    String variant = System.getProperty("user.variant", "");
                    return new Locale(language, country, variant);
                }
            });
        }
        return systemLocale;
    }

    protected void getJREFontFamilyNames(TreeMap<String, String> familyNames, Locale requestedLocale) {
        FontManager.registerDeferredJREFonts(jreFontDirName);
        Font2D[] physicalfonts = FontManager.getPhysicalFonts();
        for (int i = 0; i < physicalfonts.length; i++) {
            if (!(physicalfonts[i] instanceof NativeFont)) {
                String name = physicalfonts[i].getFamilyName(requestedLocale);
                familyNames.put(name.toLowerCase(requestedLocale), name);
            }
        }
    }

    private String[] allFamilies;

    private Locale lastDefaultLocale;

    public String[] getInstalledFontFamilyNames(Locale requestedLocale) {
        if (requestedLocale == null) {
            requestedLocale = Locale.getDefault();
        }
        if (allFamilies != null && lastDefaultLocale != null && requestedLocale.equals(lastDefaultLocale)) {
            String[] copyFamilies = new String[allFamilies.length];
            System.arraycopy(allFamilies, 0, copyFamilies, 0, allFamilies.length);
            return copyFamilies;
        }
        TreeMap<String, String> familyNames = new TreeMap<String, String>();
        String str;
        str = Font.SERIF;
        familyNames.put(str.toLowerCase(), str);
        str = Font.SANS_SERIF;
        familyNames.put(str.toLowerCase(), str);
        str = Font.MONOSPACED;
        familyNames.put(str.toLowerCase(), str);
        str = Font.DIALOG;
        familyNames.put(str.toLowerCase(), str);
        str = Font.DIALOG_INPUT;
        familyNames.put(str.toLowerCase(), str);
        if (requestedLocale.equals(getSystemStartupLocale()) && FontManager.getFamilyNamesFromPlatform(familyNames, requestedLocale)) {
            getJREFontFamilyNames(familyNames, requestedLocale);
        } else {
            loadFontFiles();
            Font2D[] physicalfonts = FontManager.getPhysicalFonts();
            for (int i = 0; i < physicalfonts.length; i++) {
                if (!(physicalfonts[i] instanceof NativeFont)) {
                    String name = physicalfonts[i].getFamilyName(requestedLocale);
                    familyNames.put(name.toLowerCase(requestedLocale), name);
                }
            }
        }
        String[] retval = new String[familyNames.size()];
        Object[] keyNames = familyNames.keySet().toArray();
        for (int i = 0; i < keyNames.length; i++) {
            retval[i] = (String) familyNames.get(keyNames[i]);
        }
        if (requestedLocale.equals(Locale.getDefault())) {
            lastDefaultLocale = requestedLocale;
            allFamilies = new String[retval.length];
            System.arraycopy(retval, 0, allFamilies, 0, allFamilies.length);
        }
        return retval;
    }

    public String[] getAvailableFontFamilyNames(Locale requestedLocale) {
        String[] installed = getInstalledFontFamilyNames(requestedLocale);
        TreeMap<String, String> map = FontManager.getCreatedFontFamilyNames();
        if (map == null || map.size() == 0) {
            return installed;
        } else {
            for (int i = 0; i < installed.length; i++) {
                map.put(installed[i].toLowerCase(requestedLocale), installed[i]);
            }
            String[] retval = new String[map.size()];
            Object[] keyNames = map.keySet().toArray();
            for (int i = 0; i < keyNames.length; i++) {
                retval[i] = (String) map.get(keyNames[i]);
            }
            return retval;
        }
    }

    public String[] getAvailableFontFamilyNames() {
        return getAvailableFontFamilyNames(Locale.getDefault());
    }

    /**
     * Returns a file name for the physical font represented by this platform
     * font name. The default implementation tries to obtain the file name
     * from the font configuration.
     * Subclasses may override to provide information from other sources.
     */
    protected String getFileNameFromPlatformName(String platformFontName) {
        return fontConfig.getFileNameFromPlatformName(platformFontName);
    }

    /**
     * Gets a <code>PrintJob2D</code> object suitable for the
     * the current platform.
     * @return    a <code>PrintJob2D</code> object.
     * @see       java.awt.PrintJob2D
     * @since     1.2
     */
    public PrinterJob getPrinterJob() {
        new Exception().printStackTrace();
        return null;
    }

    public static class TTFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            int offset = name.length() - 4;
            if (offset <= 0) {
                return false;
            } else {
                return (name.startsWith(".ttf", offset) || name.startsWith(".TTF", offset) || name.startsWith(".ttc", offset) || name.startsWith(".TTC", offset));
            }
        }
    }

    public static class T1Filter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            if (noType1Font) {
                return false;
            }
            int offset = name.length() - 4;
            if (offset <= 0) {
                return false;
            } else {
                return (name.startsWith(".pfa", offset) || name.startsWith(".pfb", offset) || name.startsWith(".PFA", offset) || name.startsWith(".PFB", offset));
            }
        }
    }

    public static final TTFilter ttFilter = new TTFilter();

    public static final T1Filter t1Filter = new T1Filter();

    protected void registerJREFontsWithPlatform(String pathName) {
        return;
    }

    public void register1dot0Fonts() {
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

            public Object run() {
                String type1Dir = "/usr/openwin/lib/X11/fonts/Type1";
                registerFontsInDir(type1Dir, true, Font2D.TYPE1_RANK, false, false);
                return null;
            }
        });
    }

    protected void registerFontDirs(String pathName) {
        return;
    }

    public void registerFontsInDir(String dirName) {
        registerFontsInDir(dirName, true, Font2D.JRE_RANK, true, false);
    }

    private void registerFontsInDir(String dirName, boolean useJavaRasterizer, int fontRank, boolean defer, boolean resolveSymLinks) {
        File pathFile = new File(dirName);
        addDirFonts(dirName, pathFile, ttFilter, FontManager.FONTFORMAT_TRUETYPE, useJavaRasterizer, fontRank == Font2D.UNKNOWN_RANK ? Font2D.TTF_RANK : fontRank, defer, resolveSymLinks);
        addDirFonts(dirName, pathFile, t1Filter, FontManager.FONTFORMAT_TYPE1, useJavaRasterizer, fontRank == Font2D.UNKNOWN_RANK ? Font2D.TYPE1_RANK : fontRank, defer, resolveSymLinks);
    }

    private void registerFontsOnPath(String pathName, boolean useJavaRasterizer, int fontRank, boolean defer, boolean resolveSymLinks) {
        StringTokenizer parser = new StringTokenizer(pathName, File.pathSeparator);
        try {
            while (parser.hasMoreTokens()) {
                registerFontsInDir(parser.nextToken(), useJavaRasterizer, fontRank, defer, resolveSymLinks);
            }
        } catch (NoSuchElementException e) {
        }
    }

    protected void registerFontFile(String fontFileName, String[] nativeNames, int fontRank, boolean defer) {
        if (registeredFontFiles.contains(fontFileName)) {
            return;
        }
        int fontFormat;
        if (ttFilter.accept(null, fontFileName)) {
            fontFormat = FontManager.FONTFORMAT_TRUETYPE;
        } else if (t1Filter.accept(null, fontFileName)) {
            fontFormat = FontManager.FONTFORMAT_TYPE1;
        } else {
            fontFormat = FontManager.FONTFORMAT_NATIVE;
        }
        registeredFontFiles.add(fontFileName);
        if (defer) {
            FontManager.registerDeferredFont(fontFileName, fontFileName, nativeNames, fontFormat, false, fontRank);
        } else {
            FontManager.registerFontFile(fontFileName, nativeNames, fontFormat, false, fontRank);
        }
    }

    protected void registerFontDir(String path) {
    }

    protected String[] getNativeNames(String fontFileName, String platformName) {
        return null;
    }

    private void addDirFonts(String dirName, File dirFile, FilenameFilter filter, int fontFormat, boolean useJavaRasterizer, int fontRank, boolean defer, boolean resolveSymLinks) {
        String[] ls = dirFile.list(filter);
        if (ls == null || ls.length == 0) {
            return;
        }
        String[] fontNames = new String[ls.length];
        String[][] nativeNames = new String[ls.length][];
        int fontCount = 0;
        for (int i = 0; i < ls.length; i++) {
            File theFile = new File(dirFile, ls[i]);
            String fullName = null;
            if (resolveSymLinks) {
                try {
                    fullName = theFile.getCanonicalPath();
                } catch (IOException e) {
                }
            }
            if (fullName == null) {
                fullName = dirName + File.separator + ls[i];
            }
            if (registeredFontFiles.contains(fullName)) {
                continue;
            }
            if (badFonts != null && badFonts.contains(fullName)) {
                if (debugFonts) {
                    logger.warning("skip bad font " + fullName);
                }
                continue;
            }
            registeredFontFiles.add(fullName);
            if (debugFonts && logger.isLoggable(Level.INFO)) {
                String message = "Registering font " + fullName;
                String[] natNames = getNativeNames(fullName, null);
                if (natNames == null) {
                    message += " with no native name";
                } else {
                    message += " with native name(s) " + natNames[0];
                    for (int nn = 1; nn < natNames.length; nn++) {
                        message += ", " + natNames[nn];
                    }
                }
                logger.info(message);
            }
            fontNames[fontCount] = fullName;
            nativeNames[fontCount++] = getNativeNames(fullName, null);
        }
        FontManager.registerFonts(fontNames, nativeNames, fontCount, fontFormat, useJavaRasterizer, fontRank, defer);
        return;
    }

    protected void addToMissingFontFileList(String fileName) {
        if (missingFontFiles == null) {
            missingFontFiles = new HashSet<String>();
        }
        missingFontFiles.add(fileName);
    }

    /**
     * Creates this environment's FontConfiguration.
     */
    protected abstract FontConfiguration createFontConfiguration();

    public abstract FontConfiguration createFontConfiguration(boolean preferLocaleFonts, boolean preferPropFonts);

    private void initCompositeFonts(FontConfiguration fontConfig, Hashtable altNameCache) {
        int numCoreFonts = fontConfig.getNumberCoreFonts();
        String[] fcFonts = fontConfig.getPlatformFontNames();
        for (int f = 0; f < fcFonts.length; f++) {
            String platformFontName = fcFonts[f];
            String fontFileName = getFileNameFromPlatformName(platformFontName);
            String[] nativeNames = null;
            if (fontFileName == null) {
                fontFileName = platformFontName;
            } else {
                if (f < numCoreFonts) {
                    addFontToPlatformFontPath(platformFontName);
                }
                nativeNames = getNativeNames(fontFileName, platformFontName);
            }
            registerFontFile(fontFileName, nativeNames, Font2D.FONT_CONFIG_RANK, true);
        }
        registerPlatformFontsUsedByFontConfiguration();
        CompositeFontDescriptor[] compositeFontInfo = fontConfig.get2DCompositeFontInfo();
        for (int i = 0; i < compositeFontInfo.length; i++) {
            CompositeFontDescriptor descriptor = compositeFontInfo[i];
            String[] componentFileNames = descriptor.getComponentFileNames();
            String[] componentFaceNames = descriptor.getComponentFaceNames();
            if (missingFontFiles != null) {
                for (int ii = 0; ii < componentFileNames.length; ii++) {
                    if (missingFontFiles.contains(componentFileNames[ii])) {
                        componentFileNames[ii] = getDefaultFontFile();
                        componentFaceNames[ii] = getDefaultFontFaceName();
                    }
                }
            }
            if (altNameCache != null) {
                FontManager.registerCompositeFont(descriptor.getFaceName(), componentFileNames, componentFaceNames, isOpenSolaris ? 1 : descriptor.getCoreComponentCount(), descriptor.getExclusionRanges(), descriptor.getExclusionRangeLimits(), true, altNameCache);
            } else {
                FontManager.registerCompositeFont(descriptor.getFaceName(), componentFileNames, componentFaceNames, isOpenSolaris ? 1 : descriptor.getCoreComponentCount(), descriptor.getExclusionRanges(), descriptor.getExclusionRangeLimits(), true);
            }
            if (debugFonts) {
                logger.info("registered " + descriptor.getFaceName());
            }
        }
    }

    /**
     * Notifies graphics environment that the logical font configuration
     * uses the given platform font name. The graphics environment may
     * use this for platform specific initialization.
     */
    protected void addFontToPlatformFontPath(String platformFontName) {
    }

    protected void registerPlatformFontsUsedByFontConfiguration() {
    }

    /**
     * Determines whether the given font is a logical font.
     */
    public static boolean isLogicalFont(Font f) {
        return FontConfiguration.isLogicalFontFamilyName(f.getFamily());
    }

    /**
     * Return the default font configuration.
     */
    public FontConfiguration getFontConfiguration() {
        return fontConfig;
    }

    /**
     * Return the bounds of a GraphicsDevice, less its screen insets.
     * See also java.awt.GraphicsEnvironment.getUsableBounds();
     */
    public static Rectangle getUsableBounds(GraphicsDevice gd) {
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        Rectangle usableBounds = gc.getBounds();
        usableBounds.x += insets.left;
        usableBounds.y += insets.top;
        usableBounds.width -= (insets.left + insets.right);
        usableBounds.height -= (insets.top + insets.bottom);
        return usableBounds;
    }

    /**
     * This method is provided for internal and exclusive use by Swing.
     * This method should no longer be called, instead directly call
     * FontManager.fontSupportsDefaultEncoding(Font).
     * This method will be removed once Swing is updated to no longer
     * call it.
     */
    public static boolean fontSupportsDefaultEncoding(Font font) {
        return FontManager.fontSupportsDefaultEncoding(font);
    }

    public static void useAlternateFontforJALocales() {
        FontManager.useAlternateFontforJALocales();
    }

    public void createCompositeFonts(Hashtable altNameCache, boolean preferLocale, boolean preferProportional) {
        FontConfiguration fontConfig = createFontConfiguration(preferLocale, preferProportional);
        initCompositeFonts(fontConfig, altNameCache);
    }

    protected void getPlatformFontPathFromFontConfig() {
    }

    /**
     * From the DisplayChangedListener interface; called
     * when the display mode has been changed.
     */
    public void displayChanged() {
        for (GraphicsDevice gd : getScreenDevices()) {
            if (gd instanceof DisplayChangedListener) {
                ((DisplayChangedListener) gd).displayChanged();
            }
        }
        displayChanger.notifyListeners();
    }

    /**
     * Part of the DisplayChangedListener interface: 
     * propagate this event to listeners
     */
    public void paletteChanged() {
        displayChanger.notifyPaletteChanged();
    }

    protected SunDisplayChanger displayChanger = new SunDisplayChanger();

    /**
     * Add a DisplayChangeListener to be notified when the display settings
     * are changed.  
     */
    public void addDisplayChangedListener(DisplayChangedListener client) {
        displayChanger.add(client);
    }

    /**
     * Remove a DisplayChangeListener from Win32GraphicsEnvironment
     */
    public void removeDisplayChangedListener(DisplayChangedListener client) {
        displayChanger.remove(client);
    }

    /**
     * Returns true if FlipBufferStrategy with COPIED buffer contents
     * is preferred for this peer's GraphicsConfiguration over 
     * BlitBufferStrategy, false otherwise.
     *
     * The reason FlipBS could be preferred is that in some configurations
     * an accelerated copy to the screen is supported (like Direct3D 9)
     */
    public boolean isFlipStrategyPreferred(ComponentPeer peer) {
        return false;
    }

    /**
     * default implementation does nothing.
     */
    public HashMap<String, FamilyDescription> populateHardcodedFileNameMap() {
        return new HashMap<String, FamilyDescription>(0);
    }
}
