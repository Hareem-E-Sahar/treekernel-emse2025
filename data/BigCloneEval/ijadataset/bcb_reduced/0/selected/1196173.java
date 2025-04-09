package vademecum.advisor;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.tree.TreePath;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.Plugin;
import org.java.plugin.PluginClassLoader;
import org.java.plugin.PluginManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMSource;
import vademecum.Core;
import vademecum.core.experiment.ExperimentModel;
import vademecum.core.experiment.ExperimentNode;
import vademecum.extensionPoint.ExtensionFactory;
import vademecum.extensionPoint.IDataNode;
import vademecum.extensionPoint.IVisualizer;
import vademecum.ui.project.DataNavigation;
import vademecum.ui.project.DocBrowser;
import vademecum.ui.project.Expertice;

public class Advisor implements HyperlinkListener {

    /**
	 * private logger instance
	 */
    private static Log log = LogFactory.getLog(Advisor.class);

    /**
	 * The experimentNode to analyze
	 */
    private ExperimentNode node;

    private Document document;

    /**
	 * The XML-Stylesheet
	 */
    private static File xsltFile = new File("data/advisor.xsl");

    /**
	 * static html file to show, if the datanode plugin has no
	 * help file available
	 */
    private static final String noAdvisorAvailable = "data" + File.separator + "xhtml" + File.separator + "advisor" + File.separator + "noAdvisorAvailable.html";

    private static final String welcome = "data" + File.separator + "xhtml" + File.separator + "advisor" + File.separator + "welcome.html";

    /**
	 * String with the results of the transforming process
	 */
    private StringWriter output;

    public Advisor() {
        output = new StringWriter();
    }

    public Advisor(PluginManager manager, ExperimentNode node) {
        this();
        setNode(node);
    }

    /**
	 * set the advisors experiment node
	 * @param node
	 */
    public void setNode(ExperimentNode node) {
        log.debug("setting node " + node.getName());
        this.node = node;
    }

    public ExperimentNode getNode() {
        return node;
    }

    /**
	 * init the advisor xml document with the given node
	 * @throws IOException
	 *
	 */
    public void build() {
        if (node.getParent() == null) {
            log.debug("rendering EXPERIMENT ROOT");
            FileReader fr;
            try {
                fr = new FileReader(welcome);
                BufferedReader br = new BufferedReader(fr);
                int c;
                while ((c = br.read()) != -1) {
                    output.write((char) c);
                }
                br.close();
                fr.close();
                return;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        URL url = null;
        IDataNode method = node.getMethod();
        Plugin plugin = Core.manager.getPluginFor(method);
        if (plugin == null) {
            log.warn("unable to get plugin for " + node.getName());
            return;
        }
        PluginClassLoader pcl = Core.manager.getPluginClassLoader(plugin.getDescriptor());
        log.debug("trying to load advisor.xml from " + plugin.getDescriptor().getId());
        url = pcl.getResource("/resources/advisor.xml");
        if (url == null) {
            url = pcl.getResource("advisor.xml");
        }
        log.debug(url == null ? "advisor.xml not found" : "advisor.xml found !");
        if (url != null) {
            SAXBuilder b = new SAXBuilder();
            try {
                document = b.build(url);
                Document analyzerDoc = analyze(document, node);
                Source xmlSource = new JDOMSource(analyzerDoc);
                Source xsltSource = new StreamSource(xsltFile);
                TransformerFactory transFact = TransformerFactory.newInstance();
                Transformer trans;
                trans = transFact.newTransformer(xsltSource);
                log.debug("processing " + url.toString());
                trans.transform(xmlSource, new StreamResult(output = new StringWriter()));
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        }
        log.trace("#chars outputted: " + output.toString().length());
        if (output.toString().trim().equals("")) {
            try {
                FileReader fr = new FileReader(noAdvisorAvailable);
                log.debug("reading default file: " + noAdvisorAvailable);
                int c;
                while ((c = fr.read()) != -1) {
                    output.write((char) c);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
	 * this will analyze the given node and call node specific advise() function
	 * if interface <code>vademecum.advisor.IAdvisor</code> is implemented
	 *
	 */
    protected Document analyze(Document spec, ExperimentNode node) {
        Document analyzed = (Document) spec.clone();
        AdvisorBean advisorBean = new AdvisorBean(analyzed, node.getState());
        Element root = analyzed.getRootElement();
        Element requirements = root.getChild("requires");
        Element suggestions = root.getChild("suggests");
        ArrayList<Element> toRemove = new ArrayList<Element>();
        IDataNode dataNode = node.getMethod();
        if (dataNode instanceof IAdvisor) {
            log.info(node.getName() + " implements IAdvisor !!! Sending analyze request");
            ((IAdvisor) dataNode).analyze(advisorBean);
        }
        Iterator iter = requirements.getChildren("requirement").iterator();
        TreePath path = node.getTreePath().getParentPath();
        while (iter.hasNext()) {
            Element elem = (Element) iter.next();
            String extId = elem.getAttribute("id").getValue();
            log.debug("checking if requirement " + extId + " is in treepath");
            Object[] nodes = path.getPath();
            for (Object obj : nodes) {
                ExperimentNode n = (ExperimentNode) obj;
                if (extId.equals(n.getExtensionUid())) {
                    toRemove.add(elem);
                }
            }
        }
        iter = toRemove.iterator();
        while (iter.hasNext()) {
            Element elem = (Element) iter.next();
            requirements.removeContent(elem);
        }
        toRemove.clear();
        iter = suggestions.getChildren("suggestion").iterator();
        while (iter.hasNext()) {
            Element elem = (Element) iter.next();
            if (node.getState() != ExperimentNode.EXECUTED) {
                log.debug("removing element " + elem.getName());
                toRemove.add(elem);
            }
        }
        iter = toRemove.iterator();
        while (iter.hasNext()) {
            Element elem = (Element) iter.next();
            suggestions.removeContent(elem);
        }
        return analyzed;
    }

    /**
	 * return the internal <code>ExperimentNode</code>'s State.
	 * The states are defined in <code>ExperimentNode</code> and you
	 *
	 * @return int The state of the internal <code>ExperimentNode</code>
	 */
    public int getState() {
        return node.getState();
    }

    /**
	 * this will add the given extension before the internal node and add
	 * the internal node to the given extension
	 */
    protected void prependNode(String extensionUid) {
        if (node.getParent() == null) {
            log.debug("kann nicht vor der Wurzel einfügen");
            return;
        }
        ExperimentNode newNode = ExtensionFactory.createDataNode(extensionUid);
        ExperimentNode parent = (ExperimentNode) node.getParent();
        DataNavigation datanav = ((Expertice) Core.projectPanel.getSelectedComponent()).getDataNavigation();
        log.debug("removing node from parent");
        ((ExperimentModel) datanav.getModel()).removeNodeFromParent(node);
        log.debug("addNode newNode,node");
        datanav.addNode(newNode, node);
        log.debug("addNode parent,newNode");
        datanav.addNode(parent, newNode);
        datanav.expandPath(node.getTreePath());
        log.debug("prepend finished");
    }

    /**
	 * this will add the given extension before the internal node
	 */
    protected void appendNode(String extensionUid) {
        log.info("adding " + extensionUid + " from Advisor");
        DataNavigation datanav = ((Expertice) Core.projectPanel.getSelectedComponent()).getDataNavigation();
        ExperimentNode newNode = datanav.addNode(node, extensionUid);
        datanav.showPreferencesDialog(newNode);
    }

    /**
	 * Render XML Data to HTML
	 *
	 * @return String HTML Data
	 */
    public String render() {
        if (output.equals("")) {
            log.debug("rendering advisor for " + node.getName());
            build();
        }
        return output.toString();
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        Desktop desktop = null;
        if (e.getEventType().equals(EventType.ACTIVATED) && e.getURL() != null) {
            String query = e.getURL().getQuery();
            String key = "", value = "";
            if (query != null) {
                key = query.substring(0, e.getURL().getQuery().indexOf('='));
                value = query.substring(e.getURL().getQuery().indexOf('=') + 1);
                log.debug(key);
                log.debug(value);
            }
            if ("view".equals(key)) {
                IVisualizer vis = ExtensionFactory.createVisualizer(value, node);
                if (vis != null) vis.visualize(node);
            } else if ("prepend".equals(key)) {
                if (node.getState() != ExperimentNode.EXECUTING) {
                    prependNode(value);
                } else JOptionPane.showMessageDialog(Core.frame, "Kann den Knoten während der Berechnung nicht ändern.");
            } else if ("append".equals(key)) {
                this.appendNode(value);
            } else if ("docs".equals(key)) {
                log.debug("open javahelp for id " + value);
                DocBrowser docBrowser = DocBrowser.getInstance();
                docBrowser.jumpTo(value);
                docBrowser.setVisible(true);
            } else {
                try {
                    if (Desktop.isDesktopSupported()) {
                        desktop = Desktop.getDesktop();
                        if (desktop.isSupported(Desktop.Action.BROWSE)) {
                            desktop.browse(e.getURL().toURI());
                        }
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(Core.frame, "Ungültiger URL");
                }
            }
        }
    }
}
