package gawky.message.part;

/**
 * Packed Decimal Part
 * 
 * @author Ingo Harbeck
 *
 */
public class DescP extends Desc {

    public DescP(int len, String name, boolean signed) {
        super(Desc.FMT_9, Desc.CODE_R, len, name);
        setPacked(true);
        setUnsigned(!signed);
    }

    public DescP(int len, String name) {
        this(len, name, true);
    }

    private static int packedsize(int integer, int decimal) {
        int size = integer + decimal;
        if (size % 2 == 0) size = (size / 2) + 1; else size = (size + 1) / 2;
        return size;
    }
}
