import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class Utility {

    public static BufferedReader getKongUserInfoStream(String name) throws IOException {
        BufferedReader in;
        try {
            URL url = new URL("http://www.kongregate.com/accounts/" + name.toLowerCase());
            in = new BufferedReader(new InputStreamReader(url.openStream()));
        } catch (MalformedURLException e) {
            in = null;
            throw e;
        }
        return in;
    }

    public static BufferedReader getKongUserBadgeStream(String name, String sort) throws IOException {
        BufferedReader in;
        try {
            URL url = new URL("http://www.kongregate.com/accounts/" + name.toLowerCase() + "/badges?sort=" + sort);
            in = new BufferedReader(new InputStreamReader(url.openStream()));
        } catch (MalformedURLException e) {
            in = null;
            throw e;
        }
        return in;
    }

    public static BufferedReader getKongBadgesStream(String category) throws IOException {
        BufferedReader in;
        try {
            URL url = new URL("http://www.kongregate.com/badges" + category);
            in = new BufferedReader(new InputStreamReader(url.openStream()));
        } catch (MalformedURLException e) {
            in = null;
            throw e;
        }
        return in;
    }
}
