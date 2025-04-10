package teamspeak.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;
import teamspeak.reader.subclasses.Channel;
import teamspeak.reader.subclasses.User;

/**
 * 
 * @author lalbrecht
 * 
 * This class is the main "Java TS2 Viewer"-class. It contains all necessary methods to show the viewer on a website.
 *
 */
public class Teamspeak {

    private String serverAddress = "localhost";

    private int serverQueryPort = 51234;

    private int serverUDPPort = 8767;

    private String serverPassword = "";

    private String loginname = "WebGuest";

    private String pictureBase = "pictures/ts2";

    private Vector<Channel> channel = new Vector<Channel>();

    private Vector<User> user = new Vector<User>();

    /**
	 * public empty (main) constructor.
	 */
    public Teamspeak() {
    }

    /**
	 * One of the main method to initialize the class.
	 * 
	 * @param serverAddress serverAddress
	 * @param serverQueryPort serverQueryPort
	 * @param serverUDPPort serverUDPPort
	 * @param serverPassword serverPassword
	 * @param loginName loginName
	 * @param pictureBase pictureBase
	 */
    public final void setInfo(final String serverAddress, final int serverQueryPort, final int serverUDPPort, final String serverPassword, final String loginName, final String pictureBase) {
        this.serverAddress = serverAddress;
        this.serverQueryPort = serverQueryPort;
        this.serverUDPPort = serverUDPPort;
        this.serverPassword = serverPassword;
        this.loginname = loginName;
        this.pictureBase = pictureBase;
    }

    /**
	 * This method get the info for the user and the channel.
	 */
    public final void getInfo() {
        try {
            this.channel = getChannelInfo();
            this.user = getUserInfo();
        } catch (Exception e) {
            this.channel = null;
            this.user = null;
        }
    }

    /**
	 * The method to get the codec name. 
	 * 
	 * @param codecId codecId
	 * @return codec codec
	 */
    private String getVerboseCodec(int codecId) {
        String codec = null;
        if (codecId == 0) {
            codec = "CELP 5.1 Kbit";
        } else if (codecId == 1) {
            codec = "CELP 6.3 Kbit";
        } else if (codecId == 2) {
            codec = "GSM 14.8 Kbit";
        } else if (codecId == 3) {
            codec = "GSM 16.4 Kbit";
        } else if (codecId == 4) {
            codec = "CELP Windows 5.2 Kbit";
        } else if (codecId == 5) {
            codec = "Speex 3.4 Kbit";
        } else if (codecId == 6) {
            codec = "Speex 5.2 Kbit";
        } else if (codecId == 7) {
            codec = "Speex 7.2 Kbit";
        } else if (codecId == 8) {
            codec = "Speex 9.3 Kbit";
        } else if (codecId == 9) {
            codec = "Speex 12.3 Kbit";
        } else if (codecId == 10) {
            codec = "Speex 16.3 Kbit";
        } else if (codecId == 11) {
            codec = "Speex 19.5 Kbit";
        } else if (codecId == 12) {
            codec = "Speex 25.9 Kbit";
        } else {
            codec = "unknown (" + codecId + ")";
        }
        return codec;
    }

    /**
	 * 
	 * Returns the player flags as a string. 
	 * 
	 * @param pv playerflag
	 * @param cv channelflag
	 * @return playerflags for the user
	 */
    private String getPlayerFlags(int pv, int cv) {
        String plpriv = null;
        String clpriv = null;
        if (pv == 13) {
            plpriv = "(R SA";
        } else if (pv == 5) {
            plpriv = "(R SA";
        } else if (pv == 4) {
            plpriv = "(R";
        } else if (pv < 4) {
            plpriv = "(U";
        }
        if (cv == 1) {
            clpriv = " CA)";
        } else {
            clpriv = ")";
        }
        return plpriv + clpriv;
    }

    /**
	 * Returns the flags for the channel as a string.
	 * 
	 * @param flag channelflags
	 * @return channelflags
	 */
    private String getChannelFlags(int flag) {
        String clflag = null;
        if (flag == 30) {
            clflag = "(RMPSD)";
        } else if (flag == 28) {
            clflag = "(RPSD)";
        } else if (flag == 26) {
            clflag = "(RMSD)";
        } else if (flag == 24) {
            clflag = "(RSD)";
        } else if (flag == 22) {
            clflag = "(RMPD)";
        } else if (flag == 20) {
            clflag = "(RPD)";
        } else if (flag == 18) {
            clflag = "(RMD)";
        } else if (flag == 16) {
            clflag = "(RD)";
        } else if (flag == 15) {
            clflag = "(UMPS)";
        } else if (flag == 14) {
            clflag = "(RMPS)";
        } else if (flag == 13) {
            clflag = "(UPS)";
        } else if (flag == 12) {
            clflag = "(RPS)";
        } else if (flag == 11) {
            clflag = "(UMS)";
        } else if (flag == 10) {
            clflag = "(RMS)";
        } else if (flag == 9) {
            clflag = "(US)";
        } else if (flag == 8) {
            clflag = "(RS)";
        } else if (flag == 7) {
            clflag = "(UMP)";
        } else if (flag == 6) {
            clflag = "(RMP)";
        } else if (flag == 5) {
            clflag = "(UP)";
        } else if (flag == 4) {
            clflag = "(RP)";
        } else if (flag == 3) {
            clflag = "(UM)";
        } else if (flag == 2) {
            clflag = "(RM)";
        } else if (flag == 1) {
            clflag = "(U)";
        } else if (flag == 0) {
            clflag = "(R)";
        } else {
            clflag = "";
        }
        return clflag;
    }

    /**
	 * Returns the name of the status picture of the user.
	 * 
	 * @param flags flags
	 * @return user-status-picture
	 */
    private String getUserStatusPic(int flags) {
        String playergif = "player.gif";
        if (flags == 0) playergif = "player.gif";
        if ((flags == 8) || (flags == 9) || (flags == 12) || (flags == 13) || (flags == 24) || (flags == 25) || (flags == 28) || (flags == 29) || (flags == 40) || (flags == 41) || (flags == 44) || (flags == 45) || (flags == 56) || (flags == 57)) playergif = "away.gif";
        if ((flags == 16) || (flags == 17) || (flags == 20) || (flags == 21)) playergif = "mutemicro.gif";
        if ((flags == 32) || (flags == 33) || (flags == 36) || (flags == 37) || (flags == 48) || (flags == 49) || (flags == 52) || (flags == 53)) playergif = "mutespeakers.gif";
        if (flags == 4) playergif = "player.gif";
        if ((flags == 1) || (flags == 5)) playergif = "channelcommander.gif";
        if (flags >= 64) playergif = "record.gif";
        return playergif;
    }

    /**
	 * This method is one of the main parts. The method connects via socket to the teamspeak server, and sends the "pl" command with the required parameter (server UDP-port) to get the playerlist.
	 * 
	 * @return a vector of user
	 */
    private Vector<User> getUserInfo() {
        Vector<User> pList = null;
        Socket socket = null;
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            socket = new Socket(serverAddress, serverQueryPort);
            socket.setReceiveBufferSize(4069);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String line = in.readLine();
            while (line != null) {
                if (line.equals("[TS]")) {
                    out.write("pl " + serverUDPPort + "\nquit\n");
                    out.flush();
                    line = in.readLine();
                    line = in.readLine();
                    StringTokenizer tokenizer;
                    pList = new Vector<User>();
                    while ((line != null) && (!line.equals("OK"))) {
                        tokenizer = new StringTokenizer(line, "	");
                        while (tokenizer.hasMoreTokens()) {
                            pList.add(new User());
                            pList.lastElement().setP_id(Integer.parseInt(tokenizer.nextToken()));
                            pList.lastElement().setC_id(Integer.parseInt(tokenizer.nextToken()));
                            pList.lastElement().setPs(Integer.parseInt(tokenizer.nextToken()));
                            pList.lastElement().setBs(Integer.parseInt(tokenizer.nextToken()));
                            pList.lastElement().setPr(Integer.parseInt(tokenizer.nextToken()));
                            pList.lastElement().setBr(Integer.parseInt(tokenizer.nextToken()));
                            pList.lastElement().setPl(Integer.parseInt(tokenizer.nextToken()));
                            pList.lastElement().setPing(Integer.parseInt(tokenizer.nextToken()));
                            pList.lastElement().setLogintime(Integer.parseInt(tokenizer.nextToken()));
                            pList.lastElement().setIdletime(Integer.parseInt(tokenizer.nextToken()));
                            pList.lastElement().setCprivs(Integer.parseInt(tokenizer.nextToken()));
                            pList.lastElement().setPprivs(Integer.parseInt(tokenizer.nextToken()));
                            pList.lastElement().setPflags(Integer.parseInt(tokenizer.nextToken()));
                            pList.lastElement().setAttributes(this.getPlayerFlags(pList.lastElement().getPprivs(), pList.lastElement().getCprivs()));
                            pList.lastElement().setIp(tokenizer.nextToken());
                            pList.lastElement().setNick(deleteQuotes(tokenizer.nextToken()));
                            pList.lastElement().setLoginname(tokenizer.nextToken());
                            pList.lastElement().setStatusPicture(this.getUserStatusPic(pList.lastElement().getPflags()));
                            line = in.readLine();
                        }
                    }
                }
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pList;
    }

    /**
	 * This method is one of the main parts. The method connects via socket to the teamspeak server, and sends the "cl" command with the required parameter (server UDP-port) to get the channellist.
	 * 
	 * @return a vector of channel
	 */
    private Vector<Channel> getChannelInfo() {
        Vector<Channel> cList = null;
        Socket socket = null;
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            socket = new Socket(serverAddress, serverQueryPort);
            socket.setReceiveBufferSize(4069);
        } catch (UnknownHostException e) {
        } catch (IOException e) {
        } catch (Exception e) {
        }
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String line = in.readLine();
            while (line != null) {
                if (line.equals("[TS]")) {
                    out.write("cl " + serverUDPPort + "\nquit\n");
                    out.flush();
                    line = in.readLine();
                    line = in.readLine();
                    StringTokenizer tokenizer;
                    cList = new Vector<Channel>();
                    while ((line != null) && (!line.equals("OK"))) {
                        tokenizer = new StringTokenizer(line, "	");
                        while (tokenizer.hasMoreTokens()) {
                            cList.add(new Channel());
                            cList.lastElement().setId(Integer.parseInt(tokenizer.nextToken()));
                            cList.lastElement().setCodec(Integer.parseInt(tokenizer.nextToken()));
                            cList.lastElement().setCodecStr(getVerboseCodec(cList.lastElement().getCodec()));
                            cList.lastElement().setParent(Integer.parseInt(tokenizer.nextToken()));
                            cList.lastElement().setOrder(Integer.parseInt(tokenizer.nextToken()));
                            cList.lastElement().setMaxusers(Integer.parseInt(tokenizer.nextToken()));
                            cList.lastElement().setName(deleteQuotes(tokenizer.nextToken()));
                            cList.lastElement().setFlags(Integer.parseInt(tokenizer.nextToken()));
                            cList.lastElement().setAttributes(this.getChannelFlags(cList.lastElement().getFlags()));
                            cList.lastElement().setPassword(tokenizer.nextToken());
                            cList.lastElement().setTopic(deleteQuotes(tokenizer.nextToken()));
                            line = in.readLine();
                        }
                    }
                }
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(cList);
        return cList;
    }

    /**
	 * This method returns a string without quotes at the start and the end.
	 * 
	 * @param text text
	 * @return quoteless text
	 */
    private String deleteQuotes(String text) {
        if (text.startsWith("\"")) {
            text = text.substring(1);
        }
        if (text.endsWith("\"")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    /**
	 * This method returns the sub-channel and the user. The call is recursive.
	 * 
	 * @param channelId channel id
	 * @param subCounter subcounter
	 * @return subchannel
	 */
    public String getSubChannel(int channelId, int subCounter) {
        String subChannel = "";
        if (channelId == -1) {
            subCounter = 0;
        } else {
            subCounter++;
        }
        for (Channel channelInfo : channel) {
            if (channelInfo.getParent() == channelId) {
                subChannel = subChannel + "<div class=\"ts2Channel\">";
                int width = 32;
                String gitter = "";
                for (int i = 1; i <= subCounter; i++) {
                    width += 16;
                    gitter = gitter + "<img class=\"ts2Grid\" src=\"" + pictureBase + "/gitter.gif\" alt=\"\" />";
                }
                subChannel = subChannel + "    <div class=\"ts2GridChannel\" style=\"width:" + width + "px ;\">" + gitter + "<img class=\"ts2GridPic\" src=\"" + pictureBase + "/gitter2.gif\" alt=\"\" /><img class=\"ts2ChannelPic\" src=\"" + pictureBase + "/channel.gif\" alt=\"\" /></div>\n";
                subChannel = subChannel + "    <div class=\"ts2ChannelLink\">&nbsp;<a class=\"channellink\" href=\"teamspeak://" + this.serverAddress + ":" + this.serverUDPPort + "/?channel=" + channelInfo.getName() + "?nickname=" + this.loginname + "?password=" + this.serverPassword + "\" title=\"" + channelInfo.getTopic() + "\">" + channelInfo.getName() + "</a></div>\n";
                if (subCounter == 0) subChannel = subChannel + ("    <div class=\"ts2ChannelFlags\">&nbsp;" + channelInfo.getAttributes() + "</div>\n");
                subChannel = subChannel + "</div>\n";
                if (subChannel != "") subChannel = subChannel + getSubChannel(channelInfo.getId(), subCounter);
                subChannel = subChannel + getUser(channelInfo.getId(), channelInfo.getId(), subCounter);
            }
        }
        return subChannel;
    }

    /**
	 * Returns an user with all infos.
	 * 
	 * @param channelId channelId
	 * @param channelId2 channelId2
	 * @param subCounter subCounter
	 * @return an user
	 */
    private final String getUser(int channelId, int channelId2, int subCounter) {
        String player = "";
        for (User userInfo : user) {
            if (userInfo.getC_id() == channelId) {
                player = player + "<div class=\"ts2User\">";
                int width = 32;
                String gitter = "";
                for (int i = 0; i <= subCounter; i++) {
                    width += 16;
                    gitter = gitter + "<img class=\"ts2Grid\" src=\"" + pictureBase + "/gitter.gif\" alt=\"\" />";
                }
                player = player + "   " + "<div class=\"ts2UserGrid\" style=\"width:" + width + "px;\">" + gitter + "" + "<img class=\"ts2GridPic\" src=\"" + pictureBase + "/gitter2.gif\" alt=\"\" />" + "<img src=\"" + pictureBase + "/" + userInfo.getStatusPicture() + "\" alt=\"Time [online: " + userInfo.getLogintime() + " | idle: " + userInfo.getIdletime() + "] Ping: " + userInfo.getPing() + "\" />";
                player = player + "</div>";
                player = player + "<div class=\"ts2UserInfo\" title=\"Time [online: " + userInfo.getLogintime() + " | idle: " + userInfo.getIdletime() + "] Ping: " + userInfo.getPing() + "ms\">" + "&nbsp;" + userInfo.getNick() + " " + userInfo.getAttributes() + "" + "</div>";
                player = player + "</div>";
            }
        }
        return player;
    }

    /**
	 * @return the channel
	 */
    public final Vector<Channel> getChannel() {
        return channel;
    }

    /**
	 * @param channel the channel to set
	 */
    public final void setChannel(Vector<Channel> channel) {
        this.channel = channel;
    }

    /**
	 * @return the user
	 */
    public final Vector<User> getUser() {
        return user;
    }

    /**
	 * @param user the user to set
	 */
    public final void setUser(Vector<User> user) {
        this.user = user;
    }
}
