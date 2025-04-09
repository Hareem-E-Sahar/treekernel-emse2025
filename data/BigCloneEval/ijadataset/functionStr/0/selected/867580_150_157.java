public class Test {	public static boolean confirmFileOverwrite(final Window parent, final File file) {
		return customConfirm(
			parent, null,
			MActionInfo.OVERWRITE,
			null,
			UI.makeHTML(_("File already exists. Overwrite?") + makeInfo(file))
		);
	}
}