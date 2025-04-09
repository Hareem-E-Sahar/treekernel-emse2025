package com.rapidminer.operator.valueseries;

import java.util.LinkedList;
import java.util.List;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.SimpleOperatorChain;
import com.rapidminer.operator.valueseries.functions.CombinedFunction;
import com.rapidminer.operator.valueseries.functions.Function;
import com.rapidminer.operator.valueseries.functions.SingleCombinedFunction;
import com.rapidminer.operator.valueseries.transformations.SimpleWindowing;
import com.rapidminer.operator.valueseries.transformations.Transformation;
import com.rapidminer.operator.valueseries.transformations.Windowing;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.RandomGenerator;

/**
 * <p>
 * This class is a helper class for all operator purposes in respect to genetic programming approches for automatic
 * value series preprocessing. Objects of an OperatorFactory can create a new operator of a given type. Additionally
 * some useful static methods for handling operator changing, removing and adding are provided.
 * </p>
 * <p>
 * At initiation time it searches for all usable value series operators defined in any of the operators.xml files and
 * checks if they are usable for automatic preprocessing.
 * </p>
 * 
 * <p>
 * The methods of this class fulfill the constraints described in the master thesis. They are in detail:
 * <ul>
 * <li>Generation of a new transformation only before a function.</li>
 * <li>New windowing operators with a greater overlap than 1 ends must contain a function, arbitrary number of
 * transformations are allowed.</li>
 * <li>Windowing operators with overlap 1 can ommit the function (piecewise filtering). The inner transformations
 * should be filter operators. A combined transformation can build a filter, too.</li>
 * <li>The same applies for combined functions. They should not be empty and contains the correct number of children.</li>
 * <li>Empty method trees are not allowed.</li>
 * <li>Removing of operators consider the mentioned contraints</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Automatic approaches can make use of this class.
 * </p>
 * 
 * @author Ingo Mierswa
 * @version $Id: OperatorFactory.java,v 1.7 2009-03-14 08:48:19 ingomierswa Exp $
 */
@Deprecated
public class OperatorFactory {

    /** Indicates an unknown operator type. */
    public static final int UNKNOWN_TYPE = -1;

    /** Indicates an operator of type transformation. */
    public static final int TRANSFORMATION = 0;

    /** Indicates an operator of type windowing. */
    public static final int WINDOWING = 1;

    /** Indicates an operator of type function. */
    public static final int FUNCTION = 2;

    /** Indicates an operator of type combined function. */
    public static final int COMBINED_FUNCTION = 3;

    /** Indicates an operator of type single combined function. */
    public static final int SINGLE_COMBINED_FUNCTION = 4;

    /** Indicates an operator chain in a windowing operator. */
    public static final int WINDOWING_CHAIN = 5;

    /** Indicates an operator chain in a branching operator. */
    public static final int BRANCHING_CHAIN = 6;

    /** Indicates a branching operator. */
    public static final int BRANCHING = 7;

    /** Indicates a preprocessing operator chain. */
    public static final int PREPROCESSING_CHAIN = 8;

    /** This is the highest index of operator type which can be validly created. */
    public static final int MAX_VALID_TYPE = SINGLE_COMBINED_FUNCTION;

    /** This collection holds all applicable transformations. */
    private List<String> transformations = new LinkedList<String>();

    /** This collection holds all applicable functions. */
    private List<String> functions = new LinkedList<String>();

    /** This is used as overlap for all windowings. */
    private double windowingOverlap = 2.0;

    /** This value is used as minimum number of values of each windowing. */
    private int windowingMinValues = 100;

    /**
     * Specifies the probability that a freshly created Windowing operator contains a transformation before the inner
     * function operator.
     */
    private double transformationInWindowingProb = 0.5;

    /**
     * Specifies the probability that a freshly created Windowing operator with overlap 1 contains a function operator.
     * If not, the windowing will contain only a transformation (piecewise filtering).
     */
    private double functionInWindowingProb = 1.0;

    /**
     * Creates a new OperatorFactory. For freshly created
     * Windowing operators the default step size and windowing size are used and they do not contain a transformation
     * before their inner function operator.
     */
    public OperatorFactory() {
        this(2.0, 100, 0.5, 1.0);
    }

    /**
     * Creates a new OperatorFactory. The given window and
     * step size are used as initial values for newly created windowing operators. The probabilities specify the chance
     * that a freshly created Windowing operator contains a transformation before the inner function operator. The
     * functionInWindowingProb will only be used for windowing operators with overlap 1.
     */
    public OperatorFactory(double windowingOverlap, int windowingMinValues, double transformationInWindowingProb, double functionInWindowingProb) {
        this.windowingOverlap = windowingOverlap;
        this.windowingMinValues = windowingMinValues;
        this.transformationInWindowingProb = transformationInWindowingProb;
        this.functionInWindowingProb = functionInWindowingProb;
        init();
    }

    /**
     * Searches all value series operators defined in any of the operator.xml files and adds them to the correct list if
     * they are usable for automatic preprocessing. Windowing in the combined function will be handled separately.
     */
    private void init() {
        for (String key : OperatorService.getOperatorKeys()) {
            OperatorDescription description = OperatorService.getOperatorDescription(key);
            if (ValueSeriesOperator.class.isAssignableFrom(description.getOperatorClass())) handleOperator(description);
        }
    }

    /** Adds the given class to the correct list if is usable for automatic preprocessing. */
    private void handleOperator(OperatorDescription description) {
        Class<?> clazz = description.getOperatorClass();
        try {
            ValueSeriesOperator operator = (ValueSeriesOperator) description.createOperatorInstance();
            if (operator.isUsableForAutomaticPreprocessing()) {
                if (Transformation.class.isAssignableFrom(clazz)) {
                    if (!Windowing.class.isAssignableFrom(clazz)) transformations.add(description.getName());
                } else if (Function.class.isAssignableFrom(clazz)) {
                    if ((!CombinedFunction.class.isAssignableFrom(clazz)) && (!SingleCombinedFunction.class.isAssignableFrom(clazz))) functions.add(description.getName());
                } else {
                    LogService.getGlobal().log("VSP operator class not used for automatic feature extraction: " + description.getName(), LogService.WARNING);
                }
            }
        } catch (OperatorCreationException e) {
            LogService.getGlobal().log("Cannot instantiate class: " + clazz.getName(), LogService.ERROR);
        }
    }

    /**
     * Creates a single operator of the given type. Windowing operators do not contain a windowing chain and a function,
     * branching operators are empty! Ready-to-use operators are delivered by createValidOperator(int).
     */
    public Operator createSingleOperator(int type, RandomGenerator random) throws NoValidOperatorException {
        Operator vsOp = null;
        try {
            switch(type) {
                case TRANSFORMATION:
                    String trafoName = transformations.get(random.nextInt(transformations.size()));
                    vsOp = OperatorService.createOperator(trafoName);
                    break;
                case WINDOWING:
                    vsOp = OperatorService.createOperator("Windowing");
                    ((SimpleWindowing) vsOp).setWindowParameters(windowingOverlap, windowingMinValues);
                    break;
                case FUNCTION:
                    String functionName = functions.get(random.nextInt(functions.size()));
                    vsOp = OperatorService.createOperator(functionName);
                    break;
                case COMBINED_FUNCTION:
                    vsOp = OperatorService.createOperator("CombinedFunction");
                    break;
                case SINGLE_COMBINED_FUNCTION:
                    vsOp = OperatorService.createOperator("SingleCombinedFunction");
                    break;
                case WINDOWING_CHAIN:
                    vsOp = OperatorService.createOperator("OperatorChain");
                    break;
                case BRANCHING_CHAIN:
                    vsOp = OperatorService.createOperator("OperatorChain");
                    break;
                case BRANCHING:
                    vsOp = OperatorService.createOperator("Branching");
                    break;
                case PREPROCESSING_CHAIN:
                    vsOp = OperatorService.createOperator("OperatorChain");
                    break;
                default:
                    throw new NoValidOperatorException("No operator defined for type: " + type);
            }
        } catch (Exception e) {
            throw new NoValidOperatorException(e.getMessage());
        }
        return vsOp;
    }

    /**
     * Creates a valid operator chain of the given type. Only types up to MAX_VALID_TYPE are allowed. Windowing
     * operators will contain an inner operator chain and a function, and branching operators provides empty inner
     * operator chains.
     */
    public Operator createValidOperator(int type, RandomGenerator random) throws NoValidOperatorException, UndefinedParameterError {
        if ((type == UNKNOWN_TYPE) || (type > MAX_VALID_TYPE)) throw new NoValidOperatorException("OperatorFactory: Type " + type + " is not allowed for creating valid operators!");
        if (type == WINDOWING) {
            Windowing windowing = (Windowing) createSingleOperator(type, random);
            OperatorChain chain = (OperatorChain) createSingleOperator(WINDOWING_CHAIN, random);
            windowing.addOperator(chain);
            if ((windowing.getOverlap() <= 1.0d) || (random.nextDouble() < transformationInWindowingProb)) chain.addOperator(createSingleOperator(TRANSFORMATION, random));
            if ((windowing.getOverlap() > 1.0d) || (random.nextDouble() < functionInWindowingProb)) chain.addOperator(createSingleOperator(FUNCTION, random));
            return windowing;
        } else if (type == COMBINED_FUNCTION) {
            OperatorChain combinedFunction = (OperatorChain) createSingleOperator(type, random);
            Operator function1 = createSingleOperator(FUNCTION, random);
            Operator function2 = createSingleOperator(FUNCTION, random);
            combinedFunction.addOperator(function1);
            combinedFunction.addOperator(function2);
            return combinedFunction;
        } else if (type == SINGLE_COMBINED_FUNCTION) {
            OperatorChain combinedFunction = (OperatorChain) createSingleOperator(type, random);
            Operator innerFunction = createSingleOperator(FUNCTION, random);
            combinedFunction.addOperator(innerFunction);
            return combinedFunction;
        } else {
            return createSingleOperator(type, random);
        }
    }

    /**
     * Adds a randomly choosen valid operator to a random place in the given chain. The new created operator will be
     * returned. This is especially useful for automatic generating purposes. Each of the valid types has equal
     * probability.
     * 
     * @throws NoValidOperatorException
     * @throws UndefinedParameterError
     */
    public Operator addRandomValidOperatorTo(OperatorChain chain, RandomGenerator random) throws UndefinedParameterError, NoValidOperatorException {
        int which = random.nextInt(MAX_VALID_TYPE + 1);
        Operator vsOp = createValidOperator(which, random);
        addOperator(chain, vsOp, random);
        return vsOp;
    }

    /**
     * Performs a DFS to determine all possible operator chains to add the generated operator to. Randomly selects one
     * of these chains and a place in this chain. Only Transformations can be added to windowing chains and they will be
     * added before the windowing function.
     */
    public void addOperator(OperatorChain op, Operator vsOp, RandomGenerator random) {
        List<Operator> possibleChains = new LinkedList<Operator>();
        addChain(op, possibleChains, vsOp instanceof Transformation);
        if (possibleChains.size() > 0) {
            int index = random.nextInt(possibleChains.size());
            OperatorChain chain = (OperatorChain) possibleChains.get(index);
            int maxIndex = chain.getParent() instanceof Windowing ? chain.getNumberOfOperators() : chain.getNumberOfOperators() + 1;
            int chainIndex = 0;
            if (maxIndex > 0) chainIndex = random.nextInt(maxIndex);
            chain.addOperator(vsOp, chainIndex);
        }
    }

    /** Adds a chain to the given list. */
    private void addChain(OperatorChain op, List<Operator> chains, boolean addToWindowing) {
        if (!(op instanceof Windowing) && !(op instanceof CombinedFunction) && !(op instanceof SingleCombinedFunction)) chains.add(op);
        if (!(op instanceof Windowing) || addToWindowing) {
            for (int i = 0; i < op.getNumberOfOperators(); i++) {
                Operator child = op.getOperator(i);
                if (child instanceof OperatorChain) {
                    addChain((OperatorChain) child, chains, addToWindowing);
                }
            }
        }
    }

    /**
     * Creates a ValueSeriesPreprocessing operator. The given operator is used as inner operator for the preprocessing
     * (attribute transforming) of an input example set.
     * 
     * @throws OperatorCreationException
     */
    public Operator createValueSeriesPreprocessing(Operator preprocessing) throws InstantiationException, IllegalAccessException, OperatorCreationException {
        OperatorChain vsp = (OperatorChain) OperatorService.createOperator("ValueSeriesPreprocessing");
        vsp.addOperator(preprocessing);
        return vsp;
    }

    /** Returns the type of the given operator. */
    public static int getTypeOfOperator(Operator op) {
        if (op instanceof Function) return FUNCTION;
        if (op instanceof Windowing) return WINDOWING;
        if (op instanceof Transformation) return TRANSFORMATION;
        if (op instanceof MergeSeriesOperator) return BRANCHING;
        if (op instanceof SimpleOperatorChain) {
            Operator parent = op.getParent();
            if (parent == null) return PREPROCESSING_CHAIN; else {
                if (parent instanceof Windowing) return WINDOWING_CHAIN; else if (parent instanceof MergeSeriesOperator) return BRANCHING_CHAIN; else return UNKNOWN_TYPE;
            }
        }
        return UNKNOWN_TYPE;
    }

    /** Returns the number of inner operators with the given class. */
    public static int getNumberOf(Operator op, Class<?> clazz) {
        int result = 0;
        if (clazz.isInstance(op)) result++;
        if (op instanceof OperatorChain) {
            OperatorChain chain = (OperatorChain) op;
            for (int i = 0; i < chain.getNumberOfOperators(); i++) result += getNumberOf(chain.getOperator(i), clazz);
        }
        return result;
    }

    /** Returns true if the given operator chain has a child with the given class. */
    public static boolean hasChild(OperatorChain op, Class<?> clazz) {
        for (int i = 0; i < op.getNumberOfOperators(); i++) if (clazz.isInstance(op.getOperator(i))) return true;
        return false;
    }

    /** Returns a list with all operators in the given chain which has the specified type. */
    public static List<Operator> getAllOfClass(Operator op, Class<?> clazz) {
        List<Operator> result = new LinkedList<Operator>();
        getAllOfClass(result, op, clazz);
        return result;
    }

    /** Fills the given list with all operators in the given chain which has the specified type. */
    private static void getAllOfClass(List<Operator> result, Operator op, Class<?> clazz) {
        if (clazz.isInstance(op)) result.add(op);
        if (op instanceof OperatorChain) {
            OperatorChain chain = (OperatorChain) op;
            for (int i = 0; i < chain.getNumberOfOperators(); i++) getAllOfClass(result, chain.getOperator(i), clazz);
        }
    }

    /**
     * Returns true if the given operator is nested in an operator of the given class. Returns also true if the given
     * operator itself is from the class.
     */
    public static boolean isNestedIn(Operator op, Class<?> clazz) {
        if (clazz.isInstance(op)) return true;
        Operator parent = op.getParent();
        if (parent == null) return false; else return isNestedIn(parent, clazz);
    }
}
