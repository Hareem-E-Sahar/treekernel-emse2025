package net.sf.jode.decompiler;

import net.sf.jode.bytecode.ClassInfo;
import net.sf.jode.bytecode.ClassPath;
import net.sf.jode.bytecode.ClassFormatException;
import net.sf.jode.GlobalOptions;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

public class Main extends Options {

    private static final int OPTION_START = 0x10000;

    private static final int OPTION_END = 0x20000;

    private static final LongOpt[] longOptions = new LongOpt[] { new LongOpt("cp", LongOpt.REQUIRED_ARGUMENT, null, 'c'), new LongOpt("classpath", LongOpt.REQUIRED_ARGUMENT, null, 'c'), new LongOpt("dest", LongOpt.REQUIRED_ARGUMENT, null, 'd'), new LongOpt("keep-going", LongOpt.NO_ARGUMENT, null, 'k'), new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'), new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'V'), new LongOpt("verbose", LongOpt.OPTIONAL_ARGUMENT, null, 'v'), new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'D'), new LongOpt("import", LongOpt.REQUIRED_ARGUMENT, null, 'i'), new LongOpt("style", LongOpt.REQUIRED_ARGUMENT, null, 's'), new LongOpt("chars-per-line", LongOpt.REQUIRED_ARGUMENT, null, 'l'), new LongOpt("lvt", LongOpt.OPTIONAL_ARGUMENT, null, OPTION_START + 0), new LongOpt("inner", LongOpt.OPTIONAL_ARGUMENT, null, OPTION_START + 1), new LongOpt("anonymous", LongOpt.OPTIONAL_ARGUMENT, null, OPTION_START + 2), new LongOpt("push", LongOpt.OPTIONAL_ARGUMENT, null, OPTION_START + 3), new LongOpt("pretty", LongOpt.OPTIONAL_ARGUMENT, null, OPTION_START + 4), new LongOpt("decrypt", LongOpt.OPTIONAL_ARGUMENT, null, OPTION_START + 5), new LongOpt("onetime", LongOpt.OPTIONAL_ARGUMENT, null, OPTION_START + 6), new LongOpt("immediate", LongOpt.OPTIONAL_ARGUMENT, null, OPTION_START + 7), new LongOpt("verify", LongOpt.OPTIONAL_ARGUMENT, null, OPTION_START + 8), new LongOpt("contrafo", LongOpt.OPTIONAL_ARGUMENT, null, OPTION_START + 9) };

    public static void usage() {
        PrintWriter err = GlobalOptions.err;
        err.println("Version: " + GlobalOptions.version);
        err.println("Usage: java net.sf.jode.decompiler.Main [OPTION]* {CLASS|JAR}*");
        err.println("Give a fully qualified CLASS name, e.g. net.sf.jode.decompiler.Main, if you want to");
        err.println("decompile a single class, or a JAR file containing many classes.");
        err.println("OPTION is any of these:");
        err.println("  -h, --help           " + "show this information.");
        err.println("  -V, --version        " + "output version information and exit.");
        err.println("  -v, --verbose        " + "be verbose (multiple times means more verbose).");
        err.println("  -c, --classpath <path> " + "search for classes in specified classpath.");
        err.println("                       " + "The directories should be separated by ','.");
        err.println("  -d, --dest <dir>     " + "write decompiled files to disk into directory destdir.");
        err.println("  -s, --style {sun|gnu|pascal|python}  " + "specify indentation style");
        err.println("  -l, --chars-per-line <number>  " + "specify line length");
        err.println("  -i, --import <pkglimit>,<clslimit>");
        err.println("                       " + "import classes used more than clslimit times");
        err.println("                       " + "and packages with more then pkglimit used classes.");
        err.println("                       " + "Limit 0 means never import. Default is 0,1.");
        err.println("  -k, --keep-going     " + "After an error continue to decompile the other classes.");
        err.println("                       " + "after an error decompiling one of them.");
    }

    public static boolean handleOption(int option, int longind, String arg) {
        if (arg == null) options ^= 1 << option; else if ("yes".startsWith(arg) || arg.equals("on")) options |= 1 << option; else if ("no".startsWith(arg) || arg.equals("off")) options &= ~(1 << option); else {
            GlobalOptions.err.println("net.sf.jode.decompiler.Main: option --" + longOptions[longind].getName() + " takes one of `yes', `no', `on', `off' as parameter");
            return false;
        }
        return true;
    }

    public static boolean decompileClass(String className, ClassPath classPath, String classPathStr, ZipOutputStream destZip, String destDir, TabbedPrintWriter writer, ImportHandler imports) {
        try {
            ClassInfo clazz;
            try {
                clazz = classPath.getClassInfo(className);
            } catch (IllegalArgumentException ex) {
                GlobalOptions.err.println("`" + className + "' is not a class name");
                return false;
            }
            if (skipClass(clazz)) return true;
            String filename = className.replace('.', File.separatorChar) + ".java";
            if (destZip != null) {
                writer.flush();
                destZip.putNextEntry(new ZipEntry(filename));
            } else if (destDir != null) {
                File file = new File(destDir, filename);
                File directory = new File(file.getParent());
                if (!directory.exists() && !directory.mkdirs()) {
                    GlobalOptions.err.println("Could not create directory " + directory.getPath() + ", check permissions.");
                }
                writer = new TabbedPrintWriter(new BufferedOutputStream(new FileOutputStream(file)), imports, false);
            }
            GlobalOptions.err.println(className);
            ClassAnalyzer clazzAna = new ClassAnalyzer(clazz, imports);
            clazzAna.dumpJavaFile(writer);
            if (destZip != null) {
                writer.flush();
                destZip.closeEntry();
            } else if (destDir != null) writer.close();
            System.gc();
            return true;
        } catch (FileNotFoundException ex) {
            GlobalOptions.err.println("Can't read " + ex.getMessage() + ".");
            GlobalOptions.err.println("Check the class path (" + classPathStr + ") and check that you use the java class name.");
            return false;
        } catch (ClassFormatException ex) {
            GlobalOptions.err.println("Error while reading " + className + ".");
            ex.printStackTrace(GlobalOptions.err);
            return false;
        } catch (IOException ex) {
            GlobalOptions.err.println("Can't write source of " + className + ".");
            GlobalOptions.err.println("Check the permissions.");
            ex.printStackTrace(GlobalOptions.err);
            return false;
        } catch (RuntimeException ex) {
            GlobalOptions.err.println("Error whilst decompiling " + className + ".");
            ex.printStackTrace(GlobalOptions.err);
            return false;
        } catch (InternalError ex) {
            GlobalOptions.err.println("Internal error whilst decompiling " + className + ".");
            ex.printStackTrace(GlobalOptions.err);
            return false;
        }
    }

    public static void main(String[] params) {
        try {
            decompile(params);
        } catch (ExceptionInInitializerError ex) {
            ex.getException().printStackTrace();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }

    public static void decompile(String[] params) {
        if (params.length == 0) {
            usage();
            return;
        }
        ClassPath classPath;
        String classPathStr = System.getProperty("java.class.path").replace(File.pathSeparatorChar, ClassPath.altPathSeparatorChar);
        String bootClassPath = System.getProperty("sun.boot.class.path");
        if (bootClassPath != null) classPathStr = classPathStr + ClassPath.altPathSeparatorChar + bootClassPath.replace(File.pathSeparatorChar, ClassPath.altPathSeparatorChar);
        String destDir = null;
        int importPackageLimit = ImportHandler.DEFAULT_PACKAGE_LIMIT;
        int importClassLimit = ImportHandler.DEFAULT_CLASS_LIMIT;
        ;
        int outputStyle = TabbedPrintWriter.BRACE_AT_EOL;
        int indentSize = 4;
        int outputLineLength = 79;
        boolean keepGoing = false;
        GlobalOptions.err.println(GlobalOptions.copyright);
        boolean errorInParams = false;
        Getopt g = new Getopt("net.sf.jode.decompiler.Main", params, "hVvkc:d:D:i:s:l:", longOptions, true);
        for (int opt = g.getopt(); opt != -1; opt = g.getopt()) {
            switch(opt) {
                case 0:
                    break;
                case 'h':
                    usage();
                    errorInParams = true;
                    break;
                case 'V':
                    GlobalOptions.err.println(GlobalOptions.version);
                    break;
                case 'c':
                    classPathStr = g.getOptarg();
                    break;
                case 'd':
                    destDir = g.getOptarg();
                    break;
                case 'k':
                    keepGoing = true;
                    break;
                case 'v':
                    {
                        String arg = g.getOptarg();
                        if (arg == null) GlobalOptions.verboseLevel++; else {
                            try {
                                GlobalOptions.verboseLevel = Integer.parseInt(arg);
                            } catch (NumberFormatException ex) {
                                GlobalOptions.err.println("net.sf.jode.decompiler.Main: Argument `" + arg + "' to --verbose must be numeric:");
                                errorInParams = true;
                            }
                        }
                        break;
                    }
                case 'D':
                    {
                        String arg = g.getOptarg();
                        if (arg == null) arg = "help";
                        errorInParams |= !GlobalOptions.setDebugging(arg);
                        break;
                    }
                case 's':
                    {
                        String arg = g.getOptarg();
                        if (arg.equals("gnu")) {
                            outputStyle = TabbedPrintWriter.GNU_SPACING | TabbedPrintWriter.INDENT_BRACES;
                            indentSize = 2;
                        } else if (arg.equals("sun")) {
                            outputStyle = TabbedPrintWriter.BRACE_AT_EOL;
                            indentSize = 4;
                        } else if (arg.equals("pascal")) {
                            outputStyle = 0;
                            indentSize = 4;
                        } else if (arg.equals("python") || arg.equals("codd")) {
                            outputStyle = TabbedPrintWriter.BRACE_AT_EOL | TabbedPrintWriter.CODD_FORMATTING;
                            indentSize = 4;
                        } else {
                            GlobalOptions.err.println("net.sf.jode.decompiler.Main: Unknown style `" + arg + "'.");
                            errorInParams = true;
                        }
                        break;
                    }
                case 'l':
                    {
                        String arg = g.getOptarg();
                        try {
                            outputLineLength = Integer.parseInt(arg.trim());
                        } catch (RuntimeException rte) {
                            GlobalOptions.err.println("net.sf.jode.decompiler.Main: Invalid Linelength " + arg);
                            errorInParams = true;
                        }
                        break;
                    }
                case 'i':
                    {
                        String arg = g.getOptarg();
                        int comma = arg.indexOf(',');
                        try {
                            int packLimit = Integer.parseInt(arg.substring(0, comma));
                            if (packLimit == 0) packLimit = Integer.MAX_VALUE;
                            if (packLimit < 0) throw new IllegalArgumentException();
                            int clazzLimit = Integer.parseInt(arg.substring(comma + 1));
                            if (clazzLimit == 0) clazzLimit = Integer.MAX_VALUE;
                            if (clazzLimit < 0) throw new IllegalArgumentException();
                            importPackageLimit = packLimit;
                            importClassLimit = clazzLimit;
                        } catch (RuntimeException ex) {
                            GlobalOptions.err.println("net.sf.jode.decompiler.Main: Invalid argument for -i option.");
                            errorInParams = true;
                        }
                        break;
                    }
                default:
                    if (opt >= OPTION_START && opt <= OPTION_END) {
                        errorInParams |= !handleOption(opt - OPTION_START, g.getLongind(), g.getOptarg());
                    } else errorInParams = true;
                    break;
            }
        }
        if (errorInParams) return;
        classPath = new ClassPath(classPathStr);
        ImportHandler imports = new ImportHandler(classPath, importPackageLimit, importClassLimit);
        ZipOutputStream destZip = null;
        TabbedPrintWriter writer = null;
        if (destDir == null) writer = new TabbedPrintWriter(System.out, imports, true, outputStyle, indentSize, 0, outputLineLength); else if (destDir.toLowerCase().endsWith(".zip") || destDir.toLowerCase().endsWith(".jar")) {
            try {
                destZip = new ZipOutputStream(new FileOutputStream(destDir));
            } catch (IOException ex) {
                GlobalOptions.err.println("Can't open zip file " + destDir);
                ex.printStackTrace(GlobalOptions.err);
                return;
            }
            writer = new TabbedPrintWriter(new BufferedOutputStream(destZip), imports, false, outputStyle, indentSize, 0, outputLineLength);
        }
        for (int i = g.getOptind(); i < params.length; i++) {
            try {
                if ((params[i].endsWith(".jar") || params[i].endsWith(".zip")) && new File(params[i]).isFile()) {
                    ClassPath zipClassPath = new ClassPath(params[i], classPath);
                    Enumeration enumeration = new ZipFile(params[i]).entries();
                    while (enumeration.hasMoreElements()) {
                        String entry = ((ZipEntry) enumeration.nextElement()).getName();
                        if (entry.endsWith(".class")) {
                            entry = entry.substring(0, entry.length() - 6).replace('/', '.');
                            if (!decompileClass(entry, zipClassPath, classPathStr, destZip, destDir, writer, imports) && !keepGoing) break;
                        }
                    }
                } else {
                    if (!decompileClass(params[i], classPath, classPathStr, destZip, destDir, writer, imports) && !keepGoing) break;
                }
            } catch (IOException ex) {
                GlobalOptions.err.println("Can't read zip file " + params[i] + ".");
                ex.printStackTrace(GlobalOptions.err);
            }
        }
        if (destZip != null) {
            try {
                destZip.close();
            } catch (IOException ex) {
                GlobalOptions.err.println("Can't close Zipfile");
                ex.printStackTrace(GlobalOptions.err);
            }
        }
    }
}
