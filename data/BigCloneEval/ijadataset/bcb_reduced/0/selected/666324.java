package org.mobicents.slee.container.profile;

import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.persistence.PersistenceException;
import javax.slee.InvalidStateException;
import javax.slee.SLEEException;
import javax.slee.management.ManagementException;
import javax.slee.profile.ProfileImplementationException;
import javax.slee.profile.ProfileMBean;
import javax.slee.profile.ProfileVerificationException;
import javax.slee.profile.ReadOnlyProfileException;
import javax.transaction.RollbackException;
import javax.transaction.Transaction;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.mobicents.slee.container.SleeContainer;
import org.mobicents.slee.container.profile.entity.ProfileEntity;
import org.mobicents.slee.container.security.Utility;
import org.mobicents.slee.container.transaction.SleeTransactionManager;

/**
 * Start time:12:23:15 2009-03-18<br>
 * Project: mobicents-jainslee-server-core<br>
 * 
 * This is stub class, that is extended and instrumented to allow access to
 * underlying Profile
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author martins
 */
public abstract class AbstractProfileMBeanImpl extends StandardMBean implements AbstractProfileMBean {

    private enum State {

        read, write
    }

    ;

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(AbstractProfileMBeanImpl.class);

    protected static final SleeContainer sleeContainer = SleeContainer.lookupFromJndi();

    /**
	 * the table related with the profile mbean
	 */
    private final ProfileTableImpl profileTable;

    /**
	 * the name of the profile assigned to the mbean
	 */
    private final String profileName;

    /**
	 * current state in the mbean
	 */
    private State state = State.read;

    /**
	 * the transaction curretly assigned to the mbean, if any
	 */
    private Transaction transaction;

    /**
	 * 
	 * @param mbeanInterface
	 * @param profileObject
	 * @throws NotCompliantMBeanException
	 */
    public AbstractProfileMBeanImpl(Class<?> mbeanInterface, String profileName, ProfileTableImpl profileTable) throws NotCompliantMBeanException, ManagementException {
        super(mbeanInterface);
        this.profileTable = profileTable;
        this.profileName = profileName;
    }

    /**
	 * Retrieves the profile object currently bound to the mbean transaction
	 * @return
	 */
    protected ProfileObjectImpl getProfileObject() {
        return profileTable.getProfile(profileName);
    }

    /**
	 * Registers the mbean in the server
	 * @throws ManagementException
	 */
    public void register() throws ManagementException {
        Utility.registerSafelyMBean(sleeContainer, getObjectName(), this);
    }

    /**
	 * Unregisters the mbean in the server
	 * @throws ManagementException
	 */
    public void unregister() throws ManagementException {
        Utility.unregisterSafelyMBean(sleeContainer, getObjectName());
    }

    public void close() throws ManagementException {
        try {
            if (state == State.write) {
                restoreProfile();
            }
            closeProfile();
        } catch (Throwable e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
	 * Closes and unregisters the mbean for the specified profile, if exists
	 * @param profileTableName
	 * @param profileName
	 */
    public static void close(String profileTableName, String profileName) {
        final ObjectName objectName = getObjectName(profileTableName, profileName);
        if (sleeContainer.getMBeanServer().isRegistered(objectName)) {
            Runnable r = new Runnable() {

                public void run() {
                    try {
                        sleeContainer.getMBeanServer().invoke(objectName, "close", new Object[] {}, new String[] {});
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            };
            Thread t = new Thread(r);
            t.start();
        }
    }

    /**
	 * Retrieves the object name used to register this mbean.
	 * @return
	 */
    public ObjectName getObjectName() {
        return getObjectName(profileTable.getProfileTableName(), profileName);
    }

    /**
	 * 
	 * Retrieves the JMX ObjectName for a profile, given its profile name and
	 * profile table name
	 * 
	 * @param profileTableName
	 * @param profileName
	 * @return
	 * @throws MalformedObjectNameException
	 */
    public static ObjectName getObjectName(String profileTableName, String profileName) {
        try {
            return new ObjectName(ProfileMBean.BASE_OBJECT_NAME + ',' + ProfileMBean.PROFILE_TABLE_NAME_KEY + '=' + profileTableName + ',' + ProfileMBean.PROFILE_NAME_KEY + '=' + (profileName != null ? profileName : ""));
        } catch (Throwable e) {
            try {
                return new ObjectName(ProfileMBean.BASE_OBJECT_NAME + ',' + ProfileMBean.PROFILE_TABLE_NAME_KEY + '=' + ObjectName.quote(profileTableName) + ',' + ProfileMBean.PROFILE_NAME_KEY + '=' + ObjectName.quote(profileName != null ? profileName : ""));
            } catch (Throwable f) {
                throw new SLEEException(e.getMessage(), e);
            }
        }
    }

    /**
	 * Retrieves the profile name
	 * @return
	 */
    protected String getProfileName() {
        return profileName;
    }

    /**
	 * Retreives the profile tbale name
	 * @return
	 */
    protected String getProfileTableName() {
        return profileTable.getProfileTableName();
    }

    /**
	 * Moves to the write mode, using specified object. The current java transaction will be suspended.
	 * @throws ManagementException 
	 */
    private void writeMode() throws SLEEException, ManagementException {
        if (!isProfileWriteable()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Changing state to read-write, for profile mbean with name " + profileName + ", from table with name " + this.profileTable.getProfileTableName());
            }
            ProfileObjectImpl profileObject = profileTable.getProfile(profileName);
            profileObject.getProfileEntity().setReadOnly(false);
            state = State.write;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Already in write state, for profile mbean with name " + profileName + ", from table with name " + this.profileTable.getProfileTableName());
            }
        }
    }

    /**
	 * 
	 */
    private void readMode() {
        if (logger.isDebugEnabled()) {
            logger.debug("Changing state to read-only, for profile mbean with name " + profileName + ", from table with name " + this.profileTable.getProfileTableName());
        }
        state = State.read;
    }

    public void closeProfile() throws InvalidStateException, ManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("closeProfile() on: " + profileName + ", from table:" + profileTable.getProfileTableName());
        }
        if (this.isProfileWriteable()) throw new InvalidStateException();
        unregister();
    }

    public void commitProfile() throws InvalidStateException, ProfileVerificationException, ManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("commitProfile() on: " + profileName + ", from table:" + profileTable.getProfileTableName());
        }
        if (!this.isProfileWriteable()) throw new InvalidStateException("not in write state");
        final SleeTransactionManager txManager = sleeContainer.getTransactionManager();
        ProfileEntity profileEntity = null;
        try {
            txManager.resume(this.transaction);
            ProfileObjectImpl profileObject = getProfileObject();
            profileObject.profileVerify();
            if (txManager.getRollbackOnly()) {
                txManager.rollback();
                readMode();
                this.transaction = null;
                throw new RollbackException("the tx is marked for rollback, can't proceeed with commit");
            }
            profileEntity = profileObject.getProfileEntity();
            this.transaction = null;
            txManager.commit();
            readMode();
        } catch (ProfileVerificationException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
            throw e;
        } catch (RollbackException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
            if (e.getCause() instanceof PersistenceException && e.getCause().getCause() instanceof ConstraintViolationException) {
                throw new ProfileVerificationException(e.getCause().getMessage(), e);
            } else if (e.getCause() != null && e.getCause().getClass() == Throwable.class && e.getCause().getMessage() != null && e.getCause().getMessage().equals("setRollbackOnly called from:")) {
                throw new ProfileVerificationException(e.getCause().getMessage(), e);
            } else {
                throw new ManagementException(e.getMessage(), e);
            }
        } catch (Throwable e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
            throw new ManagementException(e.getMessage(), e);
        } finally {
            if (this.transaction == null) {
                if (isProfileWriteable()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("The tx commit failed, recreating tx with current profile entity state");
                    }
                    try {
                        txManager.begin();
                        this.transaction = txManager.getTransaction();
                        ProfileEntity newTxProfileEntity = profileEntity.isCreate() ? profileTable.createProfile(profileName).getProfileEntity() : getProfileObject().getProfileEntity();
                        profileTable.getProfileSpecificationComponent().getProfileEntityFramework().getProfileEntityFactory().copyAttributes(profileEntity, newTxProfileEntity);
                        newTxProfileEntity.setReadOnly(false);
                        txManager.suspend();
                    } catch (Throwable e) {
                        throw new ManagementException(e.getMessage(), e);
                    }
                }
            } else {
                try {
                    txManager.suspend();
                } catch (Throwable f) {
                    logger.error(f.getMessage(), f);
                }
            }
        }
    }

    public void createProfile() throws ManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating profile with name " + profileName + ", from table with name " + this.profileTable.getProfileTableName());
        }
        if (!isProfileWriteable()) {
            final SleeTransactionManager txManager = sleeContainer.getTransactionManager();
            try {
                writeMode();
                this.transaction = txManager.suspend();
            } catch (Throwable e) {
                throw new ManagementException(e.getMessage(), e);
            }
        }
    }

    public void editProfile() throws ManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("Editing profile with name " + profileName + ", from table with name " + this.profileTable.getProfileTableName());
        }
        if (!isProfileWriteable()) {
            final SleeTransactionManager txManager = sleeContainer.getTransactionManager();
            try {
                txManager.begin();
                writeMode();
                this.transaction = txManager.suspend();
            } catch (Throwable e) {
                throw new ManagementException(e.getMessage(), e);
            }
        }
    }

    public boolean isProfileDirty() throws ManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("isProfileDirty() on: " + profileName + ", from table:" + profileTable.getProfileTableName());
        }
        if (isProfileWriteable()) {
            final SleeTransactionManager txManager = sleeContainer.getTransactionManager();
            try {
                txManager.resume(this.transaction);
                return getProfileObject().getProfileEntity().isDirty();
            } catch (Throwable e) {
                throw new ManagementException(e.getMessage(), e);
            } finally {
                try {
                    txManager.suspend();
                } catch (Throwable e) {
                    throw new ManagementException(e.getMessage(), e);
                }
            }
        } else {
            return false;
        }
    }

    public boolean isProfileWriteable() throws ManagementException {
        return state == State.write;
    }

    public void restoreProfile() throws InvalidStateException, ManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("restoreProfile() on: " + profileName + ", from table:" + profileTable.getProfileTableName());
        }
        if (!isProfileWriteable()) {
            throw new InvalidStateException("The restoreProfile method must throw a javax.slee.InvalidStateException if the Profile MBean object is not in the read-write state.");
        }
        final SleeTransactionManager txManager = sleeContainer.getTransactionManager();
        try {
            txManager.resume(transaction);
            txManager.rollback();
            transaction = null;
            readMode();
        } catch (Throwable e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
   * Logic to execute before invoking a cmp setter method on the mbean
   * @return true if the method resumed a transaction
   * @throws ManagementException
   */
    protected void beforeSetCmpField() throws ManagementException, InvalidStateException {
        if (logger.isDebugEnabled()) {
            logger.debug("beforeSetCmpField() on profile with name " + profileName + " of table " + profileTable.getProfileTableName());
        }
        if (isProfileWriteable()) {
            try {
                sleeContainer.getTransactionManager().resume(transaction);
            } catch (Throwable e) {
                throw new ManagementException(e.getMessage(), e);
            }
        } else {
            throw new InvalidStateException();
        }
    }

    /**
   * Logic to execute after invoking a cmp setter method on the mbean
   * @throws ManagementException
   */
    protected void afterSetCmpField() throws ManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("afterSetCmpField() on profile with name " + profileName + " of table " + profileTable.getProfileTableName());
        }
        try {
            sleeContainer.getTransactionManager().suspend();
        } catch (Throwable e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
   * Logic to execute before invoking a cmp getter method on the mbean
   * @return true if the method initiated or resumed a transaction
   * @throws ManagementException
   */
    protected boolean beforeGetCmpField() throws ManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("beforeGetCmpField() on profile with name " + profileName + " of table " + profileTable.getProfileTableName());
        }
        return beforeNonSetCmpField();
    }

    /**
   * Logic to execute after invoking a cmp getter method on the mbean
   * @param activatedTransaction if the method beforeGetCmpField() initiated or resumed a transaction
   * @throws ManagementException
   */
    protected void afterGetCmpField(boolean activatedTransaction) throws ManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("afterGetCmpField( activatedTransaction = " + activatedTransaction + " ) on profile with name " + profileName + " of table " + profileTable.getProfileTableName());
        }
        afterNonSetCmpField(activatedTransaction);
    }

    private boolean beforeNonSetCmpField() throws ManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("beforeManagementMethodInvocation() on profile with name " + profileName + " of table " + profileTable.getProfileTableName());
        }
        if (isProfileWriteable()) {
            try {
                sleeContainer.getTransactionManager().resume(transaction);
            } catch (Throwable e) {
                throw new ManagementException(e.getMessage(), e);
            }
            return true;
        } else {
            try {
                sleeContainer.getTransactionManager().begin();
                return true;
            } catch (Throwable e) {
                throw new ManagementException(e.getMessage(), e);
            }
        }
    }

    private void afterNonSetCmpField(boolean activatedTransaction) throws ManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("afterManagementMethodInvocation( activatedTransaction = " + activatedTransaction + " ) on profile with name " + profileName + " of table " + profileTable.getProfileTableName());
        }
        if (activatedTransaction) {
            if (isProfileWriteable()) {
                try {
                    sleeContainer.getTransactionManager().suspend();
                } catch (Throwable e) {
                    throw new ManagementException(e.getMessage(), e);
                }
            } else {
                try {
                    final SleeTransactionManager txManager = sleeContainer.getTransactionManager();
                    if (txManager.getRollbackOnly()) {
                        txManager.rollback();
                    } else {
                        txManager.commit();
                    }
                } catch (Throwable e) {
                    throw new ManagementException(e.getMessage(), e);
                }
            }
        }
    }

    /**
   * Logic to execute before invoking a management method on the mbean
   * @return true if the method initiated or resumed a transaction
   * @throws ManagementException
   */
    protected boolean beforeManagementMethodInvocation() throws ManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("beforeManagementMethodInvocation() on profile with name " + profileName + " of table " + profileTable.getProfileTableName());
        }
        return beforeNonSetCmpField();
    }

    /**
   * Logic to execute after invoking a management method on the mbean
   * @param activatedTransaction if the method beforeManagementMethodInvocation() initiated or resumed a transaction
   * @throws ManagementException
   */
    protected void afterManagementMethodInvocation(boolean activatedTransaction) throws ManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("afterManagementMethodInvocation( activatedTransaction = " + activatedTransaction + " ) on profile with name " + profileName + " of table " + profileTable.getProfileTableName());
        }
        afterNonSetCmpField(activatedTransaction);
    }

    /**
   * Handles a {@link Throwable}, which was the result of a management method invocation
   * @param t
   * @throws ProfileImplementationException
   * @throws InvalidStateException
   * @throws ManagementException
   */
    protected void throwableOnManagementMethodInvocation(Throwable t) throws ProfileImplementationException, InvalidStateException, ManagementException {
        if (t instanceof ProfileImplementationException) {
            throw (ProfileImplementationException) t;
        } else if (t instanceof InvalidStateException) {
            throw (InvalidStateException) t;
        } else if (t instanceof ReadOnlyProfileException) {
            throw new InvalidStateException(t.getMessage());
        } else if (t instanceof ManagementException) {
            throw (ManagementException) t;
        } else if (t instanceof RuntimeException) {
            try {
                getProfileObject().invalidateObject();
                sleeContainer.getTransactionManager().setRollbackOnly();
            } catch (Throwable e) {
                throw new ManagementException(e.getMessage(), e);
            }
            throw new ProfileImplementationException(t);
        } else {
            throw new ProfileImplementationException(t);
        }
    }
}
