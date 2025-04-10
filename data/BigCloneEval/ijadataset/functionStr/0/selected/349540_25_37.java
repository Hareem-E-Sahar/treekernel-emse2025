public class Test {    public void testXMLConversion() {
        StringReader reader = new StringReader(html);
        StringWriter writer = new StringWriter();
        try {
            HTMLParser.process(reader, writer, this, false);
            String buffer = new String(writer.toString());
            System.out.println(buffer);
            assertEquals(buffer, "<html><head><title>test</title></head><body><img src=\"http://images.google.com\"></body></html>");
        } catch (HandlingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}