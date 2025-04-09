package net.sf.lnn;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import lotus.domino.AgentBase;
import lotus.domino.AgentContext;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.Session;
import net.sf.lnn.jabber.JabberDocumentNotifier;
import net.sf.lnn.util.BooleanPropertyChecker;

public class LotusNotifierAgent extends AgentBase {

    private static final String CFG_FILE_NAME = "lnn.properties";

    public static final String CFG_NOTIFIER_CLASS = "lnn.notifier.class";

    public static final String CFG_DEBUG = "lnn.debug";

    private Properties cfg;

    private ArrayList notifiers;

    public LotusNotifierAgent() {
        super();
        cfg = getDefaultConfig();
        loadConfig();
        notifiers = new ArrayList();
        addConfigNotifiers();
    }

    private static Properties getDefaultConfig() {
        Properties cfg = new Properties();
        cfg.setProperty(CFG_NOTIFIER_CLASS + ".1", JabberDocumentNotifier.class.toString());
        return cfg;
    }

    private void loadConfig() {
        try {
            cfg.load(this.getClass().getClassLoader().getResourceAsStream(CFG_FILE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isDebugEnabled()) cfg.list(System.out);
    }

    public void addNotifier(DocumentNotifier notifier) {
        notifiers.add(notifier);
    }

    private void addConfigNotifiers() {
        for (int i = 1; ; i++) {
            String notifierClassName = cfg.getProperty(CFG_NOTIFIER_CLASS + "." + i);
            if (notifierClassName == null) break;
            try {
                if (isDebugEnabled()) System.out.println("adding notifier " + notifierClassName);
                DocumentNotifier notifier = createNotifier(notifierClassName);
                addNotifier(notifier);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected DocumentNotifier createNotifier(String className) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class notifierClass = Class.forName(className);
        Constructor constructor = notifierClass.getConstructor(new Class[] { Properties.class });
        return (DocumentNotifier) constructor.newInstance(new Object[] { cfg });
    }

    public void NotesMain() {
        try {
            Session session = getSession();
            AgentContext agentContext = session.getAgentContext();
            DocumentCollection unprocessed = agentContext.getUnprocessedDocuments();
            for (Document doc = unprocessed.getFirstDocument(); doc != null; doc = unprocessed.getNextDocument()) {
                process(doc);
                agentContext.updateProcessedDoc(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process(Document doc) {
        for (Iterator itr = notifiers.iterator(); itr.hasNext(); ) {
            DocumentNotifier notifier = (DocumentNotifier) itr.next();
            try {
                notifier.notify(doc);
            } catch (DocumentNotifierException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isDebugEnabled() {
        return BooleanPropertyChecker.getProperty(cfg, CFG_DEBUG);
    }
}
