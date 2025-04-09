package test;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import main.Fight;
import org.junit.Test;
import checker.SafVisitor;
import checker.SafVisitorCheck;
import config.settings;
import saf.*;

public class SafTester extends TestCase {

    Saf saf;

    @Test
    public void testAndOrs() {
        saf = Fight.createFighter(settings.PLAYER1_FILE);
        Behaviour behaviour = saf.getBehaviour();
        boolean foundCondition = false;
        for (BehaviourRule behaviourrule : behaviour.getRules()) {
            if (behaviour.checkCondition(behaviourrule.getCondition(), settings.STRONGER, settings.FAR)) foundCondition = true;
        }
        assertTrue(foundCondition);
    }

    @Test
    public void testBehaviour() {
        List<BehaviourRule> rules = new ArrayList<BehaviourRule>();
        Condition c = new ConditionAction(settings.STRONGER);
        WalkAction w = new WalkAction(settings.STAND);
        FightAction f = new FightAction(settings.BLOCK_HIGH);
        BehaviourRule rule = new BehaviourRule(c, w, f);
        rules.add(rule);
        Behaviour behaviour = new Behaviour(rules);
        assertEquals(rule, behaviour.getCondition(settings.FAR, settings.STRONGER));
    }

    @Test
    public void testAlwaysRule() {
        List<BehaviourRule> rules = new ArrayList<BehaviourRule>();
        Condition c = new ConditionAction(settings.ALWAYS);
        WalkAction w = new WalkAction(settings.STAND);
        FightAction f = new FightAction(settings.BLOCK_HIGH);
        BehaviourRule rule = new BehaviourRule(c, w, f);
        rules.add(rule);
        Behaviour behaviour = new Behaviour(rules);
        assertEquals(rule, behaviour.getAlwaysRule());
    }

    @Test
    public void testFightAction() {
        FightAction f = new FightAction(settings.BLOCK_HIGH);
        assertEquals(settings.BLOCK_HIGH, f.getType());
    }

    @Test
    public void testWalkAction() {
        WalkAction w = new WalkAction(settings.JUMP);
        assertEquals(settings.JUMP, w.getType());
    }

    @Test
    public void testPersonality() {
        List<Strength> strenghts = new ArrayList<Strength>();
        Strength s = new Strength(settings.KICK_POWER, 9);
        strenghts.add(s);
        Personality p = new Personality(strenghts);
        assertEquals(p.getStrength(settings.KICK_POWER).getCharacteristic(), settings.KICK_POWER);
        assertEquals(p.getStrength(settings.KICK_POWER).getValue(), 9);
    }

    @Test
    public void testSpeed() {
        List<Strength> strenghts = new ArrayList<Strength>();
        strenghts.add(new Strength(settings.KICK_POWER, 10));
        strenghts.add(new Strength(settings.KICK_REACH, 10));
        strenghts.add(new Strength(settings.PUNCH_POWER, 10));
        strenghts.add(new Strength(settings.PUNCH_REACH, 10));
        Personality p = new Personality(strenghts);
        int weight = (10 + 10) / 2;
        int height = (10 + 10) / 2;
        float speed = ((height - weight) / 2);
        assertEquals(p.getSpeed(), speed);
    }

    @Test
    public void testSaf() {
        saf = Fight.createFighter(settings.PLAYER1_FILE);
        assertNotNull("Check for personality", saf.getPersonality());
        assertNotNull("Check for Behaviour", saf.getBehaviour());
    }

    @Test
    public void testValidFighter() {
        saf = Fight.createFighter(settings.PLAYER1_FILE);
        SafVisitor safVisitor = new SafVisitorCheck();
        saf.accept(safVisitor);
        assertEquals(0, safVisitor.getCountErrors());
    }
}
