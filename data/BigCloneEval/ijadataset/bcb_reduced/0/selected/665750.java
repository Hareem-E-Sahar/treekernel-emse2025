package fr.cnes.sitools.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import org.restlet.Request;
import org.restlet.data.ClientInfo;
import org.restlet.data.Method;
import org.restlet.ext.crypto.DigestUtils;
import org.restlet.security.Authorizer;
import org.restlet.security.Enroler;
import org.restlet.security.Role;
import org.restlet.security.User;
import fr.cnes.sitools.common.SitoolsSettings;
import fr.cnes.sitools.common.application.SitoolsApplication;
import fr.cnes.sitools.inscription.model.Inscription;
import fr.cnes.sitools.security.authentication.SitoolsRealm;

/**
 * Utility class for security checking / encrypting
 * 
 * @author jp.boignard (AKKA Technologies)
 * 
 */
public final class SecurityUtil {

    /** Prefix for LDAP MD5 encryption */
    public static final String OPENLDAP_MD5_PREFIX = "{MD5}";

    /** Prefix for DIGEST MD5 encryption */
    public static final String DIGEST_MD5_PREFIX = "MD5://";

    /** code setting for DIGEST_MD5 algorithm */
    public static final String DIGEST_MD5_ALGORITHM = "DIGEST-MD5";

    /** code setting for OPENLDAP_MD5 algorithm */
    public static final String OPENLDAP_MD5_ALGORITHM = "OPENLDAP-MD5";

    /** public Role */
    public static final String PUBLIC_ROLE = "public";

    /** logger */
    private static Logger logger = Logger.getLogger(SecurityUtil.class.getName());

    /**
   * Private constructor for utility class
   */
    private SecurityUtil() {
        super();
    }

    /**
   * Server internal checks of user authorization on a specific application.
   * 
   * @param myApp
   *          Application instance identifier
   * @param userIdentifier
   *          user identifier
   * @param method
   *          Restlet Method
   * @return boolean
   */
    public static boolean authorize(SitoolsApplication myApp, String userIdentifier, Method method) {
        if (myApp == null) {
            return false;
        }
        SitoolsRealm smr = myApp.getSettings().getAuthenticationRealm();
        if (smr == null) {
            logger.warning("SecurityUtil.authorize error : SitoolsMemoyRealm is null.");
            return false;
        }
        ClientInfo ci = new ClientInfo();
        Role publicRole = smr.getPublicRole();
        if (publicRole != null) {
            ci.getRoles().add(publicRole);
        }
        if ((userIdentifier == null) || userIdentifier.equals("")) {
            ci.setAuthenticated(false);
        } else {
            User usr = smr.findUser(userIdentifier);
            ci.setUser(usr);
            ci.setAuthenticated(true);
        }
        Enroler enroler = smr.getEnroler();
        enroler.enrole(ci);
        Request request = new Request();
        request.setMethod(method);
        request.setClientInfo(ci);
        Authorizer authorizer = myApp.getAuthorizer();
        if (authorizer == null) {
            return true;
        }
        boolean authorization = authorizer.authorize(request, null);
        return authorization;
    }

    /**
   * Encrypt password of user according to security settings
   * 
   * @param settings
   *          SitoolsSettings where getting security configuration
   * @param input
   *          user on which to encrypt password
   */
    public static void encodeUserPassword(SitoolsSettings settings, fr.cnes.sitools.security.model.User input) {
        String realm = settings.getAuthenticationDOMAIN();
        String storeAlgorithm = settings.getAuthenticationALGORITHM();
        if (!input.getSecret().startsWith(DIGEST_MD5_PREFIX) && !input.getSecret().startsWith(OPENLDAP_MD5_PREFIX)) {
            if (storeAlgorithm.equals(DIGEST_MD5_ALGORITHM)) {
                input.setSecret(digestMd5(input.getIdentifier(), input.getSecret().toCharArray(), realm));
            }
            if (storeAlgorithm.equals(OPENLDAP_MD5_ALGORITHM)) {
                input.setSecret(openldapDigestMd5(input.getSecret()));
            }
        }
    }

    /**
   * Encrypt password of inscription according to security settings
   * 
   * @param settings
   *          SitoolsSettings where getting security configuration
   * @param inscriptionInput
   *          inscription on which to encrypt password
   */
    public static void encodeUserInscriptionPassword(SitoolsSettings settings, Inscription inscriptionInput) {
        String realm = settings.getAuthenticationDOMAIN();
        String storeAlgorithm = settings.getAuthenticationALGORITHM();
        if (!inscriptionInput.getPassword().startsWith(DIGEST_MD5_PREFIX) && !inscriptionInput.getPassword().startsWith(OPENLDAP_MD5_PREFIX)) {
            if (storeAlgorithm.equals(DIGEST_MD5_ALGORITHM)) {
                inscriptionInput.setPassword(digestMd5(inscriptionInput.getIdentifier(), inscriptionInput.getPassword().toCharArray(), realm));
            }
            if (storeAlgorithm.equals(OPENLDAP_MD5_ALGORITHM)) {
                inscriptionInput.setPassword(openldapDigestMd5(inscriptionInput.getPassword()));
            }
        }
    }

    /**
   * Encryption with OpenLDAP digest md5 algorithm (<> HTTP digest MD5)
   * @param password user password
   * @return encrypted key
   */
    public static String openldapDigestMd5(final String password) {
        String base64;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(password.getBytes());
            base64 = fr.cnes.sitools.util.Base64.encodeBytes(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return OPENLDAP_MD5_PREFIX + base64;
    }

    /**
   * Encryption with HTTP Digest algorithm
   * @param identifier user login
   * @param secret user password
   * @param realm domain
   * @return encrypted key
   */
    public static String digestMd5(final String identifier, final char[] secret, final String realm) {
        return DIGEST_MD5_PREFIX + DigestUtils.toHttpDigest(identifier, secret, realm);
    }
}
