package net.sf.gridarta.model.validation.checks;

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.gridarta.model.archetype.Archetype;
import net.sf.gridarta.model.gameobject.GameObject;
import net.sf.gridarta.model.io.MapWriter;
import net.sf.gridarta.model.maparchobject.MapArchObject;
import net.sf.gridarta.model.mapmodel.MapModel;
import net.sf.gridarta.model.mapmodel.MapSquare;
import net.sf.gridarta.model.validation.AbstractValidator;
import net.sf.gridarta.model.validation.ErrorCollector;
import net.sf.gridarta.model.validation.MapValidator;
import net.sf.gridarta.model.validation.ValidatorPreferences;
import net.sf.gridarta.utils.CopyReader;
import net.sf.gridarta.utils.IOUtils;
import net.sf.gridarta.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Executes a script to check a map. The script's output is parsed and converted
 * into validation errors.
 * @author Andreas Kirschbaum
 */
public class MapCheckerScriptChecker<G extends GameObject<G, A, R>, A extends MapArchObject<A>, R extends Archetype<G, A, R>> extends AbstractValidator<G, A, R> implements MapValidator<G, A, R> {

    /**
     * The maximum execution time of the checker script in milliseconds. If the
     * script runs longer, it will be terminated.
     */
    private static final int MAX_EXEC_TIME = 30000;

    /**
     * The placeholder in the command's arguments for the map to check.
     */
    @NotNull
    public static final String MAP_PLACEHOLDER = "${MAP}";

    /**
     * The quoted. placeholder in the command's arguments for the map to check.
     */
    @NotNull
    private static final String QUOTED_MAP_PLACEHOLDER = Pattern.quote(MAP_PLACEHOLDER);

    /**
     * The {@link Pattern} for matching per map square messages.
     */
    @NotNull
    private static final Pattern PATTERN_MAP_SQUARE_MESSAGE = Pattern.compile("(\\d+)\\s+(\\d+)\\s+(.*)");

    /**
     * The {@link MapWriter} for saving temporary map files.
     */
    @NotNull
    private final MapWriter<G, A, R> mapWriter;

    /**
     * The {@link CommandFinder} for the script to execute.
     */
    @NotNull
    private final CommandFinder commandFinder1 = new CommandFinder();

    /**
     * The {@link CommandFinder} for the script interpreter.
     */
    @NotNull
    private final CommandFinder commandFinder2 = new CommandFinder();

    /**
     * The command to execute and the arguments to pass.
     */
    @NotNull
    private final String[] args;

    /**
     * The temp file for saving maps to be checked. Set to <code>null</code>
     * until created.
     */
    @Nullable
    private File tmpFile = null;

    /**
     * Creates a new instance.
     * @param validatorPreferences the validator preferences to use
     * @param mapWriter the map writer for saving temporary map files
     * @param args the command to execute and the arguments to pass
     */
    public MapCheckerScriptChecker(@NotNull final ValidatorPreferences validatorPreferences, @NotNull final MapWriter<G, A, R> mapWriter, @NotNull final String[] args) {
        super(validatorPreferences);
        this.mapWriter = mapWriter;
        this.args = args.clone();
        if (this.args.length < 1) {
            throw new IllegalArgumentException("no script to execute");
        }
        boolean found = false;
        for (int i = 1; i < this.args.length; i++) {
            if (this.args[i].contains(MAP_PLACEHOLDER)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("the script to execute doesn't receive the map to check (" + MAP_PLACEHOLDER + ")");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateMap(@NotNull final MapModel<G, A, R> mapModel, @NotNull final ErrorCollector<G, A, R> errorCollector) {
        final String[] command = getCommand(mapModel, errorCollector);
        if (command == null) {
            return;
        }
        final Process process;
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (final IOException ex) {
            errorCollector.collect(new MapCheckerScriptMissingError<G, A, R>(mapModel, command[0], ex.getMessage()));
            return;
        }
        @Nullable final String output;
        try {
            try {
                process.getOutputStream().close();
            } catch (final IOException ex) {
                errorCollector.collect(new MapCheckerScriptFailureError<G, A, R>(mapModel, command[0], ex.getMessage() + " (closing stdin)"));
                return;
            }
            output = runProcess(process, errorCollector, mapModel, command[0]);
        } finally {
            process.destroy();
        }
        if (output != null) {
            parseOutput(output, errorCollector, mapModel, command[0]);
        }
    }

    /**
     * Parses output of the executed script. Adds validation error to an {@link
     * ErrorCollector}.
     * @param output the output to parse
     * @param errorCollector the error collector to add to
     * @param mapModel the map model being checked
     * @param command the command being run
     */
    private void parseOutput(@NotNull final CharSequence output, @NotNull final ErrorCollector<G, A, R> errorCollector, @NotNull final MapModel<G, A, R> mapModel, @NotNull final String command) {
        final String[] lines = StringUtils.PATTERN_END_OF_LINE.split(output);
        for (final String line : lines) {
            if (!line.isEmpty()) {
                final Matcher matcher = PATTERN_MAP_SQUARE_MESSAGE.matcher(line);
                if (matcher.matches()) {
                    final int x;
                    final int y;
                    try {
                        x = Integer.parseInt(matcher.group(1));
                        y = Integer.parseInt(matcher.group(2));
                    } catch (final NumberFormatException ignored) {
                        errorCollector.collect(new MapCheckerScriptFailureError<G, A, R>(mapModel, command, "syntax error in '" + line + "'"));
                        continue;
                    }
                    final String message = matcher.group(3);
                    final MapSquare<G, A, R> mapSquare;
                    try {
                        mapSquare = mapModel.getMapSquare(new Point(x, y));
                    } catch (final IndexOutOfBoundsException ignored) {
                        errorCollector.collect(new MapCheckerScriptFailureError<G, A, R>(mapModel, command, "invalid map square in '" + line + "'"));
                        continue;
                    }
                    errorCollector.collect(new MapCheckerScriptMapSquareError<G, A, R>(mapSquare, message));
                } else {
                    errorCollector.collect(new MapCheckerScriptMapError<G, A, R>(mapModel, line));
                }
            }
        }
    }

    /**
     * Waits for a {@link Process} to terminate and returns its output.
     * @param process the process
     * @param errorCollector the error collector to add problems to
     * @param mapModel the map model that is being checked
     * @param command the command that is being run
     * @return the command's output
     */
    @Nullable
    private String runProcess(@NotNull final Process process, @NotNull final ErrorCollector<G, A, R> errorCollector, @NotNull final MapModel<G, A, R> mapModel, @NotNull final String command) {
        @Nullable String output;
        final InputStreamReader stdoutReader = new InputStreamReader(process.getInputStream());
        try {
            final CopyReader stdout = new CopyReader(stdoutReader);
            final InputStreamReader stderrReader = new InputStreamReader(process.getErrorStream());
            try {
                final CopyReader stderr = new CopyReader(stderrReader);
                stdout.start();
                try {
                    stderr.start();
                    try {
                        if (!waitForTermination(process, errorCollector, mapModel, command, stdout, stderr)) {
                            return null;
                        }
                    } finally {
                        stderr.stop();
                    }
                } finally {
                    stdout.stop();
                }
            } finally {
                try {
                    stderrReader.close();
                } catch (final IOException ex) {
                    errorCollector.collect(new MapCheckerScriptFailureError<G, A, R>(mapModel, command, ex.getMessage() + " (closing stderr)"));
                }
            }
            final String stdoutFailure = stdout.getFailure();
            if (stdoutFailure != null) {
                errorCollector.collect(new MapCheckerScriptFailureError<G, A, R>(mapModel, command, stdoutFailure));
                return null;
            }
            output = stdout.getOutput();
        } finally {
            try {
                stdoutReader.close();
            } catch (final IOException ex) {
                errorCollector.collect(new MapCheckerScriptFailureError<G, A, R>(mapModel, command, ex.getMessage() + " (closing stdout)"));
            }
        }
        return output;
    }

    /**
     * Waits for a {@link Process} to terminate.
     * @param process the process
     * @param errorCollector the error collector to add problems to
     * @param mapModel the map model being checked
     * @param command the command being executed
     * @param stdout the stdout stream of the process
     * @param stderr the stderr stream of the process
     * @return whether the process did exit successfully
     */
    private boolean waitForTermination(@NotNull final Process process, @NotNull final ErrorCollector<G, A, R> errorCollector, @NotNull final MapModel<G, A, R> mapModel, @NotNull final String command, @NotNull final CopyReader stdout, @NotNull final CopyReader stderr) {
        final Semaphore sem = new Semaphore(0);
        final int[] tmp = { -1 };
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    try {
                        stdout.join();
                        stderr.join();
                        tmp[0] = process.waitFor();
                    } catch (final InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                } finally {
                    sem.release();
                }
            }
        };
        final Thread thread = new Thread(runnable);
        thread.start();
        try {
            if (!sem.tryAcquire(MAX_EXEC_TIME, TimeUnit.MILLISECONDS)) {
                errorCollector.collect(new MapCheckerScriptFailureError<G, A, R>(mapModel, command, "timeout waiting for script to terminate"));
                return false;
            }
        } catch (final InterruptedException ignored) {
            errorCollector.collect(new MapCheckerScriptFailureError<G, A, R>(mapModel, command, "interrupted waiting for script to terminate"));
            return false;
        }
        thread.interrupt();
        if (tmp[0] != 0) {
            errorCollector.collect(new MapCheckerScriptFailureError<G, A, R>(mapModel, command, "command exited with status " + tmp[0]));
            return false;
        }
        final String stderrFailure = stderr.getFailure();
        if (stderrFailure != null) {
            errorCollector.collect(new MapCheckerScriptFailureError<G, A, R>(mapModel, command, stderrFailure));
            return false;
        }
        final String stderrOutput = stderr.getOutput();
        if (!stderrOutput.isEmpty()) {
            errorCollector.collect(new MapCheckerScriptFailureError<G, A, R>(mapModel, command, StringUtils.PATTERN_NEWLINE.matcher(stderrOutput).replaceAll("<br>")));
            return false;
        }
        return true;
    }

    /**
     * Returns the command to execute. Returns {@link #args} but replaces {@link
     * #MAP_PLACEHOLDER} with the map to check.
     * @param mapModel the map to check
     * @param errorCollector the error collector for reporting problems
     * @return the command to execute or <code>null</code> if an error occurred
     */
    @Nullable
    private String[] getCommand(@NotNull final MapModel<G, A, R> mapModel, @NotNull final ErrorCollector<G, A, R> errorCollector) {
        if (tmpFile == null) {
            try {
                tmpFile = File.createTempFile("gridarta", null);
            } catch (final IOException ex) {
                errorCollector.collect(new MapCheckerScriptIOError<G, A, R>(mapModel, "create temporary file", ex.getMessage()));
                return null;
            }
            assert tmpFile != null;
            tmpFile.deleteOnExit();
        }
        assert tmpFile != null;
        final String mapPath = tmpFile.getPath();
        try {
            final OutputStream outputStream = new FileOutputStream(tmpFile);
            try {
                final Writer writer = new OutputStreamWriter(outputStream, IOUtils.MAP_ENCODING);
                try {
                    mapWriter.encodeMapFile(mapModel, writer);
                } finally {
                    writer.close();
                }
            } finally {
                outputStream.close();
            }
        } catch (final IOException ex) {
            errorCollector.collect(new MapCheckerScriptIOError<G, A, R>(mapModel, mapPath, ex.getMessage()));
            return null;
        }
        final boolean isWindows = System.getProperty("os.name").contains("Windows");
        final String[] result;
        int index = 0;
        try {
            if (isWindows) {
                if (args[0].toLowerCase().endsWith(".py")) {
                    result = new String[args.length + 1];
                    String command;
                    try {
                        command = commandFinder2.getCommand("python.exe");
                    } catch (final IOException ex) {
                        command = "C:" + File.separator + "python27" + File.separator + "python.exe";
                        if (!new File(command).exists()) {
                            throw ex;
                        }
                    }
                    result[index++] = "\"" + command + "\"";
                } else {
                    result = new String[args.length];
                }
                result[index++] = "\"" + commandFinder1.getCommand(args[0]) + "\"";
            } else {
                result = new String[args.length];
                result[index++] = commandFinder1.getCommand(args[0]);
            }
        } catch (final IOException ex) {
            errorCollector.collect(new MapCheckerScriptMissingError<G, A, R>(mapModel, args[0], ex.getMessage()));
            return null;
        }
        final String mapPathQuoted = Matcher.quoteReplacement(mapPath);
        for (int i = 1; i < args.length; i++) {
            final String tmp = args[i].replaceAll(QUOTED_MAP_PLACEHOLDER, mapPathQuoted);
            result[index++] = isWindows ? "\"" + tmp + "\"" : tmp;
        }
        return result;
    }

    /**
     * Searches for commands in the PATH environment variable.
     * @author Andreas Kirschbaum
     */
    private static class CommandFinder {

        /**
         * The command to execute. Set to <code>null</code> if unknown.
         * Otherwise points to <code>{@link #args}[0]</code>.
         */
        @Nullable
        private File cachedCommand = null;

        /**
         * Returns the command to execute. Returns or updates {@link
         * #cachedCommand}.
         * @param commandName the command name to search
         * @return the command to execute
         * @throws IOException if the command cannot be found
         */
        @NotNull
        private String getCommand(@NotNull final String commandName) throws IOException {
            final File existingCommand = cachedCommand;
            if (existingCommand != null && existingCommand.exists()) {
                return existingCommand.getPath();
            }
            final File command = IOUtils.findPathFile(commandName);
            cachedCommand = command;
            return command.getPath();
        }
    }
}
