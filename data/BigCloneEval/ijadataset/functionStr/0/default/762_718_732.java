public class Test {            public void actionPerformed(ActionEvent evt) {
                if (fc2.showSaveDialog(resultFrame) != JFileChooser.CANCEL_OPTION) {
                    File file = fc2.getSelectedFile();
                    if (file != null) {
                        try {
                            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                            writer.write(resultArea.getText());
                            writer.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            new ErrorHandler(ex);
                        }
                    }
                }
            }
}