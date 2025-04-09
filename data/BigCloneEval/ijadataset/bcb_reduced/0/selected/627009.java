package org.amhm.ui.dialogs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import org.amhm.mail.MailInbox;
import org.amhm.persistence.Contact;
import org.amhm.ui.controler.ContactControler;
import org.amhm.ui.helpers.UIHelper;
import org.amhm.ui.list.TableList;
import org.amhm.ui.menus.MainMenuBar;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Display;

public class MainShell {

    private Shell shell;

    private CTabFolder tab;

    private MainMenuBar menubar;

    public MainShell(Display disp) {
        this.shell = new Shell(disp);
        shell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        shell.setText("Amhm");
        this.shell.setSize(640, 515);
        this.shell.setLocation(UIHelper.getScreenPosition(this.shell));
        this.shell.setBackground(UIHelper.getGrayDefaultColor());
        shell.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = layout.marginWidth = 0;
        layout.verticalSpacing = layout.horizontalSpacing = 0;
        shell.setLayout(layout);
        new org.amhm.ui.menus.MenuLeft(this);
        tab = new CTabFolder(shell, SWT.NONE);
        menubar = new MainMenuBar(shell, this.tab);
        tab.setLayoutData(new GridData(GridData.FILL_BOTH));
        tab.setTabHeight(20);
        tab.setBorderVisible(true);
        Color color = UIHelper.getLightBlueColor();
        tab.setSelectionBackground(new Color[] { this.shell.getDisplay().getSystemColor(SWT.COLOR_WHITE), color, color }, new int[] { 50, 100 }, true);
        CTabItem item = new CTabItem(tab, SWT.NONE, 0);
        item.setText("G�n�ral");
        item.setControl(new org.amhm.ui.list.DataList(tab, ContactControler.getContacts()));
        tab.setSelection(item);
        this.shell.open();
    }

    public void addTabItem(Set<Contact> list) {
        CTabItem item = new CTabItem(tab, SWT.CLOSE);
        item.setText("R�sultat " + (tab.getItemCount() - 1));
        item.setControl(new org.amhm.ui.list.DataList(tab, list));
        tab.setSelection(item);
    }

    public void addTabItem(Class<?> cl, Set<?> list, String tabText) {
        boolean exists = false;
        for (CTabItem it : tab.getItems()) {
            if (it.getText().equalsIgnoreCase(tabText)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            CTabItem item = new CTabItem(tab, SWT.CLOSE);
            item.setText(tabText);
            try {
                Constructor<?>[] c = cl.getConstructors();
                Object o = c[0].newInstance(tab, list);
                TableList<?> tablist = (TableList<?>) o;
                item.setControl(tablist);
                tab.setSelection(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public Composite getShell() {
        return this.shell;
    }

    public void display(Display disp) {
        while (!shell.isDisposed()) {
            if (!disp.readAndDispatch()) disp.sleep();
        }
        menubar.dispose();
        disp.dispose();
        MailInbox.getInstance().close();
    }
}
