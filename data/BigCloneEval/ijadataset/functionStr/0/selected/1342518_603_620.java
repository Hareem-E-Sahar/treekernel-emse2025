public class Test {    private String deleteData(String id, DefaultHttpClient httpclient) {
        String responseMessage = "Error";
        try {
            HttpDelete del = new HttpDelete("http://3dforandroid.appspot.com/api/v2/delete/" + SQLiteBackup.Kind.getSimpleName() + "/" + id);
            del.setHeader("Content-Type", "application/json");
            del.setHeader("Accept", "*/*");
            HttpResponse response = httpclient.execute(del);
            HttpEntity entity = response.getEntity();
            InputStream instream;
            instream = entity.getContent();
            responseMessage = read(instream);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseMessage;
    }
}