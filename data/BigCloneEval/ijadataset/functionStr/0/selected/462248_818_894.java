public class Test {    private void actionExtractToDiskImage() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setMultiSelectionEnabled(false);
        SimplerFileFilter ffDmg = new SimplerFileFilter(".dmg", "Mac OS X read/write disk image (.dmg)");
        jfc.setFileFilter(ffDmg);
        if (jfc.showSaveDialog(FileSystemBrowserWindow.this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            final File saveFile;
            FileFilter selectedFileFilter = jfc.getFileFilter();
            if (selectedFileFilter instanceof SimplerFileFilter) {
                SimplerFileFilter sff = (SimplerFileFilter) selectedFileFilter;
                if (!selectedFile.getName().endsWith(sff.getExtension())) saveFile = new File(selectedFile.getParentFile(), selectedFile.getName() + sff.getExtension()); else saveFile = selectedFile;
            } else {
                saveFile = selectedFile;
            }
            if (saveFile.exists()) {
                int res = JOptionPane.showConfirmDialog(this, "The file:\n  " + saveFile.getPath() + "\nAlready exists. Do you want to overwrite?", "Confirm overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (res != JOptionPane.YES_OPTION) return;
            }
            final javax.swing.ProgressMonitor pm = new javax.swing.ProgressMonitor(FileSystemBrowserWindow.this, "Extracting file system data to disk image...", "Starting extraction...", 0, Integer.MAX_VALUE);
            pm.setMillisToDecideToPopup(0);
            pm.setMillisToPopup(0);
            pm.setProgress(0);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    ReadableRandomAccessStream fsStream = fsDataLocator.createReadOnlyFile();
                    FileOutputStream fileOut = null;
                    try {
                        fileOut = new FileOutputStream(saveFile);
                        DecimalFormat df = new DecimalFormat("0.00");
                        CommonHFSVolumeHeader vh = fsHandler.getFSView().getVolumeHeader();
                        final long bytesToExtract = vh.getFileSystemEnd();
                        String bytesToExtractString = SpeedUnitUtils.bytesToBinaryUnit(bytesToExtract, df);
                        long lastUpdateTimestamp = 0;
                        long bytesExtracted = 0;
                        byte[] buffer = new byte[64 * 1024];
                        while (bytesExtracted < bytesToExtract && !pm.isCanceled()) {
                            long bytesLeftToRead = bytesToExtract - bytesExtracted;
                            int curBytesToRead = (int) (bytesLeftToRead < buffer.length ? bytesLeftToRead : buffer.length);
                            int bytesRead = fsStream.read(buffer, 0, curBytesToRead);
                            if (bytesRead > 0) {
                                fileOut.write(buffer, 0, bytesRead);
                                bytesExtracted += bytesRead;
                                long currentTimestamp = System.currentTimeMillis();
                                long millisSinceLastUpdate = currentTimestamp - lastUpdateTimestamp;
                                if (millisSinceLastUpdate >= 40) {
                                    pm.setProgress((int) ((bytesExtracted / (double) bytesToExtract) * Integer.MAX_VALUE));
                                    pm.setNote("Extracted " + SpeedUnitUtils.bytesToBinaryUnit(bytesExtracted, df) + " / " + bytesToExtractString + " ...");
                                    lastUpdateTimestamp = currentTimestamp;
                                }
                            } else {
                                throw new RuntimeException("Unexpectedly reached end of file!" + " fp=" + fsStream.getFilePointer() + " length=" + fsStream.length() + " bytesExtracted=" + bytesExtracted + " bytesToExtract=" + bytesToExtract);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        GUIUtil.displayExceptionDialog(e, 15, FileSystemBrowserWindow.this, "Exception while extracting data!");
                    } finally {
                        pm.close();
                        try {
                            fsStream.close();
                            if (fileOut != null) {
                                fileOut.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            GUIUtil.displayExceptionDialog(e, FileSystemBrowserWindow.this);
                        }
                    }
                }
            };
            new Thread(r).start();
        }
    }
}