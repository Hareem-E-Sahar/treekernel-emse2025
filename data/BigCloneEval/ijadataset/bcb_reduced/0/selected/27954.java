package nps.core;

import java.io.*;
import java.util.*;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import com.microfly.core.common.Constants;
import nps.exception.NpsException;
import nps.compiler.PageClassBase;
import nps.exception.ErrorHelper;
import nps.util.Utils;

/**
 * 2008.09.28 jialin
 *    ����վ��ȫ�ֱ�������
 * 
 *  a new publishing system
 *  Copyright (c) 2007
 *
 * @author jialin
 * @version 1.0
 */
public class Site implements Constants, IPublishable, IPortable, Serializable {

    private String id;

    private String name;

    private String domain;

    private String rooturl = DEFAULT_SITE_ROOTURL;

    private File art_publish_dir;

    private String art_suffix = DEFAULT_SITE_ART_SUFFIX;

    private File img_publish_dir;

    private String img_rooturl = DEFAULT_SITE_IMG_ROOTURL;

    private Unit unit = null;

    private int state = SITE_NORMAL;

    private boolean fulltext_index = true;

    private String solr_core = null;

    private List article_ftp_hosts = null;

    private List img_ftp_hosts = null;

    private Hashtable owners = null;

    private TopicTree topic_tree = null;

    private Hashtable vars = null;

    public Site(String inId, String inName, File inArtPubDir, Unit inUnit) throws NpsException {
        id = inId;
        name = inName;
        unit = inUnit;
        art_publish_dir = null;
        img_publish_dir = null;
        SetArticleDir(inArtPubDir);
        String imgPubDir = null;
        SetImgDir(imgPubDir);
        ResolveURLs();
    }

    public Site(String inId, String inName, String inArtPubDir, Unit inUnit) throws NpsException {
        id = inId;
        name = inName;
        unit = inUnit;
        art_publish_dir = null;
        img_publish_dir = null;
        SetArticleDir(inArtPubDir);
        SetImgDir((String) null);
        ResolveURLs();
    }

    public static Site GetSite(NpsContext ctxt, String site_id) throws NpsException {
        Site site = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        ResultSet rs_host = null;
        PreparedStatement pstmt_host = null;
        ResultSet rs_owner = null;
        PreparedStatement pstmt_owner = null;
        try {
            String sql = "select a.*,b.id unitid,b.name unitname,b.code unitcode from site a,unit b where a.id=? and a.unit=b.id";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, site_id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Unit unit = new Unit(rs.getString("unitid"), rs.getString("unitname"), rs.getString("unitcode"));
                site = new Site(site_id, rs.getString("name"), new File(rs.getString("artpubdir") + File.separator), unit);
                if (rs.getString("domain") != null) site.SetDomain(rs.getString("domain"));
                if (rs.getString("suffix") != null) site.SetSuffix(rs.getString("suffix"));
                if (rs.getString("rooturl") != null) site.SetRootURL(rs.getString("rooturl"));
                if (rs.getString("img_rooturl") != null) site.SetImgURL(rs.getString("img_rooturl"));
                if (rs.getString("img_publish_dir") != null) site.SetImgDir(rs.getString("img_publish_dir") + File.separator);
                if (rs.getInt("state") != SITE_NORMAL) site.state = SITE_FREEZED;
                if (rs.getInt("fulltext") == 0) site.fulltext_index = false;
                site.solr_core = rs.getString("solr_core");
                site.ResolveURLs();
                sql = "select * from site_host where siteid=?";
                pstmt_host = ctxt.GetConnection().prepareStatement(sql);
                pstmt_host.setString(1, site_id);
                rs_host = pstmt_host.executeQuery();
                while (rs_host.next()) {
                    switch(rs_host.getInt("type")) {
                        case 0:
                            site.AddFtp4Article(rs_host.getString("host"), rs_host.getString("remotedir"), rs_host.getInt("port"), rs_host.getString("uname"), rs_host.getString("upasswd"));
                            break;
                        case 1:
                            site.AddFtp4Image(rs_host.getString("host"), rs_host.getString("remotedir"), rs_host.getInt("port"), rs_host.getString("uname"), rs_host.getString("upasswd"));
                            break;
                    }
                }
                if (rs_host != null) try {
                    rs_host.close();
                } catch (Exception e) {
                }
                if (pstmt_host != null) try {
                    pstmt_host.close();
                } catch (Exception e) {
                }
                sql = "select b.Id userid,b.Name username from site_owner a,users b where a.siteid=? and a.userid=b.id";
                pstmt_owner = ctxt.GetConnection().prepareStatement(sql);
                pstmt_owner.setString(1, site_id);
                rs_owner = pstmt_owner.executeQuery();
                while (rs_owner.next()) {
                    site.AddOwner(rs_owner.getString("userid"), rs_owner.getString("username"));
                }
                if (rs_owner != null) try {
                    rs_owner.close();
                } catch (Exception e) {
                }
                if (pstmt_owner != null) try {
                    pstmt_owner.close();
                } catch (Exception e) {
                }
                sql = "select * from site_vars where siteid=?";
                pstmt_owner = ctxt.GetConnection().prepareStatement(sql);
                pstmt_owner.setString(1, site_id);
                rs_owner = pstmt_owner.executeQuery();
                while (rs_owner.next()) {
                    site.AddVar(rs_owner.getString("varname"), rs_owner.getString("value"));
                }
                if (rs_owner != null) try {
                    rs_owner.close();
                } catch (Exception e) {
                }
                if (pstmt_owner != null) try {
                    pstmt_owner.close();
                } catch (Exception e) {
                }
                site.LoadTopicTree(ctxt);
            }
        } catch (Exception e) {
            site = null;
            nps.util.DefaultLog.error(e);
        } finally {
            if (rs_host != null) try {
                rs_host.close();
            } catch (Exception e) {
            }
            if (pstmt_host != null) try {
                pstmt_host.close();
            } catch (Exception e) {
            }
            if (rs_owner != null) try {
                rs_owner.close();
            } catch (Exception e) {
            }
            if (pstmt_owner != null) try {
                pstmt_owner.close();
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
        return site;
    }

    public TopicTree LoadTopicTree(NpsContext ctxt) throws NpsException {
        if (topic_tree == null) {
            topic_tree = TopicTree.LoadTree(ctxt, this, name);
        }
        return topic_tree;
    }

    public TopicTree ReloadTopicTree(NpsContext ctxt) throws NpsException {
        topic_tree = TopicTree.LoadTree(ctxt, this, name);
        return topic_tree;
    }

    public TopicTree GetTopicTree() {
        return topic_tree;
    }

    public void Freeze(NpsContext ctxt) throws NpsException {
        UpdateSiteState(ctxt, SITE_FREEZED);
    }

    public void Defreeze(NpsContext ctxt) throws NpsException {
        UpdateSiteState(ctxt, SITE_NORMAL);
    }

    public void ClearFtps4Article() {
        article_ftp_hosts = null;
    }

    public void AddFtp4Article(String hostname, String remotedir, int remoteport, String uname, String upasswd) {
        FtpHost aFtp = new FtpHost(hostname, remotedir, remoteport, uname, upasswd);
        if (article_ftp_hosts == null) article_ftp_hosts = Collections.synchronizedList(new ArrayList());
        article_ftp_hosts.add(aFtp);
    }

    public void AddFtp4Article(String hostname, String uname, String upasswd) {
        FtpHost aFtp = new FtpHost(hostname, uname, upasswd);
        if (article_ftp_hosts == null) article_ftp_hosts = Collections.synchronizedList(new ArrayList());
        article_ftp_hosts.add(aFtp);
    }

    public void ClearFtps4Image() {
        img_ftp_hosts = null;
    }

    public void AddFtp4Image(String hostname, String remotedir, int remoteport, String uname, String upasswd) {
        FtpHost aFtp = new FtpHost(hostname, remotedir, remoteport, uname, upasswd);
        if (img_ftp_hosts == null) img_ftp_hosts = Collections.synchronizedList(new ArrayList());
        img_ftp_hosts.add(aFtp);
    }

    public void AddFtp4Image(String hostname, String uname, String upasswd) {
        FtpHost aFtp = new FtpHost(hostname, uname, upasswd);
        if (img_ftp_hosts == null) img_ftp_hosts = Collections.synchronizedList(new ArrayList());
        img_ftp_hosts.add(aFtp);
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

    public boolean IsOwner(String uid) {
        if (owners != null && owners.containsKey(uid)) return true;
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

    public void Add2Ftp(Article art) {
        if (article_ftp_hosts == null || article_ftp_hosts.isEmpty()) return;
        if (art == null) return;
        for (Object obj : article_ftp_hosts) {
            FtpHost ftphost = (FtpHost) obj;
            FtpQueue ftp_queue = new FtpQueue(ftphost);
            ftp_queue = FtpScheduler.GetScheduler().Add(ftp_queue);
            File local_file = art.GetOutputFile();
            String str_art_publish_dir = art_publish_dir.getAbsolutePath();
            str_art_publish_dir = Utils.FixPath(str_art_publish_dir);
            String remote_file = local_file.getAbsolutePath();
            remote_file = Utils.FixPath(remote_file);
            remote_file = remote_file.replaceFirst(str_art_publish_dir, ftphost.remotedir);
            remote_file = Utils.FixPath(remote_file);
            try {
                ftp_queue.AddTask(local_file, remote_file);
            } catch (NpsException e) {
                nps.util.DefaultLog.error_noexception(e);
                continue;
            }
        }
    }

    public void Add2Ftp(Article art, File page) {
        if (article_ftp_hosts == null || article_ftp_hosts.isEmpty()) return;
        if (art == null) return;
        for (Object obj : article_ftp_hosts) {
            FtpHost ftphost = (FtpHost) obj;
            FtpQueue ftp_queue = new FtpQueue(ftphost);
            ftp_queue = FtpScheduler.GetScheduler().Add(ftp_queue);
            File local_file = page;
            String str_art_publish_dir = art_publish_dir.getAbsolutePath();
            str_art_publish_dir = Utils.FixPath(str_art_publish_dir);
            String remote_file = local_file.getAbsolutePath();
            remote_file = Utils.FixPath(remote_file);
            remote_file = remote_file.replaceFirst(str_art_publish_dir, ftphost.remotedir);
            remote_file = Utils.FixPath(remote_file);
            try {
                ftp_queue.AddTask(local_file, remote_file);
            } catch (NpsException e) {
                nps.util.DefaultLog.error_noexception(e);
                continue;
            }
        }
    }

    public void Add2Ftp(Attach att) {
        if (img_ftp_hosts == null || img_ftp_hosts.isEmpty()) return;
        if (att == null) return;
        for (Object obj : img_ftp_hosts) {
            FtpHost ftphost = (FtpHost) obj;
            FtpQueue ftp_queue = new FtpQueue(ftphost);
            ftp_queue = FtpScheduler.GetScheduler().Add(ftp_queue);
            File local_file = att.GetOutputFile();
            String str_att_publish_dir = img_publish_dir.getAbsolutePath();
            str_att_publish_dir = Utils.FixPath(str_att_publish_dir);
            String remote_file = local_file.getAbsolutePath();
            remote_file = Utils.FixPath(remote_file);
            remote_file = remote_file.replaceFirst(str_att_publish_dir, ftphost.remotedir);
            remote_file = Utils.FixPath(remote_file);
            try {
                ftp_queue.AddTask(local_file, remote_file);
            } catch (NpsException e) {
                nps.util.DefaultLog.error_noexception(e);
                continue;
            }
        }
    }

    public void Add2Ftp(PageClassBase pcb) {
        if (article_ftp_hosts == null || article_ftp_hosts.isEmpty()) return;
        if (pcb == null) return;
        for (Object obj : article_ftp_hosts) {
            FtpHost ftphost = (FtpHost) obj;
            FtpQueue ftp_queue = new FtpQueue(ftphost);
            ftp_queue = FtpScheduler.GetScheduler().Add(ftp_queue);
            File local_file = pcb.GetOutputFile();
            String str_art_publish_dir = art_publish_dir.getAbsolutePath();
            str_art_publish_dir = Utils.FixPath(str_art_publish_dir);
            String remote_file = local_file.getAbsolutePath();
            remote_file = Utils.FixPath(remote_file);
            remote_file = remote_file.replaceFirst(str_art_publish_dir, ftphost.remotedir);
            remote_file = Utils.FixPath(remote_file);
            try {
                ftp_queue.AddTask(local_file, remote_file);
            } catch (NpsException e) {
                nps.util.DefaultLog.error_noexception(e);
                continue;
            }
        }
    }

    public void Add2Ftp(PageClassBase pcb, File page) {
        if (article_ftp_hosts == null || article_ftp_hosts.isEmpty()) return;
        if (pcb == null) return;
        for (Object obj : article_ftp_hosts) {
            FtpHost ftphost = (FtpHost) obj;
            FtpQueue ftp_queue = new FtpQueue(ftphost);
            ftp_queue = FtpScheduler.GetScheduler().Add(ftp_queue);
            File local_file = page;
            String str_art_publish_dir = art_publish_dir.getAbsolutePath();
            str_art_publish_dir = Utils.FixPath(str_art_publish_dir);
            String remote_file = local_file.getAbsolutePath();
            remote_file = Utils.FixPath(remote_file);
            remote_file = remote_file.replaceFirst(str_art_publish_dir, ftphost.remotedir);
            remote_file = Utils.FixPath(remote_file);
            try {
                ftp_queue.AddTask(local_file, remote_file);
            } catch (NpsException e) {
                nps.util.DefaultLog.error_noexception(e);
                continue;
            }
        }
    }

    public void Add2Ftp(File local_file) {
        if (article_ftp_hosts == null || article_ftp_hosts.isEmpty()) return;
        if (local_file == null || !local_file.exists() || !local_file.isFile()) return;
        for (Object obj : article_ftp_hosts) {
            FtpHost ftphost = (FtpHost) obj;
            FtpQueue ftp_queue = new FtpQueue(ftphost);
            ftp_queue = FtpScheduler.GetScheduler().Add(ftp_queue);
            String str_art_publish_dir = art_publish_dir.getAbsolutePath();
            str_art_publish_dir = Utils.FixPath(str_art_publish_dir);
            String remote_file = local_file.getAbsolutePath();
            remote_file = Utils.FixPath(remote_file);
            remote_file = remote_file.replaceFirst(str_art_publish_dir, ftphost.remotedir);
            remote_file = Utils.FixPath(remote_file);
            try {
                ftp_queue.AddTask(local_file, remote_file);
            } catch (NpsException e) {
                nps.util.DefaultLog.error_noexception(e);
                continue;
            }
        }
    }

    public void AddImage2Ftp(File local_file) {
        if (img_ftp_hosts == null || img_ftp_hosts.isEmpty()) return;
        if (local_file == null || !local_file.exists() || !local_file.isFile()) return;
        for (Object obj : img_ftp_hosts) {
            FtpHost ftphost = (FtpHost) obj;
            FtpQueue ftp_queue = new FtpQueue(ftphost);
            ftp_queue = FtpScheduler.GetScheduler().Add(ftp_queue);
            String str_att_publish_dir = img_publish_dir.getAbsolutePath();
            str_att_publish_dir = Utils.FixPath(str_att_publish_dir);
            String remote_file = local_file.getAbsolutePath();
            remote_file = Utils.FixPath(remote_file);
            remote_file = remote_file.replaceFirst(str_att_publish_dir, ftphost.remotedir);
            remote_file = Utils.FixPath(remote_file);
            try {
                ftp_queue.AddTask(local_file, remote_file);
            } catch (NpsException e) {
                nps.util.DefaultLog.error_noexception(e);
                continue;
            }
        }
    }

    public void Add2Ftp(String remote) {
        if (article_ftp_hosts == null || article_ftp_hosts.isEmpty()) return;
        for (Object obj : article_ftp_hosts) {
            FtpHost ftphost = (FtpHost) obj;
            FtpQueue ftp_queue = new FtpQueue(ftphost);
            ftp_queue = FtpScheduler.GetScheduler().Add(ftp_queue);
            try {
                ftp_queue.AddTask(remote);
            } catch (NpsException e) {
                nps.util.DefaultLog.error_noexception(e);
                continue;
            }
        }
    }

    public void Add2UserDir(File local_file) {
        if (article_ftp_hosts == null || article_ftp_hosts.isEmpty()) return;
        for (Object obj : article_ftp_hosts) {
            FtpHost ftphost = (FtpHost) obj;
            FtpQueue ftp_queue = new FtpQueue(ftphost);
            ftp_queue = FtpScheduler.GetScheduler().Add(ftp_queue);
            String str_art_publish_dir = art_publish_dir.getAbsolutePath();
            str_art_publish_dir = Utils.FixPath(str_art_publish_dir);
            String remote_file = local_file.getAbsolutePath();
            remote_file = Utils.FixPath(remote_file);
            remote_file = remote_file.replaceFirst(str_art_publish_dir, ftphost.remotedir);
            remote_file = Utils.FixPath(remote_file);
            try {
                ftp_queue.AddTask(local_file, remote_file);
            } catch (NpsException e) {
                nps.util.DefaultLog.error_noexception(e);
                continue;
            }
        }
    }

    public void ResolveURLs() {
        if (!art_publish_dir.exists()) art_publish_dir.mkdirs();
        if (!img_publish_dir.exists()) img_publish_dir.mkdirs();
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

    public void SetDomain(String domain) {
        this.domain = domain;
    }

    public void SetRootURL(String rooturl) {
        this.rooturl = rooturl;
    }

    public void SetUnit(Unit u) {
        unit = u;
    }

    public String GetDomain() {
        return domain;
    }

    public String GetRootURL() {
        return rooturl;
    }

    public String GetSuffix() {
        return art_suffix;
    }

    public Unit GetUnit() {
        return unit;
    }

    public int GetState() {
        return state;
    }

    public File GetArticleDir() {
        return art_publish_dir;
    }

    public String GetImgURL() {
        return img_rooturl;
    }

    public File GetImgDir() {
        return img_publish_dir;
    }

    public String GetId() {
        return id;
    }

    public String GetName() {
        return name;
    }

    public List GetImageFtpHosts() {
        return img_ftp_hosts;
    }

    public List GetArticleFtpHosts() {
        return article_ftp_hosts;
    }

    public Hashtable GetOwner() {
        return owners;
    }

    public void SetName(String s) {
        name = s;
    }

    public void SetSuffix(String art_suffix) {
        if (!art_suffix.startsWith(".")) art_suffix = "." + art_suffix;
        this.art_suffix = art_suffix;
    }

    public void SetArticleDir(String dir) throws NpsException {
        if (dir == null) throw new NpsException("site article dir not specified", ErrorHelper.INPUT_ERROR);
        dir = Utils.FixPath(dir);
        if (!dir.endsWith("/")) dir += "/";
        SetArticleDir(new File(dir));
    }

    public void SetArticleDir(File art_publish_dir) throws NpsException {
        this.art_publish_dir = art_publish_dir;
        art_publish_dir.mkdirs();
    }

    public void SetImgURL(String img_rooturl) {
        this.img_rooturl = img_rooturl;
    }

    public void SetImgDir(String dir) throws NpsException {
        if (dir == null || dir.length() == 0) {
            dir = art_publish_dir.getAbsolutePath() + File.separator + "images" + File.separator;
        }
        dir = Utils.FixPath(dir);
        if (!dir.endsWith("/")) dir += "/";
        SetImgDir(new File(dir));
    }

    public void SetImgDir(File img_publish_dir) throws NpsException {
        this.img_publish_dir = img_publish_dir;
    }

    public boolean IsFulltextIndex() {
        return fulltext_index;
    }

    public void SetFulltextIndex(boolean b) {
        fulltext_index = b;
    }

    public String GetSolrCore() {
        return solr_core == null ? "npscore" : solr_core;
    }

    public void SetSolrCore(String core) {
        solr_core = core;
    }

    public String GetURL() {
        return rooturl;
    }

    public File GetOutputFile() {
        return null;
    }

    public boolean HasField(String fieldName) {
        if (fieldName == null || fieldName.length() == 0) return false;
        String key = fieldName.trim();
        if (key.length() == 0) return false;
        key = key.toUpperCase();
        if (key.equalsIgnoreCase("site_id")) return true;
        if (key.equalsIgnoreCase("site_name")) return true;
        if (key.equalsIgnoreCase("site_domain")) return true;
        if (HasVar(key)) return true;
        return false;
    }

    public Object GetField(String fieldName) throws NpsException {
        if (fieldName == null || fieldName.length() == 0) return null;
        String key = fieldName.trim();
        if (key.length() == 0) return null;
        key = key.toUpperCase();
        if (key.equalsIgnoreCase("site_id")) return id;
        if (key.equalsIgnoreCase("site_name")) return name;
        if (key.equalsIgnoreCase("site_domain")) return domain;
        if (HasVar(key)) {
            return GetVarValue(key);
        }
        return null;
    }

    public String GetField(String fieldName, int wordcount) throws NpsException {
        return GetField(fieldName, wordcount, "");
    }

    public String GetField(String fieldName, String format) throws NpsException {
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
        GetUnit().Zip(ctxt, out);
        ZipInfo(out);
        ZipArticleFtps(out);
        ZipImageFtps(out);
        ZipOwners(out);
        ZipVars(out);
        GetTopicTree().Zip(ctxt, out);
    }

    private void ZipInfo(ZipOutputStream out) throws Exception {
        String filename = "SITE" + GetId() + ".site";
        out.putNextEntry(new ZipEntry(filename));
        ZipWriter writer = new ZipWriter(out);
        try {
            writer.println(id);
            writer.println(name);
            writer.println(rooturl);
            writer.println(Utils.FixPath(art_publish_dir.getAbsolutePath()));
            writer.println(art_suffix);
            writer.println(Utils.FixPath(img_publish_dir.getAbsolutePath()));
            writer.println(img_rooturl);
            writer.println(unit.GetId());
            writer.println(state);
            writer.println(domain);
            writer.println(fulltext_index ? "1" : "0");
            writer.println(Utils.Null2Empty(solr_core));
        } finally {
            out.closeEntry();
        }
    }

    private void ZipArticleFtps(ZipOutputStream out) throws Exception {
        if (article_ftp_hosts == null || article_ftp_hosts.isEmpty()) return;
        String filename = "SITE" + GetId() + ".aftp";
        out.putNextEntry(new ZipEntry(filename));
        try {
            ZipWriter writer = new ZipWriter(out);
            for (Object obj : article_ftp_hosts) {
                FtpHost ahost = (FtpHost) obj;
                writer.println(ahost.GetHostname());
                writer.println(ahost.GetRemotedir());
                writer.println(ahost.GetRemoteport());
                writer.println(ahost.GetUsername());
                writer.println(ahost.GetUserpassword());
            }
        } finally {
            out.closeEntry();
        }
    }

    private void ZipImageFtps(ZipOutputStream out) throws Exception {
        if (img_ftp_hosts == null || img_ftp_hosts.isEmpty()) return;
        String filename = "SITE" + GetId() + ".iftp";
        out.putNextEntry(new ZipEntry(filename));
        try {
            ZipWriter writer = new ZipWriter(out);
            for (Object obj : img_ftp_hosts) {
                FtpHost ahost = (FtpHost) obj;
                writer.println(ahost.GetHostname());
                writer.println(ahost.GetRemotedir());
                writer.println(ahost.GetRemoteport());
                writer.println(ahost.GetUsername());
                writer.println(ahost.GetUserpassword());
            }
        } finally {
            out.closeEntry();
        }
    }

    private void ZipOwners(ZipOutputStream out) throws Exception {
        if (owners == null || owners.isEmpty()) return;
        String filename = "SITE" + GetId() + ".owners";
        out.putNextEntry(new ZipEntry(filename));
        try {
            ZipWriter writer = new ZipWriter(out);
            java.util.Enumeration enum_owners = owners.elements();
            while (enum_owners.hasMoreElements()) {
                Owner owner = (Owner) enum_owners.nextElement();
                writer.println(owner.GetID());
            }
        } finally {
            out.closeEntry();
        }
    }

    private void ZipVars(ZipOutputStream out) throws Exception {
        if (vars == null || vars.isEmpty()) return;
        String filename = "SITE" + GetId() + ".vars";
        out.putNextEntry(new ZipEntry(filename));
        try {
            ZipWriter writer = new ZipWriter(out);
            java.util.Enumeration enum_vars = vars.elements();
            while (enum_vars.hasMoreElements()) {
                Var var = (Var) enum_vars.nextElement();
                writer.println(var.name);
                writer.println(Utils.Null2Empty(var.value));
            }
        } finally {
            out.closeEntry();
        }
    }

    public static Site GetSite(String id, String name, String domain, String artdir, Unit unit, String imgdir, String encoding, String suffix, String rooturl, String imgurl) throws Exception {
        if (id == null || id.length() == 0) throw new NpsException("no site id specified", ErrorHelper.INPUT_ERROR);
        if (name == null || name.length() == 0) throw new NpsException("no site name specified", ErrorHelper.INPUT_ERROR);
        if (artdir == null || artdir.length() == 0) throw new NpsException("no site article dir specified", ErrorHelper.INPUT_ERROR);
        artdir = Utils.FixPath(artdir);
        if (!artdir.endsWith("/")) artdir += "/";
        java.io.File artPubDir = new java.io.File(artdir);
        artPubDir.mkdirs();
        if (!artPubDir.isDirectory()) throw new NpsException(artdir, ErrorHelper.SYS_NOTFILE);
        if (imgdir != null && imgdir.length() > 0) {
            imgdir = Utils.FixPath(imgdir);
            if (!imgdir.endsWith("/")) imgdir += "/";
            java.io.File imgPubDir = new java.io.File(imgdir);
            imgPubDir.mkdirs();
            if (!imgPubDir.isDirectory()) throw new NpsException(imgdir, ErrorHelper.SYS_NOTFILE);
        }
        Site site = new Site(id, name, artPubDir, unit);
        if (domain != null && domain.length() > 0) site.SetDomain(domain);
        if (suffix != null && suffix.length() > 0) site.SetSuffix(suffix);
        if (rooturl != null && rooturl.length() > 0) site.SetRootURL(rooturl);
        if (imgurl != null && imgurl.length() > 0) site.SetImgURL(imgurl);
        if (imgdir != null && imgdir.length() > 0) site.SetImgDir(imgdir);
        site.ResolveURLs();
        site.topic_tree = new TopicTree(site, name);
        return site;
    }

    private void UpdateSiteState(NpsContext ctxt, int state) throws NpsException {
        PreparedStatement pstmt = null;
        try {
            String sql = "update site set state=? where id=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setInt(1, state);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
            this.state = state;
        } catch (Exception e) {
            nps.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    public void Save(NpsContext ctxt, boolean bNew) throws NpsException {
        if (bNew) {
            SaveSiteInfo(ctxt.GetConnection());
        } else {
            UpdateSiteInfo(ctxt.GetConnection());
        }
        SaveSiteOwner(ctxt.GetConnection());
        SaveSiteHost(ctxt.GetConnection());
        SaveSiteVar(ctxt.GetConnection());
    }

    private void SaveSiteInfo(Connection conn) throws NpsException {
        PreparedStatement pstmt = null;
        try {
            String sql = "insert into site(id,name,domain,unit,suffix,rooturl,artpubdir,img_rooturl,img_publish_dir,fulltext,solr_core) values(?,?,?,?,?,?,?,?,?,?,?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, domain);
            if (unit == null) {
                pstmt.setNull(4, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(4, unit.GetId());
            }
            pstmt.setString(5, GetSuffix());
            pstmt.setString(6, GetRootURL());
            pstmt.setString(7, GetArticleDir().getAbsolutePath());
            pstmt.setString(8, GetImgURL());
            pstmt.setString(9, GetImgDir().getAbsolutePath());
            pstmt.setInt(10, fulltext_index ? 1 : 0);
            pstmt.setString(11, solr_core);
            pstmt.executeUpdate();
        } catch (Exception e) {
            nps.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    private void UpdateSiteInfo(Connection conn) throws NpsException {
        PreparedStatement pstmt = null;
        try {
            String sql = "update site set name=?,domain=?,unit=?,suffix=?,rooturl=?,artpubdir=?,img_rooturl=?,img_publish_dir=?,fulltext=?,solr_core=? where id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, domain);
            if (unit == null) {
                pstmt.setNull(3, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(3, unit.GetId());
            }
            pstmt.setString(4, GetSuffix());
            pstmt.setString(5, GetRootURL());
            pstmt.setString(6, GetArticleDir().getAbsolutePath());
            pstmt.setString(7, GetImgURL());
            pstmt.setString(8, GetImgDir().getAbsolutePath());
            pstmt.setInt(9, fulltext_index ? 1 : 0);
            pstmt.setString(10, solr_core);
            pstmt.setString(11, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            nps.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    private void SaveSiteOwner(Connection conn) throws NpsException {
        PreparedStatement pstmt = null;
        try {
            String sql = "delete from site_owner where siteid=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
            Hashtable owners = GetOwner();
            if (owners != null && !owners.isEmpty()) {
                sql = "insert into site_owner(siteid,userid) values(?,?)";
                pstmt = conn.prepareStatement(sql);
                Enumeration owners_elements = owners.elements();
                while (owners_elements.hasMoreElements()) {
                    Site.Owner owner = (Site.Owner) owners_elements.nextElement();
                    pstmt.setString(1, id);
                    pstmt.setString(2, owner.GetID());
                    pstmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            nps.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    private void SaveSiteHost(Connection conn) throws NpsException {
        PreparedStatement pstmt = null;
        try {
            String sql = "delete from site_host where siteid=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "insert into site_host(siteid,type,host,remotedir,port,uname,upasswd) values(?,?,?,?,?,?,?)";
            pstmt = conn.prepareStatement(sql);
            if (article_ftp_hosts != null && !article_ftp_hosts.isEmpty()) {
                for (Object obj : article_ftp_hosts) {
                    FtpHost article_host = (FtpHost) obj;
                    pstmt.setString(1, id);
                    pstmt.setInt(2, 0);
                    pstmt.setString(3, article_host.GetHostname());
                    pstmt.setString(4, article_host.GetRemotedir());
                    pstmt.setInt(5, article_host.GetRemoteport());
                    pstmt.setString(6, article_host.GetUsername());
                    pstmt.setString(7, article_host.GetUserpassword());
                    pstmt.executeUpdate();
                }
            }
            if (img_ftp_hosts != null && !img_ftp_hosts.isEmpty()) {
                for (Object obj : img_ftp_hosts) {
                    FtpHost img_host = (FtpHost) obj;
                    pstmt.setString(1, id);
                    pstmt.setInt(2, 1);
                    pstmt.setString(3, img_host.GetHostname());
                    pstmt.setString(4, img_host.GetRemotedir());
                    pstmt.setInt(5, img_host.GetRemoteport());
                    pstmt.setString(6, img_host.GetUsername());
                    pstmt.setString(7, img_host.GetUserpassword());
                    pstmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            nps.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    private void SaveSiteVar(Connection conn) throws NpsException {
        PreparedStatement pstmt = null;
        try {
            String sql = "delete from site_vars where siteid=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
            if (vars != null && !vars.isEmpty()) {
                sql = "insert into site_vars(siteid,varname,value) values(?,?,?)";
                pstmt = conn.prepareStatement(sql);
                Enumeration vars_elements = vars.elements();
                while (vars_elements.hasMoreElements()) {
                    Site.Var var = (Site.Var) vars_elements.nextElement();
                    pstmt.setString(1, id);
                    pstmt.setString(2, var.name);
                    pstmt.setString(3, var.value);
                    pstmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            nps.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    public void Delete(NpsContext ctxt) throws NpsException {
        PreparedStatement pstmt = null;
        TriggerManager manager = TriggerManager.LoadTriggers(ctxt);
        manager.DeleteTriggersInSite(ctxt, this);
        try {
            String sql = "delete from attach c where c.artid in (select a.id from article a where a.siteid=?)";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "Delete from article a Where a.siteid=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "delete from topic_pts where topid in (select id from topic b where b.siteid=?)";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "delete from template where siteid=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "delete from topic_owner where topid in (select id from topic where siteid=?)";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "delete from topic_vars where topid in (select id from topic where siteid=?)";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "Delete from topic Where siteid=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "Delete from site_owner where siteid=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "Delete from site_host where siteid=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "Delete from site_vars where siteid=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            sql = "Delete from site where id=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            try {
                pstmt.close();
            } catch (Exception e) {
            }
            nps.processor.JobScheduler.DeleteJobs(ctxt, id);
        } catch (Exception e) {
            nps.util.DefaultLog.error(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    public static Site GetSite(NpsContext ctxt, ZipFile file, User importer) throws Exception {
        Site asite = null;
        try {
            Unit aunit = Unit.GetUnit(ctxt.GetConnection(), file);
            if (aunit == null) throw new NpsException(ErrorHelper.ZIP_NOUNIT);
            asite = GetSite(file, aunit);
            if (asite == null) throw new NpsException(ErrorHelper.ZIP_NOSITE);
            if (ctxt.GetSite(asite.id) != null) asite.Save(ctxt, false); else asite.Save(ctxt, true);
            Hashtable templates_indexby_oldid = asite.GetTemplates(ctxt, file, asite, importer);
            asite.topic_tree = TopicTree.LoadTree(ctxt, asite, templates_indexby_oldid, file);
        } catch (Exception e1) {
            asite = null;
            nps.util.DefaultLog.error(e1);
        }
        try {
            if (asite != null && asite.GetTopicTree() != null) asite.GetTopicTree().CreateDsTable(ctxt);
        } catch (Exception e) {
            nps.util.DefaultLog.error(e);
        }
        return asite;
    }

    private static Site GetSite(ZipFile file, Unit aunit) throws Exception {
        Site asite = null;
        java.util.Enumeration files = file.entries();
        while (files.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) files.nextElement();
            if (!entry.isDirectory()) {
                if (entry.getName().endsWith(".site")) {
                    InputStream in = file.getInputStream(entry);
                    java.io.InputStreamReader r = new InputStreamReader(in, "UTF-8");
                    java.io.BufferedReader br = new BufferedReader(r);
                    String site_id = null;
                    String s = br.readLine();
                    if (s != null) site_id = s.trim();
                    String site_name = null;
                    s = br.readLine();
                    if (s != null) site_name = s.trim();
                    String site_rooturl = null;
                    s = br.readLine();
                    if (s != null) site_rooturl = s.trim();
                    String site_art_publish_dir = null;
                    s = br.readLine();
                    if (s != null) site_art_publish_dir = s.trim();
                    String site_art_suffix = null;
                    s = br.readLine();
                    if (s != null) site_art_suffix = s.trim();
                    String site_art_encoding = "UTF-8";
                    String site_img_publish_dir = null;
                    s = br.readLine();
                    if (s != null) site_img_publish_dir = s.trim();
                    String site_img_rooturl = null;
                    s = br.readLine();
                    if (s != null) site_img_rooturl = s.trim();
                    s = br.readLine();
                    s = br.readLine();
                    String site_domain = null;
                    s = br.readLine();
                    if (s != null) site_domain = s.trim();
                    String fulltext_index = null;
                    s = br.readLine();
                    if (s != null) fulltext_index = s.trim();
                    String solr_core = null;
                    s = br.readLine();
                    if (s != null) solr_core = s.trim();
                    asite = GetSite(site_id, site_name, site_domain, site_art_publish_dir, aunit, site_img_publish_dir, site_art_encoding, site_art_suffix, site_rooturl, site_img_rooturl);
                    if ("0".equals(fulltext_index)) {
                        asite.fulltext_index = false;
                    }
                    asite.solr_core = solr_core;
                    try {
                        br.close();
                    } catch (Exception e1) {
                    }
                    break;
                }
            }
        }
        if (asite != null) {
            asite.GetSiteArticleFtps(file);
            asite.GetSiteImageFtps(file);
            asite.GetSiteVars(file);
        }
        return asite;
    }

    private void GetSiteArticleFtps(ZipFile file) throws Exception {
        java.util.Enumeration files = file.entries();
        while (files.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) files.nextElement();
            if (!entry.isDirectory()) {
                if (entry.getName().endsWith(".aftp")) {
                    InputStream in = file.getInputStream(entry);
                    java.io.InputStreamReader r = new InputStreamReader(in, "UTF-8");
                    java.io.BufferedReader br = new BufferedReader(r);
                    String host_name = null;
                    String s = br.readLine();
                    if (s != null) host_name = s.trim();
                    String host_remotedir = null;
                    s = br.readLine();
                    if (s != null) host_remotedir = s.trim();
                    String host_remoteport = null;
                    s = br.readLine();
                    if (s != null) host_remoteport = s.trim();
                    int host_port = 21;
                    try {
                        host_port = (int) Float.parseFloat(host_remoteport);
                    } catch (Exception e1) {
                    }
                    String host_username = null;
                    s = br.readLine();
                    if (s != null) host_username = s.trim();
                    String host_userpassword = null;
                    s = br.readLine();
                    if (s != null) host_userpassword = s.trim();
                    AddFtp4Article(host_name, host_remotedir, host_port, host_username, host_userpassword);
                    try {
                        br.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }
    }

    private void GetSiteImageFtps(ZipFile file) throws Exception {
        java.util.Enumeration files = file.entries();
        while (files.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) files.nextElement();
            if (!entry.isDirectory()) {
                if (entry.getName().endsWith(".iftp")) {
                    InputStream in = file.getInputStream(entry);
                    java.io.InputStreamReader r = new InputStreamReader(in, "UTF-8");
                    java.io.BufferedReader br = new BufferedReader(r);
                    String host_name = null;
                    String s = br.readLine();
                    if (s != null) host_name = s.trim();
                    String host_remotedir = null;
                    s = br.readLine();
                    if (s != null) host_remotedir = s.trim();
                    String host_remoteport = null;
                    s = br.readLine();
                    if (s != null) host_remoteport = s.trim();
                    int host_port = 21;
                    try {
                        host_port = (int) Float.parseFloat(host_remoteport);
                    } catch (Exception e1) {
                    }
                    String host_username = null;
                    s = br.readLine();
                    if (s != null) host_username = s.trim();
                    String host_userpassword = null;
                    s = br.readLine();
                    if (s != null) host_userpassword = s.trim();
                    AddFtp4Image(host_name, host_remotedir, host_port, host_username, host_userpassword);
                    try {
                        br.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }
    }

    private void GetSiteVars(ZipFile file) throws Exception {
        java.util.Enumeration files = file.entries();
        while (files.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) files.nextElement();
            if (!entry.isDirectory()) {
                if (entry.getName().endsWith(".svar")) {
                    InputStream in = file.getInputStream(entry);
                    java.io.InputStreamReader r = new InputStreamReader(in, "UTF-8");
                    java.io.BufferedReader br = new BufferedReader(r);
                    String var_name = null;
                    String s = br.readLine();
                    if (s != null) var_name = s.trim();
                    if (var_name == null || var_name.length() == 0) continue;
                    String var_value = br.readLine();
                    AddVar(var_name, var_value);
                    try {
                        br.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }
    }

    private Hashtable GetTemplates(NpsContext ctxt, ZipFile file, Site asite, User importer) throws Exception {
        Hashtable templates_indexby_oldid = new Hashtable();
        java.util.Enumeration files = file.entries();
        while (files.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) files.nextElement();
            if (!entry.isDirectory()) {
                String entry_name = entry.getName();
                if (entry_name.endsWith(".template")) {
                    InputStream in = file.getInputStream(entry);
                    java.io.InputStreamReader r = new InputStreamReader(in, "UTF-8");
                    java.io.BufferedReader br = new BufferedReader(r);
                    TemplateBase aTemplate = null;
                    if (entry_name.startsWith("ARTICLE")) {
                        String template_oldid = null;
                        String s = br.readLine();
                        if (s != null) template_oldid = s.trim();
                        String template_name = null;
                        s = br.readLine();
                        if (s != null) template_name = s.trim();
                        String template_scope = null;
                        s = br.readLine();
                        if (s != null) template_scope = s.trim();
                        int template_iscope = 1;
                        try {
                            template_iscope = (int) Float.parseFloat(template_scope);
                        } catch (Exception e1) {
                        }
                        String template_siteid = null;
                        s = br.readLine();
                        if (s != null) template_siteid = s.trim();
                        if (template_siteid != null && template_siteid.length() > 0) template_siteid = asite.GetId();
                        aTemplate = ArticleTemplate.GetTemplate(ctxt, template_name, importer.GetUID(), template_iscope, template_siteid);
                        templates_indexby_oldid.put(template_oldid, aTemplate);
                    } else if (entry_name.startsWith("PAGE")) {
                        String template_oldid = null;
                        String s = br.readLine();
                        if (s != null) template_oldid = s.trim();
                        String template_name = null;
                        s = br.readLine();
                        if (s != null) template_name = s.trim();
                        String template_fname = null;
                        s = br.readLine();
                        if (s != null) template_fname = s.trim();
                        String template_scope = null;
                        s = br.readLine();
                        if (s != null) template_scope = s.trim();
                        int template_iscope = 1;
                        try {
                            template_iscope = (int) Float.parseFloat(template_scope);
                        } catch (Exception e1) {
                        }
                        String template_siteid = null;
                        s = br.readLine();
                        if (s != null) template_siteid = s.trim();
                        if (template_siteid != null && template_siteid.length() > 0) template_siteid = asite.GetId();
                        aTemplate = PageTemplate.GetTemplate(ctxt, template_name, template_fname, importer.GetUID(), template_iscope, template_siteid);
                        templates_indexby_oldid.put(template_oldid, aTemplate);
                    } else {
                        try {
                            br.close();
                        } catch (Exception e1) {
                        }
                        continue;
                    }
                    try {
                        br.close();
                    } catch (Exception e1) {
                    }
                    if (aTemplate == null) continue;
                    aTemplate.Save(ctxt, true);
                    int pos = entry_name.lastIndexOf(".");
                    if (pos != -1) entry_name = entry_name.substring(0, pos);
                    String entryname_templatedata = entry_name + ".data";
                    ZipEntry entry_templatedata = file.getEntry(entryname_templatedata);
                    if (entry_templatedata != null) {
                        InputStream in_templatedata = file.getInputStream(entry_templatedata);
                        aTemplate.UpdateTemplate(ctxt, in_templatedata);
                        try {
                            in_templatedata.close();
                        } catch (Exception e1) {
                        }
                    }
                }
            }
        }
        return templates_indexby_oldid;
    }
}
