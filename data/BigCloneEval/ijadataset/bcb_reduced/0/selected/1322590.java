package com.limegroup.gnutella.io;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

public class StubAcceptChannelObserver implements AcceptChannelObserver {

    private List channels = new LinkedList();

    private List ioxes = new LinkedList();

    private boolean shutdown;

    public void handleAcceptChannel(SocketChannel channel) throws IOException {
        channels.add(channel);
    }

    public void handleIOException(IOException iox) {
        ioxes.add(iox);
    }

    public void shutdown() {
        this.shutdown = true;
    }

    public List getChannels() {
        return channels;
    }

    public SocketChannel getNextSocketChannel() {
        return (SocketChannel) channels.remove(0);
    }

    public List getIOXes() {
        return ioxes;
    }

    public IOException getNextIOX() {
        return (IOException) ioxes.remove(0);
    }

    public boolean isShutdown() {
        return shutdown;
    }
}
