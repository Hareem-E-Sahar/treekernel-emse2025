public class Test {    @Test
    public void insertDeriving() {
        addCue();
        lightCues.setChannel(0, 0, 0.5f);
        lightCues.setCueSubmaster(0, 0, 0.7f);
        addCue();
        new CuePrinter(System.out).print(lightCues);
        Cue cue = newCue("Inserted", "L 1");
        LightCueDetail detail = (LightCueDetail) cue.getDetail();
        detail.getChannelLevel(1).setChannelValue(0.3f);
        detail.getChannelLevel(1).setDerived(false);
        detail.getSubmasterLevel(1).getLevelValue().setValue(0.4f);
        detail.getSubmasterLevel(1).setDerived(false);
        lightCues.insert(1, cue);
        new CuePrinter(System.out).print(lightCues);
        assertChannelDerived(0, 0, false);
        assertChannelDerived(1, 0, true);
        assertChannelDerived(2, 0, true);
        assertChannelDerived(0, 1, true);
        assertChannelDerived(1, 1, false);
        assertChannelDerived(2, 1, true);
        assertCueSubmasterDerived(0, 0, false);
        assertCueSubmasterDerived(1, 0, true);
        assertCueSubmasterDerived(2, 0, true);
        assertCueSubmasterDerived(0, 1, true);
        assertCueSubmasterDerived(1, 1, false);
        assertCueSubmasterDerived(2, 1, true);
        assertChannelValue(0, 0, 0.5f);
        assertChannelValue(1, 0, 0.5f);
        assertChannelValue(2, 0, 0.5f);
        assertChannelValue(0, 1, 0.0f);
        assertChannelValue(1, 1, 0.3f);
        assertChannelValue(2, 1, 0.3f);
        assertCueSubmasterValue(0, 0, 0.7f);
        assertCueSubmasterValue(1, 0, 0.7f);
        assertCueSubmasterValue(2, 0, 0.7f);
        assertCueSubmasterValue(0, 1, 0.0f);
        assertCueSubmasterValue(1, 1, 0.4f);
        assertCueSubmasterValue(2, 1, 0.4f);
    }
}