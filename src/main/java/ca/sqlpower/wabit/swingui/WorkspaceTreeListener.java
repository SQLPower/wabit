/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.wabit.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.enterprise.client.Group;
import ca.sqlpower.wabit.enterprise.client.ReportTask;
import ca.sqlpower.wabit.enterprise.client.User;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.swingui.action.AddDataSourceAction;
import ca.sqlpower.wabit.swingui.action.CopyImageAction;
import ca.sqlpower.wabit.swingui.action.CopyOlapDatasource;
import ca.sqlpower.wabit.swingui.action.CopyQueryAction;
import ca.sqlpower.wabit.swingui.action.CopyReportAction;
import ca.sqlpower.wabit.swingui.action.CopyReportTaskAction;
import ca.sqlpower.wabit.swingui.action.CopyTemplateAction;
import ca.sqlpower.wabit.swingui.action.DeleteFromTreeAction;
import ca.sqlpower.wabit.swingui.action.EditCellAction;
import ca.sqlpower.wabit.swingui.action.NewChartAction;
import ca.sqlpower.wabit.swingui.action.NewGroupAction;
import ca.sqlpower.wabit.swingui.action.NewImageAction;
import ca.sqlpower.wabit.swingui.action.NewOLAPQueryAction;
import ca.sqlpower.wabit.swingui.action.NewQueryAction;
import ca.sqlpower.wabit.swingui.action.NewReportAction;
import ca.sqlpower.wabit.swingui.action.NewReportTaskAction;
import ca.sqlpower.wabit.swingui.action.NewTemplateAction;
import ca.sqlpower.wabit.swingui.action.NewUserAction;
import ca.sqlpower.wabit.swingui.action.ReportFromTemplateAction;
import ca.sqlpower.wabit.swingui.action.ScheduleReportAction;
import ca.sqlpower.wabit.swingui.action.SecurityAction;
import ca.sqlpower.wabit.swingui.action.ShowEditorAction;
import ca.sqlpower.wabit.swingui.tree.FolderNode;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeCellRenderer;
import ca.sqlpower.wabit.swingui.tree.FolderNode.FolderType;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeModel.Olap4jTreeObject;

/**
 * This listener is the main listener on the workspace tree in Wabit.
 * It will listen and handle all tree events from creating new elements
 * to changing the view based on selected nodes.
 */
public class WorkspaceTreeListener extends MouseAdapter {
	
	private static final Logger logger = Logger.getLogger(WorkspaceTreeListener.class);
	
    public static final Icon DB_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/connection-db-16.png"));
    public static final Icon OLAP_DB_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/connection-olap-16.png"));
	
	private final WabitSwingSession session;
	private final WabitSwingSessionContext context;

	public WorkspaceTreeListener(WabitSwingSession session) {
		this.session = session;
		context = (WabitSwingSessionContext) session.getContext();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		maybeShowPopup(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Object lastPathComponent = getLastPathComponent(e);
		if (e.isPopupTrigger()) {
			maybeShowPopup(e);
		}
		if (lastPathComponent != null) {
			if (e.getButton() == MouseEvent.BUTTON1  && e.getClickCount() == 2) {
				if (lastPathComponent instanceof WabitObject) {
					session.getWorkspace().setEditorPanelModel((WabitObject) lastPathComponent);
				} else if (lastPathComponent instanceof FolderNode) {
					JTree tree = session.getTree();
					if (tree.isExpanded(tree.getSelectionRows()[0])) {
						tree.collapsePath(tree.getSelectionPath());
					} else {
						tree.expandPath(tree.getSelectionPath());
					}
				}
			}
		}
	}

	private Object getLastPathComponent(MouseEvent e) {
		JTree t = (JTree) e.getSource();
		int row = t.getRowForLocation(e.getX(), e.getY());
		TreePath tp = t.getPathForRow(row);
		Object lastPathComponent = null;
		if (tp != null) {
			lastPathComponent = tp.getLastPathComponent();
			logger.debug("Clicked on " + lastPathComponent.getClass());
		}
		return lastPathComponent;
	}
	
	/**
	 * This will Display a List of options once you right click on the WorkspaceTree.
	 */
	private void maybeShowPopup(MouseEvent e) {
		if (!e.isPopupTrigger()) {
			return;
		}
		
		JPopupMenu menu = new JPopupMenu();
		final Object lastPathComponent = getLastPathComponent(e);

		JMenuItem newQuery = new JMenuItem(new NewQueryAction(session));
		newQuery.setIcon(WorkspaceTreeCellRenderer.QUERY_ICON);
		
		JMenuItem newOlapQuery = new JMenuItem(new NewOLAPQueryAction(session));
		newOlapQuery.setIcon(WorkspaceTreeCellRenderer.OLAP_QUERY_ICON);
		
		JMenuItem newChart = new JMenuItem(new NewChartAction(session));
		newChart.setIcon(WorkspaceTreeCellRenderer.CHART_ICON);
		
		JMenuItem newImage = new JMenuItem(new NewImageAction(session));
		
		JMenuItem newReport = new JMenuItem(new NewReportAction(session));
		
		JMenuItem newTemplate = new JMenuItem(new NewTemplateAction(session));
		
		JMenuItem newReportTask = new JMenuItem(new NewReportTaskAction(session));
		
		JMenuItem newUser = new JMenuItem(new NewUserAction(session));
		
		JMenuItem newGroup = new JMenuItem(new NewGroupAction(session));
		
		
		if (lastPathComponent != null) {
			JTree tree = (JTree) e.getSource();
			if (lastPathComponent instanceof FolderNode) {
				FolderNode lastFolderNode = (FolderNode) lastPathComponent;
				if (lastFolderNode.getFolderType().equals(FolderType.CONNECTIONS)) {
					menu.add(new AbstractAction("Database Connection Manager...") {

						public void actionPerformed(ActionEvent e) {
							session.getDbConnectionManager().showDialog(context.getFrame());
						}
					});

					menu.add(createDataSourcesMenu());
					securityMenu(menu,WabitDataSource.class.getSimpleName(),null);
				} else if (lastFolderNode.getFolderType().equals(FolderType.QUERIES)) {
					menu.add(newQuery);
					menu.add(newOlapQuery);
					securityMenu(menu,QueryCache.class.getSimpleName(),null);
					securityMenu(menu,OlapQuery.class.getSimpleName(),null);
				} else if (lastFolderNode.getFolderType().equals(FolderType.IMAGES)) {
					menu.add(newImage);
					securityMenu(menu,WabitImage.class.getSimpleName(),null);
				} else if (lastFolderNode.getFolderType().equals(FolderType.CHARTS)) {
					menu.add(newChart);
					securityMenu(menu,Chart.class.getSimpleName(),null);
				} else if (lastFolderNode.getFolderType().equals(FolderType.REPORTS)) {
					menu.add(newReport);
					securityMenu(menu,Report.class.getSimpleName(),null);
					securityMenu(menu,Template.class.getSimpleName(),null);
				} else if (lastFolderNode.getFolderType().equals(FolderType.TEMPLATES)) {
					menu.add(newTemplate);
					securityMenu(menu,Template.class.getSimpleName(),null);
				} else if (lastFolderNode.getFolderType().equals(FolderType.REPORTTASK)) {
					menu.add(newReportTask);
					securityMenu(menu,ReportTask.class.getSimpleName(),null);
				} else if (lastFolderNode.getFolderType().equals(FolderType.USERS)) {
					menu.add(newUser);
					securityMenu(menu,User.class.getSimpleName(),null);
				} else if (lastFolderNode.getFolderType().equals(FolderType.GROUPS)) {
					menu.add(newGroup);
					securityMenu(menu,Group.class.getSimpleName(),null);
				}
			} else {
			    if (lastPathComponent instanceof WabitObject) {
			        menu.add(new JMenuItem(new ShowEditorAction(session.getWorkspace(),
			                (WabitObject) lastPathComponent)));
			        securityMenu(menu,null,(WabitObject) lastPathComponent);
			        menu.addSeparator();
			    }
			    
				if (lastPathComponent instanceof WabitDataSource) {
					SPDataSource ds = ((WabitDataSource) lastPathComponent).getSPDataSource();
					if (ds instanceof JDBCDataSource) {
						NewQueryAction newQueryOnDS = new NewQueryAction(session, (JDBCDataSource) ds);
						JMenuItem newQueryItem = new JMenuItem(newQueryOnDS);
						newQueryItem.setIcon(WorkspaceTreeCellRenderer.QUERY_ICON);
						menu.add(newQueryItem);
						menu.addSeparator();
					}
					if (ds instanceof Olap4jDataSource) {
						NewOLAPQueryAction newOlapQueryOnDS = new NewOLAPQueryAction(session, (Olap4jDataSource) ds);
						JMenuItem newOlapQueryItem = new JMenuItem(newOlapQueryOnDS);
						newOlapQueryItem.setIcon(WorkspaceTreeCellRenderer.OLAP_QUERY_ICON);
						menu.add(newOlapQueryItem);
						menu.addSeparator();
					}
					menu.add(new AbstractAction("Database Connection Manager...") {
						
						public void actionPerformed(ActionEvent e) {
							session.getDbConnectionManager().showDialog(context.getFrame());
						}
					});
					
					menu.add(createDataSourcesMenu());
					menu.addSeparator();
					menu.add(new CopyOlapDatasource(session, (WabitDataSource) lastPathComponent));
				} else if (lastPathComponent instanceof QueryCache || lastPathComponent instanceof OlapQuery) {
					menu.add(newQuery);
					menu.add(newOlapQuery);
					
					menu.addSeparator();
					menu.add(new CopyQueryAction((WabitObject) lastPathComponent, session, session.getContext().getFrame()));
				} else if (lastPathComponent instanceof Report) {
					menu.add(newReport);
					menu.addSeparator();
					menu.add(new CopyReportAction((Report) lastPathComponent, session, session.getContext().getFrame()));
					if (this.session.isEnterpriseServerSession() &&
							this.session.getSystemWorkspace() != null) {
						menu.add(new ScheduleReportAction((Report) lastPathComponent, session));
					}
				} else if (lastPathComponent instanceof Template) {
					JMenuItem item = new JMenuItem(new ReportFromTemplateAction(session, (Template) lastPathComponent));
					item.setIcon(WabitIcons.REPORT_ICON_16);
					menu.add(item); 
					menu.add(newTemplate); 
					
					menu.addSeparator();
					menu.add(new CopyTemplateAction((Template) lastPathComponent, session, session.getContext().getFrame()));
				} else if (lastPathComponent instanceof WabitImage) {
					menu.add(newImage);
					
					menu.addSeparator();
					menu.add(new CopyImageAction((WabitImage) lastPathComponent, session, session.getContext().getFrame()));
				} else if (lastPathComponent instanceof ReportTask) {
					menu.add(newReportTask);
					menu.addSeparator();
					menu.add(new CopyReportTaskAction((ReportTask) lastPathComponent, session, session.getContext().getFrame()));
				} else if (lastPathComponent instanceof User) {
					menu.add(newUser);
					menu.addSeparator();
				} else if (lastPathComponent instanceof Group) {
					menu.add(newGroup);
					menu.addSeparator();
				} else if (lastPathComponent instanceof Chart) {
					menu.add(newChart);
					menu.addSeparator();
					//TODO Rename, Delete, Copy
				}
				
				if (lastPathComponent instanceof WabitObject &&
				        !(lastPathComponent instanceof ContentBox)) {
					menu.add(new EditCellAction(tree));
					menu.add(new DeleteFromTreeAction(session.getWorkspace(), 
					        (WabitObject) lastPathComponent, context.getFrame(), context));
				}
				
				if (lastPathComponent instanceof QueryCache) {
					menu.addSeparator();
					menu.add(new AbstractAction("Stop Running") {
						public void actionPerformed(ActionEvent e) {
							((QueryCache) lastPathComponent).cancel();
						}
					});
				}
			}
			//For some bizarre reason, you cannot select a node
			//in the JTree on right-click. So the coordinates for e.getSource()
			//are different from e.getPoint().setSelectionRow(tree.getRowForLocation(e.getX(), e.getY()));
			tree.setSelectionRow(tree.getRowForLocation(e.getX(), e.getY()));
		} else {
			menu.add(new AbstractAction("Database Connection Manager...") {

				public void actionPerformed(ActionEvent e) {
					session.getDbConnectionManager().showDialog(context.getFrame());
				}
			});

			menu.add(createDataSourcesMenu());
			menu.addSeparator();
			securityMenu(menu, null, this.session.getWorkspace());
			securityMenu(menu, WabitWorkspace.class.getSimpleName(), null);
			menu.addSeparator();
			
			menu.add(newQuery);
			menu.add(newOlapQuery);
			menu.add(newChart);
			menu.add(newImage);
			menu.add(newTemplate);
			menu.add(newReport);
			if (this.session.isEnterpriseServerSession()) {
				menu.add(newReportTask);
			}
		}
		if (!(lastPathComponent instanceof ContentBox) && 
				!(lastPathComponent instanceof SQLObject) &&
				!(lastPathComponent instanceof Olap4jTreeObject)) {
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
    private void securityMenu(JPopupMenu menu, String simpleName, WabitObject object) {
    	
    	WabitWorkspace systemWorkspace = this.session.getSystemWorkspace();
    	
    	if (!this.session.isEnterpriseServerSession() ||
    			systemWorkspace == null) {
    		return;
    	}
    	

		if (simpleName != null && object == null) {
			String label = null;
			if (simpleName.equals("WabitDataSource")) {
				label = "datasources";
			} else if (simpleName.equals("QueryCache")) {
				label = "relational queries";
			} else if (simpleName.equals("OlapQuery")) {
				label = "OLAP queries";
			} else if (simpleName.equals("WabitImage")) {
				label = "images";
			} else if (simpleName.equals("Chart")) {
				label = "charts";
			} else if (simpleName.equals("Report")) {
				label = "reports";
			} else if (simpleName.equals("Template")) {
				label = "templates";
			} else if (simpleName.equals("ReportTask")) {
				label = "scheduled reports";
			} else if (simpleName.equals("User")) {
				label = "users";
			} else if (simpleName.equals("Group")) {
				label = "groups";
			} else if (simpleName.equals("WabitWorkspace")) {
				label = "all workspaces";
			} else {
				throw new IllegalStateException(simpleName);
			}
			menu.add(
				new JMenuItem(
					new SecurityAction(
						this.session.getWorkspace(), 
						systemWorkspace, 
						null,
						simpleName,
						label)));
		} else if (simpleName == null && object != null) {
			if (object instanceof WabitDataSource ||
					object instanceof QueryCache ||
					object instanceof OlapQuery ||
					object instanceof WabitImage ||
					object instanceof Chart ||
					object instanceof Report ||
					object instanceof Template ||
					object instanceof ReportTask) {
				menu.add(new JMenuItem(
					new SecurityAction(
						this.session.getWorkspace(), 
						systemWorkspace, 
						object.getUUID(),
						null,
						object.getName())));
			} else if (object instanceof WabitWorkspace) {
				menu.add(new JMenuItem(
						new SecurityAction(
							this.session.getWorkspace(), 
							systemWorkspace, 
							object.getUUID(),
							null,
							object.getName().concat(" workspace"))));
			}
		} else {
			throw new IllegalStateException();
		}
	}

	/**
     * Creates a JMenu with an item for each data source defined in the context's
     * data source collection. When one of these items is selected, it invokes an
     * action that adds that data source to the workspace. 
     */
	public JMenu createDataSourcesMenu() {
        JMenu dbcsMenu = new JMenu("Add Data Source"); //$NON-NLS-1$
//        dbcsMenu.add(new JMenuItem(new NewDataSourceAction(this)));
//        dbcsMenu.addSeparator();

        for (SPDataSource dbcs : session.getContext().getDataSources().getConnections()) {
        	JMenuItem newMenuItem = new JMenuItem(new AddDataSourceAction(session.getWorkspace(), dbcs));
        	if (dbcs instanceof Olap4jDataSource) {
        		newMenuItem.setIcon(OLAP_DB_ICON);
        	} else if (dbcs instanceof JDBCDataSource) {
        		newMenuItem.setIcon(DB_ICON);
        	}
        	dbcsMenu.add(newMenuItem);
        }
        SPSUtils.breakLongMenu(context.getFrame(), dbcsMenu);
        
        return dbcsMenu;
	}

}
