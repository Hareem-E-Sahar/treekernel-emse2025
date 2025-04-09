package uk.org.ogsadai.activity.indexedfile;

import java.lang.reflect.Constructor;
import uk.org.ogsadai.activity.ActivityProcessingException;
import uk.org.ogsadai.activity.ActivityTerminatedException;
import uk.org.ogsadai.activity.ActivityUserException;
import uk.org.ogsadai.activity.MatchedIterativeActivity;
import uk.org.ogsadai.activity.extension.ConfigurableActivity;
import uk.org.ogsadai.activity.io.ActivityInput;
import uk.org.ogsadai.activity.io.ActivityPipeProcessingException;
import uk.org.ogsadai.activity.io.BlockWriter;
import uk.org.ogsadai.activity.io.PipeClosedException;
import uk.org.ogsadai.activity.io.PipeIOException;
import uk.org.ogsadai.activity.io.PipeTerminatedException;
import uk.org.ogsadai.activity.io.TypedOptionalActivityInput;
import uk.org.ogsadai.common.msgs.DAILogger;
import uk.org.ogsadai.config.Key;
import uk.org.ogsadai.config.KeyValueProperties;
import uk.org.ogsadai.config.KeyValueUnknownException;
import uk.org.ogsadai.exception.ErrorID;
import uk.org.ogsadai.extension.ClassInstantiationException;
import uk.org.ogsadai.extension.CreateObjectFromInputValueActivity;

/**
 * An activity that takes as input a string that maps to a class name in the configuration properties of the activity and then 
 * creates the corresponding Object based on the class name.
 * <p>
 * Activity inputs:
 * </p>
 * <ul>
 * <li> <code>mapper</code>. Type: {@link java.lang.String}. The name of a class that 
 * can be used to specify what kind of files are expected to be indexed. This class is expected to be an implementation of 
 * <code>uk.org.ogsadai.activity.indexedfile.FlatFileIndexWriter</code>. Currently, supported types are: 
 * SwissProt and OMIM and as a result the corresponding
 * expected values are "Swiss" and "OMIM". This is a mandatory input. </li>
 * </ul>
 * <p>
 * Activity outputs: 
 * </p>
 * <ul>
 * <li> <code>result</code>. Type: {@link uk.org.ogsadai.activity.indexedfile.FlatFileIndexWriter}. The object that 
 * is created based on the value of the configuration property whose key contains 
 * the input value.</li>
 * </ul>
 * <p>
 * Configuration parameters:
 * </p>
 * <ul>
 * <li><code>mapper.*</code>. This is a mandatory
 * configuration parameter. This must specify the name of a class that
 * implements {@link
 * uk.org.ogsadai.activity.indexedfile.FlatFileIndexWriter} and provides the functionality to
 * index the text. The activity
 * will be parsing the keys starting with <code>mapper.*</code> and will be
 * attempting to find the value of the <code>mapper</code> input as the second
 * token of the key. Then the corresponding value will be extracted.</li>
 * </ul>
 * <p>
 * Activity input/output ordering: none.
 * </p>
 * <p>
 * Activity contracts: none.
 * </p>
 * <p>
 * Target data resource: none.
 * </p>
 * <p>
 * Behaviour:
 * </p>
 * <ul>
 * <li> The activity activity that takes as input a string that maps to a class 
 * name in the configuration properties of the activity and then 
 * creates the corresponding Object based on the class name.
 * <li> The activity has one configuration parameter. The activity 
 * is trying to find keys that contain the provided value of <code>mapper</code> 
 * so that this value will be the classes needed for indexing. 
 * </li>
 * <li>The key that will provide the mapper class are expected to start with "mapper.".
 * The corresponding values are expected to be class names that implement {@link
 * uk.org.ogsadai.activity.indexedfile.FlatFileIndexWriter}.
 * </li>
 * <li>
 * A typical example of how the server-side configuration of the activity could look like is shown below:
 * <pre>
 * mapper.OMIM=uk.org.ogsadai.activity.indexedfile.OMIMIndexWriter
 * mapper.Swiss= uk.org.ogsadai.activity.indexedfile.SwissProtIndexWriter
 * </pre>
 * </li>
 * </ul>
 * 
 * @author The OGSA-DAI Project Team.
 */
public class CreateMapperActivity extends MatchedIterativeActivity implements ConfigurableActivity, CreateObjectFromInputValueActivity {

    /** Copyright statement. */
    private static final String COPYRIGHT_NOTICE = "Copyright (c) The University of Edinburgh, 2007-2009.";

    /** Logger. */
    private static final DAILogger LOG = DAILogger.getLogger(CreateMapperActivity.class);

    /** 
     * Activity input name <code>mapper</code> - the mapper
     * for text indexing.
     * ({@link java.lang.String}).
     */
    public static final String MAPPER_INPUT = "mapper";

    /** 
     * Prefix in order to get the analyzer keys.
     */
    private static final String MAPPER_CLASS_PREFIX = new String("mapper.*");

    /** 
     * Activity output name <code>mapper</code> - The new Mapper object ({@link
     * uk.org.ogsadai.activity.indexedfile.FlatFileIndexWriter}).
     */
    public static final String OUTPUT = "result";

    /** The configuration properties. */
    private KeyValueProperties mProperties;

    /** The output block writer. */
    private BlockWriter mOutput;

    /**
     * {@inheritDoc}
     */
    protected ActivityInput[] getIterationInputs() {
        return new ActivityInput[] { new TypedOptionalActivityInput(MAPPER_INPUT, String.class, "OMIM") };
    }

    /**
     * {@inheritDoc}
     */
    protected void preprocess() throws ActivityUserException, ActivityProcessingException, ActivityTerminatedException {
        validateOutput(OUTPUT);
        mOutput = getOutput();
    }

    /**
     * {@inheritDoc}
     */
    protected void processIteration(Object[] iterationData) throws ActivityProcessingException, ActivityTerminatedException, ActivityUserException {
        final String mapperStr = (String) iterationData[0];
        String mapperClass = null;
        Key[] mapperKeys = mProperties.getKeysRegexp(MAPPER_CLASS_PREFIX);
        if (mapperKeys.length < 1) {
            throw new ActivityProcessingException(new KeyPatternNotExistException(MAPPER_CLASS_PREFIX));
        } else {
            for (int i = 0; i < mapperKeys.length; i++) {
                if (mapperKeys[i].toString().split("\\.")[1].equals(mapperStr)) {
                    mapperClass = (String) mProperties.get(mapperKeys[i]);
                    break;
                }
            }
        }
        if (mapperClass == null) {
            throw new ActivityUserException(ErrorID.KEY_VALUE_UNKNOWN_ERROR, new String[] { mapperClass });
        }
        FlatFileIndexWriter indexHelper = null;
        try {
            indexHelper = (FlatFileIndexWriter) getObject(mapperClass);
            mOutput.write(indexHelper);
        } catch (PipeClosedException e) {
            iterativeStageComplete();
        } catch (PipeIOException e) {
            throw new ActivityPipeProcessingException(e);
        } catch (PipeTerminatedException e) {
            throw new ActivityTerminatedException();
        } catch (Throwable e) {
            throw new ActivityProcessingException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void postprocess() throws ActivityUserException, ActivityProcessingException, ActivityTerminatedException {
    }

    /**
     * {@inheritDoc}
     */
    public void configureActivity(KeyValueProperties properties) throws KeyValueUnknownException {
        mProperties = properties;
    }

    /**
     * {@inheritDoc}
     */
    public Object getObject(String inputClassName) throws ClassInstantiationException {
        FlatFileIndexWriter indexHelper = null;
        try {
            Class analyzerClass = Class.forName(inputClassName);
            Constructor constructor = analyzerClass.getConstructor(new Class[] {});
            indexHelper = (FlatFileIndexWriter) constructor.newInstance(new Object[] {});
        } catch (Throwable e) {
            throw new ClassInstantiationException(inputClassName);
        }
        return indexHelper;
    }
}
