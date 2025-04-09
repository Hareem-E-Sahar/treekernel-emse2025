package com.abiquo.api.access;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;
import org.ietf.jgss.GSSCredential;
import com.abiquo.framework.IGridModel;
import com.abiquo.framework.access.IGridAccess;
import com.abiquo.framework.comm.Communication;
import com.abiquo.framework.comm.CommunicationSimple;
import com.abiquo.framework.config.FrameworkConfiguration;
import com.abiquo.framework.config.FrameworkTypes.AccessMode;
import com.abiquo.framework.config.FrameworkTypes.ExceptionType;
import com.abiquo.framework.config.FrameworkTypes.ObjectType;
import com.abiquo.framework.config.FrameworkTypes.QueryType;
import com.abiquo.framework.exception.CommunicationException;
import com.abiquo.framework.exception.FrameworkException;
import com.abiquo.framework.exception.NoDefaultRemoteFwException;
import com.abiquo.framework.messages.IGridMessage;
import com.abiquo.framework.messages.QueryRequest;
import com.abiquo.framework.messages.QueryResponse;
import com.abiquo.framework.model.NameModel;
import com.abiquo.framework.model.db.DbSecurityModel;
import com.abiquo.framework.model.query.QueryModel;
import com.abiquo.framework.security.ICredentialManager;
import com.abiquo.framework.security.ISecurityModel;
import com.abiquo.framework.xml.MessageConstants;

/**
 * Client mode access using a new communication layer to contact a remote framework instance. The communication layer
 * will be secured depending on the boolean flag "secured" of the framework configuration. By default a secured
 * communication is configured with the default credential, to use an specific credential the method setCredential
 * should be used
 * 
 */
public class RemoteAccess implements IGridAccess {

    /** The logger object. */
    private static final Logger logger = Logger.getLogger(RemoteAccess.class);

    /** A communication layer pointing the remote framework. */
    private Communication comm;

    /** The configuration used to construct framework entity. */
    private FrameworkConfiguration config;

    /** The credential used to construct framework and accessing it. */
    private GSSCredential credential;

    /** Default credential used to secure communications. */
    private GSSCredential defaultCredential;

    /** A remote model access based on query transactions. */
    private final IGridModel model;

    /** Default remote gateway framework nodeName or FQDN (from config). */
    private String nameFramework;

    /** Stores nodename address resolution (using FrameworkConfiguration). */
    private final NameModel nameModel;

    /** A Remote security model access. */
    private ISecurityModel securityModel;

    /**
	 * The remote access class constructor. Configure an access to reach a remote framework instance.
	 * 
	 * @param configuration
	 *            the configuration, where the default framework is read from "FQDN" attribute
	 * 
	 * @throws NoDefaultRemoteFwException
	 *             can not establish the client-framework communication
	 */
    public RemoteAccess(FrameworkConfiguration configuration) throws NoDefaultRemoteFwException {
        config = configuration;
        securityModel = new DbSecurityModel(configuration);
        nameModel = new NameModel(configuration);
        if (configuration.isSecured()) {
            try {
                ICredentialManager credentialManager = (ICredentialManager) Class.forName("com.abiquo.framework.security.credentials.CredentialManager").newInstance();
                defaultCredential = credentialManager.getCredential(configuration.getCommunityCertPath(), configuration.getCommunityKeyPath());
                comm = getCommunicationSecure(nameModel, configuration, defaultCredential);
                Class<?> commClass = Class.forName("com.abiquo.framework.model.db.EnhancedDbSecurityModel");
                Class<?>[] types = new Class[] { FrameworkConfiguration.class };
                Constructor<?> securityModelConstructor = commClass.getConstructor(types);
                securityModel = (ISecurityModel) securityModelConstructor.newInstance(configuration);
            } catch (Exception e) {
                final String msg = "Secure communication with the remote framework could not be stablished";
                logger.error(msg, e);
                throw new NoDefaultRemoteFwException(msg, e);
            }
        } else {
            comm = new CommunicationSimple(nameModel, configuration);
        }
        nameFramework = configuration.getFqdn();
        locateDefaultRemoteFw();
        model = new QueryModel(this);
    }

    /**
	 * Returns a communication layer pointing to framework remote entity to send messages to it
	 * 
	 * @return the communication used on this access
	 */
    public Communication getCommunication() {
        return comm;
    }

    /**
	 * Gets the configuration used to access the framework
	 * 
	 * @return the framework configuration
	 */
    public FrameworkConfiguration getConfiguration() {
        return this.config;
    }

    /**
	 * Returns on which mode are accessing the grid
	 * 
	 * @return RemoteAccess
	 */
    public AccessMode getConnectionMode() {
        return AccessMode.REMOTE;
    }

    /**
	 * Gets the credential used for the client access the framework
	 * 
	 * @return the credential used on access
	 */
    public GSSCredential getCredential() {
        return credential;
    }

    /**
	 * Gets the IGridModel implementation used on this access
	 * 
	 * @return QueryModel (based on remote query transactions)
	 */
    public IGridModel getModel() {
        return model;
    }

    /**
	 * Return the ISecurityModel used on this access
	 * 
	 * @return the ISecurityModel implementation where ask for users/credentials
	 */
    public ISecurityModel getSecurityModel() {
        return securityModel;
    }

    /**
	 * Assures the presence of the default fw gateway (form the FQDN read at FrameworkConfiguration) and sends a
	 * QueryRequest to this address.
	 * 
	 * And it prepares appropriate communication to handle ActiveCommunications (sockets) to the Framework
	 * 
	 * @throws NoDefaultRemoteFwException
	 *             fail establishing the communication (or response message isn't a Query)
	 */
    private void locateDefaultRemoteFw() throws NoDefaultRemoteFwException {
        IGridMessage mesReq, mesResp;
        QueryResponse qResp;
        Callable<IGridMessage> commRun;
        mesReq = new QueryRequest(QueryType.DESCRIPTION, ObjectType.NODE, null);
        logger.debug("Creating local communication layer. Trying to contact the framework at" + nameFramework);
        try {
            commRun = comm.send(mesReq, nameFramework);
            mesResp = commRun.call();
            if (mesResp.getType() == MessageConstants.ET_QUERY_RESPONSE) {
                qResp = (QueryResponse) mesResp;
                if (qResp.getObjectType().equals(ObjectType.NODE) && qResp.getQueryType().equals(QueryType.DESCRIPTION)) {
                    logger.debug("Succesfully contacted");
                } else {
                    throw new NoDefaultRemoteFwException("Bad Protocol: response a Query with no description at " + nameFramework);
                }
            } else {
                throw new NoDefaultRemoteFwException("Bad Protocol: response isnt a Query, it's a " + mesResp.getClass().getName() + " fw at " + nameFramework);
            }
        } catch (UnknownHostException eUn) {
            final String message = "UnknownHostException from default remote framework ";
            logger.error(message, eUn);
            throw new NoDefaultRemoteFwException(message + nameFramework, eUn);
        } catch (IOException eIO) {
            final String message = "IOException from default remote framework at ";
            logger.error(message, eIO);
            throw new NoDefaultRemoteFwException(message + nameFramework, eIO);
        } catch (Exception e) {
            final String message = "Exception (--ICommRun.call--) from default remote framework ";
            logger.error(message, e);
            throw new NoDefaultRemoteFwException(message + nameFramework, e);
        }
    }

    /**
	 * Sends a message using communication gateway to default framework gateway
	 * 
	 * @param messageRequest
	 *            the message Request starting the communication
	 * @throws CommunicationException
	 *             if the message transaction do not success
	 */
    public Callable<IGridMessage> send(IGridMessage messageRequest) throws CommunicationException {
        Callable<IGridMessage> com;
        logger.debug("Sending through a remote framework");
        try {
            com = comm.send(messageRequest, nameFramework);
        } catch (UnknownHostException eU) {
            final String msg = "Unknow remote framework name: " + nameFramework;
            logger.error(msg, eU);
            throw new CommunicationException(msg, eU);
        }
        return com;
    }

    /**
	 * Changes the credential used to access the framework. Cause communication layer to use a secure implementation.
	 * 
	 * @param credential
	 *            the new user credential for the secure communication
	 */
    public void setCredential(GSSCredential credential) {
        this.credential = credential;
        logger.debug("Setting credential for secured communication");
        try {
            comm = getCommunicationSecure(nameModel, config, credential);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    /**
	 * Terminates the framework instance
	 */
    public void terminate() throws FrameworkException {
        throw new FrameworkException(ExceptionType.INTERNAL, "The remote framework can't be terminated");
    }

    /**
	 * Private helper to get a secure Communication through reflection mechanism
	 * 
	 * @param nameModel
	 *            the nameModel
	 * @param configuration
	 *            the FrameworkConfiguration
	 * @param credential
	 *            the credential
	 * @return
	 * @throws Exception
	 */
    private Communication getCommunicationSecure(NameModel nameModel, FrameworkConfiguration configuration, GSSCredential credential) throws Exception {
        Communication commSecure;
        Class<?> commClass = Class.forName("com.abiquo.framework.comm.CommunicationSecure");
        Class<?>[] types = new Class[] { NameModel.class, FrameworkConfiguration.class, GSSCredential.class };
        Constructor<?> commConstructor = commClass.getConstructor(types);
        commSecure = (Communication) commConstructor.newInstance(nameModel, configuration, defaultCredential);
        return commSecure;
    }
}
