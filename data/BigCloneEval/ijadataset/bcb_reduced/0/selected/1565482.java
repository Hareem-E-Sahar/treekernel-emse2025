package org.cleartk.classifier.feature.extractor.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.BooleanArray;
import org.apache.uima.jcas.cas.ByteArray;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.FloatArray;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.LongArray;
import org.apache.uima.jcas.cas.ShortArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.util.UIMAUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 * 
 *         TODO handle cases where the pathValue is not a String so that they are not necessarily
 *         converted to strings
 */
public class TypePathExtractor implements SimpleFeatureExtractor {

    Class<? extends Annotation> focusClass;

    Type type;

    String path;

    boolean allPaths;

    boolean allValues;

    boolean uniqueValues;

    boolean pathChecked = false;

    TypeSystem typeSystem;

    Logger logger = Logger.getLogger(TypePathExtractor.class.getName());

    /**
   * This extractor creates features from attributes of an annotation. For example, if you had a
   * type called Token with an attribute called 'pos' you could use this extractor to extract a pos
   * attribute for tokens. This would be done by calling the constructor with the values "pos",
   * "pos", false, and false.
   * 
   * The value you want may be nested within the type structure of the annotation that is being
   * examined. For example, if you might have a type called 'NamedEntity' that has an attribute of
   * type 'ResourceEntry' that has an attribute of type 'DbRecord' that has a String attribute
   * called 'identifier'. You may want to extract the value of the identifier as a feature of the
   * NamedEntity annotation. This could be done by calling the constructor with the values
   * "identifier", "resourceEntry/dbRecord/identifier", false, and false (or something similar).
   * 
   * The value you want for your featured may be multi-valued
   * 
   * @param focusClass
   *          the type of annotation that you are doing feature extraction on.
   * @param typePath
   *          a string representation of the path that should be traversed to extract a feature
   *          value (e.g. "resourceEntry/dbRecord/identifier" or "pos" from the examples above.)
   * @param traverseAllPaths
   *          The path you traverse to the value you are trying to retrieve may include attributes
   *          that have multiple-values. If true, then all paths are examined and features for each
   *          traversal are added if possible. If false, then the first path that results in a
   *          non-null value will be examined.
   * @param returnAllValues
   *          The last node of the path may be multi-valued. If true, then all values found in the
   *          last node will be returned as features. If false, then only the first value of the
   *          last node is returned. If traverseAllPaths and returnAllValues are both false, then a
   *          list of size 0 or 1 will be returned. The other three combinations are each valid and
   *          may return a list of size 0 or greater.
   * @param uniqueValues
   *          if true, then the returned values of the extract method will be unique.
   */
    public TypePathExtractor(Class<? extends Annotation> focusClass, String typePath, boolean traverseAllPaths, boolean returnAllValues, boolean uniqueValues) {
        this.focusClass = focusClass;
        this.path = typePath;
        this.allPaths = traverseAllPaths;
        this.allValues = returnAllValues;
        this.uniqueValues = uniqueValues;
    }

    /**
   * calls this(type, typePath, false, false, true, jCas)
   */
    public TypePathExtractor(Class<? extends Annotation> focusClass, String typePath) {
        this(focusClass, typePath, false, false, true);
    }

    public List<org.cleartk.classifier.Feature> extract(JCas view, Annotation focusAnnotation) throws CleartkExtractorException {
        if (this.type == null) this.type = UIMAUtil.getCasType(view, this.focusClass);
        this.typeSystem = view.getTypeSystem();
        if (!isValidPath(view)) throw CleartkExtractorException.invalidTypePath(path, type);
        String[] pathMembers = path.split("/");
        List<Object> pathValues = new ArrayList<Object>();
        _extract(view, focusAnnotation, pathMembers, pathValues);
        List<org.cleartk.classifier.Feature> returnValues = new ArrayList<org.cleartk.classifier.Feature>();
        Set<Object> values = new HashSet<Object>();
        for (Object pathValue : pathValues) {
            if (uniqueValues) {
                if (!values.contains(pathValue)) {
                    org.cleartk.classifier.Feature feature = new org.cleartk.classifier.feature.TypePathFeature(null, pathValue, this.path);
                    returnValues.add(feature);
                    values.add(pathValue);
                }
            } else {
                org.cleartk.classifier.Feature feature = new org.cleartk.classifier.feature.TypePathFeature(null, pathValue, this.path);
                returnValues.add(feature);
            }
        }
        return returnValues;
    }

    private void _extract(JCas view, FeatureStructure featureStructure, String[] pathMembers, List<Object> pathValues) throws CleartkExtractorException {
        if (pathMembers.length == 1) {
            Feature feature = featureStructure.getType().getFeatureByBaseName(pathMembers[0]);
            if (feature == null) {
                return;
            }
            Type featureType = feature.getRange();
            if (featureType.isPrimitive()) {
                Object pathValue = getPrimitiveFeatureValue(view, featureStructure, feature);
                if (pathValue != null) pathValues.add(pathValue);
            } else if (typeSystem.subsumes(typeSystem.getType("uima.tcas.Annotation"), featureType)) {
                String coveredText = ((Annotation) featureStructure.getFeatureValue(feature)).getCoveredText();
                if (coveredText != null) pathValues.add(coveredText);
            } else if (featureType.isArray()) {
                Type componentType = featureType.getComponentType();
                if (componentType.isPrimitive()) {
                    Object[] values = getPrimitiveArrayFeatureValue(view, featureStructure, feature);
                    if (allValues) pathValues.addAll(Arrays.asList(values)); else if (values.length > 0) pathValues.add(values[0]);
                } else if (typeSystem.subsumes(typeSystem.getType("uima.tcas.Annotation"), componentType)) {
                    ArrayFS fsArray = (ArrayFS) featureStructure.getFeatureValue(feature);
                    FeatureStructure[] array = fsArray.toArray();
                    if (allValues) {
                        for (FeatureStructure ftr : array) pathValues.add(((Annotation) ftr).getCoveredText());
                    } else {
                        if (array.length > 0) pathValues.add(((Annotation) array[0]).getCoveredText());
                    }
                }
            }
        } else {
            String[] remainingPathMembers = new String[pathMembers.length - 1];
            System.arraycopy(pathMembers, 1, remainingPathMembers, 0, pathMembers.length - 1);
            Feature feature = featureStructure.getType().getFeatureByBaseName(pathMembers[0]);
            FeatureStructure featureValue = featureStructure.getFeatureValue(feature);
            if (featureValue == null) return;
            if (featureValue instanceof FSArray) {
                FSArray fsArray = (FSArray) featureValue;
                if (allPaths) {
                    for (int i = 0; i < fsArray.size(); i++) {
                        FeatureStructure fs = fsArray.get(i);
                        _extract(view, fs, remainingPathMembers, pathValues);
                    }
                } else {
                    if (fsArray.size() > 0) _extract(view, fsArray.get(0), remainingPathMembers, pathValues);
                }
            } else {
                _extract(view, featureValue, remainingPathMembers, pathValues);
            }
        }
    }

    private boolean isValidPath(JCas view) {
        if (!pathChecked) {
            boolean validPath = isValidPath(type, path, view);
            if (validPath) pathChecked = true;
            return validPath;
        } else return true;
    }

    public static boolean isValidPath(Type type, String path, JCas view) {
        String[] pathMembers = path.split("/");
        Type pathMemberType = type;
        for (String pathMember : pathMembers) {
            Feature feature = pathMemberType.getFeatureByBaseName(pathMember);
            if (feature == null) {
                return false;
            }
            pathMemberType = feature.getRange();
            if (pathMemberType.isArray()) pathMemberType = pathMemberType.getComponentType();
        }
        return isValidType(pathMemberType, view.getTypeSystem());
    }

    private static final String[] HANDLED_TYPES = new String[] { "uima.cas.Boolean", "uima.cas.BooleanArray", "uima.cas.Byte", "uima.cas.ByteArray", "uima.cas.Double", "uima.cas.DoubleArray", "uima.cas.Float", "uima.cas.FloatArray", "uima.cas.FloatList", "uima.cas.Integer", "uima.cas.IntegerArray", "uima.cas.IntegerList", "uima.cas.Long", "uima.cas.LongArray", "uima.cas.Short", "uima.cas.ShortArray", "uima.cas.String", "uima.cas.StringArray", "uima.cas.StringList", "uima.tcas.Annotation" };

    public static boolean isValidType(Type type, TypeSystem typeSystem) {
        String typeName = type.getName();
        for (String handledType : HANDLED_TYPES) {
            if (typeName.equals(handledType)) return true;
        }
        if (typeSystem.subsumes(typeSystem.getType("uima.cas.String"), type)) return true;
        if (typeSystem.subsumes(typeSystem.getType("uima.tcas.Annotation"), type)) return true;
        return false;
    }

    /**
   * see section 4.2.1 of the UIMA References documentation.
   * 
   * @param view
   * @param featureStructure
   * @param feature
   * @return The feature value.
   */
    private static Object getPrimitiveFeatureValue(JCas view, FeatureStructure featureStructure, Feature feature) throws CleartkExtractorException {
        TypeSystem typeSystem = view.getTypeSystem();
        Type type = feature.getRange();
        if (type.equals(typeSystem.getType("uima.cas.Boolean"))) return featureStructure.getBooleanValue(feature); else if (type.equals(typeSystem.getType("uima.cas.Double"))) return featureStructure.getDoubleValue(feature); else if (type.equals(typeSystem.getType("uima.cas.Float"))) return featureStructure.getFloatValue(feature); else if (type.equals(typeSystem.getType("uima.cas.Byte"))) return featureStructure.getByteValue(feature); else if (type.equals(typeSystem.getType("uima.cas.Short"))) return featureStructure.getShortValue(feature); else if (type.equals(typeSystem.getType("uima.cas.Integer"))) return featureStructure.getIntValue(feature); else if (type.equals(typeSystem.getType("uima.cas.Long"))) return featureStructure.getLongValue(feature); else if (type.equals(typeSystem.getType("uima.cas.String"))) return featureStructure.getStringValue(feature); else throw CleartkExtractorException.notPrimitive(feature);
    }

    private static Object[] getPrimitiveArrayFeatureValue(JCas view, FeatureStructure featureStructure, Feature feature) throws CleartkExtractorException {
        TypeSystem typeSystem = view.getTypeSystem();
        Type type = feature.getRange();
        if (type.isArray()) {
            Type componentType = type.getComponentType();
            FeatureStructure featureValue = featureStructure.getFeatureValue(feature);
            if (componentType.equals(typeSystem.getType("uima.cas.String"))) {
                return ((StringArray) featureValue).toArray();
            } else if (componentType.equals(typeSystem.getType("uima.cas.Boolean"))) {
                return Arrays.asList(((BooleanArray) featureValue).toArray()).toArray();
            } else if (componentType.equals(typeSystem.getType("uima.cas.Double"))) {
                return Arrays.asList(((DoubleArray) featureValue).toArray()).toArray();
            } else if (componentType.equals(typeSystem.getType("uima.cas.Float"))) {
                return Arrays.asList(((FloatArray) featureValue).toArray()).toArray();
            } else if (componentType.equals(typeSystem.getType("uima.cas.Byte"))) {
                return Arrays.asList(((ByteArray) featureValue).toArray()).toArray();
            } else if (componentType.equals(typeSystem.getType("uima.cas.Short"))) {
                return Arrays.asList(((ShortArray) featureValue).toArray()).toArray();
            } else if (componentType.equals(typeSystem.getType("uima.cas.Integer"))) {
                return Arrays.asList(((IntegerArray) featureValue).toArray()).toArray();
            } else if (componentType.equals(typeSystem.getType("uima.cas.Long"))) {
                return Arrays.asList(((LongArray) featureValue).toArray()).toArray();
            }
        } else throw CleartkExtractorException.notPrimitiveArray(feature);
        return null;
    }

    public boolean isAllPaths() {
        return allPaths;
    }

    public boolean isAllValues() {
        return allValues;
    }

    public Class<? extends Annotation> getFocusClass() {
        return focusClass;
    }

    public String getPath() {
        return path;
    }

    public boolean isUniqueValues() {
        return uniqueValues;
    }
}
