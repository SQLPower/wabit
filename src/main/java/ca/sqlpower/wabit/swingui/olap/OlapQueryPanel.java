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
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;

import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.swingui.WabitPanel;

public class OlapQueryPanel implements WabitPanel {
    
    /**
     * This is the view component that shows what's in the query.
     */
    private CellSetViewer cellSetViewer;
    
    /**
     * The parent component to the query panel. Message dialogs will be parented
     * to this component or the component's ancestor window.
     */
    private final JComponent parentComponent;

    /**
     * The model that stores values displayed by this panel.
     */
    private final OlapQuery query;
    
    /**
     * This is the JComponent that emcompasses the entire view.
     */
    private JSplitPane queryAndResultsPanel = null;

    private Olap4jGuiQueryPanel olap4jGuiQueryPanel;

    public OlapQueryPanel(JComponent parentComponent, OlapQuery query) {
        this.parentComponent = parentComponent;
        this.query = query;
        cellSetViewer = new CellSetViewer();
        
        buildUI();
    }

    private void buildUI() {
    	JComponent textQueryPanel;
        try {
            olap4jGuiQueryPanel = new Olap4jGuiQueryPanel(SwingUtilities.getWindowAncestor(parentComponent), cellSetViewer, query);
            textQueryPanel = createTextQueryPanel();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        JTabbedPane queryPanels = new JTabbedPane();
        JComponent viewComponent = cellSetViewer.getViewComponent();
        queryPanels.add("GUI", viewComponent);
        queryPanels.add("MDX", textQueryPanel);
        
        queryAndResultsPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(olap4jGuiQueryPanel.getPanel(), BorderLayout.CENTER);
        rightPanel.add(new JButton(new AbstractAction("Execute Query") {
            public void actionPerformed(ActionEvent e) {
                olap4jGuiQueryPanel.executeQuery();
            }
        }), BorderLayout.NORTH);
		queryAndResultsPanel.setLeftComponent(queryPanels);
        queryAndResultsPanel.setRightComponent(rightPanel);
        queryAndResultsPanel.setDividerLocation(viewComponent.getPreferredSize().width);
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
        JButton executeButton = new JButton("Execute");
        executeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CellSet cellSet;
                OlapStatement statement = null;
                OlapConnection connection = null;
                try {
                    connection = query.createOlapConnection();
                    if (connection != null) {
                        try {
                            statement = connection.createStatement();
                            cellSet = statement.executeOlapQuery(mdxQuery.getText());
                        } catch (OlapException e1) {
                            e1.printStackTrace();
                            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parentComponent), "FAIL\n" + e1.getMessage());
                            return;
                        } finally {
                            if (statement != null) {
                                try {
                                    statement.close();
                                } catch (SQLException e1) {
                                    //squish exception to not hide any exceptions thrown by the catch.
                                }
                            }
                        }

                        cellSetViewer.showCellSet(cellSet);
                    }
                } catch (Exception e1) {
                    throw new RuntimeException(e1);
                } finally {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException e1) {
                            //squish exception to not hide any exceptions thrown by the catch.
                        }
                    }
                }
            }
        });
        
        JPanel queryPanel = new JPanel(new BorderLayout());
        queryPanel.add(new JScrollPane(mdxQuery), BorderLayout.CENTER);
        queryPanel.add(executeButton, BorderLayout.SOUTH);

        return queryPanel;
    }

    public void maximizeEditor() {
        // TODO Auto-generated method stub

    }

    public boolean applyChanges() {
        //no-op
        return true;
    }

    public void discardChanges() {
        //do nothing
    }

    public JComponent getPanel() {
        if (queryAndResultsPanel == null) {
            buildUI();
        }
        return queryAndResultsPanel;
    }

    public boolean hasUnsavedChanges() {
        return false;
    }

    /**
	 * Executes the containing OlapQuery and updates the results in the
	 * CellSetViewer.
	 */
    public void executeQuery() {
    	olap4jGuiQueryPanel.executeQuery();
    }
}
