package org.xtreemfs.babudb.pbrpc;

public final class Replication {

    private Replication() {
    }

    public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    }

    public abstract static class RemoteAccessService implements com.google.protobuf.Service {

        protected RemoteAccessService() {
        }

        public interface Interface {

            public abstract void makePersistent(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database> done);

            public abstract void getDatabaseByName(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseName request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database> done);

            public abstract void getDatabaseById(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseId request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database> done);

            public abstract void getDatabases(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases> done);

            public abstract void lookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done);

            public abstract void plookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done);

            public abstract void plookupReverse(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done);

            public abstract void rlookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done);

            public abstract void rlookupReverse(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done);
        }

        public static com.google.protobuf.Service newReflectiveService(final Interface impl) {
            return new RemoteAccessService() {

                @Override
                public void makePersistent(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database> done) {
                    impl.makePersistent(controller, request, done);
                }

                @Override
                public void getDatabaseByName(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseName request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database> done) {
                    impl.getDatabaseByName(controller, request, done);
                }

                @Override
                public void getDatabaseById(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseId request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database> done) {
                    impl.getDatabaseById(controller, request, done);
                }

                @Override
                public void getDatabases(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases> done) {
                    impl.getDatabases(controller, request, done);
                }

                @Override
                public void lookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done) {
                    impl.lookup(controller, request, done);
                }

                @Override
                public void plookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done) {
                    impl.plookup(controller, request, done);
                }

                @Override
                public void plookupReverse(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done) {
                    impl.plookupReverse(controller, request, done);
                }

                @Override
                public void rlookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done) {
                    impl.rlookup(controller, request, done);
                }

                @Override
                public void rlookupReverse(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done) {
                    impl.rlookupReverse(controller, request, done);
                }
            };
        }

        public static com.google.protobuf.BlockingService newReflectiveBlockingService(final BlockingInterface impl) {
            return new com.google.protobuf.BlockingService() {

                public final com.google.protobuf.Descriptors.ServiceDescriptor getDescriptorForType() {
                    return getDescriptor();
                }

                public final com.google.protobuf.Message callBlockingMethod(com.google.protobuf.Descriptors.MethodDescriptor method, com.google.protobuf.RpcController controller, com.google.protobuf.Message request) throws com.google.protobuf.ServiceException {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException("Service.callBlockingMethod() given method descriptor for " + "wrong service type.");
                    }
                    switch(method.getIndex()) {
                        case 0:
                            return impl.makePersistent(controller, (org.xtreemfs.babudb.pbrpc.Common.emptyRequest) request);
                        case 1:
                            return impl.getDatabaseByName(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseName) request);
                        case 2:
                            return impl.getDatabaseById(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseId) request);
                        case 3:
                            return impl.getDatabases(controller, (org.xtreemfs.babudb.pbrpc.Common.emptyRequest) request);
                        case 4:
                            return impl.lookup(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup) request);
                        case 5:
                            return impl.plookup(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup) request);
                        case 6:
                            return impl.plookupReverse(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup) request);
                        case 7:
                            return impl.rlookup(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup) request);
                        case 8:
                            return impl.rlookupReverse(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup) request);
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }

                public final com.google.protobuf.Message getRequestPrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException("Service.getRequestPrototype() given method " + "descriptor for wrong service type.");
                    }
                    switch(method.getIndex()) {
                        case 0:
                            return org.xtreemfs.babudb.pbrpc.Common.emptyRequest.getDefaultInstance();
                        case 1:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseName.getDefaultInstance();
                        case 2:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseId.getDefaultInstance();
                        case 3:
                            return org.xtreemfs.babudb.pbrpc.Common.emptyRequest.getDefaultInstance();
                        case 4:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup.getDefaultInstance();
                        case 5:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup.getDefaultInstance();
                        case 6:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup.getDefaultInstance();
                        case 7:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup.getDefaultInstance();
                        case 8:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup.getDefaultInstance();
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }

                public final com.google.protobuf.Message getResponsePrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException("Service.getResponsePrototype() given method " + "descriptor for wrong service type.");
                    }
                    switch(method.getIndex()) {
                        case 0:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance();
                        case 1:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance();
                        case 2:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance();
                        case 3:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases.getDefaultInstance();
                        case 4:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance();
                        case 5:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance();
                        case 6:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance();
                        case 7:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance();
                        case 8:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance();
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }
            };
        }

        public abstract void makePersistent(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database> done);

        public abstract void getDatabaseByName(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseName request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database> done);

        public abstract void getDatabaseById(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseId request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database> done);

        public abstract void getDatabases(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases> done);

        public abstract void lookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done);

        public abstract void plookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done);

        public abstract void plookupReverse(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done);

        public abstract void rlookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done);

        public abstract void rlookupReverse(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done);

        public static final com.google.protobuf.Descriptors.ServiceDescriptor getDescriptor() {
            return org.xtreemfs.babudb.pbrpc.Replication.getDescriptor().getServices().get(0);
        }

        public final com.google.protobuf.Descriptors.ServiceDescriptor getDescriptorForType() {
            return getDescriptor();
        }

        public final void callMethod(com.google.protobuf.Descriptors.MethodDescriptor method, com.google.protobuf.RpcController controller, com.google.protobuf.Message request, com.google.protobuf.RpcCallback<com.google.protobuf.Message> done) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("Service.callMethod() given method descriptor for wrong " + "service type.");
            }
            switch(method.getIndex()) {
                case 0:
                    this.makePersistent(controller, (org.xtreemfs.babudb.pbrpc.Common.emptyRequest) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database>specializeCallback(done));
                    return;
                case 1:
                    this.getDatabaseByName(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseName) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database>specializeCallback(done));
                    return;
                case 2:
                    this.getDatabaseById(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseId) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database>specializeCallback(done));
                    return;
                case 3:
                    this.getDatabases(controller, (org.xtreemfs.babudb.pbrpc.Common.emptyRequest) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases>specializeCallback(done));
                    return;
                case 4:
                    this.lookup(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse>specializeCallback(done));
                    return;
                case 5:
                    this.plookup(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap>specializeCallback(done));
                    return;
                case 6:
                    this.plookupReverse(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap>specializeCallback(done));
                    return;
                case 7:
                    this.rlookup(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap>specializeCallback(done));
                    return;
                case 8:
                    this.rlookupReverse(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap>specializeCallback(done));
                    return;
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public final com.google.protobuf.Message getRequestPrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("Service.getRequestPrototype() given method " + "descriptor for wrong service type.");
            }
            switch(method.getIndex()) {
                case 0:
                    return org.xtreemfs.babudb.pbrpc.Common.emptyRequest.getDefaultInstance();
                case 1:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseName.getDefaultInstance();
                case 2:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseId.getDefaultInstance();
                case 3:
                    return org.xtreemfs.babudb.pbrpc.Common.emptyRequest.getDefaultInstance();
                case 4:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup.getDefaultInstance();
                case 5:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup.getDefaultInstance();
                case 6:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup.getDefaultInstance();
                case 7:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup.getDefaultInstance();
                case 8:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup.getDefaultInstance();
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public final com.google.protobuf.Message getResponsePrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("Service.getResponsePrototype() given method " + "descriptor for wrong service type.");
            }
            switch(method.getIndex()) {
                case 0:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance();
                case 1:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance();
                case 2:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance();
                case 3:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases.getDefaultInstance();
                case 4:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance();
                case 5:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance();
                case 6:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance();
                case 7:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance();
                case 8:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance();
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public static Stub newStub(com.google.protobuf.RpcChannel channel) {
            return new Stub(channel);
        }

        public static final class Stub extends org.xtreemfs.babudb.pbrpc.Replication.RemoteAccessService implements Interface {

            private Stub(com.google.protobuf.RpcChannel channel) {
                this.channel = channel;
            }

            private final com.google.protobuf.RpcChannel channel;

            public com.google.protobuf.RpcChannel getChannel() {
                return channel;
            }

            public void makePersistent(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database> done) {
                channel.callMethod(getDescriptor().getMethods().get(0), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance()));
            }

            public void getDatabaseByName(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseName request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database> done) {
                channel.callMethod(getDescriptor().getMethods().get(1), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance()));
            }

            public void getDatabaseById(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseId request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Database> done) {
                channel.callMethod(getDescriptor().getMethods().get(2), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance()));
            }

            public void getDatabases(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases> done) {
                channel.callMethod(getDescriptor().getMethods().get(3), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases.getDefaultInstance()));
            }

            public void lookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done) {
                channel.callMethod(getDescriptor().getMethods().get(4), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance()));
            }

            public void plookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done) {
                channel.callMethod(getDescriptor().getMethods().get(5), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance()));
            }

            public void plookupReverse(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done) {
                channel.callMethod(getDescriptor().getMethods().get(6), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance()));
            }

            public void rlookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done) {
                channel.callMethod(getDescriptor().getMethods().get(7), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance()));
            }

            public void rlookupReverse(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap> done) {
                channel.callMethod(getDescriptor().getMethods().get(8), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance()));
            }
        }

        public static BlockingInterface newBlockingStub(com.google.protobuf.BlockingRpcChannel channel) {
            return new BlockingStub(channel);
        }

        public interface BlockingInterface {

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.Database makePersistent(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.Database getDatabaseByName(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseName request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.Database getDatabaseById(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseId request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases getDatabases(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse lookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap plookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap plookupReverse(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap rlookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap rlookupReverse(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup request) throws com.google.protobuf.ServiceException;
        }

        private static final class BlockingStub implements BlockingInterface {

            private BlockingStub(com.google.protobuf.BlockingRpcChannel channel) {
                this.channel = channel;
            }

            private final com.google.protobuf.BlockingRpcChannel channel;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.Database makePersistent(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.Database) channel.callBlockingMethod(getDescriptor().getMethods().get(0), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.Database getDatabaseByName(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseName request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.Database) channel.callBlockingMethod(getDescriptor().getMethods().get(1), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.Database getDatabaseById(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.DatabaseId request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.Database) channel.callBlockingMethod(getDescriptor().getMethods().get(2), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.Database.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases getDatabases(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases) channel.callBlockingMethod(getDescriptor().getMethods().get(3), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.Databases.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse lookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse) channel.callBlockingMethod(getDescriptor().getMethods().get(4), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap plookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap) channel.callBlockingMethod(getDescriptor().getMethods().get(5), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap plookupReverse(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Lookup request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap) channel.callBlockingMethod(getDescriptor().getMethods().get(6), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap rlookup(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap) channel.callBlockingMethod(getDescriptor().getMethods().get(7), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap rlookupReverse(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.RangeLookup request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap) channel.callBlockingMethod(getDescriptor().getMethods().get(8), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.EntryMap.getDefaultInstance());
            }
        }
    }

    public abstract static class ReplicationService implements com.google.protobuf.Service {

        protected ReplicationService() {
        }

        public interface Interface {

            public abstract void state(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN> done);

            public abstract void load(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas> done);

            public abstract void chunk(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Chunk request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done);

            public abstract void flease(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.FLease request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done);

            public abstract void localTime(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp> done);

            public abstract void replica(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSNRange request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries> done);

            public abstract void heartbeat(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done);

            public abstract void replicate(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done);

            public abstract void synchronize(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done);

            public abstract void volatileState(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN> done);
        }

        public static com.google.protobuf.Service newReflectiveService(final Interface impl) {
            return new ReplicationService() {

                @Override
                public void state(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN> done) {
                    impl.state(controller, request, done);
                }

                @Override
                public void load(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas> done) {
                    impl.load(controller, request, done);
                }

                @Override
                public void chunk(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Chunk request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done) {
                    impl.chunk(controller, request, done);
                }

                @Override
                public void flease(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.FLease request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done) {
                    impl.flease(controller, request, done);
                }

                @Override
                public void localTime(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp> done) {
                    impl.localTime(controller, request, done);
                }

                @Override
                public void replica(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSNRange request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries> done) {
                    impl.replica(controller, request, done);
                }

                @Override
                public void heartbeat(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done) {
                    impl.heartbeat(controller, request, done);
                }

                @Override
                public void replicate(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done) {
                    impl.replicate(controller, request, done);
                }

                @Override
                public void synchronize(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done) {
                    impl.synchronize(controller, request, done);
                }

                @Override
                public void volatileState(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN> done) {
                    impl.volatileState(controller, request, done);
                }
            };
        }

        public static com.google.protobuf.BlockingService newReflectiveBlockingService(final BlockingInterface impl) {
            return new com.google.protobuf.BlockingService() {

                public final com.google.protobuf.Descriptors.ServiceDescriptor getDescriptorForType() {
                    return getDescriptor();
                }

                public final com.google.protobuf.Message callBlockingMethod(com.google.protobuf.Descriptors.MethodDescriptor method, com.google.protobuf.RpcController controller, com.google.protobuf.Message request) throws com.google.protobuf.ServiceException {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException("Service.callBlockingMethod() given method descriptor for " + "wrong service type.");
                    }
                    switch(method.getIndex()) {
                        case 0:
                            return impl.state(controller, (org.xtreemfs.babudb.pbrpc.Common.emptyRequest) request);
                        case 1:
                            return impl.load(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN) request);
                        case 2:
                            return impl.chunk(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.Chunk) request);
                        case 3:
                            return impl.flease(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.FLease) request);
                        case 4:
                            return impl.localTime(controller, (org.xtreemfs.babudb.pbrpc.Common.emptyRequest) request);
                        case 5:
                            return impl.replica(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.LSNRange) request);
                        case 6:
                            return impl.heartbeat(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage) request);
                        case 7:
                            return impl.replicate(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN) request);
                        case 8:
                            return impl.synchronize(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage) request);
                        case 9:
                            return impl.volatileState(controller, (org.xtreemfs.babudb.pbrpc.Common.emptyRequest) request);
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }

                public final com.google.protobuf.Message getRequestPrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException("Service.getRequestPrototype() given method " + "descriptor for wrong service type.");
                    }
                    switch(method.getIndex()) {
                        case 0:
                            return org.xtreemfs.babudb.pbrpc.Common.emptyRequest.getDefaultInstance();
                        case 1:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance();
                        case 2:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.Chunk.getDefaultInstance();
                        case 3:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.FLease.getDefaultInstance();
                        case 4:
                            return org.xtreemfs.babudb.pbrpc.Common.emptyRequest.getDefaultInstance();
                        case 5:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.LSNRange.getDefaultInstance();
                        case 6:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage.getDefaultInstance();
                        case 7:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance();
                        case 8:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage.getDefaultInstance();
                        case 9:
                            return org.xtreemfs.babudb.pbrpc.Common.emptyRequest.getDefaultInstance();
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }

                public final com.google.protobuf.Message getResponsePrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException("Service.getResponsePrototype() given method " + "descriptor for wrong service type.");
                    }
                    switch(method.getIndex()) {
                        case 0:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance();
                        case 1:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas.getDefaultInstance();
                        case 2:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance();
                        case 3:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance();
                        case 4:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp.getDefaultInstance();
                        case 5:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries.getDefaultInstance();
                        case 6:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance();
                        case 7:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance();
                        case 8:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance();
                        case 9:
                            return org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance();
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }
            };
        }

        public abstract void state(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN> done);

        public abstract void load(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas> done);

        public abstract void chunk(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Chunk request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done);

        public abstract void flease(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.FLease request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done);

        public abstract void localTime(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp> done);

        public abstract void replica(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSNRange request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries> done);

        public abstract void heartbeat(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done);

        public abstract void replicate(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done);

        public abstract void synchronize(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done);

        public abstract void volatileState(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN> done);

        public static final com.google.protobuf.Descriptors.ServiceDescriptor getDescriptor() {
            return org.xtreemfs.babudb.pbrpc.Replication.getDescriptor().getServices().get(1);
        }

        public final com.google.protobuf.Descriptors.ServiceDescriptor getDescriptorForType() {
            return getDescriptor();
        }

        public final void callMethod(com.google.protobuf.Descriptors.MethodDescriptor method, com.google.protobuf.RpcController controller, com.google.protobuf.Message request, com.google.protobuf.RpcCallback<com.google.protobuf.Message> done) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("Service.callMethod() given method descriptor for wrong " + "service type.");
            }
            switch(method.getIndex()) {
                case 0:
                    this.state(controller, (org.xtreemfs.babudb.pbrpc.Common.emptyRequest) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN>specializeCallback(done));
                    return;
                case 1:
                    this.load(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas>specializeCallback(done));
                    return;
                case 2:
                    this.chunk(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.Chunk) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse>specializeCallback(done));
                    return;
                case 3:
                    this.flease(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.FLease) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse>specializeCallback(done));
                    return;
                case 4:
                    this.localTime(controller, (org.xtreemfs.babudb.pbrpc.Common.emptyRequest) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp>specializeCallback(done));
                    return;
                case 5:
                    this.replica(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.LSNRange) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries>specializeCallback(done));
                    return;
                case 6:
                    this.heartbeat(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse>specializeCallback(done));
                    return;
                case 7:
                    this.replicate(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse>specializeCallback(done));
                    return;
                case 8:
                    this.synchronize(controller, (org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse>specializeCallback(done));
                    return;
                case 9:
                    this.volatileState(controller, (org.xtreemfs.babudb.pbrpc.Common.emptyRequest) request, com.google.protobuf.RpcUtil.<org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN>specializeCallback(done));
                    return;
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public final com.google.protobuf.Message getRequestPrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("Service.getRequestPrototype() given method " + "descriptor for wrong service type.");
            }
            switch(method.getIndex()) {
                case 0:
                    return org.xtreemfs.babudb.pbrpc.Common.emptyRequest.getDefaultInstance();
                case 1:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance();
                case 2:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.Chunk.getDefaultInstance();
                case 3:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.FLease.getDefaultInstance();
                case 4:
                    return org.xtreemfs.babudb.pbrpc.Common.emptyRequest.getDefaultInstance();
                case 5:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.LSNRange.getDefaultInstance();
                case 6:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage.getDefaultInstance();
                case 7:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance();
                case 8:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage.getDefaultInstance();
                case 9:
                    return org.xtreemfs.babudb.pbrpc.Common.emptyRequest.getDefaultInstance();
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public final com.google.protobuf.Message getResponsePrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("Service.getResponsePrototype() given method " + "descriptor for wrong service type.");
            }
            switch(method.getIndex()) {
                case 0:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance();
                case 1:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas.getDefaultInstance();
                case 2:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance();
                case 3:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance();
                case 4:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp.getDefaultInstance();
                case 5:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries.getDefaultInstance();
                case 6:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance();
                case 7:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance();
                case 8:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance();
                case 9:
                    return org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance();
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public static Stub newStub(com.google.protobuf.RpcChannel channel) {
            return new Stub(channel);
        }

        public static final class Stub extends org.xtreemfs.babudb.pbrpc.Replication.ReplicationService implements Interface {

            private Stub(com.google.protobuf.RpcChannel channel) {
                this.channel = channel;
            }

            private final com.google.protobuf.RpcChannel channel;

            public com.google.protobuf.RpcChannel getChannel() {
                return channel;
            }

            public void state(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN> done) {
                channel.callMethod(getDescriptor().getMethods().get(0), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance()));
            }

            public void load(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas> done) {
                channel.callMethod(getDescriptor().getMethods().get(1), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas.getDefaultInstance()));
            }

            public void chunk(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Chunk request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done) {
                channel.callMethod(getDescriptor().getMethods().get(2), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance()));
            }

            public void flease(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.FLease request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done) {
                channel.callMethod(getDescriptor().getMethods().get(3), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance()));
            }

            public void localTime(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp> done) {
                channel.callMethod(getDescriptor().getMethods().get(4), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp.getDefaultInstance()));
            }

            public void replica(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSNRange request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries> done) {
                channel.callMethod(getDescriptor().getMethods().get(5), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries.getDefaultInstance()));
            }

            public void heartbeat(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done) {
                channel.callMethod(getDescriptor().getMethods().get(6), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance()));
            }

            public void replicate(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done) {
                channel.callMethod(getDescriptor().getMethods().get(7), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance()));
            }

            public void synchronize(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse> done) {
                channel.callMethod(getDescriptor().getMethods().get(8), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance()));
            }

            public void volatileState(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request, com.google.protobuf.RpcCallback<org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN> done) {
                channel.callMethod(getDescriptor().getMethods().get(9), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.class, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance()));
            }
        }

        public static BlockingInterface newBlockingStub(com.google.protobuf.BlockingRpcChannel channel) {
            return new BlockingStub(channel);
        }

        public interface BlockingInterface {

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN state(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas load(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse chunk(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Chunk request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse flease(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.FLease request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp localTime(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries replica(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSNRange request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse heartbeat(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse replicate(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse synchronize(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage request) throws com.google.protobuf.ServiceException;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN volatileState(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request) throws com.google.protobuf.ServiceException;
        }

        private static final class BlockingStub implements BlockingInterface {

            private BlockingStub(com.google.protobuf.BlockingRpcChannel channel) {
                this.channel = channel;
            }

            private final com.google.protobuf.BlockingRpcChannel channel;

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN state(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN) channel.callBlockingMethod(getDescriptor().getMethods().get(0), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas load(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas) channel.callBlockingMethod(getDescriptor().getMethods().get(1), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.DBFileMetaDatas.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse chunk(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.Chunk request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse) channel.callBlockingMethod(getDescriptor().getMethods().get(2), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse flease(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.FLease request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse) channel.callBlockingMethod(getDescriptor().getMethods().get(3), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp localTime(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp) channel.callBlockingMethod(getDescriptor().getMethods().get(4), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.Timestamp.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries replica(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSNRange request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries) channel.callBlockingMethod(getDescriptor().getMethods().get(5), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.LogEntries.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse heartbeat(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse) channel.callBlockingMethod(getDescriptor().getMethods().get(6), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse replicate(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse) channel.callBlockingMethod(getDescriptor().getMethods().get(7), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse synchronize(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.GlobalTypes.HeartbeatMessage request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse) channel.callBlockingMethod(getDescriptor().getMethods().get(8), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.ErrorCodeResponse.getDefaultInstance());
            }

            public org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN volatileState(com.google.protobuf.RpcController controller, org.xtreemfs.babudb.pbrpc.Common.emptyRequest request) throws com.google.protobuf.ServiceException {
                return (org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN) channel.callBlockingMethod(getDescriptor().getMethods().get(9), controller, request, org.xtreemfs.babudb.pbrpc.GlobalTypes.LSN.getDefaultInstance());
            }
        }
    }

    public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }

    private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

    static {
        java.lang.String[] descriptorData = { "\n\033interface/replication.proto\022\022org.xtree" + "mfs.pbrpc\032\033interface/GlobalTypes.proto\032%" + "share/foundation/include/Common.proto\032$s" + "hare/foundation/include/PBRPC.proto2\335\006\n\023" + "RemoteAccessService\022]\n\016makePersistent\022 ." + "org.xtreemfs.pbrpc.emptyRequest\032\034.org.xt" + "reemfs.pbrpc.Database\"\013\215\265\030\001\000\000\000\240\265\030\001\022\\\n\021ge" + "tDatabaseByName\022 .org.xtreemfs.pbrpc.Dat" + "abaseName\032\034.org.xtreemfs.pbrpc.Database\"" + "\007\215\265\030\002\000\000\000\022X\n\017getDatabaseById\022\036.org.xtreem", "fs.pbrpc.DatabaseId\032\034.org.xtreemfs.pbrpc" + ".Database\"\007\215\265\030\003\000\000\000\022X\n\014getDatabases\022 .org" + ".xtreemfs.pbrpc.emptyRequest\032\035.org.xtree" + "mfs.pbrpc.Databases\"\007\215\265\030\004\000\000\000\022\\\n\006lookup\022\032" + ".org.xtreemfs.pbrpc.Lookup\032%.org.xtreemf" + "s.pbrpc.ErrorCodeResponse\"\017\215\265\030\005\000\000\000\240\265\030\001\230\265" + "\030\001\022T\n\007plookup\022\032.org.xtreemfs.pbrpc.Looku" + "p\032\034.org.xtreemfs.pbrpc.EntryMap\"\017\215\265\030\006\000\000\000" + "\240\265\030\001\230\265\030\001\022[\n\016plookupReverse\022\032.org.xtreemf" + "s.pbrpc.Lookup\032\034.org.xtreemfs.pbrpc.Entr", "yMap\"\017\215\265\030\007\000\000\000\240\265\030\001\230\265\030\001\022Y\n\007rlookup\022\037.org.x" + "treemfs.pbrpc.RangeLookup\032\034.org.xtreemfs" + ".pbrpc.EntryMap\"\017\215\265\030\010\000\000\000\240\265\030\001\230\265\030\001\022`\n\016rloo" + "kupReverse\022\037.org.xtreemfs.pbrpc.RangeLoo" + "kup\032\034.org.xtreemfs.pbrpc.EntryMap\"\017\215\265\030\t\000" + "\000\000\240\265\030\001\230\265\030\001\032\007\225\265\030\021\'\000\0002\217\007\n\022ReplicationServi" + "ce\022K\n\005state\022 .org.xtreemfs.pbrpc.emptyRe" + "quest\032\027.org.xtreemfs.pbrpc.LSN\"\007\215\265\030\001\000\000\000\022" + "M\n\004load\022\027.org.xtreemfs.pbrpc.LSN\032#.org.x" + "treemfs.pbrpc.DBFileMetaDatas\"\007\215\265\030\002\000\000\000\022V", "\n\005chunk\022\031.org.xtreemfs.pbrpc.Chunk\032%.org" + ".xtreemfs.pbrpc.ErrorCodeResponse\"\013\215\265\030\003\000" + "\000\000\230\265\030\001\022X\n\006flease\022\032.org.xtreemfs.pbrpc.FL" + "ease\032%.org.xtreemfs.pbrpc.ErrorCodeRespo" + "nse\"\013\215\265\030\004\000\000\000\240\265\030\001\022U\n\tlocalTime\022 .org.xtre" + "emfs.pbrpc.emptyRequest\032\035.org.xtreemfs.p" + "brpc.Timestamp\"\007\215\265\030\005\000\000\000\022T\n\007replica\022\034.org" + ".xtreemfs.pbrpc.LSNRange\032\036.org.xtreemfs." + "pbrpc.LogEntries\"\013\215\265\030\006\000\000\000\230\265\030\001\022a\n\theartbe" + "at\022$.org.xtreemfs.pbrpc.HeartbeatMessage", "\032%.org.xtreemfs.pbrpc.ErrorCodeResponse\"" + "\007\215\265\030\007\000\000\000\022X\n\treplicate\022\027.org.xtreemfs.pbr" + "pc.LSN\032%.org.xtreemfs.pbrpc.ErrorCodeRes" + "ponse\"\013\215\265\030\010\000\000\000\240\265\030\001\022c\n\013synchronize\022$.org." + "xtreemfs.pbrpc.HeartbeatMessage\032%.org.xt" + "reemfs.pbrpc.ErrorCodeResponse\"\007\215\265\030\t\000\000\000\022" + "S\n\rvolatileState\022 .org.xtreemfs.pbrpc.em" + "ptyRequest\032\027.org.xtreemfs.pbrpc.LSN\"\007\215\265\030" + "\n\000\000\000\032\007\225\265\030!N\000\000B\033\n\031org.xtreemfs.babudb.pbr" + "pc" };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner = new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {

            public com.google.protobuf.ExtensionRegistry assignDescriptors(com.google.protobuf.Descriptors.FileDescriptor root) {
                descriptor = root;
                com.google.protobuf.ExtensionRegistry registry = com.google.protobuf.ExtensionRegistry.newInstance();
                registerAllExtensions(registry);
                org.xtreemfs.babudb.pbrpc.GlobalTypes.registerAllExtensions(registry);
                org.xtreemfs.babudb.pbrpc.Common.registerAllExtensions(registry);
                org.xtreemfs.babudb.pbrpc.generatedinterfaces.PBRPC.registerAllExtensions(registry);
                return registry;
            }
        };
        com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new com.google.protobuf.Descriptors.FileDescriptor[] { org.xtreemfs.babudb.pbrpc.GlobalTypes.getDescriptor(), org.xtreemfs.babudb.pbrpc.Common.getDescriptor(), org.xtreemfs.babudb.pbrpc.generatedinterfaces.PBRPC.getDescriptor() }, assigner);
    }

    public static void internalForceInit() {
    }
}
