public class Test {    public static void main(String[] args) {
        if (args.length <= 0) {
            System.out.println(" *** SQL script generator and executor ***");
            System.out.println(" You must specify name of the file with SQL script data");
            System.out.println(" Fisrt rows of this file must be:");
            System.out.println(" 1) JDBC driver class for your DBMS");
            System.out.println(" 2) URL for your database instance");
            System.out.println(" 3) user in that database (with administrator priviliges)");
            System.out.println(" 4) password of that user");
            System.out.println(" Next rows can have: '@' before schema to create,");
            System.out.println("   '#' before table to create, '&' before table to insert,");
            System.out.println("   '$' before trigger (inverse 'FK on delete cascade') to create,");
            System.out.println("   '>' before table to drop, '<' before schema to drop.");
            System.out.println(" Other rows contain parameters of these actions:");
            System.out.println("   for & action each parameter is a list of values,");
            System.out.println("   for @ -//- is # acrion, for # -//- is column/constraint ");
            System.out.println("    definition or $ action. $ syntax to delete from table:");
            System.out.println("    fullNameOfTable:itsColInWhereClause=matchingColOfThisTable");
            System.out.println(" '!' before row means that it is a comment.");
            System.out.println(" If some exception is occured, all script is rolled back.");
            System.out.println(" If you specify 2nd command line argument - file name too -");
            System.out.println("   connection will be established but all statements will");
            System.out.println("   be saved in that output file and not transmitted to DB");
            System.out.println(" If you specify 3nd command line argument - connect_string -");
            System.out.println("   connect information will be added to output file");
            System.out.println("   in the form 'connect user/password@connect_string'");
            System.exit(0);
        }
        try {
            String[] info = new String[4];
            BufferedReader reader = new BufferedReader(new FileReader(new File(args[0])));
            Writer writer = null;
            try {
                for (int i = 0; i < 4; i++) info[i] = reader.readLine();
                try {
                    Class.forName(info[0]);
                    Connection connection = DriverManager.getConnection(info[1], info[2], info[3]);
                    SQLScript script = new SQLScript(connection);
                    if (args.length > 1) {
                        writer = new FileWriter(args[1]);
                        if (args.length > 2) writer.write("connect " + info[2] + "/" + info[3] + "@" + args[2] + script.statementTerminator);
                    }
                    try {
                        System.out.println(script.executeScript(reader, writer) + " updates has been performed during script execution");
                    } catch (SQLException e4) {
                        reader.close();
                        if (writer != null) writer.close();
                        System.out.println(" Script execution error: " + e4);
                    }
                    connection.close();
                } catch (Exception e3) {
                    reader.close();
                    if (writer != null) writer.close();
                    System.out.println(" Connection error: " + e3);
                }
            } catch (IOException e2) {
                System.out.println("Error in file " + args[0]);
            }
        } catch (FileNotFoundException e1) {
            System.out.println("File " + args[0] + " not found");
        }
    }
}