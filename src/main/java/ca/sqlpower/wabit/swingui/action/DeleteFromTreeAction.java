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

package ca.sqlpower.wabit.swingui.action;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import org.apache.log4j.Logger;

import ca.sqlpower.object.CleanupExceptions;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.WorkspaceGraphModel;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.object.WorkspaceGraphTreeModel;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.UserPrompterFactory;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.swingui.WabitIcons;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeCellRenderer;

/**
 * This action will prompt the user asking if they want to delete the given
 * object before deleting it. It will also disconnect the object from other
 * uses of it if other objects are dependent on it.
 */
public class DeleteFromTreeAction extends AbstractAction {
	
	private static final Logger logger = Logger
			.getLogger(DeleteFromTreeAction.class);

    /**
     * This border buffer is for the tree in the dialog that asks users if
     * dependent objects on the object being deleted can also be deleted.
     * This buffer is needed because the tree and the scroll bar are a bit
     * too small to not have the right scroll bar appear.
     */
    private static final int TREE_BORDER_BUFFER_WIDTH = 5;
    
    private SPObject item ;
    private final WabitWorkspace workspace;
    
    /**
     * Stores the last response by the user when prompted to confirm the 
     * deletion of a WabitObject.
     */
    private boolean deleteConfirmed;

    /**
     * This component will have all of the dialogs parented to it.
     */
    private final Component parent;

    /**
     * Used to properly display messages to the user.
     */
    private final UserPrompterFactory upf;

    /**
     * @param workspace
     *            The workspace to remove the given node from.
     * @param node
     *            The object that is being deleted from the workspace tree.
     * @param parent
     *            A parent component to attach dialogs to.
     * @param upf
     *            A user prompter to display useful messages to the user.
     */
    public DeleteFromTreeAction(WabitWorkspace workspace, SPObject node, 
            Component parent, UserPrompterFactory upf) {
        super("Delete", WabitIcons.DELETE_12);
        this.workspace = workspace;
        item = node;
        this.parent = parent;
        this.upf = upf;
    }

    public void actionPerformed(ActionEvent e) {

        final WorkspaceGraphModel graph = new WorkspaceGraphModel(workspace, item, true, true);
        
        List<SPObject> dependentList = new ArrayList<SPObject>(graph.getNodes());
        dependentList.remove(item);
        
        if (dependentList.isEmpty()) {
            int response = JOptionPane.showConfirmDialog(parent, 
                    "Do you really wish to delete " + item.getName(), "Confirm Delete", 
                    JOptionPane.YES_NO_OPTION);
            if (response != JOptionPane.YES_OPTION) return;
        } else {
            
            DataEntryPanel displayPanel = new DataEntryPanel() {

                public boolean applyChanges() {
                    //do nothing
                    return true;
                }

                public void discardChanges() {
                    //do nothing
                }

                public JComponent getPanel() {
                    JPanel panel = new JPanel(new BorderLayout());
                    final JLabel questionLabel = new JLabel("Do you really wish to delete " 
                        + item.getName() 
                        + " and its dependent objects?");
                    panel.add(questionLabel, BorderLayout.NORTH);
                    final JTree dependencyTree = new JTree(new WorkspaceGraphTreeModel(graph));
                    for (int i = 0; i < dependencyTree.getRowCount(); i++) {
                        dependencyTree.expandRow(i);
                    }
                    dependencyTree.setCellRenderer(new WorkspaceTreeCellRenderer());
                    JScrollPane scrollPane = new JScrollPane(dependencyTree);
                    scrollPane.setPreferredSize(new Dimension(questionLabel.getWidth(), 
                            (int) Math.min((dependencyTree.getPreferredSize().getHeight()
                                + scrollPane.getHorizontalScrollBar().getPreferredSize().getHeight()
                                + TREE_BORDER_BUFFER_WIDTH), 
                                (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2))));
                    
                    panel.add(scrollPane, 
                            BorderLayout.CENTER);
                    return panel;
                }

                public boolean hasUnsavedChanges() {
                    return false;
                }
                
            };
            
            deleteConfirmed = false;
            JDialog depDialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
                    displayPanel, parent, "Confirm Delete", "OK",
                   new Callable<Boolean>() {
                        public Boolean call() throws Exception {
                            deleteConfirmed = true;
                            return true;
                        }
                    },
                    new Callable<Boolean>() {
                       public Boolean call() throws Exception {
                           return true;
                       }
                    }
                    );
            depDialog.setModal(true);
            depDialog.setVisible(true);
            
            if (!deleteConfirmed) return;
        }
        
        SPObject startNode = graph.getGraphStartNode();
        
        try {
            boolean nodeRemoved = removeNode(startNode, graph);
            if (!nodeRemoved) {
                JOptionPane.showMessageDialog(parent, "The object " + 
                        item.getName() + " or one of its dependencies was not removed " +
                                "correctly.", 
                                "Unsuccessful Remove", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            SPSUtils.showExceptionDialogNoReport(parent, item.getName() + 
                    " was not found in its parent " + item.getParent().getName(), ex);
        } catch (ObjectDependentException ex) {
            SPSUtils.showExceptionDialogNoReport(parent, item.getName() + 
                    " still has objects dependent on it.", ex);
        }
        
    }

    /**
     * This recursive method will remove the given node and all nodes that
     * are reachable from it from their parents based on the graph given.
     * <p>
     * package private for testing.
     * 
     * @param nodeToRemove
     *            The node to remove from its parents. The nodes reachable
     *            from this node based on the given graph will also be
     *            removed from their parents.
     * @param graph
     *            The graph that will decide what objects are adjacent to
     *            this node. As this method is being used for removing a
     *            {@link WabitObject} and its dependencies from the tree
     *            this graph should contain the {@link WabitObject} being
     *            removed by the action and all of the {@link WabitObject}s
     *            depending on this object and the dependent object's
     *            dependencies.
     * @return true if the node and its descendants were successful. False
     *         otherwise.
     * @throws ObjectDependentException
     *             Thrown if the node that was being removed is still
     *             dependent on other objects existing. This suggests that
     *             there is a problem in the graph given that should connect
     *             all of the objects to their dependencies.
     * @throws IllegalArgumentException
     *             Thrown if a node being removed is not a child of its
     *             parent. This suggests there is something wrong with the
     *             parent/child relationships in the workspace.
     */
    boolean removeNode(SPObject nodeToRemove, WorkspaceGraphModel graph) throws IllegalArgumentException, ObjectDependentException  {
        boolean successfullyRemoved = true;
        for (SPObject dependent : graph.getAdjacentNodes(nodeToRemove)) {
            
            //Check if the dependency exists to prevent infinite recursion if there is
            //a cycle in the graph.
            if (!dependent.getDependencies().contains(nodeToRemove)) continue;
            
            dependent.removeDependency(nodeToRemove);
            successfullyRemoved = successfullyRemoved && removeNode(dependent, graph);
            if (logger.isDebugEnabled() && !successfullyRemoved) {
            	logger.debug("Could not remove " + dependent.getName());
            }
        }
        CleanupExceptions cleanupObject = SQLPowerUtils.cleanupSPObject(nodeToRemove);
        SQLPowerUtils.displayCleanupErrors(cleanupObject, upf);
        if (nodeToRemove.getParent() != null) {
            successfullyRemoved = successfullyRemoved && 
                nodeToRemove.getParent().removeChild(nodeToRemove);
        }
        if (logger.isDebugEnabled() && !successfullyRemoved) {
        	logger.debug("Failed to remove " + nodeToRemove.getName());
        }
        return successfullyRemoved;
    }

}
