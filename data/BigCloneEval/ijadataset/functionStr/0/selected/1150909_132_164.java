public class Test {    public void testParsePluralEntry() throws ParseException, IOException {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        out.println("#");
        out.println("msgid \"\"");
        out.println("msgstr \"\"");
        out.println("\"Project-Id-Version: PACKAGE VERSION\\n\"");
        out.println("\"POT-Creation-Date: 2001-02-09 01:25+0100\\n\"");
        out.println("\"Plural-Forms: nplurals=3; plural=(n != 1);\\n\"");
        out.println();
        out.println("#: gpl.xml:11 gpl.xml:30");
        out.println("#, no-c-format");
        out.println("#. Tag: title");
        out.println("msgid \"GNU General Public License\"");
        out.println("msgid_plural \"GNU General Public Licenses\"");
        out.println("msgstr[0] \"test1\"");
        out.println("msgstr[1] \"test2\"");
        out.println("msgstr[2] \"test3\"");
        writer.flush();
        Reader reader = new StringReader(writer.toString());
        POParser parser = new POParser(callback);
        parser.parse(reader);
        assertEquals(3, callback.nplural);
        assertEquals("plural=(n != 1)", callback.pluralExpression);
        assertEquals(1, callback.entries.size());
        ParserEntry entry = callback.entries.get(0);
        assertEquals("GNU General Public Licenses", entry.getMsgIdPlural());
        assertNotNull(entry.getMsgStr());
        assertEquals(3, entry.getMsgStr().size());
        assertEquals("test1", entry.getMsgStr().get(0));
        assertEquals("test2", entry.getMsgStr().get(1));
        assertEquals("test3", entry.getMsgStr().get(2));
    }
}