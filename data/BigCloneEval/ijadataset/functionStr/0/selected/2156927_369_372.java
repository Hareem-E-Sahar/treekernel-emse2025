public class Test {        public void halt() {
            System.out.print("halting write thread...");
            shouldWrite = false;
        }
}