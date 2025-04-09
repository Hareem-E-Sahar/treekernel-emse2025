public class Test {    public static boolean confirmFileOverwrite(final Window parent, final String oldFileDisplayName, final File oldFile, final File newFile) {
        TK.checkNull(oldFile, "oldFile");
        String oldFileString = (oldFileDisplayName != null) ? _("Existing File ({0})", oldFileDisplayName) : _("Existing File");
        FileInfoPanel oldFileInfo = new FileInfoPanel(oldFile, oldFileString);
        FileInfoPanel newFileInfo = (newFile != null) ? (new FileInfoPanel(newFile, _("New File"))) : null;
        MDialog dialog = new MDialog(parent, _("Confirm"), MIcon.stock("ui/question"), MDialog.STANDARD_DIALOG);
        dialog.changeButton(dialog.getOKButton(), _("Overwrite"));
        dialog.addNorth(new MLabel(_("File already exists. Overwrite?")));
        MPanel p = MPanel.createVBoxPanel();
        p.addGap();
        p.add(oldFileInfo);
        p.addGap();
        if (newFileInfo != null) p.add(newFileInfo);
        dialog.addCenter(p);
        oldFileInfo.infoPanel.alignLabels();
        oldFileInfo.startPreview();
        if (newFileInfo != null) {
            newFileInfo.infoPanel.alignLabels();
            newFileInfo.startPreview();
        }
        dialog.pack();
        boolean result = dialog.exec();
        oldFileInfo.stopPreview();
        if (newFileInfo != null) newFileInfo.stopPreview();
        return result;
    }
}