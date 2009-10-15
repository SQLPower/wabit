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

package ca.sqlpower.wabit.dao.session;

import java.beans.PropertyChangeEvent;

import ca.sqlpower.query.Item;
import ca.sqlpower.query.QueryImpl;
import ca.sqlpower.query.TableContainer;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitChildEvent;
import ca.sqlpower.wabit.WabitConstantsContainer;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitItem;
import ca.sqlpower.wabit.WabitJoin;
import ca.sqlpower.wabit.WabitListener;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitTableContainer;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.dao.WabitPersistenceException;
import ca.sqlpower.wabit.dao.WabitPersister;
import ca.sqlpower.wabit.dao.WabitSessionPersister;
import ca.sqlpower.wabit.dao.WabitPersister.DataType;
import ca.sqlpower.wabit.enterprise.client.GroupMember;
import ca.sqlpower.wabit.enterprise.client.ReportTask;
import ca.sqlpower.wabit.enterprise.client.User;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.WabitOlapAxis;
import ca.sqlpower.wabit.olap.WabitOlapDimension;
import ca.sqlpower.wabit.olap.WabitOlapSelection;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.ReportContentRenderer;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.WabitObjectReportRenderer;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.report.chart.ChartColumn;
import ca.sqlpower.wabit.rs.ResultSetProducer;

/**
 * An implementation of {@link WabitListener} used exclusively for listening to
 * a {@link WabitWorkspace} and its children. When an event is fired from an
 * object this listener will convert the event into persist calls. The persist
 * calls will be made on the target persister.
 */
public class WorkspacePersisterListener implements WabitListener {

	/**
	 * This will connect a new instance of this listener to the workspace and
	 * all of its descendants. When the children of a workspace change the
	 * listener will be added to or removed from the children. When the session
	 * is being disposed of the listener will be removed from the workspace
	 * tree.
	 * 
	 * @param session
	 *            The session to listen to for lifecycle changes and its
	 *            workspace will be listened to by a new persister listener.
	 * @param targetPersister
	 *            This persister will have persist methods called on it when
	 *            events occur in the workspace in the given session.
	 */
	public static WorkspacePersisterListener attachListener(final WabitSession session, WabitPersister targetPersister, WabitSessionPersister eventSource) {
		final WorkspacePersisterListener listener = 
			new WorkspacePersisterListener(session, targetPersister, eventSource);
		WabitUtils.listenToHierarchy(session.getWorkspace(), listener);
		
		session.addSessionLifecycleListener(new SessionLifecycleListener<WabitSession>() {
			
			public void sessionClosing(SessionLifecycleEvent<WabitSession> e) {
				WabitUtils.unlistenToHierarchy(session.getWorkspace(), listener);
			}
		});
		return listener;
	}

	/**
	 * This is the persister to call the appropriate persist methods on when an
	 * event occurs signaling a change to the model.
	 */
	private final WabitPersister target;

	/**
	 * Converts any object into a simple type and converts any simple type back.
	 */
	private final SessionPersisterSuperConverter converter;

	private final WabitSessionPersister eventSource;

	/**
	 * This listener should be added through the static method for attaching a
	 * listener to a session.
	 * <p>
	 * A new listener should only be created in testing. To properly add a
	 * listener to a session see
	 * {@link #attachListener(WabitSession, WabitPersister)}.
	 * 
	 * @param session
	 *            The session whose workspace will be listened to.
	 * @param targetPersister
	 *            The persister that will have the events be forwarded to as
	 *            persist calls.
	 */
	public WorkspacePersisterListener(WabitSession session,
			WabitPersister targetPersister) {
		this(session, targetPersister, null);
	}

	/**
	 * This listener should be added through the static method for attaching a
	 * listener to a session.
	 * <p>
	 * A new listener should only be created in testing. To properly add a
	 * listener to a session see
	 * {@link #attachListener(WabitSession, WabitPersister)}.
	 * 
	 * @param session
	 *            The session whose workspace will be listened to.
	 * @param targetPersister
	 *            The persister that will have the events be forwarded to as
	 *            persist calls.
	 * @param eventSource
	 *            A {@link WabitPersister} that this listener will consult in
	 *            order to perform 'echo-cancellation' of events.
	 */
	public WorkspacePersisterListener(WabitSession session,
			WabitPersister targetPersister, WabitSessionPersister eventSource) {
		this.converter = new SessionPersisterSuperConverter(session, session.getWorkspace());
		this.target = targetPersister;
		this.eventSource = eventSource;
	}

	/**
	 * Returns true if the WabitSessionPersister that this listener complements
	 * is currently in the middle of an update. In that case, none of the
	 * WabitListener methods should make calls into the target persister.
	 * 
	 * @return True if forwarding an event to the target persister would
	 *         constitute an echo.
	 */
	private boolean wouldEcho() {
		return eventSource != null && eventSource.isUpdatingWabitWorkspace();
	}

	public void transactionEnded(TransactionEvent e) {
		if (wouldEcho()) return;
		try {
			target.commit();
		} catch (WabitPersistenceException e1) {
			throw new RuntimeException("Could not commit the transaction.", e1);
		}
	}

	public void transactionRollback(TransactionEvent e) {
		if (wouldEcho()) return;
		try {
			target.rollback();
		} catch (WabitPersistenceException e1) {
			throw new RuntimeException("Could not rollback the transaction.",
					e1);
		}
	}

	public void transactionStarted(TransactionEvent e) {
		if (wouldEcho()) return;
		try {
			target.begin();
		} catch (WabitPersistenceException e1) {
			throw new RuntimeException("Could not begin the transaction.", e1);
		}
	}

	public void wabitChildAdded(WabitChildEvent e) {
		e.getChild().addWabitListener(this);
		if (wouldEcho()) return;
		persistChild(e.getSource(), e.getChild(), e.getChildType(), e.getIndex());
	}
	
	/**
	 * Persists the given object and all of its descendants to the next
	 * persister. The root object and every descendant will be sent to the
	 * persister as a persist object and all of its properties will be sent as
	 * unconditional property persists.
	 * 
	 * @param root
	 *            The root of the tree of objects that will be persisted. This
	 *            object and all of its children will be persisted.
	 */
	 void persistObject(WabitObject root) throws WabitPersistenceException {
		target.begin();

		int index = 0;
		if (root.getParent() != null) {
			index = root.getParent().getChildren().indexOf(root);
		}
		persistChild(root.getParent(), root, root.getClass(), index);
		for (WabitObject child : root.getChildren()) {
			persistObject(child);
		}
		target.commit();
	}

	/**
	 * Calls {@link WabitPersister#persistObject(String, String, String, int)}
	 * for the child object and
	 * {@link WabitPersister#persistProperty(String, String, DataType, Object)}
	 * for each property on the object.
	 * 
	 * @param parent
	 *            The parent of the object being persisted as added to this
	 *            object.
	 * @param child
	 *            The child object that was added to its parent.
	 * @param childClassType
	 *            The object type of the child added.
	 * @param indexOfChild
	 *            The index of the child in the child list of the parent.
	 */
	private void persistChild(WabitObject parent, WabitObject child, 
			Class<? extends WabitObject> childClassType, int indexOfChild) {
		try {
			String parentUUID = null;
			if (parent != null) {
				parentUUID = parent.getUUID();
			}
			String className = childClassType.getSimpleName();
			String uuid = child.getUUID();

			target.persistObject(parentUUID, className, uuid, indexOfChild);
			target.persistProperty(uuid, "name", DataType.STRING, child
					.getName());

			// Persist any properties required for WabitObject constructor
			if (child instanceof CellSetRenderer) {
				CellSetRenderer csRenderer = (CellSetRenderer) child;

				// Remaining properties
				target.persistProperty(uuid, "bodyAlignment", DataType.STRING,
						csRenderer.getBodyAlignment().name());
				target.persistProperty(uuid, "bodyFont", DataType.STRING,
						converter.convertToBasicType(csRenderer.getBodyFont()));
				target.persistProperty(uuid, "bodyFormat", DataType.STRING,
						converter.convertToBasicType(csRenderer.getBodyFormat()));
				target.persistProperty(uuid, "headerFont", DataType.STRING,
						converter.convertToBasicType(csRenderer.getHeaderFont()));

			} else if (child instanceof ChartColumn) {
				ChartColumn chartColumn = (ChartColumn) child;

				// Constructor arguments
				target.persistProperty(uuid, "columnName", DataType.STRING,
						chartColumn.getColumnName());
				target.persistProperty(uuid, "dataType", DataType.STRING,
						converter.convertToBasicType(chartColumn.getDataType()));

				// Remaining properties
				target.persistProperty(uuid, "roleInChart", DataType.STRING,
						converter.convertToBasicType(chartColumn.getRoleInChart()));

				target.persistProperty(uuid, "XAxisIdentifier",
						DataType.REFERENCE, converter.convertToBasicType(
								chartColumn.getXAxisIdentifier(), DataType.REFERENCE));


			} else if (child instanceof Chart) {
				Chart chart = (Chart) child;

				// Remaining properties
				target.persistProperty(uuid, "gratuitouslyAnimated",
						DataType.BOOLEAN, chart.isGratuitouslyAnimated());
				target.persistProperty(uuid, "legendPosition", DataType.STRING,
						converter.convertToBasicType(chart.getLegendPosition()));
				
				ResultSetProducer rsProducer = chart.getQuery();
				if (rsProducer != null) {
					target.persistProperty(uuid, "query", DataType.REFERENCE,
							rsProducer.getUUID());
				} else {
					target.persistProperty(uuid, "query", DataType.REFERENCE,
							null);
				}
				
				target.persistProperty(uuid, "type", DataType.STRING,
						converter.convertToBasicType(chart.getType()));
				target.persistProperty(uuid, "XAxisLabelRotation",
						DataType.DOUBLE, chart.getXAxisLabelRotation());
				target.persistProperty(uuid, "xaxisName", DataType.STRING,
						chart.getXaxisName());
				target.persistProperty(uuid, "yaxisName", DataType.STRING,
						chart.getYaxisName());
				target.persistProperty(uuid, "backgroundColour", DataType.STRING,
						converter.convertToBasicType(chart.getBackgroundColour()));

			} else if (child instanceof ChartRenderer) {
				ChartRenderer cRenderer = (ChartRenderer) child;

				// Constructor argument
				target.persistProperty(uuid, "chart", DataType.REFERENCE,
						cRenderer.getChart().getUUID());

				// Remaining properties

			} else if (child instanceof ColumnInfo) {
				ColumnInfo columnInfo = (ColumnInfo) child;

				// Constructor argument
				target.persistProperty(uuid, ColumnInfo.COLUMN_ALIAS,
						DataType.STRING, columnInfo.getColumnAlias());

				// Remaining properties
				Item item = columnInfo.getColumnInfoItem();
				if (item != null) {
					target.persistProperty(uuid,
							ColumnInfo.COLUMN_INFO_ITEM_CHANGED,
							DataType.STRING, converter.convertToBasicType(item));
				}

				target.persistProperty(uuid, ColumnInfo.DATATYPE_CHANGED,
						DataType.STRING, converter.convertToBasicType(
								columnInfo.getDataType()));

				target.persistProperty(uuid,
						ColumnInfo.HORIZONAL_ALIGNMENT_CHANGED,
						DataType.STRING, converter.convertToBasicType(
								columnInfo.getHorizontalAlignment()));
				target.persistProperty(uuid, ColumnInfo.WIDTH_CHANGED,
						DataType.INTEGER, columnInfo.getWidth());
				target.persistProperty(uuid,
						ColumnInfo.WILL_GROUP_OR_BREAK_CHANGED,
						DataType.STRING, converter.convertToBasicType(
								columnInfo.getWillGroupOrBreak()));
				target.persistProperty(uuid, ColumnInfo.WILL_SUBTOTAL_CHANGED,
						DataType.BOOLEAN, columnInfo.getWillSubtotal());
				target.persistProperty(uuid, "format", DataType.STRING, 
						converter.convertToBasicType(columnInfo.getFormat()));

			} else if (child instanceof ContentBox) {
				ContentBox contentBox = (ContentBox) child;

				// Remaining arguments
				target.persistProperty(uuid, "contentRenderer",	DataType.REFERENCE,
						converter.convertToBasicType(contentBox.getContentRenderer(), DataType.REFERENCE));
				target.persistProperty(uuid, "font", DataType.STRING,
						converter.convertToBasicType(contentBox.getFont()));
				target.persistProperty(uuid, "height", DataType.DOUBLE,	contentBox.getHeight());
				target.persistProperty(uuid, "width", DataType.DOUBLE, contentBox.getWidth());
				target.persistProperty(uuid, "x", DataType.DOUBLE, contentBox.getX());
				target.persistProperty(uuid, "y", DataType.DOUBLE, contentBox.getY());

			} else if (child instanceof GroupMember) {
				GroupMember groupMember = (GroupMember) child;
				
				target.persistProperty(uuid, "user", DataType.REFERENCE, 
						converter.convertToBasicType(groupMember.getUser()));
				
			} else if (child instanceof Guide) {
				Guide guide = (Guide) child;

				// Constructor arguments
				target.persistProperty(uuid, "axis", DataType.STRING,
						converter.convertToBasicType(guide.getAxis()));
				target.persistProperty(uuid, "offset", DataType.DOUBLE, guide.getOffset());

				// Remaining properties

			} else if (child instanceof ImageRenderer) {
				ImageRenderer iRenderer = (ImageRenderer) child;

				// Remaining arguments
				target.persistProperty(uuid, "HAlign", DataType.STRING,
						iRenderer.getHAlign().name());
				target.persistProperty(uuid, "VAlign", DataType.STRING,
						iRenderer.getVAlign().name());
				target.persistProperty(uuid, "image", DataType.REFERENCE,
						iRenderer.getImage().getUUID());
				target.persistProperty(uuid, "preserveAspectRatioWhenResizing",
						DataType.BOOLEAN, iRenderer
								.isPreserveAspectRatioWhenResizing());
				target.persistProperty(uuid, "preservingAspectRatio",
						DataType.BOOLEAN, iRenderer.isPreservingAspectRatio());

			} else if (child instanceof Label) {
				Label label = (Label) child;

				target.persistProperty(uuid, "font", DataType.STRING,
						converter.convertToBasicType(label.getFont()));
				target.persistProperty(uuid, "horizontalAlignment",
						DataType.STRING, label.getHorizontalAlignment().name());
				target.persistProperty(uuid, "text", DataType.STRING, label
						.getText());
				target.persistProperty(uuid, "verticalAlignment",
						DataType.STRING, label.getVerticalAlignment().name());

			} else if (child instanceof Layout) {
				Layout layout = (Layout) child;

				// Constructor argument

				// Remaining parameters
				// target.persistProperty(uuid, "page", DataType.REFERENCE,
				// layout.getPage().getUUID());
				// target.persistProperty(uuid, "varContext", ...)
				target.persistProperty(uuid, Layout.PROPERTY_ZOOM,
						DataType.INTEGER, layout.getZoomLevel());

			} else if (child instanceof OlapQuery) {
				OlapQuery olapQuery = (OlapQuery) child;

				// Constructor arguments
				target.persistProperty(uuid, "queryName", DataType.STRING,
						olapQuery.getQueryName());
				target.persistProperty(uuid, "catalogName", DataType.STRING,
						olapQuery.getCatalogName());
				target.persistProperty(uuid, "schemaName", DataType.STRING,
						olapQuery.getSchemaName());
				target.persistProperty(uuid, "cubeName", DataType.STRING,
						olapQuery.getCubeName());

				// Remaining properties
				target.persistProperty(uuid, "nonEmpty", DataType.BOOLEAN,
						olapQuery.isNonEmpty());
				target.persistProperty(uuid, "olapDataSource", DataType.STRING, 
						converter.convertToBasicType(olapQuery.getOlapDataSource()));
				target.persistProperty(uuid, "currentCube", DataType.STRING,
						converter.convertToBasicType(
								olapQuery.getCurrentCube()));

			} else if (child instanceof Page) {
				Page page = (Page) child;

				// Constructor arguments
				target.persistProperty(uuid, "width", DataType.INTEGER, page
						.getWidth());
				target.persistProperty(uuid, "height", DataType.INTEGER, page
						.getHeight());
				target.persistProperty(uuid, "orientation", DataType.STRING,
						converter.convertToBasicType(page.getOrientation()));

				// Remaining properties
				target.persistProperty(uuid, "defaultFont", DataType.STRING,
						converter.convertToBasicType(page.getDefaultFont()));

			} else if (child instanceof QueryCache) {
				QueryCache query = (QueryCache) child;
				
				target.persistProperty(uuid, "zoomLevel", DataType.INTEGER,
						query.getZoomLevel());
				target.persistProperty(uuid, "streaming", DataType.BOOLEAN,
						query.isStreaming());
				target.persistProperty(uuid, "streamingRowLimit",
						DataType.INTEGER, query.getStreamingRowLimit());
				target.persistProperty(uuid, QueryImpl.ROW_LIMIT,
						DataType.INTEGER, query.getRowLimit());
				target.persistProperty(uuid, QueryImpl.GROUPING_ENABLED,
						DataType.BOOLEAN, query.isGroupingEnabled());
				target.persistProperty(uuid, "promptForCrossJoins",
						DataType.BOOLEAN, query.getPromptForCrossJoins());
				target.persistProperty(uuid, "automaticallyExecuting",
						DataType.BOOLEAN, query.isAutomaticallyExecuting());
				target.persistProperty(uuid, QueryImpl.GLOBAL_WHERE_CLAUSE,
						DataType.STRING, query.getGlobalWhereClause());
				target.persistProperty(uuid, QueryImpl.USER_MODIFIED_QUERY,
						DataType.STRING, query.getUserModifiedQuery());
				target.persistProperty(uuid, "executeQueriesWithCrossJoins",
						DataType.BOOLEAN, query
								.getExecuteQueriesWithCrossJoins());
				target.persistProperty(uuid, "dataSource",
						DataType.STRING,
						converter.convertToBasicType(query.getWabitDataSource()));
				
			} else if (child instanceof ReportTask) {
				ReportTask task = (ReportTask) child;
				
				target.persistProperty(uuid, "email", DataType.STRING, task.getEmail());
				target.persistProperty(uuid, "triggerType", DataType.STRING, task.getTriggerType());
				target.persistProperty(uuid, "triggerHourParam", DataType.INTEGER, 
						converter.convertToBasicType(task.getTriggerHourParam(), DataType.INTEGER));
				target.persistProperty(uuid, "triggerMinuteParam", DataType.INTEGER, 
						converter.convertToBasicType(task.getTriggerMinuteParam(), DataType.INTEGER));
				target.persistProperty(uuid, "triggerDayOfWeekParam", DataType.INTEGER, 
						converter.convertToBasicType(task.getTriggerDayOfWeekParam(), DataType.INTEGER));
				target.persistProperty(uuid, "triggerDayOfMonthParam", DataType.INTEGER, 
						converter.convertToBasicType(task.getTriggerDayOfMonthParam(), DataType.INTEGER));
				target.persistProperty(uuid, "triggerIntervalParam", DataType.INTEGER, 
						converter.convertToBasicType(task.getTriggerIntervalParam(), DataType.INTEGER));
				target.persistProperty(uuid, "report", DataType.REFERENCE, 
						converter.convertToBasicType(task.getReport(), DataType.REFERENCE));
				
			} else if (child instanceof ResultSetRenderer) {
				ResultSetRenderer renderer = (ResultSetRenderer) child;
				
				target.persistProperty(uuid, "bodyFont", DataType.STRING, 
						converter.convertToBasicType(renderer.getBodyFont()));
				target.persistProperty(uuid, "headerFont", DataType.STRING, 
						converter.convertToBasicType(renderer.getHeaderFont()));
				target.persistProperty(uuid, "borderType", DataType.STRING, 
						converter.convertToBasicType(renderer.getBorderType()));
				target.persistProperty(uuid, "nullString", DataType.STRING, 
						renderer.getNullString());
				target.persistProperty(uuid, "printingGrandTotals", DataType.BOOLEAN, 
						renderer.isPrintingGrandTotals());
				
			} else if (child instanceof User) {
				User user = (User) child;
				target.persistProperty(uuid, "password", DataType.STRING, user.getPassword());
				
			} else if (child instanceof WabitConstantsContainer) {
				WabitConstantsContainer container = (WabitConstantsContainer) child;
				
				target.persistProperty(uuid, "delegate", DataType.STRING,
						converter.convertToBasicType(container.getDelegate()));
				
				target.persistProperty(uuid, "alias", DataType.STRING, container.getAlias());
				target.persistProperty(uuid, "position", DataType.STRING, 
						converter.convertToBasicType(container.getPosition()));
				
			} else if (child instanceof WabitImage) {
				WabitImage image = (WabitImage) child;
				
				target.persistProperty(uuid, "image", DataType.PNG_IMG, 
						converter.convertToBasicType(image.getImage(), DataType.PNG_IMG));
				
			} else if (child instanceof WabitItem) {
				WabitItem item = (WabitItem) child;
				
				target.persistProperty(uuid, "delegate", DataType.STRING, 
						converter.convertToBasicType(item.getDelegate()));
				
				target.persistProperty(uuid, "alias", DataType.STRING, item.getAlias());
				target.persistProperty(uuid, "selected", DataType.INTEGER, item.getSelected());
				target.persistProperty(uuid, "where", DataType.STRING, item.getWhere());
				target.persistProperty(uuid, "groupBy", DataType.STRING, 
						converter.convertToBasicType(item.getGroupBy()));
				target.persistProperty(uuid, "having", DataType.STRING, item.getHaving());
				target.persistProperty(uuid, "orderBy", DataType.STRING, 
						converter.convertToBasicType(item.getOrderBy()));
				target.persistProperty(uuid, "orderByOrdering", DataType.INTEGER,
						item.getOrderByOrdering());
				target.persistProperty(uuid, "columnWidth", DataType.INTEGER, item.getColumnWidth());
				

			} else if (child instanceof WabitDataSource) {
				WabitDataSource ds = (WabitDataSource) child;
				
				target.persistProperty(uuid, "SPDataSource", DataType.STRING, 
						converter.convertToBasicType(ds.getSPDataSource()));

			} else if (child instanceof WabitJoin) {
				WabitJoin sqlJoin = ((WabitJoin) child);
				
				target.persistProperty(uuid, "delegate", DataType.STRING, 
						converter.convertToBasicType(sqlJoin.getDelegate()));

				target.persistProperty(uuid, "comparator", DataType.STRING,
						sqlJoin.getComparator());
				target.persistProperty(uuid, "leftColumnOuterJoin",
						DataType.BOOLEAN, sqlJoin.isLeftColumnOuterJoin());
				target.persistProperty(uuid, "rightColumnOuterJoin",
						DataType.BOOLEAN, sqlJoin.isRightColumnOuterJoin());

			} else if (child instanceof WabitOlapAxis) {
				WabitOlapAxis wabitOlapAxis = (WabitOlapAxis) child;

				// Constructor argument
				target.persistProperty(uuid, "ordinal", DataType.STRING,
						converter.convertToBasicType(wabitOlapAxis.getOrdinal()));

				// Remaining properties
				target.persistProperty(uuid, "nonEmpty", DataType.BOOLEAN,
						wabitOlapAxis.isNonEmpty());
				target.persistProperty(uuid, "sortEvaluationLiteral",
						DataType.STRING, wabitOlapAxis
								.getSortEvaluationLiteral());
				target.persistProperty(uuid, "sortOrder", DataType.STRING,
						wabitOlapAxis.getSortOrder());

			} else if (child instanceof WabitOlapDimension) {
				// Constructor argument

			} else if (child instanceof WabitOlapSelection) {
				WabitOlapSelection wabitOlapSelection = (WabitOlapSelection) child;

				// Constructor argument
				target.persistProperty(uuid, "operator", DataType.STRING,
						wabitOlapSelection.getOperator().name());
				target.persistProperty(uuid, "uniqueMemberName",
						DataType.STRING, wabitOlapSelection
								.getUniqueMemberName());

			} else if (child instanceof WabitTableContainer) {
				WabitTableContainer wabitTableContainer = (WabitTableContainer) child;
				TableContainer tableContainer = (TableContainer) wabitTableContainer
						.getDelegate();

				// Constructor arguments
				target.persistProperty(uuid, "delegate",
						DataType.STRING, converter.convertToBasicType(
								tableContainer));

				// Remaining properties
				target.persistProperty(uuid, "alias", DataType.STRING,
						tableContainer.getAlias());
				target.persistProperty(uuid, "position", DataType.STRING,
						converter.convertToBasicType(tableContainer.getPosition()));

			} else if (child instanceof WabitWorkspace) {
				WabitWorkspace workspace = (WabitWorkspace) child;
				
				target.persistProperty(uuid, "editorPanelModel", DataType.REFERENCE,
						converter.convertToBasicType(workspace.getEditorPanelModel(),
								DataType.REFERENCE));
				
			}
			
			if (child instanceof ReportContentRenderer) {
				ReportContentRenderer rcr = (ReportContentRenderer) child;
				target.persistProperty(uuid, "backgroundColour",
						DataType.STRING,
						converter.convertToBasicType(rcr.getBackgroundColour()));
			}
			if (child instanceof WabitObjectReportRenderer) {
				WabitObjectReportRenderer renderer = (WabitObjectReportRenderer) child;
				target.persistProperty(uuid, "content", DataType.REFERENCE, 
						converter.convertToBasicType(renderer.getContent(), DataType.REFERENCE));
			}
			
		} catch (WabitPersistenceException e1) {
			throw new RuntimeException("Could not add WabitObject as a child.",
					e1);
		}
	}

	public void wabitChildRemoved(WabitChildEvent e) {
		e.getChild().removeWabitListener(this);
		if (wouldEcho()) return;
		try {
			target.removeObject(e.getSource().getUUID(), e.getChild()
							.getUUID());
		} catch (WabitPersistenceException e1) {
			throw new RuntimeException(
					"Could not remove WabitObject from its parent.", e1);
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (wouldEcho()) return;
		WabitObject source = (WabitObject) evt.getSource();
		String uuid = source.getUUID();
		String propertyName = evt.getPropertyName();
		Object oldValue = evt.getOldValue();
		Object newValue = evt.getNewValue();
		
		//XXX special case that I want to remove even though I'm implementing it
		SPDataSource ds = null;
		if (source instanceof OlapQuery && propertyName.equals("currentCube")) {
			ds = ((OlapQuery) source).getOlapDataSource();
		}

		try {
			DataType typeForClass = WabitPersister.DataType
					.getTypeByClass(newValue.getClass());
			Object oldBasicType;
			Object newBasicType; 
			if (ds == null) {
				oldBasicType = converter.convertToBasicType(oldValue, typeForClass);
				newBasicType = converter.convertToBasicType(newValue, typeForClass);
			} else {
				oldBasicType = converter.convertToBasicType(oldValue, typeForClass, ds);
				newBasicType = converter.convertToBasicType(newValue, typeForClass, ds);
			}
			
			target.persistProperty(uuid, propertyName, typeForClass, 
					oldBasicType, newBasicType);
		} catch (WabitPersistenceException e) {
			throw new RuntimeException(e);
		}
	}
}