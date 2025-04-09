package vademecum.protocol;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.TextAction;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import vademecum.core.experiment.ExperimentNode;
import vademecum.protocol.ProtModel.ProtModelCom;
import vademecum.protocol.comment.CBEvent;
import vademecum.protocol.comment.CBListener;
import vademecum.protocol.comment.CommentPane;
import vademecum.protocol.comment.StyledChar;
import vademecum.protocol.comment.TextActions;
import vademecum.protocol.comment.UnredoQueue;

/**
 * This class manages most of ProtModels Listener Stuff TIt was generated to
 * move code from the ProtModel Class, which was much to big
 */
public class ProtModelListeners {

    private final ProtModel pm;

    private final IInnerProtocolChangeListener ipcl;

    private static final Log log = LogFactory.getLog(ProtModel.class);

    private LinkedList<StyledChar> clipboard = null;

    private Clipboard systemclipboard = null;

    private UnredoQueue undoable = new UnredoQueue();

    private UnredoQueue redoable = new UnredoQueue();

    LinkedList<ISelectListener> il;

    /**
	 * Create a new ProtModelListener
	 * @param pm The ProtModel this Listener shall listen to
	 * @param ipcl An InnerChangeListener to listen to changes of the protocol
	 */
    public ProtModelListeners(ProtModel pm, IInnerProtocolChangeListener ipcl) {
        systemclipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        systemclipboard.addFlavorListener(getFlavorListener());
        this.pm = pm;
        this.ipcl = ipcl;
        clipboard = new LinkedList<StyledChar>();
    }

    /**
	 * Takes text from the Clipboard. Need an extra check to ensure no
	 * overriding of internal copy caused by external copy. See
	 * {@link CommentPane#copy(int, int)} for more Information
	 * 
	 * @return
	 */
    FlavorListener getFlavorListener() {
        return new FlavorListener() {

            public void flavorsChanged(FlavorEvent e) {
                try {
                    Transferable transferData = systemclipboard.getContents(null);
                    for (DataFlavor dataFlavor : transferData.getTransferDataFlavors()) {
                        try {
                            Object content = transferData.getTransferData(dataFlavor);
                            if (content instanceof String) {
                                String c = (String) content;
                                LinkedList<StyledChar> sty = new LinkedList<StyledChar>();
                                boolean test = true;
                                Iterator<StyledChar> it = null;
                                if (clipboard != null && clipboard.size() != c.length()) {
                                    test = false;
                                } else {
                                    if (clipboard != null) {
                                        it = clipboard.iterator();
                                    }
                                }
                                for (char ch : c.toCharArray()) {
                                    if (test) {
                                        char clip = it.next().getCharacter();
                                        if (ch != clip) {
                                            test = false;
                                        }
                                    }
                                    sty.add(new StyledChar(ch, new SimpleAttributeSet(), new SimpleAttributeSet()));
                                }
                                if (!test) {
                                    clipboard = sty;
                                }
                                break;
                            }
                        } catch (UnsupportedFlavorException e1) {
                        } catch (IOException e1) {
                        }
                    }
                } catch (Exception es) {
                }
            }
        };
    }

    /**
	 * This method generates a basic FocusListener to update hasfocus. hasfocus.
	 * Only used for CommentPanes.
	 * 
	 * @return The FocusListener for a Commentpane
	 */
    FocusListener getCommentFocusListener() {
        return new FocusListener() {

            public void focusGained(FocusEvent arg0) {
                pm.hasfocus = (JComponent) arg0.getComponent();
                pm.ul.commentActivation((JTextPane) pm.hasfocus, getCommentBarListener());
            }

            public void focusLost(FocusEvent arg0) {
                if (!arg0.isTemporary()) {
                    pm.ul.commentDeactivation();
                }
            }

            ;
        };
    }

    /**
	 * This method return a CommentBar Listener for CommentPanes.
	 * 
	 * @return A CommentBarListener for CommentPanes
	 */
    CBListener getCommentBarListener() {
        return new CBListener() {

            public void copy(CBEvent ev) {
                log.debug("copy started");
                clipboard = TextActions.getClipboard(ev.getTa());
            }

            public void cut(CBEvent ev) {
                log.debug("cut started");
                clipboard = TextActions.getClipboard(ev.getTa());
                ((MyUndoableEditListener) ((DefaultStyledDocument) ev.getSrc().getDocument()).getUndoableEditListeners()[0]).sendCloseEvent();
            }

            public void paste(CBEvent ev) {
                log.debug("paste started");
                MyUndoableEditListener muel = (MyUndoableEditListener) ((DefaultStyledDocument) ev.getSrc().getDocument()).getUndoableEditListeners()[0];
                if (clipboard != null) {
                    muel.sendOpenEvent();
                    TextAction ta = ev.getTa();
                    TextActions.setClipboard(ta, clipboard);
                    ta.actionPerformed(new ActionEvent(ev.getSrc(), 0, ""));
                    muel.sendCloseEvent();
                }
            }

            public void doRedo() {
                if (redoable.size() > 0) {
                    redoable.peek().redo();
                    undoable.offer(redoable.poll());
                }
            }

            public void doUndo() {
                if (undoable.size() > 0) {
                    undoable.peek().undo();
                    redoable.offer(undoable.poll());
                }
            }

            public void sendCutEvent(CBEvent ev) {
                ((MyUndoableEditListener) ((DefaultStyledDocument) ev.getSrc().getDocument()).getUndoableEditListeners()[0]).sendOpenEvent();
            }
        };
    }

    /**
	 * this method just returns an instance of the inner class
	 * MyUndoableEditListener
	 * 
	 * @return told you
	 */
    UndoableEditListener getUndoableEditListener() {
        return new MyUndoableEditListener();
    }

    /**
	 * This method returns a CaretListener,which takes the given Caret,
	 * calculates its left and rightend, gets all the Attributs set in this
	 * range to finally set the Buttons of the related commentbar.
	 * 
	 * @return
	 */
    CaretListener getCaretListener() {
        return new CaretListener() {

            public void caretUpdate(CaretEvent e) {
                int leftend = Math.min(e.getDot(), e.getMark());
                int rightend = Math.max(e.getDot(), e.getMark());
                CommentPane cp = (CommentPane) e.getSource();
                pm.ul.setButtons(cp.getAttributes(leftend, rightend));
            }
        };
    }

    /**
	 * This method returns a FocusListener to primary handle the changes of the
	 * namepart
	 * 
	 * @return The specified FocusListener
	 */
    FocusListener getOrderListener() {
        return new FocusListener() {

            String text = "";

            public void focusGained(FocusEvent arg0) {
                pm.hasfocus = (JComponent) (arg0.getComponent().getParent());
                JTextComponent tc = (JTextComponent) arg0.getSource();
                if (tc.isEditable()) {
                    text = tc.getText();
                }
            }

            public void focusLost(FocusEvent arg0) {
                JTextComponent tc = (JTextComponent) arg0.getSource();
                if (tc.isEditable()) {
                    if (text != tc.getText()) {
                        ipcl.changedInner();
                        String num = ((JTextComponent) (JComponent) tc.getParent().getComponent(0)).getText();
                        pm.ul.changeOrderName((JComponent) tc.getParent(), num + tc.getText());
                        text = "";
                    }
                }
            }
        };
    }

    /**
	 * This Method returns a Listener to handle the problem with the
	 * Mousewheellistener
	 * 
	 * @param jc
	 *            The ResultArea this Listener refers to
	 * @return The specified MouseListener
	 */
    MouseListener getResAreaMouseListener(final JComponent jc) {
        class ResPaneMouseListener extends MouseAdapter {

            private MouseWheelListener mwl;

            public ResPaneMouseListener() {
                this.mwl = jc.getMouseWheelListeners()[0];
                jc.removeMouseWheelListener(jc.getMouseWheelListeners()[0]);
            }

            public void mouseClicked(MouseEvent e) {
                JScrollPane src = (JScrollPane) e.getComponent();
                src.addMouseWheelListener(mwl);
            }

            public void mouseExited(MouseEvent e) {
                JScrollPane src = (JScrollPane) e.getComponent();
                if (src.getMouseWheelListeners().length > 0) {
                    src.removeMouseWheelListener(src.getMouseWheelListeners()[0]);
                }
            }
        }
        return new ResPaneMouseListener();
    }

    /**
	 * This method returns a Listener to handle the Selection of noncomment
	 * Components in the Protocol. This method reports both to the
	 * DataNavigation and the Outline
	 * 
	 * @return The specified MouseListener
	 */
    MouseListener getSelectionMouseListener() {
        class SimpleMouseListener extends MouseAdapter {

            public void mouseClicked(MouseEvent e) {
                if (e.getComponent().getParent() instanceof JComponent) {
                    pm.hasfocus = (JComponent) e.getComponent().getParent();
                    ProtModelCom pc = pm.findProtComponent();
                    ExperimentNode en = null;
                    switch(pc.pc.getType()) {
                        case ProtCompConst.RESULTPANE:
                            en = ((ResultPane) pc.pc).en;
                            break;
                        case ProtCompConst.PROGRESS_PANE:
                            en = ((ProgressPane) pc.pc).en;
                            break;
                        default:
                            return;
                    }
                    SelectEvent ev = new SelectEvent(en);
                    if (il != null && il.size() != 0) {
                        for (ISelectListener ils : il) {
                            ils.selected(ev);
                        }
                    }
                    pm.ul.callFocusInOutline(pc.pc);
                }
            }
        }
        return new SimpleMouseListener();
    }

    /**
	 * This method returns a Listener for Right- and DoubleClickActions on the
	 * Protcol
	 * 
	 * @return The specified MouseListener
	 */
    MouseListener getActionML() {
        class GeneralML extends MouseAdapter {

            long currenttime = 0;

            boolean set = false;

            public void mouseClicked(MouseEvent e) {
                long act = System.currentTimeMillis();
                JPanel maybe = null;
                if (((JComponent) e.getSource()) instanceof JPanel) {
                    maybe = (JPanel) e.getSource();
                    e.setSource(((JComponent) e.getSource()).getParent().getParent().getParent());
                }
                if (((JComponent) e.getSource()).getParent() instanceof JComponent) {
                    pm.hasfocus = (JComponent) ((JComponent) e.getSource()).getParent();
                } else {
                    pm.hasfocus = ((JComponent) e.getSource());
                }
                if (set) {
                    if (act - currenttime <= 700 && maybe != null) {
                        ProtModelCom pmc = pm.findProtComponent();
                        ResultPane rp = (ResultPane) pmc.pc;
                        JMenuItem[] items = rp.menuItems.get(maybe);
                        if (items != null) {
                            for (JMenuItem jm : items) {
                                for (ActionListener al : jm.getActionListeners()) {
                                    al.actionPerformed(new ActionEvent(e.getSource(), 0, ""));
                                }
                            }
                        }
                    }
                    set = false;
                } else {
                    currenttime = System.currentTimeMillis();
                    set = true;
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    pm.createPopUpMenu(maybe).show(((JComponent) e.getSource()), e.getX(), e.getY());
                }
            }
        }
        return new GeneralML();
    }

    /**
	 * This class implements my own UndoableEditListener to handle undo and redo
	 * between the commentpanes. This implementation is extended to handle the
	 * cases: cut paste and the insertion of complex elements like resultpanes
	 * where no undo/redo is allowed
	 */
    class MyUndoableEditListener implements UndoableEditListener {

        private CompoundEdit ce = null;

        public void undoableEditHappened(UndoableEditEvent arg0) {
            UndoableEdit ue = arg0.getEdit();
            if (!pm.insertcominprogress) {
                ipcl.changedInner();
                if (ce != null) {
                    ce.addEdit(ue);
                } else {
                    undoable.offer(ue);
                }
            }
        }

        public void sendOpenEvent() {
            this.ce = new CompoundEdit();
        }

        public void sendCloseEvent() {
            this.ce.end();
            undoable.offer(this.ce);
            this.ce = null;
        }
    }
}
