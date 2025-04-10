public class Test {    public boolean validateTraitName(String traitName) {
        if (traitName.trim().length() == 0) {
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        if (traitName.equalsIgnoreCase("date")) {
            JOptionPane.showMessageDialog(this, "This trait name has a special meaning. Use the 'Tip Date' panel\n" + " to set dates for taxa.", "Reserved trait name", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (options.useStarBEAST && traitName.equalsIgnoreCase(TraitData.TRAIT_SPECIES)) {
            JOptionPane.showMessageDialog(this, "This trait name is already in used to denote species\n" + "for *BEAST. Please select a different name.", "Reserved trait name", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (options.traitExists(traitName)) {
            int option = JOptionPane.showConfirmDialog(this, "A trait of this name already exists. Do you wish to replace\n" + "it with this new trait? This may result in the loss or change\n" + "in trait values for the taxa.", "Overwrite trait?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option == JOptionPane.NO_OPTION) {
                return false;
            }
        }
        return true;
    }
}