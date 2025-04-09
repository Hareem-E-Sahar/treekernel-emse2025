import org.roober.hedgehog.*;

class TestServer {

    long startTime = 0;

    int endRange = 10;

    int returnedResults = 0;

    public static void main(String[] args) {
        new TestServer();
    }

    TestServer() {
        JobServer server = new JobServer();
        JobListener l = new PrintJobListener();
        Job j = new SquareJob(40);
        startTime = System.currentTimeMillis();
        for (int i = 1; i < endRange; i++) server.postJob(j, l);
    }

    static class SquareJob implements Job {

        int n;

        SquareJob(int n) {
            this.n = n;
        }

        public Object run() {
            int result = fib(n);
            System.out.println("doJob(" + n + ")=" + result);
            return "doJob(" + n + ")=" + result;
        }

        private int fib(int n) {
            if (n < 2) return 1; else return fib(n - 1) + fib(n - 2);
        }

        public String description() {
            return "prints out a message on both client and server";
        }
    }

    class PrintJobListener implements JobListener {

        public void jobFinished(Job j, Object results) {
            System.out.println(results);
            if (++returnedResults >= endRange - 1) System.out.println(System.currentTimeMillis() - startTime);
        }
    }
}
