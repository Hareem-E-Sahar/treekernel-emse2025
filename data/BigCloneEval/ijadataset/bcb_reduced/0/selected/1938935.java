package edu.dimacs.mms.boxer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Vector;
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/** The abstract parent of Learning Algorithm classes. Contains the
 * common API, as well as the shared auxiliary methods.

 A Learner is set up to classify data points with respect to
 multiple classifications (called  "{@link edu.dimacs.mms.boxer.Discrimination
 Discriminations}"). Categoryzing data points with respect to each
 Discrimination will be carried out mathematically independently from
 other Discirminations, but the algorithm may use of common data
 structures etc. to provide extra efficiency as compared to processing
 each Discrimination completely separately. This multi-discrimination
 efficiency should be especially prominent in the case of algorithms
 such as kNN.

 <p>
 There are two ways for a BOXER application to create a
 learner. One can deserialize an entire "learner complex" (one or
 more learners, complete with the suite they are using) that has been
 serialized on an earlier run, using
 Learner.deserializeLearnerComplex(Element e) (or its other flavors).
 One can also create a single learner using a suite with
 Suite.addLearner().

 */
public abstract class Learner implements Model {

    final Double Zero = new Double(0);

    /** Contains all data and methods pertaining to a classifier for a
     * single discrimination
     */
    public abstract class LearnerBlock implements Measurable {

        /** Discrimination associated with this learner block */
        Discrimination dis;

        /** This flag is set to true in the block corresponding to the
	    fallback discrimination. That block can't be updated other
	    than in a "default" fashion
	*/
        boolean isFallback = false;

        void setIsFallback() {
            isFallback = true;
        }

        /** Returns true if this learning block contains no
	non-trivial information obtained from learning.
	*/
        abstract boolean isZero();

        /** Estimates probabilities of a given data point's belonging to
	various classes in this block's discrimination.

	@param p DataPoint to score

	@return double[], an array of probabilities for all classes of
	the specified discriminations. 
	*/
        public abstract double[] applyModel(DataPoint p);

        /** Computes logarithms of the scores returned by
	    applyModel. May be redefined by child classes for better
	    precision (preventing underflow) */
        public double[] applyModelLog(DataPoint p) {
            double[] q = applyModel(p);
            double[] qlog = new double[q.length];
            for (int i = 0; i < q.length; i++) qlog[i] = Math.log(q[i]);
            return qlog;
        }

        /** This method is called by Learner.absorbExample() method,
	    in order to ensure that we don't update the learner, and
	    in particular, the fallback discrimination's learner block
	    inappropriately. Currently, this method does two things:
	    <ul> 
	    
	    <li>Ensures that we won't update the fallback
	    discrimination's learner in a non-default way.

	    <li> Verifies that the learner can handle the
	    discrimination's structure (.e.g, reports error for DCS3)

	    </ul> <p>

	    @throws IllegalArgumentException If it is not appropriate
	    to update the learner with the specified examples.
	*/
        void validateExamples(Vector<DataPoint> xvec, int i1, int i2) {
            if (dis.dcs == Suite.DCS.Uncommitted) {
                throw new AssertionError("How come I have not commiitted discr before training?");
            } else if (dis.dcs == Suite.DCS.Fixed || dis.dcs == Suite.DCS.Bounded) {
            } else if (dis.dcs == Suite.DCS.Unbounded) {
                throw new UnsupportedOperationException("No learner currently supported discrimination class structure DCS3, a.k.a. " + dis.dcs);
            } else {
                throw new AssertionError("Unknonw DCS type: " + dis.dcs);
            }
            if (!isFallback) return;
            for (int i = i1; i < i2; i++) {
                DataPoint x = xvec.elementAt(i);
                Discrimination.Cla labC = x.claForDisc(dis);
                if (labC != dis.getDefaultCla()) {
                    throw new IllegalArgumentException("DataPoint " + x + " carries a non-default class label " + labC + " for the fallback discrimination, which is prohibited");
                }
            }
        }

        /** Training for this particular block */
        public abstract void absorbExample(Vector<DataPoint> xvec, int i1, int i2) throws BoxerException;

        /** Reads in the data - parameters and the classifier's state
	    - pertaining to this particular discrimination
	 */
        abstract void parseDisc(Element e) throws BoxerXMLException;

        /** Saves the data - parameters and the classifier's state -
	    pertaining to this particular discrimination
	 */
        public abstract Element saveAsXML(Document xmldoc);

        /** Prints a human-readable description of the classifier for
	  this particular discrimination. The method in the root class
	  prints nothing; it should be overriden by concrete classes
	  as needed
	  @param verbose If true, print a lot more data, including the
	  full content of coefficient matrices
	*/
        void describe(PrintWriter out, boolean verbose) {
        }
    }

    ;

    /** Classifiers for individual discriminations */
    LearnerBlock[] blocks;

    String name;

    /** The name of this learner (for reporting purposes). It is
     * distinct from the name of the algorithm implemented by the
     * learner */
    public String getName() {
        return name;
    }

    /** The suite of discriminations into which we classify. Besides
	this learnber, there may be other learners using the same Suite
	as well. */
    Suite suite;

    /** Gets the {@link edu.dimacs.mms.boxer.Suite suite} associated with this
	learning algorithm */
    public Suite getSuite() {
        return suite;
    }

    /** Applies the classifiers to the example p and returns the
	probabilities of membership in different classes.

	@param p An example to apply the model to
	@return An array of the probabilities of membership in
	different classes. For details, see {@link Model#applyModel( DataPoint p)}

     */
    public double[][] applyModel(DataPoint p) {
        double[][] s = new double[blocks.length][];
        for (int did = 0; did < s.length; did++) s[did] = applyModel(p, did);
        return s;
    }

    /** Similar to {@link #applyModel( DataPoint p)}, but returns
     * <em>logarithms</em> of probabilities, rather than probabilities
     * themselves. This is useful when one want to look at the
     * smallest values, and to prevent digital underflow from making
     * them look like zeros. 
     */
    public double[][] applyModelLog(DataPoint p) {
        double[][] s = new double[blocks.length][];
        for (int did = 0; did < s.length; did++) {
            s[did] = blocks[did].applyModelLog(p);
        }
        return s;
    }

    /** Applies the model for a particular discrimination to all data
       point in a given Vector section. This is implemented here as a
       wrapper over a single-datapoint method, but may be overriden by
       a learner such as BXRLearner. */
    public double[][] applyModelLog(Vector<DataPoint> v, int i0, int i1, int did) throws BoxerException {
        double[][] s = new double[i1 - i0][];
        for (int i = i0; i < i1; i++) {
            s[i - i0] = blocks[did].applyModelLog(v.elementAt(i));
        }
        return s;
    }

    public double[][] applyModel(Vector<DataPoint> v, int i0, int i1, int did) throws BoxerException {
        double[][] s = new double[i1 - i0][];
        for (int i = i0; i < i1; i++) {
            s[i - i0] = blocks[did].applyModel(v.elementAt(i));
        }
        return s;
    }

    /** Estimates probabilities of a given data point's belonging to
	various classes of a specified discrimination.

	@param p DataPoint to score
	@param did Discrimination id

	@return double[], an array of probabilities for all classes of
	the discrimination in question. It will be aligned with
	Discrimination.classes of the selected discrimination */
    public final double[] applyModel(DataPoint p, int did) {
        return blocks[did].applyModel(p);
    }

    /** Incrementally trains this classifier on data points xvec[i],
     * i1&le;i&lt;i2. 

     A particular algorithm may process these vectors in a purely
     sequential ("online") way (i.e., when it performs training on
     xvec[i1], it makes use of no knowledge whatsoever about xvec[j]
     with j&gt;i), or it may make use of all (i2-i1) training data
     points at once (e.g., by computing certain aggregate properties
     of the set first). Thus if you desire to carry out training in a
     "purely online" fashion, one data point at time, you may want to
     invoke this method for one data point at a time.

     This is the main absorbExample() method; other methods of the
     same name are just "syntactic sugar" for it.

     @param xvec A vector of data points for training. Only a section of this vector (from i1 thru i2-1) will be used.

     // FIXME should throw BoxerException

     */
    public void absorbExample(Vector<DataPoint> xvec, int i1, int i2) {
        createMissingBlocks();
        for (LearnerBlock block : blocks) {
            block.dis.ensureCommitted();
            block.validateExamples(xvec, i1, i2);
            try {
                block.absorbExample(xvec, i1, i2);
            } catch (BoxerException ex) {
                Logging.error(ex.getMessage());
                throw new AssertionError(ex.getMessage());
            }
        }
    }

    /** The SD-emulating act. When the learning rate is very small, the
     results should be similar to absorbExample(...) with the same arguments.

     Presently, this method is only supported for TruncatedGradient.
    */
    public void absorbExamplesSD(Vector<DataPoint> xvec, int i1, int i2) {
        if (!(this instanceof TruncatedGradient)) throw new UnsupportedOperationException("Only TG can emulate SD");
        createMissingBlocks();
        for (LearnerBlock block : blocks) {
            block.dis.ensureCommitted();
            block.validateExamples(xvec, i1, i2);
            ((TruncatedGradient.TruncatedGradientLearnerBlock) block).absorbExamplesSD(xvec, i1, i2);
        }
    }

    /** Runs Steepest Descent (a batch method) with adaptive
	learning rate until it converges.

	<p>Presently, this method is only supported for optimizing 
	log-likelihood without the penalty term. This means that you 
	need to initialize the learner as a TruncatedGradient learner with
	theta=0 and no individual priors.

	<p>For algorithm details, see {@link TruncatedGradient.TruncatedGradientLearnerBlock#runAdaptiveSD}.
	
	<p>The value of {@link Suite#verbosity} is used to control
	what, if anything, is reported during the iterative process.
       

	@param xvec xvec[i1:i2-1] is interpreted as the training set
	@param eps The (original) convergence criterion. The iterations will
	stop when the log-likelihood increment will be smaller
	than this value. Something like 1e-8 is a reasonable value
	on a data set of a few hundreds data points with a dozen
	features each. A smaller value will, of course, make the
	resulting model closer to the ideal Bayesian model (optimizing
	the log-lik), but a significant computation cost.

	@param gradEps The alternative convergence criterion: |grad L|.

    */
    public void runAdaptiveSD(Vector<DataPoint> xvec, int i1, int i2, double eps, double gradEps) {
        if (!(this instanceof TruncatedGradient)) throw new UnsupportedOperationException("Only TG can emulate SD");
        createMissingBlocks();
        for (LearnerBlock block : blocks) {
            block.dis.ensureCommitted();
            block.validateExamples(xvec, i1, i2);
            ((TruncatedGradient.TruncatedGradientLearnerBlock) block).runAdaptiveSD(xvec, i1, i2, eps, gradEps);
        }
    }

    /**  Ensures that a "learner block" (classifier object) is provided
	 for each discriminations in the current suite. This method is
	 invoked both from the derived classes' constructors (when it
	 initializes all blocks), and from absorbExamples (when it
	 just takes care of missing ones, due to recently created
	 discriminations).

	 This method actually creates new blocks in two cases:
	 when the Learner is first created, and after a discrimination
	 has been added to the suite.
     */
    final void createAllBlocks() {
        if (blocks != null) throw new AssertionError("CreateAllBlocks() should only be called from constructors");
        int nd = suite.disCnt();
        blocks = new LearnerBlock[nd];
        for (int did = 0; did < nd; did++) {
            Discrimination dis = suite.getDisc(did);
            if (blocks[did] != null) {
                throw new AssertionError("CreateAllBlocks() should only be called from constructors");
            }
            blocks[did] = createBlock(dis, null);
            if (suite.isFallback(did)) blocks[did].setIsFallback();
            if (dis != blocks[did].dis) throw new AssertionError("Discrimination mismatch on  learner block creation?!");
        }
    }

    final void createMissingBlocks() {
        int nd = suite.disCnt();
        if (blocks == null) throw new AssertionError("CreateAllBlocks() should have been called prior to createMissingBlocks()");
        if (blocks.length < nd) {
            LearnerBlock b[] = new LearnerBlock[nd];
            for (int i = 0; i < blocks.length; i++) b[i] = blocks[i];
            blocks = b;
        } else if (blocks.length > nd) {
            throw new AssertionError(blocks.length > nd);
        }
        for (int did = 0; did < nd; did++) {
            Discrimination dis = suite.getDisc(did);
            if (blocks[did] == null) {
                if (suite.isFallback(did)) throw new AssertionError("It is too late to create a learner for the fallback discrimination - it had to be done the very first thing!");
                LearnerBlock fb = null;
                if (suite.createNDMode == Suite.CreateNewDiscriminationMode.AssumeDefaultHistory) {
                    fb = findFallbackBlock();
                    if (fb == null) {
                        throw new AssertionError("This learner has not been provided with the fallback learning block, which means that we cannot AssumeDefaultHistory for new discrimination " + dis);
                    } else if (fb.dis.claCount() != dis.claCount() || fb.dis.getDefaultCla() == null || dis.getDefaultCla() == null) {
                        String errmsg = "Cannot AssumeDefaultHistory for discrimination '" + dis + "' with " + dis.claCount() + " classes (def=" + dis.getDefaultCla() + "), because of a structure mismatch with the fallback discrimination " + fb.dis + " with " + fb.dis.claCount() + " classes (def=" + fb.dis.getDefaultCla() + ")";
                        if (fb.isZero()) {
                            Logging.warning("Although " + errmsg + ", it does not matter, since the fallback learner is still zero");
                            fb = null;
                        } else {
                            throw new AssertionError(errmsg);
                        }
                    }
                }
                blocks[did] = createBlock(dis, fb);
            }
            if (dis != blocks[did].dis) throw new AssertionError("Discrimination mismatch on learner block validation?!");
        }
    }

    /** Finds the block that's marked as the one for the "fallback"
	discrimination, so that we can use it as the model for new
	blocks in the AssumeDefaultHistory mode.
	@return null if none is found
    */
    private final LearnerBlock findFallbackBlock() {
        for (LearnerBlock b : blocks) {
            if (b != null && b.isFallback) return b;
        }
        return null;
    }

    /** Incrementally trains this classifier on all data points from
	the supplied Vector (xvec).  This method is simply "syntactic
	sugar" for {@link #absorbExample(Vector, int, int)
	absorbExample(Vector, int, int)}, which it invokes.

	@param xvec A training set (list of data points to train the classifier on)
     */
    public final void absorbExample(Vector<DataPoint> xvec) {
        absorbExample(xvec, 0, xvec.size());
    }

    /** Incrementally trains this classifier on all data points from
      the "dataset" XML element e. Assumes that the element contains
      all relevante labels (so that no separate "label store") is
      required.

      @param e A "dataset" XML element. See also the <a href="../../../../../tags.html">Overview of the XML elements used by BOXER</a>
     */
    public final void absorbExample(Element e) throws BoxerXMLException, SAXException {
        Vector<DataPoint> xvec = ParseXML.parseDatasetElement(e, suite, true);
        absorbExample(xvec, 0, xvec.size());
    }

    /** Produces a human-readable description of the current state of
     * the algorithm, with the summary of all relevant tables etc. */
    public final String describe() {
        StringWriter sw = new StringWriter();
        describe(new PrintWriter(sw));
        return sw.toString();
    }

    public final void describe(PrintStream out) {
        describe(out, false);
    }

    public final void describe(PrintStream out, boolean verbose) {
        PrintWriter w = new PrintWriter(out);
        describe(w, verbose);
        w.flush();
        out.flush();
    }

    /** Much the same thing as {@link #describe() describe()}, but
	with immediate printing out the data. It is recommended to use
	this method to avoid running out of heap space.  */
    public void describe(PrintWriter out) {
        describe(out, false);
    }

    public abstract void describe(PrintWriter out, boolean verbose);

    /** Saves the complete internal state of the classifier (with the
     * current values of all parameters, any latent coefficients or
     * whatever) into an XML file. The file can be read in later on to
     * re-create the classifier.

     This method is simply a wrapper around {@link #saveAsXML(Document)}

     @param fname The name of the XML file to create.

     @see #describe() describe()
     */
    public final void saveAsXML(String fname) {
        Document xmldoc = new DocumentImpl();
        Element e = saveAsXML(xmldoc);
        xmldoc.appendChild(e);
        XMLUtil.writeXML(xmldoc, fname);
    }

    /** Saves the complete internal state of the classifier (with the
     * current values of all parameters, any latent coefficients or
     * whatever, as well as its {@link edu.dimacs.mms.boxer.Suite
     * Suite}) as an XML Document object, which can later be written
     * into an XML file. The file can be read in later on to re-create
     * the classifier.
     @return An XML document that can be saved to the file

     @see #describe() describe()
     */
    public Element saveAsXML(Document xmldoc) {
        Element root = xmldoc.createElement(XMLUtil.LEARNER);
        root.setAttribute(ParseXML.ATTR.NAME_ATTR, getName());
        root.setAttribute(ParseXML.ATTR.LEARNER.ALGORITHM, algoName());
        root.setAttribute("version", Version.version);
        root.appendChild(saveParamsAsXML(xmldoc));
        for (LearnerBlock b : blocks) {
            root.appendChild(b.saveAsXML(xmldoc));
        }
        return root;
    }

    /** Names used in XML files */
    static final String PARAMETER = "parameter", PARAMETERS = "parameters", NAME = "name", VALUE = "value";

    /** An auxiliary method for saveAsXML; creates an XML element
      describing one parameter of the algorithm.
      @param value An object whose string representation will be printed as the paramter value 
     */
    protected Element createParamElement(Document xmldoc, String name, Object value) {
        Element e = xmldoc.createElement(PARAMETER);
        e.setAttribute(NAME, name);
        e.setAttribute(VALUE, value.toString());
        return e;
    }

    /** An auxiliary method for saveAsXML; creates an XML element
     * describing all parameter of the algorithm. */
    protected Element createParamsElement(Document xmldoc, String name[], double value[]) {
        Object o[] = new Object[name.length];
        for (int i = 0; i < name.length; i++) {
            o[i] = new Double(value[i]);
        }
        return createParamsElement(xmldoc, name, o);
    }

    protected Element createParamsElement(Document xmldoc, String name[], Object[] value) {
        Element pe = xmldoc.createElement(PARAMETERS);
        if (name.length != value.length) throw new IllegalArgumentException("Names and values arrays are of different length");
        for (int i = 0; i < name.length; i++) {
            pe.appendChild(createParamElement(xmldoc, name[i], value[i]));
        }
        return pe;
    }

    static HashMap<String, Object> parseParamsElement(Element pe, String name[]) {
        return parseParamsElement(pe, makeHashMap(name, new Object[0]));
    }

    /** An auxiliary method for {@link #deserializeLearner(File)};
      parses a "parameters" element, which may be created by {@link
      #createParamsElement(Document, String, double)} or hand-coded by
      the user.
      @param name Names of parameters we expect to find
      @param value Array with the default values. It may be shorter than name
      @return a HashMap mapping each param name to an object (Double
      or Param constant) corresponding to the found value.
     */
    static HashMap<String, Object> parseParamsElement(Element pe, String name[], final double value[]) {
        int n = Math.min(name.length, value.length);
        Object[] o = new Object[n];
        for (int i = 0; i < n; i++) o[i] = new Double(value[i]);
        return parseParamsElement(pe, makeHashMap(name, o));
    }

    static HashMap<String, Object> makeHashMap(String name[], Object value[]) {
        HashMap<String, Object> h = new HashMap<String, Object>();
        for (int i = 0; i < name.length; i++) {
            h.put(name[i], i < value.length ? value[i] : null);
        }
        return h;
    }

    /** An auxiliary method for {@link #deserializeLearner(File)};
      parses a "parameters" element, which may be created by {@link
      #createParamsElement(Document, String, double)} or hand-coded by
      the user.

      Names can be understood in a case-insensitive way, depending on
      the value of the flag ignoreCase (which is currently hard-coded in).

      @param h A HashMap whose keys are the names of the parameters we expect to find, and the values are those params' default values.
      @return a HashMap mapping each param name to an object (Double
      or Param constant) corresponding to the found value.
     */
    static HashMap<String, Object> parseParamsElement(Element pe, HashMap<String, Object> h) {
        final boolean ignoreCase = true;
        HashMap<String, String> lc2name = new HashMap<String, String>();
        if (ignoreCase) {
            for (String name : h.keySet()) lc2name.put(name.toLowerCase(), name);
        }
        int cnt = 0;
        for (Node n = pe.getFirstChild(); n != null; n = n.getNextSibling()) {
            int type = n.getNodeType();
            if (type == Node.ELEMENT_NODE && n.getNodeName().equals(PARAMETER)) {
                String aname = ((Element) n).getAttribute(NAME);
                String aval = ((Element) n).getAttribute(VALUE);
                if (aname == null) throw new IllegalArgumentException("Found a parameter element with no name");
                if (aval == null) throw new IllegalArgumentException("Found parameter element '" + aname + "' with no value");
                if (!h.containsKey(aname)) {
                    String altName = lc2name.get(aname.toLowerCase());
                    if (altName != null) {
                        Logging.warning("Parameter name '" + aname + "' permissively converted to '" + altName + "'");
                        aname = altName;
                    } else throw new IllegalArgumentException("Found parameter element with unexpected name '" + aname + "'");
                }
                Object o;
                try {
                    o = Param.valueOf(aval);
                } catch (java.lang.IllegalArgumentException ex) {
                    o = new Double(XMLUtil.parseDouble(aval));
                }
                h.put(aname, o);
                cnt++;
            } else if (XMLUtil.isIgnorable(n)) {
            } else {
                throw new IllegalArgumentException("Unexpected child found within a PARAMETERS element: " + n);
            }
        }
        return h;
    }

    /** Returns the estimated size, in bytes, of the classifier's main
	data structures. Does not try to include the feature dictionary
	or the Suite data. Child classes may override as needed,
	probably piggybacking on the parent class's method.  */
    public long memoryEstimate() {
        long sum = 2 * Sizeof.OBJ + Sizeof.OBJREF;
        if (blocks != null) {
            for (LearnerBlock block : blocks) {
                sum += Sizeof.OBJREF + block.memoryEstimate();
            }
        }
        return sum;
    }

    /** Returns the name of this algorithm as used e.g. in XML files.
     Presently, it's just the (full) class name of the class
     implementing the algorithm */
    public String algoName() {
        return getClass().getName();
    }

    /** Creates a new Suite instance, and a set of Learners using it,
	from an XML file that has been created on an earlier run with
	{@link #saveAsXML(String) saveAsXML}. 

	This method is a wrapper around {@link
	#deserializeLearnerComplex(Element)}, and has the same
	functionality.

	@throws BoxerXMLException if no proper Suite can be created from the XML 
    */
    public static Suite deserializeLearnerComplex(File f) throws IOException, SAXException, BoxerXMLException {
        return deserializeLearnerComplex(ParseXML.readFileToElement(f));
    }

    /** Creates a new Suite instance, and a set of Learners using it,
	from an XML element that has been created on an earlier run with
	{@link #saveAsXML(String) saveAsXML}. Same functionality as
	{@link #deserializeLearnerComplex(Element)}.

	<p>The proper child order is as follows:
	<pre>
	suite
	features
	priors
	discriminations
	</pre>

	@throws BoxerXMLException if no proper Suite can be created from the XML 
    */
    public static Suite deserializeLearnerComplex(Element e) throws SAXException, BoxerXMLException {
        XMLUtil.assertName(e, XMLUtil.LEARNER_COMPLEX);
        Suite suite = null;
        FeatureDictionary dic = null;
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            int type = n.getNodeType();
            String val = n.getNodeValue();
            if (type == Node.TEXT_NODE && val.trim().length() > 0) {
                Logging.warning("Found an unexpected non-empty text node, val=" + val.trim());
            } else if (type == Node.ELEMENT_NODE) {
                String name = n.getNodeName();
                if (name.equals(XMLUtil.SUITE)) {
                    suite = new Suite((Element) n);
                    if (dic != null) suite.setDic(dic);
                } else if (name.equals(FeatureDictionary.XML.FEATURES)) {
                    if (suite != null && !suite.getDic().isPracticallyEmpty()) {
                        throw new BoxerXMLException("The 'features' element appeared too late in the learner complex. " + suite.getDic().getDimension() + " features had already been initialized in the suite (via a 'priors' element, perhaps");
                    }
                    dic = new FeatureDictionary((Element) n);
                    if (suite != null) {
                        suite.setDic(dic);
                    }
                } else if (name.equals(Priors.NODE.PRIORS)) {
                    if (suite == null) {
                        throw new BoxerXMLException("Cannot read the priors element before the suite element!");
                    }
                    Priors p = new Priors((Element) n, suite);
                    suite.setPriors(p);
                } else if (name.equals(XMLUtil.LEARNERS)) {
                    if (suite == null) throw new AssertionError("Missing suite info");
                    if (suite.getDic() == null) throw new AssertionError("Missing feature dictionary info");
                    suite.deserializeLearners((Element) n);
                } else {
                    throw new IllegalArgumentException("Unexpected element " + name + " within element " + e.getTagName());
                }
            }
        }
        return suite;
    }

    /** This is invoked from {@link Suite.deleteDiscrimination()},
     * before the discrimination is purged from the Suite. Child
     * classes may override it, to delete more structures.
     */
    void deleteDiscrimination(RenumMap map) {
        int did = map.did;
        LearnerBlock b[] = new LearnerBlock[blocks.length - 1];
        for (int i = 0; i < did; i++) b[i] = blocks[i];
        for (int i = did; i < b.length; i++) b[i] = blocks[i + 1];
        blocks = b;
    }

    /** "Links" a learner with a particular suite. This method is typically
     invoked from Learner constructors. */
    final void setSuite(Suite _suite) {
        suite = _suite;
        suite.addLearner(this);
    }

    /** An auxiliary method for converting an int[] array to double[]
     */
    static double[] intArray2doubleArray(int[] ix) {
        double x[] = new double[ix.length];
        for (int i = 0; i < x.length; i++) x[i] = ix[i];
        return x;
    }

    static int[] doubleArray2intArray(double[] y) {
        int ix[] = new int[y.length];
        for (int i = 0; i < y.length; i++) ix[i] = (int) (y[i]);
        return ix;
    }

    /** Creates a new learner block of a suitable type for the
	specified discrimination, based on the learner's general
	parameters. Each concrete learner class defines this method to
	return an object of its appropriate LearnerBlock subclass.

	If a "model" LearnerBlock is supplied (i.e., not null), the
	method verifies that it's a suitable fallback-discrimination
	learner block, and creates the new block based on that
	model. Proper care is taken to map the fallback
	discrimination's default class to the fallback class of this
	block's discriminations.

	@param model The learner block to be copied into the new
	block. This is only used in DefaultHistoryMode, and is null if
	we are to create a block from scratch
     */
    abstract LearnerBlock createBlock(Discrimination did, LearnerBlock model);

    /** Creates an XML "parameters" element describing the param of
	this particular learner. In the root class, this method
	returns an empty "parameters" element; child classes override
	as needed.
     */
    Element saveParamsAsXML(Document xmldoc) {
        return createParamsElement(xmldoc, new String[] {}, new Object[] {});
    }

    /** Sets the name - from the XML element if available, and as an
     * anon learner, otherwise. This method must be called from all
     * derived class constructors.
     */
    protected void initName(Element e) throws BoxerXMLException {
        if (e != null) {
            XMLUtil.assertName(e, XMLUtil.LEARNER);
            name = e.getAttribute(ParseXML.ATTR.NAME_ATTR);
        }
        if (name == null) name = suite.makeNewAnonLearnerName();
        if (name == null) throw new AssertionError("Failed to set learner name. Why?!");
        if (!nameIsUnique()) throw new IllegalArgumentException("Learner name '" + name + "' is not unique within the suite '" + suite.getName() + "'");
    }

    /** Is the name of this learner unique among the names of all
     * learners associated with the same suite? This method is used in
     * Learner constructors, as part of the validation of the input data. */
    boolean nameIsUnique() {
        for (Learner other : suite.getAllLearners()) {
            if (other != this && name.equals(other.getName())) return false;
        }
        return true;
    }

    public LearnerBlock findBlockForDis(Discrimination dis) {
        for (LearnerBlock b : blocks) {
            if (b.dis == dis) return b;
        }
        return null;
    }

    /** Finds the "parameters" element enclosed into the given "learner" element
	@param e A "learner" XML element
	@return A "parameters" element, or null if none is found
     */
    Element findParameters(Element e) throws org.xml.sax.SAXException, BoxerXMLException {
        XMLUtil.assertName(e, XMLUtil.LEARNER);
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element ce = (Element) n;
                if (n.getNodeName().equals(PARAMETERS)) return ce;
            }
        }
        return null;
    }
}
