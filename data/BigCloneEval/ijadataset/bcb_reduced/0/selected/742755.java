package org.iascf.itmm.server.filegenerators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.iascf.itmm.ITMMProperties;
import org.iascf.itmm.client.configuration.impl.AIdentifiableComponent;
import org.iascf.itmm.client.configuration.impl.ExternalModule;
import org.iascf.itmm.client.configuration.impl.ModuleLocation;
import org.iascf.itmm.client.configuration.interfaces.IModule;
import org.iascf.itmm.client.configuration.interfaces.ISubModule;
import org.iascf.itmm.client.configuration.interfaces.ITaxonomy;
import org.iascf.itmm.client.rpc.interfaces.ConfigurationService;
import org.iascf.itmm.client.selection.impl.ClientSession;
import org.iascf.itmm.server.impl.ConfigurationServiceImpl;

/**
 *
 * This class handles the taxonomy bundle creation process. Corresponding to the clientsession selection required files will be created and zipped together.
 * 
 * @author Haiko Philipp (hphilipp@iasb.org)
 */
public class SessionHandler {

    private ITMMProperties config;

    private ConfigurationService conf;

    private Logger log = Logger.getLogger(this.getClass());

    private File sessionFolder;

    private File zipFolder;

    private ClientSession clientSession;

    private ITaxonomy taxonomy;

    private File entrypointFile;

    private File readmeFile;

    private File instanceFile;

    private File taxonomyFolder;

    private File zipOutputFile;

    private String zipFileName;

    private List schemaRef;

    private List linkbaseRefLL;

    private List linkbaseRefCL;

    private List linkbaseRefRL;

    private List linkbaseRefDL;

    private List linkbaseRefPL;

    /**
     * initiate the creation process and creates all required Files. 
     * 
     * @param clientSession holds all necessary information.
     */
    public SessionHandler(ClientSession clientSession) {
        this.clientSession = clientSession;
        config = ITMMProperties.getInstance();
        if (conf == null) {
            conf = new ConfigurationServiceImpl();
        }
        taxonomy = conf.getTaxonomy(clientSession.getTaxonomyID());
        schemaRef = new ArrayList();
        linkbaseRefLL = new ArrayList();
        linkbaseRefCL = new ArrayList();
        linkbaseRefRL = new ArrayList();
        linkbaseRefDL = new ArrayList();
        linkbaseRefPL = new ArrayList();
        zipFolder = new File(getClass().getClassLoader().getResource("../../zip").getFile());
        if (!zipFolder.exists()) {
            log.error("Can not find zip folder: " + zipFolder.getAbsolutePath());
            throw new RuntimeException("Can not find zip folder: " + zipFolder.getAbsolutePath());
        }
        sessionFolder = new File(zipFolder.getPath() + "/session" + clientSession.getSessionID());
        sessionFolder.mkdirs();
        if (!sessionFolder.exists()) {
            log.error("Can not find session folder: " + sessionFolder.getAbsolutePath());
            throw new RuntimeException("Can not find session folder: " + sessionFolder.getAbsolutePath());
        }
        sessionFolder.deleteOnExit();
        URL folderURL = getClass().getClassLoader().getResource("../../Taxonomy/" + clientSession.getTaxonomyID());
        if (folderURL == null) {
            log.error("Cannot find taxonomy folder. ResourceURL is null! ../../Taxonomy/" + clientSession.getTaxonomyID());
            throw new RuntimeException("Cannot find taxonomy folder. ResourceURL is null!../../Taxonomy/" + clientSession.getTaxonomyID());
        }
        taxonomyFolder = new File(getClass().getClassLoader().getResource("../../Taxonomy/" + clientSession.getTaxonomyID()).getFile());
        if (!taxonomyFolder.exists()) {
            log.error("Cannot find taxonomy folder: " + taxonomyFolder.getAbsolutePath());
            throw new RuntimeException("Can not find taxonomy folder: " + taxonomyFolder.getAbsolutePath());
        }
        if (clientSession.getTaxonomyOutputTyp().equals("instance")) {
            String instanceFilename = clientSession.getInstanceFilename();
            if (!instanceFilename.endsWith(".xml") && !instanceFilename.endsWith(".xbrl")) {
                instanceFilename = instanceFilename + ".xbrl";
            }
            instanceFile = new File(sessionFolder.getPath() + "/" + instanceFilename);
            if (!instanceFile.exists()) {
                try {
                    instanceFile.createNewFile();
                } catch (IOException e) {
                    System.err.println("instance file could not be written.(" + instanceFile.getAbsolutePath() + ")\n\r" + e.getMessage());
                    e.printStackTrace();
                    log.error("instance file could not be written.(" + instanceFile.getAbsolutePath() + ")\n\r" + e.getMessage());
                }
                instanceFile.deleteOnExit();
            }
            writeInstanceFile();
        } else if (clientSession.getTaxonomyOutputTyp().equals("schema")) {
            Date date = new Date();
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
            entrypointFile = new File(sessionFolder.getPath() + "/entry-point_" + s.format(date) + ".xsd");
            if (!entrypointFile.exists()) {
                try {
                    entrypointFile.createNewFile();
                } catch (IOException e) {
                    System.err.println("EntrypointSchema file could not be written.(" + entrypointFile.getAbsolutePath() + ")\n\r" + e.getMessage());
                    e.printStackTrace();
                    log.error("EntrypointSchema file could not be written.(" + entrypointFile.getAbsolutePath() + ")\n\r" + e.getMessage());
                }
                entrypointFile.deleteOnExit();
            }
            writeEntryPointSchema();
        }
        readmeFile = new File(sessionFolder.getPath() + "/readme.html");
        if (!readmeFile.exists()) {
            try {
                readmeFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Readme file could not be written.(" + readmeFile.getAbsolutePath() + ")\n\r" + e.getMessage());
                e.printStackTrace();
                log.error("Readme file could not be written.(" + readmeFile.getAbsolutePath() + ")\n\r" + e.getMessage());
            }
            readmeFile.deleteOnExit();
        }
        writeReadmeFile();
        zipOutputFile = new File(sessionFolder.getPath() + "/" + taxonomy.getName().replace(" ", "_").replace("(", "").replace(")", "").toLowerCase() + ".zip");
        if (!zipOutputFile.exists()) {
            try {
                zipOutputFile.createNewFile();
                zipOutputFile.deleteOnExit();
            } catch (IOException e) {
                System.err.println("ZipOutput file could not be written.(" + zipOutputFile.getAbsolutePath() + ")\n\r" + e.getMessage());
                e.printStackTrace();
                log.error("ZipOutput file could not be written.(" + zipOutputFile.getAbsolutePath() + ")\n\r" + e.getMessage());
            }
            zipOutputFile.deleteOnExit();
        }
        zipPackage();
        log.info("Finished ziping " + zipOutputFile.getAbsolutePath());
        if (clientSession.getTaxonomyOutputTyp().equals("instance")) {
            instanceFile.delete();
        } else if (clientSession.getTaxonomyOutputTyp().equals("schema")) {
            entrypointFile.delete();
        }
        readmeFile.delete();
        int interval = Integer.parseInt(config.getProperty("TIME_TO_DELETE_TEMPFILES"));
        Date timeToRun = new Date(System.currentTimeMillis() + interval);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                zipOutputFile.delete();
                System.err.println("deleting file");
                log.info("deleting file: " + zipOutputFile.getAbsolutePath());
                sessionFolder.delete();
                log.info("deleting sessionfolder: " + sessionFolder.getAbsolutePath());
            }
        }, timeToRun);
    }

    /**
     * This method initiates the filesgeneration process and the zip creation with the required parameters.
     */
    private void zipPackage() {
        try {
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipOutputFile));
            zipGeneratedFiles("", sessionFolder, zipOut);
            zipFolder("", taxonomyFolder, zipOut);
            zipOut.close();
            zipFileName = zipOutputFile.getName();
        } catch (IOException e) {
            log.error("Zipfile could not be written.\n\r" + e.getMessage());
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            log.error("Please make sure, that the Taxonomy is accessable at ProjectFolder/Taxonomy/... (e.g. ITMM/Taxonomy/2008-03-01_taxonomy) " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * This method copies the selected taxonomy in the output zip file.
     * 
     * @param internalPath relative path in the output zip package
     * @param dirZip directory to zip
     * @param zipOut outputstream of the resulting zip file
     * @throws java.io.IOException
     */
    private void zipFolder(String internalPath, File dirZip, ZipOutputStream zipOut) throws IOException {
        byte[] buf = new byte[4096];
        File[] fileArray = dirZip.listFiles();
        String fileName = "";
        if (fileArray != null) {
            for (int i = 0; i < fileArray.length; i++) {
                fileName = fileArray[i].getName();
                if (fileArray[i].isDirectory() && !fileName.startsWith(".")) {
                    zipFolder(internalPath + fileName + "/", fileArray[i], zipOut);
                } else if (!fileArray[i].isDirectory()) {
                    boolean include = true;
                    if (clientSession.getTranslationFileMapToSubModuleID().containsKey(fileName)) {
                        System.out.println("File: " + fileName);
                        String submoduleID = clientSession.getTranslationFileMapToSubModuleID().get(fileName);
                        System.out.println("SubModuleID: " + submoduleID);
                        String moduleID = null;
                        for (IModule module : taxonomy.getModules()) {
                            ISubModule sm = module.getSubModuleByID(submoduleID);
                            if (sm != null) {
                                moduleID = module.getID();
                                System.out.println("ModuelID: " + moduleID);
                            }
                        }
                        if (!clientSession.getClientSelection().constainsSubModule(moduleID, submoduleID)) {
                            System.out.println("NOT INCLUDED");
                            include = false;
                        }
                    }
                    if (include) {
                        FileInputStream inFile = new FileInputStream(fileArray[i]);
                        zipOut.putNextEntry(new ZipEntry(internalPath + fileName));
                        int len;
                        while ((len = inFile.read(buf)) > 0) {
                            zipOut.write(buf, 0, len);
                        }
                        inFile.close();
                    }
                }
            }
        }
    }

    /**
     * This method zippes the entrypoint schema / instance file and the readme file.
     * 
     * @param internalPath relative path in the output zip package
     * @param dirZip folder to zip
     * @param zipOut output zip file
     * @throws java.io.IOException
     */
    private void zipGeneratedFiles(String internalPath, File dirZip, ZipOutputStream zipOut) throws IOException {
        byte[] buf = new byte[4096];
        File[] fileArray = dirZip.listFiles();
        String fileName = "";
        if (fileArray != null) {
            for (int i = 0; i < fileArray.length; i++) {
                fileName = fileArray[i].getName();
                if (fileArray[i].isDirectory() && !fileName.startsWith(".")) {
                } else if (!fileArray[i].isDirectory() && !fileName.endsWith(".zip")) {
                    FileInputStream inFile = new FileInputStream(fileArray[i]);
                    zipOut.putNextEntry(new ZipEntry(internalPath + fileName));
                    int len;
                    while ((len = inFile.read(buf)) > 0) {
                        zipOut.write(buf, 0, len);
                    }
                    inFile.close();
                }
            }
        }
    }

    /**
     * This method writes the entry-point schema
     * 
     */
    private void writeEntryPointSchema() {
        OutputStreamWriter fos = null;
        try {
            fos = new OutputStreamWriter(new FileOutputStream(entrypointFile), "UTF-8");
            fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n\r" + "<!-- You should NOT OPEN this entry-point schema but IMPORT it into the entity specific extension schema. -->" + "\n\r" + "<schema xmlns:" + clientSession.getTaxonomyNamespacePrefix() + "=\"" + new URL(clientSession.getTaxonomyNamespace()).toURI().toURL().toString() + "\"" + "\n\r" + "    elementFormDefault=\"qualified\"" + "\n\r" + "    attributeFormDefault=\"unqualified\"" + "\n\r" + "    targetNamespace=\"" + new URL(clientSession.getTaxonomyNamespace()).toURI().toURL().toString() + "\"" + "\n\r" + "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"" + "\n\r" + "    xmlns:link=\"http://www.xbrl.org/2003/linkbase\"" + "\n\r" + "    xmlns:xlink=\"http://www.w3.org/1999/xlink\"" + "\n\r" + "    xmlns=\"http://www.w3.org/2001/XMLSchema\">" + "\n\r" + "    <annotation>" + "\n\r" + "        <appinfo>\n\r");
            boolean dimensionsSelected = false;
            Map selModules = clientSession.getClientSelection().getSelectedModules();
            Iterator keys = selModules.keySet().iterator();
            while (keys.hasNext()) {
                String moduleKey = (String) keys.next();
                IModule m = (IModule) taxonomy.getModuleByID(moduleKey);
                if (m.getType().equals(AIdentifiableComponent.MODULE_TYPE_DIMENSIONS_MODULE)) {
                    dimensionsSelected = true;
                }
                String modulePath = m.getPath();
                createEntryPointEntry(m.getModuleLocations(), m.getDescription(), modulePath);
                List ssm = clientSession.getClientSelection().getSelectedSubModules(moduleKey);
                ISubModule sm;
                for (int i = 0; i < ssm.size(); i++) {
                    sm = (ISubModule) taxonomy.getSubModuleByID(moduleKey, ssm.get(i).toString());
                    String subModulePath = sm.getPath();
                    createEntryPointEntry(sm.getModuleLocations(), sm.getDescription(), modulePath + subModulePath);
                }
            }
            for (int i = 0; i < linkbaseRefLL.size(); i++) {
                fos.write((String) linkbaseRefLL.get(i));
            }
            for (int i = 0; i < linkbaseRefRL.size(); i++) {
                fos.write((String) linkbaseRefRL.get(i));
            }
            for (int i = 0; i < linkbaseRefPL.size(); i++) {
                fos.write((String) linkbaseRefPL.get(i));
            }
            for (int i = 0; i < linkbaseRefCL.size(); i++) {
                fos.write((String) linkbaseRefCL.get(i));
            }
            for (int i = 0; dimensionsSelected && i < linkbaseRefDL.size(); i++) {
                fos.write((String) linkbaseRefDL.get(i));
            }
            fos.write("        </appinfo>\n\r" + "    </annotation>\n\r");
            for (int i = 0; i < schemaRef.size(); i++) {
                fos.write(schemaRef.get(i).toString());
            }
            List<ExternalModule> externalModules = taxonomy.getExternalModules();
            for (int i = 0; i < externalModules.size(); i++) {
                fos.write("    <import namespace=\"" + externalModules.get(i).getTargetNamespace() + "\" schemaLocation=\"" + externalModules.get(i).getFileLocation() + "\"/>\n\r");
            }
            fos.write("</schema>");
        } catch (IOException e) {
            System.err.println("Entrypoint-Schemafile could not be written.\n\r" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method creates the linkbaseRef entries in the entry-point schema.
     * 
     * @param mLocs selected components
     * @param description description of the file
     * @param pathAddition additional path information
     * @throws java.io.IOException
     */
    private void createEntryPointEntry(List mLocs, String description, String pathAddition) throws IOException {
        for (int i = 0; i < mLocs.size(); i++) {
            ModuleLocation mLoc = (ModuleLocation) mLocs.get(i);
            String type = mLoc.getType();
            String path = "";
            if (clientSession.getTaxonomyRefTyp().equals("relative")) {
                path = pathAddition + mLoc.getLocalFile();
            } else if (clientSession.getTaxonomyRefTyp().equals("absolute")) {
                path = mLoc.getAbsoluteURL();
            }
            if (type.equals(ModuleLocation.CLB)) {
                linkbaseRefCL.add("        <link:linkbaseRef xlink:href=\"" + path + "\"" + " xlink:title=\"" + config.getProperty("TEXT_CALCULATIONLB") + description + "\"" + " xlink:type=\"simple\"" + " xlink:role=\"http://www.xbrl.org/2003/role/calculationLinkbaseRef\"" + " xlink:arcrole=\"http://www.w3.org/1999/xlink/properties/linkbase\"/>\n\r");
            } else if (type.equals(ModuleLocation.GLB)) {
            } else if (type.equals(ModuleLocation.LLB)) {
                linkbaseRefLL.add("        <link:linkbaseRef xlink:href=\"" + path + "\"" + " xlink:title=\"" + config.getProperty("TEXT_LABELLB") + description + "\"" + " xlink:type=\"simple\"" + " xlink:role=\"http://www.xbrl.org/2003/role/labelLinkbaseRef\"" + " xlink:arcrole=\"http://www.w3.org/1999/xlink/properties/linkbase\"/>");
            } else if (type.equals(ModuleLocation.PLB)) {
                linkbaseRefPL.add("        <link:linkbaseRef xlink:href=\"" + path + "\"" + " xlink:title=\"" + config.getProperty("TEXT_PRESENTATIONLB") + description + "\"" + " xlink:type=\"simple\"" + " xlink:role=\"http://www.xbrl.org/2003/role/presentationLinkbaseRef\"" + " xlink:arcrole=\"http://www.w3.org/1999/xlink/properties/linkbase\"/>\n\r");
            } else if (type.equals(ModuleLocation.RLB)) {
                linkbaseRefRL.add("        <link:linkbaseRef xlink:href=\"" + path + "\"" + " xlink:title=\"" + config.getProperty("TEXT_REFERENCELB") + description + "\"" + " xlink:type=\"simple\"" + " xlink:role=\"http://www.xbrl.org/2003/role/referenceLinkbaseRef\"" + " xlink:arcrole=\"http://www.w3.org/1999/xlink/properties/linkbase\"/>\n\r");
            } else if (type.equals(ModuleLocation.DLB)) {
                linkbaseRefDL.add("        <link:linkbaseRef xlink:href=\"" + path + "\"" + " xlink:title=\"" + config.getProperty("TEXT_DEFINITIONLB") + description + "\"" + " xlink:type=\"simple\"" + " xlink:role=\"http://www.xbrl.org/2003/role/definitionLinkbaseRef\"" + " xlink:arcrole=\"http://www.w3.org/1999/xlink/properties/linkbase\"/>\n\r");
            } else if (type.equals(ModuleLocation.SCHEMA)) {
                schemaRef.add("    <import namespace=\"" + mLoc.getTargetNamespace() + "\" schemaLocation=\"" + path + "\"/>\n\r");
            }
        }
    }

    /**
     * Writes the readme file.
     */
    private void writeReadmeFile() {
        OutputStreamWriter fos = null;
        try {
            fos = new OutputStreamWriter(new FileOutputStream(readmeFile), "UTF-8");
            fos.write("<html>");
            String head = ITMMProperties.getInstance().getProperty("ITMM_README_HEADER");
            fos.write(head);
            fos.write("<body>");
            fos.write("<div class = 'normal'>");
            fos.write("<p class='header1'>Taxonomy Readme</p>");
            fos.write("<p>This Readme provides information about file structure and files included in the Taxonomy entry point file</p>");
            fos.write("<p class='header2'>Taxonomy</p>");
            fos.write("<p>Taxonomy name: " + taxonomy.getName() + "</p>");
            fos.write("<p>Taxonomy ID: " + taxonomy.getId() + "</p>");
            fos.write("<p>Taxonomy issue date: " + taxonomy.getIssueDate() + "</p>");
            fos.write("<p class='header2'>Configuration</p>");
            String entry = "";
            if (clientSession.getTaxonomyOutputTyp().equals("instance")) {
                entry = "Entry Point: " + instanceFile.getName();
            } else if (clientSession.getTaxonomyOutputTyp().equals("schema")) {
                entry = "Entry Point: " + entrypointFile.getName();
            }
            fos.write("<p>" + entry + "</p>");
            fos.write("<p>Entry Point type: " + clientSession.getTaxonomyOutputTyp() + "</p>");
            fos.write("<p>File path: " + clientSession.getTaxonomyRefTyp() + "</p>");
            fos.write("<p class='header2'>Module Selection</p>");
            Iterator keys = clientSession.getClientSelection().getSelectedModules().keySet().iterator();
            while (keys.hasNext()) {
                String moduleKey = (String) keys.next();
                IModule m = (IModule) taxonomy.getModuleByID(moduleKey);
                String modulePath = m.getID();
                fos.write("<p>" + modulePath + " (" + m.getDescription() + ")</p>");
                writeReadmeModuleLocation(m.getModuleLocations(), modulePath + "/", fos);
                List ssm = clientSession.getClientSelection().getSelectedSubModules(moduleKey);
                ISubModule sm;
                for (int i = 0; i < ssm.size(); i++) {
                    sm = (ISubModule) taxonomy.getSubModuleByID(moduleKey, ssm.get(i).toString());
                    String subModulePath = sm.getPath();
                    fos.write("<p>" + modulePath + subModulePath + " (" + sm.getDescription() + ")</p>");
                    writeReadmeModuleLocation(sm.getModuleLocations(), modulePath + subModulePath + "/", fos);
                }
                fos.write("<p><br/></p>");
            }
            fos.write("</div></body></html>");
        } catch (IOException e) {
            log.error("Readme could not be written.\n\r" + e.getMessage());
            throw new RuntimeException("Readme could not be written.\n\r" + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This method writes the path information into the readme file.
     * 
     * @param mLocs selected components
     * @param pathAddition additional path information
     * @param fos readme output stream
     * @throws java.io.IOException
     */
    private void writeReadmeModuleLocation(List mLocs, String pathAddition, OutputStreamWriter fos) throws IOException {
        for (int i = 0; i < mLocs.size(); i++) {
            ModuleLocation mLoc = (ModuleLocation) mLocs.get(i);
            String path = "";
            if (clientSession.getTaxonomyRefTyp().equals("relative")) {
                path = pathAddition + mLoc.getLocalFile();
            } else if (clientSession.getTaxonomyRefTyp().equals("absolute")) {
                path = pathAddition + mLoc.getAbsoluteURL();
            }
            fos.write("<p>" + path + "</p>");
        }
    }

    /**
     * Writes the instance file.
     */
    private void writeInstanceFile() {
        try {
            Map selModules = clientSession.getClientSelection().getSelectedModules();
            OutputStreamWriter fos = new OutputStreamWriter(new FileOutputStream(instanceFile), "UTF-8");
            String schemaLocation = ((ModuleLocation) taxonomy.getModuleByID("coreSchemas").getSubModules().get(0).getModuleLocations().get(0)).getTargetNamespace() + " ";
            if (clientSession.getTaxonomyRefTyp().equals("relative")) {
                schemaLocation += taxonomy.getModuleByID("coreSchemas").getSubModules().get(0).getModuleLocations().get(0).getLocalFile() + "\" ";
            } else if (clientSession.getTaxonomyRefTyp().equals("absolute")) {
                schemaLocation += taxonomy.getModuleByID("coreSchemas").getSubModules().get(0).getModuleLocations().get(0).getAbsoluteURL() + "\" ";
            }
            fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\r" + "<xbrl " + "xsi:schemaLocation=\"" + schemaLocation + "xmlns:link=\"http://www.xbrl.org/2003/linkbase\" " + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" " + "xmlns=\"http://www.xbrl.org/2003/instance\" " + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "xmlns:xbrli=\"http://www.xbrl.org/2003/instance\" " + "xmlns:iso4217=\"http://www.xbrl.org/2003/iso4217\" >\n\r");
            boolean dimensionsSelected = false;
            Iterator keys = selModules.keySet().iterator();
            while (keys.hasNext()) {
                String moduleKey = (String) keys.next();
                IModule m = (IModule) taxonomy.getModuleByID(moduleKey);
                if (m.getType().equals(AIdentifiableComponent.MODULE_TYPE_DIMENSIONS_MODULE)) {
                    dimensionsSelected = true;
                }
                String modulePath = "";
                modulePath = m.getPath();
                createInstanceEntry(m.getModuleLocations(), m.getDescription(), modulePath);
                List ssm = clientSession.getClientSelection().getSelectedSubModules(moduleKey);
                ISubModule sm;
                for (int i = 0; i < ssm.size(); i++) {
                    sm = (ISubModule) taxonomy.getSubModuleByID(moduleKey, ssm.get(i).toString());
                    String subModulePath = sm.getPath();
                    createInstanceEntry(sm.getModuleLocations(), sm.getDescription(), modulePath + subModulePath);
                }
            }
            List<ExternalModule> externalModules = taxonomy.getExternalModules();
            for (int i = 0; i < externalModules.size(); i++) {
                fos.write("        <link:schemaRef xlink:type=\"simple\" xlink:href=\"" + externalModules.get(i).getFileLocation() + "\"/>\n\r");
            }
            for (int i = 0; i < schemaRef.size(); i++) {
                fos.write((String) schemaRef.get(i));
            }
            for (int i = 0; i < linkbaseRefLL.size(); i++) {
                fos.write((String) linkbaseRefLL.get(i));
            }
            for (int i = 0; i < linkbaseRefRL.size(); i++) {
                fos.write((String) linkbaseRefRL.get(i));
            }
            for (int i = 0; i < linkbaseRefPL.size(); i++) {
                fos.write((String) linkbaseRefPL.get(i));
            }
            for (int i = 0; i < linkbaseRefCL.size(); i++) {
                fos.write((String) linkbaseRefCL.get(i));
            }
            for (int i = 0; dimensionsSelected && i < linkbaseRefDL.size(); i++) {
                fos.write((String) linkbaseRefDL.get(i));
            }
            fos.write("</xbrl>");
            fos.close();
        } catch (IOException e) {
            log.error("instancefile could not be written.\n\r" + e.getMessage());
            throw new RuntimeException("instancefile could not be written.\n\r" + e.getMessage());
        }
    }

    /**
     * Creates the linkbaseRef entries in the instance file.
     * 
     * @param mLocs selected components
     * @param description description of the selected components
     * @param pathAddition additional path information.
     */
    private void createInstanceEntry(List mLocs, String description, String pathAddition) {
        for (int i = 0; i < mLocs.size(); i++) {
            ModuleLocation mLoc = (ModuleLocation) mLocs.get(i);
            String type = mLoc.getType();
            String path = "";
            if (clientSession.getTaxonomyRefTyp().equals("relative")) {
                path = pathAddition + mLoc.getLocalFile();
            } else if (clientSession.getTaxonomyRefTyp().equals("absolute")) {
                path = mLoc.getAbsoluteURL();
            }
            if (type.equals(ModuleLocation.CLB)) {
                linkbaseRefCL.add("        <link:linkbaseRef xlink:href=\"" + path + "\"" + " xlink:title=\"" + config.getProperty("TEXT_CALCULATIONLB") + description + "\"" + " xlink:type=\"simple\"" + " xlink:role=\"http://www.xbrl.org/2003/role/calculationLinkbaseRef\"" + " xlink:arcrole=\"http://www.w3.org/1999/xlink/properties/linkbase\"/>\n\r");
            } else if (type.equals(ModuleLocation.GLB)) {
            } else if (type.equals(ModuleLocation.LLB)) {
                linkbaseRefLL.add("        <link:linkbaseRef xlink:href=\"" + path + "\"" + " xlink:title=\"" + config.getProperty("TEXT_LABELLB") + description + "\"" + " xlink:type=\"simple\" xlink:role=\"http://www.xbrl.org/2003/role/labelLinkbaseRef\"" + " xlink:arcrole=\"http://www.w3.org/1999/xlink/properties/linkbase\"/>\n\r");
            } else if (type.equals(ModuleLocation.PLB)) {
                linkbaseRefPL.add("        <link:linkbaseRef xlink:href=\"" + path + "\"" + " xlink:title=\"" + config.getProperty("TEXT_PRESENTATIONLB") + description + "\"" + " xlink:type=\"simple\"" + " xlink:role=\"http://www.xbrl.org/2003/role/presentationLinkbaseRef\"" + " xlink:arcrole=\"http://www.w3.org/1999/xlink/properties/linkbase\"/>\n\r");
            } else if (type.equals(ModuleLocation.RLB)) {
                linkbaseRefRL.add("        <link:linkbaseRef xlink:href=\"" + path + "\"" + " xlink:title=\"" + config.getProperty("TEXT_REFERENCELB") + description + "\"" + " xlink:type=\"simple\"" + " xlink:role=\"http://www.xbrl.org/2003/role/referenceLinkbaseRef\"" + " xlink:arcrole=\"http://www.w3.org/1999/xlink/properties/linkbase\"/>\n\r");
            } else if (type.equals(ModuleLocation.DLB)) {
                linkbaseRefDL.add("        <link:linkbaseRef xlink:href=\"" + path + "\"" + " xlink:title=\"" + config.getProperty("TEXT_DEFINITIONLB") + description + "\"" + " xlink:type=\"simple\"" + " xlink:role=\"http://www.xbrl.org/2003/role/definitionLinkbaseRef\"" + " xlink:arcrole=\"http://www.w3.org/1999/xlink/properties/linkbase\"/>\n\r");
            } else if (type.equals(ModuleLocation.SCHEMA)) {
                schemaRef.add("        <link:schemaRef xlink:type=\"simple\" xlink:title=\"" + description + "\" xlink:href=\"" + path + "\"/>\n\r");
            }
        }
    }

    /**
     * returns the zip file name.
     * 
     * @return name of the zip file
     */
    public String getZipFileName() {
        return zipFileName;
    }
}
