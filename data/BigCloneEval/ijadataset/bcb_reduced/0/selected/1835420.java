package jonelo.jacksum.adapt.gnu.crypto.hash;

import jonelo.jacksum.adapt.gnu.crypto.Registry;
import jonelo.jacksum.adapt.gnu.crypto.util.Util;

public class Tiger160 extends Tiger {

    /** Result when no data has been input. */
    private static final String DIGEST0 = "3293AC630C13F0245F92BBB1766E16167A4E5849";

    /**
    * Trivial 0-arguments constructor.
    */
    public Tiger160() {
        super();
        name = Registry.TIGER160_HASH;
    }

    /**
    * Private copying constructor for cloning.
    *
    * @param that The instance being cloned.
    */
    private Tiger160(Tiger160 that) {
        this();
        this.a = that.a;
        this.b = that.b;
        this.c = that.c;
        this.count = that.count;
        this.buffer = (that.buffer != null) ? (byte[]) that.buffer.clone() : null;
    }

    public Object clone() {
        return new Tiger160(this);
    }

    public boolean selfTest() {
        if (valid == null) {
            valid = new Boolean(DIGEST0.equals(Util.toString(new Tiger160().digest())));
        }
        return valid.booleanValue();
    }

    protected byte[] getResult() {
        return new byte[] { (byte) a, (byte) (a >>> 8), (byte) (a >>> 16), (byte) (a >>> 24), (byte) (a >>> 32), (byte) (a >>> 40), (byte) (a >>> 48), (byte) (a >>> 56), (byte) b, (byte) (b >>> 8), (byte) (b >>> 16), (byte) (b >>> 24), (byte) (b >>> 32), (byte) (b >>> 40), (byte) (b >>> 48), (byte) (b >>> 56), (byte) c, (byte) (c >>> 8), (byte) (c >>> 16), (byte) (c >>> 24) };
    }
}
