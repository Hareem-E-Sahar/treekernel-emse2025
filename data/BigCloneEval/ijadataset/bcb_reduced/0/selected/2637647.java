package org.in4ama.documentengine.compile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.in4ama.datasourcemanager.cfg.DataConfigurationMgr;
import org.in4ama.documentautomator.DocumentMgr;
import org.in4ama.documentautomator.DocumentMgrConfigurationMgr;
import org.in4ama.documentautomator.util.XmlHelper;
import org.in4ama.documentengine.compile.acroform.AcroFormDocumentCompiler;
import org.in4ama.documentengine.compile.acroform.OOAcroFormDocumentCompiler;
import org.in4ama.documentengine.compile.compound.CompoundDocumentCompiler;
import org.in4ama.documentengine.compile.email.EmailDocumentCompiler;
import org.in4ama.documentengine.compile.odt.OdtEmailDocumentCompiler;
import org.in4ama.documentengine.compile.odt.OdtLetterCompiler;
import org.in4ama.documentengine.compile.odt.OdtLetterFragmentCompiler;
import org.in4ama.documentengine.compile.odt.OdtOOAcroFormDocumentCompiler;
import org.in4ama.documentengine.compile.odt.OdtTableTemplateCompiler;
import org.in4ama.documentengine.compile.pack.PackCompiler;
import org.in4ama.documentengine.compile.xslfo.XslFoLetterCompiler;
import org.in4ama.documentengine.compile.xslfo.XslFoLetterFragmentCompiler;
import org.in4ama.documentengine.compile.xslfo.templates.XslFoTableTemplateCompiler;
import org.in4ama.documentengine.exception.MailshotException;
import org.in4ama.documentengine.exception.ProjectException;
import org.in4ama.documentengine.generator.InforamaContext;
import org.in4ama.documentengine.generator.ProjectContext;
import org.in4ama.documentengine.mailshots.MailshotScriptMgr;
import org.in4ama.documentengine.project.Project;
import org.in4ama.documentengine.project.ProjectMgr;
import org.in4ama.documentengine.project.cfg.ProjectConfigurationMgr;
import org.in4ama.documentengine.project.cfg.SettingsConfigurationMgr;
import org.in4ama.documentengine.project.cfg.TestDataConfigurationMgr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/** Utility class for compressing a project to the ZIP output stream. */
public class ProjectCompiler {

    public static final String PROJECT_FILE = "inforama.xml";

    public static final String PROJECTCONFIG_FILE = "projectconfig.xml";

    public static final String VARIABLES_FILE = "variables.xml";

    public static final String DATASOURCES_FILE = "datasourceset.cfg.xml";

    public static final String DATASETS_FILE = "datasets.xml";

    public static final String DOCUMENTS_FILE = "documents.cfg.xml";

    public static final String TESTDATA_FILE = "testdata.xml";

    public static final String MAILSHOTS_DIR = "mailshots";

    public static final String MAILSHOT_EXT = ".xml";

    public static Map<String, DocumentCompiler> documentCompilersMap = buildDocumentCompilers();

    /** Builds the document compiler objects. */
    public static Map<String, DocumentCompiler> buildDocumentCompilers() {
        Map<String, DocumentCompiler> map = new HashMap<String, DocumentCompiler>();
        DocumentCompiler compound = new CompoundDocumentCompiler();
        map.put(compound.getType(), compound);
        DocumentCompiler pack = new PackCompiler();
        map.put(pack.getType(), pack);
        DocumentCompiler odtLetter = new OdtLetterCompiler();
        map.put(odtLetter.getType(), odtLetter);
        DocumentCompiler odtEmailDocumentCompiler = new OdtEmailDocumentCompiler();
        map.put(odtEmailDocumentCompiler.getType(), odtEmailDocumentCompiler);
        DocumentCompiler odtLetterFragmentCompiler = new OdtLetterFragmentCompiler();
        map.put(odtLetterFragmentCompiler.getType(), odtLetterFragmentCompiler);
        DocumentCompiler odtOOAcroFormDocumentCompiler = new OdtOOAcroFormDocumentCompiler();
        map.put(odtOOAcroFormDocumentCompiler.getType(), odtOOAcroFormDocumentCompiler);
        DocumentCompiler odtTableTemplateCompiler = new OdtTableTemplateCompiler();
        map.put(odtTableTemplateCompiler.getType(), odtTableTemplateCompiler);
        DocumentCompiler xslFoLetterCompiler = new XslFoLetterCompiler();
        map.put(xslFoLetterCompiler.getType(), xslFoLetterCompiler);
        DocumentCompiler xslFoLetterFragmentCompiler = new XslFoLetterFragmentCompiler();
        map.put(xslFoLetterFragmentCompiler.getType(), xslFoLetterFragmentCompiler);
        DocumentCompiler xslFoTableTemplateCompiler = new XslFoTableTemplateCompiler();
        map.put(xslFoTableTemplateCompiler.getType(), xslFoTableTemplateCompiler);
        DocumentCompiler emailDocumentCompiler = new EmailDocumentCompiler();
        map.put(emailDocumentCompiler.getType(), emailDocumentCompiler);
        DocumentCompiler acroFormDocumentCompiler = new AcroFormDocumentCompiler();
        map.put(acroFormDocumentCompiler.getType(), acroFormDocumentCompiler);
        DocumentCompiler ooAcroFormDocumentCompiler = new OOAcroFormDocumentCompiler();
        map.put(ooAcroFormDocumentCompiler.getType(), ooAcroFormDocumentCompiler);
        return map;
    }

    /** Creates a new 'abstract' project context from the specified input stream. */
    public static void importProject(InforamaContext inforamaContext, ProjectMgr projectMgr, File file, String projectName) throws CompileException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (Exception ex) {
            String msg = "Unable to import the project.";
            throw new CompileException(msg, ex);
        }
        importProject(inforamaContext, projectMgr, in, projectName, null);
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception ex) {
            String msg = "Unable to close the file " + "input stream while importing the project.";
            throw new CompileException(msg, ex);
        }
    }

    /** Creates a new 'abstract' project context from the specified ZIP file. 
	 * @throws CompileException */
    public static void importProject(InforamaContext inforamaContext, ProjectMgr projectMgr, InputStream in, String projectName, CompileListener listener) throws CompileException {
        try {
            ProjectContext projectContext = createProjectStub(inforamaContext, projectMgr, projectName);
            Project project = projectContext.getProject();
            ProjectEntries projectEntries = new ProjectEntries(in, listener);
            importConfigurationFiles(project, projectEntries);
            importMailshots(project, projectEntries);
            List<org.in4ama.documentautomator.documents.Document> documents = importDocuments(projectContext, projectEntries);
            projectMgr.saveProject(project);
            projectMgr.saveMailshotScripts(project);
            projectContext.getDefaultDocumentMgr().saveDocuments(documents);
        } catch (Exception ex) {
            String msg = "Unable to import the project.";
            throw new CompileException(msg, ex);
        }
    }

    /** Creates a stub of the project context 
	 * @throws ProjectException */
    private static ProjectContext createProjectStub(InforamaContext inforamaContext, ProjectMgr projectMgr, String name) throws ProjectException {
        Project project = new Project(name);
        projectMgr.setDefaultDocumentMgrConfiguration(project);
        return new ProjectContext(project, inforamaContext);
    }

    /** Retrieves the name of the project from the specified ZIP file. */
    private static String toProjectName(File file) {
        String name = file.getName();
        if (name.toLowerCase().endsWith(".zip")) {
            name = name.substring(0, name.length() - 4);
        }
        return name;
    }

    /** Imports all configuration files of the specified project. 
	 * @throws IOException */
    private static void importConfigurationFiles(Project project, ProjectEntries projectEntries) throws Exception {
        ProjectEntry projectFileEntry = projectEntries.getEntry(PROJECT_FILE);
        if (projectFileEntry != null) {
            Document doc = projectFileEntry.getContentDoc();
            project.getProjectConfigurationMgr().buildProjectConfig(doc);
        }
        ProjectEntry projectConfigFileEntry = projectEntries.getEntry(PROJECTCONFIG_FILE);
        if (projectConfigFileEntry != null) {
            Document doc = projectConfigFileEntry.getContentDoc();
            project.getSettingsConfigurationMgr().buildSettingsConfig(doc);
        }
        ProjectEntry variablesFileEntry = projectEntries.getEntry(VARIABLES_FILE);
        if (variablesFileEntry != null) {
            Document doc = variablesFileEntry.getContentDoc();
            project.getDataConfigurationMgr().buildVariables(doc);
        }
        ProjectEntry dataSourceFileEntry = projectEntries.getEntry(DATASOURCES_FILE);
        if (dataSourceFileEntry != null) {
            Document doc = dataSourceFileEntry.getContentDoc();
            project.getDataConfigurationMgr().buildDataSourcesConfig(doc);
        }
        ProjectEntry dataSetsFileEntry = projectEntries.getEntry(DATASETS_FILE);
        if (dataSetsFileEntry != null) {
            Document doc = dataSetsFileEntry.getContentDoc();
            project.getDataConfigurationMgr().buildDataSetsConfig(doc);
        }
        ProjectEntry testDataFileEntry = projectEntries.getEntry(TESTDATA_FILE);
        if (testDataFileEntry != null) {
            Document doc = testDataFileEntry.getContentDoc();
            project.getTestDataConfigurationMgr().buildParametersConfig(doc);
        }
    }

    /** Imports mailshot scripts of the specified project. 
	 * @throws Exception */
    private static void importMailshots(Project project, ProjectEntries projectEntries) throws Exception {
        MailshotScriptMgr mailshotScriptMgr = project.getMailshotScriptMgr();
        Collection<ProjectEntry> entries = projectEntries.getEntries(MAILSHOTS_DIR);
        if (entries != null) {
            for (ProjectEntry entry : entries) {
                Document doc = entry.getContentDoc();
                mailshotScriptMgr.putDocumentScript(doc);
            }
        }
    }

    /** Imports all documents of the specified project. 
	 * @throws CompileException */
    private static List<org.in4ama.documentautomator.documents.Document> importDocuments(ProjectContext context, ProjectEntries entries) throws CompileException {
        List<org.in4ama.documentautomator.documents.Document> documents = new LinkedList<org.in4ama.documentautomator.documents.Document>();
        Collection<DocumentCompiler> compilers = getDocumentCompilers();
        for (DocumentCompiler compiler : compilers) {
            documents.addAll(compiler.extractDocuments(entries, context));
        }
        return documents;
    }

    /** Compiles the project and stores it into the specified file. */
    public static void exportProject(ProjectContext projectContext, File file) throws CompileException {
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            exportConfigurationFiles(projectContext, out);
            exportMailshots(projectContext, out);
            exportDocuments(projectContext, out);
            out.flush();
            out.close();
        } catch (Exception ex) {
            String msg = "Unable to export the project.";
            throw new CompileException(msg, ex);
        }
    }

    /** Saves all documents from the given project context. */
    private static void exportDocuments(ProjectContext projectContext, ZipOutputStream out) throws CompileException, ProjectException {
        DocumentMgr documentMgr = projectContext.getDocumentMgrRepository().getDefaultDocumentMgr();
        Collection<DocumentCompiler> documentCompilers = getDocumentCompilers();
        for (DocumentCompiler documentCompiler : documentCompilers) {
            String type = documentCompiler.getType();
            documentCompiler.addDocuments(documentMgr.loadDocuments(type), out);
        }
    }

    /** Returns a collection of available document compilers. */
    private static Collection<DocumentCompiler> getDocumentCompilers() {
        return documentCompilersMap.values();
    }

    /** Stores mailshot scripts to the specified ZIP output stream. */
    private static void exportMailshots(ProjectContext projectContext, ZipOutputStream out) throws CompileException, MailshotException {
        Project project = projectContext.getProject();
        MailshotScriptMgr mailshotScriptMgr = project.getMailshotScriptMgr();
        Collection<String> scriptNames = mailshotScriptMgr.getScriptNames();
        for (String scriptName : scriptNames) {
            Document scriptDoc = mailshotScriptMgr.getDocumentScript(scriptName);
            if (scriptDoc != null) {
                String fileName = MAILSHOTS_DIR + "/" + scriptName + MAILSHOT_EXT;
                addDocument(out, fileName, scriptDoc);
            }
        }
    }

    /** Stores all project configuration files 
	 * to the specified ZIP output stream. */
    private static void exportConfigurationFiles(ProjectContext projectContext, ZipOutputStream out) throws CompileException, ProjectException {
        Project project = projectContext.getProject();
        ProjectConfigurationMgr projectConfigurationMgr = project.getProjectConfigurationMgr();
        Document projectConfigDoc = projectConfigurationMgr.createProjectConfigDoc();
        if (projectConfigDoc != null) {
            addDocument(out, PROJECT_FILE, projectConfigDoc);
        }
        SettingsConfigurationMgr settingsConfigurationMgr = project.getSettingsConfigurationMgr();
        Document settingsConfigDoc = settingsConfigurationMgr.createSettingsConfigDoc();
        if (settingsConfigDoc != null) {
            addDocument(out, PROJECTCONFIG_FILE, settingsConfigDoc);
        }
        DataConfigurationMgr dataSourceConfigurationMgr = project.getDataConfigurationMgr();
        Document variablesConfigDoc = dataSourceConfigurationMgr.createVariablesConfigDoc();
        if (variablesConfigDoc != null) {
            addDocument(out, VARIABLES_FILE, variablesConfigDoc);
        }
        Document dataSourcesConfigDoc = dataSourceConfigurationMgr.createDataSourcesConfigDoc();
        if (dataSourcesConfigDoc != null) {
            addDocument(out, DATASOURCES_FILE, dataSourcesConfigDoc);
        }
        Document dataSetsConfigDoc = dataSourceConfigurationMgr.createDataSetsConfigDoc();
        if (dataSetsConfigDoc != null) {
            addDocument(out, DATASETS_FILE, dataSetsConfigDoc);
        }
        DocumentMgrConfigurationMgr documentMgrConfigurationMgr = project.getDocumentMgrConfigurationMgr();
        Document documentMgrConfigDoc = documentMgrConfigurationMgr.createDocumentMgrConfigDoc();
        if (documentMgrConfigDoc != null) {
            addDocument(out, DOCUMENTS_FILE, documentMgrConfigDoc);
        }
        TestDataConfigurationMgr testDataConfigurationMgr = project.getTestDataConfigurationMgr();
        Document parametersConfigDoc = testDataConfigurationMgr.createParametersConfigDoc();
        if (parametersConfigDoc != null) {
            addDocument(out, TESTDATA_FILE, parametersConfigDoc);
        }
    }

    /** Adds the specified XML node to the ZIP output stream. */
    private static void addDocument(ZipOutputStream out, String filePath, Node node) throws CompileException {
        InputStream content = null;
        try {
            content = XmlHelper.convertToInputStream(node);
        } catch (Exception ex) {
            String msg = "Unable to compile the project.";
            throw new CompileException(msg, ex);
        }
        addFile(out, filePath, content);
    }

    /** Adds the specified input stream to the ZIP output stream. */
    private static void addFile(ZipOutputStream out, String filePath, InputStream content) throws CompileException {
        try {
            byte[] buf = new byte[1024];
            out.putNextEntry(new ZipEntry(filePath));
            for (int len = 0; (len = content.read(buf)) > 0; ) {
                out.write(buf, 0, len);
            }
        } catch (Exception ex) {
            String msg = "Unable to add an entry to the ZIP file.";
            throw new CompileException(msg, ex);
        }
    }
}
