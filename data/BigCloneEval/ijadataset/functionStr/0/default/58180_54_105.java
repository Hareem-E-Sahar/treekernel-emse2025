public class Test {    public static void start(final Farmyard yard) {
        try {
            SmartCard.start();
            CardTerminalRegistry registry = CardTerminalRegistry.getRegistry();
            registry.setPollInterval(200);
            JibMultiFactory factory = new JibMultiFactory();
            factory.addJiBListener(new JibMultiListener() {

                public void iButtonInserted(JibMultiEvent event) {
                    Farmyard.playSound("inserted.au");
                    int slot = event.getSlotID();
                    SlotChannel channel = event.getChannel();
                    iButtonCardTerminal terminal = (iButtonCardTerminal) channel.getCardTerminal();
                    int[] buttonId = terminal.getiButtonId(channel.getSlotNumber());
                    IButtonAnimalProxy re_ap = (IButtonAnimalProxy) AnimalRegistry.getReusableAnimalProxy(buttonId);
                    if (re_ap == null) {
                        boolean selected = IButtonAnimalProxy.selectApplet(channel);
                        if (selected) {
                            LocationPanel lp = yard.getSelectedLocationPanel();
                            AnimalProxy new_ap;
                            if (lp != null) new_ap = lp.getFactory().createAnimalProxy(channel, buttonId); else {
                                try {
                                    new_ap = new IButtonAnimalProxy(channel, buttonId);
                                } catch (java.rmi.RemoteException e) {
                                    return;
                                }
                            }
                            yard.addAnimal(new_ap);
                        } else {
                            System.err.println("iButton applet \"Animal\" not found");
                            Farmyard.playSound("failure.au");
                        }
                    } else {
                        re_ap.setChannel(channel);
                    }
                }

                public void iButtonRemoved(JibMultiEvent event) {
                    Farmyard.playSound("removed.au");
                }
            });
            registry.addCTListener(factory);
        } catch (CardTerminalException e) {
            e.printStackTrace();
        } catch (CardServiceException e) {
            e.printStackTrace();
        } catch (OpenCardPropertyLoadingException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}