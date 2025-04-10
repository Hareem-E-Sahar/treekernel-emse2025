public class Test {        public void connect(DXEntry entry) {
            if (debug) System.out.println("connect: " + entry);
            if (myOps != null) {
                try {
                    myOps.close();
                } catch (NamingException e) {
                    System.err.println("exception closing ops");
                    e.printStackTrace();
                }
            }
            String url = "";
            if (myUrl != null) url = myUrl; else url = entry.getString("url");
            String user = entry.getString("user");
            String pwd = entry.getString("pwd");
            String tracing = entry.getString("tracing");
            String version = entry.getString("ldapVersion");
            String referral = entry.getString("referral");
            String useSSL = entry.getString("useSSL");
            boolean trace = ((tracing != null) && (tracing.equalsIgnoreCase("true")));
            boolean ssl = ((useSSL != null) && (useSSL.equalsIgnoreCase("true")));
            openConnection(url, user, pwd, trace, version, referral, ssl);
        }
}