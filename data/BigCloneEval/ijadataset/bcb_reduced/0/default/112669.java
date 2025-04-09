import java.lang.reflect.InvocationTargetException;

/**
 * Will execute an Applescript through reflect, so we can still
 * compile this with inferior OSs.
 */
final class AppleScriptExecutor {

    public String execute(String script) {
        Class NSAppleScriptClass = null;
        Class NSMutableDictionaryClass = null;
        try {
            NSAppleScriptClass = Class.forName("com.apple.cocoa.foundation.NSAppleScript");
        } catch (ClassNotFoundException e) {
            handle(e, "Trouble creating class 'com.apple.cocoa.foundation.NSAppleScript'");
        }
        try {
            NSMutableDictionaryClass = Class.forName("com.apple.cocoa.foundation.NSMutableDictionary");
        } catch (ClassNotFoundException e) {
            handle(e, "Trouble creating class 'com.apple.cocoa.foundation.NSMutableDictionary'");
        }
        try {
            Object myScript = NSAppleScriptClass.getConstructor(new Class[] { String.class }).newInstance(new Object[] { script });
            Object errors = NSMutableDictionaryClass.getConstructor(new Class[0]).newInstance(new Object[0]);
            Object results = NSAppleScriptClass.getMethod("execute", new Class[] { NSMutableDictionaryClass }).invoke(myScript, new Object[] { errors });
            if (results == null) return null;
            return String.valueOf(results.getClass().getMethod("stringValue", new Class[0]).invoke(results, new Object[0]));
        } catch (IllegalAccessException e) {
            handle(e, "Trouble executing script");
        } catch (NoSuchMethodException e) {
            handle(e, "Trouble executing script");
        } catch (InstantiationException e) {
            handle(e, "Trouble executing script");
        } catch (InvocationTargetException e) {
            handle(e, "Trouble executing script");
        }
        return null;
    }

    private void handle(Throwable e, String msg) {
        System.err.println(msg);
        System.err.println("Your current OS is " + System.getProperty("os.name") + ", you need ");
        System.err.println("to be running Mac OSX to use this class");
        e.printStackTrace();
        throw new RuntimeException(e);
    }

    public static void main(String[] args) throws java.io.IOException {
        System.out.println("Type a script and then '.' or 'quit' to quit...");
        final AppleScriptExecutor app = new AppleScriptExecutor();
        final StringBuffer script = new StringBuffer();
        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            System.out.flush();
            String line = in.readLine();
            if (line.equals("quit")) break;
            if (line.equals(".")) {
                System.out.println(app.execute(script.toString()));
                script.delete(0, script.length());
            } else {
                boolean execute = false;
                if (line.endsWith(".")) {
                    line = line.substring(0, line.length() - 1);
                    execute = true;
                }
                script.append(line).append("\n");
                if (execute) {
                    System.out.println(app.execute(script.toString()));
                    script.delete(0, script.length());
                }
                script.append(line).append("\n");
            }
        }
    }
}
