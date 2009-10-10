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
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

import org.apache.commons.beanutils.ConvertUtils;

import ca.sqlpower.query.Item;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.query.TableContainer;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.wabit.OlapConnectionMapping;
import ca.sqlpower.wabit.WabitChildEvent;
import ca.sqlpower.wabit.WabitColumnItem;
import ca.sqlpower.wabit.WabitConstantItem;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitJoin;
import ca.sqlpower.wabit.WabitListener;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitTableContainer;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.dao.WabitPersistenceException;
import ca.sqlpower.wabit.dao.WabitPersister;
import ca.sqlpower.wabit.dao.WabitPersister.DataType;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.WabitOlapAxis;
import ca.sqlpower.wabit.olap.WabitOlapDimension;
import ca.sqlpower.wabit.olap.WabitOlapSelection;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Page.PageOrientation;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.report.chart.ChartColumn;
import ca.sqlpower.wabit.report.chart.ChartType;
import ca.sqlpower.wabit.report.chart.LegendPosition;
import ca.sqlpower.wabit.rs.ResultSetProducer;

/**
 * An implementation of {@link WabitListener} used exclusively for listening to
 * a {@link WabitWorkspace} and its children. When an event is fired from an
 * object this listener will convert the event into persist calls. The persist
 * calls will be made on the target persister.
 */
public class WorkspacePersisterListener implements WabitListener {

	/**
	 * This is the persister to call the appropriate persist methods on when an
	 * event occurs signaling a change to the model.
	 */
	private final WabitPersister target;

	/**
	 * Converts any object into a simple type and converts any simple type back.
	 */
	private final SessionPersisterSuperConverter converter;

	public WorkspacePersisterListener(WabitWorkspace workspace,
			OlapConnectionMapping mapping, DataSourceCollection<SPDataSource> dsCollection,
			WabitPersister persister) {
		converter = new SessionPersisterSuperConverter(workspace, mapping, dsCollection);
		target = persister;
	}

	public void transactionEnded(TransactionEvent e) {
		try {
			target.commit();
		} catch (WabitPersistenceException e1) {
			throw new RuntimeException("Could not commit the transaction.", e1);
		}
	}

	public void transactionRollback(TransactionEvent e) {
		try {
			target.rollback();
		} catch (WabitPersistenceException e1) {
			throw new RuntimeException("Could not rollback the transaction.",
					e1);
		}
	}

	public void transactionStarted(TransactionEvent e) {
		try {
			target.begin();
		} catch (WabitPersistenceException e1) {
			throw new RuntimeException("Could not begin the transaction.", e1);
		}
	}

	public void wabitChildAdded(WabitChildEvent e) {
		try {
			String parentUUID = e.getSource().getUUID();
			WabitObject child = e.getChild();
			String className = e.getChildType().getSimpleName();
			String uuid = child.getUUID();

			target.persistObject(parentUUID, className, uuid, e.getIndex());
			target.persistProperty(uuid, "name", DataType.STRING, child
					.getName());

			// Persist any properties required for WabitObject constructor
			if (child instanceof CellSetRenderer) {
				CellSetRenderer csRenderer = (CellSetRenderer) child;

				// Constructor argument
				target.persistProperty(uuid, "modifiedOlapQuery",
								DataType.REFERENCE, csRenderer.getOlapQuery()
										.getUUID());

				// Remaining properties
				target.persistProperty(uuid, "bodyAlignment", DataType.STRING,
						csRenderer.getBodyAlignment().name());
				target.persistProperty(uuid, "bodyFont", DataType.FONT,
						converter.convertToBasicType(csRenderer.getBodyFont(), DataType.FONT));
				target.persistProperty(uuid, "bodyFormat", DataType.STRING,
						csRenderer.getBodyFormat().toPattern());
				target.persistProperty(uuid, "headerFont", DataType.FONT,
						converter.convertToBasicType(csRenderer.getHeaderFont(), DataType.FONT));
				// target.persistProperty(uuid, "selectedMember",
				// DataType.STRING,
				// csRenderer.getSelectedMember().getProperties());
				// TODO

			} else if (child instanceof ChartColumn) {
				ChartColumn chartColumn = (ChartColumn) child;

				// Constructor arguments
				target.persistProperty(uuid, "columnName", DataType.STRING,
						chartColumn.getColumnName());
				target.persistProperty(uuid, "dataType", DataType.ENUM,
						converter.convertToBasicType(chartColumn.getDataType(), DataType.ENUM));

				// Remaining properties
				target.persistProperty(uuid, "roleInChart", DataType.ENUM,
						converter.convertToBasicType(chartColumn.getRoleInChart(), DataType.ENUM));

				ChartColumn xAxisIdentifier = chartColumn.getXAxisIdentifier();
				if (xAxisIdentifier != null) {
					target.persistProperty(uuid, "XAxisIdentifier",
							DataType.REFERENCE, ConvertUtils.convert(
									xAxisIdentifier, String.class));
				} else {
					target.persistProperty(uuid, "XAxisIdentifier",
							DataType.REFERENCE, null);
				}


			} else if (child instanceof Chart) {
				Chart chart = (Chart) child;

				// Remaining properties
				target.persistProperty(uuid, "gratuitouslyAnimated",
						DataType.BOOLEAN, chart.isGratuitouslyAnimated());
				target.persistProperty(uuid, "legendPosition", DataType.ENUM,
						converter.convertToBasicType(chart.getLegendPosition(), DataType.ENUM));
				
				ResultSetProducer rsProducer = chart.getQuery();
				if (rsProducer != null) {
					target.persistProperty(uuid, "query", DataType.REFERENCE,
							rsProducer.getUUID());
				} else {
					target.persistProperty(uuid, "query", DataType.REFERENCE,
							null);
				}
				
				target.persistProperty(uuid, "type", DataType.ENUM,
						converter.convertToBasicType(chart.getType(), DataType.ENUM));
				target.persistProperty(uuid, "xAxisLabelRotation",
						DataType.DOUBLE, chart.getXaxisLabelRotation());
				target.persistProperty(uuid, "xaxisName", DataType.STRING,
						chart.getXaxisName());
				target.persistProperty(uuid, "yaxisName", DataType.STRING,
						chart.getYaxisName());

			} else if (child instanceof ChartRenderer) {
				ChartRenderer cRenderer = (ChartRenderer) child;

				// Constructor argument
				target.persistProperty(uuid, "chart", DataType.REFERENCE,
						cRenderer.getChart().getUUID());

				// Remaining properties

			} else if (child instanceof ColumnInfo) {
				ColumnInfo columnInfo = (ColumnInfo) child;

				// Constructor argument
				target.persistProperty(uuid, ColumnInfo.COLUMN_ALIAS,
						DataType.STRING, columnInfo.getColumnAlias());

				// Remaining properties
				Item item = columnInfo.getColumnInfoItem();
				if (item != null) {
					target.persistProperty(uuid,
							ColumnInfo.COLUMN_INFO_ITEM_CHANGED,
							DataType.STRING, item.getUUID());
				}

				target.persistProperty(uuid, ColumnInfo.DATATYPE_CHANGED,
						DataType.STRING, columnInfo.getDataType().name());

				Format formatType = columnInfo.getFormat();

				if (formatType != null) {
					String formatString;

					if (formatType instanceof SimpleDateFormat) {
						formatString = "date-format";
					} else if (formatType instanceof DecimalFormat) {
						formatString = "decimal-format";
					} else {
						throw new WabitPersistenceException(uuid,
								"Invalid format type: "
										+ formatType.getClass().getSimpleName());
					}
					target.persistProperty(uuid, ColumnInfo.FORMAT_CHANGED,
							DataType.STRING, formatString);
				}

				target.persistProperty(uuid,
						ColumnInfo.HORIZONAL_ALIGNMENT_CHANGED,
						DataType.STRING, columnInfo.getHorizontalAlignment()
								.name());
				target.persistProperty(uuid, ColumnInfo.WIDTH_CHANGED,
						DataType.INTEGER, columnInfo.getWidth());
				target.persistProperty(uuid,
						ColumnInfo.WILL_GROUP_OR_BREAK_CHANGED,
						DataType.BOOLEAN, columnInfo.getWillGroupOrBreak());
				target.persistProperty(uuid, ColumnInfo.WILL_SUBTOTAL_CHANGED,
						DataType.BOOLEAN, columnInfo.getWillSubtotal());

			} else if (child instanceof ContentBox) {
				ContentBox contentBox = (ContentBox) child;

				// Remaining arguments
				target.persistProperty(uuid, "contentRenderer",
						DataType.REFERENCE, contentBox.getContentRenderer()
								.getUUID());
				target.persistProperty(uuid, "font", DataType.FONT,
						converter.convertToBasicType(contentBox.getFont(), DataType.FONT));
				target.persistProperty(uuid, "height", DataType.DOUBLE,
						contentBox.getHeight());
				target.persistProperty(uuid, "width", DataType.DOUBLE,
						contentBox.getWidth());
				target.persistProperty(uuid, "x", DataType.DOUBLE, contentBox
						.getX());
				target.persistProperty(uuid, "y", DataType.DOUBLE, contentBox
						.getY());

			} else if (child instanceof Guide) {
				Guide guide = (Guide) child;

				// Constructor arguments
				target.persistProperty(uuid, "axis", DataType.STRING, guide
						.getAxis().name());
				target.persistProperty(uuid, "offset", DataType.DOUBLE, guide
						.getOffset());

				// Remaining properties

			} else if (child instanceof ImageRenderer) {
				ImageRenderer iRenderer = (ImageRenderer) child;

				// Remaining arguments
				target.persistProperty(uuid, "HAlign", DataType.STRING,
						iRenderer.getHAlign().name());
				target.persistProperty(uuid, "image", DataType.REFERENCE,
						iRenderer.getImage().getUUID());
				target.persistProperty(uuid, "preserveAspectRatioWhenResizing",
						DataType.BOOLEAN, iRenderer
								.isPreserveAspectRatioWhenResizing());
				target.persistProperty(uuid, "preservingAspectRatio",
						DataType.BOOLEAN, iRenderer.isPreservingAspectRatio());

			} else if (child instanceof Label) {
				Label label = (Label) child;

				// Remaining arguments
				target.persistProperty(uuid, "backgroundColour",
						DataType.COLOR,
						converter.convertToBasicType(label.getBackgroundColour(), DataType.COLOR));
				target.persistProperty(uuid, "font", DataType.FONT,
						converter.convertToBasicType(label.getFont(), DataType.FONT));
				target.persistProperty(uuid, "horizontalAlignment",
						DataType.STRING, label.getHorizontalAlignment().name());
				target.persistProperty(uuid, "text", DataType.STRING, label
						.getText());
				target.persistProperty(uuid, "verticalAlignment",
						DataType.STRING, label.getVerticalAlignment().name());

			} else if (child instanceof Layout) {
				Layout layout = (Layout) child;

				// Constructor argument

				// Remaining parameters
				// target.persistProperty(uuid, "page", DataType.REFERENCE,
				// layout.getPage().getUUID());
				// target.persistProperty(uuid, "varContext", ...)
				target.persistProperty(uuid, Layout.PROPERTY_ZOOM,
						DataType.INTEGER, layout.getZoomLevel());

			} else if (child instanceof OlapQuery) {
				OlapQuery olapQuery = (OlapQuery) child;

				// Constructor arguments
				target.persistProperty(uuid, "queryName", DataType.STRING,
						olapQuery.getQueryName());
				target.persistProperty(uuid, "catalogName", DataType.STRING,
						olapQuery.getCatalogName());
				target.persistProperty(uuid, "schemaName", DataType.STRING,
						olapQuery.getSchemaName());
				target.persistProperty(uuid, "cubeName", DataType.STRING,
						olapQuery.getCubeName());

				// Remaining properties
				target.persistProperty(uuid, "nonEmpty", DataType.BOOLEAN,
						olapQuery.isNonEmpty());
				target.persistProperty(uuid, "currentCube", DataType.CUBE,
						converter.convertToBasicType(
								olapQuery.getCurrentCube(), DataType.CUBE));

			} else if (child instanceof Page) {
				Page page = (Page) child;

				// Constructor arguments
				target.persistProperty(uuid, "width", DataType.INTEGER, page
						.getWidth());
				target.persistProperty(uuid, "height", DataType.INTEGER, page
						.getHeight());
				target.persistProperty(uuid, "orientation", DataType.ENUM,
						page.getOrientation().name());

				// Remaining properties
				target.persistProperty(uuid, "defaultFont", DataType.FONT,
						converter.convertToBasicType(page.getDefaultFont(), DataType.FONT));

			} else if (child instanceof WabitColumnItem) {

			} else if (child instanceof WabitConstantItem) {

			} else if (child instanceof WabitDataSource) {

			} else if (child instanceof WabitJoin) {
				SQLJoin sqlJoin = ((WabitJoin) child).getDelegate();

				target.persistProperty(uuid, SQLJoin.LEFT_JOIN_CHANGED,
						DataType.REFERENCE, sqlJoin.getLeftColumn().getUUID());
				target.persistProperty(uuid, SQLJoin.RIGHT_JOIN_CHANGED,
						DataType.REFERENCE, sqlJoin.getRightColumn());

			} else if (child instanceof WabitOlapAxis) {
				WabitOlapAxis wabitOlapAxis = (WabitOlapAxis) child;

				// Constructor argument
				target.persistProperty(uuid, "ordinal", DataType.STRING,
						wabitOlapAxis.getOrdinal().name());

				// Remaining properties
				target.persistProperty(uuid, "nonEmpty", DataType.BOOLEAN,
						wabitOlapAxis.isNonEmpty());
				target.persistProperty(uuid, "sortEvaluationLiteral",
						DataType.STRING, wabitOlapAxis
								.getSortEvaluationLiteral());
				target.persistProperty(uuid, "sortOrder", DataType.STRING,
						wabitOlapAxis.getSortOrder());

			} else if (child instanceof WabitOlapDimension) {
				// Constructor argument

			} else if (child instanceof WabitOlapSelection) {
				WabitOlapSelection wabitOlapSelection = (WabitOlapSelection) child;

				// Constructor argument
				target.persistProperty(uuid, "operator", DataType.STRING,
						wabitOlapSelection.getOperator().name());
				target.persistProperty(uuid, "uniqueMemberName",
						DataType.STRING, wabitOlapSelection
								.getUniqueMemberName());

			} else if (child instanceof WabitTableContainer) {
				WabitTableContainer wabitTableContainer = (WabitTableContainer) child;
				TableContainer tableContainer = (TableContainer) wabitTableContainer
						.getDelegate();

				// Constructor arguments
				target.persistProperty(uuid, "schema", DataType.STRING,
						tableContainer.getSchema());
				target.persistProperty(uuid, "catalog", DataType.STRING,
						tableContainer.getCatalog());

				// Remaining properties
				target.persistProperty(uuid, "alias", DataType.STRING,
						tableContainer.getAlias());
				target.persistProperty(uuid, "position", DataType.POINT2D,
						converter.convertToBasicType(tableContainer.getPosition(), DataType.POINT2D));

			}
			
			System.out.println("wabitChildAdded. type: "
					+ e.getChildType().getSimpleName() + ". source: "
					+ e.getSource().getClass().getSimpleName());

		} catch (WabitPersistenceException e1) {
			throw new RuntimeException("Could not add WabitObject as a child.",
					e1);
		}
	}

	public void wabitChildRemoved(WabitChildEvent e) {
		try {
			target.removeObject(e.getSource().getUUID(), e.getChild()
							.getUUID());
		} catch (WabitPersistenceException e1) {
			throw new RuntimeException(
					"Could not remove WabitObject from its parent.", e1);
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		WabitObject source = (WabitObject) evt.getSource();
		String uuid = source.getUUID();
		String propertyName = evt.getPropertyName();
		Object oldValue = evt.getOldValue();
		Object newValue = evt.getNewValue();
		
		//XXX special case that I want to remove even though I'm implementing it
		SPDataSource ds = null;
		if (source instanceof OlapQuery && propertyName.equals("currentCube")) {
			ds = ((OlapQuery) source).getOlapDataSource();
		}

		try {
			DataType typeForClass = WabitPersister.DataType
					.getTypeByClass(newValue.getClass());
			Object oldBasicType;
			Object newBasicType; 
			if (ds == null) {
				oldBasicType = converter.convertToBasicType(oldValue, typeForClass);
				newBasicType = converter.convertToBasicType(newValue, typeForClass);
			} else {
				oldBasicType = converter.convertToBasicType(oldValue, typeForClass, ds);
				newBasicType = converter.convertToBasicType(newValue, typeForClass, ds);
			}
			
			target.persistProperty(uuid, propertyName, typeForClass, 
					oldBasicType, newBasicType);
		} catch (WabitPersistenceException e) {
			throw new RuntimeException(e);
		}
	}
}