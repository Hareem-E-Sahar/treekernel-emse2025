package jm.midi.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Andrew Sorensen
 */
public final class TimeSig implements Event {

    private final short id;

    private int time;

    private int numerator;

    private int denominator;

    /**Default Constructor*/
    public TimeSig() {
        this.id = 021;
        this.time = 0;
        this.numerator = 4;
        this.denominator = 4;
    }

    public TimeSig(int numerator, int denominator) {
        this.id = 021;
        this.time = 0;
        this.denominator = denominator;
        this.numerator = numerator;
    }

    public TimeSig(int time, int numerator, int denominator) {
        this.id = 021;
        this.time = time;
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public int getDenominator() {
        return this.denominator;
    }

    public void setDenominator(int denom) {
        this.denominator = denom;
    }

    public int getNumerator() {
        return this.numerator;
    }

    public void setNumerator(int numer) {
        this.numerator = numer;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public short getID() {
        return id;
    }

    public int write(DataOutputStream dos) throws IOException {
        int bytes_out = jm.midi.MidiUtil.writeVarLength(this.time, dos);
        dos.writeByte(0xFF);
        dos.writeByte(0x58);
        bytes_out += jm.midi.MidiUtil.writeVarLength(4, dos);
        dos.writeByte((byte) this.numerator);
        int num = this.denominator;
        int cnt = 0;
        while (num % 2 == 0) {
            num = num / 2;
            cnt++;
        }
        dos.writeByte((byte) cnt);
        dos.writeByte(0x18);
        dos.writeByte(0x08);
        return bytes_out + 6;
    }

    public int read(DataInputStream dis) throws IOException {
        return 0;
    }

    public Event copy() throws CloneNotSupportedException {
        TimeSig event;
        try {
            event = (TimeSig) this.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println(e);
            event = new TimeSig();
        }
        return event;
    }

    public void print() {
        System.out.println("TimeSig(021):             [time = " + this.time + "][numerator = " + this.numerator + "][denominator = " + this.denominator + "]");
    }
}
