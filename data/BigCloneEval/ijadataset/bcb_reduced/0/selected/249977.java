package bee.core;

import java.io.File;
import java.lang.reflect.Constructor;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

/**
 * Class for loading/saving and finding available entities.
 *
 * @author boto
 */
public class EntityBuilder {

    private List<String> packagePaths = new ArrayList<String>();

    /**
     * Create entity builder.
     */
    public EntityBuilder() {
        packagePaths.add("");
    }

    /**
     * Add a search path for finding available entities.
     */
    public void addSearchPackage(String packagepath) {
        packagePaths.add(packagepath);
    }

    /**
     * Get a list of available entities which reside in search paths.
     */
    public List<String> getAvailableEntities() {
        List<String> ae = new ArrayList<String>();
        for (int pkg = 0; pkg < packagePaths.size(); pkg++) {
            String p = packagePaths.get(pkg);
            try {
                ae.addAll(getClasses(p));
            } catch (Exception e) {
            }
        }
        Collections.sort(ae);
        return ae;
    }

    /**
     * Get all available classes in a package.
     * Thanks go to RKeene, this code is basing on his idea.
     */
    protected List<String> getClasses(String pkg) throws Exception {
        List<String> classes = new ArrayList<String>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL res = cl.getResource(pkg.replace('.', '/'));
        if (res == null) {
            return classes;
        }
        File dir = new File(res.getFile());
        if (dir.exists()) {
            String[] files = dir.list();
            for (int i = 0; i < files.length; i++) {
                if (files[i].endsWith(".class") && !files[i].contains("$")) {
                    try {
                        String classname = files[i].substring(0, files[i].length() - 6);
                        Class c = Class.forName(pkg + '.' + classname);
                        if (Entity.class.isAssignableFrom(c)) classes.add(c.getSimpleName());
                    } catch (Exception e) {
                        Log.warning(getClass().getSimpleName() + ": invalid java class implementation detected: " + pkg + "/" + files[i] + "\n reason: " + e);
                    }
                }
            }
        }
        return classes;
    }

    /**
     * Load entities from given resource and return the top group containing the entity hierarchy.
     */
    public EntityGroup loadEntities(String resname) throws Exception {
        Log.debug("EntityBuilder: loading entities from " + resname);
        InputStream in = Resource.get().getFileStream(resname);
        EntityGroup topgroup = new EntityGroup("$TopGroup");
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(in);
        doc.getDocumentElement().normalize();
        Node top = doc.getFirstChild();
        while (top != null && !top.getNodeName().equals("Entities")) top = top.getNextSibling();
        if (top == null) {
            Log.error(getClass().getSimpleName() + ": file " + resname + " does not seem to be a level file! expecting top element 'Entities'");
            return null;
        }
        NodeList nodes = top.getChildNodes();
        for (int cnt = 0; cnt < nodes.getLength(); cnt++) {
            Node node = nodes.item(cnt);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
            String elemname = node.getNodeName();
            if (!elemname.equalsIgnoreCase("entitygroup")) {
                Log.error(getClass().getSimpleName() + ": file " + resname + " does not seem to be a valid level file! expecting 'EntityGroup' elements");
                return null;
            }
            EntityGroup grp = new EntityGroup();
            String grpname = ((Element) node).getAttribute("name");
            if (grpname != null) grp.setName(grpname);
            topgroup.addGroup(grp);
            loadEntityGroup((Element) node, grp);
        }
        EntityGroup firstgroup = null;
        if (topgroup.getGroups().size() > 1) {
            firstgroup = topgroup.getGroups().get(0);
            Log.warning("EntityBuilder: more than one top entity group exists in file, taking the first one, ignoring the rest");
        } else if (topgroup.getGroups().size() == 1) {
            firstgroup = topgroup.getGroups().get(0);
        }
        return firstgroup;
    }

    /**
     * Method used for post-initializing entities.
     */
    public void postInitializeEntities(EntityGroup grp) {
        for (Entity entity : grp.getEntities()) {
            Log.verbose(getClass().getSimpleName() + ": post-initializing '" + entity.toString() + "'");
            try {
                entity.postInitialize();
            } catch (Exception e) {
                Log.error(getClass().getSimpleName() + ": exception on entity post-initialization '" + entity.toString() + ":" + entity.getInstanceName() + "'");
                Log.error(" reason: " + e);
                e.printStackTrace();
            }
        }
        for (EntityGroup group : grp.getGroups()) {
            postInitializeEntities(group);
        }
    }

    /**
     * Load an entity group from given element.
     */
    private void loadEntityGroup(Element elem, EntityGroup group) {
        String elemname = elem.getNodeName();
        if (!elemname.equalsIgnoreCase("entitygroup")) {
            Log.error(getClass().getSimpleName() + ": unexpected element type, EntityGroup expected");
            return;
        }
        String grpname = elem.getAttribute("name");
        if (grpname != null) {
            group.setName(grpname);
            Log.debug("  loading entity group " + grpname);
        }
        NodeList nodes = elem.getChildNodes();
        for (int cnt = 0; cnt < nodes.getLength(); cnt++) {
            Node node = nodes.item(cnt);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
            elemname = node.getNodeName();
            if (elemname.equalsIgnoreCase("entitygroup")) {
                grpname = ((Element) node).getAttribute("name");
                EntityGroup subgrp = new EntityGroup(grpname);
                loadEntityGroup((Element) node, subgrp);
                group.addGroup(subgrp);
            } else if (elemname.equalsIgnoreCase("entity")) {
                Entity ent = loadEntity((Element) node);
                if (ent != null) group.addEntity(ent);
            }
        }
    }

    /**
     * Load an entity from given element.
     */
    private Entity loadEntity(Element elem) {
        Entity ent = null;
        String typename = elem.getAttribute("type");
        String instname = elem.getAttribute("instance");
        if (typename.isEmpty()) {
            Log.error(getClass().getSimpleName() + ": entity has no type information, skipping");
            return null;
        }
        if (instname.isEmpty()) {
            instname = "$noname$";
        }
        try {
            ent = createEntity(typename, instname);
        } catch (Exception e) {
            Log.error(getClass().getSimpleName() + ": could not create entity " + typename + "\n reason: " + e);
            return null;
        }
        Node param = elem.getFirstChild();
        while (param != null) {
            if (param.getNodeType() != Node.ELEMENT_NODE) {
                param = param.getNextSibling();
                continue;
            }
            Element paramelem = (Element) param;
            String name = paramelem.getAttribute("name");
            String value = paramelem.getAttribute("value");
            try {
                ent.setParamValueAsString(name, value);
            } catch (Exception e) {
                Log.error(getClass().getSimpleName() + ": (" + typename + " " + instname + ") problem occured while setting entity parameter " + name + "\n reason: " + e);
            }
            param = param.getNextSibling();
        }
        Log.verbose(getClass().getSimpleName() + ": initializing '" + ent.toString() + "'");
        try {
            ent.initialize();
        } catch (Exception e) {
            Log.error(getClass().getSimpleName() + ": exception on entity initialization '" + typename + ":" + instname + "'");
            Log.error(" reason: " + e);
            e.printStackTrace();
        }
        return ent;
    }

    /**
     * Save given entities to file.
     */
    public void saveEntities(String filename, EntityGroup group) throws Exception {
        Log.debug("EntityBuilder: saving entities to " + filename);
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.newDocument();
        Node top = doc.createElement("Entities");
        doc.appendChild(top);
        doc.setXmlStandalone(true);
        Element grpelem = doc.createElement("EntityGroup");
        grpelem.setAttribute("name", group.getName());
        top.appendChild(grpelem);
        saveEntityGroup(doc, grpelem, group);
        doc.normalize();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        String xmlstring = result.getWriter().toString();
        FileWriter f = new FileWriter(filename);
        f.write(xmlstring);
        f.close();
    }

    /**
     * Save the given entity group to element.
     */
    private void saveEntityGroup(Document doc, Element elem, EntityGroup group) {
        List<Entity> entities = group.getEntities();
        for (int cnt = 0; cnt < entities.size(); cnt++) {
            Entity ent = entities.get(cnt);
            String typename = ent.getClass().getSimpleName();
            String instname = ent.getInstanceName();
            Element entelem = doc.createElement("Entity");
            elem.appendChild(entelem);
            entelem.setAttribute("instance", instname);
            entelem.setAttribute("type", typename);
            List<String> paramnames = ent.getParamNames();
            for (int p = 0; p < paramnames.size(); p++) {
                Element paramelem = doc.createElement("Parameter");
                entelem.appendChild(paramelem);
                try {
                    paramelem.setAttribute("name", paramnames.get(p));
                    paramelem.setAttribute("value", ent.getParamValue(paramnames.get(p)).toString());
                } catch (Exception e) {
                    Log.error(getClass().getSimpleName() + ": problem occured while setting entity parameter " + paramnames.get(p) + "\n reason: " + e);
                }
            }
        }
        for (EntityGroup grp : group.getGroups()) {
            Element grpelem = doc.createElement("EntityGroup");
            grpelem.setAttribute("name", grp.getName());
            elem.appendChild(grpelem);
            saveEntityGroup(doc, grpelem, grp);
        }
    }

    /**
     * Find an entity class given it's class name.
     */
    private Class findEntityClass(String classname) throws Exception {
        Class entclass = null;
        for (int p = 0; p < packagePaths.size(); p++) {
            try {
                entclass = Class.forName(packagePaths.get(p) + "." + classname);
                return entclass;
            } catch (Exception e) {
            }
        }
        throw new Exception("Entity class not found");
    }

    /**
     * Create entity with given type and instance name.
     */
    public Entity createEntity(String type, String instname) throws Exception {
        Entity ent = null;
        try {
            Class entclass = findEntityClass(type);
            Constructor<String> ctor = entclass.getConstructor(String.class);
            Object obj = ctor.newInstance(instname);
            if (obj instanceof Entity) {
                ((BaseEntity) obj).setInstanceName(instname);
                ent = (Entity) obj;
            } else {
                throw new Exception("requested entity " + type + " is not compatible to BaseEntity.");
            }
        } catch (ClassNotFoundException e) {
            throw new Exception("could not create entity " + type + ".\n  reason: " + e);
        } catch (Exception e) {
            throw new Exception("could not create entity " + type + ".\n  reason: " + e);
        }
        return ent;
    }
}
