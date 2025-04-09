package org.sloc;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.sloc.config.FileType;
import org.sloc.img.ImageRegistry;
import org.sloc.model.SLOCTableModel;
import org.sloc.view.SLOCCounterView;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceGreen;

/**
 * @author G. Vaidhyanathan
 */
public class SLOCController {

    private SLOCTableModel model;

    private SLOCCounterView view;

    private JDialog dialog;

    private JFrame frame = new JFrame("SLOC Counter...");

    private JLabel busyIconLbl = new JLabel();

    private boolean blankLinesIgnored;

    public SLOCController() {
        view = new SLOCCounterView();
        model = new SLOCTableModel();
        model.setView(view);
        view.setController(this);
        view.setModel(model);
    }

    /**
   * @return the model
   */
    public SLOCTableModel getModel() {
        return model;
    }

    /**
   * @param model_p
   *          the model to set
   */
    public void setModel(SLOCTableModel model_p) {
        model = model_p;
    }

    /**
   * @return the view
   */
    public SLOCCounterView getView() {
        return view;
    }

    /**
   * @param view_p
   *          the view to set
   */
    public void setView(SLOCCounterView view_p) {
        view = view_p;
    }

    public void createAndShowSLOCCounter() {
        view.initComponents();
        initDialog();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(view.getMenuBar());
        frame.setContentPane(view);
        frame.pack();
        locateOnScreen(frame);
        frame.setVisible(true);
        frame.setIconImage(((ImageIcon) ImageRegistry.getIcon(ImageRegistry.COUNT_ICON)).getImage());
        frame.addWindowListener(new WindowAdapter() {

            /**
       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
       */
            @SuppressWarnings("synthetic-access")
            @Override
            public void windowClosing(WindowEvent e_p) {
                SLOCCounterUtil.getInstance().saveXML();
                dialog.dispose();
                super.windowClosing(e_p);
            }
        });
    }

    /**
   *
   */
    private void initDialog() {
        dialog = new JDialog(frame, "Counting...", true);
        dialog.setLocationRelativeTo(view);
        JPanel panel = new JPanel();
        dialog.setUndecorated(true);
        Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        dialog.setCursor(cursor);
        Icon icon = ImageRegistry.getIcon(ImageRegistry.LOADING_ICON);
        busyIconLbl.setIcon(icon);
        busyIconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(busyIconLbl);
        dialog.setContentPane(panel);
        dialog.pack();
    }

    private void locateOnScreen(Component component) {
        Dimension paneSize = component.getSize();
        Dimension screenSize = component.getToolkit().getScreenSize();
        component.setLocation((screenSize.width - paneSize.width) / 2, (screenSize.height - paneSize.height) / 2);
    }

    public void countSLOC(final FileType fileType, final String dirName_p) {
        model.getDataVector().clear();
        model.fireTableDataChanged();
        new Thread() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {
                FileCounter counter = new FileCounter(fileType, model);
                try {
                    counter.countSLOC(dirName_p);
                } catch (Exception exception_p) {
                    exception_p.printStackTrace();
                }
            }
        }.start();
    }

    @SuppressWarnings("nls")
    public void exportToExcel(JTable table, String fileName_p) throws Exception {
        File f = new File(fileName_p);
        if (!f.exists()) {
            f.createNewFile();
        }
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("SLOC-COUNTER");
        HSSFRichTextString str = null;
        HSSFRow row = sheet.createRow(0);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setFillBackgroundColor(new HSSFColor.BRIGHT_GREEN().getIndex());
        style.setWrapText(false);
        HSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontName("Verdana");
        HSSFCell cell = row.createCell((short) 0);
        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
        str = new HSSFRichTextString("File Name");
        cell.setCellValue(str);
        cell.setCellStyle(style);
        str.applyFont(font);
        cell = row.createCell((short) 1);
        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
        str = new HSSFRichTextString("SLOC");
        cell.setCellValue(str);
        cell.setCellStyle(style);
        str.applyFont(font);
        cell = row.createCell((short) 2);
        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
        str = new HSSFRichTextString("Total LOC");
        cell.setCellValue(str);
        cell.setCellStyle(style);
        str.applyFont(font);
        cell = row.createCell((short) 3);
        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
        str = new HSSFRichTextString("Comment Density (%)");
        cell.setCellValue(str);
        cell.setCellStyle(style);
        str.applyFont(font);
        int rows = table.getRowCount();
        int cols = table.getColumnCount();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                row = sheet.createRow(i + 1);
                cell = row.createCell((short) j);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                str = new HSSFRichTextString(table.getValueAt(i, j).toString());
                cell.setCellValue(str);
                if (i == rows - 1) {
                    str.applyFont(font);
                    cell.setCellStyle(style);
                }
            }
        }
        sheet.setColumnWidth((short) 0, (short) (20 * 256));
        sheet.setColumnWidth((short) 1, (short) (16 * 256));
        sheet.setColumnWidth((short) 2, (short) (18 * 256));
        sheet.setColumnWidth((short) 3, (short) (30 * 256));
        FileOutputStream fos = new FileOutputStream(f);
        workbook.write(fos);
    }

    public void showBusyDialog() {
        dialog.setVisible(true);
    }

    public void closeBusyDialog() {
        dialog.setVisible(false);
    }

    @SuppressWarnings("nls")
    public void openFile(String fileName) {
        File file = new File(fileName);
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view, "Cannot open file \"" + file.getName() + "\" !", "SLOC Counter", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            Plastic3DLookAndFeel lnf = new PlasticXPLookAndFeel();
            PlasticLookAndFeel.set3DEnabled(true);
            MetalLookAndFeel.setCurrentTheme(new ExperienceGreen());
            UIManager.setLookAndFeel(lnf);
        } catch (Throwable exception_p) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        new SLOCController().createAndShowSLOCCounter();
    }

    /**
   * @return the blankLinesIgnored
   */
    public boolean isBlankLinesIgnored() {
        return blankLinesIgnored;
    }

    /**
   * @param blankLinesIgnored_p the blankLinesIgnored to set
   */
    public void setBlankLinesIgnored(boolean blankLinesIgnored_p) {
        blankLinesIgnored = blankLinesIgnored_p;
    }
}
