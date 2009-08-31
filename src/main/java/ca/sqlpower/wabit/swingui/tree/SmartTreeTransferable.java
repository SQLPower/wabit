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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.swingui.dbtree.SQLObjectSelection;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.dao.WorkspaceXMLDAO;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.swingui.olap.OlapMetadataTransferable;
import ca.sqlpower.wabit.swingui.report.ReportQueryTransferable;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeModel.Olap4jTreeObject;

import com.rc.retroweaver.runtime.Collections;

/**
 * This is a transferrable which can take any datatype that is in the left tree,
 * with it you can transfer Olap objects (cubes etc..), {@link SQLObject}'s (tables).
 * Items that can be dragged into reports (all wabit objects except connections and 
 * other reports) and finally files to import and export. 
 */
public class SmartTreeTransferable implements Transferable {
	public static final DataFlavor WABIT_OBJECT_FLAVOUR_TO_EXPORT = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                "; class=\"[Ljava.lang.Object;\"", "WabitByteStream");
	private final List<Object> transferObjects;
	
	/**
	 * This variable is used to track whether or not this transferable has Olap
	 * objects to export
	 */
	private boolean hasOlap = false;
	
	/**
	 * This variable is used to track whether or not this transferable has
	 * Wabit objects to export to other workspaces
	 */
	private boolean hasWabitObjectToExport = false;
	
	/**
	 * This variable is used to track whether or not this transferable has
	 * wabit objects which can be imported into a report (aka not connections)
	 */
	private boolean hasWabitObjectForReport = false;
	
	/**
	 * This variable is used to track whether or not this transferable
	 * contains sql objects to be brought into a query pen.
	 */
	private boolean hasSQLObject = false;
	
	/**
	 * The context is needed for when a user tries to export to the desktop.
	 */
	private WabitSessionContext context = null;
	
	/**
	 * This is a smart transferable class which knows what to return given a specific
	 * {@link DataFlavor}. It also will know what types of data it has in it. Then
	 * when asking for a specific {@link DataFlavor} it will only objects
	 * of that type even when it has all kinds of different objects
	 * 
	 * @param transferObjects
	 * 		The objects to transfer, can either be of type {@link Olap4jTreeObject},
	 * 		{@link WabitObject} or {@link SQLObject} or can be for exporting.
	 * @param context
	 * 		A context is needed for when a user tries to export to the desktop,
	 * 		null is a valid value for this if no exporting to the desktop will
	 * 		be done.
	 */
	public SmartTreeTransferable(List<Object> transferObjects, WabitSessionContext context) {
		super();
		this.transferObjects = transferObjects;
		this.context = context;
		for (Object object : transferObjects) {
			if (object instanceof Olap4jTreeObject) {
				hasOlap = true;
			} else if (object instanceof WabitObject) {
				hasWabitObjectToExport = true;
				if (!(object instanceof WabitDataSource) && !(object instanceof Report)) {
					hasWabitObjectForReport = true;
				}
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
		if (flavor == WABIT_OBJECT_FLAVOUR_TO_EXPORT && hasWabitObjectToExport) {
			for (Object object : transferObjects) {
				if (object instanceof WabitObject) {
					filteredList.add(object);
				}
			}
			WabitObject[] WabitObjects = new WabitObject[filteredList.size()];
			for (int i = 0; i < filteredList.size(); i++) {
				WabitObjects[i] = (WabitObject) filteredList.get(i);
			}
			return WabitObjects;
		} else if (flavor == ReportQueryTransferable.LOCAL_QUERY_ARRAY_FLAVOUR && hasWabitObjectForReport) {
			for (Object object : transferObjects) {
				if (object instanceof WabitObject) {
					filteredList.add(object);
				}
			}
			WabitObject[] WabitObjects = new WabitObject[filteredList.size()];
			for (int i = 0; i < filteredList.size(); i++) {
				if (!(filteredList.get(i) instanceof WabitDataSource)) {
					WabitObjects[i] = (WabitObject) filteredList.get(i);
				}
			}
			return WabitObjects;
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
		} else if (flavor == DataFlavor.javaFileListFlavor && hasWabitObjectToExport) {
			List<WabitObject> wabitObjectsToExport = new ArrayList<WabitObject>();
			for (Object object : transferObjects) {
				if (object instanceof WabitObject) {
					wabitObjectsToExport.add((WabitObject) object);
				}
			}
			
			File wabitTmpDir = new File(System.getProperty("java.io.tmpdir"), "wabit");
			wabitTmpDir.mkdir();
			File file = new File(wabitTmpDir, wabitObjectsToExport.get(0).getName() + ".wabit");
			file.deleteOnExit();
			FileOutputStream byteOut = new FileOutputStream(file);
			WorkspaceXMLDAO dao = new WorkspaceXMLDAO(byteOut, context);
			dao.save(wabitObjectsToExport);
			byteOut.flush();
			return Collections.singletonList(file);
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
		
		if (hasWabitObjectForReport) {
			flavors.add(ReportQueryTransferable.LOCAL_QUERY_ARRAY_FLAVOUR);
		}
		
		if (hasWabitObjectToExport) {
			flavors.add(WABIT_OBJECT_FLAVOUR_TO_EXPORT);
			if (context != null) {
				flavors.add(DataFlavor.javaFileListFlavor);
			}
		}
		
		DataFlavor[] flavorArray = new DataFlavor[flavors.size()];
		for (int i = 0; i < flavors.size(); i++) {
			flavorArray[i] = flavors.get(i);
		}
		return flavorArray;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor == WABIT_OBJECT_FLAVOUR_TO_EXPORT && hasWabitObjectToExport) {
			return true;
		} else if (flavor == ReportQueryTransferable.LOCAL_QUERY_ARRAY_FLAVOUR && hasWabitObjectForReport) {
			return true;
		} else if (flavor == OlapMetadataTransferable.OLAP_ARRAY_FLAVOUR && hasOlap) {
			return true;
		} else if (flavor == SQLObjectSelection.LOCAL_SQLOBJECT_ARRAY_FLAVOUR && hasSQLObject) {
			return true;
		} else if (flavor == DataFlavor.javaFileListFlavor && hasWabitObjectToExport && context != null) {
			return true; 
		} else {
			return false;
		}
	}

}