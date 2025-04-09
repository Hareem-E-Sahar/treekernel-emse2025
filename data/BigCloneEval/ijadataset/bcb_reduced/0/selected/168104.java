package org.jul.dsm.representation;

import java.util.*;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Map.Entry;
import org.jul.dsm.*;
import org.jul.dsm.constructor.PrimitiveArrayConstructor;

public class PrimitiveArrayRepresentation extends Representation {

    public static class PrimitiveArrayChange extends AbstractChange {

        private final Map<Integer, Object> changes = new HashMap<Integer, Object>();

        private Class<?> primitiveType;

        public void decode(DataInput input, IdEncoder idEncoder, ClassLoader classLoader) throws IOException {
            int changesNumber = input.readInt();
            byte primitiveType = input.readByte();
            this.primitiveType = Utils.getPrimitiveType(primitiveType);
            for (int i = 0; i < changesNumber; i++) {
                changes.put(input.readInt(), Utils.readPrimitive(input, primitiveType));
            }
        }

        public void encode(DataOutput output, IdEncoder idEncoder) throws IOException {
            if (primitiveType != null) {
                byte type = Utils.getPrimitiveType(primitiveType);
                output.writeInt(changes.size());
                output.writeByte(type);
                for (Entry<Integer, Object> entry : changes.entrySet()) {
                    output.writeInt(entry.getKey());
                    Utils.writePrimitive(output, type, entry.getValue());
                }
            } else {
                throw new IOException("Unitialized change!");
            }
        }

        public Set<Long> getFinalRepresentationsRequired() {
            return null;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(super.toString());
            builder.append(" [");
            int initialLength = builder.length();
            for (Entry<Integer, Object> entry : changes.entrySet()) {
                if (builder.length() > initialLength) {
                    builder.append(", ");
                }
                builder.append(entry.getKey());
                builder.append(" = ");
                builder.append(entry.getValue());
            }
            builder.append("]");
            return builder.toString();
        }

        public PrimitiveArrayChange(ConstructorFactory constructorFactory, Class<?> primitiveType) {
            super(constructorFactory);
            this.primitiveType = primitiveType;
        }

        public PrimitiveArrayChange(ConstructorFactory constructorFactory) {
            this(constructorFactory, null);
        }
    }

    private final int length;

    private Object array = null;

    @Override
    public Change clone(PatchConstructor patchConstructor) {
        if (array == null) {
            throw new UnitializedRepresentationException();
        } else {
            PrimitiveArrayChange change = new PrimitiveArrayChange(getConstructorFactory(), getReference().getClass().getComponentType());
            for (int i = 0; i < length; i++) {
                change.changes.put(i, Array.get(getReference(), i));
            }
            return change;
        }
    }

    @Override
    public Change findChanges(PatchConstructor patchConstructor) {
        PrimitiveArrayChange change = null;
        if (array == null || patchConstructor.isPatchFromScratch()) {
            change = new PrimitiveArrayChange(getConstructorFactory(), getReference().getClass().getComponentType());
            for (int i = 0; i < length; i++) {
                change.changes.put(i, Array.get(getReference(), i));
            }
        } else {
            for (int i = 0; i < length; i++) {
                Object element = Array.get(getReference(), i);
                if (!Array.get(array, i).equals(element)) {
                    if (change == null) {
                        change = new PrimitiveArrayChange(getConstructorFactory(), getReference().getClass().getComponentType());
                    }
                    change.changes.put(i, element);
                }
            }
        }
        return change;
    }

    @Override
    public void apply(PatchApplicator patchApplicator, Change change) {
        if (change instanceof PrimitiveArrayChange) {
            PrimitiveArrayChange primitiveArrayChange = (PrimitiveArrayChange) change;
            if (array == null) {
                array = Array.newInstance(getReference().getClass().getComponentType(), length);
            }
            for (Entry<Integer, Object> entry : primitiveArrayChange.changes.entrySet()) {
                Array.set(array, entry.getKey(), entry.getValue());
            }
            if (patchApplicator.isOverride()) {
                System.arraycopy(array, 0, getReference(), 0, length);
            }
        } else {
            raiseIncompatibleChange(change);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append(" [");
        if (array == null) {
            builder.append("UNDEFINED!");
        } else {
            for (int i = 0; i < length; i++) {
                if (i != 0) {
                    builder.append(", ");
                }
                builder.append(Array.get(array, i));
            }
        }
        builder.append(']');
        return builder.toString();
    }

    public PrimitiveArrayRepresentation(SharedMemory memory, Representation parentRepresentation, long id, Object reference) {
        super(memory, parentRepresentation, id, reference);
        this.length = Array.getLength(reference);
        setConstructor(new PrimitiveArrayConstructor(getConstructorFactory(), length, getReference().getClass().getComponentType()));
    }
}
