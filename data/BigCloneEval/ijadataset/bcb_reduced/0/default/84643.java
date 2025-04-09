import java.io.*;
import java.util.*;

/**
 * SoDoKu
 */
public class SoDoKu {

    public static void main(String[] args) {
        System.out.println("Wecome to SoDoKu Solver 0.1");
        try {
            BufferedReader br = new BufferedReader(new FileReader("sodoku.txt"));
            PrintWriter outfile = new PrintWriter(new FileWriter("result.txt"));
            String line = br.readLine();
            while (line != null) {
                int puzzleSize = 0;
                int boxSize = 0;
                String strFEN = "";
                StringTokenizer st = new StringTokenizer(line);
                if (st.hasMoreTokens()) {
                    puzzleSize = Integer.valueOf(st.nextToken());
                    boxSize = Integer.valueOf(st.nextToken());
                    strFEN = st.nextToken();
                }
                SoDoKuPuzzle puzzle = new SoDoKuPuzzle(puzzleSize, boxSize);
                ConstraintSet cs = new ConstraintSet(puzzle.getNumSquares());
                GenerateConstraints(puzzle, cs);
                boolean doArc = false;
                SolvePuzzle(outfile, puzzle, cs, doArc, Search.SearchAlg.cspBT, strFEN);
                doArc = true;
                line = br.readLine();
            }
            outfile.close();
            br.close();
        } catch (IOException ioe) {
            System.out.println("IO error reading input file.");
            System.exit(1);
        }
    }

    static void GenerateConstraints(SoDoKuPuzzle puzzle, ConstraintSet cs) {
        for (int x = 0; x < puzzle.getNumSquares(); ++x) {
            for (int col = puzzle.Col(x) + 1; col < puzzle.getPuzzleSize(); ++col) {
                cs.addNotEqual(x, puzzle.Sqr(col, puzzle.Row(x)));
            }
            for (int row = puzzle.Row(x) + 1; row < puzzle.getPuzzleSize(); ++row) {
                cs.addNotEqual(x, puzzle.Sqr(puzzle.Col(x), row));
            }
            int rowFirst = puzzle.Row(x);
            int rowLast = puzzle.Row(x) - (puzzle.Row(x) % puzzle.getBoxSize()) + puzzle.getBoxSize();
            int colFirst = puzzle.Col(x) + 1;
            int colLast = puzzle.Col(x) - (puzzle.Col(x) % puzzle.getBoxSize()) + puzzle.getBoxSize();
            for (int row = rowFirst; row < rowLast; ++row) {
                for (int col = colFirst; col < colLast; ++col) {
                    cs.addNotEqual(x, puzzle.Sqr(col, row));
                }
                colFirst = puzzle.Col(x) - puzzle.Col(x) % puzzle.getBoxSize();
                colLast = colFirst + puzzle.getBoxSize();
            }
        }
    }

    static void GenerateDomains(SoDoKuPuzzle puzzle, Domain domain[]) {
        for (int i = 0; i < puzzle.getNumSquares(); ++i) {
            domain[i] = new Domain();
            if (puzzle.getSqr(i) != 0) {
                domain[i].add(puzzle.getSqr(i));
            } else {
                for (int val = 1; val <= puzzle.getPuzzleSize(); ++val) {
                    domain[i].add(val);
                }
            }
        }
    }

    static void SolvePuzzle(PrintWriter os, SoDoKuPuzzle puzzle, ConstraintSet cs, boolean doArcConsist, Search.SearchAlg cspAlg, String strFEN) {
        if (!puzzle.setPuzzle(strFEN)) {
            System.out.print("Error setting puzzle '");
            System.out.print(strFEN);
            System.out.println("'");
        } else {
            int assignment[] = new int[ConstraintSet.MAX_NUM_VARIABLES];
            for (int i = 0; i < assignment.length; ++i) {
                assignment[i] = 0;
            }
            Domain[] domain = new Domain[ConstraintSet.MAX_NUM_VARIABLES];
            GenerateDomains(puzzle, domain);
            if (doArcConsist) {
                AC3(cs, domain);
            }
            Search search = new Search();
            SearchStatistic stat = new SearchStatistic();
            stat.solved = false;
            stat.nodes = 0;
            stat.checks = 0;
            stat.sec = 0;
            PrintWriter cout = new PrintWriter(System.out);
            cout.write("======================================================================================\n");
            puzzle.display(cout);
            os.write("======================================================================================\n");
            puzzle.display(os);
            search.setSearchMethod(cspAlg);
            search.solve(cs, domain, assignment, stat);
            if (stat.solved) {
                for (int i = 0; i < cs.getNumberOfVariables(); i++) {
                    puzzle.set(i, assignment[i]);
                }
            }
            cout.print("Alg: ");
            cout.print(cspAlg.toString());
            cout.print(" Solved: ");
            cout.print(stat.solved);
            cout.print("  Expanded: ");
            cout.print(stat.nodes);
            cout.print("  Checks: ");
            cout.print(stat.checks);
            cout.print("  Sec: ");
            cout.println(stat.sec);
            cout.print("Sol: ");
            for (int i = 0; i < cs.getNumberOfVariables() && stat.solved; i++) {
                cout.print(assignment[i]);
            }
            cout.print("\n");
            if (stat.solved) {
                puzzle.display(cout);
            }
            cout.flush();
            os.print("Alg: ");
            os.print(cspAlg.toString());
            os.print(" Solved: ");
            os.print(stat.solved);
            os.print("  Expanded: ");
            os.print(stat.nodes);
            os.print("  Checks: ");
            os.print(stat.checks);
            os.print("  Sec: ");
            os.println(stat.sec);
            os.print("Sol: ");
            for (int i = 0; i < cs.getNumberOfVariables() && stat.solved; i++) {
                os.print(assignment[i]);
            }
            os.print("\n");
            if (stat.solved) {
                puzzle.display(os);
            }
            os.flush();
        }
    }

    static void AC3(ConstraintSet cs, Domain domain[]) {
    }
}
