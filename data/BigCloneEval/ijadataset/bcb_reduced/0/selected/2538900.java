package nzdis.util.configSWT;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * This file is different from the original one from opal package (see nzdis.util.config)
 * (use of SWT instead of SWING)
 * @author sylvie
 *
 */
class UnknownControl extends AbstractControl {

    Text field = null;

    Object mObj = null;

    String text = "";

    public UnknownControl(Composite parent, String name, Object obj, final Configuration c) {
        super(parent, name, obj);
        mObj = obj;
        Class[] params = new Class[0];
        Object[] args = new Object[0];
        Object o = null;
        try {
            Method m = mObj.getClass().getMethod("stringify", params);
            o = m.invoke(mObj, args);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException("Problem writing unknown class to configuration");
        }
        field = new Text(this, SWT.BORDER | SWT.SINGLE);
        field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        field.setSize(3, 30);
        text = (String) o;
        field.setText(text);
        if (c != null) {
            field.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    updateVal(c);
                }
            });
        }
    }

    public void updateVal(Configuration config) {
        updateValSuper(config);
        String value = field.getText();
        Class[] params = new Class[1];
        Object[] args = new Object[1];
        params[0] = String.class;
        args[0] = value;
        Object o = null;
        try {
            Constructor c = mObj.getClass().getConstructor(params);
            o = c.newInstance(args);
        } catch (Exception ex) {
            throw new IllegalStateException("Problem writing unknown class to configuration");
        }
        config.put(key, o);
    }

    public void defaultVal(Configuration config) {
        updateValSuper(config);
        field.setText(text);
    }
}
