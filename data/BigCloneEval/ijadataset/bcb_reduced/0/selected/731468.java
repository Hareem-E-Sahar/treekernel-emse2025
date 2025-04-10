package org.apache.harmony.awt.im;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;
import org.apache.harmony.awt.ComponentInternals;
import org.apache.harmony.awt.ContextStorage;
import org.apache.harmony.awt.wtk.NativeIM;
import com.google.code.appengine.awt.AWTException;
import com.google.code.appengine.awt.CheckboxMenuItem;
import com.google.code.appengine.awt.Component;
import com.google.code.appengine.awt.HeadlessException;
import com.google.code.appengine.awt.Menu;
import com.google.code.appengine.awt.MenuContainer;
import com.google.code.appengine.awt.MenuItem;
import com.google.code.appengine.awt.PopupMenu;
import com.google.code.appengine.awt.Window;
import com.google.code.appengine.awt.event.ItemEvent;
import com.google.code.appengine.awt.event.ItemListener;
import com.google.code.appengine.awt.event.KeyEvent;
import com.google.code.appengine.awt.im.spi.InputMethodDescriptor;

/**
 * Helper class which manages resources shared by
 * several InputMethodContexts, such as list of
 * IM descriptors, IM selection popup, currently visible composition
 * window, etc
 */
public class IMManager {

    private static final String SEL_KEY_NODE = "java/awt/im/selectionKey";

    private static final String INPUT_STYLE_PROP = "java.awt.im.style";

    private static final String BELOW_THE_SPOT = "below-the-spot";

    private static final String SERVICES = "META-INF/services/";

    /**
     * Input method selection popup menu
     */
    private static class IMSelection extends PopupMenu implements ItemListener {

        private class IMenuItem extends CheckboxMenuItem {

            private final Locale locale;

            IMenuItem(Locale loc) throws HeadlessException {
                super(loc.getDisplayName());
                locale = loc;
            }

            private final InputMethodDescriptor getDesc() {
                MenuContainer parent = getParent();
                if (parent instanceof IMSubmenu) {
                    return ((IMSubmenu) parent).getDesc();
                }
                return null;
            }

            private final Locale getLocale() {
                return locale;
            }

            @Override
            public String paramString() {
                return super.paramString() + ",desc=" + getDesc() + ",locale=" + locale;
            }
        }

        private class IMSubmenu extends Menu {

            private final InputMethodDescriptor desc;

            IMSubmenu(InputMethodDescriptor imd) throws HeadlessException {
                super(imd.getInputMethodDisplayName(null, null));
                desc = imd;
                addLocales();
            }

            private final InputMethodDescriptor getDesc() {
                return desc;
            }

            @Override
            public String paramString() {
                return super.paramString() + ",desc=" + desc;
            }

            private void addLocales() {
                try {
                    Locale[] locs = desc.getAvailableLocales();
                    for (Locale element : locs) {
                        addMenuItem(element);
                    }
                } catch (AWTException e) {
                    e.printStackTrace();
                }
            }

            private void addMenuItem(Locale loc) {
                IMenuItem item = new IMenuItem(loc);
                item.addItemListener(IMSelection.this);
                add(item);
            }

            private void checkItems() {
                for (int i = 0; i < getItemCount(); i++) {
                    IMenuItem item = (IMenuItem) getItem(i);
                    try {
                        item.setState(item.getLocale().equals(imContext.getLocale()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        InputMethodContext imContext;

        public IMSelection() {
            Iterator<InputMethodDescriptor> it = getIMDescriptors().iterator();
            addNativeIM(it);
            while (it.hasNext()) {
                addIM(it);
            }
        }

        private void addIM(Iterator<InputMethodDescriptor> it) {
            InputMethodDescriptor desc = it.next();
            IMSubmenu subMenu = new IMSubmenu(desc);
            add(subMenu);
        }

        private void addNativeIM(Iterator<InputMethodDescriptor> it) {
            if (it.hasNext()) {
                addIM(it);
            }
            add(new MenuItem("-"));
        }

        public void itemStateChanged(ItemEvent e) {
            if (imContext == null) {
                return;
            }
            Object src = e.getSource();
            if (src instanceof IMenuItem) {
                IMenuItem item = (IMenuItem) src;
                imContext.selectIM(item.getDesc(), item.getLocale());
            }
        }

        private void show(Component origin, InputMethodContext imc) {
            imContext = imc;
            for (int i = 0; i < getItemCount(); i++) {
                MenuItem item = getItem(i);
                if (!(item instanceof IMSubmenu)) {
                    continue;
                }
                IMSubmenu subMenu = (IMSubmenu) item;
                InputMethodDescriptor desc = subMenu.getDesc();
                if (desc == null) {
                    continue;
                }
                if (desc.hasDynamicLocaleList()) {
                    subMenu.removeAll();
                    subMenu.addLocales();
                }
                subMenu.checkItems();
                i++;
            }
            show(origin, 50, 50);
        }
    }

    private static List<InputMethodDescriptor> imd;

    private static IMSelection imPopup;

    private static Window curCompositionWindow;

    private static InputMethodContext lastActiveIMC;

    static List<InputMethodDescriptor> getIMDescriptors() {
        if (imd == null) {
            imd = loadIMDescriptors();
        }
        return imd;
    }

    /**
     * Loads all IM descriptors from
     * extension jars(services).
     * Does roughly the same as
     * Service.providers(InputMethodDescriptor.class)
     * @return list of input method descriptors
     */
    private static List<InputMethodDescriptor> loadIMDescriptors() {
        String nm = SERVICES + InputMethodDescriptor.class.getName();
        Enumeration<URL> en;
        LinkedList<InputMethodDescriptor> imdList = new LinkedList<InputMethodDescriptor>();
        NativeIM nativeIM = ContextStorage.getNativeIM();
        imdList.add(nativeIM);
        try {
            en = ClassLoader.getSystemResources(nm);
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            while (en.hasMoreElements()) {
                URL url = en.nextElement();
                InputStreamReader isr = new InputStreamReader(url.openStream(), "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                String str = br.readLine();
                while (str != null) {
                    str = str.trim();
                    int comPos = str.indexOf("#");
                    if (comPos >= 0) {
                        str = str.substring(0, comPos);
                    }
                    if (str.length() > 0) {
                        imdList.add((InputMethodDescriptor) cl.loadClass(str).newInstance());
                    }
                    str = br.readLine();
                }
            }
        } catch (Exception e) {
        }
        return imdList;
    }

    private static void showIMPopup(InputMethodContext imc, Window parent) {
        List<InputMethodDescriptor> descriptors = getIMDescriptors();
        if ((descriptors == null) || descriptors.isEmpty()) {
            return;
        }
        if (imPopup == null) {
            imPopup = new IMSelection();
        }
        if (parent != null) {
            parent.add(imPopup);
            imPopup.show(parent, imc);
        }
    }

    private static int getPref(String key, int def) {
        int pref = getPref(Preferences.userRoot(), key, def);
        if (pref != def) {
            return pref;
        }
        return getPref(Preferences.systemRoot(), key, def);
    }

    private static int getPref(Preferences root, String key, int def) {
        return root.node(SEL_KEY_NODE).getInt(key, def);
    }

    static void selectIM(KeyEvent ke, InputMethodContext imc, Window parent) {
        int def = KeyEvent.VK_UNDEFINED;
        int keyCode = getPref("keyCode", def);
        if (keyCode != def) {
            int modifiers = getPref("modifiers", 0);
            if ((ke.getKeyCode() == keyCode) && (ke.getModifiers() == modifiers)) {
                IMManager.showIMPopup(imc, parent);
            }
        }
    }

    @SuppressWarnings("deprecation")
    static void showCompositionWindow(Window w) {
        if (curCompositionWindow != null) {
            if (curCompositionWindow != w) {
                curCompositionWindow.hide();
            }
        }
        curCompositionWindow = w;
        if ((curCompositionWindow != null) && !curCompositionWindow.isVisible()) {
            curCompositionWindow.show();
        }
    }

    private static String getInputStyle() {
        String propName = INPUT_STYLE_PROP;
        String inputStyle = org.apache.harmony.awt.Utils.getSystemProperty(propName);
        if (inputStyle != null) {
            return inputStyle;
        }
        return java.awt.Toolkit.getProperty(propName, null);
    }

    static boolean belowTheSpot() {
        return BELOW_THE_SPOT.equals(getInputStyle());
    }

    static Window getWindow(Component comp) {
        if (comp == null) {
            return null;
        }
        Component parent = comp.getParent();
        while ((parent != null) && !(parent instanceof Window)) {
            parent = parent.getParent();
        }
        return (Window) parent;
    }

    static void makeIMWindow(Window win) {
        win.setFocusableWindowState(false);
        win.setAlwaysOnTop(true);
        ComponentInternals ci = ComponentInternals.getComponentInternals();
        ci.getNativeWindow(win).setIMStyle();
    }

    public static final InputMethodContext getLastActiveIMC() {
        return lastActiveIMC;
    }

    static final void setLastActiveIMC(InputMethodContext lastActiveIMC) {
        IMManager.lastActiveIMC = lastActiveIMC;
    }
}
