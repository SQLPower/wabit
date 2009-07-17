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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;

import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SpecificDataSourceCollection;
import ca.sqlpower.swingui.query.Messages;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.swingui.WabitPanel;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContextImpl;
import ca.sqlpower.wabit.swingui.action.CreateLayoutFromQueryAction;

public class OlapQueryPanel implements WabitPanel {
    
    private static final Logger logger = Logger.getLogger(OlapQueryPanel.class);
    
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
    

    private static final Object UNDO_MDX_EDIT = "Undo MDX Edit";

    private static final Object REDO_MDX_EDIT = "Redo MDX Edit";

	private WabitSwingSession session;
	
	/**
	 * Keeps a link to the text control
	 */
	private RSyntaxTextArea mdxTextArea;

    private UndoManager undoManager = null;

    public OlapQueryPanel(WabitSwingSession session, JComponent parentComponent, OlapQuery query) {
        this.parentComponent = parentComponent;
        this.query = query;
        this.session = session;
        cellSetViewer = new CellSetViewer(query);
        
        buildUI();
    }

    private void buildUI() {
    	JComponent textQueryPanel;
        try {
            textQueryPanel = createTextQueryPanel();
            olap4jGuiQueryPanel = new Olap4jGuiQueryPanel(new SpecificDataSourceCollection<Olap4jDataSource>(session.getWorkspace(), Olap4jDataSource.class), SwingUtilities.getWindowAncestor(parentComponent), cellSetViewer, query, this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        queryPanels = new JTabbedPane();
        olap4jGuiQueryPanel.setOlapPanelToolbar(createOlapPanelToolBar(olap4jGuiQueryPanel));
        
        JPanel guiPanel = new JPanel(new BorderLayout());
        
        JToolBar wabitBar = new JToolBar();
		wabitBar.setFloatable(false);
		JButton forumButton = new JButton(WabitSwingSessionContextImpl.FORUM_ACTION);
		forumButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		wabitBar.add(forumButton);
        
        JToolBar toolBar = new JToolBar();
		toolBar.setLayout(new BorderLayout());
		toolBar.add(olap4jGuiQueryPanel.getOlapPanelToolbar(), BorderLayout.CENTER);
		toolBar.add(wabitBar, BorderLayout.EAST);
        
        guiPanel.add(toolBar, BorderLayout.NORTH);
        JComponent viewComponent = cellSetViewer.getViewComponent();
		guiPanel.add(viewComponent, BorderLayout.CENTER);
		
        queryPanels.add("GUI", guiPanel);
        queryPanels.add("MDX", textQueryPanel);
        
        queryAndResultsPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		queryAndResultsPanel.setLeftComponent(queryPanels);
        queryAndResultsPanel.setRightComponent(olap4jGuiQueryPanel.getPanel());
        queryAndResultsPanel.setDividerLocation(queryAndResultsPanel.getWidth() - olap4jGuiQueryPanel.getPanel().getMinimumSize().width);
        queryAndResultsPanel.setResizeWeight(1);
    }
    
    private JComponent createTextQueryPanel() throws OlapException {
        
        // Set basic properties for the mdx window
        this.mdxTextArea = new RSyntaxTextArea();
        this.mdxTextArea.setText("");
        this.mdxTextArea.setLineWrap(true);
        this.mdxTextArea.restoreDefaultSyntaxHighlightingColorScheme();
        this.mdxTextArea.setSyntaxEditingStyle(RSyntaxTextArea.SQL_SYNTAX_STYLE);
        
        // Add support for undo
        this.undoManager  = new UndoManager();
        this.mdxTextArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                undoManager.addEdit(e.getEdit());
            }
        });
        this.mdxTextArea.getActionMap().put(UNDO_MDX_EDIT, undoMdxStatementAction);
        this.mdxTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), UNDO_MDX_EDIT);
        
        this.mdxTextArea.getActionMap().put(REDO_MDX_EDIT, redoMdxStatementAction);
        this.mdxTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + InputEvent.SHIFT_MASK), REDO_MDX_EDIT);
        
        
        
        
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
                            cellSet = statement.executeOlapQuery(mdxTextArea.getText());
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

                        cellSetViewer.showCellSet(query, cellSet);
                        queryPanels.setSelectedIndex(0);
                    }
                } catch (Exception e1) {
                    throw new RuntimeException(e1);
                }
            }
        });
        
        JPanel queryPanel = new JPanel(new BorderLayout());
        queryPanel.add(new JScrollPane(mdxTextArea), BorderLayout.CENTER);
        queryPanel.add(executeButton, BorderLayout.SOUTH);

        return queryPanel;
    }
    
	private JToolBar createOlapPanelToolBar(Olap4jGuiQueryPanel queryPanel) {
	    JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
	    toolBar.setFloatable(false);
	    
	    toolBar.add(queryPanel.getResetQueryButton());
	    toolBar.addSeparator();
	    
	    toolBar.add(new CreateLayoutFromQueryAction(session.getWorkspace(), query, query.getName()));
	    
	    return toolBar;
	}

    public void maximizeEditor() {
        // TODO Auto-generated method stub

    }

    public boolean applyChanges() {
        //no-op
        return true;
    }
    
    public void updateMdxText(String mdx) {
        this.mdxTextArea.setText(mdx);
        this.mdxTextArea.repaint();
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
    public void updateCellSetViewer() {
    	try {
			olap4jGuiQueryPanel.updateCellSetViewer(query.execute());
		} catch (Exception e) {
			cellSetViewer.showMessage(query, "Could not execute the query due to the following error: \n" + e.getClass().getName() + ":" + e.getMessage());
			logger.warn("Could not execute the query " + query, e);
		}
    }
    
    private Action undoMdxStatementAction = new AbstractAction(Messages.getString("SQLQuery.undo")){

        public void actionPerformed(ActionEvent arg0) {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
            
        }
    };
        
    private Action redoMdxStatementAction = new AbstractAction(Messages.getString("SQLQuery.redo")){

        public void actionPerformed(ActionEvent arg0) {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
            
        }
    };

    private JTabbedPane queryPanels;
}
