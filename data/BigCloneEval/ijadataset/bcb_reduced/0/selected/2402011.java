package edu.uwm.nlp.jude.internal;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author qing
 *
 * Jun 18, 2009
 */
public class Feedbacker {

    public ArrayList<ReferenceEntity> suplementList;

    public ArrayList<ReferenceEntity> rEntList;

    public int fcPtnCode;

    public Feedbacker(ArrayList<ReferenceEntity> suplementList, ArrayList<ReferenceEntity> rEntList, int fcPtnCode) {
        this.suplementList = suplementList;
        this.rEntList = rEntList;
        this.fcPtnCode = fcPtnCode;
    }

    public ArrayList<ReferenceEntity> refindSplit(int offset) {
        String id;
        String regex;
        Pattern p;
        Matcher m;
        String fc;
        ArrayList<ReferenceEntity> found;
        int i = 0;
        for (ReferenceEntity suplEnt : suplementList) {
            id = suplEnt.getIdInRef();
            regex = reformIndexRegex(id);
            p = Pattern.compile(regex);
            found = new ArrayList<ReferenceEntity>();
            if (regex != null) {
                for (ReferenceEntity entity : rEntList) {
                    fc = entity.getFullRef();
                    m = p.matcher(fc);
                    i = 0;
                    while (m.find(i) && (m.end() < fc.length())) {
                        if (checkContext(fc, m.start(), m.end(), offset)) {
                            suplEnt.setFullRef(fc.substring(m.end()));
                            found.add(suplEnt);
                            entity.setFullRef(fc.substring(0, m.start()));
                            break;
                        } else i = m.end();
                    }
                }
                rEntList.addAll(found);
            }
        }
        return rEntList;
    }

    public String reformIndexRegex(String id) {
        String regex = null;
        if (fcPtnCode == IFullRefFormats.BRANCKET_INT) regex = "\\[[\\s]*" + id + "[\\s]*\\]"; else if (fcPtnCode == IFullRefFormats.INT_DOT) regex = id + "[\\s]*[.]"; else if (fcPtnCode == IFullRefFormats.INT_ONLY) regex = id + "[\\s]*"; else if (fcPtnCode == IFullRefFormats.INT_HALF_PRANTH) regex = id + "[\\s]*\\)";
        return regex;
    }

    public boolean checkContext(String fc, int start, int end, int offset) {
        if (start == 0) return false;
        String after = fc.substring(end, (end + offset < fc.length()) ? (end + offset) : fc.length());
        char ch = fc.charAt(start - 1);
        return after.matches("[\\s]*[A-Z][A-Za-z\\s\\-.,]*") && (ch < '0' || ch > '9');
    }
}
