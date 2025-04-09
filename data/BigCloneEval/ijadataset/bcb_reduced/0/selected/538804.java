package org.gzigzag.module;

import org.gzigzag.*;
import java.util.*;
import java.text.*;
import java.io.*;
import java.awt.*;
import gnu.regexp.*;

/** A mailbox file reader.
 * <p>
 * AAAAAAAARGGGGGGGGGGHHHHH!!!!!! How to do 8-bit cleanliness and
 * internationalization correctly? It's still just a stream of bytes
 * that we have underneath. Should probably use encoding when converting
 * bytescroll stuff to strings. Gnnnh...
 * <p>
 * The way to test this is: 
 * <ul> <li> Put a path to the mbox to a cell, put right-hand cursor in it and
 * execute ZZMbox.READMBOX 
 * </ul>
 * <P>
 * TODO: <ul><li>put all headers somewhere (sorted)</li> 
 *           <li>more ways to connect messages (references?)</li>
 *           <li>smart way to move in threads </li>
 *       </ul>
 */
public class ZZMbox {

    public static final String rcsid = "$Id: ZZMbox.java,v 1.32 2000/11/06 12:06:17 ajk Exp $";

    public static boolean dbg = false;

    static final void p(String s) {
        if (dbg) ZZLogger.log(s);
    }

    static final void pa(String s) {
        ZZLogger.log(s);
    }

    static ZZCell byDate;

    static ZZCell bySender;

    static ZZCell bySubject;

    static ZZCell byId;

    static Hashtable hash = new Hashtable();

    public static ZZModule module = new ZZModule() {

        public void action(String id, ZZCell code, ZZCell target, ZZView v, ZZView cv, String key, Point pt, ZZScene xi) {
            try {
                ZZCell viewCell = v.getViewcell();
                ZZCell viewCursor = ZZCursorReal.get(viewCell);
                p("ZZMbox ACTION!");
                if (id.equals("CREATEFLOBWINDOW")) {
                    String[] fields = new String[] { "Subject", "From", "Date", "ARDate" };
                    ZZCell mli = viewCell.getHomeCell().N("d.2", 1);
                    mli.setText("MailflobDims");
                    ZZCell dlc = mli.N("d.1", 1);
                    ZZCell odlc = dlc;
                    ZZCell[] flobdims = new ZZCell[fields.length];
                    for (int i = 0; i < fields.length; i++) {
                        if (i != 0) dlc = dlc.N("d.2", 1);
                        dlc.setText(fields[i]);
                        flobdims[i] = dlc;
                        ZZCell path0 = dlc.N("d.1", 1);
                        ZZCell path1 = path0.N("d.1", 1);
                        path0 = ZZUtil.appendCommand(path0, new Object[] { "FIND", "d.handle", "1", fields[i] + ":" });
                        path0 = ZZUtil.appendCommand(path0, new Object[] { "STEP", "d.1", "1" });
                        path0 = ZZUtil.appendCommand(path0, new Object[] { "HEAD", "d.clone", "-1" });
                        path0 = ZZUtil.appendCommand(path0, new Object[] { "STEP", "d.order", "1" });
                        path1 = ZZUtil.appendCommand(path1, new Object[] { "FIND", "d.handle", "1", fields[i] + ":" });
                        path1 = ZZUtil.appendCommand(path1, new Object[] { "STEP", "d.1", "1" });
                        path1 = ZZUtil.appendCommand(path1, new Object[] { "HEAD", "d.clone", "-1" });
                    }
                    mli = mli.N("d.2", 1);
                    mli.setText("MailSingleRasters");
                    ZZCell om = mli.N("d.1", 1);
                    om.setText("Single message");
                    {
                        ZZCell om1 = om.N("d.1", 1);
                        om1.setText("ZZMbox.onemsg");
                    }
                    mli = mli.N("d.2", 1);
                    mli.setText("MailflobRasters");
                    ZZCell fl = mli.N("d.1", 1);
                    fl.setText("Message space");
                    {
                        ZZCell fl1 = fl.N("d.1", 1);
                        fl1.setText("SimpleFlobRaster");
                        ZZCell dec = fl1.N("d.1", 1).N("d.2", 1);
                        dec.setText("decorator");
                        dec = dec.N("d.1", 1);
                        dec.setText("Something");
                        dec = dec.N("d.1", 1);
                        dec.setText("SimpleFlobConnector");
                        dec = dec.N("d.1", 1);
                        dec.setText("connection");
                        dec = dec.N("d.1", 1);
                        dec = ZZUtil.appendCommand(dec, new Object[] { "FIND", "d.handle", "1", "In-Reply-To:" });
                        dec = ZZUtil.appendCommand(dec, new Object[] { "STEP", "d.1", "1" });
                        dec = ZZUtil.appendCommand(dec, new Object[] { "HEAD", "d.clone", "-1" });
                        dec = ZZUtil.appendCommand(dec, new Object[] { "STEP", "d.1", "-1" });
                        dec = ZZUtil.appendCommand(dec, new Object[] { "HEAD", "d.handle", "-1" });
                    }
                    ZZCell vc1 = ZZDefaultSpace.newToplevelView(viewCell.getSpace(), "mailflob", 0, 0, 500, 500, fl, null, null, viewCell, flobdims, new Color(0x8080ff));
                    ZZCell vc2 = ZZDefaultSpace.newToplevelView(viewCell.getSpace(), "mailsingle", 500, 0, 500, 500, om, null, null, viewCell, null, new Color(0x101020));
                } else if (id.equals("READMBOX")) {
                    StringScroll scr = code.getSpace().getStringScroll("mbox");
                    byte[] barr;
                    String mbox_path = target.getText();
                    p("" + mbox_path);
                    RandomAccessFile f = new RandomAccessFile(mbox_path, "rw");
                    int l = (int) f.length();
                    barr = new byte[l];
                    int got;
                    if ((got = f.read(barr, 0, barr.length)) < l) {
                        throw new ZZError("Not enough data!" + got);
                    }
                    String s = new String(barr, "ISO8859_1");
                    long addr = scr.append(s);
                    long addr2 = scr.curEnd() - 1;
                    ZZCell c = target.N("d.1", 1);
                    byId = target.N("d.2", 1);
                    byId.setText("by id");
                    byId = byId.N("d.1", 1);
                    byDate = target.N("d.2", 1);
                    byDate.setText("by date");
                    byDate = byDate.N("d.1", 1);
                    bySubject = target.N("d.2", 1);
                    bySubject.setText("by subject");
                    bySubject = bySubject.N("d.1", 1);
                    bySender = target.N("d.2", 1);
                    bySender.setText("by sender");
                    bySender = bySender.N("d.1", 1);
                    c = c.N("d.1", 1);
                    c = c.N("d.1", 1);
                    c.setText("s1");
                    parse(scr, c);
                } else {
                    pa("UNKNOWN ZZMBOX COMMAND " + id);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new ZZError("" + e);
            }
        }

        public ZOb newZOb(String id) {
            if (id.equals("onemsg")) return new org.gzigzag.module.mbox.SingleMail();
            return null;
        }
    };

    static RE startws;

    static RE startfrom;

    static RE iline;

    static RE hdrline;

    public static void sort() {
        ZZUtil.Comparator cmp = new ZZUtil.Comparator() {

            public int compare(ZZCell c1, ZZCell c2) {
                if (c1 == null && c2 == null) return 0;
                if (c1 == null) return 1;
                if (c2 == null) return -1;
                int ret = c1.getText().compareTo(c2.getText());
                return ret;
            }
        };
        ZZUtil.sortRank(bySender, "d.byfield", 1, cmp, false, true);
        ZZUtil.sortRank(bySubject, "d.byfield", 1, cmp, false, true);
        ZZUtil.sortRank(byDate, "d.byfield", 1, new ZZUtil.Comparator() {

            public int compare(ZZCell c1, ZZCell c2) {
                SimpleDateFormat df = new SimpleDateFormat();
                Date d1;
                Date d2;
                try {
                    d1 = df.parse(c1.getText());
                    d2 = df.parse(c2.getText());
                } catch (ParseException pe) {
                    pa("EX: " + pe);
                    return 1;
                }
                if (d1.equals(d2)) return 0;
                if (d1.before(d2)) return -1; else return 1;
            }
        }, false, true);
    }

    public static void parse(final StringScroll scr, final ZZCell start) {
        (new Runnable() {

            long offs = 0;

            ZZCell lastCell = start;

            Hashtable ids = new Hashtable();

            String getLine() throws Exception {
                p("getline " + scr.curEnd() + " " + offs);
                int ind = -1;
                if (offs == scr.curEnd()) return null;
                String s = null;
                for (int i = 100; ind < 0 && i < scr.curEnd() - offs; i += 100) {
                    if (offs + i >= scr.curEnd()) {
                        i = (int) (scr.curEnd() - offs - 1);
                    }
                    s = scr.getString(offs, i);
                    ind = s.indexOf('\n');
                }
                if (ind < 0 && offs != scr.curEnd()) {
                    p("Problem: no newline at end!");
                    s = scr.getString(offs, (int) scr.curEnd());
                    offs = scr.curEnd();
                    return s;
                }
                s = s.substring(0, ind);
                offs += ind + 1;
                return s;
            }

            long hlstart;

            long hlend;

            String getHdrLine() throws Exception {
                hlstart = offs;
                long o0 = offs;
                String s = getLine();
                if (s.equals("")) {
                    return null;
                }
                if (startws == null) startws = new RE("^\\s");
                if (startws.getMatch(s, 0, RE.REG_ANCHORINDEX) != null) {
                    throw new ZZError("ARGH! Invalid header");
                }
                while (true) {
                    o0 = offs;
                    String extra = getLine();
                    if (startws.getMatch(extra, 0, RE.REG_ANCHORINDEX) == null) break;
                    s += extra;
                    p("Hdrline reloop " + extra);
                }
                offs = o0;
                hlend = offs - 2;
                return s;
            }

            boolean was_empty = false;

            String getBodyLine() throws Exception {
                p("Getbody");
                long o0 = offs;
                String s = getLine();
                if (s == null) return null;
                if (startfrom == null) startfrom = new RE("^From (.+) (... ... .. ..:..:.. ....)(.*)");
                if (startfrom.getMatch(s, 0, RE.REG_ANCHORINDEX) != null && was_empty) {
                    p("GETBODY: END BODY " + s);
                    offs = o0;
                    return null;
                }
                p("GETBODYRET: " + s);
                if (s.equals("")) {
                    was_empty = true;
                } else {
                    was_empty = false;
                }
                return s;
            }

            Span sp(long offs, REMatch m, int i) {
                return sp(offs, m.getSubStartIndex(i), m.getSubEndIndex(i));
            }

            Span sp(long offs, long i1, long i2) {
                return sp(i1 + offs, i2 + offs);
            }

            Span sp(long i1, long i2) {
                return Span.create(Address.scrollOffs(scr, i1), Address.scrollOffs(scr, i2));
            }

            void dohoriz(ZZCell c, long o, REMatch m, int n) {
                for (int i = 1; i < n + 1; i++) {
                    c.setSpan(sp(o, m, i));
                    if (i < n - 1) c = c.N("d.1", 1);
                }
            }

            void doMessage() throws Exception {
                lastCell = lastCell.N("d.2", 1);
                boolean received = false;
                ZZCell cur = lastCell;
                long o0 = offs;
                String s = getLine();
                if (iline == null) iline = new RE("^(From) (.*)");
                REMatch m = iline.getMatch(s, 0, RE.REG_ANCHORINDEX);
                String h;
                ZZCell fullhdr = cur;
                long soffs = o0;
                long eoffs = soffs;
                ZZCell ar = null;
                String irt = null;
                while ((h = getHdrLine()) != null) {
                    ZZCell ptr;
                    if (hdrline == null) hdrline = new RE("^(\\S+):(.*)");
                    m = hdrline.getMatch(h, 0, RE.REG_ANCHORINDEX);
                    if (m == null) throw new ZZError("Inv hdr line " + h);
                    String hid = m.toString(1);
                    int poffs = m.getSubStartIndex(2);
                    String hparm = m.toString(2);
                    eoffs = offs;
                    if (!hid.equals("Date") && !hid.equals("From") && !hid.equals("Received") && !hid.equalsIgnoreCase("Message-Id") && !hid.equals("In-Reply-To") && !hid.equals("References") && !hid.equals("Subject")) {
                        continue;
                    }
                    ptr = fullhdr;
                    if (hid.equals("Subject")) {
                        cur = cur.N("d.handle", 1);
                        cur.setText("Subject:");
                        ar = cur.N("d.1", 1);
                        ar.setText(hparm);
                        bySubject.insert("d.byfield", 1, ar);
                    } else if (hid.equals("From")) {
                        cur = cur.N("d.handle", 1);
                        cur.setText("From:");
                        ar = cur.N("d.1", 1);
                        int idx = 0;
                        hparm = hparm.replace('"', ' ');
                        ar.setText(hparm.trim());
                        bySender.insert("d.byfield", 1, ar);
                    } else if (hid.equals("Date")) {
                        cur = cur.N("d.handle", 1);
                        cur.setText("Date:");
                        ar = cur.N("d.1", 1);
                        try {
                            Calendar c = ZZDateParser.parse(hparm);
                            Date d = c.getTime();
                            ar.setText(new SimpleDateFormat().format(d));
                        } catch (NumberFormatException nfe) {
                            pa("Not a rfc822 date! Fix it !");
                            ar.setText(hparm);
                            ar = ar.N("d.1", 1);
                            ar.setText("not RFC822 compliant");
                        }
                        byDate.insert("d.byfield", 1, ar);
                    } else if (hid.equalsIgnoreCase("Message-Id")) {
                        String mi = hparm.trim();
                        p("Id: " + mi);
                        ZZCell t = byId;
                        cur = cur.N("d.handle", 1);
                        cur.setText("Message-Id:");
                        ar = cur.N("d.1", 1);
                        byId.insert("d.byfield", 1, ar);
                        ar.setText(mi);
                        ZZCell old = (ZZCell) ids.get(mi);
                        if (old != null) {
                            ZZCell clone = old.s("d.clone", 1);
                            old.excise("d.clone");
                            if (clone != null) ar.insert("d.clone", 1, clone);
                            old.excise("d.byfield");
                            ids.remove(mi);
                        }
                        ids.put(mi, ar);
                    } else if (hid.equals("In-Reply-To")) {
                        if (irt != null) pa("REFERENCES USED AND NOW I-R-T ");
                        irt = hparm;
                        try {
                            irt = irt.substring(irt.lastIndexOf('<'), irt.lastIndexOf('>') + 1);
                        } catch (StringIndexOutOfBoundsException e) {
                            irt = "";
                        }
                        if (!irt.equals("")) {
                            cur = cur.N("d.handle", 1);
                            cur.setText("In-Reply-To:");
                            ar = cur.N("d.1", 1);
                            ZZCell tmp = byId;
                            ZZCell repliedTo = (ZZCell) ids.get(irt);
                            if (repliedTo == null) {
                                repliedTo = byId.N("d.byfield", 1);
                                repliedTo.setText(irt);
                                ar.setText(irt);
                                ids.put(irt, repliedTo);
                            }
                            repliedTo.insert("d.clone", 1, ar);
                        }
                    } else if (hid.equals("References")) {
                        if (irt != null) {
                            irt = hparm;
                            try {
                                irt = irt.substring(irt.lastIndexOf('<'), irt.lastIndexOf('>') + 1);
                            } catch (StringIndexOutOfBoundsException e) {
                                irt = "";
                            }
                            if (!irt.equals("")) {
                                cur = cur.N("d.handle", 1);
                                cur.setText("In-Reply-To:");
                                ar = cur.N("d.1", 1);
                                ZZCell tmp = byId;
                                ZZCell repliedTo = (ZZCell) ids.get(irt);
                                if (repliedTo == null) {
                                    repliedTo = byId.N("d.byfield", 1);
                                    repliedTo.setText(irt);
                                    ar.setText(irt);
                                    ids.put(irt, repliedTo);
                                }
                                repliedTo.insert("d.clone", 1, ar);
                            }
                        }
                    } else if (hid.equals("Received") && !received) {
                        cur = cur.N("d.handle", 1);
                        cur.setText("ARDate:");
                        ar = cur.N("d.1", 1);
                        String dar = hparm;
                        dar = dar.substring(dar.indexOf(";") + 1, dar.length());
                        try {
                            Calendar c = ZZDateParser.parse(dar);
                            Date d = c.getTime();
                            ar.setText(new SimpleDateFormat().format(d));
                        } catch (NumberFormatException nfe) {
                            pa("Not a rfc822 date!");
                            ar.setText(dar);
                            ar = ar.N("d.1", 1);
                            ar.setText("not RFC822 compliant");
                        }
                        received = true;
                    }
                }
                ZZCell head = fullhdr.N("d.headers", 1);
                head.setSpan(sp(soffs, eoffs - 1));
                String b;
                long bod = offs;
                while ((b = getBodyLine()) != null) {
                }
                cur = cur.N("d.handle", 1);
                cur.setSpan(sp(bod, offs));
            }

            public void run() {
                try {
                    while (offs < scr.curEnd()) {
                        doMessage();
                    }
                } catch (Exception e) {
                    p("" + e);
                    e.printStackTrace();
                }
                sort();
                float date_float = 0;
                float subj_float = 0;
                float from_float = 0;
                ZZCell c = byDate.h("d.byfield", -1);
                if (c != null) {
                    int length = c.getRankLength("d.byfield") - 1;
                    c = c.s("d.byfield", 1);
                    for (int i = 0; i < length; i++) {
                        c.N("d.order", 1).setText("" + date_float);
                        date_float += 1.0 / length;
                        c = c.s("d.byfield", 1);
                    }
                }
                c = bySubject.h("d.byfield", -1);
                if (c != null) {
                    int length = c.getRankLength("d.byfield") - 1;
                    c = c.s("d.byfield", 1);
                    for (int i = 0; i < length; i++) {
                        c.N("d.order", 1).setText("" + subj_float);
                        subj_float = subj_float + ((float) 1) / length;
                        c = c.s("d.byfield", 1);
                    }
                }
                c = bySender.h("d.byfield", -1);
                if (c != null) {
                    int length = c.getRankLength("d.byfield") - 1;
                    c = c.s("d.byfield", 1);
                    for (int i = 0; i < length; i++) {
                        c.N("d.order", 1).setText("" + from_float);
                        from_float += 1.0 / length;
                        c = c.s("d.byfield", 1);
                    }
                }
            }
        }).run();
    }
}
