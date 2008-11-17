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

package ca.sqlpower.wabit.dao;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import ca.sqlpower.sql.SQLGroupFunction;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.wabit.query.Container;
import ca.sqlpower.wabit.query.Item;
import ca.sqlpower.wabit.query.QueryCache;
import ca.sqlpower.wabit.query.SQLJoin;
import ca.sqlpower.xml.XMLHelper;

public class ProjectXMLDAO {

	/**
	 * This output stream will be used to  write the project to a file.
	 */
	private final PrintWriter out;
	
	/**
	 * This XML helper will do the formatting and outputting of the XML that
	 * creates our save file.
	 */
	private final XMLHelper xml;
	
	public ProjectXMLDAO(OutputStream out) {
		this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
		xml = new XMLHelper();
	}
	
	public void save() {
		xml.println(out, "<?xml version='1.0' encoding='UTF-8'?>");
		xml.println(out, "");
		xml.println(out, "<wabit export-format=\"1.0.0\">");
		xml.indent++;

		//TODO: Actually save something on save
		
		xml.indent--;
		xml.println(out, "</wabit>");
		out.flush();
		out.close();
	}
	
	public void saveQueryCache(QueryCache cache) {
		xml.print(out, "<query");
		printAttribute("name", cache.getName());
		printAttribute("data-source", cache.getDataSource().getName());
		xml.println(out, ">");
		xml.indent++;

		Map<Item, String> itemIdMap = new HashMap<Item, String>();

		int columnId = 0;
		
		xml.print(out, "<constants");
		Container constants = cache.getConstantsContainer();
		printAttribute("xpos", constants.getPosition().getX());
		printAttribute("ypos", constants.getPosition().getY());
		xml.println(out, ">");
		xml.indent++;
		for (Item item : constants.getItems()) {
			xml.print(out, "<column");
			String ident = "COL" + columnId;
			printAttribute("id", ident);
			itemIdMap.put(item, ident);
			printAttribute("name", item.getName());
			printAttribute("alias", item.getAlias());
			printAttribute("where-text", item.getWhere());
			xml.println(out, "/>");
			columnId++;
		}
		xml.indent--;
		xml.println(out, "</constants>");
		
		for (Container table : cache.getFromTableList()) {
			xml.print(out, "<table");
			printAttribute("name", table.getName());
			printAttribute("alias", table.getAlias());
			printAttribute("xpos", table.getPosition().getX());
			printAttribute("ypos", table.getPosition().getY());
			xml.println(out, ">");
			xml.indent++;
			for (Item item : table.getItems()) {
				xml.print(out, "<column");
				String ident = "COL" + columnId;
				printAttribute("id", ident);
				itemIdMap.put(item, ident);
				printAttribute("name", item.getName());
				printAttribute("alias", item.getAlias());
				printAttribute("where-text", item.getWhere());
				xml.println(out, "/>");
				columnId++;
			}
			xml.indent--;
			xml.println(out, "</table>");
		}	
		
		for (SQLJoin join : cache.getJoins()) {
			xml.print(out, "<join");
			printAttribute("left-item-id", itemIdMap.get(join.getLeftColumn()));
			printAttribute("left-is-outer", Boolean.toString(join.isLeftColumnOuterJoin()));
			printAttribute("right-item-id", itemIdMap.get(join.getRightColumn())); 
			printAttribute("right-is-outer", Boolean.toString(join.isRightColumnOuterJoin()));
			printAttribute("comparator", join.getComparator()); 
			xml.println(out, "/>");
		}
				
		xml.println(out, "<select>");
		xml.indent++;
		for (Item col : cache.getSelectedColumns()) {
			xml.print(out, "<column");
			printAttribute("id", itemIdMap.get(col));
			xml.println(out, "/>");
		}
		xml.indent--;
		xml.println(out, "</select>");
		
		xml.print(out, "<global-where");
		printAttribute("text", cache.getGlobalWhereClause());
		xml.println(out, "/>");
		
		if (cache.isGroupingEnabled()) {
			for (Map.Entry<Item, SQLGroupFunction> entry: cache.getGroupByAggregateMap().entrySet()) {
				xml.print(out, "<group-by-aggregate");
				printAttribute("column-id", itemIdMap.get(entry.getKey()));
				printAttribute("aggregate", entry.getValue().name());
				xml.println(out, "/>");
			}
			
			for (Map.Entry<Item, String> entry : cache.getHavingMap().entrySet()) {
				xml.print(out, "<having");
				printAttribute("column-id", itemIdMap.get(entry.getKey()));
				printAttribute("text", entry.getValue());
				xml.println(out, "/>");
			}
			
		}
		
		for (Item item : cache.getOrderByList()) {
			xml.println(out, "<order-by");
			printAttribute("column-id", itemIdMap.get(item));
			printAttribute("direction", cache.getOrderByArgument(item).name());
			xml.println(out, "/>");
		}
		
		xml.print(out, "<query-string");
		printAttribute("string", cache.generateQuery());
		xml.println(out, "/>");		

		xml.indent--;
		xml.println(out, "</query>");
	}
	
	/**
	 * Prints an attribute to the file. If the attribute value is null
	 * no attribute will be printed.
	 */
    private void printAttribute(String name, String value) {
        if (value == null) return;
        xml.niprint(out, " " + name + "=\"");
        xml.niprint(out, SQLPowerUtils.escapeXML(value) + "\"");
    }
    
    private void printAttribute(String name, double value) {
    	xml.niprint(out, " " + name + "=\"" + value + "\"");
    }
    
    /**
     * Call this to flush and close the output stream if only part
     * of the file is being saved.
     */
    public void close() {
    	out.flush();
    	out.close();
    }
	
}
