package image;

import java.io.*;
import java.net.*;
import java.util.regex.*;

/**
 *
 * @author zhanshi
 */
public class YahooImageFetcher extends ImageFetcher {

    /** Creates a new instance of YahooImageFetcher */
    public YahooImageFetcher() {
        imageFetcherName = "Yahoo Images";
    }

    public String getImageURL(String text) {
        String imgURL = "";
        try {
            URL url = new URL("http://images.search.yahoo.com/search/images?p=" + URLEncoder.encode(text));
            URLConnection connection = url.openConnection();
            DataInputStream in = new DataInputStream(connection.getInputStream());
            String line;
            Pattern imgPattern = Pattern.compile("isrc=\"([^\"]*)\"");
            while ((line = in.readLine()) != null) {
                Matcher match = imgPattern.matcher(line);
                if (match.find()) {
                    imgURL = match.group(1);
                    break;
                }
            }
            in.close();
        } catch (Exception e) {
        }
        return imgURL;
    }
}
