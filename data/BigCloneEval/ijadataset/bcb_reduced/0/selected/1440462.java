package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ExAskJoinMPCC;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * Format: (ch) S
 * @author chris_00
 *
 * D0 0D 00 5A 00 77 00 65 00 72 00 67 00 00 00
 *
 */
public final class RequestExAskJoinMPCC extends L2GameClientPacket {

    private static final String _C__D0_0D_REQUESTEXASKJOINMPCC = "[C] D0:0D RequestExAskJoinMPCC";

    private String _name;

    @Override
    protected void readImpl() {
        _name = readS();
    }

    @Override
    protected void runImpl() {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null) return;
        L2PcInstance player = L2World.getInstance().getPlayer(_name);
        if (player == null) return;
        if (activeChar.isInParty() && player.isInParty() && activeChar.getParty().equals(player.getParty())) return;
        if (activeChar.isInParty()) {
            L2Party activeParty = activeChar.getParty();
            if (activeParty.getPartyMembers().get(0).equals(activeChar)) {
                if (activeParty.isInCommandChannel() && activeParty.getCommandChannel().getChannelLeader().equals(activeChar)) {
                    if (player.isInParty()) {
                        if (player.getParty().isInCommandChannel()) {
                            activeChar.sendMessage("Your target is already in a CommandChannel");
                        } else {
                            askJoinMPCC(activeChar, player);
                        }
                    } else {
                        activeChar.sendMessage("Your target has no Party.");
                    }
                } else if (activeParty.isInCommandChannel() && !activeParty.getCommandChannel().getChannelLeader().equals(activeChar)) {
                    activeChar.sendMessage("Only the CommandChannelLeader can give out an invite.");
                } else {
                    if (player.isInParty()) {
                        if (player.getParty().isInCommandChannel()) {
                            activeChar.sendMessage("Your target is already in a CommandChannel");
                        } else {
                            askJoinMPCC(activeChar, player);
                        }
                    } else {
                        activeChar.sendMessage("Your target has no Party.");
                    }
                }
            } else {
                activeChar.sendMessage("Only the Partyleader can give out an invite.");
            }
        }
    }

    private void askJoinMPCC(L2PcInstance requestor, L2PcInstance target) {
        boolean hasRight = false;
        if (requestor.getClan() != null && requestor.getClan().getLeaderId() == requestor.getObjectId()) hasRight = true; else if (requestor.getInventory().getItemByItemId(8871) != null) hasRight = true; else {
            for (L2Skill skill : requestor.getAllSkills()) {
                if (skill.getId() == 391) {
                    hasRight = true;
                    break;
                }
            }
        }
        if (!hasRight && !requestor.getParty().isInCommandChannel()) {
            requestor.sendMessage("You dont have the rights to open a Command Channel!");
            return;
        }
        if (!target.isProcessingRequest()) {
            requestor.onTransactionRequest(target);
            target.getParty().getPartyMembers().get(0).sendPacket(new ExAskJoinMPCC(requestor.getName()));
            requestor.sendMessage("You invited " + target.getName() + " to your Command Channel.");
        } else {
            requestor.sendPacket(new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER));
        }
    }

    @Override
    public String getType() {
        return _C__D0_0D_REQUESTEXASKJOINMPCC;
    }
}
