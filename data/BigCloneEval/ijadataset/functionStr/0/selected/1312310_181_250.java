public class Test {    public void testSerialization() throws Exception {
        Style style = new CamelCaseStyle();
        Format format = new Format(style);
        Persister camelPersister = new Persister(format);
        Persister persister = new Persister();
        ExampleAssembledType example = new ExampleAssembledType();
        StringWriter writer = new StringWriter();
        StringWriter camelWriter = new StringWriter();
        example.setA("This is a");
        example.setB("This is b");
        example.setC("This is c");
        example.setD("This is d");
        example.setE("This is e");
        example.setF("This is f");
        example.setG("This is g");
        example.setH("This is h");
        example.setFirst("The first element");
        example.setSecond("The second element");
        example.setThird("The third element");
        example.setFourth("The fourth element");
        example.setFifth("The fifth element");
        example.setSixth("The sixth element");
        example.setSeventh("The seventh element");
        example.setEight("The eight element");
        example.setNinth("The ninth element");
        example.setTenth("The tenth element");
        camelPersister.write(example, System.err);
        persister.write(example, System.out);
        persister.write(example, writer);
        camelPersister.write(example, camelWriter);
        ExampleAssembledType recovered = persister.read(ExampleAssembledType.class, writer.toString());
        ExampleAssembledType camelRecovered = camelPersister.read(ExampleAssembledType.class, camelWriter.toString());
        assertEquals(recovered.a, example.a);
        assertEquals(recovered.b, example.b);
        assertEquals(recovered.c, example.c);
        assertEquals(recovered.d, example.d);
        assertEquals(recovered.e, example.e);
        assertEquals(recovered.f, example.f);
        assertEquals(recovered.g, example.g);
        assertEquals(recovered.h, example.h);
        assertEquals(recovered.first, example.first);
        assertEquals(recovered.second, example.second);
        assertEquals(recovered.third, example.third);
        assertEquals(recovered.fourth, example.fourth);
        assertEquals(recovered.fifth, example.fifth);
        assertEquals(recovered.sixth, example.sixth);
        assertEquals(recovered.seventh, example.seventh);
        assertEquals(recovered.eight, example.eight);
        assertEquals(recovered.ninth, example.ninth);
        assertEquals(recovered.tenth, example.tenth);
        assertEquals(camelRecovered.a, example.a);
        assertEquals(camelRecovered.b, example.b);
        assertEquals(camelRecovered.c, example.c);
        assertEquals(camelRecovered.d, example.d);
        assertEquals(camelRecovered.e, example.e);
        assertEquals(camelRecovered.f, example.f);
        assertEquals(camelRecovered.g, example.g);
        assertEquals(camelRecovered.h, example.h);
        assertEquals(camelRecovered.first, example.first);
        assertEquals(camelRecovered.second, example.second);
        assertEquals(camelRecovered.third, example.third);
        assertEquals(camelRecovered.fourth, example.fourth);
        assertEquals(camelRecovered.fifth, example.fifth);
        assertEquals(camelRecovered.sixth, example.sixth);
        assertEquals(camelRecovered.seventh, example.seventh);
        assertEquals(camelRecovered.eight, example.eight);
        assertEquals(camelRecovered.ninth, example.ninth);
        assertEquals(camelRecovered.tenth, example.tenth);
        validate(example, persister);
    }
}