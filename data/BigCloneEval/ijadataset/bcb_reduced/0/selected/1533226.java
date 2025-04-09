package it.gashale.jacolib.process;

import it.gashale.jacolib.console.ConsoleInterface;
import it.gashale.jacolib.console.StandardIOConsole;
import it.gashale.jacolib.core.JacolibError;
import it.gashale.jacolib.core.ProtocolError;
import it.gashale.jacolib.util.FileSystem;
import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

public abstract class RemoteProcess {

    private ConsoleInterface console;

    private ProcessReader in_reader;

    private ProcessReader out_reader;

    private ProcessReader err_reader;

    private Process process;

    protected String process_command_name;

    protected String bin_paths;

    protected String command_line_options;

    protected String load_option;

    protected String load_file;

    protected String initial_input_resource;

    protected RemoteProcess(String process_command_name, String bin_paths, String command_line_options, String load_option, String load_file, String initial_input_resource) {
        console = null;
        in_reader = null;
        out_reader = null;
        err_reader = null;
        process = null;
        this.process_command_name = process_command_name;
        this.bin_paths = bin_paths;
        this.command_line_options = command_line_options;
        this.load_option = load_option;
        this.load_file = load_file;
        this.initial_input_resource = initial_input_resource;
    }

    public ConsoleInterface getConsole() {
        return console;
    }

    public void setConsole(ConsoleInterface con) {
        console = con;
        if (in_reader != null) {
            err_reader.printer = console.getErr();
            out_reader.printer = console.getOut();
            in_reader.in = console.getIn();
        }
    }

    public String getProcessCommandName() {
        return process_command_name;
    }

    public void setProcessCommandName(String v) {
        process_command_name = v;
    }

    public String getBinPaths() {
        return bin_paths;
    }

    public void setBinPaths(String v) {
        bin_paths = v;
    }

    public String getCommandLineOptions() {
        return command_line_options;
    }

    public void setCommandLineOptions(String v) {
        command_line_options = v;
    }

    public String getLoadOption() {
        return load_option;
    }

    public String getLoadFile() {
        return load_file;
    }

    public String getInitialInputResource() {
        return initial_input_resource;
    }

    protected String getProcessCommandLine() throws JacolibError {
        String prg = null;
        String paths[] = bin_paths.split(":");
        for (int i = 0; i < paths.length; ++i) {
            prg = paths[i] + File.separatorChar + getProcessCommandName();
            if (existFile(prg)) break; else prg = null;
        }
        if (prg == null) throw new JacolibError("Impossible to find the lips command " + getProcessCommandName() + ".");
        String args = getCommandLineOptions();
        if (load_file != null) {
            args += getLoadOption() + " " + getLoadFile();
        }
        String command = prg + " " + args;
        return command;
    }

    public void start() throws JacolibError {
        String command = getProcessCommandLine();
        _start(command);
        _init(getInitialInputResource());
        post_init();
    }

    public void exit() {
        in_reader.thread.interrupt();
    }

    protected abstract void post_init() throws JacolibError;

    protected void writeIntoProcessInput(String command) throws ProtocolError {
        try {
            process.getOutputStream().write(command.getBytes());
            process.getOutputStream().write('\n');
            process.getOutputStream().flush();
        } catch (IOException e) {
            throw new ProtocolError("Error. " + e.getMessage());
        }
    }

    private void _start(String command) throws ProtocolError {
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new ProtocolError("Impossible to run the program: " + command + ". " + e.getMessage());
        }
        if (console == null) {
            console = new StandardIOConsole();
        }
        in_reader = new ProcessReader(console.getIn(), process.getOutputStream(), true);
        err_reader = new ProcessReader(process.getErrorStream(), console.getErr(), false);
        out_reader = new ProcessReader(process.getInputStream(), console.getOut(), false);
    }

    private void _init(String resource) throws ProtocolError {
        if (resource != null) {
            InputStream in = FileSystem.class.getClassLoader().getResourceAsStream(resource);
            if (in == null) throw new ProtocolError("Impossible to initialize external process with resource " + resource + ".");
            byte[] aoBuffer = new byte[512];
            int nBytesRead;
            try {
                while ((nBytesRead = in.read(aoBuffer)) > 0) {
                    process.getOutputStream().write(aoBuffer, 0, nBytesRead);
                }
                process.getOutputStream().write('\n');
                process.getOutputStream().flush();
                in.close();
            } catch (IOException e) {
                throw new ProtocolError("Impossible to initialize external process. " + e.getMessage());
            }
        }
    }

    protected boolean existFile(String filename) {
        File f = new File(filename);
        return f.exists();
    }

    private class ProcessReader implements Runnable {

        private Reader in = null;

        private PrintStream printer = null;

        private boolean line_based;

        private Thread thread;

        public ProcessReader(InputStream in, PrintStream printer, boolean line_based) {
            this.in = new InputStreamReader(in);
            this.printer = printer;
            this.line_based = line_based;
            thread = new Thread(this);
            thread.start();
        }

        public ProcessReader(Reader in, OutputStream printer, boolean line_based) {
            this.in = in;
            this.printer = new PrintStream(printer);
            this.line_based = line_based;
            thread = new Thread(this);
            thread.start();
        }

        public synchronized void run() {
            if (in != null && printer != null) try {
                read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void read() throws IOException {
            if (line_based) {
                BufferedReader br = new BufferedReader(in);
                String line;
                while ((line = br.readLine()) != null) {
                    printer.println(line);
                }
            } else {
                int c;
                while ((c = in.read()) != -1) {
                    printer.print((char) c);
                }
            }
        }
    }
}
