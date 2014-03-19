/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.client;

import net.java.btrace.api.core.BTraceLogger;
import net.java.btrace.api.extensions.ExtensionsRepository;
import net.java.btrace.api.extensions.ExtensionsRepositoryFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * An extension aware compiler wrapper
 * @author Jaroslav Bachorik
 * @since 2.0
 */
public class Compiler {
    private boolean unsafe;
    private ExtensionsRepository extRepository;
    private PrintWriter writer = new PrintWriter(System.err);

    public Compiler(boolean unsafe, ExtensionsRepository extRepository) {
        this.unsafe = unsafe;
        this.extRepository = extRepository != null ? extRepository : ExtensionsRepositoryFactory.builtin(ExtensionsRepository.Location.SERVER);
    }
    
    public byte[] compile(String fileName, String classPath, String includePath) {
        byte[] code = null;
        File file = new File(fileName);
        if (fileName.toLowerCase().endsWith(".java")) {
            net.java.btrace.compiler.Compiler compiler = new net.java.btrace.compiler.Compiler(includePath, unsafe, extRepository);
            StringBuilder cpBuilder = new StringBuilder(classPath);
            cpBuilder.append(File.pathSeparator).append(System.getProperty("java.class.path"));
            cpBuilder.append(File.pathSeparator).append(extRepository.getClassPath());
            
            BTraceLogger.debugPrint("compiling *" + fileName + "*");
            BTraceLogger.debugPrint("compiler classpath = " + cpBuilder.toString());
            Map<String, byte[]> classes = compiler.compile(file, writer, ".", cpBuilder.toString());
            if (classes == null) {
                writer.println("btrace compilation failed!");
                return null;
            }

            int size = classes.size();
            if (size != 1) {
                writer.println("no classes or more than one class");
                return null;
            }
            String name = classes.keySet().iterator().next();
            code = classes.get(name);
            BTraceLogger.debugPrint("compiled " + fileName);
        } else if (fileName.endsWith(".class")) {
            code = new byte[(int) file.length()];
            try {
                FileInputStream fis = new FileInputStream(file);
                BTraceLogger.debugPrint("reading " + fileName);
                try {
                    fis.read(code);
                } finally {
                    fis.close();
                }
                BTraceLogger.debugPrint("read " + fileName);
            } catch (IOException exp) {
                exp.printStackTrace();
                writer.println(exp.getMessage());
                return null;
            }
        } else {
            writer.println("BTrace script has to be a .java or a .class");
            return null;
        }

        return code;
    }
}
