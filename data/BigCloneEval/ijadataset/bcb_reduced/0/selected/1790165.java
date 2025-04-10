package net.jetrix.commands;

import java.util.*;
import net.jetrix.*;
import net.jetrix.messages.*;
import net.jetrix.messages.channel.CommandMessage;
import net.jetrix.messages.channel.PlineMessage;

/**
 * Summon a player to the current channel.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 798 $, $Date: 2009-02-18 10:24:28 -0500 (Wed, 18 Feb 2009) $
 */
public class SummonCommand extends AbstractCommand implements ParameterCommand {

    public SummonCommand() {
        setAccessLevel(AccessLevel.OPERATOR);
    }

    public String getAlias() {
        return "summon";
    }

    public String getUsage(Locale locale) {
        return "/summon <" + Language.getText("command.params.player_name", locale) + ">";
    }

    public int getParameterCount() {
        return 1;
    }

    public void execute(CommandMessage m) {
        Client client = (Client) m.getSource();
        String targetName = m.getParameter(0);
        ClientRepository repository = ClientRepository.getInstance();
        Client target = repository.getClient(targetName);
        if (target == null) {
            client.send(new PlineMessage("command.player_not_found", targetName));
        } else {
            Channel channel = client.getChannel();
            if (target == client) {
                PlineMessage cantsummon = new PlineMessage();
                cantsummon.setKey("command.summon.yourself");
                client.send(cantsummon);
            } else if (channel == target.getChannel()) {
                PlineMessage cantsummon = new PlineMessage();
                cantsummon.setKey("command.summon.same_channel", target.getUser().getName());
                client.send(cantsummon);
            } else if (channel.isFull()) {
                PlineMessage channelfull = new PlineMessage();
                channelfull.setKey("command.summon.full");
                client.send(channelfull);
            } else {
                AddPlayerMessage move = new AddPlayerMessage(target);
                channel.send(move);
                PlineMessage summoned1 = new PlineMessage();
                summoned1.setKey("command.summon.summoned", target.getUser().getName());
                client.send(summoned1);
                PlineMessage summoned2 = new PlineMessage();
                summoned2.setKey("command.summon.summoned_by", client.getUser().getName());
                target.send(summoned2);
            }
        }
    }
}
