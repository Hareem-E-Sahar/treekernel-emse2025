import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import org.apache.mina.common.IoSession;

public class CoPusher implements Stopable {

    private int boardX, boardY;

    private String boardString;

    private char[] directions = { 'U', 'D', 'L', 'R' };

    private String moves;

    private boolean isClear = false;

    private boolean stopTag = false;

    private int blocksNum = 0;

    private int startPos;

    private HashSet<String> NoSolutionSituations;

    private IoSession session;

    private Process searchProc;

    private boolean isSearchProcRun = false;

    CoPusher() {
        NoSolutionSituations = new HashSet<String>();
    }

    public void parsePuzzleInfo(String puzzle) {
        startPos = Integer.parseInt(puzzle.split(":")[1]);
        blocksNum = Integer.parseInt(puzzle.split(":")[2]);
        boardString = puzzle.split(":")[3];
        boardX = boardString.split(",")[0].length();
        boardY = boardString.split(",").length;
        NoSolutionSituations.clear();
    }

    private void parseBoardString() {
        int steps = 0;
        for (int i = 0; i < boardString.length(); i++) {
            if (boardString.charAt(i) != '.' && boardString.charAt(i) != ',') {
                steps += boardString.charAt(i) - 'a' + 1;
                blocksNum++;
            }
        }
        System.out.println("grid:" + boardX + "*" + boardY + " blocks:" + blocksNum + " steps:" + steps);
        String lines[] = boardString.split(",");
        for (int y = 0; y < lines.length; y++) {
            for (int x = 0; x < lines[y].length(); x++) {
                System.out.print(lines[y].charAt(x) + " ");
            }
            System.out.println();
        }
    }

    public void reflash() {
        this.stopTag = false;
        this.blocksNum = 0;
        this.isClear = false;
        isSearchProcRun = false;
    }

    private void findSolution(String boardStr, int blocks, int startPos, int currPos, String trace) {
        if (blocks == 0) {
            int x, y;
            moves = trace;
            isClear = true;
            x = startPos % (boardX + 1);
            y = startPos / (boardX + 1);
            session.write("solution:" + x + "," + y + "," + moves);
            System.out.println("collaborator solved,solution:" + x + "," + y + "," + moves);
            session.write("stoped");
            return;
        }
        if (isClear == true || stopTag == true) return;
        for (char d : directions) {
            int bl = blocks;
            StringBuffer bs = new StringBuffer(boardStr);
            int i = currPos;
            int span;
            String move;
            if (isClear == true || stopTag == true) return;
            switch(d) {
                case 'U':
                    span = -(boardX + 1);
                    move = "U";
                    break;
                case 'D':
                    span = boardX + 1;
                    move = "D";
                    break;
                case 'L':
                    span = -1;
                    move = "L";
                    break;
                case 'R':
                    span = 1;
                    move = "R";
                    break;
                default:
                    span = 0;
                    move = "";
                    break;
            }
            if ((currPos + span) < 0 || (currPos + span) >= bs.length() || bs.charAt(currPos + span) != '.') {
                continue;
            }
            for (; i >= 0 && i < bs.length() && bs.charAt(i) == '.'; i += span) ;
            if (i < 0 || i >= bs.length() || bs.charAt(i) == ',') continue;
            if (bs.charAt(i) == 'a') bl--; else {
                int des = i + span;
                if (des > 0 && des <= bs.length()) {
                    if (bs.charAt(des) == ',') {
                        continue;
                    }
                    char base;
                    if (bs.charAt(des) >= 'a') {
                        bl--;
                        base = bs.charAt(des);
                    } else {
                        base = 'a' - 1;
                    }
                    bs.setCharAt(des, (char) ((bs.charAt(i) - 'a') + base));
                } else continue;
            }
            bs.setCharAt(i, '.');
            findSolution(bs.toString(), bl, startPos, i, trace.concat(move));
        }
    }

    public void solve(IoSession s, String puzzle) throws Exception {
        int i = 0, x = 0, y = 0;
        int p = 0;
        reflash();
        session = s;
        parsePuzzleInfo(puzzle);
        System.out.println("collaborator solving...");
        for (i = startPos; i >= 0; i--) {
            int startPos = 0;
            if (boardString.charAt(i) >= 'a') {
                if (isClear == true || stopTag == true) break;
                int span = 0;
                boolean FindStartPos = false;
                String firstStep = "";
                StringBuffer board = new StringBuffer(boardString);
                board.setCharAt(i, '.');
                for (char d : directions) {
                    StringBuffer bs = new StringBuffer(boardString);
                    switch(d) {
                        case 'U':
                            span = -(boardX + 1);
                            firstStep = "D";
                            break;
                        case 'D':
                            span = (boardX + 1);
                            firstStep = "U";
                            break;
                        case 'L':
                            span = -1;
                            firstStep = "R";
                            break;
                        case 'R':
                            span = 1;
                            firstStep = "L";
                            break;
                    }
                    int k;
                    for (k = i + span; k >= 0 && k < bs.length() && bs.charAt(k) == '.'; k += span) ;
                    if (Math.abs(k - i) > 2 * Math.abs(span)) {
                        FindStartPos = true;
                        startPos = k - span;
                        if (boardString.charAt(i) > 'a') {
                            int des = i - span;
                            int bl = blocksNum;
                            if (des >= 0 && des < boardString.length()) {
                                char base;
                                if (bs.charAt(des) >= 'a') {
                                    bl--;
                                    base = bs.charAt(des);
                                } else {
                                    base = 'a' - 1;
                                }
                                bs.setCharAt(des, (char) ((bs.charAt(i) - 'a') + base));
                            }
                            bs.setCharAt(i, '.');
                            findSolution(bs.toString(), bl, startPos, i, firstStep);
                            if (isClear == true || stopTag == true) break;
                        } else if (boardString.charAt(i) == 'a') break;
                    }
                }
                if (FindStartPos && boardString.charAt(i) == 'a') findSolution(board.toString(), blocksNum - 1, startPos, i, firstStep);
                p++;
                System.out.println("progress:" + p + "/" + blocksNum);
            }
            if (stopTag == true) return;
        }
        return;
    }

    public void solve2(IoSession s, String puzzle) throws Exception {
        long startTime = System.currentTimeMillis();
        reflash();
        session = s;
        parsePuzzleInfo(puzzle);
        int offset = startPos;
        searchProc = Runtime.getRuntime().exec("cmd /c E:\\workspace\\pushboy\\src\\pusherboy_v1_1.exe " + offset + " -1 " + boardX + " " + blocksNum + " " + boardString);
        isSearchProcRun = true;
        if (stopTag) return;
        BufferedReader ir = new BufferedReader(new InputStreamReader(searchProc.getInputStream()));
        String rs = ir.readLine();
        if (rs.compareTo("") > 0) {
            session.write("solution:" + rs);
            System.out.println("collaborator solved,solution:" + rs);
            session.write("stoped");
        }
        ir.close();
        long endTime = System.currentTimeMillis();
        System.out.println("slove2 timeused: " + (endTime - startTime));
    }

    public void stopSolving(IoSession session) {
        synchronized (searchProc) {
            if (isSearchProcRun) searchProc.destroy();
            try {
                searchProc.waitFor();
                isSearchProcRun = false;
            } catch (Exception e) {
            }
            stopTag = true;
            session.write("stoped");
        }
    }
}
