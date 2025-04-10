package jgamess.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.SwingUtilities;
import com.sun.pdfview.action.GoToAction;
import com.sun.pdfview.action.PDFAction;
import com.sun.pdfview.*;
import java.lang.reflect.InvocationTargetException;

/**
 * A PDF Viewer application that integrates with the
 * <a href="http://sunlabs.east/mc/">Meeting Central</a> project.
 */
public class PDFDisplayFrame extends JFrame implements KeyListener, TreeSelectionListener, PageChangeListener {

    public static final String TITLE = "SwingLabs PDF Viewer";

    public static final String FULL_SCREEN = "FULL SCREEN";

    public static final String CLOSE = "CLOSE";

    public static final String PRINT = "PRINT";

    /** The current PDFFile */
    PDFFile curFile;

    /** the name of the current document */
    String docName;

    /** The split between thumbs and page */
    JSplitPane split;

    /** The thumbnail scroll pane */
    JScrollPane thumbscroll;

    /** The thumbnail display */
    ThumbPanel thumbs;

    /** The page display */
    PagePanel page;

    /** The full screen page display, or null if not in full screen mode */
    PagePanel fspp;

    JTextField pageField;

    /** The current page number (starts at 0), or -1 if no page */
    int curpage = -1;

    /** the full screen button */
    JToggleButton fullScreenButton;

    /** the full screen window, or null if not in full screen mode */
    FullScreenWindow fullScreen;

    /** the root of the outline, or null if there is no outline */
    OutlineNode outline = null;

    /** The page format for printing */
    PageFormat pformat = PrinterJob.getPrinterJob().defaultPage();

    /** true if the thumb panel should exist at all */
    boolean doThumb = true;

    /** flag to indicate when a newly added document has been announced */
    Flag docWaiter;

    /** a thread that pre-loads the next page for faster response */
    PagePreparer pagePrep;

    /** the window containing the pdf outline, or null if one doesn't exist */
    JDialog olf;

    /** the document menu */
    JMenu docMenu;

    /**
     * utility method to get an icon from the resources of this class
     * @param name the name of the icon
     * @return the icon, or null if the icon wasn't found.
     */
    public Icon getIcon(String name) {
        ImageIcon icon = new ImageIcon(name);
        return icon;
    }

    Action openAction = new AbstractAction("Open...") {

        static final long serialVersionUID = 1;

        public void actionPerformed(ActionEvent evt) {
            doOpen();
        }
    };

    Action pageSetupAction = new AbstractAction("Page setup...") {

        static final long serialVersionUID = 1;

        public void actionPerformed(ActionEvent evt) {
            doPageSetup();
        }
    };

    Action printAction = new AbstractAction("Print...", getIcon("gfx/print.gif")) {

        static final long serialVersionUID = 1;

        public void actionPerformed(ActionEvent evt) {
            doPrint();
        }
    };

    Action closeAction = new AbstractAction("Close") {

        static final long serialVersionUID = 1;

        public void actionPerformed(ActionEvent evt) {
            doClose();
        }
    };

    Action quitAction = new AbstractAction("Quit") {

        static final long serialVersionUID = 1;

        public void actionPerformed(ActionEvent evt) {
            doQuit();
        }
    };

    class ZoomAction extends AbstractAction {

        double zoomfactor = 1.0;

        static final long serialVersionUID = 1;

        public ZoomAction(String name, double factor) {
            super(name);
            zoomfactor = factor;
        }

        public ZoomAction(String name, Icon icon, double factor) {
            super(name, icon);
            zoomfactor = factor;
        }

        public void actionPerformed(ActionEvent evt) {
            doZoom(zoomfactor);
        }
    }

    ZoomAction zoomInAction = new ZoomAction("Zoom in", getIcon("zoom_out_med.png"), 2.0);

    ZoomAction zoomOutAction = new ZoomAction("Zoom out", getIcon("zoom_out_med.png"), 0.5);

    Action zoomToolAction = new AbstractAction("", getIcon("zoom_in_med.png")) {

        public void actionPerformed(ActionEvent evt) {
            doZoomTool();
        }
    };

    Action fitInWindowAction = new AbstractAction("Fit in window", getIcon("zoom_in_med.png")) {

        static final long serialVersionUID = 1;

        public void actionPerformed(ActionEvent evt) {
            doFitInWindow();
        }
    };

    class ThumbAction extends AbstractAction implements PropertyChangeListener {

        static final long serialVersionUID = 1;

        boolean isOpen = true;

        public ThumbAction() {
            super("Hide thumbnails");
        }

        public void propertyChange(PropertyChangeEvent evt) {
            int v = ((Integer) evt.getNewValue()).intValue();
            if (v <= 1) {
                isOpen = false;
                putValue(ACTION_COMMAND_KEY, "Show thumbnails");
                putValue(NAME, "Show thumbnails");
            } else {
                isOpen = true;
                putValue(ACTION_COMMAND_KEY, "Hide thumbnails");
                putValue(NAME, "Hide thumbnails");
            }
        }

        public void actionPerformed(ActionEvent evt) {
            doThumbs(!isOpen);
        }
    }

    ThumbAction thumbAction = new ThumbAction();

    Action fullScreenAction = new AbstractAction("Full screen", getIcon("gfx/fullscrn.gif")) {

        static final long serialVersionUID = 1;

        public void actionPerformed(ActionEvent evt) {
            doFullScreen((evt.getModifiers() & ActionEvent.SHIFT_MASK) != 0);
        }
    };

    Action nextAction = new AbstractAction("Next", getIcon("next.png")) {

        static final long serialVersionUID = 1;

        public void actionPerformed(ActionEvent evt) {
            doNext();
        }
    };

    Action firstAction = new AbstractAction("First", getIcon("gfx/first.gif")) {

        static final long serialVersionUID = 1;

        public void actionPerformed(ActionEvent evt) {
            doFirst();
        }
    };

    Action lastAction = new AbstractAction("Last", getIcon("gfx/last.gif")) {

        static final long serialVersionUID = 1;

        public void actionPerformed(ActionEvent evt) {
            doLast();
        }
    };

    Action prevAction = new AbstractAction("Prev", getIcon("prev.png")) {

        static final long serialVersionUID = 1;

        public void actionPerformed(ActionEvent evt) {
            doPrev();
        }
    };

    /**
     * Create a new PDFViewer based on a user, with or without a thumbnail
     * panel.
     * @param useThumbs true if the thumb panel should exist, false if not.
     */
    public PDFDisplayFrame(boolean useThumbs) {
        super(TITLE);
        doThumb = useThumbs;
        init();
    }

    /**
     * Initialize this PDFViewer by creating the GUI.
     */
    protected void init() {
        page = new PagePanel();
        page.setBackground(Color.DARK_GRAY);
        page.addKeyListener(this);
        if (doThumb) {
            split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, thumbAction);
            split.setOneTouchExpandable(true);
            thumbs = new ThumbPanel(null);
            thumbscroll = new JScrollPane(thumbs, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            split.setLeftComponent(thumbscroll);
            split.setRightComponent(page);
            getContentPane().add(split, BorderLayout.CENTER);
        } else {
            getContentPane().add(page, BorderLayout.CENTER);
        }
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton jb;
        jb = new JButton(firstAction);
        jb.setText("");
        toolbar.add(jb);
        jb = new JButton(prevAction);
        jb.setText("");
        toolbar.add(jb);
        pageField = new JTextField("-", 3);
        pageField.setMaximumSize(new Dimension(45, 32));
        pageField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                doPageTyped();
            }
        });
        toolbar.add(pageField);
        jb = new JButton(nextAction);
        jb.setText("");
        toolbar.add(jb);
        jb = new JButton(lastAction);
        jb.setText("");
        toolbar.add(jb);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(Box.createHorizontalGlue());
        jb = new JButton(printAction);
        jb.setText("");
        toolbar.add(jb);
        getContentPane().add(toolbar, BorderLayout.NORTH);
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");
        file.add(openAction);
        file.addSeparator();
        file.add(pageSetupAction);
        file.add(printAction);
        file.addSeparator();
        file.add(quitAction);
        mb.add(file);
        JMenu view = new JMenu("View");
        JMenu zoom = new JMenu("Zoom");
        zoom.add(zoomInAction);
        zoom.add(zoomOutAction);
        zoom.add(fitInWindowAction);
        zoom.setEnabled(false);
        view.add(zoom);
        if (doThumb) {
            view.addSeparator();
            view.add(thumbAction);
        }
        mb.add(view);
        setJMenuBar(mb);
        setEnabling();
        pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - getWidth()) / 2;
        int y = (screen.height - getHeight()) / 2;
        setLocation(x, y);
        if (SwingUtilities.isEventDispatchThread()) {
            show();
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        show();
                    }
                });
            } catch (InvocationTargetException ie) {
            } catch (InterruptedException ie) {
            }
        }
    }

    /**
     * Changes the displayed page, desyncing if we're not on the
     * same page as a presenter.
     * @param pagenum the page to display
     */
    public void gotoPage(int pagenum) {
        if (pagenum < 0) {
            pagenum = 0;
        } else if (pagenum >= curFile.getNumPages()) {
            pagenum = curFile.getNumPages() - 1;
        }
        forceGotoPage(pagenum);
    }

    /**
     * Changes the displayed page.
     * @param pagenum the page to display
     */
    public void forceGotoPage(int pagenum) {
        if (pagenum <= 0) {
            pagenum = 0;
        } else if (pagenum >= curFile.getNumPages()) {
            pagenum = curFile.getNumPages() - 1;
        }
        curpage = pagenum;
        pageField.setText(String.valueOf(curpage + 1));
        PDFPage pg = curFile.getPage(pagenum + 1);
        if (fspp != null) {
            fspp.showPage(pg);
            fspp.requestFocus();
        } else {
            page.showPage(pg);
            page.requestFocus();
        }
        if (doThumb) {
            thumbs.pageShown(pagenum);
        }
        if (pagePrep != null) {
            pagePrep.quit();
        }
        pagePrep = new PagePreparer(pagenum);
        pagePrep.start();
        setEnabling();
    }

    /**
     * A class to pre-cache the next page for better UI response
     */
    class PagePreparer extends Thread {

        int waitforPage;

        int prepPage;

        /**
         * Creates a new PagePreparer to prepare the page after the current
         * one.
         * @param waitforPage the current page number, 0 based 
         */
        public PagePreparer(int waitforPage) {
            setDaemon(true);
            this.waitforPage = waitforPage;
            this.prepPage = waitforPage + 1;
        }

        public void quit() {
            waitforPage = -1;
        }

        public void run() {
            Dimension size = null;
            Rectangle2D clip = null;
            if (fspp != null) {
                fspp.waitForCurrentPage();
                size = fspp.getCurSize();
                clip = fspp.getCurClip();
            } else if (page != null) {
                page.waitForCurrentPage();
                size = page.getCurSize();
                clip = page.getCurClip();
            }
            if (waitforPage == curpage) {
                PDFPage pdfPage = curFile.getPage(prepPage + 1, true);
                if (pdfPage != null && waitforPage == curpage) {
                    pdfPage.getImage(size.width + 200, size.height, clip, null, true, true);
                }
            }
        }
    }

    /**
     * Enable or disable all of the actions based on the current state.
     */
    public void setEnabling() {
        boolean fileavailable = curFile != null;
        boolean pageshown = ((fspp != null) ? fspp.getPage() != null : page.getPage() != null);
        boolean printable = fileavailable && curFile.isPrintable();
        pageField.setEnabled(fileavailable);
        printAction.setEnabled(printable);
        closeAction.setEnabled(fileavailable);
        fullScreenAction.setEnabled(false);
        prevAction.setEnabled(pageshown);
        nextAction.setEnabled(pageshown);
        firstAction.setEnabled(fileavailable);
        lastAction.setEnabled(fileavailable);
        zoomToolAction.setEnabled(pageshown);
        fitInWindowAction.setEnabled(pageshown);
        zoomInAction.setEnabled(pageshown);
        zoomOutAction.setEnabled(pageshown);
    }

    /**
     * Open a specific pdf file.  Creates a DocumentInfo from the file,
     * and opens that.
     * @param file the file to open
     */
    public void openFile(File file) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        PDFFile newfile = null;
        try {
            newfile = new PDFFile(buf);
        } catch (IOException ioe) {
            openError(file.getPath() + " doesn't appear to be a PDF file.");
            return;
        }
        doClose();
        this.curFile = newfile;
        docName = file.getName();
        setTitle(TITLE + ": " + docName);
        if (doThumb) {
            thumbs = new ThumbPanel(curFile);
            thumbs.addPageChangeListener(this);
            thumbscroll.getViewport().setView(thumbs);
            thumbscroll.getViewport().setBackground(Color.gray);
        }
        setEnabling();
        forceGotoPage(0);
        try {
            outline = curFile.getOutline();
        } catch (IOException ioe) {
        }
        if (outline != null) {
            if (outline.getChildCount() > 0) {
                olf = new JDialog(this, "Outline");
                olf.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                olf.setLocation(this.getLocation());
                JTree jt = new JTree(outline);
                jt.setRootVisible(false);
                jt.addTreeSelectionListener(this);
                JScrollPane jsp = new JScrollPane(jt);
                olf.getContentPane().add(jsp);
                olf.pack();
                olf.setVisible(true);
            } else {
                if (olf != null) {
                    olf.setVisible(false);
                    olf = null;
                }
            }
        }
    }

    /**
         * Display a dialog indicating an error.
         */
    public void openError(String message) {
        JOptionPane.showMessageDialog(split, message, "Error opening file", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * A file filter for PDF files.
     */
    FileFilter pdfFilter = new FileFilter() {

        public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".pdf");
        }

        public String getDescription() {
            return "Choose a PDF file";
        }
    };

    private File prevDirChoice;

    /**
     * Ask the user for a PDF file to open from the local file system
     */
    public void doOpen() {
        try {
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(prevDirChoice);
            fc.setFileFilter(pdfFilter);
            fc.setMultiSelectionEnabled(false);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    prevDirChoice = fc.getSelectedFile();
                    openFile(fc.getSelectedFile());
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(split, "Opening files from your local " + "disk is not available\nfrom the " + "Java Web Start version of this " + "program.\n", "Error opening directory", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Open a local file, given a string filename
     * @param name the name of the file to open
     */
    public void doOpen(String name) {
        try {
            openFile(new File(name));
        } catch (IOException ioe) {
        }
    }

    /**
     * Posts the Page Setup dialog
     */
    public void doPageSetup() {
        PrinterJob pjob = PrinterJob.getPrinterJob();
        pformat = pjob.pageDialog(pformat);
    }

    /**
     * A thread for printing in.
     */
    class PrintThread extends Thread {

        PDFPrintPage ptPages;

        PrinterJob ptPjob;

        public PrintThread(PDFPrintPage pages, PrinterJob pjob) {
            ptPages = pages;
            ptPjob = pjob;
        }

        public void run() {
            try {
                ptPages.show(ptPjob);
                ptPjob.print();
            } catch (PrinterException pe) {
                JOptionPane.showMessageDialog(PDFDisplayFrame.this, "Printing Error: " + pe.getMessage(), "Print Aborted", JOptionPane.ERROR_MESSAGE);
            }
            ptPages.hide();
        }
    }

    /**
     * Print the current document.
     */
    public void doPrint() {
        PrinterJob pjob = PrinterJob.getPrinterJob();
        pjob.setJobName(docName);
        Book book = new Book();
        PDFPrintPage pages = new PDFPrintPage(curFile);
        book.append(pages, pformat, curFile.getNumPages());
        pjob.setPageable(book);
        if (pjob.printDialog()) {
            new PrintThread(pages, pjob).start();
        }
    }

    /**
     * Close the current document.
     */
    public void doClose() {
        if (thumbs != null) {
            thumbs.stop();
        }
        if (olf != null) {
            olf.setVisible(false);
            olf = null;
        }
        if (doThumb) {
            thumbs = new ThumbPanel(null);
            thumbscroll.getViewport().setView(thumbs);
        }
        setFullScreenMode(false, false);
        page.showPage(null);
        curFile = null;
        setTitle(TITLE);
        setEnabling();
    }

    /**
     * Shuts down all known threads.  This ought to cause the JVM to quit
     * if the PDFViewer is the only application running.
     */
    public void doQuit() {
        doClose();
        dispose();
    }

    /**
     * Turns on zooming
     */
    public void doZoomTool() {
        if (fspp == null) {
            page.useZoomTool(true);
        }
    }

    /**
     * Turns off zooming; makes the page fit in the window
     */
    public void doFitInWindow() {
        if (fspp == null) {
            page.useZoomTool(false);
            page.setClip(null);
        }
    }

    /**
     * Shows or hides the thumbnails by moving the split pane divider
     */
    public void doThumbs(boolean show) {
        if (show) {
            split.setDividerLocation((int) thumbs.getPreferredSize().width + (int) thumbscroll.getVerticalScrollBar().getWidth() + 4);
        } else {
            split.setDividerLocation(0);
        }
    }

    /**
     * Enter full screen mode
     * @param force true if the user should be prompted for a screen to
     * use in a multiple-monitor setup.  If false, the user will only be
     * prompted once.
     */
    public void doFullScreen(boolean force) {
        setFullScreenMode(fullScreen == null, force);
    }

    public void doZoom(double factor) {
    }

    /**
     * Goes to the next page
     */
    public void doNext() {
        gotoPage(curpage + 1);
        repaint();
    }

    /**
     * Goes to the previous page
     */
    public void doPrev() {
        gotoPage(curpage - 1);
    }

    /**
     * Goes to the first page
     */
    public void doFirst() {
        gotoPage(0);
    }

    /**
     * Goes to the last page
     */
    public void doLast() {
        gotoPage(curFile.getNumPages() - 1);
    }

    /**
     * Goes to the page that was typed in the page number text field
     */
    public void doPageTyped() {
        int pagenum = -1;
        try {
            pagenum = Integer.parseInt(pageField.getText()) - 1;
        } catch (NumberFormatException nfe) {
        }
        if (pagenum >= curFile.getNumPages()) {
            pagenum = curFile.getNumPages() - 1;
        }
        if (pagenum >= 0) {
            if (pagenum != curpage) {
                gotoPage(pagenum);
            }
        } else {
            pageField.setText(String.valueOf(curpage));
        }
    }

    /**
     * Runs the FullScreenMode change in another thread
     */
    class PerformFullScreenMode implements Runnable {

        boolean force;

        public PerformFullScreenMode(boolean forcechoice) {
            force = forcechoice;
        }

        public void run() {
            fspp = new PagePanel();
            fspp.setBackground(Color.black);
            page.showPage(null);
            fullScreen = new FullScreenWindow(fspp, force);
            fspp.addKeyListener(PDFDisplayFrame.this);
            gotoPage(curpage);
            fullScreenAction.setEnabled(true);
        }
    }

    /**
     * Starts or ends full screen mode.
     * @param full true to enter full screen mode, false to leave
     * @param force true if the user should be prompted for a screen
     * to use the second time full screen mode is entered.
     */
    public void setFullScreenMode(boolean full, boolean force) {
        if (full && fullScreen == null) {
            fullScreenAction.setEnabled(false);
            new Thread(new PerformFullScreenMode(force)).start();
            fullScreenButton.setSelected(true);
        } else if (!full && fullScreen != null) {
            fullScreen.close();
            fspp = null;
            fullScreen = null;
            gotoPage(curpage);
            fullScreenButton.setSelected(false);
        }
    }

    public static void main(String args[]) {
        String fileName = null;
        boolean useThumbs = true;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-noThumb")) {
                useThumbs = false;
            } else if (args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("-?")) {
                System.out.println("java com.sun.awc.PDFViewer [flags] [file]");
                System.out.println("flags: [-noThumb] [-help or -h or -?]");
                System.exit(0);
            } else {
                fileName = args[i];
            }
        }
        PDFDisplayFrame viewer;
        viewer = new PDFDisplayFrame(useThumbs);
        if (fileName != null) {
            viewer.doOpen(fileName);
        }
    }

    /**
     * Handle a key press for navigation
     */
    public void keyPressed(KeyEvent evt) {
        int code = evt.getKeyCode();
        if (code == KeyEvent.VK_LEFT) {
            doPrev();
        } else if (code == KeyEvent.VK_RIGHT) {
            doNext();
        } else if (code == KeyEvent.VK_UP) {
            doPrev();
        } else if (code == KeyEvent.VK_DOWN) {
            doNext();
        } else if (code == KeyEvent.VK_HOME) {
            doFirst();
        } else if (code == KeyEvent.VK_END) {
            doLast();
        } else if (code == KeyEvent.VK_PAGE_UP) {
            doPrev();
        } else if (code == KeyEvent.VK_PAGE_DOWN) {
            doNext();
        } else if (code == KeyEvent.VK_SPACE) {
            doNext();
        } else if (code == KeyEvent.VK_ESCAPE) {
            setFullScreenMode(false, false);
        }
    }

    /**
     * Combines numeric key presses to build a multi-digit page number.
     */
    class PageBuilder implements Runnable {

        int value = 0;

        long timeout;

        Thread anim;

        static final long TIMEOUT = 500;

        /** add the digit to the page number and start the timeout thread */
        public synchronized void keyTyped(int keyval) {
            value = value * 10 + keyval;
            timeout = System.currentTimeMillis() + TIMEOUT;
            if (anim == null) {
                anim = new Thread(this);
                anim.start();
            }
        }

        /**
         * waits for the timeout, and if time expires, go to the specified
         * page number
         */
        public void run() {
            long now, then;
            synchronized (this) {
                now = System.currentTimeMillis();
                then = timeout;
            }
            while (now < then) {
                try {
                    Thread.sleep(timeout - now);
                } catch (InterruptedException ie) {
                }
                synchronized (this) {
                    now = System.currentTimeMillis();
                    then = timeout;
                }
            }
            synchronized (this) {
                gotoPage(value - 1);
                anim = null;
                value = 0;
            }
        }
    }

    PageBuilder pb = new PageBuilder();

    public void keyReleased(KeyEvent evt) {
    }

    /**
     * gets key presses and tries to build a page if they're numeric
     */
    public void keyTyped(KeyEvent evt) {
        char key = evt.getKeyChar();
        if (key >= '0' && key <= '9') {
            int val = key - '0';
            pb.keyTyped(val);
        }
    }

    /**
     * Someone changed the selection of the outline tree.  Go to the new
     * page.
     */
    public void valueChanged(TreeSelectionEvent e) {
        if (e.isAddedPath()) {
            OutlineNode node = (OutlineNode) e.getPath().getLastPathComponent();
            if (node == null) {
                return;
            }
            try {
                PDFAction action = node.getAction();
                if (action == null) {
                    return;
                }
                if (action instanceof GoToAction) {
                    PDFDestination dest = ((GoToAction) action).getDestination();
                    if (dest == null) {
                        return;
                    }
                    PDFObject page = dest.getPage();
                    if (page == null) {
                        return;
                    }
                    int pageNum = curFile.getPageNumber(page);
                    if (pageNum >= 0) {
                        gotoPage(pageNum);
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
