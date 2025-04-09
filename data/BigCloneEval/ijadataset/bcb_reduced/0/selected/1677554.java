package org.gbif.portal.web.content.map;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.portal.dto.geospatial.CellDensityDTO;
import org.gbif.portal.dto.geospatial.CountryDTO;
import org.gbif.portal.dto.util.BoundingBoxDTO;
import org.gbif.portal.dto.util.EntityType;
import org.gbif.portal.service.GeospatialManager;
import org.gbif.portal.service.ServiceException;
import org.gbif.portal.util.geospatial.CellIdUtils;
import org.gbif.portal.util.geospatial.LatLongBoundingBox;
import org.gbif.portal.web.content.ContentProvider;
import org.gbif.portal.web.content.ContentView;
import org.gbif.portal.web.content.geospatial.ZoomLevel;
import org.springframework.web.bind.ServletRequestUtils;

/**
 * A Content Provider that provides map content to requests.
 * 
 * @author dmartin
 */
public class MapContentProvider implements ContentProvider {

    private static Log logger = LogFactory.getLog(MapContentProvider.class);

    /** Geospatial Manager for service layer queries */
    protected GeospatialManager geospatialManager;

    /**The default Map Server Key **/
    protected String defaultMapServerKey;

    /**The default Map Server used if the preference key is missing **/
    protected String defaultMapServerValue;

    /**The default Geo Server used if the preference key is missing **/
    protected String defaultGeoServerValue;

    /**The default Map Layers Key **/
    protected String defaultMapLayerKey;

    /** The url to the webapp instance serving map layers */
    protected String mapLayerServerURL;

    /** Plotted points total represented by cell densities */
    protected String pointsTotalRequestKey = "pointsTotal";

    protected String minMapLongRequestKey = "minMapLong";

    protected String minMapLatRequestKey = "minMapLat";

    protected String maxMapLongRequestKey = "maxMapLong";

    protected String maxMapLatRequestKey = "maxMapLat";

    protected String zoomRequestKey = "zoom";

    protected static String extentRequestKey = "extent";

    protected static final String WMS_FILTER_REQUEST_KEY = "wmsFilter";

    /**
	 * @see org.gbif.portal.web.content.ContentProvider#addContent(org.gbif.portal.web.content.ContentView, java.lang.Object)
	 */
    public void addContent(ContentView cc, HttpServletRequest request, HttpServletResponse response) {
    }

    /**
	 * Verifies if a zoom level is already specified in the current request.
	 * @param request
	 * @return
	 */
    public static boolean zoomLevelSpecified(HttpServletRequest request) {
        return StringUtils.isNotEmpty(request.getParameter(extentRequestKey));
    }

    /**
	 * Retrieves the specified view area.
	 * 
	 * @param request
	 * @return bounding box for the view area specified in the request.
	 */
    public BoundingBoxDTO getSpecifiedViewArea(HttpServletRequest request) {
        Float minLongitude = ServletRequestUtils.getFloatParameter(request, minMapLongRequestKey, -180);
        Float minLatitude = ServletRequestUtils.getFloatParameter(request, minMapLatRequestKey, -90);
        Float maxLongitude = ServletRequestUtils.getFloatParameter(request, maxMapLongRequestKey, 180);
        Float maxLatitude = ServletRequestUtils.getFloatParameter(request, maxMapLatRequestKey, 90);
        return new BoundingBoxDTO(minLongitude, minLatitude, maxLongitude, maxLatitude);
    }

    /**
	 * Adds map content to the request. This method just uses the request parameters to ascertain
	 * the zoom level.
	 * 
	 * @param request
	 * @param mapLayerPathRoot
	 * @param idOrQueryString
	 */
    public void addMapContent(HttpServletRequest request, String mapLayerPathRoot, String idOrQueryString) {
        String zoomAsString = request.getParameter(zoomRequestKey);
        int zoom = 1;
        if (zoomAsString != null) {
            try {
                zoom = Integer.parseInt(zoomAsString);
            } catch (NumberFormatException e) {
                logger.warn("Zoom not parsable - using global view.  Zoom: " + zoomAsString);
            }
        }
        Integer minLongitude = ServletRequestUtils.getIntParameter(request, minMapLongRequestKey, -180);
        Integer minLatitude = ServletRequestUtils.getIntParameter(request, minMapLatRequestKey, -90);
        Integer maxLongitude = ServletRequestUtils.getIntParameter(request, maxMapLongRequestKey, 180);
        Integer maxLatitude = ServletRequestUtils.getIntParameter(request, maxMapLatRequestKey, 90);
        request.setAttribute(minMapLongRequestKey, minLongitude);
        request.setAttribute(minMapLatRequestKey, minLatitude);
        request.setAttribute(maxMapLongRequestKey, maxLongitude);
        request.setAttribute(maxMapLatRequestKey, maxLatitude);
        request.setAttribute(zoomRequestKey, zoom);
        addGeoServerDetails2Request(request, mapLayerPathRoot, idOrQueryString, minLongitude, minLatitude, zoom);
    }

    /**
	 * Adds map content to the request for the given entity type.
	 * 
	 * @param request
	 * @param entityType
	 * @param entityKey
	 */
    public void addMapContentForEntity(HttpServletRequest request, EntityType entityType, String entityKey) {
        addWmsFilterToRequest(request, entityType, entityKey);
        if (logger.isDebugEnabled()) logger.debug("Adding map content for entity type:" + entityType + ", key:" + entityKey);
        if (MapContentProvider.zoomLevelSpecified(request)) {
            addMapContent(request, entityType.getName(), entityKey);
        } else {
            try {
                List<CellDensityDTO> cellDensities = geospatialManager.get1DegCellDensities(entityType, entityKey);
                addMapContent(request, entityType.getName(), entityKey, cellDensities, null);
            } catch (ServiceException e) {
                logger.error(e.getMessage(), e);
            }
        }
        try {
            addPointsTotalsToRequest(request, entityType, entityKey, null);
        } catch (ServiceException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
	 * Adds map content to the request for the given entity type.
	 * 
	 * @param request
	 * @param entityType
	 * @param entityKey
	 */
    public void addMapContentForEntities(HttpServletRequest request, EntityType entityType, List<String> entityKeys) {
        if (logger.isDebugEnabled()) logger.debug("Adding map content for entity type:" + entityType + ", key:" + entityKeys);
        if (MapContentProvider.zoomLevelSpecified(request)) {
            addMapContent(request, entityType.getName(), idsToCommaList(entityKeys));
        } else {
            try {
                List<CellDensityDTO> cellDensities = geospatialManager.get1DegCellDensities(entityType, entityKeys);
                addMapContent(request, entityType.getName(), idsToCommaList(entityKeys), cellDensities, null);
            } catch (ServiceException e) {
                logger.error(e.getMessage(), e);
            }
        }
        try {
            addPointsTotalsToRequest(request, entityType, entityKeys, null);
        } catch (ServiceException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
	 * Adds map content to the request for the given entity type with the supplied bounding box.
	 * 
	 * @param request
	 * @param entityType
	 * @param entityKey
	 */
    public void addMapContentForEntity(HttpServletRequest request, EntityType entityType, List<String> entityKeys, BoundingBoxDTO boundingBox) throws ServiceException {
        if (MapContentProvider.zoomLevelSpecified(request)) {
            addMapContent(request, entityType.getName(), idsToCommaList(entityKeys));
        } else {
            addMapContent(request, entityType.getName(), idsToCommaList(entityKeys), boundingBox.getLeft(), boundingBox.getLower(), boundingBox.getRight(), boundingBox.getUpper());
        }
        addPointsTotalsToRequest(request, entityType, entityKeys, boundingBox);
    }

    /**
	 * Adds map content to the request for the given entity type with the supplied bounding box.
	 * 
	 * @param request
	 * @param entityType
	 * @param entityKey
	 */
    public void addMapContentForEntity(HttpServletRequest request, EntityType entityType, String entityKey, BoundingBoxDTO boundingBox) throws ServiceException {
        addWmsFilterToRequest(request, entityType, entityKey);
        if (MapContentProvider.zoomLevelSpecified(request)) {
            addMapContent(request, entityType.getName(), entityKey);
        } else {
            addMapContent(request, entityType.getName(), entityKey, boundingBox.getLeft(), boundingBox.getLower(), boundingBox.getRight(), boundingBox.getUpper());
        }
        addPointsTotalsToRequest(request, entityType, entityKey, boundingBox);
    }

    /**
	 * Convert list to comma separated string.
	 * 
	 * @param entityKeys
	 * @return
	 */
    public String idsToCommaList(List<String> entityKeys) {
        StringBuffer sb = new StringBuffer();
        for (Iterator iterator = entityKeys.iterator(); iterator.hasNext(); ) {
            String element = (String) iterator.next();
            sb.append(element);
            if (iterator.hasNext()) sb.append(",");
        }
        return sb.toString();
    }

    /**
	 * Adds map content to the request. This methods just uses supplied min/max lat/long to calculate the zoom level required.
	 * Useful when the zoom level required is not based on the cell densities retrieved - e.g. Country
	 * 
	 * @param request
	 * @param mapLayerPathRoot
	 * @param key
	 * @param minLongitude
	 * @param minLatitude
	 * @param maxLongitude
	 * @param maxLatitude
	 */
    public void addMapContent(HttpServletRequest request, String mapLayerPathRoot, String idOrQuery, float minLongitude, float minLatitude, float maxLongitude, float maxLatitude) {
        ZoomLevel zoomLevel = ZoomLevel.getZoomLevel(minLongitude, minLatitude, maxLongitude, maxLatitude);
        logger.debug(zoomLevel);
        float longCentre = (maxLongitude + minLongitude) / 2;
        float latCentre = (maxLatitude + minLatitude) / 2;
        LatLongBoundingBox llbb = getBoundingBoxForZoomLevel(zoomLevel, longCentre, latCentre);
        roundLatLongValues(llbb);
        logger.debug(llbb);
        String extent = createExtent(llbb.getMinLong(), llbb.getMinLat(), llbb.getMaxLong(), llbb.getMaxLat());
        request.setAttribute(extentRequestKey, extent.toString());
        request.setAttribute(zoomRequestKey, zoomLevel.getLevel());
        request.setAttribute(minMapLongRequestKey, (int) llbb.getMinLong());
        request.setAttribute(minMapLatRequestKey, (int) llbb.getMinLat());
        request.setAttribute(maxMapLongRequestKey, (int) llbb.getMaxLong());
        request.setAttribute(maxMapLatRequestKey, (int) llbb.getMaxLat());
        addGeoServerDetails2Request(request, mapLayerPathRoot, idOrQuery, (int) llbb.getMinLong(), (int) llbb.getMinLat(), zoomLevel.getLevel());
    }

    /**
	 * Create mapserver specific extent.
	 * 
	 * @param minLong
	 * @param minLat
	 * @param maxLong
	 * @param maxLat
	 * @return
	 */
    private String createExtent(Float minLong, Float minLat, Float maxLong, Float maxLat) {
        StringBuffer extent = new StringBuffer();
        extent.append(minLong);
        extent.append('+');
        extent.append(minLat);
        extent.append('+');
        extent.append(maxLong);
        extent.append('+');
        extent.append(maxLat);
        return extent.toString();
    }

    /**
	 * Adds map content to the request. This methods just uses cell densities to calculate a bounding box and
	 * hence the required zoom level.
	 * 
	 * @param request
	 * @param mapLayerPathRoot
	 * @param idOrQuery
	 * @param cellDensities
	 */
    public void addMapContent(HttpServletRequest request, String mapLayerPathRoot, String idOrQuery, List<CellDensityDTO> cellDensities, BoundingBoxDTO bbDTO) {
        if (MapContentProvider.zoomLevelSpecified(request)) {
            addMapContent(request, mapLayerPathRoot, idOrQuery);
        } else {
            if (bbDTO == null) {
                List<Integer> cellIds = new ArrayList<Integer>();
                for (CellDensityDTO cellDensity : cellDensities) {
                    cellIds.add(cellDensity.getCellId());
                }
                LatLongBoundingBox llbb = CellIdUtils.getBoundingBoxForCells(cellIds);
                if (logger.isDebugEnabled()) logger.debug("LatLongBoundingBox for cellids: " + llbb);
                if (llbb == null) llbb = LatLongBoundingBox.GLOBAL_BOUNDING_BOX;
                addMapContent(request, mapLayerPathRoot, idOrQuery, llbb.getMinLong(), llbb.getMinLat(), llbb.getMaxLong(), llbb.getMaxLat());
            } else {
                addMapContent(request, mapLayerPathRoot, idOrQuery, bbDTO.getLeft(), bbDTO.getLower(), bbDTO.getRight(), bbDTO.getUpper());
            }
        }
    }

    /**
	 * Adds map content to the request.
	 * 
	 * @param request
	 * @param mapLayerPathRoot
	 * @param idOrQuery
	 */
    private void addMapServerDetails2Request(HttpServletRequest request, String mapLayerPathRoot, String idOrQuery, float minLongitude, float minLatitude, int zoom) {
        StringBuffer sb = new StringBuffer();
        sb.append(mapLayerPathRoot);
        sb.append('/');
        sb.append(idOrQuery);
        String mapLayerUrl = getMapLayerURL(zoom, sb.toString(), (int) minLongitude, (int) minLatitude);
        String mapLayerUrlEscaped = mapLayerUrl;
        try {
            mapLayerUrlEscaped = URLEncoder.encode(mapLayerUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        ZoomLevel zoomLevel = ZoomLevel.getZoomLevel(zoom);
        String extent = createExtent(minLongitude, minLatitude, minLongitude + zoomLevel.getLongitudeRange(), minLatitude + zoomLevel.getLatitudeRange());
        request.setAttribute("mapServerUrl", getMapServerURL());
        request.setAttribute("mapLayerUrl", mapLayerUrlEscaped);
        request.setAttribute("unEscapedMapLayerUrl", mapLayerUrl);
        request.setAttribute("overviewMapUrl", getGeoServerURL() + "?request=GetMap&bgcolor=0x666698&styles=,,&layers=gbif:country_fill,gbif:tabDensityLayer,gbif:country_borders" + "&srs=EPSG:4326&filter=()(<Filter><PropertyIsEqualTo><PropertyName>url</PropertyName><Literal><![CDATA[" + mapLayerUrlEscaped + "]]></Literal></PropertyIsEqualTo></Filter>)()" + "&width=720&height=360&Format=image/png&bbox=-180,-90,180,90");
    }

    /**
	 * Adds map content to the request.
	 * 
	 * @param request
	 * @param mapLayerPathRoot
	 * @param idOrQuery
	 */
    private void addGeoServerDetails2Request(HttpServletRequest request, String mapLayerPathRoot, String idOrQuery, float minLongitude, float minLatitude, int zoom) {
        StringBuffer sb = new StringBuffer();
        sb.append(mapLayerPathRoot);
        sb.append('/');
        sb.append(idOrQuery);
        String mapLayerUrl = getMapLayerURL(zoom, sb.toString(), (int) minLongitude, (int) minLatitude);
        String mapLayerUrlEscaped = mapLayerUrl;
        try {
            mapLayerUrlEscaped = URLEncoder.encode(mapLayerUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        ZoomLevel zoomLevel = ZoomLevel.getZoomLevel(zoom);
        String extent = createExtent(minLongitude, minLatitude, minLongitude + zoomLevel.getLongitudeRange(), minLatitude + zoomLevel.getLatitudeRange());
        request.setAttribute("geoServerUrl", getGeoServerURL());
        request.setAttribute("mapLayerUrl", mapLayerUrlEscaped);
        request.setAttribute("unEscapedMapLayerUrl", mapLayerUrl);
        request.setAttribute("overviewMapUrl", getGeoServerURL() + "?request=GetMap&bgcolor=0x666698&styles=,,&layers=gbif:country_fill,gbif:tabDensityLayer,gbif:country_borders" + "&srs=EPSG:4326&filter=()(<Filter><PropertyIsEqualTo><PropertyName>url</PropertyName><Literal><![CDATA[" + mapLayerUrlEscaped + "]]></Literal></PropertyIsEqualTo></Filter>)()" + "&width=720&height=360&Format=image/png&bbox=-180,-90,180,90");
    }

    /**
	 * Retrieve a bounding box for the given zoom level and centre point.
	 * 
	 * @param zoomLevel
	 * @param longCentre
	 * @param latCentre
	 * @return LatLongBoundingBox
	 */
    public static LatLongBoundingBox getBoundingBoxForZoomLevel(ZoomLevel zoomLevel, float longCentre, float latCentre) {
        float minLong = longCentre - (zoomLevel.getLongitudeRange() / 2);
        float minLat = latCentre - (zoomLevel.getLatitudeRange() / 2);
        float maxLong = minLong + zoomLevel.getLongitudeRange();
        float maxLat = minLat + zoomLevel.getLatitudeRange();
        if (minLong < -180) {
            minLong = -180;
            maxLong = minLong + zoomLevel.getLongitudeRange();
        }
        if (minLat < -90) {
            minLat = -90;
            maxLat = minLat + zoomLevel.getLatitudeRange();
        }
        if (maxLong > 180) {
            maxLong = 180;
            minLong = maxLong - zoomLevel.getLongitudeRange();
        }
        if (maxLat > 90) {
            maxLat = 90;
            minLat = maxLat - zoomLevel.getLatitudeRange();
        }
        return new LatLongBoundingBox(minLong, minLat, maxLong, maxLat);
    }

    /**
	 * Rounds the lat long values down in this bounding box dto.
	 * 
	 * @param llbb
	 */
    public static void roundLatLongValues(LatLongBoundingBox llbb) {
        llbb.setMinLong(Math.round(llbb.getMinLong()));
        llbb.setMinLat(Math.round(llbb.getMinLat()));
        llbb.setMaxLong(Math.round(llbb.getMaxLong()));
        llbb.setMaxLat(Math.round(llbb.getMaxLat()));
    }

    /**
	 * Rounds the lat long values down in this bounding box dto.
	 * 
	 * @param llbb
	 */
    public static int getPointsTotal(List<CellDensityDTO> cellDensities) {
        int total = 0;
        if (cellDensities != null) {
            for (CellDensityDTO cdt : cellDensities) total += cdt.getCount();
        }
        return total;
    }

    /**
	 * Add points totals to the request.
	 * 
	 * @param request
	 * @param entityType
	 * @param entityKey
	 * @throws ServiceException
	 */
    public void addPointsTotalsToRequest(HttpServletRequest request, EntityType entityType, String entityKey, BoundingBoxDTO boundingBoxDTO) throws ServiceException {
        addWmsFilterToRequest(request, entityType, entityKey);
        List<String> entityIds = new ArrayList<String>();
        entityIds.add(entityKey);
        addPointsTotalsToRequest(request, entityType, entityIds, boundingBoxDTO);
    }

    /**
	 * Add points totals to the request.
	 * 
	 * @param request
	 * @param entityType
	 * @param entityKey
	 * @throws ServiceException
	 */
    public void addPointsTotalsToRequest(HttpServletRequest request, EntityType entityType, List<String> entityKeys, BoundingBoxDTO boundingBoxDTO) throws ServiceException {
        int pointsTotal = geospatialManager.countGeoreferencedPointsFor(entityType, entityKeys);
        request.setAttribute("pointsTotal", pointsTotal);
        if (logger.isDebugEnabled()) logger.debug("pointsTotal: " + pointsTotal);
        if (MapContentProvider.zoomLevelSpecified(request) || boundingBoxDTO != null) {
            if (MapContentProvider.zoomLevelSpecified(request)) {
                boundingBoxDTO = getSpecifiedViewArea(request);
            }
            int viewablePoints = 0;
            if (boundingBoxDTO.equals(BoundingBoxDTO.GLOBAL_BOUNDING_BOX)) {
                viewablePoints = pointsTotal;
            } else {
                viewablePoints = geospatialManager.countGeoreferencedPointsFor(entityType, entityKeys, boundingBoxDTO);
            }
            request.setAttribute("viewablePoints", viewablePoints);
            if (logger.isDebugEnabled()) logger.debug("viewablePoints: " + viewablePoints);
        }
    }

    /**
	 * Gets the map layer url. 
	 *
	 * @param zoom The zoom level
	 * @param pathAfterRoot
	 * @param extent the value required for the map server
	 * @return url for the map layer
	 */
    public String getMapLayerURL(int zoom, String pathAfterRoot, Integer minLongitude, Integer minLatitude) {
        StringBuffer sb = new StringBuffer();
        sb.append(getMapLayerServerURL());
        sb.append(pathAfterRoot);
        if (zoom == ZoomLevel.ZOOM_LEVEL_5.getLevel()) {
            sb.append("/tenDeg/");
        } else if (zoom == ZoomLevel.ZOOM_LEVEL_6.getLevel()) {
            sb.append("/oneDeg/");
        }
        if (zoom > ZoomLevel.ZOOM_LEVEL_4.getLevel()) {
            sb.append(minLatitude);
            sb.append('/');
            sb.append(minLongitude);
            sb.append('/');
        }
        return sb.toString();
    }

    /**
	 * Builds and adds a WMS filter to the request
	 * If the type is a country, then the ISO country code is determined  
	 * @param request To add to
	 * @param entityType The entity type
	 * @param entityKey The ID of the entity
	 */
    protected void addWmsFilterToRequest(HttpServletRequest request, EntityType entityType, String entityKey) {
        if (entityType.equals(EntityType.TYPE_COUNTRY) || entityType.equals(EntityType.TYPE_HOME_COUNTRY)) {
            CountryDTO country = geospatialManager.getCountryFor(entityKey, request.getLocale());
            if (country == null) {
                logger.info("entityKey refers to unknown country");
                return;
            }
            entityKey = country.getIsoCountryCode();
            logger.info("Entity type refers to country: " + entityKey);
        }
        logger.info("Adding the WMS filter to the request");
        String filter = "<Filter>" + "<And>" + "<PropertyIsEqualTo>" + "<PropertyName>type</PropertyName>" + "<Literal>" + entityType.getId() + "</Literal>" + "</PropertyIsEqualTo>" + "<PropertyIsEqualTo>" + "<PropertyName>concept</PropertyName>" + "<Literal>" + entityKey + "</Literal>" + "</PropertyIsEqualTo>" + "</And>" + "</Filter>";
        request.setAttribute(WMS_FILTER_REQUEST_KEY, filter);
    }

    /**
	 * Returns the url for the configured map server.
	 * @return the map server url.
	 */
    public String getMapServerURL() {
        return defaultMapServerValue;
    }

    /**
	 * @return the defaultMapLayerKey
	 */
    public String getDefaultMapLayerKey() {
        return defaultMapLayerKey;
    }

    /**
	 * @param defaultMapLayerKey the defaultMapLayerKey to set
	 */
    public void setDefaultMapLayerKey(String defaultMapLayerKey) {
        this.defaultMapLayerKey = defaultMapLayerKey;
    }

    /**
	 * @return the defaultMapServerKey
	 */
    public String getDefaultMapServerKey() {
        return defaultMapServerKey;
    }

    /**
	 * @param defaultMapServerKey the defaultMapServerKey to set
	 */
    public void setDefaultMapServerKey(String defaultMapServerKey) {
        this.defaultMapServerKey = defaultMapServerKey;
    }

    /**
	 * @return the defaultMapServerValue
	 */
    public String getDefaultMapServerValue() {
        return defaultMapServerValue;
    }

    /**
	 * @param defaultMapServerValue the defaultMapServerValue to set
	 */
    public void setDefaultMapServerValue(String defaultMapServerValue) {
        this.defaultMapServerValue = defaultMapServerValue;
    }

    /**
	 * @return the mapLayerServerURL
	 */
    public String getMapLayerServerURL() {
        return mapLayerServerURL;
    }

    /**
	 * @param mapLayerServerURL the mapLayerServerURL to set
	 */
    public void setMapLayerServerURL(String mapLayerServerURL) {
        this.mapLayerServerURL = mapLayerServerURL;
    }

    /**
	 * @return the defaultGeoServerValue
	 */
    public String getGeoServerURL() {
        return defaultGeoServerValue;
    }

    /**
	 * @param defaultGeoServerValue the defaultGeoServerValue to set
	 */
    public void setDefaultGeoServerValue(String defaultGeoServerValue) {
        this.defaultGeoServerValue = defaultGeoServerValue;
    }

    /**
	 * @param geospatialManager the geospatialManager to set
	 */
    public void setGeospatialManager(GeospatialManager geospatialManager) {
        this.geospatialManager = geospatialManager;
    }
}
