package org.solrmarc.solr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.solrmarc.tools.Utils;
import org.xml.sax.InputSource;

public class SolrCoreLoader {

    public static SolrProxy loadCore(String solrCoreDir, String solrDataDir, String solrCoreName, Logger logger) {
        return loadEmbeddedCore(solrCoreDir, solrDataDir, solrCoreName, true, logger);
    }

    public static SolrProxy loadEmbeddedCore(String solrCoreDir, String solrDataDir, String solrCoreName, boolean useBinaryRequestHandler, Logger logger) {
        try {
            Object solrCoreObj = null;
            Class<?> coreContainerClass = Class.forName("org.apache.solr.core.CoreContainer");
            Object coreContainerObj = null;
            File multicoreConfigFile = new File(solrCoreDir + "/solr.xml");
            if (multicoreConfigFile.exists()) {
                logger.info("Using the multicore schema file at : " + multicoreConfigFile.getAbsolutePath());
                logger.info("Using the " + solrCoreName + " core");
                if (solrDataDir == null) {
                    solrDataDir = solrCoreDir + "/" + solrCoreName;
                }
                System.setProperty("solr.data.dir", solrDataDir);
                logger.info("Using the data directory of: " + solrDataDir);
                Constructor<?> coreContainerConstructor = coreContainerClass.getConstructor(String.class, File.class);
                coreContainerObj = coreContainerConstructor.newInstance(solrCoreDir, multicoreConfigFile);
                Method getCoreMethod = coreContainerClass.getMethod("getCore", String.class);
                solrCoreObj = getCoreMethod.invoke(coreContainerObj, solrCoreName);
            } else {
                if (solrDataDir == null) {
                    solrDataDir = solrCoreDir + "/" + "data";
                }
                System.setProperty("solr.data.dir", solrDataDir);
                Class<?> solrConfigClass = Class.forName("org.apache.solr.core.SolrConfig");
                Constructor<?> solrConfigConstructor = null;
                try {
                    solrConfigConstructor = solrConfigClass.getConstructor(String.class, String.class, InputStream.class);
                } catch (NoSuchMethodException e) {
                    solrConfigConstructor = solrConfigClass.getConstructor(String.class, String.class, InputSource.class);
                }
                Object solrConfig = solrConfigConstructor.newInstance(solrCoreDir, "solrconfig.xml", null);
                Class<?> indexSchemaClass = Class.forName("org.apache.solr.schema.IndexSchema");
                Constructor<?> IndexSchemaConstructor = null;
                try {
                    IndexSchemaConstructor = indexSchemaClass.getConstructor(solrConfigClass, String.class, InputStream.class);
                } catch (NoSuchMethodException e) {
                    IndexSchemaConstructor = indexSchemaClass.getConstructor(solrConfigClass, String.class, InputSource.class);
                }
                Object solrSchema = IndexSchemaConstructor.newInstance(solrConfig, "schema.xml", null);
                Constructor<?> coreContainerConstructor = coreContainerClass.getConstructor();
                coreContainerObj = coreContainerConstructor.newInstance();
                solrCoreName = "Solr";
                Class<?> coreDescClass = Class.forName("org.apache.solr.core.CoreDescriptor");
                Constructor<?> coreDescConstructor = coreDescClass.getConstructor(coreContainerClass, String.class, String.class);
                Object coreDescObj = coreDescConstructor.newInstance(coreContainerObj, solrCoreName, solrCoreDir + "/conf");
                Class<?> solrCoreClass = Class.forName("org.apache.solr.core.SolrCore");
                Constructor<?> solrCoreConstructor = solrCoreClass.getConstructor(String.class, String.class, solrConfigClass, indexSchemaClass, coreDescClass);
                solrCoreObj = solrCoreConstructor.newInstance(solrCoreName, solrDataDir, solrConfig, solrSchema, coreDescObj);
                coreContainerClass.getMethod("register", String.class, solrCoreClass, boolean.class).invoke(coreContainerObj, solrCoreName, solrCoreObj, false);
            }
            Object solrServerObj = null;
            if (useBinaryRequestHandler) {
                Class<?> embeddedSolrServerClass = Class.forName("org.solrmarc.solr.embedded.SolrServerEmbeddedImpl");
                Constructor<?> embeddedSolrServerConstructor = embeddedSolrServerClass.getConstructor(Object.class, Object.class);
                solrServerObj = embeddedSolrServerConstructor.newInstance(solrCoreObj, coreContainerObj);
            } else {
                try {
                    Class<?> embeddedSolrServerClass = Class.forName("org.apache.solr.client.solrj.embedded.EmbeddedSolrServer");
                    Constructor<?> embeddedSolrServerConstructor = embeddedSolrServerClass.getConstructor(coreContainerClass, String.class);
                    solrServerObj = embeddedSolrServerConstructor.newInstance(coreContainerObj, solrCoreName);
                } catch (Exception e) {
                    if (e instanceof ClassNotFoundException || (e instanceof InvocationTargetException && e.getCause() instanceof java.lang.NoClassDefFoundError)) {
                        logger.error("Error loading class:org.apache.solr.client.solrj.embedded.EmbeddedSolrServer : " + e.getCause());
                        Class<?> embeddedSolrServerClass = Class.forName("org.solrmarc.solr.embedded.SolrServerEmbeddedImpl");
                        Constructor<?> embeddedSolrServerConstructor = embeddedSolrServerClass.getConstructor(Object.class, Object.class);
                        solrServerObj = embeddedSolrServerConstructor.newInstance(solrCoreObj, coreContainerObj);
                    } else {
                        logger.error("Error loading class:org.apache.solr.client.solrj.embedded.EmbeddedSolrServer : " + e.getCause());
                        e.printStackTrace();
                    }
                }
            }
            return (new SolrServerProxy((SolrServer) solrServerObj, coreContainerObj));
        } catch (Exception e) {
            System.err.println("Error: Problem instantiating SolrCore");
            logger.error("Error: Problem instantiating SolrCore");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public static SolrProxy loadRemoteSolrServer(String solrHostUpdateURL, boolean useBinaryRequestHandler, boolean useStreamingServer) {
        CommonsHttpSolrServer httpsolrserver;
        SolrProxy solrProxy = null;
        String urlString = solrHostUpdateURL.replaceAll("[/\\\\]update$", "");
        try {
            Class<?> clazz = Class.forName("org.apache.solr.client.solrj.impl.ResponseParserFactory");
            if (useStreamingServer) {
                httpsolrserver = new StreamingUpdateSolrServer(urlString, 100, 2);
            } else {
                httpsolrserver = new CommonsHttpSolrServer(urlString);
            }
            if (!useBinaryRequestHandler) {
                httpsolrserver.setRequestWriter(new RequestWriter());
                httpsolrserver.setParser(new XMLResponseParser());
            }
            solrProxy = new SolrServerProxy(httpsolrserver);
            return (solrProxy);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return (null);
    }
}
