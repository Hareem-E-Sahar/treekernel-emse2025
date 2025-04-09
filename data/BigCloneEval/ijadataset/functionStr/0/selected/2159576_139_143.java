public class Test {        @Override
        protected void hookGraphicalViewer() {
            getSelectionSynchronizer().addViewer(getGraphicalViewer());
            SarosWhiteboardView.this.getViewSite().setSelectionProvider(getGraphicalViewer());
        }
}