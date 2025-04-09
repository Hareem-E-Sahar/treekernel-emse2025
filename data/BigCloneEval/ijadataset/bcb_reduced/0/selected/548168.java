package com.pallas.unicore.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Thomas Kentemich
 * @version $Id: DeleteAll.java,v 1.1 2004/05/25 14:58:53 rmenday Exp $
 */
public class DeleteAll {

    /**
	 * Recursivley deletes all subdirectories and files in
	 *  
	 */
    public static final boolean deleteAll(File what) throws FileNotFoundException, IOException {
        if (what == null) {
            return false;
        }
        if (what.isDirectory()) {
            File[] files = what.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteAll(files[i]);
            }
        }
        return what.delete();
    }

    public DeleteAll() {
    }
}
