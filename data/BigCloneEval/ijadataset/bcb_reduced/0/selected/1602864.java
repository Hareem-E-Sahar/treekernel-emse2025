package uk.org.toot.audio.server;

import java.util.Hashtable;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.ChannelFormat;

public class MultiIOAudioServer extends AbstractAudioServerDecorator {

    Hashtable<String, IOAudioProcess> outputMap = new Hashtable<String, IOAudioProcess>();

    Hashtable<String, IOAudioProcess> inputMap = new Hashtable<String, IOAudioProcess>();

    public MultiIOAudioServer(AudioServer server) {
        super(server);
    }

    public IOAudioProcess openAudioOutput(String name, String label) throws Exception {
        if (name == null) {
            name = getAvailableOutputNames().get(0);
            System.out.println(label + " null name specified, using " + name);
        }
        IOAudioProcess p = outputMap.get(name);
        System.out.println(name + "   " + p);
        if (p == null) outputMap.put(name, p = new AudioProcessWrapper(super.openAudioOutput(name, label)));
        return p;
    }

    public IOAudioProcess openAudioInput(String name, String label) throws Exception {
        if (name == null) {
            name = getAvailableInputNames().get(0);
            System.out.println(label + " null name specified, using " + name);
        }
        IOAudioProcess p;
        if ((p = inputMap.get(name)) == null) inputMap.put(name, p = new AudioProcessWrapper(super.openAudioInput(name, label)));
        return p;
    }

    class AudioProcessWrapper implements IOAudioProcess {

        IOAudioProcess process;

        int openCount = 0;

        public AudioProcessWrapper(IOAudioProcess process) {
            this.process = process;
        }

        public void open() throws Exception {
            if (openCount == 0) process.open();
            openCount++;
        }

        public int processAudio(AudioBuffer buffer) {
            return process.processAudio(buffer);
        }

        public void close() throws Exception {
            openCount--;
            if (openCount == 0) process.close();
        }

        public ChannelFormat getChannelFormat() {
            return process.getChannelFormat();
        }

        public String getName() {
            return process.getName();
        }
    }

    public void closeAudioInput(IOAudioProcess input) {
    }

    public void closeAudioOutput(IOAudioProcess output) {
    }
}
