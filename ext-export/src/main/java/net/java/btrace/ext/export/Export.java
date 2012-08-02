/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package net.java.btrace.ext.export;

import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.api.extensions.Runtime;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;
import javax.annotation.Resource;

/*
 * Wraps the data export related BTrace utility methods
 * @since 1.2
 */
@BTraceExtension
public class Export {
    @Resource
    private static Runtime ctx;
    private static Properties dotWriterProps;
    
    /**
     * Serialize a given object into the given file.
     * Under the current dir of traced app, ./btrace&lt;pid>/&lt;btrace-class>/
     * directory is created. Under that directory, a file of given
     * fileName is created.
     *
     * @param obj object that has to be serialized.
     * @param fileName name of the file to which the object is serialized.
     */
    public void serialize(Serializable obj, String fileName) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(ctx.getFilePath(fileName)));
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.close();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    /**
     * Creates an XML document to persist the tree of the all
     * transitively reachable objects from given "root" object.
     */
    public static String toXML(Object obj) {
        try {
            return XMLSerializer.toXML(obj);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    /**
     * Writes an XML document to persist the tree of the all the
     * transitively reachable objects from the given "root" object.
     * Under the current dir of traced app, ./btrace&lt;pid>/&lt;btrace-class>/
     * directory is created. Under that directory, a file of the given
     * fileName is created.
     */
    public static void writeXML(Object obj, String fileName) {
        try {
            BufferedWriter bw = new BufferedWriter(
                new FileWriter(ctx.getFilePath(fileName)));
            XMLSerializer.write(obj, bw);
            bw.close();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    /**
     * Writes a .dot document to persist the tree of the all the
     * transitively reachable objects from the given "root" object.
     * .dot documents can be viewed by Graphviz application (www.graphviz.org)
     * Under the current dir of traced app, ./btrace&lt;pid>/&lt;btrace-class>/
     * directory is created. Under that directory, a file of the given
     * fileName is created.
     * @since 1.1
     */
    public static void writeDOT(Object obj, String fileName) {
        DOTWriter writer = new DOTWriter(ctx.getFilePath(fileName));
        initDOTWriterProps();
        writer.customize(dotWriterProps);
        writer.addNode(null, obj);
        writer.close();
    }
    
    private synchronized static void initDOTWriterProps() {
        if (dotWriterProps == null) {
            dotWriterProps = new Properties();
            InputStream is = Export.class.getResourceAsStream("btrace.dotwriter.properties");
            if (is != null) {
                try {
                    dotWriterProps.load(is);
                } catch (IOException ioExp) {
                    ioExp.printStackTrace();
                }
            }
            try {
                String home = System.getProperty("user.home");
                File file = new File(home, "btrace.dotwriter.properties");
                if (file.exists() && file.isFile()) {
                    is = new BufferedInputStream(new FileInputStream(file));
                    if (is != null) {
                        dotWriterProps.load(is);
                    }
                }
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
    }
}
