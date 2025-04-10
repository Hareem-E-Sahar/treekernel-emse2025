public class Test {    private void modifyActionSchedulability(Action action, Var writeIndex, Var readIndex, OpBinary op, Expression reference, Port port) {
        int index = 0;
        Procedure scheduler = action.getScheduler();
        NodeBlock bodyNode = scheduler.getLast();
        EList<Var> locals = scheduler.getLocals();
        Var localRead = factory.createVar(0, factory.createTypeInt(32), "readIndex_" + port.getName() + "_" + inputIndex, true, inputIndex);
        locals.add(localRead);
        bodyNode.add(index, factory.createInstLoad(localRead, readIndex));
        index++;
        Var localWrite = factory.createVar(0, factory.createTypeInt(32), "writeIndex_" + port.getName() + "_" + inputIndex, true, inputIndex);
        locals.add(localWrite);
        bodyNode.add(index, factory.createInstLoad(localWrite, writeIndex));
        index++;
        Var diff = factory.createVar(0, factory.createTypeInt(32), "diff" + port.getName() + "_" + inputIndex, true, inputIndex);
        locals.add(diff);
        Expression value = factory.createExprBinary(factory.createExprVar(localRead), OpBinary.MINUS, factory.createExprVar(localWrite), factory.createTypeInt(32));
        bodyNode.add(index, factory.createInstAssign(diff, value));
        index++;
        Var conditionVar = factory.createVar(0, factory.createTypeBool(), "condition_" + port.getName(), true, inputIndex);
        locals.add(conditionVar);
        Expression value2 = factory.createExprBinary(factory.createExprVar(diff), op, reference, factory.createTypeBool());
        bodyNode.add(index, factory.createInstAssign(conditionVar, value2));
        index++;
        Var myResult = factory.createVar(0, factory.createTypeBool(), "myResult_" + port.getName(), true, inputIndex);
        locals.add(myResult);
        int returnIndex = bodyNode.getInstructions().size() - 1;
        InstReturn actionReturn = (InstReturn) bodyNode.getInstructions().get(returnIndex);
        Expression e = factory.createExprBinary(actionReturn.getValue(), OpBinary.LOGIC_AND, factory.createExprVar(conditionVar), factory.createTypeBool());
        bodyNode.add(returnIndex, factory.createInstAssign(myResult, e));
        actionReturn.setValue(factory.createExprVar(myResult));
    }
}