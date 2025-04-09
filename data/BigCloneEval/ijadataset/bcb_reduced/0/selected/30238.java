package digiscrap;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.JLayeredPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

/**
 *
 * @author william
 */
public class FileHandler extends DefaultHandler {

    String imageName;

    String text;

    String fontName;

    String title;

    int fontSize;

    boolean fontBold;

    boolean fontItalics;

    int colorRed, colorBlue, colorGreen;

    int scale;

    int rotation;

    int xPos;

    int yPos;

    int bgDisplay = 0;

    boolean bgManifested = false;

    JLayeredPane sp;

    String curElement;

    File file;

    /** Creates a new instance of FileHandler */
    public FileHandler(JLayeredPane sp, File file) {
        this.sp = sp;
        this.file = file;
    }

    public JLayeredPane getSP() {
        return sp;
    }

    static void createSaveFile(File file, JLayeredPane sp, String title) throws FileNotFoundException, IOException {
        int bgDisplay = 0;
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file));
        String manifest = "<?xml version='1.0' encoding='utf-8'?>\n" + "<!DOCTYPE slideshow [" + "<!ELEMENT scrapbook (scrappage+)>\n" + "<!ELEMENT scrappage (color, imageelem*, textelem*)>\n" + "<!ELEMENT imageelem (file, scale, rotataion, xpos, ypos)>\n" + "<!ELEMENT textelem (text, fontname, fontsize, fontbold, fontitalis, color, scale, rotataion, xpos, ypos)>\n" + "<!ELEMENT color (red, green, blue)>]>\n" + "<scrapbook>\n" + "<scrappage>\n" + "<title><![CDATA[" + title + "]]></title>\n";
        for (int i = 0; i < sp.getComponentCount(); i++) {
            if (sp.getComponent(i).getClass().getName().equals("digiscrap.ScrapElementImage")) {
                String fName = "images/image" + i + ".png";
                manifest += manifestImage((ScrapElementImage) (sp.getComponent(i)), fName);
                zip.putNextEntry(new ZipEntry(fName));
                ImageIO.write(((ScrapElementImage) (sp.getComponent(i))).getImg(), "png", zip);
            } else if (sp.getComponent(i).getClass().getName().equals("digiscrap.ScrapElementText")) {
                manifest += manifestText((ScrapElementText) (sp.getComponent(i)));
            } else if (sp.getComponent(i).getClass().getName().equals("digiscrap.ScrapPanel")) {
                String fName = "images/background.png";
                bgDisplay = ((ScrapPanel) (sp.getComponent(i))).getBgDisplay();
                manifest += "<bgManifested />";
                zip.putNextEntry(new ZipEntry(fName));
                ImageIO.write(((ScrapPanel) (sp.getComponent(i))).getBgImage(), "png", zip);
            }
        }
        manifest += "<background>\n" + "<color>\n" + "<red>" + sp.getBackground().getRed() + "</red>\n" + "<green>" + sp.getBackground().getGreen() + "</green>\n" + "<blue>" + sp.getBackground().getBlue() + "</blue>\n" + "<bgDisplay>" + bgDisplay + "</bgDisplay>\n" + "</color>\n" + "</background>\n" + "</scrappage>\n" + "</scrapbook>\n";
        zip.putNextEntry(new ZipEntry("digiscrap.xml"));
        zip.write(manifest.getBytes());
        String fName = "previews/preview1.png";
        manifest += "<bgManifested />";
        zip.putNextEntry(new ZipEntry(fName));
        BufferedImage bi = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);
        sp.print(bi.getGraphics());
        ImageIO.write(bi, "png", zip);
        zip.close();
    }

    private static String manifestImage(ScrapElementImage sei, String fName) {
        String retString = "<imageelem>\n";
        retString += "<file>" + fName + "</file>\n";
        retString += "<scale>" + sei.getScale() + "</scale>\n";
        retString += "<rotation>" + sei.getRotation() + "</rotation>\n";
        retString += "<xpos>" + sei.getX() + "</xpos>\n";
        retString += "<ypos>" + sei.getY() + "</ypos>\n";
        retString += "</imageelem>\n";
        return retString;
    }

    private static String manifestText(ScrapElementText set) {
        String retString = "<textelem>\n";
        retString += "<text><![CDATA[" + set.getText() + "]]></text>\n";
        retString += "<fontname>" + set.getFontName() + "</fontname>\n";
        retString += "<fontsize>" + set.getFontSize() + "</fontsize>\n";
        retString += "<fontbold>" + set.isFontBold() + "</fontbold>\n";
        retString += "<fontitalics>" + set.isFontItalics() + "</fontitalics>\n";
        retString += "<color>\n";
        retString += "<red>" + set.getForeground().getRed() + "</red>\n";
        retString += "<green>" + set.getForeground().getGreen() + "</green>\n";
        retString += "<blue>" + set.getForeground().getBlue() + "</blue>\n";
        retString += "</color>\n";
        retString += "<scale>" + set.getScale() + "</scale>\n";
        retString += "<rotation>" + set.getRotation() + "</rotation>\n";
        retString += "<xpos>" + set.getX() + "</xpos>\n";
        retString += "<ypos>" + set.getY() + "</ypos>\n";
        retString += "</textelem>\n";
        return retString;
    }

    public void loadSaveFile() throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {
        ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
        ZipEntry entry = zip.getNextEntry();
        while (!entry.getName().toString().equals("digiscrap.xml")) {
            entry = zip.getNextEntry();
        }
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(zip, new FileHandler(sp, file));
    }

    @Override
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
        curElement = qName;
    }

    @Override
    public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
        if (qName.equals("imageelem")) {
            try {
                ZipInputStream zip;
                zip = new ZipInputStream(new FileInputStream(file));
                ZipEntry entry;
                entry = zip.getNextEntry();
                while (!entry.getName().toString().equals(imageName)) {
                    entry = zip.getNextEntry();
                }
                BufferedImage img;
                img = ImageIO.read(zip);
                ScrapElementImage sei = new ScrapElementImage(img, xPos, yPos);
                sei.setVisible(true);
                sp.add(sei);
                sei.setRotationScale(rotation, scale);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (qName.equals("textelem")) {
            ScrapElementText set = new ScrapElementText();
            set.setVisible(true);
            sp.add(set);
            set.setXY(xPos, yPos);
            set.setText(text);
            set.setFontName(fontName);
            set.setFontSize(fontSize);
            set.setFontBold(fontBold);
            set.setFontItalics(fontItalics);
            set.setForeground(new Color(colorRed, colorGreen, colorBlue));
            set.setRotationScale(rotation, scale);
        } else if (qName.equals("background")) {
            sp.setBackground(new Color(colorRed, colorGreen, colorBlue));
            if (bgManifested) {
                try {
                    ZipInputStream zip;
                    zip = new ZipInputStream(new FileInputStream(file));
                    ZipEntry entry;
                    entry = zip.getNextEntry();
                    while (!entry.getName().toString().equals("images/background.png")) {
                        entry = zip.getNextEntry();
                    }
                    BufferedImage img;
                    img = ImageIO.read(zip);
                    ((DigiScrap) sp.getTopLevelAncestor()).scrapPanelChangeBackgroundImage(img);
                    ((DigiScrap) sp.getTopLevelAncestor()).scrapPanelChangeBackgroundDisplay(bgDisplay);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else if (qName.equals("bgManifested")) {
            bgManifested = true;
        } else if (qName.equals("title")) {
            ((DigiScrap) sp.getTopLevelAncestor()).setPageTitle(title);
        }
    }

    @Override
    public void characters(char buf[], int offset, int len) throws SAXException {
        String input = String.valueOf(buf, offset, len);
        if (curElement == null || input.trim().equals("")) {
        } else if (curElement.equals("text")) {
            text = input;
        } else if (curElement.equals("fontname")) {
            fontName = input;
        } else if (curElement.equals("fontsize")) {
            fontSize = Integer.parseInt(input);
        } else if (curElement.equals("fontbold")) {
            if (input.equals("false")) fontBold = false; else fontBold = true;
        } else if (curElement.equals("fontitalics")) {
            if (input.equals("false")) fontBold = false; else fontBold = true;
        } else if (curElement.equals("red")) {
            colorRed = Integer.parseInt(input);
        } else if (curElement.equals("green")) {
            colorGreen = Integer.parseInt(input);
        } else if (curElement.equals("blue")) {
            colorBlue = Integer.parseInt(input);
        } else if (curElement.equals("scale")) {
            scale = Integer.parseInt(input);
        } else if (curElement.equals("rotation")) {
            rotation = Integer.parseInt(input);
        } else if (curElement.equals("xpos")) {
            xPos = Integer.parseInt(input);
        } else if (curElement.equals("ypos")) {
            yPos = Integer.parseInt(input);
        } else if (curElement.equals("file")) {
            imageName = String.valueOf(buf, offset, len);
        } else if (curElement.equals("title")) {
            title = String.valueOf(buf, offset, len);
        } else if (curElement.equals("bgDisplay")) {
            bgDisplay = Integer.parseInt(input);
        }
    }
}
