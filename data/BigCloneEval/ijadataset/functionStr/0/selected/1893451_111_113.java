public class Test {    static boolean isBrowseSupported() {
        return (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE));
    }
}