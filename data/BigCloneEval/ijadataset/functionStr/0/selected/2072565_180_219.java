public class Test {    public void saveImage() {
        JFileChooser fc = new JFileChooser("./samples/");
        FileFilter ff = new FileFilter() {

            public String getDescription() {
                return "JPEG Images";
            }

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String extension = (f.getName().substring(f.getName().lastIndexOf('.'))).toLowerCase();
                if (extension != null) {
                    extension = extension.substring(1);
                    if (extension.equals("jpeg") || extension.equals("jpg")) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            }
        };
        fc.addChoosableFileFilter(ff);
        if (fc.showSaveDialog(UI) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file.exists()) {
                Object[] options = { "OK", "Cancel" };
                if (JOptionPane.showOptionDialog(UI, "File already exists: " + file.getName() + "\nDo you want to overwrite it?", "Warning!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]) == 1) {
                    return;
                }
            }
            try {
                displayManager3D.capture(file.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(UI, "Internal error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}