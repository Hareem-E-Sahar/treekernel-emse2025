package com.toedter.calendar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import com.toedter.components.JLocaleChooser;
import com.toedter.components.JSpinField;
import com.toedter.components.JTitlePanel;

/**
 * A demonstration Applet for the JCalendar bean. The demo can also be started as Java application.
 *
 * @author Kai Toedter
 * @version 1.2
 */
public class JCalendarDemo extends JApplet implements PropertyChangeListener {

    private JSplitPane splitPane;

    private JPanel calendarPanel;

    private JComponent[] beans;

    private JPanel propertyPanel;

    private JTitlePanel propertyTitlePanel;

    private JTitlePanel componentTitlePanel;

    private JPanel componentPanel;

    private JToolBar toolBar;

    /**
     * Initializes the applet.
     */
    public void init() {
        initializeLookAndFeels();
        beans = new JComponent[6];
        beans[0] = new JCalendar();
        beans[1] = new JDateChooser();
        beans[2] = new JDayChooser();
        beans[3] = new JMonthChooser();
        beans[4] = new JYearChooser();
        beans[5] = new JSpinField();
        ((JSpinField) beans[5]).adjustWidthToMaximumValue();
        ((JYearChooser) beans[4]).setMaximum(((JSpinField) beans[5]).getMaximum());
        ((JYearChooser) beans[4]).adjustWidthToMaximumValue();
        getContentPane().setLayout(new BorderLayout());
        setJMenuBar(createMenuBar());
        toolBar = createToolBar();
        getContentPane().add(toolBar, BorderLayout.NORTH);
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        splitPane.setDividerSize(4);
        BasicSplitPaneDivider divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();
        if (divider != null) {
            divider.setBorder(null);
        }
        propertyPanel = new JPanel();
        componentPanel = new JPanel();
        URL iconURL = beans[0].getClass().getResource("images/" + beans[0].getName() + "Color16.gif");
        ImageIcon icon = new ImageIcon(iconURL);
        propertyTitlePanel = new JTitlePanel("Properties", null, propertyPanel, BorderFactory.createEmptyBorder(4, 4, 4, 4));
        componentTitlePanel = new JTitlePanel("Component", icon, componentPanel, BorderFactory.createEmptyBorder(4, 4, 0, 4));
        splitPane.setBottomComponent(propertyTitlePanel);
        splitPane.setTopComponent(componentTitlePanel);
        installBean(beans[0]);
        getContentPane().add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Installs the Kunststoff and Plastic Look And Feels if available in classpath.
     */
    public final void initializeLookAndFeels() {
        try {
            UIManager.installLookAndFeel("JGoodies Plastic 3D", "com.jgoodies.plaf.plastic.Plastic3DLookAndFeel");
            UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.Plastic3DLookAndFeel");
        } catch (Throwable t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates the menu bar
     *
     * @return Description of the Return Value
     */
    public JToolBar createToolBar() {
        toolBar = new JToolBar();
        toolBar.putClientProperty("jgoodies.headerStyle", "Both");
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        for (int i = 0; i < beans.length; i++) {
            Icon icon;
            JButton button;
            try {
                final JComponent bean = beans[i];
                URL iconURL = bean.getClass().getResource("images/" + bean.getName() + "Color16.gif");
                icon = new ImageIcon(iconURL);
                button = new JButton(icon);
                ActionListener actionListener = new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        installBean(bean);
                    }
                };
                button.addActionListener(actionListener);
            } catch (Exception e) {
                System.out.println("JCalendarDemo.createToolBar(): " + e);
                button = new JButton(beans[i].getName());
            }
            button.setFocusPainted(false);
            toolBar.add(button);
        }
        return toolBar;
    }

    /**
     * Creates the menu bar
     *
     * @return Description of the Return Value
     */
    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.putClientProperty("jgoodies.headerStyle", "Both");
        JMenu componentsMenu = new JMenu("Components");
        componentsMenu.setMnemonic('C');
        menuBar.add(componentsMenu);
        for (int i = 0; i < beans.length; i++) {
            Icon icon;
            JMenuItem menuItem;
            try {
                URL iconURL = beans[i].getClass().getResource("images/" + beans[i].getName() + "Color16.gif");
                icon = new ImageIcon(iconURL);
                menuItem = new JMenuItem(beans[i].getName(), icon);
            } catch (Exception e) {
                System.out.println("JCalendarDemo.createMenuBar(): " + e);
                menuItem = new JMenuItem(beans[i].getName());
            }
            componentsMenu.add(menuItem);
            final JComponent bean = beans[i];
            ActionListener actionListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    installBean(bean);
                }
            };
            menuItem.addActionListener(actionListener);
        }
        UIManager.LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
        ButtonGroup lnfGroup = new ButtonGroup();
        JMenu lnfMenu = new JMenu("Look&Feel");
        lnfMenu.setMnemonic('L');
        menuBar.add(lnfMenu);
        for (int i = 0; i < lnfs.length; i++) {
            if (!lnfs[i].getName().equals("CDE/Motif")) {
                JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem(lnfs[i].getName());
                lnfMenu.add(rbmi);
                rbmi.setSelected(UIManager.getLookAndFeel().getName().equals(lnfs[i].getName()));
                rbmi.putClientProperty("lnf name", lnfs[i]);
                rbmi.addItemListener(new ItemListener() {

                    public void itemStateChanged(ItemEvent ie) {
                        JRadioButtonMenuItem rbmi2 = (JRadioButtonMenuItem) ie.getSource();
                        if (rbmi2.isSelected()) {
                            UIManager.LookAndFeelInfo info = (UIManager.LookAndFeelInfo) rbmi2.getClientProperty("lnf name");
                            try {
                                UIManager.setLookAndFeel(info.getClassName());
                                SwingUtilities.updateComponentTreeUI(JCalendarDemo.this);
                                BasicSplitPaneDivider divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();
                                if (divider != null) {
                                    divider.setBorder(null);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.err.println("Unable to set UI " + e.getMessage());
                            }
                        }
                    }
                });
                lnfGroup.add(rbmi);
            }
        }
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        JMenuItem aboutItem = helpMenu.add(new AboutAction(this));
        aboutItem.setMnemonic('A');
        aboutItem.setAccelerator(KeyStroke.getKeyStroke('A', java.awt.Event.CTRL_MASK));
        menuBar.add(helpMenu);
        return menuBar;
    }

    /**
     * The applet is a PropertyChangeListener for "locale" and "calendar".
     *
     * @param evt Description of the Parameter
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (calendarPanel != null) {
            if (evt.getPropertyName().equals("calendar")) {
            }
        }
    }

    /**
     * Creates a JFrame with a JCalendarDemo inside and can be used for testing.
     *
     * @param s The command line arguments
     */
    public static void main(String[] s) {
        WindowListener l = new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        };
        JFrame frame = new JFrame("JCalendar Demo");
        frame.addWindowListener(l);
        JCalendarDemo demo = new JCalendarDemo();
        demo.init();
        frame.getContentPane().add(demo);
        frame.pack();
        frame.setBounds(200, 200, (int) frame.getPreferredSize().getWidth(), (int) frame.getPreferredSize().getHeight());
        frame.setVisible(true);
    }

    /**
     * Installes a demo bean.
     *
     * @param bean the demo bean
     */
    private void installBean(JComponent bean) {
        try {
            componentPanel.removeAll();
            componentPanel.add(bean);
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass(), bean.getClass().getSuperclass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            propertyPanel.removeAll();
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            propertyPanel.setLayout(gridbag);
            int count = 0;
            String[] types = new String[] { "class java.util.Locale", "boolean", "int", "class java.awt.Color", "class java.util.Date", "class java.lang.String" };
            for (int t = 0; t < types.length; t++) {
                for (int i = 0; i < propertyDescriptors.length; i++) {
                    if (propertyDescriptors[i].getWriteMethod() != null) {
                        String type = propertyDescriptors[i].getPropertyType().toString();
                        final PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
                        final JComponent currentBean = bean;
                        final Method readMethod = propertyDescriptor.getReadMethod();
                        final Method writeMethod = propertyDescriptor.getWriteMethod();
                        if (type.equals(types[t]) && (((readMethod != null) && (writeMethod != null)) || ("class java.util.Locale".equals(type)))) {
                            if ("boolean".equals(type)) {
                                boolean isSelected = false;
                                try {
                                    Boolean booleanObj = ((Boolean) readMethod.invoke(bean, null));
                                    isSelected = booleanObj.booleanValue();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                final JCheckBox checkBox = new JCheckBox("", isSelected);
                                checkBox.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent event) {
                                        try {
                                            if (checkBox.isSelected()) {
                                                writeMethod.invoke(currentBean, new Object[] { new Boolean(true) });
                                            } else {
                                                writeMethod.invoke(currentBean, new Object[] { new Boolean(false) });
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                addProperty(propertyDescriptors[i], checkBox, gridbag);
                                count += 1;
                            } else if ("int".equals(type)) {
                                JSpinField spinField = new JSpinField();
                                spinField.addPropertyChangeListener(new PropertyChangeListener() {

                                    public void propertyChange(PropertyChangeEvent evt) {
                                        try {
                                            if (evt.getPropertyName().equals("value")) {
                                                writeMethod.invoke(currentBean, new Object[] { evt.getNewValue() });
                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                });
                                try {
                                    Integer integerObj = ((Integer) readMethod.invoke(bean, null));
                                    spinField.setValue(integerObj.intValue());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                addProperty(propertyDescriptors[i], spinField, gridbag);
                                count += 1;
                            } else if ("class java.lang.String".equals(type)) {
                                String string = "";
                                try {
                                    string = ((String) readMethod.invoke(bean, null));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                JTextField textField = new JTextField(string);
                                ActionListener actionListener = new ActionListener() {

                                    public void actionPerformed(ActionEvent e) {
                                        System.out.println("JCalendarDemo.installBean(): " + e);
                                        try {
                                            writeMethod.invoke(currentBean, new Object[] { e.getActionCommand() });
                                        } catch (Exception ex) {
                                        }
                                    }
                                };
                                textField.addActionListener(actionListener);
                                addProperty(propertyDescriptors[i], textField, gridbag);
                                count += 1;
                            } else if ("class java.util.Locale".equals(type)) {
                                JLocaleChooser localeChooser = new JLocaleChooser(bean);
                                localeChooser.setPreferredSize(new Dimension(200, localeChooser.getPreferredSize().height));
                                addProperty(propertyDescriptors[i], localeChooser, gridbag);
                                count += 1;
                            } else if ("class java.util.Date".equals(type)) {
                                JDateChooser dateChooser = new JDateChooser();
                                dateChooser.addPropertyChangeListener((PropertyChangeListener) bean);
                                addProperty(propertyDescriptors[i], dateChooser, gridbag);
                                count += 1;
                            } else if ("class java.awt.Color".equals(type)) {
                                final JButton button = new JButton();
                                try {
                                    final Color colorObj = ((Color) readMethod.invoke(bean, null));
                                    button.setText("...");
                                    button.setBackground(colorObj);
                                    ActionListener actionListener = new ActionListener() {

                                        public void actionPerformed(ActionEvent e) {
                                            Color newColor = JColorChooser.showDialog(JCalendarDemo.this, "Choose Color", colorObj);
                                            button.setBackground(newColor);
                                            try {
                                                writeMethod.invoke(currentBean, new Object[] { newColor });
                                            } catch (Exception e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    };
                                    button.addActionListener(actionListener);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                addProperty(propertyDescriptors[i], button, gridbag);
                                count += 1;
                            }
                        }
                    }
                }
            }
            URL iconURL = bean.getClass().getResource("images/" + bean.getName() + "Color16.gif");
            ImageIcon icon = new ImageIcon(iconURL);
            componentTitlePanel.setTitle(bean.getName(), icon);
            bean.validate();
            propertyPanel.validate();
            componentPanel.validate();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }

    private void addProperty(PropertyDescriptor propertyDescriptor, JComponent editor, GridBagLayout grid) {
        String text = propertyDescriptor.getDisplayName();
        String newText = "";
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (((c >= 'A') && (c <= 'Z')) || (i == 0)) {
                if (i == 0) {
                    c += ('A' - 'a');
                }
                newText += (" " + c);
            } else {
                newText += c;
            }
        }
        JLabel label = new JLabel(newText + ": ", null, JLabel.RIGHT);
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill = GridBagConstraints.BOTH;
        grid.setConstraints(label, c);
        propertyPanel.add(label);
        c.gridwidth = GridBagConstraints.REMAINDER;
        grid.setConstraints(editor, c);
        propertyPanel.add(editor);
    }

    /**
     * Action to show the About dialog
     *
     * @author toedter_k
     */
    class AboutAction extends AbstractAction {

        private JCalendarDemo demo;

        /**
         * Constructor for the AboutAction object
         *
         * @param demo Description of the Parameter
         */
        AboutAction(JCalendarDemo demo) {
            super("About...");
            this.demo = demo;
        }

        /**
         * Description of the Method
         *
         * @param event Description of the Parameter
         */
        public void actionPerformed(ActionEvent event) {
            JOptionPane.showMessageDialog(demo, "JCalendar Demo\nVersion 1.2.1\n\nKai Toedter\nkai@toedter.com\nwww.toedter.com", "About...", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
