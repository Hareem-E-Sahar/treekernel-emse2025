package org.saiko.ai.genetics.tsp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilderFactory;
import org.saiko.ai.genetics.tsp.engines.jgapCrossover.JGapGreedyCrossoverEngine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Dusan Saiko (dusan@saiko.cz)
 * Last change $Date: 2005/08/23 23:18:05 $
 *
 * GUI menu definition and actions for TSP
 */
public class TSPMenu {

    /** String containing the CVS revision. **/
    public static final String CVS_REVISION = "$Revision: 1.9 $";

    /**
    * Parent TSP Gui instance 
    */
    TSP parent;

    /**
    * Class constructor
    * @param parent - parent TSP application
    */
    protected TSPMenu(TSP parent) {
        this.parent = parent;
    }

    /** menu PROGRAM **/
    protected final JMenu menuProgram = new JAntialiasedMenu();

    /** menu item **/
    protected final JMenuItem menuItemStart = new JAntialiasedMenuItem();

    /** menu item **/
    protected final JMenuItem menuItemPause = new JAntialiasedMenuItem();

    /** menu item **/
    protected final JMenuItem menuItemPDFReport = new JAntialiasedMenuItem();

    /** menu item **/
    protected final JMenuItem menuItemXMLReport = new JAntialiasedMenuItem();

    /** menu item **/
    protected final JMenuItem menuItemXML2PDFReport = new JAntialiasedMenuItem();

    /** menu item **/
    protected final JMenuItem menuItemExit = new JAntialiasedMenuItem();

    /** menu MAPS **/
    protected final JMenu menuMaps = new JAntialiasedMenu();

    /** menu item **/
    protected final JMenuItem menuItemExportMaps = new JAntialiasedMenuItem();

    /** menu ENGINE **/
    protected final JMenu menuEngine = new JAntialiasedMenu();

    /** menu SETTINGS **/
    protected final JMenu menuSettings = new JAntialiasedMenu();

    /** menu item **/
    protected final JMenuItem menuItemPopulationSize = new JAntialiasedMenuItem();

    /** menu item **/
    protected final JMenuItem menuItemPopulationGrow = new JAntialiasedMenuItem();

    /** menu item **/
    protected final JMenuItem menuItemMutationRatio = new JAntialiasedMenuItem();

    /** menu item **/
    protected final JMenuItem menuItemMaxBestAge = new JAntialiasedMenuItem();

    /** menu item **/
    protected final JCheckBoxMenuItem menuItemRMS = new JAntialiasedCheckBoxMenuItem();

    /** menu GRAPHICS **/
    protected final JMenu menuGraphics = new JAntialiasedMenu();

    /** menu item **/
    protected final JCheckBoxMenuItem menuItemAntialiasing = new JAntialiasedCheckBoxMenuItem();

    /** menu HELP **/
    protected final JMenu menuHelp = new JAntialiasedMenu();

    /** menu item **/
    protected final JMenuItem menuItemAbout = new JAntialiasedMenuItem();

    /** menu PRIORITY **/
    protected final JMenu menuPriority = new JAntialiasedMenu();

    /** menu item **/
    protected final JRadioButtonMenuItem menuItemPriorityHighest = new JAntialiasedRadioButtonMenuItem();

    /** menu item **/
    protected final JRadioButtonMenuItem menuItemPriorityHigh = new JAntialiasedRadioButtonMenuItem();

    /** menu item **/
    protected final JRadioButtonMenuItem menuItemPriorityNormal = new JAntialiasedRadioButtonMenuItem();

    /** menu item **/
    protected final JRadioButtonMenuItem menuItemPriorityLo = new JAntialiasedRadioButtonMenuItem();

    /** menu item **/
    protected final JRadioButtonMenuItem menuItemPriorityLowest = new JAntialiasedRadioButtonMenuItem();

    /**
    * Create menu for the application
    * @return this menu instance
    */
    public TSPMenu createMenuBar() {
        JMenuBar menuBar;
        menuBar = new JMenuBar();
        resetMenu();
        Font menuFont = new Font("courier", Font.BOLD, menuEngine.getFont().getSize());
        menuBar.add(menuProgram);
        {
            menuProgram.add(menuItemStart);
            menuProgram.add(menuItemPause);
            menuProgram.addSeparator();
            menuProgram.add(menuItemPDFReport);
            menuProgram.add(menuItemXMLReport);
            menuProgram.add(menuItemXML2PDFReport);
            menuProgram.addSeparator();
            menuProgram.add(menuItemExit);
            menuItemPDFReport.setEnabled(false);
            menuItemXMLReport.setEnabled(false);
            setMenuProgramActionListeners();
        }
        menuBar.add(menuMaps);
        {
            addMenuMapsItems();
            setMenuMapsActionListeners();
            menuMaps.addSeparator();
            menuMaps.add(menuItemExportMaps);
        }
        menuBar.add(menuEngine);
        {
            addMenuEnginesItems();
            setMenuEnginesActionListeners();
        }
        menuBar.add(menuSettings);
        {
            menuSettings.add(menuItemPopulationSize);
            menuSettings.add(menuItemPopulationGrow);
            menuSettings.add(menuItemMutationRatio);
            menuSettings.add(menuItemMaxBestAge);
            menuSettings.addSeparator();
            menuSettings.add(menuItemRMS);
            setMenuSettingsActionListeners();
        }
        menuBar.add(menuPriority);
        {
            ButtonGroup group = new ButtonGroup();
            menuPriority.add(menuItemPriorityHighest);
            menuPriority.add(menuItemPriorityHigh);
            menuPriority.add(menuItemPriorityNormal);
            menuPriority.add(menuItemPriorityLo);
            menuPriority.add(menuItemPriorityLowest);
            group.add(menuItemPriorityHighest);
            group.add(menuItemPriorityHigh);
            group.add(menuItemPriorityNormal);
            group.add(menuItemPriorityLo);
            group.add(menuItemPriorityLowest);
            menuItemPriorityNormal.setSelected(true);
            setMenuPriorityActionListeners();
        }
        menuBar.add(menuGraphics);
        {
            menuGraphics.add(menuItemAntialiasing);
            menuItemAntialiasing.setSelected(false);
            setMenuGraphicsActionListeners();
        }
        menuBar.add(menuHelp);
        {
            menuHelp.add(menuItemAbout);
            setMenuHelpActionListeners();
        }
        Component components[] = menuBar.getComponents();
        for (Component c : components) {
            c.setFont(menuFont);
            if (c instanceof JMenu) {
                Component components2[] = ((JMenu) c).getMenuComponents();
                for (Component c2 : components2) {
                    c2.setFont(menuFont);
                }
            }
        }
        parent.gui.setJMenuBar(menuBar);
        return this;
    }

    /**
    * Resets the menu labels
    * menu labels can change during app lifecycle (eg. Start/Stop)
    */
    protected void resetMenu() {
        menuProgram.setText("Program");
        {
            menuItemStart.setText("Start");
            menuItemPause.setText("Pause");
            menuItemPDFReport.setText("Save PDF report as ...");
            menuItemXMLReport.setText("Save XML report as ...");
            menuItemXML2PDFReport.setText("Convert XML report to PDF ...");
            menuItemExit.setText("Exit");
            menuItemPause.setEnabled(false);
            menuItemStart.setEnabled(true);
        }
        menuMaps.setText("Maps");
        {
            menuItemExportMaps.setText("Save maps as ..");
        }
        menuEngine.setText("Engine");
        menuSettings.setText("Settings");
        {
            menuItemPopulationSize.setText(alignText("Population size:", parent.configuration.getInitialPopulationSize()));
            menuItemPopulationGrow.setText(alignText("Population grow:", parent.configuration.getPopulationGrow()));
            menuItemMutationRatio.setText(alignText("Mutation ratio:", parent.configuration.getMutationRatio()));
            menuItemMaxBestAge.setText(alignText("Max best age:", parent.configuration.getMaxBestCostAge()));
            menuItemRMS.setText("RMS cost");
        }
        menuGraphics.setText("Grahics");
        {
            menuItemAntialiasing.setText("Antialiasing");
        }
        menuHelp.setText("Help");
        {
            menuItemAbout.setText("About application");
        }
        menuPriority.setText("Priority");
        {
            menuItemPriorityHighest.setText("Highest");
            menuItemPriorityHigh.setText("High");
            menuItemPriorityNormal.setText("Normal");
            menuItemPriorityLo.setText("Lo");
            menuItemPriorityLowest.setText("Lowest");
        }
        enableMenus(true);
    }

    /**
    * Enables/disables all dependent menus
    * which should not be anabled when the application is running
    * @param enable
    */
    protected void enableMenus(boolean enable) {
        Component subMenus[] = menuMaps.getMenuComponents();
        for (Component menu : subMenus) {
            menu.setEnabled(enable);
        }
        subMenus = menuEngine.getMenuComponents();
        for (Component menu : subMenus) {
            menu.setEnabled(enable);
        }
        subMenus = menuSettings.getMenuComponents();
        for (Component menu : subMenus) {
            menu.setEnabled(enable);
        }
    }

    /**
    * Add menu items to menu
    */
    protected void addMenuEnginesItems() {
        ButtonGroup group = new ButtonGroup();
        for (Class e : TSP.engineClasses) {
            final Class engineClass = e;
            JRadioButtonMenuItem menu = new JAntialiasedRadioButtonMenuItem();
            String menuText = e.getSimpleName();
            menu.setText(menuText);
            menu.putClientProperty(menuText, engineClass);
            if (parent.engineClass.equals(e)) {
                menu.setSelected(true);
            }
            group.add(menu);
            menuEngine.add(menu);
        }
    }

    /**
    * Add action listeners to menu
    */
    protected void setMenuEnginesActionListeners() {
        for (Component m : menuEngine.getMenuComponents()) {
            ((JMenuItem) m).addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    menuItemPDFReport.setEnabled(false);
                    menuItemXMLReport.setEnabled(false);
                    JMenuItem menu = ((JMenuItem) e.getSource());
                    Class engineClass = (Class) menu.getClientProperty(menu.getText());
                    parent.engineClass = engineClass;
                    if (engineClass.equals(JGapGreedyCrossoverEngine.class)) {
                        menuItemPopulationGrow.setVisible(false);
                    } else {
                        menuItemPopulationGrow.setVisible(true);
                    }
                }
            });
        }
    }

    /**
    * Add action listeners to menu
    */
    protected void setMenuPriorityActionListeners() {
        menuItemPriorityHighest.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                parent.configuration.setThreadPriority(10);
                if (parent.runingThread != null) {
                    parent.runingThread.setPriority(parent.configuration.getThreadPriority());
                }
            }
        });
        menuItemPriorityHigh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                parent.configuration.setThreadPriority(7);
                if (parent.runingThread != null) {
                    parent.runingThread.setPriority(parent.configuration.getThreadPriority());
                }
            }
        });
        menuItemPriorityNormal.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                parent.configuration.setThreadPriority(5);
                if (parent.runingThread != null) {
                    parent.runingThread.setPriority(parent.configuration.getThreadPriority());
                }
            }
        });
        menuItemPriorityLo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                parent.configuration.setThreadPriority(3);
                if (parent.runingThread != null) {
                    parent.runingThread.setPriority(parent.configuration.getThreadPriority());
                }
            }
        });
        menuItemPriorityLowest.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                parent.configuration.setThreadPriority(1);
                if (parent.runingThread != null) {
                    parent.runingThread.setPriority(parent.configuration.getThreadPriority());
                }
            }
        });
    }

    /**
    * Menu item action
    * @param e
    */
    protected void actionAntialiasing(ActionEvent e) {
        JCheckBoxMenuItem menu = (JCheckBoxMenuItem) e.getSource();
        if (menu.isSelected()) {
            parent.configuration.setAntialiasing(true);
        } else {
            parent.configuration.setAntialiasing(false);
        }
        parent.gui.invalidate();
        parent.gui.repaint();
    }

    /**
    * Menu item action
    * @param e
    */
    protected void actionPause(@SuppressWarnings("unused") ActionEvent e) {
        if (parent.pauseRequestFlag) {
            parent.pauseRequestFlag = false;
            menuItemPause.setText("Pause");
            menuItemStart.setEnabled(true);
            parent.runingThread.interrupt();
        } else {
            menuItemPause.setText("Resume");
            parent.pauseRequestFlag = true;
            menuItemStart.setEnabled(false);
        }
    }

    /**
    * Menu item action
    * @param e
    */
    protected void actionStart(@SuppressWarnings("unused") ActionEvent e) {
        if (parent.startedFlag == false) {
            enableMenus(false);
            parent.gui.statusBar.setText("Initializing..");
            parent.startedFlag = true;
            menuItemStart.setText("Stop");
            menuItemPause.setEnabled(true);
            menuItemPDFReport.setEnabled(false);
            menuItemXMLReport.setEnabled(false);
            menuItemXML2PDFReport.setEnabled(false);
            parent.runingThread = new Thread(new Runnable() {

                public void run() {
                    parent.run();
                }
            });
            parent.runingThread.setPriority(parent.configuration.getThreadPriority());
            parent.runingThread.start();
        } else {
            menuItemPDFReport.setEnabled(false);
            menuItemXMLReport.setEnabled(false);
            menuItemXML2PDFReport.setEnabled(false);
            menuItemStart.setEnabled(false);
            menuItemPause.setEnabled(false);
            parent.stopRequestFlag = true;
        }
    }

    /**
    * Aligns text for menu items in Settings menu
    * @param str
    * @param value
    * @return aligned text
    */
    protected String alignText(String str, Object value) {
        String str2 = value.toString();
        while (str.length() < 20) {
            str += " ";
        }
        if (value instanceof Double) {
            DecimalFormat f = new DecimalFormat("#################.################");
            double d = ((Double) value).doubleValue();
            str2 = f.format(d);
        }
        if (str2.indexOf('.') == -1) {
            while (str2.length() < 7) {
                str2 = " " + str2;
            }
        } else {
            while (str2.indexOf('.') < 7) {
                str2 = " " + str2;
            }
        }
        return str + str2;
    }

    /**
    * JMenu with support for antialiasing
    */
    class JAntialiasedMenu extends JMenu {

        /**
       * serialVersionUID
       */
        private static final long serialVersionUID = 6749630508955245193L;

        @Override
        public void paint(Graphics g) {
            if (parent.configuration.isAntialiasing()) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }
            super.paint(g);
        }
    }

    /**
    * JMenuItem with support for antialiasing
    */
    class JAntialiasedMenuItem extends JMenuItem {

        /**
       * serialVersionUID
       */
        private static final long serialVersionUID = 7591332664361538950L;

        @Override
        public void paint(Graphics g) {
            if (parent.configuration.isAntialiasing()) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }
            super.paint(g);
        }
    }

    /**
    * JMenuItem with support for antialiasing
    */
    class JAntialiasedCheckBoxMenuItem extends JCheckBoxMenuItem {

        /**
       * serialVersionUID
       */
        private static final long serialVersionUID = 5452268115174827502L;

        @Override
        public void paint(Graphics g) {
            if (parent.configuration.isAntialiasing()) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }
            super.paint(g);
        }
    }

    /**
    * JMenuItem with support for antialiasing
    */
    class JAntialiasedRadioButtonMenuItem extends JRadioButtonMenuItem {

        /**
       * serialVersionUID
       */
        private static final long serialVersionUID = 3277282470785731582L;

        @Override
        public void paint(Graphics g) {
            if (parent.configuration.isAntialiasing()) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }
            super.paint(g);
        }
    }

    /**
    * Input dialog for double value
    * @param message
    * @param doubleValue - default value for input
    * @return input value or null
    */
    protected Double doubleInputDialog(String message, double doubleValue) {
        String value = (String) JOptionPane.showInputDialog(parent.gui, message, "TSP Settings", JOptionPane.QUESTION_MESSAGE, null, null, new DecimalFormat("################.################").format(doubleValue));
        if (value == null) return null;
        try {
            Double d = Double.parseDouble(value);
            if (d >= 0 && d <= 1) return d;
        } catch (Throwable ex) {
        }
        JOptionPane.showMessageDialog(parent.gui, "Can not use this value.", "Error", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    /**
    * Input dialog for integer value
    * @param message
    * @param intValue
    * @return input value or null
    */
    protected Integer intInputDialog(String message, int intValue) {
        String value = (String) JOptionPane.showInputDialog(parent.gui, message, "TSP Settings", JOptionPane.QUESTION_MESSAGE, null, null, new DecimalFormat("################").format(intValue));
        if (value == null) return null;
        try {
            Integer i = Integer.parseInt(value);
            if (i >= 10) return i;
        } catch (Throwable ex) {
        }
        JOptionPane.showMessageDialog(parent.gui, "Can not use this value.", "Error", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    /**
   * preview save dir for map export 
   */
    File mapsPrevDir = null;

    /**
    * Saves/export all available maps into .zip file.
    * @param e
    */
    protected void actionExportMaps(@SuppressWarnings("unused") ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        if (mapsPrevDir == null) {
            mapsPrevDir = new File(".").getAbsoluteFile();
        } else {
            fileChooser.setCurrentDirectory(mapsPrevDir);
        }
        fileChooser.setSelectedFile(new File(mapsPrevDir, "tsp_maps.zip"));
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getAbsolutePath().toLowerCase().endsWith(".zip")) return true;
                return false;
            }

            @Override
            public String getDescription() {
                return "*.zip";
            }
        });
        if (fileChooser.showSaveDialog(parent.gui) == JFileChooser.APPROVE_OPTION) {
            try {
                mapsPrevDir = fileChooser.getCurrentDirectory();
                File f = fileChooser.getSelectedFile();
                String fileName = f.getName();
                if (!fileName.toLowerCase().endsWith(".zip")) {
                    String ext = "zip";
                    if (!fileName.endsWith(".")) {
                        ext = "." + ext;
                    }
                    fileName += ext;
                    File parentDir = f.getParentFile();
                    f = new File(parentDir, fileName);
                }
                if (f.exists()) {
                    if (JOptionPane.showConfirmDialog(parent.gui, "Should be existing file overwritten ?", "Question", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
                ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(f));
                zip.setLevel(9);
                for (String mapName : TSP.mapFiles) {
                    if (mapName != null) {
                        InputStream i = new Object().getClass().getResourceAsStream("/org/saiko/ai/genetics/tsp/etc/" + mapName + ".csv");
                        if (i != null) {
                            ZipEntry zipEntry = new ZipEntry(mapName + ".csv");
                            zip.putNextEntry(zipEntry);
                            byte b[] = new byte[1024];
                            int size;
                            while ((size = i.read(b)) > 0) {
                                zip.write(b, 0, size);
                            }
                            zip.closeEntry();
                        }
                        i.close();
                    }
                }
                zip.close();
                JOptionPane.showMessageDialog(parent.gui, "OK, maps exported to the file: \n" + f, "Info", JOptionPane.INFORMATION_MESSAGE);
            } catch (Throwable ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent.gui, "Can not export maps.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
  * preview save dir for PDF report
  */
    File reportPrevDir = null;

    /**
   * Creates PDF report for current computation
   * NOTE: when running application on some server machines, no all needed graphics
   * libraries were installed there and image operations and even antialiasing for example
   * were not working there (in general).
   * @param e
   */
    protected void actionPDFReport(@SuppressWarnings("unused") ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        if (reportPrevDir == null) {
            reportPrevDir = new File(".").getAbsoluteFile();
        }
        fileChooser.setCurrentDirectory(reportPrevDir);
        String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(Calendar.getInstance().getTime());
        fileChooser.setSelectedFile(new File(reportPrevDir, "tsp_report_" + timestamp + ".pdf"));
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getAbsolutePath().toLowerCase().endsWith(".pdf")) return true;
                return false;
            }

            @Override
            public String getDescription() {
                return "*.pdf";
            }
        });
        if (fileChooser.showSaveDialog(parent.gui) == JFileChooser.APPROVE_OPTION) {
            try {
                reportPrevDir = fileChooser.getCurrentDirectory();
                File filePDF = fileChooser.getSelectedFile();
                String fileName = filePDF.getName();
                if (!fileName.toLowerCase().endsWith(".pdf")) {
                    String ext = "pdf";
                    if (!fileName.endsWith(".")) {
                        ext = "." + ext;
                    }
                    fileName += ext;
                    File parentDir = filePDF.getParentFile();
                    filePDF = new File(parentDir, fileName);
                }
                if (filePDF.exists()) {
                    if (JOptionPane.showConfirmDialog(parent.gui, "Should be existing file overwritten ?", "Question", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
                Component c = parent.gui.getContentPane();
                BufferedImage screenImage = (BufferedImage) c.createImage(c.getWidth(), c.getHeight());
                Graphics2D graphics = screenImage.createGraphics();
                boolean antialiasing = parent.configuration.antialiasing;
                parent.configuration.antialiasing = true;
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                parent.gui.statusBar.setVisible(false);
                c.paint(graphics);
                parent.gui.statusBar.setVisible(true);
                parent.configuration.antialiasing = antialiasing;
                Map<String, String> params = Report.getResultInfo(parent);
                City cities[] = parent.cities;
                if (parent.bestChromosome != null) {
                    cities = parent.bestChromosome.cities;
                }
                City cities2[] = new City[cities.length + 1];
                int i;
                int iStart = 0;
                for (i = 0; i < cities.length; i++) {
                    if (cities[i].startCity) {
                        iStart = i;
                        break;
                    }
                }
                i = 0;
                while (i < cities.length) {
                    cities2[i] = cities[iStart];
                    iStart++;
                    i++;
                    if (iStart >= cities.length) iStart = 0;
                }
                cities2[cities.length] = cities2[0];
                new Report().saveReport(filePDF, cities2, screenImage, params, Report.getSystemProperties());
                JOptionPane.showMessageDialog(parent.gui, "OK, report created to the file: \n" + filePDF, "Info", JOptionPane.INFORMATION_MESSAGE);
                try {
                    Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", filePDF.getAbsolutePath() });
                } catch (Throwable ex2) {
                    try {
                        Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", "start", filePDF.getAbsolutePath() });
                    } catch (Throwable ex3) {
                    }
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent.gui, "Can not create pdf report.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
    * Menu item action listeners
    */
    protected void setMenuProgramActionListeners() {
        menuItemExit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menuItemStart.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionStart(e);
            }
        });
        menuItemPause.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionPause(e);
            }
        });
        menuItemPDFReport.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionPDFReport(e);
            }
        });
        menuItemXMLReport.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionXMLReport(null);
            }
        });
        menuItemXML2PDFReport.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionXML2PDFReport(e);
            }
        });
    }

    /**
    * Menu item action listeners
    */
    protected void setMenuSettingsActionListeners() {
        menuItemPopulationSize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                menuItemPDFReport.setEnabled(false);
                menuItemXMLReport.setEnabled(false);
                Integer value = intInputDialog("Initial population size", parent.configuration.getInitialPopulationSize());
                if (value != null) {
                    parent.configuration.setInitialPopulationSize(value);
                    resetMenu();
                }
            }
        });
        menuItemPopulationGrow.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                menuItemPDFReport.setEnabled(false);
                menuItemXMLReport.setEnabled(false);
                Double value = doubleInputDialog("Population grow (float number <= 1)", parent.configuration.getPopulationGrow());
                if (value != null) {
                    parent.configuration.setPopulationGrow(value);
                    resetMenu();
                }
            }
        });
        menuItemMutationRatio.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                menuItemPDFReport.setEnabled(false);
                menuItemXMLReport.setEnabled(false);
                Double value = doubleInputDialog("Mutation ratio (float number <= 1)", parent.configuration.getMutationRatio());
                if (value != null) {
                    parent.configuration.setMutationRatio(value);
                    resetMenu();
                }
            }
        });
        menuItemMaxBestAge.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                menuItemPDFReport.setEnabled(false);
                menuItemXMLReport.setEnabled(false);
                Integer value = intInputDialog("Maximum best cost age", parent.configuration.getMaxBestCostAge());
                if (value != null) {
                    parent.configuration.setMaxBestCostAge(value);
                    resetMenu();
                }
            }
        });
        menuItemRMS.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                menuItemPDFReport.setEnabled(false);
                menuItemXMLReport.setEnabled(false);
                if (menuItemRMS.isSelected()) {
                    parent.configuration.setRmsCost(true);
                } else {
                    parent.configuration.setRmsCost(false);
                }
            }
        });
    }

    /**
    * Menu item action listeners
    */
    protected void addMenuMapsItems() {
        ButtonGroup group = new ButtonGroup();
        for (String m : TSP.mapFiles) {
            if (m == null) {
                menuMaps.addSeparator();
            } else {
                JRadioButtonMenuItem menu = new JAntialiasedRadioButtonMenuItem();
                menu.setText(m);
                if (parent.mapFile.equals(m)) {
                    menu.setSelected(true);
                }
                group.add(menu);
                menuMaps.add(menu);
            }
        }
    }

    /**
    * Menu item action listeners
    */
    protected void setMenuMapsActionListeners() {
        for (Component m : menuMaps.getMenuComponents()) {
            if (m instanceof JMenuItem) {
                ((JMenuItem) m).addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        menuItemPDFReport.setEnabled(false);
                        menuItemXMLReport.setEnabled(false);
                        JMenuItem menu = (JMenuItem) e.getSource();
                        parent.mapFile = menu.getText();
                        parent.gui.createCityMap(true);
                    }
                });
            }
        }
        menuItemExportMaps.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionExportMaps(e);
            }
        });
    }

    /**
    * Menu item action listeners
    */
    protected void setMenuGraphicsActionListeners() {
        menuItemAntialiasing.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionAntialiasing(e);
            }
        });
    }

    /**
    * Menu item action listeners
    */
    protected void setMenuHelpActionListeners() {
        menuItemAbout.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionAbout(e);
            }
        });
    }

    /**
    * Creates computation report to XML file 
    * @param XMLFileName fileName of output report, if NULL, then dialog will be displayed
    */
    protected void actionXMLReport(String XMLFileName) {
        if (XMLFileName == null) {
            JFileChooser fileChooser = new JFileChooser();
            if (reportPrevDir == null) {
                reportPrevDir = new File(".").getAbsoluteFile();
            }
            fileChooser.setCurrentDirectory(reportPrevDir);
            String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(Calendar.getInstance().getTime());
            fileChooser.setSelectedFile(new File(reportPrevDir, "tsp_report_" + timestamp + ".xml"));
            fileChooser.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) return true;
                    if (f.getAbsolutePath().toLowerCase().endsWith(".xml")) return true;
                    return false;
                }

                @Override
                public String getDescription() {
                    return "*.xml";
                }
            });
            if (fileChooser.showSaveDialog(parent.gui) == JFileChooser.APPROVE_OPTION) {
                reportPrevDir = fileChooser.getCurrentDirectory();
                XMLFileName = fileChooser.getSelectedFile().getAbsolutePath();
            } else {
                return;
            }
        }
        try {
            File fileXML = new File(XMLFileName);
            String fileName = fileXML.getName();
            if (!fileName.toLowerCase().endsWith(".xml")) {
                String ext = "xml";
                if (!fileName.endsWith(".")) {
                    ext = "." + ext;
                }
                fileName += ext;
                File parentDir = fileXML.getParentFile();
                fileXML = new File(parentDir, fileName);
            }
            if (!parent.configuration.console) {
                if (fileXML.exists()) {
                    if (JOptionPane.showConfirmDialog(parent.gui, "Should be existing file overwritten ?", "Question", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
            }
            PrintWriter report = new PrintWriter(fileXML.getAbsolutePath(), "UTF-8");
            report.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            report.println();
            report.println("<tsp-report>");
            report.println(" <results>");
            Map<String, String> params = Report.getResultInfo(parent);
            Iterator keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                report.println("  <result name=\"" + key + "\" value=\"" + params.get(key) + "\"/>");
            }
            report.println(" </results>");
            report.println(" <system-info>");
            Map<String, String> info = Report.getSystemProperties();
            keys = info.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                report.println("  <info name=\"" + key + "\" value=\"" + info.get(key) + "\"/>");
            }
            report.println(" </system-info>");
            City cities[] = parent.cities;
            if (parent.bestChromosome != null) {
                cities = parent.bestChromosome.cities;
            }
            City cities2[] = new City[cities.length + 1];
            int i;
            int iStart = 0;
            for (i = 0; i < cities.length; i++) {
                if (cities[i].startCity) {
                    iStart = i;
                    break;
                }
            }
            i = 0;
            while (i < cities.length) {
                cities2[i] = cities[iStart];
                iStart++;
                i++;
                if (iStart >= cities.length) iStart = 0;
            }
            cities2[cities.length] = cities2[0];
            report.println(" <path>");
            for (City city : cities2) {
                report.println(" <city name=\"" + city.name + "\" x=\"" + city.SJTSKX + "\" y=\"" + city.SJTSKY + "\"/>");
            }
            report.println(" </path>");
            report.print("</tsp-report>");
            report.close();
            if (!parent.configuration.console) {
                JOptionPane.showMessageDialog(parent.gui, "OK, report created to the file: \n" + fileXML, "Info", JOptionPane.INFORMATION_MESSAGE);
                try {
                    Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", fileXML.getAbsolutePath() });
                } catch (Throwable ex2) {
                    try {
                        Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", "start", fileXML.getAbsolutePath() });
                    } catch (Throwable ex3) {
                    }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent.gui, "Can not create pdf report.", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
    * Creates PDF Report from previously saved XML report 
    * @param e
    */
    protected void actionXML2PDFReport(@SuppressWarnings("unused") ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        if (reportPrevDir == null) {
            reportPrevDir = new File(".").getAbsoluteFile();
        }
        fileChooser.setCurrentDirectory(reportPrevDir);
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getAbsolutePath().toLowerCase().endsWith(".xml")) return true;
                return false;
            }

            @Override
            public String getDescription() {
                return "*.xml";
            }
        });
        File fileXML = null;
        Map<String, String> params = new LinkedHashMap<String, String>();
        Map<String, String> info = new LinkedHashMap<String, String>();
        City path[] = null;
        BufferedImage screenImage = null;
        if (fileChooser.showOpenDialog(parent.gui) == JFileChooser.APPROVE_OPTION) {
            try {
                reportPrevDir = fileChooser.getCurrentDirectory();
                fileXML = fileChooser.getSelectedFile();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                Document document = factory.newDocumentBuilder().parse(new InputSource(new FileInputStream(fileXML)));
                Element root = document.getDocumentElement();
                {
                    NodeList resultInfos = root.getElementsByTagName("result");
                    for (int i = 0; i < resultInfos.getLength(); i++) {
                        Node resultInfo = resultInfos.item(i);
                        NamedNodeMap attributes = resultInfo.getAttributes();
                        String name = attributes.getNamedItem("name").getNodeValue();
                        String value = attributes.getNamedItem("value").getNodeValue();
                        params.put(name, value);
                    }
                }
                {
                    NodeList systemInfos = root.getElementsByTagName("info");
                    for (int i = 0; i < systemInfos.getLength(); i++) {
                        Node systemInfo = systemInfos.item(i);
                        NamedNodeMap attributes = systemInfo.getAttributes();
                        String name = attributes.getNamedItem("name").getNodeValue();
                        String value = attributes.getNamedItem("value").getNodeValue();
                        info.put(name, value);
                    }
                }
                {
                    NodeList cities = root.getElementsByTagName("city");
                    path = new City[cities.getLength()];
                    for (int i = 0; i < cities.getLength(); i++) {
                        Node city = cities.item(i);
                        NamedNodeMap attributes = city.getAttributes();
                        String name = attributes.getNamedItem("name").getNodeValue();
                        String x = attributes.getNamedItem("x").getNodeValue();
                        String y = attributes.getNamedItem("y").getNodeValue();
                        path[i] = new City(i, null, name, Integer.parseInt(x), Integer.parseInt(y));
                    }
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent.gui, "Can not read XML report file.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            return;
        }
        fileChooser = new JFileChooser();
        if (reportPrevDir == null) {
            reportPrevDir = fileChooser.getCurrentDirectory();
        } else {
            fileChooser.setCurrentDirectory(reportPrevDir);
        }
        String pdfFileName = fileXML.getName();
        int iDot = pdfFileName.lastIndexOf('.');
        if (iDot != -1) {
            pdfFileName = pdfFileName.substring(0, iDot);
        }
        if (!pdfFileName.endsWith(".")) {
            pdfFileName += ".";
        }
        pdfFileName += "pdf";
        fileChooser.setSelectedFile(new File(fileXML.getParent(), pdfFileName));
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getAbsolutePath().toLowerCase().endsWith(".pdf")) return true;
                return false;
            }

            @Override
            public String getDescription() {
                return "*.pdf";
            }
        });
        if (fileChooser.showSaveDialog(parent.gui) == JFileChooser.APPROVE_OPTION) {
            try {
                reportPrevDir = fileChooser.getCurrentDirectory();
                File filePDF = fileChooser.getSelectedFile();
                String fileName = filePDF.getName();
                if (!fileName.toLowerCase().endsWith(".pdf")) {
                    String ext = "pdf";
                    if (!fileName.endsWith(".")) {
                        ext = "." + ext;
                    }
                    fileName += ext;
                    File parentDir = filePDF.getParentFile();
                    filePDF = new File(parentDir, fileName);
                }
                if (filePDF.exists()) {
                    if (JOptionPane.showConfirmDialog(parent.gui, "Should be existing file overwritten ?", "Question", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
                {
                    TSP tsp2 = new TSP(false);
                    tsp2.cities = new City[path.length - 1];
                    tsp2.configuration.setAntialiasing(true);
                    for (int i = 0; i < tsp2.cities.length; i++) {
                        tsp2.cities[i] = path[i];
                        tsp2.cities[i].configuration = tsp2.configuration;
                    }
                    tsp2.loadCities(tsp2.cities, false);
                    for (int i = 0; i < tsp2.cities.length; i++) {
                        path[i] = tsp2.cities[i];
                    }
                    path[path.length - 1] = path[0];
                    tsp2.start();
                    tsp2.gui.setEnabled(false);
                    tsp2.bestChromosome = new TSPChromosome(tsp2.cities, false);
                    tsp2.gui.repaint();
                    Component c = tsp2.gui.getContentPane();
                    screenImage = (BufferedImage) c.createImage(c.getWidth(), c.getHeight());
                    Graphics2D graphics = screenImage.createGraphics();
                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    tsp2.gui.statusBar.setVisible(false);
                    c.paint(graphics);
                    tsp2.gui.dispose();
                }
                new Report().saveReport(filePDF, path, screenImage, params, info);
                JOptionPane.showMessageDialog(parent.gui, "OK, report created to the file: \n" + filePDF, "Info", JOptionPane.INFORMATION_MESSAGE);
                try {
                    Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", filePDF.getAbsolutePath() });
                } catch (Throwable ex2) {
                    try {
                        Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", "start", filePDF.getAbsolutePath() });
                    } catch (Throwable ex3) {
                    }
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent.gui, "Can not create pdf report.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            return;
        }
    }

    /**
    * Displays application help 
    * @param e
    */
    protected void actionAbout(ActionEvent e) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/org/saiko/ai/genetics/tsp/etc/about.html")));
        try {
            StringBuffer message = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                message.append(line);
                message.append(" ");
            }
            reader.close();
            showHtmlDialog("About application", message.toString().replaceAll("__VERSION__", TSP.getAppVersion()));
        } catch (Exception ex) {
        }
    }

    /**
	 * Shows dialog with html content
	 * @param title
	 * @param htmlMessage
	*/
    protected void showHtmlDialog(String title, String htmlMessage) {
        final JDialog dlg = new JDialog(parent.gui);
        dlg.setSize(600, 400);
        dlg.setTitle(title);
        dlg.setLayout(new BorderLayout());
        dlg.setComponentOrientation(parent.gui.getComponentOrientation());
        dlg.setLocationRelativeTo(parent.gui);
        dlg.setResizable(true);
        JButton ok = new JButton(" Close ");
        ok.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dlg.dispose();
            }
        });
        JPanel south = new JPanel();
        dlg.add(south, BorderLayout.SOUTH);
        south.add(ok);
        dlg.setModal(true);
        JScrollPane scrollPane = new JScrollPane();
        JEditorPane content = new JEditorPane("text/html", htmlMessage);
        content.setCaretPosition(0);
        content.setEditable(false);
        content.setBackground(null);
        content.setOpaque(true);
        content.addHyperlinkListener(createHyperLinkListener());
        scrollPane.getViewport().add(content);
        dlg.add(scrollPane, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    /**
    * @return HyperlinkListener which transfers the requests to system
   */
    protected static HyperlinkListener createHyperLinkListener() {
        return new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String url = e.getURL().toExternalForm();
                    try {
                        Runtime.getRuntime().exec(new String[] { url });
                    } catch (Throwable ex2) {
                        try {
                            Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", "start", url });
                        } catch (Throwable ex3) {
                        }
                    }
                }
            }
        };
    }
}
