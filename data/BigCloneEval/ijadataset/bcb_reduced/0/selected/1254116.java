package cx.fbn.nevernote.gui;

import java.awt.Desktop;
import java.util.List;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.core.Qt.WidgetAttribute;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QDesktopServices;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QMdiSubWindow;
import com.trolltech.qt.gui.QPrintDialog;
import com.trolltech.qt.gui.QPrinter;
import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.dialog.FindDialog;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class ExternalBrowse extends QMdiSubWindow {

    private final DatabaseConnection conn;

    private final BrowserWindow browser;

    public Signal4<String, String, Boolean, BrowserWindow> contentsChanged;

    public Signal1<String> windowClosing;

    boolean noteDirty;

    String saveTitle;

    private final FindDialog find;

    ExternalBrowserMenuBar menu;

    public ExternalBrowse(DatabaseConnection c) {
        setAttribute(WidgetAttribute.WA_QuitOnClose, false);
        setWindowTitle(tr("NixNote"));
        conn = c;
        contentsChanged = new Signal4<String, String, Boolean, BrowserWindow>();
        windowClosing = new Signal1<String>();
        browser = new BrowserWindow(conn);
        menu = new ExternalBrowserMenuBar(this);
        for (int i = 0; i < menu.actions().size(); i++) {
            addAction(menu.actions().get(i));
        }
        setWidget(browser);
        noteDirty = false;
        browser.titleLabel.textChanged.connect(this, "titleChanged(String)");
        browser.getBrowser().page().contentsChanged.connect(this, "contentChanged()");
        find = new FindDialog();
        find.getOkButton().clicked.connect(this, "doFindText()");
    }

    @SuppressWarnings("unused")
    private void contentChanged() {
        noteDirty = true;
        contentsChanged.emit(getBrowserWindow().getNote().getGuid(), getBrowserWindow().getContent(), false, getBrowserWindow());
    }

    @Override
    public void closeEvent(QCloseEvent event) {
        if (noteDirty) contentsChanged.emit(getBrowserWindow().getNote().getGuid(), getBrowserWindow().getContent(), true, getBrowserWindow());
        windowClosing.emit(getBrowserWindow().getNote().getGuid());
    }

    public BrowserWindow getBrowserWindow() {
        return browser;
    }

    @SuppressWarnings("unused")
    private void titleChanged(String value) {
        setWindowTitle(tr("NixNote - ") + value);
    }

    @SuppressWarnings("unused")
    private void updateTitle(String guid, String title) {
        if (guid.equals(getBrowserWindow().getNote().getGuid()) && (saveTitle != null && !title.equals(saveTitle) || saveTitle == null)) {
            saveTitle = title;
            getBrowserWindow().loadingData(true);
            getBrowserWindow().setTitle(title);
            getBrowserWindow().getNote().setTitle(title);
            getBrowserWindow().loadingData(false);
        }
    }

    @SuppressWarnings("unused")
    private void updateNotebook(String guid, String notebook) {
        if (guid.equals(getBrowserWindow().getNote().getGuid())) {
            getBrowserWindow().loadingData(true);
            getBrowserWindow().setNotebook(notebook);
            getBrowserWindow().loadingData(false);
        }
    }

    @SuppressWarnings("unused")
    private void updateTags(String guid, List<String> tags) {
        if (guid.equals(getBrowserWindow().getNote().getGuid())) {
            StringBuffer tagLine = new StringBuffer(100);
            for (int i = 0; i < tags.size(); i++) {
                if (i > 0) tagLine.append(Global.tagDelimeter + " ");
                tagLine.append(tags.get(i));
            }
            getBrowserWindow().loadingData(true);
            getBrowserWindow().getTagLine().setText(tagLine.toString());
            getBrowserWindow().loadingData(false);
        }
    }

    @SuppressWarnings("unused")
    private void findText() {
        find.show();
        find.setFocusOnTextField();
    }

    @SuppressWarnings("unused")
    private void doFindText() {
        browser.getBrowser().page().findText(find.getText(), find.getFlags());
        find.setFocus();
    }

    @SuppressWarnings("unused")
    private void printNote() {
        QPrintDialog dialog = new QPrintDialog();
        if (dialog.exec() == QDialog.DialogCode.Accepted.value()) {
            QPrinter printer = dialog.printer();
            browser.getBrowser().print(printer);
        }
    }

    @SuppressWarnings("unused")
    private void emailNote() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            String text2 = browser.getContentsToEmail();
            QUrl url = new QUrl("mailto:");
            url.addQueryItem("subject", browser.getTitle());
            url.addQueryItem("body", text2);
            QDesktopServices.openUrl(url);
        }
    }
}
