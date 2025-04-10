public class Test {            public void run() {
                try {
                    while (!isFinished()) {
                        while (in.available() > 0) out.write(in.read());
                        synchronized (this) {
                            this.wait(100);
                        }
                    }
                    out.flush();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
}