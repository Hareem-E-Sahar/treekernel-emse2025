package biz.xsoftware.test.nio.tcp;

import java.util.HashMap;
import java.util.Map;
import biz.xsoftware.api.nio.ChannelManager;
import biz.xsoftware.api.nio.ChannelService;
import biz.xsoftware.api.nio.ChannelServiceFactory;
import biz.xsoftware.api.nio.Settings;
import biz.xsoftware.api.nio.libs.FactoryCreator;
import biz.xsoftware.api.nio.libs.PacketProcessorFactory;

public class TestZFailureExceptionCM extends ZNioFailureSuperclass {

    private ChannelServiceFactory factory;

    private PacketProcessorFactory procFactory;

    private Settings factoryHolder;

    public TestZFailureExceptionCM(String name) {
        super(name);
        ChannelServiceFactory basic = ChannelServiceFactory.createFactory(null);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_PACKET_CHANNEL_MGR);
        props.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, basic);
        ChannelServiceFactory packetFactory = ChannelServiceFactory.createFactory(props);
        Map<String, Object> props2 = new HashMap<String, Object>();
        props2.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_EXCEPTION_CHANNEL_MGR);
        props2.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, packetFactory);
        factory = ChannelServiceFactory.createFactory(props2);
        FactoryCreator creator = FactoryCreator.createFactory(null);
        procFactory = creator.createPacketProcFactory(null);
        factoryHolder = new Settings(null, procFactory);
    }

    @Override
    protected ChannelService getClientChanMgr() {
        Map<String, Object> p = new HashMap<String, Object>();
        p.put(ChannelManager.KEY_ID, "[client]");
        p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());
        return factory.createChannelManager(p);
    }

    @Override
    protected ChannelService getServerChanMgr() {
        Map<String, Object> p = new HashMap<String, Object>();
        p.put(ChannelManager.KEY_ID, "[server]");
        p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());
        return factory.createChannelManager(p);
    }

    @Override
    protected Settings getClientFactoryHolder() {
        return factoryHolder;
    }

    @Override
    protected Settings getServerFactoryHolder() {
        return factoryHolder;
    }

    @Override
    protected String getChannelImplName() {
        return "biz.xsoftware.impl.nio.cm.exception.ExcTCPChannel";
    }

    @Override
    protected String getServerChannelImplName() {
        return "biz.xsoftware.impl.nio.cm.exception.ExcTCPServerChannel";
    }
}
