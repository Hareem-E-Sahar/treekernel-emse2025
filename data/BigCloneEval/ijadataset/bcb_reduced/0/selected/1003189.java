package uit.server.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import uit.comm.Constants;
import uit.comm.util.RequestUtil;
import uit.comm.util.StringUtil;
import uit.server.arcims.ArcXML;
import uit.server.model.LayerDef;
import uit.server.model.Model;
import uit.server.model.RequestMapObject;
import uit.server.model.ResponseMapObject;
import uit.upis.model.Layer;
import com.esri.aims.mtier.model.envelope.Envelope;

public class RequestMapHelper {

    protected static final Logger log = Logger.getLogger(RequestMapHelper.class);

    public RequestMapObject setRequestMapObject(HttpServletRequest request) {
        List<LayerDef> visibleLayerList = getSelectLayers(request);
        int width = RequestUtil.getIntegerRequest(request, "screenWidth", 0);
        int height = RequestUtil.getIntegerRequest(request, "screenHeight", 0);
        double minX = RequestUtil.getDoubleRequest(request, "minX");
        double minY = RequestUtil.getDoubleRequest(request, "minY");
        double maxX = RequestUtil.getDoubleRequest(request, "maxX");
        double maxY = RequestUtil.getDoubleRequest(request, "maxY");
        int MOVE_ACTION = RequestUtil.getIntegerRequest(request, "move");
        long directionParm = -1;
        long scaleFactor = RequestUtil.getLongRequest(request, "scaleFator", 0);
        int mapMode = -1;
        if (width > 0 && minX == 0) {
            if (MOVE_ACTION == 0) mapMode = RequestMapObject.DO_ZOOM_FULL_EXTENT; else if (MOVE_ACTION == 1) {
                mapMode = RequestMapObject.DO_PAN_MOVE;
                String direction = RequestUtil.getRequest(request, "direction", "NORTH");
                if (direction.equals("NORTH")) {
                    directionParm = com.esri.aims.mtier.model.map.Map.NORTH;
                } else if (direction.equals("EAST")) {
                    directionParm = com.esri.aims.mtier.model.map.Map.EAST;
                } else if (direction.equals("SOUTH")) {
                    directionParm = com.esri.aims.mtier.model.map.Map.SOUTH;
                } else if (direction.equals("WEST")) {
                    directionParm = com.esri.aims.mtier.model.map.Map.WEST;
                }
            } else {
                mapMode = RequestMapObject.DO_SCALE_FACTOR;
            }
        } else if (minX > 0) mapMode = RequestMapObject.DO_ZOOM_ENVEOPE; else {
            mapMode = RequestMapObject.DO_REFRESH_ACTION;
        }
        RequestMapObject req = new RequestMapObject(mapMode);
        req.setWidth(width);
        req.setHeight(height);
        req.setVisibleLayerList(visibleLayerList);
        Envelope zoomEnvelope = new Envelope();
        zoomEnvelope.setMinX(minX);
        zoomEnvelope.setMinY(minY);
        zoomEnvelope.setMaxX(maxX);
        zoomEnvelope.setMaxY(maxY);
        req.setZoomEnvelope(zoomEnvelope);
        req.setDirection(directionParm);
        req.setLevel(scaleFactor);
        return req;
    }

    private int setMapHistory(HttpSession session, ResponseMapObject mapObject) {
        String indexStr = null;
        Envelope envelope = new Envelope();
        envelope.setMaxX(mapObject.getEnvelope().getMaxX());
        envelope.setMaxY(mapObject.getEnvelope().getMaxY());
        envelope.setMinX(mapObject.getEnvelope().getMinX());
        envelope.setMinY(mapObject.getEnvelope().getMinY());
        Map hashMap = (Map) session.getAttribute(Constants.USER_MAP_HISTORY);
        if (hashMap == null) {
            hashMap = Collections.synchronizedMap(new HashMap());
        }
        indexStr = String.valueOf(hashMap.size());
        hashMap.put(indexStr, envelope);
        session.setAttribute(Constants.USER_MAP_HISTORY, hashMap);
        return Integer.parseInt(indexStr);
    }

    public Envelope getMapHistory(HttpSession session, int index) {
        Envelope envelope = new Envelope();
        Map<String, Envelope> hashMap = (Map<String, Envelope>) session.getAttribute(Constants.USER_MAP_HISTORY);
        if (hashMap != null) {
            if (hashMap.containsKey(index + "")) {
                envelope = (Envelope) hashMap.get(index + "");
            }
        }
        return envelope;
    }

    public int getMapHistoryIndex(HttpSession session) {
        Map<String, ResponseMapObject> hashMap = new HashMap<String, ResponseMapObject>((Map<String, ResponseMapObject>) session.getAttribute(Constants.USER_MAP_HISTORY));
        return hashMap != null ? hashMap.size() : 0;
    }

    public String generateMapObjToXML(ResponseMapObject mapObject) {
        StringBuffer temp = new StringBuffer();
        temp.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        temp.append("<ARCXML version=\"1.1\">");
        temp.append("<RESPONSE>");
        temp.append("<IMAGE>");
        temp.append("<ENVELOPE minx=\"" + mapObject.getEnvelope().getMinX() + "\" miny=\"" + mapObject.getEnvelope().getMinY() + "\" maxx=\"" + mapObject.getEnvelope().getMaxX() + "\" maxy=\"" + mapObject.getEnvelope().getMaxY() + "\" />");
        temp.append("<OUTPUT url=\"" + mapObject.getURL() + "\"  scale=\"" + mapObject.getScale() + "\"  />  ");
        temp.append("</IMAGE>  ");
        temp.append("</RESPONSE>");
        temp.append("</ARCXML> ");
        return temp.toString();
    }

    public String generateMapObjToXML(HttpServletRequest request, ResponseMapObject mapObject) {
        HttpSession session = request.getSession(false);
        int histPosIndex = RequestUtil.getIntegerRequest(request, "mapIndex", -1);
        int histIdx = histPosIndex == -1 ? setMapHistory(session, mapObject) : getMapHistoryIndex(session);
        StringBuffer temp = new StringBuffer();
        temp.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        temp.append("<ARCXML version=\"1.1\">");
        temp.append("<RESPONSE>");
        temp.append("<IMAGE>");
        temp.append("<ENVELOPE minx=\"" + mapObject.getEnvelope().getMinX() + "\" miny=\"" + mapObject.getEnvelope().getMinY() + "\" maxx=\"" + mapObject.getEnvelope().getMaxX() + "\" maxy=\"" + mapObject.getEnvelope().getMaxY() + "\" />");
        temp.append("<OUTPUT url=\"" + mapObject.getURL() + "\"  scale=\"" + mapObject.getScale() + "\" />  ");
        temp.append("<HISTORY index=\"" + histIdx + "\"  histPosIndex=\"" + histPosIndex + "\" />  ");
        temp.append("</IMAGE>  ");
        temp.append("</RESPONSE>");
        temp.append("</ARCXML> ");
        return temp.toString();
    }

    private List<LayerDef> convertLayerList(List<Layer> visibleLayer) {
        List<LayerDef> covertVisibleLayerVOlist = new ArrayList<LayerDef>();
        for (Iterator iter = visibleLayer.iterator(); iter.hasNext(); ) {
            Layer layer = (Layer) iter.next();
            LayerDef layerDef = new LayerDef();
            layerDef.setLayerId(layer.getId() + "");
            layerDef.setLayerName(layer.getName());
            layerDef.setVisible(layer.getVisible().equals("F") ? "false" : "true");
            covertVisibleLayerVOlist.add(layerDef);
        }
        return covertVisibleLayerVOlist;
    }

    public List<LayerDef> getSelectLayers(HttpServletRequest request) {
        List<Layer> layerList = new ArrayList<Layer>();
        int layerId = StringUtil.stoi(request.getParameter("layerId"));
        boolean checked = StringUtil.parseBoolean(RequestUtil.getRequest(request, "checked", "false"));
        HttpSession session = request.getSession(false);
        List userSessionLayerList = (List) session.getAttribute(Constants.LAYER_LIST);
        int layerListsize = userSessionLayerList != null ? userSessionLayerList.size() : 0;
        for (int i = 0; i < layerListsize; i++) {
            Layer layer = (Layer) userSessionLayerList.get(i);
            if (layer.getId() == layerId) {
                layer.setChecked(checked ? "T" : "F");
            }
            layerList.add(layer);
        }
        if (userSessionLayerList != null) {
            session.setAttribute(Constants.LAYER_LIST, userSessionLayerList);
            layerList = LayerUtil.getOrderLayer(layerList);
        }
        return convertLayerList(layerList);
    }

    /**
	 * request���� ��ǥ������ model��ü�� setting�Ѵ�.
	 * @param request
	 * @param model
	 * @return
	 */
    public Model setModel(HttpServletRequest request, Model model) throws Exception {
        model.setSerivce("UPIS3");
        int screenWidth = RequestUtil.getIntegerRequest(request, "screenWidth", 1107);
        int screenHeight = RequestUtil.getIntegerRequest(request, "screenHeight", 706);
        model.setWindowWidth(screenWidth);
        model.setWindowHeight(screenHeight);
        model = ArcXML.getInitModel(model);
        int layerId = StringUtil.stoi(request.getParameter("layerId"));
        boolean checked = StringUtil.parseBoolean(RequestUtil.getRequest(request, "checked", "false"));
        HttpSession session = request.getSession();
        List layerList = (List) session.getAttribute(Constants.LAYER_LIST);
        int layerListsize = layerList != null ? layerList.size() : 0;
        for (int i = 0; i < layerListsize; i++) {
            Layer layer = (Layer) layerList.get(i);
            if (layer.getId() == layerId) layer.setChecked(checked ? "T" : "F");
        }
        if (layerList != null) {
            session.setAttribute(Constants.LAYER_LIST, layerList);
            List list = LayerUtil.getOrderLayer(layerList);
            model.setTempLayer(list);
        }
        int level = StringUtil.stoi(request.getParameter("scaleLevel"), 13);
        model.setScaleLevel(level);
        model.setScale(StringUtil.parseDouble(request.getParameter("scale")));
        if (request.getParameter("maxX") != null) {
            Envelope envelope = model.getEnvelope();
            envelope.setMaxX(RequestUtil.getDoubleRequest(request, "maxX").doubleValue());
            envelope.setMaxY(RequestUtil.getDoubleRequest(request, "maxY").doubleValue());
            envelope.setMinX(RequestUtil.getDoubleRequest(request, "minX").doubleValue());
            envelope.setMinY(RequestUtil.getDoubleRequest(request, "minY").doubleValue());
            model.setEnvelope(envelope);
        }
        return model;
    }

    /**
	 * request���� ��ǥ������ model��ü�� setting�Ѵ�.
	 * @param request
	 * @param model
	 * @return
	 */
    public Model setModel(Model model) throws Exception {
        model.setSerivce("UPIS3");
        int screenWidth = 1107;
        int screenHeight = 706;
        model.setWindowWidth(screenWidth);
        model.setWindowHeight(screenHeight);
        model = ArcXML.getInitModel(model, ArcXML.getAxlDocument(model.getSerivce()));
        return model;
    }

    /**
	 * ��ô ������ �� envelope��ü�� �����Ѵ�.
	 * @param request
	 * @param model
	 * @return
	 */
    public Model changeEnvelope(HttpServletRequest request, Model model) {
        int level = RequestUtil.getIntegerRequest(request, "scaleLevel", 13);
        double scale = 0;
        if (level == 13) scale = RequestUtil.getDoubleRequest(request, "newScale").doubleValue(); else scale = ScaleUtil.getScaleValue(level);
        log.debug("level----------" + level);
        log.debug("scale----------" + scale);
        model.setEnvelope(getEnvelopeNewScale(model, scale));
        return model;
    }

    private double[] getCenterPoint(Envelope envelope) {
        double[] xy = new double[2];
        double oldMinX = envelope.getMinX();
        double oldMinY = envelope.getMinY();
        double oldMaxX = envelope.getMaxX();
        double oldMaxY = envelope.getMaxY();
        double mapX = (oldMinX + oldMaxX) / 2;
        double mapY = (oldMinY + oldMaxY) / 2;
        xy[0] = mapX;
        xy[1] = mapY;
        return xy;
    }

    private Envelope getEnvelopeNewScale(Model model, double scale) {
        Envelope envelope = new Envelope();
        double mapX = getCenterPoint(model.getEnvelope())[0];
        double mapY = getCenterPoint(model.getEnvelope())[1];
        log.debug("center " + mapX + " , " + mapY);
        log.debug("scale " + model.getScale());
        log.debug("POINT  MAX X  " + model.getEnvelope().getMaxX() + " , " + model.getEnvelope().getMaxY());
        log.debug("POINT  MIN Y" + model.getEnvelope().getMinX() + " , " + model.getEnvelope().getMinY());
        double minX = (mapX - (mapX - model.getEnvelope().getMinX()) * scale / model.getScale());
        double minY = (mapY - (mapY - model.getEnvelope().getMinY()) * scale / model.getScale());
        double maxX = (mapX + (model.getEnvelope().getMaxX() - mapX) * scale / model.getScale());
        double maxY = (mapY + (model.getEnvelope().getMaxY() - mapY) * scale / model.getScale());
        log.debug("POINT  MAX X  " + maxX + " , " + maxY);
        log.debug("POINT  MIN Y" + minX + " , " + minY);
        envelope.setMaxX(maxX);
        envelope.setMaxY(maxY);
        envelope.setMinX(minX);
        envelope.setMinY(minY);
        return envelope;
    }
}
