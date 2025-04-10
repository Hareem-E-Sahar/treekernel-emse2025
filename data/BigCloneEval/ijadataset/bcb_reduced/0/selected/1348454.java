package com.l2jserver.gameserver.model;

import java.util.List;
import javolution.util.FastList;
import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;
import com.l2jserver.gameserver.network.serverpackets.ExCloseMPCC;
import com.l2jserver.gameserver.network.serverpackets.ExMPCCPartyInfoUpdate;
import com.l2jserver.gameserver.network.serverpackets.ExOpenMPCC;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 *
 * @author  chris_00
 */
public class L2CommandChannel {

    private final List<L2Party> _partys;

    private L2PcInstance _commandLeader = null;

    private int _channelLvl;

    /**
	 * Creates a New Command Channel and Add the Leaders party to the CC
	 *
	 * @param CommandChannelLeader
	 *
	 */
    public L2CommandChannel(L2PcInstance leader) {
        _commandLeader = leader;
        _partys = new FastList<L2Party>();
        _partys.add(leader.getParty());
        _channelLvl = leader.getParty().getLevel();
        leader.getParty().setCommandChannel(this);
        leader.getParty().broadcastToPartyMembers(new SystemMessage(SystemMessageId.COMMAND_CHANNEL_FORMED));
        leader.getParty().broadcastToPartyMembers(new ExOpenMPCC());
    }

    /**
	 * Adds a Party to the Command Channel
	 * @param Party
	 */
    public void addParty(L2Party party) {
        if (party == null) return;
        this.broadcastToChannelMembers(new ExMPCCPartyInfoUpdate(party, 1));
        _partys.add(party);
        if (party.getLevel() > _channelLvl) _channelLvl = party.getLevel();
        party.setCommandChannel(this);
        party.broadcastToPartyMembers(new SystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL));
        party.broadcastToPartyMembers(new ExOpenMPCC());
    }

    /**
	 * Removes a Party from the Command Channel
	 * @param Party
	 */
    public void removeParty(L2Party party) {
        if (party == null) return;
        _partys.remove(party);
        _channelLvl = 0;
        for (L2Party pty : _partys) {
            if (pty.getLevel() > _channelLvl) _channelLvl = pty.getLevel();
        }
        party.setCommandChannel(null);
        party.broadcastToPartyMembers(new ExCloseMPCC());
        if (_partys.size() < 2) {
            broadcastToChannelMembers(new SystemMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED));
            disbandChannel();
        } else {
            this.broadcastToChannelMembers(new ExMPCCPartyInfoUpdate(party, 0));
        }
    }

    /**
	 * disbands the whole Command Channel
	 */
    public void disbandChannel() {
        if (_partys != null) {
            for (L2Party party : _partys) {
                if (party != null) removeParty(party);
            }
        }
        _partys.clear();
    }

    /**
	 * @return overall membercount of the Command Channel
	 */
    public int getMemberCount() {
        int count = 0;
        for (L2Party party : _partys) {
            if (party != null) count += party.getMemberCount();
        }
        return count;
    }

    /**
	 * Broadcast packet to every channelmember
	 * @param L2GameServerPacket
	 */
    public void broadcastToChannelMembers(L2GameServerPacket gsp) {
        if (_partys != null && !_partys.isEmpty()) {
            for (L2Party party : _partys) {
                if (party != null) party.broadcastToPartyMembers(gsp);
            }
        }
    }

    public void broadcastCSToChannelMembers(CreatureSay gsp, L2PcInstance broadcaster) {
        if (_partys != null && !_partys.isEmpty()) {
            for (L2Party party : _partys) {
                if (party != null) party.broadcastCSToPartyMembers(gsp, broadcaster);
            }
        }
    }

    /**
	 * @return list of Parties in Command Channel
	 */
    public List<L2Party> getPartys() {
        return _partys;
    }

    /**
	 * @return list of all Members in Command Channel
	 */
    public List<L2PcInstance> getMembers() {
        List<L2PcInstance> members = new FastList<L2PcInstance>();
        for (L2Party party : getPartys()) {
            members.addAll(party.getPartyMembers());
        }
        return members;
    }

    /**
	 *
	 * @return Level of CC
	 */
    public int getLevel() {
        return _channelLvl;
    }

    /**
	 * @param sets the leader of the Command Channel
	 */
    public void setChannelLeader(L2PcInstance leader) {
        _commandLeader = leader;
    }

    /**
	 * @return the leader of the Command Channel
	 */
    public L2PcInstance getChannelLeader() {
        return _commandLeader;
    }

    /**
	 *
	 *
	 * @param obj
	 * @return true if proper condition for RaidWar
	 */
    public boolean meetRaidWarCondition(L2Object obj) {
        if (!(obj instanceof L2Character && ((L2Character) obj).isRaid())) return false;
        return (getMemberCount() >= Config.LOOT_RAIDS_PRIVILEGE_CC_SIZE);
    }
}
