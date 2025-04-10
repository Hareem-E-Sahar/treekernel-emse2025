public class Test {    public void _testDoJobAndCollectMoneyFairview() throws Exception {
        HttpURLConnection connection = null;
        File docSerializeMoney = new File("C:\\TIC_LOCAL\\EclipseProjects\\OpenSourceRacsor\\cc_request\\jmeter\\pickupItemMoney.binary");
        File docSerializeCollection = new File("C:\\TIC_LOCAL\\EclipseProjects\\OpenSourceRacsor\\cc_request\\jmeter\\pickupItemCollection.binary");
        File doc = new File("C:\\TIC_LOCAL\\EclipseProjects\\OpenSourceRacsor\\cc_request\\jmeter\\doJob_car_Fairview.binary");
        byte[] theFile = FileUtils.readFileToByteArray(doc);
        String urlString = "http://ct-google.crimecitygame.com/ct-google/index.php/amf_gateway?f=104995124568475809013&r=43&t=none";
        String contentType = "application/x-amf";
        URL url = new URL(urlString);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Content-Type", contentType);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.write(theFile);
        wr.flush();
        wr.close();
        System.out.println("responsecode:" + connection.getResponseCode());
        InputStream is = connection.getInputStream();
        UtilsFlexMessage utilsFlex = new UtilsFlexMessage();
        utilsFlex.parseInputStream(is);
        String content = utilsFlex.messageToXML();
        System.out.println(content);
        String pickupItem = "pickup_item:" + StringUtils.substringBetween(content, "<string>pickup_item:", "</string>");
        System.out.println(pickupItem);
        String oldPickupItem = "pickup_item:276698290864898721:917:dn34ppe0ireh";
        theFile = FileUtils.readFileToByteArray(docSerializeMoney);
        ByteArrayInputStream bais = new ByteArrayInputStream(theFile);
        DataInputStream din = new DataInputStream(bais);
        utilsFlex = new UtilsFlexMessage();
        utilsFlex.parseInputStream(din);
        String update = utilsFlex.updateFlexMessage(oldPickupItem, pickupItem);
        byte[] newdataPickupItem = utilsFlex.serializeMessage(update);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Content-Type", contentType);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        wr = new DataOutputStream(connection.getOutputStream());
        wr.write(newdataPickupItem);
        wr.flush();
        wr.close();
        System.out.println("responsecode:" + connection.getResponseCode());
        int temp = StringUtils.indexOf(content, "<string>collection_loot_item_id</string>\n                  <null/>");
        System.out.println(temp);
        if (temp != -1) {
            theFile = FileUtils.readFileToByteArray(docSerializeCollection);
            bais = new ByteArrayInputStream(theFile);
            din = new DataInputStream(bais);
            utilsFlex = new UtilsFlexMessage();
            utilsFlex.parseInputStream(din);
            update = utilsFlex.updateFlexMessage(oldPickupItem, pickupItem);
            newdataPickupItem = utilsFlex.serializeMessage(update);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Content-Type", contentType);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            wr = new DataOutputStream(connection.getOutputStream());
            wr.write(newdataPickupItem);
            wr.flush();
            wr.close();
            System.out.println("responsecode:" + connection.getResponseCode());
        }
    }
}