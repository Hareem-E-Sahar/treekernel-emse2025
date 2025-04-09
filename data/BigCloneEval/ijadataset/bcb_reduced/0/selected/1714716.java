package jumpingnotes.deploy.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import jumpingnotes.codec.CodecParams;
import jumpingnotes.codec.CodecService;
import jumpingnotes.codec.CodecTask;
import jumpingnotes.codec.CodecTaskListener;
import jumpingnotes.deploy.DeployEventListener;
import jumpingnotes.deploy.DeployService;
import jumpingnotes.deploy.DeployTask;
import jumpingnotes.model.entity.Deploy;
import jumpingnotes.model.ErrorType;
import jumpingnotes.model.ResourceTypes;
import jumpingnotes.model.ResourceTypesUtil;
import jumpingnotes.model.Result;
import jumpingnotes.storage.StorageService;
import jumpingnotes.storage.UnsupportedStorageObject;

public class DeployServiceImpl implements DeployService, CodecTaskListener, ApplicationContextAware {

    private ApplicationContext appCtx;

    private String deployAppRoot;

    private String httpHost;

    private String httpPort;

    private CodecService codecService;

    private StorageService storageService;

    private Map<String, DeployTask> codecTaskMap = new HashMap<String, DeployTask>();

    private Pattern syncPlaceHolder;

    private List<DeployEventListener> listeners = new CopyOnWriteArrayList<DeployEventListener>();

    private ConcurrentHashMap<String, String> codecMap = new ConcurrentHashMap<String, String>();

    public void init() {
        codecService.addListener(this);
        syncPlaceHolder = Pattern.compile("\\$\\[(.+?)\\]");
    }

    public void setHttpHost(String httpHost) {
        this.httpHost = httpHost;
    }

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    public void setCodecService(CodecService codecService) {
        this.codecService = codecService;
    }

    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void setApplicationContext(ApplicationContext appCtx) {
        this.appCtx = appCtx;
    }

    public void setDeployAppRoot(String deployAppRoot) {
        this.deployAppRoot = deployAppRoot;
    }

    public int deploy(Deploy deploy, Object taskContext, CodecParams codecParams) {
        String deployType = deploy.getDeployType();
        if (deployType == null) {
            return -1;
        }
        String[] splits = deployType.split("/");
        if (splits.length != 2) {
            return -1;
        }
        String audioType = splits[0];
        String packageType = splits[1];
        if (!"packaged".equals(packageType) && !"unpackaged".equals(packageType)) {
            return -1;
        }
        int subType = ResourceTypesUtil.getAudioSubType(audioType);
        DeployTask deployTask = new DeployTask(deploy, taskContext);
        switch(subType) {
            case ResourceTypes.SUBTYPE_AUDIO_MP3:
                deploy.setSubtype(ResourceTypes.SUBTYPE_AUDIO_MP3);
                if (doDeploy(deployTask)) {
                    return 0;
                } else {
                    return -1;
                }
            case ResourceTypes.SUBTYPE_AUDIO_SWF:
            case ResourceTypes.SUBTYPE_AUDIO_3GPP:
            case ResourceTypes.SUBTYPE_AUDIO_FLV:
            case ResourceTypes.SUBTYPE_AUDIO_AAC:
                String taskId = UUID.randomUUID().toString();
                if (codecParams == null) {
                    codecParams = new CodecParams();
                    if (subType == ResourceTypes.SUBTYPE_AUDIO_SWF || subType == ResourceTypes.SUBTYPE_AUDIO_FLV) {
                        codecParams.setCopyInputCodec(true);
                    }
                }
                CodecTask task = new CodecTask(taskId, deploy.getAudio().getUuid() + ".mp3", deploy.getUuid() + "." + ResourceTypesUtil.audioSubTypeToExt(subType), codecParams);
                try {
                    task.setOutputFilePath(storageService.getObjectLocation(ResourceTypes.TYPE_DEPLOY, subType, deploy.getUuid()));
                } catch (Exception e) {
                    return -1;
                }
                task.setContext(taskContext);
                synchronized (codecTaskMap) {
                    codecTaskMap.put(taskId, deployTask);
                }
                if (codecService.addTask(task)) {
                    return 1;
                } else {
                    return -1;
                }
            case ResourceTypes.SUBTYPE_AUDIO_UNKNOWN:
            default:
                return -1;
        }
    }

    public void onTaskDone(CodecTask task) {
        String key = task.getOutputFilename().substring(0, 36);
        codecMap.replace(key, "100");
        System.out.println("DeployService.onTaskDone");
        synchronized (codecTaskMap) {
            DeployTask deployTask = codecTaskMap.get(task.getId());
            if (deployTask != null) {
                if (task.getCode() == CodecTask.CODEC_TASK_CODE_SUCCESS) {
                    doDeploy(deployTask);
                } else {
                }
                codecTaskMap.remove(task.getId());
            }
        }
    }

    @Override
    public void onTaskProgress(CodecTask task, int percentage) {
        String key = task.getOutputFilename().substring(0, 36);
        codecMap.replace(key, String.valueOf(percentage));
        System.out.println("DeployService.onTaskProgress.key:" + key + " percentage:" + percentage);
    }

    @Override
    public void onTaskStart(CodecTask task) {
        String key = task.getOutputFilename().substring(0, 36);
        if (codecMap.replace(key, "0") == null) {
            codecMap.put(key, "0");
        }
        System.out.println("DeployService.onTaskStart");
    }

    @Override
    public String getCodecTaskProgress(String uuid) {
        String key = uuid;
        if (codecMap.containsKey(key)) {
            return codecMap.get(key);
        } else {
            return null;
        }
    }

    public void addListener(DeployEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DeployEventListener listener) {
        listeners.remove(listener);
    }

    /**
	 * Generate the deploy sync file and package.
	 * @param deploy
	 * @return <tt>true</tt> if successful or <tt>false</tt> otherwise.
	 */
    protected boolean doDeploy(DeployTask deployTask) {
        Deploy deploy = deployTask.getDeploy();
        boolean isPackaged;
        String[] splits = deploy.getDeployType().split("/");
        String audioType = splits[0];
        String packageType = splits[1];
        if ("packaged".equals(packageType)) {
            isPackaged = true;
        } else {
            isPackaged = false;
        }
        int subType = ResourceTypesUtil.getAudioSubType(audioType);
        String ext = ResourceTypesUtil.audioSubTypeToExt(subType);
        String bookURL;
        String audioURL;
        boolean isSWF = subType == ResourceTypes.SUBTYPE_AUDIO_SWF;
        String bookUuid = deploy.getAudio().getRecordTask().getChapter().getUuid();
        String audioUuid;
        if (subType == ResourceTypes.SUBTYPE_AUDIO_MP3) {
            audioUuid = deploy.getAudio().getUuid();
        } else {
            audioUuid = deploy.getUuid();
        }
        if (isPackaged) {
            bookURL = "content/" + bookUuid + ".txt";
            audioURL = "content/" + audioUuid + "." + ext;
        } else {
            bookURL = getHttpURL(ResourceTypes.TYPE_BOOK, ResourceTypes.SUBTYPE_RESERVED, bookUuid);
            audioURL = getHttpURL(ResourceTypes.TYPE_AUDIO, subType, audioUuid);
        }
        try {
            fireDeployStartEvent(deployTask);
            BufferedReader syncFileReader = null;
            BufferedWriter deploySyncWriter = null;
            try {
                String syncUuid = deploy.getAudio().getSync().getUuid();
                syncFileReader = new BufferedReader(new FileReader(storageService.getObjectLocation(ResourceTypes.TYPE_SYNC, ResourceTypes.SUBTYPE_RESERVED, syncUuid)));
                String deployUuid = deploy.getUuid();
                deploySyncWriter = new BufferedWriter(new FileWriter(storageService.getObjectLocation(ResourceTypes.TYPE_DEPLOY, ResourceTypes.SUBTYPE_DEPLOY_SYNC, deployUuid)));
                String line = null;
                while ((line = syncFileReader.readLine()) != null) {
                    StringBuffer lineToWrite = new StringBuffer();
                    Matcher placeHolderMatcher = syncPlaceHolder.matcher(line);
                    while (placeHolderMatcher.find()) {
                        String toReplace = null;
                        String coreStr = placeHolderMatcher.group(1);
                        if (coreStr.equals("format")) {
                            toReplace = isSWF ? "swf" : "audio";
                        } else {
                            if (coreStr.startsWith("book:")) {
                                toReplace = bookURL;
                            } else if (coreStr.startsWith("audio:")) {
                                toReplace = audioURL;
                            }
                        }
                        if (toReplace != null) {
                            placeHolderMatcher.appendReplacement(lineToWrite, toReplace);
                        }
                    }
                    placeHolderMatcher.appendTail(lineToWrite);
                    deploySyncWriter.write(lineToWrite.toString() + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (syncFileReader != null) {
                        syncFileReader.close();
                    }
                    if (deploySyncWriter != null) {
                        deploySyncWriter.close();
                    }
                } catch (IOException e) {
                }
            }
            if (isPackaged) {
                ZipOutputStream deployFile = null;
                try {
                    String deployUuid = deploy.getUuid();
                    deployFile = new ZipOutputStream(new FileOutputStream(storageService.getObjectLocation(ResourceTypes.TYPE_DEPLOY, ResourceTypes.SUBTYPE_DEPLOY_ZIP, deployUuid)));
                    ZipEntry entry = new ZipEntry(bookURL);
                    deployFile.putNextEntry(entry);
                    storageService.loadStream(ResourceTypes.TYPE_BOOK, ResourceTypes.SUBTYPE_RESERVED, bookUuid, deployFile);
                    deployFile.closeEntry();
                    entry = new ZipEntry(audioURL);
                    deployFile.putNextEntry(entry);
                    storageService.loadStream(ResourceTypes.TYPE_DEPLOY, subType, audioUuid, deployFile);
                    deployFile.closeEntry();
                    entry = new ZipEntry("content/syncFile.xml");
                    deployFile.putNextEntry(entry);
                    storageService.loadStream(ResourceTypes.TYPE_DEPLOY, ResourceTypes.SUBTYPE_DEPLOY_SYNC, deploy.getUuid(), deployFile);
                    deployFile.closeEntry();
                    String appRoot = null;
                    if (deployAppRoot != null) {
                        appRoot = deployAppRoot;
                    } else {
                        File ctxRoot = appCtx.getResource("/").getFile();
                        appRoot = ctxRoot.getCanonicalPath() + File.separator + "DeployApp";
                    }
                    zipFolder(deployFile, appRoot, "");
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } catch (UnsupportedStorageObject e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    try {
                        if (deployFile != null) {
                            deployFile.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
            return true;
        } finally {
            fireDeployDoneEvent(deployTask);
        }
    }

    private String getHttpURL(int resourceType, int resourceSubType, String uuid) {
        StringBuffer buf = new StringBuffer();
        buf.append("http://").append(httpHost).append(":").append(httpPort).append("/jumpingNotes/download?type=");
        switch(resourceType) {
            case ResourceTypes.TYPE_BOOK:
                buf.append(ResourceTypes.TYPE_BOOK);
                break;
            case ResourceTypes.TYPE_AUDIO:
                if (resourceSubType == ResourceTypes.SUBTYPE_AUDIO_MP3) {
                    buf.append(ResourceTypes.TYPE_AUDIO);
                } else {
                    buf.append(ResourceTypes.TYPE_DEPLOY);
                }
                break;
        }
        buf.append("&uuid=").append(uuid);
        buf.append("&subtype=").append(resourceSubType);
        return buf.toString();
    }

    private void zipFolder(ZipOutputStream zout, String folder, String zipinPrefix) throws IOException {
        byte[] buf = new byte[1024];
        File folderDir = new File(folder);
        File[] files = folderDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (".svn".equals(file.getName())) {
                    continue;
                }
                if (file.isDirectory()) {
                    String newPrefix = null;
                    if (zipinPrefix.equals("")) {
                        newPrefix = file.getName();
                    } else {
                        newPrefix = zipinPrefix + "/" + file.getName();
                    }
                    zipFolder(zout, file.getCanonicalPath(), newPrefix);
                } else {
                    String entryName = null;
                    if (zipinPrefix.equals("")) {
                        entryName = file.getName();
                    } else {
                        entryName = zipinPrefix + "/" + file.getName();
                    }
                    ZipEntry entry = new ZipEntry(entryName);
                    zout.putNextEntry(entry);
                    FileInputStream fin = null;
                    try {
                        fin = new FileInputStream(file);
                        int bytesRead;
                        while ((bytesRead = fin.read(buf)) >= 0) {
                            zout.write(buf, 0, bytesRead);
                        }
                    } finally {
                        if (fin != null) {
                            fin.close();
                        }
                    }
                    zout.closeEntry();
                }
            }
        }
    }

    private void fireDeployStartEvent(DeployTask deployTask) {
        for (DeployEventListener listener : listeners) {
            listener.onDeployStart(deployTask);
        }
    }

    private void fireDeployDoneEvent(DeployTask deployTask) {
        for (DeployEventListener listener : listeners) {
            listener.onDeployDone(deployTask);
        }
    }

    private void fireDeployProgressEvent(DeployTask deployTask, int percentage) {
        for (DeployEventListener listener : listeners) {
            listener.onDeployProgress(deployTask, percentage);
        }
    }
}
