package ingenias.editor.persistence;

import java.lang.reflect.*;
import javax.swing.tree.*;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.OutputStreamWriter;
import java.io.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;
import java.util.Map;
import java.util.Hashtable;
import java.util.ArrayList;
import javax.xml.parsers.*;
import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import ingenias.editor.entities.*;
import ingenias.exception.*;
import ingenias.editor.cell.*;
import ingenias.editor.*;

public class RelationshipLoadImp1 implements RelationshipLoad {

    public void restoreRelationships(ObjectManager om, GraphManager gm, Document doc) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        RelationshipManager rm = new RelationshipManager();
        NodeList nl = doc.getElementsByTagName("relationships");
        NodeList objects = nl.item(0).getChildNodes();
        for (int k = 0; k < objects.getLength(); k++) {
            Node n = objects.item(k);
            if (n.getNodeName().equalsIgnoreCase("relationship")) {
                String type = n.getAttributes().getNamedItem("type").getNodeValue();
                int index = type.lastIndexOf(".");
                Node onode = this.getObject(n);
                NAryEdgeEntity nedge = (NAryEdgeEntity) PersistenceManager.getOL().restoreObject(om, gm, onode);
                NodeList nroles = n.getChildNodes();
                boolean wrongRelationship = false;
                for (int j = 0; j < nroles.getLength(); j++) {
                    Node crole = nroles.item(j);
                    if (crole.getNodeName().equalsIgnoreCase("role")) {
                        String entID = crole.getAttributes().getNamedItem("idEntity").getNodeValue();
                        String classID = crole.getAttributes().getNamedItem("class").getNodeValue();
                        String role = crole.getAttributes().getNamedItem("roleName").getNodeValue();
                        ingenias.editor.entities.Entity ent = om.getEntity(entID, classID);
                        RoleEntity re = this.restoreRole(om, rm, gm, crole);
                        if (ent != null) {
                            nedge.addObject(ent, re, role, classID);
                        } else {
                            Log.getInstance().logERROR("Relationship of type " + type + " could not load role " + entID);
                            Log.getInstance().logERROR("Relationship " + nedge + " discarded");
                            wrongRelationship = true;
                        }
                    }
                }
                if (!wrongRelationship) {
                    rm.addRelationship(nedge);
                }
                wrongRelationship = false;
            }
        }
    }

    private Node getObject(Node propertyNode) {
        NodeList children = propertyNode.getChildNodes();
        for (int k = 0; k < children.getLength(); k++) {
            Node current = children.item(k);
            if (current.getNodeName().equalsIgnoreCase("object")) {
                return current;
            }
        }
        return null;
    }

    private ingenias.editor.entities.RoleEntity restoreRole(ObjectManager om, RelationshipManager rm, GraphManager gm, Node n) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        String type = n.getAttributes().getNamedItem("type").getNodeValue();
        Class encClass = Class.forName(type);
        int index = type.lastIndexOf(".");
        String className = type.substring(index + 1, type.length());
        Class objectManager = om.getClass();
        Object[] params = { "" };
        Class[] paramtype = { "".getClass() };
        Constructor c = encClass.getConstructor(paramtype);
        RoleEntity en = (RoleEntity) c.newInstance(params);
        NodeList children = n.getChildNodes();
        for (int k = 0; k < children.getLength(); k++) {
            Node current = children.item(k);
            PersistenceManager.getPL().restoreProperty(om, gm, current, en);
        }
        return en;
    }

    public static void main(String[] args) {
    }
}
