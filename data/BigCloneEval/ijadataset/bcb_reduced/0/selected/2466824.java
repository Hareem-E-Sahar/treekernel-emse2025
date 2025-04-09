package org.mitre.rt.client.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.mitre.rt.rtclient.*;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.mitre.cpe.language.x20.CheckFactRefType;
import org.mitre.cpe.language.x20.PlatformSpecificationType;
import org.mitre.rt.client.core.MetaManager;
import org.mitre.rt.client.util.smash.FileIdTuple;
import org.mitre.rt.client.util.smash.oval.OvalSmasherImpl;
import org.mitre.rt.client.exceptions.RTClientException;
import org.mitre.rt.client.properties.RTClientProperties;
import org.mitre.rt.client.util.smash.ocil.OcilSmasherImpl;
import org.mitre.rt.client.xml.ApplicationHelper;
import org.mitre.rt.client.xml.CheckLanguageHelper;
import org.mitre.rt.client.xml.CheckValueHelper;
import org.mitre.rt.client.xml.ComplianceCheckHelper;
import org.mitre.rt.client.xml.FileTypeHelper;
import org.mitre.rt.client.xml.GroupHelper;
import org.mitre.rt.client.xml.ProfileHelper;
import org.mitre.rt.client.xml.RecommendationHelper;
import org.mitre.rt.client.xml.ReferenceHelper;
import org.mitre.rt.common.util.files.CopyFile;
import org.mitre.rt.common.xml.XSLProcessor;
import org.mitre.rt.rtclient.ApplicationType.PlatformReferences;

/**
 *
 * @author BWORRELL
 */
public class XCCDFGenerator {

    protected Logger logger = Logger.getLogger(XCCDFGenerator.class.getPackage().getName());

    private boolean boolZipOutput = false, boolMergeChecks = false, boolCreateHTMLView = false, boolCreateHTMLCheckListView = false;

    private int intStatusOrder = -1;

    private HashSet<String> filesToZip = new HashSet<String>();

    private final ApplicationHelper applicationHelper = new ApplicationHelper();

    private final GroupHelper groupHelper = new GroupHelper();

    private final ProfileHelper profileHelper = new ProfileHelper();

    private final RecommendationHelper recommendationHelper = new RecommendationHelper();

    private final ReferenceHelper referenceHelper = new ReferenceHelper();

    private final CheckLanguageHelper checkLanguageHelper = new CheckLanguageHelper();

    private final FileTypeHelper fileTypeHelper = new FileTypeHelper();

    private final ComplianceCheckHelper complianceCheckHelper = new ComplianceCheckHelper();

    private final CheckValueHelper checkValueHelper = new CheckValueHelper();

    public XCCDFGenerator() {
        this.init();
    }

    private void init() {
    }

    /**
     * Zip the specified directory into the specified file name.
     * @param dir The directory of files to be compressed
     * @param zipFilename The name of the resulting zip file
     */
    private void zipFiles(File dir, String zipFilename) {
        byte[] buf = new byte[1024];
        try {
            File zipFile = new File(zipFilename);
            if (zipFile.exists()) {
                zipFile.delete();
            }
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFilename));
            for (String zipThis : filesToZip) {
                File fileToZip = new File(dir, zipThis);
                if (fileToZip.exists() && fileToZip.canRead() && !fileToZip.isDirectory()) {
                    try {
                        FileInputStream in = new FileInputStream(fileToZip);
                        out.putNextEntry(new ZipEntry(fileToZip.getName()));
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.closeEntry();
                        in.close();
                    } catch (FileNotFoundException fnfe) {
                        logger.info("Couldn't open file to zip " + fnfe);
                    }
                }
            }
            out.close();
        } catch (IOException e) {
            logger.fatal("Error zipping generated XCCDF.", e);
        }
    }

    /**
     * Copy all the references files into place. 
     * Only copy the files associated with rule that have a status oorder less
     * than or equal to the input status order.
     * 
     * @param dir
     * @param statusOrder
     */
    private void copyReferenceFiles(ApplicationType application, List<String> platformSpecFiles, File dir, int statusOrder) {
        if (application.isSetReferences()) {
            List<ReferenceType> appRefs = referenceHelper.getActiveItems(application.getReferences().getReferenceList());
            for (ReferenceType appRef : appRefs) {
                if (appRef.isSetFileRef()) {
                    this.copyFile(application, dir, appRef.getFileRef());
                }
            }
            if (application.isSetRecommendations()) {
                List<RecommendationType> rules = application.getRecommendations().getRecommendationList();
                for (RecommendationType rule : rules) {
                    if ((rule.getChangeType() != ChangeTypeEnum.DELETED) && (!rule.isSetDeleted() || !rule.getDeleted())) {
                        if (rule.isSetReferences()) {
                            OrderedDeleteableSharedIdValuePairType statusObj = recommendationHelper.getStatusObj(application, rule);
                            int currentStatusOrder = -1;
                            if (statusObj != null) {
                                currentStatusOrder = statusObj.getOrder().intValue();
                            }
                            if (currentStatusOrder >= statusOrder) {
                                List<String> ruleRefs = rule.getReferences().getReferenceRefList();
                                for (String ruleRef : ruleRefs) {
                                    ReferenceType ref = referenceHelper.getItem(application.getReferences().getReferenceList(), ruleRef);
                                    if (ref.isSetFileRef()) {
                                        this.copyFile(application, dir, ref.getFileRef());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (application.isSetGroups()) {
                List<GroupType> groups = application.getGroups().getGroupList();
                for (GroupType group : groups) {
                    if (group.getChangeType() != ChangeTypeEnum.DELETED) {
                        if (group.isSetReferences()) {
                            OrderedDeleteableSharedIdValuePairType statusObj = groupHelper.getStatusObj(application, group);
                            int currentStatusOrder = -1;
                            if (statusObj != null) {
                                currentStatusOrder = statusObj.getOrder().intValue();
                            }
                            if (currentStatusOrder >= statusOrder) {
                                List<String> groupRefs = group.getReferences().getReferenceRefList();
                                for (String groupRef : groupRefs) {
                                    ReferenceType ref = referenceHelper.getItem(application.getReferences().getReferenceList(), groupRef);
                                    if (ref.isSetFileRef()) {
                                        this.copyFile(application, dir, ref.getFileRef());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (application.isSetProfiles()) {
            List<ProfileType> profiles = application.getProfiles().getProfileList();
            for (ProfileType profile : profiles) {
                if (profile.getChangeType() != ChangeTypeEnum.DELETED) {
                    if (profile.isSetReferences()) {
                        OrderedDeleteableSharedIdValuePairType statusObj = profileHelper.getStatusObj(application, profile);
                        int currentStatusOrder = -1;
                        if (statusObj != null) {
                            currentStatusOrder = statusObj.getOrder().intValue();
                        }
                        if (currentStatusOrder >= statusOrder) {
                            List<String> profileRefs = profile.getReferences().getReferenceRefList();
                            for (String profileRef : profileRefs) {
                                ReferenceType ref = referenceHelper.getItem(application.getReferences().getReferenceList(), profileRef);
                                if (ref.isSetFileRef()) {
                                    this.copyFile(application, dir, ref.getFileRef());
                                }
                            }
                        }
                    }
                }
            }
        }
        for (String id : platformSpecFiles) {
            this.copyFile(application, dir, id);
        }
    }

    /**
     * Returns a list of compliance checks for all rules which comply with the status baseline
     * @param app
     * @param statusOrder
     * @return 
     */
    private List<ComplianceCheckType> getComplianceChecks(ApplicationType application, int statusOrder) {
        List<ComplianceCheckType> listChecks = null;
        if (application.isSetComplianceChecks() && application.isSetRecommendations()) {
            List<RecommendationType> rules = application.getRecommendations().getRecommendationList();
            List<ComplianceCheckType> complianceChecks = application.getComplianceChecks().getComplianceCheckList();
            listChecks = new ArrayList<ComplianceCheckType>(complianceChecks.size());
            for (RecommendationType rule : rules) {
                if ((rule.getChangeType() != ChangeTypeEnum.DELETED) && (!rule.isSetDeleted() || !rule.getDeleted())) {
                    if (rule.isSetComplianceCheckRefs()) {
                        OrderedDeleteableSharedIdValuePairType statusObj = this.recommendationHelper.getStatusObj(application, rule);
                        int currentStatusOrder = -1;
                        if (statusObj != null) {
                            currentStatusOrder = statusObj.getOrder().intValue();
                        }
                        if (currentStatusOrder >= statusOrder) {
                            List<String> checkRefs = rule.getComplianceCheckRefs().getComplianceCheckRefList();
                            for (String checkRef : checkRefs) {
                                ComplianceCheckType check = this.complianceCheckHelper.getItem(complianceChecks, checkRef);
                                if (check != null) {
                                    listChecks.add(check);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            listChecks = Collections.emptyList();
        }
        return listChecks;
    }

    /**
     * Copy all the check files into place.
     * Only copy the files associated with rules that have a status order less
     * than or equal to the input status order.
     *
     * @param dir
     * @param statusOrder
     */
    private void copyCheckFiles(ApplicationType application, File dir, int statusOrder) throws RTClientException {
        List<ComplianceCheckType> complianceChecks = this.getComplianceChecks(application, statusOrder);
        for (ComplianceCheckType check : complianceChecks) {
            if (check.isSetCheckContent()) {
                CheckContentType checkContent = check.getCheckContent();
                this.copyFile(application, dir, checkContent.getFileRef());
            }
        }
    }

    /**
     * Copy all the cpe files into place.
     * @param dir
     */
    private void copyCPEFiles(ApplicationType application, File dir) throws RTClientException {
        if (application.isSetPlatformReferences()) {
            PlatformReferences pRefs = application.getPlatformReferences();
            if (pRefs.isSetDictionaryFile()) {
                String dictFileId = pRefs.getDictionaryFile();
                String destFilename = application.getName() + "-" + "cpe-dictionary.xml";
                this.copyFile(application, dir, dictFileId, destFilename);
            }
            if (pRefs.isSetOVALFile()) {
                String ovalFileId = pRefs.getOVALFile();
                String destFilename = application.getName() + "-" + "cpe-oval.xml";
                this.copyFile(application, dir, ovalFileId, destFilename);
            }
        }
    }

    /**
     * Copy the file that is referenced by the RT fileType object with the specified id to
     * the specified output directory
     * 
     * @param dir output directory
     * @param fileTypeId id of the file to copy.
     */
    private void copyFile(ApplicationType application, File dir, String fileTypeId) {
        FileTypeHelper fileHelper = new FileTypeHelper();
        FileType fileType = fileHelper.getItem(application.getFiles().getFileList(), fileTypeId);
        RTClientProperties props = RTClientProperties.instance();
        String path = fileType.getPath();
        String name = fileType.getFileName();
        StringBuilder srcFilePath = new StringBuilder(props.getFilesDir());
        srcFilePath.append(path);
        srcFilePath.append(File.separator);
        srcFilePath.append(name);
        StringBuilder destFilePath = new StringBuilder(dir.toString());
        destFilePath.append(File.separator);
        destFilePath.append(fileType.getId());
        destFilePath.append(fileType.getOrigFileName());
        try {
            CopyFile.copyFile(srcFilePath.toString(), destFilePath.toString(), true);
            filesToZip.add(name);
        } catch (IOException ex) {
            logger.fatal("Error copying file into place", ex);
        }
    }

    /**
     * Copy the file that is referenced by the RT fileType object with the specified id to
     * the specified output directory with the specified name
     *
     * @param dir output directory
     * @param fileTypeId id of the file to copy.
     * @param destName name of the file in the new output directory
     */
    private void copyFile(ApplicationType application, File dir, String fileTypeId, String destName) {
        FileTypeHelper fileHelper = new FileTypeHelper();
        FileType fileType = fileHelper.getItem(application.getFiles().getFileList(), fileTypeId);
        RTClientProperties props = RTClientProperties.instance();
        String path = fileType.getPath();
        String name = fileType.getFileName();
        StringBuilder srcFilePath = new StringBuilder(props.getFilesDir());
        srcFilePath.append(path);
        srcFilePath.append(File.separator);
        srcFilePath.append(name);
        StringBuilder destFilePath = new StringBuilder(dir.toString());
        destFilePath.append(File.separator);
        destFilePath.append(destName);
        try {
            CopyFile.copyFile(srcFilePath.toString(), destFilePath.toString(), true);
            filesToZip.add(name);
        } catch (IOException ex) {
            logger.fatal("Error copying file into place", ex);
        }
    }

    /**
     * Merges multiple OVAL documents into a single OVAL Document.
     * @param smasher
     * @param rt
     * @param application
     * @param outputDir
     * @param namespace
     * @throws Exception 
     */
    private void mergeOVALDocuments(OvalSmasherImpl smasher, final RTDocument rt, final ApplicationType application, String outputDir, final String namespace) throws Exception {
        List<ComplianceCheckType> complianceChecks = this.getComplianceChecks(application, intStatusOrder);
        List<String> ovalCheckFiles = new ArrayList<String>(complianceChecks.size());
        List<CheckLanguageType> checkLanguages = (application.isSetCheckLanguages()) ? application.getCheckLanguages().getCheckLanguageList() : Collections.<CheckLanguageType>emptyList();
        List<FileType> appFiles = (application.isSetFiles()) ? application.getFiles().getFileList() : Collections.<FileType>emptyList();
        List<ComplianceCheckType> listOvalCheck = new ArrayList<ComplianceCheckType>(complianceChecks.size());
        Map<String, File> mapOvalCheckToFile = new LinkedHashMap<String, File>(complianceChecks.size());
        Map<String, FileType> mapCheckToFileType = new LinkedHashMap<String, FileType>(complianceChecks.size());
        for (ComplianceCheckType check : complianceChecks) {
            if (check.isSetCheckContent()) {
                CheckContentType cct = check.getCheckContent();
                String checkLanguageId = cct.getCheckLanguageId();
                CheckLanguageType checkLanguage = checkLanguageHelper.getItem(checkLanguages, checkLanguageId);
                if (checkLanguage != null && checkLanguage.getNamespaceURI().equals(smasher.getNamespaceURI())) {
                    String fileId = cct.getFileRef();
                    FileType file = fileTypeHelper.getItem(appFiles, fileId);
                    if (file != null) {
                        String path = fileTypeHelper.getFilePath(file);
                        if (ovalCheckFiles.contains(path) == false) {
                            ovalCheckFiles.add(path);
                        }
                        mapOvalCheckToFile.put(check.getId(), new File(path));
                        mapCheckToFileType.put(check.getId(), file);
                        listOvalCheck.add(check);
                    } else throw new FileNotFoundException("Unable to locate OVAL check file for compliance check: " + check.getId());
                }
            }
        }
        if (ovalCheckFiles.isEmpty() == false) {
            File outputFile = new File(outputDir, application.getName() + "-oval.xml");
            smasher.smash(ovalCheckFiles, namespace, outputFile.getPath());
            Map<FileIdTuple, String> mapNewDefinitions = smasher.getNewDefinitionIdMap();
            Map<FileIdTuple, String> mapNewVariables = smasher.getNewVariableIdMap();
            for (ComplianceCheckType check : listOvalCheck) {
                CheckContentType cct = check.getCheckContent();
                File checkFile = mapOvalCheckToFile.get(check.getId());
                if (mapNewDefinitions.isEmpty() == false) {
                    String origDefId = cct.getCheckName();
                    FileIdTuple defIdTuple = new FileIdTuple(origDefId, checkFile);
                    String newDefId = mapNewDefinitions.get(defIdTuple);
                    if (newDefId != null) {
                        logger.debug(defIdTuple.toString() + " == " + newDefId);
                        cct.setCheckName(newDefId);
                    }
                }
                if (mapNewVariables.isEmpty() == false) {
                    if (cct.isSetCheckValueRefs()) {
                        List<String> valueRefs = cct.getCheckValueRefs().getCheckValueRefList();
                        List<CheckValueType> checkValues = application.getCheckValues().getCheckValueList();
                        List<CheckValueType> referencedValues = checkValueHelper.getStringReferencedItems(checkValues, valueRefs);
                        for (CheckValueType checkValue : referencedValues) {
                            String origCheckId = (checkValue.isSetName()) ? checkValue.getName() : "";
                            FileIdTuple checkIdTuple = new FileIdTuple(origCheckId, checkFile);
                            String newCheckId = mapNewVariables.get(checkIdTuple);
                            if (newCheckId != null) {
                                logger.debug(checkIdTuple.toString() + " == " + newCheckId);
                                checkValue.setName(newCheckId);
                            }
                        }
                    }
                }
                FileType fileType = mapCheckToFileType.get(check.getId());
                fileType.setFileName(outputFile.getName());
            }
        }
    }

    private void mergeOCILDocuments(OcilSmasherImpl smasher, final RTDocument rt, final ApplicationType application, String outputDir, final String namespace) throws Exception {
        List<ComplianceCheckType> complianceChecks = this.getComplianceChecks(application, intStatusOrder);
        List<String> ocilCheckFiles = new ArrayList<String>(complianceChecks.size());
        List<CheckLanguageType> checkLanguages = (application.isSetCheckLanguages()) ? application.getCheckLanguages().getCheckLanguageList() : Collections.<CheckLanguageType>emptyList();
        List<FileType> appFiles = (application.isSetFiles()) ? application.getFiles().getFileList() : Collections.<FileType>emptyList();
        List<ComplianceCheckType> listOcilCheck = new ArrayList<ComplianceCheckType>(complianceChecks.size());
        Map<String, File> mapOcilCheckToFile = new LinkedHashMap<String, File>(complianceChecks.size());
        Map<String, FileType> mapCheckToFileType = new LinkedHashMap<String, FileType>(complianceChecks.size());
        for (ComplianceCheckType check : complianceChecks) {
            if (check.isSetCheckContent()) {
                CheckContentType cct = check.getCheckContent();
                String checkLanguageId = cct.getCheckLanguageId();
                CheckLanguageType checkLanguage = checkLanguageHelper.getItem(checkLanguages, checkLanguageId);
                if (checkLanguage != null && checkLanguage.getNamespaceURI().equals(smasher.getNamespaceURI())) {
                    String fileId = cct.getFileRef();
                    FileType file = fileTypeHelper.getItem(appFiles, fileId);
                    if (file != null) {
                        String path = fileTypeHelper.getFilePath(file);
                        if (ocilCheckFiles.contains(path) == false) {
                            ocilCheckFiles.add(path);
                        }
                        mapOcilCheckToFile.put(check.getId(), new File(path));
                        mapCheckToFileType.put(check.getId(), file);
                        listOcilCheck.add(check);
                    } else throw new FileNotFoundException("Unable to locate OCIL check file for compliance check: " + check.getId());
                }
            }
        }
        if (ocilCheckFiles.isEmpty() == false) {
            File outputFile = new File(outputDir, application.getName() + "-ocil.xml");
            smasher.smash(ocilCheckFiles, namespace, outputFile.getPath());
            Map<FileIdTuple, String> mapNewQuestionnaires = smasher.getMapNewQuestionnaireIds();
            Map<FileIdTuple, String> mapNewVariables = smasher.getMapNewVariableIds();
            for (ComplianceCheckType check : listOcilCheck) {
                CheckContentType cct = check.getCheckContent();
                File checkFile = mapOcilCheckToFile.get(check.getId());
                if (mapNewQuestionnaires.isEmpty() == false) {
                    String origQuestionnaireId = cct.getCheckName();
                    FileIdTuple questionnaireIdTuple = new FileIdTuple(origQuestionnaireId, checkFile);
                    String newQuestionnaireId = mapNewQuestionnaires.get(questionnaireIdTuple);
                    if (newQuestionnaireId != null) {
                        logger.debug(questionnaireIdTuple.toString() + " == " + newQuestionnaireId);
                        cct.setCheckName(newQuestionnaireId);
                    }
                }
                if (mapNewVariables.isEmpty() == false) {
                    if (cct.isSetCheckValueRefs()) {
                        List<String> valueRefs = cct.getCheckValueRefs().getCheckValueRefList();
                        List<CheckValueType> checkValues = application.getCheckValues().getCheckValueList();
                        List<CheckValueType> referencedValues = checkValueHelper.getStringReferencedItems(checkValues, valueRefs);
                        for (CheckValueType checkValue : referencedValues) {
                            String origCheckId = (checkValue.isSetName()) ? checkValue.getName() : "";
                            FileIdTuple checkIdTuple = new FileIdTuple(origCheckId, checkFile);
                            String newCheckId = mapNewVariables.get(checkIdTuple);
                            if (newCheckId != null) {
                                logger.debug(checkIdTuple.toString() + " == " + newCheckId);
                                checkValue.setName(newCheckId);
                            }
                        }
                    }
                }
                FileType fileType = mapCheckToFileType.get(check.getId());
                fileType.setFileName(outputFile.getName());
            }
        }
    }

    public boolean canMergeCheckDocuments(final ApplicationType application, int statusOrder) {
        boolean canMerge = true;
        if (application != null && application.isSetComplianceChecks() && application.isSetCheckLanguages()) {
            List<ComplianceCheckType> listComplianceChecks = this.getComplianceChecks(application, statusOrder);
            List<CheckLanguageType> listCheckLanguges = application.getCheckLanguages().getCheckLanguageList();
            for (ComplianceCheckType complianceCheck : listComplianceChecks) {
                if (complianceCheck.isSetCheckContent()) {
                    CheckContentType cct = complianceCheck.getCheckContent();
                    String checkLanguageId = cct.getCheckLanguageId();
                    if (checkLanguageId != null) {
                        CheckLanguageType checkLanguage = checkLanguageHelper.getItem(listCheckLanguges, checkLanguageId);
                        if (checkLanguage != null) {
                            String namespaceURI = checkLanguage.getNamespaceURI();
                            if (!namespaceURI.equals(OvalSmasherImpl.NAMESPACE_URI) && !namespaceURI.equals(OcilSmasherImpl.NAMESPACE_URI)) {
                                canMerge = false;
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            canMerge = false;
        }
        return canMerge;
    }

    public void generateXCCDF(final RTDocument rtOriginal, String applicationId, final String outputDir) throws Exception {
        RTDocument rtWorking = (RTDocument) rtOriginal.copy();
        List<String> listPlatformSpecFiles = null;
        ApplicationType application = null;
        if (rtWorking.getRT() != null && rtWorking.getRT().isSetApplications()) {
            List<ApplicationType> applicationList = rtWorking.getRT().getApplications().getApplicationList();
            application = applicationHelper.getItem(applicationList, applicationId);
            if (application == null) throw new Exception("Unable to lookup application: " + applicationId);
        }
        if (application.isSetPlatformSpecification()) {
            listPlatformSpecFiles = resetCheckFactRefFileNames(application);
        } else {
            listPlatformSpecFiles = Collections.emptyList();
        }
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("applicationId", applicationId);
        int statusOrder = -1;
        if (applicationHelper.hasRuleStatuses(application)) {
            statusOrder = this.getIntStatusOrder();
        }
        parameters.put("ruleStatusOrder", statusOrder);
        File outDir = new File(outputDir);
        if (!outDir.exists()) {
            boolean dirMade = outDir.mkdir();
            if (dirMade == false) {
                throw new Exception("Error creating not create directory: " + outputDir);
            }
        }
        if (this.isBoolMergeChecks()) {
            OvalSmasherImpl ovalSmasher = new OvalSmasherImpl();
            OcilSmasherImpl ocilSmasher = new OcilSmasherImpl();
            this.mergeOVALDocuments(ovalSmasher, rtWorking, application, outputDir, "org.mitre.oval");
            this.mergeOCILDocuments(ocilSmasher, rtWorking, application, outputDir, "gov.nist.ocil");
        }
        parameters.put("checksMerged", this.isBoolMergeChecks());
        parameters.put("dnsNamespace", this.getUserDns());
        String genXCCDFXsl = RTClientProperties.instance().getGenerateXCCDFXsl();
        File genXCCDFXslFile = new File(genXCCDFXsl);
        Reader rtXmlFile = new InputStreamReader(rtWorking.newInputStream(), "UTF8");
        File outputXccdfXml = new File(outDir, application.getName() + ".xccdf.xml");
        Writer outputXccdfWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputXccdfXml), "UTF8"));
        filesToZip.add(outputXccdfXml.getName());
        XSLProcessor.Instance().processWithCache(rtXmlFile, genXCCDFXslFile, outputXccdfWriter, parameters);
        if (this.isBoolCreateHTMLView()) {
            String xccdfToHtmlXsl = RTClientProperties.instance().getXCCDFToHTMLXsl();
            File xccdfToHtmlXslFile = new File(xccdfToHtmlXsl);
            FileInputStream fileInputStream = new FileInputStream(outputXccdfXml);
            Reader xccdfFile = new InputStreamReader(fileInputStream, "UTF8");
            File outputHtml = new File(outDir, application.getAbbr() + ".guide.html");
            FileOutputStream outputHtmlStream = new FileOutputStream(outputHtml);
            Writer outputHtmlWriter = new OutputStreamWriter(outputHtmlStream);
            filesToZip.add(outputHtml.getName());
            XSLProcessor.Instance().processWithCache(xccdfFile, xccdfToHtmlXslFile, outputHtmlWriter, parameters);
            try {
                xccdfFile.close();
                outputHtmlWriter.close();
            } catch (IOException ioe) {
                logger.warn("Couldn't close files", ioe);
            }
        }
        if (this.isBoolCreateHTMLCheckListView()) {
            String xccdfToHtmlChecklistXsl = RTClientProperties.instance().getXCCDFToHTMLChecklistXsl();
            File xccdfToHtmlChecklistXslFile = new File(xccdfToHtmlChecklistXsl);
            FileInputStream fileInputStream = new FileInputStream(outputXccdfXml);
            Reader xccdfFile = new InputStreamReader(fileInputStream, "UTF8");
            File outputHtml = new File(outDir, application.getAbbr() + ".checklist.html");
            FileOutputStream outputHtmlStream = new FileOutputStream(outputHtml);
            Writer outputHtmlWriter = new OutputStreamWriter(outputHtmlStream);
            filesToZip.add(outputHtml.getName());
            XSLProcessor.Instance().processWithCache(xccdfFile, xccdfToHtmlChecklistXslFile, outputHtmlWriter, parameters);
            try {
                xccdfFile.close();
                outputHtmlWriter.close();
            } catch (IOException ioe) {
                logger.warn("Couldn't close files", ioe);
            }
        }
        this.copyReferenceFiles(application, listPlatformSpecFiles, outDir, statusOrder);
        if (this.isBoolMergeChecks() == false) {
            this.copyCheckFiles(application, outDir, statusOrder);
        }
        this.copyCPEFiles(application, outDir);
        if (this.isBoolZipOutput()) {
            this.zipFiles(outDir, outDir + File.separator + application.getAbbr() + ".xccdf.zip");
        }
        try {
            outputXccdfWriter.close();
        } catch (IOException ioe) {
            logger.warn("Couldn't close files", ioe);
        }
        logger.debug("Completed generating xccdf.");
    }

    public void setBoolCreateHTMLCheckListView(boolean boolCreateHTMLCheckListView) {
        this.boolCreateHTMLCheckListView = boolCreateHTMLCheckListView;
    }

    public void setBoolCreateHTMLView(boolean boolCreateHTMLView) {
        this.boolCreateHTMLView = boolCreateHTMLView;
    }

    public void setBoolMergeChecks(boolean boolMergeChecks) {
        this.boolMergeChecks = boolMergeChecks;
    }

    public void setBoolZipOutput(boolean boolZipOutput) {
        this.boolZipOutput = boolZipOutput;
    }

    public boolean isBoolCreateHTMLCheckListView() {
        return boolCreateHTMLCheckListView;
    }

    public boolean isBoolCreateHTMLView() {
        return boolCreateHTMLView;
    }

    public boolean isBoolMergeChecks() {
        return boolMergeChecks;
    }

    public boolean isBoolZipOutput() {
        return boolZipOutput;
    }

    public int getIntStatusOrder() {
        return intStatusOrder;
    }

    public void setIntStatusOrder(int intStatusOrder) {
        this.intStatusOrder = intStatusOrder;
    }

    private String getUserDns() {
        UserType user = MetaManager.getAuthenticatedUser();
        String email = user.getEmail();
        int at = email.indexOf("@");
        String dns = email.substring(at + 1);
        String[] parts = dns.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (int i = parts.length - 1; i >= 0; i--) {
            sb.append(parts[i]);
            if (i != 0) sb.append('.');
        }
        logger.debug("Using reverse DNS namespace " + sb);
        return sb.toString();
    }

    /**
     * Inside the RT, the platform specification uses FileType id for CheckFactRef Href.
     * XCCDF uses the file name. Returns a list of file ids originally referenced.
     * @param application
     */
    private List<String> resetCheckFactRefFileNames(ApplicationType application) {
        List<String> listFileIds = new ArrayList<String>();
        PlatformSpecificationType ps = application.getPlatformSpecification();
        String xPath = "$this//*:check-fact-ref";
        XmlObject[] xmlObjs = ps.selectPath(xPath);
        for (XmlObject ref : xmlObjs) {
            CheckFactRefType cfr = (CheckFactRefType) ref;
            String id = cfr.getHref();
            FileType ft = null;
            try {
                if (application.isSetFiles()) {
                    ft = fileTypeHelper.getItem(application.getFiles(), id);
                }
            } catch (Exception ex) {
                logger.warn("Couldn't find FileType for id" + id, ex);
            }
            if (ft != null) {
                listFileIds.add(ft.getId());
                cfr.setHref(ft.getId() + ft.getOrigFileName());
            }
        }
        return listFileIds;
    }
}
