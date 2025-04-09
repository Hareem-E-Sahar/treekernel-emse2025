package ps.net;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import ps.server.trigger.TriggerEntry;

public class TriggerDescContent implements PacketContent {

    public static final int CMD_ADD = 1;

    public static final int CMD_REMOVE = 2;

    public static final String NO_CATEGORY = "<keine Kategorie>";

    int cmd;

    TriggerEntry[] triggers;

    ByteArrayOutputStream zipedBytes = new ByteArrayOutputStream(16 * 1024);

    TriggerDescContent() {
    }

    public TriggerDescContent(int cmd, TriggerEntry trigger) {
        this(cmd, new TriggerEntry[] { trigger });
    }

    public TriggerDescContent(int cmd, TriggerEntry[] triggers) {
        this.cmd = cmd;
        this.triggers = triggers;
        try {
            zipContent(zipedBytes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void writeContent(OutputStream out) throws IOException {
        zipedBytes.writeTo(out);
    }

    private void zipContent(OutputStream out) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(out);
        zipOut.putNextEntry(new ZipEntry("TriggerDescContent"));
        writeContentUncompressed(zipOut);
        zipOut.closeEntry();
    }

    private void writeContentUncompressed(OutputStream out) throws IOException {
        out.write(cmd);
        Packet.write2ByteNumber(out, triggers.length);
        for (int i = 0; i < triggers.length; i++) {
            Packet.write2ByteNumber(out, triggers[i].getId());
            Packet.writeString(out, triggers[i].getTitle());
            Packet.writeBoolean(out, triggers[i].isActive());
            Packet.writeString(out, triggers[i].getCategory());
            Packet.writeString(out, triggers[i].getRegex());
            Packet.write2ByteNumber(out, triggers[i].getQuantity());
            Packet.write2ByteNumber(out, triggers[i].getIgnoreTimer());
            Packet.writeBoolean(out, triggers[i].isServerMsgActive());
            Packet.writeString(out, triggers[i].getServerMsg());
            out.write(triggers[i].getServerMsgSize());
            out.write(triggers[i].getServerMsgColor().getRed());
            out.write(triggers[i].getServerMsgColor().getGreen());
            out.write(triggers[i].getServerMsgColor().getBlue());
            Packet.writeBoolean(out, triggers[i].isSoundActive());
            Packet.writeString(out, triggers[i].getSound());
            Packet.writeBoolean(out, triggers[i].isTimerActive());
            Packet.writeBoolean(out, triggers[i].isTimerShow1());
            Packet.writeBoolean(out, triggers[i].isTimerShow2());
            Packet.write2ByteNumber(out, triggers[i].getTimerPeriod());
            Packet.write2ByteNumber(out, triggers[i].getTimerWarning());
            Packet.writeString(out, triggers[i].getTimerWarningMsg());
            out.write(triggers[i].getTimerWarningMsgSize());
            out.write(triggers[i].getTimerWarningMsgColor().getRed());
            out.write(triggers[i].getTimerWarningMsgColor().getGreen());
            out.write(triggers[i].getTimerWarningMsgColor().getBlue());
            Packet.writeString(out, triggers[i].getTimerWarningSound());
            Packet.write2ByteNumber(out, triggers[i].getTimerRemove());
        }
    }

    @Override
    public void readContent(InputStream in) throws IOException {
        ZipInputStream zipIn = new ZipInputStream(in);
        ZipEntry entry = zipIn.getNextEntry();
        if (entry != null) {
            readContentUncompressed(zipIn);
        }
        zipIn.closeEntry();
    }

    public void readContentUncompressed(InputStream in) throws IOException {
        cmd = in.read();
        int length = Packet.read2ByteNumber(in);
        triggers = new TriggerEntry[length];
        for (int i = 0; i < triggers.length; i++) {
            triggers[i] = new TriggerEntry();
            triggers[i].setId(Packet.read2ByteNumber(in));
            triggers[i].setTitle(Packet.readString(in));
            triggers[i].setActive(Packet.readBoolean(in));
            triggers[i].setCategory(Packet.readString(in));
            triggers[i].setRegex(Packet.readString(in));
            triggers[i].setQuantity(Packet.read2ByteNumber(in));
            triggers[i].setIgnoreTimer(Packet.read2ByteNumber(in));
            triggers[i].setServerMsgActive(Packet.readBoolean(in));
            triggers[i].setServerMsg(Packet.readString(in));
            triggers[i].setServerMsgSize(in.read());
            int red = in.read();
            int green = in.read();
            int blue = in.read();
            triggers[i].setServerMsgColor(new Color(red, green, blue));
            triggers[i].setSoundActive(Packet.readBoolean(in));
            triggers[i].setSound(Packet.readString(in));
            triggers[i].setTimerActive(Packet.readBoolean(in));
            triggers[i].setTimerShow1(Packet.readBoolean(in));
            triggers[i].setTimerShow2(Packet.readBoolean(in));
            triggers[i].setTimerPeriod(Packet.read2ByteNumber(in));
            triggers[i].setTimerWarning(Packet.read2ByteNumber(in));
            triggers[i].setTimerWarningMsg(Packet.readString(in));
            triggers[i].setTimerWarningMsgSize(in.read());
            red = in.read();
            green = in.read();
            blue = in.read();
            triggers[i].setTimerWarningMsgColor(new Color(red, green, blue));
            triggers[i].setTimerWarningSound(Packet.readString(in));
            triggers[i].setTimerRemove(Packet.read2ByteNumber(in));
        }
    }

    @Override
    public String toString() {
        String ret = "[ TriggerDesc |";
        ret += " cmd=\"" + cmd + "\"";
        for (int i = 0; i < triggers.length; i++) {
            ret += "\r\n    ";
            ret += " id=\"" + triggers[i].getId() + "\"";
            ret += " title=\"" + triggers[i].getTitle() + "\"";
            ret += " active=\"" + triggers[i].isActive() + "\"";
            ret += " category=\"" + triggers[i].getCategory() + "\"";
            ret += " regex=\"" + triggers[i].getRegex() + "\"";
            ret += " quantity=\"" + triggers[i].getQuantity() + "\"";
            ret += " ignoreTimer=\"" + triggers[i].getIgnoreTimer() + "\"";
            ret += " serverMsgActive=\"" + triggers[i].isServerMsgActive() + "\"";
            ret += " serverMsg=\"" + triggers[i].getServerMsg() + "\"";
            ret += " size=\"" + triggers[i].getServerMsgSize() + "\"";
            ret += " color=\"" + triggers[i].getServerMsgColor() + "\"";
            ret += " soundActive=\"" + triggers[i].isSoundActive() + "\"";
            ret += " sound=\"" + triggers[i].getSound() + "\"";
            ret += " timerActive=\"" + triggers[i].isTimerActive() + "\"";
            ret += " timerShow1=\"" + triggers[i].isTimerShow1() + "\"";
            ret += " timerShow2=\"" + triggers[i].isTimerShow2() + "\"";
            ret += " timerPeriod=\"" + triggers[i].getTimerPeriod() + "\"";
            ret += " timerWarning=\"" + triggers[i].getTimerWarning() + "\"";
            ret += " timerWarningMsg=\"" + triggers[i].getTimerWarningMsg() + "\"";
            ret += " timerWarningMsgSize=\"" + triggers[i].getTimerWarningMsgSize() + "\"";
            ret += " timerWarningMsgColor=\"" + triggers[i].getTimerWarningMsgColor() + "\"";
            ret += " timerWarningMsgSound=\"" + triggers[i].getTimerWarningSound() + "\"";
            ret += " timerRemove=\"" + triggers[i].getTimerRemove() + "\"";
        }
        ret += " ]";
        return ret;
    }

    public int getCmd() {
        return cmd;
    }

    public TriggerEntry getTriggerEntry() {
        return triggers[0];
    }

    public TriggerEntry[] getTriggerEntries() {
        return triggers;
    }
}
