package com.aelitis.azureus.ui;

import java.lang.reflect.Constructor;

/**
 * This is the main of all mains! 
 * 
 * @author TuxPaper
 * @created May 17, 2007
 *
 */
public class Main {

    public static void main(String[] args) {
        try {
            final Class startupClass = Class.forName("org.gudy.azureus2.ui.swt.Main");
            final Constructor constructor = startupClass.getConstructor(new Class[] { String[].class });
            constructor.newInstance(new Object[] { args });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
