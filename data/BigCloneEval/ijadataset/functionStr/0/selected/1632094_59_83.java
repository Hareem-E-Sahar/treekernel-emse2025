public class Test {    public void operate(AChannelSelection ch1) {
        MMArray s1 = ch1.getChannel().getSamples();
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        ch1.getChannel().markChange();
        MMArray x = new MMArray(2, 0);
        MMArray y = new MMArray(2, 0);
        x.set(0, 0);
        x.set(1, period * dutyCycle);
        y.set(0, offset + amplitude);
        y.set(1, offset - amplitude);
        MMArray tmp = new MMArray(period, 0);
        for (int i = 0; i < tmp.getLength(); i++) {
            tmp.set(i, AOToolkit.interpolate0(x, y, i));
        }
        for (int i = 0; i < l1; i++) {
            float s = s1.get(o1 + i);
            if (add) {
                s += tmp.get(i % tmp.getLength());
            } else {
                s = tmp.get(i % tmp.getLength());
            }
            s1.set(o1 + i, ch1.mixIntensity(i + o1, s1.get(o1 + i), s));
        }
    }
}