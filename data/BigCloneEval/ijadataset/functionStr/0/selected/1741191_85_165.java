public class Test {    protected void saveAs(ActionEvent event) {
        int ready = 1;
        while (ready == 1) {
            IChemModel model = jcpPanel.getChemModel();
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(jcpPanel.getCurrentWorkDirectory());
            JCPSaveFileFilter.addChoosableFileFilters(chooser);
            if (jcpPanel.getCurrentSaveFileFilter() != null) {
                for (int i = 0; i < chooser.getChoosableFileFilters().length; i++) {
                    if (chooser.getChoosableFileFilters()[i].getDescription().equals(jcpPanel.getCurrentSaveFileFilter().getDescription())) chooser.setFileFilter(chooser.getChoosableFileFilters()[i]);
                }
            }
            chooser.setFileView(new JCPFileView());
            if (jcpPanel.isAlreadyAFile() != null) chooser.setSelectedFile(jcpPanel.isAlreadyAFile());
            int returnVal = chooser.showSaveDialog(jcpPanel);
            IChemObject object = getSource(event);
            FileFilter currentFilter = chooser.getFileFilter();
            if (returnVal == JFileChooser.CANCEL_OPTION) {
                ready = 0;
                wasCancelled = true;
            }
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                if (!(currentFilter instanceof IJCPFileFilter)) {
                    JOptionPane.showMessageDialog(jcpPanel, GT._("Please chose a file type!"), GT._("No file type chosen"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                } else {
                    type = ((IJCPFileFilter) currentFilter).getType();
                    File outFile = chooser.getSelectedFile();
                    if (outFile.exists()) {
                        ready = JOptionPane.showConfirmDialog((Component) null, GT._("File") + " " + outFile.getName() + " " + GT._("already exists. Do you want to overwrite it?"), GT._("File already exists"), JOptionPane.YES_NO_OPTION);
                    } else {
                        try {
                            if (new File(outFile.getCanonicalFile() + "." + type).exists()) {
                                ready = JOptionPane.showConfirmDialog((Component) null, GT._("File") + " " + outFile.getName() + " " + GT._("already exists. Do you want to overwrite it?"), GT._("File already exists"), JOptionPane.YES_NO_OPTION);
                            }
                        } catch (Throwable ex) {
                            jcpPanel.announceError(ex);
                        }
                        ready = 0;
                    }
                    if (ready == 0) {
                        if (object == null) {
                            try {
                                if (type.equals(JCPSaveFileFilter.mol)) {
                                    outFile = saveAsMol(model, outFile);
                                } else if (type.equals(JCPSaveFileFilter.inchi)) {
                                    outFile = saveAsInChI(model, outFile);
                                } else if (type.equals(JCPSaveFileFilter.cml)) {
                                    outFile = saveAsCML2(model, outFile);
                                } else if (type.equals(JCPSaveFileFilter.smiles)) {
                                    outFile = saveAsSMILES(model, outFile);
                                } else if (type.equals(JCPSaveFileFilter.cdk)) {
                                    outFile = saveAsCDKSourceCode(model, outFile);
                                } else if (type.equals(JCPSaveFileFilter.rxn)) {
                                    outFile = saveAsRXN(model, outFile);
                                } else {
                                    String error = GT._("Cannot save file in this format:") + " " + type;
                                    logger.error(error);
                                    JOptionPane.showMessageDialog(jcpPanel, error);
                                    return;
                                }
                                jcpPanel.setModified(false);
                            } catch (Exception exc) {
                                String error = GT._("Error while writing file") + ": " + exc.getMessage();
                                logger.error(error);
                                logger.debug(exc);
                                JOptionPane.showMessageDialog(jcpPanel, error);
                            }
                        }
                        jcpPanel.setCurrentWorkDirectory(chooser.getCurrentDirectory());
                        jcpPanel.setCurrentSaveFileFilter(chooser.getFileFilter());
                        jcpPanel.setIsAlreadyAFile(outFile);
                        if (outFile != null) {
                            jcpPanel.getChemModel().setID(outFile.getName());
                            if (jcpPanel instanceof JChemPaintPanel) ((JChemPaintPanel) jcpPanel).setTitle(outFile.getName());
                        }
                    }
                }
            }
        }
    }
}