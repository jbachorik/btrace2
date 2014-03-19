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

package net.java.btrace.jps;

public class JpsVM {

    final private String mainArgs;
    final private String mainClass;
    final private String vmArgs;
    final private String vmFlags;
    final private int pid;

    public JpsVM(int pid, String vmFlags, String vmArgs, String mainClass, String mainArgs) {
        this.pid = pid;
        this.vmFlags = vmFlags;
        this.vmArgs = vmArgs;
        this.mainClass = mainClass;
        this.mainArgs = mainArgs;
    }

    public String getMainArgs() {
        return mainArgs;
    }

    public String getMainClass() {
        return mainClass;
    }

    public int getPid() {
        return pid;
    }

    public String getVMArgs() {
        return vmArgs;
    }

    public String getVMFlags() {
        return vmFlags;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.mainArgs != null ? this.mainArgs.hashCode() : 0);
        hash = 31 * hash + (this.mainClass != null ? this.mainClass.hashCode() : 0);
        hash = 31 * hash + (this.vmArgs != null ? this.vmArgs.hashCode() : 0);
        hash = 31 * hash + (this.vmFlags != null ? this.vmFlags.hashCode() : 0);
        hash = 31 * hash + this.pid;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JpsVM other = (JpsVM) obj;
        if ((this.mainArgs == null) ? (other.mainArgs != null) : !this.mainArgs.equals(other.mainArgs)) {
            return false;
        }
        if ((this.mainClass == null) ? (other.mainClass != null) : !this.mainClass.equals(other.mainClass)) {
            return false;
        }
        if ((this.vmArgs == null) ? (other.vmArgs != null) : !this.vmArgs.equals(other.vmArgs)) {
            return false;
        }
        if ((this.vmFlags == null) ? (other.vmFlags != null) : !this.vmFlags.equals(other.vmFlags)) {
            return false;
        }
        if (this.pid != other.pid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "JpsVM{" + "mainArgs=" + mainArgs + ", mainClass=" + mainClass + ", vmArgs=" + vmArgs + ", vmFlags=" + vmFlags + ", pid=" + pid + '}';
    }
}
