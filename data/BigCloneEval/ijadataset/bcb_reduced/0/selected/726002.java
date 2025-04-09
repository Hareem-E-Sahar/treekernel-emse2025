package prefwork.rater;

import java.lang.reflect.Constructor;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;
import prefwork.Attribute;
import prefwork.AttributeValue;
import prefwork.CommonUtils;
import weka.classifiers.Classifier;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class WekaRater implements Rater {

    Classifier cModel = null;

    String classifierName = "";

    boolean wantsNumericClass = true;

    Integer targetAttribute = 2;

    Attribute[] attributes;

    FastVector fvWekaAttributes;

    Instances isTrainingSet;

    public String toString() {
        return "WekaRater" + classifierName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void configClassifier(XMLConfiguration config, String section) {
        Configuration methodConf = config.configurationAt(section);
        try {
            classifierName = CommonUtils.getFromConfIfNotNull(methodConf, "classifier", classifierName);
            Class c = Class.forName(classifierName);
            Constructor[] a = c.getConstructors();
            cModel = (Classifier) a[0].newInstance();
        } catch (Exception e) {
        }
        try {
            wantsNumericClass = methodConf.getBoolean("wantsNumericClass", wantsNumericClass);
        } catch (Exception e) {
        }
    }

    @Override
    public Double getRating(Double[] ratings, AttributeValue[] record) {
        try {
            double[] fDistribution = cModel.distributionForInstance(getWekaInstance(ratings));
            double res = 0.0;
            for (int i = 0; i < fDistribution.length; i++) if (wantsNumericClass) res += fDistribution[i]; else res += CommonUtils.objectToDouble(((weka.core.Attribute) fvWekaAttributes.elementAt(0)).value(i)) * fDistribution[i];
            return res;
        } catch (Exception e) {
        }
        return null;
    }

    protected Instance getWekaInstance(List<Object> rec) {
        Instance iExample = null;
        iExample = new Instance(fvWekaAttributes.size());
        iExample.setDataset(isTrainingSet);
        iExample.setValue((weka.core.Attribute) fvWekaAttributes.elementAt(0), CommonUtils.objectToDouble(rec.get(2)));
        if (wantsNumericClass) iExample.setValue((weka.core.Attribute) fvWekaAttributes.elementAt(0), CommonUtils.objectToDouble(rec.get(targetAttribute))); else {
            iExample.setValue((weka.core.Attribute) fvWekaAttributes.elementAt(0), rec.get(targetAttribute).toString());
        }
        for (int j = 3; j < rec.size(); j++) {
            iExample.setValue((weka.core.Attribute) fvWekaAttributes.elementAt(j - 2), attributes[j].getNorm().normalize(rec));
        }
        return iExample;
    }

    protected Instance getWekaInstance(Double[] ratings) {
        Instance iExample = null;
        iExample = new Instance(fvWekaAttributes.size());
        iExample.setDataset(isTrainingSet);
        for (int j = 3; j < ratings.length; j++) {
            iExample.setValue((weka.core.Attribute) fvWekaAttributes.elementAt(j - 2), ratings[j]);
        }
        return iExample;
    }

    protected FastVector getClasses(Attribute[] attributes) {
        FastVector f = new FastVector();
        Attribute attr = attributes[3];
        for (AttributeValue val : attr.getValues()) {
            for (List<Object> rec : val.getRecords()) {
                if (!f.contains(rec.get(2).toString())) f.addElement(rec.get(2).toString());
            }
        }
        return f;
    }

    protected void loadAttributes(Attribute[] attributes) {
        this.attributes = attributes;
        fvWekaAttributes = new FastVector(attributes.length - 2);
        for (int i = 0; i < attributes.length - 2; i++) {
            fvWekaAttributes.addElement(new weka.core.Attribute("Attr" + i));
        }
        if (!wantsNumericClass) fvWekaAttributes.setElementAt(new weka.core.Attribute("class", getClasses(attributes)), 0);
        isTrainingSet = new Instances("Rel", fvWekaAttributes, 10);
        isTrainingSet.setClassIndex(0);
        Attribute attr = attributes[3];
        for (AttributeValue attrVal : attr.getValues()) {
            for (List<Object> rec : attrVal.getRecords()) {
                isTrainingSet.add(getWekaInstance(rec));
            }
        }
    }

    @Override
    public void init(Attribute[] attributes) {
        loadAttributes(attributes);
        try {
            cModel.buildClassifier(isTrainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public double compareTo(Rater n) {
        return 0;
    }
}
