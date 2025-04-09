public class Test {    public void write(String text) {
        synchronized (this.treeLock) {
            if (this.reader != null) {
                try {
                    this.reader.write(text);
                } catch (IOException ioe) {
                }
            }
        }
    }
}