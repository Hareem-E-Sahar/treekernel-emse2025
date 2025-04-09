package org.fao.waicent.kids.giews.servlet;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Hashtable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.RequestDispatcher;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.fao.waicent.kids.server.kidsSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.NodeList;
import org.fao.waicent.kids.giews.dao.bean.Project;
import org.fao.waicent.kids.giews.dao.bean.Featurelayer;
import org.fao.waicent.kids.giews.dao.bean.Rasterlayer;
import org.fao.waicent.db.dbConnectionManager;
import org.fao.waicent.db.dbConnectionManagerPool;
import org.fao.waicent.util.Debug;
import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import java.io.FileWriter;
import java.io.StringWriter;

/**
 * This class is run to generate the map.xml of each project. Generation can be
 * single project, by region, or all countries, and regions of the world.
 *
 * @version        GIEWS 2.2    August 16, 2007
 */
public class ProjectGeneratorServlet extends HttpServlet {

    private String database_ini;

    private String projects_path;

    private Document map_doc;

    private final String ESTIMATED_RAINFALL = "Estimated-Rainfall";

    private final String VEGETATION_INDEX = "Vegetation-Index";

    private final String FEATURE_LAYER = "org.fao.waicent.xmap2D.FeatureLayer";

    private final String RASTER_LAYER = "org.fao.waicent.xmap2D.RasterLayer";

    private final String MAP = "org.fao.waicent.xmap2D.Map";

    private final String FILE_RESOURCE = "org.fao.waicent.util.FileResource";

    private final String GROUP_LAYER = "org.fao.waicent.xmap2D.layer.BaseGroupLayer";

    private final String GIEWS_INI = "WEB-INF/giews.ini";

    private final String GIEWS_EXTERNALIZER = "org.fao.waicent.kids.giews.GIEWSAttributesExternalizer";

    private final String GIEWS_PROJECTS = "/WEB-INF/projects/";

    private static final String TIFF_RASTER_TYPE = "5";

    private Hashtable generation_summary;

    private StringBuffer generation_text;

    private String NO_UPDATES = "There are no updates in groups, layers, and datasets.";

    private String FL_UPDATES = "There were updates in the vector/feature layers in the past 15 days.";

    private String RL_UPDATES = "There were updates in the raster layers in the past 15 days.";

    private String DS_UPDATES = "There were updates in the datasets in the past 15 days.";

    private String GL_UPDATES = "There were updates in the groups in the past 15 days.";

    private boolean gl_upd, rl_upd, fl_upd, ds_upd = false;

    private Date today;

    /**
     *  Executed just once, the first time ProjectGenerator. Initializes the
     *           DB parameters, and projects path.
     *
     * @param        ServletConfig
     *
     * @throws       ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        database_ini = config.getServletContext().getRealPath(GIEWS_INI);
        projects_path = config.getServletContext().getRealPath(GIEWS_PROJECTS);
    }

    /**
     *   This method is called everytime ProjectGeneration is executed.
     *        Reads the parameters from the request and start the project
     *        generation by calling the generateProject method. Forward to
     *        project_generation_summary.jsp to display the results.
     *
     * @param        HttpServletRequest
     * @param        HttpServletResponse
     *
     * @throws       ServletException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] projects = request.getParameterValues("selectedOptions");
        String url = "/project_generation_summary.jsp";
        RequestDispatcher dispatcher = null;
        try {
            dispatcher = getServletContext().getRequestDispatcher(url);
            today = new Date(System.currentTimeMillis());
            generation_summary = new Hashtable();
            Vector vec_projs = generateProjects(projects);
            request.setAttribute("PROJECTS", vec_projs);
            request.setAttribute("SUMMARY", generation_summary);
            kidsSession kids = (kidsSession) request.getSession(true).getValue("kids");
            if (kids != null) {
                kids.removeAttribute("PG_REGIONPROJECTS");
                kids.removeAttribute("PG_Regional");
                kids.removeAttribute("PG_SelectedRegion");
            }
            dispatcher.forward(request, response);
        } catch (Exception e) {
            request.setAttribute("ERROR", e);
            dispatcher.forward(request, response);
        }
    }

    /**
     *   This method is called when  parameters are entered thru the URL
     *
     * @param        HttpServletRequest
     * @param        HttpServletResponse
     *
     * @throws       ServletException, IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * This method gets the DB connection and loops through the array of project
     *      codes, creates the File object containing the map.xml file and the
     *      project path and calls the generateMapXML method to generate each
     *      project.
     *
     * @param       String[] codes     String array of project codes to be
     *                                 generated.
     *
     * @throws      Exception
     */
    public Vector generateProjects(String[] codes) throws Exception {
        Connection con = null;
        Project project = null;
        Vector vec_projs = new Vector();
        try {
            con = popConnection();
            for (int i = 0; i < codes.length; i++) {
                String proj_code = codes[i];
                project = getProject(con, proj_code);
                gl_upd = false;
                rl_upd = false;
                fl_upd = false;
                ds_upd = false;
                if (projects_path != null) {
                    String map_path = projects_path + File.separator + proj_code + File.separator + "map.xml";
                    File map_file = new File(map_path);
                    generateMapXML(con, project, map_file);
                    vec_projs.addElement(project);
                }
            }
        } catch (Exception e) {
            throw new Exception("exception thrown in generateProject method: " + e.getMessage());
        } finally {
            pushConnection(con);
        }
        return vec_projs;
    }

    /**
     *  This method queries the DB to get the project's attributes.
     *
     * @param       Connection con        DB connection
     * @param       String proj_code      The code of the project to be
     *                                    generated
     *
     * @return      Project               The model/entity class that contains
     *                                    the attributes of the project.
     *
     * @throws      SQLException
     */
    private Project getProject(Connection con, String proj_code) throws SQLException {
        String query = "select a.Proj_ID, a.Proj_Name, b.ProjCoordSys_Xmin, " + "b.ProjCoordSys_Ymin, b.ProjCoordSys_Area_Xmax, " + "b.ProjCoordSys_Area_Ymax, a.Proj_LastUpdated, " + "a.Proj_SelectedLayer, a.Proj_PatternOutlineXML " + "from project a, projectcoordsystem b " + "where a.Proj_Code = '" + proj_code + "' and " + "b.Proj_ID = a.Proj_ID";
        Statement stmt = null;
        ResultSet rs = null;
        int proj_id = -1;
        String proj_name = null;
        double ProjCoordSys_x = -1;
        double ProjCoordSys_y = -1;
        double ProjCoordSys_width = -1;
        double ProjCoordSys_height = -1;
        String proj_lastUpdated = null;
        Project project = null;
        String selected_layer = null;
        String pattern_outline = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) {
                proj_id = rs.getInt(1);
                proj_name = rs.getString(2);
                ProjCoordSys_x = rs.getDouble(3);
                ProjCoordSys_y = rs.getDouble(4);
                ProjCoordSys_width = rs.getDouble(5);
                ProjCoordSys_height = rs.getDouble(6);
                proj_lastUpdated = rs.getString(7);
                selected_layer = rs.getString(8);
                pattern_outline = rs.getString(9);
                project = new Project();
                project.setProjId(proj_id);
                project.setProjCode(proj_code);
                project.setProjName(proj_name);
                project.setProjCoordSys_height(ProjCoordSys_height);
                project.setProjCoordSys_width(ProjCoordSys_width);
                project.setProjCoordSys_x(ProjCoordSys_x);
                project.setProjCoordSys_y(ProjCoordSys_y);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date last_updated = formatter.parse(proj_lastUpdated);
                project.setProjLastupdated(last_updated);
                project.setSelectedLayer(selected_layer);
                project.setProjPatternoutlinexml(pattern_outline);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            String sql_exc = "ProjectGenerator.getProjectID - SQLException: ";
            throw new SQLException(sql_exc + e);
        }
        return project;
    }

    /**
     *  This method generates the map.xml of the project being generated.
     *       Calls the methods; generateMapDocument, generateFeatureLayer,
     *       generateRasterLayers, generateGroupLayers to compose the body
     *       of the map.xml document and then save the file to project folder.
     *
     * @param   Connection con       DB conncetion
     * @param   Project project      The model/entity class that contains
     *                               the project's attributes.
     * @param   File map_file        The object that contains the file name
     *                               map.xml and the location to save it.
     *
     * @throws  Exception
     */
    private void generateMapXML(Connection con, Project project, File map_file) throws Exception {
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            map_doc = docBuilder.newDocument();
            generation_text = new StringBuffer();
            generateMapDocument(project);
            generateFeatureLayers(con, project, "", map_doc.getDocumentElement());
            generateRasterLayers(con, project, null, map_doc.getDocumentElement());
            generateGroupLayers(con, project);
            if (generation_text.length() == 0) {
                generation_text.append(NO_UPDATES);
            }
            generation_summary.put(project.getProjCode(), generation_text.toString());
            StringWriter writer = new StringWriter();
            OutputFormat outputformat = new OutputFormat("XML", "UTF-8", true);
            DOMSerializer serializer = new XMLSerializer(writer, outputformat);
            serializer.serialize(map_doc);
            FileWriter out = new FileWriter(map_file);
            out.write(writer.toString());
            out.close();
        } catch (Exception e) {
            throw new Exception(" ProjectGenerator.generateMapXML ERROR: " + e.getMessage());
        }
    }

    /**
     *  This method generates the elements of Map based on the attributes
     *       stored in the Project model/entity object.
     *
     * @param       Project project     The model/entity class that contains
     *                                  the attributes of the project being
     *                                  generated.
     *
     * @throws      Exception
     */
    private void generateMapDocument(Project project) throws Exception {
        try {
            if (map_doc != null) {
                Element map_elem = map_doc.createElement("Map");
                map_elem.setAttribute("bounds.height", Double.toString(project.getProjCoordSys_height()));
                map_elem.setAttribute("bounds.width", Double.toString(project.getProjCoordSys_width()));
                map_elem.setAttribute("bounds.x", Double.toString(project.getProjCoordSys_x()));
                map_elem.setAttribute("bounds.y", Double.toString(project.getProjCoordSys_y()));
                map_elem.setAttribute("fixed_bounds", "true");
                map_elem.setAttribute("id", project.getProjId().toString());
                map_elem.setAttribute("selected_layer", project.getSelectedLayer());
                map_elem.setAttribute("type", MAP);
                map_doc.appendChild(map_elem);
                Element po_xml = getPatternOutline(project.getProjPatternoutlinexml());
                map_doc.getDocumentElement().appendChild(po_xml);
            }
        } catch (Exception e) {
            throw new Exception("ProjectGenerator.generateMapDocument: " + e.getMessage());
        }
    }

    /**
     *  This method queries the DB for the Feature/Vector layers associated to
     *       the current project being generated given the group. Calls the
     *       generateFeatureLayerElement method to compose the layer's elements.
     *
     *
     * @param      Connection con       DB connection
     * @param      Project project      The current project being generated
     * @param      String  group_code   The current group being handled.
     * @param      Element element      This is the element where the layer's
     *                                  related elements are to be attached.
     *
     * @throws     Exception            SQLException
     */
    private void generateFeatureLayers(Connection con, Project project, String group_code, Element element) throws Exception {
        String query = "select a.Feature_ID, a.Feature_Name, a.Feature_Path, " + "a.Feature_PatternOutlineXML, a.Layer_Type_Code, " + "b.pattern_mode_name, a.Feature_LastUpdated, " + "a.Feature_Visibility, a.Feature_Resource, " + "a.Feature_CodeColumn, a.Feature_LabelColumn " + "from featurelayer a, pattern_mode b " + "where a.Proj_ID = ? and a.Group_Code = ? and " + "a.Pattern_Mode_Code = b.pattern_mode_code " + "order by a.Feature_Order";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long feature_id = -1;
        String feature_name = null;
        String feature_lastUpdated = null;
        String feature_path = null;
        String layer_type = null;
        String pattern_mode = null;
        Featurelayer feature_layer = null;
        String pattern_outline = null;
        String feature_visibility = null;
        String feature_resource = null;
        String feature_codecol = null;
        String feature_labelcol = null;
        int project_id = project.getProjId().intValue();
        try {
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, project_id);
            pstmt.setString(2, group_code);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                feature_id = rs.getLong(1);
                feature_name = rs.getString(2);
                feature_path = rs.getString(3);
                pattern_outline = rs.getString(4);
                layer_type = rs.getString(5);
                pattern_mode = rs.getString(6);
                feature_lastUpdated = rs.getString(7);
                feature_visibility = rs.getString(8);
                feature_resource = rs.getString(9);
                feature_codecol = rs.getString(10);
                feature_labelcol = rs.getString(11);
                feature_layer = new Featurelayer();
                feature_layer.setFeatureId(feature_id);
                feature_layer.setFeatureName(feature_name);
                feature_layer.setFeaturePath(feature_path);
                feature_layer.setPatternModeCode(pattern_mode);
                feature_layer.setLayerTypeCode(layer_type);
                feature_layer.setFeaturePatternoutlinexml(pattern_outline);
                feature_layer.setFeatureVisibility(feature_visibility);
                feature_layer.setFeatureResource(feature_resource);
                feature_layer.setFeatureCodeColumn(feature_codecol);
                feature_layer.setFeatureLabelColumn(feature_labelcol);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date last_updated = formatter.parse(feature_lastUpdated);
                feature_layer.setFeatureLastupdated(last_updated);
                long date_difference = (today.getTime() - last_updated.getTime()) / (1000L * 60L * 60L * 24L);
                if (date_difference < 15 && !fl_upd) {
                    generation_text.append("<br>" + FL_UPDATES);
                    fl_upd = true;
                }
                generateFeatureLayerElement(con, element, feature_layer);
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            String sql_exc = "ProjectGenerator.generateFeatureLayers - " + " SQLException: ";
            throw new Exception(sql_exc + e);
        }
    }

    /**
     *  This method generates the necessary Elements for FeatureLayer. The
     *       attribute "resource" for FeatureResource Elements must contain the
     *       file name without the path. The bounds are not needed because
     *       they are calculated at run time. Calls the
     *       generateAttributeElements method to include the Datasets for this
     *       layer.
     *
     * @param        Connection con              DB connection
     * @param        Element element             The element where the Feature
     *                                           related elements are to be
     *                                           attached.
     * @param        Featurelayer feature_layer  The model/entity object where
     *                                          Feature's attributes are stored.
     * @throws       Exception
     */
    private void generateFeatureLayerElement(Connection con, Element element, Featurelayer feature_layer) throws Exception {
        if (map_doc != null && element != null && feature_layer != null) {
            Element layer = map_doc.createElement("Layer");
            layer.setAttribute("type", FEATURE_LAYER);
            layer.setAttribute("code_col", feature_layer.geFeatureCodeColumn());
            layer.setAttribute("label_col", feature_layer.geFeatureLabelColumn());
            Element base_layer = map_doc.createElement("BaseLayer");
            base_layer.setAttribute("name", feature_layer.getFeatureName());
            base_layer.setAttribute("type", FEATURE_LAYER);
            base_layer.setAttribute("visible", feature_layer.getFeatureVisibility());
            base_layer.setAttribute("style", feature_layer.getLayerTypeCode());
            base_layer.setAttribute("layer_id", feature_layer.getFeatureId().toString());
            layer.appendChild(base_layer);
            Element po_xml = getPatternOutline(feature_layer.getFeaturePatternoutlinexml());
            layer.appendChild(po_xml);
            Element feature_resource = map_doc.createElement("FeatureResource");
            feature_resource.setAttribute("name", feature_layer.getFeaturePath());
            feature_resource.setAttribute("resource", feature_layer.geFeatureResource());
            feature_resource.setAttribute("type", FILE_RESOURCE);
            layer.appendChild(feature_resource);
            generateAttributeElements(con, layer, feature_layer.getFeatureId());
            element.appendChild(layer);
        }
    }

    /**
     *  This method generates the Attribute and Externalizer elements to
     *       include the datasets.
     *
     * @param        Connection con         DB connection
     * @param        Element element        The element where the Attribute
     *                                      and Externalizer will be attached.
     * @param        Long feature_id        The id of the layer where the
     *                                      Datasets are associated.
     *
     * @throws       Exception              SQLException
     */
    private void generateAttributeElements(Connection con, Element element, Long feature_id) throws Exception {
        String query = "select a.Dataset_ID, a.Dataset_Name, " + "a.Dataset_LastUpdated " + "from dataset a, layerdataset b " + "where b.Layer_ID = ? and a.Dataset_ID = b.Dataset_ID " + "order by b.LayerDataset_Index asc";
        Element attribute = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        if (map_doc != null && element != null && feature_id != null) {
            attribute = map_doc.createElement("Attribute");
            attribute.setAttribute("name", "none");
            attribute.setAttribute("resource", "");
            attribute.setAttribute("type", FILE_RESOURCE);
            element.appendChild(attribute);
            pstmt = con.prepareStatement(query);
            pstmt.setLong(1, feature_id.longValue());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String dataset_id = new Integer(rs.getInt(1)).toString();
                String name = rs.getString(2);
                String dataset_lastUpdated = rs.getString(3);
                attribute = map_doc.createElement("Attribute");
                attribute.setAttribute("name", name);
                attribute.setAttribute("resource", "");
                attribute.setAttribute("type", FILE_RESOURCE);
                Element externalizer = map_doc.createElement("Externalizer");
                externalizer.setAttribute("dataset_id", dataset_id);
                externalizer.setAttribute("resource", GIEWS_INI);
                externalizer.setAttribute("type", GIEWS_EXTERNALIZER);
                attribute.appendChild(externalizer);
                element.appendChild(attribute);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date last_updated = formatter.parse(dataset_lastUpdated);
                long date_difference = (today.getTime() - last_updated.getTime()) / (1000L * 60L * 60L * 24L);
                if (date_difference < 15 && !ds_upd) {
                    generation_text.append("<br>" + DS_UPDATES);
                    ds_upd = true;
                }
            }
            rs.close();
            pstmt.close();
        }
    }

    /**
     *  This method queries the DB for the Raster images associated to the
     *       current project given the group. Calls the
     *       generateRasterLayerElement method to compose the Raster's elements.
     *
     *  About the QUERY: The query takes the latest 3 mos of Raster images, if
     *       the Raster is periodically generated.
     *
     *  Special CASE consideration: For SPOT Vegetation and Estimated Rainfall
     *      groups, apart from the DA (Difference Averages), there are Raster
     *      Averages, so this is handled separately so that the latest Average
     *      will be the one considerd.
     *
     *
     * @param      Connection con       DB connection
     * @param      Project project      The current project being generated
     * @param      Group   group        The current group being handled.
     * @param      Element element      This is the element where the Raster's
     *                                  related elements are to be attached.
     *
     * @throws     Exception            SQLException
     */
    private void generateRasterLayers(Connection con, Project project, Group group, Element element) throws Exception {
        String query = null;
        StringBuffer parent_group = null;
        String group_code = "";
        if (group != null && new Boolean(group.getPeriodical()).booleanValue()) {
            if (group != null && group.getCode().contains("months")) {
                parent_group = new StringBuffer();
                parent_group.append("%" + group.getParent().trim() + "%");
            } else {
                parent_group = new StringBuffer();
                parent_group.append("%%");
            }
            query = "select Raster_ID, Raster_Name, Raster_Path, " + "Raster_Visibility, Raster_TimePeriod, " + "Raster_CLR, Raster_Group_Parent, " + "Layer_Type_Code, Raster_LastUpdated, " + "Raster_Resource, CLR_Resource, Raster_TimeCode " + "from rasterlayer where Proj_ID = ? and Group_Code = ? " + "and raster_timeperiod > " + "(SELECT DATE_SUB(max(Raster_TimePeriod), " + "INTERVAL 2 MONTH) from rasterlayer) " + "and LTRIM(Raster_Group_Parent) like ? " + "order by raster_timeperiod desc, Raster_Name, " + "raster_order";
        } else {
            query = "select Raster_ID, Raster_Name, Raster_Path, " + "Raster_Visibility, Raster_TimePeriod, " + "Raster_CLR, Raster_Group_Parent, " + "Layer_Type_Code, Raster_LastUpdated, " + "Raster_Resource, CLR_Resource, Raster_TimeCode " + "from rasterlayer where Proj_ID = ? and Group_Code = ? " + "order by raster_timeperiod desc,Raster_Name,raster_order";
        }
        if (group != null) {
            group_code = group.getCode();
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long raster_id = -1;
        String raster_name = null;
        String raster_lastUpdated = null;
        String raster_path = null;
        String layer_type = null;
        String raster_visibility = null;
        String time_period = null;
        String raster_clr = null;
        String group_parent = null;
        String raster_resource = null;
        String clr_resource = null;
        String time_code = null;
        int project_id = project.getProjId().intValue();
        try {
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, project_id);
            pstmt.setString(2, group_code);
            if (parent_group != null) {
                pstmt.setString(3, parent_group.toString());
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                raster_id = rs.getLong(1);
                raster_name = rs.getString(2);
                raster_path = rs.getString(3);
                raster_visibility = rs.getString(4);
                time_period = rs.getString(5);
                if (rs.getString(6) != null) {
                    raster_clr = rs.getString(6).trim();
                } else {
                    raster_clr = rs.getString(6);
                }
                if (rs.getString(7) != null) {
                    group_parent = rs.getString(7).trim();
                } else {
                    group_parent = rs.getString(7);
                }
                if (rs.getString(8) != null) {
                    layer_type = rs.getString(8).trim();
                } else {
                    layer_type = "0";
                }
                if (rs.getString(9) != null) {
                    raster_lastUpdated = rs.getString(9);
                } else {
                    Date today_date = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    raster_lastUpdated = formatter.format(today_date);
                }
                raster_resource = rs.getString(10);
                clr_resource = rs.getString(11);
                time_code = rs.getString(12);
                Rasterlayer raster_layer = new Rasterlayer();
                raster_layer.setRasterID(raster_id);
                raster_layer.setRasterName(raster_name);
                raster_layer.setRasterPath(raster_path);
                raster_layer.setRasterVisibility(raster_visibility);
                raster_layer.setRasterCLR(raster_clr);
                raster_layer.setRasterGroupParent(group_parent);
                raster_layer.setLayerTypeCode(layer_type);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date dtime_period = formatter.parse(time_period);
                raster_layer.setRasterTimePeriod(dtime_period);
                Date last_updated = formatter.parse(raster_lastUpdated);
                raster_layer.setRasterLastUpdated(last_updated);
                raster_layer.setRasterResource(raster_resource);
                raster_layer.setCLRResource(clr_resource);
                long date_difference = (today.getTime() - last_updated.getTime()) / (1000L * 60L * 60L * 24L);
                if (date_difference < 15 && !rl_upd) {
                    generation_text.append("<br>" + RL_UPDATES);
                    rl_upd = true;
                }
                generateRasterLayerElement(con, element, raster_layer, project);
                if ((group_code.equals(ESTIMATED_RAINFALL) || group_code.equals(VEGETATION_INDEX)) && (!raster_name.contains("DA") && !raster_name.contains("DP") && !raster_name.contains("DY"))) {
                    PreparedStatement apstmt = null;
                    ResultSet ars = null;
                    String sql = "select Raster_ID, Raster_Name, Raster_Path, " + "Raster_Visibility, Raster_TimePeriod, " + "Raster_CLR, Raster_Group_Parent, " + "Layer_Type_Code, Raster_LastUpdated, " + "Raster_Resource, CLR_Resource, " + "Raster_TimeCode " + "from rasterlayer where Proj_ID = ? and " + "Group_Code = ? and Raster_Name like " + "'%Average%' and " + "MONTH(raster_timeperiod) = ? and " + "DAYOFMONTH(raster_timeperiod) = ? and " + "Raster_TimeCode = ? " + "order by raster_timeperiod desc";
                    int month = Integer.parseInt(time_period.substring(5, 7));
                    int day = Integer.parseInt(time_period.substring(8, 10));
                    apstmt = con.prepareStatement(sql);
                    apstmt.setInt(1, project_id);
                    apstmt.setString(2, group_code);
                    apstmt.setInt(3, month);
                    apstmt.setInt(4, day);
                    apstmt.setString(5, time_code);
                    ars = apstmt.executeQuery();
                    if (ars.next()) {
                        raster_id = ars.getLong(1);
                        raster_name = ars.getString(2);
                        raster_path = ars.getString(3);
                        raster_visibility = ars.getString(4);
                        time_period = ars.getString(5);
                        if (ars.getString(6) != null) {
                            raster_clr = ars.getString(6).trim();
                        } else {
                            raster_clr = ars.getString(6);
                        }
                        if (ars.getString(7) != null) {
                            group_parent = ars.getString(7).trim();
                        } else {
                            group_parent = ars.getString(7);
                        }
                        if (ars.getString(8) != null) {
                            layer_type = ars.getString(8).trim();
                        } else {
                            layer_type = "0";
                        }
                        raster_lastUpdated = ars.getString(9);
                        raster_resource = ars.getString(10);
                        clr_resource = ars.getString(11);
                        time_code = ars.getString(12);
                        Rasterlayer avg_raster_layer = new Rasterlayer();
                        avg_raster_layer.setRasterID(raster_id);
                        avg_raster_layer.setRasterName(raster_name);
                        avg_raster_layer.setRasterPath(raster_path);
                        avg_raster_layer.setRasterVisibility(raster_visibility);
                        avg_raster_layer.setRasterCLR(raster_clr);
                        avg_raster_layer.setRasterGroupParent(group_parent);
                        avg_raster_layer.setLayerTypeCode(layer_type);
                        Date avg_dtime_period = formatter.parse(time_period);
                        avg_raster_layer.setRasterTimePeriod(avg_dtime_period);
                        Date avg_last_updated = formatter.parse(raster_lastUpdated);
                        avg_raster_layer.setRasterLastUpdated(avg_last_updated);
                        avg_raster_layer.setRasterResource(raster_resource);
                        avg_raster_layer.setCLRResource(clr_resource);
                        generateRasterLayerElement(con, element, avg_raster_layer, project);
                    }
                }
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            String sql_exc = "ProjectGenerator.generateRasterLayers - " + " SQLException: ";
            throw new Exception(sql_exc + e);
        }
    }

    /**
     *  This method generates the necessary Elements for RasterLayer. The
     *       attribute "resource" for both RasterResource and LegendResource
     *       Elements must contain the file name without the path. The bounds
     *       should also be included. For rasters, the map bounds are taken
     *       because using the Raster's bounds from the DB does not produce
     *       desired result.
     *
     * @param        Connection con            DB connection
     * @param        Element element           The element where the Raster
     *                                         related elements are to be
     *                                         attached.
     * @param        Rasterlayer raster_layer  The model/entity object where
     *                                         Raster's attributes are stored.
     * @param        Project project           The model/entity object where
     *                                         current Project's attributes
     *                                         are stored.
     * @throws       Exception
     */
    private void generateRasterLayerElement(Connection con, Element element, Rasterlayer raster_layer, Project project) throws Exception {
        if (map_doc != null && element != null && raster_layer != null) {
            Element layer = map_doc.createElement("Layer");
            layer.setAttribute("type", RASTER_LAYER);
            layer.setAttribute("raster_format", TIFF_RASTER_TYPE);
            Element base_layer = map_doc.createElement("BaseLayer");
            base_layer.setAttribute("name", raster_layer.getRasterName());
            base_layer.setAttribute("type", RASTER_LAYER);
            base_layer.setAttribute("visible", raster_layer.getRasterVisibility());
            base_layer.setAttribute("style", raster_layer.getLayerTypeCode());
            base_layer.setAttribute("bounds.height", Double.toString(project.getProjCoordSys_height()));
            base_layer.setAttribute("bounds.width", Double.toString(project.getProjCoordSys_width()));
            base_layer.setAttribute("bounds.x", Double.toString(project.getProjCoordSys_x()));
            base_layer.setAttribute("bounds.y", Double.toString(project.getProjCoordSys_y()));
            base_layer.setAttribute("layer_id", (new Long(raster_layer.getRasterID())).toString());
            layer.appendChild(base_layer);
            Element raster_resource = map_doc.createElement("RasterResource");
            raster_resource.setAttribute("name", raster_layer.getRasterPath());
            raster_resource.setAttribute("resource", raster_layer.getRasterResource());
            raster_resource.setAttribute("type", FILE_RESOURCE);
            layer.appendChild(raster_resource);
            Element legend_resource = map_doc.createElement("LegendResource");
            legend_resource.setAttribute("name", raster_layer.getRasterCLR());
            legend_resource.setAttribute("resource", raster_layer.getCLRResource());
            legend_resource.setAttribute("type", FILE_RESOURCE);
            layer.appendChild(legend_resource);
            element.appendChild(layer);
        }
    }

    /**
     * This method creates or looks the Group hierarchy and calls the
     *      generateGroupDocumentHierarchy to create the Element and append
     *      to the document.
     *
     * @param      Connection con      DB connection
     * @param      Project project     The project being generated
     *
     * @throws     Exception
     */
    private void generateGroupLayers(Connection con, Project project) throws Exception {
        ArrayList groups = getGroupLayers(con, project.getProjId().intValue());
        Iterator iterator = groups.iterator();
        Element element = null;
        if (map_doc != null) {
            while (iterator.hasNext()) {
                Group group = (Group) iterator.next();
                List order = getHierarchy(groups, group);
                java.util.Iterator ilist = order.iterator();
                while (ilist.hasNext()) {
                    Group order_group = (Group) ilist.next();
                    if (order_group.getTree() != null && !order_group.getTree().equals("")) {
                        String tree = "";
                        String root = "";
                        StringBuffer node = new StringBuffer();
                        String name = "";
                        StringTokenizer st = new StringTokenizer(order_group.getTree(), "@");
                        int ctr = st.countTokens();
                        st = null;
                        if (ctr == 1) {
                            root = "Map/Layer/BaseLayer";
                        } else {
                            root = "Map/Layer";
                        }
                        node.append(root);
                        name = order_group.getParent();
                        for (int i = 1; i < ctr; i++) {
                            if (i == ctr - 1) {
                                tree = "/GroupLayer/Layer/BaseLayer";
                            } else {
                                tree = "/GroupLayer/Layer";
                            }
                            node.append(tree);
                        }
                        NodeList list = XPathAPI.selectNodeList(map_doc, node.toString());
                        for (int i = 1; i < list.getLength(); i++) {
                            Element ele = (Element) list.item(i);
                            boolean generate = true;
                            if (ele.getAttribute("name") != null && ele.getAttribute("name").equals(name)) {
                                element = (Element) ele.getNextSibling();
                                if (group.getCode().equals(order_group.getCode())) {
                                    generateGroupDocumentHierarchy(con, group, element, project);
                                }
                            }
                        }
                    } else {
                        element = map_doc.getDocumentElement();
                        if (!(order.size() > 1)) {
                            generateGroupDocumentHierarchy(con, group, element, project);
                        }
                    }
                }
            }
        }
    }

    /**
     *  This method compose the Document fragment that contains the Group
     *       and the layers or subgroups, if there are any associated.
     *
     * @param    Connection con      DB connection
     * @param    Group group         Current group being handled
     * @param    Element elem        Where the composed elements will be
     *                               appended.
     * @param    Project project     Current project being generated.
     *
     * @throws   Exception
     */
    private void generateGroupDocumentHierarchy(Connection con, Group group, Element elem, Project project) throws Exception {
        Element group_layer = null;
        if (elem != null && group != null) {
            Element layer = map_doc.createElement("Layer");
            layer.setAttribute("type", GROUP_LAYER);
            Element base_layer = map_doc.createElement("BaseLayer");
            base_layer.setAttribute("name", group.getLabel());
            base_layer.setAttribute("type", GROUP_LAYER);
            base_layer.setAttribute("visible", group.getVisibility());
            layer.appendChild(base_layer);
            group_layer = map_doc.createElement("GroupLayer");
            generateFeatureLayers(con, project, group.getCode(), group_layer);
            generateRasterLayers(con, project, group, group_layer);
            layer.appendChild(group_layer);
            elem.appendChild(layer);
        }
    }

    /**
     *  This method queries the DB for all groups associated to the given
     *       project id.
     *
     * @param     Connection con   Db connection.
     * @param     int project_id   Current project being generated.
     *
     * @return    ArrayList        Contains the list of Group objects associated
     *                             with this project.
     *
     * @throws    Exception        SQLException
     */
    private ArrayList getGroupLayers(Connection con, int project_id) throws Exception {
        String query = "select Group_Code, Group_Name, Group_Parent, " + "Group_Depth, Group_Order, Group_Visibility, " + "Group_Periodical, Group_LastUpdated " + "from grouplayer where Proj_ID = ? " + "order by Group_Order, Group_Depth";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList groups = new ArrayList();
        try {
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, project_id);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String code = rs.getString(1);
                String name = rs.getString(2);
                String parent = rs.getString(3);
                int depth = rs.getInt(4);
                int seq = rs.getInt(5);
                String visibility = rs.getString(6);
                String periodical = rs.getString(7);
                String group_lastUpdated = rs.getString(8);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date last_updated = formatter.parse(group_lastUpdated);
                long date_difference = (today.getTime() - last_updated.getTime()) / (1000L * 60L * 60L * 24L);
                if (date_difference < 15 && !gl_upd) {
                    generation_text.append("<br>" + GL_UPDATES);
                    gl_upd = true;
                }
                Group group = new Group(code, name, parent, depth, seq, visibility, periodical, last_updated);
                groups.add(group);
            }
        } catch (Exception e) {
            throw new Exception(" Exception getGroupLayer : " + e.getMessage());
        }
        return groups;
    }

    /**
     *  This method looks for the parent of the current group until it finds the
     *       root/parent group. The heirarchy/tree starts from the root to the
     *       current group and stored in a list.
     *
     * @param   ArrayList groups    All groups and subgroups associated to the
     *                              current project being generated.
     * @param   Group group         Current group being handled.
     *
     * @return  List                Contains the arranged groups starting from
     *                              the root group to the current group.
     */
    private List getHierarchy(ArrayList groups, Group group) {
        List hierarchy = new ArrayList();
        int depth = group.getDepth();
        StringBuffer sb = null;
        if (depth == 0) {
            hierarchy.add(0, group);
        } else {
            hierarchy.add(0, group);
            for (int i = 0; i <= depth - 1; i++) {
                group = getParent(groups, group.getParent());
                hierarchy.add(i + 1, group);
            }
            Collections.reverse(hierarchy);
            for (int i = 1; i < hierarchy.size(); i++) {
                Group grp = (Group) hierarchy.get(i);
                sb = new StringBuffer();
                if (i == 1) {
                    sb.append("@" + grp.getParent());
                } else {
                    group = getParent((ArrayList) hierarchy, grp.getParent());
                    if (group.getCode() != null && !group.getCode().equals("")) {
                        sb.append("@" + group.getCode());
                    }
                    if (group.getTree() != null && !group.getTree().equals("")) {
                        sb.append("@" + group.getTree());
                    }
                }
                if (!sb.equals("")) {
                    grp.setTree(sb.toString());
                }
            }
        }
        return hierarchy;
    }

    /**
     *  This class looks for the Parent of the group specified from the list
     *       of all groups of the current project being generated.
     *
     * @param   ArrayList groups    All groups and subgroups associated to the
     *                              current project being generated.
     * @param   String parent       Contains the parent group code.
     *
     * @return  Group               Group object matching the parent's code.
     */
    private Group getParent(ArrayList groups, String parent) {
        java.util.Iterator i = groups.iterator();
        Group group = null;
        while (i.hasNext()) {
            group = (Group) i.next();
            if (group.getCode() != null && group.getCode().equalsIgnoreCase(parent)) {
                return group;
            }
        }
        return group;
    }

    /**
	 *  This method converts a String XML snippet to Element document.
	 *
	 * @param       String xml      Contains the xml snippet of PatternOutline
     *                              of layer and map.
     * @return      Element         The converted xml String to Element.
     *
	 * @throws      Exception       IOException
	 */
    private Element getPatternOutline(String xml) throws Exception {
        Element element = null;
        if (xml != null && xml.length() != 0) {
            StringReader ir = new StringReader(xml);
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(ir));
            element = (Element) map_doc.importNode(document.getDocumentElement(), true);
        }
        return element;
    }

    /**
     *  This method asks for a DB connection from the connection pool.
     *
     * @return      Connection    Available DB connection.
     */
    private Connection popConnection() {
        Connection con = null;
        dbConnectionManager manager = dbConnectionManagerPool.getConnectionManager(database_ini);
        con = manager.popConnection();
        return con;
    }

    /**
     *  This method puts back the DB connection into the connection pool to be
     *       used by other resource.
     *
     * @param     Connection     DB connection used in this class.
     */
    private void pushConnection(Connection con) {
        if (database_ini != null && con != null) {
            dbConnectionManagerPool.getConnectionManager(database_ini).pushConnection(con);
        }
    }
}

/**
 *  This is an entity/model class to store group attributes.
 *
 * @param   String code           Group code
 * @param   String label          Group name/label.
 * @param   String parent         Group's parent code.
 * @param   int depth             The node of this group in the tree.
 * @param   int seq               The order of this group from the list
 * @param   String visiblity      If the group will be displayed at initial load
 * @param   String group_hierarchy   Contains the hierarchy of this group in
 *                                   the tree.
 * @param   String periodical        If this group is generated periodical (this
 *                                   is specifically for rasters.)
 */
class Group {

    private String code;

    private String label;

    private String parent;

    private int depth;

    private int seq;

    private String visibility;

    private String group_hierarchy = null;

    private String periodical;

    private Date last_updated;

    Group(String code, String label, String parent, int depth, int seq, String visibility, String periodical, Date last_updated) {
        this.code = code;
        this.label = label;
        this.parent = parent;
        this.depth = depth;
        this.seq = seq;
        this.visibility = visibility;
        this.periodical = periodical;
        this.last_updated = last_updated;
    }

    public String getVisibility() {
        return this.visibility;
    }

    public String getCode() {
        return this.code;
    }

    public String getLabel() {
        return this.label;
    }

    public String getParent() {
        return this.parent;
    }

    public int getDepth() {
        return this.depth;
    }

    public int getSeq() {
        return this.seq;
    }

    public String getTree() {
        return this.group_hierarchy;
    }

    public String getPeriodical() {
        return this.periodical;
    }

    public void setTree(String group_hierarchy) {
        this.group_hierarchy = group_hierarchy;
    }

    public Date getLastUpdated() {
        return this.last_updated;
    }
}
