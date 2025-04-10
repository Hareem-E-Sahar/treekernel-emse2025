package org.jcp.xml.dsig.internal.dom;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Miscellaneous static utility methods for use in JSR 105 RI.
 *
 * @author Sean Mullan
 */
public final class Utils {

    private Utils() {
    }

    public static byte[] readBytesFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (true) {
            int read = is.read(buf);
            if (read == -1) {
                break;
            }
            baos.write(buf, 0, read);
            if (read < 1024) {
                break;
            }
        }
        return baos.toByteArray();
    }

    /**
     * Converts an Iterator to a Set of Nodes, according to the XPath
     * Data Model.
     *
     * @param i the Iterator
     * @return the Set of Nodes
     */
    static Set toNodeSet(Iterator i) {
        Set nodeSet = new HashSet();
        while (i.hasNext()) {
            Node n = (Node) i.next();
            nodeSet.add(n);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap nnm = n.getAttributes();
                for (int j = 0, length = nnm.getLength(); j < length; j++) {
                    nodeSet.add(nnm.item(j));
                }
            }
        }
        return nodeSet;
    }

    /**
     * Returns the ID from a same-document URI (ex: "#id")
     */
    public static String parseIdFromSameDocumentURI(String uri) {
        if (uri.length() == 0) {
            return null;
        }
        String id = uri.substring(1);
        if (id != null && id.startsWith("xpointer(id(")) {
            int i1 = id.indexOf('\'');
            int i2 = id.indexOf('\'', i1 + 1);
            id = id.substring(i1 + 1, i2);
        }
        return id;
    }

    /**
     * Returns true if uri is a same-document URI, false otherwise.
     */
    public static boolean sameDocumentURI(String uri) {
        return (uri != null && (uri.length() == 0 || uri.charAt(0) == '#'));
    }
}
