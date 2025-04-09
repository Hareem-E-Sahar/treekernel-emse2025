package org.vardb.sequences;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.vardb.CConstants;
import org.vardb.CVardbException;
import org.vardb.util.CStringHelper;
import org.vardb.util.CTable;

public class CSimpleLocation {

    public static final String DELIMITER = "..";

    private List<SubLocation> sublocations = new ArrayList<SubLocation>();

    public CSimpleLocation() {
    }

    public CSimpleLocation(String location) {
        parse(location);
    }

    public List<SubLocation> getSublocations() {
        return this.sublocations;
    }

    public void setSublocations(final List<SubLocation> sublocations) {
        this.sublocations = sublocations;
    }

    private void parse(String location) {
        if (location.indexOf(';') != -1 || location.indexOf('[') != -1) throw new CVardbException("unexpected delimiters in simple location: " + location);
        for (String sublocation : CStringHelper.split(location, ",")) {
            int index = sublocation.indexOf(DELIMITER);
            if (index == -1) throw new CVardbException("can't parse sublocation: [" + sublocation + "], location=[" + location + "]");
            int start = Integer.parseInt(sublocation.substring(0, index));
            int end = Integer.parseInt(sublocation.substring(index + 2));
            add(start, end);
        }
        sort();
    }

    public void add(SubLocation sublocation) {
        sublocation.setLocation(this);
        this.sublocations.add(sublocation);
    }

    public void add(int start, int end) {
        add(new SubLocation(start, end));
    }

    public int getMin() {
        if (isEmpty()) throw new CVardbException("no sublocations - can't find minumimum");
        return this.sublocations.get(0).getStart();
    }

    public int getMax() {
        if (isEmpty()) throw new CVardbException("no sublocations - can't find maximum");
        return this.sublocations.get(this.sublocations.size() - 1).getEnd();
    }

    public boolean isEmpty() {
        return this.sublocations.isEmpty();
    }

    public String getName() {
        throw new CVardbException("should not be called from a SimpleLocation");
    }

    public int getWidth() {
        int min = getMin();
        int max = getMax();
        if (min > max) throw new CVardbException("can't determine width - min > max: " + toString());
        return max - min;
    }

    public List<Integer> toList() {
        List<Integer> list = new ArrayList<Integer>();
        for (SubLocation sublocation : this.sublocations) {
            for (int position = sublocation.getStart(); position <= sublocation.getEnd(); position++) {
                if (!list.contains(position)) list.add(position);
            }
        }
        Collections.sort(list);
        return list;
    }

    public void sort() {
        Collections.sort(this.sublocations, new SubLocationComparator());
    }

    public CSimpleLocation invert() {
        CSimpleLocation location = new CSimpleLocation();
        int start = 0;
        int end = 0;
        for (SubLocation sublocation : this.sublocations) {
            end = sublocation.getStart() - 1;
            if (start != 0 && end != 0) {
                location.add(start, end);
                start = end = 0;
            }
            start = sublocation.getEnd() + 1;
        }
        return location;
    }

    public String extract(String sequence, int start, CConstants.StrandType strand) {
        if (strand == CConstants.StrandType.reverse) sequence = CStringHelper.reverse(sequence);
        StringBuilder buffer = new StringBuilder();
        for (SubLocation sublocation : getSublocations()) {
            String subseq = sublocation.extract(sequence, start);
            buffer.append(subseq);
        }
        String spliced = buffer.toString();
        if (strand == CConstants.StrandType.reverse) spliced = CStringHelper.reverse(spliced);
        return spliced;
    }

    public CSimpleLocation convertCodons() {
        CSimpleLocation location = new CSimpleLocation();
        for (SubLocation sublocation : this.sublocations) {
            int start = (sublocation.getStart() - 1) * 3;
            int end = (sublocation.getEnd()) * 3;
            location.add(start + 1, end);
        }
        System.out.println("aa location=" + toString());
        System.out.println("nt location=" + location.toString());
        return location;
    }

    public static String convertLocationRun(String sequence, String symbol) {
        String regex = symbol + "+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sequence);
        List<String> ranges = new ArrayList<String>();
        while (matcher.find()) {
            int start = matcher.start() + 1;
            int end = matcher.end();
            ranges.add(start + DELIMITER + end);
        }
        return CStringHelper.join(ranges, ",");
    }

    public static String normalizeLocation(String str) {
        CSimpleLocation oldlocation = new CSimpleLocation(str);
        CSimpleLocation newlocation = new CSimpleLocation();
        int offset = oldlocation.getMin() - 1;
        for (SubLocation subloc : oldlocation.getSublocations()) {
            newlocation.add(subloc.getStart() - offset, subloc.getEnd() - offset);
        }
        return newlocation.toString();
    }

    public String toString() {
        List<String> sublocations = new ArrayList<String>();
        for (SubLocation sublocation : this.sublocations) {
            sublocations.add(sublocation.toString());
        }
        return CStringHelper.join(sublocations, ",");
    }

    public String applyToUngappedSequence(String sequence) {
        List<String> parts = new ArrayList<String>();
        for (CSimpleLocation.SubLocation sublocation : this.sublocations) {
            int start = convertPosition(sequence, sublocation.getStart());
            int end = convertPosition(sequence, sublocation.getEnd());
            if (end - start == 0) continue;
            parts.add(start + DELIMITER + end);
        }
        return CStringHelper.join(parts, ",");
    }

    private int convertPosition(String sequence, int position) {
        int index = position - 1;
        String substr = sequence.substring(0, index);
        substr = substr.replaceAll(CConstants.GAP, "");
        return substr.length() + 1;
    }

    public static CTable applyToUngappedSequences(Map<String, String> sequences, CSimpleLocation location, String name) {
        CTable table = new CTable();
        table.getHeader().add("SEQUENCE");
        table.getHeader().add(name);
        for (String accession : sequences.keySet()) {
            String sequence = sequences.get(accession);
            String locstr = location.applyToUngappedSequence(sequence);
            if (CStringHelper.isEmpty(locstr)) continue;
            CTable.Row row = table.addRow();
            row.add(accession);
            row.add(locstr);
        }
        return table;
    }

    public static class SubLocation {

        private Integer start;

        private Integer end;

        private CSimpleLocation location;

        public SubLocation() {
        }

        public SubLocation(int start, int end) {
            if (start > end) {
                int temp = end;
                end = start;
                start = temp;
            }
            this.start = start;
            this.end = end;
        }

        public Integer getStart() {
            return this.start;
        }

        public void setStart(Integer start) {
            this.start = start;
        }

        public Integer getEnd() {
            return this.end;
        }

        public void setEnd(Integer end) {
            this.end = end;
        }

        public CSimpleLocation getLocation() {
            return this.location;
        }

        public void setLocation(final CSimpleLocation location) {
            this.location = location;
        }

        public String extract(String sequence) {
            return extract(sequence, this.start);
        }

        public String extract(String sequence, int contig_start) {
            return sequence.substring(this.start - contig_start, this.end - contig_start + 1);
        }

        public int getWidth() {
            return this.end - this.start;
        }

        public String toString() {
            return this.start + DELIMITER + this.end;
        }
    }

    @SuppressWarnings("serial")
    public static class SubLocationComparator implements Comparator<SubLocation>, Serializable {

        public int compare(SubLocation l1, SubLocation l2) {
            Integer start1 = l1.getStart();
            Integer start2 = l2.getStart();
            return start1.compareTo(start2);
        }
    }

    @SuppressWarnings("serial")
    public static class LocationComparator implements Comparator<CSimpleLocation>, Serializable {

        public int compare(CSimpleLocation l1, CSimpleLocation l2) {
            Integer start1 = l1.getSublocations().get(0).getStart();
            Integer start2 = l2.getSublocations().get(0).getStart();
            return start1.compareTo(start2);
        }
    }
}
