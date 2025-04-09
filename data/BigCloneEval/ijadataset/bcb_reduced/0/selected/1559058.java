package com.fangr.servers.email.pop3;

import java.io.*;
import java.util.zip.CRC32;

/**
 * Represents a message in the POP3 mailbox.  
**/
class POP3Message {

    String data;

    long size;

    boolean deleted;

    File file;

    /**
	 * Constructor
	 * @param in BufferedReader containing message data
	 * @param f File pointer to the file so we can delete it.
	**/
    public POP3Message(BufferedReader in, File f) {
        this.file = f;
        try {
            data = in.readLine();
            while (in.ready()) {
                data = data + "\r\n" + in.readLine();
            }
            size = data.length();
            deleted = false;
            in.close();
        } catch (Exception e) {
            data = "";
            size = 0;
            deleted = false;
        }
    }

    /**
	 * @return The message data
	**/
    public String getData() {
        return data;
    }

    /**
	 * @return The message size
	 * FIXME: Should be octets, is currently bytes
	**/
    public long getSize() {
        return size;
    }

    /**
	 * @return whether message has been deleted or not
	**/
    public boolean isDeleted() {
        return deleted;
    }

    /**
	 * Delete the message
	 * @param deleted Whether to set the deletion to true or false
	**/
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
	 * Delete the file.
	**/
    public void delete() {
        file.delete();
    }

    /**
	 * @return The message id
	**/
    public String getID() {
        CRC32 checksum = new CRC32();
        int i = 0;
        checksum.reset();
        while ((i < data.length()) && (i < 5000)) {
            checksum.update((int) data.charAt(i));
            i++;
        }
        return (Long.toHexString(checksum.getValue()) + Integer.toHexString(data.hashCode()));
    }
}
