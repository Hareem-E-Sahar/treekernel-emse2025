package org.bing.engine.controller.helper;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import org.bing.engine.common.logging.Log;
import org.bing.engine.common.logging.LogFactory;
import org.bing.engine.utility.helper.ThreadHelper;

public class TemplateHelper2 {

    protected static final Log logger = LogFactory.getLog(TemplateHelper2.class);

    private static final Random random = new SecureRandom();

    public static String transform(String template, List<String> args) {
        String express = StringHelper.getProperty(template);
        while (express != null) {
            String res = "";
            if (express.indexOf(":") > 0) {
                String[] ss = express.split(":");
                res = calc(ss[1]);
                if (ss[0].equalsIgnoreCase("engine.port")) {
                    int k = 0;
                    while (true) {
                        k++;
                        if (!PortHelper.isAlive(res)) {
                            if (k % 10 == 0) {
                                logger.warn("Port is used, retry over " + k + " times! " + ss[1]);
                            }
                            break;
                        } else {
                            res = calc(ss[1]);
                            ThreadHelper.sleep(500);
                        }
                    }
                }
                args.add("-D" + ss[0] + "=" + res);
            } else {
                res = calc(express);
            }
            template = StringHelper.replaceWithProperty(template, express, res);
            express = StringHelper.getProperty(template);
        }
        return template;
    }

    private static String calc(String exp) {
        if (exp.startsWith("random")) {
            int beg = exp.indexOf("(");
            int end = exp.indexOf(")");
            String[] ss = exp.substring(beg + 1, end).split(",");
            int rb = Integer.parseInt(ss[0]);
            int re = Integer.parseInt(ss[1]);
            int rr = rb + random.nextInt(re - rb);
            return String.valueOf(rr);
        }
        return exp;
    }
}
