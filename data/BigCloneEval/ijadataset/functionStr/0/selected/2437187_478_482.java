public class Test {    private JMenu getChannelMenu() {
        JMenu menu = new JMenu(CHANNEL_MENU);
        menu.add(getAddChannelMenu());
        return menu;
    }
}