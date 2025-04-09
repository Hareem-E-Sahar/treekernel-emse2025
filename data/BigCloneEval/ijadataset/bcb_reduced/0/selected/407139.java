package org.openscience.nmrshiftdb.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.renderer.Renderer2D;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.nmrshiftdb.om.DBAtom;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.SpectrumModel;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.SpectrumNavigation;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *  This class converts spectra/molecules to various other formats
 *
 * @author     shk3
 * @created    5. August 2002
 */
public class Export extends Thread {

    /**
   *  This are the image files used for making pdf
   */
    public String[] pictures = new String[2];

    public Color backColor = Color.white;

    private DBSpectrum spectrum;

    private IMolecule cdkmol;

    private IMolecule cdkmolwithh;

    private boolean useCSS = false;

    public int selected = -1;

    /**
   *Constructor for the Export object
   *
   * @param  spectrum       The spectrum to export
   * @exception  Exception  Database problems
   */
    public Export(DBSpectrum spectrum) throws Exception {
        super("export");
        this.spectrum = spectrum;
        cdkmol = spectrum.getDBMolecule().getAsCDKMoleculeAsEntered(1);
        cdkmolwithh = spectrum.getDBMolecule().getAsCDKMolecule(1);
    }

    /**
   *Constructor for the Export object
   *
   * @param  molecle        The molecule to export
   * @exception  Exception  Database problems
   */
    public Export(DBMolecule molecule) throws Exception {
        super("export");
        cdkmol = molecule.getAsCDKMoleculeAsEntered(1);
        cdkmolwithh = molecule.getAsCDKMolecule(1);
    }

    /**
   *Constructor for the Export object
   *
   * @param  molecle        The molecule to export
   * @param  spectrum       The spectrum to export
   * @exception  Exception  Database problems
   */
    public Export(DBMolecule molecule, DBSpectrum spectrum) throws Exception {
        super("export");
        this.spectrum = spectrum;
        cdkmol = molecule.getAsCDKMoleculeAsEntered(1);
        cdkmolwithh = spectrum.getDBMolecule().getAsCDKMolecule(1);
    }

    /**
   *  Gets the molecule/spectrum as a simple html file
   *
   * @return    The html value
   * @exception  Exception  Database problems
   */
    public String getHtml() throws Exception {
        VelocityContext context = new VelocityContext();
        context.put("spectrum", spectrum);
        context.put("atoms", spectrum.getOptions());
        context.put("moleculepicture", pictures[0]);
        context.put("spectrumpicture", pictures[1]);
        StringWriter w = new StringWriter();
        Velocity.mergeTemplate("exporthtml.vm", "ISO-8859-1", context, w);
        w.flush();
        return (w.toString());
    }

    /**
   *  Gets the spectrum and molecule as docbook
   *
   * @param  format         rtf=no nested tables, pdf=nested tables
   * @param  formatPic      Description of Parameter
   * @return                The docbook document
   * @exception  Exception  Description of Exception
   */
    public String getDocbook(String format, String formatPic) throws Exception {
        VelocityContext context = new VelocityContext();
        context.put("spectrum", spectrum);
        context.put("atoms", spectrum.getOptions());
        context.put("moleculepicture", pictures[0]);
        context.put("spectrumpicture", pictures[1]);
        context.put("format", format);
        context.put("formatpic", formatPic);
        StringWriter w = new StringWriter();
        Velocity.mergeTemplate("export.vm", "ISO-8859-1", context, w);
        w.flush();
        return (w.toString());
    }

    /**
   *  Gets the molecule as an svg, with and height set to a reasonable value for the molecule.
   *
   * @param      drawnumbers Should the svg contain atom numbers?
   * @return                The molecule as svg.
   * @exception  Exception  Description of Exception.
   */
    public String getMolSvg(boolean drawNumbers) throws Exception {
        return getMolSvg(-1, -1, drawNumbers);
    }

    /**
   *  Gets the molecule as an svg
   *
   * @param      width      The width of the svg
   * @param      height     The height of the svg
   * @param      drawnumbers Should the svg contain atom numbers?
   * @return                The molecule as svg
   * @exception  Exception  Description of Exception
   */
    public String getMolSvg(int width, int height, boolean drawNumbers) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Renderer2DModel r2dm = new Renderer2DModel();
        r2dm.setDrawNumbers(drawNumbers);
        r2dm.setBackColor(backColor);
        r2dm.setShowImplicitHydrogens(false);
        r2dm.setShowEndCarbons(false);
        if (selected > -1) {
            r2dm.setExternalHighlightColor(Color.RED);
            IAtomContainer ac = cdkmol.getAtom(selected).getBuilder().newAtomContainer();
            ac.addAtom(cdkmol.getAtom(selected));
            r2dm.setExternalSelectedPart(ac);
        }
        Renderer2D renderer = new Renderer2D(r2dm);
        int number = ((int) Math.sqrt(cdkmol.getAtomCount())) + 1;
        int moleculewidth = number * 100;
        int moleculeheight = number * 100;
        if (width > -1) {
            moleculewidth = width;
            moleculeheight = height;
        }
        if (moleculeheight < 200 || moleculewidth < 200) {
            r2dm.setIsCompact(true);
            r2dm.setBondDistance(3);
        }
        r2dm.setBackgroundDimension(new Dimension(moleculewidth, moleculeheight));
        GeometryTools.translateAllPositive(cdkmol, r2dm.getRenderingCoordinates());
        GeometryTools.scaleMolecule(cdkmol, new Dimension(moleculewidth, moleculeheight), 0.8, r2dm.getRenderingCoordinates());
        GeometryTools.center(cdkmol, new Dimension(moleculewidth, moleculeheight), r2dm.getRenderingCoordinates());
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        svgGenerator.setBackground(backColor);
        svgGenerator.setColor(backColor);
        svgGenerator.fill(new Rectangle(0, 0, moleculewidth, moleculeheight));
        renderer.paintMolecule(cdkmol, svgGenerator, false, true);
        boolean useCSS = false;
        baos = new ByteArrayOutputStream();
        Writer outwriter = new OutputStreamWriter(baos, "UTF-8");
        StringBuffer sb = new StringBuffer();
        svgGenerator.stream(outwriter, useCSS);
        StringTokenizer tokenizer = new StringTokenizer(baos.toString(), "\n");
        while (tokenizer.hasMoreTokens()) {
            String name = tokenizer.nextToken();
            if (name.length() > 4 && name.substring(0, 5).equals("<svg ")) {
                sb.append(name.substring(0, name.length() - 1)).append(" width=\"" + moleculewidth + "\" height=\"" + moleculeheight + "\">" + "\n\r");
            } else {
                sb.append(name + "\n\r");
            }
        }
        return (sb.toString());
    }

    /**
   *  Gets the spectrum as svg which was set by the constructor.
   *
   * @param      spectrumwidth Width of svg
   * @param      spectrumheight Height of svg   * @return                The spectrum as svg
   * @exception  Exception  Description of Exception
   */
    public String getSpecSvg(int spectrumwidth, int spectrumheight) throws Exception {
        return getSpecSvg(spectrum, spectrumwidth, spectrumheight, this.useCSS, null);
    }

    /**
   *  Gets the spectrum as svg which was set by the constructor.
   *
   * @param      spectrumwidth Width of svg
   * @param      spectrumheight Height of svg   * @return                The spectrum as svg
   * @exception  Exception  Description of Exception
   */
    public String getSpecSvg(int spectrumwidth, int spectrumheight, DBAtom[] atoms) throws Exception {
        return getSpecSvg(spectrum, spectrumwidth, spectrumheight, this.useCSS, atoms);
    }

    /**
   *  Gets a spectrum as svg, given by spectrum
   *
   * @param      spectrum   The spectrum to export.
   * @param      spectrumwidth Width of svg
   * @param      spectrumheight Height of svg
   * @param      useCSS
   * @return                The spectrum as svg
   * @exception  Exception  Description of Exception
   */
    public String getSpecSvg(DBSpectrum spectrum, int spectrumwidth, int spectrumheight, boolean useCSS, DBAtom[] atoms) throws Exception {
        return getSpecSvg(atoms == null ? spectrum.getSpectrumForAppletNewFormat() : spectrum.getSpectrumForAppletNewFormat(atoms, cdkmolwithh), spectrum.getDBSpectrumType().getNameAsString(), (spectrum.getMeasurementConditionWithName("Field Strength [MHz]", null) == null || spectrum.getMeasurementConditionWithName("Field Strength [MHz]", null).getValue().equals("unknown") || spectrum.getMeasurementConditionWithName("Field Strength [MHz]", null).getValue().equals("Unreported")) ? "" : spectrum.getMeasurementConditionWithName("Field Strength [MHz]", null).getValue(), spectrumwidth, spectrumheight, useCSS);
    }

    /**
   *  Gets a spectrum as svg, given by specfile
   *
   * @param      spectrum   The spectrum to export, a string of type shift,intensity|...
   * @param      spectype   The type of spectrum, string like 13C, 1H ...
   ~ @param      fieldstrength The fieldstrength
   * @param      spectrumwidth Width of svg
   * @param      spectrumheight Height of svg
   * @param      useCSS
   * @return                The spectrum as svg
   * @exception  Exception  Description of Exception
   */
    public static String getSpecSvg(String specfile, String spectype, String fieldstrength, int spectrumwidth, int spectrumheight, boolean useCSS) throws Exception {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        if (spectrumwidth == -1) spectrumwidth = 300;
        if (spectrumheight == -1) spectrumheight = 200;
        try {
            SpectrumModel model = new SpectrumModel();
            model.setShowNavigationView(false);
            SpectrumNavigation sn = new SpectrumNavigation(spectype, fieldstrength, "", specfile, "", "", model, null, true);
            Dimension spectrumdim = new Dimension(spectrumwidth, spectrumheight);
            sn.setSize(spectrumdim);
            sn.setPreferredSize(spectrumdim);
            sn.setMinimumSize(spectrumdim);
            sn.setMaximumSize(spectrumdim);
            sn.printAll(svgGenerator);
        } catch (Exception ex) {
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer outwriter = new OutputStreamWriter(baos, "UTF-8");
        StringBuffer sb = new StringBuffer();
        svgGenerator.stream(outwriter, useCSS);
        StringTokenizer tokenizer = new StringTokenizer(baos.toString(), "\n");
        while (tokenizer.hasMoreTokens()) {
            String name = tokenizer.nextToken();
            if (name.length() > 4 && name.substring(0, 5).equals("<svg ")) {
                sb.append(name.substring(0, name.length() - 1)).append(" width=\"" + spectrumwidth + "\" height=\"" + spectrumheight + "\">");
            } else {
                sb.append(name);
            }
        }
        return (GeneralUtils.replace(sb.toString(), "clip-path=\"url(#clipPath1)\" ", " "));
    }

    /**
   *  Gets an image of spectrum or molecule, width/height set to a reasonable value for molecules (Hint: don't use this for spectra).
   *
   * @param  molecule       true=image of molecule, falso=of spectrum
   * @param  format         jpeg, tiff or png
   * @param  path           Where to save the image
   * @param      drawnumbers Should the svg contain atom numbers?
   * @return                The path to the image
   * @exception  Exception  Description of Exception
   */
    public String getImage(boolean molecule, String format, String path, boolean drawNumbers) throws Exception {
        return getImage(molecule, format, path, -1, -1, drawNumbers, null);
    }

    /**
   *  Gets an image of spectrum or molecule
   *
   * @param  molecule       true=image of molecule, falso=of spectrum
   * @param  format         jpeg, tiff or png
   * @param  path           Where to save the image
   * @param      width      The width of the svg
   * @param      height     The height of the svg
   * @param      drawnumbers Should the svg contain atom numbers?
   * @return                The path to the image
   * @exception  Exception  Description of Exception
   */
    public String getImage(boolean molecule, String format, String path, int width, int height, boolean drawNumbers, DBAtom[] atoms) throws Exception {
        String inputstr;
        if (molecule) {
            inputstr = getMolSvg(width, height, drawNumbers);
        } else {
            inputstr = getSpecSvg(width, height, atoms);
        }
        ImageTranscoder it = null;
        String imagefile = path;
        if (format.equals("jpeg")) {
            it = new JPEGTranscoder();
            it.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));
            imagefile += ".jpg";
        }
        if (format.equals("tiff")) {
            it = new TIFFTranscoder();
            imagefile += ".tif";
        }
        if (format.equals("png")) {
            it = new PNGTranscoder();
            imagefile += ".png";
        }
        TranscoderInput input = new TranscoderInput(new StringReader(inputstr));
        OutputStream ostream = new FileOutputStream(imagefile);
        TranscoderOutput output = new TranscoderOutput(ostream);
        it.transcode(input, output);
        ostream.flush();
        ostream.close();
        return (imagefile);
    }

    /**
   *  Makes a zip file out of several files
   *
   * @param  filenames        The files to be zipped
   * @return                  The zip file
   * @exception  IOException  Problem reading files
   */
    public ByteArrayOutputStream makeZip(String[] filenames) throws IOException {
        byte[] buf = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(baos);
        for (int i = 0; i < filenames.length; i++) {
            FileInputStream in = new FileInputStream(filenames[i]);
            out.putNextEntry(new ZipEntry(new File(filenames[i]).getName()));
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
        out.flush();
        out.close();
        return (baos);
    }
}
