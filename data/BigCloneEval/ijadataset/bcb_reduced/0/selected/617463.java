package org.jcvi.fasta.fastq.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jcvi.common.command.CommandLineOptionBuilder;
import org.jcvi.common.command.CommandLineUtils;
import org.jcvi.common.core.datastore.DataStoreException;
import org.jcvi.common.core.io.IOUtil;
import org.jcvi.common.core.seq.fastx.fastq.FastqQualityCodec;
import org.jcvi.common.core.seq.fastx.fastq.FastqRecord;
import org.jcvi.common.core.seq.fastx.fastq.LargeFastqFileDataStore;
import org.jcvi.common.core.util.iter.CloseableIterator;
import org.jcvi.common.io.fileServer.DirectoryFileServer;
import org.jcvi.common.io.fileServer.DirectoryFileServer.ReadWriteDirectoryFileServer;

/**
 * @author dkatzel
 *
 *
 */
public class SplitFastq {

    /**
     * @param args
     * @throws ParseException 
     * @throws IOException 
     * @throws DataStoreException 
     */
    public static void main(String[] args) throws IOException, DataStoreException {
        Options options = new Options();
        options.addOption(new CommandLineOptionBuilder("i", "input fastq file").isRequired(true).build());
        options.addOption(new CommandLineOptionBuilder("o", "output directory to write split files to").isRequired(true).build());
        options.addOption(new CommandLineOptionBuilder("n", "number of files to split into").isRequired(true).build());
        options.addOption(new CommandLineOptionBuilder("sanger", "input fastq file is encoded as a SANGER fastq file (default is ILLUMINA 1.3+)").isFlag(true).build());
        options.addOption(CommandLineUtils.createHelpOption());
        if (CommandLineUtils.helpRequested(args)) {
            printHelp(options);
            System.exit(0);
        }
        CommandLine commandLine;
        try {
            commandLine = CommandLineUtils.parseCommandLine(options, args);
            File fastqFile = new File(commandLine.getOptionValue("i"));
            final FastqQualityCodec fastqQualityCodec;
            if (commandLine.hasOption("sanger")) {
                fastqQualityCodec = FastqQualityCodec.SANGER;
            } else {
                fastqQualityCodec = FastqQualityCodec.ILLUMINA;
            }
            int n = Integer.parseInt(commandLine.getOptionValue("n"));
            ReadWriteDirectoryFileServer outputDir = DirectoryFileServer.createReadWriteDirectoryFileServer(commandLine.getOptionValue("o"));
            List<PrintWriter> writers = createWriters(outputDir, fastqFile, n);
            CloseableIterator<FastqRecord> iterator = LargeFastqFileDataStore.create(fastqFile, fastqQualityCodec).iterator();
            int counter = 0;
            try {
                while (iterator.hasNext()) {
                    int mod = counter % n;
                    FastqRecord record = iterator.next();
                    writers.get(mod).print(record.toFormattedString(fastqQualityCodec));
                    counter++;
                }
            } finally {
                for (PrintWriter writer : writers) {
                    IOUtil.closeAndIgnoreErrors(writer);
                }
                iterator.close();
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            printHelp(options);
            System.exit(1);
        }
    }

    /**
     * @param outputDir
     * @param fastqFile
     * @param n
     * @return
     * @throws IOException 
     */
    private static List<PrintWriter> createWriters(ReadWriteDirectoryFileServer outputDir, File fastqFile, int n) throws IOException {
        List<PrintWriter> writers = new ArrayList<PrintWriter>();
        String basename = fastqFile.getName();
        try {
            for (int i = 0; i < n; i++) {
                File newFile = outputDir.createNewFile(String.format("%s.part_%d.fastq", basename, i));
                writers.add(new PrintWriter(newFile));
            }
            return writers;
        } catch (IOException e) {
            for (PrintWriter writer : writers) {
                IOUtil.closeAndIgnoreErrors(writer);
            }
            throw e;
        }
    }

    /**
     * @param options
     */
    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("splitFastq [OPTIONS] -i <fastq file> -n <# files>", "Parse a fastQ file and re-write the data into several files.  Each file will contain 1/nth of the total number of reads." + "the files will be named <original fastq file>.part_[0-(n-1)].fastq", options, "Created by Danny Katzel");
    }
}
