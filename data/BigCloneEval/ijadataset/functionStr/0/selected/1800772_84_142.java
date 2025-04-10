public class Test {    private void renderThreadsPage(Hashtable<String, String> parameters, HtmlWriter writer) {
        writer.startPanel("WARNING");
        writer.writeLn("On this page, you can alter some key functions of the Spamato system.");
        writer.writeLn("Please be aware that you should really know what you are doing; otherwise, Spamato might not work properly anymore.");
        writer.writeLn("(If you know what you are doing, you might be able to optimize your Spamato system for your particular machine and get some info about what's going on.)");
        writer.addBr();
        writer.addBr();
        writer.writeLn("<b>You have been warned!</b>");
        writer.endPanel();
        writer.startPanel("Process Thread Pool");
        writer.writeLn("The Process Thread Pool limits the number of concurrent email checks.");
        writer.addBr();
        writer.addBr();
        writer.writeLn("You can change the initial ");
        writer.addTextField(writer.getPropertyName(SpamatoSettings.INITIAL_PROCESS_THREADS_PROPERTY_NAME), config.get(SpamatoSettings.INITIAL_PROCESS_THREADS_PROPERTY_NAME), "2");
        writer.writeLn(" and maximum ");
        writer.addTextField(writer.getPropertyName(SpamatoSettings.MAX_PROCESS_THREADS_PROPERTY_NAME), config.get(SpamatoSettings.MAX_PROCESS_THREADS_PROPERTY_NAME), "2");
        writer.writeLn(" number of threads.");
        writer.write("You can set the ");
        writer.addButton("default", "?action=defaultProcessThreads");
        writer.writeLn(" values here.");
        writer.writeLn("<b>Don't forget to click 'Save' to apply your changes.</b>");
        writer.addBr();
        writer.addBr();
        writer.writeLn("<b>Current state</b>");
        writer.addBr();
        writer.writeLn("All Threads: " + spamato.processThreadPool.getThreadNumber() + "<br>");
        writer.writeLn(String.format("Active Threads: %1d<br>", spamato.processThreadPool.getActiveThreadNumber()));
        writer.writeLn("Bored Threads: " + spamato.processThreadPool.getBoredThreadNumber() + "<br>");
        writer.writeLn("Buffered Tasks: " + spamato.processThreadPool.getBufferedTaskNumber() + "<br>");
        writer.addBr();
        writer.writeLnIndent(String.format("<a href='%1s' target='_top'>Click here to see more information about emails currently being checked.</a>", getComponentPath() + "process_threads"));
        writer.endPanel();
        writer.startPanel("PreChecker/Filter Thread Pool");
        writer.writeLn("The PreChecker/Filter Thread Pool limits the number of concurrently running PreChecker/Filters.");
        writer.writeLn("We handle this separately since one email check invokes several PreCheckers/Filters.");
        writer.addBr();
        writer.addBr();
        writer.writeLn("You can change the initial ");
        writer.addTextField(writer.getPropertyName(SpamatoSettings.INITIAL_FILTER_THREADS_PROPERTY_NAME), config.get(SpamatoSettings.INITIAL_FILTER_THREADS_PROPERTY_NAME), "2");
        writer.writeLn(" and maximum ");
        writer.addTextField(writer.getPropertyName(SpamatoSettings.MAX_FILTER_THREADS_PROPERTY_NAME), config.get(SpamatoSettings.MAX_FILTER_THREADS_PROPERTY_NAME), "2");
        writer.writeLn(" number of threads.");
        writer.write("You can set the ");
        writer.addButton("default", "?action=defaultFilterThreads");
        writer.writeLn(" values here.");
        writer.writeLn("<b>Don't forget to click 'Save' to apply your changes.</b>");
        writer.addBr();
        writer.addBr();
        writer.writeLn("<b>Current state</b>");
        writer.addBr();
        writer.writeLn("All Threads: " + spamato.filterThreadPool.getThreadNumber() + "<br>");
        writer.writeLn(String.format("Active Threads: %1d<br>", spamato.filterThreadPool.getActiveThreadNumber()));
        writer.writeLn("Bored Threads: " + spamato.filterThreadPool.getBoredThreadNumber() + "<br>");
        writer.writeLn("Buffered Tasks: " + spamato.filterThreadPool.getBufferedTaskNumber() + "<br>");
        writer.addBr();
        writer.writeLnIndent(String.format("<a href='%1s' target='_top'>Click here to see more information about emails currently being checked.</a>", getComponentPath() + "filter_threads"));
        writer.endPanel();
    }
}