public class Test {    public static boolean confirmFileOverwrite(final Window parent, final String path) {
        return customConfirm(parent, null, MActionInfo.OVERWRITE, null, UI.makeHTML(_("File already exists. Overwrite?") + makeInfo(path)));
    }
}