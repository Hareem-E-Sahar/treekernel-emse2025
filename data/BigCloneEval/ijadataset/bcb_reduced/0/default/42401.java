import java.sql.*;
import java.util.*;

public class JDBCAccess {

    private final String d1 = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=", d2 = ";DriverID=22;READONLY=true}";

    private Connection con = null;

    private Statement stmt = null;

    private String filename = "Academia.mdb";

    JDBCAccess() {
        connectDBase();
    }

    JDBCAccess(String fn) {
        filename = fn;
        connectDBase();
    }

    /** Bridge Source to Access DataBase **/
    private void connectDBase() {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.err);
        }
        try {
            con = DriverManager.getConnection(d1 + filename + d2);
            stmt = con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    /** Returns number of rows in a table */
    private int countRow(String tableName) {
        int temp = 0;
        try {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt " + "FROM " + tableName + " ");
            if (rs.next()) temp = rs.getInt("cnt"); else temp = -1;
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
        return temp;
    }

    /** Returns the Major table */
    public ObjMajor[] getMajor() {
        String tablename = "Major";
        ObjMajor m[] = new ObjMajor[countRow(tablename)];
        for (int i = 0; i < m.length; ++i) m[i] = new ObjMajor();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * " + "FROM " + tablename + " " + "ORDER BY name ASC, degree ASC");
            for (int i = 0; rs.next(); ++i) {
                m[i].setName(rs.getString("name"));
                m[i].setDegree(rs.getString("degree"));
                m[i].setTotalunits(rs.getInt("totalunits"));
                m[i].setSupportunits(rs.getInt("supportunits"));
                m[i].setCoreunits(rs.getInt("coreunits"));
                m[i].setElectiveunits(rs.getInt("electiveunits"));
                m[i].setGeaunits(rs.getInt("geaunits"));
                m[i].setGebunits(rs.getInt("gebunits"));
                m[i].setGecunits(rs.getInt("gecunits"));
                m[i].setGedunits(rs.getInt("gedunits"));
                m[i].setGeeunits(rs.getInt("geeunits"));
            }
        } catch (SQLException e) {
            qp("SQL ERROR: Found in Obj[] getMajor()");
            e.printStackTrace(System.err);
        }
        return m;
    }

    /** Returns a row in the Major table */
    public ObjMajor getMajor(String name, String degree) {
        ObjMajor m = new ObjMajor();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * " + "FROM Major " + "WHERE name='" + name + "' AND " + "      degree='" + degree + "' ");
            if (rs.next()) {
                m.setName(rs.getString("name"));
                m.setDegree(rs.getString("degree"));
                m.setTotalunits(rs.getInt("totalunits"));
                m.setSupportunits(rs.getInt("supportunits"));
                m.setCoreunits(rs.getInt("coreunits"));
                m.setElectiveunits(rs.getInt("electiveunits"));
                m.setGeaunits(rs.getInt("geaunits"));
                m.setGebunits(rs.getInt("gebunits"));
                m.setGecunits(rs.getInt("gecunits"));
                m.setGedunits(rs.getInt("gedunits"));
                m.setGeeunits(rs.getInt("geeunits"));
            }
        } catch (SQLException e) {
            qp("SQL ERROR: Found in Obj getMajor()");
            e.printStackTrace(System.err);
        }
        return m;
    }

    /** Returns the Course table */
    public ObjCourse[] getCourse() {
        String tablename = "Course";
        ObjCourse c[] = new ObjCourse[countRow(tablename)];
        for (int i = 0; i < c.length; ++i) c[i] = new ObjCourse();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * " + "FROM " + tablename + " " + "ORDER BY id ASC");
            for (int i = 0; rs.next(); ++i) {
                c[i].setId(rs.getString("id"));
                c[i].setName(rs.getString("name"));
                c[i].setDesc(rs.getString("desc"));
                c[i].setUnit(rs.getInt("unit"));
            }
        } catch (SQLException e) {
            qp("SQL ERROR: Found in Obj[] getCourse()");
            e.printStackTrace(System.err);
        }
        return c;
    }

    /** Returns a row in the Course table */
    public ObjCourse getCourse(String id) {
        ObjCourse c = new ObjCourse();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * " + "FROM Course " + "WHERE id='" + id + "' ");
            if (rs.next()) {
                c.setId(rs.getString("id"));
                c.setName(rs.getString("name"));
                c.setDesc(rs.getString("desc"));
                c.setUnit(rs.getInt("unit"));
            }
        } catch (SQLException e) {
            qp("SQL ERROR: Found in Obj getCourse()");
            e.printStackTrace(System.err);
        }
        return c;
    }

    /** Returns the Area table (Core, Support, Elective, A-E) */
    private ObjArea[] getArea(String tablename) {
        ObjArea a[] = new ObjArea[countRow(tablename)];
        for (int i = 0; i < a.length; ++i) a[i] = new ObjArea();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * " + "FROM " + tablename + " " + "ORDER BY courseid ASC, section ASC");
            for (int i = 0; rs.next(); ++i) {
                a[i].setCourseid(rs.getString("courseid"));
                a[i].setMajor(rs.getString("major"));
                a[i].setDegree(rs.getString("degree"));
                a[i].setSection(rs.getString("section"));
            }
        } catch (SQLException e) {
            qp("SQL ERROR: Found in Obj[] getArea(tablename)");
            e.printStackTrace(System.err);
        }
        return a;
    }

    /** Returns the Area table (Core, Support, Elective, A-E) */
    private ObjArea[] getArea(String tablename, String major, String degree) {
        ObjArea a[] = new ObjArea[countRow(tablename)];
        for (int i = 0; i < a.length; ++i) a[i] = new ObjArea();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * " + "FROM " + tablename + " " + "WHERE major='" + major + "' AND " + "      degree='" + degree + "' " + "ORDER BY courseid ASC, section ASC");
            for (int i = 0; rs.next(); ++i) {
                a[i].setCourseid(rs.getString("courseid"));
                a[i].setMajor(rs.getString("major"));
                a[i].setDegree(rs.getString("degree"));
                a[i].setSection(rs.getString("section"));
            }
        } catch (SQLException e) {
            qp("SQL ERROR: Found in Obj[] getArea(tablename, major, degree)");
            e.printStackTrace(System.err);
        }
        return a;
    }

    /** Returns the Area table (Core, Support, Elective, A-E) */
    public ObjArea[] getArea(String tablename, String major, String degree, String section) {
        ObjArea a[] = new ObjArea[1];
        try {
            ResultSet rs = stmt.executeQuery("SELECT * " + "FROM " + tablename + " " + "WHERE major='" + major + "' AND " + "      degree='" + degree + "' AND " + "      section='" + section + "' " + "ORDER BY courseid ASC, section ASC");
            int cnt = 0;
            while (rs.next()) {
                ++cnt;
            }
            a = new ObjArea[cnt];
            for (int i = 0; i < a.length; ++i) a[i] = new ObjArea();
            rs = stmt.executeQuery("SELECT * " + "FROM " + tablename + " " + "WHERE major='" + major + "' AND " + "      degree='" + degree + "' AND " + "      section='" + section + "' " + "ORDER BY courseid ASC, section ASC");
            for (int i = 0; rs.next(); ++i) {
                a[i].setCourseid(rs.getString("courseid"));
                a[i].setMajor(rs.getString("major"));
                a[i].setDegree(rs.getString("degree"));
                a[i].setSection(rs.getString("section"));
            }
        } catch (SQLException e) {
            qp("SQL ERROR: Found in Obj[] getArea(tablename, major, degree, section)");
            e.printStackTrace(System.err);
        }
        return a;
    }

    /** Returns the Area table (Core, Support, Elective, A-E) */
    public ObjArea[] getCore() {
        return getArea("Core");
    }

    public ObjArea[] getCore(String major, String degree) {
        return getArea("Core", major, degree);
    }

    public ObjArea[] getSupport() {
        return getArea("Support");
    }

    public ObjArea[] getSupport(String major, String degree) {
        return getArea("Support", major, degree);
    }

    public ObjArea[] getElective() {
        return getArea("Elective");
    }

    public ObjArea[] getElective(String major, String degree) {
        return getArea("Elective", major, degree);
    }

    public ObjArea[] getAreaA() {
        return getArea("AreaA");
    }

    public ObjArea[] getAreaA(String major, String degree) {
        return getArea("AreaA", major, degree);
    }

    public ObjArea[] getAreaA(String major, String degree, String section) {
        return getArea("AreaA", major, degree, section);
    }

    public ObjArea[] getAreaB() {
        return getArea("AreaB");
    }

    public ObjArea[] getAreaB(String major, String degree) {
        return getArea("AreaB", major, degree);
    }

    public ObjArea[] getAreaB(String major, String degree, String section) {
        return getArea("AreaB", major, degree, section);
    }

    public ObjArea[] getAreaC() {
        return getArea("AreaC");
    }

    public ObjArea[] getAreaC(String major, String degree) {
        return getArea("AreaC", major, degree);
    }

    public ObjArea[] getAreaC(String major, String degree, String section) {
        return getArea("AreaC", major, degree, section);
    }

    public ObjArea[] getAreaD() {
        return getArea("AreaD");
    }

    public ObjArea[] getAreaD(String major, String degree) {
        return getArea("AreaD", major, degree);
    }

    public ObjArea[] getAreaD(String major, String degree, String section) {
        return getArea("AreaD", major, degree, section);
    }

    public ObjArea[] getAreaE() {
        return getArea("AreaE");
    }

    public ObjArea[] getAreaE(String major, String degree) {
        return getArea("AreaE", major, degree);
    }

    public ObjArea[] getAreaE(String major, String degree, String section) {
        return getArea("AreaE", major, degree, section);
    }

    /** Returns the Prerequisite table */
    public ObjPrereq[] getPrereq() {
        String tablename = "Prerequisite";
        ObjPrereq p[] = new ObjPrereq[countRow(tablename)];
        for (int i = 0; i < p.length; ++i) p[i] = new ObjPrereq();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tablename + " " + "ORDER BY before ASC, after ASC");
            for (int i = 0; rs.next(); ++i) {
                p[i].setBefore(rs.getString("before"));
                p[i].setAfter(rs.getString("after"));
                p[i].setDegree(rs.getString("degree"));
                p[i].setMajor(rs.getString("major"));
            }
        } catch (SQLException e) {
            qp("SQL ERROR: Found in Obj[] getPrereq()");
            e.printStackTrace(System.err);
        }
        return p;
    }

    /** Returns the Prerequisite table */
    public ObjPrereq[] getPrereq(String major, String degree) {
        String tablename = "Prerequisite";
        ObjPrereq p[] = new ObjPrereq[countRow(tablename)];
        for (int i = 0; i < p.length; ++i) p[i] = new ObjPrereq();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * " + "FROM " + tablename + " " + "WHERE major='" + major + "' AND " + "      degree='" + degree + "' " + "ORDER BY before ASC, after ASC");
            for (int i = 0; rs.next(); ++i) {
                p[i].setBefore(rs.getString("before"));
                p[i].setAfter(rs.getString("after"));
                p[i].setDegree(rs.getString("degree"));
                p[i].setMajor(rs.getString("major"));
            }
        } catch (SQLException e) {
            qp("SQL ERROR: Found in Obj[] getPrereq(major, degree)");
            e.printStackTrace(System.err);
        }
        return p;
    }

    /** Returns the Prerequisite table */
    public ObjPrereq[] getPrereq(String major, String degree, String condition) {
        String tablename = "Prerequisite";
        ObjPrereq p[] = new ObjPrereq[countRow(tablename)];
        for (int i = 0; i < p.length; ++i) p[i] = new ObjPrereq();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * " + "FROM " + tablename + " " + "WHERE major='" + major + "' AND " + "      degree='" + degree + "' " + "      " + condition);
            for (int i = 0; rs.next(); ++i) {
                p[i].setBefore(rs.getString("before"));
                p[i].setAfter(rs.getString("after"));
                p[i].setDegree(rs.getString("degree"));
                p[i].setMajor(rs.getString("major"));
            }
        } catch (SQLException e) {
            qp("SQL ERROR: Found in Obj[] getPrereq(major, degree)");
            e.printStackTrace(System.err);
        }
        return p;
    }

    /**
    * 
    * @param quarter
    * @param year
    * @return ObjOffering[] OfferingType()
    */
    public ObjOffering[] getOffering(String quarter, int year) {
        String tablename = "Offering";
        ObjOffering off[] = new ObjOffering[countRow(tablename)];
        for (int i = 0; i < off.length; ++i) off[i] = new ObjOffering();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * " + "FROM " + tablename + " " + "WHERE quarter='" + quarter + "'  AND " + "      year= " + year + " ");
            for (int i = 0; rs.next(); ++i) {
                int start = rs.getInt("start");
                int end = rs.getInt("end");
                off[i].courseid = rs.getString("courseid");
                off[i].section = rs.getString("section");
                off[i].quarter = rs.getString("quarter");
                off[i].instructor = rs.getString("instructor");
                off[i].room = rs.getString("room");
                off[i].year = rs.getInt("year");
                off[i].startdate = rs.getString("startdate");
                if (rs.getString("sunday") != null) {
                    off[i].days[0][0] = start;
                    off[i].days[0][1] = end;
                    off[i].sunS = start;
                    off[i].sunE = end;
                } else {
                    off[i].days[0][0] = 2400;
                    off[i].days[0][1] = -1;
                    off[i].sunS = 2400;
                    off[i].sunE = -1;
                }
                if (rs.getString("monday") != null) {
                    off[i].days[1][0] = start;
                    off[i].days[1][1] = end;
                    off[i].monS = start;
                    off[i].monE = end;
                } else {
                    off[i].days[1][0] = 2400;
                    off[i].days[1][1] = -1;
                    off[i].monS = 2400;
                    off[i].monE = -1;
                }
                if (rs.getString("tuesday") != null) {
                    off[i].days[2][0] = start;
                    off[i].days[2][1] = end;
                    off[i].tueS = start;
                    off[i].tueE = end;
                } else {
                    off[i].days[2][0] = 2400;
                    off[i].days[2][1] = -1;
                    off[i].tueS = 2400;
                    off[i].tueE = -1;
                }
                if (rs.getString("wednesday") != null) {
                    off[i].days[3][0] = start;
                    off[i].days[3][1] = end;
                    off[i].wedS = start;
                    off[i].wedE = end;
                } else {
                    off[i].days[3][0] = 2400;
                    off[i].days[3][1] = -1;
                    off[i].wedS = 2400;
                    off[i].wedE = -1;
                }
                if (rs.getString("thursday") != null) {
                    off[i].days[4][0] = start;
                    off[i].days[4][1] = end;
                    off[i].thuS = start;
                    off[i].thuE = end;
                } else {
                    off[i].days[4][0] = 2400;
                    off[i].days[4][1] = -1;
                    off[i].thuS = 2400;
                    off[i].thuE = -1;
                }
                if (rs.getString("friday") != null) {
                    off[i].days[5][0] = start;
                    off[i].days[5][1] = end;
                    off[i].friS = start;
                    off[i].friE = end;
                } else {
                    off[i].days[5][0] = 2400;
                    off[i].days[5][1] = -1;
                    off[i].friS = 2400;
                    off[i].friE = -1;
                }
                if (rs.getString("saturday") != null) {
                    off[i].days[6][0] = start;
                    off[i].days[6][1] = end;
                    off[i].satS = start;
                    off[i].satE = end;
                } else {
                    off[i].days[6][0] = 2400;
                    off[i].days[6][1] = -1;
                    off[i].satS = 2400;
                    off[i].satE = -1;
                }
            }
        } catch (SQLException e) {
            qp("SQL ERROR: Found in Obj[] getOffering(quarter,year)");
            e.printStackTrace(System.err);
        }
        return off;
    }

    /**
    * 
    * @param quarter
    * @param year
    * @param condition Must be in the format of SQL and have the syntax of "AND <condition> AND ... <condition>"
    * @return ObjOffering[] OfferingType()
    */
    public ObjOffering getOfferingCondition(String quarter, int year, String courseid) {
        String tablename = "Offering";
        ObjOffering off = new ObjOffering();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * " + "FROM " + tablename + " " + "WHERE quarter='" + quarter + "' AND " + "      year=" + year + " AND " + "      courseid='" + courseid + "' ");
            for (int i = 0; rs.next(); ++i) {
                int start = rs.getInt("start");
                int end = rs.getInt("end");
                off.courseid = rs.getString("courseid");
                off.section = rs.getString("section");
                off.quarter = rs.getString("quarter");
                off.instructor = rs.getString("instructor");
                off.room = rs.getString("room");
                off.year = rs.getInt("year");
                off.startdate = rs.getString("startdate");
                if (rs.getString("sunday") != null) {
                    off.days[0][0] = start;
                    off.days[0][1] = end;
                    off.sunS = start;
                    off.sunE = end;
                } else {
                    off.days[0][0] = 2400;
                    off.days[0][1] = -1;
                    off.sunS = 2400;
                    off.sunE = -1;
                }
                if (rs.getString("monday") != null) {
                    off.days[1][0] = start;
                    off.days[1][1] = end;
                    off.monS = start;
                    off.monE = end;
                } else {
                    off.days[1][0] = 2400;
                    off.days[1][1] = -1;
                    off.monS = 2400;
                    off.monE = -1;
                }
                if (rs.getString("tuesday") != null) {
                    off.days[2][0] = start;
                    off.days[2][1] = end;
                    off.tueS = start;
                    off.tueE = end;
                } else {
                    off.days[2][0] = 2400;
                    off.days[2][1] = -1;
                    off.tueS = 2400;
                    off.tueE = -1;
                }
                if (rs.getString("wednesday") != null) {
                    off.days[3][0] = start;
                    off.days[3][1] = end;
                    off.wedS = start;
                    off.wedE = end;
                } else {
                    off.days[3][0] = 2400;
                    off.days[3][1] = -1;
                    off.wedS = 2400;
                    off.wedE = -1;
                }
                if (rs.getString("thursday") != null) {
                    off.days[4][0] = start;
                    off.days[4][1] = end;
                    off.thuS = start;
                    off.thuE = end;
                } else {
                    off.days[4][0] = 2400;
                    off.days[4][1] = -1;
                    off.thuS = 2400;
                    off.thuE = -1;
                }
                if (rs.getString("friday") != null) {
                    off.days[5][0] = start;
                    off.days[5][1] = end;
                    off.friS = start;
                    off.friE = end;
                } else {
                    off.days[5][0] = 2400;
                    off.days[5][1] = -1;
                    off.friS = 2400;
                    off.friE = -1;
                }
                if (rs.getString("saturday") != null) {
                    off.days[6][0] = start;
                    off.days[6][1] = end;
                    off.satS = start;
                    off.satE = end;
                } else {
                    off.days[6][0] = 2400;
                    off.days[6][1] = -1;
                    off.satS = 2400;
                    off.satE = -1;
                }
            }
        } catch (SQLException e) {
            qp("SQL ERROR: Found in Obj[] getOffering(quarter,year,condition)");
            e.printStackTrace(System.err);
        }
        return off;
    }

    /** Close all opened connections properly **/
    public void close() {
        try {
            stmt.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    public void qp(int n) {
        System.out.println(n);
    }

    public void qp(String n) {
        System.out.println(n);
    }

    public void test() {
        ObjMajor m = getMajor("Computer Science", "Undergraduat");
        qp(m.getGeeunits());
    }
}
