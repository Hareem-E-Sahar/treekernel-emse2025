package com.android.crepe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.Vector;
import java.io.ByteArrayInputStream;
import android.app.ActivityManagerNative;
import android.Manifest;
import android.content.Context;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.Binder;
import android.os.RemoteException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.crepe.*;
import android.os.crepe.ICrepePolicyManagerService;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class CrepePolicyManagerService extends ICrepePolicyManagerService.Stub implements IContextEventListener {

    private final String TAG = "CrepePolicyManagerService";

    private static CrepePolicyManagerService mSelf;

    private CrepeDatabaseManager mDbManager;

    private Context mContext;

    private Handler mHandler;

    private CrepeWorkerThread mWorker;

    private ContextDetectorSystem mDetector;

    private BootReceiver mOnBoot;

    private CrepeReaper mReaper;

    private CrepePolicyReceiver mPolicyReceiver;

    private final int CREPE_PERMISSION_ALLOWED = 1;

    private final int CREPE_PERMISSION_DENIED = 0;

    private ActiveRulesManager<MetaCell> mActiveRulesManager;

    private IPTables mIpTables;

    private MasterFormatParser mfParser;

    private CrepePolicyParserConnector mParserConnection;

    private UEx mUEx;

    private CrepeCertificateManager mCCM;

    private static final String PERM_CREPE_DATABASE = "android.permission.CREPE_DATABASE";

    private static final String PERM_CREPE_CONTEXT_CONTROL = "android.permission.CREPE_CONTEXT_CONTROL";

    private static final String PERM_CREPE_GENERAL = "android.permission.CREPE_GENERAL";

    private static final String PERM_CREPE_PROCESS_POLICY = "android.permission.CREPE_PROCESS_POLICY";

    private static String[] SYSTEM_SERVICES = new String[] { Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.CHANGE_CONFIGURATION, Manifest.permission.INSTALL_PACKAGES, Manifest.permission.DELETE_PACKAGES, Manifest.permission.INTERNET, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_OWNER_DATA, Manifest.permission.WRITE_OWNER_DATA, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.CAMERA, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS, Manifest.permission.ACCESS_MOCK_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.BATTERY_STATS, Manifest.permission.BIND_INPUT_METHOD, Manifest.permission.BIND_WALLPAPER, Manifest.permission.BROADCAST_PACKAGE_REMOVED, Manifest.permission.BROADCAST_SMS, Manifest.permission.BROADCAST_STICKY, Manifest.permission.BROADCAST_WAP_PUSH, Manifest.permission.CALL_PHONE, Manifest.permission.CALL_PRIVILEGED, Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.CONTROL_LOCATION_UPDATES, Manifest.permission.DEVICE_POWER, Manifest.permission.FLASHLIGHT, Manifest.permission.FORCE_BACK, Manifest.permission.INSTALL_LOCATION_PROVIDER, Manifest.permission.KILL_BACKGROUND_PROCESSES, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.MODIFY_PHONE_STATE, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.READ_HISTORY_BOOKMARKS, Manifest.permission.REBOOT, Manifest.permission.RECEIVE_WAP_PUSH, Manifest.permission.RECORD_AUDIO, Manifest.permission.REORDER_TASKS, Manifest.permission.RESTART_PACKAGES, Manifest.permission.SET_TIME, Manifest.permission.SET_TIME_ZONE, Manifest.permission.SET_WALLPAPER, Manifest.permission.VIBRATE, Manifest.permission.WAKE_LOCK, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_SETTINGS, Manifest.permission.ACCESS_SURFACE_FLINGER, Manifest.permission.EXPAND_STATUS_BAR, Manifest.permission.GET_PACKAGE_SIZE, Manifest.permission.READ_LOGS, Manifest.permission.GET_TASKS, Manifest.permission.RECEIVE_BOOT_COMPLETED, Manifest.permission.SET_ORIENTATION };

    private CrepePolicyManagerService(Context ctx) {
        mContext = ctx;
        createDirs();
        mDbManager = new CrepeDatabaseManager(ctx);
        mDbManager.openDatabase();
        mActiveRulesManager = new ActiveRulesManager<MetaCell>();
        mActiveRulesManager.initWithPermanentReources(SYSTEM_SERVICES);
        mIpTables = new IPTables(ctx);
        mWorker = new CrepeWorkerThread("CrepeWorkerThread");
        mWorker.start();
        mReaper = new CrepeReaper(CrepeReaper.MERCIFUL, mContext);
        mfParser = new MasterFormatParser();
        mParserConnection = new CrepePolicyParserConnector(ctx);
        CrepeAuthenticator.initialize();
        mUEx = new UEx();
        mCCM = CrepeCertificateManager.getInstance();
    }

    public void test() {
        mDbManager.test();
    }

    private void createDirs() {
        Process p;
        try {
            p = Runtime.getRuntime().exec("mkdir /data/crepe");
            p.waitFor();
            p = Runtime.getRuntime().exec("chown system:system /data/crepe");
            p.waitFor();
            p = Runtime.getRuntime().exec("chmod 0770 /data/crepe");
            p.waitFor();
            p = Runtime.getRuntime().exec("mkdir /data/crepe/certificates");
            p.waitFor();
            p = Runtime.getRuntime().exec("chown system:system /data/crepe/certificates");
            p.waitFor();
            p = Runtime.getRuntime().exec("chmod 0770 /data/crepe/certificates");
            p.waitFor();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static CrepePolicyManagerService getInstance(Context ctx) {
        if (ctx == null && mSelf != null) return mSelf;
        if (mSelf == null) mSelf = new CrepePolicyManagerService(ctx);
        return mSelf;
    }

    int internalCheckPerm(String permission) {
        int callerPid = Binder.getCallingPid();
        int callerUid = Binder.getCallingUid();
        int res = -1;
        try {
            long oldId = Binder.clearCallingIdentity();
            res = ActivityManagerNative.getDefault().directCallComponentPermission(permission, callerPid, callerUid);
            Binder.restoreCallingIdentity(oldId);
        } catch (RemoteException re) {
            Log.e(TAG, "RemoteException: internalCheckPerm");
        }
        return res;
    }

    public int addPolicy(CrepePolicy cp) {
        int allowed = -1;
        allowed = internalCheckPerm(PERM_CREPE_DATABASE);
        if (allowed == PackageManager.PERMISSION_GRANTED) return addPolicyLocked(cp); else {
            Log.e(TAG, "FATAL: Security Violation: You do not have the required permission for this operation");
            return -1;
        }
    }

    int addPolicyLocked(CrepePolicy cp) {
        return mDbManager.addPolicy(cp);
    }

    public CrepePolicy getPolicy(String policyId) {
        return mDbManager.getPolicy(policyId);
    }

    public ArrayList<CrepeRule> getRulesForPolicy(String policyId) {
        return mDbManager.getRulesForPolicy(policyId);
    }

    public void addRule(CrepeRule cr) {
        int allowed = -1;
        allowed = internalCheckPerm(PERM_CREPE_DATABASE);
        if (allowed == PackageManager.PERMISSION_GRANTED) addRuleLocked(cr); else {
            Log.e(TAG, "FATAL: Security Violation, you do not have the required permission to perform this operation");
        }
    }

    void addRuleLocked(CrepeRule cr) {
        if (!cr.Subject.equals("*")) {
            try {
                ApplicationInfo ai = mContext.getPackageManager().getApplicationInfo(cr.Subject, 0);
                cr.Subject = Integer.toString(ai.uid);
            } catch (NameNotFoundException nnfe) {
                Log.e(TAG, "Name not found for application: " + cr.Subject);
                return;
            }
        }
        mDbManager.addRule(cr);
    }

    public void removePolicy(String policyId) {
        int allowed = -1;
        allowed = internalCheckPerm(PERM_CREPE_DATABASE);
        if (allowed == PackageManager.PERMISSION_GRANTED) removePolicyLocked(policyId); else {
            Log.e(TAG, "FATAL: Security Violation, you do not have the required permission to perform this operation");
        }
    }

    void removePolicyLocked(String policyId) {
        mDbManager.removePolicy(policyId);
    }

    public void close() {
        Log.i(TAG, "Shutting down CRePE...");
        mContext.unregisterReceiver(mOnBoot);
        mDbManager.close();
        mDetector.finalize();
        mPolicyReceiver.unregisterAll();
        mParserConnection.disconnect();
        mCCM.close();
    }

    private void closeLooper() {
        Message msg = Message.obtain();
        msg.what = CrepeWorkerHandler.MESSAGE_QUIT;
        mHandler.sendMessage(msg);
    }

    public synchronized boolean checkCrepePermission(String subject, String object) {
        MetaCell first = transformCellForCRCP(mActiveRulesManager.getCellData(subject, object));
        MetaCell starSub = transformCellForCRCP(mActiveRulesManager.getCellData("*", object));
        MetaCell starObj = transformCellForCRCP(mActiveRulesManager.getCellData(subject, "*"));
        MetaCell inter = conflictResolveCP(first, starSub);
        MetaCell fin = conflictResolveCP(inter, starObj);
        if (fin.Access == 1) return true; else return false;
    }

    private MetaCell transformCellForCRCP(MetaCell mc) {
        if (mc == null) return new MetaCell(CREPE_PERMISSION_ALLOWED, -1); else return mc;
    }

    private MetaCell conflictResolveCP(MetaCell r1, MetaCell r2) {
        if (r1.Priority > r2.Priority) return r1; else if (r2.Priority > r1.Priority) return r2; else if (r1.Access == CREPE_PERMISSION_DENIED || r2.Access == CREPE_PERMISSION_DENIED) return new MetaCell(CREPE_PERMISSION_DENIED, r1.Priority); else return new MetaCell(CREPE_PERMISSION_ALLOWED, r1.Priority);
    }

    public synchronized void activatePolicy(String policyId) {
        int allowed = -1;
        allowed = internalCheckPerm(PERM_CREPE_GENERAL);
        if (allowed == PackageManager.PERMISSION_GRANTED) activatePolicyLocked(policyId); else {
            Log.e(TAG, "FATAL: Security Violation, you do not have the required permission for this operation");
        }
    }

    synchronized void activatePolicyLocked(String policyId) {
        Message msg = Message.obtain();
        msg.what = CrepeWorkerHandler.MESSAGE_ACTIVATE;
        msg.obj = policyId;
        mHandler.sendMessage(msg);
    }

    public synchronized void deactivatePolicy(String policyId) {
        int allowed = -1;
        allowed = internalCheckPerm(PERM_CREPE_GENERAL);
        if (allowed == PackageManager.PERMISSION_GRANTED) deactivatePolicyLocked(policyId); else {
            Log.e(TAG, "FATAL: Security Violation, you do not have the required permission to perform this operation");
        }
    }

    synchronized void deactivatePolicyLocked(String policyId) {
        Message msg = Message.obtain();
        msg.what = CrepeWorkerHandler.MESSAGE_DEACTIVATE;
        msg.obj = policyId;
        mHandler.sendMessage(msg);
    }

    private void UCRGroup(ArrayList<String> activePolicies) {
        MetaCell theFinalCell = null;
        if (activePolicies == null) return;
        for (String policyId : activePolicies) {
            ArrayList<String> subjects = mDbManager.getXFromRulesForPolicy("Subject", policyId);
            ArrayList<String> objects = mDbManager.getXFromRulesForPolicy("Object", policyId);
            for (String aSubject : subjects) {
                if (!mActiveRulesManager.exists(aSubject)) mActiveRulesManager.insertApplication(aSubject);
            }
            for (String anObject : objects) {
                if (!mActiveRulesManager.exists(anObject)) mActiveRulesManager.insertApplication(anObject);
            }
        }
        Vector<String> cols = mActiveRulesManager.getColumnHeader();
        Set<String> rows = mActiveRulesManager.getRowHeader();
        for (String subject : rows) {
            for (String object : cols) {
                ArrayList<CrepeRule> ruleSet = mDbManager.getAllActiveRulesForSubjectObject(subject, object, activePolicies);
                if (ruleSet == null) continue;
                if (ruleSet.size() == 1) {
                    theFinalCell = new MetaCell(ruleSet.get(0).Access, ruleSet.get(0).Priority);
                } else if (ruleSet.size() > 1) {
                    theFinalCell = conflictResolveGroup(ruleSet);
                }
                mActiveRulesManager.setCellData(subject, object, theFinalCell);
                if (!object.contains("android.permission") && theFinalCell.Access == CREPE_PERMISSION_DENIED) mReaper.reap(object);
                if (object.equals(SYSTEM_SERVICES[7])) processIpTablesRules(subject, theFinalCell.Access);
            }
        }
    }

    private MetaCell conflictResolveGroup(ArrayList<CrepeRule> ruleSet) {
        Collections.sort(ruleSet, new CrepeRuleComparator());
        int limit = getSamePrioritySubset(ruleSet);
        CrepeRule first = ruleSet.get(0);
        if (containsDeny(limit, ruleSet)) return new MetaCell(CREPE_PERMISSION_DENIED, first.Priority); else return new MetaCell(first.Access, first.Priority);
    }

    private boolean containsDeny(int limit, ArrayList<CrepeRule> ruleSet) {
        boolean deny = false;
        for (int i = 0; i < limit; i++) {
            if (ruleSet.get(i).Access == CREPE_PERMISSION_DENIED) {
                deny = true;
                break;
            }
        }
        return deny;
    }

    private int getSamePrioritySubset(ArrayList<CrepeRule> ruleSet) {
        int highestPriority = ruleSet.get(0).Priority;
        int i = 1;
        while (i < ruleSet.size() && highestPriority == ruleSet.get(i).Priority) {
            i++;
        }
        return i;
    }

    class CrepeRuleComparator implements Comparator<CrepeRule> {

        @Override
        public int compare(CrepeRule r1, CrepeRule r2) {
            if (r1.Priority < r2.Priority) return 1; else if (r1.Priority > r2.Priority) return -1; else return 0;
        }
    }

    private void UCRSingle(ArrayList<CrepeRule> rules) {
        MetaCell theFinalCell = null;
        for (CrepeRule r : rules) {
            if (!mActiveRulesManager.exists(r.Object)) mActiveRulesManager.insertApplication(r.Object);
            if (!mActiveRulesManager.exists(r.Subject)) mActiveRulesManager.insertApplication(r.Subject);
        }
        for (CrepeRule r : rules) {
            MetaCell mc = mActiveRulesManager.getCellData(r.Subject, r.Object);
            if (mc == null) {
                theFinalCell = new MetaCell(r.Access, r.Priority);
            } else {
                theFinalCell = conflictResolveSingle(r, mc);
            }
            mActiveRulesManager.setCellData(r.Subject, r.Object, theFinalCell);
            if (r.Object.equals(SYSTEM_SERVICES[7])) processIpTablesRules(r.Subject, theFinalCell.Access);
        }
    }

    private MetaCell conflictResolveSingle(CrepeRule r, MetaCell oldMC) {
        int current_priority = oldMC.Priority;
        int new_priority = r.Priority;
        if (new_priority > current_priority) {
            return new MetaCell(r.Access, r.Priority);
        } else if (new_priority < current_priority) {
            return oldMC;
        } else {
            return new MetaCell(CREPE_PERMISSION_DENIED, r.Priority);
        }
    }

    private void processIpTablesRules(String subject, int access) {
        switch(access) {
            case CREPE_PERMISSION_DENIED:
                if (subject.equals("*")) {
                    mIpTables.disableNetworkAccess();
                } else {
                    mIpTables.insertDisableRule(subject);
                }
                break;
            case CREPE_PERMISSION_ALLOWED:
                if (subject.equals("*")) {
                    mIpTables.enableNetworkAccess();
                } else {
                    mIpTables.removeDisableRule(subject);
                }
        }
    }

    private class CrepeWorkerThread extends Thread {

        public CrepeWorkerThread(String name) {
            super(name);
        }

        public void run() {
            Looper.prepare();
            mHandler = new CrepeWorkerHandler();
            Looper.loop();
        }
    }

    private class CrepeWorkerHandler extends Handler {

        public static final int MESSAGE_ACTIVATE = 0;

        public static final int MESSAGE_DEACTIVATE = 1;

        public static final int MESSAGE_BOOT_ACTIVATION = 2;

        public static final int MESSAGE_PROCESS_POLICY = 3;

        public static final int MESSAGE_QUIT = 4;

        @Override
        public void handleMessage(Message msg) {
            try {
                switch(msg.what) {
                    case MESSAGE_ACTIVATE:
                        if (!mDbManager.isPolicyEnabled((String) msg.obj)) {
                            Log.i(TAG, "only enabled policies can be activated");
                            break;
                        }
                        ArrayList<CrepeRule> rules = mDbManager.getRulesForPolicy((String) msg.obj);
                        UCRSingle(rules);
                        mDbManager.addActivePolicy((String) msg.obj);
                        for (CrepeRule r : rules) {
                            if (!r.Object.contains("android.permission") && r.Access == CREPE_PERMISSION_DENIED) mReaper.reap(r.Object);
                        }
                        break;
                    case MESSAGE_DEACTIVATE:
                        mActiveRulesManager.reinit();
                        mActiveRulesManager.initWithPermanentReources(SYSTEM_SERVICES);
                        mDbManager.removeActivePolicy((String) msg.obj);
                        mIpTables.flush();
                        UCRGroup(mDbManager.getActivePolicies());
                        break;
                    case MESSAGE_BOOT_ACTIVATION:
                        ArrayList<String> active = mDbManager.getActivePolicies();
                        if (active != null) {
                            for (String id : active) mDetector.installContextExpression(mDbManager.getPolicy(id).ContextCondition, id);
                        }
                        break;
                    case MESSAGE_PROCESS_POLICY:
                        Log.i(TAG, "parsing and verification started...");
                        CrepePolicyHolder cph = null;
                        ByteArrayInputStream bytes = new ByteArrayInputStream(((String) msg.obj).getBytes());
                        CrepePacket cpkt = mfParser.getPacket(bytes);
                        if (mfParser.everythingOK == false) {
                            break;
                        }
                        if (cpkt.type != CrepePacket.TYPE_CMD) cph = mParserConnection.parse(cpkt.policy);
                        if (CrepeAuthenticator.verifyCertificate(cpkt.certificate)) {
                            if (CrepeAuthenticator.verifyPacketSignature(cpkt, "SHA1withRSA", "BC")) {
                                switch(cpkt.type) {
                                    case CrepePacket.TYPE_CMD:
                                        interpretCommand(cpkt.command);
                                        break;
                                    case CrepePacket.TYPE_CMDPOL:
                                        {
                                            CrepePolicy cp = new CrepePolicy(cph.id, cph.id, cpkt.context, cph.enabled);
                                            if (addPolicyLocked(cp) != -1) {
                                                for (CrepeRule aRule : cph.rules) addRuleLocked(aRule);
                                                interpretCommand(cpkt.command);
                                            }
                                            break;
                                        }
                                    case CrepePacket.TYPE_CTXPOL:
                                        {
                                            CrepePolicy cp = new CrepePolicy(cph.id, cph.id, cpkt.context, cph.enabled);
                                            if (addPolicyLocked(cp) != -1) {
                                                for (CrepeRule aRule : cph.rules) addRuleLocked(aRule);
                                                installContextLocked(cp.ContextCondition, cp.PolicyId);
                                                Log.i(TAG, "Policy with context installed!");
                                            }
                                            break;
                                        }
                                    case CrepePacket.TYPE_POL:
                                        {
                                            CrepePolicy cp = new CrepePolicy(cph.id, cph.id, cpkt.context, cph.enabled);
                                            if (addPolicyLocked(cp) != -1) {
                                                for (CrepeRule aRule : cph.rules) addRuleLocked(aRule);
                                                Log.i(TAG, "Policy installed!");
                                            }
                                            break;
                                        }
                                }
                            } else Log.e(TAG, "Invalid Signature");
                        } else Log.e(TAG, "Invalid Certificate");
                        break;
                    case MESSAGE_QUIT:
                        Looper.myLooper().quit();
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }

    String getPolicy() {
        StringBuffer content = new StringBuffer();
        try {
            FileInputStream fin = new FileInputStream("/system/certs/policysigned1.txt");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int ch;
            while ((ch = fin.read()) != -1) {
                baos.write(ch);
            }
            fin.close();
            content.append(new String(baos.toByteArray()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("---EARL---", "length: " + content.length());
        return content.toString();
    }

    public void detectorInit() {
        mDetector = new ContextDetectorSystem();
        mDetector.initialize(mContext, this);
        mOnBoot = new BootReceiver();
    }

    public void policyReceiverInit() {
        mPolicyReceiver = new CrepePolicyReceiver(mContext);
        if (!SystemProperties.get("ro.kernel.qemu").equals("1")) {
            mPolicyReceiver.registerChannel(CrepePolicyReceiver.BLUETOOTH_CHANNEL);
            mPolicyReceiver.initializeInputChannel(CrepePolicyReceiver.BLUETOOTH_CHANNEL);
        }
        mPolicyReceiver.registerChannel(CrepePolicyReceiver.SMS_CHANNEL);
        mPolicyReceiver.registerChannel(CrepePolicyReceiver.MMS_CHANNEL);
        mParserConnection.setTargetIntent("com.android.crepe.ponder.PonderPolicyParser");
        mParserConnection.connect();
    }

    public void installContext(String expression, String policyId) {
        int allowed = -1;
        allowed = internalCheckPerm(PERM_CREPE_CONTEXT_CONTROL);
        if (allowed == PackageManager.PERMISSION_GRANTED) installContextLocked(expression, policyId); else {
            Log.e(TAG, "FATAL: Security Violation, you do not have the required permission to perform this operation");
        }
    }

    private void installContextLocked(String expression, String policyId) {
        mDetector.installContextExpression(expression, policyId);
    }

    public void uninstallContext(String policyId) {
        int allowed = -1;
        allowed = internalCheckPerm(PERM_CREPE_CONTEXT_CONTROL);
        if (allowed == PackageManager.PERMISSION_GRANTED) uninstallContextLocked(policyId); else {
            Log.e(TAG, "FATAL: Security Violation, you do not have the required permission to perform this operation");
        }
    }

    private void uninstallContextLocked(String policyId) {
        mDetector.uninstallContextExpression(policyId);
    }

    @Override
    public void onTrue(String policyId) {
        activatePolicy(policyId);
        Log.i(TAG, "Policy " + policyId + " activated");
    }

    @Override
    public void onFalse(String policyId) {
        deactivatePolicy(policyId);
        Log.i(TAG, "Policy " + policyId + " deactivated");
    }

    private class BootReceiver extends BroadcastReceiver {

        BootReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            mContext.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Message msg = Message.obtain();
            msg.what = CrepeWorkerHandler.MESSAGE_BOOT_ACTIVATION;
            mHandler.sendMessage(msg);
        }
    }

    public void processPolicy(String policy) {
        int allowed = -1;
        allowed = internalCheckPerm(PERM_CREPE_PROCESS_POLICY);
        if (allowed == PackageManager.PERMISSION_GRANTED) processPolicyLocked(policy); else {
            Log.e(TAG, "FATAL: Security Violation, you do not have the required permission to perform this operation");
        }
    }

    void processPolicyLocked(String policy) {
        Message msg = Message.obtain();
        msg.what = CrepeWorkerHandler.MESSAGE_PROCESS_POLICY;
        msg.obj = policy;
        mHandler.sendMessage(msg);
    }

    public boolean checkCertificateCache(String certName) {
        return mCCM.checkCache(certName);
    }

    private void interpretCommand(String command) {
        final String ACT = "ACTIVATE";
        final String DEACT = "DEACTIVATE";
        final String DEL = "DEL";
        final String FLIGHT_ENTER = "FLIGHT_ENTER";
        final String FLIGHT_EXIT = "FLIGHT_EXIT";
        final String STAR = "*";
        String components[] = command.trim().split(" ");
        if (components[0].equals(ACT)) {
            onTrue(components[1]);
        } else if (components[0].equals(DEACT)) {
            onFalse(components[1]);
        } else if (components[0].equals(DEL)) {
            if (components[1].equals(STAR)) {
                purge();
                Log.i(TAG, "CRePE System Purged");
            } else {
                mDbManager.removePolicy(components[1]);
                deactivatePolicyLocked(components[1]);
                Log.i(TAG, "Policy " + components[1] + " deactivated");
                uninstallContextLocked(components[1]);
                Log.i(TAG, "CommandInterpreter: policy removed: " + components[1]);
            }
        } else if (components[0].equals(FLIGHT_ENTER)) {
            setAirplaneMode(1);
            mUEx.speak("Command " + components[1] + " has been activated", mContext);
            Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.CREPE_AIRPLANE_MODE, 1);
            Log.i(TAG, "CommandInterpreter: entering airplane mode...");
        } else if (components[0].equals(FLIGHT_EXIT)) {
            setAirplaneMode(0);
            mUEx.speak("Command " + components[1] + " has been deactivated", mContext);
            Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.CREPE_AIRPLANE_MODE, 0);
            Log.i(TAG, "CommandInterpreter: exiting airplane mode...");
        } else Log.i(TAG, "Warning: command not recognized");
    }

    private void setAirplaneMode(int enabled) {
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, enabled);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        if (enabled == 1) intent.putExtra("state", true); else if (enabled == 0) intent.putExtra("state", false);
        mContext.sendBroadcast(intent);
    }

    private void purge() {
        mDbManager.reset();
        mActiveRulesManager.reinit();
        mActiveRulesManager.initWithPermanentReources(SYSTEM_SERVICES);
        mDetector.uninstallAll();
    }
}
