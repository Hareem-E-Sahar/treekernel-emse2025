import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.mina.common.*;

public class pusherBoy2 implements Stopable {

    private int boardX, boardY;

    private String boardString;

    private char[] directions = { 'U', 'D', 'L', 'R' };

    private String moves = "";

    private boolean isClear = false;

    private boolean stopTag = false;

    private int blocksNum = 0;

    private String name, pwd;

    private String solution = "";

    private HashSet<String> NoSolutionSituations;

    private Process searchProc;

    private boolean isSearchProcRun = false;

    private ThreadPoolExecutor helperThreadPool;

    private ArrayBlockingQueue<Runnable> taskQueue;

    private class searchTask implements Runnable {

        String taskBoardStr;

        int taskBlocks;

        int taskStartPos;

        int taskCurrPos;

        String taskTrace;

        searchTask(String task) {
            taskBoardStr = task.split(":")[0];
            taskBlocks = Integer.parseInt(task.split(":")[1]);
            taskStartPos = Integer.parseInt(task.split(":")[2]);
            taskCurrPos = Integer.parseInt(task.split(":")[3]);
            taskTrace = task.split(":")[4];
        }

        private void search(String boardStr, int blocks, int startPos, int currPos, String trace) {
            if (blocks == 0) {
                synchronized (moves) {
                    moves = trace;
                    isClear = true;
                    int x = startPos % (boardX + 1);
                    int y = startPos / (boardX + 1);
                    solution = x + "," + y + "," + moves;
                }
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
                search(bs.toString(), bl, startPos, i, trace.concat(move));
            }
        }

        public void run() {
            search(taskBoardStr, taskBlocks, taskStartPos, taskCurrPos, taskTrace);
        }
    }

    pusherBoy2(String usrId, String password) {
        name = usrId;
        pwd = password;
    }

    pusherBoy2(String boardInfo) {
        boardString = boardInfo;
        parseBoardString();
        NoSolutionSituations = new HashSet<String>();
    }

    public void assignBoard(String boardInfo) {
        boardString = boardInfo;
        parseBoardString();
    }

    public void reflash() {
        this.stopTag = false;
        this.blocksNum = 0;
        this.isClear = false;
        this.isSearchProcRun = false;
        solution = null;
    }

    private void input2level(InputStream is) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = in.readLine()) != null) {
            if (line.trim().startsWith("Level")) {
                System.out.println(line);
            }
            if (!line.trim().startsWith("FlashVars")) {
                continue;
            }
            line = line.split("\"")[1];
            String[] ss = line.split("(=|&)");
            boardX = Integer.parseInt(ss[1]);
            boardY = Integer.parseInt(ss[3]);
            boardString = ss[5];
            System.out.println(boardString);
            parseBoardString();
        }
        in.close();
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

    private void findSolution(String boardStr, int blocks, int startPos, int currPos, String trace) {
        if (blocks == 0) {
            synchronized (moves) {
                moves = trace;
                isClear = true;
                int x = startPos % (boardX + 1);
                int y = startPos / (boardX + 1);
                solution = x + "," + y + "," + moves;
            }
            return;
        }
        if (isClear == true || stopTag == true) return;
        boolean isTaskDispatched = false;
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

    public void solve() throws Exception {
        int i = 0, x = 0, y = 0;
        int p = 0;
        long startTime = System.currentTimeMillis();
        for (i = 324; i < boardString.length(); i++) {
            p++;
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
            }
            if (stopTag == true) break;
        }
        while (taskQueue.size() > 0) ;
        long endTime = System.currentTimeMillis();
        System.out.println("slove timeused: " + (endTime - startTime));
    }

    public void solve2(pusherboyHostHandler h) throws Exception {
        long startTime = System.currentTimeMillis();
        searchProc = Runtime.getRuntime().exec("cmd /c E:\\workspace\\pushboy\\src\\pusherboy_v1_1.exe " + "0 1 " + boardX + " " + blocksNum + " " + boardString);
        synchronized (searchProc) {
            isSearchProcRun = true;
            searchProc.waitFor();
            isSearchProcRun = false;
            if (stopTag) return;
            BufferedReader ir = new BufferedReader(new InputStreamReader(searchProc.getInputStream()));
            String rs = ir.readLine();
            if (rs.compareTo("") > 0) {
                solution = rs;
                h.notifyOthersToStop();
                System.out.println("host solved:" + rs);
            }
            ir.close();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("slove2 timeused: " + (endTime - startTime));
    }

    public synchronized void stopSolving(String s) {
        if (isSearchProcRun) {
            searchProc.destroy();
            solution = s;
            stopTag = true;
        }
    }

    public void process() throws Exception {
        solve();
    }

    public String getPuzzle() throws Exception {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "192.168.0.1");
        System.getProperties().put("proxyPort", "808");
        URL u = new URL("http://www.hacker.org/push/index.php?name=" + name + "&password=" + pwd);
        InputStream is = u.openStream();
        input2level(is);
        return blocksNum + ":" + boardString;
    }

    public String getSolution() {
        return solution;
    }
}
