package net.flysource.client.gui.matedit;

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
import java.util.zip.CRC32;

public class MaterialModel extends YModel implements Filterable, Syncable {

    Logger LOG = Logger.getLogger(MaterialModel.class);

    private final String NAME_ELEMENT = "name";

    private final String CATEGORY_ELEMENT = "category";

    private final String MANUFACTURER_ELEMENT = "manufacturer";

    private final String PART_NUM_ELEMENT = "partnumber";

    private final String REFERENCE_ELEMENT = "reference";

    private final String NOTES_ELEMENT = "notes";

    private final String SIZE_ELEMENT = "size";

    private final String COLOR_ELEMENT = "color";

    private final String DATE_ELEMENT = "date";

    private final String QTY_ELEMENT = "quantity";

    private final String COST_ELEMENT = "cost";

    private final String IMAGE_ELEMENT = "image";

    private final String CRC_ELEMENT = "crc";

    private final String SHAREABLE_ELEMENT = "shareable";

    public static final int NAME_LEN = 80;

    public static final int CATEGORY_LEN = 40;

    public static final int MANUFACTURER_LEN = 80;

    public static final int PART_NUM_LEN = 40;

    public static final int REFERENCE_LEN = 255;

    public static final int SIZE_LEN = 20;

    public static final int COLOR_LEN = 40;

    public static final int DATE_LEN = 16;

    public static final int QTY_LEN = 20;

    public static final int COST_LEN = 6;

    public static final int NOTES_LEN = 16000;

    public static final int FILENAME_LEN = 40;

    private boolean isNew;

    private boolean isSentToServer;

    private boolean isChangeSync;

    private String originalFilename;

    private String filename = "";

    private String name = "";

    private String category = "";

    private String manufacturer = "";

    private String partNum = "";

    private String supplier = "";

    private String notes;

    private String size;

    private String color;

    private String date;

    private String qty;

    private String cost;

    private boolean shareable;

    private boolean hasImage;

    private ImagePlus image = null;

    private long crc;

    public MaterialModel() {
        isNew = true;
        isSentToServer = false;
        originalFilename = "";
        name = "";
        category = "";
        filename = "";
        manufacturer = "";
        partNum = "";
        notes = "";
        size = "";
        color = "";
        date = "";
        qty = "";
        cost = "";
        supplier = "";
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

    public String getCategory() {
        if (category == null) return "";
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
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

    public String getManufacturer() {
        if (manufacturer == null) return "";
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getPartNum() {
        if (partNum == null) return "";
        return partNum;
    }

    public void setPartNum(String partNum) {
        this.partNum = partNum;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getSupplier() {
        if (supplier == null) return "";
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
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
        setManufacturer(StringUtils.trimToEmpty(getManufacturer()));
        setSupplier(StringUtils.trimToEmpty(getSupplier()));
        setPartNum(StringUtils.trimToEmpty(getPartNum()));
        setCategory(StringUtils.trimToEmpty(getCategory()));
        setSize(StringUtils.trimToEmpty(getSize()));
        setColor(StringUtils.trimToEmpty(getColor()));
        setDate(StringUtils.trimToEmpty(getDate()));
        setQty(StringUtils.trimToEmpty(getQty()));
        setCost(StringUtils.trimToEmpty(getCost()));
        String fn = StringUtils.trimToEmpty(filename);
        String ext = FileUtils.getExtension(fn);
        if (fn.length() == 0) {
            fn = FileUtils.makeNiceFilename(FSConfig.getMatStore(), getName() + getManufacturer() + getPartNum(), ".mat");
            setFilename(fn);
        }
        fn = FileUtils.removeExtension(fn);
        if (!StringUtils.isAlphanumericSpace(fn)) throw new RequiredFieldException(FSMessages.getMessage("material.badChars"), "filename"); else if (StringUtils.isEmpty(StringUtils.trimToEmpty(name))) throw new RequiredFieldException(FSMessages.getMessage("material.nameRequired"), "name"); else if (StringUtils.isEmpty(category)) throw new RequiredFieldException(FSMessages.getMessage("material.typeRequired"), "category"); else if (category.length() > MaterialModel.CATEGORY_LEN) throw new RequiredFieldException(FSMessages.getMessage("material.typeTooLong"), "category"); else if (StringUtils.isEmpty(fn)) throw new RequiredFieldException(FSMessages.getMessage("material.filenameRequired"), "filename"); else if (!ext.equalsIgnoreCase(".mat") && fn.length() >= MaterialModel.NAME_LEN - 3) throw new RequiredFieldException(FSMessages.getMessage("material.filenameTooLong"), "filename");
        if (!ext.equalsIgnoreCase(".mat")) {
            setFilename(fn + ".mat");
        }
    }

    public void save(String path) throws CantSaveException, InvalidFileException, CantReplaceFileException {
        CRC32 crc = new CRC32();
        File file = new File(path + File.separator + filename);
        FileUtils.isFileValid(file);
        if (isNew) {
            if (FileUtils.fileExists(path, filename)) {
                throw new CantReplaceFileException(FSMessages.getMessage("material.alreadyExists"), "filename");
            }
        }
        if (!isNew && !originalFilename.equalsIgnoreCase(filename)) {
            if (FileUtils.fileExists(path, filename)) {
                throw new CantReplaceFileException(FSMessages.getMessage("material.alreadyExists"), "filename");
            }
        }
        Document doc = new Document();
        doc.addContent(new Comment("This file was created by FlySource. (www.flysource.net)"));
        doc.setRootElement(new Element("FlyTyingMaterial"));
        Element root = doc.getRootElement();
        root.setAttribute("version", FlyShareApp.MATERIAL_VERSION);
        Element el = new Element(NAME_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getName()));
        root.addContent(el);
        el = new Element(CATEGORY_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getCategory()));
        root.addContent(el);
        el = new Element(MANUFACTURER_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getManufacturer()));
        root.addContent(el);
        el = new Element(PART_NUM_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getPartNum()));
        root.addContent(el);
        el = new Element(REFERENCE_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getSupplier()));
        root.addContent(el);
        el = new Element(NOTES_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getNotes()));
        root.addContent(el);
        el = new Element(SIZE_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getSize()));
        root.addContent(el);
        el = new Element(COLOR_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getColor()));
        root.addContent(el);
        el = new Element(DATE_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getDate()));
        root.addContent(el);
        el = new Element(QTY_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getQty()));
        root.addContent(el);
        el = new Element(COST_ELEMENT);
        el.setText(StringUtils.trimToEmpty(getCost()));
        root.addContent(el);
        el = new Element(SHAREABLE_ELEMENT);
        el.setText(isShareable() ? "Yes" : "No");
        root.addContent(el);
        crc.update(FlyShareApp.MATERIAL_VERSION.getBytes());
        crc.update(name.getBytes());
        crc.update(category.getBytes());
        if (manufacturer != null) crc.update(manufacturer.getBytes());
        if (notes != null) crc.update(notes.getBytes());
        if (supplier != null) crc.update(supplier.getBytes());
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
                setNotes("");
                setNew(false);
                Document doc = parser.build(file);
                Element root = doc.getRootElement();
                Element e = root.getChild(NAME_ELEMENT);
                if (e != null) setName(e.getText());
                e = root.getChild(CATEGORY_ELEMENT);
                if (e != null) setCategory(e.getText());
                e = root.getChild(MANUFACTURER_ELEMENT);
                if (e != null) setManufacturer(e.getText());
                e = root.getChild(PART_NUM_ELEMENT);
                if (e != null) setPartNum(e.getText());
                e = root.getChild(REFERENCE_ELEMENT);
                if (e != null) setSupplier(e.getText());
                e = root.getChild(SIZE_ELEMENT);
                if (e != null) {
                    setSize(e.getText());
                }
                e = root.getChild(COLOR_ELEMENT);
                if (e != null) {
                    setColor(e.getText());
                }
                e = root.getChild(QTY_ELEMENT);
                if (e != null) {
                    setQty(e.getText());
                }
                e = root.getChild(DATE_ELEMENT);
                if (e != null) {
                    setDate(e.getText());
                }
                e = root.getChild(COST_ELEMENT);
                if (e != null) {
                    setCost(e.getText());
                }
                e = root.getChild(NOTES_ELEMENT);
                if (e != null) {
                    setNotes(e.getText());
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
        if (name != null && name.toLowerCase().indexOf(filter) != -1) return true; else if (filename != null && filename.toLowerCase().indexOf(filter) != -1) return true; else if (category != null && category.toLowerCase().indexOf(filter) != -1) return true; else if (manufacturer != null && manufacturer.toLowerCase().indexOf(filter) != -1) return true; else if (partNum != null && partNum.toLowerCase().indexOf(filter) != -1) return true; else if (notes != null && notes.toLowerCase().indexOf(filter) != -1) return true;
        return false;
    }

    public boolean matchesCondition(SmartListModel condition) {
        String value = "";
        String compareValue = condition.getValue().trim().toLowerCase();
        if (condition.getField().equalsIgnoreCase("file name")) value = getFilename(); else if (condition.getField().equalsIgnoreCase("pattern name")) value = getName(); else if (condition.getField().equalsIgnoreCase("description")) value = getManufacturer(); else if (condition.getField().equalsIgnoreCase("tied by")) value = getManufacturer(); else if (condition.getField().equalsIgnoreCase("type")) value = getCategory(); else if (condition.getField().equalsIgnoreCase("reference")) value = getSupplier();
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

    public void freeImage() {
        if (getImage() == null) return;
        getImage().getProcessor().reset();
        setImage(null);
    }

    public void copyTextToClipboard() {
        StringBuffer text = new StringBuffer();
        text.append(StringUtilities.makeLabeledString("", getName(), "\n"));
        text.append(StringUtilities.makeLabeledString("", getCategory(), "\n"));
        text.append(StringUtilities.makeLabeledString("Manufacturer\t", getManufacturer(), "\n"));
        text.append(StringUtilities.makeLabeledString("Ref/Part#\t", getPartNum(), "\n"));
        text.append(StringUtilities.makeLabeledString("Size\t\t", getSize(), "\n"));
        text.append(StringUtilities.makeLabeledString("Color\t\t", getColor(), "\n"));
        text.append(StringUtilities.makeLabeledString("Quantity\t", getQty(), "\n"));
        text.append(StringUtilities.makeLabeledString("Supplier/Reference\t", getSupplier(), "\n"));
        text.append(StringUtilities.makeLabeledString("Cost\t\t", getCost(), "\n"));
        text.append(StringUtilities.makeLabeledString("Date\t\t", getDate(), "\n"));
        text.append(StringUtilities.makeLabeledString("Notes\t\t", getNotes(), "\n"));
        text.append("\n");
        ClipboardUtils.setContents(text, null);
    }

    public void copyImageToClipboard() {
        ImageSelection.copyImageToClipboard(getImage().getImage());
    }
}
