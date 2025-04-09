import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MineSweepRobot2 extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;

    JPanel pane = new JPanel();

    JButton bStart = new JButton(Messages.getString("MineSweepRobot2.bstart"));

    JButton bStop = new JButton(Messages.getString("MineSweepRobot2.bstop"));

    JButton bOption = new JButton(Messages.getString("MineSweepRobot2.boption"));

    JButton bHelp = new JButton(Messages.getString("MineSweepRobot2.bhelp"));

    void doStart() {
        try {
            bStart.setEnabled(false);
            doWork();
        } catch (Exception e) {
            e.printStackTrace();
            stopWork();
            JOptionPane.showMessageDialog(this, e.getLocalizedMessage());
            bStart.setEnabled(true);
        }
    }

    void doStop() {
        stopWork();
    }

    void doHelp() {
        JTextArea guide = new JTextArea(Messages.getString("MineSweepRobot2.userguide.content"));
        guide.setEditable(false);
        JScrollPane guidePane = new JScrollPane(guide);
        JTextArea license = new JTextArea(Messages.getString("MineSweepRobot2.license.content"));
        license.setEditable(false);
        JScrollPane licensePane = new JScrollPane(license);
        Object[] message = { Messages.getString("MineSweepRobot2.userguide.label"), guidePane, " ", Messages.getString("MineSweepRobot2.license.label"), licensePane };
        JOptionPane.showOptionDialog(this, message, Messages.getString("MineSweepRobot2.help.label"), JOptionPane.CLOSED_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
    }

    JPanel genInputPane(String label, JComponent field) {
        JPanel pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pane.add(new JLabel(label + ":"));
        pane.add(field);
        return pane;
    }

    void doOption() {
        JComboBox opMineSweeperType = new JComboBox(new String[] { "Windows XP Mine Sweeper" });
        opMineSweeperType.setEnabled(false);
        JPanel opMineSweeperTypePane = genInputPane(Messages.getString("MineSweepRobot2.minesweeper.type"), opMineSweeperType);
        JFormattedTextField opMouseDelay = new JFormattedTextField(new Long(mouseDelay));
        opMouseDelay.setColumns(5);
        JPanel opMouseDelayPane = genInputPane(Messages.getString("MineSweepRobot2.mouse.delay"), opMouseDelay);
        JCheckBox opMarkMineWithFlag = new JCheckBox(Messages.getString("MineSweepRobot2.mark.mine.with.flag"), markMineWithFlag);
        JCheckBox opDebug = new JCheckBox(Messages.getString("MineSweepRobot2.print.debug.info"), debug);
        JCheckBox opAdvancedAnalysis = new JCheckBox(Messages.getString("MineSweepRobot2.advanced.analysis"), advancedAnalysis);
        Object[] options = new Object[] { opMineSweeperTypePane, opMouseDelayPane, opMarkMineWithFlag, opAdvancedAnalysis, opDebug };
        int ret = JOptionPane.showOptionDialog(this, options, Messages.getString("MineSweepRobot2.options"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (ret == JOptionPane.OK_OPTION) {
            mouseDelay = ((Long) opMouseDelay.getValue()).longValue();
            markMineWithFlag = opMarkMineWithFlag.isSelected();
            debug = opDebug.isSelected();
            advancedAnalysis = opAdvancedAnalysis.isSelected();
        }
    }

    public MineSweepRobot2() {
        super(Messages.getString("MineSweepRobot2.minesweeper.robot"));
        init();
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        getContentPane().add("Center", pane);
        bStart.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Thread t = new Thread() {

                    public void run() {
                        doStart();
                    }
                };
                t.start();
            }
        });
        pane.add(bStart);
        bStop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                doStop();
            }
        });
        bOption.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                doOption();
            }
        });
        pane.add(bOption);
        bHelp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                doHelp();
            }
        });
        pane.add(bHelp);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        MineSweepRobot2 msr = new MineSweepRobot2();
        msr.setResizable(false);
        msr.setVisible(true);
        msr.pack();
    }

    long mouseDelay = 150;

    boolean markMineWithFlag = true;

    boolean debug = true;

    boolean advancedAnalysis = false;

    enum Icon {

        M_N0, M_N1, M_N2, M_N3, M_N4, M_N5, M_N6, M_N7, M_N8, M_BOMB, M_COVER, M_FLAG, M_MAYBE
    }

    ;

    int icon2MineNumber(Icon icon) {
        switch(icon) {
            case M_N0:
                return 0;
            case M_N1:
                return 1;
            case M_N2:
                return 2;
            case M_N3:
                return 3;
            case M_N4:
                return 4;
            case M_N5:
                return 5;
            case M_N6:
                return 6;
            case M_N7:
                return 7;
            case M_N8:
                return 8;
            default:
                return -1;
        }
    }

    Icon mineNumber2Icon(int mineNumber) {
        switch(mineNumber) {
            case 0:
                return Icon.M_N0;
            case 1:
                return Icon.M_N1;
            case 2:
                return Icon.M_N2;
            case 3:
                return Icon.M_N3;
            case 4:
                return Icon.M_N4;
            case 5:
                return Icon.M_N5;
            case 6:
                return Icon.M_N6;
            case 7:
                return Icon.M_N7;
            case 8:
                return Icon.M_N8;
            default:
                return null;
        }
    }

    enum State {

        S_UNKNOWN, S_UNSOLVED, S_SOLVED, S_ISMINE
    }

    ;

    BufferedImage n0;

    BufferedImage n1;

    BufferedImage n2;

    BufferedImage n3;

    BufferedImage n4;

    BufferedImage n5;

    BufferedImage n6;

    BufferedImage n7;

    BufferedImage n8;

    BufferedImage bomb;

    BufferedImage cover;

    BufferedImage flag;

    BufferedImage maybe;

    BufferedImage[] allIcons;

    int deltaX;

    int deltaY;

    void init() {
        try {
            r = new Robot();
            n0 = ImageIO.read(getClass().getResourceAsStream("minesweepericons/0.bmp"));
            n1 = ImageIO.read(getClass().getResourceAsStream("minesweepericons/1.bmp"));
            n2 = ImageIO.read(getClass().getResourceAsStream("minesweepericons/2.bmp"));
            n3 = ImageIO.read(getClass().getResourceAsStream("minesweepericons/3.bmp"));
            n4 = ImageIO.read(getClass().getResourceAsStream("minesweepericons/4.bmp"));
            n5 = ImageIO.read(getClass().getResourceAsStream("minesweepericons/5.bmp"));
            n6 = ImageIO.read(getClass().getResourceAsStream("minesweepericons/6.bmp"));
            n7 = ImageIO.read(getClass().getResourceAsStream("minesweepericons/7.bmp"));
            n8 = ImageIO.read(getClass().getResourceAsStream("minesweepericons/8.bmp"));
            bomb = ImageIO.read(getClass().getResourceAsStream("minesweepericons/bomb.bmp"));
            cover = ImageIO.read(getClass().getResourceAsStream("minesweepericons/cover.bmp"));
            flag = ImageIO.read(getClass().getResourceAsStream("minesweepericons/flag.bmp"));
            maybe = ImageIO.read(getClass().getResourceAsStream("minesweepericons/maybe.bmp"));
            allIcons = new BufferedImage[] { n0, n1, n2, n3, n4, n5, n6, n7, n8, bomb, cover, flag, maybe };
            deltaX = cover.getWidth() / 2;
            deltaY = cover.getHeight() / 2;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    Robot r;

    void debugPrint(Object o) {
        if (debug) System.out.print(o);
    }

    void debugPrintln(Object o) {
        if (debug) System.out.println(o);
    }

    void mouseMove(int x, int y) {
        r.mouseMove(x, y);
    }

    void sleep(long mills) {
        try {
            Thread.sleep(mills);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void mouseClick(int x, int y, int mask, boolean wait) {
        r.mouseMove(x, y);
        if (wait) sleep(mouseDelay);
        r.mousePress(mask);
        if (wait) sleep(mouseDelay);
        r.mouseRelease(mask);
    }

    void mouseClickLeftButton(int x, int y, boolean wait) {
        mouseClick(x, y, InputEvent.BUTTON1_MASK, true);
    }

    void mouseClickRightButton(int x, int y, boolean wait) {
        mouseClick(x, y, InputEvent.BUTTON3_MASK, true);
    }

    void markBlockFlagIcon(Block b) {
        switch(b.getIcon()) {
            case M_COVER:
                mouseClickRightButton(b.x + deltaX, b.y + deltaY, true);
                break;
            case M_FLAG:
                break;
            case M_MAYBE:
                mouseClickRightButton(b.x + deltaX, b.y + deltaY, true);
                mouseClickRightButton(b.x + deltaX, b.y + deltaY, true);
                break;
            default:
                throw new IndexOutOfBoundsException(Messages.getString("MineSweepRobot2.unknown.icon") + b.getIcon());
        }
        b.setIcon(Icon.M_FLAG);
    }

    void markBlockMaybeIcon(Block b) {
        switch(b.getIcon()) {
            case M_COVER:
                mouseClickRightButton(b.x + deltaX, b.y + deltaY, true);
                mouseClickRightButton(b.x + deltaX, b.y + deltaY, true);
                break;
            case M_FLAG:
                mouseClickRightButton(b.x + deltaX, b.y + deltaY, true);
                break;
            case M_MAYBE:
                break;
            default:
                throw new IndexOutOfBoundsException(Messages.getString("MineSweepRobot2.unknown.icon") + b.getIcon());
        }
        b.setIcon(Icon.M_FLAG);
    }

    void clearBlockFlag(Block b) {
        switch(b.getIcon()) {
            case M_COVER:
                break;
            case M_FLAG:
                mouseClickRightButton(b.x + deltaX, b.y + deltaY, true);
                mouseClickRightButton(b.x + deltaX, b.y + deltaY, true);
                break;
            case M_MAYBE:
                mouseClickRightButton(b.x + deltaX, b.y + deltaY, true);
                break;
            default:
                throw new IndexOutOfBoundsException(Messages.getString("MineSweepRobot2.unknown.icon") + b.getIcon());
        }
        b.setIcon(Icon.M_COVER);
    }

    boolean imagesEqual(BufferedImage i1, int x1, int y1, BufferedImage i2, int x2, int y2, int width, int height) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (i1.getRGB(x1 + x, y1 + y) != i2.getRGB(x2 + x, y2 + y)) {
                    return false;
                }
            }
        }
        return true;
    }

    Icon whichIcon(BufferedImage source, int x, int y) {
        return whichIcon(source, x, y, null);
    }

    Icon whichIcon(BufferedImage source, int x, int y, Icon preferIcon) {
        int width = cover.getWidth();
        int height = cover.getHeight();
        if (preferIcon != null) {
            if (imagesEqual(source, x, y, allIcons[preferIcon.ordinal()], 0, 0, width, height)) {
                return preferIcon;
            }
        }
        for (int i = 0; i < allIcons.length; i++) {
            if (preferIcon != null && i == preferIcon.ordinal()) {
                continue;
            }
            if (imagesEqual(source, x, y, allIcons[i], 0, 0, width, height)) {
                return Icon.values()[i];
            }
        }
        return null;
    }

    Block getBlock(int row, int col) {
        if (row < 0 || col < 0 || row >= getTotalRows() || col >= getTotalColumns()) {
            return null;
        }
        return matrix.get(row).get(col);
    }

    /**
     * Get one of block which is around given block. ��ȡ�õ�Ԫ��Χ�İ˸���Ԫ�е�һ������Ź��� 0 1 2 7 b 3 6 5 4
     * 
     * @param b
     *            ���ĵ�Ԫ
     * @param index
     *            ��Ŵ�0��ʼ�������Ͻǿ�ʼ��˳ʱ�롣
     * @return
     */
    Block getSurroundingBlock(Block b, int index) {
        switch(index) {
            case 0:
                return getBlock(b.row - 1, b.column - 1);
            case 1:
                return getBlock(b.row - 1, b.column);
            case 2:
                return getBlock(b.row - 1, b.column + 1);
            case 3:
                return getBlock(b.row, b.column + 1);
            case 4:
                return getBlock(b.row + 1, b.column + 1);
            case 5:
                return getBlock(b.row + 1, b.column);
            case 6:
                return getBlock(b.row + 1, b.column - 1);
            case 7:
                return getBlock(b.row, b.column - 1);
            default:
                throw new IllegalArgumentException("Index must between 0-7");
        }
    }

    List<List<Block>> matrix;

    List<Block> toBeCheckedBlocks;

    List<Group> toBeCheckedGroups;

    Set<Group> groups;

    void initDataBuffers() {
        matrix = null;
        toBeCheckedBlocks = new LinkedList<Block>();
        toBeCheckedGroups = new LinkedList<Group>();
        groups = new LinkedHashSet<Group>();
    }

    void addToBeCheckedBlocks(Block b) {
        toBeCheckedBlocks.add(0, b);
    }

    void addToBeCheckedGroups(Group g) {
        if (!toBeCheckedGroups.contains(g)) {
            toBeCheckedGroups.add(g);
        }
    }

    int getTotalRows() {
        return matrix.size();
    }

    int getTotalColumns() {
        return matrix.get(0).size();
    }

    boolean quickRebuidMatrix() {
        if (matrix == null) {
            return false;
        }
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage screen = r.createScreenCapture(new Rectangle(0, 0, (int) d.getWidth(), (int) d.getHeight()));
        int totalRow = getTotalRows();
        int totalCol = getTotalColumns();
        for (int row = 0; row < totalRow; row++) {
            for (int col = 0; col < totalCol; col++) {
                Block b = getBlock(row, col);
                Icon icon = whichIcon(screen, b.x, b.y, b.getIcon());
                if (icon == null) {
                    return false;
                }
                if (icon != b.getIcon()) {
                    b.setIcon(icon);
                    b.setState(getInitState(icon));
                    if (b.getState() == State.S_UNSOLVED) {
                        addToBeCheckedBlocks(b);
                    }
                }
            }
        }
        return true;
    }

    void rebuildMatrix() {
        long beginTime = System.currentTimeMillis();
        debugPrintln("Begin contruct mine matrix for minesweeper window... [0]milliSeconds");
        if (matrix != null) {
            debugPrintln("  Trying quick rebuild matrix on old data..." + " [" + (System.currentTimeMillis() - beginTime) + "]milliSeconds");
            if (quickRebuidMatrix()) {
                debugPrintln("  Trying quick rebuild matrix on old data...OK!" + " [" + (System.currentTimeMillis() - beginTime) + "]milliSeconds");
                return;
            }
            debugPrintln("  Trying quick rebuild matrix on old data...Failed! Retrying normal rebuild method..." + " [" + (System.currentTimeMillis() - beginTime) + "]milliSeconds");
        }
        initDataBuffers();
        int currentY = 0;
        int currentX = 0;
        List<Block> lineBlocks = null;
        int width = cover.getWidth();
        int height = cover.getHeight();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage screen = r.createScreenCapture(new Rectangle(0, 0, (int) d.getWidth(), (int) d.getHeight()));
        int maxX = screen.getWidth() - width;
        int maxY = screen.getHeight() - height;
        Block firstBlock = null;
        debugPrintln("  Trying to find first block..." + " [" + (System.currentTimeMillis() - beginTime) + "]milliSeconds");
        findFirst: for (currentY = 0; currentY <= maxY; currentY++) {
            for (currentX = 0; currentX <= maxX; currentX++) {
                Icon icon = whichIcon(screen, currentX, currentY);
                if (icon != null) {
                    firstBlock = new Block(icon, getInitState(icon), currentX, currentY, 0, 0);
                    matrix = new ArrayList<List<Block>>();
                    lineBlocks = new ArrayList<Block>();
                    lineBlocks.add(firstBlock);
                    if (firstBlock.getState() == State.S_UNSOLVED) {
                        addToBeCheckedBlocks(firstBlock);
                    }
                    break findFirst;
                }
            }
        }
        if (firstBlock == null) {
            throw new IllegalStateException(Messages.getString("MineSweepRobot2.not.found.minesweeper.window"));
        }
        debugPrintln("  Found first block: x=" + currentX + " y=" + currentY + " [" + (System.currentTimeMillis() - beginTime) + "]milliSeconds (continue to find other blocks)...");
        int firstX = currentX;
        currentX += width;
        endParsing: for (; currentY <= maxY; currentY += height) {
            parsingNextLine: for (; currentX <= maxX; currentX += width) {
                Icon icon = whichIcon(screen, currentX, currentY);
                if (icon != null) {
                    Block b = new Block(icon, getInitState(icon), currentX, currentY, matrix.size(), lineBlocks.size());
                    if (lineBlocks == null) {
                        lineBlocks = new ArrayList<Block>();
                        matrix.add(lineBlocks);
                    }
                    lineBlocks.add(b);
                    if (b.getState() == State.S_UNSOLVED) {
                        addToBeCheckedBlocks(b);
                    }
                } else {
                    if (matrix.size() > 0 && lineBlocks.size() == 0) {
                        break endParsing;
                    } else if (matrix.size() > 0 && lineBlocks.size() != matrix.get(matrix.size() - 1).size()) {
                        throw new IllegalStateException("Some line size[" + lineBlocks.size() + "] greater than last line size[" + matrix.get(matrix.size() - 1).size() + "]");
                    } else {
                        break parsingNextLine;
                    }
                }
            }
            if (lineBlocks.size() > 0) {
                matrix.add(lineBlocks);
            }
            lineBlocks = new ArrayList<Block>();
            currentX = firstX;
        }
        if (getTotalRows() < 2 || getTotalColumns() < 2) {
            throw new IllegalStateException("Too small minesweeper rows=[" + getTotalRows() + "] columns=[" + getTotalColumns() + "]");
        }
        debugPrintln("  Found all blocks: totalRows=" + getTotalRows() + " totalCols=" + getTotalColumns() + " [" + (System.currentTimeMillis() - beginTime) + "]milliSeconds");
    }

    State getInitState(Icon icon) {
        switch(icon) {
            case M_N0:
                return State.S_SOLVED;
            case M_N1:
            case M_N2:
            case M_N3:
            case M_N4:
            case M_N5:
            case M_N6:
            case M_N7:
            case M_N8:
                return State.S_UNSOLVED;
            case M_BOMB:
                throw new IllegalStateException(Messages.getString("MineSweepRobot2.failed.bombed"));
            case M_COVER:
            case M_FLAG:
            case M_MAYBE:
                return State.S_UNKNOWN;
            default:
                throw new IllegalStateException(Messages.getString("MineSweepRobot2.unknown.icon") + icon);
        }
    }

    void doWork() throws Exception {
        rebuildMatrix();
        Block b = getBlock(0, 0);
        mouseClickLeftButton(b.x - 2, b.y - 2, false);
        boolean completed = false;
        while ((toBeCheckedBlocks.size() > 0 || toBeCheckedGroups.size() > 0) && !completed) {
            while (toBeCheckedBlocks.size() > 0 || toBeCheckedGroups.size() > 0) {
                debugPrintln("loop");
                while (toBeCheckedBlocks.size() > 0) {
                    Block t = toBeCheckedBlocks.remove(0);
                    checkBlock(t);
                    while (toBeCheckedGroups.size() > 0) {
                        Group g = toBeCheckedGroups.remove(0);
                        checkGroup(g);
                    }
                }
                while (toBeCheckedGroups.size() > 0) {
                    Group g = toBeCheckedGroups.remove(0);
                    checkGroup(g);
                }
            }
            completed = isCompleted();
            if (!completed && advancedAnalysis) {
                tryAnalysis();
            }
        }
        if (completed) {
            throw new Exception(Messages.getString("MineSweepRobot2.game.completed"));
        } else {
            throw new Exception(Messages.getString("MineSweepRobot2.can.not.continue"));
        }
    }

    boolean isCompleted() {
        int totalRows = getTotalRows();
        int totalCols = getTotalColumns();
        boolean completed = true;
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < totalCols; col++) {
                Block t = getBlock(row, col);
                State state = t.getState();
                if (state != State.S_SOLVED && state != State.S_ISMINE) {
                    completed = false;
                    debugPrintln("Completed Check: Block[" + row + "," + col + "]=" + state);
                }
            }
        }
        return completed;
    }

    void checkBlock(Block b) {
        if (b.getState() != State.S_UNSOLVED) {
            return;
        }
        List<Block> unknownBlocks = new ArrayList<Block>();
        int mineCounter = 0;
        for (int i = 0; i < 8; i++) {
            Block t = getSurroundingBlock(b, i);
            if (t != null) {
                if (t.getState() == State.S_ISMINE) {
                    mineCounter++;
                } else if (t.getState() == State.S_UNKNOWN) {
                    unknownBlocks.add(t);
                }
            }
        }
        int mineNum = icon2MineNumber(b.getIcon());
        if (mineCounter == mineNum) {
            b.setState(State.S_SOLVED);
            for (int i = 0; i < unknownBlocks.size(); i++) {
                Block t = unknownBlocks.get(i);
                if (t.getState() == State.S_UNKNOWN) {
                    debugPrintln("Uncover Block:" + t);
                    uncoverBlock(t);
                }
            }
        } else if (mineCounter + unknownBlocks.size() == mineNum) {
            b.setState(State.S_SOLVED);
            for (int i = 0; i < unknownBlocks.size(); i++) {
                Block t = unknownBlocks.get(i);
                setBlockMine(t);
            }
        } else if (mineCounter + unknownBlocks.size() > mineNum && b.hasGenerateGroup == false) {
            Group g = new Group(mineNum - mineCounter);
            debugPrintln("Create A group:" + g.getMineNumber());
            for (int i = 0; i < unknownBlocks.size(); i++) {
                Block t = unknownBlocks.get(i);
                if (t.getState() == State.S_UNKNOWN) {
                    g.add(t);
                }
            }
            groups.add(g);
            addToBeCheckedGroups(g);
            b.hasGenerateGroup = true;
        } else if (mineCounter + unknownBlocks.size() < mineNum) {
            throw new IllegalStateException("Unknown error: mineCounter=" + mineCounter + " unknownBlocks=" + unknownBlocks.size() + " targeMineNumber=" + mineNum);
        }
    }

    void removeBlockFromAllGroups(Block b, boolean asMine) {
        if (b.sizeOfBelongToGroups() == 0) {
            return;
        }
        Group[] gs = b.getBelongToGroups();
        for (int i = 0; i < gs.length; i++) {
            gs[i].remove(b, asMine);
        }
        for (int i = 0; i < gs.length; i++) {
            addToBeCheckedGroups(gs[i]);
        }
    }

    void uncoverBlock(Block b) {
        if (b.getState() == State.S_UNKNOWN) {
            debugPrintln("To Click Block x=" + b.x + " y=" + b.y);
            clearBlockFlag(b);
            mouseClickLeftButton(b.x + deltaX, b.y + deltaY, true);
            recheckBlockIcon(b);
        }
    }

    void setBlockMine(Block b) {
        b.setState(State.S_ISMINE);
        if (markMineWithFlag) {
            markBlockFlagIcon(b);
        }
        removeBlockFromAllGroups(b, true);
        for (int i = 0; i < 8; i++) {
            Block t = getSurroundingBlock(b, i);
            if (t != null && t.getState() == State.S_UNSOLVED) {
                checkBlock(t);
            }
        }
    }

    void recheckBlockIcon(Block b) {
        BufferedImage blockArea = r.createScreenCapture(new Rectangle(b.x, b.y, cover.getWidth(), cover.getHeight()));
        Icon oldIcon = b.getIcon();
        State oldState = b.getState();
        Icon icon = whichIcon(blockArea, 0, 0, oldIcon);
        if (icon == null) {
            throw new IllegalStateException(Messages.getString("MineSweepRobot2.unknown.icon"));
        }
        if (icon == oldIcon) {
            return;
        }
        b.setIcon(icon);
        State newState = getInitState(icon);
        b.setState(newState);
        if (newState == State.S_UNSOLVED || newState == State.S_SOLVED) {
            if (oldState == State.S_UNKNOWN) {
                removeBlockFromAllGroups(b, false);
            }
        }
        if (b.getState() == State.S_UNSOLVED) {
            addToBeCheckedBlocks(b);
            return;
        }
        if (icon == Icon.M_N0) {
            for (int i = 0; i < 8; i++) {
                Block t = getSurroundingBlock(b, i);
                if (t != null && t.getState() == State.S_UNKNOWN) {
                    recheckBlockIcon(t);
                }
            }
        }
    }

    boolean groupFinalStateCheck(Group g) {
        if (g.getMineNumber() < 0) {
            throw new IllegalStateException("Unknown error: Group minenumber < 0 :" + g.getMineNumber());
        }
        if (g.size() == 0) {
            groups.remove(g);
            return true;
        }
        if (g.getMineNumber() == 0) {
            Block[] bs = g.getBlocks();
            for (int i = 0; i < bs.length; i++) {
                uncoverBlock(bs[i]);
            }
            g.clear();
            groups.remove(g);
            return true;
        }
        if (g.getMineNumber() == g.size()) {
            Block[] bs = g.getBlocks();
            for (int i = 0; i < bs.length; i++) {
                setBlockMine(bs[i]);
            }
            g.clear();
            groups.remove(g);
            return true;
        }
        return false;
    }

    void checkGroup(Group g) {
        debugPrintln("checkGroup = " + g);
        if (groupFinalStateCheck(g)) {
            return;
        }
        Set<Group> s = g.getIntersectionGroups();
        for (Group x : s) {
            if (g == x) {
                continue;
            }
            if (g.size() == x.size() && g.contains(x)) {
                g.clear();
                groups.remove(g);
                return;
            } else if (g.size() > x.size() && g.contains(x)) {
                g.removeAll(x);
                g.setMineNumber(g.getMineNumber() - x.getMineNumber());
                if (groupFinalStateCheck(g)) {
                    return;
                }
            } else if (g.size() < x.size() && x.contains(g)) {
                x.removeAll(g);
                x.setMineNumber(x.getMineNumber() - g.getMineNumber());
                debugPrintln("After  x.removeAll(g); x = " + x);
                addToBeCheckedGroups(x);
                return;
            }
        }
    }

    enum FakeState {

        FS_ISMINE, FS_NOTMINE, FS_UNKNOWN
    }

    ;

    void tryAnalysis() {
        if (groups.size() == 0) {
            throw new IllegalStateException("Unknown errror: no available groups!");
        }
        Set<Block> mineableBlocks = new HashSet<Block>();
        Set<Block> uncoverableBlocks = new HashSet<Block>();
        for (Group g : groups) {
            List<Map<Block, FakeState>> combs = g.getAvailableCombinations();
            if (combs == null || combs.size() == 0) {
                continue;
            }
            if (combs.size() == 1) {
                if (debug) debugPrintln("Found OK combination: " + combs.get(0));
                Map<Block, FakeState> c = combs.get(0);
                for (Map.Entry<Block, FakeState> e : c.entrySet()) {
                    if (e.getValue() == FakeState.FS_ISMINE) {
                        mineableBlocks.add(e.getKey());
                    } else {
                        uncoverableBlocks.add(e.getKey());
                    }
                }
            } else if (combs.size() > 1) {
                if (debug) {
                    debugPrintln("Found multi combiniations: " + combs.size());
                    for (Map<Block, FakeState> c : combs) {
                        debugPrintln("   Found possible combination: " + c);
                    }
                }
                Map<Block, FakeState> c = combs.get(0);
                for (Map.Entry<Block, FakeState> e : c.entrySet()) {
                    if (e.getValue() == FakeState.FS_NOTMINE) {
                        boolean allOK = true;
                        for (int i = 1; i < combs.size(); i++) {
                            if (combs.get(i).get(e.getKey()) != FakeState.FS_NOTMINE) {
                                allOK = false;
                            }
                        }
                        if (allOK) {
                            uncoverableBlocks.add(e.getKey());
                        }
                    }
                }
            }
            if (mineableBlocks.size() > 0 || uncoverableBlocks.size() > 0) {
                for (Block b : mineableBlocks) {
                    setBlockMine(b);
                }
                for (Block b : uncoverableBlocks) {
                    uncoverBlock(b);
                }
                return;
            }
        }
    }

    void stopWork() {
    }

    class Block {

        public int row;

        public int column;

        Icon icon;

        State state;

        public int x;

        public int y;

        public boolean hasGenerateGroup = false;

        Set<Group> belongToGroups = new HashSet<Group>();

        public Block(Icon icon, State state, int x, int y, int row, int column) {
            this.icon = icon;
            this.state = state;
            this.x = x;
            this.y = y;
            this.row = row;
            this.column = column;
        }

        public Group[] getBelongToGroups() {
            return belongToGroups.toArray(new Group[belongToGroups.size()]);
        }

        public int belongToHowManyGroups() {
            return belongToGroups.size();
        }

        public Icon getIcon() {
            return icon;
        }

        public void setIcon(Icon icon) {
            this.icon = icon;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public String toString() {
            return "[icon=" + icon + " state=" + state + " row=" + row + " col=" + column + "]" + " x=" + x + " y=" + y;
        }

        public int sizeOfBelongToGroups() {
            return belongToGroups.size();
        }
    }

    class Group {

        int mineNumber;

        List<Block> blocks = new LinkedList<Block>();

        public Group(int mineNumber) {
            this.mineNumber = mineNumber;
        }

        int getMineNumber() {
            return mineNumber;
        }

        void setMineNumber(int mineNumber) {
            this.mineNumber = mineNumber;
        }

        public Block[] getBlocks() {
            return blocks.toArray(new Block[blocks.size()]);
        }

        public int size() {
            return blocks.size();
        }

        public boolean add(Block b) {
            if (blocks.add(b)) {
                b.belongToGroups.add(this);
                return true;
            }
            return false;
        }

        public boolean remove(Block b) {
            return remove(b, false);
        }

        public boolean remove(Block b, boolean asMine) {
            if (blocks.remove(b)) {
                b.belongToGroups.remove(this);
                if (asMine) {
                    mineNumber--;
                }
                return true;
            }
            return false;
        }

        public boolean contains(Block b) {
            return blocks.contains(b);
        }

        public boolean contains(Group g) {
            return blocks.containsAll(g.blocks);
        }

        public boolean removeAll(Group g) {
            boolean ret = false;
            for (Block b : g.blocks) {
                if (this.remove(b)) {
                    ret = true;
                }
            }
            return ret;
        }

        public void clear() {
            for (Block b : blocks) {
                b.belongToGroups.remove(this);
            }
            blocks.clear();
        }

        public String toString() {
            return "{mineNumber=" + mineNumber + " size=" + size() + " blocks=" + blocks + "}";
        }

        /**
         * Get all groups which intersect with this group;
         * 
         * @return
         */
        public Set<Group> getIntersectionGroups() {
            Set<Group> s = new HashSet<Group>();
            for (Block b : blocks) {
                s.addAll(b.belongToGroups);
            }
            s.remove(this);
            return s;
        }

        public List<Map<Block, FakeState>> getAvailableCombinations() {
            final Set<Group> intersections = getIntersectionGroups();
            if (intersections.size() == 0) {
                return null;
            }
            debugPrintln("getAvailableCombinations for Group: " + this);
            final List<Map<Block, FakeState>> possibleCombs = new ArrayList<Map<Block, FakeState>>();
            final HashMap<Block, FakeState> fakeStates = new HashMap<Block, FakeState>();
            for (Block b : blocks) {
                fakeStates.put(b, FakeState.FS_NOTMINE);
            }
            Combination com = new Combination(new CombinationHandler() {

                public void setPlace(int index) {
                    fakeStates.put(blocks.get(index), FakeState.FS_ISMINE);
                }

                public void unsetPlace(int index) {
                    fakeStates.put(blocks.get(index), FakeState.FS_NOTMINE);
                }

                public boolean handleCombination() {
                    Set<Group> needNotCheckGroups = new HashSet<Group>();
                    needNotCheckGroups.add(Group.this);
                    for (Group g : intersections) {
                        if (!g.isPossibleCombination(fakeStates, needNotCheckGroups)) {
                            debugPrintln("  Checking intersected Group failed: " + g);
                            return true;
                        }
                        debugPrintln("  Checking intersected Group OK: " + g);
                    }
                    possibleCombs.add((Map<Block, FakeState>) fakeStates.clone());
                    return true;
                }
            });
            com.startExhaustingCombinations(this.size(), this.getMineNumber());
            return possibleCombs;
        }

        /**
         * 
         * @param assumeBlockStates ����������ܵ�����ϣ����صĿ��ܵ�����Ͽ϶�������һ�¡�
         * @return
         */
        boolean isPossibleCombination(Map<Block, FakeState> fakeStates, final Set<Group> needNotCheckGroups) {
            if (needNotCheckGroups.contains(this)) {
                debugPrintln("    reexhausting one group, ignore!");
                return true;
            }
            needNotCheckGroups.add(this);
            final Set<Group> intersections = getIntersectionGroups();
            if (intersections.size() == 0) {
                throw new IllegalStateException("Checking a non-intersection group has no sense");
            }
            debugPrintln("    Checking Group: " + this);
            final Map<Block, FakeState> assumeStates = fakeStates;
            final List<Block> availBlocks = new ArrayList<Block>();
            int isMineCounter = 0;
            int notMineCounter = 0;
            for (Block b : blocks) {
                FakeState s = assumeStates.get(b);
                if (s != null) {
                    switch(s) {
                        case FS_ISMINE:
                            isMineCounter++;
                            break;
                        case FS_NOTMINE:
                            notMineCounter++;
                            break;
                        default:
                            throw new IllegalStateException("Unknown state");
                    }
                } else {
                    availBlocks.add(b);
                }
            }
            debugPrintln("      assume states: mines=" + isMineCounter + " notMines=" + notMineCounter);
            if (isMineCounter > this.getMineNumber() || this.size() - notMineCounter < this.getMineNumber()) {
                return false;
            }
            if (this.size() == isMineCounter + notMineCounter) {
                debugPrintln("      all blocks of this group is checked: assume mines == target mines: " + (isMineCounter == this.getMineNumber()));
                return (isMineCounter == this.getMineNumber());
            }
            for (Block b : availBlocks) {
                assumeStates.put(b, FakeState.FS_NOTMINE);
            }
            int availMineNum = this.getMineNumber() - isMineCounter;
            Combination com = new Combination(new CombinationHandler() {

                public void setPlace(int index) {
                    assumeStates.put(availBlocks.get(index), FakeState.FS_ISMINE);
                }

                public void unsetPlace(int index) {
                    assumeStates.put(availBlocks.get(index), FakeState.FS_NOTMINE);
                }

                public boolean handleCombination() {
                    for (Group g : intersections) {
                        if (!g.isPossibleCombination(assumeStates, needNotCheckGroups)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            boolean ret = !com.startExhaustingCombinations(availBlocks.size(), availMineNum);
            for (Block b : availBlocks) {
                assumeStates.remove(b);
            }
            return ret;
        }
    }

    static interface CombinationHandler {

        public void setPlace(int index);

        public void unsetPlace(int index);

        public boolean handleCombination();
    }

    static class Combination {

        CombinationHandler handler;

        Combination(CombinationHandler handler) {
            this.handler = handler;
        }

        boolean doCombinate(int n, int m, int index) {
            if (m <= 0) {
                return handler.handleCombination();
            }
            for (int i = 0; i <= n - m; i++) {
                handler.setPlace(index + i);
                if (!doCombinate(n - 1 - i, m - 1, index + 1 + i)) {
                    return false;
                }
                handler.unsetPlace(index + i);
            }
            return true;
        }

        public boolean startExhaustingCombinations(int n, int m) {
            return doCombinate(n, m, 0);
        }
    }
}
