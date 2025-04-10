package backend.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;
import backend.constants.MoveResult;
import backend.state.Board;
import backend.util.BackendException;
import backend.util.Logger;
import backend.util.MsgUtils;

public class ServerThread extends Thread {

    private Socket sock = null;

    private int threadNum;

    public ServerThread(Socket s, int threadNum) {
        sock = s;
        this.threadNum = threadNum;
    }

    public void run() {
        String inputLine = "";
        try {
            PrintWriter myOut = (PrintWriter) Server.outputters.get(String.valueOf(threadNum));
            PrintWriter oppOut = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(inputLine, "|");
                String playerId;
                switch(Integer.parseInt(st.nextToken())) {
                    case 0:
                        playerId = st.nextToken();
                        Server.serverConsole.write("Player " + playerId + " has connected.");
                        Server.gameEngine.addPlayer(playerId, Board.deserialize(st.nextToken()));
                        while (!Server.gameEngine.isGameReady()) {
                            Thread.sleep(2000);
                            continue;
                        }
                        oppOut = (PrintWriter) Server.outputters.get(getOtherThreadNum());
                        MsgUtils.sendTurnMessage(myOut, Server.gameEngine.isMyTurn(playerId));
                        break;
                    case 1:
                        playerId = st.nextToken();
                        String x = st.nextToken();
                        String y = st.nextToken();
                        Server.serverConsole.write("Player " + playerId + " attacks [" + x + "," + y + "]");
                        MoveResult moveResult = Server.gameEngine.move(playerId, x, y);
                        if (moveResult == MoveResult.WIN) {
                            MsgUtils.sendGameOverMessage(myOut, x, y, "win");
                            Thread.sleep(1000);
                            MsgUtils.sendGameOverMessage(oppOut, x, y, "lose");
                        } else {
                            MsgUtils.sendIsHitMessage(myOut, moveResult);
                            Thread.sleep(1000);
                            MsgUtils.sendMoveNotifyMessage(oppOut, x, y);
                        }
                        break;
                }
            }
            Server.gameEngine.resetGame();
            myOut.close();
            in.close();
            sock.close();
        } catch (IOException e) {
            Server.serverConsole.write("ServerThread: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Server.serverConsole.write("ServerThread: " + e.getMessage());
            e.printStackTrace();
        } catch (BackendException e) {
            Logger.LogError(e.getMessage());
            e.printStackTrace();
        }
    }

    private String getOtherThreadNum() {
        if (threadNum == 1) {
            return "2";
        } else {
            return "1";
        }
    }
}
