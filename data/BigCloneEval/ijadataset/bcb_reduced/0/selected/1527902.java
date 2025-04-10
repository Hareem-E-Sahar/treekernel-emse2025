package lisppaste;

import javax.swing.border.EmptyBorder;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.List;
import java.util.StringTokenizer;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;

public class PasteDialog extends EnhancedDialog {

    public PasteDialog(View view, String paste) {
        super(view, jEdit.getProperty("lisp-paste.dialog-title"), true);
        this.view = view;
        JPanel content = new JPanel(new BorderLayout(12, 0));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(content);
        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints labelC = new GridBagConstraints();
        labelC.fill = GridBagConstraints.HORIZONTAL;
        labelC.gridx = 0;
        labelC.gridy = 0;
        labelC.insets = new Insets(0, 0, 12, 12);
        GridBagConstraints fieldC = new GridBagConstraints();
        fieldC.fill = GridBagConstraints.HORIZONTAL;
        fieldC.gridx = 1;
        fieldC.gridy = 0;
        fieldC.insets = new Insets(0, 0, 12, 12);
        fieldC.weighty = 0.0f;
        fields.add(new JLabel(jEdit.getProperty("lisp-paste.channel")), labelC);
        channel = createChannelList();
        fields.add(channel, fieldC);
        labelC.gridx = 2;
        reloadChannels = new RolloverButton(GUIUtilities.loadIcon("Reload.png"));
        reloadChannels.setToolTipText(jEdit.getProperty("lisp-paste.reload-channels"));
        reloadChannels.addActionListener(new ActionHandler());
        fields.add(reloadChannels, labelC);
        labelC.gridx = 0;
        labelC.gridy++;
        fieldC.gridy++;
        fields.add(new JLabel(jEdit.getProperty("lisp-paste.user")), labelC);
        user = new JTextField(15);
        String userName = jEdit.getProperty("lisp-paste.user.value");
        if (userName == null) userName = System.getProperty("user.name");
        user.setText(userName);
        fields.add(user, fieldC);
        labelC.gridy++;
        fieldC.gridy++;
        fields.add(new JLabel(jEdit.getProperty("lisp-paste.title")), labelC);
        title = new JTextField();
        fieldC.weightx = 1.0f;
        fieldC.gridwidth = 3;
        fieldC.insets = new Insets(0, 0, 12, 0);
        fields.add(title, fieldC);
        content.add(BorderLayout.NORTH, fields);
        contents = new JTextArea(10, 60);
        contents.setText(paste);
        content.add(BorderLayout.CENTER, new JScrollPane(contents));
        Box buttons = new Box(BoxLayout.X_AXIS);
        buttons.setBorder(new EmptyBorder(12, 0, 0, 0));
        buttons.add(Box.createGlue());
        ok = new JButton(jEdit.getProperty("common.ok"));
        ok.addActionListener(new ActionHandler());
        getRootPane().setDefaultButton(ok);
        buttons.add(ok);
        buttons.add(Box.createHorizontalStrut(12));
        cancel = new JButton(jEdit.getProperty("common.cancel"));
        cancel.addActionListener(new ActionHandler());
        buttons.add(cancel);
        buttons.add(Box.createGlue());
        content.add(BorderLayout.SOUTH, buttons);
        pack();
        setLocationRelativeTo(view);
        show();
    }

    public void ok() {
        String u = user.getText();
        String t = title.getText();
        String c = contents.getText();
        if (u.length() == 0 || t.length() == 0 || c.length() == 0) {
            getToolkit().beep();
            return;
        }
        save();
        dispose();
        VFSManager.runInWorkThread(new LispPasteRequest(view, (String) channel.getSelectedItem(), u, t, c));
    }

    public void cancel() {
        save();
        dispose();
    }

    private View view;

    private JComboBox channel;

    private RolloverButton reloadChannels;

    private JTextField user;

    private JTextField title;

    private JTextArea contents;

    private JButton ok, cancel;

    private JComboBox createChannelList() {
        JComboBox channel = new JComboBox();
        channel.setEditable(true);
        String list = jEdit.getProperty("lisp-paste.channels");
        StringTokenizer st = new StringTokenizer(list);
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        while (st.hasMoreTokens()) model.addElement(st.nextToken());
        channel.setModel(model);
        return channel;
    }

    private void save() {
        jEdit.setProperty("lisp-paste.user.value", user.getText());
        ComboBoxModel model = channel.getModel();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < model.getSize(); i++) {
            if (i != 0) buf.append(' ');
            buf.append((String) model.getElementAt(i));
        }
        jEdit.setProperty("lisp-paste.channels", buf.toString());
    }

    private void reloadChannels() {
        reloadChannels.setEnabled(false);
        final ChannelListRequest channelListRequest = new ChannelListRequest(view);
        VFSManager.runInWorkThread(channelListRequest);
        VFSManager.runInAWTThread(new Runnable() {

            public void run() {
                setChannelList(channelListRequest.getChannelList());
                reloadChannels.setEnabled(true);
            }
        });
    }

    private void setChannelList(List list) {
        if (list == null) return;
        String chan = (String) channel.getSelectedItem();
        channel.setModel(new DefaultComboBoxModel(list.toArray()));
        channel.setSelectedItem(chan);
    }

    class ActionHandler implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if (source == ok) ok(); else if (source == cancel) cancel(); else if (source == reloadChannels) reloadChannels();
        }
    }
}
