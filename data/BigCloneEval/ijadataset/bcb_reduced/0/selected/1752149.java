package bomberman.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.CRC32;

/**
 * Implementation of Challenge Handshake Authentication Protocol (CHAP).
 * @author Christian Lins (christian.lins@web.de)
 */
public class CHAP {

    /**
   * How many seconds the challenge will be valid.
   */
    public static final int TIMEOUT = 30;

    private HashMap<String, Long> challenges = new HashMap<String, Long>();

    private HashMap<Long, Date> challenge_validity = new HashMap<Long, Date>();

    /**
   * Creates and returns a randomized number for the given nickname.
   * This number is valid for @see{TIMEOUT} seconds.
   * @return
   */
    public long createChallenge(String nickname) {
        long challenge = new Random().nextLong();
        challenges.put(nickname, challenge);
        challenge_validity.put(challenge, new Date());
        return challenge;
    }

    /**
   * Returns a previously generated challenge for the given username.
   * @param nickname
   * @return
   */
    public long getChallenge(String nickname) {
        return this.challenges.get(nickname);
    }

    /**
   * Creates a checksum from the given random number and
   * the password. The checksum is generated using the multiplicative product of
   * random and CRC32(password).
   * @param random
   * @param password
   * @return
   */
    public static long createChecksum(long random, String password) {
        if (password == null) return -1;
        CRC32 crc = new CRC32();
        crc.update(password.getBytes());
        return crc.getValue() * random;
    }

    /**
   * Checks if the request of the given username was within
   * time.
   * @param nickname
   * @return
   */
    public boolean isValid(String nickname) {
        Long challenge = this.challenges.get(nickname);
        if (challenge == null) return false;
        Date time = challenge_validity.get(challenge);
        if (time == null) return false;
        if (time.getTime() < new Date().getTime() - 1000 * TIMEOUT) return false;
        return true;
    }
}
