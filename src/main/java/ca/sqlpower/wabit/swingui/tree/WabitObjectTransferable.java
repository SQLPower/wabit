/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.wabit.swingui.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class WabitObjectTransferable implements Transferable {
	public static final DataFlavor LOCAL_OBJECT_ARRAY_FLAVOUR =
        new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                "; class=\"[Ljava.lang.Object;\"", "WabitByteStream");
	
	private final Object byteStream;
	
	public WabitObjectTransferable(Object byteStream) {
		super();
		this.byteStream = byteStream;
	}
	
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor != LOCAL_OBJECT_ARRAY_FLAVOUR) {
			throw new UnsupportedFlavorException(flavor);
		}
		return byteStream;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { LOCAL_OBJECT_ARRAY_FLAVOUR };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor == LOCAL_OBJECT_ARRAY_FLAVOUR;
	}

}