package edu.pdx.cs.cs542s07.minichess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.Random;

/**
 * This class contains all the logic necessary to calculate 
 * the moves available to a given side for a given board state.
 * @author Michael C Smith <maxomai at gmail dot com>
 *
 */
class MoveFactory {

    ArrayList<Move> kingCapturingMoves = new ArrayList<Move>();

    ArrayList<Move> capturingMoves = new ArrayList<Move>();

    ArrayList<Move> nonCapturingMoves = new ArrayList<Move>();

    /**
	 * Generates a list of valid moves for this turn. The list is organized so that winning moves are at the
	 * head of the list, followed by queen-capturing moves, other-piece-capturing moves, pawn-capturing moves,
	 * and then moves that capture nothing. This is done in order to give the alpha-beta logic a head start.
	 * 
	 * @param board			The current board
	 * @param movablePieces	A hashmap of all movable pieces on the board (that is, those pieces belonging to the 
	 * 						player whose turn it is.)
	 * @return				A list of valid moves for this turn
	 * @throws				PawnNotPromotedException
	 */
    private ArrayList<Move> generateMoves(Board boardState) {
        clearMoveArrays();
        HashMap<Piece, Square> movablePieces;
        if (boardState.getTurn() == Color.WHITE) {
            movablePieces = boardState.getWhitePieces();
        } else {
            movablePieces = boardState.getBlackPieces();
        }
        Set<Piece> movablePieceSet = movablePieces.keySet();
        for (Piece p : movablePieceSet) {
            Square position = movablePieces.get(p);
            generateMovesForPieceHelper(p, position, boardState.getBoard());
        }
        sortCapturingMoves();
        shuffle(this.nonCapturingMoves);
        return consolidateMoveArrays();
    }

    /**
	 * Generates a list of scored moves, in order of ascending score. 
	 * @param boardState
	 * @return A list of moves according to score.
	 */
    ArrayList<Move> generateScoredMoves(Board boardState) {
        clearMoveArrays();
        ScoreFactory fac = new ScoreFactory();
        HashMap<Piece, Square> movablePieces;
        if (boardState.getTurn() == Color.WHITE) {
            movablePieces = boardState.getWhitePieces();
        } else {
            movablePieces = boardState.getBlackPieces();
        }
        Set<Piece> movablePieceSet = movablePieces.keySet();
        for (Piece p : movablePieceSet) {
            Square position = movablePieces.get(p);
            generateMovesForPieceHelper(p, position, boardState.getBoard());
        }
        int currScore = boardState.getScore();
        for (Move m : this.nonCapturingMoves) {
            m.setScore(currScore);
        }
        for (Move m : this.kingCapturingMoves) {
            m.setScore(ScoreFactory.YOU_WIN);
        }
        for (Move m : this.capturingMoves) {
            try {
                boardState.move(m);
                int score = boardState.getScore();
                if (boardState.getTurn() == Color.BLACK) m.setScore(-score); else m.setScore(score);
                boardState.undo();
            } catch (IllegalMoveException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        sortCapturingMoves();
        return consolidateMoveArrays();
    }

    /**
	 * Generates a list of valid moves for a single piece. The list is organized so that winning moves are at 
	 * the head of the list, followed by queen-capturing moves, other-piece-capturing moves, pawn-capturing moves,
	 * and then moves that capture nothing. This is done in order to give the alpha-beta logic a head start.
	 * 
	 * @param board			The current board
	 * @param allPieces		A hashmap of all pieces on the board
	 * @param pieceToMove	The piece being moved
	 * @return				A list of valid moves for this turn
	 * @throws 				PawnNotPromotedException
	 */
    ArrayList<Move> generateMovesForPiece(Board boardState, Piece pieceToMove, Square piecePosition) {
        clearMoveArrays();
        generateMovesForPieceHelper(pieceToMove, piecePosition, boardState.getBoard());
        return consolidateMoveArrays();
    }

    /**
	 * Clears all of the class's move storage arrays.
	 *
	 */
    private void clearMoveArrays() {
        this.kingCapturingMoves.clear();
        this.capturingMoves.clear();
        this.nonCapturingMoves.clear();
    }

    /**
	 * Consolidates all the move storage arrays in one array list, and returns.
	 * @return	The consolidated ArrayLists
	 */
    private ArrayList<Move> consolidateMoveArrays() {
        ArrayList<Move> allMoves = new ArrayList<Move>();
        allMoves.addAll(this.kingCapturingMoves);
        allMoves.addAll(this.capturingMoves);
        allMoves.addAll(this.nonCapturingMoves);
        return allMoves;
    }

    /**
	 * Determines the type of piece being moved and calls the appropriate method(s) to generate its possible moves.
	 * @param p
	 * @param state
	 * @param board
	 */
    private void generateMovesForPieceHelper(Piece p, Square position, Piece[][] board) {
        if (p.type == PieceType.PAWN) {
            generatePawnMoves(p.color, position, board);
        } else if (p.type == PieceType.KING) {
            generateKingMoves(p.color, position, board);
        } else if (p.type == PieceType.QUEEN) {
            generateBishopMoves(p.color, position, board);
            generateRookMoves(p.color, position, board);
        } else if (p.type == PieceType.BISHOP) {
            generateBishopMoves(p.color, position, board);
        } else if (p.type == PieceType.KNIGHT) {
            generateKnightMoves(p.color, position, board);
        } else {
            generateRookMoves(p.color, position, board);
        }
    }

    /**
	 * Scans the squares immediately in front of the pawn for valid moves.
	 * Runtime: 3 checks
	 * @param color
	 * @param position
	 * @param board
	 */
    private void generatePawnMoves(Color color, Square position, Piece[][] board) {
        if (color == Color.WHITE) {
            if (position.row + 1 < Board.NUMBER_OF_ROWS) {
                pawnMoveHelper(color, position, board, position.row + 1, position.col + 1);
                pawnMoveHelper(color, position, board, position.row + 1, position.col - 1);
                if (board[position.row + 1][position.col] == null) {
                    this.nonCapturingMoves.add(new Move(position, new Square(position.row + 1, position.col)));
                }
            }
        } else {
            if (position.row - 1 >= 0) {
                pawnMoveHelper(color, position, board, position.row - 1, position.col + 1);
                pawnMoveHelper(color, position, board, position.row - 1, position.col - 1);
                if (board[position.row - 1][position.col] == null) {
                    this.nonCapturingMoves.add(new Move(position, new Square(position.row - 1, position.col)));
                }
            }
        }
    }

    /**
	 * Determines whether a possible pawn capture move is valid. If so, it will generate that capture move. 
	 * Note: moves to non-existent squares are invalid.
	 * @param currColor
	 * @param currPosition
	 * @param board
	 * @param row
	 * @param col
	 */
    private void pawnMoveHelper(Color currColor, Square currPosition, Piece[][] board, int row, int col) {
        if (!validSquare(row, col)) return;
        Piece contents = board[row][col];
        if (contents == null) {
            return;
        }
        checkCapture(currColor, currPosition, contents, row, col);
    }

    /**
	 * Scans the area immediately surrounding the King for valid moves.
	 * Runtime: 9 checks
	 * @param color
	 * @param position
	 * @param board
	 */
    private void generateKingMoves(Color color, Square position, Piece[][] board) {
        for (int newRow = position.row - 1; newRow <= position.row + 1; newRow++) {
            for (int newCol = position.col - 1; newCol <= position.col + 1; newCol++) {
                rankMoveHelper(color, position, board, newRow, newCol);
            }
        }
    }

    /**
	 * Scans the diagonals of the piece (queen or bishop) for valid moves. 
	 * @param color
	 * @param position
	 * @param board
	 */
    private void generateBishopMoves(Color color, Square position, Piece[][] board) {
        int offset = position.row - position.col;
        boolean blocked = false;
        for (int col = position.col + 1; col < Board.NUMBER_OF_COLUMNS && !blocked; col++) {
            blocked = rankMoveHelper(color, position, board, col + offset, col);
        }
        blocked = false;
        for (int col = position.col - 1; col >= 0 && !blocked; col--) {
            blocked = rankMoveHelper(color, position, board, col + offset, col);
        }
        offset = position.row + position.col;
        blocked = false;
        for (int col = position.col + 1; col < Board.NUMBER_OF_COLUMNS && !blocked; col++) {
            blocked = rankMoveHelper(color, position, board, offset - col, col);
        }
        blocked = false;
        for (int col = position.col - 1; col >= 0 && !blocked; col--) {
            blocked = rankMoveHelper(color, position, board, offset - col, col);
        }
    }

    /**
	 * Scans the row and column of the piece (queen or rook) for valid moves.
	 * @param color
	 * @param position
	 * @param board
	 */
    private void generateRookMoves(Color color, Square position, Piece[][] board) {
        boolean blocked = false;
        for (int row = position.row + 1; row < Board.NUMBER_OF_ROWS && !blocked; row++) {
            blocked = rankMoveHelper(color, position, board, row, position.col);
        }
        blocked = false;
        for (int row = position.row - 1; row >= 0 && !blocked; row--) {
            blocked = rankMoveHelper(color, position, board, row, position.col);
        }
        blocked = false;
        for (int col = position.col + 1; col < Board.NUMBER_OF_COLUMNS && !blocked; col++) {
            blocked = rankMoveHelper(color, position, board, position.row, col);
        }
        blocked = false;
        for (int col = position.col - 1; col >= 0 && !blocked; col--) {
            blocked = rankMoveHelper(color, position, board, position.row, col);
        }
    }

    /**
	 * Scans the eight possible Knight positions for valid moves.
	 * @param color
	 * @param position
	 * @param board
	 */
    private void generateKnightMoves(Color color, Square position, Piece[][] board) {
        int row = position.row;
        int col = position.col;
        rankMoveHelper(color, position, board, row + 2, col + 1);
        rankMoveHelper(color, position, board, row + 2, col - 1);
        rankMoveHelper(color, position, board, row - 2, col + 1);
        rankMoveHelper(color, position, board, row - 2, col - 1);
        rankMoveHelper(color, position, board, row + 1, col + 2);
        rankMoveHelper(color, position, board, row + 1, col - 2);
        rankMoveHelper(color, position, board, row - 1, col + 2);
        rankMoveHelper(color, position, board, row - 1, col - 2);
    }

    /**
	 * Determines whether a ranking (non-pawn) piece move is valid. If so, it generates a move.
	 * @param currColor
	 * @param currPosition
	 * @param board
	 * @param row
	 * @param col
	 * @return true if the square contains a piece, thus blocking further moves in this direction.
	 */
    private boolean rankMoveHelper(Color currColor, Square currPosition, Piece[][] board, int row, int col) {
        if (!validSquare(row, col)) return false;
        Piece contents = board[row][col];
        if (contents == null) {
            this.nonCapturingMoves.add(new Move(currPosition, new Square(row, col)));
            return false;
        }
        checkCapture(currColor, currPosition, contents, row, col);
        return true;
    }

    /**
	 * Determines whether a piece can be captured. If so, generates a valid move according to the rank of the piece.
	 * @param currColor
	 * @param currPosition
	 * @param contents
	 * @param row
	 * @param col
	 */
    private void checkCapture(Color currColor, Square currPosition, Piece contents, int row, int col) {
        if (contents.color == currColor) {
            return;
        }
        Square newPosition = new Square(row, col);
        Move move = new Move(currPosition, newPosition);
        if (contents.type == PieceType.KING) {
            this.kingCapturingMoves.add(move);
        } else {
            this.capturingMoves.add(move);
        }
    }

    /**
	 * Determines whether a square exists on the board.
	 * @param row
	 * @param col
	 * @return true if the square exists on the board.
	 */
    private boolean validSquare(int row, int col) {
        return (row >= 0 && row < Board.NUMBER_OF_ROWS && col >= 0 && col < Board.NUMBER_OF_COLUMNS);
    }

    /**
	 * Gives us the closest thing to a true shuffle that I can get without controlling
	 * the random number generator. This is used to reduce the bias in my move list,
	 * but cannot truly eliminate it. TODO determine if we should eliminate this.
	 * @param list
	 * @return
	 */
    private ArrayList<Move> shuffle(ArrayList<Move> list) {
        Random random = new Random();
        for (int index = (list.size() - 1); index > 0; index--) {
            int other = random.nextInt(index + 1);
            Move temp = list.get(other);
            list.set(other, list.get(index));
            list.set(index, temp);
        }
        return list;
    }

    /**
	 * Returns a sorted list. 
	 * Disclosure note: I went to http://www.thescripts.com/forum/thread15992.html for advice on how to do this.
	 *
	 */
    private void sortCapturingMoves() {
        shuffle(this.capturingMoves);
        Collections.sort(this.capturingMoves);
    }
}
