public class Test {    public String getProgrammeChannelName(final TVProgramme programme) {
        return programme.getChannel().getDisplayName();
    }
}