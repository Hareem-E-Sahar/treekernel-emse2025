package com.sun.corba.se.spi.activation;

/**
* com/sun/corba/se/spi/activation/ServerAlreadyInstalledHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ../../../../src/share/classes/com/sun/corba/se/spi/activation/activation.idl
* Monday, March 9, 2009 1:55:36 AM GMT-08:00
*/
public final class ServerAlreadyInstalledHolder implements org.omg.CORBA.portable.Streamable {

    public com.sun.corba.se.spi.activation.ServerAlreadyInstalled value = null;

    public ServerAlreadyInstalledHolder() {
    }

    public ServerAlreadyInstalledHolder(com.sun.corba.se.spi.activation.ServerAlreadyInstalled initialValue) {
        value = initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i) {
        value = com.sun.corba.se.spi.activation.ServerAlreadyInstalledHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o) {
        com.sun.corba.se.spi.activation.ServerAlreadyInstalledHelper.write(o, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return com.sun.corba.se.spi.activation.ServerAlreadyInstalledHelper.type();
    }
}
