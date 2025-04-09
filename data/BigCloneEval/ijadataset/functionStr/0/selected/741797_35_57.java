public class Test {    public static void main(String[] args) {
        final DeadLockByLists r = new DeadLockByLists();
        Thread write = new Thread() {

            public void run() {
                for (int i = 0; i < 1000000000; i++) {
                    r.write(i + "");
                }
            }
        };
        Thread search = new Thread() {

            public void run() {
                for (int i = 0; i < 1000000000; i++) {
                    System.out.println(r.get(i));
                }
            }
        };
        new Thread(write, "write") {
        }.start();
        new Thread(search, "search") {
        }.start();
    }
}