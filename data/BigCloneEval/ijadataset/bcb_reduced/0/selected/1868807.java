package org.iosgi.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import org.iosgi.IsolatedFramework;
import org.iosgi.IsolationAdmin;
import org.iosgi.util.io.Streams;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sven Schulz
 */
public class BundleOperation extends AbstractOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(BundleOperation.class);

    public enum Type {

        INSTALL, UNINSTALL
    }

    private final Type type;

    private final String location;

    public BundleOperation(final Type type, final String target, final String location) {
        super(target);
        this.type = type;
        this.location = location;
    }

    @Override
    public void perform(IsolationAdmin admin) throws Exception {
        switch(type) {
            case INSTALL:
                {
                    IsolatedFramework fw = admin.getIsolatedFramework(URI.create(this.getTarget()));
                    byte[] data = null;
                    if (location.startsWith("file:")) {
                        File f = new File(URI.create(location));
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        Streams.drain(new FileInputStream(f), baos).get();
                        data = baos.toByteArray();
                    }
                    fw.installBundle(location, data);
                    break;
                }
            case UNINSTALL:
                {
                    Bundle b = admin.getBundle(location);
                    if (b == null) {
                        LOGGER.warn("bundle with location {} not found", location);
                        return;
                    }
                    b.uninstall();
                    break;
                }
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(super.toString()).append('[').append(type).append(' ').append(location).append(']');
        return b.toString();
    }
}
