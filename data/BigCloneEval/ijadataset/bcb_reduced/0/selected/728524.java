package Core;

import Datatypes.Buddy;
import Datatypes.ErrCode;
import Datatypes.Constants;
import java.util.Vector;
import Interfaces.IntCore;

/**
 * Stores a variable array of Buddys and provides methods to access and alter them.
 * @author Christian Reuther
 */
public class Buddylist {

    private Vector buddys;

    /**
     * Initializes the Vector holding the buddies
     */
    public Buddylist() {
        buddys = new Vector();
    }

    /**
     * Adds a new buddy or updates an existing buddy in the buddylist. Either
     * way, the buddy ends up being sorted lexiographically.
     * @param object The buddy to be added
     * @return Whether or not it worked
     */
    public String addBuddy(Buddy object) {
        String old_buddy_name = "";
        for (int i = 0; i < buddys.size(); i++) {
            Buddy buddy = (Buddy) (buddys.elementAt(i));
            if (buddy.getAddress().equals(object.getAddress())) {
                old_buddy_name = buddy.getName();
                removeBuddy(buddy);
            }
        }
        if (IntCore.getInstance().getFriendlyName().equals(object.getName())) {
            IntCore.getInstance().changeFriendlyNameToBTAddress();
        }
        int insert_at = this.getBuddyListPosition(object, 0, buddys.size() - 1);
        buddys.insertElementAt(object, insert_at);
        return old_buddy_name;
    }

    /**
     * Enables Network to remove a buddy, e.g. when a user shuts his cellphone
     * off. If the buddy to be removed doesn't exist in the buddylist, nothing
     * happens.
     * @param object Buddy to be removed (by Object)
     * @return
     */
    public ErrCode removeBuddy(Buddy object) {
        try {
            for (int i = 0; ; i++) {
                Buddy compare = (Buddy) buddys.elementAt(i);
                if (object.getAddress().equals(compare.getAddress())) {
                    buddys.removeElementAt(i);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return new ErrCode(Constants.ERR_SUCCESS);
    }

    /**
     * Enables Network to remove a buddy, e.g. when a user shuts his cellphone
     * off. If the buddy to be removed doesn't exist in the buddylist, nothing
     * happens.
     * @param address
     * @return
     */
    public String removeBuddy(String address) {
        String buddy_name = "";
        try {
            for (int i = 0; ; i++) {
                Buddy compare = (Buddy) buddys.elementAt(i);
                if (address.toLowerCase().equals(compare.getAddress().toLowerCase())) {
                    buddy_name = ((Buddy) buddys.elementAt(i)).getName();
                    buddys.removeElementAt(i);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return buddy_name;
    }

    /**
     * Returns the string representation of all the buddys in the buddylist.
     * @return String-Array of Buddy names
     */
    public Buddy[] getBuddys() {
        Buddy[] output = new Buddy[buddys.size()];
        try {
            for (int i = 0; i < buddys.size(); i++) {
                output[i] = (Buddy) (buddys.elementAt(i));
            }
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        }
        return output;
    }

    /**
     * Calculates the new position of a new Buddy by sorting Buddies by their
     * names (lexicographically).
     * @param object Buddy that should be added to the list
     * @param min left border of the intervall it should be added
     * @param max right border of the intervall it should be added
     * @return
     */
    private int getBuddyListPosition(Buddy object, int min, int max) {
        int pos = (min + max) / 2;
        int comp;
        if (max < min) {
            return 0;
        } else if (max == min) {
            comp = object.compareToByName((Buddy) buddys.elementAt(max));
            if (comp < 0) {
                return max;
            } else if (comp >= 0) {
                return max + 1;
            }
        } else {
            comp = object.compareToByName((Buddy) buddys.elementAt(pos));
            if (comp == 0) {
                pos++;
            } else if (comp < 0) {
                pos = this.getBuddyListPosition(object, min, pos - 1);
            } else {
                pos = this.getBuddyListPosition(object, pos + 1, max);
            }
        }
        return pos;
    }

    /**
     * Looks for an address and if its found, the corresponding name is returned.
     * @param address The address to resolve into the buddys name
     * @return The name (if it is found)
     */
    public String resolveAddress(String address) {
        String name = "";
        try {
            for (int i = 0; ; i++) {
                Buddy temp = (Buddy) buddys.elementAt(i);
                if (address.equals(temp.getAddress())) {
                    name = temp.getName();
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return name;
    }

    /**
     * Looks for a name and if its found, the corresponding address is returned.
     * @param name
     * @return The address (if it is found)
     */
    public String resolveName(String name) {
        String address = "";
        try {
            for (int i = 0; ; i++) {
                Buddy temp = (Buddy) buddys.elementAt(i);
                if (name.equals(temp.getName())) {
                    address = temp.getAddress();
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return address;
    }

    /**
     * Compares the given name to all names in the list and checks for duplicates
     * @param name
     * @return true, if a duplicate was found. false, else
     */
    public boolean checkForNameDuplicates(String name) {
        try {
            for (int i = 0; i < buddys.size(); i++) {
                Buddy temp = (Buddy) buddys.elementAt(i);
                if (name.equals(temp.getName())) {
                    return true;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return false;
    }
}
