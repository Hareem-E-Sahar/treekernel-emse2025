package edu.sharif.ce.dml.common.util.io;

import edu.sharif.ce.dml.common.data.trace.TraceWriter;
import edu.sharif.ce.dml.common.data.trace.filter.TraceFilter;
import edu.sharif.ce.dml.common.parameters.data.StringDataParameter;
import edu.sharif.ce.dml.common.util.io.loader.FileLoader;
import edu.sharif.ce.dml.common.util.io.loader.LoadingHandler;
import edu.sharif.ce.dml.common.util.io.loader.User;
import edu.sharif.ce.dml.common.util.io.loader.UsingHandler;
import edu.sharif.ce.dml.common.util.io.loader.bulk.BulkLoader;
import edu.sharif.ce.dml.common.util.io.loader.bulk.BulkLoadingHandler;
import edu.sharif.ce.dml.common.util.io.loader.bulk.BulkUser;
import edu.sharif.ce.dml.common.util.io.loader.bulk.BulkUsingHandler;
import edu.sharif.ce.dml.common.util.io.loader.stream.StreamLoader;
import edu.sharif.ce.dml.common.util.io.loader.stream.StreamLoadingHandler;
import edu.sharif.ce.dml.common.util.io.loader.stream.StreamUser;
import edu.sharif.ce.dml.common.util.io.loader.stream.StreamUsingHandler;
import edu.sharif.ce.dml.common.util.io.output.BufferOutputWriter;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Masoud
 * Date: Jun 13, 2008
 * Time: 8:20:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraceUsingFactory<T> {

    private TraceFilter<T> fileFilter;

    private FileLoader<T> fileLoader;

    private boolean streamPrefered = true;

    public TraceUsingFactory(TraceFilter<T> fileFilter) {
        this(fileFilter, true);
    }

    public TraceUsingFactory(TraceFilter<T> fileFilter, boolean streamPrefered) {
        this.fileFilter = fileFilter;
        this.streamPrefered = streamPrefered;
    }

    public FileLoader<T> getFileLoader(File file) {
        LoadingHandler<T> stl = getLoadingHandler(file);
        if (useStream(file)) {
            fileLoader = new StreamLoader<T>(file, (StreamLoadingHandler<T>) stl);
            return fileLoader;
        } else {
            fileLoader = new BulkLoader<T>(file, (BulkLoadingHandler<T>) stl);
            return fileLoader;
        }
    }

    private LoadingHandler<T> getLoadingHandler(File file) {
        Class<? extends LoadingHandler<T>> loadingHandlerClass = fileFilter.getLoadingHandlerClass(file);
        LoadingHandler<T> stl = null;
        try {
            if (useStream(file)) {
                stl = (StreamLoadingHandler<T>) loadingHandlerClass.getConstructors()[0].newInstance();
            } else {
                stl = (BulkLoadingHandler<T>) loadingHandlerClass.getConstructors()[0].newInstance();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return stl;
    }

    private boolean useStream(File file) {
        Class<? extends LoadingHandler<T>> loadingHandlerClass = fileFilter.getLoadingHandlerClass(file);
        boolean isStream = false;
        boolean isBulk = false;
        if ((StreamLoadingHandler.class.isAssignableFrom(loadingHandlerClass))) {
            isStream = true;
        }
        if ((BulkLoadingHandler.class.isAssignableFrom(loadingHandlerClass))) {
            isBulk = true;
        }
        if (isStream && isBulk) {
            return streamPrefered;
        }
        assert isStream || isBulk : "it is neither stream nor bulk!";
        return isStream;
    }

    public User<T> getDataUser(File file, UsingHandler<T> usingHandler) {
        if (fileLoader == null) {
            getFileLoader(file);
        }
        User<T> dataUser;
        if (useStream(file)) {
            dataUser = new StreamUser<T>((StreamUsingHandler<T>) usingHandler, (StreamLoader<T>) fileLoader);
        } else {
            dataUser = new BulkUser<T>((BulkUsingHandler<T>) usingHandler, (BulkLoader<T>) fileLoader);
        }
        return dataUser;
    }

    public TraceWriter getTraceWriter(String fileAddress, File input, Map<String, String> inputParams, String[] dataLabels) {
        java.util.List<StringDataParameter> params = new LinkedList<StringDataParameter>();
        for (String key : inputParams.keySet()) {
            params.add(new StringDataParameter(key, inputParams.get(key)));
        }
        try {
            return fileFilter.getTraceWriterClass(input).getConstructor(Collection.class, String[].class, BufferOutputWriter.class, String.class).newInstance(params, dataLabels, BufferOutputWriter.createRandomWriter(), fileAddress);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        assert false : "should not reach here";
        return null;
    }
}
