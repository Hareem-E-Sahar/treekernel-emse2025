package g1105.ps.servlet;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.http.*;

public class ValidationCodeServlet extends HttpServlet {

    private static String codeChars = null;

    private static Color getRandomColor(int minColor, int maxColor) {
        Random random = new Random();
        int red = minColor + random.nextInt(maxColor - minColor);
        int green = minColor + random.nextInt(maxColor - minColor);
        int blue = minColor + random.nextInt(maxColor - minColor);
        return new Color(red, green, blue);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (codeChars == null) {
            FileInputStream fis = new FileInputStream(this.getServletContext().getRealPath("/WEB-INF/code.txt"));
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String s = "";
            while ((s = br.readLine()) != null) {
                codeChars = s;
            }
        }
        int charsLength = codeChars.length();
        response.setHeader("ragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        int width = 150, height = 30;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Random random = new Random();
        g.setColor(getRandomColor(190, 250));
        g.fillRect(0, 0, width, height);
        g.setColor(getRandomColor(80, 160));
        StringBuilder validationCode = new StringBuilder();
        String[] fontNames = new String[] { "����", "����", "����" };
        for (int i = 0; i < 3 + random.nextInt(3); i++) {
            g.setFont(new Font(fontNames[random.nextInt(3)], Font.ITALIC, height));
            char codeChar = codeChars.charAt(random.nextInt(charsLength));
            validationCode.append(codeChar);
            g.setColor(getRandomColor(10, 100));
            g.drawString(String.valueOf(codeChar), 25 * i + 6, height - random.nextInt(5));
        }
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(30 * 60);
        session.setAttribute("validationCode", validationCode.toString());
        g.dispose();
        OutputStream os = response.getOutputStream();
        ImageIO.write(image, "JPEG", os);
    }
}
