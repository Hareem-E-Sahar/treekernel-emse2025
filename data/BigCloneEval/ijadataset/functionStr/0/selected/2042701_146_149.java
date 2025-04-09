public class Test {        @Override
        public String toString() {
            return app.getComponentLabel(index) + (breakOnRead ? ": read" : ": write");
        }
}