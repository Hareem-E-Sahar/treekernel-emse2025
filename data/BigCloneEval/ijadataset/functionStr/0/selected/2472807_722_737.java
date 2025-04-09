public class Test {    private boolean askOverride() {
        if (SAVE_DIALOG == getFileChooser().getDialogType()) {
            File selectedFile = getFileChooser().getSelectedFile();
            if (selectedFile == null) {
                return false;
            }
            if (selectedFile.exists()) {
                String head = _("A file named \"%s\" already exists.  Do you want to replace it?", selectedFile.getName());
                String foot = _("The file already exists in \"%s\".  Replacing it will overwrite its contents.", selectedFile.getParentFile().getName());
                String msg = "<html><p width='400px'>" + "<span style='font-weight: bold; font-size: 18pt;'>" + head + "</span></p><br /><p>" + foot + "</p></html>";
                int n = JOptionPane.showConfirmDialog(getFileChooser(), msg, "", JOptionPane.OK_CANCEL_OPTION);
                return n == JOptionPane.OK_OPTION;
            }
        }
        return true;
    }
}