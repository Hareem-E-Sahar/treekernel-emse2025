public class Test {    private byte[] loadFileData(String path, String fileName) {
        File file = new File(path, fileName);
        if (file.canRead()) {
            try {
                FileInputStream stream = new FileInputStream(file);
                ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
                byte[] b = new byte[1000];
                int n;
                while ((n = stream.read(b)) != -1) out.write(b, 0, n);
                stream.close();
                out.close();
                return out.toByteArray();
            } catch (IOException e) {
            }
        }
        return null;
    }
}