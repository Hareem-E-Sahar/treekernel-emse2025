package net.flysource.client.gui.flyeditor;

import fi.mmm.yhteinen.swing.core.YModel;
import ij.ImagePlus;
import net.flysource.client.FlyShareApp;
import net.flysource.client.exceptions.CantReplaceFileException;
import net.flysource.client.exceptions.CantSaveException;
import net.flysource.client.exceptions.InvalidFileException;
import net.flysource.client.exceptions.RequiredFieldException;
import net.flysource.client.gui.smartlist.edit.SmartListModel;
import net.flysource.client.util.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.zip.CRC32;

public class FlyPatternModel extends YModel implements Filterable, Syncable, Groupable {

    Logger LOG = Logger.getLogger(FlyPatternModel.class);

    private final String NAME_ELEMENT = "name";

    private final String TYPE_ELEMENT = "type";

    private final String ORIGINATOR_ELEMENT = "originator";

    private final String TIER_ELEMENT = "tier";

    private final String REFERENCE_ELEMENT = "reference";

    private final String MATERIALS_ELEMENT = "materials";

    private final String OTHER_INFO_ELEMENT = "otherinformation";

    private final String TYING_INSTRUCTIONS_ELEMENT = "tyinginstructions";

    private final String IMAGE_ELEMENT = "image";

    private final String CRC_ELEMENT = "crc";

    private final String SHAREABLE_ELEMENT = "shareable";

    public static final int NAME_LEN = 40;

    public static final int TYPE_LEN = 40;

    public static final int ORIGINATOR_LEN = 80;

    public static final int TIER_LEN = 80;

    public static final int FILENAME_LEN = 40;

    public static final int REFERENCE_LEN = 255;

    public static final int DETAILS_LEN = 16000;

    private boolean isNew;

    private boolean isSentToServer;

    private boolean isChangeSync;

    private String originalFilename;

    private String filename = "";

    private String name = "";

    private String type = "";

    private String originator = "";

    private String tier = "";

    private String detailText;

    private boolean shareable;

    private boolean hasImage;

    private String reference = "";

    private ImagePlus image = null;

    private long crc;

    public FlyPatternModel() {
        isNew = true;
        isSentToServer = false;
        originalFilename = "";
        name = "";
        type = "";
        filename = "";
        originator = "";
        tier = "";
        detailText = FSLabels.getLabel("flyeditor.materials") + "\n" + FSLabels.getLabel("flyeditor.instructions") + "\n" + FSLabels.getLabel("flyeditor.otherInfo") + "\n";
        reference = "";
        crc = 0;
        isChangeSync = false;
        setShareable(true);
        setHasImage(false);
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public String getName() {
        if (name == null) return "";
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        if (type == null) return "";
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilename() {
        if (filename == null) return "";
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
        notifyObservers("filename");
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public boolean isChangeSync() {
        return isChangeSync;
    }

    public void setChangeSync(boolean changeSync) {
        isChangeSync = changeSync;
    }

    public String getOriginator() {
        if (originator == null) return "";
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public String getTier() {
        if (tier == null) return "";
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getDetailText() {
        return detailText;
    }

    public void setDetailText(String detailText) {
        this.detailText = detailText;
    }

    private void appendDetailtext(String text) {
        this.detailText += text;
    }

    public String getImageField() {
        return "";
    }

    public void setImageField(String imageField) {
    }

    public long getCrc() {
        return crc;
    }

    public void setCrc(long crc) {
        this.crc = crc;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public boolean isSentToServer() {
        return isSentToServer;
    }

    public void setSentToServer(boolean sentToServer) {
        isSentToServer = sentToServer;
    }

    public ImagePlus getImage() {
        return image;
    }

    public void setImage(ImagePlus image) {
        this.image = image;
    }

    public String getReference() {
        if (reference == null) return "";
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isShareable() {
        return shareable;
    }

    public void setShareable(boolean shareable) {
        this.shareable = shareable;
    }

    public String getShared() {
        return isShareable() ? "Yes" : "No";
    }

    public void validate() throws RequiredFieldException {
        setName(StringUtils.trimToEmpty(getName()));
        setFilename(StringUtils.trimToEmpty(getFilename()));
        setOriginator(StringUtils.trimToEmpty(getOriginator()));
        setReference(StringUtils.trimToEmpty(getReference()));
        setTier(StringUtils.trimToEmpty(getTier()));
        setType(StringUtils.trimToEmpty(getType()));
        String fn = StringUtils.trimToEmpty(filename);
        String ext = FileUtils.getExtension(fn);
        if (fn.length() == 0) {
            fn = FileUtils.makeNiceFilename(FSConfig.getFlyStore(), getName(), ".fly");
            setFilename(fn);
        }
        fn = FileUtils.removeExtension(fn);
        if (!StringUtils.isAlphanumericSpace(fn)) throw new RequiredFieldException(FSMessages.getMessage("flyPattern.badChars"), "filename"); else if (StringUtils.isEmpty(StringUtils.trimToEmpty(name))) throw new RequiredFieldException(FSMessages.getMessage("flyPattern.nameRequired"), "name"); else if (StringUtils.isEmpty(type)) throw new RequiredFieldException(FSMessages.getMessage("flyPattern.typeRequired"), "type"); else if (type.length() > TYPE_LEN) throw new RequiredFieldException(FSMessages.getMessage("flyPattern.typeTooLong"), "type"); else if (StringUtils.isEmpty(fn)) throw new RequiredFieldException(FSMessages.getMessage("flyPattern.filenameRequired"), "filename"); else if (!ext.equalsIgnoreCase(".fly") && fn.length() >= NAME_LEN - 3) throw new RequiredFieldException(FSMessages.getMessage("flyPattern.filenameTooLong"), "filename");
        if (!ext.equalsIgnoreCase(".fly")) {
            setFilename(fn + ".fly");
        }
    }

    public void save(String path) throws CantSaveException, InvalidFileException, CantReplaceFileException {
        CRC32 crc = new CRC32();
        File file = new File(path + File.separator + filename);
        FileUtils.isFileValid(file);
        if (isNew) {
            if (FileUtils.fileExists(path, filename)) {
                throw new CantReplaceFileException(FSMessages.getMessage("flyEditor.alreadyExists"), "filename");
            }
        }
        if (!isNew && !originalFilename.equalsIgnoreCase(filename)) {
            if (FileUtils.fileExists(path, filename)) {
                throw new CantReplaceFileException(FSMessages.getMessage("flyEditor.alreadyExists"), "filename");
            }
        }
        Document doc = new Document();
        doc.addContent(new Comment("This file was created by FlySource. (www.flysource.net)"));
        doc.setRootElement(new Element("FlyPattern"));
        Element root = doc.getRootElement();
        root.setAttribute("version", FlyShareApp.FLY_PATTERN_VERSION);
        Element el = new Element(NAME_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getName()));
        root.addContent(el);
        el = new Element(TYPE_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getType()));
        root.addContent(el);
        el = new Element(ORIGINATOR_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getOriginator()));
        root.addContent(el);
        el = new Element(TIER_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getTier()));
        root.addContent(el);
        el = new Element(REFERENCE_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getReference()));
        root.addContent(el);
        el = new Element(MATERIALS_ELEMENT);
        el.setText(extractMaterials());
        root.addContent(el);
        el = new Element(TYING_INSTRUCTIONS_ELEMENT);
        el.setText(extractTyingInstructions());
        root.addContent(el);
        el = new Element(OTHER_INFO_ELEMENT);
        el.setText(extractOtherInfo());
        root.addContent(el);
        el = new Element(SHAREABLE_ELEMENT);
        el.setText(isShareable() ? "Yes" : "No");
        root.addContent(el);
        crc.update(FlyShareApp.FLY_PATTERN_VERSION.getBytes());
        crc.update(name.getBytes());
        crc.update(type.getBytes());
        if (originator != null) crc.update(originator.getBytes());
        if (detailText != null) crc.update(detailText.getBytes());
        if (reference != null) crc.update(reference.getBytes());
        setHasImage(false);
        if (image != null) {
            try {
                Base64 b64 = new Base64();
                Image i = image.getProcessor().createImage();
                BufferedImage bi = createBufferedImage(i);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(bi, "jpg", bos);
                byte[] buffer = bos.toByteArray();
                crc.update(buffer);
                String s = new String(b64.encode(buffer));
                el = new Element(IMAGE_ELEMENT);
                el.setText(s);
                root.addContent(el);
                setHasImage(true);
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        el = new Element(CRC_ELEMENT);
        el.setText("" + crc.getValue());
        root.addContent(el);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setTextMode(Format.TextMode.PRESERVE));
            outputter.output(doc, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            throw new CantSaveException("Cannot save.\n\n" + e.getMessage());
        }
        if (!isNew && !originalFilename.equalsIgnoreCase(filename)) {
            File oldFile = new File(path + File.separator + originalFilename);
            oldFile.delete();
        }
        if (!isNew) setChangeSync(true);
        setSentToServer(false);
    }

    public boolean load(String path, String filename, boolean loadImage) {
        try {
            File file = new File(path + File.separator + filename);
            SAXBuilder parser = new SAXBuilder();
            try {
                setOriginalFilename(filename);
                setFilename(filename);
                setDetailText("");
                setNew(false);
                Document doc = parser.build(file);
                Element root = doc.getRootElement();
                Element e = root.getChild(NAME_ELEMENT);
                if (e != null) setName(e.getText());
                e = root.getChild(TYPE_ELEMENT);
                if (e != null) setType(e.getText());
                e = root.getChild(ORIGINATOR_ELEMENT);
                if (e != null) setOriginator(e.getText());
                e = root.getChild(TIER_ELEMENT);
                if (e != null) setTier(e.getText());
                e = root.getChild(REFERENCE_ELEMENT);
                if (e != null) setReference(e.getText());
                e = root.getChild("recipe");
                if (e != null) {
                    appendDetailtext(FSLabels.getLabel("flyeditor.materials"));
                    appendDetailtext(e.getText());
                }
                e = root.getChild(MATERIALS_ELEMENT);
                if (e != null) {
                    appendDetailtext(FSLabels.getLabel("flyeditor.materials"));
                    appendDetailtext(e.getText());
                }
                e = root.getChild(TYING_INSTRUCTIONS_ELEMENT);
                if (e != null) {
                    appendDetailtext("\n");
                    appendDetailtext(FSLabels.getLabel("flyeditor.instructions"));
                    appendDetailtext(e.getText());
                } else {
                    appendDetailtext("\n\n");
                    appendDetailtext(FSLabels.getLabel("flyeditor.instructions"));
                    appendDetailtext("");
                }
                e = root.getChild(OTHER_INFO_ELEMENT);
                if (e != null) {
                    appendDetailtext("\n");
                    appendDetailtext(FSLabels.getLabel("flyeditor.otherInfo"));
                    appendDetailtext(e.getText());
                } else {
                    appendDetailtext("\n\n");
                    appendDetailtext(FSLabels.getLabel("flyeditor.otherInfo"));
                    appendDetailtext("");
                }
                e = root.getChild(SHAREABLE_ELEMENT);
                if (e != null) setShareable(e.getText().equalsIgnoreCase("no") ? false : true); else setShareable(true);
                e = root.getChild(CRC_ELEMENT);
                if (e != null) setCrc(Long.parseLong(e.getText()));
                setHasImage(false);
                e = root.getChild(IMAGE_ELEMENT);
                if (e != null) {
                    setHasImage(true);
                    if (loadImage) {
                        Base64 b64 = new Base64();
                        byte[] buf = e.getText().getBytes();
                        byte[] imgBytes = b64.decode(buf);
                        Image i = Toolkit.getDefaultToolkit().createImage(imgBytes);
                        try {
                            image = new ImagePlus("", i);
                        } catch (Exception ee) {
                            LOG.error(ee);
                        }
                    }
                }
            } catch (JDOMException e) {
                LOG.error(filename, e);
                return false;
            }
        } catch (FileNotFoundException e) {
            LOG.error(filename, e);
            return false;
        } catch (IOException e) {
            LOG.error(filename, e);
            return false;
        }
        return true;
    }

    public boolean matchesFilter(String filter) {
        if (name != null && name.toLowerCase().indexOf(filter) != -1) return true; else if (filename != null && filename.toLowerCase().indexOf(filter) != -1) return true; else if (type != null && type.toLowerCase().indexOf(filter) != -1) return true; else if (originator != null && originator.toLowerCase().indexOf(filter) != -1) return true; else if (tier != null && tier.toLowerCase().indexOf(filter) != -1) return true; else if (reference != null && reference.toLowerCase().indexOf(filter) != -1) return true; else if (detailText != null && detailText.toLowerCase().indexOf(filter) != -1) return true;
        return false;
    }

    public boolean matchesCondition(SmartListModel condition) {
        String value = "";
        String compareValue = condition.getValue().trim().toLowerCase();
        if (condition.getField().equalsIgnoreCase("file name")) value = getFilename(); else if (condition.getField().equalsIgnoreCase("pattern name")) value = getName(); else if (condition.getField().equalsIgnoreCase("created by")) value = getOriginator(); else if (condition.getField().equalsIgnoreCase("tied by")) value = getTier(); else if (condition.getField().equalsIgnoreCase("materials")) value = extractMaterials(); else if (condition.getField().equalsIgnoreCase("tying information")) value = extractTyingInstructions(); else if (condition.getField().equalsIgnoreCase("other information")) value = extractOtherInfo(); else if (condition.getField().equalsIgnoreCase("type")) value = getType(); else if (condition.getField().equalsIgnoreCase("reference")) value = getReference(); else if (condition.getField().equalsIgnoreCase("shared")) value = getShared();
        if (value == null) value = ""; else value = value.toLowerCase();
        if (condition.getCondition().equalsIgnoreCase("contains")) {
            return value.indexOf(compareValue) != -1;
        } else if (condition.getCondition().equalsIgnoreCase("does not contain")) {
            return value.indexOf(compareValue) == -1;
        } else if (condition.getCondition().equalsIgnoreCase("ends with")) {
            return value.endsWith(compareValue);
        } else if (condition.getCondition().equalsIgnoreCase("is")) {
            return value.equalsIgnoreCase(compareValue);
        } else if (condition.getCondition().equalsIgnoreCase("is not")) {
            return !value.equalsIgnoreCase(compareValue);
        } else if (condition.getCondition().equalsIgnoreCase("starts with")) {
            return value.startsWith(compareValue);
        }
        return false;
    }

    /** Make a string of keywords useful for searching.
	 */
    public String genKeywords() {
        StringBuffer data = new StringBuffer();
        StringBuffer keywords = new StringBuffer();
        HashMap map = new HashMap();
        data.append(extractMaterials().trim().toLowerCase());
        data.append(' ');
        data.append(extractTyingInstructions().trim().toLowerCase());
        data.append(' ');
        data.append(extractOtherInfo().trim().toLowerCase());
        StringTokenizer tokenizer = new StringTokenizer(data.toString(), " \r\n\t", false);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token != null && !map.containsKey(token)) map.put(token, token.replaceAll("[^\\w\\.#]", ""));
        }
        keywords.append(getFilename());
        keywords.append(' ');
        Iterator iter = map.values().iterator();
        while (iter.hasNext() && keywords.length() < 500) {
            keywords.append(iter.next());
            keywords.append(' ');
        }
        if (keywords.length() > 500) keywords.setLength(500);
        return keywords.toString();
    }

    public BufferedImage createBufferedImage(Image image) {
        BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.drawImage(image, 0, 0, null);
        return bi;
    }

    public ImagePlus makeThumbnail(int width, int height) {
        if (image == null) return null;
        ImagePlus thumbnail = null;
        if (image.getWidth() > width || image.getHeight() > height) {
            thumbnail = new ImagePlus();
            double aspect;
            if (image.getWidth() > image.getHeight()) aspect = (double) width / (double) image.getWidth(); else aspect = (double) height / (double) image.getHeight();
            thumbnail.setProcessor("", image.getProcessor().resize((int) (image.getWidth() * aspect), (int) (image.getHeight() * aspect)));
        } else {
            thumbnail = new ImagePlus("", image.getImage());
        }
        return thumbnail;
    }

    public String extractMaterials() {
        int start = FSLabels.getLabel("flyeditor.materials").length();
        int end = getDetailText().indexOf(FSLabels.getLabel("flyeditor.instructions")) - 1;
        if (start == -1 || end < 0) return "";
        return getDetailText().substring(start, end);
    }

    public String extractOtherInfo() {
        int start = getDetailText().indexOf(FSLabels.getLabel("flyeditor.otherInfo")) + FSLabels.getLabel("flyeditor.otherInfo").length();
        if (start == -1) return "";
        return getDetailText().substring(start);
    }

    public String extractTyingInstructions() {
        int start = getDetailText().indexOf(FSLabels.getLabel("flyeditor.instructions")) + FSLabels.getLabel("flyeditor.instructions").length();
        if (start == -1) return "";
        int end = getDetailText().indexOf(FSLabels.getLabel("flyeditor.otherInfo")) - 1;
        return getDetailText().substring(start, end);
    }

    public void freeImage() {
        if (getImage() == null) return;
        getImage().getProcessor().reset();
        setImage(null);
    }

    public boolean isInGroup(String field, String value) {
        if (field.equalsIgnoreCase("pattern type") && getType().equalsIgnoreCase(value)) return true; else if (field.equalsIgnoreCase("originator") && getOriginator().equalsIgnoreCase(value)) return true; else if (field.equalsIgnoreCase("tier") && getTier().equalsIgnoreCase(value)) return true;
        return false;
    }

    public String toString() {
        return getName();
    }

    public void copyTextToClipboard() {
        StringBuffer text = new StringBuffer();
        text.append(getName());
        text.append("\n");
        text.append(getType());
        text.append("\n");
        if (getTier().length() > 0) {
            text.append("Tied by ");
            text.append(getTier());
            text.append("\n");
        }
        if (getOriginator().length() > 0) {
            text.append("Created by ");
            text.append(getOriginator());
            text.append("\n");
        }
        text.append(getDetailText());
        text.append("\n");
        text.append(getReference());
        text.append("\n");
        ClipboardUtils.setContents(text, null);
    }

    public void copyImageToClipboard() {
        ImageSelection.copyImageToClipboard(getImage().getImage());
    }
}
