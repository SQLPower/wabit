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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Window;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.query.Query;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.DatabaseListChangeEvent;
import ca.sqlpower.sql.DatabaseListChangeListener;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SpecificDataSourceCollection;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.db.Olap4jConnectionPanel;
import ca.sqlpower.swingui.querypen.QueryPen;
import ca.sqlpower.wabit.OlapConnectionMapping;
import ca.sqlpower.wabit.olap.OlapConnectionPool;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.OlapQueryEvent;
import ca.sqlpower.wabit.olap.OlapQueryListener;
import ca.sqlpower.wabit.olap.QueryInitializationException;

public class MondrianTest {

    private final JFrame frame;
    private final CellSetViewer cellSetViewer;
    private final OlapQuery olapQuery;

    public MondrianTest(OlapQuery olapQuery) throws NamingException, IOException, URISyntaxException, ClassNotFoundException, SQLException {
        this.olapQuery = olapQuery;
        JTree tree = new JTree(new Olap4jTreeModel(Collections.singletonList(olapQuery.createOlapConnection())));
        tree.setCellRenderer(new Olap4JTreeCellRenderer());
        tree.setRootVisible(false);

        cellSetViewer = new CellSetViewer(olapQuery);
        
        JTabbedPane queryPanels = new JTabbedPane();
        JComponent mdxPanel = createTextQueryPanel();
        queryPanels.add("MDX", mdxPanel);
        queryPanels.add("GUI", createGuiQueryPanel(olapQuery.getOlapDataSource()));
        
        JSplitPane queryAndResultsPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        queryAndResultsPanel.setTopComponent(queryPanels);
        queryAndResultsPanel.setBottomComponent(cellSetViewer.getViewComponent());
        
        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(new JScrollPane(tree));
        splitPane.setRightComponent(queryAndResultsPanel);
        
        frame = new JFrame("MDX Explorererer");
        frame.setContentPane(splitPane);
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
        	@Override
        	public void windowClosing(WindowEvent e) {
        		System.exit(0);
        	}
        });
    }
    
    private JComponent createGuiQueryPanel(final Olap4jDataSource ds) throws SQLException {
        return new Olap4jGuiQueryPanel(new StubOlapDataSourceCollection(ds),
        		frame, cellSetViewer, olapQuery, null).getPanel();
    }

    private JComponent createTextQueryPanel() throws SQLException, ClassNotFoundException, NamingException {
        final JTextArea mdxQuery = new JTextArea();
        mdxQuery.setText(
               "with" +
               "\n member Store.[USA Total] as '[Store].[USA]', solve_order = 1" +
               "\n member Product.DrinkPct as '100 * (Product.Drink, Store.CurrentMember) / (Product.Drink, Store.USA)', solve_order = 2" +
               "\nselect" +
               "\n {[Store].[All Stores].[USA].[CA], [Store].[All Stores].[USA].[OR], [Store].[All Stores].[USA].[WA], Store.USA} ON COLUMNS," +
               "\n crossjoin(" +
               "\n  {[Gender].Children}," +
               "\n  {" +
               "\n   hierarchize(union(union(" +
               "\n    [Product].[All Products].[Drink].Children," +
               "\n    [Product].[All Products].[Drink].[Alcoholic Beverages].Children)," +
               "\n    [Product].[All Products].[Drink].[Alcoholic Beverages].[Beer and Wine].Children" +
               "\n  ))," +
               "\n  [Product].[Drink], Product.DrinkPct" +
               "\n }) ON ROWS" +
               "\nfrom [Sales]" +
               "\nwhere [Time].[1997]");
        final OlapStatement statement = olapQuery.createOlapConnection().createStatement();
        JButton executeButton = new JButton("Execute");
        executeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CellSet cellSet;
                try {
                    cellSet = statement.executeOlapQuery(mdxQuery.getText());
                } catch (OlapException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "FAIL\n" + e1.getMessage());
                    return;
                }

                cellSetViewer.showCellSet(olapQuery, cellSet);
            }
        });
        
        JPanel queryPanel = new JPanel(new BorderLayout());
        queryPanel.add(new JScrollPane(mdxQuery), BorderLayout.CENTER);
        queryPanel.add(executeButton, BorderLayout.SOUTH);

        return queryPanel;
    }

    public static void main(String[] args) throws Exception {
        Preferences prefs = Preferences.userNodeForPackage(MondrianTest.class);
        
        PlDotIni plIni = new PlDotIni();
        plIni.read(new File(System.getProperty("user.home"), "pl.ini"));
        
        final Olap4jDataSource olapDataSource = new Olap4jDataSource(plIni);
        olapDataSource.setMondrianSchema(new URI(prefs.get("mondrianSchemaURI", "")));
        final JDBCDataSource ds = plIni.getDataSource(prefs.get("mondrianDataSource", null), JDBCDataSource.class);
        final SQLDatabase db = new SQLDatabase(ds);
        olapDataSource.setDataSource(ds);
        
        Olap4jConnectionPanel dep = new Olap4jConnectionPanel(olapDataSource, new SpecificDataSourceCollection<JDBCDataSource>(plIni, JDBCDataSource.class));
        JFrame dummyFrame = new JFrame();
        dummyFrame.setSize(0, 0);
        dummyFrame.setLocation(-100, -100);
        dummyFrame.setVisible(true);
        JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(dep, dummyFrame, "Proof of concept", "OK");
        d.setModal(true);
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        if (olapDataSource.getType() == null) {
            return;
        }
        dummyFrame.dispose();
        
        prefs.put("mondrianSchemaURI", olapDataSource.getMondrianSchema().toString());
        prefs.put("mondrianDataSource", olapDataSource.getDataSource().getName());
        
        OlapQuery olapQuery = new OlapQuery(new OlapConnectionMapping() {
            final OlapConnectionPool olapPool = new OlapConnectionPool(olapDataSource, new SQLDatabaseMapping() {
            
                public SQLDatabase getDatabase(JDBCDataSource ds) {
                    return db;
                }
            });
        
            public OlapConnection createConnection(Olap4jDataSource dataSource)
                    throws SQLException, ClassNotFoundException, NamingException {
                return olapPool.getConnection();
            }
        });
        olapQuery.setOlapDataSource(olapDataSource);
        
        new MondrianTest(olapQuery);
    }
    
    private class StubOlapDataSourceCollection implements DataSourceCollection<Olap4jDataSource> {

    	private Olap4jDataSource ds;
    	List<Olap4jDataSource> dataSources = new ArrayList<Olap4jDataSource>();

		public StubOlapDataSourceCollection(Olap4jDataSource ds) {
    		this.ds = ds;
    		dataSources.add(ds);
    	}
    	
    	private final List<JDBCDataSourceType> dsTypes = new ArrayList<JDBCDataSourceType>();
    	private final List<UndoableEditListener> undoableEdits = new ArrayList<UndoableEditListener>();
    	private final List<DatabaseListChangeListener> dbListChangeListeners = new ArrayList<DatabaseListChangeListener>();
        private URI serverBaseURI;

    	public void addDataSource(Olap4jDataSource dbcs) {
    		dataSources.add(dbcs);
    	}

    	public void addDataSourceType(JDBCDataSourceType dataSourceType) {
    		dsTypes.add(dataSourceType);
    	}

    	public void addDatabaseListChangeListener(DatabaseListChangeListener l) {
    		dbListChangeListeners.add(l);
    	}

    	public void addUndoableEditListener(UndoableEditListener l) {
    		undoableEdits.add(l);
    	}

    	public List<Olap4jDataSource> getConnections() {
    		return Collections.unmodifiableList(dataSources);
    	}

        public <C extends Olap4jDataSource> C getDataSource(String name,
                Class<C> classType) {
            return (C) dataSources;
        }

    	public List<JDBCDataSourceType> getDataSourceTypes() {
    		return Collections.unmodifiableList(dsTypes);
    	}

    	public void mergeDataSource(Olap4jDataSource dbcs) {
    		dataSources.add(dbcs);
    	}

    	public void mergeDataSourceType(JDBCDataSourceType dst) {
    		dsTypes.add(dst);
    	}

    	public void read(File location) throws IOException {
    		throw new UnsupportedOperationException("Unsupported in the current stub implementation");
    	}

    	public void read(InputStream in) throws IOException {
    		throw new UnsupportedOperationException("Unsupported in the current stub implementation");
    	}

    	public void removeDataSource(Olap4jDataSource dbcs) {
    		dataSources.remove(dbcs);
    	}

    	public boolean removeDataSourceType(JDBCDataSourceType dataSourceType) {
    		return dsTypes.remove(dataSourceType);
    	}

    	public void removeDatabaseListChangeListener(DatabaseListChangeListener l) {
    		dbListChangeListeners.remove(l);
    	}

    	public void removeUndoableEditListener(UndoableEditListener l) {
    		undoableEdits.remove(l);
    	}

    	public void write() throws IOException {
    		throw new UnsupportedOperationException("Unsupported in the current stub implementation");
    	}

    	public void write(File location) throws IOException {
    		throw new UnsupportedOperationException("Unsupported in the current stub implementation");
    	}

    	public void write(OutputStream out) throws IOException {
            throw new UnsupportedOperationException("Unsupported in the current stub implementation");
    	}

        public URI getServerBaseURI() {
            return serverBaseURI;
        }

        public void setServerBaseURI(URI serverBaseURI) {
            this.serverBaseURI = serverBaseURI;
        }

        public <C extends Olap4jDataSource> List<C> getConnections(Class<C> classType) {
            return (List<C>) dataSources;
        }

        public Olap4jDataSource getDataSource(String name) {
            for (Olap4jDataSource ds : dataSources) {
                if (ds.getName().equals(name)) {
                    return ds;
                }
            }
            return null;
        }
    }
    
    
    private static class Olap4jGuiQueryPanel {

        private static final Logger logger = Logger.getLogger(Olap4jGuiQueryPanel.class);
        
        public class OlapTreeTransferHandler extends TransferHandler {
            @Override
            protected Transferable createTransferable(JComponent c) {
                logger.debug("tree handler: createTransferable()");
                JTree tree = (JTree) c;
                TreePath selectionPath = tree.getSelectionPath();
                if (selectionPath == null) return null;
                List<Object> selectedItems = new ArrayList<Object>();
                for (TreePath path : tree.getSelectionPaths()) {
                	selectedItems.add(path.getLastPathComponent());
                }
                return new OlapMetadataTransferable(selectedItems.toArray());
            }

            @Override
            public int getSourceActions(JComponent c) {
                logger.debug("tree handler: getSourceActions()");
                return MOVE;
            }
        }

        /**
         * The panel that provides the query builder's GUI. This panel is created
         * and maintained by this class.
         */
        private final JPanel panel;
        
        /**
         * The popup factory which creates the cube chooser window
         */
        private final PopupFactory pFactory = new PopupFactory();

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
         * This class is mainly used so that the CubeTree can be a DragSourceListener.
         * This enables our own Drag gesture etc... to be added to the cube tree
         * which is good because this means mac's can drag on one click and drag
         * instead of selecting then dragging.
         */
        public class CubeTree extends JTree implements DragSourceListener {
            public CubeTree() {
                setRootVisible(false);
                setCellRenderer(new Olap4JTreeCellRenderer());
            }
            
            public void dragDropEnd(DragSourceDropEvent dsde) {
                //Do nothing
            }

            public void dragEnter(DragSourceDragEvent dsde) {
                //Do nothing
            }

            public void dragExit(DragSourceEvent dse) {
                //Do nothing
            }

            public void dragOver(DragSourceDragEvent dsde) {
                //Do nothing
            }

            public void dropActionChanged(DragSourceDragEvent dsde) {
                //Do nothing
            }
            
        }
        
        /**
         * This class is the cube trees drag gesture listener, it starts the drag
         * process when necessary.
         */
        private class CubeTreeDragGestureListener implements DragGestureListener {
            public void dragGestureRecognized(DragGestureEvent dge) {
                dge.getSourceAsDragGestureRecognizer().setSourceActions(DnDConstants.ACTION_COPY);
                CubeTree t = (CubeTree) dge.getComponent();
                List<Object> objectList = new ArrayList<Object>();
                for (TreePath path : t.getSelectionPaths()) {
                	objectList.add(path.getLastPathComponent());
                }
                dge.getDragSource().startDrag(dge, null, new OlapMetadataTransferable(objectList.toArray()), t);
            }
        }
        
        /**
         * This class is for the MondrianTest class only! No other classes should access it!
         */
        Olap4jGuiQueryPanel(
                DataSourceCollection<Olap4jDataSource> dsCollection,
                final Window owningFrame, CellSetViewer cellSetViewer,
                OlapQuery query, OlapQueryPanel olapQueryPanel) throws SQLException {
            this.olapQuery = query;
            this.olapQueryPanel = olapQueryPanel;
            this.dsCollection = dsCollection;

            query.addOlapQueryListener(new OlapQueryListener() {
                public void queryExecuted(OlapQueryEvent e) {
                    updateCellSetViewer(e.getCellSet());
                }
            });
            
            this.cellSetViewer = cellSetViewer;
            if (cellSetViewer == null) {
                throw new NullPointerException("You must provide a non-null cell set viewer");
            }
            
            cubeTree = new CubeTree();
            DragSource ds = new DragSource();
            ds.createDefaultDragGestureRecognizer(cubeTree, DnDConstants.ACTION_COPY, new CubeTreeDragGestureListener());

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
                        
                        popupChooseCube(owningFrame, cubeChooserButton, tree);
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
                    try {
                        olapQuery.reset();
                    } catch (SQLException e1) {
                        throw new RuntimeException(e1);
                    }
                    updateCellSetViewer(null);
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
                    "[ | | grow,fill ]"));
            panel.add(databaseComboBox, "wrap");
            panel.add(cubeChooserButton, "grow 0,left,wrap");
            panel.add(new JScrollPane(cubeTree), "spany, wrap");
        }

        private Window owningFrame = null;
        private Popup popup = null;
        private JPanel glassPane = null;
        
        
        private MouseAdapter clickListener = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                popup.hide();
                glassPane.removeMouseListener(this);
                owningFrame.removeComponentListener(resizeListener);
            }
        };
        
        private ComponentListener resizeListener = new ComponentListener() {

            public void componentHidden(ComponentEvent e) {
                //Do nothing
            }

            public void componentMoved(ComponentEvent e) {
                popup.hide();
                owningFrame.removeComponentListener(this);
                glassPane.removeMouseListener(clickListener);
            }

            public void componentResized(ComponentEvent e) {
                //Do nothing
                
            }

            public void componentShown(ComponentEvent e) {
                //Do nothing
            }
            
        };
        
        /**
         * This function will popup the Cube chooser popup from the 'Choose Cube'
         * button. It will popup the window in the bounds of the screen no matter
         * what happens, it will add scrollbars in the right circumstances as well
         * 
         * @param owningFrame
         *      The frame which the popup is being popped up on
         * @param cubeChooserButton
         *      The button which pops up the cube chooser
         * @param tree
         *      The tree in the cube chooser
         */
        private void popupChooseCube(final Window owningFrame,
                final JButton cubeChooserButton, JTree tree) {
            this.owningFrame = owningFrame;
            glassPane = new JPanel();
            
            JScrollPane treeScroll = new JScrollPane(tree);
            treeScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            treeScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            Point windowLocation = new Point(0, 0);
            SwingUtilities.convertPointToScreen(windowLocation, cubeChooserButton);
            windowLocation.y += cubeChooserButton.getHeight();
            
            Point frameLocation = new Point(0, 0);
            SwingUtilities.convertPointToScreen(frameLocation, owningFrame);
            
            int popupScreenSpaceY = (windowLocation.y - frameLocation.y);
            int maxHeight = (int)(owningFrame.getSize().getHeight() - popupScreenSpaceY);
            
            int width = (int) Math.min(treeScroll.getPreferredSize().getWidth(), owningFrame.getSize().getWidth());
            int height = (int) Math.min(treeScroll.getPreferredSize().getHeight(), maxHeight);
            treeScroll.setPreferredSize(new java.awt.Dimension(width, height));
            
            double popupWidth = treeScroll.getPreferredSize().getWidth();
            int popupScreenSpaceX = (int) (owningFrame.getSize().getWidth() - (windowLocation.x - frameLocation.x));
            int x;
            if (popupWidth > popupScreenSpaceX) {
                x = (int) (windowLocation.x - (popupWidth - popupScreenSpaceX));
            } else {
                x = windowLocation.x;
            }
            
            treeScroll.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.GRAY, Color.GRAY));
            
            popup = pFactory.getPopup(glassPane, treeScroll, x, windowLocation.y);
            JFrame frame = (JFrame)owningFrame;
            frame.setGlassPane(glassPane);
            glassPane.setVisible(true);
            glassPane.setOpaque(false);
            popup.show();
            
            glassPane.addMouseListener(clickListener);
            owningFrame.addComponentListener(resizeListener);
            
            tree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    try {
                        TreePath path = e.getNewLeadSelectionPath();
                        Object node = path.getLastPathComponent();
                        if (node instanceof Cube) {
                            Cube cube = (Cube) node;
                            cubeChooserButton.setEnabled(true);
                            setCurrentCube(cube);
                            glassPane.removeMouseListener(clickListener);
                            owningFrame.removeComponentListener(resizeListener);
                            popup.hide();
                        }
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
        
        public JPanel getPanel() {
            return panel;
        }

        /**
         * Sets the current cube to the given cube. This affects the tree of items
         * that can be dragged into the query builder, and it resets the query
         * builder.
         * 
         * @param currentCube
         *            The new cube to make current. If this is already the current
         *            cube, the query will not be reset. Can be null to revert to
         *            the "no cube selected" state.
         * @throws SQLException
         */
        public void setCurrentCube(Cube currentCube) throws SQLException {
            if (currentCube != olapQuery.getCurrentCube()) {
                olapQuery.setCurrentCube(currentCube);
                updateCellSetViewer(null); 
            }
            if (currentCube != null) {
                cubeTree.setModel(new Olap4jTreeModel(Collections.singletonList(currentCube)));
                cubeTree.expandRow(0);
            } else {
                cubeTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Hidden")));
            }
        }

        /**
         * Updates the {@link CellSetViewer} with the contents of the given
         * {@link CellSet}. If the CellSet is null or no cube is currently chosen,
         * then it will return immediately.
         * 
         * @param cellSet
         *            The CellSet containing the results to update the
         *            CellSetViewer. If CellSet is null, then instead of displaying
         *            the results, this method will display an message as to what is
         *            missing from the query and then return.
         */
        public void updateCellSetViewer(CellSet cellSet) {
            if (olapQuery.getCurrentCube() == null) {
                cellSetViewer.showMessage(olapQuery, "No cube selected--please select one from the dropdown list");
                return;
            }
            
            if (cellSet == null) {
                List<Hierarchy> rowHierarchies;
                List<Hierarchy> columnHierarchies;
                try {
                    rowHierarchies = olapQuery.getRowHierarchies();
                    columnHierarchies = olapQuery.getColumnHierarchies();
                } catch (QueryInitializationException e) {
                    throw new RuntimeException(e);
                }
                
                if (rowHierarchies.isEmpty() && !columnHierarchies.isEmpty()) {
                    cellSetViewer.showMessage(olapQuery, "Rows axis is empty--please drop something on it");
                } else if (columnHierarchies.isEmpty() && !rowHierarchies.isEmpty()) {
                    cellSetViewer.showMessage(olapQuery, "Columns axis is empty--please drop something on it");
                } else {
                    cellSetViewer.showMessage(olapQuery, "No query defined");
                }
                return;
            }
            
            cellSetViewer.showCellSet(olapQuery, cellSet);
            try {
                olapQueryPanel.updateMdxText(olapQuery.getMdxText());
            } catch (Exception ex) {
                olapQueryPanel.updateMdxText("Exception thrown while retrieving MDX statement:\n" + ex.getMessage());
            }
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

    
    
}
