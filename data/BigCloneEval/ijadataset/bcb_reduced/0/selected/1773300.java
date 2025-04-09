package de.fzi.mapevo.algorithm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import de.fzi.harmonia.commons.Evaluator;
import de.fzi.harmonia.commons.InfeasibleEvaluatorException;
import de.fzi.kadmos.api.Alignment;
import de.fzi.kadmos.api.Correspondence;
import de.fzi.kadmos.api.IncompatibleOntologyException;
import de.fzi.kadmos.api.MultiplicityException;
import de.fzi.mapevo.data.ClassesPermutation;
import de.fzi.mapevo.data.CorrespondencePermutation;
import de.fzi.mapevo.data.DataPropertiesPermutation;
import de.fzi.mapevo.data.ObjectPropertiesPermutation;
import de.fzi.mapevo.data.PermutData;
import de.fzi.mapevo.operators.MutationOperator;

public class Species extends Thread {

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(Species.class);

    /**
     * Configuration parameter key to specify
     * the correspondence evaluator (that delivers an aggregated evaluation for a correspondence).
     */
    public static final String CORRESPONDENCE_EVALUATOR_PARAMETER_KEY = "correspondenceEvaluator";

    /**
     * Configuration parameter key to specify
     * the global alignment evaluator.
     */
    public static final String GLOBAL_ALIGNMENT_EVALUATOR_PARAMETER_KEY = "globalAlignmentEvaluator";

    protected final Alignment alignment;

    protected PermutData data;

    private ClassesPermutation classesPermutation;

    private ClassesPermutation localBestClasses;

    private DataPropertiesPermutation dataPropertiesPermutation;

    private DataPropertiesPermutation localBestDataProperties;

    private ObjectPropertiesPermutation objectPropertiesPermutation;

    private ObjectPropertiesPermutation localBestObjectProperties;

    private MutationOperator operator;

    private Evaluator corrEvaluator;

    private Evaluator alignmentEvaluator;

    private final int maxIterations;

    private int curIter;

    private final int curMaxIter;

    private final int curRound;

    protected double bestFitness;

    public Species(Alignment alignment, int curRound, int maxRounds, int maxIterations) throws MapEVOConfigurationException {
        if (logger.isInfoEnabled()) logger.info("Init Species in " + this.getName());
        this.data = new PermutData(alignment);
        this.alignment = alignment.clone();
        if (maxRounds == 0) maxRounds = 1;
        this.maxIterations = maxIterations;
        this.curIter = curRound * (maxIterations / maxRounds);
        this.curMaxIter = (curRound + 1) * (maxIterations / maxRounds);
        this.curRound = curRound;
        this.bestFitness = 0;
        readParams();
        init();
    }

    public void init() throws MapEVOConfigurationException {
        this.alignment.clear();
        this.operator = new MutationOperator(data);
        this.classesPermutation = data.getRefForClasses();
        this.dataPropertiesPermutation = data.getRefForData();
        this.objectPropertiesPermutation = data.getRefForObject();
    }

    public void run() {
        while (curIter < curMaxIter) {
            if (logger.isInfoEnabled()) logger.info("Iteration " + curIter + ": Best fitness: " + bestFitness);
            logger.debug("Mutate: swap");
            List<List<Integer>> markedSwapIndizes = operator.swapEntities();
            if (logger.isDebugEnabled()) logger.debug("Scrambled " + markedSwapIndizes.get(0).size() + " + " + markedSwapIndizes.get(1).size() + " + " + markedSwapIndizes.get(2).size() + " indexes.");
            computeDistance(markedSwapIndizes);
            logger.debug("Mutate: exchange");
            List<List<Integer>> markedOperatorIndizes = operator.operator2nd(curIter, maxIterations);
            if (logger.isDebugEnabled()) logger.debug("Exchanged " + markedOperatorIndizes.get(0).size() + " + " + markedOperatorIndizes.get(1).size() + " + " + markedOperatorIndizes.get(2).size() + " indexes.");
            computeDistance(markedOperatorIndizes);
            updateAlignment();
            computeAlignmentFitness();
            curIter++;
        }
    }

    protected void computeDistance(List<List<Integer>> markedIndizes) {
        List<Integer> markedClassIndexes = markedIndizes.get(0);
        List<Integer> markedDataPropertyIndexes = markedIndizes.get(1);
        List<Integer> markedObjektPropertyIndexes = markedIndizes.get(2);
        updateCorrConfidence(classesPermutation, markedClassIndexes);
        updateCorrConfidence(dataPropertiesPermutation, markedDataPropertyIndexes);
        updateCorrConfidence(objectPropertiesPermutation, markedObjektPropertyIndexes);
    }

    private <T extends CorrespondencePermutation> void updateCorrConfidence(T data, List<Integer> markedIndizes) {
        for (int i = 0; i < markedIndizes.size(); i++) {
            final int index = markedIndizes.get(i);
            Correspondence<? extends OWLEntity> corr = data.getCorrespondence(index);
            if (corr != null) {
                double corrEvaluation;
                try {
                    corrEvaluation = corrEvaluator.getEvaluation(corr);
                } catch (InfeasibleEvaluatorException e) {
                    final String errMsg = "Correspondence could not be evaluated. " + "Infeasible correspondence evaluator. " + "Correspondence evaluation score will be 0.";
                    logger.warn(errMsg);
                    corrEvaluation = 0.d;
                }
                data.setConfidence(index, corrEvaluation);
            } else data.setConfidence(index, null);
        }
    }

    /**
	 * Computes the fitness of the current alignment.
	 * Updates the known best fitness if the current fitness is better.
	 */
    protected void computeAlignmentFitness() {
        logger.debug("Computing alignment fitness.");
        double fitness = 0.d;
        try {
            fitness = alignmentEvaluator.getEvaluation(alignment);
        } catch (InfeasibleEvaluatorException e) {
            final String errMsg = "Alignment evaluator not applicable. Alignment fitness will be 0.";
            logger.warn(errMsg);
        }
        if (logger.isTraceEnabled()) {
            StringBuilder permBuilder = new StringBuilder("Permutation: ");
            StringBuilder confBuilder = new StringBuilder("Confidence : ");
            for (int i = 0; i < classesPermutation.getPermutations().length; i++) {
                permBuilder.append(classesPermutation.getPermutation(i) + " ");
                confBuilder.append(classesPermutation.getConfidence(i) + " ");
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Alignment fitness is " + fitness);
            logger.debug("Alignment size is " + alignment.size());
        }
        if (bestFitness < fitness) {
            bestFitness = fitness;
            if (logger.isDebugEnabled()) logger.debug("New best fitness: " + bestFitness);
            localBestClasses = classesPermutation.clone();
            localBestDataProperties = dataPropertiesPermutation.clone();
            localBestObjectProperties = objectPropertiesPermutation.clone();
        }
    }

    /**
	 * Updates the current alignment according to the correspondence permutations
	 * for the different entity types.
	 */
    private void updateAlignment() {
        logger.debug("Updating alignment.");
        alignment.clear();
        try {
            for (int i = 0; i < classesPermutation.getMaxEntities(); i++) {
                Correspondence<? extends OWLEntity> corr = classesPermutation.getCorrespondence(i);
                if (corr != null) alignment.addCorrespondence(corr);
            }
            for (int i = 0; i < objectPropertiesPermutation.getMaxEntities(); i++) {
                Correspondence<? extends OWLEntity> corr = objectPropertiesPermutation.getCorrespondence(i);
                if (corr != null) alignment.addCorrespondence(corr);
            }
            for (int i = 0; i < dataPropertiesPermutation.getMaxEntities(); i++) {
                Correspondence<? extends OWLEntity> corr = dataPropertiesPermutation.getCorrespondence(i);
                if (corr != null) alignment.addCorrespondence(corr);
            }
        } catch (MultiplicityException e) {
            final String errMsg = "Internal error. Multiplicity violated. Probably a bug in the correspondence permutation.";
            logger.fatal(errMsg);
            throw new AssertionError(errMsg);
        } catch (IncompatibleOntologyException e) {
            final String errMsg = "Internal error. Incompatible ontology. Should not happen.";
            logger.fatal(errMsg);
            throw new AssertionError(errMsg);
        }
    }

    public PermutData getPermutionData() {
        if (localBestClasses != null && localBestDataProperties != null && localBestObjectProperties != null) {
            data.setRefForClasses(localBestClasses);
            data.setRefForData(localBestDataProperties);
            data.setRefForObject(localBestObjectProperties);
        }
        return data;
    }

    public double getFitness() {
        return bestFitness;
    }

    public void setPermutionData(PermutData data) {
        this.data = data;
    }

    public void setFitness(double bestFitness) {
        this.bestFitness = bestFitness;
    }

    @SuppressWarnings("unchecked")
    private void readParams() throws MapEVOConfigurationException {
        logger.trace("Reading parameters in Species");
        String corrEvaluatorParamValue = Config.getMainConfig().getProperty(CORRESPONDENCE_EVALUATOR_PARAMETER_KEY);
        if (corrEvaluatorParamValue == null) {
            final String errMsg = "Configuration parameter \"" + CORRESPONDENCE_EVALUATOR_PARAMETER_KEY + "\" missing.";
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        }
        String[] ceParamValues = corrEvaluatorParamValue.split("\\s");
        if (ceParamValues.length != 2) {
            final String errMsg = "Configuration parameter value of \"" + CORRESPONDENCE_EVALUATOR_PARAMETER_KEY + "\" must contain exactly two tokens: classname and ID";
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        }
        final String corrEvaluatorClassname = ceParamValues[0];
        final String corrEvaluatorID = ceParamValues[1];
        try {
            Class<Evaluator> globalCorrespondenceEvaluatorClass = (Class<Evaluator>) Class.forName(corrEvaluatorClassname);
            final Class<?>[] paramTypes = { Properties.class, String.class, Alignment.class };
            final Object[] params = { Config.getMainConfig(), corrEvaluatorID, alignment };
            Constructor<Evaluator> constructor = globalCorrespondenceEvaluatorClass.getConstructor(paramTypes);
            corrEvaluator = constructor.newInstance(params);
        } catch (ClassNotFoundException e) {
            final String errMsg = "Cannot find specified correspondence evaluator class: " + corrEvaluatorClassname;
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        } catch (ClassCastException e) {
            final String errMsg = "Correspondence evaluator class must be of type " + corrEvaluator.getClass().getName();
            logger.error(errMsg, e);
            throw new MapEVOConfigurationException(errMsg, e);
        } catch (SecurityException e) {
            final String errMsg = "No access to constructor of the correspondence evaluator class.";
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        } catch (NoSuchMethodException e) {
            final String errMsg = "Invalid correspondence evaluator. Required constructor missing.";
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        } catch (IllegalArgumentException e) {
            final String errMsg = "Internal error. Constructor arguments and argument types do not match " + "when trying to instantiate correspondence evaluator.";
            logger.fatal(errMsg);
            throw new AssertionError(errMsg);
        } catch (InstantiationException e) {
            final String errMsg = "Problem instantiating correspondence evaluator. Probabily the specified class is abstrct.";
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        } catch (IllegalAccessException e) {
            final String errMsg = "Invalid correspondence evaluator. Constructor inaccessible.";
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        } catch (InvocationTargetException e) {
            final String errMsg = "Error instantiating correspondence evaluator.";
            logger.error(errMsg, e.getCause());
            throw new MapEVOConfigurationException(errMsg, e.getCause());
        }
        String globalAEParamValue = Config.getMainConfig().getProperty(GLOBAL_ALIGNMENT_EVALUATOR_PARAMETER_KEY);
        if (globalAEParamValue == null) {
            final String errMsg = "Configuration parameter \"" + GLOBAL_ALIGNMENT_EVALUATOR_PARAMETER_KEY + "\" missing.";
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        }
        String[] gaeParamValues = globalAEParamValue.split("\\s");
        if (gaeParamValues.length != 2) {
            final String errMsg = "Configuration parameter value of \"" + GLOBAL_ALIGNMENT_EVALUATOR_PARAMETER_KEY + "\" must contain exactly two tokens: classname and ID";
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        }
        final String globalAEClassname = gaeParamValues[0];
        final String globalAEID = gaeParamValues[1];
        try {
            Class<Evaluator> globalAlignmentEvaluatorClass = (Class<Evaluator>) Class.forName(globalAEClassname);
            final Class<?>[] paramTypes = { Properties.class, String.class, Alignment.class };
            final Object[] params = { Config.getMainConfig(), globalAEID, alignment };
            Constructor<Evaluator> constructor = globalAlignmentEvaluatorClass.getConstructor(paramTypes);
            alignmentEvaluator = constructor.newInstance(params);
        } catch (ClassNotFoundException e) {
            final String errMsg = "Cannot find specified global alignment evaluator class: " + globalAEClassname;
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        } catch (ClassCastException e) {
            final String errMsg = "Global alignment evaluator class must be of type " + alignmentEvaluator.getClass().getName();
            logger.error(errMsg, e);
            throw new MapEVOConfigurationException(errMsg, e);
        } catch (SecurityException e) {
            final String errMsg = "No access to constructor of the global alignment evaluator class.";
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        } catch (NoSuchMethodException e) {
            final String errMsg = "Invalid global alignment evaluator. Required constructor missing.";
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        } catch (IllegalArgumentException e) {
            final String errMsg = "Internal error. Constructor arguments and argument types do not match " + "when trying to instantiate global alignment evaluator.";
            logger.fatal(errMsg);
            throw new AssertionError(errMsg);
        } catch (InstantiationException e) {
            final String errMsg = "Problem instantiating global alignment evaluator. Probabily the specified class is abstrct.";
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        } catch (IllegalAccessException e) {
            final String errMsg = "Invalid global alignment evaluator. Constructor inaccessible.";
            logger.error(errMsg);
            throw new MapEVOConfigurationException(errMsg);
        } catch (InvocationTargetException e) {
            final String errMsg = "Error instantiating global alignment evaluator.";
            logger.error(errMsg, e.getCause());
            throw new MapEVOConfigurationException(errMsg, e.getCause());
        }
    }

    public int getCurRound() {
        return curRound;
    }
}
