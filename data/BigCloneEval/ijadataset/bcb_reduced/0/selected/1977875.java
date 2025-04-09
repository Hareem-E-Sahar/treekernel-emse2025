package algorithms;

import org.joone.helpers.factory.JooneTools;
import org.joone.io.MemoryInputSynapse;
import org.joone.net.NeuralNet;
import org.joone.util.NormalizerPlugIn;
import auxillary.products.Config;
import auxillary.products.Product;
import auxillary.products.ProductFactory.Exc;
import experiment.Algorithm;
import experiment.Dataset;
import experiment.LearnResult;
import experiment.Result;

/**
 * Sie� neuronowa.
 * 
 * @author Mateusz Markowicz
 * @author Marta Sta�ska
 */
public class Neural implements Product, Algorithm {

    private int hidden;

    private int[] neuronsCount;

    private int maxEpochs;

    private NeuralNet nnet;

    public Neural(Config c) throws NumberFormatException, Exc {
        this.hidden = Integer.parseInt(c.getString("hidden"));
        this.neuronsCount = new int[this.hidden + 2];
        for (int i = 1; i <= hidden; i++) this.neuronsCount[i] = Integer.parseInt(c.getString("hidden" + i));
        this.maxEpochs = Integer.parseInt(c.getString("maxEpochs"));
    }

    public Result classify(double[] ind) {
        return new Result(JooneTools.interrogate(nnet, ind));
    }

    public LearnResult learn(Dataset learn) {
        int n = learn.getN();
        int d = learn.getD();
        try {
            neuronsCount[0] = d;
            neuronsCount[hidden + 1] = learn.getClassCount();
            nnet = JooneTools.create_standard(neuronsCount, JooneTools.LOGISTIC);
            nnet.getMonitor().setSingleThreadMode(false);
            NormalizerPlugIn norm = new NormalizerPlugIn();
            norm.setAdvancedSerieSelector("1-" + d);
            MemoryInputSynapse inds = new MemoryInputSynapse();
            inds.setInputArray(indArray(learn));
            inds.setAdvancedColumnSelector("1-" + d);
            inds.addPlugIn(norm);
            JooneTools.train(nnet, JooneTools.getDataFromStream(inds, 1, n, 1, d), classesArray(learn), maxEpochs, 0.001, 20, System.out, false);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return null;
    }

    private double[][] classesArray(Dataset learn) {
        double[][] ret = new double[learn.getN()][learn.getClassCount()];
        double[][] columns = learn.getColumns();
        for (int i = 0; i < learn.getN(); i++) for (int c = 0; c < learn.getClassCount(); c++) ret[i][c] = columns[c][i];
        return ret;
    }

    private double[][] indArray(Dataset learn) {
        double[][] ret = new double[learn.getN()][];
        for (int i = 0; i < learn.getN(); i++) ret[i] = learn.getInd(i);
        return ret;
    }
}
