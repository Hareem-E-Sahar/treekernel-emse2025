import java.io.*;
import java.math.*;
import java.util.*;

class MiniChess {

    static State currentState = new State();

    public static void main(String args[]) throws Exception {
        currentState.setupBoard();
        currentState.updateBoard();
        currentState.printBoard();
        while (!currentState.gameOver()) {
            if (currentState.turn) {
                currentState.moveGen();
                currentState.makeMove();
            } else {
                currentState.humanMove();
            }
            currentState.updateBoard();
            currentState.printBoard();
        }
        System.out.print("Victory for ");
        if (currentState.turn) {
            System.out.println("Black");
        } else {
            System.out.println("White");
        }
    }
}

class State {

    static final int WKING = 0;

    static final int WQUEEN = 1;

    static final int WBISHOP = 2;

    static final int WKNIGHT = 3;

    static final int WROOK = 4;

    static final int WPAWN1 = 5;

    static final int WPAWN2 = 6;

    static final int WPAWN3 = 7;

    static final int WPAWN4 = 8;

    static final int WPAWN5 = 9;

    static final int WQUEEN1 = 10;

    static final int WQUEEN2 = 11;

    static final int WQUEEN3 = 12;

    static final int WQUEEN4 = 13;

    static final int WQUEEN5 = 14;

    static final int BKING = 15;

    static final int BQUEEN = 16;

    static final int BBISHOP = 17;

    static final int BKNIGHT = 18;

    static final int BROOK = 19;

    static final int BPAWN1 = 20;

    static final int BPAWN2 = 21;

    static final int BPAWN3 = 22;

    static final int BPAWN4 = 23;

    static final int BPAWN5 = 24;

    static final int BQUEEN1 = 25;

    static final int BQUEEN2 = 26;

    static final int BQUEEN3 = 27;

    static final int BQUEEN4 = 28;

    static final int BQUEEN5 = 29;

    static final int maxSquares = 30;

    static final int maxPieces = 30;

    static final int[] powOf2 = new int[maxSquares];

    int[] pieces = new int[30];

    boolean turn = true;

    int movecounter = 0;

    char[] board = new char[30];

    Random rng = new Random();

    Vector moves = new Vector();

    float value = 0;

    int nodes = 0;

    Vector oldMoves = new Vector();

    State() {
        fillPowOf2();
        setupBoard();
    }

    State(State s) {
        fillPowOf2();
        for (int i = 0; i < maxPieces; i++) {
            pieces[i] = s.pieces[i];
        }
        turn = s.turn;
        movecounter = s.movecounter;
    }

    public void fillPowOf2() {
        powOf2[0] = 1;
        powOf2[1] = 2;
        powOf2[2] = 4;
        powOf2[3] = 8;
        powOf2[4] = 16;
        powOf2[5] = 32;
        powOf2[6] = 64;
        powOf2[7] = 128;
        powOf2[8] = 256;
        powOf2[9] = 512;
        powOf2[10] = 1024;
        powOf2[11] = 2048;
        powOf2[12] = 4096;
        powOf2[13] = 8192;
        powOf2[14] = 16384;
        powOf2[15] = 32768;
        powOf2[16] = 65536;
        powOf2[17] = 131072;
        powOf2[18] = 262144;
        powOf2[19] = 524288;
        powOf2[20] = 1048576;
        powOf2[21] = 2097152;
        powOf2[22] = 4194304;
        powOf2[23] = 8388608;
        powOf2[24] = 16777216;
        powOf2[25] = 33554432;
        powOf2[26] = 67108864;
        powOf2[27] = 134217728;
        powOf2[28] = 268435456;
        powOf2[29] = 536870912;
    }

    public void setupBoard() {
        pieces[WROOK] = powOf2[0];
        pieces[WKNIGHT] = powOf2[1];
        pieces[WBISHOP] = powOf2[2];
        pieces[WQUEEN] = powOf2[3];
        pieces[WKING] = powOf2[4];
        pieces[WPAWN1] = powOf2[5];
        pieces[WPAWN2] = powOf2[6];
        pieces[WPAWN3] = powOf2[7];
        pieces[WPAWN4] = powOf2[8];
        pieces[WPAWN5] = powOf2[9];
        pieces[WQUEEN1] = -1;
        pieces[WQUEEN2] = -1;
        pieces[WQUEEN3] = -1;
        pieces[WQUEEN4] = -1;
        pieces[WQUEEN5] = -1;
        pieces[BPAWN1] = powOf2[20];
        pieces[BPAWN2] = powOf2[21];
        pieces[BPAWN3] = powOf2[22];
        pieces[BPAWN4] = powOf2[23];
        pieces[BPAWN5] = powOf2[24];
        pieces[BKING] = powOf2[25];
        pieces[BQUEEN] = powOf2[26];
        pieces[BBISHOP] = powOf2[27];
        pieces[BKNIGHT] = powOf2[28];
        pieces[BROOK] = powOf2[29];
        pieces[BQUEEN1] = -1;
        pieces[BQUEEN2] = -1;
        pieces[BQUEEN3] = -1;
        pieces[BQUEEN4] = -1;
        pieces[BQUEEN5] = -1;
    }

    public void updateBoard() throws Exception {
        for (int i = 0; i < maxSquares; i++) {
            board[i] = '.';
        }
        for (int i = 0; i < maxPieces; i++) {
            if (pieces[i] != -1) {
                int sq = getSquareNum(pieces[i], 0, maxSquares);
                if (sq == -1) {
                    throw new Exception();
                }
                board[sq] = getChar(i);
            }
        }
    }

    public char getChar(int piece) {
        switch(piece) {
            case WKING:
                return 'K';
            case WQUEEN:
            case WQUEEN1:
            case WQUEEN2:
            case WQUEEN3:
            case WQUEEN4:
            case WQUEEN5:
                return 'Q';
            case WBISHOP:
                return 'B';
            case WKNIGHT:
                return 'N';
            case WROOK:
                return 'R';
            case WPAWN1:
            case WPAWN2:
            case WPAWN3:
            case WPAWN4:
            case WPAWN5:
                return 'P';
            case BKING:
                return 'k';
            case BQUEEN:
            case BQUEEN1:
            case BQUEEN2:
            case BQUEEN3:
            case BQUEEN4:
            case BQUEEN5:
                return 'q';
            case BBISHOP:
                return 'b';
            case BKNIGHT:
                return 'n';
            case BROOK:
                return 'r';
            case BPAWN1:
            case BPAWN2:
            case BPAWN3:
            case BPAWN4:
            case BPAWN5:
                return 'p';
        }
        return '.';
    }

    public void printBoard() {
        System.out.println();
        System.out.print(movecounter);
        if (turn) {
            System.out.println(" W");
        } else {
            System.out.println(" B");
        }
        for (int i = 5; i >= 0; i--) {
            System.out.print((i + 1) + "    ");
            for (int j = 0; j < 5; j++) {
                System.out.print(board[(i * 5) + j] + " ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("     a b c d e");
    }

    public void moveGen() throws Exception {
        int lowbound = 0;
        int highbound = 0;
        int oplowbound = 0;
        int ophighbound = 0;
        if (turn) {
            lowbound = 0;
            highbound = 14;
            oplowbound = 15;
            ophighbound = 29;
        }
        if (!turn) {
            lowbound = 15;
            highbound = 29;
            oplowbound = 0;
            ophighbound = 14;
        }
        moves.clear();
        Vector targets = new Vector();
        for (int i = lowbound; i <= highbound; i++) {
            if (pieces[i] != -1) {
                if ((i >= WPAWN1) && (i <= WPAWN5)) {
                    targets.clear();
                    targets.setSize(3);
                    boolean blocked = false;
                    int thisSquare = getSquareNum(pieces[i], 0, maxSquares);
                    if (thisSquare == -1) {
                        throw new Exception();
                    }
                    if (thisSquare + 5 < maxSquares) {
                        targets.add(0, thisSquare + 5);
                    } else {
                        blocked = true;
                    }
                    if (thisSquare % 5 != 0) {
                        if (thisSquare + 4 < maxSquares) {
                            targets.add(1, thisSquare + 4);
                        }
                    }
                    if (thisSquare % 5 != 4) {
                        if (thisSquare + 6 < maxSquares) {
                            targets.add(2, thisSquare + 6);
                        }
                    }
                    for (int j = 0; j < maxPieces; j++) {
                        if (targets.elementAt(0) != null) {
                            if (pieces[j] == powOf2[((Integer) (targets.get(0))).intValue()]) {
                                blocked = true;
                            }
                            if (targets.elementAt(1) != null) {
                                if (pieces[j] == powOf2[((Integer) (targets.get(1))).intValue()] && !((j >= lowbound) && (j <= highbound))) {
                                    moves.add(new Move(new Square(thisSquare), new Square(thisSquare + 4)));
                                }
                            }
                            if (targets.elementAt(2) != null) {
                                if (pieces[j] == powOf2[((Integer) (targets.get(2))).intValue()] && !((j >= lowbound) && (j <= highbound))) {
                                    moves.add(new Move(new Square(thisSquare), new Square(thisSquare + 6)));
                                }
                            }
                        }
                    }
                    if (!blocked) {
                        moves.add(new Move(new Square(thisSquare), new Square(thisSquare + 5)));
                    }
                }
                if ((i >= BPAWN1) && (i <= BPAWN5)) {
                    targets.clear();
                    targets.setSize(3);
                    boolean blocked = false;
                    int thisSquare = getSquareNum(pieces[i], 0, maxSquares);
                    if (thisSquare == -1) {
                        throw new Exception();
                    }
                    if (thisSquare - 5 >= 0) {
                        targets.add(0, thisSquare - 5);
                    } else {
                        blocked = true;
                    }
                    if (thisSquare % 5 != 0) {
                        if (thisSquare - 6 >= 0) {
                            targets.add(1, thisSquare - 6);
                        }
                    }
                    if (thisSquare % 5 != 4) {
                        if (thisSquare - 4 >= 0) {
                            targets.add(2, thisSquare - 4);
                        }
                    }
                    for (int j = 0; j < maxPieces; j++) {
                        if (targets.elementAt(0) != null) {
                            if (pieces[j] == powOf2[((Integer) (targets.get(0))).intValue()]) {
                                blocked = true;
                            }
                            if (targets.elementAt(1) != null) {
                                if (pieces[j] == powOf2[((Integer) (targets.get(1))).intValue()] && !((j >= lowbound) && (j <= highbound))) {
                                    moves.add(new Move(new Square(thisSquare), new Square(thisSquare - 6)));
                                }
                            }
                            if (targets.elementAt(2) != null) {
                                if (pieces[j] == powOf2[((Integer) (targets.get(2))).intValue()] && !((j >= lowbound) && (j <= highbound))) {
                                    moves.add(new Move(new Square(thisSquare), new Square(thisSquare - 4)));
                                }
                            }
                        }
                    }
                    if (!blocked) {
                        moves.add(new Move(new Square(thisSquare), new Square(thisSquare - 5)));
                    }
                }
                if ((i == WKING) || (i == BKING)) {
                    targets.clear();
                    int thisSquare = getSquareNum(pieces[i], 0, maxSquares);
                    if (thisSquare == -1) {
                        throw new Exception();
                    }
                    for (int y = -1; y <= 1; y++) {
                        for (int x = -1; x <= 1; x++) {
                            int targetSquare = thisSquare + (x * 5) + y;
                            if ((targetSquare >= 0) && (targetSquare < maxSquares)) {
                                if ((thisSquare != targetSquare) && (((thisSquare % 5 == 0) && (targetSquare % 5 != 4)) || ((thisSquare % 5 == 4) && (targetSquare % 5 != 0)) || ((thisSquare % 5 > 0) && (thisSquare % 5 < 4)))) {
                                    targets.add(targetSquare);
                                }
                            }
                        }
                    }
                    for (int j = 0; j < targets.size(); j++) {
                        boolean blocked = false;
                        int targetSquare = ((Integer) (targets.get(j)));
                        for (int k = 0; k < maxPieces; k++) {
                            if (pieces[k] == powOf2[targetSquare]) {
                                blocked = true;
                                if (!((k >= lowbound) && (k <= highbound))) {
                                    moves.add(new Move(new Square(thisSquare), new Square(targetSquare)));
                                }
                            }
                        }
                        if (!blocked) {
                            moves.add(new Move(new Square(thisSquare), new Square(targetSquare)));
                        }
                    }
                }
                if ((i == WKNIGHT) || (i == BKNIGHT)) {
                    targets.clear();
                    int thisSquare = getSquareNum(pieces[i], 0, maxSquares);
                    if (thisSquare == -1) {
                        throw new Exception();
                    }
                    boolean flip = false;
                    int y = 0;
                    int counter = 1;
                    for (int x = -2; x <= 2; x += counter) {
                        if (x != 0) {
                            if ((x == 2) || (x == -2)) {
                                y = 1;
                            }
                            if ((x == 1) || (x == -1)) {
                                y = 2;
                            }
                            if (flip) {
                                y = -y;
                            }
                            int targetSquare = thisSquare + (x * 5) + y;
                            if ((targetSquare >= 0) && (targetSquare < maxSquares)) {
                                if ((thisSquare != targetSquare) && (((thisSquare % 5 == 0) && (targetSquare % 5 < 3)) || ((thisSquare % 5 == 4) && (targetSquare % 5 > 1)) || ((thisSquare % 5 == 1) && (targetSquare % 5 < 4)) || ((thisSquare % 5 == 3) && (targetSquare % 5 > 0)) || ((thisSquare % 5 == 2)))) {
                                    targets.add(targetSquare);
                                }
                            }
                        }
                        if (counter == 0) {
                            counter = 1;
                        } else {
                            counter = 0;
                        }
                        flip = !flip;
                    }
                    for (int j = 0; j < targets.size(); j++) {
                        boolean blocked = false;
                        int targetSquare = ((Integer) (targets.get(j)));
                        for (int k = 0; k < maxPieces; k++) {
                            if (pieces[k] == powOf2[targetSquare]) {
                                blocked = true;
                                if (!((k >= lowbound) && (k <= highbound))) {
                                    moves.add(new Move(new Square(thisSquare), new Square(targetSquare)));
                                }
                            }
                        }
                        if (!blocked) {
                            moves.add(new Move(new Square(thisSquare), new Square(targetSquare)));
                        }
                    }
                }
                if ((i == BROOK) || (i == WROOK)) {
                    targets.clear();
                    int thisSquare = pieces[i];
                    if (thisSquare == -1) {
                        throw new Exception();
                    }
                    boolean blocked = false;
                    boolean offboard = false;
                    int targetSquare = thisSquare;
                    while (!blocked && !offboard) {
                        for (int x = 0; x < 5; x++) {
                            targetSquare <<= 1;
                        }
                        if (((targetSquare <= powOf2[29])) && (targetSquare != 0)) {
                            for (int j = 0; j < maxPieces; j++) {
                                if (pieces[j] == targetSquare) {
                                    blocked = true;
                                    if ((j >= oplowbound) && (j <= ophighbound)) {
                                        moves.add(new Move(new Square(getSquareNum(thisSquare, 0, maxSquares)), new Square(getSquareNum(targetSquare, 0, maxSquares))));
                                    }
                                }
                            }
                            if (!blocked) {
                                moves.add(new Move(new Square(getSquareNum(thisSquare, 0, maxSquares)), new Square(getSquareNum(targetSquare, 0, maxSquares))));
                            }
                        } else {
                            offboard = true;
                        }
                    }
                    blocked = false;
                    offboard = false;
                    targetSquare = thisSquare;
                    while (!blocked && !offboard) {
                        for (int x = 0; x < 5; x++) {
                            targetSquare >>= 1;
                        }
                        if (targetSquare > 0) {
                            for (int j = 0; j < maxPieces; j++) {
                                if (pieces[j] == targetSquare) {
                                    blocked = true;
                                    if ((j >= oplowbound) && (j <= ophighbound)) {
                                        moves.add(new Move(new Square(getSquareNum(thisSquare, 0, maxSquares)), new Square(getSquareNum(targetSquare, 0, maxSquares))));
                                    }
                                }
                            }
                            if (!blocked) {
                                moves.add(new Move(new Square(getSquareNum(thisSquare, 0, maxSquares)), new Square(getSquareNum(targetSquare, 0, maxSquares))));
                            }
                        } else {
                            offboard = true;
                        }
                    }
                    blocked = false;
                    offboard = false;
                    targetSquare = thisSquare;
                    while (!blocked && !offboard) {
                        targetSquare >>= 1;
                        int thisSquareNum = getSquareNum(thisSquare, 0, maxSquares);
                        int targetSquareNum = getSquareNum(targetSquare, 0, maxSquares);
                        if (((targetSquareNum % 5) < (thisSquareNum % 5)) && (targetSquare > 0)) {
                            for (int j = 0; j < maxPieces; j++) {
                                if (pieces[j] == targetSquare) {
                                    blocked = true;
                                    if ((j >= oplowbound) && (j <= ophighbound)) {
                                        moves.add(new Move(new Square(getSquareNum(thisSquare, 0, maxSquares)), new Square(getSquareNum(targetSquare, 0, maxSquares))));
                                    }
                                }
                            }
                            if (!blocked) {
                                moves.add(new Move(new Square(getSquareNum(thisSquare, 0, maxSquares)), new Square(getSquareNum(targetSquare, 0, maxSquares))));
                            }
                        } else {
                            offboard = true;
                        }
                    }
                    blocked = false;
                    offboard = false;
                    targetSquare = thisSquare;
                    while (!blocked && !offboard) {
                        targetSquare <<= 1;
                        int thisSquareNum = getSquareNum(thisSquare, 0, maxSquares);
                        int targetSquareNum = getSquareNum(targetSquare, 0, maxSquares);
                        if (((targetSquareNum % 5) > (thisSquareNum % 5)) && (targetSquare <= powOf2[29])) {
                            for (int j = 0; j < maxPieces; j++) {
                                if (pieces[j] == targetSquare) {
                                    blocked = true;
                                    if ((j >= oplowbound) && (j <= ophighbound)) {
                                        moves.add(new Move(new Square(getSquareNum(thisSquare, 0, maxSquares)), new Square(getSquareNum(targetSquare, 0, maxSquares))));
                                    }
                                }
                            }
                            if (!blocked) {
                                moves.add(new Move(new Square(getSquareNum(thisSquare, 0, maxSquares)), new Square(getSquareNum(targetSquare, 0, maxSquares))));
                            }
                        } else {
                            offboard = true;
                        }
                    }
                }
                if ((i == WBISHOP) || (i == BBISHOP)) {
                    targets.clear();
                    int thisSquare = pieces[i];
                    if ((thisSquare < 1) || (thisSquare > powOf2[29])) {
                        throw new Exception();
                    }
                    boolean blocked, offboard;
                    int targetSquare, lastSquare;
                    boolean dir = true;
                    int shiftfactor = 6;
                    for (int count = 0; count < 4; count++) {
                        switch(count) {
                            case 0:
                                shiftfactor = 6;
                                dir = true;
                                break;
                            case 1:
                                shiftfactor = 6;
                                dir = false;
                                break;
                            case 2:
                                shiftfactor = 4;
                                dir = true;
                                break;
                            case 3:
                                shiftfactor = 4;
                                dir = false;
                                break;
                        }
                        blocked = false;
                        offboard = false;
                        lastSquare = thisSquare;
                        targetSquare = thisSquare;
                        while (!blocked && !offboard) {
                            for (int x = 0; x < shiftfactor; x++) {
                                if (dir == true) {
                                    targetSquare <<= 1;
                                } else {
                                    targetSquare >>= 1;
                                }
                            }
                            int thisSquareNum = getSquareNum(thisSquare, 0, maxSquares);
                            int lastSquareNum = getSquareNum(lastSquare, 0, maxSquares);
                            int targetSquareNum = getSquareNum(targetSquare, 0, maxSquares);
                            if (((targetSquare > 0) && (targetSquare <= powOf2[29])) && (((targetSquareNum % 5) + 1 == lastSquareNum % 5) || ((targetSquareNum % 5) - 1 == lastSquareNum % 5))) {
                                for (int j = 0; j < maxPieces; j++) {
                                    if (pieces[j] == targetSquare) {
                                        blocked = true;
                                        if ((j >= oplowbound) && (j <= ophighbound)) {
                                            moves.add(new Move(new Square(getSquareNum(thisSquare, 0, maxSquares)), new Square(getSquareNum(targetSquare, 0, maxSquares))));
                                        }
                                    }
                                }
                                if (!blocked) {
                                    moves.add(new Move(new Square(getSquareNum(thisSquare, 0, maxSquares)), new Square(getSquareNum(targetSquare, 0, maxSquares))));
                                }
                            } else {
                                offboard = true;
                            }
                            lastSquare = targetSquare;
                        }
                    }
                }
                if (((i == WQUEEN) || ((i >= WQUEEN1) && (i <= WQUEEN5))) || ((i == BQUEEN) || ((i >= BQUEEN1) && (i <= BQUEEN5)))) {
                    int thisSquare = pieces[i];
                    if ((thisSquare < 1) || (thisSquare > powOf2[29])) {
                        throw new Exception();
                    }
                    boolean blocked, offboard;
                    int targetSquare, lastSquare;
                    boolean dir = true;
                    int shiftfactor = 6;
                    for (int count = 0; count < 8; count++) {
                        switch(count) {
                            case 0:
                                shiftfactor = 1;
                                dir = true;
                                break;
                            case 1:
                                shiftfactor = 6;
                                dir = true;
                                break;
                            case 2:
                                shiftfactor = 5;
                                dir = true;
                                break;
                            case 3:
                                shiftfactor = 4;
                                dir = true;
                                break;
                            case 4:
                                shiftfactor = 1;
                                dir = false;
                                break;
                            case 5:
                                shiftfactor = 4;
                                dir = false;
                                break;
                            case 6:
                                shiftfactor = 5;
                                dir = false;
                                break;
                            case 7:
                                shiftfactor = 6;
                                dir = false;
                                break;
                        }
                        blocked = false;
                        offboard = false;
                        lastSquare = thisSquare;
                        targetSquare = thisSquare;
                        while (!blocked && !offboard) {
                            for (int x = 0; x < shiftfactor; x++) {
                                if (dir == true) {
                                    targetSquare <<= 1;
                                } else {
                                    targetSquare >>= 1;
                                }
                            }
                            int thisSquareNum = getSquareNum(thisSquare, 0, maxSquares);
                            int lastSquareNum = getSquareNum(lastSquare, 0, maxSquares);
                            int targetSquareNum = getSquareNum(targetSquare, 0, maxSquares);
                            if (((targetSquare > 0) && (targetSquare <= powOf2[29])) && (((targetSquareNum % 5) + 1 == lastSquareNum % 5) || ((targetSquareNum % 5) - 1 == lastSquareNum % 5) || (targetSquareNum % 5 == lastSquareNum % 5))) {
                                for (int j = 0; j < maxPieces; j++) {
                                    if (pieces[j] == targetSquare) {
                                        blocked = true;
                                        if ((j >= oplowbound) && (j <= ophighbound)) {
                                            moves.add(new Move(new Square(getSquareNum(thisSquare, 0, maxSquares)), new Square(getSquareNum(targetSquare, 0, maxSquares))));
                                        }
                                    }
                                }
                                if (!blocked) {
                                    moves.add(new Move(new Square(getSquareNum(thisSquare, 0, maxSquares)), new Square(getSquareNum(targetSquare, 0, maxSquares))));
                                }
                            } else {
                                offboard = true;
                            }
                            lastSquare = targetSquare;
                        }
                    }
                }
            }
        }
    }

    public void humanMove() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter move: ");
        String move = reader.readLine();
        try {
            decodeMove(move);
        } catch (Exception e) {
            System.out.println("Incorrect move format, please try again (e.g. a0-a1) ");
        }
    }

    public void decodeMove(String move) throws Exception {
        if (move.length() != 5) {
            throw new Exception("move string wrong length");
        }
        char column = move.charAt(0);
        String row = move.substring(1, 2);
        int x = 0;
        int y;
        switch(column) {
            case 'a':
                x = 0;
                break;
            case 'b':
                x = 1;
                break;
            case 'c':
                x = 2;
                break;
            case 'd':
                x = 3;
                break;
            case 'e':
                x = 4;
                break;
        }
        y = Integer.parseInt(row) - 1;
        int fromSquare = (y * 5) + x;
        column = move.charAt(3);
        row = move.substring(4, 5);
        switch(column) {
            case 'a':
                x = 0;
                break;
            case 'b':
                x = 1;
                break;
            case 'c':
                x = 2;
                break;
            case 'd':
                x = 3;
                break;
            case 'e':
                x = 4;
                break;
        }
        y = Integer.parseInt(row) - 1;
        int toSquare = (y * 5) + x;
        if ((fromSquare >= 0) && (fromSquare < maxSquares) && (toSquare >= 0) && (toSquare < maxSquares)) {
            processMove(new Move(new Square(fromSquare), new Square(toSquare)));
        }
    }

    public void undoLastMove() throws Exception {
        int capturedPiece = ((Integer) oldMoves.remove(0)).intValue();
        Move toUndo = ((Move) oldMoves.remove(0));
        int piece = findPiece(toUndo.toSquare.number);
        if (capturedPiece != -1) {
            pieces[capturedPiece] = powOf2[toUndo.toSquare.number];
        }
        pieces[piece] = powOf2[toUndo.fromSquare.number];
        turn = !turn;
        if (!turn) {
            movecounter--;
        }
        evaluate();
        if ((piece >= WQUEEN1) && (piece <= WQUEEN5)) {
            if (pieces[WPAWN1] == -1) {
                pieces[WPAWN1] = pieces[piece];
                pieces[piece] = -1;
            }
            if (pieces[WPAWN2] == -1) {
                pieces[WPAWN2] = pieces[piece];
                pieces[piece] = -1;
            }
            if (pieces[WPAWN3] == -1) {
                pieces[WPAWN3] = pieces[piece];
                pieces[piece] = -1;
            }
            if (pieces[WPAWN4] == -1) {
                pieces[WPAWN4] = pieces[piece];
                pieces[piece] = -1;
            }
            if (pieces[WPAWN5] == -1) {
                pieces[WPAWN5] = pieces[piece];
                pieces[piece] = -1;
            }
        }
        if ((piece >= BQUEEN1) && (piece <= BQUEEN5)) {
            if (pieces[BPAWN1] == -1) {
                pieces[BPAWN1] = pieces[piece];
                pieces[piece] = -1;
            }
            if (pieces[BPAWN2] == -1) {
                pieces[BPAWN2] = pieces[piece];
                pieces[piece] = -1;
            }
            if (pieces[BPAWN3] == -1) {
                pieces[BPAWN3] = pieces[piece];
                pieces[piece] = -1;
            }
            if (pieces[BPAWN4] == -1) {
                pieces[BPAWN4] = pieces[piece];
                pieces[piece] = -1;
            }
            if (pieces[BPAWN5] == -1) {
                pieces[BPAWN5] = pieces[piece];
                pieces[piece] = -1;
            }
        }
    }

    public void processMove(Move desiredMove) throws Exception {
        System.out.println(desiredMove.fromSquare.number + "-" + desiredMove.toSquare.number);
        oldMoves.add(0, desiredMove);
        int piece = findPiece(desiredMove.fromSquare.number);
        if (piece == -1) {
            updateBoard();
            printBoard();
            throw new Exception();
        } else {
            int piece2 = findPiece(desiredMove.toSquare.number);
            if (piece2 != -1) {
                pieces[piece2] = -1;
                movecounter = 0;
                oldMoves.add(0, piece2);
            } else {
                oldMoves.add(0, -1);
            }
            pieces[piece] = powOf2[desiredMove.toSquare.number];
            evaluate();
            turn = !turn;
            if (turn) {
                movecounter++;
            }
        }
        if ((piece >= WPAWN1) && (piece <= WPAWN5)) {
            if ((pieces[piece] >= powOf2[25]) && (pieces[piece] <= powOf2[29])) {
                if (pieces[WQUEEN] == -1) {
                    pieces[WQUEEN] = pieces[piece];
                    pieces[piece] = -1;
                } else if (pieces[WQUEEN1] == -1) {
                    pieces[WQUEEN1] = pieces[piece];
                    pieces[piece] = -1;
                } else if (pieces[WQUEEN2] == -1) {
                    pieces[WQUEEN2] = pieces[piece];
                    pieces[piece] = -1;
                } else if (pieces[WQUEEN3] == -1) {
                    pieces[WQUEEN3] = pieces[piece];
                    pieces[piece] = -1;
                } else if (pieces[WQUEEN4] == -1) {
                    pieces[WQUEEN4] = pieces[piece];
                    pieces[piece] = -1;
                } else if (pieces[WQUEEN5] == -1) {
                    pieces[WQUEEN5] = pieces[piece];
                    pieces[piece] = -1;
                }
            }
        }
        if ((piece >= BPAWN1) && (piece <= BPAWN5)) {
            if ((pieces[piece] >= powOf2[0]) && (pieces[piece] <= powOf2[4])) {
                if (pieces[BQUEEN] == -1) {
                    pieces[BQUEEN] = pieces[piece];
                    pieces[piece] = -1;
                } else if (pieces[BQUEEN1] == -1) {
                    pieces[BQUEEN1] = pieces[piece];
                    pieces[piece] = -1;
                } else if (pieces[BQUEEN2] == -1) {
                    pieces[BQUEEN2] = pieces[piece];
                    pieces[piece] = -1;
                } else if (pieces[BQUEEN3] == -1) {
                    pieces[BQUEEN3] = pieces[piece];
                    pieces[piece] = -1;
                } else if (pieces[BQUEEN4] == -1) {
                    pieces[BQUEEN4] = pieces[piece];
                    pieces[piece] = -1;
                } else if (pieces[BQUEEN5] == -1) {
                    pieces[BQUEEN5] = pieces[piece];
                    pieces[piece] = -1;
                }
            }
        }
    }

    public void randomMove() throws Exception {
        int rand;
        if (moves.size() > 0) {
            rand = rng.nextInt(moves.size());
        } else {
            throw new Exception("No Moves available");
        }
        try {
            processMove(((Move) moves.get(rand)));
        } catch (Exception e) {
            System.out.println("Bad move attempted.");
            rand = rng.nextInt(moves.size());
        }
    }

    public void makeMove() throws Exception {
        nodes = 0;
        int d0 = 1;
        Vector bestmoves = new Vector();
        float val = 0;
        float maxvalue = -1;
        if (moves.size() > 0) {
            for (int i = 0; i < moves.size(); i++) {
                Move m = ((Move) moves.get(i));
                System.out.println(m.fromSquare.number + "-" + m.toSquare.number);
                processMove(m);
                val = negamax(4);
                undoLastMove();
                if (val == -99) {
                    throw new Exception("Negamax failed");
                }
                if (val > maxvalue) {
                    bestmoves.clear();
                    bestmoves.add(moves.get(i));
                    maxvalue = val;
                }
                if (val == maxvalue) {
                    bestmoves.add(moves.get(i));
                }
            }
            d0 += 1;
        } else {
            throw new Exception("No Moves available");
        }
        try {
            System.out.println("Nodes searched:" + nodes);
            processMove(((Move) bestmoves.get(rng.nextInt(bestmoves.size()))));
        } catch (Exception e) {
            System.out.println("Bad move attempted");
        }
    }

    public float negamax(int depth) throws Exception {
        nodes++;
        Vector v = new Vector();
        float max = -1.0f;
        if (depth == 0 || gameOver()) {
            evaluate();
            return value;
        }
        moveGen();
        for (int i = 0; i < moves.size(); i++) {
            if ((((Move) moves.get(i)).fromSquare.number != -1) && (((Move) moves.get(i)).toSquare.number != -1)) {
                processMove(((Move) moves.get(i)));
                if (value > max) {
                    max = value;
                    v.clear();
                    v.add(moves.get(i));
                }
                if (value == max) {
                    v.add(moves.get(i));
                }
                undoLastMove();
            }
        }
        float nmax = -1;
        for (int i = 0; i < v.size(); i++) {
            processMove(((Move) v.get(i)));
            float n = -negamax(depth - 1);
            undoLastMove();
            if (n > nmax) {
                nmax = n;
            }
        }
        return nmax;
    }

    public int getSquareNum(int number, int min, int max) {
        if ((number > 0) && (number <= powOf2[29])) {
            int avg = (min + max) / 2;
            if (powOf2[avg] == number) {
                return avg;
            } else {
                if (powOf2[avg] > number) {
                    return getSquareNum(number, min, avg - 1);
                }
                if (powOf2[avg] < number) {
                    return getSquareNum(number, avg + 1, max);
                }
            }
        }
        return -1;
    }

    public int findPiece(int squareNum) {
        for (int i = 0; i < 30; i++) {
            if (pieces[i] == powOf2[squareNum]) {
                return i;
            }
        }
        return -1;
    }

    public boolean gameOver() {
        if (turn) {
            for (int i = 0; i < 11; i++) {
                if (pieces[WKING] == -1) {
                    System.out.println("No White King");
                    return true;
                }
            }
        } else if (!turn) {
            for (int i = 0; i < 11; i++) {
                if (pieces[BKING] == -1) {
                    System.out.println("No Black King");
                    return true;
                }
            }
        }
        if (movecounter > 40) {
            System.out.println("Too many moves");
            return true;
        }
        return false;
    }

    public float evaluate() {
        int low = 0;
        int high = 15;
        int oplow = 15;
        int ophigh = 30;
        if (!turn) {
            low = 15;
            high = 30;
            oplow = 0;
            ophigh = 15;
        }
        if (turn) {
            if (pieces[BKING] == -1) {
                value = -1;
                return -1;
            }
            if (pieces[WKING] == -1) {
                value = 1;
                return 1;
            }
        } else {
            if (pieces[WKING] == -1) {
                value = -1;
                return -1;
            }
            if (pieces[BKING] == -1) {
                value = 1;
                return 1;
            }
        }
        int total = 0;
        for (int i = low; i < high; i++) {
            if (pieces[i] != -1) {
                total += valueOf(i);
            }
        }
        for (int i = oplow; i < ophigh; i++) {
            if (pieces[i] != -1) {
                total -= valueOf(i);
            }
        }
        value = (total / 24.0f);
        return (total / 24.0f);
    }

    public int valueOf(int piece) {
        int v = 0;
        switch(piece) {
            case WROOK:
            case BROOK:
                v = 5;
                break;
            case WKNIGHT:
            case BKNIGHT:
                v = 3;
                break;
            case WBISHOP:
            case BBISHOP:
                v = 1;
                break;
            case WQUEEN:
            case BQUEEN:
            case WQUEEN1:
            case WQUEEN2:
            case WQUEEN3:
            case WQUEEN4:
            case WQUEEN5:
            case BQUEEN1:
            case BQUEEN2:
            case BQUEEN3:
            case BQUEEN4:
            case BQUEEN5:
                v = 9;
                break;
            case WPAWN1:
            case WPAWN2:
            case WPAWN3:
            case WPAWN4:
            case WPAWN5:
            case BPAWN1:
            case BPAWN2:
            case BPAWN3:
            case BPAWN4:
            case BPAWN5:
                v = 1;
                break;
        }
        return v;
    }
}

class Square {

    char x;

    int y;

    int number;

    Square(int num) {
        int temp = num % 5;
        switch(temp) {
            case 0:
                x = 'a';
                break;
            case 1:
                x = 'b';
                break;
            case 2:
                x = 'c';
                break;
            case 3:
                x = 'd';
                break;
            case 4:
                x = 'e';
                break;
        }
        y = (num / 5) + 1;
        number = num;
    }
}

class Move {

    Square fromSquare;

    Square toSquare;

    Move(Square one, Square two) {
        fromSquare = one;
        toSquare = two;
    }
}
