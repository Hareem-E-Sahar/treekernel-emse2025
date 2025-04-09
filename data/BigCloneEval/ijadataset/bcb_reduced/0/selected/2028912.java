package org.yehongyu.websale.common.secure;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.yehongyu.websale.AppConstants;

/**
 * ����˵����������ͼƬУ������
 * @author yehongyu.org
 * @version 1.0 2007-11-11 ����10:57:23
 */
public class GetImg extends HttpServlet {

    private String[] fontName = { "Atlantic Inline", "Arial", "Arial Black", "Arial Bold", "Arial Bold Italic", "Arial Italic", "Arial Narrow", "Arial Narrow Bold", "Arial Narrow Bold Italic", "Arial Narrow Italic", "Times New Roman" };

    private char mapTable[] = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    private int codenum[] = { 2, 3, 4 };

    private int picnum = 4;

    private int codewidth = 15;

    private int[] codepos;

    /**
	 * �������ܡ�����ȡ���λ�����֤�룬���趨�����ʾλ��
	 * @return����֤��
	 */
    private String getCode() {
        String code = "";
        int num = codenum[(int) (codenum.length * Math.random())];
        for (int i = 0; i < num; ++i) {
            code += mapTable[(int) (mapTable.length * Math.random())];
        }
        List<Integer> pos = new ArrayList<Integer>();
        int p;
        for (int i = 0; i < num; i++) {
            do {
                p = (int) (picnum * Math.random());
            } while (pos.contains(p));
            pos.add(p);
        }
        int[] ordpos = new int[num];
        Object[] objs = pos.toArray();
        for (int i = 0; i < num; i++) {
            ordpos[i] = (Integer) objs[i];
        }
        sortInt(ordpos);
        codepos = new int[num];
        for (int i = 0; i < ordpos.length; i++) {
            codepos[i] = ordpos[i] * codewidth;
        }
        return code;
    }

    /**
	 * �������ܡ�������������С��������
	 * @param ordpos
	 */
    private void sortInt(int[] ordpos) {
        for (int j = 0; j < ordpos.length; j++) {
            for (int k = 0; k < ordpos.length - 1; k++) {
                if (ordpos[k + 1] < ordpos[k]) {
                    int temp = ordpos[k + 1];
                    ordpos[k + 1] = ordpos[k];
                    ordpos[k] = temp;
                }
            }
        }
    }

    /**
	 * �������ܡ���Χ��ȡ�������
	 * @param min
	 * @param max
	 * @return
	 */
    private int getRandNum(int min, int max) {
        return min + new Random().nextInt(max - min);
    }

    /**
	 * �������ܡ���ȡ�������
	 * @return
	 */
    private Font getRandFont() {
        String fontn = "Times New Roman";
        fontn = fontName[(int) (fontName.length * Math.random())];
        return new Font(fontn, getRandNum(0, 3), getRandNum(15, 20));
    }

    /**
	 * �������ܡ���Χ��������ɫ
	 * @param fc
	 * @param bc
	 * @return Color
	 */
    private Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    /**
	 * ����:��ɲ�ɫ��֤��ͼƬ ����widthΪ���ͼƬ�Ŀ��,����heightΪ���ͼƬ�ĸ߶�,����osΪҳ��������
	 */
    public String getCertPic(OutputStream os) {
        String strEnsure = getCode();
        char[] codes = strEnsure.toCharArray();
        int width = codewidth * picnum;
        int height = 20;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(getRandColor(160, 200));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(0, 0, width - 1, height - 1);
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            g.setColor(getRandColor(160, 200));
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            g.drawOval(x, y, 1, 1);
        }
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 155; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            int xl = rand.nextInt(12);
            int yl = rand.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        g.setColor(getRandColor(10, 50));
        for (int i = 0; i < 3; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            int xl = rand.nextInt(60);
            int yl = rand.nextInt(20);
            g.drawLine(x, y, x + xl, y + yl);
        }
        for (int c = 0; c < codes.length; c++) {
            g.setColor(getRandColor(10, 50));
            g.setFont(getRandFont());
            String str = String.valueOf(codes[c]);
            g.drawString(str, codepos[c] + 2, getRandNum(13, 18));
        }
        g.dispose();
        try {
            ImageIO.write(image, "JPEG", os);
        } catch (IOException e) {
            return "";
        }
        return strEnsure;
    }

    /**
	 * ����Post����
	 */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    /**
	 * ����Get����
	 */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.removeAttribute(AppConstants.VERIFYCODE);
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        String verifycode = getCertPic(response.getOutputStream());
        session.setAttribute(AppConstants.VERIFYCODE, verifycode);
    }

    /**
	 * �������ܡ���ȡ����֧������
	 * @return �������
	 */
    public String getFont() {
        String s = "";
        Font[] f = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (Font font : f) {
            s += "\"" + font.getFontName() + "\",";
        }
        return s;
    }
}
