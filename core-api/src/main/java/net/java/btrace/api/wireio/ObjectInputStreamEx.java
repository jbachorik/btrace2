/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.api.wireio;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

/**
 * An extended version of {@linkplain ObjectInputStream} allowing to specify the used {@linkplain ClassLoader}
 * @author Jaroslav Bachorik
 * 
 * @since 2.0
 */
final public class ObjectInputStreamEx extends ObjectInputStream {

    final private ClassLoader loader;

    public ObjectInputStreamEx(InputStream in, ClassLoader cl) throws IOException {
        super(in);
        this.loader = cl;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String name = desc.getName();
        try {
            return super.resolveClass(desc);
        } catch (ClassNotFoundException ex) {
            Class<?> cl = loader.loadClass(name);
            if (cl != null) {
                return cl;
            } else {
                throw ex;
            }
        }
    }

    @Override
    protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
        ClassLoader latestLoader = loader;
        ClassLoader nonPublicLoader = null;
        boolean hasNonPublicInterface = false;

        // define proxy in class loader of non-public interface(s), if any
        Class[] classObjs = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            Class cl = Class.forName(interfaces[i], false, latestLoader);
            if ((cl.getModifiers() & Modifier.PUBLIC) == 0) {
                if (hasNonPublicInterface) {
                    if (nonPublicLoader != cl.getClassLoader()) {
                        throw new IllegalAccessError(
                                "conflicting non-public interface class loaders");
                    }
                } else {
                    nonPublicLoader = cl.getClassLoader();
                    hasNonPublicInterface = true;
                }
            }
            classObjs[i] = cl;
        }
        try {
            return Proxy.getProxyClass(
                    hasNonPublicInterface ? nonPublicLoader : latestLoader,
                    classObjs);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }
}
