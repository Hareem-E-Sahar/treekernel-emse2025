package com.insanityengine.ghia.model;

import java.io.*;
import java.net.*;
import java.util.*;
import com.insanityengine.ghia.m3.*;
import com.insanityengine.ghia.util.*;
import com.insanityengine.ghia.libograf.*;

/**
 *
 * <P>
 * Loads aoff geometry files
 * </P>
 *
 * @author BrianHammond
 *
 * $Header: /cvsroot/ghia/ghia/src/java/com/insanityengine/ghia/model/GeomLoader.java,v 1.7 2006/11/29 06:15:31 brianin3d Exp $
 *
 */
public class GeomLoader implements DrawingInterface {

    /**
	 *
	 * Main method
	 *
	 * @param args from the cli
	 *
	 */
    public static final void main(String args[]) {
        GeomLoader gloader = new GeomLoader(args[0]);
        gloader.center();
        gloader.toJava(System.out);
    }

    /**
	 *
	 * Constructor 
	 *
	 */
    public GeomLoader() {
    }

    /**
	 *
	 * Constructor 
	 *
	 */
    public GeomLoader(String href) {
        init(href);
    }

    /**
	 *
	 * Initiliaze this GeomLoader from an href 
	 * (file:// is your friend)
	 *
	 * @param href to load from
	 *
	 * @return true if load succeeds, false on fail
	 * 
	 */
    public final boolean init(String href) {
        boolean success = false;
        try {
            URL tmp = new URL(href);
            BufferedReader in = (new BufferedReader(new InputStreamReader(tmp.openStream())));
            StringTokenizer st = new StringTokenizer(in.readLine());
            int i, j;
            vno = Stdlib.atoi(st.nextToken());
            sno = Stdlib.atoi(st.nextToken());
            ptz = new Pt3[vno];
            ptIdxz = new int[sno][];
            double x, y, z;
            for (i = 0; i < vno; ++i) {
                st = new StringTokenizer(in.readLine());
                x = Stdlib.atof(st.nextToken());
                y = Stdlib.atof(st.nextToken());
                z = Stdlib.atof(st.nextToken());
                if (i == 0) {
                    xmin = xmax = x;
                    ymin = ymax = y;
                    zmin = zmax = z;
                } else {
                    if (x < xmin) xmin = x;
                    if (x > xmax) xmax = x;
                    if (y < ymin) ymin = y;
                    if (y > ymax) ymax = y;
                    if (z < zmin) zmin = z;
                    if (z > zmax) zmax = z;
                }
                ptz[i] = new Pt3(x, y, z);
            }
            int vc;
            for (i = 0; i < sno; ++i) {
                st = new StringTokenizer(in.readLine());
                vc = Stdlib.atoi(st.nextToken());
                ptIdxz[i] = new int[vc];
                for (j = 0; j < vc; ++j) {
                    ptIdxz[i][j] = Stdlib.atoi(st.nextToken()) - 1;
                }
            }
            name = href.substring(1 + href.lastIndexOf(java.io.File.separator));
            if (-1 != name.indexOf('.')) name = name.substring(0, name.indexOf('.'));
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            com.insanityengine.ghia.util.SimpleLogger.info(name + ": " + " sno = " + sno + " vno = " + vno);
            success = true;
        } catch (Exception e) {
        }
        return success;
    }

    /**
	 *
	 * Move the centroid of the model to the origin.
	 *
	 */
    public void center() {
        if (null != ptz) {
            double xminus = (xmax + xmin) / 2;
            double yminus = (ymax + ymin) / 2;
            double zminus = (zmax + zmin) / 2;
            Pt3 pt = new Pt3(xminus, yminus, zminus);
            com.insanityengine.ghia.util.SimpleLogger.info("xmax = " + xmax + " xmin = " + xmin + " xminus = " + xminus);
            com.insanityengine.ghia.util.SimpleLogger.info("ymax = " + ymax + " ymin = " + ymin + " yminus = " + yminus);
            com.insanityengine.ghia.util.SimpleLogger.info("zmax = " + zmax + " zmin = " + zmin + " zminus = " + zminus);
            com.insanityengine.ghia.util.SimpleLogger.info("Center by " + pt);
            for (int i = 0; i < ptz.length; i++) {
                ptz[i].subtract(pt);
            }
            xmin -= xminus;
            xmax -= xminus;
            ymin -= yminus;
            ymax -= yminus;
            zmin -= zminus;
            zmax -= zminus;
        }
    }

    /**
	 *
	 * Do some kinda drawing thang
	 *
	 * @param gl context to draw in
	 *
	 */
    public void draw(LiboGrafInterface gl) {
        int i, j, vc;
        Pt3 pt;
        double xdiff = xmax - xmin + 1;
        double ydiff = ymax - ymin + 1;
        for (i = 0; i < sno; ++i) {
            gl.startPolygon();
            vc = ptIdxz[i].length;
            for (j = 0; j < vc; ++j) {
                pt = ptz[ptIdxz[i][j]];
                gl.textCoord((pt.x - xmin) / xdiff, (pt.y - ymin) / ydiff);
                gl.addPoint(pt);
            }
            gl.stopPolygon();
        }
    }

    /** 
	 * 
	 * getVertexCount
	 * 
	 * @return a int
	 * 
	 */
    public final int getVertexCount() {
        return vno;
    }

    /** 
	 * 
	 * getSurfaceCount
	 * 
	 * @return a int
	 * 
	 */
    public final int getSurfaceCount() {
        return sno;
    }

    /**
	 *
	 * Write java code (crudely) representing the original object
	 *
	 * @param out to print to
	 *
	 */
    public final void toJava(java.io.PrintStream out) {
        String eol = "\n";
        String classname = "GeomLoaded" + name;
        StringBuffer str = new StringBuffer();
        Pt3 pt;
        int i, j, vc, pidx;
        str.append("package com.insanityengine.ghia.GeomLoaded;" + eol);
        str.append("" + eol);
        str.append("import com.insanityengine.ghia.m3.*;" + eol);
        str.append("import com.insanityengine.ghia.libograf.*;" + eol);
        str.append("" + eol);
        str.append("/**" + eol);
        str.append(" *" + eol);
        str.append(" *  Generated by com.insanityengine.ghia.m3.GeomLoader" + eol);
        str.append(" *" + eol);
        str.append(" */" + eol);
        str.append("" + eol);
        str.append("public class " + classname + " implements DrawingInterface {" + eol);
        str.append("" + eol);
        str.append("\t/**" + eol);
        str.append("\t *" + eol);
        str.append("\t * Constructor" + eol);
        str.append("\t *" + eol);
        str.append("\t */" + eol);
        str.append("\tpublic " + classname + "() {" + eol);
        str.append("\t}" + eol);
        str.append("" + eol);
        str.append("\t/**" + eol);
        str.append("\t *" + eol);
        str.append("\t * Do some kinda drawing thang" + eol);
        str.append("\t *" + eol);
        str.append("\t * @param gl context to draw in" + eol);
        str.append("\t *" + eol);
        str.append("\t */" + eol);
        str.append("\tpublic final void draw( LiboGrafInterface gl ) {" + eol);
        str.append("\t\t" + classname + ".staticDraw( gl );" + eol);
        str.append("\t}" + eol);
        str.append("" + eol);
        int div = 1024;
        str.append("\t/**" + eol);
        str.append("\t *" + eol);
        str.append("\t * Do some kinda drawing thang" + eol);
        str.append("\t *" + eol);
        str.append("\t * @param gl context to draw in" + eol);
        str.append("\t *" + eol);
        str.append("\t */" + eol);
        str.append("\tpublic final static void staticDraw( LiboGrafInterface gl ) {" + eol);
        str.append("" + eol);
        for (i = j = 0; i < sno; i += div, ++j) {
            str.append("\t\tgl.drawPolygons( getPolygons" + j + "() );" + eol);
        }
        str.append("\t}" + eol);
        str.append("" + eol);
        str.append("\t// P R I V A T E" + eol);
        str.append("" + eol);
        str.append("\tprivate final static int polygonCount = " + sno + ";" + eol);
        str.append("" + eol);
        for (i = 0; i < vno; ++i) {
            str.append("\tprivate final static Pt3 pt" + i + " = new Pt3( " + ptz[i].x + "f, " + ptz[i].y + "f, " + ptz[i].z + "f );" + eol);
        }
        str.append("" + eol);
        int a, b, v;
        for (i = j = 0; i < sno; i += div, ++j) {
            str.append("\tprivate final static Pt3 [][] getPolygons" + j + "() {" + eol);
            str.append("\t\tPt3 [][] polygon = {" + eol);
            b = i + div;
            if (b > sno) b = sno;
            for (a = i; a < b; a++) {
                str.append("\t\t\t{");
                vc = ptIdxz[a].length;
                for (v = 0; v < vc; v++) {
                    pidx = ptIdxz[a][v];
                    if (0 != v) str.append(",");
                    str.append(" pt" + pidx);
                }
                str.append(" }, // " + a + eol);
            }
            str.append("\t\t};" + eol);
            str.append("\t\treturn polygon;" + eol);
            str.append("" + eol);
            str.append("\t}" + eol);
            str.append("" + eol);
        }
        str.append("}; // that's all GeomLoader.toJava wrote" + eol);
        out.print(str);
    }

    int vno, sno;

    double xmin, xmax;

    double ymin, ymax;

    double zmin, zmax;

    Pt3[] ptz = null;

    int[][] ptIdxz = null;

    String name = "none";
}

;
