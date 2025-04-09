package ist.ac.simulador.guis;

import java.awt.Color;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import ist.ac.simulador.application.Gui;
import ist.ac.simulador.application.GException;
import ist.ac.simulador.application.ISimulatorOrchestrator;
import ist.ac.simulador.nucleo.IBreakpoint;
import ist.ac.simulador.modules.IMemDefinition;
import ist.ac.simulador.modules.ICpuCisc;
import ist.ac.simulador.nucleo.SElement;
import ist.ac.simulador.nucleo.SModule;
import ist.ac.simulador.nucleo.SCompoundModule;
import ist.ac.simulador.nucleo.SInPort;
import ist.ac.simulador.assembler.IParser;

/**
 *
 * @author  cnr
 */
public class GuiMemCode extends javax.swing.JPanel {

    private Hashtable[] fColoredRows = null;

    private CpuTableModel memModel = null;

    private SymbolTableModel symbolModel = null;

    private IMemDefinition mem = null;

    private ICpuCisc cpu = null;

    private DefaultListModel fBreakpoints = null;

    private Step step = new Step();

    private SInPort clockSignal;

    private IParser parser;

    protected ISimulatorOrchestrator simulator = null;

    /** Creates new form GuiMem */
    public GuiMemCode(IMemDefinition mem, ICpuCisc cpu) {
        this.mem = mem;
        this.cpu = cpu;
        clockSignal = cpu.getClock();
        parser = cpu.getParser();
        initComponents();
    }

    public void reset() {
        javax.swing.table.TableColumn column = null;
        tpCode.setModel(memModel = new CpuTableModel(mem, parser.getInstruction()));
        column = tpCode.getColumnModel().getColumn(0);
        column.setPreferredWidth(40);
        column.setMinWidth(40);
        column.setMaxWidth(40);
        column = tpCode.getColumnModel().getColumn(1);
        column.setPreferredWidth(70);
        column.setResizable(true);
        tpCode.getColumnModel().getColumn(2).setPreferredWidth(70);
        fColoredRows = new Hashtable[2];
        fColoredRows[ColorRenderer.RED] = new Hashtable();
        fColoredRows[ColorRenderer.BLUE] = new Hashtable();
        tpCode.setDefaultRenderer(Object.class, new ColorRenderer(fColoredRows));
        symbolTable.setModel(symbolModel = new SymbolTableModel(mem));
        column = symbolTable.getColumnModel().getColumn(0);
        column.setPreferredWidth(40);
        column.setMinWidth(40);
        column.setMaxWidth(40);
        column = symbolTable.getColumnModel().getColumn(1);
        column.setPreferredWidth(100);
        column = symbolTable.getColumnModel().getColumn(2);
        column.setPreferredWidth(40);
        jTextField1.setText(Long.toHexString(mem.getBaseAddress()));
        if (memModel != null) {
            memModel.reset();
            tpCode.repaint();
        }
        if (symbolModel != null) {
            symbolModel.reset();
            symbolTable.repaint();
        }
    }

    public void setSimulator(ISimulatorOrchestrator simulator) {
        this.simulator = simulator;
        fBreakpoints = simulator.getBreakPointListModel();
    }

    public void wake() {
        if (memModel == null || cpu == null || tpCode == null) return;
        fColoredRows[ColorRenderer.BLUE].clear();
        long nrow = memModel.getRowForAddress((long) cpu.getIP());
        if (nrow != -1) {
            Integer irow = new Integer((int) nrow);
            fColoredRows[ColorRenderer.BLUE].put(irow, irow);
            int x = tpCode.getRowHeight() * irow.intValue();
            java.awt.Rectangle rect = tpCode.getCellRect(irow.intValue(), 0, true);
            tpCode.scrollRectToVisible(rect);
        }
        tpCode.repaint();
        symbolTable.repaint();
        jTextField1.setText(Long.toHexString(mem.getBaseAddress()));
    }

    private class FilterCfg extends javax.swing.filechooser.FileFilter {

        private String extension;

        private String description;

        public FilterCfg(String ext, String desc) {
            extension = ext;
            description = desc;
        }

        public boolean accept(File file) {
            if (file.isDirectory()) return true;
            if (file.getName().endsWith(extension)) return true;
            return false;
        }

        public String getDescription() {
            return description;
        }
    }

    private void resetCpu() {
        int index = fBreakpoints.lastIndexOf(step);
        if (index != -1) fBreakpoints.remove(index);
        cpu.reset();
        wake();
    }

    private void saveInit(File f) {
        if (f == null) return;
        ((SModule) mem).setConfigFileName(f.getPath());
        ((JFrame) this.getRootPane().getParent()).setTitle((getName() == null ? "" : getName()) + "::" + f.getPath());
    }

    private File loadInit() {
        String sLastOpenFile = ((SModule) mem).getConfigFileName();
        File lastOpenFile = null;
        if (sLastOpenFile != null) {
            lastOpenFile = new File(sLastOpenFile);
            if (!lastOpenFile.isFile()) {
                return null;
            }
            ((JFrame) this.getRootPane().getParent()).setTitle((getName() == null ? "" : getName()) + "::" + lastOpenFile.getPath());
        }
        return lastOpenFile;
    }

    private void compileAndLoad(File f) {
        try {
            ByteArrayOutputStream code = new ByteArrayOutputStream();
            FileInputStream in = new FileInputStream(f);
            String result = parser.parsingFile(f.toString(), in, code);
            in.close();
            if (result != null) {
                JOptionPane.showMessageDialog(null, result, "Info", JOptionPane.ERROR_MESSAGE);
                return;
            }
            load(new ByteArrayInputStream(code.toByteArray()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    private void compile(File f) {
        try {
            String name = f.toString();
            FileInputStream in = new FileInputStream(f);
            String result = parser.parsingFile(name, in);
            in.close();
            if (result != null) JOptionPane.showMessageDialog(null, result, "Info", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    private void loadFile(File f) {
        FileInputStream in;
        try {
            load(in = new FileInputStream(f));
            in.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    public void reload() {
        File lastOpenFile = loadInit();
        reload(lastOpenFile);
    }

    public void reload(File lastOpenFile) {
        if (lastOpenFile.getPath().endsWith(".cod")) loadFile(lastOpenFile); else compileAndLoad(lastOpenFile);
        resetCpu();
    }

    class ColorRenderer extends JLabel implements TableCellRenderer {

        public static final int RED = 0;

        public static final int BLUE = 1;

        private Hashtable[] coloredRows = null;

        public ColorRenderer(Hashtable[] coloredRows) {
            this.coloredRows = coloredRows;
            setOpaque(true);
        }

        public java.awt.Component getTableCellRendererComponent(JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(obj.toString());
            Integer irow = new Integer(row);
            if (coloredRows[BLUE].containsKey(irow)) {
                setBackground(Color.blue);
            } else if (coloredRows[RED].containsKey(irow)) {
                setBackground(Color.red);
            } else {
                setBackground(Color.white);
            }
            this.setHorizontalAlignment(column == 1 ? this.CENTER : this.LEADING);
            return this;
        }
    }

    class BreakPointAt implements IBreakpoint {

        long address = 0;

        boolean breaked = false;

        public BreakPointAt(long address) {
            this.address = address;
        }

        public boolean isActive() {
            if (clockSignal.isChanged() && cpu.isFetching() && address == cpu.getIP()) if (!breaked) return breaked = true; else return false; else return breaked = false;
        }

        public String toString() {
            return "Break at IP = " + Long.toHexString(address);
        }
    }

    class Step implements IBreakpoint {

        boolean breaked = false;

        public boolean isActive() {
            if (clockSignal.isChanged() && cpu.isFetching()) if (!breaked) return breaked = true; else return false; else return breaked = false;
        }

        public String toString() {
            return "Break at next instruction";
        }

        public boolean equals(Object breakp) {
            return Step.class.isAssignableFrom(breakp.getClass());
        }
    }

    private long convert(byte[] buf) {
        long value = 0;
        for (int i = 0; i < buf.length; i++) value += (((long) buf[i]) & 0xFF) << (i * 8);
        return value;
    }

    private void readSymbol(InputStream f, Hashtable sbTable, long size, int wordsize) throws IOException {
        byte[] buf = new byte[wordsize];
        char[] stringBuf = new char[1024];
        Long address;
        int stringSize;
        for (int i = 0; i < size; i++) {
            f.read(buf);
            address = new Long(convert(buf));
            f.read(buf);
            stringSize = (int) convert(buf);
            if (stringSize > 1024) {
                JOptionPane.showMessageDialog(null, "Symbol too big", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (int j = 0; j < stringSize; j++) {
                f.read(buf);
                stringBuf[j] = (char) convert(buf);
            }
            sbTable.put(address, new String(stringBuf, 0, stringSize));
        }
    }

    private void load(InputStream f) throws IOException {
        int wordsize = 0;
        Hashtable symbolTable = new Hashtable();
        Hashtable usageTable = new Hashtable();
        if (f.available() == 0) return;
        wordsize = f.read();
        if ((mem.getDataBits() / 8) != wordsize) {
            JOptionPane.showMessageDialog(null, "Word size mismatch", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        byte[] addressBuf = new byte[wordsize];
        byte[] countBuf = new byte[wordsize];
        byte[] codeStartBuf = new byte[wordsize];
        byte[] symbolSizeBuf = new byte[wordsize];
        byte[] buf = new byte[wordsize];
        long count;
        long oaddress, address;
        long codeStart;
        long symbolSize;
        memModel.reset();
        symbolModel.reset();
        while (f.available() >= 3 * wordsize) {
            f.read(addressBuf);
            f.read(countBuf);
            f.read(codeStartBuf);
            f.read(symbolSizeBuf);
            oaddress = address = convert(addressBuf);
            count = convert(countBuf);
            codeStart = convert(codeStartBuf);
            symbolSize = convert(symbolSizeBuf);
            if (symbolSize > 0) readSymbol(f, symbolTable, symbolSize, wordsize);
            while (f.available() >= wordsize && count > 0) {
                f.read(buf);
                mem.setValueAt(address++, convert(buf));
                count--;
            }
            memModel.update(codeStart, address + count - 1, symbolTable, null, usageTable);
            symbolModel.update(oaddress, codeStart - 1, symbolTable);
        }
        wake();
    }

    private void initComponents() {
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        tpCode = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        symbolTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        bStart = new javax.swing.JButton();
        bStop = new javax.swing.JButton();
        bReset = new javax.swing.JButton();
        bStep = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        bCompile = new javax.swing.JButton();
        bLoad = new javax.swing.JButton();
        bCompLoad = new javax.swing.JButton();
        bReload = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jLabel111 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        setLayout(new java.awt.BorderLayout());
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setLastDividerLocation(2);
        jSplitPane1.setOneTouchExpandable(true);
        jSplitPane1.setPreferredSize(new java.awt.Dimension(305, 500));
        tpCode.setPreferredScrollableViewportSize(new java.awt.Dimension(300, 500));
        tpCode.setShowHorizontalLines(false);
        tpCode.setShowVerticalLines(false);
        tpCode.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tpCodeMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tpCode);
        jSplitPane1.setRightComponent(jScrollPane2);
        jScrollPane3.setMinimumSize(new java.awt.Dimension(20, 20));
        symbolTable.setMaximumSize(new java.awt.Dimension(300, 800));
        symbolTable.setPreferredScrollableViewportSize(new java.awt.Dimension(300, 500));
        symbolTable.setPreferredSize(new java.awt.Dimension(300, 500));
        symbolTable.setShowHorizontalLines(false);
        symbolTable.setShowVerticalLines(false);
        jScrollPane3.setViewportView(symbolTable);
        jSplitPane1.setTopComponent(jScrollPane3);
        add(jSplitPane1, java.awt.BorderLayout.CENTER);
        jPanel1.setLayout(new java.awt.GridLayout(12, 1));
        bStart.setText("Start");
        bStart.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bStartActionPerformed(evt);
            }
        });
        jPanel1.add(bStart);
        bStop.setText("Stop");
        bStop.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bStopActionPerformed(evt);
            }
        });
        jPanel1.add(bStop);
        bReset.setText("Reset");
        bReset.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bResetActionPerformed(evt);
            }
        });
        jPanel1.add(bReset);
        bStep.setText("Step");
        bStep.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bStepActionPerformed(evt);
            }
        });
        jPanel1.add(bStep);
        jPanel1.add(jLabel1);
        bCompile.setText("Compile");
        bCompile.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCompileActionPerformed(evt);
            }
        });
        jPanel1.add(bCompile);
        bLoad.setText("Load");
        bLoad.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bLoadActionPerformed(evt);
            }
        });
        jPanel1.add(bLoad);
        bCompLoad.setText("Compile & Load");
        bCompLoad.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCompLoadActionPerformed(evt);
            }
        });
        jPanel1.add(bCompLoad);
        bReload.setText("Reload");
        bReload.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bReloadActionPerformed(evt);
            }
        });
        jPanel1.add(bReload);
        jPanel1.add(jLabel11);
        jLabel111.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel111.setText("Base Address");
        jPanel1.add(jLabel111);
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField1.setText("0000");
        jTextField1.setMaximumSize(new java.awt.Dimension(32, 20));
        jTextField1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jPanel1.add(jTextField1);
        add(jPanel1, java.awt.BorderLayout.EAST);
    }

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            mem.setBaseAddress(Long.parseLong(jTextField1.getText(), 16));
        } catch (Exception e) {
            jTextField1.setText(Long.toHexString(mem.getBaseAddress()));
        }
    }

    private void bReloadActionPerformed(java.awt.event.ActionEvent evt) {
        bResetActionPerformed(evt);
        reload();
    }

    private void bStepActionPerformed(java.awt.event.ActionEvent evt) {
        if (!fBreakpoints.contains(step)) fBreakpoints.addElement(step);
        simulator.run();
    }

    private void bResetActionPerformed(java.awt.event.ActionEvent evt) {
        resetCpu();
    }

    private void bStopActionPerformed(java.awt.event.ActionEvent evt) {
        simulator.stop();
    }

    private void bStartActionPerformed(java.awt.event.ActionEvent evt) {
        int index = fBreakpoints.lastIndexOf(step);
        if (index != -1) fBreakpoints.remove(index);
        simulator.run();
    }

    private void bCompLoadActionPerformed(java.awt.event.ActionEvent evt) {
        File lastOpenFile = loadInit();
        JFileChooser chooser = new JFileChooser(lastOpenFile);
        chooser.setFileFilter(new FilterCfg(parser.getExt(), parser.getExtExplain()));
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
        compileAndLoad(chooser.getSelectedFile());
        saveInit(chooser.getSelectedFile());
    }

    private void bCompileActionPerformed(java.awt.event.ActionEvent evt) {
        File lastOpenFile = loadInit();
        JFileChooser chooser = new JFileChooser(lastOpenFile);
        chooser.setFileFilter(new FilterCfg(parser.getExt(), parser.getExtExplain()));
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
        compile(chooser.getSelectedFile());
        saveInit(chooser.getSelectedFile());
    }

    private void bLoadActionPerformed(java.awt.event.ActionEvent evt) {
        File lastOpenFile = loadInit();
        JFileChooser chooser = new JFileChooser(lastOpenFile);
        chooser.setFileFilter(new FilterCfg(".cod", "Code/Data Files"));
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
        loadFile(chooser.getSelectedFile());
        saveInit(chooser.getSelectedFile());
    }

    private void tpCodeMouseClicked(java.awt.event.MouseEvent evt) {
        if (fBreakpoints == null) return;
        int row = tpCode.getSelectedRow();
        Integer irow = new Integer(row);
        if (fColoredRows[ColorRenderer.RED].containsKey(irow)) {
            int index = fBreakpoints.lastIndexOf(fColoredRows[ColorRenderer.RED].get(irow));
            if (index != -1) fBreakpoints.remove(index);
            fColoredRows[ColorRenderer.RED].remove(irow);
        } else {
            long address = Long.parseLong(tpCode.getValueAt(row, 0).toString(), 16);
            BreakPointAt bp = new BreakPointAt(address);
            fBreakpoints.addElement(bp);
            fColoredRows[ColorRenderer.RED].put(irow, bp);
        }
        tpCode.repaint();
    }

    private javax.swing.JButton bCompLoad;

    private javax.swing.JButton bCompile;

    private javax.swing.JButton bLoad;

    private javax.swing.JButton bReload;

    private javax.swing.JButton bReset;

    private javax.swing.JButton bStart;

    private javax.swing.JButton bStep;

    private javax.swing.JButton bStop;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel11;

    private javax.swing.JLabel jLabel111;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JScrollPane jScrollPane3;

    private javax.swing.JSplitPane jSplitPane1;

    private javax.swing.JTextField jTextField1;

    private javax.swing.JTable symbolTable;

    private javax.swing.JTable tpCode;
}
