package de.ddb.conversion.ssh;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.jcraft.jsch.JSchException;
import de.ddb.charset.CharsetUtil;
import de.ddb.conversion.BinaryConverter;

/**
 * @author heck
 *
 */
public class RemoteExecTest {

    private static final Log LOGGER = LogFactory.getLog(RemoteExecTest.class);

    private static RemoteExec remoteExec;

    private static String username;

    private static String password;

    private static String host;

    private static int port = 22;

    /**
	 * 
	 */
    public int success = 0;

    /**
	 * 
	 */
    public int error = 0;

    /**
	 * @throws Exception
	 */
    @BeforeClass
    public static void setUp() throws Exception {
        remoteExec = new RemoteExec();
        try {
            ResourceBundle config = ResourceBundle.getBundle("de.ddb.conversion.config.converter");
            host = config.getString(BinaryConverter.PROPKEY_HOST);
            username = config.getString(BinaryConverter.PROPKEY_USER);
            password = config.getString(BinaryConverter.PROPKEY_PASSWORD);
            remoteExec.setHost(host);
            remoteExec.setPassword(password);
            remoteExec.setUser(username);
            remoteExec.setPort(port);
            LOGGER.debug("Initialized from config.");
        } catch (MissingResourceException e) {
            LOGGER.info("No config found.");
        }
    }

    /**
	 * @throws Exception
	 */
    @AfterClass
    public static void tearDown() throws Exception {
        if (remoteExec != null) {
            remoteExec.disconnect();
        }
        remoteExec = null;
    }

    /**
	 * 
	 */
    @Test
    public void testNumberOfconcurrentConnections() {
        ExecutorService threadPool = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 40; i++) {
            ConnectionThread run = new ConnectionThread();
            run.setHost(host);
            run.setPassword(password);
            run.setPort(port);
            run.setUsername(username);
            threadPool.execute(run);
        }
        threadPool.shutdown();
        while (true) {
            if (threadPool.isTerminated()) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                break;
            }
        }
        LOGGER.info("Successfull connections: " + success);
        LOGGER.info("Failed connections: " + error);
    }

    /**
	 * 
	 */
    public void testRemoteExecReaderWriter() {
        File pp = new File("test" + File.separator + "input" + File.separator + "ein-pp");
        InputStream is = null;
        InputStream ris = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count = 0;
        try {
            is = new FileInputStream(pp);
            Reader reader = new InputStreamReader(is, CharsetUtil.forName("x-PICA"));
            ris = new ReaderInputStream(reader, CharsetUtil.forName("x-PICA"));
            while ((count = ris.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
            out.flush();
            if (LOGGER.isDebugEnabled()) {
                OutputStream fout = new FileOutputStream("test" + File.separator + "output" + File.separator + "remoteExecReaderWriter.out");
                fout.write(out.toByteArray());
                fout.close();
            }
            byte[] expected = new byte[(int) pp.length()];
            InputStream fis = new FileInputStream(pp);
            fis.read(expected);
            fis.close();
            Assert.assertArrayEquals(expected, out.toByteArray());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (ris != null) {
                try {
                    ris.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
	 * 
	 */
    @Test
    public void testProcessRequest() {
        String command = ". .profile;\n" + "/pica/tolk/bin/csfn_pica32norm -y |\n" + "/pica/tolk/bin/csfn_fcvnorm -k FCV#pica#marc21-exchange -t ALPHA";
        remoteExec.setCommand(command);
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = new FileInputStream("test" + File.separator + "input" + File.separator + "ein-pp");
            Reader reader = new InputStreamReader(in, CharsetUtil.forName("x-PICA"));
            InputStream ris = new ReaderInputStream(reader, CharsetUtil.forName("x-PICA"));
            out = new ByteArrayOutputStream();
            remoteExec.processRequest(ris, out);
        } catch (FileNotFoundException e) {
            Assert.fail(e.getMessage());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private class ConnectionThread implements Runnable {

        private RemoteExec remoteExec;

        private String username;

        private String password;

        private String host;

        private int port = 22;

        public void run() {
            this.remoteExec = new RemoteExec();
            this.remoteExec.setHost(this.host);
            this.remoteExec.setPassword(this.password);
            this.remoteExec.setPort(this.port);
            this.remoteExec.setUser(this.username);
            try {
                this.remoteExec.connect();
                Thread.sleep(3000);
                success++;
            } catch (JSchException e) {
                error++;
                RemoteExecTest.LOGGER.info("Connection failed.", e);
            } catch (InterruptedException e) {
            } finally {
                this.remoteExec.disconnect();
            }
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    /**
	 * @param b
	 * @return
	 */
    public static String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
            sb.append(" ");
        }
        return sb.toString().toUpperCase();
    }
}
