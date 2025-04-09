import java.io.*;
import java.util.*;

public class QueryGenerator {

    private static String query_generate = "/home/adima/client/generate_query.sh";

    private static String update_generate = "/home/adima/client/generate_updates.sh";

    private static String update_path = "/home/adima/dbgen/";

    public static String preAllQ() {
        String result = "", temp;
        for (int i = 1; i <= 22; i++) {
            temp = preQ(i);
            if (temp != null) {
                result += "\n" + temp;
            }
        }
        return result;
    }

    public static String postAllQ() {
        String result = "", temp;
        for (int i = 1; i <= 22; i++) {
            temp = postQ(i);
            if (temp != null) {
                result += "\n" + temp;
            }
        }
        return result;
    }

    public static String Q(int number) {
        switch(number) {
            case 1:
                return Q1();
            case 2:
                return Q2();
            case 3:
                return Q3();
            case 4:
                return Q4();
            case 5:
                return Q5();
            case 6:
                return Q6();
            case 7:
                return Q7();
            case 8:
                return Q8();
            case 9:
                return Q9();
            case 10:
                return Q10();
            case 11:
                return Q11();
            case 12:
                return Q12();
            case 13:
                return Q13();
            case 14:
                return Q14();
            case 15:
                return Q15();
            case 16:
                return Q16();
            case 17:
                return Q17();
            case 18:
                return Q18();
            case 19:
                return Q19();
            case 20:
                return Q20();
            case 21:
                return Q21();
            case 22:
                return Q22();
        }
        return "";
    }

    public static ArrayList<String> GetUpdatePair(int myUpdateNumber) {
        String orderstbl = update_path + "orders.tbl.u" + myUpdateNumber;
        String lineitemtbl = update_path + "lineitem.tbl.u" + myUpdateNumber;
        String deletetbl = update_path + "delete." + myUpdateNumber;
        ArrayList<String> result = new ArrayList<String>();
        try {
            BufferedReader orders = new BufferedReader(new FileReader(new File(orderstbl)));
            BufferedReader lineitem = new BufferedReader(new FileReader(new File(lineitemtbl)));
            BufferedReader delete = new BufferedReader(new FileReader(new File(deletetbl)));
            String order_line = null;
            String lineitem_line = null;
            String[] order_pieces;
            String order_key;
            String[] lineitem_pieces;
            String query;
            while ((order_line = orders.readLine()) != null) {
                order_pieces = order_line.split("\\|");
                order_key = order_pieces[0];
                query = "INSERT INTO orders VALUES ('" + order_pieces[0] + "'";
                for (int i = 1; i < 9; i++) {
                    query += ", '" + order_pieces[i] + "'";
                }
                query += ");\n";
                if (lineitem_line == null) {
                    lineitem_line = lineitem.readLine();
                }
                lineitem_pieces = lineitem_line.split("\\|");
                while (lineitem_line != null && lineitem_pieces[0].equals(order_key)) {
                    query += "INSERT INTO lineitem VALUES ('" + lineitem_pieces[0] + "'";
                    for (int i = 1; i < 16; i++) {
                        query += ", '" + lineitem_pieces[i] + "'";
                    }
                    query += ");\n";
                    lineitem_line = lineitem.readLine();
                    if (lineitem_line != null) {
                        lineitem_pieces = lineitem_line.split("\\|");
                    }
                }
                result.add(query);
            }
            String delete_line = null;
            while ((delete_line = delete.readLine()) != null) {
                order_key = delete_line.split("\\|")[0];
                query = "DELETE FROM orders WHERE o_orderkey = " + order_key + ";\n";
                query += "DELETE FROM lineitem WHERE l_orderkey = " + order_key + ";\n";
                result.add(query);
            }
        } catch (FileNotFoundException e) {
            System.err.println(orderstbl + " not found! or...");
            System.err.println(lineitemtbl + " not found! or...");
            System.err.println(deletetbl + " not found!");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.err.println("I/O exception when using update " + myUpdateNumber);
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public static void GenerateUpdates(int clientsCount) {
        dbgenUpdate(clientsCount);
    }

    private static String qgenQuery(int number) {
        String result = "";
        try {
            result = myRunCommand(query_generate + " " + number);
        } catch (IOException e) {
            System.err.println("Exception generating query " + number + ": ");
            e.printStackTrace();
        }
        return result;
    }

    private static String dbgenUpdate(int clientsCount) {
        String result = "";
        try {
            result = myRunCommand(update_generate + " " + clientsCount);
        } catch (IOException e) {
            System.err.println("Exception generating updates for " + clientsCount + " clients:");
            e.printStackTrace();
        }
        return result;
    }

    private static String preQ(int number) {
        switch(number) {
            case 15:
                return preQ15();
        }
        return null;
    }

    private static String postQ(int number) {
        switch(number) {
            case 15:
                return postQ15();
        }
        return null;
    }

    private static String Q1() {
        String result = qgenQuery(1);
        result = result.replaceAll("\' day \\(3\\)", " day'");
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String Q2() {
        String result = qgenQuery(2);
        result = result.replaceAll("set rowcount 100\\ngo", "");
        return result;
    }

    private static String Q3() {
        String result = qgenQuery(3);
        result = result.replaceAll("set rowcount 10\\ngo", "");
        return result;
    }

    private static String Q4() {
        String result = qgenQuery(4);
        result = result.replaceAll("\\+ interval \\'3\\' month", "+ interval '3 month'");
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String Q5() {
        String result = qgenQuery(5);
        result = result.replaceAll("\\+ interval \\'1\\' year", "+ interval '1 year'");
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String Q6() {
        String result = qgenQuery(6);
        result = result.replaceAll("\\+ interval \\'1\\' year", "+ interval '1 year'");
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String Q7() {
        String result = qgenQuery(7);
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String Q8() {
        String result = qgenQuery(8);
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String Q9() {
        String result = qgenQuery(9);
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String Q10() {
        String result = qgenQuery(10);
        result = result.replaceAll("\\+ interval \\'3\\' month", "+ interval '3 month'");
        result = result.replaceAll("set rowcount 20\\ngo", "");
        return result;
    }

    private static String Q11() {
        String result = qgenQuery(11);
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String Q12() {
        String result = qgenQuery(12);
        result = result.replaceAll("\\+ interval \\'1\\' year", "+ interval '1 year'");
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String Q13() {
        String result = qgenQuery(13);
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String Q14() {
        String result = qgenQuery(14);
        result = result.replaceAll("\\+ interval \\'1\\' month", "+ interval '1 month'");
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String _Q15() {
        String result = qgenQuery(15);
        result = result.replaceAll("\\+ interval \\'3\\' month", "+ interval '3 month'");
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String preQ15() {
        String result = _Q15();
        return result.split(";")[0] + ";";
    }

    private static String Q15() {
        String result = _Q15();
        return result.split(";")[1] + ";";
    }

    private static String postQ15() {
        String result = _Q15();
        return result.split(";")[2] + ";";
    }

    private static String Q16() {
        String result = qgenQuery(16);
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String Q17() {
        String result = qgenQuery(17);
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String Q18() {
        String result = qgenQuery(18);
        result = result.replaceAll("set rowcount 100\\ngo", "");
        return result;
    }

    private static String Q19() {
        String result = qgenQuery(19);
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String Q20() {
        String result = qgenQuery(20);
        result = result.replaceAll("set rowcount -1\\ngo", "");
        result = result.replaceAll("\\+ interval \\'1\\' year", "+ interval '1 year'");
        return result;
    }

    private static String Q21() {
        String result = qgenQuery(21);
        result = result.replaceAll("set rowcount 100\\ngo", "");
        return result;
    }

    private static String Q22() {
        String result = qgenQuery(22);
        result = result.replaceAll("set rowcount -1\\ngo", "");
        return result;
    }

    private static String myRunCommand(String cmd) throws IOException {
        String result = "";
        Process proc = Runtime.getRuntime().exec(cmd);
        InputStream istr = proc.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(istr));
        String str;
        while ((str = br.readLine()) != null) {
            result += "\n" + str;
        }
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            System.err.println("Process was interrupted");
        }
        br.close();
        return result;
    }
}
