/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.maven;

import net.java.btrace.api.extensions.ExtensionsRepository;
import net.java.btrace.api.extensions.ExtensionsRepositoryFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * Compiles a set of BTrace sources
 * @author Jaroslav Bachorik <jaroslav.bachorik at oracle.com>
 * 
 * @goal btracec
 * @requiresDependencyResolution test
 */
public class BTraceC extends AbstractMojo {
    /**
    * The Maven project object
    *
    * @parameter expression="${project}"
    * @readonly
    */
    private MavenProject project;
    
    /**
     * Files to compile
     * @parameter
     */
    private List<FileSet> filesets;

    /**
     * Where to put the compiled traces
     * @parameter default-value="${project.outputDirectory}"
     */
    private File outputDirectory;
    
    /**
     * @parameter
     * @readonly
     */
    private List<String> classpath;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (filesets == null) {
            throw new MojoExecutionException("need input defined");
        }
                
        FileSetManager fileSetManager = new FileSetManager();

        List<File> files = new LinkedList<File>();
        for(FileSet s : filesets) {
            String baseDir = s.getDirectory();
            
            for(String is : fileSetManager.getIncludedFiles(s)) {
                File f = new File(baseDir + File.separatorChar + is);
                files.add(f);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        for(String s : classpath) {
            sb.append(s).append(File.pathSeparator);
        }
        
        net.java.btrace.compiler.Compiler c = new net.java.btrace.compiler.Compiler(null, true, ExtensionsRepositoryFactory.builtin(ExtensionsRepository.Location.BOTH), new com.sun.tools.javac.api.JavacTool());
        Map<String, byte[]> result = c.compile(files.toArray(new File[files.size()]), new PrintWriter(System.err), ".", sb.toString());
        
        if (result != null) {
            getLog().info("compiled " + result.size() + " traces");
            // write .class files.
            for (Map.Entry<String, byte[]> e : result.entrySet()) {
                String name = e.getKey().replace(".", File.separator);
                int index = name.lastIndexOf(File.separatorChar);
                String dir = outputDirectory.getAbsolutePath() + File.separator;
                if (index != -1) {
                    dir += name.substring(0, index);
                }
                new File(dir).mkdirs();
                String file;
                if (index != -1) {
                    file = name.substring(index + 1);
                } else {
                    file = name;
                }
                file += ".class";
                FileOutputStream fos = null;
                try {
                    File out = new File(dir, file);
                    fos = new FileOutputStream(out);
                    fos.write(e.getValue());
                } catch (IOException ex) {
                    throw new MojoFailureException("error compiling BTrace sources", ex);
                } finally {
                    try {
                        fos.close();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
            }
        }
        
        
//        try {
//            Class cc = ToolProvider.class.getClassLoader().loadClass("net.java.btrace.maven.SeparateCompiler");
//            cc.getMethod("compile", File[].class, String.class).invoke(null, files.toArray(new File[files.size()]), sb.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
