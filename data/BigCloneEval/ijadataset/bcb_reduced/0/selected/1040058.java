package glaceo.services;

import glaceo.data.GClub;
import glaceo.data.GMatch;
import glaceo.data.GMatchday;
import glaceo.data.GRoundRobinContestElement;
import glaceo.data.IGlaceoDao;
import java.util.List;
import java.util.Stack;

/**
 * Creates matches for a round robin style tournament.
 *
 * @version $Id$
 * @author jjanke
 */
public class GRoundRobinMatchCreator {

    private final IGlaceoDao d_dao;

    private GRoundRobinContestElement d_contestElement;

    private List<GClub> d_listClubs;

    /**
   * Initializes the match creator.
   *
   * @param dao the DAO to use for all database interactions.
   */
    public GRoundRobinMatchCreator(final IGlaceoDao dao) {
        d_dao = dao;
    }

    /**
   * Generates all matches for the given contest element according to its specification
   * (number of rounds and clubs etc.). The clubs participating in the matches must be
   * provided in a seperate list. This method does not check if there are already any
   * matches. So calling this method multiple times in a row for the same contest element
   * will result in the creation of unneccessary additional matches.
   *
   * @param contestElement the contest element for which to create matches
   * @param listClubs the clubs participating in the contest element
   * @return number of generated matches
   */
    public int generateMatches(final GRoundRobinContestElement contestElement, final List<GClub> listClubs) {
        d_contestElement = contestElement;
        d_listClubs = listClubs;
        int[][] matrix = createMatchDayMatrix();
        matrix = balanceMatchDayMatrix(matrix);
        matrix = checkHomeAwayMatchesUnevenTeamNumber(matrix);
        return createMatchdays(matrix);
    }

    /**
   * Creates a quadratic matrix. The indexes represent the teams of the league. The
   * contents says on which match day the two teams (i,j) play against each other. This
   * method fills the matrix.
   *
   * @return the filled match day matrix
   */
    private int[][] createMatchDayMatrix() {
        int nTeams = d_listClubs.size();
        if (nTeams % 2 != 0) {
            nTeams++;
        }
        int[][] matrix = new int[nTeams][nTeams];
        for (int i = 0; i < nTeams; i++) {
            for (int j = 0; j < nTeams; j++) {
                if (i == j) {
                    matrix[i][j] = -1;
                } else if (j == nTeams - 1) {
                    matrix[i][j] = (j + 2 * i);
                    while (matrix[i][j] >= nTeams) {
                        matrix[i][j] -= (nTeams - 1);
                    }
                } else {
                    if (j > i) {
                        matrix[i][j] = i + j;
                        while (matrix[i][j] >= nTeams) {
                            matrix[i][j] -= (nTeams - 1);
                        }
                    } else {
                        matrix[i][j] = matrix[j][i] + nTeams - 1;
                    }
                }
            }
        }
        return matrix;
    }

    /**
   * This methods balances the matrix in order to equally distribute home and away matches
   * over the season.
   *
   * @param matrix the initially filled match day matrix
   * @return the balanced match day matrix
   */
    private int[][] balanceMatchDayMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = i + 1; j < matrix.length; j++) {
                if (matrix[i][j] % 2 != 0) {
                    int tmp = matrix[i][j];
                    matrix[i][j] = matrix[j][i];
                    matrix[j][i] = tmp;
                }
            }
        }
        return matrix;
    }

    /**
   * <p>
   * If there is an uneven number of teams in the league, each team shall play an equal
   * number of matches at home and on the road per round. This method checks whether this
   * condition is respected and changes the match day allocation if necessary. This method
   * does not take care of the fact that a team should ideally play one game at home and
   * the next on the road etc. It just considers the total number of home and away games.
   * (it is assumed that the last index of the matrix represents the extra team which was
   * added because of the uneven total number of games).
   * </p>
   *
   * <p>
   * This method checks whether there is really an uneven number of teams, so no
   * precondition has to be respected prior to calling it.
   * </p>
   *
   * @param matrix a balanced match day matrix
   * @return matrix a balanced match day matrix in which all teams play an equal number of
   *         games at home and on the road per round if the total number of teams in the
   *         league is uneven
   */
    private int[][] checkHomeAwayMatchesUnevenTeamNumber(int[][] matrix) {
        int n = d_listClubs.size();
        if (n % 2 == 0) {
            return matrix;
        }
        Stack<Integer> tooManyHomeGames = new Stack<Integer>();
        Stack<Integer> notEnoughHomeGames = new Stack<Integer>();
        int[] numHomeGames = new int[n];
        int expectedHomeGames = (n - 1) / 2;
        for (int i = 0; i < n; i++) {
            notEnoughHomeGames.add(new Integer(i));
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                if (matrix[i][j] <= n) {
                    numHomeGames[i]++;
                    if (numHomeGames[i] == expectedHomeGames) {
                        notEnoughHomeGames.remove(new Integer(i));
                    } else if (numHomeGames[i] > expectedHomeGames) {
                        tooManyHomeGames.push(new Integer(i));
                    }
                }
            }
        }
        while (!tooManyHomeGames.isEmpty()) {
            int i = tooManyHomeGames.pop();
            int j = notEnoughHomeGames.pop();
            while (matrix[i][j] > n) {
                notEnoughHomeGames.insertElementAt(new Integer(j), 0);
                j = notEnoughHomeGames.pop();
            }
            int tmp = matrix[i][j];
            matrix[i][j] = matrix[j][i];
            matrix[j][i] = tmp;
        }
        return matrix;
    }

    /**
   * Analyses the games matrix and creates the complete schedule for the season.
   *
   * @param matchDao the DAO that is used to write generated matches to the database
   * @param matrix a balanced match day matrix
   * @return the number of generated matches
   */
    private synchronized int createMatchdays(int[][] matrix) {
        int numMatches = 0;
        int n = d_listClubs.size();
        int matchdaysPerRound = n - 1;
        if (n % 2 != 0) matchdaysPerRound = n;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                int md = matrix[i][j];
                int rnd = (md - 1) / matchdaysPerRound;
                int curRnd = 0;
                if (rnd == 1) {
                    md = md - matchdaysPerRound;
                }
                while (curRnd < d_contestElement.getNumRounds()) {
                    if (curRnd % 2 == rnd) {
                        int nMatchdayNumber = (curRnd * matchdaysPerRound) + md;
                        GMatchday matchday = d_contestElement.getMatchday(nMatchdayNumber);
                        assert matchday != null : "Matchday is null for contest element " + d_contestElement.getId() + " matchday no. " + nMatchdayNumber;
                        GMatch match = new GMatch(matchday, d_listClubs.get(i), d_listClubs.get(j));
                        d_dao.makePersistent(match);
                        numMatches++;
                    }
                    curRnd++;
                }
            }
        }
        return numMatches;
    }
}
