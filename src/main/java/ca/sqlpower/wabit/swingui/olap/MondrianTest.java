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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.UndoableEditListener;

import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;

import ca.sqlpower.sql.DataSourceCollection;
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
import ca.sqlpower.wabit.OlapConnectionMapping;
import ca.sqlpower.wabit.olap.OlapConnectionPool;
import ca.sqlpower.wabit.olap.OlapQuery;

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
}
