import java.lang.reflect.*;

public class Lstart {

    static int runningPrograms = 0;

    public void launch(String programm, String[] args) {
        String cmd = programm;
        for (int i = 0; i < args.length; i++) cmd += " " + args[i];
        Lstart.go(cmd);
    }

    static int RobotServer = 0;

    public static void go(final String cmd) {
        System.out.println("running a " + cmd);
        final String[] args = cmd.split("[ ]");
        if (args[0].equalsIgnoreCase("RobotServer")) {
            if (RobotServer > 0) {
                System.out.println("Sorry only once!");
                return;
            }
            RobotServer++;
        }
        Thread thread = new Thread(args[0]) {

            public void run() {
                try {
                    Class clazz = Class.forName(args[0]);
                    Class[] argsTypes = { String[].class };
                    Object[] args0 = { args };
                    Method method = clazz.getMethod("main", argsTypes);
                    method.invoke(clazz, args0);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(e);
                    System.out.println("coudn't run the " + cmd);
                    runningPrograms--;
                }
            }
        };
        runningPrograms++;
        thread.start();
    }

    /**
	Launcher programQuit method
	You also need to add a runningPrograms++ operation to 
	the Launcher.go method so the Launcher can track how 
	many programs are still running. When this number hits 
	zero, the Launcher itself should shut down so that its 
	resources are freed up for other uses. 

	In each application to be started through the Launcher, 
	you also need to replace System.exit with 


	 Launcher.programQuit();
	 e.getWindow().dispose();
	*/
    public static void programQuit() {
        runningPrograms--;
        if (runningPrograms <= 0) {
            System.exit(0);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Lstart l = new Lstart();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-wait")) {
                Thread.sleep(3000);
            } else if (args[i].startsWith("-")) {
                l.launch(args[i].substring(1), args);
            }
            ;
        }
        ;
    }
}
