public class Test {    public AddFeedDialog(final Window parent, final String linkOrNull, final FeedsFS fs) {
        super(parent, fs.getNewFileActionText(), "ui/feed", STANDARD_DIALOG);
        updateButtons(false);
        mainPanel = new MainPanel(false, linkOrNull);
        mainPanel.feedURLTextField.getDocument().addDocumentListener(new MDocumentAdapter<MTextField>() {

            @Override
            protected void onChange(final DocumentEvent e) {
                AddFeedDialog.this.updateButtons(false);
            }
        });
        getValidatorSupport().add(new NotEmptyValidator(mainPanel.feedURLTextField));
        MPanel previewPanel = MPanel.createBorderPanel(5);
        previewPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(_("Preview")), UI.createEmptyBorder(5)));
        preview = new FeedComponent(FeedComponent.Type.COMBO_BOX_AND_VIEWER);
        preview.addChangeListener(new ChangeListener() {

            public void stateChanged(final ChangeEvent e) {
                if ((preview == null) || preview.isActive()) return;
                if (preview.isCancelled() || preview.isError()) {
                    MText.setText(fileName, _("New Feed"));
                    newURL = null;
                    updateButtons(false);
                    updateComponents(true);
                } else {
                    MText.setText(fileName, preview.getTitle());
                    FeedComponent.ChannelInfo info = preview.getChannelInfo();
                    URL source = info.getFeed().getSource();
                    newURL = Objects.toString(source, null);
                    updateButtons(true);
                    updateComponents(true);
                }
            }
        });
        preview.setArchiveFlags(Archive.NO_ARCHIVE);
        previewPanel.addCenter(preview);
        fileName = new MTextField(_("New Feed"));
        fileName.setAutoCompletion("rename");
        getValidatorSupport().add(new FSHelper.NameValidator(fileName));
        Dimension maxMainPanelSize = new Dimension(MGroupLayout.DEFAULT_SIZE, MGroupLayout.PREFERRED_SIZE);
        getMainPanel().setGroupLayout(true).beginRows().addComponent(mainPanel, null, null, maxMainPanelSize).addComponent(previewPanel).beginColumns().addComponent(fileName, _("Name:")).end().end();
        setSize(UI.WindowSize.MEDIUM);
        installValidatorMessage();
    }
}