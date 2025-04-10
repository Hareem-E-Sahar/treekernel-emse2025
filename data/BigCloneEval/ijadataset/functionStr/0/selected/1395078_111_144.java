public class Test {    public static String obterConteudoSite(String u, String encode, Map<String, String> parametros) {
        URL url;
        try {
            StringBuilder strParams = new StringBuilder();
            if (parametros != null) {
                for (String chave : parametros.keySet()) {
                    strParams.append(URLEncoder.encode(chave, "UTF-8"));
                    strParams.append("=");
                    strParams.append(URLEncoder.encode(parametros.get(chave), encode));
                    strParams.append("&");
                }
            }
            url = new URL(u);
            URLConnection conn = null;
            if (proxy != null) conn = url.openConnection(proxy.getProxy()); else conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(strParams.toString());
            wr.flush();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName(encode)));
            String line;
            StringBuilder resultado = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                resultado.append(line);
            }
            wr.close();
            rd.close();
            return resultado.toString();
        } catch (MalformedURLException e) {
            throw new AlfredException("Não foi possível obter contato com o site " + u, e);
        } catch (IOException e) {
            throw new AlfredException("Não foi possível obter contato com o site " + u, e);
        }
    }
}