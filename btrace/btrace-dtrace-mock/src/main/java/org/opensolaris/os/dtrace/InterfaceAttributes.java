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

import java.io.*;
import java.beans.*;

/**
 * Triplet of attributes consisting of two stability levels and a
 * dependency class.  Attributes may vary independently.  They use
 * labels described in the {@code attributes(5)} man page to help set
 * expectations for what kinds of changes might occur in different kinds
 * of future releases.  The D compiler includes features to dynamically
 * compute the stability levels of D programs you create.  For more
 * information, refer to the <a
 * href=http://docs.sun.com/app/docs/doc/817-6223/6mlkidlnp?a=view>
 * <b>Stability</b></a> chapter of the <i>Solaris Dynamic Tracing
 * Guide</i>.
 * <p>
 * Immutable.  Supports persistence using {@link java.beans.XMLEncoder}.
 *
 * @see Consumer#getProgramInfo(Program program)
 * @see Consumer#enable(Program program)
 * @see Consumer#listProbes(ProbeDescription filter)
 * @see Consumer#listProgramProbes(Program program)
 *
 * @author Tom Erickson
 */
public final class InterfaceAttributes implements Serializable {
    static final long serialVersionUID = -2814012588381562694L;

    /**
     * Interface stability level.  Assists developers in making risk
     * assessments when developing scripts and tools based on DTrace by
     * indicating how likely an interface or DTrace entity is to change
     * in a future release or patch.
     */
    public enum Stability {
	/**
	 * The interface is private to DTrace itself and represents an
	 * implementation detail of DTrace.  Internal interfaces might
	 * change in minor or micro releases.
	 */
	INTERNAL("Internal"),
	/**
	 * The interface is private to Sun and represents an interface
	 * developed for use by other Sun products that is not yet
	 * publicly documented for use by customers and ISVs.  Private
	 * interfaces might change in minor or micro releases.
	 */
	PRIVATE("Private"),
	/**
	 * The interface is supported in the current release but is
	 * scheduled to be removed, most likely in a future minor
	 * release.  When support of an interface is to be discontinued,
	 * Sun will attempt to provide notification before discontinuing
	 * the interface.  The D compiler might produce warning messages
	 * if you attempt to use an Obsolete interface.
	 */
	OBSOLETE("Obsolete"),
	/**
	 * The interface is controlled by an entity other than Sun.  At
	 * Sun's discretion, Sun can deliver updated and possibly
	 * incompatible versions as part of any release, subject to
	 * their availability from the controlling entity.  Sun makes no
	 * claims regarding either the source or binary compatibility
	 * for External interfaces between two releases.  Applications
	 * based on these interfaces might not work in future releases,
	 * including patches that contain External interfaces.
	 */
	EXTERNAL("External"),
	/**
	 * The interface is provided to give developers early access to
	 * new or rapidly changing technology or to an implementation
	 * artifact that is essential for observing or debugging system
	 * behavior for which a more stable solution is anticipated in
	 * the future.  Sun makes no claims about either source of
	 * binary compatibility for Unstable interfaces from one minor
	 * release to another.
	 */
	UNSTABLE("Unstable"),
	/**
	 * The interface might eventually become Standard or Stable but
	 * is still in transition.  Sun will make reasonable efforts to
	 * ensure compatibility with previous releases as it eveolves.
	 * When non-upward compatible changes become necessary, they
	 * will occur in minor and major releases.  These changes will
	 * be avoided in micro releases whenever possible.  If such a
	 * change is necessary, it will be documented in the release
	 * notes for the affected release, and when feasible, Sun will
	 * provide migration aids for binary compatibility and continued
	 * D program development.
	 */
	EVOLVING("Evolving"),
	/**
	 * The interface is a mature interface under Sun's control.  Sun
	 * will try to avoid non-upward-compatible changes to these
	 * interfaces, especially in minor or micro releases.  If
	 * support of a Stable interface must be discontinued, Sun will
	 * attempt to provide notification and the stability level
	 * changes to Obsolete.
	 */
	STABLE("Stable"),
	/**
	 * The interface complies with an industry standard.  The
	 * corresponding documentation for the interface will describe
	 * the standard to which the interface conforms.  Standards are
	 * typically controlled by a standards development organization,
	 * and changes can be made to the interface in accordance with
	 * approved changes to the standard.  This stability level can
	 * also apply to interfaces that have been adopted (without a
	 * formal standard) by an industry convention.  Support is
	 * provided for only the specified versions of a standard;
	 * support for later versions is not guaranteed.  If the
	 * standards development organization approves a
	 * non-upward-compatible change to a Standard interface that Sun
	 * decides to support, Sun will announce a compatibility and
	 * migration strategy.
	 */
	STANDARD("Standard");

	private String s;

	private
	Stability(String displayName)
	{
	    s = displayName;
	}

	/**
	 * Overridden to get the default display value.  To
	 * internationalize the display value, use {@link
	 * java.lang.Enum#name()} instead as a lookup key.
	 */
	@Override
	public String
	toString()
	{
	    return s;
	}
    }

    /**
     * Architectural dependency class.  Tells whether an interface is
     * common to all Solaris platforms and processors, or whether the
     * interface is associated with a particular architecture such as
     * SPARC processors only.
     */
    public enum DependencyClass {
	// Note that the compareTo() method depends on the order in
	// which the instances are instantiated

	/**
	 * The interface has an unknown set of architectural dependencies.
	 * DTrace does not necessarily know the architectural dependencies of
	 * all entities, such as data types defined in the operating system
	 * implementation.  The Unknown label is typically applied to interfaces
	 * of very low stability for which dependencies cannot be computed.  The
	 * interface might not be available when using DTrace on <i>any</i>
	 * architecture other than the one you are currently using.
	 */
	UNKNOWN("Unknown"),
	/**
	 * The interface is specific to the CPU model of the current
	 * system.  You can use the {@code psrinfo(1M)} utility's {@code
	 * -v} option to display the current CPU model and
	 * implementation names.  Interfaces with CPU model dependencies
	 * might not be available on other CPU implementations, even if
	 * those CPUs export the same instruction set architecture
	 * (ISA).  For example, a CPU-dependent interface on an
	 * UltraSPARC-III+ microprocessor might not be available on an
	 * UltraSPARC-II microprocessor, even though both processors
	 * support the SPARC instruction set.
	 */
	CPU("CPU"),
	/**
	 * The interface is specific to the hardware platform of the current
	 * system.  A platform typically associates a set of system components
	 * and architectural characteristics such as a set of supported CPU
	 * models with a system name such as <code>SUNW,
	 * Ultra-Enterprise-10000</code>.  You can display the current
	 * platform name using the {@code uname(1)} {@code -i} option.
	 * The interface might not be available on other hardware
	 * platforms.
	 */
	PLATFORM("Platform"),
	/**
	 * The interface is specific to the hardware platform group of the
	 * current system.  A platform group typically associates a set of
	 * platforms with related characteristics together under a single name,
	 * such as {@code sun4u}.  You can display the current platform
	 * group name using the {@code uname(1)} {@code -m} option.  The
	 * interface is available on other platforms in the platform
	 * group, but might not be available on hardware platforms that
	 * are not members of the group.
	 */
	GROUP("Group"),
	/**
	 * The interface is specific to the instruction set architecture (ISA)
	 * supported by the microprocessor on this system.  The ISA describes a
	 * specification for software that can be executed on the
	 * microprocessor, including details such as assembly language
	 * instructions and registers.  You can display the native
	 * instruction sets supported by the system using the {@code
	 * isainfo(1)} utility.  The interface might not be supported on
	 * systems that do not export any of of the same instruction
	 * sets.  For example, an ISA-dependent interface on a Solaris
	 * SPARC system might not be supported on a Solaris x86 system.
	 */
	ISA("ISA"),
	/**
	 * The interface is common to all Solaris systems regardless of the
	 * underlying hardware.  DTrace programs and layered applications that
	 * depend only on Common interfaces can be executed and deployed on
	 * other Solaris systems with the same Solaris and DTrace revisions.
	 * The majority of DTrace interfaces are Common, so you can use them
	 * wherever you use Solaris.
	 */
	COMMON("Common");

	private String s;

	private
	DependencyClass(String displayString)
	{
	    s = displayString;
	}

	/**
	 * Overridden to get the default display value.  To
	 * internationalize the display value, use {@link
	 * java.lang.Enum#name()} instead as a lookup key.
	 */
	@Override
	public String
	toString()
	{
	    return s;
	}
    }

    /**
     * Creates an interface attribute triplet from the given attributes.
     *
     * @param nameStabilityAttribute the stability level of the
     * interface associated with its name in a D program
     * @param dataStabilityAttribute stability of the data format used
     * by the interface and any associated data semantics
     * @param dependencyClassAttribute describes whether the interface
     * is specific to the current operating platform or microprocessor
     * @throws NullPointerException if any parameter is {@code null}
     */
    public
    InterfaceAttributes(Stability nameStabilityAttribute,
	    Stability dataStabilityAttribute,
	    DependencyClass dependencyClassAttribute)
    {
    }

    /**
     * Creates an interface attribute triplet from the given attribute
     * names.  Supports XML persistence.
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws IllegalArgumentException if any parameter fails to match
     * an enumerated stability value
     */
    public
    InterfaceAttributes(String nameStabilityAttributeName,
	    String dataStabilityAttributeName,
	    String dependencyClassAttributeName)
    {
    }

    /**
     * Gets the stabiltiy level of an interface associated with its name
     * as it appears in a D program.  For example, the {@code execname}
     * D variable is a {@link Stability#STABLE STABLE} name: Sun
     * guarantees this identifier will continue to be supported in D
     * programs according to the rules described for Stable interfaces.
     *
     * @return the stability level of an interface associated with its
     * name as it appears in a D program
     */
    public Stability
    getNameStability()
    {
	return Stability.UNSTABLE;
    }

    /**
     * Gets the stability level of the data format used by an interface
     * and any associated data semantics.  For example, the {@code pid}
     * D variable is a {@link Stability#STABLE STABLE} interface:
     * process IDs are a stable concept in Solaris, and Sun guarantees
     * that the {@code pid} variable will be of type {@code pid_t} with
     * the semantic that it is set to the process ID corresponding to
     * the thread that fired a given probe in accordance with the rules
     * described for Stable interfaces.
     *
     * @return the stability level of the data format used by an
     * interface and any associated data semantics.
     */
    public Stability
    getDataStability()
    {
	return Stability.UNSTABLE;
    }

    /**
     * Gets the interface dependency class.
     *
     * @return the dependency class describing whether the interface is
     * specific to the current operating platform or microprocessor
     */
    public DependencyClass
    getDependencyClass()
    {
	return DependencyClass.UNKNOWN;
    }
}