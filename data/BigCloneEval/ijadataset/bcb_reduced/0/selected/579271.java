package net.sourceforge.scuba.smartcards;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import com.sun.javacard.apduio.CadClientInterface;
import com.sun.javacard.apduio.CadDevice;
import com.sun.javacard.apduio.CadTransportException;

/**
 * CardTerminal implementation for Sun's CREF emulator. This
 * makes it possible to use CREF as an ordinary terminal within
 * the <code>javax.smartcardio</code> framework.
 * 
 * @author Cees-Bart Breunesse (ceesb@riscure.com)
 * @author Martijn Oostdijk (martijn.oostdijk@gmail.com)
 * @version $Revision: $
 */
public class CREFEmulatorTerminal extends CardTerminal {

    private static final long CARD_CHECK_SLEEP_TIME = 150;

    private static final long HEARTBEAT_TIMEOUT = 1200;

    private static final byte OFFSET_LC = (byte) 4;

    private static final byte OFFSET_CDATA = (byte) 5;

    private String hostName;

    private int port;

    private long heartBeat;

    private boolean isCheckingForCardPresent;

    private final Object terminal;

    private boolean wasCardPresent;

    private CREFCard card;

    /**
	 * Listens for instances of the CREF emulator on the specified host and port.
	 * 
	 * @param hostName the host name, for instance <code>&quot;localhost&quot;"</code>
	 * @param port the port number, for instance <code>9025</code>
	 */
    public CREFEmulatorTerminal(String hostName, int port) {
        terminal = this;
        this.hostName = hostName;
        this.port = port;
        heartBeat = System.currentTimeMillis();
        isCheckingForCardPresent = false;
    }

    /**
	 * Connects to the emulator.
	 * 
	 * @param protocol is ignored for now
	 */
    public synchronized Card connect(String protocol) throws CardException {
        synchronized (terminal) {
            try {
                if (card == null && !isCardPresent()) {
                    throw new CardException("No card present");
                }
                if (card == null) {
                    card = new CREFCard(new Socket(hostName, port));
                }
                return card;
            } catch (IOException ioe) {
                throw new CardException(ioe.toString());
            }
        }
    }

    /**
	 * The name of this terminal.
	 * 
	 * @return the name of this terminal.
	 */
    public String getName() {
        return "CREF emulator at " + hostName + ":" + port;
    }

    public String toString() {
        return getName();
    }

    /**
	 * Determines whether a card is present.
	 * 
	 * @return whether a card is present.
	 */
    public boolean isCardPresent() {
        if (isCheckingForCardPresent) {
            return wasCardPresent;
        }
        if (isChannelOpen) {
            return true;
        }
        if ((System.currentTimeMillis() - heartBeat) < HEARTBEAT_TIMEOUT) {
            return wasCardPresent;
        }
        synchronized (terminal) {
            try {
                heartBeat = System.currentTimeMillis();
                isCheckingForCardPresent = true;
                Socket s = new Socket(hostName, port);
                card = new CREFCard(s);
                wasCardPresent = true;
            } catch (ConnectException e) {
                if (e.getMessage().startsWith("Connection refused")) {
                    wasCardPresent = false;
                }
            } catch (Exception e) {
                wasCardPresent = true;
            }
            isCheckingForCardPresent = false;
            return wasCardPresent;
        }
    }

    /**
	 * Waits for a card to be present.
	 * 
	 * @param timeout a timeout
	 * 
	 * @return whether the card became present before the timeout expired
	 */
    public boolean waitForCardAbsent(long timeout) throws CardException {
        long startTime = System.currentTimeMillis();
        if (CARD_CHECK_SLEEP_TIME > timeout) {
            return !isCardPresent();
        }
        try {
            while (isCardPresent()) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    break;
                }
                Thread.sleep(CARD_CHECK_SLEEP_TIME);
            }
        } catch (InterruptedException ie) {
        }
        return !isCardPresent();
    }

    /**
	 * Waits for a card to be absent.
	 * 
	 * @param timeout a timeout
	 * 
	 * @return whether the card became absent before the timeout expired
	 */
    public boolean waitForCardPresent(long timeout) throws CardException {
        long startTime = System.currentTimeMillis();
        if (CARD_CHECK_SLEEP_TIME > timeout) {
            return isCardPresent();
        }
        try {
            while (!isCardPresent()) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    break;
                }
                Thread.sleep(CARD_CHECK_SLEEP_TIME);
            }
        } catch (InterruptedException ie) {
        }
        return isCardPresent();
    }

    /**
	 * This merely wraps channel.
	 */
    private class CREFCard extends Card {

        private CREFCardChannel basicChannel;

        public CREFCard(Socket s) throws CardException {
            basicChannel = new CREFCardChannel(this, s);
        }

        public void beginExclusive() throws CardException {
        }

        public void disconnect(boolean reset) throws CardException {
            basicChannel.close();
        }

        public void endExclusive() throws CardException {
        }

        public ATR getATR() {
            return basicChannel.getATR();
        }

        public CardChannel getBasicChannel() {
            return basicChannel;
        }

        public String getProtocol() {
            return "T=1";
        }

        public CardChannel openLogicalChannel() throws CardException {
            return null;
        }

        public byte[] transmitControlCommand(int controlCode, byte[] command) throws CardException {
            return null;
        }
    }

    /**
	 * Basic card channel to the CREF emulator.
	 */
    private static boolean isChannelOpen = false;

    private class CREFCardChannel extends CardChannel {

        private CadClientInterface cad;

        private Socket sock;

        private ATR atr;

        private Card card;

        public CREFCardChannel(Card card, Socket sock) throws CardException {
            synchronized (terminal) {
                try {
                    this.card = card;
                    this.sock = sock;
                    InputStream is = sock.getInputStream();
                    OutputStream os = sock.getOutputStream();
                    cad = CadDevice.getCadClientInstance(CadDevice.PROTOCOL_T1, is, os);
                    byte[] atrBytes = cad.powerUp();
                    atr = new ATR(atrBytes);
                    heartBeat = System.currentTimeMillis();
                    isChannelOpen = true;
                } catch (CadTransportException e) {
                    if (sock != null) {
                        try {
                            sock.close();
                        } catch (IOException ioex) {
                        }
                    }
                    throw new CardException(e.toString());
                } catch (UnknownHostException uhe) {
                    throw new CardException(uhe.toString());
                } catch (IOException ioe) {
                    if (sock != null) {
                        try {
                            sock.close();
                        } catch (IOException ioex) {
                        }
                    }
                    throw new CardException(ioe.toString());
                }
            }
        }

        public ATR getATR() {
            return atr;
        }

        public void close() throws CardException {
            synchronized (terminal) {
                try {
                    if (cad != null) {
                        cad.powerDown();
                        sock.close();
                        isChannelOpen = false;
                    }
                } catch (Exception e) {
                }
            }
        }

        public Card getCard() {
            return card;
        }

        public int getChannelNumber() {
            return 0;
        }

        /**
		 * Transmits a command to the card.
		 * 
		 * @param command the command to send
		 * 
		 * @return the response from the card.
		 */
        public int transmit(ByteBuffer command, ByteBuffer response) throws CardException {
            synchronized (terminal) {
                ResponseAPDU rapdu = transmit(new CommandAPDU(command));
                byte[] rapduBytes = rapdu.getBytes();
                response.put(rapduBytes);
                return rapduBytes.length;
            }
        }

        /**
		 * Transmits a command to the card.
		 * 
		 * @param command the command to send
		 * 
		 * @return the response from the card.
		 */
        public ResponseAPDU transmit(CommandAPDU command) throws CardException {
            synchronized (terminal) {
                try {
                    com.sun.javacard.apduio.Apdu theirApdu = translateCommand(command);
                    cad.exchangeApdu(theirApdu);
                    ResponseAPDU ourResponseAPDU = translateResponse(theirApdu);
                    return ourResponseAPDU;
                } catch (IOException ioe) {
                    throw new CardException(ioe.toString());
                } catch (CadTransportException cte) {
                    throw new CardException(cte.toString());
                } catch (Exception e) {
                    throw new CardException(e.toString());
                }
            }
        }

        private com.sun.javacard.apduio.Apdu translateCommand(CommandAPDU ourApdu) {
            com.sun.javacard.apduio.Apdu theirApdu = new com.sun.javacard.apduio.Apdu();
            byte[] buffer = ourApdu.getBytes();
            int len = buffer.length;
            int lc = 0;
            int le = buffer[len - 1] & 0x000000FF;
            if (len == 4) {
                lc = 0;
                le = 0;
            } else if (len == 5) {
                lc = 0;
            } else if (len > 5) {
                lc = buffer[OFFSET_LC] & 0x000000FF;
            }
            if (4 + lc >= len) {
                le = 0;
            }
            theirApdu.setLc(lc);
            theirApdu.setLe(le);
            theirApdu.command = new byte[4];
            System.arraycopy(buffer, 0, theirApdu.command, 0, 4);
            theirApdu.dataIn = new byte[lc];
            System.arraycopy(buffer, OFFSET_CDATA, theirApdu.dataIn, 0, lc);
            return theirApdu;
        }

        private ResponseAPDU translateResponse(com.sun.javacard.apduio.Apdu theirApdu) {
            int len = theirApdu.getDataOut().length;
            byte[] rapdu = new byte[len + 2];
            System.arraycopy(theirApdu.getDataOut(), 0, rapdu, 0, len);
            System.arraycopy(theirApdu.getSw1Sw2(), 0, rapdu, len, 2);
            return new ResponseAPDU(rapdu);
        }
    }
}
