package org.dcm4chee.web.war;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import org.apache.wicket.protocol.http.MockServletContext;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.security.actions.Actions;
import org.apache.wicket.security.hive.HiveMind;
import org.apache.wicket.util.tester.WicketTester;
import org.jboss.system.server.ServerConfigLocator;

public class WASPTestUtil {

    private static String actionKey;

    private static String hiveKey;

    private static final String ROLE_MAPPING_FILENAME = "roles.json";

    public static WicketTester getWicketTester(WebApplication testApplicaton) throws URISyntaxException {
        if (actionKey != null) {
            HiveMind.unregisterHive(hiveKey);
            Actions.unregisterActionFactory(actionKey);
            actionKey = hiveKey = null;
        }
        String contextPath = new java.io.File(WASPTestUtil.class.getResource("WASPTestUtil.class").toURI()).getParent();
        WicketTester wicketTester = new WicketTester(testApplicaton, contextPath);
        if (actionKey == null) {
            hiveKey = "hive_" + testApplicaton.getName();
            actionKey = testApplicaton.getClass().getName() + ":" + hiveKey;
        }
        MockServletContext ctx = (MockServletContext) wicketTester.getApplication().getServletContext();
        ctx.addInitParameter("securityDomainName", "dcm4chee");
        ctx.addInitParameter("rolesGroupName", "Roles");
        URL url = WASPTestUtil.class.getResource("/wicket.login.file");
        System.setProperty("java.security.auth.login.config", "=" + url.getPath());
        return wicketTester;
    }

    public static void initRolesMappingFile() throws IOException {
        String webConfigPath = ServerConfigLocator.locate().getServerHomeDir().getAbsolutePath() + "/conf/";
        System.setProperty("dcm4chee-web3.cfg.path", webConfigPath);
        File f = new File(ROLE_MAPPING_FILENAME);
        if (!f.isAbsolute()) f = new File(webConfigPath, f.getPath());
        if (f.exists()) return;
        f.getParentFile().mkdirs();
        FileChannel fos = null;
        InputStream is = null;
        try {
            URL url = WASPTestUtil.class.getResource("/roles-test.json");
            is = url.openStream();
            ReadableByteChannel inCh = Channels.newChannel(is);
            fos = new FileOutputStream(f).getChannel();
            int pos = 0;
            while (is.available() > 0) pos += fos.transferFrom(inCh, pos, is.available());
        } finally {
            try {
                if (is != null) is.close();
            } catch (Exception ignore) {
            }
            try {
                if (fos != null) fos.close();
            } catch (Exception ignore) {
            }
        }
    }
}
