package com.clanwts.nbdf.boot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import edu.cmu.ece.agora.link.LinkManager;
import edu.cmu.ece.agora.link.internet.InternetEndpoint;
import edu.cmu.ece.agora.link.internet.InternetLinkManager;
import edu.cmu.ece.agora.session.Command;
import edu.cmu.ece.agora.session.CommandContext;
import edu.cmu.ece.agora.session.EntryPoint;
import edu.cmu.ece.agora.util.Serialization;
import edu.cmu.ece.mithril.MithrilID;
import edu.cmu.ece.mithril.MithrilKey;
import edu.cmu.ece.mithril.overlay.MithrilBootstrap;

@Command(name = "Mithril Identity Manager", aliases = { "mim" })
public class MithrilIdentityManager {

    private static final String USAGE_CREATE_DOMAIN = "Usage: MithrilIdentityManager create domain <output domain file> [parent domain file] [child number]";

    private static final String USAGE_CREATE = "Usage: MithrilIdentityManager create <domain | node> [arguments]";

    private static final String USAGE_MAIN = "Usage: MithrilIdentityManager <create> [arguments]";

    private static final String USAGE_CREATE_NODE = "Usage: MithrilIdentityManager create node <output node file> <domain file> [bootstrap address] [bootstrap port]";

    private static final String KP_ALGORITHM = "RSA";

    private static final int KP_STRENGTH = 1024;

    @EntryPoint
    public static void main(String[] args, CommandContext ctx) throws Exception {
        if (args.length < 1) {
            ctx.printLine(USAGE_MAIN);
        } else if (args[0].equalsIgnoreCase("create")) {
            handleCreate(args, ctx);
        } else {
            ctx.printLine(USAGE_MAIN);
        }
    }

    private static void handleCreate(String[] args, CommandContext ctx) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        if (args.length < 2) {
            ctx.printLine(MithrilIdentityManager.USAGE_CREATE);
        } else if (args[1].equalsIgnoreCase("domain")) {
            if (args.length < 3 || args.length > 5) {
                ctx.printLine(MithrilIdentityManager.USAGE_CREATE_DOMAIN);
                return;
            }
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KP_ALGORITHM);
            kpg.initialize(KP_STRENGTH);
            MithrilKey domain_key = MithrilKey.NULL;
            KeyPair domain_kp = kpg.generateKeyPair();
            if (args.length == 4) {
                FileInputStream fis = new FileInputStream(args[3]);
                int child = 1;
                if (args.length == 5) {
                    child = Integer.parseInt(args[4]);
                }
                domain_key = Serialization.in(fis);
                domain_key = domain_key.getChild(child);
                fis.close();
            }
            OutputStream os = new FileOutputStream(args[2]);
            Serialization.out(domain_key, os);
            Serialization.out(domain_kp, os);
            os.close();
        } else if (args[1].equalsIgnoreCase("node")) {
            if (args.length < 4 || args.length > 6) {
                ctx.printLine(MithrilIdentityManager.USAGE_CREATE_NODE);
                return;
            }
            FileInputStream fis = new FileInputStream(args[3]);
            final MithrilKey domain_key = Serialization.in(fis);
            KeyPair domain_kp = Serialization.in(fis);
            fis.close();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KP_ALGORITHM);
            kpg.initialize(KP_STRENGTH);
            MithrilID node_id = new MithrilID(domain_key, MithrilKey.ROOT);
            KeyPair node_kp = kpg.generateKeyPair();
            if (args.length == 5) {
                InternetEndpoint local_ep = new InternetEndpoint(InetAddress.getByName("0.0.0.0"), 0);
                Executor exec = Executors.newCachedThreadPool();
                LinkManager<InternetEndpoint> transport = new InternetLinkManager(local_ep, exec, exec, exec, exec, exec, exec);
                String bs_addr = args[4];
                int bs_port = 6666;
                if (args.length == 6) {
                    bs_port = Integer.parseInt(args[5]);
                }
                InternetEndpoint bs_ep = new InternetEndpoint(InetAddress.getByName(bs_addr), bs_port);
                node_id = MithrilBootstrap.bootstrap(domain_key, domain_kp, node_kp, transport, bs_ep).get();
            }
            OutputStream os = new FileOutputStream(args[2]);
            Serialization.out(domain_kp, os);
            Serialization.out(node_id, os);
            Serialization.out(node_kp, os);
            os.close();
        } else {
            ctx.printLine(MithrilIdentityManager.USAGE_CREATE);
        }
    }
}
