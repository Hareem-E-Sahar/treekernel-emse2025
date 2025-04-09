package n2hell.http;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import n2hell.config.Config;
import n2hell.utils.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

public class RpcServlet extends TextServlet {

    private final Log log = LogFactory.getLog(RpcServlet.class);

    private static final long serialVersionUID = 1L;

    private static final Charset defaultCharset = Charset.forName("UTF-8");

    private final JSONRPC rpcService;

    private final Config config;

    private final HashMap<String, Class<? extends RpcService>> services;

    private final boolean useCompression;

    public RpcServlet(Config config, JSONRPC rpcService, HashMap<String, Class<? extends RpcService>> services) {
        this.config = config;
        this.rpcService = rpcService;
        this.services = services;
        useCompression = this.config.getHttp().getCompression();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            RpcService service = getService(request);
            responseText(request, response, useCompression, rpcService.generateAPI(service.getClass(), request.getServletPath() + request.getPathInfo()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            responseText(request, response, useCompression, new JSONRPCResult(JSONRPCResult.CODE_REMOTE_EXCEPTION, 0, e).toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonReq = null;
        try {
            InputStream is = request.getInputStream();
            String jsonString = FileUtils.read(is, defaultCharset.name());
            jsonReq = new JSONObject(jsonString);
            RpcService service = getService(request);
            String jsonResp = rpcService.call(jsonReq, service).toString();
            responseText(request, response, useCompression, jsonResp);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseText(request, response, useCompression, rpcService.error(jsonReq, e).toString());
        }
    }

    private RpcService getService(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession(true);
        String key = request.getPathInfo();
        RpcService service = (RpcService) session.getAttribute(key);
        if (service == null) {
            if (!services.containsKey(key)) throw new Exception("Invalid service key: " + key);
            Constructor<? extends RpcService> constructor = services.get(key).getConstructor(String.class, Config.class);
            service = constructor.newInstance(request.getRemoteUser(), config);
            session.setAttribute(key, service);
        }
        return service;
    }
}
