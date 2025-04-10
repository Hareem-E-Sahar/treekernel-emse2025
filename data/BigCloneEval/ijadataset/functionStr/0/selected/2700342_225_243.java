public class Test {    protected void setInput(IEditorInput input) {
        super.setInput(input);
        SafeRunner.run(new SafeRunnable() {

            public void run() throws Exception {
                setDashboard(DashboardSerializer.getInstance().deserialize(getFile(), null));
                if (getGraphicalViewer() != null) {
                    getGraphicalViewer().setContents(dashboard);
                    updateRulers();
                }
            }

            public void handleException(Throwable e) {
                if (!SafeRunnable.getIgnoreErrors()) {
                    MessageDialog.openError(null, JFaceResources.getString("error"), NLS.bind(Messages.DashboardEditor_CannotLoadFile, e.getMessage()));
                }
            }
        });
    }
}