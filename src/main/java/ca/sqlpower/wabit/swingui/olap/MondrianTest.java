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
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.prefs.Preferences;

import javax.naming.Context;
import javax.naming.InitialContext;
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

import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;

import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.wabit.olap.DataSourceAdapter;
import ca.sqlpower.wabit.olap.Olap4jDataSource;

public class MondrianTest {

    private final JFrame frame;
    private final CellSetViewer cellSetViewer;
    private final OlapConnection olapConnection;

    public MondrianTest(OlapConnection olapConnection) throws NamingException, IOException, URISyntaxException, ClassNotFoundException, SQLException {
        this.olapConnection = olapConnection;
        JTree tree = new JTree(new Olap4jTreeModel(Collections.singletonList(olapConnection)));
        tree.setCellRenderer(new Olap4JTreeCellRenderer());
        tree.setRootVisible(false);

        cellSetViewer = new CellSetViewer();
        
        JTabbedPane queryPanels = new JTabbedPane();
        queryPanels.add("GUI", createGuiQueryPanel());
        queryPanels.add("MDX", createTextQueryPanel());
        
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
    
    private JComponent createGuiQueryPanel() throws SQLException {
        return new Olap4jGuiQueryPanel(frame, cellSetViewer, olapConnection).getPanel();
    }

    private JComponent createTextQueryPanel() throws OlapException {
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
        final OlapStatement statement = olapConnection.createStatement();
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

                cellSetViewer.showCellSet(cellSet);
            }
        });
        
        JPanel queryPanel = new JPanel(new BorderLayout());
        queryPanel.add(new JScrollPane(mdxQuery), BorderLayout.CENTER);
        queryPanel.add(executeButton, BorderLayout.SOUTH);

        return queryPanel;
    }

    public static void main(String[] args) throws Exception {
        Preferences prefs = Preferences.userNodeForPackage(MondrianTest.class);
        System.setProperty("java.naming.factory.initial", "org.osjava.sj.memory.MemoryContextFactory");
        System.setProperty("org.osjava.sj.jndi.shared", "true");
        Context ctx = new InitialContext();
        
        PlDotIni plIni = new PlDotIni();
        plIni.read(new File(System.getProperty("user.home"), "pl.ini"));
        
        Olap4jDataSource olapDataSource = new Olap4jDataSource();
        olapDataSource.setMondrianSchema(new URI(prefs.get("mondrianSchemaURI", "")));
        olapDataSource.setDataSource(plIni.getDataSource(prefs.get("mondrianDataSource", null)));

        Olap4jConnectionPanel dep = new Olap4jConnectionPanel(olapDataSource, plIni);
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
        
        SPDataSource ds = olapDataSource.getDataSource();
        ctx.bind(ds.getName(), new DataSourceAdapter(ds));
        
        Class.forName("mondrian.olap4j.MondrianOlap4jDriver");
        Connection connection =
            DriverManager.getConnection(
                "jdbc:mondrian:"
                    + "DataSource='" + ds.getName() + "';"
                    + "Catalog='" + olapDataSource.getMondrianSchema().toString() + "';"
                    );
        OlapConnection olapConnection = ((OlapWrapper) connection).unwrap(OlapConnection.class);
        
        new MondrianTest(olapConnection);
        
    }
}
