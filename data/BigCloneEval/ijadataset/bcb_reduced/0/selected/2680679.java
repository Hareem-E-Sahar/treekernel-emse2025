package org.ala.spatial.analysis.web;

import au.org.emii.portal.composer.MapComposer;
import au.org.emii.portal.menu.MapLayer;
import au.org.emii.portal.menu.MapLayerMetadata;
import au.org.emii.portal.settings.SettingsSupplementary;
import au.org.emii.portal.util.LayerUtilities;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.ala.spatial.data.Facet;
import org.ala.spatial.data.BiocacheQuery;
import org.ala.spatial.gazetteer.GazetteerPointSearch;
import org.ala.spatial.sampling.SimpleShapeFile;
import org.ala.spatial.util.CommonData;
import org.ala.spatial.util.LayersUtil;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;

/**
 *
 * @author Adam
 */
public class AreaMapPolygon extends AreaToolComposer {

    SettingsSupplementary settingsSupplementary;

    private Textbox displayGeom;

    Textbox txtLayerName;

    Button btnOk;

    Button btnClear;

    Button btnAddLayer;

    Radio rAddLayer;

    Vbox vbxLayerList;

    Radiogroup rgPolygonLayers;

    Checkbox displayAsWms;

    @Override
    public void afterCompose() {
        super.afterCompose();
        loadLayerSelection();
        txtLayerName.setValue(getMapComposer().getNextAreaLayerName("My Area"));
        btnOk.setDisabled(true);
        btnClear.setDisabled(true);
        Clients.evalJavaScript("mapFrame.toggleClickHandler(false);");
    }

    public void onClick$btnOk(Event event) {
        MapLayer ml = getMapComposer().getMapLayer(layerName);
        ml.setDisplayName(txtLayerName.getValue());
        getMapComposer().redrawLayersList();
        ok = true;
        Clients.evalJavaScript("mapFrame.toggleClickHandler(true);");
        String activeLayerName = "none";
        if (ml.getUri() != null) {
            activeLayerName = ml.getUri().replaceAll("^.*ALA:", "").replaceAll("&.*", "");
        }
        getMapComposer().setAttribute("activeLayerName", activeLayerName);
        getMapComposer().setAttribute("mappolygonlayer", rgPolygonLayers.getSelectedItem().getValue());
        this.detach();
    }

    public void onClick$btnClear(Event event) {
        MapComposer mc = getThisMapComposer();
        if (layerName != null && mc.getMapLayer(layerName) != null) {
            mc.removeLayer(layerName);
        }
        String script = mc.getOpenLayersJavascript().addFeatureSelectionTool();
        mc.getOpenLayersJavascript().execute(mc.getOpenLayersJavascript().iFrameReferences + script);
        displayGeom.setValue("");
        btnOk.setDisabled(true);
        btnClear.setDisabled(true);
    }

    public void onClick$btnCancel(Event event) {
        MapComposer mc = getThisMapComposer();
        if (layerName != null && mc.getMapLayer(layerName) != null) {
            mc.removeLayer(layerName);
        }
        Clients.evalJavaScript("mapFrame.toggleClickHandler(true);");
        this.detach();
    }

    public void onCheck$rgPolygonLayers(Event event) {
        Radio selectedItem = rgPolygonLayers.getSelectedItem();
        String layerName = selectedItem.getValue();
        MapComposer mc = getThisMapComposer();
        MapLayer ml = mc.getMapLayer(layerName);
        mc.removeLayer(layerName);
        mc.activateLayer(ml, true);
    }

    public void loadLayerSelection() {
        try {
            Radio rSelectedLayer = (Radio) getFellowIfAny("rSelectedLayer");
            List<MapLayer> layers = getMapComposer().getContextualLayers();
            if (!layers.isEmpty()) {
                for (int i = 0; i < layers.size(); i++) {
                    MapLayer lyr = layers.get(i);
                    Radio rAr = new Radio(lyr.getDisplayName());
                    rAr.setId(lyr.getDisplayName().replaceAll(" ", ""));
                    rAr.setValue(lyr.getDisplayName());
                    rAr.setParent(rgPolygonLayers);
                    if (i == 0) {
                        rAr.setSelected(true);
                    }
                    rgPolygonLayers.insertBefore(rAr, rSelectedLayer);
                }
                rSelectedLayer.setSelected(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Searches the gazetter at a given point and then maps the polygon feature
     * found at the location (for the current top contextual layer).
     * @param event 
     */
    public void onSearchPoint(Event event) {
        String searchPoint = (String) event.getData();
        String lon = searchPoint.split(",")[0];
        String lat = searchPoint.split(",")[1];
        System.out.println("*************************************");
        System.out.println("CommonData.getLayerList");
        System.out.println("*************************************");
        Object llist = CommonData.getLayerListJSONArray();
        JSONArray layerlist = JSONArray.fromObject(llist);
        MapComposer mc = getThisMapComposer();
        List<MapLayer> activeLayers = getPortalSession().getActiveLayers();
        Boolean searchComplete = false;
        for (int i = 0; i < activeLayers.size(); i++) {
            MapLayer ml = activeLayers.get(i);
            String activeLayerName = "none";
            if (ml.getUri() != null) {
                activeLayerName = ml.getUri().replaceAll("^.*ALA:", "").replaceAll("&.*", "");
            }
            System.out.println("ACTIVE LAYER: " + activeLayerName);
            if (ml.isDisplayed()) {
                for (int j = 0; j < layerlist.size(); j++) {
                    if (searchComplete) {
                        break;
                    }
                    JSONObject jo = layerlist.getJSONObject(j);
                    if (ml != null && jo.getString("type") != null && jo.getString("type").length() > 0 && jo.getString("type").equalsIgnoreCase("contextual") && jo.getString("name").equalsIgnoreCase(activeLayerName)) {
                        System.out.println(ml.getName());
                        Map<String, String> feature = GazetteerPointSearch.PointSearch(lon, lat, activeLayerName, CommonData.geoServer);
                        if (feature == null || !feature.containsKey("pid")) {
                            continue;
                        }
                        String wkt = readUrl(CommonData.layersServer + "/shape/wkt/" + feature.get("pid"));
                        JSONObject obj = JSONObject.fromObject(readUrl(CommonData.layersServer + "/object/" + feature.get("pid")));
                        if (wkt.contentEquals("none")) {
                            continue;
                        } else {
                            searchComplete = true;
                            if (wkt.length() > 200) {
                                displayGeom.setValue(wkt.substring(0, 200) + "...");
                            } else {
                                displayGeom.setValue(wkt);
                            }
                            System.out.println("**********************************");
                            System.out.println("setting layerName from " + layerName);
                            layerName = (mc.getMapLayer(txtLayerName.getValue()) == null) ? txtLayerName.getValue() : mc.getNextAreaLayerName(txtLayerName.getValue());
                            System.out.println("to " + layerName);
                            System.out.println("**********************************");
                            MapLayer mapLayer;
                            if (displayAsWms.isChecked()) {
                                String url = obj.getString("wmsurl");
                                mapLayer = getMapComposer().addWMSLayer(getMapComposer().getNextAreaLayerName(txtLayerName.getValue()), txtLayerName.getValue(), url, 0.6f, null, null, LayerUtilities.WKT, null, null);
                                mapLayer.setWKT(wkt);
                                mapLayer.setPolygonLayer(true);
                            } else {
                                mapLayer = mc.addWKTLayer(wkt, layerName, txtLayerName.getValue());
                            }
                            Facet facet = null;
                            if (!mapLayer.getWKT().startsWith("POINT") && !feature.get("pid").contains(":")) {
                                facet = getFacetForObject(feature.get("pid"), feature.get("value"));
                            }
                            if (facet != null) {
                                ArrayList<Facet> facets = new ArrayList<Facet>();
                                facets.add(facet);
                                mapLayer.setData("facets", facets);
                            }
                            MapLayerMetadata md = mapLayer.getMapLayerMetadata();
                            if (md == null) {
                                md = new MapLayerMetadata();
                                mapLayer.setMapLayerMetadata(md);
                            }
                            try {
                                double[][] bb = SimpleShapeFile.parseWKT(wkt).getBoundingBox();
                                ArrayList<Double> bbox = new ArrayList<Double>();
                                bbox.add(bb[0][0]);
                                bbox.add(bb[0][1]);
                                bbox.add(bb[1][0]);
                                bbox.add(bb[1][1]);
                                md.setBbox(bbox);
                            } catch (Exception e) {
                                System.out.println("failed to parse: " + wkt);
                                e.printStackTrace();
                            }
                            try {
                                String fid = getStringValue(null, "fid", readUrl(CommonData.layersServer + "/object/" + feature.get("pid")));
                                String spid = getStringValue("\"id\":\"" + fid + "\"", "spid", readUrl(CommonData.layersServer + "/fields"));
                                md.setMoreInfo(CommonData.layersServer + "/layers/view/more/" + spid);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            btnOk.setDisabled(false);
                            btnClear.setDisabled(false);
                            break;
                        }
                    }
                }
            }
        }
    }

    String getStringValue(String startAt, String tag, String json) {
        String typeStart = "\"" + tag + "\":\"";
        String typeEnd = "\"";
        int beginning = startAt == null ? 0 : json.indexOf(startAt) + startAt.length();
        int start = json.indexOf(typeStart, beginning) + typeStart.length();
        int end = json.indexOf(typeEnd, start);
        return json.substring(start, end);
    }

    /**
     * Gets the main pages controller so we can add a
     * drawing tool to the map
     * @return MapComposer = map controller class
     */
    private MapComposer getThisMapComposer() {
        MapComposer mapComposer = null;
        Page page = getPage();
        mapComposer = (MapComposer) page.getFellow("mapPortalPage");
        return mapComposer;
    }

    private String readUrl(String feature) {
        StringBuffer content = new StringBuffer();
        try {
            URL url = new URL(feature);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                content.append(line);
            }
            conn.disconnect();
        } catch (Exception e) {
        }
        return content.toString();
    }

    private Facet getFacetForObject(String pid, String name) {
        JSONObject jo = JSONObject.fromObject(readUrl(CommonData.layersServer + "/object/" + pid));
        String fieldId = jo.getString("fid");
        String objects = readUrl(CommonData.layersServer + "/field/" + fieldId);
        String lookFor = "\"name\":\"" + name + "\"";
        int p1 = objects.indexOf(lookFor);
        if (p1 > 0) {
            int p2 = objects.indexOf(lookFor, p1 + 1);
            if (p2 < 0) {
                Facet f = new Facet(fieldId, "\"" + name + "\"", true);
                ArrayList<Facet> facets = new ArrayList<Facet>();
                facets.add(f);
                if (new BiocacheQuery(null, null, null, facets, false, null).getOccurrenceCount() > 0) {
                    return f;
                }
            }
        }
        return null;
    }
}
