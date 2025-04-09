package com.android.cnes.groundsupport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.cnes.groundsupport.business.BuildingsDbAdapter;
import com.android.cnes.groundsupport.business.JSONHelper;
import com.android.cnes.groundsupport.collection.AudioObject;
import com.android.cnes.groundsupport.collection.BuildingObject;
import com.android.cnes.groundsupport.collection.DataEntryObject;
import com.android.cnes.groundsupport.collection.PhotoObject;
import com.android.cnes.groundsupport.collection.VideoObject;

public class SyncDataActivity extends Activity implements Runnable {

    private ArrayList<String> arrayList;

    private JSONHelper jsonHelper;

    private BuildingsDbAdapter myDb;

    private DataEntryObject dataEntryObject;

    private BuildingObject buildingObject;

    private ArrayList<VideoObject> videoObjects;

    private ArrayList<AudioObject> audioObjects;

    private ArrayList<PhotoObject> photoObjects;

    private String aNote;

    private String appUrlAttach = "/jeo_entry/jeo/upload";

    private Properties properties;

    private int errorCode = 0;

    private File toUpload;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sync_data);
        arrayList = new ArrayList<String>();
        Bundle extra = this.getIntent().getExtras();
        if (extra != null) {
            arrayList = extra.getStringArrayList("idList");
        }
        File propertiesFile = new File(Environment.getExternalStorageDirectory() + "/Cnes/osmmaps.properties");
        properties = new Properties();
        try {
            FileInputStream fileInputStream = new FileInputStream(propertiesFile);
            BufferedReader buf = new BufferedReader(new InputStreamReader(fileInputStream));
            properties.load(buf);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() throws ResourceException {
        myDb = new BuildingsDbAdapter(this);
        myDb.open();
        for (int i = 0; i < arrayList.size(); i++) {
            String jsonFile = Environment.getExternalStorageDirectory() + "/Cnes/" + arrayList.get(i) + "/" + arrayList.get(i) + ".json";
            dataEntryObject = myDb.getADataEntryWithId(arrayList.get(i));
            buildingObject = myDb.getABuilding(arrayList.get(i));
            videoObjects = myDb.getAllVideos(arrayList.get(i));
            photoObjects = myDb.getAllPhotos(arrayList.get(i));
            audioObjects = myDb.getAllAudios(arrayList.get(i));
            aNote = myDb.getANote(arrayList.get(i));
            jsonHelper = new JSONHelper(dataEntryObject, buildingObject, videoObjects, audioObjects, photoObjects, aNote);
            try {
                FileOutputStream fos = new FileOutputStream(new File(jsonFile));
                Writer out = new OutputStreamWriter(fos, "UTF8");
                out.write(jsonHelper.makeGlobalSyncJson());
                out.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
        myDb.close();
        for (int i = 0; i < arrayList.size(); i++) {
            String srcFolder = Environment.getExternalStorageDirectory() + "/Cnes/" + arrayList.get(i);
            String destZipFile = Environment.getExternalStorageDirectory() + "/Cnes/" + arrayList.get(i) + ".zip";
            try {
                zipFolder(srcFolder, destZipFile);
                toUpload = new File(destZipFile);
                if (!toUpload.exists()) {
                    Log.i("FILE MUST EXIST FOR TEST : ", toUpload.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Reference ref = new Reference(properties.getProperty("host_sitools") + appUrlAttach + "?" + "datastorage_url" + "=" + "/medias");
                ClientResource res = new ClientResource(ref);
                res.setChallengeResponse(ChallengeScheme.HTTP_BASIC, properties.getProperty("login"), properties.getProperty("password"));
                FileRepresentation rep = new FileRepresentation(toUpload, MediaType.APPLICATION_ZIP);
                Representation result = res.post(rep);
                Log.i("JSONUploadFile", "Resultat du post zip : " + res.getStatus().isSuccess());
            } catch (ResourceException e) {
                e.printStackTrace();
                Status status = e.getStatus();
                errorCode = status.getCode();
            }
        }
        Message message = new Message();
        handler.sendMessage(message);
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch(errorCode) {
                case 0:
                    finish();
                    break;
                case 403:
                    ConnexionError403Popup();
                    break;
                case 404:
                    ConnexionError404Popup();
                    break;
                case 500:
                    ConnexionError500Popup();
                    break;
                default:
                    ConnexionErrorPopup();
                    break;
            }
        }
    };

    public static void zipFolder(String srcFolder, String destZipFile) throws Exception {
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;
        fileWriter = new FileOutputStream(destZipFile);
        zip = new ZipOutputStream(fileWriter);
        addFolderToZip("", srcFolder, zip);
        zip.flush();
        zip.close();
    }

    private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
        File folder = new File(srcFolder);
        for (String fileName : folder.list()) {
            if (path.equals("")) {
                addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
            } else {
                addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
            }
        }
    }

    private static void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception {
        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip);
        } else {
            byte[] buf = new byte[1024];
            int len;
            FileInputStream in = new FileInputStream(srcFile);
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
        }
    }

    private void ConnexionErrorPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_connexion_error).setCancelable(false).setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void ConnexionError404Popup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_connexion_error_404).setCancelable(false).setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void ConnexionError403Popup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_connexion_error_403).setCancelable(false).setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void ConnexionError500Popup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_connexion_error_500).setCancelable(false).setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
