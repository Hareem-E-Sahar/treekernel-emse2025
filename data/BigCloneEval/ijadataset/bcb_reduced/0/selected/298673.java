package no.monsen.client.common.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;
import no.monsen.client.common.logging.Logger;
import org.apache.commons.logging.Log;

/**
 * 
 * @author Marius Breivik Created: 21.50.22 / 10. mars. 2008
 */
public class MonsenClientID {

    @Logger
    private Log log;

    private Long uniqeClientID = new Long("0");

    public MonsenClientID() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            byte[] unreadableMacAddress = ni.getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < unreadableMacAddress.length; i++) {
                buffer.append(Integer.toHexString(unreadableMacAddress[i] & 0xFF));
            }
            uniqeClientID = (long) (buffer.toString().hashCode() & 0xFFFFFFFFL);
        } catch (SocketException e) {
            log.error("An error occured while generating uniqe id " + e.getMessage());
            uniqeClientID = (long) UUID.randomUUID().hashCode();
        } catch (UnknownHostException e) {
            log.error("An error occured while generating uniqe id " + e.getMessage());
            uniqeClientID = (long) UUID.randomUUID().hashCode();
        } catch (Exception e) {
            log.error("An error occured while generating uniqe id " + e.getMessage());
            uniqeClientID = (long) UUID.randomUUID().hashCode();
        }
    }

    public Long getUniqueClientID() {
        return uniqeClientID;
    }
}
