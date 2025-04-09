package wow.ui;

import javax.microedition.lcdui.*;
import wow.*;

public class GameMenu extends WoWscreen {

    private class MenuItem {

        public final String m_action;

        public final String m_text;

        public MenuItem(final String action, final String text) {
            m_action = action;
            m_text = text;
        }
    }

    private MenuItem m_menus[];

    private int m_width;

    private int m_selected;

    private int m_menuCount;

    private int m_menuY;

    public GameMenu() {
        m_width = 0;
        m_selected = 0;
        m_menus = null;
        m_menuCount = 0;
        m_menuY = 100;
    }

    private void drawItem(Graphics g, int x, int y, String text, boolean selected) {
        g.setColor(selected ? 0x808060 : 0x606060);
        g.fillRoundRect(x - 100, y - 14, 200, 24, 8, 8);
        if (selected) {
            g.setColor(0xffff00);
            g.drawRoundRect(x - 100, y - 14, 200, 24, 8, 8);
        }
        int h = g.getFont().getHeight();
        h = (h + 1) / 2;
        g.setColor(selected ? 0xffff00 : 0xc0c0c0);
        g.drawString(text, x - 96, y - h, Graphics.LEFT | Graphics.TOP);
    }

    private boolean acceptSel() {
        if (m_selected >= m_menuCount) return false;
        MenuItem it = m_menus[m_selected];
        if (it == null) return false;
        return WoWgame.self().jumpScreen(it.m_action);
    }

    public void activate(boolean active) {
        WoWgame.self().showDebug("GameMenu.activate() active=" + active);
    }

    public void sizeEvent(int width, int height) {
        WoWgame.self().showDebug("GameMenu.sizeEvent() " + width + " x " + height);
        m_width = width;
        int n = 7;
        m_menuCount = 0;
        if (m_selected >= n) m_selected = n - 1;
        m_menuY = (height - 40 - (28 * n)) / 2;
        if (m_menuY < 32) m_menuY = 32;
        m_menus = new MenuItem[n];
        m_menus[0] = new MenuItem("MapView", "View map");
        m_menus[1] = new MenuItem("Character", "Character information");
        m_menus[2] = new MenuItem("Inventory", "View inventory");
        m_menus[3] = new MenuItem("Spellbook", "Spell Book");
        m_menus[4] = new MenuItem("Talents", "View Talents");
        m_menus[5] = new MenuItem("QuestLog", "Quest Log");
        m_menus[6] = new MenuItem("SystemInfo", "System Information");
        m_menuCount = n;
    }

    public boolean paintEvent(Graphics g) {
        g.setColor(0xffffff);
        g.drawString("Game menu", m_width / 2, m_menuY, Graphics.HCENTER | Graphics.BOTTOM);
        for (int i = 0; i < m_menuCount; i++) {
            String pre = "  ";
            if (i < 9) pre = (i + 1) + " ";
            MenuItem it = m_menus[i];
            if (it != null) drawItem(g, m_width / 2, 28 * i + 32 + m_menuY, pre + it.m_text, (i == m_selected));
        }
        return false;
    }

    public boolean keyEvent(int key, int action, boolean pressed, boolean repeated) {
        WoWgame.self().showDebug("GameMenu.keyEvent() key=" + key + " act=" + action + " press=" + pressed);
        if (!repeated && (action != 0) && (key < 0)) {
            switch(action) {
                case Canvas.FIRE:
                    if (pressed) return acceptSel();
                    return true;
                case Canvas.UP:
                    if (pressed && m_selected > 0) m_selected--;
                    return true;
                case Canvas.DOWN:
                    if (pressed && m_selected < m_menuCount - 1) m_selected++;
                    return true;
            }
        }
        if (pressed && !repeated && key >= '0' && key <= '9') {
            int idx = key - '1';
            if (idx < 0) {
                WoWgame.self().popScreen();
                return true;
            }
            if (idx < m_menuCount) {
                m_selected = idx;
                return WoWgame.self().shifted() || acceptSel();
            }
        }
        return false;
    }

    public boolean ptrEvent(int x, int y, boolean pressed, boolean dragged) {
        if (pressed && !dragged) {
            int dx = x - (m_width / 2);
            if (dx >= -100 && dx <= 100) {
                int idx = y - 32 - m_menuY;
                if (idx >= 0) {
                    idx = idx / 28;
                    if (idx < m_menuCount) {
                        m_selected = idx;
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
