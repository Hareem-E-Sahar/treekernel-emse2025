package help.sortfiles;

import jam.data.Gate;
import jam.data.HistInt1D;
import jam.data.HistInt2D;
import jam.data.Monitor;
import jam.data.Scaler;
import jam.sort.SortException;
import jam.sort.AbstractSortRoutine;

/**
 * This is an example sort routine for Jam. It sorts for a delta-E vs. E
 * telescope. The histograms consist of 1-d histograms for both Delta E and E,
 * and a 2-d histogram which is gated on. The event data are delta-E and E pulse
 * heights. The convention for 2-d Histograms is x first, then y (x vs y).
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version 0.5
 * @since JDK 1.1
 */
public class EvsDE extends AbstractSortRoutine {

    private final transient HistInt1D hEnergy, hDE, hSum, hSumGate;

    private final transient HistInt2D hEvsDE;

    private final transient Gate gEvsDE;

    private transient int idE, idDE;

    /**
	 * Constructor, not usually necessary, but be sure to
	 * call <code>super()</code>.
	 *
	 * @see #initialize()
	 */
    public EvsDE() {
        super();
        final int oneD = 2048;
        final int twoD = 256;
        hEnergy = createHist1D(oneD, "E", "Energy");
        hDE = createHist1D(oneD, "DE", "Delta-E");
        hEvsDE = createHist2D(twoD, "EvsDE", "E vs Delta E", "Energy", "Delta Energy");
        hSum = createHist1D(oneD, "sum", "Energy Sum");
        hSumGate = createHist1D(oneD, "sumGate", "Gated Energy Sum");
        gEvsDE = new Gate("PID", hEvsDE);
        final Scaler sBeam = createScaler("Beam", 0);
        final Scaler sClck = createScaler("Clock", 1);
        final Scaler sEvntRaw = createScaler("Event Raw", 2);
        createScaler("Event Accept", 3);
        new Monitor("Beam ", sBeam);
        new Monitor("Clock", sClck);
        new Monitor("Event Rate", sEvntRaw);
    }

    /**
	 * @see AbstractSortRoutine#initialize()
	 */
    public void initialize() throws SortException {
        cnafCommands.init(1, 28, 8, 26);
        cnafCommands.init(1, 28, 9, 26);
        cnafCommands.init(1, 30, 9, 26);
        cnafCommands.init(1, 3, 12, 11);
        idE = cnafCommands.eventRead(1, 3, 0, 0);
        idDE = cnafCommands.eventRead(1, 3, 1, 0);
        cnafCommands.eventCommand(1, 3, 12, 11);
        cnafCommands.scaler(1, 5, 0, 0);
        cnafCommands.scaler(1, 5, 1, 0);
        cnafCommands.scaler(1, 5, 2, 0);
        cnafCommands.scaler(1, 5, 3, 0);
        cnafCommands.clear(1, 5, 0, 9);
    }

    /**
	 * @see AbstractSortRoutine#sort(int[])
	 */
    public void sort(final int[] dataEvent) {
        final int energy = dataEvent[idE];
        final int eDE = dataEvent[idDE];
        final int ecE = energy >> 3;
        final int ecDE = eDE >> 3;
        final int sum = (energy + eDE) / 2;
        hEnergy.inc(energy);
        hDE.inc(eDE);
        hSum.inc(sum);
        hEvsDE.inc(ecE, ecDE);
        if (gEvsDE.inGate(ecE, ecDE)) {
            hSumGate.inc(sum);
        }
    }
}
