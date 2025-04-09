package com.ynhenc.gis.file.wmf;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

public class WmfDecoder implements ImageProducer {

    boolean debug = false;

    boolean drawCross_if_error = true;

    private int minsize = 8;

    private int top, left, siz, obj, max, res, inch;

    private WmfDecObj gdiObj[];

    private Stack DCstack;

    private int rgbPixels[] = null;

    private short params[];

    private Frame fr;

    private int width = -1, height = -1;

    private InputStream in;

    private ColorModel cmodel = ColorModel.getRGBdefault();

    private boolean err = false;

    private boolean producing = false;

    private Vector consumers = new Vector();

    public WmfDecoder(InputStream is) {
        in = is;
    }

    public void addConsumer(ImageConsumer ic) {
        if (debug) {
            System.out.println("addConsumer:" + ic);
        }
        if (ic != null && !isConsumer(ic)) {
            consumers.addElement(ic);
        }
    }

    public void startProduction(ImageConsumer ic) {
        if (debug) {
            System.out.println("startProduction:" + ic);
        }
        addConsumer(ic);
        if (rgbPixels == null) {
            try {
                readWmf();
            } catch (Exception ex) {
                err = true;
                width = height = -1;
                System.out.println(ex);
            }
        }
        if (!producing) {
            producing = true;
            sendImage();
        }
    }

    public boolean isConsumer(ImageConsumer ic) {
        return consumers.contains(ic);
    }

    public void removeConsumer(ImageConsumer ic) {
        if (debug) {
            System.out.println("Remove:" + ic);
        }
        consumers.removeElement(ic);
    }

    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }

    private void sendImage() {
        Vector xconsumers = (Vector) consumers.clone();
        for (Enumeration e = xconsumers.elements(); e.hasMoreElements(); ) {
            if (debug) {
                System.out.println("consumers.size:" + consumers.size());
            }
            ImageConsumer ic = (ImageConsumer) e.nextElement();
            if (isConsumer(ic)) {
                if (debug) {
                    System.out.println("setPixels:" + ic);
                }
                if (!err) {
                    ic.setDimensions(width, height);
                    ic.setColorModel(cmodel);
                    ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME);
                    for (int row = 0; row < height; row++) {
                        ic.setPixels(0, row, width, 1, cmodel, rgbPixels, row * width, width);
                    }
                    ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
                } else {
                    if (debug) {
                        System.out.println("IMAGEERROR");
                    }
                    ic.imageComplete(ImageConsumer.IMAGEABORTED);
                }
            }
        }
        producing = false;
    }

    private static void PrintObserverStatus(String text, int status) {
        System.out.print(text);
        if ((status & 0x80) > 0) {
            System.out.print("ABORT ");
        }
        if ((status & 0x40) > 0) {
            System.out.print("ERROR ");
        }
        if ((status & 0x20) > 0) {
            System.out.print("ALLBITS ");
        }
        if ((status & 0x10) > 0) {
            System.out.print("FRAMEBITS ");
        }
        if ((status & 0x08) > 0) {
            System.out.print("SOMEBITS ");
        }
        if ((status & 0x04) > 0) {
            System.out.print("PROPERTIES ");
        }
        if ((status & 0x02) > 0) {
            System.out.print("HEIGHT ");
        }
        if ((status & 0x01) > 0) {
            System.out.print("WIDTH ");
        }
        System.out.println("");
    }

    private void readWmf() throws IOException, InterruptedException {
        Dimension d = new Dimension(320, 240);
        Image offscreen;
        Graphics g;
        if (chkHeader(in, d)) {
            throw new IOException("WMF file format not supported");
        }
        DCstack = new Stack();
        gdiObj = new WmfDecObj[obj];
        width = d.width;
        height = d.height;
        if (debug) {
            System.out.println(d);
        }
        fr = new Frame();
        fr.addNotify();
        offscreen = fr.createImage(d.width, d.height);
        g = offscreen.getGraphics();
        params = new short[max];
        WmfDecDC DC = new WmfDecDC(width, height, left, top);
        DC.gr = g;
        DCstack.push(DC);
        while (readRecord(in)) {
            ;
        }
        rgbPixels = new int[d.width * d.height];
        PixelGrabber pg = new PixelGrabber(offscreen.getSource(), 0, 0, d.width, d.height, rgbPixels, 0, d.width);
        pg.grabPixels();
        if (debug) {
            PrintObserverStatus("PixelGrabber status: ", pg.status());
        }
        System.out.println("PixelGrabber status:" + pg.status());
        System.out.println("fr=" + fr);
        fr.dispose();
    }

    private boolean chkHeader(InputStream in, Dimension d) throws IOException {
        int i, j, wid = 0, hig = 0, sum = 0;
        int hdr[] = { -12841, -25914, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 9, 0x300 };
        for (i = 0; i < 14; i++) {
            j = readInt16(in);
            sum ^= j;
            if ((i < 3 || i > 7)) {
                if (j != hdr[i]) {
                    if ((i += 11) == 11 && j == hdr[11]) {
                        continue;
                    } else {
                        return true;
                    }
                }
            } else {
                switch(i) {
                    case 3:
                        left = j;
                        break;
                    case 4:
                        top = j;
                        break;
                    case 5:
                        wid = j;
                        break;
                    case 6:
                        hig = j;
                        break;
                    case 7:
                        hdr[10] = sum;
                        res = Toolkit.getDefaultToolkit().getScreenResolution();
                        inch = j;
                        if (debug) {
                            System.out.println("inch:  " + inch);
                            System.out.println("sres:  " + res);
                        }
                        d.width = ((wid - left) * res) / inch;
                        d.height = ((hig - top) * res) / inch;
                        break;
                }
            }
        }
        if (debug) {
            System.out.println("dimension: " + d);
        }
        siz = readInt32(in);
        obj = readInt16(in);
        max = readInt32(in);
        readInt16(in);
        if (debug) {
            System.out.println("filesize(16): " + siz);
            System.out.println("GDI-Objects : " + obj);
            System.out.println("max rec size: " + max);
        }
        return false;
    }

    private boolean readRecord(InputStream in) {
        int i, j, rdSize, rdFunc;
        int a, b, c, d, e, f, k, l, m, n;
        Color crco;
        Font fo;
        Image im;
        WmfDecDC DC = (WmfDecDC) DCstack.peek();
        Graphics g = DC.gr;
        boolean error;
        int xpoints[], ypoints[];
        byte text[];
        String s;
        Object ob;
        Graphics g2;
        try {
            rdSize = readInt32(in);
            rdFunc = readInt16(in);
            for (i = 0; i < rdSize - 3; i++) {
                params[i] = readInt16(in);
            }
        } catch (IOException ex) {
            return false;
        }
        if (debug) {
            System.out.println("RFunc: " + Integer.toString(rdFunc, 16));
        }
        switch(rdFunc) {
            case META_LINETO:
                if (debug) {
                    System.out.println("MetaLineTo");
                }
                g.setColor(DC.aktpen.getColor());
                a = DC.ytransfer(params[0]);
                b = DC.xtransfer(params[1]);
                g.drawLine(DC.aktXpos, DC.aktYpos, b, a);
                DC.aktXpos = b;
                DC.aktYpos = a;
                break;
            case META_MOVETO:
                if (debug) {
                    System.out.println("MetaMoveTo");
                }
                DC.aktYpos = DC.ytransfer(params[0]);
                DC.aktXpos = DC.xtransfer(params[1]);
                break;
            case META_ROUNDRECT:
                if (debug) {
                    System.out.println("MetaRoundRect");
                }
                e = transform(params[0], minsize);
                f = transform(params[1], minsize);
                a = DC.ytransfer(params[2]);
                b = DC.xtransfer(params[3]);
                c = DC.ytransfer(params[4]);
                d = DC.xtransfer(params[5]);
                if (a < c && b < d) {
                    i = a;
                    a = c;
                    c = i;
                    i = b;
                    b = d;
                    d = i;
                }
                g.setColor(DC.aktbrush.getColor());
                g.fillRoundRect(d, c, b - d - 1, a - c - 1, f, e);
                g.setColor(DC.aktpen.getColor());
                g.drawRoundRect(d, c, b - d - 1, a - c - 1, f, e);
                break;
            case META_RECTANGLE:
                if (debug) {
                    System.out.println("MetaRectangle");
                }
                a = DC.ytransfer(params[0]);
                b = DC.xtransfer(params[1]);
                c = DC.ytransfer(params[2]);
                d = DC.xtransfer(params[3]);
                if (a < c && b < d) {
                    i = a;
                    a = c;
                    c = i;
                    i = b;
                    b = d;
                    d = i;
                }
                if (DC.aktbrush.getImage() != null) {
                    drawOpaqePattern(g, DC.aktbrush.getImage(), d, c, b, a, fr);
                } else {
                    g.setColor(DC.aktbrush.getColor());
                    g.fillRect(d, c, b - d - 1, a - c - 1);
                }
                g.setColor(DC.aktpen.getColor());
                g.drawRect(d, c, b - d - 1, a - c - 1);
                break;
            case META_SETPIXEL:
                if (debug) {
                    System.out.println("MetaSetpixel");
                }
                crco = new Color(getLoByteVal(params[0]), getHiByteVal(params[0]), getLoByteVal(params[1]));
                g.setColor(crco);
                crco = null;
                a = DC.xtransfer(params[3]);
                b = DC.ytransfer(params[2]);
                g.drawLine(a, b, a, b);
                break;
            case META_POLYLINE:
            case META_POLYGON:
                if (debug) {
                    System.out.println(((rdFunc == META_POLYGON) ? "MetaPolygon: " : "MetaPolyLine: ") + params[0]);
                }
                xpoints = new int[params[0]];
                ypoints = new int[params[0]];
                for (i = 0; i < params[0]; i++) {
                    xpoints[i] = DC.xtransfer(params[i * 2 + 1]);
                    ypoints[i] = DC.ytransfer(params[i * 2 + 2]);
                    if (debug) {
                        System.out.println(Integer.toString(xpoints[i], 16) + " " + Integer.toString(ypoints[i], 16));
                    }
                }
                if (rdFunc == META_POLYGON) {
                    g.setColor(DC.aktbrush.getColor());
                    g.fillPolygon((int[]) xpoints, (int[]) ypoints, params[0]);
                    g.setColor(DC.aktpen.getColor());
                    g.drawPolygon((int[]) xpoints, (int[]) ypoints, params[0]);
                } else {
                    g.setColor(DC.aktpen.getColor());
                    g.drawPolyline((int[]) xpoints, (int[]) ypoints, params[0]);
                }
                xpoints = null;
                ypoints = null;
                break;
            case META_POLYPOLYGON:
                if (debug) {
                    System.out.println("MetaPolyPolygon: " + params[0]);
                }
                for (i = 0; i < params[0]; i++) {
                    xpoints = new int[params[i + 1]];
                    ypoints = new int[params[i + 1]];
                    if (debug) {
                        System.out.println("Polygon #" + i + " Pts=" + params[i + 1]);
                    }
                    b = params[0] + 1;
                    for (c = 0; c < i; c++) {
                        b += params[c + 1] * 2;
                    }
                    for (a = 0; a < params[i + 1]; a++) {
                        xpoints[a] = DC.xtransfer(params[b + a * 2]);
                        ypoints[a] = DC.ytransfer(params[b + a * 2 + 1]);
                        if (debug) {
                            System.out.println(Integer.toString(xpoints[a], 16) + " " + Integer.toString(ypoints[a], 16));
                        }
                    }
                    g.setColor(DC.aktbrush.getColor());
                    g.fillPolygon((int[]) xpoints, (int[]) ypoints, params[i + 1]);
                    g.drawPolygon((int[]) xpoints, (int[]) ypoints, params[i + 1]);
                }
                break;
            case META_ELLIPSE:
                if (debug) {
                    System.out.println("MetaEllipse");
                }
                a = DC.ytransfer(params[0]);
                b = DC.xtransfer(params[1]);
                c = DC.ytransfer(params[2]);
                d = DC.xtransfer(params[3]);
                g.setColor(DC.aktpen.getColor());
                g.drawOval(d, c, b - d, a - c);
                g.setColor(DC.aktbrush.getColor());
                g.fillOval(d, c, b - d, a - c);
                break;
            case META_ARC:
            case META_PIE:
            case META_CHORD:
                if (debug) {
                    switch(rdFunc) {
                        case META_ARC:
                            System.out.println("MetaArc");
                            break;
                        case META_PIE:
                            System.out.println("MetaPie");
                            break;
                        case META_CHORD:
                            System.out.println("MetaChord");
                            break;
                    }
                }
                a = DC.ytransfer(params[0]);
                b = DC.xtransfer(params[1]);
                c = DC.ytransfer(params[2]);
                d = DC.xtransfer(params[3]);
                e = DC.ytransfer(params[4]);
                f = DC.xtransfer(params[5]);
                k = DC.ytransfer(params[6]);
                l = DC.xtransfer(params[7]);
                g.setColor(DC.aktpen.getColor());
                int xm = l + (f - l) / 2;
                int ym = k + (e - k) / 2;
                if (rdFunc == META_PIE) {
                    g.drawLine(d, c, xm, ym);
                    g.drawLine(b, a, xm, ym);
                }
                if (rdFunc == META_CHORD) {
                    g.drawLine(d, c, b, a);
                }
                int beg = arcus(d - xm, c - ym);
                int arc = arcus(b - xm, a - ym) - beg;
                if (arc < 0) {
                    arc += 360;
                }
                if (debug) {
                    System.out.println("Beg=" + beg + " Arc=" + arc);
                }
                g.drawArc(l, k, f - l, e - k, beg, arc);
                break;
            case META_DELETEOBJECT:
                if (debug) {
                    System.out.println("MetaDeleteObject:" + params[0]);
                }
                gdiObj[params[0]] = null;
                break;
            case META_SELECTPALETTE:
                if (debug) {
                    System.out.println("MetaSelectPalette:" + params[0] + " = " + gdiObj[params[0]]);
                }
                if (gdiObj[params[0]].getMagic() == WmfDecObj.M_PALETTE) {
                    DC.aktpal = gdiObj[params[0]];
                } else {
                    System.out.println(" ---- internal ERROR in MetaSelectPalette -----");
                }
                break;
            case META_SELECTCLIPREGION:
                if (debug) {
                    System.out.println("MetaSelectClipRegion:" + params[0] + " = " + gdiObj[params[0]]);
                }
                if (gdiObj[params[0]].getMagic() == WmfDecObj.M_CLIP) {
                    DC.aktclip = gdiObj[params[0]];
                    g.clipRect(DC.aktclip.getRect().x, DC.aktclip.getRect().y, DC.aktclip.getRect().width, DC.aktclip.getRect().height);
                } else {
                    System.out.println(" ---- internal ERROR in MetaSelectClipregion -----");
                }
                break;
            case META_SELECTOBJECT:
                if (debug) {
                    System.out.println("MetaSelectObject:" + params[0] + " = " + gdiObj[params[0]]);
                }
                switch(gdiObj[params[0]].getMagic()) {
                    case WmfDecObj.M_PEN:
                        DC.aktpen = gdiObj[params[0]];
                        break;
                    case WmfDecObj.M_FONT:
                        DC.aktfont = gdiObj[params[0]];
                        break;
                    case WmfDecObj.M_BRUSH:
                        DC.aktbrush = gdiObj[params[0]];
                        break;
                    case WmfDecObj.M_PALETTE:
                        DC.aktpal = gdiObj[params[0]];
                        break;
                    case WmfDecObj.M_BITMAP:
                        DC.aktbmp = gdiObj[params[0]];
                        break;
                    case WmfDecObj.M_CLIP:
                        DC.aktclip = gdiObj[params[0]];
                        if (debug) {
                            System.out.println("Select clipping rect");
                            g.drawRect(DC.aktclip.getRect().x, DC.aktclip.getRect().y, DC.aktclip.getRect().width, DC.aktclip.getRect().height);
                        }
                        g.clipRect(DC.aktclip.getRect().x, DC.aktclip.getRect().y, DC.aktclip.getRect().width, DC.aktclip.getRect().height);
                        break;
                }
                break;
            case META_CREATEPENINDIRECT:
                if (debug) {
                    System.out.println("MetaCreatePenIndirect");
                }
                error = false;
                switch(params[0]) {
                    case PS_NULL:
                        crco = null;
                        System.out.println("MetaCreatePenIndirect: PS_NULL");
                        break;
                    case PS_DASH:
                    case PS_DOT:
                    case PS_DASHDOT:
                    case PS_DASHDOTDOT:
                        System.out.println("MetaCreatePenIndirect: line attribute " + params[0] + " ignored");
                    case PS_INSIDEFRAME:
                    case PS_SOLID:
                        crco = new Color(getLoByteVal(params[3]), getHiByteVal(params[3]), getLoByteVal(params[4]));
                        break;
                    default:
                        crco = Color.black;
                        error = true;
                        break;
                }
                if (!error) {
                    add_handle(new WmfDecObj(PS_SOLID, crco));
                    if (debug) {
                        System.out.println(crco);
                    }
                    crco = null;
                    a = params[1];
                    b = params[2];
                }
                if (debug || error) {
                    for (i = 0; i < rdSize - 3; i++) {
                        if (i < 16) {
                            System.out.print(Integer.toString(params[i], 16) + " ");
                        }
                    }
                    System.out.println();
                }
                break;
            case META_CREATEBRUSHINDIRECT:
                if (debug) {
                    System.out.println("MetaCreateBrushIndirect: Style_00_Object=" + params[0]);
                    showparams(params, rdSize, rdFunc);
                }
                switch(params[0]) {
                    case 1:
                        crco = DC.aktbackgnd;
                        add_handle(new WmfDecObj(crco, WmfDecObj.M_BRUSH));
                        if (debug) {
                            System.out.println(crco);
                        }
                        break;
                    case 0:
                        crco = new Color(getLoByteVal(params[1]), getHiByteVal(params[1]), getLoByteVal(params[2]));
                        add_handle(new WmfDecObj(crco, WmfDecObj.M_BRUSH));
                        if (debug) {
                            System.out.println(crco);
                        }
                        crco = null;
                        break;
                    case 2:
                        crco = new Color(getLoByteVal(params[1]), getHiByteVal(params[1]), getLoByteVal(params[2]));
                        add_handle(new WmfDecObj((int) params[3], crco, DC.aktbackgnd, fr));
                        if (debug) {
                            System.out.println(crco);
                        }
                        crco = null;
                        break;
                    case 3:
                    case 4:
                    case 5:
                        crco = Color.gray;
                        add_handle(new WmfDecObj(crco, WmfDecObj.M_BRUSH));
                        System.out.println("pattern substitution used.");
                        break;
                    default:
                        System.out.println("(bad parameter!)");
                }
                break;
            case META_CREATEREGION:
                if (debug) {
                    System.out.println("MetaCreateRegion");
                    System.out.println("params[5] sub records=" + params[5]);
                    for (i = 0; i < rdSize - 3; i++) {
                        System.out.print(Integer.toString(params[i], 10) + " ");
                    }
                    System.out.println();
                }
                add_handle(new WmfDecObj(DC.xtransfer(params[7]), DC.ytransfer(params[8]), DC.xtransfer(params[9]), DC.xtransfer(params[10])));
                break;
            case META_INTERSECTCLIPRECT:
                System.out.println("MetaIntersectClipRect is experimental");
                n = DC.ytransfer(params[0]);
                m = DC.xtransfer(params[1]);
                l = DC.ytransfer(params[2]);
                k = DC.xtransfer(params[3]);
                g.clipRect(k, l, m - k, n - l);
                break;
            case META_CREATEFONTINDIRECT:
                text = new byte[80];
                for (j = i = 0; i < rdSize - 3 - 9; i++) {
                    if ((text[2 * i] = (byte) getLoByteVal(params[i + 9])) == 0) {
                        break;
                    } else {
                        j++;
                    }
                    if ((text[2 * i + 1] = (byte) getHiByteVal(params[i + 9])) == 0) {
                        break;
                    } else {
                        j++;
                    }
                }
                s = new String(text, 0, 0, j);
                if (debug) {
                    System.out.println("MetaCreateFontIndirect: " + params[0] + " " + params[1] + " " + s);
                }
                if (s.startsWith("Times")) {
                    s = "TimesRoman";
                } else if (s.startsWith("Arial")) {
                    s = "Helvetica";
                } else if (s.startsWith("Courier")) {
                    s = "Courier";
                } else if (s.startsWith("MS")) {
                    s = "Dialog";
                } else if (s.startsWith("WingDings")) {
                    s = "ZapfDingbats";
                }
                b = params[1];
                c = params[2];
                d = params[3];
                e = params[4];
                f = params[5];
                k = params[6];
                l = params[7];
                i = params[8];
                a = transform(params[0], minsize);
                fo = new Font(s, (e > 500 ? Font.BOLD : Font.PLAIN) + (getLoByteVal(f) > 0 ? Font.ITALIC : 0), a);
                if (debug) {
                    System.out.println(fo);
                }
                add_handle(new WmfDecObj(fo, getHiByteVal(f), d));
                fo = null;
                text = null;
                break;
            case META_CREATEPALETTE:
                if (debug) {
                    System.out.println("MetaCreatePalette");
                }
                crco = Color.black;
                add_handle(new WmfDecObj(crco, WmfDecObj.M_PALETTE));
                break;
            case META_REALIZEPALETTE:
                if (debug) {
                    showparams(params, rdSize, rdFunc);
                }
                System.out.println("MetaRealizePalette");
                break;
            case META_SETROP2:
                if (debug) {
                    System.out.println("MetaSetRop2: ROP code=" + Integer.toString((i = params[0]), 16));
                }
                break;
            case META_SETPOLYFILLMODE:
                if (debug) {
                    System.out.println("MetaSetPolyFillmode:" + params[0]);
                }
                break;
            case META_SETSTRETCHBLTMODE:
                if (debug) {
                    System.out.println("MetaSetStretchBltMode:" + params[0]);
                }
                break;
            case META_INVERTREGION:
                if (debug) {
                    showparams(params, rdSize, rdFunc);
                }
                System.out.println("MetaInvertRegion:" + params[0]);
                break;
            case META_SETWINDOWEXT:
                DC.winextY = params[0];
                DC.winextX = params[1];
                if (debug) {
                    System.out.println("MetaSetWindowExt:  X:" + DC.winextX + "  Y:" + DC.winextY);
                }
                break;
            case META_SETWINDOWORG:
                DC.winorgY = params[0];
                DC.winorgX = params[1];
                if (debug) {
                    System.out.println("MetaSetWindowOrg:  X:" + DC.winorgX + "  Y:" + DC.winorgY);
                }
                break;
            case META_SETTEXTCOLOR:
                DC.akttextc = new Color(getLoByteVal(params[0]), getHiByteVal(params[0]), getLoByteVal(params[1]));
                if (debug) {
                    System.out.println("MetaSetTextColor: " + DC.akttextc);
                }
                break;
            case META_EXTTEXTOUT:
            case META_TEXTOUT:
                if (rdFunc == META_EXTTEXTOUT) {
                    a = params[2];
                    b = DC.ytransfer(params[0]);
                    c = DC.xtransfer(params[1]);
                    d = params[3];
                    if (debug) {
                        System.out.println("ExtTextOut:option =" + Integer.toString(d, 16));
                    }
                    k = DC.xtransfer(params[4]);
                    l = DC.ytransfer(params[5]);
                    m = DC.xtransfer(params[6]);
                    n = DC.ytransfer(params[7]);
                    if (debug) {
                        System.out.println("TextAlign=" + DC.akttextalign);
                        System.out.println("x  =" + c + "\ty  =" + b);
                        System.out.println("rx =" + k + "\try =" + l);
                        System.out.println("rw =" + (m - k) + "\trh =" + (n - l));
                    }
                    e = d == 0 ? 3 : 7;
                } else {
                    a = params[0];
                    b = DC.ytransfer(params[(a + 1) / 2 + 1]);
                    c = DC.xtransfer(params[(a + 1) / 2 + 2]);
                    d = e = 0;
                    k = l = m = n = 0;
                }
                if ((d & ETO_OPAQUE) != 0) {
                    g.setColor(DC.aktbackgnd);
                    g.fillRect(k, l, m - k - 1, n - l - 1);
                    if (debug) {
                        System.out.println("ExtTextOut: using OPAQUE style");
                    }
                }
                if ((d & ETO_GRAYED) != 0) {
                    g.setColor(Color.lightGray);
                } else {
                    g.setColor(DC.akttextc);
                }
                if ((d & ETO_CLIPPED) != 0) {
                    g2 = g.create();
                    g2.clipRect(k, l, m - k - 1, n - l - 1);
                    g = g2;
                    if (debug) {
                        System.out.println("ExtTextOut: using clipping rect");
                    }
                } else {
                    g2 = null;
                }
                g.setFont(DC.aktfont.getFont());
                FontMetrics fm = g.getFontMetrics();
                text = new byte[a];
                for (i = 0; i < a; i++) {
                    if (i % 2 == 0) {
                        text[i] = (byte) getLoByteVal(params[e + i / 2 + 1]);
                    } else {
                        text[i] = (byte) getHiByteVal(params[e + i / 2 + 1]);
                    }
                }
                s = new String(text, 0);
                if (DC.aktfont.getFontOrientation() != 0) {
                    System.out.println("non horizontal text is not supported: " + s);
                } else {
                    if (DC.akttextalign == TA_TOP) {
                        b += DC.aktfont.getFont().getSize();
                    }
                    g.drawString(s, c, b);
                    if (DC.aktfont.isUnderlined()) {
                        g.drawLine(c, b + 2, c + fm.stringWidth(s), b + 2);
                    }
                }
                if (debug) {
                    System.out.println((rdFunc == META_EXTTEXTOUT ? "MetaExtTextOut: " : "MetaTextOut: ") + (new String(text, 0)) + " (len=" + a + ") x=" + c + " y=" + b);
                }
                text = null;
                if (g2 != null) {
                    g2.dispose();
                }
                break;
            case META_SETMAPMODE:
                if (debug) {
                    showparams(params, rdSize, rdFunc);
                }
                System.out.println("MetaSetMapMode: " + params[0] + " (ignored)");
                break;
            case META_SETBKCOLOR:
                if (debug) {
                    System.out.println("MetaSetBkColor");
                }
                DC.aktbackgnd = new Color(getLoByteVal(params[0]), getHiByteVal(params[0]), getLoByteVal(params[1]));
                break;
            case META_SETTEXTJUSTIFICATION:
                if (debug) {
                    showparams(params, rdSize, rdFunc);
                }
                if (debug || params[0] != 0 || params[1] != 0) {
                    System.out.println("MetaSetTextJustification: " + params[0] + " " + params[1]);
                }
                break;
            case META_SETBKMODE:
                if (debug) {
                    System.out.println("MetaSetBkMode:" + (params[0] == 1 ? "TRANSPARENT" : "OPAQUE"));
                }
                DC.aktbkmode = params[0];
                break;
            case META_SETTEXTALIGN:
                if (debug) {
                    System.out.println("MetaSetTextalign: " + params[0]);
                }
                DC.akttextalign = params[0];
                break;
            case META_SAVEDC:
                if (debug) {
                    System.out.println("MetaSaveDC");
                }
                try {
                    DC = (WmfDecDC) DCstack.push(DC.clone());
                    DC.slevel++;
                    DC.gr = g.create();
                } catch (Exception ex) {
                    System.out.println(" ---- internal ERROR in MetaSaveDC -----");
                }
                break;
            case META_RESTOREDC:
                if (debug) {
                    System.out.println("MetaRestoreDC" + params[0]);
                }
                switch(params[0]) {
                    case -1:
                        g.dispose();
                        DCstack.pop();
                        DC = (WmfDecDC) DCstack.peek();
                        break;
                    default:
                        while (DC.slevel > params[0] && !DCstack.empty()) {
                            g.dispose();
                            DC = (WmfDecDC) DCstack.pop();
                            g = DC.gr;
                        }
                        break;
                }
                break;
            case META_PATBLT:
                e = (params[1] << 16) + params[0];
                if (debug) {
                    System.out.println("MetaPatBlt: ROP code=" + Integer.toString(e, 16));
                    System.out.println(DC.aktbrush.getImage());
                }
                a = DC.ytransfer(params[2]);
                b = DC.xtransfer(params[3]);
                c = DC.ytransfer(params[4]);
                d = DC.xtransfer(params[5]);
                switch(e) {
                    case WHITENESS:
                        g.setColor(Color.white);
                        g.fillRect(d, c, b, a);
                        break;
                    case BLACKNESS:
                        g.setColor(Color.black);
                        g.fillRect(d, c, b, a);
                        break;
                    case PATCOPY:
                        if ((im = DC.aktbrush.getImage()) != null) {
                            drawOpaqePattern(g, im, d, c, d + b, c + a, fr);
                        } else {
                            g.setColor(DC.aktbrush.getColor());
                            g.fillRect(d, c, b, a);
                        }
                        break;
                    case PATINVERT:
                    case DSTINVERT:
                    default:
                        System.out.println("unsupported ROP code:" + Integer.toString(e, 16));
                }
                break;
            case META_STRETCHBLT:
                if (debug) {
                    System.out.println("MetaStretchBlt:" + rdSize);
                }
                e = (params[1] << 16) + params[0];
                a = DC.ytransfer(params[6]);
                b = DC.xtransfer(params[7]);
                c = DC.ytransfer(params[8]);
                d = DC.xtransfer(params[9]);
                switch(e) {
                    case WHITENESS:
                        g.setColor(Color.white);
                        g.fillRect(d, c, b, a);
                        break;
                    case BLACKNESS:
                        g.setColor(Color.black);
                        g.fillRect(d, c, b, a);
                        break;
                    case SRCCOPY:
                        im = OldBitmapImage(10, params, fr);
                        if (im != null) {
                            g.drawImage(im, d, c, b, a, fr);
                            im = null;
                        } else if (drawCross_if_error) {
                            g.setColor(Color.black);
                            g.drawLine(0, 0, DC.xtransfer(params[7]), DC.ytransfer(params[6]));
                            g.drawLine(DC.xtransfer(params[7]), 0, 0, DC.ytransfer(params[6]));
                        }
                        break;
                    default:
                        System.out.println("unsupported ROP code:" + Integer.toString(e, 16));
                }
                break;
            case META_DIBCREATEPATTERNBRUSH:
                if (debug) {
                    System.out.println("MetaDibCreatePatternBrush:" + params[0]);
                }
                im = DIBBitmapImage(2, params, fr);
                if (im != null) {
                    add_handle(new WmfDecObj(im));
                } else {
                    System.out.println("Error in MetaDibCreatePatternBrush");
                }
                break;
            case META_DIBBITBLT:
            case META_STRETCHDIB:
            case META_DIBSTRETCHBLT:
                k = 0;
                switch(rdFunc) {
                    case META_DIBBITBLT:
                        k = -2;
                        if (debug) {
                            System.out.println("MetaDibBitBlt");
                        }
                        break;
                    case META_STRETCHDIB:
                        k = 1;
                        if (debug) {
                            System.out.println("MetaStretchDib");
                        }
                        break;
                    case META_DIBSTRETCHBLT:
                        k = 0;
                        if (debug) {
                            System.out.println("MetaDibStretchBlt");
                        }
                        break;
                }
                a = DC.ytransfer(params[6 + k]);
                b = DC.xtransfer(params[7 + k]);
                c = DC.ytransfer(params[8 + k]);
                d = DC.xtransfer(params[9 + k]);
                e = (params[1] << 16) + params[0];
                if (debug) {
                    System.out.println("dest X= " + d);
                    System.out.println("dest Y= " + c);
                    System.out.println("width = " + b);
                    System.out.println("height= " + a);
                }
                switch(e) {
                    case WHITENESS:
                        g.setColor(Color.white);
                        g.fillRect(d, c, b, a);
                        break;
                    case BLACKNESS:
                        g.setColor(Color.black);
                        g.fillRect(d, c, b, a);
                        break;
                    case SRCCOPY:
                        im = DIBBitmapImage(10 + k, params, fr);
                        if (im != null) {
                            g.drawImage(im, d, c, b, a, fr);
                            im = null;
                        } else if (drawCross_if_error) {
                            g.setColor(Color.black);
                            g.drawLine(d, c, d + b, c + a);
                            g.drawLine(d + b, c, d, c + a);
                        }
                        break;
                    default:
                        System.out.println("unsupported ROP code:" + Integer.toString(e, 16));
                }
                break;
            case META_ESCAPE:
                switch(params[0]) {
                    case MFCOMMENT:
                        if (debug) {
                            text = new byte[params[1]];
                            for (i = 0; i < params[1]; i++) {
                                if (i % 2 == 0) {
                                    text[i] = (byte) getLoByteVal(params[i / 2 + 2]);
                                } else {
                                    text[i] = (byte) getHiByteVal(params[i / 2 + 2]);
                                }
                                if (text[i] == 0) {
                                    break;
                                }
                            }
                            s = new String(text, 0);
                            System.out.println("MetaEscape/MFCOMMENT: " + s);
                        }
                        break;
                    default:
                        if (debug) {
                            System.out.println("MetaEscape #" + params[0] + " " + ((params[1] + 1) >>> 2) + " Words");
                        }
                }
                break;
            case 0:
                return false;
            default:
                showparams(params, rdSize, rdFunc);
                break;
        }
        return true;
    }

    private void drawOpaqePattern(Graphics g, Image im, int x1, int y1, int x2, int y2, ImageObserver fr) {
        int width = x2 - x1;
        int height = y2 - y1;
        int i, j;
        Graphics g2 = g.create(x1 - x1 % 8, y1 - y1 % 8, width + 8, height + 8);
        g2.clipRect(x1 % 8, y1 % 8, width, height);
        for (i = 0; i < width + 1; i += 8) {
            for (j = 0; j < height + 1; j += 8) {
                g2.drawImage(im, i, j, fr);
            }
        }
        g2.dispose();
    }

    private int getHiByteVal(int hhh) {
        byte b;
        if (hhh > 0) {
            b = (byte) (hhh / 256);
        } else {
            int iii = ~hhh;
            b = (byte) (iii >>> 8);
            b = (byte) ((byte) 255 - b);
        }
        return b < 0 ? (int) b + 256 : b;
    }

    private int getLoByteVal(int hhh) {
        byte b;
        if (hhh > 0) {
            b = (byte) (hhh % 256);
        } else {
            int iii = ~hhh;
            b = (byte) (iii & 0xff);
            b = (byte) ((byte) 255 - b);
        }
        return b < 0 ? (int) b + 256 : b;
    }

    private int transform(int param, int minsize) {
        int i = param;
        if (i < 0) {
            i = -i;
        }
        try {
            i = (i * res) / inch;
            if (i < minsize) {
                i = minsize;
            }
        } catch (ArithmeticException ex) {
        }
        return i;
    }

    private void showparams(short[] params, int recSize, int Func) {
        System.out.println("MetaRecord: " + Integer.toString(Func, 16) + " RecSize=" + recSize);
        System.out.print("Data: ");
        for (int i = 0; i < recSize - 3; i++) {
            if (i < 16) {
                System.out.print(Integer.toString(params[i], 16) + " ");
            }
        }
        System.out.println();
    }

    private int add_handle(WmfDecObj x) {
        int i;
        for (i = 0; i < obj; i++) {
            if (gdiObj[i] == null) {
                gdiObj[i] = x;
                if (debug) {
                    System.out.println("Handle: " + i + "Obj: " + x);
                }
                return i;
            }
        }
        return -1;
    }

    private int readInt32(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
    }

    private short readInt16(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (short) ((ch2 << 8) + (ch1 << 0));
    }

    private int arcus(int ank, int geg) {
        int val = -(int) (Math.atan((float) (geg) / (float) (ank)) * 180 / Math.PI);
        if (ank < 0) {
            val += 180;
        } else if (geg >= 0) {
            val += 360;
        }
        return val;
    }

    private Image DIBBitmapImage(int off, short params[], Component comp) {
        int width = params[off + 2];
        int height = params[off + 4];
        int size = width * height;
        int bpp = params[off + 7];
        int colors, i, j, k, l, m, x = 0, startbitmap;
        if (params[off + 0] != 40 || params[off + 1] != 0) {
            System.out.println("unsupported data format");
            return null;
        }
        if (params[off + 6] != 1) {
            System.out.println("not supported: planes=" + params[off + 17]);
            return null;
        }
        if (bpp != 4 && bpp != 8 && bpp != 1) {
            System.out.println("not supported: " + bpp + " bits per pixel");
            return null;
        }
        if (params[off + 8] != 0 || params[off + 9] != 0) {
            System.out.println("not supported: RLE-compression");
            return null;
        }
        colors = params[off + 16] != 0 ? params[off + 16] : (1 << bpp);
        int palette[] = new int[colors];
        for (i = 0; i < colors; i++) {
            x = params[off + 21 + 2 * i];
            palette[i] = x << 16;
            x = getHiByteVal(params[off + 20 + 2 * i]);
            palette[i] += x << 8;
            x = getLoByteVal(params[off + 20 + 2 * i]);
            palette[i] += x;
            palette[i] -= 0x1000000;
        }
        startbitmap = 20 + off + 2 * colors;
        int pixels[] = new int[size];
        if (debug) {
            System.out.println("bpp = " + bpp);
        }
        switch(bpp) {
            case 1:
                for (k = height - 1, i = 0; k >= 0; k--) {
                    for (l = 0; l < width; l += 16) {
                        i++;
                        m = params[i - 1 + startbitmap];
                        if (m < 0) {
                            m += 65536;
                        }
                        for (j = 0, m = (m >> 8) | (m << 8); j + l < width && j < 16; j++) {
                            pixels[k * width + l + j] = (m & 0x8000) == 0 ? -0x1000000 : -0x1;
                            m <<= 1;
                        }
                        if (i % 2 != 0) {
                            i++;
                        }
                    }
                }
                break;
            case 4:
                for (k = height - 1, i = 0; k >= 0; k--) {
                    for (l = 0; l < width; l++) {
                        switch(l % 4) {
                            case 0:
                                i++;
                                x = getLoByteVal(params[i - 1 + startbitmap]) >>> 4;
                                break;
                            case 1:
                                x = getLoByteVal(params[i - 1 + startbitmap]) & 0xf;
                                break;
                            case 2:
                                x = getHiByteVal(params[i - 1 + startbitmap]) >>> 4;
                                break;
                            case 3:
                                x = getHiByteVal(params[i - 1 + startbitmap]) & 0xf;
                                break;
                        }
                        pixels[k * width + l] = palette[x];
                    }
                }
                break;
            case 8:
                for (k = height - 1, i = 0; k >= 0; k--) {
                    for (l = 0; l < width; l++) {
                        switch(l % 2) {
                            case 0:
                                i++;
                                x = getLoByteVal(params[i - 1 + startbitmap]);
                                break;
                            case 1:
                                x = getHiByteVal(params[i - 1 + startbitmap]);
                                break;
                        }
                        pixels[k * width + l] = palette[x];
                    }
                    if (i % 2 != 0) {
                        i++;
                    }
                }
                break;
        }
        Image im = comp.createImage(new MemoryImageSource(width, height, pixels, 0, width));
        pixels = null;
        return im;
    }

    private Image OldBitmapImage(int off, short params[], Component comp) {
        int width = params[off];
        int height = params[off + 1];
        int i, j, k, l, m;
        if ((params[off + 3] != 1) || (params[off + 4] != 1)) {
            System.out.println("sorry, the only supported format is: planes=1,bpp=1");
            return null;
        }
        int pixels[] = new int[width * height];
        for (k = 0, i = 0; k < height; k++) {
            for (l = 0; l < width; l += 16) {
                m = params[off + 5 + i++];
                if (m < 0) {
                    m += 65536;
                }
                for (j = 0, m = (m >> 8) | (m << 8); j + l < width && j < 16; j++) {
                    pixels[k * width + l + j] = (m & 0x8000) == 0 ? -0x1000000 : -0x1;
                    m <<= 1;
                }
            }
        }
        Image im = comp.createImage(new MemoryImageSource(width, height, pixels, 0, width));
        pixels = null;
        return im;
    }

    private static final int META_SETBKCOLOR = 0x0201;

    private static final int META_SETBKMODE = 0x0102;

    private static final int META_SETMAPMODE = 0x0103;

    private static final int META_SETROP2 = 0x0104;

    private static final int META_SETRELABS = 0x0105;

    private static final int META_SETPOLYFILLMODE = 0x0106;

    private static final int META_SETSTRETCHBLTMODE = 0x0107;

    private static final int META_SETTEXTCHAREXTRA = 0x0108;

    private static final int META_SETTEXTCOLOR = 0x0209;

    private static final int META_SETTEXTJUSTIFICATION = 0x020A;

    private static final int META_SETWINDOWORG = 0x020B;

    private static final int META_SETWINDOWEXT = 0x020C;

    private static final int META_SETVIEWPORTORG = 0x020D;

    private static final int META_SETVIEWPORTEXT = 0x020E;

    private static final int META_OFFSETWINDOWORG = 0x020F;

    private static final int META_SCALEWINDOWEXT = 0x0410;

    private static final int META_OFFSETVIEWPORTORG = 0x0211;

    private static final int META_SCALEVIEWPORTEXT = 0x0412;

    private static final int META_LINETO = 0x0213;

    private static final int META_MOVETO = 0x0214;

    private static final int META_EXCLUDECLIPRECT = 0x0415;

    private static final int META_INTERSECTCLIPRECT = 0x0416;

    private static final int META_ARC = 0x0817;

    private static final int META_ELLIPSE = 0x0418;

    private static final int META_FLOODFILL = 0x0419;

    private static final int META_PIE = 0x081A;

    private static final int META_RECTANGLE = 0x041B;

    private static final int META_ROUNDRECT = 0x061C;

    private static final int META_PATBLT = 0x061D;

    private static final int META_SAVEDC = 0x001E;

    private static final int META_SETPIXEL = 0x041F;

    private static final int META_OFFSETCLIPRGN = 0x0220;

    private static final int META_TEXTOUT = 0x0521;

    private static final int META_BITBLT = 0x0922;

    private static final int META_STRETCHBLT = 0x0B23;

    private static final int META_POLYGON = 0x0324;

    private static final int META_POLYLINE = 0x0325;

    private static final int META_ESCAPE = 0x0626;

    private static final int META_RESTOREDC = 0x0127;

    private static final int META_FILLREGION = 0x0228;

    private static final int META_FRAMEREGION = 0x0429;

    private static final int META_INVERTREGION = 0x012A;

    private static final int META_PAINTREGION = 0x012B;

    private static final int META_SELECTCLIPREGION = 0x012C;

    private static final int META_SELECTOBJECT = 0x012D;

    private static final int META_SETTEXTALIGN = 0x012E;

    private static final int META_DRAWTEXT = 0x062F;

    private static final int META_CHORD = 0x0830;

    private static final int META_SETMAPPERFLAGS = 0x0231;

    private static final int META_EXTTEXTOUT = 0x0A32;

    private static final int META_SETDIBTODEV = 0x0D33;

    private static final int META_SELECTPALETTE = 0x0234;

    private static final int META_REALIZEPALETTE = 0x0035;

    private static final int META_ANIMATEPALETTE = 0x0436;

    private static final int META_SETPALENTRIES = 0x0037;

    private static final int META_POLYPOLYGON = 0x0538;

    private static final int META_RESIZEPALETTE = 0x0139;

    private static final int META_DIBBITBLT = 0x0940;

    private static final int META_DIBSTRETCHBLT = 0x0B41;

    private static final int META_DIBCREATEPATTERNBRUSH = 0x0142;

    private static final int META_STRETCHDIB = 0x0F43;

    private static final int META_EXTFLOODFILL = 0x0548;

    private static final int META_RESETDC = 0x014C;

    private static final int META_STARTDOC = 0x014D;

    private static final int META_STARTPAGE = 0x004F;

    private static final int META_ENDPAGE = 0x0050;

    private static final int META_ABORTDOC = 0x0052;

    private static final int META_ENDDOC = 0x005E;

    private static final int META_DELETEOBJECT = 0x01F0;

    private static final int META_CREATEPALETTE = 0x00F7;

    private static final int META_CREATEBRUSH = 0x00F8;

    private static final int META_CREATEPATTERNBRUSH = 0x01F9;

    private static final int META_CREATEPENINDIRECT = 0x02FA;

    private static final int META_CREATEFONTINDIRECT = 0x02FB;

    private static final int META_CREATEBRUSHINDIRECT = 0x02FC;

    private static final int META_CREATEBITMAPINDIRECT = 0x02FD;

    private static final int META_CREATEBITMAP = 0x06FE;

    private static final int META_CREATEREGION = 0x06FF;

    private static final int MFCOMMENT = 15;

    private static final int SRCCOPY = 0xCC0020;

    private static final int PATCOPY = 0xF00021;

    private static final int PATINVERT = 0x5A0049;

    private static final int DSTINVERT = 0x550009;

    private static final int BLACKNESS = 0x000042;

    private static final int WHITENESS = 0xFF0062;

    private static final int BI_RLE8 = 1;

    private static final int BI_RLE4 = 2;

    private static final int TA_BASELINE = 24;

    private static final int TA_BOTTOM = 8;

    private static final int TA_CENTER = 6;

    private static final int TA_UPDATECP = 1;

    static final int TA_TOP = 0;

    static final int OPAQUE = 2;

    static final int TRANSPARENT = 1;

    static final int ETO_GRAYED = 1;

    static final int ETO_OPAQUE = 2;

    static final int ETO_CLIPPED = 4;

    static final int PS_SOLID = 0;

    static final int PS_DASH = 1;

    static final int PS_DOT = 2;

    static final int PS_DASHDOT = 3;

    static final int PS_DASHDOTDOT = 4;

    static final int PS_NULL = 5;

    static final int PS_INSIDEFRAME = 6;
}

class WmfDecObj {

    static final int M_PEN = 1;

    static final int M_BRUSH = 2;

    static final int M_FONT = 3;

    static final int M_BITMAP = 4;

    static final int M_CLIP = 5;

    static final int M_PALETTE = 6;

    private Color c;

    private Font f;

    private boolean f_underl;

    private int f_orient;

    private Rectangle r;

    private int magic;

    private Image ibrush;

    private int hatch;

    private int p_style;

    WmfDecObj(Color cc, int mm) {
        c = cc;
        magic = mm;
    }

    WmfDecObj(int penattr, Color cc) {
        c = cc;
        magic = M_PEN;
        p_style = penattr;
    }

    WmfDecObj(Font ff, int underlined, int orientation) {
        f = ff;
        f_underl = underlined == 0 ? false : true;
        f_orient = orientation;
        magic = M_FONT;
    }

    WmfDecObj(Image ii) {
        ibrush = ii;
        c = null;
        magic = M_BRUSH;
    }

    WmfDecObj(int hatchstyle, Color cc, Color back, Component fr) {
        c = cc;
        hatch = hatchstyle;
        ibrush = createOpaqueImage(hatchstyle, cc, back, fr);
        magic = M_BRUSH;
    }

    WmfDecObj(int left, int top, int right, int bottom) {
        r = new Rectangle(left, top, right - left, bottom - top);
        magic = M_CLIP;
    }

    Color getColor() {
        return c;
    }

    Image getImage() {
        return ibrush;
    }

    Font getFont() {
        return f;
    }

    boolean isUnderlined() {
        return f_underl;
    }

    int getFontOrientation() {
        return f_orient;
    }

    int getPenStyle() {
        return p_style;
    }

    Rectangle getRect() {
        return r;
    }

    int getMagic() {
        return magic;
    }

    Image createOpaqueImage(int hatchstyle, Color cc, Color back, Component fr) {
        Image im;
        int i, pixels[] = new int[64];
        int set[][] = { { 32, 33, 34, 35, 36, 37, 38, 39 }, { 4, 12, 20, 28, 36, 44, 52, 60 }, { 0, 9, 18, 27, 36, 45, 54, 63 }, { 7, 14, 21, 28, 35, 42, 49, 56 }, { 32, 33, 34, 35, 36, 37, 38, 39, 4, 12, 20, 28, 44, 52, 60 }, { 0, 9, 18, 27, 36, 45, 54, 63, 7, 14, 21, 28, 35, 42, 49, 56 } };
        for (i = 0; i < 64; i++) {
            pixels[i] = Color.white.getRGB();
        }
        try {
            for (i = 0; i < set[hatchstyle].length; i++) {
                pixels[set[hatchstyle][i]] = cc.getRGB();
            }
            MemoryImageSource mis = new MemoryImageSource(8, 8, ColorModel.getRGBdefault(), pixels, 0, 8);
            im = fr.createImage(mis);
            mis = null;
        } catch (ArrayIndexOutOfBoundsException e) {
            im = null;
            System.out.println("unknown hatchstyle found.");
        }
        return im;
    }
}

class WmfDecDC implements Cloneable {

    WmfDecDC(int extX, int extY, int orgX, int orgY) {
        winextX = (short) (truewidth = extX);
        winextY = (short) (trueheight = extY);
        winorgX = (short) orgX;
        winorgY = (short) orgY;
        aktclip = new WmfDecObj(winorgX, winorgY, winextX, winextY);
        aktpen = new WmfDecObj(WmfDecoder.PS_SOLID, Color.black);
        aktbrush = new WmfDecObj(Color.white, WmfDecObj.M_BRUSH);
        aktpal = new WmfDecObj(Color.white, WmfDecObj.M_PALETTE);
        aktbmp = new WmfDecObj(Color.white, WmfDecObj.M_BITMAP);
        aktfont = new WmfDecObj(new Font("Courier", Font.PLAIN, 12), 0, 0);
    }

    public WmfDecObj aktpen, aktbrush, aktpal, aktbmp, aktclip, aktfont;

    public Color akttextc = Color.black;

    public Color aktbackgnd = Color.white;

    public int aktYpos = 0;

    public int aktXpos = 0;

    public short winextX = (short) 1;

    public short winextY = (short) 1;

    public short winorgX = (short) 0;

    public short winorgY = (short) 0;

    public int slevel = 0;

    public int akttextalign = WmfDecoder.TA_TOP;

    public int aktbkmode = WmfDecoder.OPAQUE;

    public Graphics gr;

    private int trueheight, truewidth;

    int ytransfer(short coo) {
        int icoo = coo;
        icoo -= winorgY;
        icoo *= trueheight;
        return icoo / winextY;
    }

    int xtransfer(short coo) {
        int icoo = coo;
        icoo -= winorgX;
        icoo *= truewidth;
        return icoo / winextX;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
