package org.openscience.jchempaint.controller;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Point2d;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;
import org.openscience.jchempaint.controller.undoredo.IUndoRedoFactory;
import org.openscience.jchempaint.controller.undoredo.IUndoRedoable;
import org.openscience.jchempaint.controller.undoredo.UndoRedoHandler;
import org.openscience.jchempaint.renderer.BoundsCalculator;
import org.openscience.jchempaint.renderer.selection.IChemObjectSelection;

/**
 * Module to rotate a selection of atoms (and their bonds).
 *
 * @cdk.module controlbasic
 */
public class RotateModule extends ControllerModuleAdapter {

    protected static ILoggingTool logger = LoggingToolFactory.createLoggingTool(RotateModule.class);

    private double rotationAngle;

    protected boolean selectionMade = false;

    protected IChemObjectSelection selection;

    protected Point2d rotationCenter;

    protected Point2d[] startCoordsRelativeToRotationCenter;

    protected Map<IAtom, Point2d[]> atomCoordsMap;

    protected boolean rotationPerformed;

    protected String ID;

    /**
     * Constructor 
     * @param chemModelRelay
     */
    public RotateModule(IChemModelRelay chemModelRelay) {
        super(chemModelRelay);
        logger.debug("constructor");
    }

    /**
     * Initializes possible rotation. Determines rotation center and stores 
     * coordinates of atoms to be rotated. These stored coordinates are relative 
     * to the rotation center.
     */
    public void mouseClickedDown(Point2d worldCoord) {
        logger.debug("rotate mouseClickedDown, initializing rotation");
        rotationCenter = null;
        selection = super.chemModelRelay.getRenderer().getRenderer2DModel().getSelection();
        if (selection == null || !selection.isFilled() || selection.getConnectedAtomContainer() == null || selection.getConnectedAtomContainer().getAtomCount() == 0) {
            logger.debug("Nothing selected for rotation");
            selectionMade = false;
            return;
        } else {
            rotationPerformed = false;
            Rectangle2D bounds = BoundsCalculator.calculateBounds(this.chemModelRelay.getRenderer().getRenderer2DModel().getSelection().getConnectedAtomContainer());
            rotationAngle = 0.0;
            selectionMade = true;
            atomCoordsMap = new HashMap<IAtom, Point2d[]>();
            for (IAtom atom : selection.getConnectedAtomContainer().atoms()) {
                Point2d[] coordsforatom = new Point2d[2];
                coordsforatom[1] = atom.getPoint2d();
                atomCoordsMap.put(atom, coordsforatom);
            }
            IAtomContainer selectedAtoms = selection.getConnectedAtomContainer();
            Double upperX = null, lowerX = null, upperY = null, lowerY = null;
            for (int i = 0; i < selectedAtoms.getAtomCount(); i++) {
                if (upperX == null) {
                    upperX = selectedAtoms.getAtom(i).getPoint2d().x;
                    lowerX = upperX;
                    upperY = selectedAtoms.getAtom(i).getPoint2d().y;
                    lowerY = selectedAtoms.getAtom(i).getPoint2d().y;
                } else {
                    double currX = selectedAtoms.getAtom(i).getPoint2d().x;
                    if (currX > upperX) upperX = currX;
                    if (currX < lowerX) lowerX = currX;
                    double currY = selectedAtoms.getAtom(i).getPoint2d().y;
                    if (currY > upperY) upperY = currY;
                    if (currY < lowerY) lowerY = currY;
                }
            }
            rotationCenter = new Point2d();
            rotationCenter.x = (upperX + lowerX) / 2;
            rotationCenter.y = (upperY + lowerY) / 2;
            logger.debug("rotationCenter " + rotationCenter.x + " " + rotationCenter.y);
            startCoordsRelativeToRotationCenter = new Point2d[selectedAtoms.getAtomCount()];
            for (int i = 0; i < selectedAtoms.getAtomCount(); i++) {
                Point2d relativeAtomPosition = new Point2d();
                relativeAtomPosition.x = selectedAtoms.getAtom(i).getPoint2d().x - rotationCenter.x;
                relativeAtomPosition.y = selectedAtoms.getAtom(i).getPoint2d().y - rotationCenter.y;
                startCoordsRelativeToRotationCenter[i] = relativeAtomPosition;
            }
        }
    }

    /**
     * On mouse drag, actual rotation around the center is done
     */
    public void mouseDrag(Point2d worldCoordFrom, Point2d worldCoordTo) {
        if (selectionMade) {
            rotationPerformed = true;
            int quadrant = 0;
            if ((worldCoordFrom.x >= rotationCenter.x)) if ((worldCoordFrom.y <= rotationCenter.y)) quadrant = 1; else quadrant = 2; else if ((worldCoordFrom.y <= rotationCenter.y)) quadrant = 4; else quadrant = 3;
            final int SLOW_DOWN_FACTOR = 4;
            switch(quadrant) {
                case 1:
                    rotationAngle += (worldCoordTo.x - worldCoordFrom.x) / SLOW_DOWN_FACTOR + (worldCoordTo.y - worldCoordFrom.y) / SLOW_DOWN_FACTOR;
                    break;
                case 2:
                    rotationAngle += (worldCoordFrom.x - worldCoordTo.x) / SLOW_DOWN_FACTOR + (worldCoordTo.y - worldCoordFrom.y) / SLOW_DOWN_FACTOR;
                    break;
                case 3:
                    rotationAngle += (worldCoordFrom.x - worldCoordTo.x) / SLOW_DOWN_FACTOR + (worldCoordFrom.y - worldCoordTo.y) / SLOW_DOWN_FACTOR;
                    break;
                case 4:
                    rotationAngle += (worldCoordTo.x - worldCoordFrom.x) / SLOW_DOWN_FACTOR + (worldCoordFrom.y - worldCoordTo.y) / SLOW_DOWN_FACTOR;
                    break;
            }
            double cosine = java.lang.Math.cos(rotationAngle);
            double sine = java.lang.Math.sin(rotationAngle);
            for (int i = 0; i < startCoordsRelativeToRotationCenter.length; i++) {
                double newX = (startCoordsRelativeToRotationCenter[i].x * cosine) - (startCoordsRelativeToRotationCenter[i].y * sine);
                double newY = (startCoordsRelativeToRotationCenter[i].x * sine) + (startCoordsRelativeToRotationCenter[i].y * cosine);
                Point2d newCoords = new Point2d(newX + rotationCenter.x, newY + rotationCenter.y);
                selection.getConnectedAtomContainer().getAtom(i).setPoint2d(newCoords);
            }
        }
        chemModelRelay.updateView();
    }

    /**
     * After the rotation (=mouse up after drag), post the undo/redo information
     * with the old and the new coordinates
     */
    public void mouseClickedUp(Point2d worldCoord) {
        if (rotationPerformed && atomCoordsMap != null) {
            logger.debug("posting undo/redo for rotation");
            for (IAtom atom : selection.getConnectedAtomContainer().atoms()) {
                Point2d[] coords = atomCoordsMap.get(atom);
                coords[0] = atom.getPoint2d();
            }
            IUndoRedoFactory factory = chemModelRelay.getUndoRedoFactory();
            UndoRedoHandler handler = chemModelRelay.getUndoRedoHandler();
            if (factory != null && handler != null) {
                IUndoRedoable undoredo = factory.getChangeCoordsEdit(atomCoordsMap, "Rotation");
                handler.postEdit(undoredo);
            }
        }
    }

    public void setChemModelRelay(IChemModelRelay relay) {
        this.chemModelRelay = relay;
    }

    public String getDrawModeString() {
        return "Rotate";
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
