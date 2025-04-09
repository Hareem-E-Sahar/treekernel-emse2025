package org.mobicents.servlet.sip.core.session;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.sip.SipSession.State;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerTask;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * This class handles the management of sip sessions and sip application sessions for a given container (context)
 * It is a delegate since it is used by many manager implementations classes (Standard and clustered ones) 
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public abstract class SipManagerDelegate {

    private static final Logger logger = Logger.getLogger(SipManagerDelegate.class);

    protected ConcurrentHashMap<SipApplicationSessionKey, MobicentsSipApplicationSession> sipApplicationSessions = new ConcurrentHashMap<SipApplicationSessionKey, MobicentsSipApplicationSession>();

    protected ConcurrentHashMap<String, MobicentsSipApplicationSession> sipApplicationSessionsByAppGeneratedKey = new ConcurrentHashMap<String, MobicentsSipApplicationSession>();

    protected ConcurrentHashMap<SipSessionKey, MobicentsSipSession> sipSessions = new ConcurrentHashMap<SipSessionKey, MobicentsSipSession>();

    protected SipFactoryImpl sipFactoryImpl;

    protected SipContext container;

    /**
     * The maximum number of active Sip Sessions allowed, or -1 for no limit.
     */
    protected int maxActiveSipSessions = -1;

    /**
     * The maximum number of active Sip Application Sessions allowed, or -1 for no limit.
     */
    protected int maxActiveSipApplicationSessions = -1;

    /**
     * Number of sip session creations that failed due to maxActiveSipSessions.
     */
    protected int rejectedSipSessions = 0;

    /**
     * Number of sip application session creations that failed due to maxActiveSipApplicationSessions.
     */
    protected int rejectedSipApplicationSessions = 0;

    /**
     * The longest time (in seconds) that an expired sip session had been alive.
     */
    private int sipSessionMaxAliveTime;

    /**
     * Average time (in seconds) that expired sip sessions had been alive.
     */
    private int sipSessionAverageAliveTime;

    /**
     * Number of sip sessions that have expired.
     */
    private int expiredSipSessions = 0;

    /**
     * The longest time (in seconds) that an expired Sip Application session had been alive.
     */
    private int sipApplicationSessionMaxAliveTime;

    /**
     * Average time (in seconds) that expired Sip Application Sessions had been alive.
     */
    private int sipApplicationSessionAverageAliveTime;

    /**
     * Number of sip application sessions that have expired.
     */
    private int expiredSipApplicationSessions = 0;

    protected int sipSessionCounter = 0;

    protected int sipApplicationSessionCounter = 0;

    private int lastUpdatedSasCreationCounter = 0;

    private long lastSipApplicationSessionUpdatedTime = 0;

    private double lastAverageSasCreationPerSecond = 0.0;

    private int lastUpdatedSsCreationCounter = 0;

    private long lastSipSessionUpdatedTime = 0;

    private double lastAverageSsCreationPerSecond = 0.0;

    /**
	 * @return the SipFactoryImpl
	 */
    public SipFactoryImpl getSipFactoryImpl() {
        return sipFactoryImpl;
    }

    /**
	 * @param sipFactoryImpl the SipFactoryImpl to set
	 */
    public void setSipFactoryImpl(SipFactoryImpl sipFactoryImpl) {
        this.sipFactoryImpl = sipFactoryImpl;
    }

    /**
	 * @return the container
	 */
    public SipContext getContainer() {
        return container;
    }

    /**
	 * @param container the container to set
	 */
    public void setContainer(SipContext container) {
        this.container = container;
    }

    /**
	 * Removes a sip session from the manager by its key
	 * @param key the identifier for this session
	 * @return the sip session that had just been removed, null otherwise
	 */
    public MobicentsSipSession removeSipSession(final MobicentsSipSessionKey key) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing a sip session with the key : " + key);
        }
        return sipSessions.remove(key);
    }

    /**
	 * Removes a sip application session from the manager by its key
	 * @param key the identifier for this session
	 * @return the sip application session that had just been removed, null otherwise
	 */
    public MobicentsSipApplicationSession removeSipApplicationSession(final MobicentsSipApplicationSessionKey key) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing a sip application session with the key : " + key);
        }
        MobicentsSipApplicationSession sipApplicationSession = sipApplicationSessions.remove(key);
        if (sipApplicationSession != null) {
            final String appGeneratedKey = sipApplicationSession.getKey().getAppGeneratedKey();
            if (appGeneratedKey != null) {
                sipApplicationSessionsByAppGeneratedKey.remove(appGeneratedKey);
            }
        }
        return sipApplicationSession;
    }

    /**
	 * Retrieve a sip application session from its key. If none exists, one can enforce
	 * the creation through the create parameter to true.
	 * @param key the key identifying the sip application session to retrieve 
	 * @param create if set to true, if no session has been found one will be created
	 * @return the sip application session matching the key
	 */
    public MobicentsSipApplicationSession getSipApplicationSession(final SipApplicationSessionKey key, final boolean create) {
        MobicentsSipApplicationSession sipApplicationSessionImpl = null;
        final String appGeneratedKey = key.getAppGeneratedKey();
        if (appGeneratedKey != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("trying to find sip application session with generated key " + appGeneratedKey);
            }
            sipApplicationSessionImpl = sipApplicationSessionsByAppGeneratedKey.get(appGeneratedKey);
        }
        if (sipApplicationSessionImpl == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("trying to find sip application session with key " + key);
            }
            sipApplicationSessionImpl = sipApplicationSessions.get(key);
        }
        if (sipApplicationSessionImpl == null && create) {
            sipApplicationSessionImpl = createSipApplicationSession(key);
        }
        return sipApplicationSessionImpl;
    }

    protected MobicentsSipApplicationSession createSipApplicationSession(final SipApplicationSessionKey key) {
        MobicentsSipApplicationSession sipApplicationSessionImpl = null;
        final MobicentsSipApplicationSession newSipApplicationSessionImpl = getNewMobicentsSipApplicationSession(key, (SipContext) container);
        final String appGeneratedKey = key.getAppGeneratedKey();
        if (appGeneratedKey != null) {
            sipApplicationSessionImpl = sipApplicationSessionsByAppGeneratedKey.putIfAbsent(appGeneratedKey, newSipApplicationSessionImpl);
            if (sipApplicationSessionImpl == null) {
                sipApplicationSessions.putIfAbsent(key, newSipApplicationSessionImpl);
                scheduleExpirationTimer(newSipApplicationSessionImpl);
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding a sip application session with the key : " + key);
                }
                sipApplicationSessionImpl = newSipApplicationSessionImpl;
            }
        } else {
            sipApplicationSessionImpl = sipApplicationSessions.putIfAbsent(key, newSipApplicationSessionImpl);
            if (sipApplicationSessionImpl == null) {
                scheduleExpirationTimer(newSipApplicationSessionImpl);
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding a sip application session with the key : " + key);
                }
                sipApplicationSessionImpl = newSipApplicationSessionImpl;
            }
        }
        return sipApplicationSessionImpl;
    }

    protected void scheduleExpirationTimer(MobicentsSipApplicationSession sipApplicationSession) {
        final SipContext sipContext = sipApplicationSession.getSipContext();
        if (sipContext != null) {
            if (sipContext.getSipApplicationSessionTimeout() > 0) {
                SipApplicationSessionTimerTask expirationTimerTask = sipContext.getSipApplicationSessionTimerService().createSipApplicationSessionTimerTask(sipApplicationSession);
                expirationTimerTask = sipContext.getSipApplicationSessionTimerService().schedule(expirationTimerTask, sipApplicationSession.getSipApplicationSessionTimeout(), TimeUnit.MILLISECONDS);
                sipApplicationSession.setExpirationTimerTask(expirationTimerTask);
            }
            sipApplicationSession.notifySipApplicationSessionListeners(SipApplicationSessionEventType.CREATION);
        }
    }

    /**
	 * Retrieve a sip session from its key. If none exists, one can enforce
	 * the creation through the create parameter to true. the sip factory cannot be null
	 * if create is set to true.
	 * @param key the key identifying the sip session to retrieve 
	 * @param create if set to true, if no session has been found one will be created
	 * @param sipFactoryImpl needed only for sip session creation.
	 * @param sipApplicationSessionImpl to associate the SipSession with if create is set to true, if false it won't be used
	 * @return the sip session matching the key
	 * @throws IllegalArgumentException if create is set to true and sip Factory is null
	 */
    public MobicentsSipSession getSipSession(final SipSessionKey key, final boolean create, final SipFactoryImpl sipFactoryImpl, final MobicentsSipApplicationSession sipApplicationSessionImpl) {
        if (create && sipFactoryImpl == null) {
            throw new IllegalArgumentException("the sip factory should not be null");
        }
        MobicentsSipSession sipSessionImpl = sipSessions.get(key);
        if (sipSessionImpl == null && create) {
            sipSessionImpl = createSipSession(key, create, sipFactoryImpl, sipApplicationSessionImpl);
        }
        if (sipSessionImpl != null) {
            return setToTag(key, sipSessionImpl);
        }
        return sipSessionImpl;
    }

    protected MobicentsSipSession createSipSession(final SipSessionKey key, final boolean create, final SipFactoryImpl sipFactoryImpl, final MobicentsSipApplicationSession sipApplicationSessionImpl) {
        MobicentsSipSession sipSessionImpl = null;
        final MobicentsSipSession newSipSessionImpl = getNewMobicentsSipSession(key, sipFactoryImpl, sipApplicationSessionImpl);
        if (sipApplicationSessionImpl.getSipContext() != null) {
            newSipSessionImpl.notifySipSessionListeners(SipSessionEventType.CREATION);
        }
        sipSessionImpl = sipSessions.putIfAbsent(key, newSipSessionImpl);
        if (sipSessionImpl == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding a sip session with the key : " + key);
            }
            sipSessionImpl = newSipSessionImpl;
        }
        return sipSessionImpl;
    }

    protected MobicentsSipSession setToTag(final SipSessionKey key, final MobicentsSipSession sipSession) {
        final String currentKeyToTag = key.getToTag();
        final MobicentsSipSessionKey existingKey = sipSession.getKey();
        final String toTag = existingKey.getToTag();
        if (toTag == null && currentKeyToTag != null) {
            existingKey.setToTag(currentKeyToTag, false);
            if (logger.isDebugEnabled()) {
                logger.debug("Setting the To tag " + currentKeyToTag + " to the session " + key);
            }
        } else if (currentKeyToTag != null && !toTag.equals(currentKeyToTag)) {
            MobicentsSipSession derivedSipSession = sipSession.findDerivedSipSession(currentKeyToTag);
            if (derivedSipSession == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Original session " + key + " with To Tag " + toTag + " creates new derived session with following to Tag " + currentKeyToTag);
                }
                key.setToTag(currentKeyToTag, true);
                derivedSipSession = createDerivedSipSession(sipSession, key);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Original session " + key + " with To Tag " + toTag + " already has a derived session differnt than the following to Tag " + currentKeyToTag + " - reusing it");
                }
            }
            return derivedSipSession;
        }
        return sipSession;
    }

    public void changeSessionKey(SipSessionKey oldKey, SipSessionKey newKey) {
        MobicentsSipSession session = this.sipSessions.get(oldKey);
        if (session == null) throw new IllegalArgumentException("oldKey doesn't exist in this application session.");
        this.sipSessions.put(newKey, session);
        this.sipSessions.remove(oldKey);
    }

    /**
	 * clone the parent sip session given in parameter except its attributes (they will be shared) 
	 * and add it to the internal map of derived sessions identifying it by its ToTag
	 * 
	 * @param parentSipSession the parent sip session holding the newly created derived session
	 * @param sessionKey the key of the new derived session to create
	 * @return the newly created derived session
	 */
    protected MobicentsSipSession createDerivedSipSession(MobicentsSipSession parentSipSession, SipSessionKey sessionKey) {
        MobicentsSipSession sipSessionImpl = getNewMobicentsSipSession(sessionKey, sipFactoryImpl, parentSipSession.getSipApplicationSession());
        sipSessionImpl.setSipSessionAttributeMap(parentSipSession.getSipSessionAttributeMap());
        try {
            sipSessionImpl.setHandler(parentSipSession.getHandler());
        } catch (ServletException e) {
            logger.error("Problem creating derived session", e);
        }
        sipSessionImpl.setState(State.INITIAL);
        sipSessionImpl.setStateInfo(parentSipSession.getStateInfo());
        sipSessionImpl.setProxy(parentSipSession.getProxy());
        if (parentSipSession.getSipSubscriberURI() != null) {
            sipSessionImpl.setSipSubscriberURI(parentSipSession.getSipSubscriberURI());
        }
        sipSessionImpl.setUserPrincipal(parentSipSession.getUserPrincipal());
        sipSessionImpl.setParentSession(parentSipSession);
        parentSipSession.addDerivedSipSessions(sipSessionImpl);
        if (parentSipSession.getSipApplicationSession().getSipContext() != null) {
            sipSessionImpl.notifySipSessionListeners(SipSessionEventType.CREATION);
        }
        return sipSessionImpl;
    }

    /**
	 * Retrieve all sip sessions currently hold by the session manager
	 * @return an iterator on the sip sessions
	 */
    public Iterator<MobicentsSipSession> getAllSipSessions() {
        return sipSessions.values().iterator();
    }

    /**
	 * Retrieve all sip application sessions currently hold by the session manager
	 * @return an iterator on the sip sessions
	 */
    public int getNumberOfSipApplicationSessions() {
        return sipApplicationSessions.size();
    }

    /**
	 * Retrieve all sip sessions currently hold by the session manager
	 * @return an iterator on the sip sessions
	 */
    public int getNumberOfSipSessions() {
        return sipSessions.size();
    }

    /**
	 * Retrieve all sip application sessions currently hold by the session manager
	 * @return an iterator on the sip sessions
	 */
    public Iterator<MobicentsSipApplicationSession> getAllSipApplicationSessions() {
        return sipApplicationSessions.values().iterator();
    }

    /**
	 * Retrieves the sip application session holding the converged http session in parameter
	 * @param convergedHttpSession the converged session to look up
	 * @return the sip application session holding a reference to it or null if none references it
	 */
    public MobicentsSipApplicationSession findSipApplicationSession(HttpSession httpSession) {
        for (MobicentsSipApplicationSession sipApplicationSessionImpl : sipApplicationSessions.values()) {
            if (sipApplicationSessionImpl.findHttpSession(httpSession.getId()) != null) {
                return sipApplicationSessionImpl;
            }
        }
        return null;
    }

    /**
	 * 
	 */
    public void dumpSipSessions() {
        if (logger.isDebugEnabled()) {
            logger.debug("sip sessions present in the session manager");
            for (SipSessionKey sipSessionKey : sipSessions.keySet()) {
                logger.debug(sipSessionKey.toString());
            }
        }
    }

    /**
	 * 
	 */
    public void dumpSipApplicationSessions() {
        if (logger.isDebugEnabled()) {
            logger.debug("sip application sessions present in the session manager");
            for (SipApplicationSessionKey sipApplicationSessionKey : sipApplicationSessions.keySet()) {
                logger.debug(sipApplicationSessionKey.toString() + "/hashed_app_name=" + sipFactoryImpl.getSipApplicationDispatcher().getHashFromApplicationName(sipApplicationSessionKey.getApplicationName()));
            }
        }
    }

    /**
	 * Remove the sip sessions and sip application sessions 
	 */
    public void removeAllSessions() {
        List<SipSessionKey> sipSessionsToRemove = new ArrayList<SipSessionKey>();
        for (SipSessionKey sipSessionKey : sipSessions.keySet()) {
            sipSessionsToRemove.add(sipSessionKey);
        }
        for (SipSessionKey sipSessionKey : sipSessionsToRemove) {
            removeSipSession(sipSessionKey);
        }
        List<SipApplicationSessionKey> sipApplicationSessionsToRemove = new ArrayList<SipApplicationSessionKey>();
        for (SipApplicationSessionKey sipApplicationSessionKey : sipApplicationSessions.keySet()) {
            sipApplicationSessionsToRemove.add(sipApplicationSessionKey);
        }
        for (SipApplicationSessionKey sipApplicationSessionKey : sipApplicationSessionsToRemove) {
            removeSipApplicationSession(sipApplicationSessionKey);
        }
    }

    protected abstract MobicentsSipSession getNewMobicentsSipSession(SipSessionKey key, SipFactoryImpl sipFactoryImpl, MobicentsSipApplicationSession mobicentsSipApplicationSession);

    protected abstract MobicentsSipApplicationSession getNewMobicentsSipApplicationSession(SipApplicationSessionKey key, SipContext sipContext);

    /**
	 * Return the maximum number of active Sessions allowed, or -1 for no limit.
	 */
    public int getMaxActiveSipSessions() {
        return (this.maxActiveSipSessions);
    }

    /**
	 * Set the maximum number of actives Sip Sessions allowed, or -1 for no
	 * limit.
	 * 
	 * @param max
	 *            The new maximum number of sip sessions
	 */
    public void setMaxActiveSipSessions(int max) {
        this.maxActiveSipSessions = max;
    }

    /**
	 * Return the maximum number of active Sessions allowed, or -1 for no limit.
	 */
    public int getMaxActiveSipApplicationSessions() {
        return (this.maxActiveSipApplicationSessions);
    }

    /**
	 * Set the maximum number of actives Sip Application Sessions allowed, or -1
	 * for no limit.
	 * 
	 * @param max
	 *            The new maximum number of sip application sessions
	 */
    public void setMaxActiveSipApplicationSessions(int max) {
        this.maxActiveSipApplicationSessions = max;
    }

    /**
	 * Number of sip session creations that failed due to maxActiveSipSessions
	 * 
	 * @return The count
	 */
    public int getRejectedSipSessions() {
        return rejectedSipSessions;
    }

    public void setRejectedSipSessions(int rejectedSipSessions) {
        this.rejectedSipSessions = rejectedSipSessions;
    }

    /**
	 * Number of sip session creations that failed due to maxActiveSipSessions
	 * 
	 * @return The count
	 */
    public int getRejectedSipApplicationSessions() {
        return rejectedSipApplicationSessions;
    }

    public void setRejectedSipApplicationSessions(int rejectedSipApplicationSessions) {
        this.rejectedSipApplicationSessions = rejectedSipApplicationSessions;
    }

    public void setSipSessionCounter(int sipSessionCounter) {
        this.sipSessionCounter = sipSessionCounter;
    }

    /**
	 * Total sessions created by this manager.
	 * 
	 * @return sessions created
	 */
    public int getSipSessionCounter() {
        return sipSessionCounter;
    }

    /**
	 * Returns the number of active sessions
	 * 
	 * @return number of sessions active
	 */
    public int getActiveSipSessions() {
        return sipSessions.size();
    }

    /**
	 * Gets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @return Longest time (in seconds) that an expired session had been alive.
	 */
    public int getSipSessionMaxAliveTime() {
        return sipSessionMaxAliveTime;
    }

    /**
	 * Sets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @param sessionMaxAliveTime
	 *            Longest time (in seconds) that an expired session had been
	 *            alive.
	 */
    public void setSipSessionMaxAliveTime(int sipSessionMaxAliveTime) {
        this.sipSessionMaxAliveTime = sipSessionMaxAliveTime;
    }

    /**
	 * Gets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @return Average time (in seconds) that expired sessions had been alive.
	 */
    public int getSipSessionAverageAliveTime() {
        return sipSessionAverageAliveTime;
    }

    /**
	 * Sets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @param sessionAverageAliveTime
	 *            Average time (in seconds) that expired sessions had been
	 *            alive.
	 */
    public void setSipSessionAverageAliveTime(int sipSessionAverageAliveTime) {
        this.sipSessionAverageAliveTime = sipSessionAverageAliveTime;
    }

    public void setSipApplicationSessionCounter(int sipApplicationSessionCounter) {
        this.sipApplicationSessionCounter = sipApplicationSessionCounter;
    }

    /**
	 * Total sessions created by this manager.
	 * 
	 * @return sessions created
	 */
    public int getSipApplicationSessionCounter() {
        return sipApplicationSessionCounter;
    }

    /**
	 * Returns the number of active sessions
	 * 
	 * @return number of sessions active
	 */
    public int getActiveSipApplicationSessions() {
        return sipApplicationSessions.size();
    }

    /**
	 * Gets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @return Longest time (in seconds) that an expired session had been alive.
	 */
    public int getSipApplicationSessionMaxAliveTime() {
        return sipApplicationSessionMaxAliveTime;
    }

    /**
	 * Sets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @param sessionMaxAliveTime
	 *            Longest time (in seconds) that an expired session had been
	 *            alive.
	 */
    public void setSipApplicationSessionMaxAliveTime(int sipApplicationSessionMaxAliveTime) {
        this.sipApplicationSessionMaxAliveTime = sipApplicationSessionMaxAliveTime;
    }

    /**
	 * Gets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @return Average time (in seconds) that expired sessions had been alive.
	 */
    public int getSipApplicationSessionAverageAliveTime() {
        return sipApplicationSessionAverageAliveTime;
    }

    /**
	 * Sets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @param sessionAverageAliveTime
	 *            Average time (in seconds) that expired sessions had been
	 *            alive.
	 */
    public void setSipApplicationSessionAverageAliveTime(int sipApplicationSessionAverageAliveTime) {
        this.sipApplicationSessionAverageAliveTime = sipApplicationSessionAverageAliveTime;
    }

    /**
	 * Gets the number of sessions that have expired.
	 * 
	 * @return Number of sessions that have expired
	 */
    public int getExpiredSipSessions() {
        return expiredSipSessions;
    }

    /**
	 * Sets the number of sessions that have expired.
	 * 
	 * @param expiredSessions
	 *            Number of sessions that have expired
	 */
    public void setExpiredSipSessions(int expiredSipSessions) {
        this.expiredSipSessions = expiredSipSessions;
    }

    /**
	 * Gets the number of sessions that have expired.
	 * 
	 * @return Number of sessions that have expired
	 */
    public int getExpiredSipApplicationSessions() {
        return expiredSipApplicationSessions;
    }

    /**
	 * Sets the number of sessions that have expired.
	 * 
	 * @param expiredSessions
	 *            Number of sessions that have expired
	 */
    public void setExpiredSipApplicationSessions(int expiredSipApplicationSessions) {
        this.expiredSipApplicationSessions = expiredSipApplicationSessions;
    }

    public double getNumberOfSipApplicationSessionCreationPerSecond() {
        return lastAverageSasCreationPerSecond;
    }

    public double getNumberOfSipSessionCreationPerSecond() {
        return lastAverageSsCreationPerSecond;
    }

    public void updateStats() {
        if (logger.isTraceEnabled()) {
            logger.trace("updating sip manager " + container.getApplicationName() + " statistics");
        }
        long now = System.currentTimeMillis();
        int elapsedNumberOfSasCreationCounter = sipApplicationSessionCounter - lastUpdatedSasCreationCounter;
        if (elapsedNumberOfSasCreationCounter > 0) {
            double elapsedSasCreationUpdatedTimeInSeconds = (now - lastSipApplicationSessionUpdatedTime) / 1000;
            double elapsedAverageSasCreationPerSecond = elapsedNumberOfSasCreationCounter / elapsedSasCreationUpdatedTimeInSeconds;
            lastAverageSasCreationPerSecond = (lastAverageSasCreationPerSecond + elapsedAverageSasCreationPerSecond) / 2;
        }
        lastUpdatedSasCreationCounter = sipApplicationSessionCounter;
        lastSipApplicationSessionUpdatedTime = now;
        if (logger.isTraceEnabled()) {
            logger.trace("elapsedNumberOfSasCreationCounter " + elapsedNumberOfSasCreationCounter);
            logger.trace("lastUpdatedSasCreationCounter " + lastUpdatedSasCreationCounter);
            logger.trace("lastSipApplicationSessionUpdatedTime " + lastSipApplicationSessionUpdatedTime);
        }
        int elapsedNumberOfSsCreationCounter = sipSessionCounter - lastUpdatedSsCreationCounter;
        if (elapsedNumberOfSsCreationCounter > 0) {
            double elapsedSsCreationUpdatedTimeInSeconds = (now - lastSipSessionUpdatedTime) / 1000;
            double elapsedAverageSsCreationPerSecond = elapsedNumberOfSsCreationCounter / elapsedSsCreationUpdatedTimeInSeconds;
            lastAverageSsCreationPerSecond = (lastAverageSsCreationPerSecond + elapsedAverageSsCreationPerSecond) / 2;
        }
        lastUpdatedSsCreationCounter = sipSessionCounter;
        lastSipSessionUpdatedTime = now;
        if (logger.isTraceEnabled()) {
            logger.trace("elapsedNumberOfSsCreationCounter " + elapsedNumberOfSsCreationCounter);
            logger.trace("lastUpdatedSsCreationCounter " + lastUpdatedSsCreationCounter);
            logger.trace("lastSipSessionUpdatedTime " + lastSipSessionUpdatedTime);
        }
    }
}
