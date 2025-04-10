public class Test {                @Override
                public void approveSelection() {
                    File f = getSelectedFile();
                    if (!f.getName().endsWith("map")) {
                        f = new File(f.toString() + ".map");
                    }
                    if (f.exists() && getDialogType() == SAVE_DIALOG) {
                        int result = JOptionPane.showConfirmDialog(getTopLevelAncestor(), "The selected file already exists. " + "Do you want to overwrite it?", "The file already exists", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                        switch(result) {
                            case JOptionPane.YES_OPTION:
                                super.approveSelection();
                                return;
                            case JOptionPane.NO_OPTION:
                                return;
                            case JOptionPane.CANCEL_OPTION:
                                cancelSelection();
                                return;
                        }
                    }
                    super.approveSelection();
                }
}