package com.voztele.sipspy.flowpanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class CircleArrow extends Arrow {

    public int x = 0;

    public int y = 0;

    public int diameter = 0;

    public CircleArrow(boolean selected, String arrowName, int x, int ymin, int ymax, int diameter, boolean flag) {
        super(selected, arrowName, flag, x, x + diameter, ymin, ymax);
        this.x = xmin;
        this.y = (ymin + ymax) / 2;
        this.diameter = diameter;
    }

    public int xmin() {
        return x;
    }

    public int xmax() {
        return x + diameter;
    }

    public int ymin() {
        return ymin;
    }

    public int ymax() {
        return ymax;
    }

    public void draw(Graphics g) {
        String time = sipMessage.getTime();
        time = time.substring(0, time.length() - 3);
        String fromPort = sipMessage.getFromPort();
        String toPort = sipMessage.getToPort();
        Color c = selected ? Color.red : color;
        g.setColor(c);
        Font font = g.getFont();
        font = new Font(font.getName(), Font.BOLD | Font.ITALIC, 12);
        Font smallFont = new Font(font.getName(), Font.BOLD | Font.ITALIC, 8);
        g.setFont(font);
        g.drawOval(x, y - (diameter / 2), diameter, diameter);
        g.drawOval(x - 1, y - (diameter / 2) - 1, diameter + 2, diameter + 2);
        String timeString;
        gc.setTimeInMillis(Long.parseLong(time));
        timeString = sdf.format(gc.getTime());
        int timeStringWidth = g.getFontMetrics(g.getFont()).stringWidth(timeString);
        int fistLineStringWidth = g.getFontMetrics(g.getFont()).stringWidth(sipMessage.getFirstLine());
        int smallFontHeight = g.getFontMetrics(smallFont).getHeight();
        int fromPortWidth = g.getFontMetrics(smallFont).stringWidth(fromPort);
        g.drawString(sipMessage.getFirstLine(), x + diameter + 5 + StaticTracesCanvas.HORIZONTAL_GAP / 2 - fistLineStringWidth / 2, y - 5);
        g.drawString(timeString, x + diameter + 5 + StaticTracesCanvas.HORIZONTAL_GAP / 2 - timeStringWidth / 2, y + g.getFontMetrics(g.getFont()).getHeight());
        g.setColor(Color.BLACK);
        g.setFont(smallFont);
        g.drawString(fromPort, xmin - fromPortWidth - 1, y + smallFontHeight / 2);
        g.drawString(toPort, xmax, y + smallFontHeight / 2);
        g.setFont(font);
        g.setColor(c);
        g.drawLine(x, y, x - 3, y + 10);
        g.drawLine(x, y, x + 7, y + 7);
        g.drawLine(x - 1, y, x - 4, y + 10);
        g.drawLine(x + 1, y, x + 8, y + 7);
        g.drawLine(x - 2, y, x - 5, y + 10);
        g.drawLine(x + 2, y, x + 9, y + 7);
    }
}
