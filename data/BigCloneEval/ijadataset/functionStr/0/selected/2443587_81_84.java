public class Test {    @Test(expected = BadParameterException.class)
    public void testOutOfRangeChannel() throws BadParameterException {
        @SuppressWarnings("unused") float f = mono.getChannelData(1)[0];
    }
}