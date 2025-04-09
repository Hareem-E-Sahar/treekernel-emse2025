package net.sourceforge.seqware.pipeline.modules.utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.crypto.Cipher;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.common.util.filetools.FileTools;
import net.sourceforge.seqware.common.util.filetools.ProvisionFilesUtil;
import net.sourceforge.seqware.pipeline.module.Module;
import net.sourceforge.seqware.pipeline.module.ModuleInterface;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 * Purpose:
 * 
 * This module takes one or more inputs (S3 URL, HTTP/HTTPS URL, or local file
 * path) and copies the file to the specified output (S3 bucket URL, HTTP/HTTPS
 * URL, or local directory path). For S3 this bundle supports large, multipart
 * file upload which is needed for files >2G.
 * 
 * FIXME: needs to return errors on failures/exceptions
 * 
 * @author boconnor
 * 
 */
@ServiceProvider(service = ModuleInterface.class)
public class ProvisionFiles extends Module {

    protected OptionSet options = null;

    protected final int READ_ATTEMPTS = 1000;

    protected long size = 0;

    protected long position = 0;

    protected String fileName = "";

    protected File inputFile = null;

    protected Key dataEncryptionKey = null;

    protected HashMap metaMap = new HashMap();

    protected String algorithmName = "ProvisionFiles";

    private ProvisionFilesUtil filesUtil = new ProvisionFilesUtil();

    private static final String DATA_ENCRYPTION_ALGORITHM = "DESede";

    protected OptionParser getOptionParser() {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(Arrays.asList("input-file", "i"), "Required: use this or --input-file-metadata, this is the input file, multiple should be specified seperately").withRequiredArg().describedAs("input file path");
        parser.acceptsAll(Arrays.asList("input-file-metadata", "im"), "Required: use this or --input-file, this is the input file, multiple should be specified seperately").withRequiredArg().describedAs("a '::' delimited list of type, meta_type, and file_path.");
        parser.acceptsAll(Arrays.asList("algorithm", "a"), "Optional: by default the algorithm is 'ProvisionFiles' but you can override here if you like.").withRequiredArg().describedAs("an algorithm name to save in the DB");
        parser.acceptsAll(Arrays.asList("encrypt-key", "e"), "Optional: if specified this key will be used to encrypt data before writing to its destination.").withRequiredArg().describedAs("cryptographic DESede key in Base64 encoded text");
        parser.acceptsAll(Arrays.asList("decrypt-key", "d"), "Optional: if specified this key will be used to decrypt data when reading from its source.").withRequiredArg().describedAs("cryptographic DESede key in Base64 encoded text");
        parser.acceptsAll(Arrays.asList("output-dir", "o"), "Required: output file location").withRequiredArg().describedAs("output directory path");
        parser.acceptsAll(Arrays.asList("recursive", "r"), "Optional: if the input-file points to a local directory then this option will cause the program to recursively copy the directory and its contents to the destination. An actual copy will be done for local to local copies rather than symlinks.");
        parser.acceptsAll(Arrays.asList("verbose", "v"), "Optional: verbose causes the S3 transfer status to display.");
        parser.accepts("force-copy", "Optional: if this is specified local to local file transfers are done with a copy rather than symlink. This is useful if you're writing to a temp area that will be deleted so you have to move the file essentially.");
        parser.accepts("skip-if-missing", "Optional: useful for workflows with variable output files, this will silently skip any missing inputs (this is a little dangerous).");
        return (parser);
    }

    public String get_syntax() {
        OptionParser parser = getOptionParser();
        StringWriter output = new StringWriter();
        try {
            parser.printHelpOn(output);
            return (output.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return (e.getMessage());
        }
    }

    /**
   * Things to check: * FIXME
   */
    @Override
    public ReturnValue do_test() {
        return new ReturnValue(ReturnValue.NOTIMPLEMENTED);
    }

    @Override
    public ReturnValue do_verify_parameters() {
        ReturnValue ret = new ReturnValue();
        ret.setExitStatus(ReturnValue.SUCCESS);
        try {
            OptionParser parser = getOptionParser();
            options = parser.parse(this.getParameters().toArray(new String[0]));
        } catch (OptionException e) {
            ret.setStderr(e.getMessage() + System.getProperty("line.separator") + this.get_syntax());
            ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
            e.printStackTrace();
            return ret;
        }
        if (!options.has("input-file") && !options.has("input-file-metadata")) {
            ret.setStderr("Must specify one or more --input-file or --input-file-metadata options" + System.getProperty("line.separator") + this.get_syntax());
            ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
            return ret;
        }
        for (String requiredOption : new String[] { "output-dir" }) {
            if (!options.has(requiredOption)) {
                ret.setStderr("Must specify a --" + requiredOption + " or -" + requiredOption.charAt(0) + " option" + System.getProperty("line.separator") + this.get_syntax());
                ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
                return ret;
            }
        }
        if (options.has("algorithm") && options.valueOf("algorith") != null && options.valueOf("algorithm").toString().length() > 0) {
            algorithmName = (String) options.valueOf("algorithm");
        }
        return (ret);
    }

    @Override
    public ReturnValue do_verify_input() {
        ReturnValue ret = new ReturnValue();
        ret.setExitStatus(ReturnValue.SUCCESS);
        List<String> inputs = (List<String>) options.valuesOf("input-file");
        ArrayList<String> newArray = new ArrayList<String>();
        List<String> metaInputs = (List<String>) options.valuesOf("input-file-metadata");
        if (metaInputs != null) {
            if (inputs != null && inputs.size() > 0) {
                newArray.addAll(inputs);
            }
            for (String input : metaInputs) {
                String[] tokens = input.split("::");
                if (tokens.length == 3) {
                    newArray.add(tokens[2]);
                }
            }
            inputs = newArray;
        }
        for (String input : inputs) {
            if (!input.startsWith("s3://") && !input.startsWith("http://") && !input.startsWith("https://") && !options.has("skip-if-missing") && FileTools.fileExistsAndReadable(new File(input)).getExitStatus() != ReturnValue.SUCCESS && FileTools.dirPathExistsAndReadable(new File(input)).getExitStatus() != ReturnValue.SUCCESS) {
                return new ReturnValue(null, "Cannot find input file: " + input, ReturnValue.FILENOTREADABLE);
            }
            if (new File(input).isDirectory() && !options.has("recursive")) {
                return new ReturnValue(null, "Cannot pass directories as input without specifying --recursive: " + input, ReturnValue.INVALIDARGUMENT);
            }
        }
        if (!((String) options.valueOf("output-dir")).startsWith("s3://") && !((String) options.valueOf("output-dir")).startsWith("http://") && !((String) options.valueOf("output-dir")).startsWith("https://")) {
            File output = new File((String) options.valueOf("output-dir"));
            if (!output.exists()) {
                output.mkdirs();
            }
            if (FileTools.dirPathExistsAndWritable(output).getExitStatus() != ReturnValue.SUCCESS) {
                ret.setExitStatus(ReturnValue.DIRECTORYNOTWRITABLE);
                ret.setStderr("Can't write to output directory " + options.valueOf("output-dir"));
                return (ret);
            }
        }
        return (ret);
    }

    @Override
    public ReturnValue do_run() {
        ReturnValue ret = new ReturnValue();
        ret.setExitStatus(ReturnValue.SUCCESS);
        boolean skipIfMissing = options.has("skip-if-missing");
        boolean verbose = options.has("verbose");
        filesUtil.setVerbose(verbose);
        ret.setAlgorithm(algorithmName);
        ArrayList<FileMetadata> fileArray = ret.getFiles();
        ArrayList<String> newArray = new ArrayList<String>();
        List<String> inputs = (List<String>) options.valuesOf("input-file");
        List<String> metaInputs = (List<String>) options.valuesOf("input-file-metadata");
        if (metaInputs != null) {
            if (inputs != null && inputs.size() > 0) {
                newArray.addAll(inputs);
            }
            for (String input : metaInputs) {
                String[] tokens = input.split("::");
                if (tokens.length == 3) {
                    newArray.add(tokens[2]);
                    FileMetadata fmd = new FileMetadata();
                    fmd.setDescription(tokens[0]);
                    fmd.setMetaType(tokens[1]);
                    fmd.setFilePath(tokens[2]);
                    fmd.setType(tokens[0]);
                    fileArray.add(fmd);
                }
            }
            inputs = newArray;
        }
        for (String input : inputs) {
            System.err.println("PROCESSING INPUT: " + input);
            this.size = 0;
            this.position = 0;
            this.fileName = "";
            if (!input.startsWith("http") && !input.startsWith("s3") && new File(input).isDirectory()) {
                if (options.has("recursive")) {
                    ReturnValue currRet = recursivelyCopyDir(new File(input).getAbsolutePath(), new File(input).list(), (String) options.valueOf("output-dir"), fileArray);
                    if (currRet.getExitStatus() != ReturnValue.SUCCESS) {
                        return (currRet);
                    }
                }
            } else if (!provisionFile(input, (String) options.valueOf("output-dir"), skipIfMissing, fileArray)) {
                ret.setExitStatus(ReturnValue.FAILURE);
                return (ret);
            }
        }
        for (FileMetadata fmd : fileArray) {
            System.err.println("FMD:\nDescription: " + fmd.getDescription() + "\nFile Path: " + fmd.getFilePath() + "\nMeta Type: " + fmd.getMetaType() + "\nType: " + fmd.getType());
        }
        return (ret);
    }

    private ReturnValue recursivelyCopyDir(String baseDir, String[] files, String outputDir, ArrayList<FileMetadata> fileArray) {
        ReturnValue ret = new ReturnValue(ReturnValue.SUCCESS);
        boolean skipIfMissing = options.has("skip-if-missing");
        for (String file : files) {
            if (baseDir.endsWith("/")) {
                baseDir = baseDir.substring(0, baseDir.length() - 1);
            }
            if (outputDir.endsWith("/")) {
                outputDir = outputDir.substring(0, outputDir.length() - 1);
            }
            File currFile = new File(baseDir + "/" + file);
            String additionalPath = currFile.getAbsolutePath().replace(baseDir + "/", "");
            if (currFile.isDirectory()) {
                ReturnValue currRet = recursivelyCopyDir(currFile.getAbsolutePath(), currFile.list(), outputDir + "/" + additionalPath, fileArray);
                if (currRet.getExitStatus() != ReturnValue.SUCCESS) {
                    return (currRet);
                }
            } else {
                System.out.println("\n  COPYING FILE: " + currFile.getAbsolutePath() + "\n    to " + outputDir + "/" + additionalPath);
                if (!provisionFile(currFile.getAbsolutePath(), outputDir + "/" + additionalPath.substring(0, additionalPath.length() - file.length()), skipIfMissing, fileArray)) {
                    ret.setExitStatus(ReturnValue.FAILURE);
                    return (ret);
                }
            }
        }
        return (ret);
    }

    protected boolean provisionFile(String input, String output, boolean skipIfMissing, ArrayList<FileMetadata> fileArray) {
        BufferedInputStream reader = null;
        int bufLen = 5000 * 1024;
        System.err.println("PROVISION FILE: Looking at file array: " + fileArray.size());
        for (FileMetadata fmd : fileArray) {
            System.err.println("Examining: " + fmd.getFilePath() + " fileUti's file: " + filesUtil.getFileName() + " fileutil's original file: " + filesUtil.getOriginalFileName());
            if (fmd.getFilePath() != null && fmd.getFilePath().equals(filesUtil.getOriginalFileName())) {
                fmd.setFilePath(filesUtil.getFileName());
            }
        }
        reader = filesUtil.getSourceReader(input, bufLen, 0L);
        if (reader == null && skipIfMissing) {
            return (true);
        } else if (reader == null) {
            return false;
        }
        return (putDestination(reader, output, bufLen, input, fileArray));
    }

    /**
   * HTTP writeback currently not supported S3 uses multi-part upload which
   * should deal with failed uploads (maybe) I'm not really dealing with failed
   * upload recovery here... Keep in mind only the writeout to local file will
   * attempt to recover from failed reader
   * 
   * @param output
   * @param bufferLength
   * @param startPosition
   * @return
   */
    public boolean putDestination(BufferedInputStream reader, String output, int bufLen, String input, ArrayList<FileMetadata> fileArray) {
        System.err.println("PUT DESTINNATION: Looking at file array: " + fileArray.size());
        for (FileMetadata fmd : fileArray) {
            System.err.println("Examining: " + fmd.getFilePath() + " fileUti's file: " + output + " " + filesUtil.getFileName() + " fileutil's original file: " + filesUtil.getOriginalFileName());
            if (fmd.getFilePath() != null && fmd.getFilePath().equals(filesUtil.getOriginalFileName())) {
                fmd.setFilePath(filesUtil.getFileName());
                System.err.println("    SETTING FINAL PATH: " + filesUtil.getFileName());
            }
        }
        if (output.startsWith("s3://")) {
            return filesUtil.putToS3(reader, output);
        } else if (output.startsWith("http://") || output.startsWith("https://")) {
            return filesUtil.putToHttp();
        } else {
            if (input.startsWith("http://") || input.startsWith("https://") || input.startsWith("s3://") || options.has("force-copy") || options.has("recursive")) {
                if (options.has("decrypt-key")) {
                    Cipher cipher = filesUtil.getCipher((String) options.valueOf("decrypt-key"));
                    if (filesUtil.copyToFile(reader, output, bufLen, input, cipher) == null) {
                        return false;
                    }
                } else {
                    if (filesUtil.copyToFile(reader, output, bufLen, input) == null) {
                        return false;
                    }
                }
            } else {
                filesUtil.createSymlink(output, input);
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ReturnValue do_verify_output() {
        ReturnValue ret = new ReturnValue();
        ret.setExitStatus(ReturnValue.SUCCESS);
        return (ret);
    }
}
