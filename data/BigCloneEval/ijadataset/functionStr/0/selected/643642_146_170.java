public class Test {    public void test_main_class_in_another_zip() throws Exception {
        File fooZip = File.createTempFile("hyts_", ".zip");
        File barZip = File.createTempFile("hyts_", ".zip");
        fooZip.deleteOnExit();
        barZip.deleteOnExit();
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        att.put(Attributes.Name.CLASS_PATH, fooZip.getName());
        File resources = Support_Resources.createTempFolder();
        ZipOutputStream zoutFoo = new ZipOutputStream(new FileOutputStream(fooZip));
        zoutFoo.putNextEntry(new ZipEntry("foo/bar/execjartest/Foo.class"));
        zoutFoo.write(getResource(resources, "hyts_Foo.ser"));
        zoutFoo.close();
        ZipOutputStream zoutBar = new ZipOutputStream(new FileOutputStream(barZip));
        zoutBar.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        man.write(zoutBar);
        zoutBar.putNextEntry(new ZipEntry("foo/bar/execjartest/Bar.class"));
        zoutBar.write(getResource(resources, "hyts_Bar.ser"));
        zoutBar.close();
        String[] args = new String[] { "-jar", barZip.getAbsolutePath() };
        String res = Support_Exec.execJava(args, null, false);
        assertTrue("Error executing JAR : result returned was incorrect.", res.startsWith("FOOBAR"));
    }
}