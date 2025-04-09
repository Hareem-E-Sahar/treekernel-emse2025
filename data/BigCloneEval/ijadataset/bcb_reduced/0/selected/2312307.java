package examples.gp.symbolicRegression;

import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import org.jgap.*;
import org.jgap.gp.*;
import org.jgap.gp.function.*;
import org.jgap.gp.impl.*;
import org.jgap.gp.terminal.*;
import org.jgap.util.*;

public class SymbolicRegression extends GPProblem {

    private static transient Logger LOGGER = Logger.getLogger(SymbolicRegression.class);

    public static int numInputVariables;

    public static Variable[] variables;

    public static String[] variableNames;

    public static Integer outputVariable;

    public static int[] ignoreVariables;

    public static ArrayList<Double> constants = new ArrayList<Double>();

    public static int numRows;

    protected static Double[][] data;

    public static boolean foundPerfect = false;

    public static int minInitDepth = 2;

    public static int maxInitDepth = 4;

    public static int populationSize = 100;

    public static int maxCrossoverDepth = 8;

    public static int programCreationMaxTries = 5;

    public static int numEvolutions = 1800;

    public static boolean verboseOutput = true;

    public static int maxNodes = 21;

    public static double functionProb = 0.9d;

    public static float reproductionProb = 0.1f;

    public static float mutationProb = 0.1f;

    public static double crossoverProb = 0.9d;

    public static float dynamizeArityProb = 0.08f;

    public static double newChromsPercent = 0.3d;

    public static int tournamentSelectorSize = 0;

    public static double lowerRange = -10.0d;

    public static double upperRange = -10.0d;

    public static boolean terminalWholeNumbers = true;

    public static String returnType = "DoubleClass";

    public static String presentation = "";

    public static int adfArity = 0;

    public static String adfType = "double";

    public static boolean useADF = false;

    public static String[] functions = { "Multiply", "Divide", "Add", "Subtract" };

    public static String[] adfFunctions = { "Multiply3", "Divide", "Add3", "Subtract" };

    public static double scaleError = -1.0d;

    public static boolean bumpPerfect = false;

    public static Double bumpValue = 0.0000;

    private static HashMap<String, Integer> foundSolutions = new HashMap<String, Integer>();

    public static long startTime;

    public static long endTime;

    public static double stopCriteria = -1.0d;

    public static boolean showPopulation = false;

    public static boolean showSimiliar = false;

    public SymbolicRegression(GPConfiguration a_conf) throws InvalidConfigurationException {
        super(a_conf);
    }

    /**
   * This method is used for setting up the commands and terminals that can be
   * used to solve the problem.
   *
   * @return GPGenotype
   * @throws InvalidConfigurationException
   */
    public GPGenotype create() throws InvalidConfigurationException {
        GPConfiguration conf = getGPConfiguration();
        Class[] types;
        Class[][] argTypes;
        if (useADF) {
            if ("boolean".equals(adfType)) {
                types = new Class[] { CommandGene.DoubleClass, CommandGene.BooleanClass };
            } else if ("integer".equals(adfType)) {
                types = new Class[] { CommandGene.DoubleClass, CommandGene.IntegerClass };
            } else {
                types = new Class[] { CommandGene.DoubleClass, CommandGene.DoubleClass };
            }
            Class[] adfs = new Class[adfArity];
            for (int i = 0; i < adfArity; i++) {
                if ("boolean".equals(adfType)) {
                    adfs[i] = CommandGene.BooleanClass;
                } else if ("integer".equals(adfType)) {
                    adfs[i] = CommandGene.IntegerClass;
                } else {
                    adfs[i] = CommandGene.DoubleClass;
                }
            }
            argTypes = new Class[][] { {}, adfs };
        } else {
            types = new Class[] { CommandGene.DoubleClass };
            argTypes = new Class[][] { {} };
        }
        int[] minDepths;
        int[] maxDepths;
        if (useADF) {
            minDepths = new int[] { 1, 1 };
            maxDepths = new int[] { 9, 9 };
        } else {
            minDepths = new int[] { 1 };
            maxDepths = new int[] { 9 };
        }
        CommandGene[] commands = makeCommands(conf, functions, lowerRange, upperRange, "plain");
        int command_len = commands.length;
        CommandGene[][] nodeSets = new CommandGene[2][numInputVariables + command_len];
        variables = new Variable[numInputVariables];
        int variableIndex = 0;
        for (int i = 0; i < numInputVariables + 1; i++) {
            String variableName = variableNames[i];
            if (i != outputVariable) {
                if (variableNames != null && variableNames.length > 0) {
                    variableName = variableNames[i];
                }
                variables[variableIndex] = Variable.create(conf, variableName, CommandGene.DoubleClass);
                nodeSets[0][variableIndex] = variables[variableIndex];
                System.out.println("input variable: " + variables[variableIndex]);
                variableIndex++;
            }
        }
        for (int i = 0; i < command_len; i++) {
            System.out.println("function1: " + commands[i]);
            nodeSets[0][i + numInputVariables] = commands[i];
        }
        if (useADF) {
            CommandGene[] adfCommands = makeCommands(conf, adfFunctions, lowerRange, upperRange, "ADF");
            int adfLength = adfCommands.length;
            nodeSets[1] = new CommandGene[adfLength];
            for (int i = 0; i < adfLength; i++) {
                System.out.println("function2: " + adfCommands[i]);
                nodeSets[1][i] = adfCommands[i];
            }
        }
        boolean[] full;
        if (useADF) {
            full = new boolean[] { true, true };
        } else {
            full = new boolean[] { true };
        }
        boolean[] fullModeAllowed = full;
        return GPGenotype.randomInitialGenotype(conf, types, argTypes, nodeSets, maxNodes, verboseOutput);
    }

    public static void readFile(String file) {
        try {
            BufferedReader inr = new BufferedReader(new FileReader(file));
            String str;
            int lineCount = 0;
            boolean gotData = false;
            ArrayList<Double[]> theData = new ArrayList<Double[]>();
            while ((str = inr.readLine()) != null) {
                lineCount++;
                str = str.trim();
                if (str.startsWith("#") || str.startsWith("%") || str.length() == 0) {
                    continue;
                }
                if ("data".equals(str)) {
                    gotData = true;
                    continue;
                }
                if (gotData) {
                    String[] dataRowStr = str.split("[\\s,]+");
                    int len = dataRowStr.length;
                    Double[] dataRow = new Double[len];
                    for (int i = 0; i < len; i++) {
                        dataRow[i] = Double.parseDouble(dataRowStr[i]);
                    }
                    theData.add(dataRow);
                } else {
                    if (str.contains(":")) {
                        String row[] = str.split(":\\s*");
                        if ("return_type".equals(row[0])) {
                            returnType = row[1];
                        } else if ("presentation".equals(row[0])) {
                            presentation = row[1];
                        } else if ("num_input_variables".equals(row[0])) {
                            numInputVariables = Integer.parseInt(row[1]);
                        } else if ("num_rows".equals(row[0])) {
                            numRows = Integer.parseInt(row[1]);
                        } else if ("terminal_range".equals(row[0])) {
                            String[] ranges = row[1].split("\\s+");
                            lowerRange = Double.parseDouble(ranges[0]);
                            upperRange = Double.parseDouble(ranges[1]);
                        } else if ("terminal_wholenumbers".equals(row[0])) {
                            terminalWholeNumbers = Boolean.parseBoolean(row[1]);
                        } else if ("max_init_depth".equals(row[0])) {
                            maxInitDepth = Integer.parseInt(row[1]);
                        } else if ("min_init_depth".equals(row[0])) {
                            minInitDepth = Integer.parseInt(row[1]);
                        } else if ("program_creation_max_tries".equals(row[0])) {
                            programCreationMaxTries = Integer.parseInt(row[1]);
                        } else if ("population_size".equals(row[0])) {
                            populationSize = Integer.parseInt(row[1]);
                        } else if ("max_crossover_depth".equals(row[0])) {
                            maxCrossoverDepth = Integer.parseInt(row[1]);
                        } else if ("function_prob".equals(row[0])) {
                            functionProb = Double.parseDouble(row[1]);
                        } else if ("reproduction_prob".equals(row[0])) {
                            reproductionProb = Float.parseFloat(row[1]);
                        } else if ("mutation_prob".equals(row[0])) {
                            mutationProb = Float.parseFloat(row[1]);
                        } else if ("crossover_prob".equals(row[0])) {
                            crossoverProb = Double.parseDouble(row[1]);
                        } else if ("dynamize_arity_prob".equals(row[0])) {
                            dynamizeArityProb = Float.parseFloat(row[1]);
                        } else if ("new_chroms_percent".equals(row[0])) {
                            newChromsPercent = Double.parseDouble(row[1]);
                        } else if ("num_evolutions".equals(row[0])) {
                            numEvolutions = Integer.parseInt(row[1]);
                        } else if ("max_nodes".equals(row[0])) {
                            maxNodes = Integer.parseInt(row[1]);
                        } else if ("bump".equals(row[0])) {
                            bumpPerfect = Boolean.parseBoolean(row[1]);
                        } else if ("bump_value".equals(row[0])) {
                            bumpValue = Double.parseDouble(row[1]);
                        } else if ("functions".equals(row[0])) {
                            functions = row[1].split("[\\s,]+");
                        } else if ("adf_functions".equals(row[0])) {
                            adfFunctions = row[1].split("[\\s,]+");
                        } else if ("variable_names".equals(row[0])) {
                            variableNames = row[1].split("[\\s,]+");
                        } else if ("output_variable".equals(row[0])) {
                            outputVariable = Integer.parseInt(row[1]);
                        } else if ("ignore_variables".equals(row[0])) {
                            String[] ignoreVariablesS = row[1].split("[\\s,]+");
                            ignoreVariables = new int[ignoreVariablesS.length];
                            for (int i = 0; i < ignoreVariablesS.length; i++) {
                                ignoreVariables[i] = Integer.parseInt(ignoreVariablesS[i]);
                            }
                        } else if ("constant".equals(row[0])) {
                            Double constant = Double.parseDouble(row[1]);
                            constants.add(constant);
                        } else if ("adf_arity".equals(row[0])) {
                            adfArity = Integer.parseInt(row[1]);
                            System.out.println("ADF arity " + adfArity);
                            if (adfArity > 0) {
                                useADF = true;
                            }
                        } else if ("adf_type".equals(row[0])) {
                            adfType = row[1];
                        } else if ("tournament_selector_size".equals(row[0])) {
                            tournamentSelectorSize = Integer.parseInt(row[1]);
                        } else if ("scale_error".equals(row[0])) {
                            scaleError = Double.parseDouble(row[1]);
                        } else if ("stop_criteria".equals(row[0])) {
                            stopCriteria = Double.parseDouble(row[1]);
                        } else if ("show_population".equals(row[0])) {
                            showPopulation = Boolean.parseBoolean(row[1]);
                        } else if ("show_similiar".equals(row[0])) {
                            showSimiliar = Boolean.parseBoolean(row[1]);
                        } else {
                            System.out.println("Unknown keyword: " + row[0] + " on line " + lineCount);
                            System.exit(1);
                        }
                    }
                }
            }
            inr.close();
            int r = theData.size();
            int c = theData.get(0).length;
            int numIgnore = 0;
            if (ignoreVariables != null) {
                numIgnore = ignoreVariables.length;
            }
            Double[][] dataTmp = new Double[r][c];
            for (int i = 0; i < r; i++) {
                Double[] this_row = theData.get(i);
                for (int j = 0; j < c; j++) {
                    dataTmp[i][j] = this_row[j];
                }
            }
            data = transposeMatrix(dataTmp);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public static Double[][] transposeMatrix(Double[][] m) {
        int r = m.length;
        int c = m[0].length;
        Double[][] t = new Double[c][r];
        for (int i = 0; i < r; ++i) {
            for (int j = 0; j < c; ++j) {
                t[j][i] = m[i][j];
            }
        }
        return t;
    }

    static CommandGene[] makeCommands(GPConfiguration conf, String[] functions, Double lowerRange, Double upperRange, String type) {
        ArrayList<CommandGene> commandsList = new ArrayList<CommandGene>();
        int len = functions.length;
        try {
            for (int i = 0; i < len; i++) {
                if ("Multiply".equals(functions[i])) {
                    commandsList.add(new Multiply(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Multiply(conf, CommandGene.BooleanClass));
                    }
                } else if ("Multiply3".equals(functions[i])) {
                    commandsList.add(new Multiply3(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Multiply3(conf, CommandGene.BooleanClass));
                    }
                } else if ("Add".equals(functions[i])) {
                    commandsList.add(new Add(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Add(conf, CommandGene.BooleanClass));
                    }
                } else if ("Divide".equals(functions[i])) {
                    commandsList.add(new Divide(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Divide(conf, CommandGene.BooleanClass));
                    }
                } else if ("Add3".equals(functions[i])) {
                    commandsList.add(new Add3(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Add3(conf, CommandGene.BooleanClass));
                    }
                } else if ("Add4".equals(functions[i])) {
                    commandsList.add(new Add4(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Add4(conf, CommandGene.BooleanClass));
                    }
                } else if ("Subtract".equals(functions[i])) {
                    commandsList.add(new Subtract(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Subtract(conf, CommandGene.BooleanClass));
                    }
                } else if ("Sine".equals(functions[i])) {
                    commandsList.add(new Sine(conf, CommandGene.DoubleClass));
                } else if ("ArcSine".equals(functions[i])) {
                    commandsList.add(new ArcSine(conf, CommandGene.DoubleClass));
                } else if ("Tangent".equals(functions[i])) {
                    commandsList.add(new Tangent(conf, CommandGene.DoubleClass));
                } else if ("ArcTangent".equals(functions[i])) {
                    commandsList.add(new ArcTangent(conf, CommandGene.DoubleClass));
                } else if ("Cosine".equals(functions[i])) {
                    commandsList.add(new Cosine(conf, CommandGene.DoubleClass));
                } else if ("ArcCosine".equals(functions[i])) {
                    commandsList.add(new ArcCosine(conf, CommandGene.DoubleClass));
                } else if ("Exp".equals(functions[i])) {
                    commandsList.add(new Exp(conf, CommandGene.DoubleClass));
                } else if ("Log".equals(functions[i])) {
                    commandsList.add(new Log(conf, CommandGene.DoubleClass));
                } else if ("Abs".equals(functions[i])) {
                    commandsList.add(new Abs(conf, CommandGene.DoubleClass));
                } else if ("Pow".equals(functions[i])) {
                    commandsList.add(new Pow(conf, CommandGene.DoubleClass));
                } else if ("Round".equals(functions[i])) {
                    commandsList.add(new Round(conf, CommandGene.DoubleClass));
                } else if ("Ceil".equals(functions[i])) {
                    commandsList.add(new Ceil(conf, CommandGene.DoubleClass));
                } else if ("Floor".equals(functions[i])) {
                    commandsList.add(new Floor(conf, CommandGene.DoubleClass));
                } else if ("Modulo".equals(functions[i])) {
                    commandsList.add(new Modulo(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Modulo(conf, CommandGene.BooleanClass));
                    }
                } else if ("ModuloD".equals(functions[i])) {
                    commandsList.add(new ModuloD(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new ModuloD(conf, CommandGene.BooleanClass));
                    }
                } else if ("Max".equals(functions[i])) {
                    commandsList.add(new Max(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Max(conf, CommandGene.BooleanClass));
                    }
                } else if ("Min".equals(functions[i])) {
                    commandsList.add(new Min(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Min(conf, CommandGene.BooleanClass));
                    }
                } else if ("Sqrt".equals(functions[i])) {
                    commandsList.add(new Sqrt(conf, CommandGene.DoubleClass));
                } else if ("Logistic".equals(functions[i])) {
                    commandsList.add(new Logistic(conf, CommandGene.DoubleClass));
                } else if ("Gaussian".equals(functions[i])) {
                    commandsList.add(new Gaussian(conf, CommandGene.DoubleClass));
                } else if ("Sigmoid".equals(functions[i])) {
                    commandsList.add(new Sigmoid(conf, CommandGene.DoubleClass));
                } else if ("Gamma".equals(functions[i])) {
                    commandsList.add(new Gamma(conf, CommandGene.DoubleClass));
                } else if ("Step".equals(functions[i])) {
                    commandsList.add(new Step(conf, CommandGene.DoubleClass));
                } else if ("Sign".equals(functions[i])) {
                    commandsList.add(new Sign(conf, CommandGene.DoubleClass));
                } else if ("Hill".equals(functions[i])) {
                    commandsList.add(new Hill(conf, CommandGene.DoubleClass));
                } else if ("LesserThan".equals(functions[i])) {
                    commandsList.add(new LesserThan(conf, CommandGene.BooleanClass));
                } else if ("GreaterThan".equals(functions[i])) {
                    commandsList.add(new GreaterThan(conf, CommandGene.BooleanClass));
                } else if ("If".equals(functions[i])) {
                    commandsList.add(new If(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new If(conf, CommandGene.BooleanClass));
                    }
                } else if ("IfElse".equals(functions[i])) {
                    commandsList.add(new IfElse(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new IfElse(conf, CommandGene.BooleanClass));
                    }
                } else if ("IfDyn".equals(functions[i])) {
                    commandsList.add(new IfDyn(conf, CommandGene.BooleanClass, 1, 1, 5));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new IfDyn(conf, CommandGene.DoubleClass, 1, 1, 5));
                    }
                } else if ("Loop".equals(functions[i])) {
                    commandsList.add(new Loop(conf, CommandGene.DoubleClass, 3));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Loop(conf, CommandGene.BooleanClass, 3));
                    }
                } else if ("Equals".equals(functions[i])) {
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Equals(conf, CommandGene.BooleanClass));
                    }
                } else if ("ForXLoop".equals(functions[i])) {
                    commandsList.add(new ForXLoop(conf, CommandGene.IntegerClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new ForXLoop(conf, CommandGene.BooleanClass));
                    } else if (useADF && "integer".equals(adfType)) {
                        commandsList.add(new ForXLoop(conf, CommandGene.IntegerClass));
                    }
                } else if ("ForLoop".equals(functions[i])) {
                    commandsList.add(new ForLoop(conf, CommandGene.IntegerClass, 10));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new ForLoop(conf, CommandGene.BooleanClass, 10));
                    } else if (useADF && "integer".equals(adfType)) {
                        commandsList.add(new ForLoop(conf, CommandGene.IntegerClass, 10));
                    }
                } else if ("Increment".equals(functions[i])) {
                    commandsList.add(new Increment(conf, CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Increment(conf, CommandGene.BooleanClass));
                    }
                } else if ("Argument".equals(functions[i])) {
                } else if ("StoreTerminal".equals(functions[i])) {
                    commandsList.add(new StoreTerminal(conf, "dmem0", CommandGene.DoubleClass));
                    commandsList.add(new StoreTerminal(conf, "dmem1", CommandGene.DoubleClass));
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new StoreTerminal(conf, "bmem0", CommandGene.DoubleClass));
                        commandsList.add(new StoreTerminal(conf, "bmem1", CommandGene.DoubleClass));
                    }
                } else if ("Pop".equals(functions[i])) {
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Pop(conf, CommandGene.BooleanClass));
                    }
                } else if ("Push".equals(functions[i])) {
                    commandsList.add(new Push(conf, CommandGene.DoubleClass));
                } else if ("And".equals(functions[i])) {
                    commandsList.add(new And(conf));
                } else if ("Or".equals(functions[i])) {
                    commandsList.add(new Or(conf));
                } else if ("Xor".equals(functions[i])) {
                    commandsList.add(new Xor(conf));
                } else if ("Not".equals(functions[i])) {
                    commandsList.add(new Not(conf));
                } else if ("AndD".equals(functions[i])) {
                    commandsList.add(new AndD(conf));
                } else if ("OrD".equals(functions[i])) {
                    commandsList.add(new OrD(conf));
                } else if ("XorD".equals(functions[i])) {
                    commandsList.add(new XorD(conf));
                } else if ("NotD".equals(functions[i])) {
                    commandsList.add(new NotD(conf));
                } else if ("SubProgram".equals(functions[i])) {
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new SubProgram(conf, new Class[] { CommandGene.BooleanClass, CommandGene.BooleanClass }));
                        commandsList.add(new SubProgram(conf, new Class[] { CommandGene.BooleanClass, CommandGene.BooleanClass, CommandGene.BooleanClass }));
                    }
                    commandsList.add(new SubProgram(conf, new Class[] { CommandGene.DoubleClass, CommandGene.DoubleClass }));
                    commandsList.add(new SubProgram(conf, new Class[] { CommandGene.DoubleClass, CommandGene.DoubleClass, CommandGene.DoubleClass }));
                } else if ("Tupel".equals(functions[i])) {
                    if (useADF && "boolean".equals(adfType)) {
                        commandsList.add(new Tupel(conf, new Class[] { CommandGene.BooleanClass, CommandGene.BooleanClass }));
                    }
                } else {
                    System.out.println("Unkown function: " + functions[i]);
                    System.exit(1);
                }
            }
            commandsList.add(new Terminal(conf, CommandGene.DoubleClass, lowerRange, upperRange, terminalWholeNumbers));
            if (useADF && !"ADF".equals(type)) {
                commandsList.add(new ADF(conf, 1, adfArity));
            }
            if (constants != null) {
                for (int i = 0; i < constants.size(); i++) {
                    Double constant = constants.get(i);
                    commandsList.add(new Constant(conf, CommandGene.DoubleClass, constant));
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        CommandGene[] commands = new CommandGene[commandsList.size()];
        commandsList.toArray(commands);
        return commands;
    }

    /**
   * Starts the example.
   *
   * @author Hakan Kjellerstrand
   */
    public static void main(String[] args) throws Exception {
        LOGGER.addAppender(new ConsoleAppender(new SimpleLayout(), "System.out"));
        if (args.length > 0) {
            String filename = args[0];
            readFile(filename);
        } else {
            numRows = 21;
            numInputVariables = 3;
            int[][] indata = { { 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765, 10946 }, { 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765, 10946, 17711 }, { 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765, 10946, 17711, 28657 }, { 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765, 10946, 17711, 28657, 46368 } };
            data = new Double[numInputVariables + 1][numRows];
            for (int i = 0; i < numInputVariables + 1; i++) {
                for (int j = 0; j < numRows; j++) {
                    data[i][j] = new Double(indata[i][j]);
                }
            }
            functions = "Multiply,Divide,Add,Subtract".split(",");
            variableNames = "F1,F2,F3,F4".split(",");
            presentation = "Fibonacci series";
        }
        System.out.println("Presentation: " + presentation);
        if (outputVariable == null) {
            outputVariable = numInputVariables;
        }
        if (variableNames == null) {
            variableNames = new String[numInputVariables + 1];
            for (int i = 0; i < numInputVariables + 1; i++) {
                variableNames[i] = "V" + i;
            }
        }
        System.out.println("output_variable: " + variableNames[outputVariable] + " (index: " + outputVariable + ")");
        GPConfiguration config = new GPConfiguration();
        config.setGPFitnessEvaluator(new DeltaGPFitnessEvaluator());
        config.setMaxInitDepth(maxInitDepth);
        config.setPopulationSize(populationSize);
        if (tournamentSelectorSize > 0) {
            config.setSelectionMethod(new TournamentSelector(tournamentSelectorSize));
        }
        config.setMaxCrossoverDepth(maxCrossoverDepth);
        config.setFitnessFunction(new SymbolicRegression.FormulaFitnessFunction());
        config.setStrictProgramCreation(false);
        config.setFunctionProb(functionProb);
        config.setReproductionProb(reproductionProb);
        config.setMutationProb(mutationProb);
        config.setDynamizeArityProb(dynamizeArityProb);
        config.setNewChromsPercent(newChromsPercent);
        config.setMinInitDepth(minInitDepth);
        config.setProgramCreationMaxTries(programCreationMaxTries);
        GPProblem problem = new SymbolicRegression(config);
        GPGenotype gp = problem.create();
        gp.setVerboseOutput(false);
        startTime = System.currentTimeMillis();
        System.out.println("Creating initial population");
        System.out.println("Mem free: " + SystemKit.niceMemory(SystemKit.getTotalMemoryMB()) + " MB");
        IGPProgram fittest = null;
        double bestFit = -1.0d;
        String bestProgram = "";
        int bestGen = 0;
        HashMap<String, Integer> similiar = null;
        if (showSimiliar) {
            similiar = new HashMap<String, Integer>();
        }
        for (int gen = 1; gen <= numEvolutions; gen++) {
            gp.evolve();
            gp.calcFitness();
            GPPopulation pop = gp.getGPPopulation();
            IGPProgram thisFittest = pop.determineFittestProgram();
            thisFittest.setApplicationData((Object) ("gen" + gen));
            ProgramChromosome chrom = thisFittest.getChromosome(0);
            String program = chrom.toStringNorm(0);
            double fitness = thisFittest.getFitnessValue();
            if (showSimiliar || showPopulation) {
                if (showPopulation) {
                    System.out.println("Generation " + gen + " (show whole population, sorted)");
                }
                pop.sortByFitness();
                for (IGPProgram p : pop.getGPPrograms()) {
                    double fit = p.getFitnessValue();
                    if (showSimiliar && fit <= bestFit) {
                        String prog = p.toStringNorm(0);
                        if (!similiar.containsKey(prog)) {
                            similiar.put(prog, 1);
                        } else {
                            similiar.put(prog, similiar.get(prog) + 1);
                        }
                    }
                    if (showPopulation) {
                        String prg = p.toStringNorm(0);
                        int sz = p.size();
                        System.out.println("\tprogram: " + prg + " fitness: " + fit);
                    }
                }
            }
            if (bestFit < 0.0d || fitness < bestFit) {
                bestGen = gen;
                myOutputSolution(thisFittest, gen);
                bestFit = fitness;
                bestProgram = program;
                fittest = thisFittest;
                if (showSimiliar) {
                    similiar.clear();
                }
            } else {
            }
        }
        System.out.println("\nAll time best (from generation " + bestGen + ")");
        myOutputSolution(fittest, numEvolutions);
        System.out.println("applicationData: " + fittest.getApplicationData());
        endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("\nTotal time " + elapsedTime + "ms");
        if (showSimiliar) {
            System.out.println("\nAll solutions with the best fitness (" + bestFit + "):");
            for (String p : similiar.keySet()) {
                System.out.println(p + " (" + similiar.get(p) + ")");
            }
        }
        System.exit(0);
    }

    /**
   * Fitness function for evaluating the produced fomulas, represented as GP
   * programs. The fitness is computed by calculating the result (Y) of the
   * function/formula for integer inputs 0 to 20 (X). The sum of the differences
   * between expected Y and actual Y is the fitness, the lower the better (as
   * it is a defect rate here).
   */
    public static class FormulaFitnessFunction extends GPFitnessFunction {

        protected double evaluate(final IGPProgram a_subject) {
            return computeRawFitness(a_subject);
        }

        public double computeRawFitness(final IGPProgram ind) {
            double error = 0.0f;
            Object[] noargs = new Object[0];
            for (int j = 0; j < numRows; j++) {
                int variableIndex = 0;
                for (int i = 0; i < numInputVariables + 1; i++) {
                    if (i != outputVariable) {
                        variables[variableIndex].set(data[i][j]);
                        variableIndex++;
                    }
                }
                try {
                    double result = ind.execute_double(0, noargs);
                    error += Math.abs(result - data[outputVariable][j]);
                    if (Double.isInfinite(error)) {
                        return Double.MAX_VALUE;
                    }
                } catch (ArithmeticException ex) {
                    System.out.println(ind);
                    throw ex;
                }
            }
            if (error <= bumpValue && bumpPerfect) {
                if (!foundPerfect) {
                    System.out.println("Found a perfect solution with err " + error + "!. Bump up the values!");
                    foundPerfect = true;
                }
                ProgramChromosome chrom = ind.getChromosome(0);
                String program = chrom.toStringNorm(0);
                if (!foundSolutions.containsKey(program)) {
                    System.out.println("PROGRAM:" + program + " error: " + error);
                    foundSolutions.put(program, 1);
                } else {
                    foundSolutions.put(program, foundSolutions.get(program) + 1);
                }
                error = 0.1d;
            }
            if (scaleError > 0.0d) {
                return error * scaleError;
            } else {
                return error;
            }
        }
    }

    /**
   * Outputs the best solution until now at standard output.
   *
   * This is stolen (and somewhat edited) from GPGenotype.outputSolution
   * which used log4j.
   *
   * @param a_best the fittest ProgramChromosome
   *
   * @author Hakan Kjellerstrand (originally by Klaus Meffert)
   */
    public static void myOutputSolution(IGPProgram a_best, int gen) {
        String freeMB = SystemKit.niceMemory(SystemKit.getFreeMemoryMB());
        System.out.println("Evolving generation " + (gen) + "/" + numEvolutions + ", memory free: " + freeMB + " MB");
        if (a_best == null) {
            System.out.println("No best solution (null)");
            return;
        }
        double bestValue = a_best.getFitnessValue();
        if (Double.isInfinite(bestValue)) {
            System.out.println("No best solution (infinite)");
            return;
        }
        System.out.println("Best solution fitness: " + NumberKit.niceDecimalNumber(bestValue, 2));
        System.out.println("Best solution: " + a_best.toStringNorm(0));
        String depths = "";
        int size = a_best.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                depths += " / ";
            }
            depths += a_best.getChromosome(i).getDepth(0);
        }
        if (size == 1) {
            System.out.println("Depth of chrom: " + depths);
        } else {
            System.out.println("Depths of chroms: " + depths);
        }
    }
}
