package org.fao.waicent.xmap2D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.fao.waicent.attributes.Attributes;
import org.fao.waicent.attributes.Axis;
import org.fao.waicent.attributes.Context;
import org.fao.waicent.attributes.Extent;
import org.fao.waicent.attributes.Key;
import org.fao.waicent.attributes.MatrixInterface;
import org.fao.waicent.kids.Configuration;
import org.fao.waicent.kids.server.kidsError;
import org.fao.waicent.util.FileResource;
import org.fao.waicent.util.FileResourceI;
import org.fao.waicent.util.FileResourceVector;
import org.fao.waicent.util.FileSystem;
import org.fao.waicent.util.Progress;
import org.fao.waicent.util.ProjectResource;
import org.fao.waicent.util.Translate;
import org.fao.waicent.util.XMLUtil;
import org.fao.waicent.util.XMLUtilOld;
import org.fao.waicent.util.XMLable;
import org.fao.waicent.util.XPatternOutline;
import org.fao.waicent.util.XPatternPaint;
import org.fao.waicent.xmap2D.coordsys.LinearUnit;
import org.fao.waicent.xmap2D.coordsys.ProjectionCategories;
import org.fao.waicent.xmap2D.layer.BaseGroupLayer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MapContext implements XMLable, Cloneable {

    public static final String FILENAME = "projects.xml";

    private Translate translate_name = null;

    protected FileResourceVector map_files;

    protected int selected_map_index = -1;

    protected transient FileResource maps_resource = null;

    protected transient Map map = null;

    protected transient Map map2 = null;

    protected transient Vector maps = new Vector(2);

    public int selected_map_image = 0;

    protected transient boolean modified = true;

    protected transient Hashtable map_cache = null;

    private String map_name = null;

    private Map load_map = null;

    private Document projects_document = null;

    private Configuration configuration = null;

    private boolean legend_use_less_more_simbol = false;

    private boolean legend_use_dash = false;

    private final int space_between_legend_ruler = 5;

    protected Vector map_id_vec;

    protected Vector translate_name_vec = null;

    private String global_home = null;

    public String getGlobalHome() {
        return global_home;
    }

    public void setGlobalHome(String gh) {
        this.global_home = gh;
    }

    public String getHome() {
        return maps_resource.getHome();
    }

    /**
     * constructor for exporting projects
     */
    public MapContext(String directory) throws IOException {
        this(directory, true);
    }

    /**
     * constructor for exporting projects
     */
    public MapContext(String directory, boolean append) throws IOException {
        this(directory, append, new Configuration());
    }

    public MapContext(String directory, Configuration configuration) throws IOException {
        this(directory, true, configuration);
    }

    public MapContext(String directory, boolean append, Configuration configuration) throws IOException {
        this.configuration = configuration;
        maps_resource = new FileResource("Map List", FILENAME, directory);
        map_id_vec = new Vector();
        if (append && maps_resource.exists()) {
            this.projects_document = XMLUtil.loadDocument(maps_resource);
            load(projects_document, projects_document.getDocumentElement());
        } else {
            map_files = new FileResourceVector();
            translate_name_vec = new Vector();
        }
    }

    public MapContext(String directory, boolean append, Configuration configuration, String global_home) throws IOException {
        this(directory, append, configuration);
        this.global_home = global_home;
    }

    public boolean isReadOnly() {
        boolean readonly = (getHome() != null && !new File(getHome()).canWrite()) || maps_resource.isReadOnly();
        System.out.println("MAP CONTEXT RESULTS IN READ MODE = " + readonly);
        return readonly;
    }

    public void load(Document doc, Element ele) throws IOException {
        XMLUtil.checkType(projects_document, ele, this);
        map_files = new FileResourceVector();
        map_id_vec = new Vector();
        translate_name_vec = new Vector();
        for (int i = 0; i < ele.getElementsByTagName("Map").getLength(); i++) {
            Element map_element = (Element) ele.getElementsByTagName("Map").item(i);
            FileResource resource = new FileResource(projects_document, map_element, configuration.getLanguageCode());
            resource.setHome(map_element.getAttribute("home"));
            if (resource.getHome() == null || resource.getHome().length() == 0) {
                resource.setHome(maps_resource.getHome());
            }
            map_files.addElement(resource);
            if (resource.getExtentCode() != null && !resource.getExtentCode().equals("")) {
                map_id_vec.addElement(resource.getExtentCode());
            } else {
                map_id_vec.addElement(map_element.getAttribute("name"));
            }
            try {
                Element label_element = XMLUtil.getChild(doc, map_element, "Label");
                String default_name = resource.getName();
                if (label_element != null) {
                    translate_name = new Translate(default_name, label_element);
                } else {
                    translate_name = new Translate(default_name);
                    translate_name.addLabel(configuration.getDefaultLanguageCode(), default_name);
                }
                translate_name_vec.addElement(translate_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            selected_map_index = Integer.parseInt(ele.getAttribute("selected_map_index"));
        } catch (Exception e) {
            System.out.println("MapContext.load(doc, ele) selected_map_index not found!");
            selected_map_index = -1;
        }
        modified = false;
    }

    /**
    * ali.safarnejad:20060910: return the translated label for the map specified by index.
    */
    public String getLabel(int index) {
        if (index < translate_name_vec.size()) {
            return ((Translate) translate_name_vec.elementAt(index)).getLabel(configuration.getLanguageCode());
        } else {
            System.out.println("MapContext.getLabel(" + index + "): No Translate element for this Map!");
            return "";
        }
    }

    /**
     *  @deprecated 
     *  ali.safarnejad@fao.org:20060825. use the translate_name_vec object to get labels in various
     *  languages, not the FileResource class.
     */
    public void changeLanguage() {
        System.out.println("DEPRECATED METHOD: MapContext.changeLanguage(). Please use translate_name_vec object");
    }

    public void save() throws IOException {
        XMLUtilOld.save(maps_resource, this, "List");
    }

    public void save(Document doc, Element ele) throws IOException {
        save(doc, ele, configuration.getDefaultLanguageCode());
    }

    public void save(Document doc, Element ele, String lang) throws IOException {
        XMLUtil.setType(doc, ele, this);
        for (int j = 0; j < map_files.size(); j++) {
            Element map_ele = doc.createElement("Map");
            ele.appendChild(map_ele);
            if (map_files.at(j).getHome() != null && maps_resource.getHome() != null && !new File(map_files.at(j).getHome()).getCanonicalPath().equals(new File(maps_resource.getHome()).getCanonicalPath())) {
                map_ele.setAttribute("home", map_files.at(j).getHome());
            }
            Element label_ele = doc.createElement("Label");
            if (translate_name_vec.elementAt(j) != null) {
                ((Translate) (translate_name_vec.elementAt(j))).appendToElement(label_ele);
            }
            map_ele.appendChild(label_ele);
            map_files.at(j).save(doc, map_ele, lang);
        }
        ele.setAttribute("selected_map_index", Integer.toString(selected_map_index));
        modified = false;
    }

    /**
   * @deprecated
   * ali.safarnejad:20060826: not used anymore.
   */
    public void saveProjectDOM(int selected_index) throws IOException {
        System.out.println("DEPRECATED METHOD: MapContext.saveProjectDOM() called!!!");
    }

    public void sortResourceByName() {
        FileResourceVector new_map_files_resource = new FileResourceVector();
        for (int i = 0; i < map_files.size(); i++) {
            int j = 0;
            for (j = 0; j < new_map_files_resource.size(); j++) {
                if (map_files.at(i).getName().compareTo(new_map_files_resource.at(j).getName()) < 0) {
                    break;
                }
            }
            new_map_files_resource.add(j, map_files.at(i));
        }
        map_files = new_map_files_resource;
        selected_map_index = 0;
    }

    /**
   * @deprecated
   * ali.safarnejad:20060826: please use the reorderProjects(int) method instead.
   */
    public void reorderResource(String list[]) {
        FileResourceVector new_map_files_resource = new FileResourceVector();
        for (int i = 0; i < list.length; i++) {
            new_map_files_resource.add(map_files.at(map_files.indexOf(list[i])));
        }
        map_files = new_map_files_resource;
        selected_map_index = -1;
    }

    public void reorderProjects(int list[]) {
        FileResourceVector new_map_files_resource = new FileResourceVector();
        Vector new_translate_name_vec = new Vector();
        int sMap = selected_map_index;
        for (int i = 0; i < list.length; i++) {
            new_map_files_resource.add(map_files.at(list[i]));
            new_translate_name_vec.add(translate_name_vec.elementAt(list[i]));
            if (selected_map_index == list[i]) {
                sMap = i;
            }
        }
        map_files = new_map_files_resource;
        translate_name_vec = new_translate_name_vec;
        try {
            int nMap = setSelectedMapIndex(sMap);
            if (nMap != sMap) {
                setSelectedMapIndex(nMap);
            }
            save();
        } catch (Exception ioex) {
            System.out.println("Error reordering Projects (save failed) : " + ioex.getMessage());
        }
    }

    public FileResourceVector getMapFiles() {
        return map_files;
    }

    public int size() {
        return map_files.size();
    }

    public FileResource at(int i) {
        return map_files.at(i);
    }

    /**
     *  alisaf: added to return the name of the current map.  This overrides the
     *  getName() method in the Map class, since that class does not get updated
     *  when a language change event takes place.
     */
    public String getSelectedMapName() {
        if (map_files.size() > selected_map_index && !(selected_map_index < 0)) return getLabel(selected_map_index); else return null;
    }

    public String getSelectedMapCode() {
        if (map_files.size() > selected_map_index && !(selected_map_index < 0)) return map_files.at(selected_map_index).getExtentCode(); else return null;
    }

    public int getSelectedMapIndex() {
        return selected_map_index;
    }

    public int findMapIndex(String name) {
        int map_index = -1;
        for (int i = 0; i < map_files.size(); i++) {
            if (at(i).getName().equals(name)) {
                map_index = i;
                break;
            }
        }
        return map_index;
    }

    public boolean reloadCurrentMap() {
        try {
            map = loadMap(selected_map_index);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     *  alisaf: this method will get the map pointed at by the argument index
     *  and set the map member attribute of this MapContext to it.
     *  It will also set the member attribute 'selected_map_index' equal to the
     *  index of the selected map.
     *
     *  @param  i  The index of the map to select and load.
     *  @return    Index of the selected map.
     */
    public int setSelectedMapIndex(int i) throws IOException {
        if (i >= map_files.size()) {
            i = map_files.size() - 1;
        }
        if (selected_map_index != -1 && selected_map_index == i && map != null) {
            return selected_map_index;
        }
        if (i != -1) {
            map = loadMap(i);
            if (maps.isEmpty()) {
                maps.addElement(map);
            } else {
                maps.setElementAt(map, 0);
            }
        } else {
            map = null;
        }
        selected_map_index = i;
        return selected_map_index;
    }

    public void saveAttributes() throws IOException {
        if (getMap() != null) {
            getMap().saveAttributes();
        }
    }

    protected void deleteMap(int i) {
        if (map_cache != null) {
            map_cache.remove(getMapFiles().at(i).getName());
        }
        Map.delete(getMapFiles().at(i).getName(), getMapFiles().at(i).getHome());
    }

    public void setCacheMaps(boolean flag) {
        if (flag == true) {
            if (map_cache == null) {
                map_cache = new Hashtable();
                map_cache.put(map_name, load_map);
            }
        } else {
            map_cache = null;
        }
    }

    public boolean getCacheMaps() {
        return (map_cache != null);
    }

    public Map loadMap(int i) throws IOException {
        load_map = null;
        map_name = getMapFiles().at(i).getName();
        if (map_cache != null && map_cache.containsKey(map_name)) {
            load_map = (Map) map_cache.get(map_name);
        }
        if (load_map == null) {
            if (this.global_home != null) {
                load_map = new Map(getMapFiles().at(i).getResource(), getMapFiles().at(i).getName(), getMapFiles().at(i).getHome(), configuration, this.global_home);
            } else {
                load_map = new Map(getMapFiles().at(i).getResource(), getMapFiles().at(i).getName(), getMapFiles().at(i).getHome(), configuration);
            }
            if (load_map != null) {
                if (map_cache != null) {
                    map_cache.put(map_name, load_map);
                }
            }
        }
        return load_map;
    }

    public Map loadMap(String name) throws IOException {
        return loadMap(findMapIndex(name));
    }

    public void setMapName(int i, String name) throws IOException {
        String oldName = getMapFiles().at(i).getName();
        FileResource res = new FileResource(getMapFiles().at(i).getName(), "map.xml", getMapFiles().at(i).getHome() + File.separatorChar + getMapFiles().at(i).getResource() + File.separatorChar);
        String old_map_home = res.getHome();
        String new_map_home = Map.getFileResource(name, getMapFiles().at(i).getHome()).getHome();
        boolean success = true;
        if (old_map_home != null && !new_map_home.equals(old_map_home)) {
            try {
                FileSystem.copyDirectory(old_map_home, new_map_home);
            } catch (Exception ex) {
                ex.printStackTrace();
                success = false;
            }
            if (success) {
                getMapFiles().at(i).setName(name);
                getMapFiles().at(i).setResource(name);
                modified = true;
            }
        }
        if (!success) {
            return;
        }
        if (selected_map_index == i && map != null) {
            map.setName(name);
            map.setOldName(oldName);
            map.setStatus(Map.UPDATED);
        }
        if (map_cache != null) {
            Map tmpMap = (Map) map_cache.get(oldName);
            if (tmpMap != null) {
                map_cache.remove(oldName);
                tmpMap.setOldName(oldName);
                tmpMap.setStatus(Map.UPDATED);
                map_cache.put(getMapFiles().at(i).getName(), tmpMap);
            }
        }
    }

    /**
     *  alisaf: return the current map in context.
     *  @return Map This MapContext's member 'map' attribute.
     */
    public Map getMap() {
        return getMap(selected_map_image);
    }

    /**
     *  ali.safarnejad20060220: return the map in context based on input index.
     *  @return Map This MapContext's member 'map' attribute.
     */
    public Map getMap(int i) {
        if (maps.size() > i && !(i < 0)) return (Map) maps.elementAt(i); else return null;
    }

    public int getMapsSize() {
        return maps.size();
    }

    private static int getLayerLegendWidth(FeatureLayer flay, FontMetrics fm) {
        int PRE = 2;
        int MIDDLE = 2;
        int POST = 2;
        int ICON_WIDTH = 18;
        return PRE + ICON_WIDTH + MIDDLE + fm.stringWidth(flay.getName()) + POST;
    }

    private Dimension getPreferredRulerSize(FontMetrics fm) {
        if (!isLegendDisplayDistanceRuler()) {
            return new Dimension(0, 0);
        }
        Point2D centro = getMap().getMapCenter();
        Point left = new Point((int) centro.getX() - 50, (int) centro.getY());
        Point right = new Point((int) centro.getX() + 50, (int) centro.getY());
        double distance = getMap().getSphericalDistance(left, right, getMap().getDistanceUnit());
        long distance_l = Math.round(distance);
        String distance_string = new Long(distance_l).toString() + " " + getMap().getDistanceUnit().getAbbreviation();
        int distance_string_size = fm.stringWidth(distance_string);
        int start_string_size = fm.stringWidth("0");
        int total = start_string_size + 2 + 1 + 100 + 2 + distance_string_size;
        return new Dimension(total, fm.getAscent());
    }

    /**
     *  Method to return the painted image of a distance ruler.
     *  Currently being used from the DistanceRulerServlet.
     *
     *  @param  ImageCreator  The Component onto which the ruler will be painted.
     *  @return Image The painted image of the distance ruler.
     */
    public Image getDistanceRuler(Component ImageCreator) {
        Font imagefont = this.configuration.getMapLegendFont();
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(imagefont);
        Dimension ruler_size = getPreferredRulerSize(fm);
        BufferedImage OffScreenImage = new BufferedImage(ruler_size.width + 2, ruler_size.height + 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) OffScreenImage.getGraphics();
        g2d.setFont(imagefont);
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, ruler_size.width + 2, ruler_size.height + 2);
        g2d.translate(1, 1);
        g2d.setColor(Color.black);
        paintDistanceRuler(g2d, fm, imagefont);
        return OffScreenImage;
    }

    /**
     *  Paint method to draw the distance ruler at a translated point
     *  in the drawing frame of the graphics object.
     *
     *  @param g The Graphics object onto which the distance ruler will be painted.
     *  @param pnt The upper-left corner of the box where the ruler will be drawn.
     */
    public void paintDistanceRuler(Graphics g, Point pnt) {
        Font imagefont = this.configuration.getMapLegendFont();
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(imagefont);
        g.translate(pnt.x, pnt.y);
        paintDistanceRuler((Graphics2D) g, fm, imagefont);
    }

    private void paintDistanceRuler(Graphics2D g, FontMetrics fm, Font font) {
        Point2D centro = getMap().getMapCenter();
        Point left = new Point((int) centro.getX() - 50, (int) centro.getY());
        Point right = new Point((int) centro.getX() + 50, (int) centro.getY());
        double distance = getMap().getSphericalDistance(left, right, getMap().getDistanceUnit());
        long distance_l = Math.round(distance);
        String distance_string = new Long(distance_l).toString() + " " + getMap().getDistanceUnit().getAbbreviation();
        g.setFont(font);
        int distance_string_size = fm.stringWidth(distance_string);
        int start_string_size = fm.stringWidth("0");
        int total = start_string_size + 2 + 1 + 100 + 2 + distance_string_size;
        int altezza = fm.getAscent();
        int half_altezza = altezza / 2;
        int x = start_string_size + 2 + 1;
        int width = 100;
        g.setColor(Color.black);
        g.setStroke(new BasicStroke(1));
        g.drawString("0", 0, (fm.getAscent() / 2) + space_between_legend_ruler);
        g.drawRect(x, -half_altezza + space_between_legend_ruler, 100 - 1, altezza - 1);
        g.fillRect(x + width / 2, -half_altezza + space_between_legend_ruler, width / 2, half_altezza);
        g.fillRect(x + width / 4, 0 + space_between_legend_ruler, width / 4, half_altezza);
        g.fillRect(x, -half_altezza + space_between_legend_ruler, 5, half_altezza);
        g.fillRect(x + 5, 0 + space_between_legend_ruler, 5, half_altezza);
        g.fillRect(x + 10, -half_altezza + space_between_legend_ruler, 5, half_altezza);
        g.fillRect(x + 15, 0 + space_between_legend_ruler, 5, half_altezza);
        g.fillRect(x + 20, -half_altezza + space_between_legend_ruler, 5, half_altezza);
        g.drawString(distance_string, x + width + 2, (fm.getAscent() / 2) + space_between_legend_ruler);
    }

    public String getKeyTitle(Key key) {
        String title = "";
        for (int k = 0; k < key.size(); k++) {
            if (key.at(k) != Key.WILD && key.at(k) != Key.NONE) {
                if (title.length() > 0) {
                    title += ", ";
                }
                int[] path = { k, key.at(k) };
                title += getAttributes().getExtents().getAttribute(path, "name");
            }
        }
        return title;
    }

    /**
     *  Paint the map legend onto the graphic component.
     */
    public Image getMapLegend(Component ImageCreator) {
        return getMapLegend(configuration.getMapLegendFont(), ImageCreator);
    }

    public Image getLayerLegend(Component ImageCreator, String layerCompositeName) {
        FontMetrics font_metrics = Toolkit.getDefaultToolkit().getFontMetrics(configuration.getMapLegendFont());
        l_height = 0;
        l_max_w = 0;
        Layer slayer = map.getLayerNode(layerCompositeName);
        calculateLegendHeight(map, slayer, font_metrics, 0, 0);
        Dimension layers_legend_size = new Dimension(l_max_w + 10, l_height);
        Dimension ruler_size = getPreferredRulerSize(font_metrics);
        whole_map_legend_size = new Dimension(Math.max(layers_legend_size.width, ruler_size.width), layers_legend_size.height + space_between_legend_ruler + ruler_size.height + 2);
        BufferedImage OffScreenImage = new BufferedImage(whole_map_legend_size.width, whole_map_legend_size.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) OffScreenImage.getGraphics();
        g2d.setFont(configuration.getMapLegendFont());
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, whole_map_legend_size.width, whole_map_legend_size.height);
        g2d.setColor(Color.black);
        l_height = 0;
        paintLayerLegend(map, slayer, 0, g2d, 0);
        return OffScreenImage;
    }

    public void paintMapTitle(Graphics g, int img_width) {
        if (getMap() == null) {
            return;
        }
        String title = null;
        Key key = new Key(getContext().getKey());
        key.set(Key.WILD, 0);
        title = getKeyTitle(key);
        FontMetrics font_metrics = Toolkit.getDefaultToolkit().getFontMetrics(configuration.getMapLegendFont());
        Dimension title_size = getTitleDimension(title);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(configuration.getMapLegendFont());
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, img_width, title_size.height);
        int op = (img_width - title_size.width) / 2;
        g2d.translate(op, 0);
        g2d.setColor(Color.BLACK);
        g2d.drawString(title, 0, font_metrics.getAscent());
        g2d.translate(0 - op, 0);
    }

    /**
     *  alisaf: special map legend painter, for exporting the map and legend as one
     *  single image.
     */
    public void paintMapLegend(Graphics g) {
        if (getMap() == null) {
            return;
        }
        FontMetrics font_metrics = Toolkit.getDefaultToolkit().getFontMetrics(configuration.getMapLegendFont());
        l_height = 0;
        l_max_w = 0;
        for (int j = 0; j < map.size(); j++) {
            Layer slayer = map.getLayer(map.getLayerOrder().at(j));
            calculateLegendHeight(map, slayer, font_metrics, j, 0);
        }
        Dimension layers_legend_size = new Dimension(l_max_w + 10, l_height);
        Dimension ruler_size = getPreferredRulerSize(font_metrics);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(configuration.getMapLegendFont());
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, whole_map_legend_size.width, whole_map_legend_size.height);
        g2d.setColor(Color.black);
        l_height = 0;
        if (configuration.getLayerOrder() == 0) {
            for (int j = 0; j < map.size(); j++) {
                Layer layer = map.getLayer(map.getLayerOrder().at(j));
                paintLayerLegend(map, layer, j, g2d, 0);
            }
        } else if (configuration.getLayerOrder() == 1) {
            for (int j = map.size() - 1; j >= 0; j--) {
                Layer layer = map.getLayer(map.getLayerOrder().at(j));
                paintLayerLegend(map, layer, j, g2d, 0);
            }
        }
        if (isLegendDisplayDistanceRuler()) {
            java.awt.geom.AffineTransform at = g2d.getTransform();
            at.translate(0, layers_legend_size.height + 6);
            g2d.setTransform(at);
            paintDistanceRuler(g2d, font_metrics, configuration.getMapLegendFont());
            at.translate(0, 0 - (layers_legend_size.height + 6));
            g2d.setTransform(at);
        }
    }

    int l_max_w = 0;

    int l_height = 0;

    Dimension whole_map_legend_size = null;

    public Dimension getDimensionLegend() {
        return (whole_map_legend_size);
    }

    public Image getMapLegend(Font imagefont, Component ImageCreator) {
        if (getMap() == null) {
            return null;
        }
        FontMetrics font_metrics = Toolkit.getDefaultToolkit().getFontMetrics(imagefont);
        l_height = 0;
        l_max_w = 0;
        for (int j = 0; j < map.size(); j++) {
            Layer slayer = map.getLayer(map.getLayerOrder().at(j));
            calculateLegendHeight(map, slayer, font_metrics, j, 0);
        }
        Dimension layers_legend_size = new Dimension(l_max_w + 10, l_height);
        Dimension ruler_size = getPreferredRulerSize(font_metrics);
        whole_map_legend_size = new Dimension(Math.max(layers_legend_size.width, ruler_size.width), layers_legend_size.height + space_between_legend_ruler + ruler_size.height + 2);
        BufferedImage OffScreenImage = new BufferedImage(whole_map_legend_size.width, whole_map_legend_size.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) OffScreenImage.getGraphics();
        g2d.setFont(imagefont);
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, whole_map_legend_size.width, whole_map_legend_size.height);
        g2d.setColor(Color.black);
        l_height = 0;
        if (configuration.getLayerOrder() == 0) {
            for (int j = 0; j < map.size(); j++) {
                Layer layer = map.getLayer(map.getLayerOrder().at(j));
                paintLayerLegend(map, layer, j, g2d, 0);
            }
        } else if (configuration.getLayerOrder() == 1) {
            for (int j = map.size() - 1; j >= 0; j--) {
                Layer layer = map.getLayer(map.getLayerOrder().at(j));
                paintLayerLegend(map, layer, j, g2d, 0);
            }
        }
        if (isLegendDisplayDistanceRuler()) {
            java.awt.geom.AffineTransform at = g2d.getTransform();
            at.translate(0, layers_legend_size.height + 6);
            g2d.setTransform(at);
            paintDistanceRuler(g2d, font_metrics, imagefont);
        }
        return OffScreenImage;
    }

    public Dimension getTitleDimension(String title) {
        FontMetrics font_metrics = Toolkit.getDefaultToolkit().getFontMetrics(configuration.getMapLegendFont());
        int tw = font_metrics.stringWidth(title);
        int th = font_metrics.getHeight();
        return new Dimension(tw, th);
    }

    public void calculateLegendHeight(GroupLayer glayer, Layer layer, FontMetrics font_metrics, int j, int depth) {
        if (layer instanceof BaseGroupLayer || layer instanceof GroupLayer) {
            BaseGroupLayer glay = (BaseGroupLayer) layer;
            Dimension laysize = glay.getPreferredLegendSize(font_metrics, depth);
            if (glay.hasVisibleChild()) {
                l_max_w = Math.max(l_max_w, laysize.width);
                l_height += laysize.height;
                depth++;
                for (int i = 0; i < glay.getSize(); i++) {
                    Layer slayer = glay.getLayer(i);
                    calculateLegendHeight(glay, slayer, font_metrics, i, depth);
                }
            }
        } else if (layer.isVisible()) {
            if (layer instanceof FeatureLayer) {
                FeatureLayer flay = (FeatureLayer) layer;
                if (getContext() != null && !getContext().getPreloadAxes() || (getContext().getLegend() != null && getContext().getLegend().isDisplay()) && (!((flay.isSelected()) && (flay.getContext() != null && flay.getContext().getLegend() != null && flay.getContext().getLegend().isDisplay() == false)))) {
                    Dimension laysize = flay.getPreferredLegendSize(font_metrics, depth);
                    l_max_w = Math.max(l_max_w, laysize.width);
                    l_height += laysize.height;
                    if (getContext().isOverlayingLayers()) {
                        try {
                            Key bkp_key = (Key) getContext().getKey().clone();
                            getContext().updateAxisForOverlayingKey();
                            laysize = flay.getPreferredLegendSize(font_metrics, depth);
                            l_max_w = Math.max(l_max_w, laysize.width);
                            l_height += laysize.height;
                            getContext().updateAxis(bkp_key);
                        } catch (CloneNotSupportedException e) {
                        }
                    }
                } else {
                    Dimension laysize = flay.getPreferredLegendSize(font_metrics, depth);
                    l_max_w = Math.max(l_max_w, laysize.width);
                    l_height += laysize.height;
                }
            } else if (layer instanceof RasterLayer) {
                RasterLayer rlay = (RasterLayer) layer;
                Dimension laysize = rlay.getPreferredLegendSize(font_metrics, depth);
                l_max_w = Math.max(l_max_w, laysize.width);
                l_height += laysize.height;
            } else if (layer instanceof LLGridLayer) {
                LLGridLayer lllay = (LLGridLayer) layer;
                Dimension laysize = lllay.getPreferredLegendSize(font_metrics, depth);
                l_max_w = Math.max(l_max_w, laysize.width);
                l_height += laysize.height;
            }
        }
    }

    public void paintLayerLegend(GroupLayer glayer, Layer layer, int j, Graphics2D g2d, int depth) {
        if (layer instanceof BaseGroupLayer || layer instanceof GroupLayer) {
            BaseGroupLayer glay = (BaseGroupLayer) layer;
            if (glay.hasVisibleChild()) {
                g2d.translate(0, l_height);
                Dimension laysize = glay.paintLegend(g2d, null, depth);
                g2d.translate(0, -l_height);
                l_height += laysize.height;
                depth++;
                if (configuration.getLayerOrder() == 0) {
                    for (int i = 0; i < glay.getSize(); i++) {
                        Layer slayer = glay.getLayer(i);
                        paintLayerLegend(glay, slayer, i, g2d, depth);
                    }
                } else if (configuration.getLayerOrder() == 1) {
                    for (int i = glay.getSize() - 1; i >= 0; i--) {
                        Layer slayer = glay.getLayer(i);
                        paintLayerLegend(glay, slayer, i, g2d, depth);
                    }
                }
            }
        } else if (layer.isVisible()) {
            if (layer instanceof FeatureLayer) {
                FeatureLayer flay = (FeatureLayer) layer;
                if (getContext() != null && !getContext().getPreloadAxes() || (getContext().getLegend() != null && getContext().getLegend().isDisplay()) && (!((flay.isSelected()) && (flay.getContext() != null && flay.getContext().getLegend() != null && flay.getContext().getLegend().isDisplay() == false)))) {
                    g2d.translate(0, l_height);
                    Dimension laysize = flay.paintLegend(g2d, null, legend_use_less_more_simbol, legend_use_dash, depth);
                    g2d.translate(0, -l_height);
                    if (flay.isVisible()) {
                        l_height += laysize.height;
                    }
                    if (getContext().isOverlayingLayers()) {
                        g2d.translate(0, l_height);
                        flay.paintLegend(g2d, null, legend_use_less_more_simbol, legend_use_dash, true, depth);
                        g2d.translate(0, -l_height);
                        if (flay.isVisible()) {
                            l_height += laysize.height;
                        }
                    }
                } else {
                    g2d.translate(0, l_height);
                    Dimension laysize = flay.paintLegend(g2d, null, legend_use_less_more_simbol, legend_use_dash, depth);
                    g2d.translate(0, -l_height);
                    if (flay.isVisible()) {
                        l_height += laysize.height;
                    }
                }
            } else if (layer instanceof RasterLayer) {
                RasterLayer rlay = (RasterLayer) layer;
                g2d.translate(0, l_height);
                Dimension laysize = rlay.paintLegend(g2d, null, legend_use_less_more_simbol, legend_use_dash, depth);
                g2d.translate(0, -l_height);
                if (rlay.isVisible()) {
                    l_height += laysize.height;
                }
            } else if (layer instanceof LLGridLayer) {
                LLGridLayer lllay = (LLGridLayer) layer;
                g2d.translate(0, l_height);
                Dimension laysize = lllay.paintLegend(g2d, null, depth);
                g2d.translate(0, -l_height);
                if (lllay.isVisible()) {
                    l_height += laysize.height;
                }
            }
        }
    }

    public Image getLayerImage(String layer_name, Component ImageCreator) {
        return getLayerImage(layer_name, ImageCreator, Color.white, Color.white);
    }

    public Image getLayerImage(String layer_name, Component ImageCreator, Color fill, Color border) {
        if (getMap() == null) {
            return null;
        }
        Layer layer = map.getLayerNode(layer_name);
        return getLayerImage(layer, ImageCreator, fill, border);
    }

    public Image getLayerImage(int layer_index, Component ImageCreator) {
        return getLayerImage(layer_index, ImageCreator, Color.white, Color.white);
    }

    public Image getLayerImage(int layer_index, Component ImageCreator, Color fill, Color border) {
        if (getMap() == null) {
            return null;
        }
        Layer layer = map.getLayer(layer_index);
        return getLayerImage(layer, ImageCreator, fill, border);
    }

    /**
   *  Create an icon image from a layer.  Used in legends and editor buttons.
   */
    public Image getLayerImage(Layer layer, Component ImageCreator, Color fill, Color border) {
        if (layer.getIconImage() != null) {
            return layer.getIconImage();
        }
        int l_height = 18;
        int l_width = 18;
        Dimension layer_image_size = new Dimension(l_width, l_height);
        BufferedImage imageLayer = new BufferedImage(layer_image_size.width, layer_image_size.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) imageLayer.getGraphics();
        if (layer instanceof FeatureLayer) {
            FeatureLayer flay = (FeatureLayer) layer;
            if (flay.SymbolMode == flay.OneSymbol) {
                flay.paintIcon(g2d, flay.getPatternOutline(), layer_image_size, fill, border);
            } else {
                if (layer.getProperties() == null) {
                    flay.paintIcon(g2d, layer_image_size, fill, border);
                    return imageLayer;
                }
                int non_null_prop_size = 0;
                for (int i = 1; i < layer.getProperties().size(); i++) {
                    if (layer.getFeatureProperties(i).label.equals("")) {
                        continue;
                    } else {
                        non_null_prop_size++;
                    }
                }
                if (non_null_prop_size <= 1) {
                    flay.paintIcon(g2d, flay.getFeatureProperties(1).pattern_outline, layer_image_size, fill, border);
                } else {
                    flay.paintIcon(g2d, flay.getFeatureProperties(0).pattern_outline, layer_image_size, fill, border);
                }
            }
        } else if (layer instanceof RasterLayer) {
            RasterLayer rlay = (RasterLayer) layer;
            rlay.paintIcon(g2d, new Dimension(l_height, l_width));
        } else if (layer instanceof LLGridLayer) {
            LLGridLayer lllay = (LLGridLayer) layer;
            lllay.paintIcon(g2d, new Dimension(l_height, l_width));
        } else if (layer instanceof BaseGroupLayer) {
            BaseGroupLayer glay = (BaseGroupLayer) layer;
            glay.paintIcon(g2d, new Dimension(l_height, l_width));
        }
        layer.setIconImage(imageLayer);
        return imageLayer;
    }

    public FileResourceVector getAttributesFiles() {
        if (getMap() == null) {
            return null;
        }
        return getMap().getAttributesFiles();
    }

    public int getSelectedAttributesIndex() {
        if (getMap() == null) {
            return -1;
        }
        return getMap().getSelectedAttributesIndex();
    }

    public int setSelectedAttributesIndex(int i) throws IOException {
        if (getMap() == null) {
            return -1;
        }
        return getMap().setSelectedAttributesIndex(i);
    }

    public int setDataLayer(int layer_index) throws IOException {
        if (getMap() == null) {
            return -1;
        }
        return getMap().setSelectedLayerIndex(layer_index);
    }

    public Context getContext() {
        if (map == null) {
            return null;
        }
        return map.getContext();
    }

    public Attributes getAttributes() {
        if (getContext() == null) {
            return null;
        }
        return getContext().getAttributes();
    }

    public MatrixInterface getMatrix() {
        if (getAttributes() == null) {
            return null;
        }
        return getAttributes().getMatrix();
    }

    public Extent getExtent(int key_index) {
        if (getAttributes() == null) {
            return null;
        }
        System.out.println("       MAPCONTEXT.getExtent: calling Attributes.getExtent(int)");
        return getAttributes().getExtent(key_index);
    }

    public Axis getAxis(int key_index) {
        if (getContext() == null) {
            return null;
        }
        return getContext().getAxis(key_index);
    }

    public int getAxisIndex(int key_index) {
        if (getContext() == null) {
            return -1;
        }
        return getContext().getAxisIndex(key_index);
    }

    public int getLayerCount() {
        if (getMap() == null) {
            return 0;
        }
        return map.size();
    }

    public FeatureLayer getDataLayer() {
        if (getMap() == null) {
            return null;
        }
        return map.getSelectedFeatureLayer();
    }

    public int getDataLayerIndex() {
        if (getMap() == null) {
            return -1;
        }
        return map.getSelectedLayerIndex();
    }

    public FeatureLayer getLayer(int layer_index) {
        if (getMap() == null) {
            return null;
        }
        return map.getFeatureLayer(layer_index);
    }

    public FeatureLayer getDataLayer(int layer_index) {
        return getLayer(layer_index);
    }

    public void removeLayer(int layer_index) throws IOException {
        map.removeLayer(layer_index);
    }

    public void removeAllMaps() throws IOException {
        for (int i = 0; i < map_files.size(); i++) {
            deleteMap(i);
        }
        map_files.removeAllElements();
        selected_map_index = -1;
        modified = true;
    }

    public int removeMap(int i) throws IOException {
        int next_map_to_show = 0;
        map_files.removeElementAt(i);
        translate_name_vec.removeElementAt(i);
        if (getSelectedMapIndex() > i) {
            selected_map_index--;
        } else if (getSelectedMapIndex() == i) {
            selected_map_index = -1;
        }
        modified = true;
        return next_map_to_show = 0;
    }

    public void addMapAt(Map map, int i) throws IOException {
        FileResource resource = new FileResource(map.getName(), map.getName(), map.getMapListHome());
        Translate translate_name = new Translate(map.getName());
        if (i >= map_files.size()) {
            map_files.addElement(resource);
            translate_name_vec.addElement(translate_name);
        } else {
            map_files.insertElementAt(resource, i);
            translate_name_vec.insertElementAt(translate_name, i);
        }
        if (map_cache != null) {
            map.setStatus(Map.NEW);
            map_cache.put(map.getName(), map);
        }
        if (getSelectedMapIndex() >= i) {
            selected_map_index++;
        }
        modified = true;
    }

    public int addMap(Map map) throws IOException {
        int index = map_files.size();
        addMapAt(map, index);
        return index;
    }

    public Map addNewMap(String name) throws IOException {
        int index = map_files.size();
        Map aMap = new Map(this, name, configuration);
        addMapAt(aMap, index);
        return aMap;
    }

    public Map addNewMap(String name, String home) throws IOException {
        int index = map_files.size();
        Map aMap = new Map(this, name, home, configuration);
        addMapAt(aMap, index);
        return aMap;
    }

    public void moveMap(int markmap, int targetmap) {
        FileResourceVector frv = getMapFiles();
        String current_map_name = null;
        if (getMap() != null) {
            current_map_name = getMap().getName();
        }
        if (markmap < targetmap && !(markmap == targetmap + 1)) {
            FileResource markfr = frv.at(markmap);
            frv.remove(markmap);
            frv.insertElementAt(markfr, targetmap - 1);
        }
        if (targetmap < markmap) {
            FileResource markfr = frv.at(markmap);
            frv.remove(markmap);
            frv.insertElementAt(markfr, targetmap);
        }
        if (current_map_name != null) {
            selected_map_index = findMapIndex(current_map_name);
        }
    }

    /**
     *  alisaf: given a country code, return that country's
     *  index in the project file. This in effect, is a translation
     *  of the regions listed in projects.xml to those in the
     *  extent.
     *  This method is used in the kidsSession to allow the
     *  user to click on a region and switch to that region.
     */
    public int translateExtentToMap(String country_code) {
        for (int i = 0; i < map_files.size(); i++) {
            if (map_files.at(i).getExtentCode().equals(country_code)) {
                return i;
            }
        }
        return -1;
    }

    public int translateNameToMap(String country_code) {
        for (int i = 0; i < map_files.size(); i++) {
            if (map_files.at(i).getResource().equals(country_code)) {
                return i;
            }
        }
        return -1;
    }

    public kidsError error = new kidsError();

    public kidsError getError() {
        return this.error;
    }

    public String errorString = null;

    public boolean CreateNewProject(String name) {
        String prj_name = name.trim();
        if (findMapIndex(prj_name) != -1) {
            errorString = "Error creating Project : Project already exists";
            return false;
        }
        try {
            Map new_map = this.addNewMap(prj_name, this.getHomeFolder());
            new_map.setPatternOutline(new XPatternOutline(new XPatternPaint(new Color(126, 192, 255), Color.blue, XPatternPaint.FILL_NONE)));
            new_map.setDistanceUnit(new LinearUnit(LinearUnit.meter.getName()));
            new_map.setCoordSys(ProjectionCategories.default_coordinate_system);
            new_map.saveNew();
        } catch (Exception e) {
            errorString = "Error creating Project : ";
            errorString += "" + e.getMessage();
            return false;
        }
        return true;
    }

    public String getHomeFolder() {
        String ret = this.getHome();
        if (ret != null) {
            return ret.endsWith(System.getProperty("file.separator")) ? ret : ret + System.getProperty("file.separator");
        } else {
            return null;
        }
    }

    /**
     * ali.safarnejad: 20060716
     * resets the label of the layer.
     */
    public boolean changeLabel(int index, String newname) {
        if (newname.trim().length() == 0) {
            System.out.println("Error renaming Project : Invalid Project name");
            return false;
        }
        String lang = configuration.getLanguageCode();
        ((Translate) (translate_name_vec.elementAt(index))).setLabel(lang, newname);
        String oldName = getMapFiles().at(index).getName();
        map.setOldName(oldName);
        map.setStatus(Map.UPDATED);
        modified = true;
        return true;
    }

    public boolean RenameProject(int index, String newname) {
        try {
            String pName = getMapFiles().at(index).getName();
            if (newname.trim().equals(pName.trim())) {
                return true;
            }
            if (newname.trim().length() == 0) {
                errorString = "Error renaming Project : Invalid Project name";
                System.out.println(errorString);
                return false;
            }
            int fIndex = findMapIndex(newname);
            if (fIndex >= 0 && fIndex != index) {
                errorString = "Error renaming Project : Project already exists";
                System.out.println(errorString);
                return false;
            }
            setMapName(index, newname);
            getMapFiles().at(index).setLabel(configuration.getLanguageCode(), newname);
        } catch (Exception e) {
            errorString = "Error renaming Project : ";
            errorString += "" + e.getMessage();
            return false;
        }
        return true;
    }

    public Progress getProgress() {
        return progress;
    }

    public void resetProgress() {
        this.progress = null;
    }

    public static final String exportFileName = "export.zip";

    public FileResource exportFileResource = null;

    public FileResource getExportFile() {
        return exportFileResource;
    }

    public void setExportFile(FileResource resource) {
        this.exportFileResource = resource;
    }

    public FileResource setExportFile(String filename) {
        if (filename == null) {
            exportFileResource = null;
        } else {
            exportFileResource = new FileResource("Export", filename, getHome());
        }
        return exportFileResource;
    }

    public Progress progress = null;

    public boolean ExportProjects(String filename, int[] list, boolean zip) {
        progress = new Progress("Exporting Projects", list.length);
        FileResource eFile = null;
        boolean result = true;
        try {
            progress.addMessageLine("validating target " + filename);
            String target_home = filename.substring(0, filename.lastIndexOf(File.separatorChar));
            if (filename == null || filename.trim().equals("")) {
                zip = true;
                progress.addMessageLine("creating export file " + filename);
                eFile = setExportFile(exportFileName);
                if (!eFile.create(true)) {
                    this.getError().setErrorString("Error exporting Projects : error creating file");
                    return false;
                }
            }
            if (zip) {
                if (!new File(filename).getParentFile().exists()) {
                    this.getError().setErrorString("Error exporting Projects : invalid location");
                    return false;
                }
            } else {
                if (!new File(filename).isDirectory()) {
                    this.getError().setErrorString("Error exporting Projects : invalid location");
                    return false;
                } else {
                    String can_map_context = new File(this.getHome()).getCanonicalPath();
                    String can_exp = new File(filename).getCanonicalPath();
                    if (can_exp.equals(can_map_context)) {
                        this.getError().setErrorString("Error exporting Projects : Cannot export to the current projects directory");
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            this.getError().setErrorString("Error exporting Projects : invalid location");
            System.out.println("Error exporting Projects : invalid location");
            e.printStackTrace();
            return false;
        }
        progress.addMessageLine("target valid");
        if (list.length <= 0) {
            this.getError().setErrorString("Error exporting Projects : no projects to export");
            System.out.println("Error exporting Projects : no projects to export");
            return false;
        }
        ZipOutputStream zos = null;
        try {
            progress.addMessageLine("preparing target");
            MapContext export_map_context = new MapContext(this.getHome(), false);
            String projects_zip = filename;
            if (zip) {
                if (!projects_zip.toLowerCase().endsWith(".zip")) {
                    projects_zip += ".zip";
                }
                try {
                    projects_zip = new File(projects_zip).getCanonicalPath().toString();
                } catch (Exception e) {
                }
                export_map_context = new MapContext(new File(projects_zip).getParent(), false);
                zos = new ZipOutputStream(new FileOutputStream(projects_zip));
            } else {
                export_map_context = new MapContext(filename, false);
            }
            boolean stopexport = false;
            String filef = new File(filename).getName();
            MapContext cloned_map_context = (MapContext) this.clone();
            for (int i = 0; i < list.length && !stopexport; i++) {
                String projectname = cloned_map_context.getMapFiles().at(list[i]).getResource();
                int project_index = list[i];
                progress.addProgressLine("exporting " + projectname);
                FileResource exported_file_resource = new FileResource(projectname, filef, new File(filename).getParent());
                String source_directory_map = cloned_map_context.getMapFiles().at(project_index).getHome() + File.separatorChar + FileResource.constructFilenameFromName(projectname) + File.separatorChar;
                if (zos != null) {
                    zos.putNextEntry(new ZipEntry(FileSystem.getRelativePathname(new File(cloned_map_context.getMapFiles().at(project_index).getHome()), new File(cloned_map_context.getMapAt(list[i]).getHome() + "map.xml"))));
                    XMLUtilOld.save(zos, (Map) cloned_map_context.getMapAt(list[i]).clone(), "Map");
                    int map_size = cloned_map_context.getMapAt(list[i]).getSize();
                    try {
                        String project_home = cloned_map_context.getMapFiles().at(project_index).getHome();
                        GroupLayer root_layer = (GroupLayer) cloned_map_context.getMapAt(list[i]);
                        result = rebuildProject(root_layer, project_home, zos);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return false;
                    }
                } else {
                    String target_directory_map = export_map_context.getHome() + File.separatorChar + FileResource.constructFilenameFromName(projectname) + File.separatorChar;
                    new File(target_directory_map).mkdirs();
                    FileSystem.copyDirectory(new File(source_directory_map), target_directory_map);
                }
                export_map_context.getMapFiles().add(exported_file_resource);
                export_map_context.translate_name_vec.addElement(cloned_map_context.translate_name_vec.elementAt(list[i]));
                export_map_context.map_id_vec.addElement(projectname);
                if (!progress.isRunning()) {
                    stopexport = true;
                }
            }
            if (true) {
                if (zos != null) {
                    zos.putNextEntry(new ZipEntry("projects.xml"));
                    XMLUtilOld.save(zos, export_map_context, "ExportedProjects");
                    File projects_zip_file = new File(projects_zip);
                    String projects_zip_filename = projects_zip_file.getName();
                    String projects_zip_parent = projects_zip_file.getParent();
                    FileResource fres = new FileResource(projects_zip_filename, projects_zip_filename, projects_zip_parent);
                    setExportFile(fres);
                    zos.closeEntry();
                    zos.close();
                } else {
                    export_map_context.save();
                }
            }
        } catch (Exception e) {
            this.getError().setErrorString("Error exporting Project");
            this.getError().addErrorString(" : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        progress.addMessageLine("export done");
        progress.stop();
        return result;
    }

    MapContext importmapcontext = null;

    public boolean isZip = false;

    public String importSource = null;

    public String[] targetprojectnames = null;

    public MapContext getImportMapContext() {
        return this.importmapcontext;
    }

    public Progress setProgress(String title, int counttop) {
        this.progress = new Progress(title, counttop);
        return progress;
    }

    public boolean setImportMapContext(String importsource) {
        progress = new Progress("Reading Projects", 100);
        progress.addProgressLine("reading source " + importsource);
        importmapcontext = null;
        if (importsource == null || importsource.trim().equals("")) {
            progress.addMessageLine("invalid source");
            this.getError().setErrorString("Error reading Projects : source is invalid");
            return false;
        }
        boolean import_from_zip;
        if (importsource.toLowerCase().endsWith("projects.xml")) {
            importsource = new File(importsource).getParent();
        }
        if (importsource.toLowerCase().endsWith(".jar") || importsource.toLowerCase().endsWith(".zip")) {
            import_from_zip = true;
            progress.addProgressLine("reading zip");
        } else {
            import_from_zip = false;
        }
        MapContext sourcemapcontext = null;
        ZipFile zip_file = null;
        try {
            progress.addProgressLine("validating source");
            if (import_from_zip) {
                String temp_map_context = System.getProperty("java.io.tmpdir") + File.separatorChar + "mc" + new Random(System.currentTimeMillis()).nextLong();
                zip_file = new ZipFile(importsource);
                ZipEntry prj_entry = zip_file.getEntry("projects.xml");
                if (prj_entry != null) {
                    new File(temp_map_context).mkdir();
                    FileSystem.copyStreamToFile(zip_file.getInputStream(prj_entry), new File(temp_map_context + File.separatorChar + "projects.xml"));
                } else {
                    this.getError().setErrorString("Error reading Projects : Invalid zip file");
                    System.out.println("Error reading Projects : Invalid zip file " + importsource);
                    return false;
                }
                progress.addProgressLine("valid source");
                progress.addProgressLine("reading source");
                sourcemapcontext = new MapContext(temp_map_context);
                new File(temp_map_context + File.separatorChar + "projects.xml").delete();
                new File(temp_map_context + File.separatorChar).delete();
            } else {
                if (new File(getHome()).getCanonicalFile().equals(new File(importsource).getCanonicalFile())) {
                    progress.addProgressLine("cannot import from current working directory");
                    this.getError().setErrorString("Error reading Projects : cannot import from current working directory");
                    System.out.println("Error reading Projects : cannot import from current working directory");
                    return false;
                }
                progress.addProgressLine("valid source");
                progress.addMessageLine("reading source");
                sourcemapcontext = new MapContext(importsource);
            }
            sourcemapcontext.isZip = import_from_zip;
            sourcemapcontext.importSource = importsource;
        } catch (Exception e) {
            this.getError().setErrorString("Error reading Projects");
            this.getError().addErrorString(" : " + e.getMessage());
            return false;
        }
        if (sourcemapcontext.getMapFiles().size() <= 0) {
            this.getError().setErrorString("Error reading Projects : no projects to import");
            return false;
        }
        progress.addProgressLine("done", 20);
        progress.stop();
        importmapcontext = sourcemapcontext;
        return true;
    }

    public boolean ImportProjects(int[] list, boolean link) {
        return ImportProjects(list, link, false);
    }

    public boolean ImportProjects(int[] list, boolean link, boolean overwrite_existing) {
        progress = new Progress("Importing Projects", list.length);
        String progetto_corrente = getSelectedMapIndex() != -1 ? getMapFiles().at(getSelectedMapIndex()).getName() : "";
        if (list.length <= 0) {
            return true;
        }
        progress.addMessageLine("validating source");
        if (importmapcontext == null) {
            this.getError().setErrorString("Error importing Projects : error reading source");
            return false;
        }
        if (importmapcontext.importSource.trim().equals("")) {
            this.getError().setErrorString("Error importing Projects : source is missing");
            return false;
        }
        if (importmapcontext.getMapFiles().size() <= 0) {
            this.getError().setErrorString("Error importing Projects : no projects to import");
            return false;
        }
        progress.addMessageLine("valid source " + importmapcontext.importSource);
        boolean stopimport = false;
        targetprojectnames = new String[list.length];
        for (int i = 0; i < list.length && !stopimport; i++) {
            String projectname = importmapcontext.getMapFiles().at(list[i]).getName();
            String targetprojectname = projectname;
            progress.addProgressLine("importing project " + projectname + (link ? " as link" : " as copy"));
            if (!overwrite_existing) {
                while (this.findMapIndex(targetprojectname) > -1) {
                    targetprojectname += "_";
                    progress.addMessageLine(" - " + projectname + " exists - set target as " + targetprojectname);
                }
            }
            targetprojectnames[i] = targetprojectname;
            if (link) {
                importmapcontext.getMapFiles().at(list[i]).setName(targetprojectname);
                this.getMapFiles().add(importmapcontext.getMapFiles().at(list[i]));
                this.translate_name_vec.addElement(importmapcontext.translate_name_vec.elementAt(list[i]));
            } else {
                String target_directory_map = this.getHome() + File.separatorChar + FileResource.constructFilenameFromName(targetprojectname) + File.separatorChar;
                progress.addMessageLine(" - target directory " + target_directory_map);
                try {
                    if (overwrite_existing) {
                        new File(target_directory_map).delete();
                    }
                    if (getConfiguration().usesGaulCode()) {
                        File new_dir = new File(target_directory_map);
                        if (new_dir.exists()) {
                            targetprojectname = targetprojectname + "_";
                            target_directory_map = this.getHome() + File.separatorChar + FileResource.constructFilenameFromName(targetprojectname) + File.separatorChar;
                            new File(target_directory_map).mkdirs();
                        } else {
                            new_dir.mkdirs();
                        }
                    } else {
                        new File(target_directory_map).mkdirs();
                    }
                    if (importmapcontext.isZip) {
                        String physname = FileResource.constructFilenameFromName(projectname);
                        String targetphysname = FileResource.constructFilenameFromName(targetprojectname) + File.separatorChar;
                        progress.addMessageLine("import from zip to " + targetphysname + " ");
                        ZipFile zip_file = new ZipFile(importmapcontext.importSource);
                        Enumeration zip_entries = zip_file.entries();
                        while (zip_entries.hasMoreElements()) {
                            ZipEntry zentry = (ZipEntry) zip_entries.nextElement();
                            String zentryName = zentry.getName();
                            if (zentryName.startsWith(physname)) {
                                String targetzentryName = zentryName;
                                if (!projectname.equals(targetprojectname)) {
                                    zentryName = zentryName.replace('\\', '/');
                                    String physname2 = physname + "/";
                                    targetphysname = targetphysname.replace('\\', '/');
                                    targetzentryName = zentryName.replaceAll(physname2, targetphysname);
                                    targetphysname = targetphysname.replace('/', File.separatorChar);
                                }
                                File file3 = new File(new File(this.getHome()), targetzentryName);
                                progress.addMessageLine("created file " + targetzentryName + " at " + getHome());
                                file3.getParentFile().mkdirs();
                                if (zentry.isDirectory()) {
                                    file3.mkdir();
                                } else {
                                    InputStream entry_stream = zip_file.getInputStream(zentry);
                                    FileSystem.copyStreamToFile(entry_stream, file3);
                                    if (!file3.exists()) {
                                        progress.addMessageLine("\tFILE NOT CREATED!!!");
                                    } else {
                                        progress.addMessageLine("\tFILE " + file3.getAbsolutePath() + " CREATED!!!");
                                    }
                                }
                            }
                        }
                    } else {
                        String source_directory_map = importmapcontext.getHome() + File.separatorChar + FileResource.constructFilenameFromName(projectname) + File.separatorChar;
                        FileSystem.copyDirectory(new File(source_directory_map), new File(target_directory_map));
                    }
                    if (getConfiguration().useProjectResource()) {
                        FileResource pr = new ProjectResource(targetprojectname, targetprojectname, this.getHome());
                        ((ProjectResource) pr).setImportedProject(true);
                        this.getMapFiles().add(pr);
                    } else {
                        this.getMapFiles().add(new FileResource(targetprojectname, targetprojectname, this.getHome()));
                    }
                    this.translate_name_vec.addElement(new Translate(targetprojectname));
                } catch (Exception exce) {
                    this.getError().setErrorString("Error importing Project (" + projectname + ") : " + exce.toString());
                    exce.printStackTrace();
                    return false;
                }
            }
            if (!progress.isRunning()) {
                stopimport = true;
            }
        }
        progress.addMessageLine("saving");
        progress.addMessageLine("import done");
        progress.stop();
        return true;
    }

    public boolean useLegendLessMoreSimbol() {
        return legend_use_less_more_simbol;
    }

    public void setLegendUseLessMoreSimbol(boolean value) {
        legend_use_less_more_simbol = value;
    }

    public boolean useLegendDash() {
        return legend_use_dash;
    }

    public void setLegendUseDash(boolean value) {
        legend_use_dash = value;
    }

    public boolean isLegendDisplayDistanceRuler() {
        return configuration.getLegendDisplayDistanceRuler();
    }

    public void setLegendDisplayDistanceRuler(boolean value) {
        configuration.setLegendDisplayDistanceRuler(value);
    }

    public Map getMapAt(int pos) {
        Map mapAt = null;
        if (map_files.at(pos) != null && map_cache != null && map_cache.containsKey(map_files.at(pos).getName())) {
            mapAt = (Map) map_cache.get(map_files.at(pos).getName());
        } else {
            try {
                mapAt = loadMap(pos);
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }
        return mapAt;
    }

    public boolean isLegendVisible() {
        return configuration.getLegendDisplay();
    }

    public void setLegendVisible(boolean value) {
        configuration.setLegendDisplay(value);
    }

    public Image getMatrixLegend(Component ImageCreator) {
        return getMatrixLegend(configuration.getMapLegendFont(), ImageCreator);
    }

    public Image getMatrixLegend(Font imagefont, Component ImageCreator) {
        if (getMap() == null || getContext() == null || getContext().getLegend() == null) {
            return null;
        }
        FontMetrics font_metrics = Toolkit.getDefaultToolkit().getFontMetrics(imagefont);
        int l_height = 0;
        int l_max_w = 0;
        FeatureLayer flay = getMap().getSelectedFeatureLayer();
        int depth = 0;
        Dimension laysize = flay.getPreferredLegendSize(font_metrics, depth);
        l_max_w = Math.max(l_max_w, laysize.width);
        l_height += laysize.height;
        Dimension whole_legend_size = new Dimension(l_max_w + 10, l_height + 2);
        BufferedImage OffScreenImage = new BufferedImage(whole_legend_size.width, whole_legend_size.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) OffScreenImage.getGraphics();
        g2d.setFont(imagefont);
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, whole_legend_size.width, whole_legend_size.height);
        g2d.setColor(Color.black);
        l_height = 0;
        g2d.translate(0, l_height);
        laysize = flay.paintLegend(g2d, null, legend_use_less_more_simbol, legend_use_dash, depth);
        g2d.translate(0, -l_height);
        if (flay.isVisible()) {
            l_height += laysize.height;
        }
        if (getContext().isOverlayingLayers()) {
            g2d.translate(0, l_height);
            flay.paintLegend(g2d, null, legend_use_less_more_simbol, legend_use_dash, true, depth);
            g2d.translate(0, -l_height);
            if (flay.isVisible()) {
                l_height += laysize.height;
            }
        }
        return OffScreenImage;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    private boolean ExportProjectsIncludeLayer(Layer layer, String project_home, ZipOutputStream zos) {
        try {
            LayerProviderHelper helper = layer.getLayerProviderHelper();
            if (helper != null) {
                FileResource feature_filename = ((FileResourceI) helper).getFileResource();
                if (feature_filename != null) {
                    if (feature_filename.getHome() == null) {
                        feature_filename.setHome(layer.getHome());
                    }
                    FileSystem.copyFileToZipStream(new File(feature_filename.getAbsoluteFilename()), FileSystem.getRelativePathname(new File(project_home), new File(feature_filename.getAbsoluteFilename())), new File(project_home), zos);
                    String extension = feature_filename.getExtension();
                    FileResource copy_feature_filename = (FileResource) feature_filename.clone();
                    if ("TXT".equalsIgnoreCase(extension) && layer instanceof FeatureLayer) {
                        FileResource feature_filename_support = (FileResource) copy_feature_filename.clone();
                        feature_filename_support.setExtension(".p2a");
                        FileSystem.copyFileToZipStream(new File(feature_filename_support.getAbsoluteFilename()), FileSystem.getRelativePathname(new File(project_home), new File(feature_filename_support.getAbsoluteFilename())), new File(project_home), zos);
                    } else if ("SHP".equalsIgnoreCase(extension)) {
                        FileResource feature_filename_support = (FileResource) copy_feature_filename.clone();
                        feature_filename_support.setExtension(".dbf");
                        FileSystem.copyFileToZipStream(new File(feature_filename_support.getAbsoluteFilename()), FileSystem.getRelativePathname(new File(project_home), new File(feature_filename_support.getAbsoluteFilename())), new File(project_home), zos);
                        feature_filename_support = (FileResource) copy_feature_filename.clone();
                        feature_filename_support.setExtension(".shx");
                        FileSystem.copyFileToZipStream(new File(feature_filename_support.getAbsoluteFilename()), FileSystem.getRelativePathname(new File(project_home), new File(feature_filename_support.getAbsoluteFilename())), new File(project_home), zos);
                    } else if ("MIF".equalsIgnoreCase(extension)) {
                        FileResource feature_filename_support = (FileResource) copy_feature_filename.clone();
                        feature_filename_support.setExtension(".mid");
                        FileSystem.copyFileToZipStream(new File(feature_filename_support.getAbsoluteFilename()), FileSystem.getRelativePathname(new File(project_home), new File(feature_filename_support.getAbsoluteFilename())), new File(project_home), zos);
                    } else if ("ADF".equalsIgnoreCase(extension)) {
                        FileResource raster_support_filename = new FileResource("dblbnd.adf", "dblbnd.adf");
                        raster_support_filename.setHome(layer.getHome());
                        if (raster_support_filename.exists()) {
                            FileSystem.copyFileToZipStream(new File(raster_support_filename.getAbsoluteFilename()), FileSystem.getRelativePathname(new File(project_home), new File(raster_support_filename.getAbsoluteFilename())), new File(project_home), zos);
                        }
                        raster_support_filename = new FileResource("sta.adf", "sta.adf");
                        raster_support_filename.setHome(layer.getHome());
                        if (raster_support_filename.exists()) {
                            FileSystem.copyFileToZipStream(new File(raster_support_filename.getAbsoluteFilename()), FileSystem.getRelativePathname(new File(project_home), new File(raster_support_filename.getAbsoluteFilename())), new File(project_home), zos);
                        }
                        raster_support_filename = new FileResource("w001001x.adf", "w001001x.adf");
                        raster_support_filename.setHome(layer.getHome());
                        if (raster_support_filename.exists()) {
                            FileSystem.copyFileToZipStream(new File(raster_support_filename.getAbsoluteFilename()), FileSystem.getRelativePathname(new File(project_home), new File(raster_support_filename.getAbsoluteFilename())), new File(project_home), zos);
                        }
                        raster_support_filename = new FileResource("w001001.adf", "w001001.adf");
                        raster_support_filename.setHome(layer.getHome());
                        if (raster_support_filename.exists()) {
                            FileSystem.copyFileToZipStream(new File(raster_support_filename.getAbsoluteFilename()), FileSystem.getRelativePathname(new File(project_home), new File(raster_support_filename.getAbsoluteFilename())), new File(project_home), zos);
                        }
                    } else if (layer instanceof RasterLayer) {
                        FileResource raster_support_filename = ((RasterLayer) layer).getLegendResource();
                        if (raster_support_filename != null && raster_support_filename.exists()) {
                            FileSystem.copyFileToZipStream(new File(raster_support_filename.getAbsoluteFilename()), FileSystem.getRelativePathname(new File(project_home), new File(raster_support_filename.getAbsoluteFilename())), new File(project_home), zos);
                        }
                    }
                    if (layer instanceof FeatureLayer) {
                        FileResource feature_properties_filename = new FileResource("properties.b", "properties.b");
                        feature_properties_filename.setHome(layer.getHome());
                        if (feature_properties_filename.exists()) {
                            FileSystem.copyFileToZipStream(new File(feature_properties_filename.getAbsoluteFilename()), FileSystem.getRelativePathname(new File(project_home), new File(feature_properties_filename.getAbsoluteFilename())), new File(project_home), zos);
                        }
                        feature_properties_filename = new FileResource("properties.csv", "properties.csv");
                        feature_properties_filename.setHome(layer.getHome());
                        if (feature_properties_filename.exists()) {
                            FileSystem.copyFileToZipStream(new File(feature_properties_filename.getAbsoluteFilename()), FileSystem.getRelativePathname(new File(project_home), new File(feature_properties_filename.getAbsoluteFilename())), new File(project_home), zos);
                        }
                        for (int k = 0; k < ((FeatureLayer) layer).getAttributesFiles().size(); k++) {
                            if (!((FeatureLayer) layer).getAttributesFiles().at(k).equals("none") && !((FeatureLayer) layer).getAttributesFiles().at(k).getAbsoluteFilename().equals("")) {
                                ((FeatureLayer) layer).getAttributesFiles().at(k).setHome(layer.getHome());
                                FileSystem.copyFileToZipStream(new File(((FeatureLayer) layer).getAttributesFiles().at(k).getAbsoluteFilename()), FileSystem.getRelativePathname(new File(project_home), new File(((FeatureLayer) layer).getAttributesFiles().at(k).getAbsoluteFilename())), new File(project_home), zos);
                            }
                        }
                    }
                } else {
                    System.out.println("No feature_filename for " + layer.getName());
                }
            } else {
                System.out.println("Provider helper not available for " + layer.getName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public void assignSelectedMapIndex(int i) {
        selected_map_index = i;
    }

    private boolean rebuildProject(GroupLayer root_layer, String project_home, ZipOutputStream zos) {
        boolean complete = false;
        for (int k = 0; k < root_layer.getSize(); k++) {
            Layer layer = root_layer.getLayer(k);
            if (layer instanceof GroupLayer) {
                GroupLayer g_layer = (BaseGroupLayer) layer;
                complete = ExportProjectsIncludeLayer(layer, project_home, zos);
                rebuildProject(g_layer, project_home, zos);
            } else {
                complete = ExportProjectsIncludeLayer(layer, project_home, zos);
            }
        }
        return complete;
    }

    public org.w3c.dom.Document getProjectsDocument() {
        return projects_document;
    }
}
