package net.richarddawkins.arthromorphs.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JPanel;
import net.richarddawkins.arthromorphs.Animal;
import net.richarddawkins.arthromorphs.Atom;
import net.richarddawkins.arthromorphs.Embryology;
import net.richarddawkins.arthromorphs.ErrorAlert;
import net.richarddawkins.arthromorphs.PreferenceParams;

public class AnimalJPanel extends JPanel implements Embryology, MouseListener {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private Animal animal;

    public Animal getAnimal() {
        return animal;
    }

    public AnimalJPanel() {
        this.addMouseListener(this);
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
        repaint();
    }

    float gradientFactor;

    int eastPole;

    int northPole;

    int southPole;

    int westPole;

    public boolean hidePen = true;

    int midriff;

    public int horizontalOffset = 0;

    public int oldHorizontalOffset = 0;

    public int oldVerticalOffset = 0;

    public int thickscale = 1;

    public int verticalOffset = 0;

    public float overlap = 1.0f;

    public static final int CUM_PARAMS_SIZE = 9;

    protected static final int[] paramOffset = new int[] { -1, 0, 3, 6, 0, 3, 6, 0, 3, 6, 0, 3, 6 };

    public static final int PARAM_SEG_HEIGHT = 0;

    public static final int PARAM_SEG_WIDTH = 1;

    public static final int PARAM_NOT_USED = 2;

    public static final int PARAM_JOINT_THICKNESS = 3;

    public static final int PARAM_JOINT_LENGTH = 4;

    public static final int PARAM_JOINT_ANGLE = 5;

    public static final int PARAM_CLAW_THICKNESS = 6;

    public static final int PARAM_CLAW_LENGTH = 7;

    public static final int PARAM_CLAW_ANGLE_BETWEEN_PINCERS = 8;

    protected void recalcBounds(int endX, int endY) {
        if (endY < northPole) northPole = endY;
        if (endY > southPole) southPole = endY;
        if (endX < westPole) westPole = endX;
        if (endX > eastPole) eastPole = endX;
    }

    protected void drawLine(Graphics2D g, int x, int y, int endX, int endY, int thick) {
        if (hidePen) {
            recalcBounds(endX, endY);
            return;
        }
        g.setStroke(new BasicStroke((float) thick));
        g.drawLine(x - thick / 2, y - thick / 2, endX - thick / 2, endY - thick / 2);
    }

    protected void drawOval(Graphics2D g, int x, int y, int width, int height) {
        Rectangle r = new Rectangle(x, y, width, height);
        if (hidePen) {
            recalcBounds(x, y);
            recalcBounds(x + width, y + height);
            return;
        }
        PreferenceParams prefs = PreferenceParams.getInstance();
        if (prefs.wantColor) {
            g.setColor(Color.GREEN);
            g.fillOval(r.x, r.y, r.width, r.height);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillOval(r.x, r.y, r.width, r.height);
        }
        g.setStroke(new BasicStroke(2.0f));
        g.setColor(Color.BLACK);
        g.drawOval(r.x, r.y, r.width, r.height);
    }

    protected void drawSeg(Graphics2D g, int x, int y, float width, float height) {
        int halfW = Math.round(width / 2);
        drawOval(g, x - halfW, y, Math.round(width), Math.round(height));
    }

    protected void drawClaw(Graphics2D g, float[] params, int x, int y, int xCenter) {
        int oldX, oldY, leftOldX, leftX, thick;
        float ang;
        g.setColor(Color.RED);
        oldX = x;
        oldY = y;
        ang = params[PARAM_CLAW_ANGLE_BETWEEN_PINCERS] / 2.0f;
        x = (int) Math.round(x + params[PARAM_CLAW_LENGTH] * Math.sin(ang));
        y = (int) Math.round(y + params[PARAM_CLAW_LENGTH] * Math.cos(ang));
        thick = (int) (1 + Math.floor(Math.abs(params[PARAM_CLAW_THICKNESS])));
        drawLine(g, oldX, oldY, x, y, thick);
        leftX = xCenter - (x - xCenter);
        leftOldX = xCenter - (oldX - xCenter);
        drawLine(g, leftOldX, oldY, leftX, y, thick);
        y = (int) Math.round(y - 2.0 * params[PARAM_CLAW_LENGTH] * Math.cos(ang));
        drawLine(g, oldX, oldY, x, y, thick);
        drawLine(g, leftOldX, oldY, leftX, y, thick);
    }

    /** 
	 * starting at the atom "which", multiply its numbers into the array of params.
	 * At the bottom, draw the part starting at x,y
	 * params accumulates the final Joint width, Claw angle, etc.
     * params: 0 Seg height, 1 Seg width, 2 (not used), 3 Joint thickness, 4 Joint length, 5 Joint angle,
     * 6 Claw thickness, 7 Claw length, 8 Claw angle between pincers
     * x,y are current local point, xCenter is the centerline of the animal (left and right Joints need this)
     */
    protected void draw(Graphics2D g, Atom which, float[] params, int x, int y, int xCenter, int ySeg) {
        int oldX = 0;
        int oldY = 0;
        int leftOldX, leftX, offset, thick;
        float ang;
        float jointscale = 0.5f;
        float[] myPars = params.clone();
        Atom currentAtom = which;
        if (currentAtom.getKind() == Atom.ATOM_ANIMALTRUNK) {
            gradientFactor = currentAtom.getGradient();
            if (gradientFactor > 1000) {
                ErrorAlert.errorAlert("AnimalDraw reports gradientFactor exceeds 1000.");
            }
        }
        offset = paramOffset[currentAtom.getKind()];
        params[offset] = params[offset] * currentAtom.getHeight();
        params[offset + 1] = params[offset + 1] * currentAtom.getWidth();
        params[offset + 2] = params[offset + 2] * currentAtom.getAngle();
        if (currentAtom.getKind() == Atom.ATOM_SECTIONTRUNK) overlap = currentAtom.getAngle();
        if (currentAtom.getKind() == Atom.ATOM_SEGMENTTRUNK) {
            if (gradientFactor > 1000) System.err.println("Warn: SegmentTrunk gradientFactor " + gradientFactor + " > 1000 ");
            params[PARAM_SEG_WIDTH] = params[PARAM_SEG_WIDTH] + gradientFactor * currentAtom.getAngle();
            params[PARAM_SEG_HEIGHT] = params[PARAM_SEG_HEIGHT] + gradientFactor * currentAtom.getAngle();
            drawSeg(g, x, ySeg, params[PARAM_SEG_WIDTH], params[PARAM_SEG_HEIGHT]);
            oldY = ySeg;
            x += Math.round(params[PARAM_SEG_WIDTH] / 2.0f);
            y = ySeg + (int) Math.round(params[PARAM_SEG_HEIGHT] / 2.0f);
        }
        if (currentAtom.getKind() == Atom.ATOM_JOINT) {
            oldX = x;
            oldY = y;
            ang = params[PARAM_JOINT_ANGLE];
            x = (int) Math.round(x + jointscale * params[PARAM_JOINT_LENGTH] * Math.cos(ang));
            y = (int) Math.round(y + jointscale * params[PARAM_JOINT_LENGTH] * Math.sin(ang));
            thick = (int) (1 + Math.floor(Math.abs(params[PARAM_JOINT_THICKNESS])));
            drawLine(g, oldX, oldY, x, y, thick);
            leftX = xCenter - (x - xCenter);
            leftOldX = xCenter - (oldX - xCenter);
            g.setColor(Color.BLACK);
            drawLine(g, leftOldX, oldY, leftX, y, thick);
        }
        if (currentAtom.getKind() == Atom.ATOM_CLAW) {
            drawClaw(g, params, x, y, xCenter);
        } else {
            if (currentAtom.getFirstBelowMe() != null) {
                draw(g, currentAtom.getFirstBelowMe(), params, x, y, xCenter, ySeg);
            }
            if (currentAtom.getKind() == Atom.ATOM_SEGMENTTRUNK) {
                x = xCenter;
                ySeg = Math.round(oldY + overlap * params[PARAM_SEG_WIDTH]);
            }
            if (currentAtom.getNextLikeMe() != null) {
                if (currentAtom.getKind() == Atom.ATOM_ANIMALJOINT || currentAtom.getKind() == Atom.ATOM_SECTIONJOINT || currentAtom.getKind() == Atom.ATOM_SEGMENTJOINT) {
                    draw(g, currentAtom.getNextLikeMe(), params, x, y, xCenter, ySeg);
                } else if (currentAtom.getKind() != Atom.ATOM_ANIMALTRUNK) {
                    draw(g, currentAtom.getNextLikeMe(), myPars, x, y, xCenter, ySeg);
                }
            }
        }
    }

    protected float[] initParams() {
        float[] params = new float[AnimalJPanel.CUM_PARAMS_SIZE];
        for (int index = 0; index < AnimalJPanel.CUM_PARAMS_SIZE; index++) {
            params[index] = 1.0f;
        }
        return params;
    }

    protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig;
        PreferenceParams prefs = PreferenceParams.getInstance();
        Dimension d = this.getSize();
        int start = 0;
        int centre = 0;
        northPole = 0;
        southPole = 0;
        eastPole = 0;
        westPole = 0;
        if (prefs.centring) {
            hidePen = true;
            draw(g, animal.getAtom(), initParams(), centre, start, centre, start);
            hidePen = false;
            midriff = (southPole + northPole) / 2;
            verticalOffset = start - midriff;
        }
        hidePen = false;
        g.translate(d.width / 2, d.height / 2);
        if (prefs.sideways) {
            g.rotate(Math.PI / 2.0d);
            int temp = d.width;
            d.width = d.height;
            d.height = temp;
        }
        g.setColor(Color.BLACK);
        g.drawRect(0 - d.width / 2, 0 - d.height / 2, d.width, d.height);
        draw(g, animal.getAtom(), initParams(), centre, start + verticalOffset, centre, start + verticalOffset);
        hidePen = true;
    }

    /** 
	 * call this as evolve(myPt) from Do_Breeding_Window immediately after defining myPt
	 */
    protected void evolve() {
        this.animal.brood.clear();
        Atom.currentAnimal = this.animal;
        BreedingPanel bp = BreedingPanel.getInstance();
        AnimalJPanel midBox = bp.getMidBox();
        midBox.setAnimal(this.animal);
        Vector<AnimalJPanel> offspring = bp.getOffspringBoxen();
        Iterator<AnimalJPanel> offspringEnum = offspring.iterator();
        while (offspringEnum.hasNext()) {
            AnimalJPanel offspringToBlank = offspringEnum.next();
            offspringToBlank.animal = null;
            offspringToBlank.repaint();
        }
        Atom.currentAnimal.produceBrood();
        Iterator<Animal> broodEnum = Atom.currentAnimal.brood.iterator();
        offspringEnum = offspring.iterator();
        while (broodEnum.hasNext()) {
            AnimalJPanel box = offspringEnum.next();
            box.setAnimal(broodEnum.next());
            box.repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        evolve();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}
