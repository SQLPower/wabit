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

package ca.sqlpower.wabit.report;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ca.sqlpower.sql.RowSetChangeEvent;
import ca.sqlpower.sql.RowSetChangeListener;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.query.StatementExecutor;
import ca.sqlpower.swingui.table.CleanupTableCellRenderer;
import ca.sqlpower.swingui.table.EditableJTable;
import ca.sqlpower.swingui.table.ResultSetTableModel;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.Query;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitProject;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class will render a graph from a query's result set in a graph format
 * defined by the user.
 */
public class GraphRenderer extends AbstractWabitObject implements ReportContentRenderer {
	
	private static final Logger logger = Logger.getLogger(GraphRenderer.class);
	
	/**
	 * This enum contains the values that each column can be defined as
	 * for laying out a graph.
	 */
	public enum DataTypeSeries {
		NONE,
		CATEGORY,
		SERIES
	};
	
	/**
	 * The types of graph this renderer can create.
	 */
	public enum ExistingGraphTypes {
		BAR,
		LINE,
		SCATTER
	}
	
	/**
	 * This is the property panel for a GraphRenderer object. This will
	 * let users specify the type of graph to display as well as what
	 * values the graph will display.
	 */
	private class GraphRendererPropertyPanel implements DataEntryPanel {
		
		/**
		 * This cell renderer is used to add combo boxes to the headers of tables returned in 
		 * the properties panel. The combo boxes will allow users to define columns to be categories
		 * or series. 
		 * <p>
		 * This is for category type graphs
		 */
		private class CategoryGraphRendererTableCellRenderer implements CleanupTableCellRenderer {
			
			/**
			 * This listens to all of the combo boxes that define how the column relates
			 * to a category. This listener will update the current category combo box to NONE
			 * if a new category is selected since there is only one category allowed at a time.
			 */
			private final ItemListener dataTypeSeriesChangeListener = new ItemListener() {
				
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						JComboBox sourceCombo = (JComboBox) e.getSource();
						if (e.getItem() == DataTypeSeries.CATEGORY) {
							if (columnsToDataTypes.containsValue(DataTypeSeries.CATEGORY)) {
								String columnName = null;
								for (Map.Entry<String, DataTypeSeries> entry : columnsToDataTypes.entrySet()) {
									if (entry.getValue() == DataTypeSeries.CATEGORY) {
										columnName = entry.getKey();
										break;
									}
								}
								int position = columnNamesInOrder.indexOf(columnName);
								JComboBox categoryComboBox = columnToComboBox.get(position);
								categoryComboBox.setSelectedItem(DataTypeSeries.NONE);
								columnsToDataTypes.put(columnName, DataTypeSeries.NONE);
							}
						}
						columnsToDataTypes.put(columnNamesInOrder.get(tableHeader.getColumnModel().getColumnIndexAtX(sourceCombo.getX())), (DataTypeSeries) e.getItem());
						logger.debug("Column data types are now " + columnsToDataTypes);
						tableHeader.repaint();
						updateChartPreview();
					}
				}
			};

			/**
			 * The header is used to attach a mouse listener to let the combo box
			 * pop up.
			 */
			private final JTableHeader tableHeader;

			/**
			 * This map will track which combo boxes are in which position. This
			 * lets us know which combo box to use to display to a user when a
			 * header is clicked.
			 */
			private final Map<Integer, JComboBox> columnToComboBox = new HashMap<Integer, JComboBox>();
			
			/**
			 * This header is used as the default way to render a table's cell. This
			 * way cells will have a similar looking header to the default. 
			 */
			private final TableCellRenderer defaultTableCellRenderer;
			
			/**
			 * This listens for mouse clicks on the table header to show the combo box's
			 * pop-up menu. This is needed as the normal mouse listeners on the combo box
			 * are removed on the table header.
			 */
			private final MouseListener comboBoxMouseListener = new MouseAdapter() {

				private int mouseX;
				private int mouseY;
				
				@Override
				public void mousePressed(MouseEvent e) {
					mouseX = e.getX();
					mouseY = e.getY();
				}
				
				@Override
				public void mouseReleased(MouseEvent e) {
					if (e.getX() - mouseX > 3 || e.getX() - mouseX < -3 || e.getY() - mouseY > 3 || e.getY() - mouseY < -3) {
						return;
					}
					final int column = tableHeader.getColumnModel().getColumnIndexAtX(e.getX());
					final JComboBox dataTypeComboBox = columnToComboBox.get(column);
					tableHeader.add(dataTypeComboBox);
					dataTypeComboBox.setBounds(getXPositionOnColumn(tableHeader.getColumnModel(), column), 0, tableHeader.getColumnModel().getColumn(column).getWidth(), dataTypeComboBox.getHeight());
					dataTypeComboBox.setPopupVisible(true);
					dataTypeComboBox.addPopupMenuListener(new PopupMenuListener() {
					
						public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
							//don't care
						}
					
						public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
							tableHeader.remove(dataTypeComboBox);
							dataTypeComboBox.removePopupMenuListener(this);
						}
					
						public void popupMenuCanceled(PopupMenuEvent e) {
							//don't care
						}
					});
					logger.debug("table header has components " + Arrays.toString(tableHeader.getComponents()));
				}
			};
			
			public CategoryGraphRendererTableCellRenderer(final JTableHeader tableHeader, TableCellRenderer defaultTableCellRenderer) {
				this.tableHeader = tableHeader;
				this.defaultTableCellRenderer = defaultTableCellRenderer;
				
				tableHeader.addMouseListener(comboBoxMouseListener);
			}

			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row,
					final int column) {
				Component defaultComponent = defaultTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				final JPanel newHeader = new JPanel(new BorderLayout());
				final JComboBox dataTypeComboBox = new JComboBox(DataTypeSeries.values());
				try {
					if (!SQL.isNumeric(rs.getMetaData().getColumnType(column + 1))) {
						dataTypeComboBox.removeItem(DataTypeSeries.SERIES);
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				columnToComboBox.put(new Integer(column), dataTypeComboBox);
				final DataTypeSeries defaultDataTypeSeries = columnsToDataTypes.get(columnNamesInOrder.get(column));
				if (defaultDataTypeSeries == null) {
					dataTypeComboBox.setSelectedItem(DataTypeSeries.NONE);
				} else {
					dataTypeComboBox.setSelectedItem(defaultDataTypeSeries);
				}
				dataTypeComboBox.addItemListener(dataTypeSeriesChangeListener);
				newHeader.add(dataTypeComboBox, BorderLayout.NORTH);
				newHeader.add(defaultComponent, BorderLayout.SOUTH);
				
				return newHeader;
			}

			public void cleanup() {
				tableHeader.removeMouseListener(comboBoxMouseListener);
			}
		
		}

		/**
		 * This table cell renderer is used to make headers for the result set
		 * table for line and scatter graphs.
		 */
		private class XYGraphRendererCellRenderer implements CleanupTableCellRenderer {
			
			/**
			 * This listens to all of the combo boxes that define how the column relates
			 * to a graph.
			 */
			private final ItemListener dataTypeSeriesChangeListener = new ItemListener() {
				
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						JComboBox sourceCombo = (JComboBox) e.getSource();
						final int columnIndexAtX = tableHeader.getColumnModel().getColumnIndexAtX(sourceCombo.getX());
						String colSeriesName = columnNamesInOrder.get(columnIndexAtX);
						columnsToDataTypes.put(colSeriesName, (DataTypeSeries) e.getItem());
						if (((DataTypeSeries) e.getItem()) == DataTypeSeries.NONE) {
							columnSeriesToColumnXAxis.remove(colSeriesName);
							columnToXAxisComboBox.remove(columnIndexAtX);
						}
						logger.debug("Column data types are now " + columnsToDataTypes);
						tableHeader.repaint();
						updateChartPreview();
					}
				}
			};
			
			
			private final ItemListener xAxisValuesChangeListener = new ItemListener() {
				
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						JComboBox sourceCombo = (JComboBox) e.getSource();
						columnSeriesToColumnXAxis.put(columnNamesInOrder.get(tableHeader.getColumnModel().getColumnIndexAtX(sourceCombo.getX())), (String) e.getItem());
						tableHeader.repaint();
						updateChartPreview();
					}
				}
			};

			/**
			 * The header is used to attach a mouse listener to let the combo box
			 * pop up.
			 */
			private final JTableHeader tableHeader;

			/**
			 * This map will track which combo boxes are in which position. This
			 * lets us know which combo box to use to display to a user when a
			 * header is clicked.
			 */
			private final Map<Integer, JComboBox> columnToDataTypeSeriesComboBox = new HashMap<Integer, JComboBox>();
			
			/**
			 * This map tracks which combo boxes are in which position for defining
			 * a column that's a series to have a column that is it's X axis values. 
			 */
			private final Map<Integer, JComboBox> columnToXAxisComboBox = new HashMap<Integer, JComboBox>();
			
			/**
			 * This header is used as the default way to render a table's cell. This
			 * way cells will have a similar looking header to the default. 
			 */
			private final TableCellRenderer defaultTableCellRenderer;
			
			/**
			 * This listens to mouse clicks on the table header to show the correct
			 * combo box's pop-up menu appear. This way the user can edit the combo
			 * boxes since the normal mouse listeners on a table header are removed.
			 */
			private final MouseListener comboBoxMouseListener = new MouseAdapter() {

				private int mouseX;
				private int mouseY;
				
				@Override
				public void mousePressed(MouseEvent e) {
					mouseX = e.getX();
					mouseY = e.getY();
				}
				
				@Override
				public void mouseReleased(MouseEvent e) {
					if (e.getX() - mouseX > 3 || e.getX() - mouseX < -3 || e.getY() - mouseY > 3 || e.getY() - mouseY < -3) {
						return;
					}
					final int column = tableHeader.getColumnModel().getColumnIndexAtX(e.getX());
					
					final JComboBox dataTypeComboBox;
					int yPosition = 0;
					if (e.getY() < new JComboBox().getPreferredSize().getHeight()) {
						dataTypeComboBox = columnToDataTypeSeriesComboBox.get(column);
					} else if (e.getY() < new JComboBox().getPreferredSize().getHeight() * 2) {
						dataTypeComboBox = columnToXAxisComboBox.get(column);
						if (dataTypeComboBox != null) {
							yPosition = dataTypeComboBox.getHeight();
						}
					} else {
						dataTypeComboBox = null;
					}
					if (dataTypeComboBox == null) return;
					tableHeader.add(dataTypeComboBox);
					dataTypeComboBox.setBounds(getXPositionOnColumn(tableHeader.getColumnModel(), column), yPosition, tableHeader.getColumnModel().getColumn(column).getWidth(), dataTypeComboBox.getHeight());
					dataTypeComboBox.setPopupVisible(true);
					dataTypeComboBox.addPopupMenuListener(new PopupMenuListener() {
					
						public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
							//don't care
						}
					
						public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
							tableHeader.remove(dataTypeComboBox);
							dataTypeComboBox.removePopupMenuListener(this);
						}
					
						public void popupMenuCanceled(PopupMenuEvent e) {
							//don't care
						}
					});
					logger.debug("table header has components " + Arrays.toString(tableHeader.getComponents()));
				}
			};
			
			public XYGraphRendererCellRenderer(final JTableHeader tableHeader, TableCellRenderer defaultTableCellRenderer) {
				this.tableHeader = tableHeader;
				this.defaultTableCellRenderer = defaultTableCellRenderer;
				
				tableHeader.addMouseListener(comboBoxMouseListener);
			}

			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row,
					final int column) {
				Component defaultComponent = defaultTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				final JPanel newHeader = new JPanel(new BorderLayout());
				newHeader.add(defaultComponent, BorderLayout.SOUTH);
				final JComboBox dataTypeComboBox = new JComboBox(DataTypeSeries.values());
				dataTypeComboBox.removeItem(DataTypeSeries.CATEGORY);
				try {
					if (!SQL.isNumeric(rs.getMetaData().getColumnType(column + 1))) {
						JLabel emptyLabel = new JLabel();
						emptyLabel.setPreferredSize(new Dimension(0, (int) dataTypeComboBox.getPreferredSize().getHeight() * 2));
						newHeader.add(emptyLabel, BorderLayout.NORTH);
						return newHeader;
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				columnToDataTypeSeriesComboBox.put(new Integer(column), dataTypeComboBox);
				final DataTypeSeries defaultDataTypeSeries = columnsToDataTypes.get(columnNamesInOrder.get(column));
				if (defaultDataTypeSeries == null) {
					dataTypeComboBox.setSelectedItem(DataTypeSeries.NONE);
				} else {
					dataTypeComboBox.setSelectedItem(defaultDataTypeSeries);
				}
				List<String> numericAndDateCols = new ArrayList<String>();
				for (String col : columnNamesInOrder) {
					int columnType;
					try {
						columnType = rs.getMetaData().getColumnType(rs.findColumn(col));
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
					if (SQL.isNumeric(columnType) || SQL.isDate(columnType)) {
						numericAndDateCols.add(col);
					}
				}
				final JComboBox comboBoxForXValues = new JComboBox(numericAndDateCols.toArray());
				if (defaultDataTypeSeries == DataTypeSeries.SERIES) {
					comboBoxForXValues.setSelectedItem(columnSeriesToColumnXAxis.get(columnNamesInOrder.get(column)));
					newHeader.add(comboBoxForXValues, BorderLayout.CENTER);
					columnToXAxisComboBox.put(new Integer(column), comboBoxForXValues);
					comboBoxForXValues.addItemListener(xAxisValuesChangeListener);
				} else {
					JLabel emptyLabel = new JLabel();
					emptyLabel.setPreferredSize(new Dimension(0, (int) dataTypeComboBox.getPreferredSize().getHeight()));
					newHeader.add(emptyLabel, BorderLayout.CENTER);
				}
				dataTypeComboBox.addItemListener(dataTypeSeriesChangeListener);
				newHeader.add(dataTypeComboBox, BorderLayout.NORTH);
				
				return newHeader;
			}

			public void cleanup() {
				tableHeader.removeMouseListener(comboBoxMouseListener);
			}
			
		}
		
		/**
		 * Returns the x position of the given a column index. This is a helper method for the
		 * TableCellRenderers.
		 */
		public int getXPositionOnColumn(TableColumnModel model, int columnIndex) {
			int sum = 0;
			for(int i = 0; i < columnIndex; i ++) {
				sum += model.getColumn(i).getWidth();
			}
			return sum;
		}
		
		/**
		 * The properties panel.
		 */
		private final JPanel panel = new JPanel();
		
		/**
		 * A field to change the graph's name in
		 */
		private final JTextField nameField = new JTextField();
		
		/**
		 * A combo box containing all the queries in the project. This
		 * will let the user choose which query to graph.
		 */
		private final JComboBox queryComboBox;
		
		/**
		 * A field to change the label on the y axis.
		 */
		private final JTextField yaxisNameField = new JTextField();
		
		/**
		 * A field to change the label on the x axis.
		 */
		private final JTextField xaxisNameField = new JTextField();
		
		/**
		 * A label for the x axis field. This is defined
		 * here to make the label not visible when the field
		 * is not needed.
		 */
		private final JLabel xaxisNameLabel = new JLabel("X Axis");
		
		/**
		 * The table that shows values returned from the queries. The headers
		 * added to this table will allow users to define which column is the
		 * category and which ones are series.
		 */
		private final JTable resultTable = new EditableJTable();
		
		/**
		 * This is the most recent result set displayed by the resultTable.
		 */
		private ResultSet rs;
		
		/**
		 * This is an error/warning label for the result table. If something 
		 * goes wrong in a query this result table will display a nicer user.
		 */
		private final JLabel resultTableLabel = new JLabel();
		
		/**
		 * This combo box holds all of the graph types that the {@link GraphRenderer}
		 * can generate and display.
		 * <p>
		 * This will eventually change to buttons to select the desired graph type.
		 */
		private final JComboBox graphTypeComboBox;
		
		/**
		 * This tracks the ordering of the actual column names, not the display names.
		 * This allows us to get back the original column name from the column index.
		 * and to track the ordering of the columns for displaying the properties
		 * panel again.
		 */
		private final List<String> columnNamesInOrder = new ArrayList<String>(); 

		/**
		 * This maps the column names for each column to the data type series.
		 */
		private final Map<String, DataTypeSeries> columnsToDataTypes = new HashMap<String, DataTypeSeries>();

		/**
		 * This map tracks the columns that are defined as series and the column
		 * defined to be the values displayed on the x axis.
		 */
		private final Map<String, String> columnSeriesToColumnXAxis = new HashMap<String, String>();
		
		/**
		 * This panel will display a JFreeChart that is a preview of what the
		 * user has selected from the result table. This chart should look
		 * identical to what would be shown on the report.
		 */
		private final ChartPanel chartPanel = new ChartPanel(null);
		
		/**
		 * This is the default renderer of the table displaying the values from the query.
		 * This is stored to give the headers a normal look depending on user settings.
		 */
		private final TableCellRenderer defaultTableCellRenderer;
		
		/**
		 * This change listener will be added to the query that is selected in the combo box.
		 * The change listener will update the graph that users can view as a preview.
		 */
		private final RowSetChangeListener rowSetChangeListener = new RowSetChangeListener() {
			public void rowAdded(RowSetChangeEvent e) {
				updateChartPreview();
			}
		};
		
		/**
		 * This {@link TableCellRenderer} is the current wrapper on the regular {@link TableCellRenderer}.
		 * This wrapper will place appropriate combo boxes above the table headers to allow users
		 * to specify if the columns are to be used as series, categories, or x-axis values in a graph.
		 */
		private CleanupTableCellRenderer currentHeaderTableCellRenderer;
		
		public GraphRendererPropertyPanel(WabitProject project, GraphRenderer renderer) {
			defaultTableCellRenderer = resultTable.getTableHeader().getDefaultRenderer();
			queryComboBox = new JComboBox(project.getQueries().toArray());
			graphTypeComboBox = new JComboBox(ExistingGraphTypes.values());
			
			queryComboBox.setSelectedItem(renderer.getQuery());
			graphTypeComboBox.setSelectedItem(renderer.getGraphType());
			if (renderer.getGraphType() == ExistingGraphTypes.BAR) {
				currentHeaderTableCellRenderer = new CategoryGraphRendererTableCellRenderer(resultTable.getTableHeader(), defaultTableCellRenderer);
				resultTable.getTableHeader().setDefaultRenderer(currentHeaderTableCellRenderer);
			} else if (renderer.getGraphType() == ExistingGraphTypes.LINE || renderer.getGraphType() == ExistingGraphTypes.SCATTER) {
				currentHeaderTableCellRenderer = new XYGraphRendererCellRenderer(resultTable.getTableHeader(), defaultTableCellRenderer);
				resultTable.getTableHeader().setDefaultRenderer(currentHeaderTableCellRenderer);
			}
			nameField.setText(renderer.getName());
			yaxisNameField.setText(renderer.getYaxisName());
			xaxisNameField.setText(renderer.getXaxisName());
			
			resultTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
				
				public void columnSelectionChanged(ListSelectionEvent e) {
					//don't care			
				}
			
				public void columnRemoved(TableColumnModelEvent e) {
					//don't care			
				}
			
				public void columnMoved(TableColumnModelEvent e) {
					columnNamesInOrder.add(e.getToIndex(), columnNamesInOrder.remove(e.getFromIndex()));
					
				}
			
				public void columnMarginChanged(ChangeEvent e) {
					//don't care			
				}
			
				public void columnAdded(TableColumnModelEvent e) {
					//don't care
				}
			});
			
			if (renderer.getQuery() != null) {
				if (renderer.getQuery() instanceof StatementExecutor) {
					StatementExecutor executor = (StatementExecutor) renderer.getQuery();
					executor.addRowSetChangeListener(rowSetChangeListener);
				}
				updateTableModel(renderer.getQuery());

				//This corrects the ordering of columns in case the user modified the query and new
				//columns exists or columns were removed.

				//This also removes the mapping for columns no longer in the query
				//and adds columns with a type of NONE if they were added.
				List<String> currentColumnNamesInOrder = new ArrayList<String>();
				Map<String, DataTypeSeries> currentNameToType = new HashMap<String, DataTypeSeries>();
				for (String colName : renderer.getColumnNamesInOrder()) {
					if (columnNamesInOrder.contains(colName)) {
						currentColumnNamesInOrder.add(colName);
						currentNameToType.put(colName, renderer.getColumnsToDataTypes().get(colName));
					}
				}
				for (String colName : columnNamesInOrder) {
					if (!renderer.getColumnNamesInOrder().contains(colName)) {
						currentColumnNamesInOrder.add(colName);
						currentNameToType.put(colName, DataTypeSeries.NONE);
					}
				}
				
				for (String colName : currentColumnNamesInOrder) {
					if (columnNamesInOrder.indexOf(colName) != currentColumnNamesInOrder.indexOf(colName)) {
						resultTable.getColumnModel().moveColumn(columnNamesInOrder.indexOf(colName), currentColumnNamesInOrder.indexOf(colName));
					}
				}
				
				for (Map.Entry<String, String> entry : renderer.getColumnSeriesToColumnXAxis().entrySet()) {
					if (columnNamesInOrder.contains(entry.getKey())) {
						columnSeriesToColumnXAxis.put(entry.getKey(), entry.getValue());
					}
				}

				columnNamesInOrder.clear();
				columnNamesInOrder.addAll(currentColumnNamesInOrder);
				columnsToDataTypes.clear();
				columnsToDataTypes.putAll(currentNameToType);
				updateChartPreview();
			}
			
			
			queryComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					resultTableLabel.setVisible(false);
					logger.debug("Selected item is " + e.getItem());
					if (e.getStateChange() == ItemEvent.DESELECTED) {
						if (e.getItem() instanceof StatementExecutor) {
							((StatementExecutor) e.getItem()).removeRowSetChangeListener(rowSetChangeListener);
						}
					} else {
						resultTable.setVisible(false);
						if (e.getItem() instanceof StatementExecutor) {
							StatementExecutor executor = (StatementExecutor) e.getItem();
							executor.addRowSetChangeListener(rowSetChangeListener);
							updateTableModel((Query) e.getItem());
						}
					}
				}
			});
			graphTypeComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (currentHeaderTableCellRenderer != null) {
							currentHeaderTableCellRenderer.cleanup();
						}
						switch ((ExistingGraphTypes) graphTypeComboBox.getSelectedItem()) {
						case BAR:
							xaxisNameField.setVisible(false);
							xaxisNameLabel.setVisible(false);
							currentHeaderTableCellRenderer = new CategoryGraphRendererTableCellRenderer(resultTable.getTableHeader(), defaultTableCellRenderer);
							resultTable.getTableHeader().setDefaultRenderer(currentHeaderTableCellRenderer);
							break;
						case LINE:
						case SCATTER:
							currentHeaderTableCellRenderer = new XYGraphRendererCellRenderer(resultTable.getTableHeader(), defaultTableCellRenderer);
							resultTable.getTableHeader().setDefaultRenderer(currentHeaderTableCellRenderer);
							xaxisNameField.setVisible(true);
							xaxisNameLabel.setVisible(true);
							break;
						default:
							throw new IllegalStateException("Unknown graph type " + graphTypeComboBox.getSelectedItem());
						}
						if(queryComboBox.getSelectedItem() != null) {
							updateTableModel((Query) queryComboBox.getSelectedItem());
						}
					}
				}
			});
			buildUI();
		}
		
		/**
		 * Given an executor this method will change the table model to
		 * show the first result set from the query. The result set will
		 * allow users to select columns as categories or series for the 
		 * graph. 
		 */
		private void updateTableModel(Query q) {
			try {
				rs = q.fetchResultSet();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			if (rs == null) {
				resultTableLabel.setText("The current query selected returns no result sets.");
				resultTableLabel.setVisible(true);
				return;
			}
			columnNamesInOrder.clear();
			columnsToDataTypes.clear();
			columnSeriesToColumnXAxis.clear();
			try {
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					String columnName = rs.getMetaData().getColumnName(i);
					columnNamesInOrder.add(columnName);
					columnsToDataTypes.put(columnName, DataTypeSeries.NONE);
				}
			} catch (SQLException e1) {
				throw new RuntimeException(e1);
			}
			resultTable.setModel(new ResultSetTableModel(rs));
			resultTable.setVisible(true);
		}
		
		/**
		 * This will update the chart based on the new values selected by the user
		 * in this property panel. The chart generated here is a preview for the
		 * property panel.
		 */
		private void updateChartPreview() {
			JFreeChart chart = GraphRenderer.createJFreeChart(columnNamesInOrder, columnsToDataTypes, columnSeriesToColumnXAxis, rs, (ExistingGraphTypes) graphTypeComboBox.getSelectedItem(), nameField.getText(), yaxisNameField.getText(), xaxisNameField.getText());
			chartPanel.setChart(chart);
		}

		private void buildUI() {
			DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 5dlu, pref:grow", "pref, pref, pref, pref, fill:max(25; pref):grow, fill:max(25; pref):grow, pref"), panel);
			builder.append("Name", nameField);
			builder.nextLine();
			builder.append("Type", graphTypeComboBox);
			builder.nextLine();
			builder.append("Query", queryComboBox);
			builder.nextLine();
			builder.append(resultTableLabel, 3);
			builder.nextLine();
			final JScrollPane tableScrollPane = new JScrollPane(resultTable);
			tableScrollPane.setPreferredSize(new Dimension(0, 0));
			builder.append(tableScrollPane, 3);
			builder.nextLine();
			final JScrollPane chartScrollPane = new JScrollPane(chartPanel);
			chartScrollPane.setPreferredSize(new Dimension(0, 0));
			builder.append(chartScrollPane, 3);
			builder.nextLine();
			builder.append("Y Axis Label", yaxisNameField);
			builder.nextLine();
			builder.append(xaxisNameLabel, xaxisNameField);
			panel.setPreferredSize(new Dimension(400, 400));
		}
	
		public boolean hasUnsavedChanges() {
			return true;
		}
	
		public JComponent getPanel() {
			return panel;
		}
	
		public void discardChanges() {
			//do nothing
		}
	
		public boolean applyChanges() {
			String oldFileName = null;
			if (getName() != null) {
				oldFileName = getName();
			}
			// If name for the content box has not been changed to something
			// user-defined, we change it everytime a
			// different name is set in the properties panel
			if(parent.getName() != null && oldFileName!= null && (parent.getName().contains(oldFileName))) {
				parent.getParent().setUniqueName(parent, parent.getName().replace(oldFileName, getName()));
			}
			try {
				defineQuery((Query) queryComboBox.getSelectedItem());
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			setGraphType((ExistingGraphTypes) graphTypeComboBox.getSelectedItem());
			setColumnNamesInOrder(columnNamesInOrder);
			setColumnsToDataTypes(columnsToDataTypes);
			setColumnSeriesToColumnXAxis(columnSeriesToColumnXAxis);
			setYaxisName(yaxisNameField.getText());
			setXaxisName(xaxisNameField.getText());
			return true;
		}
	};
	
	/**
	 * The background colour for this renderer.
	 */
	private Color backgroundColor;
	
	/**
	 * The project this renderer is in. This allows the
	 * properties panel to tell what queries are accessible
	 * to create a graph from.
	 */
	private final WabitProject project;
	
	/**
	 * The Y axis label in the graph.
	 */
	private String yaxisName;
	
	/**
	 * The X axis label in the graph.
	 */
	private String xaxisName;
	
	/**
	 * This is the current style of graph the user has made.
	 */
	private ExistingGraphTypes graphType;
	
	/**
	 * The query the graph is based off of.
	 */
	private Query query;
	
	private final ContentBox parent;

	/**
	 * This is the ordering of the columns in the result set the user specified
	 * in the properties panel. This is preserved to have the properties panel
	 * show in the same way each time the user opens the property panel and to
	 * also decide which columns comes before another when multiple series are
	 * involved.
	 */
	private final List<String> columnNamesInOrder = new ArrayList<String>();
	
	/**
	 * This maps each column in the result set to a DataTypeSeries. The types
	 * decide how each column in the result set are used to display on the graph. 
	 */
	private final Map<String, DataTypeSeries> columnsToDataTypes = new HashMap<String, DataTypeSeries>();

	/**
	 * This map contains each column listed as a series and the column to be used as X axis values.
	 * This mapping is used for line and line-like graphs.
	 */
	private final Map<String, String> columnSeriesToColumnXAxis = new HashMap<String, String>();
	
	/**
	 * This change listener watches for changes to the query and refreshes the
	 * graph when a change occurs.
	 */
	private final RowSetChangeListener queryListener = new RowSetChangeListener() {
		public void rowAdded(RowSetChangeEvent e) {
			firePropertyChange("resultSetRowAdded", null, e.getRow());
		}
	};
	
	public GraphRenderer(ContentBox parent, WabitProject project, String uuid) {
		super(uuid);
		this.project = project;
		this.parent = parent;
	}
	
	public GraphRenderer(ContentBox parent, WabitProject project) {
		this.project = project;
		this.parent = parent;
		parent.setWidth(100);
		parent.setHeight(100);
		setName("Empty graph");
	}

	public Color getBackgroundColour() {
		return backgroundColor;
	}

	public DataEntryPanel getPropertiesPanel() {
		DataEntryPanel panel = new GraphRendererPropertyPanel(project, this);
		return panel;
	}

	public boolean renderReportContent(Graphics2D g, ContentBox contentBox,
			double scaleFactor, int pageIndex, boolean printing) {
		JFreeChart chart = null;
		try {
			if (query != null) {
				chart = GraphRenderer.createJFreeChart(columnNamesInOrder, columnsToDataTypes, columnSeriesToColumnXAxis, query.fetchResultSet(), graphType, getName(), yaxisName, xaxisName);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		if (chart == null) {
			return false;
		}
		chart.draw(g, new Rectangle2D.Double(0, 0, contentBox.getWidth(), contentBox.getHeight()));
		return false;
	}

	/**
	 * Creates a JFreeChart based on the given column and series data.
	 * Returns null if the data given cannot create a chart.
	 */
	private static JFreeChart createJFreeChart(List<String> columnNamesInOrder, Map<String, DataTypeSeries> columnsToDataTypes, Map<String, String> columnSeriesToColumnXAxis, ResultSet resultSet, ExistingGraphTypes graphType, String chartName, String yaxisName, String xaxisName) {
		if (graphType == null) {
			return null;
		}
		JFreeChart chart;
		XYDataset xyCollection;
		
		switch (graphType) {
		case BAR :
			if (!columnsToDataTypes.containsValue(DataTypeSeries.CATEGORY) || !columnsToDataTypes.containsValue(DataTypeSeries.SERIES)) {
				return null;
			}
			String categoryColumnName = findCategoryColumnName(columnsToDataTypes);
			CategoryDataset dataset = createCategoryDataset(columnNamesInOrder,
						columnsToDataTypes, resultSet, categoryColumnName);
			chart = ChartFactory.createBarChart(chartName, categoryColumnName, yaxisName, dataset, PlotOrientation.VERTICAL, true, true, false);
			return chart;
		case LINE :
			if (!columnsToDataTypes.containsValue(DataTypeSeries.SERIES)) {
				return null;
			}
			xyCollection = createSeriesCollection(
						columnSeriesToColumnXAxis, resultSet);
			if (xyCollection == null) {
				return null;
			}
			chart = ChartFactory.createXYLineChart(chartName, xaxisName, yaxisName, xyCollection, PlotOrientation.VERTICAL, true, true, false);
			return chart;
		case SCATTER :
			if (!columnsToDataTypes.containsValue(DataTypeSeries.SERIES)) {
				return null;
			}
			xyCollection = createSeriesCollection(
					columnSeriesToColumnXAxis, resultSet);
			if (xyCollection == null) {
				return null;
			}
			chart = ChartFactory.createScatterPlot(chartName, xaxisName, yaxisName, xyCollection, PlotOrientation.VERTICAL, true, true, false);
			return chart;
		default:
			throw new IllegalStateException("Unknown graph type " + graphType);
		}
	}

	/**
	 * This will find the column name that is labeled as the category column
	 * in a bar chart. If there is no category column null will be returned.
	 */
	private static String findCategoryColumnName(
			Map<String, DataTypeSeries> columnsToDataTypes) {
		String categoryColumnName = null;
		for (Map.Entry<String, DataTypeSeries> entry : columnsToDataTypes.entrySet()) {
			if (entry.getValue() == DataTypeSeries.CATEGORY) {
				categoryColumnName = entry.getKey();
				break;
			}
		}
		if (categoryColumnName == null) {
			return null;
		}
		return categoryColumnName;
	}

	private static CategoryDataset createCategoryDataset(
			List<String> columnNamesInOrder,
			Map<String, DataTypeSeries> columnsToDataTypes,
			ResultSet resultSet, String categoryColumnName) {
		List<String> category = new ArrayList<String>();
		try {
			resultSet.beforeFirst();
			int columnIndex = resultSet.findColumn(categoryColumnName);
			while (resultSet.next()) {
				if (!category.contains(resultSet.getString(columnIndex))) {
					category.add(resultSet.getString(columnIndex));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		List<String> series = new ArrayList<String>();
		for (String colName : columnNamesInOrder) {
			if (columnsToDataTypes.get(colName) == DataTypeSeries.SERIES) {
				series.add(colName);
			}
		}
		
		double[][] data = new double[series.size()][category.size()];
		try {
			resultSet.beforeFirst();
			int j = 0;
			while (resultSet.next()) {
				for (String colName : series) {
					if (logger.isDebugEnabled() && (series.indexOf(colName) == -1 || category.indexOf(resultSet.getString(categoryColumnName)) == -1)) {
						logger.debug("Index of series " + colName + " is " + series.indexOf(colName) + ", index of category " + categoryColumnName + " is " + category.indexOf(resultSet.getString(categoryColumnName)));
					}
					data[series.indexOf(colName)][category.indexOf(resultSet.getString(categoryColumnName))] += resultSet.getDouble(colName); //XXX Getting numeric values as double causes problems for BigDecimal and BigInteger.
				}
				j++;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		CategoryDataset dataset = DatasetUtilities.createCategoryDataset(series.toArray(new String[]{}), category.toArray(new String[]{}), data);
		return dataset;
	}

	/**
	 * Helper method for creating line and scatter graphs in the
	 * createJFreeChart method.
	 * @return An XYDataset for use in a JFreeChart or null if an 
	 * XYDataset cannot be created.
	 */
	private static XYDataset createSeriesCollection(
			Map<String, String> columnSeriesToColumnXAxis, ResultSet resultSet) {
		boolean allNumeric = true;
		boolean allDate = true;
		try {
			for (Map.Entry<String, String> entry : columnSeriesToColumnXAxis.entrySet()) {
				int columnType = resultSet.getMetaData().getColumnType(resultSet.findColumn(entry.getValue()));
				if (columnType != Types.DATE && columnType != Types.TIMESTAMP) {
					allDate = false;
				} 
				if (!SQL.isNumeric(columnType)) {
					allNumeric = false;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		if (allNumeric) {
			XYSeriesCollection xyCollection = new XYSeriesCollection();
			for (Map.Entry<String, String> entry : columnSeriesToColumnXAxis.entrySet()) {
				XYSeries newSeries = new XYSeries(entry.getKey());
				try {
					resultSet.beforeFirst();
					while (resultSet.next()) {
						//XXX: need to switch from double to bigDecimal if it is needed.
						newSeries.add(resultSet.getDouble(entry.getValue()), resultSet.getDouble(entry.getKey()));
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				xyCollection.addSeries(newSeries);
			}
			return xyCollection;
		} else if (allDate) {
			TimePeriodValuesCollection timeCollection = new TimePeriodValuesCollection();
			for (Map.Entry<String, String> entry : columnSeriesToColumnXAxis.entrySet()) {
				TimePeriodValues newSeries = new TimePeriodValues(entry.getKey());
				try {
					resultSet.beforeFirst();
					while (resultSet.next()) {
						int columnType = resultSet.getMetaData().getColumnType(resultSet.findColumn(entry.getValue()));
						if (columnType == Types.DATE) {
							newSeries.add(new FixedMillisecond(resultSet.getDate(entry.getValue())), resultSet.getDouble(entry.getKey()));
						} else if (columnType == Types.TIMESTAMP){
							newSeries.add(new FixedMillisecond(resultSet.getTimestamp(entry.getValue())), resultSet.getDouble(entry.getKey()));
						}
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				timeCollection.addSeries(newSeries);
			}
			return timeCollection;
		} else {
			return null;
		}
	}
	
	public void resetToFirstPage() {
		//do nothing.
	}

	public boolean allowsChildren() {
		return false;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		return 0;
	}

	public List<? extends WabitObject> getChildren() {
		return new ArrayList<WabitObject>();
	}

	public ExistingGraphTypes getGraphType() {
		return graphType;
	}

	public void setGraphType(ExistingGraphTypes graphType) {
		firePropertyChange("graphType", this.graphType, graphType);
		this.graphType = graphType;
	}

	public Query getQuery() {
		return query;
	}

	public void defineQuery(Query query) throws SQLException {
		if (this.query instanceof StatementExecutor) {
			if (this.query != null) {
				((StatementExecutor) this.query).removeRowSetChangeListener(queryListener);
			}
		}
		if (query instanceof StatementExecutor) {
			((StatementExecutor) query).addRowSetChangeListener(queryListener);
		}
		this.query = query;
	}
	
	public List<String> getColumnNamesInOrder() {
		return columnNamesInOrder;
	}
	
	public void setColumnNamesInOrder(List<String> newColumnOrdering) {
		firePropertyChange("columnNamesInOrder", this.columnNamesInOrder, newColumnOrdering);
		columnNamesInOrder.clear();
		columnNamesInOrder.addAll(newColumnOrdering);
	}

	public Map<String, DataTypeSeries> getColumnsToDataTypes() {
		return columnsToDataTypes;
	}
	
	public void setColumnsToDataTypes(Map<String, DataTypeSeries> columnsToDataTypes) {
		firePropertyChange("columnsToDataTypes", this.columnsToDataTypes, columnsToDataTypes);
		this.columnsToDataTypes.clear();
		this.columnsToDataTypes.putAll(columnsToDataTypes);
	}

	public void setYaxisName(String yaxisName) {
		firePropertyChange("yaxisName", this.yaxisName, yaxisName);
		this.yaxisName = yaxisName;
	}

	public String getYaxisName() {
		return yaxisName;
	}

	public Map<String, String> getColumnSeriesToColumnXAxis() {
		return columnSeriesToColumnXAxis;
	}
	
	public void setColumnSeriesToColumnXAxis(Map<String, String> newMapping) {
		columnSeriesToColumnXAxis.clear();
		columnSeriesToColumnXAxis.putAll(newMapping);
	}

	public void setXaxisName(String xaxisName) {
		this.xaxisName = xaxisName;
	}

	public String getXaxisName() {
		return xaxisName;
	}
	
	public Dataset createDataset() {
		try {
			switch (graphType) {
			case BAR:
				return GraphRenderer.createCategoryDataset(columnNamesInOrder, columnsToDataTypes, query.fetchResultSet(), GraphRenderer.findCategoryColumnName(columnsToDataTypes));
			case LINE:
			case SCATTER:
				return GraphRenderer.createSeriesCollection(columnSeriesToColumnXAxis, query.fetchResultSet());
			default :
				throw new IllegalStateException("Unknown graph type " + graphType);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
}
