package net.sf.traser.client.minimalist;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.traser.common.Security;
import net.sf.traser.configuration.AbstractConfigurable;
import net.sf.traser.configuration.ConfigManagerImpl;
import net.sf.traser.configuration.ConfigManager;
import net.sf.traser.configuration.ConfigurationException;
import net.sf.traser.identification.IdentifierScanner;
import net.sf.traser.service.Basic;
import net.sf.traser.service.BasicStub;
import net.sf.traser.service.Management;
import net.sf.traser.service.ManagementStub;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.neethi.PolicyEngine;
import org.apache.ws.security.handler.WSHandlerConstants;

/**
 *
 * @author Marcell Szathmári
 */
public class ClientFactory extends AbstractConfigurable {

    /**
     * Creates a ClientFactory instance from the specified repository and 
     * configuration file relative to it by loading the configuration with
     * ConfigManagerImpl and returning the ClientFactory implementation
     * loaded by it after setting the password callback instance that would
     * otherwise be set by Axis2.
     * @param repository the location of the repository.
     * @param configFile the location of the configuration file.
     * @return the created ClientFactory instance.
     */
    public static ClientFactory load(String repository, String configFile) {
        File repo = new File(repository);
        File conf = new File(repo, configFile);
        ConfigManager manager = ConfigManagerImpl.loadConfig(conf, repo);
        Security sec = manager.get(Security.class);
        if (sec != null) {
            manager.getConfigCtx().setProperty(WSHandlerConstants.PW_CALLBACK_REF, sec.getPasswordCallbackInstance());
        }
        return manager.get(ClientFactory.class);
    }

    /**
     * 
     * @param <T> the class to return.
     * @param type the type of stub to instantiate.
     * @param target the target to send the message for.
     * @return the configured stub that communicates with the target.
     * @throws org.apache.axis2.AxisFault if anything goes wrong.
     */
    public <T extends Stub> T getClient(Class<T> type, String target) throws AxisFault {
        try {
            Constructor<T> c = type.getConstructor(ConfigurationContext.class, String.class);
            String service = type.getSimpleName().replace("Stub", "");
            String sUrl = target + (target.endsWith("/") ? "" : "/") + "services/" + service;
            T stub = c.newInstance(manager.getConfigCtx(), sUrl);
            Logger.getLogger(ClientFactory.class.getName()).log(Level.FINE, "Creating stub for endpoint '" + sUrl + "' with target '" + target + "'.");
            ServiceClient sc = stub._getServiceClient();
            Security security = manager.get(Security.class);
            if (security != null) {
                EndpointStorager eps = manager.get(EndpointStorager.class);
                security.configureService(sc.getAxisService(), eps.getEndpointAlias(target));
            } else {
                Logger.getLogger(ClientFactory.class.getName()).log(Level.WARNING, "Skipping security setting of generated stub due to missing Security implementation in configuration.");
            }
            Options o = new Options();
            o.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            sc.setOverrideOptions(o);
            sc.engageModule("addressing");
            if (security != null) {
                sc.engageModule("rampart");
            }
            return stub;
        } catch (Exception ex) {
            Logger.getLogger(ClientFactory.class.getName()).log(Level.SEVERE, null, ex);
            throw new AxisFault("Could not configure instance of " + type.getName() + " to communicate with: " + target);
        }
    }

    /**
     * Returns the tag scanner instance.
     * @return
     */
    public IdentifierScanner getScanner() {
        return manager.get(IdentifierScanner.class);
    }

    public Basic basic() {
        return manager.get(Basic.class);
    }

    public BasicStub basicStub(String host) throws AxisFault {
        return getClient(BasicStub.class, host);
    }

    public Management management() {
        return manager.get(Management.class);
    }

    public ManagementStub managementStub(String host) throws AxisFault {
        return getClient(ManagementStub.class, host);
    }

    public ConfigManager manager() {
        return manager;
    }

    public void configure() throws ConfigurationException {
    }
}
