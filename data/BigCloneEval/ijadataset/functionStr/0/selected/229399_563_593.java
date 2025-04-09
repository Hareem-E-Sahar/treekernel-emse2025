public class Test {            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
                fileDialog.setFilterPath(Activator.getDefault().getPreferenceStore().getString("GENERAL_WORKSPACE"));
                fileDialog.setText(_("Select a Picture"));
                String selectedFile = fileDialog.open();
                if (selectedFile != null) {
                    createPictureName();
                    File directory = new File(picturePath);
                    if (!directory.exists()) directory.mkdirs();
                    File inputFile = new File(selectedFile);
                    File outputFile = new File(filename1 + filename2);
                    try {
                        FileOutputStream out = new FileOutputStream(outputFile);
                        FileInputStream ins = new FileInputStream(inputFile);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        int c;
                        while ((c = ins.read()) != -1) {
                            byteArrayOutputStream.write((byte) c);
                        }
                        out.write(byteArrayOutputStream.toByteArray());
                        byteArrayOutputStream.close();
                        ins.close();
                        out.close();
                    } catch (IOException e1) {
                        Logger.logError(e1, "Error copying picture from " + selectedFile + " to " + filename1 + filename2);
                    }
                    setPicture();
                    checkDirty();
                }
            }
}