public class Test {    public SaveFileExistsException(String filename) {
        super("The file " + filename + " already exists.  Would you like to overwrite it?");
    }
}