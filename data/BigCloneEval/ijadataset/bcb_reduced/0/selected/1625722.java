package com.google.gwt.user.rebind.rpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.ConfigurationProperty;
import com.google.gwt.core.ext.GeneratorContextExt;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.EmittedArtifact.Visibility;
import com.google.gwt.core.ext.linker.GeneratedResource;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.generator.NameFactory;
import com.google.gwt.dev.javac.rebind.CachedClientDataMap;
import com.google.gwt.dev.util.Util;
import com.google.gwt.dev.util.log.speedtracer.CompilerEventType;
import com.google.gwt.dev.util.log.speedtracer.SpeedTracerLogger;
import com.google.gwt.dev.util.log.speedtracer.SpeedTracerLogger.Event;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.RpcToken;
import com.google.gwt.user.client.rpc.RpcToken.RpcTokenImplementation;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.FailedRequest;
import com.google.gwt.user.client.rpc.impl.FailingRequestBuilder;
import com.google.gwt.user.client.rpc.impl.RemoteServiceProxy;
import com.google.gwt.user.client.rpc.impl.RequestCallbackAdapter.ResponseReader;
import com.google.gwt.user.linker.rpc.RpcLogArtifact;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import com.google.gwt.user.server.rpc.impl.TypeNameObfuscator;

/**
 * ACHTUNG: Diese Klasse ist eine 1:1 Kopie der {@link ProxyCreator} Klasse, mit dem EINZIGEN Unterschied dass die
 * methode getSourceWriter abstakt gemacht wurde und der urspruengliche code entfernt wurde (diese muss ueberschreiben
 * werden).
 * 
 * Creates a client-side proxy for a {@link com.google.gwt.user.client.rpc.RemoteService RemoteService} interface as
 * well as the necessary type and field serializers.
 */
public abstract class ProxyCreatorCp {

    /**
	 * The directory within which RPC manifests are placed for individual permutations.
	 */
    public static final String MANIFEST_ARTIFACT_DIR = "rpcPolicyManifest/manifests";

    private static final Map<JPrimitiveType, ResponseReader> JPRIMITIVETYPE_TO_RESPONSEREADER = new HashMap<JPrimitiveType, ResponseReader>();

    private static final String PROXY_SUFFIX = "_Proxy";

    /**
	 * Adds a root type for each type that appears in the RemoteService interface methods.
	 */
    private static void addRemoteServiceRootTypes(TreeLogger logger, TypeOracle typeOracle, SerializableTypeOracleBuilder typesSentFromBrowser, SerializableTypeOracleBuilder typesSentToBrowser, JClassType remoteService) throws NotFoundException, UnableToCompleteException {
        logger = logger.branch(TreeLogger.DEBUG, "Analyzing '" + remoteService.getParameterizedQualifiedSourceName() + "' for serializable types", null);
        JMethod[] methods = remoteService.getOverridableMethods();
        JClassType exceptionClass = typeOracle.getType(Exception.class.getName());
        JClassType rteType = typeOracle.getType(RpcTokenException.class.getName());
        JClassType rpcTokenClass = typeOracle.getType(RpcToken.class.getName());
        RpcTokenImplementation tokenClassToUse = remoteService.findAnnotationInTypeHierarchy(RpcTokenImplementation.class);
        if (tokenClassToUse != null) {
            JClassType rpcTokenType = typeOracle.getType(tokenClassToUse.value());
            if (rpcTokenType.isAssignableTo(rpcTokenClass)) {
                typesSentFromBrowser.addRootType(logger, rpcTokenType);
                typesSentToBrowser.addRootType(logger, rteType);
            } else {
                logger.branch(TreeLogger.ERROR, "RPC token class " + tokenClassToUse.value() + " must implement " + RpcToken.class.getName(), null);
                throw new UnableToCompleteException();
            }
        } else {
            JClassType[] rpcTokenSubclasses = rpcTokenClass.getSubtypes();
            for (JClassType rpcTokenSubclass : rpcTokenSubclasses) {
                typesSentFromBrowser.addRootType(logger, rpcTokenSubclass);
            }
            if (rpcTokenSubclasses.length > 0) {
                typesSentToBrowser.addRootType(logger, rteType);
            }
        }
        TreeLogger validationLogger = logger.branch(TreeLogger.DEBUG, "Analyzing methods:", null);
        for (JMethod method : methods) {
            TreeLogger methodLogger = validationLogger.branch(TreeLogger.DEBUG, method.toString(), null);
            JType returnType = method.getReturnType();
            if (returnType != JPrimitiveType.VOID) {
                TreeLogger returnTypeLogger = methodLogger.branch(TreeLogger.DEBUG, "Return type: " + returnType.getParameterizedQualifiedSourceName(), null);
                typesSentToBrowser.addRootType(returnTypeLogger, returnType);
            }
            JParameter[] params = method.getParameters();
            for (JParameter param : params) {
                TreeLogger paramLogger = methodLogger.branch(TreeLogger.DEBUG, "Parameter: " + param.toString(), null);
                JType paramType = param.getType();
                typesSentFromBrowser.addRootType(paramLogger, paramType);
            }
            JType[] exs = method.getThrows();
            if (exs.length > 0) {
                TreeLogger throwsLogger = methodLogger.branch(TreeLogger.DEBUG, "Throws:", null);
                for (JType ex : exs) {
                    if (!exceptionClass.isAssignableFrom(ex.isClass())) {
                        throwsLogger = throwsLogger.branch(TreeLogger.WARN, "'" + ex.getQualifiedSourceName() + "' is not a checked exception; only checked exceptions may be used", null);
                    }
                    typesSentToBrowser.addRootType(throwsLogger, ex);
                }
            }
        }
    }

    /**
	 * Add the implicit root types that are needed to make RPC work. These would be {@link String} and
	 * {@link IncompatibleRemoteServiceException}.
	 */
    private static void addRequiredRoots(TreeLogger logger, TypeOracle typeOracle, SerializableTypeOracleBuilder stob) throws NotFoundException {
        logger = logger.branch(TreeLogger.DEBUG, "Analyzing implicit types");
        JClassType stringType = typeOracle.getType(String.class.getName());
        stob.addRootType(logger, stringType);
        JClassType icseType = typeOracle.getType(IncompatibleRemoteServiceException.class.getName());
        stob.addRootType(logger, icseType);
    }

    /**
	 * Take the union of two type arrays, and then sort the results alphabetically.
	 */
    private static JType[] unionOfTypeArrays(JType[]... types) {
        Set<JType> typesList = new HashSet<JType>();
        for (JType[] a : types) {
            typesList.addAll(Arrays.asList(a));
        }
        JType[] serializableTypes = typesList.toArray(new JType[0]);
        Arrays.sort(serializableTypes, SerializableTypeOracleBuilder.JTYPE_COMPARATOR);
        return serializableTypes;
    }

    protected JClassType serviceIntf;

    private boolean elideTypeNames;

    private Map<String, Long> cachedTypeLastModifiedTimes = null;

    /**
	 * The possibly obfuscated type signatures used to represent a type.
	 */
    private Map<JType, String> typeStrings;

    {
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(JPrimitiveType.BOOLEAN, ResponseReader.BOOLEAN);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(JPrimitiveType.BYTE, ResponseReader.BYTE);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(JPrimitiveType.CHAR, ResponseReader.CHAR);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(JPrimitiveType.DOUBLE, ResponseReader.DOUBLE);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(JPrimitiveType.FLOAT, ResponseReader.FLOAT);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(JPrimitiveType.INT, ResponseReader.INT);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(JPrimitiveType.LONG, ResponseReader.LONG);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(JPrimitiveType.SHORT, ResponseReader.SHORT);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(JPrimitiveType.VOID, ResponseReader.VOID);
    }

    public ProxyCreatorCp(JClassType serviceIntf) {
        assert (serviceIntf.isInterface() != null);
        this.serviceIntf = serviceIntf;
    }

    /**
	 * Creates the client-side proxy class.
	 * 
	 * @throws UnableToCompleteException
	 */
    public String create(TreeLogger logger, GeneratorContextExt context) throws UnableToCompleteException {
        TypeOracle typeOracle = context.getTypeOracle();
        JClassType serviceAsync = typeOracle.findType(serviceIntf.getQualifiedSourceName() + "Async");
        if (serviceAsync == null) {
            logger.branch(TreeLogger.ERROR, "Could not find an asynchronous version for the service interface " + serviceIntf.getQualifiedSourceName(), null);
            RemoteServiceAsyncValidator.logValidAsyncInterfaceDeclaration(logger, serviceIntf);
            throw new UnableToCompleteException();
        }
        SourceWriter srcWriter = getSourceWriter(logger, context, serviceAsync);
        if (srcWriter == null) {
            return getProxyQualifiedName();
        }
        RemoteServiceAsyncValidator rsav = new RemoteServiceAsyncValidator(logger, typeOracle);
        Map<JMethod, JMethod> syncMethToAsyncMethMap = rsav.validate(logger, serviceIntf, serviceAsync);
        final PropertyOracle propertyOracle = context.getPropertyOracle();
        TypeFilter blacklistTypeFilter = new BlacklistTypeFilter(logger, propertyOracle);
        Event event = SpeedTracerLogger.start(CompilerEventType.GENERATOR_RPC_STOB);
        SerializableTypeOracleBuilder typesSentFromBrowserBuilder = new SerializableTypeOracleBuilder(logger, propertyOracle, context);
        typesSentFromBrowserBuilder.setTypeFilter(blacklistTypeFilter);
        SerializableTypeOracleBuilder typesSentToBrowserBuilder = new SerializableTypeOracleBuilder(logger, propertyOracle, context);
        typesSentToBrowserBuilder.setTypeFilter(blacklistTypeFilter);
        addRoots(logger, typeOracle, typesSentFromBrowserBuilder, typesSentToBrowserBuilder);
        try {
            ConfigurationProperty prop = context.getPropertyOracle().getConfigurationProperty(TypeSerializerCreator.GWT_ELIDE_TYPE_NAMES_FROM_RPC);
            elideTypeNames = Boolean.parseBoolean(prop.getValues().get(0));
        } catch (BadPropertyValueException e) {
            logger.log(TreeLogger.ERROR, "Configuration property " + TypeSerializerCreator.GWT_ELIDE_TYPE_NAMES_FROM_RPC + " is not defined. Is RemoteService.gwt.xml inherited?");
            throw new UnableToCompleteException();
        }
        SerializableTypeOracle typesSentFromBrowser;
        SerializableTypeOracle typesSentToBrowser;
        String rpcLog;
        {
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            typesSentFromBrowserBuilder.setLogOutputWriter(writer);
            typesSentToBrowserBuilder.setLogOutputWriter(writer);
            writer.write("====================================\n");
            writer.write("Types potentially sent from browser:\n");
            writer.write("====================================\n\n");
            writer.flush();
            typesSentFromBrowser = typesSentFromBrowserBuilder.build(logger);
            writer.write("===================================\n");
            writer.write("Types potentially sent from server:\n");
            writer.write("===================================\n\n");
            writer.flush();
            typesSentToBrowser = typesSentToBrowserBuilder.build(logger);
            writer.close();
            rpcLog = stringWriter.toString();
        }
        event.end();
        generateTypeHandlers(logger, context, typesSentFromBrowser, typesSentToBrowser);
        String serializationPolicyStrongName = writeSerializationPolicyFile(logger, context, typesSentFromBrowser, typesSentToBrowser);
        String remoteServiceInterfaceName = elideTypeNames ? TypeNameObfuscator.SERVICE_INTERFACE_ID : SerializationUtils.getRpcTypeName(serviceIntf);
        generateProxyFields(srcWriter, typesSentFromBrowser, serializationPolicyStrongName, remoteServiceInterfaceName);
        generateProxyContructor(srcWriter);
        generateProxyMethods(srcWriter, typesSentFromBrowser, typeOracle, syncMethToAsyncMethMap);
        generateStreamWriterOverride(srcWriter);
        generateCheckRpcTokenTypeOverride(srcWriter, typeOracle, typesSentFromBrowser);
        srcWriter.commit(logger);
        if (context.isProdMode() || logger.isLoggable(TreeLogger.DEBUG)) {
            context.commitArtifact(logger, new RpcLogArtifact(serviceIntf.getQualifiedSourceName(), serializationPolicyStrongName, rpcLog));
        }
        return getProxyQualifiedName();
    }

    public void updateResultCacheData(CachedClientDataMap clientData) {
        if (cachedTypeLastModifiedTimes != null) {
            clientData.put(TypeSerializerCreator.CACHED_TYPE_INFO_KEY, cachedTypeLastModifiedTimes);
        }
    }

    protected void addRoots(TreeLogger logger, TypeOracle typeOracle, SerializableTypeOracleBuilder typesSentFromBrowserBuilder, SerializableTypeOracleBuilder typesSentToBrowserBuilder) throws UnableToCompleteException {
        try {
            addRequiredRoots(logger, typeOracle, typesSentFromBrowserBuilder);
            addRequiredRoots(logger, typeOracle, typesSentToBrowserBuilder);
            addRemoteServiceRootTypes(logger, typeOracle, typesSentFromBrowserBuilder, typesSentToBrowserBuilder, serviceIntf);
        } catch (NotFoundException e) {
            logger.log(TreeLogger.ERROR, "Unable to find type referenced from remote service", e);
            throw new UnableToCompleteException();
        }
    }

    protected String computeTypeNameExpression(JType paramType) {
        String typeName;
        if (typeStrings.containsKey(paramType)) {
            typeName = typeStrings.get(paramType);
        } else {
            typeName = SerializationUtils.getRpcTypeName(paramType);
        }
        return typeName == null ? null : ('"' + typeName + '"');
    }

    protected void generateCheckRpcTokenTypeOverride(SourceWriter srcWriter, TypeOracle typeOracle, SerializableTypeOracle typesSentFromBrowser) {
        JClassType rpcTokenType = typeOracle.findType(RpcToken.class.getName());
        JClassType[] rpcTokenSubtypes = rpcTokenType.getSubtypes();
        String rpcTokenImplementation = "";
        for (JClassType rpcTokenSubtype : rpcTokenSubtypes) {
            if (typesSentFromBrowser.isSerializable(rpcTokenSubtype)) {
                if (rpcTokenImplementation.length() > 0) {
                    rpcTokenImplementation = "";
                    break;
                } else {
                    rpcTokenImplementation = rpcTokenSubtype.getQualifiedSourceName();
                }
            }
        }
        if (rpcTokenImplementation.length() > 0) {
            srcWriter.println("@Override");
            srcWriter.println("protected void checkRpcTokenType(RpcToken token) {");
            srcWriter.indent();
            srcWriter.println("if (!(token instanceof " + rpcTokenImplementation + ")) {");
            srcWriter.indent();
            srcWriter.println("throw new RpcTokenException(\"Invalid RpcToken type: " + "expected '" + rpcTokenImplementation + "' but got '\" + " + "token.getClass() + \"'\");");
            srcWriter.outdent();
            srcWriter.println("}");
            srcWriter.outdent();
            srcWriter.println("}");
        }
    }

    /**
	 * Generate the proxy constructor and delegate to the superclass constructor using the default address for the
	 * {@link com.google.gwt.user.client.rpc.RemoteService RemoteService}.
	 */
    protected void generateProxyContructor(SourceWriter srcWriter) {
        srcWriter.println("public " + getProxySimpleName() + "() {");
        srcWriter.indent();
        srcWriter.println("super(GWT.getModuleBaseURL(),");
        srcWriter.indent();
        srcWriter.println(getRemoteServiceRelativePath() + ", ");
        srcWriter.println("SERIALIZATION_POLICY, ");
        srcWriter.println("SERIALIZER);");
        srcWriter.outdent();
        srcWriter.outdent();
        srcWriter.println("}");
    }

    /**
	 * Generate any fields required by the proxy.
	 * 
	 * @param serializableTypeOracle
	 *            the type oracle
	 */
    protected void generateProxyFields(SourceWriter srcWriter, SerializableTypeOracle serializableTypeOracle, String serializationPolicyStrongName, String remoteServiceInterfaceName) {
        srcWriter.println("private static final String REMOTE_SERVICE_INTERFACE_NAME = " + "\"" + remoteServiceInterfaceName + "\";");
        srcWriter.println("private static final String SERIALIZATION_POLICY =\"" + serializationPolicyStrongName + "\";");
        String typeSerializerName = SerializationUtils.getTypeSerializerQualifiedName(serviceIntf);
        srcWriter.println("private static final " + typeSerializerName + " SERIALIZER = new " + typeSerializerName + "();");
        srcWriter.println();
    }

    /**
	 * Generates the client's asynchronous proxy method.
	 * 
	 * @param serializableTypeOracle
	 *            the type oracle
	 */
    protected void generateProxyMethod(SourceWriter w, SerializableTypeOracle serializableTypeOracle, TypeOracle typeOracle, JMethod syncMethod, JMethod asyncMethod) {
        w.println();
        JType asyncReturnType = asyncMethod.getReturnType().getErasedType();
        w.print("public ");
        w.print(asyncReturnType.getQualifiedSourceName());
        w.print(" ");
        w.print(asyncMethod.getName() + "(");
        boolean needsComma = false;
        NameFactory nameFactory = new NameFactory();
        JParameter[] asyncParams = asyncMethod.getParameters();
        for (int i = 0; i < asyncParams.length; ++i) {
            JParameter param = asyncParams[i];
            if (needsComma) {
                w.print(", ");
            } else {
                needsComma = true;
            }
            JType paramType = param.getType();
            paramType = paramType.getErasedType();
            w.print(paramType.getQualifiedSourceName());
            w.print(" ");
            String paramName = param.getName();
            nameFactory.addName(paramName);
            w.print(paramName);
        }
        w.println(") {");
        w.indent();
        String helperName = nameFactory.createName("helper");
        String helperClassName = RemoteServiceProxy.ServiceHelper.class.getCanonicalName();
        w.println("%s %s = new %s(\"%s\", \"%s\");", helperClassName, helperName, helperClassName, getProxySimpleName(), syncMethod.getName());
        w.println("try {");
        w.indent();
        JParameter[] syncParams = syncMethod.getParameters();
        String streamWriterName = nameFactory.createName("streamWriter");
        w.println("%s %s = %s.start(REMOTE_SERVICE_INTERFACE_NAME, %s);", SerializationStreamWriter.class.getSimpleName(), streamWriterName, helperName, syncParams.length);
        for (JParameter param : syncParams) {
            JType paramType = param.getType().getErasedType();
            String typeNameExpression = computeTypeNameExpression(paramType);
            assert typeNameExpression != null : "Could not compute a type name for " + paramType.getQualifiedSourceName();
            w.println(streamWriterName + ".writeString(" + typeNameExpression + ");");
        }
        for (int i = 0; i < asyncParams.length - 1; ++i) {
            JParameter asyncParam = asyncParams[i];
            w.print(streamWriterName + ".");
            w.print(Shared.getStreamWriteMethodNameFor(asyncParam.getType()));
            w.println("(" + asyncParam.getName() + ");");
        }
        JParameter callbackParam = asyncParams[asyncParams.length - 1];
        JType returnType = syncMethod.getReturnType();
        String callbackName = callbackParam.getName();
        if (asyncReturnType == JPrimitiveType.VOID) {
            w.println("%s.finish(%s, ResponseReader.%s);", helperName, callbackName, getResponseReaderFor(returnType).name());
        } else if (asyncReturnType.getQualifiedSourceName().equals(RequestBuilder.class.getName())) {
            w.println("return %s.finishForRequestBuilder(%s, ResponseReader.%s);", helperName, callbackName, getResponseReaderFor(returnType).name());
        } else if (asyncReturnType.getQualifiedSourceName().equals(Request.class.getName())) {
            w.println("return %s.finish(%s, ResponseReader.%s);", helperName, callbackName, getResponseReaderFor(returnType).name());
        } else {
            throw new RuntimeException("Unhandled return type " + asyncReturnType.getQualifiedSourceName());
        }
        w.outdent();
        w.print("} catch (SerializationException ");
        String exceptionName = nameFactory.createName("ex");
        w.println(exceptionName + ") {");
        w.indent();
        if (!asyncReturnType.getQualifiedSourceName().equals(RequestBuilder.class.getName())) {
            w.println(callbackName + ".onFailure(" + exceptionName + ");");
        }
        if (asyncReturnType.getQualifiedSourceName().equals(RequestBuilder.class.getName())) {
            w.println("return new " + FailingRequestBuilder.class.getName() + "(" + exceptionName + ", " + callbackName + ");");
        } else if (asyncReturnType.getQualifiedSourceName().equals(Request.class.getName())) {
            w.println("return new " + FailedRequest.class.getName() + "();");
        } else {
            assert asyncReturnType == JPrimitiveType.VOID;
        }
        w.outdent();
        w.println("}");
        w.outdent();
        w.println("}");
    }

    protected void generateProxyMethods(SourceWriter w, SerializableTypeOracle serializableTypeOracle, TypeOracle typeOracle, Map<JMethod, JMethod> syncMethToAsyncMethMap) {
        JMethod[] syncMethods = serviceIntf.getOverridableMethods();
        for (JMethod syncMethod : syncMethods) {
            JMethod asyncMethod = syncMethToAsyncMethMap.get(syncMethod);
            assert (asyncMethod != null);
            JClassType enclosingType = syncMethod.getEnclosingType();
            JParameterizedType isParameterizedType = enclosingType.isParameterized();
            if (isParameterizedType != null) {
                JMethod[] methods = isParameterizedType.getMethods();
                for (int i = 0; i < methods.length; ++i) {
                    if (methods[i] == syncMethod) {
                        syncMethod = isParameterizedType.getBaseType().getMethods()[i];
                    }
                }
            }
            generateProxyMethod(w, serializableTypeOracle, typeOracle, syncMethod, asyncMethod);
        }
    }

    /**
	 * @param syncMethod
	 * @param asyncMethod
	 * @param statsContextName
	 */
    protected void generateRpcStatsContext(SourceWriter w, JMethod syncMethod, JMethod asyncMethod, String statsContextName) {
        w.println("RpcStatsContext " + statsContextName + " = new RpcStatsContext();");
    }

    protected void generateStreamWriterOverride(SourceWriter srcWriter) {
        srcWriter.println("@Override");
        srcWriter.println("public SerializationStreamWriter createStreamWriter() {");
        srcWriter.indent();
        srcWriter.println("ClientSerializationStreamWriter toReturn =");
        srcWriter.indentln("(ClientSerializationStreamWriter) super.createStreamWriter();");
        if (elideTypeNames) {
            srcWriter.println("toReturn.addFlags(ClientSerializationStreamWriter." + "FLAG_ELIDE_TYPE_NAMES);");
        }
        srcWriter.println("if (getRpcToken() != null) {");
        srcWriter.indent();
        srcWriter.println("toReturn.addFlags(ClientSerializationStreamWriter." + "FLAG_RPC_TOKEN_INCLUDED);");
        srcWriter.outdent();
        srcWriter.println("}");
        srcWriter.println("return toReturn;");
        srcWriter.outdent();
        srcWriter.println("}");
    }

    protected void generateTypeHandlers(TreeLogger logger, GeneratorContextExt context, SerializableTypeOracle typesSentFromBrowser, SerializableTypeOracle typesSentToBrowser) throws UnableToCompleteException {
        Event event = SpeedTracerLogger.start(CompilerEventType.GENERATOR_RPC_TYPE_SERIALIZER);
        TypeSerializerCreator tsc = new TypeSerializerCreator(logger, typesSentFromBrowser, typesSentToBrowser, context, SerializationUtils.getTypeSerializerQualifiedName(serviceIntf), SerializationUtils.getTypeSerializerSimpleName(serviceIntf));
        tsc.realize(logger);
        event.end();
        typeStrings = new HashMap<JType, String>(tsc.getTypeStrings());
        typeStrings.put(serviceIntf, TypeNameObfuscator.SERVICE_INTERFACE_ID);
        cachedTypeLastModifiedTimes = tsc.getTypeLastModifiedTimeMap();
    }

    protected String getProxySimpleName() {
        String[] name = Shared.synthesizeTopLevelClassName(serviceIntf, PROXY_SUFFIX);
        return name[1];
    }

    protected Class<? extends RemoteServiceProxy> getProxySupertype() {
        return RemoteServiceProxy.class;
    }

    protected String getRemoteServiceRelativePath() {
        RemoteServiceRelativePath moduleRelativeURL = serviceIntf.getAnnotation(RemoteServiceRelativePath.class);
        if (moduleRelativeURL != null) {
            return "\"" + moduleRelativeURL.value() + "\"";
        }
        return null;
    }

    protected Class<? extends SerializationStreamWriter> getStreamWriterClass() {
        return ClientSerializationStreamWriter.class;
    }

    protected String writeSerializationPolicyFile(TreeLogger logger, GeneratorContextExt ctx, SerializableTypeOracle serializationSto, SerializableTypeOracle deserializationSto) throws UnableToCompleteException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(baos, SerializationPolicyLoader.SERIALIZATION_POLICY_FILE_ENCODING);
            TypeOracle oracle = ctx.getTypeOracle();
            PrintWriter pw = new PrintWriter(osw);
            JType[] serializableTypes = unionOfTypeArrays(serializationSto.getSerializableTypes(), deserializationSto.getSerializableTypes(), new JType[] { serviceIntf });
            for (int i = 0; i < serializableTypes.length; ++i) {
                JType type = serializableTypes[i];
                String binaryTypeName = SerializationUtils.getRpcTypeName(type);
                pw.print(binaryTypeName);
                pw.print(", " + Boolean.toString(deserializationSto.isSerializable(type)));
                pw.print(", " + Boolean.toString(deserializationSto.maybeInstantiated(type)));
                pw.print(", " + Boolean.toString(serializationSto.isSerializable(type)));
                pw.print(", " + Boolean.toString(serializationSto.maybeInstantiated(type)));
                pw.print(", " + typeStrings.get(type));
                pw.print(", " + SerializationUtils.getSerializationSignature(oracle, type));
                pw.print('\n');
                if ((type instanceof JClassType) && ((JClassType) type).isEnhanced()) {
                    JField[] fields = ((JClassType) type).getFields();
                    JField[] rpcFields = new JField[fields.length];
                    int numRpcFields = 0;
                    for (JField f : fields) {
                        if (f.isTransient() || f.isStatic() || f.isFinal()) {
                            continue;
                        }
                        rpcFields[numRpcFields++] = f;
                    }
                    pw.print(SerializationPolicyLoader.CLIENT_FIELDS_KEYWORD);
                    pw.print(',');
                    pw.print(binaryTypeName);
                    for (int idx = 0; idx < numRpcFields; idx++) {
                        pw.print(',');
                        pw.print(rpcFields[idx].getName());
                    }
                    pw.print('\n');
                }
            }
            pw.close();
            byte[] serializationPolicyFileContents = baos.toByteArray();
            String serializationPolicyName = Util.computeStrongName(serializationPolicyFileContents);
            String serializationPolicyFileName = SerializationPolicyLoader.getSerializationPolicyFileName(serializationPolicyName);
            OutputStream os = ctx.tryCreateResource(logger, serializationPolicyFileName);
            if (os != null) {
                os.write(serializationPolicyFileContents);
                GeneratedResource resource = ctx.commitResource(logger, os);
                emitPolicyFileArtifact(logger, ctx, resource.getPartialPath());
            } else {
                if (logger.isLoggable(TreeLogger.TRACE)) {
                    logger.log(TreeLogger.TRACE, "SerializationPolicy file for RemoteService '" + serviceIntf.getQualifiedSourceName() + "' already exists; no need to rewrite it.", null);
                }
            }
            return serializationPolicyName;
        } catch (UnsupportedEncodingException e) {
            logger.log(TreeLogger.ERROR, SerializationPolicyLoader.SERIALIZATION_POLICY_FILE_ENCODING + " is not supported", e);
            throw new UnableToCompleteException();
        } catch (IOException e) {
            logger.log(TreeLogger.ERROR, null, e);
            throw new UnableToCompleteException();
        }
    }

    private void emitPolicyFileArtifact(TreeLogger logger, GeneratorContextExt context, String partialPath) throws UnableToCompleteException {
        try {
            String qualifiedSourceName = serviceIntf.getQualifiedSourceName();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Writer writer;
            writer = new OutputStreamWriter(baos, SerializationPolicyLoader.SERIALIZATION_POLICY_FILE_ENCODING);
            writer.write("serviceClass: " + qualifiedSourceName + "\n");
            writer.write("path: " + partialPath + "\n");
            writer.close();
            byte[] manifestBytes = baos.toByteArray();
            String md5 = Util.computeStrongName(manifestBytes);
            OutputStream os = context.tryCreateResource(logger, MANIFEST_ARTIFACT_DIR + "/" + md5 + ".txt");
            os.write(manifestBytes);
            GeneratedResource resource = context.commitResource(logger, os);
            resource.setVisibility(Visibility.LegacyDeploy);
        } catch (UnsupportedEncodingException e) {
            logger.log(TreeLogger.ERROR, SerializationPolicyLoader.SERIALIZATION_POLICY_FILE_ENCODING + " is not supported", e);
            throw new UnableToCompleteException();
        } catch (IOException e) {
            logger.log(TreeLogger.ERROR, null, e);
            throw new UnableToCompleteException();
        }
    }

    private String getProxyQualifiedName() {
        String[] name = Shared.synthesizeTopLevelClassName(serviceIntf, PROXY_SUFFIX);
        return name[0].length() == 0 ? name[1] : name[0] + "." + name[1];
    }

    private ResponseReader getResponseReaderFor(JType returnType) {
        if (returnType.isPrimitive() != null) {
            return JPRIMITIVETYPE_TO_RESPONSEREADER.get(returnType.isPrimitive());
        }
        if (returnType.getQualifiedSourceName().equals(String.class.getCanonicalName())) {
            return ResponseReader.STRING;
        }
        return ResponseReader.OBJECT;
    }

    public abstract SourceWriter getSourceWriter(TreeLogger logger, GeneratorContextExt ctx, JClassType serviceAsync);
}
