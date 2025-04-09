package org.monet.backoffice.presentation.user.renders;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.monet.backoffice.core.constants.ErrorCode;
import org.monet.kernel.exceptions.SystemException;
import org.monet.kernel.model.Field;
import org.monet.kernel.model.Node;
import org.monet.kernel.model.NodeLink;
import org.monet.kernel.model.NotificationList;
import org.monet.kernel.model.Task;
import org.monet.kernel.model.TaskList;

public class RendersFactory {

    private static RendersFactory instance;

    private HashMap<String, Object> renders;

    private RendersFactory() {
        this.renders = new HashMap<String, Object>();
        this.register("form_view", FormViewRender.class);
        this.register("form_page", FormPageRender.class);
        this.register("container_view", ContainerViewRender.class);
        this.register("container_page", ContainerPageRender.class);
        this.register("field_view", FieldViewRender.class);
    }

    public static synchronized RendersFactory getInstance() {
        if (instance == null) instance = new RendersFactory();
        return instance;
    }

    public Render get(Object object, String mode, NodeLink nodeLink) {
        Class<?> renderClass;
        Render render = null;
        String code = "";
        if (object instanceof Node) {
            Node node = (Node) object;
            if (node.isCatalog()) code = "catalog"; else if (node.isCollection()) code = "collection"; else if (node.isContainer()) code = "container"; else if (node.isDesktop()) code = "desktop"; else if (node.isDocument()) code = "document"; else if (node.isForm()) code = "form";
        } else if (object instanceof Field) code = "field"; else if (object instanceof Task) code = "task"; else if (object instanceof TaskList) code = "tasklist"; else if (object instanceof NotificationList) code = "notificationlist";
        if (code.isEmpty()) return null;
        code += (mode != null) ? mode : "";
        try {
            renderClass = (Class<?>) this.renders.get(code);
            Constructor<?> constructor = renderClass.getConstructor(NodeLink.class);
            render = (Render) constructor.newInstance(nodeLink);
        } catch (NullPointerException oException) {
            throw new SystemException(ErrorCode.RENDERS_FACTORY, code, oException);
        } catch (Exception oException) {
            throw new SystemException(ErrorCode.RENDERS_FACTORY, code, oException);
        }
        return render;
    }

    public Boolean register(String code, Class<?> renderClass) throws IllegalArgumentException {
        if ((renderClass == null) || (code == null)) {
            return false;
        }
        this.renders.put(code, renderClass);
        return true;
    }
}
