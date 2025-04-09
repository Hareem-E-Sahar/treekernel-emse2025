package com.frinika.renderer;

import java.util.Arrays;
import java.util.zip.CRC32;
import javax.sound.midi.MidiEvent;

public class MidiPacket {

    public long length = 0;

    public int channel = 0;

    public int[] activenotes = null;

    public int[] activenotes_velocity = null;

    public int[] controls = null;

    public int[] controls_values = null;

    public int program = -1;

    public int pitchbend_data1 = -1;

    public int pitchbend_data2 = -1;

    public MidiEvent[] events = null;

    private boolean checksumset = false;

    private int checksum_value = 0;

    public int checksum() {
        if (checksumset) return checksum_value;
        CRC32 crc32 = new CRC32();
        crc32.update((byte) channel);
        crc32.update((byte) program);
        crc32.update((byte) pitchbend_data1);
        crc32.update((byte) pitchbend_data2);
        for (int i = 0; i < events.length; i++) {
            crc32.update(events[i].getMessage().getMessage());
        }
        if (controls != null) {
            int[] sorted = new int[controls.length];
            for (int i = 0; i < controls.length; i++) {
                sorted[i] = (controls[i] << 2) + controls_values[i];
            }
            Arrays.sort(sorted);
            for (int i = 0; i < sorted.length; i++) {
                crc32.update((byte) (sorted[i] & 0xFF));
                crc32.update((byte) ((sorted[i] & 0xFF00) >> 2));
            }
        }
        if (activenotes != null) {
            int[] sorted = new int[activenotes.length];
            for (int i = 0; i < activenotes.length; i++) {
                sorted[i] = (activenotes[i] << 2) + activenotes_velocity[i];
            }
            Arrays.sort(sorted);
            for (int i = 0; i < sorted.length; i++) {
                crc32.update((byte) (sorted[i] & 0xFF));
                crc32.update((byte) ((sorted[i] & 0xFF00) >> 2));
            }
        }
        checksumset = true;
        checksum_value = (int) crc32.getValue();
        return checksum_value;
    }
}
