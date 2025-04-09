package net.sf.doolin.gui.template.component;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import net.sf.doolin.gui.service.DesktopService;
import net.sf.doolin.template.Template;
import net.sf.doolin.template.TemplateManager;
import net.sf.doolin.template.TemplateUtils;
import net.sf.doolin.util.URLUtils;
import net.sf.doolin.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that displays a template.
 * 
 * @author Damien Coraboeuf
 */
public class JTemplate extends JEditorPane {

    private static final long serialVersionUID = 1L;

    /**
	 * Logger
	 */
    private static final Logger log = LoggerFactory.getLogger(JTemplate.class);

    /**
	 * Registers URL protocol for actions.
	 */
    static {
        URLUtils.registerURLStreamHandler(ActionURLStreamHandler.PROTOCOL_ACTION, new ActionURLStreamHandler());
    }

    /**
	 * Template manager
	 */
    private TemplateManager templateManager;

    /**
	 * Format to display (MIME type)
	 */
    private String format;

    /**
	 * Path to the template
	 */
    private String path;

    /**
	 * Type of template
	 */
    private String type;

    /**
	 * Context
	 */
    private Map<String, Object> context = new HashMap<String, Object>();

    /**
	 * Callback for actions
	 */
    private ActionCallback actionCallback;

    private HyperlinkListener hyperlinkListener;

    /**
	 * Constructor.
	 */
    public JTemplate() {
        setEditable(false);
    }

    /**
	 * Returns the associated action callback.
	 * 
	 * @return Action callback
	 * 
	 * @see #setActionCallback(ActionCallback)
	 */
    public ActionCallback getActionCallback() {
        return this.actionCallback;
    }

    /**
	 * Gets the context.
	 * 
	 * @return Context used to fill the template
	 */
    public Map<String, Object> getContext() {
        return this.context;
    }

    /**
	 * Gets the format.
	 * 
	 * @return Format to display (MIME type)
	 */
    public String getFormat() {
        return this.format;
    }

    /**
	 * Gets the path.
	 * 
	 * @return Path to the template
	 */
    public String getPath() {
        return this.path;
    }

    /**
	 * Gets the template manager.
	 * 
	 * @return the template manager
	 */
    public TemplateManager getTemplateManager() {
        return this.templateManager;
    }

    /**
	 * Gets the type.
	 * 
	 * @return Type of template
	 */
    public String getType() {
        return this.type;
    }

    /**
	 * Sets the associated action callback that will be called for any
	 * <code>action</code> URL in the generated template.
	 * 
	 * @param actionCallback
	 *            Action callback
	 */
    public void setActionCallback(ActionCallback actionCallback) {
        this.actionCallback = actionCallback;
    }

    /**
	 * Sets the context.
	 * 
	 * @param context
	 *            Context used to fill the template
	 */
    public void setContext(Map<String, Object> context) {
        this.context = context;
        setup();
    }

    /**
	 * Sets the format.
	 * 
	 * @param format
	 *            Format to display (MIME type)
	 */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
	 * Sets the path.
	 * 
	 * @param path
	 *            Path to the template
	 */
    public void setPath(String path) {
        this.path = path;
    }

    /**
	 * Sets the template manager.
	 * 
	 * @param templateManager
	 *            the new template manager
	 */
    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    /**
	 * Sets the type.
	 * 
	 * @param type
	 *            Type of template
	 */
    public void setType(String type) {
        this.type = type;
    }

    /**
	 * Setup the template content.
	 */
    public void setup() {
        EditorKit editorKit;
        if ("text/html".equals(this.format)) {
            editorKit = new HTMLEditorKit();
            if (this.hyperlinkListener == null) {
                addHyperlinkListener(createListener());
            }
        } else if ("text/plain".equals(this.format)) {
            editorKit = createDefaultEditorKit();
        } else {
            log.warn("No format has beed specified, using default format");
            editorKit = createDefaultEditorKit();
        }
        setEditorKit(editorKit);
        Template template = this.templateManager.getTemplateEngine(this.type).getTemplate(this.path);
        String text = TemplateUtils.generateString(template, this.context);
        Document document = editorKit.createDefaultDocument();
        try {
            BufferedReader reader = new BufferedReader(new StringReader(text));
            try {
                editorKit.read(reader, document, 0);
                setDocument(document);
            } finally {
                reader.close();
            }
        } catch (Exception ex) {
            log.error("Cannot read the template", ex);
        }
    }

    /**
	 * Creates an hyperlink listener which is suitable for the HTML page. It
	 * listens to the <code>action</code> pseudo-protocol and forwards every
	 * other link to the browser.
	 * 
	 * @return Hyperlink listener
	 * 
	 * @see URLUtils#registerURLStreamHandler(String, java.net.URLStreamHandler)
	 * @see ActionURLStreamHandler
	 * @see DesktopService#browse(URL)
	 */
    protected HyperlinkListener createListener() {
        if (this.hyperlinkListener == null) {
            this.hyperlinkListener = new HyperlinkListener() {

                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        URL url = e.getURL();
                        String protocol = url.getProtocol();
                        if (ActionURLStreamHandler.PROTOCOL_ACTION.equals(protocol)) {
                            String actionId = url.getPath();
                            Map<String, String> request = Utils.parseQuery(url);
                            doAction(actionId, request);
                        } else {
                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                try {
                                    Desktop.getDesktop().browse(url.toURI());
                                } catch (Exception ex) {
                                    log.error(String.format("Cannot open URL %s", url), ex);
                                }
                            }
                        }
                    }
                }
            };
        }
        return this.hyperlinkListener;
    }

    /**
	 * Executes a command. By default, does nothing.
	 * 
	 * @param actionId
	 *            ID of the action to execute.
	 * @param params
	 *            Parameters for the execution context
	 */
    protected void doAction(String actionId, Map<String, String> params) {
        if (this.actionCallback != null) {
            this.actionCallback.doAction(actionId, params);
        }
    }
}
