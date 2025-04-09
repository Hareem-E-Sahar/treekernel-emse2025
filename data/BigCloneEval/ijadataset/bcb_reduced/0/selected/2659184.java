package de.fzi.harmonia.commons.basematcher.structuralbasematcher;

import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import de.fzi.harmonia.commons.InfeasibleEvaluatorException;
import de.fzi.harmonia.commons.basematcher.BaseMatcherTest;
import de.fzi.harmonia.commons.basematchers.structuralbasematcher.SimilarityFloodingDistanceMatcher;
import de.fzi.kadmos.api.Correspondence;
import de.fzi.kadmos.api.impl.SimpleCorrespondenceFactory;

/**
 * Test for the {@link SimilarityFloodingDistanceMatcher}.
 * 
 * @author Juergen Bock (bock@fzi.de)
 *
 */
public class SimilarityFloodingDistanceMatcherTest extends BaseMatcherTest {

    static {
        ONTO_FILE_1 = "/de/fzi/harmonia/commons/basematcher/structuralbasematcher/similarityFloodingTest1.owl";
        ONTO_FILE_2 = "/de/fzi/harmonia/commons/basematcher/structuralbasematcher/similarityFloodingTest2.owl";
    }

    private final SimpleCorrespondenceFactory fac = SimpleCorrespondenceFactory.getInstance();

    /**
     * Initialise base matcher under test.
     */
    @Before
    public void setUp() throws Exception {
        baseMatcher = new SimilarityFloodingDistanceMatcher(params, id, alignment);
    }

    /**
     * Clear reference to base matcher under test.
     */
    @After
    public void tearDown() throws Exception {
        baseMatcher = null;
    }

    /**
     * Evaluating class correspondence.
     * Ontology context 1: no sub- or superentities
     * Ontology context 2: no sub- or superentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testClassCorrNoNeighbours() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#NoSuperClassNoSubClass", OWLClass.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#NoSuperClassNoSubClass", OWLClass.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating object property correspondence.
     * Ontology context 1: no sub- or superentities
     * Ontology context 2: no sub- or superentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testObjPropCorrNoNeighbours() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#noSuperObjPropNoSubObjProp", OWLObjectProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#noSuperObjPropNoSubObjProp", OWLObjectProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating data property correspondence.
     * Ontology context 1: no sub- or superentities
     * Ontology context 2: no sub- or superentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testDataPropCorrNoNeighbours() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#noSuperDataPropNoSubDataProp", OWLDataProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#noSuperDataPropNoSubDataProp", OWLDataProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating class correspondence.
     * Ontology context 1: no sub- or superentities
     * Ontology context 2: superentities, no subentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testClassCorrNoNeighboursInOnt1() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#NoSuperClassNoSubClass", OWLClass.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#OneSuperClass", OWLClass.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating object property correspondence.
     * Ontology context 1: no sub- or superentities
     * Ontology context 2: superentities, no subentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testObjPropCorrNoNeighboursInOnt1() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#noSuperObjPropNoSubObjProp", OWLObjectProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#oneSuperObjProp", OWLObjectProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating data property correspondence.
     * Ontology context 1: no sub- or superentities
     * Ontology context 2: no superentities, but subentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testDataPropCorrNoNeighboursInOnt1() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#noSuperDataPropNoSubDataProp", OWLDataProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#oneSuperDataProp", OWLDataProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating class correspondence.
     * Ontology context 1: superentities, no subentities
     * Ontology context 2: no sub- or superentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testClassCorrNoNeighboursInOnt2() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#OneSuperClass", OWLClass.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#NoSuperClassNoSubClass", OWLClass.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating object property correspondence.
     * Ontology context 1: no superentities, but subentities
     * Ontology context 2: no sub- or superentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testObjPropCorrNoNeighboursInOnt2() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#oneSuperObjProp", OWLObjectProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#noSuperObjPropNoSubObjProp", OWLObjectProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating data property correspondence.
     * Ontology context 1: superentities, no subentities
     * Ontology context 2: no sub- or superentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testDataPropCorrNoNeighboursInOnt2() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#oneSuperDataProp", OWLDataProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#noSuperDataPropNoSubDataProp", OWLDataProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating class correspondence.
     * Ontology context 1: superentities, no subentities
     * Ontology context 2: superentities, no subentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testClassCorrNoSubNeighbours() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#OneSuperClass", OWLClass.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#OneSuperClass", OWLClass.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating object property correspondence.
     * Ontology context 1: superentities, no subentities
     * Ontology context 2: superentities, no subentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testObjPropCorrNoSubNeighbours() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#oneSuperObjProp", OWLObjectProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#oneSuperObjProp", OWLObjectProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating data property correspondence.
     * Ontology context 1: superentities, no subentities
     * Ontology context 2: superentities, no subentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testDataPropCorrNoSubNeighbours() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#oneSuperDataProp", OWLDataProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#oneSuperDataProp", OWLDataProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating class correspondence.
     * Ontology context 1: no superentities, but subentities
     * Ontology context 2: no superentities, but subentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testClassCorrNoSuperNeighbours() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#TwoSubClasses", OWLClass.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#TwoSubClasses", OWLClass.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating object property correspondence.
     * Ontology context 1: no superentities, but subentities
     * Ontology context 2: no superentities, but subentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testObjPropCorrNoSuperNeighbours() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#twoSubObjProps", OWLObjectProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#twoSubObjProps", OWLObjectProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating data property correspondence.
     * Ontology context 1: no superentities, but subentities
     * Ontology context 2: no superentities, but subentities
     * Alignment context: -
     * Expected: {@link InfeasibleEvaluatorException} 
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testDataPropCorrNoSuperNeighbours() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#twoSubDataProps", OWLDataProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#twoSubDataProps", OWLDataProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }

    /**
     * Evaluating class correspondence.
     * Ontology context 1: superentities, no subentities
     * Ontology context 2: superentities, no subentities
     * Alignment context: correspondence between superentities
     * Expected: confidence value of context correspondence
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testClassCorrNoSubNeighboursWithContext() throws Exception {
        final double expected = .8d;
        OWLEntity dp1 = getEntity("http://example.org/similarityFloodingTest1.owl#TwoSubClasses", OWLClass.class, ontology1);
        OWLEntity dp2 = getEntity("http://example.org/similarityFloodingTest2.owl#TwoSubClasses", OWLClass.class, ontology2);
        Correspondence<? extends OWLEntity> dpCorr = fac.createCorrespondence(dp1, dp2);
        dpCorr.setConfidence(expected);
        initWithCorrespondences(dpCorr);
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#OneSuperClass", OWLClass.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#OneSuperClass", OWLClass.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        final double actual = baseMatcher.getEvaluation(corr);
        assertEquals(expected, actual, EPSILON);
    }

    /**
     * Evaluating object property correspondence.
     * Ontology context 1: superentities, no subentities
     * Ontology context 2: superentities, no subentities
     * Alignment context: correspondence between superentities
     * Expected: confidence value of context correspondence
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testObjPropCorrNoSubNeighboursWithContext() throws Exception {
        final double expected = .8d;
        OWLEntity dp1 = getEntity("http://example.org/similarityFloodingTest1.owl#twoSubObjProps", OWLObjectProperty.class, ontology1);
        OWLEntity dp2 = getEntity("http://example.org/similarityFloodingTest2.owl#twoSubObjProps", OWLObjectProperty.class, ontology2);
        Correspondence<? extends OWLEntity> dpCorr = fac.createCorrespondence(dp1, dp2);
        dpCorr.setConfidence(expected);
        initWithCorrespondences(dpCorr);
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#oneSuperObjProp", OWLObjectProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#oneSuperObjProp", OWLObjectProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        final double actual = baseMatcher.getEvaluation(corr);
        assertEquals(expected, actual, EPSILON);
    }

    /**
     * Evaluating data property correspondence.
     * Ontology context 1: superentities, no subentities
     * Ontology context 2: superentities, no subentities
     * Alignment context: correspondence between superentities
     * Expected: confidence value of context correspondence
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testDataPropCorrNoSubNeighboursWithContext() throws Exception {
        final double expected = .8d;
        OWLEntity dp1 = getEntity("http://example.org/similarityFloodingTest1.owl#twoSubDataProps", OWLDataProperty.class, ontology1);
        OWLEntity dp2 = getEntity("http://example.org/similarityFloodingTest2.owl#twoSubDataProps", OWLDataProperty.class, ontology2);
        Correspondence<? extends OWLEntity> dpCorr = fac.createCorrespondence(dp1, dp2);
        dpCorr.setConfidence(expected);
        initWithCorrespondences(dpCorr);
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#oneSuperDataProp", OWLDataProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#oneSuperDataProp", OWLDataProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        final double actual = baseMatcher.getEvaluation(corr);
        assertEquals(expected, actual, EPSILON);
    }

    /**
     * Evaluating class correspondence.
     * Ontology context 1: no superentities, but subentities
     * Ontology context 2: no superentities, but subentities
     * Alignment context: correspondence between subentities
     * Expected: confidence value of context correspondence
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testClassCorrNoSuperNeighboursWithContext() throws Exception {
        final double expected = .8d;
        OWLEntity dp1 = getEntity("http://example.org/similarityFloodingTest1.owl#OneSuperClass", OWLClass.class, ontology1);
        OWLEntity dp2 = getEntity("http://example.org/similarityFloodingTest2.owl#OneSuperClass", OWLClass.class, ontology2);
        Correspondence<? extends OWLEntity> dpCorr = fac.createCorrespondence(dp1, dp2);
        dpCorr.setConfidence(expected);
        initWithCorrespondences(dpCorr);
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#TwoSubClasses", OWLClass.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#TwoSubClasses", OWLClass.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        final double actual = baseMatcher.getEvaluation(corr);
        assertEquals(expected, actual, EPSILON);
    }

    /**
     * Evaluating object property correspondence.
     * Ontology context 1: no superentities, but subentities
     * Ontology context 2: no superentities, but subentities
     * Alignment context: correspondence between subentities
     * Expected: confidence value of context correspondence
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testObjPropCorrNoSuperNeighboursWithContext() throws Exception {
        final double expected = .8d;
        OWLEntity dp1 = getEntity("http://example.org/similarityFloodingTest1.owl#oneSuperObjProp", OWLObjectProperty.class, ontology1);
        OWLEntity dp2 = getEntity("http://example.org/similarityFloodingTest2.owl#oneSuperObjProp", OWLObjectProperty.class, ontology2);
        Correspondence<? extends OWLEntity> dpCorr = fac.createCorrespondence(dp1, dp2);
        dpCorr.setConfidence(expected);
        initWithCorrespondences(dpCorr);
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#twoSubObjProps", OWLObjectProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#twoSubObjProps", OWLObjectProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        final double actual = baseMatcher.getEvaluation(corr);
        assertEquals(expected, actual, EPSILON);
    }

    /**
     * Evaluating data property correspondence.
     * Ontology context 1: no superentities, but subentities
     * Ontology context 2: no superentities, but subentities
     * Alignment context: correspondence between subentities
     * Expected: confidence value of context correspondence
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testDataPropCorrNoSuperNeighboursWithContext() throws Exception {
        final double expected = .8d;
        OWLEntity dp1 = getEntity("http://example.org/similarityFloodingTest1.owl#oneSuperDataProp", OWLDataProperty.class, ontology1);
        OWLEntity dp2 = getEntity("http://example.org/similarityFloodingTest2.owl#oneSuperDataProp", OWLDataProperty.class, ontology2);
        Correspondence<? extends OWLEntity> dpCorr = fac.createCorrespondence(dp1, dp2);
        dpCorr.setConfidence(expected);
        initWithCorrespondences(dpCorr);
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#twoSubDataProps", OWLDataProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#twoSubDataProps", OWLDataProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        final double actual = baseMatcher.getEvaluation(corr);
        assertEquals(expected, actual, EPSILON);
    }

    /**
     * Evaluating class correspondence.
     * Ontology context 1: superentities and subentities
     * Ontology context 2: superentities and subentities
     * Alignment context: correspondence between sub- and superentities
     * Expected: average of confidence values of context correspondence
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testClassCorrWithContext() throws Exception {
        final double confidence1 = .3d;
        final double confidence2 = .5d;
        final double expected = (confidence1 + confidence2) / 2;
        OWLEntity dp1 = getEntity("http://example.org/similarityFloodingTest1.owl#TwoSubClasses", OWLClass.class, ontology1);
        OWLEntity dp2 = getEntity("http://example.org/similarityFloodingTest2.owl#TwoSubClasses", OWLClass.class, ontology2);
        Correspondence<? extends OWLEntity> dpCorr1 = fac.createCorrespondence(dp1, dp2);
        dpCorr1.setConfidence(confidence1);
        dp1 = getEntity("http://example.org/similarityFloodingTest1.owl#Sub", OWLClass.class, ontology1);
        dp2 = getEntity("http://example.org/similarityFloodingTest2.owl#Sub", OWLClass.class, ontology2);
        Correspondence<? extends OWLEntity> dpCorr2 = fac.createCorrespondence(dp1, dp2);
        dpCorr2.setConfidence(confidence2);
        initWithCorrespondences(dpCorr1, dpCorr2);
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#OneSuperClassOneSubClass", OWLClass.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#OneSuperClassOneSubClass", OWLClass.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        final double actual = baseMatcher.getEvaluation(corr);
        assertEquals(expected, actual, EPSILON);
    }

    /**
     * Evaluating object property correspondence.
     * Ontology context 1: superentities and subentities
     * Ontology context 2: superentities and subentities
     * Alignment context: correspondence between sub- and superentities
     * Expected: average of confidence values of context correspondence
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testObjPropCorrWithContext() throws Exception {
        final double confidence1 = .3d;
        final double confidence2 = .5d;
        final double expected = (confidence1 + confidence2) / 2;
        OWLEntity dp1 = getEntity("http://example.org/similarityFloodingTest1.owl#twoSubObjProps", OWLObjectProperty.class, ontology1);
        OWLEntity dp2 = getEntity("http://example.org/similarityFloodingTest2.owl#twoSubObjProps", OWLObjectProperty.class, ontology2);
        Correspondence<? extends OWLEntity> dpCorr1 = fac.createCorrespondence(dp1, dp2);
        dpCorr1.setConfidence(confidence1);
        dp1 = getEntity("http://example.org/similarityFloodingTest1.owl#subObjProp", OWLObjectProperty.class, ontology1);
        dp2 = getEntity("http://example.org/similarityFloodingTest2.owl#subObjProp", OWLObjectProperty.class, ontology2);
        Correspondence<? extends OWLEntity> dpCorr2 = fac.createCorrespondence(dp1, dp2);
        dpCorr2.setConfidence(confidence2);
        initWithCorrespondences(dpCorr1, dpCorr2);
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#oneSuperObjPropOneSubObjProp", OWLObjectProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#oneSuperObjPropOneSubObjProp", OWLObjectProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        final double actual = baseMatcher.getEvaluation(corr);
        assertEquals(expected, actual, EPSILON);
    }

    /**
     * Evaluating data property correspondence.
     * Ontology context 1: superentities and subentities
     * Ontology context 2: superentities and subentities
     * Alignment context: correspondence between sub- and superentities
     * Expected: average of confidence values of context correspondence
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testDataPropCorrWithContext() throws Exception {
        final double confidence1 = .3d;
        final double confidence2 = .5d;
        final double expected = (confidence1 + confidence2) / 2;
        OWLEntity dp1 = getEntity("http://example.org/similarityFloodingTest1.owl#twoSubDataProps", OWLDataProperty.class, ontology1);
        OWLEntity dp2 = getEntity("http://example.org/similarityFloodingTest2.owl#twoSubDataProps", OWLDataProperty.class, ontology2);
        Correspondence<? extends OWLEntity> dpCorr1 = fac.createCorrespondence(dp1, dp2);
        dpCorr1.setConfidence(confidence1);
        dp1 = getEntity("http://example.org/similarityFloodingTest1.owl#subDataProp", OWLDataProperty.class, ontology1);
        dp2 = getEntity("http://example.org/similarityFloodingTest2.owl#subDataProp", OWLDataProperty.class, ontology2);
        Correspondence<? extends OWLEntity> dpCorr2 = fac.createCorrespondence(dp1, dp2);
        dpCorr2.setConfidence(confidence2);
        initWithCorrespondences(dpCorr1, dpCorr2);
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#oneSuperDataPropOneSubDataProp", OWLDataProperty.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#oneSuperDataPropOneSubDataProp", OWLDataProperty.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        final double actual = baseMatcher.getEvaluation(corr);
        assertEquals(expected, actual, EPSILON);
    }

    /**
     * Base matcher applied to individuals.
     * Expected: {@link InfeasibleEvaluatorException}
     */
    @SuppressWarnings("unchecked")
    @Test(expected = InfeasibleEvaluatorException.class)
    public final void testInfeasibleForIndividuals() throws Exception {
        initWithCorrespondences();
        OWLEntity ent1 = getEntity("http://example.org/similarityFloodingTest1.owl#Individual", OWLNamedIndividual.class, ontology1);
        OWLEntity ent2 = getEntity("http://example.org/similarityFloodingTest2.owl#Individual", OWLNamedIndividual.class, ontology2);
        Correspondence<? extends OWLEntity> corr = fac.createCorrespondence(ent1, ent2);
        baseMatcher.getEvaluation(corr);
    }
}
