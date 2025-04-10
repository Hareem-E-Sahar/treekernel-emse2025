package com.angis.fx.handler.login;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import com.angis.fx.util.DataParseUtil;

public class DeviceRegisterHandler {

    public static String register(String pDeviceno, String pDeviceid) {
        HttpClient lClient = new DefaultHttpClient();
        StringBuilder lBuilder = new StringBuilder();
        HttpPost lHttpPost = new HttpPost("http://122.224.201.230/ZJWHServiceTest/GIS_Duty.asmx/PDARegister");
        List<NameValuePair> lNameValuePairs = new ArrayList<NameValuePair>(2);
        lNameValuePairs.add(new BasicNameValuePair("deviceno", pDeviceno));
        lNameValuePairs.add(new BasicNameValuePair("deviceid", pDeviceid));
        try {
            lHttpPost.setEntity(new UrlEncodedFormEntity(lNameValuePairs));
            HttpResponse lResponse = lClient.execute(lHttpPost);
            BufferedReader lHeader = new BufferedReader(new InputStreamReader(lResponse.getEntity().getContent()));
            for (String s = lHeader.readLine(); s != null; s = lHeader.readLine()) {
                lBuilder.append(s);
            }
            return DataParseUtil.handleResponse(lBuilder.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
