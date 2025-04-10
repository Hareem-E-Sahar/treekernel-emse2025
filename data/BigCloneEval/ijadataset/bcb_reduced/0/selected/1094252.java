package com.dyuproject.protostuff.runtime;

import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.BIGDECIMAL;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.BIGINTEGER;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.BOOL;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.BYTE;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.BYTES;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.BYTE_ARRAY;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.CHAR;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.DATE;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.DOUBLE;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.FLOAT;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_ARRAY;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_ARRAY_MAPPED;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_BIGDECIMAL;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_BIGINTEGER;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_BOOL;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_BYTE;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_BYTES;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_BYTE_ARRAY;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_CHAR;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_CLASS;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_CLASS_ARRAY;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_CLASS_ARRAY_MAPPED;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_CLASS_MAPPED;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_COLLECTION;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_DATE;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_DELEGATE;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_DOUBLE;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_ENUM;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_ENUM_MAP;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_ENUM_SET;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_FLOAT;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_INT32;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_INT64;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_MAP;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_OBJECT;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_POJO;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_POLYMORPHIC_COLLECTION;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_POLYMORPHIC_MAP;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_SHORT;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_STRING;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.ID_THROWABLE;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.INT32;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.INT64;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.SHORT;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STRING;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_ARRAY;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_ARRAY_MAPPED;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_BIGDECIMAL;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_BIGINTEGER;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_BOOL;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_BYTE;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_BYTES;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_BYTE_ARRAY;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_CHAR;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_CLASS;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_CLASS_ARRAY;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_CLASS_ARRAY_MAPPED;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_CLASS_MAPPED;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_COLLECTION;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_DATE;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_DELEGATE;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_DOUBLE;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_ENUM;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_ENUM_MAP;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_ENUM_SET;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_FLOAT;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_INT32;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_INT64;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_MAP;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_OBJECT;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_POJO;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_POLYMOPRHIC_MAP;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_POLYMORPHIC_COLLECTION;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_SHORT;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_STRING;
import static com.dyuproject.protostuff.runtime.RuntimeFieldFactory.STR_THROWABLE;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import com.dyuproject.protostuff.GraphInput;
import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Message;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Pipe;
import com.dyuproject.protostuff.ProtostuffException;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.StatefulOutput;

/**
 * A schema for dynamic types (fields where the type is {@link Object}).
 *
 * @author David Yu
 * @created Feb 1, 2011
 */
public abstract class ObjectSchema extends PolymorphicSchema {

    static final int ID_ENUM_VALUE = 1;

    static final int ID_ARRAY_LEN = 3;

    static final int ID_ARRAY_DIMENSION = 2;

    static String name(int number) {
        switch(number) {
            case ID_POLYMORPHIC_COLLECTION:
                return STR_POLYMORPHIC_COLLECTION;
            case ID_POLYMORPHIC_MAP:
                return STR_POLYMOPRHIC_MAP;
            case ID_DELEGATE:
                return STR_DELEGATE;
            case ID_THROWABLE:
                return STR_THROWABLE;
            case ID_BOOL:
                return STR_BOOL;
            case ID_BYTE:
                return STR_BYTE;
            case ID_CHAR:
                return STR_CHAR;
            case ID_SHORT:
                return STR_SHORT;
            case ID_INT32:
                return STR_INT32;
            case ID_INT64:
                return STR_INT64;
            case ID_FLOAT:
                return STR_FLOAT;
            case ID_DOUBLE:
                return STR_DOUBLE;
            case ID_STRING:
                return STR_STRING;
            case ID_BYTES:
                return STR_BYTES;
            case ID_BYTE_ARRAY:
                return STR_BYTE_ARRAY;
            case ID_BIGDECIMAL:
                return STR_BIGDECIMAL;
            case ID_BIGINTEGER:
                return STR_BIGINTEGER;
            case ID_DATE:
                return STR_DATE;
            case ID_ARRAY:
                return STR_ARRAY;
            case ID_OBJECT:
                return STR_OBJECT;
            case ID_ARRAY_MAPPED:
                return STR_ARRAY_MAPPED;
            case ID_CLASS:
                return STR_CLASS;
            case ID_CLASS_MAPPED:
                return STR_CLASS_MAPPED;
            case ID_CLASS_ARRAY:
                return STR_CLASS_ARRAY;
            case ID_CLASS_ARRAY_MAPPED:
                return STR_CLASS_ARRAY_MAPPED;
            case ID_ENUM_SET:
                return STR_ENUM_SET;
            case ID_ENUM_MAP:
                return STR_ENUM_MAP;
            case ID_ENUM:
                return STR_ENUM;
            case ID_COLLECTION:
                return STR_COLLECTION;
            case ID_MAP:
                return STR_MAP;
            case ID_POJO:
                return STR_POJO;
            default:
                return null;
        }
    }

    static int number(String name) {
        if (name.length() != 1) return 0;
        switch(name.charAt(0)) {
            case 'B':
                return 28;
            case 'C':
                return 29;
            case 'D':
                return 30;
            case 'Z':
                return 52;
            case '_':
                return 127;
            case 'a':
                return 1;
            case 'b':
                return 2;
            case 'c':
                return 3;
            case 'd':
                return 4;
            case 'e':
                return 5;
            case 'f':
                return 6;
            case 'g':
                return 7;
            case 'h':
                return 8;
            case 'i':
                return 9;
            case 'j':
                return 10;
            case 'k':
                return 11;
            case 'l':
                return 12;
            case 'm':
                return 13;
            case 'n':
                return 14;
            case 'o':
                return 15;
            case 'p':
                return 16;
            case 'q':
                return 17;
            case 'r':
                return 18;
            case 's':
                return 19;
            case 't':
                return 20;
            case 'u':
                return 21;
            case 'v':
                return 22;
            case 'w':
                return 23;
            case 'x':
                return 24;
            case 'y':
                return 25;
            case 'z':
                return 26;
            default:
                return 0;
        }
    }

    protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema<Object>(this) {

        protected void transfer(Pipe pipe, Input input, Output output) throws IOException {
            transferObject(this, pipe, input, output, strategy);
        }
    };

    public ObjectSchema(IdStrategy strategy) {
        super(strategy);
    }

    public Pipe.Schema<Object> getPipeSchema() {
        return pipeSchema;
    }

    public String getFieldName(int number) {
        return name(number);
    }

    public int getFieldNumber(String name) {
        return number(name);
    }

    public String messageFullName() {
        return Object.class.getName();
    }

    public String messageName() {
        return Object.class.getSimpleName();
    }

    public void mergeFrom(Input input, Object owner) throws IOException {
        setValue(readObjectFrom(input, this, owner, strategy), owner);
    }

    public void writeTo(Output output, Object value) throws IOException {
        writeObjectTo(output, value, this, strategy);
    }

    static ArrayWrapper newArrayWrapper(Input input, Schema<?> schema, boolean mapped, IdStrategy strategy) throws IOException {
        final Class<?> componentType = strategy.resolveArrayComponentTypeFrom(input, mapped);
        if (input.readFieldNumber(schema) != ID_ARRAY_LEN) throw new ProtostuffException("Corrupt input.");
        final int len = input.readUInt32();
        if (input.readFieldNumber(schema) != ID_ARRAY_DIMENSION) throw new ProtostuffException("Corrupt input.");
        final int dimensions = input.readUInt32();
        if (dimensions == 1) return new ArrayWrapper(Array.newInstance(componentType, len));
        final int[] arg = new int[dimensions];
        arg[0] = len;
        return new ArrayWrapper(Array.newInstance(componentType, arg));
    }

    static void transferArray(Pipe pipe, Input input, Output output, int number, Pipe.Schema<?> pipeSchema, boolean mapped, IdStrategy strategy) throws IOException {
        strategy.transferArrayId(input, output, number, mapped);
        if (input.readFieldNumber(pipeSchema.wrappedSchema) != ID_ARRAY_LEN) throw new ProtostuffException("Corrupt input.");
        output.writeUInt32(ID_ARRAY_LEN, input.readUInt32(), false);
        if (input.readFieldNumber(pipeSchema.wrappedSchema) != ID_ARRAY_DIMENSION) throw new ProtostuffException("Corrupt input.");
        output.writeUInt32(ID_ARRAY_DIMENSION, input.readUInt32(), false);
        if (output instanceof StatefulOutput) {
            ((StatefulOutput) output).updateLast(strategy.ARRAY_PIPE_SCHEMA, pipeSchema);
        }
        Pipe.transferDirect(strategy.ARRAY_PIPE_SCHEMA, pipe, input, output);
    }

    static void transferClass(Pipe pipe, Input input, Output output, int number, Pipe.Schema<?> pipeSchema, boolean mapped, boolean array, IdStrategy strategy) throws IOException {
        strategy.transferClassId(input, output, number, mapped, array);
        if (array) {
            if (input.readFieldNumber(pipeSchema.wrappedSchema) != ID_ARRAY_DIMENSION) throw new ProtostuffException("Corrupt input.");
            output.writeUInt32(ID_ARRAY_DIMENSION, input.readUInt32(), false);
        }
    }

    static Class<?> getArrayClass(Input input, Schema<?> schema, final Class<?> componentType) throws IOException {
        if (input.readFieldNumber(schema) != ID_ARRAY_DIMENSION) throw new ProtostuffException("Corrupt input.");
        final int dimensions = input.readUInt32();
        if (dimensions == 1) return Array.newInstance(componentType, 0).getClass();
        final int[] arg = new int[dimensions];
        arg[0] = 0;
        return Array.newInstance(componentType, arg).getClass();
    }

    @SuppressWarnings("unchecked")
    static Object readObjectFrom(final Input input, final Schema<?> schema, Object owner, IdStrategy strategy) throws IOException {
        Object value = null;
        final int number = input.readFieldNumber(schema);
        switch(number) {
            case ID_BOOL:
                value = BOOL.readFrom(input);
                break;
            case ID_BYTE:
                value = BYTE.readFrom(input);
                break;
            case ID_CHAR:
                value = CHAR.readFrom(input);
                break;
            case ID_SHORT:
                value = SHORT.readFrom(input);
                break;
            case ID_INT32:
                value = INT32.readFrom(input);
                break;
            case ID_INT64:
                value = INT64.readFrom(input);
                break;
            case ID_FLOAT:
                value = FLOAT.readFrom(input);
                break;
            case ID_DOUBLE:
                value = DOUBLE.readFrom(input);
                break;
            case ID_STRING:
                value = STRING.readFrom(input);
                break;
            case ID_BYTES:
                value = BYTES.readFrom(input);
                break;
            case ID_BYTE_ARRAY:
                value = BYTE_ARRAY.readFrom(input);
                break;
            case ID_BIGDECIMAL:
                value = BIGDECIMAL.readFrom(input);
                break;
            case ID_BIGINTEGER:
                value = BIGINTEGER.readFrom(input);
                break;
            case ID_DATE:
                value = DATE.readFrom(input);
                break;
            case ID_ARRAY:
                {
                    final ArrayWrapper arrayWrapper = newArrayWrapper(input, schema, false, strategy);
                    if (input instanceof GraphInput) {
                        ((GraphInput) input).updateLast(arrayWrapper.array, owner);
                    }
                    strategy.COLLECTION_SCHEMA.mergeFrom(input, arrayWrapper);
                    return arrayWrapper.array;
                }
            case ID_OBJECT:
                if (input.readUInt32() != 0) throw new ProtostuffException("Corrupt input.");
                value = new Object();
                break;
            case ID_ARRAY_MAPPED:
                {
                    final ArrayWrapper mArrayWrapper = newArrayWrapper(input, schema, true, strategy);
                    if (input instanceof GraphInput) {
                        ((GraphInput) input).updateLast(mArrayWrapper.array, owner);
                    }
                    strategy.COLLECTION_SCHEMA.mergeFrom(input, mArrayWrapper);
                    return mArrayWrapper.array;
                }
            case ID_CLASS:
                value = strategy.resolveClassFrom(input, false, false);
                break;
            case ID_CLASS_MAPPED:
                value = strategy.resolveClassFrom(input, true, false);
                break;
            case ID_CLASS_ARRAY:
                value = getArrayClass(input, schema, strategy.resolveClassFrom(input, false, true));
                break;
            case ID_CLASS_ARRAY_MAPPED:
                value = getArrayClass(input, schema, strategy.resolveClassFrom(input, true, true));
                break;
            case ID_ENUM:
                {
                    final EnumIO<?> eio = strategy.resolveEnumFrom(input);
                    if (input.readFieldNumber(schema) != ID_ENUM_VALUE) throw new ProtostuffException("Corrupt input.");
                    value = eio.readFrom(input);
                    break;
                }
            case ID_ENUM_SET:
                {
                    final Collection<?> es = strategy.resolveEnumFrom(input).newEnumSet();
                    if (input instanceof GraphInput) {
                        ((GraphInput) input).updateLast(es, owner);
                    }
                    strategy.COLLECTION_SCHEMA.mergeFrom(input, (Collection<Object>) es);
                    return es;
                }
            case ID_ENUM_MAP:
                {
                    final Map<?, Object> em = strategy.resolveEnumFrom(input).newEnumMap();
                    if (input instanceof GraphInput) {
                        ((GraphInput) input).updateLast(em, owner);
                    }
                    strategy.MAP_SCHEMA.mergeFrom(input, (Map<Object, Object>) em);
                    return em;
                }
            case ID_COLLECTION:
                {
                    final Collection<Object> collection = strategy.resolveCollectionFrom(input).newMessage();
                    if (input instanceof GraphInput) {
                        ((GraphInput) input).updateLast(collection, owner);
                    }
                    strategy.COLLECTION_SCHEMA.mergeFrom(input, collection);
                    return collection;
                }
            case ID_MAP:
                {
                    final Map<Object, Object> map = strategy.resolveMapFrom(input).newMessage();
                    if (input instanceof GraphInput) {
                        ((GraphInput) input).updateLast(map, owner);
                    }
                    strategy.MAP_SCHEMA.mergeFrom(input, map);
                    return map;
                }
            case ID_POLYMORPHIC_COLLECTION:
                {
                    if (0 != input.readUInt32()) throw new ProtostuffException("Corrupt input.");
                    final Object collection = PolymorphicCollectionSchema.readObjectFrom(input, strategy.POLYMORPHIC_COLLECTION_SCHEMA, owner, strategy);
                    if (input instanceof GraphInput) {
                        ((GraphInput) input).updateLast(collection, owner);
                    }
                    return collection;
                }
            case ID_POLYMORPHIC_MAP:
                {
                    if (0 != input.readUInt32()) throw new ProtostuffException("Corrupt input.");
                    final Object map = PolymorphicMapSchema.readObjectFrom(input, strategy.POLYMORPHIC_MAP_SCHEMA, owner, strategy);
                    if (input instanceof GraphInput) {
                        ((GraphInput) input).updateLast(map, owner);
                    }
                    return map;
                }
            case ID_DELEGATE:
                {
                    final Delegate<Object> delegate = strategy.resolveDelegateFrom(input);
                    if (1 != input.readFieldNumber(schema)) throw new ProtostuffException("Corrupt input.");
                    value = delegate.readFrom(input);
                    break;
                }
            case ID_THROWABLE:
                return PolymorphicThrowableSchema.readObjectFrom(input, schema, owner, strategy, number);
            case ID_POJO:
                {
                    final Schema<Object> derivedSchema = strategy.resolvePojoFrom(input, number).getSchema();
                    final Object pojo = derivedSchema.newMessage();
                    if (input instanceof GraphInput) {
                        ((GraphInput) input).updateLast(pojo, owner);
                    }
                    derivedSchema.mergeFrom(input, pojo);
                    return pojo;
                }
            default:
                throw new ProtostuffException("Corrupt input.  Unknown field number: " + number);
        }
        if (input instanceof GraphInput) {
            ((GraphInput) input).updateLast(value, owner);
        }
        if (input.readFieldNumber(schema) != 0) throw new ProtostuffException("Corrupt input.");
        return value;
    }

    @SuppressWarnings("unchecked")
    static void writeObjectTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy) throws IOException {
        final Class<Object> clazz = (Class<Object>) value.getClass();
        final Delegate<Object> delegate = strategy.tryWriteDelegateIdTo(output, ID_DELEGATE, clazz);
        if (delegate != null) {
            delegate.writeTo(output, 1, value, false);
            return;
        }
        final RuntimeFieldFactory<Object> inline = RuntimeFieldFactory.getInline(clazz);
        if (inline != null) {
            inline.writeTo(output, inline.id, value, false);
            return;
        }
        if (Message.class.isAssignableFrom(clazz)) {
            final Schema<Object> schema = strategy.writeMessageIdTo(output, ID_POJO, (Message<Object>) value);
            if (output instanceof StatefulOutput) {
                ((StatefulOutput) output).updateLast(schema, currentSchema);
            }
            schema.writeTo(output, value);
            return;
        }
        if (clazz.isEnum()) {
            strategy.writeEnumIdTo(output, ID_ENUM, clazz);
            EnumIO.writeTo(output, ID_ENUM_VALUE, false, (Enum<?>) value);
            return;
        }
        if (clazz.getSuperclass() != null && clazz.getSuperclass().isEnum()) {
            strategy.writeEnumIdTo(output, ID_ENUM, clazz.getSuperclass());
            EnumIO.writeTo(output, ID_ENUM_VALUE, false, (Enum<?>) value);
            return;
        }
        if (clazz.isArray()) {
            int dimensions = 1;
            Class<?> componentType = clazz.getComponentType();
            while (componentType.isArray()) {
                dimensions++;
                componentType = componentType.getComponentType();
            }
            strategy.writeArrayIdTo(output, componentType);
            output.writeUInt32(ID_ARRAY_LEN, Array.getLength(value), false);
            output.writeUInt32(ID_ARRAY_DIMENSION, dimensions, false);
            if (output instanceof StatefulOutput) {
                ((StatefulOutput) output).updateLast(strategy.ARRAY_SCHEMA, currentSchema);
            }
            strategy.ARRAY_SCHEMA.writeTo(output, value);
            return;
        }
        if (Object.class == clazz) {
            output.writeUInt32(ID_OBJECT, 0, false);
            return;
        }
        if (Class.class == value.getClass()) {
            final Class<?> c = ((Class<?>) value);
            if (c.isArray()) {
                int dimensions = 1;
                Class<?> componentType = c.getComponentType();
                while (componentType.isArray()) {
                    dimensions++;
                    componentType = componentType.getComponentType();
                }
                strategy.writeClassIdTo(output, componentType, true);
                output.writeUInt32(ID_ARRAY_DIMENSION, dimensions, false);
                return;
            }
            strategy.writeClassIdTo(output, c, false);
            return;
        }
        if (Map.class.isAssignableFrom(clazz)) {
            if (Collections.class == clazz.getDeclaringClass()) {
                output.writeUInt32(ID_POLYMORPHIC_MAP, 0, false);
                if (output instanceof StatefulOutput) {
                    ((StatefulOutput) output).updateLast(strategy.POLYMORPHIC_MAP_SCHEMA, currentSchema);
                }
                PolymorphicMapSchema.writeNonPublicMapTo(output, value, strategy.POLYMORPHIC_MAP_SCHEMA, strategy);
                return;
            }
            if (EnumMap.class.isAssignableFrom(clazz)) {
                strategy.writeEnumIdTo(output, ID_ENUM_MAP, EnumIO.getKeyTypeFromEnumMap(value));
            } else {
                strategy.writeMapIdTo(output, ID_MAP, clazz);
            }
            if (output instanceof StatefulOutput) {
                ((StatefulOutput) output).updateLast(strategy.MAP_SCHEMA, currentSchema);
            }
            strategy.MAP_SCHEMA.writeTo(output, (Map<Object, Object>) value);
            return;
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            if (Collections.class == clazz.getDeclaringClass()) {
                output.writeUInt32(ID_POLYMORPHIC_COLLECTION, 0, false);
                if (output instanceof StatefulOutput) {
                    ((StatefulOutput) output).updateLast(strategy.POLYMORPHIC_COLLECTION_SCHEMA, currentSchema);
                }
                PolymorphicCollectionSchema.writeNonPublicCollectionTo(output, value, strategy.POLYMORPHIC_COLLECTION_SCHEMA, strategy);
                return;
            }
            if (EnumSet.class.isAssignableFrom(clazz)) {
                strategy.writeEnumIdTo(output, ID_ENUM_SET, EnumIO.getElementTypeFromEnumSet(value));
            } else {
                strategy.writeCollectionIdTo(output, ID_COLLECTION, clazz);
            }
            if (output instanceof StatefulOutput) {
                ((StatefulOutput) output).updateLast(strategy.COLLECTION_SCHEMA, currentSchema);
            }
            strategy.COLLECTION_SCHEMA.writeTo(output, (Collection<Object>) value);
            return;
        }
        if (Throwable.class.isAssignableFrom(clazz)) {
            PolymorphicThrowableSchema.writeObjectTo(output, value, currentSchema, strategy);
            return;
        }
        final Schema<Object> schema = strategy.writePojoIdTo(output, ID_POJO, clazz);
        if (output instanceof StatefulOutput) {
            ((StatefulOutput) output).updateLast(schema, currentSchema);
        }
        schema.writeTo(output, value);
    }

    static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy) throws IOException {
        final int number = input.readFieldNumber(pipeSchema.wrappedSchema);
        switch(number) {
            case ID_BOOL:
                BOOL.transfer(pipe, input, output, number, false);
                break;
            case ID_BYTE:
                BYTE.transfer(pipe, input, output, number, false);
                break;
            case ID_CHAR:
                CHAR.transfer(pipe, input, output, number, false);
                break;
            case ID_SHORT:
                SHORT.transfer(pipe, input, output, number, false);
                break;
            case ID_INT32:
                INT32.transfer(pipe, input, output, number, false);
                break;
            case ID_INT64:
                INT64.transfer(pipe, input, output, number, false);
                break;
            case ID_FLOAT:
                FLOAT.transfer(pipe, input, output, number, false);
                break;
            case ID_DOUBLE:
                DOUBLE.transfer(pipe, input, output, number, false);
                break;
            case ID_STRING:
                STRING.transfer(pipe, input, output, number, false);
                break;
            case ID_BYTES:
                BYTES.transfer(pipe, input, output, number, false);
                break;
            case ID_BYTE_ARRAY:
                BYTE_ARRAY.transfer(pipe, input, output, number, false);
                break;
            case ID_BIGDECIMAL:
                BIGDECIMAL.transfer(pipe, input, output, number, false);
                break;
            case ID_BIGINTEGER:
                BIGINTEGER.transfer(pipe, input, output, number, false);
                break;
            case ID_DATE:
                DATE.transfer(pipe, input, output, number, false);
                break;
            case ID_ARRAY:
                transferArray(pipe, input, output, number, pipeSchema, false, strategy);
                return;
            case ID_OBJECT:
                output.writeUInt32(number, input.readUInt32(), false);
                break;
            case ID_ARRAY_MAPPED:
                transferArray(pipe, input, output, number, pipeSchema, true, strategy);
                return;
            case ID_CLASS:
                transferClass(pipe, input, output, number, pipeSchema, false, false, strategy);
                break;
            case ID_CLASS_MAPPED:
                transferClass(pipe, input, output, number, pipeSchema, true, false, strategy);
                break;
            case ID_CLASS_ARRAY:
                transferClass(pipe, input, output, number, pipeSchema, false, true, strategy);
                break;
            case ID_CLASS_ARRAY_MAPPED:
                transferClass(pipe, input, output, number, pipeSchema, true, true, strategy);
                break;
            case ID_ENUM:
                strategy.transferEnumId(input, output, number);
                if (input.readFieldNumber(pipeSchema.wrappedSchema) != ID_ENUM_VALUE) throw new ProtostuffException("Corrupt input.");
                EnumIO.transfer(pipe, input, output, 1, false);
                break;
            case ID_ENUM_SET:
                strategy.transferEnumId(input, output, number);
                if (output instanceof StatefulOutput) {
                    ((StatefulOutput) output).updateLast(strategy.COLLECTION_PIPE_SCHEMA, pipeSchema);
                }
                Pipe.transferDirect(strategy.COLLECTION_PIPE_SCHEMA, pipe, input, output);
                return;
            case ID_ENUM_MAP:
                strategy.transferEnumId(input, output, number);
                if (output instanceof StatefulOutput) {
                    ((StatefulOutput) output).updateLast(strategy.MAP_PIPE_SCHEMA, pipeSchema);
                }
                Pipe.transferDirect(strategy.MAP_PIPE_SCHEMA, pipe, input, output);
                return;
            case ID_COLLECTION:
                strategy.transferCollectionId(input, output, number);
                if (output instanceof StatefulOutput) {
                    ((StatefulOutput) output).updateLast(strategy.COLLECTION_PIPE_SCHEMA, pipeSchema);
                }
                Pipe.transferDirect(strategy.COLLECTION_PIPE_SCHEMA, pipe, input, output);
                return;
            case ID_MAP:
                strategy.transferMapId(input, output, number);
                if (output instanceof StatefulOutput) {
                    ((StatefulOutput) output).updateLast(strategy.MAP_PIPE_SCHEMA, pipeSchema);
                }
                Pipe.transferDirect(strategy.MAP_PIPE_SCHEMA, pipe, input, output);
                return;
            case ID_POLYMORPHIC_COLLECTION:
                if (0 != input.readUInt32()) throw new ProtostuffException("Corrupt input.");
                output.writeUInt32(number, 0, false);
                if (output instanceof StatefulOutput) {
                    ((StatefulOutput) output).updateLast(strategy.POLYMORPHIC_COLLECTION_PIPE_SCHEMA, pipeSchema);
                }
                Pipe.transferDirect(strategy.POLYMORPHIC_COLLECTION_PIPE_SCHEMA, pipe, input, output);
                return;
            case ID_POLYMORPHIC_MAP:
                if (0 != input.readUInt32()) throw new ProtostuffException("Corrupt input.");
                output.writeUInt32(number, 0, false);
                if (output instanceof StatefulOutput) {
                    ((StatefulOutput) output).updateLast(strategy.POLYMORPHIC_MAP_PIPE_SCHEMA, pipeSchema);
                }
                Pipe.transferDirect(strategy.POLYMORPHIC_MAP_PIPE_SCHEMA, pipe, input, output);
                return;
            case ID_DELEGATE:
                {
                    final Delegate<Object> delegate = strategy.transferDelegateId(input, output, number);
                    if (1 != input.readFieldNumber(pipeSchema.wrappedSchema)) throw new ProtostuffException("Corrupt input.");
                    delegate.transfer(pipe, input, output, 1, false);
                    break;
                }
            case ID_THROWABLE:
                PolymorphicThrowableSchema.transferObject(pipeSchema, pipe, input, output, strategy, number);
                return;
            case ID_POJO:
                final Pipe.Schema<Object> derivedPipeSchema = strategy.transferPojoId(input, output, number).getPipeSchema();
                if (output instanceof StatefulOutput) {
                    ((StatefulOutput) output).updateLast(derivedPipeSchema, pipeSchema);
                }
                Pipe.transferDirect(derivedPipeSchema, pipe, input, output);
                return;
            default:
                throw new ProtostuffException("Corrupt input.  Unknown field number: " + number);
        }
        if (input.readFieldNumber(pipeSchema.wrappedSchema) != 0) throw new ProtostuffException("Corrupt input.");
    }

    /**
     * An array wrapper internally used for adding objects.
     */
    static final class ArrayWrapper implements Collection<Object> {

        final Object array;

        int offset = 0;

        ArrayWrapper(Object array) {
            this.array = array;
        }

        public boolean add(Object value) {
            Array.set(array, offset++, value);
            return true;
        }

        public boolean addAll(Collection<? extends Object> arg0) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean contains(Object arg0) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection<?> arg0) {
            throw new UnsupportedOperationException();
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        public Iterator<Object> iterator() {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object arg0) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> arg0) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> arg0) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            throw new UnsupportedOperationException();
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        public <T> T[] toArray(T[] arg0) {
            throw new UnsupportedOperationException();
        }
    }
}
