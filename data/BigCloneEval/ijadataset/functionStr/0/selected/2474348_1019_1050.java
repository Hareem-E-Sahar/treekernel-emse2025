public class Test {    public static Icon getEditIcon(int dimension, char letter) {
        BufferedImage image = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradient = new GradientPaint(dimension, 0, ImageCreator.mainDarkColor, 0, dimension, ImageCreator.mainUltraDarkColor);
        graphics.setPaint(gradient);
        Font font = new Font("Arial", Font.BOLD, dimension - 2);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setFont(font);
        letter = Character.toUpperCase(letter);
        int charWidth = graphics.getFontMetrics().charWidth(letter);
        int x = dimension - charWidth - 2;
        FontRenderContext frc = graphics.getFontRenderContext();
        TextLayout mLayout = new TextLayout("" + letter, font, frc);
        Rectangle2D bounds = mLayout.getBounds();
        graphics.drawString("" + letter, x, (int) bounds.getHeight() + 2);
        int xs = 1;
        int xe = xs + CLOSE_DIMENSION;
        int xm = (xs + xe) / 2;
        int ys = ICON_DIMENSION - CLOSE_DIMENSION - 3;
        int ye = ys + CLOSE_DIMENSION;
        int ym = (ys + ye) / 2;
        graphics.setStroke(new BasicStroke(3.5f));
        graphics.setColor(new Color(255, 255, 255, 196));
        graphics.drawLine(xs, ym, xe, ym);
        graphics.drawLine(xm, ys, xm, ye);
        graphics.setStroke(new BasicStroke(2.5f));
        graphics.setColor(ImageCreator.mainMidColor);
        graphics.drawLine(xs, ym, xe, ym);
        graphics.drawLine(xm, ys, xm, ye);
        return new ImageIcon(image);
    }
}