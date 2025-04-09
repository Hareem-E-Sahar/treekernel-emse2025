package iclab.classification.supervised.multidimensional;

import java.util.ArrayList;
import iclab.classification.ICMDClassifier;
import iclab.core.ICAttribute;
import iclab.core.ICData;
import iclab.core.ICInstance;
import iclab.distributions.ICBNet;
import iclab.estimation.bnetparams.ICBNetParamLearner;
import iclab.estimation.dag.ICDAGStructureLearner;
import iclab.exceptions.ICInvalidMethodException;
import iclab.exceptions.ICParameterException;
import iclab.utils.ICDataUtils;

/** Object to create MD Bayesian network classifiers
 */
public class ICMDBNC extends ICBNet implements ICMDClassifier {

    /** Enumeration of the classification rules applicable in multidimensional class classification
 * joint - Joint classification rule: The classifier returns the most probable
 * combination of class variables given the features. p(c_1,---,c_m|x_1,..,x_n)
 * </br>
 * marginal- Marginal classification rule: The classifier marginalizes each class
 * variable for the rest of class variables simultaneously. It estimates the probability
 * of each class variable C_i with i=1,...,m as follows:</br>
 * p(c_i|x_1,....x_n)=\sum_{j=1,...,m & j!=i} p(c_i,c_j|x_1,...,x_m)
 * </br>
 * conditional - Conditional classification rule:  In a first step, each class variable
 * is estimated using the marginal classification rule previously defined, and then,
 * each class variable value is estimated again using the class values for the rest of
 * the class variables estimated on the first step as evidences
 * </br>
 * iterative - Iterative classification rule:  It extends the previous classification
 * rule and continue estimating the class values taking into account the estimated
 * values for the rest of the classes in the previous step. This procedure should continue
 * until a stop criterion is achieved, for example that the estimated class values do not
 * change in two consecutive steps
 * </br>
 *  Further information in Learning Bayesian network classifiers for multi-dimensional prediction
 * problems by means of a multi-objective approach. Juan D. Rodr&iacute;guez & Jose A. Lozano.
 */
    public enum ICMDClassificationRule {

        joint, marginal, conditional, iterative
    }

    ;

    private ICMDClassificationRule _classificationRule = ICMDClassificationRule.joint;

    private int[] _classIndexes;

    /**
	 * Basic constructor. Basic constructor (the default parametric learning is the estimation from data using Laplace correction and the classification is based on the joint probability distribution)
	 * @param strLearner - Structural learning algorithm. This has to be an object that learns proper MD BNet classifiers (the class variables cannot have parents that are not class variables) 
	 */
    public ICMDBNC(ICDAGStructureLearner strLearner) {
        super(strLearner);
    }

    /**
	 * In this constructor we can set the type of structure to learn and the type of parameter estimation. The classification is based on the joint probability distribution 
	 * @param strLearner - Structural learning algorithm. This has to be an object that learns proper MD BNet classifiers (the class variables cannot have parents that are not class variables)
	 * @param parLearner - Object implementing the procedure to obtain the parameters of the model 
	 */
    public ICMDBNC(ICDAGStructureLearner strLearner, ICBNetParamLearner parLearner) {
        super(strLearner, parLearner);
    }

    /**
	 * Complete constructor. We can set the type of structure to learn, the type of parameter estimation and the classification rule to be used.
	 * @param strLearner - Structural learning algorithm. This has to be an object that learns proper MD BNet classifiers (the class variables cannot have parents that are not class variables)
	 * @param parLearner - Object implementing the procedure to obtain the parameters of the model
	 * @param cRule - Classification rule to be used 
	 */
    public ICMDBNC(ICDAGStructureLearner strLearner, ICBNetParamLearner parLearner, ICMDClassificationRule cRule) {
        super(strLearner, parLearner);
        _classificationRule = cRule;
    }

    /**
   * Method to classify a given instance according to the default classification rule
   * @param instance Instance to classify
   * @return List of class values for the class variables
   * @throws ICParameterException
   */
    @Override
    public double[] classify(ICInstance instance) throws ICParameterException {
        return classify(instance, _classificationRule);
    }

    /**
   * Method to classify an instance with the desired classification rule
   * @param instance Instance to classify
   * @param cRule Classification rule to use
   * @return List of class values for the class variables
   * @throws ICParameterException
   */
    public double[] classify(ICInstance instance, ICMDClassificationRule cRule) throws ICParameterException {
        double[] classDistribution;
        switch(cRule) {
            case joint:
                classDistribution = this.jointClassDistribution(instance);
                break;
            default:
                classDistribution = this.jointClassDistribution(instance);
        }
        int index = 0;
        for (int c = 1; c < classDistribution.length; c++) if (classDistribution[c] > classDistribution[index]) index = c;
        return ICDataUtils.decomposeJointIndex(_classIndexes, instance.getAttList(), index);
    }

    /**
   * Method to classify a whole data set (with the default classification rule)
   * @param dataset Data set to classify
   * @throws ICParameterException
   */
    @Override
    public void classify(ICData dataset) throws ICParameterException {
        classify(dataset, _classificationRule);
    }

    /**
   *  Method to classify a whole data set with a particular classification rule
   * @param dataset Data set to classify
   * @param cRule Classification rule to use
   * @throws ICParameterException
   */
    public void classify(ICData dataset, ICMDClassificationRule cRule) throws ICParameterException {
        int numInstances = dataset.numInstances();
        double[] classValues;
        ICInstance currentInstance;
        for (int i = 0; i < numInstances; i++) {
            currentInstance = dataset.instance(i);
            classValues = classify(currentInstance);
            for (int v = 0; v < classValues.length; v++) currentInstance.setAttValue(_classIndexes[v], classValues[v]);
        }
    }

    /**
	 * Method to obtain the joint conditional probability distribution of the class attributes (given the instance values)
	 * @param instance - Instance to compute the joint class distribution
	 * @return Distribution of the class attributes. The indexes in the array correspond to the composition of 
	 * @throws ICParameterException
	 */
    @Override
    public double[] jointClassDistribution(ICInstance instance) throws ICParameterException {
        int numClasses = _classIndexes.length;
        int cardJointClass = 1;
        ArrayList<ICAttribute> attributes = instance.getAttList();
        for (int c = 0; c < numClasses; c++) cardJointClass *= attributes.get(_classIndexes[c]).getAttCardinality();
        double[] jointClassDistribution = new double[cardJointClass];
        double minLogProb = 0;
        double maxLogProb = 0;
        for (int cv = 0; cv < cardJointClass; cv++) {
            double[] values = ICDataUtils.decomposeJointIndex(_classIndexes, instance.getAttList(), cv);
            for (int c = 0; c < numClasses; c++) instance.setAttValue(_classIndexes[c], values[c]);
            jointClassDistribution[cv] = getLogProbability(instance);
            if (cv == 0 || jointClassDistribution[cv] > maxLogProb) maxLogProb = jointClassDistribution[cv];
            if (cv == 0 || jointClassDistribution[cv] < minLogProb) minLogProb = jointClassDistribution[cv];
        }
        double cte = (maxLogProb + minLogProb) / 2;
        double sum = 0;
        for (int cv = 0; cv < jointClassDistribution.length; cv++) {
            jointClassDistribution[cv] += cte;
            jointClassDistribution[cv] = Math.pow(10, jointClassDistribution[cv]);
            sum += jointClassDistribution[cv];
        }
        for (int cv = 0; cv < jointClassDistribution.length; cv++) jointClassDistribution[cv] /= sum;
        return jointClassDistribution;
    }

    /**
	 * Method where the actual model is created
	 * @param dataset - Data set from where the BNC is learned
	 * @throws ICParameterException
	 */
    @Override
    public void learn(ICData dataset) throws ICParameterException {
        ArrayList<Integer> classes = dataset.getClassIndexes();
        int numClasses = classes.size();
        _classIndexes = new int[numClasses];
        for (int c = 0; c < numClasses; c++) _classIndexes[c] = classes.get(c);
        if (_classIndexes == null || _classIndexes.length < 2) throw new ICParameterException("The dataset for MD classifiers has to have at least 2 class variables");
        estimateFromData(dataset);
        boolean strCorrect = true;
        int c = 0;
        while (strCorrect && c < numClasses) {
            int[] parents = _parent[_classIndexes[c]];
            int p = 0;
            while (strCorrect && p < parents.length) {
                if (!classes.contains(parents[p])) strCorrect = false;
                p++;
            }
            c++;
        }
        if (!strCorrect) throw new ICParameterException("The structure learned does not fit with a MD Bayesian network classifier");
    }
}
