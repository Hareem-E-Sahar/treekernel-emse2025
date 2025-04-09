package org.slasoi.studio.plugin.deploymentproject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.slasoi.studio.plugin.deploymentproject.generated.BundleType;
import org.slasoi.studio.plugin.deploymentproject.generated.SlasoiDeployment;
import org.slasoi.studio.plugin.preferences.Preferences;
import org.slasoi.studio.plugin.support.AuxFiles;
import org.slasoi.studio.plugin.support.AuxMisc;
import org.slasoi.studio.plugin.support.XmlSupport;
import org.slasoi.studio.plugin.support.XmlSupport.XmlException;

public class SlasoiDeploymentEx {

    private final SlasoiDeployment deplType;

    private final IProject project;

    private static final Logger LOGGER = Logger.getLogger(SlasoiDeploymentEx.class.getName());

    public SlasoiDeploymentEx(IProject project) throws XmlException {
        this.project = project;
        InputStream deplXmlStream;
        try {
            deplXmlStream = project.getFile(DeploymentProjectSupport.DEPLOYMENT_XML_FILE).getContents();
        } catch (CoreException e) {
            throw new XmlException("Cannot access the deployment XML file: " + e.getLocalizedMessage(), e);
        }
        deplType = XmlSupport.unmarshal(SlasoiDeployment.class, deplXmlStream, DeploymentProjectSupport.getDeploymentXmlSchema());
    }

    public String getFrameworkRoot() {
        String res = deplType.getFrameworkRoot();
        if (res == null || res.length() == 0) res = Preferences.getFrameworkRoot();
        return res;
    }

    public String getSlaSoiHome() {
        return getFrameworkRoot() + File.separator + "common" + File.separator + "osgi-config";
    }

    public String getFrameworkQualityModelRepoDirectory() {
        return getSlaSoiHome() + File.separator + "software-servicemanager" + File.separator + "quality_model_repository";
    }

    public String getOrcHome() {
        return getFrameworkRoot() + File.separator + "orc-data";
    }

    public String getPaxRunnerDir() {
        return getFrameworkRoot() + File.separator + "pax-runner";
    }

    public String getServiceLandscape() {
        return resolveFilename(deplType.getServiceLandscape());
    }

    public List<String> getDependencySlatFiles() {
        ArrayList<String> rv = new ArrayList<String>();
        for (String filename : deplType.getDependencySlats().getFile()) {
            rv.add(resolveFilename(filename));
        }
        for (String dirname : deplType.getDependencySlats().getDirectory()) {
            rv.add(resolveFilename(dirname));
        }
        return rv;
    }

    public String getQualityModelDir() {
        return resolveFilename(deplType.getQualityModelDir());
    }

    public String getEvaluationServerUrl() {
        String res = deplType.getEvaluationServerUrl();
        if (res == null) res = Preferences.getEvaluationServerUrl();
        if (res == null || res.length() == 0) return null; else return res;
    }

    /**
	 * This is a hack. Lots of the framework uses this variable so we need to set it.
	 * @param deployment
	 */
    public void setSlasoiHomeEnvVariable() {
        AuxMisc.setEnv("SLASOI_HOME", getFrameworkRoot() + File.separator + "common" + File.separator + "osgi-config");
    }

    /**
	 * Applies the settings of the deployment project to the framework so that the latter can then be started.
	 * @return	true on success, false otherwise
	 */
    public void prepareFrameworkForLaunch() throws IOException {
        copyQualityModelsToFramework();
    }

    private static final String FLAG_FILE_NAME = ".copied-by-slasoi-studio.flag7602";

    /**
	 * Copies the quality models referenced by the slasoi.xml file to the framework.
	 * This is needed because the the framework can only load the Palladio component model
	 * from within the framework directory structure.
	 * @return	true on success, false otherwise
	 */
    public void copyQualityModelsToFramework() throws IOException {
        deleteQualityModelsFromFramework();
        File fwRepo = new File(getFrameworkQualityModelRepoDirectory());
        if (!fwRepo.isDirectory() || !fwRepo.canWrite()) throw new IOException("Cannot write to quality model repository: " + fwRepo.getAbsolutePath());
        if (getQualityModelDir() != null) {
            File qualityModelDir = new File(getQualityModelDir());
            if (!qualityModelDir.isDirectory()) throw new IOException("Directory with quality models (" + getQualityModelDir() + ") not found.");
            for (File model : qualityModelDir.listFiles()) {
                if (model.isDirectory()) {
                    if (AuxFiles.doesDirContainFile(fwRepo, model.getName())) {
                        throw new IOException("The quality model " + model.getName() + " already exists in the framework.");
                    }
                    File modelInRepo = new File(fwRepo.getAbsoluteFile() + File.separator + model.getName());
                    modelInRepo.mkdir();
                    for (File modelFile : model.listFiles()) {
                        if (!modelFile.isDirectory()) AuxFiles.copyFile(modelFile, modelInRepo);
                    }
                    File modelFlag = new File(modelInRepo.getAbsoluteFile() + File.separator + FLAG_FILE_NAME);
                    modelFlag.createNewFile();
                }
            }
        }
    }

    /**
	 * Undoes prepareFrameworkForLaunch() in order to bring the framework back to the original state.
	 */
    public void unprepareFrameworkAfterShutdown() throws IOException {
        deleteQualityModelsFromFramework();
    }

    /**
	 * Undoes copyQualityModelsToFramework.
	 * @throws IOException
	 */
    public void deleteQualityModelsFromFramework() throws IOException {
        File fwRepo = new File(getFrameworkQualityModelRepoDirectory());
        if (!fwRepo.isDirectory() || !fwRepo.canWrite()) throw new IOException("Cannot write to quality model repository: " + fwRepo.getAbsolutePath());
        for (File model : fwRepo.listFiles()) {
            if (model.isDirectory() && AuxFiles.doesDirContainFile(model, FLAG_FILE_NAME)) AuxFiles.deleteDir(model);
        }
    }

    public Map<String, String> getEnvironment() {
        HashMap<String, String> env = new HashMap<String, String>();
        env.putAll(System.getenv());
        env.put("FRAMEWORK_ROOT", this.getFrameworkRoot());
        env.put("SLASOI_HOME", this.getSlaSoiHome());
        env.put("SLASOI_ORC_HOME", this.getOrcHome());
        env.put("SERVICE_LANDSCAPE", this.getServiceLandscape());
        StringBuilder dependencySlats = new StringBuilder();
        for (String depSlatFile : getDependencySlatFiles()) dependencySlats.append(depSlatFile + ":");
        env.put("DEPENDENCY_SLAT", dependencySlats.toString());
        env.put("EVALUATION_SERVER_URL", this.getEvaluationServerUrl());
        return env;
    }

    public String getRunnerArgsFile() {
        return resolveFilename(deplType.getRunnerArgs());
    }

    private String resolveFilename(String filename) {
        if (filename == null) return null;
        File file = new File(filename);
        if (file.isAbsolute()) return filename;
        return project.getFile(filename).getLocation().toOSString();
    }

    public static class Bundle {

        public final String path;

        public final boolean autoStart;

        public Bundle(String path, boolean autoStart) {
            this.path = path;
            this.autoStart = autoStart;
        }

        public String getUrl() {
            System.out.println("file:" + path);
            return "file:" + path;
        }

        public String getPath() {
            return path;
        }

        public boolean isAutoStart() {
            return autoStart;
        }
    }

    public List<Bundle> getBundleList() {
        List<Bundle> bundles = new ArrayList<Bundle>();
        try {
            for (IProject referencedProject : project.getReferencedProjects()) {
                String refProjPath = referencedProject.getLocation().toOSString();
                addAllJars(new File(refProjPath), bundles);
            }
        } catch (CoreException e) {
            LOGGER.warning("Cannot enumerate referenced project: " + e.getMessage());
        }
        if (deplType.getBundles() != null) {
            for (BundleType bundleType : deplType.getBundles().getBundle()) {
                bundles.add(new Bundle(resolveFilename(bundleType.getValue()), bundleType.getAutoStart().equals("true")));
            }
        }
        return bundles;
    }

    private void addAllJars(File file, List<Bundle> bundles) {
        if (file.isDirectory()) {
            for (File innerFile : file.listFiles()) addAllJars(innerFile, bundles);
        } else if (file.getName().endsWith(".jar")) {
            try {
                JarFile jarFile = new JarFile(file);
                BufferedReader manifestReader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(jarFile.getEntry("META-INF/MANIFEST.MF"))));
                String line;
                boolean isValidManifest = false;
                while ((line = manifestReader.readLine()) != null) {
                    if (line.startsWith("Bundle-Name:")) {
                        isValidManifest = true;
                        break;
                    }
                }
                manifestReader.close();
                jarFile.close();
                if (isValidManifest) bundles.add(new Bundle(file.getAbsolutePath(), true));
            } catch (IOException e) {
                LOGGER.warning("The manifest of the jar archive " + file.getName() + " could not be read, ignoring this jar.");
            }
        }
    }
}
