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

/**
 * Identifies a compiled D program.  This identifier is valid only on
 * the {@link LocalConsumer} from which it was obtained.  Some {@code
 * Consumer} methods attach additional {@link ProgramInfo} to this
 * identifier.
 * <p>
 * Not intended for persistence, since it identifies nothing after its
 * source {@code LocalConsumer} closes.
 *
 * @see Consumer#compile(String program, String[] macroArgs)
 * @see Consumer#compile(java.io.File program, String[] macroArgs)
 * @see Consumer#enable(Program program)
 * @see Consumer#getProgramInfo(Program program)
 * @see Consumer#listProgramProbes(Program program)
 * @see Consumer#listProgramProbeDetail(Program program)
 *
 * @author Tom Erickson
 */
public class Program implements Serializable {
    static final long serialVersionUID = 364989786308628466L;

    /**
     * Called by native code
     */
    private Program()
    {
    }

    /**
     * Gets the full pre-compiled text of the identified program.
     *
     * @return the {@code String} passed to {@link
     * Consumer#compile(String program, String[] macroArgs)}, or the
     * contents of the {@code File} passed to {@link
     * Consumer#compile(java.io.File program, String[] macroArgs)}
     */
    public String
    getContents()
    {
	return "";
    }

    /**
     * Gets information about this compiled program provided by {@link
     * Consumer#getProgramInfo(Program program)} or {@link
     * Consumer#enable(Program program)}.
     *
     * @return information about this compiled program, or {@code null}
     * if this {@code Program} has not been passed to {@link
     * Consumer#getProgramInfo(Program program)} or {@link
     * Consumer#enable(Program program)}
     */
    public ProgramInfo
    getInfo()
    {
	return null;
    }

    /**
     * Sets additional information about this compiled program,
     * including program stability and matching probe count.  Several
     * {@code Consumer} methods attach such information to a given
     * {@code Program} argument.  The method is {@code public} to
     * support implementations of the {@code Consumer} interface other
     * than {@link LocalConsumer}.  Although a {@code Program} can only
     * be obtained from a {@code LocalConsumer}, other {@code Consumer}
     * implemenations may provide a helpful layer of abstraction while
     * using a {@code LocalConsumer} internally to compile DTrace
     * programs.  Users of the API are not otherwise expected to call
     * the {@code setInfo()} method directly.
     *
     * @param programInfo optional additional information about this
     * compiled program
     * @see #getInfo()
     * @see Consumer#enable(Program program)
     * @see Consumer#getProgramInfo(Program program)
     */
    public void
    setInfo(ProgramInfo programInfo)
    {
    }

    /**
     * Identifies a compiled D program, specifically one that has been
     * compiled from a file.
     */
    public static final class File extends Program {
	static final long serialVersionUID = 6217493430514165300L;

	/**
	 * Gets the program file.
	 *
	 * @return the {@code File} passed to {@link
	 * Consumer#compile(java.io.File program, String[] macroArgs)}
	 */
	public java.io.File
	getFile()
	{
	    return null;
	}
    }
}