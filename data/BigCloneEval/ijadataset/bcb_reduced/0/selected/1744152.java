package com.barbarianprince.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JPanel;
import com.barbarianprince.bus.enums.GameState;
import com.barbarianprince.main.BPMain;
import com.barbarianprince.main.Globals;
import com.barbarianprince.main.controllers.BPController;
import com.dalonedrau.gui.factory.DialogBorder;
import com.dalonedrau.gui.factory.GUIFactory;
import com.dalonedrau.gui.factory.NotesPanel;
import com.dalonedrau.gui.factory.StyledTextPane;

/**
 * @author <i>DaLoneDrau</i>
 *
 */
public final class IntroPanel extends JPanel {

    /** serial id. */
    private static final long serialVersionUID = 1L;

    /** the panel for displaying the actions. */
    private JPanel actionPanel;

    /** the evening panel. */
    private JButton btnQuit;

    /** the evening panel. */
    private JButton btnRules;

    /** the start button. */
    private JButton btnStart;

    /** the panel for displaying the rules. */
    private JPanel rulesPanel;

    /** Creates a new instance of GamePanel. */
    public IntroPanel() {
        super.setLayout(null);
        super.setOpaque(false);
        makeGUI();
    }

    /** Creates the rules panel. */
    private void createActionPanel() {
        btnStart = new ActionButton("NEW GAME");
        btnStart.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                BPController.setState(GameState.GAME);
            }
        });
        btnRules = new ActionButton("GAME RULES");
        final JPanel me = this;
        btnRules.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                actionPanel.setBounds(0, 0, 0, 0);
                Dimension size = rulesPanel.getPreferredSize();
                int x = (me.getPreferredSize().width - size.width) / 2;
                int y = (me.getPreferredSize().height - size.height) / 2;
                rulesPanel.setBounds(x, y, size.width, size.height);
            }
        });
        btnQuit = new ActionButton("QUIT");
        btnQuit.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                BPMain.ANIMATOR.stop();
                BPController.getMainFrame().dispose();
            }
        });
        int maxWidth = GUIFactory.getStringWidth(btnStart, btnStart.getText());
        maxWidth = Math.max(maxWidth, GUIFactory.getStringWidth(btnRules, btnRules.getText()));
        maxWidth = Math.max(maxWidth, GUIFactory.getStringWidth(btnQuit, btnQuit.getText()));
        maxWidth += Globals.BUTTON_SIDE_PADDING;
        int maxHeight = GUIFactory.getStringHeight(btnStart, btnStart.getText());
        maxHeight += Globals.BUTTON_TOP_PADDING;
        final int padding = 4;
        int x = padding;
        int y = padding;
        btnStart.setBounds(x, y, maxWidth, maxHeight);
        y += maxHeight;
        btnRules.setBounds(x, y, maxWidth, maxHeight);
        y += maxHeight;
        btnQuit.setBounds(x, y, maxWidth, maxHeight);
        final int panelWidth = padding + maxWidth + padding;
        final int panelHeight = padding + maxHeight + maxHeight + maxHeight + padding;
        actionPanel = new JPanel() {

            /** serial id. */
            private static final long serialVersionUID = 1L;

            /**
			 * {@inheritDoc}
			 */
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(panelWidth, panelHeight);
            }
        };
        actionPanel.setLayout(null);
        actionPanel.setOpaque(false);
        x = this.getPreferredSize().width;
        x -= actionPanel.getPreferredSize().width;
        x /= 2;
        y = this.getPreferredSize().height;
        y -= actionPanel.getPreferredSize().height;
        y /= 2;
        actionPanel.setBounds(x, y, actionPanel.getPreferredSize().width, actionPanel.getPreferredSize().height);
        actionPanel.setBorder(new ActionPanelBorder());
        actionPanel.add(btnStart);
        actionPanel.add(btnRules);
        actionPanel.add(btnQuit);
        super.add(actionPanel);
    }

    /** Creates the rules panel. */
    private void createRulesPanel() {
        StyledTextPane textpane = new StyledTextPane();
        textpane.setText(getRules());
        NotesPanel scrollpane = new NotesPanel(textpane);
        scrollpane.setBorder(null);
        final int padding = 4, msgWidth = 500, msgHeight = 300;
        int x = padding, y = padding;
        scrollpane.setBounds(x, y, msgWidth, msgHeight);
        y += msgHeight + padding;
        JButton btn = GUIFactory.getButton_h2WithActivationBorder("OK");
        final JPanel me = this;
        btn.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                rulesPanel.setBounds(0, 0, 0, 0);
                Dimension size = actionPanel.getPreferredSize();
                int x = (me.getPreferredSize().width - size.width) / 2;
                int y = (me.getPreferredSize().height - size.height) / 2;
                actionPanel.setBounds(x, y, size.width, size.height);
            }
        });
        int btnWidth = GUIFactory.getStringWidth(btn, btn.getText());
        int btnHeight = GUIFactory.getStringHeight(btn, btn.getText());
        btnWidth += Globals.BUTTON_SIDE_PADDING;
        btnHeight += Globals.BUTTON_TOP_PADDING;
        x = padding + (msgWidth - btnWidth) / 2;
        btn.setBounds(x, y, btnWidth, btnHeight);
        final int panelWidth = padding + msgWidth + padding;
        final int panelHeight = padding + msgHeight + padding + btnHeight + padding;
        rulesPanel = new JPanel() {

            /** serial id. */
            private static final long serialVersionUID = 1L;

            /**
			 * {@inheritDoc}
			 */
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(panelWidth, panelHeight);
            }
        };
        rulesPanel.setLayout(null);
        rulesPanel.setOpaque(false);
        rulesPanel.setBounds(0, 0, 0, 0);
        rulesPanel.setBorder(new DialogBorder());
        rulesPanel.add(scrollpane);
        rulesPanel.add(btn);
        super.add(rulesPanel);
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Globals.UI_COL2_WIDTH, Globals.UI_ROW2_HEIGHT);
    }

    /**
	 * Gets the rules.
	 * @return String
	 */
    private String getRules() {
        StringBuffer rules = new StringBuffer();
        rules.append("<h1>Rules</h1>");
        rules.append("<p>You are the <i>Barbarian Prince</i>, ");
        rules.append("exiled from your kingdom after your ");
        rules.append("father's assasination.  Explore the ");
        rules.append("southern lands, and along the way, ");
        rules.append("gather gold and allies to reclaim ");
        rules.append("your throne.</p>");
        rules.append("<p>You play the game in days. Each day ");
        rules.append("starts with you selecting an action, ");
        rules.append("such as travelling to a new hex on the ");
        rules.append("map. Depending on the action selected,");
        rules.append("you may experience a special event, ");
        rules.append("which you then resolve.</p>");
        rules.append("<p>After all events (if any) are resolved ");
        rules.append("for your daily action, you must then eat ");
        rules.append("your main (evening) meal,and if in a town, ");
        rules.append("castle, or temple hex, you must also ");
        rules.append("purchase lodging</p>");
        rules.append("<p>This ends the day, and you continue play ");
        rules.append("with the start of the next day, where you ");
        rules.append("select another action, etc. The game ");
        rules.append("continues until either you are killed, ");
        rules.append("or 70 days (10 weeks) elapse. If you haven't ");
        rules.append("won after 70 days, the game is automatically ");
        rules.append("lost!</p>");
        rules.append("<p>Many encounters may lead to fighting. ");
        rules.append("You may also have additional characters join ");
        rules.append("your \"party\". These additional characters ");
        rules.append("are especially useful in fights, although ");
        rules.append("some may have special knowledge or ");
        rules.append("abilities useful in certain events. ");
        rules.append("Magicians, wizards, witches, priests ");
        rules.append("and monks are especially useful people to ");
        rules.append("have in your party.</p>");
        return rules.toString();
    }

    /** Makes the UI. */
    private void makeGUI() {
        createRulesPanel();
        createActionPanel();
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        try {
            BufferedImage image = BPMain.getImage("qin_scroll_cm");
            if (image != null) {
                g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
