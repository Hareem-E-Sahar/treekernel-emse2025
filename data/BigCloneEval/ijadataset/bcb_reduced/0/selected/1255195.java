package com.ohua.engine.flowgraph.elements.operator;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import com.ohua.engine.exceptions.Assertion;
import com.ohua.engine.exceptions.OperatorLoadingException;
import com.ohua.engine.exceptions.XMLParserException;
import com.ohua.engine.flowgraph.elements.FlowGraph;
import com.ohua.engine.utils.parser.OperatorDescription;
import com.ohua.engine.utils.parser.OperatorDescriptorDeserializer;
import com.ohua.engine.utils.parser.OperatorMappingParser;

public class OperatorFactory {

    private static Map<String, String> _userOperatorRegistry = null;

    private static Map<String, String> _systemOperatorRegistry = null;

    private Map<String, OperatorDescription> _operatorDescriptors = new HashMap<String, OperatorDescription>();

    private static OperatorFactory _factory = new OperatorFactory();

    private OperatorDescriptorDeserializer _descriptorDeserializer = new OperatorDescriptorDeserializer();

    private OperatorFactory() {
    }

    public static OperatorFactory getInstance() {
        if (_userOperatorRegistry == null) {
            OperatorMappingParser parser = new OperatorMappingParser();
            try {
                _userOperatorRegistry = parser.loadOperatorMappings("OperatorRegistry.xml");
            } catch (XMLParserException e) {
                throw new RuntimeException(e);
            }
        }
        if (_systemOperatorRegistry == null) {
            OperatorMappingParser parser = new OperatorMappingParser();
            try {
                _systemOperatorRegistry = parser.loadOperatorMappings("SystemComponentRegistry.xml");
            } catch (XMLParserException e) {
                throw new RuntimeException(e);
            }
        }
        return _factory;
    }

    public OperatorCore createUserOperatorCore(FlowGraph graph, Class<? extends UserOperator> operatorImplementationClass, String operatorName) throws OperatorLoadingException {
        UserOperator operator = createOperator(operatorImplementationClass, operatorName);
        OperatorCore core = prepareUserOperator(graph, operatorName, operator);
        return core;
    }

    public OperatorCore createUserOperatorCore(FlowGraph graph, String operatorName) throws OperatorLoadingException {
        UserOperator operator = createUserOperatorInstance(operatorName);
        OperatorCore core = prepareUserOperator(graph, operatorName, operator);
        return core;
    }

    public UserOperator createOperator(FlowGraph graph, String operatorName) throws OperatorLoadingException {
        UserOperator operator = createUserOperatorInstance(operatorName);
        prepareUserOperator(graph, operatorName, operator);
        return operator;
    }

    private OperatorCore prepareUserOperator(FlowGraph graph, String operatorName, UserOperator operator) throws OperatorLoadingException {
        OperatorCore core = prepareOperator(operatorName, true);
        graph.addOperator(core);
        UserOperatorAdapter adapter = new UserOperatorAdapter(core, operator);
        core.setOperatorAdapter(adapter);
        operator.setOperatorAlgorithmAdapter(adapter);
        return core;
    }

    protected OperatorCore prepareSystemOperator(String operatorName, SystemOperator operator) throws OperatorLoadingException {
        OperatorCore core = prepareOperator(operatorName, false);
        SystemOperatorAdapter adapter = new SystemOperatorAdapter(core, operator);
        core.setOperatorAdapter(adapter);
        operator.setOperatorAlgorithmAdapter(adapter);
        return core;
    }

    protected OperatorCore prepareOperator(String operatorName, boolean isUserOperator) throws OperatorLoadingException {
        OperatorCore core = new OperatorCore(operatorName);
        OperatorDescription description = loadOperatorDescriptor(operatorName, isUserOperator);
        if (description != null) {
            description.apply(core, isUserOperator);
        }
        return core;
    }

    public <T extends UserOperator> T createUserOperator(FlowGraph graph, Class<T> operatorImplementationClass, String operatorName) throws OperatorLoadingException {
        T operator = createOperator(operatorImplementationClass, operatorName);
        prepareUserOperator(graph, operatorName, operator);
        return operator;
    }

    private <T extends AbstractOperatorAlgorithm> T createOperator(Class<T> operatorImplementationClass, String operatorName) throws OperatorLoadingException {
        T operator = createOperatorInstance(operatorImplementationClass);
        return operator;
    }

    @SuppressWarnings("unchecked")
    private UserOperator createUserOperatorInstance(String operatorName) throws OperatorLoadingException {
        Class<? extends UserOperator> clz = (Class<? extends UserOperator>) loadOperatorImplementationClass(operatorName);
        return createOperatorInstance(clz);
    }

    private <T extends AbstractOperatorAlgorithm> T createOperatorInstance(Class<T> clz) throws OperatorLoadingException {
        T operator = null;
        try {
            Constructor<T> constructor = clz.getConstructor();
            operator = constructor.newInstance();
        } catch (Exception e) {
            throw new OperatorLoadingException(e);
        }
        return operator;
    }

    private Class<?> loadOperatorImplementationClass(String operatorName) throws OperatorLoadingException {
        if (!_userOperatorRegistry.containsKey(operatorName)) {
            throw new IllegalArgumentException("No registry entry found for operator: " + operatorName);
        }
        String operatorImplementationClass = _userOperatorRegistry.get(operatorName);
        if (operatorImplementationClass == null) {
            throw new OperatorLoadingException("No implementation class found for operator: " + operatorName);
        }
        Class<?> clz = null;
        try {
            clz = Class.forName(operatorImplementationClass);
        } catch (ClassNotFoundException e) {
            throw new OperatorLoadingException(e);
        }
        return clz;
    }

    private OperatorDescription loadOperatorDescriptor(String operatorName, boolean isUserOperator) throws OperatorLoadingException {
        if (_operatorDescriptors.containsKey(operatorName)) {
            return _operatorDescriptors.get(operatorName);
        }
        String operatorImplName = null;
        if (isUserOperator) {
            if (!_userOperatorRegistry.containsKey(operatorName)) {
                throw new IllegalArgumentException("No registry entry found for operator: " + operatorName);
            } else {
                operatorImplName = _userOperatorRegistry.get(operatorName);
            }
        } else {
            if (!_systemOperatorRegistry.containsKey(operatorName)) {
                return null;
            } else {
                operatorImplName = _systemOperatorRegistry.get(operatorName);
            }
        }
        operatorImplName = operatorImplName.substring(operatorImplName.lastIndexOf(".") + 1);
        try {
            OperatorDescription opDescriptor = _descriptorDeserializer.deserialize(operatorImplName);
            _operatorDescriptors.put(operatorName, opDescriptor);
            return opDescriptor;
        } catch (Exception e) {
            throw new OperatorLoadingException(e);
        }
    }

    public OperatorDescription getOperatorDescription(String operatorName) {
        return _operatorDescriptors.get(operatorName);
    }

    public UserOperator createUserOperator(FlowGraph graph, String operatorType, String displayName) throws OperatorLoadingException {
        UserOperator operator = createUserOperatorInstance(operatorType);
        OperatorCore core = prepareUserOperator(graph, operatorType, operator);
        core.setOperatorName(displayName);
        return operator;
    }

    public SystemOperator createSystemOperator(Class<? extends SystemOperator> clz, String operatorName) {
        SystemOperator operator = null;
        try {
            operator = createOperatorInstance(clz);
            prepareSystemOperator(operatorName, operator);
        } catch (OperatorLoadingException e) {
            Assertion.impossible(e);
        }
        return operator;
    }

    public OperatorCore createSystemOperatorCore(Class<? extends SystemOperator> clz, String operatorName) {
        OperatorCore core = null;
        try {
            SystemOperator operator = createOperatorInstance(clz);
            core = prepareSystemOperator(operatorName, operator);
        } catch (OperatorLoadingException e) {
            Assertion.impossible(e);
        }
        return core;
    }

    public static OperatorDescription getOperatorDescription(OperatorCore operator) {
        return getInstance().getOperatorDescription(operator.getOperatorType());
    }
}
