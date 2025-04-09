package org.vardb.motifs;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.vardb.CConstants;
import org.vardb.CVardbException;
import org.vardb.sequences.CSequenceType;
import org.vardb.sequences.CSimpleLocation;
import org.vardb.sequences.dao.CSequenceView;

public final class CRegexFinder {

    private CRegexFinder() {
    }

    public static CRegexResults search(List<CSequenceView> sequences, String pattern, CConstants.RegexType regexType, CSequenceType sequenceType) {
        return search(sequences, Collections.singletonList(pattern), regexType, sequenceType);
    }

    public static CRegexResults search(List<CSequenceView> sequences, List<String> patterns, CConstants.RegexType regexType, CSequenceType sequenceType) {
        CRegexResults results = new CRegexResults();
        for (String pattern : patterns) {
            CRegexSearchParams params = new CRegexSearchParams(pattern, regexType, sequenceType);
            search(sequences, params, results);
        }
        return results;
    }

    private static void search(List<CSequenceView> sequences, CRegexSearchParams params, CRegexResults results) {
        if (params.getRegexType() == CConstants.RegexType.PSSM) {
            searchPssm(sequences, params, results);
            return;
        }
        CRegexResults.Regex regex = results.addRegex(params.getQuery(), parsePattern(params), params.getRegexType());
        results.addRegex(regex);
        Pattern pat = Pattern.compile(regex.getRegex());
        for (CSequenceView seq : sequences) {
            String str = seq.getSequence(params.getSequenceType());
            if (str == null) continue;
            CRegexResults.Sequence sequence = results.addSequence(seq);
            Matcher matcher = pat.matcher(str);
            while (matcher.find()) {
                CRegexResults.Match match = regex.addMatch(sequence);
                match.setStart(matcher.start());
                match.setEnd(matcher.end());
                match.setMatch(matcher.group());
            }
        }
    }

    private static void searchPssm(List<CSequenceView> sequences, CRegexSearchParams params, CRegexResults results) {
        CRegexResults.Regex regex = results.addRegex("PSSM", parsePattern(params), params.getRegexType());
        results.addRegex(regex);
        CWeightMatrix matrix = new CWeightMatrix(params);
        for (CSequenceView seq : sequences) {
            String str = seq.getSequence(params.getSequenceType());
            if (str == null) continue;
            CRegexResults.Sequence sequence = results.addSequence(seq);
            CSimpleLocation location = matrix.getMatches(str);
            for (CSimpleLocation.SubLocation sublocation : location.getSublocations()) {
                CRegexResults.Match match = regex.addMatch(sequence);
                match.setStart(sublocation.getStart());
                match.setEnd(sublocation.getEnd());
                match.setMatch(str.substring(sublocation.getStart(), sublocation.getEnd()));
            }
        }
    }

    public static String parsePattern(CRegexSearchParams params) {
        String pattern = params.getQuery();
        switch(params.getRegexType()) {
            case REGEX:
                return pattern;
            case PROSITE:
                return CPrositeQueryParser.parse(pattern);
            case AA:
                return CAminoAcidRegexParser.parse(pattern);
            case NT:
                return CDnaRegexParser.parse(pattern);
            case GAG:
                return CGlycanBindingMotifFinder.createRegex(pattern);
            case PSSM:
                return CWeightMatrix.createRegex(params.getSequenceType(), pattern);
            default:
                throw new CVardbException("RegexType not recognized: " + params.getRegexType());
        }
    }
}
