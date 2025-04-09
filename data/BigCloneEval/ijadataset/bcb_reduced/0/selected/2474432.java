package nps.core;

import nps.event.*;
import nps.js.*;
import java.util.Date;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.StringReader;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import com.microfly.core.db.Database;
import koala.dynamicjava.interpreter.Interpreter;
import koala.dynamicjava.interpreter.TreeInterpreter;
import koala.dynamicjava.parser.wrapper.JavaCCParserFactory;
import oracle.sql.CLOB;
import oracle.jdbc.driver.OracleResultSet;

/**
 *  ��������ʵ�ֶ�ĳ����Ŀ�ض��¼����Զ�����Ӧ
 *  a new publishing system
 *  Copyright (c) 2007
 *
 * @author jialin
 * @version 1.0
 */
public class Trigger implements IPortable, InsertEventListener, UpdateEventListener, DeleteEventListener, Ready2PublishEventListener, PublishEventListener, CancelEventListener {

    public static final int JAVASCRIPT = 0;

    public static final int DYNAMICJAVA = 1;

    public static final int EVENT_INSERT = 0;

    public static final int EVENT_UPDATE = 1;

    public static final int EVENT_READY = 2;

    public static final int EVENT_PUBLISH = 3;

    public static final int EVENT_CANCEL = 4;

    public static final int EVENT_DELETE = 5;

    private String id;

    private String name;

    private Topic topic;

    private boolean enabled;

    private boolean running = false;

    private int event;

    private int lang;

    private String code;

    private String creator;

    private String creator_name;

    private Date create_date;

    private int lastrunstate;

    private String servername;

    private Date lastrundate;

    private long lastruntime;

    public Trigger(NpsContext ctxt, ResultSet rs) throws Exception {
        this.id = rs.getString("id");
        this.name = rs.getString("name");
        this.event = rs.getInt("event");
        Site site = ctxt.GetSite(rs.getString("siteid"));
        if (site != null) {
            topic = site.GetTopicTree().GetTopic(rs.getString("topid"));
        }
        this.lang = rs.getInt("lang");
        this.code = GetClob(((OracleResultSet) rs).getCLOB("code"));
        this.creator = rs.getString("creator");
        this.creator_name = rs.getString("creator_name");
        this.create_date = rs.getTimestamp("createdate");
        this.enabled = rs.getInt("enabled") == 1;
        if (topic == null) this.enabled = false;
    }

    public Trigger(String name, int event, Topic topic, boolean enabled, User user) {
        this.name = name;
        this.event = event;
        this.topic = topic;
        this.enabled = enabled;
        this.creator = user.GetUID();
        this.creator_name = user.GetName();
        this.create_date = new java.util.Date();
    }

    public Trigger(Trigger obj) {
        this.id = null;
        this.name = obj.name;
        this.event = obj.event;
        this.topic = obj.topic;
        this.enabled = obj.enabled;
        this.lang = obj.lang;
        this.code = obj.code;
        this.create_date = new java.util.Date();
    }

    private String GetClob(CLOB clob) throws Exception {
        if (clob == null) return null;
        Reader is = null;
        StringWriter so = null;
        try {
            is = clob.getCharacterStream();
            so = new StringWriter();
            int b;
            while ((b = is.read()) != -1) {
                so.write(b);
            }
            return so.toString();
        } finally {
            if (so != null) try {
                so.close();
            } catch (Exception e) {
            }
            if (is != null) try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    public String GetId() {
        return id;
    }

    public String GetName() {
        return name;
    }

    public void SetName(String s) {
        name = s;
    }

    public int GetEvent() {
        return event;
    }

    public void SetEvent(int e) {
        event = e;
    }

    public void SetTopic(Topic t) {
        topic = t;
    }

    public Topic GetTopic() {
        return topic;
    }

    public String GetCreator() {
        return creator;
    }

    public String GetCreatorName() {
        return creator_name;
    }

    public boolean IsEnable() {
        return enabled;
    }

    public void Enable() {
        if (!enabled) {
            this.enabled = true;
            Listen();
        }
    }

    public void Disable() {
        if (enabled) {
            this.enabled = false;
            UnListen();
        }
    }

    public int GetLang() {
        return lang;
    }

    public String GetCode() {
        return code;
    }

    public void SetCode(int lang, String code) {
        this.lang = lang;
        this.code = code;
    }

    public Date GetCreateDate() {
        return create_date;
    }

    public void SetCreateDate(Date d) {
        this.create_date = d;
    }

    public String GetLastServer() {
        return servername;
    }

    public int GetLastRunState() {
        return lastrunstate;
    }

    public Date GetLastRunDate() {
        return lastrundate;
    }

    public long GetLastRunTime() {
        return lastruntime;
    }

    public void Save(NpsContext ctxt) throws Exception {
        try {
            if (id == null) {
                id = GenerateId(ctxt);
                Insert(ctxt);
            } else {
                Update(ctxt);
            }
            UpdateCode(ctxt);
        } catch (Exception e) {
            ctxt.Rollback();
            nps.util.DefaultLog.error(e);
        }
    }

    private void Insert(NpsContext ctxt) throws Exception {
        PreparedStatement pstmt = null;
        String sql = null;
        try {
            sql = "insert into EVENT_TRIGGER(id,name,siteid,topid,enabled,event,lang,code,creator,createdate) values(?,?,?,?,?,?,?,empty_clob(),?,?)";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, topic.GetSiteId());
            pstmt.setString(4, topic.GetId());
            pstmt.setInt(5, enabled ? 1 : 0);
            pstmt.setInt(6, event);
            pstmt.setInt(7, lang);
            pstmt.setString(8, creator);
            pstmt.setTimestamp(9, new java.sql.Timestamp(create_date.getTime()));
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    private void Update(NpsContext ctxt) throws Exception {
        PreparedStatement pstmt = null;
        String sql = null;
        try {
            sql = "update EVENT_TRIGGER set name=?,siteid=?,topid=?,enabled=?,event=?,lang=? where id=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, topic.GetSiteId());
            pstmt.setString(3, topic.GetId());
            pstmt.setInt(4, enabled ? 1 : 0);
            pstmt.setInt(5, event);
            pstmt.setInt(6, lang);
            pstmt.setString(7, id);
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    private void UpdateCode(NpsContext ctxt) throws Exception {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "update EVENT_TRIGGER set code=empty_clob() where id=?";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e1) {
            }
            sql = "select code from EVENT_TRIGGER where id=? for update";
            pstmt = ctxt.GetConnection().prepareStatement(sql);
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                oracle.sql.CLOB clob = (oracle.sql.CLOB) rs.getClob(1);
                java.io.Writer writer = clob.getCharacterOutputStream();
                writer.write(code);
                writer.flush();
                try {
                    writer.close();
                } catch (Exception e1) {
                }
            }
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
    }

    private String GenerateId(NpsContext ctxt) throws Exception {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = ctxt.GetConnection().prepareStatement("select seq_trigger.nextval from dual");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
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

    private synchronized void SetLastRunInfo(String servername, int state, Date begin, long milliseconds) {
        this.lastrunstate = state;
        this.servername = servername;
        this.lastrundate = begin;
        this.lastruntime = milliseconds;
    }

    public void Zip(NpsContext ctxt, ZipOutputStream out) throws Exception {
        String filename = "TRIGGER" + GetId() + ".trigger";
        out.putNextEntry(new ZipEntry(filename));
        try {
            ZipWriter writer = new ZipWriter(out);
            writer.println(id);
            writer.println(name);
            writer.println(topic.GetSiteId());
            writer.println(topic.GetCode());
            writer.println(enabled ? 1 : 0);
            writer.println(event);
            writer.println(lang);
        } finally {
            out.closeEntry();
        }
        ZipCode(out);
    }

    private void ZipCode(ZipOutputStream out) throws Exception {
        String filename_info = "TRIGGER" + id + ".data";
        out.putNextEntry(new ZipEntry(filename_info));
        try {
            out.write(code.getBytes());
        } finally {
            out.closeEntry();
        }
    }

    public void Listen() {
        if (!enabled) return;
        if (running) return;
        if (topic == null) return;
        String key = topic.GetId();
        switch(event) {
            case EVENT_INSERT:
                EventSubscriber.GetSubscriber().AddListener((InsertEventListener) this, key);
                break;
            case EVENT_UPDATE:
                EventSubscriber.GetSubscriber().AddListener((UpdateEventListener) this, key);
                break;
            case EVENT_DELETE:
                EventSubscriber.GetSubscriber().AddListener((DeleteEventListener) this, key);
                break;
            case EVENT_READY:
                EventSubscriber.GetSubscriber().AddListener((Ready2PublishEventListener) this, key);
                break;
            case EVENT_PUBLISH:
                EventSubscriber.GetSubscriber().AddListener((PublishEventListener) this, key);
                break;
            case EVENT_CANCEL:
                EventSubscriber.GetSubscriber().AddListener((CancelEventListener) this, key);
                break;
        }
        running = true;
    }

    public void UnListen() {
        if (!running) return;
        switch(event) {
            case EVENT_INSERT:
                EventSubscriber.GetSubscriber().RemoveListener(InsertEventListener.class, this);
                break;
            case EVENT_UPDATE:
                EventSubscriber.GetSubscriber().RemoveListener(UpdateEventListener.class, this);
                break;
            case EVENT_DELETE:
                EventSubscriber.GetSubscriber().RemoveListener(DeleteEventListener.class, this);
                break;
            case EVENT_READY:
                EventSubscriber.GetSubscriber().RemoveListener(Ready2PublishEventListener.class, this);
                break;
            case EVENT_PUBLISH:
                EventSubscriber.GetSubscriber().RemoveListener(PublishEventListener.class, this);
                break;
            case EVENT_CANCEL:
                EventSubscriber.GetSubscriber().RemoveListener(CancelEventListener.class, this);
                break;
        }
        running = false;
    }

    public void DataInserted(InsertEvent e) {
        if (!enabled || event != EVENT_INSERT) return;
        EventHandler(e.getSource());
    }

    public void DataUpdated(UpdateEvent e) {
        if (!enabled || event != EVENT_UPDATE) return;
        EventHandler(e.getSource());
    }

    public void DataDeleted(DeleteEvent e) {
        if (!enabled || event != EVENT_DELETE) return;
        EventHandler(e.getSource());
    }

    public void DataReady(Ready2PublishEvent e) {
        if (!enabled || event != EVENT_READY) return;
        EventHandler(e.getSource());
    }

    public void DataPublished(PublishEvent e) {
        if (!enabled || event != EVENT_PUBLISH) return;
        EventHandler(e.getSource());
    }

    public void DataCancelled(CancelEvent e) {
        if (!enabled || event != EVENT_CANCEL) return;
        EventHandler(e.getSource());
    }

    private void EventHandler(Object source) {
        NpsContext ctxt = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = Database.GetDatabase("nps").GetConnection();
            pstmt = conn.prepareStatement("select account,password from users where id=?");
            pstmt.setString(1, creator);
            rs = pstmt.executeQuery();
            if (!rs.next()) return;
            User runas = User.Login(rs.getString("account"), rs.getString("password"));
            if (rs != null) try {
                rs.close();
            } catch (Exception e) {
            }
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
            rs = null;
            pstmt = null;
            ctxt = new NpsContext(conn, runas);
            ctxt.GetSite(topic.GetSiteId());
            switch(lang) {
                case JAVASCRIPT:
                    RunJavaScript(ctxt, source);
                    break;
                case DYNAMICJAVA:
                    RunDynamicJava(ctxt, source);
                    break;
            }
        } catch (Exception e) {
            nps.util.DefaultLog.error_noexception(e);
        } finally {
            if (rs != null) try {
                rs.close();
            } catch (Exception e) {
            }
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
            if (ctxt != null) ctxt.Clear();
        }
    }

    private void RunJavaScript(NpsContext ctxt, Object source) throws Exception {
        Date begin = new java.util.Date();
        long l_begin = System.currentTimeMillis();
        Context context = Context.enter();
        nps.js.NpsSession session = null;
        try {
            Scriptable scope = context.initStandardObjects();
            ScriptableObject.defineClass(scope, NpsUser.class);
            ScriptableObject.defineClass(scope, NpsSite.class);
            ScriptableObject.defineClass(scope, NpsTopic.class);
            ScriptableObject.defineClass(scope, NpsArticle.class);
            ScriptableObject.defineClass(scope, NpsAttach.class);
            ScriptableObject.defineClass(scope, NpsArticleCollection.class);
            ScriptableObject.defineClass(scope, NpsPreparedStatement.class);
            ScriptableObject.defineClass(scope, NpsResultSet.class);
            ScriptableObject.defineClass(scope, NpsLog.class);
            ScriptableObject.defineClass(scope, NpsFile.class);
            ScriptableObject.defineClass(scope, NpsEmail.class);
            ScriptableObject.defineClass(scope, NpsSitemap.class);
            ScriptableObject.defineClass(scope, NpsRss.class);
            ScriptableObject.defineClass(scope, NpsFtp.class);
            ScriptableObject.defineClass(scope, NpsAwstats.class);
            ScriptableObject.defineClass(scope, NpsCurrencyConverter.class);
            ScriptableObject.defineClass(scope, NpsFormula.class);
            ScriptableObject.defineClass(scope, NpsHtmlFetcher.class);
            ScriptableObject.defineClass(scope, NpsHtmlPoster.class);
            ScriptableObject.defineClass(scope, NpsHtmlParser.class);
            ScriptableObject.defineClass(scope, NpsTemplate.class);
            ScriptableObject.defineClass(scope, NpsMeter.class);
            ScriptableObject.defineClass(scope, NpsSolr.class);
            ScriptableObject.defineClass(scope, NpsChineseConverter.class);
            session = new nps.js.NpsSession(scope, ctxt, ctxt.GetUser());
            Object wrappedSession = Context.javaToJS(session, scope);
            ScriptableObject.putProperty(scope, "session", wrappedSession);
            ScriptableObject.putProperty(scope, "user", session.GetCurrentUser());
            ScriptableObject.putProperty(scope, "out", session.GetDefaultLog());
            if (source instanceof Article) {
                ((Article) source).SetContext(ctxt);
                NpsArticle source_js = session.ToNpsArticle((Article) source);
                ScriptableObject.putProperty(scope, "source", source_js);
            } else {
                ScriptableObject.putProperty(scope, "source", Context.javaToJS(source, scope));
            }
            Script compiledScript = context.compileString(code, "trigger" + id, 1, null);
            compiledScript.exec(context, scope);
            ctxt.Commit();
            long l_end = System.currentTimeMillis();
            LastRun(ctxt, session == null ? "" : session.GetServerName(), 0, begin, l_end - l_begin);
        } catch (Exception e) {
            e.printStackTrace();
            ctxt.Rollback();
            long l_end = System.currentTimeMillis();
            LastRun(ctxt, session == null ? "" : session.GetServerName(), 1, begin, l_end - l_begin);
            throw e;
        } finally {
            if (source instanceof Article) ((Article) source).Clear();
            Context.exit();
        }
    }

    private void RunDynamicJava(NpsContext ctxt, Object source) throws Exception {
        Date begin = new java.util.Date();
        long l_begin = System.currentTimeMillis();
        Interpreter interpreter = new TreeInterpreter(new JavaCCParserFactory());
        nps.dj.NpsSession session = null;
        StringReader sr = new StringReader(code);
        try {
            session = new nps.dj.NpsSession(ctxt, ctxt.GetUser());
            if (source instanceof Article) ((Article) source).SetContext(ctxt);
            interpreter.defineVariable("session", session);
            interpreter.defineVariable("user", ctxt.GetUser());
            interpreter.defineVariable("out", session.GetDefaultLog());
            interpreter.defineVariable("source", source);
            interpreter.interpret(sr, "trigger" + id);
            ctxt.Commit();
            long l_end = System.currentTimeMillis();
            LastRun(ctxt, session == null ? "" : session.GetServerName(), 0, begin, l_end - l_begin);
        } catch (Exception e) {
            e.printStackTrace();
            ctxt.Rollback();
            long l_end = System.currentTimeMillis();
            LastRun(ctxt, session == null ? "" : session.GetServerName(), 1, begin, l_end - l_begin);
            throw e;
        } finally {
            sr.close();
            if (source instanceof Article) ((Article) source).Clear();
        }
    }

    private void LastRun(NpsContext ctxt, String server, int state, Date begin, long milliseconds) {
        PreparedStatement pstmt = null;
        try {
            pstmt = ctxt.GetConnection().prepareStatement("update event_trigger set lastrunstate=?,servername=?,lastrundate=?,lastruntime=? where id=?");
            pstmt.setInt(1, state);
            pstmt.setString(2, server);
            pstmt.setTimestamp(3, new java.sql.Timestamp(begin.getTime()));
            pstmt.setLong(4, milliseconds);
            pstmt.setString(5, id);
            pstmt.executeUpdate();
            ctxt.Commit();
        } catch (Exception e) {
            nps.util.DefaultLog.error_noexception(e);
        } finally {
            if (pstmt != null) try {
                pstmt.close();
            } catch (Exception e) {
            }
        }
        SetLastRunInfo(server, state, begin, milliseconds);
    }
}
