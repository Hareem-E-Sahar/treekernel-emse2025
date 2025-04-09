package org.foxtalkz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.foxtalkz.encript.AESEncriptor;
import org.foxtalkz.message.ControlMessage;
import org.foxtalkz.message.GroupDetailMessage;
import org.foxtalkz.message.Message;
import org.foxtalkz.message.MessageEncriptor;
import org.foxtalkz.user.User;

public class GroupManager {

    private final Object groupsUpdateLock = new Object();

    private Hashtable<InetAddress, GroupContainer> groups;

    private ArrayList<InetAddress> engagedGroups;

    private ArrayList<InetAddress> pendingGroups;

    private HashMap<InetAddress, Group> recievedRequests;

    private ArrayList<InetAddress> sentRequests;

    private GroupManagerListener groupManagerListener;

    private int groupCount = 0;

    private Services services;

    private ScheduledTask publishGroups;

    private ScheduledTask refreshGroups;

    public GroupManager() {
        publishGroups = new ScheduledTask() {

            public boolean isNewThread() {
                return true;
            }

            public void doTask() {
                sendOwnedGroups();
                attackGroups();
                sendOwnedPrivateGroups();
            }
        };
        engagedGroups = new ArrayList<InetAddress>();
        refreshGroups = new ScheduledTask() {

            public boolean isNewThread() {
                return false;
            }

            public void doTask() {
            }
        };
        pendingGroups = new ArrayList<InetAddress>();
        groups = new Hashtable<InetAddress, GroupContainer>();
        recievedRequests = new HashMap<InetAddress, Group>();
        sentRequests = new ArrayList<InetAddress>();
    }

    /**
     * This creates the public chat group with out encription. Alll the users can use this without encription
     * @return
     */
    public Group createLobby() {
        InetAddress groupAddress = null;
        try {
            groupAddress = InetAddress.getByName(Services.getLobbyGroup());
        } catch (UnknownHostException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        Group group = new Group();
        group.setDescription("Any one can chat with every one");
        group.setName("Lobby");
        group.setMulticastAddress(groupAddress);
        group.setGroupOwner(getServices().getUserManager().getLocalUser().getId());
        group.setOwned(false);
        group.setGroupManager(this);
        group.setPrivateGroup(false);
        synchronized (groupsUpdateLock) {
            groups.put(groupAddress, new LobbyGroupContainer(group));
        }
        engagedGroups.add(groupAddress);
        MessageEncriptor en = new MessageEncriptor() {

            public void init() {
            }

            public byte[] encript(byte[] data) {
                return data;
            }

            public byte[] decript(byte[] data) {
                return data;
            }

            public SecretKey getSecretKey() {
                return null;
            }
        };
        group.setMessageEncriptor(en);
        return group;
    }

    public Group createGroup(String groupName, String description, boolean privateGroup) {
        InetAddress groupAddress = null;
        do {
            groupAddress = generateGroupAddress();
        } while (getGroup(groupAddress) != null);
        Group group = new Group();
        group.setDescription(description);
        group.setName(groupName);
        group.setMulticastAddress(groupAddress);
        group.setGroupOwner(getServices().getUserManager().getLocalUser().getId());
        group.setOwned(true);
        group.setGroupManager(this);
        group.setPrivateGroup(privateGroup);
        synchronized (groupsUpdateLock) {
            groups.put(groupAddress, new GroupContainer(group));
        }
        engagedGroups.add(groupAddress);
        group.updateUsers(getServices().getUserManager().getLocalUser().getId());
        return group;
    }

    public void joinGroup(Group group, SecretKey key) {
        group.setGroupManager(this);
        boolean contain = getPendingGroups().contains(group.getMulticastAddress());
        if (contain) {
            getPendingGroups().remove(group.getMulticastAddress());
            group.setMessageEncriptor(new AESEncriptor(key));
        } else {
            if (group.isOwned()) {
                group.setMessageEncriptor(new AESEncriptor());
            }
        }
        try {
            if (!group.isOwned() && !contain) {
            } else {
                group.init();
                group.updateUsers(getServices().getUserManager().getLocalUser().getId());
            }
            services.getMulticastSocket().joinGroup(group.getMulticastAddress());
            if (!engagedGroups.contains(group.getMulticastAddress())) {
                engagedGroups.add(group.getMulticastAddress());
            }
            groupManagerListener.onGroupManagerChange(GroupManagerListener.GROUP_LIST_REFRESHED);
        } catch (IOException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method requsts a key form the group owner to join to a group.
     * The group owner will send the SecretKey to this user.
     * @param g
     */
    public void requestToJoin(Group g) {
        sendGroupKeyRequest(g);
        getPendingGroups().add(g.getMulticastAddress());
    }

    /**
     * This method will accept a request for a group and send the secret 
     * key to the requester by encripting with the requester's
     * publick key wich is his id
     * @param g
     * @param u
     */
    public void confirmJoinRequest(Group g, User u) {
        ControlMessage msg = (ControlMessage) Message.getMessage(Message.TYPE_CONTROL);
        SecretKey key = g.getMessageEncriptor().getSecretKey();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = null;
        try {
            oout = new ObjectOutputStream(bout);
            oout.writeObject(services.getUserManager().getLocalUser().getId());
            oout.writeObject(encriptData(key.getEncoded(), u.getId()));
            oout.writeObject(g.getMulticastAddress());
        } catch (IOException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        msg.setSubType(ControlMessage.SUBTYPE_GROUP_KEYACCEPT);
        msg.setContent(bout.toByteArray());
        msg.setAddress(u.getAddress());
        getRecievedRequests().remove(g.getMulticastAddress());
        services.getMessageSender().send(msg);
    }

    /**
     * Sends the reject join request to the requester
     * This is common for requester requesting from the group owner or group
     * owner requesting form any other person
     * @param g
     * @param u
     */
    public void rejectJoinRequest(Group g, User u) {
        ControlMessage msg = (ControlMessage) Message.getMessage(Message.TYPE_CONTROL);
        msg.setSubType(ControlMessage.SUBTYPE_GROUP_KEYREJECT);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = null;
        try {
            oout = new ObjectOutputStream(bout);
            oout.writeObject(g.getMulticastAddress());
        } catch (IOException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        msg.setContent(bout.toByteArray());
        msg.setAddress(u.getAddress());
        getRecievedRequests().remove(g.getMulticastAddress());
        services.getMessageSender().send(msg);
    }

    /**
     * Send the request to join the group g to the user u.
     * Send the groups multicast addres to the user u so that the user knows the details about the
     * group to join. The message is sent as the control message.
     * @param g
     * @param u
     */
    public void sendJoinRequest(Group g, User u) {
        ControlMessage msg = (ControlMessage) Message.getMessage(Message.TYPE_CONTROL);
        msg.setSubType(ControlMessage.SUBTYPE_GROUP_JOINREQUEST);
        msg.setAddress(u.getAddress());
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = null;
        try {
            oout = new ObjectOutputStream(bout);
            oout.writeObject(g);
        } catch (IOException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        msg.setContent(bout.toByteArray());
        getSentRequests().add(g.getMulticastAddress());
        services.getMessageSender().send(msg);
    }

    public void leaveGroup(Group group) {
        try {
            services.getMulticastSocket().leaveGroup(group.getMulticastAddress());
            engagedGroups.remove(group.getMulticastAddress());
            group.destroy();
            if (group.isOwned()) {
                groups.remove(group.getMulticastAddress());
                groupManagerListener.onGroupManagerChange(GroupManagerListener.GROUP_LIST_REFRESHED);
            }
        } catch (IOException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void joinGroup(long groupID) {
    }

    public int getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(int val) {
    }

    public Group getGroup(String multicastAddress) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(multicastAddress);
        } catch (UnknownHostException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return getGroup(address);
    }

    public Group getGroup(InetAddress multicastAddress) {
        GroupContainer gc = groups.get(multicastAddress);
        if (gc != null) {
            return gc.group;
        }
        return null;
    }

    public Hashtable<InetAddress, Group> getGroups() {
        Hashtable<InetAddress, Group> table = new Hashtable<InetAddress, Group>();
        Object[] gps = null;
        synchronized (groupsUpdateLock) {
            gps = groups.values().toArray();
        }
        for (int i = 0; i < gps.length; i++) {
            GroupContainer gc = (GroupContainer) gps[i];
            table.put(gc.getGroup().getMulticastAddress(), gc.getGroup());
        }
        return table;
    }

    public void setGroups(Hashtable<InetAddress, Group> val) {
    }

    public Services getServices() {
        return this.services;
    }

    public void setServices(Services val) {
        this.services = val;
    }

    private InetAddress generateGroupAddress() {
        InetAddress address = null;
        int second = (int) (Math.random() * 253) + 1;
        int third = (int) (Math.random() * 253) + 1;
        int fourth = (int) (Math.random() * 253) + 1;
        String generatedMulticastAddress = "239." + second + "." + third + "." + fourth;
        try {
            address = InetAddress.getByName(generatedMulticastAddress);
        } catch (UnknownHostException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return address;
    }

    public ArrayList<InetAddress> getEngagedGroups() {
        return engagedGroups;
    }

    public void setEngagedGroups(ArrayList<InetAddress> val) {
    }

    private void sendOwnedGroups() {
        boolean owning = false;
        GroupDetailMessage msg = (GroupDetailMessage) Message.getMessage(Message.TYPE_GROUP_DETAIL);
        msg.setSubType(GroupDetailMessage.GROUPLIST);
        int length = engagedGroups.size();
        for (int i = 0; i < length; i++) {
            GroupContainer g = groups.get(engagedGroups.get(i));
            if (g == null) {
                continue;
            }
            if (g.group.isOwned() && !g.group.isPrivateGroup()) {
                msg.add(g.group);
                owning = true;
            }
        }
        if (!owning) {
            return;
        }
        try {
            msg.setAddress(InetAddress.getByName(services.getControlGroup()));
        } catch (UnknownHostException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        getServices().getMessageSender().send(msg);
    }

    public void init() {
        getServices().getTaskSchedular().registerTask(publishGroups, 5);
    }

    public void updateGroup(Group g) {
        GroupContainer gc = groups.get(g.getMulticastAddress());
        if (gc != null) {
            gc.cure(getServices().getUserManager().getLocalUser().getId());
        } else {
            synchronized (groupsUpdateLock) {
                g.setGroupManager(this);
                groups.put(g.getMulticastAddress(), new GroupContainer(g));
            }
        }
    }

    private void attackGroups() {
        synchronized (groupsUpdateLock) {
            boolean changed = false;
            GroupContainer[] gc = groups.values().toArray(new GroupContainer[0]);
            int count = gc.length;
            if (groupCount != count) {
                changed = true;
            }
            groupCount = count;
            int length = gc.length;
            PublicKey me = getServices().getUserManager().getLocalUser().getId();
            for (int i = 0; i < length; i++) {
                gc[i].attack();
                if (gc[i].isDead()) {
                    changed = true;
                    if (gc[i].group.getUserList() == null) continue;
                    if (gc[i].group.getUserList().size() > 1 && gc[i].group.getUserList().get(1).equals(me)) {
                        changeOwner(gc[i].group, me);
                    } else {
                        gc[i].group.destroy();
                        engagedGroups.remove(gc[i].group.getMulticastAddress());
                        groups.remove(gc[i].group.getMulticastAddress());
                    }
                }
            }
            if (changed) {
                groupManagerListener.onGroupManagerChange(GroupManagerListener.GROUP_LIST_REFRESHED);
                System.out.println("Changed");
            }
        }
    }

    public void changeOwner(Group group, PublicKey newOwner) {
        group.setGroupOwner(newOwner);
        if (newOwner.equals(getServices().getUserManager().getLocalUser().getId())) {
            group.setOwned(true);
            ControlMessage msg = (ControlMessage) Message.getMessage(Message.TYPE_CONTROL);
            msg.setSubType(ControlMessage.SUBTYPE_GROUP_CHANGEOWNER);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oout = new ObjectOutputStream(out);
                oout.writeObject(group.getMulticastAddress());
                oout.writeObject(newOwner);
                oout.flush();
            } catch (IOException ex) {
                Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            InetAddress control = null;
            try {
                control = InetAddress.getByName(getServices().getControlGroup());
            } catch (UnknownHostException ex) {
                Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            msg.setAddress(control);
            msg.setContent(out.toByteArray());
            getServices().getMessageSender().send(msg);
        }
    }

    private void sendGroupKeyRequest(Group g) {
        ObjectOutputStream oout = null;
        try {
            User u = getServices().getUserManager().getUser(g.getGroupOwner());
            ControlMessage msg = (ControlMessage) Message.getMessage(Message.TYPE_CONTROL);
            msg.setSubType(ControlMessage.SUBTYPE_GROUP_KEYREQUEST);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            oout = new ObjectOutputStream(bout);
            oout.writeObject(g.getMulticastAddress());
            oout.writeObject(getServices().getUserManager().getLocalUser().getId());
            oout.flush();
            msg.setContent(bout.toByteArray());
            msg.setAddress(u.getAddress());
            sentRequests.add(g.getMulticastAddress());
            getServices().getMessageSender().send(msg);
        } catch (IOException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                oout.close();
            } catch (IOException ex) {
                Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private byte[] encriptData(byte[] raw, PublicKey key) {
        Cipher ciper = null;
        byte[] encripted = null;
        try {
            ciper = Cipher.getInstance("RSA");
            ciper.init(Cipher.ENCRYPT_MODE, key);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(ciper.doFinal(raw));
            encripted = out.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return encripted;
    }

    public byte[] decriptData(byte[] data) {
        Cipher ciper = null;
        byte[] decripted = null;
        try {
            ciper = Cipher.getInstance("RSA");
            ciper.init(Cipher.DECRYPT_MODE, getServices().getUserManager().getLocalUser().getPrivateKey());
            decripted = ciper.doFinal(data);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return decripted;
    }

    /**
     * @return the groupManagerListener
     */
    public GroupManagerListener getGroupManagerListener() {
        return groupManagerListener;
    }

    /**
     * @param groupManagerListener the groupManagerListener to set
     */
    public void setGroupManagerListener(GroupManagerListener groupManagerListener) {
        this.groupManagerListener = groupManagerListener;
    }

    /**
     * @return the pendingGroups
     */
    public ArrayList<InetAddress> getPendingGroups() {
        return pendingGroups;
    }

    /**
     * @return the recievedRequests
     */
    public HashMap<InetAddress, Group> getRecievedRequests() {
        return recievedRequests;
    }

    /**
     * @return the sentRequests
     */
    public ArrayList<InetAddress> getSentRequests() {
        return sentRequests;
    }

    public void rejectJoinRequest(InetAddress grp) {
        recievedRequests.remove(grp);
    }

    private void sendOwnedPrivateGroups() {
        int length = engagedGroups.size();
        for (int i = 0; i < length; i++) {
            GroupContainer g = groups.get(engagedGroups.get(i));
            if (g == null) {
                continue;
            }
            if (g.group.isOwned() && g.group.isPrivateGroup()) {
                GroupDetailMessage msg = (GroupDetailMessage) Message.getMessage(Message.TYPE_GROUP_DETAIL);
                msg.setSubType(GroupDetailMessage.GROUPLIST);
                msg.add(g.group);
                msg.setAddress(g.group.getMulticastAddress());
                services.getMessageSender().send(msg);
            }
        }
    }

    private class GroupContainer {

        private static final int FULL_HEALTH = 3;

        private Group group;

        private int health;

        /**
         * @return the group
         */
        public Group getGroup() {
            return group;
        }

        public GroupContainer(Group g) {
            group = g;
            health = FULL_HEALTH;
        }

        /**
         * @param group the group to set
         */
        public void setGroup(Group group) {
            this.group = group;
        }

        /**
         * @return the health
         */
        public int getHealth() {
            return health;
        }

        /**
         * @param health the health to set
         */
        public void setHealth(int health) {
            this.health = health;
        }

        public void attack() {
            health--;
        }

        public boolean isDead() {
            return (health <= 0);
        }

        public void cure(PublicKey me) {
            if (group.getUserList() == null) return;
            if (group.getUserList().size() > 1 && group.getUserList().get(1).equals(me)) {
                health = FULL_HEALTH - 1;
            } else {
                health = FULL_HEALTH;
            }
        }
    }

    private class LobbyGroupContainer extends GroupContainer {

        public LobbyGroupContainer(Group g) {
            super(g);
        }

        @Override
        public void attack() {
        }
    }
}
