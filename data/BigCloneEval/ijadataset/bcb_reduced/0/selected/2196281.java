package com.limegroup.gnutella.util;

import java.util.Properties;
import java.io.*;
import java.net.*;
import java.util.Locale;

public final class CommonUtils {

    /** 
	 * Constant for the current version of LimeWire.
	 */
    private static final String LIMEWIRE_VERSION = "@version@";

    /**
     * Variable used for testing only, it's value is set to whatever the test
     * needs, and getVersion method retuns this value if it's not null
     */
    private static String testVersion = null;

    /**
     * The cached value of the major revision number.
     */
    private static final int _majorVersionNumber = getMajorVersionNumberInternal(LIMEWIRE_VERSION);

    /**
     * The cached value of the minor revision number.
     */
    private static final int _minorVersionNumber = getMinorVersionNumberInternal(LIMEWIRE_VERSION);

    /**
     * The cached value of the really minor version number.
     */
    private static final int _serviceVersionNumber = getServiceVersionNumberInternal(LIMEWIRE_VERSION);

    /**
     * The cached value of the GUESS major revision number.
     */
    private static final int _guessMajorVersionNumber = 0;

    /**
     * The cached value of the GUESS minor revision number.
     */
    private static final int _guessMinorVersionNumber = 1;

    /**
     * The cached value of the Ultrapeer major revision number.
     */
    private static final int _upMajorVersionNumber = 0;

    /**
     * The cached value of the Ultrapeer minor revision number.
     */
    private static final int _upMinorVersionNumber = 1;

    public static final String QHD_VENDOR_NAME = "LION";

    /** 
	 * Constant for the java system properties.
	 */
    private static final Properties PROPS = System.getProperties();

    /** 
	 * Variable for whether or not we're on Windows.
	 */
    private static boolean _isWindows = false;

    /** 
	 * Variable for whether or not we're on Windows NT.
	 */
    private static boolean _isWindowsNT = false;

    /** 
	 * Variable for whether or not we're on Windows XP.
	 */
    private static boolean _isWindowsXP = false;

    /** 
	 * Variable for whether or not we're on Windows NT, 2000, or XP.
	 */
    private static boolean _isWindowsNTor2000orXP = false;

    /** 
	 * Variable for whether or not we're on 2000 or XP.
	 */
    private static boolean _isWindows2000orXP = false;

    /** 
	 * Variable for whether or not we're on Windows 95.
	 */
    private static boolean _isWindows95 = false;

    /** 
	 * Variable for whether or not we're on Windows 98.
	 */
    private static boolean _isWindows98 = false;

    /** 
	 * Variable for whether or not we're on Windows Me.
	 */
    private static boolean _isWindowsMe = false;

    /** 
	 * Variable for whether or not the operating system allows the 
	 * application to be reduced to the system tray.
	 */
    private static boolean _supportsTray = false;

    /**
	 * Variable for whether or not we're on Mac 9.1 or below.
	 */
    private static boolean _isMacClassic = false;

    /** 
	 * Variable for whether or not we're on MacOSX.
	 */
    private static boolean _isMacOSX = false;

    /** 
	 * Variable for whether or not we're on Linux.
	 */
    private static boolean _isLinux = false;

    /** 
	 * Variable for whether or not we're on Solaris.
	 */
    private static boolean _isSolaris = false;

    /**
     * Variable for whether or not we're on OS/2.
     */
    private static boolean _isOS2 = false;

    /**
     * Several arrays of illegal characters on various operating systems.
     * Used by convertFileName
     */
    private static final char[] ILLEGAL_CHARS_ANY_OS = { '/', '\n', '\r', '\t', '\0', '\f' };

    private static final char[] ILLEGAL_CHARS_UNIX = { '`' };

    private static final char[] ILLEGAL_CHARS_WINDOWS = { '?', '*', '\\', '<', '>', '|', '\"', ':' };

    /**
	 * Variable for whether or not the JVM is 1.1.8.
	 */
    private static boolean _isJava118 = false;

    /**
	 * Cached constant for the HTTP Server: header value.
	 */
    private static final String HTTP_SERVER;

    private static final String LIMEWIRE_PREFS_DIR_NAME = ".lionshare";

    /**
	 * Constant for the current running directory.
	 */
    private static final File CURRENT_DIRECTORY = new File(PROPS.getProperty("user.dir"));

    /**
     * Variable for whether or this this is a PRO version of LimeWire. 
     */
    private static boolean _isPro = false;

    /**
     * Variable for the settings directory.
     */
    static File SETTINGS_DIRECTORY = null;

    /**
	 * Make sure the constructor can never be called.
	 */
    private CommonUtils() {
    }

    /**
	 * Initialize the settings statically. 
	 */
    static {
        String os = System.getProperty("os.name").toLowerCase(Locale.US);
        _isWindows = os.indexOf("windows") != -1;
        if (os.indexOf("windows nt") != -1 || os.indexOf("windows 2000") != -1 || os.indexOf("windows xp") != -1) _isWindowsNTor2000orXP = true;
        if (os.indexOf("windows 2000") != -1 || os.indexOf("windows xp") != -1) _isWindows2000orXP = true;
        if (os.indexOf("windows nt") != -1) _isWindowsNT = true;
        if (os.indexOf("windows xp") != -1) _isWindowsXP = true;
        if (os.indexOf("windows 95") != -1) _isWindows95 = true;
        if (os.indexOf("windows 98") != -1) _isWindows98 = true;
        if (os.indexOf("windows me") != -1) _isWindowsMe = true;
        if (_isWindows) _supportsTray = true;
        _isSolaris = os.indexOf("solaris") != -1;
        _isLinux = os.indexOf("linux") != -1;
        _isOS2 = os.indexOf("os/2") != -1;
        if (os.startsWith("mac os")) {
            if (os.endsWith("x")) {
                _isMacOSX = true;
            } else {
                _isMacClassic = true;
            }
        }
        if (CommonUtils.getJavaVersion().startsWith("1.1.8")) {
            _isJava118 = true;
        }
        if (!LIMEWIRE_VERSION.endsWith("Pro")) {
            HTTP_SERVER = "LionShare/" + LIMEWIRE_VERSION;
        } else {
            HTTP_SERVER = ("LionShare/" + LIMEWIRE_VERSION.substring(0, LIMEWIRE_VERSION.length() - 4) + " (Pro)");
            _isPro = true;
        }
    }

    /** Gets the major version of GUESS supported.
     */
    public static int getGUESSMajorVersionNumber() {
        return _guessMajorVersionNumber;
    }

    /** Gets the minor version of GUESS supported.
     */
    public static int getGUESSMinorVersionNumber() {
        return _guessMinorVersionNumber;
    }

    /** Gets the major version of Ultrapeer Protocol supported.
     */
    public static int getUPMajorVersionNumber() {
        return _upMajorVersionNumber;
    }

    /** Gets the minor version of Ultrapeer Protocol supported.
     */
    public static int getUPMinorVersionNumber() {
        return _upMinorVersionNumber;
    }

    /**
	 * Returns the current version number of LimeWire as
     * a string, e.g., "1.4".
	 */
    public static String getLimeWireVersion() {
        if (testVersion == null) return LIMEWIRE_VERSION;
        return testVersion;
    }

    /** Gets the major version of LimeWire.
     */
    public static int getMajorVersionNumber() {
        return _majorVersionNumber;
    }

    /** Gets the minor version of LimeWire.
     */
    public static int getMinorVersionNumber() {
        return _minorVersionNumber;
    }

    /** Gets the minor minor version of LimeWire.
     */
    public static int getServiceVersionNumber() {
        return _serviceVersionNumber;
    }

    static int getMajorVersionNumberInternal(String version) {
        if (!version.equals("@" + "version" + "@")) {
            try {
                int firstDot = version.indexOf(".");
                String majorStr = version.substring(0, firstDot);
                return new Integer(majorStr).intValue();
            } catch (NumberFormatException nfe) {
            }
        }
        return 2;
    }

    /**
     * Accessor for whether or not this is LimeWire pro.
     *
     * @return <tt>true</tt> if it is pro, otherwise <tt>false</tt>
     */
    public static boolean isPro() {
        return _isPro;
    }

    /**
     * Accessor for whether or not this is a testing version
     * (@version@) of LimeWire.
     *
     * @return <tt>true</tt> if the version is @version@,
     *  otherwise <tt>false</tt>
     */
    public static boolean isTestingVersion() {
        return LIMEWIRE_VERSION.equals("@" + "version" + "@");
    }

    static int getMinorVersionNumberInternal(String version) {
        if (!version.equals("@" + "version" + "@")) {
            try {
                int firstDot = version.indexOf(".");
                String minusMajor = version.substring(firstDot + 1);
                int secondDot = minusMajor.indexOf(".");
                String minorStr = minusMajor.substring(0, secondDot);
                return new Integer(minorStr).intValue();
            } catch (NumberFormatException nfe) {
            }
        }
        return 7;
    }

    static int getServiceVersionNumberInternal(String version) {
        if (!version.equals("@" + "version" + "@")) {
            try {
                int firstDot = version.indexOf(".");
                int secondDot = version.indexOf(".", firstDot + 1);
                int p = secondDot + 1;
                int q = p;
                while (q < version.length() && Character.isDigit(version.charAt(q))) {
                    q++;
                }
                if (p != q) {
                    String service = version.substring(p, q);
                    return new Integer(service).intValue();
                }
            } catch (NumberFormatException nfe) {
            }
        }
        return 0;
    }

    /**
	 * Returns a version number appropriate for upload headers.
     * Same as '"LimeWire "+getLimeWireVersion'.
	 */
    public static String getVendor() {
        return "LionShare " + LIMEWIRE_VERSION;
    }

    /**
	 * Returns the string for the server that should be reported in the HTTP
	 * "Server: " tag.
	 * 
	 * @return the HTTP "Server: " header value
	 */
    public static String getHttpServer() {
        return HTTP_SERVER;
    }

    /**
	 * Returns the version of java we're using.
	 */
    public static String getJavaVersion() {
        return PROPS.getProperty("java.version");
    }

    /**
	 * Returns the operating system.
	 */
    public static String getOS() {
        return PROPS.getProperty("os.name");
    }

    /**
	 * Returns the operating system version.
	 */
    public static String getOSVersion() {
        return PROPS.getProperty("os.version");
    }

    /**
	 * Returns the user's current working directory as a <tt>File</tt>
	 * instance, or <tt>null</tt> if the property is not set.
	 *
	 * @return the user's current working directory as a <tt>File</tt>
	 *  instance, or <tt>null</tt> if the property is not set
	 */
    public static File getCurrentDirectory() {
        return CURRENT_DIRECTORY;
    }

    /**
     * Returns true if this is Windows NT or Windows 2000 and
	 * hence can support a system tray feature.
     */
    public static boolean supportsTray() {
        return _supportsTray;
    }

    /**
	 * Returns whether or not this operating system is considered
	 * capable of meeting the requirements of a ultrapeer.
	 *
	 * @return <tt>true</tt> if this os meets ultrapeer requirements,
	 *         <tt>false</tt> otherwise
	 */
    public static boolean isUltrapeerOS() {
        if (_isWindows98 || _isWindows95 || _isWindowsMe || _isMacClassic || _isWindowsNT) {
            return false;
        }
        return true;
    }

    /**
	 * Returns whether or not the os is some version of Windows.
	 *
	 * @return <tt>true</tt> if the application is running on some Windows 
	 *         version, <tt>false</tt> otherwise
	 */
    public static boolean isWindows() {
        return _isWindows;
    }

    /**
	 * Returns whether or not the os is Windows NT, 2000, or XP.
	 *
	 * @return <tt>true</tt> if the application is running on Windows NT,
	 *  2000, or XP <tt>false</tt> otherwise
	 */
    public static boolean isWindowsNTor2000orXP() {
        return _isWindowsNTor2000orXP;
    }

    /**
	 * Returns whether or not the os is 2000 or XP.
	 *
	 * @return <tt>true</tt> if the application is running on 2000 or XP,
	 *  <tt>false</tt> otherwise
	 */
    public static boolean isWindows2000orXP() {
        return _isWindows2000orXP;
    }

    /**
	 * Returns whether or not the os is WinXP.
	 *
	 * @return <tt>true</tt> if the application is running on WinXP,
	 *  <tt>false</tt> otherwise
	 */
    public static boolean isWindowsXP() {
        return _isWindowsXP;
    }

    /** 
	 * Returns whether or not the os is Mac 9.1 or earlier.
	 *
	 * @return <tt>true</tt> if the application is running on a Mac version
	 *         prior to OSX, <tt>false</tt> otherwise
	 */
    public static boolean isMacClassic() {
        return _isMacClassic;
    }

    /**
     * Returns whether or not the os is OS/2.
     *
     * @return <tt>true</tt> if the application is running on OS/2,
     *         <tt>false</tt> otherwise
     */
    public static boolean isOS2() {
        return _isOS2;
    }

    /** 
	 * Returns whether or not the os is Mac OSX.
	 *
	 * @return <tt>true</tt> if the application is running on Mac OSX, 
	 *         <tt>false</tt> otherwise
	 */
    public static boolean isMacOSX() {
        return _isMacOSX;
    }

    /** 
	 * Returns whether or not the os is Mac OSX 10.2 or above.
	 *
	 * @return <tt>true</tt> if the application is running on Mac OSX, 
	 *  10.2 or above, <tt>false</tt> otherwise
	 */
    public static boolean isJaguarOrAbove() {
        if (!isMacOSX()) return false;
        return getOSVersion().startsWith("10.2");
    }

    /**
	 * Returns whether or not the os is Mac OSX 10.3 or above.
	 *
	 * @return <tt>true</tt> if the application is running on Mac OSX, 
	 *  10.3 or above, <tt>false</tt> otherwise
	 */
    public static boolean isPantherOrAbove() {
        if (!isMacOSX()) return false;
        return getOSVersion().startsWith("10.3");
    }

    /**
     * Returns whether or not the Cocoa Foundation classes are available.
     */
    public static boolean isCocoaFoundationAvailable() {
        try {
            Class.forName("com.apple.cocoa.foundation.NSUserDefaults");
            Class.forName("com.apple.cocoa.foundation.NSMutableDictionary");
            Class.forName("com.apple.cocoa.foundation.NSMutableArray");
            Class.forName("com.apple.cocoa.foundation.NSObject");
            return true;
        } catch (ClassNotFoundException error) {
            return false;
        } catch (NoClassDefFoundError error) {
            return false;
        }
    }

    /** 
	 * Returns whether or not the os is any Mac os.
	 *
	 * @return <tt>true</tt> if the application is running on Mac OSX
	 *  or any previous mac version, <tt>false</tt> otherwise
	 */
    public static boolean isAnyMac() {
        return _isMacClassic || _isMacOSX;
    }

    /** 
	 * Returns whether or not the os is Solaris.
	 *
	 * @return <tt>true</tt> if the application is running on Solaris, 
	 *         <tt>false</tt> otherwise
	 */
    public static boolean isSolaris() {
        return _isSolaris;
    }

    /** 
	 * Returns whether or not the os is Linux.
	 *
	 * @return <tt>true</tt> if the application is running on Linux, 
	 *         <tt>false</tt> otherwise
	 */
    public static boolean isLinux() {
        return _isLinux;
    }

    /** 
	 * Returns whether or not the os is some version of
	 * Unix, defined here as only Solaris or Linux.
	 */
    public static boolean isUnix() {
        return _isLinux || _isSolaris;
    }

    /**
	 * Returns whether or not the current JVM is a 1.1.8 implementation.
	 *
	 * @return <tt>true</tt> if we are running on 1.1.8, <tt>false</tt>
	 *  otherwise
	 */
    public static boolean isJava118() {
        return _isJava118;
    }

    /**
	 * Returns whether or not the current JVM is 1.3.x or later
	 *
	 * @return <tt>true</tt> if we are running on 1.3.x or later, 
     *  <tt>false</tt> otherwise
	 */
    public static boolean isJava13OrLater() {
        String version = CommonUtils.getJavaVersion();
        return !version.startsWith("1.2") && !version.startsWith("1.1") && !version.startsWith("1.0");
    }

    /**
	 * Returns whether or not the current JVM is 1.4.x or later
	 *
	 * @return <tt>true</tt> if we are running on 1.4.x or later, 
     *  <tt>false</tt> otherwise
	 */
    public static boolean isJava14OrLater() {
        String version = CommonUtils.getJavaVersion();
        return !version.startsWith("1.3") && !version.startsWith("1.2") && !version.startsWith("1.1") && !version.startsWith("1.0");
    }

    /**
   *    * Returns whether or not the current JVM is 1.4.x or later
   *       *
   *          * @return <tt>true</tt> if we are running on 1.4.x or later,
   *               *  <tt>false</tt> otherwise
   *                  */
    public static boolean isJava142OrLater() {
        String version = CommonUtils.getJavaVersion();
        return !version.startsWith("1.4.1") && !version.startsWith("1.4.0") && isJava14OrLater();
    }

    /** 
	 * Attempts to copy the first 'amount' bytes of file 'src' to 'dst',
	 * returning the number of bytes actually copied.  If 'dst' already exists,
	 * the copy may or may not succeed.
     * 
     * @param src the source file to copy
     * @param amount the amount of src to copy, in bytes
     * @param dst the place to copy the file
     * @return the number of bytes actually copied.  Returns 'amount' if the
     *  entire requested range was copied.
     */
    public static int copy(File src, int amount, File dst) {
        final int BUFFER_SIZE = 1024;
        int amountToRead = amount;
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(new FileInputStream(src));
            out = new BufferedOutputStream(new FileOutputStream(dst));
            byte[] buf = new byte[BUFFER_SIZE];
            while (amountToRead > 0) {
                int read = in.read(buf, 0, Math.min(BUFFER_SIZE, amountToRead));
                if (read == -1) break;
                amountToRead -= read;
                out.write(buf, 0, read);
            }
        } catch (IOException e) {
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException e) {
            }
            if (out != null) {
                try {
                    out.flush();
                } catch (IOException e) {
                }
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return amount - amountToRead;
    }

    /** 
	 * Copies the file 'src' to 'dst', returning true iff the copy succeeded.
     * If 'dst' already exists, the copy may or may not succeed.  May also
     * fail for VERY large source files.
	 */
    public static boolean copy(File src, File dst) {
        long length = src.length();
        return copy(src, (int) length, dst) == length;
    }

    /**
     * Returns the user home directory.
     *
     * @return the <tt>File</tt> instance denoting the abstract pathname of
     *  the user's home directory, or <tt>null</tt> if the home directory
	 *  does not exist
     */
    public static File getUserHomeDir() {
        return new File(PROPS.getProperty("user.home"));
    }

    /**
     * Return the user's name.
     *
     * @return the <tt>String</tt> denoting the user's name.
     */
    public static String getUserName() {
        return PROPS.getProperty("user.name");
    }

    public static synchronized File getUserSettingsDir() {
        if (SETTINGS_DIRECTORY != null) return SETTINGS_DIRECTORY;
        File settingsDir = new File(getUserHomeDir(), LIMEWIRE_PREFS_DIR_NAME);
        if (CommonUtils.isMacOSX()) {
            File tempSettingsDir = new File(getUserHomeDir(), "Library/Preferences");
            settingsDir = new File(tempSettingsDir, "LionShare");
        }
        if (!settingsDir.isDirectory()) {
            settingsDir.delete();
            if (!settingsDir.mkdirs()) {
                String msg = "could not create preferences directory: " + settingsDir;
                throw new RuntimeException(msg);
            }
        }
        if (!settingsDir.canWrite()) {
            throw new RuntimeException("settings dir not writable");
        }
        if (!settingsDir.canRead()) {
            throw new RuntimeException("settings dir not readable");
        }
        moveWindowsFiles(settingsDir);
        moveXMLFiles(settingsDir);
        SETTINGS_DIRECTORY = settingsDir;
        return settingsDir;
    }

    /**
     * Boolean for whether or not the windows files have been copied.
     */
    private static boolean _windowsFilesMoved = false;

    /**
     * Boolean for whether or not XML files have been copied.
     */
    private static boolean _xmlFilesMoved = false;

    /**
     * The array of files that should be stored in the user's home 
     * directory.
     */
    private static final String[] USER_FILES = { "limewire.props", "gnutella.net", "fileurns.cache" };

    /**
     * On Windows, this copies files from the current directory to the
     * user's LimeWire home directory.  The installer does not have
     * access to the user's home directory, so these files must be
     * copied.  Note that they are only copied, however, if existing 
     * files are not there.  This ensures that the most recent files,
     * and the files that should be used, should always be saved in 
     * the user's home LimeWire preferences directory.
     */
    private static synchronized void moveWindowsFiles(File settingsDir) {
        if (!isWindows()) return;
        if (_windowsFilesMoved) return;
        File currentDir = CommonUtils.getCurrentDirectory();
        for (int i = 0; i < USER_FILES.length; i++) {
            File curUserFile = new File(settingsDir, USER_FILES[i]);
            File curDirFile = new File(currentDir, USER_FILES[i]);
            if (curUserFile.isFile()) {
                continue;
            }
            if (!copy(curDirFile, curUserFile)) {
                throw new RuntimeException();
            }
        }
        _windowsFilesMoved = true;
    }

    /**
     * Old metadata definitions must be moved from ./lib/xml/data/*.*
     * This is done like the windows files copying, but for all files
     * in the data directory.
     */
    private static synchronized void moveXMLFiles(File settingsDir) {
        if (_xmlFilesMoved) return;
        File currentDir = new File(CommonUtils.getCurrentDirectory().getPath() + "/lib/xml/data");
        settingsDir = new File(settingsDir.getPath() + "/xml/data");
        settingsDir.mkdirs();
        String[] filesToMove = currentDir.list();
        if (filesToMove != null) {
            for (int i = 0; i < filesToMove.length; i++) {
                File curUserFile = new File(settingsDir, filesToMove[i]);
                File curDirFile = new File(currentDir, filesToMove[i]);
                if (curUserFile.isFile()) {
                    continue;
                }
                copy(curDirFile, curUserFile);
            }
        }
        _xmlFilesMoved = true;
    }

    /**
	 * Returns whether or not the QuickTime libraries are available
	 * on the user's system.
	 *
	 * @return <tt>true</tt> if the QuickTime libraries are available,
	 *  <tt>false</tt> otherwise
	 */
    public static boolean isQuickTimeAvailable() {
        return CommonUtils.isMacOSX();
    }

    /**
	 * Returns whether or not the specified file extension is supported in 
	 * our implementation of QuickTime.  So, this will only return 
	 * <tt>true</tt> if both QuickTime supports the extension in general, 
	 * and if our QuickTime implementation supports the extension.
	 *
	 * @param ext the extension to check for QuickTime support
	 * @return <tt>true</tt> if QuickTime supports the file type and our 
	 *  implementation of QuickTime supports that part of QuickTime's 
	 *  functionality, <tt>false</tt> otherwise
	 */
    public static boolean isQuickTimeSupportedFormat(File file) {
        String fileName = file.getName();
        if (fileName.equals("") || fileName.length() < 4) {
            return false;
        }
        int i = fileName.lastIndexOf(".");
        if (i == -1 || i == fileName.length()) return false;
        String ext = fileName.substring(i + 1).toLowerCase(Locale.US);
        String[] supportedFormats = { "mp3", "wav", "au", "aif", "aiff" };
        for (int r = 0; r < supportedFormats.length; r++) {
            if (ext.equals(supportedFormats[r])) return true;
        }
        return false;
    }

    /**
	 * Convenience method that checks both that the QuickTime for Java
	 * libraries are available and that we can launch the specified 
	 * file using QuickTime.
	 *
	 * @return <tt>true</tt> if the QuickTime for Java libraries are
	 *  available and the file is of a type that our QuickTime players
	 *  support, <tt>false</tt> otherwise
	 */
    public static boolean canLaunchFileWithQuickTime(File file) {
        if (!isQuickTimeAvailable()) return false;
        return isQuickTimeSupportedFormat(file);
    }

    /**
     * Gets a resource file using the CommonUtils class loader,
     * or the system class loader if CommonUtils isn't loaded.
     */
    public static File getResourceFile(String location) {
        ClassLoader cl = CommonUtils.class.getClassLoader();
        URL resource = null;
        if (cl == null) {
            resource = ClassLoader.getSystemResource(location);
        } else {
            resource = cl.getResource(location);
        }
        if (resource == null) {
            return new File(location);
        }
        return new File(decode(resource.getFile()));
    }

    /**
     * Gets an InputStream from a resource file.
     * 
     * @param location the location of the resource in the resource file
     * @return an <tt>InputStream</tt> for the resource
     * @throws IOException if the resource could not be located or there was
     *  another IO error accessing the resource
     */
    public static InputStream getResourceStream(String location) throws IOException {
        ClassLoader cl = CommonUtils.class.getClassLoader();
        URL resource = null;
        if (cl == null) {
            resource = ClassLoader.getSystemResource(location);
        } else {
            resource = cl.getResource(location);
        }
        if (resource == null) throw new IOException("null resource: " + location); else return resource.openStream();
    }

    /**
     * Copied from URLDecoder.java
     */
    public static String decode(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch(c) {
                case '+':
                    sb.append(' ');
                    break;
                case '%':
                    try {
                        sb.append((char) Integer.parseInt(s.substring(i + 1, i + 3), 16));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(s);
                    }
                    i += 2;
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        String result = sb.toString();
        try {
            byte[] inputBytes = result.getBytes("8859_1");
            result = new String(inputBytes);
        } catch (UnsupportedEncodingException e) {
        }
        return result;
    }

    /**
	 * Copies the specified resource file into the current directory from
	 * the jar file. If the file already exists, no copy is performed.
	 *
	 * @param fileName the name of the file to copy, relative to the jar 
	 *  file -- such as "com/limegroup/gnutella/gui/images/image.gif"
	 */
    public static void copyResourceFile(final String fileName) {
        copyResourceFile(fileName, null);
    }

    /**
	 * Copies the specified resource file into the current directory from
	 * the jar file. If the file already exists, no copy is performed.
	 *
	 * @param fileName the name of the file to copy, relative to the jar
	 *  file -- such as "com/limegroup/gnutella/gui/images/image.gif"
     * @param newFile the new <tt>File</tt> instance where the resource file
     *  will be copied to
	 */
    public static void copyResourceFile(final String fileName, File newFile) {
        copyResourceFile(fileName, newFile, false);
    }

    /**
	 * Copies the specified resource file into the current directory from
	 * the jar file. If the file already exists, no copy is performed.
	 *
	 * @param fileName the name of the file to copy, relative to the jar 
	 *  file -- such as "com/limegroup/gnutella/gui/images/image.gif"
     * @param newFile the new <tt>File</tt> instance where the resource file
     *  will be copied to -- if this argument is null, the file will be
     *  copied to the current directory
     * @param forceOverwrite specifies whether or not to overwrite the 
     *  file if it already exists
	 */
    public static void copyResourceFile(final String fileName, File newFile, final boolean forceOverwrite) {
        if (newFile == null) newFile = new File(".", fileName);
        if (!forceOverwrite && newFile.exists()) return;
        String parentString = newFile.getParent();
        if (parentString == null) {
            return;
        }
        File parentFile = new File(parentString);
        if (!parentFile.isDirectory()) {
            parentFile.mkdirs();
        }
        ClassLoader cl = CommonUtils.class.getClassLoader();
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            URL resource = cl != null ? cl.getResource(fileName) : ClassLoader.getSystemResource(fileName);
            if (resource == null) throw new NullPointerException("resource: " + fileName + " doesn't exist.");
            InputStream is = resource.openStream();
            final int bufferSize = 2048;
            bis = new BufferedInputStream(is, bufferSize);
            bos = new BufferedOutputStream(new FileOutputStream(newFile), bufferSize);
            byte[] buffer = new byte[bufferSize];
            int c = 0;
            do {
                c = bis.read(buffer, 0, bufferSize);
                if (c > 0) bos.write(buffer, 0, c);
            } while (c == bufferSize);
        } catch (IOException e) {
            newFile.delete();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ignored) {
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /** 
     * Replaces OS specific illegal characters from any filename with '_', 
	 * including ( / \n \r \t ) on all operating systems, ( ? * \  < > | " ) 
	 * on Windows, ( ` ) on unix.
     *
     * @param name the filename to check for illegal characters
     * @return String containing the cleaned filename
     */
    public static String convertFileName(String name) {
        if (name.length() > 180) {
            int extStart = name.lastIndexOf('.');
            if (extStart == -1) {
                name = name.substring(0, 180);
            } else {
                int extLength = name.length() - extStart;
                int extEnd = extLength > 11 ? extStart + 11 : name.length();
                name = name.substring(0, 180 - extLength) + name.substring(extStart, extEnd);
            }
        }
        for (int i = 0; i < ILLEGAL_CHARS_ANY_OS.length; i++) name = name.replace(ILLEGAL_CHARS_ANY_OS[i], '_');
        if (_isWindows) {
            for (int i = 0; i < ILLEGAL_CHARS_WINDOWS.length; i++) name = name.replace(ILLEGAL_CHARS_WINDOWS[i], '_');
        } else if (_isLinux || _isSolaris) {
            for (int i = 0; i < ILLEGAL_CHARS_UNIX.length; i++) name = name.replace(ILLEGAL_CHARS_UNIX[i], '_');
        }
        return name;
    }

    /**
     * Utility method for determining whether or not we should record 
     * statistics.  This can depend on the operating system or other factors.
     * For example, the OS 9 implementation of Java 118 appears not to handle
     * classloading correctly, and therefore cannot properly load the statistics
     * recording classes.
     * 
     * @return <tt>true</tt> if we should record statistics, otherwise 
     *  <tt>false</tt>
     */
    public static boolean recordStats() {
        return !CommonUtils.isJava118();
    }

    public static boolean isJava15OrLater() {
        String version = CommonUtils.getJavaVersion();
        return !version.startsWith("1.4") && !version.startsWith("1.3") && !version.startsWith("1.2") && !version.startsWith("1.1") && !version.startsWith("1.0");
    }
}
