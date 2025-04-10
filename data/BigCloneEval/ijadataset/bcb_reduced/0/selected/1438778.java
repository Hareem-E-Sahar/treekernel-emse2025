package android.test.mock;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A mock {@link android.content.Context} class.  All methods are non-functional and throw 
 * {@link java.lang.UnsupportedOperationException}.  You can use this to inject other dependencies,
 * mocks, or monitors into the classes you are testing.
 */
public class MockContext extends Context {

    @Override
    public AssetManager getAssets() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resources getResources() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PackageManager getPackageManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentResolver getContentResolver() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Looper getMainLooper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context getApplicationContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTheme(int resid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resources.Theme getTheme() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPackageName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPackageResourcePath() {
        throw new UnsupportedOperationException();
    }

    /** @hide */
    @Override
    public File getSharedPrefsFile(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPackageCodePath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteFile(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getFileStreamPath(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] fileList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getFilesDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getCacheDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getDir(String name, int mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String file, int mode, SQLiteDatabase.CursorFactory factory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getDatabasePath(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] databaseList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteDatabase(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Drawable getWallpaper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Drawable peekWallpaper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getWallpaperDesiredMinimumWidth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getWallpaperDesiredMinimumHeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWallpaper(Bitmap bitmap) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWallpaper(InputStream data) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearWallpaper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startActivity(Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendBroadcast(Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendBroadcast(Intent intent, String receiverPermission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendStickyBroadcast(Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeStickyBroadcast(Intent intent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ComponentName startService(Intent service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean stopService(Intent service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean startInstrumentation(ComponentName className, String profileFile, Bundle arguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getSystemService(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkPermission(String permission, int pid, int uid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkCallingPermission(String permission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkCallingOrSelfPermission(String permission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enforcePermission(String permission, int pid, int uid, String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enforceCallingPermission(String permission, String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enforceCallingOrSelfPermission(String permission, String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void revokeUriPermission(Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
        throw new UnsupportedOperationException();
    }

    public void enforceUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags, String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRestricted() {
        throw new UnsupportedOperationException();
    }
}
