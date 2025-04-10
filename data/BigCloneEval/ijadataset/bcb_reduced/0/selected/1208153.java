package net.sourceforge.plantuml.sequencediagram.graphic;

import java.awt.geom.Dimension2D;
import net.sourceforge.plantuml.Dimension2DDouble;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.skin.Area;
import net.sourceforge.plantuml.skin.Component;
import net.sourceforge.plantuml.skin.Context2D;
import net.sourceforge.plantuml.ugraphic.UGraphic;

class GraphicalDelayText extends GraphicalElement {

    private final Component compText;

    private final ParticipantBox p1;

    private final ParticipantBox p2;

    public GraphicalDelayText(double startingY, Component compText, ParticipantBox first, ParticipantBox last) {
        super(startingY);
        this.compText = compText;
        this.p1 = first;
        this.p2 = last;
    }

    @Override
    protected void drawInternalU(UGraphic ug, double maxX, Context2D context) {
        final StringBounder stringBounder = ug.getStringBounder();
        final double x1 = p1.getCenterX(stringBounder);
        final double x2 = p2.getCenterX(stringBounder);
        final double middle = (x1 + x2) / 2;
        final double textWidth = compText.getPreferredWidth(stringBounder);
        ug.translate(middle - textWidth / 2, getStartingY());
        final Dimension2D dim = new Dimension2DDouble(textWidth, compText.getPreferredHeight(stringBounder));
        compText.drawU(ug, new Area(dim), context);
    }

    @Override
    public double getPreferredHeight(StringBounder stringBounder) {
        return compText.getPreferredHeight(stringBounder);
    }

    @Override
    public double getPreferredWidth(StringBounder stringBounder) {
        return compText.getPreferredWidth(stringBounder);
    }

    @Override
    public double getStartingX(StringBounder stringBounder) {
        return 0;
    }

    public double getEndingY(StringBounder stringBounder) {
        return getStartingY() + compText.getPreferredHeight(stringBounder);
    }
}
