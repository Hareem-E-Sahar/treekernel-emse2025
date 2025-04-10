package uk.ac.bolton.archimate.templates.wizard;

import java.util.Iterator;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import uk.ac.bolton.archimate.editor.diagram.util.DiagramUtils;
import uk.ac.bolton.archimate.model.IDiagramModel;
import uk.ac.bolton.archimate.model.IIdentifier;
import uk.ac.bolton.archimate.templates.model.TemplateManager;

/**
 * Template Utils
 * 
 * @author Phillip Beauvoir
 */
public class TemplateUtils {

    /**
     * Create and add a thumbnail preview image of diagramModel to label, scaled.
     * The image will will need to be disposed.
     * @param label
     */
    public static void createThumbnailPreviewImage(IDiagramModel diagramModel, Label label) {
        Shell shell = new Shell();
        GraphicalViewer diagramViewer = DiagramUtils.createViewer(diagramModel, shell);
        Rectangle bounds = DiagramUtils.getDiagramExtents(diagramViewer);
        double ratio = Math.min(1, Math.min((double) label.getBounds().width / bounds.width, (double) label.getBounds().height / bounds.height));
        Image image = DiagramUtils.createScaledImage(diagramViewer, ratio);
        label.setImage(image);
        shell.dispose();
    }

    /**
     * Create a thumbnail image to add to the template
     * @param dm
     * @return
     */
    public static Image createThumbnailImage(IDiagramModel diagramModel) {
        Shell shell = new Shell();
        GraphicalViewer diagramViewer = DiagramUtils.createViewer(diagramModel, shell);
        Rectangle bounds = DiagramUtils.getDiagramExtents(diagramViewer);
        double ratio = Math.min(1, Math.min((double) TemplateManager.THUMBNAIL_WIDTH / bounds.width, (double) TemplateManager.THUMBNAIL_HEIGHT / bounds.height));
        Image image = DiagramUtils.createScaledImage(diagramViewer, ratio);
        shell.dispose();
        GC gc = new GC(image);
        Color c = new Color(null, 64, 64, 64);
        gc.setForeground(c);
        gc.drawRectangle(0, 0, image.getBounds().width - 1, image.getBounds().height - 1);
        gc.dispose();
        c.dispose();
        return image;
    }

    /**
     * Generate new UUIDs for object and all of its children
     * @param object
     */
    public static void generateNewUUIDs(EObject object) {
        if (object instanceof IIdentifier) {
            ((IIdentifier) object).setId(EcoreUtil.generateUUID());
        }
        for (Iterator<EObject> iter = object.eAllContents(); iter.hasNext(); ) {
            EObject eObject = iter.next();
            if (eObject instanceof IIdentifier) {
                ((IIdentifier) eObject).setId(EcoreUtil.generateUUID());
            }
        }
    }
}
