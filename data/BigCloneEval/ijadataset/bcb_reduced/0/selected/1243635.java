package org.gaea.ui.component;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.gaea.ui.utilities.PanelGrid;

/**
 * This class draws a line with a title and a little circle. When you click on
 * that line, the component expands under another panel.
 * 
 * @author jsgoupil
 */
public class ComponentHider extends JPanel {

    /**
	 * Auto Generated
	 */
    private static final long serialVersionUID = -760332361430827410L;

    /**
	 * State fired by the component when it is clicked and enabled.
	 */
    public static final String PROPERTY_EXPANDED = "expandedState";

    /**
	 * Left and right margins of the component
	 */
    private static final int MARGIN_SIDE = 10;

    /**
	 * Top margin
	 */
    private static final int MARGIN_TOP = 10;

    /**
	 * Position in x of the written text.
	 */
    private static final int SPACE_TITLE_LEFT = 20;

    /**
	 * Space between text and line.
	 */
    private static final int SPACE_TITLE = 3;

    /**
	 * Position of the circle from on the right.
	 */
    private static final int POSITION_CIRCLE = 40;

    /**
	 * Circle width
	 */
    private static final int WIDTH_CIRCLE = 13;

    /**
	 * Text color
	 */
    private static final Color COLOR_TEXT = new Color(74, 167, 57);

    /**
	 * Color border
	 */
    private static final Color COLOR_BORDER = new Color(212, 208, 200);

    /**
	 * If the component is enabled. We use a variable since we use mutex on it.
	 */
    private boolean _enabled;

    /**
	 * Saved title. Can be empty
	 */
    private String _title;

    /**
	 * State if the under panel is displayed
	 */
    private boolean _expanded;

    /**
	 * Clickable section to show or hide the under panel.
	 */
    private Rectangle _clickable;

    /**
	 * Component under panel
	 */
    private Component _component;

    /**
	 * Panel for _component
	 */
    private JPanel _panel;

    /**
	 * Tester
	 * 
	 * @param args
	 */
    public static void main(String[] args) {
        JFrame dialog = new JFrame();
        JPanel pan = new JPanel();
        pan.add(new JButton("TESTING"));
        ComponentHider hider = new ComponentHider("Connexions R�centes", pan);
        dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new JLabel("Test"), BorderLayout.NORTH);
        mainPanel.add(hider, BorderLayout.CENTER);
        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setSize(new Dimension(400, 200));
        dialog.setVisible(true);
    }

    /**
	 * Inits with all information required
	 * 
	 * @param title
	 * @param component
	 * @param expanded
	 */
    public ComponentHider(String title, Component component, boolean expanded) {
        createAndShowGUI(title, component, expanded);
    }

    /**
	 * Inits without expanded state
	 * 
	 * @param title
	 * @param component
	 */
    public ComponentHider(String title, Component component) {
        this(title, component, false);
    }

    /**
	 * Inits without title
	 * 
	 * @param component
	 * @param expanded
	 */
    public ComponentHider(Component component, boolean expanded) {
        this("", component, expanded);
    }

    /**
	 * Inits without title and expanded state
	 * 
	 * @param component
	 */
    public ComponentHider(Component component) {
        this("", component, false);
    }

    /**
	 * Creates component and sets the position.
	 * 
	 * @param title
	 * @param component
	 * @param expanded
	 */
    private void createAndShowGUI(String title, Component component, boolean expanded) {
        _enabled = isEnabled();
        _expanded = expanded;
        LineWithExpansionCircle lineWithCircle = new LineWithExpansionCircle();
        LayoutManager layout = new BorderLayout();
        setLayout(layout);
        add(lineWithCircle, BorderLayout.NORTH);
        setComponent(component);
        _title = title;
        _clickable = new Rectangle();
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (isEnabled() && isInside(_clickable, e.getPoint())) {
                        _expanded = !_expanded;
                        repaint();
                        firePropertyChange("expandedState", !_expanded, _expanded);
                    }
                }
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        _component.setEnabled(enabled);
        synchronized (ComponentHider.this) {
            _enabled = enabled;
        }
        repaint();
    }

    /**
	 * Sets the under component
	 * 
	 * @param component
	 */
    public void setComponent(Component component) {
        _component = component;
        GridBagConstraints constraints = new GridBagConstraints();
        _panel = new PanelGrid(constraints);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.ipady = 1;
        _panel.add(component, constraints);
        add(_panel, BorderLayout.CENTER);
    }

    /**
	 * Displays the component if b is true otherwise, we hide it.
	 * 
	 * @param b
	 */
    private void displayComponent(boolean b) {
        Component[] component = getComponents();
        for (int i = 1; i < component.length; i++) {
            component[i].setVisible(b);
        }
    }

    /**
	 * Check if the mouse is in the rectangle.
	 * 
	 * @param rectangle
	 * @param point
	 * @return True if the mouse position is within the rectangle specified
	 */
    public static boolean isInside(Rectangle rectangle, Point point) {
        if (point.x >= rectangle.x && point.y >= rectangle.y && point.x <= rectangle.x + rectangle.width && point.y <= rectangle.y + rectangle.height) {
            return true;
        }
        return false;
    }

    /**
	 * @return True if the panel is opened.
	 */
    public boolean isExpanded() {
        return _expanded == true;
    }

    /**
	 * Component that displays a bar with its title. It also includes a circle
	 * with an up or down arrow.
	 * 
	 * @author jsgoupil
	 */
    private class LineWithExpansionCircle extends JComponent {

        /**
		 * Auto Generated
		 */
        private static final long serialVersionUID = -7842514036550141408L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isOpaque()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            final int w = this.getParent().getWidth();
            setSize(new Dimension(w, WIDTH_CIRCLE * 2));
            final Font font = new Font("Microsoft Sans Serif", Font.PLAIN, 11);
            final FontMetrics metric = this.getFontMetrics(font);
            final int circleHalf = (WIDTH_CIRCLE + 1) / 2;
            int title_w = metric.stringWidth(_title);
            int title_h = metric.getAscent();
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setColor(COLOR_BORDER);
            if (_title.equals("")) {
                g2.drawLine(MARGIN_SIDE, MARGIN_TOP, w - MARGIN_SIDE, MARGIN_TOP);
            } else {
                g2.drawLine(MARGIN_SIDE, MARGIN_TOP, SPACE_TITLE_LEFT - SPACE_TITLE, MARGIN_TOP);
                if (w - MARGIN_SIDE > SPACE_TITLE_LEFT + title_w + SPACE_TITLE) {
                    g2.drawLine(SPACE_TITLE_LEFT + title_w + SPACE_TITLE, MARGIN_TOP, w - MARGIN_SIDE, MARGIN_TOP);
                }
            }
            boolean enabled = true;
            synchronized (ComponentHider.this) {
                enabled = _enabled;
            }
            if (enabled) {
                g2.setColor(COLOR_TEXT);
            } else {
                g2.setColor(Color.GRAY);
            }
            g2.setFont(font);
            g2.drawString(_title, SPACE_TITLE_LEFT, MARGIN_TOP + title_h / 2);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int nMinPosition = Math.max(title_w + SPACE_TITLE_LEFT + SPACE_TITLE, w - POSITION_CIRCLE);
            g2.setColor(COLOR_BORDER);
            g2.fillOval(nMinPosition - 1, MARGIN_TOP - circleHalf, WIDTH_CIRCLE + 2, WIDTH_CIRCLE + 2);
            Color colorUsed;
            if (enabled) {
                colorUsed = COLOR_TEXT;
            } else {
                colorUsed = Color.GRAY;
            }
            g2.setPaint((new GradientPaint(nMinPosition, MARGIN_TOP, colorUsed, nMinPosition + circleHalf, MARGIN_TOP + circleHalf, colorUsed.darker(), false)));
            g2.fillOval(nMinPosition, MARGIN_TOP - circleHalf + 1, WIDTH_CIRCLE, WIDTH_CIRCLE);
            g2.setColor(COLOR_BORDER);
            g2.setStroke(new BasicStroke(2));
            if (_expanded == true) {
                g2.drawLine(nMinPosition + 3, MARGIN_TOP / 2 + 7, nMinPosition + 6, MARGIN_TOP / 2 + 4);
                g2.drawLine(nMinPosition + 6, MARGIN_TOP / 2 + 4, nMinPosition + 9, MARGIN_TOP / 2 + 7);
                displayComponent(true);
            } else {
                g2.drawLine(nMinPosition + 3, MARGIN_TOP / 2 + 4, nMinPosition + 6, MARGIN_TOP / 2 + 7);
                g2.drawLine(nMinPosition + 6, MARGIN_TOP / 2 + 7, nMinPosition + 9, MARGIN_TOP / 2 + 4);
                displayComponent(false);
            }
            _clickable = new Rectangle(MARGIN_SIDE, MARGIN_TOP - circleHalf, w - MARGIN_SIDE, WIDTH_CIRCLE);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(1, WIDTH_CIRCLE * 2);
        }
    }
}
