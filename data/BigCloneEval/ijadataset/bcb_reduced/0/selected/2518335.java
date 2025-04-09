package world.unit;

import java.lang.reflect.Constructor;
import world.owner.Owner;

/**
 * specifies a build order, must be registered with the 
 * unit engine in order to actually make something
 * @author Jack
 *
 */
public final class BuildOrder {

    private double buildTime;

    private double time = 0;

    private Unit builder;

    private Class<? extends Unit> u;

    private boolean cancel = false;

    boolean complete = false;

    /**
	 * creates a new build oder
	 * @param u the unit to create
	 * @param the unit that initiated the build order
	 */
    public BuildOrder(double buildTime, Class<? extends Unit> u, Unit builder) {
        this.u = u;
        this.builder = builder;
        this.buildTime = buildTime;
    }

    /**
	 * updates the build order, increments the timer
	 * @param tdiff
	 * @param ue
	 * @return returns returns true when the order is to be deleted,
	 * true when the order is carried out and the unit is created or
	 * when the build order is canceled, false otherwise
	 */
    public boolean update(double tdiff, UnitEngine ue) {
        if (cancel) {
            complete = true;
            return true;
        }
        time += tdiff;
        if (time >= buildTime) {
            Class<?>[] arguments = { Owner.class, double.class, double.class };
            try {
                Object[] parameters = { builder.getOwner(), builder.getLocation()[0], builder.getLocation()[1] };
                Constructor<? extends Unit> c = u.getConstructor(arguments);
                Unit u = c.newInstance(parameters);
                ue.registerUnit(u);
            } catch (Exception e) {
                e.printStackTrace();
            }
            complete = true;
            return true;
        }
        return false;
    }

    /**
	 * cancels the build order, no unit will be built, the build order
	 * will mark itself for deletion next update
	 */
    public void cancel() {
        cancel = true;
    }

    /**
	 * determines whether the build order has been carried out or canceled or
	 * is still functioning
	 * @return returns true if this build order is complete or canceled, false otherwise
	 */
    public boolean isComplete() {
        return complete;
    }
}
