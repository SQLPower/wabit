/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Wabit.
 *
 * Wabit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wabit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.wabit.swingui.report;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import ca.sqlpower.wabit.QueryCache;

/**
 * This transferable will allow dragging queries defined in a project to a layout.
 */
public class ReportQueryTransferable implements Transferable {
	
	private final QueryCache[] queries;
	
    /**
     * Data flavour that indicates a JVM-local reference to an ArrayList containing
     * 0 or more Queries.
     */
    public static final DataFlavor LOCAL_QUERY_ARRAY_FLAVOUR =
        new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                "; class=\"[Lca.sqlpower.wabit.Query;\"", "Local Array of Queries");

	public ReportQueryTransferable(QueryCache[] queries) {
        if (queries == null) {
            throw new NullPointerException("Can't transfer a null array. Try an empty one instead!");
        }
		this.queries = queries;
	}
	
	public ReportQueryTransferable(List<QueryCache> queries) {
		this(queries.toArray(new QueryCache[queries.size()]));
	}

	public Object getTransferData(DataFlavor flavor)
	throws UnsupportedFlavorException, IOException {
		if (flavor == LOCAL_QUERY_ARRAY_FLAVOUR) {
			return queries;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { LOCAL_QUERY_ARRAY_FLAVOUR };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor == LOCAL_QUERY_ARRAY_FLAVOUR;
	}

}
