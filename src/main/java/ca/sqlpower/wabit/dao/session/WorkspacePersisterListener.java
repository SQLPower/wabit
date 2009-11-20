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
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersister;
import ca.sqlpower.dao.SPPersister.DataType;
import ca.sqlpower.enterprise.client.Grant;
import ca.sqlpower.enterprise.client.GroupMember;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.QueryImpl;
import ca.sqlpower.query.TableContainer;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.dao.PersistedObjectEntry;
import ca.sqlpower.wabit.dao.PersistedPropertiesEntry;
import ca.sqlpower.wabit.dao.PersistedWabitObject;
import ca.sqlpower.wabit.dao.RemovedObjectEntry;
import ca.sqlpower.wabit.dao.WabitObjectProperty;
import ca.sqlpower.wabit.dao.WabitSessionPersister;
import ca.sqlpower.wabit.enterprise.client.ReportTask;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.WabitObjectReportRenderer;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.report.chart.ChartColumn;
import ca.sqlpower.wabit.rs.ResultSetProducer;
import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.rs.olap.WabitOlapAxis;
import ca.sqlpower.wabit.rs.olap.WabitOlapSelection;
import ca.sqlpower.wabit.rs.query.QueryCache;
import ca.sqlpower.wabit.rs.query.WabitConstantItem;
import ca.sqlpower.wabit.rs.query.WabitConstantsContainer;
import ca.sqlpower.wabit.rs.query.WabitItem;
import ca.sqlpower.wabit.rs.query.WabitJoin;
import ca.sqlpower.wabit.rs.query.WabitTableContainer;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * An implementation of {@link WabitListener} used exclusively for listening to
 * a {@link WabitWorkspace} and its children. When an event is fired from an
 * object this listener will convert the event into persist calls. The persist
 * calls will be made on the target persister.
 */
public class WorkspacePersisterListener implements SPListener {
	
	private final static Logger logger = Logger.getLogger(WorkspacePersisterListener.class);
	
	private static class PropertyToIgnore {
		
		private final String propertyName;
		private final Class<? extends WabitObject> classType;

		public PropertyToIgnore(String propertyName, Class<? extends WabitObject> classType) {
			this.propertyName = propertyName;
			this.classType = classType;
		}

		public String getPropertyName() {
			return propertyName;
		}

		public Class<? extends WabitObject> getClassType() {
			return classType;
		}
	}

	/**
	 * This list contains a description of all of the properties that fire
	 * events in Wabit but are not be persisted in the server. These properties
	 * are normally properties for use in a UI that are saved to a local file
	 * but do not make sense to be saved to the server.
	 */
	private static final List<PropertyToIgnore> ignoreList;
	
	static {
		//Creating the ignore list here
		List<PropertyToIgnore> ignored = new ArrayList<PropertyToIgnore>();
		ignored.add(new PropertyToIgnore("zoomLevel", WabitObject.class));
		ignored.add(new PropertyToIgnore("editorPanelModel", WabitWorkspace.class));
		ignored.add(new PropertyToIgnore("colBeingDragged", ResultSetRenderer.class));
		ignored.add(new PropertyToIgnore("delegate", WabitConstantItem.class));
		ignored.add(new PropertyToIgnore("modifiedOlapQuery", CellSetRenderer.class));
		ignoreList = Collections.unmodifiableList(ignored);
	}
	
	/**
	 * This will be the list we will use to rollback persisted properties
	 */
	private List<PersistedPropertiesEntry> persistedPropertiesRollbackList = new LinkedList<PersistedPropertiesEntry>();
	
	/**
	 * This will be the list we use to rollback persisted objects.
	 * It contains UUIDs of objects that were created.
	 */
	private List<PersistedObjectEntry> persistedObjectsRollbackList = new LinkedList<PersistedObjectEntry>();
	
	/**
	 * This is the list we use to rollback object removal
	 */
	private List<RemovedObjectEntry> objectsToRemoveRollbackList = new LinkedList<RemovedObjectEntry>();
	
	/**
	 * Persisted property buffer, mapping of {@link WabitObject} UUIDs to each
	 * individual persisted property
	 */
	private Multimap<String, WabitObjectProperty> persistedProperties = LinkedListMultimap.create();
	
	/**
	 * Persisted {@link WabitObject} buffer, contains all the data that was
	 * passed into the persistedObject call in the order of insertion
	 */
	private List<PersistedWabitObject> persistedObjects = new LinkedList<PersistedWabitObject>();
	
	/**
	 * {@link WabitObject} removal buffer, mapping of {@link WabitObject} UUIDs
	 * to their parents
	 */
	private List<RemovedObjectEntry> objectsToRemove = new LinkedList<RemovedObjectEntry>();
	
	private int transactionCount = 0;


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
	public static WorkspacePersisterListener attachListener(
			final WabitSession session, SPPersister targetPersister, WabitSessionPersister eventSource) {
		final WorkspacePersisterListener listener = 
			new WorkspacePersisterListener(session, targetPersister, eventSource);
		SQLPowerUtils.listenToHierarchy(session.getWorkspace(), listener);
		
		session.addSessionLifecycleListener(new SessionLifecycleListener<WabitSession>() {
			
			public void sessionClosing(SessionLifecycleEvent<WabitSession> e) {
				SQLPowerUtils.unlistenToHierarchy(session.getWorkspace(), listener);
			}
		});
		return listener;
	}

	/**
	 * This is the persister to call the appropriate persist methods on when an
	 * event occurs signaling a change to the model.
	 */
	private final SPPersister target;

	/**
	 * Converts any object into a simple type and converts any simple type back.
	 */
	private final SessionPersisterSuperConverter converter;

	private final WabitSessionPersister eventSource;

	private final WabitSession session;

	private boolean headingToWinconsin;

	/**
	 * This listener should be added through the static method for attaching a
	 * listener to a session.
	 * <p>
	 * A new listener should only be created in testing. To properly add a
	 * listener to a session see
	 * {@link #attachListener(WabitSession, SPPersister)}.
	 * 
	 * @param session
	 *            The session whose workspace will be listened to.
	 * @param targetPersister
	 *            The persister that will have the events be forwarded to as
	 *            persist calls.
	 */
	public WorkspacePersisterListener(WabitSession session,
			SPPersister targetPersister) {
		this(session, targetPersister, null);
	}

	/**
	 * This listener should be added through the static method for attaching a
	 * listener to a session.
	 * <p>
	 * A new listener should only be created in testing. To properly add a
	 * listener to a session see
	 * {@link #attachListener(WabitSession, SPPersister)}.
	 * 
	 * @param session
	 *            The session whose workspace will be listened to.
	 * @param targetPersister
	 *            The persister that will have the events be forwarded to as
	 *            persist calls.
	 * @param eventSource
	 *            A {@link SPPersister} that this listener will consult in
	 *            order to perform 'echo-cancellation' of events.
	 */
	public WorkspacePersisterListener(WabitSession session,
			SPPersister targetPersister, WabitSessionPersister eventSource) {
		this.session = session;
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
		if (this.headingToWinconsin) return true;
		return eventSource != null && eventSource.isUpdatingWabitWorkspace();
	}

	public void transactionEnded(TransactionEvent e) {
		if (wouldEcho()) return;
		try {
			logger.debug("transactionEnded " + ((e == null) ? null : e.getMessage()));
			this.commit();
		} catch (SPPersistenceException e1) {
			throw new RuntimeException(e1);
		}
	}

	public void transactionRollback(TransactionEvent e) {
		if (wouldEcho()) return;
		logger.debug("transactionRollback " + ((e == null) ? null : e.getMessage()));
		this.rollback();
	}

	public void transactionStarted(TransactionEvent e) {
		if (wouldEcho()) return;
		logger.debug("transactionStarted " + ((e == null) ? null : e.getMessage()));
		transactionCount++;
	}

	public void childAdded(SPChildEvent e) {
		SQLPowerUtils.listenToHierarchy(e.getChild(), this);
		if (wouldEcho()) return;
		logger.debug("wabitChildAdded " + e.getChildType() + " with UUID " + e.getChild().getUUID());
		persistObject(e.getChild());
	}
	
	/**
	 * Persists the given object and all of its descendants to the next
	 * persister. The root object and every descendant will be sent to the
	 * persister as a persist object and all of its properties will be sent as
	 * unconditional property persists.
	 * 
	 * @param wo
	 *            The root of the tree of objects that will be persisted. This
	 *            object and all of its children will be persisted.
	 */
	 public void persistObject(SPObject wo) {
		 
		if (wouldEcho()) return;

		this.transactionStarted(TransactionEvent.createStartTransactionEvent(this, 
				"Creating transaction started event from persistObject."));
		
		int index = 0;
		SPObject parent = wo.getParent();
		if (parent != null) {
			index = parent.getChildren().indexOf(wo) - parent.childPositionOffset(wo.getClass());
			if (index < 0) {
				index = 0;
			}
		}
		
		persistChild(parent, wo, wo.getClass(), index);
		
		for (SPObject child : wo.getChildren()) {
			persistObject(child);
		}
		
		this.transactionEnded(TransactionEvent.createEndTransactionEvent(this));
	}

	/**
	 * Calls {@link SPPersister#persistObject(String, String, String, int)}
	 * for the child object and
	 * {@link SPPersister#persistProperty(String, String, DataType, Object)}
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
	 protected void persistChild(SPObject parent, SPObject child, 
			Class<? extends SPObject> childClassType, int indexOfChild) {
		 
		if (wouldEcho()) return;
		
		this.transactionStarted(TransactionEvent.createStartTransactionEvent(this, 
				"Creating transaction started event from persistChild."));
		
		final String parentUUID;
		if (child instanceof WabitWorkspace) {
			parentUUID = null;
		} else if (parent == null) {
			this.rollback();
			throw new NullPointerException("Child is not a WabitWorkspace, " +
					"but has a null parent ID: " + child);
		} else {
			parentUUID = parent.getUUID();
		}
		
		this.persistedObjects.add(
				new PersistedWabitObject(
						parentUUID, 
						child.getClass().getSimpleName(), 
						child.getUUID(),
						indexOfChild));
		
		logger.debug("Persisting " + child.getName() + " (" + child.getClass() + ")");

		String uuid = child.getUUID();

		logger.debug("persistChild on " + childClassType + " with UUID " + uuid);
		
		// Persist any properties required for WabitObject constructor
		if (child instanceof CellSetRenderer) {
			CellSetRenderer csRenderer = (CellSetRenderer) child;

			// Remaining properties
			this.persistProperty(uuid, "bodyAlignment", DataType.STRING,
					converter.convertToBasicType(csRenderer.getBodyAlignment()));
			this.persistProperty(uuid, "bodyFont", DataType.STRING,
					converter.convertToBasicType(csRenderer.getBodyFont()));
			this.persistProperty(uuid, "bodyFormat", DataType.STRING,
					converter.convertToBasicType(csRenderer.getBodyFormat()));
			this.persistProperty(uuid, "headerFont", DataType.STRING,
					converter.convertToBasicType(csRenderer.getHeaderFont()));

		} else if (child instanceof ChartColumn) {
			ChartColumn chartColumn = (ChartColumn) child;

			// Constructor arguments
			this.persistProperty(uuid, "columnName", DataType.STRING,
					converter.convertToBasicType(chartColumn.getColumnName()));
			this.persistProperty(uuid, "dataType", DataType.STRING,
					converter.convertToBasicType(chartColumn.getDataType()));

			// Remaining properties
			this.persistProperty(uuid, "roleInChart", DataType.STRING,
					converter.convertToBasicType(chartColumn.getRoleInChart()));

			this.persistProperty(uuid, "XAxisIdentifier",
					DataType.REFERENCE, converter.convertToBasicType(
							chartColumn.getXAxisIdentifier(), DataType.REFERENCE));


		} else if (child instanceof Chart) {
			Chart chart = (Chart) child;

			// Remaining properties
			this.persistProperty(uuid, "gratuitouslyAnimated",
					DataType.BOOLEAN, converter.convertToBasicType(
							chart.isGratuitouslyAnimated()));
			this.persistProperty(uuid, "legendPosition", DataType.STRING,
					converter.convertToBasicType(chart.getLegendPosition()));
			
			ResultSetProducer rsProducer = chart.getQuery();
			this.persistProperty(uuid, "query", DataType.REFERENCE, 
					converter.convertToBasicType(rsProducer));
			this.persistProperty(uuid, "type", DataType.STRING,
					converter.convertToBasicType(chart.getType()));
			this.persistProperty(uuid, "XAxisLabelRotation",
					DataType.DOUBLE, converter.convertToBasicType(
							chart.getXAxisLabelRotation()));
			this.persistProperty(uuid, "xaxisName", DataType.STRING,
					converter.convertToBasicType(chart.getXaxisName()));
			this.persistProperty(uuid, "yaxisName", DataType.STRING,
					converter.convertToBasicType(chart.getYaxisName()));
			this.persistProperty(uuid, "backgroundColour", DataType.STRING,
					converter.convertToBasicType(chart.getBackgroundColour()));

		} else if (child instanceof ChartRenderer) {
			//The only argument to this class is handled later by the
			//report content renderer section

		} else if (child instanceof ColumnInfo) {
			ColumnInfo columnInfo = (ColumnInfo) child;

			// Constructor argument
			this.persistProperty(uuid, ColumnInfo.COLUMN_ALIAS,
					DataType.STRING, converter.convertToBasicType(
							columnInfo.getColumnAlias()));
			Item item = columnInfo.getColumnInfoItem();
			if (item != null) {
				this.persistProperty(uuid,
						ColumnInfo.COLUMN_INFO_ITEM_CHANGED,
						DataType.STRING, converter.convertToBasicType(item));
			}

			// Remaining properties
			this.persistProperty(uuid, ColumnInfo.DATATYPE_CHANGED,
					DataType.STRING, converter.convertToBasicType(
							columnInfo.getDataType()));

			this.persistProperty(uuid,
					ColumnInfo.HORIZONAL_ALIGNMENT_CHANGED,
					DataType.STRING, converter.convertToBasicType(
							columnInfo.getHorizontalAlignment()));
			this.persistProperty(uuid, ColumnInfo.WIDTH_CHANGED,
					DataType.INTEGER, columnInfo.getWidth());
			this.persistProperty(uuid,
					ColumnInfo.WILL_GROUP_OR_BREAK_CHANGED,
					DataType.STRING, converter.convertToBasicType(
							columnInfo.getWillGroupOrBreak()));
			this.persistProperty(uuid, ColumnInfo.WILL_SUBTOTAL_CHANGED,
					DataType.BOOLEAN, converter.convertToBasicType(
							columnInfo.getWillSubtotal()));
			this.persistProperty(uuid, "format", DataType.STRING, 
					converter.convertToBasicType(columnInfo.getFormat()));

		} else if (child instanceof ContentBox) {
			ContentBox contentBox = (ContentBox) child;

			// Remaining arguments
			this.persistProperty(uuid, "font", DataType.STRING,
					converter.convertToBasicType(contentBox.getFont()));
			this.persistProperty(uuid, "height", DataType.DOUBLE,	
					converter.convertToBasicType(contentBox.getHeight()));
			this.persistProperty(uuid, "width", DataType.DOUBLE, 
					converter.convertToBasicType(contentBox.getWidth()));
			this.persistProperty(uuid, "x", DataType.DOUBLE, 
					converter.convertToBasicType(contentBox.getX()));
			this.persistProperty(uuid, "y", DataType.DOUBLE, 
					converter.convertToBasicType(contentBox.getY()));

		} else if (child instanceof Grant) {
			Grant grant = (Grant) child;
			
			// Constructor arguments
			this.persistProperty(uuid, "subject", DataType.STRING, 
					converter.convertToBasicType(grant.getSubject()));
			this.persistProperty(uuid, "type", DataType.STRING, 
					converter.convertToBasicType(grant.getType()));
			this.persistProperty(uuid, "createPrivilege", DataType.BOOLEAN, 
					converter.convertToBasicType(grant.isCreatePrivilege()));
			this.persistProperty(uuid, "deletePrivilege", DataType.BOOLEAN, 
					converter.convertToBasicType(grant.isDeletePrivilege()));
			this.persistProperty(uuid, "executePrivilege", DataType.BOOLEAN, 
					converter.convertToBasicType(grant.isExecutePrivilege()));
			this.persistProperty(uuid, "grantPrivilege", DataType.BOOLEAN, 
					converter.convertToBasicType(grant.isGrantPrivilege()));
			this.persistProperty(uuid, "modifyPrivilege", DataType.BOOLEAN, 
					converter.convertToBasicType(grant.isModifyPrivilege()));
			
		} else if (child instanceof GroupMember) {
			GroupMember groupMember = (GroupMember) child;
			
			// Constructor argument
			this.persistProperty(uuid, "user", DataType.REFERENCE, 
					converter.convertToBasicType(groupMember.getUser()));
			
		} else if (child instanceof Guide) {
			Guide guide = (Guide) child;

			// Constructor arguments
			this.persistProperty(uuid, "axis", DataType.STRING,
					converter.convertToBasicType(guide.getAxis()));
			this.persistProperty(uuid, "offset", DataType.DOUBLE, 
					converter.convertToBasicType(guide.getOffset()));

			// Remaining properties

		} else if (child instanceof ImageRenderer) {
			ImageRenderer iRenderer = (ImageRenderer) child;

			// Remaining arguments
			this.persistProperty(uuid, "HAlign", DataType.STRING,
					converter.convertToBasicType(iRenderer.getHAlign()));
			this.persistProperty(uuid, "VAlign", DataType.STRING,
					converter.convertToBasicType(iRenderer.getVAlign()));
			this.persistProperty(uuid, "image", DataType.REFERENCE,
					converter.convertToBasicType(iRenderer.getImage()));
			this.persistProperty(uuid, "preserveAspectRatioWhenResizing",
					DataType.BOOLEAN, converter.convertToBasicType(
							iRenderer.isPreserveAspectRatioWhenResizing()));
			this.persistProperty(uuid, "preservingAspectRatio",
					DataType.BOOLEAN, converter.convertToBasicType(
							iRenderer.isPreservingAspectRatio()));

		} else if (child instanceof Label) {
			Label label = (Label) child;

			this.persistProperty(uuid, "font", DataType.STRING,
					converter.convertToBasicType(label.getFont()));
			this.persistProperty(uuid, "horizontalAlignment",
					DataType.STRING, converter.convertToBasicType(
							label.getHorizontalAlignment()));
			this.persistProperty(uuid, "text", DataType.STRING,
					converter.convertToBasicType(label.getText()));
			this.persistProperty(uuid, "verticalAlignment",
					DataType.STRING, converter.convertToBasicType(
							label.getVerticalAlignment()));
			this.persistProperty(uuid, "backgroundColour",
					DataType.STRING,
					converter.convertToBasicType(label.getBackgroundColour()));

		} else if (child instanceof OlapQuery) {
			OlapQuery olapQuery = (OlapQuery) child;

			// Constructor arguments
			this.persistProperty(uuid, "queryName", DataType.STRING,
					converter.convertToBasicType(olapQuery.getQueryName()));
			this.persistProperty(uuid, "catalogName", DataType.STRING,
					converter.convertToBasicType(olapQuery.getCatalogName()));
			this.persistProperty(uuid, "schemaName", DataType.STRING,
					converter.convertToBasicType(olapQuery.getSchemaName()));
			this.persistProperty(uuid, "cubeName", DataType.STRING,
					converter.convertToBasicType(olapQuery.getCubeName()));

			// Remaining properties
			this.persistProperty(uuid, "nonEmpty", DataType.BOOLEAN,
					converter.convertToBasicType(olapQuery.isNonEmpty()));
			this.persistProperty(uuid, "olapDataSource", DataType.STRING, 
					converter.convertToBasicType(olapQuery.getOlapDataSource()));
			
			if (olapQuery.getCurrentCube() != null) {
				this.persistProperty(uuid, "currentCube", DataType.STRING,
						converter.convertToBasicType(olapQuery.getCurrentCube(), 
								olapQuery.getOlapDataSource()));
			}

		} else if (child instanceof Page) {
			Page page = (Page) child;

			// Constructor arguments
			this.persistProperty(uuid, "width", DataType.INTEGER, 
					converter.convertToBasicType(page.getWidth()));
			this.persistProperty(uuid, "height", DataType.INTEGER, 
					converter.convertToBasicType(page.getHeight()));
			this.persistProperty(uuid, "orientation", DataType.STRING,
					converter.convertToBasicType(page.getOrientation()));

			// Remaining properties
			this.persistProperty(uuid, "defaultFont", DataType.STRING,
					converter.convertToBasicType(page.getDefaultFont()));

		} else if (child instanceof QueryCache) {
			QueryCache query = (QueryCache) child;
			
			// Constructor argument
			this.persistProperty(uuid, "dataSource",
					DataType.STRING,
					converter.convertToBasicType(query.getDataSource()));
			
			// Remaining properties
			
			// The zoom property is being ignored here because it does not make much
			// sense to have the query zoom change for all users working on it.
			
			this.persistProperty(uuid, "streaming", DataType.BOOLEAN,
					converter.convertToBasicType(query.isStreaming()));
			this.persistProperty(uuid, "streamingRowLimit",
					DataType.INTEGER, converter.convertToBasicType(
							query.getStreamingRowLimit()));
			this.persistProperty(uuid, QueryImpl.ROW_LIMIT,
					DataType.INTEGER, 
					converter.convertToBasicType(query.getRowLimit()));
			this.persistProperty(uuid, QueryImpl.GROUPING_ENABLED,
					DataType.BOOLEAN, 
					converter.convertToBasicType(query.isGroupingEnabled()));
			this.persistProperty(uuid, "promptForCrossJoins",
					DataType.BOOLEAN, 
					converter.convertToBasicType(query.getPromptForCrossJoins()));
			this.persistProperty(uuid, "automaticallyExecuting",
					DataType.BOOLEAN, 
					converter.convertToBasicType(query.isAutomaticallyExecuting()));
			this.persistProperty(uuid, QueryImpl.GLOBAL_WHERE_CLAUSE,
					DataType.STRING, 
					converter.convertToBasicType(query.getGlobalWhereClause()));
			this.persistProperty(uuid, QueryImpl.USER_MODIFIED_QUERY,
					DataType.STRING, 
					converter.convertToBasicType(query.getUserModifiedQuery()));
			this.persistProperty(uuid, "executeQueriesWithCrossJoins",
					DataType.BOOLEAN, 
					converter.convertToBasicType(query.getExecuteQueriesWithCrossJoins()));
			
		} else if (child instanceof ReportTask) {
			ReportTask task = (ReportTask) child;
			
			// Remaining arguments
			this.persistProperty(uuid, "email", DataType.STRING, 
					converter.convertToBasicType(task.getEmail()));
			this.persistProperty(uuid, "triggerType", DataType.STRING, 
					converter.convertToBasicType(task.getTriggerType()));
			this.persistProperty(uuid, "triggerHourParam", DataType.INTEGER, 
					converter.convertToBasicType(task.getTriggerHourParam(), DataType.INTEGER));
			this.persistProperty(uuid, "triggerMinuteParam", DataType.INTEGER, 
					converter.convertToBasicType(task.getTriggerMinuteParam(), DataType.INTEGER));
			this.persistProperty(uuid, "triggerDayOfWeekParam", DataType.INTEGER, 
					converter.convertToBasicType(task.getTriggerDayOfWeekParam(), DataType.INTEGER));
			this.persistProperty(uuid, "triggerDayOfMonthParam", DataType.INTEGER, 
					converter.convertToBasicType(task.getTriggerDayOfMonthParam(), DataType.INTEGER));
			this.persistProperty(uuid, "triggerIntervalParam", DataType.INTEGER, 
					converter.convertToBasicType(task.getTriggerIntervalParam(), DataType.INTEGER));
			this.persistProperty(uuid, "report", DataType.REFERENCE, 
					converter.convertToBasicType(task.getReport(), DataType.REFERENCE));
			
		} else if (child instanceof ResultSetRenderer) {
			ResultSetRenderer renderer = (ResultSetRenderer) child;
			
			// Remaining properties
			this.persistProperty(uuid, "bodyFont", DataType.STRING, 
					converter.convertToBasicType(renderer.getBodyFont()));
			this.persistProperty(uuid, "headerFont", DataType.STRING, 
					converter.convertToBasicType(renderer.getHeaderFont()));
			this.persistProperty(uuid, "borderType", DataType.STRING, 
					converter.convertToBasicType(renderer.getBorderType()));
			this.persistProperty(uuid, "nullString", DataType.STRING, 
					converter.convertToBasicType(renderer.getNullString()));
			this.persistProperty(uuid, "printingGrandTotals", DataType.BOOLEAN, 
					converter.convertToBasicType(renderer.isPrintingGrandTotals()));
			this.persistProperty(uuid, "backgroundColour",
					DataType.STRING,
					converter.convertToBasicType(renderer.getBackgroundColour()));
			
		} else if (child instanceof User) {
			User user = (User) child;
			
			// Constructor arguments
			this.persistProperty(uuid, "password", DataType.STRING, 
					converter.convertToBasicType(user.getPassword()));
			this.persistProperty(uuid, "email", DataType.STRING, 
					converter.convertToBasicType(user.getEmail()));
			this.persistProperty(uuid, "fullName", DataType.STRING, 
					converter.convertToBasicType(user.getFullName()));
			
		} else if (child instanceof WabitConstantsContainer) {
			WabitConstantsContainer container = (WabitConstantsContainer) child;
			
			// Constructor argument
			this.persistProperty(uuid, "delegate", DataType.STRING,
					converter.convertToBasicType(container.getDelegate()));
			
			// Remaining properties
			this.persistProperty(uuid, "alias", DataType.STRING, 
					converter.convertToBasicType(container.getAlias()));
			this.persistProperty(uuid, "position", DataType.STRING, 
					converter.convertToBasicType(container.getPosition()));
			
		} else if (child instanceof WabitImage) {
			WabitImage image = (WabitImage) child;
			
			// Remaining properties
			this.persistProperty(uuid, "image", DataType.PNG_IMG, 
					converter.convertToBasicType(image.getImage(), DataType.PNG_IMG));
			
		} else if (child instanceof WabitItem) {
			WabitItem item = (WabitItem) child;
			
			// Constructor argument
			this.persistProperty(uuid, "delegate", DataType.STRING, 
					converter.convertToBasicType(item.getDelegate()));
			
			// Remaining properties
			this.persistProperty(uuid, "alias", DataType.STRING, 
					converter.convertToBasicType(item.getAlias()));
			this.persistProperty(uuid, "selected", DataType.INTEGER, 
					converter.convertToBasicType(item.getSelected()));
			this.persistProperty(uuid, "where", DataType.STRING, 
					converter.convertToBasicType(item.getWhere()));
			this.persistProperty(uuid, "groupBy", DataType.STRING, 
					converter.convertToBasicType(item.getGroupBy()));
			this.persistProperty(uuid, "having", DataType.STRING, 
					converter.convertToBasicType(item.getHaving()));
			this.persistProperty(uuid, "orderBy", DataType.STRING, 
					converter.convertToBasicType(item.getOrderBy()));
			this.persistProperty(uuid, "orderByOrdering", DataType.INTEGER,
					converter.convertToBasicType(item.getOrderByOrdering()));
			this.persistProperty(uuid, "columnWidth", DataType.INTEGER, 
					converter.convertToBasicType(item.getColumnWidth()));
			

		} else if (child instanceof WabitDataSource) {
			WabitDataSource ds = (WabitDataSource) child;
			
			// Constructor argument
			this.persistProperty(uuid, "SPDataSource", DataType.STRING, 
					converter.convertToBasicType(ds.getSPDataSource()));

		} else if (child instanceof WabitJoin) {
			WabitJoin sqlJoin = ((WabitJoin) child);
			
			// Constructor arguments
			this.persistProperty(uuid, "query", DataType.REFERENCE, 
					converter.convertToBasicType(sqlJoin.getQuery()));
			this.persistProperty(uuid, "delegate", DataType.STRING, 
					converter.convertToBasicType(sqlJoin.getDelegate()));

			// Remaining properties
			this.persistProperty(uuid, "comparator", DataType.STRING,
					converter.convertToBasicType(sqlJoin.getComparator()));
			this.persistProperty(uuid, "leftColumnOuterJoin", DataType.BOOLEAN, 
					converter.convertToBasicType(sqlJoin.isLeftColumnOuterJoin()));
			this.persistProperty(uuid, "rightColumnOuterJoin", DataType.BOOLEAN, 
					converter.convertToBasicType(sqlJoin.isRightColumnOuterJoin()));

		} else if (child instanceof WabitOlapAxis) {
			WabitOlapAxis wabitOlapAxis = (WabitOlapAxis) child;

			// Constructor argument
			this.persistProperty(uuid, "ordinal", DataType.STRING,
					converter.convertToBasicType(wabitOlapAxis.getOrdinal()));

			// Remaining properties
			this.persistProperty(uuid, "nonEmpty", DataType.BOOLEAN,
					converter.convertToBasicType(wabitOlapAxis.isNonEmpty()));
			this.persistProperty(uuid, "sortEvaluationLiteral",
					DataType.STRING, 
					converter.convertToBasicType(wabitOlapAxis.getSortEvaluationLiteral()));
			this.persistProperty(uuid, "sortOrder", DataType.STRING,
					converter.convertToBasicType(wabitOlapAxis.getSortOrder()));

		} else if (child instanceof WabitOlapSelection) {
			WabitOlapSelection wabitOlapSelection = (WabitOlapSelection) child;

			// Constructor argument
			this.persistProperty(uuid, "operator", DataType.STRING,
					converter.convertToBasicType(wabitOlapSelection.getOperator()));
			this.persistProperty(uuid, "uniqueMemberName", DataType.STRING, 
					converter.convertToBasicType(wabitOlapSelection.getUniqueMemberName()));

		} else if (child instanceof WabitTableContainer) {
			WabitTableContainer wabitTableContainer = (WabitTableContainer) child;
			TableContainer tableContainer = (TableContainer) wabitTableContainer
					.getDelegate();

			// Constructor arguments
			this.persistProperty(uuid, "delegate",
					DataType.STRING, converter.convertToBasicType(
							tableContainer));

			// Remaining properties
			this.persistProperty(uuid, "alias", DataType.STRING,
					converter.convertToBasicType(tableContainer.getAlias()));
			this.persistProperty(uuid, "position", DataType.STRING,
					converter.convertToBasicType(tableContainer.getPosition()));

		} else if (child instanceof WabitWorkspace) {
			logger.info("Sending workspace created event");
			//no current properties
			
		}
		
		if (child instanceof WabitObjectReportRenderer) {
			WabitObjectReportRenderer renderer = (WabitObjectReportRenderer) child;
			this.persistProperty(uuid, "content", DataType.REFERENCE, 
					converter.convertToBasicType(renderer.getContent(), DataType.REFERENCE));
		}

		// Persisting the name property last because WabitObjects such as ContentBox
		// have methods that calls setName on certain events or method calls.
		// We do not want those calls to affect the name property. However, this should
		// really only be a concern for reflective tests.
		this.persistProperty(uuid, "name", DataType.STRING, child.getName());
		
		this.persistProperty(uuid, "parent", DataType.REFERENCE, 
				converter.convertToBasicType(child.getParent()));
		
		this.transactionEnded(TransactionEvent.createEndTransactionEvent(this));

	}

	public void childRemoved(SPChildEvent e) {
		logger.debug("wabitChildRemoved(" + e.getChildType() + ")");
		e.getChild().removeSPListener(this);
		if (wouldEcho()) return;
		this.transactionStarted(TransactionEvent.createStartTransactionEvent(this, 
				"Start of transaction triggered by wabitChildRemoved event"));
		this.objectsToRemove.add(
			new RemovedObjectEntry(
				e.getSource().getUUID(),
				e.getChild(),
				e.getIndex()));
		this.transactionEnded(TransactionEvent.createEndTransactionEvent(this));
	}

	public void propertyChange(PropertyChangeEvent evt) {
		
		if (wouldEcho()) return;
		
		this.transactionStarted(TransactionEvent.createStartTransactionEvent(this, 
				"Creating start transaction event from propertyChange on object " + evt.getSource().getClass().getSimpleName() + " and property name " + evt.getPropertyName()));
		
		SPObject source = (SPObject) evt.getSource();
		String uuid = source.getUUID();
		String propertyName = evt.getPropertyName();
		Object oldValue = evt.getOldValue();
		Object newValue = evt.getNewValue();
		
		PropertyDescriptor propertyDescriptor;
		try {
			propertyDescriptor= PropertyUtils.getPropertyDescriptor(source, propertyName);
		} catch (Exception ex) {
			this.rollback();
			throw new RuntimeException(ex);
		}
		
		//Not persisting non-settable properties
		if (propertyDescriptor == null 
				|| propertyDescriptor.getWriteMethod() == null) {
			this.transactionEnded(TransactionEvent.createEndTransactionEvent(this));
			return;
		}
		
		for (PropertyToIgnore ignoreProperty : ignoreList) {
			if (ignoreProperty.getPropertyName().equals(propertyName) && 
					ignoreProperty.getClassType().isAssignableFrom(source.getClass())) {
				this.transactionEnded(TransactionEvent.createEndTransactionEvent(this));
				return;
			}
		}
		
		
		//XXX special case that I want to remove even though I'm implementing it
		List<Object> additionalParams = new ArrayList<Object>();
		if (source instanceof OlapQuery && propertyName.equals("currentCube")) {
			additionalParams.add(((OlapQuery) source).getOlapDataSource());
		}

		DataType typeForClass = SessionPersisterUtils.
				getDataType(newValue == null ? Void.class : newValue.getClass());
		Object oldBasicType;
		Object newBasicType; 
		oldBasicType = converter.convertToBasicType(oldValue, additionalParams.toArray());
		newBasicType = converter.convertToBasicType(newValue, additionalParams.toArray());
		
		logger.debug("Calling persistProperty on propertyChange");
		this.persistProperty(uuid, propertyName, typeForClass, 
				oldBasicType, newBasicType);
		
		this.transactionEnded(TransactionEvent.createEndTransactionEvent(this));
	}
	
	private void persistProperty(
			String uuid, 
			String propertyName,
			DataType propertyType, 
			Object newValue)
	{
		logger.debug("persistProperty(" + uuid + ", " + propertyName + ", " + 
				propertyType.name() + ", " + newValue + ", " + newValue + ")");
		this.persistedProperties.put(
				uuid,
				new WabitObjectProperty(
					uuid,
					propertyName, 
					propertyType, 
					newValue, 
					newValue, 
					true));
	}
	
	private void persistProperty(
			String uuid, 
			String propertyName,
			DataType propertyType, 
			Object oldValue, 
			Object newValue)
	{
		logger.debug("persistProperty(" + uuid + ", " + propertyName + ", " + propertyType.name() + ", " + oldValue + ", " + newValue + ")");
		this.persistedProperties.put(
				uuid,
				new WabitObjectProperty(
					uuid,
					propertyName, 
					propertyType, 
					oldValue, 
					newValue, 
					false));
	}

	private void rollback() {
		if (this.headingToWinconsin) {
			// This happens when we pick up our own events.
			return;
		}
		if (eventSource.isHeadingToWisconsin()) {
			// This means that the SessionPersister is cleaning his stuff and
			// we need to do the same. Close all current transactions... bla bla bla.
			this.objectsToRemoveRollbackList.clear();
			this.persistedObjectsRollbackList.clear();
			this.persistedPropertiesRollbackList.clear();
			this.objectsToRemove.clear();
			this.persistedObjects.clear();
			this.persistedProperties.clear();
			this.transactionCount = 0;
			target.rollback();
			return;
		}
		this.headingToWinconsin = true;
		try {
			WabitSessionPersister.undoForSession(
				session, 
				this.persistedObjectsRollbackList, 
				this.persistedPropertiesRollbackList, 
				this.objectsToRemoveRollbackList);
		} catch (SPPersistenceException e) {
			logger.error(e);
		} finally {
			this.objectsToRemoveRollbackList.clear();
			this.persistedObjectsRollbackList.clear();
			this.persistedPropertiesRollbackList.clear();
			this.objectsToRemove.clear();
			this.persistedObjects.clear();
			this.persistedProperties.clear();
			this.transactionCount = 0;
			this.headingToWinconsin = false;
			target.rollback();
		}
	}
	
	private void commit() throws SPPersistenceException {
		logger.debug("commit(): transactionCount = " + transactionCount);
		if (transactionCount==1) {
			try {
				logger.debug("Calling commit...");
				//If nothing actually changed in the transaction do not send
				//the begin and commit to reduce server traffic.
				if (objectsToRemove.isEmpty() && persistedObjects.isEmpty() && 
						persistedProperties.isEmpty()) return;
				
				this.objectsToRemoveRollbackList.clear();
				this.persistedObjectsRollbackList.clear();
				this.persistedPropertiesRollbackList.clear();
				target.begin();
				commitRemovals();
				commitObjects();
				commitProperties();
				target.commit();
				logger.debug("...commit completed.");
			} catch (Throwable t) {
				this.rollback();
				throw new SPPersistenceException(null,t);
			} finally {
				this.objectsToRemove.clear();
				this.objectsToRemoveRollbackList.clear();
				this.persistedObjects.clear();
				this.persistedObjectsRollbackList.clear();
				this.persistedProperties.clear();
				this.persistedPropertiesRollbackList.clear();
				this.transactionCount = 0;
			}
		} else {
			transactionCount--;
		}
	}
	
	/**
	 * Commits the persisted {@link WabitObject}s
	 * 
	 * @throws SPPersistenceException
	 */
	private void commitObjects() throws SPPersistenceException {
		for (PersistedWabitObject pwo : persistedObjects) {
			target.persistObject(
				pwo.getParentUUID(), 
				pwo.getType(),
				pwo.getUUID(),
				pwo.getIndex());
			this.persistedObjectsRollbackList.add(
				new PersistedObjectEntry(
					pwo.getParentUUID(),
					pwo.getUUID()));
		}
	}
	
	private void commitProperties() throws SPPersistenceException {
		logger.debug("commitProperties()");
		for (Entry<String, WabitObjectProperty> entry : persistedProperties.entries()) {
			WabitObjectProperty wop = entry.getValue();
			String uuid = entry.getKey();
			if (wop.isUnconditional()) {
				target.persistProperty(
					uuid,
					wop.getPropertyName(),
					wop.getDataType(),
					wop.getNewValue());
			} else {
				target.persistProperty(
					uuid, 
					wop.getPropertyName(), 
					wop.getDataType(), 
					wop.getOldValue(),
					wop.getNewValue());
			}
			this.persistedPropertiesRollbackList.add(
				new PersistedPropertiesEntry(
					uuid, 
					wop.getPropertyName(),
					wop.getDataType(), 
					wop.getOldValue()));
		}
	}
	
	private void commitRemovals() throws SPPersistenceException {
		logger.debug("commitRemovals()");
		for (RemovedObjectEntry entry: this.objectsToRemove) {
			logger.debug("target.removeObject(" + entry.getParentUUID() + ", " + 
					entry.getRemovedChild().getUUID() + ")");
			target.removeObject(
				entry.getParentUUID(), 
				entry.getRemovedChild().getUUID());
			this.objectsToRemoveRollbackList.add(entry);
		}
	}
}