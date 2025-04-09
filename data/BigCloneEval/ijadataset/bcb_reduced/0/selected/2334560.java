package net.sf.brightside.chocolatefever.metamodel.beans;

import net.sf.brightside.chocolatefever.metamodel.beans.ChocolateBean;
import net.sf.brightside.chocolatefever.metamodel.Evaluation;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertNotNull;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;
import java.util.LinkedList;

public class ChocolateBeanTest {

    private ChocolateBean chocolateBeanUnderTest;

    @BeforeMethod
    public void startUp() {
        chocolateBeanUnderTest = new ChocolateBean();
        chocolateBeanUnderTest.setEvaluations(new LinkedList<Evaluation>());
    }

    @Test
    public void testName() {
        String name = "Milka";
        assertNull(chocolateBeanUnderTest.getName());
        chocolateBeanUnderTest.setName(name);
        assertEquals(name, chocolateBeanUnderTest.getName());
    }

    @Test
    public void testNameSetNull() {
        String name = "Milka";
        chocolateBeanUnderTest.setName(name);
        assertEquals(name, chocolateBeanUnderTest.getName());
        chocolateBeanUnderTest.setName(null);
        assertNull(chocolateBeanUnderTest.getName());
    }

    @Test
    public void testEvaluationsNotNull() {
        assertNotNull(chocolateBeanUnderTest.getEvaluations());
    }

    @Test
    public void testEvaluationsAssociation() {
        Evaluation evaluation = createStrictMock(Evaluation.class);
        assertFalse(chocolateBeanUnderTest.getEvaluations().contains(evaluation));
        chocolateBeanUnderTest.getEvaluations().add(evaluation);
        assertTrue(chocolateBeanUnderTest.getEvaluations().contains(evaluation));
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
        chocolateBeanUnderTest.getEvaluations().add(evalOne);
        chocolateBeanUnderTest.getEvaluations().add(evalTwo);
        assertEquals(avg, chocolateBeanUnderTest.avarageScore());
    }
}
