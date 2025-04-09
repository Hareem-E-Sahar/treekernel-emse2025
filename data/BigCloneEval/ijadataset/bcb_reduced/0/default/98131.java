import java.io.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;
import java.lang.Exception;

public class GBrowseGFFBuilder {

    private Connection conn;

    private PreparedStatement prestmt;

    private ResultSet resultSet;

    private String build;

    private String dbHost;

    private String dbUser;

    private String dbPassword;

    private String dbPort;

    private String outputFile;

    private String dataset;

    private int id = 0;

    public GBrowseGFFBuilder(String build, String dbHost, String dbUser, String dbPassword, String dbPort, String outputFile) {
        this.build = build;
        this.dbHost = dbHost;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbPort = dbPort;
        this.outputFile = outputFile;
    }

    public static void main(String[] args) {
        try {
            if (args.length < 6 && args.length > 0) {
                System.err.println("Please enter build, dbHost, dbUser, dbPassword, dbPort, and outputFile.");
                System.exit(1);
            } else if (!args[0].equals("17") && !args[0].equals("18")) {
                System.err.println("Please enter 17 or 18 for build.");
                System.exit(1);
            } else {
                GBrowseGFFBuilder ggb = new GBrowseGFFBuilder(args[0], args[1], args[2], args[3], args[4], args[5]);
                Calendar cal1 = Calendar.getInstance();
                if (args.length > 6) {
                    ggb.setDataset(args[6]);
                    ggb.appendCustomTrack(ggb.getDataset());
                } else {
                    ggb.addHeaderAndFullLength();
                    ggb.appendChopNormalCnvs();
                    ggb.appendChopNormalCnvrs();
                    ggb.appendChopNormalCnvBlocks();
                    ggb.appendTorontoCnvs();
                    ggb.appendGadPhenotypes();
                    ggb.appendUcscGenes();
                    ggb.appendUcscBands();
                    ggb.appendAllCustomTracks();
                }
                Calendar cal2 = Calendar.getInstance();
                System.out.println("GBrowseGFFBuilder wrote " + ggb.getOutputFile() + " in " + ((cal2.getTimeInMillis() - cal1.getTimeInMillis()) / 1000.0) + " seconds.");
            }
        } catch (Exception e) {
            System.err.println("GBrowseGFFBuilder failed: " + e.getMessage());
            System.exit(1);
        }
    }

    protected void addHeaderAndFullLength() {
        String text = "##gff-version 3\n";
        text += "chr1\tchop\tchromosome\t0\t245203898\t.\t.\t.\tName=chr1\n";
        text += "chr2\tchop\tchromosome\t0\t243315028\t.\t.\t.\tName=chr2\n";
        text += "chr3\tchop\tchromosome\t0\t199411731\t.\t.\t.\tName=chr3\n";
        text += "chr4\tchop\tchromosome\t0\t191610523\t.\t.\t.\tName=chr4\n";
        text += "chr5\tchop\tchromosome\t0\t180967295\t.\t.\t.\tName=chr5\n";
        text += "chr6\tchop\tchromosome\t0\t170740541\t.\t.\t.\tName=chr6\n";
        text += "chr7\tchop\tchromosome\t0\t158431299\t.\t.\t.\tName=chr7\n";
        text += "chr8\tchop\tchromosome\t0\t145908738\t.\t.\t.\tName=chr8\n";
        text += "chr9\tchop\tchromosome\t0\t134505819\t.\t.\t.\tName=chr9\n";
        text += "chr10\tchop\tchromosome\t0\t135480874\t.\t.\t.\tName=chr10\n";
        text += "chr11\tchop\tchromosome\t0\t134978784\t.\t.\t.\tName=chr11\n";
        text += "chr12\tchop\tchromosome\t0\t133464434\t.\t.\t.\tName=chr12\n";
        text += "chr13\tchop\tchromosome\t0\t114151656\t.\t.\t.\tName=chr13\n";
        text += "chr14\tchop\tchromosome\t0\t105311216\t.\t.\t.\tName=chr14\n";
        text += "chr15\tchop\tchromosome\t0\t100114055\t.\t.\t.\tName=chr15\n";
        text += "chr16\tchop\tchromosome\t0\t89995999\t.\t.\t.\tName=chr16\n";
        text += "chr17\tchop\tchromosome\t0\t81691216\t.\t.\t.\tName=chr17\n";
        text += "chr18\tchop\tchromosome\t0\t77753510\t.\t.\t.\tName=chr18\n";
        text += "chr19\tchop\tchromosome\t0\t63790860\t.\t.\t.\tName=chr19\n";
        text += "chr20\tchop\tchromosome\t0\t63644868\t.\t.\t.\tName=chr20\n";
        text += "chr21\tchop\tchromosome\t0\t46976537\t.\t.\t.\tName=chr21\n";
        text += "chr22\tchop\tchromosome\t0\t49476972\t.\t.\t.\tName=chr22\n";
        text += "chrX\tchop\tchromosome\t0\t152634166\t.\t.\t.\tName=chrX\n";
        text += "chrY\tchop\tchromosome\t0\t50961097\t.\t.\t.\tName=chrY\n";
        printToFile(text);
    }

    protected void appendChopNormalCnvs() {
        String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + "cnv2_hg" + build + "?autoReconnect=true";
        boolean err = false;
        try {
            conn = connect(dbUser, dbPassword, url);
            String query = "select chr, start, end, type, isUnique from cnv";
            prestmt = conn.prepareStatement(query);
            resultSet = prestmt.executeQuery();
            int rowCount = 0;
            while (resultSet.next()) {
                String line = "chr" + resultSet.getString("chr") + "\tchop\t";
                if (resultSet.getString("isUnique").equals("1")) {
                    line += "uniqueCnv";
                } else {
                    line += "nonUniqueCnv";
                }
                line += "\t" + resultSet.getString("start") + "\t" + resultSet.getString("end") + "\t.\t.\t.\tID=" + id + ";Note=" + resultSet.getString("type") + "\n";
                printToFile(line);
                rowCount++;
                id++;
            }
            System.out.println(rowCount + " Chop Normal CNVs appended to " + outputFile + ".");
        } catch (Exception e) {
            System.err.println("There was a problem appending Chop Normal CNVs.  " + e.getMessage());
            err = true;
        } finally {
            try {
                close(conn, prestmt, resultSet);
            } catch (Exception e) {
                System.err.println("There was a problem closing JDBC objects.");
                err = true;
            }
        }
        if (err) System.exit(1);
    }

    protected void appendChopNormalCnvrs() {
        String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + "cnv2_hg" + build + "?autoReconnect=true";
        boolean err = false;
        try {
            conn = connect(dbUser, dbPassword, url);
            String query = "select chr, start, end from cnvr";
            prestmt = conn.prepareStatement(query);
            resultSet = prestmt.executeQuery();
            int rowCount = 0;
            while (resultSet.next()) {
                String line = "chr" + resultSet.getString("chr") + "\tchop\tcnvr\t" + resultSet.getString("start") + "\t" + resultSet.getString("end") + "\t.\t.\t.\t.\n";
                printToFile(line);
                rowCount++;
            }
            System.out.println(rowCount + " Chop Normal CNVRs appended to " + outputFile + ".");
        } catch (Exception e) {
            System.err.println("There was a problem appending Chop Normal CNVRs.  " + e.getMessage());
            err = true;
        } finally {
            try {
                close(conn, prestmt, resultSet);
            } catch (Exception e) {
                System.err.println("There was a problem closing JDBC objects.");
                err = true;
            }
        }
        if (err) System.exit(1);
    }

    protected void appendChopNormalCnvBlocks() {
        String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + "cnv2_hg" + build + "?autoReconnect=true";
        boolean err = false;
        try {
            conn = connect(dbUser, dbPassword, url);
            String query = "select chr, start, end, frequency from cnvblock";
            prestmt = conn.prepareStatement(query);
            resultSet = prestmt.executeQuery();
            int rowCount = 0;
            while (resultSet.next()) {
                String line = "chr" + resultSet.getString("chr") + "\tchop\tcnvblock\t" + resultSet.getString("start") + "\t" + resultSet.getString("end") + "\t" + resultSet.getString("frequency") + "\t.\t.\t" + "cnvblock chr" + resultSet.getString("chr") + ":chop\n";
                printToFile(line);
                rowCount++;
            }
            System.out.println(rowCount + " Chop Normal CNV Blocks appended to " + outputFile + ".");
        } catch (Exception e) {
            System.err.println("There was a problem appending Chop Normal CNV Blocks.  " + e.getMessage());
            err = true;
        } finally {
            try {
                close(conn, prestmt, resultSet);
            } catch (Exception e) {
                System.err.println("There was a problem closing JDBC objects.");
                err = true;
            }
        }
        if (err) System.exit(1);
    }

    protected void appendCustomTrack(String dataset) {
        String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + "cnvCalls?autoReconnect=true";
        boolean err = false;
        try {
            conn = connect(dbUser, dbPassword, url);
            String query = "select chrom, loc_start, loc_end from cnvCalls where data_set ='" + dataset + "'";
            prestmt = conn.prepareStatement(query);
            resultSet = prestmt.executeQuery();
            int rowCount = 0;
            while (resultSet.next()) {
                String line = "chr" + resultSet.getString("chrom") + "\t" + dataset + "\tcnv\t" + resultSet.getString("loc_start") + "\t" + resultSet.getString("loc_end") + "\t.\t.\t.\tID=" + id + ";\n";
                printToFile(line);
                rowCount++;
                id++;
            }
            System.out.println(rowCount + " " + dataset + " CNVs appended to " + outputFile + ".");
        } catch (Exception e) {
            System.err.println("There was a problem appending " + dataset + " CNVs.  " + e.getMessage());
            err = true;
        } finally {
            try {
                close(conn, prestmt, resultSet);
            } catch (Exception e) {
                System.err.println("There was a problem closing JDBC objects.");
                err = true;
            }
        }
        if (err) System.exit(1);
    }

    protected void appendAllCustomTracks() {
        String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + "cnvCalls?autoReconnect=true";
        boolean err = false;
        String currentDataset = "";
        try {
            conn = connect(dbUser, dbPassword, url);
            String query = "select distinct(data_set) from cnvCalls";
            prestmt = conn.prepareStatement(query);
            resultSet = prestmt.executeQuery();
            int rowCount = 0;
            while (resultSet.next()) {
                currentDataset = resultSet.getString("data_set");
                appendCustomTrack(currentDataset);
            }
            System.out.println("All custom track CNVs have been appended to " + outputFile + ".");
        } catch (Exception e) {
            System.err.println("There was a problem appending " + currentDataset + " CNVs.  " + e.getMessage());
            err = true;
        } finally {
            try {
                close(conn, prestmt, resultSet);
            } catch (Exception e) {
                System.err.println("There was a problem closing JDBC objects.");
                err = true;
            }
        }
        if (err) System.exit(1);
    }

    protected void appendTorontoCnvs() {
        String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + "cnvCalls?autoReconnect=true";
        boolean err = false;
        try {
            conn = connect(dbUser, dbPassword, url);
            String query = "select concat('chr',Chr), Reference, Start, End from torontoDB";
            prestmt = conn.prepareStatement(query);
            resultSet = prestmt.executeQuery();
            int rowCount = 0;
            while (resultSet.next()) {
                String source = resultSet.getString("Reference");
                if (source.indexOf(" ") > 0) {
                    source = source.substring(0, source.indexOf(" ")).toLowerCase();
                }
                String line = resultSet.getString("Chr") + "\ttoronto\tcnv\t" + resultSet.getString("Start") + "\t" + resultSet.getString("End") + "\t.\t.\t.\tID=" + id + ";Note=" + source + "\n";
                printToFile(line);
                rowCount++;
                id++;
            }
            System.out.println(rowCount + " Toronto CNVs appended to " + outputFile + ".");
        } catch (Exception e) {
            System.err.println("There was a problem appending Toronto CNVs.  " + e.getMessage());
            err = true;
        } finally {
            try {
                close(conn, prestmt, resultSet);
            } catch (Exception e) {
                System.err.println("There was a problem closing JDBC objects.");
                err = true;
            }
        }
        if (err) System.exit(1);
    }

    protected void appendGadPhenotypes() {
        String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/cnvCalls?autoReconnect=true";
        boolean err = false;
        try {
            conn = connect(dbUser, dbPassword, url);
            String query = "select Broad_Phenotype, Chromosom, DNA_Start, DNA_End from gad";
            prestmt = conn.prepareStatement(query);
            resultSet = prestmt.executeQuery();
            int rowCount = 0;
            while (resultSet.next()) {
                if (resultSet.getString("Broad_Phenotype") != null && resultSet.getString("Chromosom") != null && resultSet.getString("DNA_Start") != null && resultSet.getString("DNA_End") != null) {
                    String line = "chr" + resultSet.getString("Chromosom") + "\tgad\tphenotype\t" + resultSet.getString("DNA_Start") + "\t" + resultSet.getString("DNA_End") + "\t.\t.\t.\t";
                    String phenotypes = resultSet.getString("Broad_Phenotype").replaceAll(";", ",");
                    if (phenotypes == null || phenotypes.equals("")) {
                        phenotypes = " ";
                    }
                    line += "Name=" + phenotypes + ";Note=" + phenotypes;
                    line += "\n";
                    printToFile(line);
                    rowCount++;
                }
            }
            System.out.println(rowCount + " GAD phenotypes appended to " + outputFile + ".");
        } catch (Exception e) {
            System.err.println("There was a problem appending GAD phenotypes.  " + e.getMessage());
            err = true;
        } finally {
            try {
                close(conn, prestmt, resultSet);
            } catch (Exception e) {
                System.err.println("There was a problem closing JDBC objects.");
                err = true;
            }
        }
        if (err) System.exit(1);
    }

    protected void appendUcscGenes() {
        String url = "jdbc:mysql://genome-mysql.cse.ucsc.edu/hg" + build + "?autoReconnect=true";
        Connection conn2 = null;
        PreparedStatement prestmt2 = null;
        ResultSet resultSet2 = null;
        boolean err = false;
        try {
            conn = connect("genome", "", url);
            String name = "name";
            if (build.equals("18")) {
                name = "name2";
            }
            String query = "select " + name + ", chrom, strand, txStart, txEnd from refGene";
            prestmt = conn.prepareStatement(query);
            resultSet = prestmt.executeQuery();
            int rowCount = 0;
            String url2 = "jdbc:mysql://" + dbHost + ":" + dbPort + "/cnvCalls?autoReconnect=true";
            conn2 = connect(dbUser, dbPassword, url2);
            while (resultSet.next()) {
                String gene = resultSet.getString(name);
                String chromosome = resultSet.getString("chrom");
                if (chromosome.indexOf("_") > 0) {
                    chromosome = chromosome.substring(0, chromosome.indexOf("_"));
                }
                String start = resultSet.getString("txStart");
                String end = resultSet.getString("txEnd");
                String line = chromosome + "\tucsc\t";
                String color = "";
                try {
                    String query2 = "select ucsc_color from cnvCalls.cnvCalls where chrom = '" + chromosome + "'";
                    query2 += " and loc_start <= '" + end + "' and loc_end >= '" + start + "'";
                    prestmt2 = conn2.prepareStatement(query2);
                    resultSet2 = prestmt2.executeQuery();
                    while (resultSet2.next()) {
                        color = resultSet2.getString("ucsc_color");
                    }
                } catch (Exception e) {
                    System.err.println("There was a problem getting cnvCalls.cnvCalls: " + e.getMessage());
                    err = true;
                } finally {
                    try {
                        prestmt2.close();
                        resultSet2.close();
                    } catch (Exception e) {
                        System.err.println("There was a problem closing statement and/or result set: " + e.getMessage());
                        err = true;
                    }
                }
                if (err) System.exit(1);
                String type = "gene";
                if (!color.equals("")) {
                    type += color;
                }
                line += type;
                line += "\t" + resultSet.getString("txStart") + "\t" + resultSet.getString("txEnd") + "\t.\t" + resultSet.getString("strand") + "\t.\t";
                if (gene == null || gene.equals("")) {
                    gene = " ";
                }
                line += "Name=" + gene + ";";
                line += "Note=" + gene + "\n";
                printToFile(line);
                rowCount++;
            }
            System.out.println(rowCount + " UCSC genes appended to " + outputFile + ".");
        } catch (Exception e) {
            System.err.println("There was a problem appending UCSC genes.  " + e.getMessage());
            err = true;
        } finally {
            try {
                conn2.close();
                close(conn, prestmt, resultSet);
            } catch (Exception e) {
                System.err.println("There was a problem closing JDBC objects.");
                err = true;
            }
        }
        if (err) System.exit(1);
    }

    public void appendUcscBands() {
        String url = "jdbc:mysql://genome-mysql.cse.ucsc.edu/hg" + build + "?autoReconnect=true";
        boolean err = false;
        try {
            conn = connect("genome", "", url);
            String name = "name";
            if (build.equals("18")) {
                name = "name2";
            }
            String query = "select chrom, chromStart, chromEnd, name from cytoBand";
            prestmt = conn.prepareStatement(query);
            resultSet = prestmt.executeQuery();
            int rowCount = 0;
            while (resultSet.next()) {
                String chrom = resultSet.getString("chrom");
                String band = chrom.substring(3, chrom.length()) + resultSet.getString("name");
                String line = chrom + "\tucsc\tband\t" + resultSet.getString("chromStart") + "\t" + resultSet.getString("chromEnd") + "\t.\t.\t.\tName=" + band + ";" + "Note=" + band + "\n";
                printToFile(line);
                rowCount++;
            }
            System.out.println(rowCount + " UCSC bands appended to " + outputFile + ".");
        } catch (Exception e) {
            System.err.println("There was a problem appending UCSC bands.  " + e.getMessage());
            err = true;
        } finally {
            try {
                close(conn, prestmt, resultSet);
            } catch (Exception e) {
                System.err.println("There was a problem closing JDBC objects.");
                err = true;
            }
        }
        if (err) System.exit(1);
    }

    public void printToFile(String line) {
        BufferedWriter out = null;
        boolean err = false;
        try {
            out = new BufferedWriter(new FileWriter(outputFile, true));
            out.write(line);
        } catch (IOException e) {
            System.err.println("Unable to write to " + outputFile + ".");
            err = true;
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                System.err.println("Unable to close BufferedWriter.");
                err = true;
            }
        }
        if (err) System.exit(1);
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getBuild() {
        return build;
    }

    public Connection connect(String userName, String password, String url) {
        Connection connection = null;
        boolean err = false;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception e) {
            System.err.println("Error loading MySQL driver: " + e.getMessage());
            err = true;
        }
        if (!err) {
            try {
                connection = DriverManager.getConnection(url, userName, password);
                System.out.println("Database connection established.");
            } catch (SQLException e) {
                System.err.println(String.format("Unable to connect to database server using url '%s', user '%s', and password '%s': msg is: %s", url, userName, password, e.getMessage()));
                System.err.println("SQLState: " + e.getSQLState());
                System.err.println("VendorError: " + e.getErrorCode());
                err = true;
            }
        }
        if (err) System.exit(1);
        return connection;
    }

    public void close(Connection connection, PreparedStatement prestmt, ResultSet resultSet) {
        boolean err = false;
        try {
            if (resultSet != null) {
                resultSet.close();
                System.out.println("Result set closed.");
            }
            if (prestmt != null) {
                prestmt.close();
                System.out.println("Prepared statement closed.");
            }
            if (connection != null) {
                connection.close();
                System.out.println("Connection closed.");
            }
        } catch (Exception e) {
            System.out.println("Unable to close jdbc objects.");
            err = true;
        }
        if (err) System.exit(1);
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public String getDataset() {
        return dataset;
    }
}
