public class Test {        public byte read() {
            highTempReg.write(high.read());
            return low.read();
        }
}