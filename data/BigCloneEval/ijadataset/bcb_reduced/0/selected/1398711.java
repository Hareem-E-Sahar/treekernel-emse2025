package org.swemas.rendering.common.json;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.swemas.core.ModuleNotFoundException;
import org.swemas.core.event.IEventDispatchingChannel;
import org.swemas.core.kernel.IKernel;
import org.swemas.core.messaging.ILocaleProvidingChannel;
import org.swemas.data.xml.XmlException;
import org.swemas.rendering.ErrorCode;
import org.swemas.rendering.RenderingEvent;
import org.swemas.rendering.RenderingException;
import org.swemas.rendering.SwFlowRenderingModule;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author ray
 * 
 */
public class SwJsonPairRenderer extends SwFlowRenderingModule implements IJsonPairRenderingChannel {

    /**
	 * @param kernel
	 * @throws InvocationTargetException
	 */
    public SwJsonPairRenderer(IKernel kernel) throws InvocationTargetException {
        super(kernel);
    }

    @Override
    public Map<String, Map<String, List<Node>>> render(Map<String, Map<String, List<Node>>> iflow, Map<String, String> params, Node init, HttpServletRequest request) throws RenderingException {
        String stm = "";
        try {
            Map<String, List<Node>> istream;
            if (iflow.containsKey(stm)) istream = iflow.get(stm); else istream = new HashMap<String, List<Node>>();
            Map<String, List<Node>> ostream = new HashMap<String, List<Node>>();
            List<Node> nodes = new ArrayList<Node>();
            Document doc;
            if (init != null) doc = init.getOwnerDocument(); else doc = ixml().create();
            List<Node> chnodes = istream.get("");
            Node key = chnodes.get(0);
            Node value = chnodes.get(1);
            if (key != null) {
                Node k = doc.createElement("key");
                appendChild(k, doc.createTextNode(key.getNodeValue()));
                Node v = doc.createElement("value");
                appendChild(v, doc.createTextNode(value == null ? "" : value.getNodeValue()));
                Node pair = doc.createElement("pair");
                appendChild(pair, k);
                appendChild(pair, v);
                nodes.add(pair);
            }
            ostream.put("", nodes);
            Map<String, Map<String, List<Node>>> oflow = new HashMap<String, Map<String, List<Node>>>();
            oflow.put(stm, ostream);
            for (String stmname : iflow.keySet()) if (!stmname.equals(stm)) oflow.put(stmname, iflow.get(stmname));
            return oflow;
        } catch (XmlException e) {
            try {
                IEventDispatchingChannel ed = (IEventDispatchingChannel) kernel().getChannel(IEventDispatchingChannel.class);
                ed.event(new RenderingEvent(new RenderingException(e, name(((ILocaleProvidingChannel) kernel().getChannel(ILocaleProvidingChannel.class)).getCurrentLocale()), ErrorCode.InternalError.getCode())));
            } catch (ModuleNotFoundException m) {
                try {
                    IEventDispatchingChannel ed = (IEventDispatchingChannel) kernel().getChannel(IEventDispatchingChannel.class);
                    ed.event(new RenderingEvent(new RenderingException(e, name(new Locale("en", "US")), ErrorCode.InternalError.getCode())));
                } catch (ModuleNotFoundException m1) {
                }
            }
            iflow.remove(stm);
            return new HashMap<String, Map<String, List<Node>>>(iflow);
        } catch (DOMException e) {
            try {
                IEventDispatchingChannel ed = (IEventDispatchingChannel) kernel().getChannel(IEventDispatchingChannel.class);
                ed.event(new RenderingEvent(new RenderingException(e, name(((ILocaleProvidingChannel) kernel().getChannel(ILocaleProvidingChannel.class)).getCurrentLocale()), ErrorCode.ResultComposingError.getCode())));
            } catch (ModuleNotFoundException m) {
                try {
                    IEventDispatchingChannel ed = (IEventDispatchingChannel) kernel().getChannel(IEventDispatchingChannel.class);
                    ed.event(new RenderingEvent(new RenderingException(e, name(new Locale("en", "US")), ErrorCode.ResultComposingError.getCode())));
                } catch (ModuleNotFoundException m1) {
                }
            }
            iflow.remove(stm);
            return new HashMap<String, Map<String, List<Node>>>(iflow);
        }
    }

    @Override
    protected Map<String, String> parseInit(Node init) throws RenderingException {
        return new HashMap<String, String>();
    }
}
