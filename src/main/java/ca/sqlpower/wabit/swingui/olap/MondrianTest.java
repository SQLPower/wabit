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
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.prefs.Preferences;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;

import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;
import org.olap4j.query.RectangularCellSetFormatter;

import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.wabit.olap.DataSourceAdapter;
import ca.sqlpower.wabit.olap.Olap4jDataSource;

public class MondrianTest {

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
        JFrame frame = new JFrame();
        frame.setVisible(true);
        JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(dep, frame, "Proof of concept", "OK");
        d.setModal(true);
        d.setVisible(true);
        if (olapDataSource.getType() == null) {
            return;
        }
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

        JTree tree = new JTree(new Olap4jTreeModel(Collections.singletonList(olapConnection)));
        tree.setCellRenderer(new Olap4JTreeCellRenderer());
        tree.setRootVisible(false);

        final JTable table = new JTable();
        final JTextArea mdxQuery = new JTextArea();
        mdxQuery.setText(
               "with" +
               "\n member Store.[USA Total] as '[Store].[USA]', solve_order = 1" +
               "\n member Product.DrinkPct as '100 * (Product.Drink, Store.CurrentMember) / (Product.Drink, Store.USA)', solve_order = 2" +
               "\nselect" +
               "\n {[Store].[All Stores].[USA].[CA], [Store].[All Stores].[USA].[OR], [Store].[All Stores].[USA].[WA], Store.USA} ON COLUMNS," +
               "\n {" +
               "\n  hierarchize(union(union(" +
               "\n   [Product].[All Products].[Drink].Children," +
               "\n   [Product].[All Products].[Drink].[Alcoholic Beverages].Children)," +
               "\n   [Product].[All Products].[Drink].[Alcoholic Beverages].[Beer and Wine].Children" +
               "\n ))," +
               "\n [Product].[Drink], Product.DrinkPct" +
               "\n } ON ROWS" +
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
                    JOptionPane.showMessageDialog(table, "FAIL");
                    return;
                }
                
                RectangularCellSetFormatter f = new RectangularCellSetFormatter(true);
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
                f.format(cellSet, pw);
                pw.flush();
                
                table.setModel(new CellSetTableModel(cellSet));
            }
        });

        JPanel queryPanel = new JPanel(new BorderLayout());
        queryPanel.add(new JScrollPane(mdxQuery), BorderLayout.CENTER);
        queryPanel.add(executeButton, BorderLayout.SOUTH);

        JSplitPane queryAndResultsPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        queryAndResultsPanel.setTopComponent(queryPanel);
        queryAndResultsPanel.setBottomComponent(new JScrollPane(table));
        
        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(new JScrollPane(tree));
        splitPane.setRightComponent(queryAndResultsPanel);
        frame.setContentPane(splitPane);
        frame.pack();
    }
}
