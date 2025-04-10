package org.tolven.deploy.jboss;

import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.tolven.plugin.TolvenCommandPlugin;
import org.tolven.security.hash.TolvenMessageDigest;

/**
 * This plugin deploys the JBoss configuration files.
 * 
 * @author Joseph Isaac
 *
 */
public class JBossDeploy extends TolvenCommandPlugin {

    public static final String MESSAGE_DIGEST_ALGORITHM = "md5";

    private Logger logger = Logger.getLogger(JBossDeploy.class);

    @Override
    protected void doStart() throws Exception {
        logger.debug("*** start ***");
    }

    @Override
    public void execute(String[] args) throws Exception {
        logger.debug("*** execute ***");
        String appserverHomeDirname = (String) evaluate("#{globalProperty['appserver.home']}", getDescriptor());
        File appserverHomeDir = new File(appserverHomeDirname);
        if (!appserverHomeDir.exists()) {
            throw new RuntimeException("appserver home does not exist at: " + appserverHomeDir.getPath());
        }
        File appserverStageDir = new File(getStageDir(), appserverHomeDir.getName());
        if (!appserverStageDir.exists()) {
            appserverStageDir.mkdirs();
        }
        Collection<File> stageFiles = FileUtils.listFiles(appserverStageDir, null, true);
        for (File stageFile : stageFiles) {
            String relativeStageFilename = stageFile.getPath().substring(appserverStageDir.getPath().length());
            File deployedFile = new File(appserverHomeDir, relativeStageFilename);
            if (deployedFile.exists()) {
                String stageFileDigest = TolvenMessageDigest.checksum(stageFile.toURI().toURL(), MESSAGE_DIGEST_ALGORITHM);
                String deployedFileDigest = TolvenMessageDigest.checksum(deployedFile.toURI().toURL(), MESSAGE_DIGEST_ALGORITHM);
                if (deployedFileDigest.equals(stageFileDigest)) {
                    continue;
                }
            }
            logger.info("Deploy " + stageFile.getPath() + " to " + deployedFile.getPath());
            FileUtils.copyFile(stageFile, deployedFile);
        }
    }

    @Override
    protected void doStop() throws Exception {
        logger.debug("*** stop ***");
    }
}
