import infoviewer.InfoViewer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.*;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import java.util.Enumeration;
import javax.swing.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.OptionsDialog;
import org.gjt.sp.jedit.msg.ViewURL;
import org.gjt.sp.util.Log;

public class InfoViewerPlugin extends EBPlugin {

    /** the shared InfoViewer instance */
    private static InfoViewer infoviewer = null;

    public void start() {
        jEdit.addAction(new infoviewer_open());
        jEdit.addAction(new infoviewer_open_buffer());
        jEdit.addAction(new infoviewer_open_sel());
    }

    public void createMenuItems(View view, Vector menus, Vector menuItems) {
        menus.addElement(GUIUtilities.loadMenu(view, "infoviewer-menu"));
    }

    public void createOptionPanes(OptionsDialog optionsDialog) {
        OptionGroup myGroup = new OptionGroup("InfoViewer");
        myGroup.addOptionPane(new infoviewer.ChooseBrowserOptionPane());
        myGroup.addOptionPane(new infoviewer.InfoViewerOptionPane());
        optionsDialog.addOptionGroup(myGroup);
    }

    /**
     * handle messages from the EditBus. InfoViewer reacts to messages of
     * type ViewURL. If it sees such a message, it will veto() it, so that
     * the sender knows that it was seen.
     * @param message the EditBus message
     * @see org.gjt.sp.jedit.msg.ViewURL
     * @see org.gjt.sp.jedit.EBMessage#veto()
     */
    public void handleMessage(EBMessage message) {
        if (message instanceof ViewURL) {
            ViewURL vu = (ViewURL) message;
            vu.veto();
            showURL(vu.getURL());
        }
    }

    /**
     * this function demonstrates how ViewURL messages should be send on
     * the EditBus.
     * @param url an URL that should be displayed in InfoViewer
     * @param view a View from which the message is sent
     */
    protected void sendURL(URL url, View view) {
        ViewURL vu = new ViewURL(this, view, url);
        EditBus.send(vu);
        if (!vu.isVetoed()) {
            GUIUtilities.error(view, "infoviewer.error.noinfoviewer", null);
            return;
        }
    }

    private void showURL(URL url) {
        String u = (url == null ? "" : url.toString());
        String browsertype = jEdit.getProperty("infoviewer.browsertype");
        if (u.startsWith("jeditresource:")) {
            browsertype = "internal";
        }
        Log.log(Log.DEBUG, this, "gotoURL (" + browsertype + "): " + u);
        if ("external".equals(browsertype)) {
            String cmd = jEdit.getProperty("infoviewer.otherBrowser");
            String[] args = convertCommandString(cmd, u);
            try {
                Runtime.getRuntime().exec(args);
            } catch (Exception ex) {
                GUIUtilities.error(null, "infoviewer.error.invokeBrowser", new Object[] { ex, args.toString() });
                return;
            }
        } else if ("class".equals(browsertype)) {
            String clazzname = jEdit.getProperty("infoviewer.class");
            String methodname = jEdit.getProperty("infoviewer.method");
            showURLWithMethod(u, clazzname, methodname);
        } else if ("netscape".equals(browsertype)) {
            String[] args = new String[3];
            args[0] = "sh";
            args[1] = "-c";
            args[2] = "netscape -remote openURL\\(\'" + u + "\'\\) -raise || netscape \'" + u + "\'";
            try {
                Runtime.getRuntime().exec(args);
            } catch (Exception ex) {
                GUIUtilities.error(null, "infoviewer.error.invokeBrowser", new Object[] { ex, args.toString() });
            }
        } else {
            showInternalInfoViewer(url);
        }
    }

    /** 
     * this is a convenience method to force InfoViewerPlugin to use the
     * internal InfoViewer. The static instance will be created, opened,
     * brought to front and loaded with the url. This method is the
     * default, if class/method invocation is used.
     * @param url the URL.
     */
    public void showInternalInfoViewer(URL url) {
        if (infoviewer == null) {
            infoviewer = new InfoViewer();
        }
        infoviewer.setVisible(true);
        infoviewer.gotoURL(url, true);
    }

    /**
     * converts the command string, which may contain "$u" as placeholders
     * for an url, into an array of strings, tokenized at the space char.
     * Characters in the command string may be escaped with '\\', which
     * in the case of space prevents tokenization.
     * @param command the command string.
     * @param url the URL. Spaces in the URL are converted to "%20".
     * @return the space separated parts of the command string, as array
     *   of Strings.
     */
    private String[] convertCommandString(String command, String url) {
        Vector args = new Vector();
        StringBuffer buf = new StringBuffer();
        boolean ufound = false;
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            switch(c) {
                case '$':
                    if (i == command.length() - 1) buf.append(c); else {
                        char c2 = command.charAt(++i);
                        switch(c2) {
                            case 'u':
                                buf.append(url);
                                ufound = true;
                                break;
                            default:
                                buf.append(c2);
                                break;
                        }
                    }
                    break;
                case ' ':
                    args.addElement(buf.toString());
                    buf = new StringBuffer();
                    break;
                case '\\':
                    if (i == command.length() - 1) buf.append(c); else buf.append(command.charAt(++i));
                    break;
                default:
                    buf.append(c);
                    break;
            }
        }
        args.addElement(buf.toString());
        if (!ufound) args.addElement(url);
        String[] result = new String[args.size()];
        args.copyInto(result);
        return result;
    }

    private void showURLWithMethod(String url, String clazz, String method) {
        Class c = null;
        Object obj = null;
        try {
            c = Class.forName(clazz);
        } catch (Throwable e) {
            GUIUtilities.error(null, "infoviewer.error.classnotfound", new Object[] { clazz });
            return;
        }
        if (method == null || (method != null && method.length() == 0)) {
            Constructor constr = null;
            try {
                constr = c.getConstructor(new Class[] { URL.class });
                if (constr != null) obj = constr.newInstance(new Object[] { new URL(url) });
            } catch (Exception ex) {
                Log.log(Log.DEBUG, this, ex);
            }
            if (obj == null) {
                try {
                    constr = c.getConstructor(new Class[] { String.class });
                    if (constr != null) obj = constr.newInstance(new Object[] { url });
                } catch (Exception ex) {
                    Log.log(Log.DEBUG, this, ex);
                }
            }
            if (obj == null) {
                try {
                    constr = c.getConstructor(new Class[0]);
                    if (constr != null) obj = constr.newInstance(new Object[0]);
                } catch (Exception ex) {
                    Log.log(Log.DEBUG, this, ex);
                }
            }
            if (obj == null) {
                GUIUtilities.error(null, "infoviewer.error.classnotfound", new Object[] { clazz });
                return;
            }
        } else {
            Method meth = null;
            boolean ok = false;
            try {
                meth = c.getDeclaredMethod(method, new Class[] { URL.class });
                if (meth != null) {
                    obj = meth.invoke(null, new Object[] { new URL(url) });
                    ok = true;
                }
            } catch (Exception ex) {
                Log.log(Log.DEBUG, this, ex);
            }
            if (!ok) {
                try {
                    meth = c.getDeclaredMethod(method, new Class[] { String.class });
                    if (meth != null) {
                        obj = meth.invoke(null, new Object[] { url });
                        ok = true;
                    }
                } catch (Exception ex) {
                    Log.log(Log.DEBUG, this, ex);
                }
            }
            if (!ok) {
                try {
                    meth = c.getDeclaredMethod(method, new Class[0]);
                    if (meth != null) {
                        obj = meth.invoke(null, new Object[0]);
                        ok = true;
                    }
                } catch (Exception ex) {
                    Log.log(Log.DEBUG, this, ex);
                }
            }
            if (!ok) {
                GUIUtilities.error(null, "infoviewer.error.methodnotfound", new Object[] { clazz, method });
                return;
            }
        }
        if (obj != null) {
            if (obj instanceof Window) {
                ((Window) obj).show();
            } else if (obj instanceof JComponent) {
                JFrame f = new JFrame("Infoviewer JWrapper");
                f.getContentPane().add((JComponent) obj);
                f.pack();
                f.setVisible(true);
            } else if (obj instanceof Component) {
                Frame f = new Frame("Infoviewer Wrapper");
                f.add((Component) obj);
                f.pack();
                f.setVisible(true);
            }
        }
    }

    /**
     * an action for opening the InfoViewer
     */
    protected class infoviewer_open extends EditAction {

        public infoviewer_open() {
            super("infoviewer-open");
        }

        public void actionPerformed(ActionEvent evt) {
            sendURL(null, getView(evt));
        }
    }

    public class infoviewer_open_buffer extends EditAction {

        public infoviewer_open_buffer() {
            super("infoviewer-open-buffer");
        }

        public void actionPerformed(ActionEvent evt) {
            View view = getView(evt);
            Buffer buffer = view.getBuffer();
            if (buffer.isDirty()) {
                String[] args = { buffer.getName() };
                int result = JOptionPane.showConfirmDialog(view, jEdit.getProperty("notsaved.message", args), jEdit.getProperty("notsaved.title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    buffer.save(view, null);
                } else if (result != JOptionPane.NO_OPTION) {
                    return;
                }
            }
            String bufpath = "file:" + buffer.getPath();
            try {
                sendURL(new URL(bufpath), getView(evt));
            } catch (java.net.MalformedURLException e) {
                GUIUtilities.error(view, "infoviewer.error.badurl", new String[] { bufpath });
                return;
            }
        }
    }

    protected class infoviewer_open_sel extends EditAction {

        public infoviewer_open_sel() {
            super("infoviewer-open-sel");
        }

        public void actionPerformed(ActionEvent evt) {
            View view = getView(evt);
            Buffer buffer = view.getBuffer();
            String selection = view.getTextArea().getSelectedText();
            if (selection == null) {
                GUIUtilities.error(view, "infoviewer.error.noselection", null);
                return;
            }
            URL u;
            try {
                u = new URL(selection);
            } catch (java.net.MalformedURLException e) {
                GUIUtilities.error(view, "infoviewer.error.badurl", new String[] { selection });
                return;
            }
            sendURL(u, getView(evt));
        }
    }
}
