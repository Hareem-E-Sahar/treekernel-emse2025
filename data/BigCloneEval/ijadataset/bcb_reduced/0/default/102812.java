import org.apache.xmlrpc.Base64;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

public class BlogPoster {

    public static String xmlrpcNewMediaObject(String url, String login, String password, String filename, String mimeType, String file) throws Exception {
        url = url + "xmlrpc.php?";
        final XmlRpcClient xmlrpc = new XmlRpcClient(url);
        final Vector params = new Vector(0);
        final InputStream is = new FileInputStream(file);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        final BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
        final byte[] bytes = new byte[1024];
        int len = 0;
        while ((len = bufferedInputStream.read(bytes)) > 0) {
            byteArrayOutputStream.write(bytes, 0, len);
        }
        is.close();
        bufferedInputStream.close();
        String blogID = "1";
        params.add(blogID);
        params.add(login);
        params.add(password);
        final Hashtable media = new Hashtable(0);
        media.put("name", filename);
        media.put("type", mimeType);
        media.put("bits", byteArrayOutputStream.toByteArray());
        params.add(media);
        final Hashtable response = (Hashtable) xmlrpc.execute("metaWeblog.newMediaObject", params);
        return ((String) response.get("url"));
    }

    /**
	 *
	 */
    public static String xmlrpcNewPost(String url, String login, String password, String title, String description, String bPublish) throws Exception {
        String postid = new String("unknown");
        try {
            url = url + "xmlrpc.php?";
            final XmlRpcClient xmlrpc = new XmlRpcClient(url);
            final Vector params = new Vector(0);
            String blogID = "1";
            params.add(blogID);
            params.add(login);
            params.add(password);
            final Hashtable blogEntry = new Hashtable(0);
            blogEntry.put("title", title);
            blogEntry.put("description", description);
            params.add(blogEntry);
            params.add(Boolean.valueOf(false));
            postid = (String) xmlrpc.execute("metaWeblog.newPost", params);
        } catch (XmlRpcException xex) {
            System.out.println("XmlRpc exception:");
            System.out.println(xex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return (postid);
    }

    /**
	 *
	 */
    public static String xmlrpcEditPost(String url, String postid, String login, String password, String title, String description, String bPublish) throws Exception {
        try {
            url = url + "xmlrpc.php?";
            final XmlRpcClient xmlrpc = new XmlRpcClient(url);
            final Vector params = new Vector(0);
            params.add(postid);
            params.add(login);
            params.add(password);
            final Hashtable blogEntry = new Hashtable(0);
            blogEntry.put("title", title);
            blogEntry.put("description", description);
            params.add(blogEntry);
            params.add(Boolean.valueOf(false));
            postid = (String) xmlrpc.execute("metaWeblog.editPost", params);
        } catch (XmlRpcException xex) {
            System.out.println("XmlRpc exception:");
            System.out.println(xex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return (postid);
    }

    public static void main(String[] args) {
        String output = null;
        try {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(output);
    }
}
