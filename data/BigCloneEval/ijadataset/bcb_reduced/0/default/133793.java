import java.io.IOException;
import java.net.DatagramSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;

public class Server {

    private static final String IP = "127.0.0.1";

    private static final int PORT = 5188;

    private static MainFrame m = null;

    private static MySocketController socketController;

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.startSocket();
    }

    public Server() throws IOException {
        m = new MainFrame();
        m.setTitle("Server");
        m.setContent_read("服务器启动........");
        m.setVisible(true);
    }

    public void startSocket() throws IOException {
        socketController = new MySocketController(new DatagramSocket(PORT));
        while (true) {
            try {
                String s = socketController.getContent();
                int port = socketController.getPacket().getPort();
                String ip = socketController.getPacket().getAddress().toString();
                JSONObject request = new JSONObject(s);
                if (request.get("operation").equals("login")) {
                    if (checkUserInfo(request.getString("username"), request.getString("password"))) {
                        socketController.setContent("true", port, ip.replace("/", ""));
                    } else {
                        socketController.setContent("false", port, ip.replace("/", ""));
                    }
                }
                continue;
            } catch (Exception e) {
                MainFrame.getError("用户验证信息传输错误！");
            }
        }
    }

    public Connection connectDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:angrypigs.db");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkUserInfo(String username, String password) {
        Connection conn = this.connectDB();
        try {
            ResultSet rs = conn.createStatement().executeQuery("select * from users where name = '" + username + "';");
            while (rs.next()) {
                if (rs.getString("password").equals(password)) {
                    m.setContent_read("用户： " + username + " 已登录, 他正在打小鸟.......");
                    return true;
                }
            }
            rs.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
