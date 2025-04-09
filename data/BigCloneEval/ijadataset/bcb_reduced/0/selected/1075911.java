package com.mycila.maven.ui;

import org.jdesktop.swingworker.SwingWorker;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class Deployer extends SwingWorker<Void, Void> {

    final MavenDeployerGui gui;

    final File pom;

    Process deploy;

    Thread reader;

    Deployer(MavenDeployerGui gui, File pom) {
        this.gui = gui;
        this.pom = pom;
    }

    protected Void doInBackground() throws Exception {
        try {
            java.util.List<String> cmd = buildCommands();
            StringBuilder cmdStr = new StringBuilder("Executing:\n   ");
            for (String str : cmd) {
                cmdStr.append(" ").append(str);
            }
            cmdStr.append("\n\n");
            gui.console.setText(cmdStr.toString());
            gui.cancel.setEnabled(true);
            gui.deploy.setEnabled(false);
            gui.console.setCaretPosition(gui.console.getDocument().getLength());
            ProcessBuilder builder = new ProcessBuilder(cmd).redirectErrorStream(true);
            builder.environment().putAll(System.getenv());
            deploy = builder.start();
            reader = new Thread() {

                @Override
                public void run() {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(deploy.getInputStream()));
                        try {
                            String line;
                            while ((line = in.readLine()) != null && reader != null && deploy != null && !isCancelled()) {
                                gui.console.getDocument().insertString(gui.console.getDocument().getLength(), line + "\n", null);
                                gui.console.setCaretPosition(gui.console.getDocument().getLength());
                            }
                        } finally {
                            in.close();
                        }
                    } catch (Exception ignored) {
                    }
                }
            };
            reader.start();
            deploy.waitFor();
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            gui.console.setText(ExceptionUtils.asText(e));
        } finally {
            gui.deploy.setEnabled(true);
            gui.cancel.setEnabled(false);
        }
        return null;
    }

    List<String> buildCommands() throws MalformedURLException, URISyntaxException {
        java.util.List<String> cmd = new ArrayList<String>();
        cmd.add(gui.mavenBin.getText());
        cmd.add("deploy:deploy-file");
        cmd.add("-DuniqueVersion=false");
        if (gui.cbDeployPOM.isSelected()) {
            cmd.add("-DpomFile=" + pom.getAbsolutePath());
        } else {
            cmd.add("-DgroupId=" + gui.groupId.getText());
            cmd.add("-DartifactId=" + gui.artifactId.getText());
            cmd.add("-Dversion=" + gui.version.getText());
            cmd.add("-Dpackaging=" + gui.packaging.getSelectedItem());
            String classifier = (String) gui.classifier.getSelectedItem();
            if (classifier != null && classifier.trim().length() > 0) {
                cmd.add("-Dclassifier=" + classifier);
            }
            String desc = gui.description.getText();
            if (desc != null && desc.trim().length() > 0) {
                cmd.add("-Ddescription=" + desc);
            }
        }
        cmd.add("-Dfile=" + gui.artifactFile.getText());
        cmd.add("-Durl=" + toUrl(gui.repositoryURL.getText()));
        String repoId = gui.repositoryID.getText();
        if (repoId != null && repoId.trim().length() > 0) {
            cmd.add("-DrepositoryId=" + repoId);
        }
        return cmd;
    }

    String toUrl(String path) throws MalformedURLException, URISyntaxException {
        File file = new File(path);
        if (file.exists()) {
            return file.toURI().toString();
        }
        return new URI(path).toString();
    }

    public void stop() {
        cancel(true);
        if (deploy != null) {
            deploy.destroy();
            deploy = null;
        }
        if (reader != null) {
            reader.interrupt();
            reader = null;
        }
    }
}
