import java.awt.*;
import java.lang.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class DrawPanel extends Panel implements ActionListener {

    public Vector xstrokes = new Vector();

    public Vector ystrokes = new Vector();

    public Vector curxvec = null;

    public Vector curyvec = null;

    ActionListener al = null;

    public KCanvas c = new KCanvas(this);

    static final int NUMKAN = 5;

    private Vector possible_chinese_strokes = new Vector();

    public static String current_character_strokes = "";

    public static Vector<Vector<Stroke>> currentStrokes;

    public void addActionListener(ActionListener al) {
        if (this.al != null) {
            System.out.println("Error: " + "DrawPanel does not support multiple listeners.");
            return;
        } else this.al = al;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Clear")) {
            xstrokes.removeAllElements();
            ystrokes.removeAllElements();
            curxvec = null;
            curyvec = null;
            Rectangle r = c.getBounds();
            c.getGraphics().clearRect(0, 0, r.width, r.height);
            c.paint(c.getGraphics());
            current_character_strokes = "";
            currentStrokes.removeAllElements();
            return;
        }
        int sc;
        Vector minScores = new Vector();
        Vector minChars = new Vector();
        String curk;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader("unistrok." + xstrokes.size()));
        } catch (Exception ew) {
            this.getToolkit().beep();
            return;
        }
        try {
            String line;
            while (true) {
                line = in.readLine();
                String goline = "";
                if (line == null) {
                    if (al == null) {
                        Enumeration e1 = minScores.elements();
                        Enumeration e2 = minChars.elements();
                        while (e1.hasMoreElements()) {
                            String kanji = (String) e2.nextElement();
                            Integer score = (Integer) e1.nextElement();
                        }
                    } else {
                        int sz;
                        sz = minChars.size();
                        char[] kanj = new char[sz];
                        int i;
                        for (i = 0; i < sz; i++) {
                            String s;
                            s = (String) minChars.elementAt(sz - i - 1);
                            if (s.charAt(0) == '0') kanj[i] = '?'; else {
                                int index;
                                index = s.indexOf(' ');
                                if (index != -1) s = s.substring(0, index);
                                try {
                                    int hexcode;
                                    hexcode = Integer.parseInt(s, 16);
                                    kanj[i] = (char) hexcode;
                                } catch (Exception ez11) {
                                    kanj[i] = '?';
                                }
                            }
                        }
                        ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, new String(kanj));
                        al.actionPerformed(ae);
                    }
                    return;
                } else {
                    if (line.length() == 0) continue;
                    if (line.charAt(0) == '#') continue;
                    int index;
                    index = line.indexOf('|');
                    if (index == -1) continue;
                    curk = line.substring(0, index);
                    line = line.substring(index + 1);
                    String tokline;
                    String argline;
                    int tokindex = line.indexOf('|');
                    if (tokindex != -1) {
                        tokline = line.substring(0, tokindex);
                        argline = line.substring(tokindex + 1);
                    } else {
                        argline = null;
                        tokline = line;
                    }
                    StringTokenizer st = new StringTokenizer(tokline);
                    if (st.countTokens() != xstrokes.size()) continue;
                    WhileLoop: while (st.hasMoreTokens()) {
                        String tok = st.nextToken();
                        int i;
                        for (i = 0; i < tok.length(); i++) {
                            switch(tok.charAt(i)) {
                                case '2':
                                case '1':
                                case '3':
                                case '4':
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                                    goline = goline + tok.charAt(i);
                                    break;
                                case 'b':
                                    goline = goline + "62";
                                    break;
                                case 'c':
                                    goline = goline + "26";
                                    break;
                                case 'x':
                                    goline = goline + "21";
                                    break;
                                case 'y':
                                    goline = goline + "23";
                                    break;
                                case '|':
                                    break WhileLoop;
                                default:
                                    System.out.println("unknown symbol in kanji database");
                                    System.out.println(line);
                                    continue;
                            }
                        }
                        goline = goline + " ";
                    }
                    int ns;
                    if (minScores.size() < NUMKAN) ns = getScore(goline, 999999); else {
                        int cutoff1, cutoff2;
                        cutoff1 = ((Integer) minScores.firstElement()).intValue();
                        cutoff2 = ((Integer) minScores.lastElement()).intValue() * 2;
                        ns = getScore(goline, Math.min(cutoff1, cutoff2));
                    }
                    if (argline != null) {
                        st = new StringTokenizer(argline);
                        while (st.hasMoreTokens()) {
                            try {
                                String tok = st.nextToken();
                                int minindex;
                                minindex = tok.indexOf("-");
                                if (minindex == -1) {
                                    System.out.println("bad filter");
                                    continue;
                                }
                                String arg1, arg2;
                                arg1 = tok.substring(0, minindex);
                                arg2 = tok.substring(minindex + 1, tok.length());
                                int arg1stroke, arg2stroke;
                                arg1stroke = Integer.parseInt(arg1.substring(1));
                                boolean must = (arg2.charAt(arg2.length() - 1) == '!');
                                if (must) arg2stroke = Integer.parseInt(arg2.substring(1, arg2.length() - 1)); else arg2stroke = Integer.parseInt(arg2.substring(1));
                                Vector stroke1x, stroke1y, stroke2x, stroke2y;
                                stroke1x = (Vector) xstrokes.elementAt(arg1stroke - 1);
                                stroke1y = (Vector) ystrokes.elementAt(arg1stroke - 1);
                                stroke2x = (Vector) xstrokes.elementAt(arg2stroke - 1);
                                stroke2y = (Vector) ystrokes.elementAt(arg2stroke - 1);
                                int val1, val2;
                                switch(arg1.charAt(0)) {
                                    case 'x':
                                        val1 = ((Integer) stroke1x.firstElement()).intValue();
                                        break;
                                    case 'y':
                                        val1 = ((Integer) stroke1y.firstElement()).intValue();
                                        break;
                                    case 'i':
                                        val1 = ((Integer) stroke1x.lastElement()).intValue();
                                        break;
                                    case 'j':
                                        val1 = ((Integer) stroke1y.lastElement()).intValue();
                                        break;
                                    case 'a':
                                        val1 = (((Integer) stroke1x.firstElement()).intValue() + ((Integer) stroke1x.lastElement()).intValue()) / 2;
                                        break;
                                    case 'b':
                                        val1 = (((Integer) stroke1y.firstElement()).intValue() + ((Integer) stroke1y.lastElement()).intValue()) / 2;
                                        break;
                                    case 'l':
                                        int dx, dy;
                                        dx = ((Integer) stroke1x.lastElement()).intValue() - ((Integer) stroke1x.firstElement()).intValue();
                                        dy = ((Integer) stroke1y.lastElement()).intValue() - ((Integer) stroke1y.firstElement()).intValue();
                                        val1 = (int) (Math.sqrt((double) (dx * dx + dy * dy)));
                                        break;
                                    default:
                                        System.out.println("bad filter");
                                        continue;
                                }
                                switch(arg2.charAt(0)) {
                                    case 'x':
                                        val2 = ((Integer) stroke2x.firstElement()).intValue();
                                        break;
                                    case 'y':
                                        val2 = ((Integer) stroke2y.firstElement()).intValue();
                                        break;
                                    case 'i':
                                        val2 = ((Integer) stroke2x.lastElement()).intValue();
                                        break;
                                    case 'j':
                                        val2 = ((Integer) stroke2y.lastElement()).intValue();
                                        break;
                                    case 'a':
                                        val2 = (((Integer) stroke2x.firstElement()).intValue() + ((Integer) stroke2x.lastElement()).intValue()) / 2;
                                        break;
                                    case 'b':
                                        val2 = (((Integer) stroke2y.firstElement()).intValue() + ((Integer) stroke2y.lastElement()).intValue()) / 2;
                                        break;
                                    case 'l':
                                        int dx, dy;
                                        dx = ((Integer) stroke2x.lastElement()).intValue() - ((Integer) stroke2x.firstElement()).intValue();
                                        dy = ((Integer) stroke2y.lastElement()).intValue() - ((Integer) stroke2y.firstElement()).intValue();
                                        val2 = (int) (Math.sqrt((double) (dx * dx + dy * dy)));
                                        break;
                                    default:
                                        System.out.println("bad filter");
                                        continue;
                                }
                                ns = ns - (val1 - val2);
                                if (must && (val1 < val2)) ns += 9999999;
                            } catch (Exception ez2) {
                                System.out.println("bad filter");
                                continue;
                            }
                        }
                    }
                    int size;
                    size = minScores.size();
                    if ((size < NUMKAN) || (ns < ((Integer) minScores.firstElement()).intValue())) {
                        if (size == 0) {
                            minScores.addElement(new Integer(ns));
                            minChars.addElement(new String(curk));
                        } else {
                            if (ns <= ((Integer) minScores.lastElement()).intValue()) {
                                minScores.addElement(new Integer(ns));
                                minChars.addElement(new String(curk));
                            } else {
                                int i = 0;
                                while (((Integer) minScores.elementAt(i)).intValue() > ns) i++;
                                minScores.insertElementAt(new Integer(ns), i);
                                minChars.insertElementAt(new String(curk), i);
                            }
                        }
                    }
                    size = minScores.size();
                    if (size > NUMKAN) {
                        minScores.removeElementAt(0);
                        minChars.removeElementAt(0);
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    static final int angScale = 1000;

    static final int sCost = (int) Math.round(Math.PI / 60.0 * angScale);

    static final int hugeCost = ((int) Math.round(Math.PI * angScale) + sCost) * 100;

    int scoreStroke(Vector xv, Vector yv, int begi, int endi, String dir, int depth) {
        if (dir.length() == 1) {
            int i;
            int difx, dify;
            difx = ((Integer) xv.elementAt(endi - 1)).intValue() - ((Integer) xv.elementAt(begi)).intValue();
            dify = ((Integer) yv.elementAt(endi - 1)).intValue() - ((Integer) yv.elementAt(begi)).intValue();
            if ((difx == 0) && (dify == 0)) {
                return (hugeCost);
            }
            if ((difx * difx + dify * dify > (20 * 20)) && (endi - begi > 5) && (depth < 4)) {
                int mi = (endi + begi) / 2;
                int cost1, cost2;
                cost1 = scoreStroke(xv, yv, begi, mi, dir, depth + 1);
                cost2 = scoreStroke(xv, yv, mi, endi, dir, depth + 1);
                return ((cost1 + cost2) / 2);
            }
            double ang;
            ang = Math.atan2(-dify, difx);
            double myang;
            switch(dir.charAt(0)) {
                case '6':
                    myang = 0;
                    break;
                case '9':
                    myang = Math.PI / 4;
                    break;
                case '8':
                    myang = Math.PI / 2;
                    break;
                case '7':
                    myang = Math.PI * 3 / 4;
                    break;
                case '4':
                    myang = Math.PI;
                    break;
                case '3':
                    myang = -Math.PI / 4;
                    break;
                case '2':
                    myang = -Math.PI / 2;
                    break;
                case '1':
                    myang = -Math.PI * 3 / 4;
                    break;
                default:
                    System.out.println("Illegal char");
                    myang = 0;
            }
            double difang = myang - ang;
            while (difang < 0) difang += 2 * Math.PI;
            while (difang > 2 * Math.PI) difang -= 2 * Math.PI;
            if (difang > Math.PI) difang = 2 * Math.PI - difang;
            int retcost = (int) Math.round(difang * angScale) + sCost;
            return (retcost);
        } else if (begi == endi) {
            return (hugeCost * dir.length());
        } else {
            int l1, l2;
            l1 = dir.length() / 2;
            l2 = dir.length() - l1;
            String s1, s2;
            s1 = dir.substring(0, l1);
            s2 = dir.substring(l1, dir.length());
            int i;
            int mincost = hugeCost * dir.length() * 2;
            int s1l = s1.length();
            int s2l = s2.length();
            int step = (endi - begi) / 10;
            if (step < 1) step = 1;
            for (i = begi + 1 + s1l; i < endi - 1 - s2l; i += step) {
                int ncost;
                ncost = scoreStroke(xv, yv, begi, i + 1, s1, depth) + scoreStroke(xv, yv, i - 1, endi, s2, depth);
                if (ncost < mincost) mincost = ncost;
            }
            return (mincost);
        }
    }

    public int getScore(String s, int cutoff) {
        double score = 0;
        int strokes = 0;
        int maxscore = 0;
        cutoff = cutoff * xstrokes.size();
        StringTokenizer st = new StringTokenizer(s);
        Iterator xe = xstrokes.iterator();
        Iterator ye = ystrokes.iterator();
        while (st.hasMoreTokens()) if (!xe.hasNext()) return (99997); else {
            Vector vxe = (Vector) xe.next();
            Vector vye = (Vector) ye.next();
            int thisscore;
            thisscore = scoreStroke(vxe, vye, 0, vxe.size(), st.nextToken(), 0);
            score = score + thisscore * thisscore;
            maxscore = Math.max(maxscore, thisscore);
            strokes++;
        }
        if (xe.hasNext()) return (99998); else {
            if (strokes == 0) return (99997); else return ((int) Math.round(Math.sqrt(score)));
        }
    }

    private static class KCanvas extends Canvas implements MouseMotionListener, MouseListener {

        DrawPanel paps;

        int lastx, lasty;

        static Color bg = new Color(30, 30, 50);

        static Color fg1 = new Color(235, 255, 235);

        static Color fg2 = new Color(160, 160, 255);

        public KCanvas(DrawPanel paps) {
            this.paps = paps;
            addMouseMotionListener(this);
            addMouseListener(this);
            setBackground(bg);
            setForeground(fg1);
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            if (paps.curxvec != null) {
                Graphics g = getGraphics();
                g.setColor(fg2);
                drawVec(g, paps.curxvec.elements(), paps.curyvec.elements());
            }
            paps.curxvec = new Vector();
            paps.curyvec = new Vector();
            paps.xstrokes.addElement(paps.curxvec);
            paps.ystrokes.addElement(paps.curyvec);
            lastx = e.getX();
            lasty = e.getY();
            paps.curxvec.addElement(new Integer(lastx));
            paps.curyvec.addElement(new Integer(lasty));
        }

        public void mouseReleased(MouseEvent e) {
            int this_score;
            int best_score = 99999999;
            String best_stroke_match = "stroke has not been assigned, ERROR (mouseReleased function)";
            Iterator possible_chinese_strokes_enum = paps.possible_chinese_strokes.iterator();
            Vector<Stroke> scores = new Vector();
            while (possible_chinese_strokes_enum.hasNext()) {
                String current_stroke = possible_chinese_strokes_enum.next().toString();
                this_score = paps.getScore(current_stroke, 999999);
                scores.add(new Stroke(Integer.parseInt(current_stroke), this_score));
                if (this_score < best_score) {
                    best_score = this_score;
                    best_stroke_match = current_stroke;
                }
            }
            paps.current_character_strokes += best_stroke_match + " ";
            DrawPanel.currentStrokes.add(scores);
            paps.xstrokes.removeAllElements();
            paps.ystrokes.removeAllElements();
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            int x, y;
            x = e.getX();
            y = e.getY();
            paps.curxvec.addElement(new Integer(x));
            paps.curyvec.addElement(new Integer(y));
            getGraphics().setColor(fg1);
            brushLine(getGraphics(), lastx, lasty, x, y);
            lastx = x;
            lasty = y;
        }

        public void brushLine(Graphics g, int x1, int y1, int x2, int y2) {
            g.drawLine(x1, y1, x2, y2);
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void drawVec(Graphics g, Enumeration xe2, Enumeration ye2) {
            int lastx, lasty;
            lastx = -1;
            lasty = -1;
            while (xe2.hasMoreElements()) {
                int x, y;
                x = ((Integer) xe2.nextElement()).intValue();
                y = ((Integer) ye2.nextElement()).intValue();
                if (lastx != -1) brushLine(g, lastx, lasty, x, y);
                lastx = x;
                lasty = y;
            }
        }

        public void paint(Graphics g) {
            g.setColor(fg1);
            Rectangle r = getBounds();
            g.draw3DRect(0, 0, r.width, r.height, true);
            Enumeration xe = paps.xstrokes.elements();
            Enumeration ye = paps.ystrokes.elements();
            while (xe.hasMoreElements()) {
                Vector xvec, yvec;
                xvec = (Vector) xe.nextElement();
                yvec = (Vector) ye.nextElement();
                Enumeration xe2 = xvec.elements();
                Enumeration ye2 = yvec.elements();
                if (xvec != paps.curxvec) g.setColor(fg2); else g.setColor(fg1);
                drawVec(g, xe2, ye2);
            }
        }
    }

    public DrawPanel() {
        super();
        setLayout(new BorderLayout());
        add(c, "Center");
        c.setSize(209, 198);
        c.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        populatePossibleStrokes(possible_chinese_strokes);
        currentStrokes = new Vector();
    }

    public DrawPanel(Unistrok unistrok) {
    }

    public static void main(String[] args) {
        Frame f = new Frame("Test");
        DrawPanel dp = new DrawPanel();
        f.add(dp, "Center");
        Panel lower = new Panel();
        f.add(lower, "South");
        Button testb = new Button("Lookup");
        lower.add(testb);
        testb.addActionListener(dp);
        testb.setActionCommand("Lookup");
        Button testb2 = new Button("Clear");
        lower.add(testb2);
        testb2.addActionListener(dp);
        testb2.setActionCommand("Clear");
        f.pack();
    }

    private void populatePossibleStrokes(Vector<Integer> possible_chinese_strokes) {
        possible_chinese_strokes.add(new Integer(1));
        possible_chinese_strokes.add(new Integer(6));
        possible_chinese_strokes.add(new Integer(2));
        possible_chinese_strokes.add(new Integer(21));
        possible_chinese_strokes.add(new Integer(3));
        possible_chinese_strokes.add(new Integer(9));
        possible_chinese_strokes.add(new Integer(62));
        possible_chinese_strokes.add(new Integer(626));
        possible_chinese_strokes.add(new Integer(92));
        possible_chinese_strokes.add(new Integer(623));
        possible_chinese_strokes.add(new Integer(6132));
        possible_chinese_strokes.add(new Integer(6262));
        possible_chinese_strokes.add(new Integer(6161));
        possible_chinese_strokes.add(new Integer(621));
        possible_chinese_strokes.add(new Integer(6131));
        possible_chinese_strokes.add(new Integer(61));
        possible_chinese_strokes.add(new Integer(321));
        possible_chinese_strokes.add(new Integer(2));
        possible_chinese_strokes.add(new Integer(26));
        possible_chinese_strokes.add(new Integer(262));
        possible_chinese_strokes.add(new Integer(261));
        possible_chinese_strokes.add(new Integer(2621));
        possible_chinese_strokes.add(new Integer(16));
        possible_chinese_strokes.add(new Integer(13));
        possible_chinese_strokes.add(new Integer(629));
        possible_chinese_strokes.add(new Integer(627));
        possible_chinese_strokes.add(new Integer(6134));
        possible_chinese_strokes.add(new Integer(61));
        possible_chinese_strokes.add(new Integer(34));
        possible_chinese_strokes.add(new Integer(7));
        possible_chinese_strokes.add(new Integer(23));
        possible_chinese_strokes.add(new Integer(4));
        for (int i = 1; i < 99999; i++) {
            Integer number = new Integer(i);
            if (!number.toString().contains("5") && !number.toString().contains("0")) {
            }
        }
    }
}
