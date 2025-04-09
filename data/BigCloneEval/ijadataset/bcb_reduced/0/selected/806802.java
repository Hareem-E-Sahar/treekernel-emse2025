package eu.popeye.middleware.usermanagement;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import ocp.context.ContextException;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion.User;
import eu.popeye.application.PropertiesLoader;
import eu.popeye.middleware.groupmanagement.management.Workgroup;
import eu.popeye.middleware.groupmanagement.membership.Member;
import eu.popeye.middleware.groupmanagement.membership.MemberActionListener;
import eu.popeye.middleware.networkinformation.NetworkInformation;
import eu.popeye.middleware.usermanagement.exception.BaseProfileAuthentificationFailedException;
import eu.popeye.middleware.usermanagement.exception.BaseProfileNotInitializedException;
import eu.popeye.middleware.usermanagement.exception.ContextNotInitializedException;
import eu.popeye.middleware.usermanagement.exception.PrivateProfileCreationFailedException;
import eu.popeye.networkabstraction.communication.basic.BSMProvider;
import eu.popeye.networkabstraction.communication.basic.BasicServicesManager;
import eu.popeye.networkabstraction.communication.basic.util.InitializationException;

/**
 * The main UserManagement class. This class is a facade and should therefore be used as only class of this package.
 * @author  Henning Buesch/OFFIS
 */
public class UserManagement implements MemberActionListener {

    private static UserManagement instance = null;

    private static final String BASEPROFILE_DATATYPE = "lol";

    private static final String BASEPROFILE_MAIN_FILENAME = "_BaseProfile";

    private UserInformationManager userInformationManager;

    private InternalCommunication interComm;

    private ExternalCommunication exterComm;

    private SecurityCommunication secuComm;

    private UserManagement() {
        interComm = new InternalCommunication(this);
        exterComm = new ExternalCommunication();
        secuComm = new SecurityCommunication();
        userInformationManager = new UserInformationManager();
        instance = this;
    }

    /**
     * Gets instance of UserManagement.
     * @return  An instance of UserManagement.
     */
    public static UserManagement getInstance() {
        if (instance == null) instance = new UserManagement();
        return instance;
    }

    public void initContext(BasicServicesManager bsm) {
        exterComm.initContext(bsm);
    }

    /**
     * Serializes the currently used BaseProfile to a given location.
     * @throws IOException
     */
    public void serializeBaseProfile() throws IOException, BaseProfileNotInitializedException {
        this.userInformationManager.serializeBaseProfile(this.getFullFileName(this.getUserName()));
    }

    /**
     * Deserializes a BaseProfile defined by a user's name and uses it.
     * @param userName the user's name defining the BaseProfile's owner.
     * @throws IOException
     * @throws ClassNotFoundException
     * @deprecated
     */
    public void deserializeBaseProfile(String userName) throws IOException, ClassNotFoundException, BaseProfileAuthentificationFailedException {
        this.deserializeBaseProfile(userName, "default");
    }

    public void deserializeBaseProfile(String userName, String password) throws IOException, ClassNotFoundException, BaseProfileAuthentificationFailedException {
        this.userInformationManager.deserializeBaseProfile(getFullFileName(userName), password);
    }

    /**
     * Builds a full filename using the PropertiesLoader class.
     * @param userName the name of the current user.
     * @return a String representing a full filename for serialization (e.g. "C:\POPEYE\HenningBuesch_BaseProfile.lol")
     */
    private String getFullFileName(String userName) {
        String localpath = PropertiesLoader.getLocalDataPath();
        StringBuilder strb = new StringBuilder(localpath);
        strb.append(userName);
        strb.append(UserManagement.BASEPROFILE_MAIN_FILENAME);
        strb.append(".");
        strb.append(UserManagement.BASEPROFILE_DATATYPE);
        return strb.toString();
    }

    /**
     * Creates a new BaseProfile. The Profile will NOT be registered in Context-modules or certified! automatically!
     * @see UserInformationManager#createUserAccount(User, Hashtable)
     * @param The data the UserProfile will represent (such as a name or a date of birth)
     * @deprecated
     */
    public void createBaseProfile(Hashtable<String, String> publicData) {
        this.createBaseProfile(publicData, "default");
    }

    /**
     * Creates a new BaseProfile. The Profile will NOT be registered in Context-modules or certified! automatically!
     * @see UserInformationManager#createUserAccount(User, Hashtable)
     * @param The data the UserProfile will represent (such as a name or a date of birth)
     */
    public void createBaseProfile(Hashtable<String, String> publicData, String password) {
        if (!eu.popeye.application.PropertiesLoader.isSecurityEnabled()) {
            this.userInformationManager.createBaseProfile(publicData, this.secuComm.generateKeyPair(), password);
        } else {
            KeyPair kp = eu.popeye.security.accesscontrol.CredentialManager.getInstance().getKeys();
            if (kp == null) {
                this.userInformationManager.createBaseProfile(publicData, this.secuComm.generateKeyPair(), password);
            } else {
                this.userInformationManager.createBaseProfile(publicData, kp, password);
            }
        }
    }

    /**
     * Sends a certification request to the specific InetAddress.
     * @param certifyer the InetAddress defining the device that the certification request is sent to.
     */
    public void certifyBaseProfile(InetAddress certifyer) throws BaseProfileNotInitializedException {
        this.secuComm.certificateUserProfileAsync(this.getBaseProfile(), certifyer);
    }

    /**
     * Registers the BaseProfile using Context.
     */
    public void registerBaseProfileInContext() throws BaseProfileNotInitializedException, ContextException, ContextNotInitializedException {
        if (this.getBaseProfile() == null) throw new BaseProfileNotInitializedException();
        this.exterComm.registerProfileAsContext(this.getBaseProfile());
        UserManagement.debug("BaseProfile registered as Context source. ID: " + this.getBaseProfile().getID());
    }

    /**
     * Requests the creation of a new PrivateProfile.
     * @throws PrivateProfileCreationFailedException
     */
    public PublicKey requestPrivateProfileCreation() throws PrivateProfileCreationFailedException {
        return this.secuComm.requestPrivateProfileCreation();
    }

    /**
     * Sets the private data defining the user. This data will be stored to Security modules.
     * @param profileIdentifier the java.security.PublicKey identifying the private profile the data will be added to.
     * @param privateData the private data to store.
     */
    public void setPrivateData(PublicKey profileIdentifier, Hashtable<String, String> privateData) {
    }

    /**
     * Updates the BaseProfile deleting ALL old data and using the parsed data as basis.
     * @param publicData the public data that is used as only data inside the BaseProfile.
     */
    public void updateBaseProfile(Hashtable<String, String> publicData) throws BaseProfileNotInitializedException {
    }

    /**
     * Delivers a UserProfile identified by a unique key asynchronously (for MANET-members only!).
     * @param The java.security.PublicKey identifying the user's profile
     * @param The UserManagementConsumer that requests the profile
     */
    public void getProfileAsync(PublicKey key, UserManagementConsumer sender) {
        this.exterComm.getProfileAsyncFromContext(key, sender);
    }

    /**
     * Delivers the UserProfile that a specific Member uses inside a specific Workgroup(/Workspace) (for Workspace-internal use only!).
     * @param group the Workgroup the profile is used in
     * @param whose the Mmember to request the profile  from
     * @param sender the UserManagementConsumer that requests the profile
     */
    public void getProfileAsync(Workgroup group, Member whose, UserManagementConsumer sender) {
        this.interComm.getProfileAsync(group, whose, sender);
    }

    /**
     * Gets the base profile.
     * @return the base profile
     */
    public BaseProfile getBaseProfile() throws BaseProfileNotInitializedException {
        BaseProfile bp = this.userInformationManager.getBaseProfile();
        if (bp == null) throw new BaseProfileNotInitializedException();
        return bp;
    }

    /**
     * Gets the KeyPair identifying a given UserProfile.
     * Please only use this method in Security modules!
     * @param profile the UserProfile to get the belonging java.security.KeyPair to
     * @return the KeyPair identifying the given UserProfile
     */
    public KeyPair getProfileKeyPair(UserProfile profile) {
        return profile.getKeyPair();
    }

    /**
     * Gets the user's name defined in the base profile.
     * @return the user's name defined in the base profile.
     */
    public String getUserName() throws BaseProfileNotInitializedException {
        return this.getBaseProfile().getValue("name");
    }

    public Member getUserInNetwork(String username) {
        Member result;
        try {
            BasicServicesManager bsm = BSMProvider.getInstance().getBasicServicesManager();
            NetworkInformation ni = bsm.getNetworkInformartion();
            List<Member> members = ni.getManetMembers();
            Iterator<Member> i = members.iterator();
            for (; i.hasNext(); ) {
                result = i.next();
                if (result.getUsername().equals(username)) return result;
            }
        } catch (InitializationException e) {
            e.printStackTrace();
        }
        return null;
    }

    PrivateProfile getPrivateProfile(String workspaceName) {
        return null;
    }

    public void onMemberJoin(String wgName, Member m) {
        this.interComm.onMemberJoin(wgName, m);
    }

    public void onMemberLeave(String wgName, Member m) {
        this.interComm.onMemberLeave(wgName, m);
    }

    public void onMemberChange(String wgName, List members) {
        this.interComm.onMemberChange(wgName, members);
    }

    /**
     * Registers the UserManagement as MemberActionListener to the recently joined Workgroup.
     * Whenever a Workgroup is joined, this method has to be called and the Workgroup has to be parsed as param.
     * This method will be used by WorkspaceManagement.
     * @see InternalCommunication#onJoinWorkgroup(Workgroup)
     * @param wg the Workgroup that was recently joined
     */
    public void onJoinWorkgroup(Workgroup wg) {
        wg.addMemberActionListener(this);
        this.interComm.onJoinWorkgroup(wg);
    }

    /**
     * Unregisters the UserManagement as MemberActionListener from the recently left Workgroup.
     * Whenever a previously joined Workgroup is left, this method has to be called with the Workgroup as param
     * Will be used by WorkspaceManagement
     * @see InternalCommunication#onLeaveWorkgroup(Workgroup)
     * @param wg the Workgroup that was recently left
     */
    public void onLeaveWorkgroup(Workgroup wg) {
        wg.removeMemberActionListener(this);
        this.interComm.onLeaveWorkgroup(wg);
    }

    protected static void debug(String output) {
        output = "USER_MANAGEMENT: " + output;
        System.out.println(output);
    }

    protected static void errorOccured(String debugMessage, String userNotification) {
        System.out.println(debugMessage);
    }
}
