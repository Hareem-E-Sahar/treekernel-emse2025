package org.apache.jk.common;

import java.io.IOException;
import org.apache.jk.core.JkHandler;
import org.apache.jk.core.Msg;
import org.apache.jk.core.MsgContext;
import org.apache.jk.core.JkChannel;
import org.apache.coyote.Request;

/** Pass messages using jni 
 *
 * @author Costin Manolache
 */
public class ChannelJni extends JniHandler implements JkChannel {

    int receivedNote = 1;

    public ChannelJni() {
    }

    public void init() throws IOException {
        super.initNative("channel.jni:jni");
        if (apr == null) return;
        apr.addJkHandler("channelJni", this);
        log.info("JK: listening on channel.jni:jni");
        if (next == null) {
            if (nextName != null) setNext(wEnv.getHandler(nextName));
            if (next == null) next = wEnv.getHandler("dispatch");
            if (next == null) next = wEnv.getHandler("request");
            if (log.isDebugEnabled()) log.debug("Setting default next " + next.getClass().getName());
        }
    }

    /** Receives does nothing - send will put the response
     *  in the same buffer
     */
    public int receive(Msg msg, MsgContext ep) throws IOException {
        Msg sentResponse = (Msg) ep.getNote(receivedNote);
        ep.setNote(receivedNote, null);
        if (sentResponse == null) {
            if (log.isDebugEnabled()) log.debug("No send() prior to receive(), no data buffer");
            msg.reset();
            msg.end();
            sentResponse = msg;
        }
        sentResponse.processHeader();
        if (log.isTraceEnabled()) sentResponse.dump("received response ");
        if (msg != sentResponse) {
            log.error("Error, in JNI mode the msg used for receive() must be identical with the one used for send()");
        }
        return 0;
    }

    /** Send the packet. XXX This will modify msg !!!
     *  We could use 2 packets, or sendAndReceive().
     *    
     */
    public int send(Msg msg, MsgContext ep) throws IOException {
        ep.setNote(receivedNote, null);
        if (log.isDebugEnabled()) log.debug("ChannelJni.send: " + msg);
        int rc = super.nativeDispatch(msg, ep, JK_HANDLE_JNI_DISPATCH, 0);
        ep.setNote(receivedNote, msg);
        return rc;
    }

    public int flush(Msg msg, MsgContext ep) throws IOException {
        ep.setNote(receivedNote, null);
        return OK;
    }

    public boolean isSameAddress(MsgContext ep) {
        return true;
    }

    public void registerRequest(Request req, MsgContext ep, int count) {
    }

    public String getChannelName() {
        return getName();
    }

    /** Receive a packet from the C side. This is called from the C
     *  code using invocation, but only for the first packet - to avoid
     *  recursivity and thread problems.
     *
     *  This may look strange, but seems the best solution for the
     *  problem ( the problem is that we don't have 'continuation' ).
     *
     *  sendPacket will move the thread execution on the C side, and
     *  return when another packet is available. For packets that
     *  are one way it'll return after it is processed too ( having
     *  2 threads is far more expensive ).
     *
     *  Again, the goal is to be efficient and behave like all other
     *  Channels ( so the rest of the code can be shared ). Playing with
     *  java objects on C is extremely difficult to optimize and do
     *  right ( IMHO ), so we'll try to keep it simple - byte[] passing,
     *  the conversion done in java ( after we know the encoding and
     *  if anyone asks for it - same lazy behavior as in 3.3 ).
     */
    public int invoke(Msg msg, MsgContext ep) throws IOException {
        if (apr == null) return -1;
        long xEnv = ep.getJniEnv();
        long cEndpointP = ep.getJniContext();
        int type = ep.getType();
        if (log.isDebugEnabled()) log.debug("ChannelJni.invoke: " + ep + " " + type);
        switch(type) {
            case JkHandler.HANDLE_RECEIVE_PACKET:
                return receive(msg, ep);
            case JkHandler.HANDLE_SEND_PACKET:
                return send(msg, ep);
            case JkHandler.HANDLE_FLUSH:
                return flush(msg, ep);
        }
        ep.setNote(receivedNote, null);
        try {
            if (log.isDebugEnabled()) log.debug("Received request " + xEnv);
            msg.processHeader();
            if (log.isTraceEnabled()) msg.dump("Incoming msg ");
            int status = next.invoke(msg, ep);
            if (log.isDebugEnabled()) log.debug("after processCallbacks " + status);
            return status;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(ChannelJni.class);
}
