package org.sink.exceptions;

public class ConfigException extends SinkWrappedException {

    public ConfigException(Exception e, String msg) {
        super(e, msg);
    }

    public ConfigException(Exception e) {
        super(e, "Error", "The Sink configuration file could not be read or written. Make sure it exists and you have read and write access.");
    }
}
