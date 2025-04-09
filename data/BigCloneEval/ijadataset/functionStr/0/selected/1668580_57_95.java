public class Test {    @Override
    public final boolean performFinish() {
        final ExportFileWizardPage wizardPage = ((ExportFileWizardPage) getPages()[0]);
        final File file = wizardPage.getDestinationFile();
        if (file.isDirectory()) {
            MessageDialog.openError(getShell(), "Export Problems", "Export destination must be a file, not a directory.");
            return false;
        }
        if (file.exists()) {
            if (!MessageDialog.openQuestion(getShell(), "Overwrite", "Target file already exists. Would you like to overwrite it?")) {
                return false;
            }
        }
        try {
            wizardPage.saveSettings();
            final List<Legislator> legislators = wizardPage.isExportAll() ? allLegislators : selectedLegislators;
            final boolean includePhotos = wizardPage.isIncludePhotos();
            IRunnableWithProgress runnable = new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        doExport(legislators, file, includePhotos, monitor);
                        if (!monitor.isCanceled()) {
                            Program.launch(file.getAbsolutePath());
                        }
                    } catch (SunlightException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            };
            new ProgressMonitorDialog(getShell()).run(true, true, runnable);
        } catch (InvocationTargetException e) {
            Throwable e2 = (e.getCause() != null) ? e.getCause() : e;
            CongressExplorerPlugin.getDefault().showErrorDialog(getShell(), "Export Problems", "Unable to export", e2.getMessage(), e2);
            return false;
        } catch (InterruptedException e) {
        }
        return true;
    }
}