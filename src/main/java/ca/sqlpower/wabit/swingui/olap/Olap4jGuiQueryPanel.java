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

import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.sql.SQLException;
import java.util.Collections;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JWindow;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.query.Query;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;

public class Olap4jGuiQueryPanel {

    private static final Logger logger = Logger.getLogger(Olap4jGuiQueryPanel.class);
    
    public class OlapTreeTransferHandler extends TransferHandler {
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            logger.debug("tree handler: createTransferable()");
            JTree tree = (JTree) c;
            TreePath selectionPath = tree.getSelectionPath();
            if (selectionPath == null) return null;
            Object selectedItem = selectionPath.getLastPathComponent();
            return new OlapMetadataTransferable(selectedItem);
        }

        @Override
        public int getSourceActions(JComponent c) {
            logger.debug("tree handler: getSourceActions()");
            return MOVE;
        }
    }

    private class OlapListTransferHandler extends TransferHandler {
        @Override
        protected Transferable createTransferable(JComponent c) {
            logger.debug("createTransferable()");
            return super.createTransferable(c);
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            logger.debug("canImport()");
            for (DataFlavor dataFlavor : transferFlavors) {
                if (dataFlavor == OlapMetadataTransferable.LOCAL_OBJECT_FLAVOUR) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void exportAsDrag(JComponent comp, InputEvent e, int action) {
            logger.debug("exportAsDrag()");
            super.exportAsDrag(comp, e, action);
        }

        @Override
        protected void exportDone(JComponent source, Transferable data,
                int action) {
            logger.debug("exportDone()");
            super.exportDone(source, data, action);
        }

        @Override
        public void exportToClipboard(JComponent comp, Clipboard clip,
                int action) throws IllegalStateException {
            logger.debug("exportToClipboard()");
            super.exportToClipboard(comp, clip, action);
        }

        @Override
        public int getSourceActions(JComponent c) {
            logger.debug("getSourceActions()");
            return super.getSourceActions(c);
        }

        @Override
        public Icon getVisualRepresentation(Transferable t) {
            logger.debug("getVisualRepresentation()");
            return super.getVisualRepresentation(t);
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            logger.debug("importData("+t+")");
            if (t.isDataFlavorSupported(OlapMetadataTransferable.LOCAL_OBJECT_FLAVOUR)) {
                try {
                    JList list = (JList) comp;
                    int index = list.getSelectedIndex();
                    Object transferData = t.getTransferData(OlapMetadataTransferable.LOCAL_OBJECT_FLAVOUR);
                    ((DefaultListModel) list.getModel()).add(index + 1, transferData);
                    logger.debug("  -- import complete");
                    
                    executeQuery();
                    
                    return true;
                } catch (Exception e) {
                    logger.info("Error processing drop", e);
                    // note: exceptions thrown here get eaten by the DnD system
                    return false;
                }
            }
            logger.debug("  -- import failed");
            return false;
        }
    }

    /**
     * The panel that provides the query builder's GUI. This panel is created
     * and maintained by this class.
     */
    private final JPanel panel;

    /**
     * The cell set viewer that is used to display the results of queries being
     * executed. May also participate in query building (for example, the
     * components that show the axes can be drop points for adding new
     * dimensions to the query). Initialized in the constructor, and never null.
     */
    private final CellSetViewer cellSetViewer;

    /**
     * The current cube (this can be selected/changed via the GUI or the
     * {@link #setCurrentCube(Cube)} method). Null by default.
     */
    private Cube currentCube;
    
    private JList rowAxisList;
    private JList columnAxisList;
    private JTree cubeTree;
    
    public Olap4jGuiQueryPanel(final JFrame owningFrame, CellSetViewer cellSetViewer, final OlapConnection olapConnection) {
        this.cellSetViewer = cellSetViewer;
        if (cellSetViewer == null) {
            throw new NullPointerException("You must provide a non-null cell set viewer");
        }
        cubeTree = new JTree();
        cubeTree.setRootVisible(false);
        cubeTree.setCellRenderer(new Olap4JTreeCellRenderer());
        cubeTree.setDragEnabled(true);
        cubeTree.setTransferHandler(new OlapTreeTransferHandler());
        
        OlapListTransferHandler axisListTransferHandler = new OlapListTransferHandler();

        ListCellRenderer olapListCellRenderer = new Olap4jListCellRenderer();
        
        rowAxisList = new JList(new DefaultListModel());
        rowAxisList.setTransferHandler(axisListTransferHandler);
        rowAxisList.setCellRenderer(olapListCellRenderer);
        
        columnAxisList = new JList(new DefaultListModel());
        columnAxisList.setTransferHandler(axisListTransferHandler);
        columnAxisList.setCellRenderer(olapListCellRenderer);
        
        setCurrentCube(null); // inits cubetree state
        
        final JButton cubeChooserButton = new JButton("Choose Cube...");
        cubeChooserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cubeChooserButton.setEnabled(false);
                final JWindow w = new JWindow(owningFrame);
                JTree tree = new JTree(
                        new Olap4jTreeModel(
                                Collections.singletonList(olapConnection),
                                Cube.class,
                                Dimension.class));
                tree.setCellRenderer(new Olap4JTreeCellRenderer());
                int row = 0;
                while (row < tree.getRowCount()) {
                    tree.expandRow(row);
                    row++;
                }
                w.setContentPane(new JScrollPane(tree));
                w.pack();
                Point windowLocation = new Point(0, 0);
                SwingUtilities.convertPointToScreen(windowLocation, cubeChooserButton);
                w.setLocation(windowLocation);
                w.setVisible(true);
                
                tree.addTreeSelectionListener(new TreeSelectionListener() {
                    public void valueChanged(TreeSelectionEvent e) {
                        TreePath path = e.getNewLeadSelectionPath();
                        Object node = path.getLastPathComponent();
                        if (node instanceof Cube) {
                            Cube cube = (Cube) node;
                            cubeChooserButton.setEnabled(true);
                            setCurrentCube(cube);
                            w.dispose();
                        }
                    }
                });
            }
        });
        
        panel = new JPanel(new MigLayout(
                "fill",
                "[fill,grow 1][fill,grow 1][fill,grow 1]",
                "[][grow,fill,100][]"));
        panel.add(cubeChooserButton, "grow 0,left");
        panel.add(new JLabel("Rows Axis"));
        panel.add(new JLabel("Columns Axis"), "wrap");
        
        panel.add(new JScrollPane(cubeTree));
        panel.add(new JScrollPane(rowAxisList));
        panel.add(new JScrollPane(columnAxisList), "wrap");
    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    public void setCurrentCube(Cube currentCube) {
        this.currentCube = currentCube;
        if (currentCube != null) {
            cubeTree.setModel(new Olap4jTreeModel(Collections.singletonList(currentCube)));
            cubeTree.expandRow(0);
        } else {
            cubeTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Hidden")));
        }
    }
    
    /**
     * Executes the query, given the current settings in this GUI.
     */
    public void executeQuery() {
        Query mdxQuery;
        try {
            mdxQuery = new Query("GUI Query", currentCube);

            QueryAxis rows = mdxQuery.getAxes().get(Axis.ROWS);
            QueryAxis columns = mdxQuery.getAxes().get(Axis.COLUMNS);

            for (int i = 0; i < rowAxisList.getModel().getSize(); i++) {
                Object listItem = rowAxisList.getModel().getElementAt(i);
                if (listItem instanceof Dimension) {
                    Dimension d = (Dimension) listItem;
                    QueryDimension qd = new QueryDimension(mdxQuery, d);
                    Selection selection = qd.createSelection(d.getDefaultHierarchy().getDefaultMember());
                    qd.getSelections().add(selection);
                    rows.getDimensions().add(qd);
                }
            }

            for (int i = 0; i < columnAxisList.getModel().getSize(); i++) {
                Object listItem = columnAxisList.getModel().getElementAt(i);
                if (listItem instanceof Dimension) {
                    Dimension d = (Dimension) listItem;
                    QueryDimension qd = new QueryDimension(mdxQuery, d);
                    Selection selection = qd.createSelection(d.getDefaultHierarchy().getDefaultMember());
                    qd.getSelections().add(selection);
                    columns.getDimensions().add(qd);
                }
            }

            if (rows.getDimensions().isEmpty()) {
                cellSetViewer.showMessage("Rows axis is empty--please drop something on it");
                return;
            }
            
            if (columns.getDimensions().isEmpty()) {
                cellSetViewer.showMessage("Columns axis is empty--please drop something on it");
                return;
            }
            
            CellSet cellSet = mdxQuery.execute();
            cellSetViewer.showCellSet(cellSet);
        } catch (SQLException ex) {
            logger.error("failed to build/execute MDX query", ex);
            // TODO add error reporting to CellSetViewer
        }
    }
    

}
