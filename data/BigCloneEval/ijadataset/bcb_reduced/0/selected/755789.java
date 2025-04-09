package bioweka.filters.sequence.alignments;

import bioweka.aligners.Aligner;
import bioweka.core.BioWekaUtils;
import bioweka.core.inspection.Report;
import bioweka.core.sequence.SequenceProperty;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;

/**
 * Alignment scorer that precomputes the alignment scores for a classification
 * of sequences based on alignments. This filter performs an all-against-all
 * alignment and returns for each input sequence the scores of the alignments
 * against all input sequences. 
 * @author <a href="mailto:Martin.Szugat@GMX.net">Martin Szugat</a>
 * @version $Revision: 1.2 $
 * @see bioweka.classifiers.sequence.alignments.AlignmentScoreClassifier
 */
public class AlignmentScorer extends AbstractAlignmentFilter {

    /**
     * The unique class identifier.
     */
    private static final long serialVersionUID = 3905799786216110133L;

    /**
     * The description of the alignment scorer component.
     */
    public static final String ALIGNMENT_SCORER_GLOBAL_INFO = "Alignment scorer that precomputes the alignment scores for a " + "classification of sequences based on alignments. This filter " + "performs an all-against-all alignment and returns for each input " + "sequence the scores of the alignments against all input sequences.";

    /**
     * The name of the sequence alignment index attribute.
     */
    public static final String ATTR_NAME_SEQUENCE_ALIGNMENT_INDEX = "sequence.alignment.index";

    /**
     * The base name for the sequence alignment score attributes. 
     */
    public static final String ATTR_NAME_SEQUENCE_ALIGNMENT_SCORE = "sequence.alignment.score.";

    /**
     * The description of the symmetric alignment property. 
     */
    public static final String SYMMETRIC_ALIGNMENT_TIP_TEXT = "If set the alignment scorer assumes that the specified aligner " + "performs a symmetrical alignment, that is it makes no difference " + "if sequence A is aligned against sequence B or vice versa. It uses " + "this information to minimize the number of required alignments from " + "n^2 to n^2/2 where n is the number of sequences/instances.";

    /**
     * The option flag to set the symmetric alignment property on command line. 
     */
    public static final String SYMMETRIC_ALIGNMENT_OPTION_FLAG = "X";

    /**
     * The name of the symmetric alignment property. 
     */
    public static final String SYMMETRIC_ALIGNMENT_PROPERTY_NAME = "symmetricAlignment";

    /**
     * The default value for the symmetric alignment property.
     */
    public static final boolean SYMMETRIC_ALIGNMENT_DEFAULT_VALUE = false;

    /**
     * Indicates if the aligner is a symmetric aligner or if the all-against-all 
     * alignment should be performed symmetricly.
     */
    private boolean symmetricAlignment = SYMMETRIC_ALIGNMENT_DEFAULT_VALUE;

    /**
     * Initializes the alignment scorer.
     */
    public AlignmentScorer() {
        super();
    }

    /**
     * Indicates if the aligner performs a symmetrical alignment.
     * @return <code>true</code> if the aligner is symmetrical, 
     * <code>false</code> otherwise
     */
    public final boolean getSymmetricAlignment() {
        return symmetricAlignment;
    }

    /**
     * Specifies if the aligner performs a symmetrical alignment.
     * @param symmetricAlignment <code>true</code> if the aligner is symmetrical
     * , <code>false</code> otherwise
     */
    public final void setSymmetricAlignment(boolean symmetricAlignment) {
        getDebugger().config(SYMMETRIC_ALIGNMENT_PROPERTY_NAME, Boolean.toString(symmetricAlignment));
        this.symmetricAlignment = symmetricAlignment;
    }

    /**
     * Returns the description of the symmetrical alignment property.
     * @return a human readable text
     */
    public final String symmetricAlignmentTipText() {
        return SYMMETRIC_ALIGNMENT_TIP_TEXT;
    }

    /**
     * {@inheritDoc}
     */
    public String globalInfo() {
        return ALIGNMENT_SCORER_GLOBAL_INFO;
    }

    /**
     * {@inheritDoc}
     */
    public boolean batchFinished() throws Exception {
        Instances input = getInputFormat();
        SequenceProperty sequenceProperty = sequenceProperty();
        Aligner aligner = getAligner();
        String relationName = BioWekaUtils.makeRelationName(input.relationName(), aligner);
        int numInstances = input.numInstances();
        int numAttributes = input.numAttributes() + 1 + numInstances;
        FastVector newAttributes = new FastVector(numAttributes);
        copyAttributes(newAttributes);
        newAttributes.addElement(new Attribute(ATTR_NAME_SEQUENCE_ALIGNMENT_INDEX));
        for (int i = 0; i < numInstances; i++) {
            newAttributes.addElement(new Attribute(ATTR_NAME_SEQUENCE_ALIGNMENT_SCORE + Integer.toString(i)));
        }
        Instances newInstances = new Instances(relationName, newAttributes, numInstances);
        newInstances.setClassIndex(input.classIndex());
        setOutputFormat(newInstances);
        double[][] scores = new double[numInstances][numInstances];
        for (int i = 0; i < numInstances; i++) {
            Instance target = input.instance(i);
            double[] values = target.toDoubleArray();
            double[] newValues = new double[numAttributes];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = i;
            String targetSeq = sequenceProperty.getSequence(target);
            for (int j = 0; j < numInstances; j++) {
                Instance template = input.instance(j);
                String templateSeq = sequenceProperty.getSequence(template);
                if (symmetricAlignment && j < i) {
                    scores[i][j] = scores[j][i];
                } else {
                    scores[i][j] = aligner.align(targetSeq, templateSeq);
                }
                newValues[values.length + 1 + j] = scores[i][j];
            }
            push(target, newValues);
        }
        return (numPendingOutput() > 0);
    }

    /**
     * {@inheritDoc}
     */
    public void inspect(Report report) throws NullPointerException {
        super.inspect(report);
        report.append(SYMMETRIC_ALIGNMENT_PROPERTY_NAME, Boolean.toString(symmetricAlignment));
    }

    /**
     * {@inheritDoc}
     */
    public void getOptions(FastVector options) throws NullPointerException {
        super.getOptions(options);
        if (symmetricAlignment != SYMMETRIC_ALIGNMENT_DEFAULT_VALUE) {
            options.addElement("-" + SYMMETRIC_ALIGNMENT_OPTION_FLAG);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void listOptions(FastVector options) throws NullPointerException {
        super.listOptions(options);
        options.addElement(new Option(BioWekaUtils.formatDescription(SYMMETRIC_ALIGNMENT_TIP_TEXT, Boolean.toString(SYMMETRIC_ALIGNMENT_DEFAULT_VALUE)), SYMMETRIC_ALIGNMENT_OPTION_FLAG, 0, "-" + SYMMETRIC_ALIGNMENT_OPTION_FLAG));
    }

    /**
     * {@inheritDoc}
     */
    public void setOptions(String[] options) throws Exception {
        super.setOptions(options);
        setSymmetricAlignment(Utils.getFlag(SYMMETRIC_ALIGNMENT_OPTION_FLAG, options));
    }
}
