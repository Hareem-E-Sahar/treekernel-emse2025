package com.depthexplorer.animation;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import java.util.*;
import java.lang.reflect.Constructor;

/**
 * The AnimationElement class represents a single XML element.
 * The transform() method advances the interpolation of this element one frame forward.
 * @author Ashish Datta
 *
 */
public class AnimationElement {

    private String id, attr, itemName;

    private int lastKeyFrame, frameCount, fps, currentFrame = 0;

    private String value, valueAnimateAttribute, xmlChanges;

    private boolean valueAnimate = false;

    private Node myNode;

    private XMLinterpolator interpolate;

    private Hashtable<String, String> endValues = new Hashtable<String, String>();

    private Hashtable<String, String> startValues = new Hashtable<String, String>();

    public AnimationElement() {
    }

    /**
	 * Causes the object to animate one frame forward.
	 */
    public void transform() {
        NamedNodeMap n = myNode.getAttributes();
        int nodeIndex = -1;
        Enumeration<String> keys = endValues.keys();
        String searchString = "", newVal = "", currentKey = "", newValString = "", endValue = "-1";
        while (keys.hasMoreElements()) {
            currentKey = keys.nextElement();
            endValue = endValues.get(currentKey);
            try {
                newVal = interpolate.interpolateItem(itemName, currentKey, startValues.get(currentKey), endValue, ((double) currentFrame / lastKeyFrame));
                if (valueAnimate) newValString += currentKey + ": " + newVal + "; ";
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        if (valueAnimate) {
            if (newValString.charAt(newValString.length() - 2) == ';') newValString = newValString.substring(0, newValString.length() - 2);
            searchString = valueAnimateAttribute;
            newVal = newValString;
        } else {
            searchString = currentKey;
        }
        nodeIndex = -1;
        for (int i = 0; i < n.getLength(); i++) {
            if (n.item(i).getNodeName().compareToIgnoreCase(searchString) == 0) {
                nodeIndex = i;
            }
        }
        if (nodeIndex == -1) {
            System.err.println("transform(): Could not find " + searchString + " in the attr list.");
            return;
        }
        xmlChanges = searchString + '=' + '"' + newVal + '"';
        n.item(nodeIndex).setNodeValue(newVal);
        currentFrame++;
    }

    public String getId() {
        return id;
    }

    /**
	 * Links in a DOM node.
	 * @param n DOM node to link.
	 */
    public void setNode(Node n) {
        String valString = "", parsedValue = "", key = "";
        NamedNodeMap nodeAttributes = n.getAttributes();
        Enumeration<String> keys = endValues.keys();
        myNode = n;
        itemName = n.getNodeName();
        if (valueAnimate) {
            for (int i = 0; i < nodeAttributes.getLength(); i++) {
                if (nodeAttributes.item(i).getNodeName().compareToIgnoreCase("value") == 0 || nodeAttributes.item(i).getNodeName().compareToIgnoreCase("parameter") == 0) valString = nodeAttributes.item(i).getNodeValue();
            }
            while (keys.hasMoreElements()) {
                key = keys.nextElement();
                parsedValue = extractValue(key, valString);
                startValues.put(key, parsedValue);
            }
        } else {
            key = keys.nextElement();
            for (int i = 0; i < nodeAttributes.getLength(); i++) {
                if (nodeAttributes.item(i).getNodeName().compareToIgnoreCase(key) == 0) startValues.put(key, nodeAttributes.item(i).getNodeValue());
            }
        }
    }

    /**
	 * Sets up some animation variables.
	 * @param framecount Number of frames in the animation.
	 * @param fps Frames per second.
	 * @param interpolateClass The class to use to interpolate the attributes.
	 */
    public void setParams(int framecount, int fps, String interpolateClass) {
        this.fps = fps;
        this.frameCount = framecount;
        try {
            Class<?> cls = Class.forName(interpolateClass);
            Constructor<?>[] crt = cls.getConstructors();
            interpolate = (XMLinterpolator) crt[0].newInstance();
        } catch (Exception e) {
            System.out.println("Fatal error instantiating class " + interpolateClass);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
	 * Loads the animation XML node.
	 * @param xmlNode XML DOM node.
	 */
    public void load(Node xmlNode) {
        NamedNodeMap attrs = xmlNode.getAttributes();
        Node attr = null;
        if (attrs == null) return;
        for (int i = 0; i < attrs.getLength(); i++) {
            attr = attrs.item(i);
            String name = attr.getNodeName().trim();
            String value = attr.getNodeValue().trim();
            setAttribute(name, value);
        }
    }

    /**
	 * Links attributes to values in the class.
	 * @param name Name of the attribute.
	 * @param value Value of the attribute.
	 */
    private void setAttribute(String name, String value) {
        if (name.compareToIgnoreCase("id") == 0) {
            id = value.trim();
        }
        if (name.compareToIgnoreCase("attribute") == 0) {
            attr = value.trim();
        }
        if (name.compareToIgnoreCase("frame") == 0) {
            lastKeyFrame = Integer.parseInt(value.trim());
        }
        if (name.compareToIgnoreCase("value") == 0 || name.compareToIgnoreCase("parameter") == 0) {
            if (value.indexOf(":") > 0) {
                parseValue(value);
                valueAnimate = true;
                valueAnimateAttribute = name;
            } else {
                this.value = value;
                endValues.put(attr, value);
            }
        }
    }

    /**
	 * Pulls out the value of field in the transformation string.
	 * @param field Field to extract.
	 * @param transformString Transformation string to extract from.
	 * @return Returns the value of field in transformString.
	 */
    private String extractValue(String field, String transformString) {
        String matches[] = transformString.split("\\s*;\\s*");
        for (int i = 0; i < matches.length; i++) {
            String parts[] = matches[i].split("\\s*:\\s*");
            parts[0] = parts[0].trim();
            parts[1] = parts[1].trim();
            if (parts[0].toLowerCase().compareToIgnoreCase(field) == 0) return parts[1].toLowerCase();
        }
        System.err.println("extractValue: could not find " + field + " in " + transformString);
        return "-1";
    }

    /**
	 * Parses a transformation string and saves the values.
	 * @param transformString The transformation string.
	 */
    private void parseValue(String transformString) {
        String matches[] = transformString.split("\\s*;\\s*");
        for (int i = 0; i < matches.length; i++) {
            String parts[] = matches[i].split("\\s*:\\s*");
            parts[0] = parts[0].trim();
            parts[1] = parts[1].trim();
            endValues.put(parts[0].toLowerCase(), parts[1].toLowerCase());
        }
    }

    public String toString() {
        String t = new String();
        if (this.valueAnimate) t = endValues.toString(); else t = attr + "," + value;
        return t;
    }

    /**
	 * Retrieves the modified XML tags
	 * @return Returns modified XML tag.
	 */
    public String getXmlChanges() {
        return xmlChanges;
    }
}
