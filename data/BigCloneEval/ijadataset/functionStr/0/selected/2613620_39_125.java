public class Test {    void draw(Theme theme, GC gc, Rectangle bounds) {
        int state = this.state[DrawData.WIDGET_WHOLE];
        int drawable = gc.getGCData().drawable;
        if ((style & SWT.SEPARATOR) != 0) {
            int state_type = getStateType(DrawData.WIDGET_WHOLE);
            int separatorHandle = theme.separatorHandle;
            byte[] detail = Converter.wcsToMbcs(null, "vseparator", true);
            int gtkStyle = OS.gtk_widget_get_style(separatorHandle);
            theme.transferClipping(gc, gtkStyle);
            if ((parent.style & SWT.VERTICAL) != 0) {
                OS.gtk_paint_hline(gtkStyle, drawable, state_type, null, separatorHandle, detail, bounds.x, bounds.x + bounds.width, bounds.y + bounds.height / 2);
            } else {
                OS.gtk_paint_vline(gtkStyle, drawable, state_type, null, separatorHandle, detail, bounds.y, bounds.y + bounds.height, bounds.x + bounds.width / 2);
            }
            return;
        }
        int buttonHandle = theme.buttonHandle;
        int gtkStyle = OS.gtk_widget_get_style(buttonHandle);
        theme.transferClipping(gc, gtkStyle);
        int focus_line_width = theme.getWidgetProperty(buttonHandle, "focus-line-width");
        int focus_padding = theme.getWidgetProperty(buttonHandle, "focus-padding");
        int border_width = OS.gtk_container_get_border_width(buttonHandle);
        int x = bounds.x + border_width;
        int y = bounds.y + border_width;
        int width = bounds.width - border_width * 2;
        int height = bounds.height - border_width * 2;
        byte[] detail = null;
        if ((style & (SWT.PUSH | SWT.DROP_DOWN)) != 0) {
            detail = Converter.wcsToMbcs(null, "button", true);
        } else if ((style & (SWT.CHECK | SWT.RADIO)) != 0) {
            detail = Converter.wcsToMbcs(null, "togglebutton", true);
        }
        int[] relief = new int[1];
        int toolbarHandle = theme.toolbarHandle;
        OS.gtk_widget_style_get(toolbarHandle, OS.button_relief, relief, 0);
        int shadow_type = OS.GTK_SHADOW_OUT;
        if ((state & (DrawData.SELECTED | DrawData.PRESSED)) != 0) shadow_type = OS.GTK_SHADOW_IN;
        int state_type = getStateType(DrawData.WIDGET_WHOLE);
        if (relief[0] != OS.GTK_RELIEF_NONE || ((state & (DrawData.PRESSED | DrawData.HOT | DrawData.SELECTED)) != 0)) {
            OS.gtk_paint_box(gtkStyle, drawable, state_type, shadow_type, null, buttonHandle, detail, x, y, width, height);
        }
        if (clientArea != null) {
            clientArea.x = bounds.x + border_width;
            clientArea.y = bounds.y + border_width;
            clientArea.width = bounds.width - 2 * border_width;
            clientArea.height = bounds.height - 2 * border_width;
        }
        int xthickness = OS.gtk_style_get_xthickness(gtkStyle);
        int interior_focus = theme.getWidgetProperty(buttonHandle, "interior-focus");
        if ((style & SWT.DROP_DOWN) != 0) {
            int arrow_width = ARROW_WIDTH;
            int arrow_height = ARROW_HEIGHT;
            int arrow_x = x + width - arrow_width - xthickness - focus_padding;
            if (interior_focus == 0) arrow_x -= focus_line_width;
            int arrow_y = y + (height - arrow_height) / 2;
            byte[] arrow_detail = Converter.wcsToMbcs(null, "arrow", true);
            OS.gtk_paint_arrow(gtkStyle, drawable, state_type, OS.GTK_SHADOW_NONE, null, theme.arrowHandle, arrow_detail, OS.GTK_ARROW_DOWN, true, arrow_x, arrow_y, arrow_width, arrow_height);
            if (clientArea != null) {
                clientArea.width -= bounds.x + bounds.width - arrow_x;
            }
        }
        if ((state & DrawData.FOCUSED) != 0) {
            int child_displacement_y = theme.getWidgetProperty(buttonHandle, "child-displacement-y");
            int child_displacement_x = theme.getWidgetProperty(buttonHandle, "child-displacement-x");
            int displace_focus = 0;
            if (OS.GTK_VERSION >= OS.VERSION(2, 6, 0)) {
                displace_focus = theme.getWidgetProperty(buttonHandle, "displace-focus");
            }
            if (interior_focus != 0) {
                int ythickness = OS.gtk_style_get_ythickness(gtkStyle);
                x += xthickness + focus_padding;
                y += ythickness + focus_padding;
                width -= 2 * (xthickness + focus_padding);
                height -= 2 * (ythickness + focus_padding);
            } else {
                x -= focus_line_width + focus_padding;
                y -= focus_line_width + focus_padding;
                width += 2 * (focus_line_width + focus_padding);
                height += 2 * (focus_line_width + focus_padding);
            }
            if ((state & (DrawData.PRESSED | DrawData.SELECTED)) != 0 && displace_focus != 0) {
                x += child_displacement_x;
                y += child_displacement_y;
            }
            OS.gtk_paint_focus(gtkStyle, drawable, state_type, null, buttonHandle, detail, x, y, width, height);
        }
    }
}