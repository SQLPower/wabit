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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
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
import org.olap4j.mdx.ParseTreeWriter;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;
import org.olap4j.query.Query;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.DatabaseListChangeEvent;
import ca.sqlpower.sql.DatabaseListChangeListener;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.swingui.querypen.QueryPen;
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

        public void memberClicked(MemberEvent e) {
            try {
                toggleMember(e.getMember());
            } catch (OlapException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void memberDropped(MemberDroppedEvent e) {
            try {
            	addToAxis(e.getOrdinal(), e.getMember(), e.getAxis());
            } catch (OlapException ex) {
                throw new RuntimeException(ex);
            }
        }

		public void memberRemoved(MemberEvent e) {
			removeHierarchy(e.getMember().getHierarchy(), e.getAxis());
		}

        public void memberExcluded(MemberEvent e) {
            excludeMember((MemberExcludedEvent)e);
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
	 * This {@link JToolBar} is at the top of the Olap4jGuiQueryPanel, it contains the
	 * traditional buttons at the top and is similar to the queryPenBar in the
	 * {@link QueryPen} for relational queries
	 */
    private JToolBar olapPanelToolbar;
    
    /**
     * This is the {@link JButton} which will reset the Olap4j {@link Query} in the table below.
     * This will allow a user to start their query over without going through the painful and
     * slow steps required to remove each hierarchy. Additionally if the user somehow gets their
     * query into a broken state they can just easily restart.
     */
    private final JButton resetQueryButton;

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
    /**
     * Keeps a link to the parent OlapQueryPanel
     */
    private final OlapQueryPanel olapQueryPanel;
    
	/**
	 * A {@link JComboBox} containing a list of OLAP data sources to choose from
	 * to use for the OLAP query
	 */
    private JComboBox databaseComboBox;
    
	/**
	 * A {@link DataSourceCollection} of {@link Olap4jDataSource}es to choose
	 * from for running the OLAP query
	 */
    private DataSourceCollection<Olap4jDataSource> dsCollection;
    
    
    /**
     * This recreates the database combo box when the list of databases changes.
     */
    private DatabaseListChangeListener dbListChangeListener = new DatabaseListChangeListener() {

        public void databaseAdded(DatabaseListChangeEvent e) {
            if (!(e.getDataSource() instanceof Olap4jDataSource)) return;
        	logger.debug("dataBase added");
            databaseComboBox.addItem(e.getDataSource());
            databaseComboBox.revalidate();
        }

        public void databaseRemoved(DatabaseListChangeEvent e) {
            if (!(e.getDataSource() instanceof Olap4jDataSource)) return;
        	logger.debug("dataBase removed");
            if (databaseComboBox.getSelectedItem() != null && databaseComboBox.getSelectedItem().equals(e.getDataSource())) {
                databaseComboBox.setSelectedItem(null);
            }
            
            databaseComboBox.removeItem(e.getDataSource());
            databaseComboBox.revalidate();
        }
        
    };

    /**
     * Creates 
     * @param dsCollection
     * @param owningFrame
     * @param cellSetViewer
     * @param query
     * @param textQueryPanel 
     * @throws SQLException
     */
    public Olap4jGuiQueryPanel(DataSourceCollection<Olap4jDataSource> dsCollection, final Window owningFrame, CellSetViewer cellSetViewer, OlapQuery query, OlapQueryPanel olapQueryPanel) throws SQLException {
        this.olapQuery = query;
        this.olapQueryPanel = olapQueryPanel;
        this.dsCollection = dsCollection;
        if (olapQuery.getMDXQuery() != null) {
            for (QueryDimension queryDim : olapQuery.getMDXQuery().getAxes().get(Axis.ROWS).getDimensions()) {
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
            for (QueryDimension queryDim : olapQuery.getMDXQuery().getAxes().get(Axis.COLUMNS).getDimensions()) {
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
            	if (databaseComboBox.getSelectedItem() == null) {
            		JOptionPane.showMessageDialog(panel, 
            				"Please choose a database from the above list first", 
            				"Choose a database", 
            				JOptionPane.WARNING_MESSAGE);
            		return;
            	}
                try {
	            	cubeChooserButton.setEnabled(false);
	                //final JWindow w = new JWindow(owningFrame);
	                final JPopupMenu p = new JPopupMenu();
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
	                Point windowLocation = new Point(0, 0);
	                SwingUtilities.convertPointToScreen(windowLocation, cubeChooserButton);
	                p.add(new JScrollPane(tree));
	                p.pack();
	                windowLocation.y += cubeChooserButton.getHeight();
	                p.setLocation(windowLocation);
	                p.setVisible(true);
	                	
	                
	                tree.addTreeSelectionListener(new TreeSelectionListener() {
	                    public void valueChanged(TreeSelectionEvent e) {
	                        try {
	                            TreePath path = e.getNewLeadSelectionPath();
	                            Object node = path.getLastPathComponent();
	                            if (node instanceof Cube) {
	                                Cube cube = (Cube) node;
	                                cubeChooserButton.setEnabled(true);
	                                setCurrentCube(cube);
	                                p.setVisible(false);
	                            }
	                        } catch (SQLException ex) {
	                            throw new RuntimeException(ex);
	                        }
	                    }
	                });
                } finally {
                	cubeChooserButton.setEnabled(true);
                }
            }
        });
        resetQueryButton = new JButton();
        resetQueryButton.setIcon(new ImageIcon(Olap4jGuiQueryPanel.class.getClassLoader().getResource("icons/reset.png")));
        resetQueryButton.setToolTipText("Reset Query");
        resetQueryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				olapQuery.resetMDXQuery();
				hierarchiesBeingUsed.clear();
				expandedMembers.clear();
				rowHierarchies.clear();
				columnHierarchies.clear();
				executeQuery();
			}
        });
        
        databaseComboBox = new JComboBox(dsCollection.getConnections(Olap4jDataSource.class).toArray());
        databaseComboBox.setSelectedItem(olapQuery.getOlapDataSource());
        databaseComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Object item = e.getItem();
				if (item instanceof Olap4jDataSource) {
					olapQuery.setOlapDataSource((Olap4jDataSource) item);
					try {
						setCurrentCube(null);
					} catch (SQLException ex) {
						throw new RuntimeException(
								"SQL exception occured while trying to set the current cube to null",
								ex);
					}
				}
			}
        });
        this.dsCollection.addDatabaseListChangeListener(dbListChangeListener);
        
        panel = new JPanel(new MigLayout(
                "fill",
                "[fill,grow 1]",
                "[][][grow,fill]"));
        panel.add(databaseComboBox, "wrap");
        panel.add(cubeChooserButton, "grow 0,left,wrap");
        panel.add(new JScrollPane(cubeTree), "wrap");
        
    }
    
    private void removeHierarchy(Hierarchy hierarchy, Axis axis) {
    	expandedMembers.remove(hierarchy);
    	hierarchiesBeingUsed.remove(hierarchy.getDimension());
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

    private void excludeMember(MemberExcludedEvent e) {
        Member member = e.getMember();
        olapQuery.excludeMember(
                member.getDimension().getName(), 
                member, 
                e.getOperator());
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
//        try {
            mdxQuery = olapQuery.getMDXQuery();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }

            if (mdxQuery == null) {
            cellSetViewer.showMessage("No cube selected--please select one from the dropdown list");
            return;
        }
        try {
            QueryAxis rows = mdxQuery.getAxes().get(Axis.ROWS);
            QueryAxis columns = mdxQuery.getAxes().get(Axis.COLUMNS);
            logger.debug("Contents of rowHierarchies: " + rowHierarchies);
            logger.debug("Contents of columnHierarchies: " + columnHierarchies);
            setupAxis(mdxQuery, rows, rowHierarchies);
            setupAxis(mdxQuery, columns, columnHierarchies);
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
            
            StringWriter sw = new StringWriter();
            ParseTreeWriter ptw = new ParseTreeWriter(new PrintWriter(sw));
            mdxQuery.getSelect().unparse(ptw);
            logger.debug("Executing MDX Query : \n\r".concat(sw.toString()));
            
            CellSet cellSet = mdxQuery.execute();
            cellSetViewer.showCellSet(cellSet);
            this.olapQueryPanel.updateMdxText(sw.toString());
        } catch (SQLException ex) {
            logger.error("failed to build/execute MDX query", ex);
            cellSetViewer.showMessage("Query failed: " + ex.getMessage());
        }
    }

    private void setupAxis(Query mdxQuery, QueryAxis axis, List<Hierarchy> hierarchies) {
        axis.getDimensions().clear(); // XXX not optimal--the rest of this class could manipulate the query directly
        logger.debug("Setting up " + axis.getName() + " axis");
        
        for (Hierarchy h : hierarchies) {
            Dimension d = h.getDimension();
            logger.debug("  Processing dimension " + d.getName());
            QueryDimension qd = new QueryDimension(mdxQuery, d);
            for (Member m : expandedMembers.get(h)) {
                logger.debug("    Creating selection for member " + m.getName());
                qd.select(m);
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

    private void addToAxis(int ordinal, Member m, Axis a) throws OlapException {
    	if (ordinal < 0) {
    		throw new IllegalArgumentException("Ordinal " + ordinal + " is less than 0!");
    	}
    	List<Hierarchy> axisHierarchies;
    	if (a == Axis.ROWS) {
    		axisHierarchies = rowHierarchies;
    	} else if (a == Axis.COLUMNS) {
    		axisHierarchies = columnHierarchies;
    	} else {
    		throw new IllegalArgumentException(
    				"I only know how to add to the ROWS or COLUMNS axis," +
    				" but you asked for " + a);
    	}
        Hierarchy h = m.getHierarchy();
        Dimension d = h.getDimension();
        if (hierarchiesBeingUsed.containsKey(d)) {
        	Hierarchy hierarchyToRemove = hierarchiesBeingUsed.get(d);
        	int indexInAxis = axisHierarchies.indexOf(hierarchyToRemove);
			if (indexInAxis >= 0 && indexInAxis < ordinal) {
				// adjust for inserting new member after something that has been removed 
        		ordinal--;
        	}
			if (!(m instanceof Measure)) {
				expandedMembers.remove(h);
			}
            removeDimensionFromQuery(d);
        }
        hierarchiesBeingUsed.put(d, h);
        if (!axisHierarchies.contains(h)) {
            logger.debug("Adding Hierarchy '" + h.getName() + "' to " + a + " with ordinal " + ordinal);
            axisHierarchies.add(ordinal, h);
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
    
    public JToolBar getOlapPanelToolbar() {
		return olapPanelToolbar;
	}

	public void setOlapPanelToolbar(JToolBar olapToolbar) {
		this.olapPanelToolbar = olapToolbar;
	}
	
	public JButton getResetQueryButton() {
		return resetQueryButton;
	}
}
