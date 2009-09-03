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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.swingui.action.AddDataSourceAction;
import ca.sqlpower.wabit.swingui.action.CopyImageAction;
import ca.sqlpower.wabit.swingui.action.CopyOlapDatasource;
import ca.sqlpower.wabit.swingui.action.CopyQueryAction;
import ca.sqlpower.wabit.swingui.action.CopyReportAction;
import ca.sqlpower.wabit.swingui.action.CopyTemplateAction;
import ca.sqlpower.wabit.swingui.action.EditCellAction;
import ca.sqlpower.wabit.swingui.action.NewChartAction;
import ca.sqlpower.wabit.swingui.action.NewImageAction;
import ca.sqlpower.wabit.swingui.action.NewOLAPQueryAction;
import ca.sqlpower.wabit.swingui.action.NewQueryAction;
import ca.sqlpower.wabit.swingui.action.NewReportAction;
import ca.sqlpower.wabit.swingui.action.NewTemplateAction;
import ca.sqlpower.wabit.swingui.action.ReportFromTemplateAction;
import ca.sqlpower.wabit.swingui.action.ShowEditorAction;
import ca.sqlpower.wabit.swingui.tree.FolderNode;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeCellRenderer;
import ca.sqlpower.wabit.swingui.tree.FolderNode.FolderType;

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
	
	
	private class DeleteFromTreeAction extends AbstractAction {
		
		Object item ;
		public DeleteFromTreeAction(Object node) {
			super("Delete");
			item = node;
		}

		public void actionPerformed(ActionEvent e) {
			
		    // this whole method needs to be genericized. See bug 2149.
		    
			if(item instanceof QueryCache) {
				int response = JOptionPane.showOptionDialog(context.getFrame(), "By deleting this query, you will be deleting layout parts dependent on it\n" +
						"Do you want to proceed with deleting?", "Delete Query", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {"Ok", "Cancel"}, null);
				if(response == 0) {
					final QueryCache query = (QueryCache)item;
					session.getWorkspace().removeQuery(query, session);
					removeLayoutPartsDependentOnQuery(query);
				} else {
					return;
				}
			} else if (item instanceof WabitDataSource) {
				WabitDataSource wabitDS = (WabitDataSource) item;
				int response = JOptionPane.showOptionDialog(context.getFrame(),
							"Are you sure you want to delete the data source " + wabitDS.getName() + ", its queries,\n and all report content boxes associated with the data source?", 
							"Delete Data Source", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
		                    new Object[] {"Delete All", "Replace", "Cancel"}, null);
		            if (response == 0) {
		            	if (wabitDS.getSPDataSource() instanceof JDBCDataSource) {
			                //A temporary list is used instead of directly using session.getWorkspace().getQueries()
			            	//to prevent a ConcurrentModificationException
			            	List <QueryCache> queries = new ArrayList<QueryCache>(session.getWorkspace().getQueries());
			            	for(QueryCache query : queries) {
			                	if(item.equals(query.getWabitDataSource())) {
			                		removeLayoutPartsDependentOnQuery(query);
			                		session.getWorkspace().removeQuery(query, session);
			                	}
			                }
		            	} else if (wabitDS.getSPDataSource() instanceof Olap4jDataSource) {
			            	List <OlapQuery> olapQueries = new ArrayList<OlapQuery>(session.getWorkspace().getOlapQueries());
			            	logger.debug("Workspace has " + olapQueries.size() + " queries");
			            	for(OlapQuery query : olapQueries) {
			            		logger.debug("Currently on query '" + query.getName() + "'");
			            		if(wabitDS.getSPDataSource().equals(query.getOlapDataSource())) {
			            			logger.debug("Removing this query");
			            			removeLayoutPartsDependentOnOlapQuery(query);
			            			session.getWorkspace().removeOlapQuery(query);
			            		}
			            	}
		            	}
		            	session.getWorkspace().removeDataSource((WabitDataSource)item);
		            } else if (response == 1) {
		            	List<Class<? extends SPDataSource>> dsTypes = new ArrayList<Class<? extends SPDataSource>>();
		            	dsTypes.add(Olap4jDataSource.class);
		            	dsTypes.add(JDBCDataSource.class);
		            	UserPrompter dbPrompter = session.getContext().createDatabaseUserPrompter("Replacing " + wabitDS.getName(), dsTypes,
		            			UserPromptOptions.OK_NOTOK_CANCEL, UserPromptResponse.NOT_OK, null, session.getContext().getDataSources(), 
		            			"Select Data Source", "Skip Data Source", "Cancel");
		            	UserPromptResponse getResponseType = dbPrompter.promptUser();
		        		if (getResponseType == UserPromptResponse.OK || getResponseType == UserPromptResponse.NEW) {
		        			session.getWorkspace().removeDataSource((WabitDataSource)item);
		        			SPDataSource ds = (SPDataSource) dbPrompter.getUserSelectedResponse();
		        			session.getWorkspace().addDataSource(ds);
		        			if (ds instanceof JDBCDataSource) {
		        				List <QueryCache> queries = new ArrayList<QueryCache>(session.getWorkspace().getQueries());
				            	for(QueryCache query : queries) {
				                	if(item.equals(query.getWabitDataSource())) {
				                		removeLayoutPartsDependentOnQuery(query);
				                		int queryIndex = session.getWorkspace().getQueries().indexOf(query);
										session.getWorkspace().getQueries().get(queryIndex).setDataSource((JDBCDataSource)ds);
				                	}
				                }
		        			} else if (ds instanceof Olap4jDataSource) {
		        				List <OlapQuery> queries = new ArrayList<OlapQuery>(session.getWorkspace().getOlapQueries());
				            	for(OlapQuery query : queries) {
				                	if(wabitDS.getSPDataSource().equals(query.getOlapDataSource())) {
				                		removeLayoutPartsDependentOnOlapQuery(query);
				                		int queryIndex = session.getWorkspace().getOlapQueries().indexOf(query);
										session.getWorkspace().getOlapQueries().get(queryIndex).setOlapDataSource((Olap4jDataSource)ds);
				                	}
				                }
		        			}
			            	
		        		} else {
		        			return;
		        		}
		        	} else if (response == JOptionPane.CLOSED_OPTION || response == 2) {
		            	return;
		            } 
		    } else if (item instanceof Report) {
				session.getWorkspace().removeReport((Report)item);
		    } else if (item instanceof Template) {
		    	session.getWorkspace().removeTemplate((Template) item);
		    } else if (item instanceof OlapQuery) {
				int response = JOptionPane.showOptionDialog(context.getFrame(), "By deleting this query, you will be deleting layout parts dependent on it\n" +
						"Do you want to proceed with deleting?", "Delete Query", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {"Ok", "Cancel"}, null);
				if(response == 0) {
					final OlapQuery query = (OlapQuery)item;
					session.getWorkspace().removeOlapQuery(query);
					removeLayoutPartsDependentOnOlapQuery(query);
				} else {
					return;
				}
		    } else if (item instanceof WabitImage) {
		        int response = JOptionPane.showOptionDialog(context.getFrame(), "By deleting this image, " +
		        		"you will be deleting layout parts dependent on it\n" +
                        "Do you want to proceed with deleting?", "Delete Image", 
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, 
                        new Object[] {"Ok", "Cancel"}, null);
                if(response == 0) {
                    final WabitImage image = (WabitImage) item;
                    session.getWorkspace().removeImage(image);
                    removeLayoutPartsDependentOnImage(image);
                } else {
                    return;
                }
		    } else if (item instanceof Chart) {
		        int response = JOptionPane.showOptionDialog(context.getFrame(), 
		                "By deleting this chart, " +
                        "you will be deleting layout parts dependent on it\n" +
                        "Do you want to proceed with deleting?", "Delete Chart", 
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, 
                        new Object[] {"Ok", "Cancel"}, null);
                if(response == 0) {
                    final Chart chart = (Chart) item;
                    for (Report layout : session.getWorkspace().getReports()) {
                        List<ContentBox> cbList = 
                            new ArrayList<ContentBox>(layout.getPage().getContentBoxes());
                        for (ContentBox cb : cbList) {
                            if (cb.getContentRenderer() instanceof ChartRenderer 
                                    && ((ChartRenderer) cb.getContentRenderer()).getChart() == chart) {
                                cb.setContentRenderer(null);
                            }
                        }
                    }
                    session.getWorkspace().removeChart(chart);
                } else {
                    return;
                }
			} else {
				logger.debug("This shoudl not Happen");
				throw new IllegalStateException("Trying to delete a workspace tree object that " +
						"is unknown. Object is " + item + " and type is " + item.getClass() + ".");
			}
			
		}

        /**
         * Removes any content boxes dependent on the image passed to the method.
         */
		private void removeLayoutPartsDependentOnImage(WabitImage image) {
		    for (Report layout : session.getWorkspace().getReports()) {
                List<ContentBox> cbList = new ArrayList<ContentBox>(layout.getPage().getContentBoxes());
                for (ContentBox cb : cbList) {
                    if (cb.getContentRenderer() instanceof ImageRenderer && 
                            ((ImageRenderer) cb.getContentRenderer()).getImage() == image) {
                        cb.setContentRenderer(null);
                    }
                }
            }
        }

        /**
		 * Removes any content boxes dependent on the query passed to the method
		 * @param query
		 */
		private void removeLayoutPartsDependentOnQuery(QueryCache query) {
			List<ca.sqlpower.wabit.report.Layout> allLayouts = new ArrayList<ca.sqlpower.wabit.report.Layout>();
			allLayouts.addAll(session.getWorkspace().getReports());
			allLayouts.addAll(session.getWorkspace().getTemplates());
			for (ca.sqlpower.wabit.report.Layout layout : allLayouts) {
				List<ContentBox> cbList = new ArrayList<ContentBox>(layout.getPage().getContentBoxes());
				for(ContentBox cb : cbList) {
				    if(cb.getContentRenderer() instanceof ResultSetRenderer 
				            && ((ResultSetRenderer) cb.getContentRenderer()).getQuery() == query) {
				    	cb.setContentRenderer(null);
					}
				}
			}
		}
		
		/**
		 * Removes any content boxes dependent on the query passed to the method
		 * @param query
		 */
		private void removeLayoutPartsDependentOnOlapQuery(OlapQuery query) {
			List<ca.sqlpower.wabit.report.Layout> allLayouts = new ArrayList<ca.sqlpower.wabit.report.Layout>();
			allLayouts.addAll(session.getWorkspace().getReports());
			allLayouts.addAll(session.getWorkspace().getTemplates());
			for (ca.sqlpower.wabit.report.Layout layout : allLayouts) {
				List<ContentBox> cbList = new ArrayList<ContentBox>(layout.getPage().getContentBoxes());
				for(ContentBox cb : cbList) {
				    if(cb.getContentRenderer() instanceof CellSetRenderer &&
				            ((CellSetRenderer) cb.getContentRenderer()).getOlapQuery() == query) {
				    	cb.setContentRenderer(null);
					}
				}
			}
		}
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
				} else if (lastFolderNode.getFolderType().equals(FolderType.QUERIES)) {
					menu.add(newQuery);
					menu.add(newOlapQuery);
				} else if (lastFolderNode.getFolderType().equals(FolderType.IMAGES)) {
					menu.add(newImage);
				} else if (lastFolderNode.getFolderType().equals(FolderType.CHARTS)) {
					menu.add(newChart);					
				} else if (lastFolderNode.getFolderType().equals(FolderType.REPORTS)) {
					menu.add(newReport);
				} else if (lastFolderNode.getFolderType().equals(FolderType.TEMPLATES)) {
					menu.add(newTemplate);
				}
			} else {
			    if (lastPathComponent instanceof WabitObject) {
			        menu.add(new JMenuItem(new ShowEditorAction(session.getWorkspace(),
			                (WabitObject) lastPathComponent)));
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
					menu.add(new CopyQueryAction(session, (WabitObject) lastPathComponent));
				} else if (lastPathComponent instanceof Report) {
					menu.add(newReport);
					menu.addSeparator();
					menu.add(new CopyReportAction((Report) lastPathComponent, session));
				} else if (lastPathComponent instanceof Template) {
					JMenuItem item = new JMenuItem(new ReportFromTemplateAction(session, (Template) lastPathComponent));
					item.setIcon(WabitIcons.REPORT_ICON_16);
					menu.add(item); 
					menu.add(newTemplate); 
					
					menu.addSeparator();
					menu.add(new CopyTemplateAction((Template) lastPathComponent, session));
				} else if (lastPathComponent instanceof WabitImage) {
					menu.add(newImage);
					
					menu.addSeparator();
					menu.add(new CopyImageAction(session, (WabitImage) lastPathComponent));
				} else if (lastPathComponent instanceof Chart) {
					menu.add(newChart);
					menu.addSeparator();
					//TODO Rename, Delete, Copy
				}
				
				if (!(lastPathComponent instanceof ContentBox)) {
					menu.add(new EditCellAction(tree));
					menu.add(new DeleteFromTreeAction(lastPathComponent));
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
			
			menu.add(newQuery);
			menu.add(newOlapQuery);
			menu.add(newChart);
			menu.add(newImage);
			menu.add(newTemplate);
			menu.add(newReport);
		}
		if (!(lastPathComponent instanceof ContentBox)) {
			menu.show(e.getComponent(), e.getX(), e.getY());
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
