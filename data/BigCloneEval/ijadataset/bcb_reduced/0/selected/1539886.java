package ti.plato.ui.views.properties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import ti.plato.ui.images.util.ImagesUtil;
import ti.plato.ui.views.properties.constants.Constants;
import ti.plato.ui.views.properties.views.PropertiesView;

public class PropertiesAccess {

    private static Action actionProperties = null;

    public static Action getActionProperties() {
        if (actionProperties == null) {
            actionProperties = new Action() {

                public void run() {
                    IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(PropertiesView.class.getName());
                    if (viewPart == null) {
                        try {
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(PropertiesView.class.getName());
                        } catch (PartInitException e) {
                            e.printStackTrace();
                        }
                    } else PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(viewPart);
                }
            };
            actionProperties.setText(Constants.actionPropertiesName);
            actionProperties.setToolTipText(Constants.actionPropertiesName);
            actionProperties.setImageDescriptor(ImagesUtil.getImageDescriptor("e_properties"));
            actionProperties.setDisabledImageDescriptor(ImagesUtil.getImageDescriptor("d_properties"));
        }
        return actionProperties;
    }

    public static void updateView() {
        updateView(null);
    }

    public static void updateView(final IViewPart viewPart) {
        Display.getDefault().asyncExec(new Runnable() {

            public void run() {
                PropertiesView.refresh(viewPart);
            }
        });
    }

    public static RGB getRgbFromString(String rgbString) {
        if (rgbString == null) return null;
        Pattern pattern = Pattern.compile("\\b\\d+\\b");
        Matcher matcher = pattern.matcher(rgbString);
        int[] rgb = new int[3];
        int i = 0;
        while (matcher.find()) {
            String extracted = rgbString.substring(matcher.start(), matcher.end());
            rgb[i++] = Integer.parseInt(extracted);
            if (i == 4) return null;
        }
        return new RGB(rgb[0], rgb[1], rgb[2]);
    }

    public static Object getValue(int propertyType, Action action) {
        switch(propertyType) {
            case PropertiesElement.operationTypeBoolean:
                return Boolean.valueOf(action.isChecked());
            case PropertiesElement.operationTypeCombo:
                return action.getId();
        }
        throw new RuntimeException("NYI");
    }

    public static void setValue(int propertyType, Action action, Object value) {
        switch(propertyType) {
            case PropertiesElement.operationTypeBoolean:
                action.setChecked((Boolean) value);
                return;
            case PropertiesElement.operationTypeCombo:
                action.setId((String) value);
                return;
        }
        throw new RuntimeException("NYI");
    }

    public static String value2str(int propertyType, Object value) {
        switch(propertyType) {
            case PropertiesElement.operationTypeBoolean:
                return value.toString();
            case PropertiesElement.operationTypeCombo:
                return value.toString();
        }
        throw new RuntimeException("NYI");
    }

    public static Object str2value(int propertyType, String str) {
        switch(propertyType) {
            case PropertiesElement.operationTypeBoolean:
                return Boolean.valueOf(str);
            case PropertiesElement.operationTypeCombo:
                return str;
        }
        throw new RuntimeException("NYI");
    }
}
