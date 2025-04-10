package com.google.gson;

import com.google.gson.common.TestTypes.BagOfPrimitives;
import junit.framework.TestCase;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.StringReader;

/**
 * Unit test for {@link JsonParser}
 * 
 * @author Inderjeet Singh
 */
public class JsonParserTest extends TestCase {

    private JsonParser parser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = new JsonParser();
    }

    public void testParseString() {
        String json = "{a:10,b:'c'}";
        JsonElement e = parser.parse(json);
        assertTrue(e.isJsonObject());
        assertEquals(10, e.getAsJsonObject().get("a").getAsInt());
        assertEquals("c", e.getAsJsonObject().get("b").getAsString());
    }

    public void testParseEmptyString() {
        JsonElement e = parser.parse("\"   \"");
        assertTrue(e.isJsonPrimitive());
        assertEquals("   ", e.getAsString());
    }

    public void testParseEmptyWhitespaceInput() {
        JsonElement e = parser.parse("     ");
        assertTrue(e.isJsonNull());
    }

    public void testParseMixedArray() {
        String json = "[{},13,\"stringValue\"]";
        JsonElement e = parser.parse(json);
        assertTrue(e.isJsonArray());
        JsonArray array = e.getAsJsonArray();
        assertEquals("{}", array.get(0).toString());
        assertEquals(13, array.get(1).getAsInt());
        assertEquals("stringValue", array.get(2).getAsString());
    }

    public void testParseReader() {
        StringReader reader = new StringReader("{a:10,b:'c'}");
        JsonElement e = parser.parse(reader);
        assertTrue(e.isJsonObject());
        assertEquals(10, e.getAsJsonObject().get("a").getAsInt());
        assertEquals("c", e.getAsJsonObject().get("b").getAsString());
    }

    public void testReadWriteTwoObjects() throws Exception {
        Gson gson = new Gson();
        CharArrayWriter writer = new CharArrayWriter();
        BagOfPrimitives expectedOne = new BagOfPrimitives(1, 1, true, "one");
        writer.write(gson.toJson(expectedOne).toCharArray());
        BagOfPrimitives expectedTwo = new BagOfPrimitives(2, 2, false, "two");
        writer.write(gson.toJson(expectedTwo).toCharArray());
        CharArrayReader reader = new CharArrayReader(writer.toCharArray());
        JsonParserJavacc parser = new JsonParserJavacc(reader);
        JsonElement element1 = parser.parse();
        JsonElement element2 = parser.parse();
        BagOfPrimitives actualOne = gson.fromJson(element1, BagOfPrimitives.class);
        assertEquals("one", actualOne.stringValue);
        BagOfPrimitives actualTwo = gson.fromJson(element2, BagOfPrimitives.class);
        assertEquals("two", actualTwo.stringValue);
    }
}
