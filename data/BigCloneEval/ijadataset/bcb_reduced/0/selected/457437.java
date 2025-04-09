package com.waiml.ssentinel.processors.factory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.List;
import javax.xml.bind.*;

public class Generator {

    public static List<Processor> getProcessors() throws Exception {
        JAXBContext context = JAXBContext.newInstance("com.waiml.ssentinel.processors.factory");
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Processors items = (Processors) unmarshaller.unmarshal(new FileReader("./src/main/java/Processors.xml"));
        List<Processor> listOfProcessors = items.getProcessor();
        for (Processor processor : listOfProcessors) {
            System.out.println("Code = " + processor.getCode() + ", getClassName = " + processor.getClassName() + ", getThreadPriority = " + processor.getThreadPriority() + ", Miliseconds = " + processor.getMilisecondsToSleep() + ", getTotalThreads = " + processor.getTotalThreads() + ", Id = " + processor.getId());
        }
        return listOfProcessors;
    }

    public static Object getRunnableProcessor(Processor processor) {
        Object retobj = null;
        try {
            Class cls = Class.forName(processor.getClassName());
            Class partypes[] = new Class[1];
            partypes[0] = processor.getClass();
            Constructor ct = cls.getConstructor(partypes);
            Object arglist[] = new Object[1];
            arglist[0] = processor;
            retobj = ct.newInstance(arglist);
        } catch (Throwable e) {
            System.err.println(e);
        }
        return retobj;
    }
}
