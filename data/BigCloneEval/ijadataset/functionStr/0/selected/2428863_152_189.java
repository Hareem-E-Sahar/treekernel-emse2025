public class Test {    @Test
    public void testChannelSortingTwoErrors() {
        List<NodeChannel> channels = new ArrayList<NodeChannel>();
        NodeChannel channelA = new NodeChannel("a");
        channelA.setProcessingOrder(1);
        NodeChannel channelB = new NodeChannel("b");
        channelB.setProcessingOrder(2);
        NodeChannel channelC = new NodeChannel("c");
        channelC.setProcessingOrder(3);
        channels.add(channelC);
        channels.add(channelB);
        channels.add(channelA);
        List<OutgoingBatch> batches = new ArrayList<OutgoingBatch>();
        OutgoingBatch batch1 = new OutgoingBatch("1", channelA.getChannelId(), Status.NE);
        batch1.setStatus(OutgoingBatch.Status.ER);
        batch1.setErrorFlag(true);
        batch1.setLastUpdatedTime(new Date());
        batches.add(batch1);
        AppUtils.sleep(50);
        OutgoingBatch batch2 = new OutgoingBatch("1", channelB.getChannelId(), Status.NE);
        batch2.setStatus(OutgoingBatch.Status.ER);
        batch2.setErrorFlag(true);
        batch2.setLastUpdatedTime(new Date());
        batches.add(batch2);
        OutgoingBatch batch3 = new OutgoingBatch("1", channelC.getChannelId(), Status.NE);
        batches.add(batch3);
        OutgoingBatches outgoingBatches = new OutgoingBatches(batches);
        outgoingBatches.sortChannels(channels);
        Assert.assertEquals(channelC, channels.get(0));
        Assert.assertEquals(channelA, channels.get(1));
        Assert.assertEquals(channelB, channels.get(2));
        AppUtils.sleep(50);
        batch1.setLastUpdatedTime(new Date());
        outgoingBatches.sortChannels(channels);
        Assert.assertEquals(channelC, channels.get(0));
        Assert.assertEquals(channelB, channels.get(1));
        Assert.assertEquals(channelA, channels.get(2));
    }
}