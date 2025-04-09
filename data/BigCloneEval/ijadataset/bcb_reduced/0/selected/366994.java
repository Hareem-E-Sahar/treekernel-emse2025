package emast.util.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import emast.model.NamedObject;
import emast.model.action.Action;
import emast.model.state.State;
import emast.model.transition.Transition;

/**
 *
 * @author anderson
 */
public class ModelUtils {

    private ModelUtils() {
    }

    public static <E extends NamedObject> List<E> createList(final Class<E> pClass, final int pN) {
        final List<E> list = new ArrayList<E>(pN);
        for (int i = 0; i < pN; i++) {
            try {
                final E e = pClass.getConstructor(int.class).newInstance(i);
                list.add(e);
            } catch (Exception e) {
            }
        }
        return list;
    }

    public static <E extends NamedObject> Set<E> createSet(final Class<E> pClass, final int pN) {
        final Set<E> list = new HashSet<E>(pN);
        for (int i = 0; i < pN; i++) {
            try {
                final E e = pClass.getConstructor(int.class).newInstance(i);
                list.add(e);
            } catch (Exception e) {
            }
        }
        return list;
    }

    public static Set<State> getStates(final Collection<Transition> pPi) {
        final Set<State> list = new HashSet<State>();
        for (final Transition trans : pPi) {
            list.add(trans.getState());
        }
        return list;
    }

    public static Set<List<Action>> getActions(final Collection<Transition> pPi) {
        final Set<List<Action>> result = new HashSet<List<Action>>();
        for (final Transition trans : pPi) {
            result.add(trans.getActions());
        }
        return result;
    }
}
