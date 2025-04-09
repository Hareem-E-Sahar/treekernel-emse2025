package gate.creole.measurements;

import java.util.List;

/**
 * A unit, prefix, or function.
 */
abstract class Entity {

    /** Name of this Entity. */
    String name;

    MeasurementsParser gnuUnits;

    /**
   * Constructor.
   * 
   * @param nam
   *          name.
   * @param loc
   *          where defined.
   */
    Entity(final String nam, MeasurementsParser gnuUnits) {
        name = nam;
        this.gnuUnits = gnuUnits;
    }

    /**
   * Check the definition.Used in 'checkunits'.
   */
    abstract void check();

    /**
   * If object defined by this Entity is compatible with Value 'v', add this
   * Entity to 'list'. Used in 'tryallunits'.
   */
    abstract void addtolist(final Measurement v, List<Entity> list);

    /**
   * Return short description of the defined object to be shown by
   * 'tryallunits'.
   */
    abstract String desc();

    /**
   * Inserts this Entity into a given Vector in the increasing alphabetic order
   * of <code>name</code> fields. <br>
   * (This method replaces more advanced features present in the later releases
   * of Java. Remember we are supposed to work under release 1.1.)
   * 
   * @param v
   *          Vector to insert in.
   */
    @SuppressWarnings("unchecked")
    void insertAlph(@SuppressWarnings("rawtypes") List v) {
        int left = 0;
        int right = v.size();
        while (left != right) {
            int middle = (left + right) / 2;
            int c = name.compareTo(((Entity) v.get(middle)).name);
            if (c < 0) right = middle; else left = middle + 1;
        }
        v.add(left, this);
    }
}
