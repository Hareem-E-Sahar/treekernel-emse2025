package org.opencraft.server.model;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import org.opencraft.server.Configuration;
import org.opencraft.server.Constants;
import org.opencraft.server.game.GameMode;
import org.opencraft.server.heartbeat.HeartbeatManager;
import org.opencraft.server.io.LevelGzipper;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.persistence.SavedGameManager;
import org.opencraft.server.persistence.SavePersistenceRequest;
import org.opencraft.server.util.PlayerList;

/**
 * Manages the in-game world.
 * @author Graham Edgecombe
 */
public final class World {

    /**
	 * The singleton instance.
	 */
    private static final World INSTANCE;

    /**
	 * Logger instance.
	 */
    private static final Logger logger = Logger.getLogger(World.class.getName());

    /**
	 * Static initializer.
	 */
    static {
        World w = null;
        try {
            w = new World();
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
        INSTANCE = w;
    }

    /**
	 * Gets the world instance.
	 * @return The world instance.
	 */
    public static World getWorld() {
        return INSTANCE;
    }

    /**
	 * The level.
	 */
    private final Level level = new Level();

    /**
	 * The player list.
	 */
    private final PlayerList playerList = new PlayerList();

    /**
	 * The game mode.
	 */
    private GameMode gameMode;

    /**
	 * Default private constructor.
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
    private World() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        gameMode = (GameMode) Class.forName(Configuration.getConfiguration().getGameMode()).newInstance();
        logger.info("Active game mode : " + gameMode.getClass().getName() + ".");
    }

    /**
	 * Gets the current game mode.
	 * @return The current game mode.
	 */
    public GameMode getGameMode() {
        return gameMode;
    }

    /**
	 * Gets the player list.
	 * @return The player list.
	 */
    public PlayerList getPlayerList() {
        return playerList;
    }

    /**
	 * Gets the level.
	 * @return The level.
	 */
    public Level getLevel() {
        return level;
    }

    /**
	 * Registers a session.
	 * @param session The session.
	 * @param username The username.
	 * @param verificationKey The verification key.
	 */
    public void register(MinecraftSession session, String username, String verificationKey) {
        if (Configuration.getConfiguration().isVerifyingNames()) {
            long salt = HeartbeatManager.getHeartbeatManager().getSalt();
            String hash = new StringBuilder().append(String.valueOf(salt)).append(username).toString();
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("No MD5 algorithm!");
            }
            digest.update(hash.getBytes());
            if (!verificationKey.equals(new BigInteger(1, digest.digest()).toString(16))) {
                session.getActionSender().sendLoginFailure("Illegal name.");
                return;
            }
        }
        char[] nameChars = username.toCharArray();
        for (char nameChar : nameChars) {
            if (nameChar < ' ' || nameChar > '\177') {
                session.getActionSender().sendLoginFailure("Invalid name!");
                return;
            }
        }
        for (Player p : playerList.getPlayers()) {
            if (p.getName().equalsIgnoreCase(username)) {
                p.getSession().getActionSender().sendLoginFailure("Logged in from another computer.");
                break;
            }
        }
        final Player player = new Player(session, username);
        if (!playerList.add(player)) {
            player.getSession().getActionSender().sendLoginFailure("Too many players online!");
            return;
        }
        session.setPlayer(player);
        final Configuration c = Configuration.getConfiguration();
        session.getActionSender().sendLoginResponse(Constants.PROTOCOL_VERSION, c.getName(), c.getMessage(), false);
        LevelGzipper.getLevelGzipper().gzipLevel(session);
    }

    /**
	 * Unregisters a session.
	 * @param session The session.
	 */
    public void unregister(MinecraftSession session) {
        if (session.isAuthenticated()) {
            playerList.remove(session.getPlayer());
            World.getWorld().getGameMode().playerDisconnected(session.getPlayer());
            SavedGameManager.getSavedGameManager().queuePersistenceRequest(new SavePersistenceRequest(session.getPlayer()));
            session.setPlayer(null);
        }
    }

    /**
	 * Completes registration of a session.
	 * @param session The session.
	 */
    public void completeRegistration(MinecraftSession session) {
        if (!session.isAuthenticated()) {
            session.close();
            return;
        }
        session.getActionSender().sendChatMessage("Welcome to OpenCraft!");
        World.getWorld().getGameMode().playerConnected(session.getPlayer());
    }

    /**
	 * Broadcasts a chat message.
	 * @param player The source player.
	 * @param message The message.
	 */
    public void broadcast(Player player, String message) {
        for (Player otherPlayer : playerList.getPlayers()) {
            otherPlayer.getSession().getActionSender().sendChatMessage(player.getId(), message);
        }
    }

    /**
	 * Broadcasts a server message.
	 * @param message The message.
	 */
    public void broadcast(String message) {
        for (Player player : playerList.getPlayers()) {
            player.getSession().getActionSender().sendChatMessage(message);
        }
    }
}
