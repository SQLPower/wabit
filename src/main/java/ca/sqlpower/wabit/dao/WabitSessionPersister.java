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

package ca.sqlpower.wabit.dao;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.olap4j.query.Selection.Operator;

import ca.sqlpower.query.Item;
import ca.sqlpower.query.QueryImpl;
import ca.sqlpower.query.Container;
import ca.sqlpower.query.SQLGroupFunction;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.query.SQLObjectItem;
import ca.sqlpower.query.StringItem;
import ca.sqlpower.query.TableContainer;
import ca.sqlpower.query.QueryImpl.OrderByArgument;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.wabit.ObjectDependentException;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitChildEvent;
import ca.sqlpower.wabit.WabitColumnItem;
import ca.sqlpower.wabit.WabitConstantItem;
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
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.WabitOlapAxis;
import ca.sqlpower.wabit.olap.WabitOlapDimension;
import ca.sqlpower.wabit.olap.WabitOlapExclusion;
import ca.sqlpower.wabit.olap.WabitOlapInclusion;
import ca.sqlpower.wabit.olap.WabitOlapSelection;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.HorizontalAlignment;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.ReportContentRenderer;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.report.VerticalAlignment;
import ca.sqlpower.wabit.report.ColumnInfo.GroupAndBreak;
import ca.sqlpower.wabit.report.Guide.Axis;
import ca.sqlpower.wabit.report.Page.PageOrientation;
import ca.sqlpower.wabit.report.ResultSetRenderer.BorderStyles;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.report.chart.ChartColumn;
import ca.sqlpower.wabit.report.chart.ChartType;
import ca.sqlpower.wabit.report.chart.ColumnRole;
import ca.sqlpower.wabit.report.chart.LegendPosition;
import ca.sqlpower.wabit.rs.ResultSetProducer;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * This class represents a Data Access Object for {@link WabitSession}s.
 */
public class WabitSessionPersister implements WabitPersister {

	private static final Logger logger = Logger
			.getLogger(WabitSessionPersister.class);

	/**
	 * A {@link WabitSession} to persisted objects and properties onto.
	 */
	private WabitSession session;

	/**
	 * A {@link WabitPersister} to make persist calls on whenever there is a
	 * child added/removed or property change event
	 */
	private WabitPersister target;

	/**
	 * A count of transactions, mainly to keep track of nested transactions.
	 */
	private int transactionCount = 0;

	/**
	 * Persisted property buffer, mapping of {@link WabitObject} UUIDs to each
	 * individual persisted property
	 */
	private Multimap<String, WabitObjectProperty> persistedProperties = LinkedListMultimap
			.create();

	/**
	 * Persisted {@link WabitObject} buffer, contains all the data that was
	 * passed into the persistedObject call in the order of insertion
	 */
	private List<PersistedWabitObject> persistedObjects = new LinkedList<PersistedWabitObject>();

	/**
	 * {@link WabitObject} removal buffer, mapping of {@link WabitObject} UUIDs
	 * to their parents
	 */
	private Map<String, String> objectsToRemove = new LinkedHashMap<String, String>();

	/**
	 * A class representing an individual persisted {@link WabitObject}
	 * property.
	 */
	private class WabitObjectProperty {

		final private String propertyName;
		final private Object newValue;
		final private boolean unconditional;

		/**
		 * Constructor to persist a {@link WabitObject} property, keeping track
		 * of all the parameters of the persistProperty(...) method call. These
		 * fields will be necessary for when commit() is called.
		 * 
		 * @param propertyName
		 *            The name of the property to persist
		 * @param newValue
		 *            The property value to persist
		 * @param unconditional
		 *            Whether or not to validate if oldValue matches the actual
		 *            property value before persisting
		 */
		private WabitObjectProperty(String propertyName, Object newValue,
				boolean unconditional) {
			this.propertyName = propertyName;
			this.newValue = newValue;
			this.unconditional = unconditional;
		}

		/**
		 * Accessor for the property name field
		 * 
		 * @return The property name to persist upon
		 */
		public String getPropertyName() {
			return propertyName;
		}

		/**
		 * Accessor for the property value to persist
		 * 
		 * @return The property value to persist
		 */
		public Object getNewValue() {
			return newValue;
		}

		/**
		 * Accessor for the unconditional persist determinant
		 * 
		 * @return The unconditional field
		 */
		public boolean isUnconditional() {
			return unconditional;
		}

	}

	/**
	 * A class representing an individual persisted {@link WabitObject}.
	 */
	private class PersistedWabitObject {
		final private String parentUUID;
		final private String type;
		final private String uuid;

		/**
		 * Constructor to persist a {@link WabitObject}.
		 * 
		 * @param parentUUID
		 *            The parent UUID of the {@link WabitObject} to persist
		 * @param type
		 *            The {@link WabitObject} class name
		 * @param uuid
		 *            The UUID of the {@link WabitObject} to persist
		 */
		private PersistedWabitObject(String parentUUID, String type, String uuid) {
			this.parentUUID = parentUUID;
			this.type = type;
			this.uuid = uuid;
		}

		/**
		 * Accessor for the parent UUID field
		 * 
		 * @return The parent UUID of the object to persist
		 */
		public String getParentUUID() {
			return parentUUID;
		}

		/**
		 * Accessor for the {@link WabitObject} class name
		 * 
		 * @return The {@link WabitObject} class name
		 */
		public String getType() {
			return type;
		}

		/**
		 * Accessor for the UUID field
		 * 
		 * @return The UUID of the object to persist
		 */
		public String getUUID() {
			return uuid;
		}

	}

	/**
	 * Constructor to set the {@link WabitSession} this DAO should work under
	 * 
	 * @param session
	 *            The {@link WabitSession} this DAO should work under
	 */
	public WabitSessionPersister(WabitSession session, WabitPersister target) {
		this.session = session;
		this.target = target;

		WabitUtils.listenToHierarchy(session.getWorkspace(),
				new WabitWorkspaceListener());
	}

	/**
	 * Begins a transaction
	 */
	public void begin() {
		transactionCount++;
	}

	/**
	 * Commits the persisted {@link WabitObject}s, its properties and removals
	 */
	public void commit() throws WabitPersistenceException {
		if (transactionCount <= 0) {
			throw new WabitPersistenceException(null,
					"Commit attempted while not in a transaction");
		}

		commitObjects();
		commitProperties();
		commitRemovals();

		transactionCount--;
	}

	/**
	 * Commits the persisted {@link WabitObject}s
	 * 
	 * @throws WabitPersistenceException
	 */
	private void commitObjects() throws WabitPersistenceException {

		for (PersistedWabitObject pwo : persistedObjects) {
			String uuid = pwo.getUUID();
			String type = pwo.getType();
			WabitWorkspace workspace = session.getWorkspace();
			WabitObject wo = null;
			WabitObject parent = workspace.findByUuid(pwo.getParentUUID(),
					WabitObject.class);

			if (type.equals(CellSetRenderer.class.getSimpleName())) {
				OlapQuery olapQuery = workspace.findByUuid(
						getPropertyAndRemove(uuid, "modifiedOlapQuery")
								.toString(), OlapQuery.class);
				wo = new CellSetRenderer(olapQuery);

			} else if (type.equals(Chart.class.getSimpleName())) {
				wo = new Chart();

			} else if (type.equals(ChartColumn.class.getSimpleName())) {
				String columnName = getPropertyAndRemove(uuid, "name")
						.toString();
				ca.sqlpower.wabit.report.chart.ChartColumn.DataType dataType = ca.sqlpower.wabit.report.chart.ChartColumn.DataType
						.valueOf(getPropertyAndRemove(uuid, "dataType")
								.toString());

				wo = new ChartColumn(columnName, dataType);

			} else if (type.equals(ChartRenderer.class.getSimpleName())) {
				Chart chart = workspace.findByUuid(getPropertyAndRemove(uuid,
						"chart").toString(), Chart.class);
				wo = new ChartRenderer(chart);

			} else if (type.equals(ContentBox.class.getSimpleName())) {
				wo = new ContentBox();

			} else if (type.equals(Guide.class.getSimpleName())) {
				Axis axis = Axis.valueOf(getPropertyAndRemove(uuid, "axis")
						.toString());
				double offset = Double.valueOf(getPropertyAndRemove(uuid,
						"offset").toString());

				wo = new Guide(axis, offset);

			} else if (type.equals(ImageRenderer.class.getSimpleName())) {
				wo = new ImageRenderer();

			} else if (type.equals(Label.class.getSimpleName())) {
				wo = new Label();

			} else if (type.equals(OlapQuery.class.getSimpleName())) {
				String name = getPropertyAndRemove(uuid, "name").toString();
				String queryName = getPropertyAndRemove(uuid, "queryName")
						.toString();
				String catalogName = getPropertyAndRemove(uuid, "catalogName")
						.toString();
				String schemaName = getPropertyAndRemove(uuid, "schemaName")
						.toString();
				String cubeName = getPropertyAndRemove(uuid, "cubeName")
						.toString();
				wo = new OlapQuery(uuid, session.getContext(), name, queryName,
						catalogName, schemaName, cubeName);

			} else if (type.equals(Page.class.getSimpleName())) {
				String name = getPropertyAndRemove(uuid, "name").toString();
				int width = Integer.valueOf(getPropertyAndRemove(uuid, "width")
						.toString());
				int height = Integer.valueOf(getPropertyAndRemove(uuid,
						"height").toString());
				PageOrientation orientation = PageOrientation
						.valueOf(getPropertyAndRemove(uuid, "orientation")
								.toString());

				wo = new Page(name, width, height, orientation);

			} else if (type.equals(QueryCache.class.getSimpleName())) {
				wo = new QueryCache(session.getContext());

			} else if (type.equals(Report.class.getSimpleName())) {
				String name = getPropertyAndRemove(uuid, "name").toString();

				wo = new Report(name);

			} else if (type.equals(Template.class.getSimpleName())) {
				String name = getPropertyAndRemove(uuid, "name").toString();

				wo = new Template(name);

			} else if (type.equals(WabitColumnItem.class.getSimpleName())) {
				String name = getPropertyAndRemove(uuid, "name").toString();
				SQLObjectItem soItem = new SQLObjectItem(name, uuid);

				wo = new WabitColumnItem(soItem);

			} else if (type.equals(WabitConstantsContainer.class
					.getSimpleName())) {
				wo = ((QueryCache) parent).getWabitConstantsContainer();

			} else if (type.equals(WabitConstantItem.class.getSimpleName())) {
				String name = getPropertyAndRemove(uuid, "name").toString();
				StringItem stringItem = new StringItem(name);

				wo = new WabitConstantItem(stringItem);

			} else if (type.equals(WabitDataSource.class.getSimpleName())) {
				SPDataSource spds = session.getContext().getDataSources()
						.getDataSource(
								getPropertyAndRemove(uuid, "name").toString());

				wo = new WabitDataSource(spds);

			} else if (type.equals(WabitImage.class.getSimpleName())) {
				wo = new WabitImage();

			} else if (type.equals(WabitJoin.class.getSimpleName())) {
				Item leftItem = workspace.findByUuid(
						getPropertyAndRemove(uuid, SQLJoin.LEFT_JOIN_CHANGED)
								.toString(), WabitColumnItem.class)
						.getDelegate();
				Item rightItem = workspace.findByUuid(
						getPropertyAndRemove(uuid, SQLJoin.RIGHT_JOIN_CHANGED)
								.toString(), WabitColumnItem.class)
						.getDelegate();
				wo = new WabitJoin((QueryCache) parent, new SQLJoin(leftItem,
						rightItem));

			} else if (type.equals(WabitOlapAxis.class.getSimpleName())) {
				org.olap4j.Axis axis = org.olap4j.Axis.Factory
						.forOrdinal(Integer.valueOf(getPropertyAndRemove(uuid,
								"ordinal").toString()));

				wo = new WabitOlapAxis(axis);

			} else if (type.equals(WabitOlapDimension.class.getSimpleName())) {
				String name = getPropertyAndRemove(uuid, "name").toString();

				wo = new WabitOlapDimension(name);

			} else if (type.equals(WabitOlapExclusion.class.getSimpleName())) {
				Operator operator = Operator.valueOf(getPropertyAndRemove(uuid,
						"operator").toString());
				String uniqueMemberName = getPropertyAndRemove(uuid,
						"uniqueMemberName").toString();
				wo = new WabitOlapExclusion(operator, uniqueMemberName);

			} else if (type.equals(WabitOlapInclusion.class.getSimpleName())) {
				Operator operator = Operator.valueOf(getPropertyAndRemove(uuid,
						"operator").toString());
				String uniqueMemberName = getPropertyAndRemove(uuid,
						"uniqueMemberName").toString();
				wo = new WabitOlapInclusion(operator, uniqueMemberName);

			} else if (type.equals(WabitTableContainer.class.getSimpleName())) {
				String name = getPropertyAndRemove(uuid, "name").toString();
				String schema = getPropertyAndRemove(uuid, "schema").toString();
				String catalog = getPropertyAndRemove(uuid, "catalog")
						.toString();
				List<SQLObjectItem> items = Collections.emptyList();
				SQLDatabase db = ((QueryCache) parent).getDatabase();

				TableContainer tableContainer = new TableContainer(uuid, db,
						name, schema, catalog, items);
				wo = new WabitTableContainer(tableContainer);

			} else {
				throw new WabitPersistenceException(uuid,
						"Unknown WabitObject type: " + type);
			}

			if (wo != null) {
				wo.setUUID(uuid);
				parent.addChild(wo, parent.getChildren().size());
			}

		}

		persistedObjects.clear();
	}

	/**
	 * Retrieves a persisted property value given by the UUID of the
	 * {@link WabitObject} and its property name. The property is removed from
	 * the {@link Multimap} if found.
	 * 
	 * @param uuid
	 *            The UUID of the {@link WabitObject}
	 * @param propertyName
	 *            The persisted property name
	 * @return The persisted property value
	 */
	private Object getPropertyAndRemove(String uuid, String propertyName) {
		for (WabitObjectProperty wop : persistedProperties.get(uuid)) {
			if (wop.getPropertyName().equals(propertyName)) {
				Object value = wop.getNewValue();

				persistedProperties.remove(uuid, wop);

				return value;
			}
		}

		return null;
	}

	/**
	 * Checks to see if a {@link WabitObject} with a certain UUID exists
	 * 
	 * @param uuid
	 *            The UUID to search for
	 * @return Whether or not the {@link WabitObject} exists
	 */
	private boolean exists(String uuid) {
		if (!objectsToRemove.containsKey(uuid)) {
			for (PersistedWabitObject pwo : persistedObjects) {
				if (uuid.equals(pwo.getUUID())) {
					return true;
				}
			}
			if (session.getWorkspace().findByUuid(uuid, WabitObject.class) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Commits the persisted {@link WabitObject} property values
	 * 
	 * @throws WabitPersistenceException
	 */
	private void commitProperties() throws WabitPersistenceException {
		WabitWorkspace workspace = session.getWorkspace();
		WabitObject wo;
		String propertyName;
		Object newValue;

		for (String uuid : persistedProperties.keySet()) {
			wo = workspace.findByUuid(uuid, WabitObject.class);

			for (WabitObjectProperty wop : persistedProperties.get(uuid)) {
				propertyName = wop.getPropertyName();
				newValue = wop.getNewValue();

				if (isCommonProperty(propertyName)) {
					commitCommonProperty(wo, propertyName, newValue);
				} else if (wo instanceof CellSetRenderer) {
					commitCellSetRendererProperty((CellSetRenderer) wo,
							propertyName, newValue);
				} else if (wo instanceof Chart) {
					commitChartProperty((Chart) wo, propertyName, newValue);
				} else if (wo instanceof ChartColumn) {
					commitChartColumnProperty((ChartColumn) wo, propertyName,
							newValue);
				} else if (wo instanceof ChartRenderer) {
					commitChartRendererProperty((ChartRenderer) wo,
							propertyName, newValue);
				} else if (wo instanceof ColumnInfo) {
					commitColumnInfoProperty((ColumnInfo) wo, propertyName,
							newValue);
				} else if (wo instanceof ContentBox) {
					commitContentBoxProperty((ContentBox) wo, propertyName,
							newValue);
				} else if (wo instanceof Guide) {
					commitGuideProperty((Guide) wo, propertyName, newValue);
				} else if (wo instanceof ImageRenderer) {
					commitImageRendererProperty((ImageRenderer) wo,
							propertyName, newValue);
				} else if (wo instanceof Label) {
					commitLabelProperty((Label) wo, propertyName, newValue);
				} else if (wo instanceof Layout) {
					commitLayoutProperty((Layout) wo, propertyName, newValue);
				} else if (wo instanceof OlapQuery) {
					commitOlapQueryProperty((OlapQuery) wo, propertyName,
							newValue);
				} else if (wo instanceof Page) {
					commitPageProperty((Page) wo, propertyName, newValue);
				} else if (wo instanceof QueryCache) {
					commitQueryCacheProperty((QueryCache) wo, propertyName,
							newValue);
				} else if (wo instanceof ResultSetRenderer) {
					commitResultSetRendererProperty((ResultSetRenderer) wo,
							propertyName, newValue);
				} else if (wo instanceof WabitConstantsContainer) {
					commitWabitConstantsContainerProperty(
							(WabitConstantsContainer) wo, propertyName,
							newValue);
				} else if (wo instanceof WabitDataSource) {
					commitWabitDataSourceProperty((WabitDataSource) wo,
							propertyName, newValue);
				} else if (wo instanceof WabitOlapAxis) {
					commitWabitOlapAxisProperty((WabitOlapAxis) wo,
							propertyName, newValue);
				} else if (wo instanceof WabitOlapDimension) {
					commitWabitOlapDimensionProperty((WabitOlapDimension) wo,
							propertyName, newValue);
				} else if (wo instanceof WabitOlapSelection) {
					commitWabitOlapSelectionProperty((WabitOlapSelection) wo,
							propertyName, newValue);
				} else if (wo instanceof WabitImage) {
					commitWabitImageProperty((WabitImage) wo, propertyName,
							newValue);
				} else if (wo instanceof WabitItem) {
					commitWabitItemProperty((WabitItem) wo, propertyName,
							newValue);
				} else if (wo instanceof WabitJoin) {
					commitWabitJoinProperty((WabitJoin) wo, propertyName,
							newValue);
				} else if (wo instanceof WabitTableContainer) {
					commitWabitTableContainerProperty((WabitTableContainer) wo,
							propertyName, newValue);
				} else if (wo instanceof WabitWorkspace) {
					commitWabitWorkspaceProperty((WabitWorkspace) wo,
							propertyName, newValue);
				} else {
					throw new WabitPersistenceException(uuid,
							"Invalid WabitObject");
				}

			}

		}

		persistedProperties.clear();

	}

	/**
	 * Commits the removal of persisted {@link WabitObject}s
	 * 
	 * @throws WabitPersistenceException
	 */
	private void commitRemovals() throws WabitPersistenceException {
		WabitWorkspace workspace = session.getWorkspace();

		for (String uuid : objectsToRemove.keySet()) {
			WabitObject wo = workspace.findByUuid(uuid, WabitObject.class);
			WabitObject parent = workspace.findByUuid(
					objectsToRemove.get(uuid), WabitObject.class);

			try {
				parent.removeChild(wo);
			} catch (IllegalArgumentException e) {
				throw new WabitPersistenceException(uuid, e);
			} catch (ObjectDependentException e) {
				throw new WabitPersistenceException(uuid, e);
			}
		}

		objectsToRemove.clear();
	}

	/**
	 * Persists a {@link WabitObject} given by its UUID, class name, and parent
	 * UUID
	 * 
	 * @param parentUUID
	 *            The parent UUID of the {@link WabitObject} to persist
	 * @param type
	 *            The class name of the {@link WabitObject} to persist
	 * @param uuid
	 *            The UUID of the {@link WabitObject} to persist
	 * @param index
	 *            The index of the {@link WabitObject} within its parents' list
	 *            of children
	 * 
	 * @throws WabitPersistenceException
	 */
	public void persistObject(String parentUUID, String type, String uuid,
			int index) throws WabitPersistenceException {

		if (exists(uuid)) {
			throw new WabitPersistenceException(uuid,
					"A WabitObject with UUID " + uuid + " already exists.");
		}

		PersistedWabitObject pwo = new PersistedWabitObject(parentUUID, type,
				uuid);

		persistedObjects.add(pwo);

		if (transactionCount == 0) {
			commitObjects();
		}

	}

	/**
	 * Persists a {@link WabitObject} property conditionally given by its object
	 * UUID, property name, property type, expected old value, and new value
	 * 
	 * @param uuid
	 *            The UUID of the {@link WabitObject} to persist the property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param propertyType
	 *            The property type
	 * @param oldValue
	 *            The expected old property value
	 * @param newValue
	 *            The new property value to persist
	 * @throws WabitPersistenceException
	 */
	public void persistProperty(String uuid, String propertyName,
			DataType propertyType, Object oldValue, Object newValue)
			throws WabitPersistenceException {
		persistPropertyHelper(uuid, propertyName, propertyType, oldValue,
				newValue, false);
	}

	/**
	 * Persists a {@link WabitObject} property unconditionally given by its
	 * object UUID, property name, property type, and new value
	 * 
	 * @param uuid
	 *            The UUID of the {@link WabitObject} to persist the property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param propertyType
	 *            The property type
	 * @param newValue
	 *            The new property value to persist
	 * @throws WabitPersistenceException
	 */
	public void persistProperty(String uuid, String propertyName,
			DataType propertyType, Object newValue)
			throws WabitPersistenceException {
		persistPropertyHelper(uuid, propertyName, propertyType, null, newValue,
				true);
	}

	/**
	 * Helper to persist a {@link WabitObject} property given by its object
	 * UUID, property name, property type, expected old value, and new value.
	 * This can be done either conditionally or unconditionally based on which
	 * persistProperty method called this one.
	 * 
	 * @param uuid
	 *            The UUID of the {@link WabitObject} to persist the property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param propertyType
	 *            The property type
	 * @param oldValue
	 *            The expected old property value
	 * @param newValue
	 *            The new property value to persist
	 * @throws WabitPersistenceException
	 */
	private void persistPropertyHelper(String uuid, String propertyName,
			DataType propertyType, Object oldValue, Object newValue,
			boolean unconditional) throws WabitPersistenceException {
		if (!exists(uuid)) {
			throw new WabitPersistenceException(uuid, "WabitObject with UUID "
					+ uuid + " does not exist.");
		}

		Object lastPropertyValueFound = null;

		for (WabitObjectProperty wop : persistedProperties.get(uuid)) {
			if (propertyName.equals(wop.getPropertyName())) {
				lastPropertyValueFound = wop.getNewValue();
				if (wop.isUnconditional() && unconditional) {
					throw new WabitPersistenceException(
							uuid,
							"Cannot make more than one unconditional persist property call in the same transaction.");
				}

			}
		}

		if (lastPropertyValueFound != null) {
			if (!unconditional && !oldValue.equals(lastPropertyValueFound)) {
				throw new WabitPersistenceException(
						uuid,
						"The expected property value \""
								+ oldValue
								+ "\" does not match with the actual property value \""
								+ lastPropertyValueFound + "\"");
			}
		} else if (!unconditional) {
			WabitObject wo = session.getWorkspace().findByUuid(uuid,
					WabitObject.class);
			Object propertyValue = null;

			if (isCommonProperty(propertyName)) {
				propertyValue = getCommonProperty(wo, propertyName);
			} else if (wo instanceof CellSetRenderer) {
				propertyValue = getCellSetRendererProperty(
						(CellSetRenderer) wo, propertyName);
			} else if (wo instanceof Chart) {
				propertyValue = getChartProperty((Chart) wo, propertyName);
			} else if (wo instanceof ChartColumn) {
				propertyValue = getChartColumnProperty((ChartColumn) wo,
						propertyName);
			} else if (wo instanceof ChartRenderer) {
				propertyValue = getChartRendererProperty((ChartRenderer) wo,
						propertyName);
			} else if (wo instanceof ColumnInfo) {
				propertyValue = getColumnInfoProperty((ColumnInfo) wo,
						propertyName);
			} else if (wo instanceof ContentBox) {
				propertyValue = getContentBoxProperty((ContentBox) wo,
						propertyName);
			} else if (wo instanceof Guide) {
				propertyValue = getGuideProperty((Guide) wo, propertyName);
			} else if (wo instanceof ImageRenderer) {
				propertyValue = getImageRendererProperty((ImageRenderer) wo,
						propertyName);
			} else if (wo instanceof Label) {
				propertyValue = getLabelProperty((Label) wo, propertyName);
			} else if (wo instanceof Layout) {
				propertyValue = getLayoutProperty((Layout) wo, propertyName);
			} else if (wo instanceof OlapQuery) {
				propertyValue = getOlapQueryProperty((OlapQuery) wo,
						propertyName);
			} else if (wo instanceof Page) {
				propertyValue = getPageProperty((Page) wo, propertyName);
			} else if (wo instanceof QueryCache) {
				propertyValue = getQueryCacheProperty((QueryCache) wo,
						propertyName);
			} else if (wo instanceof ResultSetRenderer) {
				propertyValue = getResultSetRendererProperty(
						(ResultSetRenderer) wo, propertyName);
			} else if (wo instanceof WabitConstantsContainer) {
				propertyValue = getWabitConstantsContainerProperty(
						(WabitConstantsContainer) wo, propertyName);
			} else if (wo instanceof WabitDataSource) {
				propertyValue = getWabitDataSourceProperty(
						(WabitDataSource) wo, propertyName);
			} else if (wo instanceof WabitImage) {
				propertyValue = getWabitImageProperty((WabitImage) wo,
						propertyName);

				// Convert oldValue into a byte array so that it can be compared
				// with propertyValue
				final Image wabitInnerImage = ((WabitImage) wo).getImage();

				if (wabitInnerImage != null) {
					BufferedImage image;
					if (wabitInnerImage instanceof BufferedImage) {
						image = (BufferedImage) wabitInnerImage;
					} else {
						image = new BufferedImage(wabitInnerImage
								.getWidth(null), wabitInnerImage
								.getHeight(null), BufferedImage.TYPE_INT_ARGB);
						final Graphics2D g = image.createGraphics();
						g.drawImage(wabitInnerImage, 0, 0, null);
						g.dispose();
					}
					if (image != null) {
						try {
							ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
							ImageIO.write(image, "PNG", byteStream);
							byte[] currentByteArray = (byte[]) propertyValue;

							InputStream inputStream = (InputStream) oldValue;
							byte[] oldByteArray = new byte[currentByteArray.length];
							int size = inputStream.read(oldByteArray);

							if (size == currentByteArray.length
									&& inputStream.available() > 0) {
								oldValue = oldByteArray;

							} else {
								throw new WabitPersistenceException(
										uuid,
										"The expected property value \""
												+ oldValue
												+ "\" does not match with the actual property value \""
												+ propertyValue + "\"");
							}
						} catch (IOException e) {
							throw new WabitPersistenceException(uuid, e);
						}
					} else {
						throw new WabitPersistenceException(uuid,
								"Invalid image.");
					}
				} else {
					throw new WabitPersistenceException(uuid, "Invalid image.");
				}

			} else if (wo instanceof WabitItem) {
				propertyValue = getWabitItemProperty((WabitItem) wo,
						propertyName);
			} else if (wo instanceof WabitJoin) {
				propertyValue = getWabitJoinProperty((WabitJoin) wo,
						propertyName);
			} else if (wo instanceof WabitOlapAxis) {
				propertyValue = getWabitOlapAxisProperty((WabitOlapAxis) wo,
						propertyName);
			} else if (wo instanceof WabitOlapDimension) {
				propertyValue = getWabitOlapDimensionProperty(
						(WabitOlapDimension) wo, propertyName);
			} else if (wo instanceof WabitOlapSelection) {
				propertyValue = getWabitOlapSelectionProperty(
						(WabitOlapSelection) wo, propertyName);
			} else if (wo instanceof WabitTableContainer) {
				propertyValue = getWabitTableContainerProperty(
						(WabitTableContainer) wo, propertyName);
			} else if (wo instanceof WabitWorkspace) {
				propertyValue = getWabitWorkspaceProperty((WabitWorkspace) wo,
						propertyName);
			} else {
				throw new WabitPersistenceException(uuid, "Invalid WabitObject");
			}

			if (!oldValue.equals(propertyValue)) {
				throw new WabitPersistenceException(
						uuid,
						"The expected property value \""
								+ oldValue
								+ "\" does not match with the actual property value \""
								+ propertyValue + "\"");
			}
		}

		persistedProperties.put(uuid, new WabitObjectProperty(propertyName,
				newValue, unconditional));

		if (transactionCount == 0) {
			commitProperties();
		}
	}

	/**
	 * Determines if a given property name is a common property among all
	 * {@link WabitObject}s
	 * 
	 * @param propertyName
	 *            The property name to check if it is common
	 * @return Determinant of whether the given property name is common
	 */
	private boolean isCommonProperty(String propertyName) {
		return (propertyName.equals("name") || propertyName.equals("uuid"));
	}

	/**
	 * Retrieves a common property value from a {@link WabitObject}. The only
	 * two common properties are "name" and "uuid".
	 * 
	 * @param wo
	 *            The {@link WabitObject} to retrieve the property from
	 * @param propertyName
	 *            The property name of the value to retrieve
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getCommonProperty(WabitObject wo, String propertyName)
			throws WabitPersistenceException {
		if (propertyName.equals("name")) {
			return wo.getName();
		} else if (propertyName.equals("UUID")) {
			return wo.getUUID();
		} else {
			throw new WabitPersistenceException(wo.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Commits a persisted {@link WabitObject} common property.
	 * 
	 * @param wo
	 *            The {@link WabitObject} to commit the persisted common
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitCommonProperty(WabitObject wo, String propertyName,
			Object newValue) throws WabitPersistenceException {
		if (propertyName.equals("name")) {
			wo.setName(newValue.toString());
		} else if (propertyName.equals("UUID")) {
			wo.setUUID(newValue.toString());
		} else {
			throw new WabitPersistenceException(wo.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitWorkspace} object.
	 * 
	 * @param workspace
	 *            The {@link WabitWorkspace} object to retrieve the property
	 *            from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getWabitWorkspaceProperty(WabitWorkspace workspace,
			String propertyName) throws WabitPersistenceException {
		if (propertyName.equals("editorPanelModel")) {
			return workspace.getEditorPanelModel().getUUID();
		} else {
			throw new WabitPersistenceException(workspace.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Commits a persisted {@link WabitWorkspace} object property.
	 * 
	 * @param workspace
	 *            The {@link WabitWorkspace} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitWabitWorkspaceProperty(WabitWorkspace workspace,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		String uuid = workspace.getUUID();

		if (propertyName.equals("editorPanelModel")) {
			String editorUUID = newValue.toString();
			WabitObject editorPanel = workspace.findByUuid(editorUUID,
					WabitObject.class);

			if (editorPanel == null) {
				throw new WabitPersistenceException(uuid,
						"Invalid editorPanelModel UUID: " + editorUUID);
			}

			workspace.setEditorPanelModel(editorPanel);
		} else {
			throw new WabitPersistenceException(uuid, "Invalid property: "
					+ propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitDataSource} object.
	 * Currently, uncommon properties cannot be retrieved from this class.
	 * 
	 * @param wds
	 *            The {@link WabitDataSource} object to retrieve the property
	 *            from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getWabitDataSourceProperty(WabitDataSource wds,
			String propertyName) throws WabitPersistenceException {
		throw new WabitPersistenceException(wds.getUUID(), "Invalid property: "
				+ propertyName);
	}

	/**
	 * Commits a persisted {@link WabitDataSource} property. Currently, uncommon
	 * properties cannot be persisted for this class.
	 * 
	 * @param wds
	 *            The {@link WabitDataSource} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitWabitDataSourceProperty(WabitDataSource wds,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		throw new WabitPersistenceException(wds.getUUID(), "Invalid property: "
				+ propertyName);
	}

	/**
	 * Retrieves a property value from a {@link QueryCache} object.
	 * 
	 * @param query
	 *            The {@link QueryCache} object to retrieve the property from.
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getQueryCacheProperty(QueryCache query, String propertyName)
			throws WabitPersistenceException {
		if (propertyName.equals("zoomLevel")) {
			return query.getZoomLevel();

		} else if (propertyName.equals("streaming")) {
			return query.isStreaming();

		} else if (propertyName.equals("streamingRowLimit")) {
			return query.getStreamingRowLimit();

		} else if (propertyName.equals(QueryImpl.ROW_LIMIT)) {
			return query.getRowLimit();

		} else if (propertyName.equals(QueryImpl.GROUPING_ENABLED)) {
			return query.isGroupingEnabled();

		} else if (propertyName.equals("promptForCrossJoins")) {
			return query.getPromptForCrossJoins();

		} else if (propertyName.equals("automaticallyExecuting")) {
			return query.isAutomaticallyExecuting();

		} else if (propertyName.equals(QueryImpl.GLOBAL_WHERE_CLAUSE)) {
			return query.getGlobalWhereClause();

		} else if (propertyName.equals(QueryImpl.USER_MODIFIED_QUERY)) {
			return query.generateQuery();
			// TODO

		} else if (propertyName.equals("executeQueriesWithCrossJoins")) {
			return query.getExecuteQueriesWithCrossJoins();

		} else if (propertyName.equals("dataSource")) {
			return query.getWabitDataSource().getName();

		} else {
			throw new WabitPersistenceException(query.getUUID(),
					"Invalid property: " + propertyName);
		}

	}

	/**
	 * Commits a persisted {@link QueryCache} property
	 * 
	 * @param query
	 *            The {@link QueryCache} object to commit the persisted property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitQueryCacheProperty(QueryCache query,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		String uuid = query.getUUID();

		if (propertyName.equals("zoomLevel")) {
			query.setZoomLevel(Integer.valueOf(newValue.toString()));

		} else if (propertyName.equals("streaming")) {
			query.setStreaming(Boolean.valueOf(newValue.toString()));

		} else if (propertyName.equals("streamingRowLimit")) {
			query.setStreamingRowLimit(Integer.valueOf(newValue.toString()));

		} else if (propertyName.equals(QueryImpl.ROW_LIMIT)) {
			query.setRowLimit(Integer.valueOf(newValue.toString()));

		} else if (propertyName.equals(QueryImpl.GROUPING_ENABLED)) {
			query.setGroupingEnabled(Boolean.valueOf(newValue.toString()));

		} else if (propertyName.equals("promptForCrossJoins")) {
			query.setPromptForCrossJoins(Boolean.valueOf(newValue.toString()));

		} else if (propertyName.equals("automaticallyExecuting")) {
			query.setAutomaticallyExecuting(Boolean
					.valueOf(newValue.toString()));

		} else if (propertyName.equals(QueryImpl.GLOBAL_WHERE_CLAUSE)) {
			query.setGlobalWhereClause(newValue.toString());

		} else if (propertyName.equals(QueryImpl.USER_MODIFIED_QUERY)) {
			query.defineUserModifiedQuery(newValue.toString());
			// TODO

		} else if (propertyName.equals("executeQueriesWithCrossJoins")) {
			query.setExecuteQueriesWithCrossJoins(Boolean.valueOf(newValue
					.toString()));

		} else if (propertyName.equals("dataSource")) {
			query.setDataSource(session.getWorkspace().getDataSource(
					newValue.toString(), JDBCDataSource.class));

		} else {
			throw new WabitPersistenceException(uuid, "Invalid property: "
					+ propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitConstantsContainer} object.
	 * 
	 * @param wabitConstantsContainer
	 *            The {@link WabitConstantsContainer} to retrieve the property
	 *            from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getWabitConstantsContainerProperty(
			WabitConstantsContainer wabitConstantsContainer, String propertyName)
			throws WabitPersistenceException {
		Point2D position = wabitConstantsContainer.getDelegate().getPosition();

		if (propertyName.equals("position")) {
			return position.toString();
			// TODO

		} else {
			throw new WabitPersistenceException(wabitConstantsContainer
					.getUUID(), "Invalid property: " + propertyName);
		}
	}

	/**
	 * Commits a persisted {@link WabitConstantsContainer} object property
	 * 
	 * @param wabitConstantsContainer
	 *            The {@link WabitConstantsContainer} object to commit the
	 *            persisted property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitWabitConstantsContainerProperty(
			WabitConstantsContainer wabitConstantsContainer,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		ca.sqlpower.query.Container container = wabitConstantsContainer
				.getDelegate();
		Point2D position = container.getPosition();

		if (propertyName.equals("position")) {
			// TODO Use commons converter
			// container.setPosition(new Point2D.Double(Double.valueOf(newValue
			// .toString()), position.getY()));

		} else {
			throw new WabitPersistenceException(wabitConstantsContainer
					.getUUID(), "Invalid property: " + propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitTableContainer} object.
	 * 
	 * @param wabitTableContainer
	 *            The {@link WabitTableContainer} to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getWabitTableContainerProperty(
			WabitTableContainer wabitTableContainer, String propertyName)
			throws WabitPersistenceException {
		ca.sqlpower.query.Container container = wabitTableContainer
				.getDelegate();
		Point2D position = container.getPosition();

		if (propertyName.equals("position")) {
			return null;
			// TODO Use commons converter

		} else if (propertyName.equals("alias")) {
			return container.getAlias();

		} else {
			throw new WabitPersistenceException(wabitTableContainer.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Commits a persisted {@link WabitTableContainer} object property
	 * 
	 * @param wabitTableContainer
	 *            The {@link WabitTableContainer} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitWabitTableContainerProperty(
			WabitTableContainer wabitTableContainer, String propertyName,
			Object newValue) throws WabitPersistenceException {
		Container container = wabitTableContainer.getDelegate();
		Point2D position = container.getPosition();

		if (propertyName.equals("position")) {
			// TODO Use commons converter
			// container.setPosition(new Point2D.Double(Double.valueOf(newValue
			// .toString()), position.getY()));

		} else if (propertyName.equals(Container.CONTAINTER_ALIAS_CHANGED)) {
			container.setAlias(newValue.toString());

		} else {
			throw new WabitPersistenceException(wabitTableContainer.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitItem} object.
	 * 
	 * @param wabitItem
	 *            The {@link WabitItem} to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getWabitItemProperty(WabitItem wabitItem, String propertyName)
			throws WabitPersistenceException {
		String uuid = wabitItem.getUUID();
		Item item = wabitItem.getDelegate();

		if (item instanceof SQLObjectItem || item instanceof StringItem) {
			if (propertyName.equals(Item.ALIAS)) {
				return item.getAlias();

			} else if (propertyName.equals(Item.WHERE)) {
				return item.getWhere();

			} else if (propertyName.equals(Item.GROUP_BY)) {
				return item.getGroupBy().name();

			} else if (propertyName.equals(Item.HAVING)) {
				return item.getHaving();

			} else if (propertyName.equals(Item.ORDER_BY)) {
				return item.getOrderBy().name();

			} else if (propertyName.equals(Item.SELECTED)) {
				return item.getSelected();

			} else {
				throw new WabitPersistenceException(uuid, "Invalid property: "
						+ propertyName);
			}
		} else {
			throw new WabitPersistenceException(uuid, "Unknown WabitItem: "
					+ wabitItem.toString());
		}
	}

	/**
	 * Commits a persisted {@link WabitItem} object property
	 * 
	 * @param wabitItem
	 *            The {@link WabitItem} object to commit the persisted property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitWabitItemProperty(WabitItem wabitItem,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		String uuid = wabitItem.getUUID();
		Item item = wabitItem.getDelegate();

		if (item instanceof SQLObjectItem || item instanceof StringItem) {
			if (propertyName.equals(Item.ALIAS)) {
				item.setAlias(newValue.toString());

			} else if (propertyName.equals(Item.WHERE)) {
				item.setWhere(newValue.toString());

			} else if (propertyName.equals(Item.GROUP_BY)) {
				item.setGroupBy(SQLGroupFunction.valueOf(newValue.toString()));

			} else if (propertyName.equals(Item.HAVING)) {
				item.setHaving(newValue.toString());

			} else if (propertyName.equals(Item.ORDER_BY)) {
				item.setOrderBy(OrderByArgument.valueOf(newValue.toString()));

			} else if (propertyName.equals(Item.SELECTED)) {
				item.setSelected(Integer.valueOf(newValue.toString()));

			} else {
				throw new WabitPersistenceException(uuid, "Invalid property: "
						+ propertyName);
			}
		} else {
			throw new WabitPersistenceException(uuid, "Unknown WabitItem: "
					+ wabitItem.toString());
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitJoin} object.
	 * 
	 * @param wabitJoin
	 *            The {@link WabitJoin} object to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getWabitJoinProperty(WabitJoin wabitJoin, String propertyName)
			throws WabitPersistenceException {
		SQLJoin join = wabitJoin.getDelegate();

		if (propertyName.equals(SQLJoin.LEFT_JOIN_CHANGED)) {
			return join.isLeftColumnOuterJoin();
		} else if (propertyName.equals(SQLJoin.RIGHT_JOIN_CHANGED)) {
			return join.isRightColumnOuterJoin();
		} else if (propertyName.equals(SQLJoin.COMPARATOR_CHANGED)) {
			return join.getComparator();
		} else {
			throw new WabitPersistenceException(wabitJoin.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Commits a persisted {@link WabitJoin} object property.
	 * 
	 * @param wabitJoin
	 *            The {@link WabitJoin} object to commit the persisted property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The property value
	 * @throws WabitPersistenceException
	 */
	private void commitWabitJoinProperty(WabitJoin wabitJoin,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		SQLJoin join = wabitJoin.getDelegate();

		if (propertyName.equals(SQLJoin.LEFT_JOIN_CHANGED)) {
			join.setLeftColumnOuterJoin(Boolean.valueOf(newValue.toString()));
		} else if (propertyName.equals(SQLJoin.RIGHT_JOIN_CHANGED)) {
			join.setRightColumnOuterJoin(Boolean.valueOf(newValue.toString()));
		} else if (propertyName.equals(SQLJoin.COMPARATOR_CHANGED)) {
			join.setComparator(newValue.toString());
		} else {
			throw new WabitPersistenceException(wabitJoin.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Retrieves a property value from an {@link OlapQuery} object.
	 * 
	 * @param olapQuery
	 *            The {@link OlapQuery} object to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getOlapQueryProperty(OlapQuery olapQuery, String propertyName)
			throws WabitPersistenceException {
		if (propertyName.equals("olapDataSource")) {
			return olapQuery.getOlapDataSource().getName();
		} else if (propertyName.equals("catalogName")) {
			return olapQuery.getCatalogName();

		} else if (propertyName.equals("schemaName")) {
			return olapQuery.getSchemaName();

		} else if (propertyName.equals("cubeName")) {
			return olapQuery.getCubeName();

		} else {
			throw new WabitPersistenceException(olapQuery.getUUID(),
					"Invalid property: " + propertyName);
		}
		// TODO
	}

	/**
	 * Commits a persisted {@link OlapQuery} object property
	 * 
	 * @param olapQuery
	 *            The {@link OlapQuery} object to commit the persisted property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitOlapQueryProperty(OlapQuery olapQuery,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		if (propertyName.equals("olapDataSource")) {
			olapQuery.setOlapDataSource((Olap4jDataSource) newValue);
		} else if (propertyName.equals("currentCube")) {
			// TODO
		} else if (propertyName.equals("nonEmpty")) {
			olapQuery.setNonEmpty(Boolean.valueOf(newValue.toString()));
		} else {
			throw new WabitPersistenceException(olapQuery.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitOlapSelection} object.
	 * 
	 * @param selection
	 *            The {@link WabitOlapSelection} object to retrieve the property
	 *            from.
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getWabitOlapSelectionProperty(WabitOlapSelection selection,
			String propertyName) throws WabitPersistenceException {
		if (propertyName.equals("operator")) {
			return selection.getOperator();

		} else if (propertyName.equals("uniqueMemberName")) {
			return selection.getUniqueMemberName();

		} else {
			throw new WabitPersistenceException(selection.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Commits a persisted {@link WabitOlapSelection} object property.
	 * Currently, uncommon properties cannot be set.
	 * 
	 * @param selection
	 *            The {@link WabitOlapSelection} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitWabitOlapSelectionProperty(WabitOlapSelection selection,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		throw new WabitPersistenceException(selection.getUUID(),
				"Invalid property: " + propertyName);
	}

	/**
	 * Retrieve a property value from a WabitOlapDimension objection. Currently,
	 * there are no uncommon properties to retrieve.
	 * 
	 * @param dimension
	 *            The {@link WabitOlapDimension} to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getWabitOlapDimensionProperty(WabitOlapDimension dimension,
			String propertyName) throws WabitPersistenceException {
		throw new WabitPersistenceException(dimension.getUUID(),
				"Invalid property: " + propertyName);
	}

	/**
	 * Commits a persisted {@link WabitOlapDimension} object property
	 * 
	 * @param dimension
	 *            The {@link WabitOlapDimension} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitWabitOlapDimensionProperty(WabitOlapDimension dimension,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		throw new WabitPersistenceException(dimension.getUUID(),
				"Invalid property: " + propertyName);
	}

	/**
	 * Retrieves a property value from a {@link WabitOlapAxis} object.
	 * 
	 * @param olapAxis
	 *            The {@link WabitOlapAxis} object to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getWabitOlapAxisProperty(WabitOlapAxis olapAxis,
			String propertyName) throws WabitPersistenceException {
		if (propertyName.equals("ordinal")) {
			return olapAxis.getOrdinal().axisOrdinal();

		} else if (propertyName.equals("nonEmpty")) {
			return olapAxis.isNonEmpty();

		} else if (propertyName.equals("sortEvaluationLiteral")) {
			return olapAxis.getSortEvaluationLiteral();

		} else if (propertyName.equals("sortOrder")) {
			return olapAxis.getSortOrder();

		} else {
			throw new WabitPersistenceException(olapAxis.getUUID(),
					"Invalid property: " + propertyName);
		}

	}

	/**
	 * Commits a persisted {@link WabitOlapAxis} object property
	 * 
	 * @param olapAxis
	 *            The {@link WabitOlapAxis} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitWabitOlapAxisProperty(WabitOlapAxis olapAxis,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		if (propertyName.equals("nonEmpty")) {
			olapAxis.setNonEmpty(Boolean.valueOf(newValue.toString()));

		} else if (propertyName.equals("sortEvaluationLiteral")) {
			olapAxis.setSortEvaluationLiteral(newValue.toString());

		} else if (propertyName.equals("sortOrder")) {
			olapAxis.setSortOrder(newValue.toString());

		} else {
			throw new WabitPersistenceException(olapAxis.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link Chart} object.
	 * 
	 * @param chart
	 *            The {@link Chart} object to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getChartProperty(Chart chart, String propertyName)
			throws WabitPersistenceException {
		if (propertyName.equals("xaxisName")) {
			return chart.getXaxisName();

		} else if (propertyName.equals("yaxisName")) {
			return chart.getYaxisName();

		} else if (propertyName.equals("xAxisLabelRotation")) {
			return chart.getXaxisLabelRotation();

		} else if (propertyName.equals("gratuitousAnimated")) {
			return chart.isGratuitouslyAnimated();

		} else if (propertyName.equals("type")) {
			return chart.getType().toString();

		} else if (propertyName.equals("legendPosition")) {
			return chart.getLegendPosition().name();

		} else if (propertyName.equals("query")) {
			return chart.getQuery().getUUID();

		} else {
			throw new WabitPersistenceException(chart.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Commits a persisted {@link Chart} object property
	 * 
	 * @param chart
	 *            The {@link Chart} object to commit the persisted property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitChartProperty(Chart chart, String propertyName,
			Object newValue) throws WabitPersistenceException {
		String uuid = chart.getUUID();

		if (propertyName.equals("xaxisName")) {
			chart.setXaxisName(newValue.toString());

		} else if (propertyName.equals("yaxisName")) {
			chart.setYaxisName(newValue.toString());

		} else if (propertyName.equals("xAxisLabelRrotation")) {
			chart.setXAxisLabelRotation(Double.valueOf(newValue.toString()));

		} else if (propertyName.equals("gratuitousAnimated")) {
			chart.setGratuitouslyAnimated(Boolean.valueOf(newValue.toString()));

		} else if (propertyName.equals("type")) {
			chart.setType(ChartType.valueOf(newValue.toString()));

		} else if (propertyName.equals("legendPosition")) {
			chart
					.setLegendPosition(LegendPosition.valueOf(newValue
							.toString()));

		} else if (propertyName.equals("query")) {
			ResultSetProducer rsProducer = session.getWorkspace().findByUuid(
					newValue.toString(), ResultSetProducer.class);
			if (rsProducer == null) {
				throw new WabitPersistenceException(uuid, "Invalid query-id: "
						+ newValue.toString());
			}

			try {
				chart.setQuery(rsProducer);
			} catch (SQLException e) {
				throw new WabitPersistenceException(uuid, e);
			}

		} else {
			throw new WabitPersistenceException(uuid, "Invalid property: "
					+ propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link ChartColumn} object.
	 * 
	 * @param chartColumn
	 *            The {@link ChartColumn} object to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getChartColumnProperty(ChartColumn chartColumn,
			String propertyName) throws WabitPersistenceException {
		if (propertyName.equals("roleInChart")) {
			return chartColumn.getRoleInChart().name();

		} else if (propertyName.equals("XAxisIdentifier")) {
			return chartColumn.getXAxisIdentifier().getName();

		} else {
			throw new WabitPersistenceException(chartColumn.getUUID(),
					"Invalid property: " + propertyName);
		}

	}

	/**
	 * Commits a persisted {@link ChartColumn} object property
	 * 
	 * @param chartColumn
	 *            The {@link ChartColumn} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitChartColumnProperty(ChartColumn chartColumn,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		if (propertyName.equals("roleInChart")) {
			chartColumn.setRoleInChart(ColumnRole.valueOf(newValue.toString()));

		} else if (propertyName.equals("XAxisIdentifier")) {
			chartColumn.setXAxisIdentifier(new ChartColumn(newValue.toString(),
					chartColumn.getDataType()));

		} else {
			throw new WabitPersistenceException(chartColumn.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	private Object getWabitImageProperty(WabitImage wabitImage,
			String propertyName) throws WabitPersistenceException {
		String uuid = wabitImage.getUUID();

		if (propertyName.equals("image")) {
			final Image wabitInnerImage = wabitImage.getImage();

			if (wabitInnerImage != null) {
				BufferedImage image;
				if (wabitInnerImage instanceof BufferedImage) {
					image = (BufferedImage) wabitInnerImage;
				} else {
					image = new BufferedImage(wabitInnerImage.getWidth(null),
							wabitInnerImage.getHeight(null),
							BufferedImage.TYPE_INT_ARGB);
					final Graphics2D g = image.createGraphics();
					g.drawImage(wabitInnerImage, 0, 0, null);
					g.dispose();
				}
				if (image != null) {
					try {
						ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
						ImageIO.write(image, "PNG", byteStream);
						byte[] currentByteArray = new Base64()
								.encode(byteStream.toByteArray());

						return currentByteArray;

					} catch (IOException e) {
						throw new WabitPersistenceException(uuid, e);
					}
				} else {
					throw new WabitPersistenceException(uuid, "Invalid image.");
				}
			} else {
				throw new WabitPersistenceException(uuid, "Invalid image.");
			}

		} else {
			throw new WabitPersistenceException(uuid, "Invalid property: "
					+ propertyName);
		}

	}

	/**
	 * Commits a persisted {@link WabitImage} object property
	 * 
	 * @param wabitImage
	 *            The {@link WabitImage} object to commit the persisted property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitWabitImageProperty(WabitImage wabitImage,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		String uuid = wabitImage.getUUID();

		if (propertyName.equals("image")) {
			try {
				wabitImage.setImage(ImageIO.read((InputStream) newValue));
			} catch (IOException e) {
				throw new WabitPersistenceException(uuid,
						"Cannot set image from InputStream.");
			}

		} else {
			throw new WabitPersistenceException(uuid, "Invalid property: "
					+ propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link Layout} object.
	 * 
	 * @param layout
	 *            The {@link Layout} object to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getLayoutProperty(Layout layout, String propertyName)
			throws WabitPersistenceException {
		if (propertyName.equals("zoomLevel")) {
			return layout.getZoomLevel();

		} else {
			throw new WabitPersistenceException(layout.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Commits a persisted {@link Layout} object property
	 * 
	 * @param layout
	 *            The {@link Layout} object to commit the persisted property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitLayoutProperty(Layout layout, String propertyName,
			Object newValue) throws WabitPersistenceException {
		if (propertyName.equals("zoomLevel")) {
			layout.setZoomLevel(Integer.valueOf(newValue.toString()));

		} else {
			throw new WabitPersistenceException(layout.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link Page} object.
	 * 
	 * @param page
	 *            The {@link Page} object to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getPageProperty(Page page, String propertyName)
			throws WabitPersistenceException {
		if (propertyName.equals("height")) {
			return page.getHeight();

		} else if (propertyName.equals("width")) {
			return page.getWidth();

		} else if (propertyName.equals("orientation")) {
			return page.getOrientation();

		} else if (propertyName.equals("defaultFont")) {
			return page.getDefaultFont().toString();
			// TODO

		} else {
			throw new WabitPersistenceException(page.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Commits a persisted {@link Page} object property
	 * 
	 * @param page
	 *            The {@link Page} object to commit the persisted property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitPageProperty(Page page, String propertyName,
			Object newValue) throws WabitPersistenceException {
		if (propertyName.equals("height")) {
			page.setHeight(Integer.parseInt(newValue.toString()));

		} else if (propertyName.equals("width")) {
			page.setWidth(Integer.parseInt(newValue.toString()));

		} else if (propertyName.equals("orientation")) {
			page.setOrientation(PageOrientation.valueOf(newValue.toString()));

		} else if (propertyName.equals("defaultFont")) {
			page.setDefaultFont(Font.decode(newValue.toString()));
			// TODO

		} else {
			throw new WabitPersistenceException(page.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link ContentBox} object.
	 * 
	 * @param contentBox
	 *            The {@link ContentBox} object to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getContentBoxProperty(ContentBox contentBox,
			String propertyName) throws WabitPersistenceException {
		if (propertyName.equals("height")) {
			return contentBox.getHeight();

		} else if (propertyName.equals("width")) {
			return contentBox.getWidth();

		} else if (propertyName.equals("x")) {
			return contentBox.getX();

		} else if (propertyName.equals("y")) {
			return contentBox.getY();

		} else if (propertyName.equals("contentRenderer")) {
			return contentBox.getContentRenderer().getUUID();

		} else if (propertyName.equals("font")) {
			return contentBox.getFont().toString();

		} else {
			throw new WabitPersistenceException(contentBox.getUUID(),
					"Invalid property: " + propertyName);
		}
		// TODO
	}

	/**
	 * Commits a persisted {@link ContentBox} object property
	 * 
	 * @param contentBox
	 *            The {@link ContentBox} object to commit the persisted property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitContentBoxProperty(ContentBox contentBox,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		if (propertyName.equals("height")) {
			contentBox.setHeight(Double.valueOf(newValue.toString()));

		} else if (propertyName.equals("width")) {
			contentBox.setWidth(Double.valueOf(newValue.toString()));

		} else if (propertyName.equals("x")) {
			contentBox.setX(Double.valueOf(newValue.toString()));

		} else if (propertyName.equals("y")) {
			contentBox.setY(Double.valueOf(newValue.toString()));

		} else if (propertyName.equals("contentRenderer")) {
			contentBox.setContentRenderer(session.getWorkspace().findByUuid(
					newValue.toString(), ReportContentRenderer.class));

		} else if (propertyName.equals("font")) {
			contentBox.setFont(Font.decode(newValue.toString()));
			// TODO

		} else {
			throw new WabitPersistenceException(contentBox.getUUID(),
					"Invalid property: " + propertyName);
		}
		// TODO
	}

	/**
	 * Retrieves a property value from a {@link ChartRenderer} object.
	 * Currently, uncommon properties cannot be retrieved from this object.
	 * 
	 * @param cRenderer
	 *            The {@link ChartRenderer} object to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getChartRendererProperty(ChartRenderer cRenderer,
			String propertyName) throws WabitPersistenceException {
		throw new WabitPersistenceException(cRenderer.getUUID(),
				"Invalid property: " + propertyName);
	}

	/**
	 * Commits a persisted {@link ChartRenderer} object property
	 * 
	 * @param cRenderer
	 *            The {@link ChartRenderer} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitChartRendererProperty(ChartRenderer cRenderer,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		throw new WabitPersistenceException(cRenderer.getUUID(),
				"Invalid property: " + propertyName);
	}

	/**
	 * Retrieves a property value from a {@link CellSetRenderer} object.
	 * 
	 * @param csRenderer
	 *            The {@link CellSetRenderer} object to retrieve the property
	 *            from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getCellSetRendererProperty(CellSetRenderer csRenderer,
			String propertyName) throws WabitPersistenceException {
		if (propertyName.equals("modifiedOlapQuery")) {
			return csRenderer.getModifiedOlapQuery().getUUID();

		} else if (propertyName.equals("bodyAlignment")) {
			return csRenderer.getBodyAlignment().toString();

		} else if (propertyName.equals("bodyFormat")) {
			return csRenderer.getBodyFormat().toPattern();

		} else if (propertyName.equals("headerFont")) {
			return csRenderer.getHeaderFont().toString();
			// TODO

		} else if (propertyName.equals("bodyFont")) {
			return csRenderer.getBodyFont().toString();
			// TODO

		} else {
			throw new WabitPersistenceException(csRenderer.getUUID(),
					"Invalid property: " + propertyName);
		}
		// TODO
	}

	/**
	 * Commits a persisted {@link CellSetRenderer} object property
	 * 
	 * @param csRenderer
	 *            The {@link CellSetRenderer} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitCellSetRendererProperty(CellSetRenderer csRenderer,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		if (propertyName.equals("modifiedOlapQuery")) {
			csRenderer.setModifiedOlapQuery(session.getWorkspace().findByUuid(
					newValue.toString(), OlapQuery.class));

		} else if (propertyName.equals("bodyAlignment")) {
			csRenderer.setBodyAlignment(HorizontalAlignment.valueOf(newValue
					.toString()));

		} else if (propertyName.equals("bodyFormat")) {
			csRenderer.setBodyFormat(new DecimalFormat(newValue.toString()));

		} else if (propertyName.equals("headerFont")) {
			csRenderer.setHeaderFont(Font.decode(newValue.toString()));

		} else if (propertyName.equals("bodyFont")) {
			csRenderer.setBodyFont(Font.decode(newValue.toString()));

		} else {
			throw new WabitPersistenceException(csRenderer.getUUID(),
					"Invalid property: " + propertyName);
		}
		// TODO
	}

	/**
	 * Retrieves a property value from an {@link ImageRenderer} object.
	 * 
	 * @param iRenderer
	 *            The {@link ImageRenderer} object to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getImageRendererProperty(ImageRenderer iRenderer,
			String propertyName) throws WabitPersistenceException {
		if (propertyName.equals("image")) {
			return iRenderer.getImage().getUUID();

		} else if (propertyName.equals("preservingAspectRatio")) {
			return iRenderer.isPreservingAspectRatio();

		} else if (propertyName.equals("preserveAspectRatioWhenResizing")) {
			return iRenderer.isPreserveAspectRatioWhenResizing();

		} else if (propertyName.equals("HAlign")) {
			return iRenderer.getHAlign().name();

		} else if (propertyName.equals("VAlign")) {
			return iRenderer.getVAlign().name();

		} else {
			throw new WabitPersistenceException(iRenderer.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Commits a persisted {@link ImageRenderer} object property
	 * 
	 * @param iRenderer
	 *            The {@link ImageRenderer} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitImageRendererProperty(ImageRenderer iRenderer,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		if (propertyName.equals("image")) {
			iRenderer.setImage(session.getWorkspace().findByUuid(
					newValue.toString(), WabitImage.class));

		} else if (propertyName.equals("preservingAspectRatio")) {
			iRenderer.setPreservingAspectRatio(Boolean.valueOf(newValue
					.toString()));

		} else if (propertyName.equals("preserveAspectRatioWhenResizing")) {
			iRenderer.setPreserveAspectRatioWhenResizing(Boolean
					.valueOf(newValue.toString()));

		} else if (propertyName.equals("HAlign")) {
			iRenderer.setHAlign(HorizontalAlignment
					.valueOf(newValue.toString()));

		} else if (propertyName.equals("VAlign")) {
			iRenderer.setVAlign(VerticalAlignment.valueOf(newValue.toString()));

		} else {
			throw new WabitPersistenceException(iRenderer.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link Label} object.
	 * 
	 * @param label
	 *            The {@link Label} object to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getLabelProperty(Label label, String propertyName)
			throws WabitPersistenceException {
		if (propertyName.equals("horizontalAlignment")) {
			return label.getHorizontalAlignment().name();

		} else if (propertyName.equals("verticalAlignment")) {
			return label.getVerticalAlignment().name();

		} else if (propertyName.equals("text")) {
			return label.getText();

		} else if (propertyName.equals("backgroundColour")) {
			return label.getBackgroundColour().toString();

		} else if (propertyName.equals("font")) {
			return label.getFont().toString();
			// TODO

		} else {
			throw new WabitPersistenceException(label.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Commits a persisted {@link Label} object property
	 * 
	 * @param label
	 *            The {@link Label} object to commit the persisted property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitLabelProperty(Label label, String propertyName,
			Object newValue) throws WabitPersistenceException {
		if (propertyName.equals("horizontalAlignment")) {
			label.setHorizontalAlignment(HorizontalAlignment.valueOf(newValue
					.toString()));

		} else if (propertyName.equals("verticalAlignment")) {
			label.setVerticalAlignment(VerticalAlignment.valueOf(newValue
					.toString()));

		} else if (propertyName.equals("text")) {
			label.setText(newValue.toString());

		} else if (propertyName.equals("backgroundColour")) {
			label.setBackgroundColour(Color.decode(newValue.toString()));

		} else if (propertyName.equals("font")) {
			label.setFont(Font.decode(newValue.toString()));
			// TODO

		} else {
			throw new WabitPersistenceException(label.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link ResultSetRenderer} object.
	 * 
	 * @param rsRenderer
	 *            The {@link ResultSetRenderer} object to retrieve the property
	 *            from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getResultSetRendererProperty(ResultSetRenderer rsRenderer,
			String propertyName) throws WabitPersistenceException {
		if (propertyName.equals("nullString")) {
			return rsRenderer.getNullString();

		} else if (propertyName.equals("borderType")) {
			return rsRenderer.getBorderType().name();

		} else if (propertyName.equals("backgroundColour")) {
			return rsRenderer.getBackgroundColour().toString();

		} else if (propertyName.equals("headerFont")) {
			return rsRenderer.getHeaderFont().toString();
			// TODO

		} else if (propertyName.equals("bodyFont")) {
			return rsRenderer.getBodyFont().toString();
			// TODO

		} else {
			throw new WabitPersistenceException(rsRenderer.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Commits a persisted {@link ResultSetRenderer} object property
	 * 
	 * @param rsRenderer
	 *            The {@link ResultSetRenderer} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitResultSetRendererProperty(ResultSetRenderer rsRenderer,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		if (propertyName.equals("nullString")) {
			rsRenderer.setNullString(newValue.toString());

		} else if (propertyName.equals("border")) {
			rsRenderer.setBorderType(BorderStyles.valueOf(newValue.toString()));

		} else if (propertyName.equals("backgroundColour")) {
			rsRenderer.setBackgroundColour(Color.decode(newValue.toString()));

		} else if (propertyName.equals("headerFont")) {
			rsRenderer.setHeaderFont(Font.decode(newValue.toString()));

		} else if (propertyName.equals("bodyFont")) {
			rsRenderer.setBodyFont(Font.decode(newValue.toString()));

		} else {
			throw new WabitPersistenceException(rsRenderer.getUUID(),
					"Invalid property: " + propertyName);
		}
		// TODO
	}

	/**
	 * Retrieves a property value from a {@link ColumnInfo} object.
	 * 
	 * @param colInfo
	 *            The {@link ColumnInfo} object to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getColumnInfoProperty(ColumnInfo colInfo, String propertyName)
			throws WabitPersistenceException {
		String uuid = colInfo.getUUID();

		if (propertyName.equals(ColumnInfo.COLUMN_ALIAS)) {
			return colInfo.getColumnAlias();

		} else if (propertyName.equals(ColumnInfo.WIDTH_CHANGED)) {
			return colInfo.getWidth();

		} else if (propertyName.equals(ColumnInfo.HORIZONAL_ALIGNMENT_CHANGED)) {
			return colInfo.getHorizontalAlignment().name();

		} else if (propertyName.equals(ColumnInfo.DATATYPE_CHANGED)) {
			return colInfo.getDataType().name();

		} else if (propertyName.equals(ColumnInfo.WILL_GROUP_OR_BREAK_CHANGED)) {
			return colInfo.getWillGroupOrBreak().name();

		} else if (propertyName.equals(ColumnInfo.WILL_SUBTOTAL_CHANGED)) {
			return colInfo.getWillSubtotal();

		} else if (propertyName.equals(ColumnInfo.COLUMN_INFO_ITEM_CHANGED)) {
			return colInfo.getColumnInfoItem().getUUID();

		} else if (propertyName.equals(ColumnInfo.FORMAT_CHANGED)) {
			Format formatType = colInfo.getFormat();

			if (formatType instanceof SimpleDateFormat) {
				return "date-format";
			} else if (formatType instanceof DecimalFormat) {
				return "decimal-format";
			} else {
				throw new WabitPersistenceException(uuid,
						"Invalid format-type: " + formatType.toString());
			}

		} else {
			throw new WabitPersistenceException(uuid, "Invalid property: "
					+ propertyName);
		}
	}

	/**
	 * Commits a persisted {@link ColumnInfo} object property
	 * 
	 * @param colInfo
	 *            The {@link ColumnInfo} object to commit the persisted property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitColumnInfoProperty(ColumnInfo colInfo,
			String propertyName, Object newValue)
			throws WabitPersistenceException {
		String uuid = colInfo.getUUID();
		if (propertyName.equals(ColumnInfo.COLUMN_ALIAS)) {
			colInfo.setColumnAlias(newValue.toString());

		} else if (propertyName.equals(ColumnInfo.WIDTH_CHANGED)) {
			colInfo.setWidth(Integer.valueOf(newValue.toString()));

		} else if (propertyName.equals(ColumnInfo.HORIZONAL_ALIGNMENT_CHANGED)) {
			colInfo.setHorizontalAlignment(HorizontalAlignment.valueOf(newValue
					.toString()));

		} else if (propertyName.equals(ColumnInfo.DATATYPE_CHANGED)) {
			colInfo.setDataType(ca.sqlpower.wabit.report.DataType
					.valueOf(newValue.toString()));

		} else if (propertyName.equals(ColumnInfo.WILL_GROUP_OR_BREAK_CHANGED)) {
			colInfo.setWillGroupOrBreak(GroupAndBreak.valueOf(newValue
					.toString()));

		} else if (propertyName.equals(ColumnInfo.WILL_SUBTOTAL_CHANGED)) {
			colInfo.setWillSubtotal(Boolean.valueOf(newValue.toString()));

		} else if (propertyName.equals(ColumnInfo.COLUMN_INFO_ITEM_CHANGED)) {
			boolean found = false;
			for (Item queryItem : ((ResultSetRenderer) colInfo.getParent())
					.getQuery().getSelectedColumns()) {
				Item item = queryItem;
				if (item.getUUID().equals(newValue.toString())) {
					colInfo.setColumnInfoItem(item);
					found = true;
					break;
				}
			}
			if (!found) {
				throw new WabitPersistenceException(uuid,
						"Could not find QueryItem with uuid "
								+ newValue.toString());
			}

		} else if (propertyName.equals(ColumnInfo.FORMAT_CHANGED)) {
			Format formatType = colInfo.getFormat();
			String newFormatType = newValue.toString();
			String pattern = "";

			if (formatType instanceof SimpleDateFormat) {
				pattern = ((SimpleDateFormat) formatType).toPattern();
			} else if (formatType instanceof DecimalFormat) {
				pattern = ((DecimalFormat) formatType).toPattern();
			} else {
				throw new WabitPersistenceException(uuid,
						"Invalid format-type: " + formatType.toString());
			}

			if (newFormatType.equals("date-format")) {
				colInfo.setFormat(new SimpleDateFormat(pattern));
			} else if (newFormatType.equals("decimal-format")) {
				colInfo.setFormat(new DecimalFormat(pattern));
			} else {
				throw new WabitPersistenceException(uuid,
						"Invalid format-type: " + formatType.toString());
			}

		} else {
			throw new WabitPersistenceException(uuid, "Invalid property: "
					+ propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link Guide} object.
	 * 
	 * @param guide
	 *            The {@link Guide} object to retrieve the property from
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 * @throws WabitPersistenceException
	 */
	private Object getGuideProperty(Guide guide, String propertyName)
			throws WabitPersistenceException {
		if (propertyName.equals("offset")) {
			return guide.getOffset();

		} else {
			throw new WabitPersistenceException(guide.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Commits a persisted {@link Guide} object property
	 * 
	 * @param guide
	 *            The {@link Guide} object to commit the persisted property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws WabitPersistenceException
	 */
	private void commitGuideProperty(Guide guide, String propertyName,
			Object newValue) throws WabitPersistenceException {
		if (propertyName.equals("offset")) {
			guide.setOffset(Double.valueOf(newValue.toString()));

		} else {
			throw new WabitPersistenceException(guide.getUUID(),
					"Invalid property: " + propertyName);
		}
	}

	/**
	 * Removes {@link WabitObject}s from persistent storage.
	 * 
	 * @param parentUUID
	 *            The parent UUID of the {@link WabitObject} to remove
	 * @param uuid
	 *            The UUID of the {@link WabitObject} to remove
	 * @throws WabitPersistenceException
	 */
	public void removeObject(String parentUUID, String uuid)
			throws WabitPersistenceException {

		if (!exists(uuid)) {
			throw new WabitPersistenceException(uuid,
					"Cannot remove a non-existent WabitObject.");
		}

		objectsToRemove.put(uuid, parentUUID);

		if (transactionCount == 0) {
			commitRemovals();
		}
	}

	/**
	 * Rollback all changes to persistent storage to the beginning of the
	 * transaction
	 * 
	 * @throws WabitPersistenceException
	 */
	public void rollback() throws WabitPersistenceException {
		// TODO Auto-generated method stub
		if (transactionCount <= 0) {
			throw new WabitPersistenceException(null,
					"Cannot rollback while not in a transaction.");
		}
	}

	/**
	 * Accessor for the {@link WabitSession} object.
	 * 
	 * @return The {@link WabitSession} object this class refers to
	 */
	public WabitSession getWabitSession() {
		return session;
	}

	/**
	 * Accessor for the target {@link WabitPersister} object.
	 * 
	 * @return The {@link WabitPersister} object this class targets
	 */
	public WabitPersister getTargetPersister() {
		return target;
	}

	/**
	 * Mutator for the {@link WabitSession} object.
	 * 
	 * @param session
	 *            The {@link WabitSession} object to make this class refer to
	 */
	public void setWabitSession(WabitSession session) {
		this.session = session;
	}

	/**
	 * Mutator for the target {@link WabitPersister} object.
	 * 
	 * @param target
	 *            The {@link WabitPersister} object to make this class target
	 */
	public void setTargetPersister(WabitPersister target) {
		this.target = target;
	}

	/**
	 * An implementation of {@link WabitListener} used exclusively for listening
	 * to a {@link WabitWorkspace} and its children
	 */
	private class WabitWorkspaceListener implements WabitListener {

		public void transactionEnded(TransactionEvent e) {
			try {
				target.commit();
			} catch (WabitPersistenceException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		public void transactionRollback(TransactionEvent e) {
			try {
				target.rollback();
			} catch (WabitPersistenceException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		public void transactionStarted(TransactionEvent e) {
			try {
				target.begin();
			} catch (WabitPersistenceException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		public void wabitChildAdded(WabitChildEvent e) {
			try {
				String parentUUID = e.getSource().getUUID();
				WabitObject child = e.getChild();
				String className = e.getChildType().getSimpleName();
				String uuid = e.getChild().getUUID();

				target.persistObject(parentUUID, className, uuid, e.getIndex());

				// Persist any properties required for WabitObject constructor
				if (child instanceof CellSetRenderer) {
					target.persistProperty(uuid, "modifiedOlapQuery",
							DataType.REFERENCE, ((CellSetRenderer) child)
									.getOlapQuery().getUUID());

				} else if (child instanceof ChartColumn) {
					ChartColumn chartColumn = (ChartColumn) child;

					target.persistProperty(uuid, "name", DataType.STRING,
							chartColumn.getName());
					target.persistProperty(uuid, "dataType", DataType.STRING,
							chartColumn.getDataType().name());

				} else if (child instanceof ChartRenderer) {
					target.persistProperty(uuid, "chart", DataType.REFERENCE,
							((ChartRenderer) child).getChart().getUUID());

				} else if (child instanceof Guide) {
					Guide guide = (Guide) child;

					target.persistProperty(uuid, "axis", DataType.STRING, guide
							.getAxis().name());
					target.persistProperty(uuid, "offset", DataType.DOUBLE,
							guide.getOffset());

				} else if (child instanceof Layout) {
					target.persistProperty(uuid, "name", DataType.STRING, child
							.getName());

				} else if (child instanceof OlapQuery) {
					OlapQuery olapQuery = (OlapQuery) child;

					target.persistProperty(uuid, "queryName", DataType.STRING,
							olapQuery.getQueryName());
					target.persistProperty(uuid, "catalogName",
							DataType.STRING, olapQuery.getCatalogName());
					target.persistProperty(uuid, "schemaName", DataType.STRING,
							olapQuery.getSchemaName());
					target.persistProperty(uuid, "cubeName", DataType.STRING,
							olapQuery.getCubeName());

				} else if (child instanceof Page) {
					Page page = (Page) child;

					target.persistProperty(uuid, "name", DataType.STRING, page
							.getName());
					target.persistProperty(uuid, "width", DataType.INTEGER,
							page.getWidth());
					target.persistProperty(uuid, "height", DataType.INTEGER,
							page.getHeight());
					target.persistProperty(uuid, "orientation",
							DataType.STRING, page.getOrientation().name());

				} else if (child instanceof WabitColumnItem) {
					target.persistProperty(uuid, "name", DataType.STRING, child
							.getName());

				} else if (child instanceof WabitConstantItem) {
					target.persistProperty(uuid, "name", DataType.STRING, child
							.getName());

				} else if (child instanceof WabitDataSource) {
					target.persistProperty(uuid, "name", DataType.STRING, child
							.getName());

				} else if (child instanceof WabitJoin) {
					SQLJoin sqlJoin = ((WabitJoin) child).getDelegate();

					target.persistProperty(uuid, SQLJoin.LEFT_JOIN_CHANGED,
							DataType.REFERENCE, sqlJoin.getLeftColumn()
									.getUUID());
					target.persistProperty(uuid, SQLJoin.RIGHT_JOIN_CHANGED,
							DataType.REFERENCE, sqlJoin.getRightColumn());

				} else if (child instanceof WabitOlapAxis) {
					target.persistProperty(uuid, "ordinal", DataType.INTEGER,
							((WabitOlapAxis) child).getOrdinal().axisOrdinal());

				} else if (child instanceof WabitOlapDimension) {
					target.persistProperty(uuid, "name", DataType.STRING, child
							.getName());

				} else if (child instanceof WabitOlapSelection) {
					WabitOlapSelection wabitOlapSelection = (WabitOlapSelection) child;

					target.persistProperty(uuid, "operator", DataType.STRING,
							wabitOlapSelection.getOperator().name());
					target.persistProperty(uuid, "uniqueMemberName",
							DataType.STRING, wabitOlapSelection
									.getUniqueMemberName());

				} else if (child instanceof WabitTableContainer) {
					WabitTableContainer wabitTableContainer = (WabitTableContainer) child;
					TableContainer tableContainer = (TableContainer) wabitTableContainer
							.getDelegate();

					persistProperty(uuid, "name", DataType.STRING,
							wabitTableContainer.getName());
					persistProperty(uuid, "schema", DataType.STRING,
							tableContainer.getSchema());
					persistProperty(uuid, "catalog", DataType.STRING,
							tableContainer.getCatalog());

				}

				System.out.println("wabitChildAdded. type: "
						+ e.getChildType().getSimpleName() + ". source: "
						+ e.getSource().getClass().getSimpleName());

			} catch (WabitPersistenceException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		public void wabitChildRemoved(WabitChildEvent e) {
			try {
				target.removeObject(e.getSource().getUUID(), e.getChild()
						.getUUID());
			} catch (WabitPersistenceException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		public void propertyChange(PropertyChangeEvent evt) {
			WabitObject source = (WabitObject) evt.getSource();
			String uuid = source.getUUID();
			String propertyName = evt.getPropertyName();
			Object oldValue = evt.getOldValue();
			Object newValue = evt.getNewValue();

			System.out.println("propertyChange. "
					+ source.getClass().getSimpleName() + ". " + propertyName);

			try {
				if (newValue instanceof WabitObject) {
					String newValueUUID = ((WabitObject) newValue).getUUID();

					if (oldValue == null) {
						target.persistProperty(uuid, propertyName,
								DataType.REFERENCE, oldValue, newValueUUID);
					} else {
						target.persistProperty(uuid, propertyName,
								DataType.REFERENCE, ((WabitObject) oldValue)
										.getUUID(), newValueUUID);
					}
				} else {
					target.persistProperty(uuid, propertyName,
							WabitPersister.DataType.getTypeByClass(newValue
									.getClass()), oldValue, newValue);
				}
			} catch (WabitPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
