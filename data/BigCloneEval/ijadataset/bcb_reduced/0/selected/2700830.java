package jaapy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.*;
import javax.servlet.http.*;
import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.Blip;
import com.google.wave.api.Element;
import com.google.wave.api.ElementType;
import com.google.wave.api.Event;
import com.google.wave.api.EventType;
import com.google.wave.api.Gadget;
import com.google.wave.api.GadgetView;
import com.google.wave.api.Range;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;

public class Jaap_yServlet extends AbstractRobotServlet {

    @Override
    public void processEvents(RobotMessageBundle bundle) {
        if (bundle.wasSelfAdded()) {
        }
        for (Event e : bundle.getEvents()) {
            if (e.getType() == EventType.BLIP_SUBMITTED) {
                Process(e.getBlip().getDocument());
            }
        }
    }

    private void Process(TextView document) {
        Pattern re = Pattern.compile("(http://)?(www.)?jaap.nl/koophuis/[^\\s]+/[^\\s]+/(\\d{1,10})/[^\\s]+");
        Matcher ma = re.matcher(document.getText());
        List<UrlMatch> list = new ArrayList<UrlMatch>();
        while (ma.find()) {
            if (ma.groupCount() > 2) {
                UrlMatch u = new UrlMatch();
                u.propertyID = Integer.parseInt(ma.group(3));
                u.start = ma.start();
                u.end = ma.end();
                list.add(u);
            }
        }
        Collections.reverse(list);
        for (UrlMatch u : list) {
            String gadgetUrl = "http://jaap-y.appspot.com/servlet/gadget.xml?id=" + u.propertyID.toString();
            document.delete(new Range(u.start, u.end));
            Gadget gadget = new Gadget(gadgetUrl);
            document.insertElement(u.start, gadget);
        }
    }
}
