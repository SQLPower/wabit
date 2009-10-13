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
import java.awt.geom.Point2D;

import org.olap4j.metadata.Cube;

import ca.sqlpower.query.TableContainer;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.dao.WabitPersister.DataType;

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
	public SessionPersisterSuperConverter(WabitSession session) {
		wabitObjectConverter = new WabitObjectConverter(session.getWorkspace());
		cubeConverter = new CubeConverter(session.getContext(), session.getDataSources());
		tableContainerConverter = new TableContainerConverter(session);
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
	public Object convertToBasicType(Object convertFrom, DataType fromType, Object ... additionalInfo) {
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
			return cubeConverter.convertToSimpleType(c);
			
		} else if (convertFrom instanceof Font) {
			Font f = (Font) convertFrom;
			return fontConverter.convertToSimpleType(f);
			
		} else if (convertFrom instanceof Point2D) {
			Point2D p = (Point2D) convertFrom;
			return point2DConverter.convertToSimpleType(p);
			
		} else if (convertFrom instanceof TableContainer) {
			TableContainer table = (TableContainer) convertFrom;
			return tableContainerConverter.convertToSimpleType(table);
		} else if (convertFrom instanceof String) {
			if (fromType != DataType.STRING) {
				throw new IllegalArgumentException("Converting a string should " +
						"define the type as " + DataType.STRING);
			}
			return convertFrom;
			
		} else if (convertFrom instanceof Integer) {
			if (fromType != DataType.INTEGER) {
				throw new IllegalArgumentException("Converting an integer should " +
						"define the type as " + DataType.INTEGER);
			}
			return convertFrom;
			
		} else if (convertFrom instanceof Double) {
			if (fromType != DataType.DOUBLE) {
				throw new IllegalArgumentException("Converting a double should " +
						"define the type as " + DataType.DOUBLE);
			}
			return convertFrom;
			
		} else if (convertFrom instanceof Boolean) {
			if (fromType != DataType.BOOLEAN) {
				throw new IllegalArgumentException("Converting a boolean should " +
						"define the type as " + DataType.BOOLEAN);
			}
			return convertFrom;
			
		} else if (convertFrom.getClass().isEnum()) {
			return new EnumConverter(convertFrom.getClass()).convertToSimpleType(convertFrom);
			
		} else {
			throw new IllegalArgumentException("Cannot convert " + convertFrom + " of type " + 
					convertFrom.getClass() + " to the type " + fromType);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public Object convertToComplexType(Object o, Class<? extends Object> type) {
		if (WabitObject.class.isAssignableFrom(type)) {
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
