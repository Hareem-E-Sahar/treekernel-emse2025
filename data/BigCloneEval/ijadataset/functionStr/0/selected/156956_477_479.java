public class Test {    public void visit(MSP430Instr.BIC_B i) {
        $write_poly_int8(i.dest, ~$read_poly_int8(i.source) & $read_poly_int8(i.dest));
    }
}