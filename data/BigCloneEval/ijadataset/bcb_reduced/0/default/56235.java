import javax.swing.*;
import java.util.prefs.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.lang.reflect.*;
import globals.*;
import circuit.*;
import export.*;
import timer.*;

/** FidoMain.java 

	The starting point of FidoCadJ.


<pre>
    This file is part of FidoCadJ.

    FidoCadJ is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FidoCadJ is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FidoCadJ.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2008-2012 by Davide Bucci
</pre>

    
    @author Davide Bucci
*/
public class FidoMain {

    /** The main method. Shows an instance of the FidoFrame */
    public static void main(String[] args) {
        String loadFile = "";
        String libDirectory = "";
        boolean commandLineOnly = false;
        boolean convertFile = false;
        int totx = 0, toty = 0;
        String exportFormat = "";
        String outputFile = "";
        boolean headlessMode = false;
        boolean resolutionBasedExport = false;
        boolean printSize = false;
        double resolution = 1;
        if (args.length >= 1) {
            int i;
            boolean loaded = false;
            boolean nextLib = false;
            for (i = 0; i < args.length; ++i) {
                if (args[i].startsWith("-")) {
                    if (args[i].startsWith("-n")) {
                        commandLineOnly = true;
                        System.setProperty("java.awt.headless", "true");
                    } else if (args[i].startsWith("-d")) {
                        nextLib = true;
                    } else if (args[i].startsWith("-c")) {
                        try {
                            if (args[++i].startsWith("r")) {
                                resolution = Double.parseDouble(args[i].substring(1));
                                resolutionBasedExport = true;
                                if (resolution <= 0) {
                                    System.err.println("Resolution should be" + "a positive real number");
                                    System.exit(1);
                                }
                            } else {
                                totx = Integer.parseInt(args[i]);
                                toty = Integer.parseInt(args[++i]);
                            }
                            exportFormat = args[++i];
                            outputFile = args[++i];
                            convertFile = true;
                            headlessMode = true;
                        } catch (Exception E) {
                            System.err.println("Unable to read the parameters" + "given to -c");
                            System.exit(1);
                        }
                        convertFile = true;
                    } else if (args[i].startsWith("-h")) {
                        showCommandLineHelp();
                        System.exit(0);
                    } else if (args[i].startsWith("-s")) {
                        headlessMode = true;
                        printSize = true;
                    } else {
                        System.err.println("Unrecognized option: " + args[i]);
                        showCommandLineHelp();
                        System.exit(1);
                    }
                } else {
                    if (nextLib) {
                        libDirectory = args[i];
                        System.out.println("Changed the library directory: " + args[i]);
                    } else {
                        if (loaded) {
                            System.err.println("Only one file can be" + "specified in the command line");
                        }
                        loadFile = args[i];
                        loaded = true;
                    }
                    nextLib = false;
                }
            }
        }
        if (headlessMode) {
            ParseSchem P = new ParseSchem();
            if (loadFile.equals("")) {
                System.err.println("You should specify a FidoCadJ file to read");
                System.exit(1);
            }
            readLibraries(P, false, libDirectory);
            StringBuffer txt = new StringBuffer();
            try {
                FileReader input = new FileReader(loadFile);
                BufferedReader bufRead = new BufferedReader(input);
                String line = "";
                txt = new StringBuffer(bufRead.readLine());
                txt.append("\n");
                while (line != null) {
                    line = bufRead.readLine();
                    txt.append(line);
                    txt.append("\n");
                }
                bufRead.close();
                Vector layerDesc = Globals.createStandardLayers();
                P.setLayers(layerDesc);
                P.parseString(new StringBuffer(txt.toString()));
            } catch (IllegalArgumentException iae) {
                System.err.println("Illegal filename");
            } catch (Exception e) {
                System.err.println("Unable to export: " + e);
            }
            if (convertFile) {
                try {
                    if (resolutionBasedExport) {
                        ExportGraphic.export(new File(outputFile), P, exportFormat, resolution, true, false, true, true);
                    } else {
                        ExportGraphic.exportSize(new File(outputFile), P, exportFormat, totx, toty, true, false, true, true);
                    }
                    System.out.println("Export completed");
                } catch (IOException ioe) {
                    System.err.println("Export error: " + ioe);
                }
            }
            if (printSize) {
                Dimension d = ExportGraphic.getImageSize(P, 1, true);
                System.out.println("" + d.width + " " + d.height);
            }
        }
        if (!commandLineOnly) {
            SwingUtilities.invokeLater(new CreateSwingInterface(libDirectory, loadFile));
        }
    }

    /** Print a short summary of each option available for launching
    	FidoCadJ.
    */
    private static void showCommandLineHelp() {
        String help = "\nThis is FidoCadJ, version " + Globals.version + ".\n" + "By Davide Bucci, 2007-2012.\n\n" + "Use: java -jar fidocadj.jar [-options] [file] \n" + "where options include:\n\n" + " -n     Do not start the graphical user interface (headless mode)\n\n" + " -d     Set the extern library directory\n" + "        Usage: -d dir\n" + "        where 'dir' is the path of the directory you want to use.\n\n" + " -c     Convert the given file to a graphical format.\n" + "        Usage: -d sx sy eps|pdf|svg|png|jpg|fcd|sch outfile\n" + "        If you use this command line option, you *must* specify a FidoCad file to convert.\n" + "        An alternative is to specify the resolution in pixels per logical unit by\n" + "        preceding it by the letter 'r' (without spaces), instead of giving sx and sy.\n\n" + " -s     Print the size  of the specified file in logical coordinates.\n\n" + " -h     Print this help and exit.\n\n" + " [file] The optional (except if you use the -d or -s options) FidoCad file to load at\n" + "        startup time.\n\n" + "Example: load and convert a FidoCad drawing to a 800x600 pixel png file without using the GUI.\n" + "java -jar fidocadj.jar -n -c 800 600 png out1.png test1.fcd\n\n" + "Example: load and convert a FidoCad drawing to a png file without using the GUI.\n" + "         Each FidoCadJ logical unit will be converted in 2 pixels on the image.\n" + "java -jar fidocadj.jar -n -c r2 png out2.png test2.fcd\n\n";
        System.out.println(help);
    }

    /** Read the libraries, eventually by inspecting the directory specified
		by the user. There are three standard directories: IHRAM.FCL, 
		FCDstdlib.fcl and PCB.fcl. If those files are found in the external 
		directory specified, the internal version is not loaded. Other files
		on the external directory are loaded.
		
		@param P the parsing class in which the libraries should be loaded
		@param englishLibraries a flag to specify if the internal libraries 
			should be loaded in English or in Italian.
		@param libDirectory the path of the external directory.

	*/
    public static void readLibraries(ParseSchem P, boolean englishLibraries, String libDirectory) {
        P.loadLibraryDirectory(libDirectory);
        if (!(new File(Globals.createCompleteFileName(libDirectory, "IHRAM.FCL"))).exists()) {
            if (englishLibraries) P.loadLibraryInJar(FidoFrame.class.getResource("lib/IHRAM_en.FCL"), "ihram"); else P.loadLibraryInJar(FidoFrame.class.getResource("lib/IHRAM.FCL"), "ihram");
        } else System.out.println("IHRAM library got from external file");
        if (!(new File(Globals.createCompleteFileName(libDirectory, "FCDstdlib.fcl"))).exists()) {
            if (englishLibraries) P.loadLibraryInJar(FidoFrame.class.getResource("lib/FCDstdlib_en.fcl"), ""); else P.loadLibraryInJar(FidoFrame.class.getResource("lib/FCDstdlib.fcl"), "");
        } else System.out.println("Standard library got from external file");
        if (!(new File(Globals.createCompleteFileName(libDirectory, "PCB.fcl"))).exists()) {
            if (englishLibraries) P.loadLibraryInJar(FidoFrame.class.getResource("lib/PCB_en.fcl"), "pcb"); else P.loadLibraryInJar(FidoFrame.class.getResource("lib/PCB.fcl"), "pcb");
        } else System.out.println("Standard PCB library got from external file");
        if (!(new File(Globals.createCompleteFileName(libDirectory, "elettrotecnica.fcl"))).exists()) {
            if (englishLibraries) P.loadLibraryInJar(FidoFrame.class.getResource("lib/elettrotecnica_en.fcl"), "elettrotecnica"); else P.loadLibraryInJar(FidoFrame.class.getResource("lib/elettrotecnica.fcl"), "elettrotecnica");
        } else System.out.println("Electrotechnics library got from external file");
    }
}

/** Creates the Swing elements needed for the interface.
*/
class CreateSwingInterface implements Runnable {

    String libDirectory;

    String loadFile;

    public CreateSwingInterface(String ld, String lf) {
        libDirectory = ld;
        loadFile = lf;
    }

    public CreateSwingInterface() {
        libDirectory = "";
        loadFile = "";
    }

    public void run() {
        if (System.getProperty("os.name").startsWith("Mac")) {
            Globals g = new Globals();
            Preferences prefs_static = Preferences.userNodeForPackage(g.getClass());
            Globals.quaquaActive = prefs_static.get("QUAQUA", "true").equals("true");
            Globals.weAreOnAMac = true;
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "FidoCadJ");
            try {
                if (Globals.quaquaActive) {
                    UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
                    System.out.println("Quaqua look and feel active");
                }
            } catch (Exception e) {
                System.out.println("The Quaqua look and feel is not available");
                System.out.println("I will continue with the basic Apple l&f");
            }
        } else if (System.getProperty("os.name").startsWith("Win")) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception E) {
            }
            Globals.quaquaActive = false;
        } else {
            Globals.quaquaActive = false;
        }
        if (Globals.weAreOnAMac) {
            try {
                Class a = Class.forName("AppleSpecific");
                Object b = a.newInstance();
                Method m = a.getMethod("answerFinder", null);
                m.invoke(b, null);
            } catch (Exception exc) {
                Globals.weAreOnAMac = false;
                System.out.println("It seems that this software has been " + "compiled on a system different from MacOSX. Some nice " + "integrations with MacOSX will therefore be absent. If " + "you have compiled on MacOSX, make sure you used the " + "'compile' or 'rebuild' script along with the 'mac' " + "option.");
            }
        }
        FidoFrame popFrame = new FidoFrame(true);
        if (!libDirectory.equals("")) {
            popFrame.libDirectory = libDirectory;
        }
        popFrame.init();
        popFrame.loadLibraries();
        if (!loadFile.equals("")) popFrame.load(loadFile);
        popFrame.setVisible(true);
    }
}
