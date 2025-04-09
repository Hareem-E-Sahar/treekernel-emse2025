package iclab.classification.supervised;

import java.util.ArrayList;
import iclab.classification.ICClassifier;
import iclab.core.ICData;
import iclab.core.ICInstance;
import iclab.distributions.ICBNet;
import iclab.estimation.bnetparams.ICBNetParamLearner;
import iclab.estimation.dag.ICDAGStructureLearner;
import iclab.exceptions.ICParameterException;

/**
 * Object for constructing Bayesian Network Classifiers
 */
public class ICBNC extends ICBNet implements ICClassifier {

    int _classIndex;

    /**
	 * Basic constructor. Basic constructor (the default parametric learning is the estimation from data using laplace correction)
	 * @param strLearner - Structural learning algorithm. This has to be an object that learns proper BNet classifiers (the class has to be parent of all attributes) 
	 */
    public ICBNC(ICDAGStructureLearner strLearner) {
        super(strLearner);
    }

    /**
	 * Complete constructor. We can set the type of structure to learn and the type of parameter estimation 
	 * @param strLearner - Structural learning algorithm. This has to be an object that learns proper BNet classifiers (the class has to be parent of all attributes)
	 * @param parLearner - Object implementing the procedure to obtain the parameters of the model 
	 */
    public ICBNC(ICDAGStructureLearner strLearner, ICBNetParamLearner parLearner) {
        super(strLearner, parLearner);
    }

    /**
	 * Method where the actual model is created
	 * @param dataset - Dataset from where the BNC is learnt
	 * @throws ICParameterException
	 */
    @Override
    public void learn(ICData dataset) throws ICParameterException {
        ArrayList<Integer> classIndex = dataset.getClassIndexes();
        if (classIndex == null) throw new ICParameterException("The dataset from where the BNC has to be learnt does not have any attribute " + "							defined as the class");
        if (classIndex.size() != 1) throw new ICParameterException("BNC that you are trying to learn only accepts unidimensional class " + "							attributes but more than one attribute is defined as class in the dataset");
        _classIndex = classIndex.get(0);
        estimateFromData(dataset);
        boolean correctStr = true;
        for (int a = 0; a < _parent.length; a++) {
            if (a == _classIndex) {
                if (_parent[a] != null) correctStr = false;
            } else {
                if (_parent[a] == null) {
                    correctStr = false;
                } else {
                    int p = 0;
                    boolean stop = false;
                    while (!stop && p < _parent[a].length) {
                        if (_parent[a][p] == _classIndex) stop = true;
                        p++;
                    }
                    if (!stop) correctStr = false;
                }
            }
        }
        if (!correctStr) throw new ICParameterException("The structure learner does not return a Bayesian network classifier");
    }

    /**
	 * Method to obtain the conditional probability distribution of the class (given the instance values)
	 * @param instance - Instance to be classified
	 * @return Distribution of the class attribute
	 * @throws ICParameterException
	 */
    @Override
    public double[] classDistribution(ICInstance instance) throws ICParameterException {
        double[] classDistrib = new double[_attList.get(_classIndex).getAttCardinality()];
        double minLogProb = 0;
        double maxLogProb = 0;
        int classIndex = instance.getClassIndexes().get(0);
        for (int c = 0; c < classDistrib.length; c++) {
            ICInstance i = new ICInstance(instance);
            i.setAttValue(classIndex, c);
            classDistrib[c] = getLogProbability(i);
            if (c == 0 || classDistrib[c] > maxLogProb) maxLogProb = classDistrib[c];
            if (c == 0 || classDistrib[c] < minLogProb) minLogProb = classDistrib[c];
        }
        double cte = (maxLogProb + minLogProb) / 2;
        double sum = 0;
        for (int i = 0; i < classDistrib.length; i++) {
            classDistrib[i] -= cte;
            classDistrib[i] = Math.pow(10, classDistrib[i]);
            sum += classDistrib[i];
        }
        for (int i = 0; i < classDistrib.length; i++) classDistrib[i] /= sum;
        return classDistrib;
    }

    /**
	 * Method to classify a particular instance using the maximum a posteriori probability
	 * @param instance - instance to be classified
	 * @return class value
	 * @throws ICParameterException
	 */
    @Override
    public double classify(ICInstance instance) throws ICParameterException {
        int classValue = 0;
        double[] distribution = this.classDistribution(instance);
        for (int i = 0; i < distribution.length; i++) if (distribution[i] > distribution[classValue]) classValue = i;
        return classValue;
    }

    /**
	 * Method to classify a whole dataset (see classify (ICInstance))
	 * @param dataset - Dataset to be classified
	 * @throws ICParameterException
	 */
    @Override
    public void classify(ICData dataset) throws ICParameterException {
        for (int i = 0; i < dataset.numInstances(); i++) dataset.instance(i).setAttValue(_classIndex, this.classify(dataset.instance(i)));
    }

    /**
   * Method to obatin the class index of the classifier
   * @return
   */
    public int getClassIndex() {
        return _classIndex;
    }
}
