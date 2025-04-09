package com.gite.jxta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import org.apache.log4j.Logger;
import net.jxta.credential.AuthenticationCredential;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLElement;
import net.jxta.id.IDFactory;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.NetPeerGroupFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.ModuleSpecID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.protocol.ConfigParams;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

public class MyNetwork {

    private static String NetPeerGroupID = "urn:jxta:uuid-8B33E028B054497B8BF9A446A224B1FF02";

    private static String NetPeerGroupName = "GITe PEER GROUP";

    private static String NetPeerGroupDesc = "Just Give It To mE";

    private String userPeerID;

    private NetworkConfigurator configurator;

    private PeerGroup netPeerGroup;

    private PeerGroup appPeerGroup;

    private Logger logger = null;

    public static String getNetPeerGroupID() {
        return NetPeerGroupID;
    }

    public static String getNetPeerGroupName() {
        return NetPeerGroupName;
    }

    public static String getNetPeerGroupDesc() {
        return NetPeerGroupDesc;
    }

    public String getUserPeerID() {
        if (userPeerID != null) return userPeerID; else return appPeerGroup.getPeerID().toString();
    }

    public void startJXTA(String jxtaHome) throws Throwable {
        clearCache(new File(jxtaHome, "cm"));
        if (logger != null) logger.info("Starting JXTA platform");
        NetPeerGroupFactory factory = null;
        try {
            factory = new NetPeerGroupFactory((ConfigParams) configurator.getPlatformConfig(), new File(jxtaHome).toURI(), IDFactory.fromURI(new URI(getNetPeerGroupID())), getNetPeerGroupName(), (XMLElement) StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, "desc", getNetPeerGroupName()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.out.println("Exiting...");
            System.exit(1);
        }
        netPeerGroup = factory.getInterface();
        if (logger != null) logger.info("Platform started");
    }

    private void handleConfiguration(NetworkConfigurator configurator, String peerID, String Principal, String password) {
        configurator.setName("My Peer Name");
        configurator.setPrincipal("ofno");
        configurator.setPassword("consequence");
        configurator.setDescription("Created by GITe MyNetwork.");
        configurator.setUseMulticast(false);
        if (peerID == null) {
            configurator.setPeerID(IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID));
            URI seedingURI = new File("seeds.txt").toURI();
            configurator.addRdvSeedingURI(seedingURI);
            configurator.addRelaySeedingURI(seedingURI);
            configurator.setTcpIncoming(false);
        } else {
            configurator.setPeerId(peerID);
            try {
                String rdvSeedingURI = "";
                String relaySeedingURI = "";
                configurator.addRdvSeedingURI(new URI(rdvSeedingURI));
                configurator.addRelaySeedingURI(new URI(relaySeedingURI));
                configurator.setMode(NetworkConfigurator.RDV_SERVER + NetworkConfigurator.RELAY_SERVER);
                configurator.setTcpEnabled(true);
                configurator.setTcpIncoming(true);
                configurator.setTcpOutgoing(true);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        if (logger != null) logger.info("Configuring private net");
        try {
            configurator.save();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Platform configured and saved");
        if (logger != null) logger.info("Platform configured and saved");
    }

    public void configureJXTA(String jxtaHome, String peerID) {
        if (logger != null) logger.info("Configuring platform");
        System.out.println("Configuring platform");
        configurator = new NetworkConfigurator();
        configurator.setHome(new File(jxtaHome));
        if (!configurator.exists()) {
            handleConfiguration(configurator, peerID, "ofno", "consequence");
        } else {
            if (logger != null) logger.info("Found configuration... Loading...");
            System.out.println("Found configuration... Loading...");
            try {
                File pc = new File(configurator.getHome(), "PlatformConfig");
                configurator.load(pc.toURI());
                if (logger != null) logger.info("Configuration loaded successfully.");
                System.out.println("Configuration loaded successfully.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void clearCache(final File rootDir) {
        try {
            if (rootDir.exists()) {
                File[] list = rootDir.listFiles();
                for (File aList : list) {
                    if (aList.isDirectory()) {
                        clearCache(aList);
                    } else {
                        aList.delete();
                    }
                }
            }
            rootDir.delete();
            System.out.println("Cache component " + rootDir.toString() + " cleared.");
        } catch (Throwable t) {
            System.out.println("Unable to clear " + rootDir.toString());
            t.printStackTrace();
        }
    }

    public PeerGroup getAppPeerGroup() {
        return appPeerGroup;
    }

    public void setAppPeerGroup(PeerGroup appPeerGroup) {
        this.appPeerGroup = appPeerGroup;
    }

    public PeerGroup getNetPeerGroup() {
        return netPeerGroup;
    }

    public void joinGroup(PeerGroup peerGroup) {
        try {
            AuthenticationCredential cred = new AuthenticationCredential(peerGroup, null, null);
            MembershipService membershipService = peerGroup.getMembershipService();
            Authenticator authenticator = membershipService.apply(cred);
            if (authenticator.isReadyForJoin()) {
                membershipService.join(authenticator);
                System.out.println("Joined group: " + peerGroup);
                if (logger != null) logger.info("Joined group: " + peerGroup);
            } else {
                System.out.println("Impossible to join the group");
                if (logger != null) logger.info("Impossible to join the group");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PeerGroup createApplicationPeerGroup() {
        String name = "GITe APPLICATION PEER GROUP";
        String desc = "Just Give It To mE";
        String gid = "urn:jxta:uuid-79B6A084D3264DF8B641867D926C48D902";
        String specID = "urn:jxta:uuid-309B33F10EDF48738183E3777A7C3DE9C5BFE5794E974DD99AC7D409F5686F3306";
        try {
            StringBuilder sb = new StringBuilder("=Creating group:  ");
            sb.append(name).append(", ");
            sb.append(desc).append(", ");
            sb.append(gid).append(", ");
            sb.append(specID);
            if (logger != null) logger.info(sb.toString());
            ModuleImplAdvertisement implAdv = netPeerGroup.getAllPurposePeerGroupImplAdvertisement();
            ModuleSpecID modSpecID = (ModuleSpecID) IDFactory.fromURI(new URI(specID));
            implAdv.setModuleSpecID(modSpecID);
            PeerGroupID groupID = (PeerGroupID) IDFactory.fromURI(new URI(gid));
            appPeerGroup = netPeerGroup.newGroup(groupID, implAdv, name, desc);
            PeerGroupAdvertisement pgAdv = appPeerGroup.getPeerGroupAdvertisement();
            userPeerID = appPeerGroup.getPeerID().toString();
            netPeerGroup.getDiscoveryService().publish(implAdv);
            netPeerGroup.getDiscoveryService().publish(pgAdv);
            netPeerGroup.getDiscoveryService().remotePublish(null, implAdv);
            netPeerGroup.getDiscoveryService().remotePublish(null, pgAdv);
            if (logger != null) logger.info("Private Application newGroup = " + name + " created and published");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exiting.");
            if (logger != null) logger.error("Exiting.");
            System.exit(1);
        }
        return appPeerGroup;
    }

    public void stop() {
        netPeerGroup.stopApp();
    }

    public Logger initLogger(String filename) {
        System.setProperty("log4j.defaultInitOverride", "true");
        Logger logger = null;
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            InputStream is = cl.getResourceAsStream(filename);
            Properties p = new Properties();
            p.load(is);
            is.close();
            org.apache.log4j.PropertyConfigurator.configure(p);
            logger = Logger.getLogger(this.getClass());
        } catch (Exception e) {
            e.printStackTrace();
            return logger;
        }
        this.logger = logger;
        return logger;
    }
}
