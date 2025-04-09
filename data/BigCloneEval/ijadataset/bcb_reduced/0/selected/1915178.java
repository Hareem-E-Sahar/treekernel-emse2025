package org.nvframe.factory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.newdawn.slick.util.ResourceLoader;
import org.nvframe.component.AbstractComponent;
import org.nvframe.entity.EntityImpl;
import org.nvframe.entity.Entity;
import org.nvframe.exception.NVFrameException;
import org.nvframe.manager.EntityManager;
import org.nvframe.util.settings.SettingsObj;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * @author Nik Van Looy
 */
public class EntityFactory {

    private static final EntityFactory _instance = new EntityFactory();

    public static EntityFactory getInstance() {
        return _instance;
    }

    private Map<String, Class<? extends Entity>> prototypeEntityCls;

    private Map<String, LinkedHashMap<String, SettingsObj>> prototypeEntityComp;

    private Map<String, SettingsObj> prototypeEntitySettings;

    private EntityFactory() {
        prototypeEntityComp = new HashMap<String, LinkedHashMap<String, SettingsObj>>();
        prototypeEntitySettings = new HashMap<String, SettingsObj>();
        prototypeEntityCls = new HashMap<String, Class<? extends Entity>>();
    }

    /**
	 * 
	 * @param location
	 * @throws NVFrameException
	 */
    public void loadEntitiesFromXml(String location) throws NVFrameException {
        InputStream inRes = ResourceLoader.getResourceAsStream(location);
        try {
            loadEntitiesFromXml(inRes);
        } catch (Exception e) {
            throw new NVFrameException("could not load resource: " + location, e);
        }
    }

    /**
	 * Loads entity nodes from a xml file
	 * 
	 * @param is
	 * @throws NVFrameException
	 */
    @SuppressWarnings("unchecked")
    public void loadEntitiesFromXml(InputStream is) throws NVFrameException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new NVFrameException("Could not load entities xmlfile", e);
        }
        Document doc = null;
        try {
            doc = docBuilder.parse(is);
        } catch (SAXException e) {
            throw new NVFrameException("Could not load entities xmlfile", e);
        } catch (IOException e) {
            throw new NVFrameException("Could not load entities xmlfile", e);
        }
        doc.getDocumentElement().normalize();
        NodeList listEntities = doc.getElementsByTagName("entity");
        int totalEntities = listEntities.getLength();
        for (int entity_id = 0; entity_id < totalEntities; entity_id++) {
            Node entityNode = listEntities.item(entity_id);
            if (entityNode.getNodeType() == Node.ELEMENT_NODE) {
                Element entityEl = (Element) entityNode;
                String entityId = entityEl.getAttribute("id");
                Map<String, SettingsObj> components = buildComponentMap(entityEl);
                SettingsObj settings = buildSettingsMap(entityEl);
                if (settings.containsKey("maxInstances")) EntityManager.getInstance().addInstanceRestriction(entityId, settings.getInt("maxInstances"));
                if (entityEl.hasAttribute("type")) {
                    Class<? extends Entity> cls;
                    try {
                        cls = (Class<? extends Entity>) Class.forName(entityEl.getAttribute("type"));
                        prototypeEntityCls.put(entityId, cls);
                    } catch (ClassNotFoundException e) {
                        throw new NVFrameException("Could not load entities xmlfile: entityclass [" + entityEl.getAttribute("type") + "] not found", e);
                    }
                } else prototypeEntityCls.put(entityId, EntityImpl.class);
                prototypeEntityComp.put(entityId, (LinkedHashMap<String, SettingsObj>) components);
                prototypeEntitySettings.put(entityId, settings);
            }
        }
    }

    /**
	 * Create a map with prototype components
	 * 
	 * @param parentEl
	 * @return HashMap<String, String> with components (id and type)
	 */
    private Map<String, SettingsObj> buildComponentMap(Element parentEl) {
        Map<String, SettingsObj> map = new LinkedHashMap<String, SettingsObj>();
        if (parentEl.getElementsByTagName("components") == null) return map;
        NodeList entitySettings = (NodeList) parentEl.getElementsByTagName("components").item(0);
        if (entitySettings == null) return map;
        NodeList entitySetting = ((Element) entitySettings).getElementsByTagName("component");
        for (int component_id = 0; component_id < entitySetting.getLength(); component_id++) {
            Node componentNode = entitySetting.item(component_id);
            if (componentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element componentEl = (Element) componentNode;
                SettingsObj settings = buildSettingsMap(componentEl);
                String compId = componentEl.getAttribute("id");
                String compCls = componentEl.getAttribute("type");
                settings.setSetting("id", compId);
                settings.setSetting("cls", compCls);
                map.put(compId, settings);
            }
        }
        return map;
    }

    /**
	 * Creates a Settings map
	 * 
	 * @param parentEl
	 * @return HashMap<String, String> with entity settings
	 */
    private SettingsObj buildSettingsMap(Element parentEl) {
        SettingsObj settings = new SettingsObj();
        if (parentEl.getElementsByTagName("settings") == null) return settings;
        NodeList entitySettings = (NodeList) parentEl.getElementsByTagName("settings").item(0);
        if (entitySettings == null) return settings;
        NodeList entitySetting = ((Element) entitySettings).getElementsByTagName("setting");
        for (int settingId = 0; settingId < entitySetting.getLength(); settingId++) {
            Node settingNode = entitySetting.item(settingId);
            if (settingNode.getNodeType() == Node.ELEMENT_NODE) {
                Element settingEl = (Element) settingNode;
                NodeList listNodes = (NodeList) settingEl.getElementsByTagName("list");
                if (listNodes.getLength() == 1) settings.setSetting(settingEl.getAttribute("id"), buildList(listNodes.item(0))); else settings.setSetting(settingEl.getAttribute("id"), settingEl.getTextContent().trim());
            }
        }
        return settings;
    }

    /**
	 * 
	 * @param listNode
	 * @return
	 */
    private List<String> buildList(Node listNode) {
        NodeList valueNodes = ((Element) listNode).getElementsByTagName("value");
        List<String> list = new ArrayList<String>();
        for (int valueId = 0; valueId < valueNodes.getLength(); valueId++) {
            Node valueNode = valueNodes.item(valueId);
            list.add(valueNode.getTextContent().trim());
        }
        return list;
    }

    public Entity getEntityFromPrototype(String entityId) throws NVFrameException {
        return getEntityFromPrototype(entityId, null);
    }

    /**
	 * create an instance of a given pre-defined prototype entity
	 * 
	 * @param entityId
	 * @param entityName 
	 * @return The new Entity instance
	 * @throws NVFrameException
	 */
    @SuppressWarnings("rawtypes")
    public Entity getEntityFromPrototype(String entityId, String entityName) throws NVFrameException {
        if (!prototypeEntityComp.containsKey(entityId) || !prototypeEntitySettings.containsKey(entityId)) throw new NVFrameException("cannot find entity [entityId: " + entityId + "]");
        Map<String, SettingsObj> components = prototypeEntityComp.get(entityId);
        SettingsObj settings = prototypeEntitySettings.get(entityId);
        Class<? extends Entity> cls = prototypeEntityCls.get(entityId);
        Entity entity = null;
        Constructor constr;
        try {
            constr = cls.getConstructor(String.class, String.class, SettingsObj.class);
            entity = (Entity) constr.newInstance(entityId, entityName, settings);
        } catch (Exception e) {
            throw new NVFrameException("Could not instantiate entity object with id: " + entityId, e);
        }
        for (String component_id : components.keySet()) {
            SettingsObj componentSettings = components.get(component_id);
            AbstractComponent comp = ComponentFactory.getInstance().getComponent(component_id, componentSettings, entity);
            entity.addComponent(comp);
        }
        EntityManager.getInstance().addEntity(entity);
        entity.initialize();
        return entity;
    }
}
