public class Test {    @Test
    public void test() {
        IExtractorInputReader reader = new DumyExtractorInputReader();
        reader.getExtractorRegister3D().add(DumyExtractorInputReader.createExtractorVector("test1"));
        File file = new File("./target/test.csv");
        List<File> files = wekaArffDaoImpl.write(reader, file);
        Assert.assertTrue("Exists", files.get(0).exists());
    }
}