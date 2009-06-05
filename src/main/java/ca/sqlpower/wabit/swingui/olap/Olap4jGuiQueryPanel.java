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
import java.awt.Window;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JWindow;
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
import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;
import org.olap4j.query.Query;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;

import ca.sqlpower.wabit.olap.MemberHierarchyComparator;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.OlapUtils;

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

    private AxisListener axisEventHandler = new AxisListener() {

        public void memberClicked(MemberClickEvent e) {
            try {
                toggleMember(e.getMember());
            } catch (OlapException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void memberDropped(MemberClickEvent e) {
            try {
                if (e.getAxis() == Axis.ROWS) {
                    addToRows(0, e.getMember());  // FIXME need correct ordinal in event
                } else if (e.getAxis() == Axis.COLUMNS) {
                    addToColumns(0, e.getMember());  // FIXME need correct ordinal in event
                }
            } catch (OlapException ex) {
                throw new RuntimeException(ex);
            }
        }

		public void memberRemoved(MemberClickEvent e) {
			removeHierarchy(e.getMember().getHierarchy(), e.getAxis());
		}
    };
    
    /**
     * Tracks which hierarchy we're using for each dimension in the query.
     */
    private final Map<Dimension, Hierarchy> hierarchiesBeingUsed = new HashMap<Dimension, Hierarchy>();
    
    /**
     * Tracks which members of each hierarchy in the query are in use.
     */
    private final Map<Hierarchy, Set<Member>> expandedMembers = new HashMap<Hierarchy, Set<Member>>();
    
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

    private JTree cubeTree;

    /**
     * Current hierarchies being selected on the rows axis, in the order they are
     * to appear.
     */
    private final List<Hierarchy> rowHierarchies = new ArrayList<Hierarchy>();

    /**
     * Current hierarchies being selected on the columns axis, in the order they are
     * to appear.
     */
    private final List<Hierarchy> columnHierarchies = new ArrayList<Hierarchy>();
    
    /**
     * This models the query and persists it when the view is removed.
     */
    private final OlapQuery olapQuery;
    
    public Olap4jGuiQueryPanel(final Window owningFrame, CellSetViewer cellSetViewer, OlapQuery query) throws SQLException {
        olapQuery = query;
        if (olapQuery.getMdxQueryCopy() != null) {
            for (QueryDimension queryDim : olapQuery.getMdxQueryCopy().getAxes().get(Axis.ROWS).getDimensions()) {
                for (Selection sel : queryDim.getSelections()) {
                    final Member member = sel.getMember();
                    if (!rowHierarchies.contains(member.getHierarchy())) {
                        rowHierarchies.add(member.getHierarchy());
                    }
                    hierarchiesBeingUsed.put(member.getDimension(), member.getHierarchy());
                    Set<Member> memberSet = expandedMembers.get(member.getHierarchy());
                    if (memberSet == null) {
                        memberSet = new TreeSet<Member>(new MemberHierarchyComparator());
                        expandedMembers.put(member.getHierarchy(), memberSet);
                    }
                    memberSet.add(member);
                }
            }
            for (QueryDimension queryDim : olapQuery.getMdxQueryCopy().getAxes().get(Axis.COLUMNS).getDimensions()) {
                for (Selection sel : queryDim.getSelections()) {
                    final Member member = sel.getMember();
                    if (!columnHierarchies.contains(member.getHierarchy())) {
                        columnHierarchies.add(member.getHierarchy());
                    }
                    hierarchiesBeingUsed.put(member.getDimension(), member.getHierarchy());
                    Set<Member> memberSet = expandedMembers.get(member.getHierarchy());
                    if (memberSet == null) {
                        memberSet = new TreeSet<Member>(new MemberHierarchyComparator());
                        expandedMembers.put(member.getHierarchy(), memberSet);
                    }
                    memberSet.add(member);
                }
            }
        }
        
        this.cellSetViewer = cellSetViewer;
        if (cellSetViewer == null) {
            throw new NullPointerException("You must provide a non-null cell set viewer");
        }
        
        cellSetViewer.addAxisListener(axisEventHandler);
        
        cubeTree = new JTree();
        cubeTree.setRootVisible(false);
        cubeTree.setCellRenderer(new Olap4JTreeCellRenderer());
        cubeTree.setDragEnabled(true);
        cubeTree.setTransferHandler(new OlapTreeTransferHandler());
        
        setCurrentCube(olapQuery.getCurrentCube()); // inits cubetree state
        
        final JButton cubeChooserButton = new JButton("Choose Cube...");
        cubeChooserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cubeChooserButton.setEnabled(false);
                final JWindow w = new JWindow(owningFrame);
                JTree tree;
                try {
                    tree = new JTree(
                            new Olap4jTreeModel(
                                    Collections.singletonList(olapQuery.createOlapConnection()),
                                    Cube.class,
                                    Dimension.class));
                } catch (Exception e1) {
                    throw new RuntimeException(e1);
                }
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
                        try {
                            TreePath path = e.getNewLeadSelectionPath();
                            Object node = path.getLastPathComponent();
                            if (node instanceof Cube) {
                                Cube cube = (Cube) node;
                                cubeChooserButton.setEnabled(true);
                                setCurrentCube(cube);
                                w.dispose();
                            }
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
            }
        });
        
        panel = new JPanel(new MigLayout(
                "fill",
                "[fill,grow 1]",
                "[][grow,fill]"));
        panel.add(cubeChooserButton, "grow 0,left,wrap");

        panel.add(new JScrollPane(cubeTree), "wrap");
        
    }
    
    private void removeHierarchy(Hierarchy hierarchy, Axis axis) {
    	expandedMembers.remove(hierarchy);
    	hierarchiesBeingUsed.remove(hierarchy);
    	if (axis == Axis.ROWS) {
    		logger.debug("Removing Hierarchy " + hierarchy.getName() + " from Rows");
    		rowHierarchies.remove(hierarchy);
    		logger.debug("Content of rowHierarchies after: " + rowHierarchies);
    	} else if (axis == Axis.COLUMNS) {
    		logger.debug("Removing Hierarchy " + hierarchy.getName() + " from Columns");
    		columnHierarchies.remove(hierarchy);
    		logger.debug("Content of columnHierarchies after: " + columnHierarchies);
    	}
    	executeQuery();
    }

	public JPanel getPanel() {
        return panel;
    }
    
    public void setCurrentCube(Cube currentCube) throws SQLException {
        olapQuery.setCurrentCube(currentCube);
        if (currentCube != null) {
            cubeTree.setModel(new Olap4jTreeModel(Collections.singletonList(currentCube)));
            cubeTree.expandRow(0);
        } else {
            cubeTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Hidden")));
        }
        
        if (currentCube != olapQuery.getCurrentCube()) {
            expandedMembers.clear();
            rowHierarchies.clear();
            columnHierarchies.clear();
        }
    }

    /**
     * Executes the query, given the current settings in this GUI. Returns right
     * away if there's no current query (which is the same as when there's no
     * current cube).
     */
    public void executeQuery() {
        Query mdxQuery;
        try {
            mdxQuery = olapQuery.getMdxQueryCopy();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        if (mdxQuery == null) {
            cellSetViewer.showMessage("No cube selected--please select one from the dropdown list");
            return;
        }
        try {
            QueryAxis rows = mdxQuery.getAxes().get(Axis.ROWS);
            QueryAxis columns = mdxQuery.getAxes().get(Axis.COLUMNS);
            logger.debug("Contents of rowHierarchies: " + rowHierarchies);
            logger.debug("Contents of columnHierarchies: " + columnHierarchies);
            setupAxis(rows, rowHierarchies);
            setupAxis(columns, columnHierarchies);
            olapQuery.setMdxQuery(mdxQuery);
            
            if (rows.getDimensions().isEmpty() && columns.getDimensions().isEmpty()) {
            	cellSetViewer.showMessage("No query defined");
            	return;
            }
            
            if (rows.getDimensions().isEmpty()) {
                cellSetViewer.showMessage("Rows axis is empty--please drop something on it", rowHierarchies, columnHierarchies);
                return;
            }
            
            if (columns.getDimensions().isEmpty()) {
                cellSetViewer.showMessage("Columns axis is empty--please drop something on it", rowHierarchies, columnHierarchies);
                return;
            }
            
            CellSet cellSet = mdxQuery.execute();
            cellSetViewer.showCellSet(cellSet);
        } catch (SQLException ex) {
            logger.error("failed to build/execute MDX query", ex);
            cellSetViewer.showMessage("Query failed: " + ex.getMessage());
        }
    }

    private void setupAxis(QueryAxis axis, List<Hierarchy> hierarchies) {
        axis.getDimensions().clear(); // XXX not optimal--the rest of this class could manipulate the query directly
        logger.debug("Setting up " + axis.getName() + " axis");
        Query mdxQueryCopy;
        try {
            mdxQueryCopy = olapQuery.getMdxQueryCopy();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        for (Hierarchy h : hierarchies) {
            Dimension d = h.getDimension();
            logger.debug("  Processing dimension " + d.getName());
            QueryDimension qd = new QueryDimension(mdxQueryCopy, d);
            for (Member m : expandedMembers.get(h)) {
                logger.debug("    Creating selection for member " + m.getName());
                Selection selection = qd.createSelection(m);
                qd.getSelections().add(selection);
            }
            axis.getDimensions().add(qd);
        }
    }
    
    /**
     * If the member is currently "expanded" (its children are part of the MDX
     * query), its children will be removed from the query. Otherwise (the
     * member's children are not showing), the member's children will be added
     * to the query. In either case, the query will be re-executed after the
     * member selections have been adjusted.
     * 
     * @param member The member whose drilldown state to toggle. Must not be null.
     * @throws OlapException if the list of child members can't be retrieved
     */
    private void toggleMember(Member member) throws OlapException {
        // XXX probably best to manipulate mdxQuery object directly now!
        Hierarchy h = member.getHierarchy();
        Set<Member> memberSet = expandedMembers.get(h);
        if (memberSet == null) {
            memberSet = new TreeSet<Member>(new MemberHierarchyComparator());
            expandedMembers.put(h, memberSet);
        }
        
        NamedList<? extends Member> childMembers = member.getChildMembers();
        if ( (!childMembers.isEmpty()) && memberSet.containsAll(childMembers)) {
            logger.debug("toggleMember(): removing member " + member.getName() + " and its descendants");
            Iterator<Member> it = memberSet.iterator();
            while (it.hasNext()) {
                if (OlapUtils.isDescendant(member, it.next())) {
                    it.remove();
                }
            }
        } else {
            logger.debug("toggleMember(): adding member " + member.getName() + " and its children");
            memberSet.add(member);
            memberSet.addAll(childMembers);
        }
        
        executeQuery();
    }

    private void addToRows(int ordinal, Member m) throws OlapException {
        Hierarchy h = m.getHierarchy();
        Dimension d = h.getDimension();
        if (hierarchiesBeingUsed.containsKey(d)) {
            removeDimensionFromQuery(d);
        }
        hierarchiesBeingUsed.put(d, h);
        if (!rowHierarchies.contains(h)) {
            logger.debug("Adding Hierarchy '" + h.getName() + "' to rows");
            rowHierarchies.add(h);
        }
        toggleMember(m);
    }

    private void addToColumns(int ordinal, Member m) throws OlapException {
        Hierarchy h = m.getHierarchy();
        Dimension d = h.getDimension();
        if (hierarchiesBeingUsed.containsKey(d)) {
            removeDimensionFromQuery(d);
        }
        hierarchiesBeingUsed.put(d, h);
        if (!columnHierarchies.contains(h)) {
            logger.debug("Adding Hierarchy '" + h.getName() + "' to columns");
            columnHierarchies.add(ordinal, h);
        }
        toggleMember(m);
    }
    
    /**
     * Removes the given dimension from all axes of this query.
     * 
     * @param d The dimension to remove from the query.
     */
    private void removeDimensionFromQuery(Dimension d) {
        rowHierarchies.removeAll(d.getHierarchies());
        columnHierarchies.removeAll(d.getHierarchies());
    }

}
