import java.util.regex.*;

public class BlitzScript {

    private static final boolean dontJsEscape = false;

    private static final boolean doJsEscape = true;

    public static String getServerURL() {
        StringBuilder httpToServer = new StringBuilder(BlitzServer.serverprefix);
        httpToServer.append(BlitzServer.localhost);
        httpToServer.append(":");
        httpToServer.append(BlitzServer.port);
        return httpToServer.toString();
    }

    /**
	 * Sets all the server variables and the user attributes within a particular
	 * page being sent back to a browser.
	 * @param aSessionUsername The current user logged in
	 * @param strRequest The request being sent back
	 * @return the compiled page
	 */
    public static StringBuilder setPageVariables(String aSessionUsername, StringBuilder strRequest) {
        String aRequest = strRequest.toString();
        aRequest = aRequest.replaceAll("\\$server.url\\$", getServerURL());
        aRequest = aRequest.replaceAll("\\$server.port\\$", Integer.toString(BlitzServer.port));
        aRequest = aRequest.replaceAll("\\$server.name\\$", BlitzServer.localhost);
        aRequest = aRequest.replaceAll("\\$server.refreshrate\\$", Long.toString(BlitzServer.pagerefresh));
        if (aSessionUsername != null && aSessionUsername.length() > 0 && BlitzServer.ConnectedUsers.containsKey(aSessionUsername)) {
            BlitzUser aUser = (BlitzUser) BlitzServer.ConnectedUsers.get(aSessionUsername);
            aRequest = aRequest.replaceAll("\\$user.id\\$", Long.toString(aUser.id));
            aRequest = aRequest.replaceAll("\\$user.username\\$", aUser.username);
            aRequest = aRequest.replaceAll("\\$user.firstname\\$", aUser.firstname);
            aRequest = aRequest.replaceAll("\\$user.lastname\\$", aUser.lastname);
            aRequest = aRequest.replaceAll("\\$user.gender\\$", aUser.gender);
            aRequest = aRequest.replaceAll("\\$user.email\\$", aUser.email);
            aRequest = aRequest.replaceAll("\\$user.language\\$", aUser.language);
            aRequest = aRequest.replaceAll("\\$user.recv_textcolor\\$", Integer.toString(aUser.recvTextColor));
            aRequest = aRequest.replaceAll("\\$user.recv_textsize\\$", Integer.toString(aUser.recvTextSize));
            aRequest = aRequest.replaceAll("\\$user.recv_textfont\\$", Integer.toString(aUser.recvTextFont));
            aRequest = aRequest.replaceAll("\\$user.send_textcolor\\$", Integer.toString(aUser.sendTextColor));
            aRequest = aRequest.replaceAll("\\$user.send_textsize\\$", Integer.toString(aUser.sendTextSize));
            aRequest = aRequest.replaceAll("\\$user.send_textfont\\$", Integer.toString(aUser.sendTextFont));
            aRequest = aRequest.replaceAll("\\$user.requestorid\\$", Long.toString(aUser.requestorid));
        }
        return new StringBuilder(aRequest);
    }

    public static StringBuilder compilePage(String aSessionUsername, StringBuilder strRequest) {
        BlitzUser aUser = new BlitzUser();
        if (aSessionUsername != null && aSessionUsername.length() > 0) {
            if (BlitzServer.ConnectedUsers.containsKey(aSessionUsername)) {
                aUser = (BlitzUser) BlitzServer.ConnectedUsers.get(aSessionUsername);
            } else {
                aUser.populate(aSessionUsername);
            }
        } else {
            aUser.language = BlitzServer.defaultLanguage;
        }
        strRequest = deString("(\\$(str[a-zA-Z]*)\\$)", strRequest, aUser.language, dontJsEscape);
        strRequest = deString("(\\$jsEscape\\((str[a-zA-Z]*)\\)\\$)", strRequest, aUser.language, doJsEscape);
        return strRequest;
    }

    private static StringBuilder deString(String aToken, StringBuilder aPage, String aLangCode, boolean jsEscape) {
        Pattern p = Pattern.compile(aToken);
        Matcher m = p.matcher(aPage);
        String aCultString = "";
        try {
            BlitzCulture aCulture = (BlitzCulture) BlitzCulture.loadedCultures.get(aLangCode);
            while (m.find()) {
                if (jsEscape) {
                    aCultString = aCulture.getString(m.group(2));
                    aCultString = _jsEscapeValue(aCultString);
                } else {
                    aCultString = aCulture.getString(m.group(2));
                }
                if (BlitzServer.labelStrings == 1) aPage.replace(m.start(1), m.end(1), aCultString + "[" + m.group(2) + "]"); else aPage.replace(m.start(1), m.end(1), aCultString);
            }
        } catch (Exception e) {
            ;
        }
        return aPage;
    }

    private static String _jsEscapeValue(String aStr) {
        try {
            aStr = aStr.replace("'", "\'");
            aStr = aStr.replace("\"", "\\\"");
            aStr = "\"" + aStr + "\"";
        } catch (Exception e) {
            Log.severe("BlitzScript::_jsEscapeValue(" + aStr + ") -> Caught an exception : " + e);
        }
        return aStr;
    }
}
