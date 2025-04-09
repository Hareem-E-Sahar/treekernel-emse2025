package net.alinnistor.nk.visual;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.alinnistor.nk.domain.Context;
import net.alinnistor.nk.domain.Message;
import net.alinnistor.nk.domain.SerFile;
import net.alinnistor.nk.domain.Status;
import net.alinnistor.nk.domain.User;
import net.alinnistor.nk.domain.persistence.TextSave;
import net.alinnistor.nk.service.Encryption;
import net.alinnistor.nk.service.NKExpress;
import net.alinnistor.nk.service.network.Executable;
import net.alinnistor.nk.visual.bricks.DiscussionRenderer;
import net.alinnistor.nk.visual.bricks.JConfigFrame;
import net.alinnistor.nk.visual.bricks.JTextFrame;
import net.alinnistor.nk.visual.bricks.LookAndFeel;
import net.alinnistor.nk.visual.bricks.NKTab;
import snoozesoft.systray4j.SysTrayMenu;
import snoozesoft.systray4j.SysTrayMenuEvent;
import snoozesoft.systray4j.SysTrayMenuIcon;
import snoozesoft.systray4j.SysTrayMenuItem;
import snoozesoft.systray4j.SysTrayMenuListener;
import com.sun.awt.AWTUtilities;

/**
 * @author <a href="mailto:nad7ir@yahoo.com">Alin NISTOR</a>
 */
public class ListAndDiscCtrl extends MouseAdapter implements SysTrayMenuListener, ActionListener {

    public static final int MIN_KEYSTROKES = 1;

    private int keystorkes = 0;

    private ListWind uimain;

    private DiscussionWind uiwind;

    private NKExpress nke;

    private TrayIcon trayIcon;

    private DiscussionRenderer discRend;

    public ListAndDiscCtrl(NKExpress nne) {
        nke = nne;
        uimain = new ListWind(this);
        uiwind = new DiscussionWind(this, uimain);
        nke.addChangeListener(uimain);
        nke.addChangeListener(uiwind);
        uimain.setVisible(true);
        Context.setWind(uiwind);
        Context.listWind = uimain;
        discRend = new DiscussionRenderer();
        addTryOptions(new String[] { "help", Context.HELP_COMMAND }, new String[] { "configure", Context.CONFIGURATION_COMMAND }, new String[] { "look & feel", Context.LOOKFEEL_COMMAND }, new String[] { "change passkey", Context.PASSKEY_COMMAND }, new String[] { "open saved", Context.OPEN_SAVED_COMMAND }, new String[] { "restart", Context.RESTART_COMMAND }, new String[] { "refresh list", Context.REFRESH_LIST }, new String[] { "nk list", Context.LIST_COMMAND }, new String[] { "exit", Context.EXIT_COMMAND });
        uimain.list.requestFocus();
    }

    private void addTryOptions(String[]... options) {
        if (!SystemTray.isSupported()) {
            try {
                SysTrayMenuIcon icon = new SysTrayMenuIcon("img/nk.ico");
                icon.addSysTrayMenuListener(this);
                SysTrayMenu menu = new SysTrayMenu(icon, "Why ? Cause .. You Never Know");
                for (String[] option : options) {
                    String text = option[0];
                    String command = option[1];
                    SysTrayMenuItem item = new SysTrayMenuItem(text, command);
                    item.addSysTrayMenuListener(this);
                    menu.addItem(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SystemTray tray = SystemTray.getSystemTray();
            PopupMenu popup = new PopupMenu();
            for (String[] option : options) {
                String text = option[0];
                String command = option[1];
                MenuItem item = new MenuItem(text);
                item.setActionCommand(command);
                item.addActionListener(this);
                popup.add(item);
            }
            trayIcon = new TrayIcon(Context.GREEN_CLOVER_ICON, "Why ? Cause .. You Never Know", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(this);
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON3 && e.getClickCount() == 2) {
            iconLeftDoubleClicked(null);
        } else if (e.getButton() != MouseEvent.BUTTON3) {
            iconLeftClicked(null);
        }
    }

    @Override
    public void iconLeftClicked(SysTrayMenuEvent e) {
        if (uiwind.isVisible()) {
            uiwind.setVisible(false);
        } else {
            uiwind.setVisible(true);
        }
    }

    @Override
    public void iconLeftDoubleClicked(SysTrayMenuEvent e) {
        uiwind.setVisible(false);
        if (uimain.isVisible()) {
            uimain.setVisible(false);
        } else {
            uimain.setVisible(true);
        }
    }

    @Override
    public void menuItemSelected(SysTrayMenuEvent e) {
        actionPerformed(new ActionEvent(this, 77, e.getActionCommand()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(Context.RESTART_COMMAND)) {
            nke.stopNK();
            System.exit(177);
        }
        if (e.getActionCommand().equals(Context.EXIT_COMMAND)) {
            nke.stopNK();
            System.exit(0);
        }
        if (e.getActionCommand().equals(Context.MESSAGE)) {
            User user = (User) e.getSource();
            user = syncViewUser(user);
            String text = (String) user.getCliProp(User.LAST_MSG);
            try {
                Object[] objs = discRend.parseText(text);
                String message = (String) objs[0];
                Map<String, SerFile> imgmap = (Map<String, SerFile>) objs[1];
                nke.sendPlainTextMessage(user, message, imgmap);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (e.getActionCommand().equals(Context.DOUBLE_CLICK)) {
            User user = (User) e.getSource();
            user = syncViewUser(user);
            DiscussionPanel dsp = uiwind.getDiscussionPanelForUser(user);
            if (dsp == null) {
                if (uiwind.removed.get(user) == null) {
                    dsp = new DiscussionPanel(user, this, uiwind);
                    dsp.putClientProperty(Context.NK_TAB, new NKTab(uiwind, dsp));
                } else {
                    dsp = uiwind.removed.get(user);
                }
                uiwind.addDiscussionPanel(dsp);
            }
            uiwind.setVisible(true);
            uimain.setVisible(false);
            dsp.requestFocus();
        }
        if (e.getActionCommand().equals(Context.SIMPLE_CLICK)) {
            User user = (User) e.getSource();
            user = syncViewUser(user);
            DiscussionPanel dsp = uiwind.getDiscussionPanelForUser(user);
            if (dsp != null) {
                uiwind.jtabs.setSelectedComponent(dsp);
                dsp.requestFocus();
            }
        }
        if (e.getActionCommand().equals(Context.PASSKEY_COMMAND)) {
            String passkey = JOptionPane.showInputDialog(uiwind, "Enter new passkey : ");
            if (passkey != null) {
                Encryption encryption = new Encryption(passkey);
                nke.setNewEncription(encryption);
            }
        }
        if (e.getActionCommand().equals(Context.CONFIGURATION_COMMAND)) {
            JConfigFrame jcf = new JConfigFrame(Context.xml);
        }
        if (e.getActionCommand().equals(Context.SAVE_COMMAND)) {
            DiscussionPanel dsp = uiwind.getSelectedDiscussionPanel();
            if (dsp != null) {
                TextSave ts = new TextSave(dsp.jedit.getText());
                ts.formatAsHTML();
                ts.chooseFile("/", ".html");
                ts.executeSave();
            }
        }
        if (e.getActionCommand().equals(Context.CHANGE_ENVIRONMENT)) {
            Object[] objs = (Object[]) e.getSource();
            User user = (User) objs[0];
            user = syncViewUser(user);
            Color[] colors = (Color[]) objs[1];
            nke.sendObject(user, colors);
        }
        if (e.getActionCommand().equals(Context.SEND_FILE)) {
            Object[] objs = (Object[]) e.getSource();
            User user = (User) objs[0];
            user = syncViewUser(user);
            File[] files = (File[]) objs[1];
            Executable callback = (Executable) objs[2];
            nke.sendFiles(user, files, callback, false);
        }
        if (e.getActionCommand().equals(Context.SEND_FILE_EMBEDDED)) {
            Object[] objsr = (Object[]) e.getSource();
            User user = (User) objsr[0];
            user = syncViewUser(user);
            File file = (File) objsr[1];
            Object[] objs = discRend.prepareImageToSendEmbedded(file);
            String message = (String) objs[0];
            Map<String, SerFile> imgmap = (Map<String, SerFile>) objs[1];
            nke.sendPlainTextMessage(user, message, imgmap);
            if (file != null) {
                file.delete();
            }
        }
        if (e.getActionCommand().equals(Context.GET_FILE)) {
            User user = (User) e.getSource();
            user = syncViewUser(user);
            nke.sendMessage(user, "7*", Message.GET_FILE);
        }
        if (e.getActionCommand().equals(Context.OPEN_SAVED_COMMAND)) {
            TextSave ts = new TextSave("");
            ts.chooseFile("/", null);
            String text = ts.executeRestore();
            if (text != null) {
                new JTextFrame(text);
            }
        }
        if (e.getActionCommand().equals(Context.TRANSLUCENCY_COMMAND)) {
            final JSlider jsl = new JSlider();
            jsl.setValue(100);
            jsl.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    AWTUtilities.setWindowOpacity(uiwind, (float) ((float) jsl.getValue() / (float) 100));
                }
            });
            JOptionPane.showMessageDialog(uiwind, jsl, "Select Translucency", JOptionPane.QUESTION_MESSAGE);
        }
        if (e.getActionCommand().equals(Context.LOOKFEEL_COMMAND)) {
            new LookAndFeel(uiwind, uimain);
        }
        if (e.getActionCommand().equals(Context.FONT_CHANGED)) {
            User user = (User) e.getSource();
            user = syncViewUser(user);
            Font font = (Font) user.getCliProp(User.FONT_PROPERTY);
            nke.sendObject(user, font);
        }
        if (e.getActionCommand().equals(Context.BUZZ)) {
            User user = (User) e.getSource();
            user = syncViewUser(user);
            nke.sendQuickMessage(user.getSa(), Message.BUZZ);
        }
        if (e.getActionCommand().equals(Context.REMOTE_HIDE)) {
            User user = (User) e.getSource();
            user = syncViewUser(user);
            nke.sendQuickMessage(user.getSa(), Message.HIDE);
        }
        if (e.getActionCommand().equals(Context.REMOTEWIND_HIDE)) {
            User user = (User) e.getSource();
            user = syncViewUser(user);
            nke.sendQuickMessage(user.getSa(), Message.HIDEWIND);
        }
        if (e.getActionCommand().equals(Context.REMOTE_ERASE)) {
            User user = (User) e.getSource();
            user = syncViewUser(user);
            nke.sendQuickMessage(user.getSa(), Message.REMOTE_ERASE);
        }
        if (e.getActionCommand().equals(Context.ONLINE_CHECK)) {
            Boolean active = (Boolean) e.getSource();
        }
        if (e.getActionCommand().equals(Context.REFRESH_LIST)) {
            nke.broadcastIAmAlive();
        }
        if (e.getActionCommand().equals(Context.SET_STATUS)) {
            User us = (User) e.getSource();
            Status status = (Status) us.getCliProp(User.STATUS_TO_SEND);
            Object[] usersobj = null;
            usersobj = nke.getOnline().toArray();
            for (Object ojs : usersobj) {
                User user = (User) ojs;
                user = syncViewUser(user);
                nke.sendObject(user, status);
            }
        }
        if (e.getActionCommand().equals(Context.HELP_COMMAND)) {
            TextSave ts = new TextSave("", "html/help.html");
            String text = ts.executeRestore();
            new JTextFrame(text);
        }
        if (e.getActionCommand().equals(Context.LIST_COMMAND)) {
            if (uimain != null) {
                uimain.setVisible(true);
            }
        }
    }

    private User syncViewUser(User user) {
        User newUser = nke.getCurrentUserObject(user);
        if (newUser != null) {
            user.putCliProp(User.NIOSRVADDRESS, newUser.getCliProp(User.NIOSRVADDRESS));
        }
        return user;
    }

    public void textAreaChanged(int length, User user) {
        user = syncViewUser(user);
        if (++keystorkes >= MIN_KEYSTROKES) {
            keystorkes = 0;
            nke.sendQuickMessage(user.getSa(), Message.KEYSTROKE, length);
        }
    }

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }

    public void setTrayIcon(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }
}
