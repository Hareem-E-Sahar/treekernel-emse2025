package nox.ui.chat.common;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import nox.ui.chat.peer.PeerChatroom;
import nox.ui.common.DialogEarthquakeCenter;
import nox.ui.common.JNABalloon;
import nox.ui.common.SystemPath;

/**
 * 更加"完美"的聊天窗口: 可以选择字体及颜色; 可以插入图片
 * 
 * @author shinysky
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * 
 * (要时刻准备着从服务器接收信息) 信息格式: 第一部分:who sayTo who at time 第二部分:the message
 * 将消息添加到消息记录, 然后通过检查发信人和noDisturb变量来决定是否添加该信息到历史消息窗口
 */
public class ChatroomPane extends JSplitPane implements ActionListener {

    /**
	 * we don't know much about it
	 */
    private static final long serialVersionUID = -1915394855935441419L;

    /**
	 * 历史消息JScrollPane
	 */
    private JScrollPane sp_historymsg;

    /**
	 * 历史消息JTextPane
	 */
    private JTextPane tp_historymsg;

    /**
	 * 消息输入窗口及零件
	 */
    private JPanel p_inputpaneAndButtons;

    /**
	 * 消息JScrollPane
	 */
    private JScrollPane sp_input;

    /**
	 * 消息输入框 JTextPane
	 */
    private JTextPane tp_input;

    /**
	 * 按钮JPanel, 含插入表情按钮/闪屏按钮/.../发送按钮
	 */
    private JPanel p_buttons;

    /**
	 * 插入表情JButton
	 */
    private JButton b_emoticon;

    /**
	 * 表情选择对话框
	 */
    private FaceDialog selFace;

    /**
	 * 闪屏振动
	 */
    private JButton b_nudge;

    private static final String nudgeMsg = "[F:999]";

    /**
	 * 发送图片按钮
	 */
    private JButton b_sendPic;

    /**
	 * 发送文件按钮
	 */
    private JButton b_sendFile;

    /**
	 * 截屏按钮
	 */
    private JButton b_snapshot;

    private JButton b_snapconfig;

    JPopupMenu menuSnap;

    JMenuItem doSnap;

    JCheckBoxMenuItem hideFrame;

    /**
	 * 图片索引格式化处理
	 */
    public static final DecimalFormat fmNum = new DecimalFormat("000");

    /**
	 * 消息加密JToggleButton
	 */
    private JToggleButton tb_encrypt;

    /**
	 * 消息发送JButton
	 */
    private JButton b_send;

    /**
	 * 历史消息,用于保存操作
	 */
    String historymsg_save;

    /**
	 * 当前处于输入框中的消息, 用于保存操作
	 */
    String currentmsg_save;

    /**
	 * 文本风格模型
	 */
    StyledDocument styledDoc;

    /**
	 * 普通
	 */
    Style normal;

    /**
	 * 蓝色
	 */
    Style blue;

    /**
	 * 绿色
	 */
    Style green;

    /**
	 * 灰色
	 */
    Style gray;

    /**
	 * 红色
	 */
    Style red;

    /**
	 * 黑体
	 */
    Style bold;

    /**
	 * 斜体
	 */
    Style italic;

    /**
	 * 大号
	 */
    Style bigSize;

    /**
	 * 日期标签格式
	 */
    private Format fmDate = new SimpleDateFormat("yyyy/MM/dd E HH:mm:ss");

    /**
	 * 欢迎消息
	 */
    private String sayHello;

    Chatroom parent;

    /**
	 * JSplitPane 聊天组件, 含输入框/消息窗口/表情按钮/闪屏按钮/.../发送按钮 等
	 * 
	 * @param par
	 *            父组件, 用于使窗口par振动
	 */
    public ChatroomPane(Chatroom par) {
        super(JSplitPane.VERTICAL_SPLIT);
        parent = par;
        sayHello = new String("\t------====  Welcome to the Chat Room  ====------\n" + "\t  ------====     What do U wanna say ?   ====------\n");
        tp_historymsg = new JTextPane();
        historymsg_save = new String();
        historymsg_save += sayHello;
        styledDoc = tp_historymsg.getStyledDocument();
        normal = styledDoc.addStyle("normal", null);
        StyleConstants.setFontFamily(normal, "SansSerif");
        blue = styledDoc.addStyle("blue", normal);
        StyleConstants.setForeground(blue, Color.blue);
        green = styledDoc.addStyle("green", normal);
        StyleConstants.setForeground(green, Color.GREEN.darker());
        gray = styledDoc.addStyle("gray", normal);
        StyleConstants.setForeground(gray, Color.GRAY);
        red = styledDoc.addStyle("red", normal);
        StyleConstants.setForeground(red, Color.red);
        bold = styledDoc.addStyle("bold", normal);
        StyleConstants.setBold(bold, true);
        italic = styledDoc.addStyle("italic", normal);
        StyleConstants.setItalic(italic, true);
        bigSize = styledDoc.addStyle("bigSize", normal);
        StyleConstants.setFontSize(bigSize, 24);
        styledDoc.setLogicalStyle(0, red);
        tp_historymsg.replaceSelection(sayHello);
        tp_historymsg.setBackground(new Color(180, 250, 250));
        tp_historymsg.setSelectionColor(Color.YELLOW);
        tp_historymsg.setEditable(false);
        sp_historymsg = new JScrollPane(tp_historymsg);
        sp_historymsg.setAutoscrolls(true);
        p_inputpaneAndButtons = new JPanel();
        tp_input = new JTextPane();
        tp_input.setToolTipText(getHtmlText("Input your message and press \"Send\" <br>or press Ctrl+Enter"));
        tp_input.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent event) {
                int keyCode = event.getKeyCode();
                if (keyCode == KeyEvent.VK_ENTER && event.isControlDown()) {
                    System.out.println("You press the combo-key : Ctrl+Enter");
                    sendMessage();
                }
            }

            public void keyTyped(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
            }
        });
        sp_input = new JScrollPane(tp_input);
        p_buttons = new JPanel();
        Dimension buttonSize = new Dimension(26, 26);
        b_emoticon = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "emoticon.png"));
        b_emoticon.setToolTipText(getHtmlText("Insert a emoticon"));
        b_emoticon.setActionCommand("Emoticon");
        b_emoticon.addActionListener(this);
        b_emoticon.setSize(buttonSize);
        b_emoticon.setPreferredSize(buttonSize);
        b_emoticon.setMaximumSize(buttonSize);
        b_emoticon.setMinimumSize(buttonSize);
        selFace = new FaceDialog("Insert a face", true, SystemPath.FACES_RESOURCE_PATH);
        selFace.setBounds(450, 350, FaceDialog.FACECELLWIDTH * FaceDialog.FACECOLUMNS, FaceDialog.FACECELLHEIGHT * FaceDialog.FACEROWS + 30);
        selFace.pack();
        b_nudge = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "nudge.png"));
        b_nudge.setToolTipText(getHtmlText("Rock and Roll !"));
        b_nudge.setActionCommand("Nudge");
        b_nudge.addActionListener(this);
        b_nudge.setSize(buttonSize);
        b_nudge.setPreferredSize(buttonSize);
        b_nudge.setMaximumSize(buttonSize);
        b_nudge.setMinimumSize(buttonSize);
        b_sendPic = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "sendpic.png"));
        b_sendPic.setToolTipText(getHtmlText("Send a picture"));
        b_sendPic.setActionCommand("SendPic");
        b_sendPic.addActionListener(this);
        b_sendPic.setSize(buttonSize);
        b_sendPic.setPreferredSize(buttonSize);
        b_sendPic.setMaximumSize(buttonSize);
        b_sendPic.setMinimumSize(buttonSize);
        b_sendFile = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "sendfile.png"));
        b_sendFile.setToolTipText(getHtmlText("Send a file"));
        b_sendFile.setActionCommand("SendFile");
        b_sendFile.addActionListener(this);
        b_sendFile.setSize(buttonSize);
        b_sendFile.setPreferredSize(buttonSize);
        b_sendFile.setMaximumSize(buttonSize);
        b_sendFile.setMinimumSize(buttonSize);
        b_snapshot = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "snapshot.png"));
        b_snapshot.setToolTipText(getHtmlText("Snap it !"));
        b_snapshot.setActionCommand("Snapshot");
        b_snapshot.addActionListener(this);
        b_snapshot.setSize(buttonSize);
        b_snapshot.setPreferredSize(buttonSize);
        b_snapshot.setMaximumSize(buttonSize);
        b_snapshot.setMinimumSize(buttonSize);
        b_snapconfig = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "snapconfig.png"));
        b_snapconfig.setMargin(new Insets(0, 0, 0, 0));
        b_snapconfig.setToolTipText(getHtmlText("Snap Config"));
        b_snapconfig.setActionCommand("SnapshotConfig");
        b_snapconfig.addActionListener(this);
        b_snapconfig.setSize(new Dimension(buttonSize.width / 2, buttonSize.height));
        b_snapconfig.setPreferredSize(new Dimension(buttonSize.width / 2, buttonSize.height));
        b_snapconfig.setMaximumSize(new Dimension(buttonSize.width / 2, buttonSize.height));
        b_snapconfig.setMinimumSize(new Dimension(buttonSize.width / 2, buttonSize.height));
        menuSnap = new JPopupMenu();
        doSnap = new JMenuItem("Let's GO!");
        hideFrame = new JCheckBoxMenuItem("Hide this window while snapping", true);
        doSnap.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (hideFrame.getState()) {
                    parent.setState(JFrame.ICONIFIED);
                }
                try {
                    Thread.sleep(300);
                    Robot ro = new Robot();
                    Toolkit tk = Toolkit.getDefaultToolkit();
                    Dimension screenSize = tk.getScreenSize();
                    Rectangle rec = new Rectangle(0, 0, screenSize.width, screenSize.height);
                    BufferedImage buffImg = ro.createScreenCapture(rec);
                    final JDialog fakeWin = new JDialog(parent, true);
                    fakeWin.addKeyListener(new KeyListener() {

                        public void keyPressed(KeyEvent event) {
                            int keyCode = event.getKeyCode();
                            if (keyCode == KeyEvent.VK_ESCAPE) {
                                fakeWin.dispose();
                            }
                        }

                        public void keyTyped(KeyEvent e) {
                        }

                        public void keyReleased(KeyEvent e) {
                        }
                    });
                    ScreenCapturer temp = new ScreenCapturer(fakeWin, buffImg, screenSize.width, screenSize.height);
                    fakeWin.getContentPane().add(temp, BorderLayout.CENTER);
                    fakeWin.setUndecorated(true);
                    fakeWin.setSize(screenSize);
                    fakeWin.setVisible(true);
                    fakeWin.setAlwaysOnTop(true);
                    parent.setState(JFrame.NORMAL);
                    buffImg = temp.getWhatWeGot();
                    if (buffImg != null) {
                        ChatroomPane.this.sendAPicture(buffImg);
                    } else {
                        System.out.println("phew~we got nothing.");
                    }
                } catch (Exception exe) {
                    exe.printStackTrace();
                }
            }
        });
        menuSnap.add(doSnap);
        menuSnap.addSeparator();
        menuSnap.add(hideFrame);
        menuSnap.pack();
        tb_encrypt = new JToggleButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "unlock.png"));
        tb_encrypt.setToolTipText(getHtmlText("Encrypt or not"));
        tb_encrypt.setSelectedIcon(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "lock.png"));
        tb_encrypt.setSelected(true);
        tb_encrypt.setSize(buttonSize);
        tb_encrypt.setPreferredSize(buttonSize);
        tb_encrypt.setMaximumSize(buttonSize);
        tb_encrypt.setMinimumSize(buttonSize);
        b_send = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "send.png"));
        b_send.setMnemonic('S');
        b_send.setActionCommand("Send");
        b_send.setToolTipText(getHtmlText("Send"));
        b_send.addActionListener(this);
        b_send.setSize(buttonSize);
        b_send.setPreferredSize(buttonSize);
        b_send.setMaximumSize(buttonSize);
        b_send.setMinimumSize(buttonSize);
        p_buttons.setOpaque(false);
        p_buttons.setLayout(new BoxLayout(p_buttons, BoxLayout.X_AXIS));
        p_buttons.add(b_emoticon);
        p_buttons.add(b_nudge);
        p_buttons.add(b_sendPic);
        p_buttons.add(b_sendFile);
        p_buttons.add(b_snapshot);
        p_buttons.add(b_snapconfig);
        p_buttons.add(Box.createHorizontalGlue());
        p_buttons.add(tb_encrypt);
        p_buttons.add(b_send);
        p_inputpaneAndButtons.setOpaque(false);
        p_inputpaneAndButtons.setLayout(new BorderLayout());
        p_inputpaneAndButtons.add(p_buttons, BorderLayout.NORTH);
        p_inputpaneAndButtons.add(sp_input, BorderLayout.CENTER);
        this.setSize(new Dimension(PeerChatroom.WIDTH_DEFLT, PeerChatroom.HEIGHT_DEFLT - 35));
        this.setDividerLocation(0.65);
        this.setResizeWeight(0.62d);
        this.setDividerSize(3);
        this.add(sp_historymsg);
        this.add(p_inputpaneAndButtons);
    }

    private String getHtmlText(String text) {
        return ("<html><BODY bgColor=#ffffff><Font color=black>" + text + "</Font></BODY></html>");
    }

    /**
	 * 显示系统消息
	 * 
	 * @param msg
	 *            系统消息
	 */
    public void showMsgDialog(String msg) {
        JOptionPane.showMessageDialog((Component) null, msg, "Message form the Server", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showFailedSendingMsg(String msg) {
        tp_historymsg.setEditable(true);
        tp_historymsg.setCaretPosition(styledDoc.getLength());
        styledDoc.setLogicalStyle(tp_historymsg.getCaretPosition(), italic);
        tp_historymsg.replaceSelection("Sorry, failed to send out the msg:\n" + msg + '\n');
        tp_historymsg.setEditable(false);
    }

    /**
	 * 用于外部程序调用,以显示消息
	 * 
	 * @param strs
	 *            1:sender;2:receiver;3:time;4:msg
	 * @param incomingPic 外来图片
	 */
    public void incomingMsgProcessor(String sender, String time, Object msgdata) {
        if (msgdata instanceof String) incomingMsgProcessor(sender, time, (String) msgdata, null); else if (msgdata instanceof ImageIcon) incomingMsgProcessor(sender, time, null, (ImageIcon) msgdata);
    }

    public void incomingMsgProcessor(String sender, String time, final String strmsg, ImageIcon incomingPic) {
        System.out.println("playAudio()...");
        Thread playThd = new Thread(new Runnable() {

            @Override
            public void run() {
                if (strmsg != null && strmsg.equals(nudgeMsg)) playNudgeAudio(); else playAudio();
            }
        }, "Beeper");
        playThd.start();
        Date date = new Date(Long.parseLong(time));
        String label = fmDate.format(date) + " " + sender + ":";
        StringBuffer strbuf_msg = null;
        if (strmsg != null) {
            strbuf_msg = new StringBuffer(strmsg);
            int caretPos = -1;
            for (; (caretPos = strbuf_msg.indexOf("^n", caretPos + 1)) >= 0; ) {
                strbuf_msg.replace(caretPos, caretPos + 2, "\n");
            }
        }
        appendToHMsg(label, (strbuf_msg != null) ? strbuf_msg.toString() : null, incomingPic, true, false);
    }

    /**
	 * 接收消息时播放提示音
	 * 
	 */
    public void playAudio() {
        final AudioClip msgBeep;
        try {
            URL url = new URL("file:/" + System.getProperty("user.dir") + System.getProperty("file.separator") + SystemPath.AUDIO_RESOURCE_PATH + "typewpcm.wav");
            msgBeep = Applet.newAudioClip(url);
            msgBeep.play();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }

    /**
	 * 接收/发送闪屏时播放提示音
	 */
    public void playNudgeAudio() {
        final AudioClip msgBeep;
        try {
            URL url = new URL("file:/" + System.getProperty("user.dir") + System.getProperty("file.separator") + SystemPath.AUDIO_RESOURCE_PATH + "nudgewpcm.wav");
            msgBeep = Applet.newAudioClip(url);
            msgBeep.play();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }

    /**
	 * 向历史消息窗口添加文本
	 * 
	 * 注意:如果ckb_nodisturb 为 true,表示防打扰模式开启, 此时从服务器传来的群聊消息只会被添加到历史消息字符串
	 * 而不会被添加到窗口中 只有私聊对象的消息才会被添加到窗口中
	 * 
	 * @param label
	 *            发送者/接收者/发送时间 标签
	 * @param msg
	 *            要添加到消息记录的字符串
	 * @param incomingPic 外来图片
	 * @param visible
	 *            是否要添加到历史消息窗口中(可见)
	 * @param isFromMe
	 *            是否是自己向外发送的消息
	 * 
	 * 原来这个函数的功能是将消息输入框的字符串插入到历史消息窗口, 显然这是不够的.
	 * 添加参数后,从服务器接受到的历史消息可以通过调用这个函数来插入到历史消息窗口.
	 * 简言之,增强了这个函数的通用性.将消息添加到消息记录的功能从中移出.
	 */
    public void appendToHMsg(String label, String msg, ImageIcon incomingPic, boolean visible, boolean isFromMe) {
        StringBuffer label_buf = new StringBuffer(label);
        Style labelStyle = isFromMe ? green : blue;
        historymsg_save += (label_buf + "\n");
        if (msg != null && msg.equals(nudgeMsg)) {
            labelStyle = gray;
            DialogEarthquakeCenter dec = new DialogEarthquakeCenter(parent);
            dec.startShake();
            tp_historymsg.setEditable(true);
            tp_historymsg.setCaretPosition(styledDoc.getLength());
            styledDoc.setLogicalStyle(tp_historymsg.getCaretPosition(), labelStyle);
            tp_historymsg.replaceSelection(label + '\n');
            tp_historymsg.setCaretPosition(styledDoc.getLength());
            styledDoc.setLogicalStyle(tp_historymsg.getCaretPosition(), italic);
            tp_historymsg.replaceSelection("YOU JUST RECEIVED A NUDGE\n");
            tp_historymsg.setEditable(false);
            return;
        }
        if (visible) {
            tp_historymsg.setEditable(true);
            tp_historymsg.setCaretPosition(styledDoc.getLength());
            styledDoc.setLogicalStyle(tp_historymsg.getCaretPosition(), labelStyle);
            tp_historymsg.replaceSelection(label + '\n');
            System.out.println("label :" + label);
            if (msg != null && !msg.equals("")) {
                StringBuffer msg_buf = new StringBuffer(msg);
                historymsg_save += (msg_buf + "\n");
                System.out.println("msg :" + msg);
                tp_historymsg.setCaretPosition(styledDoc.getLength());
                styledDoc.setLogicalStyle(tp_historymsg.getCaretPosition(), bold);
                int position = 0, caretPos = 0;
                for (; (caretPos = msg_buf.indexOf("[F:", position)) >= 0; ) {
                    if (msg_buf.substring(caretPos, caretPos + 7).matches("\\[F\\:[0-9][0-9][0-9]\\]")) {
                        tp_historymsg.setCaretPosition(styledDoc.getLength());
                        tp_historymsg.replaceSelection(msg_buf.substring(position, caretPos));
                        tp_historymsg.setCaretPosition(styledDoc.getLength());
                        int faceindex = Integer.parseInt(msg_buf.substring(caretPos + 3, caretPos + 6));
                        tp_historymsg.insertIcon(getImageIconFace(faceindex));
                        position = caretPos + 7;
                    } else {
                        tp_historymsg.setCaretPosition(styledDoc.getLength());
                        tp_historymsg.replaceSelection(msg_buf.substring(position, caretPos + 3));
                        position = caretPos + 3;
                    }
                }
                tp_historymsg.setCaretPosition(styledDoc.getLength());
                tp_historymsg.replaceSelection(msg_buf.substring(position) + '\n');
            }
            if (incomingPic != null) {
                tp_historymsg.setCaretPosition(styledDoc.getLength());
                tp_historymsg.insertIcon(incomingPic);
                tp_historymsg.setCaretPosition(styledDoc.getLength());
                tp_historymsg.replaceSelection("\n");
            }
            tp_historymsg.setEditable(false);
        }
        this.repaint();
    }

    /**
	 * 添加表情图片到msg输入窗口中
	 * 
	 * @param selectedFace
	 *            被选择的图片的索引
	 */
    private void appendFaceToInputPane(int selectedFace) {
        tp_input.setEditable(true);
        tp_input.replaceSelection("[F:" + fmNum.format(selectedFace) + ']');
    }

    /**
	 * 添加表情图片到msg输入窗口中
	 * 
	 * @param selectedFace
	 *            被选择的图片
	 */
    @SuppressWarnings("unused")
    private void appendFaceToInputPane(ImageIcon selectedFace) {
        tp_input.setEditable(true);
        tp_input.insertIcon(selectedFace);
    }

    /**
	 * 获取历史消息
	 * 
	 * @return history messages
	 */
    public String getHistoryMsgs() {
        return historymsg_save;
    }

    /**
	 * 将用户输入的消息发送到历史消息窗口 (还有,要发送到服务器)
	 */
    private void sendMessage() {
        if (tp_input.getText().equals("")) {
            System.out.println("You're trying to send a empty message, it's not suggested.");
            final String BALLOON_TEXT = "<html><center>" + "You're trying to send an empty message<br>" + "which is not suggested/supported.<br>" + "(Click to dismiss this balloon)</center></html>";
            JNABalloon balloon = new JNABalloon(BALLOON_TEXT, tp_input, 100, 20);
            balloon.showBalloon();
            return;
        }
        Date date = new Date();
        String label = fmDate.format(date) + " Me:";
        appendToHMsg(label, tp_input.getText(), null, true, true);
        StringBuffer strbuf_msg = new StringBuffer(tp_input.getText());
        int caretPos = -1;
        for (; (caretPos = strbuf_msg.indexOf("\r\n", caretPos + 1)) >= 0; ) {
            strbuf_msg.replace(caretPos, caretPos + 2, "^n");
        }
        System.out.println("strbuf_msg :" + strbuf_msg);
        boolean succeed = parent.SendMsg(new String(strbuf_msg), tb_encrypt.isSelected());
        if (!succeed) {
            showFailedSendingMsg(new String(strbuf_msg));
        }
        tp_input.setText("");
    }

    private void sendANudge() {
        Date date = new Date();
        String label = fmDate.format(date) + " Nudging " + parent.getRoomName() + ":";
        appendToHMsg(label, null, null, true, true);
        boolean succeed = parent.SendMsg(nudgeMsg, tb_encrypt.isSelected());
        if (!succeed) {
            showFailedSendingMsg("(It's a nudge actually.)");
        }
    }

    /**
	 * 根据索引获取表情图片
	 * 
	 * @param index
	 *            图片索引
	 * @return 表情图片
	 */
    private ImageIcon getImageIconFace(int index) {
        if (index < 105) return new ImageIcon(SystemPath.FACES_RESOURCE_PATH + index + ".gif"); else return new ImageIcon(SystemPath.FACES_RESOURCE_PATH + "newFace\\" + (int) (index - 105) + ".png");
    }

    /**
	 * 用于选择图片发送, 或许可以扩展为发送文件
	 * @param imgPath 图片路径
	 */
    private void sendAPicture(String imgPath) {
        File thePicFile = new File(imgPath);
        if (thePicFile.exists()) {
            BufferedImage bufImg = null;
            try {
                bufImg = javax.imageio.ImageIO.read(thePicFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendAPicture(bufImg);
        }
    }

    /**
	 * 用于发送图片文件/截屏图片
	 * @param img 图片buffer
	 */
    private void sendAPicture(BufferedImage bufImg) {
        if (bufImg == null) {
            System.out.println("bufImg is null in sendAPicture()");
            return;
        }
        Date date = new Date();
        String label = "I send a picture to " + parent.getRoomName() + ", at " + fmDate.format(date) + " :";
        StringBuffer label_buf = new StringBuffer(label);
        StringBuffer msg_buf = new StringBuffer("A Screen Snapshot");
        historymsg_save += (label_buf + "\n");
        historymsg_save += (msg_buf + "\n");
        tp_historymsg.setEditable(true);
        tp_historymsg.setCaretPosition(styledDoc.getLength());
        styledDoc.setLogicalStyle(tp_historymsg.getCaretPosition(), blue);
        tp_historymsg.replaceSelection(label + '\n');
        System.out.println("label :" + label);
        ImageIcon img = new ImageIcon(bufImg);
        tp_historymsg.setCaretPosition(styledDoc.getLength());
        tp_historymsg.insertIcon(img);
        tp_historymsg.setCaretPosition(styledDoc.getLength());
        tp_historymsg.replaceSelection("\n");
        tp_historymsg.setEditable(false);
        boolean succeed = parent.SendMsg(bufImg, tb_encrypt.isSelected());
        if (!succeed) {
            showFailedSendingMsg("(It's a picture actually.)");
        }
    }

    /**
	 * 用于发送选择的文件
	 * @param filePath 文件路径
	 */
    private void sendAFile(String filePath) {
        File theFile = new File(filePath);
        if (theFile.exists()) {
            boolean succeed = parent.SendMsg(theFile, tb_encrypt.isSelected());
            if (!succeed) {
                showFailedSendingMsg("(It's a file actually.)");
            }
        }
    }

    /**
	 * (按钮)事件响应
	 */
    public void actionPerformed(ActionEvent e) {
        JButton srcButton = (JButton) e.getSource();
        if (srcButton.getActionCommand().equals("Send")) {
            System.out.println("You clicked the button : Send");
            sendMessage();
        } else if (srcButton.getActionCommand().equals("Emoticon")) {
            System.out.println("You clicked the button : InsertImage");
            selFace.setLocationRelativeTo(b_emoticon);
            selFace.setVisible(true);
            int selectedfaceIndex = selFace.getSelectedFaceIndex();
            if (selectedfaceIndex != -1) {
                System.out.println("You selected the face : " + selectedfaceIndex + ".gif");
                appendFaceToInputPane(selectedfaceIndex);
            }
        } else if (srcButton.getActionCommand().equals("Nudge")) {
            DialogEarthquakeCenter dec = new DialogEarthquakeCenter(parent);
            dec.startShake();
            playNudgeAudio();
            sendANudge();
        } else if (srcButton.getActionCommand().equals("SendPic")) {
            JFileChooser chooser = new JFileChooser();
            FileFilter filter = new FileFilter() {

                public boolean accept(File f) {
                    return f.isDirectory() || (f.isFile() && (f.getName().endsWith(".PNG") || f.getName().endsWith(".png") || f.getName().endsWith(".JPG") || f.getName().endsWith(".jpg") || f.getName().endsWith(".BMP") || f.getName().endsWith(".bmp") || f.getName().endsWith(".GIF") || f.getName().endsWith(".gif")));
                }

                @Override
                public String getDescription() {
                    return "BMP, JPG, PNG, or GIF";
                }
            };
            chooser.setFileFilter(filter);
            chooser.setDialogTitle("Please choose a picture to send");
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("You chose a pic: " + chooser.getSelectedFile().getPath());
                sendAPicture(chooser.getSelectedFile().getPath());
            }
        } else if (srcButton.getActionCommand().equals("SendFile")) {
            JFileChooser chooser = new JFileChooser();
            FileFilter filter = new FileFilter() {

                public boolean accept(File f) {
                    return f.isDirectory() || f.isFile();
                }

                @Override
                public String getDescription() {
                    return "*.*";
                }
            };
            chooser.setFileFilter(filter);
            chooser.setDialogTitle("Please choose a file to send");
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("You chose a pic: " + chooser.getSelectedFile().getPath());
                sendAFile(chooser.getSelectedFile().getPath());
            }
        } else if (srcButton.getActionCommand().equals("Snapshot")) {
            doSnap.doClick();
        } else if (srcButton.getActionCommand().equals("SnapshotConfig")) {
            menuSnap.show((Component) e.getSource(), 0, 26);
        }
    }
}
