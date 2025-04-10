public class Test {                public void run() {
                    try {
                        Process p = Runtime.getRuntime().exec(cmd);
                        Util.writeLog(cmd, "started", "");
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        String s;
                        while ((s = stdInput.readLine()) != null) Util.writeLog("standart output:", s, "");
                        while ((s = stdError.readLine()) != null) Util.writeLog("standart error:", s, "");
                    } catch (Exception e) {
                        Util.writeLog(cmd, "not started", e.getMessage());
                    }
                }
}