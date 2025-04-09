package edu.upmc.opi.caBIG.caTIES.installer.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.xpath.XPath;
import edu.upmc.opi.caBIG.caTIES.common.CaTIES_JDomUtils;
import edu.upmc.opi.caBIG.caTIES.common.GeneralUtilities;
import edu.upmc.opi.caBIG.caTIES.installer.CaTIES_ConfigurationProperties;

public class CaTIES_InstallationTaskStartTomcat extends CaTIES_InstallationTaskImpl {

    private static final Logger logger = Logger.getLogger(CaTIES_InstallationTaskStartTomcat.class);

    public static final String CONST_PUB = "Pub";

    public static final String CONST_PVT = "Pvt";

    private CaTIES_ConfigurationProperties cfg;

    private String pvtOrPubKey = "Pub";

    private boolean isPublic = true;

    private String httpsPort = "";

    private String tomcatBinaryPath = null;

    public static void main(String[] args) {
        CaTIES_InstallationTaskStartTomcat task = new CaTIES_InstallationTaskStartTomcat();
        task.setTomcatBinaryPath("E:\\caties_5_0\\nwu\\tomcatPub");
        task.performTask();
    }

    protected void performTask() {
        logger.debug("Started " + this.getClass().getSimpleName());
        if (this.tomcatBinaryPath == null) {
            modifyStartTomcatFile();
            updateTomcatServerConfigPort(this.httpsPort + "");
            this.tomcatBinaryPath = this.cfg.getOrgNodePath() + File.separator + "tomcat" + this.pvtOrPubKey + File.separator + "bin";
        }
        logger.debug("Starting tomcat in " + this.tomcatBinaryPath);
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec("cmd /K echo Started Shell");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (proc != null) {
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
            PrintWriter out = new PrintWriter(bw, true);
            out.println("cd /D " + tomcatBinaryPath);
            out.println("caties_startup.bat");
            out.println("exit");
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                    if (line.endsWith("exit")) {
                        break;
                    }
                }
                proc.waitFor();
                int eValue = proc.exitValue();
                System.out.println("Got exit value of " + eValue);
                in.close();
                out.close();
                proc.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.debug("Completed " + this.getClass().getSimpleName());
    }

    private void modifyStartTomcatFile() {
        try {
            String caTIEsStartTomcatCmdFilePath = this.cfg.getOrgNodePath() + File.separator + "tomcat" + this.pvtOrPubKey + File.separator + "bin" + File.separator + "caties_startup.bat";
            StringBuffer sb = new StringBuffer();
            sb.append("rem\n");
            sb.append("rem Windows OS Tomcat Startup Script for CaTIES 5.0 Network Addition\n");
            sb.append("rem\n");
            sb.append("\n");
            sb.append("set CATIES_NODE=" + this.cfg.getOrgNodePath() + "\n");
            sb.append("\n");
            sb.append("set JAVA_HOME=%CATIES_NODE%\\jdk1.6.0_24\n");
            sb.append("set CATALINA_HOME=%CATIES_NODE%\\tomcat" + this.pvtOrPubKey + "\n");
            sb.append("set CATALINA_OPTS=-Djava.security.gsi.signing.policy=false -Xmx512M\n");
            sb.append("\n");
            sb.append("cd %CATALINA_HOME%\bin\n");
            sb.append("\n");
            sb.append("%CATALINA_HOME%\\bin\\startup.bat\n");
            BufferedWriter out = new BufferedWriter(new FileWriter(caTIEsStartTomcatCmdFilePath));
            out.write(sb.toString());
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    protected void updateTomcatServerConfigPort(String publicPort) {
        File serverXml = new File(this.cfg.getOrgNodePath() + File.separator + "tomcat" + this.pvtOrPubKey + File.separator + "conf" + File.separator + "server.xml");
        logger.debug("updateTomcatServerConfigPort called with " + serverXml.getAbsolutePath());
        SAXBuilder builder = new SAXBuilder();
        try {
            Document serverXmlDocument = builder.build(serverXml);
            XPath p = XPath.newInstance("//Connector");
            Element connectorElement = (Element) p.selectSingleNode(serverXmlDocument);
            if (connectorElement != null) {
                connectorElement.setAttribute("port", publicPort);
            }
            GeneralUtilities.backupFile(serverXml);
            FileOutputStream fos = new FileOutputStream(serverXml);
            CaTIES_JDomUtils.writeDocument(serverXmlDocument, Format.getPrettyFormat(), fos);
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void updateTomcatServerConfigCerts(String keyFilePath, String certFilePath) {
        File serverXml = new File(this.cfg.getOrgNodePath() + File.separator + "tomcat" + this.pvtOrPubKey + File.separator + "conf" + File.separator + "server.xml");
        SAXBuilder builder = new SAXBuilder();
        try {
            Document serverXmlDocument = builder.build(serverXml);
            XPath p = XPath.newInstance("//Service[@name=\"Catalina\"]");
            Element serviceElement = (Element) p.selectSingleNode(serverXmlDocument);
            p = XPath.newInstance("//Connector[@port=\"" + this.cfg.getSpokeOrg().getPublicHTTPPort() + "\"]");
            Element connectorElement = (Element) p.selectSingleNode(serverXmlDocument);
            if (connectorElement == null) {
                connectorElement = createTomcatHTTPSConnectorElement(this.cfg.getSpokeOrg().getPublicHTTPPort(), keyFilePath, certFilePath);
                serviceElement.addContent(connectorElement);
            } else {
                connectorElement.setAttribute("cert", certFilePath);
                connectorElement.setAttribute("key", keyFilePath);
            }
            GeneralUtilities.backupFile(serverXml);
            FileOutputStream fos = new FileOutputStream(serverXml);
            CaTIES_JDomUtils.writeDocument(serverXmlDocument, Format.getPrettyFormat(), fos);
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Element createTomcatHTTPSConnectorElement(String port, String keyFilePath, String certFilePath) {
        Element connectorElement = new Element("Connector");
        connectorElement.setAttribute("className", "org.globus.tomcat.coyote.net.HTTPSConnector");
        connectorElement.setAttribute("port", port);
        connectorElement.setAttribute("maxThreads", "150");
        connectorElement.setAttribute("minSpareThreads", "25");
        connectorElement.setAttribute("maxSpareThreads", "75");
        connectorElement.setAttribute("enableLookups", "false");
        connectorElement.setAttribute("acceptCount", "100");
        connectorElement.setAttribute("debug", "0");
        connectorElement.setAttribute("protocolHandlerClassName", "org.apache.coyote.http11.Http11Protocol");
        connectorElement.setAttribute("socketFactory", "org.globus.tomcat.catalina.net.BaseHTTPSServerSocketFactory");
        connectorElement.setAttribute("cert", certFilePath);
        connectorElement.setAttribute("key", keyFilePath);
        return connectorElement;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        if (this.isPublic) {
            this.pvtOrPubKey = CONST_PUB;
        } else {
            this.pvtOrPubKey = CONST_PVT;
        }
    }

    public String getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(String httpsPort) {
        this.httpsPort = httpsPort;
    }

    public CaTIES_ConfigurationProperties getCfg() {
        return cfg;
    }

    public void setCfg(CaTIES_ConfigurationProperties cfg) {
        this.cfg = cfg;
    }

    public String getTomcatBinaryPath() {
        return tomcatBinaryPath;
    }

    public void setTomcatBinaryPath(String tomcatBinaryPath) {
        this.tomcatBinaryPath = tomcatBinaryPath;
    }
}
