package toxtree.ui.tree;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.ButtonUI;
import javax.swing.text.html.HTMLEditorKit;
import org.openscience.cdk.interfaces.IAtomContainer;
import toxTree.core.IDecisionInteractive;
import toxTree.core.IDecisionMethod;
import toxTree.core.IDecisionRule;
import toxTree.core.IDecisionRuleList;
import toxTree.io.Tools;
import toxtree.ui.PropertyEditor;
import ambit2.base.config.Preferences;
import ambit2.base.config.Preferences.VTAGS;
import ambit2.ui.editors.PreferencesPanel;
import com.l2fprod.common.swing.JOutlookBar;
import com.l2fprod.common.swing.PercentLayout;

public class TreeOptions extends JSplitPane {

    /**
	 * 
	 */
    private static final long serialVersionUID = -4430297393022588312L;

    public TreeOptions(IDecisionMethod tree, IAtomContainer atomcontainer) {
        super(JSplitPane.HORIZONTAL_SPLIT);
        JOutlookBar outlook = new JOutlookBar();
        outlook.setBackground(Color.white);
        outlook.setTabPlacement(JTabbedPane.LEFT);
        addGenericTab(outlook);
        if (atomcontainer != null) addMoleculeTab(outlook, atomcontainer);
        addTreeTab(outlook, tree);
        setLeftComponent(outlook);
        JTextPane details = new JTextPane();
        details.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } else Tools.openURL(e.getURL().toString());
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
            }
        });
        details.setEditorKit(new HTMLEditorKit());
        details.setText(tree.getExplanation());
        setRightComponent(new JScrollPane(details));
        setPreferredSize(new Dimension(450, 400));
    }

    void addGenericTab(JOutlookBar tabs) {
        JPanel panel = new JPanel();
        panel.setLayout(new PercentLayout(PercentLayout.VERTICAL, 0));
        panel.setOpaque(false);
        JButton button = new JButton(new PropertiesAction("Options", "General Toxtree options") {

            @Override
            public void actionPerformed(ActionEvent e) {
                setRightComponent(new PreferencesPanel(new Preferences.VTAGS[] { VTAGS.General, VTAGS.Structure, VTAGS.RemoteQuery }).getJComponent());
            }
        });
        try {
            button.setUI((ButtonUI) Class.forName((String) UIManager.get("OutlookButtonUI")).newInstance());
            button.setIcon(Tools.getImage("cog.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        panel.add(button);
        JScrollPane scroll = tabs.makeScrollPane(panel);
        tabs.addTab("General", scroll);
    }

    void addMoleculeTab(JOutlookBar tabs, IAtomContainer atomcontainer) {
        JPanel panel = new JPanel();
        panel.setLayout(new PercentLayout(PercentLayout.VERTICAL, 0));
        panel.setOpaque(false);
        JButton button = new JButton(new StructureAction(atomcontainer, "Structure", "Current structure") {

            public void actionPerformed(ActionEvent e) {
                setRightComponent(new PropertyEditor(getMolecule(), null));
            }
        });
        try {
            button.setUI((ButtonUI) Class.forName((String) UIManager.get("OutlookButtonUI")).newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        panel.add(button);
        JScrollPane scroll = tabs.makeScrollPane(panel);
        tabs.addTab("Structure", scroll);
    }

    void addTreeTab(JOutlookBar tabs, IDecisionMethod tree) {
        JPanel panel = null;
        IDecisionRuleList rules = tree.getRules();
        for (int i = 0; i < rules.size(); i++) {
            IDecisionRule rule = rules.getRule(i);
        }
        if (panel != null) {
            JScrollPane scroll = tabs.makeScrollPane(panel);
            tabs.addTab("", scroll);
            int index = tabs.indexOfComponent(scroll);
            tabs.setTitleAt(index, "Rules options");
            tabs.setToolTipTextAt(index, tree.getTitle());
        }
    }
}

class StructureAction extends PropertiesAction {

    protected IAtomContainer molecule = null;

    /**
	 * 
	 */
    private static final long serialVersionUID = -1074623615249070194L;

    public StructureAction(IAtomContainer molecule, String title, String hint) {
        super(title, hint);
        setMolecule(molecule);
    }

    public IAtomContainer getMolecule() {
        return molecule;
    }

    public void setMolecule(IAtomContainer molecule) {
        this.molecule = molecule;
    }
}

class RuleAction extends PropertiesAction {

    protected IDecisionInteractive rule = null;

    /**
	 * 
	 */
    private static final long serialVersionUID = -1074623615249070194L;

    public RuleAction(IDecisionInteractive rule, String title, String hint) {
        super(title, hint);
        this.rule = rule;
    }

    public IDecisionInteractive getRule() {
        return rule;
    }

    public void setRule(IDecisionInteractive rule) {
        this.rule = rule;
    }
}

class PropertiesAction extends AbstractAction {

    /**
	 * 
	 */
    private static final long serialVersionUID = -1074623615249070194L;

    public PropertiesAction(String title, String hint) {
        super(title);
        putValue(AbstractAction.SHORT_DESCRIPTION, hint);
    }

    public void actionPerformed(ActionEvent e) {
    }
}
