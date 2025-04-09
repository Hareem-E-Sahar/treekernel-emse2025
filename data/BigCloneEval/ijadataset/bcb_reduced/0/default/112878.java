import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jostraca.util.FileUtil;
import org.jostraca.util.Standard;
import org.jostraca.util.PropertySet;
import org.jostraca.util.TextUtil;
import org.jostraca.util.ArgUtil;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/** Abstract Test class specifying the standard set of tests
 *  for code writers built from BasicFormats.
 */
public abstract class BasicFormatTest extends TestCase {

    public static final String BACKUP_FOLDER_NAME = ".jostraca";

    public static final String ARGWORKFOLDER_FOLDER_NAME = "work";

    public static final String ARGOUTPUTFOLDER_FOLDER_NAME = "output";

    public static final String SETOUTPUTFOLDER_FOLDER_NAME = "setOutputFolder";

    public static final String BACKUP_CONTENTS = "backup = notdone";

    public static final String STANDARD_JOSTRACA_CMD = "java -cp ../../src org.jostraca.Jostraca";

    public static final String STANDARD_JOSTRACA_CONF_ARG = " -f ../../conf/system.conf ";

    public static final String PREFIX_TEST = "test";

    public static final String PREFIX_RESULT = "result";

    public static final String DOT = ".";

    public static final String TXT = "txt";

    public static final String P = "resultp";

    public static final String S = "s";

    public static final String A = "a";

    public static final String B = "b";

    public static final String EMPTY = "";

    public static final String SPACE = " ";

    public static final String ZERO = "0";

    public static final String ONE = "1";

    public static final String TWO = "2";

    public static final String SLASH = "/";

    public static final String FOO = "foo";

    public static final String BOO = "boo";

    public static final String BAR = "bar";

    public static final String TEST_MethodSetFileNameRoot = "MethodSetFileNameRoot";

    public static final String TEST_MethodSetFileNameRoots = "MethodSetFileNameRoots";

    public static final String TEST_ForOneFileMethodSetFileNamePrefix = "ForOneFileMethodSetFileNamePrefix";

    public static final String TEST_ForManyFilesMethodSetFileNamePrefix = "ForManyFilesMethodSetFileNamePrefix";

    public static final String TEST_ForOneFileMethodSetFileNameSuffix = "ForOneFileMethodSetFileNameSuffix";

    public static final String TEST_ForManyFilesMethodSetFileNameSuffix = "ForManyFilesMethodSetFileNameSuffix";

    public static final String TEST_MethodSetFullFileName = "MethodSetFullFileName";

    public static final String TEST_MethodSetFullFileNames = "MethodSetFullFileNames";

    public static final String TEST_MethodGetFullFileNames = "MethodGetFullFileNames";

    public static final String TEST_MethodGetFileIndex = "MethodGetFileIndex";

    public static final String TEST_ForOneFileMethodGetNumFiles = "ForOneFileMethodGetNumFiles";

    public static final String TEST_ForManyFilesMethodGetNumFiles = "ForManyFilesMethodGetNumFiles";

    public static final String TEST_MethodsGetSetText = "MethodsGetSetText";

    public static final String TEST_MethodInsert = "MethodInsert";

    public static final String TEST_Transforms = "Transforms";

    public static final String TEST_MethodSetOutputFolder = "MethodSetOutputFolder";

    public static final String TEST_MethodGetOutputFolder = "MethodGetOutputFolder";

    public static final String TEST_MethodsSaveLoadTextFile = "MethodsSaveLoadTextFile";

    public static final String TEST_MethodGetProperty = "MethodGetProperty";

    public static final String TEST_MethodGetArgs = "MethodGetArgs";

    public static final String TEST_MethodGetNumArgs = "MethodGetNumArgs";

    public static final String TEST_MethodGetFirstUserArg = "MethodGetFirstUserArg";

    public static final String TEST_MethodGetSecondUserArg = "MethodGetSecondUserArg";

    public static final String TEST_MethodGetThirdUserArg = "MethodGetThirdUserArg";

    public static final String TEST_MethodGetUserArg = "MethodGetUserArg";

    public static final String TEST_MethodGetUserArgs = "MethodGetUserArgs";

    public static final String TEST_MethodGetNumUserArgs = "MethodGetNumUserArgs";

    public static final String TEST_MethodSpaces = "MethodSpaces";

    public static final String TEST_MethodLeft = "MethodLeft";

    public static final String TEST_MethodRight = "MethodRight";

    public static final String TEST_MethodAlign = "MethodAlign";

    public static final String TEST_InitDeclarations = "InitDeclarations";

    public static final String TEST_DirectiveSection = "DirectiveSection";

    public static final String TEST_DirectiveInclude = "DirectiveInclude";

    public static final String TEST_ArgOutputFolder = "ArgOutputFolder";

    public static final String TEST_ArgWorkFolder = "ArgWorkFolder";

    public static final String TEST_ArgOutputAndWorkFolders = "ArgOutputAndWorkFolders";

    public static final String TEST_Backup = "Backup";

    public static final String TEST_InternalController = "InternalController";

    public static final String TEST_ExternalController = "ExternalController";

    public static final String TEST_Version_0_1 = "Version_0_1";

    public static final String TEST_Version_0_2 = "Version_0_2";

    public static final String TEST_Version_0_3 = "Version_0_3";

    public static final String USER_ARGS_testMethodGetArgs = "ua1 \"u a 2\" ua3";

    public static final String USER_ARGS_testMethodGetNumArgs = "ua1 \"u a 2\" ua3";

    public static final String USER_ARGS_testMethodGetFirstUserArg = "ua1";

    public static final String USER_ARGS_testMethodGetSecondUserArg = "ua1 ua2";

    public static final String USER_ARGS_testMethodGetThirdUserArg = "ua1 ua2 ua3";

    public static final String USER_ARGS_testMethodGetUserArg = "ua1 ua2 ua3 ua4";

    public static final String USER_ARGS_testMethodGetUserArgs = "ua1 \"u a 2\" ua3";

    public static final String USER_ARGS_testMethodGetNumUserArgs = "ua1 \"u a 2\" ua3";

    public static int NUM_USER_ARGS_testMethodGetNumArgs = 6;

    public static int NUM_USER_ARGS_testMethodGetNumUserArgs = 3;

    public static final String NAME_FileNameRoot = "FileNameRoot";

    public static final String NAME_FileNamePrefix = "FileNamePrefix";

    public static final String NAME_FileNameSuffix = "FileNameSuffix";

    public static final String NAME_FullFileName = "FullFileName";

    public static final String NAME_FileIndex = "FileIndex";

    public static final String NAME_NumFiles = "NumFiles";

    public static final String NAME_OutputFolder = "OutputFolder";

    public static final String NAME_WorkFolder = "WorkFolder";

    public static final String NAME_SaveLoadTextFile = "SaveLoadTextFile";

    private static PropertySet sReferencePS = new PropertySet();

    private static PropertySet sTestPS = new PropertySet();

    private static boolean sCleaningDone = false;

    protected static boolean sSpecifyConfig = true;

    public BasicFormatTest(String pName) {
        super(pName);
    }

    public static TestSuite suite() {
        return new TestSuite(BasicFormatTest.class);
    }

    public static void main(String[] pArgs) {
        TestRunner.run(suite());
    }

    public void setUp() throws Exception {
        if (!sCleaningDone) {
            clean();
            sCleaningDone = true;
        }
    }

    public PrintWriter getPrintWriter(String pFileName) throws IOException {
        FileOutputStream fout = new FileOutputStream(pFileName);
        PrintWriter pout = new PrintWriter(fout);
        return pout;
    }

    public boolean runCommand(String pCommand) throws Exception {
        boolean error = false;
        String cmdtext = pCommand;
        System.out.print("\n[ " + cmdtext);
        String[] cmd = ArgUtil.splitQuoted(cmdtext);
        cmd = TextUtil.quoteSpaces(cmd);
        Process p = Runtime.getRuntime().exec(cmd);
        String line_out = null;
        BufferedReader br_out = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while (null != (line_out = br_out.readLine())) {
            if (null != line_out) {
            }
        }
        int result = 0;
        boolean success = (!error) && (0 == result);
        System.out.print(" ]");
        return success;
    }

    public void clean() throws Exception {
        System.out.print("cleaning...");
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodSetFileNameRoot + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodSetFileNameRoots + A + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodSetFileNameRoots + B + DOT + TXT);
        cleanFile(getTestFolder(), P + TEST_ForOneFileMethodSetFileNamePrefix + DOT + TXT);
        cleanFile(getTestFolder(), P + TEST_ForManyFilesMethodSetFileNamePrefix + A + DOT + TXT);
        cleanFile(getTestFolder(), P + TEST_ForManyFilesMethodSetFileNamePrefix + B + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_ForOneFileMethodSetFileNameSuffix + DOT + S + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_ForManyFilesMethodSetFileNameSuffix + A + DOT + S + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_ForManyFilesMethodSetFileNameSuffix + B + DOT + S + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodSetFullFileName + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodSetFullFileNames + A + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodSetFullFileNames + B + DOT + TXT);
        cleanFile(getTestFolder(), P + TEST_MethodGetFullFileNames + A + DOT + TXT);
        cleanFile(getTestFolder(), P + TEST_MethodGetFullFileNames + B + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodGetFileIndex + A + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodGetFileIndex + B + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_ForOneFileMethodGetNumFiles + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_ForManyFilesMethodGetNumFiles + A + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_ForManyFilesMethodGetNumFiles + B + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodsGetSetText + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodInsert + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_Transforms + DOT + TXT);
        File sof = new File(getTestFolder() + "/" + SETOUTPUTFOLDER_FOLDER_NAME);
        if (!sof.exists()) {
            sof.mkdirs();
        }
        cleanFile(getTestFolder() + "/" + SETOUTPUTFOLDER_FOLDER_NAME, PREFIX_RESULT + TEST_MethodSetOutputFolder + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodGetOutputFolder + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodGetProperty + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodsSaveLoadTextFile + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodGetArgs + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodGetNumArgs + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodGetFirstUserArg + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodGetSecondUserArg + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodGetThirdUserArg + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodGetUserArg + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodGetUserArgs + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodGetNumUserArgs + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodSpaces + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodLeft + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodRight + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_MethodAlign + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_InitDeclarations + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_DirectiveSection + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_DirectiveInclude + DOT + TXT);
        File aof = new File(getTestFolder() + "/" + ARGOUTPUTFOLDER_FOLDER_NAME);
        if (!aof.exists()) {
            aof.mkdirs();
        }
        cleanFile(getTestFolder() + "/" + ARGOUTPUTFOLDER_FOLDER_NAME, PREFIX_RESULT + TEST_ArgOutputFolder + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_ArgWorkFolder + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_Backup + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_InternalController + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_ExternalController + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_Version_0_1 + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_Version_0_2 + DOT + TXT);
        cleanFile(getTestFolder(), PREFIX_RESULT + TEST_Version_0_3 + DOT + TXT);
        System.out.println("done.");
    }

    public abstract String getTestFolder();

    public abstract String getWriterFolder();

    public abstract boolean argArrayContainsProgramName();

    public abstract String getJostracaCodeWriterFileName();

    public abstract String getControllerClass();

    public abstract void testMethodSetFileNameRoot() throws Exception;

    public abstract void testMethodSetFileNameRoots() throws Exception;

    public abstract void testForOneFileMethodSetFileNamePrefix() throws Exception;

    public abstract void testForManyFilesMethodSetFileNamePrefix() throws Exception;

    public abstract void testForOneFileMethodSetFileNameSuffix() throws Exception;

    public abstract void testForManyFilesMethodSetFileNameSuffix() throws Exception;

    public abstract void testMethodSetFullFileName() throws Exception;

    public abstract void testMethodSetFullFileNames() throws Exception;

    public abstract void testMethodGetFullFileNames() throws Exception;

    public abstract void testMethodGetFileIndex() throws Exception;

    public abstract void testForOneFileMethodGetNumFiles() throws Exception;

    public abstract void testForManyFilesMethodGetNumFiles() throws Exception;

    public abstract void testMethodsGetSetText() throws Exception;

    public abstract void testMethodInsert() throws Exception;

    public abstract void testTransforms() throws Exception;

    public abstract void testMethodSetOutputFolder() throws Exception;

    public abstract void testMethodGetOutputFolder() throws Exception;

    public abstract void testMethodsSaveLoadTextFile() throws Exception;

    public abstract void testMethodGetProperty() throws Exception;

    public abstract void testMethodGetArgs() throws Exception;

    public abstract void testMethodGetNumArgs() throws Exception;

    public abstract void testMethodGetFirstUserArg() throws Exception;

    public abstract void testMethodGetSecondUserArg() throws Exception;

    public abstract void testMethodGetThirdUserArg() throws Exception;

    public abstract void testMethodGetUserArg() throws Exception;

    public abstract void testMethodGetUserArgs() throws Exception;

    public abstract void testMethodGetNumUserArgs() throws Exception;

    public abstract void testMethodSpaces() throws Exception;

    public abstract void testMethodLeft() throws Exception;

    public abstract void testMethodRight() throws Exception;

    public abstract void testMethodAlign() throws Exception;

    public abstract void testInitDeclarations() throws Exception;

    public abstract void testDirectiveSection() throws Exception;

    public abstract void testDirectiveInclude() throws Exception;

    public abstract void testArgOutputFolder() throws Exception;

    public abstract void testArgWorkFolder() throws Exception;

    public abstract void testArgOutputAndWorkFolders() throws Exception;

    public abstract void testBackup() throws Exception;

    public abstract void testInternalController() throws Exception;

    public abstract void testExternalController() throws Exception;

    public boolean validateMethodSetFileNameRoot() throws Exception {
        String testRoot = TEST_MethodSetFileNameRoot;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot);
        sReferencePS.set(NAME_FileNameSuffix, DOT + TXT);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS);
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodSetFileNameRoots() throws Exception {
        String testRoot = TEST_MethodSetFileNameRoots;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot + A);
        sReferencePS.set(NAME_FileNameSuffix, DOT + TXT);
        sReferencePS.set(NAME_FileNameRoot + A, PREFIX_RESULT + testRoot + A);
        sReferencePS.set(NAME_FileNameRoot + B, PREFIX_RESULT + testRoot + B);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + A + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot + B);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + B + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateForOneFileMethodSetFileNamePrefix() throws Exception {
        String testRoot = TEST_ForOneFileMethodSetFileNamePrefix;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set(NAME_FileNamePrefix, P);
        sReferencePS.set(NAME_FileNameRoot, testRoot);
        sReferencePS.set(NAME_FileNameSuffix, DOT + TXT);
        sTestPS.load(new File(getTestFolder(), P + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS);
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateForManyFilesMethodSetFileNamePrefix() throws Exception {
        String testRoot = TEST_ForManyFilesMethodSetFileNamePrefix;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set(NAME_FileNamePrefix, P);
        sReferencePS.set(NAME_FileNameRoot, testRoot + A);
        sReferencePS.set(NAME_FileNameSuffix, DOT + TXT);
        sTestPS.load(new File(getTestFolder(), P + testRoot + A + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        sReferencePS.set(NAME_FileNameRoot, testRoot + B);
        sTestPS.load(new File(getTestFolder(), P + testRoot + B + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateForOneFileMethodSetFileNameSuffix() throws Exception {
        String testRoot = TEST_ForOneFileMethodSetFileNameSuffix;
        sReferencePS.clear();
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot);
        sReferencePS.set(NAME_FileNameSuffix, DOT + S + TXT);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + S + TXT));
        boolean test = sTestPS.equals(sReferencePS);
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateForManyFilesMethodSetFileNameSuffix() throws Exception {
        String testRoot = TEST_ForManyFilesMethodSetFileNameSuffix;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot + A);
        sReferencePS.set(NAME_FileNameSuffix, DOT + S + TXT);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + A + DOT + S + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot + B);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + B + DOT + S + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodSetFullFileName() throws Exception {
        String testRoot = TEST_MethodSetFullFileName;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot + DOT + TXT);
        sReferencePS.set(NAME_FullFileName, PREFIX_RESULT + testRoot + DOT + TXT);
        sReferencePS.set(NAME_FileNamePrefix, EMPTY);
        sReferencePS.set(NAME_FileNameSuffix, EMPTY);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS);
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodSetFullFileNames() throws Exception {
        String testRoot = TEST_MethodSetFullFileNames;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot + A + DOT + TXT);
        sReferencePS.set(NAME_FullFileName, PREFIX_RESULT + testRoot + A + DOT + TXT);
        sReferencePS.set(NAME_FileNamePrefix, EMPTY);
        sReferencePS.set(NAME_FileNameSuffix, EMPTY);
        sReferencePS.set(NAME_FullFileName + A, PREFIX_RESULT + testRoot + A + DOT + TXT);
        sReferencePS.set(NAME_FullFileName + B, PREFIX_RESULT + testRoot + B + DOT + TXT);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + A + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot + B + DOT + TXT);
        sReferencePS.set(NAME_FullFileName, PREFIX_RESULT + testRoot + B + DOT + TXT);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + B + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodGetFullFileNames() throws Exception {
        String testRoot = TEST_MethodGetFullFileNames;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set(NAME_FullFileName, P + testRoot + A + DOT + TXT);
        sReferencePS.set(NAME_FullFileName + A, P + testRoot + A + DOT + TXT);
        sReferencePS.set(NAME_FullFileName + B, P + testRoot + B + DOT + TXT);
        sTestPS.load(new File(getTestFolder(), P + testRoot + A + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        sReferencePS.set(NAME_FullFileName, P + testRoot + B + DOT + TXT);
        sTestPS.load(new File(getTestFolder(), P + testRoot + B + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodGetFileIndex() throws Exception {
        String testRoot = TEST_MethodGetFileIndex;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot + A);
        sReferencePS.set(NAME_FileIndex, ZERO);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + A + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot + B);
        sReferencePS.set(NAME_FileIndex, ONE);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + B + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateForOneFileMethodGetNumFiles() throws Exception {
        String testRoot = TEST_ForOneFileMethodGetNumFiles;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot + DOT + TXT);
        sReferencePS.set(NAME_FileIndex, ZERO);
        sReferencePS.set(NAME_NumFiles, ONE);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateForManyFilesMethodGetNumFiles() throws Exception {
        String testRoot = TEST_ForManyFilesMethodGetNumFiles;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot + A);
        sReferencePS.set(NAME_FileIndex, ZERO);
        sReferencePS.set(NAME_NumFiles, TWO);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + A + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        sReferencePS.set(NAME_FileNameRoot, PREFIX_RESULT + testRoot + B);
        sReferencePS.set(NAME_FileIndex, ONE);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + B + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodsGetSetText() throws Exception {
        String testRoot = TEST_MethodsGetSetText;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set(BOO, BAR);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodInsert() throws Exception {
        String testRoot = TEST_MethodInsert;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("string", "string");
        sReferencePS.set("char", "c");
        sReferencePS.set("integer", "123");
        sReferencePS.set("float", "4.56");
        sReferencePS.set("insert", "insert");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.contains(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateTransforms() throws Exception {
        String testRoot = TEST_Transforms;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("oneLine", "one Line");
        sReferencePS.set("line1", "line1");
        sReferencePS.set("line2", "line2");
        sReferencePS.set("collapse1", "a b c");
        sReferencePS.set("collapse2", "d  e f");
        sReferencePS.set("replace1", "[after1]");
        sReferencePS.set("replace2", "[after2]");
        sReferencePS.set("replace-regexp1", "[[re1]]");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.contains(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodSetOutputFolder() throws Exception {
        String testRoot = TEST_MethodSetOutputFolder;
        boolean test = true;
        String actualOutputFolder = getTestFolder() + SLASH + SETOUTPUTFOLDER_FOLDER_NAME;
        String writerOutputFolder = getWriterFolder() + SLASH + SETOUTPUTFOLDER_FOLDER_NAME;
        sReferencePS.clear();
        sReferencePS.set(NAME_OutputFolder, writerOutputFolder);
        sTestPS.load(new File(actualOutputFolder, PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodGetOutputFolder() throws Exception {
        String testRoot = TEST_MethodGetOutputFolder;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set(NAME_OutputFolder, getWriterFolder());
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodGetProperty() throws Exception {
        String testRoot = TEST_MethodGetProperty;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("jostraca.template.folder", getTestFolder());
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodsSaveLoadTextFile() throws Exception {
        String testRoot = TEST_MethodsSaveLoadTextFile;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("saveload", "yes");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodGetArgs() throws Exception {
        String testRoot = TEST_MethodGetArgs;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("arg0", "jsa:o=" + getWriterFolder());
        sReferencePS.set("arg1", "jsa:p=");
        sReferencePS.set("arg2", "jsa:B");
        sReferencePS.set("arg3", "ua1");
        sReferencePS.set("arg4", "u a 2");
        sReferencePS.set("arg5", "ua3");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodGetNumArgs() throws Exception {
        String testRoot = TEST_MethodGetNumArgs;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("arg0", "jsa:o=" + getWriterFolder());
        sReferencePS.set("arg1", "jsa:p=");
        sReferencePS.set("arg2", "jsa:B");
        sReferencePS.set("arg3", "ua1");
        sReferencePS.set("arg4", "u a 2");
        sReferencePS.set("arg5", "ua3");
        sReferencePS.set("numArgs", String.valueOf(NUM_USER_ARGS_testMethodGetNumArgs + (argArrayContainsProgramName() ? 1 : 0)));
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodGetFirstUserArg() throws Exception {
        String testRoot = TEST_MethodGetFirstUserArg;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("ua1", "ua1");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodGetSecondUserArg() throws Exception {
        String testRoot = TEST_MethodGetSecondUserArg;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("ua1", "ua1");
        sReferencePS.set("ua2", "ua2");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodGetThirdUserArg() throws Exception {
        String testRoot = TEST_MethodGetThirdUserArg;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("ua1", "ua1");
        sReferencePS.set("ua2", "ua2");
        sReferencePS.set("ua3", "ua3");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodGetUserArg() throws Exception {
        String testRoot = TEST_MethodGetUserArg;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("ua1", "ua1");
        sReferencePS.set("ua2", "ua2");
        sReferencePS.set("ua3", "ua3");
        sReferencePS.set("ua4", "ua4");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodGetUserArgs() throws Exception {
        String testRoot = TEST_MethodGetUserArgs;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("arg0", "ua1");
        sReferencePS.set("arg1", "u a 2");
        sReferencePS.set("arg2", "ua3");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodGetNumUserArgs() throws Exception {
        String testRoot = TEST_MethodGetNumUserArgs;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("arg0", "ua1");
        sReferencePS.set("arg1", "u a 2");
        sReferencePS.set("arg2", "ua3");
        sReferencePS.set("numUserArgs", String.valueOf(NUM_USER_ARGS_testMethodGetNumUserArgs));
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodSpaces() throws Exception {
        String testRoot = TEST_MethodSpaces;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("-1s", "[ ]");
        sReferencePS.set("0s", "[]");
        sReferencePS.set("1s", "[ ]");
        sReferencePS.set("2s", "[  ]");
        sReferencePS.set("4s", "[    ]");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodLeft() throws Exception {
        String testRoot = TEST_MethodLeft;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("a", "[a]");
        sReferencePS.set("b", "[a  ]");
        sReferencePS.set("c", "[aa ]");
        sReferencePS.set("d", "[aaa]");
        sReferencePS.set("e", "[aaaa]");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodRight() throws Exception {
        String testRoot = TEST_MethodRight;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("a", "[a]");
        sReferencePS.set("b", "[  a]");
        sReferencePS.set("c", "[ aa]");
        sReferencePS.set("d", "[aaa]");
        sReferencePS.set("e", "[aaaa]");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateMethodAlign() throws Exception {
        String testRoot = TEST_MethodAlign;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("a", "[001]");
        sReferencePS.set("b", "[1--]");
        sReferencePS.set("c", "[1]");
        sReferencePS.set("d", "[1]");
        sReferencePS.set("e", "[1-]");
        sReferencePS.set("f", "[-1-]");
        sReferencePS.set("g", "[-1--]");
        sReferencePS.set("h", "[--1--]");
        sReferencePS.set("i", "[--11--]");
        sReferencePS.set("j", "[-121--]");
        sReferencePS.set("k", "[--121--]");
        sReferencePS.set("l", "[1-+-+-]");
        sReferencePS.set("m", "[-+1-+-]");
        sReferencePS.set("n", "[-+-+-1]");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.equals(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateInitDeclarations() throws Exception {
        String testRoot = TEST_InitDeclarations;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("foo", "bar");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.contains(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateDirectiveSection() throws Exception {
        String testRoot = TEST_DirectiveSection;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("list0", EMPTY);
        sReferencePS.set("list1", "[0]");
        sReferencePS.set("list2", "[0][1]");
        sReferencePS.set(FOO, BAR);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.contains(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateDirectiveInclude() throws Exception {
        String testRoot = TEST_DirectiveInclude;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("include0", "zero");
        sReferencePS.set("include1", "one-zero-one");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.contains(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateArgOutputFolder() throws Exception {
        String testRoot = TEST_ArgOutputFolder;
        boolean test = true;
        String outputFolder = getTestFolder() + SLASH + ARGOUTPUTFOLDER_FOLDER_NAME;
        sReferencePS.clear();
        sReferencePS.set(NAME_OutputFolder, outputFolder);
        sTestPS.load(new File(outputFolder, PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.contains(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateArgWorkFolder() throws Exception {
        String testRoot = TEST_ArgWorkFolder;
        boolean test = true;
        String outputFolder = getWriterFolder();
        String workFolder = getTestFolder() + SLASH + ARGWORKFOLDER_FOLDER_NAME;
        sReferencePS.clear();
        sReferencePS.set(NAME_OutputFolder, outputFolder);
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.contains(sReferencePS) && test;
        File jostracaCodeWriter = new File(workFolder, getJostracaCodeWriterFileName());
        boolean jostracaCodeWriterExists = jostracaCodeWriter.exists();
        test = test && jostracaCodeWriterExists;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateArgOutputAndWorkFolders() throws Exception {
        String testRoot = TEST_ArgOutputAndWorkFolders;
        boolean test = true;
        String outputFolder = getTestFolder() + SLASH + ARGOUTPUTFOLDER_FOLDER_NAME;
        String workFolder = getTestFolder() + SLASH + ARGWORKFOLDER_FOLDER_NAME;
        sReferencePS.clear();
        sReferencePS.set(NAME_OutputFolder, outputFolder);
        sTestPS.load(new File(getTestFolder() + SLASH + ARGOUTPUTFOLDER_FOLDER_NAME, PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.contains(sReferencePS) && test;
        File jostracaCodeWriter = new File(workFolder, getJostracaCodeWriterFileName());
        test = test && jostracaCodeWriter.exists();
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateBackup() throws Exception {
        String testRoot = TEST_Backup;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("backup", "done");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.contains(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateInternalController() throws Exception {
        String testRoot = TEST_InternalController;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("c", getControllerClass());
        return confirmPropertySet(testRoot, sReferencePS);
    }

    public boolean validateExternalController() throws Exception {
        String testRoot = TEST_ExternalController;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("c", getControllerClass());
        return confirmPropertySet(testRoot, sReferencePS);
    }

    public boolean validateVersion_0_1() throws Exception {
        String testRoot = TEST_Version_0_1;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("body", "bodyCheck");
        sReferencePS.set("init", "initCheck");
        sReferencePS.set("support", "supportCheck");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.contains(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateVersion_0_2() throws Exception {
        String testRoot = TEST_Version_0_2;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("body", "bodyCheck");
        sReferencePS.set("init", "initCheck");
        sReferencePS.set("declare", "declareCheck");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.contains(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public boolean validateVersion_0_3() throws Exception {
        String testRoot = TEST_Version_0_3;
        boolean test = true;
        sReferencePS.clear();
        sReferencePS.set("version_0_3", "version_0_3");
        sTestPS.load(new File(getTestFolder(), PREFIX_RESULT + testRoot + DOT + TXT));
        test = sTestPS.contains(sReferencePS) && test;
        if (!test) {
            displayPropertySets(sTestPS, sReferencePS);
        }
        return test;
    }

    public PropertySet getReferencePS() {
        return sReferencePS;
    }

    public PropertySet getTestPS() {
        return sTestPS;
    }

    protected void generateTemplate(String pTemplateNameSansSuffix) throws Exception {
        generateTemplate(pTemplateNameSansSuffix, "", "");
    }

    protected void generateTemplate(String pTemplateNameSansSuffix, String pExtraJostracaArgs) throws Exception {
        generateTemplate(pTemplateNameSansSuffix, pExtraJostracaArgs, "");
    }

    protected void generateTemplate(String pTemplateNameSansSuffix, String pExtraJostracaArgs, boolean pSetOutputFolder) throws Exception {
        generateTemplate(pTemplateNameSansSuffix, pExtraJostracaArgs, "", pSetOutputFolder);
    }

    protected void generateTemplate(String pTemplateNameSansSuffix, String pExtraJostracaArgs, String pCodeWriterArgs) throws Exception {
        generateTemplate(pTemplateNameSansSuffix, pExtraJostracaArgs, pCodeWriterArgs, true);
    }

    protected void generateTemplate(String pTemplateNameSansSuffix, String pExtraJostracaArgs, String pCodeWriterArgs, boolean pSetOutputFolder) throws Exception {
        String cmd = makeCMDForTemplate(pTemplateNameSansSuffix, pExtraJostracaArgs, pCodeWriterArgs, pSetOutputFolder);
        assertTrue(runCommand(cmd));
    }

    protected abstract String makeCMDForTemplate(String pTemplateNameSansSuffix, String pExtraJostracaArgs, String pCodeWriterArgs, boolean pSetOutputFolder);

    protected void cleanFile(String pFolder, String pFileName) throws IOException {
        File workFilePath = new File(pFolder, pFileName);
        FileUtil.writeFile(workFilePath.getPath(), EMPTY);
    }

    protected void deleteFile(String pFolder, String pFileName) throws IOException {
        File workFilePath = new File(pFolder, pFileName);
        workFilePath.delete();
    }

    protected String getJostracaCmd() {
        if (sSpecifyConfig) {
            return STANDARD_JOSTRACA_CMD + STANDARD_JOSTRACA_CONF_ARG;
        } else {
            return STANDARD_JOSTRACA_CMD;
        }
    }

    protected static void handleArgs(String[] pArgs) {
        if (0 < pArgs.length) {
            String runspec = pArgs[0];
            if (-1 != runspec.indexOf("noconf")) {
                sSpecifyConfig = false;
            }
        }
    }

    protected void displayPropertySets(PropertySet pTestPS, PropertySet pRefPS) {
        System.out.println("test:" + pTestPS);
        System.out.println("ref:" + pRefPS);
    }

    protected boolean confirmPropertySet(String pTestRoot, PropertySet pReferencePS) {
        PropertySet testps = new PropertySet();
        testps.load(new File(getTestFolder(), PREFIX_RESULT + pTestRoot + DOT + TXT));
        boolean test = testps.equals(pReferencePS);
        if (!test) {
            displayPropertySets(testps, pReferencePS);
        }
        return test;
    }
}
