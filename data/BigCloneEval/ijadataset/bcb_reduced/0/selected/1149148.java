package com.affinetech.one2one.core.mutators;

import java.io.*;

public class MutatorClassLoader extends ClassLoader {

    private final String path = "/home/su-root/IdeaProjects/one2one/mutators/classes";

    public IMutator loadMutatorClass(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoMutatorInterfaceException {
        IMutator ret = null;
        Class<?> mutator = loadClass(className, true);
        Class[] interfaces = mutator.getInterfaces();
        boolean hasMutatorInterface = false;
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].getName().equals(IMutator.class.getName())) {
                hasMutatorInterface = true;
                break;
            }
        }
        if (!hasMutatorInterface) {
            throw new NoMutatorInterfaceException("trying to load class that does not implement: IMutator");
        }
        ret = (IMutator) mutator.newInstance();
        return ret;
    }

    public synchronized Class<?> loadClass(String typeName, boolean resolveIt) throws ClassNotFoundException {
        Class<?> result = findLoadedClass(typeName);
        if (result != null) {
            return result;
        }
        try {
            result = super.findSystemClass(typeName);
            return result;
        } catch (ClassNotFoundException e) {
        }
        if (typeName.startsWith("java.")) {
            throw new ClassNotFoundException();
        }
        byte typeData[] = getTypeFromMutator(typeName);
        if (typeData == null) {
            throw new ClassNotFoundException();
        }
        result = defineClass(typeName, typeData, 0, typeData.length);
        if (result == null) {
            throw new ClassFormatError();
        }
        if (resolveIt) {
            resolveClass(result);
        }
        return result;
    }

    private byte[] getTypeFromMutator(String typeName) {
        FileInputStream fis;
        String fileName = path + File.separatorChar + typeName.replace('.', File.separatorChar) + ".class";
        System.out.println("trying to load: " + fileName);
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            return null;
        }
        BufferedInputStream bis = new BufferedInputStream(fis);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int c = bis.read();
            while (c != -1) {
                out.write(c);
                c = bis.read();
            }
        } catch (IOException e) {
            return null;
        }
        return out.toByteArray();
    }
}
