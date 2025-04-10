package com.dyuproject.protostuff.me;

import java.io.IOException;
import com.dyuproject.protostuff.me.Input;
import com.dyuproject.protostuff.me.Message;
import com.dyuproject.protostuff.me.Output;
import com.dyuproject.protostuff.me.Pipe;
import com.dyuproject.protostuff.me.Schema;

public final class Baz implements Message, Schema {

    public static Schema getSchema() {
        return DEFAULT_INSTANCE;
    }

    public static Baz getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    static final Baz DEFAULT_INSTANCE = new Baz();

    private int id;

    private String name;

    private long timestamp;

    public Baz() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Schema cachedSchema() {
        return DEFAULT_INSTANCE;
    }

    public Object newMessage() {
        return new Baz();
    }

    public Class typeClass() {
        return Baz.class;
    }

    public String messageName() {
        return "Baz";
    }

    public String messageFullName() {
        return Baz.class.getName();
    }

    public boolean isInitialized(Object message) {
        return true;
    }

    public void mergeFrom(Input input, Object messageObj) throws IOException {
        Baz message = (Baz) messageObj;
        for (int number = input.readFieldNumber(this); ; number = input.readFieldNumber(this)) {
            switch(number) {
                case 0:
                    return;
                case 1:
                    message.id = input.readInt32();
                    break;
                case 2:
                    message.name = input.readString();
                    break;
                case 3:
                    message.timestamp = input.readFixed64();
                    break;
                default:
                    input.handleUnknownField(number, this);
            }
        }
    }

    public void writeTo(Output output, Object messageObj) throws IOException {
        Baz message = (Baz) messageObj;
        if (message.id != 0) output.writeInt32(1, message.id, false);
        if (message.name != null) output.writeString(2, message.name, false);
        if (message.timestamp != 0) output.writeFixed64(3, message.timestamp, false);
    }

    public String getFieldName(int number) {
        switch(number) {
            case 1:
                return "id";
            case 2:
                return "name";
            case 3:
                return "timestamp";
            default:
                return null;
        }
    }

    public int getFieldNumber(String name) {
        final Integer number = (Integer) __fieldMap.get(name);
        return number == null ? 0 : number.intValue();
    }

    private static final java.util.Hashtable __fieldMap = new java.util.Hashtable();

    static {
        __fieldMap.put("id", new Integer(1));
        __fieldMap.put("name", new Integer(2));
        __fieldMap.put("timestamp", new Integer(3));
    }

    static final Pipe.Schema PIPE_SCHEMA = new Pipe.Schema(DEFAULT_INSTANCE) {

        protected void transfer(Pipe pipe, Input input, Output output) throws IOException {
            for (int number = input.readFieldNumber(wrappedSchema); ; number = input.readFieldNumber(wrappedSchema)) {
                switch(number) {
                    case 0:
                        return;
                    case 1:
                        output.writeInt32(number, input.readInt32(), false);
                        break;
                    case 2:
                        input.transferByteRangeTo(output, true, number, false);
                        break;
                    case 3:
                        output.writeFixed64(number, input.readFixed64(), false);
                        break;
                    default:
                        input.handleUnknownField(number, wrappedSchema);
                }
            }
        }
    };

    public static Pipe.Schema getPipeSchema() {
        return PIPE_SCHEMA;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Baz other = (Baz) obj;
        if (id != other.id) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        if (timestamp != other.timestamp) return false;
        return true;
    }
}
