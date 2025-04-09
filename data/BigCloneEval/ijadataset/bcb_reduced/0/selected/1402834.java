package jbomberman.client.game;

import jbomberman.server.command.CECCreateGame;
import jbomberman.server.command.CECGameKeyPressed;
import jbomberman.server.command.CECGetBotList;
import jbomberman.server.command.CECGetGameList;
import jbomberman.server.command.CECGetLevelList;
import jbomberman.server.command.CECGetMaxPlayerNumber;
import jbomberman.server.command.CECGetRuleSetList;
import jbomberman.server.command.CECJoinGame;
import jbomberman.server.command.CECPlayerQuit;
import jbomberman.server.command.CECSendMessage;
import jbomberman.server.command.CECSetTeam;
import jbomberman.server.command.CECSystemReady;
import jbomberman.server.command.CECUserReady;
import jbomberman.server.command.ClientEventCommand;
import jbomberman.server.command.ServerEventCommand;
import jbomberman.util.ByteArrayReader;
import jbomberman.util.Log;

/**
 * Protocol.java
 *
 *
 *
 * @author Wolfgang Schriebl
 * @version 1.0
 */
public class Protocol {

    /**
   * Package-Path to the commands
   */
    private static final String COMMAND_PACKAGE = "jbomberman.server.command";

    /**
   * Command classes
   */
    private static final String[] COMMAND_CLASSES = { "SECFrameSend", "SECGameCreated", "SECGameFinished", "SECGameStarted", "SECMessageSend", "SECPlayerJoined", "SECPlayerLoggedOut", "SECPlayerReady", "SECSendGameInfos", "SECSendLevelList", "SECSendMaxPlayerNumber", "SECSendRuleSetList", "SECTeamSelectedByPlayer", "SECSendBotList" };

    /**
   * Message when error was thrown thru the deserializing process
   */
    private static final String DESERIALIZE_ERROR = "Error while deserializing incoming data in the protocol";

    /**
   * The connected GameLogic
   */
    private GameLogic logic_;

    /**
   * The connected network interface
   */
    private ClientNetworkAdapter network_;

    /**
   *
   */
    public Protocol() {
        this(null, null);
    }

    /**
   *
   */
    public Protocol(GameLogic logic, ClientNetworkAdapter network) {
        logic_ = logic;
        network_ = network;
    }

    /**
   *
   */
    public void setGameLogic(GameLogic logic) {
        logic_ = logic;
    }

    /**
   *
   */
    public void setNetwork(ClientNetworkAdapter network) {
        network_ = network;
    }

    /**
   *
   */
    public void deserialize(ByteArrayReader bar) throws Exception {
        Protocol.deserialize(logic_, bar);
    }

    /**
   *
   */
    public void createGame(String game_name, String level_name, String rule_set) {
        send(new CECCreateGame(game_name, level_name, rule_set));
    }

    /**
   *
   */
    public void createGame(String game_name, String level_name, String rule_set, int max_player_number) {
        send(new CECCreateGame(game_name, level_name, rule_set, max_player_number));
    }

    /**
   *
   */
    public void gameKeyPressed(String game_name, byte direction, byte button, byte player_id) {
        send(new CECGameKeyPressed(game_name, direction, button, player_id));
    }

    /**
   *
   */
    public void getGameList() {
        send(new CECGetGameList());
    }

    /**
   *
   */
    public void getLevelList() {
        send(new CECGetLevelList());
    }

    /**
   *
   */
    public void getMaxPlayerNumber() {
        send(new CECGetMaxPlayerNumber());
    }

    /**
   *
   */
    public void getRuleSetList() {
        send(new CECGetRuleSetList());
    }

    /**
   *
   */
    public void getBotList() {
        send(new CECGetBotList());
    }

    /**
   *
   */
    public void joinGame(String game_name, String player_name, byte player_type) {
        send(new CECJoinGame(game_name, player_name, player_type));
    }

    /**
   *
   */
    public void playerQuit(String game_name, byte player_id) {
        send(new CECPlayerQuit(game_name, player_id));
    }

    /**
   *
   */
    public void setTeam(String game_name, byte team_nr, byte player_id) {
        send(new CECSetTeam(game_name, team_nr, player_id));
    }

    /**
   *
   */
    public void systemReady(String game_name, byte player_id) {
        send(new CECSystemReady(game_name, player_id));
    }

    /**
   *
   */
    public void userReady(String game_name, boolean user_ready, byte player_id) {
        send(new CECUserReady(game_name, user_ready, player_id));
    }

    /**
   *
   */
    public void sendMessage(String game_name, String message, byte player_id) {
        send(new CECSendMessage(game_name, message, player_id));
    }

    /**
   *
   */
    private void send(byte[] data) {
        if (network_ != null) {
            network_.send(data);
        }
    }

    /**
   *
   */
    private void send(ClientEventCommand cec) {
        try {
            send(cec.getBytesToSend());
        } catch (java.io.IOException e) {
            Log.error(e.toString());
        }
    }

    /**
   *
   */
    private static void deserialize(GameLogic logic, ByteArrayReader bar) throws Exception {
        ServerEventCommand sec;
        byte sid;
        int len;
        Class sec_class = null;
        Class[] constr_arg_classes = { bar.getClass() };
        Object[] constr_arg = { bar };
        while (bar.available() > 0) {
            try {
                sec_class = null;
                sec = null;
                sid = bar.readByte();
                len = bar.readInt();
                for (int index = 0; index < COMMAND_CLASSES.length; index++) {
                    sec_class = Class.forName(COMMAND_PACKAGE + "." + COMMAND_CLASSES[index]);
                    if (sec_class.getField("SID").getByte(null) == sid) {
                        break;
                    }
                }
                if (sec_class != null) {
                    sec = (ServerEventCommand) sec_class.getConstructor(constr_arg_classes).newInstance(constr_arg);
                    if (logic != null) {
                        sec.action(logic);
                    }
                } else {
                    bar.skip(len);
                }
            } catch (Exception e) {
                Log.error(DESERIALIZE_ERROR + "\n" + e + "\n");
            }
        }
    }
}
