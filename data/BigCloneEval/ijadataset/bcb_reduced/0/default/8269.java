import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.*;

public class ErrorList extends JFrame implements EBComponent {

    public static final ImageIcon ERROR_ICON = new ImageIcon(ErrorList.class.getResource("TrafficRed.gif"));

    public static final ImageIcon WARNING_ICON = new ImageIcon(ErrorList.class.getResource("TrafficYellow.gif"));

    public ErrorList(View view) {
        super(jEdit.getProperty("error-list.title"));
        this.view = view;
        getContentPane().add(BorderLayout.NORTH, status = new JLabel());
        getContentPane().add(BorderLayout.CENTER, createListScroller());
        updateStatus();
        EditBus.addToBus(this);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowHandler());
        pack();
        GUIUtilities.loadGeometry(this, "error-list");
        show();
    }

    public void handleMessage(EBMessage message) {
        if (message instanceof ErrorSourceUpdate) handleErrorSourceMessage((ErrorSourceUpdate) message); else if (message instanceof ViewUpdate) handleViewMessage((ViewUpdate) message);
    }

    public void close() {
        EditBus.removeFromBus(this);
        ErrorListPlugin.closeErrorList(view);
        GUIUtilities.saveGeometry(this, "error-list");
        dispose();
    }

    private View view;

    private JLabel status;

    private DefaultListModel errorModel;

    private JList errorList;

    private void updateStatus() {
        int warningCount = 0;
        int errorCount = 0;
        for (int i = 0; i < errorModel.getSize(); i++) {
            ErrorSource.Error error = (ErrorSource.Error) errorModel.getElementAt(i);
            if (error.getErrorType() == ErrorSource.ERROR) errorCount++; else warningCount++;
        }
        Integer[] args = { new Integer(errorCount), new Integer(warningCount) };
        status.setText(jEdit.getProperty("error-list.status", args));
    }

    private void handleErrorSourceMessage(ErrorSourceUpdate message) {
        Object what = message.getWhat();
        if (what == ErrorSourceUpdate.ERROR_ADDED) {
            errorModel.addElement(message.getError());
            updateStatus();
        } else if (what == ErrorSourceUpdate.ERROR_REMOVED) {
            errorModel.removeElement(message.getError());
            updateStatus();
        } else if (what == ErrorSourceUpdate.ERRORS_CLEARED) {
            ErrorSource source = message.getErrorSource();
            for (int i = errorModel.getSize() - 1; i >= 0; i--) {
                if (((ErrorSource.Error) errorModel.getElementAt(i)).getErrorSource() == source) errorModel.removeElementAt(i);
            }
            updateStatus();
        }
    }

    private void handleViewMessage(ViewUpdate message) {
        if (message.getWhat() == ViewUpdate.CLOSED) {
            if (message.getView() == view) view = null;
        }
    }

    private JScrollPane createListScroller() {
        Vector errorVector = new Vector();
        Object[] sources = EditBus.getNamedList(ErrorSource.ERROR_SOURCES_LIST);
        if (sources != null) {
            for (int i = 0; i < sources.length; i++) {
                ErrorSource source = (ErrorSource) sources[i];
                ErrorSource.Error[] errors = source.getAllErrors();
                if (errors == null) continue;
                for (int j = 0; j < errors.length; j++) {
                    errorVector.addElement(errors[j]);
                }
            }
        }
        MiscUtilities.quicksort(errorVector, new ErrorCompare());
        errorModel = new DefaultListModel();
        for (int i = 0; i < errorVector.size(); i++) {
            errorModel.addElement(errorVector.elementAt(i));
        }
        errorList = new JList(errorModel);
        errorList.setCellRenderer(new ErrorCellRenderer());
        errorList.addListSelectionListener(new ListHandler());
        JScrollPane scroller = new JScrollPane(errorList);
        scroller.setPreferredSize(new Dimension(640, 300));
        return scroller;
    }

    class ErrorCompare implements MiscUtilities.Compare {

        public int compare(Object obj1, Object obj2) {
            ErrorSource.Error err1 = (ErrorSource.Error) obj1;
            ErrorSource.Error err2 = (ErrorSource.Error) obj2;
            String path1 = err1.getFilePath();
            String path2 = err2.getFilePath();
            int comp = path1.compareTo(path2);
            if (comp != 0) return comp;
            int line1 = err1.getLineNumber();
            int line2 = err2.getLineNumber();
            return line1 - line2;
        }
    }

    class ErrorCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            ErrorSource.Error error = (ErrorSource.Error) value;
            super.getListCellRendererComponent(list, error, index, isSelected, cellHasFocus);
            setIcon(error.getErrorType() == ErrorSource.WARNING ? WARNING_ICON : ERROR_ICON);
            return this;
        }
    }

    class ListHandler implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent evt) {
            if (evt.getValueIsAdjusting()) return;
            final ErrorSource.Error error = (ErrorSource.Error) errorList.getSelectedValue();
            final Buffer buffer;
            if (error.getBuffer() != null) buffer = error.getBuffer(); else {
                buffer = jEdit.openFile(view, null, error.getFilePath(), false, false, false);
            }
            view.toFront();
            view.requestFocus();
            Runnable r = new Runnable() {

                public void run() {
                    view.setBuffer(buffer);
                    int start = error.getStartOffset();
                    int end = error.getEndOffset();
                    int lineNo = error.getLineNumber();
                    Element line = buffer.getDefaultRootElement().getElement(lineNo);
                    if (line != null) {
                        start += line.getStartOffset();
                        if (end == 0) end = line.getEndOffset() - 1; else end += line.getStartOffset();
                    }
                    view.getTextArea().select(start, end);
                }
            };
            try {
                Class.forName("org.gjt.sp.jedit.io.VFSManager").getMethod("runInAWTThread", new Class[] { Runnable.class }).invoke(null, new Object[] { r });
            } catch (Exception e) {
                r.run();
            }
        }
    }

    class WindowHandler extends WindowAdapter {

        public void windowClosing(WindowEvent evt) {
            close();
        }
    }
}
