package net.sourceforge.jradiusclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.sourceforge.jradiusclient.exception.InvalidParameterException;
import net.sourceforge.jradiusclient.exception.RadiusException;

/**
 * Released under the LGPL<BR>
 *
 * This class provides basic functionality required to implement a NAS as
 * defined by the RADIUS protocol as specified in RFC 2865 and RFC 2866.
 * This implementation is stateless and not thread safe, i.e. since the
 * user name could be changed by the current thread or any other thread,
 * it is difficult to ensure that the responseAttributes correlate to the
 * request we think we are dealing with. It is up to the user of this class
 * to ensure these things at this point. A future release may change this class
 * to a stateful, threadsafe object, but it works for now. Users of this class
 * must also manage building their own request attributes and submitting them with
 * their call to authenticate. For example a programmer using this library, wanting
 * to do chap authentication needs to generate the random challenge, send it to
 *  the user, who generates the MD5 of
 * <UL><LI>a self generated CHAP identifier (a byte)</LI>
 * <LI>their password</LI>
 * <LI>and the CHAP challenge.</LI></UL>(see RFC 2865 section 2.2) The user
 * software returns the CHAP Identifier and the MD5 result and the programmer using RadiusClient
 * sets that as the CHAP Password. The programmer also sets the CHAP-Challenge attribute and
 * sends that to the Radius Server for authentication.
 *
 * <BR>Special Thanks to the original creator of the "RadiusClient"
 * <a href="http://augiesoft.com/java/radius/">August Mueller </a>
 * http://augiesoft.com/java/radius/ and to
 * <a href="http://sourceforge.net/projects/jradius-client">Aziz Abouchi</a>
 * for laying the groundwork for the development of this class.
 *
 * @author <a href="mailto:bloihl@users.sourceforge.net">Robert J. Loihl</a>
 * @version $Revision: 1.35 $
 */
public class RadiusClient {

    private static byte[] NAS_ID;

    private static byte[] NAS_IP;

    private static final int AUTH_LOOP_COUNT = 3;

    private static final int ACCT_LOOP_COUNT = 3;

    private static final int DEFAULT_AUTH_PORT = 1812;

    private static final int DEFAULT_ACCT_PORT = 1813;

    private static final int DEFAULT_SOCKET_TIMEOUT = 6000;

    private String sharedSecret = "";

    private InetAddress hostname = null;

    private int authenticationPort = DEFAULT_AUTH_PORT;

    private int accountingPort = DEFAULT_ACCT_PORT;

    private DatagramSocket socket = null;

    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

    private MessageDigest md5MessageDigest;

    static {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            NAS_ID = (localHost.getHostName()).getBytes();
            NAS_IP = (localHost.getHostAddress()).getBytes();
        } catch (UnknownHostException uhex) {
            throw new RuntimeException(uhex.getMessage());
        }
    }

    /**
     * Constructor - uses the default port 1812 for authentication and 1813 for accounting
     * @param hostname java.lang.String
     * @param sharedSecret java.lang.String
     * @exception java.net.SocketException If we could not create the necessary socket
     * @exception java.security.NoSuchAlgorithmException If we could not get an
     *                              instance of the MD5 algorithm.
     * @exception net.sourceforge.jradiusclient.exception.InvalidParameterException If an invalid hostname
     *                              (null or empty string), an invalid port
     *                              (port < 0 or port > 65536) or an invalid
     *                              shared secret (null, shared secret can be
     *                              empty string) is passed in.
     */
    public RadiusClient(String hostname, String sharedSecret) throws RadiusException, InvalidParameterException {
        this(hostname, DEFAULT_AUTH_PORT, DEFAULT_ACCT_PORT, sharedSecret, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Constructor allows the user to specify an alternate port for the radius server
     * @param hostname java.lang.String
     * @param authPort int the port to use for authentication requests
     * @param acctPort int the port to use for accounting requests
     * @param sharedSecret java.lang.String
     * @exception java.net.SocketException If we could not create the necessary socket
     * @exception java.security.NoSuchAlgorithmException If we could not get an
     *                              instance of the MD5 algorithm.
     * @exception net.sourceforge.jradiusclient.exception.InvalidParameterException If an invalid hostname
     *                              (null or empty string), an invalid
     *                              port ( port < 0 or port > 65536)
     *                              or an invalid shared secret (null, shared
     *                              secret can be empty string) is passed in.
     */
    public RadiusClient(String hostname, int authPort, int acctPort, String sharedSecret) throws RadiusException, InvalidParameterException {
        this(hostname, authPort, acctPort, sharedSecret, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Constructor allows the user to specify an alternate port for the radius server
     * @param hostname java.lang.String
     * @param authPort int the port to use for authentication requests
     * @param acctPort int the port to use for accounting requests
     * @param sharedSecret java.lang.String
     * @param timeout int the timeout to use when waiting for return packets can't be neg and shouldn't be zero
     * @exception net.sourceforge.jradiusclient.exception.RadiusException If we could not create the necessary socket,
     * If we could not get an instance of the MD5 algorithm, or the hostname did not pass validation
     * @exception net.sourceforge.jradiusclient.exception.InvalidParameterException If an invalid hostname
     *                              (null or empty string), an invalid
     *                              port ( port < 0 or port > 65536)
     *                              or an invalid shared secret (null, shared
     *                              secret can be empty string) is passed in.
     */
    public RadiusClient(String hostname, int authPort, int acctPort, String sharedSecret, int sockTimeout) throws RadiusException, InvalidParameterException {
        this.setHostname(hostname);
        this.setSharedSecret(sharedSecret);
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException sex) {
            throw new RadiusException(sex.getMessage());
        }
        this.setTimeout(sockTimeout);
        try {
            this.md5MessageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsaex) {
            throw new RadiusException(nsaex.getMessage());
        }
        this.setAuthPort(authPort);
        this.setAcctPort(acctPort);
    }

    /**
     * This method performs the job of authenticating the given <code>RadiusPacket</code> against
     * the radius server.
     *
     * @param RadiusPacket containing all of the <code>RadiusAttributes</code> for this request. This
     * <code>RadiusPacket</code> must include the USER_NAME attribute and be of type ACCEES_REQUEST.
     * If the USER_PASSWORD attribute is set it must contain the plaintext bytes, we will encode the
     * plaintext to send to the server with a REVERSIBLE algorithm. We will set the NAS_IDENTIFIER
     * Attribute, so even if it is set in the RadiusPacket we will overwrite it
     *
     * @return RadiusPacket containing the response attributes for this request
     * @exception net.sourceforge.jradiusclient.exception.RadiusException
     * @exception net.sourceforge.jradiusclient.exception.InvalidParameterException
     */
    public RadiusPacket authenticate(RadiusPacket accessRequest) throws RadiusException, InvalidParameterException {
        return this.authenticate(accessRequest, RadiusClient.AUTH_LOOP_COUNT);
    }

    /**
     * This method performs the job of authenticating the given <code>RadiusPacket</code> against
     * the radius server.
     *
     * @param RadiusPacket containing all of the <code>RadiusAttributes</code> for this request. This
     * <code>RadiusPacket</code> must include the USER_NAME attribute and be of type ACCEES_REQUEST.
     * If the USER_PASSWORD attribute is set it must contain the plaintext bytes, we will encode the
     * plaintext to send to the server with a REVERSIBLE algorithm. We will set the NAS_IDENTIFIER
     * Attribute, so even if it is set in the RadiusPacket we will overwrite it
     * @param int retries must be zero or greater, if it is zero default value of 3 will be used
     *
     * @return RadiusPacket containing the response attributes for this request
     * @exception net.sourceforge.jradiusclient.exception.RadiusException
     * @exception net.sourceforge.jradiusclient.exception.InvalidParameterException
     */
    public RadiusPacket authenticate(RadiusPacket accessRequest, int retries) throws RadiusException, InvalidParameterException {
        if (null == accessRequest) {
            throw new InvalidParameterException("accessRequest parameter cannot be null");
        }
        if (retries < 0) {
            throw new InvalidParameterException("retries must be zero or greater!");
        } else if (retries == 0) {
            retries = RadiusClient.AUTH_LOOP_COUNT;
        }
        byte code = accessRequest.getPacketType();
        if (code != RadiusPacket.ACCESS_REQUEST) {
            throw new InvalidParameterException("Invalid packet type submitted to authenticate");
        }
        byte identifier = accessRequest.getPacketIdentifier();
        byte[] requestAuthenticator = this.makeRFC2865RequestAuthenticator();
        try {
            byte[] userPass = accessRequest.getAttribute(RadiusAttributeValues.USER_PASSWORD).getValue();
            if (userPass.length > 0) {
                byte[] encryptedPass = this.encodePapPassword(userPass, requestAuthenticator);
                accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.USER_PASSWORD, encryptedPass));
            }
        } catch (RadiusException rex) {
        }
        if (!accessRequest.hasAttribute(RadiusAttributeValues.NAS_IDENTIFIER)) {
            accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.NAS_IDENTIFIER, RadiusClient.NAS_ID));
        }
        byte[] requestAttributes = accessRequest.getAttributeBytes();
        short length = (short) (RadiusPacket.RADIUS_HEADER_LENGTH + requestAttributes.length);
        DatagramPacket packet = this.composeRadiusPacket(this.getAuthPort(), code, identifier, length, requestAuthenticator, requestAttributes);
        RadiusPacket responsePacket = null;
        if ((packet = this.sendReceivePacket(packet, retries)) != null) {
            responsePacket = this.checkRadiusPacket(packet, identifier, requestAuthenticator);
        } else {
            throw new RadiusException("null returned from sendReceivePacket");
        }
        return responsePacket;
    }

    /**
      * This method performs the job of sending accounting information for the
      * current user to the radius accounting server.
      * @param requestPacket Any  request attributes to add to the accounting packet.
      * @return RadiusPacket a packet containing the response from the Radius server
      * @exception net.sourceforge.jradiusclient.exception.RadiusException
      * @exception net.sourceforge.jradiusclient.exception.InvalidParameterException
      */
    public RadiusPacket account(RadiusPacket requestPacket) throws InvalidParameterException, RadiusException {
        if (null == requestPacket) {
            throw new InvalidParameterException("requestPacket parameter cannot be null");
        }
        byte code = requestPacket.getPacketType();
        if (code != RadiusPacket.ACCOUNTING_REQUEST) {
            throw new InvalidParameterException("Invalid type passed in for RadiusPacket");
        }
        try {
            requestPacket.getAttribute(RadiusAttributeValues.USER_NAME);
            requestPacket.getAttribute(RadiusAttributeValues.ACCT_STATUS_TYPE);
            requestPacket.getAttribute(RadiusAttributeValues.ACCT_SESSION_ID);
            requestPacket.getAttribute(RadiusAttributeValues.SERVICE_TYPE);
        } catch (RadiusException rex) {
            throw new InvalidParameterException("Missing RadiusAttribute in Accounting RequestPacket: " + rex.getMessage());
        }
        byte identifier = requestPacket.getPacketIdentifier();
        requestPacket.setAttribute(new RadiusAttribute(RadiusAttributeValues.NAS_IDENTIFIER, RadiusClient.NAS_ID));
        byte[] requestAttributesBytes = requestPacket.getAttributeBytes();
        short length = (short) (RadiusPacket.RADIUS_HEADER_LENGTH + requestAttributesBytes.length);
        byte[] requestAuthenticator = this.makeRFC2866RequestAuthenticator(code, identifier, length, requestAttributesBytes);
        DatagramPacket packet = this.composeRadiusPacket(this.getAcctPort(), code, identifier, length, requestAuthenticator, requestAttributesBytes);
        RadiusPacket responsePacket = null;
        if ((packet = this.sendReceivePacket(packet, RadiusClient.ACCT_LOOP_COUNT)) != null) {
            responsePacket = this.checkRadiusPacket(packet, identifier, requestAuthenticator);
            if (RadiusPacket.ACCOUNTING_RESPONSE != responsePacket.getPacketType()) {
                throw new RadiusException("The radius Server responded with an incorrect response type.");
            }
        } else {
            throw new RadiusException("null returned from sendReceivePacket");
        }
        return responsePacket;
    }

    /**
     * This method encodes the plaintext user password according to RFC 2865
     * @param userPass java.lang.String the password to encrypt
     * @param requestAuthenticator byte[] the requestAuthenicator to use in the encryption
     * @return byte[] the byte array containing the encrypted password
     */
    private byte[] encodePapPassword(final byte[] userPass, final byte[] requestAuthenticator) {
        byte[] userPassBytes = null;
        if (userPass.length > 128) {
            userPassBytes = new byte[128];
            System.arraycopy(userPass, 0, userPassBytes, 0, 128);
        } else {
            userPassBytes = userPass;
        }
        byte[] encryptedPass = null;
        if (userPassBytes.length < 128) {
            if (userPassBytes.length % 16 == 0) {
                encryptedPass = new byte[userPassBytes.length];
            } else {
                encryptedPass = new byte[((userPassBytes.length / 16) * 16) + 16];
            }
        } else {
            encryptedPass = new byte[128];
        }
        System.arraycopy(userPassBytes, 0, encryptedPass, 0, userPassBytes.length);
        for (int i = userPassBytes.length; i < encryptedPass.length; i++) {
            encryptedPass[i] = 0;
        }
        this.md5MessageDigest.reset();
        this.md5MessageDigest.update(this.sharedSecret.getBytes());
        this.md5MessageDigest.update(requestAuthenticator);
        byte bn[] = this.md5MessageDigest.digest();
        for (int i = 0; i < 16; i++) {
            encryptedPass[i] = (byte) (bn[i] ^ encryptedPass[i]);
        }
        if (encryptedPass.length > 16) {
            for (int i = 16; i < encryptedPass.length; i += 16) {
                this.md5MessageDigest.reset();
                this.md5MessageDigest.update(this.sharedSecret.getBytes());
                this.md5MessageDigest.update(encryptedPass, i - 16, 16);
                bn = this.md5MessageDigest.digest();
                for (int j = 0; j < 16; j++) {
                    encryptedPass[i + j] = (byte) (bn[j] ^ encryptedPass[i + j]);
                }
            }
        }
        return encryptedPass;
    }

    /**
     * This method builds a Request Authenticator for use in outgoing RADIUS
     * Access-Request packets as specified in RFC 2865.
     * @return byte[]
     */
    private byte[] makeRFC2865RequestAuthenticator() {
        byte[] requestAuthenticator = new byte[16];
        Random r = new Random();
        for (int i = 0; i < 16; i++) {
            requestAuthenticator[i] = (byte) r.nextInt();
        }
        this.md5MessageDigest.reset();
        this.md5MessageDigest.update(this.sharedSecret.getBytes());
        this.md5MessageDigest.update(requestAuthenticator);
        return this.md5MessageDigest.digest();
    }

    /**
     * This method builds a Response Authenticator for use in validating
     * responses from the RADIUS Authentication process as specified in RFC 2865.
     * The byte array returned should match exactly the response authenticator
     * recieved in the response packet.
     * @param code byte
     * @param identifier byte
     * @param length short
     * @param requestAuthenticator byte[]
     * @param responseAttributeBytes byte[]
     * @return byte[]
     */
    private byte[] makeRFC2865ResponseAuthenticator(byte code, byte identifier, short length, byte[] requestAuthenticator, byte[] responseAttributeBytes) {
        this.md5MessageDigest.reset();
        this.md5MessageDigest.update((byte) code);
        this.md5MessageDigest.update((byte) identifier);
        this.md5MessageDigest.update((byte) (length >> 8));
        this.md5MessageDigest.update((byte) (length & 0xff));
        this.md5MessageDigest.update(requestAuthenticator, 0, requestAuthenticator.length);
        this.md5MessageDigest.update(responseAttributeBytes, 0, responseAttributeBytes.length);
        this.md5MessageDigest.update(this.sharedSecret.getBytes());
        return this.md5MessageDigest.digest();
    }

    /**
     * This method builds a Request Authenticator for use in RADIUS Accounting
     * packets as specified in RFC 2866.
     * @param code byte
     * @param identifier byte
     * @param length short
     * @param requestAttributes byte[]
     * @return byte[]
     */
    private byte[] makeRFC2866RequestAuthenticator(byte code, byte identifier, short length, byte[] requestAttributes) {
        byte[] requestAuthenticator = new byte[16];
        for (int i = 0; i < 16; i++) {
            requestAuthenticator[i] = 0;
        }
        this.md5MessageDigest.reset();
        this.md5MessageDigest.update((byte) code);
        this.md5MessageDigest.update((byte) identifier);
        this.md5MessageDigest.update((byte) (length >> 8));
        this.md5MessageDigest.update((byte) (length & 0xff));
        this.md5MessageDigest.update(requestAuthenticator, 0, requestAuthenticator.length);
        this.md5MessageDigest.update(requestAttributes, 0, requestAttributes.length);
        this.md5MessageDigest.update(this.sharedSecret.getBytes());
        return this.md5MessageDigest.digest();
    }

    /**
     * This method returns the current Host Name to be used for RADIUS
     * authentication or accounting
     * @return java.lang.String The name of the host the radius server is
     *                          running on. Can be either the name or the
     *                          dotted-quad IP address
     */
    public String getHostname() {
        return this.hostname.getHostName();
    }

    /**
     * This method sets the Host Name to be used for RADIUS
     * authentication or accounting
     * @param hostname java.lang.String The name of the host the RADIUS server is
     *                          running on. Can be either the name or the
     *                          dotted-quad IP address
     * @exception net.sourceforge.jradiusclient.exception.InvalidParameterException If the hostname is null,
     *                          empty or all blanks
     */
    private void setHostname(String hostname) throws InvalidParameterException {
        if (hostname == null) {
            throw new InvalidParameterException("Hostname can not be null!");
        } else if (hostname.trim().equals("")) {
            throw new InvalidParameterException("Hostname can not be empty or all blanks!");
        } else {
            try {
                this.hostname = InetAddress.getByName(hostname);
            } catch (java.net.UnknownHostException uhex) {
                throw new InvalidParameterException("Hostname failed InetAddress.getByName() validation!");
            }
        }
    }

    /**
     * This method returns the current port to be used for authentication
     * @return int
     */
    public int getAuthPort() {
        return this.authenticationPort;
    }

    /**
     * This method sets the port to be used for authentication
     * @param port int
     * @exception net.sourceforge.jradiusclient.exception.InvalidParameterException If the port is less
     *                          than 0 or greater than 65535
     */
    private void setAuthPort(int port) throws InvalidParameterException {
        if ((port > 0) && (port < 65536)) {
            this.authenticationPort = port;
        } else {
            throw new InvalidParameterException("Port value out of range!");
        }
    }

    /**
     * This method returns the current port to be used for accounting
     * @return int
     */
    public int getAcctPort() {
        return this.accountingPort;
    }

    /**
     * This method sets the port to be used for accounting
     * @param port int
     * @exception net.sourceforge.jradiusclient.exception.InvalidParameterException If the port is less
     *                          than 0 or greater than 65535
     */
    private void setAcctPort(int port) throws InvalidParameterException {
        if ((port > 0) && (port < 65536)) {
            this.accountingPort = port;
        } else {
            throw new InvalidParameterException("Port value out of range!");
        }
    }

    /**
     * This method returns the current secret value that the Radius Client
     * shares with the RADIUS Server.
     * @return java.lang.String
     */
    public String getSharedSecret() {
        return this.sharedSecret;
    }

    /**
     * This method sets the secret value that the Radius Client shares with the
     * RADIUS Server.
     * @param sharedSecret java.lang.String
     * @exception net.sourceforge.jradiusclient.exception.InvalidParameterException If the shared secret is null,
     *                          or the empty string
     */
    private void setSharedSecret(String sharedSecret) throws InvalidParameterException {
        if (sharedSecret == null) {
            throw new InvalidParameterException("Shared secret can not be null!");
        } else if (sharedSecret.equals("")) {
            throw new InvalidParameterException("Shared secret can not be an empty string!");
        } else {
            this.sharedSecret = sharedSecret;
        }
    }

    /**
     * This method returns the current timeout period on a recieve of a response
     * from the RADIUS Server.
     * @return int
     */
    public int getTimeout() {
        return this.socketTimeout;
    }

    /**
     * This method sets the timeout period on a recieve of a response from the
     * RADIUS Server.
     * @param socket_timeout int a positive timeout value
     * @exception net.sourceforge.jradiusclient.exception.InvalidParameterException If the timeout value is
     *                          less than 0. a 0 value for timeout means that the
     *                          request will block until a response is recieved,
     *                          which is not recommended due to the nature of RADIUS
     *                          (i.e. RADIUS server may be silently dropping your
     *                          packets and never sending a response)
     */
    private void setTimeout(int socket_timeout) throws InvalidParameterException {
        if (socket_timeout < 0) {
            throw new InvalidParameterException("A negative timeout value is not allowed!");
        } else {
            this.socketTimeout = socket_timeout;
            try {
                if (null == this.socket) {
                    this.socket = new DatagramSocket();
                }
                this.socket.setSoTimeout(this.socketTimeout);
            } catch (SocketException sex) {
            }
        }
    }

    /**
     * @param packet java.net.DatagramPacket
     * @param requestIdentifier byte
     * @param requestAuthenticator byte[]
     * @return int the code value from the radius response packet
     * @exception net.sourceforge.jradiusclient.exception.RadiusException
     * @exception java.io.IOException
     */
    private RadiusPacket checkRadiusPacket(DatagramPacket packet, byte requestIdentifier, byte[] requestAuthenticator) throws RadiusException {
        ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
        DataInputStream input = new DataInputStream(bais);
        try {
            int returnCode = -1;
            int packetLength = packet.getLength();
            byte code = input.readByte();
            returnCode = code & 0xff;
            byte identifierByte = input.readByte();
            if (identifierByte != requestIdentifier) {
                throw new RadiusException("The RADIUS Server returned the wrong Identifier.");
            }
            short length = (short) ((int) input.readShort() & 0xffff);
            byte[] responseAuthenticator = new byte[16];
            input.readFully(responseAuthenticator);
            byte[] responseAttributeBytes = new byte[length - RadiusPacket.RADIUS_HEADER_LENGTH];
            input.readFully(responseAttributeBytes);
            byte[] myResponseAuthenticator = this.makeRFC2865ResponseAuthenticator(code, identifierByte, length, requestAuthenticator, responseAttributeBytes);
            if ((responseAuthenticator.length != 16) || (myResponseAuthenticator.length != 16)) {
                throw new RadiusException("Authenticator length is incorrect.");
            } else {
                for (int i = 0; i < responseAuthenticator.length; i++) {
                    if (responseAuthenticator[i] != myResponseAuthenticator[i]) {
                        throw new RadiusException("Authenticators do not match, response packet not validated!");
                    }
                }
            }
            RadiusPacket responsePacket = new RadiusPacket(returnCode, identifierByte);
            int attributesLength = responseAttributeBytes.length;
            if (attributesLength > 0) {
                int attributeType;
                int attributeLength;
                byte[] attributeValue;
                DataInputStream attributeInput = new DataInputStream(new ByteArrayInputStream(responseAttributeBytes));
                for (int left = 0; left < attributesLength; ) {
                    attributeType = (attributeInput.readByte() & 0xff);
                    attributeLength = attributeInput.readByte() & 0xff;
                    attributeValue = new byte[attributeLength - 2];
                    attributeInput.read(attributeValue, 0, attributeLength - 2);
                    responsePacket.setAttribute(new RadiusAttribute(attributeType, attributeValue));
                    left += attributeLength;
                }
                attributeInput.close();
            }
            return responsePacket;
        } catch (IOException ioex) {
            throw new RadiusException(ioex.getMessage());
        } catch (InvalidParameterException ipex) {
            throw new RadiusException("Invalid response attributes sent back from server.");
        } finally {
            try {
                input.close();
                bais.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * This method builds a Radius packet for transmission to the Radius Server
     * @param byte code
     * @param byte identifier
     * @param short length
     * @param byte[] requestAuthenticator
     * @param byte[] requestAttributes
     * @exception java.net.UnknownHostException
     * @exception java.io.IOException
     */
    private DatagramPacket composeRadiusPacket(int port, byte code, byte identifier, short length, byte[] requestAuthenticator, byte[] requestAttributes) throws RadiusException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(baos);
        DatagramPacket packet_out = null;
        try {
            output.writeByte(code);
            output.writeByte(identifier);
            output.writeShort(length);
            output.write(requestAuthenticator, 0, 16);
            output.write(requestAttributes, 0, requestAttributes.length);
            packet_out = new DatagramPacket(new byte[length], length);
            packet_out.setPort(port);
            packet_out.setAddress(this.hostname);
            packet_out.setLength(length);
            packet_out.setData(baos.toByteArray());
            output.close();
            baos.close();
        } catch (IOException ioex) {
            throw new RadiusException(ioex.getMessage());
        }
        return packet_out;
    }

    /**
     * This method sends the outgoing packet and recieves the incoming response
     * @param packet_out java.net.DatagramPacket
     * @param retryCount int Number of retries we will allow
     * @return java.net.DatagramPacket
     * @exception java.io.IOException if there is a problem sending or recieving the packet, i.e recieve timeout
     */
    private DatagramPacket sendReceivePacket(DatagramPacket packet_out, int retry) throws RadiusException {
        if (packet_out.getLength() > RadiusPacket.MAX_PACKET_LENGTH) {
            throw new RadiusException("Packet too big!");
        } else if (packet_out.getLength() < RadiusPacket.MIN_PACKET_LENGTH) {
            throw new RadiusException("Packet too short !");
        } else {
            DatagramPacket packet_in = new DatagramPacket(new byte[RadiusPacket.MAX_PACKET_LENGTH], RadiusPacket.MAX_PACKET_LENGTH);
            for (int i = 1; i <= retry; i++) {
                try {
                    this.socket.send(packet_out);
                    this.socket.receive(packet_in);
                    return packet_in;
                } catch (IOException ioex) {
                    if (i == retry) {
                        throw new RadiusException(ioex.getMessage());
                    }
                }
            }
        }
        return null;
    }

    /**
     * This method returns a string representation of this
     * <code>RadiusClient</code>.
     *
     * @return a string representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("RadiusClient: HostName = ");
        sb.append(this.getHostname());
        sb.append(" Port = ");
        sb.append(Integer.toString(this.getAuthPort()));
        sb.append(" Shared Secret = ");
        sb.append(this.getSharedSecret());
        return sb.toString();
    }

    /**
     * Compares the specified Object with this <code>RadiusClient</code>
     * for equality.  Returns true if the given object is also a
     * <code>RadiusClient</code> and the two RadiusClient
     * have the same host, port, sharedSecret & username.
     * @param object Object to be compared for equality with this
     *		<code>RadiusClient</code>.
     *
     * @return true if the specified Object is equal to this
     *		<code>RadiusClient</code>.
     */
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (!(object instanceof RadiusClient)) {
            return false;
        }
        RadiusClient that = (RadiusClient) object;
        if ((this.getHostname().equals(that.getHostname())) && (this.getAuthPort() == that.getAuthPort()) && (this.getSharedSecret().equals(that.getSharedSecret()))) {
            return true;
        }
        return true;
    }

    /**
     * @return int the hashCode for this <code>RadiusClient</code>
     */
    public int hashCode() {
        StringBuffer sb = new StringBuffer(this.getHostname());
        sb.append(Integer.toString(this.getAuthPort()));
        sb.append(this.getSharedSecret());
        return sb.toString().hashCode();
    }

    /**
     * closes the socket
     *
     */
    protected void closeSocket() {
        this.socket.close();
    }

    /**
     * overrides finalize to close socket and then normal finalize on super class
     */
    public void finalize() throws Throwable {
        this.closeSocket();
        super.finalize();
    }
}
