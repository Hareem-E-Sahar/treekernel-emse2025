package pl.webd.jhartman.scrabble;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Encapsulates pool of letters (a "bag") where from players gets new letters.
 * @author jhartman
 *
 */
public class Pool {

    private List<Letter> pool = new ArrayList<Letter>();

    private Iterator<Letter> iter;

    /**
	 * Default constructor. Shuffles the pool and adds standard set of letters.
	 * 
	 */
    public Pool() {
        for (Character ch : Letter.letters) for (int i = 0; i < Letter.getAmountOfLettersInPool(ch); i++) pool.add(new Letter(ch));
        shufflePool(pool);
        iter = pool.iterator();
    }

    private static void shufflePool(List<Letter> a) {
        int n = a.toArray().length;
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < n; i++) {
            int change = i + random.nextInt(n - i);
            swap(a, i, change);
        }
    }

    private static void swap(List<Letter> a, int i, int change) {
        Letter helper = a.get(i);
        a.set(i, a.get(change));
        a.set(change, helper);
    }

    /**
	 * Player get next letter from the pool
	 * 
	 * @return A random Letter
	 */
    public Letter getNext() {
        if (!iter.hasNext()) return null;
        return (iter.next());
    }

    /**
	 * Checks if there is any letter left in the pool 
	 * @return True or False
	 */
    public boolean hasNext() {
        return (iter.hasNext());
    }
}
