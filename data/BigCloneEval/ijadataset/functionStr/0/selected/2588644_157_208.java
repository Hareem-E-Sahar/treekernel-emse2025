public class Test {    public void getToken(String realname, String service, String time, String pseudonym) throws IOException {
        URL m1url = new URL(issuer, "message1?realname=" + realname + "&service=" + service + "&time=" + time);
        System.out.println("---->");
        System.out.println("Request: " + m1url);
        BufferedReader br = new BufferedReader(new InputStreamReader(m1url.openConnection().getInputStream()));
        String num = br.readLine();
        String tokenInformation = br.readLine();
        String message1 = br.readLine();
        br.close();
        System.out.println("<----");
        System.out.println(num);
        System.out.println(tokenInformation);
        System.out.println(message1);
        byte[][] attributes = buildAttributes(realname, service, time);
        ProverProtocolParameters proverProtocolParams = new ProverProtocolParameters();
        proverProtocolParams.setIssuerParameters(ip);
        proverProtocolParams.setNumberOfTokens(1);
        proverProtocolParams.setTokenAttributes(attributes);
        proverProtocolParams.setTokenInformation(convert(tokenInformation));
        proverProtocolParams.setProverInformation(pseudonym.getBytes("UTF-8"));
        com.microsoft.uprove.Prover prover = proverProtocolParams.generate();
        byte[][] message2 = prover.generateSecondMessage(convertArray(message1));
        String msg2str = num + "\n" + convert(message2);
        URL m3url = new URL(issuer, "message3");
        System.out.println("---->");
        System.out.println("Request: " + m3url);
        System.out.println(msg2str);
        HttpURLConnection conn = (HttpURLConnection) m3url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        PrintWriter pw = new PrintWriter(conn.getOutputStream());
        pw.print(msg2str);
        pw.flush();
        BufferedReader br3 = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String message3 = br3.readLine();
        conn.disconnect();
        System.out.println("<----");
        System.out.println(message3);
        UProveKeyAndToken[] upkt = prover.generateTokens(convertArray(message3));
        System.out.println(upkt.length + " token generated:");
        System.out.println("IssuerParametersUID: " + new String(upkt[0].getToken().getIssuerParametersUID()));
        System.out.println("TokenInformation: " + new String(upkt[0].getToken().getTokenInformation()));
        System.out.println("ProverInformation: " + new String(upkt[0].getToken().getProverInformation()));
        System.out.println("Token Private Key: " + convert(upkt[0].getTokenPrivateKey()));
        System.out.println("Token Public Key: " + convert(upkt[0].getToken().getPublicKey()));
        System.out.println("SigmaC: " + convert(upkt[0].getToken().getSigmaC()));
        System.out.println("SigmaR: " + convert(upkt[0].getToken().getSigmaR()));
        System.out.println("SigmaZ: " + convert(upkt[0].getToken().getSigmaZ()));
        for (int i = 0; i < upkt.length; i++) {
            new TokenUI(upkt[i], realname, service, time, ip);
        }
    }
}