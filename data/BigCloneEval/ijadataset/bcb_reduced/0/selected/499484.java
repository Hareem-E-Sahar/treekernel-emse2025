package com.ericdaugherty.mail.server.auth;

import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;
import javax.security.sasl.SaslException;

/**
 * This class is used in a gss-api context to envelope a smtp/pop server session
 * in a privileged block bound by (a) jgss subject(s).
 * 
 * @author Andreas Kyrmegalos
 */
public class AuthContext {

    private static AuthContext instance;

    private final Subject[] subjects;

    private AuthContext(Subject[] subjects) {
        this.subjects = subjects;
    }

    public static final AuthContext getInstance() {
        return instance;
    }

    public static final AuthContext initialize(Subject[] subjects) {
        if (instance == null) {
            instance = new AuthContext(subjects);
        }
        return instance;
    }

    public synchronized GSSServerMode getGSSServerMode(final boolean isSMTP, final String clientIp) throws SaslException {
        try {
            return (GSSServerMode) Subject.doAsPrivileged(((isSMTP || subjects.length == 1) ? subjects[0] : subjects[1]), new PrivilegedExceptionAction() {

                public Object run() throws Exception {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    Class c = Class.forName("com.ericdaugherty.mail.server.auth.GSSServerMode", true, cl);
                    Object instance = c.getConstructor(Boolean.class, String.class).newInstance(new Boolean(isSMTP), clientIp);
                    java.lang.reflect.Method mainMethod = c.getMethod("negotiateGSSAuthenticationContext");
                    mainMethod.invoke(instance);
                    return instance;
                }
            }, null);
        } catch (java.security.PrivilegedActionException pae) {
            throw (SaslException) pae.getException();
        }
    }
}
