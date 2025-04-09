public class Test {    public Object clone() {
        Note note = new Note(getNoteValue(), getOffset(), getDuration(), getVelocity(), getChannel(), getDetune());
        return note;
    }
}