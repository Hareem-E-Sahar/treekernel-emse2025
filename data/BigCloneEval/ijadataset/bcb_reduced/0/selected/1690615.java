package synthdrivers.Line6BassPod;

import core.*;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.InvalidMidiDataException;

/** POD generic Continuous Controller message sender class. 
* @author Jeff Weber
*/
class CCSender extends SysexSender implements SysexWidget.ISender {

    /** Represents the Continuous Controller number. */
    private int param;

    /** Scaling factor used to normalize range of values sent by sysexWidget.
    * When the associated widget sends a range of values other than 0 to 127,
    * the multiplier can be used to normalize the range back to 0 to 127.
    * e.g if the widget sends a range 0 to 1, the scaling factor can be set to
    * 127.
    */
    private int multiplier;

    /** Reverses the range of values sent by the sysexWidget over the range
        * 127 to 0. A value of true causes the range to be reversed.
        */
    private boolean reverse = false;

    /** Offset added to the values sent by the sysexWidget. Used when a 
        controller has a range that starts with something other than zero.
        */
    private int offset = 0;

    /** Constructs a CCSender for the given CC number.
        * The CC number is given by param.
        * This constructor is used when the range of values sent by the
        * associated widget is 0 to 127 and the range does not need to be
        * reversed.
        */
    CCSender(int param) {
        this(param, 1, false, 0);
    }

    /** Constructs a CCSender for the given CC number, with an option to reverse
        * the value. This constructor is used when the range of values sent by the
        * associated widget is 0 to 127 and the output to the device needs to
        * be reversed.
        */
    CCSender(int param, boolean reverse) {
        this(param, 1, reverse, 0);
    }

    /** Constructs a CCSender for the given CC number, where the output to the
        * device needs to be scaled by a multiplier.
        * multiplier is a scaling factor applied to the output value.
        */
    CCSender(int param, int multiplier) {
        this(param, multiplier, false, 0);
    }

    /** Constructs a CCSender for the given CC number, where the output to the
        * device needs to be reversed and an offset applied.
        * This constructor is used when values sent by the SysexWidget both  
        * needs to have an offset applied and needs to be reversed over
        * the range 127 to 0.
        */
    CCSender(int param, boolean reverse, int offset) {
        this(param, 1, reverse, offset);
    }

    /** This is the base constructor for CCSender.
        * param is the CC number.
        * multiplier is a scaling factor that is applied to the output value.
        * A reverse value of true reverses the value over the range.
        * The offset value is added to the input value before it is sent.
        */
    private CCSender(int param, int multiplier, boolean reverse, int offset) {
        this.param = param;
        this.multiplier = multiplier;
        this.reverse = reverse;
        this.offset = offset;
    }

    /** Sends a CC message for the given parameter and value.
        * IPatchDriver driver supplies the reference to the driver.
        * int value supplies the value of the associated widget.
        */
    public void send(IPatchDriver driver, int value) {
        value = Math.min(127, value * multiplier);
        if (reverse) {
            value = 127 - value;
        }
        value = Math.min(127, value + offset);
        ShortMessage m = new ShortMessage();
        try {
            m.setMessage(ShortMessage.CONTROL_CHANGE, ((Driver) driver).getChannel() - 1, param, value);
            driver.send(m);
        } catch (InvalidMidiDataException e) {
            ErrorMsg.reportStatus(e);
        }
    }
}
