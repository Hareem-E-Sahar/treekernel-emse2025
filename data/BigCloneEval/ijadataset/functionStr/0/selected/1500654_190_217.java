public class Test {    public static String getUserNetwork(String User) {
        URL url;
        String line, finalstring;
        StringBuffer buffer = new StringBuffer();
        try {
            thread = Thread.currentThread().getName();
            int t = Integer.parseInt(thread);
            System.out.println("WebMoleThread " + t + " using proxy: " + proxy[t]);
            url = new URL(proxy[t] + JSONusernetwork + User + JSONusernetworkappend);
            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
            connect.addRequestProperty("User-Agent", userAgent);
            System.out.println("moling: network of " + User);
            BufferedReader input = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }
            input.close();
            connect.disconnect();
            finalstring = buffer.toString();
            return finalstring;
        } catch (MalformedURLException e) {
            System.err.println("Bad URL: " + e);
            return null;
        } catch (IOException io) {
            System.err.println("IOException: " + io);
            return null;
        }
    }
}