import java.awt.*;
import java.util.*;

public class IEFProteinSwingVersion extends Component {

    private static Color[] colors = { Color.blue, Color.green, Color.yellow, Color.red, Color.orange, Color.pink };

    private Color myColor;

    private Vector proteins;

    private int myX;

    private static int myY = 5;

    private double tempX = 0;

    private double increments = 0;

    private double myPi;

    private double maxPi;

    private double minPi;

    private static int myHeight = 40;

    private static double myWidth = 0;

    private static double tempWidth = 0;

    private Vector names;

    private static double maxpH = 10;

    private static double minpH = 3;

    private static GelCanvasSwingVersion gelcanvas;

    /**
     * constructor - creates the IEFProtein object
     *
     * @param p the first protein to be represented by this object
     */
    public IEFProteinSwingVersion(E2DProtein p, GelCanvasSwingVersion g) {
        names = new Vector();
        gelcanvas = g;
        proteins = new Vector();
        proteins.add(p);
        myPi = p.getPI();
        minPi = p.getPI();
        maxPi = minPi;
        if (myPi < minpH) {
            maxPi = minpH;
        }
        names.add(p.toString());
        myColor = colors[0];
        double w = gelcanvas.getWidth();
        if (myPi <= ((double) minpH)) {
            myX = 1;
        } else if (myPi >= maxpH) {
            myX = (int) (w - 6);
        } else {
            myX = (int) ((w - 4) * (((myPi - minpH) / (maxpH - minpH))));
        }
        Random rand = new Random();
        tempX = rand.nextInt((int) w - 7);
        tempX = tempX + 1;
        increments = (myX - tempX) / 50;
    }

    /**
     * addProtein adds a collection of proteins c to the vector of proteins
     * being represented by this IEFProtein object
     *
     * @param c the collection of proteins to be added to the vector
     */
    public void addProtein(Collection c) {
        proteins.addAll((Vector) (c));
        if (proteins.size() < 10) {
            myColor = colors[0];
        } else if (proteins.size() < 20) {
            myColor = colors[1];
        } else if (proteins.size() < 30) {
            myColor = colors[2];
        } else if (proteins.size() < 40) {
            myColor = colors[3];
        } else if (proteins.size() < 60) {
            myColor = colors[4];
        } else {
            myColor = colors[5];
        }
        this.repaint();
        E2DProtein p = null;
        double pi = 0;
        for (int i = 0; i < ((Vector) c).size(); i++) {
            names.add(((E2DProtein) ((Vector) c).elementAt(i)).toString());
            p = ((E2DProtein) ((Vector) c).elementAt(i));
            pi = p.getPI();
            if (pi > maxPi) {
                maxPi = pi;
            } else if (pi < minPi) {
                minPi = pi;
            }
        }
        myPi = (maxPi + minPi) / 2;
    }

    /**
     * accessor method
     *
     * @return the X coordinate of the protein
     */
    public int returnX() {
        return myX;
    }

    /**
     * accessor method
     *
     * @return the Y coordinate of the protein
     */
    public int returnY() {
        return myY;
    }

    /**
     * accessor method
     *
     * @return the maximum pI value
     */
    public double getMaxPI() {
        return maxPi;
    }

    /**
     * accessor method
     *
     * @return the minimum pI value
     */
    public double getMinPI() {
        return minPi;
    }

    /**
     * getProtein returns the protein object(s) that this IEFProtein
     * is representing.
     *
     * @return proteins
     */
    public Vector getProtein() {
        return proteins;
    }

    /**
     * changeWidth increments the width of the IEFProtein for the initial
     * animation.
     */
    public static void changeWidth() {
        tempWidth = tempWidth + myWidth / 50;
    }

    /**
     * changes the position of the object a 50th of the distance from its
     * final position.
     */
    public void changeX() {
        tempX = tempX + increments;
    }

    /**
     * Sets the tempX equal to the final X value for the end of the animation
     * of the IEF process
     */
    public void setX() {
        tempX = myX;
    }

    /**
     * Sets the tempWidth equal to the final width for the end of the
     * animation of the IEF process
     */
    public static void setWidth() {
        tempWidth = myWidth;
    }

    /**
     * draw displays the IEFProtein as a rectangle on the screen
     */
    public void draw(Graphics g) {
        myWidth = 563 / 100;
        g.setColor(myColor);
        g.fillRect((int) tempX, myY, (int) tempWidth, myHeight);
    }

    /**
     * decreases the height of the protein for the beginning of the SDS-PAGE
     * animation
     */
    public static void shrinkProtein() {
        myHeight = myHeight - 2;
        myY = myY + 2;
    }

    /**
     * accessor method
     *
     * @return current height of the object
     */
    public static int returnHeight() {
        return myHeight;
    }

    /**
     * accessor method
     *
     * @return final width of this object
     */
    public static double returnWidth() {
        return myWidth;
    }

    /**
     * accessor method
     *
     * @return current width of this object
     */
    public static double returnTempWidth() {
        return tempWidth;
    }

    /**
     * This method is called when the reset button is pressed.  It sets the
     * tempWidth data member back to zero to prepare to draw the IEFProteins
     * again
     */
    public static void resetTempWidth() {
        tempWidth = 0;
    }

    /**
     * This method is called when the reset button is pressed.  It sets the
     * height and y positions back to their original values
     */
    public static void resetProtein() {
        myHeight = 40;
        myY = 5;
    }

    /**
     * accessor method
     *
     * @return vector of protein titles
     */
    public Vector getNames() {
        return names;
    }

    /**
     * sets maxpH equal to the maximum pH value and minpH to the minimum
     * pH value entered by the user for the IEF animation
     */
    public static void setRange(double max, double min) {
        maxpH = max;
        minpH = min;
        myWidth = 563 / 100;
    }
}
