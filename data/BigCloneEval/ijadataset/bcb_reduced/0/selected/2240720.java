package games.strategy.engine.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author Sean Bridges
 * @version 1.0
 */
public class ChangeTest extends TestCase {

    private GameData m_data;

    /** Creates new ChangeTest */
    public ChangeTest(final String name) {
        super(name);
    }

    public static Test suite() {
        final TestSuite suite = new TestSuite();
        suite.addTestSuite(ChangeTest.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        final URL url = this.getClass().getResource("Test.xml");
        final InputStream input = url.openStream();
        m_data = (new GameParser()).parse(input, false);
    }

    private Change serialize(final Change aChange) throws Exception {
        final ByteArrayOutputStream sink = new ByteArrayOutputStream();
        final ObjectOutputStream output = new GameObjectOutputStream(sink);
        output.writeObject(aChange);
        output.flush();
        final InputStream source = new ByteArrayInputStream(sink.toByteArray());
        final ObjectInputStream input = new GameObjectInputStream(new games.strategy.engine.framework.GameObjectStreamFactory(m_data), source);
        final Change newChange = (Change) input.readObject();
        return newChange;
    }

    public void testUnitsAddTerritory() {
        final Territory can = m_data.getMap().getTerritory("canada");
        assertEquals(can.getUnits().getUnitCount(), 5);
        final Change change = ChangeFactory.addUnits(can, m_data.getUnitTypeList().getUnitType("inf").create(10, null));
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(can.getUnits().getUnitCount(), 15);
        changePerformer.perform(change.invert());
        assertEquals(can.getUnits().getUnitCount(), 5);
    }

    public void testUnitsRemoveTerritory() {
        final Territory can = m_data.getMap().getTerritory("canada");
        assertEquals(can.getUnits().getUnitCount(), 5);
        final Collection<Unit> units = can.getUnits().getUnits(m_data.getUnitTypeList().getUnitType("inf"), 3);
        final Change change = ChangeFactory.removeUnits(can, units);
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(can.getUnits().getUnitCount(), 2);
        changePerformer.perform(change.invert());
        assertEquals(can.getUnits().getUnitCount(), 5);
    }

    public void testSerializeUnitsRemoteTerritory() throws Exception {
        final Territory can = m_data.getMap().getTerritory("canada");
        assertEquals(can.getUnits().getUnitCount(), 5);
        final Collection<Unit> units = can.getUnits().getUnits(m_data.getUnitTypeList().getUnitType("inf"), 3);
        Change change = ChangeFactory.removeUnits(can, units);
        change = serialize(change);
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(can.getUnits().getUnitCount(), 2);
        changePerformer.perform(change.invert());
        assertEquals(can.getUnits().getUnitCount(), 5);
    }

    public void testUnitsAddPlayer() {
        final PlayerID chretian = m_data.getPlayerList().getPlayerID("chretian");
        assertEquals(chretian.getUnits().getUnitCount(), 10);
        final Change change = ChangeFactory.addUnits(chretian, m_data.getUnitTypeList().getUnitType("inf").create(10, null));
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(chretian.getUnits().getUnitCount(), 20);
        changePerformer.perform(change.invert());
        assertEquals(chretian.getUnits().getUnitCount(), 10);
    }

    public void testUnitsRemovePlayer() {
        final PlayerID chretian = m_data.getPlayerList().getPlayerID("chretian");
        assertEquals(chretian.getUnits().getUnitCount(), 10);
        final Collection<Unit> units = chretian.getUnits().getUnits(m_data.getUnitTypeList().getUnitType("inf"), 3);
        final Change change = ChangeFactory.removeUnits(chretian, units);
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(chretian.getUnits().getUnitCount(), 7);
        changePerformer.perform(change.invert());
        assertEquals(chretian.getUnits().getUnitCount(), 10);
    }

    public void testUnitsMove() {
        final Territory canada = m_data.getMap().getTerritory("canada");
        final Territory greenland = m_data.getMap().getTerritory("greenland");
        assertEquals(canada.getUnits().getUnitCount(), 5);
        assertEquals(greenland.getUnits().getUnitCount(), 0);
        final Collection<Unit> units = canada.getUnits().getUnits(m_data.getUnitTypeList().getUnitType("inf"), 3);
        final Change change = ChangeFactory.moveUnits(canada, greenland, units);
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(canada.getUnits().getUnitCount(), 2);
        assertEquals(greenland.getUnits().getUnitCount(), 3);
        changePerformer.perform(change.invert());
        assertEquals(canada.getUnits().getUnitCount(), 5);
        assertEquals(greenland.getUnits().getUnitCount(), 0);
    }

    public void testUnitsMoveSerialization() throws Exception {
        final Territory canada = m_data.getMap().getTerritory("canada");
        final Territory greenland = m_data.getMap().getTerritory("greenland");
        assertEquals(canada.getUnits().getUnitCount(), 5);
        assertEquals(greenland.getUnits().getUnitCount(), 0);
        final Collection<Unit> units = canada.getUnits().getUnits(m_data.getUnitTypeList().getUnitType("inf"), 3);
        Change change = ChangeFactory.moveUnits(canada, greenland, units);
        change = serialize(change);
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(canada.getUnits().getUnitCount(), 2);
        assertEquals(greenland.getUnits().getUnitCount(), 3);
        changePerformer.perform(serialize(change.invert()));
        assertEquals(canada.getUnits().getUnitCount(), 5);
        assertEquals(greenland.getUnits().getUnitCount(), 0);
    }

    public void testProductionFrontierChange() {
        final PlayerID can = m_data.getPlayerList().getPlayerID("chretian");
        final ProductionFrontier uspf = m_data.getProductionFrontierList().getProductionFrontier("usProd");
        final ProductionFrontier canpf = m_data.getProductionFrontierList().getProductionFrontier("canProd");
        assertEquals(can.getProductionFrontier(), canpf);
        final Change change = ChangeFactory.changeProductionFrontier(can, uspf);
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(can.getProductionFrontier(), uspf);
        changePerformer.perform(change.invert());
        assertEquals(can.getProductionFrontier(), canpf);
    }

    public void testChangeResourcesChange() {
        final PlayerID can = m_data.getPlayerList().getPlayerID("chretian");
        final Resource gold = m_data.getResourceList().getResource("gold");
        final Change change = ChangeFactory.changeResourcesChange(can, gold, 50);
        assertEquals(can.getResources().getQuantity(gold), 100);
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(can.getResources().getQuantity(gold), 150);
        changePerformer.perform(change.invert());
        assertEquals(can.getResources().getQuantity(gold), 100);
    }

    public void testSerializeResourceChange() throws Exception {
        final PlayerID can = m_data.getPlayerList().getPlayerID("chretian");
        final Resource gold = m_data.getResourceList().getResource("gold");
        Change change = ChangeFactory.changeResourcesChange(can, gold, 50);
        change = serialize(change);
        assertEquals(can.getResources().getQuantity(gold), 100);
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(can.getResources().getQuantity(gold), 150);
    }

    public void testChangeOwner() {
        final PlayerID can = m_data.getPlayerList().getPlayerID("chretian");
        final PlayerID us = m_data.getPlayerList().getPlayerID("bush");
        final Territory greenland = m_data.getMap().getTerritory("greenland");
        final Change change = ChangeFactory.changeOwner(greenland, us);
        assertEquals(greenland.getOwner(), can);
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(greenland.getOwner(), us);
        changePerformer.perform(change.invert());
        assertEquals(greenland.getOwner(), can);
    }

    public void testChangeOwnerSerialize() throws Exception {
        final PlayerID can = m_data.getPlayerList().getPlayerID("chretian");
        final PlayerID us = m_data.getPlayerList().getPlayerID("bush");
        final Territory greenland = m_data.getMap().getTerritory("greenland");
        Change change = ChangeFactory.changeOwner(greenland, us);
        change = serialize(change);
        assertEquals(greenland.getOwner(), can);
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(greenland.getOwner(), us);
        change = change.invert();
        change = serialize(change);
        changePerformer.perform(change);
        assertEquals(greenland.getOwner(), can);
    }

    public void testPlayerOwnerChange() throws Exception {
        final PlayerID can = m_data.getPlayerList().getPlayerID("chretian");
        final PlayerID us = m_data.getPlayerList().getPlayerID("bush");
        final UnitType infantry = m_data.getUnitTypeList().getUnitType("inf");
        final Unit inf1 = infantry.create(1, can).iterator().next();
        final Unit inf2 = infantry.create(1, us).iterator().next();
        final Collection<Unit> units = new ArrayList<Unit>();
        units.add(inf1);
        units.add(inf2);
        assertEquals(can, inf1.getOwner());
        assertEquals(us, inf2.getOwner());
        Change change = ChangeFactory.changeOwner(units, can, m_data.getMap().getTerritory("greenland"));
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(can, inf1.getOwner());
        assertEquals(can, inf2.getOwner());
        change = change.invert();
        changePerformer.perform(change);
        assertEquals(can, inf1.getOwner());
        assertEquals(us, inf2.getOwner());
    }

    public void testPlayerOwnerChangeSerialize() throws Exception {
        final PlayerID can = m_data.getPlayerList().getPlayerID("chretian");
        final PlayerID us = m_data.getPlayerList().getPlayerID("bush");
        final UnitType infantry = m_data.getUnitTypeList().getUnitType("inf");
        final Unit inf1 = infantry.create(1, can).iterator().next();
        final Unit inf2 = infantry.create(1, us).iterator().next();
        final Collection<Unit> units = new ArrayList<Unit>();
        units.add(inf1);
        units.add(inf2);
        assertEquals(can, inf1.getOwner());
        assertEquals(us, inf2.getOwner());
        Change change = ChangeFactory.changeOwner(units, can, m_data.getMap().getTerritory("greenland"));
        change = serialize(change);
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(change);
        assertEquals(can, inf1.getOwner());
        assertEquals(can, inf2.getOwner());
        change = change.invert();
        change = serialize(change);
        changePerformer.perform(change);
        assertEquals(can, inf1.getOwner());
        assertEquals(us, inf2.getOwner());
    }

    public void testChangeProductionFrontier() throws Exception {
        final ProductionFrontier usProd = m_data.getProductionFrontierList().getProductionFrontier("usProd");
        final ProductionFrontier canProd = m_data.getProductionFrontierList().getProductionFrontier("canProd");
        final PlayerID can = m_data.getPlayerList().getPlayerID("chretian");
        assertEquals(can.getProductionFrontier(), canProd);
        Change prodChange = ChangeFactory.changeProductionFrontier(can, usProd);
        final ChangePerformer changePerformer = new ChangePerformer(m_data);
        changePerformer.perform(prodChange);
        assertEquals(can.getProductionFrontier(), usProd);
        prodChange = prodChange.invert();
        changePerformer.perform(prodChange);
        assertEquals(can.getProductionFrontier(), canProd);
        prodChange = serialize(prodChange.invert());
        changePerformer.perform(prodChange);
        assertEquals(can.getProductionFrontier(), usProd);
        prodChange = serialize(prodChange.invert());
        changePerformer.perform(prodChange);
        assertEquals(can.getProductionFrontier(), canProd);
    }

    public void testBlank() {
        final CompositeChange compositeChange = new CompositeChange();
        assertTrue(compositeChange.isEmpty());
        compositeChange.add(new CompositeChange());
        assertTrue(compositeChange.isEmpty());
        final Territory can = m_data.getMap().getTerritory("canada");
        final Collection<Unit> units = Collections.emptyList();
        compositeChange.add(ChangeFactory.removeUnits(can, units));
        assertFalse(compositeChange.isEmpty());
    }
}
