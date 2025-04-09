import java.util.*;
import java.io.*;
import java.awt.*;

class ecl {

    static String version = "ECL compiler version 3.1.0";

    static table tableManager;

    static checkECL checker;

    static boolean doInit = false;

    static structuredStream outES;

    static int lastLineNum = -1;

    static Yylex scanner;

    static common_ecl_xecl argEcl;

    static optionConstant Const;

    public static void main(String argv[]) throws java.io.IOException, java.lang.Exception {
        String lcvDir = null, lcDir = null, base = null;
        Hashtable paramTable = new Hashtable();
        Hashtable cdslibTable = new Hashtable();
        FileInputStream ec;
        String cc, dirsep;
        Const = new optionConstant();
        ecl.checker = new checkECL();
        argEcl = new common_ecl_xecl();
        argEcl.setOS();
        if (argEcl.nt) dirsep = "\\"; else dirsep = "/";
        argEcl.parserCommandLine(argv, false);
        if (argEcl.name == null) {
            System.err.println("Missing source file name");
            printUsage(false);
        }
        if (argEcl.vccWrapper || argEcl.vccXEcl) {
            if (argEcl.workspace == null) {
                if (argEcl.nt) {
                    System.err.println("Warning: assuming c:\\MyWorkspace as workspace");
                    argEcl.workspace = "c:\\MyWorkspace";
                } else {
                    System.err.println("fatal: no workspace default under UNIX (must use -WORKSPACE)");
                    printUsage(false);
                }
            }
        }
        if (argEcl.vccNoImport) {
            argEcl.vccImport = false;
        }
        if (lcDir == null) {
            lcDir = new String("");
            lcvDir = new String("");
        }
        if (argEcl.workspace != null) {
            cdslib.readCdslib(argEcl.workspace, cdslibTable);
        }
        if (argEcl.vccWrapper || argEcl.vccXEcl) {
            StringTokenizer celltok = new StringTokenizer(argEcl.cellib, ".");
            String lib = celltok.nextToken();
            lcDir = (String) cdslibTable.get(lib);
            if (lcDir == null) {
                System.err.println("Error: cannot find '" + lib + "' in cds.lib");
                printUsage(false);
            }
            lcDir = lcDir + dirsep;
            lcDir = lcDir + celltok.nextToken();
            lcvDir = lcDir + dirsep + Const.VCC_ECL_DIR + dirsep;
            param.readParam(lcDir + dirsep + "interface" + dirsep + "cell.face", paramTable);
        }
        if (argEcl.printDebug && (argEcl.vccWrapper || argEcl.vccXEcl || argEcl.polisWrapper)) argEcl.dummySimul = true;
        StringTokenizer tokenizer = new StringTokenizer(argEcl.name, ".");
        base = tokenizer.nextToken();
        cc = System.getProperties().getProperty("CC");
        if (cc == null) {
            if (argEcl.nt) cc = "cl"; else cc = "cc";
        }
        if (argEcl.typelibFile != null) {
            typelib.readTypelib(lcvDir + argEcl.typelibFile, argEcl.typelibTable);
        }
        if (argEcl.clean) {
            cleanFiles(lcvDir + base, lcvDir);
            return;
        }
        if (argEcl.strlSimul || argEcl.regTest) {
            argEcl.macros.addElement(new String(Const.MACRO_SIMUL_OPT));
            argEcl.values.addElement(new String(""));
        }
        if (argEcl.runCpp) {
            runPreprocessor(lcvDir + argEcl.name, lcvDir + base, cc, argEcl.macros, argEcl.values, argEcl.includes, argEcl.typelibTable, cdslibTable, argEcl.workspace);
        }
        if (argEcl.mainmodule == null) argEcl.mainmodule = base;
        runParser(lcvDir + base, argEcl.mainmodule, argEcl.debug_parse_flag, argEcl.vccWrapper || argEcl.vccXEcl);
        extractModule();
        if (ecl.checker.getCheck()) eclCheck(base);
        eclCompile(base, lcvDir, argEcl.mainmodule, argEcl.vccWrapper || argEcl.vccXEcl, argEcl.vccImport, argEcl.polisWrapper, argEcl.typelibTable, paramTable);
        if (argEcl.strlSimul || argEcl.regTest || argEcl.vccWrapper || argEcl.polisWrapper) {
            runEsterelCompiler(lcvDir + base, lcvDir, argEcl.strlSimul || argEcl.regTest || (argEcl.vccWrapper || argEcl.polisWrapper) && argEcl.printDebug);
        }
        if (argEcl.strlSimul || argEcl.regTest) {
            runCCompiler(lcvDir + base, cc);
        }
        if (argEcl.regTest) {
            runExeAndCompare(lcvDir + base);
            System.out.println("Regression test for " + argEcl.name + " succeeded");
        }
        if (argEcl.vccImport) {
            runVCCImport(lcvDir, lcDir, dirsep, argEcl.cellib, argEcl.vccImportTypes);
        } else if (argEcl.vccWrapper) {
            copyVCCFiles(argEcl.name, base, lcvDir, lcDir, cc);
        }
    }

    static void printUsage(boolean full) {
        System.err.println("Usage: ecl [options] source_file");
        if (!full) {
            System.err.println("  (ecl -H prints the complete list of options)");
            System.exit(-1);
        }
        System.err.println("  Compiler mode options:");
        System.err.println("    -ESTEREL: generate Esterel simulation model");
        System.err.println("    -VCC lib.cell: generate VCC whitebox model");
        System.err.println("         Internal option: for use inside VCC.");
        System.err.println("         Must have only one module in the ECL file,");
        System.err.println("          or must have a module with the same name as the base filename");
        System.err.println("          or must specify main module with the -MAINMODULE option");
        System.err.println("         Must be in the lib/cell/clr_ecl directory with file.ecl there.");
        System.err.println("    -CLEAN: remove all compiler-generated files");
        System.err.println("  General options:");
        System.err.println("    -G: enable source-level debugging");
        System.err.println("    -P: use C procedures for extraction (default)");
        System.err.println("    -F: use C functions for extraction");
        System.err.println("    -C: prefer C over Esterel when splitting (default)");
        System.err.println("    -E: prefer Esterel over C when splitting");
        System.err.println("    -CHECK: do a simple ECL-usage check on the input file");
        System.err.println("  C preprocessor options:");
        System.err.println("    -D macro value: define macro");
        System.err.println("    -I directory: add #include directory");
        System.err.println("  Esterel simulation options:");
        System.err.println("    -SF: generate empty signal I/O functions");
        System.err.println("  VCC simulation options (internal, for use inside VCC):");
        System.err.println("    -VCC lib.cell: generate VCC whitebox model");
        System.err.println("       and import it to the specified lib cell");
        System.err.println("         Must have only one module in the ECL file,");
        System.err.println("          or must have a module with the same name as the base filename,");
        System.err.println("          or must specify main module with the -MAINMODULE option");
        System.err.println("         Must be in the lib/cell/clr_ecl directory with file.ecl there.");
        System.err.println("    -MAINMODULE name: specifies the name of the main module used in VCC import ");
        System.err.println("    -DEFAULTTYPELIB lib: default VCC library to read or import user-defined types");
        System.err.println("    -TYPELIB lib type: for a given user-defined type name, read from or import to");
        System.err.println("       the given lib in VCC");
        System.err.println("    -TYPELIBFILE filename: read or import VCC user-defined types for all the types");
        System.err.println("       in filename; the format is a list of TYPELIB arguments in the form lib=type");
        System.err.println("    -WORKSPACE root: specifies the VCC workspace pathname ");
        System.err.println("    -NOIMPORT: do not import VCC whitebox model");
        System.err.println("       but just copy the files to the specified workspace");
        System.err.println("       (valid after initial import if interface did not change)");
        System.err.println("    -IMPORTTYPES: for all types not found in given VCC libraries, import them");
        System.err.println("       as VCC types");
        System.err.println("  Internal options (compiler debugging)");
        System.err.println("    -NOCPP: do not execute the C preprocessor");
        System.err.println("    -TEST: run regression test on file");
        System.err.println("    -PD:   generate output to debug ECL parser");
        System.err.println("    -CD:   generate output to debug ECL compiler");
        System.err.println("    -NG    do not group statements in C");
        System.exit(0);
    }

    static void cleanFiles(String base, String lcvDir) throws java.lang.Exception {
        String command;
        if (argEcl.nt) {
            command = "cmd /c del " + base + ".c " + base + ".h";
            runCommand(command, null, null);
            command = "cmd /c del " + base + ".exe " + base + ".i " + base + ".obj";
            runCommand(command, null, null);
            command = "cmd /c del " + base + ".strl " + base + ".test " + base + ".est";
            runCommand(command, null, null);
            if (lcvDir != null && !lcvDir.equals("")) {
                command = "cmd /c del " + lcvDir + " " + Const.VCC_LIBRARY_FILE + lcvDir + "white.c ";
                runCommand(command, null, null);
            }
        } else {
            command = "rm -f " + base + ".c " + base + ".h";
            runCommand(command, null, null);
            command = "rm -f " + base + ".exe " + base + ".i " + base + ".o";
            runCommand(command, null, null);
            command = "rm -f " + base + ".strl " + base + ".test " + base + ".est";
            runCommand(command, null, null);
            if (lcvDir != null && !lcvDir.equals("")) {
                command = "rm -f " + lcvDir + " " + Const.VCC_LIBRARY_FILE + lcvDir + "white.c ";
                runCommand(command, null, null);
            }
        }
    }

    static void runPreprocessor(String name, String base, String cc, Vector macros, Vector values, Vector includes, Hashtable typelibTable, Hashtable cdslibTable, String workspace) throws java.lang.Exception {
        String command;
        if (argEcl.nt && !cc.equals("gcc")) {
            command = cc + Const.CPRE_OPT_NT;
        } else if (argEcl.linux || cc == "gcc") {
            command = cc + Const.CPRE_OPT_LINUX;
        } else {
            command = cc + Const.CPRE_OPT_UX;
        }
        for (int i = 0; i < includes.size(); i++) {
            String include = (String) includes.elementAt(i);
            if (include != null && include.length() != 0) {
                if (argEcl.nt && !cc.equals("gcc")) {
                    include = include.replace('/', '\\');
                    command = command + " /I" + include;
                } else {
                    include = include.replace('\\', '/');
                    command = command + " -I" + include;
                }
            }
        }
        Enumeration libs = typelibTable.keys();
        while (libs.hasMoreElements()) {
            String type = (String) libs.nextElement();
            if (type.equals("*")) continue;
            if (workspace == null) {
                System.err.println("Error: -TYPELIB specified without the workspace pathname");
                printUsage(false);
            }
            String lib = (String) typelibTable.get(type);
            String dir = (String) cdslibTable.get(lib);
            if (dir == null) {
                System.err.println("Error: library " + lib + " is not in the cds.lib file");
                System.exit(-1);
            }
            if (argEcl.nt && !cc.equals("gcc")) dir = dir.replace('/', '\\'); else dir = dir.replace('\\', '/');
            if (argEcl.nt && !cc.equals("gcc")) {
                command = command + " /I" + dir + "\\" + type + "\\type_definition";
            } else {
                command = command + " -I" + dir + "/" + type + "/type_definition";
            }
        }
        for (int i = 0; i < macros.size(); i++) {
            String macro = (String) macros.elementAt(i);
            String value = (String) values.elementAt(i);
            if (macro != null && macro.length() != 0) {
                if (argEcl.nt && !cc.equals("gcc")) {
                    command = command + " /D" + macro;
                } else {
                    command = command + " -D" + macro;
                }
                if (value != null && value.length() != 0) {
                    command = command + "=" + value;
                }
            }
        }
        if (argEcl.linux || cc.equals("gcc")) {
            command = command + " -x c ";
        } else {
            command = command + " ";
        }
        command = command + name;
        runCommand(command, base + ".i", null);
    }

    static void runParser(String base, String module_name, boolean debug_parse_flag, boolean vccWrapper) throws java.lang.Exception {
        String command;
        System.out.println("Compiling " + base + ".i");
        tableManager = new table();
        scanner = new Yylex(new FileInputStream(base + ".i"));
        scanner.setTableManager(tableManager);
        parser p = new parser(scanner, tableManager);
        if (debug_parse_flag) {
            p.debug_parse();
        } else {
            p.parse();
        }
        if (!tableManager.checkTags()) {
            scanner.error("Error: There are undefined tags");
        }
        if (scanner.error_count > 0) {
            System.err.println("\n" + scanner.error_count + " parse error(s): exiting...\n");
            System.exit(-1);
        }
        Vector ext_decl = tableManager.v_symbolTable;
        int i;
        identifier decl;
        for (i = 0; i < ext_decl.size(); i++) {
            decl = (identifier) ext_decl.elementAt(i);
            if (decl.getStorageClass() == identifier.MODULE) {
                if (decl.getName().compareTo("main") == 0) {
                    System.err.println("'main' is not allowed as a module name");
                    System.exit(-1);
                }
                char c0 = decl.getName().charAt(0);
                if (c0 == '_') {
                    System.err.println("Module names cannot begin with the '_' character.");
                    System.exit(-1);
                }
            } else {
                if (decl.getName().compareTo("main") == 0) {
                    System.err.println("'main' is not allowed as a function name");
                    System.exit(-1);
                }
            }
        }
        if (vccWrapper) {
            int found = 0, count_modules = 0;
            String this_module = null;
            for (i = 0; i < ext_decl.size(); i++) {
                decl = (identifier) ext_decl.elementAt(i);
                if (decl.getStorageClass() == identifier.MODULE) {
                    if (decl.getName().compareTo(module_name) == 0) {
                        found = 1;
                        count_modules++;
                        this_module = decl.getName();
                    }
                }
            }
            if (count_modules == 1) {
                found = 1;
                module_name = this_module;
            }
            if (found == 1) {
                System.out.println("Module " + module_name + " being used as main module for VCC Whitebox generation.");
            } else {
                System.err.println("Module " + module_name + " not found in source file.");
                System.err.println("See the help for option -VCC for more information.");
                System.exit(-1);
            }
        } else {
            int found = 0;
            for (i = 0; i < ext_decl.size(); i++) {
                decl = (identifier) ext_decl.elementAt(i);
                if (decl.getStorageClass() == identifier.MODULE) {
                    found = 1;
                    break;
                }
            }
            if (found == 0) {
                System.err.println("Error: ECL programs must have at least one module");
                System.exit(-1);
            }
        }
        if (argEcl.debug) System.out.println("Parsing successful");
    }

    static void eclCompile(String base, String lcvDir, String mainmodule, boolean vccWrapper, boolean vccImport, boolean polisWrapper, Hashtable typelibTable, Hashtable paramTable) throws java.lang.Exception {
        String command;
        if (argEcl.debug) System.out.println("Extraction done. Output files");
        structuredStream s_strl = null;
        FileOutputStream data = new FileOutputStream(lcvDir + base + ".h");
        structuredStream s_data = new structuredStream(new PrintWriter(data));
        structuredStream s_polis = null;
        structuredStream s_vcc = null;
        structuredStream s_vtypes = null;
        if (polisWrapper) {
            s_polis = new structuredStream(new PrintWriter(new FileOutputStream(lcvDir + base + ".strl")));
            s_strl = new structuredStream(new PrintWriter(new FileOutputStream(lcvDir + base + "_polis.strl")));
            s_data = new structuredStream(new PrintWriter(new FileOutputStream(lcvDir + base + "_polis.h")));
        } else {
            s_strl = new structuredStream(new PrintWriter(new FileOutputStream(lcvDir + base + ".strl")));
            s_data = new structuredStream(new PrintWriter(new FileOutputStream(lcvDir + base + ".h")));
        }
        if (vccWrapper) {
            s_vcc = new structuredStream(new PrintWriter(new FileOutputStream(lcvDir + "white.c")));
            s_vtypes = new structuredStream(new PrintWriter(new FileOutputStream(lcvDir + Const.VCC_LIBRARY_FILE)));
        }
        if (argEcl.printESTable) {
            ecl.outES = new structuredStream(new PrintWriter(new FileOutputStream(lcvDir + base + ".est")));
        }
        if (vccWrapper) {
            s_vtypes.println("#ifndef ECL_TYPES_DEFINED");
            s_vtypes.println("#define ECL_TYPES_DEFINED");
            s_vtypes.eol();
        }
        s_data.println("#ifndef ECL_DEFINITIONS");
        s_data.println("#define ECL_DEFINITIONS");
        s_data.eol();
        if (argEcl.printDebug) {
            s_data.println("static void debugLine( );");
            if (vccWrapper) {
                s_data.println("#define vccPrintPdxDebugInfo printf");
                s_data.println("#define __OUTPUT");
            }
        }
        Vector ext_decl = tableManager.v_symbolTable;
        for (int i = 0; i < ext_decl.size(); i++) {
            identifier decl = (identifier) ext_decl.elementAt(i);
            if (decl.getStorageClass() == identifier.STYPEDEF) {
                if (vccWrapper) {
                    decl.printVCCext_type(s_vtypes, typelibTable);
                    s_vtypes.eol();
                    if (argEcl.printDebug) {
                        decl.printCext_decl(s_data);
                        s_data.eol();
                    }
                } else {
                    decl.printCext_decl(s_data);
                    s_data.eol();
                }
            } else if (decl.getStorageClass() == identifier.MODULE) {
                decl.printEext_decl(s_strl);
                if (vccWrapper && (decl.getName().compareTo(mainmodule) == 0)) {
                    decl.printVCCext_decl(s_vcc, base, paramTable);
                    if (argEcl.printDebug) {
                        decl.printVCCreadSensors_hack(s_data, base);
                    }
                }
                if (polisWrapper) {
                    decl.printPolisDummyStrl(s_polis, s_data, base);
                }
            } else if (decl.isMacro()) {
                decl.printCext_def(s_data);
            } else if (!decl.getRetType().isEnumElem()) {
                if (vccWrapper) {
                    decl.printCext_decl(s_data);
                    s_data.eol();
                    if (argEcl.printDebug) {
                        decl.printCext_decl(s_vcc);
                        s_vcc.eol();
                    }
                } else {
                    decl.printCext_decl(s_data);
                    s_data.eol();
                }
            }
        }
        if (argEcl.printDebug) {
            printDebugInfo(base, s_data, vccWrapper);
        }
        if (vccWrapper) {
            s_vtypes.println("#endif /* ECL_TYPES_DEFINED */");
        }
        s_data.println("#endif /* ECL_DEFINITIONS */");
        s_strl.close();
        if (polisWrapper) {
            s_polis.close();
        }
        if (vccWrapper) {
            s_vcc.close();
            s_vtypes.close();
        }
        s_data.close();
        data.close();
        if (argEcl.printESTable) {
            ecl.outES.close();
        }
    }

    static void runEsterelCompiler(String base, String lcvDir, boolean simul) throws java.lang.Exception {
        String command, suffix = ".strl";
        if (argEcl.polisWrapper) suffix = "_polis.strl";
        if (simul) {
            if (!lcvDir.equals("")) {
                command = "esterel -strlic:-shared_var -simul -D " + lcvDir + " " + base + suffix;
            } else {
                command = "esterel -strlic:-shared_var -simul " + base + suffix;
            }
        } else {
            if (!lcvDir.equals("")) {
                command = "esterel -strlic:-shared_var -D " + lcvDir + " " + base + suffix;
            } else {
                command = "esterel -strlic:-shared_var " + base + suffix;
            }
        }
        if (argEcl.nt) command = "cmd /c " + command;
        runCommand(command, null, null);
    }

    static void runCCompiler(String base, String cc) throws java.lang.Exception {
        String command;
        if (argEcl.nt && !cc.equals("gcc")) {
            command = cc + Const.CCOMPILER_OPT_OBJ_NT + "/c " + base + ".c ";
        } else {
            command = cc + Const.CCOMPILER_OPT_OBJ_UX + base + ".c ";
        }
        runCommand(command, null, null);
        if (argEcl.nt && !cc.equals("gcc")) {
            command = cc + Const.CCOMPILER_OPT_OBJ_NT + base + ".obj " + Const.CCOMPILER_OPT_LINK1_NT + System.getProperties().getProperty("ESTEREL") + Const.CCOMPILER_OPT_LINK2_NT;
        } else {
            command = cc + Const.CCOMPILER_OPT_LINK_UX + base + ".exe " + base + ".o " + System.getProperties().getProperty("ESTEREL") + Const.CCOMPILER_OPT_LIB;
        }
        runCommand(command, null, null);
    }

    static void runExeAndCompare(String base) throws java.lang.Exception {
        String command;
        command = base + ".exe -novarcheck " + base + ".in";
        runCommand(command, base + ".test", null);
        if (argEcl.nt) {
            command = "fc " + base + ".gd " + base + ".test";
        } else {
            command = "cmp " + base + ".gd " + base + ".test";
        }
        runCommand(command, null, null);
    }

    static void runVCCImport(String lcvDir, String lcDir, String dirsep, String cellib, boolean vccImportTypes) throws java.lang.Exception {
        String command;
        if (vccImportTypes) {
            if (argEcl.nt) {
                command = Const.VCC_IMPORT_NT + Const.VCC_IMPORTTYPES_OPT + lcvDir;
            } else {
                command = Const.VCC_IMPORT_UX + Const.VCC_IMPORTTYPES_OPT + lcvDir;
            }
            runCommand(command, null, null);
        }
        if (argEcl.nt) {
            command = Const.VCC_IMPORT_NT + " -LCV " + cellib + ":" + Const.VCC_WHTBOX_DIR + Const.VCC_IMPORT_OPT + lcvDir + Const.VCC_SYMBOL_OPT;
        } else {
            command = Const.VCC_IMPORT_UX + " -LCV " + cellib + ":" + Const.VCC_WHTBOX_DIR + Const.VCC_IMPORT_OPT + lcvDir + Const.VCC_SYMBOL_OPT;
        }
        runCommand(command, null, null);
        PrintWriter tmp = new PrintWriter(new FileOutputStream(lcDir + dirsep + "wht_ecl" + dirsep + "master.tag"));
        tmp.write("white.c\n");
        tmp.close();
    }

    static void copyVCCFiles(String name, String base, String lcvDir, String lcDir, String cc) throws java.lang.Exception {
        String command;
        if (argEcl.printDebug) {
            if (argEcl.nt && !cc.equals("gcc")) {
                command = cc + Const.COPY_VCC_DEBUG_NT + lcvDir + base + ".c ";
            } else {
                command = cc + Const.COPY_VCC_DEBUG_UX + lcvDir + base + ".c ";
            }
            runCommand(command, null, null);
        }
        if (argEcl.nt) {
            command = "cmd /c copy " + lcvDir + name + " " + lcDir + "\\" + Const.VCC_WHTBOX_DIR + "\\";
            runCommand(command, null, "File copy error: make sure the new view wht_ecl was created");
            if (argEcl.printDebug) {
                command = "cmd /c copy " + base + ".obj " + lcDir + "\\" + Const.VCC_WHTBOX_DIR + "\\";
                runCommand(command, null, null);
            }
            command = "cmd /c copy " + lcvDir + base + ".c " + lcDir + "\\" + Const.VCC_WHTBOX_DIR + "\\";
            runCommand(command, null, null);
            command = "cmd /c copy " + lcvDir + base + ".h " + lcDir + "\\" + Const.VCC_WHTBOX_DIR + "\\";
            runCommand(command, null, null);
            command = "cmd /c copy " + lcvDir + "white.c " + lcDir + "\\" + Const.VCC_WHTBOX_DIR + "\\";
            runCommand(command, null, null);
            command = "cmd /c copy " + lcvDir + Const.VCC_LIBRARY_FILE + lcDir + "\\" + Const.VCC_WHTBOX_DIR + "\\";
            runCommand(command, null, null);
        } else {
            command = "cp " + lcvDir + base + ".c " + lcvDir + base + ".h " + lcvDir + "white.c " + lcDir + "/" + Const.VCC_WHTBOX_DIR;
            runCommand(command, null, "File copy error: make sure the new view " + Const.VCC_WHTBOX_DIR + " was created");
            if (argEcl.printDebug) {
                command = "cp " + lcvDir + name + " " + lcvDir + base + ".o " + lcDir + "/" + Const.VCC_WHTBOX_DIR;
                runCommand(command, null, null);
            }
        }
    }

    static void runCommand(String command, String outfile, String errorstring) throws java.lang.Exception {
        System.out.print(command);
        if (outfile != null) System.out.print(" > " + outfile);
        System.out.println();
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(command);
        PrintWriter outf;
        if (outfile != null) {
            outf = new PrintWriter(new FileOutputStream(outfile));
        } else {
            outf = new PrintWriter(System.out);
        }
        InputStream procStdout = p.getInputStream();
        InputStream procStderr = p.getErrorStream();
        int exit = 0, n1, n2;
        boolean processEnded = false;
        while (!processEnded) {
            try {
                exit = p.exitValue();
                processEnded = true;
            } catch (IllegalThreadStateException e) {
            }
            n1 = procStdout.available();
            if (n1 > 0) {
                for (int i = 0; i < n1; i++) {
                    int b = procStdout.read();
                    if (b < 0) break;
                    outf.write(b);
                }
            }
            n2 = procStderr.available();
            if (n2 > 0) {
                byte[] pbytes = new byte[n2];
                procStderr.read(pbytes);
                System.err.print(new String(pbytes));
                System.err.flush();
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        do {
            n1 = procStdout.available();
            if (n1 > 0) {
                for (int i = 0; i < n1; i++) {
                    int b = procStdout.read();
                    if (b < 0) break;
                    outf.write(b);
                }
            }
            n2 = procStderr.available();
            if (n2 > 0) {
                byte[] pbytes = new byte[n2];
                procStderr.read(pbytes);
                System.err.print(new String(pbytes));
                System.err.flush();
            }
        } while (n1 > 0 || n2 > 0);
        if (outfile != null) outf.close(); else outf.flush();
        if (exit != 0) {
            System.err.println("Error " + exit + " during subprocess execution");
            if (errorstring != null) System.err.println(errorstring);
            System.exit(-1);
        }
    }

    static void printDebugInfo(String base, structuredStream s_data, boolean vccWrapper) throws java.io.IOException, java.lang.Exception {
        if (base == null) {
            System.err.println("Error: need top-level module name same as file name for debug record generation");
            System.exit(-1);
        }
        s_data.println("extern int __NoVarCheckFlag;");
        s_data.println("void __SetNoVarCheckFlag( i )");
        s_data.println("int i;");
        s_data.println("{__NoVarCheckFlag = i;}");
        s_data.println("extern struct __VariableEntry __" + base + "_VariableTable [];");
        s_data.println("extern struct __SignalEntry __" + base + "_SignalTable [];");
        s_data.println("static struct __ModuleEntry __" + base + "_ModuleData;");
        s_data.println("static char *__GetVariablePtr( s )");
        s_data.println("char *s;");
        s_data.println("{");
        s_data.println("  int i, k, n = __" + base + "_ModuleData.number_of_variables;");
        s_data.println("  for ( i = 0; i < n; i++ ) {");
        s_data.println("    k = __" + base + "_VariableTable [i].comment_kind;");
        s_data.println("    if ( k == 2 || k == 5) continue;");
        s_data.println("    if ( !strcmp( __" + base + "_VariableTable [i].source_name, s ) )");
        s_data.println("      return __" + base + "_VariableTable [i].p_variable;");
        s_data.println("  }");
        s_data.println("  return 0;");
        s_data.println("}");
        s_data.eol();
        s_data.println("static int __GetSignalIndex( s )");
        s_data.println("char *s;");
        s_data.println("{");
        s_data.println("  int i, n = __" + base + "_ModuleData.number_of_variables;");
        s_data.println("  for ( i = 0; i < n; i++ ) {");
        s_data.println("    if ( !strcmp( __" + base + "_SignalTable [i].name, s ) )");
        s_data.println("      return i;");
        s_data.println("  }");
        s_data.println("  return -1;");
        s_data.println("}");
        s_data.eol();
        Vector deb_decl = tableManager.v_debugTable;
        if (vccWrapper) {
            for (int i = 0; i < deb_decl.size(); i++) {
                identifier ident = (identifier) deb_decl.elementAt(i);
                if (ident.getStorageClass() == identifier.OUTPUT) {
                    if (ident.getRetType().isPure()) {
                        s_data.println("static int E_" + ident.getName() + ";");
                        s_data.print("void " + base + "_O_" + ident.getName() + "( ) {");
                        s_data.println(" E_" + ident.getName() + " = 1;}");
                        s_data.print("int _E_" + base + "_" + ident.getName() + "( ) { return ");
                        s_data.println("E_" + ident.getName() + "; }");
                    } else {
                        s_data.print("static int E_" + ident.getName() + ";");
                        s_data.print("  static ");
                        ident.printCtype(s_data);
                        s_data.println(";");
                        s_data.print("void " + base + "_O_" + ident.getName() + "( ");
                        ident.getRetType().printC(s_data, new identifier("a"));
                        s_data.println(" ) {");
                        s_data.println("  E_" + ident.getName() + " = 1;");
                        if (ident.getRetType().realType() instanceof typeBasic) {
                            s_data.println("  " + ident.getName() + " = a;");
                        } else {
                            s_data.print("  memcpy ( (char*) &" + ident.getName());
                            s_data.print(", (char*) &a , sizeof( ");
                            s_data.println(ident.getName() + " ) );");
                        }
                        s_data.println("}");
                        s_data.print("int _E_" + base + "_" + ident.getName() + "( ) { return ");
                        s_data.println("E_" + ident.getName() + "; }");
                        ident.getRetType().printC(s_data, new identifier("_V_" + base + "_" + ident.getName()));
                        s_data.print("( ) { return ");
                        s_data.println(ident.getName() + "; }");
                    }
                }
            }
            s_data.println("int _R_" + base + "( ) {");
            for (int i = 0; i < deb_decl.size(); i++) {
                identifier ident = (identifier) deb_decl.elementAt(i);
                if (ident.getStorageClass() == identifier.OUTPUT) {
                    s_data.print("  E_" + ident.getName() + " = 0;");
                }
            }
            s_data.println("}");
        }
        s_data.println("int __debug_" + base + " = 1;");
        s_data.println("int _set_debug_" + base + "( i ) int i; {");
        s_data.println("  __debug_" + base + " = i; }");
        s_data.println("static void debugLine( int line ) {");
        s_data.println("  static int __dummy, __initialized;");
        s_data.println("  /*debug variables*/");
        for (int i = 0; i < deb_decl.size(); i++) {
            identifier ident = (identifier) deb_decl.elementAt(i);
            if (ident.isSignal()) {
                s_data.print("  static int E_" + ident.getName() + ";");
                s_data.println(" static int _P_E_" + ident.getName() + ";");
            }
            if (!ident.getRetType().isPure()) {
                s_data.print("  static ");
                ident.printCtype(s_data);
                s_data.print("; static ");
                if (ident.getRetType().realType() instanceof typeBasic) {
                    identifier ident_ptr = (identifier) ident.clone();
                    ident_ptr.setName("_P_" + ident.getName());
                    ident_ptr.setRetType(new typePointer(ident.getRetType()));
                    ident_ptr.printCtype(s_data);
                } else {
                    s_data.print(" char* _P_" + ident.getName());
                }
                s_data.println(";");
            }
        }
        s_data.println("  if ( !__debug_" + base + " ) return;");
        s_data.println("  if ( !__initialized ) {");
        s_data.println("    __initialized = 1;");
        s_data.println("    __InitializeInputBufferStack( );");
        s_data.println("    __NoVarCheckFlag = 1;");
        s_data.println("    __UseFirstModule( );");
        s_data.println("    " + base + "_reset( );");
        for (int i = 0; i < deb_decl.size(); i++) {
            identifier ident = (identifier) deb_decl.elementAt(i);
            if (ident.isSignal()) {
                if (ident.getStorageClass() == identifier.INPUT) {
                    s_data.print("    _P_E_");
                    s_data.print(ident.getName() + " = ");
                    s_data.print(" __GetSignalIndex (\"");
                    s_data.println(ident.getName() + "\" );");
                } else {
                    s_data.print("    _P_E_");
                    s_data.print(ident.getName() + " = ");
                    s_data.print(" __GetSignalIndex (\"");
                    s_data.println(ident.getName() + "\" );");
                }
            }
            if (!ident.getRetType().isPure()) {
                s_data.print("    _P_");
                s_data.print(ident.getName() + " = ");
                if (ident.getRetType().realType() instanceof typeBasic) {
                    s_data.print("( ");
                    ident.getRetType().printC(s_data, null);
                    s_data.print("*)");
                }
                s_data.print("__GetVariablePtr (\"");
                s_data.println(ident.getName() + "\" );");
            }
        }
        s_data.println("  }");
        for (int i = 0; i < deb_decl.size(); i++) {
            identifier ident = (identifier) deb_decl.elementAt(i);
            if (ident.isSignal()) {
                s_data.print("  if (_P_E_" + ident.getName() + " >= 0) ");
                if (ident.getStorageClass() == identifier.INPUT) {
                    s_data.print("  E_" + ident.getName() + " = __IsPresentInput( __InputIndexOfSignalIndex( _P_E_");
                    s_data.println(ident.getName() + " ) );");
                } else {
                    s_data.print("  E_" + ident.getName() + " = __IsEmittedSignal( _P_E_");
                    s_data.println(ident.getName() + " );");
                }
            }
            if (!ident.getRetType().isPure()) {
                s_data.print("  if (_P_" + ident.getName() + ") ");
                if (ident.getRetType().realType() instanceof typeBasic) {
                    s_data.print("  " + ident.getName() + " = *_P_");
                    s_data.println(ident.getName() + ";");
                } else {
                    s_data.print("  memcpy ( (char*) &" + ident.getName());
                    s_data.print(", (char*)_P_");
                    s_data.print(ident.getName() + ", sizeof( ");
                    s_data.println(ident.getName() + " ) );");
                }
            }
        }
        s_data.println("  /*debug lines*/");
        s_data.println("  switch( line ) {");
        for (int i = 1; i <= lastLineNum; i++) {
            Integer number = new Integer(i);
            s_data.println("    case " + number.toString() + ":");
            s_data.println("#line " + number.toString() + " \"" + base + ".ecl\"");
            s_data.println("    __dummy = " + number.toString() + "; break;");
        }
        s_data.println("  }");
        s_data.println("}");
    }

    static void extractModule() throws IOException {
        Enumeration e = tableManager.symbolTable.elements();
        while (e.hasMoreElements()) {
            identifier ident = (identifier) e.nextElement();
            ident.setProcessed(false);
        }
        e = tableManager.symbolTable.elements();
        while (e.hasMoreElements()) {
            identifier ident = (identifier) e.nextElement();
            if (ident.getStorageClass() == identifier.MODULE && !ident.getProcessed()) {
                ident.setProcessed(true);
                ident.adjustInterface();
                ident.splitEC();
                ident.extractFunctions();
                ident.extractTypes();
                ident.extractPrototypes();
            }
        }
    }

    static void eclCheck(String base) throws IOException {
        Enumeration e = tableManager.symbolTable.elements();
        System.err.println("Checking " + base);
        while (e.hasMoreElements()) {
            identifier ident = (identifier) e.nextElement();
            if (ident.getStorageClass() == identifier.MODULE) {
                ident.checkAbort();
            }
        }
    }
}
