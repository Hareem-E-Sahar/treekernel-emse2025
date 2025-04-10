package org.dspace.content.packager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.crosswalk.CrosswalkException;
import org.dspace.content.crosswalk.DisseminationCrosswalk;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.PluginManager;
import org.dspace.core.Utils;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import edu.harvard.hul.ois.mets.Agent;
import edu.harvard.hul.ois.mets.AmdSec;
import edu.harvard.hul.ois.mets.Checksumtype;
import edu.harvard.hul.ois.mets.Div;
import edu.harvard.hul.ois.mets.DmdSec;
import edu.harvard.hul.ois.mets.FLocat;
import edu.harvard.hul.ois.mets.FileGrp;
import edu.harvard.hul.ois.mets.FileSec;
import edu.harvard.hul.ois.mets.Fptr;
import edu.harvard.hul.ois.mets.Loctype;
import edu.harvard.hul.ois.mets.MdWrap;
import edu.harvard.hul.ois.mets.Mdtype;
import edu.harvard.hul.ois.mets.Mets;
import edu.harvard.hul.ois.mets.MetsHdr;
import edu.harvard.hul.ois.mets.Name;
import edu.harvard.hul.ois.mets.Role;
import edu.harvard.hul.ois.mets.StructMap;
import edu.harvard.hul.ois.mets.TechMD;
import edu.harvard.hul.ois.mets.Type;
import edu.harvard.hul.ois.mets.XmlData;
import edu.harvard.hul.ois.mets.helper.MetsElement;
import edu.harvard.hul.ois.mets.helper.MetsException;
import edu.harvard.hul.ois.mets.helper.MetsValidator;
import edu.harvard.hul.ois.mets.helper.MetsWriter;
import edu.harvard.hul.ois.mets.helper.PCData;
import edu.harvard.hul.ois.mets.helper.PreformedXML;

/**
 * Base class for disseminator of
 * METS (Metadata Encoding & Transmission Standard) Package.<br>
 *   See <a href="http://www.loc.gov/standards/mets/">http://www.loc.gov/standards/mets/</a>
 * <p>
 * This is a generic packager framework intended to be subclassed to create
 * packagers for more specific METS "profiles".   METS is an
 * abstract and flexible framework that can encompass many
 * different kinds of metadata and inner package structures.
 * <p>
 * <b>Package Parameters:</b><br>
 * <code>manifestOnly</code> -- if true, generate a standalone XML
 * document of the METS manifest instead of a complete package.  Any
 * other metadata (such as licenses) will be encoded inline.
 * Default is <code>false</code>.
 *
 *   <code>unauthorized</code> -- this determines what is done when the
 *   packager encounters a Bundle or Bitstream it is not authorized to
 *   read.  By default, it just quits with an AuthorizeException.
 *   If this option is present, it must be one of the following values:
 *     <code>skip</code> -- simply exclude unreadable content from package.
 *     <code>zero</code> -- include unreadable bitstreams as 0-length files;
 *       unreadable Bundles will still cause authorize errors.
 *
 * @author Larry Stone
 * @author Robert Tansley
 * @version $Revision: 1446 $
 */
public abstract class AbstractMETSDisseminator implements PackageDisseminator {

    /** log4j category */
    private static Logger log = Logger.getLogger(AbstractMETSDisseminator.class);

    /** Filename of manifest, relative to package toplevel. */
    public static final String MANIFEST_FILE = "mets.xml";

    private static XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

    private int idCounter = 1;

    /**
     * Table of files to add to package, such as mdRef'd metadata.
     * Key is relative pathname of file, value is <code>InputStream</code>
     * with contents to put in it.
     * New map is created by disseminate().
     */
    protected Map extraFiles = null;

    /**
     * Make a new unique ID with specified prefix.
     * @param prefix the prefix of the identifier, constrained to XML ID schema
     * @return a new string identifier unique in this session (instance).
     */
    protected String gensym(String prefix) {
        return prefix + "_" + String.valueOf(idCounter++);
    }

    public String getMIMEType(PackageParameters params) {
        return (params != null && params.getProperty("manifestOnly") != null) ? "text/xml" : "application/zip";
    }

    /**
     * Export the object (Item, Collection, or Community) to a
     * package file on the indicated OutputStream.
     * Gets an exception of the object cannot be packaged or there is
     * a failure creating the package.
     *
     * @param context - DSpace context.
     * @param dso - DSpace object (item, collection, etc)
     * @param pkg - output stream on which to write package
     * @throws PackageException if package cannot be created or there is
     *  a fatal error in creating it.
     */
    public void disseminate(Context context, DSpaceObject dso, PackageParameters params, OutputStream pkg) throws PackageValidationException, CrosswalkException, AuthorizeException, SQLException, IOException {
        if (dso.getType() == Constants.ITEM) {
            Item item = (Item) dso;
            long lmTime = item.getLastModified().getTime();
            String unauth = (params == null) ? null : params.getProperty("unauthorized");
            if (params != null && params.getProperty("manifestOnly") != null) {
                extraFiles = null;
                writeManifest(context, item, params, pkg);
            } else {
                extraFiles = new HashMap();
                ZipOutputStream zip = new ZipOutputStream(pkg);
                zip.setComment("METS archive created by DSpace METSDisseminationCrosswalk");
                ZipEntry me = new ZipEntry(MANIFEST_FILE);
                me.setTime(lmTime);
                zip.putNextEntry(me);
                writeManifest(context, item, params, zip);
                zip.closeEntry();
                Iterator fi = extraFiles.keySet().iterator();
                while (fi.hasNext()) {
                    String fname = (String) fi.next();
                    ZipEntry ze = new ZipEntry(fname);
                    ze.setTime(lmTime);
                    zip.putNextEntry(ze);
                    Utils.copy((InputStream) extraFiles.get(fname), zip);
                    zip.closeEntry();
                }
                Bundle bundles[] = item.getBundles();
                for (int i = 0; i < bundles.length; i++) {
                    if (!PackageUtils.isMetaInfoBundle(bundles[i])) {
                        if (!AuthorizeManager.authorizeActionBoolean(context, bundles[i], Constants.READ)) {
                            if (unauth != null && (unauth.equalsIgnoreCase("skip"))) {
                                log.warn("Skipping Bundle[\"" + bundles[i].getName() + "\"] because you are not authorized to read it.");
                                continue;
                            } else throw new AuthorizeException("Not authorized to read Bundle named \"" + bundles[i].getName() + "\"");
                        }
                        Bitstream[] bitstreams = bundles[i].getBitstreams();
                        for (int k = 0; k < bitstreams.length; k++) {
                            boolean auth = AuthorizeManager.authorizeActionBoolean(context, bitstreams[k], Constants.READ);
                            if (auth || (unauth != null && unauth.equalsIgnoreCase("zero"))) {
                                ZipEntry ze = new ZipEntry(makeBitstreamName(bitstreams[k]));
                                ze.setTime(lmTime);
                                ze.setSize(auth ? bitstreams[k].getSize() : 0);
                                zip.putNextEntry(ze);
                                if (auth) Utils.copy(bitstreams[k].retrieve(), zip); else log.warn("Adding zero-length file for Bitstream, SID=" + String.valueOf(bitstreams[k].getSequenceID()) + ", not authorized for READ.");
                                zip.closeEntry();
                            } else if (unauth != null && unauth.equalsIgnoreCase("skip")) {
                                log.warn("Skipping Bitstream, SID=" + String.valueOf(bitstreams[k].getSequenceID()) + ", not authorized for READ.");
                            } else {
                                throw new AuthorizeException("Not authorized to read Bitstream, SID=" + String.valueOf(bitstreams[k].getSequenceID()));
                            }
                        }
                    }
                }
                zip.close();
                extraFiles = null;
            }
        } else throw new PackageValidationException("Can only disseminate an Item now.");
    }

    /**
     * Create name that bitstream will have in archive.  Name must
     * be unique and relative to archive top level, e.g. "bitstream_<id>.ext"
     */
    private String makeBitstreamName(Bitstream bitstream) {
        String base = "bitstream_" + String.valueOf(bitstream.getID());
        String ext[] = bitstream.getFormat().getExtensions();
        return (ext.length > 0) ? base + "." + ext[0] : base;
    }

    private void setMdType(MdWrap mdWrap, String mdtype) {
        try {
            mdWrap.setMDTYPE(Mdtype.parse(mdtype));
        } catch (MetsException e) {
            mdWrap.setMDTYPE(Mdtype.OTHER);
            mdWrap.setOTHERMDTYPE(mdtype);
        }
    }

    /**
     * Write out a METS manifest.
     * Mostly lifted from Rob Tansley's METS exporter.
     */
    private void writeManifest(Context context, Item item, PackageParameters params, OutputStream out) throws PackageValidationException, CrosswalkException, AuthorizeException, SQLException, IOException {
        try {
            Mets mets = new Mets();
            mets.setID(gensym("mets"));
            mets.setOBJID("hdl:" + item.getHandle());
            mets.setLABEL("DSpace Item");
            mets.setPROFILE(getProfile());
            MetsHdr metsHdr = new MetsHdr();
            metsHdr.setCREATEDATE(new Date());
            Agent agent = new Agent();
            agent.setROLE(Role.CUSTODIAN);
            agent.setTYPE(Type.ORGANIZATION);
            Name name = new Name();
            name.getContent().add(new PCData(ConfigurationManager.getProperty("dspace.name")));
            agent.getContent().add(name);
            metsHdr.getContent().add(agent);
            mets.getContent().add(metsHdr);
            String dmdTypes[] = getDmdTypes(params);
            String dmdGroup = gensym("dmd_group");
            String dmdId[] = new String[dmdTypes.length];
            for (int i = 0; i < dmdTypes.length; ++i) {
                dmdId[i] = gensym("dmd");
                XmlData xmlData = new XmlData();
                String xwalkName, metsName;
                String parts[] = dmdTypes[i].split(":", 2);
                if (parts.length > 1) {
                    metsName = parts[0];
                    xwalkName = parts[1];
                } else xwalkName = metsName = dmdTypes[i];
                DisseminationCrosswalk xwalk = (DisseminationCrosswalk) PluginManager.getNamedPlugin(DisseminationCrosswalk.class, xwalkName);
                if (xwalk == null) throw new PackageValidationException("Cannot find " + dmdTypes[i] + " crosswalk plugin!"); else crosswalkToMets(xwalk, item, xmlData);
                DmdSec dmdSec = new DmdSec();
                dmdSec.setID(dmdId[i]);
                dmdSec.setGROUPID(dmdGroup);
                MdWrap mdWrap = new MdWrap();
                setMdType(mdWrap, metsName);
                mdWrap.getContent().add(xmlData);
                dmdSec.getContent().add(mdWrap);
                mets.getContent().add(dmdSec);
            }
            String licenseID = null;
            try {
                AmdSec amdSec = new AmdSec();
                addRightsMd(context, item, amdSec);
                if (amdSec.getContent().size() > 0) {
                    licenseID = gensym("license");
                    amdSec.setID(licenseID);
                    mets.getContent().add(amdSec);
                }
            } catch (AuthorizeException e) {
                String unauth = (params == null) ? null : params.getProperty("unauthorized");
                if (!(unauth != null && unauth.equalsIgnoreCase("skip"))) throw e; else log.warn("Skipping license metadata because of access failure: " + e.toString());
            }
            FileSec fileSec = new FileSec();
            String techMdType = getTechMdType(params);
            String parts[] = techMdType.split(":", 2);
            String xwalkName, metsName;
            if (parts.length > 1) {
                metsName = parts[0];
                xwalkName = parts[1];
            } else xwalkName = metsName = techMdType;
            DisseminationCrosswalk xwalk = (DisseminationCrosswalk) PluginManager.getNamedPlugin(DisseminationCrosswalk.class, xwalkName);
            if (xwalk == null) throw new PackageValidationException("Cannot find " + xwalkName + " crosswalk plugin!");
            String primaryBitstreamFileID = null;
            List contentDivs = new ArrayList();
            String unauth = (params == null) ? null : params.getProperty("unauthorized");
            Bundle[] bundles = item.getBundles();
            for (int i = 0; i < bundles.length; i++) {
                if (PackageUtils.isMetaInfoBundle(bundles[i])) continue;
                if (!AuthorizeManager.authorizeActionBoolean(context, bundles[i], Constants.READ)) {
                    if (unauth != null && (unauth.equalsIgnoreCase("skip"))) continue; else throw new AuthorizeException("Not authorized to read Bundle named \"" + bundles[i].getName() + "\"");
                }
                Bitstream[] bitstreams = bundles[i].getBitstreams();
                FileGrp fileGrp = new FileGrp();
                String bName = bundles[i].getName();
                if ((bName != null) && !bName.equals("")) fileGrp.setUSE(bundleToFileGrp(bName));
                int primaryBitstreamID = -1;
                boolean isContentBundle = false;
                if ((bName != null) && bName.equals("ORIGINAL")) {
                    isContentBundle = true;
                    primaryBitstreamID = bundles[i].getPrimaryBitstreamID();
                }
                for (int bits = 0; bits < bitstreams.length; bits++) {
                    boolean auth = AuthorizeManager.authorizeActionBoolean(context, bitstreams[bits], Constants.READ);
                    if (!auth) {
                        if (unauth != null && unauth.equalsIgnoreCase("skip")) continue; else if (!(unauth != null && unauth.equalsIgnoreCase("zero"))) throw new AuthorizeException("Not authorized to read Bitstream, SID=" + String.valueOf(bitstreams[bits].getSequenceID()));
                    }
                    String sid = String.valueOf(bitstreams[bits].getSequenceID());
                    edu.harvard.hul.ois.mets.File file = new edu.harvard.hul.ois.mets.File();
                    String xmlIDstart = "bitstream_";
                    String fileID = xmlIDstart + sid;
                    file.setID(fileID);
                    if (bitstreams[bits].getID() == primaryBitstreamID) primaryBitstreamFileID = fileID;
                    if (isContentBundle) {
                        Div div = new Div();
                        div.setID(gensym("div"));
                        div.setTYPE("DSpace Content Bitstream");
                        Fptr fptr = new Fptr();
                        fptr.setFILEID(fileID);
                        div.getContent().add(fptr);
                        contentDivs.add(div);
                    }
                    file.setSEQ(bitstreams[bits].getSequenceID());
                    String groupID = "GROUP_" + xmlIDstart + sid;
                    if ((bundles[i].getName() != null) && (bundles[i].getName().equals("THUMBNAIL") || bundles[i].getName().startsWith("TEXT"))) {
                        Bitstream original = findOriginalBitstream(item, bitstreams[bits]);
                        if (original != null) {
                            groupID = "GROUP_" + xmlIDstart + original.getSequenceID();
                        }
                    }
                    file.setGROUPID(groupID);
                    file.setMIMETYPE(bitstreams[bits].getFormat().getMIMEType());
                    file.setSIZE(auth ? bitstreams[bits].getSize() : 0);
                    String csType = bitstreams[bits].getChecksumAlgorithm();
                    String cs = bitstreams[bits].getChecksum();
                    if (auth && cs != null && csType != null) {
                        try {
                            file.setCHECKSUMTYPE(Checksumtype.parse(csType));
                            file.setCHECKSUM(cs);
                        } catch (MetsException e) {
                            log.warn("Cannot set bitstream checksum type=" + csType + " in METS.");
                        }
                    }
                    FLocat flocat = new FLocat();
                    flocat.setLOCTYPE(Loctype.URL);
                    flocat.setXlinkHref(makeBitstreamName(bitstreams[bits]));
                    String techID = "techMd_for_bitstream_" + bitstreams[bits].getSequenceID();
                    AmdSec fAmdSec = new AmdSec();
                    fAmdSec.setID(techID);
                    TechMD techMd = new TechMD();
                    techMd.setID(gensym("tech"));
                    MdWrap mdWrap = new MdWrap();
                    setMdType(mdWrap, metsName);
                    XmlData xmlData = new XmlData();
                    mdWrap.getContent().add(xmlData);
                    techMd.getContent().add(mdWrap);
                    fAmdSec.getContent().add(techMd);
                    mets.getContent().add(fAmdSec);
                    crosswalkToMets(xwalk, bitstreams[bits], xmlData);
                    file.setADMID(techID);
                    file.getContent().add(flocat);
                    fileGrp.getContent().add(file);
                }
                fileSec.getContent().add(fileGrp);
            }
            mets.getContent().add(fileSec);
            StringBuffer dmdIds = new StringBuffer();
            for (int i = 0; i < dmdId.length; ++i) dmdIds.append(" " + dmdId[i]);
            StructMap structMap = new StructMap();
            structMap.setID(gensym("struct"));
            structMap.setTYPE("LOGICAL");
            structMap.setLABEL("DSpace");
            Div div0 = new Div();
            div0.setID(gensym("div"));
            div0.setTYPE("DSpace Item");
            div0.setDMDID(dmdIds.substring(1));
            if (licenseID != null) div0.setADMID(licenseID);
            if (primaryBitstreamFileID != null) {
                Fptr fptr = new Fptr();
                fptr.setFILEID(primaryBitstreamFileID);
                div0.getContent().add(fptr);
            }
            div0.getContent().addAll(contentDivs);
            structMap.getContent().add(div0);
            addStructMap(context, item, params, mets);
            mets.getContent().add(structMap);
            mets.validate(new MetsValidator());
            mets.write(new MetsWriter(out));
        } catch (MetsException e) {
            throw new PackageValidationException(e);
        }
    }

    /**
     * For a bitstream that's a thumbnail or extracted text, find the
     * corresponding bitstream it was derived from, in the ORIGINAL bundle.
     *
     * @param item
     *            the item we're dealing with
     * @param derived
     *            the derived bitstream
     *
     * @return the corresponding original bitstream (or null)
     */
    protected static Bitstream findOriginalBitstream(Item item, Bitstream derived) throws SQLException {
        Bundle[] bundles = item.getBundles();
        String originalFilename = derived.getName().substring(0, derived.getName().length() - 4);
        for (int i = 0; i < bundles.length; i++) {
            if ((bundles[i].getName() != null) && bundles[i].getName().equals("ORIGINAL")) {
                Bitstream[] bitstreams = bundles[i].getBitstreams();
                for (int bsnum = 0; bsnum < bitstreams.length; bsnum++) {
                    if (bitstreams[bsnum].getName().equals(originalFilename)) {
                        return bitstreams[bsnum];
                    }
                }
            }
        }
        return null;
    }

    private void crosswalkToMets(DisseminationCrosswalk xwalk, DSpaceObject dso, MetsElement me) throws CrosswalkException, IOException, SQLException, AuthorizeException {
        String raw = xwalk.getSchemaLocation();
        String sloc[] = raw == null ? null : raw.split("\\s+");
        Namespace ns[] = xwalk.getNamespaces();
        for (int i = 0; i < ns.length; ++i) {
            String uri = ns[i].getURI();
            if (sloc != null && sloc.length > 1 && uri.equals(sloc[0])) me.setSchema(ns[i].getPrefix(), uri, sloc[1]); else me.setSchema(ns[i].getPrefix(), uri);
        }
        PreformedXML pXML = new PreformedXML(xwalk.preferList() ? outputter.outputString(xwalk.disseminateList(dso)) : outputter.outputString(xwalk.disseminateElement(dso)));
        me.getContent().add(pXML);
    }

    /**
     * Returns name of METS profile to which this package conforms, e.g.
     *  "DSpace METS DIP Profile 1.0"
     * @return string name of profile.
     */
    public abstract String getProfile();

    /**
     * Returns fileGrp's USE attribute value corresponding to a DSpace bundle name.
     *
     * @param bname name of DSpace bundle.
     * @return string name of fileGrp
     */
    public abstract String bundleToFileGrp(String bname);

    /**
     * Get the types of Item-wide DMD to include in package.
     * Each element of the returned array is a String, which
     * MAY be just a simple name, naming both the Crosswalk Plugin and
     * the METS "MDTYPE", <em>or</em> a colon-separated pair consisting of
     * the METS name followed by a colon and the Crosswalk Plugin name.
     * E.g. the type string <code>"DC:qualifiedDublinCore"</code> tells it to
     * create a METS section with <code>MDTYPE="DC"</code> and use the plugin
     * named "qualifiedDublinCore" to obtain the data.
     * @param params the PackageParameters passed to the disseminator.
     * @return array of metadata type strings, never null.
     */
    public abstract String[] getDmdTypes(PackageParameters params) throws SQLException, IOException, AuthorizeException;

    /**
     * Get the type string of the technical metadata to create for each
     * Bitstream in the Item.  The type string may be a simple name or
     * colon-separated compound as specified for <code>getDmdTypes()</code> above.
     * @param params the PackageParameters passed to the disseminator.
     * @return array of metadata type strings, never null.
     */
    public abstract String getTechMdType(PackageParameters params) throws SQLException, IOException, AuthorizeException;

    /**
     * Add Rights metadata for the Item, in the form of
     * (<code>rightsMd</code> elements) to the given metadata section.
     *
     */
    public abstract void addRightsMd(Context context, Item item, AmdSec amdSec) throws SQLException, IOException, AuthorizeException, MetsException;

    /**
     * Add any additional <code>structMap</code> elements to the
     * METS document, as required by this subclass.  A simple default
     * structure map which fulfills the minimal DSpace METS DIP/SIP
     * requirements is already present, so this does not need to do anything.
     * @param mets the METS document to which to add structMaps
     */
    public abstract void addStructMap(Context context, Item item, PackageParameters params, Mets mets) throws SQLException, IOException, AuthorizeException, MetsException;
}
