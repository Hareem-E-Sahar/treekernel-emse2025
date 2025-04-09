package org.dreamfly.netshop.common;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * A Servlet to generate a jpeg containing random characeter for verification. <p/> This servlet generate a specified
 * scope (DIGIT_ONLY, ALPHABET_ONLY, DIGIT_ALPHABET) of characters in specified length into a JPEG picture, and then set
 * the generated random string into session attribute '{@code jpeg-random-code}'. <p/> Following parameters can be set
 * in request: <p/> <b>{@code sessionAttrName}</b> the attribute name when the correct code saved in the session,
 * default value is '{@code jpeg-random-code}'. <p/> <b>{@code font}</b> the font of the character of the code in
 * the picture, default to "{@code Times New Roman}". <p/> <b>{@code charScope}</b> the scope of the characters when
 * get random character for the code, which can be set to 0 - only digit, 1 - only alphabets, 2 - both, the default
 * value is 2. <p/> <b>{@code charLength}</b> the length of the random code string, default to 4. <p/> <b>{@code width}</b>
 * the width of the picture output, default to 60. <p/> <b>{@code height}</b> the height of the picture output,
 * default to 20.
 * 
 * @author <A href="mailto:gregory@konlink.com">Gregory Song</A>
 * @version $Revision: 1.4 $
 * @since 2.0
 */
public class JpegRandomCodeServlet extends HttpServlet {

    /**
     * 仅在数字中生成随机码
     */
    public static final int DIGIT_ONLY = 0;

    /**
     * 仅在字母中生成随机码
     */
    public static final int ALPHABET_ONLY = 1;

    /**
     * 在数字和字母中生成随机码
     */
    public static final int DIGIT_ALPHABET = 2;

    /**
     * 可设置的参数，已设置默认值
     */
    private String sessionAttrName = "jpeg-random-code";

    private String font = "Arial";

    private int charScope = DIGIT_ONLY;

    private int charLength = 4;

    private int width = 60;

    private int height = 20;

    /**
     * Called by the server (via the <code>service</code> method) to allow a servlet to handle a GET request.
     * 
     * @param req an {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param res an {@link HttpServletResponse} object that contains the response the servlet sends to the client
     * @exception ServletException if the request for the GET could not be handled
     * @exception IOException if an input or output error is detected when the servlet handles the GET request
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String reqSessionAttrName = req.getParameter("sessionAttrName");
        if (reqSessionAttrName != null && reqSessionAttrName.length() > 0) {
            sessionAttrName = reqSessionAttrName;
        }
        String reqFont = req.getParameter("font");
        if (reqFont != null && reqFont.length() > 0) {
            font = reqFont;
        }
        try {
            String reqScope = req.getParameter("charScope");
            if (reqScope != null && reqScope.length() > 0) {
                charScope = Integer.parseInt(reqScope);
            }
            String reqLength = req.getParameter("charLength");
            if (reqLength != null && reqLength.length() > 0) {
                charLength = Integer.parseInt(reqLength);
            }
            String reqWidth = req.getParameter("width");
            if (reqWidth != null && reqWidth.length() > 0) {
                width = Integer.parseInt(reqWidth);
            }
            String reqHeight = req.getParameter("height");
            if (reqHeight != null && reqHeight.length() > 0) {
                height = Integer.parseInt(reqHeight);
            }
        } catch (NumberFormatException e) {
        }
        generate(req, res);
    }

    private void generate(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setHeader("Pragma", "No-cache");
        res.setHeader("Cache-Control", "no-cache");
        res.setDateHeader("Expires", 0);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Random random = new Random();
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        int fontSize = height - 3;
        Font strFont = new Font(font, Font.BOLD, fontSize);
        g.setFont(strFont);
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 155; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        String sRand = getRandString(charLength);
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
        TextLayout sLayout = new TextLayout(sRand, strFont, frc);
        Rectangle2D sRec = sLayout.getBounds();
        int fontPadding = (int) ((width - sRec.getWidth()) / 2);
        for (int i = 0; i < charLength; i++) {
            String rand = sRand.substring(i, i + 1);
            TextLayout layout = new TextLayout(rand, strFont, frc);
            Rectangle2D rec = layout.getBounds();
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(rand, 13 * i + 6, 16);
            fontPadding += rec.getWidth() + 1;
        }
        req.getSession().setAttribute(sessionAttrName, sRand);
        g.dispose();
        ImageIO.write(image, "JPEG", res.getOutputStream());
    }

    private String getRandString(int length) {
        if (length < 0) {
            return "";
        }
        int randScope, randBase;
        switch(charScope) {
            case DIGIT_ONLY:
                randScope = 9;
                randBase = 0;
                break;
            case ALPHABET_ONLY:
                randScope = 25;
                randBase = 10;
                break;
            case DIGIT_ALPHABET:
                randScope = 35;
                randBase = 0;
                break;
            default:
                randScope = 9;
                randBase = 0;
                break;
        }
        StringBuffer randStr = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
            Random rand = new Random();
            int charOffset = randBase + rand.nextInt(randScope);
            if (charOffset == 18 || charOffset == 24 || charOffset == 26) {
                i--;
                continue;
            }
            char randChar;
            if (charOffset < 10) {
                randChar = (char) (48 + charOffset);
            } else {
                randChar = (char) (65 + charOffset - 10);
            }
            randStr.append(randChar);
        }
        return randStr.toString();
    }

    private static Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int red = fc + random.nextInt(bc - fc);
        int green = fc + random.nextInt(bc - fc);
        int blue = fc + random.nextInt(bc - fc);
        return new Color(red, green, blue);
    }

    /**
     * Called by the server (via the <code>service</code> method) to allow a servlet to handle a POST request.
     * 
     * @param req an {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param res an {@link HttpServletResponse} object that contains the response the servlet sends to the client
     * @exception ServletException if the request for the POST could not be handled
     * @exception IOException if an input or output error is detected when the servlet handles the POST request
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }
}
