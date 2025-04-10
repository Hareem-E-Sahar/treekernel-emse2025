package org.jcvi.assembly.cas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.concurrent.Callable;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jcvi.Builder;
import org.jcvi.assembly.ace.AceContig;
import org.jcvi.assembly.ace.AceFileWriter;
import org.jcvi.assembly.ace.AcePlacedRead;
import org.jcvi.assembly.cas.read.DefaultCasFileReadIndexToContigLookup;
import org.jcvi.command.Command;
import org.jcvi.datastore.DataStoreException;
import org.jcvi.fastX.fasta.seq.DefaultNucleotideEncodedSequenceFastaRecord;
import org.jcvi.glyph.nuc.NucleotideGlyph;
import org.jcvi.io.IOUtil;
import org.jcvi.io.fileServer.DirectoryFileServer;
import org.jcvi.io.fileServer.DirectoryFileServer.ReadWriteDirectoryFileServer;
import org.jcvi.trace.sanger.phd.Phd;
import org.jcvi.trace.sanger.phd.PhdDataStore;
import org.jcvi.trace.sanger.phd.PhdWriter;
import org.jcvi.util.MultipleWrapper;
import org.joda.time.DateTimeUtils;
import org.joda.time.Period;

/**
 * @author dkatzel
 *
 *
 */
public abstract class AbstractMultiThreadedCasAssemblyBuilder implements Builder<CasAssembly> {

    public static final String DEFAULT_PREFIX = "cas2consed";

    private final File casFile;

    private File tempDir;

    private CommandLine commandLine;

    /**
     * @param casFile
     */
    public AbstractMultiThreadedCasAssemblyBuilder(File casFile) {
        this.casFile = casFile;
    }

    public AbstractMultiThreadedCasAssemblyBuilder commandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
        return this;
    }

    public AbstractMultiThreadedCasAssemblyBuilder tempDir(File tempDir) {
        this.tempDir = tempDir;
        return this;
    }

    protected abstract void prepareForBuild();

    /**
    * {@inheritDoc}
    */
    @Override
    public CasAssembly build() {
        try {
            prepareForBuild();
            File casWorkingDirectory = casFile.getParentFile();
            DefaultCasFileReadIndexToContigLookup read2contigMap = new DefaultCasFileReadIndexToContigLookup();
            AbstractDefaultCasFileLookup readIdLookup = new DefaultReadCasFileLookup(casWorkingDirectory);
            CasParser.parseOnlyMetaData(casFile, MultipleWrapper.createMultipleWrapper(CasFileVisitor.class, read2contigMap, readIdLookup));
            ReadWriteDirectoryFileServer consedOut = DirectoryFileServer.createReadWriteDirectoryFileServer(commandLine.getOptionValue("o"));
            long startTime = DateTimeUtils.currentTimeMillis();
            int numberOfCasContigs = read2contigMap.getNumberOfContigs();
            for (long i = 0; i < numberOfCasContigs; i++) {
                File outputDir = consedOut.createNewDir("" + i);
                Command aCommand = new Command(new File("fakeCommand"));
                aCommand.setOption("-casId", "" + i);
                aCommand.setOption("-cas", commandLine.getOptionValue("cas"));
                aCommand.setOption("-o", outputDir.getAbsolutePath());
                aCommand.setOption("-tempDir", tempDir.getAbsolutePath());
                aCommand.setOption("-prefix", "temp");
                if (commandLine.hasOption("useIllumina")) {
                    aCommand.addFlag("-useIllumina");
                }
                if (commandLine.hasOption("useClosureTrimming")) {
                    aCommand.addFlag("-useClosureTrimming");
                }
                if (commandLine.hasOption("trim")) {
                    aCommand.setOption("-trim", commandLine.getOptionValue("trim"));
                }
                if (commandLine.hasOption("trimMap")) {
                    aCommand.setOption("-trimMap", commandLine.getOptionValue("trimMap"));
                }
                if (commandLine.hasOption("chromat_dir")) {
                    aCommand.setOption("-chromat_dir", commandLine.getOptionValue("chromat_dir"));
                }
                submitSingleCasAssemblyConversion(aCommand);
            }
            waitForAllAssembliesToFinish();
            int numContigs = 0;
            int numReads = 0;
            for (int i = 0; i < numberOfCasContigs; i++) {
                File countMap = consedOut.getFile(i + "/temp.counts");
                Scanner scanner = new Scanner(countMap);
                if (!scanner.hasNextInt()) {
                    throw new IllegalStateException("single assembly conversion # " + i + " did not complete");
                }
                numContigs += scanner.nextInt();
                numReads += scanner.nextInt();
                scanner.close();
            }
            System.out.println("num contigs =" + numContigs);
            System.out.println("num reads =" + numReads);
            consedOut.createNewDir("edit_dir");
            consedOut.createNewDir("phd_dir");
            String prefix = commandLine.hasOption("prefix") ? commandLine.getOptionValue("prefix") : DEFAULT_PREFIX;
            OutputStream masterAceOut = new FileOutputStream(consedOut.createNewFile("edit_dir/" + prefix + ".ace.1"));
            OutputStream masterPhdOut = new FileOutputStream(consedOut.createNewFile("phd_dir/" + prefix + ".phd.ball"));
            OutputStream masterConsensusOut = new FileOutputStream(consedOut.createNewFile(prefix + ".consensus.fasta"));
            OutputStream logOut = new FileOutputStream(consedOut.createNewFile(prefix + ".log"));
            try {
                masterAceOut.write(String.format("AS %d %d%n", numContigs, numReads).getBytes());
                for (int i = 0; i < numberOfCasContigs; i++) {
                    InputStream aceIn = consedOut.getFileAsStream(i + "/temp.ace");
                    IOUtils.copy(aceIn, masterAceOut);
                    InputStream phdIn = consedOut.getFileAsStream(i + "/temp.phd");
                    IOUtils.copy(phdIn, masterPhdOut);
                    InputStream consensusIn = consedOut.getFileAsStream(i + "/temp.consensus.fasta");
                    IOUtils.copy(consensusIn, masterConsensusOut);
                    IOUtil.closeAndIgnoreErrors(aceIn, phdIn, consensusIn);
                    File tempDir = consedOut.getFile(i + "");
                    IOUtil.recursiveDelete(tempDir);
                }
                consedOut.createNewSymLink("../phd_dir/" + prefix + ".phd.ball", "edit_dir/phd.ball");
                if (commandLine.hasOption("chromat_dir")) {
                    consedOut.createNewDir("chromat_dir");
                    File originalChromatDir = new File(commandLine.getOptionValue("chromat_dir"));
                    for (File chromat : originalChromatDir.listFiles(new FilenameFilter() {

                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".scf");
                        }
                    })) {
                        File newChromatFile = consedOut.createNewFile("chromat_dir/" + FilenameUtils.getBaseName(chromat.getName()));
                        FileOutputStream newChromat = new FileOutputStream(newChromatFile);
                        InputStream in = new FileInputStream(chromat);
                        IOUtils.copy(in, newChromat);
                        IOUtil.closeAndIgnoreErrors(in, newChromat);
                    }
                }
                System.out.println("finished making casAssemblies");
                for (File traceFile : readIdLookup.getFiles()) {
                    final String name = traceFile.getName();
                    String extension = FilenameUtils.getExtension(name);
                    if (name.contains("fastq")) {
                        if (!consedOut.contains("solexa_dir")) {
                            consedOut.createNewDir("solexa_dir");
                        }
                        if (consedOut.contains("solexa_dir/" + name)) {
                            IOUtil.delete(consedOut.getFile("solexa_dir/" + name));
                        }
                        consedOut.createNewSymLink(traceFile.getAbsolutePath(), "solexa_dir/" + name);
                    } else if ("sff".equals(extension)) {
                        if (!consedOut.contains("sff_dir")) {
                            consedOut.createNewDir("sff_dir");
                        }
                        if (consedOut.contains("sff_dir/" + name)) {
                            IOUtil.delete(consedOut.getFile("sff_dir/" + name));
                        }
                        consedOut.createNewSymLink(traceFile.getAbsolutePath(), "sff_dir/" + name);
                    }
                }
                long endTime = DateTimeUtils.currentTimeMillis();
                logOut.write(String.format("took %s%n", new Period(endTime - startTime)).getBytes());
            } finally {
                IOUtil.closeAndIgnoreErrors(masterAceOut, masterPhdOut, masterConsensusOut, logOut);
            }
        } catch (Exception e) {
            handleException(e);
        } finally {
            cleanup();
        }
        return null;
    }

    protected abstract void submitSingleCasAssemblyConversion(Command command) throws IOException;

    protected abstract void waitForAllAssembliesToFinish() throws Exception;

    protected abstract void cleanup();

    protected abstract void handleException(Exception e);

    public static class AceWriterCallable implements Callable<Void> {

        private final AceContig aceContig;

        private final PhdDataStore phdDataStore;

        private final OutputStream aceOutputStream;

        private final OutputStream consensusOutputStream;

        /**
         * @param aceContig
         * @param phdDataStore
         * @param phdOutputStream
         */
        public AceWriterCallable(AceContig aceContig, PhdDataStore phdDataStore, OutputStream phdOutputStream, OutputStream consensusOutputStream) {
            this.aceContig = aceContig;
            this.phdDataStore = phdDataStore;
            this.aceOutputStream = phdOutputStream;
            this.consensusOutputStream = consensusOutputStream;
        }

        @Override
        public Void call() throws IOException, DataStoreException {
            consensusOutputStream.write(new DefaultNucleotideEncodedSequenceFastaRecord(aceContig.getId(), NucleotideGlyph.convertToString(NucleotideGlyph.convertToUngapped(aceContig.getConsensus().decode()))).toString().getBytes());
            AceFileWriter.writeAceFile(aceContig, phdDataStore, aceOutputStream);
            return null;
        }
    }

    public static class PhdWriterCallable implements Callable<Void> {

        private final AceContig aceContig;

        private final PhdDataStore phdDataStore;

        private final OutputStream phdOutputStream;

        /**
         * @param aceContig
         * @param phdDataStore
         * @param phdOutputStream
         */
        public PhdWriterCallable(AceContig aceContig, PhdDataStore phdDataStore, OutputStream phdOutputStream) {
            this.aceContig = aceContig;
            this.phdDataStore = phdDataStore;
            this.phdOutputStream = phdOutputStream;
        }

        @Override
        public Void call() throws IOException, DataStoreException {
            for (AcePlacedRead read : aceContig.getPlacedReads()) {
                String id = read.getId();
                final Phd phd = phdDataStore.get(id);
                if (phd == null) {
                    throw new NullPointerException("phd is null for " + id);
                }
                PhdWriter.writePhd(phd, phdOutputStream);
            }
            return null;
        }
    }
}
