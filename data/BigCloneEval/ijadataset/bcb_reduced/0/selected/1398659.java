package nps.core;

import nps.util.tree.TreeNode;
import nps.util.Utils;
import java.util.*;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.File;
import java.io.Serializable;
import com.microfly.core.common.Constants;
import nps.exception.NpsException;
import nps.event.*;

/**
 *  2.2008.10.18 jialin
 *     �������Զ������Դʱ�Զ������¼��ļ���ȡ��ʱ���Զ�ȡ��
 * 
 *  1.2008.09.28 jialin
 *     ������Ŀ�Զ������
 * 
 *  a new publishing system
 *  Copyright (c) 2007
 *
 * @author jialin
 * @version 1.0
 */
public class Topic implements TreeNode, Constants, IPublishable, IPortable, Serializable, InsertEventListener, UpdateEventListener, DeleteEventListener, Ready2PublishEventListener, PublishEventListener, CancelEventListener {

    private Site site;

    private String id;

    private String parentid;

    private String alias = null;

    private String code = null;

    private String name = null;

    private int order = 0;

    private int default_article_state = ARTICLE_DRAFT;

    private float default_article_score = 0;

    private String table = null;

    private boolean visible = true;

    private ArticleTemplate article_template = null;

    private List page_templates = null;

    private Hashtable owners = null;

    private Hashtable vars = null;

    public Topic(Site site, String parentid, String id, String name, String alias, String code, int order) {
        this.site = site;
        this.parentid = parentid;
        this.id = id;
        this.name = name;
        this.alias = alias;
        if (code != null) this.code = code.toLowerCase();
        this.order = order;
    }

    public Topic(String newid, Topic parent, Topic src) {
        this.site = parent.GetSite();
        this.parentid = parent.GetId();
        this.id = newid;
        this.name = src.GetName();
        this.alias = src.GetAlias();
        this.code = parent.GetCode() + "." + src.GetAlias();
        this.code = this.code.toLowerCase();
        this.order = src.GetOrder();
        this.default_article_state = src.GetDefaultArticleState();
        this.default_article_score = src.GetScore();
        this.table = src.GetTable();
        this.visible = src.visible;
        this.article_template = src.GetArticleTemplate();
        this.page_templates = null;
        List src_page_templates = src.GetPageTemplates();
        if (src_page_templates != null && !src_page_templates.isEmpty()) {
            this.page_templates = Collections.synchronizedList(new ArrayList(src_page_templates.size()));
            for (Object obj : src_page_templates) {
                this.AddPageTemplate((PageTemplate) obj);
            }
        }
        this.owners = null;
        Hashtable src_owners = src.GetOwner();
        if (src_owners != null && !src_owners.isEmpty()) {
            this.owners = new Hashtable(src_owners.size());
            java.util.Enumeration src_owners_list = src_owners.elements();
            while (src_owners_list.hasMoreElements()) {
                Owner aOwner = (Owner) src_owners_list.nextElement();
                this.owners.put(aOwner.uid, aOwner.uname);
            }
        }
        this.vars = null;
        Hashtable src_vars = src.GetVars();
        if (src_vars != null && !src_vars.isEmpty()) {
            this.vars = new Hashtable(src_vars.size());
            java.util.Enumeration src_vars_list = src_vars.elements();
            while (src_vars_list.hasMoreElements()) {
                Var var = (Var) src_vars_list.nextElement();
                this.vars.put(var.name, var.value);
            }
        }
    }

    public Topic(String newid, Site site, Topic src) {
        this.site = site;
        this.parentid = "-1";
        this.id = newid;
        this.name = src.GetName();
        this.alias = src.GetAlias();
        this.code = src.GetAlias();
        this.code = this.code.toLowerCase();
        this.order = src.GetOrder();
        this.default_article_state = src.GetDefaultArticleState();
        this.default_article_score = src.GetScore();
        this.table = src.GetTable();
        this.visible = src.visible;
        this.article_template = src.GetArticleTemplate();
        this.page_templates = null;
        List src_page_templates = src.GetPageTemplates();
        if (src_page_templates != null && !src_page_templates.isEmpty()) {
            this.page_templates = Collections.synchronizedList(new ArrayList(src_page_templates.size()));
            for (Object obj : src_page_templates) {
                this.AddPageTemplate((PageTemplate) obj);
            }
        }
        this.owners = null;
        Hashtable src_owners = src.GetOwner();
        if (src_owners != null && !src_owners.isEmpty()) {
            this.owners = new Hashtable(src_owners.size());
            java.util.Enumeration src_owners_list = src_owners.elements();
            while (src_owners_list.hasMoreElements()) {
                Owner aOwner = (Owner) src_owners_list.nextElement();
                this.owners.put(aOwner.uid, aOwner.uname);
            }
        }
        this.vars = null;
        Hashtable src_vars = src.GetVars();
        if (src_vars != null && !src_vars.isEmpty()) {
            this.vars = new Hashtable(src_vars.size());
            java.util.Enumeration src_vars_list = src_vars.elements();
            while (src_vars_list.hasMoreElements()) {
                Var var = (Var) src_vars_list.nextElement();
                this.vars.put(var.name, var.value);
            }
        }
    }

    public boolean IsCustom() {
        return table != null && table.length() > 0;
    }

    public Site GetSite() {
        return site;
    }

    public String GetSiteId() {
        return site.GetId();
    }

    protected void SetSite(Site site) {
        this.site = site;
    }

    public String GetCode() {
        return code;
    }

    public void SetCode(String c) {
        if (c != null) c = c.toLowerCase();
        code = c;
    }

    public void SetName(String n) {
        name = n;
    }

    public String GetName() {
        return name;
    }

    public void SetAlias(String s) {
        alias = s;
    }

    public String GetAlias() {
        return alias;
    }

    public void SetOrder(int i) {
        order = i;
    }

    public int GetOrder() {
        return order;
    }

    public boolean IsVisible() {
        return visible;
    }

    public void SetVisible(boolean b) {
        this.visible = b;
    }

    public void SetDefaultArticleState(int state) {
        this.default_article_state = state;
    }

    public int GetDefaultArticleState() {
        return default_article_state;
    }

    public float GetScore() {
        return default_article_score;
    }

    public void SetScore(float x) {
        default_article_score = x;
    }

    public void SetArticleTemplate(ArticleTemplate t) {
        this.article_template = t;
    }

    public ArticleTemplate GetArticleTemplate() {
        return article_template;
    }

    public void ClearPageTemplates() {
        page_templates = null;
    }

    public void AddPageTemplate(PageTemplate t) {
        if (t == null) return;
        if (page_templates == null) page_templates = Collections.synchronizedList(new ArrayList());
        page_templates.add(t);
    }

    public List GetPageTemplates() {
        return page_templates;
    }

    public PageTemplate GetPageTemplate(int i) {
        if (page_templates == null || page_templates.isEmpty()) return null;
        return (PageTemplate) page_templates.get(i);
    }

    public PageTemplate GetPageTemplate(String id) {
        if (page_templates == null || page_templates.isEmpty()) return null;
        PageTemplate aTemplate = null;
        Iterator i_page_templates = page_templates.iterator();
        while (i_page_templates.hasNext()) {
            aTemplate = (PageTemplate) i_page_templates.next();
            if (aTemplate.GetId().equalsIgnoreCase(id)) {
                return aTemplate;
            }
        }
        return null;
    }

    public void RemovePageTemplate(String id) {
        if (page_templates == null || page_templates.isEmpty()) return;
        if (id == null || id.length() == 0) return;
        for (int i = 0; i < page_templates.size(); i++) {
            PageTemplate aTemplate = (PageTemplate) page_templates.get(i);
            if (aTemplate.GetId().equalsIgnoreCase(id)) {
                page_templates.remove(i);
                return;
            }
        }
    }

    public String GetTable() {
        return table;
    }

    public void SetTable(String t) {
        if (t.length() == 0) t = null;
        if (table == null && t != null) {
            String key = t.toUpperCase();
            EventSubscriber.GetSubscriber().AddListener((Ready2PublishEventListener) this, key);
            EventSubscriber.GetSubscriber().AddListener((InsertEventListener) this, key);
            EventSubscriber.GetSubscriber().AddListener((UpdateEventListener) this, key);
            EventSubscriber.GetSubscriber().AddListener((DeleteEventListener) this, key);
            EventSubscriber.GetSubscriber().AddListener((PublishEventListener) this, key);
            EventSubscriber.GetSubscriber().AddListener((CancelEventListener) this, key);
        } else if (table != null && t == null) {
            EventSubscriber.GetSubscriber().RemoveListener(Ready2PublishEventListener.class, this);
            EventSubscriber.GetSubscriber().RemoveListener(InsertEventListener.class, this);
            EventSubscriber.GetSubscriber().RemoveListener(UpdateEventListener.class, this);
            EventSubscriber.GetSubscriber().RemoveListener(DeleteEventListener.class, this);
            EventSubscriber.GetSubscriber().RemoveListener(PublishEventListener.class, this);
            EventSubscriber.GetSubscriber().RemoveListener(CancelEventListener.class, this);
        }
        table = t;
    }

    public Hashtable GetOwner() {
        return owners;
    }

    public void ClearOwner() {
        owners = null;
    }

    public void AddOwner(String uid, String uname) {
        Owner aOwner = new Owner(uid, uname);
        if (owners == null) {
            owners = new Hashtable();
        } else if (!owners.isEmpty()) {
            owners.remove(uid);
        }
        owners.put(uid, aOwner);
    }

    public void RemoveOwner(String uid) {
        if (uid == null || uid.length() == 0) return;
        if (owners == null || owners.isEmpty()) return;
        owners.remove(uid);
    }

    public boolean IsOwner(String uid) {
        if (owners != null && owners.containsKey(uid)) return true;
        Topic parent = GetParent();
        if (parent != null) {
            return parent.IsOwner(uid);
        }
        return false;
    }

    public Hashtable GetVars() {
        return vars;
    }

    public Var GetVar(String key) {
        if (key == null || key.length() == 0) return null;
        if (vars == null || vars.isEmpty()) return null;
        return (Var) vars.get(key.toUpperCase());
    }

    public String GetVarValue(String key) {
        if (key == null || key.length() == 0) return null;
        if (vars == null || vars.isEmpty()) return null;
        Var var = (Var) vars.get(key.toUpperCase());
        if (var != null) return var.value;
        return null;
    }

    public void AddVar(String name, String value) {
        String key = name.toUpperCase();
        Var var = new Var(key, value);
        if (vars == null) {
            vars = new Hashtable();
        } else if (!vars.isEmpty()) {
            vars.remove(key);
        }
        vars.put(key, var);
    }

    public void RemoveVar(String name) {
        if (name == null || name.length() == 0) return;
        if (vars == null || vars.isEmpty()) return;
        vars.remove(name.toUpperCase());
    }

    public boolean HasVar(String name) {
        if (name == null || name.length() == 0) return false;
        if (vars != null && vars.containsKey(name.toUpperCase())) return true;
        return false;
    }

    public void ClearVars() {
        if (vars != null) vars.clear();
    }

    public Topic GetParent() {
        if ("-1".equals(parentid) || parentid == null || parentid.length() == 0) return null;
        return site.GetTopicTree().GetTopic(parentid);
    }

    public String GetId() {
        return id;
    }

    public String GetParentId() {
        return parentid;
    }

    public void SetParentId(String s) {
        this.parentid = s;
    }

    public int GetIndex() {
        return order;
    }

    public int GetLayer() {
        int layer = 0;
        int pos_dot = code.indexOf(".");
        if (pos_dot == -1) return layer;
        while (pos_dot != -1) {
            layer++;
            pos_dot = code.indexOf(".", pos_dot + 1);
        }
        return layer;
    }

    public String GetNavCode(Topic topic) {
        return GetNavCode(topic, " \\> ");
    }

    public String GetNavCode(Topic topic, String split) {
        if (topic == null) return null;
        String nav_code = "\\<a href=\\\"" + topic.GetURL() + "\\\"\\>" + topic.GetName() + "\\</a\\>";
        String nav_parent = GetNavCode(topic.GetParent(), split);
        if (nav_parent == null) return nav_code;
        return nav_parent + split + nav_code;
    }

    public String GetURL() {
        String url = site.GetRootURL() + "/" + code.replaceAll("[.]", "/") + "/";
        url = Utils.FixURL(url);
        return url;
    }

    public String GetPath() {
        String path = code.replaceAll("[.]", "/");
        path = "/" + path + "/";
        path = Utils.FixURL(path);
        return path;
    }

    public File GetOutputFile() {
        return null;
    }

    public boolean HasField(String fieldName) {
        if (fieldName == null || fieldName.length() == 0) return false;
        String key = fieldName.trim();
        if (key.length() == 0) return false;
        key = key.toUpperCase();
        if (key.equalsIgnoreCase("top_id")) return true;
        if (key.equalsIgnoreCase("top_name")) return true;
        if (key.equalsIgnoreCase("top_code")) return true;
        if (key.equalsIgnoreCase("top_alias")) return true;
        if (key.equalsIgnoreCase("top_url")) return true;
        if (key.equalsIgnoreCase("top_path")) return true;
        if (key.equalsIgnoreCase("top_navigator")) return true;
        if (key.equalsIgnoreCase("top_nav")) return true;
        if (key.equalsIgnoreCase("top_parentid")) return true;
        if (key.equalsIgnoreCase("top_parentname")) return true;
        if (key.equalsIgnoreCase("top_parentcode")) return true;
        if (key.equalsIgnoreCase("top_parentalias")) return true;
        if (key.equalsIgnoreCase("top_parenturl")) return true;
        if (key.equalsIgnoreCase("top_parentpath")) return true;
        if (key.equalsIgnoreCase("top_layer")) return true;
        return HasVar(key);
    }

    public Object GetField(String fieldName) throws NpsException {
        if (fieldName == null || fieldName.length() == 0) return null;
        String key = fieldName.trim();
        if (key.length() == 0) return null;
        key = key.toUpperCase();
        if (key.equalsIgnoreCase("top_id")) return id;
        if (key.equalsIgnoreCase("top_name")) return name;
        if (key.equalsIgnoreCase("top_code")) return code;
        if (key.equalsIgnoreCase("top_alias")) return alias;
        if (key.equalsIgnoreCase("top_url")) return GetURL();
        if (key.equalsIgnoreCase("top_path")) return GetPath();
        if (key.equalsIgnoreCase("top_navigator")) return GetNavCode(this);
        if (key.equalsIgnoreCase("top_nav")) return GetNavCode(this);
        if (key.equalsIgnoreCase("top_parentid")) {
            Topic parent = GetParent();
            return parent == null ? "" : parent.GetId();
        }
        if (key.equalsIgnoreCase("top_parentname")) {
            Topic parent = GetParent();
            return parent == null ? "" : parent.GetName();
        }
        if (key.equalsIgnoreCase("top_parentcode")) {
            Topic parent = GetParent();
            return parent == null ? "" : parent.GetCode();
        }
        if (key.equalsIgnoreCase("top_parentalias")) {
            Topic parent = GetParent();
            return parent == null ? "" : parent.GetAlias();
        }
        if (key.equalsIgnoreCase("top_parenturl")) {
            Topic parent = GetParent();
            return parent == null ? "" : parent.GetURL();
        }
        if (key.equalsIgnoreCase("top_parentpath")) {
            Topic parent = GetParent();
            return parent == null ? "" : parent.GetPath();
        }
        if (key.equalsIgnoreCase("top_layer")) return GetLayer();
        if (key.startsWith("SITE_") && site.HasField(key)) return site.GetField(key);
        if (key.startsWith("UNIT_") && site.GetUnit().HasField(key)) return site.GetUnit().GetField(key);
        if (HasVar(key)) {
            return GetVarValue(key);
        }
        if (site.HasField(key)) return site.GetField(key);
        return null;
    }

    public String GetField(String fieldName, int wordcount) throws NpsException {
        return GetField(fieldName, wordcount, "");
    }

    public String GetField(String fieldName, String format) throws NpsException {
        if ("top_navigator".equalsIgnoreCase(fieldName) || "top_nav".equalsIgnoreCase(fieldName)) {
            return GetNavCode(this, format);
        }
        Object fld_obj = GetField(fieldName);
        if (fld_obj == null) return null;
        if (fld_obj instanceof java.util.Date) {
            return nps.util.Utils.FormateDate((java.util.Date) fld_obj, format);
        }
        if (fld_obj instanceof java.lang.Number) {
            return nps.util.Utils.FormateNumber(fld_obj, format);
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
        if (fld_obj instanceof java.util.Date) {
            return nps.util.Utils.FormateDate((java.util.Date) fld_obj, "yyyy-MM-dd");
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
        ZipInfo(out);
        ZipPageTemplates(out);
        ZipOwners(out);
        ZipVars(out);
        if (article_template != null) article_template.Zip(ctxt, out);
        if (!(page_templates == null || page_templates.isEmpty())) {
            for (Object obj : page_templates) {
                PageTemplate pt = (PageTemplate) obj;
                if (pt != null) pt.Zip(ctxt, out);
            }
        }
    }

    private void ZipInfo(ZipOutputStream out) throws Exception {
        String filename = "TOPIC" + GetId() + ".topic";
        out.putNextEntry(new ZipEntry(filename));
        try {
            ZipWriter writer = new ZipWriter(out);
            writer.println(id);
            writer.println(parentid);
            writer.println(site.GetId());
            writer.println(name);
            writer.println(alias);
            writer.println(code);
            writer.println(order);
            writer.println(default_article_state);
            writer.println(default_article_score);
            writer.println(table);
            if (article_template == null) writer.println(); else writer.println(article_template.GetId());
            writer.println(visible ? 1 : 0);
        } finally {
            out.closeEntry();
        }
    }

    private void ZipPageTemplates(ZipOutputStream out) throws Exception {
        if (page_templates == null || page_templates.isEmpty()) return;
        String filename = "TOPIC" + GetId() + ".pts";
        out.putNextEntry(new ZipEntry(filename));
        try {
            ZipWriter writer = new ZipWriter(out);
            for (Object obj : page_templates) {
                PageTemplate pt = (PageTemplate) obj;
                if (pt != null) {
                    writer.println(pt.GetId());
                }
            }
        } finally {
            out.closeEntry();
        }
    }

    private void ZipOwners(ZipOutputStream out) throws Exception {
        if (owners == null || owners.isEmpty()) return;
        String filename = "TOPIC" + GetId() + ".owners";
        out.putNextEntry(new ZipEntry(filename));
        try {
            ZipWriter writer = new ZipWriter(out);
            java.util.Enumeration owners_enum = owners.elements();
            while (owners_enum.hasMoreElements()) {
                Owner owner = (Owner) owners_enum.nextElement();
                if (owner != null) writer.println(owner.GetID());
            }
        } finally {
            out.closeEntry();
        }
    }

    private void ZipVars(ZipOutputStream out) throws Exception {
        if (vars == null || vars.isEmpty()) return;
        String filename = "TOPIC" + GetId() + ".vars";
        out.putNextEntry(new ZipEntry(filename));
        try {
            ZipWriter writer = new ZipWriter(out);
            java.util.Enumeration vars_enum = vars.elements();
            while (vars_enum.hasMoreElements()) {
                Var var = (Var) vars_enum.nextElement();
                if (var != null) {
                    writer.println(var.name);
                    writer.println(Utils.Null2Empty(var.value));
                }
            }
        } finally {
            out.closeEntry();
        }
    }

    public class Owner {

        protected String uid;

        protected String uname;

        public Owner(String uid, String uname) {
            this.uid = uid;
            this.uname = uname;
        }

        public String GetID() {
            return uid;
        }

        public String GetName() {
            return uname;
        }
    }

    public class Var {

        protected String name;

        protected String value;

        public Var(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String GetName() {
            return name;
        }

        public String GetValue() {
            return value;
        }
    }

    public void DataInserted(InsertEvent e) {
        IEventAction act = (IEventAction) e.getSource();
        act.Insert(this, e);
    }

    public void DataUpdated(UpdateEvent e) {
        IEventAction act = (IEventAction) e.getSource();
        act.Update(this, e);
    }

    public void DataDeleted(DeleteEvent e) {
        IEventAction act = (IEventAction) e.getSource();
        act.Delete(this, e);
    }

    public void DataReady(Ready2PublishEvent e) {
        IEventAction act = (IEventAction) e.getSource();
        act.Ready(this, e);
    }

    public void DataPublished(PublishEvent e) {
        IEventAction act = (IEventAction) e.getSource();
        act.Publish(this, e);
    }

    public void DataCancelled(CancelEvent e) {
        IEventAction act = (IEventAction) e.getSource();
        act.Cancel(this, e);
    }
}
