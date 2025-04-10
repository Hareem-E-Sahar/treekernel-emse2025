package com.xy.sframe.business.webentry;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.xy.sframe.component.exception.ReframeException;
import com.xy.sframe.component.log.LoggerFactory;
import com.xy.sframe.component.net.socket.SocketClient;
import com.xy.sframe.component.xml.Response;
import com.xy.sframe.component.xml.XMLDataObject;
import com.xy.sframe.frame.config.Constans;
import com.xy.sframe.frame.config.WebAction;
import com.xy.sframe.frame.config.WebActionResult;
import com.xy.sframe.frame.config.WebMapConfig;
import com.xy.sframe.frame.service.Deal;
import com.xy.sframe.frame.service.IModel;
import com.xy.sframe.frame.service.WebSet;

/**
 * Created on 2005-12-28 ��¼�����sevlet���ڵ�¼�����У����û��Ĺ�������ʼ����
 * 
 * @author chengang
 * @version 1.0
 */
public class WebEntryServlet extends HttpServlet {

    /**
     *  
     */
    public WebEntryServlet() {
        super();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setCharacterEncoding(Constans.REQUEST_CHARSET);
        resp.setContentType(Constans.RESPONSE_CONTENT_TYPE);
        String actionName = req.getServletPath().substring(req.getServletPath().lastIndexOf("/") + 1, req.getServletPath().lastIndexOf("."));
        WebAction action = (WebAction) WebMapConfig.webMap.get(actionName);
        if (action == null) {
            throw new ServletException("��webmap.xml��û���ҵ�action[" + actionName + "]");
        }
        Map params = req.getParameterMap();
        Map newParams = new HashMap();
        if ((params != null) && (params.size() > 0)) {
            Iterator iter = params.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = (String) entry.getKey();
                String value = ((String[]) entry.getValue())[0];
                newParams.put(name, value);
            }
        }
        req.setAttribute("request", newParams);
        HttpSession session = req.getSession(false);
        if (session == null) {
            session = req.getSession(true);
        }
        WebSet ws = new WebSet();
        ws.setRequest(req);
        ws.setResponse(resp);
        ws.setSession(session);
        String reqXml = null;
        String retXml = null;
        XMLDataObject xdo = XMLDataObject.parseMap(params, "argument");
        String argXml = xdo.toXML().toString();
        String sessionid = (String) newParams.get("sessionid");
        if (sessionid == null) {
            sessionid = "";
        }
        if (sessionid.equals("") && session.getAttribute("sessionid") != null) {
            sessionid = (String) session.getAttribute("sessionid");
        }
        reqXml = WebUtil.makeXdo(sessionid, "1001", action.getServiceName(), "", argXml).toXML().toString();
        ws.setReqXml(reqXml);
        if (action.getDealClass() != null && !action.getDealClass().trim().equals("")) {
            Deal deal = null;
            try {
                deal = (Deal) Class.forName(action.getDealClass()).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                throw new ServletException("dealclass���ô���[" + action.getDealClass() + "]");
            }
            deal.predeal(ws);
        }
        reqXml = ws.getReqXml();
        if (ws.getRsp() != null) {
            this.forward(req, resp, action, ws);
            return;
        }
        if (Constans.SFRAME_SERVLETADDRESS.length() > 0) {
            String surl = null;
            URL url;
            HttpURLConnection conn;
            if (actionName.equalsIgnoreCase("login")) {
                surl = Constans.SFRAME_SERVLETADDRESS + "/LoginServlet";
            } else if (actionName.equalsIgnoreCase("logout")) {
                surl = Constans.SFRAME_SERVLETADDRESS + "/LogoutServlet";
            } else {
                surl = Constans.SFRAME_SERVLETADDRESS + "/EntryServlet";
            }
            url = new URL(surl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(conn.getOutputStream()));
            out.write(reqXml);
            out.flush();
            out.close();
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (true) {
                byte[] bytes = new byte[1024];
                int read = is.read(bytes);
                if (read <= 0) break;
                baos.write(bytes, 0, read);
            }
            retXml = new String(baos.toByteArray(), Constans.RESPONSE_CHARSET);
            if (retXml.length() > 5 && !retXml.substring(0, 5).equalsIgnoreCase("<?xml")) {
                retXml = "<?xml version=\"1.0\" encoding=\"" + Constans.RESPONSE_CHARSET + "\"?>" + retXml;
            }
        } else if ((Constans.SFRAME_SOCKET_IP.length() > 0) && (Constans.SFRAME_SOCKET_PORT.length() > 0)) {
            if (reqXml.length() > 5 && !reqXml.substring(0, 5).equalsIgnoreCase("<?xml")) {
                reqXml = "<?xml version=\"1.0\" encoding=\"" + Constans.REQUEST_CHARSET + "\"?>" + reqXml;
            }
            XMLDataObject sockReqXdo = XMLDataObject.parseString(reqXml);
            sockReqXdo.insertLeaf("service.actionName");
            sockReqXdo.setItemValue("service.actionName", actionName);
            reqXml = sockReqXdo.toXML().toString();
            SocketClient client = new SocketClient(Constans.SFRAME_SOCKET_IP, Integer.valueOf(Constans.SFRAME_SOCKET_PORT).intValue());
            client.setReqCharset(Constans.REQUEST_CHARSET);
            client.setRespCharset(Constans.RESPONSE_CHARSET);
            try {
                retXml = client.request(reqXml);
            } catch (ReframeException e) {
                e.printStackTrace();
                throw new ServletException(e.getMessage());
            }
        } else {
            throw new ServletException("sframe�ӿڷ�ʽ���ô���");
        }
        LoggerFactory.getDefaultLog().debug("retXml : " + retXml);
        Response response = new Response(retXml);
        ws.setRsp(response);
        this.forward(req, resp, action, ws);
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, WebAction action, WebSet ws) throws ServletException, IOException {
        String cdata = String.valueOf(ws.getRsp().getArgCdataString());
        ws.setCdataXml(cdata);
        HashMap respMap = new HashMap();
        respMap.put("code", String.valueOf(ws.getRsp().getRtnCode()));
        respMap.put("message", ws.getRsp().getMessage());
        respMap.put("cdata", cdata);
        req.setAttribute("response", respMap);
        WebActionResult result = (WebActionResult) action.getResultMap().get(String.valueOf(ws.getRsp().getRtnCode()));
        String page = (String) result.getParamMap().get("location");
        if (result.getParamMap().get("model") != null && !((String) result.getParamMap().get("model")).equals("")) {
            IModel model = null;
            try {
                model = (IModel) Class.forName((String) result.getParamMap().get("model")).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                throw new ServletException("model���ô���[" + (String) result.getParamMap().get("model") + "]");
            }
            model.createModel(ws);
        }
        try {
            req.getRequestDispatcher(page).forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("����ҳ���location���ô���[" + page + "]");
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        doPost(req, resp);
    }
}
