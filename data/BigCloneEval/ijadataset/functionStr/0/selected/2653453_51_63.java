public class Test {            @Override
            public void partActivated(IWorkbenchPart part) {
                if (part instanceof IDiagramModelEditor) {
                    IDiagramModelEditor diagramEditor = (IDiagramModelEditor) part;
                    if (!fDiagramEditors.contains(diagramEditor)) {
                        GraphicalViewer viewer = (GraphicalViewer) diagramEditor.getAdapter(GraphicalViewer.class);
                        if (viewer != null) {
                            viewer.addSelectionChangedListener(TreeSelectionSynchroniser.this);
                            fDiagramEditors.add(diagramEditor);
                        }
                    }
                }
            }
}