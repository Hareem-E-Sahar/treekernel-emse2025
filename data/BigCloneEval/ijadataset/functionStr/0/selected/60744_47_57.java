public class Test {    public void createTempFile(String resourceName) throws IOException {
        InputStream input = JulImportCallableTest.class.getResourceAsStream(resourceName);
        if (input == null) {
            fail("Couldn't resolve resource '" + resourceName + "'!");
        }
        inputFile = File.createTempFile("Import", "test");
        inputFile.delete();
        FileOutputStream output = new FileOutputStream(inputFile);
        IOUtils.copyLarge(input, output);
        IOUtilities.closeQuietly(output);
    }
}