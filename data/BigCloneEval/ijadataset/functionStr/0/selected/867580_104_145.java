public class Test {	public static boolean confirmFileOverwrite(final Window parent, final String oldFileDisplayName, final File oldFile, final File newFile) {
		Objects.requireNonNull(oldFile);
		
		FileInfoPanel oldFileInfo = new FileInfoPanel(oldFile, _("Existing File"));
		FileInfoPanel newFileInfo = (newFile != null) ? (new FileInfoPanel(newFile, _("New File"))) : null;
		
		MDialog dialog = new MDialog(
			parent,
			_("File already exists. Overwrite?"),
			MIcon.stock("ui/question"),
			MDialog.STANDARD_DIALOG
		);
		dialog.changeButton(dialog.getOKButton(), _("Overwrite"), "ui/warning");
		
		MPanel p = dialog.getMainPanel();
		p.setVBoxLayout();
		
		if (oldFileDisplayName != null)
			oldFileInfo.addInfo(_("Name:"), oldFileDisplayName);
		p.add(oldFileInfo);
		
		if (newFileInfo != null) {
			p.addContentGap();
			p.add(newFileInfo);
		}
		
		oldFileInfo.alignLabels();
		oldFileInfo.startPreview();
		if (newFileInfo != null) {
			newFileInfo.alignLabels();
			newFileInfo.startPreview();
		}
		
		dialog.pack();
		boolean result = dialog.exec();
		
		oldFileInfo.stopPreview();
		if (newFileInfo != null)
			newFileInfo.stopPreview();
		
		return result;
	}
}