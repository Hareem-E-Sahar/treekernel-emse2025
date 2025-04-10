public class Test {    public void testLevelWithLinkToFileWereFileIsInLog() throws Exception {
        report.startLevel("FileLevel", EnumReportLevel.CurrentPlace);
        FileUtils.write("myFileinRootLog.txt", "file message");
        File f = new File("myFileinRootLog.txt");
        File destination = new File(new File(report.getCurrentTestFolder()).getParent(), f.getName());
        FileUtils.copyFile(f, destination);
        report.addLink("file", f.getName());
        report.stopLevel();
    }
}