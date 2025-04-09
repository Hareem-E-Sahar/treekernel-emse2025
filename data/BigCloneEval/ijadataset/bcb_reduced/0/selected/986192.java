package net.virtualhockey.tools.matchcreator;

import java.util.List;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import net.virtualhockey.data.Club;
import net.virtualhockey.data.Competition;
import net.virtualhockey.data.Match;
import net.virtualhockey.data.Season;
import net.virtualhockey.data.helper.CompetitionSubType;
import net.virtualhockey.infrastructure.HibernateUtil;
import net.virtualhockey.tools.VHToolsException;

/**
 * Creates matches for a round robin style tournament. This class is intentionally kept
 * package visible since it shall not be used from other classes than those ones
 * implementing the {@link net.virtualhockey.tools.matchcreator.MatchCreator} interface
 * and residing in the same package.
 * 
 * @version $Id: RoundRobinMatchGenerator.java 81 2005-09-25 14:35:31Z jankejan $
 * @author jankejan
 */
public class RoundRobinMatchGenerator {

    public static final String CLASS_REVISION = "$Id: RoundRobinMatchGenerator.java 81 2005-09-25 14:35:31Z jankejan $";

    private static Log s_log = LogFactory.getLog(RoundRobinMatchGenerator.class);

    private Season d_season;

    private Competition d_competition;

    private CompetitionSubType d_compSubType;

    private int d_numRounds;

    private List<Club> d_clubs;

    /**
   * Initializes the RoundRobinMatchGenerator.
   * 
   * @param season the season for which the matches are generated
   * @param competition the competition for which the matches are generated
   * @param compSubType the competition phase the matches are intended for
   * @param clubs The clubs that participate in the round robin style tournament. The
   *        clubs that play against one another are inferred from this vector and not
   *        determined automatically from the competition. So you must make sure to pass
   *        the right (qualified) clubs to this constructor.
   * @param numRounds indicates how often each team plays against each other team from the
   *        same competition
   */
    RoundRobinMatchGenerator(Season season, Competition competition, CompetitionSubType compSubType, List<Club> clubs, int numRounds) {
        d_season = season;
        d_competition = competition;
        d_compSubType = compSubType;
        d_numRounds = numRounds;
        d_clubs = clubs;
    }

    /**
   * Generates the matches according to the data supplied to the constructor. The
   * generated matches are also written to the database.
   * 
   * @return the number of generated matches
   */
    int generateMatches() {
        int numMatches = 0;
        Session hibernateSession = HibernateUtil.getSession();
        HibernateUtil.beginTransaction();
        int[][] matrix = createMatchDayMatrix();
        matrix = balanceMatchDayMatrix(matrix);
        matrix = checkHomeAwayMatchesUnevenTeamNumber(matrix);
        try {
            numMatches = createMatchDays(hibernateSession, matrix);
            HibernateUtil.commitTransaction();
        } catch (VHToolsException ex) {
            HibernateUtil.rollbackTransaction();
            s_log.error("Match generation aborted with an error.", ex);
        }
        return numMatches;
    }

    /**
   * Creates a quadratic matrix. The indexes represent the teams of the league. The
   * contents says on which match day the two teams (i,j) play against each other. This
   * method fills the matrix.
   * 
   * @return the filled match day matrix
   */
    private int[][] createMatchDayMatrix() {
        int nTeams = d_clubs.size();
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
        int n = d_clubs.size();
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
                notEnoughHomeGames.add(new Integer(j));
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
   * @param hibernateSession the Hibernate session which is used to persist newly created
   *        matches
   * @param matrix a balanced match day matrix
   * @return the number of generated matches
   * @throws VHTagException if the saving of a match to the database fails
   */
    private synchronized int createMatchDays(Session hibernateSession, int[][] matrix) throws VHToolsException {
        int numMatches = 0;
        Club[] clubs = d_clubs.toArray(new Club[0]);
        int n = d_clubs.size();
        int matchdaysPerRound = n - 1;
        if (n % 2 != 0) {
            n--;
            matchdaysPerRound = n;
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                int md = matrix[i][j];
                int rnd = (md - 1) / matchdaysPerRound;
                int curRnd = 0;
                if (rnd == 1) {
                    md = md - matchdaysPerRound;
                }
                while (curRnd < d_numRounds) {
                    if (curRnd % 2 == rnd) {
                        int mdNumber = (curRnd * matchdaysPerRound) + md;
                        Match match = new Match(d_season, d_competition, d_compSubType, mdNumber, 0, clubs[i], clubs[j]);
                        try {
                            hibernateSession.save(match);
                        } catch (HibernateException ex) {
                            throw new VHToolsException("Impossible to save generated match.", ex);
                        }
                        numMatches++;
                    }
                    curRnd++;
                }
            }
        }
        return numMatches;
    }

    /**
   * Calculates the total number of match days to be played in a round robin competition.
   * 
   * @param numTeams the number of teams in the competition
   * @param numRounds the number of rounds to be played (how often plays each club against
   *        each of the other clubs that participate in the tournament)
   * @return the number of match days
   */
    public static int getNumberOfMatchDays(int numTeams, int numRounds) {
        if (numTeams % 2 == 0) {
            return (numTeams - 1) * numRounds;
        } else {
            return numTeams * numRounds;
        }
    }

    /**
   * Returns the number of matches that are played on a single match day.
   * 
   * @return the number of matches per match day
   */
    private int getNumberOfMatchesPerMatchDay() {
        int numTeams = d_clubs.size();
        if (numTeams % 2 == 0) {
            return numTeams / 2;
        } else {
            return (numTeams - 1) / 2;
        }
    }
}
