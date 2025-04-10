package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Chris
 */
public class ChannelDelete implements IUserCommandHandler {

    private static final int[] COMMAND_IDS = { 93 };

    /**
	 * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
	 */
    public boolean useUserCommand(int id, L2PcInstance activeChar) {
        if (id != COMMAND_IDS[0]) return false;
        if (activeChar.isInParty()) if (activeChar.getParty().isLeader(activeChar) && activeChar.getParty().isInCommandChannel() && activeChar.getParty().getCommandChannel().getChannelLeader().equals(activeChar)) {
            L2CommandChannel channel = activeChar.getParty().getCommandChannel();
            SystemMessage sm = SystemMessage.sendString("The Command Channel was disbanded.");
            channel.broadcastToChannelMembers(sm);
            channel.disbandChannel();
            return true;
        }
        return false;
    }

    /**
	 * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
