package ca.sqlpower.wabit.swingui.event;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

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
