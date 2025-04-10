public class Test {    public void testEnmueration_04() throws Exception {
        EXISchema corpus = EXISchemaFactoryTestUtil.getEXISchema("/interop/datatypes/enumeration/enumeration.xsd", getClass(), m_compilerErrors);
        Assert.assertEquals(0, m_compilerErrors.getTotalCount());
        GrammarCache grammarCache = new GrammarCache(corpus, GrammarOptions.STRICT_OPTIONS);
        String exiFiles = "/interop/datatypes/enumeration/enumeration-valid-04.byteAligned";
        EXIDecoder decoder = new EXIDecoder();
        Scanner scanner;
        decoder.setAlignmentType(AlignmentType.byteAligned);
        URL url = resolveSystemIdAsURL(exiFiles);
        int n_events;
        decoder.setEXISchema(grammarCache);
        decoder.setInputStream(url.openStream());
        scanner = decoder.processHeader();
        ArrayList<EXIEvent> exiEventList = new ArrayList<EXIEvent>();
        EXIEvent exiEvent;
        n_events = 0;
        while ((exiEvent = scanner.nextEvent()) != null) {
            ++n_events;
            exiEventList.add(exiEvent);
        }
        Assert.assertEquals(10, n_events);
        EventType eventType;
        EventTypeList eventTypeList;
        int pos = 0;
        exiEvent = exiEventList.get(pos++);
        Assert.assertEquals(EXIEvent.EVENT_SD, exiEvent.getEventVariety());
        eventType = exiEvent.getEventType();
        Assert.assertSame(exiEvent, eventType);
        Assert.assertEquals(0, eventType.getIndex());
        eventTypeList = eventType.getEventTypeList();
        Assert.assertEquals(1, eventTypeList.getLength());
        exiEvent = exiEventList.get(pos++);
        Assert.assertEquals(EXIEvent.EVENT_SE, exiEvent.getEventVariety());
        Assert.assertEquals("root", exiEvent.getName());
        Assert.assertEquals("", exiEvent.getURI());
        exiEvent = exiEventList.get(pos++);
        Assert.assertEquals(EXIEvent.EVENT_SE, exiEvent.getEventVariety());
        Assert.assertEquals("union", exiEvent.getName());
        Assert.assertEquals("", exiEvent.getURI());
        exiEvent = exiEventList.get(pos++);
        Assert.assertEquals(EXIEvent.EVENT_CH, exiEvent.getEventVariety());
        Assert.assertEquals("+10", exiEvent.getCharacters().makeString());
        exiEvent = exiEventList.get(pos++);
        Assert.assertEquals(EXIEvent.EVENT_EE, exiEvent.getEventVariety());
        exiEvent = exiEventList.get(pos++);
        Assert.assertEquals(EXIEvent.EVENT_SE, exiEvent.getEventVariety());
        Assert.assertEquals("union", exiEvent.getName());
        Assert.assertEquals("", exiEvent.getURI());
        exiEvent = exiEventList.get(pos++);
        Assert.assertEquals(EXIEvent.EVENT_CH, exiEvent.getEventVariety());
        Assert.assertEquals("12:32:00", exiEvent.getCharacters().makeString());
        exiEvent = exiEventList.get(pos++);
        Assert.assertEquals(EXIEvent.EVENT_EE, exiEvent.getEventVariety());
        exiEvent = exiEventList.get(pos++);
        Assert.assertEquals(EXIEvent.EVENT_EE, exiEvent.getEventVariety());
        exiEvent = exiEventList.get(pos++);
        Assert.assertEquals(EXIEvent.EVENT_ED, exiEvent.getEventVariety());
    }
}