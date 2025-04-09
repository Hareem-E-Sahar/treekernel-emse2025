package wow.ui;

import javax.microedition.lcdui.*;
import java.util.Random;
import wow.*;

public class CharSelect extends WoWscreen {

    private static final String s_music[] = { "Sacred01", "sacred02", "angelic01" };

    private WoWchar m_chars[];

    private int m_width;

    private int m_selected;

    private int m_charCount;

    private int m_charFirst;

    private int m_charSeen;

    private int m_charY;

    Image m_logo;

    public CharSelect() {
        m_width = 0;
        m_selected = 0;
        m_chars = null;
        m_charCount = 0;
        m_charFirst = 0;
        m_charSeen = 2;
        m_charY = 100;
        m_logo = null;
        try {
            m_logo = Image.createImage("/images/logo_wrath.png");
        } catch (Exception e) {
            e.printStackTrace();
            WoWgame.self().setDebug(e.toString());
        }
    }

    private void drawChar(Graphics g, int x, int y, WoWchar chr, int index) {
        boolean selected = (index == m_selected);
        g.setColor(selected ? 0x808060 : 0x606060);
        g.fillRoundRect(x - 100, y - 26, 200, 52, 8, 8);
        if (selected) {
            g.setColor(0xffff00);
            g.drawRoundRect(x - 100, y - 26, 200, 52, 8, 8);
        }
        if (chr.icon() != null) g.drawImage(chr.icon(), x + 96, y, Graphics.RIGHT | Graphics.VCENTER);
        String name = (index + 1) + " " + chr.name();
        if ((chr.m_flags & WoWchar.CHAR_GHOST) != 0) name += " (Ghost)";
        int h = g.getFont().getHeight();
        h = (h + 1) / 2;
        g.setColor(selected ? 0xffff00 : 0xc0c0c0);
        g.drawString(name, x - 96, y - 25, Graphics.LEFT | Graphics.TOP);
        g.setColor(selected ? 0x0000ff : 0x000000);
        g.drawString(chr.level(), x - 96, y - h, Graphics.LEFT | Graphics.TOP);
        g.drawString(chr.area(), x - 96, y + 25, Graphics.LEFT | Graphics.BOTTOM);
    }

    public void activate(boolean active) {
        System.err.println("CharSelect.activate() active=" + active);
        if (active) {
            Random rnd = new Random();
            int idx = rnd.nextInt(s_music.length);
            WoWgame.self().setBgPath("/sounds/music/moments/" + s_music[idx] + ".mp3");
        }
    }

    public void sizeEvent(int width, int height) {
        System.err.println("CharSelect.sizeEvent() " + width + " x " + height);
        m_width = width;
        int n = 5;
        m_charCount = 0;
        m_charFirst = 0;
        if (m_selected >= n) m_selected = n - 1;
        if (m_logo != null) m_charY = m_logo.getHeight() + 20; else m_charY = height - 40 - (56 * n);
        if (m_charY < 64) m_charY = 64;
        m_charSeen = (height - m_charY - 6) / 56;
        if (true || WoWgame.self().auth() == null) {
            m_chars = new WoWchar[n];
            m_chars[0] = new WoWchar("SenLeffai", "Level 15 Warrior", "Darnassis", "/images/abilities/Stealth.png");
            m_chars[1] = new WoWchar("Gouju", "Level 72 Hunter", "Durotar", "/images/abilities/Hunter_Pet_Cat.png");
            m_chars[2] = new WoWchar("Nemmah", "Level 12 Warlock", "Elwynn", "/images/abilities/Creature_Disease_05.png");
            m_chars[3] = new WoWchar("Hathor", "Level 2 Paladin", "AzuremystIsle", "/images/abilities/ThunderBolt.png");
            m_chars[4] = new WoWchar("Meana", "Level 1 Druid", "Teldrassil", "/images/abilities/Druid_FerociousBite.png");
            m_charCount = n;
            WoWgame.self().setDebug("Builtin chars: " + n + " (" + m_charSeen + ")");
        }
    }

    public boolean paintEvent(Graphics g) {
        if (m_logo != null) g.drawImage(m_logo, m_width / 2, 4, Graphics.HCENTER | Graphics.TOP);
        g.setColor(0xffffff);
        g.drawString(WoWgame.self().playerRealm(), m_width / 2, m_charY, Graphics.HCENTER | Graphics.BOTTOM);
        for (int i = m_charFirst; i < m_charCount; i++) {
            int n = i - m_charFirst;
            if (n >= m_charSeen) break;
            WoWchar c = m_chars[i];
            if (c != null) drawChar(g, m_width / 2, 56 * n + 32 + m_charY, c, i);
        }
        return false;
    }

    public boolean keyEvent(int key, int action, boolean pressed, boolean repeated) {
        System.err.println("CharSelect.keyEvent() key=" + key + " act=" + action + " press=" + pressed);
        if (!repeated && (action != 0) && (key < 0)) {
            switch(action) {
                case Canvas.FIRE:
                    if (pressed && (m_selected < m_charCount)) {
                        WoWgame.self().setCharacter(m_chars[m_selected]);
                        if (m_chars[m_selected] != null && m_chars[m_selected].guid() != 0 && WoWgame.self().conn() != null) {
                            WoWgame.self().conn().writePacket(WoWpacket.CMSG_PLAYER_LOGIN, m_chars[m_selected].guid());
                        }
                        WoWgame.self().jumpScreen("EnterWorld");
                    }
                    return true;
                case Canvas.UP:
                    if (pressed && m_selected > 0) {
                        m_selected--;
                        if (m_charFirst > m_selected) m_charFirst = m_selected;
                    }
                    return true;
                case Canvas.DOWN:
                    if (pressed && m_selected < m_charCount - 1) {
                        m_selected++;
                        if (m_selected >= (m_charFirst + m_charSeen)) m_charFirst = m_selected - m_charSeen + 1;
                    }
                    return true;
            }
        }
        if (pressed && !repeated && key >= '1' && key <= '9') {
            int idx = key - '1';
            if (idx < m_charCount) {
                m_selected = idx;
                if (m_charFirst > m_selected) m_charFirst = m_selected; else if (m_selected >= (m_charFirst + m_charSeen)) m_charFirst = m_selected - m_charSeen + 1;
                return true;
            }
        }
        return false;
    }

    public boolean ptrEvent(int x, int y, boolean pressed, boolean dragged) {
        if (pressed && !dragged) {
            int dx = x - (m_width / 2);
            if (dx >= -100 && dx <= 100) {
                int idx = y - 32 - m_charY;
                if (idx >= 0) {
                    idx = (idx / 56) + m_charFirst;
                    if (idx < m_charCount) {
                        m_selected = idx;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean netEvent(WoWpacket pkt) {
        switch(pkt.code()) {
            case WoWpacket.SMSG_AUTH_RESPONSE:
                if (WoWgame.self().conn() != null) WoWgame.self().conn().writePacket(WoWpacket.CMSG_CHAR_ENUM);
                return true;
            case WoWpacket.SMSG_CHAR_ENUM:
                int n = pkt.getByte();
                if (n > 0) {
                    WoWchar chars[] = new WoWchar[n];
                    for (int i = 0; i < n; i++) chars[i] = parseChar(pkt);
                    if (!pkt.eof()) return false;
                    m_selected = 0;
                    m_charCount = 0;
                    m_chars = chars;
                    m_charCount = n;
                    WoWgame.self().setDebug("Server chars: " + n + " (" + m_charSeen + ")");
                }
                return true;
        }
        return false;
    }

    private WoWchar parseChar(WoWpacket pkt) {
        long guid = pkt.getLong();
        String name = pkt.getString();
        int race = pkt.getByte();
        int cls = pkt.getByte();
        int gender = pkt.getByte();
        pkt.skip(5);
        int level = pkt.getByte();
        int zone = pkt.getInt();
        int map = pkt.getInt();
        pkt.skip(16);
        int flags = pkt.getInt();
        pkt.skip(224);
        String area = null;
        WoWwdbc wma = WoWwdbc.cached("resource:/dbc/WorldMapArea.dbc");
        if (wma != null) {
            int idx = wma.getRecord(2, zone);
            area = wma.getString(3, idx);
        }
        if (area == null) area = "Darnassis";
        System.err.println("Character: " + guid + " '" + name + "' zone " + zone + " map " + map);
        WoWchar c = new WoWchar(name, "Level " + level + " " + WoWchar.textRace(race) + " " + WoWchar.textClass(cls), area, "/images/abilities/Stealth.png");
        WoWwdbc cc = WoWwdbc.cached("resource:/dbc/ChrClasses.dbc");
        if (cc != null) {
            int idx = cc.getRecord(0, cls);
            System.err.println("Class found at index " + idx + " names=" + cc.getString(4, idx) + "," + cc.getString(21, idx) + "," + cc.getString(38, idx) + " internal=" + cc.getString(55, idx));
        }
        WoWwdbc cr = WoWwdbc.cached("resource:/dbc/ChrRaces.dbc");
        if (cr != null) {
            int idx = cr.getRecord(0, race);
            System.err.println("Race found at index " + idx + " names=" + cr.getString(14, idx) + "," + cr.getString(31, idx) + "," + cr.getString(48, idx) + " internal=" + cr.getString(11, idx));
        }
        c.m_guid = guid;
        c.m_race = race;
        c.m_class = cls;
        c.m_gender = gender;
        c.m_flags = flags;
        return c;
    }
}
