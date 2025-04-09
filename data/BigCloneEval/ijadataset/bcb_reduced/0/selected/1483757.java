package eu.popeye.middleware.usermanagement;

import eu.popeye.middleware.usermanagement.exception.BaseProfileNotInitializedException;
import eu.popeye.security.management.ToolBox;
import java.util.Hashtable;

/**
 * @author  Henning Buesch/OFFIS
 */
public class DummySecurity {

    private Hashtable<String, String> privateData;

    private Hashtable<String, PrivateProfile> privateProfiles;

    public DummySecurity() {
        this.privateData = new Hashtable<String, String>();
        this.privateProfiles = new Hashtable<String, PrivateProfile>();
    }

    public void createPrivateProfile(Hashtable<String, String> specificData, String workspaceName) {
        PrivateProfile pp = null;
        try {
            pp = new PrivateProfile(specificData, ToolBox.generateKeyPair());
        } catch (BaseProfileNotInitializedException ex) {
        }
        this.privateProfiles.put(workspaceName, pp);
    }

    public void delPrivateProfile(String profileName) {
        this.privateProfiles.remove(profileName);
    }

    public PrivateProfile getPrivateProfile(String workspaceName) {
        return this.privateProfiles.get(workspaceName);
    }

    /**
	 * @param privateData  the privateData to set
	 * @uml.property  name="privateData"
	 */
    public void setPrivateData(Hashtable<String, String> privateData) {
        this.privateData = privateData;
    }

    /**
	 * @return  the privateData
	 * @uml.property  name="privateData"
	 */
    public Hashtable<String, String> getPrivateData() {
        return privateData;
    }
}
