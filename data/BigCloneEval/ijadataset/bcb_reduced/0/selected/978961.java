package com.microfly.core;

import com.microfly.exception.NpsException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

/**
 * FCKEditor Template
 *    
 * a new publishing system
 * Copyright: Copyright (c) 2007
 * @author jialin
 * @version 1.0
 */
public class FCKTemplate implements IPortable {

    private String id = null;

    private String title = null;

    private String desc = null;

    private int scope = 0;

    private String unitid = null;

    private String html;

    private String creator = null;

    private String creator_cn = null;

    protected java.util.Date createdate = null;

    public FCKTemplate(NpsContext ctxt, String title, int scope, User user) throws NpsException {
        this.id = GenerateId(ctxt);
        this.title = title;
        this.scope = scope;
        this.creator = user.GetUID();
        this.creator_cn = user.GetName();
        this.unitid = user.GetUnitId();
        this.createdate = new java.util.Date();
    }

    protected FCKTemplate(String id, String title, int scope, String creator, String creator_cn, String unitid, Date createdate) {
        this.id = id;
        this.title = title;
        this.scope = scope;
        this.creator = creator;
        this.creator_cn = creator_cn;
        this.unitid = unitid;
        this.createdate = createdate;
    }

    public static FCKTemplate GetTemplate(NpsContext ctxt, String id) throws NpsException {
        FCKTemplate template = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "select a.*,c.unit unitid,b.name uname from fcktemplate a,Users b,dept c Where b.dept=c.id and  a.creator=b.id and a.id=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                template = new FCKTemplate(id, rs.getString("title"), rs.getInt("scope"), rs.getString("creator"), rs.getString("uname"), rs.getString("unitid"), rs.getTimestamp("createdate"));
                template.desc = rs.getString("description");
                template.html = rs.getString("html");
            }
        } catch (Exception e) {
            template = null;
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
        return template;
    }

    public void Save(NpsContext ctxt, boolean bNew) throws NpsException {
        if (bNew) {
            Save(ctxt);
        } else {
            Update(ctxt);
        }
    }

    private void Save(NpsContext ctxt) throws NpsException {
        java.sql.PreparedStatement pstmt = null;
        try {
            String sql = "insert into fcktemplate(id,title,description,scope,html,creator,createdate) values(?,?,?,?,?,?,?)";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, title);
            pstmt.setString(3, desc);
            pstmt.setInt(4, scope);
            pstmt.setString(5, html);
            pstmt.setString(6, creator);
            pstmt.setTimestamp(7, new java.sql.Timestamp(createdate.getTime()));
            pstmt.executeUpdate();
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e1) {
            }
        }
    }

    private void Update(NpsContext ctxt) throws NpsException {
        java.sql.PreparedStatement pstmt = null;
        try {
            String sql = "update fcktemplate set title=?,scope=?,description=?,html=? where id=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, title);
            pstmt.setInt(2, scope);
            pstmt.setString(3, desc);
            pstmt.setString(4, html);
            pstmt.setString(5, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e1) {
            }
        }
    }

    public void Delete(NpsContext ctxt) throws NpsException {
        java.sql.PreparedStatement pstmt = null;
        try {
            String sql = "delete from fcktemplate where id=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e1) {
            }
        }
    }

    private String GenerateId(NpsContext ctxt) throws NpsException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "select seq_fcktemplate.nextval from dual";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
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

    public String GetId() {
        return id;
    }

    public String GetTitle() {
        return title;
    }

    public void SetTitle(String title) {
        this.title = title;
    }

    public int GetScope() {
        return scope;
    }

    public boolean IsGlobal() {
        return scope == 0;
    }

    public void SetScope(int scope) {
        this.scope = scope;
    }

    public String GetCreator() {
        return creator;
    }

    public String GetCreatorCN() {
        return creator_cn;
    }

    public String GetUnitId() {
        return unitid;
    }

    public Date GetCreateDate() {
        return createdate;
    }

    public String GetHtmlData() {
        return html;
    }

    public void SetHtmlData(String html) {
        this.html = html;
    }

    public void SetDescription(String desc) {
        this.desc = desc;
    }

    public String GetDescription() {
        return desc;
    }

    public void Zip(NpsContext ctxt, ZipOutputStream out) throws Exception {
        ZipInfo(ctxt, out);
        ZipData(ctxt, out);
    }

    private void ZipInfo(NpsContext ctxt, ZipOutputStream out) throws Exception {
        String filename = "FCK" + GetId() + ".fck";
        out.putNextEntry(new ZipEntry(filename));
        ZipWriter writer = new ZipWriter(out);
        try {
            writer.println(id);
            writer.println(title);
            writer.println(scope);
            writer.println(unitid);
            writer.println(creator);
            writer.println(createdate);
            writer.print(desc);
        } finally {
            out.closeEntry();
        }
    }

    private void ZipData(NpsContext ctxt, ZipOutputStream out) throws Exception {
        String filename = "FCK" + GetId() + ".data";
        out.putNextEntry(new ZipEntry(filename));
        ZipWriter writer = new ZipWriter(out);
        try {
            writer.print(html);
        } finally {
            out.closeEntry();
        }
    }
}
