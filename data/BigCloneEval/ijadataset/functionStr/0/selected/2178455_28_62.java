public class Test {    public void initialize(SchemaStore schemaStore, Message message, Channel channel, Binding binding, ModuleContext moduleContext) throws NamingException, ModuleException, CPAException, HeaderMappingException {
        final Tracer tracer = baseTracer.entering("initialize(SchemaStore schemaStore, Message message, Channel channel, Binding binding, ModuleContext moduleContext)");
        this.schemaStore = schemaStore;
        this.adapterType = channel.getAdapterType();
        getChannelInformation(message);
        getXsdNameFromModuleContext(moduleContext);
        retrieveXsdDocument();
        setUnbPartyInformation(moduleContext);
        getUnbSenderAndReceiverIds(moduleContext);
        this.testIndicator = moduleContext.getContextData(TEST_INDICATOR);
        if ((this.testIndicator == null) || ((!this.testIndicator.equals("0")) && (!this.testIndicator.equals("1")))) ErrorHelper.logErrorAndThrow(tracer, "Invalid entry for module context parameter " + TEST_INDICATOR); else tracer.info("Module context parameter read : key = {0}, value = {1}", new Object[] { TEST_INDICATOR, String.valueOf(this.testIndicator) });
        this.characterSet = moduleContext.getContextData(CHARACTER_SET);
        if (this.characterSet == null) {
            String errorMessage = "Invalid entry for module context parameter " + CHARACTER_SET;
            tracer.error(errorMessage);
            ModuleException me = new ModuleException(errorMessage);
            tracer.throwing(me);
            throw me;
        }
        tracer.info("Module context parameter read : key = {0}, value = {1}", new Object[] { CHARACTER_SET, String.valueOf(this.characterSet) });
        this.unbSenderRoutingAddress = moduleContext.getContextData(UNB_ROUTING_ADDR_SND);
        if ((this.unbSenderRoutingAddress == null) || (this.unbSenderRoutingAddress.trim().equals(""))) {
            tracer.info("Module context parameter has not been defined : key = {0}", UNB_ROUTING_ADDR_SND);
        } else tracer.info("Module context parameter read : key = {0}, value = {1}", new Object[] { UNB_ROUTING_ADDR_SND, String.valueOf(this.unbSenderRoutingAddress) });
        this.unbReceiverRoutingAddress = moduleContext.getContextData(UNB_ROUTING_ADDR_REC);
        if ((this.unbReceiverRoutingAddress == null) || (this.unbReceiverRoutingAddress.trim().equals(""))) {
            tracer.info("Module context parameter has not been defined : key = {0}", UNB_ROUTING_ADDR_REC);
        } else tracer.info("Module context parameter read : key = {0}, value = {1}", new Object[] { UNB_ROUTING_ADDR_REC, String.valueOf(this.unbReceiverRoutingAddress) });
        getMessageDirection(message);
        String strSchemaValidation = moduleContext.getContextData(SCHEMA_VALIDATION);
        this.schemaValidation = !strSchemaValidation.equals("0");
        tracer.info("Module context parameter read : key = {0}, value = {1}", new Object[] { SCHEMA_VALIDATION, String.valueOf(this.schemaValidation) });
        tracer.info("Set messageType in configuration to: " + this.messageType);
        tracer.leaving();
    }
}