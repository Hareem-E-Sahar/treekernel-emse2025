package com.volantis.mcs.eclipse.ab.views.layout;

import com.volantis.mcs.eclipse.ab.ABPlugin;
import com.volantis.mcs.eclipse.ab.editors.dom.ProxyElementDetails;
import com.volantis.mcs.eclipse.common.EclipseCommonPlugin;
import com.volantis.mcs.eclipse.common.odom.MCSNamespace;
import com.volantis.mcs.eclipse.common.odom.ODOMElement;
import com.volantis.mcs.layouts.Layout;
import com.volantis.mcs.layouts.Format;
import com.volantis.mcs.layouts.FormatConstants;
import com.volantis.mcs.layouts.FormatType;
import com.volantis.mcs.layouts.LayoutSchemaType;
import com.volantis.mcs.layouts.Replica;
import com.volantis.mcs.layouts.SpatialFormatIterator;
import com.volantis.mcs.layouts.TemporalFormatIterator;
import com.volantis.mcs.layouts.CanvasLayout;
import com.volantis.mcs.layouts.MontageLayout;
import com.volantis.synergetics.log.LogDispatcher;
import com.volantis.mcs.localization.LocalizationFactory;
import com.volantis.mcs.utilities.StringUtils;
import org.jdom.Element;
import org.jdom.Namespace;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Proxy element details that will provide an intersection of attributes that
 * may be edited/viewed by the user.
 */
public class LayoutProxyElementDetails implements ProxyElementDetails {

    /**
     * Used for logging
     */
    private static final LogDispatcher logger = LocalizationFactory.createLogger(LayoutProxyElementDetails.class);

    /**
     * The empty list constant.
     */
    private static final String[] EMPTY_LIST = {};

    /**
     * Map of attribute names to their xml name. E.g. 'Name' to 'name'
     */
    private static Map attributeMap = null;

    /**
     * A list of formats (Format objects). {@link #findFormat}
     */
    private static List formats;

    /**
     * List of grid row element names.
     */
    private static final List GRID_ROW_ELEMENT_NAMES = Arrays.asList(new Object[] { LayoutSchemaType.SEGMENT_GRID_FORMAT_ROW_ELEMENT.getName(), LayoutSchemaType.GRID_FORMAT_ROWS_ELEMENT.getName() });

    /**
     * List of grid column element names.
     */
    private static final List GRID_COLUMN_ELEMENT_NAMES = Arrays.asList(new Object[] { LayoutSchemaType.SEGMENT_GRID_FORMAT_COLUMN_ELEMENT.getName(), LayoutSchemaType.GRID_FORMAT_COLUMN_ELEMENT.getName() });

    /**
     * List of grid row attribute names.
     */
    private static final List GRID_ROW_ATTRIBUTE_NAMES = Arrays.asList(new Object[] { LayoutSchemaType.GRID_HEIGHT_ATTRIBUTE.getName(), LayoutSchemaType.GRID_HEIGHT_UNITS_ATTRIBUTE.getName(), LayoutSchemaType.STYLE_CLASS_ATTRIBUTE.getName() });

    /**
     * List of grid column attribute names.
     */
    private static final List GRID_COLUMN_ATTRIBUTE_NAMES = Arrays.asList(new Object[] { LayoutSchemaType.GRID_WIDTH_ATTRIBUTE.getName(), LayoutSchemaType.GRID_WIDTH_UNITS_ATTRIBUTE.getName(), LayoutSchemaType.STYLE_CLASS_ATTRIBUTE.getName() });

    /**
     * The name of the current proxy element.
     */
    private String elementName = ODOMElement.NULL_ELEMENT_NAME;

    /**
     * A cache of the attribute names that may be displayed.
     */
    private String[] attributesToDisplay;

    /**
     * An intersection of all the attribute names in the current (multiple)
     * selection.
     */
    private List selectedElementNames;

    /**
     * Default constructor.
     */
    public LayoutProxyElementDetails() {
        initializeAttributesMap();
    }

    /**
     * Those attributes obtained from the Format#getUserAttributes methods that
     * don't also appear in the XML file will be ignored anyway.
     */
    private void initializeAttributesMap() {
        if (attributeMap == null) {
            attributeMap = new HashMap();
            initializeFormats();
            Iterator iterator = formats.iterator();
            StringBuffer newName = new StringBuffer();
            while (iterator.hasNext()) {
                Format format = (Format) iterator.next();
                String[] attrs = format.getUserAttributes();
                for (int i = 0; i < attrs.length; i++) {
                    if (!attributeMap.containsKey(attrs[i])) {
                        newName.setLength(0);
                        newName.append(StringUtils.toLowerIgnoreLocale(String.valueOf(attrs[i].charAt(0))));
                        newName.append(attrs[i].substring(1, attrs[i].length()));
                        attributeMap.put(attrs[i], newName.toString());
                    }
                }
            }
            attributeMap.put(FormatConstants.RESET_ATTRIBUTE, "allowReset");
            attributeMap.put(FormatConstants.BACKGROUND_COLOUR_ATTRIBUTE, "backgroundColor");
            attributeMap.put(FormatConstants.BORDER_COLOUR_ATTRIBUTE, "borderColor");
            attributeMap.put(FormatConstants.ITERATOR_2D_INDEXING_DIR_ATTRIBUTE, "indexingDirection");
            attributeMap.put(FormatConstants.ITERATOR_ROW_COUNT_ATTRIBUTE, "rowCount");
            attributeMap.put(FormatConstants.ITERATOR_COLUMN_COUNT_ATTRIBUTE, "columnCount");
            attributeMap.put(FormatConstants.FRAGMENT_LINK_STYLE_CLASS_ATTRIBUTE, "linkStyleClass");
            attributeMap.put(FormatConstants.PEER_LINK_ATTRIBUTE, "showPeerLinks");
            attributeMap.put(FormatConstants.FRAG_LINK_ORDER_ATTRIBUTE, "fragmentLinkOrder");
            attributeMap.put(Replica.REPLICANT_ATTRIBUTE, "sourceFormatName");
            attributeMap.put(Replica.REPLICANT_TYPE_ATTRIBUTE, "sourceFormatType");
            attributeMap.put(SpatialFormatIterator.SPATIAL_ITERATOR_COLUMNS, "columns");
            attributeMap.put(SpatialFormatIterator.SPATIAL_ITERATOR_ROWS, "rows");
            attributeMap.put(TemporalFormatIterator.TEMPORAL_ITERATOR_CLOCK_VALUES, "clockValues");
            attributeMap.put(TemporalFormatIterator.TEMPORAL_ITERATOR_CELLS, "cells");
            attributeMap.put(TemporalFormatIterator.TEMPORAL_ITERATOR_CELL_COUNT, "cellCount");
            attributeMap.put(FormatConstants.FILTER_KEYBOARD_USABILITY_ATTRIBUTE, "filterOnKeyboardUsability");
            attributeMap.put(FormatConstants.NEXT_SHARD_LINK_TEXT_ATTRIBUTE, "nextLinkText");
            attributeMap.put(FormatConstants.NEXT_SHARD_SHORTCUT_ATTRIBUTE, "nextLinkShortcut");
            attributeMap.put(FormatConstants.NEXT_SHARD_LINK_CLASS_ATTRIBUTE, "nextLinkStyleClass");
            attributeMap.put(FormatConstants.PREVIOUS_SHARD_LINK_TEXT_ATTRIBUTE, "previousLinkText");
            attributeMap.put(FormatConstants.PREVIOUS_SHARD_SHORTCUT_ATTRIBUTE, "previousLinkShortcut");
            attributeMap.put(FormatConstants.PREVIOUS_SHARD_LINK_CLASS_ATTRIBUTE, "previousLinkStyleClass");
            attributeMap.put(FormatConstants.NEXT_LINK_TEXT_ATTRIBUTE, "nextLinkText");
            attributeMap.put(FormatConstants.NEXT_LINK_POSITION_ATTRIBUTE, "nextLinkPosition");
            attributeMap.put(FormatConstants.NEXT_LINK_STYLE_ATTRIBUTE, "nextLinkStyleClass");
            attributeMap.put(FormatConstants.PREVIOUS_LINK_TEXT_ATTRIBUTE, "previousLinkText");
            attributeMap.put(FormatConstants.PREVIOUS_LINK_POSITION_ATTRIBUTE, "previousLinkPosition");
            attributeMap.put(FormatConstants.PREVIOUS_LINK_STYLE_ATTRIBUTE, "previousLinkStyleClass");
            attributeMap.put(FormatConstants.MAXIMUM_CONTENT_SIZE_ATTRIBUTE, "maxContentSize");
        }
    }

    /**
     * Initialize the array of format objects.
     * {@link #findFormat}
     */
    private void initializeFormats() {
        if (formats == null) {
            formats = new ArrayList();
        }
        Iterator iterator = FormatType.iterator();
        List exceptions = new ArrayList();
        Object canvasArgs[] = { new CanvasLayout() };
        Object montageArgs[] = { new MontageLayout() };
        while (iterator.hasNext()) {
            FormatType type = (FormatType) iterator.next();
            Class formatClass = type.getFormatClass();
            Format format = null;
            Class[] argTypes;
            argTypes = new Class[] { CanvasLayout.class };
            try {
                Constructor constructor = formatClass.getConstructor(argTypes);
                format = (Format) constructor.newInstance(canvasArgs);
            } catch (NoSuchMethodException e) {
                trackException(exceptions, e);
            } catch (SecurityException e) {
                trackException(exceptions, e);
            } catch (InstantiationException e) {
                trackException(exceptions, e);
            } catch (IllegalAccessException e) {
                trackException(exceptions, e);
            } catch (IllegalArgumentException e) {
                trackException(exceptions, e);
            } catch (InvocationTargetException e) {
                trackException(exceptions, e);
            }
            if (format == null) {
                argTypes = new Class[] { MontageLayout.class };
                try {
                    Constructor constructor = formatClass.getConstructor(argTypes);
                    format = (Format) constructor.newInstance(montageArgs);
                } catch (NoSuchMethodException e) {
                    trackException(exceptions, e);
                } catch (SecurityException e) {
                    trackException(exceptions, e);
                } catch (InstantiationException e) {
                    trackException(exceptions, e);
                } catch (IllegalAccessException e) {
                    trackException(exceptions, e);
                } catch (IllegalArgumentException e) {
                    trackException(exceptions, e);
                } catch (InvocationTargetException e) {
                    trackException(exceptions, e);
                }
            }
            if (format != null) {
                formats.add(format);
            } else if (exceptions.size() > 0) {
                StringBuffer msgs = new StringBuffer();
                for (int i = 0; i < exceptions.size(); i++) {
                    Exception exception = (Exception) exceptions.get(i);
                    msgs.append(exception.getMessage()).append(Character.LINE_SEPARATOR);
                }
                Exception exception = new IllegalStateException(msgs.toString());
                EclipseCommonPlugin.handleError(ABPlugin.getDefault(), exception);
            }
        }
    }

    /**
     * Helper method to track the exception.
     *
     * @param exceptions list of exceptions
     * @param e          the current exception.
     */
    private void trackException(List exceptions, Exception e) {
        exceptions.add(e);
        if (logger.isDebugEnabled()) {
            logger.debug("Exception occured: ", e);
        }
    }

    /**
     * Compute the intersection of attribute names with the format's user
     * attribute names thus returning a list of all user editable attribute
     * names.
     *
     * @param elementNames the current list of element names, may be empty or
     *                     null.
     * @return the intersection of attributes names, or EMPTY_LIST there is no
     *         intersection.
     */
    protected String[] computeIntersection(List elementNames) {
        String[] result = EMPTY_LIST;
        if (elementNames != null && elementNames.size() > 0) {
            List intersection = null;
            boolean finished = false;
            Iterator iterator = elementNames.iterator();
            while (!finished && iterator.hasNext()) {
                String element = (String) iterator.next();
                Format format = findFormat(element);
                if (format != null) {
                    List attributeList = Arrays.asList(format.getUserAttributes());
                    intersection = updateIntersection(intersection, attributeList);
                } else {
                    if (GRID_ROW_ELEMENT_NAMES.contains(element)) {
                        intersection = updateIntersection(intersection, GRID_ROW_ATTRIBUTE_NAMES);
                    } else if (GRID_COLUMN_ELEMENT_NAMES.contains(element)) {
                        intersection = updateIntersection(intersection, GRID_COLUMN_ATTRIBUTE_NAMES);
                    } else {
                        intersection = null;
                        finished = true;
                    }
                }
            }
            if (intersection != null) {
                result = new String[intersection.size()];
                for (int i = 0; i < intersection.size(); i++) {
                    String key = (String) intersection.get(i);
                    String value = (String) attributeMap.get(key);
                    if (value == null) {
                        value = key;
                    }
                    result[i] = value;
                }
            }
        }
        return result;
    }

    /**
     * Helper method for updating the intersection list based on the current
     * intersection list and the list of attributes.
     *
     * @param intersection  the current intersection list.
     * @param attributeList the attributes list.
     * @return the updated intersection list.
     */
    private List updateIntersection(List intersection, List attributeList) {
        if (intersection == null) {
            intersection = new ArrayList(attributeList);
        } else {
            intersection.retainAll(attributeList);
        }
        return intersection;
    }

    /**
     * Search for the format in the formats list. <p>
     *
     * The number of formats is expected to be small which is why the format are
     * held in a list and not a map.
     *
     * @param element the element name to match with a format's element name.
     * @return the found format, or null if not found.
     */
    private Format findFormat(String element) {
        Format result = null;
        if (element != null) {
            Iterator iterator = formats.iterator();
            while (iterator.hasNext() && result == null) {
                Format format = (Format) iterator.next();
                if (element.equals(format.getFormatType().getElementName())) {
                    result = format;
                }
            }
        }
        return result;
    }

    public String[] getAttributeNames() {
        if (attributesToDisplay == null) {
            attributesToDisplay = computeIntersection(selectedElementNames);
        }
        if (attributesToDisplay != null && attributesToDisplay.length > 0) {
            Arrays.sort(attributesToDisplay, String.CASE_INSENSITIVE_ORDER);
        }
        return attributesToDisplay;
    }

    public String getElementName() {
        return elementName;
    }

    /**
     * Returns the default Namespace with the "lpdm" prefix.
     */
    public Namespace getElementNamespace() {
        return MCSNamespace.LPDM;
    }

    public boolean isAttributeName(String name) {
        return Arrays.binarySearch(getAttributeNames(), name) >= 0;
    }

    public boolean setProxiedElements(Iterator elements, ProxyElementDetails.ChangeReason reason) {
        if (logger.isDebugEnabled()) {
            logger.debug("setProxiedElements");
        }
        boolean needsUpdate = false;
        if (reason != ATTRIBUTES && reason != ATTRIB_VALUES) {
            if (logger.isDebugEnabled()) {
                logger.debug("setProxiedElements: reason is valid");
            }
            List elementNames = new ArrayList();
            String newElementName = ODOMElement.NULL_ELEMENT_NAME;
            while (elements.hasNext()) {
                Element next = (Element) elements.next();
                if (next != null) {
                    String name = next.getName();
                    if (!elementNames.contains(name)) {
                        elementNames.add(name);
                    }
                    if (!ODOMElement.UNDEFINED_ELEMENT_NAME.equals(newElementName)) {
                        if (newElementName == ODOMElement.NULL_ELEMENT_NAME && !name.equals(ODOMElement.UNDEFINED_ELEMENT_NAME)) {
                            newElementName = name;
                        } else if (!newElementName.equals(name)) {
                            newElementName = ODOMElement.UNDEFINED_ELEMENT_NAME;
                        }
                    }
                }
            }
            if (!newElementName.equals(elementName)) {
                elementName = newElementName;
                needsUpdate = true;
            }
            if (elementNames.size() > 0) {
                if (selectedElementNames == null || selectedElementNames.size() != elementNames.size() || !elementNames.containsAll(selectedElementNames)) {
                    selectedElementNames = elementNames;
                }
            } else {
                selectedElementNames = null;
            }
            attributesToDisplay = computeIntersection(selectedElementNames);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("setProxiedElements: needsUpdate=" + needsUpdate);
            logger.debug("setProxiedElements: elementName=\"" + elementName + "\"");
        }
        return needsUpdate;
    }
}
