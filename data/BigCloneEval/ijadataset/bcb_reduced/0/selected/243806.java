package com.voztele.sipspy.flowpanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class StraightArrow extends Arrow {

    public Dimension dimensionInfo;

    public StraightArrow(boolean selected, String arrowName, int xmin, int xmax, int ymin, int ymax, boolean flag) {
        super(selected, arrowName, flag, xmin, xmax, ymin, ymax);
    }

    public int xmin() {
        return Math.min(xmin, xmax);
    }

    public int xmax() {
        return Math.max(xmin, xmax);
    }

    public int ymin() {
        return Math.min(ymin, ymax);
    }

    public int ymax() {
        return Math.max(ymin, ymax);
    }

    public void draw(Graphics g) {
        String time = sipMessage.getTime();
        time = time.substring(0, time.length() - 3);
        String fromPort = "Port: " + sipMessage.getFromPort();
        String toPort = "Port: " + sipMessage.getToPort();
        Color c = selected ? Color.red : color;
        g.setColor(c);
        Font font = g.getFont();
        font = new Font(font.getName(), Font.BOLD, 12);
        Font smallFont = new Font(font.getName(), Font.BOLD, 10);
        g.setFont(font);
        int y = (ymin + ymax) / 2;
        g.drawLine(Math.min(xmin, xmax), y - 1, Math.max(xmin, xmax), y - 1);
        g.drawLine(Math.min(xmin, xmax), y, Math.max(xmin, xmax), y);
        g.drawLine(Math.min(xmin, xmax), y + 1, Math.max(xmax, xmax), y + 1);
        String timeString;
        gc.setTimeInMillis(Long.parseLong(time));
        timeString = sdf.format(gc.getTime());
        int timeStringWidth = g.getFontMetrics(g.getFont()).stringWidth(timeString);
        int fistLineStringWidth = g.getFontMetrics(g.getFont()).stringWidth(sipMessage.getFirstLine());
        int smallFontHeight = g.getFontMetrics(smallFont).getHeight();
        int fromPortWidth = g.getFontMetrics(smallFont).stringWidth(fromPort);
        int toPortWidth = g.getFontMetrics(smallFont).stringWidth(toPort);
        if (xmax > xmin) {
            g.setColor(Color.BLACK);
            g.setFont(smallFont);
            g.drawString(fromPort, xmin - fromPortWidth - 1, y + smallFontHeight / 2);
            g.drawString(" " + toPort, xmax, y + smallFontHeight / 2);
            g.setFont(font);
            g.setColor(c);
            g.drawString(sipMessage.getFirstLine(), xmin + StaticTracesCanvas.HORIZONTAL_GAP / 2 - fistLineStringWidth / 2, y - 5);
            g.drawString(timeString, xmin + StaticTracesCanvas.HORIZONTAL_GAP / 2 - timeStringWidth / 2, y + g.getFontMetrics(g.getFont()).getHeight());
            g.drawLine(xmax, y, xmax - 10, y - 5);
            g.drawLine(xmax - 1, y, xmax - 11, y - 5);
            g.drawLine(xmax - 2, y, xmax - 12, y - 5);
            g.drawLine(xmax, y, xmax - 10, y + 5);
            g.drawLine(xmax - 1, y, xmax - 11, y + 5);
            g.drawLine(xmax - 2, y, xmax - 12, y + 5);
        } else {
            g.setColor(Color.BLACK);
            g.setFont(smallFont);
            g.drawString(toPort, xmax - toPortWidth - 1, y + smallFontHeight / 2);
            g.drawString(" " + fromPort, xmin, y + smallFontHeight / 2);
            g.setFont(font);
            g.setColor(c);
            g.drawString(sipMessage.getFirstLine(), xmax + StaticTracesCanvas.HORIZONTAL_GAP / 2 - fistLineStringWidth / 2, y - 2);
            g.drawString(timeString, xmax + StaticTracesCanvas.HORIZONTAL_GAP / 2 - timeStringWidth / 2, y + 2 + g.getFontMetrics(g.getFont()).getHeight());
            g.drawLine(xmax, y, xmax + 10, y + 5);
            g.drawLine(xmax + 1, y, xmax + 11, y + 5);
            g.drawLine(xmax + 2, y, xmax + 12, y + 5);
            g.drawLine(xmax, y, xmax + 10, y - 5);
            g.drawLine(xmax + 1, y, xmax + 11, y - 5);
            g.drawLine(xmax + 2, y, xmax + 12, y - 5);
        }
    }
}
