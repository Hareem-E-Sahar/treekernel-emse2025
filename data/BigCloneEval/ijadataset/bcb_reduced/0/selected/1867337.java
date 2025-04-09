package twjcalc.model.calculate;

import twjcalc.model.whistle.Whistle;

public class HBracker extends WhistleCalculator {

    @Override
    protected double embouchureCorrection(final Whistle whistle) {
        final double embHeight = whistle.wallThickness;
        final double bore = whistle.bore;
        final double embWidth = whistle.embouchure.width;
        final double embLength = whistle.embouchure.length;
        final double Bd = (bore * bore) / (embWidth * embLength);
        final double De = (embWidth + embLength) / 2;
        return Bd * (embHeight + 0.3 * De);
    }

    @Override
    protected double endCorrection(final Whistle whistle) {
        return 0.56 * whistle.bore;
    }

    @Override
    public String getName() {
        if (isIterative()) {
            return "HB Flutomat (Iterative)";
        }
        return "HB Flutomat";
    }
}
