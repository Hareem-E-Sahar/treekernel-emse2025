public class Test {                public void run() {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    try {
                        int read = -1;
                        byte[] buf = new byte[1024 * 50];
                        while ((read = is.read(buf)) != -1) {
                            bout.write(buf, 0, read);
                        }
                        _result = bout.toByteArray();
                    } catch (IOException e) {
                        _iox = e;
                        _result = bout.toByteArray();
                    } finally {
                        synchronized (StreamReader.this) {
                            _finished = true;
                            StreamReader.this.notifyAll();
                        }
                    }
                }
}