public class Test {    public void testParseEntry() throws ParseException, IOException {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        out.println("#");
        out.println("msgid \"\"");
        out.println("msgstr \"\"");
        out.println("\"Project-Id-Version: PACKAGE VERSION\\n\"");
        out.println("\"POT-Creation-Date: 2001-02-09 01:25+0100\\n\"");
        out.println();
        out.println("#: gpl.xml:11 gpl.xml:30");
        out.println("#, no-c-format");
        out.println("#. Tag: title");
        out.println("msgid \"GNU General Public License\"");
        out.println("msgstr \"test\"");
        out.println();
        out.println("#: gpl.xml:15");
        out.println("#, no-c-format");
        out.println("#, fuzzy");
        out.println("msgid \"Free Software Foundation, Inc.\"");
        out.println("msgstr \"test2\"");
        writer.flush();
        Reader reader = new StringReader(writer.toString());
        POParser parser = new POParser(callback);
        parser.parse(reader);
        assertEquals(2, callback.entries.size());
        ParserEntry entry = callback.entries.get(0);
        assertFalse(entry.isFuzzy());
        assertNotNull("test", entry.getMsgStr());
        assertEquals("gpl.xml:11 gpl.xml:30", entry.getReferences());
        assertEquals("GNU General Public License", entry.getMsgId());
        assertEquals("test", entry.getMsgStr().get(0));
        entry = callback.entries.get(1);
        assertTrue(entry.isFuzzy());
        assertEquals("gpl.xml:15", entry.getReferences());
        assertEquals("Free Software Foundation, Inc.", entry.getMsgId());
        assertEquals("test2", entry.getMsgStr().get(0));
    }
}