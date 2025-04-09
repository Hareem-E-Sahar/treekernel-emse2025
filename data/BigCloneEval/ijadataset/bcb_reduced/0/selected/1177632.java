package net.boogie.calamari.neural.learn;

import java.util.Arrays;
import java.util.Set;
import net.boogie.calamari.CalamariTestCase;
import net.boogie.calamari.domain.exception.CalamariException;
import net.boogie.calamari.domain.utils.ArrayUtils;
import net.boogie.calamari.neural.function.activation.SigmoidFunction;
import net.boogie.calamari.neural.model.INode;
import net.boogie.calamari.neural.network.FeedForwardNetwork;
import net.boogie.calamari.neural.network.INetwork;
import net.boogie.calamari.neural.network.NetworkUtils;

public class BackPropTrainerTest extends CalamariTestCase {

    private static final double LEARNING_RATE_BOOLEAN = 5.0;

    private static final long MAX_CYCLES = 100000;

    private static final double MAX_MUTATION_FACTOR = 0.20;

    private static Example[] EXAMPLES_XOR = new Example[] { buildExample(new double[] { 1, 1 }, new double[] { 0 }), buildExample(new double[] { 1, 0 }, new double[] { 1 }), buildExample(new double[] { 0, 1 }, new double[] { 1 }), buildExample(new double[] { 0, 0 }, new double[] { 0 }) };

    private static Example[] EXAMPLES_XNOR = new Example[] { buildExample(new double[] { 1, 1 }, new double[] { 1 }), buildExample(new double[] { 1, 0 }, new double[] { 0 }), buildExample(new double[] { 0, 1 }, new double[] { 0 }), buildExample(new double[] { 0, 0 }, new double[] { 1 }) };

    private static Example[] EXAMPLES_PALINDROME_3 = new Example[] { buildExample(new double[] { 1, 1, 1 }, new double[] { 1 }), buildExample(new double[] { 1, 1, 0 }, new double[] { 0 }), buildExample(new double[] { 1, 0, 1 }, new double[] { 1 }), buildExample(new double[] { 1, 0, 0 }, new double[] { 0 }), buildExample(new double[] { 0, 1, 1 }, new double[] { 0 }), buildExample(new double[] { 0, 1, 0 }, new double[] { 1 }), buildExample(new double[] { 0, 0, 1 }, new double[] { 0 }), buildExample(new double[] { 0, 0, 0 }, new double[] { 1 }) };

    private static Example[] EXAMPLES_PALINDROME_4 = new Example[] { buildExample(new double[] { 1, 1, 1, 1 }, new double[] { 1 }), buildExample(new double[] { 1, 1, 1, 0 }, new double[] { 0 }), buildExample(new double[] { 1, 1, 0, 1 }, new double[] { 0 }), buildExample(new double[] { 1, 1, 0, 0 }, new double[] { 0 }), buildExample(new double[] { 1, 0, 1, 1 }, new double[] { 0 }), buildExample(new double[] { 1, 0, 1, 0 }, new double[] { 0 }), buildExample(new double[] { 1, 0, 0, 1 }, new double[] { 1 }), buildExample(new double[] { 1, 0, 0, 0 }, new double[] { 0 }), buildExample(new double[] { 0, 1, 1, 1 }, new double[] { 0 }), buildExample(new double[] { 0, 1, 1, 0 }, new double[] { 1 }), buildExample(new double[] { 0, 1, 0, 1 }, new double[] { 0 }), buildExample(new double[] { 0, 1, 0, 0 }, new double[] { 0 }), buildExample(new double[] { 0, 0, 1, 1 }, new double[] { 0 }), buildExample(new double[] { 0, 0, 1, 0 }, new double[] { 0 }), buildExample(new double[] { 0, 0, 0, 1 }, new double[] { 0 }), buildExample(new double[] { 0, 0, 0, 0 }, new double[] { 1 }) };

    private static Example[] EXAMPLES_ADDITION_2_BIT = new Example[] { buildExample(new double[] { 0, 0, 0, 0 }, new double[] { 0, 0 }), buildExample(new double[] { 0, 0, 0, 1 }, new double[] { 0, 1 }), buildExample(new double[] { 0, 0, 1, 0 }, new double[] { 1, 0 }), buildExample(new double[] { 0, 0, 1, 1 }, new double[] { 1, 1 }), buildExample(new double[] { 0, 1, 0, 0 }, new double[] { 0, 1 }), buildExample(new double[] { 0, 1, 0, 1 }, new double[] { 1, 0 }), buildExample(new double[] { 0, 1, 1, 0 }, new double[] { 1, 1 }), buildExample(new double[] { 1, 0, 0, 0 }, new double[] { 1, 0 }), buildExample(new double[] { 1, 0, 0, 1 }, new double[] { 1, 1 }), buildExample(new double[] { 1, 1, 0, 0 }, new double[] { 1, 1 }) };

    private static Example[] EXAMPLES_ADDITION_3_BIT = new Example[] { buildExample(new double[] { 0, 0, 0, 0 }, new double[] { 0, 0, 0 }), buildExample(new double[] { 0, 0, 0, 1 }, new double[] { 0, 0, 1 }), buildExample(new double[] { 0, 0, 1, 0 }, new double[] { 0, 1, 0 }), buildExample(new double[] { 0, 0, 1, 1 }, new double[] { 0, 1, 1 }), buildExample(new double[] { 0, 1, 0, 0 }, new double[] { 0, 0, 1 }), buildExample(new double[] { 0, 1, 0, 1 }, new double[] { 0, 1, 0 }), buildExample(new double[] { 0, 1, 1, 0 }, new double[] { 0, 1, 1 }), buildExample(new double[] { 0, 1, 1, 1 }, new double[] { 1, 0, 0 }), buildExample(new double[] { 1, 0, 0, 0 }, new double[] { 0, 1, 0 }), buildExample(new double[] { 1, 0, 0, 1 }, new double[] { 0, 1, 1 }), buildExample(new double[] { 1, 0, 1, 0 }, new double[] { 1, 0, 0 }), buildExample(new double[] { 1, 0, 1, 1 }, new double[] { 1, 0, 1 }), buildExample(new double[] { 1, 1, 0, 0 }, new double[] { 0, 1, 1 }), buildExample(new double[] { 1, 1, 0, 1 }, new double[] { 1, 0, 0 }), buildExample(new double[] { 1, 1, 1, 0 }, new double[] { 1, 0, 1 }), buildExample(new double[] { 1, 1, 1, 1 }, new double[] { 1, 1, 0 }) };

    public void testXor() throws CalamariException {
        doTestBooleanExamples(EXAMPLES_XOR, EXAMPLES_XOR);
    }

    public void testXnor() throws CalamariException {
        doTestBooleanExamples(EXAMPLES_XNOR, EXAMPLES_XNOR);
    }

    public void testAddition2Bit() throws CalamariException {
        doTestDoubleExamples(EXAMPLES_ADDITION_2_BIT, EXAMPLES_ADDITION_2_BIT, 1, 4, 5.0, MAX_CYCLES);
    }

    public void DONT_testAddition3Bit1Layer() throws CalamariException {
        doTestDoubleExamples(EXAMPLES_ADDITION_3_BIT, EXAMPLES_ADDITION_3_BIT, 1, 8, 5.0, MAX_CYCLES);
    }

    public void DONT_testAddition3Bit2Layer() throws CalamariException {
        doTestDoubleExamples(EXAMPLES_ADDITION_3_BIT, EXAMPLES_ADDITION_3_BIT, 2, 4, 2.0, MAX_CYCLES);
    }

    public void DONT_testPalindrome3() throws CalamariException {
        doTestBooleanExamples(EXAMPLES_PALINDROME_3, EXAMPLES_PALINDROME_3);
    }

    public void DONT_testPalindrome4() throws CalamariException {
        doTestBooleanExamples(EXAMPLES_PALINDROME_4, EXAMPLES_PALINDROME_4);
    }

    protected void doTestDoubleExamples(Example[] examplesTrain, Example[] examplesTest, int hiddenLayerCount, int hiddenLayerNodeCount, double learningRate, long maxCycles) throws CalamariException {
        int inputCount = examplesTrain[0].getInputNodes().size();
        int outputCount = examplesTrain[0].getOutputNodes().size();
        FeedForwardNetwork net = buildFeedForwardNetwork(new SigmoidFunction(), null, new boolean[inputCount], hiddenLayerCount, hiddenLayerNodeCount, outputCount);
        ITrainer trainer = new BackPropTrainer(learningRate);
        trainer.train(net, Arrays.asList(examplesTrain), maxCycles);
        for (int i = 0; i < examplesTest.length; i++) {
            Example example = examplesTest[i];
            doDoubleTest(net, example);
            Example mutatedCopy = mutateInputs(example, MAX_MUTATION_FACTOR);
            doDoubleTest(net, mutatedCopy);
        }
    }

    protected void doTestBooleanExamples(Example[] examplesTrain, Example[] examplesTest) throws CalamariException {
        int inputCount = examplesTrain[0].getInputNodes().size();
        int outputCount = examplesTrain[0].getOutputNodes().size();
        double hiddenLayerNodeCount = Math.pow(2, inputCount) / inputCount;
        int hiddenLayerNodeCountInt = (int) Math.round(hiddenLayerNodeCount);
        FeedForwardNetwork net = buildFeedForwardNetwork(new SigmoidFunction(), null, new boolean[inputCount], 1, hiddenLayerNodeCountInt, outputCount);
        ITrainer trainer = new BackPropTrainer(LEARNING_RATE_BOOLEAN);
        trainer.train(net, Arrays.asList(examplesTrain), MAX_CYCLES);
        for (int i = 0; i < examplesTest.length; i++) {
            Example example = examplesTest[i];
            doBooleanTest(net, example);
            Example mutatedCopy = mutateInputs(example, MAX_MUTATION_FACTOR);
            doBooleanTest(net, mutatedCopy);
        }
    }

    protected void doBooleanTest(INetwork net, Example example) throws CalamariException {
        net.setInputLevels(example);
        net.compute();
        StringBuffer buf = new StringBuffer();
        buf.append(ArrayUtils.toString(NetworkUtils.getLevels(net.getInputNodes()), ", ", "<null>"));
        buf.append(" --> [");
        buf.append(ArrayUtils.toString(NetworkUtils.getLevels(example.getOutputNodes()), ", ", "<null>"));
        buf.append("]\t");
        buf.append(ArrayUtils.toString(NetworkUtils.getLevels(net.getOutputNodes()), ", ", "<null>"));
        buf.append("\t");
        System.out.println(buf.toString());
        boolean output = NetworkUtils.extractBooleanOutput(net);
        Set<INode> exampleOutputNodes = example.getOutputNodes();
        Double[] exampleLevels = NetworkUtils.getLevels(exampleOutputNodes);
        double exampleLevel = exampleLevels[0];
        boolean expected = NetworkUtils.toBoolean(exampleLevel);
        assertEquals(output, expected);
    }

    protected void doDoubleTest(INetwork net, Example example) throws CalamariException {
        net.setInputLevels(example);
        net.compute();
        StringBuffer buf = new StringBuffer();
        buf.append(ArrayUtils.toString(NetworkUtils.getLevels(net.getInputNodes()), ", ", "<null>"));
        buf.append(" --> [");
        buf.append(ArrayUtils.toString(NetworkUtils.getLevels(example.getOutputNodes()), ", ", "<null>"));
        buf.append("]\t");
        buf.append(ArrayUtils.toString(NetworkUtils.getLevels(net.getOutputNodes()), ", ", "<null>"));
        buf.append("\t");
        System.out.println(buf.toString());
        double output = NetworkUtils.extractDoubleOutput(net);
        Set<INode> exampleOutputNodes = example.getOutputNodes();
        Double[] exampleLevels = NetworkUtils.getLevels(exampleOutputNodes);
        double exampleLevel = exampleLevels[0];
        assertEquals(Math.round(output), Math.round(exampleLevel));
    }
}
