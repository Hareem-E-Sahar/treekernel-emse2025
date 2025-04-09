import java.net.*;
import java.lang.*;
import java.text.*;
import java.util.*;
import java.io.*;
import java.text.*;
import javax.naming.*;
import javax.naming.directory.*;
import gnu.regexp.*;
import java.sql.*;

class findStudent {

    private SimpleDateFormat sqlDateFormat;

    public Vector groupVector;

    public String groupSqlString;

    public void getGroup(String groupName) throws Exception {
        groupName = groupName.toLowerCase();
        String base = "ou=College of Health Sciences,o=University of Sydney,c=AU";
        Properties env = new Properties();
        env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.provider", "ldap://ldap.gmp.usyd.edu.au:389/");
        env.put("java.naming.provider.url", "ldap://ldap.gmp.usyd.edu.au:389/");
        env.put("java.naming.security.principal", "");
        env.put("java.naming.security.credentials", "");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put("java.naming.security.authentication", "simple");
        env.put("java.naming.ldap.version", "3");
        DirContext dc = null;
        dc = new InitialDirContext(env);
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setTimeLimit(10000);
        String[] atr = new String[1];
        atr[0] = "uniquemember";
        constraints.setReturningAttributes(atr);
        String filt = "(cn=" + groupName + ")";
        NamingEnumeration results = (NamingEnumeration) dc.search(base, filt, constraints);
        StringBuffer sret = new StringBuffer();
        groupVector = new Vector();
        while (results.hasMoreElements()) {
            Attributes answera = ((SearchResult) results.next()).getAttributes();
            for (NamingEnumeration ae = answera.getAll(); ae.hasMore(); ) {
                Attribute a = (Attribute) ae.next();
                for (NamingEnumeration aee = a.getAll(); aee.hasMore(); ) {
                    String n = cutGroupName((String) aee.next());
                    groupVector.addElement(n);
                    sret.append("'" + n + "',");
                }
            }
        }
        String r = sret.toString();
        groupSqlString = r.substring(0, r.length() - 1);
    }

    private String cutGroupName(String s) {
        StringBuffer ret = new StringBuffer();
        int l = s.length();
        int i = 4;
        while (i < l) {
            if (s.charAt(i) == ',') return ret.toString();
            ret.append(s.charAt(i));
            i++;
        }
        return ret.toString();
    }

    private static java.util.Date getCurrentWeekStart(java.util.Date d) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(d);
        cal.add(Calendar.DAY_OF_WEEK, -cal.get(Calendar.DAY_OF_WEEK) + 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public static void main(String[] args) throws Exception {
        genText g = new genText();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sqdf = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
        String cohortName = args[0];
        java.util.Date nowDate = new java.util.Date();
        String dateString = sdf.format(nowDate);
        String sqlDate = sqdf.format(nowDate);
        java.util.Date cws = getCurrentWeekStart(nowDate);
        g.getGroup(cohortName);
        Class.forName("com.sybase.jdbc2.jdbc.SybDriver").newInstance();
        Connection db = DriverManager.getConnection("jdbc:sybase:Tds:sin.gmp.usyd.edu.au:4100/VolAssessment", "sa", "frumious");
        String query = null;
        query = "select id from Users where name in (" + g.groupSqlString + ") and not name = 'anonymous'";
        ResultSet users = db.createStatement().executeQuery(query);
        StringBuffer idStringBuffer = new StringBuffer();
        while (users.next()) {
            query = "select name from Users where id = " + users.getString(1);
            ResultSet user = db.createStatement().executeQuery(query);
            while (user.next()) {
                System.out.print(user.getString(1) + "|||");
            }
            query = "select avg(mark), count(id) from AnswerLog where uid = " + users.getString(1);
            ResultSet averages = db.createStatement().executeQuery(query);
            while (averages.next()) {
                emit(averages.getFloat(1) + "|||" + averages.getInt(2) + "|||>>>");
            }
        }
    }

    private static void emit(String s) {
        System.out.println(s);
    }

    private static String xclean(String s) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            if ('&' == s.charAt(i)) ret.append("&amp;"); else if ('"' == s.charAt(i)) ret.append("&quot;"); else if ('<' == s.charAt(i)) ret.append("&lt;"); else if ('>' == s.charAt(i)) ret.append("&gt;"); else if ('\'' == s.charAt(i)) ret.append("&apos;"); else ret.append(s.charAt(i));
        }
        return ret.toString();
    }
}
