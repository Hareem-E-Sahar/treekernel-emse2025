package edu.uwm.nlp.jude.internal;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.uwm.nlp.jude.util.StringUtil;

/**
 * @author qing
 *
 * Jun 16, 2009
 */
public class NumberFullTextSpot {

    private String fullText;

    private ArrayList<ReferenceEntity> rEntList;

    private ArrayList<ReferenceEntity> suplList;

    private int spotCount = 0;

    public int getSpotCount() {
        return spotCount;
    }

    public NumberFullTextSpot(String fullText, ArrayList<ReferenceEntity> rEntList) {
        this.fullText = fullText;
        this.rEntList = rEntList;
    }

    public void testFunc(String regex, char rcoat) {
        this.extractSpot(regex, rcoat);
    }

    public ArrayList<ReferenceEntity> extractSpot(String regex, char rcoat) {
        spotCount = 0;
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(fullText);
        int openIdx = 0;
        int closeIdx;
        int fullTextLength = fullText.length();
        ArrayList<Integer> idList = new ArrayList<Integer>();
        suplList = new ArrayList<ReferenceEntity>();
        while (m.find(openIdx) && m.start() < fullTextLength) {
            closeIdx = fullText.indexOf(rcoat, m.start() + 1);
            if (closeIdx == -1 || m.start() == -1 || m.start() + 1 > closeIdx) break;
            String fieldCand = fullText.substring(m.start() + 1, closeIdx);
            if (!fieldCand.equals("")) idList = checkField(fieldCand, m.start(), m.end(), fullText);
            String context = StringUtil.getContext(fullText, m.start(), m.end(), 15);
            if (idList.size() != 0) {
                spotCount += idList.size();
                for (Integer num : idList) {
                    boolean flag = false;
                    for (ReferenceEntity entity : rEntList) {
                        if (entity.getIdInRef().equals(String.valueOf(num))) {
                            entity.getContextList().add(context);
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) addToSuplList(num, context);
                }
            } else System.err.println("No referece for the field : " + fieldCand);
            openIdx = closeIdx;
        }
        System.out.println("\nspotCount=" + spotCount);
        return rEntList;
    }

    /**
	 * get the numbers in [ and ]. 
	 * @param field the string including [ and ]
	 * @param start TODO
	 * @param end TODO
	 * @param text TODO
	 * @return numbers
	 */
    public static ArrayList<Integer> checkField(String field, int start, int end, String text) {
        ArrayList<Integer> res = new ArrayList<Integer>();
        String nakeStr = StringUtil.replaceHyphen8211(field);
        nakeStr = StringUtil.removeEscp(nakeStr);
        if (!advancedPtnCheck(nakeStr) || StringUtil.isInNumberPool(text, start, end, 3) || StringUtil.isInFormula(text, start, 4)) return res;
        String[] numStrs = nakeStr.split(",");
        for (int i = 0; i < numStrs.length; i++) {
            res.addAll(getHyphenNums(numStrs[i].trim()));
        }
        return res;
    }

    public static ArrayList<Integer> checkField(String field) {
        ArrayList<Integer> res = new ArrayList<Integer>();
        String nakeStr = StringUtil.replaceHyphen8211(field);
        nakeStr = StringUtil.removeEscp(nakeStr);
        if (!advancedPtnCheck(nakeStr)) return res;
        String[] numStrs = nakeStr.split(",");
        for (int i = 0; i < numStrs.length; i++) {
            res.addAll(getHyphenNums(numStrs[i].trim()));
        }
        return res;
    }

    private static boolean advancedPtnCheck(String str) {
        return str.trim().matches("[1-9]|([1-9][0-9\\,\\-\\s]*[0-9])");
    }

    public static ArrayList<Integer> getHyphenNums(String hyStr) {
        String[] strs = hyStr.split("-");
        ArrayList<Integer> res = new ArrayList<Integer>();
        if (hyStr.equals("")) return res;
        for (int i = 0; i < strs.length; i++) if (StringUtil.hasSpace(strs[i]) || !strs[i].matches("[1-9][0-9]*")) return res;
        if (strs.length == 1 && Double.parseDouble(strs[0].trim()) < 1000) {
            res.add(Integer.parseInt(strs[0].trim()));
        } else if (strs.length == 2) {
            int start = Integer.parseInt(strs[0]);
            int end = Integer.parseInt(strs[1]);
            while (start <= end) {
                res.add(start);
                start++;
            }
        }
        return res;
    }

    public void addToSuplList(int num, String context) {
        for (ReferenceEntity ent : suplList) {
            if (ent.getIdInRef().equals(String.valueOf(num))) {
                ent.getContextList().add(context);
                return;
            }
        }
        ReferenceEntity e = new ReferenceEntity();
        e.setIdInRef(String.valueOf(num));
        e.getContextList().add(context);
        suplList.add(e);
    }

    public ArrayList<ReferenceEntity> getSuplList() {
        return suplList;
    }

    public void setSuplList(ArrayList<ReferenceEntity> suplList) {
        this.suplList = suplList;
    }
}
