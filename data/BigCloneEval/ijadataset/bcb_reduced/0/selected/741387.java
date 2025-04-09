package uit.server.arcims;

import java.util.Iterator;
import java.util.List;
import uit.server.ImageServiceIF;
import uit.server.model.LayerDef;
import uit.server.model.Model;
import uit.server.model.RequestMapObject;
import uit.server.model.ResponseMapObject;
import uit.server.util.ScaleUtil;
import com.esri.aims.mtier.model.acetate.Acetate;
import com.esri.aims.mtier.model.acetate.Line;
import com.esri.aims.mtier.model.acetate.NorthArrow;
import com.esri.aims.mtier.model.acetate.Point;
import com.esri.aims.mtier.model.acetate.Polygon;
import com.esri.aims.mtier.model.acetate.ScaleBar;
import com.esri.aims.mtier.model.acetate.Shape;
import com.esri.aims.mtier.model.envelope.Envelope;
import com.esri.aims.mtier.model.map.Layers;
import com.esri.aims.mtier.model.map.Map;
import com.esri.aims.mtier.model.map.layer.AcetateLayer;
import com.esri.aims.mtier.model.map.layer.FeatureLayer;
import com.esri.aims.mtier.model.map.layer.ImageLayer;
import com.esri.aims.mtier.model.map.layer.Layer;
import com.esri.aims.mtier.model.map.layer.renderer.symbol.SimpleLineSymbol;
import com.esri.aims.mtier.model.map.layer.renderer.symbol.SimpleMarkerSymbol;
import com.esri.aims.mtier.model.map.layer.renderer.symbol.SimplePolygonSymbol;
import com.esri.aims.mtier.model.map.layer.renderer.symbol.Symbol;

public class ArcIMSImageService implements ImageServiceIF {

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("finallize!!!!!!");
    }

    public Map map;

    public long width;

    public long height;

    public String servicename;

    /**
	 * Default Constructor
	 * UPIS3 
	 * @throws Exception
	 */
    public ArcIMSImageService() throws Exception {
        this.servicename = "UPIS3_NEW";
        this.width = 0;
        this.height = 0;
        this.map = ArcIMSServer.getMapInstance(this.servicename);
    }

    public void initMapSize(long width, long height) throws Exception {
        this.width = width;
        this.height = height;
    }

    private FeatureLayer getFeatureLayer(int id) {
        Layers layers = map.getLayers();
        FeatureLayer featureLayer = null;
        for (Iterator iter = layers.getLayersCollection().iterator(); iter.hasNext(); ) {
            Layer layer = (Layer) iter.next();
            if (layer.getType().equals("Feature")) {
                featureLayer = (FeatureLayer) layer;
                if (featureLayer.getID().equals(id + "")) {
                    featureLayer = (FeatureLayer) layer;
                    break;
                }
            }
        }
        return featureLayer;
    }

    private String getAxlRequest() {
        return map.getArcXML();
    }

    private String getAxlResponse(String axlRequest) {
        return map.sendArcXML(axlRequest, Map.GET_IMAGE);
    }

    /**
	 * request xml�� ��û�Ͽ� reqeust xml�� ���Ѵ�.
	 *  <p>
	 * ���̾��Ʈ�� �� xml�� ���Ѵ�. 
	 *  </p>
	 * @param serviceName
	 * @return
	 */
    private String getAxlRequest(List<LayerDef> LayerDefList) {
        String requestXMLString = "";
        getAxlRequest();
        try {
            requestXMLString = ArcXML.getRemoveLayerXML(ArcXML.parsingToDocment(getAxlRequest()), LayerDefList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requestXMLString;
    }

    public Model getMapImage(Model model) throws Exception {
        String requestXMLString = "";
        if (model.getExceptLayer() != null) {
            requestXMLString = getAxlRequest(model.getExceptLayer());
        } else requestXMLString = getAxlRequest();
        model.setRequestArcXML(requestXMLString);
        model = ArcXML.updateModel(model);
        String axlRespoonse = getAxlResponse(model.getRequestArcXML());
        model.setResponseArcXML(axlRespoonse);
        return ArcXML.getArcXMLResponse(model);
    }

    public Layers visibleLayers(List<LayerDef> visibleLayerList) {
        Layers layers = map.getLayers();
        for (Iterator<Layer> iter = layers.getLayersCollection().iterator(); iter.hasNext(); ) {
            Layer layer = (Layer) iter.next();
            boolean isVisible = false;
            for (Iterator iterator = visibleLayerList.iterator(); iterator.hasNext(); ) {
                LayerDef layerDef = (LayerDef) iterator.next();
                if (layer.getID().equals(layerDef.getLayerId())) {
                    iterator.remove();
                    isVisible = true;
                }
            }
            if (isVisible) {
                System.out.println(layer.getID() + " in service true ");
                layer.setVisible(true);
            } else {
                if (layer.getName().equals("AcetateLayer") || layer.getName().equals("HighlightLayer")) {
                    layer.setVisible(true);
                } else {
                    layer.setVisible(false);
                }
            }
        }
        return layers;
    }

    public ResponseMapObject getImageMap(RequestMapObject p) throws Exception {
        map.setWidth(p.getWidth());
        map.setHeight(p.getHeight());
        switch(p.getAction()) {
            case RequestMapObject.DO_ZOOM_FULL_EXTENT:
                visibleLayers(p.getVisibleLayerList());
                break;
            case RequestMapObject.DO_ZOOM_SELECTION:
                visibleLayers(p.getVisibleLayerList());
                break;
            case RequestMapObject.DO_PAN_ACTION:
                visibleLayers(p.getVisibleLayerList());
                doPan(p.getX(), p.getY());
                break;
            case RequestMapObject.DO_REFRESH_ACTION:
                doRefresh(p.getVisibleLayerList());
                break;
            case RequestMapObject.DO_ZOOM_ENVEOPE:
                visibleLayers(p.getVisibleLayerList());
            case RequestMapObject.DO_ZOOM_LEVEL:
                visibleLayers(p.getVisibleLayerList());
            default:
                doRefresh(p.getVisibleLayerList());
                break;
        }
        return getMapOut();
    }

    public ResponseMapObject doZoomSelection(List<LayerDef> visibleLayerList, int selectionActiveLayer, String whereExpression, boolean zoomSelection, boolean highlightSelection) throws Exception {
        map.reset();
        FeatureLayer featureLayer = getFeatureLayer(selectionActiveLayer);
        Symbol symbol = null;
        if (highlightSelection) symbol = getSymbol(featureLayer);
        map.displayFeatures(featureLayer, whereExpression, zoomSelection, symbol);
        map.refresh();
        ResponseMapObject response = new ResponseMapObject();
        response.setURL(map.getMapOutput().getURL());
        response.setEnvelope(map.getEnvelope());
        response.setScale(ScaleUtil.getScale(map.getWidth(), map.getHeight(), map.getEnvelope()));
        return response;
    }

    public ResponseMapObject doZoomSelection(List<LayerDef> visibleLayerList, Shape shape, boolean highlightSelection) throws Exception {
        map.reset();
        map.setWidth(this.width);
        map.setHeight(this.height);
        if (highlightSelection) addAcetateLayer(shape);
        map.refresh();
        ResponseMapObject response = new ResponseMapObject();
        response.setURL(map.getMapOutput().getURL());
        response.setEnvelope(map.getEnvelope());
        response.setScale(ScaleUtil.getScale(map.getWidth(), map.getHeight(), map.getEnvelope()));
        return response;
    }

    public ResponseMapObject doZoomSelection(List<LayerDef> visibleLayerList, int selectionActiveLayer, Shape shape, Envelope shapeEnvelope) throws Exception {
        map.reset();
        map.setWidth(this.width);
        map.setHeight(this.height);
        visibleLayers(visibleLayerList);
        Envelope env = coordinatePNU(map.getEnvelope(), shapeEnvelope);
        FeatureLayer featureLayer = getFeatureLayer(selectionActiveLayer);
        Symbol symbol = null;
        symbol = getSymbol(featureLayer);
        map.displayFeatures(featureLayer, shape, true, symbol);
        map.setEnvelope(env);
        map.refresh();
        ResponseMapObject response = new ResponseMapObject();
        response.setURL(map.getMapOutput().getURL());
        response.setEnvelope(map.getEnvelope());
        response.setScale(ScaleUtil.getScale(map.getWidth(), map.getHeight(), map.getEnvelope()));
        return response;
    }

    public ResponseMapObject doZoom(List<LayerDef> visibleLayerList, Point point, boolean highlightSelection) throws Exception {
        map.reset();
        map.setWidth(this.width);
        map.setHeight(this.height);
        visibleLayers(visibleLayerList);
        double x = point.getX();
        double y = point.getY();
        double scale = .3175;
        double wWidth = scale * map.getWidth();
        double wHeight = scale * map.getHeight();
        double eLeft = x - (wWidth * scale);
        double eRight = x + (wWidth * scale);
        double eTop = y + (wHeight * scale);
        double eBottom = y - (wHeight * scale);
        Envelope env = new Envelope();
        env.setMinX(eLeft);
        env.setMinY(eBottom);
        env.setMaxX(eRight);
        env.setMaxY(eTop);
        map.setEnvelope(env);
        if (highlightSelection) addAcetateLayer(point);
        map.refresh();
        ResponseMapObject response = new ResponseMapObject();
        response.setURL(map.getMapOutput().getURL());
        response.setEnvelope(map.getEnvelope());
        response.setScale(ScaleUtil.getScale(map.getWidth(), map.getHeight(), map.getEnvelope()));
        return response;
    }

    public ResponseMapObject doZoomToFullExtent(List<LayerDef> visibleLayerList) throws Exception {
        map.reset();
        map.setWidth(this.width);
        map.setHeight(this.height);
        visibleLayers(visibleLayerList);
        map.getLayers().setOrder(true);
        for (Iterator iter = map.getLayers().getLayersCollection().iterator(); iter.hasNext(); ) {
            Layer layer = (Layer) iter.next();
            if (layer.getType().equals("Feature")) {
                System.out.println(layer.getMaxScale() + layer.getName() + " :" + layer.getID() + " isVisible  :" + layer.isVisible());
            } else {
                System.out.println(layer + " : " + layer.getName() + " :" + layer.getID());
            }
        }
        System.out.println(map.getLayers().getCount() + " layer count  arcxml");
        map.doZoomToFullExtent();
        map.refresh();
        System.out.println(ArcXML.readArcXML(map.getArcXML()) + " aaaa ");
        System.out.println(map.getMapOutput().getURL());
        System.out.println(ScaleUtil.getScale(map.getWidth(), map.getHeight(), map.getEnvelope()));
        ResponseMapObject response = new ResponseMapObject();
        response.setURL(map.getMapOutput().getURL());
        response.setEnvelope(map.getEnvelope());
        response.setScale(ScaleUtil.getScale(map.getWidth(), map.getHeight(), map.getEnvelope()));
        return response;
    }

    public ResponseMapObject doZoom(List<LayerDef> visibleLayerList, Envelope zoomEnvelope) throws Exception {
        map.setWidth(this.width);
        map.setHeight(this.height);
        visibleLayers(visibleLayerList);
        map.doZoomToExtent(zoomEnvelope);
        map.refresh();
        ResponseMapObject response = new ResponseMapObject();
        response.setURL(map.getMapOutput().getURL());
        response.setEnvelope(map.getEnvelope());
        response.setScale(ScaleUtil.getScale(map.getWidth(), map.getHeight(), map.getEnvelope()));
        return response;
    }

    public ResponseMapObject doRefresh(List<LayerDef> visibleLayerList) throws Exception {
        map.setWidth(this.width);
        map.setHeight(this.height);
        visibleLayers(visibleLayerList);
        map.refresh();
        ResponseMapObject response = new ResponseMapObject();
        response.setURL(map.getMapOutput().getURL());
        response.setEnvelope(map.getEnvelope());
        response.setScale(ScaleUtil.getScale(map.getWidth(), map.getHeight(), map.getEnvelope()));
        return response;
    }

    public ResponseMapObject doZoom(List<LayerDef> visibleLayerList, long level) throws Exception {
        map.setWidth(this.width);
        map.setHeight(this.height);
        visibleLayers(visibleLayerList);
        map.doZoom(level);
        map.refresh();
        ResponseMapObject response = new ResponseMapObject();
        response.setURL(map.getMapOutput().getURL());
        response.setEnvelope(map.getEnvelope());
        response.setScale(ScaleUtil.getScale(map.getWidth(), map.getHeight(), map.getEnvelope()));
        return response;
    }

    public ResponseMapObject doPan(List<LayerDef> visibleLayerList, long direction, long step) throws Exception {
        map.setWidth(this.width);
        map.setHeight(this.height);
        visibleLayers(visibleLayerList);
        map.getLayers().setOrder(true);
        map.doPan(direction, step);
        map.refresh();
        ResponseMapObject response = new ResponseMapObject();
        response.setURL(map.getMapOutput().getURL());
        response.setEnvelope(map.getEnvelope());
        response.setScale(ScaleUtil.getScale(map.getWidth(), map.getHeight(), map.getEnvelope()));
        return response;
    }

    public void doPan(long x, long y) throws Exception {
        map.reset();
        map.getLayers().setOrder(true);
        map.doPan(x, y);
    }

    private Symbol getSymbol(FeatureLayer featureLayer) {
        Symbol symbol = null;
        String red = "255";
        String green = "0";
        String blue = "0";
        if (featureLayer.getFeatureClass().equals("point")) {
            SimpleMarkerSymbol sms = new SimpleMarkerSymbol();
            sms.setColor(red + "," + green + "," + blue);
            sms.setTransparency(1.0);
            sms.setWidth(25);
            sms.setOutline("0,0,0");
            sms.setShadow(red + "," + green + "," + blue);
            symbol = sms;
        } else if (featureLayer.getFeatureClass().equals("line")) {
            SimpleLineSymbol sls = new SimpleLineSymbol();
            sls.setColor(red + "," + green + "," + blue);
            sls.setTransparency(1.0);
            symbol = sls;
        } else if (featureLayer.getFeatureClass().equals("polygon")) {
            SimplePolygonSymbol sps = new SimplePolygonSymbol();
            sps.setFillColor(red + "," + green + ",128");
            sps.setFillType(SimplePolygonSymbol.GRAY);
            sps.setFillTransparency(1.0);
            sps.setBoundaryWidth(5);
            sps.setBoundaryColor(red + "," + green + "," + blue);
            sps.setBoundaryTransparency(1.0);
            symbol = sps;
        }
        return symbol;
    }

    private Symbol getSymbol(String featureClass) {
        Symbol symbol = null;
        String red = "255";
        String green = "0";
        String blue = "0";
        if (featureClass.equals("point")) {
            SimpleMarkerSymbol sms = new SimpleMarkerSymbol();
            sms.setColor(red + "," + green + "," + blue);
            sms.setTransparency(1.0);
            sms.setWidth(25);
            sms.setOutline("0,0,0");
            sms.setShadow(red + "," + green + "," + blue);
            symbol = sms;
        } else if (featureClass.equals("line")) {
            SimpleLineSymbol sls = new SimpleLineSymbol();
            sls.setColor(red + "," + green + "," + blue);
            sls.setTransparency(1.0);
            symbol = sls;
        } else if (featureClass.equals("polygon")) {
            SimplePolygonSymbol sps = new SimplePolygonSymbol();
            sps.setFillColor(red + "," + green + ",128");
            sps.setFillType(SimplePolygonSymbol.GRAY);
            sps.setFillTransparency(1.0);
            sps.setBoundaryWidth(5);
            sps.setBoundaryColor(red + "," + green + "," + blue);
            sps.setBoundaryTransparency(1.0);
            symbol = sps;
        }
        return symbol;
    }

    private void addAcetateLayerPoint(Point point) {
        double x = point.getX();
        double y = point.getY();
        Point gPoint = new Point();
        gPoint.setX(x);
        gPoint.setY(y);
        SimpleMarkerSymbol sm = new SimpleMarkerSymbol();
        sm.setColor("255,0,0");
        sm.setWidth(15);
        sm.setAntialiasing(true);
        sm.setMarkerType(SimpleMarkerSymbol.STAR);
        gPoint.setSymbol(sm);
        Acetate ao = new Acetate();
        ao.setAcetateElement(gPoint);
        ao.setUnits("pixel");
        AcetateLayer al = new AcetateLayer(map.getLayers().getCount() + "", null, null);
        al.addAcetate(ao);
        al.setVisible(true);
        al.setName("AcetateLayer");
        map.getLayers().add(al);
    }

    /**
	 * �˻��� Shape�� ���� �߻��̾�� �ɹ��� �߰��Ѵ�.
	 * @param shape
	 */
    private void addAcetateLayer(Shape shape) {
        if (shape.getType().toLowerCase().equals("polygon")) {
            Polygon polygon = (Polygon) shape;
            Symbol symbol = getSymbol("polygon");
            polygon.setSymbol(symbol);
            Acetate aObject = new Acetate();
            aObject.setUnits("pixel");
            aObject.setAcetateElement(polygon);
            AcetateLayer aLayer = new AcetateLayer(map.getLayers().getCount() + "", null, null);
            aLayer.addAcetate(aObject);
            aLayer.setVisible(true);
            aLayer.setName("AcetateLayer");
            map.getLayers().add(aLayer);
        } else if (shape.getType().equals("point")) {
            Point gPoint = (Point) shape;
            Symbol symbol = getSymbol("point");
            SimpleMarkerSymbol sm = new SimpleMarkerSymbol();
            sm.setColor("255,0,0");
            sm.setWidth(15);
            sm.setAntialiasing(true);
            sm.setMarkerType(SimpleMarkerSymbol.STAR);
            gPoint.setSymbol(sm);
            Acetate ao = new Acetate();
            ao.setAcetateElement(gPoint);
            ao.setUnits("pixel");
            AcetateLayer al = new AcetateLayer(map.getLayers().getCount() + "", null, null);
            al.addAcetate(ao);
            al.setVisible(true);
            al.setName("AcetateLayer");
            map.getLayers().add(al);
        } else if (shape.getType().equals("line")) {
            Line line = (Line) shape;
            Symbol symbol = getSymbol("line");
            line.setSymbol(symbol);
            Acetate aObject = new Acetate();
            aObject.setUnits("pixel");
            aObject.setAcetateElement(line);
            AcetateLayer aLayer = new AcetateLayer(map.getLayers().getCount() + "", String.valueOf(25), String.valueOf(50));
            aLayer.addAcetate(aObject);
            aLayer.setVisible(true);
            aLayer.setName("AcetateLayer");
            map.getLayers().add(aLayer);
        }
    }

    /**
	 * TODO ���߿� �߰��Ұ� Ȯ���غ���.
	 * @param action
	 */
    private void addAcetateLayer(String action) {
        if (action.equals("scalebar")) {
            ScaleBar so = new ScaleBar();
            so.setAntialiasing(true);
            so.setBarColor("155,155,155");
            so.setBarWidth(8);
            so.setFont("arial");
            so.setFontColor("255,0,0");
            so.setFontSize(15);
            so.setFontStyle("bold");
            so.setMapUnits("database");
            so.setMode("cartesian");
            so.setOutline("255,255,255");
            so.setPrecision(1);
            so.setRound(1.0);
            so.setScaleUnits("miles");
            so.setScreenLength(100);
            so.setDistance(10.0);
            so.setX(5);
            so.setY(5);
            AcetateLayer aLayer = new AcetateLayer("sbar", String.valueOf(25), String.valueOf(50));
            Acetate ao = new Acetate();
            ao.setAcetateElement(so);
            ao.setUnits("pixel");
            aLayer.setName("scalebar");
            aLayer.setVisible(true);
            aLayer.addAcetate(ao);
            map.getLayers().add(aLayer);
        }
        if (action.equals("northarrow")) {
            NorthArrow northArrow = new NorthArrow();
            northArrow.setArrowType(northArrow.STYLE7);
            northArrow.setAngle(0);
            northArrow.setSize(30);
            northArrow.setX(25);
            northArrow.setY(80);
            northArrow.setAntialiasing(true);
            northArrow.setTransparency(1.0);
            northArrow.setShadow("255,0,0");
            northArrow.setOutline("0,255,0");
            northArrow.setOverlap(true);
            Acetate aObject = new Acetate();
            aObject.setAcetateElement(northArrow);
            aObject.setUnits("pixel");
            AcetateLayer aLayer = new AcetateLayer("northarrow", String.valueOf(25), String.valueOf(50));
            aLayer.setName("NorthArrow");
            aLayer.setVisible(true);
            aLayer.addAcetate(aObject);
            map.getLayers().add(aLayer);
        }
    }

    private ResponseMapObject getMapOut() {
        ResponseMapObject response = new ResponseMapObject();
        response.setURL(map.getMapOutput().getURL());
        response.setEnvelope(map.getEnvelope());
        response.setScale(ScaleUtil.getScale(map.getWidth(), map.getHeight(), map.getEnvelope()));
        return response;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    private Envelope coordinatePNU(Envelope currEnvelope, Envelope toEnvelope) throws Exception {
        try {
            double maxX = currEnvelope.getMaxX();
            double maxY = currEnvelope.getMaxY();
            double minX = currEnvelope.getMinX();
            double minY = currEnvelope.getMinY();
            double newMaxX = toEnvelope.getMaxX();
            double newMaxY = toEnvelope.getMaxY();
            double newMinX = toEnvelope.getMinX();
            double newMinY = toEnvelope.getMinY();
            double pointX = (newMaxX + newMinX) / 2;
            double pointY = (newMaxY + newMinY) / 2;
            newMaxX += (newMaxX - pointX) / 2;
            newMaxY += (newMaxY - pointY) / 2;
            newMinX -= (newMaxX - pointX) / 2;
            newMinY -= (newMaxY - pointY) / 2;
            double ratioX = (maxX - minX) / (newMaxX - newMinX);
            double ratioY = (maxY - minY) / (newMaxY - newMinY);
            if (ratioX > ratioY) {
                double distY = (newMaxX - newMinX) * (maxY - minY) / (maxX - minX);
                newMinY = pointY - distY / 2;
                newMaxY = pointY + distY / 2;
            } else if (ratioX < ratioY) {
                double distX = (maxX - minX) * (newMaxY - newMinY) / (maxY - minY);
                newMinX = pointX - distX / 2;
                newMaxX = pointX + distX / 2;
            }
            toEnvelope.setMinX(newMinX);
            toEnvelope.setMinY(newMinY);
            toEnvelope.setMaxX(newMaxX);
            toEnvelope.setMaxY(newMaxY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toEnvelope;
    }
}
