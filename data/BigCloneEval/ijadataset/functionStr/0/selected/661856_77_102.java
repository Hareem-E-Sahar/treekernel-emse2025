public class Test {    private int computeIndex(DockingPanel panel) {
        if (tab.getTabCount() == 0) return 0;
        if (panel instanceof IRCSessionPanel) {
            return tab.getTabCount();
        } else if (panel instanceof IRCChatPanel) {
            int index = 0;
            boolean session = false;
            Component c = null;
            IRCChatPanel p = (IRCChatPanel) panel;
            for (index = 0; index < tab.getTabCount(); index++) {
                c = tab.getComponentAt(index);
                if (c instanceof IRCSessionPanel) {
                    if (((IRCSessionPanel) c).getSession() == p.getChannel().getParentSession()) {
                        session = true;
                    } else {
                        if (session) return index;
                    }
                } else if (c instanceof IRCChatPanel) {
                    if (((IRCChatPanel) c).getChannel().getName().compareTo(p.getChannel().getName()) > 0 && session) {
                        return index;
                    }
                }
            }
        }
        return tab.getTabCount();
    }
}