package com.sts.webmeet.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import com.sts.webmeet.api.AbstractScheduledPluginTask;
import com.sts.webmeet.api.LockToggleListener;
import com.sts.webmeet.api.PluginMessage;
import com.sts.webmeet.api.PluginScheduledTaskContext;
import com.sts.webmeet.api.RecordingToggleListener;
import com.sts.webmeet.api_impl.PluginContextImpl;
import com.sts.webmeet.common.AntiModeratorMessage;
import com.sts.webmeet.common.Constants;
import com.sts.webmeet.common.Droppable;
import com.sts.webmeet.common.EjectMessage;
import com.sts.webmeet.common.EndMeetingMessage;
import com.sts.webmeet.common.ExceptionMessage;
import com.sts.webmeet.common.IOUtil;
import com.sts.webmeet.common.LockUnlockMeetingMessage;
import com.sts.webmeet.common.MeetingLockedStatusMessage;
import com.sts.webmeet.common.ModeratorInfoMessage;
import com.sts.webmeet.common.ModeratorMessage;
import com.sts.webmeet.common.OpenMeetingMessage;
import com.sts.webmeet.common.ParticipantInfo;
import com.sts.webmeet.common.PingClientMessage;
import com.sts.webmeet.common.RecordedWebmeetMessage;
import com.sts.webmeet.common.RecordingOffMessage;
import com.sts.webmeet.common.RecordingOnMessage;
import com.sts.webmeet.common.RecordingStartStopMessage;
import com.sts.webmeet.common.RosterChangedMessage;
import com.sts.webmeet.common.RosterExitMessage;
import com.sts.webmeet.common.RosterForceoffMessage;
import com.sts.webmeet.common.RosterJoinAcceptMessage;
import com.sts.webmeet.common.RosterJoinMessage;
import com.sts.webmeet.common.RosterMessage;
import com.sts.webmeet.common.RosterRequestMessage;
import com.sts.webmeet.common.RosterRosterMessage;
import com.sts.webmeet.common.ViewControlMessage;
import com.sts.webmeet.common.WebmeetMessage;
import com.sts.webmeet.content.common.ContentMessage;
import com.sts.webmeet.content.common.ScriptIndexMessage;
import com.sts.webmeet.content.common.ScriptItemImpl;
import com.sts.webmeet.content.common.ScriptItemSelectedMessage;
import com.sts.webmeet.content.common.ScriptMessage;
import com.sts.webmeet.content.common.ScriptReplyMessage;
import com.sts.webmeet.content.common.ScriptRequestMessage;
import com.sts.webmeet.content.server.AbstractPluginServer;
import com.sts.webmeet.content.server.Server;
import com.sts.webmeet.content.server.ServerContext;
import com.sts.webmeet.pluginmanager.PluginManager;
import com.sts.webmeet.server.interfaces.*;
import com.sts.webmeet.web.ImageDBUtil;
import com.sts.webmeet.web.util.MailUtil;
import com.sts.webmeet.web.util.Util;
import com.sts.webmeet.web.util.FreeMarkerUtil;
import com.sts.webmeet.server.util.FileUtil;

class LiveConference extends Conference implements ServerContext {

    public LiveConference(String strID, OutboundConnectionServer outServer, ConferenceEndedListener endListener, String strCC, String strExp, String strCustomerID, String strPassword) {
        super(strID, outServer, endListener, strCC, strExp, strCustomerID);
        hashServerIDToPartInfo = new Hashtable();
        this.password = strPassword;
    }

    public void switchView(ScriptItemImpl item, String strServerID) {
        String strPackage = item.getShortPackageName();
        String strLongPackage = "com.sts.webmeet.content.client." + strPackage + ".Content";
        if (getCurrentView().equals(strLongPackage) && null == strServerID) {
            return;
        }
        handleViewControlMessage(new ViewControlMessage(strLongPackage), strServerID);
    }

    public void init() {
        try {
            super.init();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        bCapture = confData.getRecorded();
        if (bCapture) {
            try {
                bCapture = initRecording();
            } catch (Exception e) {
                e.printStackTrace();
                bCapture = false;
            }
        }
        Timer timerRoster = new Timer(true);
        this.listTimers.add(timerRoster);
        TimerTask taskRoster = new TimerTask() {

            public void run() {
                alertRosterChanged();
            }
        };
        timerRoster.schedule(taskRoster, new Date(), ROSTER_PERIOD);
        Timer timerPing = new Timer(true);
        this.listTimers.add(timerPing);
        TimerTask taskPing = new TimerTask() {

            public void run() {
                pingEverybody();
            }
        };
        timerPing.schedule(taskPing, new Date(), PING_PERIOD);
    }

    public void setContentTypes(String[] astrTypes, AbstractPluginServer[] pluginServers) {
        try {
            Server server = null;
            for (int i = 0; i < astrTypes.length; i++) {
                server = (Server) Class.forName(astrTypes[i]).newInstance();
                hashServers.put(astrTypes[i], server);
                server.init(this);
            }
            for (int i = 0; i < pluginServers.length; i++) {
                hashServers.put(pluginServers[i].getClass().getName(), pluginServers[i]);
                pluginServers[i].init(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error instantiating content servers");
            throw new RuntimeException("error instantiating content servers");
        }
    }

    public void sendMessageToAll(WebmeetMessage message) {
        broadcastMessage(message);
    }

    public void sendMessageToAllButSelf(WebmeetMessage message) {
        broadcastMessage(message, message.getSender().getServerID());
    }

    public void sendMessageToAllExcept(WebmeetMessage message, String[] astrServerID) {
        broadcastMessage(message, astrServerID);
    }

    public void sendMessageToParticipant(WebmeetMessage message, String strServerID) {
        sendAddressedMessage(message, strServerID);
    }

    public void sendMessageToParticipant(WebmeetMessage message) {
        sendAddressedMessage(message);
    }

    public String getMeetingID() {
        return strConfID;
    }

    public void handleDisconnect(String strServerID) {
        ParticipantInfo pi = (ParticipantInfo) hashServerIDToPartInfo.get(strServerID);
        if (null != pi) {
            logger.info("\tLog disconnect to DB:" + Thread.currentThread().getName());
            hashServerIDToPartInfo.remove(strServerID);
            logger.info("participant removed [disconnect]: " + pi.getLabel());
            super.logMeetingExit(strServerID);
            if (hashServerIDToPartInfo.size() == 0) {
                startReaper();
            } else {
                rosterChanged();
            }
        }
    }

    public boolean handleObject(Object obj) {
        WebmeetMessage mess = (WebmeetMessage) obj;
        logger.debug("Got WebmeetMessage: " + obj);
        if (mess instanceof RosterMessage) {
            if (mess instanceof RosterJoinMessage) {
                return handleJoin((RosterJoinMessage) mess);
            } else if (mess instanceof RosterRequestMessage) {
                return handleRosterRequest(mess);
            } else if (mess instanceof RosterForceoffMessage) {
                return sendAddressedMessage(mess);
            } else if (mess instanceof RosterExitMessage) {
                return handleRosterExit(mess);
            } else {
                logger.info("warning: unhandled roster message");
                return true;
            }
        } else if (mess instanceof OpenMeetingMessage) {
            return handleOpenMeeting((OpenMeetingMessage) mess);
        } else if (mess instanceof PluginMessage) {
            processPluginMessage((PluginMessage) mess);
            return true;
        } else if (mess instanceof ContentMessage) {
            processContentMessage((ContentMessage) mess);
            return true;
        } else if (mess instanceof ExceptionMessage) {
            return handleExceptionMessage((ExceptionMessage) mess);
        } else if (mess instanceof ViewControlMessage) {
            return handleViewControlMessage((ViewControlMessage) mess, mess.getRecipient() == null ? null : mess.getRecipient().getServerID());
        } else if (mess instanceof ModeratorMessage) {
            return handleModeratorMessage((ModeratorMessage) mess);
        } else if (mess instanceof ScriptMessage) {
            return handleScriptMessage((ScriptMessage) mess);
        } else if (mess instanceof EndMeetingMessage) {
            logger.info("Got end meeting message");
            handleEndMeeting((EndMeetingMessage) mess);
            return true;
        } else if (mess instanceof RecordingStartStopMessage) {
            startStopRecording();
            return true;
        } else if (mess instanceof LockUnlockMeetingMessage) {
            handleLockUnlock();
            return true;
        } else if (mess instanceof EjectMessage) {
            handleEjectMessage((EjectMessage) mess);
            return true;
        } else {
            forwardMessage(mess);
            return true;
        }
    }

    public ScriptItemImpl getCurrentScriptItem() {
        if (0 == script.size()) {
            return null;
        }
        return script.getItemAt(iCurrentScriptItem);
    }

    public void forwardMessage(WebmeetMessage message) {
        if (message.getRecipient() == null) {
            broadcastMessage(message, message.getSender().getServerID());
        } else {
            this.sendAddressedMessage(message);
        }
    }

    public void sendMessageToModerator(WebmeetMessage message) {
        sendAddressedMessage(message, piModerator.getServerID());
    }

    public int getParticipantCount() {
        return hashServerIDToPartInfo.size();
    }

    private void handleLockUnlock() {
        if (this.isLockedNow()) {
            unlockMeeting();
        } else {
            lockMeeting();
        }
        this.serversHandleToggleLock(this.isLockedNow());
    }

    private void lockMeeting() {
        lockUnlockMeeting(true);
    }

    private void unlockMeeting() {
        lockUnlockMeeting(false);
    }

    private void lockUnlockMeeting(boolean bLocked) {
        this.bLocked = bLocked;
        try {
            MeetingLocal meeting = this.getLocalMeeting();
            meeting.setLocked(bLocked);
            this.broadcastMessage(new MeetingLockedStatusMessage(bLocked));
        } catch (Exception e) {
            logger.error("unable to set meeting locked state", e);
        }
    }

    private void cancelTimers() {
        Timer[] timers = (Timer[]) listTimers.toArray(new Timer[0]);
        for (int i = 0; i < timers.length; i++) {
            timers[i].cancel();
            timers[i] = null;
        }
        listTimers.clear();
        listTimers = null;
    }

    private void processPluginMessage(PluginMessage message) {
        logger.info(".processPluginMessage: " + message);
        String name = message.getServerClassName();
        Server server = (Server) hashServers.get(name);
        server.processMessage((ContentMessage) message);
    }

    private void processContentMessage(ContentMessage message) {
        logger.debug(".processContentMessage: " + message);
        ((Server) (hashServers.get(message.getServerPackageName()))).processMessage(message);
    }

    private void handleEndMeeting(EndMeetingMessage mess) {
        bIsOver = true;
        logger.debug("stopping recording...");
        this.stopRecording();
        logger.debug("...returned from stopping recording.");
        String strDonePage = getDonePage();
        mess.setDonePage(strDonePage);
        this.broadcastMessage(mess);
        logger.debug("starting reaper...");
        startReaper();
        logger.debug("...done starting reaper.");
    }

    private String getDonePage() {
        String strDonePage = confData.getDonePage();
        if (null == strDonePage || strDonePage.length() < 1) {
            strDonePage = System.getProperty("webhuddle.property.done.url", "donepage.jsp");
        }
        return strDonePage;
    }

    private void startReaper() {
        if (!bReaperStarted) {
            timerReaper = new Timer(true);
            TimerTask taskReaper = new TimerTask() {

                public void run() {
                    if (bIsOver || hashServerIDToPartInfo.size() == 0) {
                        logger.debug("running reaper");
                        disconnectEverybody();
                        endMeeting();
                        logger.debug("returned from endMeeting");
                        this.cancel();
                    }
                    bReaperStarted = false;
                }
            };
            timerReaper.schedule(taskReaper, REAPER_WAIT);
            logger.debug("reaper task scheduled");
            bReaperStarted = true;
        }
    }

    private void disconnectEverybody() {
        logger.info("disconnectEverybody...");
        String[] astrIDs = getServerIDs();
        for (int i = 0; i < astrIDs.length; i++) {
            logger.info("disconnecting client: " + astrIDs[i]);
            outServer.disconnectClient(astrIDs[i]);
        }
    }

    private boolean handleScriptMessage(ScriptMessage mess) {
        if (mess instanceof ScriptItemSelectedMessage) {
            iCurrentScriptItem = ((ScriptItemSelectedMessage) mess).getItemIndex();
            ScriptItemImpl item = script.getItemAt(iCurrentScriptItem);
            String strClass = item.getClassName();
            String strServer = strClass.replaceFirst("content.common", "content.server").replaceFirst(".ScriptItem", "");
            switchView(item, mess.getRecipient() == null ? null : mess.getRecipient().getServerID());
            ((Server) hashServers.get(strServer)).handleScriptItemSelected(item, null);
            broadcastMessage(new ScriptIndexMessage(iCurrentScriptItem));
            return true;
        } else if (mess instanceof ScriptRequestMessage) {
            sendAddressedMessage(new ScriptReplyMessage(script, mess.getSender()));
            sendAddressedMessage(new ScriptIndexMessage(iCurrentScriptItem), mess.getSender().getServerID());
            if (bCapture && !bScriptRecorded && null != script) {
                try {
                    recordMessage(new ScriptReplyMessage(script, null));
                    bScriptRecorded = true;
                    recordMessage(new ViewControlMessage(getCurrentView(), null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private boolean handleOpenMeeting(OpenMeetingMessage message) {
        if (PasswordUtil.checkMeetingKey(message.getMeetingID(), message.getMeetingKey()) && checkMeetingOwner(message.getCustomerID())) {
            bOpen = true;
            try {
            } catch (Exception e) {
                logger.error("problem sending meeting started email", e);
            }
        }
        return true;
    }

    private boolean checkMeetingOwner(String strID) {
        try {
            return strID.equals(getLocalMeeting().getCustomer().getCustomerId() + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean handleRosterRequest(WebmeetMessage mess) {
        sendAddressedMessage(new ModeratorInfoMessage(piModerator, password), mess.getSender().getServerID());
        return sendAddressedMessage(new RosterRosterMessage(getRoster(), mess.getSender()));
    }

    private ParticipantInfo[] getRoster() {
        List listRoster = new ArrayList(hashServerIDToPartInfo.values());
        Collections.sort(listRoster, new Comparator() {

            public int compare(Object obj1, Object obj2) {
                return ((ParticipantInfo) obj1).getLabel().compareTo(((ParticipantInfo) obj2).getLabel());
            }
        });
        return (ParticipantInfo[]) listRoster.toArray(new ParticipantInfo[0]);
    }

    private String[] getServerIDs() {
        String[] astrID = new String[hashServerIDToPartInfo.size()];
        Enumeration enumer = hashServerIDToPartInfo.keys();
        int i = 0;
        while (enumer.hasMoreElements()) {
            astrID[i++] = (String) enumer.nextElement();
        }
        return astrID;
    }

    protected void finalize() {
        logger.info(getClass().getName() + " finalized.");
    }

    boolean handleRosterExit(WebmeetMessage mess) {
        logger.info("handling RosterExit: ");
        logger.info("\t" + outServer.getClass().getName());
        outServer.disconnectClient(mess.getSender().getServerID());
        return true;
    }

    private void endMeeting() {
        if (null != oosRecording) {
            try {
                endRecording();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.lElapsedTimeMillis > 0) {
            super.logMeetingEnd((int) (this.lElapsedTimeMillis / 1000));
        } else {
            super.logMeetingEnd();
        }
        Enumeration enumer = hashServers.elements();
        while (enumer.hasMoreElements()) {
            ((Server) enumer.nextElement()).destroy();
        }
        hashServers.clear();
        this.cancelTimers();
    }

    private boolean handleModeratorMessage(ModeratorMessage mess) {
        if (null != piModerator) {
            try {
                sendAddressedMessage(new AntiModeratorMessage(piModerator));
            } catch (Exception e) {
                logger.info("error sending anti-moderator" + " to: " + piModerator.getLabel());
                e.printStackTrace();
            }
        }
        piModerator = mess.getRecipient();
        sendAddressedMessage(mess);
        return broadcastMessage(new ModeratorInfoMessage(mess.getRecipient(), this.password));
    }

    private void handleEjectMessage(EjectMessage mess) {
        logger.info("ejecting " + mess.getRecipient());
        sendAddressedMessage(mess);
    }

    private boolean handleViewControlMessage(ViewControlMessage mess, String strServerID) {
        setCurrentView(mess.getViewClassName());
        if (null == strServerID) {
            return broadcastMessage(mess);
        } else {
            return sendAddressedMessage(mess, strServerID);
        }
    }

    private void alertRosterChanged() {
        if (bRosterChanged && !bIsOver) {
            bRosterChanged = false;
            broadcastRosterChanged();
            if (bCapture) {
                recordNewRoster();
            }
        }
    }

    private void pingEverybody() {
        Date now = new Date();
        if (null == dateLastBroadcast || (now.getTime() - dateLastBroadcast.getTime()) > PING_PERIOD) {
            broadcastMessage(new PingClientMessage());
        }
    }

    private void rosterChanged() {
        bRosterChanged = true;
    }

    private void broadcastRosterChanged() {
        broadcastMessage(new RosterChangedMessage());
    }

    private void recordNewRoster() {
        try {
            recordMessage(new RosterRosterMessage(getRoster(), null));
        } catch (Exception e) {
            e.printStackTrace();
            bCapture = false;
        }
    }

    protected boolean handleJoin(RosterJoinMessage mess) {
        ParticipantInfo pi = mess.getSender();
        logger.info(getClass().getName() + ".handleJoin: " + pi.getLabel());
        boolean bNewConnection = true;
        if (checkParticipantID(mess)) {
            String strParticipantID = mess.getParticipantID();
            if (hashServerIDToPartInfo.containsKey(strParticipantID)) {
                bNewConnection = false;
                outServer.disconnectClient(strParticipantID);
            } else {
                if (this.isLockedNow()) {
                    outServer.participantNotAuthorized(pi.getServerID());
                    logger.info("participant blocked -- meeting is locked: " + pi);
                    return false;
                }
            }
            hashServerIDToPartInfo.put(strParticipantID, pi);
            logger.debug("renaming client: " + pi.getServerID() + " " + mess.getParticipantID());
            outServer.renameClient(pi.getServerID(), mess.getParticipantID());
            outServer.participantAuthorized(mess.getParticipantID());
            pi.setServerID(strParticipantID);
            if (this.piOwner == null) {
                this.piOwner = pi;
            }
            sendMessageToParticipant(new RosterJoinAcceptMessage(), mess.getParticipantID());
            if (bNewConnection) {
                sendAddressedMessage(new ViewControlMessage(getCurrentView()), mess.getParticipantID());
                notifyServersJoin(mess.getParticipantID());
                super.logMeetingJoin(mess);
            }
            rosterChanged();
            return true;
        } else {
            logger.info("@@@ bad password @@@");
            outServer.participantNotAuthorized(pi.getServerID());
            return false;
        }
    }

    private void notifyServersJoin(String strParticipantID) {
        Enumeration enumerServers = hashServers.elements();
        while (enumerServers.hasMoreElements()) {
            Server server = (Server) enumerServers.nextElement();
            server.userJoined(strParticipantID);
            if (bCapture && !bFirstJoinSent) {
                server.userJoined(null);
            }
        }
        bFirstJoinSent = true;
    }

    private boolean broadcastMessage(WebmeetMessage mess) {
        return broadcastMessage(mess, new String[0]);
    }

    private boolean broadcastMessage(WebmeetMessage mess, String strExcept) {
        String[] astr = null;
        if (null != strExcept) {
            astr = new String[1];
            astr[0] = strExcept;
        } else {
            astr = new String[0];
        }
        return broadcastMessage(mess, astr);
    }

    private boolean broadcastMessage(WebmeetMessage mess, String[] astrExcept) {
        int iRecipients = hashServerIDToPartInfo.size();
        if (null != astrExcept && astrExcept.length > 0) {
            iRecipients -= astrExcept.length;
        } else {
            dateLastBroadcast = new Date();
        }
        String[] astrClients = new String[iRecipients];
        Enumeration enumer = hashServerIDToPartInfo.keys();
        List listExcepts = Arrays.asList(astrExcept);
        for (int i = 0; enumer.hasMoreElements() && i < astrClients.length; ) {
            String strNext = (String) enumer.nextElement();
            if (!listExcepts.contains(strNext)) {
                astrClients[i++] = strNext;
            }
        }
        outServer.broadcastObject(mess, astrClients);
        if (bCapture && !(mess instanceof PingClientMessage)) {
            try {
                recordMessage(mess);
            } catch (Exception e) {
                e.printStackTrace();
                bCapture = false;
            }
        }
        return true;
    }

    private boolean initRecording() throws Exception {
        fileRecordingDir = new File(com.sts.webmeet.server.util.Recordings.getRecordingDir(strConfID));
        fileRecordingDir.mkdirs();
        zosArchive = new ZipOutputStream(new FileOutputStream(fileRecordingDir.getAbsolutePath() + "/" + com.sts.webmeet.server.util.Recordings.getArchiveForConf(strConfID)));
        zosArchive.putNextEntry(new ZipEntry(strConfID + "/" + PlaybackConstants.STREAM_FILE_ARCHIVE));
        zosStream = new ZipOutputStream(zosArchive);
        zosStream.putNextEntry(new ZipEntry(PlaybackConstants.STREAM_FILE));
        oosRecording = new ObjectOutputStream(zosStream);
        dateStart = new Date();
        logger.info("recording inited (" + fileRecordingDir.getAbsolutePath() + ")");
        return true;
    }

    private void endRecording() throws Exception {
        oosRecording.flush();
        zosStream.finish();
        ScriptItemImpl[] items = script.getItems();
        for (int i = 0; i < items.length; i++) {
            if (items[i].getClassName().equals("com.sts.webmeet.content.server.slides.Server") && -1 != ((Integer) items[i].getItem()).intValue()) {
                ZipEntry entry = new ZipEntry(strConfID + "/" + (Integer) items[i].getItem());
                zosArchive.putNextEntry(entry);
                logger.info("trying to get image: " + (Integer) items[i].getItem());
                ImageDBUtil.writeImageData(zosArchive, ImageDBUtil.getImageFromDB((Integer) items[i].getItem()));
            }
        }
        InputStream is = null;
        ClassLoader loader = getClass().getClassLoader();
        String strZipRoot = strConfID + "/";
        for (int i = 0; i < PlaybackConstants.STATIC_FILES.length; i++) {
            is = loader.getResourceAsStream(PlaybackConstants.STATIC_FILES[i]);
            if (null != is) {
                writeStreamToZip(is, strZipRoot + PlaybackConstants.STATIC_FILES[i], zosArchive);
            }
        }
        Map map = new HashMap();
        map.put("product", System.getProperty("webhuddle.property.product", "WebHuddle"));
        for (int i = 0; i < PlaybackConstants.TEMPLATE_FILES.length; i++) {
            String strTranslated = FreeMarkerUtil.applyTemplate(PlaybackConstants.TEMPLATE_FILES[i], map);
            if (null != strTranslated) {
                writeStreamToZip(new ByteArrayInputStream(strTranslated.getBytes()), strZipRoot + FileUtil.replaceDotSuffix(PlaybackConstants.TEMPLATE_FILES[i], "html"), zosArchive);
            }
        }
        is = loader.getResourceAsStream(PlaybackConstants.PHP_LIST);
        writeStreamToZip(is, PlaybackConstants.PHP_LIST_OUT, zosArchive);
        is = new ByteArrayInputStream(confData.getName().getBytes());
        writeStreamToZip(is, strZipRoot + PlaybackConstants.NAME_FILE, zosArchive);
        is = new ByteArrayInputStream(confData.getDescription() != null ? confData.getDescription().getBytes() : "[no description]".getBytes());
        writeStreamToZip(is, strZipRoot + PlaybackConstants.DESCRIPTION_FILE, zosArchive);
        Properties recordingProps = new Properties();
        recordingProps.put(Constants.PLAYBACK_MEETING_LENGTH_PARAM, "" + this.lElapsedTimeMillis);
        ByteArrayOutputStream baosProps = new ByteArrayOutputStream();
        recordingProps.store(baosProps, null);
        baosProps.flush();
        is = new ByteArrayInputStream(baosProps.toByteArray());
        writeStreamToZip(is, strZipRoot + Constants.RECORDED_SESSION_INFO_PROPERTIES, zosArchive);
        zosArchive.close();
    }

    public void startStopRecording() {
        if (bRecordingNow) {
            stopRecording();
        } else {
            startRecording();
        }
        serversHandleToggleRecording(isRecordingNow());
    }

    private void startRecording() {
        if (!bRecordingNow) {
            this.bRecordingNow = true;
            this.dateRecordingLastStarted = new Date();
            this.broadcastMessage(new RecordingOnMessage());
        }
    }

    private void stopRecording() {
        if (bRecordingNow) {
            this.bRecordingNow = false;
            this.lElapsedTimeMillis += (new Date()).getTime() - this.dateRecordingLastStarted.getTime();
            this.broadcastMessage(new RecordingOffMessage());
        }
    }

    private void serversHandleToggleRecording(boolean recording) {
        Enumeration enumer = this.hashServers.elements();
        while (enumer.hasMoreElements()) {
            Server server = (Server) enumer.nextElement();
            if (server instanceof RecordingToggleListener) {
                ((RecordingToggleListener) server).recordingToggled(recording);
            }
        }
    }

    private void serversHandleToggleLock(boolean lock) {
        Enumeration enumer = this.hashServers.elements();
        while (enumer.hasMoreElements()) {
            Server server = (Server) enumer.nextElement();
            if (server instanceof LockToggleListener) {
                ((LockToggleListener) server).lockToggled(lock);
            }
        }
    }

    private boolean isRecordingNow() {
        return this.bRecordingNow;
    }

    private boolean isLockedNow() {
        return this.bLocked;
    }

    public synchronized void recordMessage(WebmeetMessage mess) throws Exception {
        if (null == oosRecording) {
            return;
        }
        if (!this.bRecordingNow && mess instanceof Droppable) {
            return;
        }
        RecordedWebmeetMessage recorded = new RecordedWebmeetMessage();
        long dateStamp = this.lElapsedTimeMillis;
        if (this.bRecordingNow) {
            dateStamp = this.lElapsedTimeMillis + ((new Date()).getTime() - dateRecordingLastStarted.getTime());
        }
        recorded.setTimestamp(dateStamp);
        recorded.setConfID(strConfID);
        recorded.setMessage(mess);
        oosRecording.writeObject(recorded);
        oosRecording.flush();
        oosRecording.reset();
    }

    static void writeStreamToFile(InputStream is, String strOutFile) throws IOException {
        BufferedInputStream bis = null;
        if (is instanceof BufferedInputStream) {
            bis = (BufferedInputStream) is;
            logger.info("stream is already buffered.");
        } else {
            bis = new BufferedInputStream(is);
        }
        File file = new File(strOutFile);
        String strParent = file.getParent();
        if (null != strParent) {
            File dir = new File(strParent);
            dir.mkdirs();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        IOUtil.copyStream(bis, bos);
        bos.flush();
        bos.close();
        is.close();
    }

    static void writeStreamToZip(InputStream is, String entry, ZipOutputStream zos) throws java.io.IOException {
        zos.putNextEntry(new ZipEntry(entry));
        for (int iRead = is.read(); iRead != -1; iRead = is.read()) {
            zos.write(iRead);
        }
    }

    private void emailInvitees(String strExtraInstructions) throws Exception {
        InvitationData[] invites = getLocalMeeting().getInvitees();
        for (int i = 0; i < invites.length; i++) {
            sendInviteeEmail(invites[i], strExtraInstructions);
        }
    }

    private void sendInviteeEmail(InvitationData inviteData, String strExtraInstructions) throws Exception {
        String strJoinURL = buildInviteeJoinURL(inviteData.getInvitationId() + "");
        String strFullName = this.piOwner.getName();
        String strSubject = formatWebMessage("invitee.email.personal.subject", new Object[] { Util.getProductName(), strFullName, new Date() });
        String strBody = formatWebMessage("invitee.email.personal.body", new Object[] { strFullName, Util.getProductName(), strJoinURL, strExtraInstructions });
        logger.info("sending email [to " + inviteData.getEmail() + "]: \n" + strBody);
        MailUtil.sendEmailAsync(inviteData.getEmail(), Util.getHelpEmail(), strSubject, strBody);
    }

    private void emailModeratorOnlyInstructions(String strInstructions) throws Exception {
        String strFullName = this.piOwner.getName();
        String strMeetingName = this.getMeetingData().getName();
        String strSubject = this.formatWebMessage("moderator.only.email.subject", new Object[] { Util.getProductName(), strFullName, new Date() });
        String strBody = this.formatWebMessage("moderator.only.email.body", new Object[] { strFullName, Util.getProductName(), strMeetingName, strInstructions });
        logger.debug("sending email:\n" + strBody);
        MailUtil.sendEmailAsync(this.piOwner.getEmail(), Util.getHelpEmail(), strSubject, strBody);
    }

    private void emailGenericInstructionsToModerator(String strInstructions) throws Exception {
        String strJoinURL = buildAnonymousJoinURL();
        String strFullName = this.piOwner.getName();
        String strSubject = this.formatWebMessage("invitee.email.personal.subject", new Object[] { Util.getProductName(), strFullName, new Date() });
        String strBody = this.formatWebMessage("invitee.email.anonymous.body", new Object[] { strFullName, Util.getProductName(), strJoinURL, strInstructions });
        logger.debug("sending email:\n" + strBody);
        MailUtil.sendEmailAsync(this.piOwner.getEmail(), Util.getHelpEmail(), strSubject, strBody);
    }

    private String buildAnonymousJoinURL() {
        return Util.buildAnonymousJoinURL(this.getMeetingData().getMeetingId() + "", this.getMeetingData().getMeetingKey());
    }

    private String buildInviteeJoinURL(String strID) {
        return Util.buildInviteeJoinURL(strID, this.getMeetingData().getMeetingKey());
    }

    private boolean handleExceptionMessage(ExceptionMessage em) {
        logger.info(em.getException());
        logger.info("### Above Exception received from client:" + "\n\t" + em.getSender().getName() + " " + em.getSender().getLabel());
        return true;
    }

    public synchronized Properties getProperties() {
        if (null == this.properties) {
            this.properties = System.getProperties();
        }
        return this.properties;
    }

    public void persistCustomerProperty(Class serverClass, String name, String value) {
        try {
            PluginSettingsManagerUtil.getLocalHome().create().setPluginCustomerSetting(new Integer(this.strCustomerID), serverClass.getName(), name, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String retrieveCustomerProperty(Class serverClass, String name) {
        String strRet = null;
        try {
            strRet = PluginSettingsManagerUtil.getLocalHome().create().getPluginCustomerSetting(new Integer(this.strCustomerID), serverClass.getName(), name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strRet;
    }

    public void persistMeetingProperty(Class serverClass, String name, String value) {
        try {
            PluginSettingsManagerUtil.getLocalHome().create().setPluginMeetingSetting(new Integer(this.strConfID), serverClass.getName(), name, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String retrieveMeetingProperty(Class serverClass, String name) {
        String strRet = null;
        try {
            strRet = PluginSettingsManagerUtil.getLocalHome().create().getPluginMeetingSetting(new Integer(this.strConfID), serverClass.getName(), name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strRet;
    }

    public String getCustomerFirstName() {
        return piOwner.getName();
    }

    public String getCustomerLastName() {
        return piOwner.getName();
    }

    public ParticipantInfo getMeetingOwner() {
        return this.piOwner;
    }

    public boolean isMeetingRecorded() {
        return confData.getRecorded();
    }

    public void schedulePluginTask(AbstractScheduledPluginTask task) throws Exception {
        PluginScheduledTaskInfoUtil.getLocalHome().create(task.getClass().getName(), task.getScheduledRun(), task.getBaseDir(), MeetingUtil.getLocalHome().findByPrimaryKey(new Integer(this.strConfID)));
    }

    public static final int PING_PERIOD = 10 * 1000;

    public static final int ROSTER_PERIOD = 4 * 1000;

    public static final int REAPER_WAIT = 1 * PING_PERIOD;

    private boolean bScriptRecorded;

    private boolean bRosterChanged;

    private List listTimers = new Vector();

    protected boolean bCapture;

    private boolean bFirstJoinSent;

    private Hashtable hashServerIDToPartInfo;

    boolean bRecordingNow;

    Date dateRecordingLastStarted;

    long lElapsedTimeMillis;

    private ParticipantInfo piModerator;

    private ParticipantInfo piOwner;

    private boolean bReaperStarted;

    private Hashtable hashServers = new Hashtable();

    private Date dateLastBroadcast;

    private Timer timerReaper;

    private File fileRecordingDir;

    private ObjectOutputStream oosRecording;

    private ZipOutputStream zosArchive;

    private ZipOutputStream zosStream;

    private static Logger logger = Logger.getLogger(LiveConference.class);

    private Properties properties;

    private boolean bLocked;

    private String password;
}
