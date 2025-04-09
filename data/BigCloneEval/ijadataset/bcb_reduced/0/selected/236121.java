package au.vermilion.fileio;

import au.vermilion.utils.ExposedArrayList;
import static au.vermilion.Vermilion.logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Implements a document class using a simple tree/node structure for the
 * searching and using Java serialisation to do the storage. Paths to nodes
 * are separated with "/". Storage is handled by codec classes that know how
 * to write certain objects.
 */
public final class Document {

    /**
     * This stores the list of types for translating between real objects and
     * SerialisedNode (the private class this implementation uses to store data).
     */
    private static final TreeMap<Short, IDocumentCodec> docObjTypes = new TreeMap<Short, IDocumentCodec>();

    /**
     * Registers a type for storage. The type supplied is an implementation of
     * the IDocumentCodec interface, the class actually stored is dependent on
     * this implentation. System plugins generally call this during initialise
     * to register the types they use, so we ignore reregistering of the same
     * codecs.
     */
    public static void registerType(IDocumentCodec type) {
        docObjTypes.put(type.getType(), type);
    }

    /**
     * The head of the document, an empty node where we can attach paths. There
     * is no need to directly reference the head node, paths start below this
     * node.
     */
    private final DocumentNode head = new DocumentNode("", null);

    /**
     * Indicates that the file should be saved every time an update is made.
     * This is useful for things like preferences where we don't want to have
     * to remember to write them directly all the time or coordinate the saving
     * via some other mechanism.
     */
    private IFileHandle writeBackLocation;

    /**
     * The constructor provides an empty file. A head node is already created
     * but it is only used as a place to start looking for nodes when decoding
     * a path.
     */
    public Document() {
        writeBackLocation = null;
    }

    /**
     * This sets up a write-thru file which is flushed to the specified location
     * on any update.
     */
    public void setWriteBackLocation(IFileHandle prefsFile) {
        writeBackLocation = prefsFile;
        if (load(prefsFile) == false) {
            logger.log(Level.INFO, "Unable to load preferences file.");
        }
    }

    /**
     * Indicates that the 'file' has been updated since the last time it was
     * saved. This allows us to check if the user wants to save their document
     * before exiting, etc.
     */
    private boolean needsSaving = false;

    /**
     * Gets whatever is stored in the file at a given location. If a value
     * could not be deserialised on loading the value returned will be null.
     * The same applies if the location does not exist.
     */
    public Object getValue(String location) {
        DocumentNode curr = findNode(location, false);
        if (curr != null) return curr.nodeValue;
        return null;
    }

    /**
     * Overwrites whatever is stored at the given location, replacing it with
     * obj. If the location is not found it will be created. If writeBackLocation
     * is set the file will be saved.
     */
    public void setValue(String location, Object data) {
        setValueImp(location, data, true);
    }

    public void setValueImp(String location, Object data, boolean setSave) {
        DocumentNode curr = findNode(location, true);
        curr.nodeValue = data;
        if (setSave && needsSaving == false && writeBackLocation == null) {
            logger.log(Level.INFO, "Writing to '" + location + "' set save flag at ", new Exception("Not really an exception"));
            needsSaving = true;
        }
        if (writeBackLocation != null) save(writeBackLocation);
    }

    /**
     * Returns the children of a given location in an arraylist. This is used
     * for e.g. enumerating the patterns for a machine.
     */
    public ExposedArrayList<Object> getChildren(String location) {
        final ExposedArrayList<Object> list = new ExposedArrayList<Object>(Object.class);
        DocumentNode parent = findNode(location, false);
        if (parent == null) return null;
        for (DocumentNode node : parent.subNodes.values()) {
            list.add(node.nodeValue);
        }
        return list;
    }

    /**
     * Removes the node at the given location. The reference is dropped and
     * the data will not be saved with the document.
     */
    public void deleteValue(String location) {
        deleteValueImp(location, true);
    }

    public void deleteValueImp(String location, boolean setSaveFlag) {
        final StringTokenizer toke = new StringTokenizer(location, "/");
        DocumentNode curr = head;
        DocumentNode parent = null;
        while (toke.hasMoreTokens() && curr != null) {
            String path = toke.nextToken();
            parent = curr;
            curr = curr.getChild(path);
        }
        if (parent != null && curr != null) {
            parent.removeChild(curr);
            if (setSaveFlag) needsSaving = true;
        }
    }

    /**
     * Tests whether a value exists in the document at the specified path. This
     * also returns true if the value is there but is null.
     */
    public boolean valueExists(String location) {
        return (findNode(location, false) != null);
    }

    public long getNewID(boolean setSaveFlag) {
        final String SERIAL_NODE = "NEXT_DOCUMENT_SERIAL";
        Long serial = (Long) getValue(SERIAL_NODE);
        if (serial == null) {
            serial = new Long(1);
        }
        setValueImp(SERIAL_NODE, serial + 1, setSaveFlag);
        return serial;
    }

    /**
     * Helper method to locate nodes using location strings. If create is true,
     * the node and the path to it will be created, otherwise if the node or
     * the path to it is not found then null is returned.
     */
    private DocumentNode findNode(String location, boolean create) {
        final StringTokenizer toke = new StringTokenizer(location, "/");
        DocumentNode curr = head;
        while (toke.hasMoreTokens() && curr != null) {
            String path = toke.nextToken();
            if (create) curr = curr.getOrAddChild(path); else curr = curr.getChild(path);
        }
        return curr;
    }

    /**
     * Loads the specified file by reading SerialisedNodes from a zipped stream.
     * Each SerialisedNode in the document is then translated to a DocumentNode
     * using the registered decoders.
     */
    public boolean load(IFileHandle input) {
        clear();
        if (input == null || input.canOpenInput() == false) return false;
        try {
            logger.log(Level.FINE, "Loading ''{0}''", input.toString());
            InputStream fis = input.openInput();
            ZipInputStream zis = new ZipInputStream(fis);
            zis.getNextEntry();
            ObjectInputStream in = new ObjectInputStream(zis);
            SerialisedNode sDoc = new SerialisedNode();
            try {
                sDoc = ((SerialisedNode) in.readObject());
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Unable to read serialised node", ex);
                return false;
            }
            in.close();
            zis.close();
            fis.close();
            recursiveDeserialise(sDoc, head);
            logger.log(Level.FINE, "Loaded ''{0}''", input.toString());
            return true;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Problem reading from file", ex);
            return false;
        }
    }

    /**
     * Saves the structure to the file specified. Each DocumentNode is translated
     * to a SerialisedNode using the registed encoders and then written to a
     * zipped stream.
     */
    public boolean save(IFileHandle output) {
        if (output == null || output.canOpenOutput() == false) return false;
        try {
            logger.log(Level.FINE, "Saving ''{0}''", output.toString());
            OutputStream fos = output.openOutput();
            ZipOutputStream zos = new ZipOutputStream(fos);
            zos.putNextEntry(new ZipEntry("OK"));
            ObjectOutputStream out = new ObjectOutputStream(zos);
            SerialisedNode sDoc = new SerialisedNode();
            recursiveSerialise(head, sDoc);
            try {
                out.writeObject(sDoc);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Unable to write serialised node", ex);
                return false;
            }
            zos.closeEntry();
            out.close();
            zos.close();
            fos.close();
            needsSaving = false;
            logger.log(Level.FINE, "Saved ''{0}''", output.toString());
            return true;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Problem writing to file", ex);
            return false;
        }
    }

    /**
     * Returns true if the document has been modified since it was last saved.
     */
    public boolean contentsChanged() {
        return needsSaving;
    }

    /**
     * Recursively scans the document creating a serialised mirror image.
     */
    private void recursiveSerialise(final DocumentNode in, final SerialisedNode out) {
        out.nodeName = in.nodeName;
        if (in.nodeValue == null) {
            out.nodeType = 0;
            out.nodeVersion = 0;
            out.nodeValue = new byte[0];
        } else {
            boolean encoded = false;
            for (IDocumentCodec encoder : docObjTypes.values()) {
                if (encoder.canStore(in.nodeValue)) {
                    try {
                        out.nodeType = encoder.getType();
                        out.nodeVersion = encoder.getVersion();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(bos);
                        encoder.writeObject(in.nodeValue, dos);
                        dos.flush();
                        byte[] data = bos.toByteArray();
                        dos.close();
                        bos.close();
                        out.nodeValue = data;
                        encoded = true;
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "Error encoding value of type " + encoder.getType(), ex);
                    }
                }
                if (encoded) break;
            }
            if (encoded == false) {
                logger.log(Level.SEVERE, "Unable to encode type {0} at {1}", new Object[] { in.nodeValue.getClass(), in.nodeName });
            }
        }
        out.subNodes = new SerialisedNode[in.subNodes.size()];
        DocumentNode[] tNodes = new DocumentNode[in.subNodes.size()];
        in.subNodes.values().toArray(tNodes);
        for (int x = 0; x < out.subNodes.length; x++) {
            DocumentNode subIn = tNodes[x];
            SerialisedNode subOut = new SerialisedNode();
            out.subNodes[x] = subOut;
            recursiveSerialise(subIn, subOut);
        }
    }

    /**
     * Recursively scans SerialisedNodes creating a document mirror image.
     */
    private void recursiveDeserialise(final SerialisedNode in, final DocumentNode out) {
        out.nodeName = in.nodeName;
        if (in.nodeValue == null || in.nodeValue.length == 0) {
            out.nodeValue = null;
        } else {
            boolean decoded = false;
            for (IDocumentCodec decoder : docObjTypes.values()) {
                if (decoder.getType() == in.nodeType) {
                    try {
                        ByteArrayInputStream bis = new ByteArrayInputStream(in.nodeValue);
                        DataInputStream dis = new DataInputStream(bis);
                        out.nodeValue = decoder.readObject(dis, in.nodeVersion);
                        dis.close();
                        bis.close();
                        decoded = true;
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "Error decoding value of type " + decoder.getType(), ex);
                    }
                }
                if (decoded == true) break;
            }
            if (decoded == false) {
                logger.log(Level.SEVERE, "Unable to decode type {0}", in.nodeType);
            }
        }
        for (SerialisedNode subIn : in.subNodes) {
            DocumentNode subOut = new DocumentNode("", null);
            recursiveDeserialise(subIn, subOut);
            out.subNodes.put(subOut.nodeName, subOut);
        }
    }

    public void clear() {
        needsSaving = false;
        head.subNodes.clear();
    }
}

/**
 * Stores runtime data in the searchable structure.
 */
final class DocumentNode {

    /**
     * The list of nodes that exist beneath this node.
     */
    public final TreeMap<String, DocumentNode> subNodes = new TreeMap<String, DocumentNode>();

    ;

    /**
     * The name to identify this node, the last part of the path to the node.
     */
    public String nodeName = null;

    /**
     * The actual object being stored is kept here. IDocumentCodecs are used
     * to encode this to bytes.
     */
    public Object nodeValue = null;

    /**
     * Construct a new node with the specified data attached.
     */
    public DocumentNode(String name, Object data) {
        nodeName = name;
        nodeValue = data;
    }

    /**
     * Get the child below this node with the specified name.
     */
    public DocumentNode getChild(String path) {
        return subNodes.get(path);
    }

    /**
     * Get the child below this node with the specified name, or create it if
     * it doesn't exist.
     */
    public DocumentNode getOrAddChild(String path) {
        DocumentNode child = subNodes.get(path);
        if (child != null) return child;
        DocumentNode newNode = new DocumentNode(path, null);
        subNodes.put(path, newNode);
        return newNode;
    }

    /**
     * Remove the specified child from this node, discarding its data forever.
     */
    public void removeChild(DocumentNode child) {
        subNodes.remove(child.nodeName);
    }
}

/**
 * WARNING! WARNING! DANGER WILL ROBINSON!
 *
 * This class is serialised to files, storing the encoded versions of objects
 * and providing methods for finding children etc.
 *
 * If you change this class all documents saved previously will be UNREADABLE.
 * Whatever reason you have for changing anything below these lines, forget it.
 * Figure out another way to implement your half-assed hack Mr. Code Monkey! ;-)
 */
final class SerialisedNode implements Serializable {

    /**
     * The name to identify this node, the last part of the path to the node.
     */
    public String nodeName = "";

    /**
     * The version of the node as found on the codec it was stored with.
     */
    public short nodeVersion = 0;

    /**
     * The type of data stored here, so we can look up a decoder.
     */
    public short nodeType = 0;

    /**
     * The actual data being stored is kept here for us to decode.
     */
    public byte[] nodeValue = null;

    /**
     * The list of nodes that exist beneath this node. This is serialised as
     * part of this node by Java's recursive mechanisms.
     */
    public SerialisedNode[] subNodes = new SerialisedNode[0];

    private static final long serialVersionUID = -1L;
}
