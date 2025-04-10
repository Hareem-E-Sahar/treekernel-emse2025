public class Test {    public void testXML() throws Exception {
        System.out.println("readFromXML, writeToXML");
        TagsetImpl tagset = new TagsetImpl();
        ArrayList categories = new ArrayList();
        GrammaticalCategory gc1 = createGrammaticalCategory("1");
        GrammaticalCategory gc2 = createGrammaticalCategory("2");
        GrammaticalCategory gc3 = createGrammaticalCategory("3");
        categories.add(gc1);
        categories.add(gc2);
        categories.add(gc3);
        tagset.setGrammaticalCategories(categories);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tagset.writeAsXML(baos);
        byte[] bytes = baos.toByteArray();
        baos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        tagset = new TagsetImpl();
        tagset.readFromXML(bais);
        bais.close();
        Collection result = tagset.getGrammaticalCategories();
        assertTrue(result.size() == 3);
        assertTrue(result.contains(gc1));
        assertTrue(result.contains(gc2));
        assertTrue(result.contains(gc3));
        assertEquals(gc1, tagset.getGrammaticalCategory("category_1"));
        assertEquals(gc2, tagset.getGrammaticalCategory("category_2"));
        assertEquals(gc3, tagset.getGrammaticalCategory("category_3"));
        assertEquals(createGrammaticalTag("1.1"), tagset.getTag("tag_1.1"));
        assertEquals(createGrammaticalTag("1.2"), tagset.getTag("tag_1.2"));
        assertEquals(createGrammaticalTag("1.3"), tagset.getTag("tag_1.3"));
        assertEquals(createGrammaticalTag("2.1"), tagset.getTag("tag_2.1"));
        assertEquals(createGrammaticalTag("2.2"), tagset.getTag("tag_2.2"));
        assertEquals(createGrammaticalTag("2.3"), tagset.getTag("tag_2.3"));
        assertEquals(createGrammaticalTag("3.1"), tagset.getTag("tag_3.1"));
        assertEquals(createGrammaticalTag("3.2"), tagset.getTag("tag_3.2"));
        assertEquals(createGrammaticalTag("3.3"), tagset.getTag("tag_3.3"));
    }
}