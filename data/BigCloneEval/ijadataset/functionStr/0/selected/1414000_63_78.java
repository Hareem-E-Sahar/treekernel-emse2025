public class Test {            public void stateChanged(final ChangeEvent e) {
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
}