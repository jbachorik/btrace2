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
package org.opensolaris.os.dtrace;

import java.util.*;
import java.io.*;
import java.beans.*;

/**
 * A {@link ProbeDescription} identifying a single probe combined with
 * information about the identified probe.
 * <p>
 * Immutable.  Supports persistence using {@link java.beans.XMLEncoder}.
 *
 * @see Consumer#listProbes(ProbeDescription filter)
 * @see Consumer#listProgramProbes(Program program)
 *
 * @author Tom Erickson
 */
public final class Probe implements Serializable {
    static final long serialVersionUID = 8917481979541675727L;

    static {
	try {
	    BeanInfo info = Introspector.getBeanInfo(Probe.class);
	    PersistenceDelegate persistenceDelegate =
		    new DefaultPersistenceDelegate(
		    new String[] {"description", "info"})
	    {
		/*
		 * Need to prevent DefaultPersistenceDelegate from using
		 * overridden equals() method, resulting in a
		 * StackOverFlowError.  Revert to PersistenceDelegate
		 * implementation.  See
		 * http://forum.java.sun.com/thread.jspa?threadID=
		 * 477019&tstart=135
		 */
		protected boolean
		mutatesTo(Object oldInstance, Object newInstance)
		{
		    return (newInstance != null && oldInstance != null &&
			    oldInstance.getClass() == newInstance.getClass());
		}
	    };
	    BeanDescriptor d = info.getBeanDescriptor();
	    d.setValue("persistenceDelegate", persistenceDelegate);
	} catch (IntrospectionException e) {
	    System.out.println(e);
	}
    }

    /** @serial */
    private final ProbeDescription description;
    /** @serial */
    private final ProbeInfo info;

    /**
     * Creates a {@code Probe} instance with the given identifying
     * description and associated probe information.  Supports XML
     * persistence.
     *
     * @param probeDescription probe description that identifies a
     * single DTrace probe
     * @param probeInfo information about the identified probe, {@code
     * null} indicating that the information could not be obtained
     * @throws NullPointerException if the given probe description is
     * {@code null}
     */
    public
    Probe(ProbeDescription probeDescription, ProbeInfo probeInfo)
    {
	description = probeDescription;
	info = probeInfo;
	validate();
    }

    private final void
    validate()
    {
	if (description == null) {
	    throw new NullPointerException("description is null");
	}
    }

    /**
     * Gets the probe description identifying a single probe described
     * by this instance.
     *
     * @return non-null probe description matching a single probe on the
     * system
     */
    public ProbeDescription
    getDescription()
    {
	return description;
    }

    /**
     * Gets information including attributes and argument types of the
     * probe identified by {@link #getDescription()}.
     *
     * @return information about the probe identified by {@link
     * #getDescription()}, or {@code null} if that information could not
     * be obtained for any reason
     */
    public ProbeInfo
    getInfo()
    {
	return info;
    }

    /**
     * Compares the specified object with this {@code Probe} instance
     * for equality.  Defines equality as having the same probe
     * description.
     *
     * @return {@code true} if and only if the specified object is also
     * a {@code Probe} and both instances return equal values from
     * {@link #getDescription()}.
     */
    @Override
    public boolean
    equals(Object o)
    {
	if (o instanceof Probe) {
	    Probe p = (Probe)o;
	    return description.equals(p.description);
	}
	return false;
    }

    /**
     * Overridden to ensure that equal instances have equal hash codes.
     */
    @Override
    public int
    hashCode()
    {
	return description.hashCode();
    }

    private void
    readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException
    {
	s.defaultReadObject();
	// Check class invariants
	try {
	    validate();
	} catch (Exception e) {
	    InvalidObjectException x = new InvalidObjectException(
		    e.getMessage());
	    x.initCause(e);
	    throw x;
	}
    }

    /**
     * Returns a string representation of this {@code Probe} useful for
     * logging and not intended for display.  The exact details of the
     * representation are unspecified and subject to change, but the
     * following format may be regarded as typical:
     * <pre><code>
     * class-name[property1 = value1, property2 = value2]
     * </code></pre>
     */
    public String
    toString()
    {
	StringBuilder buf = new StringBuilder();
	buf.append(Probe.class.getName());
	buf.append("[description = ");
	buf.append(description);
	buf.append(", info = ");
	buf.append(info);
	buf.append(']');
	return buf.toString();
    }
}