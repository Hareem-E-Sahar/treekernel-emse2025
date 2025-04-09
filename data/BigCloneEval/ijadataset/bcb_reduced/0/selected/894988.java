package ist.ac.simulador.guis;

import ist.ac.simulador.application.Gui;
import ist.ac.simulador.application.GException;
import ist.ac.simulador.modules.ModuleMicroPepe;
import ist.ac.simulador.modules.ModulePepe;
import ist.ac.simulador.assembler.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
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
import ist.ac.simulador.modules.IPepe;
import ist.ac.simulador.nucleo.SElement;
import ist.ac.simulador.nucleo.SModule;
import ist.ac.simulador.nucleo.Simulator;
import ist.ac.simulador.nucleo.SCompoundModule;
import ist.ac.simulador.nucleo.SInPort;
import ist.ac.simulador.assembler.IParser;
import java.awt.Dimension;

/**
 *
 * @author  cnr
 */
public class GuiPepe extends Gui {

    protected Hashtable[] fColoredRows = null;

    protected CpuCodeModel memModel = null;

    protected SymbolListModel symbolModel = null;

    protected IMemDefinition mem = null;

    protected DefaultListModel fBreakpoints = null;

    protected Step step = new Step();

    protected SInPort clockSignal;

    protected IParser parser;

    protected Simulator simulador;

    protected boolean isLoading = false;

    protected JToolBar toolBar;

    /** Creates new form GuiOutput */
    public GuiPepe() {
        super(ModulePepe.class);
        initComponents();
        toolBar = setToolBar();
    }

    public GuiPepe(Class element) {
        super(element);
        initComponents();
        toolBar = setToolBar();
    }

    public void setBaseElement(SElement e) throws GException {
        super.setBaseElement(e);
        clockSignal = ((IPepe) element).getClock();
        parser = ((IPepe) element).getParser();
    }

    public void reset() {
        if (simulator != null) simulator.stop();
        super.reset();
        resetUserInterface();
        wake();
    }

    protected void resetUserInterface() {
        javax.swing.table.TableColumn column = null;
        tpCode.setModel(memModel = new CpuCodeModel(((IPepe) element).getAddressBits(), ((IPepe) element).getDataBits(), parser.getInstruction()));
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
        symbolTable.setModel(symbolModel = new SymbolListModel(((IPepe) element).getAddressBits(), ((IPepe) element).getDataBits()));
        column = symbolTable.getColumnModel().getColumn(0);
        column.setPreferredWidth(40);
        column.setMinWidth(40);
        column.setMaxWidth(40);
        column = symbolTable.getColumnModel().getColumn(1);
        column.setPreferredWidth(100);
        if (memModel != null) {
            memModel.reset();
            tpCode.repaint();
        }
        if (symbolModel != null) {
            symbolModel.reset();
            symbolTable.repaint();
        }
        isLoading = false;
    }

    public void wake() {
        wakeUserInterface();
    }

    public void wakeUserInterface() {
        PC.setText(rightJustify(Integer.toHexString(((IPepe) element).getIP()), 4));
        R0.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(0)), 4));
        R1.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(1)), 4));
        R2.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(2)), 4));
        R3.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(3)), 4));
        R4.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(4)), 4));
        R5.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(5)), 4));
        R6.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(6)), 4));
        R7.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(7)), 4));
        R8.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(8)), 4));
        R9.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(9)), 4));
        R10.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(10)), 4));
        SP.setText(rightJustify(Integer.toHexString(((IPepe) element).getUSP()), 4));
        SSP.setText(rightJustify(Integer.toHexString(((IPepe) element).getSSP()), 4));
        RE.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(13)), 4));
        RL.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(11)), 4));
        R14.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(14)), 4));
        R15.setText(rightJustify(Integer.toHexString(((IPepe) element).getREG(15)), 4));
        rcn.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(0)), 4));
        rccd.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(1)), 4));
        rcci.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(2)), 4));
        rcmv.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(3)), 4));
        rtp.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(4)), 4));
        rpid.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(5)), 4));
        a6.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(6)), 4));
        a7.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(7)), 4));
        a8.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(8)), 4));
        a9.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(9)), 4));
        a10.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(10)), 4));
        a11.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(11)), 4));
        a12.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(12)), 4));
        a13.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(13)), 4));
        a14.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(14)), 4));
        a15.setText(rightJustify(Integer.toHexString(((IPepe) element).getAuxReg(15)), 4));
        int flags = ((IPepe) element).getFlags();
        Z.setSelected((flags & ModulePepe.fZ) != 0);
        N.setSelected((flags & ModulePepe.fN) != 0);
        C.setSelected((flags & ModulePepe.fC) != 0);
        V.setSelected((flags & ModulePepe.fV) != 0);
        A.setSelected((flags & ModulePepe.fA) != 0);
        B.setSelected((flags & ModulePepe.fB) != 0);
        TV.setSelected((flags & ModulePepe.fTV) != 0);
        TD.setSelected((flags & ModulePepe.fTD) != 0);
        IE.setSelected((flags & ModulePepe.fIE) != 0);
        IE0.setSelected((flags & ModulePepe.fIE0) != 0);
        IE1.setSelected((flags & ModulePepe.fIE1) != 0);
        IE2.setSelected((flags & ModulePepe.fIE2) != 0);
        IE3.setSelected((flags & ModulePepe.fIE3) != 0);
        DE.setSelected((flags & ModulePepe.fDE) != 0);
        NP.setSelected((flags & ModulePepe.fNP) != 0);
        R.setSelected((flags & ModulePepe.fR) != 0);
        if (memModel == null || element == null || tpCode == null) return;
        fColoredRows[ColorRenderer.BLUE].clear();
        int nrow = memModel.getRowForAddress(((IPepe) element).getPointer());
        if (nrow != -1) {
            Integer irow = new Integer((int) nrow);
            fColoredRows[ColorRenderer.BLUE].put(irow, irow);
            int x = tpCode.getRowHeight() * irow.intValue();
            java.awt.Rectangle rect = tpCode.getCellRect(irow.intValue(), 0, true);
            tpCode.scrollRectToVisible(rect);
        }
        tpCode.repaint();
        symbolTable.repaint();
    }

    public void setParent(Object cmp) throws GException {
        super.setParent(cmp);
        simulator.wakeThis(this);
        fBreakpoints = simulator.getBreakPointListModel();
    }

    public class ToolBarButton extends JButton {

        private final Insets margins = new Insets(0, 0, 0, 0);

        public ToolBarButton(String name, String icon, String tip) {
            ImageIcon acIcon = null;
            URL iconURL = ClassLoader.getSystemResource("images/" + icon);
            if (iconURL != null) {
                acIcon = new ImageIcon(iconURL);
                setIcon(acIcon);
            } else {
                setText(name);
            }
            setName(name);
            setToolTipText(tip);
            setMargin(margins);
            setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
            setVerticalTextPosition(BOTTOM);
            setHorizontalTextPosition(CENTER);
        }
    }

    public class ToggleButton extends javax.swing.JToggleButton {

        private final Insets margins = new Insets(0, 0, 0, 0);

        public ToggleButton(String name, String icon, String pressedIcon, String tip) {
            ImageIcon acIcon = null;
            URL iconURL = ClassLoader.getSystemResource("images/" + icon);
            if (iconURL != null) {
                acIcon = new ImageIcon(iconURL);
                setIcon(acIcon);
            } else {
                setText(name);
            }
            iconURL = ClassLoader.getSystemResource("images/" + pressedIcon);
            if (iconURL != null) {
                acIcon = new ImageIcon(iconURL);
                this.setSelectedIcon(acIcon);
            }
            setName(name);
            setToolTipText(tip);
            setMargin(margins);
            setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
            setVerticalTextPosition(BOTTOM);
            setHorizontalTextPosition(CENTER);
        }
    }

    protected JToolBar setToolBar() {
        String[] buttonIcons = { "forward24.png", "pause24.png", "stop24.png", "play24.png", "up24.png", "folder24.png", "foldermove24.png", "refresh24.png" };
        String[] buttonNames = { "start", "stop", "reset", "step", "compile", "load", "compileandload", "reload" };
        String[] buttonTips = { "Run simulation", "Stop simulation", "Reset CPU", "Step instruction", "Compile Program", "Load Compiled Program", "Compile and Load Program", "Reload or recompile and load previous program" };
        ToolBarButton[] toolButtons = new ToolBarButton[buttonNames.length];
        final ToolBarButton clockButton = new ToolBarButton("OneClock", "ClockClick.png", "Press to execute a single clock cycle");
        JToolBar toolbar = new JToolBar();
        for (int i = 0; i < buttonNames.length; i++) {
            toolButtons[i] = new ToolBarButton(buttonNames[i], buttonIcons[i], buttonTips[i]);
            toolbar.add(toolButtons[i]);
        }
        JPanel toolPanel = new JPanel();
        toolPanel.add(toolbar, BorderLayout.NORTH);
        this.getContentPane().add(toolbar, BorderLayout.NORTH);
        toolButtons[0].addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bStartActionPerformed(evt);
            }
        });
        toolButtons[1].addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bStopActionPerformed(evt);
            }
        });
        toolButtons[2].addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bResetActionPerformed(evt);
            }
        });
        toolButtons[3].addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bStepActionPerformed(evt);
            }
        });
        toolButtons[4].addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCompileActionPerformed(evt);
            }
        });
        toolButtons[5].addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bLoadActionPerformed(evt);
            }
        });
        toolButtons[6].addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCompLoadActionPerformed(evt);
            }
        });
        toolButtons[7].addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bReloadActionPerformed(evt);
            }
        });
        toolbar.addSeparator();
        ToggleButton clockSwitch = new ToggleButton("ClockSwitch", "ClockOff.png", "ClockOn.png", "Enable/Disable the clock");
        ActionListener actionListener = new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
                boolean selected = abstractButton.getModel().isSelected();
                ((IPepe) element).setClockState(!selected);
                clockButton.setEnabled(selected);
            }
        };
        clockSwitch.addActionListener(actionListener);
        toolbar.add(clockSwitch);
        clockButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simulator.run();
                ((IPepe) element).setStartClock();
            }
        });
        clockButton.setEnabled(false);
        toolbar.add(clockButton);
        return toolbar;
    }

    protected class FilterCfg extends javax.swing.filechooser.FileFilter {

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
        ((IPepe) element).reset();
        wake();
    }

    private void saveInit(File f) {
        if (f == null) return;
        ((SModule) element).setConfigFileName(f.getPath());
        ((JFrame) this.getRootPane().getParent()).setTitle((getName() == null ? "" : getName()) + "::" + f.getPath());
    }

    private File loadInit() {
        String sLastOpenFile = ((SModule) element).getConfigFileName();
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
        if (lastOpenFile != null) reload(lastOpenFile);
    }

    public void reload(File lastOpenFile) {
        if (lastOpenFile.getPath().endsWith(".cod")) loadFile(lastOpenFile); else compileAndLoad(lastOpenFile);
    }

    class BreakPointAt implements IBreakpoint {

        int address = 0;

        long lastBreakpointTime = 0;

        public BreakPointAt(int address) {
            this.address = address;
            simulador = ((SModule) element).getSimulator();
        }

        public boolean isActive() {
            if (isLoading) return false;
            if (lastBreakpointTime == simulador.getTime()) return false;
            boolean result = ((IPepe) element).isOn(address);
            if (result) lastBreakpointTime = simulador.getTime();
            return result;
        }

        public String toString() {
            return "Break at IP = " + Long.toHexString(address);
        }
    }

    class Step implements IBreakpoint {

        int old_address = -1;

        long lastBreakpointTime = 0;

        public boolean isActive() {
            if (simulador == null) simulador = ((SModule) element).getSimulator();
            if (isLoading) return false;
            if (lastBreakpointTime == simulador.getTime()) return false;
            boolean result = ((IPepe) element).isOn(-1);
            if (result) lastBreakpointTime = simulador.getTime();
            return result;
        }

        public String toString() {
            return "Break at next instruction of " + ((SModule) element).getName();
        }

        public boolean equals(Object breakp) {
            return Step.class.isAssignableFrom(breakp.getClass()) && ((Step) breakp).getElement().equals(element);
        }

        public Object getElement() {
            return element;
        }
    }

    class stopWhenLoaded implements IBreakpoint {

        public boolean isActive() {
            if (!((IPepe) element).isWritingCode()) {
                fBreakpoints.removeElement(this);
                isLoading = false;
                return true;
            }
            return false;
        }

        public String toString() {
            return "Stop when code loaded";
        }
    }

    private int convert(byte[] buffer) {
        int value = 0;
        for (int i = 0; i < buffer.length; i++) value += (((int) buffer[i]) & 0xFF) << ((buffer.length - i - 1) * 8);
        return value;
    }

    private int compact(byte[] buffer) {
        int value = 0;
        for (int i = 0; i < buffer.length; i++) value += (((int) buffer[i]) & 0xFF) << (i * 8);
        return value;
    }

    private void readSymbol(InputStream f, Hashtable sbTable, Hashtable usageTable, int size, int wordsize) throws IOException {
        byte[] buf = new byte[wordsize];
        byte[] stringBuf = new byte[1024];
        Integer address;
        int stringSize;
        for (int i = 0; i < size; i++) {
            f.read(buf);
            address = new Integer(convert(buf));
            f.read(buf);
            stringSize = (int) convert(buf);
            if (stringSize > 1024) {
                JOptionPane.showMessageDialog(null, "Symbol too big", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            f.read(stringBuf, 0, stringSize);
            if ((stringSize & 1) != 0) {
                f.skip(1);
            }
            String label = new String(stringBuf, 0, stringSize);
            sbTable.put(address, label);
            f.read(buf);
            int nusages = convert(buf);
            for (int j = 0; j < nusages; j++) {
                f.read(buf);
                address = new Integer(convert(buf));
                usageTable.put(address, label);
            }
        }
    }

    private void readNewInstr(InputStream f, Hashtable sbTable, int size, int wordsize) throws IOException {
        byte[] buf = new byte[wordsize];
        byte[] stringBuf = new byte[1024];
        int indexsize;
        InstrType instr;
        int stringSize;
        for (int i = 0; i < size; i++) {
            f.read(buf);
            indexsize = (int) convert(buf);
            instr = new InstrType();
            for (int j = 0; j < indexsize; j++) {
                int aux;
                f.read(buf);
                aux = (int) convert(buf);
                instr.push(aux);
            }
            f.read(buf);
            stringSize = (int) convert(buf);
            if (stringSize > 1024) {
                JOptionPane.showMessageDialog(null, "Symbol too big", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            f.read(stringBuf, 0, stringSize);
            instr.name = new String(stringBuf, 0, stringSize);
            if ((stringSize & 1) != 0) f.skip(1);
            sbTable.put(new Integer(instr.pop() | instr.pop() << 4), instr);
        }
    }

    private void load(InputStream f) throws IOException {
        int wordsize = 0;
        Hashtable symbolTable = new Hashtable();
        Hashtable newInstrTable = new Hashtable();
        Hashtable usageTable = new Hashtable();
        int[] mem;
        if (f.available() == 0) return;
        wordsize = f.read();
        f.skip(wordsize - 1);
        if ((((IPepe) element).getDataBits() / 8) != wordsize) {
            JOptionPane.showMessageDialog(null, "Word size mismatch", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        byte[] addressBuf = new byte[wordsize];
        byte[] countBuf = new byte[wordsize];
        byte[] codeStartBuf = new byte[wordsize];
        byte[] symbolSizeBuf = new byte[wordsize];
        byte[] buf = new byte[wordsize];
        int count;
        int address;
        int codeStart;
        int symbolSize;
        memModel.reset();
        symbolModel.reset();
        if (f.read(symbolSizeBuf) < wordsize) return;
        symbolSize = convert(symbolSizeBuf);
        if (symbolSize > 0) readSymbol(f, symbolTable, usageTable, symbolSize, wordsize);
        symbolModel.updateNew(symbolTable);
        this.symbolTable.setPreferredSize(new Dimension(300, this.symbolTable.getRowHeight() * this.symbolTable.getRowCount()));
        this.symbolTable.revalidate();
        if (f.read(symbolSizeBuf) < wordsize) return;
        symbolSize = convert(symbolSizeBuf);
        if (symbolSize > 0) readNewInstr(f, newInstrTable, symbolSize, wordsize);
        while (f.available() >= 3 * wordsize) {
            f.read(addressBuf);
            f.read(countBuf);
            f.read(codeStartBuf);
            address = convert(addressBuf);
            count = convert(countBuf) / wordsize;
            codeStart = convert(codeStartBuf);
            mem = new int[count];
            for (int i = 0; f.available() >= wordsize && i < count; i++) {
                f.read(buf);
                mem[i] = convert(buf);
            }
            memModel.update(address, codeStart, address + 2 * (count - 1), mem, symbolTable, newInstrTable, usageTable);
            ((IPepe) element).writeThisCode(mem, address);
        }
        fBreakpoints.addElement(new stopWhenLoaded());
        isLoading = true;
        simulator.run();
        wake();
    }

    private String rightJustify(String str, int width) {
        for (int i = width - str.length(); i > 0; i--) str = "0" + str;
        return str.toUpperCase();
    }

    protected void addPane(String name, java.awt.Component cpt) {
        jTabbedPane1.addTab(name, cpt);
    }

    protected void removePane(String name) {
        int i = jTabbedPane1.indexOfTab(name);
        if (i < 0) return;
        jTabbedPane1.removeTabAt(i);
    }

    public void setTabbedSize(int x, int y) {
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(x, y));
    }

    private void initComponents() {
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tpCode = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        IPC = new javax.swing.JLabel();
        PC = new javax.swing.JTextField();
        lR0 = new javax.swing.JLabel();
        R0 = new javax.swing.JTextField();
        lR1 = new javax.swing.JLabel();
        R1 = new javax.swing.JTextField();
        lR2 = new javax.swing.JLabel();
        R2 = new javax.swing.JTextField();
        lR3 = new javax.swing.JLabel();
        R3 = new javax.swing.JTextField();
        lR4 = new javax.swing.JLabel();
        R4 = new javax.swing.JTextField();
        lR5 = new javax.swing.JLabel();
        R5 = new javax.swing.JTextField();
        lR6 = new javax.swing.JLabel();
        R6 = new javax.swing.JTextField();
        lR7 = new javax.swing.JLabel();
        R7 = new javax.swing.JTextField();
        lR8 = new javax.swing.JLabel();
        R8 = new javax.swing.JTextField();
        lR9 = new javax.swing.JLabel();
        R9 = new javax.swing.JTextField();
        lR10 = new javax.swing.JLabel();
        R10 = new javax.swing.JTextField();
        lRL = new javax.swing.JLabel();
        RL = new javax.swing.JTextField();
        lSP = new javax.swing.JLabel();
        SP = new javax.swing.JTextField();
        lSSP = new javax.swing.JLabel();
        SSP = new javax.swing.JTextField();
        lRE = new javax.swing.JLabel();
        RE = new javax.swing.JTextField();
        lR14 = new javax.swing.JLabel();
        R14 = new javax.swing.JTextField();
        lR15 = new javax.swing.JLabel();
        R15 = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        TD = new javax.swing.JRadioButton();
        TV = new javax.swing.JRadioButton();
        B = new javax.swing.JRadioButton();
        A = new javax.swing.JRadioButton();
        V = new javax.swing.JRadioButton();
        C = new javax.swing.JRadioButton();
        N = new javax.swing.JRadioButton();
        Z = new javax.swing.JRadioButton();
        R = new javax.swing.JRadioButton();
        NP = new javax.swing.JRadioButton();
        DE = new javax.swing.JRadioButton();
        IE3 = new javax.swing.JRadioButton();
        IE2 = new javax.swing.JRadioButton();
        IE1 = new javax.swing.JRadioButton();
        IE0 = new javax.swing.JRadioButton();
        IE = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        lrcn = new javax.swing.JLabel();
        rcn = new javax.swing.JTextField();
        lrccd = new javax.swing.JLabel();
        rccd = new javax.swing.JTextField();
        lrcci = new javax.swing.JLabel();
        rcci = new javax.swing.JTextField();
        lrcmv = new javax.swing.JLabel();
        rcmv = new javax.swing.JTextField();
        lrtp = new javax.swing.JLabel();
        rtp = new javax.swing.JTextField();
        lrpid = new javax.swing.JLabel();
        rpid = new javax.swing.JTextField();
        la6 = new javax.swing.JLabel();
        a6 = new javax.swing.JTextField();
        la7 = new javax.swing.JLabel();
        a7 = new javax.swing.JTextField();
        la8 = new javax.swing.JLabel();
        a8 = new javax.swing.JTextField();
        la9 = new javax.swing.JLabel();
        a9 = new javax.swing.JTextField();
        la10 = new javax.swing.JLabel();
        a10 = new javax.swing.JTextField();
        la11 = new javax.swing.JLabel();
        a11 = new javax.swing.JTextField();
        la12 = new javax.swing.JLabel();
        a12 = new javax.swing.JTextField();
        la13 = new javax.swing.JLabel();
        a13 = new javax.swing.JTextField();
        la14 = new javax.swing.JLabel();
        a14 = new javax.swing.JTextField();
        la15 = new javax.swing.JLabel();
        a15 = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        symbolTable = new javax.swing.JTable();
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(0, 0));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(560, 628));
        jPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel2ComponentShown(evt);
            }
        });
        jPanel2.setLayout(new java.awt.BorderLayout());
        jScrollPane2.setMinimumSize(new java.awt.Dimension(303, 403));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(303, 503));
        tpCode.setShowHorizontalLines(false);
        tpCode.setShowVerticalLines(false);
        tpCode.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tpCodeMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tpCode);
        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);
        jPanel5.setMinimumSize(new java.awt.Dimension(250, 334));
        jPanel5.setPreferredSize(new java.awt.Dimension(250, 334));
        jPanel5.setLayout(new java.awt.BorderLayout());
        jPanel4.setLayout(new java.awt.BorderLayout());
        jPanel1.setLayout(new java.awt.GridLayout(9, 1));
        IPC.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        IPC.setText("PC");
        IPC.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        IPC.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(IPC);
        PC.setText("0000");
        PC.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PCActionPerformed(evt);
            }
        });
        jPanel1.add(PC);
        lR0.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lR0.setText("R0");
        lR0.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lR0.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lR0);
        R0.setText("0000");
        R0.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R0ActionPerformed(evt);
            }
        });
        jPanel1.add(R0);
        lR1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lR1.setText("R1");
        lR1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lR1.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lR1);
        R1.setText("0000");
        R1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R1ActionPerformed(evt);
            }
        });
        jPanel1.add(R1);
        lR2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lR2.setText("R2");
        lR2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lR2.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lR2);
        R2.setText("0000");
        R2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R2ActionPerformed(evt);
            }
        });
        jPanel1.add(R2);
        lR3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lR3.setText("R3");
        lR3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lR3.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lR3);
        R3.setText("0000");
        R3.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R3ActionPerformed(evt);
            }
        });
        jPanel1.add(R3);
        lR4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lR4.setText("R4");
        lR4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lR4.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lR4);
        R4.setText("0000");
        R4.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R4ActionPerformed(evt);
            }
        });
        jPanel1.add(R4);
        lR5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lR5.setText("R5");
        lR5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lR5.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lR5);
        R5.setText("0000");
        R5.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R5ActionPerformed(evt);
            }
        });
        jPanel1.add(R5);
        lR6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lR6.setText("R6");
        lR6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lR6.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lR6);
        R6.setText("0000");
        R6.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R6ActionPerformed(evt);
            }
        });
        jPanel1.add(R6);
        lR7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lR7.setText("R7");
        lR7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lR7.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lR7);
        R7.setText("0000");
        R7.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R7ActionPerformed(evt);
            }
        });
        jPanel1.add(R7);
        lR8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lR8.setText("R8");
        lR8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lR8.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lR8);
        R8.setText("0000");
        R8.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R8ActionPerformed(evt);
            }
        });
        jPanel1.add(R8);
        lR9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lR9.setText("R9");
        lR9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lR9.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lR9);
        R9.setText("0000");
        R9.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R9ActionPerformed(evt);
            }
        });
        jPanel1.add(R9);
        lR10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lR10.setText("R10");
        lR10.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lR10.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lR10);
        R10.setText("0000");
        R10.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R10ActionPerformed(evt);
            }
        });
        jPanel1.add(R10);
        lRL.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lRL.setText("RL");
        lRL.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lRL.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lRL);
        RL.setText("0000");
        RL.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RLActionPerformed(evt);
            }
        });
        jPanel1.add(RL);
        lSP.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lSP.setText("USP");
        lSP.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lSP.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lSP);
        SP.setText("0000");
        SP.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SPActionPerformed(evt);
            }
        });
        jPanel1.add(SP);
        lSSP.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lSSP.setText("SSP");
        lSSP.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lSSP.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lSSP);
        SSP.setText("0000");
        SSP.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SSPActionPerformed(evt);
            }
        });
        jPanel1.add(SSP);
        lRE.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lRE.setText("RE");
        lRE.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lRE.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lRE);
        RE.setText("0000");
        RE.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                REActionPerformed(evt);
            }
        });
        jPanel1.add(RE);
        lR14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lR14.setText("BTE");
        lR14.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lR14.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lR14);
        R14.setText("0000");
        R14.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R14ActionPerformed(evt);
            }
        });
        jPanel1.add(R14);
        lR15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lR15.setText("TEMP");
        lR15.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lR15.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel1.add(lR15);
        R15.setText("0000");
        R15.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                R15ActionPerformed(evt);
            }
        });
        jPanel1.add(R15);
        jPanel4.add(jPanel1, java.awt.BorderLayout.NORTH);
        jPanel9.setLayout(new java.awt.GridLayout(2, 2));
        TD.setText("TD");
        TD.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TD.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        TD.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        TD.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TDActionPerformed(evt);
            }
        });
        jPanel9.add(TD);
        TV.setText("TV");
        TV.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TV.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        TV.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        TV.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TVActionPerformed(evt);
            }
        });
        jPanel9.add(TV);
        B.setText("B");
        B.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        B.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        B.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        B.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BActionPerformed(evt);
            }
        });
        jPanel9.add(B);
        A.setText("A");
        A.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        A.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        A.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        A.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AActionPerformed(evt);
            }
        });
        jPanel9.add(A);
        V.setText("V");
        V.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        V.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        V.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        V.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VActionPerformed(evt);
            }
        });
        jPanel9.add(V);
        C.setText("C");
        C.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        C.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        C.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        C.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CActionPerformed(evt);
            }
        });
        jPanel9.add(C);
        N.setText("N");
        N.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        N.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        N.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        N.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NActionPerformed(evt);
            }
        });
        jPanel9.add(N);
        Z.setText("Z");
        Z.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Z.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        Z.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        Z.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ZActionPerformed(evt);
            }
        });
        jPanel9.add(Z);
        R.setText("R");
        R.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        R.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        R.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        R.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RActionPerformed(evt);
            }
        });
        jPanel9.add(R);
        NP.setText("NP");
        NP.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        NP.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        NP.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        NP.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NPActionPerformed(evt);
            }
        });
        jPanel9.add(NP);
        DE.setText("DE");
        DE.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        DE.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        DE.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        DE.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DEActionPerformed(evt);
            }
        });
        jPanel9.add(DE);
        IE3.setText("IE3");
        IE3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        IE3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        IE3.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        IE3.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IE3ActionPerformed(evt);
            }
        });
        jPanel9.add(IE3);
        IE2.setText("IE2");
        IE2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        IE2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        IE2.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        IE2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IE2ActionPerformed(evt);
            }
        });
        jPanel9.add(IE2);
        IE1.setText("IE1");
        IE1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        IE1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        IE1.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        IE1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IE1ActionPerformed(evt);
            }
        });
        jPanel9.add(IE1);
        IE0.setText("IE0");
        IE0.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        IE0.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        IE0.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        IE0.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IE0ActionPerformed(evt);
            }
        });
        jPanel9.add(IE0);
        IE.setText("IE");
        IE.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        IE.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        IE.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        IE.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IEActionPerformed(evt);
            }
        });
        jPanel9.add(IE);
        jPanel4.add(jPanel9, java.awt.BorderLayout.CENTER);
        jPanel3.setLayout(new java.awt.GridLayout(8, 1));
        lrcn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lrcn.setText("RCN");
        lrcn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lrcn.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(lrcn);
        rcn.setText("0000");
        rcn.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rcnActionPerformed(evt);
            }
        });
        jPanel3.add(rcn);
        lrccd.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lrccd.setText("RCCD");
        lrccd.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lrccd.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(lrccd);
        rccd.setText("0000");
        rccd.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rccdActionPerformed(evt);
            }
        });
        jPanel3.add(rccd);
        lrcci.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lrcci.setText("RCCI");
        lrcci.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lrcci.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(lrcci);
        rcci.setText("0000");
        rcci.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rcciActionPerformed(evt);
            }
        });
        jPanel3.add(rcci);
        lrcmv.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lrcmv.setText("RCMV");
        lrcmv.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lrcmv.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(lrcmv);
        rcmv.setText("0000");
        rcmv.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rcmvActionPerformed(evt);
            }
        });
        jPanel3.add(rcmv);
        lrtp.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lrtp.setText("RTP");
        lrtp.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lrtp.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(lrtp);
        rtp.setText("0000");
        rtp.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                repActionPerformed(evt);
            }
        });
        jPanel3.add(rtp);
        lrpid.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lrpid.setText("RPID");
        lrpid.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lrpid.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(lrpid);
        rpid.setText("0000");
        rpid.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rpidActionPerformed(evt);
            }
        });
        jPanel3.add(rpid);
        la6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        la6.setText("A6");
        la6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        la6.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(la6);
        a6.setText("0000");
        a6.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                a6ActionPerformed(evt);
            }
        });
        jPanel3.add(a6);
        la7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        la7.setText("A7");
        la7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        la7.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(la7);
        a7.setText("0000");
        a7.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                a7ActionPerformed(evt);
            }
        });
        jPanel3.add(a7);
        la8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        la8.setText("A8");
        la8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        la8.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(la8);
        a8.setText("0000");
        a8.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                a8ActionPerformed(evt);
            }
        });
        jPanel3.add(a8);
        la9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        la9.setText("A9");
        la9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        la9.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(la9);
        a9.setText("0000");
        a9.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                a91ActionPerformed(evt);
            }
        });
        jPanel3.add(a9);
        la10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        la10.setText("A10");
        la10.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        la10.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(la10);
        a10.setText("0000");
        a10.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                a10ActionPerformed(evt);
            }
        });
        jPanel3.add(a10);
        la11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        la11.setText("A11");
        la11.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        la11.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(la11);
        a11.setText("0000");
        a11.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                a11ActionPerformed(evt);
            }
        });
        jPanel3.add(a11);
        la12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        la12.setText("A12");
        la12.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        la12.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(la12);
        a12.setText("0000");
        a12.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                a12ActionPerformed(evt);
            }
        });
        jPanel3.add(a12);
        la13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        la13.setText("A13");
        la13.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        la13.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(la13);
        a13.setText("0000");
        a13.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                a13ActionPerformed(evt);
            }
        });
        jPanel3.add(a13);
        la14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        la14.setText("A14");
        la14.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        la14.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(la14);
        a14.setText("0000");
        a14.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rpdActionPerformed(evt);
            }
        });
        jPanel3.add(a14);
        la15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        la15.setText("A15");
        la15.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        la15.setPreferredSize(new java.awt.Dimension(30, 17));
        jPanel3.add(la15);
        a15.setText("0000");
        a15.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                a15ActionPerformed(evt);
            }
        });
        a15.addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsListener() {

            public void ancestorMoved(java.awt.event.HierarchyEvent evt) {
                a15AncestorMoved(evt);
            }

            public void ancestorResized(java.awt.event.HierarchyEvent evt) {
            }
        });
        jPanel3.add(a15);
        jPanel4.add(jPanel3, java.awt.BorderLayout.SOUTH);
        jPanel5.add(jPanel4, java.awt.BorderLayout.NORTH);
        jScrollPane3.setMinimumSize(new java.awt.Dimension(20, 20));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(303, 103));
        symbolTable.setMaximumSize(new java.awt.Dimension(300, 32500));
        symbolTable.setPreferredSize(new java.awt.Dimension(300, 5000));
        symbolTable.setShowHorizontalLines(false);
        symbolTable.setShowVerticalLines(false);
        jScrollPane3.setViewportView(symbolTable);
        jPanel5.add(jScrollPane3, java.awt.BorderLayout.CENTER);
        jPanel2.add(jPanel5, java.awt.BorderLayout.EAST);
        jTabbedPane1.addTab("User Interface", jPanel2);
        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);
        pack();
    }

    private void a15AncestorMoved(java.awt.event.HierarchyEvent evt) {
    }

    private void a15ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(15, Integer.parseInt(a15.getText(), 16));
    }

    private void rpdActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(14, Integer.parseInt(a14.getText(), 16));
    }

    private void a13ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(13, Integer.parseInt(a13.getText(), 16));
    }

    private void a12ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(12, Integer.parseInt(a12.getText(), 16));
    }

    private void a11ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(11, Integer.parseInt(a11.getText(), 16));
    }

    private void a10ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(10, Integer.parseInt(a10.getText(), 16));
    }

    private void a91ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(9, Integer.parseInt(a9.getText(), 16));
    }

    private void a8ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(8, Integer.parseInt(a8.getText(), 16));
    }

    private void a7ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(7, Integer.parseInt(a7.getText(), 16));
    }

    private void a6ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(6, Integer.parseInt(a6.getText(), 16));
    }

    private void rpidActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(5, Integer.parseInt(rpid.getText(), 16));
    }

    private void repActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void rcmvActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(3, Integer.parseInt(rcmv.getText(), 16));
        wake();
    }

    private void rcciActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(2, Integer.parseInt(rcci.getText(), 16));
        wake();
    }

    private void rccdActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(1, Integer.parseInt(rccd.getText(), 16));
        wake();
    }

    private void rcnActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setAuxReg(0, Integer.parseInt(rcn.getText(), 16));
        wake();
    }

    protected void setUserWindowSize() {
    }

    private void bReloadActionPerformed(java.awt.event.ActionEvent evt) {
        reload();
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

    private void bLoadActionPerformed(java.awt.event.ActionEvent evt) {
        File lastOpenFile = loadInit();
        JFileChooser chooser = new JFileChooser(lastOpenFile);
        chooser.setFileFilter(new FilterCfg(".cod", "Code/Data Files"));
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
        loadFile(chooser.getSelectedFile());
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

    private void bStepActionPerformed(java.awt.event.ActionEvent evt) {
        if (!fBreakpoints.contains(step)) fBreakpoints.addElement(step);
        simulator.run();
    }

    private void bReset1ActionPerformed(java.awt.event.ActionEvent evt) {
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

    private void R15ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(15, Integer.parseInt(R15.getText(), 16));
    }

    private void R14ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(14, Integer.parseInt(R14.getText(), 16));
    }

    private void RLActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(11, Integer.parseInt(RL.getText(), 16));
    }

    private void REActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(13, Integer.parseInt(RE.getText(), 16));
    }

    private void SSPActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setSSP(Integer.parseInt(SSP.getText(), 16));
    }

    private void R10ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(10, Integer.parseInt(R10.getText(), 16));
    }

    private void R9ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(9, Integer.parseInt(R9.getText(), 16));
    }

    private void R8ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(8, Integer.parseInt(R8.getText(), 16));
    }

    private void IE0ActionPerformed(java.awt.event.ActionEvent evt) {
        if (IE0.isSelected()) ((IPepe) element).setFlag(ModulePepe.fIE0); else ((IPepe) element).resetFlag(ModulePepe.fIE0);
    }

    private void RActionPerformed(java.awt.event.ActionEvent evt) {
        if (R.isSelected()) ((IPepe) element).setFlag(ModulePepe.fR); else ((IPepe) element).resetFlag(ModulePepe.fR);
    }

    private void NPActionPerformed(java.awt.event.ActionEvent evt) {
        if (NP.isSelected()) ((IPepe) element).setFlag(ModulePepe.fNP); else ((IPepe) element).resetFlag(ModulePepe.fNP);
    }

    private void DEActionPerformed(java.awt.event.ActionEvent evt) {
        if (DE.isSelected()) ((IPepe) element).setFlag(ModulePepe.fDE); else ((IPepe) element).resetFlag(ModulePepe.fDE);
    }

    private void IE2ActionPerformed(java.awt.event.ActionEvent evt) {
        if (IE2.isSelected()) ((IPepe) element).setFlag(ModulePepe.fIE2); else ((IPepe) element).resetFlag(ModulePepe.fIE2);
    }

    private void IE1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (IE1.isSelected()) ((IPepe) element).setFlag(ModulePepe.fIE1); else ((IPepe) element).resetFlag(ModulePepe.fIE1);
    }

    private void IEActionPerformed(java.awt.event.ActionEvent evt) {
        if (IE.isSelected()) ((IPepe) element).setFlag(ModulePepe.fIE); else ((IPepe) element).resetFlag(ModulePepe.fIE);
    }

    private void TDActionPerformed(java.awt.event.ActionEvent evt) {
        if (TD.isSelected()) ((IPepe) element).setFlag(ModulePepe.fTD); else ((IPepe) element).resetFlag(ModulePepe.fTD);
    }

    private void AActionPerformed(java.awt.event.ActionEvent evt) {
        if (A.isSelected()) ((IPepe) element).setFlag(ModulePepe.fA); else ((IPepe) element).resetFlag(ModulePepe.fA);
    }

    private void IE3ActionPerformed(java.awt.event.ActionEvent evt) {
        if (IE3.isSelected()) ((IPepe) element).setFlag(ModulePepe.fIE3); else ((IPepe) element).resetFlag(ModulePepe.fIE3);
    }

    private void R7ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(7, Integer.parseInt(R7.getText(), 16));
    }

    private void R6ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(6, Integer.parseInt(R6.getText(), 16));
    }

    private void NActionPerformed(java.awt.event.ActionEvent evt) {
        if (N.isSelected()) ((IPepe) element).setFlag(ModulePepe.fN); else ((IPepe) element).resetFlag(ModulePepe.fN);
    }

    private void bResetActionPerformed(java.awt.event.ActionEvent evt) {
        simulator.reset();
    }

    private void TVActionPerformed(java.awt.event.ActionEvent evt) {
        if (TV.isSelected()) ((IPepe) element).setFlag(ModulePepe.fTV); else ((IPepe) element).resetFlag(ModulePepe.fTV);
    }

    private void BActionPerformed(java.awt.event.ActionEvent evt) {
        if (B.isSelected()) ((IPepe) element).setFlag(ModulePepe.fB); else ((IPepe) element).resetFlag(ModulePepe.fB);
    }

    private void VActionPerformed(java.awt.event.ActionEvent evt) {
        if (V.isSelected()) ((IPepe) element).setFlag(ModulePepe.fV); else ((IPepe) element).resetFlag(ModulePepe.fV);
    }

    private void CActionPerformed(java.awt.event.ActionEvent evt) {
        if (C.isSelected()) ((IPepe) element).setFlag(ModulePepe.fC); else ((IPepe) element).resetFlag(ModulePepe.fC);
    }

    private void ZActionPerformed(java.awt.event.ActionEvent evt) {
        if (Z.isSelected()) ((IPepe) element).setFlag(ModulePepe.fZ); else ((IPepe) element).resetFlag(ModulePepe.fZ);
    }

    private void PCActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setIP(Integer.parseInt(PC.getText(), 16));
    }

    private void SPActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setUSP(Integer.parseInt(SP.getText(), 16));
    }

    private void R5ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(5, Integer.parseInt(R5.getText(), 16));
    }

    private void R4ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(4, Integer.parseInt(R4.getText(), 16));
    }

    private void R3ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(3, Integer.parseInt(R3.getText(), 16));
    }

    private void R2ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(2, Integer.parseInt(R2.getText(), 16));
    }

    private void R1ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(1, Integer.parseInt(R1.getText(), 16));
    }

    private void R0ActionPerformed(java.awt.event.ActionEvent evt) {
        ((IPepe) element).setREG(0, Integer.parseInt(R0.getText(), 16));
    }

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {
        this.setVisible(false);
    }

    private void jPanel2ComponentShown(java.awt.event.ComponentEvent evt) {
        setUserWindowSize();
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
            int address = Integer.parseInt(tpCode.getValueAt(row, 0).toString(), 16);
            BreakPointAt bp = new BreakPointAt(address);
            fBreakpoints.addElement(bp);
            fColoredRows[ColorRenderer.RED].put(irow, bp);
        }
        tpCode.repaint();
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new GuiPepe().setVisible(true);
    }

    private javax.swing.JRadioButton A;

    private javax.swing.JRadioButton B;

    private javax.swing.JRadioButton C;

    private javax.swing.JRadioButton DE;

    private javax.swing.JRadioButton IE;

    private javax.swing.JRadioButton IE0;

    private javax.swing.JRadioButton IE1;

    private javax.swing.JRadioButton IE2;

    private javax.swing.JRadioButton IE3;

    private javax.swing.JLabel IPC;

    private javax.swing.JRadioButton N;

    private javax.swing.JRadioButton NP;

    private javax.swing.JTextField PC;

    private javax.swing.JRadioButton R;

    private javax.swing.JTextField R0;

    private javax.swing.JTextField R1;

    private javax.swing.JTextField R10;

    private javax.swing.JTextField R14;

    private javax.swing.JTextField R15;

    private javax.swing.JTextField R2;

    private javax.swing.JTextField R3;

    private javax.swing.JTextField R4;

    private javax.swing.JTextField R5;

    private javax.swing.JTextField R6;

    private javax.swing.JTextField R7;

    private javax.swing.JTextField R8;

    private javax.swing.JTextField R9;

    private javax.swing.JTextField RE;

    private javax.swing.JTextField RL;

    private javax.swing.JTextField SP;

    private javax.swing.JTextField SSP;

    private javax.swing.JRadioButton TD;

    private javax.swing.JRadioButton TV;

    private javax.swing.JRadioButton V;

    private javax.swing.JRadioButton Z;

    private javax.swing.JTextField a10;

    private javax.swing.JTextField a11;

    private javax.swing.JTextField a12;

    private javax.swing.JTextField a13;

    private javax.swing.JTextField a14;

    private javax.swing.JTextField a15;

    private javax.swing.JTextField a6;

    private javax.swing.JTextField a7;

    private javax.swing.JTextField a8;

    private javax.swing.JTextField a9;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JPanel jPanel3;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JPanel jPanel5;

    private javax.swing.JPanel jPanel9;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JScrollPane jScrollPane3;

    private javax.swing.JTabbedPane jTabbedPane1;

    private javax.swing.JLabel lR0;

    private javax.swing.JLabel lR1;

    private javax.swing.JLabel lR10;

    private javax.swing.JLabel lR14;

    private javax.swing.JLabel lR15;

    private javax.swing.JLabel lR2;

    private javax.swing.JLabel lR3;

    private javax.swing.JLabel lR4;

    private javax.swing.JLabel lR5;

    private javax.swing.JLabel lR6;

    private javax.swing.JLabel lR7;

    private javax.swing.JLabel lR8;

    private javax.swing.JLabel lR9;

    private javax.swing.JLabel lRE;

    private javax.swing.JLabel lRL;

    private javax.swing.JLabel lSP;

    private javax.swing.JLabel lSSP;

    private javax.swing.JLabel la10;

    private javax.swing.JLabel la11;

    private javax.swing.JLabel la12;

    private javax.swing.JLabel la13;

    private javax.swing.JLabel la14;

    private javax.swing.JLabel la15;

    private javax.swing.JLabel la6;

    private javax.swing.JLabel la7;

    private javax.swing.JLabel la8;

    private javax.swing.JLabel la9;

    private javax.swing.JLabel lrccd;

    private javax.swing.JLabel lrcci;

    private javax.swing.JLabel lrcmv;

    private javax.swing.JLabel lrcn;

    private javax.swing.JLabel lrpid;

    private javax.swing.JLabel lrtp;

    private javax.swing.JTextField rccd;

    private javax.swing.JTextField rcci;

    private javax.swing.JTextField rcmv;

    private javax.swing.JTextField rcn;

    private javax.swing.JTextField rpid;

    private javax.swing.JTextField rtp;

    private javax.swing.JTable symbolTable;

    private javax.swing.JTable tpCode;
}
