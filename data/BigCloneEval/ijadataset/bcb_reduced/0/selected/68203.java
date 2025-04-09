package fr.ign.cogit.geoxygene.spatial.coordgeom;

import org.apache.log4j.Logger;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Ring;

/**
 * Rectangle englobant minimum en 2D, ou pave englobant minimium en 3D. Un
 * GM_envelope est parallele aux axes.
 * 
 * @author Thierry Badard
 * @author Arnaud Braun
 * @version 1.0
 * 
 */
public class GM_Envelope implements IEnvelope {

    protected static final Logger logger = Logger.getLogger(GM_Envelope.class.getName());

    /** Coin superieur : Xmax, Ymax, (Zmax). */
    protected IDirectPosition upperCorner;

    @Override
    public void setUpperCorner(IDirectPosition UpperCorner) {
        this.upperCorner = (IDirectPosition) UpperCorner.clone();
    }

    @Override
    public IDirectPosition getUpperCorner() {
        return this.upperCorner;
    }

    /** Coin inferieur : Xmin, Ymin, (Zmin). */
    protected IDirectPosition lowerCorner;

    @Override
    public void setLowerCorner(IDirectPosition LowerCorner) {
        this.lowerCorner = (IDirectPosition) LowerCorner.clone();
    }

    @Override
    public IDirectPosition getLowerCorner() {
        return this.lowerCorner;
    }

    /** Constructeur par defaut (initialise des points 3D par defaut). */
    public GM_Envelope() {
        this.upperCorner = new DirectPosition();
        this.lowerCorner = new DirectPosition();
    }

    /** Constructeur a partir des 2 coins. Attention a l'ordre des points. */
    public GM_Envelope(IDirectPosition UpperCorner, IDirectPosition LowerCorner) {
        this.upperCorner = UpperCorner;
        this.lowerCorner = LowerCorner;
    }

    /** Constructeur a partir de coordonnees (2D). */
    public GM_Envelope(double Xmin, double Xmax, double Ymin, double Ymax) {
        this.upperCorner = new DirectPosition(Xmax, Ymax);
        this.lowerCorner = new DirectPosition(Xmin, Ymin);
    }

    /** Constructeur a partir de coordonnees (3D). */
    public GM_Envelope(double Xmin, double Xmax, double Ymin, double Ymax, double Zmin, double Zmax) {
        this.upperCorner = new DirectPosition(Xmax, Ymax, Zmax);
        this.lowerCorner = new DirectPosition(Xmin, Ymin, Zmin);
    }

    /** Construit un carre dont P est le centre, de cote d. */
    public GM_Envelope(IDirectPosition P, double d) {
        double c = d / 2;
        this.upperCorner = new DirectPosition(P.getX() + c, P.getY() + c, P.getZ() + c);
        this.lowerCorner = new DirectPosition(P.getX() - c, P.getY() - c, P.getZ() - c);
    }

    @Override
    public int getDimension() {
        if (this.upperCorner.getDimension() == this.lowerCorner.getDimension()) {
            return this.upperCorner.getDimension();
        }
        System.out.println("GM_Enveloppe::getDimension() : Les points upperCorner et lowerCorner n'ont pas la même dimension.");
        return 0;
    }

    @Override
    public double width() {
        return this.upperCorner.getX() - this.lowerCorner.getX();
    }

    @Override
    public double length() {
        return this.upperCorner.getY() - this.lowerCorner.getY();
    }

    @Override
    public double height() {
        return this.upperCorner.getZ() - this.lowerCorner.getZ();
    }

    @Override
    public double maxX() {
        return this.upperCorner.getX();
    }

    @Override
    public double minX() {
        return this.lowerCorner.getX();
    }

    @Override
    public double maxY() {
        return this.upperCorner.getY();
    }

    @Override
    public double minY() {
        return this.lowerCorner.getY();
    }

    @Override
    public double maxZ() {
        return this.upperCorner.getZ();
    }

    @Override
    public double minZ() {
        return this.lowerCorner.getZ();
    }

    @Override
    public IDirectPosition center() {
        int n = this.getDimension();
        DirectPosition result = new DirectPosition();
        for (int i = 0; i < n; i++) {
            double theMin = this.lowerCorner.getCoordinate(i);
            double theMax = this.upperCorner.getCoordinate(i);
            double val = theMin + (theMax - theMin) / 2;
            if (!Double.isNaN(val)) {
                result.setCoordinate(i, val);
                if (GM_Envelope.logger.isTraceEnabled()) {
                    GM_Envelope.logger.trace("Center " + i + " " + theMin + " " + theMax + " = " + (theMin + (theMax - theMin) / 2));
                }
            }
        }
        return result;
    }

    @Override
    public boolean contains(IDirectPosition dp) {
        return this.contains(dp.getX(), dp.getY());
    }

    @Override
    public boolean contains(IPoint point) {
        return this.contains(point.getPosition());
    }

    @Override
    public boolean contains(double x, double y) {
        if (x < this.lowerCorner.getX()) {
            return false;
        } else if (x > this.upperCorner.getX()) {
            return false;
        } else if (y < this.lowerCorner.getY()) {
            return false;
        } else if (y > this.upperCorner.getY()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean contains(double x, double y, double z) {
        double Xmin = this.lowerCorner.getX();
        double Xmax = this.upperCorner.getX();
        double Ymin = this.lowerCorner.getY();
        double Ymax = this.upperCorner.getY();
        double Zmin = this.lowerCorner.getZ();
        double Zmax = this.upperCorner.getZ();
        return !((x < Xmin) || (x > Xmax) || (y < Ymin) || (y > Ymax) || (z < Zmin) || (z > Zmax));
    }

    @Override
    public boolean contains(IEnvelope env) {
        return env != null && this.contains(env.getLowerCorner()) && this.contains(env.getUpperCorner());
    }

    @Override
    public boolean overlaps(IEnvelope env) {
        if (this.getUpperCorner().getX() < env.getLowerCorner().getX()) {
            return false;
        }
        if (this.getLowerCorner().getX() > env.getUpperCorner().getX()) {
            return false;
        }
        if (this.getUpperCorner().getY() < env.getLowerCorner().getY()) {
            return false;
        }
        if (this.getLowerCorner().getY() > env.getUpperCorner().getY()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean intersects(IEnvelope env) {
        return !(env.minX() > this.maxX() || env.maxX() < this.minX() || env.minY() > this.maxY() || env.maxY() < this.minY());
    }

    @Override
    public void expand(IDirectPosition thePoint) {
        if (!this.contains(thePoint)) {
            int n = this.getDimension();
            for (int i = 0; i < n; i++) {
                double theCoord = thePoint.getCoordinate(i);
                double theMin = this.lowerCorner.getCoordinate(i);
                double theMax = this.upperCorner.getCoordinate(i);
                if (theCoord > theMax) {
                    this.upperCorner.setCoordinate(i, theCoord);
                } else if (theCoord < theMin) {
                    this.lowerCorner.setCoordinate(i, theCoord);
                }
            }
        }
    }

    @Override
    public void expand(double x, double y) {
        if (!this.contains(x, y)) {
            double Xmin = this.lowerCorner.getX();
            double Xmax = this.upperCorner.getX();
            double Ymin = this.lowerCorner.getY();
            double Ymax = this.upperCorner.getY();
            if (y > Ymax) {
                this.upperCorner.setY(y);
            } else if (y < Ymin) {
                this.lowerCorner.setY(y);
            }
            if (x > Xmax) {
                this.upperCorner.setX(x);
            } else if (x < Xmin) {
                this.lowerCorner.setX(x);
            }
        }
    }

    @Override
    public void expand(double x, double y, double z) {
        if (!this.contains(x, y, z)) {
            double Xmin = this.lowerCorner.getX();
            double Xmax = this.upperCorner.getX();
            double Ymin = this.lowerCorner.getY();
            double Ymax = this.upperCorner.getY();
            double Zmin = this.lowerCorner.getZ();
            double Zmax = this.upperCorner.getZ();
            if (z > Zmax) {
                this.upperCorner.setZ(z);
            } else if (z < Zmin) {
                this.lowerCorner.setZ(z);
            }
            if (y > Ymax) {
                this.upperCorner.setY(y);
            } else if (y < Ymin) {
                this.lowerCorner.setY(y);
            }
            if (x > Xmax) {
                this.upperCorner.setX(x);
            } else if (x < Xmin) {
                this.lowerCorner.setX(x);
            }
        }
    }

    @Override
    public void expand(IEnvelope env) {
        if (env != null && !this.contains(env)) {
            this.expand(env.getUpperCorner());
            this.expand(env.getLowerCorner());
        }
    }

    @Override
    public void expandBy(double h) {
        IDirectPosition theCenter = this.center();
        int n = this.getDimension();
        for (int i = 0; i < n; i++) {
            double center = theCenter.getCoordinate(i);
            double delta = this.upperCorner.getCoordinate(i) - center;
            this.upperCorner.setCoordinate(i, center + h * delta);
            this.lowerCorner.setCoordinate(i, center - h * delta);
        }
    }

    @Override
    public void expandBy(double w, double l) {
        IDirectPosition theCenter = this.center();
        double centerX = theCenter.getX();
        double deltaX = this.maxX() - centerX;
        this.upperCorner.setX(centerX + w * deltaX);
        this.lowerCorner.setX(centerX - w * deltaX);
        double centerY = theCenter.getY();
        double deltaY = this.maxY() - centerY;
        this.upperCorner.setY(centerY + l * deltaY);
        this.lowerCorner.setY(centerY - l * deltaY);
    }

    @Override
    public void expandBy(double w, double l, double h) {
        IDirectPosition theCenter = this.center();
        double centerX = theCenter.getX();
        double deltaX = this.maxX() - centerX;
        this.upperCorner.setX(centerX + w * deltaX);
        this.lowerCorner.setX(centerX - w * deltaX);
        double centerY = theCenter.getY();
        double deltaY = this.maxY() - centerY;
        this.upperCorner.setY(centerY + l * deltaY);
        this.lowerCorner.setY(centerY - l * deltaY);
        double centerZ = theCenter.getZ();
        double deltaZ = this.maxZ() - centerZ;
        this.upperCorner.setZ(centerZ + h * deltaZ);
        this.lowerCorner.setZ(centerZ - h * deltaZ);
    }

    @Override
    public IPolygon getGeom() {
        DirectPositionList coords = new DirectPositionList();
        coords.add(new DirectPosition(this.minX(), this.minY()));
        coords.add(new DirectPosition(this.minX(), this.maxY()));
        coords.add(new DirectPosition(this.maxX(), this.maxY()));
        coords.add(new DirectPosition(this.maxX(), this.minY()));
        coords.add(coords.get(0));
        return new GM_Polygon(new GM_Ring(new GM_LineString(coords)));
    }

    @Override
    public boolean isEmpty() {
        int n = this.getDimension();
        for (int i = 0; i < n; i++) {
            double theMin = this.lowerCorner.getCoordinate(i);
            double theMax = this.upperCorner.getCoordinate(i);
            if (theMin > theMax) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object clone() {
        DirectPosition up = (DirectPosition) this.upperCorner.clone();
        DirectPosition low = (DirectPosition) this.lowerCorner.clone();
        return new GM_Envelope(up, low);
    }

    @Override
    public String toString() {
        return this.samplePoint().toString();
    }

    @Override
    public IDirectPositionList samplePoint() {
        DirectPositionList dpl = new DirectPositionList();
        DirectPosition dp;
        dpl.add(this.lowerCorner);
        if (!Double.isNaN(this.lowerCorner.getZ())) {
            dp = new DirectPosition(this.upperCorner.getX(), this.lowerCorner.getY(), this.lowerCorner.getZ());
        } else {
            dp = new DirectPosition(this.upperCorner.getX(), this.lowerCorner.getY());
        }
        dpl.add(dp);
        dpl.add(this.upperCorner);
        if (!Double.isNaN(this.upperCorner.getZ())) {
            dp = new DirectPosition(this.lowerCorner.getX(), this.upperCorner.getY(), this.upperCorner.getZ());
        } else {
            dp = new DirectPosition(this.lowerCorner.getX(), this.upperCorner.getY());
        }
        dpl.add(dp);
        return dpl;
    }

    @Override
    public boolean equals(Object env) {
        if (!(env instanceof GM_Envelope)) {
            return false;
        }
        GM_Envelope envelope = (GM_Envelope) env;
        return this.lowerCorner.equals(envelope.getLowerCorner()) && this.upperCorner.equals(envelope.getUpperCorner());
    }
}
