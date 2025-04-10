public class Test {    private void execute(final Runnable readAction, final Runnable writeAction) {
        final ProgressWindow progressWindow = new ProgressWindow(true, myProject);
        progressWindow.setTitle(title);
        progressWindow.setText(message);
        final ModalityState modalityState = ModalityState.current();
        final Runnable process = new Runnable() {

            public void run() {
                ApplicationManager.getApplication().runReadAction(readAction);
            }
        };
        Runnable runnable = new Runnable() {

            public void run() {
                try {
                    ProgressManager.getInstance().runProcess(process, progressWindow);
                } catch (ProcessCanceledException processcanceledexception) {
                    return;
                }
                ApplicationManager.getApplication().invokeLater(new Runnable() {

                    public void run() {
                        CommandProcessor.getInstance().executeCommand(myProject, new Runnable() {

                            public void run() {
                                CommandProcessor.getInstance().markCurrentCommandAsComplex(myProject);
                                try {
                                    ApplicationManager.getApplication().runWriteAction(writeAction);
                                } catch (ProcessCanceledException processcanceledexception) {
                                    return;
                                }
                                if (postProcess != null) {
                                    ApplicationManager.getApplication().invokeLater(postProcess);
                                }
                            }
                        }, title, null);
                    }
                }, modalityState);
            }
        };
        (new Thread(runnable, title)).start();
    }
}