package examples.gp;

import org.jgap.*;
import org.jgap.event.*;
import org.jgap.gp.*;
import org.jgap.gp.impl.*;
import org.jgap.gp.function.*;
import org.jgap.gp.terminal.*;
import org.jgap.util.*;

/**
 * Example demonstrating Genetic Programming (GP) capabilities of JGAP.<p>
 * Here, the Fibonacci sequence is calculated (only integers are used).<p>
 * Please note: We try to find a program that computes Fibonacci iteratively.<p>
 * This example utilizes a INodeValidator (see FibonacciNodeValidator).<p>
 * Each new best solution found will be displayed as a graphical tree
 * representing the GP. The tree is written to a PNG-imagefile onto harddisk.
 *
 * @author Klaus Meffert
 * @since 3.0
 */
public class Fibonacci extends GPProblem {

    /** String containing the CVS revision. Read out via reflection!*/
    private static final String CVS_REVISION = "$Revision: 1.28 $";

    static Variable vx;

    static Variable va;

    private static final int NUMFIB = 10;

    static Integer[] x = new Integer[NUMFIB];

    static int[] y = new int[NUMFIB];

    public Fibonacci(GPConfiguration a_conf) throws InvalidConfigurationException {
        super(a_conf);
    }

    /**
   * Sets up the functions to use and other parameters. Then creates the
   * initial genotype.
   *
   * @return the genotype created
   * @throws InvalidConfigurationException
   *
   * @author Klaus Meffert
   * @since 3.0
   */
    public GPGenotype create() throws InvalidConfigurationException {
        Class[] types = { CommandGene.VoidClass, CommandGene.VoidClass, CommandGene.IntegerClass };
        Class[][] argTypes = { {}, {}, {} };
        int[] minDepths = new int[] { 2, 3, 1 };
        int[] maxDepths = new int[] { 2, 9, 1 };
        GPConfiguration conf = getGPConfiguration();
        CommandGene[][] nodeSets = { { new SubProgram(conf, new Class[] { CommandGene.VoidClass, CommandGene.VoidClass }), new StoreTerminal(conf, "mem0", CommandGene.IntegerClass), new StoreTerminal(conf, "mem1", CommandGene.IntegerClass), new Increment(conf, CommandGene.IntegerClass), new NOP(conf), new Terminal(conf, CommandGene.IntegerClass, 0.0, 10.0) }, { vx = Variable.create(conf, "X", CommandGene.IntegerClass), new AddAndStore(conf, CommandGene.IntegerClass, "mem2"), new ForLoop(conf, CommandGene.IntegerClass, 1, NUMFIB), new Increment(conf, CommandGene.IntegerClass, -1), new TransferMemory(conf, "mem2", "mem1"), new TransferMemory(conf, "mem1", "mem0"), new ReadTerminal(conf, CommandGene.IntegerClass, "mem0"), new ReadTerminal(conf, CommandGene.IntegerClass, "mem1"), new SubProgram(conf, new Class[] { CommandGene.VoidClass, CommandGene.VoidClass, CommandGene.VoidClass }) }, {} };
        nodeSets[2] = CommandFactory.createReadOnlyCommands(nodeSets[2], conf, CommandGene.IntegerClass, "mem", 1, 2, !true);
        for (int i = 0; i < NUMFIB; i++) {
            int index = i;
            x[i] = new Integer(index);
            y[i] = fib_iter(index);
            System.out.println(i + ") " + x[i] + "   " + y[i]);
        }
        return GPGenotype.randomInitialGenotype(conf, types, argTypes, nodeSets, minDepths, maxDepths, 10, new boolean[] { !true, !true, false }, true);
    }

    private static int fib_iter(int a_index) {
        if (a_index == 0 || a_index == 1) {
            return 1;
        }
        int a = 1;
        int b = 1;
        int x = 0;
        for (int i = 2; i <= a_index; i++) {
            x = a + b;
            a = b;
            b = x;
        }
        return x;
    }

    private int fib_array(int a_index) {
        if (a_index == 0 || a_index == 1) {
            return 1;
        }
        int[] numbers = new int[a_index + 1];
        numbers[0] = numbers[1] = 1;
        for (int i = 2; i <= a_index; i++) {
            numbers[i] = numbers[i - 1] + numbers[i - 2];
        }
        return numbers[a_index];
    }

    private static int fib(int a_index) {
        if (a_index == 0 || a_index == 1) {
            return 1;
        }
        return fib(a_index - 1) + fib(a_index - 2);
    }

    /**
   * Starts the example.
   *
   * @param args ignored
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 3.0
   */
    public static void main(String[] args) {
        try {
            System.out.println("Program to discover: Fibonacci(x)");
            GPConfiguration config = new GPConfiguration();
            config.setGPFitnessEvaluator(new DeltaGPFitnessEvaluator());
            config.setSelectionMethod(new TournamentSelector(4));
            int popSize;
            if (args.length == 1) {
                popSize = Integer.parseInt(args[0]);
            } else {
                popSize = 600;
            }
            System.out.println("Using population size of " + popSize);
            config.setMaxInitDepth(6);
            config.setPopulationSize(popSize);
            config.setFitnessFunction(new Fibonacci.FormulaFitnessFunction());
            config.setStrictProgramCreation(false);
            config.setProgramCreationMaxTries(3);
            config.setMaxCrossoverDepth(5);
            config.setNodeValidator(new FibonacciNodeValidator());
            config.setUseProgramCache(true);
            final GPProblem problem = new Fibonacci(config);
            GPGenotype gp = problem.create();
            gp.setVerboseOutput(true);
            final Thread t = new Thread(gp);
            config.getEventManager().addEventListener(GeneticEvent.GPGENOTYPE_EVOLVED_EVENT, new GeneticEventListener() {

                public void geneticEventFired(GeneticEvent a_firedEvent) {
                    GPGenotype genotype = (GPGenotype) a_firedEvent.getSource();
                    int evno = genotype.getGPConfiguration().getGenerationNr();
                    double freeMem = SystemKit.getFreeMemoryMB();
                    if (evno % 50 == 0) {
                        double allBestFitness = genotype.getAllTimeBest().getFitnessValue();
                        System.out.println("Evolving generation " + evno + ", all-time-best fitness: " + allBestFitness + ", memory free: " + freeMem + " MB");
                    }
                    if (evno > 3000) {
                        t.stop();
                    } else {
                        try {
                            if (freeMem < 50) {
                                System.gc();
                                t.sleep(500);
                            } else {
                                t.sleep(30);
                            }
                        } catch (InterruptedException iex) {
                            iex.printStackTrace();
                            System.exit(1);
                        }
                    }
                }
            });
            config.getEventManager().addEventListener(GeneticEvent.GPGENOTYPE_NEW_BEST_SOLUTION, new GeneticEventListener() {

                /**
         * New best solution found.
         *
         * @param a_firedEvent GeneticEvent
         */
                public void geneticEventFired(GeneticEvent a_firedEvent) {
                    GPGenotype genotype = (GPGenotype) a_firedEvent.getSource();
                    int evno = genotype.getGPConfiguration().getGenerationNr();
                    String indexString = "" + evno;
                    while (indexString.length() < 5) {
                        indexString = "0" + indexString;
                    }
                    String filename = "fibonacci_best" + indexString + ".png";
                    IGPProgram best = genotype.getAllTimeBest();
                    try {
                        problem.showTree(best, filename);
                    } catch (InvalidConfigurationException iex) {
                        iex.printStackTrace();
                    }
                    double bestFitness = genotype.getFittestProgram().getFitnessValue();
                    if (bestFitness < 0.001) {
                        genotype.outputSolution(best);
                        t.stop();
                        System.exit(0);
                    }
                }
            });
            t.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static class FormulaFitnessFunction extends GPFitnessFunction {

        protected double evaluate(final IGPProgram a_subject) {
            return computeRawFitness(a_subject);
        }

        public double computeRawFitness(final IGPProgram a_program) {
            double error = 0.0f;
            Object[] noargs = new Object[0];
            a_program.getGPConfiguration().clearStack();
            a_program.getGPConfiguration().clearMemory();
            for (int i = 2; i < NUMFIB; i++) {
                for (int j = 0; j < a_program.size(); j++) {
                    vx.set(x[i]);
                    try {
                        try {
                            if (j == a_program.size() - 1) {
                                double result = a_program.execute_int(j, noargs);
                                error += Math.abs(result - y[i]);
                            } else {
                                a_program.execute_void(j, noargs);
                            }
                        } catch (IllegalStateException iex) {
                            error = GPFitnessFunction.MAX_FITNESS_VALUE;
                            break;
                        }
                    } catch (ArithmeticException ex) {
                        System.out.println("x = " + x[i].intValue());
                        System.out.println(a_program.getChromosome(j));
                        throw ex;
                    }
                }
            }
            if (a_program.getGPConfiguration().stackSize() > 0) {
                error = GPFitnessFunction.MAX_FITNESS_VALUE;
            }
            if (error < 0.000001) {
                error = 0.0d;
            } else if (error < GPFitnessFunction.MAX_FITNESS_VALUE) {
            }
            return error;
        }
    }
}
