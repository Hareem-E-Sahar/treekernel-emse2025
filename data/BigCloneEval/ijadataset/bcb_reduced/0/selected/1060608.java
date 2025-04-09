package com.google.gdt.eclipse.designer.gwtext.model.layout;

import com.google.gdt.eclipse.designer.gwtext.model.layout.assistant.AnchorLayoutAssistant;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Model for <code>com.gwtext.client.widgets.layout.AnchorLayout</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
public class AnchorLayoutInfo extends LayoutInfo {

    public AnchorLayoutInfo(AstEditor editor, ComponentDescription description, CreationSupport creationSupport) throws Exception {
        super(editor, description, creationSupport);
    }

    @Override
    protected void initializeLayoutAssistant() {
        new AnchorLayoutAssistant(this);
    }

    @Override
    protected Object getDefaultVirtualDataObject(WidgetInfo widget) throws Exception {
        ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
        Class<?> dataClass = classLoader.loadClass("com.gwtext.client.widgets.layout.AnchorLayoutData");
        return ReflectionUtils.getConstructor(dataClass, String.class).newInstance("100% 0");
    }

    /**
   * @return {@link AnchorLayoutDataInfo} association with given {@link WidgetInfo}.
   */
    public static AnchorLayoutDataInfo getAnchorData(WidgetInfo widget) {
        return (AnchorLayoutDataInfo) getLayoutData(widget);
    }
}
