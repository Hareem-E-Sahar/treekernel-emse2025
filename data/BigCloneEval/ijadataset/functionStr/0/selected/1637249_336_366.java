public class Test {    public final void testAddNewSpecificToConfig() throws MarshalException, ValidationException, IOException {
        String snmpConfigXml = "<?xml version=\"1.0\"?>\n" + "<snmp-config retry=\"3\" timeout=\"800\"\n" + "   read-community=\"public\" write-community=\"private\">\n" + "   <definition version=\"v2c\">\n" + "       <specific>192.168.0.5</specific>\n" + "       <specific>192.168.0.6</specific>\n" + "   </definition>\n" + "\n" + "</snmp-config>\n" + "";
        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());
        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.0.6");
        MergeableDefinition configDef = new MergeableDefinition(SnmpPeerFactory.getSnmpConfig().getDefinition(0));
        MergeableDefinition matchingDef = mgr.findDefMatchingAttributes(info.createDef());
        assertNull(matchingDef);
        assertTrue(configDef.hasMatchingSpecific(info.getFirstIPAddress()));
        assertEquals(2, configDef.getConfigDef().getSpecificCount());
        assertEquals(0, configDef.getConfigDef().getRangeCount());
        assertNull(configDef.getConfigDef().getReadCommunity());
        assertEquals("v2c", configDef.getConfigDef().getVersion());
        mgr.mergeIntoConfig(info.createDef());
        matchingDef = mgr.findDefMatchingAttributes(info.createDef());
        assertNotNull(matchingDef);
        assertFalse(configDef.hasMatchingSpecific(info.getFirstIPAddress()));
        assertEquals(1, configDef.getConfigDef().getSpecificCount());
        assertEquals(0, configDef.getConfigDef().getRangeCount());
        assertEquals("v2c", configDef.getConfigDef().getVersion());
        assertEquals(1, matchingDef.getConfigDef().getSpecificCount());
        assertEquals(0, matchingDef.getConfigDef().getRangeCount());
        assertTrue(matchingDef.hasMatchingSpecific(info.getFirstIPAddress()));
        assertNull(matchingDef.getConfigDef().getReadCommunity());
        assertEquals("v1", matchingDef.getConfigDef().getVersion());
        assertTrue(matchingDef.getConfigDef() != configDef.getConfigDef());
        assertEquals(2, SnmpPeerFactory.getSnmpConfig().getDefinitionCount());
    }
}