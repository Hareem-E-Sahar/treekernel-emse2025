public class Test {    public void testNoFilter() {
        StringReader reader = new StringReader(html);
        StringWriter writer = new StringWriter();
        try {
            HTMLParser.process(reader, writer, null, true);
            String buffer = new String(writer.toString());
            System.out.println(buffer);
            assertEquals(buffer, "<html><head><title>test</title></head><body><p style=\"font-weight: bold;\">Some text</p></body></html>");
        } catch (HandlingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}