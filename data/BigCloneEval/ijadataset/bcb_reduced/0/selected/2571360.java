package com.l2jserver.loginserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import com.l2jserver.Base64;
import com.l2jserver.Config;
import com.l2jserver.L2DatabaseFactory;
import com.l2jserver.loginserver.GameServerTable.GameServerInfo;
import com.l2jserver.loginserver.gameserverpackets.ServerStatus;
import com.l2jserver.loginserver.serverpackets.LoginFail.LoginFailReason;
import com.l2jserver.util.Rnd;
import com.l2jserver.util.crypt.ScrambledKeyPair;
import com.l2jserver.util.lib.Log;
import javolution.util.FastMap;

/**
 * This class ...
 *
 * @version $Revision: 1.7.4.3 $ $Date: 2005/03/27 15:30:09 $
 */
public class LoginController {

    protected static final Logger _log = Logger.getLogger(LoginController.class.getName());

    private static LoginController _instance;

    /** Time before kicking the client if he didnt logged yet */
    public static final int LOGIN_TIMEOUT = 60 * 1000;

    /** Authed Clients on LoginServer*/
    protected FastMap<String, L2LoginClient> _loginServerClients = new FastMap<String, L2LoginClient>().shared();

    private Map<String, BanInfo> _bannedIps = new FastMap<String, BanInfo>().shared();

    private Map<InetAddress, FailedLoginAttempt> _hackProtection;

    protected ScrambledKeyPair[] _keyPairs;

    private Thread _purge;

    protected byte[][] _blowfishKeys;

    private static final int BLOWFISH_KEYS = 20;

    public static void load() throws GeneralSecurityException {
        synchronized (LoginController.class) {
            if (_instance == null) {
                _instance = new LoginController();
            } else {
                throw new IllegalStateException("LoginController can only be loaded a single time.");
            }
        }
    }

    public static LoginController getInstance() {
        return _instance;
    }

    private LoginController() throws GeneralSecurityException {
        _log.info("Loading LoginController...");
        _hackProtection = new FastMap<InetAddress, FailedLoginAttempt>();
        _keyPairs = new ScrambledKeyPair[10];
        KeyPairGenerator keygen = null;
        keygen = KeyPairGenerator.getInstance("RSA");
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
        keygen.initialize(spec);
        for (int i = 0; i < 10; i++) {
            _keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
        }
        _log.info("Cached 10 KeyPairs for RSA communication");
        testCipher((RSAPrivateKey) _keyPairs[0]._pair.getPrivate());
        generateBlowFishKeys();
        _purge = new PurgeThread();
        _purge.setDaemon(true);
        _purge.start();
    }

    /**
	 * This is mostly to force the initialization of the Crypto Implementation, avoiding it being done on runtime when its first needed.<BR>
	 * In short it avoids the worst-case execution time on runtime by doing it on loading.
	 * @param key Any private RSA Key just for testing purposes.
	 * @throws GeneralSecurityException if a underlying exception was thrown by the Cipher
	 */
    private void testCipher(RSAPrivateKey key) throws GeneralSecurityException {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
        rsaCipher.init(Cipher.DECRYPT_MODE, key);
    }

    private void generateBlowFishKeys() {
        _blowfishKeys = new byte[BLOWFISH_KEYS][16];
        for (int i = 0; i < BLOWFISH_KEYS; i++) {
            for (int j = 0; j < _blowfishKeys[i].length; j++) {
                _blowfishKeys[i][j] = (byte) (Rnd.nextInt(255) + 1);
            }
        }
        _log.info("Stored " + _blowfishKeys.length + " keys for Blowfish communication");
    }

    /**
	 * @return Returns a random key
	 */
    public byte[] getBlowfishKey() {
        return _blowfishKeys[(int) (Math.random() * BLOWFISH_KEYS)];
    }

    public SessionKey assignSessionKeyToClient(String account, L2LoginClient client) {
        SessionKey key;
        key = new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt());
        _loginServerClients.put(account, client);
        return key;
    }

    public void removeAuthedLoginClient(String account) {
        if (account == null) return;
        _loginServerClients.remove(account);
    }

    public boolean isAccountInLoginServer(String account) {
        return _loginServerClients.containsKey(account);
    }

    public L2LoginClient getAuthedClient(String account) {
        return _loginServerClients.get(account);
    }

    public static enum AuthLoginResult {

        INVALID_PASSWORD, ACCOUNT_BANNED, ALREADY_ON_LS, ALREADY_ON_GS, AUTH_SUCCESS
    }

    public AuthLoginResult tryAuthLogin(String account, String password, L2LoginClient client) throws HackingException {
        AuthLoginResult ret = AuthLoginResult.INVALID_PASSWORD;
        if (loginValid(account, password, client)) {
            ret = AuthLoginResult.ALREADY_ON_GS;
            if (!isAccountInAnyGameServer(account)) {
                ret = AuthLoginResult.ALREADY_ON_LS;
                synchronized (_loginServerClients) {
                    if (!_loginServerClients.containsKey(account)) {
                        _loginServerClients.put(account, client);
                        ret = AuthLoginResult.AUTH_SUCCESS;
                    }
                }
            }
        } else {
            if (client.getAccessLevel() < 0) {
                ret = AuthLoginResult.ACCOUNT_BANNED;
            }
        }
        return ret;
    }

    /**
	 * Adds the address to the ban list of the login server, with the given duration.
	 *
	 * @param address The Address to be banned.
	 * @param expiration Timestamp in miliseconds when this ban expires
	 * @throws UnknownHostException if the address is invalid.
	 */
    public void addBanForAddress(String address, long expiration) throws UnknownHostException {
        InetAddress netAddress = InetAddress.getByName(address);
        if (!_bannedIps.containsKey(netAddress.getHostAddress())) _bannedIps.put(netAddress.getHostAddress(), new BanInfo(netAddress, expiration));
    }

    /**
	 * Adds the address to the ban list of the login server, with the given duration.
	 *
	 * @param address The Address to be banned.
	 * @param duration is miliseconds
	 */
    public void addBanForAddress(InetAddress address, long duration) {
        if (!_bannedIps.containsKey(address.getHostAddress())) _bannedIps.put(address.getHostAddress(), new BanInfo(address, System.currentTimeMillis() + duration));
    }

    public boolean isBannedAddress(InetAddress address) {
        String[] parts = address.getHostAddress().split("\\.");
        BanInfo bi = _bannedIps.get(address.getHostAddress());
        if (bi == null) bi = _bannedIps.get(parts[0] + "." + parts[1] + "." + parts[2] + ".0");
        if (bi == null) bi = _bannedIps.get(parts[0] + "." + parts[1] + ".0.0");
        if (bi == null) bi = _bannedIps.get(parts[0] + ".0.0.0");
        if (bi != null) {
            if (bi.hasExpired()) {
                _bannedIps.remove(address.getHostAddress());
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public Map<String, BanInfo> getBannedIps() {
        return _bannedIps;
    }

    /**
	 * Remove the specified address from the ban list
	 * @param address The address to be removed from the ban list
	 * @return true if the ban was removed, false if there was no ban for this ip
	 */
    public boolean removeBanForAddress(InetAddress address) {
        return _bannedIps.remove(address.getHostAddress()) != null;
    }

    /**
	 * Remove the specified address from the ban list
	 * @param address The address to be removed from the ban list
	 * @return true if the ban was removed, false if there was no ban for this ip or the address was invalid.
	 */
    public boolean removeBanForAddress(String address) {
        try {
            return this.removeBanForAddress(InetAddress.getByName(address));
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public SessionKey getKeyForAccount(String account) {
        L2LoginClient client = _loginServerClients.get(account);
        if (client != null) {
            return client.getSessionKey();
        }
        return null;
    }

    public int getOnlinePlayerCount(int serverId) {
        GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
        if (gsi != null && gsi.isAuthed()) {
            return gsi.getCurrentPlayerCount();
        }
        return 0;
    }

    public boolean isAccountInAnyGameServer(String account) {
        Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
        for (GameServerInfo gsi : serverList) {
            GameServerThread gst = gsi.getGameServerThread();
            if (gst != null && gst.hasAccountOnGameServer(account)) {
                return true;
            }
        }
        return false;
    }

    public GameServerInfo getAccountOnGameServer(String account) {
        Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
        for (GameServerInfo gsi : serverList) {
            GameServerThread gst = gsi.getGameServerThread();
            if (gst != null && gst.hasAccountOnGameServer(account)) {
                return gsi;
            }
        }
        return null;
    }

    public int getTotalOnlinePlayerCount() {
        int total = 0;
        Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
        for (GameServerInfo gsi : serverList) {
            if (gsi.isAuthed()) {
                total += gsi.getCurrentPlayerCount();
            }
        }
        return total;
    }

    public int getMaxAllowedOnlinePlayers(int id) {
        GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(id);
        if (gsi != null) {
            return gsi.getMaxPlayers();
        }
        return 0;
    }

    /**
	 *
	 * @return
	 */
    public boolean isLoginPossible(L2LoginClient client, int serverId) {
        GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
        int access = client.getAccessLevel();
        if (gsi != null && gsi.isAuthed()) {
            boolean loginOk = (gsi.getCurrentPlayerCount() < gsi.getMaxPlayers() && gsi.getStatus() != ServerStatus.STATUS_GM_ONLY) || access > 0;
            if (loginOk && client.getLastServer() != serverId) {
                Connection con = null;
                PreparedStatement statement = null;
                try {
                    con = L2DatabaseFactory.getInstance().getConnection();
                    String stmt = "UPDATE accounts SET lastServer = ? WHERE login = ?";
                    statement = con.prepareStatement(stmt);
                    statement.setInt(1, serverId);
                    statement.setString(2, client.getAccount());
                    statement.executeUpdate();
                    statement.close();
                } catch (Exception e) {
                    _log.log(Level.WARNING, "Could not set lastServer: " + e.getMessage(), e);
                } finally {
                    L2DatabaseFactory.close(con);
                }
            }
            return loginOk;
        }
        return false;
    }

    public void setAccountAccessLevel(String account, int banLevel) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = L2DatabaseFactory.getInstance().getConnection();
            String stmt = "UPDATE accounts SET accessLevel=? WHERE login=?";
            statement = con.prepareStatement(stmt);
            statement.setInt(1, banLevel);
            statement.setString(2, account);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            _log.log(Level.WARNING, "Could not set accessLevel: " + e.getMessage(), e);
        } finally {
            try {
                L2DatabaseFactory.close(con);
            } catch (Exception e) {
            }
        }
    }

    public void setAccountLastTracert(String account, String pcIp, String hop1, String hop2, String hop3, String hop4) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = L2DatabaseFactory.getInstance().getConnection();
            String stmt = "UPDATE accounts SET pcIp=?, hop1=?, hop2=?, hop3=?, hop4=? WHERE login=?";
            statement = con.prepareStatement(stmt);
            statement.setString(1, pcIp);
            statement.setString(2, hop1);
            statement.setString(3, hop2);
            statement.setString(4, hop3);
            statement.setString(5, hop4);
            statement.setString(6, account);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            _log.log(Level.WARNING, "Could not set last tracert: " + e.getMessage(), e);
        } finally {
            try {
                L2DatabaseFactory.close(con);
            } catch (Exception e) {
            }
        }
    }

    public boolean isGM(String user) {
        boolean ok = false;
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT accessLevel FROM accounts WHERE login=?");
            statement.setString(1, user);
            ResultSet rset = statement.executeQuery();
            if (rset.next()) {
                int accessLevel = rset.getInt(1);
                if (accessLevel > 0) {
                    ok = true;
                }
            }
            rset.close();
            statement.close();
        } catch (Exception e) {
            _log.log(Level.WARNING, "Could not check gm state:" + e.getMessage(), e);
            ok = false;
        } finally {
            try {
                L2DatabaseFactory.close(con);
            } catch (Exception e) {
            }
        }
        return ok;
    }

    /**
	 * <p>This method returns one of the cached {@link ScrambledKeyPair ScrambledKeyPairs} for communication with Login Clients.</p>
	 * @return a scrambled keypair
	 */
    public ScrambledKeyPair getScrambledRSAKeyPair() {
        return _keyPairs[Rnd.nextInt(10)];
    }

    /**
	 * user name is not case sensitive any more
	 * @param user
	 * @param password
	 * @param address
	 * @return
	 */
    public boolean loginValid(String user, String password, L2LoginClient client) {
        boolean ok = false;
        InetAddress address = client.getConnection().getInetAddress();
        if (address == null || user == null) {
            return false;
        }
        Connection con = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] raw = password.getBytes("UTF-8");
            byte[] hash = md.digest(raw);
            byte[] expected = null;
            int access = 0;
            int lastServer = 1;
            String userIP = null;
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT password, accessLevel, lastServer, userIP FROM accounts WHERE login=?");
            statement.setString(1, user);
            ResultSet rset = statement.executeQuery();
            if (rset.next()) {
                expected = Base64.decode(rset.getString("password"));
                access = rset.getInt("accessLevel");
                lastServer = rset.getInt("lastServer");
                userIP = rset.getString("userIP");
                if (lastServer <= 0) lastServer = 1;
                if (Config.DEBUG) _log.fine("account exists");
            }
            rset.close();
            statement.close();
            if (expected == null) {
                if (Config.AUTO_CREATE_ACCOUNTS) {
                    if ((user.length() >= 2) && (user.length() <= 14)) {
                        statement = con.prepareStatement("INSERT INTO accounts (login,password,lastactive,accessLevel,lastIP) values(?,?,?,?,?)");
                        statement.setString(1, user);
                        statement.setString(2, Base64.encodeBytes(hash));
                        statement.setLong(3, System.currentTimeMillis());
                        statement.setInt(4, 0);
                        statement.setString(5, address.getHostAddress());
                        statement.execute();
                        statement.close();
                        if (Config.LOG_LOGIN_CONTROLLER) Log.add("'" + user + "' " + address.getHostAddress() + " - OK : AccountCreate", "loginlog");
                        _log.info("Created new account for " + user);
                        return true;
                    }
                    if (Config.LOG_LOGIN_CONTROLLER) Log.add("'" + user + "' " + address.getHostAddress() + " - ERR : ErrCreatingACC", "loginlog");
                    _log.warning("Invalid username creation/use attempt: " + user);
                    return false;
                } else {
                    if (Config.LOG_LOGIN_CONTROLLER) Log.add("'" + user + "' " + address.getHostAddress() + " - ERR : AccountMissing", "loginlog");
                    _log.warning("Account missing for user " + user);
                    FailedLoginAttempt failedAttempt = _hackProtection.get(address);
                    int failedCount;
                    if (failedAttempt == null) {
                        _hackProtection.put(address, new FailedLoginAttempt(address, password));
                        failedCount = 1;
                    } else {
                        failedAttempt.increaseCounter();
                        failedCount = failedAttempt.getCount();
                    }
                    if (failedCount >= Config.LOGIN_TRY_BEFORE_BAN) {
                        _log.info("Banning '" + address.getHostAddress() + "' for " + Config.LOGIN_BLOCK_AFTER_BAN + " seconds due to " + failedCount + " invalid user name attempts");
                        this.addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
                    }
                    return false;
                }
            } else {
                if (access < 0) {
                    if (Config.LOG_LOGIN_CONTROLLER) Log.add("'" + user + "' " + address.getHostAddress() + " - ERR : AccountBanned", "loginlog");
                    client.setAccessLevel(access);
                    return false;
                }
                if (userIP != null && !address.getHostAddress().equalsIgnoreCase(userIP)) {
                    if (Config.LOG_LOGIN_CONTROLLER) Log.add("'" + user + "' " + address.getHostAddress() + "/" + userIP + " - ERR : INCORRECT IP", "loginlog");
                    return false;
                }
                ok = true;
                for (int i = 0; i < expected.length; i++) {
                    if (hash[i] != expected[i]) {
                        ok = false;
                        break;
                    }
                }
            }
            if (ok) {
                client.setAccessLevel(access);
                client.setLastServer(lastServer);
                statement = con.prepareStatement("UPDATE accounts SET lastactive=?, lastIP=? WHERE login=?");
                statement.setLong(1, System.currentTimeMillis());
                statement.setString(2, address.getHostAddress());
                statement.setString(3, user);
                statement.execute();
                statement.close();
            }
        } catch (Exception e) {
            _log.log(Level.WARNING, "Could not check password:" + e.getMessage(), e);
            ok = false;
        } finally {
            L2DatabaseFactory.close(con);
        }
        if (!ok) {
            if (Config.LOG_LOGIN_CONTROLLER) Log.add("'" + user + "' " + address.getHostAddress() + " - ERR : LoginFailed", "loginlog");
            FailedLoginAttempt failedAttempt = _hackProtection.get(address);
            int failedCount;
            if (failedAttempt == null) {
                _hackProtection.put(address, new FailedLoginAttempt(address, password));
                failedCount = 1;
            } else {
                failedAttempt.increaseCounter(password);
                failedCount = failedAttempt.getCount();
            }
            if (failedCount >= Config.LOGIN_TRY_BEFORE_BAN) {
                _log.info("Banning '" + address.getHostAddress() + "' for " + Config.LOGIN_BLOCK_AFTER_BAN + " seconds due to " + failedCount + " invalid user/pass attempts");
                this.addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
            }
        } else {
            _hackProtection.remove(address);
            if (Config.LOG_LOGIN_CONTROLLER) Log.add("'" + user + "' " + address.getHostAddress() + " - OK : LoginOk", "loginlog");
        }
        return ok;
    }

    public boolean loginBanned(String user) {
        boolean ok = false;
        Connection con = null;
        try {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT accessLevel FROM accounts WHERE login=?");
            statement.setString(1, user);
            ResultSet rset = statement.executeQuery();
            if (rset.next()) {
                int accessLevel = rset.getInt(1);
                if (accessLevel < 0) ok = true;
            }
            rset.close();
            statement.close();
        } catch (Exception e) {
            _log.log(Level.WARNING, "Could not check ban state:" + e.getMessage(), e);
            ok = false;
        } finally {
            try {
                L2DatabaseFactory.close(con);
            } catch (Exception e) {
            }
        }
        return ok;
    }

    class FailedLoginAttempt {

        private int _count;

        private long _lastAttempTime;

        private String _lastPassword;

        public FailedLoginAttempt(InetAddress address, String lastPassword) {
            _count = 1;
            _lastAttempTime = System.currentTimeMillis();
            _lastPassword = lastPassword;
        }

        public void increaseCounter(String password) {
            if (!_lastPassword.equals(password)) {
                if (System.currentTimeMillis() - _lastAttempTime < 300 * 1000) {
                    _count++;
                } else {
                    _count = 1;
                }
                _lastPassword = password;
                _lastAttempTime = System.currentTimeMillis();
            } else {
                _lastAttempTime = System.currentTimeMillis();
            }
        }

        public int getCount() {
            return _count;
        }

        public void increaseCounter() {
            _count++;
        }
    }

    class BanInfo {

        private InetAddress _ipAddress;

        private long _expiration;

        public BanInfo(InetAddress ipAddress, long expiration) {
            _ipAddress = ipAddress;
            _expiration = expiration;
        }

        public InetAddress getAddress() {
            return _ipAddress;
        }

        public boolean hasExpired() {
            return System.currentTimeMillis() > _expiration && _expiration > 0;
        }
    }

    class PurgeThread extends Thread {

        public PurgeThread() {
            setName("PurgeThread");
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                synchronized (_loginServerClients) {
                    for (FastMap.Entry<String, L2LoginClient> e = _loginServerClients.head(), end = _loginServerClients.tail(); (e = e.getNext()) != end; ) {
                        L2LoginClient client = e.getValue();
                        if ((client.getConnectionStartTime() + LOGIN_TIMEOUT) < System.currentTimeMillis()) {
                            client.close(LoginFailReason.REASON_ACCESS_FAILED);
                        }
                    }
                }
                try {
                    Thread.sleep(LOGIN_TIMEOUT / 2);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}
