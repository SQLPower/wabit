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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.table.TableModelSortDecorator;
import ca.sqlpower.wabit.query.Container;
import ca.sqlpower.wabit.query.Item;
import ca.sqlpower.wabit.query.QueryCache;
import ca.sqlpower.wabit.query.SQLJoin;
import ca.sqlpower.wabit.query.StringCountItem;
import ca.sqlpower.wabit.query.QueryCache.OrderByArgument;
import ca.sqlpower.wabit.swingui.querypen.QueryPen;

/**
 * This is the controller between the QueryCache and the QueryPen.
 * It will pass relevant events from the QueryPen to the QueryCache
 * like Containers being added and removed.
 */
public class QueryController {
	
	private static final Logger logger = Logger.getLogger(QueryController.class);
	
	private final QueryCache queryCache;

	/**
	 * This is the current cell renderer we are listening on for group by 
	 * and having values.
	 */
	private ComponentCellRenderer cellRenderer;

	private final PropertyChangeListener fromChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(Container.PROPERTY_TABLE_ADDED)) {
				queryCache.addTable((Container)evt.getNewValue());
			} else if (evt.getPropertyName().equals(Container.PROPERTY_TABLE_REMOVED)) {
				queryCache.removeTable((Container)evt.getOldValue());
			}
		}
	};
	
	private final PropertyChangeListener tableItemListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(Container.CONTAINTER_ITEM_ADDED)) {
				Item item = (Item)evt.getNewValue();
				queryCache.addItem(item);
				logger.debug("Item " + item + " added to the query cache");
			} else if (evt.getPropertyName().equals(Container.CONTAINER_ITEM_REMOVED)) {
				Item item = (Item)evt.getOldValue();
				queryCache.removeItem(item);
			}
		}
	};

	private final PropertyChangeListener joinChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(SQLJoin.PROPERTY_JOIN_ADDED)) {
				queryCache.addJoin((SQLJoin)e.getNewValue());
			} else if (e.getPropertyName().equals(SQLJoin.PROPERTY_JOIN_REMOVED)) {
				SQLJoin joinLine = (SQLJoin) e.getOldValue();
				queryCache.removeJoin(joinLine);
			}
		}
	};
	
	private final PropertyChangeListener whereListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Container.PROPERTY_WHERE_MODIFIED)) {
				queryCache.setGlobalWhereClause((String)e.getNewValue());
			}
		}
	};
	
	/**
	 * This handles listening for order by changes in the sort decorator of a component
	 * cell renderer.
	 */
	private TableModelListener orderByListener = new TableModelListener() {
	
		public void tableChanged(TableModelEvent e) {
			TableModelSortDecorator sortDecorator = (TableModelSortDecorator)e.getSource();
			queryCache.startCompoundEdit();
			for (int i = 0; i < sortDecorator.getColumnCount(); i++) {
				int sortStatus = sortDecorator.getSortingStatus(i);
				Item column = queryCache.getSelectedColumns().get(i);
				OrderByArgument orderByArgument = queryCache.getOrderByArgumentMap().get(column);
				if ((sortStatus == TableModelSortDecorator.NOT_SORTED && orderByArgument == null)
						|| (sortStatus == TableModelSortDecorator.ASCENDING && orderByArgument == OrderByArgument.ASC)
						|| (sortStatus == TableModelSortDecorator.DESCENDING && orderByArgument == OrderByArgument.DESC)) {
					if (sortStatus != TableModelSortDecorator.NOT_SORTED) {
						logger.debug("Column " + column.getName() + " is sorted by type " + sortStatus + " and has a stored sort order of " + orderByArgument);
					}
					continue;
				}
				
				if (sortStatus == TableModelSortDecorator.NOT_SORTED) {
					queryCache.removeSort(column);
				} else if (sortStatus == TableModelSortDecorator.ASCENDING) {
					logger.debug("Setting sort order of " + column.getName() + " to ascending.");
					queryCache.setSortOrder(column, OrderByArgument.ASC);
				} else if (sortStatus == TableModelSortDecorator.DESCENDING) {
					logger.debug("Setting sort order of " + column.getName() + " to descending.");
					queryCache.setSortOrder(column, OrderByArgument.DESC);
				} else {
					throw new IllegalStateException("The column " + column.getName() + " was sorted in an unknown way");
				}
			}
			queryCache.endCompoundEdit();
		}
	};
	
	/**
	 * A listener that handles changes to the group by and having clauses.
	 */
	private PropertyChangeListener groupByAndHavingListener = new PropertyChangeListener() {
	
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(ComponentCellRenderer.PROPERTY_GROUP_BY)) {
				Item column = queryCache.getSelectedColumns().get(cellRenderer.getComboBoxes().indexOf((JComboBox)e.getSource()));
				if(column instanceof StringCountItem) {
					logger.debug("this column is a StringCountItem, we will only setGrouping to Count");
					queryCache.setGrouping(column, "COUNT");
				} else {
					queryCache.setGrouping(column, (String)e.getNewValue());
				}
			} else if (e.getPropertyName().equals(ComponentCellRenderer.PROPERTY_HAVING)) {
				String newValue = (String)e.getNewValue();
				int indexOfTextField = cellRenderer.getTextFields().indexOf((JTextField)e.getSource());
				if (indexOfTextField < 0) {
					return;
				}
				Item item = queryCache.getSelectedColumns().get(indexOfTextField);
				queryCache.setHavingClause(item, newValue);
			}

		}
	};
	
	/**
	 * This is the last column move in the result table registered by the
	 * reorderSelectionByHeaderListener listener. This will be null if there was
	 * no column move since the last time a table column was dragged.
	 */
	private TableColumnModelEvent lastTableColumnMove = null;

	/**
	 * This listens to the mouse releases on a table to know when to try and
	 * handle a table column move. The table columns should only be moved after
	 * the user is done dragging.
	 */
	private final MouseListener reorderSelectionByHeaderMouseListener = new MouseAdapter() {

		@Override
		public void mouseReleased(MouseEvent e) {
			if (lastTableColumnMove != null) {
				logger.debug("Moving column in select from " + lastTableColumnMove.getFromIndex() + " to " + lastTableColumnMove.getToIndex());
				Item movedColumn = queryCache.getSelectedColumns().get(lastTableColumnMove.getFromIndex());
				queryCache.moveItem(movedColumn, lastTableColumnMove.getToIndex());
				lastTableColumnMove = null;
			}
		}
	};
	
	/**
	 * This will listen to the table for column reordering and update the select statement accordingly.
	 */
	private final TableColumnModelListener reorderSelectionByHeaderListener = new TableColumnModelListener() {
		public void columnSelectionChanged(ListSelectionEvent e) {
			//do nothing
		}
		public void columnRemoved(TableColumnModelEvent e) {
			//do nothing	
		}
		public void columnMoved(TableColumnModelEvent e) {
			if (e.getToIndex() != e.getFromIndex()) {
				if (lastTableColumnMove == null) {
					lastTableColumnMove = e;
				} else {
					lastTableColumnMove = new TableColumnModelEvent((TableColumnModel)e.getSource(), lastTableColumnMove.getFromIndex(), e.getToIndex());
				}
			}
		}
		public void columnMarginChanged(ChangeEvent e) {
			//do nothing
		}
		public void columnAdded(TableColumnModelEvent e) {
			//do nothing
		}
	};
	
	/**
	 * This listens to changes in the data source combo box and updates the
	 * query cache appropriately.
	 */
	private final ActionListener dataSourceListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Object selectedItem = dataSourceComboBox.getSelectedItem();
			if (selectedItem != null && !(selectedItem instanceof SPDataSource)) {
				throw new IllegalStateException("The data source combo box does not have data sources in it.");
			}
			queryCache.setDataSource((SPDataSource) selectedItem);
			if (logger.isDebugEnabled()) {
				logger.debug("Data source in the model is " + ((SPDataSource) selectedItem).getName());
			}
		}
	};
	
	/**
	 * This listener will set the query cache text to the user defined query.
	 */
	private final DocumentListener queryTextListener = new DocumentListener() {
		public void removeUpdate(DocumentEvent e) {
			queryCache.defineUserModifiedQuery(queryText.getText());
		}
		public void insertUpdate(DocumentEvent e) {
			queryCache.defineUserModifiedQuery(queryText.getText());	
		}
		public void changedUpdate(DocumentEvent e) {
			queryCache.defineUserModifiedQuery(queryText.getText());	
		}
	};

	/**
	 * The combo box that holds all of the data sources available to the query.
	 * This combo box should also allow the selection of which data source
	 * the query is executing on.
	 */
	private final JComboBox dataSourceComboBox;

	/**
	 * The query pen this controller is listening to.
	 */
	private final QueryPen pen;

	/**
	 * This is the text component that the user can edit to manually
	 * edit the SQL query.
	 */
	private final JTextComponent queryText;

	private final JSlider zoomSlider;

	private final ChangeListener zoomListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			queryCache.setZoomLevel(zoomSlider.getValue());
		}
	};
	
	/**
	 * This constructor will attach listeners to the {@link QueryPen} to update
	 * the state of the {@link QueryCache}. The dataSourceComboBox will also have
	 * a listener added so the {@link QueryCache} can track which database to execute
	 * on.
	 */
	public QueryController(QueryCache cache, QueryPen pen, JComboBox dataSourceComboBox, JTextComponent textComponent, JSlider zoomSlider) {
		queryCache = cache;
		this.pen = pen;
		this.dataSourceComboBox = dataSourceComboBox;
		queryText = textComponent;
		this.zoomSlider = zoomSlider;
		pen.addQueryListener(fromChangeListener);
		pen.addQueryListener(joinChangeListener);
		pen.addQueryListener(whereListener);
		pen.addQueryListener(tableItemListener);
		dataSourceComboBox.addActionListener(dataSourceListener);
		queryText.getDocument().addDocumentListener(queryTextListener);
		zoomSlider.addChangeListener(zoomListener );
	}
	
	/**
	 * This disconnects the QueryController from the QueryPen it started to listen to when it
	 * was created. This should be called when this controller is no longer needed.
	 */
	public void disconnect() {
		pen.removeQueryListener(fromChangeListener);
		pen.removeQueryListener(joinChangeListener);
		pen.removeQueryListener(whereListener);
		pen.removeQueryListener(tableItemListener);
		dataSourceComboBox.removeActionListener(dataSourceListener);
		queryText.getDocument().removeDocumentListener(queryTextListener);
		zoomSlider.removeChangeListener(zoomListener);
		unlistenToCellRenderer();
	}
	
	public void listenToCellRenderer(ComponentCellRenderer renderer) {
		unlistenToCellRenderer();
		cellRenderer = renderer;
		renderer.addGroupAndHavingListener(groupByAndHavingListener);
		renderer.addTableListenerToSortDecorator(orderByListener);
		renderer.getTable().getColumnModel().addColumnModelListener(reorderSelectionByHeaderListener);
		renderer.getTable().getTableHeader().addMouseListener(reorderSelectionByHeaderMouseListener);
	}
	
	public void unlistenToCellRenderer() {
		if (cellRenderer != null) {
			cellRenderer.removeGroupAndHavingListener(groupByAndHavingListener);
			cellRenderer.removeTableListenerToSortDecorator(orderByListener);
			cellRenderer.getTable().getColumnModel().removeColumnModelListener(reorderSelectionByHeaderListener);
			cellRenderer.getTable().getTableHeader().removeMouseListener(reorderSelectionByHeaderMouseListener);
		}
	}
	
}
