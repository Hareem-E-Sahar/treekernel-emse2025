package com.google.code.ptrends.locators.stubs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang.StringUtils;
import com.google.code.ptrends.locators.Locator;

public class TestLocator implements Locator {

    private byte[] buffer = null;

    private static final int BUFFER_SIZE = 256;

    private final String sourceLocation;

    public TestLocator(final String sourceLocation) {
        if (StringUtils.isBlank(sourceLocation)) {
            throw new IllegalArgumentException("Illegal blank source location");
        }
        this.sourceLocation = sourceLocation;
    }

    @Override
    public InputStream getStream() throws IOException {
        if (buffer == null) {
            buffer = initBuffer();
        }
        return new ByteArrayInputStream(buffer);
    }

    private byte[] initBuffer() throws IOException {
        FileInputStream input = null;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final byte[] bytes = new byte[BUFFER_SIZE];
        try {
            input = new FileInputStream(sourceLocation);
            while (true) {
                final int len = input.read(bytes, 0, BUFFER_SIZE);
                if (len == -1) {
                    break;
                }
                output.write(bytes, 0, len);
            }
        } finally {
            if (input != null) input.close();
            output.close();
        }
        return output.toByteArray();
    }

    @Override
    public void close() {
    }
}
