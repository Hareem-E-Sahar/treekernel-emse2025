package com.clanwts.nbdf.boot;

import java.io.FileOutputStream;
import java.security.KeyPairGenerator;
import edu.cmu.ece.agora.futures.FutureManager;
import edu.cmu.ece.agora.session.AbstractCommand;
import edu.cmu.ece.agora.session.CommandContext;
import edu.cmu.ece.agora.util.Serialization;

public class Domain extends AbstractCommand {

    @Override
    protected void onRun(FutureManager<Void> fm, CommandContext ctx, String[] args) {
        if (args.length < 1) {
            ctx.printLine("Usage: domain <command> [arguments]");
            fm.completeFuture(null);
            return;
        }
        if (args[0].equalsIgnoreCase("create")) {
            if (args.length != 4) {
                ctx.printLine("Usage: domain create <key-pair file> <key-pair algorithm> <cipher strength>");
                fm.completeFuture(null);
                return;
            }
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(args[2]);
                kpg.initialize(Integer.valueOf(args[3]));
                FileOutputStream fos = new FileOutputStream(args[1]);
                Serialization.out(kpg.generateKeyPair(), fos);
                fos.close();
            } catch (Exception e) {
                fm.cancelFuture(e);
                return;
            }
        } else {
            ctx.printLine("Domain management subcommand \"" + args[0] + "\" not recognized.");
        }
        fm.completeFuture(null);
    }
}
