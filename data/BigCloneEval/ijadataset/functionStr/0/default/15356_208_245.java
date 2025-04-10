public class Test {    private void loadTreeItemActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == loadTreeItem) {
            int returnVal = fc.showOpenDialog(Demarcations.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                treeFile = fc.getSelectedFile();
                if (!TreeFinder.verifyTreeFile(treeFile)) {
                    log.append("That is not a valid tree file, please choose a valid newick tree file.\n");
                    return;
                }
            } else {
                log.append("Dialog cancelled by user. \n");
                return;
            }
            String message = "You must now choose the fasta file corresponding to the tree you just loaded, continue?";
            int option = JOptionPane.showConfirmDialog(null, message);
            if (option != JOptionPane.YES_OPTION) {
                log.append("Dialog cancelled by user.");
                return;
            }
            returnVal = fc.showOpenDialog(Demarcations.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                inputFile = fc.getSelectedFile();
                if (!BinningFasta.verifyInputFile(inputFile)) {
                    log.append("That is not a valid fasta file, please choose a properly formatted fasta file.\n");
                    return;
                }
                Thread thread = new Thread() {

                    public void run() {
                        runTree();
                    }
                };
                thread.start();
            } else {
                log.append("Dialog Cancelled by hser.\n");
            }
        }
    }
}