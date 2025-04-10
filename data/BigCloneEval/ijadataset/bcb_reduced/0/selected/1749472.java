package org.jenia.faces.datatools.renderkit.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.jenia.faces.datatools.component.html.HtmlMultipleRowsSelector;

/**
 * @author Andrea Tessaro Porta
 */
public class MultipleRowsSelectorRenderer extends DataToolsRenderer {

    public void decode(FacesContext context, UIComponent component) {
        HtmlMultipleRowsSelector selector = (HtmlMultipleRowsSelector) component;
        if (selector.getDisabled()) return;
        UIData data = (UIData) getMyDatTable(context, selector);
        if (data == null) {
            throw new RuntimeException("JSF dataTable needed to use this component");
        }
        Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();
        String sel = (String) parameters.get(selector.getClientId(context));
        Collection<Object> c = (Collection<Object>) selector.getSelectionList();
        if (c == null) {
            c = new ArrayList<Object>();
            selector.setSelectionList(c);
        }
        if (sel == null) {
            selector.addFacesListener(selector.getMultipleRowsSelectorListener());
            HtmlMultipleRowsSelector.MultipleRowsSelectorRemoveEvent event = selector.new MultipleRowsSelectorRemoveEvent(component, data.getRowData());
            selector.queueEvent(event);
        } else {
            int selected = -1;
            try {
                selected = Integer.parseInt(sel);
            } catch (Exception e) {
                return;
            }
            if (selected == data.getRowIndex()) {
                selector.addFacesListener(selector.getMultipleRowsSelectorListener());
                HtmlMultipleRowsSelector.MultipleRowsSelectorAddEvent event = selector.new MultipleRowsSelectorAddEvent(component, data.getRowData());
                selector.queueEvent(event);
            }
        }
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (!component.isRendered()) {
            return;
        }
        HtmlMultipleRowsSelector selector = (HtmlMultipleRowsSelector) component;
        UIData data = (UIData) getMyDatTable(context, selector);
        ResponseWriter rw = context.getResponseWriter();
        rw.startElement("input", selector);
        rw.writeAttribute("type", "checkbox", null);
        rw.writeAttribute("name", selector.getClientId(context), null);
        rw.writeAttribute("value", "" + data.getRowIndex(), null);
        Collection<Object> c = (Collection<Object>) selector.getSelectionList();
        if (c == null) {
            c = new ArrayList<Object>();
            selector.setSelectionList(c);
        }
        if (c.contains(data.getRowData())) rw.writeAttribute("checked", "checked", null);
        String accesskey = (String) selector.getAccesskey();
        if (accesskey != null) {
            rw.writeAttribute("accesskey", accesskey, "accesskey");
        }
        String border = (String) selector.getBorder();
        if (border != null) {
            rw.writeAttribute("border", border, "border");
        }
        String dir = (String) selector.getDir();
        if (dir != null) {
            rw.writeAttribute("dir", dir, "dir");
        }
        boolean disabled = selector.getDisabled();
        if (disabled) {
            rw.writeAttribute("disabled", "disabled", "disabled");
        }
        String cl = selector.getStyleClass();
        if (disabled) {
            cl = cl + selector.getDisabledClass();
        } else {
            cl = cl + selector.getEnabledClass();
        }
        if (cl != null && !cl.equals("")) {
            rw.writeAttribute("class", cl, "labelClass");
        }
        String lang = (String) selector.getLang();
        if (lang != null) {
            rw.writeAttribute("lang", lang, "lang");
        }
        String layout = (String) selector.getLayout();
        if (layout != null) {
            rw.writeAttribute("layout", layout, "layout");
        }
        String onblur = (String) selector.getOnblur();
        if (onblur != null) {
            rw.writeAttribute("onblur", onblur, "onblur");
        }
        String onchange = (String) selector.getOnchange();
        if (onchange != null) {
            rw.writeAttribute("onchange", onchange, "onchange");
        }
        String onclick = (String) selector.getOnclick();
        if (onclick != null) {
            rw.writeAttribute("onclick", onclick, "onclick");
        }
        String ondblclick = (String) selector.getOndblclick();
        if (ondblclick != null) {
            rw.writeAttribute("ondblclick", ondblclick, "ondblclick");
        }
        String onfocus = (String) selector.getOnfocus();
        if (onfocus != null) {
            rw.writeAttribute("onfocus", onfocus, "onfocus");
        }
        String onkeydown = (String) selector.getOnkeydown();
        if (onkeydown != null) {
            rw.writeAttribute("onkeydown", onkeydown, "onkeydown");
        }
        String onkeypress = (String) selector.getOnkeypress();
        if (onkeypress != null) {
            rw.writeAttribute("onkeypress", onkeypress, "onkeypress");
        }
        String onkeyup = (String) selector.getOnkeyup();
        if (onkeyup != null) {
            rw.writeAttribute("onkeyup", onkeyup, "onkeyup");
        }
        String onmousedown = (String) selector.getOnmousedown();
        if (onmousedown != null) {
            rw.writeAttribute("onmousedown", onmousedown, "onmousedown");
        }
        String onmousemove = (String) selector.getOnmousemove();
        if (onmousemove != null) {
            rw.writeAttribute("onmousemove", onmousemove, "onmousemove");
        }
        String onmouseout = (String) selector.getOnmouseout();
        if (onmouseout != null) {
            rw.writeAttribute("onmouseout", onmouseout, "onmouseout");
        }
        String onmouseover = (String) selector.getOnmouseover();
        if (onmouseover != null) {
            rw.writeAttribute("onmouseover", onmouseover, "onmouseover");
        }
        String onmouseup = (String) selector.getOnmouseup();
        if (onmouseup != null) {
            rw.writeAttribute("onmouseup", onmouseup, "onmouseup");
        }
        String onselect = (String) selector.getOnselect();
        if (onselect != null) {
            rw.writeAttribute("onselect", onselect, "onselect");
        }
        Boolean readonly = selector.getReadonlyObject();
        if (readonly != null) {
            rw.writeAttribute("readonly", readonly.toString(), "readonly");
        }
        String style = (String) selector.getStyle();
        if (style != null) {
            rw.writeAttribute("style", style, "style");
        }
        String tabindex = (String) selector.getTabindex();
        if (tabindex != null) {
            rw.writeAttribute("tabindex", tabindex, "tabindex");
        }
        String title = (String) selector.getTitle();
        if (title != null) {
            rw.writeAttribute("title", title, "title");
        }
        rw.endElement("input");
    }

    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        return;
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        return;
    }
}
