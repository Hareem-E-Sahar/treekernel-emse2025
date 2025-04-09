package com.winiex.weibospider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import weibo4j.Status;
import weibo4j.Weibo;
import weibo4j.WeiboException;
import weibo4j.http.AccessToken;
import weibo4j.http.RequestToken;

public class Spider {

    public static void main(String[] args) {
        OAuth mOAuth = new OAuth();
        try {
            Weibo weibo = new Weibo();
            weibo.setToken(OAuth.accessToken, OAuth.accessTokenSecret);
            List<Status> statuses = weibo.getPublicTimeline();
            for (Status status : statuses) {
                System.out.println(status.getUser().getName() + ":" + status.getText() + ":" + status.getCreatedAt());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showBrowser(String tokenKey) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            System.err.println("Desktop is not supported (fatal)");
            System.exit(1);
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if (desktop == null || !desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            System.err.println("Desktop doesn't support the browse action (fatal)");
            System.exit(1);
        }
        try {
            desktop.browse(new URI("http://api.t.sina.com.cn/oauth/authorize?oauth_token=" + tokenKey));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
