public class Test {        public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated) throws IOException {
            output.writeDouble(number, input.readDouble(), repeated);
        }
}