public class Test {    public PdfViewerMenuBar(ActionListener aActionListener, boolean aViewModeOnly) {
        JMenu tFileMenu = new JMenu(getLocaleString("FileMenu"));
        JMenu tViewMenu = new JMenu(getLocaleString("ViewMenu"));
        JMenu tHelpMenu = new JMenu(getLocaleString("HelpMenu"));
        iOpenFileMI = new JMenuItem(getLocaleString("OpenFileMI"));
        iCloseAppMI = new JMenuItem(getLocaleString("CloseAppMI"));
        iSaveFileMI = new JMenuItem(getLocaleString("SaveFileMI"));
        iSaveAsMI = new JMenuItem(getLocaleString("SaveAsMI"));
        iHighlightComponentsMI = new JCheckBoxMenuItem(getLocaleString("HighlightComponentsMI"));
        iShowLicenseMI = new JMenuItem(getLocaleString("ShowLicenseMI"));
        tFileMenu.setMnemonic(getLocaleString("FileMenu.Mnemonic").charAt(0));
        tViewMenu.setMnemonic(getLocaleString("ViewMenu.Mnemonic").charAt(0));
        iOpenFileMI.setMnemonic(getLocaleString("OpenFileMI.Mnemonic").charAt(0));
        iCloseAppMI.setMnemonic(getLocaleString("CloseAppMI.Mnemonic").charAt(0));
        iSaveFileMI.setMnemonic(getLocaleString("SaveFileMI.Mnemonic").charAt(0));
        iSaveAsMI.setMnemonic(getLocaleString("SaveAsMI.Mnemonic").charAt(0));
        iHighlightComponentsMI.setMnemonic(getLocaleString("HighlightComponentsMI.Mnemonic").charAt(0));
        iShowLicenseMI.setMnemonic(getLocaleString("ShowLicenseMI.Mnemonic").charAt(0));
        iCloseAppMI.setActionCommand("closeApp");
        iOpenFileMI.setActionCommand("openFile");
        iSaveFileMI.setActionCommand("saveFile");
        iSaveAsMI.setActionCommand("saveAs");
        iHighlightComponentsMI.setActionCommand("highlightComponents");
        iShowLicenseMI.setActionCommand("showLicense");
        iCloseAppMI.addActionListener(aActionListener);
        iOpenFileMI.addActionListener(aActionListener);
        iSaveFileMI.addActionListener(aActionListener);
        iSaveAsMI.addActionListener(aActionListener);
        iHighlightComponentsMI.addActionListener(aActionListener);
        iShowLicenseMI.addActionListener(aActionListener);
        tFileMenu.add(iOpenFileMI);
        tFileMenu.add(iSaveFileMI);
        tFileMenu.add(iSaveAsMI);
        tFileMenu.addSeparator();
        tFileMenu.add(iCloseAppMI);
        tViewMenu.add(iHighlightComponentsMI);
        tHelpMenu.add(iShowLicenseMI);
        add(tFileMenu);
        add(tViewMenu);
        add(tHelpMenu);
        if (aViewModeOnly) {
            iOpenFileMI.setVisible(false);
        }
    }
}