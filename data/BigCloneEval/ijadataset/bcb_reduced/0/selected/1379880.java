package sf.qof.codegen;

import static sf.qof.codegen.Constants.EXCEPTION_COLLECTIONS_DIFFERENT_SIZE;
import static sf.qof.codegen.Constants.FIELD_NAME_BATCH_SIZE;
import static sf.qof.codegen.Constants.SIG_addBatch;
import static sf.qof.codegen.Constants.SIG_arraycopy;
import static sf.qof.codegen.Constants.SIG_executeBatch;
import static sf.qof.codegen.Constants.SIG_executeUpdate;
import static sf.qof.codegen.Constants.SIG_hasNext;
import static sf.qof.codegen.Constants.SIG_iterator;
import static sf.qof.codegen.Constants.SIG_iterator_next;
import static sf.qof.codegen.Constants.SIG_prepareStatement;
import static sf.qof.codegen.Constants.SIG_size;
import static sf.qof.codegen.Constants.TYPE_Collection;
import static sf.qof.codegen.Constants.TYPE_Connection;
import static sf.qof.codegen.Constants.TYPE_Iterator;
import static sf.qof.codegen.Constants.TYPE_PreparedStatement;
import static sf.qof.codegen.Constants.TYPE_SQLException;
import static sf.qof.codegen.Constants.TYPE_System;
import static sf.qof.codegen.Constants.TYPE_Throwable;
import static sf.qof.codegen.Constants.TYPE_int;
import static sf.qof.codegen.Constants.TYPE_intArray;
import java.util.List;
import net.sf.cglib.core.Block;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Local;
import net.sf.cglib.core.Signature;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import sf.qof.ParameterReplacer;
import sf.qof.exception.ValidationException;
import sf.qof.mapping.Mapper;
import sf.qof.mapping.MethodParameterInfo;
import sf.qof.mapping.ParameterMapping;
import sf.qof.mapping.QueryType;

/**
 * Internal - InsertUpdateDeleteQueryMethodGenerator is the main generator class for insert, update and delete query methods.
 */
public class InsertUpdateDeleteQueryMethodGenerator {

    public static void addInsertUpdateDeleteQueryBody(CodeEmitter co, QueryObjectGenerator generator, Mapper mapper) {
        if (mapper.getQueryType() == QueryType.INSERT && mapper.usesArray()) {
            throw new ValidationException("Array parameters are not allowed for insert statements");
        }
        if (mapper.getMethod().getCollectionParameterInfos().length > 0) {
            addInsertUpdateDeleteQueryBodyWithCollection(co, generator, mapper);
        } else {
            addInsertUpdateDeleteQueryBodyNoCollection(co, generator, mapper);
        }
    }

    private static void addInsertUpdateDeleteQueryBodyNoCollection(CodeEmitter co, QueryObjectGenerator generator, Mapper mapper) {
        Local localConnection = co.make_local(TYPE_Connection);
        Local localPreparedStatement = co.make_local(TYPE_PreparedStatement);
        Local localException = co.make_local(TYPE_Throwable);
        Class<?> returnType = mapper.getMethod().getReturnInfo().getType();
        if (returnType != Integer.TYPE && returnType != Void.TYPE) {
            throw new ValidationException("Only int or void is allowed as return type");
        }
        EmitUtils.emitGetConnection(co, generator, localConnection);
        Block tryBlockConnection = co.begin_block();
        co.load_local(localConnection);
        pushSql(co, mapper, mapper.getSql());
        co.invoke_interface(TYPE_Connection, SIG_prepareStatement);
        co.store_local(localPreparedStatement);
        Block tryBlockStatement = co.begin_block();
        Local localParameterIndexOffset = null;
        if (mapper.usesArray()) {
            localParameterIndexOffset = co.make_local(TYPE_int);
            co.push(0);
            co.store_local(localParameterIndexOffset);
        }
        ParameterMappingGenerator pmg = new ParameterMappingGenerator(co, localPreparedStatement, null, null, localParameterIndexOffset);
        mapper.acceptParameterMappers(pmg);
        co.load_local(localPreparedStatement);
        co.invoke_interface(TYPE_PreparedStatement, SIG_executeUpdate);
        Local localResult = null;
        if (returnType == Integer.TYPE) {
            localResult = co.make_local(TYPE_int);
            co.store_local(localResult);
        } else {
            co.pop();
        }
        tryBlockStatement.end();
        EmitUtils.emitClose(co, localPreparedStatement);
        tryBlockConnection.end();
        EmitUtils.emitUngetConnection(co, generator, localConnection);
        if (localResult != null) {
            co.load_local(localResult);
        }
        co.return_value();
        EmitUtils.emitCatchException(co, tryBlockStatement, null);
        Block tryBlockStatement2 = co.begin_block();
        co.store_local(localException);
        EmitUtils.emitClose(co, localPreparedStatement);
        co.load_local(localException);
        co.athrow();
        tryBlockStatement2.end();
        EmitUtils.emitCatchException(co, tryBlockConnection, null);
        EmitUtils.emitCatchException(co, tryBlockStatement2, null);
        co.store_local(localException);
        EmitUtils.emitUngetConnection(co, generator, localConnection);
        co.load_local(localException);
        co.athrow();
    }

    private static void addInsertUpdateDeleteQueryBodyWithCollection(CodeEmitter co, QueryObjectGenerator generator, Mapper mapper) {
        MethodParameterInfo[] collectionParameterInfos = mapper.getMethod().getCollectionParameterInfos();
        int numParameterCollections = collectionParameterInfos.length;
        Class<?> returnType = mapper.getMethod().getReturnInfo().getType();
        if (!(returnType == Void.TYPE) && !(returnType.isArray() && returnType.getComponentType() == Integer.TYPE)) {
            throw new ValidationException("Only int[] or void is allowed as return type");
        }
        if (numParameterCollections > 1) {
            Label labelException = co.make_label();
            Label labelNoException = co.make_label();
            for (int i = 0; i < numParameterCollections - 1; i++) {
                co.load_arg(collectionParameterInfos[i].getIndex());
                co.invoke_interface(TYPE_Collection, SIG_size);
                co.load_arg(collectionParameterInfos[i + 1].getIndex());
                co.invoke_interface(TYPE_Collection, SIG_size);
                co.if_icmp(CodeEmitter.NE, labelException);
            }
            co.goTo(labelNoException);
            co.mark(labelException);
            co.throw_exception(TYPE_SQLException, EXCEPTION_COLLECTIONS_DIFFERENT_SIZE);
            co.mark(labelNoException);
        }
        co.load_arg(collectionParameterInfos[0].getIndex());
        co.invoke_interface(TYPE_Collection, SIG_size);
        Label labelNotZero = co.make_label();
        co.if_jump(CodeEmitter.NE, labelNotZero);
        if (returnType.isArray()) {
            co.push(0);
            co.newarray(TYPE_int);
        }
        co.return_value();
        co.mark(labelNotZero);
        Local localConnection = co.make_local(TYPE_Connection);
        Local localPreparedStatement = co.make_local(TYPE_PreparedStatement);
        Local localException = co.make_local(TYPE_Throwable);
        EmitUtils.emitGetConnection(co, generator, localConnection);
        Block tryBlockConnection = co.begin_block();
        co.load_local(localConnection);
        pushSql(co, mapper, mapper.getSql());
        co.invoke_interface(TYPE_Connection, SIG_prepareStatement);
        co.store_local(localPreparedStatement);
        Block tryBlockStatement = co.begin_block();
        Local localResult = null;
        Local localPartResult = null;
        Local localIndex = null;
        if (returnType.isArray()) {
            localResult = co.make_local(TYPE_intArray);
            localPartResult = co.make_local(TYPE_intArray);
            co.load_arg(collectionParameterInfos[0].getIndex());
            co.invoke_interface(TYPE_Collection, SIG_size);
            co.newarray(TYPE_int);
            co.store_local(localResult);
            localIndex = co.make_local(TYPE_int);
            co.push(0);
            co.store_local(localIndex);
        }
        Local localCounter = co.make_local(TYPE_int);
        co.push(0);
        co.store_local(localCounter);
        Local[] localIterators = new Local[numParameterCollections];
        Local[] localObjects = new Local[numParameterCollections];
        for (int i = 0; i < numParameterCollections; i++) {
            localIterators[i] = co.make_local(TYPE_Iterator);
            co.load_arg(collectionParameterInfos[i].getIndex());
            co.invoke_interface(TYPE_Collection, SIG_iterator);
            co.store_local(localIterators[i]);
            localObjects[i] = co.make_local(Type.getType(collectionParameterInfos[i].getCollectionElementType()));
        }
        Label labelBeginWhile = co.make_label();
        Label labelEndWhile = co.make_label();
        co.mark(labelBeginWhile);
        co.load_local(localIterators[0]);
        co.invoke_interface(TYPE_Iterator, SIG_hasNext);
        co.if_jump(CodeEmitter.EQ, labelEndWhile);
        for (int i = 0; i < numParameterCollections; i++) {
            co.load_local(localIterators[i]);
            co.invoke_interface(TYPE_Iterator, SIG_iterator_next);
            co.checkcast(Type.getType(collectionParameterInfos[i].getCollectionElementType()));
            co.store_local(localObjects[i]);
        }
        co.iinc(localCounter, 1);
        Local localParameterIndexOffset = null;
        if (mapper.usesArray()) {
            localParameterIndexOffset = co.make_local(TYPE_int);
            co.push(0);
            co.store_local(localParameterIndexOffset);
        }
        ParameterMappingGenerator pmg = new ParameterMappingGenerator(co, localPreparedStatement, localObjects, collectionParameterInfos, localParameterIndexOffset);
        mapper.acceptParameterMappers(pmg);
        co.load_this();
        co.getfield(FIELD_NAME_BATCH_SIZE);
        Label labelNoBatching = co.make_label();
        co.if_jump(CodeEmitter.LE, labelNoBatching);
        co.load_local(localPreparedStatement);
        co.invoke_interface(TYPE_PreparedStatement, SIG_addBatch);
        co.load_local(localCounter);
        co.load_this();
        co.getfield(FIELD_NAME_BATCH_SIZE);
        Label labelAfter = co.make_label();
        co.if_icmp(CodeEmitter.LT, labelAfter);
        co.load_local(localPreparedStatement);
        co.invoke_interface(TYPE_PreparedStatement, SIG_executeBatch);
        if (returnType.isArray()) {
            co.store_local(localPartResult);
            co.load_local(localPartResult);
            co.push(0);
            co.load_local(localResult);
            co.load_local(localIndex);
            co.load_local(localPartResult);
            co.arraylength();
            co.invoke_static(TYPE_System, SIG_arraycopy);
            co.load_local(localIndex);
            co.load_local(localPartResult);
            co.arraylength();
            co.math(CodeEmitter.ADD, TYPE_int);
            co.store_local(localIndex);
        } else {
            co.pop();
        }
        co.push(0);
        co.store_local(localCounter);
        co.mark(labelAfter);
        co.goTo(labelBeginWhile);
        co.mark(labelNoBatching);
        if (returnType.isArray()) {
            co.load_local(localResult);
            co.load_local(localIndex);
            co.iinc(localIndex, 1);
            co.load_local(localPreparedStatement);
            co.invoke_interface(TYPE_PreparedStatement, SIG_executeUpdate);
            co.array_store(TYPE_int);
        } else {
            co.load_local(localPreparedStatement);
            co.invoke_interface(TYPE_PreparedStatement, SIG_executeUpdate);
            co.pop();
        }
        co.goTo(labelBeginWhile);
        co.mark(labelEndWhile);
        Label labelAfter2 = co.make_label();
        co.load_local(localCounter);
        co.if_jump(CodeEmitter.LE, labelAfter2);
        co.load_this();
        co.getfield(FIELD_NAME_BATCH_SIZE);
        co.if_jump(CodeEmitter.LE, labelAfter2);
        co.load_local(localPreparedStatement);
        co.invoke_interface(TYPE_PreparedStatement, SIG_executeBatch);
        if (returnType.isArray()) {
            co.store_local(localPartResult);
            co.load_local(localPartResult);
            co.push(0);
            co.load_local(localResult);
            co.load_local(localIndex);
            co.load_local(localPartResult);
            co.arraylength();
            co.invoke_static(TYPE_System, SIG_arraycopy);
        } else {
            co.pop();
        }
        co.mark(labelAfter2);
        tryBlockStatement.end();
        EmitUtils.emitClose(co, localPreparedStatement);
        tryBlockConnection.end();
        EmitUtils.emitUngetConnection(co, generator, localConnection);
        if (returnType.isArray()) {
            co.load_local(localResult);
        }
        co.return_value();
        EmitUtils.emitCatchException(co, tryBlockStatement, null);
        Block tryBlockStatement2 = co.begin_block();
        co.store_local(localException);
        EmitUtils.emitClose(co, localPreparedStatement);
        co.load_local(localException);
        co.athrow();
        tryBlockStatement2.end();
        EmitUtils.emitCatchException(co, tryBlockConnection, null);
        EmitUtils.emitCatchException(co, tryBlockStatement2, null);
        co.store_local(localException);
        EmitUtils.emitUngetConnection(co, generator, localConnection);
        co.load_local(localException);
        co.athrow();
    }

    private static void pushSql(CodeEmitter co, Mapper mapper, String sql) {
        if (mapper.usesArray()) {
            co.push(sql);
            List<ParameterMapping> mappings = mapper.getParameters();
            for (int i = mappings.size() - 1; i >= 0; i--) {
                ParameterMapping mapping = mappings.get(i);
                if (mapping.usesArray()) {
                    co.push(mapping.getSqlIndexes()[0]);
                    co.load_arg(mapping.getIndex());
                    co.arraylength();
                    if (mapping.getParameterSeparator() == null) {
                        co.push(",");
                    } else {
                        co.push(mapping.getParameterSeparator());
                    }
                    co.invoke_static(Type.getType(ParameterReplacer.class), new Signature("replace", "(Ljava/lang/String;IILjava/lang/String;)Ljava/lang/String;"));
                }
            }
        } else {
            co.push(sql);
        }
    }
}
