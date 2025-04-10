package org.arch.event.misc;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.encrypt.SimpleEncrypt;
import org.arch.event.Event;
import org.arch.event.EventConstants;
import org.arch.event.EventDispatcher;
import org.arch.event.EventType;
import org.arch.event.EventVersion;

/**
 *
 */
@EventType(EventConstants.ENCRYPT_EVENT_TYPE)
@EventVersion(2)
public class EncryptEventV2 extends Event {

    public EncryptEventV2() {
    }

    public EncryptEventV2(EncryptType type, Event ev) {
        this.type = type;
        this.ev = ev;
    }

    public EncryptType type;

    public Event ev;

    @Override
    protected boolean onDecode(Buffer buffer) {
        int t;
        try {
            t = BufferHelper.readVarInt(buffer);
            type = EncryptType.fromInt(t);
            int size = BufferHelper.readVarInt(buffer);
            Buffer content = buffer;
            switch(type) {
                case SE1:
                    {
                        SimpleEncrypt se1 = new SimpleEncrypt();
                        se1.decrypt(buffer.getRawBuffer(), buffer.getReadIndex(), size);
                        break;
                    }
                default:
                    {
                        break;
                    }
            }
            ev = EventDispatcher.getSingletonInstance().parse(content);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected boolean onEncode(Buffer buffer) {
        BufferHelper.writeVarInt(buffer, type.getValue());
        Buffer content = new Buffer(256);
        ev.encode(content);
        switch(type) {
            case SE1:
                {
                    SimpleEncrypt se1 = new SimpleEncrypt();
                    se1.encrypt(content);
                    break;
                }
            default:
                {
                    break;
                }
        }
        BufferHelper.writeVarInt(buffer, content.readableBytes());
        buffer.write(content, content.readableBytes());
        return true;
    }
}
