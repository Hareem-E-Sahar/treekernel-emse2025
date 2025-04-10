package cn.jsprun.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SeccodeBuild extends HttpServlet {

    private static final long serialVersionUID = -205545450335033053L;

    public void destroy() {
        super.destroy();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getImage(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public void init() throws ServletException {
    }

    public void getImage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> settingMap = ForumInit.settings;
        String seccodedataString = settingMap.get("seccodedata");
        Map<String, Object> seccodedata = ((DataParse) BeanFactory.getBean("dataParse")).characterParse(seccodedataString, false);
        String widthString = String.valueOf(seccodedata.get("width"));
        String heightString = String.valueOf(seccodedata.get("height"));
        int width = Integer.parseInt(widthString);
        int height = Integer.parseInt(heightString);
        int fontSize = width / 4;
        int wordPlace = (height / 2 + fontSize / 3);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Random random = new Random();
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("����", Font.PLAIN, fontSize));
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 155; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(width);
            int yl = random.nextInt(height);
            g.drawLine(x, y, xl, yl);
        }
        String str = "ABCDEFGHIGKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String sRand = "";
        for (int i = 0; i < 4; i++) {
            String rand = String.valueOf(str.charAt(random.nextInt(62)));
            sRand += rand;
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(rand, fontSize * i + fontSize / 4, wordPlace);
        }
        g.dispose();
        HttpSession session = request.getSession(true);
        session.setAttribute("rand", sRand);
        ImageIO.write(image, "JPEG", response.getOutputStream());
        image = null;
    }

    Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
