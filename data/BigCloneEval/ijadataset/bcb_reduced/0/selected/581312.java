package fastforward.wicket.renderer.component;

import java.lang.reflect.Array;
import java.util.List;
import org.apache.wicket.model.PropertyModel;
import fastforward.meta.util.PropertyUtil;
import fastforward.wicket.ArrayUtil;

public class DefaultAjaxObjectListStorageCallback implements AjaxObjectListStorageCallback {

    public void deleteObject(PropertyModel propertyModel, Object selectedChoice) {
        Object values = propertyModel.getObject();
        if (values.getClass().isArray()) {
            Object[] valuesArray = (Object[]) values;
            int selectedChoiceIndex = getIndex(selectedChoice, valuesArray);
            if (selectedChoiceIndex >= 0) {
                Object[] newArray = (Object[]) Array.newInstance(valuesArray.getClass().getComponentType(), valuesArray.length - 1);
                System.arraycopy(valuesArray, 0, newArray, 0, selectedChoiceIndex);
                System.arraycopy(valuesArray, selectedChoiceIndex + 1, newArray, selectedChoiceIndex, valuesArray.length - selectedChoiceIndex - 1);
                PropertyUtil.setPropertyValue(propertyModel.getTarget(), propertyModel.getPropertyExpression(), newArray);
            }
        } else if (List.class.isAssignableFrom(values.getClass())) {
            List valuesCollection = (List) values;
            int selectedChoiceIndex = valuesCollection.indexOf(selectedChoice);
            if (selectedChoiceIndex != -1) {
                valuesCollection.remove(selectedChoiceIndex);
            }
        }
    }

    public void storeNewObject(PropertyModel propertyModel, Object newObject) {
        Object values = propertyModel.getObject();
        if (values.getClass().isArray()) {
            Object[] valuesArray = (Object[]) values;
            Object[] newArray = null;
            if (valuesArray != null) {
                newArray = (Object[]) Array.newInstance(valuesArray.getClass().getComponentType(), valuesArray.length + 1);
                ArrayUtil.expandByOne(valuesArray, newArray, newObject);
            } else {
                newArray = (Object[]) Array.newInstance(newObject.getClass(), 1);
                newArray[0] = newObject;
            }
            PropertyUtil.setPropertyValue(propertyModel.getTarget(), propertyModel.getPropertyExpression(), newArray);
        } else if (List.class.isAssignableFrom(values.getClass())) {
            List valuesCollection = (List) values;
            valuesCollection.add(newObject);
        }
    }

    public int getIndex(Object selectedChoice, Object[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(selectedChoice)) return i;
        }
        return -1;
    }
}
