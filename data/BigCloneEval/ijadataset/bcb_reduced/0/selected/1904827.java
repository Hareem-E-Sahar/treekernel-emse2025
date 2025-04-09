package actions.menugeneral;

import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import data.Data;
import data.TableManagment;

/**
 * Action, which is occurred when user clicks "Export..." component, it saves
 * current chart as image file to a disk.
 *
 * @author <a href="mailto:vixentael@gmail.com">Voitova Anastasiia</a>
 *
 */
public class ExportingAction extends AbstractAction {

    /**
     * JDK 1.1 serialVersionUID.
     */
    private static final long serialVersionUID = 5745683949359609280L;

    private JFrame mainFrame;

    private TableManagment tm;

    /**
     * @param data
     * @param mainFrame
     */
    public ExportingAction(Data data, JFrame mainFrame) {
        super();
        this.mainFrame = mainFrame;
        tm = new TableManagment(data);
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(final ActionEvent arg0) {
        JFileChooser fc = new JFileChooser();
        FileFilter ff1 = new FileFilter() {

            @Override
            public boolean accept(final File f) {
                return f.getName().toLowerCase().endsWith(".png") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "PNG-files (*.png)";
            }
        };
        FileFilter ff2 = new FileFilter() {

            @Override
            public boolean accept(final File f) {
                return f.getName().toLowerCase().endsWith(".txt") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "TEXT-files (*.txt)";
            }
        };
        FileFilter ff3 = new FileFilter() {

            @Override
            public boolean accept(final File f) {
                return f.getName().toLowerCase().endsWith(".html") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "HTML-files (*.html)";
            }
        };
        fc.setCurrentDirectory(new File("."));
        fc.addChoosableFileFilter(ff1);
        fc.addChoosableFileFilter(ff2);
        fc.addChoosableFileFilter(ff3);
        int returnVal = fc.showSaveDialog(mainFrame);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String path = fc.getSelectedFile().getAbsolutePath();
        if (fc.getFileFilter() == ff1) {
            if (!path.endsWith(".png")) {
                path += ".png";
            }
            try {
                BufferedImage bufim = new BufferedImage(mainFrame.getWidth(), mainFrame.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics g = bufim.getGraphics();
                mainFrame.paint(g);
                FileOutputStream os = new FileOutputStream(path);
                ImageIO.write(bufim, "png", os);
                os.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (fc.getFileFilter() == ff2) {
            if (!path.endsWith(".txt")) {
                path += ".txt";
            }
            try {
                FileWriter fileW = new FileWriter(path);
                BufferedWriter outf = new BufferedWriter(fileW);
                int di[][] = tm.getMatrixDi();
                writeMatrixToTxt("Input Matrix", di, outf);
                int dq[][] = tm.getMatrixDq();
                writeMatrixToTxt("Output Matrix", dq, outf);
                int dr[][] = tm.getMatrixDr(di, dq);
                writeMatrixToTxt("Dr matrix", dr, outf);
                int mark[] = tm.getMarkirovka();
                writeArrayToTxt("Markirovka", mark, outf);
                outf.close();
                fileW.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (fc.getFileFilter() == ff3) {
            if (!path.endsWith(".html")) {
                path += ".html";
            }
            makeHTML(path);
            launchHTML(path);
        }
    }

    protected void launchHTML(String path) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            File file = new File(path);
            try {
                desktop.open(file);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame, "Help file \"UserGuide.htm\" can't be opened.", "Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("Error: Help file \"UserGuide.htm\"" + "can't be opened.");
            }
        }
    }

    protected void makeHTML(String path) {
        try {
            FileWriter fileW = new FileWriter(path);
            BufferedWriter outf = new BufferedWriter(fileW);
            outf.write("<html>\n<head>\n<title>Matrixes</title>");
            outf.write("</head>\n");
            outf.write("<body bgcolor=\"#ccffcc\" text=\"#000000\"> \n");
            int di[][] = tm.getMatrixDi();
            int dq[][] = tm.getMatrixDq();
            writeMatrixToHTML("Di", di, outf);
            writeMatrixToHTML("Dq", dq, outf);
            int mark[] = tm.getMarkirovka();
            writeArrayToHTML(mark, outf);
            outf.write("</body>\n </html>\n");
            outf.close();
            fileW.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeMatrixToHTML(String name, int[][] matr, BufferedWriter outf) {
        try {
            outf.write("<table bordercolor=\"#003300\" border=\"1\" " + "cellspacing=\"0\" cellpadding=\"3\" align=center> \n");
            outf.write("<TH><H2>" + name + "</H2></TH>\n");
            outf.write("<tr> \n");
            for (int j = 0; j < tm.getAllP().size(); j++) {
                if (j == 0) {
                    outf.write("<td> \t </td>");
                }
                outf.write("<td bgcolor=\"ffffcc\"> \n " + "<bold> " + tm.getAllP().get(j).getTitle() + " </bold>\n" + "</td> \n");
            }
            outf.write("</tr> \n");
            for (int i = 0; i < tm.getAllT().size(); i++) {
                outf.write("<tr> \n");
                outf.write("<td bgcolor=\"ffffcc\"> \n " + "<bold> " + tm.getAllT().get(i).getTitle() + "</bold> \n " + "</td> \n");
                for (int j = 0; j < tm.getAllP().size(); j++) {
                    outf.write("<td> \n");
                    outf.write(matr[i][j] + "\t");
                    outf.write("</td>\n");
                }
                outf.write("</tr>\n");
            }
            outf.write("</table>\n");
            outf.write("<br>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeArrayToHTML(int[] mark, BufferedWriter outf) {
        try {
            outf.write("<div align=\"center\">");
            outf.write("M = { ");
            for (int j = 0; j < tm.getAllP().size(); j++) {
                outf.write(mark[j] + " \t");
            }
            outf.write("} </div>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeMatrixToTxt(String name, int[][] matr, BufferedWriter outf) {
        try {
            outf.write(name + "\n");
            for (int i = 0; i < tm.getAllT().size(); i++) {
                for (int j = 0; j < tm.getAllP().size(); j++) {
                    outf.write(matr[i][j] + "\t");
                }
                outf.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeArrayToTxt(String name, int[] mark, BufferedWriter outf) {
        try {
            outf.write(name + "\n");
            for (int j = 0; j < tm.getAllP().size(); j++) {
                outf.write(mark[j] + "\t");
            }
            outf.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
