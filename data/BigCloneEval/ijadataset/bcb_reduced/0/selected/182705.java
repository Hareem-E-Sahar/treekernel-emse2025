package edu.upmc.opi.caBIG.caTIES.installer.pipes.lucenefinder.ae;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.FSDirectory;
import edu.upmc.opi.caBIG.caTIES.installer.pipes.creole.CaTIES_SortedAnnotationSet;
import edu.upmc.opi.caBIG.caTIES.installer.pipes.creole.CaTIES_Utilities;
import edu.upmc.opi.caBIG.caTIES.installer.pipes.lucenefinder.ae.strategy.ODIE_LuceneNerStrategyInterface;
import edu.upmc.opi.caBIG.caTIES.installer.pipes.lucenefinder.ae.utils.Stemmer;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.util.InvalidOffsetException;

/**
 * UIMA annotator that identified entities based on IndexFinder algorithm.
 * 
 * @author University of Pittsburgh ODIE Program
 * 
 */
public class ODIE_LuceneNerAnnotationEngine {

    private static final Logger logger = Logger.getLogger(ODIE_LuceneNerAnnotationEngine.class);

    private ODIE_LuceneNerStrategyInterface strategyEngine;

    private Stemmer stemmer = null;

    private String fsDirectoryPath = null;

    protected FSDirectory fsDirectory = null;

    protected Searcher searcher = null;

    protected boolean isContiguous = true;

    protected boolean isOverlapping = false;

    protected int maxHits = 1000;

    public ODIE_LuceneNerAnnotationEngine() {
        checkClassPath();
    }

    private void checkClassPath() {
        try {
            Class.forName("org.apache.log4j.Logger");
            Class.forName("org.apache.lucene.index.IndexReader");
            Class.forName("org.apache.lucene.search.IndexSearcher");
            Class.forName("org.apache.lucene.search.Searcher");
            Class.forName("org.apache.lucene.store.FSDirectory");
            Class.forName("edu.upmc.opi.caBIG.caTIES.server.ties.creole.CaTIES_SortedAnnotationSet");
            Class.forName("edu.upmc.opi.caBIG.caTIES.server.ties.creole.CaTIES_Utilities");
            Class.forName("edu.upmc.opi.caBIG.caTIES.server.ties.lucenefinder.ae.strategy.ODIE_LuceneNerStrategyInterface");
            Class.forName("edu.upmc.opi.caBIG.caTIES.server.ties.lucenefinder.ae.utils.Stemmer");
            Class.forName("gate.Annotation");
            Class.forName("gate.AnnotationSet");
            Class.forName("gate.Document");
            Class.forName("gate.Factory");
            Class.forName("gate.FeatureMap");
            Class.forName("gate.util.InvalidOffsetException");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Reads configuration parameters.
	 * 
	 * @param annotatorContext
	 */
    public void initialize() {
        try {
            openIndex();
            openSearcher();
            this.stemmer = new Stemmer();
            if (!isOverlapping && isContiguous) {
                this.strategyEngine = new edu.upmc.opi.caBIG.caTIES.installer.pipes.lucenefinder.ae.strategy.nonoverlapping.contiguous.ODIE_LuceneNerStategy(searcher);
            } else if (!isOverlapping && !isContiguous) {
                this.strategyEngine = new edu.upmc.opi.caBIG.caTIES.installer.pipes.lucenefinder.ae.strategy.nonoverlapping.noncontiguous.ODIE_LuceneNerStategy(searcher);
            } else if (isOverlapping && isContiguous) {
                this.strategyEngine = new edu.upmc.opi.caBIG.caTIES.installer.pipes.lucenefinder.ae.strategy.overlapping.contiguous.ODIE_LuceneNerStategy(searcher);
            } else if (isOverlapping && !isContiguous) {
                this.strategyEngine = new edu.upmc.opi.caBIG.caTIES.installer.pipes.lucenefinder.ae.strategy.overlapping.noncontiguous.ODIE_LuceneNerStategy(searcher);
            }
            this.strategyEngine.setMaxHits(getMaxHits());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void openIndex() {
        try {
            this.fsDirectory = FSDirectory.open(new File(this.fsDirectoryPath));
            logger.debug("Opened the index at " + this.fsDirectoryPath);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public void closeIndex() {
        try {
            this.fsDirectory.close();
        } catch (Exception x) {
            ;
        }
    }

    protected void openSearcher() {
        logger.debug("Try to open the searcher with FSDirectory ==> " + this.fsDirectory.getFile().getAbsolutePath());
        try {
            boolean readOnly = true;
            this.searcher = new IndexSearcher(IndexReader.open(this.fsDirectory, readOnly));
            logger.debug("Succeeded in opening the searcher with FSDirectory ==> " + this.fsDirectory.getFile().getAbsolutePath());
        } catch (Exception x) {
            logger.error(x.getMessage());
            x.printStackTrace();
            logger.error("Failed opening the searcher with FSDirectory ==> " + this.fsDirectory.getFile().getAbsolutePath());
        }
    }

    public void process(Document doc, AnnotationSet sentenceAnnots) {
        try {
            Iterator<Annotation> sentenceIterator = sentenceAnnots.iterator();
            while (sentenceIterator.hasNext()) {
                Annotation sentenceAnnot = (Annotation) sentenceIterator.next();
                Iterator<Annotation> wordIterator = constrainToSentenceWindow(doc, sentenceAnnot);
                ArrayList<ODIE_IndexFinderAnnotation> odieAnnots = genericallyWrapTokenAnnotations(doc, wordIterator);
                this.strategyEngine.setSortedTokens(odieAnnots);
                this.strategyEngine.execute();
                ArrayList<ODIE_IndexFinderAnnotation> resultingConcepts = this.strategyEngine.getResultingConcepts();
                unWrapOdieAnnotations(doc, resultingConcepts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ODIE_IndexFinderAnnotation> process(String phrase) {
        Pattern tokeniserPattern = Pattern.compile("\\S+");
        Matcher matcher = tokeniserPattern.matcher(phrase);
        ArrayList<ODIE_IndexFinderAnnotation> odieAnnots = new ArrayList<ODIE_IndexFinderAnnotation>();
        int annotationId = 0;
        while (matcher.find()) {
            int spos = matcher.start();
            int epos = matcher.end();
            ODIE_IndexFinderNode sNode = new ODIE_IndexFinderNode();
            sNode.setOffset(new Long(spos));
            ODIE_IndexFinderNode eNode = new ODIE_IndexFinderNode();
            eNode.setOffset(new Long(epos));
            String token = matcher.group(0);
            ODIE_IndexFinderAnnotation annot = new ODIE_IndexFinderAnnotation();
            annot.setStartNode(sNode);
            annot.setEndNode(eNode);
            annot.setAnnotationId(annotationId++);
            annot.setAnnotationSetName("");
            annot.setAnnotationTypeName("Token");
            annot.setFeatures(new HashMap<String, Object>());
            annot.getFeatures().put("string", token);
            if (this.stemmer != null) {
                this.stemmer.add(token);
                this.stemmer.stem();
                String normalizedForm = this.stemmer.getResultString();
                annot.getFeatures().put("normalizedForm", normalizedForm);
            }
            odieAnnots.add(annot);
        }
        this.strategyEngine.setSortedTokens(odieAnnots);
        this.strategyEngine.execute();
        return this.strategyEngine.getResultingConcepts();
    }

    /**
	 * Gets a list of LookupToken objects within the specified window
	 * annotation.
	 * 
	 * @param jcas
	 * 
	 * @param window
	 * @param lookupTokenItr
	 * @return
	 * @throws Exception
	 */
    @SuppressWarnings("unchecked")
    private Iterator<Annotation> constrainToSentenceWindow(Document doc, Annotation window) throws Exception {
        AnnotationSet tokenAnnots = doc.getAnnotations().get("Token", window.getStartNode().getOffset(), window.getEndNode().getOffset());
        CaTIES_SortedAnnotationSet sortedTokenAnnots = new CaTIES_SortedAnnotationSet(tokenAnnots);
        Iterator<Annotation> wordTokenIterator = sortedTokenAnnots.iterator();
        return wordTokenIterator;
    }

    private ArrayList<ODIE_IndexFinderAnnotation> genericallyWrapTokenAnnotations(Document doc, Iterator<Annotation> tokenAnnotationsIterator) {
        ArrayList<ODIE_IndexFinderAnnotation> result = new ArrayList<ODIE_IndexFinderAnnotation>();
        for (; tokenAnnotationsIterator.hasNext(); ) {
            Annotation token = tokenAnnotationsIterator.next();
            ODIE_IndexFinderAnnotation odieAnnot = new ODIE_IndexFinderAnnotation();
            ODIE_IndexFinderNode odieAnnotSNode = new ODIE_IndexFinderNode();
            ODIE_IndexFinderNode odieAnnotENode = new ODIE_IndexFinderNode();
            String tokenKind = "word";
            String tokenString = CaTIES_Utilities.spanStrings(doc, token);
            tokenString = (tokenString != null) ? tokenString.toLowerCase() : null;
            if (tokenString == null) {
                continue;
            }
            odieAnnotSNode.setOffset(token.getStartNode().getOffset());
            odieAnnotENode.setOffset(token.getStartNode().getOffset());
            odieAnnot.setStartNode(odieAnnotSNode);
            odieAnnot.setEndNode(odieAnnotENode);
            odieAnnot.setAnnotationSetName("Default");
            odieAnnot.setAnnotationTypeName("Token");
            odieAnnot.getFeatures().put("kind", tokenKind);
            odieAnnot.getFeatures().put("string", tokenString);
            if (this.stemmer != null) {
                this.stemmer.add(tokenString);
                this.stemmer.stem();
                String normalizedForm = this.stemmer.getResultString();
                odieAnnot.getFeatures().put("normalizedForm", normalizedForm);
            }
            result.add(odieAnnot);
        }
        return result;
    }

    private void unWrapOdieAnnotations(Document doc, ArrayList<ODIE_IndexFinderAnnotation> odieAnnots) {
        for (Iterator<ODIE_IndexFinderAnnotation> odieAnnotIterator = odieAnnots.iterator(); odieAnnotIterator.hasNext(); ) {
            ODIE_IndexFinderAnnotation odieAnnot = odieAnnotIterator.next();
            FeatureMap features = Factory.newFeatureMap();
            String clsQName = (String) odieAnnot.getFeatures().get("cn");
            String[] clsQNameParts = clsQName.split("#");
            String ontologyUri = clsQNameParts[0];
            String clsName = clsQNameParts[clsQNameParts.length - 1];
            features.put("clsName", clsName);
            features.put("oid", ontologyUri);
            features.put("codingScheme", ontologyUri);
            features.put("cui", (String) odieAnnot.getFeatures().get("umlsCui"));
            features.put("tui", (String) odieAnnot.getFeatures().get("umlsTuis"));
            features.put("cn", (String) odieAnnot.getFeatures().get("string"));
            features.put("sty", (String) odieAnnot.getFeatures().get("cTakesSemanticType"));
            features.put("term", spanStrings(doc, odieAnnot));
            try {
                doc.getAnnotations().add(odieAnnot.getStartNode().getOffset(), odieAnnot.getEndNode().getOffset(), "Concept", features);
            } catch (InvalidOffsetException e) {
                e.printStackTrace();
            }
        }
    }

    private String spanStrings(gate.Document doc, ODIE_IndexFinderAnnotation odieAnnot) {
        String retValue = null;
        try {
            gate.DocumentContent documentContent = doc.getContent();
            documentContent = documentContent.getContent(odieAnnot.getStartNode().getOffset(), odieAnnot.getEndNode().getOffset());
            retValue = ((gate.corpora.DocumentContentImpl) documentContent).getOriginalContent();
        } catch (Exception x) {
            retValue = null;
        }
        return retValue;
    }

    public boolean isContiguous() {
        return isContiguous;
    }

    public void setContiguous(boolean isContiguous) {
        this.isContiguous = isContiguous;
    }

    public boolean isOverlapping() {
        return isOverlapping;
    }

    public void setOverlapping(boolean isOverlapping) {
        this.isOverlapping = isOverlapping;
    }

    public String getFsDirectoryPath() {
        return fsDirectoryPath;
    }

    public void setFsDirectoryPath(String fsDirectoryPath) {
        this.fsDirectoryPath = fsDirectoryPath;
    }

    public int getMaxHits() {
        return maxHits;
    }

    public void setMaxHits(int maxHits) {
        this.maxHits = maxHits;
    }
}
