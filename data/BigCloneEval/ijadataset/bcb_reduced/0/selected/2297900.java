package com.microfly.core;

import com.microfly.exception.NpsException;
import com.microfly.compiler.JavaWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import com.microfly.core.db.Database;

/**
 * ��λ�����
 * a new publishing system
 * Copyright (c) 2007
 *
 * @author jialin
 * @version 1.0
 */
public class Unit implements java.io.Serializable, IPortable, IPublishable {

    private String id = null;

    private String name = null;

    private String code = null;

    private String address = null;

    private String email = null;

    private String attachman = null;

    private String zipcode = null;

    private String phonenum = null;

    private String mobile = null;

    private Date createdate = null;

    public Unit(String id, String name, String code) {
        this.id = id;
        this.name = name;
        if (code != null) this.code = code.toLowerCase();
    }

    public String GetId() {
        return id;
    }

    public String GetName() {
        return name;
    }

    public String GetCode() {
        return code;
    }

    public String GetAddress() {
        return address;
    }

    public String GetEmail() {
        return email;
    }

    public String GetAttachman() {
        return attachman;
    }

    public String GetZipcode() {
        return zipcode;
    }

    public String GetPhonenum() {
        return phonenum;
    }

    public String GetMobile() {
        return mobile;
    }

    public Date GetCreateDate() {
        return createdate;
    }

    public void SetName(String s) {
        name = s;
    }

    public void SetCode(String s) {
        if (s != null) s = s.toLowerCase();
        code = s;
    }

    public void SetAddress(String s) {
        address = s;
    }

    public void SetEmail(String s) {
        email = s;
    }

    public void SetAttachman(String s) {
        attachman = s;
    }

    public void SetZipcode(String s) {
        zipcode = s;
    }

    public void SetPhonenum(String s) {
        phonenum = s;
    }

    public void SetMobile(String s) {
        mobile = s;
    }

    private void SetCreateDate(Date d) {
        createdate = d;
    }

    public static Unit GetUnit(String unitid) throws NpsException {
        Connection conn = null;
        try {
            conn = Database.GetDatabase("fly").GetConnection();
            return GetUnit(conn, unitid);
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static Unit GetUnit(Connection conn, String unitid) throws NpsException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from unit where id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, unitid);
            rs = pstmt.executeQuery();
            if (!rs.next()) return null;
            Unit unit = new Unit(unitid, rs.getString("name"), rs.getString("code"));
            unit.SetAddress(rs.getString("address"));
            unit.SetAttachman(rs.getString("attachman"));
            unit.SetMobile(rs.getString("mobile"));
            unit.SetCreateDate(rs.getTimestamp("createdate"));
            unit.SetPhonenum(rs.getString("phonenum"));
            unit.SetZipcode(rs.getString("zipcode"));
            unit.SetEmail(rs.getString("email"));
            return unit;
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (rs != null) try {
                rs.close();
            } catch (Exception e) {
            }
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static Unit GetUnitByCode(String unitcode) throws NpsException {
        Connection conn = null;
        try {
            conn = Database.GetDatabase("fly").GetConnection();
            return GetUnitByCode(conn, unitcode);
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static Unit GetUnitByCode(Connection conn, String unitcode) throws NpsException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from unit where code=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, unitcode);
            rs = pstmt.executeQuery();
            if (!rs.next()) return null;
            Unit unit = new Unit(rs.getString("id"), rs.getString("name"), rs.getString("code"));
            unit.SetAddress(rs.getString("address"));
            unit.SetAttachman(rs.getString("attachman"));
            unit.SetMobile(rs.getString("mobile"));
            unit.SetCreateDate(rs.getTimestamp("createdate"));
            unit.SetPhonenum(rs.getString("phonenum"));
            unit.SetZipcode(rs.getString("zipcode"));
            unit.SetEmail(rs.getString("email"));
            return unit;
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (rs != null) try {
                rs.close();
            } catch (Exception e) {
            }
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static Unit GetUnit(String name, String code) throws NpsException {
        Connection conn = null;
        try {
            conn = Database.GetDatabase("fly").GetConnection();
            return GetUnit(conn, name, code);
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static Unit GetUnit(Connection conn, String name, String code) throws NpsException {
        String id = GenerateUnitID(conn);
        return new Unit(id, name, code);
    }

    private static String GenerateUnitID(Connection conn) throws NpsException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("select seq_unit.nextval unitid from dual");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("unitid");
            }
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            try {
                rs.close();
            } catch (Exception e1) {
            }
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
        }
        return null;
    }

    public DeptTree GetDeptTree() throws NpsException {
        DeptTree tree = DeptPool.GetPool().get(id);
        if (tree == null) {
            tree = DeptTree.LoadTree(this, name);
            DeptPool.GetPool().put(tree);
        }
        return tree;
    }

    public DeptTree GetDeptTree(Connection conn) throws NpsException {
        DeptTree tree = DeptPool.GetPool().get(id);
        if (tree == null) {
            tree = DeptTree.LoadTree(conn, this, name);
            DeptPool.GetPool().put(tree);
        }
        return tree;
    }

    public void Save(boolean bNew) throws NpsException {
        Connection conn = null;
        try {
            conn = Database.GetDatabase("fly").GetConnection();
            Save(conn, bNew);
            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception e1) {
            }
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (Exception e) {
            }
        }
    }

    public void Save(Connection conn, boolean bNew) throws NpsException {
        try {
            if (bNew) SaveUnit(conn); else UpdateUnit(conn);
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        }
    }

    private void SaveUnit(Connection conn) throws NpsException {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("insert into unit(id,name,code,address,email,attachman,zipcode,phonenum,mobile) values(?,?,?,?,?,?,?,?,?)");
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, code);
            pstmt.setString(4, address);
            pstmt.setString(5, email);
            pstmt.setString(6, attachman);
            pstmt.setString(7, zipcode);
            pstmt.setString(8, phonenum);
            pstmt.setString(9, mobile);
            pstmt.executeUpdate();
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
        }
    }

    private void UpdateUnit(Connection conn) throws NpsException {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("update unit set name=?,code=?,address=?,email=?,attachman=?,zipcode=?,phonenum=?,mobile=? where id=?");
            pstmt.setString(1, name);
            pstmt.setString(2, code);
            pstmt.setString(3, address);
            pstmt.setString(4, email);
            pstmt.setString(5, attachman);
            pstmt.setString(6, zipcode);
            pstmt.setString(7, phonenum);
            pstmt.setString(8, mobile);
            pstmt.setString(9, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            String sql = "Update dept a Set a.code='" + code + "'||'.'||substr(code,INSTR(code,'.')+1) where unit=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            DeptPool.GetPool().remove(id);
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
        }
    }

    public void Delete() throws NpsException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = Database.GetDatabase("fly").GetConnection();
            conn.setAutoCommit(false);
            Delete(conn);
            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception e1) {
            }
            com.microfly.util.DefaultLog.error(e);
        } finally {
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            if (conn != null) try {
                conn.close();
            } catch (Exception e) {
            }
        }
    }

    public void Delete(Connection conn) throws NpsException {
        PreparedStatement pstmt = null;
        try {
            if (conn.getAutoCommit()) conn.setAutoCommit(false);
            String sql = null;
            sql = "delete From Attach a Where artid In (Select b.Id From article b,site d Where b.siteid=d.id and d.unit=?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            sql = "delete from article where siteid in (select id from site where unit=?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            sql = "delete from topic_pts where topid in (select id from topic where siteid in (select d.id from site d where d.unit=?))";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            sql = "delete from topic c where siteid in (select d.id from site d where d.unit=?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            sql = "delete from template a Where a.siteid In (Select Id From site b Where b.unit=?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            sql = "delete from template a Where a.Scope=1 And \n" + "Exists (Select b.Id  From users b,dept c Where b.Id=a.creator And b.dept=c.Id And c.unit=?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            String owner = GetDefaultSysAdmin(conn);
            sql = "update template a set a.creator=? where scope=0 and creator in (select b.id from users b,dept c where b.dept=c.id and c.unit=?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, owner);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            sql = "delete from site_host where siteid in (select id from site where unit=?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            sql = "delete from site_owner where siteid in (select id from site where unit=?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            sql = "delete from site where unit=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            sql = "delete from userrole a where a.userid in (select b.id from users b,dept c where b.dept=c.id and c.unit=?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            sql = "delete from users a where a.dept in (select c.id from dept c where c.unit=?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            sql = "delete from dept where unit=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
            sql = "delete from unit where id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            DeptPool.GetPool().remove(id);
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            try {
                pstmt.close();
            } catch (Exception e1) {
            }
        }
    }

    private String GetDefaultSysAdmin(Connection conn) throws NpsException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("select id from users where utype=9");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (rs != null) try {
                rs.close();
            } catch (Exception e1) {
            }
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e1) {
            }
        }
        return null;
    }

    public File GetOutputFile() {
        return null;
    }

    public String GetURL() {
        return null;
    }

    public boolean HasField(String fieldName) {
        if (fieldName == null || fieldName.length() == 0) return false;
        String key = fieldName.trim();
        if (key.length() == 0) return false;
        key = key.toUpperCase();
        if (key.equalsIgnoreCase("unit_id")) return true;
        if (key.equalsIgnoreCase("unit_name")) return true;
        if (key.equalsIgnoreCase("unit_code")) return true;
        if (key.equalsIgnoreCase("unit_address")) return true;
        if (key.equalsIgnoreCase("unit_email")) return true;
        if (key.equalsIgnoreCase("unit_attachman")) return true;
        if (key.equalsIgnoreCase("unit_zipcode")) return true;
        if (key.equalsIgnoreCase("unit_phone")) return true;
        if (key.equalsIgnoreCase("unit_phonenum")) return true;
        if (key.equalsIgnoreCase("unit_mobile")) return true;
        return false;
    }

    public Object GetField(String fieldName) throws NpsException {
        if (fieldName == null || fieldName.length() == 0) return null;
        String key = fieldName.trim();
        if (key.length() == 0) return null;
        key = key.toUpperCase();
        if (key.equalsIgnoreCase("unit_id")) return id;
        if (key.equalsIgnoreCase("unit_name")) return name;
        if (key.equalsIgnoreCase("unit_code")) return code;
        if (key.equalsIgnoreCase("unit_address")) return address;
        if (key.equalsIgnoreCase("unit_email")) return email;
        if (key.equalsIgnoreCase("unit_attachman")) return attachman;
        if (key.equalsIgnoreCase("unit_zipcode")) return zipcode;
        if (key.equalsIgnoreCase("unit_phonenum")) return phonenum;
        if (key.equalsIgnoreCase("unit_phone")) return phonenum;
        if (key.equalsIgnoreCase("unit_mobile")) return mobile;
        return null;
    }

    public String GetField(String fieldName, int wordcount) throws NpsException {
        return GetField(fieldName, wordcount, "");
    }

    public String GetField(String fieldName, String format) throws NpsException {
        Object fld_obj = GetField(fieldName);
        if (fld_obj == null) return null;
        if (fld_obj instanceof java.util.Date) {
            return com.microfly.util.Utils.FormateDate((java.util.Date) fld_obj, format);
        }
        if (fld_obj instanceof java.lang.Number) {
            return com.microfly.util.Utils.FormateNumber(fld_obj, format);
        }
        return fld_obj.toString();
    }

    public String GetField(String fieldName, int width, int height) throws NpsException {
        return GetField(fieldName, width);
    }

    public String GetField(String fieldName, int wordcount, String append) throws NpsException {
        Object fld_obj = GetField(fieldName);
        if (fld_obj == null) return null;
        if (fld_obj instanceof String) {
            String s = (String) fld_obj;
            if (wordcount <= 0) return s;
            if (wordcount >= s.length()) return s;
            return s.substring(0, wordcount) + append;
        }
        return fld_obj.toString();
    }

    public String GetField(String fieldName, String format, int wordcount) throws NpsException {
        return GetField(fieldName, format, wordcount, "");
    }

    public String GetField(String fieldName, String format, int wordcount, String append) throws NpsException {
        String s = GetField(fieldName, format);
        if (s == null) return null;
        if (wordcount <= 0) return s;
        if (wordcount >= s.length()) return s;
        return s.substring(0, wordcount) + append;
    }

    public void Zip(NpsContext ctxt, ZipOutputStream out) throws Exception {
        String filename = "UNIT" + GetId() + ".unit";
        out.putNextEntry(new ZipEntry(filename));
        try {
            ZipWriter writer = new ZipWriter(out);
            writer.println(id);
            writer.println(name);
            writer.println(code);
            writer.println(address);
            writer.println(email);
            writer.println(attachman);
            writer.println(zipcode);
            writer.println(phonenum);
            writer.println(mobile);
        } finally {
            out.closeEntry();
        }
    }

    public static Unit GetUnit(Connection conn, ZipFile file) throws Exception {
        Unit aunit = null;
        java.util.Enumeration files = file.entries();
        while (files.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) files.nextElement();
            if (!entry.isDirectory()) {
                if (entry.getName().endsWith(".unit")) {
                    InputStream in = file.getInputStream(entry);
                    java.io.InputStreamReader r = new InputStreamReader(in, "UTF-8");
                    java.io.BufferedReader br = new BufferedReader(r);
                    String s = br.readLine();
                    String unit_name = null;
                    s = br.readLine();
                    if (s != null) unit_name = s.trim();
                    String unit_code = null;
                    s = br.readLine();
                    if (s != null) unit_code = s.trim();
                    String unit_address = null;
                    s = br.readLine();
                    if (s != null) unit_address = s.trim();
                    String unit_email = null;
                    s = br.readLine();
                    if (s != null) unit_email = s.trim();
                    String unit_attachman = null;
                    s = br.readLine();
                    if (s != null) unit_attachman = s.trim();
                    String unit_zipcode = null;
                    s = br.readLine();
                    if (s != null) unit_zipcode = s.trim();
                    String unit_phonenum = null;
                    s = br.readLine();
                    if (s != null) unit_phonenum = s.trim();
                    String unit_mobile = null;
                    s = br.readLine();
                    if (s != null) unit_mobile = s.trim();
                    try {
                        br.close();
                    } catch (Exception e1) {
                    }
                    aunit = Unit.GetUnitByCode(conn, unit_code);
                    if (aunit == null) {
                        aunit = Unit.GetUnit(conn, unit_name, unit_code);
                        aunit.SetAddress(unit_address);
                        aunit.SetEmail(unit_email);
                        aunit.SetAddress(unit_attachman);
                        aunit.SetZipcode(unit_zipcode);
                        aunit.SetPhonenum(unit_phonenum);
                        aunit.SetMobile(unit_mobile);
                        aunit.Save(conn, true);
                    }
                    break;
                }
            }
        }
        return aunit;
    }
}
