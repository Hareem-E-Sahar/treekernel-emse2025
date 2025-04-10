package org.parallelj.designer.validation.providers;

import java.util.Arrays;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.emf.ui.providers.marker.AbstractModelMarkerNavigationProvider;
import org.parallelj.designer.part.ParallelJDiagramEditorUtil;
import org.parallelj.designer.validation.DiagramValidationPlugin;

/**
 * MarkerNavigationProvider provides the necessary wrapping required to perform
 * model operations related to the navigation of markers. The marker attributes
 * contain model element information that needs to be resolved.
 */
public class MarkerNavigationProvider extends AbstractModelMarkerNavigationProvider {

    public static final String MARKER_TYPE = DiagramValidationPlugin.PLUGIN_ID + ".diagnostic";

    @Override
    protected void doGotoMarker(IMarker marker) {
        String elementId = marker.getAttribute(org.eclipse.gmf.runtime.common.core.resources.IMarker.ELEMENT_ID, null);
        if (elementId == null || !(this.getEditor() instanceof DiagramEditor)) {
            return;
        }
        DiagramEditor editor = (DiagramEditor) this.getEditor();
        Map<?, ?> editPartRegistry = editor.getDiagramGraphicalViewer().getEditPartRegistry();
        EObject targetView = editor.getDiagram().eResource().getEObject(elementId);
        if (targetView == null) {
            return;
        }
        EditPart targetEditPart = (EditPart) editPartRegistry.get(targetView);
        if (targetEditPart != null) {
            ParallelJDiagramEditorUtil.selectElementsInDiagram(editor, Arrays.asList(new EditPart[] { targetEditPart }));
        }
    }

    /**
	 * Deletes the marker from the resource.
	 * 
	 * @param resource
	 */
    public static void deleteMarkers(IResource resource) {
        try {
            if (resource != null && resource.exists()) {
                resource.deleteMarkers(MarkerNavigationProvider.MARKER_TYPE, true, IResource.DEPTH_ZERO);
            }
        } catch (CoreException e) {
            DiagramValidationPlugin.getDefault().logError("Failed to delete validation markers", e);
        }
    }

    /**
	 * adds the marker from the resource.
	 * 
	 * @param file
	 * @param elementId
	 * @param location
	 * @param message
	 * @param statusSeverity
	 * @return
	 */
    public static IMarker addMarker(IFile file, String elementId, String location, String message, int statusSeverity) {
        IMarker marker = null;
        try {
            marker = file.createMarker(MarkerNavigationProvider.MARKER_TYPE);
            marker.setAttribute(IMarker.MESSAGE, message);
            marker.setAttribute(IMarker.LOCATION, location);
            marker.setAttribute(org.eclipse.gmf.runtime.common.core.resources.IMarker.ELEMENT_ID, elementId);
            int markerSeverity = IMarker.SEVERITY_INFO;
            if (statusSeverity == IStatus.WARNING) {
                markerSeverity = IMarker.SEVERITY_WARNING;
            } else if (statusSeverity == IStatus.ERROR || statusSeverity == IStatus.CANCEL) {
                markerSeverity = IMarker.SEVERITY_ERROR;
            }
            marker.setAttribute(IMarker.SEVERITY, markerSeverity);
        } catch (CoreException e) {
            DiagramValidationPlugin.getDefault().logError("Failed to create validation marker", e);
        }
        return marker;
    }
}
