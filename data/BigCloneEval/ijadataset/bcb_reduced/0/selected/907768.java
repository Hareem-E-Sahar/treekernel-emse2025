package org.jnet.popup;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Properties;
import org.jnet.i18n.GT;

/** For popup jnet menu (right click), menuContents maps out menu in String[][]
 * structureContents maps menu tags (from menuCont) to script commands
 * getWordContents maps menu tags to display labels
 * so the way this is set up a menu item has to go to a script - may be hard to subvert that
 */
class PopupResourceBundle {

    PopupResourceBundle(String menuStructure, Properties menuText) {
        buildStructure(menuStructure);
        localize(menuStructure != null, menuText);
    }

    String getMenu(String title) {
        return "# Jnet.mnu " + title + "\n\n" + "# Part I -- Menu Structure\n" + "# ------------------------\n\n" + dumpStructure(menuContents) + "\n\n" + "# Part II -- Key Definitions\n" + "# --------------------------\n\n" + dumpStructure(structureContents) + "\n\n" + "# Part III -- Word Translations\n" + "# -----------------------------\n\n" + dumpWords();
    }

    String getStructure(String key) {
        return structure.getProperty(key);
    }

    void addStructure(String key, String value) {
        structure.setProperty(key, value);
    }

    String getWord(String key) {
        String str = words.getProperty(key);
        return (str == null ? key : str);
    }

    private Properties structure = new Properties();

    private Properties words = new Properties();

    private static String Box(String cmd) {
        return "if not(showBoundBox);if not(showUnitcell);boundbox on;" + cmd + ";boundbox off;else;" + cmd + ";endif;endif;";
    }

    private static String[][] menuContents = { { "@COLOR", "black white red orange yellow green cyan blue indigo violet" }, { "@AXESCOLOR", "gray salmon maroon olive slateblue gold orchid" }, { "popupMenu", "modelSetMenu - selectMenuText viewMenu renderMenu colorMenu -  " + "zoomMenu " }, { "selectMenuText", "hideNotSelectedCheckbox showSelectionsCheckbox - selectAll selectNone invertSelection" }, { "viewMenu", "front left right top bottom back" }, { "renderMenu", "perspectiveDepthCheckbox showBoundBoxCheckbox showAxesCheckbox stereoMenu - renderSchemeMenu - nodeMenu labelMenu edgeMenu" }, { "renderSchemeMenu", "renderBallAndStick renderWireframe" }, { "nodeMenu", "nodeNone - " + "node15 node20 node25 node50 node75 node100" }, { "edgeMenu", "edgeNone edgeWireframe - " + "edge20 edge50 edge100 edge150 edge200 edge250" }, { "stereoMenu", "stereoNone stereoRedCyan stereoRedBlue stereoRedGreen stereoCrossEyed stereoWallEyed" }, { "labelMenu", "labelNone - " + "labelName labelNumber - " + "labelPositionMenu" }, { "labelPositionMenu", "labelCentered labelUpperRight labelLowerRight labelUpperLeft labelLowerLeft" }, { "colorMenu", "[color_nodes]Menu [color_edges]Menu" + " - [color_labels]Menu - [color_axes]Menu [color_boundbox]Menu [color_background]Menu" }, { "[color_nodes]Menu", "@COLOR - opaque translucent" }, { "[color_edges]Menu", "@COLOR - opaque translucent" }, { "[color_labels]Menu", null }, { "[color_background]Menu", "@COLOR" }, { "[color_axes]Menu", "@AXESCOLOR" }, { "[color_boundbox]Menu", null }, { "zoomMenu", "zoom50 zoom100 zoom150 zoom200 zoom400 zoom800 - " + "zoomIn zoomOut" }, { "FRAMESanimateMenu", "animModeMenu - play pause resume stop - nextframe prevframe rewind - playrev restart - " + "FRAMESanimFpsMenu" }, { "FRAMESanimFpsMenu", "animfps5 animfps10 animfps20 animfps30 animfps50" }, { "SIGNEDwriteMenu", "writeFileTextVARIABLE writeState writeHistory - writeJpg writePng writePovray - writeVrml writeMaya" }, { "animModeMenu", "onceThrough palindrome loop" }, { "[set_axes]Menu", "off#axes dotted - byPixelMenu" }, { "[set_boundbox]Menu", null }, { "byPixelMenu", "1p 3p 5p 10p" }, { "aboutComputedMenu", "APPLETjnetUrl APPLETmouseManualUrl APPLETtranslationUrl" } };

    private static String[][] structureContents = { { "hideNotSelectedCheckbox", "set hideNotSelected true | set hideNotSelected false; hide(none)" }, { "perspectiveDepthCheckbox", "" }, { "showAxesCheckbox", "set showAxes true | set showAxes false;set axesMolecular" }, { "showBoundBoxCheckbox", "" }, { "showHydrogensCheckbox", "" }, { "showMeasurementsCheckbox", "" }, { "showSelectionsCheckbox", "" }, { "showUNITCELLCheckbox", "" }, { "selectAll", "SELECT all" }, { "selectNone", "SELECT none" }, { "invertSelection", "SELECT not selected" }, { "front", Box("moveto 2.0 front;delay 1") }, { "left", Box("moveto 1.0 front;moveto 2.0 left;delay 1") }, { "right", Box("moveto 1.0 front;moveto 2.0 right;delay 1") }, { "top", Box("moveto 1.0 front;moveto 2.0 top;delay 1") }, { "bottom", Box("moveto 1.0 front;moveto 2.0 bottom;delay 1") }, { "back", Box("moveto 1.0 front;moveto 2.0 back;delay 1") }, { "renderBallAndStick", "restrict not selected;select not selected;spacefill 20%;wireframe 0.03" }, { "renderWireframe", "restrict not selected;select not selected;wireframe on" }, { "PDBrenderCartoonsOnly", "restrict not selected;select not selected;cartoons on;color structure" }, { "PDBrenderTraceOnly", "restrict not selected;select not selected;trace on;color structure" }, { "nodeNone", "cpk off" }, { "node15", "cpk 15%" }, { "node20", "cpk 20%" }, { "node25", "cpk 25%" }, { "node50", "cpk 50%" }, { "node75", "cpk 75%" }, { "node100", "cpk on" }, { "edgeNone", "wireframe off" }, { "edgeWireframe", "wireframe on" }, { "edge20", "wireframe .02" }, { "edge50", "wireframe .05" }, { "edge100", "wireframe .1" }, { "edge150", "wireframe .1.5" }, { "edge200", "wireframe .2" }, { "edge250", "wireframe .25" }, { "stereoNone", "stereo off" }, { "stereoRedCyan", "stereo redcyan 3" }, { "stereoRedBlue", "stereo redblue 3" }, { "stereoRedGreen", "stereo redgreen 3" }, { "stereoCrossEyed", "stereo -5" }, { "stereoWallEyed", "stereo 5" }, { "labelNone", "label off" }, { "labelSymbol", "label %e" }, { "labelName", "label %a" }, { "labelNumber", "label %i" }, { "labelCentered", "set labeloffset 0 0" }, { "labelUpperRight", "set labeloffset 4 4" }, { "labelLowerRight", "set labeloffset 4 -4" }, { "labelUpperLeft", "set labeloffset -4 4" }, { "labelLowerLeft", "set labeloffset -4 -4" }, { "zoom50", "zoom 50" }, { "zoom100", "zoom 100" }, { "zoom150", "zoom 150" }, { "zoom200", "zoom 200" }, { "zoom400", "zoom 400" }, { "zoom800", "zoom 800" }, { "zoomIn", "move 0 0 0 40 0 0 0 0 1" }, { "zoomOut", "move 0 0 0 -40 0 0 0 0 1" }, { "onceThrough", "anim mode once#" }, { "palindrome", "anim mode palindrome#" }, { "loop", "anim mode loop#" }, { "play", "anim play#" }, { "pause", "anim pause#" }, { "resume", "anim resume#" }, { "stop", "anim off#" }, { "nextframe", "frame next#" }, { "prevframe", "frame prev#" }, { "playrev", "anim playrev#" }, { "rewind", "anim rewind#" }, { "restart", "anim on#" }, { "animfps5", "anim fps 5#" }, { "animfps10", "anim fps 10#" }, { "animfps20", "anim fps 20#" }, { "animfps30", "anim fps 30#" }, { "animfps50", "anim fps 50#" }, { "Console", "console" }, { "showFile", "console on;show file" }, { "showFileHeader", "console on;getProperty FileHeader" }, { "showHistory", "console on;show history" }, { "showIsosurface", "console on;show isosurface" }, { "showMeasure", "console on;show measure" }, { "showMo", "console on;show mo" }, { "showModel", "console on;show model" }, { "showOrient", "console on;show orientation" }, { "showSpacegroup", "console on;show spacegroup" }, { "showState", "console on;show state" }, { "writeFileTextVARIABLE", "write file \"?FILE?\"" }, { "writeState", "write state \"?FILEROOT?.spt\"" }, { "writeHistory", "write history \"?FILEROOT?.his\"" }, { "writeIsosurface", "write isosurface \"?FILEROOT?.jvxl\"" }, { "writeJpg", "write image \"?FILEROOT?.jpg\"" }, { "writePng", "write image \"?FILEROOT?.png\"" }, { "writePovray", "write POVRAY ?FILEROOT?.pov" }, { "writeVrml", "write VRML ?FILEROOT?.vrml" }, { "writeMaya", "write MAYA ?FILEROOT?.maya" }, { "SYMMETRYshowSymmetry", "console on;show symmetry" }, { "UNITCELLshow", "console on;show unitcell" }, { "extractMOL", "console on;getproperty extractModel \"visible\" " }, { "1p", "on" }, { "3p", "3" }, { "5p", "5" }, { "10p", "10" }, { "10a", "0.1" }, { "20a", "0.20" }, { "25a", "0.25" }, { "50a", "0.50" }, { "100a", "1.0" }, { "APPLETjnetUrl", "show url \"http://www.jnet.org\"" }, { "APPLETmouseManualUrl", "show url \"http://wiki.jnet.org/index.php/Mouse_Manual\"" }, { "APPLETtranslationUrl", "show url \"http://wiki.jnet.org/index.php/Internationalisation\"" } };

    private String[] getWordContents() {
        boolean wasTranslating = GT.getDoTranslate();
        if (!wasTranslating) GT.setDoTranslate(true);
        String[] words = new String[] { "modelSetMenu", GT._("No nodes loaded"), "configurationComputedMenu", GT._("Configurations"), "elementsComputedMenu", GT._("Element"), "FRAMESbyModelComputedMenu", GT._("Model/Frame"), "languageComputedMenu", GT._("Language"), "PDBaaResiduesComputedMenu", GT._("By Residue Name"), "PDBnucleicResiduesComputedMenu", GT._("By Residue Name"), "PDBcarboResiduesComputedMenu", GT._("By Residue Name"), "PDBheteroComputedMenu", GT._("By HETATM"), "surfMoComputedMenu", GT._("Molecular Orbitals"), "SYMMETRYComputedMenu", GT._("Symmetry"), "hiddenModelSetText", GT._("Model information"), "selectMenuText", GT._("Select ({0})"), "allModelsText", GT._("All {0} models"), "configurationMenuText", GT._("Configurations ({0})"), "modelSetCollectionText", GT._("Collection of {0} models"), "nodesText", GT._("nodes: {0}"), "edgesText", GT._("edges: {0}"), "groupsText", GT._("groups: {0}"), "chainsText", GT._("chains: {0}"), "polymersText", GT._("polymers: {0}"), "modelMenuText", GT._("model {0}"), "viewMenuText", GT._("View {0}"), "mainMenuText", GT._("Main Menu"), "biomoleculesMenuText", GT._("Biomolecules"), "biomoleculeText", GT._("biomolecule {0} ({1} nodes)"), "loadBiomoleculeText", GT._("load biomolecule {0} ({1} nodes)"), "selectAll", GT._("All"), "selectNone", GT._("None"), "hideNotSelectedCheckbox", GT._("Display Selected Only"), "invertSelection", GT._("Invert Selection"), "viewMenu", GT._("View"), "front", GT._("Front"), "left", GT._("Left"), "right", GT._("Right"), "top", GT._("Top"), "bottom", GT._("Bottom"), "back", GT._("Back"), "renderMenu", GT._("Style"), "renderSchemeMenu", GT._("Scheme"), "renderBallAndStick", GT._("Ball and Stick"), "renderWireframe", GT._("Wireframe"), "nodeMenu", GT._("Nodes"), "nodeNone", GT._("Off"), "node15", GT._("Small size", "15"), "node20", GT._("Small size + 5%", "20"), "node25", GT._("Small size + 10%", "25"), "node50", GT._("Medium size", "50"), "node75", GT._("Medium size + 25%", "75"), "node100", GT._("Full size", "100"), "edgeMenu", GT._("Edges"), "edgeNone", GT._("Off"), "edgeWireframe", GT._("On"), "edge20", GT._("{0}", "0.02"), "edge50", GT._("{0}", "0.05"), "edge100", GT._("{0}", "0.10"), "edge150", GT._("{0}", "0.15"), "edge200", GT._("{0}", "0.20"), "edge250", GT._("{0}", "0.25"), "stereoMenu", GT._("Stereographic"), "stereoNone", GT._("None"), "stereoRedCyan", GT._("Red+Cyan glasses"), "stereoRedBlue", GT._("Red+Blue glasses"), "stereoRedGreen", GT._("Red+Green glasses"), "stereoCrossEyed", GT._("Cross-eyed viewing"), "stereoWallEyed", GT._("Wall-eyed viewing"), "labelMenu", GT._("Labels"), "labelNone", GT._("None"), "labelName", GT._("With Node Name"), "labelNumber", GT._("With Node Number"), "labelPositionMenu", GT._("Position Label on Node"), "labelCentered", GT._("Centered"), "labelUpperRight", GT._("Upper Right"), "labelLowerRight", GT._("Lower Right"), "labelUpperLeft", GT._("Upper Left"), "labelLowerLeft", GT._("Lower Left"), "colorMenu", GT._("Color"), "[color_nodes]Menu", GT._("Nodes"), "none", GT._("Inherit"), "black", GT._("Black"), "white", GT._("White"), "cyan", GT._("Cyan"), "red", GT._("Red"), "orange", GT._("Orange"), "yellow", GT._("Yellow"), "green", GT._("Green"), "blue", GT._("Blue"), "indigo", GT._("Indigo"), "violet", GT._("Violet"), "salmon", GT._("Salmon"), "olive", GT._("Olive"), "maroon", GT._("Maroon"), "gray", GT._("Gray"), "slateblue", GT._("Slate Blue"), "gold", GT._("Gold"), "orchid", GT._("Orchid"), "opaque", GT._("Make Opaque"), "translucent", GT._("Make Translucent"), "[color_edges]Menu", GT._("Edges"), "[color_labels]Menu", GT._("Labels"), "[color_background]Menu", GT._("Background"), "[color_vectors]Menu", GT._("Vectors"), "[color_axes]Menu", GT._("Axes"), "[color_boundbox]Menu", GT._("Boundbox"), "zoomMenu", GT._("Zoom"), "zoom50", "50%", "zoom100", "100%", "zoom150", "150%", "zoom200", "200%", "zoom400", "400%", "zoom800", "800%", "zoomIn", GT._("Zoom In"), "zoomOut", GT._("Zoom Out"), "FRAMESanimateMenu", GT._("Animation"), "animModeMenu", GT._("Animation Mode"), "onceThrough", GT._("Play Once"), "palindrome", GT._("Palindrome"), "loop", GT._("Loop"), "play", GT._("Play"), "pause", GT._("Pause"), "resume", GT._("Resume"), "stop", GT._("Stop"), "nextframe", GT._("Next Frame"), "prevframe", GT._("Previous Frame"), "rewind", GT._("Rewind"), "playrev", GT._("Reverse"), "restart", GT._("Restart"), "FRAMESanimFpsMenu", GT._("Set FPS"), "animfps5", "5", "animfps10", "10", "animfps20", "20", "animfps30", "30", "animfps50", "50", "SIGNEDwriteMenu", GT._("Save"), "writeFileTextVARIABLE", GT._("File {0}"), "writeState", GT._("Script with state"), "writeHistory", GT._("Script with history"), "writeJpg", GT._("{0} Image", "JPG"), "writePng", GT._("{0} Image", "PNG"), "[set_axes]Menu", GT._("Axes"), "[set_boundbox]Menu", GT._("Boundbox"), "[set_UNITCELL]Menu", GT._("Unit cell"), "off#axes", GT._("Hide"), "dotted", GT._("Dotted"), "byPixelMenu", GT._("Pixel Width"), "1p", GT._("{0} px", "1"), "3p", GT._("{0} px", "3"), "5p", GT._("{0} px", "5"), "10p", GT._("{0} px", "10"), "byAngstromMenu", GT._("Angstrom Width"), "10a", GT._("{0} Å", "0.10"), "20a", GT._("{0} Å", "0.20"), "25a", GT._("{0} Å", "0.25"), "50a", GT._("{0} Å", "0.50"), "100a", GT._("{0} Å", "1.0"), "showSelectionsCheckbox", GT._("Selection Halos"), "showHydrogensCheckbox", GT._("Show Hydrogens"), "showMeasurementsCheckbox", GT._("Show Measurements"), "perspectiveDepthCheckbox", GT._("Perspective Depth"), "showBoundBoxCheckbox", GT._("Boundbox"), "showAxesCheckbox", GT._("Axes"), "showUNITCELLCheckbox", GT._("Unit cell"), "colorrasmolCheckbox", GT._("RasMol Colors"), "aboutComputedMenu", GT._("About Jnet") };
        if (!wasTranslating) GT.setDoTranslate(wasTranslating);
        return words;
    }

    private void buildStructure(String menuStructure) {
        addItems(menuContents);
        addItems(structureContents);
        setStructure(menuStructure);
    }

    private String dumpWords() {
        String[] wordContents = getWordContents();
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < wordContents.length; i++) {
            String key = wordContents[i++];
            if (structure.getProperty(key) == null) s.append(key).append(" | ").append(wordContents[i]).append('\n');
        }
        return s.toString();
    }

    private String dumpStructure(String[][] items) {
        String previous = "";
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < items.length; i++) {
            String key = items[i][0];
            String label = words.getProperty(key);
            if (label != null) key += " | " + label;
            s.append(key).append(" = ").append(items[i][1] == null ? previous : (previous = items[i][1])).append('\n');
        }
        return s.toString();
    }

    public void setStructure(String slist) {
        if (slist == null) return;
        BufferedReader br = new BufferedReader(new StringReader(slist));
        String line;
        int pt;
        try {
            while ((line = br.readLine()) != null) {
                if (line.length() == 0 || line.charAt(0) == '#') continue;
                pt = line.indexOf("=");
                if (pt < 0) {
                    pt = line.length();
                    line += "=";
                }
                String name = line.substring(0, pt).trim();
                String value = line.substring(pt + 1).trim();
                String label = null;
                if ((pt = name.indexOf("|")) >= 0) {
                    label = name.substring(pt + 1).trim();
                    name = name.substring(0, pt).trim();
                }
                if (name.length() == 0) continue;
                if (value.length() > 0) structure.setProperty(name, value);
                if (label != null && label.length() > 0) words.setProperty(name, GT._(label));
            }
        } catch (Exception e) {
        }
        try {
            br.close();
        } catch (Exception e) {
        }
    }

    private void addItems(String[][] itemPairs) {
        String previous = "";
        for (int i = 0; i < itemPairs.length; i++) {
            String str = itemPairs[i][1];
            if (str == null) str = previous;
            previous = str;
            structure.setProperty(itemPairs[i][0], str);
        }
    }

    private void localize(boolean haveUserMenu, Properties menuText) {
        String[] wordContents = getWordContents();
        for (int i = 0; i < wordContents.length; ) if (haveUserMenu && words.getProperty(wordContents[i]) != null) {
            i += 2;
        } else {
            String item = wordContents[i++];
            String word = wordContents[i++];
            words.setProperty(item, word);
            if (menuText != null && item.indexOf("Text") >= 0) menuText.setProperty(item, word);
        }
    }
}
