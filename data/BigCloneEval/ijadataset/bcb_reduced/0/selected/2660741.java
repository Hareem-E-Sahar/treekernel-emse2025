package atp;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import javax.swing.JComponent;

public class sHotEqn {

    JComponent PC;

    private int width;

    private int height;

    private String _fldcase;

    private String _fldint;

    ActionListener G;

    private final b S;

    private e D;

    private Font d;

    private Font b;

    private Font _fldnull;

    private Font _fldlong;

    private final int j[] = { 14, 12, 10, 8 };

    private final int _fldfor[] = { 3, 3, 3, 3 };

    private Image s;

    private boolean Q;

    private int f;

    private int H;

    private final boolean K;

    private int g;

    private String z;

    private String E;

    private final f _flddo;

    private final MediaTracker B;

    private final Hashtable<String, Image> w;

    public boolean r;

    public boolean L;

    public boolean q;

    private boolean A;

    private boolean t;

    private int _fldelse;

    private int _fldchar;

    private int J;

    private int I;

    private int a;

    private int m;

    private int R;

    private int l;

    private int F;

    private boolean c;

    private int p;

    private int o;

    public sHotEqn(final JComponent PC) {
        width = 0;
        height = 0;
        _fldcase = null;
        _fldint = "Helvetica";
        Q = false;
        f = 0;
        H = 0;
        K = false;
        g = 0;
        z = "left";
        E = "top";
        w = new Hashtable<String, Image>(13);
        r = false;
        L = false;
        q = true;
        A = false;
        t = true;
        _fldelse = 0;
        _fldchar = 0;
        J = 0;
        I = 0;
        a = 0;
        m = 0;
        R = 0;
        l = 0;
        F = 5;
        c = false;
        p = 0;
        o = 0;
        _flddo = new f();
        B = new MediaTracker(PC);
        S = new b("");
    }

    public void setEquation(final String s1) {
        _fldcase = s1;
        S.a(s1);
        Q = false;
    }

    public String getEquation() {
        return _fldcase;
    }

    public String Status;

    public void printStatus(final String s1) {
        Status = s1;
    }

    private void a(final String s1) {
        Status = s1;
    }

    public Image getImage() {
        if (Q) {
            return s;
        } else {
            return null;
        }
    }

    public void setDebug(final boolean flag) {
        q = flag;
    }

    public boolean isDebug() {
        return q;
    }

    public void setFontname(final String s1) {
        _fldint = s1;
    }

    public String getFontname() {
        return _fldint;
    }

    int i10, j10, k10, l10;

    boolean bold0, fonts = false;

    public void setHAlign(final String s1) {
        z = s1;
        Q = false;
    }

    public String getHAlign() {
        return z;
    }

    public void setVAlign(final String s1) {
        E = s1;
        Q = false;
    }

    public String getVAlign() {
        return E;
    }

    public void setEditable(final boolean flag) {
        t = flag;
    }

    public boolean isEditable() {
        return t;
    }

    public String getSelectedArea() {
        return S.a(p, o);
    }

    public Dimension getPreferredSize() {
        if ((width == 0) & (height == 0)) {
            final Graphics g1 = PC.getGraphics();
            if (g1 != null) {
                g1.setFont(d);
                S._mthdo();
                final c c1 = _mthnew(0, 150, false, g1, 1);
                if (K) {
                    g = 5;
                } else {
                    g = 0;
                }
                f = 1 + c1._flddo + 2 * g;
                H = 1 + c1._fldif + c1.a + 2 * g;
            }
        }
        width = f;
        height = H;
        if (f <= 1) {
            return new Dimension(100, 100);
        } else {
            return new Dimension(f, H);
        }
    }

    final int ff1 = 8, ff2 = 6;

    public Dimension getSizeof(final String s1, final Graphics g1) {
        if (d != g1.getFont()) {
            d = g1.getFont();
            final int fs = d.getSize();
            b = new Font(d.getFamily(), d.getStyle(), fs * ff1 / 10);
            _fldnull = _fldlong = new Font(d.getFamily(), d.getStyle(), fs * ff2 / 10);
        }
        S.a(s1);
        final c c1 = _mthnew(0, 150, false, g1, 1);
        byte byte0;
        if (K) {
            byte0 = 5;
        } else {
            byte0 = 0;
        }
        return new Dimension(1 + c1._flddo + 2 * byte0, 1 + c1._fldif + c1.a + 2 * byte0);
    }

    public int getAscent(final String s1, final Graphics g1) {
        if (d != g1.getFont()) {
            d = g1.getFont();
            final int fs = d.getSize();
            b = new Font(d.getFamily(), d.getStyle(), fs * ff1 / 10);
            _fldnull = _fldlong = new Font(d.getFamily(), d.getStyle(), fs * ff2 / 10);
        }
        S.a(s1);
        final c c1 = _mthnew(0, 150, false, g1, 1);
        byte byte0;
        if (K) {
            byte0 = 5;
        } else {
            byte0 = 0;
        }
        return c1._fldif + byte0;
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public synchronized int paint(final int cc, final int rr, final Graphics g2) {
        if (d != g2.getFont()) {
            d = g2.getFont();
            final int fs = d.getSize();
            b = new Font(d.getFamily(), d.getStyle(), fs * ff1 / 10);
            _fldnull = _fldlong = new Font(d.getFamily(), d.getStyle(), fs * ff2 / 10);
        }
        c c1 = new c();
        g2.setFont(d);
        g = 0;
        S._mthdo();
        c1 = _mthnew(cc, rr, false, g2, 1);
        final int hh = c1._fldif;
        c1 = new c();
        S._mthdo();
        c1 = _mthnew(cc, rr + hh, true, g2, 1);
        a(" ");
        if (z.equals("center")) {
        } else if (z.equals("right")) {
        }
        if (E.equals("middle")) {
        } else if (E.equals("bottom")) {
        }
        f = 1 + c1._flddo + 2 * g;
        H = 1 + c1._fldif + c1.a + 2 * g;
        if (f > width) {
        }
        if (H > height) {
        }
        Q = true;
        notify();
        return c1._fldif + c1.a;
    }

    private c _mthnew(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1) {
        return _mthtry(i1, j1, flag, g1, k1, true);
    }

    private c _mthtry(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1, final boolean flag1) {
        c c1 = new c();
        final c c2 = new c();
        boolean flag2 = true;
        boolean flag5 = false;
        int l1 = 0;
        while (!S._mthnew() && flag2) {
            D = S._mthint();
            if (A && flag) {
                l1 = S._mthif();
            }
            boolean flag4 = false;
            final int i2 = l1;
            final int j2 = D.y;
            switch(D.y) {
                case 4:
                case 7:
                case 8:
                case 17:
                case 51:
                    if (c && flag) {
                        if (l1 > o) {
                            o = l1;
                        }
                        if (l1 < p) {
                            p = l1;
                        }
                    }
                    return c2;
                case 22:
                    c1 = _mthbyte(i1 + c2._flddo, j1, flag, g1, k1);
                    break;
                case 5:
                    c1 = a(i1 + c2._flddo, j1, flag, g1);
                    break;
                case 15:
                    if (c && flag) {
                        flag5 = true;
                    }
                    c1 = _mthif(i1 + c2._flddo, j1, flag, g1, k1);
                    break;
                case 50:
                    if (c && flag) {
                        flag5 = true;
                    }
                    c1 = _mthgoto(i1 + c2._flddo, j1, flag, g1, k1);
                    break;
                case 3:
                    c1 = _mthtry(i1 + c2._flddo, j1, flag, g1, k1, true);
                    break;
                case 123:
                    if (c && flag) {
                        flag5 = true;
                    }
                    c1 = _mthelse(i1 + c2._flddo, j1, flag, g1, k1);
                    break;
                case 1:
                    c1 = _mthnew(i1 + c2._flddo, j1, flag, g1);
                    break;
                case 110:
                    c1 = _mthtry(i1 + c2._flddo, j1, flag, g1, k1);
                    break;
                case 108:
                    c1 = _mthint(i1 + c2._flddo, j1, flag, g1);
                    break;
                case 12:
                    c1 = _mthfor(i1 + c2._flddo, j1, flag, g1, k1, true);
                    break;
                case 1001:
                    c1 = _mthfor(i1 + c2._flddo, j1, flag, g1, k1 - 1, true);
                    break;
                case 115:
                    c1 = _mthfor(i1 + c2._flddo, j1, flag, g1, k1, false);
                    break;
                case 2:
                case 9:
                    c1 = _mthif(i1 + c2._flddo, j1, flag, g1);
                    break;
                case 18:
                    c1 = _mthif(i1 + c2._flddo, j1, flag, g1, k1, false);
                    break;
                case 19:
                    c1 = _mthif(i1 + c2._flddo, j1, flag, g1, k1, true);
                    break;
                case 16:
                    if (c && flag) {
                        flag5 = true;
                    }
                    c1 = _mthnull(i1 + c2._flddo, j1, flag, g1, k1);
                    break;
                case 24:
                    c1 = _mthfor(i1 + c2._flddo, j1, flag, g1, k1);
                    break;
                case 124:
                    c1 = _mthdo(i1 + c2._flddo, j1, flag, g1);
                    break;
                case 118:
                    c1 = a(i1 + c2._flddo, j1, flag, g1, k1);
                    break;
                case 119:
                    c1 = _mthlong(i1 + c2._flddo, j1, flag, g1, k1);
                    break;
                case 116:
                    c1 = a(i1 + c2._flddo, j1, flag, g1, k1, true);
                    break;
                case 117:
                    c1 = a(i1 + c2._flddo, j1, flag, g1, k1, false);
                    break;
                case 109:
                    c1 = _mthtry(i1 + c2._flddo, j1, flag, g1);
                    break;
                case 113:
                    c1 = _mthfor(i1 + c2._flddo, j1, flag, g1);
                    break;
                case 13:
                    if (c && flag) {
                        flag5 = true;
                    }
                    c1 = _mthcase(i1 + c2._flddo, j1, flag, g1, k1);
                    break;
                case 120:
                    c1 = _mthchar(i1 + c2._flddo, j1, flag, g1, k1);
                    break;
                case 10:
                    c1 = _mthint(i1 + c2._flddo, j1, flag, g1, k1, true);
                    break;
                case 11:
                    c1 = _mthdo(i1 + c2._flddo, j1, flag, g1, k1, true);
                    break;
                case 20:
                    c1 = _mthdo(i1 + c2._flddo, j1, flag, g1, k1);
                    break;
                case 14:
                    c1 = _mthint(i1 + c2._flddo, j1, flag, g1, k1);
                    break;
                case 25:
                    c1 = new c(0, 0, 0);
                    flag4 = true;
                    break;
                case 99:
                case 100:
                    c1 = new c(0, 0, 0);
                    break;
                case 6:
                case 21:
                case 23:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 913:
                case 914:
                case 915:
                case 916:
                case 917:
                case 918:
                case 919:
                case 920:
                case 921:
                case 922:
                case 923:
                case 924:
                case 925:
                case 926:
                case 927:
                case 928:
                case 929:
                case 930:
                case 931:
                case 932:
                case 933:
                case 934:
                case 935:
                case 936:
                case 937:
                case 945:
                case 946:
                case 947:
                case 948:
                case 949:
                case 950:
                case 951:
                case 952:
                case 953:
                case 954:
                case 955:
                case 956:
                case 957:
                case 958:
                case 959:
                case 960:
                case 961:
                case 962:
                case 963:
                case 964:
                case 965:
                case 966:
                case 967:
                case 968:
                case 969:
                case 65:
                case 66:
                case 67:
                case 68:
                case 69:
                case 70:
                case 71:
                case 72:
                case 73:
                case 74:
                case 75:
                case 76:
                case 77:
                case 78:
                case 79:
                case 80:
                case 81:
                case 82:
                case 83:
                case 84:
                case 85:
                case 86:
                case 87:
                case 88:
                case 89:
                case 90:
                case 91:
                case 92:
                case 93:
                case 94:
                case 95:
                case 96:
                case 97:
                case 98:
                case 101:
                case 102:
                case 103:
                case 104:
                case 105:
                case 106:
                case 107:
                case 111:
                case 112:
                case 114:
                default:
                    printStatus("Parser: unknown token: " + D.y + " " + D.w);
                    break;
            }
            if (flag) {
                if (A) {
                    if (!c && i1 + c2._flddo <= _fldelse && _fldelse <= i1 + c2._flddo + c1._flddo && j1 - c1._fldif <= _fldchar && _fldchar <= j1 + c1.a) {
                        m = l = _fldelse;
                        a = R = _fldchar;
                        c = true;
                        p = l1;
                        o = l1;
                    }
                    if (!c && i1 + c2._flddo <= J && J <= i1 + c2._flddo + c1._flddo && j1 - c1._fldif <= I && I <= j1 + c1.a) {
                        m = l = J;
                        a = R = I;
                        c = true;
                        p = l1;
                        o = l1;
                        final int k2 = J;
                        final int l2 = I;
                        J = _fldelse;
                        I = _fldchar;
                        _fldelse = k2;
                        _fldchar = l2;
                    }
                    if (c) {
                        m = Math.min(m, i1 + c2._flddo);
                        l = Math.max(l, i1 + c2._flddo + c1._flddo);
                        a = Math.min(a, j1 - c1._fldif);
                        R = Math.max(R, j1 + c1.a);
                        if (F > k1) {
                            F = k1;
                        }
                        switch(j2) {
                            case 13:
                            case 15:
                            case 16:
                            case 50:
                            case 123:
                            case 124:
                                flag5 = true;
                                if (i2 > o) {
                                    o = i2;
                                }
                                if (i2 < p) {
                                    p = i2;
                                }
                                l1 = S._mthif();
                                break;
                        }
                        if (l1 > o) {
                            o = l1;
                        }
                        if (l1 < p) {
                            p = l1;
                        }
                        if (i1 + c2._flddo <= J && J <= i1 + c2._flddo + c1._flddo && j1 - c1._fldif <= I && I <= j1 + c1.a && F == k1) {
                            A = false;
                            c = false;
                        }
                    }
                }
                if (flag5) {
                    m = Math.min(m, i1 + c2._flddo);
                    l = Math.max(l, i1 + c2._flddo + c1._flddo);
                    a = Math.min(a, j1 - c1._fldif);
                    R = Math.max(R, j1 + c1.a);
                    switch(j2) {
                        case 13:
                        case 15:
                        case 16:
                        case 50:
                        case 123:
                        case 124:
                            if (i2 > o) {
                                o = i2;
                            }
                            if (i2 < p) {
                                p = i2;
                            }
                            l1 = S._mthif();
                            break;
                    }
                    if (l1 > o) {
                        o = l1;
                    }
                    if (l1 < p) {
                        p = l1;
                    }
                    flag5 = false;
                }
            }
            c2._flddo += c1._flddo;
            c2._fldif = Math.max(c2._fldif, c1._fldif);
            c2.a = Math.max(c2.a, c1.a);
            if (!flag1 && !flag4) {
                flag2 = false;
            }
        }
        return c2;
    }

    private c _mthbyte(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1) {
        c c1 = new c();
        int l1 = 0;
        final FontMetrics fontmetrics = g1.getFontMetrics();
        final String s1 = D.w;
        if (flag) {
            l1 = S._mthif();
        }
        c1 = _mthtry(i1, j1, false, g1, k1, false);
        final int i2 = Math.max(c1._flddo, fontmetrics.stringWidth(s1));
        final int j2 = c1._fldif + fontmetrics.getAscent() / 2;
        final int k2 = c1.a;
        if (flag) {
            S.a(l1);
            _mthtry(i1, j1, true, g1, k1, false);
            final int l2 = 3 * ((i2 - fontmetrics.stringWidth(s1)) / 4);
            if (s1.equals(".") | s1.equals("..")) {
                g1.drawString(s1, i1 + l2, j1 - fontmetrics.getAscent());
            } else if (s1.equals("\264") | s1.equals("`")) {
                g1.drawString(s1, i1 + l2, j1 - fontmetrics.getAscent() / 3);
            } else {
                g1.drawString(s1, i1 + l2, j1 - (fontmetrics.getAscent() * 2) / 3);
            }
        }
        return new c(i2, j2, k2);
    }

    private c a(final int i1, final int j1, final boolean flag, final Graphics g1) {
        new c();
        final FontMetrics fontmetrics = g1.getFontMetrics();
        final int k1 = g1.getFont().getSize() / 2;
        final int l1 = fontmetrics.getHeight() - fontmetrics.getDescent();
        final int i2 = fontmetrics.getDescent();
        if (flag) {
            final int j2 = (j1 - l1) + 1;
            final int k2 = (j1 + i2) - 1;
            final int l2 = (j2 + k2) / 2;
            if (D.w.equals("<")) {
                g1.drawLine(i1 + k1, j2, i1, l2);
                g1.drawLine(i1, l2, i1 + k1, k2);
            } else {
                g1.drawLine(i1, j2, i1 + k1, l2);
                g1.drawLine(i1 + k1, l2, i1, k2);
            }
        }
        return new c(k1, l1, i2);
    }

    private c _mthif(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1) {
        int l2 = 0;
        final int ai[] = new int[100];
        final int ai1[] = new int[100];
        final int ai2[] = new int[100];
        new c();
        int i3 = 0;
        final FontMetrics fontmetrics = g1.getFontMetrics();
        final int j3 = g1.getFont().getSize();
        if (flag) {
            i3 = S._mthif();
        }
        if (!a(3, "ARRAY: BeginSym")) {
            return new c(0, 0, 0);
        }
        for (int k3 = 0; k3 < 99; k3++) {
            int i2 = 0;
            int k2 = 0;
            for (int l3 = 0; l3 < 99; l3++) {
                final c c2 = _mthnew(i1, j1, false, g1, k1);
                i2 = Math.max(i2, c2._fldif);
                k2 = Math.max(k2, c2.a);
                ai[l3] = Math.max(ai[l3], c2._flddo + j3);
                if (D.y == 8 || D.y == 4) {
                    break;
                }
            }
            ai1[k3] = Math.max(ai1[k3], i2);
            ai2[k3] = Math.max(ai2[k3], k2);
            l2 += i2 + k2;
            if (D.y == 4) {
                break;
            }
        }
        int i4 = 0;
        for (int j4 = 0; j4 < 99; j4++) {
            i4 += ai[j4];
        }
        if (flag) {
            S.a(i3);
            a(3, "ARRAY: Begin");
            int j2 = 0;
            for (int k4 = 0; k4 < 99; k4++) {
                int l1 = 0;
                if (k4 == 0) {
                    j2 = ai1[k4];
                } else {
                    j2 += ai2[k4 - 1] + ai1[k4];
                }
                for (int l4 = 0; l4 < 99; l4++) {
                    _mthnew(i1 + l1, (j1 - l2 / 2 - fontmetrics.getDescent()) + j2, true, g1, k1);
                    l1 += ai[l4];
                    if (D.y == 8 || D.y == 4) {
                        break;
                    }
                }
                if (D.y == 4) {
                    break;
                }
            }
        }
        return new c(i4 - j3, l2 / 2 + fontmetrics.getDescent(), l2 / 2 - fontmetrics.getDescent());
    }

    private c _mthgoto(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1) {
        int i2 = 0;
        int j3 = 0;
        final int ai[] = new int[100];
        final int ai1[] = new int[100];
        final int ai2[] = new int[100];
        final int ai3[] = new int[100];
        final int ai4[] = new int[100];
        int k3 = 0;
        int l3 = 0;
        int i4 = 0;
        new c();
        int j4 = 0;
        final FontMetrics fontmetrics = g1.getFontMetrics();
        final int k5 = g1.getFont().getSize();
        int l5 = 0;
        boolean flag3 = false;
        boolean flag4 = true;
        boolean flag5 = true;
        if (!a(3)) {
            return new c(0, 0, 0);
        }
        if (S._mthint().w.equals("eqnarray")) {
            flag5 = false;
        }
        if (!a(4, "BEGIN: EndSym")) {
            return new c(0, 0, 0);
        }
        if (flag5) {
            j4 = S._mthif();
            if (!a(3)) {
                flag4 = false;
                S.a(j4);
            }
        }
        if (flag5 && flag4) {
            new e();
            for (e e2 = S._mthint(); e2.y != 4; e2 = S._mthint()) {
                final StringBuffer stringbuffer = new StringBuffer(e2.w);
                for (int i7 = 0; i7 < stringbuffer.length(); i7++) {
                    switch(stringbuffer.charAt(i7)) {
                        case 108:
                            ai3[l5] = 1;
                            if (l5 < 99) {
                                l5++;
                            }
                            break;
                        case 99:
                            ai3[l5] = 2;
                            if (l5 < 99) {
                                l5++;
                            }
                            break;
                        case 114:
                            ai3[l5] = 3;
                            if (l5 < 99) {
                                l5++;
                            }
                            break;
                        case 64:
                            ai3[l5] = 4;
                            ai4[l5] = S._mthif();
                            final c c2 = _mthtry(i1, j1, false, g1, k1, false);
                            k3 += c2._flddo;
                            l3 = Math.max(l3, c2._fldif);
                            i4 = Math.max(i4, c2.a);
                            if (l5 < 99) {
                                l5++;
                            }
                            break;
                        case 42:
                            a(3, "Begin *{");
                            int j6;
                            try {
                                j6 = Integer.parseInt(S._mthint().w);
                            } catch (final NumberFormatException numberformatexception) {
                                j6 = 0;
                            }
                            a(4, 3, "Begin }{");
                            final int l7 = S._mthif();
                            for (int j8 = 0; j8 < j6; j8++) {
                                S.a(l7);
                                for (e2 = S._mthint(); e2.y != 4; e2 = S._mthint()) {
                                    final StringBuffer stringbuffer1 = new StringBuffer(e2.w);
                                    for (int l8 = 0; l8 < stringbuffer1.length(); l8++) {
                                        switch(stringbuffer1.charAt(l8)) {
                                            case 108:
                                                ai3[l5] = 1;
                                                if (l5 < 99) {
                                                    l5++;
                                                }
                                                break;
                                            case 99:
                                                ai3[l5] = 2;
                                                if (l5 < 99) {
                                                    l5++;
                                                }
                                                break;
                                            case 114:
                                                ai3[l5] = 3;
                                                if (l5 < 99) {
                                                    l5++;
                                                }
                                                break;
                                            case 64:
                                                ai3[l5] = 4;
                                                ai4[l5] = S._mthif();
                                                final c c3 = _mthtry(i1, j1, false, g1, k1, false);
                                                k3 += c3._flddo;
                                                l3 = Math.max(l3, c3._fldif);
                                                i4 = Math.max(i4, c3.a);
                                                if (l5 < 99) {
                                                    l5++;
                                                }
                                                break;
                                            default:
                                                printStatus("P: begin: illegal format 2");
                                                break;
                                        }
                                    }
                                }
                            }
                            break;
                        default:
                            printStatus("P: begin: illegal format 1");
                            break;
                    }
                }
            }
        }
        if (!flag5) {
            ai3[0] = 3;
            ai3[1] = 2;
            ai3[2] = 1;
            l5 = 3;
        }
        for (int k6 = 0; k6 < l5 - 1; k6++) {
            if (ai3[k6] != 4 && ai3[k6 + 1] != 4) {
                i2 += k5 / 2;
            }
        }
        if (flag) {
            j4 = S._mthif();
        }
        for (int l6 = 0; l6 < 99; l6++) {
            int j2 = 0;
            int l2 = 0;
            for (int j7 = 0; j7 < 99; j7++) {
                final c c4 = _mthnew(i1, j1, false, g1, k1);
                j2 = Math.max(j2, c4._fldif);
                l2 = Math.max(l2, c4.a);
                ai[j7] = Math.max(ai[j7], c4._flddo);
                if (D.y == 8 || D.y == 51) {
                    break;
                }
            }
            j2 = Math.max(j2, l3);
            l2 = Math.max(l2, i4);
            ai1[l6] = j2;
            ai2[l6] = l2;
            j3 += j2 + l2;
            if (D.y == 51) {
                break;
            }
        }
        for (int i6 = 0; i6 < 99; i6++) {
            i2 += ai[i6];
        }
        i2 += (2 * k5) / 2;
        if (flag) {
            S.a(j4);
            int k2 = 0;
            final int i3 = j3 / 2 + fontmetrics.getDescent();
            for (int k7 = 0; k7 < 99; k7++) {
                int l1 = k5 / 2;
                if (k7 == 0) {
                    k2 = ai1[k7];
                } else {
                    k2 += ai2[k7 - 1] + ai1[k7];
                }
                int i8 = 0;
                for (int k8 = 0; k8 < 99; k8++) {
                    while (ai3[i8] == 4) {
                        final int k4 = S._mthif();
                        S.a(ai4[i8]);
                        final c c5 = _mthtry(i1 + l1, (j1 - i3) + k2, true, g1, k1, false);
                        l1 += c5._flddo;
                        S.a(k4);
                        i8++;
                    }
                    switch(ai3[i8]) {
                        case 0:
                        case 1:
                            _mthnew(i1 + l1, (j1 - i3) + k2, true, g1, k1);
                            i8++;
                            break;
                        case 2:
                            final int l4 = S._mthif();
                            c c7 = _mthnew(i1, j1, false, g1, k1);
                            S.a(l4);
                            c7 = _mthnew(i1 + l1 + (ai[k8] - c7._flddo) / 2, (j1 - i3) + k2, true, g1, k1);
                            i8++;
                            break;
                        case 3:
                            final int i5 = S._mthif();
                            c c8 = _mthnew(i1, j1, false, g1, k1);
                            S.a(i5);
                            c8 = _mthnew((i1 + l1 + ai[k8]) - c8._flddo, (j1 - i3) + k2, true, g1, k1);
                            i8++;
                            break;
                    }
                    if (ai3[i8] != 4) {
                        l1 += k5 / 2;
                    }
                    l1 += ai[k8];
                    boolean flag2 = false;
                    flag3 = false;
                    if (D.y == 8) {
                        flag2 = true;
                    } else if (D.y == 51) {
                        flag2 = true;
                        flag3 = true;
                    }
                    for (; ai3[i8] == 4; i8++) {
                        final int j5 = S._mthif();
                        S.a(ai4[i8]);
                        final c c9 = _mthtry(i1 + l1, (j1 - i3) + k2, true, g1, k1, false);
                        l1 += c9._flddo;
                        S.a(j5);
                    }
                    if (flag2) {
                        break;
                    }
                }
                if (flag3) {
                    break;
                }
            }
        }
        if (!a(3, "BEGIN 2: begin")) {
            return new c(0, 0, 0);
        }
        S._mthint();
        if (!a(4, "BEGIN 2: end")) {
            return new c(0, 0, 0);
        } else {
            return new c(i2 + k3, j3 / 2 + fontmetrics.getDescent(), j3 / 2 - fontmetrics.getDescent());
        }
    }

    private c _mthelse(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1) {
        c c1 = new c();
        final int l1 = g1.getFont().getSize() / 2;
        c1 = _mthtry(i1 + l1, j1, flag, g1, k1, false);
        if (flag) {
            g1.drawRect(i1 + l1 / 2, j1 - c1._fldif - l1 / 2, c1._flddo + l1, c1._fldif + c1.a + l1);
        }
        return new c(c1._flddo + l1 + l1, c1._fldif + l1, c1.a + l1);
    }

    private c _mthfor(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1, final boolean flag1) {
        int l1 = 0;
        new c();
        c c3 = new c();
        c c4 = new c();
        int i2 = 0;
        final Font font = g1.getFont();
        font.getSize();
        a(g1, k1 + 1);
        final FontMetrics fontmetrics = g1.getFontMetrics();
        if (flag) {
            i2 = S._mthif();
        }
        c3 = _mthtry(i1, j1, false, g1, k1 + 1, false);
        int k2 = c3._fldif + c3.a;
        c4 = _mthtry(i1, j1, false, g1, k1 + 1, false);
        int l2 = Math.max(c3._flddo, c4._flddo);
        int i3 = c4._fldif + c4.a;
        final Font font1 = g1.getFont();
        final int j3 = (3 * font1.getSize()) / 18;
        l2 += 2 * j3;
        if (fontmetrics.getAscent() < i3) {
            l1 = fontmetrics.getAscent() / 2;
        }
        k2 += 2 + l1;
        i3 += 1 - l1;
        if (flag) {
            S.a(i2);
            if (flag1) {
                g1.drawLine(i1 + j3, j1 - l1, (i1 + l2) - j3, j1 - l1);
            }
            _mthtry(i1 + (l2 - c3._flddo) / 2, j1 - 2 - c3.a - l1, true, g1, k1 + 1, false);
            if (c && k1 < F) {
                F = k1;
            }
            _mthtry(i1 + (l2 - c4._flddo) / 2, (j1 + 1 + c4._fldif) - l1, true, g1, k1 + 1, false);
        }
        a(g1, k1);
        return new c(l2, k2, i3);
    }

    private c _mthnew(final int i1, final int j1, final boolean flag, final Graphics g1) {
        final FontMetrics fontmetrics = g1.getFontMetrics();
        if (flag) {
            g1.drawString(D.w, i1, j1);
        }
        final int k1 = fontmetrics.stringWidth(D.w);
        final int l1 = fontmetrics.getHeight() - fontmetrics.getDescent();
        final int i2 = fontmetrics.getDescent();
        return new c(k1, l1, i2);
    }

    private void a(final Graphics g1, final int i1, final int j1, final int k1, final int l1, final int i2) {
        g1.drawArc(i1 - k1, j1 - k1, 2 * k1, 2 * k1, l1, i2);
    }

    private void a(final Graphics g1, final String s1, final int i1, final int j1, final int k1, final int l1, final int i2, final int j2) {
        final int k2 = j1 / 2;
        final int l2 = i1 + k2;
        final int i3 = i1 + j1;
        final int j3 = i1 + k2 / 2;
        final int k3 = l2 + k2 / 2;
        final int l3 = (k1 + l1) / 2;
        final int i4 = (int) ((double) k2 * 0.86602540378444004D);
        final int j4 = k1 + i4;
        final int k4 = l1 - i4;
        if (s1.equals("[")) {
            g1.drawLine(j3, k1, j3, l1);
            g1.drawLine(j3, l1, k3, l1);
            g1.drawLine(j3, k1, k3, k1);
        } else if (s1.equals("]")) {
            g1.drawLine(k3, k1, k3, l1);
            g1.drawLine(j3, l1, k3, l1);
            g1.drawLine(j3, k1, k3, k1);
        } else if (s1.equals("|")) {
            g1.drawLine(l2, k1, l2, l1);
        } else if (s1.equals("||")) {
            final int l4 = l2 + i2 / 4;
            g1.drawLine(l2, k1, l2, l1);
            g1.drawLine(l4, k1, l4, l1);
        } else if (s1.equals("(")) {
            for (int i5 = j2; i5 < 2 + j2; i5++) {
                final int i6 = j3 + i5;
                a(g1, k3 + i5, j4, k2, 180, -60);
                g1.drawLine(i6, j4, i6, k4);
                a(g1, k3 + i5, k4, k2, 180, 60);
            }
        } else if (s1.equals(")")) {
            for (int j5 = j2; j5 < 2 + j2; j5++) {
                final int j6 = k3 + j5;
                a(g1, j3 + j5, j4, k2, 0, 60);
                g1.drawLine(j6, j4, j6, k4);
                a(g1, j3 + j5, k4, k2, 0, -60);
            }
        } else if (s1.equals("<")) {
            g1.drawLine(j3, l3, k3, k1);
            g1.drawLine(j3, l3, k3, l1);
        } else if (s1.equals(">")) {
            g1.drawLine(k3, l3, j3, k1);
            g1.drawLine(k3, l3, j3, l1);
        } else if (s1.equals("{")) {
            for (int k5 = j2; k5 < 2 + j2; k5++) {
                final int k6 = l2 + k5;
                a(g1, i3 + k5, j4, k2, 180, -60);
                g1.drawLine(k6, j4, k6, l3 - k2);
                a(g1, i1 + k5, l3 - k2, k2, 0, -90);
                a(g1, i1 + k5, l3 + k2, k2, 0, 90);
                g1.drawLine(k6, l3 + k2, k6, k4);
                a(g1, i3 + k5, k4, k2, 180, 60);
            }
        } else if (s1.equals("}")) {
            for (int l5 = j2; l5 < 2 + j2; l5++) {
                final int l6 = l2 + l5;
                a(g1, i1 + l5, j4, k2, 0, 60);
                g1.drawLine(l6, j4, l6, l3 - k2);
                a(g1, i3 + l5, l3 - k2, k2, -180, 90);
                a(g1, i3 + l5, l3 + k2, k2, 180, -90);
                g1.drawLine(l6, l3 + k2, l6, k4);
                a(g1, i1 + l5, k4, k2, 0, -60);
            }
        }
    }

    private c _mthnull(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1) {
        int l1 = 0;
        int i2 = 0;
        c c1 = new c();
        int j2 = 0;
        final Font font = g1.getFont();
        final int k2 = font.getSize();
        final int l2 = (int) (2.0F * (float) k2);
        int i3 = k2 / 9;
        if (flag) {
            j2 = S._mthif();
        }
        final String s1 = S._mthint().w;
        c1 = _mthnew(i1, j1, false, g1, k1);
        final int j3 = c1._flddo;
        int k3 = c1._fldif;
        int l3 = c1.a;
        final int i4 = (j1 - k3) + 1;
        final int j4 = (j1 + l3) - 1;
        final String s3 = S._mthint().w;
        final int k4 = (k3 + l3) - 2;
        final Font font1 = new Font(font.getFamily(), font.getStyle(), k4);
        g1.setFont(font1);
        final FontMetrics fontmetrics = g1.getFontMetrics();
        if (s1.equals("<") || s1.equals(">")) {
            l1 = k2;
        } else if (k4 < l2) {
            l1 = fontmetrics.stringWidth(s1);
            if ("([{)]}".indexOf(s1) >= 0) {
                l1 += i3;
            }
        } else {
            l1 = k2;
        }
        if (s3.equals("<") || s3.equals(">")) {
            i2 = k2;
        } else if (k4 < l2) {
            i2 = fontmetrics.stringWidth(s3);
            if ("([{)]}".indexOf(s3) >= 0) {
                i2 += i3;
            }
        } else {
            i2 = k2;
        }
        g1.setFont(font);
        final int l4 = S._mthif();
        int j5 = 0;
        int k5 = 0;
        if (S._mthint().y == 11) {
            final c c2 = _mthdo(i1, j1, false, g1, k1, false);
            j5 = c2._flddo;
            k5 = (j4 + c2._fldif) - (c2._fldif + c2.a) / 2;
            l3 += (c2._fldif + c2.a) / 2;
        } else {
            S.a(l4);
        }
        final int l5 = S._mthif();
        int j6 = 0;
        int k6 = 0;
        if (S._mthint().y == 10) {
            final c c3 = _mthint(i1, j1, false, g1, k1, false);
            j6 = c3._flddo;
            k6 = (i4 + c3._fldif) - (c3._fldif + c3.a) / 2;
            k3 += (c3._fldif + c3.a) / 2;
        } else {
            S.a(l5);
        }
        j5 = Math.max(j5, j6);
        if (flag) {
            S.a(j2);
            final String s2 = S._mthint().w;
            if (!s2.equals(".")) {
                if (k4 < l2 && !s2.equals("<") && !s2.equals(">")) {
                    g1.setFont(font1);
                    g1.drawString(s2, i1, j4 - fontmetrics.getDescent() - fontmetrics.getLeading() / 2);
                    g1.setFont(font);
                } else {
                    a(g1, s2, i1, l1, i4, j4, k2, 0);
                }
            }
            _mthnew(i1 + l1, j1, true, g1, k1);
            final String s4 = S._mthint().w;
            if (!s4.equals(".")) {
                if (k4 < l2 && !s4.equals("<") && !s4.equals(">")) {
                    g1.setFont(font1);
                    if ("([{)]}".indexOf(s4) < 0) {
                        i3 = 0;
                    }
                    g1.drawString(s4, i1 + j3 + l1 + i3, j4 - fontmetrics.getDescent() - fontmetrics.getLeading() / 2);
                    g1.setFont(font);
                } else {
                    a(g1, s4, i1 + j3 + l1, i2, i4, j4, -k2, -1);
                }
            }
            final int i5 = S._mthif();
            if (a(11)) {
                _mthdo(i1 + j3 + l1 + i2, k5, true, g1, k1, false);
            } else {
                S.a(i5);
            }
            final int i6 = S._mthif();
            if (a(10)) {
                _mthint(i1 + j3 + l1 + i2, k6, true, g1, k1, false);
            } else {
                S.a(i6);
            }
        }
        return new c(j3 + l1 + i2 + j5, k3 + 2, l3 + 2);
    }

    private c _mthfor(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1) {
        int l1 = 0;
        new c();
        int i2 = 0;
        int j2 = 0;
        final FontMetrics fontmetrics = g1.getFontMetrics();
        final String s1 = D.w;
        final int k2 = S._mthif();
        final int l2 = l1 = fontmetrics.stringWidth(s1);
        final int i3 = fontmetrics.getHeight() - fontmetrics.getDescent();
        int j3 = fontmetrics.getDescent();
        if (a(11)) {
            final c c2 = _mthdo(i1, j1, false, g1, k1, false);
            i2 = c2._flddo;
            l1 = Math.max(l1, c2._flddo);
            j2 = c2._fldif;
            j3 = c2._fldif + c2.a;
        } else {
            S.a(k2);
        }
        if (flag) {
            S.a(k2);
            g1.drawString(s1, i1 + (l1 - l2) / 2, j1);
            if (a(11)) {
                _mthdo(i1 + (l1 - i2) / 2, j1 + j2, true, g1, k1, false);
            } else {
                S.a(k2);
            }
        }
        return new c(l1, i3, j3);
    }

    private c _mthdo(final int i1, final int j1, final boolean flag, final Graphics g1) {
        int k1 = 0;
        int l1 = 0;
        int i2 = 0;
        new c();
        if (!a(3)) {
            return new c(0, 0, 0);
        }
        while (!S._mthnew()) {
            D = S._mthint();
            if (D.y == 4) {
                break;
            }
            final c c2 = _mthif(i1 + k1, j1, flag, g1);
            k1 += c2._flddo;
            l1 = Math.max(l1, c2._fldif);
            i2 = Math.max(i2, c2.a);
        }
        return new c(k1, l1, i2);
    }

    private c _mthtry(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1) {
        c c1 = new c();
        c1 = _mthtry(i1, j1, flag, g1, k1, false);
        if (flag) {
            g1.drawLine(i1 + c1._flddo / 4, j1 + c1.a, i1 + (c1._flddo * 3) / 4, j1 - c1._fldif);
        }
        return c1;
    }

    private c _mthint(final int i1, final int j1, final boolean flag, final Graphics g1) {
        final FontMetrics fontmetrics = g1.getFontMetrics();
        if (flag) {
            g1.drawString(D.w, i1 + 1, j1);
        }
        return new c(fontmetrics.stringWidth(D.w) + 2, fontmetrics.getHeight() - fontmetrics.getDescent(), fontmetrics.getDescent());
    }

    private c a(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1) {
        int l1 = 0;
        c c1 = new c();
        final int i2 = g1.getFont().getSize() / 4;
        final int j2 = i2 / 2;
        int k2 = 0;
        int l2 = 0;
        int i3 = 0;
        if (flag) {
            l1 = S._mthif();
        }
        c1 = _mthtry(i1, j1, false, g1, k1, false);
        int j3 = c1._flddo;
        final int k3 = j3 / 2;
        int l3 = k3;
        int i4 = c1._fldif;
        final int j4 = c1.a;
        final int k4 = S._mthif();
        if (a(10)) {
            final c c2 = _mthint(i1, j1, false, g1, k1, false);
            k2 = c2._flddo;
            l3 = Math.max(l3, k2 / 2);
            l2 = i4 + c2.a;
            i3 = c2._fldif + c2.a;
        } else {
            S.a(k4);
        }
        if (flag) {
            S.a(l1);
            final int i5 = (i1 + l3) - k3;
            _mthtry(i5, j1, true, g1, k1, false);
            final int j5 = (int) ((double) i2 * 0.86602540378444004D);
            for (int k5 = 0; k5 < 2; k5++) {
                final int l5 = (j1 - i4 - j2) + k5;
                a(g1, i5 + j5, l5 + i2, i2, 90, 60);
                g1.drawLine(i5 + j5, l5, (i5 + k3) - i2, l5);
                a(g1, (i5 + k3) - i2, l5 - i2, i2, 0, -90);
                a(g1, i5 + k3 + i2, l5 - i2, i2, -90, -90);
                g1.drawLine(i5 + k3 + i2, l5, (i5 + j3) - j5, l5);
                a(g1, (i5 + j3) - j5, l5 + i2, i2, 90, -60);
            }
            final int l4 = S._mthif();
            if (a(10)) {
                _mthint((i1 + l3) - k2 / 2, j1 - l2 - i2 - j2, true, g1, k1, false);
            } else {
                S.a(l4);
            }
        }
        i4 += i3 + i2 + j2;
        j3 = Math.max(j3, k2);
        return new c(j3, i4, j4);
    }

    private c _mthlong(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1) {
        int l1 = 0;
        c c1 = new c();
        final int i2 = g1.getFont().getSize() / 4;
        final int j2 = i2 / 2;
        int k2 = 0;
        int l2 = 0;
        int i3 = 0;
        if (flag) {
            l1 = S._mthif();
        }
        c1 = _mthtry(i1, j1, false, g1, k1, false);
        int j3 = c1._flddo;
        final int k3 = j3 / 2;
        int l3 = k3;
        final int i4 = c1._fldif;
        int j4 = c1.a;
        final int k4 = S._mthif();
        if (a(11)) {
            final c c2 = _mthdo(i1, j1, false, g1, k1, false);
            k2 = c2._flddo;
            l3 = Math.max(l3, k2 / 2);
            l2 = j4 + c2._fldif;
            i3 = c2._fldif + c2.a;
        } else {
            S.a(k4);
        }
        if (flag) {
            S.a(l1);
            final int i5 = (i1 + l3) - k3;
            _mthtry(i5, j1, true, g1, k1, false);
            final int j5 = (int) ((double) i2 * 0.86602540378444004D);
            for (int k5 = 0; k5 < 2; k5++) {
                final int l5 = (j1 + j4 + j2) - k5;
                a(g1, i5 + j5, l5 - i2, i2, -90, -60);
                g1.drawLine(i5 + j5, l5, (i5 + k3) - i2, l5);
                a(g1, (i5 + k3) - i2, l5 + i2, i2, 90, -90);
                a(g1, i5 + k3 + i2, l5 + i2, i2, 90, 90);
                g1.drawLine(i5 + k3 + i2, l5, (i5 + j3) - j5, l5);
                a(g1, (i5 + j3) - j5, l5 - i2, i2, -90, 60);
            }
            final int l4 = S._mthif();
            if (S._mthint().y == 11) {
                _mthdo((i1 + l3) - k2 / 2, j1 + l2 + i2 + j2, true, g1, k1, false);
            } else {
                S.a(l4);
            }
        }
        j4 += i3 + i2 + j2;
        j3 = Math.max(j3, k2);
        return new c(j3, i4, j4);
    }

    private c a(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1, final boolean flag1) {
        int l1 = 0;
        c c1 = new c();
        if (flag) {
            l1 = S._mthif();
        }
        c1 = _mthtry(i1, j1, false, g1, k1, false);
        if (flag1) {
            c1._fldif += 2;
        } else {
            c1.a += 2;
        }
        final int i2 = c1._fldif;
        final int j2 = c1.a;
        if (flag) {
            S.a(l1);
            if (flag1) {
                g1.drawLine(i1 + 1, (j1 - i2) + 2, (i1 + c1._flddo) - 1, (j1 - i2) + 2);
            } else {
                g1.drawLine(i1, (j1 + j2) - 2, i1 + c1._flddo, (j1 + j2) - 2);
            }
            c1 = _mthtry(i1, j1, true, g1, k1, false);
        }
        return new c(c1._flddo, i2, j2);
    }

    private c _mthtry(int i1, final int j1, final boolean flag, final Graphics g1) {
        final FontMetrics fontmetrics = g1.getFontMetrics();
        final int k1 = g1.getFont().getSize() / 9;
        int l1 = fontmetrics.stringWidth(D.w);
        final int i2 = "([{)]}".indexOf(D.w);
        if (i2 >= 0) {
            l1 += k1;
            if (i2 > 2) {
                i1 += k1;
            }
        }
        if (flag) {
            g1.drawString(D.w, i1, j1);
        }
        return new c(l1, fontmetrics.getHeight() - fontmetrics.getDescent(), fontmetrics.getDescent());
    }

    private c _mthif(final int i1, final int j1, final boolean flag, final Graphics g1) {
        final FontMetrics fontmetrics = g1.getFontMetrics();
        if (flag) {
            g1.drawString(D.w, i1, j1);
        }
        return new c(fontmetrics.stringWidth(D.w), fontmetrics.getHeight() - fontmetrics.getDescent(), fontmetrics.getDescent());
    }

    private c _mthfor(final int i1, final int j1, final boolean flag, final Graphics g1) {
        int k1 = 0;
        final Font font = g1.getFont();
        try {
            k1 = Integer.parseInt(D.w);
        } catch (final NumberFormatException numberformatexception) {
            k1 = 0;
        }
        k1 = (k1 * font.getSize()) / 18;
        return new c(k1, 0, 0);
    }

    private c _mthcase(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1) {
        c c1 = new c();
        int l1 = 0;
        final FontMetrics fontmetrics = g1.getFontMetrics();
        int i2 = 0;
        int k2 = 0;
        int l2 = 0;
        boolean flag2 = false;
        if (flag) {
            l1 = S._mthif();
        }
        final int i3 = fontmetrics.stringWidth("A");
        final int j3 = i3 / 2;
        final int k3 = S._mthif();
        e e1 = new e();
        e1 = S._mthint();
        if (e1.w.equals("[")) {
            a(g1, k1 + 1);
            c1 = _mthtry(i1, j1, false, g1, k1 + 1, true);
            a(g1, k1);
            i2 = c1._flddo;
            final int j2 = c1._fldif;
            k2 = c1.a;
            l2 = k2 + j2;
            flag2 = true;
        } else {
            S.a(k3);
        }
        c1 = _mthtry(i1, j1, false, g1, k1, false);
        int l3 = c1._flddo + i3;
        int i4 = c1._fldif + 2;
        final int j4 = c1.a;
        if (flag2 & (i2 > j3)) {
            l3 += i2 - j3;
        }
        if (flag) {
            S.a(l1);
            int k4 = 0;
            if (flag2 & (i2 > j3)) {
                k4 = i2 - j3;
            }
            g1.drawLine(i1 + k4 + 1, j1 - i4 / 2, i1 + k4 + j3, (j1 + j4) - 1);
            g1.drawLine(i1 + k4 + j3, (j1 + j4) - 1, (i1 + k4 + i3) - 2, (j1 - i4) + 2);
            g1.drawLine((i1 + k4 + i3) - 2, (j1 - i4) + 2, i1 + l3, (j1 - i4) + 2);
            if (flag2) {
                S._mthint();
                a(g1, k1 + 1);
                if (i2 >= j3) {
                    g1.drawLine(i1 + 1, j1 - i4 / 2, i1 + k4 + 1, j1 - i4 / 2);
                    _mthtry(i1 + 1, j1 - i4 / 2 - k2 - 1, true, g1, k1 + 1, true);
                } else {
                    _mthtry(i1 + 1 + (j3 - i2), j1 - i4 / 2 - k2 - 1, true, g1, k1 + 1, true);
                }
                a(g1, k1);
            }
            _mthtry(i1 + k4 + i3, j1, true, g1, k1, false);
        }
        if (flag2 & (i4 / 2 < l2)) {
            i4 = i4 / 2 + l2;
        }
        return new c(l3, i4, j4);
    }

    private c _mthchar(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1) {
        c c1 = new c();
        int l1 = 0;
        final int i2 = g1.getFontMetrics().getLeading();
        if (flag) {
            l1 = S._mthif();
        }
        c1 = _mthint(i1, j1, false, g1, k1, true);
        int j2 = c1._flddo;
        final int k2 = c1._flddo;
        int l2 = (c1._fldif + c1.a) - i2;
        int i3 = c1.a - i2;
        c1 = _mthtry(i1, j1, false, g1, k1, false);
        j2 = Math.max(j2, c1._flddo);
        final int j3 = j2 / 2;
        final int k3 = c1._flddo;
        l2 += c1._fldif;
        final int l3 = c1.a;
        i3 += c1._fldif;
        if (flag) {
            S.a(l1);
            _mthint((i1 + j3) - k2 / 2, j1 - i3, true, g1, k1, false);
            _mthtry((i1 + j3) - k3 / 2, j1, true, g1, k1, false);
        }
        return new c(j2, l2, l3);
    }

    private c _mthdo(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1, final boolean flag1) {
        int l1 = 0;
        int i2 = 0;
        c c1 = new c();
        int j2 = 0;
        final int l2 = g1.getFontMetrics().getAscent() / 2;
        if (flag) {
            j2 = S._mthif();
        }
        a(g1, k1 + 1);
        c1 = _mthtry(i1, j1, false, g1, k1 + 1, false);
        int i3 = c1._flddo;
        if (flag1) {
            l1 = l2 - 1;
            i2 = (c1._fldif + c1.a) - l1;
        } else {
            i2 = c1._fldif + c1.a;
        }
        if (flag) {
            S.a(j2);
            if (flag1) {
                c1 = _mthtry(i1, (j1 + c1._fldif) - l1, true, g1, k1 + 1, false);
            } else {
                c1 = _mthtry(i1, j1 + c1._fldif, true, g1, k1 + 1, false);
            }
        }
        a(g1, k1);
        if (flag1) {
            final int k2 = S._mthif();
            if (a(10)) {
                final c c2 = _mthint(i1, j1, flag, g1, k1, true);
                i3 = Math.max(i3, c2._flddo);
                l1 = Math.max(l1, c2._fldif);
            } else {
                S.a(k2);
            }
        }
        return new c(i3, l1, i2);
    }

    private c _mthint(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1, final boolean flag1) {
        int l1 = 0;
        int i2 = 0;
        c c1 = new c();
        int j2 = 0;
        final int l2 = g1.getFontMetrics().getAscent() / 2;
        if (flag) {
            j2 = S._mthif();
        }
        a(g1, k1 + 1);
        c1 = _mthtry(i1, j1, false, g1, k1 + 1, false);
        int i3 = c1._flddo;
        if (flag1) {
            i2 = -l2 - 1;
            l1 = (c1._fldif + c1.a) - i2;
        } else {
            l1 = c1._fldif + c1.a;
        }
        if (flag) {
            S.a(j2);
            if (flag1) {
                c1 = _mthtry(i1, (j1 - c1.a) + i2, true, g1, k1 + 1, false);
            } else {
                c1 = _mthtry(i1, j1 - c1.a, true, g1, k1 + 1, false);
            }
        }
        a(g1, k1);
        if (flag1) {
            final int k2 = S._mthif();
            if (a(11)) {
                final c c2 = _mthdo(i1, j1, flag, g1, k1, true);
                i3 = Math.max(i3, c2._flddo);
                i2 = Math.max(i2, c2.a);
            } else {
                S.a(k2);
            }
        }
        return new c(i3, l1, i2);
    }

    private Image _mthif(final Graphics g1, final int i1) {
        final String s1 = D.w + j[i1 - 1] + g1.getColor().getRGB();
        if (!w.containsKey(s1)) {
            final String s2 = "Fonts/Greek" + j[i1 - 1] + "/" + D.w + ".gif";
            final Image image = _flddo.a(s2, g1);
            final int j1 = S._mthif();
            B.addImage(image, j1);
            a("Loading " + D.w);
            try {
                B.waitForID(j1, 10000L);
            } catch (final InterruptedException interruptedexception) {
            }
            if (B.isErrorID(j1)) {
                a("Error loading " + D.w);
            } else {
                w.put(s1, image);
            }
            return image;
        } else {
            return (Image) w.get(s1);
        }
    }

    private c _mthif(final int i1, final int j1, final boolean flag, final Graphics g1, int k1, final boolean flag1) {
        final FontMetrics fontmetrics = g1.getFontMetrics();
        k1 = Math.min(k1, j.length);
        final Image image = _mthif(g1, k1);
        int l1 = image.getWidth(PC);
        if (l1 < 0) {
            l1 = fontmetrics.getMaxAdvance();
        }
        if (flag) {
            int i2 = 0;
            if (flag1) {
                i2 = _fldfor[k1 - 1];
            }
            g1.drawImage(image, i1, (j1 - image.getHeight(PC)) + i2, PC);
        }
        return new c(l1, fontmetrics.getHeight() - fontmetrics.getDescent(), fontmetrics.getDescent());
    }

    private c _mthdo(final int i1, final int j1, final boolean flag, final Graphics g1, int k1) {
        int l1 = 0;
        new c();
        int i2 = 0;
        int j2 = 0;
        int k2 = 0;
        int l2 = 0;
        int i3 = 0;
        final int j3 = g1.getFontMetrics().getAscent();
        k1 = Math.min(k1, j.length);
        final Image image = _mthif(g1, k1);
        int k3 = l1 = image.getWidth(PC);
        int l3 = image.getHeight(PC);
        if (l3 < 0) {
            l3 = 2 * j3;
            k3 = l1 = j3;
        }
        int i4 = (int) ((double) (l3 / 2) - 0.40000000000000002D * (double) j3);
        int j4 = i3 = l3 - i4;
        final int k4 = S._mthif();
        if (a(11)) {
            final c c2 = _mthdo(i1, j1, false, g1, k1, false);
            k2 = c2._flddo;
            l1 = Math.max(l1, c2._flddo);
            i2 = i4 + c2._fldif;
            i4 += c2._fldif + c2.a;
        } else {
            S.a(k4);
        }
        final int l4 = S._mthif();
        if (a(10)) {
            final c c3 = _mthint(i1, j1, false, g1, k1, false);
            l2 = c3._flddo;
            l1 = Math.max(l1, c3._flddo);
            j2 = j4 + c3.a;
            j4 += c3._fldif + c3.a;
        } else {
            S.a(l4);
        }
        if (flag) {
            S.a(k4);
            g1.drawImage(image, i1 + (l1 - k3) / 2, j1 - i3, PC);
            if (a(11)) {
                _mthdo(i1 + (l1 - k2) / 2, j1 + i2, true, g1, k1, false);
            } else {
                S.a(k4);
            }
            final int i5 = S._mthif();
            if (a(10)) {
                _mthint(i1 + (l1 - l2) / 2, j1 - j2, true, g1, k1, false);
            } else {
                S.a(i5);
            }
        }
        return new c(l1, j4, i4);
    }

    private c _mthint(final int i1, final int j1, final boolean flag, final Graphics g1, final int k1) {
        c c1 = new c();
        final int l1 = g1.getFont().getSize();
        final String s1 = D.w;
        c1 = _mthtry(i1, j1, flag, g1, k1, false);
        final int i2 = c1._flddo;
        final int j2 = i2 / 2;
        final int k2 = l1 / 4;
        final int l2 = c1._fldif + k2;
        final int i3 = c1.a;
        if (flag) {
            final int j3 = (j1 - l2) + k2;
            final int k3 = l1 / 8;
            final int l3 = i1 + i2;
            final int i4 = i1 + j2;
            if (s1.equals("")) {
                g1.drawLine(i1, j3, l3, j3);
                g1.drawLine(i1 + (int) ((double) i2 * 0.80000000000000004D), j3 - k3, l3, j3);
                g1.drawLine(i1 + (int) ((double) i2 * 0.80000000000000004D), j3 + k3, l3, j3);
            } else if (s1.equals("bar")) {
                g1.drawLine(i1, j3, l3, j3);
            } else if (s1.equals("widehat")) {
                g1.drawLine(i1, j3, i4, j3 - k2);
                g1.drawLine(i4, j3 - k2, l3, j3);
            } else if (s1.equals("widetilde")) {
                int k4 = 0;
                for (int l4 = 1; l4 < j2; l4++) {
                    final int j4 = k4;
                    k4 = (int) ((double) k3 * Math.sin((4.0840704496667311D * (double) l4) / (double) j2));
                    g1.drawLine((i4 + l4) - 1, j3 + j4, i4 + l4, j3 + k4);
                    g1.drawLine((i4 - l4) + 1, j3 - j4, i4 - l4, j3 - k4);
                }
            }
        }
        return new c(i2, l2 + 2, i3);
    }

    private boolean a(final int i1) {
        return a(i1, "");
    }

    private boolean a(final int i1, final String s1) {
        int j1;
        while ((j1 = S._mthint().y) == 25) {
        }
        if (j1 == i1) {
            return true;
        }
        if (!s1.equals("")) {
            printStatus("Parser: " + s1 + " not found");
        }
        return false;
    }

    private boolean a(final int i1, final int j1, final String s1) {
        int k1;
        while ((k1 = S._mthint().y) == 25) {
        }
        boolean flag = k1 == i1;
        while ((k1 = S._mthint().y) == 25) {
        }
        flag = k1 == j1;
        if (!flag && !s1.equals("")) {
            printStatus("Parser: " + s1 + " not found");
        }
        return flag;
    }

    private void a(final Graphics g1, final int i1) {
        if (i1 <= 1) {
            g1.setFont(d);
        } else if (i1 == 2) {
            g1.setFont(b);
        } else if (i1 == 3) {
            g1.setFont(_fldnull);
        } else {
            g1.setFont(_fldlong);
        }
    }
}
