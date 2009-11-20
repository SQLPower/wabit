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
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.olap4j.metadata.Cube;
import org.olap4j.query.Selection.Operator;

import ca.sqlpower.dao.PersisterUtils;
import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersister;
import ca.sqlpower.enterprise.client.Grant;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.GroupMember;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.query.Container;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.QueryImpl;
import ca.sqlpower.query.SQLGroupFunction;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.query.SQLObjectItem;
import ca.sqlpower.query.StringItem;
import ca.sqlpower.query.TableContainer;
import ca.sqlpower.query.QueryImpl.OrderByArgument;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.dao.session.SessionPersisterSuperConverter;
import ca.sqlpower.wabit.dao.session.WorkspacePersisterListener;
import ca.sqlpower.wabit.enterprise.client.ReportTask;
import ca.sqlpower.wabit.image.WabitImage;
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
import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.rs.olap.WabitOlapAxis;
import ca.sqlpower.wabit.rs.olap.WabitOlapDimension;
import ca.sqlpower.wabit.rs.olap.WabitOlapExclusion;
import ca.sqlpower.wabit.rs.olap.WabitOlapInclusion;
import ca.sqlpower.wabit.rs.olap.WabitOlapSelection;
import ca.sqlpower.wabit.rs.query.QueryCache;
import ca.sqlpower.wabit.rs.query.WabitColumnItem;
import ca.sqlpower.wabit.rs.query.WabitConstantItem;
import ca.sqlpower.wabit.rs.query.WabitConstantsContainer;
import ca.sqlpower.wabit.rs.query.WabitItem;
import ca.sqlpower.wabit.rs.query.WabitJoin;
import ca.sqlpower.wabit.rs.query.WabitTableContainer;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * This class represents a Data Access Object for {@link WabitSession}s.
 * <p>
 * Special Case: If this persister receives a new WabitWorkspace the workspace
 * in the session this persister is attached to must be cleaned up or removed. A
 * new workspace event signals that an entire new set of objects that make up
 * the workspace as a whole is about to be sent to the session. If the objects
 * still exist in the workspace when the persister tries to create new objects
 * exceptions will be thrown as an object with the same UUID will exist in the
 * workspace.
 */
public class WabitSessionPersister implements SPPersister {

	/**
	 * The god mode means that this listener will output
	 * events that are unconditional, always. This makes it the
	 * purveyor of the truth.
	 */
	private boolean godMode = false;
	
	private static final Logger logger = Logger
			.getLogger(WabitSessionPersister.class);

	/**
	 * A {@link WabitSession} to persisted objects and properties onto.
	 */
	private WabitSession session;

	/**
	 * A count of transactions, mainly to keep track of nested transactions.
	 */
	private int transactionCount = 0;

	/**
	 * Persisted property buffer, mapping of {@link WabitObject} UUIDs to each
	 * individual persisted property
	 */
	private Multimap<String, WabitObjectProperty> persistedProperties = LinkedListMultimap.create();

	/**
	 * This will be the list we will use to rollback persisted properties
	 */
	private List<PersistedPropertiesEntry> persistedPropertiesRollbackList = new LinkedList<PersistedPropertiesEntry>();
	
	/**
	 * Persisted {@link WabitObject} buffer, contains all the data that was
	 * passed into the persistedObject call in the order of insertion
	 */
	protected List<PersistedWabitObject> persistedObjects = new LinkedList<PersistedWabitObject>();

	/**
	 * This will be the list we use to rollback persisted objects.
	 * It contains UUIDs of objects that were created.
	 */
	private List<PersistedObjectEntry> persistedObjectsRollbackList = new LinkedList<PersistedObjectEntry>();

	/**
	 * This comparator sorts buffered removeObject calls by each
	 * {@link SPObject} UUID. The UUIDs being compared must matchup with an
	 * existing SPObject in the {@link #root}. If it does not exist, it means
	 * that the SPObject has just been removed and this comparator is
	 * reshuffling the map.
	 * TODO We need a generic way of comparing
	 * {@link SPObject}s. Instead of using the WabitObjectOrder enums which
	 * currently exist in {@link WabitWorkspace}, {@link QueryCache},
	 * {@link WabitOlapDimension} and {@link Page}, we need to somehow make use
	 * of the {@link WabitObjectComparator} instead.
	 */
	protected final Comparator<String> removedObjectComparator = new Comparator<String>() {
		public int compare(String uuid1, String uuid2) {
			SPObject spo1 = SQLPowerUtils.findByUuid(root, uuid1, SPObject.class);
			SPObject spo2 = SQLPowerUtils.findByUuid(root, uuid2, SPObject.class);
			
			if (uuid1.equals(uuid2)) {
				return 0;
			} else if (spo1 == null && spo2 == null) {
				return uuid2.compareTo(uuid1);
			} else if (spo1 == null) {
				return -1;
			} else if (spo2 == null) {
				return 1;
			} else if (spo1.getParent() == null && spo2.getParent() == null) {
				return 0;
			} else if (spo1.getParent() == null) {
				return 1;
			} else if (spo2.getParent() == null) {
				return -1;
			} else if (spo1.equals(spo2)) {
				return 0;
			} else if (spo1.getParent().equals(spo2.getParent())) {
				List<? extends SPObject> siblings = spo1.getParent().getChildren();
				return Integer.signum(siblings.indexOf(spo2) - siblings.indexOf(spo1));
			}
				
			List<SPObject> ancestorList1 = SQLPowerUtils.getAncestorList(spo1);
			List<SPObject> ancestorList2 = SQLPowerUtils.getAncestorList(spo2);

			SPObject previousAncestor = null;
			SPObject ancestor1 = spo1;
			SPObject ancestor2 = spo2;
			boolean compareWithAncestor = false;

			for (int i = 0, j = 0; i < ancestorList1.size() && j < ancestorList2.size(); i++, j++) {
				ancestor1 = ancestorList1.get(i);
				ancestor2 = ancestorList2.get(j);

				if (previousAncestor != null && !ancestor1.equals(ancestor2)) {
					compareWithAncestor = true;
					break;
				}

				previousAncestor = ancestor1;

			}

			if (!compareWithAncestor) {
				if (ancestorList1.size() < ancestorList2.size()) {
					ancestor1 = spo1;
					ancestor2 = ancestorList2.get(ancestorList1.size());
				} else if (ancestorList1.size() > ancestorList2.size()) {
					ancestor1 = ancestorList1.get(ancestorList2.size());
					ancestor2 = spo2;
				} else {
					ancestor1 = spo1;
					ancestor2 = spo2;
				}
			}

			String simpleName1 = ancestor1.getClass().getSimpleName();
			String simpleName2 = ancestor2.getClass().getSimpleName();
			int c;

			if (ancestor1.equals(ancestor2)) {
				c = ancestorList2.size() - ancestorList1.size();

			} else if (ancestor1.getClass() == ancestor2.getClass()) {
				List<? extends SPObject> siblings = previousAncestor.getChildren();
				int index1 =  siblings.indexOf(ancestor1);
				int index2 = siblings.indexOf(ancestor2);

				c = index2 - index1;

			} else if (previousAncestor.equals(WabitWorkspace.class.getSimpleName())) {
				WabitWorkspace.WabitObjectOrder order1 = WabitWorkspace.WabitObjectOrder.getOrderBySimpleClassName(simpleName1);
				WabitWorkspace.WabitObjectOrder order2 = WabitWorkspace.WabitObjectOrder.getOrderBySimpleClassName(simpleName2);

				c = order2.compareTo(order1);

			} else if (previousAncestor.equals(QueryCache.class.getSimpleName())) {
				QueryCache.WabitObjectOrder order1 = QueryCache.WabitObjectOrder.getOrderBySimpleClassName(simpleName1);
				QueryCache.WabitObjectOrder order2 = QueryCache.WabitObjectOrder.getOrderBySimpleClassName(simpleName2);

				c = order2.compareTo(order1);

			} else if (previousAncestor.equals(WabitOlapDimension.class.getSimpleName())) {
				WabitOlapDimension.WabitObjectOrder order1 = WabitOlapDimension.WabitObjectOrder.getOrderBySimpleClassName(simpleName1);
				WabitOlapDimension.WabitObjectOrder order2 = WabitOlapDimension.WabitObjectOrder.getOrderBySimpleClassName(simpleName2);

				c = order2.compareTo(order1);

			} else if (previousAncestor.equals(Page.class.getSimpleName())) {
				Page.WabitObjectOrder order1 = Page.WabitObjectOrder.getOrderBySimpleClassName(simpleName1);
				Page.WabitObjectOrder order2 = Page.WabitObjectOrder.getOrderBySimpleClassName(simpleName2);

				c = order2.compareTo(order1);

			} else {
				// XXX The comparator should really never reach
				// this else block. However in the case that it does, compare by UUID.

				c = uuid2.compareTo(uuid1);
			}

			return Integer.signum(c);
		}
	};
	
	/**
	 * {@link WabitObject} removal buffer, mapping of {@link WabitObject} UUIDs
	 * to their parents
	 */
	protected Map<String, String> objectsToRemove = new TreeMap<String, String>(removedObjectComparator);
	
	/**
	 * This is the list we use to rollback object removal
	 */
	private List<RemovedObjectEntry> objectsToRemoveRollbackList = new LinkedList<RemovedObjectEntry>();

	/**
	 * This converter will do all of the converting for this persister.
	 */
	private final SessionPersisterSuperConverter converter;

	/**
	 * This root object is used to find other objects by UUID by walking the
	 * descendant tree when an object is required.
	 */
	private final WabitObject root;

	/**
	 * Name of this persister (for debugging purposes).
	 */
	private final String name;

	private Thread currentThread;

	private boolean headingToWisconsin;

	/**
	 * Creates a session persister that can update any object at or a descendant
	 * of the given session's workspace object. If the persist call to this
	 * persister is involving an object that is not the workspace or descendant
	 * of the workspace in the given session an exception will be thrown
	 * depending on the call. See the specific method being called for more
	 * information about the exceptions that will be thrown.
	 */
	public WabitSessionPersister(String name, WabitSession session) {
		this(name, session, session.getWorkspace());
	}

	/**
	 * Creates a session persister that can update an object at or a descendant
	 * of the given root now. If the persist call involves an object that is
	 * outside of the scope of the root node and its descendant tree an
	 * exception will be thrown depending on the method called as the object
	 * will not be found.
	 */
	public WabitSessionPersister(String name, WabitSession session,
			WabitObject root) {
		this.name = name;
		this.session = session;
		this.root = root;

		converter = new SessionPersisterSuperConverter(session, root);
	}

	@Override
	public String toString() {
		return "WabitSessionPersister \"" + name + "\"";
	}

	/**
	 * Begins a transaction
	 */
	public void begin() {
		synchronized (session) {
			this.enforeThreadSafety();
			transactionCount++;
			logger.debug("wsp.begin(); - transaction count : "+transactionCount);
		}
	}

	/**
	 * Commits the persisted {@link WabitObject}s, its properties and removals
	 */
	public void commit() throws SPPersistenceException {
		synchronized (session) {
			this.enforeThreadSafety();
			logger.debug("wsp.commit(); - transaction count : "+transactionCount);
			
			final WabitWorkspace workspace = session.getWorkspace();
			synchronized (workspace) {
				try {
					workspace.setMagicDisabled(true);
					if (transactionCount == 0) {
						throw new SPPersistenceException(null,
							"Commit attempted while not in a transaction");
					}
	
					// Make sure the rollback lists are empty.
					this.objectsToRemoveRollbackList.clear();
					this.persistedObjectsRollbackList.clear();
					this.persistedPropertiesRollbackList.clear();
					
					if (transactionCount == 1) {
						logger.debug("Begin of commit phase...");
						logger.debug("Committing " + persistedObjects.size() + " new objects, " + 
								persistedProperties.size() + " changes to different property names, " +
										"and " + objectsToRemove.size() + " objects are being removed.");
						workspace.begin("Begin batch transaction...");
						commitRemovals();
						commitObjects();
						commitProperties();
						workspace.commit();
						this.objectsToRemove.clear();
						this.objectsToRemoveRollbackList.clear();
						this.persistedObjects.clear();
						this.persistedObjectsRollbackList.clear();
						this.persistedProperties.clear();
						this.persistedPropertiesRollbackList.clear();
						this.currentThread = null;
						transactionCount = 0;
						logger.debug("...commit succeeded.");
					} else {
						transactionCount--;
					}
				} catch (Throwable t) {
					logger.error("WabitSessionPersister caught an exception while performing a commit operation. Will try to rollback...", t);
					this.rollback();
					throw new SPPersistenceException(null, t);
				} finally {
					workspace.setMagicDisabled(false);
				}
			}
		}
	}

	/**
	 * Returns an ancestor list of {@link PersistedWabitObject}s from a given
	 * child PersistedWabitObject.
	 */
	private List<PersistedWabitObject> getAncestorListFromPersistedObjects(PersistedWabitObject child) {
		List<PersistedWabitObject> resultList = new ArrayList<PersistedWabitObject>();

		// Iterate through list of persisted WabitObjects to build an ancestor
		// list from objects that do not exist in the workspace yet.
		String uuid = child.getParentUUID();
		PersistedWabitObject pwo;
		while ((pwo = findPersistedObjectByUUID(uuid)) != null) {
			resultList.add(0, pwo);
			uuid = pwo.getParentUUID();
		}

		// Iterate through list of existing WabitObjects in the workspace and
		// build the rest of the ancestor list.
		SPObject spo = SQLPowerUtils.findByUuid(root, uuid, SPObject.class);
		if (spo != null) {
			resultList.add(0, createPersistedObjectFromSPObject(spo));
			List<SPObject> ancestorList = SQLPowerUtils.getAncestorList(spo);

			for (SPObject ancestor : ancestorList) {
				resultList.add(0, createPersistedObjectFromSPObject(ancestor));
			}
		}
		
		return resultList;
	}
	
	/**
	 * Returns a new {@link PersistedWabitObject} based on a given {@link SPObject}.
	 */
	private PersistedWabitObject createPersistedObjectFromSPObject(SPObject spo) {
		String parentUUID = null;
		int index = 0;
		
		if (spo.getParent() != null) {
			parentUUID = spo.getParent().getUUID();
			index = spo.getParent().getChildren(spo.getClass()).indexOf(spo);
		}
		
		return new PersistedWabitObject(parentUUID, spo.getClass().getSimpleName(), 
				spo.getUUID(), index);
	}

	/**
	 * Returns an existing {@link PersistedWabitObject} in the
	 * {@link #persistedObjects} list given by the UUID. If it does not exist,
	 * null is returned.
	 */
	private PersistedWabitObject findPersistedObjectByUUID(String uuid) {
		if (uuid != null) {
			for (PersistedWabitObject pwo : persistedObjects) {
				if (uuid.equals(pwo.getUUID())) {
					return pwo;
				}
			}
		}
		return null;
	}
	
	protected final Comparator<PersistedWabitObject> persistedObjectComparator = new Comparator<PersistedWabitObject>() {

		// If the two objects being compared are of the same type and are children of the same parent, the one with the lower index should go first.
		// Otherwise, the one with the smaller ancestor tree should go first (e.g. Report should go before Page).
		public int compare(PersistedWabitObject o1, PersistedWabitObject o2) {
			
			if (o1.getParentUUID() == null && o2.getParentUUID() == null) {
				return 0;
			} else if (o1.getParentUUID() == null) {
				return -1;
			} else if (o2.getParentUUID() == null) {
				return 1;
			} else if (o1.getParentUUID().equals(o2.getParentUUID()) && o1.getType().equals(o2.getType())) {
				return Integer.signum(o1.getIndex() - o2.getIndex());
			}
			
			List<PersistedWabitObject> ancestorList1 = getAncestorListFromPersistedObjects(o1);
			List<PersistedWabitObject> ancestorList2 = getAncestorListFromPersistedObjects(o2);
			
			PersistedWabitObject previousAncestor = null;
			PersistedWabitObject ancestor1 = null;
			PersistedWabitObject ancestor2 = null;
			boolean compareWithAncestor = false;
			
			for (int i = 0, j = 0; i < ancestorList1.size() && j < ancestorList2.size(); i++, j++) {
				ancestor1 = ancestorList1.get(i);
				ancestor2 = ancestorList2.get(j);
				
				if (previousAncestor != null && !ancestor1.equals(ancestor2)) {
					compareWithAncestor = true;
					break;
				}
				
				previousAncestor = ancestor1;
			}
			
			if (!compareWithAncestor) {
				if (ancestorList1.size() < ancestorList2.size()) {
					ancestor1 = o1;
					ancestor2 = ancestorList2.get(ancestorList1.size());
				} else if (ancestorList1.size() > ancestorList2.size()) {
					ancestor1 = ancestorList1.get(ancestorList2.size());
					ancestor2 = o2;
				} else {
					ancestor1 = o1;
					ancestor2 = o2;
				}
			}
			
			int c;
			
			if (ancestor1.equals(ancestor2)) {
				c = ancestorList1.size() - ancestorList2.size();
				
			} else if (ancestor1.getType().equals(ancestor2.getType())) {
				c = ancestor1.getIndex() - ancestor2.getIndex();
				
			} else if (previousAncestor.getType().equals(WabitWorkspace.class.getSimpleName())) {
				WabitWorkspace.WabitObjectOrder order1 = WabitWorkspace.WabitObjectOrder.getOrderBySimpleClassName(ancestor1.getType());
				WabitWorkspace.WabitObjectOrder order2 = WabitWorkspace.WabitObjectOrder.getOrderBySimpleClassName(ancestor2.getType());
				
				c = order1.compareTo(order2);
				
			} else if (previousAncestor.getType().equals(QueryCache.class.getSimpleName())) {
				QueryCache.WabitObjectOrder order1 = QueryCache.WabitObjectOrder.getOrderBySimpleClassName(ancestor1.getType());
				QueryCache.WabitObjectOrder order2 = QueryCache.WabitObjectOrder.getOrderBySimpleClassName(ancestor2.getType());
				
				c = order1.compareTo(order2);
				
			} else if (previousAncestor.getType().equals(WabitOlapDimension.class.getSimpleName())) {
				WabitOlapDimension.WabitObjectOrder order1 = WabitOlapDimension.WabitObjectOrder.getOrderBySimpleClassName(ancestor1.getType());
				WabitOlapDimension.WabitObjectOrder order2 = WabitOlapDimension.WabitObjectOrder.getOrderBySimpleClassName(ancestor2.getType());
				
				c = order1.compareTo(order2);
				
			} else if (previousAncestor.getType().equals(Page.class.getSimpleName())) {
				Page.WabitObjectOrder order1 = Page.WabitObjectOrder.getOrderBySimpleClassName(ancestor1.getType());
				Page.WabitObjectOrder order2 = Page.WabitObjectOrder.getOrderBySimpleClassName(ancestor2.getType());
				
				c = order1.compareTo(order2);
				
			} else {
				// XXX The comparator should really never reach
				// this else block. However in the case that it does, compare by UUID.
				
				c = ancestor1.getUUID().compareTo(ancestor2.getUUID());
				
			}
			
			return Integer.signum(c);
		}
	};

	/**
	 * Commits the persisted {@link WabitObject}s
	 * 
	 * @throws SPPersistenceException
	 */
	private void commitObjects() throws SPPersistenceException {
		Collections.sort(persistedObjects, persistedObjectComparator);
		
		for (PersistedWabitObject pwo : persistedObjects) {
			if (pwo.isLoaded())
				continue;
			SPObject parent = SQLPowerUtils.findByUuid(root, pwo
					.getParentUUID(), SPObject.class);
			SPObject spo = loadWabitObject(pwo);
			if (spo != null) {
				SPListener removeChildOnAddListener = new SPListener() {
					public void propertyChange(PropertyChangeEvent arg0) {
						//do nothing
					}
					public void childRemoved(SPChildEvent e) {
						objectsToRemoveRollbackList.add(
								new RemovedObjectEntry(e.getSource().getUUID(), e.getChild(), e.getIndex()));
					}
					public void childAdded(SPChildEvent e) {
						//do nothing
					}
					public void transactionStarted(TransactionEvent e) {
						//do nothing
					}
					public void transactionRollback(TransactionEvent e) {
						//do nothing
					}
					public void transactionEnded(TransactionEvent e) {
						//do nothing
					}
				};
				parent.addSPListener(removeChildOnAddListener);
				// FIXME Terrible hack, see bug 2326
				parent.addChild(spo, Math.min(pwo.getIndex(), parent.getChildren(spo.getClass()).size()));
				parent.removeSPListener(removeChildOnAddListener);
				this.persistedObjectsRollbackList.add(
					new PersistedObjectEntry(
						parent.getUUID(), 
						spo.getUUID()));
			}
		}
		persistedObjects.clear();
	}

	/**
	 * This method creates the actual new object defined by the
	 * {@link PersistedWabitObject}. Additional properties may be retrieved from
	 * the transaction to be used as constructor arguments depending on the
	 * object being created.
	 * <p>
	 * A new {@link WabitWorkspace} object signals that there is a new workspace
	 * being persisted and the current one attached to this listener must either
	 * go away or clean up. See the class level documentation for more on this
	 * special case.
	 * 
	 * @param pwo
	 *            Describes what kind of object needs to be created and where to
	 *            parent it to.
	 * @return The object created by this method.
	 * @throws SPPersistenceException
	 */
	private SPObject loadWabitObject(PersistedWabitObject pwo)
			throws SPPersistenceException {
		String uuid = pwo.getUUID();
		String type = pwo.getType();
		SPObject spo = null;

		if (type.equals(CellSetRenderer.class.getSimpleName())) {
			OlapQuery olapQuery = (OlapQuery) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "content"), OlapQuery.class);
			spo = new CellSetRenderer(olapQuery);

		} else if (type.equals(Chart.class.getSimpleName())) {
			spo = new Chart();

		} else if (type.equals(ChartColumn.class.getSimpleName())) {
			String columnName = (String) getPropertyAndRemove(uuid,
					"columnName");
			ca.sqlpower.wabit.report.chart.ChartColumn.DataType dataType = (ca.sqlpower.wabit.report.chart.ChartColumn.DataType) converter
					.convertToComplexType(
							getPropertyAndRemove(uuid, "dataType"),
							ca.sqlpower.wabit.report.chart.ChartColumn.DataType.class);

			spo = new ChartColumn(columnName, dataType);
			spo.setUUID(uuid);

		} else if (type.equals(ChartRenderer.class.getSimpleName())) {
			Chart chart = (Chart) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "content"), Chart.class);
			spo = new ChartRenderer(chart);

		} else if (type.equals(ColumnInfo.class.getSimpleName())) {

			if (containsProperty(uuid, "columnInfoItem")) {
				String label = (String) converter.convertToComplexType(
						getPropertyAndRemove(uuid, "name"),
						String.class);
				spo = new ColumnInfo((Item) converter.convertToComplexType(
						getPropertyAndRemove(uuid, "columnInfoItem"),
						Item.class), label);
			} else {
				spo = new ColumnInfo((String) converter.convertToComplexType(
						getPropertyAndRemove(uuid, ColumnInfo.COLUMN_ALIAS),
						String.class));
			}

		} else if (type.equals(ContentBox.class.getSimpleName())) {
			spo = new ContentBox();

		} else if (type.equals(Grant.class.getSimpleName())) {
			String subject = null;
			try {
				subject = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "subject"), String.class);
			} catch (SPPersistenceException e) {
				// no op
			}
			String grantType = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "type"), String.class);
			boolean create = (Boolean) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "createPrivilege"),
					Boolean.class);
			boolean modify = (Boolean) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "modifyPrivilege"),
					Boolean.class);
			boolean delete = (Boolean) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "deletePrivilege"),
					Boolean.class);
			boolean execute = (Boolean) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "executePrivilege"),
					Boolean.class);
			boolean grant = (Boolean) converter
					.convertToComplexType(getPropertyAndRemove(uuid,
							"grantPrivilege"), Boolean.class);

			spo = new Grant(subject, grantType, create, modify, delete, execute,
					grant);

		} else if (type.equals(Group.class.getSimpleName())) {
			String name = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "name"), String.class);

			spo = new Group(name);

		} else if (type.equals(GroupMember.class.getSimpleName())) {
			User user = (User) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "user"), User.class);

			spo = new GroupMember(user);

		} else if (type.equals(Guide.class.getSimpleName())) {
			Axis axis = (Axis) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "axis"), Axis.class);
			double offset = (Double) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "offset"), Double.class);

			spo = new Guide(axis, offset);

		} else if (type.equals(ImageRenderer.class.getSimpleName())) {
			ImageRenderer renderer = new ImageRenderer();
			
			WabitImage image = (WabitImage) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "content"), WabitImage.class);
			
			renderer.setImage(image);
			
			spo = renderer;

		} else if (type.equals(Label.class.getSimpleName())) {
			spo = new Label();

		} else if (type.equals(OlapQuery.class.getSimpleName())) {
			String name = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "name"), String.class);
			String queryName = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "queryName"), String.class);
			String catalogName = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "catalogName"), String.class);
			String schemaName = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "schemaName"), String.class);
			String cubeName = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "cubeName"), String.class);

			spo = new OlapQuery(uuid, session.getContext(), name, queryName,
					catalogName, schemaName, cubeName);

		} else if (type.equals(Page.class.getSimpleName())) {
			String name = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "name"), String.class);
			int width = (Integer) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "width"), Integer.class);
			int height = (Integer) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "height"), Integer.class);
			PageOrientation orientation = (PageOrientation) converter
					.convertToComplexType(getPropertyAndRemove(uuid,
							"orientation"), PageOrientation.class);

			spo = new Page(name, width, height, orientation, false);

		} else if (type.equals(QueryCache.class.getSimpleName())) {
			JDBCDataSource dataSource = (JDBCDataSource) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "dataSource"), JDBCDataSource.class);
			try {
				WabitConstantsContainer constantsContainer = (WabitConstantsContainer) createObjectByCalls(
						uuid, WabitConstantsContainer.class.getSimpleName());
				
				spo = new QueryCache(session.getContext(), false, constantsContainer, dataSource);
			} catch (IllegalArgumentException e) {
				spo = new QueryCache(session.getContext(), false, null, dataSource);
			}
		} else if (type.equals(Report.class.getSimpleName())) {
			String name = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "name"), String.class);
			try {
				Page page = (Page) createObjectByCalls(uuid, Page.class
					.getSimpleName());
				spo = new Report(name, uuid, page);
			} catch (IllegalArgumentException e) {
				spo = new Report(name, uuid);
			}
		} else if (type.equals(ReportTask.class.getSimpleName())) {
			spo = new ReportTask();

		} else if (type.equals(ResultSetRenderer.class.getSimpleName())) {
			String contentID = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "content"), String.class);

			QueryCache query = (QueryCache) converter.convertToComplexType(
					contentID, QueryCache.class);

			if (query == null) {
				throw new SPPersistenceException(uuid,
						"Cannot commit ResultSetRenderer with UUID " + uuid
								+ " as its QueryCache reference with UUID "
								+ contentID
								+ " does not exist in the workspace.");
			}

			spo = new ResultSetRenderer(query);

		} else if (type.equals(Template.class.getSimpleName())) {
			String name = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "name"), String.class);
			Page page = (Page) createObjectByCalls(uuid, Page.class
					.getSimpleName());

			spo = new Template(name, uuid, page);

		} else if (type.equals(User.class.getSimpleName())) {
			String username = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "name"), String.class);
			String password = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "password"), String.class);

			spo = new User(username, password);

		} else if (type.equals(WabitColumnItem.class.getSimpleName())) {
			Item item = (Item) converter
					.convertToComplexType(
							getPropertyAndRemove(uuid, "delegate"),
							Item.class);
			
			if (!(item instanceof SQLObjectItem)) {
				throw new ClassCastException("WabitColumnItem with UUID " + uuid + 
						" cannot contain a delegate of type " + item.getClass());
			}

			spo = new WabitColumnItem((SQLObjectItem) item);

		} else if (type.equals(WabitConstantsContainer.class.getSimpleName())) {
			Container delegate = (Container) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "delegate"), Container.class);

			spo = new WabitConstantsContainer(delegate, false);

		} else if (type.equals(WabitConstantItem.class.getSimpleName())) {
			Item item = (Item) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "delegate"), Item.class);
			
			if (!(item instanceof StringItem)) {
				throw new ClassCastException("WabitConstantItem with UUID " + uuid + 
						" cannot contain a delegate of type " + item.getClass());
			}

			spo = new WabitConstantItem((StringItem) item);

		} else if (type.equals(WabitDataSource.class.getSimpleName())) {
			String dsName = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "SPDataSource"), String.class);
			if (session.getDataSources() == null) throw new RuntimeException("No data sources exist.");
			SPDataSource spds = session.getDataSources().getDataSource(dsName);
			
			if (spds == null) {
				
				synchronized (this) {
					try {
						this.wait(100);
					} catch (InterruptedException e) {
						// no op
					}
				}
				
				dsName = (String) converter.convertToComplexType(
						getPropertyAndRemove(uuid, "SPDataSource"), String.class);
				spds = session.getDataSources().getDataSource(dsName);
				
				if (spds == null) {
					throw new SPPersistenceException(uuid,
						"The Wabit does not know about Datasource '" + dsName
						+ "'");
				}
			}

			spo = new WabitDataSource(spds);

		} else if (type.equals(WabitImage.class.getSimpleName())) {
			spo = new WabitImage();

		} else if (type.equals(WabitJoin.class.getSimpleName())) {

			QueryCache query = (QueryCache) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "query"), QueryCache.class);
			SQLJoin delegate = (SQLJoin) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "delegate"), SQLJoin.class);

			spo = new WabitJoin(query, delegate);

		} else if (type.equals(WabitOlapAxis.class.getSimpleName())) {
			Object ordinal = getPropertyAndRemove(uuid, "ordinal");

			org.olap4j.Axis axis = (org.olap4j.Axis) converter
					.convertToComplexType(ordinal, org.olap4j.Axis.class);

			spo = new WabitOlapAxis(axis);

		} else if (type.equals(WabitOlapDimension.class.getSimpleName())) {
			String name = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "name"), String.class);

			spo = new WabitOlapDimension(name);

		} else if (type.equals(WabitOlapExclusion.class.getSimpleName())) {
			Operator operator = (Operator) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "operator"), Operator.class);
			String uniqueMemberName = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "uniqueMemberName"),
					String.class);

			spo = new WabitOlapExclusion(operator, uniqueMemberName);

		} else if (type.equals(WabitOlapInclusion.class.getSimpleName())) {
			Operator operator = (Operator) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "operator"), Operator.class);
			String uniqueMemberName = (String) converter.convertToComplexType(
					getPropertyAndRemove(uuid, "uniqueMemberName"),
					String.class);

			spo = new WabitOlapInclusion(operator, uniqueMemberName);

		} else if (type.equals(WabitTableContainer.class.getSimpleName())) {
			TableContainer tableContainer = (TableContainer) converter
					.convertToComplexType(
							getPropertyAndRemove(uuid, "delegate"),
							TableContainer.class);

			spo = new WabitTableContainer(tableContainer, false);
		} else if (type.equals(WabitWorkspace.class.getSimpleName())) {
			session.getWorkspace().reset();

		} else {
			throw new SPPersistenceException(uuid,
					"Unknown WabitObject type: " + type);
		}

		if (spo != null) {
			spo.setUUID(uuid);
		}

		pwo.setLoaded(true);
		return spo;
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
	 * @throws SPPersistenceException
	 *             Thrown if the object does not have the specified property
	 *             name.
	 */
	private Object getPropertyAndRemove(String uuid, String propertyName)
			throws SPPersistenceException {
		for (WabitObjectProperty wop : persistedProperties.get(uuid)) {
			if (wop.getPropertyName().equals(propertyName)) {
				Object value = wop.getNewValue();

				persistedProperties.remove(uuid, wop);

				return value;
			}
		}

		// Property might not be persisted because it might be null. 
		// We therefore need to return null.
		return null;
	}

	/**
	 * This method searches through the {@link Multimap} of persisted properties
	 * under a specific {@link WabitObject} UUID to see if it contains a
	 * property name.
	 * 
	 * @param uuid
	 *            The {@link WabitObject} UUID to search for.
	 * @param propertyName
	 *            The property name to search for.
	 * @return The determinant of whether the {@link Multimap} of persisted
	 *         properties contains the property name under the specified UUID.
	 */
	private boolean containsProperty(String uuid, String propertyName) {
		for (WabitObjectProperty wop : persistedProperties.get(uuid)) {
			if (wop.getPropertyName().equals(propertyName)) {
				return true;
			}
		}
		return false;
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
			if (SQLPowerUtils.findByUuid(root, uuid, SPObject.class) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This will create a wabit object based on persist calls that we have
	 * already pooled.
	 */
	private SPObject createObjectByCalls(String parentUUID, String classType)
			throws SPPersistenceException {
		for (PersistedWabitObject pwo : persistedObjects) {
			if (pwo.isLoaded())
				continue;
			if (pwo.getType().equals(classType)
					&& pwo.getParentUUID().equals(parentUUID)) {
				return loadWabitObject(pwo);
			}
		}
		throw new IllegalArgumentException("Cannot find the object "
				+ classType + " that is the child of " + parentUUID
				+ " that we are loading.");
	}

	/**
	 * Commits the persisted {@link WabitObject} property values
	 * 
	 * @throws SPPersistenceException
	 *             Thrown if an invalid WabitObject type has been persisted into
	 *             storage. This theoretically should not occur.
	 */
	private void commitProperties() throws SPPersistenceException {
		SPObject spo;
		String propertyName;
		Object newValue;

		for (String uuid : persistedProperties.keySet()) {
			spo = SQLPowerUtils.findByUuid(root, uuid, SPObject.class);
			if (spo == null) {
				throw new IllegalStateException("Couldn't locate object "
						+ uuid + " in session");
			}

			for (WabitObjectProperty wop : persistedProperties.get(uuid)) {
				
				propertyName = wop.getPropertyName();
				newValue = wop.getNewValue();

				applyProperty(spo, propertyName, newValue);
				
				this.persistedPropertiesRollbackList.add(
					new PersistedPropertiesEntry(
						spo.getUUID(), //The uuid can be changed so using the currently set one.
						wop.getPropertyName(), 
						wop.getDataType(), 
						wop.getOldValue()));
			}
		}
		persistedProperties.clear();
	}

	private void applyProperty(SPObject spo, String propertyName, Object newValue) throws SPPersistenceException {
		logger.debug("Applying property " + propertyName + " to " + spo.getClass().getSimpleName() + " at " + spo.getUUID());
		if (isCommonProperty(propertyName)) {
			commitCommonProperty(spo, propertyName, newValue);
		} else if (spo instanceof CellSetRenderer) {
			commitCellSetRendererProperty((CellSetRenderer) spo,
					propertyName, newValue);
		} else if (spo instanceof Chart) {
			commitChartProperty((Chart) spo, propertyName, newValue);
		} else if (spo instanceof ChartColumn) {
			commitChartColumnProperty((ChartColumn) spo, propertyName,
					newValue);
		} else if (spo instanceof ChartRenderer) {
			commitChartRendererProperty((ChartRenderer) spo,
					propertyName, newValue);
		} else if (spo instanceof ColumnInfo) {
			commitColumnInfoProperty((ColumnInfo) spo, propertyName,
					newValue);
		} else if (spo instanceof ContentBox) {
			commitContentBoxProperty((ContentBox) spo, propertyName,
					newValue);
		} else if (spo instanceof Grant) {
			commitGrantProperty((Grant) spo, propertyName, newValue);
		} else if (spo instanceof Group) {
			commitGroupProperty((Group) spo, propertyName, newValue);
		} else if (spo instanceof GroupMember) {
			commitGroupMemberProperty((GroupMember) spo, propertyName,
					newValue);
		} else if (spo instanceof Guide) {
			commitGuideProperty((Guide) spo, propertyName, newValue);
		} else if (spo instanceof ImageRenderer) {
			commitImageRendererProperty((ImageRenderer) spo,
					propertyName, newValue);
		} else if (spo instanceof Label) {
			commitLabelProperty((Label) spo, propertyName, newValue);
		} else if (spo instanceof Layout) {
			commitLayoutProperty((Layout) spo, propertyName, newValue);
		} else if (spo instanceof OlapQuery) {
			commitOlapQueryProperty((OlapQuery) spo, propertyName,
					newValue);
		} else if (spo instanceof Page) {
			commitPageProperty((Page) spo, propertyName, newValue);
		} else if (spo instanceof QueryCache) {
			commitQueryCacheProperty((QueryCache) spo, propertyName,
					newValue);
		} else if (spo instanceof ReportTask) {
			commitReportTaskProperty((ReportTask) spo, propertyName,
					newValue);
		} else if (spo instanceof ResultSetRenderer) {
			commitResultSetRendererProperty((ResultSetRenderer) spo,
					propertyName, newValue);
		} else if (spo instanceof User) {
			commitUserProperty((User) spo, propertyName, newValue);
		} else if (spo instanceof WabitConstantsContainer) {
			commitWabitConstantsContainerProperty(
					(WabitConstantsContainer) spo, propertyName,
					newValue);
		} else if (spo instanceof WabitDataSource) {
			commitWabitDataSourceProperty((WabitDataSource) spo,
					propertyName, newValue);
		} else if (spo instanceof WabitImage) {
			commitWabitImageProperty((WabitImage) spo, propertyName,
					newValue);
		} else if (spo instanceof WabitItem) {
			commitWabitItemProperty((WabitItem) spo, propertyName,
					newValue);
		} else if (spo instanceof WabitJoin) {
			commitWabitJoinProperty((WabitJoin) spo, propertyName,
					newValue);
		} else if (spo instanceof WabitOlapAxis) {
			commitWabitOlapAxisProperty((WabitOlapAxis) spo,
					propertyName, newValue);
		} else if (spo instanceof WabitOlapDimension) {
			commitWabitOlapDimensionProperty((WabitOlapDimension) spo,
					propertyName, newValue);
		} else if (spo instanceof WabitOlapSelection) {
			commitWabitOlapSelectionProperty((WabitOlapSelection) spo,
					propertyName, newValue);
		} else if (spo instanceof WabitTableContainer) {
			commitWabitTableContainerProperty((WabitTableContainer) spo,
					propertyName, newValue);
		} else if (spo instanceof WabitWorkspace) {
			commitWabitWorkspaceProperty((WabitWorkspace) spo,
					propertyName, newValue);
		} else {
			throw new SPPersistenceException(spo.getUUID(),
					"Invalid WabitObject of type " + spo.getClass());
		}
	}

	/**
	 * Commits the removal of persisted {@link WabitObject}s
	 * 
	 * @throws SPPersistenceException
	 *             Thrown if a WabitObject could not be removed from its parent.
	 */
	private void commitRemovals() throws SPPersistenceException {
		for (String uuid : objectsToRemove.keySet()) {
			SPObject spo = SQLPowerUtils.findByUuid(root, uuid,
					SPObject.class);
			SPObject parent = SQLPowerUtils.findByUuid(root, objectsToRemove
					.get(uuid), SPObject.class);
			try {
				int index = parent.getChildren().indexOf(spo);
				index -= parent.childPositionOffset(spo.getClass());
				parent.removeChild(spo);
				this.objectsToRemoveRollbackList.add(
					new RemovedObjectEntry(
						parent.getUUID(), 
						spo,
						index));
			} catch (IllegalArgumentException e) {
				throw new SPPersistenceException(uuid, e);
			} catch (ObjectDependentException e) {
				throw new SPPersistenceException(uuid, e);
			}
		}
		objectsToRemove.clear();
	}
	
	/**
	 * Rolls back the removal of persisted {@link WabitObject}s
	 * 
	 * @throws SPPersistenceException
	 *             Thrown if a WabitObject could not be rolled back from its parent.
	 */
	private void rollbackRemovals() throws IllegalStateException {
		// We must rollback in the inverse order the operations were performed.
		Collections.reverse(this.objectsToRemoveRollbackList);
		for (RemovedObjectEntry entry : this.objectsToRemoveRollbackList) {
			final String parentUuid = entry.getParentUUID();
			final SPObject objectToRestore = entry.getRemovedChild();
			final int index = entry.getIndex();
			final SPObject parent = SQLPowerUtils.findByUuid(root, parentUuid, SPObject.class);
			try {
				parent.addChild(objectToRestore, index);
			} catch (Throwable t) {
				// Keep going. We need to rollback as much as we can.
				logger.error("Cannot rollback " + entry.getRemovedChild() + " child removal", t);
			}
		}
	}
	
	private void rollbackProperties() throws SPPersistenceException {
		Collections.reverse(this.persistedPropertiesRollbackList);
		for (PersistedPropertiesEntry entry : this.persistedPropertiesRollbackList) {
			try {
				final String parentUuid = entry.uuid;
				final String propertyName = entry.propertyName;
				final Object rollbackValue = entry.rollbackValue;
				final SPObject parent = SQLPowerUtils.findByUuid(root, parentUuid, SPObject.class);
				if (parent != null) {
					this.applyProperty(parent, propertyName, rollbackValue);
				}
			} catch (Throwable t) {
				// Keep going. We need to rollback as much as we can.
				logger.error("Cannot rollback change to " + entry.propertyName + " to value " + entry.rollbackValue, t);
			}
		}
	}
	
	private void rollbackCreations() throws Exception {
		Collections.reverse(this.persistedObjectsRollbackList);
		for (PersistedObjectEntry entry : this.persistedObjectsRollbackList) {
			try {
				// We need to verify if the entry specifies a parent.
				// WabitWorkspaces don't have parents so we can't remove them really...
				if (entry.parentId != null) {
					final SPObject parent = SQLPowerUtils.findByUuid(root, entry.parentId, SPObject.class);
					final SPObject child = SQLPowerUtils.findByUuid(root, entry.childrenId, SPObject.class);
					parent.removeChild(child);
				}
			} catch (Throwable t) {
				// Keep going. We need to rollback as much as we can.
				logger.error("Cannot rollback " + entry.childrenId + " child creation", t);
			}
		}
	}

	/**
	 * Persists a {@link WabitObject} given by its UUID, class name, and parent
	 * UUID
	 * <p>
	 * A new {@link WabitWorkspace} object signals that there is a new workspace
	 * being persisted and the current one attached to this listener must either
	 * go away or clean up. See the class level documentation for more on this special
	 * case.
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	public void persistObject(String parentUUID, String type, String uuid,
			int index) throws SPPersistenceException {
		synchronized (session) {
			this.enforeThreadSafety();
			logger.debug(String.format(
					"wsp.persistObject(\"%s\", \"%s\", \"%s\", %d);", parentUUID,
					type, uuid, index));
			if (transactionCount == 0) {
				this.rollback();
				throw new SPPersistenceException("Cannot persist objects while outside a transaction.");
			}
			SPObject objectToPersist = SQLPowerUtils.findByUuid(root, uuid, SPObject.class);
			boolean isWorkspace= objectToPersist instanceof WabitWorkspace;
			if (objectToPersist != null && isWorkspace) {
				//reset now or the next object persisted will fail a few lines down.
				((WabitWorkspace) objectToPersist).reset();
			}
			if (exists(uuid) && !isWorkspace) {
				this.rollback();
				throw new SPPersistenceException(uuid,
						"A WabitObject with UUID " + uuid + " and type " + type
						+ " under parent with UUID " + parentUUID
						+ " already exists.");
			}

			PersistedWabitObject pwo = new PersistedWabitObject(parentUUID,
					type, uuid, index);
			persistedObjects.add(pwo);
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	public void persistProperty(String uuid, String propertyName,
			DataType propertyType, Object oldValue, Object newValue)
			throws SPPersistenceException {
		if (transactionCount <= 0) {
			this.rollback();
			throw new SPPersistenceException("Cannot persist objects while outside a transaction.");
		}
		synchronized (session) {
			this.enforeThreadSafety();
			logger.debug(String.format(
					"wsp.persistProperty(\"%s\", \"%s\", DataType.%s, %s, %s);",
					uuid, propertyName, propertyType.name(), oldValue, newValue));
			try {
				persistPropertyHelper(uuid, propertyName, propertyType, oldValue,
						newValue, this.godMode);
			} catch (SPPersistenceException e) {
				this.rollback();
				throw e;
			}
		}
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	public void persistProperty(String uuid, String propertyName,
			DataType propertyType, Object newValue)
			throws SPPersistenceException {
		if (transactionCount <= 0) {
			this.rollback();
			throw new SPPersistenceException("Cannot persist objects while outside a transaction.");
		}
		synchronized (session) {
			this.enforeThreadSafety();
			logger.debug(String.format(
					"wsp.persistProperty(\"%s\", \"%s\", DataType.%s, %s); // unconditional",
					uuid, propertyName, propertyType.name(),
					newValue));
			try {
				if (newValue instanceof InputStream && !((InputStream) newValue).markSupported()) {
					newValue = new BufferedInputStream((InputStream) newValue);
				}
				persistPropertyHelper(uuid, propertyName, propertyType, newValue,
					newValue, true);
			} catch (SPPersistenceException e) {
				this.rollback();
				throw e;
			}
		}
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void persistPropertyHelper(String uuid, String propertyName,
			DataType propertyType, Object oldValue, Object newValue,
			boolean unconditional) throws SPPersistenceException {
		
		if (!exists(uuid)) {
			throw new SPPersistenceException(uuid,
					"WabitObject with UUID " + uuid + " could not be found." +
					" Was trying to set its property \"" + propertyName + "\" " +
					"to value \"" + newValue + "\".");
		}
		
		Object lastPropertyValueFound = null;
		
		for (WabitObjectProperty wop : persistedProperties.get(uuid)) {
			if (propertyName.equals(wop.getPropertyName())) {
				lastPropertyValueFound = wop.getNewValue();
			}
		}
		
		Object propertyValue = null;
		SPObject spo = SQLPowerUtils.findByUuid(root, uuid,
				SPObject.class);
		
		if (lastPropertyValueFound != null) {
			if (!unconditional && !lastPropertyValueFound.equals(oldValue)) {
				throw new SPPersistenceException(uuid, "For property \""
						+ propertyName + "\", the expected property value \""
						+ oldValue
						+ "\" does not match with the actual property value \""
						+ lastPropertyValueFound + "\"");
			}
		} else {
			if (spo != null) {

				if (isCommonProperty(propertyName)) {
					propertyValue = getCommonProperty(spo, propertyName);
				} else if (spo instanceof CellSetRenderer) {
					propertyValue = getCellSetRendererProperty(
							(CellSetRenderer) spo, propertyName);
				} else if (spo instanceof Chart) {
					propertyValue = getChartProperty((Chart) spo, propertyName);
				} else if (spo instanceof ChartColumn) {
					propertyValue = getChartColumnProperty((ChartColumn) spo,
							propertyName);
				} else if (spo instanceof ChartRenderer) {
					propertyValue = getChartRendererProperty((ChartRenderer) spo,
							propertyName);
				} else if (spo instanceof ColumnInfo) {
					propertyValue = getColumnInfoProperty((ColumnInfo) spo,
							propertyName);
				} else if (spo instanceof ContentBox) {
					propertyValue = getContentBoxProperty((ContentBox) spo,
							propertyName);
				} else if (spo instanceof Grant) {
					propertyValue = getGrantProperty((Grant) spo, propertyName);
				} else if (spo instanceof Group) {
					propertyValue = getGroupProperty((Group) spo, propertyName);
				} else if (spo instanceof GroupMember) {
					propertyValue = getGroupMemberProperty((GroupMember) spo,
							propertyName);
				} else if (spo instanceof Guide) {
					propertyValue = getGuideProperty((Guide) spo, propertyName);
				} else if (spo instanceof ImageRenderer) {
					propertyValue = getImageRendererProperty((ImageRenderer) spo,
							propertyName);
				} else if (spo instanceof Label) {
					propertyValue = getLabelProperty((Label) spo, propertyName);
				} else if (spo instanceof Layout) {
					propertyValue = getLayoutProperty((Layout) spo, propertyName);
				} else if (spo instanceof OlapQuery) {
					propertyValue = getOlapQueryProperty((OlapQuery) spo,
							propertyName);
				} else if (spo instanceof Page) {
					propertyValue = getPageProperty((Page) spo, propertyName);
				} else if (spo instanceof QueryCache) {
					propertyValue = getQueryCacheProperty((QueryCache) spo,
							propertyName);
				} else if (spo instanceof ReportTask) {
					propertyValue = getReportTaskProperty((ReportTask) spo,
							propertyName);
				} else if (spo instanceof ResultSetRenderer) {
					propertyValue = getResultSetRendererProperty(
							(ResultSetRenderer) spo, propertyName);
				} else if (spo instanceof User) {
					propertyValue = getUserProperty((User) spo, propertyName);
				} else if (spo instanceof WabitConstantsContainer) {
					propertyValue = getWabitConstantsContainerProperty(
							(WabitConstantsContainer) spo, propertyName);
				} else if (spo instanceof WabitDataSource) {
					propertyValue = getWabitDataSourceProperty(
							(WabitDataSource) spo, propertyName);
				} else if (spo instanceof WabitImage) {
					
					propertyValue = getWabitImageProperty((WabitImage) spo,
							propertyName);
					
					// We are converting the expected old value InputStream in this
					// way because we want to ensure that the conversion process is
					// the same as the one used to convert the current image into
					// a byte array.
					if (oldValue != null) {
						// We cannot destroy the old value here, because in some
						// cases it is the same object as the new value.
						try {
							InputStream old = (InputStream) oldValue;
							old.mark(old.available());
							oldValue = PersisterUtils.convertImageToStreamAsPNG(
									(Image) converter.convertToComplexType(oldValue,
											Image.class)).toByteArray();
							old.reset();
						} catch (IOException e) {
							throw new SPPersistenceException(uuid, e);
						}
					}

				} else if (spo instanceof WabitItem) {
					propertyValue = getWabitItemProperty((WabitItem) spo,
							propertyName);
				} else if (spo instanceof WabitJoin) {
					propertyValue = getWabitJoinProperty((WabitJoin) spo,
							propertyName);
				} else if (spo instanceof WabitOlapAxis) {
					propertyValue = getWabitOlapAxisProperty((WabitOlapAxis) spo,
							propertyName);
				} else if (spo instanceof WabitOlapDimension) {
					propertyValue = getWabitOlapDimensionProperty(
							(WabitOlapDimension) spo, propertyName);
				} else if (spo instanceof WabitOlapSelection) {
					propertyValue = getWabitOlapSelectionProperty(
							(WabitOlapSelection) spo, propertyName);
				} else if (spo instanceof WabitTableContainer) {
					propertyValue = getWabitTableContainerProperty(
							(WabitTableContainer) spo, propertyName);
				} else if (spo instanceof WabitWorkspace) {
					propertyValue = getWabitWorkspaceProperty((WabitWorkspace) spo,
							propertyName);
				} else {
					throw new SPPersistenceException(uuid,
							"Invalid WabitObject type " + spo.getClass());
				}
				
				if (!unconditional && propertyValue != null &&
						((oldValue == null) ||
								(oldValue != null &&
										(spo instanceof WabitImage && propertyName.equals("image") && 
												!Arrays.equals((byte[]) oldValue, (byte[]) propertyValue)) ||
										(!(spo instanceof WabitImage && propertyName.equals("image")) && 
												!oldValue.equals(propertyValue))))) {
					throw new SPPersistenceException(uuid, "For property \""
							+ propertyName + "\" on WabitObject of type "
							+ spo.getClass() + " and UUID + " + spo.getUUID()
							+ ", the expected property value \"" + oldValue
							+ "\" does not match with the actual property value \""
							+ propertyValue + "\"");
				}
//			} else if (!unconditional) {
//				throw new SPPersistenceException(uuid, "Could not find the object with id " + 
//						uuid + " to set property " + propertyValue);
			}
		}
		
		if (spo != null) {
			persistedProperties.put(uuid, new WabitObjectProperty(uuid,
					propertyName, propertyType, propertyValue, newValue, unconditional));
		} else {
			persistedProperties.put(uuid, new WabitObjectProperty(uuid,
					propertyName, propertyType, oldValue, newValue, unconditional));
		}
	}

	/**
	 * Returns a simple string for use in exceptions in multiple locations
	 * within this class. This message describes that a property cannot be found
	 * on the object. This is refactored here as a lot of methods throw an
	 * exception with a message equivalent to this one.
	 * 
	 * @param spo
	 *            The {@link WabitObject} that does not contain the given
	 *            property.
	 * @param propertyName
	 *            The property we want to find on the {@link WabitObject} that
	 *            cannot be found.
	 * @return An error message for exceptions that describes the above.
	 */
	private String getSPPersistenceExceptionMessage(SPObject spo,
			String propertyName) {
		return "Cannot persist property \"" + propertyName + "\" on "
				+ spo.getClass() + " with name \"" + spo.getName()
				+ "\" and UUID \"" + spo.getUUID() + "\"";
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
		return (propertyName.equals("name") || propertyName.equals("UUID") || propertyName
				.equals("parent"));
	}

	/**
	 * Retrieves a common property value from a {@link WabitObject} based on the
	 * property name and converts it to something that can be passed to a
	 * persister. The only two common properties are "name" and "uuid".
	 * 
	 * @param spo
	 *            The {@link WabitObject} to retrieve the named property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getCommonProperty(SPObject spo, String propertyName)
			throws SPPersistenceException {
		if (propertyName.equals("name")) {
			return converter.convertToBasicType(spo.getName());
		} else if (propertyName.equals("UUID")) {
			return converter.convertToBasicType(spo.getUUID());
		} else if (propertyName.equals("parent")) {
			return converter.convertToBasicType(spo.getParent());
		} else {
			throw new SPPersistenceException(spo.getUUID(),
					getSPPersistenceExceptionMessage(spo, propertyName));
		}
	}

	/**
	 * Commits a persisted {@link WabitObject} common property.
	 * 
	 * @param spo
	 *            The {@link WabitObject} to commit the persisted common
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitCommonProperty(SPObject spo, String propertyName,
			Object newValue) throws SPPersistenceException {
		if (propertyName.equals("name")) {
			spo.setName((String) converter.convertToComplexType(newValue,
					String.class));
		} else if (propertyName.equals("UUID")) {
			spo.setUUID((String) converter.convertToComplexType(newValue,
					String.class));
		} else if (propertyName.equals("parent")) {
			SPObject parent = (SPObject) converter.convertToComplexType(newValue,
					SPObject.class);
			if (logger.isDebugEnabled()) {
				if (parent != null) {
					logger.debug("Setting property " + propertyName + " on " + spo.getName() + 
							" to " + parent.getName());
				} else {
					logger.debug("Setting property " + propertyName + " on " + spo.getName() + 
							" to null");
				}
			}
			spo.setParent(parent);

		} else {
			throw new SPPersistenceException(spo.getUUID(),
					getSPPersistenceExceptionMessage(spo, propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitWorkspace} object based on
	 * the property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param workspace
	 *            The {@link WabitWorkspace} object to retrieve the named
	 *            property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getWabitWorkspaceProperty(WabitWorkspace workspace,
			String propertyName) throws SPPersistenceException {
		throw new SPPersistenceException(
				workspace.getUUID(),
				getSPPersistenceExceptionMessage(workspace, propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method or
	 *             the new value could not be committed.
	 */
	private void commitWabitWorkspaceProperty(WabitWorkspace workspace,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		String uuid = workspace.getUUID();

		throw new SPPersistenceException(
				uuid,
				getSPPersistenceExceptionMessage(workspace, propertyName));
	}

	/**
	 * Retrieves a property value from a {@link WabitDataSource} object based on
	 * the property name and converts it to something that can be passed to a
	 * persister. Currently, uncommon properties cannot be retrieved from this
	 * class.
	 * 
	 * @param wds
	 *            The {@link WabitDataSource} object to retrieve the named
	 *            property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getWabitDataSourceProperty(WabitDataSource wds,
			String propertyName) throws SPPersistenceException {
		throw new SPPersistenceException(wds.getUUID(), "Invalid property: "
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitWabitDataSourceProperty(WabitDataSource wds,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		throw new SPPersistenceException(wds.getUUID(), "Invalid property: "
				+ propertyName);
	}

	/**
	 * Retrieves a property value from a {@link QueryCache} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param query
	 *            The {@link QueryCache} object to retrieve the named property
	 *            from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getQueryCacheProperty(QueryCache query, String propertyName)
			throws SPPersistenceException {

		if (propertyName.equals("streaming")) {
			return converter.convertToBasicType(query.isStreaming());

		} else if (propertyName.equals("streamingRowLimit")) {
			return converter.convertToBasicType(query.getStreamingRowLimit());

		} else if (propertyName.equals(QueryImpl.ROW_LIMIT)) {
			return converter.convertToBasicType(query.getRowLimit());

		} else if (propertyName.equals(QueryImpl.GROUPING_ENABLED)) {
			return converter.convertToBasicType(query.isGroupingEnabled());

		} else if (propertyName.equals("promptForCrossJoins")) {
			return converter.convertToBasicType(query.getPromptForCrossJoins());

		} else if (propertyName.equals("automaticallyExecuting")) {
			return converter.convertToBasicType(query
					.isAutomaticallyExecuting());

		} else if (propertyName.equals(QueryImpl.GLOBAL_WHERE_CLAUSE)) {
			return converter.convertToBasicType(query.getGlobalWhereClause());

		} else if (propertyName.equals(QueryImpl.USER_MODIFIED_QUERY)) {
			return converter.convertToBasicType(query.getUserModifiedQuery());

		} else if (propertyName.equals("executeQueriesWithCrossJoins")) {
			return converter.convertToBasicType(query
					.getExecuteQueriesWithCrossJoins());

		} else if (propertyName.equals("dataSource")) {
			return converter.convertToBasicType(query.getDataSource());

		} else {
			throw new SPPersistenceException(query.getUUID(),
					getSPPersistenceExceptionMessage(query, propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitQueryCacheProperty(QueryCache query,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		String uuid = query.getUUID();

		if (propertyName.equals("streaming")) {
			query.setStreaming((Boolean) converter.convertToComplexType(
					newValue, Boolean.class));

		} else if (propertyName.equals("streamingRowLimit")) {
			query.setStreamingRowLimit((Integer) converter
					.convertToComplexType(newValue, Integer.class));

		} else if (propertyName.equals(QueryImpl.ROW_LIMIT)) {
			query.setRowLimit((Integer) converter.convertToComplexType(
					newValue, Integer.class));

		} else if (propertyName.equals(QueryImpl.GROUPING_ENABLED)) {
			query.setGroupingEnabled((Boolean) converter.convertToComplexType(
					newValue, Boolean.class));

		} else if (propertyName.equals("promptForCrossJoins")) {
			query.setPromptForCrossJoins((Boolean) converter
					.convertToComplexType(newValue, Boolean.class));

		} else if (propertyName.equals("automaticallyExecuting")) {
			query.setAutomaticallyExecuting((Boolean) converter
					.convertToComplexType(newValue, Boolean.class));

		} else if (propertyName.equals(QueryImpl.GLOBAL_WHERE_CLAUSE)) {
			query.setGlobalWhereClause((String) converter.convertToComplexType(
					newValue, String.class));

		} else if (propertyName.equals(QueryImpl.USER_MODIFIED_QUERY)) {
			query.setUserModifiedQuery((String) converter.convertToComplexType(
					newValue, String.class));

		} else if (propertyName.equals("executeQueriesWithCrossJoins")) {
			query.setExecuteQueriesWithCrossJoins((Boolean) converter
					.convertToComplexType(newValue, Boolean.class));

		} else if (propertyName.equals("dataSource")) {
			query.setDataSourceWithoutSideEffects((JDBCDataSource) converter.convertToComplexType(
					newValue, JDBCDataSource.class)); 

		} else {
			throw new SPPersistenceException(uuid, "Invalid property: "
					+ propertyName);
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitConstantsContainer} object
	 * based on the property name and converts it to something that can be
	 * passed to a persister.
	 * 
	 * @param wabitConstantsContainer
	 *            The {@link WabitConstantsContainer} to retrieve the named
	 *            property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getWabitConstantsContainerProperty(
			WabitConstantsContainer wabitConstantsContainer, String propertyName)
			throws SPPersistenceException {
		Container delegate = wabitConstantsContainer.getDelegate();

		if (propertyName.equals("alias")) {
			return converter.convertToBasicType(delegate.getAlias());

		} else if (propertyName.equals("position")) {
			return converter.convertToBasicType(delegate.getPosition());

		} else {
			throw new SPPersistenceException(wabitConstantsContainer
					.getUUID(), getSPPersistenceExceptionMessage(
					wabitConstantsContainer, propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitWabitConstantsContainerProperty(
			WabitConstantsContainer wabitConstantsContainer,
			String propertyName, Object newValue)
			throws SPPersistenceException {

		Container delegate = wabitConstantsContainer.getDelegate();

		if (propertyName.equals("alias")) {
			delegate.setAlias((String) converter.convertToComplexType(newValue,
					String.class));

		} else if (propertyName.equals("position")) {
			delegate.setPosition((Point2D) converter.convertToComplexType(
					newValue, Point2D.class));

		} else {
			throw new SPPersistenceException(wabitConstantsContainer
					.getUUID(), getSPPersistenceExceptionMessage(
					wabitConstantsContainer, propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitTableContainer} object
	 * based on the property name and converts it to something that can be
	 * passed to a persister.
	 * 
	 * @param wabitTableContainer
	 *            The {@link WabitTableContainer} to retrieve the named property
	 *            from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getWabitTableContainerProperty(
			WabitTableContainer wabitTableContainer, String propertyName)
			throws SPPersistenceException {

		if (propertyName.equals("position")) {
			return converter.convertToBasicType(wabitTableContainer
					.getPosition());

		} else if (propertyName.equals("alias")) {
			return converter.convertToBasicType(wabitTableContainer.getAlias());

		} else {
			throw new SPPersistenceException(wabitTableContainer.getUUID(),
					getSPPersistenceExceptionMessage(wabitTableContainer,
							propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitWabitTableContainerProperty(
			WabitTableContainer wabitTableContainer, String propertyName,
			Object newValue) throws SPPersistenceException {

		if (propertyName.equals("position")) {
			wabitTableContainer.setPosition((Point2D) converter
					.convertToComplexType(newValue, Point2D.class));

		} else if (propertyName.equals("alias")) {
			wabitTableContainer.setAlias((String) converter
					.convertToComplexType(newValue, String.class));

		} else {
			throw new SPPersistenceException(wabitTableContainer.getUUID(),
					getSPPersistenceExceptionMessage(wabitTableContainer,
							propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitItem} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param wabitItem
	 *            The {@link WabitItem} to retrieve the named property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getWabitItemProperty(WabitItem wabitItem, String propertyName)
			throws SPPersistenceException {
		String uuid = wabitItem.getUUID();
		Item item = wabitItem.getDelegate();

		if (item instanceof SQLObjectItem || item instanceof StringItem) {
			if (propertyName.equals(Item.ALIAS)) {
				return converter.convertToBasicType(item.getAlias());

			} else if (propertyName.equals(Item.WHERE)) {
				return converter.convertToBasicType(item.getWhere());

			} else if (propertyName.equals(Item.GROUP_BY)) {
				return converter.convertToBasicType(item.getGroupBy());

			} else if (propertyName.equals(Item.HAVING)) {
				return converter.convertToBasicType(item.getHaving());

			} else if (propertyName.equals(Item.ORDER_BY)) {
				return converter.convertToBasicType(item.getOrderBy());

			} else if (propertyName.equals(Item.SELECTED)) {
				return converter.convertToBasicType(item.getSelected());

			} else if (propertyName.equals(Item.WHERE)) {
				return converter.convertToBasicType(item.getWhere());

			} else if (propertyName.equals("orderByOrdering")) {
				return converter.convertToBasicType(item.getOrderByOrdering());

			} else if (propertyName.equals("columnWidth")) {
				return converter.convertToBasicType(item.getColumnWidth());

			} else {
				throw new SPPersistenceException(uuid,
						getSPPersistenceExceptionMessage(wabitItem,
								propertyName));
			}
		} else {
			throw new SPPersistenceException(uuid,
					"Unknown WabitItem with name " + wabitItem.getName());
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method or if
	 *             an invalid delegate is contained within this wrapper.
	 */
	private void commitWabitItemProperty(WabitItem wabitItem,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		String uuid = wabitItem.getUUID();
		Item item = wabitItem.getDelegate();

		if (item instanceof SQLObjectItem || item instanceof StringItem) {
			if (propertyName.equals(Item.ALIAS)) {
				item.setAlias((String) converter.convertToComplexType(newValue,
						String.class));

			} else if (propertyName.equals(Item.WHERE)) {
				item.setWhere((String) converter.convertToComplexType(newValue,
						String.class));

			} else if (propertyName.equals(Item.GROUP_BY)) {
				item.setGroupBy((SQLGroupFunction) converter
								.convertToComplexType(newValue,
										SQLGroupFunction.class));

			} else if (propertyName.equals(Item.HAVING)) {
				item.setHaving((String) converter.convertToComplexType(
						newValue, String.class));

			} else if (propertyName.equals(Item.ORDER_BY)) {
				item.setOrderBy((OrderByArgument) converter
						.convertToComplexType(newValue, OrderByArgument.class));

			} else if (propertyName.equals(Item.SELECTED)) {
				item.setSelected((Integer) converter.convertToComplexType(
						newValue, Integer.class));

			} else if (propertyName.equals(Item.WHERE)) {
				item.setWhere((String) converter.convertToComplexType(newValue,
						String.class));

			} else if (propertyName.equals("orderByOrdering")) {
				item.setOrderByOrdering((Integer) converter
						.convertToComplexType(newValue, Integer.class));

			} else if (propertyName.equals("columnWidth")) {
				item.setColumnWidth((Integer) converter.convertToComplexType(
						newValue, Integer.class));

			} else {
				throw new SPPersistenceException(uuid,
						getSPPersistenceExceptionMessage(wabitItem,
								propertyName));
			}
		} else {
			throw new SPPersistenceException(uuid,
					"Unknown WabitItem with name " + wabitItem.getName());
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitJoin} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param wabitJoin
	 *            The {@link WabitJoin} object to retrieve the named property
	 *            from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getWabitJoinProperty(WabitJoin wabitJoin, String propertyName)
			throws SPPersistenceException {

		if (propertyName.equals("leftColumnOuterJoin")) {
			return converter.convertToBasicType(wabitJoin
					.isLeftColumnOuterJoin());

		} else if (propertyName.equals("rightColumnOuterJoin")) {
			return converter.convertToBasicType(wabitJoin
					.isRightColumnOuterJoin());

		} else if (propertyName.equals("comparator")) {
			return converter.convertToBasicType(wabitJoin.getComparator());
		} else {
			throw new SPPersistenceException(
					wabitJoin.getUUID(),
					getSPPersistenceExceptionMessage(wabitJoin, propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitWabitJoinProperty(WabitJoin wabitJoin,
			String propertyName, Object newValue)
			throws SPPersistenceException {

		if (propertyName.equals("leftColumnOuterJoin")) {
			wabitJoin.setLeftColumnOuterJoin((Boolean) converter
					.convertToComplexType(newValue, Boolean.class));

		} else if (propertyName.equals("rightColumnOuterJoin")) {
			wabitJoin.setRightColumnOuterJoin((Boolean) converter
					.convertToComplexType(newValue, Boolean.class));

		} else if (propertyName.equals("comparator")) {
			wabitJoin.setComparator((String) converter.convertToComplexType(
					newValue, String.class));

		} else {
			throw new SPPersistenceException(
					wabitJoin.getUUID(),
					getSPPersistenceExceptionMessage(wabitJoin, propertyName));
		}
	}

	/**
	 * Retrieves a property value from an {@link OlapQuery} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param olapQuery
	 *            The {@link OlapQuery} object to retrieve the named property
	 *            from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getOlapQueryProperty(OlapQuery olapQuery, String propertyName)
			throws SPPersistenceException {
		if (propertyName.equals("olapDataSource")) {
			return converter.convertToBasicType(olapQuery.getOlapDataSource());

		} else if (propertyName.equals("catalogName")) {
			return converter.convertToBasicType(olapQuery.getCatalogName());

		} else if (propertyName.equals("schemaName")) {
			return converter.convertToBasicType(olapQuery.getSchemaName());

		} else if (propertyName.equals("cubeName")) {
			return converter.convertToBasicType(olapQuery.getCubeName());

		} else if (propertyName.equals("currentCube")) {
			return converter.convertToBasicType(olapQuery.getCurrentCube(),
					olapQuery.getOlapDataSource());

		} else if (propertyName.equals("nonEmpty")) {
			return converter.convertToBasicType(olapQuery.isNonEmpty());

		} else {
			throw new SPPersistenceException(
					olapQuery.getUUID(),
					getSPPersistenceExceptionMessage(olapQuery, propertyName));
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
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method or if
	 *             the new value could not be committed.
	 */
	private void commitOlapQueryProperty(OlapQuery olapQuery,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		if (propertyName.equals("olapDataSource")) {
			olapQuery.setOlapDataSource((Olap4jDataSource) converter
					.convertToComplexType(newValue, Olap4jDataSource.class));

		} else if (propertyName.equals("currentCube")) {
			try {
				olapQuery.setCurrentCube((Cube) converter.convertToComplexType(
						newValue, Cube.class), false);
			} catch (SQLException e) {
				throw new SPPersistenceException(olapQuery.getUUID(),
						"Cannot commit currentCube property for OlapQuery with name \""
								+ olapQuery.getName() + "\" and UUID \""
								+ olapQuery.getUUID() + "\" to value "
								+ newValue.toString(), e);
			}

		} else if (propertyName.equals("nonEmpty")) {
			olapQuery.setNonEmpty((Boolean) converter.convertToComplexType(
					newValue, Boolean.class));

		} else {
			throw new SPPersistenceException(
					olapQuery.getUUID(),
					getSPPersistenceExceptionMessage(olapQuery, propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitOlapSelection} object based
	 * on the property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param selection
	 *            The {@link WabitOlapSelection} object to retrieve the named
	 *            property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getWabitOlapSelectionProperty(WabitOlapSelection selection,
			String propertyName) throws SPPersistenceException {
		if (propertyName.equals("operator")) {
			return converter.convertToBasicType(selection.getOperator());

		} else if (propertyName.equals("uniqueMemberName")) {
			return converter
					.convertToBasicType(selection.getUniqueMemberName());

		} else {
			throw new SPPersistenceException(
					selection.getUUID(),
					getSPPersistenceExceptionMessage(selection, propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitWabitOlapSelectionProperty(WabitOlapSelection selection,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		throw new SPPersistenceException(selection.getUUID(),
				getSPPersistenceExceptionMessage(selection, propertyName));
	}

	/**
	 * Retrieve a property value from a WabitOlapDimension object based on the
	 * property name and converts it to something that can be passed to a
	 * persister. Currently, there are no uncommon properties to retrieve.
	 * 
	 * @param dimension
	 *            The {@link WabitOlapDimension} to retrieve the named property
	 *            from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getWabitOlapDimensionProperty(WabitOlapDimension dimension,
			String propertyName) throws SPPersistenceException {
		throw new SPPersistenceException(dimension.getUUID(),
				getSPPersistenceExceptionMessage(dimension, propertyName));
	}

	/**
	 * Commits a persisted {@link WabitOlapDimension} object property.
	 * Currently, uncommon properties cannot be set.
	 * 
	 * @param dimension
	 *            The {@link WabitOlapDimension} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitWabitOlapDimensionProperty(WabitOlapDimension dimension,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		throw new SPPersistenceException(dimension.getUUID(),
				getSPPersistenceExceptionMessage(dimension, propertyName));
	}

	/**
	 * Retrieves a property value from a {@link WabitOlapAxis} object based on
	 * the property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param olapAxis
	 *            The {@link WabitOlapAxis} object to retrieve the named
	 *            property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getWabitOlapAxisProperty(WabitOlapAxis olapAxis,
			String propertyName) throws SPPersistenceException {

		if (propertyName.equals("nonEmpty")) {
			return converter.convertToBasicType(olapAxis.isNonEmpty());

		} else if (propertyName.equals("sortEvaluationLiteral")) {
			return converter.convertToBasicType(olapAxis
					.getSortEvaluationLiteral());

		} else if (propertyName.equals("sortOrder")) {
			return converter.convertToBasicType(olapAxis.getSortOrder());

		} else {
			throw new SPPersistenceException(olapAxis.getUUID(),
					getSPPersistenceExceptionMessage(olapAxis, propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitWabitOlapAxisProperty(WabitOlapAxis olapAxis,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		if (propertyName.equals("nonEmpty")) {
			olapAxis.setNonEmpty((Boolean) converter.convertToComplexType(
					newValue, Boolean.class));

		} else if (propertyName.equals("sortEvaluationLiteral")) {
			olapAxis.setSortEvaluationLiteral((String) converter
					.convertToComplexType(newValue, String.class));

		} else if (propertyName.equals("sortOrder")) {
			olapAxis.setSortOrder((String) converter.convertToComplexType(
					newValue, String.class));

		} else {
			throw new SPPersistenceException(olapAxis.getUUID(),
					getSPPersistenceExceptionMessage(olapAxis, propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link Chart} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param chart
	 *            The {@link Chart} object to retrieve the named property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getChartProperty(Chart chart, String propertyName)
			throws SPPersistenceException {
		if (propertyName.equals("xaxisName")) {
			return converter.convertToBasicType(chart.getXaxisName());

		} else if (propertyName.equals("yaxisName")) {
			return converter.convertToBasicType(chart.getYaxisName());

		} else if (propertyName.equals("XAxisLabelRotation")) {
			return converter.convertToBasicType(chart.getXAxisLabelRotation());

		} else if (propertyName.equals("gratuitouslyAnimated")) {
			return converter.convertToBasicType(chart.isGratuitouslyAnimated());

		} else if (propertyName.equals("type")) {
			return converter.convertToBasicType(chart.getType());

		} else if (propertyName.equals("legendPosition")) {
			return converter.convertToBasicType(chart.getLegendPosition());

		} else if (propertyName.equals("query")) {
			return converter.convertToBasicType(chart.getQuery());

		} else if (propertyName.equals("backgroundColour")) {
			return converter.convertToBasicType(chart.getBackgroundColour());

		} else {
			throw new SPPersistenceException(chart.getUUID(),
					getSPPersistenceExceptionMessage(chart, propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitChartProperty(Chart chart, String propertyName,
			Object newValue) throws SPPersistenceException {
		String uuid = chart.getUUID();

		if (propertyName.equals("xaxisName")) {
			chart.setXaxisName((String) converter.convertToComplexType(
					newValue, String.class));

		} else if (propertyName.equals("yaxisName")) {
			chart.setYaxisName((String) converter.convertToComplexType(
					newValue, String.class));

		} else if (propertyName.equals("XAxisLabelRotation")) {
			chart.setXAxisLabelRotation((Double) converter
					.convertToComplexType(newValue, Double.class));

		} else if (propertyName.equals("gratuitouslyAnimated")) {
			chart.setGratuitouslyAnimated((Boolean) converter
					.convertToComplexType(newValue, Boolean.class));

		} else if (propertyName.equals("type")) {
			chart.setType((ChartType) converter.convertToComplexType(newValue,
					ChartType.class));

		} else if (propertyName.equals("legendPosition")) {
			chart.setLegendPosition((LegendPosition) converter
					.convertToComplexType(newValue, LegendPosition.class));

		} else if (propertyName.equals("query")) {
			ResultSetProducer rsProducer = (ResultSetProducer) converter
					.convertToComplexType(newValue, ResultSetProducer.class);
			try {
				chart.setQuery(rsProducer);
			} catch (SQLException e) {
				throw new SPPersistenceException(uuid,
						"Cannot commit property query on Chart with name \""
								+ chart.getName() + "\" and UUID \""
								+ chart.getUUID() + "\" for value \""
								+ newValue.toString() + "\"", e);
			}

		} else if (propertyName.equals("backgroundColour")) {
			chart.setBackgroundColour((Color) converter.convertToComplexType(
					newValue, Color.class));

		} else {
			throw new SPPersistenceException(uuid,
					getSPPersistenceExceptionMessage(chart, propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link ChartColumn} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param chartColumn
	 *            The {@link ChartColumn} object to retrieve the named property
	 *            from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getChartColumnProperty(ChartColumn chartColumn,
			String propertyName) throws SPPersistenceException {
		if (propertyName.equals("roleInChart")) {
			return converter.convertToBasicType(chartColumn.getRoleInChart());

		} else if (propertyName.equals("XAxisIdentifier")) {
			return converter.convertToBasicType(chartColumn
					.getXAxisIdentifier());

		} else {
			throw new SPPersistenceException(chartColumn.getUUID(),
					getSPPersistenceExceptionMessage(chartColumn,
							propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitChartColumnProperty(ChartColumn chartColumn,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		if (propertyName.equals("roleInChart")) {
			chartColumn.setRoleInChart((ColumnRole) converter
					.convertToComplexType(newValue, ColumnRole.class));

		} else if (propertyName.equals("XAxisIdentifier")) {
			chartColumn.setXAxisIdentifier((ChartColumn) converter
					.convertToComplexType(newValue, ChartColumn.class));

		} else {
			throw new SPPersistenceException(chartColumn.getUUID(),
					getSPPersistenceExceptionMessage(chartColumn,
							propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link WabitWorkspace} object based on
	 * the property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param wabitImage
	 *            The {@link WabitImage} object to retrieve the named property
	 *            from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getWabitImageProperty(WabitImage wabitImage,
			String propertyName) throws SPPersistenceException {
		String uuid = wabitImage.getUUID();

		if (propertyName.equals("image")) {
			if (wabitImage.getImage() == null) {
				return null;
			}
			return PersisterUtils.convertImageToStreamAsPNG(
					wabitImage.getImage()).toByteArray();

		} else {
			throw new SPPersistenceException(uuid,
					getSPPersistenceExceptionMessage(wabitImage,
							propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method or if
	 *             the new value could not be committed.
	 */
	private void commitWabitImageProperty(WabitImage wabitImage,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		
		if (propertyName.equals("image")) {
			wabitImage.setImage((Image) converter.convertToComplexType(
					newValue, Image.class));

		} else {
			throw new SPPersistenceException(wabitImage.getUUID(),
					getSPPersistenceExceptionMessage(wabitImage,
							propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link Layout} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param layout
	 *            The {@link Layout} object to retrieve the named property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getLayoutProperty(Layout layout, String propertyName)
			throws SPPersistenceException {
		throw new SPPersistenceException(layout.getUUID(),
				getSPPersistenceExceptionMessage(layout, propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitLayoutProperty(Layout layout, String propertyName,
			Object newValue) throws SPPersistenceException {
		throw new SPPersistenceException(layout.getUUID(),
				getSPPersistenceExceptionMessage(layout, propertyName));
	}

	/**
	 * Retrieves a property value from a {@link Page} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param page
	 *            The {@link Page} object to retrieve the named property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getPageProperty(Page page, String propertyName)
			throws SPPersistenceException {
		if (propertyName.equals("height")) {
			return converter.convertToBasicType(page.getHeight());

		} else if (propertyName.equals("width")) {
			return converter.convertToBasicType(page.getWidth());

		} else if (propertyName.equals("orientation")) {
			return converter.convertToBasicType(page.getOrientation());

		} else if (propertyName.equals("defaultFont")) {
			return converter.convertToBasicType(page.getDefaultFont());

		} else {
			throw new SPPersistenceException(page.getUUID(),
					getSPPersistenceExceptionMessage(page, propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitPageProperty(Page page, String propertyName,
			Object newValue) throws SPPersistenceException {
		if (propertyName.equals("height")) {
			page.setHeight((Integer) converter.convertToComplexType(newValue,
					Integer.class));

		} else if (propertyName.equals("width")) {
			page.setWidth((Integer) converter.convertToComplexType(newValue,
					Integer.class));

		} else if (propertyName.equals("orientation")) {
			page.setOrientation((PageOrientation) converter
					.convertToComplexType(newValue, PageOrientation.class));

		} else if (propertyName.equals("defaultFont")) {
			page.setDefaultFont((Font) converter.convertToComplexType(newValue,
					Font.class));

		} else {
			throw new SPPersistenceException(page.getUUID(),
					getSPPersistenceExceptionMessage(page, propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link ContentBox} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param contentBox
	 *            The {@link ContentBox} object to retrieve the named property
	 *            from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getContentBoxProperty(ContentBox contentBox,
			String propertyName) throws SPPersistenceException {
		if (propertyName.equals("height")) {
			return converter.convertToBasicType(contentBox.getHeight());

		} else if (propertyName.equals("width")) {
			return converter.convertToBasicType(contentBox.getWidth());

		} else if (propertyName.equals("x")) {
			return converter.convertToBasicType(contentBox.getX());

		} else if (propertyName.equals("y")) {
			return converter.convertToBasicType(contentBox.getY());

		} else if (propertyName.equals("contentRenderer")) {
			return converter
					.convertToBasicType(contentBox.getContentRenderer());

		} else if (propertyName.equals("font")) {
			return converter.convertToBasicType(contentBox.getFont());

		} else {
			throw new SPPersistenceException(contentBox.getUUID(),
					getSPPersistenceExceptionMessage(contentBox,
							propertyName));
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
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitContentBoxProperty(ContentBox contentBox,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		if (propertyName.equals("height")) {
			contentBox.setHeight((Double) converter.convertToComplexType(
					newValue, Double.class));

		} else if (propertyName.equals("width")) {
			contentBox.setWidth((Double) converter.convertToComplexType(
					newValue, Double.class));

		} else if (propertyName.equals("x")) {
			contentBox.setX((Double) converter.convertToComplexType(newValue,
					Double.class));

		} else if (propertyName.equals("y")) {
			contentBox.setY((Double) converter.convertToComplexType(newValue,
					Double.class));

		} else if (propertyName.equals("font")) {
			contentBox.setFont((Font) converter.convertToComplexType(newValue,
					Font.class));

		} else {
			throw new SPPersistenceException(contentBox.getUUID(),
					getSPPersistenceExceptionMessage(contentBox,
							propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link ChartRenderer} object based on
	 * the property name and converts it to something that can be passed to a
	 * persister. Currently, uncommon properties cannot be retrieved from this
	 * object.
	 * 
	 * @param cRenderer
	 *            The {@link ChartRenderer} object to retrieve the named
	 *            property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getChartRendererProperty(ChartRenderer cRenderer,
			String propertyName) throws SPPersistenceException {
		throw new SPPersistenceException(cRenderer.getUUID(),
				getSPPersistenceExceptionMessage(cRenderer, propertyName));
	}

	/**
	 * Commits a persisted {@link ChartRenderer} object property. Currently,
	 * uncommon properties cannot be persisted for this class.
	 * 
	 * @param cRenderer
	 *            The {@link ChartRenderer} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitChartRendererProperty(ChartRenderer cRenderer,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		throw new SPPersistenceException(cRenderer.getUUID(),
				getSPPersistenceExceptionMessage(cRenderer, propertyName));
	}

	/**
	 * Retrieves a property value from a {@link CellSetRenderer} object based on
	 * the property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param csRenderer
	 *            The {@link CellSetRenderer} object to retrieve the named
	 *            property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getCellSetRendererProperty(CellSetRenderer csRenderer,
			String propertyName) throws SPPersistenceException {

		if (propertyName.equals("bodyAlignment")) {
			return converter.convertToBasicType(csRenderer.getBodyAlignment());

		} else if (propertyName.equals("bodyFormat")) {
			return converter.convertToBasicType(csRenderer.getBodyFormat());

		} else if (propertyName.equals("headerFont")) {
			return converter.convertToBasicType(csRenderer.getHeaderFont());

		} else if (propertyName.equals("bodyFont")) {
			return converter.convertToBasicType(csRenderer.getBodyFont());
			
		} else if (propertyName.equals("backgroundColour")) {
			return converter.convertToBasicType(csRenderer.getBackgroundColour());

		} else {
			throw new SPPersistenceException(csRenderer.getUUID(),
					getSPPersistenceExceptionMessage(csRenderer,
							propertyName));
		}
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitCellSetRendererProperty(CellSetRenderer csRenderer,
			String propertyName, Object newValue)
			throws SPPersistenceException {

		if (propertyName.equals("bodyAlignment")) {
			csRenderer.setBodyAlignment((HorizontalAlignment) converter
					.convertToComplexType(newValue, HorizontalAlignment.class));

		} else if (propertyName.equals("bodyFormat")) {
			csRenderer.setBodyFormat((DecimalFormat) converter
					.convertToComplexType(newValue, DecimalFormat.class));

		} else if (propertyName.equals("headerFont")) {
			csRenderer.setHeaderFont((Font) converter.convertToComplexType(
					newValue, Font.class));

		} else if (propertyName.equals("bodyFont")) {
			csRenderer.setBodyFont((Font) converter.convertToComplexType(
					newValue, Font.class));
			
		} else if (propertyName.equals("backgroundColour")) {
			//This is not implemented yet, placeholder for the future.

		} else {
			throw new SPPersistenceException(csRenderer.getUUID(),
					getSPPersistenceExceptionMessage(csRenderer,
							propertyName));
		}
	}

	/**
	 * Retrieves a property value from an {@link ImageRenderer} object based on
	 * the property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param iRenderer
	 *            The {@link ImageRenderer} object to retrieve the named
	 *            property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getImageRendererProperty(ImageRenderer iRenderer,
			String propertyName) throws SPPersistenceException {
		if (propertyName.equals("image")) {
			return converter.convertToBasicType(iRenderer.getImage());

		} else if (propertyName.equals("preservingAspectRatio")) {
			return converter.convertToBasicType(iRenderer
					.isPreservingAspectRatio());

		} else if (propertyName.equals("preserveAspectRatioWhenResizing")) {
			return converter.convertToBasicType(iRenderer
					.isPreserveAspectRatioWhenResizing());

		} else if (propertyName.equals("HAlign")) {
			return converter.convertToBasicType(iRenderer.getHAlign());

		} else if (propertyName.equals("VAlign")) {
			return converter.convertToBasicType(iRenderer.getVAlign());
			
		} else {
			throw new SPPersistenceException(
					iRenderer.getUUID(),
					getSPPersistenceExceptionMessage(iRenderer, propertyName));
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
	 * @throws SPPersistenceException
	 */
	private void commitImageRendererProperty(ImageRenderer iRenderer,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		
		if (propertyName.equals("image")) {
			iRenderer.setImage((WabitImage) converter.convertToComplexType(
					newValue, WabitImage.class));

		} else if (propertyName.equals("preservingAspectRatio")) {
			iRenderer.setPreservingAspectRatio((Boolean) converter
					.convertToComplexType(newValue, Boolean.class));

		} else if (propertyName.equals("preserveAspectRatioWhenResizing")) {
			iRenderer.setPreserveAspectRatioWhenResizing((Boolean) converter
					.convertToComplexType(newValue, Boolean.class));

		} else if (propertyName.equals("HAlign")) {
			iRenderer.setHAlign((HorizontalAlignment) converter
					.convertToComplexType(newValue, HorizontalAlignment.class));

		} else if (propertyName.equals("VAlign")) {
			iRenderer.setVAlign((VerticalAlignment) converter
					.convertToComplexType(newValue, VerticalAlignment.class));
			
		} else {
			throw new SPPersistenceException(
					iRenderer.getUUID(),
					getSPPersistenceExceptionMessage(iRenderer, propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link Label} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param label
	 *            The {@link Label} object to retrieve the named property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getLabelProperty(Label label, String propertyName)
			throws SPPersistenceException {
		if (propertyName.equals("horizontalAlignment")) {
			return converter.convertToBasicType(label.getHorizontalAlignment());

		} else if (propertyName.equals("verticalAlignment")) {
			return converter.convertToBasicType(label.getVerticalAlignment());

		} else if (propertyName.equals("text")) {
			return converter.convertToBasicType(label.getText());

		} else if (propertyName.equals("backgroundColour")) {
			return converter.convertToBasicType(label.getBackgroundColour());

		} else if (propertyName.equals("font")) {
			return converter.convertToBasicType(label.getFont());

		} else {
			throw new SPPersistenceException(label.getUUID(),
					getSPPersistenceExceptionMessage(label, propertyName));
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
	 * @throws SPPersistenceException
	 */
	private void commitLabelProperty(Label label, String propertyName,
			Object newValue) throws SPPersistenceException {
		if (propertyName.equals("horizontalAlignment")) {
			label.setHorizontalAlignment((HorizontalAlignment) converter
					.convertToComplexType(newValue, HorizontalAlignment.class));

		} else if (propertyName.equals("verticalAlignment")) {
			label.setVerticalAlignment((VerticalAlignment) converter
					.convertToComplexType(newValue, VerticalAlignment.class));

		} else if (propertyName.equals("text")) {
			label.setText((String) converter.convertToComplexType(newValue,
					String.class));

		} else if (propertyName.equals("backgroundColour")) {
			label.setBackgroundColour((Color) converter.convertToComplexType(
					newValue, Color.class));

		} else if (propertyName.equals("font")) {
			label.setFont((Font) converter.convertToComplexType(newValue,
					Font.class));

		} else {
			throw new SPPersistenceException(label.getUUID(),
					getSPPersistenceExceptionMessage(label, propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link ReportTask} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param task
	 *            The {@link ReportTask} object to retrieve the named property
	 *            from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getReportTaskProperty(ReportTask task, String propertyName)
			throws SPPersistenceException {

		if (propertyName.equals("email")) {
			return converter.convertToBasicType(task.getEmail());
		} else if (propertyName.equals("report")) {
			return converter.convertToBasicType(task.getReport());
		} else if (propertyName.equals("triggerType")) {
			return converter.convertToBasicType(task.getTriggerType());
		} else if (propertyName.equals("triggerHourParam")) {
			return converter.convertToBasicType(task.getTriggerHourParam());
		} else if (propertyName.equals("triggerMinuteParam")) {
			return converter.convertToBasicType(task.getTriggerMinuteParam());
		} else if (propertyName.equals("triggerDayOfWeekParam")) {
			return converter
					.convertToBasicType(task.getTriggerDayOfWeekParam());
		} else if (propertyName.equals("triggerDayOfMonthParam")) {
			return converter.convertToBasicType(task
					.getTriggerDayOfMonthParam());
		} else if (propertyName.equals("triggerIntervalParam")) {
			return converter.convertToBasicType(task.getTriggerIntervalParam());

		} else {
			throw new SPPersistenceException(task.getUUID(),
					"Unknown property " + propertyName
							+ " for ReportTask with name " + task.getName());
		}
	}

	/**
	 * Commits a persisted {@link ReportTask} object property
	 * 
	 * @param task
	 *            The {@link ReportTask} object to commit the persisted property
	 *            upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitReportTaskProperty(ReportTask task, String propertyName,
			Object newValue) throws SPPersistenceException {

		if (propertyName.equals("email")) {
			task.setEmail((String) converter.convertToComplexType(newValue,
					String.class));
		} else if (propertyName.equals("report")) {
			task.setReport((Report) converter.convertToComplexType(newValue,
					Report.class));
		} else if (propertyName.equals("triggerType")) {
			task.setTriggerType((String) converter.convertToComplexType(
					newValue, String.class));
		} else if (propertyName.equals("triggerHourParam")) {
			task.setTriggerHourParam((Integer) converter.convertToComplexType(
					newValue, Integer.class));
		} else if (propertyName.equals("triggerMinuteParam")) {
			task.setTriggerMinuteParam((Integer) converter
					.convertToComplexType(newValue, Integer.class));
		} else if (propertyName.equals("triggerDayOfWeekParam")) {
			task.setTriggerDayOfWeekParam((Integer) converter
					.convertToComplexType(newValue, Integer.class));
		} else if (propertyName.equals("triggerDayOfMonthParam")) {
			task.setTriggerDayOfMonthParam((Integer) converter
					.convertToComplexType(newValue, Integer.class));
		} else if (propertyName.equals("triggerIntervalParam")) {
			task.setTriggerIntervalParam((Integer) converter
					.convertToComplexType(newValue, Integer.class));

		} else {
			throw new SPPersistenceException(task.getUUID(),
					"Unknown property " + propertyName
							+ " for ReportTask with name " + task.getName());
		}
	}

	/**
	 * Retrieves a property value from a {@link ResultSetRenderer} object based
	 * on the property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param rsRenderer
	 *            The {@link ResultSetRenderer} object to retrieve the named
	 *            property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getResultSetRendererProperty(ResultSetRenderer rsRenderer,
			String propertyName) throws SPPersistenceException {
		if (propertyName.equals("nullString")) {
			return converter.convertToBasicType(rsRenderer.getNullString());

		} else if (propertyName.equals("borderType")) {
			return converter.convertToBasicType(rsRenderer.getBorderType());

		} else if (propertyName.equals("backgroundColour")) {
			return converter.convertToBasicType(rsRenderer
					.getBackgroundColour());

		} else if (propertyName.equals("headerFont")) {
			return converter.convertToBasicType(rsRenderer.getHeaderFont());

		} else if (propertyName.equals("bodyFont")) {
			return converter.convertToBasicType(rsRenderer.getBodyFont());

		} else if (propertyName.equals("printingGrandTotals")) {
			return converter.convertToBasicType(rsRenderer
					.isPrintingGrandTotals());

		} else {
			throw new SPPersistenceException(rsRenderer.getUUID(),
					getSPPersistenceExceptionMessage(rsRenderer,
							propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitResultSetRendererProperty(ResultSetRenderer rsRenderer,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		if (propertyName.equals("nullString")) {
			rsRenderer.setNullString((String) converter.convertToComplexType(
					newValue, String.class));

		} else if (propertyName.equals("borderType")) {
			rsRenderer.setBorderType((BorderStyles) converter
					.convertToComplexType(newValue, BorderStyles.class));

		} else if (propertyName.equals("backgroundColour")) {
			rsRenderer.setBackgroundColour((Color) converter
					.convertToComplexType(newValue, Color.class));

		} else if (propertyName.equals("headerFont")) {
			rsRenderer.setHeaderFont((Font) converter.convertToComplexType(
					newValue, Font.class));

		} else if (propertyName.equals("bodyFont")) {
			rsRenderer.setBodyFont((Font) converter.convertToComplexType(
					newValue, Font.class));

		} else if (propertyName.equals("printingGrandTotals")) {
			rsRenderer.setPrintingGrandTotals((Boolean) converter
					.convertToComplexType(newValue, Boolean.class));

		} else {
			throw new SPPersistenceException(rsRenderer.getUUID(),
					getSPPersistenceExceptionMessage(rsRenderer,
							propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link ColumnInfo} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param colInfo
	 *            The {@link ColumnInfo} object to retrieve the named property
	 *            from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getColumnInfoProperty(ColumnInfo colInfo, String propertyName)
			throws SPPersistenceException {
		String uuid = colInfo.getUUID();

		if (propertyName.equals(ColumnInfo.COLUMN_ALIAS)) {
			return converter.convertToBasicType(colInfo.getColumnAlias());

		} else if (propertyName.equals(ColumnInfo.WIDTH_CHANGED)) {
			return converter.convertToBasicType(colInfo.getWidth());

		} else if (propertyName.equals(ColumnInfo.HORIZONAL_ALIGNMENT_CHANGED)) {
			return converter.convertToBasicType(colInfo
					.getHorizontalAlignment());

		} else if (propertyName.equals(ColumnInfo.DATATYPE_CHANGED)) {
			return converter.convertToBasicType(colInfo.getDataType());

		} else if (propertyName.equals(ColumnInfo.WILL_GROUP_OR_BREAK_CHANGED)) {
			return converter.convertToBasicType(colInfo.getWillGroupOrBreak());

		} else if (propertyName.equals(ColumnInfo.WILL_SUBTOTAL_CHANGED)) {
			return converter.convertToBasicType(colInfo.getWillSubtotal());

		} else if (propertyName.equals(ColumnInfo.FORMAT_CHANGED)) {
			return converter.convertToBasicType(colInfo.getFormat());

		} else {
			throw new SPPersistenceException(uuid,
					getSPPersistenceExceptionMessage(colInfo, propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method or if
	 *             the new property value cannot be committed.
	 */
	private void commitColumnInfoProperty(ColumnInfo colInfo,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		String uuid = colInfo.getUUID();
		if (propertyName.equals(ColumnInfo.COLUMN_ALIAS)) {
			colInfo.setColumnAlias((String) converter.convertToComplexType(
					newValue, String.class));

		} else if (propertyName.equals(ColumnInfo.WIDTH_CHANGED)) {
			colInfo.setWidth((Integer) converter.convertToComplexType(newValue,
					Integer.class));

		} else if (propertyName.equals(ColumnInfo.HORIZONAL_ALIGNMENT_CHANGED)) {
			colInfo.setHorizontalAlignment((HorizontalAlignment) converter
					.convertToComplexType(newValue, HorizontalAlignment.class));

		} else if (propertyName.equals(ColumnInfo.DATATYPE_CHANGED)) {
			colInfo.setDataType((ca.sqlpower.wabit.report.DataType) converter
					.convertToComplexType(newValue,
							ca.sqlpower.wabit.report.DataType.class));

		} else if (propertyName.equals(ColumnInfo.WILL_GROUP_OR_BREAK_CHANGED)) {
			colInfo.setWillGroupOrBreak((GroupAndBreak) converter
					.convertToComplexType(newValue, GroupAndBreak.class));

		} else if (propertyName.equals(ColumnInfo.WILL_SUBTOTAL_CHANGED)) {
			colInfo.setWillSubtotal((Boolean) converter.convertToComplexType(
					newValue, Boolean.class));

		} else if (propertyName.equals(ColumnInfo.FORMAT_CHANGED)) {
			colInfo.setFormat((Format) converter.convertToComplexType(newValue,
					Format.class));

		} else {
			throw new SPPersistenceException(uuid,
					getSPPersistenceExceptionMessage(colInfo, propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link Guide} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param guide
	 *            The {@link Guide} object to retrieve the named property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getGuideProperty(Guide guide, String propertyName)
			throws SPPersistenceException {
		if (propertyName.equals("offset")) {
			return converter.convertToBasicType(guide.getOffset());

		} else {
			throw new SPPersistenceException(guide.getUUID(),
					getSPPersistenceExceptionMessage(guide, propertyName));
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
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitGuideProperty(Guide guide, String propertyName,
			Object newValue) throws SPPersistenceException {
		if (propertyName.equals("offset")) {
			guide.setOffset((Double) converter.convertToComplexType(newValue,
					Double.class));

		} else {
			throw new SPPersistenceException(guide.getUUID(),
					getSPPersistenceExceptionMessage(guide, propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link User} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param user
	 *            The {@link User} object to retrieve the named property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getUserProperty(User user, String propertyName)
			throws SPPersistenceException {
		if (propertyName.equals("password")) {
			return converter.convertToBasicType(user.getPassword());
		} else if (propertyName.equals("fullName")) {
			return converter.convertToBasicType(user.getFullName());
		} else if (propertyName.equals("email")) {
			return converter.convertToBasicType(user.getEmail());
		} else {
			throw new SPPersistenceException(user.getUUID(),
					getSPPersistenceExceptionMessage(user, propertyName));
		}
	}

	/**
	 * Commits a persisted {@link User} object property
	 * 
	 * @param user
	 *            The {@link User} object to commit the persisted property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitUserProperty(User user, String propertyName,
			Object newValue) throws SPPersistenceException {

		if (propertyName.equals("password")) {
			user.setPassword((String) converter.convertToComplexType(newValue,
					String.class));
		} else if (propertyName.equals("email")) {
			user.setEmail((String) converter.convertToComplexType(newValue,
					String.class));
		} else if (propertyName.equals("fullName")) {
			user.setFullName((String) converter.convertToComplexType(newValue,
					String.class));
		} else {
			throw new SPPersistenceException(user.getUUID(),
					getSPPersistenceExceptionMessage(user, propertyName));
		}
	}

	/**
	 * Retrieves a property value from a {@link Group} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister. Currently, uncommon properties cannot be retrieved from this
	 * class.
	 * 
	 * @param group
	 *            The {@link Group} object to retrieve the named property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getGroupProperty(Group group, String propertyName)
			throws SPPersistenceException {
		throw new SPPersistenceException(group.getUUID(),
				getSPPersistenceExceptionMessage(group, propertyName));
	}

	/**
	 * Commits a persisted {@link Group} property. Currently, uncommon
	 * properties cannot be persisted for this class.
	 * 
	 * @param group
	 *            The {@link Group} object to commit the persisted property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitGroupProperty(Group group, String propertyName,
			Object newValue) throws SPPersistenceException {
		throw new SPPersistenceException(group.getUUID(),
				getSPPersistenceExceptionMessage(group, propertyName));
	}

	/**
	 * Retrieves a property value from a {@link GroupMember} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister. Currently, uncommon properties cannot be retrieved from this
	 * class.
	 * 
	 * @param groupMember
	 *            The {@link GroupMember} object to retrieve the named property
	 *            from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getGroupMemberProperty(GroupMember groupMember,
			String propertyName) throws SPPersistenceException {
		throw new SPPersistenceException(groupMember.getUUID(),
				getSPPersistenceExceptionMessage(groupMember, propertyName));
	}

	/**
	 * Commits a persisted {@link GroupMember} property. Currently, uncommon
	 * properties cannot be persisted for this class.
	 * 
	 * @param groupMember
	 *            The {@link GroupMember} object to commit the persisted
	 *            property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitGroupMemberProperty(GroupMember groupMember,
			String propertyName, Object newValue)
			throws SPPersistenceException {
		throw new SPPersistenceException(groupMember.getUUID(),
				getSPPersistenceExceptionMessage(groupMember, propertyName));
	}

	/**
	 * Retrieves a property value from a {@link Grant} object based on the
	 * property name and converts it to something that can be passed to a
	 * persister.
	 * 
	 * @param grant
	 *            The {@link v} object to retrieve the named property from.
	 * @param propertyName
	 *            The property name that needs to be retrieved and converted.
	 *            This is the name of the property in the class itself based on
	 *            the property fired by the setter for the event which is
	 *            enforced by tests using JavaBeans methods even though the
	 *            values are hard coded in here and won't change if the class
	 *            changes.
	 * @return The value stored in the variable of the object we are given at
	 *         the property name after it has been converted to a type that can
	 *         be stored. The conversion is based on the
	 *         {@link SessionPersisterSuperConverter}.
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private Object getGrantProperty(Grant grant, String propertyName)
			throws SPPersistenceException {
		// Grants are immutable
		throw new SPPersistenceException(grant.getUUID(),
				getSPPersistenceExceptionMessage(grant, propertyName));
	}

	/**
	 * Commits a persisted {@link Grant} property.
	 * 
	 * @param grant
	 *            The {@link Grant} object to commit the persisted property upon
	 * @param propertyName
	 *            The property name
	 * @param newValue
	 *            The persisted property value to be committed
	 * @throws SPPersistenceException
	 *             Thrown if the property name is not known in this method.
	 */
	private void commitGrantProperty(Grant grant, String propertyName,
			Object newValue) throws SPPersistenceException {
		// Grants are immutable
		throw new SPPersistenceException(grant.getUUID(),
				getSPPersistenceExceptionMessage(grant, propertyName));
	}

	/**
	 * Removes {@link WabitObject}s from persistent storage.
	 * 
	 * @param parentUUID
	 *            The parent UUID of the {@link WabitObject} to remove
	 * @param uuid
	 *            The UUID of the {@link WabitObject} to remove
	 * @throws SPPersistenceException
	 */
	public void removeObject(String parentUUID, String uuid)
			throws SPPersistenceException {
		synchronized (session) {
			this.enforeThreadSafety();
			logger.debug(String.format("wsp.removeObject(\"%s\", \"%s\");",
					parentUUID, uuid));
			if (this.transactionCount==0) {
				logger.error("Remove Object attempted while not in a transaction. Rollback initiated.");
				this.rollback();
				throw new SPPersistenceException(uuid,"Remove Object attempted while not in a transaction. Rollback initiated.");
			}
			if (!exists(uuid)) {
				this.rollback();
				throw new SPPersistenceException(uuid,
						"Cannot remove the WabitObject with UUID " + uuid
						+ " from parent UUID " + parentUUID
						+ " as it does not exist.");
			}
			objectsToRemove.put(uuid, parentUUID);
		}
	}

	public void rollback() {
		this.rollback(false);
	}
	
	/**
	 * Rollback all changes to persistent storage to the beginning of the
	 * transaction
	 * 
	 * @pa
	 * 
	 * @throws SPPersistenceException
	 */
	public void rollback(boolean force) {
		final WabitWorkspace workspace = session.getWorkspace();
		synchronized (workspace) {
			if (this.headingToWisconsin) {
				return;
			}
			this.headingToWisconsin = true;
			if (!force) {
				this.enforeThreadSafety();
			}
			try {
				// We catch ANYTHING that comes out of here and rollback.
				// Some exceptions are Runtimes, so we must catch those too.
				workspace.begin(null);
				rollbackProperties();
				rollbackCreations();
				rollbackRemovals();
				workspace.commit();
			} catch (Throwable t2) {
				// This is a major fuck up. We could not rollback so now we must restore
				// by whatever means
				logger.fatal("First try at restore failed.", t2);
				// TODO Monitor this
			} finally {
				this.objectsToRemove.clear();
				this.objectsToRemoveRollbackList.clear();
				this.persistedObjects.clear();
				this.persistedObjectsRollbackList.clear();
				this.persistedProperties.clear();
				this.persistedPropertiesRollbackList.clear();
				transactionCount = 0;
				this.headingToWisconsin = false;
				logger.debug("wsp.rollback(); - Killed all current transactions.");
			}
		}
	}
	
	public boolean isHeadingToWisconsin() {
		return headingToWisconsin;
	}

	/**
	 * This is part of the 'echo-cancellation' system to notify any
	 * {@link WorkspacePersisterListener} listening to the same session to
	 * ignore modifications to that session.
	 */
	public boolean isUpdatingWabitWorkspace() {
		if (transactionCount > 0) {
			return true;
		} else if (transactionCount == 0) {
			return false;
		} else {
			this.rollback();
			throw new IllegalStateException("This persister is in an illegal state. transactionCount was :"+transactionCount);
		}
	}

	public void enforeThreadSafety() {
		if (this.currentThread == null) {
			this.currentThread = Thread.currentThread();
		} else {
			if (this.currentThread!=Thread.currentThread()) {
				this.rollback(true);
				throw new RuntimeException("A call from two different threads was detected. Callers of a sessionPersister should synchronize prior to opening transactions.");
			}
		}
	}
	
	
	/**
	 * Turns this persister as a preacher of the truth and
	 * always the truth. All calls are turned into unconditionals.
	 * @param godMode True or False
	 */
	public void setGodMode(boolean godMode) {
		this.godMode = godMode;
	}
	
	/**
	 * This static accessible method allows 
	 * @param session
	 * @param creations
	 * @param properties
	 * @param removals
	 * @throws SPPersistenceException
	 */
	public static void undoForSession(
			WabitSession session,
			List<PersistedObjectEntry> creations,
			List<PersistedPropertiesEntry> properties,
			List<RemovedObjectEntry> removals) throws SPPersistenceException
	{
		WabitSessionPersister persister = new WabitSessionPersister("undoer", session);
		persister.setGodMode(true);
		persister.setObjectsToRemoveRollbackList(removals);
		persister.setPersistedObjectsRollbackList(creations);
		persister.setPersistedPropertiesRollbackList(properties);
		persister.rollback(true);
	}
	
	private void setObjectsToRemoveRollbackList(
			List<RemovedObjectEntry> objectsToRemoveRollbackList) {
		this.objectsToRemoveRollbackList = objectsToRemoveRollbackList;
	}
	
	private void setPersistedObjectsRollbackList(
			List<PersistedObjectEntry> persistedObjectsRollbackList) {
		this.persistedObjectsRollbackList = persistedObjectsRollbackList;
	}
	
	private void setPersistedPropertiesRollbackList(
			List<PersistedPropertiesEntry> persistedPropertiesRollbackList) {
		this.persistedPropertiesRollbackList = persistedPropertiesRollbackList;
	}
}
