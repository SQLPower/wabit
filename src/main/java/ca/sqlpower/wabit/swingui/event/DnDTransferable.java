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

package ca.sqlpower.wabit.swingui.event;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * A tranferable for objects in a JTree. This is used in dragging items from the
 * left tree to be dropped onto different parts of Wabit.
 * 
 * TODO: Add more data flavours to this including string.
 */
public class DnDTransferable implements Transferable {
	
	public static final DataFlavor OBJECT_NAME_FLAVOR = new DataFlavor(Object.class, "Selected Object");
	
	private Object data;
	
	public DnDTransferable(Object o) {
		data = o;
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor != OBJECT_NAME_FLAVOR) {
			throw new IllegalArgumentException("Unsupported flavor "+flavor);
		}
		return data;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {OBJECT_NAME_FLAVOR};
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor.equals(OBJECT_NAME_FLAVOR));
	}

}
