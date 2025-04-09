package br.unb.entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import br.unb.entities.interfaces.IDrawnable;
import br.unb.entities.interfaces.IEntity;
import br.unb.entities.interfaces.IUpdatable;
import br.unb.entities.signalControllers.Detector;
import br.unb.entities.signalControllers.ReportMeasuresStart;
import br.unb.entities.signalControllers.SignalHead;
import br.unb.entities.vehicles.Vehicle;
import br.unb.entities.vehicles.reactionModels.AbstractVehicleReactionModel;
import br.unb.gui.editPanels.RoadSegmentEditPanel;
import br.unb.main.ModelController;

/**
 * A road segment is an entity in which many @link{IEntity}s may be.
 * Vehicles, signal heads, stop signals, yield signals, or
 * detectors are examples.
 * 
 * @author Marcelo Vale Asari
 */
public class RoadSegment implements IDrawnable, IEntity, IUpdatable {

    private Integer id;

    private double numOfVehicles;

    private double width;

    private double length;

    private double speedLimit;

    private List<IEntity> listOfEntities;

    private List<Vehicle> listOfVehicles;

    private ModelController modelController;

    private boolean isATurn;

    private Map<Destination, Integer> mapOfDestPossibility;

    private List<RoadSegment> listOfNextRoadSegments;

    private List<RoadSegment> listOfPreviousRoadSegments;

    private RoadSegment leftLane;

    private RoadSegment rightLane;

    private Set<RoadSegment> setOfConflictingRoads;

    private Destination destination;

    private boolean showRuler = true;

    private boolean selected;

    private double x0;

    private double y0;

    private double xf;

    private double yf;

    private double angle;

    private RoadSegmentEditPanel editPanel;

    private AffineTransform affineTransform;

    private Color[] colors = { Color.black, Color.red };

    /**
	 * Constructor, requires an <code>id</code> and a <code>modelController</code>
	 * 
	 * @param id
	 * @param modelController
	 */
    public RoadSegment(int id, ModelController modelController) {
        this.id = id;
        this.modelController = modelController;
        affineTransform = new AffineTransform();
        numOfVehicles = 0;
        width = 5;
        speedLimit = 100;
        setOfConflictingRoads = null;
        listOfEntities = new ArrayList<IEntity>();
        listOfVehicles = new ArrayList<Vehicle>();
        isATurn = false;
    }

    /**
	 * For use before a new simulation starts, <code>init</code> removes
	 * all vehicles from this road segment and sets the number of vehicle to zero. 
	 * 
	 */
    public void init() {
        listOfVehicles.clear();
        numOfVehicles = 0;
    }

    /**
	 * Given a initial and a final position, returns all entities 
	 * (including vehicles) in this pipe between these positions.  
	 * 
	 * @param initialPosition
	 * @param finalPosition
	 * @return List<IEntity> List of entities between initialPosition to finalPosition
	 */
    public List<IEntity> getEntities(double initialPosition, double finalPosition) {
        int i;
        IEntity entityAux;
        List<IEntity> listAux = new ArrayList<IEntity>();
        for (i = 0; i < listOfEntities.size(); i++) {
            entityAux = listOfEntities.get(i);
            if (((entityAux.getFrontPosition() >= initialPosition) && (entityAux.getFrontPosition() <= finalPosition)) || ((entityAux.getBackPosition() >= initialPosition) && (entityAux.getBackPosition() <= finalPosition))) {
                listAux.add(entityAux);
            }
        }
        listAux.addAll(getVehicles(initialPosition, finalPosition));
        return listAux;
    }

    /**
	 * Given a initial and a final position, returns all vehicles in this pipe 
	 * between them.
	 * 
	 * TODO fix this
	 * 
	 * @param initialPosition
	 * @param finalPosition
	 * @param entity
	 * @return List<Vehicle> List of entities between initialPosition to finalPosition
	 */
    public List<Vehicle> getVehicles(double initialPosition, double finalPosition) {
        List<Vehicle> listAux = new ArrayList<Vehicle>();
        for (int i = 0; i < listOfVehicles.size(); i++) {
            Vehicle vehicle = listOfVehicles.get(i);
            if (this.equals(vehicle.getCurrentRoadSegment())) {
                if (((vehicle.getFrontPosition() >= initialPosition) && (vehicle.getFrontPosition() <= finalPosition)) || ((vehicle.getBackPosition() >= initialPosition) && (vehicle.getBackPosition() <= finalPosition))) {
                    listAux.add(vehicle);
                }
            }
        }
        return listAux;
    }

    /**
	 * Simply returns all entities within this road segment
	 * @return List<IEntity>
	 */
    public List<IEntity> getEntities() {
        List<IEntity> list = new ArrayList<IEntity>();
        list.addAll(listOfEntities);
        for (Vehicle v : listOfVehicles) {
            if (this.equals(v.getCurrentRoadSegment())) {
                list.add(v);
            }
        }
        return list;
    }

    /**
	 * @return true if there are any vehicles in this road segment
	 */
    public boolean isOccupied() {
        if (listOfVehicles.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
	 * Sends the signal for vehicles that are before this road segment.
	 * 
	 * @return If the vehicle's speed is greater than this' speed limit, send STOP, 
	 * otherwise send GO
	 * @see IEntity
	 */
    public Signal getSignal(Vehicle vehicle, double time) {
        if (setOfConflictingRoads != null) {
            for (RoadSegment road : setOfConflictingRoads) {
                if (road.isOccupied()) {
                    return Signal.STOP;
                }
            }
        }
        if (vehicle.getSpeed() > speedLimit) {
            return Signal.STOP;
        } else {
            if (vehicle.getTemporaryDesiredSpeed() > speedLimit) {
                vehicle.setTemporaryDesiredSpeed(speedLimit);
            }
            return Signal.GO;
        }
    }

    /**
	 * Finds the first entity after the vehicle that signals stop for the it
	 * 
	 * @param vehicle
	 * @param time Required for getting the signal of the entities
	 * 
	 * @return IEntity The first entity in front
	 * @return null If there is no entity ahead
	 */
    public IEntity getFirstEntityAfter(Vehicle vehicle, double time) {
        IEntity entity = null;
        double position = vehicle.getFrontPosition();
        entity = getFirstVehicleAfter(position, vehicle);
        if (entity == null) {
            for (IEntity e : listOfEntities) {
                if ((e.getBackPosition() > position) && (Signal.STOP.equals(e.getSignal(vehicle, time)))) {
                    return e;
                }
            }
        }
        IEntity entityAux;
        double posAux = Double.MAX_VALUE;
        if (listOfNextRoadSegments != null) {
            for (RoadSegment roadSegment : listOfNextRoadSegments) {
                entityAux = roadSegment.getFirstEntityAfter(vehicle, time);
                if (entityAux != null) {
                    if (entityAux.getBackPosition() < posAux) {
                        posAux = entityAux.getBackPosition();
                        entity = entityAux;
                    }
                }
            }
        }
        return entity;
    }

    /**
	 * Finds the first vehicle after <code>position</code> and <code>vehicle</code>
	 * 
	 * @param position The position to be considered to get the nearer vehicle ahead
	 * @return Vehicle The first vehicle in front
	 * @return null If there is no vehicle ahead
	 */
    public Vehicle getFirstVehicleAfter(double position, Vehicle vehicle) {
        for (Vehicle v : listOfVehicles) {
            if ((!v.equals(vehicle)) && (v.getCurrentRoadSegment().equals(this))) {
                if ((v.getBackPosition() > position) || (v.getFrontPosition() > position)) {
                    return v;
                }
            }
        }
        Vehicle vehicleReturn = null;
        Vehicle vehicleAux;
        double distanceMin = Double.MAX_VALUE;
        double distanceAux;
        if (listOfNextRoadSegments != null) {
            for (RoadSegment roadSegment : listOfNextRoadSegments) {
                vehicleAux = roadSegment.getFirstVehicleAfter(0, vehicle);
                if (vehicleAux != null) {
                    distanceAux = AbstractVehicleReactionModel.getDistance(vehicle, vehicleAux);
                    if (distanceAux < distanceMin) {
                        distanceMin = distanceAux;
                        vehicleReturn = vehicleAux;
                    }
                }
            }
        }
        return vehicleReturn;
    }

    /**
	 * Finds the first vehicle behind <code>vehicle</code>
	 * 
	 * @param position The position to be considered to get the nearer vehicle behind
	 * @return Vehicle The first vehicle behind 
	 * @return null If there is no vehicle behind
	 */
    public Vehicle getFirstVehicleBefore(double position, Vehicle vehicle) {
        for (int i = listOfVehicles.size() - 1; i >= 0; i--) {
            Vehicle v = listOfVehicles.get(i);
            if ((!v.equals(vehicle)) && (v.getCurrentRoadSegment().equals(this))) {
                if ((v.getBackPosition() < position) || (v.getFrontPosition() < position)) {
                    return v;
                }
            }
        }
        Vehicle vehicleReturn = null;
        Vehicle vehicleAux;
        double distanceMin = Double.MAX_VALUE;
        double distanceAux;
        if (listOfPreviousRoadSegments != null) {
            for (RoadSegment roadSegment : listOfPreviousRoadSegments) {
                vehicleAux = roadSegment.getFirstVehicleBefore(roadSegment.getFrontPosition() + position, vehicle);
                if (vehicleAux != null) {
                    distanceAux = AbstractVehicleReactionModel.getDistance(vehicle, vehicleAux);
                    if (distanceAux < distanceMin) {
                        distanceMin = distanceAux;
                        vehicleReturn = vehicleAux;
                    }
                }
            }
        }
        return vehicleReturn;
    }

    /** 
	 * Returns the mapping of a destination to a possibility as an integer
	 * of reaching it from this road segment.
	 * There are three valid possibilities:
	 * <ul>
	 * <li>0 - There is no path to reach the destination from this road segment 
	 * <li>1 - This road segment lead directly to the destination, without the need
	 * 			of lane changes.
	 * <li>2 - This road segment does not lead directly to the destination, 
	 * 			a lane change is required.
	 * </ul>
	 * <p>
	 * 
	 * @param destination
	 * @return possibility
	 * */
    public int getDestinationPossibility(Destination destination) {
        if (mapOfDestPossibility != null) {
            return mapOfDestPossibility.get(destination);
        } else {
            return 0;
        }
    }

    /**
	 * Adds <code>nextSegment</code> to the <code>listOfNextRoadSegments</code>
	 * 
	 * Changes the initial position of <code>nextSegment</code>, if  
	 * its initial position is 0, else changes this initial position
	 * and keeps <code>nextSegment</code> initial position the same.
	 * 
	 * Note that this can lead to negative initial positions, however as this 
	 * positions are just a guidance, as an axis, there is no problem.
	 * 
	 * @param nextSegment
	 */
    public boolean addNextSegment(RoadSegment nextSegment) {
        if (listOfNextRoadSegments == null) {
            listOfNextRoadSegments = new ArrayList<RoadSegment>();
        }
        if (!listOfNextRoadSegments.contains(nextSegment)) {
            listOfNextRoadSegments.add(nextSegment);
            nextSegment.addPreviousSegment(this);
            return true;
        } else {
            return false;
        }
    }

    /**
	 * Inserts a <code>vehicle</code> into this road segment, in order by 
	 * the front position
	 *  
	 * @param vehicle
	 */
    public void insertVehicle(Vehicle vehicle) {
        int i;
        if (!listOfVehicles.contains(vehicle)) {
            for (i = 0; i < listOfVehicles.size(); i++) {
                Vehicle roadVehicle = listOfVehicles.get(i);
                if ((roadVehicle.getBackPosition() > vehicle.getFrontPosition()) && (roadVehicle.getCurrentRoadSegment().equals(this))) {
                    break;
                }
            }
            if (i < listOfVehicles.size()) {
                listOfVehicles.add(i, vehicle);
            } else {
                listOfVehicles.add(vehicle);
            }
        }
    }

    /**
	 * Inserts an <code>entity</code> into this road segment, in order by 
	 * the front position
	 *  
	 * @param entity
	 */
    public void insert(IEntity entity) {
        int i;
        if (!listOfEntities.contains(entity)) {
            for (i = 0; i < listOfEntities.size(); i++) {
                if (listOfEntities.get(i).getBackPosition() > entity.getFrontPosition()) {
                    break;
                }
            }
            if (i < listOfEntities.size()) {
                listOfEntities.add(i, entity);
            } else {
                listOfEntities.add(entity);
            }
        }
    }

    /**
	 * Updates all vehicles within this road segment
	 */
    public void update(double time) {
        for (int i = listOfVehicles.size() - 1; i >= 0; i--) {
            Vehicle vehicle = listOfVehicles.get(i);
            if (vehicle.getCurrentRoadSegment().equals(this)) {
                vehicle.update(time);
            }
        }
    }

    /**
	 * Effectively updates the vehicles, changing their positions
	 */
    public void updateState(double time) {
        for (int i = listOfVehicles.size() - 1; i >= 0; i--) {
            Vehicle vehicle = listOfVehicles.get(i);
            if (vehicle.getCurrentRoadSegment().equals(this)) {
                vehicle.updateState(time);
            }
        }
    }

    /**
	 * Verifies if the entity is in the entities list
	 * @param entity
	 * @return True if it is, false otherwise
	 */
    public boolean contains(IEntity entity) {
        boolean bool = false;
        bool = listOfEntities.contains(entity);
        if (bool == false) {
            if (entity instanceof Vehicle) {
                Vehicle vehicle = (Vehicle) entity;
                if (vehicle.getCurrentRoadSegment().equals(this)) {
                    bool = true;
                }
            }
        }
        return bool;
    }

    /**
	 * Changes this Road segment length and calculate
	 * <code>xf</code> and <code>yf</code> for the painting
	 * to be accurate
	 * 
	 * @param length The length to be set
	 */
    public void setLength(double length) {
        this.length = length;
        if (xf > x0) {
            yf = Math.sin(angle) * length + y0;
            xf = Math.cos(angle) * length + x0;
        } else {
            if (yf >= y0) {
                yf = Math.sin(angle - Math.PI) * length + y0;
                xf = Math.cos(angle - Math.PI) * length + x0;
            } else {
                yf = Math.sin(angle + Math.PI) * length + y0;
                xf = Math.cos(angle + Math.PI) * length + x0;
            }
        }
        RoadSegmentsUtil.moveAdjacentRoadSegments(this, true, true);
        modelController.repaint();
    }

    public double getLength() {
        return length;
    }

    /**
	 * Sets the final point, calculating the distance 
	 * between (x0, y0) and (xf,yf) to set the length.
	 * 
	 * @param xf
	 * @param yf
	 * @param updateLength If true, the length will be calculate from the distance, otherwise not 
	 */
    public void setFinalPosition(double xf, double yf, boolean updateLength) {
        this.xf = xf;
        this.yf = yf;
        if (updateLength) {
            this.length = Point.distance(x0, y0, xf, yf);
        }
        calculateAndSetAngle();
        if (editPanel != null) {
            editPanel.notifyChanges();
            modelController.repaint();
        }
    }

    /**
	 * Sets the initial point, calculating the distance 
	 * between (x0, y0) and (xf,yf) to set the length.
	 * 
	 * @param x0
	 * @param y0
	 * @param updateLength If true, the length will be calculate from the distance, otherwise not
	 */
    public void setInitialPosition(double x0, double y0, boolean updateLength) {
        this.x0 = x0;
        this.y0 = y0;
        if (updateLength) {
            this.length = Point.distance(x0, y0, xf, yf);
        }
        calculateAndSetAngle();
        if (editPanel != null) {
            editPanel.notifyChanges();
            modelController.repaint();
        }
    }

    /**
	 * Calculates the angle this road has with the x axis, setting 
	 * the <code>affineTransform</code> to make the task of painting easier. 
	 */
    private void calculateAndSetAngle() {
        double tg;
        if ((xf - x0) == 0) {
            tg = 90;
        } else {
            tg = (yf - y0) / (xf - x0);
        }
        if (tg == 90) {
            if (yf >= y0) {
                angle = -Math.PI / 2;
            } else {
                angle = Math.PI / 2;
            }
        } else {
            angle = Math.atan(tg);
        }
        affineTransform.setToIdentity();
        affineTransform.translate(x0, y0);
        affineTransform.rotate(angle);
        if (xf <= x0) {
            affineTransform.rotate(Math.PI);
        }
    }

    /**
	 * Calculate the initial and final points of the roadSeg passed as parameter, so that they are parallel
	 * 
	 * @return Point2D[] a array with 2 elements: index 0 is the initial point and index 1 i the final point
	 * @return null if the left lane is null 
	 */
    public Point2D[] calculateAdjacentLanePoints(RoadSegment roadSeg, boolean rightLane) {
        if (roadSeg != null) {
            double ty;
            if (rightLane) {
                ty = (width / 2 + roadSeg.getWidth() / 2);
            } else {
                ty = -(width / 2 + roadSeg.getWidth() / 2);
            }
            Point2D ptSrcIni = new Point2D.Double(0.0, ty);
            Point2D ptSrcFin = new Point2D.Double(length, ty);
            Point2D.Double ptDstIni = new Point2D.Double(0.0, 0.0);
            Point2D.Double ptDstFin = new Point2D.Double(0.0, 0.0);
            affineTransform.transform(ptSrcIni, ptDstIni);
            affineTransform.transform(ptSrcFin, ptDstFin);
            Point2D[] pointArray = { ptDstIni, ptDstFin };
            return pointArray;
        } else {
            return null;
        }
    }

    public void paint(Graphics2D g2) {
        g2.setColor(colors[0]);
        g2.setStroke(new BasicStroke((float) 1));
        Line2D l;
        if (listOfNextRoadSegments != null) {
            for (RoadSegment roadSeg : listOfNextRoadSegments) {
                l = new Line2D.Double(xf, yf, roadSeg.getX0(), roadSeg.getY0());
                g2.draw(l);
            }
        }
        if (destination != null) {
            l = new Line2D.Double(xf, yf, destination.getX0(), destination.getY0());
            g2.draw(l);
        }
        g2.setStroke(new BasicStroke((float) 0.1));
        Rectangle2D.Double rect = new Rectangle2D.Double(0, 0 - width / 2, Point2D.distance(x0, y0, xf, yf), width);
        if ((showRuler) && (setOfConflictingRoads != null)) {
            g2.setColor(Color.RED);
            for (RoadSegment road : setOfConflictingRoads) {
                double finalX = (road.getX0() + road.getXf()) / 2;
                double finalY = (road.getY0() + road.getYf()) / 2;
                double initialx = (x0 + xf) / 2;
                double initialy = (y0 + yf) / 2;
                l = new Line2D.Double(initialx, initialy, finalX, finalY);
                Ellipse2D ellipse = new Ellipse2D.Double(finalX - 1, finalY - 1, 2.0, 2.0);
                g2.draw(l);
                g2.draw(ellipse);
            }
        }
        if (selected) {
            double size = width / 2;
            double center = size / 2;
            Rectangle2D rect1 = new Rectangle2D.Double(x0 - center, y0 - center, size, size);
            g2.fill(rect1);
            rect1.setFrame(xf - center, yf - center, size, size);
            g2.fill(rect1);
            g2.setColor(colors[1]);
        } else {
            g2.setColor(colors[0]);
        }
        AffineTransform originalTransform = g2.getTransform();
        g2.transform(affineTransform);
        g2.draw(rect);
        Line2D.Double l1 = new Line2D.Double(0, width / 2, width, 0);
        Line2D.Double l2 = new Line2D.Double(0, -width / 2, width, 0);
        g2.draw(l1);
        g2.draw(l2);
        if (showRuler) {
            Font font = new Font("Dialog", Font.PLAIN, 6);
            g2.setFont(font);
            for (int i = 100; i <= length; i += 100) {
                l1.setLine(i, 0 - width / 2 - 2, i, 0 - width / 2 + 2);
                g2.draw(l1);
                g2.drawString(Integer.toString(i), i, (int) (0 - width / 2 - 3));
            }
            for (int j = 10; j <= length; j += 10) {
                l1.setLine(j, 0 - width / 2 - 1, j, 0 - width / 2 + 1);
                g2.draw(l1);
            }
        }
        for (int i = listOfVehicles.size() - 1; i >= 0; i--) {
            Vehicle vehicle = listOfVehicles.get(i);
            if (this.equals(vehicle.getCurrentRoadSegment())) {
                vehicle.paint(g2);
            }
        }
        boolean show;
        g2.setStroke(new BasicStroke(1));
        for (IEntity entity : listOfEntities) {
            show = false;
            if (entity instanceof SignalHead) {
                g2.setColor(Color.black);
                show = true;
            } else {
                if (entity instanceof ReportMeasuresStart) {
                    g2.setColor(Color.gray);
                    show = true;
                } else {
                    if (entity instanceof Detector) {
                        g2.setColor(Color.green);
                        show = true;
                    }
                }
            }
            if (show) {
                l1.setLine(entity.getBackPosition(), 0, entity.getFrontPosition(), 0);
                g2.draw(l1);
                l1.setLine(entity.getBackPosition(), 0 - width / 2, entity.getBackPosition(), 0 + width / 2);
                g2.draw(l1);
                l1.setLine(entity.getFrontPosition(), 0 - width / 2, entity.getFrontPosition(), 0 + width / 2);
                g2.draw(l1);
            }
        }
        g2.setTransform(originalTransform);
    }

    public boolean contains(Point2D point) {
        try {
            Point2D ptDst = affineTransform.inverseTransform(new Point2D.Double(point.getX(), point.getY()), null);
            Rectangle2D rect = new Rectangle2D.Double(0, 0 - width / 2, Point2D.distance(x0, y0, xf, yf), width);
            if (rect.contains(ptDst)) {
                return true;
            } else {
                Rectangle2D rect1 = new Rectangle2D.Double(x0 - 1.5, y0 - 1.5, 3, 3);
                if (rect1.contains(point)) {
                    return true;
                }
                rect1.setFrame(xf - 1.5, yf - 1.5, 3, 3);
                if (rect1.contains(point)) {
                    return true;
                }
            }
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
	 * 
	 * @return AffineTransform New Instance of this class affine transform, so that it does not get modified
	 */
    public AffineTransform getAffineTransform() {
        AffineTransform at = new AffineTransform(affineTransform);
        return at;
    }

    public void openEditPanel() {
        if (editPanel == null) {
            editPanel = new RoadSegmentEditPanel(this, modelController);
        } else {
            editPanel.notifyChanges();
            editPanel.dispose();
            editPanel.setVisible(true);
            editPanel.toFront();
        }
    }

    /***********************************************************************
	 *                          Getters and setters                        *
	 ***********************************************************************/
    public Set<RoadSegment> getListOfConflictingRoads() {
        return setOfConflictingRoads;
    }

    public void removeConflictingRoad(RoadSegment road) {
        if (setOfConflictingRoads != null) {
            setOfConflictingRoads.remove(road);
            if (setOfConflictingRoads.isEmpty()) {
                setOfConflictingRoads = null;
            }
        }
    }

    /**
	 * Adds a conflicting road that will be used in this getSignal method
	 * 
	 * @param road
	 * @return true if <code>road</code> was added, false if it already existed
	 */
    public boolean addConflictiongRoad(RoadSegment road) {
        if (setOfConflictingRoads == null) {
            setOfConflictingRoads = new HashSet<RoadSegment>();
        }
        if (!setOfConflictingRoads.contains(road)) {
            setOfConflictingRoads.add(road);
            road.addConflictiongRoad(this);
            return true;
        } else {
            return false;
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
        if (editPanel != null) {
            editPanel.notifyChanges();
        }
    }

    public void addPreviousSegment(RoadSegment previousSegment) {
        if (listOfPreviousRoadSegments == null) {
            listOfPreviousRoadSegments = new ArrayList<RoadSegment>();
        }
        if (!listOfPreviousRoadSegments.contains(previousSegment)) {
            listOfPreviousRoadSegments.add(previousSegment);
        }
    }

    public void removePreviousSegment(RoadSegment previousSegment) {
        if (listOfPreviousRoadSegments != null) {
            listOfPreviousRoadSegments.remove(previousSegment);
            if (listOfPreviousRoadSegments.isEmpty()) {
                listOfPreviousRoadSegments = null;
            }
        }
    }

    public List<RoadSegment> getListOfPreviousSegments() {
        return listOfPreviousRoadSegments;
    }

    public List<RoadSegment> getListOfNextSegments() {
        return listOfNextRoadSegments;
    }

    public void removeNextSegment(RoadSegment nextSegment) {
        if (listOfNextRoadSegments != null) {
            listOfNextRoadSegments.remove(nextSegment);
            if (listOfNextRoadSegments.isEmpty()) {
                listOfNextRoadSegments = null;
            }
        }
    }

    public void setDestinationPossibility(Destination destination, int possibility) {
        if (mapOfDestPossibility == null) {
            mapOfDestPossibility = new HashMap<Destination, Integer>();
        }
        mapOfDestPossibility.put(destination, possibility);
    }

    public void removeDestinationPossibility(Destination destination) {
        if (mapOfDestPossibility != null) {
            mapOfDestPossibility.remove(destination);
        }
    }

    public List<Vehicle> getListOfVehicles() {
        return listOfVehicles;
    }

    @Override
    public double getBackPosition() {
        return 0;
    }

    @Override
    public double getFrontPosition() {
        return length;
    }

    public void notifyVehicleEntrance(Vehicle vehicle) {
        numOfVehicles++;
    }

    public void notifyVehicleExit(Vehicle vehicle) {
        numOfVehicles--;
    }

    public void remove(IEntity entity) {
        if (!listOfVehicles.remove(entity)) {
            listOfEntities.remove(entity);
        }
    }

    public RoadSegment getLeftLane() {
        return leftLane;
    }

    public void setLeftLane(RoadSegment leftLane) {
        this.leftLane = leftLane;
        if (leftLane != null) {
            leftLane.setRightLane(this);
        }
    }

    public RoadSegment getRightLane() {
        return rightLane;
    }

    public void setRightLane(RoadSegment rightLane) {
        this.rightLane = rightLane;
    }

    public double getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(double speedLimit) {
        this.speedLimit = speedLimit;
        if (editPanel != null) {
            editPanel.notifyChanges();
        }
    }

    public double getSpeed() {
        return 0;
    }

    public boolean isPhysical() {
        return false;
    }

    @Override
    public void setColor(Color color) {
        colors[0] = color;
    }

    @Override
    public void setSelectedColor(Color color) {
        colors[1] = color;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        modelController.repaint();
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
        if (editPanel != null) {
            editPanel.notifyChanges();
            modelController.repaint();
        }
    }

    public double getX0() {
        return x0;
    }

    public double getY0() {
        return y0;
    }

    public double getXf() {
        return xf;
    }

    public double getYf() {
        return yf;
    }

    public RoadSegmentEditPanel getEditPanel() {
        return editPanel;
    }

    public void setEditPanel(RoadSegmentEditPanel editPanel) {
        this.editPanel = editPanel;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public boolean isShowRuler() {
        return showRuler;
    }

    public void setShowRuler(boolean showRuler) {
        this.showRuler = showRuler;
    }

    /**
	 * To verify if this roadsegment is a turn.
	 * @return
	 */
    public boolean isATurn() {
        return isATurn;
    }

    public void setIsATurn(boolean isATurn) {
        this.isATurn = isATurn;
    }
}
