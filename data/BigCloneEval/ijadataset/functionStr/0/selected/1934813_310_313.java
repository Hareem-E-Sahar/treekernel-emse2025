public class Test {    private void assertDefaultCueChannelLevel(final String message, final CueChannelLevel level) {
        assertDefaultLevelValue(message, level.getChannelLevelValue());
        assertDefaultLevelValue(message, level.getSubmasterLevelValue());
    }
}