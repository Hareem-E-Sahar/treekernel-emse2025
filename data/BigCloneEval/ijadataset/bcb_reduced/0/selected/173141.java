package lug.serenity.npc.random;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Random;
import lug.util.RandomFactory;

/**
 * @author Luggy
 *
 */
public class RandomPassengerGenerator {

    private static final String LIST_RESOURCE = "data/passengers.dat";

    private static LinkedList<Passenger> list;

    private static final Random random = RandomFactory.getRandom();

    static {
        try {
            readRandomList();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
	 * Read in the random passenger source list
	 */
    private static void readRandomList() throws IOException {
        InputStream instream = ClassLoader.getSystemClassLoader().getResourceAsStream(LIST_RESOURCE);
        if (instream == null) {
            throw new IOException("Cannot locate resource for random passengers.");
        }
        list = new LinkedList<Passenger>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
        String line;
        int l = 0;
        while ((line = reader.readLine()) != null) {
            try {
                l++;
                Passenger rp = new Passenger();
                rp.parseFromDataFile(line);
                list.add(rp);
            } catch (Exception e) {
                System.out.println("error on line; " + (l));
            }
        }
        reader.close();
    }

    /**
	 * Return a numbered passenger.
	 * @param roll roll for passenger ( 0 to 999).
	 * @return
	 */
    public static Passenger getPassenger(int roll) {
        if (roll < 0 || roll > 999) {
            throw new IllegalArgumentException("Passenger roll must be 0-999 (inclusive).");
        }
        for (Passenger rp : list) {
            int l = rp.getLowRoll();
            int h = rp.getHighRoll();
            if (roll >= l && roll <= h) {
                return rp;
            }
        }
        return list.get(0);
    }

    /**
	 * Return a random passenger
	 * @return
	 */
    public static Passenger getRandomPassenger() {
        int n = random.nextInt(1000);
        return getPassenger(n);
    }

    public static String getRandomPassengerDescription() {
        StringBuilder sb = new StringBuilder(256);
        Passenger passenger = getRandomPassenger();
        if (passenger.getLikeLow() != -1) {
            sb.append(getLikeDescription(passenger.getLikeLow(), passenger.getLikeHigh()));
        }
        sb.append(passenger.getDescription());
        return sb.toString();
    }

    /**
	 * Get a "Like character but" description.
	 * @param likeLow Low roll for like
	 * @param likeHigh high roll for like
	 * @return
	 */
    private static String getLikeDescription(int likeLow, int likeHigh) {
        int passnum = likeLow;
        if (likeHigh != -1) {
            passnum = likeLow + random.nextInt(likeHigh - likeLow);
        }
        return getPassenger(passnum).getDescription();
    }
}
