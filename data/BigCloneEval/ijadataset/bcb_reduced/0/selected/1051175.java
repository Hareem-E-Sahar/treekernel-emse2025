package chatserver.network.aion.serverpackets;

import org.jboss.netty.buffer.ChannelBuffer;
import chatserver.model.message.Message;
import chatserver.network.aion.AbstractServerPacket;
import chatserver.network.netty.handler.ClientChannelHandler;

public class SM_CHANNEL_MESSAGE extends AbstractServerPacket {

    private Message message;

    public SM_CHANNEL_MESSAGE(Message message) {
        super(0x1A);
        this.message = message;
    }

    @Override
    protected void writeImpl(ClientChannelHandler cHandler, ChannelBuffer buf) {
        writeC(buf, getOpCode());
        writeC(buf, 0x00);
        writeD(buf, message.getChannel().getChannelId());
        writeD(buf, message.getSender().getClientId());
        writeD(buf, 0x00);
        writeH(buf, message.getSender().getIdentifier().length / 2);
        writeB(buf, message.getSender().getIdentifier());
        writeH(buf, message.size() / 2);
        writeB(buf, message.getText());
    }
}
