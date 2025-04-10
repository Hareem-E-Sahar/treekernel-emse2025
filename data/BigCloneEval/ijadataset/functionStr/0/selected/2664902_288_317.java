public class Test {    private String fromValueCondition(ValueCondition valueCondition) {
        StringBuffer conditionStr = new StringBuffer();
        conditionStr.append(Identifiers.CONDITION_START.getIdentifier());
        conditionStr.append(Identifiers.CONDITION.getIdentifier());
        conditionStr.append(Identifiers.NAME.getIdentifier());
        conditionStr.append(Delimeters.KEYVALUE.getDelimeter());
        conditionStr.append(valueCondition.getConditionName());
        conditionStr.append(Delimeters.KEYVALUE_PAIR.getDelimeter());
        conditionStr.append(Identifiers.HW_ADDR_REF.getIdentifier());
        conditionStr.append(Delimeters.KEYVALUE.getDelimeter());
        conditionStr.append(valueCondition.getHardwareAddr());
        conditionStr.append(Delimeters.KEYVALUE_PAIR.getDelimeter());
        conditionStr.append(Identifiers.IO_CHANNEL.getIdentifier());
        conditionStr.append(Delimeters.KEYVALUE.getDelimeter());
        conditionStr.append(valueCondition.getChannel().toString());
        conditionStr.append(Delimeters.KEYVALUE_PAIR.getDelimeter());
        conditionStr.append(Identifiers.TYPE.getIdentifier());
        conditionStr.append(Delimeters.KEYVALUE.getDelimeter());
        conditionStr.append(Inputs.VALUE_CONDITION.asString());
        conditionStr.append(Delimeters.KEYVALUE_PAIR.getDelimeter());
        conditionStr.append(Identifiers.TEST_OPERATOR.getIdentifier());
        conditionStr.append(Delimeters.KEYVALUE.getDelimeter());
        conditionStr.append(valueCondition.getTestOperator().getOperatorStr());
        conditionStr.append(Delimeters.KEYVALUE_PAIR.getDelimeter());
        conditionStr.append(Identifiers.TEST_VALUE.getIdentifier());
        conditionStr.append(Delimeters.KEYVALUE.getDelimeter());
        conditionStr.append(valueCondition.getTestValue().toString());
        conditionStr.append(Identifiers.CONDITION_STOP.getIdentifier());
        return conditionStr.toString();
    }
}