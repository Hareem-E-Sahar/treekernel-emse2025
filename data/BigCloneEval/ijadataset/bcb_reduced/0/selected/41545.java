package org.springframework.faces.model.converter;

import java.lang.reflect.Constructor;
import java.util.List;
import javax.faces.model.DataModel;
import org.springframework.binding.convert.converters.Converter;
import org.springframework.faces.model.OneSelectionTrackingListDataModel;
import org.springframework.util.ClassUtils;

/**
 * A {@link Converter} implementation that converts an Object, Object array, or {@link List} into a JSF
 * {@link DataModel}.
 * 
 * @author Jeremy Grelle
 */
public class DataModelConverter implements Converter {

    public Class getSourceClass() {
        return Object.class;
    }

    public Class getTargetClass() {
        return DataModel.class;
    }

    public Object convertSourceToTargetClass(Object source, Class targetClass) throws Exception {
        if (targetClass.equals(DataModel.class)) {
            targetClass = OneSelectionTrackingListDataModel.class;
        }
        Constructor emptyConstructor = ClassUtils.getConstructorIfAvailable(targetClass, new Class[] {});
        DataModel model = (DataModel) emptyConstructor.newInstance(new Object[] {});
        model.setWrappedData(source);
        return model;
    }
}
