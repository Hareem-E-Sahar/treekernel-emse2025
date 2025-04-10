public class Test {    public static void writeDialogFileBytes(Component parent, byte[] data) {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(GUI.getTopParentContainer());
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File saveFile = fc.getSelectedFile();
        if (saveFile.exists()) {
            String s1 = "Overwrite";
            String s2 = "Cancel";
            Object[] options = { s1, s2 };
            int n = JOptionPane.showOptionDialog(GUI.getTopParentContainer(), "A file with the same name already exists.\nDo you want to remove it?", "Overwrite existing file?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, s1);
            if (n != JOptionPane.YES_OPTION) {
                return;
            }
        }
        if (saveFile.exists() && !saveFile.canWrite()) {
            logger.fatal("No write access to file: " + saveFile);
            return;
        }
        try {
            FileOutputStream outStream = new FileOutputStream(saveFile);
            outStream.write(data);
            outStream.close();
        } catch (Exception ex) {
            logger.fatal("Could not write to file: " + saveFile);
            return;
        }
    }
}