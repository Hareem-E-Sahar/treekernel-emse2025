package com.carey.renren.example;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.TreeMap;
import com.carey.renren.RenRenHttpClient;
import com.carey.renren.RenRenOAuth;
import com.carey.renren.utils.RenRenHttpUtil;

public class RenRenMain {

    public static void main(String[] args) {
        renrenRequest();
    }

    private static void renrenRequest() {
        if (!java.awt.Desktop.isDesktopSupported()) {
            System.err.println("Desktop is not supported (fatal)");
            System.exit(1);
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if (desktop == null || !desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            System.err.println("Desktop doesn't support the browse action");
            System.exit(1);
        }
        testAPI();
    }

    private static void getAuthorizationCode(java.awt.Desktop desktop) {
        System.out.println("get Authorization Code......");
        try {
            String urlStr = RenRenOAuth.AuthorizationURL + "?" + "client_id=" + RenRenOAuth.APIKey + "&" + "response_type=code" + "&redirect_uri=" + RenRenOAuth.RedirectURL;
            System.out.println("authorization url: \n" + urlStr);
            desktop.browse(new URI(urlStr));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getAccessToken(java.awt.Desktop desktop) {
        System.out.println("get Access Token......");
        System.out.println("Input your Authorization code：");
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        try {
            String urlStr = RenRenOAuth.OAuthURL + "?" + "client_id=" + RenRenOAuth.APIKey + "&client_secret=" + RenRenOAuth.SecretKey + "&redirect_uri=" + RenRenOAuth.RedirectURL + "&grant_type=authorization_code" + "&code=" + input;
            System.out.println("access url: \n" + urlStr);
            desktop.browse(new URI(urlStr));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getSessionToken(java.awt.Desktop desktop) {
        System.out.println("get Session Token......");
        System.out.println("Input your Access token：");
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        try {
            input = URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            String urlStr = RenRenOAuth.SessionURL + "?" + "oauth_token=" + input;
            System.out.println("session url: \n" + urlStr);
            desktop.browse(new URI(urlStr));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testAPI() {
        System.out.println("Input your Session Token：");
        String input = "5.57d2ad88cdd22398c03a2fcbb3bbd229.86400.1306422000-1167553647";
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("session_key", input);
        params.put("method", "users.getInfo");
        params.put("uids", "224547745");
        String content = sendPostRestRequest(params, "JSON", RenRenOAuth.ApiUrl);
        if (content.indexOf("error_code") >= 0) {
        }
        System.out.println(content);
    }

    public static String sendPostRestRequest(TreeMap<String, String> params, String format, String url) {
        RenRenHttpUtil.prepareParams(params, format);
        String content = RenRenHttpClient.doPost(url, params);
        return content;
    }
}
