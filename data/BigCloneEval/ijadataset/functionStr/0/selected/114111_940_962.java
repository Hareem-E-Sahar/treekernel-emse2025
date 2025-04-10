public class Test {        public void actionPerformed(ActionEvent ae) {
            String name = null;
            int conf = JOptionPane.YES_OPTION;
            int overwrite = -1;
            if (dialogState == EDITED) {
                name = getConnectionName();
                overwrite = findProfile(name);
                if (overwrite >= 0) {
                    conf = JOptionPane.showConfirmDialog(instance, "Overwrite " + name + " profile?", "Profile already exists", JOptionPane.YES_NO_OPTION);
                }
            }
            if (conf == JOptionPane.YES_OPTION) {
                if (dialogState == EDITED) {
                    addToList(overwrite);
                    model = new MyComboBoxModel(profileList);
                    connNameCombo.setModel(model);
                    getConnNameTextfield().setText(name);
                }
                writeProfiles();
                setDialogState(SAVED);
                connNameCombo.requestFocus();
            }
        }
}