public class Test {    public static LinkedList<String> read(URL url, String exc) throws IOException {
        LinkedList<String> data = new LinkedList<String>();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String input = "";
        while (true) {
            input = br.readLine();
            if (input == null) break;
            if (!input.startsWith(exc)) {
                data.add(input);
            }
        }
        br.close();
        return data;
    }
}