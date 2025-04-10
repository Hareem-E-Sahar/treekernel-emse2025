public class Test {    private void assertRss1Valid(Rdf rdf) throws Exception {
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#", rdf.getXmlnsRdf());
        assertEquals("http://purl.org/rss/1.0/", rdf.getXmlns());
        RdfChannel channel = rdf.getChannels().get(0);
        assertEquals("http://www.xml.com/xml/news.rss", channel.getAbout());
        assertEquals("XML.com", channel.getTitle());
        assertEquals("XML.com features a rich mix of information and services " + "for the XML community.", channel.getDescription());
        assertEquals("http://xml.com/pub", channel.getLink());
        RdfChannelImage rImage = channel.getImage();
        assertEquals("http://xml.com/universal/images/xml_tiny.gif", rImage.getRdfResource());
        List<RdfLi> listItems = channel.getItems().getRdfSeq().getListItems();
        assertEquals(2, listItems.size());
        assertEquals("http://xml.com/pub/2000/08/09/xslt/xslt.html", listItems.get(0).getRdfResource());
        assertEquals("http://xml.com/pub/2000/08/09/rdfdb/index.html", listItems.get(1).getRdfResource());
        assertEquals("http://search.xml.com", channel.getTextinput().getRdfResource());
        RdfImage image = rdf.getImage();
        assertEquals("http://xml.com/universal/images/xml_tiny.gif", image.getAbout());
        assertEquals("XML.com", image.getTitle());
        assertEquals("http://www.xml.com", image.getLink());
        assertEquals("http://xml.com/universal/images/xml_tiny.gif", image.getUrl());
        List<RdfItem> items = rdf.getItems();
        assertEquals(2, items.size());
        RdfItem item1 = items.get(0);
        RdfItem item2 = items.get(1);
        assertEquals("http://xml.com/pub/2000/08/09/xslt/xslt.html", item1.getAbout());
        assertEquals("Processing Inclusions with XSLT", item1.getTitle());
        assertEquals("http://xml.com/pub/2000/08/09/xslt/xslt.html", item1.getLink());
        assertEquals("Processing document inclusions with general XML tools can be " + "problematic. This article proposes a way of preserving inclusion " + "information through SAX-based processing.", item1.getDescription());
        assertEquals("http://xml.com/pub/2000/08/09/rdfdb/index.html", item2.getAbout());
        assertEquals("Putting RDF to Work", item2.getTitle());
        assertEquals("http://xml.com/pub/2000/08/09/rdfdb/index.html", item2.getLink());
        assertEquals("Tool and API support for the Resource Description Framework " + "is slowly coming of age. Edd Dumbill takes a look at RDFDB, " + "one of the most exciting new RDF toolkits.", item2.getDescription());
    }
}