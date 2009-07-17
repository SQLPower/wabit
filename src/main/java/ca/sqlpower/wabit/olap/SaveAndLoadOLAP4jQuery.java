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

package ca.sqlpower.wabit.olap;

import java.io.PrintWriter;
import java.util.Map;

import org.olap4j.Axis;
import org.olap4j.query.Query;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;

import ca.sqlpower.wabit.dao.WorkspaceXMLDAO;
import ca.sqlpower.xml.XMLHelper;

/**
 * This is the class which saves and loads an OlapQuery, it is in the same package as the OlapQuery
 * in order to prevent the Olap4jQuery model to leak out. This way the saving and loading can
 * access the Olap4jQuery model from the OlapQuery in a package private manner. This is all
 * necessary because in order to save an OlapQuery we need the Olap4jQuery model and the
 * whole point of the OlapQuery is to protect the Olap4jQuery model.
 */
public class SaveAndLoadOLAP4jQuery {
	/**
	 * This is the class which saves and loads the Olap4jQuery, this is done in this separate package
	 * to prevent the Olap4jQuery model from leaking out. See the class level comment for more details.
	 */
	private SaveAndLoadOLAP4jQuery() {
	}
	
	/**
	 * This method saves the OlapQuery object. It is in the same package as the OlapQuery
	 * in order to prevent the Olap4jQuery model from leaking out so it accesses the Olap4jQuery
	 * in a package protected manner.

	 * @param savingClass
	 * 		This class is included in order to reuse the code in the 'printAttribute' method
	 * @param olapQuery
	 * 		This is the query containing the Olap4jQuery that we need
	 */
	public static void saveOlap4jQuery(OlapQuery olapQuery,
			XMLHelper xml, PrintWriter out, final WorkspaceXMLDAO savingClass) {
		
		if (olapQuery.hasCachedXml()) {
			//The query has not been intialized, write out just the xml
			olapQuery.writeCachedXml(xml, out);
		} else {
			Query mdxQuery;
			try {
				mdxQuery = olapQuery.getMDXQuery();
			} catch (QueryInitializationException e) {
				throw new AssertionError("Threw an exception while saving, for some reason the query was " +
						"initialised while saving. The query should never be initialised while saving because " +
						"if it has not been initialised the query should just print out the xml data that it " +
						"recieved while loading.");
			}
			
			if (mdxQuery == null) return;
			
            xml.print(out, "<olap4j-query");
            savingClass.printAttribute("name", mdxQuery.getName());
            xml.niprintln(out, ">");
            xml.indent++;
            
	        for (Map.Entry<Axis, QueryAxis> axisEntry : mdxQuery.getAxes().entrySet()) {
	            // Check if it is the UNUSED axis, we can skip it safely.
		        if (axisEntry.getKey() == null) continue;
		        xml.print(out, "<olap4j-axis");
		        savingClass.printAttribute("ordinal", axisEntry.getKey().axisOrdinal());
		        xml.niprintln(out, ">");
		        xml.indent++;
		        for (QueryDimension dimension : axisEntry.getValue().getDimensions()) {
		            xml.print(out, "<olap4j-dimension");
		            savingClass.printAttribute("dimension-name", dimension.getDimension().getName());
		            xml.niprintln(out, ">");
		            xml.indent++;
		            for (Selection selection : dimension.getInclusions()) {
		            	xml.print(out, "<olap4j-selection");
		            	savingClass.printAttribute("dimension-name", selection.getMember().getDimension().getName());
		                savingClass.printAttribute("unique-member-name", selection.getMember().getUniqueName());
		                savingClass.printAttribute("operator", selection.getOperator().toString());
		                xml.niprintln(out, "/>");
		            }
		            xml.indent--;
		            xml.println(out, "</olap4j-dimension>");
		        }
		        xml.indent--;
		        xml.println(out, "</olap4j-axis>");
		    }
	        
	        xml.indent--;
            xml.println(out, "</olap4j-query>");
		}
	}
}
