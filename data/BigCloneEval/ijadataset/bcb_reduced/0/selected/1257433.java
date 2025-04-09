package edu.miami.cs.research.apg.generator.search.representations.criteria;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import edu.miami.cs.research.apg.agregator.ontology.SongTrack.FieldNames;
import edu.miami.cs.research.apg.generator.search.representations.CombinationGenerator;
import edu.miami.cs.research.apg.generator.search.representations.FieldType;
import edu.miami.cs.research.apg.generator.search.representations.PlaylistNode;

/**
 * @author Darrius Serrant
 *
 */
public class PairsGlobalConstraint extends GlobalConstraint {

    ArrayList<BinaryConstraint> binaryConstraints;

    private int startIndex;

    private int endIndex;

    private String type;

    private HashSet<Object> domainValues;

    /**
	 * @param field
	 * @param type
	 * @param constraints
	 */
    public PairsGlobalConstraint(FieldNames field, FieldType type, int startIndex, int endIndex, Class<?> constraintType, HashSet<Object> values) {
        super(field, type, startIndex, endIndex, 0, 0, values);
        this.setStartIndex(startIndex);
        this.setEndIndex(endIndex);
        this.setType(constraintType.getCanonicalName());
        this.setDomainValues(values);
        ArrayList<Integer> set = new ArrayList<Integer>();
        for (int a = startIndex; a <= endIndex; a++) {
            set.add(a);
        }
        CombinationGenerator<Integer> combinationGenerator = new CombinationGenerator<Integer>(set, 2);
        binaryConstraints = new ArrayList<BinaryConstraint>();
        try {
            Constructor<?> constraintConstruct = constraintType.getConstructor(FieldNames.class, FieldType.class, Integer.class, Integer.class, HashSet.class);
            while (combinationGenerator.hasNext()) {
                List<Integer> comb = combinationGenerator.next();
                binaryConstraints.add((BinaryConstraint) constraintConstruct.newInstance(field, type, comb.get(0), comb.get(1), values));
            }
            binaryConstraints.trimToSize();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public String getType() {
        return type;
    }

    public void setType(String string) {
        this.type = string;
    }

    public HashSet<Object> getDomainValues() {
        return domainValues;
    }

    public void setDomainValues(HashSet<Object> domainValues) {
        this.domainValues = domainValues;
    }

    @Override
    public double calculatePenalty(PlaylistNode value) {
        double score = 0;
        for (BinaryConstraint constraint : binaryConstraints) {
            score += constraint.calculatePenalty(value);
        }
        score /= (binaryConstraints.size());
        return score;
    }
}
