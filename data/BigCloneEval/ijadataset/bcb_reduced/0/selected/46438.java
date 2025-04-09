package register_virtual_stack;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.plugin.PlugIn;
import ij.VirtualStack;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.io.FileSaver;
import ij.io.OpenDialog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import mpicbg.ij.FeatureTransform;
import mpicbg.ij.SIFT;
import mpicbg.ij.util.Util;
import mpicbg.imagefeatures.*;
import mpicbg.models.Model;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.Point;
import mpicbg.models.PointMatch;
import mpicbg.trakem2.transform.AffineModel2D;
import mpicbg.trakem2.transform.CoordinateTransform;
import mpicbg.trakem2.transform.CoordinateTransformList;
import mpicbg.trakem2.transform.MovingLeastSquaresTransform;
import mpicbg.trakem2.transform.RigidModel2D;
import mpicbg.trakem2.transform.SimilarityModel2D;
import mpicbg.trakem2.transform.TransformMesh;
import mpicbg.trakem2.transform.TransformMeshMapping;
import mpicbg.trakem2.transform.TranslationModel2D;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import javax.swing.JFileChooser;
import bunwarpj.Transformation;
import bunwarpj.bUnwarpJ_;
import bunwarpj.trakem2.transform.CubicBSplineTransform;

/** 
 * Fiji plugin to register sequences of images in a concurrent (multi-thread) way.
 * <p>
 * <b>Requires</b>: a directory with images, of any size and type (8, 16, 32-bit gray-scale or RGB color)
 * <p>
 * <b>Performs</b>: registration of a sequence of images, by 6 different registration models:
 * <ul>
 * 				<li> Translation (no deformation)</li>
 * 				<li> Rigid (translation + rotation)</li>
 * 				<li> Similarity (translation + rotation + isotropic scaling)</li>
 * 				<li> Affine (free affine transformation)</li>
 * 				<li> Elastic (consistent elastic deformations by B-splines)</li>
 * 				<li> Moving least squares (maximal warping)</li>
 * </ul>
 * <p>
 * <b>Outputs</b>: the list of new images, one for slice, into a target directory as .tif files.
 * <p>
 * For a detailed documentation, please visit the plugin website at:
 * <p>
 * <A target="_blank" href="http://pacific.mpi-cbg.de/wiki/Register_Virtual_Stack_Slices">http://pacific.mpi-cbg.de/wiki/Register_Virtual_Stack_Slices</A>
 * 
 * @version 09/23/2009
 * @author Ignacio Arganda-Carreras (ignacio.arganda@gmail.com), Stephan Saalfeld and Albert Cardona
 */
public class Register_Virtual_Stack_MT implements PlugIn {

    /** translation registration model id */
    public static final int TRANSLATION = 0;

    /** rigid-body registration model id */
    public static final int RIGID = 1;

    /** rigid-body + isotropic scaling registration model id */
    public static final int SIMILARITY = 2;

    /** affine registration model id */
    public static final int AFFINE = 3;

    /** elastic registration model id */
    public static final int ELASTIC = 4;

    /** maximal warping registration model id */
    public static final int MOVING_LEAST_SQUARES = 5;

    /** index of the features model check-box */
    public static int featuresModelIndex = Register_Virtual_Stack_MT.RIGID;

    /** index of the registration model check-box */
    public static int registrationModelIndex = Register_Virtual_Stack_MT.RIGID;

    /** working directory path */
    public static String currentDirectory = (OpenDialog.getLastDirectory() == null) ? OpenDialog.getDefaultDirectory() : OpenDialog.getLastDirectory();

    /** advance options flag */
    public static boolean advanced = false;

    /** shrinkage constraint flag */
    public static boolean non_shrinkage = false;

    /** save transformation flag */
    public static boolean save_transforms = false;

    /** scaling regularization parameter [0.0-1.0] */
    public static double tweakScale = 0.95;

    /** shear regularization parameter [0.0-1.0] */
    public static double tweakShear = 0.95;

    /** isotropy (aspect ratio) regularization parameter [0.0-1.0] */
    public static double tweakIso = 0.95;

    /** display relaxation graph flag */
    public static boolean displayRelaxGraph = false;

    /** array of x- coordinate image centers */
    private static double[] centerX = null;

    /** array of y- coordinate image centers */
    private static double[] centerY = null;

    /** post-processing flag */
    public static boolean postprocess = true;

    /** registration model string labels */
    public static final String[] registrationModelStrings = { "Translation          -- no deformation                      ", "Rigid                -- translate + rotate                  ", "Similarity           -- translate + rotate + isotropic scale", "Affine               -- free affine transform               ", "Elastic              -- bUnwarpJ splines                    ", "Moving least squares -- maximal warping                     " };

    /** feature model string labels */
    public static final String[] featuresModelStrings = new String[] { "Translation", "Rigid", "Similarity", "Affine" };

    /** relaxation threshold (if the difference between last two iterations is below this threshold, the relaxation stops */
    public static final float STOP_THRESHOLD = 0.01f;

    /** maximum number of iterations in the relaxation loop */
    public static final int MAX_ITER = 1000;

    /**
	 * Plug-in run method
	 * 
	 * @param arg plug-in arguments
	 */
    public void run(String arg) {
        GenericDialog gd = new GenericDialog("Register Virtual Stack");
        gd.addChoice("Feature extraction model: ", featuresModelStrings, featuresModelStrings[featuresModelIndex]);
        gd.addChoice("Registration model: ", registrationModelStrings, registrationModelStrings[registrationModelIndex]);
        gd.addCheckbox("Advanced setup", advanced);
        gd.addCheckbox("Shrinkage constrain", non_shrinkage);
        gd.addCheckbox("Save transforms", save_transforms);
        gd.showDialog();
        if (gd.wasCanceled()) return;
        featuresModelIndex = gd.getNextChoiceIndex();
        registrationModelIndex = gd.getNextChoiceIndex();
        advanced = gd.getNextBoolean();
        non_shrinkage = gd.getNextBoolean();
        save_transforms = gd.getNextBoolean();
        JFileChooser chooser = new JFileChooser();
        if (currentDirectory != null) chooser.setCurrentDirectory(new java.io.File(currentDirectory)); else chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Choose directory with Source images");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(gd) != JFileChooser.APPROVE_OPTION) return;
        String source_dir = chooser.getSelectedFile().toString();
        if (null == source_dir) return;
        source_dir = source_dir.replace('\\', '/');
        if (!source_dir.endsWith("/")) source_dir += "/";
        chooser.setDialogTitle("Choose directory to store Output images");
        if (chooser.showOpenDialog(gd) != JFileChooser.APPROVE_OPTION) return;
        String target_dir = chooser.getSelectedFile().toString();
        if (null == target_dir) return;
        target_dir = target_dir.replace('\\', '/');
        if (!target_dir.endsWith("/")) target_dir += "/";
        String save_dir = null;
        if (save_transforms) {
            chooser.setDialogTitle("Choose directory to store Transform files");
            if (chooser.showOpenDialog(gd) != JFileChooser.APPROVE_OPTION) return;
            save_dir = chooser.getSelectedFile().toString();
            if (null == save_dir) return;
            save_dir = save_dir.replace('\\', '/');
            if (!save_dir.endsWith("/")) save_dir += "/";
        }
        String referenceName = null;
        if (non_shrinkage == false) {
            chooser.setDialogTitle("Choose reference image");
            chooser.setCurrentDirectory(new java.io.File(source_dir));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setAcceptAllFileFilterUsed(true);
            if (chooser.showOpenDialog(gd) != JFileChooser.APPROVE_OPTION) return;
            referenceName = chooser.getSelectedFile().getName();
        }
        exec(source_dir, target_dir, save_dir, referenceName, featuresModelIndex, registrationModelIndex, advanced, non_shrinkage);
    }

    /** 
	 * Execution method. Execute registration after setting parameters. 
	 * 
	 * @param source_dir Directory to read all images from, where each image is a slice in a sequence. Their names must be bit-sortable, i.e. if numbered, they must be padded with zeros.
	 * @param target_dir Directory to store registered slices into.
	 * @param save_dir Directory to store transform files into.
	 * @param referenceName File name of the reference image.
	 * @param featuresModelIndex Index of the features extraction model (0=TRANSLATION, 1=RIGID, 2=SIMILARITY, 3=AFFINE)
	 * @param registrationModelIndex Index of the registration model (0=TRANSLATION, 1=RIGID, 2=SIMILARITY, 3=AFFINE, 4=ELASTIC, 5=MOVING_LEAST_SQUARES)
	 * @param advanced Triggers showing parameters setup dialogs
	 * @param non_shrink Triggers showing non-shrinking dialog (if advanced options are selected as well) and execution
	 */
    public static void exec(final String source_dir, final String target_dir, final String save_dir, final String referenceName, final int featuresModelIndex, final int registrationModelIndex, final boolean advanced, final boolean non_shrink) {
        Param p = new Param();
        Param.featuresModelIndex = featuresModelIndex;
        Param.registrationModelIndex = registrationModelIndex;
        if (advanced && !p.showDialog()) return;
        if (non_shrink && advanced && !showRegularizationDialog(p)) return;
        exec(source_dir, target_dir, save_dir, referenceName, p, non_shrink);
    }

    /**
	 * Execute registration when all parameters are set.
	 * 
	 * @param source_dir Directory to read all images from, where each image is a slice in a sequence. Their names must be bit-sortable, i.e. if numbered, they must be padded with zeros.
	 * @param target_dir Directory to store registered slices into.
	 * @param save_dir Directory to store transform files into.
	 * @param referenceName File name of the reference image (if necessary, for non-shrinkage mode, it can be null)
	 * @param p Registration parameters
	 * @param non_shrink non shrinking mode flag
	 */
    public static void exec(final String source_dir, final String target_dir, final String save_dir, final String referenceName, final Param p, final boolean non_shrink) {
        final String exts = ".tif.jpg.png.gif.tiff.jpeg.bmp.pgm";
        final String[] names = new File(source_dir).list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                int idot = name.lastIndexOf('.');
                if (-1 == idot) return false;
                return exts.contains(name.substring(idot).toLowerCase());
            }
        });
        Arrays.sort(names);
        if (non_shrink) {
            exec(source_dir, names, target_dir, save_dir, p);
            return;
        }
        int referenceIndex = -1;
        for (int i = 0; i < names.length; i++) if (names[i].equals(referenceName)) {
            referenceIndex = i;
            break;
        }
        if (referenceIndex == -1) {
            IJ.error("The reference image was not found in the source folder!");
            return;
        }
        exec(source_dir, names, referenceIndex, target_dir, save_dir, p);
    }

    /**
	 * Registration parameters class. It stores SIFT and bUnwarpJ registration parameters. 
	 *
	 */
    public static class Param {

        /** SIFT parameters */
        public final FloatArray2DSIFT.Param sift = new FloatArray2DSIFT.Param();

        /**
		 * Closest/next neighbor distance ratio
		 */
        public static float rod = 0.92f;

        /**
		 * Maximal allowed alignment error in pixels
		 */
        public static float maxEpsilon = 25.0f;

        /**
		 * Inlier/candidates ratio
		 */
        public static float minInlierRatio = 0.05f;

        /**
		 * Implemented transformation models for choice
	 	 *  0=TRANSLATION, 1=RIGID, 2=SIMILARITY, 3=AFFINE
		 */
        public static int featuresModelIndex = Register_Virtual_Stack_MT.RIGID;

        /**
		 * Implemented transformation models for choice
	 	*  0=TRANSLATION, 1=RIGID, 2=SIMILARITY, 3=AFFINE, 4=ELASTIC, 5=MOVING_LEAST_SQUARES
		 */
        public static int registrationModelIndex = Register_Virtual_Stack_MT.RIGID;

        /** bUnwarpJ parameters for consistent elastic registration */
        public bunwarpj.Param elastic_param = new bunwarpj.Param();

        /**
         * Shows parameter dialog when "advanced options" is checked
         * @return false when dialog is canceled or true when is not
         */
        public boolean showDialog() {
            GenericDialog gd = new GenericDialog("Feature extraction");
            gd.addMessage("Scale Invariant Interest Point Detector:");
            gd.addNumericField("initial_gaussian_blur :", sift.initialSigma, 2, 6, "px");
            gd.addNumericField("steps_per_scale_octave :", sift.steps, 0);
            gd.addNumericField("minimum_image_size :", sift.minOctaveSize, 0, 6, "px");
            gd.addNumericField("maximum_image_size :", sift.maxOctaveSize, 0, 6, "px");
            gd.addMessage("Feature Descriptor:");
            gd.addNumericField("feature_descriptor_size :", 8, 0);
            gd.addNumericField("feature_descriptor_orientation_bins :", sift.fdBins, 0);
            gd.addNumericField("closest/next_closest_ratio :", rod, 2);
            gd.addMessage("Geometric Consensus Filter:");
            gd.addNumericField("maximal_alignment_error :", maxEpsilon, 2, 6, "px");
            gd.addNumericField("inlier_ratio :", minInlierRatio, 2);
            gd.addChoice("Feature_extraction_model :", featuresModelStrings, featuresModelStrings[featuresModelIndex]);
            gd.addMessage("Registration:");
            gd.addChoice("Registration_model:", registrationModelStrings, registrationModelStrings[registrationModelIndex]);
            gd.showDialog();
            if (gd.wasCanceled()) return false;
            sift.initialSigma = (float) gd.getNextNumber();
            sift.steps = (int) gd.getNextNumber();
            sift.minOctaveSize = (int) gd.getNextNumber();
            sift.maxOctaveSize = (int) gd.getNextNumber();
            sift.fdSize = (int) gd.getNextNumber();
            sift.fdBins = (int) gd.getNextNumber();
            rod = (float) gd.getNextNumber();
            maxEpsilon = (float) gd.getNextNumber();
            minInlierRatio = (float) gd.getNextNumber();
            featuresModelIndex = gd.getNextChoiceIndex();
            registrationModelIndex = gd.getNextChoiceIndex();
            if (registrationModelIndex == Register_Virtual_Stack_MT.ELASTIC) {
                if (!this.elastic_param.showDialog()) return false;
            }
            return true;
        }
    }

    /**
	 * Execute registration with non-shrinking constrain 
	 * 
	 * @param source_dir Directory to read all images from, where each image is a slice in a sequence. Their names must be bit-sortable, i.e. if numbered, they must be padded with zeros.
	 * @param sorted_file_names Array of sorted source file names.
	 * @param target_dir Directory to store registered slices into.
	 * @param save_dir Directory to store transform files into.
	 * @param p registration parameters
	 */
    public static void exec(final String source_dir, final String[] sorted_file_names, final String target_dir, final String save_dir, final Param p) {
        if (source_dir.equals(target_dir)) {
            IJ.error("Source and target directories MUST be different\n or images would get overwritten.\nDid NOT register stack slices.");
            return;
        }
        if (Param.registrationModelIndex < TRANSLATION || Param.registrationModelIndex > MOVING_LEAST_SQUARES) {
            IJ.error("Don't know how to process registration type " + Param.registrationModelIndex);
            return;
        }
        final ExecutorService exe = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Model<?> featuresModel;
        switch(Param.featuresModelIndex) {
            case Register_Virtual_Stack_MT.TRANSLATION:
                featuresModel = new TranslationModel2D();
                break;
            case Register_Virtual_Stack_MT.RIGID:
                featuresModel = new RigidModel2D();
                break;
            case Register_Virtual_Stack_MT.SIMILARITY:
                featuresModel = new SimilarityModel2D();
                break;
            case Register_Virtual_Stack_MT.AFFINE:
                featuresModel = new AffineModel2D();
                break;
            default:
                IJ.error("ERROR: unknown featuresModelIndex = " + Param.featuresModelIndex);
                return;
        }
        final List<PointMatch>[] inliers = new ArrayList[sorted_file_names.length - 1];
        CoordinateTransform[] transform = new CoordinateTransform[sorted_file_names.length];
        centerX = new double[sorted_file_names.length];
        centerY = new double[sorted_file_names.length];
        transform[0] = new RigidModel2D();
        final ArrayList<Feature>[] fs = new ArrayList[sorted_file_names.length];
        final Future<ArrayList<Feature>> fu[] = new Future[sorted_file_names.length];
        try {
            for (int i = 0; i < sorted_file_names.length; i++) {
                IJ.showStatus("Extracting features from slices...");
                final ImagePlus imp = IJ.openImage(source_dir + sorted_file_names[i]);
                centerX[i] = imp.getWidth() / 2;
                centerY[i] = imp.getHeight() / 2;
                fu[i] = exe.submit(extractFeatures(p, imp.getProcessor()));
            }
            for (int i = 0; i < sorted_file_names.length; i++) {
                IJ.showStatus("Extracting features " + (i + 1) + "/" + sorted_file_names.length);
                IJ.showProgress((double) (i + 1) / sorted_file_names.length);
                fs[i] = fu[i].get();
            }
            final Future<ArrayList<PointMatch>>[] fpm = new Future[sorted_file_names.length - 1];
            for (int i = 1; i < sorted_file_names.length; i++) {
                IJ.showStatus("Matching features...");
                try {
                    fpm[i - 1] = exe.submit(matchFeatures(p, fs[i], fs[i - 1], featuresModel));
                } catch (NotEnoughDataPointsException e) {
                    IJ.log("No features model found for file " + i + ": " + sorted_file_names[i]);
                    if (Param.registrationModelIndex != Register_Virtual_Stack_MT.ELASTIC) {
                        IJ.error("No features model found for file " + i + ": " + sorted_file_names[i]);
                        return;
                    }
                }
            }
            for (int i = 1; i < sorted_file_names.length; i++) {
                IJ.showStatus("Matching features " + (i + 1) + "/" + sorted_file_names.length);
                IJ.showProgress((double) (i + 1) / sorted_file_names.length);
                inliers[i - 1] = fpm[i - 1].get();
                if (inliers[i - 1].size() < 2) IJ.log("Error: not model found for images " + sorted_file_names[i - 1] + " and " + sorted_file_names[i]);
            }
            for (int i = 1; i < sorted_file_names.length; i++) {
                IJ.showStatus("Registering slice " + (i + 1) + "/" + sorted_file_names.length);
                IJ.showProgress((double) (i + 1) / sorted_file_names.length);
                RigidModel2D initialModel = new RigidModel2D();
                inliers[i - 1] = applyTransformReverse(inliers[i - 1], transform[i - 1]);
                initialModel.fit(inliers[i - 1]);
                transform[i] = initialModel;
            }
            PointMatch.apply(inliers[inliers.length - 1], transform[transform.length - 1]);
            IJ.showStatus("Relaxing inliers...");
            if (!relax(inliers, transform, p)) {
                IJ.log("Error when relaxing inliers!");
                return;
            }
            if (postprocess) {
                postProcessTransforms(transform);
            }
            IJ.showStatus("Calculating final images...");
            if (createResults(source_dir, sorted_file_names, target_dir, save_dir, exe, transform) == false) {
                IJ.log("Error when creating target images");
                return;
            }
        } catch (Exception e) {
            IJ.error("ERROR: " + e);
            e.printStackTrace();
        } finally {
            IJ.showProgress(1);
            IJ.showStatus("Done!");
            exe.shutdownNow();
        }
    }

    /**
	 * Apply a transformation to the second point (P2) of a list of Point matches
	 * 
	 * @param list list of point matches
	 * @param t transformation to be applied
	 * 
	 * @return new list of point matches (after the transformation)
	 */
    public static List<PointMatch> applyTransformReverse(List<PointMatch> list, CoordinateTransform t) {
        List<PointMatch> new_list = (List<PointMatch>) PointMatch.flip(list);
        PointMatch.apply(new_list, t);
        new_list = (List<PointMatch>) PointMatch.flip(new_list);
        return new_list;
    }

    /**
	 * Relax inliers 
	 * 
	 * @param inliers array of list of inliers in the sequence (one per pair of slices) 
	 * @param transform array of relaxed transforms (output)
	 * @param p registration parameters
	 * @return true or false in case of proper result or error
	 */
    public static boolean relax(List<PointMatch>[] inliers, CoordinateTransform[] transform, Param p) {
        if (Param.registrationModelIndex == Register_Virtual_Stack_MT.ELASTIC) return true;
        final boolean display = displayRelaxGraph;
        int n_iterations = 0;
        float[] mean_distance = new float[MAX_ITER + 1];
        for (int iSlice = 0; iSlice < inliers.length; iSlice++) mean_distance[0] += PointMatch.meanDistance(inliers[iSlice]);
        mean_distance[0] /= inliers.length;
        int[] index = new int[inliers.length + 1];
        for (int i = 0; i < index.length; i++) index[i] = i;
        for (int n = 0; n < MAX_ITER; n++) {
            n_iterations++;
            randomize(index);
            for (int j = 0; j < index.length; j++) {
                final int iSlice = index[j];
                CoordinateTransform t = getCoordinateTransform(p);
                if (iSlice == 0) {
                    ArrayList<PointMatch> firstMatches = new ArrayList<PointMatch>();
                    PointMatch.flip(inliers[0], firstMatches);
                    try {
                        fitInliers(p, t, firstMatches);
                        regularize(t, 0);
                        inliers[0] = applyTransformReverse(inliers[0], t);
                        transform[0] = t;
                    } catch (Exception e) {
                        e.printStackTrace();
                        IJ.error("Error when relaxing first matches...");
                        return false;
                    }
                } else {
                    List<PointMatch> combined_inliers = new ArrayList<PointMatch>(inliers[iSlice - 1]);
                    if (iSlice - 1 < inliers.length - 1) {
                        ArrayList<PointMatch> flippedMatches = new ArrayList<PointMatch>();
                        PointMatch.flip(inliers[iSlice], flippedMatches);
                        for (final PointMatch match : flippedMatches) combined_inliers.add(match);
                    }
                    t = getCoordinateTransform(p);
                    try {
                        fitInliers(p, t, combined_inliers);
                        regularize(t, iSlice);
                        PointMatch.apply(inliers[iSlice - 1], t);
                        if (iSlice - 1 < inliers.length - 1) inliers[iSlice] = applyTransformReverse(inliers[iSlice], t);
                        transform[iSlice] = t;
                    } catch (Exception e) {
                        e.printStackTrace();
                        IJ.error("Error when relaxing...");
                        return false;
                    }
                }
            }
            mean_distance[n + 1] = 0;
            for (int k = 0; k < inliers.length; k++) mean_distance[n + 1] += PointMatch.meanDistance(inliers[k]);
            mean_distance[n + 1] /= inliers.length;
            if (Math.abs(mean_distance[n + 1] - mean_distance[n]) < STOP_THRESHOLD) break;
        }
        if (display) {
            float[] x_label = new float[n_iterations + 1];
            for (int i = 0; i < x_label.length; i++) x_label[i] = (float) i;
            float[] distance = new float[n_iterations + 1];
            for (int i = 0; i < distance.length; i++) distance[i] = mean_distance[i];
            Plot pl = new Plot("Mean distance", "iterations", "MSE", x_label, distance);
            pl.setColor(Color.MAGENTA);
            pl.show();
        }
        return true;
    }

    /**
	 * Randomize array of integers
	 * 
	 * @param array array of integers to randomize
	 */
    public static void randomize(final int[] array) {
        Random generator = new Random();
        final int n = array.length;
        for (int i = 0; i < n; i++) {
            final int randomIndex1 = generator.nextInt(n);
            final int randomIndex2 = generator.nextInt(n);
            final int aux = array[randomIndex1];
            array[randomIndex1] = array[randomIndex2];
            array[randomIndex2] = aux;
        }
    }

    /** 
	 * Create final target images  
	 * 
	 * @param source_dir Directory to read all images from, where each image is a slice in a sequence. Their names must be bit-sortable, i.e. if numbered, they must be padded with zeros.
	 * @param sorted_file_names Array of sorted source file names.
	 * @param target_dir Directory to store registered slices into.
	 * @param save_dir Directory to store transform files into (null if transformations are not saved).
	 * @param exe executor service to save the images.
	 * @param transform array of transforms for every source image (including the first one).
	 * @return true or false in case of proper result or error
	 */
    public static boolean createResults(final String source_dir, final String[] sorted_file_names, final String target_dir, final String save_dir, final ExecutorService exe, final CoordinateTransform[] transform) {
        ImagePlus imp2 = IJ.openImage(source_dir + sorted_file_names[0]);
        final Rectangle commonBounds = new Rectangle(0, 0, imp2.getWidth(), imp2.getHeight());
        final List<Rectangle> bounds = new ArrayList<Rectangle>();
        for (int i = 0; i < sorted_file_names.length; i++) {
            imp2 = IJ.openImage(source_dir + sorted_file_names[i]);
            TransformMesh mesh = new TransformMesh(transform[i], 32, imp2.getWidth(), imp2.getHeight());
            TransformMeshMapping mapping = new TransformMeshMapping(mesh);
            imp2.getProcessor().setValue(0);
            imp2.setProcessor(imp2.getTitle(), mapping.createMappedImageInterpolated(imp2.getProcessor()));
            final Rectangle currentBounds = mesh.getBoundingBox();
            bounds.add(currentBounds);
            int min_x = commonBounds.x;
            int min_y = commonBounds.y;
            int max_x = commonBounds.x + commonBounds.width;
            int max_y = commonBounds.y + commonBounds.height;
            if (currentBounds.x < commonBounds.x) min_x = currentBounds.x;
            if (currentBounds.y < commonBounds.y) min_y = currentBounds.y;
            if (currentBounds.x + currentBounds.width > max_x) max_x = currentBounds.x + currentBounds.width;
            if (currentBounds.y + currentBounds.height > max_y) max_y = currentBounds.y + currentBounds.height;
            commonBounds.x = min_x;
            commonBounds.y = min_y;
            commonBounds.width = max_x - min_x;
            commonBounds.height = max_y - min_y;
            exe.submit(saveImage(imp2, makeTargetPath(target_dir, sorted_file_names[i])));
        }
        for (int i = 0; i < bounds.size(); ++i) {
            final Rectangle b = bounds.get(i);
            b.x -= commonBounds.x;
            b.y -= commonBounds.y;
        }
        final Future[] jobs = new Future[sorted_file_names.length];
        for (int j = 0, i = 0; i < sorted_file_names.length; i++, j++) {
            final Rectangle b = bounds.get(j);
            jobs[i] = exe.submit(resizeAndSaveImage(makeTargetPath(target_dir, sorted_file_names[i]), b.x, b.y, commonBounds.width, commonBounds.height));
        }
        final VirtualStack stack = new VirtualStack(commonBounds.width, commonBounds.height, null, target_dir);
        for (final Future<String> job : jobs) {
            String filename = null;
            try {
                filename = job.get();
            } catch (InterruptedException e) {
                IJ.error("Interruption exception!");
                e.printStackTrace();
                return false;
            } catch (ExecutionException e) {
                IJ.error("Execution exception!");
                e.printStackTrace();
                return false;
            }
            if (null == filename) {
                IJ.log("Image failed: " + filename);
                return false;
            }
            stack.addSlice(filename);
        }
        new ImagePlus("Registered " + new File(source_dir).getName(), stack).show();
        if (save_dir != null) {
            saveTransforms(transform, save_dir, sorted_file_names, exe);
        }
        IJ.showStatus("Done!");
        return true;
    }

    /**
	 * Save transforms into XML files.
	 * @param transform array of transforms.
	 * @param save_dir directory to save transforms into.
	 * @param sorted_file_names array of sorted file image names.
	 * @param exe executor service to run everything concurrently.
	 * @return true if every file is save correctly, false otherwise.
	 */
    private static boolean saveTransforms(CoordinateTransform[] transform, String save_dir, String[] sorted_file_names, ExecutorService exe) {
        final Future[] jobs = new Future[transform.length];
        for (int i = 0; i < transform.length; i++) {
            jobs[i] = exe.submit(saveTransform(makeTransformPath(save_dir, sorted_file_names[i]), transform[i]));
        }
        for (final Future<String> job : jobs) {
            String filename = null;
            try {
                filename = job.get();
            } catch (InterruptedException e) {
                IJ.error("Interruption exception!");
                e.printStackTrace();
                return false;
            } catch (ExecutionException e) {
                IJ.error("Execution exception!");
                e.printStackTrace();
                return false;
            }
            if (null == filename) {
                IJ.log("Not able to save file: " + filename);
                return false;
            }
        }
        return true;
    }

    /**
	 * Execution method. Execute registration when all parameters are set.
	 * 
	 * @param source_dir Directory to read all images from, where each image is a slice in a sequence. Their names must be bit-sortable, i.e. if numbered, they must be padded with zeros.
	 * @param sorted_file_names Array of sorted source file names.
	 * @param referenceIndex index of the reference image in the array of sorted source images.
	 * @param target_dir Directory to store registered slices into.
	 * @param save_dir Directory to store transform files into.
	 * @param p registration parameters.
	 */
    public static void exec(final String source_dir, final String[] sorted_file_names, final int referenceIndex, final String target_dir, final String save_dir, final Param p) {
        if (source_dir.equals(target_dir)) {
            IJ.error("Source and target directories MUST be different\n or images would get overwritten.\nDid NOT register stack slices.");
            return;
        }
        if (Param.registrationModelIndex < TRANSLATION || Param.registrationModelIndex > MOVING_LEAST_SQUARES) {
            IJ.error("Don't know how to process registration type " + Param.registrationModelIndex);
            return;
        }
        final ExecutorService exe = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            ImagePlus imp1 = null;
            ImagePlus imp2 = IJ.openImage(source_dir + sorted_file_names[referenceIndex]);
            ImagePlus imp1mask = new ImagePlus();
            ImagePlus imp2mask = new ImagePlus();
            final Rectangle commonBounds = new Rectangle(0, 0, imp2.getWidth(), imp2.getHeight());
            final List<Rectangle> boundsFor = new ArrayList<Rectangle>();
            boundsFor.add(new Rectangle(0, 0, imp2.getWidth(), imp2.getHeight()));
            exe.submit(saveImage(imp2, makeTargetPath(target_dir, sorted_file_names[referenceIndex])));
            CoordinateTransform[] transform = new CoordinateTransform[sorted_file_names.length];
            for (int i = referenceIndex + 1; i < sorted_file_names.length; i++) {
                imp1 = imp2;
                imp1mask = imp2mask;
                imp2mask = new ImagePlus();
                imp2 = IJ.openImage(source_dir + sorted_file_names[i]);
                final CoordinateTransform t = getCoordinateTransform(p);
                if (!register(imp1, imp2, imp1mask, imp2mask, i, sorted_file_names, source_dir, target_dir, exe, p, t, commonBounds, boundsFor, referenceIndex)) return;
                transform[i] = t;
            }
            transform[referenceIndex] = new AffineModel2D();
            imp2 = IJ.openImage(source_dir + sorted_file_names[referenceIndex]);
            final List<Rectangle> boundsBack = new ArrayList<Rectangle>();
            boundsBack.add(new Rectangle(0, 0, imp2.getWidth(), imp2.getHeight()));
            for (int i = referenceIndex - 1; i >= 0; i--) {
                imp1 = imp2;
                imp1mask = imp2mask;
                imp2mask = new ImagePlus();
                imp2 = IJ.openImage(source_dir + sorted_file_names[i]);
                final CoordinateTransform t = getCoordinateTransform(p);
                if (!register(imp1, imp2, imp1mask, imp2mask, i, sorted_file_names, source_dir, target_dir, exe, p, t, commonBounds, boundsBack, referenceIndex)) return;
                transform[i] = t;
            }
            for (int i = 1, j = referenceIndex + 1; i < boundsFor.size(); i++, j++) {
                final Rectangle b = boundsFor.get(i - 1);
                final CoordinateTransformList<CoordinateTransform> ctl = new CoordinateTransformList<CoordinateTransform>();
                ctl.add(transform[j]);
                final TranslationModel2D tr = new TranslationModel2D();
                tr.set(b.x, b.y);
                ctl.add(tr);
                transform[j] = ctl;
            }
            for (int i = 1, j = referenceIndex - 1; i < boundsBack.size(); i++, j--) {
                final Rectangle b = boundsBack.get(i - 1);
                final CoordinateTransformList<CoordinateTransform> ctl = new CoordinateTransformList<CoordinateTransform>();
                ctl.add(transform[j]);
                final TranslationModel2D tr = new TranslationModel2D();
                tr.set(b.x, b.y);
                ctl.add(tr);
                transform[j] = ctl;
            }
            for (int i = 0; i < boundsFor.size(); ++i) {
                final Rectangle b = boundsFor.get(i);
                b.x -= commonBounds.x;
                b.y -= commonBounds.y;
            }
            for (int i = 0; i < boundsBack.size(); ++i) {
                final Rectangle b = boundsBack.get(i);
                b.x -= commonBounds.x;
                b.y -= commonBounds.y;
            }
            final Future[] jobs = new Future[sorted_file_names.length];
            for (int j = 0, i = referenceIndex; i < sorted_file_names.length; i++, j++) {
                final Rectangle b = boundsFor.get(j);
                jobs[i] = exe.submit(resizeAndSaveImage(makeTargetPath(target_dir, sorted_file_names[i]), b.x, b.y, commonBounds.width, commonBounds.height));
            }
            for (int j = 1, i = referenceIndex - 1; i >= 0; i--, j++) {
                final Rectangle b = boundsBack.get(j);
                jobs[i] = exe.submit(resizeAndSaveImage(makeTargetPath(target_dir, sorted_file_names[i]), b.x, b.y, commonBounds.width, commonBounds.height));
            }
            final VirtualStack stack = new VirtualStack(commonBounds.width, commonBounds.height, null, target_dir);
            for (final Future<String> job : jobs) {
                String filename = job.get();
                if (null == filename) {
                    IJ.log("Image failed: " + filename);
                    return;
                }
                stack.addSlice(filename);
            }
            if (save_dir != null) {
                saveTransforms(transform, save_dir, sorted_file_names, exe);
            }
            new ImagePlus("Registered " + new File(source_dir).getName(), stack).show();
            IJ.showStatus("Done!");
        } catch (Exception e) {
            IJ.error("ERROR: " + e);
            e.printStackTrace();
        } finally {
            IJ.showProgress(1);
            exe.shutdownNow();
        }
    }

    /**
	 * Resize an image to a new size and save it 
	 * 
	 * @param path saving path
	 * @param x x- image origin (offset)
	 * @param y y- image origin (offset)
	 * @param width final image width
	 * @param height final image height
	 * @return file name of the saved image, or null if there was an error
	 */
    private static Callable<String> resizeAndSaveImage(final String path, final int x, final int y, final int width, final int height) {
        return new Callable<String>() {

            public String call() {
                try {
                    final ImagePlus imp = IJ.openImage(path);
                    if (null == imp) {
                        IJ.log("Could not open target image at " + path);
                        return null;
                    }
                    final ImageProcessor ip = imp.getProcessor().createProcessor(width, height);
                    if (imp.getType() == ImagePlus.COLOR_RGB) {
                        ip.setRoi(0, 0, width, height);
                        ip.setValue(0);
                        ip.fill();
                    }
                    ip.insert(imp.getProcessor(), x, y);
                    imp.flush();
                    final ImagePlus big = new ImagePlus(imp.getTitle(), ip);
                    big.setCalibration(imp.getCalibration());
                    if (!new FileSaver(big).saveAsTiff(path)) {
                        return null;
                    }
                    return new File(path).getName();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    /**
	 * Save transform into a file
	 * 
	 * @param path saving path and file name
	 * @param t coordinate transform to save
	 * @return file name of the saved file, or null if there was an error
	 */
    private static Callable<String> saveTransform(final String path, final CoordinateTransform t) {
        return new Callable<String>() {

            public String call() {
                try {
                    final FileWriter fw = new FileWriter(path);
                    fw.write(t.toXML(""));
                    fw.close();
                    return new File(path).getName();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    /**
	 * Make target (output) path by adding the output directory and the file name.
	 * File names are forced to have ".tif" extension.
	 * 
	 * @param dir output directory.
	 * @param name output file name.
	 * @return complete path for the target image.
	 */
    private static String makeTargetPath(final String dir, final String name) {
        String filepath = dir + name;
        if (!name.toLowerCase().matches("^.*ti[f]{1,2}$")) filepath += ".tif";
        return filepath;
    }

    /**
	 * Make transform file path.
	 * File names are forced to have ".xml" extension.
	 * 
	 * @param dir output directory.
	 * @param name output file name.
	 * @return complete path for the transform file.
	 */
    private static String makeTransformPath(final String dir, final String name) {
        final int i = name.lastIndexOf(".");
        final String no_ext = name.substring(0, i + 1);
        return dir + no_ext + "xml";
    }

    /**
	 * Generate object to concurrently extract features
	 * 
	 * @param p feature extraction parameters
	 * @param ip input image
	 * @return callable object to execute feature extraction
	 */
    private static Callable<ArrayList<Feature>> extractFeatures(final Param p, final ImageProcessor ip) {
        return new Callable<ArrayList<Feature>>() {

            public ArrayList<Feature> call() {
                final ArrayList<Feature> fs = new ArrayList<Feature>();
                new SIFT(new FloatArray2DSIFT(p.sift)).extractFeatures(ip, fs);
                return fs;
            }
        };
    }

    /**
	 * Generate object to concurrently save an image
	 * 
	 * @param imp image to save
	 * @param path output path
	 * @return callable object to execute the saving
	 */
    private static Callable<Boolean> saveImage(final ImagePlus imp, final String path) {
        return new Callable<Boolean>() {

            public Boolean call() {
                try {
                    return new FileSaver(imp).saveAsTiff(path);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };
    }

    /**
	 * Match features into inliers in a concurrent way
	 * 
	 * @param p registration parameters
	 * @param fs2 collection of features to match
	 * @param fs1 collection of features to match
	 * @param featuresModel features model 
	 * @return list of matched features
	 * @throws Exception if not enough points
	 */
    private static Callable<ArrayList<PointMatch>> matchFeatures(final Param p, final Collection<Feature> fs2, final Collection<Feature> fs1, final Model<?> featuresModel) throws Exception {
        return new Callable<ArrayList<PointMatch>>() {

            public ArrayList<PointMatch> call() throws Exception {
                final List<PointMatch> candidates = new ArrayList<PointMatch>();
                FeatureTransform.matchFeatures(fs2, fs1, candidates, Param.rod);
                final ArrayList<PointMatch> inliers = new ArrayList<PointMatch>();
                featuresModel.filterRansac(candidates, inliers, 1000, Param.maxEpsilon, Param.minInlierRatio);
                return inliers;
            }
        };
    }

    /**
	 * Register two images with corresponding masks and 
	 * following the features and registration models.
	 * 
	 * @param imp1 target image
	 * @param imp2 source image
	 * @param imp1mask target mask
	 * @param imp2mask source mask
	 * @param i index in the loop of images (just to show information)
	 * @param sorted_file_names array of sorted source file names
	 * @param source_dir source directory
	 * @param target_dir target (output) directory
	 * @param exe executor service to save the images
	 * @param p registration parameters
	 * @param t coordinate transform
	 * @param commonBounds current common bounds of the registration space
	 * @param bounds list of bounds for the already registered images
	 * @param referenceIndex index of the reference image
	 * @return false if there is an error, true otherwise
	 * @throws Exception if something fails
	 */
    public static boolean register(ImagePlus imp1, ImagePlus imp2, ImagePlus imp1mask, ImagePlus imp2mask, final int i, final String[] sorted_file_names, final String source_dir, final String target_dir, final ExecutorService exe, final Param p, CoordinateTransform t, Rectangle commonBounds, List<Rectangle> bounds, final int referenceIndex) throws Exception {
        IJ.showStatus("Registering slice " + (i + 1) + "/" + sorted_file_names.length);
        Future<ArrayList<Feature>> fu1 = exe.submit(extractFeatures(p, imp1.getProcessor()));
        Future<ArrayList<Feature>> fu2 = exe.submit(extractFeatures(p, imp2.getProcessor()));
        ArrayList<Feature> fs1 = fu1.get();
        ArrayList<Feature> fs2 = fu2.get();
        final List<PointMatch> candidates = new ArrayList<PointMatch>();
        FeatureTransform.matchFeatures(fs2, fs1, candidates, Param.rod);
        final List<PointMatch> inliers = new ArrayList<PointMatch>();
        Model<?> featuresModel;
        switch(Param.featuresModelIndex) {
            case Register_Virtual_Stack_MT.TRANSLATION:
                featuresModel = new TranslationModel2D();
                break;
            case Register_Virtual_Stack_MT.RIGID:
                featuresModel = new RigidModel2D();
                break;
            case Register_Virtual_Stack_MT.SIMILARITY:
                featuresModel = new SimilarityModel2D();
                break;
            case Register_Virtual_Stack_MT.AFFINE:
                featuresModel = new AffineModel2D();
                break;
            default:
                IJ.error("ERROR: unknown featuresModelIndex = " + Param.featuresModelIndex);
                return false;
        }
        try {
            featuresModel.filterRansac(candidates, inliers, 1000, Param.maxEpsilon, Param.minInlierRatio);
        } catch (NotEnoughDataPointsException e) {
            IJ.log("No features model found for file " + i + ": " + sorted_file_names[i]);
            if (Param.registrationModelIndex != Register_Virtual_Stack_MT.ELASTIC) {
                IJ.error("No features model found for file " + i + ": " + sorted_file_names[i]);
                return false;
            }
        }
        switch(Param.registrationModelIndex) {
            case Register_Virtual_Stack_MT.TRANSLATION:
            case Register_Virtual_Stack_MT.SIMILARITY:
            case Register_Virtual_Stack_MT.RIGID:
            case Register_Virtual_Stack_MT.AFFINE:
                ((Model<?>) t).fit(inliers);
                break;
            case Register_Virtual_Stack_MT.ELASTIC:
                final List<Point> sourcePoints = new ArrayList<Point>();
                final List<Point> targetPoints = new ArrayList<Point>();
                if (inliers.size() != 0) {
                    PointMatch.sourcePoints(inliers, sourcePoints);
                    PointMatch.targetPoints(inliers, targetPoints);
                    imp2.setRoi(Util.pointsToPointRoi(sourcePoints));
                    imp1.setRoi(Util.pointsToPointRoi(targetPoints));
                }
                ImageProcessor mask1 = imp1mask.getProcessor() == null ? null : imp1mask.getProcessor();
                ImageProcessor mask2 = imp2mask.getProcessor() == null ? null : imp2mask.getProcessor();
                Transformation warp = bUnwarpJ_.computeTransformationBatch(imp2, imp1, mask2, mask1, p.elastic_param);
                ((CubicBSplineTransform) t).set(warp.getIntervals(), warp.getDirectDeformationCoefficientsX(), warp.getDirectDeformationCoefficientsY(), imp2.getWidth(), imp2.getHeight());
                break;
            case Register_Virtual_Stack_MT.MOVING_LEAST_SQUARES:
                ((MovingLeastSquaresTransform) t).setModel(AffineModel2D.class);
                ((MovingLeastSquaresTransform) t).setAlpha(1);
                ((MovingLeastSquaresTransform) t).setMatches(inliers);
                break;
        }
        TransformMesh mesh = new TransformMesh(t, 32, imp2.getWidth(), imp2.getHeight());
        TransformMeshMapping mapping = new TransformMeshMapping(mesh);
        if (Param.registrationModelIndex == Register_Virtual_Stack_MT.ELASTIC) {
            imp2mask.setProcessor(imp2mask.getTitle(), new ByteProcessor(imp2.getWidth(), imp2.getHeight()));
            imp2mask.getProcessor().setValue(255);
            imp2mask.getProcessor().fill();
            imp2mask.setProcessor(imp2mask.getTitle(), mapping.createMappedImageInterpolated(imp2mask.getProcessor()));
        }
        imp2.getProcessor().setValue(0);
        imp2.setProcessor(imp2.getTitle(), mapping.createMappedImageInterpolated(imp2.getProcessor()));
        final Rectangle currentBounds = mesh.getBoundingBox();
        final Rectangle previousBounds = bounds.get(bounds.size() - 1);
        currentBounds.x += previousBounds.x;
        currentBounds.y += previousBounds.y;
        bounds.add(currentBounds);
        int min_x = commonBounds.x;
        int min_y = commonBounds.y;
        int max_x = commonBounds.x + commonBounds.width;
        int max_y = commonBounds.y + commonBounds.height;
        if (currentBounds.x < commonBounds.x) min_x = currentBounds.x;
        if (currentBounds.y < commonBounds.y) min_y = currentBounds.y;
        if (currentBounds.x + currentBounds.width > max_x) max_x = currentBounds.x + currentBounds.width;
        if (currentBounds.y + currentBounds.height > max_y) max_y = currentBounds.y + currentBounds.height;
        commonBounds.x = min_x;
        commonBounds.y = min_y;
        commonBounds.width = max_x - min_x;
        commonBounds.height = max_y - min_y;
        exe.submit(saveImage(imp2, makeTargetPath(target_dir, sorted_file_names[i])));
        return true;
    }

    /**
	 * Get a new coordinate transform given a registration model
	 * 
	 * @param p registration parameters
	 * @return new coordinate transform
	 */
    public static CoordinateTransform getCoordinateTransform(Param p) {
        CoordinateTransform t;
        switch(Param.registrationModelIndex) {
            case Register_Virtual_Stack_MT.TRANSLATION:
                t = new TranslationModel2D();
                break;
            case Register_Virtual_Stack_MT.RIGID:
                t = new RigidModel2D();
                break;
            case Register_Virtual_Stack_MT.SIMILARITY:
                t = new SimilarityModel2D();
                break;
            case Register_Virtual_Stack_MT.AFFINE:
                t = new AffineModel2D();
                break;
            case Register_Virtual_Stack_MT.ELASTIC:
                t = new CubicBSplineTransform();
                break;
            case Register_Virtual_Stack_MT.MOVING_LEAST_SQUARES:
                t = new MovingLeastSquaresTransform();
                break;
            default:
                IJ.log("ERROR: unknown registrationModelIndex = " + Param.registrationModelIndex);
                return null;
        }
        return t;
    }

    /**
	 * Fit inliers given a registration model
	 * 
	 * @param p registration parameters
	 * @param t coordinate transform
	 * @param inliers point matches
	 * @throws Exception if something fails
	 */
    public static void fitInliers(Param p, CoordinateTransform t, List<PointMatch> inliers) throws Exception {
        switch(Param.registrationModelIndex) {
            case Register_Virtual_Stack_MT.TRANSLATION:
            case Register_Virtual_Stack_MT.SIMILARITY:
            case Register_Virtual_Stack_MT.RIGID:
            case Register_Virtual_Stack_MT.AFFINE:
                ((Model<?>) t).fit(inliers);
                break;
            case Register_Virtual_Stack_MT.MOVING_LEAST_SQUARES:
                ((MovingLeastSquaresTransform) t).setModel(AffineModel2D.class);
                ((MovingLeastSquaresTransform) t).setAlpha(1);
                ((MovingLeastSquaresTransform) t).setMatches(inliers);
                break;
        }
        return;
    }

    /**
	 * Regularize coordinate transform
	 * 
	 * @param t coordinate transform
	 * @param index slice index
	 */
    public static void regularize(CoordinateTransform t, int index) {
        if (t instanceof AffineModel2D || t instanceof SimilarityModel2D) {
            final AffineTransform a = (t instanceof AffineModel2D) ? ((AffineModel2D) t).createAffine() : ((SimilarityModel2D) t).createAffine();
            a.translate(centerX[index], centerY[index]);
            final double a11 = a.getScaleX();
            final double a21 = a.getShearY();
            final double scaleX = Math.sqrt(a11 * a11 + a21 * a21);
            final double rotang = Math.atan2(a21 / scaleX, a11 / scaleX);
            final double a12 = a.getShearX();
            final double a22 = a.getScaleY();
            final double shearX = Math.cos(-rotang) * a12 - Math.sin(-rotang) * a22;
            final double scaleY = Math.sin(-rotang) * a12 + Math.cos(-rotang) * a22;
            final double transX = Math.cos(-rotang) * a.getTranslateX() - Math.sin(-rotang) * a.getTranslateY();
            final double transY = Math.sin(-rotang) * a.getTranslateX() + Math.cos(-rotang) * a.getTranslateY();
            final double new_shearX = shearX * (1.0 - tweakShear);
            final double avgScale = (scaleX + scaleY) / 2;
            final double aspectRatio = scaleX / scaleY;
            final double regAvgScale = avgScale * (1.0 - tweakScale) + 1.0 * tweakScale;
            final double regAspectRatio = aspectRatio * (1.0 - tweakIso) + 1.0 * tweakIso;
            final double new_scaleY = (2.0 * regAvgScale) / (regAspectRatio + 1.0);
            final double new_scaleX = regAspectRatio * new_scaleY;
            final AffineTransform b = makeAffineMatrix(new_scaleX, new_scaleY, new_shearX, 0, rotang, transX, transY);
            b.translate(-centerX[index], -centerY[index]);
            if (t instanceof AffineModel2D) ((AffineModel2D) t).set(b); else ((SimilarityModel2D) t).set((float) b.getScaleX(), (float) b.getShearY(), (float) b.getTranslateX(), (float) b.getTranslateY());
        }
    }

    /**
	 * Makes an affine transformation matrix from the given scale, shear,
	 * rotation and translation values
     * if you want a uniquely retrievable matrix, give sheary=0
     * 
	 * @param scalex scaling in x
	 * @param scaley scaling in y
	 * @param shearx shearing in x
	 * @param sheary shearing in y
	 * @param rotang angle of rotation (in radians)
	 * @param transx translation in x
	 * @param transy translation in y
	 * @return affine transformation matrix
	 */
    public static AffineTransform makeAffineMatrix(final double scalex, final double scaley, final double shearx, final double sheary, final double rotang, final double transx, final double transy) {
        final double m00 = Math.cos(rotang) * scalex - Math.sin(rotang) * sheary;
        final double m01 = Math.cos(rotang) * shearx - Math.sin(rotang) * scaley;
        final double m02 = Math.cos(rotang) * transx - Math.sin(rotang) * transy;
        final double m10 = Math.sin(rotang) * scalex + Math.cos(rotang) * sheary;
        final double m11 = Math.sin(rotang) * shearx + Math.cos(rotang) * scaley;
        final double m12 = Math.sin(rotang) * transx + Math.cos(rotang) * transy;
        return new AffineTransform(m00, m10, m01, m11, m02, m12);
    }

    /**
     * Shows regularization dialog when "Shrinkage constrain" is checked.
     * 
     * @param p registration parameters
     * @return false when dialog is canceled or true when it is not
     */
    public static boolean showRegularizationDialog(Param p) {
        GenericDialog gd = new GenericDialog("Shrinkage regularization");
        if (Param.registrationModelIndex == Register_Virtual_Stack_MT.SIMILARITY) tweakIso = 1.0;
        gd.addNumericField("shear :", tweakShear, 2);
        gd.addNumericField("scale :", tweakScale, 2);
        gd.addNumericField("isotropy :", tweakIso, 2);
        TextField isotropyTextField = (TextField) gd.getNumericFields().lastElement();
        if (Param.registrationModelIndex == Register_Virtual_Stack_MT.SIMILARITY) isotropyTextField.setEnabled(false); else isotropyTextField.setEnabled(true);
        gd.addMessage("Values between 0 and 1 are expected");
        gd.addMessage("(the closest to 1, the closest to rigid)");
        gd.addCheckbox("Display_relaxation_graph", displayRelaxGraph);
        gd.showDialog();
        if (gd.wasCanceled()) return false;
        tweakShear = (double) gd.getNextNumber();
        tweakScale = (double) gd.getNextNumber();
        tweakIso = (double) gd.getNextNumber();
        displayRelaxGraph = gd.getNextBoolean();
        return true;
    }

    /**
	 * Correct transforms from global scaling or rotation
	 * 
	 * @param transform array of transforms
	 */
    public static void postProcessTransforms(CoordinateTransform[] transform) {
        double avgAngle = 0;
        double avgScale = 0;
        for (int i = 0; i < transform.length; i++) {
            final CoordinateTransform t = transform[i];
            if (t instanceof AffineModel2D || t instanceof SimilarityModel2D) {
                final AffineTransform a = (t instanceof AffineModel2D) ? ((AffineModel2D) t).createAffine() : ((SimilarityModel2D) t).createAffine();
                final double a11 = a.getScaleX();
                final double a21 = a.getShearY();
                final double scaleX = Math.sqrt(a11 * a11 + a21 * a21);
                final double rotang = Math.atan2(a21 / scaleX, a11 / scaleX);
                final double a12 = a.getShearX();
                final double a22 = a.getScaleY();
                final double scaleY = Math.sin(-rotang) * a12 + Math.cos(-rotang) * a22;
                avgScale += (scaleX + scaleY) / 2.0;
                avgAngle += rotang;
            }
        }
        avgScale /= transform.length;
        avgAngle /= transform.length;
        AffineTransform correctionMatrix = new AffineTransform(Math.cos(-avgAngle) / avgScale, Math.sin(-avgAngle) / avgScale, -Math.sin(-avgAngle) / avgScale, Math.cos(-avgAngle) / avgScale, 0, 0);
        for (int i = 0; i < transform.length; i++) {
            if (transform[i] instanceof AffineModel2D || transform[i] instanceof SimilarityModel2D) {
                final AffineTransform a = (transform[i] instanceof AffineModel2D) ? ((AffineModel2D) transform[i]).createAffine() : ((SimilarityModel2D) transform[i]).createAffine();
                final AffineTransform b = new AffineTransform(a);
                b.concatenate(correctionMatrix);
                if (transform[i] instanceof AffineModel2D) ((AffineModel2D) transform[i]).set(b); else ((SimilarityModel2D) transform[i]).set((float) b.getScaleX(), (float) b.getShearY(), (float) b.getTranslateX(), (float) b.getTranslateY());
            }
        }
    }
}
