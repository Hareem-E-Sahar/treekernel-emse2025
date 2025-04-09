package de.sic_consult.forms.editor.swing;

import java.awt.Component;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Constructor;
import java.sql.Blob;
import de.sic_consult.forms.ReflectionException;
import de.sic_consult.forms.editor.FieldEditorFactory;
import de.sic_consult.forms.editor.SubForm;
import de.sic_consult.forms.editor.SubTable;
import de.sic_consult.forms.field.FieldDescription;
import de.sic_consult.forms.formatter.ListAnnotation;

public class SwingPropertyEditorFactory implements FieldEditorFactory<Component, SwingFieldEditor> {

    private static SwingPropertyEditorFactory instance = new SwingPropertyEditorFactory();

    public SwingPropertyEditorFactory() {
        PropertyEditorManager.registerEditor(Object.class, DefaultPropertyEditor.class);
        PropertyEditorManager.registerEditor(Byte.TYPE, DefaultPropertyEditor.class);
        PropertyEditorManager.registerEditor(Float.TYPE, DefaultPropertyEditor.class);
        PropertyEditorManager.registerEditor(Double.TYPE, DefaultPropertyEditor.class);
        PropertyEditorManager.registerEditor(Enum.class, EnumPropertyEditor.class);
        PropertyEditorManager.registerEditor(Integer.TYPE, NumberEditor.class);
        PropertyEditorManager.registerEditor(Short.TYPE, NumberEditor.class);
        PropertyEditorManager.registerEditor(Long.TYPE, NumberEditor.class);
        PropertyEditorManager.registerEditor(Byte.TYPE, DefaultPropertyEditor.class);
        PropertyEditorManager.registerEditor(Float.class, DefaultPropertyEditor.class);
        PropertyEditorManager.registerEditor(Double.class, DefaultPropertyEditor.class);
        PropertyEditorManager.registerEditor(Enum.class, EnumPropertyEditor.class);
        PropertyEditorManager.registerEditor(Integer.class, NumberEditor.class);
        PropertyEditorManager.registerEditor(Short.class, NumberEditor.class);
        PropertyEditorManager.registerEditor(Long.class, NumberEditor.class);
        PropertyEditorManager.registerEditor(Blob.class, BlobEditor.class);
        PropertyEditorManager.setEditorSearchPath(new String[] { this.getClass().getPackage().getName() });
    }

    public static SwingPropertyEditorFactory getInstance() {
        return instance;
    }

    public Class<?> getPropertyTyp(PropertyDescriptor prop) {
        if (prop.getReadMethod() != null) {
            return prop.getReadMethod().getReturnType();
        }
        return null;
    }

    private DefaultPropertyEditor getPredefinedEditor(Class<?> parentClass, PropertyDescriptor prop) {
        Class<?> typ = getPropertyTyp(prop);
        if (typ == null) {
            return new DefaultPropertyEditor();
        }
        if (Enum.class.isAssignableFrom(typ)) {
            return new EnumPropertyEditor(typ);
        }
        if (prop.getReadMethod().isAnnotationPresent(ListAnnotation.class)) {
            ListAnnotation list = prop.getReadMethod().getAnnotation(ListAnnotation.class);
            return new ListPropertyEditor(list);
        }
        if (prop.getReadMethod().isAnnotationPresent(SubForm.class)) {
            SubForm subForm = prop.getReadMethod().getAnnotation(SubForm.class);
            return new SubFormPropertyEditor(subForm, typ);
        }
        if (prop.getReadMethod().isAnnotationPresent(SubTable.class)) {
            SubTable subForm = prop.getReadMethod().getAnnotation(SubTable.class);
            return new SubTableEditor(subForm);
        }
        return null;
    }

    private DefaultPropertyEditor createDefault(Class<?> propClass) {
        DefaultPropertyEditor e = (DefaultPropertyEditor) PropertyEditorManager.findEditor(propClass);
        if (e == null) {
            e = new DefaultPropertyEditor();
        }
        return e;
    }

    private DefaultPropertyEditor createInstance(Class<? extends DefaultPropertyEditor> clazz, Class<?> editClass) {
        for (Constructor con : clazz.getConstructors()) {
            if (con.getParameterTypes().length == 1 && con.getParameterTypes()[0] == Class.class) {
                try {
                    DefaultPropertyEditor back = (DefaultPropertyEditor) con.newInstance(editClass.getClasses());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        DefaultPropertyEditor back = null;
        try {
            clazz.newInstance();
        } catch (Exception e) {
            throw new ReflectionException(e, "Der PropertyEitor kann nciht erstellt werden");
        }
        return back;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SwingFieldEditor getEditor(Class<?> parentClass, PropertyDescriptor prop) {
        DefaultPropertyEditor editor = getPredefinedEditor(parentClass, prop);
        if (editor == null) {
            if (prop.getPropertyEditorClass() != null && PropertyEditor.class.isAssignableFrom(prop.getPropertyEditorClass())) {
                editor = createInstance((Class<? extends DefaultPropertyEditor>) prop.getPropertyEditorClass(), parentClass);
            }
        }
        if (editor == null) {
            editor = createDefault(prop.getPropertyType());
        }
        if (editor instanceof DefaultPropertyEditor) {
            ((DefaultPropertyEditor) editor).setPropertie(prop);
        }
        return new SwingFieldEditor(editor);
    }

    public SwingFieldEditor getEditor(FieldDescription fieldDesc) {
        return getEditor(fieldDesc.getParentClass(), fieldDesc.getPropertyDescription());
    }
}
