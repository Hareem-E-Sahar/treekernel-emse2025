public class Test {    @Override
    public void onCommand(User sender, UserLevel level, String channel, String command, String args[]) {
        if (level.compareTo(UserLevel.IRC_OP) < 0) {
            return;
        }
        if (args.length < 1) {
            Main.getInstance().getIRCHandler().sendMessage(channel, Colors.RED + "* Usage:" + Colors.NORMAL + " !say [message]");
            return;
        }
        String chatMessage;
        StringBuilder chatMessageBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            chatMessageBuilder.append(args[i]);
            chatMessageBuilder.append(" ");
        }
        chatMessage = chatMessageBuilder.toString().substring(0, chatMessageBuilder.toString().length() - 1);
        Utilities.sendMessageToAll(ChatColor.AQUA + "[Admin] " + sender.getNick() + " (IRC): " + chatMessage);
        Main.getInstance().getIRCHandler().sendMessage(channel, Colors.RED + "[Admin] " + Colors.OLIVE + sender.getNick() + Colors.NORMAL + ": " + chatMessage);
    }
}