package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Vector;
import javax.swing.SwingUtilities;
import net.claribole.zvtm.engine.DraggableCameraPortal;
import net.claribole.zvtm.engine.Java2DPainter;
import net.claribole.zvtm.engine.Location;
import net.claribole.zvtm.engine.OverviewPortal;
import net.claribole.zvtm.engine.PortalEventHandler;
import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.FSGaussianLens;
import net.claribole.zvtm.lens.Lens;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VRectangle;
import net.claribole.eval.glyphs.ZRoundRect;
import com.xerox.VTM.glyphs.ZSegment;

public class ZLAbstractTask implements PostAnimationAction, Java2DPainter {

    static int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;

    static int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;

    static final int VIEW_MAX_W = 1280;

    static final int VIEW_MAX_H = 1024;

    int VIEW_W, VIEW_H;

    int VIEW_X, VIEW_Y;

    static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 10);

    static int ANIM_MOVE_LENGTH = 300;

    int panelWidth, panelHeight;

    int hpanelWidth, hpanelHeight;

    VirtualSpaceManager vsm;

    AbstractTaskEventHandler eh;

    AbstractTaskLogManager logm;

    View demoView;

    Camera demoCamera;

    VirtualSpace mainVS;

    static String mainVSname = "mainSpace";

    Lens lens;

    static int LENS_R1 = 100;

    static int LENS_R2 = 60;

    static final int WHEEL_ANIM_TIME = 50;

    static final int LENS_ANIM_TIME = 300;

    static final int GRID_ANIM_TIME = 500;

    static double DEFAULT_MAG_FACTOR = 4.0;

    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;

    static double INV_MAG_FACTOR = 1 / MAG_FACTOR;

    static float WHEEL_MM_STEP = 1.0f;

    static final float MAX_MAG_FACTOR = 12.0f;

    ZSegment[][] hGridLevels = new ZSegment[GRID_DEPTH + 1][];

    ZSegment[][] vGridLevels = new ZSegment[GRID_DEPTH + 1][];

    Vector tmpHGrid;

    Vector tmpVGrid;

    Camera portalCamera;

    static final int DM_PORTAL_WIDTH = 200;

    static final int DM_PORTAL_HEIGHT = 200;

    static final int DM_PORTAL_INITIAL_X_OFFSET = 150;

    static final int DM_PORTAL_INITIAL_Y_OFFSET = 150;

    DraggableCameraPortal dmPortal;

    VRectangle dmRegion;

    int dmRegionW, dmRegionN, dmRegionE, dmRegionS;

    boolean paintLinks = false;

    OverviewPortal ovPortal;

    static final Color HCURSOR_COLOR = new Color(200, 48, 48);

    static final Color GRID_COLOR = new Color(156, 53, 53);

    static final int GRID_DEPTH = 4;

    int currentLevel = -1;

    static final String GLYPH_TYPE_GRID = "G";

    static final String GLYPH_TYPE_WORLD = "W";

    static final float START_ALTITUDE = 40000.0f;

    static final float FLOOR_ALTITUDE = 300.0f;

    boolean cameraOnFloor = false;

    int SELECTION_RECT_X = 0;

    int SELECTION_RECT_Y = 0;

    int SELECTION_RECT_W = 16;

    int SELECTION_RECT_H = 16;

    int SELECTION_RECT_HW = SELECTION_RECT_W / 2;

    int SELECTION_RECT_HH = SELECTION_RECT_H / 2;

    boolean SHOW_SELECTION_RECT = true;

    static final Color SELECTION_RECT_COLOR = Color.RED;

    static java.awt.Robot robot;

    static final short PZ_TECHNIQUE = 1;

    static final short PZO_TECHNIQUE = 2;

    static final short PZL_TECHNIQUE = 3;

    static final short DM_TECHNIQUE = 4;

    static final String PZ_TECHNIQUE_NAME = "Pan-Zoom";

    static final String PZO_TECHNIQUE_NAME = "Pan-Zoom + Overview";

    static final String PZL_TECHNIQUE_NAME = "Pan Zoom + Probing Lenses";

    static final String DM_TECHNIQUE_NAME = "Drag Mag";

    short technique = PZ_TECHNIQUE;

    String techniqueName;

    static final int[] vispad = { 100, 100, 100, 100 };

    static int TREE_DEPTH = 2;

    static int DENSITY = 3;

    static final long SMALLEST_ELEMENT_WIDTH = 500;

    static final long MUL_FACTOR = 50;

    static final double ROUND_CORNER_RATIO = 4;

    static long WORLD_WIDTH;

    static long WORLD_HEIGHT;

    static long HALF_WORLD_WIDTH;

    static long HALF_WORLD_HEIGHT;

    static final Color DISC_BORDER_COLOR = Color.BLACK;

    static final Color VISITED_BORDER_COLOR = Color.GREEN;

    static final float NEXT_LEVEL_VIS_FACTOR = 2.0f;

    static long[] widthByLevel;

    static int[] cornerByLevel;

    static LongPoint[][] offsetsByLevel;

    ZRoundRect[][] elementsByLevel;

    static final Color[] COLOR_BY_LEVEL = { Color.GRAY, Color.LIGHT_GRAY };

    boolean[][] visitsByLevel = new boolean[TREE_DEPTH][DENSITY * DENSITY];

    static {
        widthByLevel = new long[TREE_DEPTH];
        cornerByLevel = new int[TREE_DEPTH];
        offsetsByLevel = new LongPoint[TREE_DEPTH][DENSITY * DENSITY];
        for (int i = 0; i < TREE_DEPTH; i++) {
            widthByLevel[i] = SMALLEST_ELEMENT_WIDTH * Math.round(Math.pow(MUL_FACTOR, (TREE_DEPTH - i - 1)));
            cornerByLevel[i] = ((int) Math.round(widthByLevel[i] / ROUND_CORNER_RATIO));
            if (i > 0) {
                long step = Math.round(widthByLevel[i - 1] / ((double) DENSITY)) * 2;
                long y = widthByLevel[i - 1] - step / 2;
                for (int j = 0; j < DENSITY; j++) {
                    long x = -widthByLevel[i - 1] + step / 2;
                    for (int k = 0; k < DENSITY; k++) {
                        offsetsByLevel[i][j * DENSITY + k] = new LongPoint(x, y);
                        x += step;
                    }
                    y -= step;
                }
            }
        }
        WORLD_WIDTH = widthByLevel[0] * 2;
        WORLD_HEIGHT = WORLD_WIDTH;
        HALF_WORLD_WIDTH = WORLD_WIDTH / 2;
        HALF_WORLD_HEIGHT = WORLD_HEIGHT / 2;
    }

    ZLAbstractTask(short t) {
        vsm = new VirtualSpaceManager();
        vsm.setDebug(true);
        init(t);
    }

    public void init(short t) {
        try {
            robot = new java.awt.Robot();
        } catch (java.awt.AWTException ex) {
            ex.printStackTrace();
        }
        windowLayout();
        this.technique = t;
        if (this.technique == PZ_TECHNIQUE) {
            eh = new AbstractTaskPZEventHandler(this);
            techniqueName = PZ_TECHNIQUE_NAME;
        }
        if (this.technique == PZO_TECHNIQUE) {
            eh = new AbstractTaskPZOEventHandler(this);
            techniqueName = PZO_TECHNIQUE_NAME;
        } else if (this.technique == PZL_TECHNIQUE) {
            eh = new AbstractTaskPZLEventHandler(this);
            techniqueName = PZL_TECHNIQUE_NAME;
        } else if (this.technique == DM_TECHNIQUE) {
            eh = new AbstractTaskDMEventHandler(this);
            techniqueName = DM_TECHNIQUE_NAME;
        }
        mainVS = vsm.addVirtualSpace(mainVSname);
        vsm.setZoomLimit((int) FLOOR_ALTITUDE);
        demoCamera = vsm.addCamera(mainVSname);
        Vector cameras = new Vector();
        cameras.add(demoCamera);
        portalCamera = vsm.addCamera(mainVSname);
        demoView = vsm.addExternalView(cameras, techniqueName, View.STD_VIEW, VIEW_W, VIEW_H, false, true, false, null);
        logm = new AbstractTaskLogManager(this);
        demoView.setEventHandler(eh);
        demoView.setVisibilityPadding(vispad);
        demoView.mouse.setHintColor(HCURSOR_COLOR);
        demoView.setLocation(VIEW_X, VIEW_Y);
        robot.mouseMove(VIEW_X + VIEW_W / 2, VIEW_Y + VIEW_H / 2);
        updatePanelSize();
        demoView.getPanel().addComponentListener(eh);
        demoView.setNotifyMouseMoved(true);
        demoView.setJava2DPainter(this, Java2DPainter.FOREGROUND);
        buildWorld();
        buildGrid();
        if (this.technique == DM_TECHNIQUE) {
            initDM();
        } else if (this.technique == PZO_TECHNIQUE) {
            initOverview();
        }
        System.gc();
        logm.im.say(LocateTask.PSTS);
    }

    void initDM() {
        dmRegion = new VRectangle(0, 0, 0, 1, 1, Color.RED);
        dmRegion.setFilled(false);
        dmRegion.setBorderColor(Color.RED);
        vsm.addGlyph(dmRegion, mainVS);
        mainVS.hide(dmRegion);
    }

    void initOverview() {
        ovPortal = new OverviewPortal(VIEW_W - AbstractTaskInstructionsManager.PADDING - DM_PORTAL_WIDTH, VIEW_H - AbstractTaskInstructionsManager.PADDING - DM_PORTAL_HEIGHT, DM_PORTAL_WIDTH, DM_PORTAL_HEIGHT, portalCamera, demoCamera);
        ovPortal.setPortalEventHandler((PortalEventHandler) eh);
        ovPortal.setBackgroundColor(Color.LIGHT_GRAY);
        vsm.addPortal(ovPortal, demoView);
        ovPortal.setBorder(Color.BLACK);
        updateOverview();
    }

    void windowLayout() {
        if (Utilities.osIsWindows()) {
            VIEW_X = VIEW_Y = 0;
        } else if (Utilities.osIsMacOS()) {
            VIEW_X = 80;
            SCREEN_WIDTH -= 80;
        }
        VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
        VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }

    void buildWorld() {
        elementsByLevel = new ZRoundRect[TREE_DEPTH][DENSITY * DENSITY];
        elementsByLevel[0][0] = new ZRoundRect(0, 0, 0, widthByLevel[0], widthByLevel[0], COLOR_BY_LEVEL[0], cornerByLevel[0], cornerByLevel[0], false);
        vsm.addGlyph(elementsByLevel[0][0], mainVS);
        elementsByLevel[0][0].setDrawBorder(false);
        for (int i = 1; i < TREE_DEPTH; i++) {
            for (int j = 0; j < DENSITY; j++) {
                for (int k = 0; k < DENSITY; k++) {
                    elementsByLevel[i][j * DENSITY + k] = new ZRoundRect(offsetsByLevel[i][j * DENSITY + k].x, offsetsByLevel[i][j * DENSITY + k].y, 0, widthByLevel[i], widthByLevel[i], COLOR_BY_LEVEL[1], cornerByLevel[i], cornerByLevel[i], false);
                    vsm.addGlyph(elementsByLevel[i][j * DENSITY + k], mainVS);
                    elementsByLevel[i][j * DENSITY + k].setDrawBorder(true);
                    elementsByLevel[i][j * DENSITY + k].setBorderColor(DISC_BORDER_COLOR);
                    elementsByLevel[i][j * DENSITY + k].setType(ZLAbstractTask.GLYPH_TYPE_WORLD);
                    elementsByLevel[i][j * DENSITY + k].setOwner(String.valueOf(j * DENSITY + k + 1));
                }
            }
        }
        resetVisits();
        hideTargets();
    }

    void resetWorld() {
        for (int i = 2; i < TREE_DEPTH; i++) {
            for (int j = 0; j < DENSITY; j++) {
                for (int k = 0; k < DENSITY; k++) {
                    elementsByLevel[i][j * DENSITY + k].moveTo(offsetsByLevel[i][j * DENSITY + k].x, offsetsByLevel[i][j * DENSITY + k].y);
                }
            }
        }
        for (int i = 1; i < TREE_DEPTH; i++) {
            for (int j = 0; j < DENSITY * DENSITY; j++) {
                elementsByLevel[i][j].setColor(COLOR_BY_LEVEL[i]);
                elementsByLevel[i][j].setBorderColor(DISC_BORDER_COLOR);
            }
        }
        resetVisits();
        hideTargets();
    }

    void resetVisits() {
        for (int i = 1; i < TREE_DEPTH; i++) {
            for (int j = 0; j < DENSITY * DENSITY; j++) {
                visitsByLevel[i][j] = false;
            }
        }
    }

    void hideTargets() {
        for (int i = 1; i < TREE_DEPTH; i++) {
            for (int j = 0; j < DENSITY * DENSITY; j++) {
                elementsByLevel[i][j].renderRound(false);
            }
        }
    }

    void buildGrid() {
        ZSegment s = new ZSegment(-HALF_WORLD_WIDTH, 0, 0, 0, HALF_WORLD_HEIGHT, GRID_COLOR);
        s.setType(GLYPH_TYPE_GRID);
        vsm.addGlyph(s, mainVSname);
        s = new ZSegment(HALF_WORLD_WIDTH, 0, 0, 0, HALF_WORLD_HEIGHT, GRID_COLOR);
        s.setType(GLYPH_TYPE_GRID);
        vsm.addGlyph(s, mainVSname);
        s = new ZSegment(0, -HALF_WORLD_HEIGHT, 0, HALF_WORLD_WIDTH, 0, GRID_COLOR);
        s.setType(GLYPH_TYPE_GRID);
        vsm.addGlyph(s, mainVSname);
        s = new ZSegment(0, HALF_WORLD_HEIGHT, 0, HALF_WORLD_WIDTH, 0, GRID_COLOR);
        s.setType(GLYPH_TYPE_GRID);
        vsm.addGlyph(s, mainVSname);
        tmpHGrid = new Vector();
        tmpVGrid = new Vector();
        buildHorizontalGridLevel(-HALF_WORLD_HEIGHT, HALF_WORLD_HEIGHT, 0);
        buildVerticalGridLevel(-HALF_WORLD_WIDTH, HALF_WORLD_WIDTH, 0);
        storeGrid();
        showGridLevel(1);
    }

    void buildHorizontalGridLevel(long c1, long c2, int depth) {
        long c = (c1 + c2) / 2;
        ZSegment s = new ZSegment(0, c, 0, HALF_WORLD_WIDTH, 0, GRID_COLOR);
        storeSegmentInHGrid(s, depth);
        vsm.addGlyph(s, mainVSname);
        s.setType(GLYPH_TYPE_GRID);
        s.setVisible(false);
        if (depth < GRID_DEPTH) {
            buildHorizontalGridLevel(c1, c, depth + 1);
            buildHorizontalGridLevel(c, c2, depth + 1);
        }
    }

    void buildVerticalGridLevel(long c1, long c2, int depth) {
        long c = (c1 + c2) / 2;
        ZSegment s = new ZSegment(c, 0, 0, 0, HALF_WORLD_HEIGHT, GRID_COLOR);
        storeSegmentInVGrid(s, depth);
        vsm.addGlyph(s, mainVSname);
        s.setType(GLYPH_TYPE_GRID);
        s.setVisible(false);
        if (depth < GRID_DEPTH) {
            buildVerticalGridLevel(c1, c, depth + 1);
            buildVerticalGridLevel(c, c2, depth + 1);
        }
    }

    void storeSegmentInHGrid(ZSegment s, int depth) {
        if (tmpHGrid.size() > depth) {
            Vector v = (Vector) tmpHGrid.elementAt(depth);
            v.add(s);
        } else {
            Vector v = new Vector();
            v.add(s);
            tmpHGrid.add(v);
        }
    }

    void storeSegmentInVGrid(ZSegment s, int depth) {
        if (tmpVGrid.size() > depth) {
            Vector v = (Vector) tmpVGrid.elementAt(depth);
            v.add(s);
        } else {
            Vector v = new Vector();
            v.add(s);
            tmpVGrid.add(v);
        }
    }

    void storeGrid() {
        int levelSize;
        Vector v;
        for (int i = 0; i < tmpHGrid.size(); i++) {
            v = (Vector) tmpHGrid.elementAt(i);
            levelSize = v.size();
            hGridLevels[i] = new ZSegment[levelSize];
            for (int j = 0; j < v.size(); j++) {
                hGridLevels[i][j] = (ZSegment) v.elementAt(j);
            }
        }
        for (int i = 0; i < tmpVGrid.size(); i++) {
            v = (Vector) tmpVGrid.elementAt(i);
            levelSize = v.size();
            vGridLevels[i] = new ZSegment[levelSize];
            for (int j = 0; j < v.size(); j++) {
                vGridLevels[i][j] = (ZSegment) v.elementAt(j);
            }
        }
    }

    void showGridLevel(int level) {
        if (level > GRID_DEPTH || level < -1 || level == currentLevel) {
            return;
        }
        if (level < currentLevel) {
            for (int i = level + 1; i <= currentLevel; i++) {
                for (int j = 0; j < hGridLevels[i].length; j++) {
                    hGridLevels[i][j].setVisible(false);
                }
            }
            for (int i = level + 1; i <= currentLevel; i++) {
                for (int j = 0; j < vGridLevels[i].length; j++) {
                    vGridLevels[i][j].setVisible(false);
                }
            }
        } else if (level > currentLevel) {
            for (int i = currentLevel + 1; i <= level; i++) {
                for (int j = 0; j < hGridLevels[i].length; j++) {
                    hGridLevels[i][j].setVisible(true);
                }
            }
            for (int i = currentLevel + 1; i <= level; i++) {
                for (int j = 0; j < vGridLevels[i].length; j++) {
                    vGridLevels[i][j].setVisible(true);
                }
            }
        }
        currentLevel = level;
    }

    void updateGridLevel(long visibleSize) {
        if (visibleSize < 48828.0f) {
            showGridLevel(4);
        } else if (visibleSize < 97656.0f) {
            showGridLevel(3);
        } else if (visibleSize < 195312.0f) {
            showGridLevel(2);
        } else {
            showGridLevel(1);
        }
    }

    void updateOverview() {
        portalCamera.setAltitude((float) ((demoCamera.getAltitude() + demoCamera.getFocal()) * 24 - demoCamera.getFocal()));
    }

    void centerOverview() {
        vsm.animator.createCameraAnimation(300, AnimManager.CA_TRANS_SIG, new LongPoint(demoCamera.posx - portalCamera.posx, demoCamera.posy - portalCamera.posy), portalCamera.getID(), null);
    }

    void setLens(int t) {
        eh.lensType = t;
        switch(eh.lensType) {
            case AbstractTaskEventHandler.ZOOMIN_LENS:
                {
                    logm.lensStatus = AbstractTaskLogManager.ZOOMIN_LENS;
                    logm.lensPositionChanged(true);
                    break;
                }
            case AbstractTaskEventHandler.ZOOMOUT_LENS:
                {
                    logm.lensStatus = AbstractTaskLogManager.ZOOMOUT_LENS;
                    logm.lensPositionChanged(true);
                    break;
                }
            case AbstractTaskEventHandler.NO_LENS:
                {
                    logm.lensStatus = AbstractTaskLogManager.NO_LENS;
                    logm.lensxS = AbstractTaskLogManager.NaN;
                    logm.lensyS = AbstractTaskLogManager.NaN;
                    break;
                }
        }
    }

    void moveLens(int x, int y, boolean write) {
        lens.setAbsolutePosition(x, y);
        logm.lensPositionChanged(write);
        vsm.repaintNow();
    }

    void zoomInRegion(long[] wnes) {
        long c2x = (wnes[2] + wnes[0]) / 2;
        long c2y = (wnes[1] + wnes[3]) / 2;
        long[] regionBounds = demoView.getVisibleRegion(demoCamera);
        long[] newRegionDimensions = { regionBounds[0] + c2x - demoCamera.posx, regionBounds[3] + c2y - demoCamera.posy };
        float ratio = 0;
        if (newRegionDimensions[0] != 0) {
            ratio = (c2x - wnes[0]) / ((float) (c2x - newRegionDimensions[0]));
        }
        if (newRegionDimensions[1] != 0) {
            float tmpRatio = (c2y - wnes[3]) / ((float) (c2y - newRegionDimensions[1]));
            if (tmpRatio > ratio) {
                ratio = tmpRatio;
            }
        }
        float cameraAbsAlt = demoCamera.getAltitude() + demoCamera.getFocal();
        Float deltAlt = new Float(cameraAbsAlt * Math.abs(ratio) - cameraAbsAlt);
        Vector cadata = new Vector();
        if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE) {
            cadata.add(deltAlt);
            cadata.add(new LongPoint(c2x - demoCamera.posx, c2y - demoCamera.posy));
            vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new AbstractTaskZIP2CameraAction(this));
        } else {
            Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
            double ratio2 = actualDeltAlt.floatValue() / deltAlt.floatValue();
            cadata.add(actualDeltAlt);
            cadata.add(new LongPoint(Math.round((c2x - demoCamera.posx) * ratio2), Math.round((c2y - demoCamera.posy) * ratio2)));
            vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new AbstractTaskZIP2CameraAction(this));
        }
    }

    void zoomOutOfRegion(long[] wnes) {
        long[] viewportRegion = demoView.getVisibleRegion(demoCamera);
        float ratio = Math.min((viewportRegion[2] - viewportRegion[0]) / ((float) (wnes[2] - wnes[0])), (viewportRegion[1] - viewportRegion[3]) / ((float) (wnes[1] - wnes[3])));
        float cameraAbsAlt = demoCamera.getAltitude() + demoCamera.getFocal();
        Float deltAlt = new Float((ratio - 1) * cameraAbsAlt);
        Vector cadata = new Vector();
        cadata.add(deltAlt);
        cadata.add(new LongPoint(0, 0));
        vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new AbstractTaskZIP2CameraAction(this));
    }

    void zoomInPhase1(int x, int y) {
        if (lens == null) {
            Dimension d = demoView.getPanel().getSize();
            lens = demoView.setLens(new FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2));
            lens.setBufferThreshold(1);
        }
        vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR - 1), lens.getID(), null);
        setLens(AbstractTaskEventHandler.ZOOMIN_LENS);
    }

    void zoomInPhase2(long mx, long my) {
        float cameraAbsAlt = demoCamera.getAltitude() + demoCamera.getFocal();
        long c2x = Math.round(mx - INV_MAG_FACTOR * (mx - demoCamera.posx));
        long c2y = Math.round(my - INV_MAG_FACTOR * (my - demoCamera.posy));
        Vector cadata = new Vector();
        Float deltAlt = new Float((cameraAbsAlt) * (1 - MAG_FACTOR) / MAG_FACTOR);
        if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE) {
            cadata.add(deltAlt);
            cadata.add(new LongPoint(c2x - demoCamera.posx, c2y - demoCamera.posy));
            vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR + 1), lens.getID(), new AbstractTaskZIP2LensAction(this));
            vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new AbstractTaskZIP2CameraAction(this));
        } else {
            Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
            double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
            cadata.add(actualDeltAlt);
            cadata.add(new LongPoint(Math.round((c2x - demoCamera.posx) * ratio), Math.round((c2y - demoCamera.posy) * ratio)));
            vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR + 1), lens.getID(), new AbstractTaskZIP2LensAction(this));
            vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new AbstractTaskZIP2CameraAction(this));
        }
    }

    void zoomOutPhase1(int x, int y, long mx, long my) {
        float cameraAbsAlt = demoCamera.getAltitude() + demoCamera.getFocal();
        long c2x = Math.round(mx - MAG_FACTOR * (mx - demoCamera.posx));
        long c2y = Math.round(my - MAG_FACTOR * (my - demoCamera.posy));
        Vector cadata = new Vector();
        cadata.add(new Float(cameraAbsAlt * (MAG_FACTOR - 1)));
        cadata.add(new LongPoint(c2x - demoCamera.posx, c2y - demoCamera.posy));
        if (lens == null) {
            Dimension d = demoView.getPanel().getSize();
            lens = demoView.setLens(new FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2));
            lens.setBufferThreshold(1);
        }
        vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR - 1), lens.getID(), null);
        vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), null);
        setLens(AbstractTaskEventHandler.ZOOMOUT_LENS);
    }

    void zoomOutPhase2() {
        vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR + 1), lens.getID(), new AbstractTaskZOP2LensAction(this));
    }

    void killLens() {
        vsm.getOwningView(lens.getID()).setLens(null);
        lens.dispose();
        setMagFactor(ZLAbstractTask.DEFAULT_MAG_FACTOR);
        lens = null;
        setLens(WorldTaskEventHandler.NO_LENS);
    }

    void setMagFactor(double m) {
        MAG_FACTOR = m;
        INV_MAG_FACTOR = 1 / MAG_FACTOR;
        if (logm.trialStarted) {
            logm.lensmmS = TrialInfo.doubleFormatter(MAG_FACTOR);
            logm.writeCinematic();
        }
    }

    synchronized void magnifyFocus(double magOffset, int zooming, Camera ca) {
        synchronized (lens) {
            double nmf = MAG_FACTOR + magOffset;
            if (nmf <= MAX_MAG_FACTOR && nmf > 1.0f) {
                setMagFactor(nmf);
                if (zooming == AbstractTaskEventHandler.ZOOMOUT_LENS) {
                    float a1 = demoCamera.getAltitude();
                    lens.setMaximumMagnification((float) nmf, true);
                    demoCamera.altitudeOffset((float) ((a1 + demoCamera.getFocal()) * magOffset / (MAG_FACTOR - magOffset)));
                    demoCamera.move(Math.round((a1 - demoCamera.getAltitude()) / demoCamera.getFocal() * lens.lx), -Math.round((a1 - demoCamera.getAltitude()) / demoCamera.getFocal() * lens.ly));
                } else {
                    vsm.animator.createLensAnimation(WHEEL_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(magOffset), lens.getID(), null);
                }
            }
        }
    }

    void triggerDM(int x, int y) {
        if (dmPortal != null) {
            killDM();
            logm.lensStatus = AbstractTaskLogManager.NO_LENS;
        } else {
            createDM(x, y);
            logm.lensStatus = AbstractTaskLogManager.DM_LENS;
        }
        logm.portalPositionChanged(true);
    }

    void createDM(int x, int y) {
        dmPortal = new DraggableCameraPortal(x, y, DM_PORTAL_WIDTH, DM_PORTAL_HEIGHT, portalCamera);
        dmPortal.setPortalEventHandler((PortalEventHandler) eh);
        dmPortal.setBackgroundColor(Color.LIGHT_GRAY);
        vsm.addPortal(dmPortal, demoView);
        dmPortal.setBorder(Color.RED);
        Location l = dmPortal.getSeamlessView(demoCamera);
        portalCamera.moveTo(l.vx, l.vy);
        portalCamera.setAltitude((float) ((demoCamera.getAltitude() + demoCamera.getFocal()) / (DEFAULT_MAG_FACTOR) - demoCamera.getFocal()));
        updateDMRegion();
        int w = Math.round(dmRegion.getWidth() * 2 * demoCamera.getFocal() / ((float) (demoCamera.getFocal() + demoCamera.getAltitude())));
        int h = Math.round(dmRegion.getHeight() * 2 * demoCamera.getFocal() / ((float) (demoCamera.getFocal() + demoCamera.getAltitude())));
        dmPortal.sizeTo(w, h);
        mainVS.show(dmRegion);
        paintLinks = true;
        Point[] data = { new Point(DM_PORTAL_WIDTH - w, DM_PORTAL_HEIGHT - h), new Point(DM_PORTAL_INITIAL_X_OFFSET - w / 2, DM_PORTAL_INITIAL_Y_OFFSET - h / 2) };
        vsm.animator.createPortalAnimation(150, AnimManager.PT_SZ_TRANS_LIN, data, dmPortal.getID(), new DMActivator(this, x, y));
    }

    void killDM() {
        vsm.destroyPortal(dmPortal);
        dmPortal = null;
        mainVS.hide(dmRegion);
        paintLinks = false;
        ((AbstractTaskDMEventHandler) eh).inPortal = false;
    }

    void meetDM() {
        if (dmPortal != null) {
            Vector data = new Vector();
            data.add(new Float(portalCamera.getAltitude() - demoCamera.getAltitude()));
            data.add(new LongPoint(portalCamera.posx - demoCamera.posx, portalCamera.posy - demoCamera.posy));
            vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH, AnimManager.CA_ALT_TRANS_SIG, data, demoCamera.getID());
            vsm.destroyPortal(dmPortal);
            dmPortal = null;
            mainVS.hide(dmRegion);
            paintLinks = false;
            ((AbstractTaskDMEventHandler) eh).inPortal = false;
        }
    }

    void altitudeChanged() {
        long[] wnes = demoView.getVisibleRegion(demoCamera);
        updateGridLevel(Math.max(wnes[2] - wnes[0], wnes[1] - wnes[3]));
    }

    void getGlobalView() {
        Location l = vsm.getGlobalView(demoCamera, ANIM_MOVE_LENGTH);
    }

    long[] dmwnes = new long[4];

    void updateDMRegion() {
        if (dmPortal == null) {
            return;
        }
        dmPortal.getVisibleRegion(dmwnes);
        dmRegion.moveTo(portalCamera.posx, portalCamera.posy);
        dmRegion.setWidth((dmwnes[2] - dmwnes[0]) / 2 + 1);
        dmRegion.setHeight((dmwnes[1] - dmwnes[3]) / 2 + 1);
    }

    void updateDMWindow() {
        portalCamera.moveTo(dmRegion.vx, dmRegion.vy);
        logm.portalPositionChanged(true);
    }

    public void paint(Graphics2D g2d, int viewWidth, int viewHeight) {
        if (paintLinks) {
            float coef = (float) (demoCamera.focal / (demoCamera.focal + demoCamera.altitude));
            int dmRegionX = (viewWidth / 2) + Math.round((dmRegion.vx - demoCamera.posx) * coef);
            int dmRegionY = (viewHeight / 2) - Math.round((dmRegion.vy - demoCamera.posy) * coef);
            int dmRegionW = Math.round(dmRegion.getWidth() * coef);
            int dmRegionH = Math.round(dmRegion.getHeight() * coef);
            g2d.setColor(Color.RED);
            g2d.drawLine(dmRegionX - dmRegionW, dmRegionY - dmRegionH, dmPortal.x, dmPortal.y);
            g2d.drawLine(dmRegionX + dmRegionW, dmRegionY - dmRegionH, dmPortal.x + dmPortal.w, dmPortal.y);
            g2d.drawLine(dmRegionX - dmRegionW, dmRegionY + dmRegionH, dmPortal.x, dmPortal.y + dmPortal.h);
            g2d.drawLine(dmRegionX + dmRegionW, dmRegionY + dmRegionH, dmPortal.x + dmPortal.w, dmPortal.y + dmPortal.h);
        }
    }

    static int START_BUTTON_TL_X = 0;

    static int START_BUTTON_TL_Y = 0;

    static int START_BUTTON_BR_X = 0;

    static int START_BUTTON_BR_Y = 0;

    static final int START_BUTTON_W = 80;

    static final int START_BUTTON_H = 20;

    void updatePanelSize() {
        Dimension d = demoView.getPanel().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
        hpanelWidth = panelWidth / 2;
        hpanelHeight = panelHeight / 2;
        SELECTION_RECT_X = panelWidth / 2 - SELECTION_RECT_W / 2;
        SELECTION_RECT_Y = panelHeight / 2 - SELECTION_RECT_H / 2;
        START_BUTTON_TL_X = hpanelWidth - START_BUTTON_W / 2;
        START_BUTTON_TL_Y = hpanelHeight + START_BUTTON_H / 2;
        START_BUTTON_BR_X = START_BUTTON_TL_X + START_BUTTON_W;
        START_BUTTON_BR_Y = START_BUTTON_TL_Y + START_BUTTON_H;
    }

    static boolean clickOnStartButton(int jpx, int jpy) {
        return (jpx >= START_BUTTON_TL_X && jpy >= START_BUTTON_TL_Y && jpx <= START_BUTTON_BR_X && jpy <= START_BUTTON_BR_Y);
    }

    void cameraIsOnFloor(boolean b) {
        if (b != cameraOnFloor) {
            cameraOnFloor = b;
        }
    }

    public void animationEnded(Object target, short type, String dimension) {
        ((Glyph) target).setVisible(false);
    }

    void gc() {
        System.gc();
    }

    public static void main(String[] args) {
        final short tech = (args.length > 0) ? Short.parseShort(args[0]) : PZ_TECHNIQUE;
        new ZLAbstractTask(tech);
    }
}
