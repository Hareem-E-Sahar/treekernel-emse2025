package com.k42b3.zubat;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import com.k42b3.neodym.ServiceItem;
import com.k42b3.neodym.Services;

/**
 * MenuPanel
 *
 * @author     Christoph Kappestein <k42b3.x@gmail.com>
 * @license    http://www.gnu.org/licenses/gpl.html GPLv3
 * @link       http://code.google.com/p/delta-quadrant
 * @version    $Revision: 212 $
 */
public class MenuPanel extends JMenuBar {

    private static final long serialVersionUID = 1L;

    private Zubat zubat;

    public MenuPanel(Zubat zubatInstance) {
        this.zubat = zubatInstance;
        this.add(this.buildMenu("http://ns.amun-project.org/2011/amun/content"));
        this.add(this.buildMenu("http://ns.amun-project.org/2011/amun/system"));
        this.add(this.buildMenu("http://ns.amun-project.org/2011/amun/user"));
        this.add(this.buildMenu("http://ns.amun-project.org/2011/amun/service"));
        this.add(this.buildHelpMenu());
        this.add(Box.createHorizontalGlue());
        this.add(this.buildInfo());
    }

    private JMenu buildMenu(String baseUrl) {
        Services services = zubat.getAvailableServices();
        int baseDeep = this.charCount('/', baseUrl) + 1;
        String baseTitle = baseUrl.substring(baseUrl.lastIndexOf("/") + 1);
        baseTitle = (baseTitle.charAt(0) + "").toUpperCase() + baseTitle.substring(1).toLowerCase();
        JMenu menu = new JMenu(baseTitle);
        for (int i = 0; i < services.getSize(); i++) {
            ServiceItem item = services.getElementAt(i);
            String type = item.getTypeStartsWith(baseUrl);
            if (type != null) {
                int deep = this.charCount('/', type);
                String title = type.substring(type.lastIndexOf("/") + 1);
                title = (title.charAt(0) + "").toUpperCase() + title.substring(1).toLowerCase();
                if (deep == baseDeep) {
                    JMenu childMenu = this.buildMenu(type);
                    if (childMenu.getItemCount() == 0) {
                        JMenuItem serviceItem = new JMenuItem(title);
                        serviceItem.addActionListener(new MenuItemListener(item));
                        menu.add(serviceItem);
                    } else {
                        JMenuItem serviceItem = new JMenuItem(title);
                        serviceItem.addActionListener(new MenuItemListener(item));
                        childMenu.insert(serviceItem, 0);
                        childMenu.insertSeparator(1);
                        menu.add(childMenu);
                    }
                }
            }
        }
        return menu;
    }

    private JMenu buildHelpMenu() {
        JMenu menu = new JMenu("Help");
        JMenuItem websiteItem = new JMenuItem("Website");
        websiteItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String websiteUrl = "http://amun.phpsx.org";
                try {
                    URI websiteUri = new URI(websiteUrl);
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        if (desktop.isSupported(Desktop.Action.BROWSE)) {
                            desktop.browse(websiteUri);
                        } else {
                            JOptionPane.showMessageDialog(null, websiteUrl);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, websiteUrl);
                    }
                } catch (Exception ex) {
                    Zubat.handleException(ex);
                    JOptionPane.showMessageDialog(null, websiteUrl);
                }
            }
        });
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                StringBuilder out = new StringBuilder();
                out.append("Version: zubat (version: " + Zubat.version + ")\n");
                out.append("Author: Christoph \"k42b3\" Kappestein" + "\n");
                out.append("Website: http://code.google.com/p/delta-quadrant" + "\n");
                out.append("License: GPLv3 <http://www.gnu.org/licenses/gpl-3.0.html>" + "\n");
                out.append("\n");
                out.append("An java application to access the API of Amun (amun.phpsx.org). It is" + "\n");
                out.append("used to debug and control a website based on Amun. This is the reference" + "\n");
                out.append("implementation howto access the API so feel free to hack and extend." + "\n");
                JOptionPane.showMessageDialog(null, out, "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menu.add(websiteItem);
        menu.add(aboutItem);
        return menu;
    }

    private JLabel buildInfo() {
        JLabel lblInfo = new JLabel(zubat.getAccount().getName() + " (" + zubat.getAccount().getGroup() + ")");
        lblInfo.setBorder(new EmptyBorder(4, 4, 4, 8));
        return lblInfo;
    }

    private int charCount(char c, String content) {
        int j = 0;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == c) {
                j++;
            }
        }
        return j;
    }

    class MenuItemListener implements ActionListener {

        private ServiceItem item;

        public MenuItemListener(ServiceItem item) {
            this.item = item;
        }

        public void actionPerformed(ActionEvent e) {
            zubat.loadContainer(item);
        }
    }
}
