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

package com.sun.btrace.spi.impl;

import com.sun.btrace.api.BTraceTask;
import com.sun.btrace.spi.ToolsJarLocator;
import java.io.File;
import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.VmIdentifier;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ToolsJarLocatorImpl extends ToolsJarLocator {
    @Override
    public File locateToolsJar(BTraceTask task) {
        return locateToolsJar(task.getPid());
    }

    @Override
    public File locateToolsJar(int pid) {
        String javaHome, classPath;
        
        try {
            HostIdentifier hostId = new HostIdentifier((String)null);
            MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost(hostId);
            
            String uriString = "//" + pid + "?mode=r"; // NOI18N
            VmIdentifier id = new VmIdentifier(uriString);
            MonitoredVm vm = monitoredHost.getMonitoredVm(id, 0);
            try {
                javaHome = (String) vm.findByName("java.property.java.home").getValue();
                classPath = (String) vm.findByName("java.property.java.class.path").getValue();

                // try to get absolute path of tools.jar
                // first check this application's classpath
                String[] components = classPath.split(File.pathSeparator);
                for (String c : components) {
                    if (c.endsWith("tools.jar")) {
                        return new File(c);
                    } else if (c.endsWith("classes.jar")) { // MacOS specific
                        return new File(c);
                    }
                }
                // we didn't find -- make a guess! If this app is running on a JDK rather 
                // than a JRE there will be a tools.jar in $JDK_HOME/lib directory.
                if (System.getProperty("os.name").startsWith("Mac")) {
                    String java_mac_home = javaHome.substring(0, javaHome.indexOf("/Home"));
                    return new File(java_mac_home + "/Classes/classes.jar");
                } else {
                    File tj = new File(javaHome + "/lib/tools.jar");
                    if (!tj.exists()) {
                        tj = new File(javaHome + "/../lib/tools.jar"); // running on JRE
                    }
                    return tj;
                }
            } catch (Exception e) {
            } finally {
                monitoredHost.detach(vm);
            }
        } catch (Exception e) {
        }
        return null;
    }
}
