package watij.runtime.ie;

import com.jniwrapper.*;
import com.jniwrapper.win32.automation.OleMessageLoop;
import com.jniwrapper.win32.automation.types.Variant;
import com.jniwrapper.win32.automation.types.VariantBool;
import com.jniwrapper.win32.com.IUnknown;
import com.jniwrapper.win32.com.impl.IUnknownImpl;
import com.jniwrapper.win32.ie.*;
import com.jniwrapper.win32.ie.command.BrowserCommand;
import com.jniwrapper.win32.ie.dom.Cookie;
import com.jniwrapper.win32.ie.dom.DomFactory;
import com.jniwrapper.win32.ie.dom.HTMLDocument;
import com.jniwrapper.win32.ie.dom.HTMLElement;
import com.jniwrapper.win32.ie.event.*;
import com.jniwrapper.win32.mshtml.IHTMLDocument2;
import com.jniwrapper.win32.mshtml.impl.IHTMLDocument2Impl;
import com.jniwrapper.win32.shdocvw.IWebBrowser2;
import com.jniwrapper.win32.ui.Wnd;
import com.jniwrapper.win32.registry.RegistryKey;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import watij.WatijBrowser;
import watij.dialogs.*;
import watij.elements.HtmlElement;
import watij.finders.Symbol;
import watij.finders.TitleFinder;
import watij.finders.UrlFinder;
import watij.runtime.BrowserControllerException;
import watij.runtime.MissingWayOfFindingObjectException;
import watij.runtime.NotImplementedYetException;
import watij.time.Ready;
import watij.time.Waiter;
import watij.time.WaiterImpl;
import watij.utilities.Debug;
import watij.utilities.StringUtils;
import watij.utilities.WatijResourceLoader;
import watij.utilities.WatijResources;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IE extends IEContainer implements WatijBrowser {

    WebBrowser webBrowser;

    String additionalHttpHeaders;

    List<IE> childBrowsers;

    IE parentIE;

    IEEventHandler ieEventHandler;

    long hwnd;

    protected static final int TIMEOUT_NAVIGATE = 180000;

    protected static final int TIMEOUT_CHILDBROWSER = 60000;

    protected static final int TIMEOUT_WAIT_CLOSED = 10000;

    protected static final int TIMEOUT_BROWSER = 180000;

    protected static final int TIMEOUT_DOCUMENT = 180000;

    public static final String TITLE = WatijResourceLoader.getString(WatijResources.IE_Title, majorVersion());

    private static String IEMajorVersion;

    private HTMLDocument cachedHtmlDocument;

    public IE() {
    }

    protected IE(WebBrowser webBrowser, boolean init) throws Exception {
        this.webBrowser = webBrowser;
        if (init) {
            init();
        }
    }

    protected IE(WebBrowser webBrowser) throws Exception {
        this(webBrowser, false);
    }

    protected IE(WebBrowser webBrowser, IE parentIE) throws Exception {
        this(webBrowser);
        init();
        this.parentIE = parentIE;
        parentIE.childBrowsers.add(this);
    }

    public static String majorVersion() {
        if (IEMajorVersion == null) {
            IEMajorVersion = ((String) RegistryKey.LOCAL_MACHINE.openSubKey("SOFTWARE\\Microsoft\\Internet Explorer").values().get("Version")).substring(0, 1);
        }
        return IEMajorVersion;
    }

    public void attach(Symbol how, String what) throws Exception {
        if (how instanceof UrlFinder) {
            webBrowser = IEUtil.attach(what, true);
        } else if (how instanceof TitleFinder) {
            webBrowser = IEUtil.attach(what, false);
        } else {
            throw new MissingWayOfFindingObjectException();
        }
        init();
    }

    public void start() throws Exception {
        webBrowser = new IEAutomation();
        init();
    }

    private void init() throws Exception {
        hwnd = iWebBrowser2().getHWND().getValue();
        childBrowsers = new ArrayList<IE>();
        setupEventHandler();
        visible(true);
        waitUntilExists();
    }

    protected void setupEventHandler() {
        ieEventHandler = new IEEventHandler(this);
        if (isIEAutomationClass()) {
            ((IEAutomation) webBrowser).addIEApplicationEventListener(ieEventHandler);
        }
        webBrowser.setEventHandler(ieEventHandler);
        webBrowser.addNewWindowListener(ieEventHandler);
        webBrowser.addNavigationListener(ieEventHandler);
    }

    public IWebBrowser2 iWebBrowser2() throws Exception {
        return (IWebBrowser2) webBrowser.getBrowserPeer();
    }

    protected HTMLDocument htmlDocument() throws Exception {
        webBrowser.waitReady();
        if (cachedHtmlDocument == null) {
            cachedHtmlDocument = webBrowser.getDocument();
        }
        return cachedHtmlDocument;
    }

    protected IE ie() {
        return this;
    }

    protected OleMessageLoop oleMessageLoop() {
        return webBrowser.getOleMessageLoop();
    }

    public AlertDialog alertDialog() throws Exception {
        bringToFront();
        return IEAlertDialog.findAlertDialog(this);
    }

    public ConfirmDialog confirmDialog() throws Exception {
        bringToFront();
        return IEConfirmDialog.findConfirmDialog(this);
    }

    public PromptDialog promptDialog() throws Exception {
        bringToFront();
        return IEPromptDialog.findPromptDialog(this);
    }

    public FileDownloadDialog fileDownloadDialog() throws Exception {
        bringToFront();
        return IEFileDownloadDialog.findFileDownloadDialog(this);
    }

    public FileDownloadDialog fileDownloadDialog(final String url) throws Exception {
        bringToFront();
        new Thread(new Runnable() {

            public void run() {
                try {
                    goTo(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return IEFileDownloadDialog.findFileDownloadDialog(this);
    }

    public String text() throws Exception {
        HTMLElement body = htmlDocument().getBody();
        return body.getText();
    }

    public void refresh() throws Exception {
        webBrowser.refresh();
    }

    public void minimize() throws Exception {
        Wnd ieWindow = getBrowserWindow();
        ieWindow.show(Wnd.ShowWindowCommand.SHOWMINIMIZED);
    }

    protected Wnd getBrowserWindow() throws Exception {
        return new Wnd(hwnd);
    }

    public void maximize() throws Exception {
        Wnd ieWindow = getBrowserWindow();
        ieWindow.show(Wnd.ShowWindowCommand.SHOWMAXIMIZED);
    }

    public void restore() throws Exception {
        Wnd ieWindow = getBrowserWindow();
        ieWindow.show(Wnd.ShowWindowCommand.RESTORE);
    }

    public class Counter {

        int i = 0;

        public void add(int i) {
            this.i += i;
        }

        public int getCount() {
            return i;
        }
    }

    public void navigate(String url) throws Exception {
        ieEventHandler.trackNavigationCompleted();
        try {
            webBrowser.navigate(url);
            new WaiterImpl(TIMEOUT_NAVIGATE, 200).waitUntil(new Ready() {

                public boolean isReady() throws Exception {
                    Debug.getInstance().println("navigating");
                    return ieEventHandler.isNavigationCompleted();
                }

                public String getNotReadyReason() {
                    return "Navigation never completed";
                }
            });
        } catch (Throwable t) {
            Debug.handleException(t);
        }
    }

    public void bringToFront() throws Exception {
        IEUtil.switchToThisWindow(getBrowserWindow());
    }

    public void sendKeys(String title, String keys, boolean blocking) throws Exception {
        bringToFront();
        IEUtil.sendKeys(title, keys, blocking, oleMessageLoop());
    }

    public void sendKeys(String title, String keys) throws Exception {
        sendKeys(title, keys, true);
    }

    public void sendKeys(String keys) throws Exception {
        sendKeys(keys, true);
    }

    public void sendKeys(String keys, boolean blocking) throws Exception {
        sendKeys(title(), keys, blocking);
    }

    public Object executeScript(final String script) throws Exception {
        waitUntilReady();
        Object o = webBrowser.executeScript(script);
        return o;
    }

    public void setAdditionalHttpHeaders(String additionalHttpHeaders) {
        this.additionalHttpHeaders = additionalHttpHeaders;
    }

    public String getAdditionalHttpHeaders() {
        return additionalHttpHeaders;
    }

    public String html() throws Exception {
        Debug.getInstance().println("Begin IE.html()");
        waitUntilReady();
        String content = webBrowser.getContent();
        Debug.getInstance().println("End IE.html()");
        return content;
    }

    private void waitUntilExists() throws Exception {
        new WaiterImpl(TIMEOUT_BROWSER, 200).waitUntil(new Ready() {

            public boolean isReady() throws Exception {
                return exists();
            }

            public String getNotReadyReason() {
                return "Browser doesn't exist";
            }
        });
    }

    public void waitUntilReady() throws Exception {
        Debug.getInstance().println("Begin IE.waitUntilReady()");
        doWaitUntilReady(TIMEOUT_BROWSER);
        Debug.getInstance().println("End IE.waitUntilReady()");
    }

    public void waitUntilReady(int seconds) throws Exception {
        Debug.getInstance().println("Begin IE.waitUntilReady(int seconds)");
        doWaitUntilReady(seconds * 1000);
        Debug.getInstance().println("End IE.waitUntilReady(int seconds)");
    }

    private void doWaitUntilReady(long timeMillis) throws Exception {
        Debug.getInstance().println("Begin IE.doWaitUntilReady(long timeMillis)");
        webBrowser.waitReady(timeMillis);
        Debug.getInstance().println("End IE.doWaitUntilReady(long timeMillis)");
    }

    protected void waitUntilDocumentInitializedAndComplete() throws Exception {
        Debug.getInstance().println("Begin IE.waitUntilDocumentInitializedAndComplete()");
        new WaiterImpl(TIMEOUT_DOCUMENT, 200).waitUntil(new Ready() {

            public boolean isReady() throws Exception {
                boolean notNull = webBrowser.getDocument() != null;
                debug("Document is null = " + (!notNull));
                return notNull;
            }

            public String getNotReadyReason() {
                return "Document is null";
            }
        });
        waitUntilDocumentComplete(webBrowser.getDocument(), webBrowser.getOleMessageLoop());
        Debug.getInstance().println("End IE.waitUntilDocumentInitializedAndComplete()");
    }

    protected void waitUntilDocumentComplete(HTMLDocument htmlDocument, OleMessageLoop oleMessageLoop) throws Exception {
        final IHTMLDocument2 ihtmlDocument2 = (IHTMLDocument2) oleMessageLoop.bindObject((IUnknown) htmlDocument.getDocumentPeer());
        IEUtil.waitUntilDocumentComplete(ihtmlDocument2);
    }

    private void checkExists() throws Exception {
        if (!exists()) {
            throw new BrowserControllerException("Browser Does Not Exist");
        }
    }

    protected Document document() throws Exception {
        Debug.getInstance().println("Begin IE.document()");
        Document d = document(html());
        Debug.getInstance().println("End IE.document()");
        return d;
    }

    public Element element() throws Exception {
        Debug.getInstance().println("Begin IE.element()");
        Element e = document().getDocumentElement();
        Debug.getInstance().println("End IE.element()");
        return e;
    }

    public IE childBrowser() throws Exception {
        return childBrowser(0);
    }

    public IE childBrowser(int index) throws Exception {
        waitForChildBrowser(index);
        IE childIE = childBrowsers.get(index);
        return childIE;
    }

    public int childBrowserCount() throws Exception {
        return childBrowsers.size();
    }

    public void waitForChildBrowser(final int i) throws Exception {
        Waiter waiter = new WaiterImpl(TIMEOUT_CHILDBROWSER, 1000);
        waiter.waitUntil(new Ready() {

            public boolean isReady() {
                return childBrowsers.size() > i;
            }

            public String getNotReadyReason() {
                return "ChildBrowser does not exist yet";
            }
        });
    }

    protected void removeSelfFromParent() {
        if (parentIE != null) {
            parentIE.childBrowsers.remove(this);
        }
    }

    public void show() throws Exception {
        htmlElements().show();
    }

    public String toJson() throws Exception {
        return htmlElements().toJson();
    }

    public HtmlElement active() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void start(String url) throws Exception {
        start();
        goTo(url);
    }

    public void addChecker(Object checker) throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void attachInit(Symbol how, String what) throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void back() throws Exception {
        webBrowser.goBack();
    }

    public void checkForHttpError(WatijBrowser watijBrowser) throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void clearUrlList() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public boolean exists() throws Exception {
        return new Wnd(hwnd).isWindow();
    }

    public void waitUntilClosed() throws Exception {
        doWaitUntilClosed(TIMEOUT_WAIT_CLOSED);
    }

    public void waitUntilClosed(int seconds) throws Exception {
        doWaitUntilClosed(seconds * 1000);
    }

    private void doWaitUntilClosed(int timeMillis) throws Exception {
        new WaiterImpl(timeMillis, 200).waitUntil(new Ready() {

            public boolean isReady() throws Exception {
                Debug.getInstance().println("waiting for browser to be gone");
                return !exists();
            }

            public String getNotReadyReason() {
                return "webBrowser never closed.";
            }
        });
        removeSelfFromParent();
    }

    public void close() throws Exception {
        if (exists()) {
            IEUtil.closeAllDialogs();
            try {
                Debug.getInstance().println("About to Close Browser");
                webBrowser.close();
                waitUntilClosed();
            } catch (Throwable t) {
                Debug.handleException(t);
            }
            Thread.sleep(200);
            removeSelfFromParent();
        }
    }

    public boolean containsText(String textOrRegex) throws Exception {
        return StringUtils.matchesOrContains(textOrRegex, text());
    }

    public Object dir() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void disableChecker(Object checker) throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void focus() throws Exception {
        getBrowserWindow().setFocus();
    }

    public void forward() throws Exception {
        webBrowser.goForward();
    }

    public void fullScreen(boolean full) throws Exception {
        iWebBrowser2().setFullScreen(new VariantBool(full));
    }

    public boolean fullScreen() throws Exception {
        return iWebBrowser2().getFullScreen().getBooleanValue();
    }

    public boolean isFront() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void goTo(String url) throws Exception {
        navigate(url);
    }

    public void log(String what) throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void screenCapture(String fullyQualifiedFileName_PngFormat) throws Exception {
        screenCapture(fullyQualifiedFileName_PngFormat, "png");
    }

    public void windowCapture(String fullyQualifiedFileName_PngFormat) throws Exception {
        windowCapture(fullyQualifiedFileName_PngFormat, "png");
    }

    public void screenCapture(String fullyQualifiedFileName, String format) throws Exception {
        bringToFront();
        BufferedImage bufferedImage = takeCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        writeCapture(bufferedImage, format, fullyQualifiedFileName);
    }

    public void windowCapture(String fullyQualifiedFileName, String format) throws Exception {
        bringToFront();
        BufferedImage bufferedImage = takeCapture(new Rectangle(left(), top(), width(), height()));
        writeCapture(bufferedImage, format, fullyQualifiedFileName);
    }

    private void writeCapture(BufferedImage image, String format, String fullyQualifiedFileName) throws Exception {
        ImageIO.write(image, format, new File(fullyQualifiedFileName));
    }

    private BufferedImage takeCapture(Rectangle rec) throws Exception {
        Robot robot = new Robot();
        return robot.createScreenCapture(rec);
    }

    public void left(int left) throws Exception {
        iWebBrowser2().setLeft(new Int32(left));
    }

    public int left() throws Exception {
        return iWebBrowser2().getLeft().toLong().intValue();
    }

    public void top(int top) throws Exception {
        iWebBrowser2().setTop(new Int32(top));
    }

    public int top() throws Exception {
        return iWebBrowser2().getTop().toLong().intValue();
    }

    public void width(int width) throws Exception {
        iWebBrowser2().setWidth(new Int32(width));
    }

    public void height(int height) throws Exception {
        iWebBrowser2().setHeight(new Int32(height));
    }

    public int width() throws Exception {
        return iWebBrowser2().getWidth().toLong().intValue();
    }

    public int height() throws Exception {
        return iWebBrowser2().getHeight().toLong().intValue();
    }

    public void runErrorChecks() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void setFastSpeed() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void setSlowSpeed() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public String status() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public String title() throws Exception {
        return htmlDocument().getTitle();
    }

    public void theatreMode(boolean theatre) throws Exception {
        iWebBrowser2().setTheaterMode(new VariantBool(theatre));
    }

    public boolean theatreMode() throws Exception {
        return iWebBrowser2().getTheaterMode().getBooleanValue();
    }

    public String url() throws Exception {
        waitUntilReady();
        return webBrowser.getLocationURL();
    }

    public List<String> urlList() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void visible(boolean visible) throws Exception {
        if (isIEAutomationClass()) {
            ((IEAutomation) webBrowser).setVisible(visible);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private boolean isIEAutomationClass() {
        return webBrowser instanceof IEAutomation;
    }

    public boolean visible() throws Exception {
        waitUntilReady();
        if (isIEAutomationClass()) {
            return ((IEAutomation) webBrowser).isVisible();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public ModalDialog modalDialog() throws Exception {
        waitUntilReady();
        final OleMessageLoop modalDialogMessageLoop = new OleMessageLoop("modalDialogMessageLoop");
        final ModalDialogFinder modalDialogFinder = new ModalDialogFinder((int) hwnd, modalDialogMessageLoop);
        modalDialogMessageLoop.doStart();
        modalDialogMessageLoop.doInvokeAndWait(modalDialogFinder);
        return new IEModalDialog(modalDialogFinder.getHtmlDocument(), modalDialogMessageLoop, modalDialogFinder.getIE());
    }

    static class ModalDialogFinder implements Runnable {

        Pointer.Void popupDocPointer = new Pointer.Void();

        Int hWndParent = new Int();

        HTMLDocument htmlDocument = null;

        DummyWB dummyWB;

        DummyIE dummyIE;

        OleMessageLoop oleMessageLoop;

        final UInt GW_ENABLEDPOPUP = new UInt(6);

        final UInt GW_CHILD = new UInt(5);

        final UInt GW_HWNDNEXT = new UInt(2);

        final UInt SMTO_ABORTIFHUNG = new UInt(2);

        Int32 hWnd = new Int32(0), hWnd2 = new Int32(0);

        AnsiString className = new AnsiString();

        public ModalDialogFinder(int _hWndParent, OleMessageLoop oleMessageLoop) {
            hWndParent.setValue(_hWndParent);
            this.oleMessageLoop = oleMessageLoop;
        }

        public HTMLDocument getHtmlDocument() {
            return htmlDocument;
        }

        public Wnd getModalWnd() {
            return new Wnd(hWnd.getValue());
        }

        public IE getIE() {
            return dummyIE;
        }

        public void run() {
            try {
                final Library user32 = new Library("user32");
                new WaiterImpl(60000, 200).waitUntil(new Ready() {

                    public boolean isReady() throws Exception {
                        user32.getFunction("GetWindow").invoke(hWnd2, new Int32(hWndParent), GW_ENABLEDPOPUP);
                        return hWnd2.getValue() != 0;
                    }

                    public String getNotReadyReason() {
                        return "Handle to Browser not found";
                    }
                });
                new WaiterImpl(60000, 200).waitUntil(new Ready() {

                    public boolean isReady() throws Exception {
                        user32.getFunction("GetWindow").invoke(hWnd, hWnd2, GW_CHILD);
                        if (hWnd.getValue() != 0) {
                            do {
                                user32.getFunction("GetClassNameA").invoke(null, hWnd, className, new UInt(255));
                                if (className.getValue().compareTo("Internet Explorer_Server") == 0) {
                                    return true;
                                }
                                user32.getFunction("GetWindow").invoke(hWnd, hWnd, GW_HWNDNEXT);
                            } while (hWnd.getValue() != 0);
                        }
                        return false;
                    }

                    public String getNotReadyReason() {
                        return "Handle to Internet Explorer_Server not found";
                    }
                });
                Int lResult = new Int(0);
                Pointer lresPointer = new Pointer(lResult);
                UInt nMsg = new UInt(0);
                user32.getFunction("RegisterWindowMessageA").invoke(nMsg, new AnsiString("WM_HTML_GETOBJECT"));
                Parameter[] params = new Parameter[] { hWnd, nMsg, new UInt(0), new UInt(0), SMTO_ABORTIFHUNG, new UInt(100), lresPointer };
                user32.getFunction("SendMessageTimeoutA").invoke(null, params);
                IHTMLDocument2Impl doc2Impl = new IHTMLDocument2Impl();
                Pointer ppDoc = new Pointer(popupDocPointer);
                Int retVal = new Int(0);
                Function.call("oleacc", "ObjectFromLresult", retVal, lResult, new Pointer(doc2Impl.getIID()), new UInt(0), ppDoc);
                IHTMLDocument2 doc2 = new IHTMLDocument2Impl(new IUnknownImpl(popupDocPointer));
                IEUtil.waitUntilDocumentComplete(doc2);
                dummyWB = new DummyWB(oleMessageLoop);
                dummyIE = new DummyIE();
                htmlDocument = DomFactory.getInstance(dummyWB).createDocument(doc2);
                dummyWB.setDocument(htmlDocument);
                dummyIE.setWebBrowser(dummyWB);
            } catch (Exception e) {
                Debug.handleException(e);
            }
        }
    }

    public static class DummyIE extends IE {

        public void setWebBrowser(WebBrowser webBrowser) {
            this.webBrowser = webBrowser;
        }

        public void waitUntilReady() throws Exception {
        }
    }

    public static class DummyWB implements WebBrowser {

        OleMessageLoop oleMessageLoop;

        HTMLDocument htmlDocument;

        public DummyWB(OleMessageLoop oleMessageLoop) {
            this.oleMessageLoop = oleMessageLoop;
        }

        public void setDocument(HTMLDocument htmlDocument) {
            this.htmlDocument = htmlDocument;
        }

        public void navigate(String string) {
        }

        public void navigate(String string, String string1) {
        }

        public void navigate(String string, String string1, String string2) {
        }

        public String getLocationURL() {
            return null;
        }

        public HTMLDocument getDocument() {
            return htmlDocument;
        }

        public WebBrowser getParentBrowser() {
            return null;
        }

        public void setParentBrowser(WebBrowser webBrowser) {
        }

        public void goForward() {
        }

        public void goBack() {
        }

        public void goHome() {
        }

        public void stop() {
        }

        public void refresh() {
        }

        public void execute(BrowserCommand browserCommand) {
        }

        public void setContent(String string) {
        }

        public String getContent() {
            return null;
        }

        public Object executeScript(String string) {
            return null;
        }

        public ReadyState getReadyState() {
            return null;
        }

        public String getStatusText() {
            return null;
        }

        public void addPropertyChangeListener(String string, PropertyChangeListener propertyChangeListener) {
        }

        public void removePropertyChangeListener(String string, PropertyChangeListener propertyChangeListener) {
        }

        public void waitReady() {
        }

        public void waitReady(long l) {
        }

        public Object getBrowserPeer() {
            return null;
        }

        public void addNavigationListener(NavigationEventListener navigationEventListener) {
        }

        public void removeNavigationListener(NavigationEventListener navigationEventListener) {
        }

        public List getNavigationListeners() {
            return null;
        }

        public void addStatusListener(StatusEventListener statusEventListener) {
        }

        public void removeStatusListener(StatusEventListener statusEventListener) {
        }

        public List getStatusListeners() {
            return null;
        }

        public void setEventHandler(WebBrowserEventsHandler webBrowserEventsHandler) {
        }

        public WebBrowserEventsHandler getEventHandler() {
            return null;
        }

        public void setAuthenticateHandler(AuthenticateHandler authenticateHandler) {
        }

        public AuthenticateHandler getAuthenticateHandler() {
            return null;
        }

        public void setDialogEventHandler(DialogEventHandler dialogEventHandler) {
        }

        public DialogEventHandler getDialogEventHandler() {
            return null;
        }

        public void setScriptErrorListener(ScriptErrorListener scriptErrorListener) {
        }

        public ScriptErrorListener getScriptErrorListener() {
            return null;
        }

        public void close() {
        }

        public void setSilent(boolean b) {
        }

        public boolean isSilent() {
            return false;
        }

        public void setCookie(String string, Cookie cookie) {
        }

        public Set getCookies(String string) {
            return null;
        }

        public void setNewWindowHandler(NewWindowEventHandler newWindowEventHandler) {
        }

        public NewWindowEventHandler getNewWindowHandler() {
            return null;
        }

        public void addNewWindowListener(NewWindowEventListener newWindowEventListener) {
        }

        public void removeNewWindowListener(NewWindowEventListener newWindowEventListener) {
        }

        public List getNewWindowListeners() {
            return null;
        }

        public void setKeyFilter(KeyFilter keyFilter) {
        }

        public KeyFilter getKeyFilter() {
            return null;
        }

        public Properties getProperties() {
            return null;
        }

        public OleMessageLoop getOleMessageLoop() {
            return oleMessageLoop;
        }

        public void trackChildren() {
        }

        public WebBrowser getRecentChild() {
            return null;
        }

        public WebBrowser waitChildCreation() {
            return null;
        }

        public WebBrowser waitChildCreation(Runnable runnable) {
            return null;
        }
    }

    public static class IEEventHandler implements WebBrowserEventsHandler, NewWindowEventListener, NavigationEventListener, IEApplicationEventListener {

        IE ie;

        private boolean hasNavigated = false;

        private boolean navigationCompleted = false;

        public IEEventHandler(IE ie) {
            this.ie = ie;
        }

        public void trackNavigationCompleted() {
            navigationCompleted = false;
        }

        public boolean isNavigationCompleted() {
            return navigationCompleted;
        }

        public void onQuit() {
            Debug.getInstance().println("IE$IEEventHandler.onQuit");
            ie.removeSelfFromParent();
        }

        public void onVisible(boolean b) {
        }

        public void onToolBar(boolean b) {
        }

        public void onMenuBar(boolean b) {
        }

        public void onStatusBar(boolean b) {
        }

        public boolean beforeNavigate(WebBrowser webBrowser, String url, String targetFrameName, String postData, String headers) {
            if (ie.getAdditionalHttpHeaders() != null && !hasNavigated) {
                String newAddHeaders = existingHeadersValue(headers) + ie.getAdditionalHttpHeaders();
                IWebBrowser2 iWebBrowser2 = (IWebBrowser2) webBrowser.getBrowserPeer();
                iWebBrowser2.stop();
                iWebBrowser2.navigate2(new Variant(url), new Variant(0), new Variant(targetFrameName), new Variant(postData), new Variant(newAddHeaders));
                hasNavigated = true;
                return true;
            }
            return false;
        }

        private String existingHeadersValue(String existingHeaders) {
            return existingHeaders == null ? "" : existingHeaders;
        }

        public boolean beforeFileDownload() {
            Debug.getInstance().println("***************IE$IEEventHandler.beforeFileDownload");
            return false;
        }

        public boolean windowClosing(boolean b) {
            System.out.println("windowclosing");
            Debug.getInstance().println("IE$IEEventHandler.windowClosing");
            return false;
        }

        public Dimension clientAreaSizeRequested(Dimension dimension) {
            return null;
        }

        public boolean navigationErrorOccured(WebBrowser webBrowser, String string, String string1, StatusCode statusCode) {
            return false;
        }

        public void windowOpened(WebBrowser webBrowser) {
            try {
                new IE(webBrowser, ie);
            } catch (Exception e) {
                Debug.handleException(e);
            }
        }

        public void downloadBegin() {
        }

        public void downloadCompleted() {
            Debug.getInstance().println("IE$IEEventHandler.downloadCompleted");
        }

        public void documentCompleted(WebBrowser webBrowser, String string) {
        }

        public void entireDocumentCompleted(WebBrowser webBrowser, String string) {
        }

        public void navigationCompleted(WebBrowser webBrowser, String string) {
            Debug.getInstance().println("IE$IEEventHandler.navigationCompleted");
            hasNavigated = false;
            navigationCompleted = true;
        }

        public void progressChanged(int i, int i1) {
        }
    }
}
