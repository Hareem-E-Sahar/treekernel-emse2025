package alp;

import alp.Session.state;

/**
   <h2>COMMANDS</h2>
   <b>client2server single</b><br>
 *	protocolErrorReq<br>
 *	discReq<br>
 *	infoReq<br>

   <p><b>client2server double</b><br>
 *  keepAliveReq<br>
 p  keepAliveCnf<br>
	(UDP)serverQ<br>
	(UDP)serverR<br>

	<p><b>server2client single</b><br>
	protocolErrorInf<br>
	redirectInf<br>

	<p><b>server2client double</b><br>
 p  connInf<br>
 *  connRsp<br>
    controlInf<br>
 -  controlRsp<br>
    controlSmartCard<br>
 -  infoSmartCard<br>
	discInf<br>
 -  discRsp<br>
	keepAliveInf<br>
 -  keepAliveRsp<br>
<p>
// MAX COMMAND SIZE: 2048 byte!<br>
// commandX key1=value1 key2=value2 ... keyN=valueN<br>
// end: either '\0', '\r' or '\KeyOrValue'<br>
// white-space: ' ' or '\t'<br>
// command and key/value-pairs are separated by 1 or more white-space chars.<br>
// key1  =  value1 key2 = value2 key3=value3 is all valid<br>
// key4     (without =) means key4=true<br>
//SessionCmd key and value must not contain unescaped '=' or '\' or ' ' or '\t'<br>
// binary-data: escaped octal numbers (bytes)<br>
 * @author niki.waibel{@}gmx.net
 */
public final class SessionCmds {

    enum cause {

        redirect, insert, keepAliveExpiry, protocolErrorReq, redirectError, connError, discError, keepAliveError, controlError, protocolErrorError, controlSmartCardError
    }

    ;

    enum event {

        insert, remove
    }

    ;

    enum type {

        pseudo, TOUnknown, invalid, card
    }

    ;

    static final java.util.HashMap<String, String> parseSessionString(String str) throws java.text.ParseException {
        java.util.HashMap<String, String> strh = new java.util.HashMap<String, String>();
        java.util.ArrayList<String> stra = new java.util.ArrayList<String>();
        final String regex1 = "[^\\\\][ \\t]+";
        final String regex2 = "[^\\\\]=";
        final java.util.regex.Pattern p1 = java.util.regex.Pattern.compile(regex1);
        final java.util.regex.Pattern p2 = java.util.regex.Pattern.compile(regex2);
        final java.util.regex.Matcher m1 = p1.matcher(str);
        final String err = "SessionCmd(parser): ";
        final String err_ic = err + "Illegal Command (must not contain '=')";
        int prev = 0;
        while (m1.find()) {
            int a = m1.start();
            int b = m1.end() - 1;
            String s = str.substring(prev, a + 1);
            stra.add(s);
            prev = m1.end();
        }
        if (prev != str.length()) {
            stra.add(str.substring(prev, str.length()));
        }
        boolean cSet = false;
        boolean nSet = false;
        boolean oSet = false;
        boolean bSet = false;
        String KeyOrValue = null;
        for (String s : stra) {
            String prompt;
            if (s.equals("=")) {
                if (!cSet) {
                    throw new java.text.ParseException(err_ic, 0);
                }
                if (oSet) {
                    throw new java.text.ParseException(err + "detected '=' '=', but need '=' 'value'", 0);
                }
                if (bSet) {
                    throw new java.text.ParseException(err + "detected 'key=' '=', but need 'key=' 'value'", 0);
                }
                if (nSet) {
                    oSet = true;
                } else {
                    throw new java.text.ParseException(err + "detected '=', but no 'key' set", 0);
                }
                prompt = "O:";
            } else if (s.matches(".*[^\\\\]=")) {
                if (!cSet) {
                    throw new java.text.ParseException(err_ic, 0);
                }
                if (oSet) {
                    throw new java.text.ParseException(err + "detected '=' 'key=', but need '=' 'value'", 0);
                }
                if (bSet) {
                    throw new java.text.ParseException(err + "detected 'key=' 'key=', but need 'key=' 'value'", 0);
                }
                if (nSet) {
                    strh.put(unEscape(KeyOrValue), "true");
                }
                bSet = true;
                KeyOrValue = s.substring(0, s.length() - 1);
                prompt = "B:";
            } else if (s.matches("=..*")) {
                if (!cSet) {
                    throw new java.text.ParseException(err_ic, 0);
                }
                if (oSet) {
                    throw new java.text.ParseException(err + "detected '=' '=value', but need '=' 'value'", 0);
                }
                if (bSet) {
                    throw new java.text.ParseException(err + "detected 'key=' '=value', but need 'key=' 'value'", 0);
                }
                if (nSet) {
                    strh.put(unEscape(KeyOrValue), unEscape(s.substring(1)));
                    nSet = false;
                } else {
                    throw new java.text.ParseException(err + "detected '=value', but no 'key' set", 0);
                }
                prompt = "E:";
            } else if (s.matches(".*[^\\\\]=..*")) {
                if (!cSet) {
                    throw new java.text.ParseException(err_ic, 0);
                }
                if (oSet) {
                    throw new java.text.ParseException(err + "detected '=' 'key=value', but need '=' 'value'", 0);
                }
                if (bSet) {
                    throw new java.text.ParseException(err + "detected 'key=' 'key=value', but need 'key=' 'value'", 0);
                }
                if (nSet) {
                    strh.put(unEscape(KeyOrValue), "true");
                    nSet = false;
                }
                final java.util.regex.Matcher m2 = p2.matcher(s);
                m2.find();
                strh.put(unEscape(s.substring(0, m2.start() + 1)), unEscape(s.substring(m2.end(), s.length())));
                prompt = "m:";
            } else {
                if (!cSet) {
                    prompt = "C:";
                    strh.put(null, unEscape(s));
                    cSet = true;
                } else {
                    prompt = "N:";
                    if (nSet && oSet) {
                        strh.put(unEscape(KeyOrValue), unEscape(s));
                        nSet = false;
                        oSet = false;
                    } else if (bSet) {
                        strh.put(unEscape(KeyOrValue), unEscape(s));
                        bSet = false;
                    } else if (nSet) {
                        strh.put(unEscape(KeyOrValue), "true");
                        KeyOrValue = s;
                    } else {
                        nSet = true;
                        KeyOrValue = s;
                    }
                }
            }
        }
        if (bSet) {
            throw new java.text.ParseException(err + "detected 'key=', but no 'value'", 0);
        } else if (nSet && oSet) {
            throw new java.text.ParseException(err + "detected 'key' '=', but no 'value'", 0);
        } else if (nSet) {
            strh.put(unEscape(KeyOrValue), "true");
        }
        return strh;
    }

    private static String unEscape(String s) throws java.text.ParseException {
        final String qr = java.util.regex.Matcher.quoteReplacement("\\");
        final java.util.regex.Pattern p = java.util.regex.Pattern.compile("" + "([^\\\\]\\\\[1-3][0-7][0-7]" + "|[^\\\\]\\\\[1-7][0-7]" + "|[^\\\\]\\\\[0-7]" + ")");
        s = s.replaceAll("\\\\=", "=");
        s = s.replaceAll("\\\\ ", " ");
        s = s.replaceAll("\\\\000", " ");
        s = s.replaceAll("\\\\\t", "\t");
        final java.util.regex.Matcher m = p.matcher(s);
        while (m.find()) {
            int a = m.start() + 1;
            int b = m.end() - 1;
            throw new java.text.ParseException("unEscape: octal is currently" + " unsupported (pos=" + a + ":" + b + ")", 0);
        }
        s = s.replaceAll("\\\\\\\\", qr);
        return s;
    }

    /**
	 * @see #keepAliveReq(int, String, String, int, String,
	 *                  String, alp.Session.state, long, long, long, long, long)
	 */
    static String keepAliveReq(int version, String fw, String hw, int pn, String sn, String namespace, state state) {
        checkVersion(version);
        checkFw(fw);
        checkHw(hw);
        checkPn(pn);
        checkSn(sn);
        checkNamespace(namespace);
        if (!isDisconnected(state) && !isConnected(state)) {
            throw new IllegalArgumentException("Incorrect state," + " must be 'disconnected' or 'connected'");
        }
        return "keepAliveReq" + " _=" + version + " fw=" + fw + " hw=" + hw + " pn=" + pn + " sn=" + sn + " namespace=" + namespace + " state=" + state + "\n";
    }

    /**
	 * The client tells the server that it is alive. Happens every 40sec in
	 * <em>disconnected</em> and every 20sec in <em>connected</em> state -- does
	 * not happen in <em>uninitialized</em> state.
	 *
	 * <p>The client expects <code>keepAliveCnf</code> from the server as
	 * answer.
	 *
	 * <p>If nothing happens (no <code>keepAliveCnf</code> or any other message
	 * from the server) for 60sec, the client sends a
	 * {@link #discReq discReq} and goes to the <em>uninitialized</em> state.
	 */
    static String keepAliveReq(int version, String fw, String hw, int pn, String sn, String namespace, state state, long byteCount, long connTime, long idleTime, long lossCount, long pktCount) {
        String s = keepAliveReq(version, fw, hw, pn, sn, namespace, state);
        checkByteCount(byteCount);
        checkConnTime(connTime);
        checkIdleTime(idleTime);
        checkLossCount(lossCount);
        checkPktCount(pktCount);
        s = s.substring(0, s.length() - 1) + " byteCount=" + byteCount + " connTime=" + connTime + " idleTime=" + idleTime + " lossCount=" + lossCount + " pktCount=" + pktCount + "\n";
        return s;
    }

    static boolean keepAliveCnf(java.util.HashMap<String, String> h) {
        return true;
    }

    /**
	 * The client tells the server that it is going to disconnect
	 * (into the <em>uninitialized</em> state). Happens if
	 * the client does not get any reply from the server for 60sec.
	 *
	 * <p>No reply is expected from the server.
	 */
    static String protocolErrorReq(int version, String fw, String hw, int pn, String sn, String namespace, cause cause) {
        checkVersion(version);
        checkFw(fw);
        checkHw(hw);
        checkPn(pn);
        checkSn(sn);
        checkNamespace(namespace);
        if (cause != cause.connError && cause != cause.discError && cause != cause.keepAliveError && cause != cause.controlError && cause != cause.redirectError && cause != cause.protocolErrorError && cause != cause.controlSmartCardError) {
            throw new IllegalArgumentException("Incorrect cause," + " must be 'connError', 'connError', 'keepAliveError'," + " 'controlError', 'redirectError', 'protocolErrorError'" + " or 'controlSmartCardError'");
        }
        return "protocolErrorReq" + " _=" + version + " fw=" + fw + " hw=" + hw + " pn=" + pn + " sn=" + sn + " namespace=" + namespace + " cause=" + cause + "\n";
    }

    static void discInf(java.util.HashMap<String, String> h) throws java.text.ParseException {
        if (!h.containsKey("access")) {
            throw new java.text.ParseException("'access'" + " key missing.", 0);
        }
        String p1 = h.get("access");
        if (p1 == null) {
            throw new java.text.ParseException("'access'" + " has no value.", 0);
        }
        if (!p1.equals("denied")) {
            throw new java.text.ParseException("value of 'access' must be" + "'denied'.", 0);
        }
    }

    /**
	 * The client tells the server that it is going to disconnect
	 * (into the <em>uninitialized</em> state). Happens if
	 * the client does not get any reply from the server for 60sec.
	 *
	 * <p>No reply is expected from the server.
	 */
    static String discReq(int version, cause cause, String fw, String hw, int pn, String sn, String namespace) {
        checkVersion(version);
        if (cause != cause.redirect && cause != cause.keepAliveExpiry) {
            throw new IllegalArgumentException("Incorrect cause," + " must be 'redirect' or 'keepAliveExpiry'");
        }
        checkFw(fw);
        checkHw(hw);
        checkPn(pn);
        checkSn(sn);
        checkNamespace(namespace);
        return "discReq" + " _=" + version + " cause=" + cause + " fw=" + fw + " hw=" + hw + " pn=" + pn + " sn=" + sn + " namespace=" + namespace + " state=disconnected" + " type=unknown" + "\n";
    }

    /**
	 * The client tells the server that it has gone to the disconnected state,
	 * after it has been receiving <code>discInf</code> from server.
	 *
	 * <p>No reply is expected from the server.
	 */
    static String discRsp(int version, String fw, String hw, int pn, String sn, String namespace) {
        checkVersion(version);
        checkFw(fw);
        checkHw(hw);
        checkPn(pn);
        checkSn(sn);
        checkNamespace(namespace);
        return "discRsp" + " _=" + version + " fw=" + fw + " hw=" + hw + " pn=" + pn + " sn=" + sn + " namespace=" + namespace + " state=disconnected" + "\n";
    }

    /**
	 * The client confirms that the server initiated connection
	 * <code>connInf</code> can be established or not.
	 *
	 * <p>No reply is expected from the server.
	 */
    static String connRsp(int version, String access, String fw, String hw, int pn, String sn, String namespace, state state) {
        checkVersion(version);
        checkFw(fw);
        checkHw(hw);
        checkPn(pn);
        checkSn(sn);
        checkNamespace(namespace);
        if (!isDisconnected(state) && !isConnected(state)) {
            throw new IllegalArgumentException("Incorrect state," + " must be 'disconneced' or 'connected'");
        }
        return "connRsp" + " _=" + version + " access=" + access + " fw=" + fw + " hw=" + hw + " pn=" + pn + " sn=" + sn + " namespace=" + namespace + " state=" + state + "\n";
    }

    static void connInf(java.util.HashMap<String, String> h) throws java.text.ParseException {
        if (!h.containsKey("tokenSeq") || !h.containsKey("access")) {
            throw new java.text.ParseException("'tokenSeq' or 'access'" + " key missing.", 0);
        }
        String p1 = h.get("tokenSeq");
        String p2 = h.get("access");
        if (p1 == null || p2 == null) {
            throw new java.text.ParseException("'tokenSeq' or 'access'" + " has no value.", 0);
        }
        try {
            Integer.parseInt(h.get("tokenSeq"));
        } catch (NumberFormatException e) {
            throw new java.text.ParseException("value of 'tokenSeq' can't be" + "converted to a number.", 0);
        }
        if (!p2.equals("allowed") && !p2.equals("denied")) {
            throw new java.text.ParseException("value of 'access' must be" + "'allowed' or 'denied'.", 0);
        }
    }

    /**
	 * The client tells the server that a token (smartcard or builtin)
	 * has been inserted or removed, or a session redirection has taken place.
	 *
	 * <p>No reply is expected from the server.
	 *
	 * @param id Smartcard id or 0 (if unreadable) or builtin token id.
	 * @see #infoReq_removed
	 * @see #infoReq_insertedSmartcard
	 * @see #infoReq_insertedBuiltin
	 * @see #infoReq_redirected
	 */
    private static String infoReq(int version, event event, String fw, String hw, String namespace, String id, int pn, String sn, state state, type type) {
        checkVersion(version);
        checkFw(fw);
        checkHw(hw);
        checkNamespace(namespace);
        checkId(id);
        checkPn(pn);
        checkSn(sn);
        if (!isDisconnected(state) && !isConnected(state)) {
            throw new IllegalArgumentException("Incorrect state," + " must be 'disconneced' or 'connected'");
        }
        return "infoReq" + " _=" + version + " event=" + event + " fw=" + fw + " hw=" + hw + " namespace=" + namespace + " id=" + id + " pn=" + pn + " sn=" + sn + " state=" + state + " type=" + type + "\n";
    }

    /**
	 * @param type must be <em>pseudo</em> or <em>card</em>
	 * @see #infoReq
	 */
    static String infoReq_removed(int version, String fw, String hw, String namespace, String id, int pn, String sn, type type) {
        if (type != type.pseudo && type != type.card) {
            throw new IllegalArgumentException("Incorrect type," + " must be 'pseudo' or 'card'");
        }
        return infoReq(version, event.remove, fw, hw, namespace, id, pn, sn, state.connected, type);
    }

    /**
	 * @param initState Client settings have been (0) / have not been (1)
	 *        changed during operation.
	 * @param cause must be <em>insert</em> or <em>redirect</em>
	 * @param atr Answer to Reset
	 * @param atr_hs offset to ATR history bytes
	 * @param startRes XxY resolution (640x480)
	 * @param tokenSeq
	 * @see #infoReq
	 */
    static String infoReq_insertedSmartcard(int version, String fw, String hw, String namespace, String id, int pn, String sn, String atr, String atr_hs, cause cause, int initState, String startRes, int tokenSeq) {
        if (cause != cause.insert && cause != cause.redirect) {
            throw new IllegalArgumentException("Incorrect cause," + " must be 'insert' or 'redirect'");
        }
        if (initState != 0 && initState != 1) {
            throw new IllegalArgumentException("Incorrect initState," + " must be 0 (settings changed) or 1 (settings unchanged)");
        }
        String s = infoReq(version, event.insert, fw, hw, namespace, id, pn, sn, state.disconnected, type.card);
        s = s.substring(0, s.length() - 1);
        return s + " atr=" + atr + " atr.hs=" + atr_hs + " cause=" + cause + " initState=" + initState + " startRes=" + startRes + " tokenSeq=" + tokenSeq + "\n";
    }

    /**
	 * @param initState Client settings have been (0) / have not been (1)
	 *        changed during operation.
	 * @param cause must be <em>insert</em> or <em>redirect</em>
	 * @param startRes XxY resolution (640x480)
	 * @param tokenSeq
	 * @see #infoReq
	 */
    static String infoReq_insertedBuiltin(int version, String fw, String hw, String namespace, String id, int pn, String sn, cause cause, int initState, String startRes, int tokenSeq) {
        if (cause != cause.insert && cause != cause.redirect) {
            throw new IllegalArgumentException("Incorrect cause," + " must be 'insert' or 'redirect'");
        }
        if (initState != 0 && initState != 1) {
            throw new IllegalArgumentException("Incorrect initState," + " must be 0 (settings changed) or 1 (settings unchanged)");
        }
        String s = infoReq(version, event.insert, fw, hw, namespace, id, pn, sn, state.disconnected, type.pseudo);
        s = s.substring(0, s.length() - 1);
        return s + " cause=" + cause + " initState=" + initState + " startRes=" + startRes + " tokenSeq=" + tokenSeq + "\n";
    }

    /**
	 * @param initState Client settings have been (0) / have not been (1)
	 *        changed during operation.
	 * @param cause must be <em>insert</em> or <em>redirect</em>
	 * @param startRes XxY resolution (640x480)
	 * @param tokenSeq
	 * @see #infoReq
	 */
    static String infoReq_redirected(int version, event event, String fw, String hw, String namespace, String id, int pn, String sn, state state, type type, int initState, String startRes, int tokenSeq, String realType, String realId, String redirectProps) {
        if (initState != 0 && initState != 1) {
            throw new IllegalArgumentException("Incorrect initState," + " must be 0 (settings changed) or 1 (settings unchanged)");
        }
        String s = infoReq(version, event, fw, hw, namespace, id, pn, sn, state, type);
        s = s.substring(0, s.length() - 1);
        return s + " realType=" + realType + " realId=" + realId + " redirectProps=" + redirectProps + "\n";
    }

    private static void checkVersion(int version) {
        if (version != 1) {
            throw new IllegalArgumentException("Incorrect version (_)," + " must be 1");
        }
    }

    private static void checkFw(String fw) {
        if (fw == null || fw.equals("")) {
            throw new IllegalArgumentException("Incorrect firmware (fw)," + " must be set and non empty");
        }
    }

    private static void checkHw(String hw) {
        if (hw == null || hw.equals("")) {
            throw new IllegalArgumentException("Incorrect hardware (hw)," + " must be set and non empty");
        }
    }

    private static void checkPn(int pn) {
        if (pn < 1 || pn > 65535) {
            throw new IllegalArgumentException("Incorrect port number (pn)," + " must be <= 65535 and >= 1");
        }
    }

    private static void checkSn(String sn) {
        if (sn == null || sn.equals("")) {
            throw new IllegalArgumentException("Incorrect serial number (sn)," + " must be set and non empty");
        }
    }

    private static void checkNamespace(String namespace) {
        if (namespace == null || namespace.equals("")) {
            throw new IllegalArgumentException("Incorrect namespace," + " must be set and non empty");
        }
    }

    private static void checkId(String id) {
        if (id == null || id.equals("")) {
            throw new IllegalArgumentException("Incorrect identification (id)," + " must be set and non empty");
        }
    }

    private static void checkByteCount(long byteCount) {
    }

    ;

    private static void checkConnTime(long connTime) {
    }

    ;

    private static void checkIdleTime(long idleTime) {
    }

    ;

    private static void checkLossCount(long lossCount) {
    }

    ;

    private static void checkPktCount(long pktCount) {
    }

    ;

    private static boolean isUninitialized(state s) {
        return s == state.uninitialized;
    }

    private static boolean isDisconnected(state s) {
        return s == state.disconnected;
    }

    private static boolean isConnected(state s) {
        return s == state.connected;
    }
}
