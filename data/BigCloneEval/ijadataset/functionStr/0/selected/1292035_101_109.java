public class Test {    public void test_cannot_write_back_a_read_entry() {
        this.bb.write(Zone.DEFAULT, "Stefan");
        Object entry = bb.read(new ExactZoneSelector(Zone.DEFAULT), new AnyObjectFilter());
        try {
            this.bb.write(Zone.DEFAULT, entry);
            fail("exception expected when writing back an object that was read from the blackboard");
        } catch (WriteBlackboardException _) {
        }
    }
}