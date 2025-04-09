package net.sourceforge.seqware.pipeline.modules.alignment;

import java.io.File;
import java.io.IOException;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.common.util.processtools.stderr.StdErr;
import net.sourceforge.seqware.common.util.runtools.RunTools;
import net.sourceforge.seqware.pipeline.module.Module;
import net.sourceforge.seqware.pipeline.module.ModuleInterface;
import org.apache.commons.lang.StringUtils;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ModuleInterface.class)
public class Bfast extends Module {

    @Override
    public ReturnValue init() {
        if (this.getParameters().size() > 1) {
            this.setAlgorithm(this.getAlgorithm() + " " + this.getParameters().get(1));
            return new ReturnValue();
        } else return new ReturnValue(null, "Bfast requires an arg[1] to specify which module to run", 2);
    }

    @Override
    public ReturnValue do_verify_parameters() {
        ReturnValue ret = new ReturnValue();
        if (this.getParameters().size() < 2) {
            StringBuffer syntax = new StringBuffer("To run bfast you must specify atleast one command. Usage:" + System.getProperty("line.separator"));
            syntax.append(get_syntax());
            ret.setStderr(syntax.toString());
            ret.setExitStatus(2);
            return ret;
        }
        try {
            String[] args = { this.getParameters().get(0), this.getParameters().get(1) };
            Process p = Runtime.getRuntime().exec(args);
            p.waitFor();
            ret.setExitStatus(p.exitValue());
            if (ret.getExitStatus() != 0) {
                StringBuffer syntax = new StringBuffer(this.getParameters().get(1) + " is not a valid command for bfast. Usage:" + System.getProperty("line.separator"));
                syntax.append(get_syntax());
                ret.setStderr(syntax.toString());
            }
        } catch (IOException e) {
            ret.setExitStatus(4);
            ret.setStderr(e.toString());
        } catch (InterruptedException e) {
            ret.setExitStatus(5);
            ret.setStderr(e.toString());
        }
        if (ret.getExitStatus() != 0) return ret;
        if (this.getStdoutFile() == null) {
            ret.setStderr("Bfast writes results to stdout, so you must redirect stdout to a file in order to use it. See the -o option for the seqware runner.");
            ret.setExitStatus(ReturnValue.STDOUTERR);
            return ret;
        }
        boolean valid = false;
        for (int i = 2; i < this.getParameters().size(); i++) {
            if (this.getParameters().get(i).compareTo("-f") == 0) {
                if (this.getParameters().get(i + 1).endsWith(".fa")) {
                    valid = true;
                    break;
                }
            }
        }
        if (valid == false) {
            ret.setExitStatus(3);
            ret.setStderr("Bfast always requires a '-f file.fa' argument");
            return ret;
        }
        if (this.getParameters().get(1).compareTo("match") == 0) {
            valid = false;
            for (int i = 2; i < this.getParameters().size(); i++) {
                if (this.getParameters().get(i).compareTo("-r") == 0) {
                    if (this.getParameters().get(i + 1).endsWith(".fastq")) {
                        valid = true;
                        break;
                    }
                }
            }
            if (valid == false) {
                ret.setExitStatus(3);
                ret.setStderr("Bfast match always requires a '-r file.fastq' argument");
                return ret;
            }
        } else if (this.getParameters().get(1).compareTo("localalign") == 0) {
            valid = false;
            for (int i = 2; i < this.getParameters().size(); i++) {
                if (this.getParameters().get(i).compareTo("-m") == 0) {
                    if (this.getParameters().get(i + 1).endsWith(".bmf")) {
                        valid = true;
                        break;
                    }
                }
            }
            if (valid == false) {
                ret.setExitStatus(3);
                ret.setStderr("Bfast localalign always requires a '-m file.bmf' argument");
                return ret;
            }
        } else if (this.getParameters().get(1).compareTo("postprocess") == 0) {
            valid = false;
            for (int i = 2; i < this.getParameters().size(); i++) {
                if (this.getParameters().get(i).compareTo("-i") == 0) {
                    if (this.getParameters().get(i + 1).endsWith(".baf")) {
                        valid = true;
                        break;
                    }
                }
            }
            if (valid == false) {
                ret.setExitStatus(3);
                ret.setStderr("Bfast postprocess always requires a '-i file.baf' argument");
                return ret;
            }
        }
        ret.setExitStatus(0);
        return ret;
    }

    @Override
    public ReturnValue do_run() {
        String cmd = StringUtils.join(this.getParameters(), ' ');
        if (this.getStdoutFile() != null) {
            cmd += " > " + this.getStdoutFile().getAbsolutePath();
            cmd += " 2> " + this.getStdoutFile().getAbsolutePath() + ".stderr";
            return RunTools.runCommand(new String[] { "bash", "-c", cmd });
        } else {
            return RunTools.runCommand(cmd);
        }
    }

    @Override
    public ReturnValue do_verify_input() {
        for (int i = 2; i < this.getParameters().size(); i++) {
            if (this.getParameters().get(i).endsWith(".fa")) {
            }
        }
        if (this.getParameters().get(1).compareTo("match") == 0) {
            for (int i = 2; i < this.getParameters().size(); i++) {
                if (this.getParameters().get(i).endsWith(".fastq")) {
                    File file = new File(this.getParameters().get(i));
                    if (!(file.exists() && file.canRead() && file.length() > 0)) {
                        return new ReturnValue(null, ".fastq file must be readable and non-zero", 1);
                    }
                }
            }
        } else if (this.getParameters().get(1).compareTo("localalign") == 0) {
            for (int i = 2; i < this.getParameters().size(); i++) {
                if (this.getParameters().get(i).endsWith(".bmf")) {
                    File file = new File(this.getParameters().get(i));
                    if (!(file.exists() && file.canRead() && file.length() > 0)) {
                        return new ReturnValue(null, ".bmf file must be readable and non-zero", 1);
                    }
                }
            }
        } else if (this.getParameters().get(1).compareTo("postprocess") == 0) {
            for (int i = 2; i < this.getParameters().size(); i++) {
                if (this.getParameters().get(i).endsWith(".baf")) {
                    File file = new File(this.getParameters().get(i));
                    if (!(file.exists() && file.canRead() && file.length() > 0)) {
                        return new ReturnValue(null, ".baf file must be readable and non-zero", 1);
                    }
                }
            }
        }
        return new ReturnValue(null, null, 0);
    }

    @Override
    public ReturnValue do_test() {
        ReturnValue ret = new ReturnValue();
        if (this.getParameters().get(1).compareTo("match") == 0) {
        } else if (this.getParameters().get(1).compareTo("localalign") == 0) {
        } else if (this.getParameters().get(1).compareTo("postprocess") == 0) {
        }
        return ret;
    }

    @Override
    public ReturnValue do_verify_output() {
        if (this.getParameters().get(1).compareTo("match") == 0) {
            if (!(this.getStdoutFile().exists() && this.getStdoutFile().length() > 0)) {
                return new ReturnValue(null, ".bmf file must be readable and non-zero", 1);
            }
        } else if (this.getParameters().get(1).compareTo("localalign") == 0) {
            if (!(this.getStdoutFile().exists() && this.getStdoutFile().length() > 0)) {
                return new ReturnValue(null, ".baf file must be readable and non-zero", 1);
            }
        } else if (this.getParameters().get(1).compareTo("postprocess") == 0) {
            if (!(this.getStdoutFile().exists() && this.getStdoutFile().length() > 0)) {
                return new ReturnValue(null, ".sam file must be readable and non-zero", 1);
            }
        }
        return new ReturnValue(null, null, 0);
    }

    @Override
    public String get_syntax() {
        StringBuffer ReturnString = new StringBuffer("Module to Bfast. Bfast syntax:" + System.getProperty("line.separator"));
        Process p = null;
        try {
            if (this.getParameters().size() > 1) {
                p = Runtime.getRuntime().exec(this.getAlgorithm() + " " + this.getParameters().get(1));
            } else {
                p = Runtime.getRuntime().exec(this.getAlgorithm());
            }
        } catch (IOException e) {
            return new String("Exception occured when trying to get_syntax from bfast. Please make sure it is setup properly." + System.getProperty("line.separator") + e.toString());
        }
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            return new String("Exception occured when trying to get_syntax from bfast. Please make sure it is setup properly." + System.getProperty("line.separator") + e.toString());
        }
        if (p.exitValue() != 0 && this.getParameters().size() > 1) {
            try {
                p = Runtime.getRuntime().exec("bfast");
            } catch (IOException e) {
                return new String("Exception occured when trying to get_syntax from bfast. Please make sure it is setup properly." + System.getProperty("line.separator") + e.toString());
            }
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                return new String("Exception occured when trying to get_syntax from bfast. Please make sure it is setup properly." + System.getProperty("line.separator") + e.toString());
            }
        }
        try {
            ReturnString.append(StdErr.stderr2string(p));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ReturnString.toString();
    }
}
