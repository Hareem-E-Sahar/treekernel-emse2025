public class Test {    public void run() {
        Selector selector = null;
        ServerSocketChannel server = null;
        SelectionKey acceptKey = null;
        try {
            selector = Selector.open();
            if (ssl) {
                try {
                    ServerSocket s = null;
                    if (address != null) s = SSLUtil.getSSLServerSocket(port, InetAddress.getByName(address), ssl_keystore, ssl_secrets); else s = SSLUtil.getSSLServerSocket(port, ssl_keystore, ssl_secrets);
                    server = s.getChannel();
                } catch (Exception x) {
                    logger.fatal("error creating SSL socket: " + x);
                    return;
                }
            } else {
                server = ServerSocketChannel.open();
                if (address != null) server.socket().bind(new InetSocketAddress(address, port)); else server.socket().bind(new InetSocketAddress(port));
            }
            logger.warn("server socket channel is " + server);
            server.configureBlocking(false);
            acceptKey = server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ioe) {
            logger.fatal("could not bind server: " + ioe);
            return;
        }
        logger.info("jarsyncd version unknown" + " listening on port " + port);
        String motd = "";
        try {
            StringBuffer buf = new StringBuffer();
            char[] c = new char[512];
            int len;
            FileReader motdIn = new FileReader(motdFile);
            while ((len = motdIn.read(c)) != -1) {
                buf.append(c, 0, len);
            }
            motd = buf.toString();
        } catch (IOException ioe) {
            logger.warn("error reading MOTD file: " + ioe.getMessage());
        }
        StatsModule statsMod = null;
        if (modules.containsKey("#stats")) statsMod = (StatsModule) modules.get("#stats");
        for (; ; ) {
            Protocol prot;
            SocketChannel client;
            try {
                selector.select();
            } catch (IOException ioe) {
                logger.warn(ioe.toString());
                continue;
            }
            Set keys = selector.selectedKeys();
            for (Iterator i = keys.iterator(); i.hasNext(); ) {
                SelectionKey key = (SelectionKey) i.next();
                i.remove();
                if (key == acceptKey) {
                    try {
                        if (key.isAcceptable()) {
                            SocketChannel c = server.accept();
                            c.configureBlocking(false);
                            InetAddress host = c.socket().getInetAddress();
                            SelectionKey newKey = c.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new Protocol(motd, modules, host));
                            logger.info("connection made by " + host.getHostName() + " (" + host.getHostAddress() + ") on port " + c.socket().getPort());
                            if (statsMod != null) {
                                statsMod.currentConnections++;
                                statsMod.numConnections++;
                            }
                        }
                    } catch (IOException ioe) {
                        logger.warn("error accepting connection: " + ioe.getMessage());
                    }
                } else {
                    prot = (Protocol) key.attachment();
                    client = (SocketChannel) key.channel();
                    Statistics stats = prot.getStatistics();
                    try {
                        ByteBuffer out = prot.getOutputBuffer();
                        ByteBuffer in = prot.getInputBuffer();
                        if (key.isWritable()) {
                            prot.updateOutput();
                            out.flip();
                            if (out.hasRemaining()) {
                                int len = client.write(out);
                                stats.total_written += len;
                                if (statsMod != null) statsMod.bytesWritten += len;
                            }
                            out.compact();
                        }
                        int len = 0;
                        if (key.isReadable()) {
                            in.compact();
                            len = client.read(in);
                            in.flip();
                        }
                        if (len > 0) {
                            stats.total_read += len;
                            if (statsMod != null) statsMod.bytesRead += len;
                        }
                        if (in.hasRemaining()) {
                            prot.updateInput();
                        }
                        if (len == -1) {
                            if (in.hasRemaining() || out.position() > 0) logger.warn("connection unexpectedly closed (" + in.position() + " bytes in buffer).");
                            key.cancel();
                            client.close();
                            continue;
                        }
                        if (prot.connectionFinished() && !(in.hasRemaining() || out.position() > 0)) {
                            logger.info("connection to " + client.socket().getInetAddress() + " finished");
                            key.cancel();
                            client.close();
                            if (statsMod != null) statsMod.currentConnections--;
                        }
                    } catch (IOException ioe) {
                        logger.warn(client.socket().getInetAddress() + ": " + ioe);
                        key.cancel();
                        try {
                            client.close();
                        } catch (IOException ioe2) {
                            logger.warn("error closing connection: " + ioe2);
                        }
                        if (statsMod != null) statsMod.currentConnections--;
                    } catch (BufferOverflowException boe) {
                        logger.warn("buffer overflow on connection to " + client.socket().getInetAddress());
                        key.cancel();
                        try {
                            client.close();
                        } catch (IOException ioe) {
                            logger.warn("error closing connection: " + ioe);
                        }
                        if (statsMod != null) statsMod.currentConnections--;
                    } catch (BufferUnderflowException bue) {
                        logger.warn("buffer underflow on connection to " + client.socket().getInetAddress());
                        key.cancel();
                        try {
                            client.close();
                        } catch (IOException ioe) {
                            logger.warn("error closing connection: " + ioe);
                        }
                        if (statsMod != null) statsMod.currentConnections--;
                    } catch (Exception x) {
                        logger.warn("uncaught exception: " + x + " on the connection to " + client.socket().getInetAddress());
                        x.printStackTrace();
                        key.cancel();
                        try {
                            client.close();
                        } catch (IOException ioe) {
                            logger.warn("error closing connection: " + ioe);
                        }
                        if (statsMod != null) statsMod.currentConnections--;
                    }
                }
            }
        }
    }
}