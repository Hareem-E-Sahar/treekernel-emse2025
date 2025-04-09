package com.microfly.core;

import com.microfly.exception.NpsException;
import com.microfly.exception.ErrorHelper;
import com.microfly.util.tree.Node;
import com.microfly.util.tree.Tree;
import com.microfly.util.Utils;
import com.microfly.event.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipFile;
import java.io.*;

/**
 * a new publishing system
 * Copyright (c) 2007

 * @author jialin
 * @version 1.0
 */
public class TopicTree implements IPortable, Serializable {

    private Site site = null;

    private String treename = null;

    private Tree tree = null;

    private Hashtable ids = new Hashtable();

    private Hashtable codes = new Hashtable();

    private Hashtable templates = new Hashtable();

    public TopicTree(Site site, String treename) {
        this.site = site;
        this.treename = treename;
        this.tree = Tree.GetTree("-1");
    }

    public void AddTopic(Topic t) {
        if (tree == null) tree = Tree.GetTree("-1");
        String parentid = "-1";
        if (t.GetParentId() != null) parentid = t.GetParentId();
        tree.AddNode(t.GetId(), tree.GetNode(parentid), t);
        ids.put(t.GetId(), t);
        codes.put(t.GetCode(), t);
        AddTemplate(t.GetArticleTemplate());
        if (t.GetPageTemplates() != null) {
            for (Object obj : t.GetPageTemplates()) {
                AddTemplate((PageTemplate) obj);
            }
        }
    }

    private void AddTemplate(TemplateBase t) {
        if (t == null) return;
        if (templates.containsKey(t.GetId())) templates.remove(t.GetId());
        templates.put(t.GetId(), t);
    }

    public TemplateBase GetTemplate(String template_id) {
        if (templates.containsKey(template_id)) return (TemplateBase) templates.get(template_id);
        return null;
    }

    public Tree GetTree() {
        return tree;
    }

    public Topic GetTopic(String id) {
        return (Topic) ids.get(id);
    }

    public Topic GetTopicByCode(String code) {
        return (Topic) codes.get(code);
    }

    private Node GetNodeByTopic(Topic t) {
        return tree.GetNode(t.GetId());
    }

    public Site GetSite() {
        return site;
    }

    public String GetSiteId() {
        return site.GetId();
    }

    public static TopicTree LoadTree(NpsContext inCtxt, Site site, String treename) throws NpsException {
        TopicTree aTopicTree = new TopicTree(site, treename);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        PreparedStatement pstmt_pts = null;
        ResultSet rs_pts = null;
        try {
            aTopicTree.CorrectLayerInDB(inCtxt);
            String sql = "select a.*,(select name from template b where a.art_template=b.id and b.type=0) art_template_name" + " from topic a  where a.siteid=? order by layer,idx";
            pstmt = inCtxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, site.GetId());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String parent_top_id = rs.getString("parentid");
                parent_top_id = parent_top_id == null ? "-1" : parent_top_id;
                Topic aTopic = new Topic(site, parent_top_id, rs.getString("id"), rs.getString("name"), rs.getString("alias"), rs.getString("code"), rs.getInt("idx"));
                if (rs.getString("default_article_state") != null) aTopic.SetDefaultArticleState(rs.getInt("default_article_state"));
                if (rs.getString("default_article_score") != null) aTopic.SetScore(rs.getFloat("default_article_score"));
                if (rs.getString("tname") != null) {
                    aTopic.SetTable(rs.getString("tname"));
                }
                if (rs.getString("art_template") != null && rs.getString("art_template_name") != null) {
                    ArticleTemplate aArticleTemplate = (ArticleTemplate) TemplatePool.GetPool().get(rs.getString("art_template"));
                    if (aArticleTemplate == null) {
                        aArticleTemplate = new ArticleTemplate(rs.getString("art_template"), rs.getString("art_template_name"));
                        TemplatePool.GetPool().put(aArticleTemplate);
                    }
                    aTopic.SetArticleTemplate(aArticleTemplate);
                }
                if (rs.getInt("visible") == 0) aTopic.SetVisible(false);
                sql = "select b.id,b.name,b.fname from topic_pts a,template b where a.topid=? and b.type=2 and a.templateid=b.id";
                pstmt_pts = inCtxt.GetConnection().prepareStatement(sql);
                pstmt_pts.setString(1, rs.getString("id"));
                rs_pts = pstmt_pts.executeQuery();
                while (rs_pts.next()) {
                    PageTemplate aPageTemplate = (PageTemplate) TemplatePool.GetPool().get(rs_pts.getString("id"));
                    if (aPageTemplate == null) {
                        aPageTemplate = new PageTemplate(rs_pts.getString("id"), rs_pts.getString("name"), rs_pts.getString("fname"));
                        TemplatePool.GetPool().put(aPageTemplate);
                    }
                    aTopic.AddPageTemplate(aPageTemplate);
                }
                if (rs_pts != null) try {
                    rs_pts.close();
                } catch (Exception e) {
                }
                if (pstmt_pts != null) try {
                    pstmt_pts.close();
                } catch (Exception e) {
                }
                sql = "select b.id,b.name from topic_owner a,users b where a.topid=? and a.userid=b.id";
                pstmt_pts = inCtxt.GetConnection().prepareStatement(sql);
                pstmt_pts.setString(1, rs.getString("id"));
                rs_pts = pstmt_pts.executeQuery();
                while (rs_pts.next()) {
                    aTopic.AddOwner(rs_pts.getString("id"), rs_pts.getString("name"));
                }
                if (rs_pts != null) try {
                    rs_pts.close();
                } catch (Exception e) {
                }
                if (pstmt_pts != null) try {
                    pstmt_pts.close();
                } catch (Exception e) {
                }
                sql = "select * from topic_vars where topid=?";
                pstmt_pts = inCtxt.GetConnection().prepareStatement(sql);
                pstmt_pts.setString(1, rs.getString("id"));
                rs_pts = pstmt_pts.executeQuery();
                while (rs_pts.next()) {
                    aTopic.AddVar(rs_pts.getString("varname"), rs_pts.getString("value"));
                }
                if (rs_pts != null) try {
                    rs_pts.close();
                } catch (Exception e) {
                }
                if (pstmt_pts != null) try {
                    pstmt_pts.close();
                } catch (Exception e) {
                }
                aTopicTree.AddTopic(aTopic);
            }
            aTopicTree.tree.Sort();
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (rs_pts != null) try {
                rs_pts.close();
            } catch (Exception e) {
            }
            if (pstmt_pts != null) try {
                pstmt_pts.close();
            } catch (Exception e) {
            }
            if (rs != null) try {
                rs.close();
            } catch (Exception e) {
            }
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
        return aTopicTree;
    }

    public Topic NewTopic(NpsContext ctxt, String parentid, String name, String alias, int index, String tname, ArticleTemplate art_template, int art_state, float art_score) throws Exception {
        if (parentid == null || parentid.length() == 0) parentid = "-1";
        String code = GenerateTopicCode(parentid, alias);
        String id = GenerateTopicId(ctxt);
        Topic aTopic = new Topic(site, parentid, id, name, alias, code, index);
        aTopic.SetArticleTemplate(art_template);
        aTopic.SetTable(tname);
        aTopic.SetDefaultArticleState(art_state);
        aTopic.SetScore(art_score);
        return aTopic;
    }

    private String GenerateTopicId(NpsContext ctxt) throws NpsException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "select seq_topic.nextval topid from dual";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("topid");
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

    public String GenerateTopicCode(String top_parentid, String alias) throws NpsException {
        if ("-1".equals(top_parentid)) return alias;
        Topic parent = GetTopic(top_parentid);
        return GenerateTopicCode(parent, alias);
    }

    public String GenerateTopicCode(Topic parent, String alias) throws NpsException {
        if (parent == null) return alias;
        if ("-1".equals(parent.GetId())) return alias;
        if (parent.GetCode() == null) return alias;
        String code_try = parent.GetCode() + "." + alias;
        if (GetTopicByCode(code_try) != null) {
            throw new NpsException("alias for topic exists:" + alias, ErrorHelper.SYS_INVALIDTOPICALIAS);
        }
        return code_try;
    }

    public int GenerateTopicIndex(String top_parentid) {
        if (top_parentid == null || top_parentid.length() == 0) top_parentid = "-1";
        Node parent_node = tree.GetNode(top_parentid);
        if (parent_node == null) return 1;
        Iterator childs = parent_node.GetChilds();
        if (childs == null) return 1;
        int max_index = 0;
        while (childs.hasNext()) {
            Node top_node = (Node) childs.next();
            Topic top = (Topic) top_node.GetValue();
            if (top.GetIndex() > max_index) max_index = top.GetIndex();
        }
        return max_index + 1;
    }

    public int GenerateTopicIndex(Topic parent) {
        if (parent == null) return 1;
        return GenerateTopicIndex(parent.GetId());
    }

    public int GetLayer(Topic topic) {
        return GetLayer(topic.GetCode());
    }

    public int GetLayer(String top_code) {
        int layer = 0;
        int pos_dot = top_code.indexOf(".");
        if (pos_dot == -1) return layer;
        while (pos_dot != -1) {
            layer++;
            pos_dot = top_code.indexOf(".", pos_dot + 1);
        }
        return layer;
    }

    public void Save(NpsContext inCtxt, Topic t, boolean bNew) throws NpsException {
        if (bNew) {
            SaveTopic(inCtxt, t);
        } else {
            UpdateTopic(inCtxt, t);
        }
        UpdateTopicPageTemplate(inCtxt, t);
        UpdateTopicOwners(inCtxt, t);
        UpdateTopicVars(inCtxt, t);
        if (bNew) {
            if (t.GetTable() != null && t.GetTable().length() > 0) {
                String key = t.GetTable().toUpperCase();
                EventSubscriber.GetSubscriber().AddListener((InsertEventListener) t, key);
                EventSubscriber.GetSubscriber().AddListener((UpdateEventListener) t, key);
                EventSubscriber.GetSubscriber().AddListener((DeleteEventListener) t, key);
                EventSubscriber.GetSubscriber().AddListener((Ready2PublishEventListener) t, key);
                EventSubscriber.GetSubscriber().AddListener((PublishEventListener) t, key);
                EventSubscriber.GetSubscriber().AddListener((CancelEventListener) t, key);
            }
            AddTopic(t);
        }
        tree.Sort();
        CreateDsTable(inCtxt, t);
    }

    public void Update(NpsContext inCtxt, Topic t) throws NpsException {
        Save(inCtxt, t, false);
    }

    public void Delete(NpsContext ctxt, Topic t) throws NpsException {
        if (t == null) return;
        Node node = GetNodeByTopic(t);
        if (node == null) return;
        DeleteNode(ctxt, node);
        try {
            DeleteNodeTables(ctxt, node);
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error_noexception(e);
        }
        DeleteNode(node);
        tree.RemoveNode(node);
    }

    private void DeleteNode(NpsContext ctxt, Node node) throws NpsException {
        if (node.HasChilds()) {
            Iterator childs = node.GetChilds();
            while (childs.hasNext()) {
                Node child_node = (Node) childs.next();
                DeleteNode(ctxt, child_node);
            }
        }
        Topic t = (Topic) node.GetValue();
        if (t.GetTable() != null && t.GetTable().length() > 0) {
            EventSubscriber.GetSubscriber().RemoveListener(InsertEventListener.class, t);
            EventSubscriber.GetSubscriber().RemoveListener(UpdateEventListener.class, t);
            EventSubscriber.GetSubscriber().RemoveListener(DeleteEventListener.class, t);
            EventSubscriber.GetSubscriber().RemoveListener(Ready2PublishEventListener.class, t);
            EventSubscriber.GetSubscriber().RemoveListener(PublishEventListener.class, t);
            EventSubscriber.GetSubscriber().RemoveListener(CancelEventListener.class, t);
        }
        TriggerManager manager = TriggerManager.LoadTriggers(ctxt);
        manager.DeleteTriggersInTopic(ctxt, t);
        PreparedStatement pstmt = null;
        try {
            String sql = "delete from article Where topic=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, t.GetId());
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "delete from article_topics where topid=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, t.GetId());
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "delete from topic_pts where topid=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, t.GetId());
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "delete from topic_owner where topid=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, t.GetId());
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "delete from topic_vars where topid=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, t.GetId());
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "delete from topic where id=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, t.GetId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            ctxt.Rollback();
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    private void DeleteNodeTables(NpsContext ctxt, Node node) {
        Topic t = (Topic) node.GetValue();
        PreparedStatement pstmt = null;
        try {
            if (t.GetTable() != null && t.GetTable().length() > 0) {
                String dstable_name = t.GetTable() + "_prop";
                String sql = "drop table " + dstable_name;
                pstmt = ctxt.GetConnection().prepareStatement(sql);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
        if (node.HasChilds()) {
            Iterator childs = node.GetChilds();
            while (childs.hasNext()) {
                Node child_node = (Node) childs.next();
                DeleteNodeTables(ctxt, child_node);
            }
        }
    }

    private void DeleteNode(Node node) throws NpsException {
        if (node.HasChilds()) {
            Iterator childs = node.GetChilds();
            while (childs.hasNext()) {
                Node child_node = (Node) childs.next();
                DeleteNode(child_node);
            }
        }
        Topic topic = (Topic) node.GetValue();
        ids.remove(topic.GetId());
        codes.remove(topic.GetCode());
    }

    private void SaveTopic(NpsContext inCtxt, Topic t) throws NpsException {
        if (t == null) return;
        PreparedStatement pstmt = null;
        Topic try_topic_by_code = GetTopicByCode(t.GetCode());
        if (try_topic_by_code != null && !try_topic_by_code.GetId().equalsIgnoreCase(t.GetId())) throw new NpsException("alias for topic exists:" + t.GetAlias(), ErrorHelper.SYS_INVALIDTOPICALIAS);
        try {
            String sql = "insert into topic(id,name,siteid,alias,code,parentid,idx,tname,art_template,default_article_state,default_article_score,layer,visible) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            pstmt = inCtxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, t.GetId());
            pstmt.setString(2, t.GetName());
            pstmt.setString(3, site.GetId());
            pstmt.setString(4, t.GetAlias());
            pstmt.setString(5, t.GetCode());
            pstmt.setString(6, "-1".equalsIgnoreCase(t.GetParentId()) ? null : t.GetParentId());
            pstmt.setInt(7, t.GetIndex());
            pstmt.setString(8, t.GetTable());
            if (t.GetArticleTemplate() != null) pstmt.setString(9, t.GetArticleTemplate().GetId()); else pstmt.setNull(9, java.sql.Types.VARCHAR);
            pstmt.setInt(10, t.GetDefaultArticleState());
            pstmt.setFloat(11, t.GetScore());
            pstmt.setInt(12, GetLayer(t));
            pstmt.setInt(13, t.IsVisible() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    private void UpdateTopic(NpsContext inCtxt, Topic t) throws NpsException {
        if (t == null) return;
        PreparedStatement pstmt = null;
        try {
            String sql = "update topic set name=?,idx=?,tname=?,art_template=?,default_article_state=?,default_article_score=?,siteid=?,code=?,parentid=?,layer=?,visible=? where id=?";
            pstmt = inCtxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, t.GetName());
            pstmt.setInt(2, t.GetIndex());
            pstmt.setString(3, t.GetTable());
            if (t.GetArticleTemplate() != null) pstmt.setString(4, t.GetArticleTemplate().GetId()); else pstmt.setNull(4, java.sql.Types.VARCHAR);
            pstmt.setInt(5, t.GetDefaultArticleState());
            pstmt.setFloat(6, t.GetScore());
            pstmt.setString(7, t.GetSiteId());
            pstmt.setString(8, t.GetCode());
            pstmt.setString(9, t.GetParentId());
            pstmt.setInt(10, GetLayer(t));
            pstmt.setInt(11, t.IsVisible() ? 1 : 0);
            pstmt.setString(12, t.GetId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    private void UpdateTopicPageTemplate(NpsContext inCtxt, Topic t) throws NpsException {
        if (t == null) return;
        PreparedStatement pstmt = null;
        try {
            String sql = "delete from topic_pts where topid=?";
            pstmt = inCtxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, t.GetId());
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            java.util.List pts = t.GetPageTemplates();
            if (pts == null || pts.isEmpty()) return;
            sql = "insert into topic_pts(topid,templateid) values(?,?)";
            pstmt = inCtxt.GetConnection().prepareStatement(sql);
            for (Object obj : pts) {
                PageTemplate pt = (PageTemplate) obj;
                pstmt.setString(1, t.GetId());
                pstmt.setString(2, pt.GetId());
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    private void UpdateTopicOwners(NpsContext inCtxt, Topic t) throws NpsException {
        if (t == null) return;
        PreparedStatement pstmt = null;
        try {
            String sql = "delete from topic_owner where topid=?";
            pstmt = inCtxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, t.GetId());
            pstmt.executeUpdate();
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
            Hashtable owners = t.GetOwner();
            if (owners != null && !owners.isEmpty()) {
                sql = "insert into topic_owner(topid,userid) values(?,?)";
                pstmt = inCtxt.GetConnection().prepareStatement(sql);
                Enumeration owners_elements = owners.elements();
                while (owners_elements.hasMoreElements()) {
                    Topic.Owner owner = (Topic.Owner) owners_elements.nextElement();
                    pstmt.setString(1, t.GetId());
                    pstmt.setString(2, owner.GetID());
                    pstmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    public void UpdateTopicVars(NpsContext inCtxt, Topic t) throws NpsException {
        if (t == null) return;
        PreparedStatement pstmt = null;
        try {
            String sql = "delete from topic_vars where topid=?";
            pstmt = inCtxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, t.GetId());
            pstmt.executeUpdate();
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
            Hashtable vars = t.GetVars();
            if (vars != null && !vars.isEmpty()) {
                sql = "insert into topic_vars(topid,varname,value) values(?,?,?)";
                pstmt = inCtxt.GetConnection().prepareStatement(sql);
                Enumeration vars_elements = vars.elements();
                while (vars_elements.hasMoreElements()) {
                    Topic.Var var = (Topic.Var) vars_elements.nextElement();
                    pstmt.setString(1, t.GetId());
                    pstmt.setString(2, var.name);
                    pstmt.setString(3, var.value);
                    pstmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    public void CreateDsTable(NpsContext ctxt) throws NpsException {
        CreateDsTable(ctxt, tree.GetChilds());
    }

    private void CreateDsTable(NpsContext ctxt, Iterator childs) throws NpsException {
        while (childs.hasNext()) {
            Node node = (Node) childs.next();
            Topic t = (Topic) node.GetValue();
            if (t != null && !"-1".equalsIgnoreCase(t.GetId())) {
                CreateDsTable(ctxt, t);
            }
            if (node.HasChilds()) CreateDsTable(ctxt, node.GetChilds());
        }
    }

    private void CreateDsTable(NpsContext ctxt, Topic t) throws NpsException {
        if (t == null) return;
        String tablename = t.GetTable();
        if (tablename == null || tablename.length() == 0) return;
        String dstable_name = tablename + "_prop";
        dstable_name = dstable_name.toUpperCase();
        PreparedStatement pstmt = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "select count(*) table_count from all_tables Where table_name=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, dstable_name);
            rs = pstmt.executeQuery();
            rs.next();
            if (rs.getInt("table_count") > 0) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
                try {
                    pstmt.close();
                } catch (Exception e) {
                }
                sql = "select id,title,siteid,topic,state,createdate,publishdate from " + dstable_name + " where rownum<2";
                try {
                    pstmt = ctxt.GetConnection().prepareStatement(sql);
                    rs = pstmt.executeQuery();
                    try {
                        rs.close();
                    } catch (Exception e1) {
                    }
                    try {
                        pstmt.close();
                    } catch (Exception e1) {
                    }
                    stmt = ctxt.GetConnection().createStatement();
                    sql = "Alter Table " + dstable_name + " Modify siteid default('" + t.GetSiteId() + "')";
                    stmt.executeUpdate(sql);
                    sql = "Alter Table " + dstable_name + " Modify topic default('" + t.GetId() + "')";
                    stmt.executeUpdate(sql);
                    sql = "COMMENT ON COLUMN " + dstable_name + ".SITEID IS 'site id,default:" + treename + "'";
                    stmt.executeUpdate(sql);
                    sql = "COMMENT ON COLUMN " + dstable_name + ".TOPIC IS 'topic id,default:" + t.GetName() + "'";
                    stmt.executeUpdate(sql);
                    try {
                        stmt.close();
                    } catch (Exception e1) {
                    }
                    return;
                } catch (Exception e) {
                    try {
                        rs.close();
                    } catch (Exception e1) {
                    }
                    try {
                        pstmt.close();
                    } catch (Exception e1) {
                    }
                    sql = "drop table " + dstable_name;
                    pstmt = ctxt.GetConnection().prepareStatement(sql);
                    pstmt.executeUpdate(sql);
                }
            }
            try {
                rs.close();
            } catch (Exception e) {
            }
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            stmt = ctxt.GetConnection().createStatement();
            sql = "CREATE TABLE " + dstable_name + " (ID VARCHAR2(50) NOT NULL," + "TITLE VARCHAR2(1000) NOT NULL," + "SITEID VARCHAR2(10) DEFAULT '" + t.GetSiteId() + "' NOT NULL," + "TOPIC VARCHAR2(10) DEFAULT '" + t.GetId() + "' NOT NULL," + "STATE NUMBER," + "CREATEDATE DATE DEFAULT SYSDATE NOT NULL," + "PUBLISHDATE DATE," + "CONSTRAINT PK_" + dstable_name + " PRIMARY KEY (ID,TOPIC)" + ")";
            stmt.executeUpdate(sql);
            sql = "COMMENT ON COLUMN " + dstable_name + ".ID IS 'id'";
            stmt.executeUpdate(sql);
            sql = "COMMENT ON COLUMN " + dstable_name + ".TITLE IS 'title'";
            stmt.executeUpdate(sql);
            sql = "COMMENT ON COLUMN " + dstable_name + ".SITEID IS 'site id,site name is:" + treename + "'";
            stmt.executeUpdate(sql);
            sql = "COMMENT ON COLUMN " + dstable_name + ".TOPIC IS 'topic id,topic name is:" + t.GetName() + "'";
            stmt.executeUpdate(sql);
            sql = "COMMENT ON COLUMN " + dstable_name + ".STATE IS 'status'";
            stmt.executeUpdate(sql);
            sql = "COMMENT ON COLUMN " + dstable_name + ".CREATEDATE IS 'create date'";
            stmt.executeUpdate(sql);
            sql = "COMMENT ON COLUMN " + dstable_name + ".PUBLISHDATE IS 'publish date'";
            stmt.executeUpdate(sql);
            sql = "create index idx_" + dstable_name + "_topstate on " + dstable_name + "(siteid,topic,state)";
            stmt.executeUpdate(sql);
            try {
                stmt.close();
            } catch (Exception e) {
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
            if (stmt != null) try {
                stmt.close();
            } catch (Exception e) {
            }
        }
    }

    public void MoveTo(NpsContext ctxt, String src_topid, TopicTree dest, String dest_topid) throws NpsException {
        if (site.GetId().equals(dest.GetSiteId())) {
            MoveTo(ctxt, src_topid, dest_topid);
            return;
        }
        try {
            Node node_src = null;
            Node node_dest = null;
            Topic topic_dest = null;
            if (dest_topid != null && dest_topid.length() > 0) {
                topic_dest = dest.GetTopic(dest_topid);
                if (topic_dest == null) throw new NpsException("û���ҵ�Ŀ����Ŀ��Ϣ��", ErrorHelper.SYS_NOTOPIC);
                node_dest = dest.GetNodeByTopic(topic_dest);
            }
            if (src_topid == null || src_topid.length() == 0) {
                Iterator node_src_list = tree.GetChilds();
                while (node_src_list.hasNext()) {
                    node_src = (Node) node_src_list.next();
                    MoveTo(ctxt, node_src, dest, node_dest);
                }
                node_src_list = tree.GetChilds();
                while (node_src_list.hasNext()) {
                    node_src = (Node) node_src_list.next();
                    tree.RemoveNode(node_src);
                }
            } else {
                Topic topic_src = GetTopic(src_topid);
                if (topic_src == null) throw new NpsException("û���ҵ�Ҫ�ƶ�����Ŀ��Ϣ��", ErrorHelper.SYS_NOTOPIC);
                node_src = GetNodeByTopic(topic_src);
                MoveTo(ctxt, node_src, dest, node_dest);
                tree.RemoveNode(node_src);
            }
        } catch (NpsException e) {
            com.microfly.util.DefaultLog.error_noexception(e);
            site.ReloadTopicTree(ctxt);
            dest.site.ReloadTopicTree(ctxt);
            throw e;
        }
    }

    private void MoveTo(NpsContext ctxt, Node node_src, TopicTree tree_dest, Node node_dest) throws NpsException {
        Topic topic = (Topic) node_src.GetValue();
        if (node_dest != null) {
            Topic topic_parent = (Topic) node_dest.GetValue();
            topic.SetSite(tree_dest.GetSite());
            topic.SetParentId(topic_parent.GetId());
            topic.SetCode(topic_parent.GetCode() + "." + topic.GetAlias());
        } else {
            topic.SetSite(tree_dest.GetSite());
            topic.SetParentId("-1");
            topic.SetCode(topic.GetAlias());
        }
        tree_dest.AddTopic(topic);
        tree_dest.Save(ctxt, topic, false);
        if (node_src.HasChilds()) {
            Node copyNode = tree_dest.GetNodeByTopic(topic);
            Iterator childs = node_src.GetChilds();
            while (childs.hasNext()) {
                Node node_src_child = (Node) childs.next();
                MoveTo(ctxt, node_src_child, tree_dest, copyNode);
            }
        }
    }

    private void MoveTo(NpsContext ctxt, String src_topid, String dest_topid) throws NpsException {
        try {
            Node node_dest = null;
            Topic topic_dest = null;
            if (dest_topid != null && dest_topid.length() > 0) {
                topic_dest = GetTopic(dest_topid);
                if (topic_dest == null) throw new NpsException("topic id=" + dest_topid, ErrorHelper.SYS_NOTOPIC);
                node_dest = GetNodeByTopic(topic_dest);
            }
            if (src_topid == null || src_topid.length() == 0) throw new NpsException("source topic is null", ErrorHelper.SYS_NOTOPIC);
            Topic topic_src = GetTopic(src_topid);
            if (topic_src == null) throw new NpsException("topic id=" + src_topid, ErrorHelper.SYS_NOTOPIC);
            Node node_src = GetNodeByTopic(topic_src);
            if (dest_topid == null || dest_topid.length() == 0) dest_topid = "-1";
            tree.RemoveNode(node_src);
            MoveTo(ctxt, node_src, node_dest);
        } catch (NpsException e) {
            com.microfly.util.DefaultLog.error_noexception(e);
            site.ReloadTopicTree(ctxt);
            throw e;
        }
    }

    private void MoveTo(NpsContext ctxt, Node node_src, Node node_dest) throws NpsException {
        Topic topic = (Topic) node_src.GetValue();
        codes.remove(topic.GetCode());
        if (node_dest != null) {
            Topic topic_parent = (Topic) node_dest.GetValue();
            topic.SetParentId(topic_parent.GetId());
            topic.SetCode(topic_parent.GetCode() + "." + topic.GetAlias());
        } else {
            topic.SetParentId("-1");
            topic.SetCode(topic.GetAlias());
        }
        AddTopic(topic);
        Save(ctxt, topic, false);
        codes.put(topic.GetCode(), topic);
        if (node_src.HasChilds()) {
            Node copyNode = GetNodeByTopic(topic);
            Iterator childs = node_src.GetChilds();
            while (childs.hasNext()) {
                Node node_src_child = (Node) childs.next();
                MoveTo(ctxt, node_src_child, copyNode);
            }
        }
    }

    public void CopyTo(NpsContext ctxt, String src_topid, TopicTree dest, String dest_topid) throws NpsException {
        try {
            Node node_src = null;
            Node node_dest = null;
            Topic topic_dest = null;
            if (dest_topid != null && dest_topid.length() > 0) {
                topic_dest = dest.GetTopic(dest_topid);
                if (topic_dest == null) throw new NpsException("topic id=" + dest_topid, ErrorHelper.SYS_NOTOPIC);
                node_dest = dest.GetNodeByTopic(topic_dest);
            }
            if (src_topid == null || src_topid.length() == 0) {
                Iterator node_src_list = tree.GetChilds();
                while (node_src_list.hasNext()) {
                    node_src = (Node) node_src_list.next();
                    CopyTo(ctxt, node_src, dest, node_dest);
                }
            } else {
                Topic topic_src = GetTopic(src_topid);
                if (topic_src == null) throw new NpsException("topic id=" + src_topid, ErrorHelper.SYS_NOTOPIC);
                node_src = GetNodeByTopic(topic_src);
                CopyTo(ctxt, node_src, dest, node_dest);
            }
        } catch (NpsException e) {
            com.microfly.util.DefaultLog.error_noexception(e);
            site.ReloadTopicTree(ctxt);
            if (!site.GetId().equals(dest.GetSiteId())) dest.site.ReloadTopicTree(ctxt);
            throw e;
        }
    }

    private void CopyTo(NpsContext ctxt, Node node_src, TopicTree tree_dest, Node node_dest) throws NpsException {
        String new_id = GenerateTopicId(ctxt);
        Topic topic_src = (Topic) node_src.GetValue();
        Topic topic = null;
        if (node_dest != null) topic = new Topic(new_id, (Topic) node_dest.GetValue(), topic_src); else topic = new Topic(new_id, tree_dest.GetSite(), topic_src);
        tree_dest.Save(ctxt, topic, true);
        if (node_src.HasChilds()) {
            Node copyNode = tree_dest.GetNodeByTopic(topic);
            Iterator childs = node_src.GetChilds();
            while (childs.hasNext()) {
                Node node_src_child = (Node) childs.next();
                CopyTo(ctxt, node_src_child, tree_dest, copyNode);
            }
        }
    }

    public String toDHXTree(String dhxtree, String rootId, boolean show_all) {
        String site_nodeid = "site" + site.GetId();
        String jstree = dhxtree + ".insertNewItem(\"" + rootId + "\"," + "\"" + site_nodeid + "\"," + "\"" + Utils.TransferToHtmlEntity(treename) + "\");";
        jstree += dhxtree + ".setUserData(\"" + site_nodeid + "\"," + "\"siteid\"," + "\"" + site.GetId() + "\");";
        jstree += dhxtree + ".setUserData(\"" + site_nodeid + "\"," + "\"sitename\"," + "\"" + Utils.TransferToHtmlEntity(treename) + "\");";
        jstree += dhxtree + ".setUserData(\"" + site_nodeid + "\"," + "\"topid\"," + "\"\");";
        jstree += dhxtree + ".setUserData(\"" + site_nodeid + "\"," + "\"topname\"," + "\"\");";
        jstree += PaintDHXTree(dhxtree, site_nodeid, tree.GetChilds(), show_all);
        jstree += dhxtree + ".closeAllItems(\"" + site_nodeid + "\");";
        jstree += dhxtree + ".openItem(\"" + site_nodeid + "\");";
        return jstree;
    }

    private String PaintDHXTree(String dhxtree, String parentid, Iterator childs, boolean show_all) {
        String jstree = "";
        while (childs.hasNext()) {
            Node node = (Node) childs.next();
            String id = node.GetId();
            if (!"-1".equalsIgnoreCase(id)) {
                Topic topic = (Topic) node.GetValue();
                if (!show_all && !topic.IsVisible()) continue;
                String top_nodeid = "topic" + id;
                jstree += dhxtree + ".insertNewItem(\"" + parentid + "\"," + "\"" + top_nodeid + "\"," + "\"" + Utils.TransferToHtmlEntity(topic.GetName()) + "\");";
                jstree += dhxtree + ".setUserData(\"" + top_nodeid + "\"," + "\"siteid\"," + "\"" + site.GetId() + "\");";
                jstree += dhxtree + ".setUserData(\"" + top_nodeid + "\"," + "\"sitename\"," + "\"" + Utils.TransferToHtmlEntity(treename) + "\");";
                jstree += dhxtree + ".setUserData(\"" + top_nodeid + "\"," + "\"topid\"," + "\"" + id + "\");";
                jstree += dhxtree + ".setUserData(\"" + top_nodeid + "\"," + "\"topname\"," + "\"" + Utils.TransferToHtmlEntity(topic.GetName()) + "\");";
                if (topic.GetTable() != null && topic.GetTable().length() > 0) jstree += dhxtree + ".setUserData(\"" + top_nodeid + "\"," + "\"tname\"," + "\"" + topic.GetTable() + "\");";
                if (node.HasChilds()) jstree += PaintDHXTree(dhxtree, top_nodeid, node.GetChilds(), show_all);
            } else {
                if (node.HasChilds()) jstree += PaintDHXTree(dhxtree, parentid, node.GetChilds(), show_all);
            }
        }
        return jstree;
    }

    public Iterator GetChilds(Topic top) {
        if (top == null) return tree.GetChilds();
        Node node = GetNodeByTopic(top);
        if (!node.HasChilds()) return null;
        return node.GetChilds();
    }

    public String toSelectBox(String name, String selected, boolean show_all) {
        String html = "<select name=\"" + name + "\">";
        html += PaintSelectOption(selected, tree.GetChilds(), show_all);
        html += "</select>";
        return html;
    }

    private String PaintSelectOption(String selected, Iterator childs, boolean show_all) {
        String options = "";
        while (childs.hasNext()) {
            Node node = (Node) childs.next();
            String id = node.GetId();
            if (!"-1".equalsIgnoreCase(id)) {
                Topic topic = (Topic) node.GetValue();
                if (!show_all && !topic.IsVisible()) continue;
            }
            if (node.HasChilds()) {
                options += PaintSelectOption(selected, node.GetChilds(), show_all);
            } else {
                Topic topic = (Topic) node.GetValue();
                options += "<option value=\"" + topic.GetId() + "\"";
                if (topic.GetId().equals(selected)) options += " selected ";
                options += ">";
                options += Utils.TransferToHtmlEntity(topic.GetName());
                options += "</option>";
            }
        }
        return options;
    }

    public void Zip(NpsContext ctxt, ZipOutputStream out) throws Exception {
        String filename = "TOPIC.list";
        out.putNextEntry(new ZipEntry(filename));
        try {
            ZipWriter writer = new ZipWriter(out);
            ZipSummary(writer, tree.GetChilds());
        } finally {
            out.closeEntry();
        }
        Zip(ctxt, out, tree.GetChilds());
    }

    private void Zip(NpsContext ctxt, ZipOutputStream out, Iterator childs) throws Exception {
        while (childs.hasNext()) {
            Node node = (Node) childs.next();
            Topic topic = (Topic) node.GetValue();
            if (topic != null && !"-1".equalsIgnoreCase(topic.GetId())) topic.Zip(ctxt, out);
            if (node.HasChilds()) Zip(ctxt, out, node.GetChilds());
        }
    }

    private void ZipSummary(ZipWriter writer, Iterator childs) throws Exception {
        while (childs.hasNext()) {
            Node node = (Node) childs.next();
            Topic topic = (Topic) node.GetValue();
            if (topic != null && !"-1".equalsIgnoreCase(topic.GetId())) {
                writer.println(topic.GetId());
            }
            if (node.HasChilds()) ZipSummary(writer, node.GetChilds());
        }
    }

    public static TopicTree LoadTree(NpsContext ctxt, Site asite, Hashtable templates, ZipFile file) throws Exception {
        if (file == null || asite == null) return null;
        TopicTree aTopicTree = new TopicTree(asite, asite.GetName());
        Hashtable topic_indexby_oldid = new Hashtable();
        ZipEntry entry_list = file.getEntry("TOPIC.list");
        if (entry_list == null) return aTopicTree;
        InputStream list_in = file.getInputStream(entry_list);
        java.io.InputStreamReader list_r = new InputStreamReader(list_in, "UTF-8");
        java.io.BufferedReader list_br = new BufferedReader(list_r);
        String top_oldid = null;
        while ((top_oldid = list_br.readLine()) != null) {
            top_oldid = top_oldid.trim();
            if (top_oldid.length() == 0) continue;
            ZipEntry entry = file.getEntry("TOPIC" + top_oldid + ".topic");
            InputStream in = file.getInputStream(entry);
            java.io.InputStreamReader r = new InputStreamReader(in, "UTF-8");
            java.io.BufferedReader br = new BufferedReader(r);
            br.readLine();
            String top_parentid = null;
            String s = br.readLine();
            if (s != null) top_parentid = s.trim();
            if ("-1".equalsIgnoreCase(top_parentid)) top_parentid = null;
            br.readLine();
            String top_name = null;
            s = br.readLine();
            if (s != null) top_name = s.trim();
            String top_alias = null;
            s = br.readLine();
            if (s != null) top_alias = s.trim();
            String top_code = null;
            s = br.readLine();
            if (s != null) top_code = s.trim();
            int top_order = 0;
            s = br.readLine();
            if (s != null) try {
                top_order = (int) Float.parseFloat(s);
            } catch (Exception e1) {
            }
            int top_default_article_state = 0;
            s = br.readLine();
            if (s != null) try {
                top_default_article_state = (int) Float.parseFloat(s);
            } catch (Exception e1) {
            }
            float top_default_article_score = 0.0f;
            s = br.readLine();
            if (s != null) try {
                top_default_article_score = Float.parseFloat(s);
            } catch (Exception e1) {
            }
            String top_table = null;
            s = br.readLine();
            if (s != null) top_table = s.trim();
            String top_articletemplateid = null;
            s = br.readLine();
            if (s != null) top_articletemplateid = s.trim();
            int top_visible = 1;
            s = br.readLine();
            if (s != null) try {
                top_visible = (int) Float.parseFloat(s);
            } catch (Exception e1) {
            }
            try {
                br.close();
            } catch (Exception e1) {
            }
            String top_newid = aTopicTree.GenerateTopicId(ctxt);
            Topic aTopic = new Topic(asite, top_parentid == null ? null : ((Topic) topic_indexby_oldid.get(top_parentid)).GetId(), top_newid, top_name, top_alias, top_code, top_order);
            aTopic.SetDefaultArticleState(top_default_article_state);
            aTopic.SetScore(top_default_article_score);
            if (top_table != null && top_table.length() > 0) {
                aTopic.SetTable(top_table);
            }
            if (top_articletemplateid != null && top_articletemplateid.length() > 0 && templates != null) {
                aTopic.SetArticleTemplate((ArticleTemplate) templates.get(top_articletemplateid));
            }
            aTopic.SetVisible(top_visible == 1);
            String entry_ptsname = "TOPIC" + top_oldid + ".pts";
            ZipEntry entry_pts = file.getEntry(entry_ptsname);
            if (entry_pts != null) {
                InputStream in_pts = file.getInputStream(entry_pts);
                java.io.InputStreamReader r_pts = new InputStreamReader(in_pts, "UTF-8");
                java.io.BufferedReader br_pts = new BufferedReader(r_pts);
                String pt_oldid = null;
                while ((s = br_pts.readLine()) != null) {
                    pt_oldid = s.trim();
                    if (pt_oldid.length() > 0 && templates != null) {
                        aTopic.AddPageTemplate((PageTemplate) templates.get(pt_oldid));
                    }
                }
                try {
                    br_pts.close();
                } catch (Exception e1) {
                }
            }
            String entry_varsname = "TOPIC" + top_oldid + ".vars";
            ZipEntry entry_vars = file.getEntry(entry_varsname);
            if (entry_vars != null) {
                InputStream in_vars = file.getInputStream(entry_vars);
                java.io.InputStreamReader r_vars = new InputStreamReader(in_vars, "UTF-8");
                java.io.BufferedReader br_vars = new BufferedReader(r_vars);
                while ((s = br_vars.readLine()) != null) {
                    String var_name = s.trim();
                    String var_value = br_vars.readLine();
                    aTopic.AddVar(var_name, var_value);
                }
                try {
                    br_vars.close();
                } catch (Exception e1) {
                }
            }
            aTopicTree.SaveTopic(ctxt, aTopic);
            aTopicTree.UpdateTopicPageTemplate(ctxt, aTopic);
            aTopicTree.UpdateTopicVars(ctxt, aTopic);
            aTopicTree.AddTopic(aTopic);
            topic_indexby_oldid.put(top_oldid, aTopic);
        }
        try {
            list_br.close();
        } catch (Exception e1) {
        }
        return aTopicTree;
    }

    private void CorrectLayerInDB(NpsContext inCtxt) throws NpsException {
        PreparedStatement pstmt = null;
        PreparedStatement pstmt_update = null;
        ResultSet rs = null;
        String sql = null;
        try {
            sql = "select id,code,layer from topic where layer is null";
            pstmt = inCtxt.GetConnection().prepareStatement(sql);
            rs = pstmt.executeQuery();
            sql = "update topic set layer=? where id=?";
            pstmt_update = inCtxt.GetConnection().prepareStatement(sql);
            while (rs.next()) {
                pstmt_update.setInt(1, GetLayer(rs.getString("code")));
                pstmt_update.setString(2, rs.getString("id"));
                pstmt_update.executeUpdate();
            }
            inCtxt.Commit();
        } catch (Exception e) {
            inCtxt.Rollback();
            com.microfly.util.DefaultLog.error(e);
        } finally {
            if (rs != null) try {
                rs.close();
            } catch (Exception e) {
            }
            ;
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
            ;
            if (pstmt_update != null) try {
                pstmt_update.close();
            } catch (Exception e) {
            }
            ;
        }
    }
}
