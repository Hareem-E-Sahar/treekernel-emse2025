package net.sf.brightside.chocolatefever.metamodel.beans;

import net.sf.brightside.chocolatefever.metamodel.beans.TasterBean;
import net.sf.brightside.chocolatefever.metamodel.Evaluation;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertNotNull;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import java.util.LinkedList;

public class TasterBeanTest {

    private TasterBean tasterBeanUnderTest;

    @BeforeMethod
    public void startUp() {
        tasterBeanUnderTest = new TasterBean();
        tasterBeanUnderTest.setEvaluations(new LinkedList<Evaluation>());
    }

    @Test
    public void testName() {
        String name = "Pera Peric";
        assertNull(tasterBeanUnderTest.getName());
        tasterBeanUnderTest.setName(name);
        assertEquals(name, tasterBeanUnderTest.getName());
    }

    @Test
    public void testNameSetNull() {
        String name = "Pera Peric";
        tasterBeanUnderTest.setName(name);
        assertEquals(name, tasterBeanUnderTest.getName());
        tasterBeanUnderTest.setName(null);
        assertNull(tasterBeanUnderTest.getName());
    }

    @Test
    public void testEvaluationsNotNull() {
        assertNotNull(tasterBeanUnderTest.getEvaluations());
    }

    @Test
    public void testEvaluationsAssociation() {
        Evaluation evaluation = createStrictMock(Evaluation.class);
        assertFalse(tasterBeanUnderTest.getEvaluations().contains(evaluation));
        tasterBeanUnderTest.getEvaluations().add(evaluation);
        assertTrue(tasterBeanUnderTest.getEvaluations().contains(evaluation));
    }

    @Test
    public void testAvarageScore() {
        int a = 10;
        int b = 6;
        double avg = (a + b) / 2;
        Evaluation evalOne = createStrictMock(Evaluation.class);
        Evaluation evalTwo = createStrictMock(Evaluation.class);
        expect(evalOne.getScore()).andReturn(a);
        expect(evalTwo.getScore()).andReturn(b);
        replay(evalOne);
        replay(evalTwo);
        tasterBeanUnderTest.getEvaluations().add(evalOne);
        tasterBeanUnderTest.getEvaluations().add(evalTwo);
        assertEquals(avg, tasterBeanUnderTest.avarageScore());
    }
}
