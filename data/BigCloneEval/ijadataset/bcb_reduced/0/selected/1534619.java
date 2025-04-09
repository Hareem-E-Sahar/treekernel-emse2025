package Action.mapfilter;

import java.awt.Color;
import java.text.ParseException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * TraitMap�f�[�^�x�[�X�y�щ{���V�X�e��, �Q�O�O�R�N�P��<br>
 * �S���ҁF�L�c�N�Y�A�Q�m���m���x�[�X�����J���`�[���E�C���t�H�}�e�B�N�X��Վ{�݁EGSC�ERIKEN
 * 
 * @version	2.0
 * @author	T.Isobe
 */
public class Dot {

    private Color col;

    private long xpos;

    private long ypos;

    private String xchr;

    private String ychr;

    static Logger log = Logger.getLogger(Dot.class);

    public Dot(Node dot) throws ParseException {
        try {
            Element element = (Element) dot;
            String color = element.getAttribute("color");
            this.paeseColor(color);
            NodeList nlistPos = element.getElementsByTagName("pos");
            String dotx = ((Element) nlistPos.item(0)).getAttribute("x");
            String doty = ((Element) nlistPos.item(1)).getAttribute("x");
            this.parseHead(dotx, "X");
            this.parseHead(doty, "Y");
        } catch (Exception e) {
            log.error("parse error: " + dot.getNodeName());
            throw new ParseException(e.getMessage(), 0);
        }
    }

    public Color getColor() {
        return col;
    }

    public String getXChr() {
        return xchr;
    }

    public String getYChr() {
        return ychr;
    }

    public long getXPos() {
        return xpos;
    }

    public long getYPos() {
        return ypos;
    }

    private void paeseColor(String color) throws ParseException {
        try {
            int r = Integer.parseInt(color.substring(0, 2), 16);
            int g = Integer.parseInt(color.substring(2, 4), 16);
            int b = Integer.parseInt(color.substring(4, 6), 16);
            col = new Color(r, g, b);
        } catch (Exception e) {
            col = new Color(255, 255, 255);
            throw new ParseException(e.getMessage(), 0);
        }
    }

    private void parseHead(String head, String axis) {
        try {
            StringTokenizer st1 = new StringTokenizer(head, ":", true);
            String speciesAlias = st1.nextToken();
            st1.nextToken();
            String chromosome = st1.nextToken();
            if (chromosome.equals(":")) {
                chromosome = "";
            } else {
                st1.nextToken();
            }
            long start = Long.parseLong(st1.nextToken("-"));
            st1.nextToken();
            long end = Long.parseLong(st1.nextToken());
            if (axis.equals("X")) {
                xchr = chromosome;
                xpos = (start + end) / 2;
            } else {
                ychr = chromosome;
                ypos = (start + end) / 2;
            }
        } catch (NoSuchElementException e) {
            log.warn(e);
        } catch (NumberFormatException e) {
            log.warn(e);
        }
    }
}
