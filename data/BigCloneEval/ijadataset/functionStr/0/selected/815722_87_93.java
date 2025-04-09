public class Test {    private void formatPreferenceChannelItems() {
        if (Utility.isEmpty(disableViewChannel)) {
            disableViewChannel = UserHomePreferenceConstant.SYSTEMPREFERENCESTR;
        }
        disableViewChannel = preferenceMg.fitTheOldChannelConfig(disableViewChannel);
        getChannelSequence();
    }
}