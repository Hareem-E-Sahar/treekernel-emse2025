package com.paintguy67.connect4;

import java.util.Random;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author paintguy67
 *
 */
public class C4Game {

    public int[] gameState;

    public boolean computerTurn;

    public boolean gameOver;

    private boolean firstMove;

    public final int COL_MAX = 6;

    public final int ROW_MAX = 5;

    public static final int NO_PIECE = 0;

    public static final int NO_WINYET = NO_PIECE;

    public static final int USER_PIECE = 1;

    public static final int USER_WIN = USER_PIECE;

    public static final int COMPUTER_PIECE = 2;

    public static final int COMPUTER_WIN = COMPUTER_PIECE;

    public static final int INVALID_MOVE = 3;

    public static final int TIE = 3;

    public final int MAX_LEVEL = 4;

    public int lastRow, lastCol;

    public int level = 2;

    public int whoWon = NO_WINYET;

    /**
	 * Constructor initializes the game
	 *
	 */
    public C4Game() {
        newGame(2, true);
    }

    /**
 	 * This method must be called to initialize the game when a new game is
	 * started
	 *
	 * @param lvl - the level of the game to start (0=easy, 1=medium, 2=hard)
	 */
    public void newGame(int lvl, boolean userFirst) {
        level = lvl;
        firstMove = false;
        gameState = new int[ROW_MAX + 1];
        computerTurn = false;
        gameOver = false;
        lastRow = lastCol = -1;
        computerTurn = !userFirst;
        for (int row = 0; row <= ROW_MAX; row++) {
            for (int col = 0; col <= COL_MAX; col++) {
                gameState[row] = NO_PIECE;
            }
        }
    }

    /**
	 * This method checks if the game is over (no more free spots)
	 * 
	 * @return true if the game is finished
	 */
    private boolean gameOver() {
        if (gameOver == true) return (true);
        int row = gameState[ROW_MAX];
        for (int col = 0; col <= COL_MAX; col++) {
            if ((row & 0x03) == 0) return (false);
            row = row >> 2;
        }
        gameOver = true;
        whoWon = TIE;
        return (true);
    }

    /**
	 * Performs the user move if it is valid
	 * 
	 * @param userCol column where the user wants to place a piece
	 * @return game status: TIE, INVALID_MOVE, USER_WIN, 
	 */
    public int userMove(int userCol) {
        if (gameOver()) {
            return (whoWon);
        }
        if (computerTurn) {
            return (INVALID_MOVE);
        }
        if (makeMove(USER_PIECE, userCol, gameState) == -1) {
            return (INVALID_MOVE);
        }
        computerTurn = true;
        if (checkForWin(USER_PIECE, gameState) == USER_WIN) {
            gameOver = true;
            return (USER_WIN);
        }
        return (gameOver() ? TIE : NO_WINYET);
    }

    public int getPieceAt(int row, int col) {
        return ((gameState[row] & (0x03 << (col * 2))) >> (col * 2));
    }

    /**
	 * Performs a move for a given player in a given column. Finds the lowest
	 * row to put the piece in.
	 * 
	 * @param player
	 * @param col
	 * @param game
	 * @return
	 */
    private int makeMove(int player, int col, int[] game) {
        int bcol = 0x03 << (col * 2);
        if ((game[ROW_MAX] & bcol) != NO_PIECE) return -1;
        int row;
        for (row = 0; row <= ROW_MAX; row++) {
            if ((game[row] & bcol) == NO_PIECE) {
                game[row] |= player << (col * 2);
                break;
            }
        }
        return row;
    }

    public boolean validMove(int column) {
        if ((gameState[ROW_MAX] & (0x03 << (2 * column))) != 0) return false; else return true;
    }

    /**
	 * Attempt at much more efficient checkForWin implementation
	 * 
	 * @param player
	 * @param game
	 * @return
	 */
    public int checkForWin(int player, int[] game) {
        int ret = NO_WINYET;
        for (int row = 0; row <= ROW_MAX; row++) {
            int mask = (player == USER_PIECE ? 0x55 : 0xAA);
            int rData = game[row];
            for (int cnt = 0; cnt < 4; cnt++) {
                if ((rData & mask) == mask) return (player);
                rData = rData >> 2;
            }
        }
        for (int col = 0; col <= COL_MAX; col++) {
            int mask = player << (col * 2);
            for (int cnt = 0; cnt < 3; cnt++) {
                if ((game[cnt] & mask) == mask && (game[cnt + 1] & mask) == mask && (game[cnt + 2] & mask) == mask && (game[cnt + 3] & mask) == mask) return (player);
            }
        }
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 3; row++) {
                int mask = player << 2 * col;
                if ((game[row] & mask) == mask) {
                    mask = mask << 2;
                    if ((game[row + 1] & mask) == mask) {
                        mask = mask << 2;
                        if ((game[row + 2] & mask) == mask) {
                            mask = mask << 2;
                            if ((game[row + 3] & mask) == mask) {
                                return (player);
                            }
                        }
                    }
                }
            }
        }
        for (int col = 0; col < 4; col++) {
            for (int row = 3; row < 6; row++) {
                int mask = player << 2 * col;
                if ((game[row] & mask) == mask) {
                    mask = mask << 2;
                    if ((game[row - 1] & mask) == mask) {
                        mask = mask << 2;
                        if ((game[row - 2] & mask) == mask) {
                            mask = mask << 2;
                            if ((game[row - 3] & mask) == mask) {
                                return (player);
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
	 * Removes the top piece from the given column.
	 * 
	 * @param col
	 * @param game
	 * @return
	 */
    private int removePiece(int col, int[] game) {
        if (((game[0] >> (col * 2)) & 0x03) == NO_PIECE) return -1;
        int row;
        for (row = ROW_MAX; row >= 0; row--) {
            if (((game[row] >> (col * 2)) & 0x03) != NO_PIECE) {
                game[row] &= ~(0x03 << (col * 2));
                break;
            }
        }
        return row;
    }

    /**
	 * Makes a copy of the given board and returns it.
	 * 
	 * @param board
	 * @return
	 */
    private int[] copyBoard(int[] board) {
        int[] newBoard = new int[ROW_MAX + 1];
        for (int row = 0; row <= ROW_MAX; row++) {
            newBoard[row] = board[row];
        }
        return (newBoard);
    }

    /**
	 * Calculates the user's chances of winning by making the given move.
	 * 
	 * @param move
	 * @param board
	 * @param level
	 * @return
	 */
    private int calculateUserChances(int move, int[] board, int level) {
        if (makeMove(USER_PIECE, move, board) == -1) {
            return (-1);
        }
        if (checkForWin(USER_PIECE, board) == USER_WIN) {
            return (-(10000 / (MAX_LEVEL + 1 - level)) + 1);
        }
        if (level <= 1) {
            return 0;
        }
        int score = 0;
        int games = COL_MAX + 1;
        for (int col = 0; col <= COL_MAX; col++) {
            int[] testBoard = copyBoard(board);
            int cScore = calculateComputerChances(col, testBoard, level - 1);
            if (cScore != -1) score += cScore; else games--;
        }
        if (games > 0) return (score / games); else return (score);
    }

    /**
	 * Calculates the computer's chances of winning by making the given move.
	 * 
	 * @param move
	 * @param board
	 * @param level
	 * @return
	 */
    private int calculateComputerChances(int move, int[] board, int level) {
        if (makeMove(COMPUTER_PIECE, move, board) == -1) {
            return (-1);
        }
        if (checkForWin(COMPUTER_PIECE, board) == COMPUTER_WIN) {
            return (10000 / (MAX_LEVEL + 1 - level));
        }
        if (level <= 1) {
            return 0;
        }
        int score = 0;
        int games = COL_MAX + 1;
        for (int col = 0; col <= COL_MAX; col++) {
            int[] testBoard = copyBoard(board);
            int cScore = calculateUserChances(col, testBoard, level);
            if (cScore != -1) score += cScore; else games--;
        }
        if (games > 0) return (score / games); else return (score);
    }

    /**
	 * Performs the computer move by doing the look ahead n-levels
	 * 
	 * @return
	 */
    public int computerMove(IProgressMonitor progMonitor) {
        if (gameOver()) {
            return (whoWon);
        }
        if (!computerTurn) return (INVALID_MOVE);
        int maxScore = -322000;
        int bestPlay = -1;
        if (!firstMove && level != 0) {
            try {
                for (int col = 0; col <= COL_MAX; col++) {
                    if (validMove(col)) {
                        int[] testBoard = copyBoard(gameState);
                        int score = 0;
                        score = calculateComputerChances(col, testBoard, level == 1 ? 2 : MAX_LEVEL);
                        progMonitor.worked(col);
                        if (score > maxScore && makeMove(COMPUTER_PIECE, col, gameState) != -1) {
                            removePiece(col, gameState);
                            maxScore = score;
                            bestPlay = col;
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("Problem:" + ex);
            }
        } else {
            firstMove = false;
        }
        int row, col;
        if (bestPlay != -1) {
            col = bestPlay;
            row = makeMove(COMPUTER_PIECE, bestPlay, gameState);
        } else {
            Random rand = new Random();
            col = rand.nextInt(COL_MAX + 1);
            while ((row = makeMove(COMPUTER_PIECE, col, gameState)) == -1) {
                col = rand.nextInt(COL_MAX + 1);
            }
        }
        lastRow = row;
        lastCol = col;
        if (checkForWin(COMPUTER_PIECE, gameState) == COMPUTER_WIN) {
            gameOver = true;
            whoWon = COMPUTER_WIN;
            return (COMPUTER_WIN);
        }
        computerTurn = false;
        if (gameOver()) return (whoWon); else return (NO_WINYET);
    }
}
