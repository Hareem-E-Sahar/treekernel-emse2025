package statechum.analysis.Erlang;

import static statechum.Helper.checkForCorrectException;
import static statechum.analysis.learning.rpnicore.FsmParser.buildLearnerGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;
import statechum.Configuration;
import statechum.GlobalConfiguration;
import statechum.Configuration.LABELKIND;
import statechum.GlobalConfiguration.G_PROPERTIES;
import statechum.Helper;
import statechum.Helper.whatToRun;
import statechum.Label;
import statechum.analysis.Erlang.ErlangRunner.ERL;
import statechum.analysis.Erlang.ErlangRunner.ErlangThrownException;
import statechum.analysis.Erlang.Signatures.FuncSignature;
import statechum.analysis.Erlang.Signatures.TestTypes;
import statechum.analysis.learning.ErlangOracleLearner;
import statechum.analysis.learning.ErlangOracleLearner.TraceOutcome;
import statechum.analysis.learning.ErlangOracleLearner.TraceOutcome.TRACEOUTCOME;
import statechum.analysis.learning.RPNILearner;
import statechum.analysis.learning.experiments.ExperimentRunner;
import statechum.analysis.learning.experiments.ExperimentRunner.HandleProcessIO;
import statechum.analysis.learning.observers.ProgressDecorator.LearnerEvaluationConfiguration;
import statechum.analysis.learning.rpnicore.AbstractLearnerGraph;
import statechum.analysis.learning.rpnicore.LTL_to_ba;
import statechum.analysis.learning.rpnicore.LearnerGraph;

public class TestErlangModule {

    @Before
    public void beforeTest() {
        if (!TestErlangRunner.testDir.isDirectory()) Assert.assertTrue("could not create " + TestErlangRunner.testDir.getAbsolutePath(), TestErlangRunner.testDir.mkdir());
        ErlangModule.flushRegistry();
    }

    @After
    public void afterTest() {
        ExperimentRunner.zapDir(TestErlangRunner.testDir);
    }

    @Test
    public void testRunParserFailure1() {
        statechum.Helper.checkForCorrectException(new statechum.Helper.whatToRun() {

            @Override
            public void run() throws IOException {
                ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File("ErlangExamples/WibbleMonster/wibble.erla")));
            }
        }, IllegalArgumentException.class, "Invalid module");
    }

    /** This is one of those odd tests which behaves differently on different operating systems,
	 * hence there are two different cases.
	 * @throws IOException 
	 */
    @Test
    public void testRunParserFailure2() throws IOException {
        final String wibbleDir = "ErlangExamples/WibbleMonster";
        final File fileWibble = new File(wibbleDir, "Wibble.erl"), filewibble = new File(wibbleDir, "wibble.erl");
        if (!fileWibble.canRead()) statechum.Helper.checkForCorrectException(new statechum.Helper.whatToRun() {

            @Override
            public void run() throws IOException {
                ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(fileWibble));
            }
        }, IOException.class, "does not exist"); else {
            File beam = new File(ErlangRunner.getName(fileWibble, ERL.BEAM));
            if (beam.canRead()) Assert.assertTrue(beam.delete());
            ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(filewibble), true);
            File plt = new File(ErlangRunner.getName(fileWibble, ERL.PLT));
            if (plt.canRead()) Assert.assertTrue(plt.delete());
            statechum.Helper.checkForCorrectException(new statechum.Helper.whatToRun() {

                @Override
                public void run() throws IOException {
                    ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(fileWibble), true);
                }
            }, RuntimeException.class, "Invalid file name");
        }
    }

    /** Only used for testing against runTyperAsAProcessInsideErlang. 
     * @throws IOException */
    protected static String runTyperAsAProcess(File f) throws IOException {
        ErlangRunner.compileErl(f, ErlangRunner.getRunner());
        {
            Map<String, String> environment = System.getenv();
            String[] envp = new String[environment.size() + 1];
            int i = 0;
            for (Entry<String, String> entry : System.getenv().entrySet()) envp[i++] = entry.getKey() + "=" + entry.getValue();
            envp[i++] = "HOME=" + f.getParentFile().getAbsolutePath();
            Process p = Runtime.getRuntime().exec(new String[] { ErlangRunner.getErlangBin() + "dialyzer", "--build_plt", "--output_plt", ErlangRunner.getName(f, ERL.PLT), ErlangRunner.getName(f, ERL.BEAM) }, envp, f.getParentFile());
            ErlangRunner.dumpProcessOutputOnFailure("dialyzer", p);
        }
        Process p = Runtime.getRuntime().exec(new String[] { ErlangRunner.getErlangBin() + "typer", "--plt", ErlangRunner.getName(f, ERL.PLT), ErlangRunner.getName(f, ERL.ERL) }, null, f.getParentFile());
        final StringBuffer err = new StringBuffer(), out = new StringBuffer();
        ExperimentRunner.dumpStreams(p, LTL_to_ba.timeBetweenHearbeats, new HandleProcessIO() {

            @Override
            public void OnHeartBeat() {
            }

            @Override
            public void StdErr(StringBuffer b) {
                err.append(b);
            }

            @Override
            public void StdOut(StringBuffer b) {
                out.append(b);
            }
        });
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            ;
        }
        if (p.exitValue() != 0) throw new IllegalArgumentException("Failure running " + f.getName() + "\n" + err + (err.length() > 0 ? "\n" : "") + out);
        return out.toString();
    }

    /** Only used for testing against runTyperAsAProcess. 
     * @throws IOException */
    protected String runDialyzerAndTyperAsAProcessInsideErlang(File f) throws IOException {
        ErlangRunner erl = ErlangRunner.getRunner();
        ErlangRunner.compileErl(f, erl);
        OtpErlangObject otpArgs[] = new OtpErlangObject[] { null, new OtpErlangList(new OtpErlangObject[] { new OtpErlangString(ErlangRunner.getName(f, ERL.BEAM)) }), new OtpErlangString(ErlangRunner.getName(f, ERL.PLT)), new OtpErlangList(new OtpErlangObject[] { new OtpErlangString(ErlangRunner.getName(f, ERL.ERL)) }), new OtpErlangAtom("text") };
        otpArgs[0] = new OtpErlangAtom("dialyzer");
        erl.call(otpArgs, "Could not run dialyzer");
        otpArgs[0] = new OtpErlangAtom("typer");
        OtpErlangTuple response = erl.call(otpArgs, "Could not run typer");
        return ((OtpErlangString) response.elementAt(1)).stringValue();
    }

    /** Only used for testing against runTyperAsAProcess. 
     * @throws IOException */
    protected String runOnlyTyperAsAProcessInsideErlang(File f) throws IOException {
        ErlangRunner erl = ErlangRunner.getRunner();
        ErlangRunner.compileErl(f, erl);
        OtpErlangObject otpArgs[] = new OtpErlangObject[] { null, new OtpErlangList(new OtpErlangObject[] { new OtpErlangString(ErlangRunner.getName(f, ERL.BEAM)) }), new OtpErlangString(ErlangRunner.getName(f, ERL.PLT)), new OtpErlangList(new OtpErlangObject[] { new OtpErlangString(ErlangRunner.getName(f, ERL.ERL)) }), new OtpErlangAtom("text") };
        otpArgs[0] = new OtpErlangAtom("typer");
        OtpErlangTuple response = erl.call(otpArgs, "Could not run typer");
        return ((OtpErlangString) response.elementAt(1)).stringValue();
    }

    /** Loads the contents of a file into a string. 
     * @param file file to load
     * 
     * @throws IOException 
     */
    private static String loadFile(File file) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(file));
        StringBuffer result = new StringBuffer();
        String line;
        while ((line = input.readLine()) != null) {
            result.append(line);
            result.append('\n');
        }
        input.close();
        return result.toString();
    }

    public void testConsistencyBetweenOriginalAndOurTyperHelper(File origFile, boolean ignoreModuleInfo) throws IOException {
        Assert.assertFalse(erlangFile.equals(erlangFileOther));
        String moduleName = ErlangRunner.getName(origFile, ERL.MOD);
        final String someErlang = loadFile(origFile);
        String typerInRunner = null, typerAsProcess = null;
        {
            Assert.assertTrue(new File(TestErlangRunner.testDir.getAbsolutePath() + File.separator + "A").mkdir());
            String fileA = TestErlangRunner.testDir.getAbsolutePath() + File.separator + "A" + File.separator + moduleName + ".erl";
            Writer wr = new FileWriter(fileA);
            wr.write(someErlang);
            wr.close();
            typerInRunner = runDialyzerAndTyperAsAProcessInsideErlang(new File(fileA)).replace("\\\\", "\\").replace(fileA, "FileName");
        }
        {
            Assert.assertTrue(new File(TestErlangRunner.testDir.getAbsolutePath() + File.separator + "B").mkdir());
            String fileB = TestErlangRunner.testDir.getAbsolutePath() + File.separator + "B" + File.separator + moduleName + ".erl";
            Writer wr = new FileWriter(fileB);
            wr.write(someErlang);
            wr.close();
            typerAsProcess = runTyperAsAProcess(new File(fileB)).replace("\\\\", "\\").replace(fileB, "FileName");
        }
        if (ignoreModuleInfo) {
            typerInRunner = typerInRunner.replaceAll("-spec module_info.*\n", "");
            typerAsProcess = typerAsProcess.replaceAll("-spec module_info.*\n", "");
        }
        Assert.assertEquals(typerAsProcess, typerInRunner);
    }

    @Test
    public void testConsistencyBetweenOriginalAndOurTyper1() throws IOException {
        testConsistencyBetweenOriginalAndOurTyperHelper(new File("ErlangExamples/WibbleMonster/wibble.erl"), false);
    }

    @Test
    public void testConsistencyBetweenOriginalAndOurTyper2() throws IOException {
        testConsistencyBetweenOriginalAndOurTyperHelper(new File("ErlangExamples/locker/locker.erl"), true);
    }

    @Test
    public void testTyperWithInvalidPLTerror() throws IOException {
        final File file = new File("ErlangExamples/locker/locker.erl");
        new File(ErlangRunner.getName(file, ERL.PLT)).delete();
        Writer wr = new FileWriter(ErlangRunner.getName(file, ERL.PLT));
        wr.write("junk");
        wr.close();
        Helper.checkForCorrectException(new statechum.Helper.whatToRun() {

            @Override
            public void run() throws IOException {
                runOnlyTyperAsAProcessInsideErlang(file);
            }
        }, ErlangThrownException.class, "has thrown");
        new File(ErlangRunner.getName(file, ERL.BEAM)).delete();
        new File(ErlangRunner.getName(file, ERL.PLT)).delete();
    }

    @Test
    public void testTyperWithInvalidPLT() throws IOException {
        File file = new File("ErlangExamples/locker/locker.erl");
        new File(ErlangRunner.getName(file, ERL.PLT)).delete();
        Writer wr = new FileWriter(ErlangRunner.getName(file, ERL.PLT));
        wr.write("junk");
        wr.close();
        ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(file));
        runOnlyTyperAsAProcessInsideErlang(file);
    }

    protected final Configuration defaultConfig = Configuration.getDefaultConfiguration().copy();

    @Test
    public void testExtractFunctionTypes1() throws IOException {
        GlobalConfiguration.getConfiguration().getProperty(G_PROPERTIES.TEMP);
        File file = new File("ErlangExamples/WibbleMonster/wibble.erl");
        ErlangModule mod = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(file));
        Assert.assertTrue(mod.behaviour instanceof OTPGenServerBehaviour);
        Assert.assertTrue(mod.behaviour.dependencies.isEmpty());
        for (FuncSignature s : mod.sigs.values()) {
            FuncSignature newSig = new FuncSignature(defaultConfig, ErlangLabel.parseText(s.toErlangTerm()), null);
            Assert.assertEquals(s, newSig);
            Assert.assertEquals(s, new FuncSignature(defaultConfig, ErlangLabel.parseText(newSig.toErlangTerm()), null));
        }
    }

    @Test
    public void testExtractFunctionTypes2() throws IOException {
        GlobalConfiguration.getConfiguration().getProperty(G_PROPERTIES.TEMP);
        File file = new File("ErlangExamples/locker/locker.erl");
        ErlangModule mod = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(file));
        Assert.assertTrue(mod.behaviour instanceof OTPGenServerBehaviour);
        Assert.assertTrue(mod.behaviour.dependencies.isEmpty());
        for (FuncSignature s : mod.sigs.values()) {
            FuncSignature newSig = new FuncSignature(defaultConfig, ErlangLabel.parseText(s.toErlangTerm()), null);
            Assert.assertEquals(s, newSig);
            Assert.assertEquals(s, new FuncSignature(defaultConfig, ErlangLabel.parseText(newSig.toErlangTerm()), null));
        }
    }

    @Test
    public void testWibbleAlphabet() throws IOException {
        ErlangModule mod = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File("ErlangExamples/WibbleMonster/wibble.erl")));
        Assert.assertEquals("[" + "{" + ErlangLabel.missingFunction + ",'call','xyz','here_kirill'}," + "{" + ErlangLabel.missingFunction + ",'call','xyz','listing'}," + "{" + ErlangLabel.missingFunction + ",'call','xyz','wibbling'}," + "{" + ErlangLabel.missingFunction + ",'call','xyz','wobbling'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble'],'here_kirill'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble'],'listing'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble'],'wibbling'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble'],'wobbling'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble' | 'AnyListElemWibble'],'here_kirill'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble' | 'AnyListElemWibble'],'listing'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble' | 'AnyListElemWibble'],'wibbling'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble' | 'AnyListElemWibble'],'wobbling'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble','AnyListElemWibble'],'here_kirill'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble','AnyListElemWibble'],'listing'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble','AnyListElemWibble'],'wibbling'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble','AnyListElemWibble'],'wobbling'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble','AnyListElemWibble' | 'AnyListElemWibble'],'here_kirill'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble','AnyListElemWibble' | 'AnyListElemWibble'],'listing'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble','AnyListElemWibble' | 'AnyListElemWibble'],'wibbling'}," + "{" + ErlangLabel.missingFunction + ",'call',['AnyListElemWibble','AnyListElemWibble' | 'AnyListElemWibble'],'wobbling'}," + "{" + ErlangLabel.missingFunction + ",'cast','JustAnythingA',[]}," + "{" + ErlangLabel.missingFunction + ",'cast','JustAnythingA',[128]}," + "{" + ErlangLabel.missingFunction + ",'cast','JustAnythingA',[128,128]}," + "{" + ErlangLabel.missingFunction + ",'cast','JustAnythingA',[128,128,128]}," + "{" + ErlangLabel.missingFunction + ",'cast',[],[]}," + "{" + ErlangLabel.missingFunction + ",'cast',[],[128]}," + "{" + ErlangLabel.missingFunction + ",'cast',[],[128,128]}," + "{" + ErlangLabel.missingFunction + ",'cast',[],[128,128,128]}," + "{" + ErlangLabel.missingFunction + ",'cast',['WibbleA'],[]}," + "{" + ErlangLabel.missingFunction + ",'cast',['WibbleA'],[128]}," + "{" + ErlangLabel.missingFunction + ",'cast',['WibbleA'],[128,128]}," + "{" + ErlangLabel.missingFunction + ",'cast',['WibbleA'],[128,128,128]}," + "{" + ErlangLabel.missingFunction + ",'cast',['WibbleA','WobbleA'],[]}," + "{" + ErlangLabel.missingFunction + ",'cast',['WibbleA','WobbleA'],[128]}," + "{" + ErlangLabel.missingFunction + ",'cast',['WibbleA','WobbleA'],[128,128]}," + "{" + ErlangLabel.missingFunction + ",'cast',['WibbleA','WobbleA'],[128,128,128]}," + "{" + ErlangLabel.missingFunction + ",'info','JustAnythingA',{'noreply','JustAnythingA'}}," + "{" + ErlangLabel.missingFunction + ",'info','JustAnythingA',{'noreply',[]}}," + "{" + ErlangLabel.missingFunction + ",'info','JustAnythingA',{'noreply',['WibbleA']}}," + "{" + ErlangLabel.missingFunction + ",'info','JustAnythingA',{'noreply',['WibbleA','WobbleA']}}," + "{" + ErlangLabel.missingFunction + ",'info',[],{'noreply','JustAnythingA'}}," + "{" + ErlangLabel.missingFunction + ",'info',[],{'noreply',[]}}," + "{" + ErlangLabel.missingFunction + ",'info',[],{'noreply',['WibbleA']}}," + "{" + ErlangLabel.missingFunction + ",'info',[],{'noreply',['WibbleA','WobbleA']}}," + "{" + ErlangLabel.missingFunction + ",'info',['WibbleA'],{'noreply','JustAnythingA'}}," + "{" + ErlangLabel.missingFunction + ",'info',['WibbleA'],{'noreply',[]}}," + "{" + ErlangLabel.missingFunction + ",'info',['WibbleA'],{'noreply',['WibbleA']}}," + "{" + ErlangLabel.missingFunction + ",'info',['WibbleA'],{'noreply',['WibbleA','WobbleA']}}," + "{" + ErlangLabel.missingFunction + ",'info',['WibbleA','WobbleA'],{'noreply','JustAnythingA'}}," + "{" + ErlangLabel.missingFunction + ",'info',['WibbleA','WobbleA'],{'noreply',[]}}," + "{" + ErlangLabel.missingFunction + ",'info',['WibbleA','WobbleA'],{'noreply',['WibbleA']}}," + "{" + ErlangLabel.missingFunction + ",'info',['WibbleA','WobbleA'],{'noreply',['WibbleA','WobbleA']}}," + "{" + ErlangLabel.missingFunction + ",'init','JustAnythingA','ok'}," + "{" + ErlangLabel.missingFunction + ",'init',[],'ok'}," + "{" + ErlangLabel.missingFunction + ",'init',['WibbleA'],'ok'}," + "{" + ErlangLabel.missingFunction + ",'init',['WibbleA','WobbleA'],'ok'}" + "]", TestTypes.getAlphabetAsString(mod));
    }

    @Test
    public void testAttemptTracesNotInAlphabet() {
        GlobalConfiguration.getConfiguration().getProperty(G_PROPERTIES.TEMP);
        LearnerEvaluationConfiguration evalConf = new LearnerEvaluationConfiguration(null);
        evalConf.config = Configuration.getDefaultConfiguration().copy();
        final String moduleName = "locker";
        evalConf.config.setErlangModuleName(moduleName);
        evalConf.config.setErlangSourceFile(new File("ErlangExamples/locker", moduleName + ".erl"));
        evalConf.config.setLabelKind(LABELKIND.LABEL_ERLANG);
        final ErlangOracleLearner learner = new ErlangOracleLearner(null, evalConf);
        ErlangModule mod = ErlangModule.findModule(evalConf.config.getErlangModuleName());
        final ErlangLabel initLabel = mod.behaviour.convertErlToMod(AbstractLearnerGraph.generateNewLabel("{" + ErlangLabel.missingFunction + ",'init','AnyWibble','ok'}", evalConf.config)), labelLock = mod.behaviour.convertErlToMod(AbstractLearnerGraph.generateNewLabel("{" + ErlangLabel.missingFunction + ",'call','lock',{'ok','locked'}}", evalConf.config));
        final ErlangLabel labelInvalidRead = new ErlangLabel(labelLock.function, labelLock.callName, labelLock.input, new OtpErlangInt(88));
        statechum.Helper.checkForCorrectException(new statechum.Helper.whatToRun() {

            @Override
            public void run() {
                learner.askErlang(Arrays.asList(new Label[] { initLabel, labelLock, labelInvalidRead }));
            }
        }, IllegalArgumentException.class, "does not belong");
    }

    @Test
    public void testAttemptTracesWrongModule() throws IOException {
        GlobalConfiguration.getConfiguration().getProperty(G_PROPERTIES.TEMP);
        LearnerEvaluationConfiguration evalConf = new LearnerEvaluationConfiguration(null);
        evalConf.config = ErlangModule.setupErlangConfiguration(new File("ErlangExamples/locker/locker" + ErlangRunner.ERL.ERL.toString()));
        final ErlangOracleLearner learner = new ErlangOracleLearner(null, evalConf);
        Configuration exporterConfiguration = ErlangModule.setupErlangConfiguration(new File("ErlangExamples/exporter/exporter" + ErlangRunner.ERL.ERL.toString()));
        ErlangModule modLocker = ErlangModule.findModule(evalConf.config.getErlangModuleName()), modExporter = ErlangModule.loadModule(exporterConfiguration);
        final ErlangLabel pushLabel = modExporter.behaviour.convertErlToMod(AbstractLearnerGraph.generateNewLabel("{" + ErlangLabel.missingFunction + ",'exporter:push/1',['JustAnythingA'],'ok'}", exporterConfiguration));
        statechum.Helper.checkForCorrectException(new statechum.Helper.whatToRun() {

            @Override
            public void run() {
                learner.askErlang(Arrays.asList(new Label[] { pushLabel }));
            }
        }, IllegalArgumentException.class, "but attempting to call");
    }

    @Test
    public void testAttemptTraces() {
        GlobalConfiguration.getConfiguration().getProperty(G_PROPERTIES.TEMP);
        LearnerEvaluationConfiguration evalConf = new LearnerEvaluationConfiguration(null);
        final String moduleName = "locker";
        evalConf.config = ErlangModule.setupErlangConfiguration(new File("ErlangExamples/locker", moduleName + ErlangRunner.ERL.ERL.toString()));
        ErlangOracleLearner learner = new ErlangOracleLearner(null, evalConf);
        ErlangModule mod = ErlangModule.findModule(moduleName);
        Assert.assertTrue(mod.behaviour instanceof OTPGenServerBehaviour);
        Assert.assertTrue(mod.behaviour.dependencies.isEmpty());
        ErlangLabel initLabel = mod.behaviour.convertErlToMod(AbstractLearnerGraph.generateNewLabel("{" + ErlangLabel.missingFunction + ",'init','AnyWibble','ok'}", evalConf.config)), labelLock = mod.behaviour.convertErlToMod(AbstractLearnerGraph.generateNewLabel("{" + ErlangLabel.missingFunction + ",'call','lock',{'ok','locked'}}", evalConf.config)), labelRead = mod.behaviour.convertErlToMod(AbstractLearnerGraph.generateNewLabel("{" + ErlangLabel.missingFunction + ",'call','read','AnyWibble'}", evalConf.config)), labelWrite = mod.behaviour.convertErlToMod(AbstractLearnerGraph.generateNewLabel("{" + ErlangLabel.missingFunction + ",'call',{'write','AnyWibble'},{'ok','AnyWibble'}}", evalConf.config));
        mod.behaviour.getAlphabet().add(initLabel);
        mod.behaviour.getAlphabet().add(labelLock);
        mod.behaviour.getAlphabet().add(labelRead);
        mod.behaviour.getAlphabet().add(labelWrite);
        List<Label> trace = Arrays.asList(new Label[] { initLabel, labelLock });
        TraceOutcome tr = learner.askErlang(trace);
        Assert.assertEquals(TRACEOUTCOME.TRACE_OK, tr.outcome);
        Assert.assertEquals("[{" + ErlangLabel.missingFunction + ",'init','AnyWibble','ok'},{" + ErlangLabel.missingFunction + ",'call','lock',{'ok','locked'}}]", RPNILearner.questionToString(Arrays.asList(tr.answerDetails)));
        tr = learner.askErlang(Arrays.asList(new Label[] { initLabel, labelLock, labelLock }));
        Assert.assertEquals(TRACEOUTCOME.TRACE_FAIL, tr.outcome);
        Assert.assertEquals("[{" + ErlangLabel.missingFunction + ",'init','AnyWibble','ok'},{" + ErlangLabel.missingFunction + ",'call','lock',{'ok','locked'}},{" + ErlangLabel.missingFunction + ",'call','lock',{'ok','locked'}}]", RPNILearner.questionToString(Arrays.asList(tr.answerDetails)));
        tr = learner.askErlang(Arrays.asList(new Label[] { initLabel, labelLock, labelWrite, labelRead }));
        Assert.assertEquals(TRACEOUTCOME.TRACE_OK, tr.outcome);
        Assert.assertEquals("[{" + ErlangLabel.missingFunction + ",'init','AnyWibble','ok'},{" + ErlangLabel.missingFunction + ",'call','lock',{'ok','locked'}},{" + ErlangLabel.missingFunction + ",'call',{'write','AnyWibble'},{'ok','AnyWibble'}},{" + ErlangLabel.missingFunction + ",'call','read','AnyWibble'}]", RPNILearner.questionToString(Arrays.asList(tr.answerDetails)));
        ErlangLabel lbl = tr.answerDetails[3];
        tr.answerDetails[3] = new ErlangLabel(lbl.function, lbl.callName, lbl.input, new OtpErlangAtom("aa"));
        mod.behaviour.getAlphabet().add(tr.answerDetails[3]);
        tr = learner.askErlang(Arrays.asList(tr.answerDetails));
        Assert.assertEquals(TRACEOUTCOME.TRACE_DIFFERENTOUTPUT, tr.outcome);
        Assert.assertEquals("[{" + ErlangLabel.missingFunction + ",'init','AnyWibble','ok'},{" + ErlangLabel.missingFunction + ",'call','lock',{'ok','locked'}},{" + ErlangLabel.missingFunction + ",'call',{'write','AnyWibble'},{'ok','AnyWibble'}},{" + ErlangLabel.missingFunction + ",'call','read','AnyWibble'}]", RPNILearner.questionToString(Arrays.asList(tr.answerDetails)));
    }

    @Test
    public void testTraces() throws IOException {
        Configuration config = ErlangModule.setupErlangConfiguration(new File("ErlangExamples/locker/locker.erl"));
        ErlangModule.loadModule(config);
        final String LBL1 = "{call, lock}", LBL2 = "{call, unlock}";
        final LearnerGraph gr = buildLearnerGraph("A- " + LBL1 + " ->B-" + LBL2 + "->B", "testConvertToModuleFailure1", config);
        Iterator<Label> lblIter = gr.pathroutines.computeAlphabet().iterator();
        ErlangLabel lbl1 = (ErlangLabel) lblIter.next(), lbl2 = (ErlangLabel) lblIter.next();
        List<Label> trace = AbstractLearnerGraph.buildList(Arrays.asList(new String[] { LBL1, LBL2, LBL2 }), config), expected = Arrays.asList(new Label[] { lbl1, lbl2, lbl2 });
        Assert.assertEquals(expected, trace);
    }

    /** The name of test file - should not be static to ensure it picks the value of TestErlangRunner's variable
     * after it has been initialised.
     */
    protected final String erlangFile = TestErlangRunner.testDir.getAbsolutePath() + File.separator + "testFile.erl", erlangFileOther = TestErlangRunner.testDir.getAbsolutePath() + File.separator + "testFileOther.erl";

    @Test
    public void testInvalidModuleName() throws IOException {
        Writer wr = new FileWriter(erlangFile);
        wr.write("-module(testFile).\n");
        wr.close();
        File origFile = new File(erlangFile);
        ErlangRunner.compileErl(origFile, ErlangRunner.getRunner());
        final File renamedFile = new File(TestErlangRunner.testDir.getAbsolutePath() + File.separator + "otherFile.erl");
        origFile.renameTo(new File(ErlangRunner.getName(renamedFile, ERL.ERL)));
        new File(ErlangRunner.getName(origFile, ERL.BEAM)).renameTo(new File(ErlangRunner.getName(renamedFile, ERL.BEAM)));
        checkForCorrectException(new whatToRun() {

            @Override
            public void run() throws IOException {
                ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(renamedFile));
            }
        }, RuntimeException.class, "Invalid file name");
    }

    @Test
    public void testLoadModule1() throws IOException {
        File fileLocker = new File("ErlangExamples/locker/locker.erl");
        Assert.assertNull(ErlangModule.findModule("locker"));
        ErlangModule modA = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(fileLocker));
        Assert.assertSame(modA, ErlangModule.findModule("locker"));
    }

    @Test
    public void testLoadModule2() throws IOException {
        final File fileLocker = new File("ErlangExamples/locker/locker.erl");
        Assert.assertNull(ErlangModule.findModule("locker"));
        ErlangModule mod = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(fileLocker));
        Assert.assertSame(mod, ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(fileLocker)));
    }

    /** Forces the reload and checks that outcome is different every time. */
    @Test
    public void testLoadModule3() throws IOException {
        final File fileLocker = new File("ErlangExamples/locker/locker.erl");
        Configuration loadConfiguration = Configuration.getDefaultConfiguration().copy();
        loadConfiguration.setErlangSourceFile(fileLocker);
        loadConfiguration.setErlangModuleName(fileLocker.getName());
        loadConfiguration.setLabelKind(LABELKIND.LABEL_ERLANG);
        Assert.assertNull(ErlangModule.findModule("locker"));
        ErlangModule mod = ErlangModule.loadModule(loadConfiguration, true);
        Assert.assertNotSame(mod, ErlangModule.loadModule(loadConfiguration, true));
    }

    protected static final String stdFunctions = "\nhandle_call(_,_,_)->{reply,ok,5}.\nhandle_cast(_,_)->{noreply,ok,5}.\nhandle_info(_,_)->{reply,ok}.\ninit(_)->{ok,5}.\n";

    @Test
    public void testExtraAttribute1() throws IOException {
        Writer wr = new FileWriter(erlangFile);
        wr.write("-module(testFile).\n-behaviour(gen_server).\n-justsomething(aa)." + stdFunctions);
        wr.close();
        ErlangModule mod = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile)));
        Assert.assertTrue(mod.ignoredBehaviours.isEmpty());
    }

    @Test
    public void testInvalidAttribute1() throws IOException {
        Writer wr = new FileWriter(erlangFile);
        wr.write("-module(testFile).\n-behaviour(gen_server).\n-behaviour(aa)." + stdFunctions);
        wr.close();
        ErlangModule mod = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile)));
        Assert.assertTrue(mod.ignoredBehaviours.contains("aa"));
    }

    @Test
    public void testInvalidAttribute2() throws IOException {
        Writer wr = new FileWriter(erlangFile);
        wr.write("-module(testFile).\n-behaviour(56)." + stdFunctions);
        wr.close();
        checkForCorrectException(new whatToRun() {

            @Override
            public void run() throws IOException {
                ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile)));
            }
        }, IllegalArgumentException.class, "\"8\" is of the wrong kind");
    }

    @Test
    public void testInvalidAttribute3() throws IOException {
        Writer wr = new FileWriter(erlangFile);
        wr.write("-module(testFile).\n-behaviour(565)." + stdFunctions);
        wr.close();
        checkForCorrectException(new whatToRun() {

            @Override
            public void run() throws IOException {
                ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile)));
            }
        }, IllegalArgumentException.class, "565 is of the wrong type");
    }

    @Test
    public void testInvalidAttribute4() throws IOException {
        Writer wr = new FileWriter(erlangFile);
        wr.write("-module(testFile).\n-behaviour(\"junk\")." + stdFunctions);
        wr.close();
        checkForCorrectException(new whatToRun() {

            @Override
            public void run() throws IOException {
                ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile)));
            }
        }, IllegalArgumentException.class, "\"junk\" is of the wrong kind");
    }

    @Test
    public void testSpecificValue1() throws IOException {
        Writer wr = new FileWriter(erlangFile);
        wr.write("-module(testFile).\n-behaviour(gen_server)." + stdFunctions);
        wr.close();
        Assert.assertEquals("gen_server_wrapper", ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile))).behaviour.getWrapperName());
    }

    @Test
    public void testSpecificValue2() throws IOException {
        Writer wr = new FileWriter(erlangFile);
        wr.write("-module(testFile).\n-behaviour(gen_fsm).\n" + "\nhandle_event(_,_,_)->{reply,ok,5}.\nhandle_sync_event(_,_)->{noreply,ok,5}.\n\ninit(_)->{ok,5}.\nhandle_info(_,_)->{reply,ok}.\n");
        wr.close();
        Assert.assertEquals("gen_fsm_wrapper", ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile))).behaviour.getWrapperName());
    }

    /** One of the interface functions is missing. */
    @Test
    public void testSpecificValueFail() throws IOException {
        Writer wr = new FileWriter(erlangFile);
        wr.write("-module(testFile).\n-behaviour(gen_server).\n" + "\nhandle_call(_,_,_)->{reply,ok,5}.\nhandle_cast(_,_)->{noreply,ok,5}.\n\ninitRenamed(_)->{ok,5}.\nhandle_info(_,_)->{reply,ok}.\n");
        wr.close();
        Assert.assertEquals("[{" + ErlangLabel.missingFunction + ",'call','JustAnythingA','ok'}," + "{" + ErlangLabel.missingFunction + ",'call',[],'ok'}," + "{" + ErlangLabel.missingFunction + ",'call',['WibbleA'],'ok'}," + "{" + ErlangLabel.missingFunction + ",'call',['WibbleA','WobbleA'],'ok'}," + "{" + ErlangLabel.missingFunction + ",'cast','JustAnythingA','ok'}," + "{" + ErlangLabel.missingFunction + ",'cast',[],'ok'}," + "{" + ErlangLabel.missingFunction + ",'cast',['WibbleA'],'ok'}," + "{" + ErlangLabel.missingFunction + ",'cast',['WibbleA','WobbleA'],'ok'}," + "{" + ErlangLabel.missingFunction + ",'info','JustAnythingA',{'reply','ok'}}," + "{" + ErlangLabel.missingFunction + ",'info',[],{'reply','ok'}}," + "{" + ErlangLabel.missingFunction + ",'info',['WibbleA'],{'reply','ok'}}," + "{" + ErlangLabel.missingFunction + ",'info',['WibbleA','WobbleA'],{'reply','ok'}}]", TestTypes.getAlphabetAsString(ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile)))));
    }

    @Test
    public void testDependencies1() throws IOException {
        Writer wr = new FileWriter(erlangFile);
        wr.write("-module(testFile).\n-behaviour(gen_server)." + stdFunctions);
        wr.close();
        Assert.assertTrue(ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile))).behaviour.dependencies.isEmpty());
    }

    @Test
    public void testDependencies2() throws IOException {
        final String erlangFile2 = TestErlangRunner.testDir.getAbsolutePath() + File.separator + "testFile2.erl";
        Writer wr = new FileWriter(erlangFile);
        wr.write("-module(testFile).\n-export[funct/1].\nfunct(5) -> ok.");
        wr.close();
        wr = new FileWriter(erlangFile2);
        wr.write("-module(testFile2).\n-export[f/1].\nf(2) -> testFile:funct(5).");
        wr.close();
        Assert.assertTrue(ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile))).behaviour.dependencies.isEmpty());
        Collection<String> deps = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile2))).behaviour.dependencies;
        Assert.assertEquals(1, deps.size());
        Assert.assertEquals("testFile", deps.toArray()[0]);
    }

    @Test
    public void testLoadExportsEmptySetBecauseFunctionDoesNotReturnAValue() throws IOException {
        final String someErlang = "-module(testFile).\n-export([testFun/1]).\ntestFun([Arg])->io:format(\"42~n\"),halt().\n";
        Writer wr = new FileWriter(erlangFile);
        wr.write(someErlang);
        wr.close();
        ErlangModule mod = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile)));
        Assert.assertTrue(mod.behaviour.getAlphabet().isEmpty());
    }

    @Test
    public void testLoadExportsEmptySet1() throws IOException {
        final String someErlang = "-module(testFile).\n\n";
        Writer wr = new FileWriter(erlangFile);
        wr.write(someErlang);
        wr.close();
        ErlangModule mod = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile)));
        Assert.assertTrue(mod.behaviour.getAlphabet().isEmpty());
    }

    @Test
    public void testLoadExportsEmptySet2() throws IOException {
        final String someErlang = "-module(testFile).\ntestFun([Arg])->io:format(\"42~n\"),halt().\n";
        Writer wr = new FileWriter(erlangFile);
        wr.write(someErlang);
        wr.close();
        ErlangModule mod = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile)));
        Assert.assertTrue(mod.behaviour.getAlphabet().isEmpty());
    }

    @Test
    public void testLoadExports1() throws IOException {
        final String someErlang = "-module(testFile).\n-export([testFun/1]).\ntestFun([Arg])->42.\n";
        Writer wr = new FileWriter(erlangFile);
        wr.write(someErlang);
        wr.close();
        ErlangModule mod = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile)));
        Assert.assertEquals("[{" + ErlangLabel.missingFunction + ",'testFile:testFun/1',[[]],42}," + "{" + ErlangLabel.missingFunction + ",'testFile:testFun/1',[['AnyListElemWibble']],42}," + "{" + ErlangLabel.missingFunction + ",'testFile:testFun/1',[['AnyListElemWibble','AnyListElemWibble']],42}" + "]", TestTypes.getAlphabetAsString(mod));
    }

    @Test
    public void testLoadExports2() throws IOException {
        final String someErlang = "-module(testFile).\n-export([testFun/1]).\ntestFun([Arg])->42.\naFun(34)->33.";
        Writer wr = new FileWriter(erlangFile);
        wr.write(someErlang);
        wr.close();
        ErlangModule mod = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile)));
        Assert.assertEquals("[{" + ErlangLabel.missingFunction + ",'testFile:testFun/1',[[]],42}," + "{" + ErlangLabel.missingFunction + ",'testFile:testFun/1',[['AnyListElemWibble']],42}," + "{" + ErlangLabel.missingFunction + ",'testFile:testFun/1',[['AnyListElemWibble','AnyListElemWibble']],42}" + "]", TestTypes.getAlphabetAsString(mod));
    }

    @Test
    public void testLoadIgnoreFunctions() throws IOException {
        final String someErlang = "-module(testFile).\n-export([testFun/1]).\ntestFun([Arg])->testFun([Arg]).\n";
        Writer wr = new FileWriter(erlangFile);
        wr.write(someErlang);
        wr.close();
        ErlangModule mod = ErlangModule.loadModule(ErlangModule.setupErlangConfiguration(new File(erlangFile)));
        Assert.assertTrue(mod.behaviour.getAlphabet().isEmpty());
        Assert.assertTrue(mod.ignoredFunctions.contains("testFile:testFun/1"));
    }

    @Test
    public void testLoadExportsZeroArity1() throws IOException {
        final String someErlang = "-module(testFile).\n-export([testFun/1]).\ntestFun([Arg])->42.\naFun()->33.";
        Writer wr = new FileWriter(erlangFile);
        wr.write(someErlang);
        wr.close();
        Configuration config = ErlangModule.setupErlangConfiguration(new File(erlangFile));
        config.setErlangAlphabetAnyListLength(1);
        ErlangModule mod = ErlangModule.loadModule(config);
        Assert.assertEquals("[{" + ErlangLabel.missingFunction + ",'testFile:testFun/1',[[]],42}," + "{" + ErlangLabel.missingFunction + ",'testFile:testFun/1',[['AnyListElemWibble']],42}" + "]", TestTypes.getAlphabetAsString(mod));
    }

    @Test
    public void testLoadExportsZeroArity2() throws IOException {
        final String someErlang = "-module(testFile).\n-export([testFun/1,aFun/0]).\ntestFun([Arg])->42.\naFun()->33.";
        Writer wr = new FileWriter(erlangFile);
        wr.write(someErlang);
        wr.close();
        Configuration config = ErlangModule.setupErlangConfiguration(new File(erlangFile));
        config.setErlangAlphabetAnyListLength(1);
        ErlangModule mod = ErlangModule.loadModule(config);
        Assert.assertEquals("[{" + ErlangLabel.missingFunction + ",'testFile:aFun/0',[],33}," + "{" + ErlangLabel.missingFunction + ",'testFile:testFun/1',[[]],42}," + "{" + ErlangLabel.missingFunction + ",'testFile:testFun/1',[['AnyListElemWibble']],42}" + "]", TestTypes.getAlphabetAsString(mod));
    }
}
