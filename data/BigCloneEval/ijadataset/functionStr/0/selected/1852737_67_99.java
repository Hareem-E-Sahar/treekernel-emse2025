public class Test {    public void testGetMixerGraphChildStructure2() {
        Mixer mixer = new Mixer();
        Channel channel = new Channel();
        channel.setName("1");
        mixer.getChannels().addChannel(channel);
        Channel channel2 = new Channel();
        channel2.setName("2");
        mixer.getChannels().addChannel(channel2);
        Channel subChannel1 = new Channel();
        subChannel1.setName("subChannel1");
        mixer.getSubChannels().addChannel(subChannel1);
        Channel subChannel2 = new Channel();
        subChannel2.setName("subChannel2");
        mixer.getSubChannels().addChannel(subChannel2);
        channel.setOutChannel(subChannel1.getName());
        channel2.setOutChannel(subChannel2.getName());
        subChannel1.setOutChannel(subChannel2.getName());
        MixerNode node = MixerNode.getMixerGraph(mixer);
        assertEquals(Channel.MASTER, node.channel.getName());
        assertEquals(1, node.children.size());
        node = (MixerNode) node.children.get(0);
        assertEquals(2, node.children.size());
        assertEquals(subChannel2, node.channel);
        MixerNode node2 = (MixerNode) node.children.get(1);
        node = (MixerNode) node.children.get(0);
        assertEquals(0, node.children.size());
        assertEquals(channel2, node.channel);
        assertEquals(1, node2.children.size());
        assertEquals(subChannel1, node2.channel);
        node = (MixerNode) node2.children.get(0);
        assertEquals(0, node.children.size());
        assertEquals(channel, node.channel);
    }
}