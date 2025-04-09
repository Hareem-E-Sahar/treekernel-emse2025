package cytoprophetServer;

import java.io.*;
import java.util.*;
import java.net.URLDecoder;
import javax.servlet.*;
import javax.servlet.http.*;

public class testLargeFile extends HttpServlet {

    static final long serialVersionUID = 23424;

    ResourceBundle rb = ResourceBundle.getBundle("LocalStrings");

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String run_loc = "/usr/local/apache-tomcat-6.0.14/runs/" + request.getRemoteAddr();
        File folder = new File(run_loc);
        folder.mkdir();
        int run_no = ((int) (Math.random() * 25555));
        String config_fname, proteins_fname, ddi_fname, ppi_fname, go_fname, att_fname, sif_fname, output_fname, alias_fname;
        config_fname = run_loc + "/config" + run_no + ".xml";
        proteins_fname = run_loc + "/proteins" + run_no + ".plist";
        String algorithm;
        algorithm = request.getParameter("algorithm");
        if (algorithm == null) algorithm = "mp";
        String file_home, ppi_src, ddi_src;
        file_home = "/usr/local/tomcat/database_sets/";
        if (algorithm == "mssc") {
            ppi_src = file_home + "cytoprophet_input_mssc.ppi";
            ddi_src = file_home + "cytoprophet_input_mssc.ddi";
        } else if (algorithm == "mle") {
            ppi_src = file_home + "cytoprophet_input_mle.ppi";
            ddi_src = file_home + "cytoprophet_input_mle.ddi";
        } else {
            ppi_src = file_home + "cytoprophet_input_spa.ppi";
            ddi_src = file_home + "cytoprophet_input_spa.ddi";
        }
        if (request.getParameter("proteins") != null) {
            try {
                BufferedWriter proteins = new BufferedWriter(new FileWriter(proteins_fname));
                proteins.write(URLDecoder.decode(request.getParameter("proteins"), "UTF-8"));
                proteins.close();
            } catch (UnsupportedEncodingException uee) {
                out.write("Unsupported encoding");
            } catch (IOException ioexec) {
                out.write("Error in creating plist");
            }
        }
        File f_config = new File(config_fname);
        copy(new File("/usr/local/apache-tomcat-6.0.14/runs/header.xml"), f_config);
        ppi_fname = run_loc + "/testoutput" + run_no + ".ppi";
        ddi_fname = run_loc + "/testoutput" + run_no + ".ddi";
        go_fname = run_loc + "/gofile" + run_no + ".gd";
        alias_fname = run_loc + "/myalias" + run_no + ".alias";
        File ppi = new File(ppi_fname);
        ppi.createNewFile();
        File ddi = new File(ddi_fname);
        ddi.createNewFile();
        output_fname = run_loc + "/myplugin" + run_no + ".ppi";
        try {
            BufferedWriter config = new BufferedWriter(new FileWriter(config_fname, true));
            String pfam_fname;
            pfam_fname = "/usr/local/tomcat/database_sets/swiss_alias_v23_yeast_human_No_PfamB.pfam";
            config.write("<parameters configuration=\"http_execution\">\n");
            config.write("<pfam_filename>" + pfam_fname + "</pfam_filename>\n");
            config.write("<simulation_objective>ppi predict protein list</simulation_objective>\n");
            config.write("<prediction_ppi_filename>" + ppi_src + "</prediction_ppi_filename>\n");
            config.write("<prediction_ddi_filename>" + ddi_src + "</prediction_ddi_filename>\n");
            config.write("<alias_filename>" + alias_fname + "</alias_filename>");
            config.write("<prediction_protein_list>" + proteins_fname + "</prediction_protein_list>\n");
            config.write("<ppi_list_filename>" + ppi_fname + "</ppi_list_filename>\n");
            config.write("<ddi_list_filename>" + ddi_fname + "</ddi_list_filename>\n");
            config.write("<ppi_to_plugin_filename>" + output_fname + "</ppi_to_plugin_filename>");
            config.write("</parameters>\n");
            config.write("\n\n<parameters configuration=\"calc_go\">\n");
            config.write("<go_filename>/usr/local/tomcat/database_sets/uniprot.go</go_filename>\n");
            config.write("<simulation_objective>objective ppi go distance</simulation_objective>\n");
            config.write("<ppi_list_filename>" + output_fname + "</ppi_list_filename>\n");
            config.write("<go_list_filename>" + go_fname + "</go_list_filename>\n");
            config.write("<alias_filename>" + alias_fname + "</alias_filename>");
            config.write("<prob_threshold>0</prob_threshold>\n");
            config.write("</parameters>\n\n");
            config.write("</simulation>\n");
            config.close();
        } catch (IOException e) {
            out.write("ERROR IN CONFIG CREATION");
        }
        String ppi2_cmd;
        ppi2_cmd = "/usr/local/tomcat/ppi2/ppi2 " + config_fname + " http_execution";
        att_fname = run_loc + "/results" + run_no + ".attributes";
        sif_fname = run_loc + "/results" + run_no + ".attributes";
        try {
            Runtime.getRuntime().exec("touch " + att_fname);
            Runtime.getRuntime().exec("touch " + sif_fname);
            Runtime.getRuntime().exec("touch " + output_fname);
            Runtime.getRuntime().exec("touch " + go_fname);
            Runtime.getRuntime().exec("touch " + alias_fname);
            Runtime.getRuntime().exec("chmod 777 " + alias_fname);
            Runtime.getRuntime().exec("chmod 777 " + go_fname);
            Runtime.getRuntime().exec("chown clamanna " + go_fname);
            Runtime.getRuntime().exec("chmod 777 " + output_fname);
            Runtime.getRuntime().exec("chmod 777 " + att_fname);
            Runtime.getRuntime().exec("chmod 777 " + sif_fname);
            Runtime.getRuntime().exec("chown clamanna " + output_fname);
            Runtime.getRuntime().exec("chmod 777 " + ppi_fname);
            Runtime.getRuntime().exec("chmod 777 " + ddi_fname);
            Process p_ppi2 = Runtime.getRuntime().exec(ppi2_cmd);
            p_ppi2.waitFor();
            Process go_ppi2 = Runtime.getRuntime().exec("/usr/local/tomcat/ppi2/ppi2 " + config_fname + " calc_go");
            go_ppi2.waitFor();
            streamFile(output_fname, out);
            out.write("<START PPI>\n");
            streamFile(ppi_fname, out);
            out.write("<START DDI>\n");
            streamFile(ddi_fname, out);
            out.write("<START GO>\n");
            streamFile(go_fname, out);
            out.write("<START ALIAS>\n");
            streamFile(alias_fname, out);
            out.close();
        } catch (IOException ex) {
            out.write("Error in file i/o");
        } catch (Exception e) {
            out.write("\n\nError in proc\n");
        }
    }

    void streamFile(String fname, PrintWriter out) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(fname));
        String str;
        while ((str = in.readLine()) != null) {
            out.write(str + "\n");
        }
        in.close();
    }

    void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    boolean execute_this(String command, PrintWriter out) {
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            process = runtime.exec(command);
            process.waitFor();
            DataInputStream in = new DataInputStream(process.getInputStream());
            String line = null;
            while ((line = in.readLine()) != null) {
                out.print(line + "||\n");
            }
        } catch (Exception e) {
            out.println("Problem with command");
            return false;
        }
        return true;
    }
}
