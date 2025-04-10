import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * An out of process executable.
 */
class Command {

    private final List<String> args;

    private final boolean permitNonZeroExitStatus;

    Command(String... args) {
        this(Arrays.asList(args));
    }

    Command(List<String> args) {
        this.args = new ArrayList<String>(args);
        this.permitNonZeroExitStatus = false;
    }

    private Command(Builder builder) {
        this.args = new ArrayList<String>(builder.args);
        this.permitNonZeroExitStatus = builder.permitNonZeroExitStatus;
    }

    static class Builder {

        private final List<String> args = new ArrayList<String>();

        private boolean permitNonZeroExitStatus = false;

        public Builder args(String... args) {
            return args(Arrays.asList(args));
        }

        public Builder args(Collection<String> args) {
            this.args.addAll(args);
            return this;
        }

        public Builder permitNonZeroExitStatus() {
            permitNonZeroExitStatus = true;
            return this;
        }

        public Command build() {
            return new Command(this);
        }

        public List<String> execute() {
            return build().execute();
        }
    }

    public List<String> execute() {
        try {
            Process process = new ProcessBuilder().command(args).redirectErrorStream(true).start();
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> outputLines = new ArrayList<String>();
            String outputLine;
            while ((outputLine = in.readLine()) != null) {
                outputLines.add(outputLine);
            }
            if (process.waitFor() != 0 && !permitNonZeroExitStatus) {
                StringBuilder message = new StringBuilder();
                for (String line : outputLines) {
                    message.append("\n").append(line);
                }
                throw new RuntimeException("Process failed: " + args + message);
            }
            return outputLines;
        } catch (IOException e) {
            throw new RuntimeException("Process failed: " + args, e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Process failed: " + args, e);
        }
    }
}
