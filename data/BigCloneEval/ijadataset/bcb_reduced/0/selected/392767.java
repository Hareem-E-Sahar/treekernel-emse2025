package org.processmining.analysis.performance.componentstate.ui;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.MouseEvent;
import org.processmining.analysis.performance.dottedchart.ui.ColorReference;
import org.processmining.analysis.performance.dottedchart.ui.ShapeReference;
import org.processmining.analysis.performance.componentstate.*;
import org.processmining.analysis.performance.componentstate.model.*;
import org.processmining.analysis.performance.componentstate.ui.ComponentStatePanel;
import org.processmining.analysis.performance.componentstate.ui.ComponentStateOptionPanel;
import org.processmining.analysis.performance.componentstate.logutil.AbstractLogUnit;
import org.processmining.analysis.performance.componentstate.logutil.LogUnitList;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

/**
 * ComponentStatePanel.
 * 
 * The purpose of this class is to provide a view of component state
 * 
 * @author Gabriela Alina Chipaila
 */
public class ComponentStatePanel extends JPanel implements MouseListener, MouseMotionListener {

    public static String STR_NONE = "None";

    public static String ITEM_HANDLE_DOT = "Dot";

    public static String ITEM_HANDLE_CIRCLE = "Circle";

    public static String ITEM_HANDLE_TRIANGLE = "Triangle";

    public static String ITEM_HANDLE_BOX = "Box";

    public static String ITEM_HANDLE_RHOMBUS = "Rhombus";

    public static String ITEM_HANDLE_ROUND_BOX = "RoundBox";

    public static String ITEM_HANDLE_DRAW_BOX = "DrawBox";

    public static String ITEM_HANDLE_DRAW_CIRCLE = "Circle";

    public static String ITEM_HANDLE_DRAW_TRIANGLE = "Triangle";

    public static String ITEM_HANDLE_DRAW_RHOMBUS = "DrawRhombusBox";

    public static String ITEM_HANDLE_DRAW_ROUND_BOX = "DrawRoundBox";

    public static final String TIME_ACTUAL = "Actual";

    public static final String TIME_RELATIVE_TIME = "Relative(Time)";

    public static final String TIME_RELATIVE_RATIO = "Relative(Ratio)";

    public static final String TIME_LOGICAL = "Logical";

    public static final String TIME_LOGICAL_RELATIVE = "Logical(Relative)";

    public static final String ST_ORIG = "Originator";

    public static final String ST_TASK = "Task ID";

    public static final String ST_INST = "Instance ID";

    public static final String ST_EVEN = "Event";

    public static final String ST_DATA = "Data";

    public static final String ST_ZOOMIN = "Zoom in";

    public static final String ST_DRAG = "Drag";

    public static final String STATE_RUNNING = "Running";

    public static final String STATE_ASSIGNED = "Assigned";

    public static final String STATE_SUSPENDED = "Suspended";

    public static final String STATE_COMPLETED = "Completed";

    public static final String STATE_MANUALSKIP = "ManualSkipped";

    public static final String STATE_ABORTED = "Aborted";

    public static final String STATE_INITIAL = "Initial";

    public static final String STATE_READY = "Ready";

    public static final String STATE_WORKING = "Working";

    public static final String STATE_UNASSIGNED = "Unassigned";

    public static final String STATE_UNDEFINED = "Undefined";

    public static final String STATE_DEFINED = "Defined";

    public static final String STATE_UNCONFIRMED = "Unconfirmed";

    public static final String STATE_SHAPE_LANE = "Lane";

    public static final String STATE_SHAPE_SEPARATOR = "Separator";

    private HashMap componentStateMap = new HashMap();

    private BufferedImage matrixBuffer;

    public static String HANDLE_ICONS[] = { STR_NONE, ITEM_HANDLE_CIRCLE, ITEM_HANDLE_TRIANGLE, ITEM_HANDLE_BOX, ITEM_HANDLE_RHOMBUS };

    ComponentStateAnalysis csa = null;

    ComponentStateOptionPanel csOptionPanel = null;

    protected ComponentStateModel csModel = null;

    protected double milli2pixels = 1.0;

    protected double height2pixels = 1.0;

    protected long logLaneStep = 0;

    protected int logLanePixStep = 0;

    protected double viewportZoomX = 1.0;

    protected double viewportZoomY = 1.0;

    protected int updWidth = 0;

    protected int updHight = 0;

    protected String itemHandle = null;

    protected Color colorBg = null;

    protected Color colorFg = null;

    protected Color colorLogDark = null;

    protected Color colorLogBright = null;

    protected Color colorTimeLine = null;

    protected Color colorBaseGreen = null;

    protected ShapeReference itemShapes = null;

    protected String timeOption = TIME_ACTUAL;

    protected HashMap timeBaseHashMap = null;

    protected HashMap endTimeBaseHashMap = null;

    protected String relativeTimeBase = ST_INST;

    protected ColorReference itemColors = null;

    private String stRalativeTimeOptions = null;

    private String stRalativeTime = null;

    private boolean bAdjust = true;

    private boolean bTooltip = false;

    private long[] widthDividers = { 1, 10, 100, 500, 1000, 60000, 3600000L, 86400000L, 604800000L, 2592000000L, 31536000000L };

    private long timeSort = 1000;

    long clipLeftTs;

    long clipRightTs;

    int clipL = 0;

    int clipR = 0;

    protected int border = 5;

    protected double handleRatio = 0.5;

    protected Calendar cal = null;

    private Point p1;

    private Point p2;

    private boolean clicked = false;

    private Point p3;

    private Point p4;

    /**
	 * constructor
	 * 
	 * @param aCSA
	 *            ComponentStateAnalysis to be displayed
	 * @param aDcModel
	 *            ComponentStateModel the componentstatemodel that includes the
	 *            data structure
	 */
    public ComponentStatePanel(ComponentStateAnalysis aCSA, ComponentStateModel aCSModel) {
        csa = aCSA;
        csModel = aCSModel;
        csOptionPanel = new ComponentStateOptionPanel(this, aCSA);
        calculateCurrentTime();
        csModel.adjustLogBoundaries(timeOption);
        viewportZoomX = 1.0;
        viewportZoomY = 1.0;
        border = 10;
        handleRatio = 0.5;
        updWidth = this.getWidth();
        updHight = this.getHeight();
        itemHandle = ComponentStatePanel.ITEM_HANDLE_CIRCLE;
        colorBg = new Color(240, 240, 220);
        colorFg = new Color(10, 10, 10);
        colorLogDark = new Color(170, 170, 160);
        colorLogBright = new Color(210, 210, 200);
        colorTimeLine = new Color(225, 225, 225);
        colorBaseGreen = new Color(50, 100, 100);
        itemColors = new ColorReference();
        cal = new GregorianCalendar();
        logLaneStep = 1000;
        logLanePixStep = 200;
        itemShapes = new ShapeReference();
        updateMilli2pixelsRatio();
        this.setAutoscrolls(true);
        this.setOpaque(true);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
    }

    public ComponentStateModel getComponentStateModel() {
        return csModel;
    }

    public ComponentStateOptionPanel getComponentStateOptionPanel() {
        return csOptionPanel;
    }

    public void changeComponentType() {
        csModel.setTypeHashMap(csOptionPanel.getComponentType());
    }

    public void setTimeOption(String str) {
        timeOption = str;
    }

    public String getTimeOption() {
        return timeOption;
    }

    public void setTimeBaseHaspMap(HashMap aHashMap) {
        timeBaseHashMap = null;
        timeBaseHashMap = aHashMap;
    }

    public HashMap getTimeBaseHaspMap() {
        return timeBaseHashMap;
    }

    public void setEndTimeBaseHaspMap(HashMap aHashMap) {
        endTimeBaseHashMap = null;
        endTimeBaseHashMap = aHashMap;
    }

    public HashMap getEndTimeBaseHaspMap() {
        return endTimeBaseHashMap;
    }

    public void setRelativeTimeBase(String str) {
        relativeTimeBase = str;
    }

    public String getRelativeTimeBase() {
        return relativeTimeBase;
    }

    public void changeEventTypeToKeep(ArrayList<String> aEventTypeToKeep) {
        csModel.setEventTypeToKeep(aEventTypeToKeep);
        this.calculateCurrentTime();
        csModel.adjustLogBoundaries(timeOption);
        this.updateMilli2pixelsRatio();
    }

    public void changeInstanceTypeToKeep(ArrayList anInstanceIDs) {
        csModel.setInstanceTypeToKeep(anInstanceIDs);
        this.calculateCurrentTime();
        csModel.adjustLogBoundaries(timeOption);
        this.updateMilli2pixelsRatio();
    }

    public int getHashMapSize() {
        if (!csModel.getTypeHashMap().equals(ST_INST)) return csModel.getItemMap().size(); else return csModel.getInstanceTypeToKeep().size();
    }

    public ColorReference getColorReference() {
        return itemColors;
    }

    public void changeSelectedEventsForStates(String type, HashMap selectedEventsMap) {
        csModel.setSelectedEventsForStates(type, selectedEventsMap);
        this.calculateCurrentTime();
        csModel.adjustLogBoundaries(timeOption);
        this.updateMilli2pixelsRatio();
    }

    protected String assignShapeByItem(AbstractLogUnit logUnit) {
        String shapeStandard = csOptionPanel.getShapeStandard();
        String str = ITEM_HANDLE_CIRCLE;
        if (shapeStandard.equals(ComponentStateAnalysis.ST_ORIG)) str = itemShapes.getShape(logUnit.getOriginator()); else if (shapeStandard.equals(ComponentStateAnalysis.ST_TASK)) str = itemShapes.getShape(logUnit.getElement()); else if (shapeStandard.equals(ComponentStateAnalysis.ST_EVEN)) str = itemShapes.getShape(logUnit.getType()); else if (shapeStandard.equals(ComponentStateAnalysis.ST_INST)) str = itemShapes.getShape(logUnit.getProcessInstance().getName());
        return str;
    }

    protected void assignColorByItem(AbstractLogUnit logUnit, Graphics g) {
        String colorStandard = csOptionPanel.getColorStandard();
        if (colorStandard.equals(ComponentStateAnalysis.STR_NONE)) g.setColor(colorBaseGreen); else if (colorStandard.equals(ComponentStateAnalysis.ST_ORIG)) g.setColor(itemColors.getColor(logUnit.getOriginator())); else if (colorStandard.equals(ComponentStateAnalysis.ST_TASK)) g.setColor(itemColors.getColor(logUnit.getElement())); else if (colorStandard.equals(ComponentStateAnalysis.ST_EVEN)) g.setColor(itemColors.getColor(logUnit.getType())); else if (colorStandard.equals(ComponentStateAnalysis.ST_INST)) g.setColor(itemColors.getColor(logUnit.getProcessInstance().getName()));
    }

    protected String getStateShapeType(String aState) {
        if ((aState.equals(ComponentStatePanel.STATE_ASSIGNED)) || (aState.equals(ComponentStatePanel.STATE_RUNNING)) || (aState.equals(ComponentStatePanel.STATE_SUSPENDED)) || (aState.equals(ComponentStatePanel.STATE_WORKING)) || (aState.equals(ComponentStatePanel.STATE_DEFINED)) || (aState.equals(ComponentStatePanel.STATE_UNCONFIRMED))) {
            return ComponentStatePanel.STATE_SHAPE_LANE;
        } else if ((aState.equals(ComponentStatePanel.STATE_ABORTED)) || (aState.equals(ComponentStatePanel.STATE_COMPLETED)) || (aState.equals(ComponentStatePanel.STATE_MANUALSKIP)) || (aState.equals(ComponentStatePanel.STATE_UNASSIGNED)) || (aState.equals(ComponentStatePanel.STATE_UNDEFINED)) || (aState.equals(ComponentStatePanel.STATE_INITIAL)) || (aState.equals(ComponentStatePanel.STATE_READY))) {
            return ComponentStatePanel.STATE_SHAPE_SEPARATOR;
        } else return ("state shape unknown - " + aState);
    }

    /**
	 * convenience method for the Separator state, looks inside the DrawUnitList
	 * and removes the starting event of the corresponding Lane state so that
	 * the lane state will be finished drawing.
	 * 
	 * @param aDrawList
	 * @param event
	 */
    protected void updateDrawUnitList(LogUnitList aDrawList, AbstractLogUnit event) {
        AbstractLogUnit event2 = null;
        if (aDrawList.size() >= 1) {
            for (Iterator it = ((LogUnitList) aDrawList).iterator(); it.hasNext(); ) {
                event2 = (AbstractLogUnit) it.next();
                if (event2.getElement().equals(event.getElement()) && event2.getProcessInstance().getName().equals(event.getProcessInstance().getName())) {
                    aDrawList.removeEvent(event2);
                    break;
                }
            }
        }
    }

    protected void cleanDrawUnitList(LogUnitList aDrawList, AbstractLogUnit event) {
        AbstractLogUnit event2 = null;
        Date endTimePI = null;
        if (aDrawList.size() >= 1) {
            for (Iterator it = ((LogUnitList) aDrawList).iterator(); it.hasNext(); ) {
                event2 = (AbstractLogUnit) it.next();
                if (timeOption.equals(TIME_RELATIVE_TIME)) {
                    endTimePI = new Date(csModel.getEndDateMap(ST_INST).get(event2.getProcessInstance().getName()).getTime() - csModel.getStartDateMap(ST_INST).get(event2.getProcessInstance().getName()).getTime());
                } else if (timeOption.equals(TIME_RELATIVE_RATIO)) {
                    endTimePI = new Date(10000);
                } else {
                    endTimePI = csModel.getEndDateMap(ST_INST).get(event2.getProcessInstance().getName());
                }
                if (event.getCurrentTimeStamp().after(endTimePI)) {
                    aDrawList.removeEvent(event2);
                    it = ((LogUnitList) aDrawList).iterator();
                }
            }
        }
    }

    protected Date getEndEventTimestamp(AbstractLogUnit startEvent, String key, ArrayList helpList) {
        AbstractLogUnit endEvent = null;
        String eventType = null;
        String state = null;
        String stateShape = null;
        int indexEndItem = helpList.indexOf(startEvent) + 1;
        while (indexEndItem < helpList.size()) {
            endEvent = (AbstractLogUnit) helpList.get(indexEndItem);
            if (endEvent == null || !csModel.getInstanceTypeToKeep().contains(endEvent.getProcessInstance().getName()) || csModel.getEventTypeToKeep() == null) {
                indexEndItem++;
            } else if (!csModel.getTypeHashMap().equals(ST_ORIG)) {
                if (!csModel.getEventTypeToKeep().contains(endEvent.getType())) {
                    indexEndItem++;
                } else if (endEvent.getProcessInstance().getName().equals(startEvent.getProcessInstance().getName()) && endEvent.getElement().equals(startEvent.getElement())) {
                    return endEvent.getCurrentTimeStamp();
                } else {
                    indexEndItem++;
                }
            } else if (csModel.getTypeHashMap().equals(ST_ORIG)) {
                ArrayList usersList = new ArrayList();
                if (csModel.getAssignedUsersMap().containsKey(endEvent)) {
                    usersList = (ArrayList) csModel.getAssignedUsersMap().get(endEvent);
                }
                if (endEvent.getProcessInstance().getName().equals(startEvent.getProcessInstance().getName()) && endEvent.getElement().equals(startEvent.getElement())) {
                    if (csModel.getEventTypeToKeep().contains(endEvent.getType())) {
                        state = getStateByEvent(endEvent, ST_ORIG);
                        if ((state.equals(ComponentStatePanel.STATE_WORKING) && endEvent.getOriginator().equals(key)) || (state.equals(ComponentStatePanel.STATE_ASSIGNED) && usersList != null && usersList.contains(key))) {
                            return endEvent.getCurrentTimeStamp();
                        } else if ((csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK) != null) && (csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK).containsKey(endEvent.getType()))) {
                            return endEvent.getCurrentTimeStamp();
                        } else indexEndItem++;
                    } else if ((csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK) != null) && (csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK).containsKey(endEvent.getType()))) {
                        return endEvent.getCurrentTimeStamp();
                    } else indexEndItem++;
                } else indexEndItem++;
            } else indexEndItem++;
        }
        if (timeOption.equals(TIME_RELATIVE_TIME)) return new Date(csModel.getEndDateMap(ST_INST).get(startEvent.getProcessInstance().getName()).getTime() - csModel.getStartDateMap(ST_INST).get(startEvent.getProcessInstance().getName()).getTime());
        if (timeOption.equals(TIME_RELATIVE_RATIO)) return new Date(10000);
        return csModel.getEndDateMap(ST_INST).get(startEvent.getProcessInstance().getName());
    }

    protected Date getNextEventTimestamp(AbstractLogUnit startEvent, String key, ArrayList helpList) {
        AbstractLogUnit nextEvent = null;
        String eventType = null;
        String state = null;
        String stateShape = null;
        int indexEndItem = helpList.indexOf(startEvent) + 1;
        while (indexEndItem < helpList.size()) {
            nextEvent = (AbstractLogUnit) helpList.get(indexEndItem);
            if (nextEvent == null || !csModel.getInstanceTypeToKeep().contains(nextEvent.getProcessInstance().getName()) || csModel.getEventTypeToKeep() == null) {
                indexEndItem++;
            } else if (!csModel.getTypeHashMap().equals(ST_ORIG)) {
                if (!csModel.getEventTypeToKeep().contains(nextEvent.getType())) {
                    indexEndItem++;
                } else {
                    return nextEvent.getCurrentTimeStamp();
                }
            } else if (csModel.getTypeHashMap().equals(ST_ORIG)) {
                if (csModel.getEventTypeToKeep().contains(nextEvent.getType())) {
                    ArrayList usersList = new ArrayList();
                    if (csModel.getAssignedUsersMap().containsKey(nextEvent)) {
                        usersList = (ArrayList) csModel.getAssignedUsersMap().get(nextEvent);
                    }
                    state = getStateByEvent(nextEvent, ST_ORIG);
                    if ((state.equals(ComponentStatePanel.STATE_WORKING) && nextEvent.getOriginator().equals(key)) || (state.equals(ComponentStatePanel.STATE_ASSIGNED) && usersList != null && usersList.contains(key))) {
                        return nextEvent.getCurrentTimeStamp();
                    } else if ((csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK) != null) && (csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK).containsKey(nextEvent.getType()))) {
                        return nextEvent.getCurrentTimeStamp();
                    } else indexEndItem++;
                } else if ((csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK) != null) && (csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK).containsKey(nextEvent.getType()))) {
                    return nextEvent.getCurrentTimeStamp();
                } else indexEndItem++;
            } else indexEndItem++;
        }
        if (timeOption.equals(TIME_RELATIVE_TIME)) return new Date(csModel.getEndDateMap(ST_INST).get(startEvent.getProcessInstance().getName()).getTime() - csModel.getStartDateMap(ST_INST).get(startEvent.getProcessInstance().getName()).getTime());
        if (timeOption.equals(TIME_RELATIVE_RATIO)) return new Date(10000);
        return csModel.getEndDateMap(ST_INST).get(startEvent.getProcessInstance().getName());
    }

    protected AbstractLogUnit getNextEvent(AbstractLogUnit startEvent, String key, ArrayList helpList) {
        AbstractLogUnit nextEvent = null;
        String eventType = null;
        String state = null;
        String stateShape = null;
        int indexEndItem = helpList.indexOf(startEvent) + 1;
        while (indexEndItem < helpList.size()) {
            nextEvent = (AbstractLogUnit) helpList.get(indexEndItem);
            if (nextEvent == null || !csModel.getInstanceTypeToKeep().contains(nextEvent.getProcessInstance().getName()) || csModel.getEventTypeToKeep() == null) {
                indexEndItem++;
            } else if (!csModel.getTypeHashMap().equals(ST_ORIG)) {
                if (!csModel.getEventTypeToKeep().contains(nextEvent.getType())) {
                    indexEndItem++;
                } else {
                    return nextEvent;
                }
            } else if (csModel.getTypeHashMap().equals(ST_ORIG)) {
                if (csModel.getEventTypeToKeep().contains(nextEvent.getType())) {
                    ArrayList usersList = new ArrayList();
                    if (csModel.getAssignedUsersMap().containsKey(nextEvent)) {
                        usersList = (ArrayList) csModel.getAssignedUsersMap().get(nextEvent);
                    }
                    state = getStateByEvent(nextEvent, ST_ORIG);
                    if ((state.equals(ComponentStatePanel.STATE_WORKING) && nextEvent.getOriginator().equals(key)) || (state.equals(ComponentStatePanel.STATE_ASSIGNED) && usersList != null && usersList.contains(key))) {
                        return nextEvent;
                    } else if ((csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK) != null) && (csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK).containsKey(nextEvent.getType()))) {
                        return nextEvent;
                    } else indexEndItem++;
                } else if ((csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK) != null) && (csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK).containsKey(nextEvent.getType()))) {
                    return nextEvent;
                } else indexEndItem++;
            } else indexEndItem++;
        }
        return null;
    }

    public void changeTimeOption() {
        String base = csOptionPanel.getRelativeTimeOption();
        timeOption = csOptionPanel.getTimeOption();
        if (timeOption.equals(TIME_RELATIVE_TIME) || timeOption.equals(TIME_RELATIVE_RATIO)) {
            setTimeBaseHaspMap(csModel.getStartDateMap(base));
            setEndTimeBaseHaspMap(csModel.getEndDateMap(base));
        }
        calculateCurrentTime();
        csModel.adjustLogBoundaries(timeOption);
        updateMilli2pixelsRatio();
        clipLeftTs = coord2timeMillis(clipL);
        clipRightTs = coord2timeMillis(clipR);
        bAdjust = true;
    }

    public void changeWidthSort() {
        bAdjust = true;
    }

    public long adjustWidthSort(long sort) {
        return (clipRightTs - clipLeftTs) / sort;
    }

    public void setTimeSort(long sort) {
        timeSort = sort;
    }

    protected void updateMilli2pixelsRatio() {
        milli2pixels = (double) (this.getWidth() - border * 2) / (double) (csModel.getLogBoundaryRight().getTime() - csModel.getLogBoundaryLeft().getTime());
        if ((milli2pixels == 1) || (milli2pixels == 0)) {
            return;
        }
        logLaneStep = 1000;
        while ((logLaneStep * milli2pixels) < 150) {
            logLaneStep *= 2;
        }
        logLanePixStep = (int) (logLaneStep * milli2pixels) + 1;
    }

    /**
	 * overwritten as proxy, to update mill2pixels ratio
	 */
    public void setSize(int width, int height) {
        super.setSize(width, height);
        updateMilli2pixelsRatio();
        revalidate();
    }

    /**
	 * overwritten as proxy, to update mill2pixels ratio
	 */
    public void setSize(Dimension d) {
        super.setSize(d);
        updateMilli2pixelsRatio();
        revalidate();
    }

    /**
	 * adjusts the viewable area of the log (zoom)
	 * 
	 * @param aZoom
	 *            fraction of the log to be viewable (within (0,1] !)
	 */
    public void setViewportZoomX(double aZoom) {
        if (aZoom > 1.0) {
            viewportZoomX = 1.0;
        } else if (aZoom <= 0.0) {
            viewportZoomX = 0.00000000001;
        } else {
            viewportZoomX = aZoom;
        }
        double ratio = updWidth;
        updWidth = (int) ((double) getParent().getWidth() * (1.0 / aZoom));
        ratio = updWidth / ratio;
        Dimension dim = new Dimension(updWidth, updHight);
        Point d = csa.getScrollPane().getViewport().getViewPosition();
        Point p = new Point((int) ((double) d.getX() * ratio), (int) ((double) d.getY()));
        this.setPreferredSize(dim);
        updateMilli2pixelsRatio();
        this.revalidate();
        csa.setScrollBarPosition(p);
    }

    /**
	 * adjusts the viewable area of the log (zoom)
	 * 
	 * @param aZoom
	 *            fraction of the log to be viewable (within (0,1] !)
	 */
    public void setViewportZoomY(double aZoom) {
        if (aZoom > 1.0) {
            viewportZoomY = 1.0;
        } else if (aZoom <= 0.0) {
            viewportZoomY = 0.00000000001;
        } else {
            viewportZoomY = aZoom;
        }
        double ratio = updHight;
        updHight = (int) ((double) getParent().getHeight() * (1.0 / aZoom));
        ratio = updHight / ratio;
        Dimension dim = new Dimension(updWidth, updHight);
        Point d = csa.getScrollPane().getViewport().getViewPosition();
        Point p = new Point((int) d.getX(), (int) ((double) d.getY() * (ratio)));
        this.setPreferredSize(dim);
        updateMilli2pixelsRatio();
        this.revalidate();
        csa.setScrollBarPosition(p);
    }

    /**
	 * adjusts the viewable area of the log (zoom)
	 */
    public void setViewportZoomIn() {
        Dimension d = csa.getViewportSize();
        int width = Math.abs(p1.x - p2.x);
        int height = Math.abs(p1.y - p2.y);
        int value = (int) (Math.log10((double) this.getWidth() * (d.getWidth() / width) / (double) csa.getViewportSize().getWidth()) * 1000.0);
        if (value > 3000) return;
        value = (int) (Math.log10((double) this.getHeight() * (d.getHeight() / height) / (double) csa.getViewportSize().getHeight()) * 1000.0);
        if (value > 3000) return;
        updWidth = (int) ((double) this.getWidth() * (d.getWidth() / width));
        updHight = (int) ((double) this.getHeight() * (d.getHeight() / height));
        Dimension dim = new Dimension(updWidth, updHight);
        int pos_x = Math.min(p1.x, p2.x);
        int pos_y = Math.min(p1.y, p2.y);
        Point p = new Point((int) (pos_x * d.getWidth() / width), (int) (pos_y * d.getHeight() / height));
        this.setPreferredSize(dim);
        updateMilli2pixelsRatio();
        this.revalidate();
        csa.setScrollBarPosition(p);
        p1 = null;
        p2 = null;
        adjustSlideBar();
    }

    /**
	 * adjusts the viewable area of the log (zoom)
	 */
    public Point zoomInViewPort() {
        if (p1 == null || p2 == null) return null;
        Dimension d = csa.getViewportSize();
        int width = Math.abs(p1.x - p2.x);
        int height = Math.abs(p1.y - p2.y);
        int value = (int) (Math.log10((double) this.getWidth() * (d.getWidth() / width) / (double) csa.getViewportSize().getWidth()) * 1000.0);
        if (value > 3000) return null;
        value = (int) (Math.log10((double) this.getHeight() * (d.getHeight() / height) / (double) csa.getViewportSize().getHeight()) * 1000.0);
        if (value > 3000) return null;
        updWidth = (int) ((double) this.getWidth() * (d.getWidth() / width));
        updHight = (int) ((double) this.getHeight() * (d.getHeight() / height));
        Dimension dim = new Dimension(updWidth, updHight);
        int pos_x = Math.min(p1.x, p2.x);
        int pos_y = Math.min(p1.y, p2.y);
        this.setPreferredSize(dim);
        updateMilli2pixelsRatio();
        this.revalidate();
        p1 = null;
        p2 = null;
        adjustSlideBar();
        return new Point((int) (pos_x * d.getWidth() / width), (int) (pos_y * d.getHeight() / height));
    }

    public void adjustSlideBar() {
        int value = (int) (Math.log10((double) updWidth / (double) csa.getViewportSize().getWidth()) * 1000.0);
        if (value > 3000) value = 3000;
        csOptionPanel.getZoomSliderX().setValue(value);
        csOptionPanel.getZoomSliderX().repaint();
        value = (int) (Math.log10((double) updHight / (double) csa.getViewportSize().getHeight()) * 1000.0);
        if (value > 3000) value = 3000;
        csOptionPanel.getZoomSliderY().setValue(value);
        csOptionPanel.getZoomSliderY().repaint();
    }

    /**
	 * Convenience method. Transforms a timestamp (Date) into the corresponding
	 * horizontal position within the viewport.
	 * 
	 * @param aTimestamp
	 * @return
	 */
    protected int time2coord(Date aTimestamp) {
        return (int) ((aTimestamp.getTime() - csModel.getLogBoundaryLeft().getTime()) * milli2pixels);
    }

    /**
	 * Convenience method. Transforms a timestamp (Date) into the corresponding
	 * horizontal position within the viewport.
	 * 
	 * @param aTimestamp
	 * @return
	 */
    protected int time2coord_buffer(Date aTimestamp, double milli2pixels) {
        return (int) ((aTimestamp.getTime() - csModel.getLogBoundaryLeft().getTime()) * milli2pixels);
    }

    /**
	 * Convenience method. Transforms a timestamp (Date) into the corresponding
	 * horizontal position within the viewport.
	 * 
	 * @param aTimestamp
	 * @return
	 */
    protected int unit2Cord(int unit_number) {
        return (int) (border + ((double) (this.getHeight() - 2 * border) / getHashMapSize() * unit_number));
    }

    /**
	 * Convenience method. Transforms a timestamp (Date) into the corresponding
	 * horizontal position within the viewport.
	 * 
	 * @param aTimestamp
	 * @return
	 */
    protected int unit2Cord_buffer(int unit_number, int height) {
        return (int) (((double) (height) / getHashMapSize() * unit_number));
    }

    /**
	 * Convenience method. Transforms a timestamp (milliseconds) into the
	 * corresponding horizontal position within the viewport.
	 * 
	 * @param aTimeMillis
	 * @return
	 */
    protected int time2coord(long aTimeMillis) {
        return (int) ((aTimeMillis - csModel.getLogBoundaryLeft().getTime()) * milli2pixels);
    }

    /**
	 * convenience method. transforms a given horizontal coordinate within the
	 * viewport into the correspondig timestamp (Date) in the log space.
	 * 
	 * @param anX
	 * @return
	 */
    protected Date coord2time(double anX) {
        return new Date(coord2timeMillis(anX));
    }

    /**
	 * convenience method. transforms a given horizontal coordinate within the
	 * viewport into the correspondig timestamp (milliseconds) in the log space.
	 * 
	 * @param anX
	 * @return
	 */
    protected long coord2timeMillis(double anX) {
        return (long) ((double) anX / milli2pixels) + csModel.getLogBoundaryLeft().getTime();
    }

    /**
	 * convenience method. transforms a given horizontal coordinate within the
	 * viewport into the correspondig timestamp (milliseconds) in the log space.
	 * 
	 * @param anX
	 * @return
	 */
    protected long coord2timeMillis_buffer(double anX, double milli2pixels) {
        return (long) ((double) anX / milli2pixels) + csModel.getLogBoundaryLeft().getTime();
    }

    /**
	 * convenience method. adjust width on the screen
	 * 
	 * @param void
	 * @return
	 */
    public void adjustWidth() {
        long widthSort = csOptionPanel.getWidthDivider();
        long number = (clipRightTs - clipLeftTs) / widthSort;
        int idx = 0;
        if (number == 0) {
            for (idx = 0; idx < widthDividers.length; idx++) if (widthDividers[idx] == widthSort) break;
            if (idx == 0) return;
            idx--;
            while (idx >= 0) {
                widthSort = widthDividers[idx];
                number = (clipRightTs - clipLeftTs) / widthSort;
                if (number > 0) {
                    break;
                }
                idx--;
            }
            if (idx < 0) csOptionPanel.changeWidthSort(0); else csOptionPanel.changeWidthSort(idx);
        } else if (number > 100) {
            for (idx = 0; idx < widthDividers.length; idx++) if (widthDividers[idx] == widthSort) break;
            idx++;
            while (idx < widthDividers.length) {
                widthSort = widthDividers[idx];
                number = (clipRightTs - clipLeftTs) / widthSort;
                if (number <= 100) {
                    break;
                }
                idx++;
            }
            if (idx >= widthDividers.length) csOptionPanel.changeWidthSort(widthDividers.length - 1); else csOptionPanel.changeWidthSort(idx);
        }
    }

    /**
	 * Checks two time frames for intersection. Interpret as follows: one
	 * timeframe is delimited by d1Left on the right and d1Right on the right,
	 * this is the reference frame. the second timeframe is delimited by
	 * d2Left/Right. This method checks, if any part of the second timeframe is
	 * contained within the first timeframe.
	 * 
	 * @param d1Left
	 * @param d1Right
	 * @param d2Left
	 * @param d2Right
	 * @return
	 */
    protected static boolean intersects(Date d1Left, Date d1Right, Date d2Left, Date d2Right) {
        if (d2Left.after(d1Left)) {
            if (d2Left.before(d1Right)) {
                return true;
            } else {
                return false;
            }
        } else if (d2Right.after(d1Left)) {
            return true;
        } else {
            return false;
        }
    }

    /**
	 * calcuate current time
	 */
    protected void calculateCurrentTime() {
        if (timeOption.equals(TIME_LOGICAL)) {
            csModel.calculateCurrentTimeLogical();
            return;
        } else if (timeOption.equals(TIME_LOGICAL_RELATIVE)) {
            csModel.calculateCurrentTimeLogical_Relative();
            return;
        } else if (timeOption.equals(TIME_RELATIVE_TIME) || timeOption.equals(TIME_RELATIVE_RATIO)) {
            stRalativeTime = timeOption;
            stRalativeTimeOptions = relativeTimeBase;
        }
        if (csModel.getItemMap().size() <= 0) return;
        String key = null;
        AbstractLogUnit item = null;
        int index = -1;
        for (Iterator itSets = csModel.getItemMap().keySet().iterator(); itSets.hasNext(); ) {
            index++;
            key = (String) itSets.next();
            if (csModel.getTypeHashMap().equals(ST_INST) && !csModel.getInstanceTypeToKeep().contains(key)) continue;
            LogUnitList tempLogUnit = (LogUnitList) csModel.getItemMap().get(key);
            for (Iterator itItm = tempLogUnit.iterator(); itItm.hasNext(); ) {
                item = (AbstractLogUnit) itItm.next();
                if (csModel.getEventTypeToKeep() != null && (!csModel.getInstanceTypeToKeep().contains(item.getProcessInstance().getName()))) continue;
                if (timeOption.equals(TIME_ACTUAL)) {
                    item.setCurrentTimeStamp();
                } else if (timeOption.equals(TIME_RELATIVE_TIME)) {
                    if (relativeTimeBase.equals(ST_INST)) item.setCurrentTimeStampRelative_Instance(); else if (relativeTimeBase.equals(ST_TASK)) item.setCurrentTimeStampRelative_Others((Date) timeBaseHashMap.get(item.getElement())); else if (relativeTimeBase.equals(ST_ORIG)) {
                        if (timeBaseHashMap.containsKey(item.getOriginator())) item.setCurrentTimeStampRelative_Others((Date) timeBaseHashMap.get(item.getOriginator()));
                    } else if (relativeTimeBase.equals(ST_EVEN)) item.setCurrentTimeStampRelative_Others((Date) timeBaseHashMap.get(item.getType()));
                } else if (timeOption.equals(TIME_RELATIVE_RATIO)) {
                    if (relativeTimeBase.equals(ST_INST)) item.setCurrentTimeStampRelativeRatio_Instance(); else if (relativeTimeBase.equals(ST_TASK)) item.setCurrentTimeStampRelativeRatio_Others((Date) timeBaseHashMap.get(item.getElement()), (Date) endTimeBaseHashMap.get(item.getElement())); else if (relativeTimeBase.equals(ST_ORIG)) {
                        if (timeBaseHashMap.containsKey(item.getOriginator())) item.setCurrentTimeStampRelativeRatio_Others((Date) timeBaseHashMap.get(item.getOriginator()), (Date) endTimeBaseHashMap.get(item.getOriginator()));
                    } else if (relativeTimeBase.equals(ST_EVEN)) item.setCurrentTimeStampRelativeRatio_Others((Date) timeBaseHashMap.get(item.getType()), (Date) endTimeBaseHashMap.get(item.getType()));
                }
            }
        }
    }

    /**
	 * convenience method for internal use. paints a log item handle
	 * visualization.
	 * 
	 * @param x
	 *            horizontal anchor coordinate of the handle
	 * @param y
	 *            vertical anchor coordinate of the handle
	 * @param g
	 *            the Graphics object used for painting
	 */
    protected void paintItem(int x, int y, Graphics g, String shape) {
        if (shape.equals(STR_NONE)) {
            return;
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_DOT)) {
            g.fillOval(x - 2, y - 2, 4, 4);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_BOX)) {
            g.fill3DRect(x - 5, y - 5, 10, 10, false);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_CIRCLE)) {
            g.fillOval(x - 5, y - 5, 11, 11);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_RHOMBUS)) {
            int rhombX[] = { x, x - 5, x, x + 5 };
            int rhombY[] = { y - 5, y, y + 5, y };
            g.fillPolygon(rhombX, rhombY, 4);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_TRIANGLE)) {
            int triX[] = { x, x - 5, x + 5 };
            int triY[] = { y + 5, y - 5, y - 5 };
            g.fillPolygon(triX, triY, 3);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_ROUND_BOX)) {
            g.fillRoundRect(x - 5, y - 5, 10, 10, 2, 2);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_DRAW_BOX)) {
            g.drawRect(x - 5, y - 5, 10, 10);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_DRAW_CIRCLE)) {
            g.drawOval(x - 5, y - 5, 11, 11);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_DRAW_RHOMBUS)) {
            int rhombX[] = { x, x - 5, x, x + 5 };
            int rhombY[] = { y - 5, y, y + 5, y };
            g.drawPolygon(rhombX, rhombY, 4);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_DRAW_TRIANGLE)) {
            int triX[] = { x, x - 5, x + 5 };
            int triY[] = { y + 5, y - 5, y - 5 };
            g.drawPolygon(triX, triY, 3);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_DRAW_ROUND_BOX)) {
            g.drawRoundRect(x - 5, y - 5, 10, 10, 2, 2);
        }
    }

    /**
	 * convenience method for internal use. paints a log item handle
	 * visualization.
	 * 
	 * @param x
	 *            horizontal anchor coordinate of the handle
	 * @param y
	 *            vertical anchor coordinate of the handle
	 * @param g
	 *            the Graphics object used for painting
	 */
    protected void paintItem_buffer(int x, int y, Graphics g, String shape) {
        if (shape.equals(STR_NONE)) {
            return;
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_DOT)) {
            g.fillOval(x - 2, y - 2, 7, 7);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_BOX)) {
            g.fill3DRect(x - 3, y - 3, 6, 6, false);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_CIRCLE)) {
            g.fillOval(x - 2, y - 2, 7, 7);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_RHOMBUS)) {
            int rhombX[] = { x, x - 3, x, x + 3 };
            int rhombY[] = { y - 3, y, y + 3, y };
            g.fillPolygon(rhombX, rhombY, 4);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_TRIANGLE)) {
            int triX[] = { x, x - 3, x + 3 };
            int triY[] = { y + 3, y - 3, y - 3 };
            g.fillPolygon(triX, triY, 3);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_ROUND_BOX)) {
            g.fillRoundRect(x - 3, y - 3, 6, 6, 2, 2);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_DRAW_BOX)) {
            g.drawRect(x - 3, y - 3, 6, 6);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_DRAW_CIRCLE)) {
            g.drawOval(x - 2, y - 2, 7, 7);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_DRAW_RHOMBUS)) {
            int rhombX[] = { x, x - 3, x, x + 3 };
            int rhombY[] = { y - 3, y, y + 3, y };
            g.drawPolygon(rhombX, rhombY, 4);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_DRAW_TRIANGLE)) {
            int triX[] = { x, x - 3, x + 3 };
            int triY[] = { y + 3, y - 3, y - 3 };
            g.drawPolygon(triX, triY, 3);
        } else if (shape.equals(ComponentStatePanel.ITEM_HANDLE_DRAW_ROUND_BOX)) {
            g.drawRoundRect(x - 3, y - 3, 6, 6, 2, 2);
        }
    }

    protected void paintItemLine(int x1, int y1, int x2, Graphics g) {
        g.fillRoundRect(x1, y1 - 2, x2 - x1, 4, 2, 2);
    }

    protected void paintItemLine_buffer(int x1, int y1, int x2, Graphics g) {
        g.fillRoundRect(x1, y1 - 1, x2 - x1, 2, 1, 1);
    }

    protected void paintComponentLane(Graphics g) {
        g.setFont(g.getFont().deriveFont((float) 10.0));
        Color fgColor = null;
        Color bgColor = null;
        Color tmpColor = null;
        fgColor = null;
        bgColor = null;
        int unitHeight = 30;
        if (getHashMapSize() != 0) unitHeight = (this.getHeight() - 2 * border) / getHashMapSize();
        int yTop = border;
        int yBottom = this.getHeight() - border;
        int pixStart = 0;
        String dateStr, timeStr, millisStr = null;
        clipL = (int) g.getClipBounds().getMinX() - 1;
        clipR = (int) g.getClipBounds().getMaxX() + 1;
        fgColor = colorLogDark;
        bgColor = colorLogBright;
        int currentTop = yTop;
        Iterator itr = csModel.getItemMap().keySet().iterator();
        g.setFont(new Font("Dialog", Font.BOLD, 13));
        int index = 0;
        currentTop = yTop;
        while (itr.hasNext()) {
            String dimName = (String) itr.next();
            if (csModel.getTypeHashMap().equals(ST_INST) && !csModel.getInstanceTypeToKeep().contains(dimName)) continue;
            g.setColor(bgColor);
            g.fillRect(pixStart, currentTop, clipR, currentTop + unitHeight);
            g.setColor(fgColor);
            g.drawString(dimName, pixStart + 5, currentTop + 20);
            index++;
            currentTop = unit2Cord(index);
            tmpColor = fgColor;
            fgColor = bgColor;
            bgColor = tmpColor;
        }
        g.setFont(new Font("Dialog", Font.PLAIN, 12));
        g.setColor(colorTimeLine);
        g.drawLine(clipL, yTop, clipR, yTop);
        g.drawLine(clipL, yBottom, clipR, yBottom);
        clipLeftTs = coord2timeMillis(clipL);
        clipRightTs = coord2timeMillis(clipR);
        if (bAdjust) {
            adjustWidth();
            bAdjust = false;
        }
        for (long timeStart = csModel.getLogBoundaryLeft().getTime(); timeStart < clipRightTs; timeStart += csOptionPanel.getWidthDivider()) {
            pixStart = time2coord(timeStart) + border;
            cal.setTimeInMillis(timeStart);
            g.setColor(colorTimeLine);
            g.drawLine(pixStart, yTop, pixStart, yBottom);
            g.setColor(colorLogDark);
            if (timeOption.equals(TIME_ACTUAL)) {
                dateStr = cal.get(Calendar.DAY_OF_MONTH) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR);
                g.drawString(dateStr, pixStart + 2, yTop);
                timeStr = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
                g.drawString(timeStr, pixStart + 2, yTop + 10);
            } else if (timeOption.equals(TIME_RELATIVE_TIME)) {
                long days = timeStart / 1000 / 60 / 60 / 24;
                long hours = (timeStart - days * 24 * 60 * 60 * 1000) / 1000 / 60 / 60;
                long minutes = (timeStart - days * 24 * 60 * 60 * 1000 - hours * 60 * 60 * 1000) / 1000 / 60;
                long seconds = (timeStart - days * 24 * 60 * 60 * 1000 - hours * 60 * 60 * 1000 - minutes * 60 * 1000) / 1000;
                timeStr = days + "days:" + hours + ":" + minutes + ":" + seconds;
                g.drawString(timeStr, pixStart + 2, yTop);
            } else if (timeOption.equals(TIME_RELATIVE_RATIO)) {
                timeStr = timeStart / 100 + "." + (timeStart - timeStart / 100 * 100) + "%";
                g.drawString(timeStr, pixStart + 2, yTop);
            } else if (timeOption.equals(TIME_LOGICAL) || timeOption.equals(TIME_LOGICAL_RELATIVE)) {
                timeStr = String.valueOf(timeStart);
                g.drawString(timeStr, pixStart + 2, yTop);
            }
        }
    }

    protected void paintComponentLane(Graphics2D g, int width, int hight) {
        g.setFont(g.getFont().deriveFont((float) 10.0));
        Color fgColor = null;
        Color bgColor = null;
        Color tmpColor = null;
        fgColor = null;
        bgColor = null;
        int unitHeight = hight / getHashMapSize();
        int yTop = 0;
        int yBottom = hight;
        int pixStart = 0;
        String dateStr, timeStr, millisStr = null;
        clipL = 0;
        clipR = width;
        fgColor = colorLogDark;
        bgColor = colorLogBright;
        int currentTop = yTop;
        Iterator itr = csModel.getItemMap().keySet().iterator();
        g.setFont(new Font("Dialog", Font.BOLD, 13));
        int index = 0;
        currentTop = yTop;
        while (itr.hasNext()) {
            String dimName = (String) itr.next();
            if (csModel.getTypeHashMap().equals(ST_INST) && !csModel.getInstanceTypeToKeep().contains(dimName)) continue;
            g.setColor(bgColor);
            g.fillRect(pixStart, currentTop, clipR, currentTop + unitHeight);
            g.setColor(fgColor);
            index++;
            currentTop = unit2Cord_buffer(index, hight);
            tmpColor = fgColor;
            fgColor = bgColor;
            bgColor = tmpColor;
        }
        g.setFont(new Font("Dialog", Font.PLAIN, 12));
        g.setColor(colorTimeLine);
        g.drawLine(clipL, yTop, clipR, yTop);
        g.drawLine(clipL, yBottom, clipR, yBottom);
        clipLeftTs = coord2timeMillis(clipL);
        clipRightTs = coord2timeMillis(clipR);
    }

    protected Color getColorByState(String aState, String type) {
        Color stateColor = null;
        stateColor = (Color) csModel.getStateColorMap(type).get(aState);
        return stateColor;
    }

    protected Color combineColors(Color[] colors) {
        int r = 0, g = 0, b = 0;
        int len = colors.length;
        for (int i = 0; i < len; i++) {
            r += colors[i].getRed();
            g += colors[i].getGreen();
            b += colors[i].getBlue();
        }
        r /= len;
        g /= len;
        b /= len;
        return new Color(r, g, b);
    }

    protected Color prioritizeColors(ArrayList aStateList, String type) {
        String maxState = (String) aStateList.get(0);
        int len = aStateList.size();
        int priority = (Integer) csModel.getStatePriorityMap(type).get(aStateList.get(0));
        for (int i = 0; i < len; i++) {
            String state = (String) aStateList.get(i);
            if (priority > (Integer) csModel.getStatePriorityMap(type).get(state)) {
                priority = (Integer) csModel.getStatePriorityMap(type).get(state);
                maxState = state;
            }
        }
        return getColorByState(maxState, type);
    }

    public String getStateByEvent(AbstractLogUnit event, String type) {
        String componentState = "no state";
        if (event != null) {
            if ((csModel.getSelectedEventsForStates(type)).containsKey(event.getType())) {
                componentState = (String) (csModel.getSelectedEventsForStates(csModel.getTypeHashMap())).get(event.getType());
            }
        }
        return componentState;
    }

    protected void initPaintOriginatorLane(String key, int top, int height, ArrayList instancesList, Graphics g) {
        Color laneColorRed = new Color(200, 50, 50);
        int unitHeight = height;
        int bottom = top - height;
        clipL = (int) g.getClipBounds().getMinX() - 1;
        clipR = (int) g.getClipBounds().getMaxX() + 1;
        for (int i = 0; i < instancesList.size(); i++) {
            int startPos = time2coord(csModel.getStartDateMap(ST_INST).get(instancesList.get(i))) + border;
            int endPos = time2coord(csModel.getEndDateMap(ST_INST).get(instancesList.get(i))) + border;
            g.setColor(laneColorRed);
            if (endPos < clipR) {
                g.fillRect(startPos, top, endPos - startPos, unitHeight);
                g.setColor(Color.BLACK);
                g.drawRect(startPos, top, endPos - startPos, unitHeight);
            } else {
                g.fillRect(startPos, top, clipR - startPos, unitHeight);
                g.setColor(Color.BLACK);
                g.drawRect(startPos, top, endPos - startPos, unitHeight);
            }
            g.setFont(new Font("Dialog", Font.BOLD, 13));
            g.setColor(Color.black);
            if (startPos < 300) {
                g.drawString(key, 5, top + 20);
            }
            if (bAdjust) {
                adjustWidth();
                bAdjust = false;
            }
        }
    }

    protected void paintStateLane(int top, int height, int startPos, int endPos, LogUnitList eventsList, Graphics g, String key) {
        g.setFont(g.getFont().deriveFont((float) 10.0));
        Color fgColor = null;
        Color bgColor = null;
        Color tmpColor = null;
        Color laneColor = null;
        Color[] stateColors = new Color[eventsList.size()];
        ArrayList stateList = new ArrayList();
        AbstractLogUnit event = null;
        String elementState = null;
        int unitHeight = height;
        int bottom = top - height;
        String dateStr, timeStr, millisStr = null;
        clipL = (int) g.getClipBounds().getMinX() - 1;
        clipR = (int) g.getClipBounds().getMaxX() + 1;
        g.setFont(new Font("Dialog", Font.BOLD, 13));
        if (eventsList.size() >= 1) {
            int i = 0;
            for (Iterator it = ((LogUnitList) eventsList).iterator(); it.hasNext(); ) {
                event = (AbstractLogUnit) it.next();
                elementState = getStateByEvent(event, csModel.getTypeHashMap());
                stateList.add(elementState);
                tmpColor = getColorByState(elementState, csModel.getTypeHashMap());
                stateColors[i] = tmpColor;
                i++;
            }
            if (csa.getSettingPanel().combineColor()) {
                laneColor = (Color) combineColors(stateColors);
            } else if (csa.getSettingPanel().prioritizeColor()) {
                laneColor = (Color) prioritizeColors(stateList, csModel.getTypeHashMap());
            }
            g.setColor(laneColor);
            if (endPos < clipR) {
                g.fillRect(startPos, top, endPos - startPos, unitHeight);
                g.setColor(Color.BLACK);
                g.drawRect(startPos, top, endPos - startPos, unitHeight);
            } else {
                g.fillRect(startPos, top, clipR - startPos, unitHeight);
                g.setColor(Color.BLACK);
                g.drawRect(startPos, top, clipR - startPos, unitHeight);
            }
            clipLeftTs = coord2timeMillis(clipL);
            clipRightTs = coord2timeMillis(clipR);
            if (bAdjust) {
                adjustWidth();
                bAdjust = false;
            }
        }
    }

    protected void paintItemWithLaneHeight(int x, int y, Graphics g, Color aColor, int laneHeight) {
        g.setColor(aColor);
        int y0, x0;
        x0 = 5;
        y0 = laneHeight / 2 - 4;
        if (y0 <= 5) y0 = 5;
        g.fillRoundRect(x - x0, y - y0, 2 * x0, 2 * y0, x0 / 2, x0 / 2);
        g.setColor(Color.BLACK);
        g.drawRoundRect(x - x0, y - y0, 2 * x0, 2 * y0, x0 / 2, x0 / 2);
    }

    protected void paintItemWithLaneHeightOLD(int x, int y, Graphics g, String shape, int laneHeight) {
    }

    public ArrayList orderPIEndTimestampList(LogUnitList tempDrawList) {
        ArrayList orderedList = new ArrayList();
        AbstractLogUnit event = null;
        Date endPIDate = null;
        if (tempDrawList != null && tempDrawList.size() > 0) {
            for (Iterator iter = tempDrawList.iterator(); iter.hasNext(); ) {
                event = (AbstractLogUnit) iter.next();
                if (timeOption.equals(TIME_RELATIVE_TIME)) {
                    endPIDate = new Date(csModel.getEndDateMap(ST_INST).get(event.getProcessInstance().getName()).getTime() - csModel.getStartDateMap(ST_INST).get(event.getProcessInstance().getName()).getTime());
                } else if (timeOption.equals(TIME_RELATIVE_RATIO)) {
                    endPIDate = new Date(10000);
                } else endPIDate = csModel.getEndDateMap(ST_INST).get(event.getProcessInstance().getName());
                if (orderedList.size() != 0) {
                    if (((Date) orderedList.get(orderedList.size() - 1)).before(endPIDate)) {
                        orderedList.add(endPIDate);
                        continue;
                    } else if (!((Date) orderedList.get(orderedList.size() - 1)).after(endPIDate)) {
                        orderedList.add(endPIDate);
                        continue;
                    }
                    if (((Date) orderedList.get(0)).after(endPIDate)) {
                        orderedList.add(0, endPIDate);
                        continue;
                    } else if (!((Date) orderedList.get(0)).before(endPIDate)) {
                        orderedList.add(0, endPIDate);
                        continue;
                    }
                    int x_min = 0;
                    int x_max = orderedList.size();
                    int x_mean;
                    while (true) {
                        int x_temp;
                        x_mean = (x_min + x_max) / 2;
                        if (((Date) orderedList.get(x_mean)).before(endPIDate)) {
                            if (x_min == (x_mean + x_max) / 2) {
                                orderedList.add(x_min + 1, endPIDate);
                                break;
                            }
                            x_min = x_mean;
                        } else if (((Date) orderedList.get(x_mean)).after(endPIDate)) {
                            if (x_min == (x_min + x_mean) / 2) {
                                orderedList.add(x_min + 1, endPIDate);
                                break;
                            }
                            x_max = x_mean;
                        } else {
                            orderedList.add(x_mean + 1, endPIDate);
                            break;
                        }
                    }
                } else {
                    orderedList.add(endPIDate);
                }
            }
            return orderedList;
        } else return null;
    }

    public void paintInterval(LogUnitList tempDrawList, String key, Date itemTimestamp, Date nextItemTimestamp, int height, int top, Graphics gr) {
        Date endPIDate = null;
        Date prevDate = itemTimestamp;
        AbstractLogUnit event = null;
        ArrayList orderedPIList = (ArrayList) orderPIEndTimestampList(tempDrawList);
        if (tempDrawList != null && tempDrawList.size() > 0 && orderedPIList != null) {
            int index = 0;
            while (index < orderedPIList.size() && !((Date) orderedPIList.get(index)).after(nextItemTimestamp)) {
                for (Iterator iter = tempDrawList.iterator(); iter.hasNext(); ) {
                    event = (AbstractLogUnit) iter.next();
                    if (timeOption.equals(TIME_RELATIVE_TIME)) {
                        endPIDate = new Date(csModel.getEndDateMap(ST_INST).get(event.getProcessInstance().getName()).getTime() - csModel.getStartDateMap(ST_INST).get(event.getProcessInstance().getName()).getTime());
                    } else if (timeOption.equals(TIME_RELATIVE_RATIO)) {
                        endPIDate = new Date(10000);
                    } else endPIDate = csModel.getEndDateMap(ST_INST).get(event.getProcessInstance().getName());
                    if (((Date) orderedPIList.get(index)).equals(endPIDate) && endPIDate != null) {
                        paintStateLane(top, height, time2coord(prevDate) + border, time2coord(endPIDate) + border, tempDrawList, gr, key);
                        tempDrawList.removeEvent(event);
                        iter = tempDrawList.iterator();
                        prevDate = endPIDate;
                        break;
                    }
                }
                index++;
            }
            paintStateLane(top, height, time2coord(prevDate) + border, time2coord(nextItemTimestamp) + border, tempDrawList, gr, key);
        }
    }

    public void paintInterval(LogUnitList tempDrawList, String key, AbstractLogUnit firstEvent, AbstractLogUnit secondEvent, int height, int top, Graphics gr) {
        paintStateLane(top, height, time2coord(firstEvent.getCurrentTimeStamp()) + border, time2coord(firstEvent.getCurrentTimeStamp()) + border, tempDrawList, gr, key);
        if (secondEvent == null) return;
        Date endPIDate = null;
        AbstractLogUnit event = null;
        ArrayList orderedPIList = (ArrayList) orderPIEndTimestampList(tempDrawList);
        if (tempDrawList != null && tempDrawList.size() > 0 && orderedPIList != null) {
            int index = 0;
            if (timeOption.equals(TIME_LOGICAL) || timeOption.equals(TIME_LOGICAL_RELATIVE)) {
                Date endTempDate = null;
                AbstractLogUnit tempEvent = null;
                boolean flag = true;
                while (flag && tempDrawList.size() > 0) {
                    for (Iterator iter = tempDrawList.iterator(); iter.hasNext(); ) {
                        event = (AbstractLogUnit) iter.next();
                        Date endDate = csModel.getEndDateMap(ST_INST).get(event.getProcessInstance().getName());
                        if (endTempDate == null || endTempDate.after(endDate)) {
                            endTempDate = endDate;
                            tempEvent = event;
                        }
                    }
                    if (endTempDate.before(secondEvent.getActualTimeStamp())) {
                        tempDrawList.removeEvent(event);
                    } else flag = false;
                }
                if (tempDrawList.size() > 0) {
                    paintStateLane(top, height, time2coord(firstEvent.getCurrentTimeStamp()) + border, time2coord(secondEvent.getCurrentTimeStamp()) + border, tempDrawList, gr, key);
                }
            }
        } else {
            paintInterval(tempDrawList, key, firstEvent.getCurrentTimeStamp(), secondEvent.getCurrentTimeStamp(), height, top, gr);
        }
    }

    public boolean isFirstExternal(AbstractLogUnit event, LogUnitList tempDrawList, ArrayList evListForExternalEvents) {
        boolean firstExternal = false;
        AbstractLogUnit item = null;
        if (tempDrawList != null && tempDrawList.size() > 0) {
            for (Iterator ite = tempDrawList.iterator(); ite.hasNext(); ) {
                item = (AbstractLogUnit) ite.next();
                if (item.getElement().equals(event.getElement()) && item.getProcessInstance().equals(event.getProcessInstance()) && (evListForExternalEvents.isEmpty() || !evListForExternalEvents.contains(item))) {
                    firstExternal = true;
                    evListForExternalEvents.add(item);
                }
            }
        }
        return firstExternal;
    }

    /**
	 * paints this log item panel and all contained log items as specified.
	 * 
	 * @param g
	 *            the graphics object used for painting
	 */
    public void paintComponent(Graphics grx) {
        Graphics gr = grx.create();
        if (this.isOpaque()) {
            gr.setColor(colorBg);
            gr.fillRect(0, 0, getWidth(), getHeight());
        }
        paintComponentLane(gr);
        int height = (int) ((double) (getHeight() - (2 * border)));
        int unitHeight = 30;
        if (getHashMapSize() != 0) unitHeight = height / getHashMapSize();
        int currentTop = 0;
        if (csModel.getItemMap().size() > 0) {
            String key = null;
            AbstractLogUnit item = null;
            AbstractLogUnit nextItem = null;
            Date endItemTimestamp = null;
            Date nextItemTimestamp = null;
            String type = csModel.getTypeHashMap();
            int index = 0;
            for (Iterator itSets = csModel.getItemMap().keySet().iterator(); itSets.hasNext(); ) {
                key = (String) itSets.next();
                if (csModel.getTypeHashMap().equals(ST_INST) && !csModel.getInstanceTypeToKeep().contains(key)) continue;
                if (csModel.getEventTypeToKeep() == null || csModel.getEventTypeToKeep().isEmpty()) continue;
                currentTop = unit2Cord(index) + unitHeight / 2;
                if (csModel.getTypeHashMap().equals(ST_ORIG)) {
                    initPaintOriginatorLane(key, currentTop - unitHeight / 2, unitHeight, csModel.getInstanceTypeToKeep(), gr);
                }
                LogUnitList sortedElementList = (LogUnitList) csModel.getSortedItemMap().get(key);
                ArrayList helpList = new ArrayList();
                helpList = csModel.getHelpListElement(sortedElementList);
                LogUnitList tempDrawList = new LogUnitList();
                for (Iterator itItm = sortedElementList.iterator(); itItm.hasNext(); ) {
                    item = (AbstractLogUnit) itItm.next();
                    if (!csModel.getInstanceTypeToKeep().contains(item.getProcessInstance().getName())) continue;
                    if (!csModel.getTypeHashMap().equals(ST_ORIG) && csModel.getEventTypeToKeep() != null && !csModel.getEventTypeToKeep().contains(item.getType())) continue;
                    if (csModel.getTypeHashMap().equals(ST_ORIG) && (((csModel.getEventTypeToKeep() != null && !csModel.getEventTypeToKeep().contains(item.getType()))) && (csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK) != null && !csModel.getSelectedEventsForStates(ComponentStateModel.ST_TASK).containsKey(item.getType())))) {
                        continue;
                    }
                    assignColorByItem(item, gr);
                    clipL = (int) gr.getClipBounds().getMinX() - 1;
                    clipR = (int) gr.getClipBounds().getMaxX() + 1;
                    long clipLeftTs2 = coord2timeMillis(clipL);
                    long clipRightTs2 = coord2timeMillis(clipR);
                    String componentState = null;
                    String stateShapeType = null;
                    ArrayList usersList = null;
                    boolean externalEvent = false;
                    cleanDrawUnitList(tempDrawList, item);
                    if (csModel.getTypeHashMap().equals(ST_ORIG)) {
                        if (csModel.getAssignedUsersMap().containsKey(item)) {
                            usersList = (ArrayList) csModel.getAssignedUsersMap().get(item);
                        }
                        if (csModel.getEventTypeToKeep().contains(item.getType()) && (item.getOriginator().equals(key) || (usersList != null && usersList.contains(key)))) {
                            componentState = getStateByEvent(item, ComponentStateModel.ST_ORIG);
                        } else externalEvent = true;
                    } else if (csModel.getTypeHashMap().equals(ST_INST) || csModel.getTypeHashMap().equals(ST_TASK) || csModel.getTypeHashMap().equals(ST_DATA)) {
                        componentState = getStateByEvent(item, csModel.getTypeHashMap());
                    }
                    if (externalEvent) {
                        updateDrawUnitList(tempDrawList, item);
                        if (item.getCurrentTimeStamp().getTime() > clipRightTs2) continue;
                        if (tempDrawList != null && tempDrawList.size() > 0) {
                            nextItemTimestamp = getNextEventTimestamp(item, key, helpList);
                            if (nextItemTimestamp == null || nextItemTimestamp.getTime() < clipLeftTs2) continue;
                            if (timeOption.equals(TIME_ACTUAL) || timeOption.equals(TIME_RELATIVE_TIME) || timeOption.equals(TIME_RELATIVE_RATIO)) paintInterval(tempDrawList, key, item.getCurrentTimeStamp(), nextItemTimestamp, unitHeight, currentTop - unitHeight / 2, gr); else paintInterval(tempDrawList, key, item, getNextEvent(item, key, helpList), unitHeight, currentTop - unitHeight / 2, gr);
                        }
                        continue;
                    }
                    if (componentState == null) {
                        continue;
                    } else {
                        stateShapeType = getStateShapeType(componentState);
                    }
                    if ((stateShapeType != null) && stateShapeType.equals(ComponentStatePanel.STATE_SHAPE_SEPARATOR)) {
                        updateDrawUnitList(tempDrawList, item);
                        if (item.getCurrentTimeStamp().getTime() > clipRightTs2) continue;
                        if (tempDrawList != null && tempDrawList.size() > 0) {
                            nextItemTimestamp = getNextEventTimestamp(item, key, helpList);
                            if (nextItemTimestamp == null || nextItemTimestamp.getTime() < clipLeftTs2) continue;
                            if (timeOption.equals(TIME_ACTUAL) || timeOption.equals(TIME_RELATIVE_TIME) || timeOption.equals(TIME_RELATIVE_RATIO)) paintInterval(tempDrawList, key, item.getCurrentTimeStamp(), nextItemTimestamp, unitHeight, currentTop - unitHeight / 2, gr); else paintInterval(tempDrawList, key, item, getNextEvent(item, key, helpList), unitHeight, currentTop - unitHeight / 2, gr);
                        }
                        if (item.getCurrentTimeStamp().getTime() < clipLeftTs2) continue;
                        Color stateColor = this.getColorByState(componentState, type);
                        paintItemWithLaneHeight(time2coord(item.getCurrentTimeStamp()) + border, currentTop, gr, stateColor, unitHeight);
                    } else if ((stateShapeType != null) && stateShapeType.equals(ComponentStatePanel.STATE_SHAPE_LANE)) {
                        updateDrawUnitList(tempDrawList, item);
                        if (item.getCurrentTimeStamp().getTime() > clipRightTs2) continue;
                        boolean addState = false;
                        if ((csModel.getTypeHashMap().equals(ST_TASK)) || (csModel.getTypeHashMap().equals(ST_INST)) || csModel.getTypeHashMap().equals(ST_DATA)) {
                            addState = true;
                        }
                        if (csModel.getTypeHashMap().equals(ST_ORIG)) {
                            if (csModel.getEventTypeToKeep().contains(item.getType()) && ((componentState.equals(ComponentStatePanel.STATE_WORKING) && item.getOriginator().equals(key)) || (componentState.equals(ComponentStatePanel.STATE_ASSIGNED) && usersList != null && usersList.contains(key)))) {
                                addState = true;
                            }
                        }
                        if (addState) {
                            tempDrawList.addEvent(item);
                            nextItemTimestamp = getNextEventTimestamp(item, key, helpList);
                            if (nextItemTimestamp == null || nextItemTimestamp.getTime() < clipLeftTs2) continue;
                            if (timeOption.equals(TIME_ACTUAL) || timeOption.equals(TIME_RELATIVE_TIME) || timeOption.equals(TIME_RELATIVE_RATIO)) paintInterval(tempDrawList, key, item.getCurrentTimeStamp(), nextItemTimestamp, unitHeight, currentTop - unitHeight / 2, gr); else paintInterval(tempDrawList, key, item, getNextEvent(item, key, helpList), unitHeight, currentTop - unitHeight / 2, gr);
                        }
                    }
                    gr.setFont(new Font("Dialog", Font.BOLD, 13));
                    gr.setColor(Color.black);
                    if (time2coord(item.getCurrentTimeStamp()) + border < 300) {
                        gr.drawString(key, 5, currentTop - unitHeight / 2 + 20);
                    }
                }
                index++;
            }
        }
        if (p1 != null && p2 != null) {
            int x1 = Math.min(p1.x, p2.x);
            int y1 = Math.min(p1.y, p2.y);
            int width = Math.abs(p1.x - p2.x);
            height = Math.abs(p1.y - p2.y);
            grx.drawRect(x1, y1, width, height);
        }
    }

    public BufferedImage getBufferedImage() {
        return matrixBuffer;
    }

    public void generateBufferedImage(int width, int height) {
        matrixBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D gr = matrixBuffer.createGraphics();
        gr.setColor(colorBg);
        gr.fillRect(0, 0, width, height);
        paintComponentLane(gr, width, height);
        int unitHeight = height / getHashMapSize();
        int currentTop = 0;
        if (csModel.getItemMap().size() > 0) {
            String key = null;
            AbstractLogUnit item = null;
            int index = 0;
            for (Iterator itSets = csModel.getItemMap().keySet().iterator(); itSets.hasNext(); ) {
                key = (String) itSets.next();
                if (csModel.getTypeHashMap().equals(ST_INST) && !csModel.getInstanceTypeToKeep().contains(key)) continue;
                currentTop = unit2Cord_buffer(index, height) + unitHeight / 2;
                LogUnitList tempUnitList = new LogUnitList();
                for (Iterator itItm = ((LogUnitList) csModel.getItemMap().get(key)).iterator(); itItm.hasNext(); ) {
                    item = (AbstractLogUnit) itItm.next();
                    if (csModel.getEventTypeToKeep() != null && (!csModel.getEventTypeToKeep().contains(item.getType()) || !csModel.getInstanceTypeToKeep().contains(item.getProcessInstance().getName()))) continue;
                    assignColorByItem(item, gr);
                    clipL = 0;
                    clipR = width;
                    double milli2pixels = (double) ((width) - 10) / (double) (csModel.getLogBoundaryRight().getTime() - csModel.getLogBoundaryLeft().getTime());
                    long clipLeftTs2 = coord2timeMillis_buffer(clipL, milli2pixels);
                    long clipRightTs2 = coord2timeMillis_buffer(clipR, milli2pixels);
                    if (item.getCurrentTimeStamp().getTime() < clipLeftTs2 || item.getCurrentTimeStamp().getTime() > clipRightTs2) continue;
                    this.paintItem_buffer(time2coord_buffer(item.getCurrentTimeStamp(), milli2pixels) + 3, currentTop, gr, assignShapeByItem(item));
                }
                index++;
            }
        }
    }

    public String timeFormat(Date aDate) {
        String str = null;
        if (timeOption.equals(TIME_ACTUAL)) str = String.valueOf(aDate); else if (timeOption.equals(TIME_RELATIVE_TIME)) str = String.valueOf((float) aDate.getTime() / (float) timeSort); else if (timeOption.equals(TIME_RELATIVE_RATIO)) str = String.valueOf((float) aDate.getTime() / 100) + "%"; else if (timeOption.equals(TIME_LOGICAL) || timeOption.equals(TIME_LOGICAL_RELATIVE)) str = String.valueOf(aDate.getTime());
        return str;
    }

    public void redrawTitle(int xPos) {
        Graphics g = this.getGraphics();
        Color fgColor = null;
        Color bgColor = null;
        Color tmpColor = null;
        fgColor = null;
        bgColor = null;
        int yTop = border;
        fgColor = colorLogDark;
        bgColor = colorLogBright;
        int currentTop = yTop;
        Iterator itr = csModel.getItemMap().keySet().iterator();
        g.setFont(new Font("Dialog", Font.BOLD, 13));
        int index = 0;
        currentTop = yTop;
        while (itr.hasNext()) {
            String dimName = (String) itr.next();
            if (csModel.getTypeHashMap().equals(ST_INST) && !csModel.getInstanceTypeToKeep().contains(dimName)) continue;
            g.setColor(fgColor);
            g.drawString(dimName, xPos + 5, currentTop + 20);
            index++;
            currentTop = unit2Cord(index);
            tmpColor = fgColor;
            fgColor = bgColor;
            bgColor = tmpColor;
        }
    }

    /**
	 * Shows information in a tooltip about the sequence over which the mouse
	 * moved.
	 * 
	 * @param e
	 *            MouseEvent
	 */
    public void mouseMoved(MouseEvent e) {
        try {
            Point p = e.getPoint();
            String str = "<html>";
            int height = (int) ((double) (getHeight() - (2 * border)));
            int unitHeight = 30;
            if (getHashMapSize() != 0) unitHeight = height / getHashMapSize();
            int currentTop = 0;
            String key = null;
            AbstractLogUnit item = null;
            boolean flag = false;
            String componentState = null;
            int index = -1;
            for (Iterator itSets = csModel.getItemMap().keySet().iterator(); itSets.hasNext(); ) {
                key = (String) itSets.next();
                if (csModel.getTypeHashMap().equals(ST_INST) && !csModel.getInstanceTypeToKeep().contains(key)) continue;
                if (csModel.getEventTypeToKeep() == null || csModel.getEventTypeToKeep().isEmpty()) continue;
                index++;
                currentTop = unit2Cord(index) + unitHeight / 2;
                if (currentTop - unitHeight / 2 >= p.getY() || p.getY() >= currentTop + unitHeight / 2) continue;
                LogUnitList tempLogUnit = (LogUnitList) csModel.getSortedItemMap().get(key);
                ArrayList helpList = new ArrayList();
                helpList = csModel.getHelpListElement(tempLogUnit);
                for (Iterator itItm = tempLogUnit.iterator(); itItm.hasNext(); ) {
                    item = (AbstractLogUnit) itItm.next();
                    if (!csModel.getInstanceTypeToKeep().contains(item.getProcessInstance().getName())) continue;
                    if (csModel.getEventTypeToKeep() != null && !csModel.getEventTypeToKeep().contains(item.getType())) continue;
                    int x = 0;
                    String stateShapeType = null;
                    ArrayList usersList = null;
                    if (csModel.getAssignedUsersMap().containsKey(item)) {
                        usersList = (ArrayList) csModel.getAssignedUsersMap().get(item);
                    }
                    componentState = getStateByEvent(item, csModel.getTypeHashMap());
                    if (componentState != null) {
                        stateShapeType = getStateShapeType(componentState);
                    } else {
                        continue;
                    }
                    if (csModel.getTypeHashMap().equals(ST_ORIG) && !(csModel.getEventTypeToKeep().contains(item.getType()) && ((componentState.equals(ComponentStatePanel.STATE_WORKING) && item.getOriginator().equals(key)) || (componentState.equals(ComponentStatePanel.STATE_ASSIGNED) && usersList != null && usersList.contains(key))))) {
                        continue;
                    }
                    x = time2coord(item.getCurrentTimeStamp()) + border;
                    if ((stateShapeType != null) && stateShapeType.equals(ComponentStatePanel.STATE_SHAPE_SEPARATOR)) {
                        if (x - 5 <= p.getX() && p.getX() <= x + 5) {
                            flag = true;
                            str += item.getProcessInstance().getName() + " | " + item.getElement() + " | " + item.getOriginator() + " | " + item.getTimestamp() + " | " + componentState;
                            if (!timeOption.equals(TIME_ACTUAL)) str += " | Position:" + timeFormat(item.getCurrentTimeStamp());
                            str += "<br>";
                        }
                    } else if ((stateShapeType != null) && stateShapeType.equals(ComponentStatePanel.STATE_SHAPE_LANE)) {
                        int x2 = time2coord(getEndEventTimestamp(item, key, helpList)) + border;
                        if (x <= p.getX() && p.getX() <= x2) {
                            flag = true;
                            if (csModel.getTypeHashMap().equals(ST_INST) || csModel.getTypeHashMap().equals(ST_TASK) || csModel.getTypeHashMap().equals(ST_DATA)) {
                                str += item.getProcessInstance().getName() + " | " + item.getElement() + " | " + item.getOriginator() + " | " + item.getTimestamp() + " | " + componentState;
                                if (!timeOption.equals(TIME_ACTUAL)) str += " | Position:" + timeFormat(item.getCurrentTimeStamp());
                                str += "<br>";
                            } else if (csModel.getTypeHashMap().equals(ST_ORIG)) {
                                str += item.getProcessInstance().getName() + " | " + item.getElement() + " | " + key + " | " + item.getTimestamp() + " | " + componentState;
                                if (!timeOption.equals(TIME_ACTUAL)) str += " | Position:" + timeFormat(item.getCurrentTimeStamp());
                                str += "<br>";
                            }
                        }
                    }
                }
            }
            str += "</html>";
            if (flag) {
                this.setToolTipText(str);
                repaint();
                bTooltip = true;
            } else {
                this.setToolTipText(null);
                if (bTooltip) {
                    repaint();
                    bTooltip = false;
                }
            }
        } catch (Exception ex) {
            System.out.println("Mouse exception: " + ex);
        } finally {
        }
    }

    /**
	 * Required for mouselistener
	 * 
	 * @param e
	 *            MouseEvent
	 */
    public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();
        if (csOptionPanel.getMouseMode().equals(ST_DRAG)) {
            int x2 = p4.x - (p.x - p3.x);
            int y2 = p4.y - (p.y - p3.y);
            if (x2 < 0) x2 = 0;
            if (y2 < 0) y2 = 0;
            csa.setScrollBarPosition(new Point(x2, y2));
        } else {
            p2 = e.getPoint();
            repaint();
        }
    }

    public void mousePressed(MouseEvent e) {
        if (!clicked && csOptionPanel.getMouseMode().equals(ST_ZOOMIN)) {
            clicked = true;
            p1 = e.getPoint();
            p2 = e.getPoint();
            repaint();
        } else {
            p3 = e.getPoint();
            p4 = csa.getScrollPane().getViewport().getViewPosition();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (csOptionPanel.getMouseMode().equals(ST_ZOOMIN)) {
            clicked = false;
            p2 = e.getPoint();
            setViewportZoomIn();
        } else {
            Point p = e.getPoint();
            int x2 = p4.x - (p.x - p3.x);
            int y2 = p4.y - (p.y - p3.y);
            if (x2 < 0) x2 = 0;
            if (y2 < 0) y2 = 0;
            csa.setScrollBarPosition(new Point(x2, y2));
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void drawSelectionBox(Point p2) {
        Graphics g = this.getGraphics();
        int x1 = Math.min(p1.x, p2.x);
        int y1 = Math.min(p1.y, p2.y);
        int width = Math.abs(p1.x - p2.x);
        int height = Math.abs(p1.y - p2.y);
        g.drawRect(x1, y1, width, height);
        repaint();
    }
}
