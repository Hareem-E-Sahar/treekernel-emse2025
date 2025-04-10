package mya_dc.shared_classes.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.util.StringTokenizer;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * A class that allows the use a Multi-label Label
 *
 * @author      Internet
 */
public class MultiLabel extends JComponent implements SwingConstants {

    private static final long serialVersionUID = -1611795277013703880L;

    int num_lines, line_height, line_ascent;

    int max_width = -1;

    int text_height = -1;

    int[] line_widths;

    int btnMarginWidth = 1;

    private String lines[] = null;

    private int horizontalAlignment = LEADING;

    private int textAlignment = LEADING;

    private int verticalAlignment = CENTER;

    public MultiLabel() {
        this("", LEADING, LEADING, CENTER);
    }

    public MultiLabel(String text) {
        this(text, LEADING, LEADING, CENTER);
    }

    public MultiLabel(String text, int horizontalAlignment) {
        this(text, horizontalAlignment, LEADING, CENTER);
    }

    public MultiLabel(String text, int horizontalAlignment, int textAlignment) {
        this(text, horizontalAlignment, textAlignment, CENTER);
    }

    public MultiLabel(String str, int horizontalAlignment, int textAlignment, int verticalAlignment) {
        this.setForeground(UIManager.getColor("Label.foreground"));
        this.setBackground(UIManager.getColor("Label.background"));
        this.setFont(UIManager.getFont("Label.font"));
        setText(str);
        this.horizontalAlignment = horizontalAlignment;
        this.textAlignment = textAlignment;
        this.verticalAlignment = verticalAlignment;
    }

    public void setText(String text) {
        if (text == null) text = "";
        StringTokenizer tkn = new StringTokenizer(text, "\n");
        num_lines = tkn.countTokens();
        lines = new String[num_lines];
        line_widths = new int[num_lines];
        for (int i = 0; i < num_lines; i++) lines[i] = tkn.nextToken();
        recalculateDimension();
    }

    public void addText(String text) {
        if (text == null) text = "";
        StringTokenizer tkn = new StringTokenizer(text, "\n");
        String[] prev = new String[num_lines];
        int prevNum = num_lines;
        for (int i = 0; i < num_lines; i++) {
            prev[i] = lines[i];
        }
        num_lines += tkn.countTokens();
        lines = new String[num_lines];
        line_widths = new int[num_lines];
        for (int i = 0; i < prevNum; i++) lines[i] = prev[i];
        for (int i = prevNum; i < num_lines; i++) lines[i] = tkn.nextToken();
        recalculateDimension();
    }

    private void recalculateDimension() {
        FontMetrics fontmetrics = getFontMetrics(getFont());
        line_height = fontmetrics.getHeight();
        line_ascent = fontmetrics.getAscent();
        max_width = 0;
        for (int i = 0; i < num_lines; i++) {
            line_widths[i] = fontmetrics.stringWidth(lines[i]);
            max_width = Math.max(max_width, line_widths[i]);
        }
        max_width += 2 * btnMarginWidth;
        text_height = num_lines * line_height;
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }

    public Dimension getMinimumSize() {
        if (max_width == -1 || text_height == -1) recalculateDimension();
        Insets insets = getInsets();
        return new Dimension(max_width + insets.left + insets.right, text_height + insets.top + insets.bottom);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension d = getSize();
        if (d.width != max_width || d.height != text_height) recalculateDimension();
        Insets insets = this.getInsets();
        int y = 0;
        if (verticalAlignment == TOP) {
            y = insets.top + line_ascent;
        } else if (verticalAlignment == CENTER) {
            y = insets.top + line_ascent;
            int clientAreaHeight = d.height - insets.top - insets.bottom;
            y = y + (clientAreaHeight - text_height) / 2;
        } else if (verticalAlignment == BOTTOM) {
            int clientAreaBottom = d.height - insets.bottom;
            y = clientAreaBottom - text_height;
            y += line_ascent;
        }
        for (int i = 0; i < num_lines; i++) {
            int ha = getBidiHorizontalAlignment(horizontalAlignment);
            int x = 0;
            if (ha == LEFT) {
                ha = getBidiHorizontalAlignment(textAlignment);
                if (ha == LEFT) x = insets.left; else if (ha == RIGHT) x = max_width - line_widths[i] + insets.left; else if (ha == CENTER) x = insets.left + (max_width - line_widths[i]) / 2;
            } else if (ha == RIGHT) {
                ha = getBidiHorizontalAlignment(textAlignment);
                if (ha == LEFT) x = d.width - max_width - insets.right; else if (ha == RIGHT) x = d.width - line_widths[i] - insets.right; else if (ha == CENTER) x = d.width - max_width - insets.right + (max_width - line_widths[i]) / 2;
            } else if (ha == CENTER) {
                ha = getBidiHorizontalAlignment(textAlignment);
                int clientAreaWidth = d.width - insets.left - insets.right;
                if (ha == LEFT) x = insets.left + (clientAreaWidth - max_width) / 2; else if (ha == RIGHT) x = insets.left + (clientAreaWidth - max_width) / 2 + (max_width - line_widths[i]); else if (ha == CENTER) x = insets.left + (clientAreaWidth - line_widths[i]) / 2;
            }
            x += btnMarginWidth;
            g.drawString(lines[i], x, y);
            y += line_height;
        }
    }

    private int getBidiHorizontalAlignment(int ha) {
        if (ha == LEADING) {
            if (getComponentOrientation().isLeftToRight()) ha = LEFT; else ha = RIGHT;
        } else if (ha == TRAILING) {
            if (getComponentOrientation().isLeftToRight()) ha = RIGHT; else ha = LEFT;
        }
        return ha;
    }

    public int getVerticalAlignment() {
        return this.verticalAlignment;
    }

    public void setVerticalAlignment(int verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        repaint();
    }

    public int getHorizontalAlignment() {
        return this.horizontalAlignment;
    }

    public void setHorizontalAlignment(int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        repaint();
    }

    public int getTextAlignment() {
        return this.textAlignment;
    }

    public void setTextAlignment(int textAlignment) {
        this.textAlignment = textAlignment;
        repaint();
    }

    @SuppressWarnings("deprecation")
    public static void main(String args[]) throws Exception {
        String wiseText = "This is a true example\n" + "Of the MultiLine" + "\n" + "Label class";
        MultiLabel x0 = new MultiLabel(wiseText);
        MultiLabel x1 = new MultiLabel(wiseText, MultiLabel.RIGHT, MultiLabel.RIGHT);
        MultiLabel x2 = new MultiLabel(wiseText, MultiLabel.CENTER);
        MultiLabel x3 = new MultiLabel(wiseText, MultiLabel.CENTER, MultiLabel.RIGHT);
        MultiLabel x4 = new MultiLabel(wiseText, MultiLabel.CENTER, MultiLabel.CENTER);
        MultiLabel x5 = new MultiLabel(wiseText, MultiLabel.RIGHT, MultiLabel.LEFT, MultiLabel.BOTTOM);
        JPanel mainPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        mainPanel.add(x0);
        mainPanel.add(x1);
        mainPanel.add(x2);
        mainPanel.add(x3);
        mainPanel.add(x4);
        mainPanel.add(x5);
        mainPanel.setBackground(SystemColor.control);
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        for (int i = 0; i < mainPanel.getComponentCount(); i++) ((JComponent) mainPanel.getComponent(i)).setBorder(new LineBorder(Color.red));
        JFrame fr = new JFrame("MultiLabel Example");
        fr.getContentPane().add(mainPanel);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.setSize(460, 350);
        fr.setLocationRelativeTo(null);
        fr.show();
    }
}
