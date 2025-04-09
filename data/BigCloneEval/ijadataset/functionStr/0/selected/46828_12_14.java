public class Test {    public void run(Emulator em) throws EmulatorException {
        em.writePC(em.readPC() + this.imm);
    }
}