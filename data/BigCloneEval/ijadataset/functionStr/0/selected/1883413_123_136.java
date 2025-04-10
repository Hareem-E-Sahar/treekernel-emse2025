public class Test {    public void send(IPatchDriver driver, int value) {
        value = Math.min(127, value * multiplier);
        if (reverse) {
            value = 127 - value;
        }
        value = Math.min(127, value + offset);
        ShortMessage m = new ShortMessage();
        try {
            m.setMessage(ShortMessage.CONTROL_CHANGE, driver.getChannel(), param, value);
            driver.send(m);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }
}