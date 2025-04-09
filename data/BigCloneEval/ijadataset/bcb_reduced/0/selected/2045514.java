package pogvue.gui;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import pogvue.datamodel.*;
import pogvue.gui.event.*;
import pogvue.gui.hub.*;
import pogvue.analysis.*;
import pogvue.io.*;
import pogvue.util.*;

public class ExpressionGrid extends JPanel {

    public JPanel panel;

    public Vector exp_levels;

    public int boxsize = 12;

    public boolean absolute = false;

    public int minval;

    public int maxval;

    public ExpressionGrid(Vector exp_levels) {
        this.exp_levels = exp_levels;
    }

    public void setLevels(Vector exp_levels) {
        this.exp_levels = exp_levels;
    }

    public void setAbsoluteValues(boolean val) {
        this.absolute = val;
    }

    public void setMinValue(int minval) {
        this.minval = minval;
    }

    public void setMaxValue(int maxval) {
        this.maxval = maxval;
    }

    public void paint(Graphics g) {
        Vector tmp = new Vector();
        for (int i = 0; i < exp_levels.size(); i++) {
            tmp.addElement(exp_levels.elementAt(i));
        }
        Collections.sort(tmp, new ExpressionComparer());
        int midindex = (int) (tmp.size() / 2);
        int mid = ((ExpressionLevel) tmp.elementAt(midindex)).getLevel();
        int min = ((ExpressionLevel) tmp.elementAt(0)).getLevel();
        int max = ((ExpressionLevel) tmp.lastElement()).getLevel();
        if (absolute) {
            min = minval;
            max = maxval;
            mid = (min + max) / 2;
        }
        Color c = null;
        for (int i = 0; i < exp_levels.size(); i++) {
            int val = ((ExpressionLevel) exp_levels.elementAt(i)).getLevel();
            if (val < min) {
                c = Color.cyan;
            } else if (val > max) {
                c = Color.magenta;
            } else if (val >= mid) {
                int tmpval = (val - mid) * 255 / (max - mid + 1);
                if (tmpval > 255) {
                    tmpval = 255;
                }
                if (tmpval < 0) {
                    tmpval = 0;
                }
                c = new Color(tmpval, 0, 0);
            } else {
                int tmpval = (mid - val) * 255 / (mid - min + 1);
                if (tmpval > 255) {
                    tmpval = 255;
                }
                if (tmpval < 0) {
                    tmpval = 0;
                }
                c = new Color(0, tmpval, 0);
            }
            g.setColor(c);
            g.fillRect(boxsize * i, 0, boxsize, boxsize);
        }
    }

    public Dimension preferredSize() {
        if (exp_levels != null) {
            return new Dimension(boxsize * exp_levels.size(), boxsize);
        } else {
            return new Dimension(boxsize * 79, boxsize);
        }
    }

    public Dimension minimumSize() {
        return preferredSize();
    }

    public static void main(String[] args) {
        try {
            Hashtable opts = GetOptions.get(args);
            String expfile = null;
            if (opts.containsKey("-expfile")) {
                expfile = (String) opts.get("-expfile");
            }
            ExpFile ef = new ExpFile(expfile, "File");
            Vector exp_levels = ef.getExpLevels();
            Vector tmp = (Vector) exp_levels.elementAt(0);
            JFrame jf = new JFrame();
            ExpressionGrid ep = new ExpressionGrid(tmp);
            jf.getContentPane().setLayout(new BorderLayout());
            jf.add(ep, "Center");
            jf.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
