public class Test {        public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy) {
            final boolean primitive = f.getType().isPrimitive();
            return new Field<T>(FieldType.UINT32, number, name) {

                {
                    f.setAccessible(true);
                }

                public void mergeFrom(Input input, T message) throws IOException {
                    try {
                        if (primitive) f.setByte(message, (byte) input.readUInt32()); else f.set(message, Byte.valueOf((byte) input.readUInt32()));
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                public void writeTo(Output output, T message) throws IOException {
                    try {
                        if (primitive) output.writeUInt32(number, f.getByte(message), false); else {
                            Byte value = (Byte) f.get(message);
                            if (value != null) output.writeUInt32(number, value.byteValue(), false);
                        }
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                public void transfer(Pipe pipe, Input input, Output output, boolean repeated) throws IOException {
                    output.writeUInt32(number, input.readUInt32(), repeated);
                }
            };
        }
}