public class Test {    void receiveDeviceGotoCommand(UPBMessage theMessage) {
        updateInternalDeviceLevel(theMessage.getLevel(), theMessage.getFadeRate(), theMessage.getChannel());
    }
}