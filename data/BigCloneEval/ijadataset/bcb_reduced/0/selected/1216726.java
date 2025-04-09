package de.uni_hamburg.golem.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.uni_hamburg.golem.model.GAbstractRecord;
import de.uni_hamburg.golem.model.GDevice;
import de.uni_hamburg.golem.model.GEnterprisePackage;
import de.uni_hamburg.golem.model.GGroup;
import de.uni_hamburg.golem.model.GInstitution;
import de.uni_hamburg.golem.model.GMembership;
import de.uni_hamburg.golem.model.GMessage;
import de.uni_hamburg.golem.model.GPerson;
import de.uni_hamburg.golem.model.GWriterDevice;
import de.uni_hamburg.golem.model.PackageFactoryBean;

public class BatchProcessor {

    public static final String STAR = "*";

    public static final String TARGET_SEPARATOR = ";";

    private Log log = LogFactory.getLog(this.getClass());

    private GRepository repository;

    private PackageFactoryBean packager;

    private SecurityController security;

    private String dumpDirectory;

    private String loadDirectory;

    private boolean tolowercase = false;

    /**
	 * @return the repository
	 */
    public GRepository getRepository() {
        return repository;
    }

    /**
	 * @param repository
	 *            the repository to set
	 */
    public void setRepository(GRepository repository) {
        this.repository = repository;
    }

    /**
	 * @return the packager
	 */
    public PackageFactoryBean getPackager() {
        return packager;
    }

    /**
	 * @param packager
	 *            the packager to set
	 */
    public void setPackager(PackageFactoryBean packager) {
        this.packager = packager;
    }

    /**
	 * Check Package Content Authorization.
	 *
	 * @param user
	 * @param pkg
	 * @return
	 */
    public GEnterprisePackage authorizeBatch(GPerson user, GEnterprisePackage pkg) {
        GEnterprisePackage clonePkg = new GEnterprisePackage();
        try {
            for (Entry<String, Object> entry : pkg.getMapping().entrySet()) {
                try {
                    clonePkg.setVar(entry.getKey(), entry.getValue());
                } catch (Exception e1) {
                    log.warn("Error while cloning package", e1);
                }
            }
        } catch (Exception e1) {
            log.warn("Error while cloning package", e1);
        }
        GInstitution institution = null;
        HashSet<String> itargetset = new HashSet<String>();
        try {
            institution = repository.getInstitution(user.getInstitution());
            for (String s : institution.getTargets().split("[ \t,;:]+")) {
                itargetset.add(s);
            }
        } catch (Exception e) {
            log.warn("Get Institution Error: \n" + pkg.getDatasource());
            clonePkg.add(new GMessage(GMessage.CODE_ERROR, GMessage.TARGET_RETURN, GMessage.SOURCE_GOLEM, pkg.getID(), "invalid id format"));
            return clonePkg;
        }
        boolean authorized = security.isInstitutionalAdmin(user, institution) || security.isSysAdmin(user);
        if (!authorized) {
            clonePkg.add(new GMessage(GMessage.CODE_ERROR, GMessage.TARGET_RETURN, GMessage.SOURCE_GOLEM, pkg.getID(), "User ist not authorized"));
            return clonePkg;
        }
        try {
            List<GDevice> devices = repository.getDevices(pkg);
            StringBuffer targets = new StringBuffer();
            for (GDevice device : devices) {
                if (itargetset.contains(device.getID())) {
                    targets.append(device.getID() + TARGET_SEPARATOR);
                }
            }
            pkg.setTarget(targets.toString().trim());
        } catch (Exception e) {
            log.warn("Get Targets Error: \n" + pkg.getDatasource());
            clonePkg.add(new GMessage(GMessage.CODE_ERROR, GMessage.TARGET_RETURN, GMessage.SOURCE_GOLEM, pkg.getID(), "invalid target format!?"));
            return clonePkg;
        }
        if (!security.isSysAdmin(user)) {
            pkg.apply(GEnterprisePackage.CTXPERSON, "INSTITUTION", institution.getID());
            pkg.apply(GEnterprisePackage.CTXGROUP, "INSTITUTION", institution.getID());
        }
        ArrayList<GAbstractRecord> records = pkg.getRecords();
        for (int i = 0; i < records.size(); i++) {
            GAbstractRecord record = records.get(i);
            if (record.getClass().equals(GPerson.class)) {
                GPerson person = (GPerson) record;
                if (tolowercase) {
                    person.setUserid(person.getUserid().toLowerCase());
                }
                if (person.getOperation() <= GAbstractRecord.ADD && !security.isSysAdmin(user)) {
                    person.setManagedBy(user.getUserid());
                }
                if (person.getID().matches(institution.getIdpattern())) {
                    if (person.getOperation() == 0) {
                        try {
                            if (repository.getPerson(person.getUserid()) == null) {
                                person.setADD();
                            } else {
                                person.setEDIT();
                            }
                        } catch (Exception e) {
                            log.warn("Correct RECSTATUS error: ", e);
                            person.setADD();
                        }
                    }
                    clonePkg.add(person);
                } else {
                    log.warn("invalid id format: \n" + record.toString());
                    clonePkg.add(new GMessage(GMessage.CODE_ERROR, GMessage.TARGET_RETURN, GMessage.SOURCE_GOLEM, record.getID(), "invalid id format"));
                }
            } else if (record.getClass().equals(GGroup.class)) {
                GGroup group = (GGroup) record;
                if (tolowercase) {
                    group.setGroupid(group.getGroupid().toLowerCase());
                }
                if (group.getID().matches(institution.getIdpattern())) {
                    HashSet<String> tset = new HashSet<String>();
                    if (group.getTargets().equals(STAR)) {
                        group.setTargets(institution.getTargets());
                    }
                    for (String s : group.getTargets().split("[ \t,;:]+")) {
                        tset.add(s);
                    }
                    tset.retainAll(itargetset);
                    if (tset.isEmpty()) {
                        log.warn("Record Targets Error: \n" + pkg.getDatasource());
                        clonePkg.add(new GMessage(GMessage.CODE_ERROR, GMessage.TARGET_RETURN, GMessage.SOURCE_GOLEM, pkg.getID(), "invalid target format!?"));
                    } else {
                        if (group.getOperation() == 0) {
                            try {
                                if (repository.getGroup(group.getGroupid()) == null) {
                                    group.setADD();
                                } else {
                                    group.setEDIT();
                                }
                            } catch (Exception e) {
                                log.warn("Correct group RECSTATUS error: ", e);
                                group.setADD();
                            }
                        }
                        clonePkg.add(record);
                    }
                } else {
                    log.warn("invalid id format: \n" + record.toString());
                    clonePkg.add(new GMessage(GMessage.CODE_ERROR, GMessage.TARGET_RETURN, GMessage.SOURCE_GOLEM, record.getID(), "invalid id format"));
                }
            } else if (record.getClass().equals(GMembership.class)) {
                GMembership ms = (GMembership) record;
                if (ms.getOperation() == 0) {
                    ms.setADD();
                }
                try {
                    String gid = ms.getGroupid();
                    GGroup group = repository.getGroup(gid);
                    if (group == null) {
                        for (GGroup g : pkg.getGroups()) {
                            if (g.getID().equals(gid)) {
                                group = g;
                                break;
                            }
                        }
                    }
                    GPerson person = repository.getPerson(ms.getUserid());
                    String pid = ms.getUserid();
                    if (person == null) {
                        for (GPerson p : pkg.getPersons()) {
                            if (p.getID().equals(pid)) {
                                person = p;
                                break;
                            }
                        }
                    }
                    if (group != null && person != null) {
                        clonePkg.add(record);
                    } else {
                        clonePkg.add(new GMessage(GMessage.CODE_ERROR, GMessage.TARGET_RETURN, GMessage.SOURCE_GOLEM, pkg.getID(), "Membership cannot be established for " + ms.toString() + ": repository says: user=" + person + " group=" + group));
                    }
                } catch (Exception e) {
                    log.warn("Enrolment failure [" + ms.toString() + "]:\n", e);
                    clonePkg.add(new GMessage(GMessage.CODE_ERROR, GMessage.TARGET_RETURN, GMessage.SOURCE_GOLEM, pkg.getID(), "Membership cannot be established for " + ms.toString()));
                }
            } else {
                clonePkg.add(record);
            }
        }
        return clonePkg;
    }

    public String nextSystemID() throws Exception {
        return repository.nextSystemID();
    }

    public String nextUserID() throws Exception {
        return repository.nextUserID();
    }

    /**
	 * Load Package into repository and send to target devices.
	 *
	 * @param pkg
	 * @return
	 * @throws Exception
	 */
    public GEnterprisePackage process(GEnterprisePackage pkg) throws Exception {
        GEnterprisePackage msgpkg = new GEnterprisePackage();
        msgpkg.addAll(pkg.getMessages());
        if (pkg.getContent().size() == 0) {
            msgpkg.add(new GMessage(GMessage.CODE_ERROR, GMessage.TARGET_RETURN, GMessage.SOURCE_GOLEM, GMessage.REF_PKG, "Package has no content"));
            return msgpkg;
        }
        GEnterprisePackage repmsgs = repository.addPackage(pkg);
        msgpkg.addAll(repmsgs.getMessages());
        log.info(pkg.getContent().size() + " Records imported into Repository");
        List<GDevice> devices = repository.getDevices(pkg);
        for (GDevice device : devices) {
            try {
                GEnterprisePackage msgs = ((GWriterDevice) device).write(pkg);
                msgpkg.addAll(msgs.getMessages());
            } catch (Exception e) {
                log.error("Device " + device.getID() + " : " + e.toString() + " ... dumping package");
                dumpPackage(device.getID().replaceAll("[^A-Za-z0-9_-]", "b"), pkg);
                GMessage msg = new GMessage(GMessage.CODE_WARNING, GMessage.TARGET_RETURN, device.getID(), GMessage.REF_PKG, e.toString() + " (package dumped - GOLEM will handle this)");
                msgpkg.add(msg);
            }
        }
        return msgpkg;
    }

    /**
	 * Dump zipped Package with temp name in appropriate dump location.
	 *
	 * @param device
	 * @param pkg
	 * @throws IOException
	 * @throws ConfigurationException
	 */
    public void dumpPackage(String device, GEnterprisePackage pkg) throws IOException, ConfigurationException {
        String ddir = this.dumpDirectory + File.separator + device + File.separator;
        File dumpdir = new File(ddir);
        if (!dumpdir.exists()) dumpdir.mkdirs();
        File dfile = File.createTempFile("pkg-", ".zip", dumpdir);
        FileOutputStream out = new FileOutputStream(dfile);
        ZipOutputStream zipout = new ZipOutputStream(out);
        ZipEntry ze = new ZipEntry("pkg-" + (int) System.currentTimeMillis() + ".xml");
        zipout.putNextEntry(ze);
        zipout.setLevel(7);
        pkg.getPackageFactory().writePackage(pkg, zipout);
        zipout.closeEntry();
        zipout.close();
        out.close();
    }

    public void initInstitution(GInstitution institution) {
        repository.initExternal(institution);
    }

    public SecurityController getSecurity() {
        return security;
    }

    public void setSecurity(SecurityController security) {
        this.security = security;
    }

    public boolean isTolowercase() {
        return tolowercase;
    }

    public void setTolowercase(boolean tolowercase) {
        this.tolowercase = tolowercase;
    }

    public String getDumpDirectory() {
        return dumpDirectory;
    }

    public void setDumpDirectory(String dumpDirectory) {
        this.dumpDirectory = dumpDirectory;
    }

    public String getLoadDirectory() {
        return loadDirectory;
    }

    public void setLoadDirectory(String loadDirectory) {
        this.loadDirectory = loadDirectory;
    }
}
