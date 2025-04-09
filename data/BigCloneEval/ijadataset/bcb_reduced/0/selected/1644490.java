package com.kaixinff.kaixin001.xw;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import com.kaixinff.kaixin001.util.Filter;
import com.kaixinff.kaixin001.util.KXClient;
import com.kaixinff.kaixin001.util.KXUtil;
import com.kaixinff.kaixin001.util.RegStep;
import com.kaixinff.name.User;

public class Gift extends Thread {

    private boolean startNow;

    private String name = "Shengdan";

    private HttpServletResponse resp;

    public Gift(HttpServletResponse resp, boolean startNow) {
        this.resp = resp;
        this.startNow = startNow;
    }

    public synchronized void run() {
        if (!startNow) {
            long t = getTomorrow() - System.currentTimeMillis() + 120000;
            System.out.println("wait " + t);
            try {
                Thread.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            String url = KXUtil.HOST_URL + "/!spiderman/inventory.php?item=collection&cat=festa";
            String referer = KXUtil.HOST_URL + "/!spiderman/inventory.php?item=collection";
            KXClient client = KXClient.getInstance(KXUtil.getMainUserEmail());
            String html = client.doGet(url, referer).getContent();
            Pattern p = Pattern.compile("<li id=\"co_" + name + "(\\d+?)_Col\" num=\"(\\d+?)\" giftnum=\"(\\d+?)\">(.+?)<br>");
            Matcher m = p.matcher(html);
            int[] flags = new int[7];
            String[] ids = new String[7];
            String[] names = new String[7];
            int i = 0;
            while (m.find()) {
                ids[i] = name + m.group(1);
                flags[i] = Integer.parseInt(m.group(2));
                names[i] = m.group(4);
                i++;
            }
            List<User> users = KXUtil.getUsers(new Filter() {

                @Override
                public boolean access(User user) {
                    return user.hasStep(RegStep.ADD_XW);
                }
            }, resp);
            int index = 0;
            flags = new int[] { 999, 999, 52, 8, 999, 999, 999 };
            for (User user : users) {
                while (flags[index] > 0) {
                    flags[index]--;
                    index = (index + 1) % 7;
                }
                KXClient c = KXClient.getInstance(user.getId());
                String r = c.doGet(url, referer).getContent();
                p = Pattern.compile("id=\"co_" + ids[index] + "_Col\" num=\"(\\d+?)\"");
                m = p.matcher(r);
                if (m.find()) {
                    int num = Integer.parseInt(m.group(1));
                    if (num == 0) {
                        continue;
                    }
                } else {
                    continue;
                }
                int result = gift(c, ids[index], names[index]);
                if (result == 1) {
                    index = (index + 1) % 7;
                } else if (result == -1) {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long getTomorrow() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String prefix = sdf.format(new Date());
        try {
            Date d = sdf.parse(prefix);
            return d.getTime() + 86400000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis() + 86400000;
    }

    private int gift(KXClient client, String id, String name) throws IOException {
        String url = KXUtil.HOST_URL + "/!spiderman/!ajax_gift.php?wkey=" + id + "_Col&type=collection&num=1&msg=&uid=" + KXUtil.getMainUserUid() + "&flag=sendgift&cid=2&tmp=" + Math.random() + "&rt=json";
        String html = client.doGet(url, null).getContent();
        if (html.indexOf("\"flag\":\"suc\"") > -1) {
            System.out.println("赠送：" + name);
            return 1;
        } else if (html.indexOf("\\u4e0d\\u80fd\\u518d\\u6536\\u793c\\u7269\\u4e86") > -1) {
            System.out.println("赠送结束！");
            return -1;
        }
        return 0;
    }
}
