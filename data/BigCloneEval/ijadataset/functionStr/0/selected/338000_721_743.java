public class Test {        PlainFileNode(String name, File file) throws IOException {
            this.name = name;
            FileInputStream is = new FileInputStream(file);
            try {
                CRC32 crc32 = new CRC32();
                long size = 0L;
                byte[] buffer = new byte[8192];
                for (; ; ) {
                    int count = is.read(buffer);
                    if (count == -1) break;
                    crc32.update(buffer, 0, count);
                    size += count;
                }
                this.size = size;
                this.crc32 = (int) crc32.getValue();
            } finally {
                try {
                    is.close();
                } catch (IOException ioe) {
                }
            }
            this.modificationTime = file.lastModified();
        }
}