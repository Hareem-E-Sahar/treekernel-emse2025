package net.sf.amemailchecker.gui.settings;

import net.sf.amemailchecker.app.ApplicationContext;
import net.sf.amemailchecker.app.ResourceContext;
import net.sf.amemailchecker.app.extension.ExtensionInfo;
import net.sf.amemailchecker.command.CommandExecutionService;
import net.sf.amemailchecker.command.impl.app.OpenWebAddressCommand;
import net.sf.amemailchecker.gui.ActionControlFactory;
import net.sf.amemailchecker.gui.messageviewer.messagedetails.MessageDetailsMediator;
import net.sf.amemailchecker.gui.messageviewer.messagedetails.MessageDetailsEditState;
import net.sf.amemailchecker.util.StringUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExtensionInfoPanel extends JPanel {

    private JLabel author, version, description;

    private JButton homePage, email;

    private ResourceContext resourceContext;

    private String notProvidedTxt;

    public ExtensionInfoPanel() {
        resourceContext = ApplicationContext.getInstance();
        GridLayout layout = new GridLayout(2, 1);
        layout.setVgap(10);
        setLayout(layout);
        notProvidedTxt = resourceContext.getI18NBundleValue("label.extension.version");
        JPanel commonInfo = new JPanel();
        commonInfo.setLayout(new GridLayout(4, 2));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        commonInfo.add(new JLabel(resourceContext.getI18NBundleValue("label.extension.version")));
        version = new JLabel();
        version.setHorizontalAlignment(SwingConstants.CENTER);
        commonInfo.add(version);
        commonInfo.add(new JLabel(resourceContext.getI18NBundleValue("label.extension.author")));
        author = new JLabel();
        author.setHorizontalAlignment(SwingConstants.CENTER);
        commonInfo.add(author);
        commonInfo.add(new JLabel(resourceContext.getI18NBundleValue("label.email")));
        email = ActionControlFactory.Factory.createLinkButton();
        email.setHorizontalAlignment(SwingConstants.RIGHT);
        commonInfo.add(email);
        email.addActionListener(new EmailActionListener());
        commonInfo.add(new JLabel(resourceContext.getI18NBundleValue("label.extension.homepage")));
        homePage = ActionControlFactory.Factory.createLinkButton();
        homePage.setHorizontalAlignment(SwingConstants.RIGHT);
        commonInfo.add(homePage);
        homePage.addActionListener(new HomePageActionListener());
        description = new JLabel();
        description.setHorizontalAlignment(JLabel.CENTER);
        add(commonInfo);
        add(ActionControlFactory.Factory.createTitleWrapper(description, resourceContext.getI18NBundleValue("label.description")));
        Font font = new Font(author.getFont().getName(), Font.PLAIN, author.getFont().getSize());
        author.setFont(font);
        version.setFont(font);
        email.setFont(font);
        homePage.setFont(font);
        description.setFont(font);
    }

    void update(ExtensionInfo extensionInfo) {
        author.setText(getLabelText(extensionInfo.getAuthor()));
        version.setText(getLabelText(extensionInfo.getVersion()));
        email.setText(getLabelText(extensionInfo.getEmail()));
        homePage.setText(getLabelText(extensionInfo.getHomePage()));
        description.setText(getLabelText(ApplicationContext.getInstance().getI18NBundleValue(extensionInfo.getName(), extensionInfo.getDescriptionBundleKey())));
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE) || homePage.getText().equals(notProvidedTxt)) homePage.setEnabled(false);
        if (email.getText().equals(notProvidedTxt)) email.setEnabled(false);
    }

    private String getLabelText(String text) {
        return (StringUtil.isNullOrEmpty(text)) ? notProvidedTxt : text;
    }

    private class EmailActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            (new MessageDetailsMediator()).expandLetterToEdit(author.getText(), email.getText(), MessageDetailsEditState.COMPOSE_NEW);
        }
    }

    private class HomePageActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            CommandExecutionService.Service.executeCommandSilent(new OpenWebAddressCommand(((JButton) e.getSource()).getText()));
        }
    }
}
