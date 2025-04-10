package net.sf.xpontus.view;

import net.sf.xpontus.codecompletion.xml.XMLAssistProcessor;
import net.sf.xpontus.codecompletion.xml.XMLSchemaCompletionParser;
import net.sf.xpontus.core.utils.BeanUtilities;
import net.sf.xpontus.parsers.XMLLexer;
import net.sf.xpontus.parsers.XMLParser;
import net.sf.xpontus.parsers.XmlNode;
import net.sf.xpontus.view.editor.SyntaxDocument;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Element;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author Yves Zoundi
 */
public class XMLOutlineBuilder {

    private DefaultTreeModel treeModel;

    private XMLParser parser;

    /**
     * <p>
     * This initializes the needed Swing settings.
     * </p>
     */
    public XMLOutlineBuilder() {
    }

    /**
     * <p>
     * This will construct the tree using Swing.
     * </p>
     *
     * @param doc  
     */
    public void init(final javax.swing.text.Document doc) {
        String dtdLocation = null;
        XMLLexer lexer = null;
        String schemaLocation = null;
        SyntaxDocument mDoc = (SyntaxDocument) doc;
        if (mDoc.getContentAssist() != null) {
            try {
                String mText = doc.getText(0, doc.getLength());
                Reader mReader = new StringReader(mText);
                lexer = new XMLLexer(mReader);
                parser = new XMLParser(lexer);
                parser.parse();
            } catch (Exception err) {
            }
            if (lexer != null) {
                dtdLocation = lexer.getDTDLocation();
                schemaLocation = lexer.getSchemaLocation();
            }
            try {
                if (dtdLocation != null) {
                    dtdLocation = dtdLocation.substring(1, dtdLocation.length() - 1);
                    XMLAssistProcessor contentAssist = mDoc.getContentAssist();
                    if (contentAssist != null) {
                        contentAssist.setCompletionParser(new net.sf.xpontus.codecompletion.xml.DTDCompletionParser());
                        java.net.URL url = new java.net.URL(dtdLocation);
                        java.io.Reader dtdReader = new java.io.InputStreamReader(url.openStream());
                        contentAssist.updateAssistInfo(lexer.getdtdPublicId(), dtdLocation, dtdReader);
                    }
                } else if (schemaLocation != null) {
                    XMLAssistProcessor contentAssist = mDoc.getContentAssist();
                    if (contentAssist != null) {
                        contentAssist.setCompletionParser(new XMLSchemaCompletionParser());
                        java.net.URL url = new java.net.URL(schemaLocation);
                        java.io.Reader dtdReader = new java.io.InputStreamReader(url.openStream());
                        contentAssist.updateAssistInfo(lexer.getdtdPublicId(), schemaLocation, dtdReader);
                    }
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }

    /**
     * 
     * @param doc 
     */
    public void updateOutline(final javax.swing.text.Document doc) {
        Thread t = new Thread() {

            public void run() {
                DefaultMutableTreeNode rootNode = parser.getRootNode();
                if (rootNode.getChildCount() > 0) {
                    final XmlNode child = (XmlNode) parser.getRootNode().getFirstChild();
                    XmlNode lexerNode = recursivelyCopyNodes(child);
                    doc.putProperty("OUTLINE_DATA", lexerNode);
                }
                XPontusWindow.getInstance().getOutlineDockable().updateAll();
            }
        };
        t.start();
    }

    /**
     * 
     * @param doc 
     * @return 
     */
    private XmlNode recursivelyCopyNodes(XmlNode aNode) {
        XmlNode copy = new XmlNode(aNode.toString(), aNode.line, aNode.column);
        BeanUtilities.copyProperties(aNode, copy);
        copy.setAllowsChildren(aNode.getAllowsChildren());
        if (aNode.getChildCount() != 0) {
            Enumeration children = aNode.children();
            while (children.hasMoreElements()) {
                XmlNode child = (XmlNode) children.nextElement();
                XmlNode childCopy = this.recursivelyCopyNodes(child);
                copy.add(childCopy);
            }
        }
        return copy;
    }

    /**
     * 
     * @param line
     * @param column
     */
    private void gotoLine(int line, int column) {
        JEditorPane edit = XPontusWindow.getInstance().getCurrentEditor();
        Element element = edit.getDocument().getDefaultRootElement();
        if (element.getElement(line) == null) {
            JOptionPane.showMessageDialog(XPontusWindow.getInstance().getFrame(), XPontusWindow.getInstance().getI18nMessage("msg.noSuchLine"), XPontusWindow.getInstance().getI18nMessage("msg.error"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        int lineOffset = element.getElement(line).getStartOffset();
        int tokenOffset = lineOffset + column;
        edit.setCaretPosition(tokenOffset);
    }
}
