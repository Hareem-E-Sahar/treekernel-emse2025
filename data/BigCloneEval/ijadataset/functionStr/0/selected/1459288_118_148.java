public class Test {    public void testMixedDuplicates() throws Exception {
        DeleteDuplicates dedup = new DeleteDuplicates(conf);
        dedup.dedup(new Path[] { index1, index2 });
        FsDirectory dir = new FsDirectory(fs, new Path(index1, "part-0000"), false, conf);
        IndexReader reader = IndexReader.open(dir);
        assertEquals("only one doc left", reader.numDocs(), 1);
        for (int i = 0; i < reader.maxDoc(); i++) {
            if (reader.isDeleted(i)) {
                System.out.println("-doc " + i + " deleted");
                continue;
            }
            Document doc = reader.document(i);
            assertEquals("check url", "http://www.example.com/2", doc.get("url"));
            System.out.println(doc);
        }
        reader.close();
        dir = new FsDirectory(fs, new Path(index2, "part-0000"), false, conf);
        reader = IndexReader.open(dir);
        assertEquals("only one doc left", reader.numDocs(), 1);
        MD5Hash hash = MD5Hash.digest("2");
        for (int i = 0; i < reader.maxDoc(); i++) {
            if (reader.isDeleted(i)) {
                System.out.println("-doc " + i + " deleted");
                continue;
            }
            Document doc = reader.document(i);
            assertEquals("check hash", hash.toString(), doc.get("digest"));
            System.out.println(doc);
        }
        reader.close();
    }
}