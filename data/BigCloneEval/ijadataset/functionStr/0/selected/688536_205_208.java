public class Test {        @Override
        void copySecKey(RecordInput input, RecordOutput output) {
            output.writeFast(input.readFast());
        }
}