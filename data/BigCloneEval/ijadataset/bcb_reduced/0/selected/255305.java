package org.fudaa.fudaa.commun.report;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.Icon;
import com.memoire.dja.DjaForm;
import com.memoire.dja.DjaGridEvent;
import com.memoire.dja.DjaGridInteractive;
import com.memoire.dja.DjaGridListener;
import com.memoire.dja.DjaVector;
import org.fudaa.ebli.commun.EbliActionSimple;
import org.fudaa.fudaa.commun.FudaaLib;
import org.fudaa.fudaa.ressource.FudaaResource;

/**
 * @author fred deniger
 * @version $Id: FudaaDjaAlignAction.java,v 1.3 2007-03-30 15:37:08 deniger Exp $
 */
public class FudaaDjaAlignAction extends EbliActionSimple implements DjaGridListener {

    final DjaGridInteractive grid_;

    public static void addActionToList(DjaGridInteractive _grid, List _l) {
        _l.add(new Left(_grid));
        _l.add(new Center(_grid));
        _l.add(new Right(_grid));
        _l.add(null);
        _l.add(new Top(_grid));
        _l.add(new Middle(_grid));
        _l.add(new Bottom(_grid));
        _l.add(null);
        _l.add(new Width(_grid));
        _l.add(new Height(_grid));
    }

    public FudaaDjaAlignAction(DjaGridInteractive _grid, String _name, Icon _ic, String _ac) {
        super(_name, _ic, _ac);
        grid_ = _grid;
        grid_.addGridListener(this);
        setEnabled(false);
    }

    public void objectAdded(DjaGridEvent _evt) {
    }

    public void objectConnected(DjaGridEvent _evt) {
    }

    public void objectDisconnected(DjaGridEvent _evt) {
    }

    public void objectModified(DjaGridEvent _evt) {
    }

    public void objectRemoved(DjaGridEvent _evt) {
    }

    public void objectSelected(DjaGridEvent _evt) {
        updateStateBeforeShow();
    }

    public void objectUnselected(DjaGridEvent _evt) {
        updateStateBeforeShow();
    }

    public void updateStateBeforeShow() {
        setEnabled(grid_.getSelection() != null && grid_.getSelection().size() > 1);
    }

    protected void fireModified() {
        grid_.repaint();
        grid_.fireGridEvent(null, DjaGridEvent.MODIFIED);
    }

    public static class Left extends FudaaDjaAlignAction {

        public Left(DjaGridInteractive _grid) {
            super(_grid, FudaaLib.getS("Alignement � gauche"), FudaaResource.FUDAA.getToolIcon("aoleft"), "LEFT");
        }

        public void actionPerformed(ActionEvent _e) {
            DjaVector vect = grid_.getSelection();
            int size = vect.size();
            if (size > 1) {
                int min = grid_.getWidth();
                for (int i = 0; i < size; i++) {
                    min = Math.min(min, ((DjaForm) vect.elementAt(i)).getX());
                }
                for (int i = 0; i < size; i++) {
                    ((DjaForm) vect.elementAt(i)).setX(min);
                }
                fireModified();
            }
        }
    }

    public static class Right extends FudaaDjaAlignAction {

        public Right(DjaGridInteractive _grid) {
            super(_grid, FudaaLib.getS("Alignement � droite"), FudaaResource.FUDAA.getToolIcon("aoright"), "RIGHT");
        }

        public void actionPerformed(ActionEvent _e) {
            DjaVector vect = grid_.getSelection();
            int size = vect.size();
            if (size > 1) {
                int max = 0;
                for (int i = 0; i < size; i++) {
                    DjaForm djaForm = ((DjaForm) vect.elementAt(i));
                    max = Math.max(max, djaForm.getX() + djaForm.getWidth());
                }
                for (int i = 0; i < size; i++) {
                    DjaForm djaForm = ((DjaForm) vect.elementAt(i));
                    djaForm.setX(max - djaForm.getWidth());
                }
                fireModified();
            }
        }
    }

    public static class Center extends FudaaDjaAlignAction {

        public Center(DjaGridInteractive _grid) {
            super(_grid, FudaaLib.getS("Centrer horizontalement"), FudaaResource.FUDAA.getToolIcon("aocenterh"), "CENTER");
        }

        public void actionPerformed(ActionEvent _e) {
            DjaVector vect = grid_.getSelection();
            int size = vect.size();
            if (size > 1) {
                int max = 0;
                int min = grid_.getWidth();
                for (int i = 0; i < size; i++) {
                    DjaForm djaForm = ((DjaForm) vect.elementAt(i));
                    max = Math.max(max, djaForm.getX() + djaForm.getWidth());
                    min = Math.min(min, djaForm.getX());
                }
                int center = (min + max) / 2;
                for (int i = 0; i < size; i++) {
                    DjaForm djaForm = ((DjaForm) vect.elementAt(i));
                    djaForm.setX(center - djaForm.getWidth() / 2);
                }
            }
            fireModified();
        }
    }

    public static class Top extends FudaaDjaAlignAction {

        public Top(DjaGridInteractive _grid) {
            super(_grid, FudaaLib.getS("En haut"), FudaaResource.FUDAA.getToolIcon("aotop"), "TOP");
        }

        public void actionPerformed(ActionEvent _e) {
            DjaVector vect = grid_.getSelection();
            int size = vect.size();
            if (size > 1) {
                int min = grid_.getHeight();
                for (int i = 0; i < size; i++) {
                    min = Math.min(min, ((DjaForm) vect.elementAt(i)).getY());
                }
                for (int i = 0; i < size; i++) {
                    DjaForm djaForm = ((DjaForm) vect.elementAt(i));
                    djaForm.setY(min);
                }
            }
            fireModified();
        }
    }

    public static class Bottom extends FudaaDjaAlignAction {

        public Bottom(DjaGridInteractive _grid) {
            super(_grid, FudaaLib.getS("En bas"), FudaaResource.FUDAA.getToolIcon("aobottom"), "BOTTOM");
        }

        public void actionPerformed(ActionEvent _e) {
            DjaVector vect = grid_.getSelection();
            int size = vect.size();
            if (size > 1) {
                int max = 0;
                for (int i = 0; i < size; i++) {
                    DjaForm djaForm = (DjaForm) vect.elementAt(i);
                    max = Math.max(max, djaForm.getY() + djaForm.getHeight());
                }
                for (int i = 0; i < size; i++) {
                    DjaForm djaForm = ((DjaForm) vect.elementAt(i));
                    djaForm.setY(max - djaForm.getHeight());
                }
            }
            fireModified();
        }
    }

    public static class Middle extends FudaaDjaAlignAction {

        public Middle(DjaGridInteractive _grid) {
            super(_grid, FudaaLib.getS("Centrer verticalement"), FudaaResource.FUDAA.getToolIcon("aocenterv"), "MIDDLE");
        }

        public void actionPerformed(ActionEvent _e) {
            DjaVector vect = grid_.getSelection();
            int size = vect.size();
            if (size > 1) {
                int max = 0;
                int min = grid_.getHeight();
                for (int i = 0; i < size; i++) {
                    DjaForm djaForm = (DjaForm) vect.elementAt(i);
                    max = Math.max(max, djaForm.getY() + djaForm.getHeight());
                    min = Math.min(min, ((DjaForm) vect.elementAt(i)).getY());
                }
                int middle = (max + min) / 2;
                for (int i = 0; i < size; i++) {
                    DjaForm djaForm = ((DjaForm) vect.elementAt(i));
                    djaForm.setY(middle - djaForm.getHeight() / 2);
                }
            }
            fireModified();
        }
    }

    public static class Width extends FudaaDjaAlignAction {

        public Width(DjaGridInteractive _grid) {
            super(_grid, FudaaLib.getS("M�me largeur"), FudaaResource.FUDAA.getToolIcon("aowidest"), "SAME_WIDTH");
        }

        public void actionPerformed(ActionEvent _e) {
            DjaVector vect = grid_.getSelection();
            int size = vect.size();
            if (size > 1) {
                int max = 0;
                for (int i = 0; i < size; i++) {
                    DjaForm djaForm = (DjaForm) vect.elementAt(i);
                    max = Math.max(max, djaForm.getWidth());
                }
                for (int i = 0; i < size; i++) {
                    DjaForm djaForm = ((DjaForm) vect.elementAt(i));
                    int x = Math.max(0, djaForm.getX() + (djaForm.getWidth() - max) / 2);
                    djaForm.setX(x);
                    djaForm.setWidth(max);
                }
            }
            fireModified();
        }
    }

    public static class Height extends FudaaDjaAlignAction {

        public Height(DjaGridInteractive _grid) {
            super(_grid, FudaaLib.getS("M�me hauteur"), FudaaResource.FUDAA.getToolIcon("aotallest"), "SAME_HEIGHT");
        }

        public void actionPerformed(ActionEvent _e) {
            DjaVector vect = grid_.getSelection();
            int size = vect.size();
            if (size > 1) {
                int max = 0;
                for (int i = 0; i < size; i++) {
                    DjaForm djaForm = (DjaForm) vect.elementAt(i);
                    max = Math.max(max, djaForm.getHeight());
                }
                for (int i = 0; i < size; i++) {
                    DjaForm djaForm = ((DjaForm) vect.elementAt(i));
                    int y = Math.max(0, djaForm.getY() + (djaForm.getHeight() - max) / 2);
                    djaForm.setY(y);
                    djaForm.setHeight(max);
                }
            }
            fireModified();
        }
    }
}
