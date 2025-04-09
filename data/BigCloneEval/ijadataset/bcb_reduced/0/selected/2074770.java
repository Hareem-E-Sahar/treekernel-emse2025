package javacode.net.sf.capit.detector.model.cache.distance.impl;

import javacode.cn.seed.pcap.parser.JPacket;
import javacode.net.sf.capit.detector.model.cache.distance.AbstractDistanceCache;
import javacode.net.sf.capit.detector.model.metric.distance.AbstractDistance;
import javacode.net.sf.capit.detector.model.pcap.filter.AbstractFilter;

public class NNDistanceCache extends AbstractDistanceCache {

    private double[][] distances;

    public NNDistanceCache(AbstractFilter filter, AbstractDistance distanceFunc) {
        super(distanceFunc, filter);
        int size = filter.getSize();
        distances = new double[size][size];
        for (int i = 0; i < size; i++) for (int j = 0; j < size; j++) distances[i][j] = -1;
    }

    @Override
    public double getDistance(JPacket packet1, JPacket packet2) {
        int packet1Id = packet1.getId();
        int packet2Id = packet2.getId();
        if (distances[packet1Id][packet2Id] == -1) {
            setDistance(packet1, packet2);
            distances[packet2Id][packet1Id] = distances[packet1Id][packet2Id];
        } else {
        }
        return distances[packet1Id][packet2Id];
    }

    @Override
    protected void setDistance(JPacket packet1, JPacket packet2) {
        int packet1Id = filter.getPacketId(packet1);
        int packet2Id = filter.getPacketId(packet2);
        double distance = distanceFunc.getDistance(packet1, packet2);
        distances[packet1Id][packet2Id] = distance;
    }
}
