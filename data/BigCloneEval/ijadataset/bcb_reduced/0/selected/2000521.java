package org.mineground.commands.irc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jibble.pircbot.Colors;
import org.jibble.pircbot.User;
import org.mineground.Main;
import org.mineground.handlers.irc.CommandExecutor;
import org.mineground.handlers.irc.UserLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @file LastBanned.java (21.02.2012)
 * @author Daniel Koenen
 *
 */
public class LastBanned implements CommandExecutor {

    private static final Logger ExceptionLogger = LoggerFactory.getLogger(LastBanned.class);

    @Override
    public void onCommand(User sender, UserLevel level, String channel, String command, String[] args) {
        if (level.compareTo(UserLevel.IRC_OP) < 0) {
            return;
        }
        try {
            String queryString = "SELECT p.login_name, b.reason, UNIX_TIMESTAMP(b.expiredate) FROM lvm_bans b LEFT JOIN lvm_players p ON p.player_id = b.player_id ORDER BY b.ban_id DESC LIMIT 5";
            PreparedStatement preparedStatement = Main.getInstance().getDatabaseHandler().getConnection().prepareStatement(queryString);
            preparedStatement.execute();
            ResultSet queryResult = preparedStatement.getResultSet();
            StringBuilder messageBuilder;
            SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEE, d MMMMM yyyy, HH:mm", Main.DEFAULT_LOCALE);
            String playerName;
            String banReason;
            long expireTime;
            Main.getInstance().getIRCHandler().sendMessage(channel, Colors.BROWN + "Last banned players:");
            while (queryResult.next()) {
                playerName = queryResult.getString(1);
                banReason = queryResult.getString(2);
                expireTime = queryResult.getLong(3);
                messageBuilder = new StringBuilder();
                messageBuilder.append(Colors.TEAL);
                messageBuilder.append("Name: ");
                messageBuilder.append(Colors.DARK_GREEN);
                messageBuilder.append(playerName);
                messageBuilder.append(Colors.TEAL);
                messageBuilder.append(" / Reason: ");
                messageBuilder.append(Colors.DARK_GREEN);
                messageBuilder.append(banReason);
                messageBuilder.append(Colors.TEAL);
                messageBuilder.append(" / Banned until: ");
                messageBuilder.append(Colors.DARK_GREEN);
                messageBuilder.append(dateFormatter.format(new Date(expireTime * 1000)));
                Main.getInstance().getIRCHandler().sendMessage(channel, messageBuilder.toString());
            }
            preparedStatement.close();
        } catch (Exception exception) {
            ExceptionLogger.error("Exception caught", exception);
        }
    }
}
