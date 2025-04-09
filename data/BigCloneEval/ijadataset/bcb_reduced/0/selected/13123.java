package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.data.BadFormException;
import visad.VisADException;

/**
 * Provides support for processing unsigned integer values in a DODS
 * dataset.  Processing includes checking for validity and unpacking.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class UIntValuator extends IntValuator {

    private final float floatFold;

    private final double doubleFold;

    /**
     * Constructs from the attributes of a DODS variable.
     *
     * @param table		The attribute table for a DODS variable.
     * @param upper		Natural upper limit on packed values.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    protected UIntValuator(AttributeTable table, long upper) throws BadFormException, VisADException, RemoteException {
        super(table, 0, upper);
        floatFold = (upper + 1) / 2;
        doubleFold = (upper + 1) / 2;
    }

    /**
     * Processes a value.
     *
     * @param value		The packed value to be processed.
     */
    public float process(float value) {
        return super.process(value < 0 ? value + floatFold : value);
    }

    /**
     * Processes a value.
     *
     * @param value		The packed value to be processed.
     */
    public float[] process(float[] values) {
        for (int i = 0; i < values.length; ++i) if (values[i] < 0) values[i] += floatFold;
        return super.process(values);
    }

    /**
     * Processes values.
     *
     * @param values		The packed values to be processed.
     */
    public double process(double value) {
        return super.process(value < 0 ? value + doubleFold : value);
    }

    /**
     * Processes values.
     *
     * @param values		The packed values to be processed.
     */
    public double[] process(double[] values) {
        for (int i = 0; i < values.length; ++i) if (values[i] < 0) values[i] += doubleFold;
        return super.process(values);
    }
}
