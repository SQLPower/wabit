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
import java.awt.Image;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Vector;

import com.google.common.collect.Multimap;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.wabit.ObjectDependentException;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
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

/**
 * This class represents a Data Access Object for {@link WabitSession}s.
 */
public class WabitSessionDAO implements WabitPersister {

	private WabitSession session;

	private boolean transactionBegun;

	private Multimap<String, WabitObjectProperty> persistedProperties;

	private Vector<PersistedWabitObject> persistedObjects;

	private Map<String, String> objectsToRemove;

	/**
	 * A class representing an individual persisted {@link WabitObject}
	 * property.
	 */
	private class WabitObjectProperty {

		final private String propertyName;
		final private DataType dataType;
		final private Object oldValue;
		final private Object newValue;
		final private boolean unconditional;

		/**
		 * Constructor to persist a {@link WabitObject} property, keeping track
		 * of all the parameters of the persistProperty(...) method call. These
		 * fields will be necessary for when commit() is called.
		 * 
		 * @param propertyName
		 *            The name of the property to persist
		 * @param dataType
		 *            The data type representation of the property value
		 * @param oldValue
		 *            The expected current property value
		 * @param newValue
		 *            The property value to persist
		 * @param unconditional
		 *            Whether or not to validate if oldValue matches the actual
		 *            property value before persisting
		 */
		private WabitObjectProperty(String propertyName, DataType dataType,
				Object oldValue, Object newValue, boolean unconditional) {
			this.propertyName = propertyName;
			this.dataType = dataType;
			this.oldValue = oldValue;
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
		 * Accessor for the data type field
		 * 
		 * @return The data type representation of the property value
		 */
		public DataType getDataType() {
			return dataType;
		}

		/**
		 * Accessor for the expected current property value
		 * 
		 * @return The expected current property value
		 */
		public Object getOldValue() {
			return oldValue;
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
		public String getUuid() {
			return uuid;
		}

	}

	/**
	 * Constructor to set the {@link WabitSession} this DAO should work under
	 * 
	 * @param session
	 *            The {@link WabitSession} this DAO should work under
	 */
	public WabitSessionDAO(WabitSession session) {
		this.session = session;
		transactionBegun = false;
	}

	/**
	 * Begins a transaction
	 */
	public void begin() {
		transactionBegun = true;
	}

	/**
	 * Commits the persisted {@link WabitObject}s, its properties and removals
	 */
	public void commit() throws WabitPersistenceException {
		commitObjects();
		commitProperties();
		commitRemovals();
	}

	/**
	 * Commits the persisted {@link WabitObject}s
	 * 
	 * @throws WabitPersistenceException
	 */
	private void commitObjects() throws WabitPersistenceException {

		for (PersistedWabitObject pwo : persistedObjects) {
			String uuid = pwo.getUuid();
			String type = pwo.getType();
			WabitWorkspace workspace = session.getWorkspace();
			WabitObject wo = null;
			WabitObject parent = workspace.findByUuid(pwo.getParentUUID(),
					WabitObject.class);

			if (type.equals(CellSetRenderer.class.toString())) {
				wo = new CellSetRenderer((OlapQuery) parent); // XXX this may be
				// incorrect
			}
			if (type.equals(Chart.class.toString())) {
				wo = new Chart();
				workspace.addChart((Chart) wo);

			} else if (type.equals(ChartColumn.class.toString())) {
				String columnName = getProperty(uuid, "name", true).toString();
				ca.sqlpower.wabit.report.chart.ChartColumn.DataType dataType = (ca.sqlpower.wabit.report.chart.ChartColumn.DataType) getProperty(
						uuid, "data-type", true);

				wo = new ChartColumn(columnName, dataType);

			} else if (type.equals(ChartRenderer.class.toString())) {
				wo = new ChartRenderer((Chart) parent); // XXX this may be
				// incorrect

			} else if (type.equals(ContentBox.class.toString())) {
				wo = new ContentBox();
				// TODO

			} else if (type.equals(Guide.class.toString())) {
				Axis axis = Axis.valueOf(getProperty(uuid, "axis", true)
						.toString());
				double offset = Double
						.valueOf(getProperty(uuid, "offset", true).toString());

				wo = new Guide(axis, offset);

			} else if (type.equals(ImageRenderer.class.toString())) {
				wo = new ImageRenderer();
			} else if (type.equals(Label.class.toString())) {
				wo = new Label();
			} else if (type.equals(OlapQuery.class.toString())) {
				wo = new OlapQuery(session.getContext());
				workspace.addOlapQuery((OlapQuery) wo);

			} else if (type.equals(QueryCache.class.toString())) {
				wo = new QueryCache(session.getContext());
				workspace.addQuery((QueryCache) wo, session);

			} else if (type.equals(Page.class.toString())) {
				// TODO
				String name = getProperty(uuid, "name", true).toString();
				int width = Integer.valueOf(getProperty(uuid, "width", true)
						.toString());
				int height = Integer.valueOf(getProperty(uuid, "height", true)
						.toString());
				PageOrientation orientation = PageOrientation
						.valueOf(getProperty(uuid, "orientation", true)
								.toString());
				wo = new Page(name, width, height, orientation);

			} else if (type.equals(Report.class.toString())) {
				String name = getProperty(uuid, "name", true).toString();

				wo = new Report(name);
				workspace.addReport((Report) wo);

			} else if (type.equals(Template.class.toString())) {
				String name = getProperty(uuid, "name", true).toString();

				wo = new Template(name);
				workspace.addTemplate((Template) wo);

			} else if (type.equals(WabitDataSource.class.toString())) {
				SPDataSource spds = session.getContext().getDataSources()
						.getDataSource(
								getProperty(uuid, "name", true).toString());

				wo = new WabitDataSource(spds);
				workspace.addDataSource((WabitDataSource) wo);

			} else if (type.equals(WabitImage.class.toString())) {
				wo = new WabitImage();
				workspace.addImage((WabitImage) wo);

			} else {
				throw new WabitPersistenceException(uuid,
						"Unknown WabitObject type");
			}

			if (wo != null) {
				wo.setUUID(uuid);

				if (parent != null && parent.allowsChildren()) {
					wo.setParent(parent);
				}
			}
		}
	}

	/**
	 * Retrieves a persisted property value given by the UUID of the
	 * {@link WabitObject} and its property name. This method can also remove
	 * the persisted property if necessary.
	 * 
	 * @param uuid
	 *            The UUID of the {@link WabitObject}
	 * @param propertyName
	 *            The persisted property name
	 * @param removeAfterDiscovery
	 *            Whether or not to remove the persisted property after
	 *            discovery
	 * @return The persisted property value
	 */
	private Object getProperty(String uuid, String propertyName,
			boolean removeAfterDiscovery) {
		for (WabitObjectProperty wop : persistedProperties.get(uuid)) {
			if (wop.getPropertyName().equals(propertyName)) {
				Object value = wop.getNewValue();

				if (removeAfterDiscovery) {
					persistedProperties.remove(uuid, wop);
				}

				return value;
			}
		}

		return null;
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
		DataType dataType;
		Object oldValue, newValue;
		boolean unconditional;

		for (String uuid : persistedProperties.keySet()) {
			wo = workspace.findByUuid(uuid, WabitObject.class);

			for (WabitObjectProperty wop : persistedProperties.get(uuid)) {
				propertyName = wop.getPropertyName();
				dataType = wop.getDataType();
				oldValue = wop.getOldValue();
				newValue = wop.getNewValue();
				unconditional = wop.isUnconditional();

				if (isCommonProperty(propertyName)) {
					commitCommonProperty(wo, propertyName, oldValue, newValue,
							unconditional);
				} else if (wo instanceof CellSetRenderer) {
					commitCellSetRendererProperty((CellSetRenderer) wo,
							propertyName, oldValue, newValue, unconditional);
				} else if (wo instanceof Chart) {
					commitChartProperty((Chart) wo, propertyName, oldValue,
							newValue, unconditional);
				} else if (wo instanceof ChartColumn) {
					commitChartColumnProperty((ChartColumn) wo, propertyName,
							oldValue, newValue, unconditional);
				} else if (wo instanceof ChartRenderer) {
					commitChartRendererProperty((ChartRenderer) wo,
							propertyName, oldValue, newValue, unconditional);
				} else if (wo instanceof ColumnInfo) {
					commitColumnInfoProperty((ColumnInfo) wo, propertyName,
							oldValue, newValue, unconditional);
				} else if (wo instanceof ContentBox) {
					commitContentBoxProperty((ContentBox) wo, propertyName,
							oldValue, newValue, unconditional);
				} else if (wo instanceof Guide) {
					commitGuideProperty((Guide) wo, propertyName, oldValue,
							newValue, unconditional);
				} else if (wo instanceof ImageRenderer) {
					commitImageRendererProperty((ImageRenderer) wo,
							propertyName, oldValue, newValue, unconditional);
				} else if (wo instanceof Label) {
					commitLabelProperty((Label) wo, propertyName, oldValue,
							newValue, unconditional);
				} else if (wo instanceof Layout) {
					commitLayoutProperty((Layout) wo, propertyName, oldValue,
							newValue, unconditional);
				} else if (wo instanceof OlapQuery) {
					commitOlapQueryProperty((OlapQuery) wo, propertyName,
							oldValue, newValue, unconditional);
				} else if (wo instanceof Page) {
					commitPageProperty((Page) wo, propertyName, oldValue,
							newValue, unconditional);
				} else if (wo instanceof QueryCache) {
					commitQueryCacheProperty((QueryCache) wo, propertyName,
							oldValue, newValue, unconditional);
				} else if (wo instanceof ResultSetRenderer) {
					commitResultSetRendererProperty((ResultSetRenderer) wo,
							propertyName, oldValue, newValue, unconditional);
				} else if (wo instanceof WabitDataSource) {
					commitWabitDataSourceProperty((WabitDataSource) wo,
							propertyName, oldValue, newValue, unconditional);
				} else if (wo instanceof WabitImage) {
					commitWabitImageProperty((WabitImage) wo, propertyName,
							oldValue, newValue, unconditional);
				}

			}

		}

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

			parent.removeDependency(wo);
			try {
				parent.removeChild(wo);
			} catch (IllegalArgumentException e) {
				throw new WabitPersistenceException(uuid,
						"Could not remove WabitObject from its parent.");
			} catch (ObjectDependentException e) {
				throw new WabitPersistenceException(uuid,
						"Could not remove WabitObject from its parent.");
			}

		}
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
	 * 
	 * @throws WabitPersistenceException
	 */
	public void persistObject(String parentUUID, String type, String uuid)
			throws WabitPersistenceException {
		if (!transactionBegun) {
			throw new WabitPersistenceException(uuid,
					"Transaction is not in progress");
		}

		persistedObjects.add(new PersistedWabitObject(parentUUID, type, uuid));

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
			ca.sqlpower.wabit.dao.WabitPersister.DataType propertyType,
			Object oldValue, Object newValue) throws WabitPersistenceException {
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
			ca.sqlpower.wabit.dao.WabitPersister.DataType propertyType,
			Object newValue) throws WabitPersistenceException {
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
			ca.sqlpower.wabit.dao.WabitPersister.DataType propertyType,
			Object oldValue, Object newValue, boolean unconditional)
			throws WabitPersistenceException {

		persistedProperties.put(uuid, new WabitObjectProperty(propertyName,
				propertyType, oldValue, newValue, unconditional));
	}

	/**
	 * Validates whether the expected property value and actual property value
	 * matches, only if it is a conditional persist property call. If the values
	 * do not match, a {@link WabitPersistenceException} is thrown.
	 * 
	 * @param uuid
	 *            The UUID of the {@link WabitObject} the property value is
	 *            being persisted upon
	 * @param expectedValue
	 *            The expected old property value
	 * @param actualValue
	 *            The actual property value
	 * @param unconditional
	 *            Whether or not this is a conditional persist property call
	 * @throws WabitPersistenceException
	 */
	private void validatePropertyValuesIfConditional(String uuid,
			Object expectedValue, Object actualValue, boolean unconditional)
			throws WabitPersistenceException {
		if (!unconditional && expectedValue != actualValue) {
			throw new WabitPersistenceException(uuid, "Expected value is "
					+ expectedValue + "; actual value is " + actualValue);
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

	private void commitCommonProperty(WabitObject wo, String propertyName,
			Object oldValue, Object newValue, boolean unconditional)
			throws WabitPersistenceException {
		String uuid = wo.getUUID();

		if (propertyName.equals("name")) {
			validatePropertyValuesIfConditional(uuid, oldValue, wo.getName(),
					unconditional);
			wo.setName(newValue.toString());
		} else if (propertyName.equals("uuid")) {
			validatePropertyValuesIfConditional(uuid, oldValue, uuid,
					unconditional);
			wo.setUUID(newValue.toString());
		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
					+ propertyName);
		}
	}

	/**
	 * Commits a persisted {@link WabitDataSource} property. Currently,
	 * properties cannot be persisted for this class.
	 * 
	 * @param wds
	 *            The {@link WabitDataSource} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitWabitDataSourceProperty(WabitDataSource wds,
			String propertyName, Object oldValue, Object newValue,
			boolean unconditional) throws WabitPersistenceException {
		throw new WabitPersistenceException(wds.getUUID(), "Unknown property: "
				+ propertyName);
	}

	/**
	 * Commits a persisted {@link QueryCache} property
	 * 
	 * @param query
	 *            The {@link QueryCache} object to commit the persisted property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitQueryCacheProperty(QueryCache query,
			String propertyName, Object oldValue, Object newValue,
			boolean unconditional) throws WabitPersistenceException {
		String uuid = query.getUUID();

		if (propertyName.equals("zoom")) {
			validatePropertyValuesIfConditional(uuid, oldValue, query
					.getZoomLevel(), unconditional);
			query.setZoomLevel(Integer.valueOf(newValue.toString()));

		} else if (propertyName.equals("streaming-row-limit")) {
			validatePropertyValuesIfConditional(uuid, oldValue, query
					.getStreamingRowLimit(), unconditional);
			query.setStreamingRowLimit(Integer.valueOf(newValue.toString()));

		} else if (propertyName.equals("row-limit")) {
			validatePropertyValuesIfConditional(uuid, oldValue, query
					.getRowLimit(), unconditional);
			query.setRowLimit(Integer.valueOf(newValue.toString()));

		} else if (propertyName.equals("grouping-enabled")) {
			validatePropertyValuesIfConditional(uuid, oldValue, query
					.isGroupingEnabled(), unconditional);
			query.setGroupingEnabled(Boolean.valueOf(newValue.toString()));

		} else if (propertyName.equals("prompt-for-cross-joins")) {
			validatePropertyValuesIfConditional(uuid, oldValue, query
					.getPromptForCrossJoins(), unconditional);
			query.setPromptForCrossJoins(Boolean.valueOf(newValue.toString()));

		} else if (propertyName.equals("automatically-executing")) {
			validatePropertyValuesIfConditional(uuid, oldValue, query
					.isAutomaticallyExecuting(), unconditional);
			query.setAutomaticallyExecuting(Boolean
					.valueOf(newValue.toString()));

		} else if (propertyName.equals("global-where")) {
			validatePropertyValuesIfConditional(uuid, oldValue, query
					.getGlobalWhereClause(), unconditional);
			query.setGlobalWhereClause(newValue.toString());

		} else if (propertyName.equals("query-text")) {
			validatePropertyValuesIfConditional(uuid, oldValue, query
					.generateQuery(), unconditional);
			query.defineUserModifiedQuery(newValue.toString());

		} else if (propertyName.equals("execute-queries-with-cross-joins")) {
			validatePropertyValuesIfConditional(uuid, oldValue, query
					.getExecuteQueriesWithCrossJoins(), unconditional);
			query.setExecuteQueriesWithCrossJoins(Boolean.valueOf(newValue
					.toString()));

		} else if (propertyName.equals("data-source")) {
			validatePropertyValuesIfConditional(uuid, oldValue, query
					.getWabitDataSource(), unconditional);

			query.setDataSource(session.getWorkspace().getDataSource(
					newValue.toString(), JDBCDataSource.class));

		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
					+ propertyName);
		}
	}

	/**
	 * Commits a persisted {@link OlapQuery} object property
	 * 
	 * @param olapQuery
	 *            The {@link OlapQuery} object to commit the persisted property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitOlapQueryProperty(OlapQuery olapQuery,
			String propertyName, Object oldValue, Object newValue,
			boolean unconditional) throws WabitPersistenceException {
		String uuid = olapQuery.getUUID();

		if (propertyName.equals("data-source")) {
			validatePropertyValuesIfConditional(uuid, oldValue, olapQuery
					.getOlapDataSource(), unconditional);
			olapQuery.setOlapDataSource((Olap4jDataSource) newValue);
		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
					+ propertyName);
		}
		// TODO: OlapQuery attributes
	}

	/**
	 * Commits a persisted {@link Chart} object property
	 * 
	 * @param chart
	 *            The {@link Chart} object to commit the persisted property upon
	 * @param propertyName
	 *            The property name
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitChartProperty(Chart chart, String propertyName,
			Object oldValue, Object newValue, boolean unconditional)
			throws WabitPersistenceException {
		String uuid = chart.getUUID();

		if (propertyName.equals("x-axis-name")) {
			validatePropertyValuesIfConditional(uuid, oldValue, chart
					.getXaxisName(), unconditional);
			chart.setXaxisName(newValue.toString());

		} else if (propertyName.equals("y-axis-name")) {
			validatePropertyValuesIfConditional(uuid, oldValue, chart
					.getYaxisName(), unconditional);
			chart.setYaxisName(newValue.toString());

		} else if (propertyName.equals("x-axis-label-rotation")) {
			validatePropertyValuesIfConditional(uuid, oldValue, chart
					.getXaxisLabelRotation(), unconditional);
			chart.setXAxisLabelRotation(Double.valueOf(newValue.toString()));

		} else if (propertyName.equals("gratuitous-animated")) {
			validatePropertyValuesIfConditional(uuid, oldValue, chart
					.isGratuitouslyAnimated(), unconditional);
			chart.setGratuitouslyAnimated(Boolean.valueOf(newValue.toString()));

		} else if (propertyName.equals("type")) {
			validatePropertyValuesIfConditional(uuid, oldValue,
					chart.getType(), unconditional);
			chart.setType((ChartType) newValue);

		} else if (propertyName.equals("legend-position")) {
			validatePropertyValuesIfConditional(uuid, oldValue, chart
					.getLegendPosition(), unconditional);
			chart.setLegendPosition((LegendPosition) newValue);

		} else if (propertyName.equals("query-id")) {
			validatePropertyValuesIfConditional(uuid, oldValue, chart
					.getQuery().getUUID(), unconditional);

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
			throw new WabitPersistenceException(uuid, "Unknown property: "
					+ propertyName);
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
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitChartColumnProperty(ChartColumn chartColumn,
			String propertyName, Object oldValue, Object newValue,
			boolean unconditional) throws WabitPersistenceException {
		String uuid = chartColumn.getUUID();

		if (propertyName.equals("role")) {
			validatePropertyValuesIfConditional(uuid, oldValue, chartColumn
					.getRoleInChart(), unconditional);
			chartColumn.setRoleInChart((ColumnRole) newValue);
		} else if (propertyName.equals("x-axis-name")) {
			validatePropertyValuesIfConditional(uuid, oldValue, chartColumn
					.getXAxisIdentifier(), unconditional);
			chartColumn.setXAxisIdentifier((ChartColumn) newValue);
		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
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
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitWabitImageProperty(WabitImage wabitImage,
			String propertyName, Object oldValue, Object newValue,
			boolean unconditional) throws WabitPersistenceException {
		String uuid = wabitImage.getUUID();

		if (propertyName.equals("image")) {
			validatePropertyValuesIfConditional(uuid, oldValue, wabitImage
					.getImage(), unconditional);
			wabitImage.setImage((Image) newValue);

		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
					+ propertyName);
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
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitLayoutProperty(Layout layout, String propertyName,
			Object oldValue, Object newValue, boolean unconditional)
			throws WabitPersistenceException {
		String uuid = layout.getUUID();

		if (propertyName.equals("zoom")) {
			validatePropertyValuesIfConditional(uuid, oldValue, layout
					.getZoomLevel(), unconditional);
			layout.setZoomLevel(Integer.valueOf(newValue.toString()));

		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
					+ propertyName);
		}
	}

	/**
	 * Commits a persisted {@link Page} object property
	 * 
	 * @param page
	 *            The {@link Page} object to commit the persisted property upon
	 * @param propertyName
	 *            The property name
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitPageProperty(Page page, String propertyName,
			Object oldValue, Object newValue, boolean unconditional)
			throws WabitPersistenceException {
		String uuid = page.getUUID();

		if (propertyName.equals("height")) {
			validatePropertyValuesIfConditional(uuid, oldValue, page
					.getHeight(), unconditional);
			page.setHeight(Integer.parseInt(newValue.toString()));

		} else if (propertyName.equals("width")) {
			validatePropertyValuesIfConditional(uuid, oldValue,
					page.getWidth(), unconditional);
			page.setWidth(Integer.parseInt(newValue.toString()));

		} else if (propertyName.equals("orientation")) {
			validatePropertyValuesIfConditional(uuid, oldValue, page
					.getOrientation(), unconditional);
			page.setOrientation(PageOrientation.valueOf(newValue.toString()));

		} else if (propertyName.equals("default-font")) {
			validatePropertyValuesIfConditional(uuid, oldValue, page
					.getDefaultFont(), unconditional);
			page.setDefaultFont((Font) newValue);

		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
					+ propertyName);
		}
	}

	/**
	 * Commits a persisted {@link ContentBox} object property
	 * 
	 * @param contentBox
	 *            The {@link ContentBox} object to commit the persisted property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitContentBoxProperty(ContentBox contentBox,
			String propertyName, Object oldValue, Object newValue,
			boolean unconditional) throws WabitPersistenceException {
		String uuid = contentBox.getUUID();

		if (propertyName.equals("height")) {
			validatePropertyValuesIfConditional(uuid, oldValue, contentBox
					.getHeight(), unconditional);
			contentBox.setHeight(Double.valueOf(newValue.toString()));

		} else if (propertyName.equals("width")) {
			validatePropertyValuesIfConditional(uuid, oldValue, contentBox
					.getWidth(), unconditional);
			contentBox.setWidth(Double.valueOf(newValue.toString()));

		} else if (propertyName.equals("xpos")) {
			validatePropertyValuesIfConditional(uuid, oldValue, contentBox
					.getX(), unconditional);
			contentBox.setX(Double.valueOf(newValue.toString()));

		} else if (propertyName.equals("ypos")) {
			validatePropertyValuesIfConditional(uuid, oldValue, contentBox
					.getY(), unconditional);
			contentBox.setY(Double.valueOf(newValue.toString()));

		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
					+ propertyName);
		}
	}

	/**
	 * Commits a persisted {@link ChartRenderer} object property
	 * 
	 * @param cRenderer
	 *            The {@link ChartRenderer} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitChartRendererProperty(ChartRenderer cRenderer,
			String propertyName, Object oldValue, Object newValue,
			boolean unconditional) throws WabitPersistenceException {
		throw new WabitPersistenceException(cRenderer.getUUID(),
				"Unknown property: " + propertyName);
	}

	/**
	 * Commits a persisted {@link CellSetRenderer} object property
	 * 
	 * @param csRenderer
	 *            The {@link CellSetRenderer} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitCellSetRendererProperty(CellSetRenderer csRenderer,
			String propertyName, Object oldValue, Object newValue,
			boolean unconditional) throws WabitPersistenceException {
		String uuid = csRenderer.getUUID();

		if (propertyName.equals("olap-query-uuid")) {
			validatePropertyValuesIfConditional(uuid, oldValue, csRenderer
					.getModifiedOlapQuery(), unconditional);
			csRenderer.setModifiedOlapQuery((OlapQuery) newValue);

		} else if (propertyName.equals("body-alignment")) {
			validatePropertyValuesIfConditional(uuid, oldValue, csRenderer
					.getBodyAlignment(), unconditional);
			csRenderer.setBodyAlignment(HorizontalAlignment.valueOf(newValue
					.toString()));

		} else if (propertyName.equals("body-format-pattern")) {
			validatePropertyValuesIfConditional(uuid, oldValue, csRenderer
					.getBodyFormat(), unconditional);
			csRenderer.setBodyFormat((DecimalFormat) newValue);

		} else if (propertyName.equals("olap-header-font")) {
			validatePropertyValuesIfConditional(uuid, oldValue, csRenderer
					.getHeaderFont(), unconditional);
			csRenderer.setHeaderFont((Font) newValue);

		} else if (propertyName.equals("olap-body-font")) {
			validatePropertyValuesIfConditional(uuid, oldValue, csRenderer
					.getBodyFont(), unconditional);
			csRenderer.setBodyFont((Font) newValue);

		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
					+ propertyName);
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
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitImageRendererProperty(ImageRenderer iRenderer,
			String propertyName, Object oldValue, Object newValue,
			boolean unconditional) throws WabitPersistenceException {
		String uuid = iRenderer.getUUID();

		if (propertyName.equals("wabit-image-uuid")) {
			validatePropertyValuesIfConditional(uuid, oldValue, iRenderer
					.getImage().getUUID(), unconditional);
			iRenderer.setImage(session.getWorkspace().findByUuid(
					newValue.toString(), WabitImage.class));

		} else if (propertyName.equals("preserving-aspect-ratio")) {
			validatePropertyValuesIfConditional(uuid, oldValue, iRenderer
					.isPreservingAspectRatio(), unconditional);
			iRenderer.setPreservingAspectRatio(Boolean.valueOf(newValue
					.toString()));

		} else if (propertyName.equals("h-align")) {
			validatePropertyValuesIfConditional(uuid, oldValue, iRenderer
					.getHAlign(), unconditional);
			iRenderer.setHAlign(HorizontalAlignment
					.valueOf(newValue.toString()));

		} else if (propertyName.equals("v-align")) {
			validatePropertyValuesIfConditional(uuid, oldValue, iRenderer
					.getVAlign(), unconditional);
			iRenderer.setVAlign(VerticalAlignment.valueOf(newValue.toString()));

		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
					+ propertyName);
		}
	}

	/**
	 * Commits a persisted {@link Label} object property
	 * 
	 * @param label
	 *            The {@link Label} object to commit the persisted property upon
	 * @param propertyName
	 *            The property name
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitLabelProperty(Label label, String propertyName,
			Object oldValue, Object newValue, boolean unconditional)
			throws WabitPersistenceException {
		String uuid = label.getUUID();

		if (propertyName.equals("horizontal-align")) {
			validatePropertyValuesIfConditional(uuid, oldValue, label
					.getHorizontalAlignment(), unconditional);
			label.setHorizontalAlignment(HorizontalAlignment.valueOf(newValue
					.toString()));

		} else if (propertyName.equals("vertical-align")) {
			validatePropertyValuesIfConditional(uuid, oldValue, label
					.getVerticalAlignment(), unconditional);
			label.setVerticalAlignment(VerticalAlignment.valueOf(newValue
					.toString()));

		} else if (propertyName.equals("text")) {
			validatePropertyValuesIfConditional(uuid, oldValue,
					label.getText(), unconditional);
			label.setText(newValue.toString());

		} else if (propertyName.equals("bg-colour")) {
			validatePropertyValuesIfConditional(uuid, oldValue, label
					.getBackgroundColour(), unconditional);
			label.setBackgroundColour((Color) newValue);
		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
					+ propertyName);
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
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitResultSetRendererProperty(ResultSetRenderer rsRenderer,
			String propertyName, Object oldValue, Object newValue,
			boolean unconditional) throws WabitPersistenceException {
		String uuid = rsRenderer.getUUID();

		if (propertyName.equals("null-string")) {
			validatePropertyValuesIfConditional(uuid, oldValue, rsRenderer
					.getNullString(), unconditional);
			rsRenderer.setNullString(newValue.toString());

		} else if (propertyName.equals("border")) {
			validatePropertyValuesIfConditional(uuid, oldValue, rsRenderer
					.getBorderType(), unconditional);
			rsRenderer.setBorderType(BorderStyles.valueOf(newValue.toString()));

		} else if (propertyName.equals("bg-colour")) {
			validatePropertyValuesIfConditional(uuid, oldValue, rsRenderer
					.getBackgroundColour(), unconditional);
			rsRenderer.setBackgroundColour((Color) newValue);

		} else if (propertyName.equals("header-font")) {
			validatePropertyValuesIfConditional(uuid, oldValue, rsRenderer
					.getHeaderFont(), unconditional);
			rsRenderer.setHeaderFont((Font) newValue);

		} else if (propertyName.equals("body-font")) {
			validatePropertyValuesIfConditional(uuid, oldValue, rsRenderer
					.getBodyFont(), unconditional);
			rsRenderer.setBodyFont((Font) newValue);

		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
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
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitColumnInfoProperty(ColumnInfo colInfo,
			String propertyName, Object oldValue, Object newValue,
			boolean unconditional) throws WabitPersistenceException {
		String uuid = colInfo.getUUID();

		if (propertyName.equals("column-alias")) {
			validatePropertyValuesIfConditional(uuid, oldValue, colInfo
					.getColumnAlias(), unconditional);
			colInfo.setColumnAlias(newValue.toString());

		} else if (propertyName.equals("width")) {
			validatePropertyValuesIfConditional(uuid, oldValue, colInfo
					.getWidth(), unconditional);
			colInfo.setWidth(Integer.valueOf(newValue.toString()));

		} else if (propertyName.equals("horizontal-align")) {
			validatePropertyValuesIfConditional(uuid, oldValue, colInfo
					.getHorizontalAlignment(), unconditional);
			colInfo.setHorizontalAlignment(HorizontalAlignment.valueOf(newValue
					.toString()));

		} else if (propertyName.equals("data-type")) {
			validatePropertyValuesIfConditional(uuid, oldValue, colInfo
					.getDataType(), unconditional);
			colInfo.setDataType(ca.sqlpower.wabit.report.DataType
					.valueOf(newValue.toString()));

		} else if (propertyName.equals("group-or-break")) {
			validatePropertyValuesIfConditional(uuid, oldValue, colInfo
					.getWillGroupOrBreak(), unconditional);
			colInfo.setWillGroupOrBreak(GroupAndBreak.valueOf(newValue
					.toString()));

		} else if (propertyName.equals("will-subtotal")) {
			validatePropertyValuesIfConditional(uuid, oldValue, colInfo
					.getWillSubtotal(), unconditional);
			colInfo.setWillSubtotal(Boolean.valueOf(newValue.toString()));

		} else if (propertyName.equals("column-info-item-id")) {
			// TODO
		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
					+ propertyName);
		}
	}

	/**
	 * Commits a persisted {@link Guide} object property
	 * 
	 * @param guide
	 *            The {@link Guide} object to commit the persisted property upon
	 * @param propertyName
	 *            The property name
	 * @param oldValue
	 *            The expected current property value
	 * @param newValue
	 *            The persisted property value to be committed
	 * @param unconditional
	 *            Whether or not to validate oldValue against the actual
	 *            property value
	 * @throws WabitPersistenceException
	 */
	private void commitGuideProperty(Guide guide, String propertyName,
			Object oldValue, Object newValue, boolean unconditional)
			throws WabitPersistenceException {
		String uuid = guide.getUUID();

		if (propertyName.equals("offset")) {
			validatePropertyValuesIfConditional(uuid, oldValue, guide
					.getOffset(), unconditional);
			guide.setOffset(Double.valueOf(newValue.toString()));
		} else {
			throw new WabitPersistenceException(uuid, "Unknown property: "
					+ propertyName);
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
		WabitWorkspace workspace = session.getWorkspace();
		WabitObject parent = workspace
				.findByUuid(parentUUID, WabitObject.class);
		WabitObject wabitObjectToRemove = workspace.findByUuid(uuid,
				WabitObject.class);

		if (!parent.allowsChildren()
				|| !parent.getChildren().contains(wabitObjectToRemove)) {
			throw new WabitPersistenceException(uuid,
					"The specified WabitObject to remove does not exist within the parent.");
		}

		try {
			parent.removeChild(wabitObjectToRemove);
		} catch (IllegalArgumentException e) {
			throw new WabitPersistenceException(uuid, e);
		} catch (ObjectDependentException e) {
			throw new WabitPersistenceException(uuid, e);
		}
	}

	/**
	 * Rollback all changes to persistent storage to the beginning of the transaction
	 */
	public void rollback() {
		// TODO Auto-generated method stub

	}

}
