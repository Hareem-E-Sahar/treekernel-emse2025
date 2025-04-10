package fr.crnan.videso3d.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.sun.opengl.util.BufferUtil;
import fr.crnan.videso3d.Couple;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwindx.examples.util.DirectedPath;
import gov.nasa.worldwind.geom.Cylinder;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.util.RestorableSupport.StateObject;

/**
 * Route en 2D.<br />
 * Couleurs respectant le codage SIA
 * @author Bruno Spyckerelle
 * @version 0.3.2
 */
public class Route2D extends DirectedPath implements Route {

    private VidesoAnnotation annotation;

    private Space space;

    private Parity parity;

    private String name;

    private List<String> balises;

    private HashMap<Position, Couple<Position, Integer>> directionsMap = new HashMap<Position, Couple<Position, Integer>>();

    private List<Integer> directions;

    private boolean locationsVisible = false;

    public Route2D() {
        super();
        this.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        this.setMaxScreenSize(9.0);
        this.setArrowLength(40000);
        this.setFollowTerrain(true);
        this.setAttributes(new BasicShapeAttributes());
        BasicShapeAttributes attrs = (BasicShapeAttributes) this.getAttributes();
        attrs.setOutlineMaterial(Material.WHITE);
        attrs.setOutlineWidth(2.0);
        this.setHighlightAttributes(attrs);
    }

    public Route2D(String name, Space s) {
        this();
        this.setAnnotation("Route " + name);
        this.setSpace(s);
        this.setName(name);
    }

    /**
	 * Affecte la couleur de la route suivant le codage SIA
	 * @param name Nom de la route
	 * @param type {@link Espace} de la route
	 */
    private void setColor(String name) {
        BasicShapeAttributes attrs = new BasicShapeAttributes();
        switch(space) {
            case FIR:
                Character c = name.charAt(0);
                switch(c) {
                    case 'A':
                        attrs.setOutlineMaterial(Material.YELLOW);
                        break;
                    case 'G':
                        attrs.setOutlineMaterial(Material.GREEN);
                        break;
                    case 'B':
                        attrs.setOutlineMaterial(Material.BLUE);
                        break;
                    case 'R':
                        attrs.setOutlineMaterial(Material.RED);
                        break;
                    default:
                        attrs.setOutlineMaterial(Material.BLACK);
                        break;
                }
                break;
            case UIR:
                if (parity != null) {
                    switch(parity) {
                        case RED:
                            attrs.setOutlineMaterial(Material.RED);
                            break;
                        case GREEN:
                            attrs.setOutlineMaterial(Material.GREEN);
                            break;
                        case BLUE:
                            attrs.setOutlineMaterial(Material.BLUE);
                            break;
                    }
                } else {
                    attrs.setOutlineMaterial(Material.BLACK);
                }
                break;
            default:
                break;
        }
        attrs.setEnableAntialiasing(true);
        attrs.setDrawInterior(false);
        attrs.setOutlineWidth(1.0);
        this.setAttributes(attrs);
    }

    @Override
    public VidesoAnnotation getAnnotation(Position pos) {
        if (annotation == null) this.setAnnotation("Route " + this.name);
        annotation.setPosition(pos);
        return annotation;
    }

    @Override
    public void setAnnotation(String text) {
        if (annotation == null) {
            annotation = new VidesoAnnotation(text);
        } else {
            annotation.setText(text);
        }
    }

    @Override
    public void setSpace(Space s) {
        Space temp = this.space;
        this.space = s;
        if (this.space != temp && this.name != null) this.setColor(this.name);
    }

    @Override
    public Space getSpace() {
        return this.space;
    }

    public void setName(String name) {
        String temp = this.name;
        this.name = name;
        if (this.name != temp && this.space != null) this.setColor(this.name);
    }

    @Override
    public void setParity(Parity p) {
        Parity temp = this.parity;
        this.parity = p;
        if (this.parity != temp && this.name != null) this.setColor(this.name);
    }

    @Override
    public Parity getParity() {
        return this.parity;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setBalises(List<String> balises) {
        this.balises = balises;
    }

    public void addBalise(String balise) {
        if (this.balises == null) {
            this.balises = new LinkedList<String>();
        }
        this.balises.add(balise);
    }

    @Override
    public List<String> getBalises() {
        return this.balises;
    }

    @Override
    public Iterable<? extends LatLon> getLocations() {
        return this.getPositions();
    }

    public void setLocations(Iterable<? extends LatLon> locations) {
        List<Position> positions = new ArrayList<Position>();
        for (LatLon loc : locations) {
            positions.add(new Position(loc, 0));
        }
        this.setPositions(positions);
    }

    /**
	 * <b>Warning !</b> locations.size() == directions.size() -1
	 * @param locations
	 * @param directions
	 */
    public void setLocations(Iterable<? extends LatLon> locations, List<Integer> directions) {
        List<Position> positions = new ArrayList<Position>();
        Position first = null;
        Iterator<Integer> direction = directions.iterator();
        for (LatLon loc : locations) {
            Position temp = new Position(loc, 0);
            positions.add(temp);
            if (first != null) {
                directionsMap.put(first, new Couple<Position, Integer>(temp, direction.next()));
            }
            first = temp;
        }
        this.setPositions(positions);
        this.directions = directions;
    }

    @Override
    protected boolean isSegmentVisible(DrawContext dc, Position posA, Position posB, Vec4 ptA, Vec4 ptB) {
        if (directionsMap.containsKey(posA)) {
            if (directionsMap.get(posA).getFirst().equals(posB)) {
                if (directionsMap.get(posA).getSecond() == LEG_FORBIDDEN) {
                    return false;
                }
            }
        }
        if (this.directionsMap.containsKey(posB)) {
            if (directionsMap.get(posB).getFirst().equals(posA)) {
                if (directionsMap.get(posB).getSecond() == LEG_FORBIDDEN) {
                    return false;
                }
            }
        }
        Frustum f = dc.getView().getFrustumInModelCoordinates();
        if (f.contains(ptA)) return true;
        if (f.contains(ptB)) return true;
        if (ptA.equals(ptB)) return false;
        Position posC = Position.interpolateRhumb(0.5, posA, posB);
        Vec4 ptC = this.computePoint(dc.getTerrain(), posC);
        if (f.contains(ptC)) return true;
        double r = Line.distanceToSegment(ptA, ptB, ptC);
        Cylinder cyl = new Cylinder(ptA, ptB, r == 0 ? 1 : r);
        return cyl.intersects(dc.getView().getFrustumInModelCoordinates());
    }

    @Override
    protected void doDrawOutline(DrawContext dc) {
        if (this.directions != null) {
            this.computeDirectionArrows(dc, this.getCurrentPathData());
            this.drawDirectionArrows(dc, this.getCurrentPathData());
        }
        super.doDrawOutline(dc);
    }

    @Override
    protected void computeDirectionArrows(DrawContext dc, PathData pathData) {
        IntBuffer polePositions = pathData.getPolePositions();
        int numPositions = polePositions.limit() / 2;
        List<Position> tessellatedPositions = pathData.getTessellatedPositions();
        final int FLOATS_PER_ARROWHEAD = 9;
        FloatBuffer buffer = (FloatBuffer) pathData.getValue(ARROWS_KEY);
        if (buffer == null || buffer.capacity() < numPositions * FLOATS_PER_ARROWHEAD) buffer = BufferUtil.newFloatBuffer(FLOATS_PER_ARROWHEAD * numPositions);
        pathData.setValue(ARROWS_KEY, buffer);
        buffer.clear();
        Terrain terrain = dc.getTerrain();
        double arrowBase = this.getArrowLength() * this.getArrowAngle().tanHalfAngle();
        int thisPole = polePositions.get(0) / 2;
        Position poleA = tessellatedPositions.get(thisPole);
        Vec4 polePtA = this.computePoint(terrain, poleA);
        int num = 0;
        for (int i = 2; i < polePositions.limit(); i += 2) {
            int nextPole = polePositions.get(i) / 2;
            Position poleB = tessellatedPositions.get(nextPole);
            Vec4 polePtB = this.computePoint(terrain, poleB);
            int midPoint = (thisPole + nextPole) / 2;
            Position posA = tessellatedPositions.get(midPoint);
            Position posB = tessellatedPositions.get(midPoint + 1);
            Vec4 ptA = this.computePoint(terrain, posA);
            Vec4 ptB = this.computePoint(terrain, posB);
            if (this.directions != null) {
                if (this.directions.get(num) != LEG_AUTHORIZED && this.directions.get(num) != LEG_FORBIDDEN) {
                    this.computeArrowheadGeometry(dc, polePtA, polePtB, ptA, ptB, this.getArrowLength(), arrowBase, buffer, pathData, this.directions.get(num));
                }
            }
            thisPole = nextPole;
            polePtA = polePtB;
            num++;
        }
    }

    protected void computeArrowheadGeometry(DrawContext dc, Vec4 polePtA, Vec4 polePtB, Vec4 ptA, Vec4 ptB, double arrowLength, double arrowBase, FloatBuffer buffer, PathData pathData, int direction) {
        double poleDistance = polePtA.distanceTo3(polePtB);
        Vec4 parallel = ptA.subtract3(ptB);
        Vec4 surfaceNormal = dc.getGlobe().computeSurfaceNormalAtPoint(ptB);
        Vec4 perpendicular = surfaceNormal.cross3(parallel);
        Vec4 midPoint = ptA.add3(ptB).divide3(2.0);
        if (!this.isArrowheadSmall(dc, midPoint, 1)) {
            View view = dc.getView();
            double midpointDistance = view.getEyePoint().distanceTo3(midPoint);
            double pixelSize = view.computePixelSizeAtDistance(midpointDistance);
            if (arrowLength / pixelSize > this.maxScreenSize) {
                arrowLength = this.maxScreenSize * pixelSize;
                arrowBase = arrowLength * this.getArrowAngle().tanHalfAngle();
            }
            if (poleDistance <= arrowLength) return;
            perpendicular = perpendicular.normalize3().multiply3(arrowBase);
            parallel = parallel.normalize3().multiply3(arrowLength);
            if (poleDistance > arrowLength) midPoint = midPoint.subtract3(parallel.divide3(2.0));
            Vec4 vertex1;
            Vec4 vertex2;
            if (direction == LEG_DIRECT) {
                vertex1 = midPoint.add3(parallel).add3(perpendicular);
                vertex2 = midPoint.add3(parallel).add3(perpendicular.multiply3(-1.0));
            } else {
                vertex1 = midPoint.add3(perpendicular);
                vertex2 = midPoint.add3(perpendicular.multiply3(-1.0));
                midPoint = midPoint.add3(parallel);
            }
            Vec4 referencePoint = pathData.getReferencePoint();
            buffer.put((float) (vertex1.x - referencePoint.x));
            buffer.put((float) (vertex1.y - referencePoint.y));
            buffer.put((float) (vertex1.z - referencePoint.z));
            buffer.put((float) (vertex2.x - referencePoint.x));
            buffer.put((float) (vertex2.y - referencePoint.y));
            buffer.put((float) (vertex2.z - referencePoint.z));
            buffer.put((float) (midPoint.x - referencePoint.x));
            buffer.put((float) (midPoint.y - referencePoint.y));
            buffer.put((float) (midPoint.z - referencePoint.z));
        }
    }

    @Override
    public Object getNormalAttributes() {
        return this.getAttributes();
    }

    @Override
    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject so) {
        super.doGetRestorableState(rs, so);
        if (this.getPositions() != null) {
            rs.addStateValueAsLatLonList(so, "locations", this.getPositions());
        }
        if (this.directions != null) {
            RestorableSupport.StateObject stateObject = rs.addStateObject(so, "directions");
            if (stateObject != null) {
                for (Integer d : this.directions) {
                    rs.addStateValueAsInteger(stateObject, "direction", d);
                }
            }
        }
        if (this.getName() != null) rs.addStateValueAsString(so, "name", this.getName());
        if (this.annotation != null) rs.addStateValueAsString(so, "annotation", this.annotation.getText(), true);
        if (this.getSpace() != null) rs.addStateValueAsString(so, "space", this.getSpace().toString());
        if (this.getParity() != null) rs.addStateValueAsString(so, "parity", this.getParity().toString());
    }

    @Override
    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context) {
        super.doRestoreState(rs, context);
        String s = rs.getStateValueAsString(context, "space");
        if (s != null) {
            if (s.equals(Space.FIR.toString())) {
                this.setSpace(Space.FIR);
            } else if (s.equals(Space.UIR.toString())) {
                this.setSpace(Space.UIR);
            }
        }
        s = rs.getStateValueAsString(context, "parity");
        if (s != null) {
            if (s.equals(Parity.RED.toString())) {
                this.setParity(Parity.RED);
            } else if (s.equals(Parity.GREEN.toString())) {
                this.setParity(Parity.GREEN);
            } else if (s.equals(Parity.BLUE.toString())) {
                this.setParity(Parity.BLUE);
            }
        }
        List<LatLon> loc = rs.getStateValueAsLatLonList(context, "locations");
        if (loc != null) {
            StateObject directions = rs.getStateObject(context, "directions");
            if (directions != null) {
                RestorableSupport.StateObject[] dsos = rs.getAllStateObjects(directions, "direction");
                if (dsos != null && dsos.length != 0) {
                    List<Integer> directionsList = new ArrayList<Integer>(dsos.length);
                    for (RestorableSupport.StateObject dso : dsos) {
                        if (dso != null) {
                            Integer d = rs.getStateObjectAsInteger(dso);
                            if (d != null) directionsList.add(d);
                        }
                    }
                    this.setLocations(loc, directionsList);
                }
            } else {
                this.setLocations(loc);
            }
        }
        s = rs.getStateValueAsString(context, "name");
        if (s != null) this.setName(s);
        s = rs.getStateValueAsString(context, "annotation");
        if (s != null) this.setAnnotation(s);
    }

    @Override
    public boolean areLocationsVisible() {
        return locationsVisible;
    }

    /**
	 * L'affichage des coordonnées est géré au niveau du contrôleur.
	 */
    @Override
    public void setLocationsVisible(boolean visible) {
        locationsVisible = visible;
    }
}
