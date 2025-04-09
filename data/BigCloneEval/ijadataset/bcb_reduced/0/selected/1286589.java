package com.barbarianprince.gui.events;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import com.barbarianprince.bus.GameCharacter;
import com.barbarianprince.bus.event2.Event;
import com.barbarianprince.bus.turn.Flow;
import com.barbarianprince.gui.HirelingButton;
import com.barbarianprince.main.Globals;
import com.barbarianprince.main.controllers.BPController;
import com.dalonedrau.gui.factory.ActivationButtonBorder;
import com.dalonedrau.gui.factory.DialogBorder;
import com.dalonedrau.gui.factory.GUIFactory;

/**
 * @author <i>DaLoneDrau</i>
 *
 */
public final class HirelingPanel extends JPanel {

    /** the default text for the cancel button. */
    public static final String CANCEL = "Cancel";

    /** the maximum width of the display items - 500px. */
    private static final int DISPLAY_WIDTH = 500;

    /** the default text for the ok button. */
    public static final String OK = "OK";

    /** the internal padding - 4px. */
    private static final int PADDING = 4;

    /** serial id. */
    private static final long serialVersionUID = 1L;

    /** the OK button. */
    private JButton btnOK;

    /** the list of buttons. */
    private HirelingButton[] buttons;

    /** the chosen hires. */
    private ArrayList<GameCharacter> choices;

    /** the message heading label. */
    private JLabel heading;

    /** the message label. */
    private JLabel lblMessage;

    /** the possible selections. */
    private ArrayList<GameCharacter> selections;

    /** the total height of this panel. */
    private int topHeight;

    /** the total height of this panel. */
    private int totalHeight;

    /** Creates a new instance of HirelingPanel. */
    public HirelingPanel() {
        super.setBorder(new DialogBorder());
        super.setOpaque(false);
        super.setLayout(null);
        selections = new ArrayList<GameCharacter>();
        makeGUI();
    }

    /**
	 * Gets the choices.
	 * @return GameCharacter[ ]
	 */
    public GameCharacter[] getChoices() {
        return choices.toArray(new GameCharacter[choices.size()]);
    }

    /**
	 * Gets the message displayed to the player.
	 * @param notes the notes from the event
	 * @return String
	 */
    private String getMessage(final String notes) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html>");
        buffer.append(notes);
        buffer.append("<br><p>Highlight each hero you wish ");
        buffer.append("to hire, and then click the ");
        buffer.append("<strong>Hire</strong> button ");
        buffer.append("to complete the transaction.</p>");
        buffer.append("</html>");
        return buffer.toString();
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DISPLAY_WIDTH + PADDING * 2, totalHeight);
    }

    /** Makes the GUI. */
    private void makeGUI() {
        final int six = 6;
        heading = GUIFactory.getLabel_h2("Heroes for Hire");
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        super.add(heading);
        int height = GUIFactory.getStringHeight(heading, "FfGgQq");
        height += 2;
        int y = PADDING;
        heading.setBounds(PADDING, y, DISPLAY_WIDTH, height * 2);
        y += height * 2;
        y += PADDING;
        lblMessage = GUIFactory.getLabel_normal();
        height = GUIFactory.getStringHeight(lblMessage, "FfGgQq");
        super.add(lblMessage);
        lblMessage.setBounds(PADDING, y, DISPLAY_WIDTH, height * six);
        y += height * six;
        y += PADDING;
        topHeight = y;
        buttons = new HirelingButton[six];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new HirelingButton();
            buttons[i].addActionListener(new ActionListener() {

                /**
				 * {@inheritDoc}
				 */
                public void actionPerformed(final ActionEvent e) {
                    HirelingButton btn = (HirelingButton) e.getSource();
                    ActivationButtonBorder border = (ActivationButtonBorder) btn.getBorder();
                    if (border.isActive()) {
                        choices.remove(btn.getHireling());
                    } else {
                        choices.add(btn.getHireling());
                    }
                    border.switchActivation();
                }
            });
            super.add(buttons[i]);
        }
        btnOK = GUIFactory.getButton_h1("OK");
        btnOK.addActionListener(new ActionListener() {

            /**
			 * {@inheritDoc}
			 */
            public void actionPerformed(final ActionEvent e) {
                BPController.hideHirelingPanel();
                Flow.fireActions();
            }
        });
        btnOK.setEnabled(true);
        super.add(btnOK);
        totalHeight = y;
    }

    /** Sets the button display. */
    private void setButtons() {
        int x = PADDING, y = topHeight;
        System.out.println(y);
        for (int i = 0; i < buttons.length; i++) {
            if (i < selections.size()) {
                buttons[i].setHireling(selections.get(i));
                Dimension size = buttons[i].getPreferredSize();
                if (i % 2 == 1) {
                    x = DISPLAY_WIDTH - size.width;
                } else {
                    x = PADDING;
                }
                if (i > 1 && i % 2 == 0) {
                    y += size.height;
                    y += PADDING;
                }
                buttons[i].setBounds(x, y, size.width, size.height);
                if (i == selections.size() - 1) {
                    y += size.height;
                    y += PADDING;
                }
            } else {
                buttons[i].setBounds(0, 0, 0, 0);
            }
        }
        int height = GUIFactory.getStringHeight(btnOK, btnOK.getText());
        height += Globals.BUTTON_TOP_PADDING;
        int width = GUIFactory.getStringWidth(btnOK, btnOK.getText());
        width += Globals.BUTTON_SIDE_PADDING;
        x = PADDING + (DISPLAY_WIDTH - width) / 2;
        btnOK.setBounds(x, y, width, height);
        y += height + PADDING;
        totalHeight = y;
    }

    /**
	 * Sets the message to be displayed.
	 * @param event the caller for the message
	 * @param title the message's title
	 * @param message the message
	 * @param hirelings the hirelings to be displayed
	 */
    public void setMessage(final Event event, final String title, final String message, final GameCharacter[] hirelings) {
        if (!title.startsWith("<html")) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<html>");
            buffer.append(title);
            buffer.append("</html>");
            heading.setText(buffer.toString());
        } else {
            heading.setText(title);
        }
        lblMessage.setText(getMessage(message));
        selections = new ArrayList<GameCharacter>();
        choices = new ArrayList<GameCharacter>();
        for (int i = 0; i < hirelings.length; i++) {
            selections.add(hirelings[i]);
        }
        setButtons();
    }
}
