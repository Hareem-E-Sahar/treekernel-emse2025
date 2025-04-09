package com.sun.opengl.impl.glu.nurbs;

/**
 * Class working with curves and surfaces
 * @author Tomas Hrasky
 *
 */
public class Subdivider {

    /**
   * Cull type 
   */
    public static final int CULL_TRIVIAL_REJECT = 0;

    /**
   * Cull type
   */
    public static final int CULL_ACCEPT = 1;

    /**
   * Maximum trimming arcs
   */
    private static final int MAXARCS = 10;

    /**
   * Linked list of Quilts
   */
    Quilt qlist;

    /**
   * Object holding rendering honts information
   */
    private Renderhints renderhints;

    /**
   * Backend object
   */
    private Backend backend;

    /**
   * Number of subdivisions
   */
    private int subdivisions;

    /**
   * U step when using domain distance sampling 
   */
    private float domain_distance_u_rate;

    /**
   * Use domain distance sampling
   */
    private int is_domain_distance_sampling;

    /**
   * Initial class holding trimming arcs
   */
    private Bin initialbin;

    /**
   * Not used
   */
    private boolean showDegenerate;

    /**
   * Is triming arc type bezier arc
   */
    private boolean isArcTypeBezier;

    /**
   * Breakpoints in v direction
   */
    private Flist tpbrkpts;

    /**
   * Breakpoints in u direction
   */
    private Flist spbrkpts;

    /**
   * Unused
   */
    private int s_index;

    /**
   * Head of linked list of trimming arcs
   */
    private Arc pjarc;

    /**
   * Class tesselating trimming arcs
   */
    private ArcTesselator arctesselator;

    /**
   * Unused
   */
    private int t_index;

    /**
   * Breakpoints
   */
    private Flist smbrkpts;

    /**
   * Not used
   */
    private float[] stepsizes;

    /**
   * Domain distance in V direction
   */
    private float domain_distance_v_rate;

    /**
   * Initializes quilt list
   */
    public void beginQuilts(Backend backend) {
        qlist = null;
        renderhints = new Renderhints();
        this.backend = backend;
        initialbin = new Bin();
        arctesselator = new ArcTesselator();
    }

    /**
   * Adds quilt to linked list
   * @param quilt added quilt
   */
    public void addQuilt(Quilt quilt) {
        if (qlist == null) qlist = quilt; else {
            quilt.next = qlist;
            qlist = quilt;
        }
    }

    /**
   * Empty method
   */
    public void endQuilts() {
    }

    /**
   * Draws a surface
   */
    public void drawSurfaces() {
        renderhints.init();
        if (qlist == null) {
            return;
        }
        for (Quilt q = qlist; q != null; q = q.next) {
            if (q.isCulled() == CULL_TRIVIAL_REJECT) {
                freejarcs(initialbin);
                return;
            }
        }
        float[] from = new float[2];
        float[] to = new float[2];
        spbrkpts = new Flist();
        tpbrkpts = new Flist();
        qlist.getRange(from, to, spbrkpts, tpbrkpts);
        boolean optimize = (is_domain_distance_sampling > 0 && (renderhints.display_method != NurbsConsts.N_OUTLINE_PATCH));
        optimize = true;
        if (!initialbin.isnonempty()) {
            if (!optimize) {
                makeBorderTrim(from, to);
            }
        } else {
            float[] rate = new float[2];
            qlist.findRates(spbrkpts, tpbrkpts, rate);
        }
        backend.bgnsurf(renderhints.wiretris, renderhints.wirequads);
        if (!initialbin.isnonempty() && optimize) {
            int i, j;
            int num_u_steps;
            int num_v_steps;
            for (i = spbrkpts.start; i < spbrkpts.end - 1; i++) {
                for (j = tpbrkpts.start; j < tpbrkpts.end - 1; j++) {
                    float[] pta = new float[2];
                    float[] ptb = new float[2];
                    pta[0] = spbrkpts.pts[i];
                    ptb[0] = spbrkpts.pts[i + 1];
                    pta[1] = tpbrkpts.pts[j];
                    ptb[1] = tpbrkpts.pts[j + 1];
                    qlist.downloadAll(pta, ptb, backend);
                    num_u_steps = (int) (domain_distance_u_rate * (ptb[0] - pta[0]));
                    num_v_steps = (int) (domain_distance_v_rate * (ptb[1] - pta[1]));
                    if (num_u_steps <= 0) num_u_steps = 1;
                    if (num_v_steps <= 0) num_v_steps = 1;
                    backend.surfgrid(pta[0], ptb[0], num_u_steps, ptb[1], pta[1], num_v_steps);
                    backend.surfmesh(0, 0, num_u_steps, num_v_steps);
                }
            }
        } else subdivideInS(initialbin);
        backend.endsurf();
    }

    /**
   * Empty method
   * @param initialbin2
   */
    private void freejarcs(Bin initialbin2) {
    }

    /**
   * Subdivide in U direction
   * @param source Trimming arcs source
   */
    private void subdivideInS(Bin source) {
        if (renderhints.display_method == NurbsConsts.N_OUTLINE_PARAM) {
            outline(source);
            freejarcs(source);
        } else {
            setArcTypeBezier();
            setNonDegenerate();
            splitInS(source, spbrkpts.start, spbrkpts.end);
        }
    }

    /**
   * Split in U direction
   * @param source Trimming arcs source
   * @param start breakpoints start
   * @param end breakpoints end
   */
    private void splitInS(Bin source, int start, int end) {
        if (source.isnonempty()) {
            if (start != end) {
                int i = start + (end - start) / 2;
                Bin left = new Bin();
                Bin right = new Bin();
                split(source, left, right, 0, spbrkpts.pts[i]);
                splitInS(left, start, i);
                splitInS(right, i + 1, end);
            } else {
                if (start == spbrkpts.start || start == spbrkpts.end) {
                    freejarcs(source);
                } else if (renderhints.display_method == NurbsConsts.N_OUTLINE_PARAM_S) {
                    outline(source);
                    freejarcs(source);
                } else {
                    setArcTypeBezier();
                    setNonDegenerate();
                    s_index = start;
                    splitInT(source, tpbrkpts.start, tpbrkpts.end);
                }
            }
        } else {
        }
    }

    /**
   * Split in V direction
   * @param source
   * @param start
   * @param end
   */
    private void splitInT(Bin source, int start, int end) {
        if (source.isnonempty()) {
            if (start != end) {
                int i = start + (end - start) / 2;
                Bin left = new Bin();
                Bin right = new Bin();
                split(source, left, right, 1, tpbrkpts.pts[i + 1]);
                splitInT(left, start, i);
                splitInT(right, i + 1, end);
            } else {
                if (start == tpbrkpts.start || start == tpbrkpts.end) {
                    freejarcs(source);
                } else if (renderhints.display_method == NurbsConsts.N_OUTLINE_PARAM_ST) {
                    outline(source);
                    freejarcs(source);
                } else {
                    t_index = start;
                    setArcTypeBezier();
                    setDegenerate();
                    float[] pta = new float[2];
                    float[] ptb = new float[2];
                    pta[0] = spbrkpts.pts[s_index - 1];
                    pta[1] = tpbrkpts.pts[t_index - 1];
                    ptb[0] = spbrkpts.pts[s_index];
                    ptb[1] = tpbrkpts.pts[t_index];
                    qlist.downloadAll(pta, ptb, backend);
                    Patchlist patchlist = new Patchlist(qlist, pta, ptb);
                    samplingSplit(source, patchlist, renderhints.maxsubdivisions, 0);
                    setNonDegenerate();
                    setArcTypeBezier();
                }
            }
        }
    }

    /**
   * Sample 
   * @param source
   * @param patchlist
   * @param subdivisions
   * @param param
   */
    private void samplingSplit(Bin source, Patchlist patchlist, int subdivisions, int param) {
        if (!source.isnonempty()) return;
        if (patchlist.cullCheck() == CULL_TRIVIAL_REJECT) {
            freejarcs(source);
            return;
        }
        patchlist.getstepsize();
        if (renderhints.display_method == NurbsConsts.N_OUTLINE_PATCH) {
            tesselation(source, patchlist);
            outline(source);
            freejarcs(source);
            return;
        }
        tesselation(source, patchlist);
        if (patchlist.needsSamplingSubdivision() && subdivisions > 0) {
            if (!patchlist.needsSubdivision(0)) {
                param = 1;
            } else if (patchlist.needsSubdivision(1)) param = 0; else param = 1 - param;
            Bin left = new Bin();
            Bin right = new Bin();
            float mid = (float) ((patchlist.pspec[param].range[0] + patchlist.pspec[param].range[1]) * .5);
            split(source, left, right, param, mid);
            Patchlist subpatchlist = new Patchlist(patchlist, param, mid);
            samplingSplit(left, subpatchlist, subdivisions - 1, param);
            samplingSplit(right, subpatchlist, subdivisions - 1, param);
        } else {
            setArcTypePwl();
            setDegenerate();
            nonSamplingSplit(source, patchlist, subdivisions, param);
            setDegenerate();
            setArcTypeBezier();
        }
    }

    /**
   * Not used
   * @param source
   * @param patchlist
   * @param subdivisions
   * @param param
   */
    private void nonSamplingSplit(Bin source, Patchlist patchlist, int subdivisions, int param) {
        if (patchlist.needsNonSamplingSubdivision() && subdivisions > 0) {
            param = 1 - param;
            Bin left = new Bin();
            Bin right = new Bin();
            float mid = (float) ((patchlist.pspec[param].range[0] + patchlist.pspec[param].range[1]) * .5);
            split(source, left, right, param, mid);
            Patchlist subpatchlist = new Patchlist(patchlist, param, mid);
            if (left.isnonempty()) {
                if (subpatchlist.cullCheck() == CULL_TRIVIAL_REJECT) freejarcs(left); else nonSamplingSplit(left, subpatchlist, subdivisions - 1, param);
            }
            if (right.isnonempty()) {
                if (patchlist.cullCheck() == CULL_TRIVIAL_REJECT) freejarcs(right); else nonSamplingSplit(right, subpatchlist, subdivisions - 1, param);
            }
        } else {
            patchlist.bbox();
            backend.patch(patchlist.pspec[0].range[0], patchlist.pspec[0].range[1], patchlist.pspec[1].range[0], patchlist.pspec[1].range[1]);
            if (renderhints.display_method == NurbsConsts.N_OUTLINE_SUBDIV) {
                outline(source);
                freejarcs(source);
            } else {
                setArcTypePwl();
                setDegenerate();
                findIrregularS(source);
                monosplitInS(source, smbrkpts.start, smbrkpts.end);
            }
        }
    }

    /**
   * Not used
   * @param source
   * @param start
   * @param end
   */
    private void monosplitInS(Bin source, int start, int end) {
    }

    /**
   * Not used
   * @param source
   */
    private void findIrregularS(Bin source) {
    }

    /**
   * Not used
   */
    private void setArcTypePwl() {
    }

    /**
   * Not used
   * @param source
   * @param patchlist
   */
    private void tesselation(Bin source, Patchlist patchlist) {
    }

    /**
   * Not used
   */
    private void setDegenerate() {
    }

    /**
   * Not used
   * @param bin
   * @param left
   * @param right
   * @param param
   * @param value
   */
    private void split(Bin bin, Bin left, Bin right, int param, float value) {
        Bin intersections = new Bin();
        Bin unknown = new Bin();
        partition(bin, left, intersections, right, unknown, param, value);
        int count = intersections.numarcs();
        if (count % 2 == 0) {
            Arc[] arclist = new Arc[MAXARCS];
            CArrayOfArcs list;
            if (count >= MAXARCS) {
                list = new CArrayOfArcs(new Arc[count]);
            } else {
                list = new CArrayOfArcs(arclist);
            }
            CArrayOfArcs last, lptr;
            Arc jarc;
            for (last = new CArrayOfArcs(list); (jarc = intersections.removearc()) != null; last.pp()) last.set(jarc);
            if (param == 0) {
                ArcSdirSorter sorter = new ArcSdirSorter(this);
                sorter.qsort(list, count);
                for (lptr = new CArrayOfArcs(list); lptr.getPointer() < last.getPointer(); lptr.raisePointerBy(2)) check_s(lptr.get(), lptr.getRelative(1));
                for (lptr = new CArrayOfArcs(list); lptr.getPointer() < last.getPointer(); lptr.raisePointerBy(2)) join_s(left, right, lptr.get(), lptr.getRelative(1));
                for (lptr = new CArrayOfArcs(list); lptr.getPointer() != last.getPointer(); lptr.pp()) {
                    if (lptr.get().head()[0] <= value && lptr.get().tail()[0] <= value) left.addarc(lptr.get()); else right.addarc(lptr.get());
                }
            } else {
                ArcTdirSorter sorter = new ArcTdirSorter(this);
                sorter.qsort(list, count);
                for (lptr = new CArrayOfArcs(list); lptr.getPointer() < last.getPointer(); lptr.raisePointerBy(2)) check_t(lptr.get(), lptr.getRelative(1));
                for (lptr = new CArrayOfArcs(list); lptr.getPointer() < last.getPointer(); lptr.raisePointerBy(2)) join_t(left, right, lptr.get(), lptr.getRelative(1));
                for (lptr = new CArrayOfArcs(list); lptr.getPointer() != last.getPointer(); lptr.raisePointerBy(2)) {
                    if (lptr.get().head()[0] <= value && lptr.get().tail()[0] <= value) left.addarc(lptr.get()); else right.addarc(lptr.get());
                }
            }
            unknown.adopt();
        }
    }

    /**
   * Not used
   * @param left
   * @param right
   * @param arc
   * @param relative
   */
    private void join_t(Bin left, Bin right, Arc arc, Arc relative) {
    }

    /**
   * Not used
   * @param arc
   * @param relative
   */
    private void check_t(Arc arc, Arc relative) {
    }

    /**
   * Not used
   * @param left
   * @param right
   * @param jarc1
   * @param jarc2
   */
    private void join_s(Bin left, Bin right, Arc jarc1, Arc jarc2) {
        if (!jarc1.getitail()) jarc1 = jarc1.next;
        if (!jarc2.getitail()) jarc2 = jarc2.next;
        float s = jarc1.tail()[0];
        float t1 = jarc1.tail()[1];
        float t2 = jarc2.tail()[1];
        if (t1 == t2) {
            simplelink(jarc1, jarc2);
        } else {
            Arc newright = new Arc(Arc.ARC_RIGHT);
            Arc newleft = new Arc(Arc.ARC_LEFT);
            if (isBezierArcType()) {
                arctesselator.bezier(newright, s, s, t1, t2);
                arctesselator.bezier(newleft, s, s, t2, t1);
            } else {
                arctesselator.pwl_right(newright, s, t1, t2, stepsizes[0]);
                arctesselator.pwl_left(newright, s, t2, t1, stepsizes[2]);
            }
            link(jarc1, jarc2, newright, newleft);
            left.addarc(newright);
            right.addarc(newleft);
        }
    }

    /**
   * Not used
   * @param jarc1
   * @param jarc2
   * @param newright
   * @param newleft
   */
    private void link(Arc jarc1, Arc jarc2, Arc newright, Arc newleft) {
    }

    /**
   * Not used
   * @return true
   */
    private boolean isBezierArcType() {
        return true;
    }

    /**
   * Not used
   * @param jarc1
   * @param jarc2
   */
    private void simplelink(Arc jarc1, Arc jarc2) {
    }

    /**
   * Not used
   * @param arc
   * @param relative
   */
    private void check_s(Arc arc, Arc relative) {
    }

    /**
   * Not used
   * @param bin
   * @param left
   * @param intersections
   * @param right
   * @param unknown
   * @param param
   * @param value
   */
    private void partition(Bin bin, Bin left, Bin intersections, Bin right, Bin unknown, int param, float value) {
        Bin headonleft = new Bin();
        Bin headonright = new Bin();
        Bin tailonleft = new Bin();
        Bin tailonright = new Bin();
        for (Arc jarc = bin.removearc(); jarc != null; jarc = bin.removearc()) {
            float tdiff = jarc.tail()[param] - value;
            float hdiff = jarc.head()[param] - value;
            if (tdiff > 0) {
                if (hdiff > 0) {
                    right.addarc(jarc);
                } else if (hdiff == 0) {
                    tailonright.addarc(jarc);
                } else {
                    Arc jtemp;
                    switch(arc_split(jarc, param, value, 0)) {
                        case 2:
                            tailonright.addarc(jarc);
                            headonleft.addarc(jarc.next);
                            break;
                        default:
                            System.out.println("TODO subdivider.partition rest cases");
                            break;
                    }
                }
            } else if (tdiff == 0) {
                if (hdiff > 0) {
                    headonright.addarc(jarc);
                } else if (hdiff == 0) {
                    unknown.addarc(jarc);
                } else {
                    headonright.addarc(jarc);
                }
            } else {
                if (hdiff > 0) {
                } else if (hdiff == 0) {
                    tailonleft.addarc(jarc);
                } else {
                    left.addarc(jarc);
                }
            }
        }
        if (param == 0) {
            classify_headonleft_s(headonleft, intersections, left, value);
            classify_tailonleft_s(tailonleft, intersections, left, value);
            classify_headonright_s(headonright, intersections, right, value);
            classify_tailonright_s(tailonright, intersections, right, value);
        } else {
            classify_headonleft_t(headonleft, intersections, left, value);
            classify_tailonleft_t(tailonleft, intersections, left, value);
            classify_headonright_t(headonright, intersections, right, value);
            classify_tailonright_t(tailonright, intersections, right, value);
        }
    }

    /**
   * Not used
   * @param tailonright
   * @param intersections
   * @param right
   * @param value
   */
    private void classify_tailonright_t(Bin tailonright, Bin intersections, Bin right, float value) {
    }

    /**
   * Not used
   * @param bin
   * @param in
   * @param out
   * @param val
   */
    private void classify_tailonleft_s(Bin bin, Bin in, Bin out, float val) {
        Arc j;
        while ((j = bin.removearc()) != null) {
            j.clearitail();
            float diff = j.next.head()[0] - val;
            if (diff > 0) {
                in.addarc(j);
            } else if (diff < 0) {
                if (ccwTurn_sl(j, j.next)) out.addarc(j); else in.addarc(j);
            } else {
                if (j.next.tail()[1] > j.next.head()[1]) in.addarc(j); else out.addarc(j);
            }
        }
    }

    /**
   * Not used
   * @param bin
   * @param in
   * @param out
   * @param val
   */
    private void classify_headonright_s(Bin bin, Bin in, Bin out, float val) {
        Arc j;
        while ((j = bin.removearc()) != null) {
            j.setitail();
            float diff = j.prev.tail()[0] - val;
            if (diff > 0) {
                if (ccwTurn_sr(j.prev, j)) out.addarc(j); else in.addarc(j);
            } else if (diff < 0) {
                out.addarc(j);
            } else {
                if (j.prev.tail()[1] > j.prev.head()[1]) out.addarc(j); else in.addarc(j);
            }
        }
    }

    /**
   * Not used
   * @param prev
   * @param j
   * @return false
   */
    private boolean ccwTurn_sr(Arc prev, Arc j) {
        return false;
    }

    /**
   * Not used
   * @param headonright
   * @param intersections
   * @param right
   * @param value
   */
    private void classify_headonright_t(Bin headonright, Bin intersections, Bin right, float value) {
    }

    /**
   * Not used
   * @param tailonleft
   * @param intersections
   * @param left
   * @param value
   */
    private void classify_tailonleft_t(Bin tailonleft, Bin intersections, Bin left, float value) {
    }

    /**
   * Not used
   * @param bin
   * @param in
   * @param out
   * @param val
   */
    private void classify_headonleft_t(Bin bin, Bin in, Bin out, float val) {
        Arc j;
        while ((j = bin.removearc()) != null) {
            j.setitail();
            float diff = j.prev.tail()[1] - val;
            if (diff > 0) {
                out.addarc(j);
            } else if (diff < 0) {
                if (ccwTurn_tl(j.prev, j)) out.addarc(j); else in.addarc(j);
            } else {
                if (j.prev.tail()[0] > j.prev.head()[0]) out.addarc(j); else in.addarc(j);
            }
        }
    }

    /**
   * Not used
   * @param prev
   * @param j
   * @return false
   */
    private boolean ccwTurn_tl(Arc prev, Arc j) {
        return false;
    }

    /**
   * Not used
   * @param bin
   * @param in
   * @param out
   * @param val
   */
    private void classify_tailonright_s(Bin bin, Bin in, Bin out, float val) {
        Arc j;
        while ((j = bin.removearc()) != null) {
            j.clearitail();
            float diff = j.next.head()[0] - val;
            if (diff > 0) {
                if (ccwTurn_sr(j, j.next)) out.addarc(j); else in.addarc(j);
            } else if (diff < 0) {
                in.addarc(j);
            } else {
                if (j.next.tail()[1] > j.next.head()[1]) out.addarc(j); else in.addarc(j);
            }
        }
    }

    /**
   * Not used
   * @param bin
   * @param in
   * @param out
   * @param val
   */
    private void classify_headonleft_s(Bin bin, Bin in, Bin out, float val) {
        Arc j;
        while ((j = bin.removearc()) != null) {
            j.setitail();
            float diff = j.prev.tail()[0] - val;
            if (diff > 0) {
                out.addarc(j);
            } else if (diff < 0) {
                if (ccwTurn_sl(j.prev, j)) out.addarc(j); else in.addarc(j);
            } else {
                if (j.prev.tail()[1] > j.prev.head()[1]) in.addarc(j); else out.addarc(j);
            }
        }
    }

    /**
   * Not used
   * @param prev
   * @param j
   * @return false
   */
    private boolean ccwTurn_sl(Arc prev, Arc j) {
        return false;
    }

    /**
   * Not used
   * @param jarc
   * @param param
   * @param value
   * @param i
   * @return 0
   */
    private int arc_split(Arc jarc, int param, float value, int i) {
        return 0;
    }

    /**
   * Not used
   */
    private void setNonDegenerate() {
        this.showDegenerate = false;
    }

    /**
   * sets trimming arc default type to bezier
   */
    private void setArcTypeBezier() {
        isArcTypeBezier = true;
    }

    /**
   * Not used
   * @param source
   */
    private void outline(Bin source) {
    }

    /**
   * Makes default trim along surface borders
   * @param from range beginnings
   * @param to range ends
   */
    private void makeBorderTrim(float[] from, float[] to) {
        float smin = from[0];
        float smax = to[0];
        float tmin = from[1];
        float tmax = to[1];
        pjarc = null;
        Arc jarc = null;
        jarc = new Arc(Arc.ARC_BOTTOM);
        arctesselator.bezier(jarc, smin, smax, tmin, tmin);
        initialbin.addarc(jarc);
        pjarc = jarc.append(pjarc);
        jarc = new Arc(Arc.ARC_RIGHT);
        arctesselator.bezier(jarc, smax, smax, tmin, tmax);
        initialbin.addarc(jarc);
        pjarc = jarc.append(pjarc);
        jarc = new Arc(Arc.ARC_TOP);
        arctesselator.bezier(jarc, smax, smin, tmax, tmax);
        initialbin.addarc(jarc);
        pjarc = jarc.append(pjarc);
        jarc = new Arc(Arc.ARC_LEFT);
        arctesselator.bezier(jarc, smin, smin, tmax, tmin);
        initialbin.addarc(jarc);
        jarc = jarc.append(pjarc);
    }

    /**
   * Draws NURBS curve
   */
    public void drawCurves() {
        float[] from = new float[1];
        float[] to = new float[1];
        Flist bpts = new Flist();
        qlist.getRange(from, to, bpts);
        renderhints.init();
        backend.bgncurv();
        for (int i = bpts.start; i < bpts.end - 1; i++) {
            float[] pta = new float[1];
            float[] ptb = new float[1];
            pta[0] = bpts.pts[i];
            ptb[0] = bpts.pts[i + 1];
            qlist.downloadAll(pta, ptb, backend);
            Curvelist curvelist = new Curvelist(qlist, pta, ptb);
            samplingSplit(curvelist, renderhints.maxsubdivisions);
        }
        backend.endcurv();
    }

    /**
   * Samples a curve in case of need, or sends curve to backend
   * @param curvelist list of curves
   * @param maxsubdivisions maximum number of subdivisions
   */
    private void samplingSplit(Curvelist curvelist, int maxsubdivisions) {
        if (curvelist.cullCheck() == CULL_TRIVIAL_REJECT) return;
        curvelist.getstepsize();
        if (curvelist.needsSamplingSubdivision() && (subdivisions > 0)) {
        } else {
            int nu = (int) (1 + curvelist.range[2] / curvelist.stepsize);
            backend.curvgrid(curvelist.range[0], curvelist.range[1], nu);
            backend.curvmesh(0, nu);
        }
    }

    /**
   * Sets new domain_distance_u_rate value
   * @param d new domain_distance_u_rate value

  */
    public void set_domain_distance_u_rate(double d) {
        domain_distance_u_rate = (float) d;
    }

    /**
   * Sets new domain_distance_v_rate value
   * @param d new domain_distance_v_rate value
   */
    public void set_domain_distance_v_rate(double d) {
        domain_distance_v_rate = (float) d;
    }

    /**
   * Sets new is_domain_distance_sampling value
   * @param i new is_domain_distance_sampling value
   */
    public void set_is_domain_distance_sampling(int i) {
        this.is_domain_distance_sampling = i;
    }
}
