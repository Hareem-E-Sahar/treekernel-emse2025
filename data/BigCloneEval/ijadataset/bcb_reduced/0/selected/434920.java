package com.google.gdt.eclipse.designer.model.property.css;

import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;
import org.eclipse.wb.internal.css.dialogs.color.ColorDialog;
import org.eclipse.wb.internal.css.semantics.Semantics;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

/**
 * {@link PropertyEditor} for color property in {@link Semantics}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
public class ColorPropertyEditor extends StringComboBoxPropertyEditor {

    public static final PropertyEditor INSTANCE = new ColorPropertyEditor();

    ColorPropertyEditor() {
        super(ColorDialog.getColorNames());
    }

    private static final int SAMPLE_SIZE = 10;

    private static final int SAMPLE_MARGIN = 3;

    private final PropertyEditorPresentation m_presentation = new ButtonPropertyEditorPresentation() {

        @Override
        protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
            openDialog(property);
        }
    };

    @Override
    public PropertyEditorPresentation getPresentation() {
        return m_presentation;
    }

    @Override
    public void paint(Property property, GC gc, int x, int y, int width, int height) throws Exception {
        String text = getText(property);
        if (text != null) {
            {
                RGB rgb = ColorDialog.getRGB(text);
                if (rgb != null) {
                    Color swtColor = new Color(null, rgb);
                    Color oldBackground = gc.getBackground();
                    Color oldForeground = gc.getForeground();
                    try {
                        int width_c = SAMPLE_SIZE;
                        int height_c = SAMPLE_SIZE;
                        int x_c = x;
                        int y_c = y + (height - height_c) / 2;
                        {
                            int delta = SAMPLE_SIZE + SAMPLE_MARGIN;
                            x += delta;
                            width -= delta;
                        }
                        {
                            gc.setBackground(swtColor);
                            gc.fillRectangle(x_c, y_c, width_c, height_c);
                        }
                        gc.setForeground(IColorConstants.gray);
                        gc.drawRectangle(x_c, y_c, width_c, height_c);
                    } finally {
                        gc.setBackground(oldBackground);
                        gc.setForeground(oldForeground);
                        swtColor.dispose();
                    }
                }
            }
            DrawUtils.drawStringCV(gc, text, x, y, width, height);
        }
    }

    /**
   * @return the text for current color value.
   */
    @Override
    public String getText(Property property) throws Exception {
        return (String) property.getValue();
    }

    private final ColorDialog m_colorDialog = new ColorDialog(DesignerPlugin.getShell());

    /**
   * Opens editing dialog.
   */
    private void openDialog(Property property) throws Exception {
        {
            Object value = property.getValue();
            if (value instanceof String) {
                RGB rgb = ColorDialog.getRGB((String) value);
                if (rgb != null) {
                    m_colorDialog.setColorInfo(new ColorInfo(null, rgb));
                }
            }
        }
        if (m_colorDialog.open() == Window.OK) {
            ColorInfo colorInfo = m_colorDialog.getColorInfo();
            String colorString = getColorString(colorInfo);
            property.setValue(colorString);
        }
    }

    private static String getColorString(ColorInfo color) {
        if (color.m_name != null) {
            return color.m_name;
        } else {
            return "#" + color.getHexRGB();
        }
    }
}
