package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import net.claribole.zvtm.engine.DraggableCameraPortal;
import net.claribole.zvtm.engine.Location;
import net.claribole.zvtm.engine.OverviewPortal;
import net.claribole.zvtm.engine.PortalEventHandler;
import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.*;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.ZSegment;

public class ZLWorldTask implements PostAnimationAction, MapApplication {

    static int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;

    static int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;

    static final int VIEW_MAX_W = 1280;

    static final int VIEW_MAX_H = 720;

    int VIEW_W, VIEW_H, CONSOLE_W, MAP_MONITOR_W, INSTRUCTIONS_W;

    int CONSOLE_MON_H = 100;

    int INSTRUCTIONS_H = 0;

    int VIEW_X, VIEW_Y, CONSOLE_X, CONSOLE_Y, MAP_MONITOR_X, MAP_MONITOR_Y, INSTRUCTIONS_X, INSTRUCTIONS_Y;

    static boolean SHOW_MEMORY_USAGE = false;

    static boolean SHOW_COORDS = false;

    static boolean SHOW_MAP_MONITOR = false;

    static boolean SHOW_CONSOLE = false;

    static boolean SHOW_INSTRUCTIONS = true;

    static final short MOVE_UP = 0;

    static final short MOVE_DOWN = 1;

    static final short MOVE_LEFT = 2;

    static final short MOVE_RIGHT = 3;

    static int ANIM_MOVE_LENGTH = 300;

    Console console;

    MapMonitor mapMonitor;

    int panelWidth, panelHeight;

    int hpanelWidth, hpanelHeight;

    static final int MAIN_MAP_WIDTH = 8000;

    static final int MAIN_MAP_HEIGHT = 4000;

    static final long MAP_WIDTH = Math.round(MAIN_MAP_WIDTH * MapData.MN000factor.doubleValue());

    static final long MAP_HEIGHT = Math.round(MAIN_MAP_HEIGHT * MapData.MN000factor.doubleValue());

    static final long HALF_MAP_WIDTH = Math.round(MAP_WIDTH / 2.0);

    static final long HALF_MAP_HEIGHT = Math.round(MAP_HEIGHT / 2.0);

    static final String LOADING_WORLDMAP_TEXT = "Loading World Map (" + MAIN_MAP_WIDTH + "x" + MAIN_MAP_HEIGHT + ") ...";

    VirtualSpaceManager vsm;

    WorldTaskEventHandler eh;

    ZLWorldTaskMapManager ewmm;

    GeoDataStore gds;

    LogManager logm;

    View demoView;

    Camera demoCamera;

    VirtualSpace mainVS;

    static String mainVSname = "mainSpace";

    Lens lens;

    TemporalLens tLens;

    static int LENS_R1 = 100;

    static int LENS_R2 = 50;

    static final int WHEEL_ANIM_TIME = 50;

    static final int LENS_ANIM_TIME = 300;

    static final int GRID_ANIM_TIME = 500;

    static double DEFAULT_MAG_FACTOR = 4.0;

    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;

    static double INV_MAG_FACTOR = 1 / MAG_FACTOR;

    static final short L1_Linear = 0;

    static final short L1_InverseCosine = 1;

    static final short L1_Manhattan = 2;

    static final short L2_Gaussian = 3;

    static final short L2_Linear = 4;

    static final short L2_InverseCosine = 5;

    static final short L2_Manhattan = 6;

    static final short L2_Scrambling = 7;

    static final short LInf_Linear = 8;

    static final short LInf_InverseCosine = 9;

    static final short LInf_Manhattan = 10;

    static final short L1_Fresnel = 11;

    static final short L2_Fresnel = 12;

    static final short LInf_Fresnel = 13;

    static final short L2_TGaussian = 14;

    static final short L2_Fading = 15;

    static final short LInf_Fading = 16;

    static final short LInf_Gaussian = 17;

    static final short L3_Linear = 18;

    static final short L3_Manhattan = 19;

    static final short L3_Gaussian = 20;

    static final short L3_InverseCosine = 21;

    static final short L3_Fresnel = 22;

    static final short LInf_TLinear = 23;

    static final short L3_TLinear = 24;

    static final short L2_HLinear = 25;

    static final short L2_DLinear = 26;

    static final short L2_XGaussian = 27;

    static final short LP_Gaussian = 28;

    short lensFamily = L2_Gaussian;

    static final String View_Title_Prefix = "Probing Lens Demo - ";

    static final String L1_Linear_Title = View_Title_Prefix + "L1 / Linear";

    static final String L1_InverseCosine_Title = View_Title_Prefix + "L1 / Inverse Cosine";

    static final String L1_Manhattan_Title = View_Title_Prefix + "L1 / Manhattan";

    static final String L2_Gaussian_Title = View_Title_Prefix + "L2 / Gaussian";

    static final String L2_Linear_Title = View_Title_Prefix + "L2 / Linear";

    static final String L2_InverseCosine_Title = View_Title_Prefix + "L2 / Inverse Cosine";

    static final String L2_Manhattan_Title = View_Title_Prefix + "L2 / Manhattan";

    static final String L2_Scrambling_Title = View_Title_Prefix + "L2 / Scrambling (for fun)";

    static final String LInf_Linear_Title = View_Title_Prefix + "LInf / Linear";

    static final String LInf_InverseCosine_Title = View_Title_Prefix + "LInf / Inverse Cosine";

    static final String LInf_Manhattan_Title = View_Title_Prefix + "LInf / Manhattan";

    static final String L1_Fresnel_Title = View_Title_Prefix + "L1 / Fresnel";

    static final String L2_Fresnel_Title = View_Title_Prefix + "L2 / Fresnel";

    static final String LInf_Fresnel_Title = View_Title_Prefix + "LInf / Fresnel";

    static final String L2_TGaussian_Title = View_Title_Prefix + "L2 / Translucence Gaussian";

    static final String L2_HLinear_Title = View_Title_Prefix + "L2 / Translucence Linear";

    static final String L2_Fading_Title = View_Title_Prefix + "L2 / Fading";

    static final String LInf_Fading_Title = View_Title_Prefix + "LInf / Fading";

    static final String LInf_Gaussian_Title = View_Title_Prefix + "LInf / Gaussian";

    static final String L3_Linear_Title = View_Title_Prefix + "L3 / Linear";

    static final String L3_InverseCosine_Title = View_Title_Prefix + "L3 / Inverse Cosine";

    static final String L3_Gaussian_Title = View_Title_Prefix + "L3 / Gaussian";

    static final String L3_Manhattan_Title = View_Title_Prefix + "L3 / Manhattan";

    static final String L3_Fresnel_Title = View_Title_Prefix + "L3 / Fresnel";

    static final String LInf_TLinear_Title = View_Title_Prefix + "LInf / Translucence Linear";

    static final String L3_TLinear_Title = View_Title_Prefix + "L3 / Translucence Linear";

    static final String L2_DLinear_Title = View_Title_Prefix + "L2 / Dynamic Linear";

    static final String L2_XGaussian_Title = View_Title_Prefix + "L2 / eXtended Gaussian";

    static final String LP_Gaussian_Title = View_Title_Prefix + "LP / Gaussian";

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

    Camera overviewCamera;

    static final Color HCURSOR_COLOR = new Color(200, 48, 48);

    static final Color CURSOR_COLOR = new Color(200, 48, 48);

    static final Color GRID_COLOR = new Color(156, 53, 53);

    static final int GRID_DEPTH = 8;

    int currentLevel = -1;

    static final float START_ALTITUDE = 4900;

    static final float FLOOR_ALTITUDE = 100.0f;

    boolean cameraOnFloor = false;

    int SELECTION_RECT_X = 0;

    int SELECTION_RECT_Y = 0;

    int SELECTION_RECT_W = 16;

    int SELECTION_RECT_H = 16;

    int SELECTION_RECT_HW = SELECTION_RECT_W / 2;

    int SELECTION_RECT_HH = SELECTION_RECT_H / 2;

    boolean SHOW_SELECTION_RECT = true;

    static final Color SELECTION_RECT_COLOR = Color.BLUE;

    ZLWorldScreenSaver screenSaver;

    static java.awt.Robot robot;

    static final short ZL_TECHNIQUE = 0;

    static final short PZ_TECHNIQUE = 1;

    static final short RZ_TECHNIQUE = 2;

    static final short PZA_TECHNIQUE = 3;

    static final short DZ_TECHNIQUE = 4;

    static final short DZA_TECHNIQUE = 5;

    static final short PZL_TECHNIQUE = 6;

    static final short SS_TECHNIQUE = 7;

    static final short DM_TECHNIQUE = 8;

    static final String PZ_TECHNIQUE_NAME = "Pan-Zoom (centered on view)";

    static final String PZA_TECHNIQUE_NAME = "Pan-Zoom (centered on cursor)";

    static final String ZL_TECHNIQUE_NAME = "Lens-Zoom";

    static final String PZL_TECHNIQUE_NAME = "Pan-Zoom-Lens";

    static final String SS_TECHNIQUE_NAME = "Screen Saver";

    static final String DZ_TECHNIQUE_NAME = "Pan-Discrete Zoom";

    static final String DZA_TECHNIQUE_NAME = "Pan-Discrete Zoom (animated transitions)";

    static final String RZ_TECHNIQUE_NAME = "Region Zoom (animated transitions)";

    static final String DM_TECHNIQUE_NAME = "DragMag";

    short technique = ZL_TECHNIQUE;

    String techniqueName;

    static final int[] vispad = { 0, 0, 0, 0 };

    ZLWorldTask(short t, boolean showConsole, boolean showMapMonitor, boolean showInstructions, boolean trainingData) {
        SHOW_MAP_MONITOR = showMapMonitor;
        SHOW_CONSOLE = showConsole;
        SHOW_INSTRUCTIONS = showInstructions;
        vsm = new VirtualSpaceManager();
        vsm.setDebug(true);
        gds = new GeoDataStore(this, trainingData);
        init(t);
    }

    public void init(short t) {
        try {
            robot = new java.awt.Robot();
        } catch (java.awt.AWTException ex) {
            ex.printStackTrace();
        }
        this.technique = t;
        if (this.technique == ZL_TECHNIQUE) {
            eh = new ZLEventHandler(this);
            techniqueName = ZL_TECHNIQUE_NAME;
        } else if (this.technique == PZ_TECHNIQUE) {
            eh = new PZEventHandler(this);
            techniqueName = PZ_TECHNIQUE_NAME;
        } else if (this.technique == RZ_TECHNIQUE) {
            eh = new RZEventHandler(this);
            techniqueName = RZ_TECHNIQUE_NAME;
        } else if (this.technique == PZL_TECHNIQUE) {
            eh = new PZLEventHandler(this);
            techniqueName = PZL_TECHNIQUE_NAME;
        } else if (this.technique == PZA_TECHNIQUE) {
            eh = new PZAEventHandler(this);
            techniqueName = PZA_TECHNIQUE_NAME;
        } else if (this.technique == DZ_TECHNIQUE) {
            eh = new DZEventHandler(this);
            techniqueName = DZ_TECHNIQUE_NAME;
        } else if (this.technique == DZA_TECHNIQUE) {
            eh = new DZAEventHandler(this);
            techniqueName = DZA_TECHNIQUE_NAME;
        } else if (this.technique == DM_TECHNIQUE) {
            eh = new DMEventHandler(this);
            techniqueName = DM_TECHNIQUE_NAME;
        } else {
            eh = new SSEventHandler(this);
            techniqueName = SS_TECHNIQUE_NAME;
            SHOW_SELECTION_RECT = false;
        }
        windowLayout();
        vsm.setMainFont(GeoDataStore.CITY_FONT);
        mainVS = vsm.addVirtualSpace(mainVSname);
        vsm.setZoomLimit(0);
        demoCamera = vsm.addCamera(mainVSname);
        Vector cameras = new Vector();
        cameras.add(demoCamera);
        portalCamera = vsm.addCamera(mainVSname);
        overviewCamera = vsm.addCamera(mainVSname);
        demoView = vsm.addExternalView(cameras, techniqueName, View.STD_VIEW, VIEW_W, VIEW_H, false, true, false, null);
        demoView.setVisibilityPadding(vispad);
        demoView.mouse.setHintColor(HCURSOR_COLOR);
        demoView.mouse.setColor(CURSOR_COLOR);
        demoView.setLocation(VIEW_X, VIEW_Y);
        robot.mouseMove(VIEW_X + VIEW_W / 2, VIEW_Y + VIEW_H / 2);
        updatePanelSize();
        demoView.setEventHandler(eh);
        demoView.getPanel().addComponentListener(eh);
        demoView.setNotifyMouseMoved(true);
        if (SHOW_CONSOLE) {
            initConsole(CONSOLE_X, CONSOLE_Y, CONSOLE_W, CONSOLE_MON_H, true);
            console.append(Utils.miscInfo, Console.GRAY_STYLE);
        }
        demoCamera.setAltitude(START_ALTITUDE);
        ewmm = new ZLWorldTaskMapManager(this, vsm, mainVS, demoCamera, demoView);
        if (SHOW_MAP_MONITOR) {
            initMapMonitor(MAP_MONITOR_X, MAP_MONITOR_Y, MAP_MONITOR_W, CONSOLE_MON_H);
            ewmm.setMapMonitor(mapMonitor);
            ewmm.initMap();
            mapMonitor.updateMaps();
        } else {
            ewmm.initMap();
        }
        buildGrid();
        gds.buildAll();
        logm = new LogManager(this);
        System.gc();
        if (this.technique == SS_TECHNIQUE) {
            screenSaver = new ZLWorldScreenSaver(this);
        } else if (this.technique == DM_TECHNIQUE) {
            initDM();
        } else {
            logm.im.say(LocateTask.PSTS);
        }
    }

    void initDM() {
        dmRegion = new VRectangle(0, 0, 0, 1, 1, Color.RED);
        dmRegion.setFilled(false);
        dmRegion.setBorderColor(Color.RED);
        vsm.addGlyph(dmRegion, mainVS);
        mainVS.hide(dmRegion);
    }

    void createOverview() {
        ovPortal = new OverviewPortal(VIEW_W - AbstractTaskInstructionsManager.PADDING - DM_PORTAL_WIDTH, VIEW_H - AbstractTaskInstructionsManager.PADDING - DM_PORTAL_HEIGHT / 2, DM_PORTAL_WIDTH, DM_PORTAL_HEIGHT / 2, overviewCamera, demoCamera);
        ovPortal.setPortalEventHandler(new WorldOverviewEventHandler(this));
        ovPortal.setBackgroundColor(Color.LIGHT_GRAY);
        ovPortal.setObservedRegionTranslucency(0.5f);
        vsm.addPortal(ovPortal, demoView);
        ovPortal.setBorder(Color.RED);
        updateOverview();
        vsm.repaintNow();
    }

    void windowLayout() {
        if (Utilities.osIsWindows()) {
            VIEW_X = VIEW_Y = 0;
        } else if (Utilities.osIsMacOS()) {
            VIEW_X = 80;
            SCREEN_WIDTH -= 80;
        }
        VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
        if (SHOW_CONSOLE || SHOW_MAP_MONITOR) {
            VIEW_H = SCREEN_HEIGHT - CONSOLE_MON_H;
            if (VIEW_H > VIEW_MAX_H) {
                VIEW_H = VIEW_MAX_H;
            }
            if (Utilities.osIsMacOS()) {
                VIEW_H -= 22;
            }
            if (SHOW_CONSOLE) {
                if (SHOW_MAP_MONITOR) {
                    CONSOLE_X = VIEW_X;
                    CONSOLE_Y = SCREEN_HEIGHT - CONSOLE_MON_H;
                    CONSOLE_W = SCREEN_WIDTH / 2;
                    MAP_MONITOR_X = VIEW_X + CONSOLE_W;
                    MAP_MONITOR_Y = CONSOLE_Y;
                    MAP_MONITOR_W = SCREEN_WIDTH / 2;
                } else {
                    CONSOLE_X = VIEW_X;
                    CONSOLE_Y = SCREEN_HEIGHT - CONSOLE_MON_H;
                    CONSOLE_W = SCREEN_WIDTH / 2;
                }
            } else if (SHOW_MAP_MONITOR) {
                MAP_MONITOR_X = VIEW_X;
                MAP_MONITOR_Y = SCREEN_HEIGHT - CONSOLE_MON_H;
                MAP_MONITOR_W = SCREEN_WIDTH / 2;
            }
        } else {
            VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
            VIEW_H -= INSTRUCTIONS_H;
        }
        INSTRUCTIONS_W = VIEW_W;
        INSTRUCTIONS_X = VIEW_X;
        INSTRUCTIONS_Y = VIEW_H;
        if (Utilities.osIsMacOS()) {
            INSTRUCTIONS_Y += 22;
            if (INSTRUCTIONS_Y + INSTRUCTIONS_H > SCREEN_HEIGHT) {
                INSTRUCTIONS_H = SCREEN_HEIGHT - INSTRUCTIONS_Y;
            }
        }
    }

    void initConsole(int x, int y, int w, int h, boolean visible) {
        console = new Console("Console", x, y, w, h, visible);
    }

    void initMapMonitor(int x, int y, int w, int h) {
        mapMonitor = new MapMonitor("Map Monitor", x, y, w, h, this);
    }

    void buildGrid() {
        ZSegment s = new ZSegment(-HALF_MAP_WIDTH, 0, 0, 0, HALF_MAP_HEIGHT, GRID_COLOR);
        vsm.addGlyph(s, mainVSname);
        s = new ZSegment(HALF_MAP_WIDTH, 0, 0, 0, HALF_MAP_HEIGHT, GRID_COLOR);
        vsm.addGlyph(s, mainVSname);
        s = new ZSegment(0, -HALF_MAP_HEIGHT, 0, HALF_MAP_WIDTH, 0, GRID_COLOR);
        vsm.addGlyph(s, mainVSname);
        s = new ZSegment(0, HALF_MAP_HEIGHT, 0, HALF_MAP_WIDTH, 0, GRID_COLOR);
        vsm.addGlyph(s, mainVSname);
        tmpHGrid = new Vector();
        tmpVGrid = new Vector();
        buildHorizontalGridLevel(-HALF_MAP_HEIGHT, HALF_MAP_HEIGHT, 0);
        buildVerticalGridLevel(-HALF_MAP_WIDTH, HALF_MAP_WIDTH, 0);
        storeGrid();
        showGridLevel(1);
    }

    void buildHorizontalGridLevel(long c1, long c2, int depth) {
        long c = (c1 + c2) / 2;
        ZSegment s = new ZSegment(0, c, 0, HALF_MAP_WIDTH, 0, GRID_COLOR);
        storeSegmentInHGrid(s, depth);
        vsm.addGlyph(s, mainVSname);
        s.setVisible(false);
        if (depth < GRID_DEPTH) {
            buildHorizontalGridLevel(c1, c, depth + 1);
            buildHorizontalGridLevel(c, c2, depth + 1);
        }
    }

    void buildVerticalGridLevel(long c1, long c2, int depth) {
        long c = (c1 + c2) / 2;
        ZSegment s = new ZSegment(c, 0, 0, 0, HALF_MAP_HEIGHT, GRID_COLOR);
        storeSegmentInVGrid(s, depth);
        vsm.addGlyph(s, mainVSname);
        s.setVisible(false);
        if (depth < GRID_DEPTH + 1) {
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
        for (int i = 1; i < tmpVGrid.size(); i++) {
            v = (Vector) tmpVGrid.elementAt(i);
            levelSize = v.size();
            vGridLevels[i - 1] = new ZSegment[levelSize];
            for (int j = 0; j < v.size(); j++) {
                vGridLevels[i - 1][j] = (ZSegment) v.elementAt(j);
            }
        }
        ZSegment[] level0 = new ZSegment[vGridLevels[0].length + 1];
        System.arraycopy(vGridLevels[0], 0, level0, 1, vGridLevels[0].length);
        level0[0] = (ZSegment) ((Vector) tmpVGrid.elementAt(0)).elementAt(0);
        vGridLevels[0] = level0;
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

    void setLens(int t) {
        eh.lensType = t;
        switch(eh.lensType) {
            case WorldTaskEventHandler.ZOOMIN_LENS:
                {
                    logm.lensStatus = LogManager.ZOOMIN_LENS;
                    logm.lensPositionChanged(true);
                    break;
                }
            case WorldTaskEventHandler.ZOOMOUT_LENS:
                {
                    logm.lensStatus = LogManager.ZOOMOUT_LENS;
                    logm.lensPositionChanged(true);
                    break;
                }
            case WorldTaskEventHandler.NO_LENS:
                {
                    logm.lensStatus = LogManager.NO_LENS;
                    logm.lensxS = LogManager.NaN;
                    logm.lensyS = LogManager.NaN;
                    break;
                }
        }
    }

    void moveLens(int x, int y, boolean write, long absTime) {
        if (tLens != null) {
            tLens.setAbsolutePosition(x, y, absTime);
        } else {
            lens.setAbsolutePosition(x, y);
        }
        logm.lensPositionChanged(write);
        vsm.repaintNow();
    }

    void dzoomIn(long mx, long my) {
        float cameraAbsAlt = demoCamera.getAltitude() + demoCamera.getFocal();
        long c2x = Math.round(mx - INV_MAG_FACTOR * (mx - demoCamera.posx));
        long c2y = Math.round(my - INV_MAG_FACTOR * (my - demoCamera.posy));
        float deltAlt = (float) ((cameraAbsAlt) * (1 - MAG_FACTOR) / MAG_FACTOR);
        if (cameraAbsAlt + deltAlt > FLOOR_ALTITUDE) {
            demoCamera.altitudeOffset(deltAlt);
            demoCamera.move(c2x - demoCamera.posx, c2y - demoCamera.posy);
        } else {
            float actualDeltAlt = FLOOR_ALTITUDE - cameraAbsAlt;
            double ratio = actualDeltAlt / deltAlt;
            demoCamera.altitudeOffset(actualDeltAlt);
            demoCamera.move(Math.round((c2x - demoCamera.posx) * ratio), Math.round((c2y - demoCamera.posy) * ratio));
        }
        eh.cameraMoved();
    }

    void dzoomOut(int x, int y, long mx, long my) {
        float cameraAbsAlt = demoCamera.getAltitude() + demoCamera.getFocal();
        long c2x = Math.round(mx - MAG_FACTOR * (mx - demoCamera.posx));
        long c2y = Math.round(my - MAG_FACTOR * (my - demoCamera.posy));
        demoCamera.altitudeOffset((float) (cameraAbsAlt * (MAG_FACTOR - 1)));
        demoCamera.move(c2x - demoCamera.posx, c2y - demoCamera.posy);
        eh.cameraMoved();
    }

    void dazoomIn(long mx, long my) {
        float cameraAbsAlt = demoCamera.getAltitude() + demoCamera.getFocal();
        long c2x = Math.round(mx - INV_MAG_FACTOR * (mx - demoCamera.posx));
        long c2y = Math.round(my - INV_MAG_FACTOR * (my - demoCamera.posy));
        Vector cadata = new Vector();
        Float deltAlt = new Float((cameraAbsAlt) * (1 - MAG_FACTOR) / MAG_FACTOR);
        if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE) {
            cadata.add(deltAlt);
            cadata.add(new LongPoint(c2x - demoCamera.posx, c2y - demoCamera.posy));
            vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
        } else {
            Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
            double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
            cadata.add(actualDeltAlt);
            cadata.add(new LongPoint(Math.round((c2x - demoCamera.posx) * ratio), Math.round((c2y - demoCamera.posy) * ratio)));
            vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
        }
    }

    void dazoomOut(int x, int y, long mx, long my) {
        float cameraAbsAlt = demoCamera.getAltitude() + demoCamera.getFocal();
        long c2x = Math.round(mx - MAG_FACTOR * (mx - demoCamera.posx));
        long c2y = Math.round(my - MAG_FACTOR * (my - demoCamera.posy));
        Vector cadata = new Vector();
        cadata.add(new Float(cameraAbsAlt * (MAG_FACTOR - 1)));
        cadata.add(new LongPoint(c2x - demoCamera.posx, c2y - demoCamera.posy));
        vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new ZLWorldZOP1CameraAction(this));
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
            vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
        } else {
            Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
            double ratio2 = actualDeltAlt.floatValue() / deltAlt.floatValue();
            cadata.add(actualDeltAlt);
            cadata.add(new LongPoint(Math.round((c2x - demoCamera.posx) * ratio2), Math.round((c2y - demoCamera.posy) * ratio2)));
            vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
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
        vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
    }

    void zoomInPhase1(int x, int y) {
        if (lens == null) {
            Dimension d = demoView.getPanel().getSize();
            lens = demoView.setLens(getLensDefinition(d, x, y));
            lens.setBufferThreshold(1.5f);
        }
        vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR - 1), lens.getID(), null);
        setLens(WorldTaskEventHandler.ZOOMIN_LENS);
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
            vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR + 1), lens.getID(), new ZLWorldZIP2LensAction(this));
            vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
        } else {
            Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
            double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
            cadata.add(actualDeltAlt);
            cadata.add(new LongPoint(Math.round((c2x - demoCamera.posx) * ratio), Math.round((c2y - demoCamera.posy) * ratio)));
            vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR + 1), lens.getID(), new ZLWorldZIP2LensAction(this));
            vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new ZLWorldZIP2CameraAction(this));
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
            lens = demoView.setLens(getLensDefinition(d, x, y));
            lens.setBufferThreshold(1.5f);
        }
        vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR - 1), lens.getID(), null);
        vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN, cadata, demoCamera.getID(), new ZLWorldZOP1CameraAction(this));
        setLens(WorldTaskEventHandler.ZOOMOUT_LENS);
    }

    void zoomOutPhase2() {
        vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR + 1), lens.getID(), new ZLWorldZOP2LensAction(this));
    }

    float F_V = 1.0f;

    Lens getLensDefinition(Dimension d, int x, int y) {
        Lens res = null;
        switch(lensFamily) {
            case L1_Linear:
                {
                    res = new L1FSLinearLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L1_InverseCosine:
                {
                    res = new L1FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L1_Manhattan:
                {
                    res = new L1FSManhattanLens(1.0f, LENS_R1, x - d.width / 2, y - d.height / 2);
                    ((FSManhattanLens) res).setBoundaryColor(Color.RED);
                    tLens = null;
                    break;
                }
            case L2_Gaussian:
                {
                    res = new FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L2_Linear:
                {
                    res = new FSLinearLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L2_InverseCosine:
                {
                    res = new FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L2_Manhattan:
                {
                    res = new FSManhattanLens(1.0f, LENS_R1, x - d.width / 2, y - d.height / 2);
                    ((FSManhattanLens) res).setBoundaryColor(Color.RED);
                    tLens = null;
                    break;
                }
            case L2_Scrambling:
                {
                    res = new FSScramblingLens(1.0f, LENS_R1, 1, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case LInf_Linear:
                {
                    res = new LInfFSLinearLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case LInf_InverseCosine:
                {
                    res = new LInfFSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case LInf_Manhattan:
                {
                    res = new LInfFSManhattanLens(1.0f, LENS_R1, x - d.width / 2, y - d.height / 2);
                    ((FSManhattanLens) res).setBoundaryColor(Color.RED);
                    tLens = null;
                    break;
                }
            case LInf_Gaussian:
                {
                    res = new LInfFSGaussianLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L1_Fresnel:
                {
                    res = new L1FSFresnelLens(1.0f, LENS_R1, LENS_R2, 4, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L2_Fresnel:
                {
                    res = new FSFresnelLens(1.0f, LENS_R1, LENS_R2, 4, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case LInf_Fresnel:
                {
                    res = new LInfFSFresnelLens(1.0f, LENS_R1, LENS_R2, 4, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L2_TGaussian:
                {
                    res = new TGaussianLens(1.0f, 0.0f, 0.85f, LENS_R1, 40, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L2_HLinear:
                {
                    res = new HLinearLens(1.0f, 0.0f, 0.85f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case LInf_TLinear:
                {
                    res = new LInfTLinearLens(1.0f, 0.0f, 0.85f, LENS_R1, 40, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L3_TLinear:
                {
                    res = new L3TLinearLens(1.0f, 0.0f, 0.85f, LENS_R1, 40, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L2_Fading:
                {
                    tLens = new TFadingLens(1.0f, 0.0f, F_V, LENS_R1, x - d.width / 2, y - d.height / 2);
                    ((TFadingLens) tLens).setBoundaryColor(Color.RED);
                    ((TFadingLens) tLens).setObservedRegionColor(Color.RED);
                    res = (Lens) tLens;
                    break;
                }
            case LInf_Fading:
                {
                    tLens = new LInfTFadingLens(1.0f, 0.0f, 0.95f, LENS_R1, x - d.width / 2, y - d.height / 2);
                    ((TFadingLens) tLens).setBoundaryColor(Color.RED);
                    ((TFadingLens) tLens).setObservedRegionColor(Color.RED);
                    res = (Lens) tLens;
                    break;
                }
            case L3_Linear:
                {
                    res = new L3FSLinearLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L3_InverseCosine:
                {
                    res = new L3FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L3_Manhattan:
                {
                    res = new L3FSManhattanLens(1.0f, LENS_R1, x - d.width / 2, y - d.height / 2);
                    ((FSManhattanLens) res).setBoundaryColor(Color.RED);
                    tLens = null;
                    break;
                }
            case L3_Gaussian:
                {
                    res = new L3FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L3_Fresnel:
                {
                    res = new L3FSFresnelLens(1.0f, LENS_R1, LENS_R2, 4, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case L2_DLinear:
                {
                    tLens = new DLinearLens(1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    ((DLinearLens) tLens).setInnerRadiusColor(Color.RED);
                    ((DLinearLens) tLens).setOuterRadiusColor(Color.RED);
                    res = (Lens) tLens;
                    break;
                }
            case L2_XGaussian:
                {
                    res = new XGaussianLens(1.0f, 0.2f, 1.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
            case LP_Gaussian:
                {
                    res = new LPFSGaussianLens(1.0f, 2.0f, LENS_R1, LENS_R2, x - d.width / 2, y - d.height / 2);
                    tLens = null;
                    break;
                }
        }
        return res;
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
                if (zooming == WorldTaskEventHandler.ZOOMOUT_LENS) {
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
        vsm.animator.createPortalAnimation(150, AnimManager.PT_SZ_TRANS_LIN, data, dmPortal.getID(), null);
    }

    void killDM() {
        vsm.destroyPortal(dmPortal);
        dmPortal = null;
        mainVS.hide(dmRegion);
        paintLinks = false;
        ((DMEventHandler) eh).inDMZoomWindow = false;
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
            ((DMEventHandler) eh).inDMZoomWindow = false;
        }
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
    }

    void updateLabels(float a) {
        gds.updateLabelLevel(a);
    }

    void altitudeChanged() {
        ewmm.updateMapLevel(demoCamera.getAltitude());
        long[] wnes = demoView.getVisibleRegion(demoCamera);
        updateLabels(demoCamera.getAltitude());
        updateGridLevel(Math.max(wnes[2] - wnes[0], wnes[1] - wnes[3]));
    }

    void updateGridLevel(long visibleSize) {
        if (visibleSize < 1200) {
            showGridLevel(8);
        } else if (visibleSize < 2400) {
            showGridLevel(7);
        } else if (visibleSize < 4800) {
            showGridLevel(6);
        } else if (visibleSize < 9600) {
            showGridLevel(5);
        } else if (visibleSize < 19200) {
            showGridLevel(4);
        } else if (visibleSize < 38400) {
            showGridLevel(3);
        } else if (visibleSize < 76800) {
            showGridLevel(2);
        } else {
            showGridLevel(1);
        }
    }

    static float MAX_OVERVIEW_ALT = 30000.0f;

    void updateOverview() {
        float newAlt = (float) ((demoCamera.getAltitude() + demoCamera.getFocal()) * 24 - demoCamera.getFocal());
        overviewCamera.setAltitude((newAlt > MAX_OVERVIEW_ALT) ? MAX_OVERVIEW_ALT : newAlt);
    }

    void centerOverview() {
        if (overviewCamera.getAltitude() < MAX_OVERVIEW_ALT) {
            vsm.animator.createCameraAnimation(300, AnimManager.CA_TRANS_SIG, new LongPoint(demoCamera.posx - overviewCamera.posx, demoCamera.posy - overviewCamera.posy), overviewCamera.getID(), null);
        }
    }

    void getGlobalView() {
        Location l = vsm.getGlobalView(demoCamera, ANIM_MOVE_LENGTH);
    }

    void getHigherView() {
        Float alt = new Float(demoCamera.getAltitude() + demoCamera.getFocal());
        vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt, demoCamera.getID());
    }

    void getLowerView() {
        Float alt = new Float(-(demoCamera.getAltitude() + demoCamera.getFocal()) / 2.0f);
        vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt, demoCamera.getID());
    }

    void translateView(short direction) {
        Camera c = demoView.getCameraNumber(0);
        LongPoint trans;
        long[] rb = demoView.getVisibleRegion(c);
        if (direction == MOVE_UP) {
            long qt = Math.round((rb[1] - rb[3]) / 2.4);
            trans = new LongPoint(0, qt);
        } else if (direction == MOVE_DOWN) {
            long qt = Math.round((rb[3] - rb[1]) / 2.4);
            trans = new LongPoint(0, qt);
        } else if (direction == MOVE_RIGHT) {
            long qt = Math.round((rb[2] - rb[0]) / 2.4);
            trans = new LongPoint(qt, 0);
        } else {
            long qt = Math.round((rb[0] - rb[2]) / 2.4);
            trans = new LongPoint(qt, 0);
        }
        vsm.animator.createCameraAnimation(ANIM_MOVE_LENGTH, AnimManager.CA_TRANS_SIG, trans, c.getID());
    }

    int CENTER_CROSS_SIZE = 15;

    int CENTER_W = 0;

    int CENTER_N = 0;

    void updatePanelSize() {
        Dimension d = demoView.getPanel().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
        hpanelWidth = panelWidth / 2;
        hpanelHeight = panelHeight / 2;
        SELECTION_RECT_X = panelWidth / 2 - SELECTION_RECT_W / 2;
        SELECTION_RECT_Y = panelHeight / 2 - SELECTION_RECT_H / 2;
        CENTER_W = hpanelWidth - CENTER_CROSS_SIZE / 2;
        CENTER_N = hpanelHeight - CENTER_CROSS_SIZE / 2;
    }

    void cameraIsOnFloor(boolean b) {
        if (b != cameraOnFloor) {
            cameraOnFloor = b;
        }
    }

    public void animationEnded(Object target, short type, String dimension) {
        ((Glyph) target).setVisible(false);
    }

    public void writeOnConsole(String s) {
        console.append(s);
    }

    public void writeOnConsole(String s, Style st) {
        console.append(s, st);
    }

    void gc() {
        if (SHOW_CONSOLE) {
            console.append("Garbage collector running...\n", Console.GRAY_STYLE);
            System.gc();
            console.append("Garbage collection ended\n", Console.GRAY_STYLE);
        } else {
            System.gc();
        }
        if (SHOW_MEMORY_USAGE) {
            vsm.repaintNow();
        }
    }

    public static void main(String[] args) {
        final short tech = (args.length > 0) ? Short.parseShort(args[0]) : SS_TECHNIQUE;
        final boolean sc = (args.length > 1) ? (Short.parseShort(args[1]) == 1) : false;
        final boolean smm = (args.length > 2) ? (Short.parseShort(args[2]) == 1) : false;
        final boolean si = (args.length > 3) ? (Short.parseShort(args[3]) == 1) : true;
        final boolean td = (args.length > 4) ? (Short.parseShort(args[4]) == 1) : false;
        new ZLWorldTask(tech, sc, smm, si, td);
    }
}
