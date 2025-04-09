package hu.sztaki.lpds.pgportal.portlets.repository;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.Deflater;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.Namespace;
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
import edu.harvard.hul.ois.mets.MdRef;
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
import edu.harvard.hul.ois.mets.helper.MetsReader;
import edu.harvard.hul.ois.mets.helper.Any;

/**
 * <pre>Prepare a METS SIP (Submission Information Package) to ingest
 * into DSpace, creating a new Item.
 *
 * This is a utility to help another application prepare a DSpace
 * SIP with as little effort as possible.  All it has to do is:
 *  - Create a SIP
 *  - Add Bitstreams
 *  - Add Descriptive Metadata
 *  - Write the SIP, to either a file or OutputStream.
 * It works in conjunction with the simple LNI client to upload a SIP
 * directly to the LNI.
 *
 * It does not rely on *any* DSpace code.  It only requires JDOM (for XML)
 * and the Harvard METS toolkit.
 *
 * Requires Sun Java JRE 5 and these libraries:
 *
 *  - Harvard METS Java toolkit, version 1.5
 *     http://hul.harvard.edu/mets/
 *
 *  - JDOM 1.0
 *     http://jdom.org/
 * </pre>
 * @author Larry Stone
 * @version $Revision: 1.1 $
 */
public class DSpaceSIP {

    private static final String METS_PROFILE = "DSpace METS SIP Profile 1.0";

    private static final boolean VALIDATE_DEFAULT = true;

    private static final String MANIFEST_FILE = "mets.xml";

    private static final String PREMIS_NS_URI = "http://www.loc.gov/standards/premis";

    private static final Namespace PREMIS_NS = Namespace.getNamespace("premis", PREMIS_NS_URI);

    private static XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

    private int idCounter = 1;

    private String dmdGroupID = gensym("dmd_group");

    private boolean validate = VALIDATE_DEFAULT;

    private int compression = 0;

    /**
     * Table of files to add to package, such as mdRef'd metadata.
     * Key is relative pathname of file, value a record of associated paths.
     */
    private Map<String, PackageFile> zipFiles = new HashMap<String, PackageFile>();

    private Map<String, List> bundles = new HashMap<String, List>();

    private String primaryBitstream = null;

    private Mets manifest = null;

    private List<String> dmdIDs = new ArrayList<String>();

    class PackageFile {

        String relPath = null;

        File absPath = null;

        String zipPath = null;

        PackageFile(String r, File a) {
            relPath = r;
            absPath = a;
            zipPath = gensym("pkgfile");
        }
    }

    /**
     * Default constructor.
     */
    public DSpaceSIP() throws MetsException {
        super();
        init(VALIDATE_DEFAULT, Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * Detailed constructor.
     * @param validate whether or not to validate the resulting METS
     * @param compression level of compression (0-9) to use in Zipfile.
     */
    public DSpaceSIP(boolean validate, int compression) throws MetsException {
        super();
        init(validate, compression);
    }

    private void init(boolean validate, int compression) {
        this.validate = validate;
        this.compression = compression;
        manifest = new Mets();
        manifest.setID(gensym("mets"));
        manifest.setLABEL("DSpace Item");
        manifest.setPROFILE(METS_PROFILE);
        MetsHdr metsHdr = new MetsHdr();
        metsHdr.setCREATEDATE(new Date());
        manifest.getContent().add(metsHdr);
    }

    /**
     * Set the OBJID attribute in the METS manifest
     * @param o new value for OBJID
     */
    public void setOBJID(String o) {
        if (o != null) manifest.setOBJID(o);
    }

    /**
     * Adds a a Bitstream to this Item, using contents of a File in the filesystem.
     * @param path the File containing the data of this Bitstream.
     * @param name logical pathname within the Item (DSpace Bitstream's "name" attribute)
     * @param isPrimaryBitstream true if this is the Item' Primary Bitstream, i.e. index page of a website.
     */
    public void addBitstream(File path, String name, String bundle, boolean isPrimaryBitstream) {
        zipFiles.put(name, new PackageFile(name, path));
        if (bundles.containsKey(bundle)) bundles.get(bundle).add(name); else {
            List<String> a = new ArrayList<String>();
            a.add(name);
            bundles.put(bundle, a);
        }
        if (isPrimaryBitstream) primaryBitstream = name;
    }

    /**
     * Adds a section of descriptive metadata (DMD) to the METS document,
     * based on the contents of a File in the filesystem.  The file is
     * added to the SIP and referenced from the METS document.
     * Note that all DMD sections apply to the entire Item in the SIP.
     * @param type METS metadata type name (e.g. "MODS")
     * @param mdFile the File containing metadata
     * @param mimeType internet media type (MIME type) of contents of mdFile
     */
    public void addDescriptiveMD(String type, File mdFile, String mimeType) {
        String locfile = gensym("mdfile");
        zipFiles.put(locfile, new PackageFile(locfile, mdFile));
        DmdSec dmdSec = new DmdSec();
        String dmdID = gensym("dmd");
        dmdSec.setID(dmdID);
        dmdIDs.add(dmdID);
        dmdSec.setGROUPID(dmdGroupID);
        MdRef ref = new MdRef();
        ref.setMIMETYPE(mimeType);
        setMdType(ref, type);
        ref.setLOCTYPE(Loctype.URL);
        ref.setXlinkHref(locfile);
        dmdSec.getContent().add(ref);
        manifest.getContent().add(dmdSec);
    }

    /**
     * Adds a section of descriptive metadata (DMD) to the METS document,
     * based on a list of JDOM elements.  The elements become the
     * contents of the dmdSec in the METS document.
     * Note that all DMD sections apply to the entire Item in the SIP.
     * @param type METS metadata type name (e.g. "MODS")
     * @param md list of JDOM elements containing the DMD
     */
    public void addDescriptiveMD(String type, List<Element> md) throws MetsException {
        Element first = md.get(0);
        XmlData xd = addDescriptiveMDInternal(type, outputter.outputString(first), first.getNamespace());
        for (Element e : md.subList(1, md.size())) addToXmlData(xd, outputter.outputString(e), e.getNamespace());
    }

    /**
     * Adds a section of descriptive metadata (DMD) to the METS document,
     * based on a JDOM element.  The element will be the
     * contents of the dmdSec in the METS document.
     * Note that all DMD sections apply to the entire Item in the SIP.
     * @param type METS metadata type name (e.g. "MODS")
     * @param md JDOM element containing the DMD
     */
    public void addDescriptiveMD(String type, Element md) throws MetsException {
        addDescriptiveMDInternal(type, outputter.outputString(md), md.getNamespace());
    }

    /**
     * Adds a section of descriptive metadata (DMD) to the METS document,
     * based on String containing serialized XML.  The string is expected
     * to contain one element, which becomes the
     * contents of the dmdSec in the METS document.
     * Note that all DMD sections apply to the entire Item in the SIP.
     * @param type METS metadata type name (e.g. "MODS")
     * @param md serialized XML metadata
     */
    public void addDescriptiveMD(String type, String md) throws MetsException {
        addDescriptiveMDInternal(type, md, null);
    }

    /**
     * Add serialized XML to the METS toolkit's XmlData object's contents.
     * This is a separate function to facilitate experimenting with
     * namespace handling and parsing techniques during development.
     */
    private void addToXmlData(XmlData xd, String md, Namespace ns) throws MetsException {
        xd.getContent().add(Any.reader(new MetsReader(new ByteArrayInputStream(md.getBytes()))));
    }

    /**
     * Adds a dmdSec to the METS manifest containing serialized XML metadata.
     * Returns the xmlData element within that dmdSec.
     */
    private XmlData addDescriptiveMDInternal(String type, String md, Namespace ns) throws MetsException {
        XmlData xmlData = new XmlData();
        if (ns != null) xmlData.setSchema(ns.getPrefix(), ns.getURI());
        addToXmlData(xmlData, md, ns);
        DmdSec dmdSec = new DmdSec();
        String dmdID = gensym("dmd");
        dmdSec.setID(dmdID);
        dmdIDs.add(dmdID);
        dmdSec.setGROUPID(dmdGroupID);
        MdWrap mdWrap = new MdWrap();
        setMdType(mdWrap, type);
        mdWrap.getContent().add(xmlData);
        dmdSec.getContent().add(mdWrap);
        manifest.getContent().add(dmdSec);
        return xmlData;
    }

    /**
     * Adds Agent element to the METS header.
     * @param role one of the acceptable METS roles, e.g. "CUSTODIAN"
     * @param type one of the acceptable METS types e.g. "ORGANIZATION"
     * @param aname proper name of the agent
     */
    public void addAgent(String role, String type, String aname) {
        Agent agent = new Agent();
        try {
            agent.setROLE(Role.parse(role.toUpperCase()));
        } catch (MetsException e) {
            agent.setROLE(Role.OTHER);
            agent.setOTHERROLE(role);
        }
        try {
            agent.setTYPE(Type.parse(type.toUpperCase()));
        } catch (MetsException e) {
            agent.setTYPE(Type.OTHER);
            agent.setOTHERTYPE(type);
        }
        Name name = new Name();
        name.getContent().add(new PCData(aname));
        agent.getContent().add(name);
        for (Object o : manifest.getContent()) {
            if (o instanceof MetsHdr) {
                ((MetsHdr) o).getContent().add(agent);
                break;
            }
        }
    }

    /**
     * Make a new unique ID with specified prefix.
     * @param prefix the prefix of the identifier, constrained to XML ID schema
     * @return a new string identifier unique in this session (instance).
     */
    private String gensym(String prefix) {
        return prefix + "_" + String.valueOf(idCounter++);
    }

    private void finishManifest(OutputStream out) throws MetsException, UnsupportedEncodingException {
        FileSec fileSec = new FileSec();
        String primaryBitstreamFileID = null;
        List<Div> contentDivs = new ArrayList<Div>();
        for (Map.Entry<String, List> e : bundles.entrySet()) {
            List<String> bitstreams = (List<String>) e.getValue();
            FileGrp fileGrp = new FileGrp();
            fileGrp.setUSE(e.getKey());
            for (String bitstream : bitstreams) {
                edu.harvard.hul.ois.mets.File file = new edu.harvard.hul.ois.mets.File();
                String fileID = gensym("bitstream");
                file.setID(fileID);
                if (primaryBitstream != null && primaryBitstream.equals(bitstream)) primaryBitstreamFileID = fileID;
                Div div = new Div();
                div.setID(gensym("div"));
                div.setTYPE("DSpace Content Bitstream");
                Fptr fptr = new Fptr();
                fptr.setFILEID(fileID);
                div.getContent().add(fptr);
                contentDivs.add(div);
                file.setSIZE(zipFiles.get(bitstream).absPath.length());
                FLocat flocat = new FLocat();
                flocat.setLOCTYPE(Loctype.URL);
                flocat.setXlinkHref(zipFiles.get(bitstream).zipPath);
                String techID = gensym("techMd_for_bitstream_");
                AmdSec fAmdSec = new AmdSec();
                fAmdSec.setID(techID);
                TechMD techMd = new TechMD();
                techMd.setID(gensym("tech"));
                MdWrap mdWrap = new MdWrap();
                setMdType(mdWrap, "PREMIS");
                mdWrap.getContent().add(makeFilePREMIS(bitstream));
                techMd.getContent().add(mdWrap);
                fAmdSec.getContent().add(techMd);
                manifest.getContent().add(fAmdSec);
                file.setADMID(techID);
                file.getContent().add(flocat);
                fileGrp.getContent().add(file);
            }
            fileSec.getContent().add(fileGrp);
        }
        if (!fileSec.getContent().isEmpty()) manifest.getContent().add(fileSec);
        StringBuffer dmdIDstr = new StringBuffer();
        for (String dmdID : dmdIDs) dmdIDstr.append(" " + dmdID);
        StructMap structMap = new StructMap();
        structMap.setID(gensym("struct"));
        structMap.setTYPE("LOGICAL");
        structMap.setLABEL("DSpace");
        Div div0 = new Div();
        div0.setID(gensym("div"));
        div0.setTYPE("DSpace Item");
        div0.setDMDID(dmdIDstr.substring(1));
        if (primaryBitstreamFileID != null) {
            Fptr fptr = new Fptr();
            fptr.setFILEID(primaryBitstreamFileID);
            div0.getContent().add(fptr);
        }
        div0.getContent().addAll(contentDivs);
        structMap.getContent().add(div0);
        manifest.getContent().add(structMap);
        if (validate) manifest.validate(new MetsValidator());
        manifest.write(new MetsWriter(out));
    }

    /**
     *
     * Construct minimal PREMIS for a bitstream:
     *   object/objectIdentifier = URL, name
     *   object/originalName = name
     *   object/objectCategory = "File"
     *   object/objectCharacteristics/size = len
     *   object/fixity/messageDigestAlgorithm (OPT)
     *   object/fixity/messageDigest (OPT)
     */
    private XmlData makeFilePREMIS(String bitstream) throws UnsupportedEncodingException {
        Element premis = new Element("premis", PREMIS_NS);
        Element object = new Element("object", PREMIS_NS);
        premis.addContent(object);
        Element oid = new Element("objectIdentifier", PREMIS_NS);
        Element oit = new Element("objectIdentifierType", PREMIS_NS);
        oit.setText("URL");
        oid.addContent(oit);
        Element oiv = new Element("objectIdentifierValue", PREMIS_NS);
        oiv.setText(URLEncoder.encode(bitstream, "UTF-8"));
        oid.addContent(oiv);
        object.addContent(oid);
        Element oc = new Element("objectCategory", PREMIS_NS);
        oc.setText("File");
        object.addContent(oc);
        Element ochar = new Element("objectCharacteristics", PREMIS_NS);
        object.addContent(ochar);
        Element size = new Element("size", PREMIS_NS);
        size.setText(String.valueOf(zipFiles.get(bitstream).absPath.length()));
        ochar.addContent(size);
        Element on = new Element("originalName", PREMIS_NS);
        on.setText(bitstream);
        object.addContent(on);
        XmlData xmlData = new XmlData();
        xmlData.setSchema(PREMIS_NS.getPrefix(), PREMIS_NS.getURI());
        xmlData.getContent().add(new PreformedXML(outputter.outputString(premis)));
        return xmlData;
    }

    private void setMdType(MdWrap mdWrap, String mdtype) {
        try {
            mdWrap.setMDTYPE(Mdtype.parse(mdtype));
        } catch (MetsException e) {
            mdWrap.setMDTYPE(Mdtype.OTHER);
            mdWrap.setOTHERMDTYPE(mdtype);
        }
    }

    private void setMdType(MdRef mdWrap, String mdtype) {
        try {
            mdWrap.setMDTYPE(Mdtype.parse(mdtype));
        } catch (MetsException e) {
            mdWrap.setMDTYPE(Mdtype.OTHER);
            mdWrap.setOTHERMDTYPE(mdtype);
        }
    }

    private static void copyStream(final InputStream input, final OutputStream output) throws IOException {
        final int BUFFER_SIZE = 1024 * 4;
        final byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            final int count = input.read(buffer, 0, BUFFER_SIZE);
            if (-1 == count) break;
            output.write(buffer, 0, count);
        }
    }

    /**
     * Write out the package to filesystem at the designated path.
     * @param path the File to which to write this SIP.
     */
    public void write(File path) throws IOException, MetsException {
        write(new FileOutputStream(path));
    }

    /**
     * Write out the package to a stream.
     * @param out OutputStream to which it is written.
     */
    public void write(OutputStream out) throws IOException, MetsException, UnsupportedEncodingException {
        ZipOutputStream zip = new ZipOutputStream(out);
        zip.setComment("METS archive created by DSpaceSIP");
        zip.setLevel(compression);
        zip.setMethod(ZipOutputStream.DEFLATED);
        ZipEntry me = new ZipEntry(MANIFEST_FILE);
        zip.putNextEntry(me);
        finishManifest(zip);
        zip.closeEntry();
        for (Map.Entry<String, PackageFile> e : zipFiles.entrySet()) {
            PackageFile pf = e.getValue();
            ZipEntry ze = new ZipEntry(pf.zipPath);
            ze.setTime(pf.absPath.lastModified());
            zip.putNextEntry(ze);
            copyStream(new FileInputStream(pf.absPath), zip);
            zip.closeEntry();
        }
        zip.close();
        zipFiles = null;
    }
}
