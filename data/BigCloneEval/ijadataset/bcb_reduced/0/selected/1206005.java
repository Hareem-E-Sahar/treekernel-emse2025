package edu.ucsd.ncmir.ontology;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * The <code>Ontology</code>.
 *
 * @author Steve Lamont
 * @version prototype
 */
public final class Ontology implements Serializable {

    private static final long serialVersionUID = 1L;

    private IDToNodeHash _id_to_node_hash;

    private NameToNodeHash _name_to_node_hash;

    private OntologyNode _root_node = new OntologyNode("--root--");

    Ontology(OntologyNode root_node, IDToNodeHash id_to_node_hash, NameToNodeHash name_to_node_hash) {
        this._root_node = root_node;
        this._id_to_node_hash = id_to_node_hash;
        this._name_to_node_hash = name_to_node_hash;
    }

    public enum OntologyAccessMode {

        SERIALIZED(SerializedAccessModeHandler.class), HTTP(HTTPAccessModeHandler.class), DATABASE(DatabaseAccessModeHandler.class);

        private Class<? extends AccessModeHandler> _mode_handler;

        OntologyAccessMode(Class<? extends AccessModeHandler> mode_handler) {
            this._mode_handler = mode_handler;
        }

        protected Ontology open(String identifier) {
            Ontology ontology = null;
            try {
                ontology = this._mode_handler.newInstance().open(identifier);
            } catch (InstantiationException ie) {
            } catch (IllegalAccessException iae) {
            }
            return ontology;
        }
    }

    public void serialize(String name) throws FileNotFoundException, IOException {
        String path = name + ".jar";
        FileOutputStream fos = new FileOutputStream(path);
        JarOutputStream jos = new JarOutputStream(fos);
        ZipEntry ze = new ZipEntry(name);
        jos.putNextEntry(ze);
        ObjectOutputStream oos = new ObjectOutputStream(jos);
        oos.writeObject(this);
        oos.close();
    }

    public OntologyNode getRootNode() {
        return this._root_node;
    }

    /**
     * Returns a node by name.
     * @param internal_name 
     * @return the node.
     */
    public OntologyNode getNodeByInternalName(String internal_name) {
        return this._id_to_node_hash.get(internal_name.toLowerCase());
    }

    /**
     * Returns a node by name.
     * @param public_name 
     * @return the node.
     */
    public ArrayList<OntologyNode> getNodesByPublicName(String public_name) {
        return this._name_to_node_hash.get(public_name.toLowerCase());
    }

    /**
     * Returns a list of all nodes.
     * @return the nodes.
     */
    public OntologyNode[] getAllNodes() {
        ArrayList<OntologyNode> list = new ArrayList<OntologyNode>();
        for (OntologyNode o : this._id_to_node_hash.values()) list.add(o);
        OntologyNode[] array = list.toArray(new OntologyNode[list.size()]);
        Arrays.sort(array);
        return array;
    }

    /**
     * Commits any changes to the ontology.
     * @return status, true if ok, false if error occurred.
     */
    public boolean commit() {
        return true;
    }
}
