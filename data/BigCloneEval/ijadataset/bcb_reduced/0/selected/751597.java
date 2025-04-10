package org.jbubblebreaker;

import java.io.Serializable;

/**
 * Stores statistic informations
 * @author Sven Strickroth
 */
@SuppressWarnings("serial")
public class StatisticData implements Serializable {

    private String mode;

    private int colors = 0;

    private int rows = 0;

    private int cols = 0;

    private int countOfGames = 0;

    private int maxPoints = 0;

    private int averagePoints = 0;

    public StatisticData(String mode, int colors, int rows, int cols, int points) {
        this.mode = mode;
        this.colors = colors;
        this.rows = rows;
        this.cols = cols;
        countOfGames = 1;
        maxPoints = points;
        averagePoints = points;
    }

    /**
	 * @param points
	 */
    public void newGame(int points) {
        if (points > maxPoints) {
            maxPoints = points;
        }
        countOfGames++;
        averagePoints = (averagePoints + points) / 2;
    }

    /**
	 * @return the countOfGames
	 */
    public int getCountOfGames() {
        return countOfGames;
    }

    /**
	 * @param countOfGames the countOfGames to set
	 */
    public void setCountOfGames(int countOfGames) {
        this.countOfGames = countOfGames;
    }

    /**
	 * @return the maxPoints
	 */
    public int getMaxPoints() {
        return maxPoints;
    }

    /**
	 * @param maxPoints the maxPoints to set
	 */
    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    /**
	 * @return the averagePoints
	 */
    public int getAveragePoints() {
        return averagePoints;
    }

    /**
	 * @return the mode
	 */
    public String getMode() {
        return mode;
    }

    /**
	 * @return the colors
	 */
    public int getColors() {
        return colors;
    }

    /**
	 * @return the cols
	 */
    public int getCols() {
        return cols;
    }

    /**
	 * @return the rows
	 */
    public int getRows() {
        return rows;
    }
}
