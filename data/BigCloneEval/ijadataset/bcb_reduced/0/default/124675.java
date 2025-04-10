import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends Panel {

    private ChatApplet _applet = null;

    TextField _nickField = new TextField();

    Choice _channelChoice = new Choice();

    Label _nickLabel = new Label();

    Label _channelLabel = new Label();

    Button _loginButton = new Button();

    Label jLabel1 = new Label();

    GridBagLayout gridBagLayout1 = new GridBagLayout();

    /** Construct a LoginPanel for a ChatApplet. */
    public LoginPanel(ChatApplet applet) {
        this();
        _applet = applet;
        for (int i = 0; i < _applet.getChannels().size(); i++) {
            _channelChoice.add((String) _applet.getChannels().elementAt(i));
        }
    }

    /** 
    * Only for design-time use in a GUI builder. 
    * @deprecated Use LoginPanel(ChatApplet applet) constructor instead.
    */
    public LoginPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Attempt to login. */
    public void login() {
        String nick = _nickField.getText();
        String channel = _channelChoice.getSelectedItem();
        if (nick == null || nick.trim().length() == 0) {
            return;
        }
        _applet.login(nick, channel);
    }

    public void _loginButton_actionPerformed(ActionEvent e) {
        login();
    }

    public void initGUI() {
    }

    private void jbInit() throws Exception {
        _nickField.setText("nickname");
        this.setLayout(gridBagLayout1);
        _nickLabel.setText("Nick");
        _channelLabel.setText("Channel");
        jLabel1.setText("Select your nick name and the channel you wish to join");
        _loginButton.setLabel("Login...");
        _loginButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                _loginButton_actionPerformed(e);
            }
        });
        this.add(_nickField, new GridBagConstraints2(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(_channelChoice, new GridBagConstraints2(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 35, 0));
        this.add(_nickLabel, new GridBagConstraints2(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(_channelLabel, new GridBagConstraints2(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(jLabel1, new GridBagConstraints2(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(_loginButton, new GridBagConstraints2(0, 4, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    }
}
