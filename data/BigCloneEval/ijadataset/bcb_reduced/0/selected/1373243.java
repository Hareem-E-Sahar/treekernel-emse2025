package webirc.client;

import java.util.HashMap;
import java.util.Vector;

/**
 * This class represents user's info in the chat. Also User can hold server name
 * in the nickName field.
 * @author Ayzen
 * @version 1.0 07.07.2006 22:56:22
 */
public class User {

    public static final char TYPE_CHANOWNER = '~';

    public static final char TYPE_ADMIN = '&';

    public static final char TYPE_OPERATOR = '@';

    public static final char TYPE_HALFOP = '%';

    public static final char TYPE_VOICE = '+';

    public static final char TYPE_NORMAL = ' ';

    private static final HashMap typesWeight = new HashMap();

    static {
        typesWeight.put(new Character(TYPE_NORMAL), new Integer(0));
        typesWeight.put(new Character(TYPE_VOICE), new Integer(1));
        typesWeight.put(new Character(TYPE_HALFOP), new Integer(2));
        typesWeight.put(new Character(TYPE_OPERATOR), new Integer(3));
        typesWeight.put(new Character(TYPE_ADMIN), new Integer(4));
        typesWeight.put(new Character(TYPE_CHANOWNER), new Integer(5));
    }

    private String nickname;

    private String userName;

    private String hostName;

    private String realName;

    private char type = TYPE_NORMAL;

    private String chanModes = "";

    public User() {
    }

    public User(String nickname) {
        String parsedNick = parseNick(nickname);
        this.nickname = parsedNick;
        this.realName = parsedNick;
        this.userName = parsedNick;
    }

    public User(String nickname, String userName, String hostName) {
        this.nickname = parseNick(nickname);
        this.userName = userName;
        this.hostName = hostName;
    }

    public User(String nickname, String userName, String hostName, String realName) {
        this.userName = userName;
        this.nickname = parseNick(nickname);
        this.hostName = hostName;
        this.realName = realName;
    }

    public void clear() {
        userName = null;
        nickname = null;
        hostName = null;
        realName = null;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    private String parseNick(String nickname) {
        char firstChar = nickname.charAt(0);
        if (firstChar == TYPE_OPERATOR || firstChar == TYPE_VOICE || firstChar == TYPE_HALFOP || firstChar == TYPE_ADMIN || firstChar == TYPE_CHANOWNER) {
            switch(firstChar) {
                case '~':
                    addMode('q');
                    break;
                case '&':
                    addMode('a');
                    break;
                case '@':
                    addMode('o');
                    break;
                case '%':
                    addMode('h');
                    break;
                case '+':
                    addMode('v');
                    break;
            }
            return nickname.substring(1);
        }
        return nickname;
    }

    private char getModePrefix(char mode) {
        switch(mode) {
            case 'q':
                return '~';
            case 'a':
                return '&';
            case 'o':
                return '@';
            case 'h':
                return '%';
            case 'v':
                return '+';
        }
        return 0;
    }

    public void addMode(char mode) {
        if (chanModes.indexOf(mode) == -1) {
            char prefix = getModePrefix(mode);
            if (prefix == 0) return;
            chanModes += mode;
            int typeWeight = ((Integer) typesWeight.get(new Character(type))).intValue();
            int newTypeWeight = ((Integer) typesWeight.get(new Character(prefix))).intValue();
            if (newTypeWeight > typeWeight) type = prefix;
        }
    }

    public void removeMode(char mode) {
        int modeIndex = chanModes.indexOf(mode);
        if (modeIndex != -1) {
            char prefix = getModePrefix(mode);
            if (prefix == 0) return;
            chanModes = chanModes.replaceAll(String.valueOf(mode), "");
            if (type == prefix) {
                char newType = TYPE_NORMAL;
                int maxWeight = 0;
                for (int i = 0; i < chanModes.length(); i++) {
                    char modePrefix = getModePrefix(chanModes.charAt(i));
                    int weight = ((Integer) typesWeight.get(new Character(modePrefix))).intValue();
                    if (weight > maxWeight) {
                        newType = modePrefix;
                        maxWeight = weight;
                    }
                }
                type = newType;
            }
        }
    }

    public static String getModeName(char mode) {
        switch(mode) {
            case 'q':
                return WebIRC.mainMessages.channelOwner();
            case 'a':
                return WebIRC.mainMessages.admin();
            case 'o':
                return WebIRC.mainMessages.operator();
            case 'h':
                return WebIRC.mainMessages.halfOperator();
            case 'v':
                return WebIRC.mainMessages.voice();
        }
        return null;
    }

    public static int insertUser(Vector users, User user) {
        int insertStart = 0;
        int insertEnd = users.size();
        User middleUser;
        while (insertStart != insertEnd) {
            int middle;
            if (insertStart + 1 != insertEnd) middle = (insertStart + insertEnd) / 2; else middle = insertStart;
            middleUser = (User) users.get(middle);
            if (user.compare(middleUser)) insertEnd = middle; else {
                if (insertStart == middle) insertStart = insertEnd; else insertStart = middle;
            }
        }
        users.insertElementAt(user, insertStart);
        return insertStart;
    }

    public int hashCode() {
        return nickname.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof User)) return false;
        User user = (User) o;
        return !(nickname != null ? !nickname.equalsIgnoreCase(user.nickname) : user.nickname != null);
    }

    public String toString() {
        return nickname;
    }

    public boolean compare(User user) {
        int thisTypeWeight = ((Integer) typesWeight.get(new Character(type))).intValue();
        int userTypeWeight = ((Integer) typesWeight.get(new Character(user.getType()))).intValue();
        if (thisTypeWeight != userTypeWeight && thisTypeWeight > userTypeWeight) return true; else {
            if (thisTypeWeight == userTypeWeight) return (nickname.toLowerCase().compareTo(user.getNickname().toLowerCase()) < 0); else return false;
        }
    }
}
