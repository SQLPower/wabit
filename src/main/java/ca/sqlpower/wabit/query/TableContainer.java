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

package ca.sqlpower.wabit.query;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * A model that stores SQLTable elements. This will store objects of a defined type and
 * can be grouped when adding the items to the model.
 *
 * @param <C> The type of object this model will store.
 */
public class TableContainer extends AbstractWabitObject implements Container {
	
	private static final Logger logger = Logger.getLogger(TableContainer.class);

	private SQLTable table;
	
	/**
	 * The list contains all of the columns of the table.
	 */
	private final List<Item> itemList;
	
	private String alias;
	
	private Point2D position;
	
	/**
	 * The catalog that the SQLTable contained in this container belongs to.
	 */
	private final String catalog;
	
	/**
	 * The schema that the SQLTable contained in this container belongs to.
	 */
	private final String schema;

	/**
	 * This is the cache this container is contained in.
	 */
	private final QueryCache cache;
	
	public TableContainer(QueryCache cache, SQLTable t) {
		this.cache = cache;
		table = t;
		schema = table.getSchemaName();
		catalog = table.getCatalogName();
		alias = "";
		itemList = new ArrayList<Item>();
		loadColumnsFromTable(t);
		position = new Point2D.Double(0, 0);
	}

	/**
	 * This will create the items from the columns for the table. This
	 * needs to be called right after the table gets set.
	 */
	private void loadColumnsFromTable(SQLTable t) {
		try {
			for (Object child : t.getColumnsFolder().getChildren()) {
				boolean itemFound = false;
				for (Item item : itemList) {
					if (item.getName().equals(((SQLColumn) child).getName())) {
						((SQLObjectItem) item).setItem((SQLColumn) child);
						itemFound = true;
						break;
					}
				}
				if (itemFound) {
					continue;
				}
				SQLObjectItem item = new SQLObjectItem((SQLObject) child);
				item.setParent(this);
				itemList.add(item);
				fireChildAdded(SQLObjectItem.class, item, itemList.indexOf(item));
			}
		} catch (ArchitectException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * This constructor creates a table that will be loaded from the database when a part of the
	 * container is accessed. To load the table the table's name, schema and catalog will be used
	 * to retrieve the table from the database. The items of this container will have it's object
	 * set when the table is loaded.
	 */
	public TableContainer(QueryCache cache, String name, String schema, String catalog, List<SQLObjectItem> items) {
		this.cache = cache;
		this.schema = schema;
		this.catalog = catalog;
		setName(name);
		table = null;
		alias = "";
		itemList = new ArrayList<Item>();
		for (Item item : items) {
			item.setParent(this);
			itemList.add(item);
			fireChildAdded(SQLObjectItem.class, item, itemList.indexOf(item));
		}
		position = new Point2D.Double(0, 0);
	}
	
	public List<Item> getItems() {
		loadTableByQualifiedName();
		return Collections.unmodifiableList(itemList);
	}
	
	public String getName() {
		loadTableByQualifiedName();
		return table.getName();
	}

	public String getAlias() {
		return alias;
	}
	
	/**
	 * Sets the alias of the container. Null is not allowed.
	 */
	public void setAlias(String alias) {
		String oldAlias = this.alias;
		if (alias.equals(oldAlias)) {
			return;
		}
		this.alias = alias;
		firePropertyChange(CONTAINTER_ALIAS_CHANGED, oldAlias, alias);
		logger.debug("Alias set to " + alias);
	}

	public Item getItem(Object item) {
		loadTableByQualifiedName();
		for (Item i : itemList) {
			if (i.getItem() == item) {
				return i;
			}
		}
		return null;
	}

	public Object getContainedObject() {
		loadTableByQualifiedName();
		return table;
	}

	public void addItem(Item item) {
		throw new IllegalStateException("Cannot add arbitrary items to a SQLObject.");		
	}

	public void removeItem(Item item) {
		throw new IllegalStateException("Cannot remove arbitrary items from a SQLObject.");		
	}

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D position) {
		this.position = position;
	}

	public boolean allowsChildren() {
		return true;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		return 0;
	}

	public List<? extends WabitObject> getChildren() {
		loadTableByQualifiedName();
		return itemList;
	}

	public String getSchema() {
		return schema;
	}
	
	public String getCatalog() {
		return catalog;
	}
	
	/**
	 * This will load the table from the query's data source as necessary
	 * if it is null and the qualified name is not null.
	 * <p>
	 * This is package private to allow the container's items to load their
	 * parent table and theirselves. 
	 */
	void loadTableByQualifiedName() {
		if (table == null) {
			SQLDatabase db = new SQLDatabase(cache.getDataSource());
			try {
				table = db.getTableByName(catalog, schema, super.getName());
			} catch (ArchitectException e) {
				throw new RuntimeException(e);
			}
			loadColumnsFromTable(table);
		}
	}

}
