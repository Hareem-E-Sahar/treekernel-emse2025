public class Test {        public static void transfer(InputStream in, OutputStream out) {
            try {
                byte[] buffer = new byte[1024 * 16];
                int read = 0;
                while ((read = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, read);
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}