package org.gbif.ipt.service.manage.impl;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.IptTerm;
import org.gbif.dwc.terms.IucnTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Resource.CoreRowType;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.model.Source.SqlSource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.DeletionNotAllowedException.Reason;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.RequireManagerInterceptor;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDwca;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.task.ReportHandler;
import org.gbif.ipt.task.StatusReport;
import org.gbif.ipt.utils.ActionLogger;
import org.gbif.metadata.BasicMetadata;
import org.gbif.metadata.MetadataException;
import org.gbif.metadata.MetadataFactory;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.metadata.eml.EmlWriter;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.file.CompressionUtil.UnsupportedCompressionType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.rtf.RtfWriter2;
import com.thoughtworks.xstream.XStream;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

@Singleton
public class ResourceManagerImpl extends BaseManager implements ResourceManager, ReportHandler {

    private Map<String, Resource> resources = new HashMap<String, Resource>();

    public static final String PERSISTENCE_FILE = "resource.xml";

    private final XStream xstream = new XStream();

    private SourceManager sourceManager;

    private ExtensionManager extensionManager;

    private RegistryManager registryManager;

    private RegistrationManager registrationManager;

    private ThreadPoolExecutor executor;

    private GenerateDwcaFactory dwcaFactory;

    private Map<String, Future<Integer>> processFutures = new HashMap<String, Future<Integer>>();

    private Map<String, StatusReport> processReports = new HashMap<String, StatusReport>();

    private Eml2Rtf eml2Rtf;

    @Inject
    public ResourceManagerImpl(AppConfig cfg, DataDir dataDir, UserEmailConverter userConverter, OrganisationKeyConverter orgConverter, ExtensionRowTypeConverter extensionConverter, JdbcInfoConverter jdbcInfoConverter, SourceManager sourceManager, ExtensionManager extensionManager, RegistryManager registryManager, ConceptTermConverter conceptTermConverter, GenerateDwcaFactory dwcaFactory, PasswordConverter passwordConverter, RegistrationManager registrationManager, Eml2Rtf eml2Rtf) {
        super(cfg, dataDir);
        this.sourceManager = sourceManager;
        this.extensionManager = extensionManager;
        this.registryManager = registryManager;
        this.registrationManager = registrationManager;
        this.dwcaFactory = dwcaFactory;
        this.eml2Rtf = eml2Rtf;
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cfg.getMaxThreads());
        defineXstreamMapping(userConverter, orgConverter, extensionConverter, conceptTermConverter, jdbcInfoConverter, passwordConverter);
    }

    private void addResource(Resource res) {
        resources.put(res.getShortname().toLowerCase(), res);
    }

    public boolean cancelPublishing(String shortname, BaseAction action) throws PublicationException {
        boolean canceled = false;
        Future<Integer> f = processFutures.get(shortname);
        if (f != null) {
            canceled = f.cancel(true);
            if (canceled) {
                log.info("Publication of resource " + shortname + " canceled");
                processFutures.remove(shortname);
            } else {
                log.warn("Canceling publication of resource " + shortname + " failed");
            }
        }
        return canceled;
    }

    public synchronized void closeWriter(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    private Eml convertMetadataToEml(BasicMetadata metadata, ActionLogger alog) {
        Eml eml = null;
        if (metadata != null) {
            if (metadata instanceof Eml) {
                eml = (Eml) metadata;
            } else {
                eml = new Eml();
                eml.setTitle(metadata.getTitle());
                eml.setDescription(metadata.getDescription());
                eml.setHomeUrl(metadata.getHomepageUrl());
                eml.setLogoUrl(metadata.getLogoUrl());
                eml.setSubject(metadata.getSubject());
                eml.setPubDate(metadata.getPublished());
            }
            alog.info("Metadata imported.");
        }
        return eml;
    }

    private Eml copyMetadata(String shortname, File emlFile) throws ImportException {
        File emlFile2 = dataDir.resourceEmlFile(shortname, null);
        try {
            FileUtils.copyFile(emlFile, emlFile2);
        } catch (IOException e1) {
            log.error("Unnable to copy EML File", e1);
        }
        Eml eml = null;
        try {
            InputStream in = new FileInputStream(emlFile2);
            eml = EmlFactory.build(in);
        } catch (FileNotFoundException e) {
            eml = new Eml();
        } catch (IOException e) {
            log.error(e);
        } catch (SAXException e) {
            log.error("Invalid EML document", e);
        }
        if (eml == null) {
            throw new ImportException("Invalid EML document");
        }
        return eml;
    }

    public Resource create(String shortname, File dwca, User creator, BaseAction action) throws AlreadyExistingException, ImportException {
        ActionLogger alog = new ActionLogger(this.log, action);
        File dwcaDir = dataDir.tmpDir();
        try {
            CompressionUtil.decompressFile(dwcaDir, dwca);
            return createFromArchive(shortname, dwcaDir, creator, alog);
        } catch (UnsupportedCompressionType e) {
            return createFromEml(shortname, dwca, creator, alog);
        } catch (AlreadyExistingException e) {
            throw e;
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            alog.warn(e);
            throw new ImportException(e);
        }
    }

    public Resource create(String shortname, User creator) throws AlreadyExistingException {
        Resource res = null;
        if (shortname != null) {
            shortname = shortname.toLowerCase();
            if (resources.containsKey(shortname)) {
                throw new AlreadyExistingException();
            }
            res = new Resource();
            res.setShortname(shortname);
            res.setCreated(new Date());
            res.setCreator(creator);
            try {
                save(res);
                log.info("Created resource " + res.getShortname());
            } catch (InvalidConfigException e) {
                log.error("Error creating resource", e);
                return null;
            }
        }
        return res;
    }

    private Resource createFromArchive(String shortname, File dwca, User creator, ActionLogger alog) throws AlreadyExistingException, ImportException {
        Resource resource;
        try {
            Archive arch = ArchiveFactory.openArchive(dwca);
            resource = create(shortname, creator);
            Map<String, FileSource> sources = new HashMap<String, FileSource>();
            if (arch.getCore() != null) {
                FileSource s = importSource(alog, resource, arch.getCore());
                sources.put(arch.getCore().getLocation(), s);
                ExtensionMapping map = importMappings(alog, arch.getCore(), s);
                String coreRowType = arch.getCore().getRowType();
                if (coreRowType != null) {
                    resource.setCoreType(StringUtils.capitalize(CoreRowType.OCCURRENCE.toString().toLowerCase()));
                    if (coreRowType.equalsIgnoreCase(Constants.DWC_ROWTYPE_TAXON)) {
                        resource.setCoreType(StringUtils.capitalize(CoreRowType.CHECKLIST.toString().toLowerCase()));
                    }
                }
                resource.addMapping(map);
                for (ArchiveFile ext : arch.getExtensions()) {
                    if (sources.containsKey(ext.getLocation())) {
                        s = sources.get(ext.getLocation());
                        log.debug("Source " + s.getName() + " shared by multiple extensions");
                    } else {
                        s = importSource(alog, resource, ext);
                        sources.put(ext.getLocation(), s);
                    }
                    map = importMappings(alog, ext, s);
                    resource.addMapping(map);
                }
                Eml eml = readMetadata(resource.getShortname(), arch, alog);
                if (eml != null) {
                    resource.setEml(eml);
                }
                save(resource);
                if (StringUtils.isBlank(resource.getCoreRowType())) {
                    alog.info("manage.resource.create.success.nocore", new String[] { String.valueOf(resource.getSources().size()), String.valueOf(resource.getMappings().size()) });
                } else {
                    alog.info("manage.resource.create.success", new String[] { resource.getCoreRowType(), String.valueOf(resource.getSources().size()), String.valueOf(resource.getMappings().size()) });
                }
            } else {
                alog.warn("manage.resource.create.core.invalid");
                throw new ImportException("Darwin core archive is invalid and does not have a core mapping");
            }
        } catch (UnsupportedArchiveException e) {
            alog.warn(e.getMessage(), e);
            throw new ImportException(e);
        } catch (IOException e) {
            alog.warn(e.getMessage(), e);
            throw new ImportException(e);
        }
        return resource;
    }

    private Resource createFromEml(String shortname, File emlFile, User creator, ActionLogger alog) throws AlreadyExistingException, ImportException {
        Eml eml = copyMetadata(shortname, emlFile);
        if (eml != null) {
            Resource resource = create(shortname, creator);
            resource.setEml(eml);
            return resource;
        } else {
            alog.error("manage.resource.create.failed");
            throw new ImportException("Cant read the uploaded file");
        }
    }

    private void defineXstreamMapping(UserEmailConverter userConverter, OrganisationKeyConverter orgConverter, ExtensionRowTypeConverter extensionConverter, ConceptTermConverter conceptTermConverter, JdbcInfoConverter jdbcInfoConverter, PasswordConverter passwordConverter) {
        xstream.alias("resource", Resource.class);
        xstream.alias("user", User.class);
        xstream.alias("filesource", FileSource.class);
        xstream.alias("sqlsource", SqlSource.class);
        xstream.alias("mapping", ExtensionMapping.class);
        xstream.alias("field", PropertyMapping.class);
        xstream.omitField(Resource.class, "shortname");
        xstream.omitField(Resource.class, "eml");
        xstream.omitField(Resource.class, "type");
        xstream.omitField(FileSource.class, "file");
        xstream.registerConverter(userConverter);
        xstream.registerConverter(extensionConverter);
        xstream.registerConverter(conceptTermConverter);
        xstream.registerConverter(passwordConverter);
        xstream.addDefaultImplementation(ExtensionProperty.class, ConceptTerm.class);
        xstream.addDefaultImplementation(DwcTerm.class, ConceptTerm.class);
        xstream.addDefaultImplementation(DcTerm.class, ConceptTerm.class);
        xstream.addDefaultImplementation(GbifTerm.class, ConceptTerm.class);
        xstream.addDefaultImplementation(IucnTerm.class, ConceptTerm.class);
        xstream.addDefaultImplementation(IptTerm.class, ConceptTerm.class);
        xstream.registerConverter(orgConverter);
        xstream.registerConverter(jdbcInfoConverter);
    }

    public void delete(Resource resource) throws IOException, DeletionNotAllowedException {
        if (resource.getKey() != null) {
            try {
                registryManager.deregister(resource);
            } catch (RegistryException e) {
                log.error("Failed to deregister resource: " + e.getMessage(), e);
                throw new DeletionNotAllowedException(Reason.REGISTRY_ERROR, e.getMessage());
            }
        }
        FileUtils.forceDelete(dataDir.resourceFile(resource, ""));
        resources.remove(resource.getShortname().toLowerCase());
    }

    /**
   * @see #isLocked(String) for removing jobs from internal maps
   */
    private void generateDwca(Resource resource, ActionLogger alog) {
        GenerateDwca worker = dwcaFactory.create(resource, this);
        Future<Integer> f = executor.submit(worker);
        processFutures.put(resource.getShortname(), f);
        worker.report();
    }

    public Resource get(String shortname) {
        if (shortname == null) {
            return null;
        }
        return resources.get(shortname.toLowerCase());
    }

    /**
   * Returns the size of the DwC-A file using the dataDir.
   */
    public long getDwcaSize(Resource resource) {
        File data = dataDir.resourceDwcaFile(resource.getShortname());
        return data.length();
    }

    private int getEmlHash(Resource resource, Eml eml) {
        return eml.hashCode();
    }

    /**
   * Returns the size of the EML file using the dataDir.
   */
    public long getEmlSize(Resource resource) {
        File data = dataDir.resourceEmlFile(resource.getShortname(), resource.getEmlVersion());
        return data.length();
    }

    public URL getResourceLink(String shortname) {
        URL url = null;
        try {
            url = new URL(cfg.getBaseUrl() + "/resource.do?id=" + shortname);
        } catch (MalformedURLException e) {
            log.error(e);
        }
        return url;
    }

    /**
   * Returns the size of the RTF file using the dataDir.
   */
    public long getRtfSize(Resource resource) {
        File data = dataDir.resourceRtfFile(resource.getShortname());
        return data.length();
    }

    private ExtensionMapping importMappings(ActionLogger alog, ArchiveFile af, Source source) {
        ExtensionMapping map = new ExtensionMapping();
        map.setSource(source);
        Extension ext = extensionManager.get(af.getRowType());
        if (ext == null) {
            alog.warn("manage.resource.create.rowType.null", new String[] { af.getRowType() });
            return null;
        }
        map.setExtension(ext);
        map.setIdColumn(af.getId().getIndex());
        Set<PropertyMapping> fields = new HashSet<PropertyMapping>();
        for (ArchiveField f : af.getFields().values()) {
            if (ext.hasProperty(f.getTerm())) {
                fields.add(new PropertyMapping(f));
            } else {
                alog.warn("manage.resource.create.mapping.concept.skip", new String[] { f.getTerm().qualifiedName(), ext.getRowType() });
            }
        }
        map.setFields(fields);
        return map;
    }

    private FileSource importSource(ActionLogger alog, Resource config, ArchiveFile af) throws ImportException {
        File extFile = af.getLocationFile();
        FileSource s = sourceManager.add(config, extFile, af.getLocation());
        SourceManagerImpl.copyArchiveFileProperties(af, s);
        if (s.getIgnoreHeaderLines() != 1) {
            log.info("Adjusting row count to " + (s.getRows() + 1 - s.getIgnoreHeaderLines()) + " from " + s.getRows() + " since header count is declared as " + s.getIgnoreHeaderLines());
        }
        s.setRows(s.getRows() + 1 - s.getIgnoreHeaderLines());
        return s;
    }

    public boolean isEmlExisting(String shortName) {
        File emlFile = dataDir.resourceEmlFile(shortName, null);
        return emlFile.exists();
    }

    /**
   * Checks if a resource is locked due some background processing.
   * While doing so it checks the known futures for completion.
   * If completed the resource is updated with the status messages and the lock is removed.
   */
    public boolean isLocked(String shortname) {
        if (processFutures.containsKey(shortname)) {
            Future<Integer> f = processFutures.get(shortname);
            if (f.isDone()) {
                try {
                    Integer coreRecords = f.get();
                    Resource res = get(shortname);
                    res.setRecordsPublished(coreRecords);
                    save(res);
                    return false;
                } catch (InterruptedException e) {
                    log.info("Process interrupted for resource " + shortname);
                } catch (CancellationException e) {
                    log.info("Process canceled for resource " + shortname);
                } catch (ExecutionException e) {
                    log.error("Process for resource " + shortname + " aborted due to error: " + e.getMessage());
                } finally {
                    processFutures.remove(shortname);
                }
            }
            return true;
        }
        return false;
    }

    public boolean isRtfExisting(String shortName) {
        File rtfFile = dataDir.resourceRtfFile(shortName);
        return rtfFile.exists();
    }

    public List<Resource> latest(int startPage, int pageSize) {
        List<Resource> resourceList = new ArrayList<Resource>();
        for (Resource resource : resources.values()) {
            if (!resource.getStatus().equals(PublicationStatus.PRIVATE)) {
                resourceList.add(resource);
            }
        }
        Collections.sort(resourceList, new Comparator<Resource>() {

            public int compare(Resource r1, Resource r2) {
                if (r1 == null || r1.getModified() == null) {
                    return 1;
                }
                if (r2 == null || r2.getModified() == null) {
                    return -1;
                }
                if (r1.getModified().before(r2.getModified())) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return resourceList;
    }

    public List<Resource> list() {
        return new ArrayList<Resource>(resources.values());
    }

    public List<Resource> list(PublicationStatus status) {
        List<Resource> result = new ArrayList<Resource>();
        for (Resource r : resources.values()) {
            if (r.getStatus() == status) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Resource> list(User user) {
        List<Resource> result = new ArrayList<Resource>();
        for (Resource res : resources.values()) {
            if (RequireManagerInterceptor.isAuthorized(user, res)) {
                result.add(res);
            }
        }
        return result;
    }

    public int load() {
        File resourcesDir = dataDir.dataFile(DataDir.RESOURCES_DIR);
        resources.clear();
        int counter = 0;
        if (resourcesDir != null) {
            File[] resources = resourcesDir.listFiles();
            if (resources != null) {
                for (File resourceDir : resources) {
                    if (resourceDir.isDirectory()) {
                        try {
                            addResource(loadFromDir(resourceDir));
                            counter++;
                        } catch (InvalidConfigException e) {
                            log.error("Cant load resource " + resourceDir.getName(), e);
                        }
                    }
                }
                log.info("Loaded " + counter + " resources into memory alltogether.");
            } else {
                log.info("Data directory does not hold a resources directory: " + dataDir.dataFile(""));
            }
        } else {
            log.info("Data directory does not hold a resources directory: " + dataDir.dataFile(""));
        }
        return counter;
    }

    private Eml loadEml(Resource resource) {
        File emlFile = dataDir.resourceEmlFile(resource.getShortname(), null);
        Eml eml = null;
        try {
            InputStream in = new FileInputStream(emlFile);
            eml = EmlFactory.build(in);
        } catch (FileNotFoundException e) {
            eml = new Eml();
        } catch (IOException e) {
            log.error(e);
        } catch (SAXException e) {
            log.error("Invalid EML document", e);
            eml = new Eml();
        } catch (Exception e) {
            eml = new Eml();
        }
        resource.setEml(eml);
        syncEmlWithResource(resource);
        return eml;
    }

    /**
   * Reads a complete resource configuration (resource config & eml) from the resource config folder
   * and returns the Resource instance for the internal in memory cache.
   */
    private Resource loadFromDir(File resourceDir) throws InvalidConfigException {
        if (resourceDir.exists()) {
            String shortname = resourceDir.getName();
            try {
                File cfgFile = dataDir.resourceFile(shortname, PERSISTENCE_FILE);
                InputStream input = new FileInputStream(cfgFile);
                Resource resource = (Resource) xstream.fromXML(input);
                resource.getManagers().remove(null);
                resource.setShortname(shortname);
                for (Source src : resource.getSources()) {
                    src.setResource(resource);
                    if (src instanceof FileSource) {
                        ((FileSource) src).setFile(dataDir.sourceFile(resource, src));
                    }
                }
                loadEml(resource);
                log.debug("Read resource configuration for " + shortname);
                return resource;
            } catch (FileNotFoundException e) {
                log.error("Cannot read resource configuration for " + shortname, e);
                throw new InvalidConfigException(TYPE.RESOURCE_CONFIG, "Cannot read resource configuration for " + shortname + ": " + e.getMessage());
            }
        }
        return null;
    }

    public boolean publish(Resource resource, BaseAction action) throws PublicationException {
        ActionLogger alog = new ActionLogger(this.log, action);
        if (isLocked(resource.getShortname())) {
            throw new PublicationException(PublicationException.TYPE.LOCKED, "Resource " + resource.getShortname() + " is currently locked by another process");
        }
        publishMetadata(resource, action);
        boolean dwca = false;
        if (resource.hasMappedData()) {
            generateDwca(resource, alog);
            dwca = true;
            if (resource.isRegistered()) {
                registryManager.updateResource(resource, registrationManager.getIpt());
            }
        } else {
            resource.setRecordsPublished(0);
        }
        resource.setLastPublished(new Date());
        save(resource);
        return dwca;
    }

    public void publishMetadata(Resource resource, BaseAction action) throws PublicationException {
        ActionLogger alog = new ActionLogger(this.log, action);
        if (isLocked(resource.getShortname())) {
            throw new PublicationException(PublicationException.TYPE.LOCKED, "Resource " + resource.getShortname() + " is currently locked by another process");
        }
        int version = resource.getEmlVersion();
        version++;
        resource.setEmlVersion(version);
        saveEml(resource);
        File trunkFile = dataDir.resourceEmlFile(resource.getShortname(), null);
        File versionedFile = dataDir.resourceEmlFile(resource.getShortname(), version);
        try {
            FileUtils.copyFile(trunkFile, versionedFile);
        } catch (IOException e) {
            alog.error("Can't publish resource " + resource.getShortname(), e);
            throw new PublicationException(PublicationException.TYPE.EML, "Can't publish eml file for resource " + resource.getShortname(), e);
        }
        publishRtf(resource, action);
        File trunkRtfFile = dataDir.resourceRtfFile(resource.getShortname());
        File versionedRtfFile = dataDir.resourceRtfFile(resource.getShortname(), version);
        try {
            FileUtils.copyFile(trunkRtfFile, versionedRtfFile);
        } catch (IOException e) {
            alog.error("Can't publish resource " + resource.getShortname() + "as RTF", e);
            throw new PublicationException(PublicationException.TYPE.EML, "Can't publish rtf file for resource " + resource.getShortname(), e);
        }
    }

    private void publishRtf(Resource resource, BaseAction action) {
        ActionLogger alog = new ActionLogger(this.log, action);
        Document doc = new Document();
        File rtfFile = dataDir.resourceRtfFile(resource.getShortname());
        try {
            OutputStream out = new FileOutputStream(rtfFile);
            RtfWriter2.getInstance(doc, out);
            eml2Rtf.writeEmlIntoRtf(doc, resource, action);
            out.close();
        } catch (FileNotFoundException e) {
            alog.error("Cant find rtf file to write metadata to: " + rtfFile.getAbsolutePath(), e);
        } catch (DocumentException e) {
            alog.error("RTF DocumentException while writing to file " + rtfFile.getAbsolutePath(), e);
        } catch (IOException e) {
            alog.error("Cant write to rtf file " + rtfFile.getAbsolutePath(), e);
        }
    }

    private Eml readMetadata(File file, ActionLogger alog) {
        MetadataFactory fact = new MetadataFactory();
        try {
            return convertMetadataToEml(fact.read(file), alog);
        } catch (MetadataException e) {
        }
        return null;
    }

    private Eml readMetadata(String shortname, Archive archive, ActionLogger alog) {
        Eml eml;
        File emlFile = archive.getMetadataLocationFile();
        try {
            if (emlFile == null || !emlFile.exists()) {
                emlFile = new File(archive.getLocation(), "eml.xml");
            }
            if (emlFile.exists()) {
                eml = copyMetadata(shortname, emlFile);
                alog.info("manage.resource.read.eml.metadata");
                return eml;
            } else {
                log.warn("Cant find any eml metadata to import");
            }
        } catch (ImportException e) {
            String msg = "Cant read basic archive metadata: " + e.getMessage();
            log.warn(msg);
            alog.warn(msg);
            return null;
        } catch (Exception e) {
            log.warn("Cant read archive eml metadata", e);
        }
        try {
            eml = convertMetadataToEml(archive.getMetadata(), alog);
            alog.info("manage.resource.read.basic.metadata");
            return eml;
        } catch (Exception e) {
            log.warn("Cant read basic archive metadata: " + e.getMessage());
        }
        alog.warn("manage.resource.read.problem");
        return null;
    }

    public void register(Resource resource, Organisation organisation, Ipt ipt) throws RegistryException {
        if (PublicationStatus.REGISTERED != resource.getStatus()) {
            UUID key = registryManager.register(resource, organisation, ipt);
            if (key == null) {
                throw new RegistryException(RegistryException.TYPE.MISSING_METADATA, "No key returned for registered resoruce.");
            }
            resource.setStatus(PublicationStatus.REGISTERED);
            save(resource);
        }
    }

    public synchronized void report(String shortname, StatusReport report) {
        processReports.put(shortname, report);
    }

    public synchronized void save(Resource resource) throws InvalidConfigException {
        File cfgFile = dataDir.resourceFile(resource, PERSISTENCE_FILE);
        Writer writer = null;
        try {
            FileUtils.forceMkdir(cfgFile.getParentFile());
            writer = org.gbif.ipt.utils.FileUtils.startNewUtf8File(cfgFile);
            xstream.toXML(resource, writer);
            addResource(resource);
        } catch (IOException e) {
            log.error(e);
            throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Can't write mapping configuration");
        } finally {
            if (writer != null) {
                closeWriter(writer);
            }
            System.gc();
        }
    }

    public synchronized void saveEml(Resource resource) throws InvalidConfigException {
        syncEmlWithResource(resource);
        resource.setModified(new Date());
        File emlFile = dataDir.resourceEmlFile(resource.getShortname(), null);
        try {
            EmlWriter.writeEmlFile(emlFile, resource.getEml());
            log.debug("Updated EML file for " + resource);
        } catch (IOException e) {
            log.error(e);
            throw new InvalidConfigException(TYPE.CONFIG_WRITE, "IO exception when writing eml for " + resource);
        } catch (TemplateException e) {
            log.error("EML template exception", e);
            throw new InvalidConfigException(TYPE.EML, "EML template exception when writing eml for " + resource + ": " + e.getMessage());
        }
    }

    public StatusReport status(String shortname) {
        isLocked(shortname);
        return processReports.get(shortname);
    }

    private void syncEmlWithResource(Resource resource) {
        resource.getEml().setEmlVersion(resource.getEmlVersion());
        if (resource.getKey() != null) {
            resource.getEml().setGuid(resource.getKey().toString());
        } else {
            resource.getEml().setGuid(getResourceLink(resource.getShortname()).toString());
        }
    }

    public void updateDwcaEml(Resource resource, BaseAction action) throws PublicationException {
        ActionLogger alog = new ActionLogger(this.log, action);
        if (isLocked(resource.getShortname())) {
            throw new PublicationException(PublicationException.TYPE.LOCKED, "Resource " + resource.getShortname() + " is currently locked by another process");
        }
        if (!resource.hasPublishedData()) {
            throw new PublicationException(PublicationException.TYPE.DWCA, "Resource " + resource.getShortname() + " has no published data - can't update a non-existent dwca.");
        }
        try {
            File dwcaFolder = dataDir.tmpDir();
            if (log.isDebugEnabled()) {
                log.debug("Using tmp dir [" + dwcaFolder.getAbsolutePath() + "]");
            }
            File dwcaFile = dataDir.resourceDwcaFile(resource.getShortname());
            if (log.isDebugEnabled()) {
                log.debug("Using dwca file [" + dwcaFile.getAbsolutePath() + "]");
            }
            File emlFile = dataDir.resourceEmlFile(resource.getShortname(), resource.getEmlVersion());
            if (log.isDebugEnabled()) {
                log.debug("Using eml file [" + emlFile.getAbsolutePath() + "]");
            }
            CompressionUtil.unzipFile(dwcaFolder, dwcaFile);
            if (log.isDebugEnabled()) {
                log.debug("Copying new eml file [" + emlFile.getAbsolutePath() + "] to [" + dwcaFolder.getAbsolutePath() + "] as eml.xml");
            }
            FileUtils.copyFile(emlFile, new File(dwcaFolder, "eml.xml"));
            File zip = dataDir.tmpFile("dwca", ".zip");
            CompressionUtil.zipDir(dwcaFolder, zip);
            dwcaFile.delete();
            FileUtils.moveFile(zip, dwcaFile);
        } catch (IOException e) {
            alog.error("Can't update dwca for resource " + resource.getShortname(), e);
            throw new PublicationException(PublicationException.TYPE.DWCA, "Could not process dwca file for resource [" + resource.getShortname() + "]");
        }
    }

    public void updateRegistration(Resource resource, Ipt ipt) throws InvalidConfigException {
        if (PublicationStatus.REGISTERED == resource.getStatus()) {
            log.debug("Updating resource with key: " + resource.getKey().toString());
            registryManager.updateResource(resource, ipt);
        }
    }

    public void visibilityToPrivate(Resource resource) throws InvalidConfigException {
        if (PublicationStatus.REGISTERED == resource.getStatus()) {
            throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED, "The resource is already registered with GBIF");
        } else if (PublicationStatus.PUBLIC == resource.getStatus()) {
            resource.setStatus(PublicationStatus.PRIVATE);
            save(resource);
        }
    }

    public void visibilityToPublic(Resource resource) throws InvalidConfigException {
        if (PublicationStatus.REGISTERED == resource.getStatus()) {
            throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED, "The resource is already registered with GBIF");
        } else if (PublicationStatus.PRIVATE == resource.getStatus()) {
            resource.setStatus(PublicationStatus.PUBLIC);
            save(resource);
        }
    }
}
