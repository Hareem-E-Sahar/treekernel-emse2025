public class Test {    private void startWriterThread() {
        Runnable writerThread = new Runnable() {

            public void run() {
                PrintWriter serverStream = new PrintWriter(os);
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while (showtime) {
                    try {
                        String line = reader.readLine();
                        if (line.equals("exit")) {
                            showtime = false;
                        }
                        serverStream.println(line);
                        serverStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(writerThread, "Writer Thread").start();
    }
}