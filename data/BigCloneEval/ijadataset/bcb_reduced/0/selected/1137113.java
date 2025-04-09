package playground.rost.controller.vismodule.implementations;

import java.awt.Color;
import java.awt.Graphics;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.network.NetworkLayer;
import playground.rost.controller.map.BasicMap;
import playground.rost.controller.vismodule.AbstractVisModuleImpl;
import playground.rost.controller.vismodule.VisModuleContainer;

public class LinkVisModule extends AbstractVisModuleImpl {

    protected NetworkLayer network;

    public LinkVisModule(VisModuleContainer vMContainer, NetworkLayer network) {
        super(vMContainer, "LinkView");
        this.network = network;
        this.attributes.put("color", "0xFF0000");
        this.attributes.put("show", "true");
        this.attributes.put("ids", "false");
        this.attributes.put("length", "false");
    }

    @Override
    public void paintGraphics(BasicMap map, Graphics g) {
        boolean show = this.parseBoolean("show", true);
        if (!show) return;
        boolean ids = this.parseBoolean("ids", false);
        boolean length = this.parseBoolean("length", false);
        Color c = this.parseColor("color", Color.red);
        g.setColor(c);
        int size, x, y, x2, y2;
        for (Link link : network.getLinks().values()) {
            if (!map.isVisible(link)) continue;
            x = map.getXonPanel(link.getFromNode());
            y = map.getYonPanel(link.getFromNode());
            x2 = map.getXonPanel(link.getToNode());
            y2 = map.getYonPanel(link.getToNode());
            g.drawLine(x, y, x2, y2);
            if (ids) {
                int mx = (x + x2) / 2, my = (y + y2) / 2;
                g.drawString(link.getId().toString(), mx, my + 10);
            }
            if (length) {
                int mx = (x + x2) / 2, my = (y + y2) / 2;
                g.drawString("" + link.getLength(), mx, my + 5);
            }
        }
    }
}
