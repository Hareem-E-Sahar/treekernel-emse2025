package com.certesystems.swingforms.grid.actions;

import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.net.URI;
import org.apache.cayenne.Persistent;
import org.apache.commons.jxpath.JXPathContext;
import com.certesystems.swingforms.entity.Entity;
import com.certesystems.swingforms.grid.Grid;
import com.certesystems.swingforms.tools.Messages;

public class ActionBrowseSite extends GridAction {

    private String fieldMapping;

    public ActionBrowseSite() {
        super(LEVEL_NONE, Messages.getString("ActionBrowseSite.text"), "images/web-browser.png", KeyEvent.VK_HOME);
    }

    protected void doGridAction(Grid grid) throws Exception {
        Persistent reg = grid.getRegister();
        JXPathContext context = JXPathContext.newContext(reg);
        Object o = context.getValue(fieldMapping);
        Desktop.getDesktop().browse(new URI("http://" + o.toString()));
    }

    public int getActionGroup() {
        return GROUP_WEB;
    }

    public Entity getEntity(Object source) {
        Grid grid = (Grid) source;
        return grid.getEntity();
    }

    protected boolean isAllowed(Object source) {
        if (!Desktop.isDesktopSupported()) {
            return false;
        }
        if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            return false;
        }
        Grid grid = (Grid) source;
        Persistent reg = grid.getRegister();
        JXPathContext context = JXPathContext.newContext(reg);
        Object o = context.getValue(fieldMapping);
        return o != null;
    }

    public String getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(String fieldMapping) {
        this.fieldMapping = fieldMapping;
    }
}
