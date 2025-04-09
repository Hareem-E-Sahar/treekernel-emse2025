package crossword;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import concrete.CspOM;
import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.exception.FailedGenerationException;
import cspom.Problem;
import cspom.extension.Extension;
import cspom.extension.ExtensionConstraint;
import cspom.variable.Domain;
import cspom.variable.IntervalDomain;
import cspom.variable.Variable;

public class Crossword {

    private static Set<String> getDict(final String file, final int length) throws IOException {
        final Set<String> dict = new HashSet<String>();
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            final String word = Normalizer.normalize(line.toUpperCase(), Normalizer.Form.NFD).replaceAll("[^A-Z]", "");
            if (word.length() == length) {
                dict.add(word);
            }
        }
        return dict;
    }

    public static <E> E[][] transpose(final E[][] matrix, final E[][] transposed) {
        for (int i = matrix.length; --i >= 0; ) {
            for (int j = matrix[i].length; --j >= 0; ) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

    /**
	 * @param args
	 * @throws FailedGenerationException
	 * @throws IOException
	 */
    public static void main(String[] args) throws FailedGenerationException, IOException {
        Logger.getLogger("").getHandlers()[0].setLevel(Level.FINE);
        final int x = Integer.valueOf(args[0]);
        final int y = Integer.valueOf(args[1]);
        final Problem problem = new Problem("Crossword-" + x + "x" + y);
        final Variable[][] variables = new Variable[x][y];
        final Domain domain = new IntervalDomain("letters", 0, 25);
        for (int i = x; --i >= 0; ) {
            for (int j = y; --j >= 0; ) {
                problem.addVariable(variables[i][j] = new Variable(("x" + i) + j, domain));
            }
        }
        final List<Number[]> tuples = new ArrayList<Number[]>();
        for (String s : getDict("crossword/french", x)) {
            Number[] tuple = new Number[s.length()];
            for (int i = s.length(); --i >= 0; ) {
                tuple[i] = s.charAt(i) - 65;
            }
            tuples.add(tuple);
        }
        final Extension relX = new Extension("dict-" + x, x, tuples.size(), true, tuples.toArray(new Number[tuples.size()][]));
        tuples.clear();
        for (String s : getDict("crossword/french", y)) {
            Number[] tuple = new Number[s.length()];
            for (int i = s.length(); --i >= 0; ) {
                tuple[i] = s.charAt(i) - 65;
            }
            tuples.add(tuple);
        }
        final Extension relY = new Extension("dict-" + y, y, tuples.size(), true, tuples.toArray(new Number[tuples.size()][]));
        for (Variable[] v : variables) {
            problem.addConstraint(new ExtensionConstraint("", Arrays.asList(v), relY));
        }
        for (Variable[] v : transpose(variables, new Variable[y][x])) {
            problem.addConstraint(new ExtensionConstraint("", Arrays.asList(v), relX));
        }
        final cspfj.problem.Problem cspfjProblem = cspfj.problem.Problem.load(new CspOM(problem, 0));
        final Solver solver = new MGACIter(cspfjProblem, new ResultDisplayer(x, y, cspfjProblem.getVariables()));
        if (!solver.runSolver()) {
            System.out.println("No crossword found");
        }
    }
}
