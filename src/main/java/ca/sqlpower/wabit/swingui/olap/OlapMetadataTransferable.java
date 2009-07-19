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

package ca.sqlpower.wabit.swingui.olap;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class OlapMetadataTransferable implements Transferable {

    private final Object[] transferData;

    /**
     * Data flavour that indicates a JVM-local reference to any object (because
     * olap4j metadata classes do not implement a common interface or extend a
     * more specific common base class).
     */
    public static final DataFlavor LOCAL_OBJECT_ARRAY_FLAVOUR =
        new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                "; class=\"[Ljava.lang.Object;\"", "Local Object Array");
    
    /**
     * @param transferData
     */
    public OlapMetadataTransferable(Object[] transferData) {
        super();
        this.transferData = transferData;
    }

    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (flavor != LOCAL_OBJECT_ARRAY_FLAVOUR) {
            throw new UnsupportedFlavorException(flavor);
        }
        return transferData;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { LOCAL_OBJECT_ARRAY_FLAVOUR };
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == LOCAL_OBJECT_ARRAY_FLAVOUR;
    }

}
