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

package net.java.btrace.wireio.commands;

import net.java.btrace.api.wireio.DataCommand;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;


/**
 * A data command that holds tabular data.
 * 
 * The elements contained within the grid must be of type Number, String or HistogramData.
 * 
 * @author Christian Glencross
 * @author Jaroslav Bachorik
 */
final public class GridDataCommand extends DataCommand<GridDataCommand.GridData>  {
    final public static class GridData {
        private String format;
        private List<Object[]> grid;

        public GridData(String format, List<Object[]> grid) {
            this.format = format;
            this.grid = grid;
        }

        /*
         * Returns the format applicable to the grid
         * @see String#format(java.lang.String, java.lang.Object[])
         */
        final public String getFormat() {
            return format == null ? "" : format;
        }

        /*
         * @param format The format to use. It mimics {@linkplain String#format(java.lang.String, java.lang.Object[]) } behaviour
         *               with the addition of the ability to address the key title as a 0-indexed item
         * @see String#format(java.lang.String, java.lang.Object[])
         */
        final public void setFormat(String format) {
            this.format = format;
        }

        final public void setGrid(List<Object[]> data) {
            grid = data;
        }

        final public List<Object[]> getGrid() {
            return grid;
        }
    }

    /**
     * Used when deserializing a {@linkplain GridDataCommand} instance.<br/>
     * The instance is then initialized by calling the {@linkplain GridDataCommand#read(java.io.ObjectInput) } method
     */
    public GridDataCommand(int typeId, int rx, int tx) {
        super(typeId, rx, tx);
    }

    @Override
    final public boolean canBeSpeculated() {
        return super.canBeSpeculated();
    }

    
    public void write(ObjectOutput out) throws IOException {
        super.write(out);
        GridData gData = getPayload();
        List<Object[]> grid = gData.getGrid();
        if (grid != null) {
            out.writeUTF(gData.getFormat());
            out.writeInt(grid.size());
            for (Object[] row : grid) {
                out.writeInt(row.length);
                for (Object cell : row) {
                    out.writeObject(cell);
                }
            }
        } else {
            out.writeInt(0);
        }
    }

    public void read(ObjectInput in) throws IOException, ClassNotFoundException {
        super.read(in);
        String format = in.readUTF();
        if (format.length() == 0) format = null;
        
        int rowCount = in.readInt();
        List<Object[]> grid = new ArrayList<Object[]>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            int cellCount = in.readInt();
            Object[] row = new Object[cellCount];
            for (int j = 0; j < cellCount; j++) {
                row[j] = in.readObject();
            }
            grid.add(row);
        }
        setPayload(new GridData(format, grid));
    }
}
