public class Test {    public int executeCommand(CommandContext context) {
        this.context = context;
        String fileName = context.getArgument(0);
        IOException error = null;
        boolean alreadyOpened = false;
        synchronized (fileTargets) {
            ft = fileTargets.get(fileName);
            if (ft == null) {
                try {
                    FileWriter writer = new FileWriter(fileName, append);
                    ft = new FileTarget(fileTargets, fileName, writer);
                } catch (IOException e) {
                    error = e;
                }
            } else if (!append) {
                alreadyOpened = true;
            }
        }
        if (error != null) {
            error.printStackTrace(context.err);
            return -1;
        }
        if (alreadyOpened) {
            context.err.println("File already opened: can not overwrite");
            return -1;
        }
        if (context.getPID() >= 0) {
            ft.addContext(context);
        }
        return 0;
    }
}