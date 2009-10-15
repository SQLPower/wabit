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

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.io.InputStream;
import java.text.Format;

import org.olap4j.Axis;
import org.olap4j.metadata.Cube;

import ca.sqlpower.query.ItemContainer;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.query.SQLObjectItem;
import ca.sqlpower.query.StringItem;
import ca.sqlpower.query.TableContainer;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;

/**
 * Converts any known object in Wabit into a simple type of object that can be
 * pushed through an HTTP request and persisted on the server. This also
 * contains a way to get the object back based on the simple type that can be
 * passed and stored.
 */
public class SessionPersisterSuperConverter {
	
	private static final ColorConverter colorConverter = new ColorConverter();
	
	private final CubeConverter cubeConverter;
	
	private final WabitObjectConverter wabitObjectConverter;
	
	private static final FontConverter fontConverter = new FontConverter();
	
	private static final Point2DConverter point2DConverter = new Point2DConverter();
	
	private final TableContainerConverter tableContainerConverter;
	
	private final SQLJoinConverter sqlJoinConverter;
	
	private final JDBCDataSourceConverter jdbcDataSourceConverter;
	
	private final Olap4jDataSourceConverter olap4jDataSourceConverter;
	
	private final StringItemConverter stringItemConverter= new StringItemConverter();
	
	private final SQLObjectItemConverter sqlObjectItemConverter = new SQLObjectItemConverter();
	
	private final ItemContainerConverter itemContainerConverter = new ItemContainerConverter();
	
	private final PNGImageConverter pngImageConverter = new PNGImageConverter();
	
	private final FormatConverter formatConverter = new FormatConverter();
	
	private final Olap4JAxisConverter olap4jAxisConverter = new Olap4JAxisConverter();

	/**
	 * This converter will allow changes between any complex object in the
	 * session's workspace and a simple type that can be passed between
	 * persisters.
	 * 
	 * @param session
	 *            The session used to find necessary parts for converting
	 *            between simple and complex types. The session may be used to
	 *            look up connections, cubes, and {@link WabitObject}s in the
	 *            workspace.
	 */
	public SessionPersisterSuperConverter(WabitSession session, WabitObject root) {
		wabitObjectConverter = new WabitObjectConverter(root);
		cubeConverter = new CubeConverter(session.getContext(), session.getDataSources());
		tableContainerConverter = new TableContainerConverter(session);
		sqlJoinConverter = new SQLJoinConverter(root);
		jdbcDataSourceConverter = new JDBCDataSourceConverter(session.getWorkspace());
		olap4jDataSourceConverter = new Olap4jDataSourceConverter(session.getWorkspace());
	}

	/**
	 * Converts a complex object to a basic type or reference value that can
	 * then be passed on to other persisters. To reverse this process see
	 * {@link #convertToComplexType}. If a basic object is given to this method
	 * it will be returned without modification.
	 * 
	 * @param convertFrom
	 *            The value to convert to a basic type
	 * @param fromType
	 *            the type that the basic type will be defined as
	 * @param additionalInfo
	 *            any additional information that is required by the converters
	 *            for specific object types. The ONLY class that currently
	 *            requires an additional type is the cube converter. If we can
	 *            remove the need to pass the data source type with the cube
	 *            then this value can go away.
	 */
	@SuppressWarnings("unchecked")
	public Object convertToBasicType(Object convertFrom, Object ... additionalInfo) {
		if (convertFrom == null) {
			return null;
		} else if (convertFrom instanceof WabitObject) {
			WabitObject wo = (WabitObject) convertFrom;
			return wabitObjectConverter.convertToSimpleType(wo);
			
		} else if (convertFrom instanceof Color) {
			Color c = (Color) convertFrom;
			return colorConverter.convertToSimpleType(c);
			
		} else if (convertFrom instanceof Cube) {
			Cube c = (Cube) convertFrom;
			return cubeConverter.convertToSimpleType(c, additionalInfo);
			
		} else if (convertFrom instanceof Font) {
			Font f = (Font) convertFrom;
			return fontConverter.convertToSimpleType(f);
			
		} else if (convertFrom instanceof Point2D) {
			Point2D p = (Point2D) convertFrom;
			return point2DConverter.convertToSimpleType(p);
			
		} else if (convertFrom instanceof TableContainer) {
			TableContainer table = (TableContainer) convertFrom;
			return tableContainerConverter.convertToSimpleType(table);
			
		} else if (convertFrom instanceof SQLJoin) {
			SQLJoin join = (SQLJoin) convertFrom;
			return sqlJoinConverter.convertToSimpleType(join);
			
		} else if (convertFrom instanceof JDBCDataSource) {
			JDBCDataSource jdbcDataSource = (JDBCDataSource) convertFrom;
			return jdbcDataSourceConverter.convertToSimpleType(jdbcDataSource);
			
		} else if (convertFrom instanceof Olap4jDataSource) {
			Olap4jDataSource olap4jDataSource = (Olap4jDataSource) convertFrom;
			return olap4jDataSourceConverter.convertToSimpleType(olap4jDataSource);
			
		} else if (convertFrom instanceof SQLObjectItem) {
			return sqlObjectItemConverter.convertToSimpleType((SQLObjectItem) convertFrom);
			
		} else if (convertFrom instanceof StringItem) {
			return stringItemConverter.convertToSimpleType((StringItem) convertFrom);
			
		} else if (convertFrom instanceof ItemContainer) {
			return itemContainerConverter.convertToSimpleType((ItemContainer) convertFrom);
			
		} else if (convertFrom instanceof Image) {
			return pngImageConverter.convertToSimpleType((Image) convertFrom);
			
		} else if (convertFrom instanceof Format) {
			return formatConverter.convertToSimpleType((Format) convertFrom);
			
		} else if (convertFrom instanceof Axis) {
			return olap4jAxisConverter.convertToSimpleType((Axis) convertFrom);
			
		} else if (convertFrom instanceof String) {
			return convertFrom;
			
		} else if (convertFrom instanceof Integer) {
			return convertFrom;
			
		} else if (convertFrom instanceof Double) {
			return convertFrom;
			
		} else if (convertFrom instanceof Boolean) {
			return convertFrom;
			
		} else if (convertFrom.getClass().isEnum()) {
			return new EnumConverter(convertFrom.getClass()).convertToSimpleType((Enum) convertFrom);
			
		} else {
			throw new IllegalArgumentException("Cannot convert " + convertFrom + " of type " + 
					convertFrom.getClass());
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public Object convertToComplexType(Object o, Class<? extends Object> type) {
		if (o == null) {
			return null;
			
		} else if (WabitObject.class.isAssignableFrom(type)) {
			return wabitObjectConverter.convertToComplexType((String) o);
			
		} else if (Color.class.isAssignableFrom(type)) {
			return colorConverter.convertToComplexType((String) o);
			
		} else if (Cube.class.isAssignableFrom(type)) {
			return cubeConverter.convertToComplexType((String) o);
			
		} else if (Font.class.isAssignableFrom(type)) {
			return fontConverter.convertToComplexType((String) o);
			
		} else if (Enum.class.isAssignableFrom(type)) {
			return new EnumConverter(type).convertToComplexType((String) o);
			
		} else if (Point2D.class.isAssignableFrom(type)) {
			return point2DConverter.convertToComplexType((String) o);
			
		} else if (TableContainer.class.isAssignableFrom(type)) {
			return tableContainerConverter.convertToComplexType((String) o);
			
		} else if (SQLJoin.class.isAssignableFrom(type)) {
			return sqlJoinConverter.convertToComplexType((String) o);

		} else if (JDBCDataSource.class.isAssignableFrom(type)) {
			return jdbcDataSourceConverter.convertToComplexType((String) o);
			
		} else if (Olap4jDataSource.class.isAssignableFrom(type)) {
			return olap4jDataSourceConverter.convertToComplexType((String) o);
			
		} else if (SQLObjectItem.class.isAssignableFrom(type)) {
			return sqlObjectItemConverter.convertToComplexType((String) o);
			
		} else if (StringItem.class.isAssignableFrom(type)) {
			return stringItemConverter.convertToComplexType((String) o);
			
		} else if (Image.class.isAssignableFrom(type)) { 
			//TODO we should pass this the data type to know that we want a PNG 
			//in case other formats are supported in the future
			return pngImageConverter.convertToComplexType((InputStream) o);
			
		} else if (Format.class.isAssignableFrom(type)) {
			return formatConverter.convertToComplexType((String) o);
			
		} else if (Axis.class.isAssignableFrom(type)) {
			return olap4jAxisConverter.convertToComplexType((String) o);
			
		} else if (String.class.isAssignableFrom(type)) {
			return (String) o;
			
		} else if (Integer.class.isAssignableFrom(type)) {
			return (Integer) o;
			
		} else if (Double.class.isAssignableFrom(type)) {
			return (Double) o;
			
		} else if (Boolean.class.isAssignableFrom(type)) {
			return (Boolean) o;
			
		} else {
			throw new IllegalArgumentException("Cannot convert " + o + " of type " + 
					o.getClass() + " to the type " + type);
		}
	}

}
