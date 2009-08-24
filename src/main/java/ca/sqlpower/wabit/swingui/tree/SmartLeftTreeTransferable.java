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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.swingui.dbtree.SQLObjectSelection;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.swingui.olap.OlapMetadataTransferable;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeModel.Olap4jTreeObject;

public class SmartLeftTreeTransferable implements Transferable {
	public static final DataFlavor WABIT_OBJECT_BYTESTREAM_FLAVOUR =
        new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                "; class=\"[Ljava.lang.Object;\"", "WabitByteStream");
	
	private final List<Object> transferObjects;
	
	private boolean hasOlap = false;
	private boolean hasWabitObject = false;
	private boolean hasSQLObject = false;
	
	/**
	 * This is a smart transferable class which knows what to return given a specific
	 * {@link DataFlavor}. It also will know what types of data it has in it. Then
	 * when asking for a specific {@link DataFlavor} it will only objects
	 * of that type even when it has all kinds of different objects
	 * 
	 * @param transferObjects
	 * 		The objects to transfer, can either be of type {@link Olap4jTreeObject},
	 * 		{@link WabitObject} or {@link SQLObject}.
	 */
	public SmartLeftTreeTransferable(List<Object> transferObjects) {
		super();
		this.transferObjects = transferObjects;
		for (Object object : transferObjects) {
			if (object instanceof Olap4jTreeObject) {
				hasOlap = true;
			} else if (object instanceof ByteArrayOutputStream) {
				hasWabitObject = true;
			} else if (object instanceof SQLObject) {
				hasSQLObject = true;
			} else {
				throw new UnsupportedOperationException("Unable to transfer object of type " + object.getClass().toString()
						+ " from the tree on the left hand side.");
			}
		}
	}
	
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {

		List<Object> filteredList = new ArrayList<Object>();
		if (flavor == WABIT_OBJECT_BYTESTREAM_FLAVOUR && hasWabitObject) {
			for (Object object : transferObjects) {
				if (object instanceof ByteArrayOutputStream) {
					filteredList.add(object);
				}
			}
			return filteredList;
		} else if (flavor == OlapMetadataTransferable.OLAP_ARRAY_FLAVOUR && hasOlap) {
			for (Object object : transferObjects) {
				if (object instanceof Olap4jTreeObject) {
					filteredList.add(((Olap4jTreeObject) object).getOlapObject());
				}
			}
			return filteredList.toArray();
		} else if (flavor == SQLObjectSelection.LOCAL_SQLOBJECT_ARRAY_FLAVOUR && hasSQLObject) {
			for (Object object : transferObjects) {
				if (object instanceof SQLObject) {
					filteredList.add(object);
				}
			}
			SQLObject[] sqlObjects = new SQLObject[filteredList.size()];
			for (int i = 0; i < filteredList.size(); i++) {
				sqlObjects[i] = (SQLObject) filteredList.get(i);
			}
			return sqlObjects;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	public DataFlavor[] getTransferDataFlavors() {
		List<DataFlavor> flavors = new ArrayList<DataFlavor>();
		if (hasOlap) {
			flavors.add(OlapMetadataTransferable.OLAP_ARRAY_FLAVOUR);
		}
		
		if (hasSQLObject) {
			flavors.add(SQLObjectSelection.LOCAL_SQLOBJECT_ARRAY_FLAVOUR);
		}
		
		if (hasWabitObject) {
			flavors.add(WABIT_OBJECT_BYTESTREAM_FLAVOUR);
		}
		
		DataFlavor[] flavorArray = new DataFlavor[flavors.size()];
		for (int i = 0; i < flavors.size(); i++) {
			flavorArray[i] = flavors.get(i);
		}
		return flavorArray;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor == WABIT_OBJECT_BYTESTREAM_FLAVOUR && hasWabitObject) {
			return true;
		} else if (flavor == OlapMetadataTransferable.OLAP_ARRAY_FLAVOUR && hasOlap) {
			return true;
		} else if (flavor == SQLObjectSelection.LOCAL_SQLOBJECT_ARRAY_FLAVOUR && hasSQLObject) {
			return true;
		} else {
			return false;
		}
	}

}