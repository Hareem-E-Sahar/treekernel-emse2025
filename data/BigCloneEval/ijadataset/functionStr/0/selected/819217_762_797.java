public class Test {            public void actionPerformed(ActionEvent e) {
                final DasCanvas canvas = DasCanvas.getFocusCanvas();
                final JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Print to " + ext.toUpperCase());
                fileChooser.setFileFilter(getFileNameExtensionFilter(ext + " files", ext));
                Preferences prefs = Preferences.userNodeForPackage(DasCanvas.class);
                String savedir = prefs.get("savedir", null);
                if (savedir != null) fileChooser.setCurrentDirectory(new File(savedir));
                if (currentFile != null) fileChooser.setSelectedFile(currentFile);
                int choice = fileChooser.showSaveDialog(parent);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    String fname = fileChooser.getSelectedFile().toString();
                    if (!fname.toLowerCase().endsWith("." + ext)) fname += "." + ext;
                    final String ffname = fname;
                    prefs.put("savedir", new File(ffname).getParent());
                    currentFile = new File(ffname.substring(0, ffname.length() - 4));
                    Runnable run = new Runnable() {

                        public void run() {
                            try {
                                if (ext.equals("png")) {
                                    canvas.writeToPng(ffname);
                                } else if (ext.equals("pdf")) {
                                    canvas.writeToPDF(ffname);
                                } else if (ext.equals("svg")) {
                                    canvas.writeToSVG(ffname);
                                }
                                app.getController().setStatus("wrote to " + ffname);
                            } catch (java.io.IOException ioe) {
                                org.das2.util.DasExceptionHandler.handle(ioe);
                            }
                        }
                    };
                    new Thread(run, "writePrint").start();
                }
            }
}