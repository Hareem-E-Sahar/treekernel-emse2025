public class Test {    public File projectFile(String filename) {
        File file = new File(getProjectFolder(), filename);
        if (!overwrite && file.exists()) throw new I18NError("FileAlreadyExists", null, filename);
        return file;
    }
}