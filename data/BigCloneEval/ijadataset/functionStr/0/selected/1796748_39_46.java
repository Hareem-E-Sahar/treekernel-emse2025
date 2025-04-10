public class Test {    @Test
    public void writeToJarThenRereadFromJarAndEnsureContentIsEqual() throws IOException {
        ArchiveScriptLocation originalScriptArchive = new ArchiveScriptLocation(scripts, "ISO-8859-1", "postprocessing", asSet(new Qualifier("qualifier1"), new Qualifier("qualifier2")), singleton(new Qualifier("patch")), "#", "@", asSet("sql", "ddl"), null, false);
        originalScriptArchive.writeToJarFile(jarFile);
        ArchiveScriptLocation scriptArchiveFromFile = new ArchiveScriptLocation(jarFile, "ISO-8859-1", "postprocessing", asSet(new Qualifier("qualifier1"), new Qualifier("qualifier2")), singleton(new Qualifier("patch")), "#", "@", asSet("sql", "ddl"), null, false);
        assertEqualProperties(originalScriptArchive, scriptArchiveFromFile);
        assertEqualScripts(originalScriptArchive.getScripts(), scriptArchiveFromFile.getScripts());
    }
}