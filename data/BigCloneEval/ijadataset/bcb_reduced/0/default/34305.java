import javax.swing.*;
import javax.swing.text.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GraphViewer extends JFrame {

    private GraphDrawer drawer;

    private String dir = "";

    public GraphViewer() {
        initComponents();
        pack();
        setSize(620, 460);
        java.awt.Dimension winSize = getSize();
        java.awt.Dimension scrSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((scrSize.width - winSize.width) >> 1, (scrSize.height - winSize.height) >> 1);
    }

    public static void main(String[] arg) {
        GraphViewer theFrame = new GraphViewer();
        theFrame.show();
    }

    private void initComponents() {
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jSeparator1 = new javax.swing.JSeparator();
        miExit = new javax.swing.JMenuItem();
        miOptions = new javax.swing.JMenuItem();
        miOpen = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jReport = new JTextArea("", 4, 0);
        jReport.setEditable(false);
        setTitle(frameName);
        jMenu1.setText("File");
        miOpen.setText("Open");
        miOpen.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                miOpenActionPerformed(evt);
            }
        });
        jMenu1.add(miOpen);
        miOptions.setText("Options...");
        jMenu1.add(miOptions);
        jMenu1.add(jSeparator1);
        miExit.setText("Exit");
        miExit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                miExitActionPerformed(evt);
            }
        });
        jMenu1.add(miExit);
        jMenuBar1.add(jMenu1);
        jMenu2.setText("About");
        jMenuBar1.add(jMenu2);
        jScrollPane1.setBorder(new javax.swing.border.EtchedBorder());
        jScrollPane2.setBorder(new javax.swing.border.EtchedBorder());
        drawer = new GraphDrawer(jReport);
        jScrollPane1.setViewportView(drawer);
        jScrollPane1.getVerticalScrollBar().setBlockIncrement(10);
        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jScrollPane2.setViewportView(jReport);
        getContentPane().add(jScrollPane2, java.awt.BorderLayout.SOUTH);
        setJMenuBar(jMenuBar1);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private void miExitActionPerformed(ActionEvent evt) {
        System.exit(0);
    }

    private void miOpenActionPerformed(ActionEvent evt) {
        JFileChooser chooser = new JFileChooser(dir);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        ExtensionFileFilter filter = new ExtensionFileFilter("grf", "GRAPH representation files (*.grf)");
        chooser.setFileFilter(filter);
        int res = chooser.showOpenDialog(this);
        String filename = chooser.getSelectedFile().getPath();
        try {
            Document doc = jReport.getDocument();
            doc.remove(0, doc.getLength());
            Graph graf = new Graph(filename);
            dir = chooser.getSelectedFile().getParent();
            drawer.setGraph(graf, dir);
            setTitle(frameName + " " + filename);
        } catch (Graph.GraphConstructorException ex) {
            jReport.append("Error: file <" + filename + "> has illegal format or not exist\n");
        } catch (java.io.EOFException ex) {
            jReport.append("Error: " + ex.getMessage() + " file format not recognized\n");
        } catch (BadLocationException ex) {
            System.out.println(ex);
        }
        invalidate();
        validate();
        repaint();
        System.out.println(filename);
    }

    private javax.swing.JMenuBar jMenuBar1;

    private javax.swing.JMenu jMenu1;

    private javax.swing.JSeparator jSeparator1;

    private javax.swing.JMenuItem miExit;

    private javax.swing.JMenu jMenu2;

    private javax.swing.JMenuItem miOptions;

    private javax.swing.JMenuItem miOpen;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JTextArea jReport;

    private String frameName = "��������";
}
