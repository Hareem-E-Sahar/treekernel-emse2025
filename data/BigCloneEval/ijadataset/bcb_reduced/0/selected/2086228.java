package cmupdaterapp.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.*;
import android.widget.Toast;
import cmupdaterapp.customTypes.FullUpdateInfo;
import cmupdaterapp.customTypes.ThemeInfo;
import cmupdaterapp.customTypes.ThemeList;
import cmupdaterapp.customTypes.UpdateInfo;
import cmupdaterapp.customization.Customization;
import cmupdaterapp.interfaces.IUpdateCheckService;
import cmupdaterapp.interfaces.IUpdateCheckServiceCallback;
import cmupdaterapp.misc.Constants;
import cmupdaterapp.misc.Log;
import cmupdaterapp.misc.State;
import cmupdaterapp.ui.MainActivity;
import cmupdaterapp.ui.R;
import cmupdaterapp.utils.Preferences;
import cmupdaterapp.utils.StringUtils;
import cmupdaterapp.utils.SysUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedList;

public class UpdateCheckService extends Service {

    private static final String TAG = "UpdateCheckService";

    private static Boolean showDebugOutput = false;

    private final RemoteCallbackList<IUpdateCheckServiceCallback> mCallbacks = new RemoteCallbackList<IUpdateCheckServiceCallback>();

    private Preferences mPreferences;

    private String systemMod;

    private String systemRom;

    private ThemeInfo themeInfos;

    private boolean showExperimentalRomUpdates;

    private boolean showAllRomUpdates;

    private boolean showExperimentalThemeUpdates;

    private boolean showAllThemeUpdates;

    private boolean WildcardUsed = false;

    private int PrimaryKeyTheme = -1;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mPreferences = new Preferences(this);
        showDebugOutput = mPreferences.displayDebugOutput();
        systemMod = mPreferences.getBoardString();
        if (systemMod == null) {
            if (showDebugOutput) Log.d(TAG, "Unable to determine System's Mod version. Updater will show all available updates");
        } else {
            if (showDebugOutput) Log.d(TAG, "System's Mod version:" + systemMod);
        }
    }

    @Override
    public void onDestroy() {
        mCallbacks.kill();
        super.onDestroy();
    }

    private final IUpdateCheckService.Stub mBinder = new IUpdateCheckService.Stub() {

        public void registerCallback(IUpdateCheckServiceCallback cb) throws RemoteException {
            if (cb != null) mCallbacks.register(cb);
        }

        public void unregisterCallback(IUpdateCheckServiceCallback cb) throws RemoteException {
            if (cb != null) mCallbacks.unregister(cb);
        }

        public void checkForUpdates() throws RemoteException {
            checkForNewUpdates();
        }
    };

    private void DisplayExceptionToast(String ex) {
        ToastHandler.sendMessage(ToastHandler.obtainMessage(0, ex));
    }

    private void checkForNewUpdates() {
        FullUpdateInfo availableUpdates;
        while (true) {
            try {
                if (showDebugOutput) Log.d(TAG, "Checking for updates...");
                availableUpdates = getAvailableUpdates();
                break;
            } catch (IOException ex) {
                Log.e(TAG, "IOEx while checking for updates", ex);
                notificateCheckError(ex.getMessage());
                return;
            } catch (RuntimeException ex) {
                Log.e(TAG, "RuntimeEx while checking for updates", ex);
                notificateCheckError(ex.getMessage());
                return;
            }
        }
        mPreferences.setLastUpdateCheck(new Date());
        int updateCountRoms = availableUpdates.getRomCount();
        int updateCountIncrementalRoms = availableUpdates.getIncrementalRomCount();
        int updateCountThemes = availableUpdates.getThemeCount();
        int updateCount = availableUpdates.getUpdateCount();
        if (showDebugOutput) Log.d(TAG, updateCountRoms + " ROM update(s) found; " + updateCountIncrementalRoms + " incremental ROM udpate(s) found; " + updateCountThemes + " Theme update(s) found");
        if (updateCountRoms == 0 && updateCountThemes == 0 && updateCountIncrementalRoms == 0) {
            if (showDebugOutput) Log.d(TAG, "No updates found");
            ToastHandler.sendMessage(ToastHandler.obtainMessage(0, R.string.no_updates_found, 0));
            FinishUpdateCheck();
        } else {
            if (mPreferences.notificationsEnabled()) {
                Intent i = new Intent(this, MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);
                Resources res = getResources();
                Notification notification = new Notification(R.drawable.icon_notification, res.getString(R.string.not_new_updates_found_ticker), System.currentTimeMillis());
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                String text = MessageFormat.format(res.getString(R.string.not_new_updates_found_body), updateCount);
                notification.setLatestEventInfo(this, res.getString(R.string.not_new_updates_found_title), text, contentIntent);
                Uri notificationRingtone = mPreferences.getConfiguredRingtone();
                if (mPreferences.getVibrate()) notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS; else notification.defaults = Notification.DEFAULT_LIGHTS;
                notification.sound = notificationRingtone;
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(R.string.not_new_updates_found_title, notification);
            }
            FinishUpdateCheck();
        }
    }

    private void notificateCheckError(String ExceptionText) {
        DisplayExceptionToast(ExceptionText);
        if (showDebugOutput) Log.d(TAG, "Update check error");
        FinishUpdateCheck();
    }

    private FullUpdateInfo getAvailableUpdates() throws IOException {
        FullUpdateInfo retValue = new FullUpdateInfo();
        boolean romException = false;
        HttpClient romHttpClient = new DefaultHttpClient();
        HttpClient themeHttpClient = new DefaultHttpClient();
        HttpEntity romResponseEntity = null;
        HttpEntity themeResponseEntity = null;
        systemRom = SysUtils.getModVersion();
        if (Customization.Screenshotsupport) themeInfos = mPreferences.getThemeInformations();
        showExperimentalRomUpdates = mPreferences.showExperimentalRomUpdates();
        showAllRomUpdates = mPreferences.showAllRomUpdates();
        boolean ThemeUpdateUrlSet = false;
        if (Customization.Screenshotsupport) {
            showExperimentalThemeUpdates = mPreferences.showExperimentalThemeUpdates();
            showAllThemeUpdates = mPreferences.showAllThemeUpdates();
            ThemeUpdateUrlSet = mPreferences.ThemeUpdateUrlSet();
            if (themeInfos == null || themeInfos.name.equalsIgnoreCase(Constants.UPDATE_INFO_WILDCARD)) {
                if (showDebugOutput) Log.d(TAG, "Wildcard is used for Theme Updates");
                themeInfos = new ThemeInfo();
                WildcardUsed = true;
            }
        }
        try {
            URI RomUpdateServerUri = URI.create(mPreferences.getRomUpdateFileURL());
            HttpUriRequest romReq = new HttpGet(RomUpdateServerUri);
            romReq.addHeader("Cache-Control", "no-cache");
            HttpResponse romResponse = romHttpClient.execute(romReq);
            int romServerResponse = romResponse.getStatusLine().getStatusCode();
            if (romServerResponse != HttpStatus.SC_OK) {
                if (showDebugOutput) Log.d(TAG, "Server returned status code for ROM " + romServerResponse);
                romException = true;
            }
            if (!romException) romResponseEntity = romResponse.getEntity();
        } catch (IllegalArgumentException e) {
            if (showDebugOutput) Log.d(TAG, "Rom Update URI wrong: " + mPreferences.getRomUpdateFileURL());
            romException = true;
        }
        if (Customization.Screenshotsupport && ThemeUpdateUrlSet) {
            try {
                LinkedList<ThemeList> tl = mPreferences.getThemeUpdateUrls();
                for (ThemeList t : tl) {
                    if (!t.enabled) {
                        if (showDebugOutput) Log.d(TAG, "Theme " + t.name + " disabled. Continuing");
                        continue;
                    }
                    PrimaryKeyTheme = -1;
                    if (showDebugOutput) Log.d(TAG, "Trying to download ThemeInfos for " + t.url.toString());
                    URI ThemeUpdateServerUri = t.url;
                    HttpUriRequest themeReq = new HttpGet(ThemeUpdateServerUri);
                    themeReq.addHeader("Cache-Control", "no-cache");
                    try {
                        HttpResponse themeResponse = themeHttpClient.execute(themeReq);
                        int themeServerResponse = themeResponse.getStatusLine().getStatusCode();
                        if (themeServerResponse != HttpStatus.SC_OK) {
                            if (showDebugOutput) Log.d(TAG, "Server returned status code for Themes " + themeServerResponse);
                            themeResponseEntity = themeResponse.getEntity();
                            continue;
                        }
                        themeResponseEntity = themeResponse.getEntity();
                    } catch (IOException ex) {
                        DisplayExceptionToast(getResources().getString(R.string.theme_download_exception) + t.name + ": " + ex.getMessage());
                        Log.e(TAG, "There was an error downloading Theme " + t.name + ": ", ex);
                        continue;
                    }
                    BufferedReader themeLineReader = new BufferedReader(new InputStreamReader(themeResponseEntity.getContent()), 2 * 1024);
                    StringBuffer themeBuf = new StringBuffer();
                    String themeLine;
                    while ((themeLine = themeLineReader.readLine()) != null) {
                        themeBuf.append(themeLine);
                    }
                    themeLineReader.close();
                    if (t.PrimaryKey > 0) PrimaryKeyTheme = t.PrimaryKey;
                    LinkedList<UpdateInfo> themeUpdateInfos = parseJSON(themeBuf, RomType.Update);
                    retValue.themes.addAll(getThemeUpdates(themeUpdateInfos));
                }
            } catch (IllegalArgumentException e) {
                if (showDebugOutput) Log.d(TAG, "Theme Update URI wrong");
            }
        }
        try {
            if (!romException) {
                PrimaryKeyTheme = -1;
                BufferedReader romLineReader = new BufferedReader(new InputStreamReader(romResponseEntity.getContent()), 2 * 1024);
                StringBuffer romBuf = new StringBuffer();
                String romLine;
                while ((romLine = romLineReader.readLine()) != null) {
                    romBuf.append(romLine);
                }
                romLineReader.close();
                LinkedList<UpdateInfo> romUpdateInfos = parseJSON(romBuf, RomType.Update);
                retValue.roms = getRomUpdates(romUpdateInfos);
                LinkedList<UpdateInfo> incrementalRomUpdateInfos = parseJSON(romBuf, RomType.IncrementalUpdate);
                retValue.incrementalRoms = getIncrementalRomUpdates(incrementalRomUpdateInfos);
            } else if (showDebugOutput) Log.d(TAG, "There was an Exception on Downloading the Rom JSON File");
        } finally {
            if (romResponseEntity != null) romResponseEntity.consumeContent();
            if (themeResponseEntity != null) themeResponseEntity.consumeContent();
        }
        FullUpdateInfo ful = FilterUpdates(retValue, State.loadState(this, showDebugOutput));
        if (!romException) State.saveState(this, retValue, showDebugOutput);
        return ful;
    }

    private enum RomType {

        Update, IncrementalUpdate
    }

    private LinkedList<UpdateInfo> parseJSON(StringBuffer buf, RomType type) {
        LinkedList<UpdateInfo> uis = new LinkedList<UpdateInfo>();
        JSONObject mainJSONObject;
        try {
            mainJSONObject = new JSONObject(buf.toString());
            JSONArray mirrorList = mainJSONObject.getJSONArray(Constants.JSON_MIRROR_LIST);
            if (showDebugOutput) Log.d(TAG, "Found " + mirrorList.length() + " mirrors in the JSON");
            switch(type) {
                case Update:
                    JSONArray updateList = mainJSONObject.getJSONArray(Constants.JSON_UPDATE_LIST);
                    if (showDebugOutput) Log.d(TAG, "Found " + updateList.length() + " updates in the JSON");
                    for (int i = 0, max = updateList.length(); i < max; i++) {
                        if (!updateList.isNull(i)) uis.add(parseUpdateJSONObject(updateList.getJSONObject(i), mirrorList)); else Log.e(TAG, "Theres an error in your JSON File(update part). Maybe a , after the last update");
                    }
                    break;
                case IncrementalUpdate:
                    if (mainJSONObject.has(Constants.JSON_INCREMENTAL_UPDATES)) {
                        JSONArray incrementalUpdateList = mainJSONObject.getJSONArray(Constants.JSON_INCREMENTAL_UPDATES);
                        if (showDebugOutput) Log.d(TAG, "Found " + incrementalUpdateList.length() + " incremental updates in the JSON");
                        for (int i = 0, max = incrementalUpdateList.length(); i < max; i++) {
                            if (!incrementalUpdateList.isNull(i)) uis.add(parseUpdateJSONObject(incrementalUpdateList.getJSONObject(i), mirrorList)); else Log.e(TAG, "Theres an error in your JSON File(incremental part). Maybe a , after the last update");
                        }
                    } else if (showDebugOutput) Log.d(TAG, "No Incremental Update Info in the JSON");
                    break;
                default:
                    Log.e(TAG, "Wrong RomType!");
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error in JSON File: ", e);
        }
        return uis;
    }

    private UpdateInfo parseUpdateJSONObject(JSONObject obj, JSONArray mirrorList) {
        UpdateInfo ui = new UpdateInfo();
        try {
            if (PrimaryKeyTheme > 0) ui.PrimaryKey = PrimaryKeyTheme;
            String[] Boards = obj.getString(Constants.JSON_BOARD).split("\\|");
            for (String item : Boards) {
                if (item != null) ui.board.add(item.trim());
            }
            ui.setType(obj.getString(Constants.JSON_TYPE).trim());
            String[] mods = obj.getString(Constants.JSON_MOD).split("\\|");
            for (String mod : mods) {
                if (mod != null) ui.mod.add(mod.trim());
            }
            ui.setName(obj.getString(Constants.JSON_NAME).trim());
            ui.setVersion(obj.getString(Constants.JSON_VERSION).trim());
            ui.setDescription(obj.getString(Constants.JSON_DESCRIPTION).trim());
            ui.setBranchCode(obj.getString(Constants.JSON_BRANCH).trim());
            ui.setFileName(obj.getString(Constants.JSON_FILENAME).trim());
            if (obj.has(Constants.JSON_VERSION_FOR_APPLY)) {
                ui.setVersionForApply(obj.getString(Constants.JSON_VERSION_FOR_APPLY));
            }
            for (int i = 0, max = mirrorList.length(); i < max; i++) {
                try {
                    if (!mirrorList.isNull(i)) ui.updateMirrors.add(new URI(mirrorList.getString(i).trim())); else Log.e(TAG, "Theres an error in your JSON File. Maybe a , after the last mirror");
                } catch (URISyntaxException e) {
                    Log.e(TAG, "Unable to parse mirror url (" + mirrorList.getString(i) + ui.getFileName() + "). Ignoring this mirror", e);
                }
            }
            if (obj.has(Constants.JSON_SCREENSHOTS)) {
                JSONArray screenshots = obj.getJSONArray(Constants.JSON_SCREENSHOTS);
                if (screenshots != null && screenshots.length() > 0) {
                    for (int screenshotcounter = 0; screenshotcounter < screenshots.length(); screenshotcounter++) {
                        try {
                            if (!screenshots.isNull(screenshotcounter)) ui.screenshots.add(new URI(screenshots.getString(screenshotcounter))); else Log.e(TAG, "Theres an error in your JSON File. Maybe a , after the last screenshot");
                        } catch (URISyntaxException e) {
                            Log.e(TAG, "Unable to parse Screenshot url (" + screenshots.getString(screenshotcounter) + ") Theme: " + ui.getName() + ". Ignoring this Screenshot", e);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error in JSON File: ", e);
        }
        return ui;
    }

    private boolean branchMatches(UpdateInfo ui, boolean experimentalAllowed) {
        if (ui == null) return false;
        boolean allow = false;
        if (ui.getBranchCode().equalsIgnoreCase(Constants.UPDATE_INFO_BRANCH_EXPERIMENTAL)) {
            if (experimentalAllowed) allow = true;
        } else {
            allow = true;
        }
        return allow;
    }

    private boolean boardMatches(UpdateInfo ui, String systemMod) {
        if (ui == null) return false;
        if (systemMod.equals(Constants.UPDATE_INFO_WILDCARD)) return true;
        for (String board : ui.board) {
            if (board.equalsIgnoreCase(systemMod) || board.equalsIgnoreCase(Constants.UPDATE_INFO_WILDCARD)) return true;
        }
        return false;
    }

    private boolean romMatches(UpdateInfo ui, String systemRom) {
        if (ui == null) return false;
        if (systemRom.equals(Constants.UPDATE_INFO_WILDCARD)) return true;
        for (String mod : ui.mod) {
            if (mod.equalsIgnoreCase(systemRom) || mod.equalsIgnoreCase(Constants.UPDATE_INFO_WILDCARD)) return true;
        }
        return false;
    }

    private LinkedList<UpdateInfo> getRomUpdates(LinkedList<UpdateInfo> updateInfos) {
        LinkedList<UpdateInfo> ret = new LinkedList<UpdateInfo>();
        for (int i = 0, max = updateInfos.size(); i < max; i++) {
            UpdateInfo ui = updateInfos.poll();
            if (ui.getType().equalsIgnoreCase(Constants.UPDATE_INFO_TYPE_ROM)) {
                if (boardMatches(ui, systemMod)) {
                    if (showAllRomUpdates || StringUtils.compareVersions(Customization.RO_MOD_START_STRING + ui.getVersion(), systemRom)) {
                        if (branchMatches(ui, showExperimentalRomUpdates)) {
                            if (showDebugOutput) Log.d(TAG, "Adding Rom: " + ui.getName() + " Version: " + ui.getVersion() + " Filename: " + ui.getFileName());
                            ret.add(ui);
                        } else {
                            if (showDebugOutput) Log.d(TAG, "Discarding Rom " + ui.getName() + " (Branch mismatch - stable/experimental)");
                        }
                    } else {
                        if (showDebugOutput) Log.d(TAG, "Discarding Rom " + ui.getName() + " (older version)");
                    }
                } else {
                    if (showDebugOutput) Log.d(TAG, "Discarding Rom " + ui.getName() + " (mod mismatch)");
                }
            } else {
                if (showDebugOutput) Log.d(TAG, String.format("Discarding Rom %s Version %s (not a ROM)", ui.getName(), ui.getVersion()));
            }
        }
        return ret;
    }

    private LinkedList<UpdateInfo> getIncrementalRomUpdates(LinkedList<UpdateInfo> updateInfos) {
        LinkedList<UpdateInfo> ret = new LinkedList<UpdateInfo>();
        for (int i = 0, max = updateInfos.size(); i < max; i++) {
            UpdateInfo ui = updateInfos.poll();
            if (!ui.isIncremental()) {
                if (showDebugOutput) Log.d(TAG, "Update " + ui.getName() + " is not an incremental update. Discarding it");
                continue;
            }
            if (!(Customization.RO_MOD_START_STRING + ui.getVersionForApply()).equalsIgnoreCase(systemRom)) {
                if (showDebugOutput) Log.d(TAG, String.format("Incremental Update %s discarded, because the VersionForAppy (%s)" + " doesn't match the current System Rom (%s).", ui.getName(), Customization.RO_MOD_START_STRING + ui.getVersionForApply(), systemRom));
                continue;
            }
            if (ui.getType().equalsIgnoreCase(Constants.UPDATE_INFO_TYPE_ROM)) {
                if (boardMatches(ui, systemMod)) {
                    if (branchMatches(ui, showExperimentalRomUpdates)) {
                        if (showDebugOutput) Log.d(TAG, "Adding Incremental Rom: " + ui.getName() + " Version: " + ui.getVersion() + " Filename: " + ui.getFileName());
                        ret.add(ui);
                    } else {
                        if (showDebugOutput) Log.d(TAG, "Discarding Incremental Rom " + ui.getName() + " (Branch mismatch - stable/experimental)");
                    }
                } else {
                    if (showDebugOutput) Log.d(TAG, "Discarding Incremental Rom " + ui.getName() + " (mod mismatch)");
                }
            } else {
                if (showDebugOutput) Log.d(TAG, String.format("Discarding Incremental Rom %s Version %s(not a ROM)", ui.getName(), ui.getVersion()));
            }
        }
        return ret;
    }

    private LinkedList<UpdateInfo> getThemeUpdates(LinkedList<UpdateInfo> updateInfos) {
        LinkedList<UpdateInfo> ret = new LinkedList<UpdateInfo>();
        for (int i = 0, max = updateInfos.size(); i < max; i++) {
            UpdateInfo ui = updateInfos.poll();
            if (themeInfos != null) {
                if (ui.getType().equalsIgnoreCase(Constants.UPDATE_INFO_TYPE_THEME)) {
                    if (romMatches(ui, systemRom)) {
                        if (boardMatches(ui, systemMod)) {
                            if (WildcardUsed || showAllThemeUpdates || (themeInfos.name != null && !themeInfos.name.equals("") && ui.getName().equalsIgnoreCase(themeInfos.name))) {
                                if (WildcardUsed || showAllThemeUpdates || StringUtils.compareVersions(ui.getVersion(), themeInfos.version)) {
                                    if (branchMatches(ui, showExperimentalThemeUpdates)) {
                                        if (showDebugOutput) Log.d(TAG, "Adding Theme: " + ui.getName() + " Version: " + ui.getVersion() + " Filename: " + ui.getFileName());
                                        ret.add(ui);
                                    } else {
                                        if (showDebugOutput) Log.d(TAG, String.format("Discarding Theme (branch mismatch) %s: Your Theme: %s %s; From JSON: %s %s", ui.getName(), themeInfos.name, themeInfos.version, ui.getName(), ui.getVersion()));
                                    }
                                } else {
                                    if (showDebugOutput) Log.d(TAG, String.format("Discarding Theme (Version mismatch) %s: Your Theme: %s %s; From JSON: %s %s", ui.getName(), themeInfos.name, themeInfos.version, ui.getName(), ui.getVersion()));
                                }
                            } else {
                                if (showDebugOutput) Log.d(TAG, String.format("Discarding Theme (name mismatch) %s: Your Theme: %s %s; From JSON: %s %s", ui.getName(), themeInfos.name, themeInfos.version, ui.getName(), ui.getVersion()));
                            }
                        } else {
                            if (showDebugOutput) Log.d(TAG, String.format("Discarding Theme (board mismatch) %s: Your Theme: %s %s; From JSON: %s %s", ui.getName(), themeInfos.name, themeInfos.version, ui.getName(), ui.getVersion()));
                        }
                    } else {
                        if (showDebugOutput) Log.d(TAG, String.format("Discarding Theme (rom mismatch) %s: Your Theme: %s %s; From JSON: %s %s", ui.getName(), themeInfos.name, themeInfos.version, ui.getName(), ui.getVersion()));
                    }
                } else {
                    if (showDebugOutput) Log.d(TAG, String.format("Discarding Update(not a Theme) %s Version %s", ui.getName(), ui.getVersion()));
                }
            } else {
                if (showDebugOutput) Log.d(TAG, String.format("Discarding Theme %s Version %s. Invalid or no Themes installed", ui.getName(), ui.getVersion()));
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static FullUpdateInfo FilterUpdates(FullUpdateInfo newList, FullUpdateInfo oldList) {
        if (showDebugOutput) Log.d(TAG, "Called FilterUpdates");
        if (showDebugOutput) Log.d(TAG, "newList Length: " + newList.getUpdateCount());
        if (showDebugOutput) Log.d(TAG, "oldList Length: " + oldList.getUpdateCount());
        FullUpdateInfo ful = new FullUpdateInfo();
        ful.roms = (LinkedList<UpdateInfo>) newList.roms.clone();
        ful.themes = (LinkedList<UpdateInfo>) newList.themes.clone();
        ful.incrementalRoms = (LinkedList<UpdateInfo>) newList.incrementalRoms.clone();
        ful.roms.removeAll(oldList.roms);
        ful.themes.removeAll(oldList.themes);
        ful.incrementalRoms.removeAll(oldList.incrementalRoms);
        if (showDebugOutput) Log.d(TAG, "fulList Length: " + ful.getUpdateCount());
        return ful;
    }

    private final Handler ToastHandler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.arg1 != 0) Toast.makeText(UpdateCheckService.this, msg.arg1, Toast.LENGTH_LONG).show(); else Toast.makeText(UpdateCheckService.this, (String) msg.obj, Toast.LENGTH_LONG).show();
        }
    };

    private void FinishUpdateCheck() {
        final int M = mCallbacks.beginBroadcast();
        for (int i = 0; i < M; i++) {
            try {
                mCallbacks.getBroadcastItem(i).UpdateCheckFinished();
            } catch (RemoteException e) {
            }
        }
        mCallbacks.finishBroadcast();
    }
}
