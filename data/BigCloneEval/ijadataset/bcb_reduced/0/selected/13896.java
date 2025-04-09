package com.rapidminer.operator.learner.meta;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;

/**
 * A model for the Bayesian Boosting algorithm by Martin Scholz.
 * 
 * @author Martin Scholz
 */
public class BayBoostModel extends PredictionModel implements MetaModel {

    private static final long serialVersionUID = 5821921049035718838L;

    private final List<BayBoostBaseModelInfo> modelInfo;

    private final double[] priors;

    private int maxModelNumber = -1;

    private static final String MAX_MODEL_NUMBER = "iteration";

    private static final String CONV_TO_CRISP = "crisp";

    private double threshold = 0.5;

    /**
	 * @param exampleSet
	 *            the example set used for training
	 * @param modelInfos
	 *            a <code>List</code> of <code>Object[2]</code> arrays, each
	 *            entry holding a model and a <code>double[][]</code> array
	 *            containing weights for all prediction/label combinations.
	 * @param priors
	 *            an array of the prior probabilities of labels
	 */
    public BayBoostModel(ExampleSet exampleSet, List<BayBoostBaseModelInfo> modelInfos, double[] priors) {
        super(exampleSet);
        this.modelInfo = modelInfos;
        this.priors = priors;
    }

    public BayBoostBaseModelInfo getBayBoostBaseModelInfo(int index) {
        return this.modelInfo.get(index);
    }

    /**
	 * Setting the parameter <code>MAX_MODEL_NUMBER</code> allows to discard
	 * all but the first n models for specified n. <code>CONV_TO_CRISP</code>
	 * allows to set another threshold than 0.5 for boolean prediction problems.
	 */
    public void setParameter(String name, String value) throws OperatorException {
        if (name.equalsIgnoreCase(MAX_MODEL_NUMBER)) {
            try {
                this.maxModelNumber = Integer.parseInt(value);
                return;
            } catch (NumberFormatException e) {
            }
        } else if (name.equalsIgnoreCase(CONV_TO_CRISP)) {
            this.threshold = Double.parseDouble(value.trim());
            return;
        }
        super.setParameter(name, value);
    }

    /**
	 * Using this setter with a positive value makes the model discard all
	 * but the specified number of base models. A value of -1 turns off this
	 * option.
	 */
    public void setMaxModelNumber(int numModels) {
        this.maxModelNumber = numModels;
    }

    /** @return a <code>String</code> representation of this boosting model. */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(super.toString() + Tools.getLineSeparator() + "Number of inner models: " + this.getNumberOfModels() + Tools.getLineSeparators(2));
        for (int i = 0; i < this.getNumberOfModels(); i++) {
            Model model = this.getModel(i);
            result.append((i > 0 ? Tools.getLineSeparator() : "") + "Embedded model #" + i + ":" + Tools.getLineSeparator() + model.toResultString());
        }
        return result.toString();
    }

    /** @return the number of embedded models */
    public int getNumberOfModels() {
        if (this.maxModelNumber >= 0) return Math.min(this.maxModelNumber, modelInfo.size()); else return modelInfo.size();
    }

    /**
	 * Gets factors for models in the case of general nominal class labels.
	 * 
	 * @return a <code>double[]</code> object with the factors to be applied
	 *         for each class if the corresponding rule yields
	 *         <code>predicted</code>.
	 * @param modelNr
	 *            the number of the model
	 * @param predicted
	 *            the predicted label
	 * @return a <code>double[]</code> with one factor per class label,
	 *         <code>Double.POSITIVE_INFINITY</code> if the rule
	 *         deterministically predicts a value, and
	 *         <code>RULE_DOES_NOT_APPLY</code> if no prediction can be made.
	 */
    private double[] getFactorsForModel(int modelNr, int predicted) {
        ContingencyMatrix cm = this.modelInfo.get(modelNr).getContingencyMatrix();
        return cm.getLiftRatiosForPrediction(predicted);
    }

    /**
	 * Getter method for prior class probabilities estimated as the relative
	 * frequencies in the training set.
	 * 
	 * @param classIndex
	 *            the index of a class, starting with 0
	 * @return the prior probability of the specified class
	 */
    private double getPriorOfClass(int classIndex) {
        return this.priors[classIndex];
    }

    /** Getter for the prior array */
    public double[] getPriors() {
        double[] result = new double[this.priors.length];
        System.arraycopy(this.priors, 0, result, 0, result.length);
        return result;
    }

    /**
	 * Getter method for embedded models
	 * 
	 * @param index
	 *            the number of a model part of this boost model
	 * @return binary or nominal decision model for the given classification
	 *         index.
	 */
    public Model getModel(int index) {
        return this.modelInfo.get(index).getModel();
    }

    /**
	 * Getter method for a specific confusion matrix
	 * 
	 * @param index
	 *            the number of the model for which to read the confusion matrix
	 * @return a <code>ConfusionMatrix</code> object
	 */
    public ContingencyMatrix getContingencyMatrix(int index) {
        return this.modelInfo.get(index).getContingencyMatrix();
    }

    /**
	 * Iterates over all models and returns the class with maximum likelihood.
	 * 
	 * @param exampleSet
	 *            the set of examples to be classified
	 * @param predictedLabel
	 *            the label that finally holds the predictions
	 */
    @Override
    public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
        final Attribute[] specialAttributes = this.createSpecialAttributes(exampleSet);
        this.initIntermediateResultAttributes(exampleSet, specialAttributes);
        for (int i = 0; i < this.getNumberOfModels(); i++) {
            Model model = this.getModel(i);
            ExampleSet clonedExampleSet = (ExampleSet) exampleSet.clone();
            clonedExampleSet = model.apply(clonedExampleSet);
            this.updateEstimates(clonedExampleSet, this.getContingencyMatrix(i), specialAttributes);
            PredictionModel.removePredictedLabel(clonedExampleSet);
        }
        Iterator<Example> reader = exampleSet.iterator();
        while (reader.hasNext()) {
            Example example = reader.next();
            this.translateOddsIntoPredictions(example, specialAttributes, getTrainingHeader().getAttributes().getLabel());
        }
        this.cleanUpSpecialAttributes(exampleSet, specialAttributes);
        return exampleSet;
    }

    /** Creates a special attribute for each label to store intermediate results. */
    private Attribute[] createSpecialAttributes(ExampleSet exampleSet) throws OperatorException {
        final String attributePrefix = "BayBoostModelPrediction";
        Attribute[] specialAttributes = new Attribute[this.getLabel().getMapping().size()];
        for (int i = 0; i < specialAttributes.length; i++) {
            specialAttributes[i] = com.rapidminer.example.Tools.createSpecialAttribute(exampleSet, attributePrefix + i, Ontology.NUMERICAL);
        }
        return specialAttributes;
    }

    /** Removes the provided special labels from the exampleSet and exampleTable. */
    private void cleanUpSpecialAttributes(ExampleSet exampleSet, Attribute[] specialAttributes) throws OperatorException {
        for (int i = 0; i < specialAttributes.length; i++) {
            exampleSet.getAttributes().remove(specialAttributes[i]);
            exampleSet.getExampleTable().removeAttribute(specialAttributes[i]);
        }
    }

    private void initIntermediateResultAttributes(ExampleSet exampleSet, Attribute[] specAttrib) {
        double[] priorOdds = new double[this.priors.length];
        for (int i = 0; i < priorOdds.length; i++) {
            priorOdds[i] = (this.priors[i] == 1) ? Double.POSITIVE_INFINITY : (this.priors[i] / (1 - this.priors[i]));
        }
        Iterator<Example> reader = exampleSet.iterator();
        while (reader.hasNext()) {
            Example example = reader.next();
            for (int i = 0; i < specAttrib.length; i++) {
                example.setValue(specAttrib[i], priorOdds[i]);
            }
        }
    }

    private void translateOddsIntoPredictions(Example example, Attribute[] specAttrib, Attribute trainingSetLabel) {
        double probSum = 0;
        double[] classProb = new double[specAttrib.length];
        int bestIndex = 0;
        for (int n = 0; n < classProb.length; n++) {
            double odds = example.getValue(specAttrib[n]);
            if (Double.isNaN(odds)) {
                logWarning("Found NaN odd ratio estimate.");
                classProb[n] = 1;
            } else classProb[n] = (Double.isInfinite(odds)) ? 1 : (odds / (1 + odds));
            probSum += classProb[n];
            if (classProb[n] > classProb[bestIndex]) {
                bestIndex = n;
            }
        }
        if (probSum != 1.0) {
            for (int k = 0; k < classProb.length; k++) {
                classProb[k] /= probSum;
            }
        }
        final String bestLabel;
        if (this.getLabel().isNominal() && this.getLabel().getMapping().size() == 2 && this.threshold != 0.5) {
            int posIndex = this.getLabel().getMapping().getPositiveIndex();
            int negIndex = this.getLabel().getMapping().getNegativeIndex();
            threshold = (this.threshold >= 0 && this.threshold <= 1) ? this.threshold : 0.5;
            bestLabel = this.getLabel().getMapping().mapIndex((classProb[posIndex] >= threshold) ? posIndex : negIndex);
        } else {
            bestLabel = this.getLabel().getMapping().mapIndex(bestIndex);
        }
        example.setValue(example.getAttributes().getPredictedLabel(), trainingSetLabel.getMapping().mapString(bestLabel));
        for (int k = 0; k < classProb.length; k++) {
            if (Double.isNaN(classProb[k]) || classProb[k] < 0 || classProb[k] > 1) {
                logWarning("Found illegal confidence value: " + classProb[k]);
            }
            example.setConfidence(this.getLabel().getMapping().mapIndex(k), classProb[k]);
        }
    }

    private void updateEstimates(ExampleSet exampleSet, ContingencyMatrix cm, Attribute[] specialAttributes) {
        Iterator<Example> reader = exampleSet.iterator();
        while (reader.hasNext()) {
            Example example = reader.next();
            int predicted = (int) example.getPredictedLabel();
            L: for (int j = 0; j < cm.getNumberOfClasses(); j++) {
                final double liftRatioCurrent = cm.getLiftRatio(j, predicted);
                if (Double.isNaN(liftRatioCurrent)) {
                    logWarning("Ignoring non-applicable model.");
                    continue L;
                } else if (Double.isInfinite(liftRatioCurrent)) {
                    if (example.getValue(specialAttributes[j]) != 0) {
                        for (int k = 0; k < specialAttributes.length; k++) {
                            example.setValue(specialAttributes[k], 0);
                        }
                        example.setValue(specialAttributes[j], liftRatioCurrent);
                    } else continue L;
                } else {
                    double oldValue = example.getValue(specialAttributes[j]);
                    if (Double.isNaN(oldValue)) {
                        logWarning("Found NaN value in intermediate odds ratio estimates!");
                    }
                    if (!Double.isInfinite(oldValue)) {
                        example.setValue(specialAttributes[j], oldValue * liftRatioCurrent);
                    }
                }
            }
        }
    }

    /**
	 * Helper method to adjust the intermediate products during model
	 * application.
	 * 
	 * @param products
	 *            the intermediate products, these values are changed by the
	 *            method
	 * @param liftFactors
	 *            the factor vector that applies for the prediction for the
	 *            current example
	 * 
	 * @return <code>true</code> iff the class is deterministically known
	 *         after applying this method
	 */
    public static boolean adjustIntermediateProducts(double[] products, double[] liftFactors) {
        L: for (int i = 0; i < liftFactors.length; i++) {
            if (Double.isNaN(liftFactors[i])) {
                LogService.getGlobal().log("Ignoring non-applicable model.", LogService.WARNING);
                continue L;
            } else if (Double.isInfinite(liftFactors[i])) {
                if (products[i] != 0) {
                    for (int j = 0; j < products.length; j++) {
                        products[j] = 0;
                    }
                    products[i] = liftFactors[i];
                    return true;
                } else continue L;
            } else {
                products[i] *= liftFactors[i];
                if (Double.isNaN(products[i])) {
                    LogService.getGlobal().log("Found NaN value in intermediate odds ratio estimates!", LogService.WARNING);
                }
            }
        }
        return false;
    }

    /**
	 * This method is only supported for boolean target attributes. It computes
	 * a flattened version of model weights. In constrast to the original
	 * version the final predictions are additive logarithms of the lift ratios,
	 * additively rescaled so that the prediction <code>false</code> of model
	 * i produces <code>-i</code> if <code>true</code> produces weight i.
	 * This means that only one weight per model is required. The first
	 * component of the returned array is the part that is independent of any
	 * prediction, the i-th component is the weight of model i. The (log-)linear
	 * model predicts depending on whether the linear combination of predictions
	 * (either -1 or 1) is greater than 0 or not. Infinite values are
	 * problematic, so a min/max value is used.
	 * 
	 * @return the flattened weights of all models
	 */
    public double[] getModelWeights() throws OperatorException {
        if (this.getLabel().getMapping().size() != 2) throw new UserError(null, 114, "BayBoostModel", this.getLabel());
        int maxWeight = 10;
        final int pos = this.getLabel().getMapping().getPositiveIndex();
        final int neg = this.getLabel().getMapping().getNegativeIndex();
        double[] weights = new double[this.getNumberOfModels() + 1];
        double odds = this.getPriorOfClass(pos) / this.getPriorOfClass(neg);
        weights[0] = Math.log(odds);
        for (int i = 1; i < weights.length; i++) {
            double logPosRatio, logNegRatio;
            {
                double liftRatiosPos[] = this.getFactorsForModel(i - 1, pos);
                logPosRatio = Math.log(liftRatiosPos[pos]);
                logPosRatio = Math.min(maxWeight, Math.max(-maxWeight, logPosRatio));
                double liftRatiosNeg[] = this.getFactorsForModel(i - 1, neg);
                logNegRatio = Math.log(liftRatiosNeg[pos]);
                logNegRatio = Math.min(maxWeight, Math.max(-maxWeight, logNegRatio));
            }
            double indep = (logPosRatio + logNegRatio) / 2;
            if (Tools.isEqual(indep, maxWeight) || Tools.isEqual(indep, -maxWeight)) {
                logPosRatio = 10 * indep;
                indep = 0;
            }
            weights[0] += indep;
            logPosRatio -= indep;
            weights[i] = logPosRatio;
        }
        return weights;
    }

    @Override
    public List<String> getModelNames() {
        List<String> names = new LinkedList<String>();
        for (int i = 0; i < this.getNumberOfModels(); i++) {
            names.add("Model " + (i + 1));
        }
        return names;
    }

    @Override
    public List<Model> getModels() {
        List<Model> models = new LinkedList<Model>();
        for (int i = 0; i < this.getNumberOfModels(); i++) {
            models.add(getModel(i));
        }
        return models;
    }
}
