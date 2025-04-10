public class Test {    private static void createCompressedZip(ByteArrayOutputStream bytesOut) throws IOException {
        ZipOutputStream out = new ZipOutputStream(bytesOut);
        try {
            int i;
            for (i = 0; i < 3; i++) {
                byte[] input = makeSampleFile(i);
                ZipEntry newEntry = new ZipEntry("file-" + i);
                if (i != 1) newEntry.setComment("this is file " + i);
                out.putNextEntry(newEntry);
                out.write(input, 0, input.length);
                out.closeEntry();
            }
            out.setComment("This is a lovely compressed archive!");
        } finally {
            out.close();
        }
    }
}