package com.sun.j3d.loaders.objectfile;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.SceneBase;
import com.sun.j3d.loaders.Loader;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.objectfile.ObjectFileParser;
import com.sun.j3d.loaders.objectfile.ObjectFileMaterials;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;
import java.io.FileNotFoundException;
import java.io.StreamTokenizer;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.StringTokenizer;
import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.TexCoord2f;
import java.net.MalformedURLException;

/**
 * The ObjectFile class implements the Loader interface for the Wavefront
 * .obj file format, a standard 3D object file format created for use with
 * Wavefront's Advanced Visualizer (tm) and available for purchase from
 * Viewpoint DataLabs, as well as other 3D model companies.  Object Files
 * are text based
 * files supporting both polygonal and free-form geometry (curves 
 * and surfaces).  The Java 3D .obj file loader supports a subset of the
 * file format, but it is enough to load almost all commonly available 
 * Object Files.  Free-form geometry is not supported.</p>
 *
 * The Object File tokens currently supported by this loader are:</p>
 * <code>v <i>float</i> <i>float</i> <i>float</i></code></p>
 *   <dl><dd>A single vertex's geometric position in space.  The first vertex
 *   listed in the file has index 1,
 *   and subsequent vertices are numbered sequentially.</dl></p>
 * <code>vn <i>float</i> <i>float</i> <i>float</i></code></p>
 *   <dl><dd>A normal.  The first normal in the file is index 1, and 
 *   subsequent normals are numbered sequentially.</dl></p>
 * <code>vt <i>float</i> <i>float</i></code></p>
 *   <dl><dd>A texture coordinate.  The first texture coordinate in the file is
 *   index 1, and subsequent normals are numbered sequentially.</dl></p>
 * <code>f <i>int</i> <i>int</i> <i>int</i> . . .</code></p>
 *   <dl><dd><i><b>or</b></i></dl></p>
 * <code>f <i>int</i>/<i>int</i> <i>int</i>/<i>int</i> <i>int</i>/<i>int</i> . . .</code></p>
 *   <dl><dd><i><b>or</i></b></dl></p>
 * <code>f <i>int</i>/<i>int</i>/<i>int</i> <i>int</i>/<i>int</i>/<i>int</i> <i>int</i>/<i>int</i>/<i>int</i> . . .</code></p>
 *   <dl><dd>A polygonal face.  The numbers are indexes into the arrays of
 *   vertex positions, texture coordinates, and normals respectively.
 *   There is no maximum number of vertices that a single polygon may
 *   contain.  The .obj file specification says that each face must
 *   be flat and convex, but if the TRIANGULATE flag is sent to the
 *   ObjectFile constructor, each face will be triangulated by the
 *   Java 3D Triangulator, and therefore may be concave.
 *   A number may be omitted if, for example, texture coordinates are
 *   not being defined in the model.  Numbers are normally positive
 *   indexes, but may also be negative.  An index of -1 means the last
 *   member added to the respective array, -2 is the one before that,
 *   and so on.</dl></p>
 * <code>g <i>name</i></code></p>
 *   <dl><dd>Faces defined after this token will be added to the named group.
 *   These geometry groups are returned as separated Shape3D objects
 *   attached to the parent SceneGroup.  Each named Shape3D will also
 *   be in the Hashtable returned by Scene.getNamedObjects().  It is
 *   legal to add faces to a group, switch to another group, and then 
 *   add more faces to the original group by reissuing the same name
 *   with the g token.  If faces are added to the model before the g
 *   token is seen, the faces are put into the default group called
 *   "default."</dl></p>
 * <code>s <i>int</i></code></p>
 *   <dl><dd><i><b>or</i></b></dl></p>
 * <code>s off</code></p>
 *   <dl><dd>If the vn token is not used in the file to specify vertex normals
 *   for the model, this token may be used to put faces into groups
 *   for normal calculation ("smoothing groups") in the same manner as
 *   the 'g' token
 *   is used to group faces geometrically.  Faces in the same smoothing
 *   group will have their normals calculated as if they are part of
 *   the same smooth surface.  To do this, we use the Java 3D NormalGenerator
 *   utility with the creaseAngle parameter set to PI (180 degrees - 
 *   smooth shading, no creases) or to whatever the user has set the 
 *   creaseAngle.  Faces in group 0 or 'off' use a 
 *   creaseAngle of zero, meaning there is no smoothing (the normal
 *   of the face is used at all vertices giving the surface a faceted
 *   look; there will be a
 *   crease, or "Hard Edge," between each face in group zero).  There is
 *   also an implied hard edge <i>between</i> each smoothing group, where they
 *   meet each other.</p>
 *   </p>
 *   If neither the vn nor the s token is used in the file, then normals
 *   are calculated using the creaseAngle set in the contructor.
 *   Normals are calculated on each geometry
 *   group separately, meaning there will be a hard edge between each
 *   geometry group.</dl></p>
 *   </p>
 * <code>usemtl <i>name</i></code></p>
 *   <dl><dd>The current (and subsequent) geometry groups (specified with
 *   the 'g' token) have applied
 *   to them the named material property.  The following set of material
 *   properties are available by default:</dl></p>
 *   <pre>
 *     amber           amber_trans       aqua            aqua_filter
 *     archwhite       archwhite2        bflesh          black
 *     blondhair       blue_pure         bluegrey        bluetint
 *     blugrn          blutan            bluteal         bone
 *     bone1           bone2             brass           brnhair
 *     bronze          brown             brownlips       brownskn
 *     brzskin         chappie           charcoal        deepgreen
 *     default         dkblue            dkblue_pure     dkbrown
 *     dkdkgrey        dkgreen           dkgrey          dkorange
 *     dkpurple        dkred             dkteal          emerald
 *     fgreen          flaqua            flblack         flblonde
 *     flblue_pure     flbrown           fldkblue_pure   fldkdkgrey
 *     fldkgreen       fldkgreen2        fldkgrey        fldkolivegreen
 *     fldkpurple      fldkred           flesh           fleshtransparent
 *     flgrey          fllime            flltbrown       flltgrey
 *     flltolivegreen  flmintgreen       flmustard       florange
 *     flpinegreen     flpurple          flred           fltan
 *     flwhite         flwhite1          flyellow        glass
 *     glassblutint    glasstransparent  gold            green
 *     greenskn        grey              hair            iris
 *     jetflame        lavendar          lcdgreen        lighttan
 *     lighttan2       lighttan3         lighttannew     lightyellow
 *     lime            lips              ltbrown         ltgrey
 *     meh             metal             mintgrn         muscle
 *     navy_blue       offwhite.cool     offwhite.warm   olivegreen
 *     orange          pale_green        pale_pink       pale_yellow
 *     peach           periwinkle        pink            pinktan
 *     plasma          purple            red             redbrick
 *     redbrown        redorange         redwood         rubber
 *     ruby            sand_stone        sapphire        shadow
 *     ship2           silver            skin            sky_blue
 *     smoked_glass    tan               taupe           teeth
 *     violet          white             yellow          yellow_green
 *     yellowbrt       yelloworng
 *   </pre>
 * <code>mtllib <i>filename</i></code></p>
 *   <dl><dd>Load material properties from the named file.  Materials
 *   with the same name as the predefined materials above will override
 *   the default value.  Any directory path information in (filename)
 *   is ignored.  The .mtl files are assumed to be in the same directory 
 *   as the .obj file.  If they are in a different directory, use
 *   Loader.setBasePath() (or Loader.setBaseUrl() ).  The format of the
 *   material properties files
 *   are as follows:</p>
 *   <code>newmtl <i>name</i></code></p>
 *     <dl><dd>Start the definition of a new named material property.</dl></p>
 *   <code>Ka <i>float</i> <i>float</i> <i>float</i></code></p>
 *     <dl><dd>Ambient color.</dl></p>
 *   <code>Kd <i>float</i> <i>float</i> <i>float</i></code></p>
 *     <dl><dd>Diffuse color.</dl></p>
 *   <code>Ks <i>float</i> <i>float</i> <i>float</i></code></p>
 *     <dl><dd>Specular color.</dl></p>
 *   <code>illum <i>(0, 1, or 2)</i></code></p>
 *     <dl><dd>0 to disable lighting, 1 for ambient & diffuse only (specular
 *     color set to black), 2 for full lighting.</dl></p>
 *   <code>Ns <i>float</i></code></p>
 *     <dl><dd>Shininess (clamped to 1.0 - 128.0).</dl></p>
 *   <code>map_Kd <i>filename</i></code></p>
 *     <dl><dd>Texture map.  Supports .rgb, .rgba, .int, .inta, .sgi, and
 *     .bw files in addition to those supported by
 *     <a href="../../utils/image/TextureLoader.html">TextureLoader</a>.
 *     </dl></dl></p>
 */
public class ObjectFile implements Loader {

    private static final int DEBUG = 0;

    /**
     * Flag sent to constructor.  The object's vertices will be changed
     * so that the object is centered at (0,0,0) and the coordinate
     * positions are all in the range of (-1,-1,-1) to (1,1,1).
     */
    public static final int RESIZE = LOAD_SOUND_NODES << 1;

    /**
     * Flag sent to constructor.  The Shape3D object will be created
     * by using the GeometryInfo POLYGON_ARRAY primitive, causing
     * them to be Triangulated by GeometryInfo.  Use
     * this if you suspect concave or other non-behaving polygons
     * in your model.
     */
    public static final int TRIANGULATE = RESIZE << 1;

    /**
     * Flag sent to constructor.  Use if the vertices in your .obj
     * file were specified with clockwise winding (Java 3D wants
     * counter-clockwise) so you see the back of the polygons and
     * not the front.  Calls GeometryInfo.reverse().
     */
    public static final int REVERSE = TRIANGULATE << 1;

    /**
     * Flag sent to contructor.  After normals are generated the data
     * will be analyzed to find triangle strips.  Use this if your
     * hardware supports accelerated rendering of strips.
     */
    public static final int STRIPIFY = REVERSE << 1;

    private static final char BACKSLASH = '\\';

    private int flags;

    private String basePath = null;

    private URL baseUrl = null;

    private boolean fromUrl = false;

    private float radians;

    private ArrayList coordList;

    private ArrayList texList;

    private ArrayList normList;

    private ArrayList coordIdxList;

    private ArrayList texIdxList;

    private ArrayList normIdxList;

    private ArrayList stripCounts;

    private HashMap groups;

    private String curGroup;

    private HashMap sGroups;

    private String curSgroup;

    private HashMap groupMaterials;

    private HashMap triGroups;

    private ArrayList curTriGroup;

    private HashMap triSgroups;

    private ArrayList curTriSgroup;

    private Point3f coordArray[] = null;

    private Vector3f normArray[] = null;

    private TexCoord2f texArray[] = null;

    private long time;

    private ObjectFileMaterials materials = null;

    void readVertex(ObjectFileParser st) throws ParsingErrorException {
        Point3f p = new Point3f();
        st.getNumber();
        p.x = (float) st.nval;
        st.getNumber();
        p.y = (float) st.nval;
        st.getNumber();
        p.z = (float) st.nval;
        if ((DEBUG & 32) != 0) System.out.println("  (" + p.x + "," + p.y + "," + p.z + ")");
        st.skipToNextLine();
        coordList.add(p);
    }

    /**
     * readNormal
     */
    void readNormal(ObjectFileParser st) throws ParsingErrorException {
        Vector3f p = new Vector3f();
        st.getNumber();
        p.x = (float) st.nval;
        st.getNumber();
        p.y = (float) st.nval;
        st.getNumber();
        p.z = (float) st.nval;
        if ((DEBUG & 32) != 0) System.out.println("  (" + p.x + "," + p.y + "," + p.z + ")");
        st.skipToNextLine();
        normList.add(p);
    }

    /**
     * readTexture
     */
    void readTexture(ObjectFileParser st) throws ParsingErrorException {
        TexCoord2f p = new TexCoord2f();
        st.getNumber();
        p.x = (float) st.nval;
        st.getNumber();
        p.y = (float) st.nval;
        if ((DEBUG & 32) != 0) System.out.println("  (" + p.x + "," + p.y + ")");
        st.skipToNextLine();
        texList.add(p);
    }

    /**
     * readFace
     *
     *    Adds the indices of the current face to the arrays.
     *
     *    ViewPoint files can have up to three arrays:  Vertex Positions,
     *    Texture Coordinates, and Vertex Normals.  Each vertex can
     *    contain indices into all three arrays.
     */
    void readFace(ObjectFileParser st) throws ParsingErrorException {
        int vertIndex, texIndex = 0, normIndex = 0;
        int count = 0;
        st.getToken();
        while (st.ttype != st.TT_EOL) {
            st.pushBack();
            st.getNumber();
            vertIndex = (int) st.nval - 1;
            if (vertIndex < 0) vertIndex += coordList.size() + 1;
            coordIdxList.add(new Integer(vertIndex));
            st.getToken();
            if (st.ttype == '/') {
                st.getToken();
                if (st.ttype == st.TT_WORD) {
                    st.pushBack();
                    st.getNumber();
                    texIndex = (int) st.nval - 1;
                    if (texIndex < 0) texIndex += texList.size() + 1;
                    texIdxList.add(new Integer(texIndex));
                    st.getToken();
                }
                if (st.ttype == '/') {
                    st.getNumber();
                    normIndex = (int) st.nval - 1;
                    if (normIndex < 0) normIndex += normList.size() + 1;
                    normIdxList.add(new Integer(normIndex));
                    st.getToken();
                }
            }
            if ((DEBUG & 32) != 0) {
                System.out.println("  " + vertIndex + '/' + texIndex + '/' + normIndex);
            }
            count++;
        }
        Integer faceNum = new Integer(stripCounts.size());
        stripCounts.add(new Integer(count));
        groups.put(faceNum, curGroup);
        if (curSgroup != null) sGroups.put(faceNum, curSgroup);
        st.skipToNextLine();
    }

    /**
     * readPartName
     */
    void readPartName(ObjectFileParser st) {
        st.getToken();
        String curMat = (String) groupMaterials.get(curGroup);
        if (st.ttype != ObjectFileParser.TT_WORD) curGroup = "default"; else curGroup = st.sval;
        if ((DEBUG & 32) != 0) System.out.println("  Changed to group " + curGroup);
        if (groupMaterials.get(curGroup) == null) {
            groupMaterials.put(curGroup, curMat);
        }
        st.skipToNextLine();
    }

    /**
     * readMaterialName
     */
    void readMaterialName(ObjectFileParser st) throws ParsingErrorException {
        st.getToken();
        if (st.ttype == ObjectFileParser.TT_WORD) {
            groupMaterials.put(curGroup, new String(st.sval));
            if ((DEBUG & 32) != 0) {
                System.out.println("  Material Property " + st.sval + " assigned to group " + curGroup);
            }
        }
        st.skipToNextLine();
    }

    /**
     * loadMaterialFile
     *
     *	Both types of slashes are returned as tokens from our parser,
     *	so we go through the line token by token and keep just the
     *	last token on the line.  This should be the filename without
     *	any directory info.
     */
    void loadMaterialFile(ObjectFileParser st) throws ParsingErrorException {
        String s = null;
        st.lowerCaseMode(false);
        do {
            st.getToken();
            if (st.ttype == ObjectFileParser.TT_WORD) s = st.sval;
        } while (st.ttype != ObjectFileParser.TT_EOL);
        materials.readMaterialFile(fromUrl, fromUrl ? baseUrl.toString() : basePath, s);
        st.lowerCaseMode(true);
        st.skipToNextLine();
    }

    /**
     * readSmoothingGroup
     */
    void readSmoothingGroup(ObjectFileParser st) throws ParsingErrorException {
        st.getToken();
        if (st.ttype != ObjectFileParser.TT_WORD) {
            st.skipToNextLine();
            return;
        }
        if (st.sval.equals("off")) curSgroup = "0"; else curSgroup = st.sval;
        if ((DEBUG & 32) != 0) System.out.println("  Smoothing group " + curSgroup);
        st.skipToNextLine();
    }

    /**
     * readFile
     *
     *    Read the model data from the file.
     */
    void readFile(ObjectFileParser st) throws ParsingErrorException {
        int t;
        st.getToken();
        while (st.ttype != ObjectFileParser.TT_EOF) {
            if ((DEBUG & 16) != 0) {
                System.out.print("Token ");
                if (st.ttype == ObjectFileParser.TT_EOL) System.out.println("EOL"); else if (st.ttype == ObjectFileParser.TT_WORD) System.out.println(st.sval); else System.out.println((char) st.ttype);
            }
            if (st.ttype == ObjectFileParser.TT_WORD) {
                if (st.sval.equals("v")) {
                    readVertex(st);
                } else if (st.sval.equals("vn")) {
                    readNormal(st);
                } else if (st.sval.equals("vt")) {
                    readTexture(st);
                } else if (st.sval.equals("f")) {
                    readFace(st);
                } else if (st.sval.equals("fo")) {
                    readFace(st);
                } else if (st.sval.equals("g")) {
                    readPartName(st);
                } else if (st.sval.equals("s")) {
                    readSmoothingGroup(st);
                } else if (st.sval.equals("p")) {
                    st.skipToNextLine();
                } else if (st.sval.equals("l")) {
                    st.skipToNextLine();
                } else if (st.sval.equals("mtllib")) {
                    loadMaterialFile(st);
                } else if (st.sval.equals("usemtl")) {
                    readMaterialName(st);
                } else if (st.sval.equals("maplib")) {
                    st.skipToNextLine();
                } else if (st.sval.equals("usemap")) {
                    st.skipToNextLine();
                } else {
                    throw new ParsingErrorException("Unrecognized token, line " + st.lineno());
                }
            }
            st.skipToNextLine();
            st.getToken();
        }
    }

    /**
     * Constructor.
     *
     * @param flags The constants from above or from
     * com.sun.j3d.loaders.Loader, possibly "or'ed" (|) together.
     * @param radians Ignored if the vn token is present in the model (user
     * normals supplied).  Otherwise, crease angle to use within smoothing
     * groups, or within geometry groups if the s token isn't present either.
     */
    public ObjectFile(int flags, float radians) {
        setFlags(flags);
        this.radians = radians;
    }

    /**
     * Constructor.  Crease Angle set to default of
     * 44 degrees (see NormalGenerator utility for details).
     * @param flags The constants from above or from
     * com.sun.j3d.loaders.Loader, possibly "or'ed" (|) together.
     */
    public ObjectFile(int flags) {
        this(flags, -1.0f);
    }

    /**
     * Default constructor.  Crease Angle set to default of
     * 44 degrees (see NormalGenerator utility for details).  Flags
     * set to zero (0).
     */
    public ObjectFile() {
        this(0, -1.0f);
    }

    /**
     * Takes a file name and sets the base path to the directory
     * containing that file.
     */
    private void setBasePathFromFilename(String fileName) {
        if (fileName.lastIndexOf(java.io.File.separator) == -1) {
            setBasePath("." + java.io.File.separator);
        } else {
            setBasePath(fileName.substring(0, fileName.lastIndexOf(java.io.File.separator)));
        }
    }

    /**
     * The Object File is loaded from the .obj file specified by
     * the filename.
     * To attach the model to your scene, call getSceneGroup() on
     * the Scene object passed back, and attach the returned
     * BranchGroup to your scene graph.  For an example, see 
     * j3d-examples/ObjLoad/ObjLoad.java.
     */
    public Scene load(String filename) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
        setBasePathFromFilename(filename);
        Reader reader = new BufferedReader(new FileReader(filename));
        return load(reader);
    }

    private void setBaseUrlFromUrl(URL url) throws FileNotFoundException {
        String u = url.toString();
        String s;
        if (u.lastIndexOf('/') == -1) {
            s = url.getProtocol() + ":";
        } else {
            s = u.substring(0, u.lastIndexOf('/') + 1);
        }
        try {
            baseUrl = new URL(s);
        } catch (MalformedURLException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    /**
     * The object file is loaded off of the web.
     * To attach the model to your scene, call getSceneGroup() on
     * the Scene object passed back, and attach the returned
     * BranchGroup to your scene graph.  For an example, see 
     * j3d-examples/ObjLoad/ObjLoad.java.
     */
    public Scene load(URL url) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
        BufferedReader reader;
        if (baseUrl == null) setBaseUrlFromUrl(url);
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
        fromUrl = true;
        return load(reader);
    }

    /**
     * getLimits
     *
     * Returns an array of Point3f which form a bounding box around the
     * object.  Element 0 is the low value, element 1 is the high value.
     * See normalize() below for an example of how to use this method.
     */
    private Point3f[] getLimits() {
        Point3f cur_vtx = new Point3f();
        Point3f[] limit = new Point3f[2];
        limit[0] = new Point3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        limit[1] = new Point3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        for (int i = 0; i < coordList.size(); i++) {
            cur_vtx = (Point3f) coordList.get(i);
            if (cur_vtx.x < limit[0].x) limit[0].x = cur_vtx.x;
            if (cur_vtx.x > limit[1].x) limit[1].x = cur_vtx.x;
            if (cur_vtx.y < limit[0].y) limit[0].y = cur_vtx.y;
            if (cur_vtx.y > limit[1].y) limit[1].y = cur_vtx.y;
            if (cur_vtx.z < limit[0].z) limit[0].z = cur_vtx.z;
            if (cur_vtx.z > limit[1].z) limit[1].z = cur_vtx.z;
        }
        if ((DEBUG & 64) != 0) {
            System.out.println("Model range: (" + limit[0].x + "," + limit[0].y + "," + limit[0].z + ") to (" + limit[1].x + "," + limit[1].y + "," + limit[1].z + ")");
        }
        return limit;
    }

    /**
     * Center the object and make it (-1,-1,-1) to (1,1,1).
     */
    private void resize() {
        int i, j;
        Point3f cur_vtx = new Point3f();
        float biggest_dif;
        Point3f[] limit = getLimits();
        Vector3f offset = new Vector3f(-0.5f * (limit[0].x + limit[1].x), -0.5f * (limit[0].y + limit[1].y), -0.5f * (limit[0].z + limit[1].z));
        if ((DEBUG & 64) != 0) {
            System.out.println("Offset amount: (" + offset.x + "," + offset.y + "," + offset.z + ")");
        }
        biggest_dif = limit[1].x - limit[0].x;
        if (biggest_dif < limit[1].y - limit[0].y) biggest_dif = limit[1].y - limit[0].y;
        if (biggest_dif < limit[1].z - limit[0].z) biggest_dif = limit[1].z - limit[0].z;
        biggest_dif /= 2.0f;
        for (i = 0; i < coordList.size(); i++) {
            cur_vtx = (Point3f) coordList.get(i);
            cur_vtx.add(cur_vtx, offset);
            cur_vtx.x /= biggest_dif;
            cur_vtx.y /= biggest_dif;
            cur_vtx.z /= biggest_dif;
        }
    }

    private int[] objectToIntArray(ArrayList inList) {
        int outList[] = new int[inList.size()];
        for (int i = 0; i < inList.size(); i++) {
            outList[i] = ((Integer) inList.get(i)).intValue();
        }
        return outList;
    }

    private Point3f[] objectToPoint3Array(ArrayList inList) {
        Point3f outList[] = new Point3f[inList.size()];
        for (int i = 0; i < inList.size(); i++) {
            outList[i] = (Point3f) inList.get(i);
        }
        return outList;
    }

    private TexCoord2f[] objectToTexCoord2Array(ArrayList inList) {
        TexCoord2f outList[] = new TexCoord2f[inList.size()];
        for (int i = 0; i < inList.size(); i++) {
            outList[i] = (TexCoord2f) inList.get(i);
        }
        return outList;
    }

    private Vector3f[] objectToVectorArray(ArrayList inList) {
        Vector3f outList[] = new Vector3f[inList.size()];
        for (int i = 0; i < inList.size(); i++) {
            outList[i] = (Vector3f) inList.get(i);
        }
        return outList;
    }

    /**
     * Each group is a list of indices into the model's index lists,
     * indicating the starting index of each triangle in the group.
     * This method converts those data structures
     * into an integer array to use with GeometryInfo.
     */
    private int[] groupIndices(ArrayList sourceList, ArrayList group) {
        int indices[] = new int[group.size() * 3];
        for (int i = 0; i < group.size(); i++) {
            int j = ((Integer) group.get(i)).intValue();
            indices[i * 3 + 0] = ((Integer) sourceList.get(j + 0)).intValue();
            indices[i * 3 + 1] = ((Integer) sourceList.get(j + 1)).intValue();
            indices[i * 3 + 2] = ((Integer) sourceList.get(j + 2)).intValue();
        }
        return indices;
    }

    /**
     * smoothingGroupNormals
     *
     * Smoothing groups are groups of faces who should be grouped
     * together for normal calculation purposes.  The faces are
     * put into a GeometryInfo object and normals are calculated
     * with a 180 degree creaseAngle (no creases) or whatever the
     * user has specified.  The normals
     * are then copied out of the GeometryInfo and back into
     * ObjectFile data structures.
     */
    private void smoothingGroupNormals() {
        NormalGenerator ng = new NormalGenerator(radians == -1.0f ? Math.PI : radians);
        NormalGenerator ng0 = new NormalGenerator(0.0);
        normList.clear();
        normIdxList = null;
        int newNormIdxArray[] = new int[coordIdxList.size()];
        Iterator e = triSgroups.keySet().iterator();
        while (e.hasNext()) {
            String curname = (String) e.next();
            ArrayList triList = (ArrayList) triSgroups.get(curname);
            if (triList.size() > 0) {
                GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
                gi.setCoordinateIndices(groupIndices(coordIdxList, triList));
                gi.setCoordinates(coordArray);
                if (curname.equals("0")) ng0.generateNormals(gi); else ng.generateNormals(gi);
                Vector3f genNorms[] = gi.getNormals();
                int genNormIndices[] = gi.getNormalIndices();
                int normIdx = 0;
                for (int i = 0; i < triList.size(); i++) {
                    int idx = ((Integer) triList.get(i)).intValue();
                    for (int j = 0; j < 3; j++) {
                        newNormIdxArray[idx + j] = normList.size();
                        normList.add(genNorms[genNormIndices[normIdx++]]);
                    }
                }
            }
        }
        normIdxList = new ArrayList(coordIdxList.size());
        for (int i = 0; i < coordIdxList.size(); i++) {
            normIdxList.add(new Integer(newNormIdxArray[i]));
        }
        normArray = objectToVectorArray(normList);
    }

    /**
     * Each face is converted to triangles.  As each face is converted,
     * we look up which geometry group and smoothing group the face
     * belongs to.  The generated triangles are added to each of these
     * groups, which are also being converted to a new triangle based format.
     *
     * We need to convert to triangles before normals are generated
     * because of smoothing groups.  The faces in a smoothing group 
     * are copied into a GeometryInfo to have their normals calculated,
     * and then the normals are copied out of the GeometryInfo using
     * GeometryInfo.getNormalIndices.  As part of Normal generation,
     * the geometry gets converted to Triangles.  So we need to convert
     * to triangles *before* Normal generation so that the normals we
     * read out of the GeometryInfo match up with the vertex data
     * that we sent in.  If we sent in TRIANGLE_FAN data, the normal
     * generator would convert it to triangles and we'd read out 
     * normals formatted for Triangle data.  This would not match up
     * with our original Fan data, so we couldn't tell which normals
     * go with which vertices.
     */
    private void convertToTriangles() {
        boolean triangulate = (flags & TRIANGULATE) != 0;
        boolean textures = !texList.isEmpty() && !texIdxList.isEmpty() && (texIdxList.size() == coordIdxList.size());
        boolean normals = !normList.isEmpty() && !normIdxList.isEmpty() && (normIdxList.size() == coordIdxList.size());
        int numFaces = stripCounts.size();
        boolean haveSgroups = curSgroup != null;
        triGroups = new HashMap(50);
        if (haveSgroups) triSgroups = new HashMap(50);
        ArrayList newCoordIdxList = null;
        ArrayList newTexIdxList = null;
        ArrayList newNormIdxList = null;
        if (triangulate) {
            GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
            gi.setStripCounts(objectToIntArray(stripCounts));
            gi.setCoordinates(coordArray);
            gi.setCoordinateIndices(objectToIntArray(coordIdxList));
            if (textures) {
                gi.setTextureCoordinateParams(1, 2);
                gi.setTextureCoordinates(0, texArray);
                gi.setTextureCoordinateIndices(0, objectToIntArray(texIdxList));
            }
            if (normals) {
                gi.setNormals(normArray);
                gi.setNormalIndices(objectToIntArray(normIdxList));
            }
            gi.convertToIndexedTriangles();
            int coordIndicesArray[] = gi.getCoordinateIndices();
            int tris = 0;
            for (int i = 0; i < numFaces; i++) tris += ((Integer) stripCounts.get(i)).intValue() - 2;
            if (coordIndicesArray.length != (tris * 3)) {
                triangulate = false;
            } else {
                int texIndicesArray[] = gi.getTextureCoordinateIndices();
                int normIndicesArray[] = gi.getNormalIndices();
                coordIdxList.clear();
                texIdxList.clear();
                normIdxList.clear();
                for (int i = 0; i < coordIndicesArray.length; i++) {
                    coordIdxList.add(new Integer(coordIndicesArray[i]));
                    if (textures) texIdxList.add(new Integer(texIndicesArray[i]));
                    if (normals) normIdxList.add(new Integer(normIndicesArray[i]));
                }
            }
        }
        if (!triangulate) {
            newCoordIdxList = new ArrayList();
            if (textures) newTexIdxList = new ArrayList();
            if (normals) newNormIdxList = new ArrayList();
        }
        int baseVertex = 0;
        for (int f = 0; f < numFaces; f++) {
            int faceSize = ((Integer) stripCounts.get(f)).intValue();
            Integer curFace = new Integer(f);
            curGroup = (String) groups.get(curFace);
            curTriGroup = (ArrayList) triGroups.get(curGroup);
            if (curTriGroup == null) {
                curTriGroup = new ArrayList();
                triGroups.put(curGroup, curTriGroup);
            }
            if (haveSgroups) {
                curSgroup = (String) sGroups.get(curFace);
                if (curSgroup == null) {
                    curSgroup = "0";
                }
                curTriSgroup = (ArrayList) triSgroups.get(curSgroup);
                if (curTriSgroup == null) {
                    curTriSgroup = new ArrayList();
                    triSgroups.put(curSgroup, curTriSgroup);
                }
            }
            if (triangulate) {
                for (int t = 0; t < faceSize - 2; t++) {
                    Integer triBaseVertex = new Integer(baseVertex);
                    curTriGroup.add(triBaseVertex);
                    if (haveSgroups) curTriSgroup.add(triBaseVertex);
                    baseVertex += 3;
                }
            } else {
                for (int v = 0; v < faceSize - 2; v++) {
                    Integer triBaseVertex = new Integer(newCoordIdxList.size());
                    curTriGroup.add(triBaseVertex);
                    if (haveSgroups) curTriSgroup.add(triBaseVertex);
                    newCoordIdxList.add(coordIdxList.get(baseVertex));
                    newCoordIdxList.add(coordIdxList.get(baseVertex + v + 1));
                    newCoordIdxList.add(coordIdxList.get(baseVertex + v + 2));
                    if (textures) {
                        newTexIdxList.add(texIdxList.get(baseVertex));
                        newTexIdxList.add(texIdxList.get(baseVertex + v + 1));
                        newTexIdxList.add(texIdxList.get(baseVertex + v + 2));
                    }
                    if (normals) {
                        newNormIdxList.add(normIdxList.get(baseVertex));
                        newNormIdxList.add(normIdxList.get(baseVertex + v + 1));
                        newNormIdxList.add(normIdxList.get(baseVertex + v + 2));
                    }
                }
                baseVertex += faceSize;
            }
        }
        stripCounts = null;
        groups = null;
        sGroups = null;
        if (!triangulate) {
            coordIdxList = newCoordIdxList;
            texIdxList = newTexIdxList;
            normIdxList = newNormIdxList;
        }
    }

    private SceneBase makeScene() {
        SceneBase scene = new SceneBase();
        BranchGroup group = new BranchGroup();
        scene.setSceneGroup(group);
        boolean gen_norms = normList.isEmpty() || normIdxList.isEmpty() || (normIdxList.size() != coordIdxList.size());
        boolean do_tex = !texList.isEmpty() && !texIdxList.isEmpty() && (texIdxList.size() == coordIdxList.size());
        coordArray = objectToPoint3Array(coordList);
        if (!gen_norms) normArray = objectToVectorArray(normList);
        if (do_tex) texArray = objectToTexCoord2Array(texList);
        convertToTriangles();
        if ((DEBUG & 8) != 0) {
            time = System.currentTimeMillis() - time;
            System.out.println("Convert to triangles: " + time + " ms");
            time = System.currentTimeMillis();
        }
        if ((gen_norms) && (curSgroup != null)) {
            smoothingGroupNormals();
            gen_norms = false;
            if ((DEBUG & 8) != 0) {
                time = System.currentTimeMillis() - time;
                System.out.println("Smoothing group normals: " + time + " ms");
                time = System.currentTimeMillis();
            }
        }
        NormalGenerator ng = null;
        if (gen_norms) ng = new NormalGenerator(radians);
        Stripifier strippy = null;
        if ((flags & STRIPIFY) != 0) strippy = new Stripifier();
        long t1 = 0, t2 = 0, t3 = 0, t4 = 0;
        Iterator e = triGroups.keySet().iterator();
        while (e.hasNext()) {
            String curname = (String) e.next();
            ArrayList triList = (ArrayList) triGroups.get(curname);
            if (triList.size() > 0) {
                GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
                gi.setCoordinateIndices(groupIndices(coordIdxList, triList));
                gi.setCoordinates(coordArray);
                if (do_tex) {
                    gi.setTextureCoordinateParams(1, 2);
                    gi.setTextureCoordinates(0, texArray);
                    gi.setTextureCoordinateIndices(0, groupIndices(texIdxList, triList));
                }
                if ((DEBUG & 8) != 0) time = System.currentTimeMillis();
                if (gen_norms) {
                    if ((flags & REVERSE) != 0) gi.reverse();
                    ng.generateNormals(gi);
                    if ((DEBUG & 8) != 0) {
                        t2 += System.currentTimeMillis() - time;
                        System.out.println("Generate normals: " + t2 + " ms");
                        time = System.currentTimeMillis();
                    }
                } else {
                    gi.setNormalIndices(groupIndices(normIdxList, triList));
                    gi.setNormals(normArray);
                    if ((flags & REVERSE) != 0) gi.reverse();
                }
                if ((flags & STRIPIFY) != 0) {
                    strippy.stripify(gi);
                    if ((DEBUG & 8) != 0) {
                        t3 += System.currentTimeMillis() - time;
                        System.out.println("Stripify: " + t3 + " ms");
                        time = System.currentTimeMillis();
                    }
                }
                Shape3D shape = new Shape3D();
                shape.setGeometry(gi.getGeometryArray(false, false, false));
                String matName = (String) groupMaterials.get(curname);
                materials.assignMaterial(matName, shape);
                group.addChild(shape);
                scene.addNamedObject(curname, shape);
                if ((DEBUG & 8) != 0) {
                    t4 += System.currentTimeMillis() - time;
                    System.out.println("Shape 3D: " + t4 + " ms");
                    time = System.currentTimeMillis();
                }
            }
        }
        return scene;
    }

    /**
     * The Object File is loaded from the already opened file.
     * To attach the model to your scene, call getSceneGroup() on
     * the Scene object passed back, and attach the returned
     * BranchGroup to your scene graph.  For an example, see 
     * j3d-examples/ObjLoad/ObjLoad.java.
     */
    public Scene load(Reader reader) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
        ObjectFileParser st = new ObjectFileParser(reader);
        coordList = new ArrayList();
        texList = new ArrayList();
        normList = new ArrayList();
        coordIdxList = new ArrayList();
        texIdxList = new ArrayList();
        normIdxList = new ArrayList();
        groups = new HashMap(50);
        curGroup = "default";
        sGroups = new HashMap(50);
        curSgroup = null;
        stripCounts = new ArrayList();
        groupMaterials = new HashMap(50);
        groupMaterials.put(curGroup, "default");
        materials = new ObjectFileMaterials();
        time = 0L;
        if ((DEBUG & 8) != 0) {
            time = System.currentTimeMillis();
        }
        readFile(st);
        if ((DEBUG & 8) != 0) {
            time = System.currentTimeMillis() - time;
            System.out.println("Read file: " + time + " ms");
            time = System.currentTimeMillis();
        }
        if ((flags & RESIZE) != 0) resize();
        return makeScene();
    }

    /**
     * For an .obj file loaded from a URL, set the URL where associated files
     * (like material properties files) will be found.
     * Only needs to be called to set it to a different URL
     * from that containing the .obj file.
     */
    public void setBaseUrl(URL url) {
        baseUrl = url;
    }

    /**
     * Return the URL where files associated with this .obj file (like
     * material properties files) will be found.
     */
    public URL getBaseUrl() {
        return baseUrl;
    }

    /**
     * Set the path where files associated with this .obj file are
     * located.
     * Only needs to be called to set it to a different directory
     * from that containing the .obj file.
     */
    public void setBasePath(String pathName) {
        basePath = pathName;
        if (basePath == null || basePath == "") basePath = "." + java.io.File.separator;
        basePath = basePath.replace('/', java.io.File.separatorChar);
        basePath = basePath.replace('\\', java.io.File.separatorChar);
        if (!basePath.endsWith(java.io.File.separator)) basePath = basePath + java.io.File.separator;
    }

    /**
     * Return the path where files associated with this .obj file (like material
     * files) are located.
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * Set parameters for loading the model.
     * Flags defined in Loader.java are ignored by the ObjectFile Loader
     * because the .obj file format doesn't include lights, fog, background,
     * behaviors, views, or sounds.  However, several flags are defined
     * specifically for use with the ObjectFile Loader (see above).
     */
    public void setFlags(int flags) {
        this.flags = flags;
        if ((DEBUG & 4) != 0) System.out.println("Flags = " + flags);
    }

    /**
     * Get the parameters currently defined for loading the model.
     * Flags defined in Loader.java are ignored by the ObjectFile Loader
     * because the .obj file format doesn't include lights, fog, background,
     * behaviors, views, or sounds.  However, several flags are defined
     * specifically for use with the ObjectFile Loader (see above).
     */
    public int getFlags() {
        return flags;
    }
}
