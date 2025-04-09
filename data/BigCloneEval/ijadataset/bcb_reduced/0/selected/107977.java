package nps.core;

import nps.exception.NpsException;
import nps.exception.ErrorHelper;
import nps.event.*;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.List;
import java.io.InputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * �����ݿⲻͬ����,ʵ���������
 *   
 * a new publishing system
 * Copyright (c) 2007
 *
 * @author jialin
 * @version 1.0
 */
public class CustomArticleHelper {

    private Hashtable classes = null;

    private static CustomArticleHelper helper = null;

    public static CustomArticleHelper GetHelper() {
        if (helper == null) helper = new CustomArticleHelper();
        return helper;
    }

    public synchronized void Init(InputStream in) throws Exception {
        if (classes != null) classes.clear();
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(in);
        List lsConfig = doc.getRootElement().getChildren("CustomArticle");
        for (Object obj : lsConfig) {
            Element eleConfig = (Element) obj;
            try {
                String table = eleConfig.getChildText("table");
                if (table != null) table = table.trim();
                String class_name = eleConfig.getChildText("class");
                if (class_name != null) class_name = class_name.trim();
                if (table == null || table.length() == 0 || class_name == null || class_name.length() == 0) continue;
                Register(table, class_name);
                Element eleProperty = eleConfig.getChild("property");
                InitProperty(eleProperty);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List lsEvents = doc.getRootElement().getChildren("event");
        for (Object obj : lsEvents) {
            Element eleEvent = (Element) obj;
            InitEventListener(eleEvent);
        }
    }

    private void InitEventListener(org.jdom.Element eleEvent) {
        if (eleEvent == null) return;
        List events = eleEvent.getChildren();
        for (Object obj : events) {
            Element event = (Element) obj;
            String event_name = event.getName();
            String event_key = event.getAttributeValue("key");
            String event_classname = event.getTextTrim();
            if (event_name == null || event_classname == null) continue;
            if (!(event_name.equalsIgnoreCase("insert") || "update".equalsIgnoreCase(event_name) || "delete".equalsIgnoreCase(event_name) || "ready".equalsIgnoreCase(event_name) || "publish".equalsIgnoreCase(event_name))) continue;
            Object clazz_obj;
            try {
                Class clazz = Config.GetClassLoader().ReloadClass(event_classname);
                java.lang.reflect.Constructor clazz_constructor = clazz.getConstructor(new Class[] {});
                clazz_obj = clazz_constructor.newInstance(new Object[] {});
            } catch (Exception e) {
                nps.util.DefaultLog.error_noexception(e);
                continue;
            }
            if ("insert".equalsIgnoreCase(event_name)) {
                if (clazz_obj instanceof InsertEventListener) {
                    EventSubscriber.GetSubscriber().AddListener((InsertEventListener) clazz_obj, event_key);
                }
            } else if ("update".equalsIgnoreCase(event_name)) {
                if (clazz_obj instanceof UpdateEventListener) {
                    EventSubscriber.GetSubscriber().AddListener((UpdateEventListener) clazz_obj, event_key);
                }
            } else if ("delete".equalsIgnoreCase(event_name)) {
                if (clazz_obj instanceof DeleteEventListener) {
                    EventSubscriber.GetSubscriber().AddListener((DeleteEventListener) clazz_obj, event_key);
                }
            } else if ("ready".equalsIgnoreCase(event_name)) {
                if (clazz_obj instanceof Ready2PublishEventListener) {
                    EventSubscriber.GetSubscriber().AddListener((Ready2PublishEventListener) clazz_obj, event_key);
                }
            } else if ("publish".equalsIgnoreCase(event_name)) {
                if (clazz_obj instanceof PublishEventListener) {
                    EventSubscriber.GetSubscriber().AddListener((PublishEventListener) clazz_obj, event_key);
                }
            } else if ("cancel".equalsIgnoreCase(event_name)) {
                if (clazz_obj instanceof CancelEventListener) {
                    EventSubscriber.GetSubscriber().AddListener((CancelEventListener) clazz_obj, event_key);
                }
            }
        }
    }

    private void InitProperty(org.jdom.Element eleProperty) {
        if (eleProperty == null) return;
        List lsProperties = eleProperty.getChildren();
        for (Object obj_prop : lsProperties) {
            Element prop = (Element) obj_prop;
            String prop_name = prop.getName();
            String prop_value = prop.getTextTrim();
            Config.PutProperty(prop_name, prop_value);
        }
    }

    public void Register(String table_name, String class_name) {
        table_name = table_name.toUpperCase();
        Class aClass = null;
        try {
            aClass = Class.forName(class_name);
            boolean is_subclass_of_custom_article = false;
            Class super_class = aClass;
            do {
                super_class = super_class.getSuperclass();
                if (super_class.getName().equals("nps.core.CustomArticle")) {
                    is_subclass_of_custom_article = true;
                    break;
                }
            } while (super_class != null);
            if (!is_subclass_of_custom_article) throw new NpsException(class_name, ErrorHelper.SYS_NOT_SUBCLASS_OF_CUSTOMARTICLE);
        } catch (Exception e) {
            nps.util.DefaultLog.error_noexception(e);
            return;
        }
        if (classes == null) classes = new Hashtable();
        if (classes.containsKey(table_name)) classes.remove(table_name);
        classes.put(table_name, aClass);
    }

    public void Unregister(String table_name) {
        if (classes == null) return;
        if (classes.containsKey(table_name.toUpperCase())) {
            classes.remove(table_name);
        }
    }

    public CustomArticle NewInstance(NpsContext ctxt, Topic top, ResultSet rs) throws Exception {
        if (top == null) throw new NpsException(ErrorHelper.SYS_NOTOPIC);
        if (top.GetTable() == null || top.GetTable().length() == 0) throw new NpsException(ErrorHelper.SYS_NEED_CUSTOM_TOPIC);
        String table_name = top.GetTable().toUpperCase();
        if (classes == null || classes.isEmpty() || !classes.containsKey(table_name)) {
            return new CustomArticle(ctxt, top, rs);
        }
        Class clazz = GetArticleClass(table_name);
        java.lang.reflect.Constructor aconstructor = clazz.getConstructor(new Class[] { NpsContext.class, Topic.class, ResultSet.class });
        return (CustomArticle) aconstructor.newInstance(new Object[] { ctxt, top, rs });
    }

    public CustomArticle NewInstance(NpsContext ctxt, String id, String title, Topic top) throws Exception {
        if (top == null) throw new NpsException(ErrorHelper.SYS_NOTOPIC);
        if (top.GetTable() == null || top.GetTable().length() == 0) throw new NpsException(ErrorHelper.SYS_NEED_CUSTOM_TOPIC);
        String table_name = top.GetTable().toUpperCase();
        if (classes == null || classes.isEmpty() || !classes.containsKey(table_name)) {
            return new CustomArticle(ctxt, id, title, top);
        }
        Class clazz = GetArticleClass(table_name);
        java.lang.reflect.Constructor aconstructor = clazz.getConstructor(new Class[] { NpsContext.class, String.class, String.class, Topic.class });
        return (CustomArticle) aconstructor.newInstance(new Object[] { ctxt, id, title, top });
    }

    private Class GetArticleClass(String table_name) throws Exception {
        return (Class) classes.get(table_name);
    }
}
