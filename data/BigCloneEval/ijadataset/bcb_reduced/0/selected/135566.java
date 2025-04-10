package uk.ac.ebi.intact.psicquic.ws;

import org.apache.cxf.jaxrs.impl.ResponseImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.intact.dataexchange.psimi.solr.CoreNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrIndexer;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.SolrJettyRunner;
import uk.ac.ebi.intact.psicquic.ws.config.PsicquicConfig;
import uk.ac.ebi.intact.psicquic.ws.util.PsicquicStreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: IntactPsicquicRestServiceTest.java 17838 2012-01-30 14:23:55Z brunoaranda $
 */
public class IntactPsicquicRestServiceTest {

    private static PsicquicRestService service;

    private static SolrJettyRunner solrJettyRunner;

    @BeforeClass
    public static void setupSolrPsicquicService() throws Exception {
        solrJettyRunner = new SolrJettyRunner();
        solrJettyRunner.setPort(19876);
        solrJettyRunner.start();
        IntactSolrIndexer indexer = new IntactSolrIndexer(solrJettyRunner.getSolrUrl(CoreNames.CORE_PUB), solrJettyRunner.getSolrUrl(CoreNames.CORE_ONTOLOGY_PUB));
        indexer.indexMitab(IntactPsicquicServiceTest.class.getResourceAsStream("/META-INF/imatinib.mitab.txt"), true);
        indexer.indexMitab(IntactPsicquicServiceTest.class.getResourceAsStream("/META-INF/400.mitab.txt"), true);
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "/META-INF/beans.spring.test.xml" });
        PsicquicConfig config = (PsicquicConfig) context.getBean("testPsicquicConfig");
        config.setSolrServerUrl(solrJettyRunner.getSolrUrl(CoreNames.CORE_PUB));
        service = (IntactPsicquicRestService) context.getBean("intactPsicquicRestService");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        solrJettyRunner.stop();
        solrJettyRunner = null;
        service = null;
    }

    @Test
    public void testGetByQuery() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("imatinib", "tab25", "0", "200", "n");
        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);
        Assert.assertEquals(11, baos.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_maxResults() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("imatinib", "tab25", "0", "3", "n");
        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);
        Assert.assertEquals(3, baos.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_maxResults_above200() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("*", "tab25", "0", "305", "n");
        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);
        Assert.assertEquals(305, baos.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_maxResults_above400() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("*", "tab25", "0", "405", "n");
        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);
        Assert.assertEquals(405, baos.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_firstResult_above200() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("*", "tab25", "150", "255", "n");
        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);
        Assert.assertEquals(255, baos.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_firstResult_above200_max() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("*", "tab25", "250", "500", "n");
        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);
        Assert.assertEquals(160, baos.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_maxResults_nolimit() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("imatinib", "tab25", "0", String.valueOf(Integer.MAX_VALUE), "n");
        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);
        Assert.assertEquals(11, baos.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_bin() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("imatinib", "tab25-bin", "0", "200", "n");
        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        GZIPInputStream gzipInputStream = new GZIPInputStream(bais);
        ByteArrayOutputStream mitabOut = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buf)) > 0) mitabOut.write(buf, 0, len);
        gzipInputStream.close();
        mitabOut.close();
        Assert.assertEquals(11, mitabOut.toString().split("\n").length);
    }
}
