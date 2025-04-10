package games.strategy.triplea.delegate;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.GameParser;
import games.strategy.engine.data.ITestDelegateBridge;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Resource;
import games.strategy.engine.data.Territory;
import games.strategy.engine.data.UnitType;
import games.strategy.triplea.Constants;
import games.strategy.triplea.attatchments.TechAttachment;
import java.io.InputStream;
import java.net.URL;
import junit.framework.TestCase;

/**
 * 
 * @author Sean Bridges
 * @version 1.0
 */
public class DelegateTest extends TestCase {

    protected GameData m_data;

    protected PlayerID british;

    protected PlayerID japanese;

    protected PlayerID russians;

    protected PlayerID germans;

    protected Territory northSea;

    protected Territory uk;

    protected Territory germany;

    protected Territory japan;

    protected Territory brazil;

    protected Territory westCanada;

    protected Territory egypt;

    protected Territory congo;

    protected Territory kenya;

    protected Territory blackSea;

    protected Territory eastAfrica;

    protected Territory syria;

    protected Territory manchuria;

    protected Territory japanSeaZone;

    protected Territory sfeSeaZone;

    protected Territory libya;

    protected Territory algeria;

    protected Territory equatorialAfrica;

    protected Territory redSea;

    protected Territory westAfrica;

    protected Territory angola;

    protected Territory angolaSeaZone;

    protected Territory eastCompass;

    protected Territory westCompass;

    protected Territory mozambiqueSeaZone;

    protected Territory eastMediteranean;

    protected Territory congoSeaZone;

    protected Territory northAtlantic;

    protected Territory redAtlantic;

    protected Territory westAfricaSea;

    protected Territory indianOcean;

    protected Territory westAfricaSeaZone;

    protected Territory southAfrica;

    protected Territory saudiArabia;

    protected Territory india;

    protected Territory southAtlantic;

    protected Territory southAfricaSeaZone;

    protected Territory antarticSea;

    protected Territory southBrazilSeaZone;

    protected Territory spain;

    protected Territory gibraltar;

    protected Territory russia;

    protected Territory balticSeaZone;

    protected Territory karelia;

    protected Territory westEurope;

    protected Territory finlandNorway;

    protected UnitType armour;

    protected UnitType infantry;

    protected UnitType transport;

    protected UnitType submarine;

    protected UnitType factory;

    protected UnitType aaGun;

    protected UnitType fighter;

    protected UnitType bomber;

    protected UnitType carrier;

    protected Resource PUs;

    /** Creates new PlaceDelegateTest */
    public DelegateTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        final URL url = this.getClass().getResource("DelegateTest.xml");
        final InputStream input = url.openStream();
        m_data = (new GameParser()).parse(input, false);
        input.close();
        british = m_data.getPlayerList().getPlayerID("British");
        british.addAttachment(Constants.TECH_ATTACHMENT_NAME, new TechAttachment());
        japanese = m_data.getPlayerList().getPlayerID("Japanese");
        japanese.addAttachment(Constants.TECH_ATTACHMENT_NAME, new TechAttachment());
        russians = m_data.getPlayerList().getPlayerID("Russians");
        russians.addAttachment(Constants.TECH_ATTACHMENT_NAME, new TechAttachment());
        germans = m_data.getPlayerList().getPlayerID("Germans");
        germans.addAttachment(Constants.TECH_ATTACHMENT_NAME, new TechAttachment());
        northSea = m_data.getMap().getTerritory("North Sea Zone");
        blackSea = m_data.getMap().getTerritory("Black Sea Zone");
        uk = m_data.getMap().getTerritory("United Kingdom");
        japan = m_data.getMap().getTerritory("Japan");
        japanSeaZone = m_data.getMap().getTerritory("Japan Sea Zone");
        sfeSeaZone = m_data.getMap().getTerritory("Soviet Far East Sea Zone");
        brazil = m_data.getMap().getTerritory("Brazil");
        westCanada = m_data.getMap().getTerritory("West Canada");
        germany = m_data.getMap().getTerritory("Germany");
        syria = m_data.getMap().getTerritory("Syria Jordan");
        manchuria = m_data.getMap().getTerritory("Manchuria");
        egypt = m_data.getMap().getTerritory("Anglo Sudan Egypt");
        congo = m_data.getMap().getTerritory("Congo");
        congoSeaZone = m_data.getMap().getTerritory("Congo Sea Zone");
        northAtlantic = m_data.getMap().getTerritory("North Atlantic Sea Zone");
        westAfricaSea = m_data.getMap().getTerritory("West Africa Sea Zone");
        kenya = m_data.getMap().getTerritory("Kenya-Rhodesia");
        eastAfrica = m_data.getMap().getTerritory("Italian East Africa");
        libya = m_data.getMap().getTerritory("Libya");
        algeria = m_data.getMap().getTerritory("Algeria");
        equatorialAfrica = m_data.getMap().getTerritory("French Equatorial Africa");
        redSea = m_data.getMap().getTerritory("Red Sea Zone");
        westAfrica = m_data.getMap().getTerritory("French West Africa");
        angola = m_data.getMap().getTerritory("Angola");
        angolaSeaZone = m_data.getMap().getTerritory("Angola Sea Zone");
        eastCompass = m_data.getMap().getTerritory("East Compass Sea Zone");
        westCompass = m_data.getMap().getTerritory("West Compass Sea Zone");
        mozambiqueSeaZone = m_data.getMap().getTerritory("Mozambique Sea Zone");
        eastMediteranean = m_data.getMap().getTerritory("East Mediteranean Sea Zone");
        indianOcean = m_data.getMap().getTerritory("Indian Ocean Sea Zone");
        westAfricaSeaZone = m_data.getMap().getTerritory("West Africa Sea Zone");
        southAfrica = m_data.getMap().getTerritory("South Africa");
        saudiArabia = m_data.getMap().getTerritory("Saudi Arabia");
        india = m_data.getMap().getTerritory("India");
        southAtlantic = m_data.getMap().getTerritory("South Atlantic Sea Zone");
        antarticSea = m_data.getMap().getTerritory("Antartic Sea Zone");
        southAfricaSeaZone = m_data.getMap().getTerritory("South Africa Sea Zone");
        southBrazilSeaZone = m_data.getMap().getTerritory("South Brazil Sea Zone");
        russia = m_data.getMap().getTerritory("Russia");
        spain = m_data.getMap().getTerritory("Spain");
        gibraltar = m_data.getMap().getTerritory("Gibraltar");
        balticSeaZone = m_data.getMap().getTerritory("Baltic Sea Zone");
        karelia = m_data.getMap().getTerritory("Karelia S.S.R.");
        westEurope = m_data.getMap().getTerritory("West Europe");
        finlandNorway = m_data.getMap().getTerritory("Finland Norway");
        armour = m_data.getUnitTypeList().getUnitType("armour");
        infantry = m_data.getUnitTypeList().getUnitType("infantry");
        transport = m_data.getUnitTypeList().getUnitType("transport");
        submarine = m_data.getUnitTypeList().getUnitType("submarine");
        factory = m_data.getUnitTypeList().getUnitType("factory");
        aaGun = m_data.getUnitTypeList().getUnitType("aaGun");
        fighter = m_data.getUnitTypeList().getUnitType("fighter");
        bomber = m_data.getUnitTypeList().getUnitType("bomber");
        carrier = m_data.getUnitTypeList().getUnitType("carrier");
        PUs = m_data.getResourceList().getResource("PUs");
    }

    public void assertValid(final String string) {
        assertNull(string, string);
    }

    public void assertError(final String string) {
        assertNotNull(string, string);
    }

    protected ITestDelegateBridge getDelegateBridge(final PlayerID player) {
        return GameDataTestUtil.getDelegateBridge(player);
    }

    public void testTest() {
        assertValid(null);
        assertError("Can not do this");
    }
}
