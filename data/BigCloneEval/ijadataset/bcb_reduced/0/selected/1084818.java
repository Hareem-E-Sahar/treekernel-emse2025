package org.jpchart.plot.price.renderer;

import org.jpchart.plot.price.*;
import org.jpchart.market.Market;
import org.jpchart.plot.PlotFrame;
import org.jpchart.time.TimeUnit;
import java.awt.Color;
import java.awt.Graphics2D;
import java.math.BigDecimal;

/**
 *
 * @author cfelde
 */
public class LinePriceRenderer implements PricePlotRenderer {

    public static enum PlotTypes {

        OPEN, HIGH, LOW, CLOSE
    }

    ;

    private final PlotTypes plotType;

    private final boolean useColors;

    public LinePriceRenderer(PlotTypes plotType, boolean useColors) {
        this.plotType = plotType;
        this.useColors = useColors;
    }

    public void paint(Graphics2D g, PlotFrame plotFrame) {
        g.setColor(Color.BLACK);
        int barWidth = plotFrame.getBarWidth();
        int xEnd = 1;
        int xStart = 1;
        Market lastMarket = plotFrame.getLastMarket();
        Market currentMarket = null;
        TimeUnit lastVisible = plotFrame.getTimeForX(g.getClipBounds().width);
        try {
            currentMarket = plotFrame.getDataSource().getOnOrBefore(lastMarket.getTicker(), lastVisible);
        } catch (Exception e) {
            currentMarket = lastMarket;
        }
        while (currentMarket != null && xEnd > 0) {
            xEnd = plotFrame.getXForTime(currentMarket.getMarketTime());
            if (xEnd <= g.getClipBounds().width - barWidth) {
                BigDecimal currentPrice = null;
                BigDecimal previousPrice = null;
                Market previousMarket = null;
                try {
                    previousMarket = currentMarket.getPrevious();
                    xStart = plotFrame.getXForTime(previousMarket.getMarketTime());
                } catch (Exception e) {
                    return;
                }
                if (plotType == PlotTypes.OPEN) {
                    currentPrice = currentMarket.getOpenPrice();
                    previousPrice = previousMarket.getOpenPrice();
                } else if (plotType == PlotTypes.HIGH) {
                    currentPrice = currentMarket.getHighPrice();
                    previousPrice = previousMarket.getHighPrice();
                } else if (plotType == PlotTypes.LOW) {
                    currentPrice = currentMarket.getLowPrice();
                    previousPrice = previousMarket.getLowPrice();
                } else if (plotType == PlotTypes.CLOSE) {
                    currentPrice = currentMarket.getClosePrice();
                    previousPrice = previousMarket.getClosePrice();
                } else {
                    return;
                }
                if (useColors) {
                    int rc = currentPrice.compareTo(previousPrice);
                    if (rc > 0) g.setColor(Color.GREEN); else if (rc < 0) g.setColor(Color.RED); else g.setColor(Color.BLACK);
                }
                int currentY = plotFrame.getYForPrice(currentPrice);
                int previousY = plotFrame.getYForPrice(previousPrice);
                g.drawLine(xStart, previousY, xEnd, currentY);
            }
            try {
                currentMarket = currentMarket.getPrevious();
            } catch (Exception e) {
                currentMarket = null;
            }
        }
    }
}
