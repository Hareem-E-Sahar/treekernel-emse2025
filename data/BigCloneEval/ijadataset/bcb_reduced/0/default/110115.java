import java.net.*;
import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;
import javax.naming.*;
import javax.naming.directory.*;
import gnu.regexp.*;
import java.sql.*;

class popUm {

    private static Hashtable leaf;

    private static SimpleDateFormat sqlDateFormat;

    private static Hashtable groupCache;

    private static Vector getGroups(String uid) {
        uid = uid.toLowerCase();
        if (groupCache.containsKey(uid)) {
            Hashtable h = (Hashtable) groupCache.get(uid);
            long t = ((Long) h.get("lastUsed")).longValue();
            if (t > (System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7))) {
                h.put("lastUsed", new Long(System.currentTimeMillis()));
                return (Vector) h.get("groups");
            }
            System.out.println("------------------------------------------------Chucking group cache entry for " + uid);
        } else {
        }
        System.out.print("miss - " + uid + " ");
        Hashtable h = new Hashtable();
        Vector v = new Vector();
        v.addElement("nogroup");
        h.put("groups", v);
        h.put("lastUsed", new Long(System.currentTimeMillis()));
        groupCache.put(uid, h);
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
        try {
            dc = new InitialDirContext(env);
        } catch (Exception ex) {
            System.out.println("connect problem: " + ex.toString());
        }
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setTimeLimit(10000);
        String[] atr = new String[1];
        atr[0] = "dn";
        constraints.setReturningAttributes(atr);
        String filt = "(uid=" + uid + ")";
        Enumeration results = null;
        try {
            results = (Enumeration) dc.search(base, filt, constraints);
        } catch (Exception ex) {
            System.out.println("search problem: " + uid + " : " + ex.toString());
        }
        SearchResult sr = null;
        while (results.hasMoreElements()) sr = (SearchResult) results.nextElement();
        if (sr == null) {
            return v;
        }
        Attributes attrs = sr.getAttributes();
        Enumeration ae = (Enumeration) attrs.getAll();
        String dn = sr.getName();
        constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(atr);
        filt = "(&(|(objectclass=groupofnames)(objectclass=groupofuniquenames))(|(uniquemember=" + dn + "," + base + ")(member=" + dn + "," + base + ")))";
        results = null;
        try {
            results = dc.search(base, filt, constraints);
        } catch (Exception ex) {
            System.out.println("probelm getting groups: " + ex.toString());
        }
        Vector grps = new Vector();
        while (results.hasMoreElements()) {
            sr = (SearchResult) results.nextElement();
            grps.addElement(cutGroupName(sr.getName()));
        }
        h.put("groups", grps);
        h.put("lastUsed", new Long(System.currentTimeMillis()));
        groupCache.put(uid, h);
        System.out.println("adding " + uid + " to cache");
        return grps;
    }

    private static String cutGroupName(String s) {
        StringBuffer ret = new StringBuffer();
        int l = s.length();
        int i = 3;
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

    public static void main(String[] argv) throws Exception {
        sqlDateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
        Connection db = null;
        groupCache = null;
        Hashtable userDb = new Hashtable();
        Hashtable topicDb = new Hashtable();
        try {
            ObjectInputStream s = new ObjectInputStream(new FileInputStream("groupCache.ser"));
            groupCache = (Hashtable) s.readObject();
        } catch (Exception ex) {
            groupCache = new Hashtable();
            System.out.println("error reading groupCache: " + ex.toString());
        }
        Vector topicUrls = new Vector();
        try {
            FileInputStream s = new FileInputStream("topicUrls.txt");
            int c = 0;
            StringBuffer cur = new StringBuffer();
            while ((c = s.read()) != -1) {
                if (((char) c) == '\n') {
                    topicUrls.addElement(cur.toString());
                    cur = new StringBuffer();
                } else cur.append((char) c);
            }
        } catch (Exception ex) {
            topicUrls = new Vector();
            System.out.println("error reading topicUrls: " + ex.toString());
        }
        try {
            Class.forName("com.sybase.jdbc2.jdbc.SybDriver").newInstance();
        } catch (Exception ex) {
            System.out.println("db problem " + ex.toString());
            System.exit(1);
        }
        try {
            db = DriverManager.getConnection("jdbc:sybase:Tds:sin.gmp.usyd.edu.au:4000/VolAssessment", "hemul", "gremble.");
        } catch (Exception ex) {
            System.out.println("db problem " + ex.toString());
            System.exit(1);
        }
        Statement st = db.createStatement();
        ResultSet rs = null;
        String query = null;
        Hashtable h = new Hashtable();
        Hashtable s97avg = new Hashtable();
        h.put("topicAverages", s97avg);
        h.put("uid", "students97");
        userDb.put("students97", h);
        h = new Hashtable();
        Hashtable s98avg = new Hashtable();
        h.put("topicAverages", s98avg);
        h.put("uid", "students98");
        userDb.put("students98", h);
        query = "select id, name from LearningTopics";
        System.out.println("executing " + query);
        rs = st.executeQuery(query);
        while (rs.next()) {
            if (topicUrls.size() == 0) break;
            h = new Hashtable();
            String id = rs.getString(1);
            String name = rs.getString(2);
            h.put("id", id);
            h.put("name", name);
            h.put("url", "http://www.gmp.usyd.edu.au/tutorials/destructure/" + topicUrls.elementAt(0));
            topicUrls.removeElementAt(0);
            topicDb.put(id, h);
            s97avg.put(id, new Float(-1f));
            s98avg.put(id, new Float(-1f));
        }
        query = "select id, name from Users where name like 'a%' and not name = 'anonymous'";
        System.out.println("executing " + query);
        rs = st.executeQuery(query);
        while (rs.next()) {
            h = new Hashtable();
            String id = rs.getString(1);
            String uid = rs.getString(2);
            System.out.println(rs.getString(2));
            h.put("uid", uid);
            h.put("id", id);
            if (((Vector) getGroups(uid)).contains("students97")) {
                h.put("group", "students97");
            } else if (((Vector) getGroups(uid)).contains("students98")) {
                h.put("group", "students98");
            } else h.put("group", "gmpstaff");
            Hashtable ut = new Hashtable();
            h.put("topicAverages", ut);
            Enumeration te = topicDb.keys();
            while (te.hasMoreElements()) ut.put(((Hashtable) topicDb.get((String) te.nextElement())).get("id"), new Float(-1f));
            userDb.put(id, h);
        }
        Enumeration topicse = topicDb.keys();
        while (topicse.hasMoreElements()) {
            Hashtable t = (Hashtable) topicDb.get(topicse.nextElement());
            query = "select problemGroup, count(problemGroup) from Questions where learningTopic = " + t.get("id") + " group by problemGroup";
            System.out.println("executing " + query);
            rs = st.executeQuery(query);
            Hashtable groupFactors = new Hashtable();
            int gcount = 0;
            while (rs.next()) {
                String gid = rs.getString(1);
                groupFactors.put(gid, new Integer(rs.getInt(2)));
                gcount += rs.getInt(2);
            }
            Vector groups = new Vector();
            Enumeration ge = groupFactors.keys();
            while (ge.hasMoreElements()) {
                String gid = (String) ge.nextElement();
                int gv = ((Integer) groupFactors.get(gid)).intValue();
                float gfac = gv / (float) gcount;
                if (gfac > 0.333f) {
                    groups.addElement(gid);
                }
            }
            t.put("groups", groups);
        }
        st.close();
        java.util.Date today = new java.util.Date();
        java.util.Date weekStart = getCurrentWeekStart(today);
        topicse = topicDb.keys();
        while (topicse.hasMoreElements()) {
            Hashtable t = (Hashtable) topicDb.get(topicse.nextElement());
            String topicId = (String) t.get("id");
            Vector peers = new Vector();
            t.put("peers", peers);
            Vector tg = (Vector) t.get("groups");
            Enumeration tge = tg.elements();
            while (tge.hasMoreElements()) {
                String i = (String) tge.nextElement();
                Enumeration te2 = topicDb.keys();
                while (te2.hasMoreElements()) {
                    Hashtable t2 = (Hashtable) topicDb.get(te2.nextElement());
                    if (((Vector) t2.get("groups")).contains(i)) {
                        peers.addElement(t2.get("id"));
                    }
                }
            }
            System.out.println(t.get("id").toString() + " peers with " + peers.toString());
            float students97Count = 0f;
            int students97Total = 0;
            float students98Count = 0f;
            int students98Total = 0;
            st = db.createStatement();
            query = "select uid, count(mark), sum(mark) from AnswerLog where questionId in (select id from Questions where learningTopic = " + topicId + ") group by uid";
            System.out.println("executing " + query);
            rs = st.executeQuery(query);
            while (rs.next()) {
                String uid = rs.getString(1);
                Hashtable u = (Hashtable) userDb.get(uid);
                if (u == null) continue;
                Hashtable ta = (Hashtable) u.get("topicAverages");
                int count = rs.getInt(2);
                float total = rs.getFloat(3);
                ta.put(topicId, new Float(total / count));
                System.out.println("uid " + u.get("uid") + " avg " + (total / count));
                if (((Vector) getGroups((String) u.get("uid"))).contains("students97")) {
                    students97Count += count;
                    students97Total += total;
                }
                if (((Vector) getGroups((String) u.get("uid"))).contains("students98")) {
                    students98Count += count;
                    students98Total += total;
                }
            }
            if (students97Count != 0) {
                s97avg.put(topicId, new Float(students97Total / students97Count));
                System.out.println("s97avg " + (students97Total / students97Count));
            }
            if (students98Count != 0) {
                s98avg.put(topicId, new Float(students98Total / students98Count));
                System.out.println("s98avg " + (students98Total / students98Count));
            }
            st.close();
        }
        try {
            db.close();
        } catch (Exception ex) {
            System.out.println("db problem: " + ex.toString());
        }
        ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream("groupCache.ser"));
        s.writeObject(groupCache);
        s.flush();
        s = new ObjectOutputStream(new FileOutputStream("userDb.ser"));
        s.writeObject(userDb);
        s.flush();
        s = new ObjectOutputStream(new FileOutputStream("topicDb.ser"));
        s.writeObject(topicDb);
        s.flush();
    }
}
