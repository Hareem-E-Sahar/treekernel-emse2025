package com.dyuproject.protostuff.me;

import java.io.IOException;
import java.util.Vector;
import com.dyuproject.protostuff.me.ByteString;
import com.dyuproject.protostuff.me.Input;
import com.dyuproject.protostuff.me.Message;
import com.dyuproject.protostuff.me.Output;
import com.dyuproject.protostuff.me.Pipe;
import com.dyuproject.protostuff.me.Schema;

public final class Foo implements Message, Schema {

    public interface EnumSample {

        public static final int TYPE0 = 0;

        public static final int TYPE1 = 1;

        public static final int TYPE2 = 2;

        public static final int TYPE3 = 3;

        public static final int TYPE4 = 4;
    }

    public static Schema getSchema() {
        return DEFAULT_INSTANCE;
    }

    public static Foo getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    static final Foo DEFAULT_INSTANCE = new Foo();

    private Vector someInt;

    private Vector someString;

    private Vector someBar;

    private Vector someEnum;

    private Vector someBytes;

    private Vector someBoolean;

    private Vector someFloat;

    private Vector someDouble;

    private Vector someLong;

    public Foo() {
    }

    public Vector getSomeIntList() {
        return someInt;
    }

    public void setSomeIntList(Vector someInt) {
        this.someInt = someInt;
    }

    public Integer getSomeInt(int index) {
        return someInt == null ? null : (Integer) someInt.elementAt(index);
    }

    public int getSomeIntCount() {
        return someInt == null ? 0 : someInt.size();
    }

    public void addSomeInt(Integer someInt) {
        if (this.someInt == null) this.someInt = new Vector();
        this.someInt.addElement(someInt);
    }

    public Vector getSomeStringList() {
        return someString;
    }

    public void setSomeStringList(Vector someString) {
        this.someString = someString;
    }

    public String getSomeString(int index) {
        return someString == null ? null : (String) someString.elementAt(index);
    }

    public int getSomeStringCount() {
        return someString == null ? 0 : someString.size();
    }

    public void addSomeString(String someString) {
        if (this.someString == null) this.someString = new Vector();
        this.someString.addElement(someString);
    }

    public Vector getSomeBarList() {
        return someBar;
    }

    public void setSomeBarList(Vector someBar) {
        this.someBar = someBar;
    }

    public Bar getSomeBar(int index) {
        return someBar == null ? null : (Bar) someBar.elementAt(index);
    }

    public int getSomeBarCount() {
        return someBar == null ? 0 : someBar.size();
    }

    public void addSomeBar(Bar someBar) {
        if (this.someBar == null) this.someBar = new Vector();
        this.someBar.addElement(someBar);
    }

    public Vector getSomeEnumList() {
        return someEnum;
    }

    public void setSomeEnumList(Vector someEnum) {
        this.someEnum = someEnum;
    }

    public Integer getSomeEnum(int index) {
        return someEnum == null ? null : (Integer) someEnum.elementAt(index);
    }

    public int getSomeEnumCount() {
        return someEnum == null ? 0 : someEnum.size();
    }

    public void addSomeEnum(Integer someEnum) {
        if (this.someEnum == null) this.someEnum = new Vector();
        this.someEnum.addElement(someEnum);
    }

    public Vector getSomeBytesList() {
        return someBytes;
    }

    public void setSomeBytesList(Vector someBytes) {
        this.someBytes = someBytes;
    }

    public ByteString getSomeBytes(int index) {
        return someBytes == null ? null : (ByteString) someBytes.elementAt(index);
    }

    public int getSomeBytesCount() {
        return someBytes == null ? 0 : someBytes.size();
    }

    public void addSomeBytes(ByteString someBytes) {
        if (this.someBytes == null) this.someBytes = new Vector();
        this.someBytes.addElement(someBytes);
    }

    public Vector getSomeBooleanList() {
        return someBoolean;
    }

    public void setSomeBooleanList(Vector someBoolean) {
        this.someBoolean = someBoolean;
    }

    public Boolean getSomeBoolean(int index) {
        return someBoolean == null ? null : (Boolean) someBoolean.elementAt(index);
    }

    public int getSomeBooleanCount() {
        return someBoolean == null ? 0 : someBoolean.size();
    }

    public void addSomeBoolean(Boolean someBoolean) {
        if (this.someBoolean == null) this.someBoolean = new Vector();
        this.someBoolean.addElement(someBoolean);
    }

    public Vector getSomeFloatList() {
        return someFloat;
    }

    public void setSomeFloatList(Vector someFloat) {
        this.someFloat = someFloat;
    }

    public Float getSomeFloat(int index) {
        return someFloat == null ? null : (Float) someFloat.elementAt(index);
    }

    public int getSomeFloatCount() {
        return someFloat == null ? 0 : someFloat.size();
    }

    public void addSomeFloat(Float someFloat) {
        if (this.someFloat == null) this.someFloat = new Vector();
        this.someFloat.addElement(someFloat);
    }

    public Vector getSomeDoubleList() {
        return someDouble;
    }

    public void setSomeDoubleList(Vector someDouble) {
        this.someDouble = someDouble;
    }

    public Double getSomeDouble(int index) {
        return someDouble == null ? null : (Double) someDouble.elementAt(index);
    }

    public int getSomeDoubleCount() {
        return someDouble == null ? 0 : someDouble.size();
    }

    public void addSomeDouble(Double someDouble) {
        if (this.someDouble == null) this.someDouble = new Vector();
        this.someDouble.addElement(someDouble);
    }

    public Vector getSomeLongList() {
        return someLong;
    }

    public void setSomeLongList(Vector someLong) {
        this.someLong = someLong;
    }

    public Long getSomeLong(int index) {
        return someLong == null ? null : (Long) someLong.elementAt(index);
    }

    public int getSomeLongCount() {
        return someLong == null ? 0 : someLong.size();
    }

    public void addSomeLong(Long someLong) {
        if (this.someLong == null) this.someLong = new Vector();
        this.someLong.addElement(someLong);
    }

    public Schema cachedSchema() {
        return DEFAULT_INSTANCE;
    }

    public Object newMessage() {
        return new Foo();
    }

    public Class typeClass() {
        return Foo.class;
    }

    public String messageName() {
        return "Foo";
    }

    public String messageFullName() {
        return Foo.class.getName();
    }

    public boolean isInitialized(Object message) {
        return true;
    }

    public void mergeFrom(Input input, Object messageObj) throws IOException {
        Foo message = (Foo) messageObj;
        for (int number = input.readFieldNumber(this); ; number = input.readFieldNumber(this)) {
            switch(number) {
                case 0:
                    return;
                case 1:
                    if (message.someInt == null) message.someInt = new Vector();
                    message.someInt.addElement(new Integer(input.readInt32()));
                    break;
                case 2:
                    if (message.someString == null) message.someString = new Vector();
                    message.someString.addElement(input.readString());
                    break;
                case 3:
                    if (message.someBar == null) message.someBar = new Vector();
                    message.someBar.addElement(input.mergeObject(null, Bar.getSchema()));
                    break;
                case 4:
                    if (message.someEnum == null) message.someEnum = new Vector();
                    message.someEnum.addElement(new Integer(input.readEnum()));
                    break;
                case 5:
                    if (message.someBytes == null) message.someBytes = new Vector();
                    message.someBytes.addElement(input.readBytes());
                    break;
                case 6:
                    if (message.someBoolean == null) message.someBoolean = new Vector();
                    message.someBoolean.addElement(input.readBool() ? Boolean.TRUE : Boolean.FALSE);
                    break;
                case 7:
                    if (message.someFloat == null) message.someFloat = new Vector();
                    message.someFloat.addElement(new Float(input.readFloat()));
                    break;
                case 8:
                    if (message.someDouble == null) message.someDouble = new Vector();
                    message.someDouble.addElement(new Double(input.readDouble()));
                    break;
                case 9:
                    if (message.someLong == null) message.someLong = new Vector();
                    message.someLong.addElement(new Long(input.readInt64()));
                    break;
                default:
                    input.handleUnknownField(number, this);
            }
        }
    }

    public void writeTo(Output output, Object messageObj) throws IOException {
        Foo message = (Foo) messageObj;
        if (message.someInt != null) {
            for (int i = 0; i < message.someInt.size(); i++) {
                Integer someInt = (Integer) message.someInt.elementAt(i);
                if (someInt != null) output.writeInt32(1, someInt.intValue(), true);
            }
        }
        if (message.someString != null) {
            for (int i = 0; i < message.someString.size(); i++) {
                String someString = (String) message.someString.elementAt(i);
                if (someString != null) output.writeString(2, someString, true);
            }
        }
        if (message.someBar != null) {
            for (int i = 0; i < message.someBar.size(); i++) {
                Bar someBar = (Bar) message.someBar.elementAt(i);
                if (someBar != null) output.writeObject(3, someBar, Bar.getSchema(), true);
            }
        }
        if (message.someEnum != null) {
            for (int i = 0; i < message.someEnum.size(); i++) {
                Integer someEnum = (Integer) message.someEnum.elementAt(i);
                if (someEnum != null) output.writeEnum(4, someEnum.intValue(), true);
            }
        }
        if (message.someBytes != null) {
            for (int i = 0; i < message.someBytes.size(); i++) {
                ByteString someBytes = (ByteString) message.someBytes.elementAt(i);
                if (someBytes != null) output.writeBytes(5, someBytes, true);
            }
        }
        if (message.someBoolean != null) {
            for (int i = 0; i < message.someBoolean.size(); i++) {
                Boolean someBoolean = (Boolean) message.someBoolean.elementAt(i);
                if (someBoolean != null) output.writeBool(6, someBoolean.booleanValue(), true);
            }
        }
        if (message.someFloat != null) {
            for (int i = 0; i < message.someFloat.size(); i++) {
                Float someFloat = (Float) message.someFloat.elementAt(i);
                if (someFloat != null) output.writeFloat(7, someFloat.floatValue(), true);
            }
        }
        if (message.someDouble != null) {
            for (int i = 0; i < message.someDouble.size(); i++) {
                Double someDouble = (Double) message.someDouble.elementAt(i);
                if (someDouble != null) output.writeDouble(8, someDouble.doubleValue(), true);
            }
        }
        if (message.someLong != null) {
            for (int i = 0; i < message.someLong.size(); i++) {
                Long someLong = (Long) message.someLong.elementAt(i);
                if (someLong != null) output.writeInt64(9, someLong.longValue(), true);
            }
        }
    }

    public String getFieldName(int number) {
        switch(number) {
            case 1:
                return "someInt";
            case 2:
                return "someString";
            case 3:
                return "someBar";
            case 4:
                return "someEnum";
            case 5:
                return "someBytes";
            case 6:
                return "someBoolean";
            case 7:
                return "someFloat";
            case 8:
                return "someDouble";
            case 9:
                return "someLong";
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
        __fieldMap.put("someInt", new Integer(1));
        __fieldMap.put("someString", new Integer(2));
        __fieldMap.put("someBar", new Integer(3));
        __fieldMap.put("someEnum", new Integer(4));
        __fieldMap.put("someBytes", new Integer(5));
        __fieldMap.put("someBoolean", new Integer(6));
        __fieldMap.put("someFloat", new Integer(7));
        __fieldMap.put("someDouble", new Integer(8));
        __fieldMap.put("someLong", new Integer(9));
    }

    static final Pipe.Schema PIPE_SCHEMA = new Pipe.Schema(DEFAULT_INSTANCE) {

        protected void transfer(Pipe pipe, Input input, Output output) throws IOException {
            for (int number = input.readFieldNumber(wrappedSchema); ; number = input.readFieldNumber(wrappedSchema)) {
                switch(number) {
                    case 0:
                        return;
                    case 1:
                        output.writeInt32(number, input.readInt32(), true);
                        break;
                    case 2:
                        input.transferByteRangeTo(output, true, number, true);
                        break;
                    case 3:
                        output.writeObject(number, pipe, Bar.getPipeSchema(), true);
                        break;
                    case 4:
                        output.writeEnum(number, input.readEnum(), true);
                        break;
                    case 5:
                        input.transferByteRangeTo(output, false, number, true);
                        break;
                    case 6:
                        output.writeBool(number, input.readBool(), true);
                        break;
                    case 7:
                        output.writeFloat(number, input.readFloat(), true);
                        break;
                    case 8:
                        output.writeDouble(number, input.readDouble(), true);
                        break;
                    case 9:
                        output.writeInt64(number, input.readInt64(), true);
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
        result = prime * result + ((someBar == null) ? 0 : someBar.hashCode());
        result = prime * result + ((someBoolean == null) ? 0 : someBoolean.hashCode());
        result = prime * result + ((someBytes == null) ? 0 : someBytes.hashCode());
        result = prime * result + ((someDouble == null) ? 0 : someDouble.hashCode());
        result = prime * result + ((someEnum == null) ? 0 : someEnum.hashCode());
        result = prime * result + ((someFloat == null) ? 0 : someFloat.hashCode());
        result = prime * result + ((someInt == null) ? 0 : someInt.hashCode());
        result = prime * result + ((someLong == null) ? 0 : someLong.hashCode());
        result = prime * result + ((someString == null) ? 0 : someString.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Foo other = (Foo) obj;
        return AbstractTest.isEqual(this.someInt, other.someInt) && AbstractTest.isEqual(this.someString, other.someString) && AbstractTest.isEqual(this.someBar, other.someBar) && AbstractTest.isEqual(this.someEnum, other.someEnum) && AbstractTest.isEqual(this.someBytes, other.someBytes) && AbstractTest.isEqual(this.someBoolean, other.someBoolean) && AbstractTest.isEqual(this.someFloat, other.someFloat) && AbstractTest.isEqual(this.someDouble, other.someDouble) && AbstractTest.isEqual(this.someLong, other.someLong);
    }
}
