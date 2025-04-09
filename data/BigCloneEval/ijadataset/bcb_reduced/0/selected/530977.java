package edu.uwm.nlp.jude.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author qing
 *
 * Jun 16, 2009
 */
public class FullText {

    private String fullText;

    private int largest;

    private int fullRefPtnCode;

    private ArrayList<ReferenceEntity> rEntList;

    public FullText(String fullText, int largest) {
        this.fullText = fullText;
        this.largest = largest;
    }

    public FullText(String fullText, int fullRefPtnCode, ArrayList<ReferenceEntity> rEntList) {
        this.fullText = fullText;
        this.fullRefPtnCode = fullRefPtnCode;
        this.rEntList = rEntList;
        largest = rEntList.size();
    }

    public ArrayList<ReferenceEntity> doSpot(boolean useFeedback) {
        ArrayList<ReferenceEntity> res = null;
        if (fullRefPtnCode == IFullRefFormats.BRANCKET_INT || fullRefPtnCode == IFullRefFormats.INT_DOT || fullRefPtnCode == IFullRefFormats.INT_ONLY || fullRefPtnCode == IFullRefFormats.PRANPH_INT || fullRefPtnCode == IFullRefFormats.INT_HALF_PRANTH) {
            int fullTextPtnCode = detectPtn(1.5f);
            NumberFullTextSpot numSpot = new NumberFullTextSpot(fullText, rEntList);
            if (fullTextPtnCode == IFullTextFormats.BRACKET_INT) res = numSpot.extractSpot(IFullTextFormats.BRACKET_INT_REGEX, ']'); else if (fullTextPtnCode == IFullTextFormats.NAKED_INT) res = numSpot.extractSpot(IFullTextFormats.NAKED_INT_REGEX, '\n'); else if (fullTextPtnCode == IFullTextFormats.PRATH_INT) res = numSpot.extractSpot(IFullTextFormats.PRATH_INT_REGEX, ')');
            System.out.println("spl size=" + numSpot.getSuplList().size());
            if (res != null && useFeedback) {
                Feedbacker fb = new Feedbacker(numSpot.getSuplList(), res, fullRefPtnCode);
                res = fb.refindSplit(10);
            }
        } else if (fullRefPtnCode == IFullRefFormats.AUTHOR) {
            AuthorFullTextSpot authorSpot = new AuthorFullTextSpot(fullText, rEntList);
            res = authorSpot.extractSpot();
        } else if (fullRefPtnCode == IFullRefFormats.BRANCKET_AUTHOR) {
            AuthorFullTextSpot authorSpot = new AuthorFullTextSpot(fullText, rEntList);
            res = authorSpot.extractBracketAuthorSpot();
        }
        if (res != null) System.out.println("total split=" + res.size());
        return res;
    }

    public int detectPtn(float ext) {
        int ftPtnCode = -1;
        int total = fullText.length();
        int absIdx = fullText.indexOf("Abstract\n");
        if (absIdx == -1) absIdx = 0;
        String passage = fullText.substring(absIdx, total / 3);
        int bracketPtnCount = countOccurence(passage, IFullTextFormats.BRACKET_INT_REGEX, ']', largest, ext);
        int nakedPtnCount = countOccurence(passage, IFullTextFormats.NAKED_INT_REGEX, '\n', largest, ext);
        int praphPtnCount = countOccurence(passage, IFullTextFormats.PRATH_INT_REGEX, ')', largest, ext);
        System.out.println("brackCount=" + bracketPtnCount + ", nakedCount=" + nakedPtnCount + ", parphCount=" + praphPtnCount);
        if (bracketPtnCount >= nakedPtnCount && bracketPtnCount >= praphPtnCount) ftPtnCode = IFullTextFormats.BRACKET_INT; else if (nakedPtnCount > bracketPtnCount && nakedPtnCount > praphPtnCount) ftPtnCode = IFullTextFormats.NAKED_INT; else if (praphPtnCount > bracketPtnCount && praphPtnCount >= nakedPtnCount) ftPtnCode = IFullTextFormats.PRATH_INT;
        return ftPtnCode;
    }

    public int countOccurence(String passage, String regex, char rcoat, int largest, float ext) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(passage);
        int openIdx = 0;
        int closeIdx;
        int fullTextLength = passage.length();
        int count = 0;
        while (m.find(openIdx) && m.start() < fullTextLength) {
            closeIdx = passage.indexOf(rcoat, m.start() + 1);
            if (closeIdx == -1 || m.start() == -1 || m.start() + 1 > closeIdx) break;
            String fieldCand = passage.substring(m.start() + 1, closeIdx);
            ArrayList<Integer> idxList = NumberFullTextSpot.checkField(fieldCand, m.start(), m.end(), fullText);
            if (idxList.size() != 0) {
                Collections.sort(idxList);
                if (idxList.get(idxList.size() - 1) <= largest * ext) count++;
            }
            openIdx = closeIdx;
        }
        return count;
    }
}
