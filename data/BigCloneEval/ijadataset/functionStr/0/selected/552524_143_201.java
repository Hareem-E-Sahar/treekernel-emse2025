public class Test {    public void send(NuPlayMidiMessage message, long timeStamp) {
        OSCMessage osc_message = new OSCMessage();
        osc_message.setAddress("/nuplay/channel_" + this.channel);
        osc_message.addArgument(new String("midi"));
        NuPlayMidiMessage midi_message = new NuPlayMidiMessage(message.getMessage());
        switch(midi_message.getCommand()) {
            case NuPlayMidiMessage.PITCH_BEND:
                error_label.setText("pitch bend :: d1=" + midi_message.getData1() + " , d2=" + midi_message.getData2());
                osc_message.addArgument(new String("pitch_bend"));
                osc_message.addArgument(new Integer(midi_message.getChannel()));
                osc_message.addArgument(new Integer(midi_message.getData1()));
                osc_message.addArgument(new Integer(midi_message.getData2()));
                osc_message.addArgument(new String(""));
                break;
            case NuPlayMidiMessage.NOTE_ON:
                error_label.setText("note on :: n=" + midi_message.getData1() + ", v=" + midi_message.getData2());
                osc_message.addArgument(new String("note_on"));
                osc_message.addArgument(new Integer(midi_message.getChannel()));
                osc_message.addArgument(new Integer(midi_message.getData1()));
                osc_message.addArgument(new Integer(midi_message.getData2()));
                osc_message.addArgument(new String(""));
                break;
            case NuPlayMidiMessage.NOTE_OFF:
                error_label.setText("note off :: n=" + midi_message.getData1() + ", v=" + midi_message.getData2());
                osc_message.addArgument(new String("note_off"));
                osc_message.addArgument(new Integer(midi_message.getChannel()));
                osc_message.addArgument(new Integer(midi_message.getData1()));
                osc_message.addArgument(new Integer(midi_message.getData2()));
                osc_message.addArgument(new String(""));
                break;
            case NuPlayMidiMessage.SYSEX_START:
                error_label.setText("sysex :: data=" + message.getStringMessage());
                osc_message.addArgument(new String("sys_ex"));
                osc_message.addArgument(new Integer(midi_message.getChannel()));
                osc_message.addArgument(new Integer(-1));
                osc_message.addArgument(new Integer(-1));
                System.out.println(message.getStringMessage());
                osc_message.addArgument(message.getStringMessage());
                break;
            case NuPlayMidiMessage.CONTROL_CHANGE:
                error_label.setText("control change :: c=" + midi_message.getData1() + ", d=" + midi_message.getData2());
                osc_message.addArgument(new String("control_change"));
                osc_message.addArgument(new Integer(midi_message.getChannel()));
                osc_message.addArgument(new Integer(midi_message.getData1()));
                osc_message.addArgument(new Integer(midi_message.getData2()));
                osc_message.addArgument(new String(""));
                break;
        }
        if (client_socket.isConnected()) {
            if (stream_out != null) {
                try {
                    stream_out.write(osc_message.getByteArray());
                } catch (IOException e) {
                    this.client.disconnect("NuPlay not active?");
                    e.printStackTrace();
                }
            }
        }
    }
}