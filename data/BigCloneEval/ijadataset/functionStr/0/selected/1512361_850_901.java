public class Test {    public ListSelectionListener createListSelectionListener(JFileChooser fc) {
        return new SelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    JFileChooser chooser = getFileChooser();
                    FileSystemView fsv = chooser.getFileSystemView();
                    JList list = (JList) e.getSource();
                    if (chooser.isMultiSelectionEnabled()) {
                        File[] files = null;
                        Object[] objects = list.getSelectedValues();
                        if (objects != null) {
                            if (objects.length == 1 && ((File) objects[0]).isDirectory() && chooser.isTraversable(((File) objects[0])) && (chooser.getFileSelectionMode() == JFileChooser.FILES_ONLY || !fsv.isFileSystem(((File) objects[0])))) {
                                setDirectorySelected(true);
                                setDirectory(((File) objects[0]));
                            } else {
                                files = new File[objects.length];
                                int j = 0;
                                for (int i = 0; i < objects.length; i++) {
                                    File f = (File) objects[i];
                                    if ((chooser.isFileSelectionEnabled() && f.isFile()) || (chooser.isDirectorySelectionEnabled() && fsv.isFileSystem(f) && f.isDirectory())) {
                                        files[j++] = f;
                                    }
                                }
                                if (j == 0) {
                                    files = null;
                                } else if (j < objects.length) {
                                    File[] tmpFiles = new File[j];
                                    System.arraycopy(files, 0, tmpFiles, 0, j);
                                    files = tmpFiles;
                                }
                                setDirectorySelected(false);
                            }
                        }
                        chooser.setSelectedFiles(files);
                    } else {
                        File file = (File) list.getSelectedValue();
                        if (file != null && file.isDirectory() && chooser.isTraversable(file) && (chooser.getFileSelectionMode() == JFileChooser.FILES_ONLY || !fsv.isFileSystem(file))) {
                            setDirectorySelected(true);
                            setDirectory(file);
                            chooser.setSelectedFile(null);
                        } else {
                            setDirectorySelected(false);
                            if (file != null) {
                                chooser.setSelectedFile(file);
                            }
                        }
                    }
                }
            }
        };
    }
}