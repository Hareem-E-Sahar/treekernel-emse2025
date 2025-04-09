import infoviewer.InfoViewer;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.lang.reflect.*;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.OptionsDialog;
import org.gjt.sp.jedit.msg.ViewURL;
import org.gjt.sp.util.Log;

public class InfoViewerPlugin extends EBPlugin {

    /** the shared InfoViewer instance */
    private static InfoViewer infoviewer = null;

    public void createMenuItems(Vector menuItems) {
        menuItems.addElement(GUIUtilities.loadMenu("infoviewer-menu"));
    }

    public void createOptionPanes(OptionsDialog optionsDialog) {
        optionsDialog.addOptionPane(new infoviewer.InfoViewerOptionPane());
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
            gotoURL(vu.getURL());
        }
    }

    /**
	 * this function demonstrates how ViewURL messages should be send on
	 * the EditBus.
	 * @param url an URL that should be displayed in InfoViewer
	 * @param view a View from which the message is sent
	 */
    public void sendURL(URL url, View view) {
        ViewURL vu = new ViewURL(this, view, url);
        EditBus.send(vu);
        if (!vu.isVetoed()) {
            GUIUtilities.error(view, "infoviewer.error.noinfoviewer", null);
            return;
        }
    }

    public void gotoURL(URL url) {
        String u = (url == null ? "" : url.toString());
        String browsertype = jEdit.getProperty("infoviewer.browsertype");
        if (u.startsWith("jeditresource:")) {
            browsertype = "internal";
        }
        Log.log(Log.DEBUG, this, "(" + browsertype + "): gotoURL: " + u);
        if ("external".equals(browsertype)) {
            String cmd = jEdit.getProperty("infoviewer.otherBrowser");
            String[] args = convertCommandString(cmd, u);
            try {
                Runtime.getRuntime().exec(args);
            } catch (Exception ex) {
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < args.length; i++) {
                    buf.append(args[i]);
                    buf.append('\n');
                }
                GUIUtilities.error(null, "infoviewer.error.invokeBrowser", new Object[] { ex, buf.toString() });
                return;
            }
        } else if ("class".equals(browsertype)) {
            String clazzname = jEdit.getProperty("infoviewer.class");
            String methodname = jEdit.getProperty("infoviewer.method");
            gotoURLWithMethod(u, clazzname, methodname);
        } else if ("netscape".equals(browsertype)) {
            String[] args = new String[3];
            args[0] = "sh";
            args[1] = "-c";
            args[2] = "netscape -remote openURL\\('" + u + "'\\) -raise || netscape '" + u + "'";
            try {
                Runtime.getRuntime().exec(args);
            } catch (Exception ex) {
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < args.length; i++) {
                    buf.append(args[i]);
                    buf.append('\n');
                }
                GUIUtilities.error(null, "infoviewer.error.invokeBrowser", new Object[] { ex, buf.toString() });
            }
        } else {
            if (infoviewer == null) {
                infoviewer = new InfoViewer();
            }
            infoviewer.setVisible(true);
            infoviewer.gotoURL(url, true);
        }
    }

    /**
	 * converts the command string, which may contain "$u" as placeholders
	 * for an url, into an array of strings, tokenized at the space char.
	 * Characters in the command string may be escaped with '\\', which
	 * in the case of space prevents tokenization.
	 * @param command  the command string.
	 * @param url  the URL
	 * @return the space separated parts of the command string, as array
	 *   of Strings.
	 */
    private String[] convertCommandString(String command, String url) {
        Vector args = new Vector();
        StringBuffer arg = new StringBuffer();
        boolean foundDollarU = false;
        boolean inQuotes = false;
        int end = command.length() - 1;
        for (int i = 0; i <= end; i++) {
            char c = command.charAt(i);
            switch(c) {
                case '$':
                    if (i == end) {
                        arg.append(c);
                    } else {
                        char c2 = command.charAt(++i);
                        if (c2 == 'u') {
                            arg.append(url);
                            foundDollarU = true;
                        } else {
                            arg.append(c);
                            arg.append(c2);
                        }
                    }
                    break;
                case '"':
                    inQuotes = !inQuotes;
                    break;
                case ' ':
                    if (inQuotes) {
                        arg.append(c);
                    } else {
                        String newArg = arg.toString().trim();
                        if (newArg.length() > 0) args.addElement(newArg);
                        arg = new StringBuffer();
                    }
                    break;
                case '\\':
                    if (i == end) {
                        arg.append(c);
                    } else {
                        char c2 = command.charAt(++i);
                        if (c2 != '\\') arg.append(c);
                        arg.append(c2);
                    }
                    break;
                default:
                    arg.append(c);
                    break;
            }
        }
        String newArg = arg.toString().trim();
        if (newArg.length() > 0) args.addElement(newArg);
        if (!foundDollarU && url.length() > 0) args.addElement(url);
        String[] result = new String[args.size()];
        args.copyInto(result);
        for (int i = 0; i < result.length; i++) Log.log(Log.DEBUG, this, "args[" + i + "]=" + result[i]);
        return result;
    }

    private void gotoURLWithMethod(String url, String clazz, String method) {
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
}
