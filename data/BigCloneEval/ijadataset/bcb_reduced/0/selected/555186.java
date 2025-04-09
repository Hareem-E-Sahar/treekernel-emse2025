package apollo.datamodel;

import java.util.*;
import apollo.gui.*;

public class SortedFeatureSet extends FeatureSet {

    public SortedFeatureSet() {
        super();
    }

    public void addFeature(SeqFeatureI feature) {
        long fLow = feature.getLow();
        long fHigh = feature.getHigh();
        int insLoc = getLocation(feature, fLow, fHigh);
        features.insertElementAt(feature, insLoc);
        if (fLow < low) {
            setLow(fLow);
        }
        if (fHigh > high) {
            setHigh(fHigh);
        }
    }

    public int getLocation(SeqFeatureI feature, long low, long high) {
        int bot = 0;
        int top = features.size() - 1;
        while (bot <= top) {
            int mid = (bot + top) / 2;
            SeqFeatureI midFeature = (SeqFeatureI) features.elementAt(mid);
            long midLow = midFeature.getLow();
            int cmp = 0;
            if (midLow > low) {
                cmp = 1;
            } else if (midLow < low) {
                cmp = -1;
            }
            if (cmp == 0) {
                long midHigh = midFeature.getHigh();
                if (midHigh > high) {
                    cmp = 1;
                } else if (midHigh < high) {
                    cmp = -1;
                }
            }
            if (cmp < 0) bot = mid + 1; else if (cmp > 0) top = mid - 1; else return mid;
        }
        return bot;
    }

    public static void main(String[] args) {
        SortedFeatureSet sfs = new SortedFeatureSet();
        sfs.addFeature(new SeqFeature(300, 3000, "test"));
        sfs.addFeature(new SeqFeature(300, 4000, "test"));
        sfs.addFeature(new SeqFeature(300, 5000, "test"));
        sfs.addFeature(new SeqFeature(300, 6000, "test"));
        sfs.addFeature(new SeqFeature(200, 1000, "test"));
        sfs.addFeature(new SeqFeature(100, 1000, "test"));
        sfs.addFeature(new SeqFeature(300, 1000, "test"));
        sfs.addFeature(new SeqFeature(400, 2000, "test"));
        sfs.addFeature(new SeqFeature(300, 2000, "test"));
        sfs.addFeature(new SeqFeature(50, 2000, "test"));
        sfs.addFeature(new SeqFeature(50, 1000, "test"));
        sfs.addFeature(new SeqFeature(300, 7000, "test"));
        sfs.addFeature(new DrawableSeqFeature(new SeqFeature(299, 1000, "test")));
        sfs.addFeature(new DrawableSeqFeature(new SeqFeature(500, 1000, "test")));
        sfs.addFeature(new DrawableSeqFeature(new SeqFeature(99, 1000, "test")));
        sfs.addFeature(new DrawableSeqFeature(new SeqFeature(199, 2000, "test")));
        sfs.addFeature(new DrawableSeqFeature(new SeqFeature(99, 2000, "test")));
        sfs.addFeature(new DrawableSeqFeature(new SeqFeature(98, 2000, "test")));
        sfs.addFeature(new DrawableSeqFeature(new SeqFeature(150, 1000, "test")));
        FeatureSet fs = new FeatureSet();
        fs.addFeature(new SeqFeature(298, 1000, "test"));
        fs.addFeature(new SeqFeature(199, 2000, "test"));
        DrawableFeatureSet dfs = new DrawableFeatureSet(fs);
        sfs.addFeature(dfs);
        for (int i = 0; i < sfs.size(); i++) {
            System.out.println(sfs.getFeatureAt(i).getName() + " low " + sfs.getFeatureAt(i).getLow() + " high " + sfs.getFeatureAt(i).getHigh());
        }
    }
}
