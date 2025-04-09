package d20chat;

import java.util.regex.*;

/**
 * Takes a client side message and checks it, reformats it, and handles it if it's only client side or sends it on.
 * If Server is not yet started messsages go straight to IncomingMessageParser.
 */
public class OutgoingMessageParser {

    /**
     * the Client class being used, set in constructer.
     */
    private Client tempclient;

    /**
     * the d20chatui class being used, set in constructer.
     */
    private d20chatui tempui;

    /**
     * the D20Chat class being used, set in constructer.
     */
    private D20Chat theMain;

    /**
     * the message string as it is sent to the class.
     */
    private MessageInfo editMessage;

    /**
     * the message string as it is sent to the class.
     */
    private String unparsedMessage;

    /**
     * the message string as the class sends it out.
     */
    private String parsedMessage;

    /**
     * holds html formatting for the message.
     */
    private String format;

    /**
     * for keeping track of afk status.
     */
    private boolean away = false;

    /**
     * Checks for slash command, if no slash command appends /say all.
     */
    private void checkSay() {
        boolean slashCommand = unparsedMessage.startsWith("/");
        if (!slashCommand) {
            unparsedMessage = "/say all " + unparsedMessage;
        }
    }

    /**
     * Finds a math expression, evaluates it with StringMath, and adds the solution to the expression.
     * Math expressions are anything between [ and ].
     */
    private void doMath() {
        StringMath convert = new StringMath();
        String math = "";
        String value;
        String newString = "";
        int pos = 0;
        int length;
        Pattern mathPattern = Pattern.compile("\\[.*?\\]");
        Matcher matcher = mathPattern.matcher(editMessage.text);
        try {
            while (matcher.find()) {
                math = editMessage.text.substring(matcher.start(), matcher.end());
                value = convert.eval(math);
                newString = newString + editMessage.text.substring(pos, matcher.end() - 1) + value + "]";
                pos = matcher.end();
            }
            length = editMessage.text.length();
            if (pos >= editMessage.text.length()) {
                editMessage.text = newString;
            } else {
                editMessage.text = newString + editMessage.text.substring(pos, length);
            }
            editMessage.text = editMessage.text.replaceAll("\\<", "&lt;");
            editMessage.text = editMessage.text.replaceAll("\\>", "&gt;");
        } catch (ArithmeticException ae) {
            editMessage.subType = "systemMessage";
            editMessage.text = math + " " + ae.getMessage();
            editMessage.text = editMessage.text.replaceAll("\\<", "&lt;");
            editMessage.text = editMessage.text.replaceAll("\\>", "&gt;");
        }
    }

    /**
     * Deconstructs message, calls methods to check message format, reconstructs message in correct form.
     * Checks a message for correct format, adds html, appends any info needed for server
     */
    private void formatContent() {
        stripHTML();
        format = "<span style=\"" + "font-family:\'" + theMain.theSettings.chatFont + "\',sans-serif" + ";font-size:" + theMain.theSettings.chatSize + ";color:rgb(" + theMain.theSettings.talkColor.red + "," + theMain.theSettings.talkColor.green + "," + theMain.theSettings.talkColor.blue + ")" + ";\">";
        checkSay();
        stripSlash();
        editMessage.seperate(unparsedMessage);
        editMessage.state = editMessage.subType;
        editMessage.targetName = editMessage.type;
        editMessage.subType = editMessage.originName;
        editMessage.originName = theMain.theUser.getUsername();
        if (editMessage.subType.equalsIgnoreCase("help") || editMessage.subType.equalsIgnoreCase("h")) {
            editMessage.type = "client";
            editMessage.subType = "help";
            BareBonesBrowserLaunch.openURL("http://sourceforge.net/forum/forum.php?forum_id=622940");
        } else if (systemMessage()) {
        } else if (afk()) {
        } else if (say()) {
        } else if (emote()) {
        } else if (tell()) {
        } else if (hide()) {
        } else if (silence()) {
        } else if (charactersheet()) {
        } else if (kick()) {
        } else if (makeGM()) {
        } else if (setPassword()) {
        } else if (changeName()) {
        } else if (userInfo()) {
        } else if (hideProfile()) {
        } else if (password()) {
        } else if (profile()) {
        } else if (charNameChange()) {
        } else if (sendcharsheet()) {
        } else if (charsheet()) {
        } else if (clear()) {
        } else {
            editMessage.type = "chat";
            editMessage.subType = "systemMessage";
            editMessage.text = "Unknown command : " + editMessage.message;
        }
        parsedMessage = editMessage.toString();
    }

    /**
     * Checks if a message is for everyone in the room, and corrects the message components.
     * /say equivalents: /s, /amsg, /g, /c, /p
     * @return boolean true for a /say command, false otherwise
     */
    private boolean say() {
        if (editMessage.subType.equalsIgnoreCase("say") || editMessage.subType.equalsIgnoreCase("s") || editMessage.subType.equalsIgnoreCase("amsg") || editMessage.subType.equalsIgnoreCase("g") || editMessage.subType.equalsIgnoreCase("c") || editMessage.subType.equalsIgnoreCase("p")) {
            editMessage.subType = "say";
            editMessage.type = "chat";
            if (editMessage.targetName.equals("all")) {
                editMessage.text = editMessage.message.substring(editMessage.endOfWord2);
            } else {
                editMessage.targetName = "all";
                editMessage.text = editMessage.message.substring(editMessage.endOfWord1);
            }
            doMath();
            editMessage.text = format + editMessage.text + "</span>";
            return true;
        }
        return false;
    }

    /**
     *    Checks if a message is for everyone in the room and represents an action, and corrects the message components.
     *    /emote equivalents: /e, /em, /me
     * @return boolean true for a /emote command, false otherwise
     */
    private boolean emote() {
        if (editMessage.subType.equalsIgnoreCase("emote") || editMessage.subType.equalsIgnoreCase("e") || editMessage.subType.equalsIgnoreCase("em") || editMessage.subType.equalsIgnoreCase("me")) {
            editMessage.subType = "emote";
            editMessage.type = "chat";
            if (editMessage.targetName.equals("all")) {
                editMessage.text = editMessage.message.substring(editMessage.endOfWord2);
            } else {
                editMessage.targetName = "all";
                editMessage.text = editMessage.message.substring(editMessage.endOfWord1);
            }
            doMath();
            editMessage.text = format + editMessage.text + "</span>";
            return true;
        }
        return false;
    }

    /**
     * Checks if a message is for one person, and corrects the message components.
     * /tell equivalents: /t, /pm, /msg, /message, /whisper, /w, /send
     * @return boolean true for a /tell command, false otherwise
     */
    private boolean tell() {
        if (editMessage.subType.equalsIgnoreCase("tell") || editMessage.subType.equalsIgnoreCase("t") || editMessage.subType.equalsIgnoreCase("pm") || editMessage.subType.equalsIgnoreCase("msg") || editMessage.subType.equalsIgnoreCase("message") || editMessage.subType.equalsIgnoreCase("whisper") || editMessage.subType.equalsIgnoreCase("w") || editMessage.subType.equalsIgnoreCase("send")) {
            if (editMessage.targetName.isEmpty()) {
                editMessage.type = "chat";
                editMessage.subType = "systemMessage";
                editMessage.text = "No name was entered in the tell command.";
            } else {
                editMessage.subType = "tell";
                editMessage.type = "chat";
                editMessage.text = editMessage.message.substring(editMessage.endOfWord2);
                doMath();
                editMessage.text = format + editMessage.text + "</span>";
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if a message is visible only to the sender, and corrects the message components.
     * Description: for sending messages to the user's self
     * Mostly useful for rolling dice.
     * /hide equivalents: /echo and /hidden
     * @return boolean true for a /hide command, false otherwise
     */
    private boolean hide() {
        if (editMessage.subType.equalsIgnoreCase("hide") || editMessage.subType.equalsIgnoreCase("echo") || editMessage.subType.equals("hidden")) {
            editMessage.subType = "hide";
            editMessage.text = editMessage.message.substring(editMessage.endOfWord1);
            doMath();
            editMessage.type = "chat";
            editMessage.targetName = "you";
            editMessage.text = format + editMessage.text + "</span>";
            return true;
        }
        return false;
    }

    /**
     * Checks for a message meant to inform user of the state of the system, and corrects the message compontents.
     * @return boolean true for a /systemMessage command, false otherwise
     */
    private boolean systemMessage() {
        if (editMessage.subType.equalsIgnoreCase("systemMessage")) {
            editMessage.subType = "systemMessage";
            editMessage.type = "chat";
            editMessage.targetName = "you";
            editMessage.text = editMessage.message.substring(editMessage.endOfWord1);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if a message is to allow or disallow the players to speak, and corrects the message compontents.
     * @return boolean true for a /silence command, false otherwise
     */
    private boolean silence() {
        if (editMessage.subType.equalsIgnoreCase("silence")) {
            editMessage.subType = "silence";
            editMessage.type = "command";
            if (editMessage.targetName.equalsIgnoreCase("on")) {
                editMessage.targetName = "all";
                editMessage.text = "on";
            } else if (editMessage.targetName.equalsIgnoreCase("off")) {
                editMessage.targetName = "all";
                editMessage.text = "off";
            } else if (editMessage.state.equalsIgnoreCase("on") || editMessage.state.equalsIgnoreCase("off")) {
                editMessage.text = editMessage.state;
            } else if (editMessage.targetName.isEmpty()) {
                editMessage.targetName = "all";
                editMessage.text = "on";
            } else {
                editMessage.text = "on";
            }
            return true;
        } else if (editMessage.subType.equalsIgnoreCase("unsilence")) {
            editMessage.type = "command";
            editMessage.subType = "silence";
            editMessage.text = "off";
            if (editMessage.targetName.isEmpty()) {
                editMessage.targetName = "all";
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if the message is a command to make another player the GM, and corrects the message components.
     * /makeGM equivalents: /make_gm, /makeLeader, /make_leader, /promote, /pr
     * @return boolean true for a /makeGM command, false otherwise
     */
    private boolean makeGM() {
        if (editMessage.subType.equalsIgnoreCase("makeGM") || editMessage.subType.equalsIgnoreCase("make_gm") || editMessage.subType.equalsIgnoreCase("makeLeader") || editMessage.subType.equalsIgnoreCase("make_leader") || editMessage.subType.equalsIgnoreCase("promote") || editMessage.subType.equalsIgnoreCase("pr")) {
            editMessage.subType = "makeGM";
            if (editMessage.targetName.isEmpty()) {
                editMessage.type = "chat";
                editMessage.subType = "systemMessage";
                editMessage.text = "No name was entered in the makeGM command.";
            } else {
                editMessage.type = "command";
                editMessage.text = "blah";
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if it's a message to remove unwanted players from room, and corrects the message components.
     * /kick equivalents: /uninvite, /u, /un
     * @return boolean true for a /kick command, false otherwise
     */
    private boolean kick() {
        if (editMessage.subType.equalsIgnoreCase("kick") || editMessage.subType.equalsIgnoreCase("univite") || editMessage.subType.equalsIgnoreCase("u") || editMessage.subType.equalsIgnoreCase("un")) {
            editMessage.subType = "kick";
            if (editMessage.targetName.isEmpty()) {
                editMessage.type = "chat";
                editMessage.subType = "systemMessage";
                editMessage.text = "No name was entered in the kick command.";
            } else {
                editMessage.type = "command";
                editMessage.text = "now";
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if it's a message to give the server a new password, and corrects the message components.
     * @return boolean true for a /setPassword command, false otherwise
     */
    private boolean setPassword() {
        if (editMessage.subType.equalsIgnoreCase("setPassword")) {
            editMessage.type = "command";
            editMessage.subType = "setPassword";
            if (editMessage.targetName.isEmpty()) {
                editMessage.targetName = "all";
                editMessage.text = "none";
                theMain.serverPassword = null;
            } else {
                editMessage.text = editMessage.targetName;
                editMessage.targetName = editMessage.type;
                theMain.serverPassword = editMessage.text;
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if it's a message to submit a password to the Server for admitance to the room, and corrects the message components.
     * The form when typed is /password <the password>.
     * @return boolean true for a /password command, false otherwise
     */
    private boolean password() {
        if (editMessage.subType.equalsIgnoreCase("password")) {
            editMessage.subType = "password";
            editMessage.type = "request";
            editMessage.text = editMessage.targetName;
            return true;
        }
        return false;
    }

    /**
     * Checks if it's a message for making sheets viewable or not, and corrects message components.
     * @return boolean true for a /charactersheet command, false otherwise
     */
    private boolean charactersheet() {
        if (editMessage.subType.equalsIgnoreCase("charactersheet")) {
            editMessage.subType = "charactersheet";
            editMessage.type = "command";
            if (editMessage.targetName.equals("on") || editMessage.state.equals("on")) {
                editMessage.text = "on";
                editMessage.targetName = "all";
            } else if (editMessage.targetName.equalsIgnoreCase("off") || editMessage.state.equalsIgnoreCase("off")) {
                editMessage.text = "off";
                editMessage.targetName = "all";
            } else {
                editMessage.type = "chat";
                editMessage.subType = "systemMessage";
                editMessage.text = "To turn on/off character sheets enter : /charactersheet on OR /charactersheet off.";
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if it's a message that sends the username and profile on to the server, and corrects message components.
     * @return boolean true for a /userInfo command, false otherwise
     */
    private boolean userInfo() {
        if (editMessage.subType.equals("userInfo")) {
            editMessage.type = "request";
            editMessage.targetName = "blah";
            editMessage.text = editMessage.message.substring(editMessage.endOfWord1);
            return true;
        }
        return false;
    }

    /**
     * Checks if it's a request message from a user to see another user's profile, and corrects message contents.
     * /profile equivalents: /inspect, /ins, /who, and /whois
     * @return boolean true for a /profile command, false otherwise
     */
    private boolean profile() {
        if (editMessage.subType.equalsIgnoreCase("profile") || editMessage.subType.equalsIgnoreCase("inspect") || editMessage.subType.equalsIgnoreCase("ins") || editMessage.subType.equalsIgnoreCase("who") || editMessage.subType.equalsIgnoreCase("whois")) {
            editMessage.subType = "profile";
            editMessage.type = "request";
            editMessage.text = "please";
            return true;
        }
        return false;
    }

    /**
     * Checks if it's a message to the Server saying that the profile shouldn't be shared, and corrects message components.
     * @return boolean true for a /hideProfile command, false otherwise
     */
    private boolean hideProfile() {
        if (editMessage.subType.equals("hideProfile")) {
            editMessage.type = "request";
            theMain.theUser.userProfile.hide();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if it's a message to change the user's characater name.
     * @return boolean true for a /charNameChange command, false otherwise
     */
    private boolean charNameChange() {
        if (editMessage.subType.equalsIgnoreCase("charNameChange")) {
            if (editMessage.targetName.isEmpty()) {
                editMessage.type = "chat";
                editMessage.subType = "systemMessage";
                editMessage.text = "No name was entered in the charNameChange command.";
            } else {
                editMessage.subType = "charNameChange";
                editMessage.type = "request";
                editMessage.targetName = editMessage.originName;
                theMain.theUser.setCharacterName(editMessage.message.substring(editMessage.endOfWord1));
                editMessage.text = "薔" + editMessage.message.substring(editMessage.endOfWord1) + "薔";
            }
            return true;
        }
        return false;
    }

    /**
     * If it's a afk message, toggles the user's charactername to or from displaying afk.
     * Reformatted to look like a charNameChange message.
     * /afk equivalent: /away
     * @return boolean true for a /afk command, false otherwise
     */
    private boolean afk() {
        if (editMessage.subType.equalsIgnoreCase("afk") || editMessage.subType.equalsIgnoreCase("away")) {
            editMessage.subType = "charNameChange";
            editMessage.type = "request";
            editMessage.targetName = editMessage.originName;
            if (away == false) {
                away = true;
                editMessage.text = "薔" + theMain.theUser.getCharacterName() + " - afk薔";
            } else {
                away = false;
                editMessage.text = "薔" + theMain.theUser.getCharacterName() + "薔";
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if it's a message to change the user name of a player, changes the username, and corrects message contents.
     * /changeName equivalents: /name and /nick
     * @return boolean true for a /changeName command, false otherwise
     */
    private boolean changeName() {
        if (editMessage.subType.equalsIgnoreCase("changeName") || editMessage.subType.equalsIgnoreCase("name") || editMessage.subType.equalsIgnoreCase("nick")) {
            if (editMessage.targetName.isEmpty()) {
                editMessage.type = "chat";
                editMessage.subType = "systemMessage";
                editMessage.text = "No name was entered in the changeName command.";
            } else {
                editMessage.subType = "changeName";
                editMessage.type = "request";
                editMessage.text = "blah";
                theMain.theUser.setUsername(editMessage.targetName);
                editMessage.targetName = theMain.theUser.getUsername();
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if it's a message to send a charactersheet to another player, and corrects message contents.
     * @return boolean true for a /sendcharsheet command, false otherwise
     */
    private boolean sendcharsheet() {
        if (editMessage.subType.equalsIgnoreCase("sendcharsheet")) {
            if (editMessage.targetName.isEmpty()) {
                editMessage.type = "chat";
                editMessage.subType = "systemMessage";
                editMessage.text = "No name was entered in the sendcharsheet command.";
            } else {
                editMessage.type = "request";
                editMessage.text = editMessage.message.substring(editMessage.endOfWord2);
                editMessage.subType = "sendcharsheet";
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if it's a command to clear the chat buffer, and if so clears the chat buffer.
     * This message sets type to client so it doesn't send a message to the Server.
     * @return boolean true for a /clear command, false otherwise
     */
    private boolean clear() {
        if (editMessage.subType.equalsIgnoreCase("clear") || editMessage.subType.equalsIgnoreCase("cls")) {
            editMessage.type = "client";
            theMain.gui.buffer.clearbuffer();
            tempui.tempchatwindowoutput("");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if it's a request from a player to see anther player's charactersheet, and corrects message contents.
     * @return boolean true for a /charsheet command, false otherwise
     */
    private boolean charsheet() {
        if (editMessage.subType.equalsIgnoreCase("charsheet")) {
            if (editMessage.targetName.isEmpty()) {
                editMessage.type = "chat";
                editMessage.subType = "systemMessage";
                editMessage.text = "No name was entered in the charsheet command.";
            } else {
                editMessage.type = "request";
                editMessage.text = "blah";
                editMessage.subType = "charsheet";
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Strips any text between < and > out of the message to remove html.
     */
    private void stripHTML() {
        unparsedMessage = unparsedMessage.replaceAll("\\<.*?\\>", "");
    }

    /**
     * Removes the slash in front of the slash command in a message.
     */
    private void stripSlash() {
        int pos;
        StringBuffer buffer;
        pos = unparsedMessage.indexOf('/');
        buffer = new StringBuffer(unparsedMessage);
        buffer.deleteCharAt(pos);
        unparsedMessage = buffer.toString();
    }

    /**
     * Handles a message from the client either performing some operation or passing the formatted message on.
     * @param message A String representing anything that the user or the machine wants to tell the Server or Client.
     */
    public void sendMessage(String message) {
        unparsedMessage = message + " ";
        editMessage = new MessageInfo();
        if (!message.isEmpty()) {
            formatContent();
            if (editMessage.type.equals("client")) {
            } else if (theMain.connected == true) {
                tempclient.SendMessage(parsedMessage);
            } else if ((editMessage.type.equals("chat") && !editMessage.subType.equals("tell"))) {
                theMain.InParse.CheckType(parsedMessage);
            } else {
                tempui.tempchatwindowoutput("You are not connected to a server.");
            }
        }
    }

    /**
     * Constructor, recieves other classes so they can be referred to.
     * @param gui The user interface.
     * @param clientthread the thread that sends messages to the server
     * @param thisD20Chat the main class
     */
    public OutgoingMessageParser(d20chatui gui, Client clientthread, D20Chat thisD20Chat) {
        tempclient = clientthread;
        tempui = gui;
        theMain = thisD20Chat;
    }
}
