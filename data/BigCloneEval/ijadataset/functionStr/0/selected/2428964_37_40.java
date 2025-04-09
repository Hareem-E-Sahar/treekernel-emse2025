public class Test {    @Override
    public void copyFile(String srcFile, String destFile) {
        DCFileUtils.copyFile(srcFile, destFile, true);
    }
}