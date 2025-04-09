package ostf.gui.frame.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.grinder.common.Logger;
import net.grinder.plugin.http.tcpproxyfilter.ConnectionCache;
import net.grinder.plugin.http.tcpproxyfilter.ConnectionHandlerFactoryImplementation;
import net.grinder.plugin.http.tcpproxyfilter.HTTPRecordingImplementation;
import net.grinder.plugin.http.tcpproxyfilter.HTTPRequestFilter;
import net.grinder.plugin.http.tcpproxyfilter.HTTPResponseFilter;
import net.grinder.plugin.http.tcpproxyfilter.ProcessHTTPRecordingWithXSLT;
import net.grinder.plugin.http.tcpproxyfilter.RegularExpressionsImplementation;
import net.grinder.tools.tcpproxy.CommentSourceImplementation;
import net.grinder.tools.tcpproxy.CompositeFilter;
import net.grinder.tools.tcpproxy.EchoFilter;
import net.grinder.tools.tcpproxy.EndPoint;
import net.grinder.tools.tcpproxy.HTTPProxyTCPProxyEngine;
import net.grinder.tools.tcpproxy.TCPProxyEngine;
import net.grinder.tools.tcpproxy.TCPProxyFilter;
import net.grinder.tools.tcpproxy.TCPProxySSLSocketFactory;
import net.grinder.tools.tcpproxy.TCPProxySSLSocketFactoryImplementation;
import net.grinder.util.AttributeStringParserImplementation;
import net.grinder.util.SimpleStringEscaper;
import net.grinder.util.URIParserImplementation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.defaults.DefaultPicoContainer;

public class HTTPRecorder {

    public static Log logger = LogFactory.getLog(HTTPRecorder.class.getName());

    public static final int TESTPLAN_ONLY = 1;

    public static final int RAW_ONLY = 2;

    public static final int TESTPLAN_AND_RAW = 3;

    public static final String rawFile = "HTTPRecorder.txt";

    public static final String testPlanFile = "HTTPRecorder.tpl";

    private int port = 8001;

    private int mode = 1;

    private DefaultPicoContainer m_filterContainer = new DefaultPicoContainer();

    private RecorderLogger rawLogger = null;

    private EchoFilter rawFilter = null;

    private ByteArrayOutputStream boStream = new ByteArrayOutputStream();

    private Logger recorderLogger = new RecorderLogger(new PrintWriter(boStream), new PrintWriter(System.err));

    private TCPProxyEngine m_proxyEngine;

    private Runnable shutdown = new Runnable() {

        private boolean m_stopped = false;

        public synchronized void run() {
            if (!m_stopped) {
                m_stopped = true;
                m_proxyEngine.stop();
                m_filterContainer.stop();
                m_filterContainer.dispose();
                if (TESTPLAN_ONLY != mode) {
                    rawLogger.getOutputLogWriter().flush();
                    rawLogger.getOutputLogWriter().close();
                }
                if (RAW_ONLY != mode) {
                    getTestPlanFromRecorder();
                }
                logger.info("HTTPProxy shuts down successfully");
                logger.info("HTTPRecorder stops");
            }
        }
    };

    private void getTestPlanFromRecorder() {
        TransformerFactory m_transformerFactory = TransformerFactory.newInstance();
        m_transformerFactory.setErrorListener(new ErrorListener() {

            public void warning(TransformerException e) throws TransformerException {
                logger.warn("Warning", e);
            }

            public void error(TransformerException e) throws TransformerException {
                logger.error("Error", e);
            }

            public void fatalError(TransformerException e) throws TransformerException {
                logger.error("Fatal", e);
            }
        });
        FileOutputStream resultStream = null;
        try {
            resultStream = new FileOutputStream(new File(testPlanFile));
            Transformer transformer = m_transformerFactory.newTransformer(new StreamSource(new FileInputStream("resource/grinder/recorder.xsl")));
            transformer.transform(new StreamSource(new ByteArrayInputStream(boStream.toByteArray())), new StreamResult(resultStream));
        } catch (Exception e) {
            logger.error("Failed to transform recording to test plan", e);
        } finally {
            if (boStream != null) try {
                boStream.close();
            } catch (IOException e1) {
            }
            if (resultStream != null) try {
                resultStream.close();
            } catch (IOException e1) {
            }
        }
    }

    public HTTPRecorder(int port, int mode, String ksFile, String ksPassword, String ksType) throws Exception {
        this.port = port;
        this.mode = mode;
        if (TESTPLAN_ONLY != mode) {
            rawLogger = new RecorderLogger(new PrintWriter(new FileWriter(rawFile)), null);
            rawFilter = new EchoFilter(rawLogger);
        }
        EndPoint localEndPoint = new EndPoint("localhost", port);
        TCPProxySSLSocketFactory sslSocketFactory = null;
        if (ksFile != null) {
            File keyStoreFile = new File(ksFile);
            char[] keyStorePassword = ksPassword != null ? ksPassword.toCharArray() : new char[0];
            sslSocketFactory = new TCPProxySSLSocketFactoryImplementation(keyStoreFile, keyStorePassword, ksType);
        } else sslSocketFactory = new TCPProxySSLSocketFactoryImplementation();
        initFilterContainer();
        TCPProxyFilter requestFilter = getTCPProxyFilter(true);
        TCPProxyFilter responseFilter = getTCPProxyFilter(false);
        m_filterContainer.start();
        m_proxyEngine = new HTTPProxyTCPProxyEngine(sslSocketFactory, requestFilter, responseFilter, recorderLogger, localEndPoint, false, 0, null, null);
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(shutdown));
        Runnable startThread = new Runnable() {

            public void run() {
                m_proxyEngine.run();
                shutdown.run();
            }
        };
        new Thread(startThread).start();
        logger.info("HTTPProxy warms up and is listening on port " + port);
        logger.info("HTTPRecorder starts");
    }

    public void stop() {
        shutdown.run();
    }

    private void initFilterContainer() throws Exception {
        m_filterContainer.registerComponentInstance(recorderLogger);
        m_filterContainer.registerComponentInstance(new CommentSourceImplementation());
        m_filterContainer.registerComponentImplementation(AttributeStringParserImplementation.class);
        m_filterContainer.registerComponentImplementation(ConnectionCache.class);
        m_filterContainer.registerComponentImplementation(ConnectionHandlerFactoryImplementation.class);
        m_filterContainer.registerComponentImplementation(HTTPRecordingImplementation.class);
        m_filterContainer.registerComponentImplementation(ProcessHTTPRecordingWithXSLT.class);
        m_filterContainer.registerComponentImplementation(RegularExpressionsImplementation.class);
        m_filterContainer.registerComponentImplementation(URIParserImplementation.class);
        m_filterContainer.registerComponentImplementation(SimpleStringEscaper.class);
        m_filterContainer.registerComponentInstance(new ProcessHTTPRecordingWithXSLT.StyleSheetInputStream(new File("resource/grinder/httpToXML.xsl")));
    }

    private TCPProxyFilter getTCPProxyFilter(boolean request) throws Exception {
        if (RAW_ONLY == mode) {
            return rawFilter;
        } else {
            CompositeFilter result = new CompositeFilter();
            ComponentAdapter adapter = null;
            if (request) {
                adapter = m_filterContainer.registerComponentImplementation(HTTPRequestFilter.class);
                result.add((TCPProxyFilter) adapter.getComponentInstance(m_filterContainer));
            } else {
                adapter = m_filterContainer.registerComponentImplementation(HTTPResponseFilter.class);
                result.add((TCPProxyFilter) adapter.getComponentInstance(m_filterContainer));
            }
            if (TESTPLAN_AND_RAW == mode) result.add(rawFilter);
            return result;
        }
    }

    class RecorderLogger implements Logger {

        private PrintWriter m_outputWriter;

        private PrintWriter m_errorWriter;

        public RecorderLogger(PrintWriter outputWriter, PrintWriter errorWriter) {
            m_outputWriter = outputWriter;
            m_errorWriter = errorWriter;
        }

        public void output(String message) {
            logger.info(message);
        }

        public void output(String message, int where) {
            logger.info(message);
        }

        public void error(String message) {
            logger.error(message);
        }

        public void error(String message, int where) {
            logger.error(message);
        }

        public PrintWriter getOutputLogWriter() {
            return m_outputWriter;
        }

        public PrintWriter getErrorLogWriter() {
            return m_errorWriter;
        }
    }
}
