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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
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
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

import ca.sqlpower.sql.RowSetChangeEvent;
import ca.sqlpower.sql.RowSetChangeListener;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.query.StatementExecutor;
import ca.sqlpower.swingui.table.CleanupTableCellRenderer;
import ca.sqlpower.swingui.table.EditableJTable;
import ca.sqlpower.swingui.table.ResultSetTableModel;
import ca.sqlpower.util.WebColour;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.olap.MemberHierarchyComparator;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.chart.AxisColumnIdentifier;
import ca.sqlpower.wabit.report.chart.ColumnIdentifier;
import ca.sqlpower.wabit.report.chart.ColumnNameColumnIdentifier;
import ca.sqlpower.wabit.report.chart.PositionColumnIdentifier;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent;
import ca.sqlpower.wabit.swingui.olap.CellSetViewer;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This class will render a graph from a query's result set in a graph format
 * defined by the user.
 */
public class GraphRenderer extends AbstractWabitObject implements ReportContentRenderer {
	
	private static final Logger logger = Logger.getLogger(GraphRenderer.class);

    /**
     * This separator is used to separate category names when more then one
     * column is selected as the category in a bar chart.
     */
	private static final String CATEGORY_SEPARATOR = ", ";
	
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
		CATEGORY_LINE,
		LINE,
		SCATTER
	}
	
	/**
	 * The possible positions a legend can occupy on a chart
	 */
	public enum LegendPosition {
		NONE,
		TOP,
		LEFT,
		RIGHT,
		BOTTOM
	}

	/**
	 * This object is used to define a row in a category dataset for an OLAP dataset. Each row
	 * can be defined by a combination of a {@link Position} and any number of strings which 
	 * are the values in the columns defined as categories. A position is always less than
	 * a string for the comparison and a shorter list is less than a longer one.
	 */
	private static class ComparableCategoryRow implements Comparable<ComparableCategoryRow> {

	    /**
	     * This list contains the elements being compared to in the order they are to be compared.
	     */
	    private final List<Object> comparableObjects = new ArrayList<Object>();
	    
	    private final MemberHierarchyComparator comparator = new MemberHierarchyComparator();
	    
        public int compareTo(ComparableCategoryRow o) {
            int i;
            for (i = 0; i < comparableObjects.size(); i++) {
                if (o.comparableObjects.size() == i) return 1;
                
                Object thisObject = comparableObjects.get(i);
                Object otherObject = o.comparableObjects.get(i);
                
                if (thisObject instanceof String && otherObject instanceof Position) return 1;
                if (thisObject instanceof Position && otherObject instanceof String) return -1;
                if (thisObject instanceof String && otherObject instanceof String) {
                    int comparedValue = ((String) thisObject).compareTo((String) otherObject);
                    if (comparedValue != 0) return comparedValue;
                } else if (thisObject instanceof Position && otherObject instanceof Position) {
                    int j;
                    final Position thisPosition = (Position) thisObject;
                    final Position otherPosition = (Position) otherObject;
                    for (j = 0; j < thisPosition.getMembers().size(); j++) {
                        if (otherPosition.getMembers().size() == j) return 1;
                        Member thisMember = thisPosition.getMembers().get(j);
                        Member otherMember = otherPosition.getMembers().get(j);
                        int comparedValue = comparator.compare(thisMember, otherMember);
                        if (comparedValue != 0) return comparedValue;
                    }
                    if (j < otherPosition.getMembers().size()) return -1;
                }
            }
            if (i < o.comparableObjects.size()) return -1;
            return 0;
        }

        public void add(String formattedValue) {
            comparableObjects.add(formattedValue);
        }

        public void add(Position position) {
            comparableObjects.add(position);
        }
        
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            boolean first = true;
            for (Object o : comparableObjects) {
                if (!first) sb.append(", ");
                if (o instanceof String) {
                    sb.append((String) o);
                } else if (o instanceof Position) {
                    boolean firstMember = true;
                    for (Member member : ((Position) o).getMembers()) {
                        if (!first || !firstMember) sb.append(", ");
                        sb.append(member.getName());
                        firstMember = false;
                    }
                }
                first = false;
            }
            return sb.toString();
        }
	    
	}
	
	/**
	 * This is the property panel for a GraphRenderer object. This will
	 * let users specify the type of graph to display as well as what
	 * values the graph will display.
	 */
	private class ChartRendererPropertyPanel implements DataEntryPanel {
		
		/**
		 * This cell renderer is used to add combo boxes to the headers of tables returned in 
		 * the properties panel. The combo boxes will allow users to define columns to be categories
		 * or series. This is for relational queries only.
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
		 * table for line and scatter graphs. This is for relational queries only.
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
						String colSeriesName = ((ColumnNameColumnIdentifier) columnNamesInOrder.get(columnIndexAtX)).getColumnName();
						columnsToDataTypes.put(new ColumnNameColumnIdentifier(colSeriesName), (DataTypeSeries) e.getItem());
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
						columnSeriesToColumnXAxis.put(columnNamesInOrder.get(tableHeader.getColumnModel().getColumnIndexAtX(sourceCombo.getX())), new ColumnNameColumnIdentifier((String) e.getItem()));
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
				for (ColumnIdentifier identifier : columnNamesInOrder) {
				    String col = ((ColumnNameColumnIdentifier) identifier).getColumnName();
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
         * This layout manager is used for laying out components above a
         * cellset's table header where the header is a row header. This lets
         * users label or define values above a row header. The specific use of
         * this at current is to add combo boxes above the rows axis to define
         * row hierarchies as categories in a bar chart. If more components are
         * in the container then there are hierarchies in the table header the
         * addtional components will be ignored.
         */
		private class CellSetTableHeaderRowLayoutManager implements LayoutManager {
		    
		    private final CellSetTableHeaderComponent tableHeader;

            public CellSetTableHeaderRowLayoutManager(CellSetTableHeaderComponent tableHeader) {
                this.tableHeader = tableHeader;
		    }

            public void layoutContainer(Container parent) {
                int x = 0;
                for (int i = 0; i < tableHeader.getHierarchies().size(); i++) {
                    if (i >= parent.getComponentCount()) return;
                    
                    final int columnWidth = tableHeader.getHierarchies().get(i).getWidth();
                    final int preferredHeight = (int) parent.getComponent(i).getPreferredSize().getHeight();
                    parent.getComponent(i).setBounds(x, 0, columnWidth, preferredHeight);
                    x += columnWidth;
                }
            }

            public Dimension minimumLayoutSize(Container parent) {
                return preferredLayoutSize(parent);
            }

            public Dimension preferredLayoutSize(Container parent) {
                JComboBox comboBox = new JComboBox();
                comboBox.paint(parent.getGraphics());
                return new Dimension(tableHeader.getWidth(), (int) comboBox.getPreferredSize().getHeight());
            }
            
            public void addLayoutComponent(String name, Component comp) {
                //do nothing
            }

            public void removeLayoutComponent(Component comp) {
                //do nothing
            }

		}
		
		/**
		 * This layout manager is used to synchronize the positions of the columns 
		 * in the CellSetViewer. Each component added to a panel with this layout
		 * will be placed in the next available column. The first component will
		 * be placed above the first column in the cell set. Each additional
		 * component will be placed over each column. If there are more components
		 * then columns only the first n components will be shown where n is
		 * the number of columns in the table.
		 */
		private class OlapTableHeaderLayoutManager implements LayoutManager {

		    private final JTable table;

            public OlapTableHeaderLayoutManager(JTable table) {
                this.table = table;
		    }

            public void layoutContainer(Container parent) {
                List<Component> components = Arrays.asList(parent.getComponents());
                int x = 0;
                
                final int tableSize = table.getColumnCount();
                for (int i = 0; i < tableSize; i++) {
                    if (i >= components.size()) return;
                    
                    final int columnWidth = table.getColumnModel().getColumn(i).getWidth();
                    final int preferredHeight = (int) components.get(i).getPreferredSize().getHeight();
                    components.get(i).setBounds(x, 0, columnWidth, preferredHeight);
                    x += columnWidth;
                }
            }

            public Dimension minimumLayoutSize(Container parent) {
                return preferredLayoutSize(parent);
            }

            public Dimension preferredLayoutSize(Container parent) {
                int maxHeight = 0;
                for (Component child : parent.getComponents()) {
                    maxHeight = Math.max(maxHeight, (int) child.getPreferredSize().getHeight());
                }
                return new Dimension(table.getWidth(), maxHeight);
            }
            
            public void addLayoutComponent(String name, Component comp) {
                //do nothing
            }

            public void removeLayoutComponent(Component comp) {
                //do nothing
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
		 * A combo box containing all the queries in the workspace. This
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
		 * This will be null if an OLAP query is being used.
		 */
		private ResultSet rs;
		
		/**
		 * This is the most recent cell set being displayed. This will be null
		 * if a relational query is being used.
		 */
		private CellSet cellSet;
		
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
		 * This combo box contains all the possible positions the legend can occupy on
		 * the chart		
		 */
		private final JComboBox legendPositionComboBox;

		
		/**
		 * This tracks the ordering of the actual column names, not the display names.
		 * This allows us to get back the original column name from the column index.
		 * and to track the ordering of the columns for displaying the properties
		 * panel again.
		 */
		private final List<ColumnIdentifier> columnNamesInOrder = new ArrayList<ColumnIdentifier>(); 

		/**
		 * This maps the column names for each column to the data type series.
		 */
		private final Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes = new HashMap<ColumnIdentifier, DataTypeSeries>();

		/**
		 * This map tracks the columns that are defined as series and the column
		 * defined to be the values displayed on the x axis.
		 */
		private final Map<ColumnIdentifier, ColumnIdentifier> columnSeriesToColumnXAxis = new HashMap<ColumnIdentifier, ColumnIdentifier>();
		
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

        /**
         * This scroll pane shows a table that allows users to edit the values
         * in the chart. The scroll pane can be set to different things like the
         * result set table for relational queries or an olap query builder for
         * olap queries.
         */
        private JScrollPane tableScrollPane;

        /**
         * This panel shows a table that allows users to edit the values in the
         * chart. The panel can be set to different things like the result set
         * table for relational queries or an olap query builder for olap
         * queries.
         */
        private JPanel chartEditorPanel;

		public ChartRendererPropertyPanel(WabitWorkspace workspace, final GraphRenderer renderer) {
			defaultTableCellRenderer = resultTable.getTableHeader().getDefaultRenderer();
			List<WabitObject> queries = new ArrayList<WabitObject>();
			queries.addAll(workspace.getQueries());
			queries.addAll(workspace.getOlapQueries());
			queryComboBox = new JComboBox(queries.toArray());
			graphTypeComboBox = new JComboBox(ExistingGraphTypes.values());
			legendPositionComboBox = new JComboBox(LegendPosition.values());
	        	        
			queryComboBox.setSelectedItem(renderer.getQuery());
			graphTypeComboBox.setSelectedItem(renderer.getGraphType());
			if (renderer.getGraphType() == ExistingGraphTypes.BAR || renderer.getGraphType() == ExistingGraphTypes.CATEGORY_LINE) {
				currentHeaderTableCellRenderer = new CategoryGraphRendererTableCellRenderer(resultTable.getTableHeader(), defaultTableCellRenderer);
				resultTable.getTableHeader().setDefaultRenderer(currentHeaderTableCellRenderer);
			} else if (renderer.getGraphType() == ExistingGraphTypes.LINE || renderer.getGraphType() == ExistingGraphTypes.SCATTER) {
				currentHeaderTableCellRenderer = new XYGraphRendererCellRenderer(resultTable.getTableHeader(), defaultTableCellRenderer);
				resultTable.getTableHeader().setDefaultRenderer(currentHeaderTableCellRenderer);
			}
			if(renderer.getLegendPosition() != null) {
			legendPositionComboBox.setSelectedItem(renderer.getLegendPosition());
			} else {
				legendPositionComboBox.setSelectedItem(LegendPosition.BOTTOM);
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
			
			queryComboBox.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
					resultTableLabel.setVisible(false);
					logger.debug("Selected item is " + e.getItem());
					if (e.getStateChange() == ItemEvent.DESELECTED) {
						if (e.getItem() instanceof StatementExecutor) {
							((StatementExecutor) e.getItem()).removeRowSetChangeListener(rowSetChangeListener);
						}
					} else {
						if (e.getItem() instanceof StatementExecutor) {
							StatementExecutor executor = (StatementExecutor) e.getItem();
							executor.addRowSetChangeListener(rowSetChangeListener);
						}
						try {
                            updateEditor((WabitObject) e.getItem());
                        } catch (OlapException e1) {
                            throw new RuntimeException(e1);
                        } catch (SQLException e1) {
                            throw new RuntimeException(e1);
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
						case CATEGORY_LINE:
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
						    final WabitObject selectedItem = (WabitObject) queryComboBox.getSelectedItem();
						    try {
                                updateEditor(selectedItem);
                            } catch (OlapException e1) {
                                throw new RuntimeException(e1);
                            } catch (SQLException e1) {
                                throw new RuntimeException(e1);
                            }
						}
					}
				}
			});
			
			legendPositionComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if(chartPanel != null) {
							updateChartPreview();
						}
					}
				}
			});
			buildUI();
			
			if (renderer.getQuery() != null) {
			    if (renderer.getQuery() instanceof StatementExecutor) {
                    StatementExecutor executor = (StatementExecutor) renderer.getQuery();
                    executor.addRowSetChangeListener(rowSetChangeListener);
                }
                
                //This corrects the ordering of columns in case the user modified the query and new
                //columns exists or columns were removed.

                //This also removes the mapping for columns no longer in the query
                //and adds columns with a type of NONE if they were added.
			    
			    try {
			        if (renderer.getQuery() instanceof QueryCache) {
			            rs = ((QueryCache) renderer.getQuery()).fetchResultSet();
			        } else if (renderer.getQuery() instanceof OlapQuery) {
			            cellSet = ((OlapQuery) renderer.getQuery()).execute();
			        }
                    resetChartColumns();
                } catch (Exception e1) {
                    throw new RuntimeException(e1);
                }
                List<ColumnIdentifier> currentColumnNamesInOrder = new ArrayList<ColumnIdentifier>();
                Map<ColumnIdentifier, DataTypeSeries> currentNameToType = new HashMap<ColumnIdentifier, DataTypeSeries>();
                for (ColumnIdentifier identifier : renderer.getColumnNamesInOrder()) {
                    if (columnNamesInOrder.contains(identifier)) {
                        currentColumnNamesInOrder.add(identifier);
                        currentNameToType.put(identifier, renderer.getColumnsToDataTypes().get(identifier));
                    }
                }
                for (ColumnIdentifier identifier : columnNamesInOrder) {
                    if (!renderer.getColumnNamesInOrder().contains(identifier)) {
                        currentColumnNamesInOrder.add(identifier);
                        currentNameToType.put(identifier, DataTypeSeries.NONE);
                    }
                }
                
                for (ColumnIdentifier identifier : currentColumnNamesInOrder) {
                    if (columnNamesInOrder.indexOf(identifier) != currentColumnNamesInOrder.indexOf(identifier)) {
                        resultTable.getColumnModel().moveColumn(columnNamesInOrder.indexOf(identifier), currentColumnNamesInOrder.indexOf(identifier));
                    }
                }
                
                for (Map.Entry<ColumnIdentifier, ColumnIdentifier> entry : renderer.getColumnSeriesToColumnXAxis().entrySet()) {
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
			
			//displaying the chart editor is done after a graphics is available to lay out
			//the OLAP editor.
			panel.addAncestorListener(new AncestorListener() {
	            
                public void ancestorRemoved(AncestorEvent event) {
                    //do nothing
                }
            
                public void ancestorMoved(AncestorEvent event) {
                    //do nothing
                }
            
                public void ancestorAdded(AncestorEvent event) {
                    if (panel.getGraphics() != null && renderer.getQuery() != null) {
                        if (renderer.getQuery() instanceof QueryCache) {
                            updateTableModel();
                        } else if (renderer.getQuery() instanceof OlapQuery) {
                            displayOlapChartEditor();
                        } else {
                            throw new IllegalStateException("Cannot set chart column chooser to a query of type " + renderer.getQuery().getClass());
                        }
                    }
                }
            });
		}

        /**
         * This will display a table for editing a chart based on the result set
         * in this class. The result set will allow users to select columns as
         * categories or series for the graph.
         */
		private void updateTableModel() {
			if (rs == null) {
				resultTableLabel.setText("The current query selected returns no result sets.");
				resultTableLabel.setVisible(true);
				return;
			}
			
			resultTable.setModel(new ResultSetTableModel(rs));
            chartEditorPanel.removeAll();
            chartEditorPanel.add(tableScrollPane, BorderLayout.CENTER);
            chartEditorPanel.revalidate();
		}

        /**
         * This method displays the OLAP chart editor based on the cell set
         * defined in this class. This method will add a header to the table to
         * let users define columns or row dimensions to take part in defining
         * the chart. This will also reset the columns selected to take part in
         * defining the chart in cases where the query changed.
         */
		private void displayOlapChartEditor() {
            CellSetViewer cellSetViewer = new CellSetViewer(null, false);
            
            JComboBox comboBoxForWidth = new JComboBox(new String[]{"MMMMM"});
            double comboBoxWidth = comboBoxForWidth.getUI().getPreferredSize(comboBoxForWidth).getWidth();
            cellSetViewer.setMinColumnWidth((int) comboBoxWidth);
            
            chartEditorPanel.removeAll();
            cellSetViewer.getScrollPane().setPreferredSize(new Dimension(0, 0));
            chartEditorPanel.add(cellSetViewer.getScrollPane(), BorderLayout.CENTER);
            cellSetViewer.showCellSet(null, cellSet);
            chartEditorPanel.revalidate();

            final CellSetAxis columnAxis = cellSetViewer.getCellSet().getAxes().get(Axis.COLUMNS.axisOrdinal());
            if (graphTypeComboBox.getSelectedItem() == ExistingGraphTypes.BAR 
                    || graphTypeComboBox.getSelectedItem() == ExistingGraphTypes.CATEGORY_LINE) {
                JPanel comboBoxCellHeader = new JPanel(new OlapTableHeaderLayoutManager(cellSetViewer.getTable()));
                for (int i = 0; i < cellSetViewer.getTable().getColumnModel().getColumnCount(); i++) {
                    final Position position = columnAxis.getPositions().get(i);
                    final JComboBox comboBox = new JComboBox(DataTypeSeries.values());
                    final DataTypeSeries columnDataType = columnsToDataTypes.get(new PositionColumnIdentifier(position));
                    if (columnDataType != null) {
                        comboBox.setSelectedItem(columnDataType);
                    }
                    comboBoxCellHeader.add(comboBox);
                    comboBox.addItemListener(new ItemListener() {

                        public void itemStateChanged(ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                logger.debug("Position ordinal is " + position.getOrdinal());
                                columnsToDataTypes.put(new PositionColumnIdentifier(position), (DataTypeSeries) e.getItem());
                                logger.debug("Column data types are now " + columnsToDataTypes);
                                updateChartPreview();
                            }
                        }
                    });
                }
                Component view = cellSetViewer.getScrollPane().getColumnHeader().getView();
                JPanel columnHeader = new JPanel(new BorderLayout());
                columnHeader.add(view, BorderLayout.CENTER);
                columnHeader.add(comboBoxCellHeader, BorderLayout.NORTH);
                cellSetViewer.getScrollPane().setColumnHeaderView(columnHeader);

                JPanel rowAxisComboBoxHeader = new JPanel(new BorderLayout());
                final JComboBox comboBox = new JComboBox(new DataTypeSeries[]{DataTypeSeries.NONE, DataTypeSeries.CATEGORY});
                final CellSetAxis rowAxis = cellSet.getAxes().get(Axis.ROWS.axisOrdinal());
                final DataTypeSeries hierarchyDataType = columnsToDataTypes.get(new AxisColumnIdentifier(rowAxis));
                if (hierarchyDataType != null) {
                    comboBox.setSelectedItem(hierarchyDataType);
                }
                rowAxisComboBoxHeader.add(comboBox, BorderLayout.NORTH);
                comboBox.addItemListener(new ItemListener() {

                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            columnsToDataTypes.put(new AxisColumnIdentifier(rowAxis), (DataTypeSeries) e.getItem());
                            logger.debug("Column data types are now " + columnsToDataTypes);
                            updateChartPreview();
                        }
                    }
                });
                JPanel upperLeftCorner = new JPanel(new BorderLayout());
                upperLeftCorner.add(rowAxisComboBoxHeader, BorderLayout.NORTH);
                cellSetViewer.getScrollPane().setCorner(JScrollPane.UPPER_LEFT_CORNER, upperLeftCorner);
            } else if (graphTypeComboBox.getSelectedItem() == ExistingGraphTypes.LINE 
                    || graphTypeComboBox.getSelectedItem() == ExistingGraphTypes.SCATTER) {
                
                JPanel comboBoxCellHeader = new JPanel(new OlapTableHeaderLayoutManager(cellSetViewer.getTable()));
                for (int i = 0; i < cellSetViewer.getTable().getColumnModel().getColumnCount(); i++) {
                    JPanel columnComboBoxPanel = new JPanel(new GridLayout(2, 1));
                    
                    final Position position = columnAxis.getPositions().get(i);
                    final JComboBox seriesComboBox = new JComboBox(new DataTypeSeries[]{DataTypeSeries.NONE, DataTypeSeries.SERIES});
                    
                    List<String> positionNames = new ArrayList<String>();
                    for (Position positionForNames : columnAxis.getPositions()) {
                        StringBuffer sb = new StringBuffer();
                        for (Member member : positionForNames.getMembers()) {
                            if (sb.length() > 0) {
                                sb.append(", ");
                            }
                            sb.append(member.getName());
                        }
                        positionNames.add(sb.toString());
                    }
                    final JComboBox xAxisComboBox = new JComboBox(positionNames.toArray());
                    
                    columnComboBoxPanel.add(seriesComboBox);
                    columnComboBoxPanel.add(xAxisComboBox);
                    xAxisComboBox.setVisible(false);
                    
                    if (columnsToDataTypes.get(new PositionColumnIdentifier(position)) == DataTypeSeries.SERIES) {
                        seriesComboBox.setSelectedItem(DataTypeSeries.SERIES);
                        xAxisComboBox.setVisible(true);
                        
                        final PositionColumnIdentifier xAxisIdentifier = ((PositionColumnIdentifier) columnSeriesToColumnXAxis.get(position));
                        if (xAxisIdentifier != null) {
                            xAxisComboBox.setSelectedIndex(xAxisIdentifier.getPosition(cellSet).getOrdinal());
                        }
                    }
                    
                    seriesComboBox.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                if (e.getItem() == DataTypeSeries.SERIES) {
                                    xAxisComboBox.setVisible(true);
                                } else if (e.getItem() == DataTypeSeries.NONE) {
                                    xAxisComboBox.setVisible(false);
                                    columnSeriesToColumnXAxis.remove(new PositionColumnIdentifier(position));
                                }
                                columnsToDataTypes.put(new PositionColumnIdentifier(position), (DataTypeSeries) e.getItem());
                                updateChartPreview();
                            }
                        }
                    });
                    
                    xAxisComboBox.addItemListener(new ItemListener() {
                    
                        public void itemStateChanged(ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                columnSeriesToColumnXAxis.put(new PositionColumnIdentifier(position), new PositionColumnIdentifier(columnAxis.getPositions().get(xAxisComboBox.getSelectedIndex())));
                                updateChartPreview();
                            }
                        }
                    });
                    
                    comboBoxCellHeader.add(columnComboBoxPanel);
                }
                Component view = cellSetViewer.getScrollPane().getColumnHeader().getView();
                JPanel columnHeader = new JPanel(new BorderLayout());
                columnHeader.add(view, BorderLayout.CENTER);
                columnHeader.add(comboBoxCellHeader, BorderLayout.NORTH);
                cellSetViewer.getScrollPane().setColumnHeaderView(columnHeader);
            }
		}
		
		/**
		 * This resets the columns being displayed in a chart editor.
		 */
		private void resetChartColumns() throws SQLException {
            columnNamesInOrder.clear();
            columnsToDataTypes.clear();
            columnSeriesToColumnXAxis.clear();
            
            if (rs != null) {
                columnNamesInOrder.clear();
                columnsToDataTypes.clear();
                columnSeriesToColumnXAxis.clear();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    columnNamesInOrder.add(new ColumnNameColumnIdentifier(columnName));
                    columnsToDataTypes.put(new ColumnNameColumnIdentifier(columnName), DataTypeSeries.NONE);
                }
            } else if (cellSet != null) {
                CellSetAxis rowAxis = cellSet.getAxes().get(Axis.ROWS.axisOrdinal());
                columnNamesInOrder.add(new AxisColumnIdentifier(rowAxis));
                columnsToDataTypes.put(new AxisColumnIdentifier(rowAxis), DataTypeSeries.NONE);
                final CellSetAxis columnsAxis = cellSet.getAxes().get(Axis.COLUMNS.axisOrdinal());
                for (int i = 0; i < columnsAxis.getPositionCount(); i++) {
                    Position position = columnsAxis.getPositions().get(i);
                    columnNamesInOrder.add(new PositionColumnIdentifier(position));
                    columnsToDataTypes.put(new PositionColumnIdentifier(position), DataTypeSeries.NONE);
                }
            }
            
        }
		
		/**
		 * When the query object or the chart type is changed this method should
		 * be called to set the state of the editor. This will setup the correct
		 * result set or cell set as well as display the correct editor in a new
		 * edit state.
		 */
		private void updateEditor(WabitObject query) throws OlapException, SQLException {
		    if (query instanceof QueryCache) {
		        rs = ((QueryCache) query).fetchResultSet();
		        cellSet = null;
		    } else if (query instanceof OlapQuery) {
		    	try {
		            cellSet = ((OlapQuery) query).execute();
		    	} catch (Exception e) {
		    		throw new RuntimeException("Encountered error when executing the query, this query may be null or broken.");
		    	}
		        rs = null;
		    } else {
		        throw new IllegalArgumentException("Unknown query type " + query.getClass());
		    }
		    
		    resetChartColumns();
		    
		    if (query instanceof QueryCache) {
		        updateTableModel();
		    } else if (query instanceof OlapQuery) {
		        displayOlapChartEditor();
		    } else {
                throw new IllegalArgumentException("Unknown query type " + query.getClass());
            }
		    updateChartPreview();
		}
		
		/**
		 * This will update the chart based on the new values selected by the user
		 * in this property panel. The chart generated here is a preview for the
		 * property panel.
		 */
		private void updateChartPreview() {
			JFreeChart chart = GraphRenderer.createJFreeChart(columnNamesInOrder, columnsToDataTypes, columnSeriesToColumnXAxis, rs, cellSet, (ExistingGraphTypes) graphTypeComboBox.getSelectedItem(), (LegendPosition) legendPositionComboBox.getSelectedItem(), nameField.getText(), yaxisNameField.getText(), xaxisNameField.getText());
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
			tableScrollPane = new JScrollPane(resultTable);
			tableScrollPane.setPreferredSize(new Dimension(0, 0));
			chartEditorPanel = new JPanel(new BorderLayout());
			builder.append(chartEditorPanel, 3);
			builder.nextLine();
			final JScrollPane chartScrollPane = new JScrollPane(chartPanel);
			chartScrollPane.setPreferredSize(new Dimension(0, 0));
			builder.append(chartScrollPane, 3);
			builder.nextLine();
			builder.append("Legend Postion", legendPositionComboBox);
			builder.nextLine();
			builder.append("Y Axis Label", yaxisNameField);
			builder.nextLine();
			builder.append(xaxisNameLabel, xaxisNameField);
			builder.nextLine();
			panel.setPreferredSize(new Dimension(800, 500));
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
			setName(nameField.getText());
			try {
				defineQuery((WabitObject) queryComboBox.getSelectedItem());
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			setGraphType((ExistingGraphTypes) graphTypeComboBox.getSelectedItem());
			setLegendPosition((LegendPosition) legendPositionComboBox.getSelectedItem());
			setColumnNamesInOrder(columnNamesInOrder);
			setColumnsToDataTypes(columnsToDataTypes);
			setColumnSeriesToColumnXAxis(columnSeriesToColumnXAxis);
			setYaxisName(yaxisNameField.getText());
			setXaxisName(xaxisNameField.getText());
			missingIdentifiers.clear();
			return true;
		}
	};
	
	/**
	 * The background colour for this renderer and chart background. 
	 */
	private Color backgroundColour;
		
	/**
	 * The workspace this renderer is in. This allows the
	 * properties panel to tell what queries are accessible
	 * to create a graph from.
	 */
	private final WabitWorkspace workspace;
	
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
	 * The position of the legend in relation to the chart. This
	 * is defaulted to below the chart.
	 */
	private LegendPosition selectedLegendPosition = LegendPosition.BOTTOM;
	
	/**
	 * The query the graph is based off of. This can be either a {@link QueryCache}
	 * or an {@link OlapQuery} object.
	 */
	private WabitObject query;
	
	/**
	 * This is the ordering of the columns in the result set the user specified
	 * in the properties panel. This is preserved to have the properties panel
	 * show in the same way each time the user opens the property panel and to
	 * also decide which columns comes before another when multiple series are
	 * involved.
	 */
	private final List<ColumnIdentifier> columnNamesInOrder = new ArrayList<ColumnIdentifier>();
	
	/**
	 * This maps each column in the result set to a DataTypeSeries. The types
	 * decide how each column in the result set are used to display on the graph. 
	 */
	private final Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes = new HashMap<ColumnIdentifier, DataTypeSeries>();

	/**
	 * This map contains each column listed as a series and the column to be used as X axis values.
	 * This mapping is used for line and line-like graphs.
	 */
	private final Map<ColumnIdentifier, ColumnIdentifier> columnSeriesToColumnXAxis = new HashMap<ColumnIdentifier, ColumnIdentifier>();
	
	/**
	 * This change listener watches for changes to the query and refreshes the
	 * graph when a change occurs.
	 */
	private final RowSetChangeListener queryListener = new RowSetChangeListener() {
		public void rowAdded(RowSetChangeEvent e) {
			firePropertyChange("resultSetRowAdded", null, e.getRow());
		}
	};
	
	/**
	 * This is a listener placed on OLAP queries to find if columns removed from a query were in use
	 * in this chart. 
	 * 
	 * XXX This can be simplified when the olap4j query can be listened to and we can specifically
	 * listen for members in the column axis being removed.
	 */
	private final PropertyChangeListener olapQueryChangeListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            
            if (!(query instanceof OlapQuery)) throw new IllegalStateException("The listener to update the chart on OLAP query changes was added to a query of type " + query + " which does not extend OlapQuery.");
            
            OlapQuery olapQuery = (OlapQuery) query;
            
            CellSetAxis columnAxis;
            final CellSet cellSet;
            try {
                
                //XXX will need to be changed once the Olap4j Query can be listened to
                cellSet = olapQuery.execute();
                
                columnAxis = cellSet.getAxes().get(Axis.COLUMNS.axisOrdinal());
            } catch (Exception e) {
            	logger.warn(e);
               	return; //XXX should not squish this exception, when we can just listen to the query directly. The problem right
               	//XXX now is that if a user has a query with a graph based upon it and they are just building up the query
               	//XXX and it is broken, this will throw an error unneccesarily.
            }
            
            //XXX Positions aren't comparable so going to compare based on the unique names of their member list.
            //This can be simplified when positions become comparable or their equals method is defined.
            List<List<String>> positionMemberUniqueNamesInColumnAxis = new ArrayList<List<String>>();
            for (Position position : columnAxis.getPositions()) {
                List<String> positionMembers = new ArrayList<String>();
                for (Member member : position.getMembers()) {
                    positionMembers.add(member.getUniqueName());
                }
                positionMemberUniqueNamesInColumnAxis.add(positionMembers);
            }
            
            List<ColumnIdentifier> positionColumnsInUse = new ArrayList<ColumnIdentifier>();
            for (Map.Entry<ColumnIdentifier, DataTypeSeries> entry : columnsToDataTypes.entrySet()) {
                if (entry.getValue() != DataTypeSeries.NONE && entry.getKey() instanceof PositionColumnIdentifier) {
                    positionColumnsInUse.add(entry.getKey());
                }
            }
            
            missingIdentifiers.clear();
            for (ColumnIdentifier identifier : positionColumnsInUse) {
                if (!positionMemberUniqueNamesInColumnAxis.contains(((PositionColumnIdentifier) identifier).getUniqueMemberNames())) {
                    missingIdentifiers.add(identifier);
                }
            }
            
        }
	    
	};
	
	/**
	 * This list tracks all of the column identifiers currently in use in the query but
	 * cannot be found in the actual query object that backs this chart. The common reason
	 * for columns being missing is that the user created a chart, modified the query and
	 * removed columns in use in the chart, and then when to modify or use the chart.
	 */
	private final List<ColumnIdentifier> missingIdentifiers = new ArrayList<ColumnIdentifier>();
	
	public GraphRenderer(ContentBox parent, WabitWorkspace workspace, String uuid) {
		super(uuid);
		this.workspace = workspace;
	}
	
	public GraphRenderer(ContentBox parent, WabitWorkspace workspace) {
		this.workspace = workspace;
		parent.setWidth(100);
		parent.setHeight(100);
		setName("Empty graph");
	}
	
	public void cleanup() {
		if (query instanceof StatementExecutor) {
			((StatementExecutor) query).removeRowSetChangeListener(queryListener);
		}
	}

	public Color getBackgroundColour() {
		return backgroundColour;
	}

	public DataEntryPanel getPropertiesPanel() {
		DataEntryPanel panel = new ChartRendererPropertyPanel(workspace, this);
		return panel;
	}

	public boolean renderReportContent(Graphics2D g, ContentBox contentBox,
			double scaleFactor, int pageIndex, boolean printing) {
	    if (!missingIdentifiers.isEmpty()) {
	        int fontHeight = g.getFontMetrics().getHeight();
	        int startingYPos = (int) ((contentBox.getHeight() - fontHeight) / 2);
	        String errorString = "There are columns missing from the query but used in the chart.";
            g.drawString(errorString, (int) ((contentBox.getWidth() - g.getFontMetrics().stringWidth(errorString)) / 2), startingYPos);
	        errorString = "Edit the query to update the columns.";
	        g.drawString(errorString, (int) ((contentBox.getWidth() - g.getFontMetrics().stringWidth(errorString)) / 2), fontHeight + startingYPos);
	        return false;
	    }
	        
		JFreeChart chart = null;
		try {
			if (query != null) {
			    if (query instanceof QueryCache) {
			        chart = GraphRenderer.createJFreeChart(columnNamesInOrder, columnsToDataTypes, columnSeriesToColumnXAxis, ((QueryCache) query).fetchResultSet(), graphType, selectedLegendPosition, getName(), yaxisName, xaxisName);
			    } else if (query instanceof OlapQuery) {
			        chart = GraphRenderer.createJFreeChart(columnNamesInOrder, columnsToDataTypes, columnSeriesToColumnXAxis, ((OlapQuery) query).execute(), graphType, selectedLegendPosition, getName(), yaxisName, xaxisName);
			    } else {
			        throw new IllegalStateException("Unknown query type " + query.getClass() + " when trying to create a chart.");
			    }
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (chart == null) {
			return false;
		}
		chart.draw(g, new Rectangle2D.Double(0, 0, contentBox.getWidth(), contentBox.getHeight()));
		return false;
	}

    /**
     * Creates a JFreeChart based on the given column and series data. Returns
     * null if the data given cannot create a chart.
     * 
     * @param columnNamesInOrder
     *            A list of {@link ColumnIdentifier}s that define the order the
     *            columns are in. This is used to decide the order the columns
     *            marked as series come in when creating the chart.
     * @param columnsToDataTypes
     *            This maps the {@link ColumnIdentifier}s to a data type that
     *            defines how the column is used in the chart. This is used for
     *            bar charts.
     * @param columnSeriesToColumnXAxis
     *            This maps {@link ColumnIdentifier}s defined to be series in a
     *            chart to columns that are used as the x-axis values. This is
     *            used for line and scatter charts.
     * @param resultSet
     *            The result set to take values from for chart data.
     * @param graphType
     *            The type of graph to create.
     * @param legendPosition
     *            Where the legend should go in the chart.
     * @param chartName
     *            The name of the chart.
     * @param yaxisName
     *            The name of the y-axis of the chart.
     * @param xaxisName
     *            The name of the x-axis of the chart.
     */
	public static JFreeChart createJFreeChart(List<ColumnIdentifier> columnNamesInOrder, 
	        Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes, 
	        Map<ColumnIdentifier, ColumnIdentifier> columnSeriesToColumnXAxis, 
	        ResultSet resultSet, ExistingGraphTypes graphType, LegendPosition legendPosition, 
	        String chartName, String yaxisName, String xaxisName) {
	    return createJFreeChart(columnNamesInOrder, columnsToDataTypes,
	            columnSeriesToColumnXAxis, resultSet, null, graphType,
	            legendPosition, chartName, yaxisName, xaxisName);
	}
	
	/**
     * Creates a JFreeChart based on the given column and series data. Returns
     * null if the data given cannot create a chart.
     * 
     * @param columnNamesInOrder
     *            A list of {@link ColumnIdentifier}s that define the order the
     *            columns are in. This is used to decide the order the columns
     *            marked as series come in when creating the chart.
     * @param columnsToDataTypes
     *            This maps the {@link ColumnIdentifier}s to a data type that
     *            defines how the column is used in the chart. This is used for
     *            bar charts.
     * @param columnSeriesToColumnXAxis
     *            This maps {@link ColumnIdentifier}s defined to be series in a
     *            chart to columns that are used as the x-axis values. This is
     *            used for line and scatter charts.
     * @param cellSet
     *            The cell set to take values from for chart data.
     * @param graphType
     *            The type of graph to create.
     * @param legendPosition
     *            Where the legend should go in the chart.
     * @param chartName
     *            The name of the chart.
     * @param yaxisName
     *            The name of the y-axis of the chart.
     * @param xaxisName
     *            The name of the x-axis of the chart.
     */
    public static JFreeChart createJFreeChart(List<ColumnIdentifier> columnNamesInOrder, 
            Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes, 
            Map<ColumnIdentifier, ColumnIdentifier> columnSeriesToColumnXAxis, 
            CellSet cellSet, ExistingGraphTypes graphType, LegendPosition legendPosition, 
            String chartName, String yaxisName, String xaxisName) {
        return createJFreeChart(columnNamesInOrder, columnsToDataTypes,
                columnSeriesToColumnXAxis, null, cellSet, graphType,
                legendPosition, chartName, yaxisName, xaxisName);
    }

    /**
     * Creates a JFreeChart based on the given column and series data. Returns
     * null if the data given cannot create a chart. If the chart is to be
     * created with a result set then the cellSet should be null. If a chart is
     * to be created with a cell set then the resultSet should be null. Only one
     * of the two values should not be null.
     */
	private static JFreeChart createJFreeChart(List<ColumnIdentifier> columnNamesInOrder, 
	        Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes, 
	        Map<ColumnIdentifier, ColumnIdentifier> columnSeriesToColumnXAxis, 
	        ResultSet resultSet, CellSet cellSet, ExistingGraphTypes graphType, LegendPosition legendPosition, 
	        String chartName, String yaxisName, String xaxisName) {
		if (graphType == null) {
			return null;
		}
		RectangleEdge rEdge = RectangleEdge.BOTTOM;
		boolean showLegend = true;
		switch (legendPosition) {
		case NONE: showLegend = false;
					break;
		case TOP: rEdge = RectangleEdge.TOP; 
					break;
		case LEFT: rEdge = RectangleEdge.LEFT; 
					break;
		case RIGHT: rEdge = RectangleEdge.RIGHT; 
					break;
		case BOTTOM: break;
		default:
			throw new IllegalStateException("Unknown legend position " + legendPosition);
		}
		
		JFreeChart chart;
		XYDataset xyCollection;
		ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
		BarRenderer.setDefaultBarPainter(new StandardBarPainter());
		
		switch (graphType) {
		case BAR :
		case CATEGORY_LINE:
			if (!columnsToDataTypes.containsValue(DataTypeSeries.CATEGORY) || !columnsToDataTypes.containsValue(DataTypeSeries.SERIES)) {
				return null;
			}
			List<ColumnIdentifier> categoryColumns = findCategoryColumnNames(columnNamesInOrder, columnsToDataTypes);
			CategoryDataset dataset;
			if (resultSet != null) {
			    dataset = createCategoryDataset(columnNamesInOrder,
			            columnsToDataTypes, resultSet, categoryColumns);
			} else if (cellSet != null) {
			    dataset = createOlapCategoryDataset(columnNamesInOrder,
                        columnsToDataTypes, cellSet, categoryColumns);
			} else {
			    return null;
			}
			List<String> categoryColumnNames = new ArrayList<String>();
			for (ColumnIdentifier identifier : categoryColumns) {
			    categoryColumnNames.add(identifier.getName());
			}
			if (graphType == ExistingGraphTypes.BAR) {
			    chart = ChartFactory.createBarChart(chartName, createCategoryName(categoryColumnNames), yaxisName, dataset, PlotOrientation.VERTICAL, showLegend, true, false);
			} else if (graphType == ExistingGraphTypes.CATEGORY_LINE) {
			    chart = ChartFactory.createLineChart(chartName, createCategoryName(categoryColumnNames), yaxisName, dataset, PlotOrientation.VERTICAL, showLegend, true, false);
			} else {
			    throw new IllegalArgumentException("Unknown graph type " + graphType + " for a category dataset.");
			}
			if (legendPosition != LegendPosition.NONE) {
				chart.getLegend().setPosition(rEdge);
				chart.getTitle().setPadding(4,4,15,4);
			}
			
			CategoryItemRenderer renderer = chart.getCategoryPlot().getRenderer();
			int seriesSize = chart.getCategoryPlot().getDataset().getRowCount();
			for (int i = 0; i < seriesSize; i++) {
				//XXX:AS LONG AS THERE ARE ONLY 10 SERIES!!!!
				renderer.setSeriesPaint(i, ColourScheme.BREWER_SET19.get(i));
			}
			if (renderer instanceof BarRenderer) {
			    BarRenderer barRenderer = (BarRenderer) renderer;
			    barRenderer.setShadowVisible(false);
			}
			setTransparentChartBackground(chart);
			return chart;
		case LINE :
			if (!columnsToDataTypes.containsValue(DataTypeSeries.SERIES)) {
				return null;
			}
			if (resultSet != null) {
			    xyCollection = createSeriesCollection(
			            columnSeriesToColumnXAxis, resultSet);
			} else if (cellSet != null) {
			    xyCollection = createOlapSeriesCollection(columnSeriesToColumnXAxis, cellSet);
			} else {
			    return null;
			}
			if (xyCollection == null) {
				return null;
			}
			chart = ChartFactory.createXYLineChart(chartName, xaxisName, yaxisName, xyCollection, PlotOrientation.VERTICAL, showLegend, true, false);
			if (legendPosition != LegendPosition.NONE) {
				chart.getLegend().setPosition(rEdge);
				chart.getTitle().setPadding(4,4,15,4);
			}
			final XYItemRenderer xyirenderer = chart.getXYPlot().getRenderer();
			int xyLineSeriesSize = chart.getXYPlot().getDataset().getSeriesCount();
			for (int i = 0; i < xyLineSeriesSize; i++) {
				//XXX:AS LONG AS THERE ARE ONLY 10 SERIES!!!!
				xyirenderer.setSeriesPaint(i, ColourScheme.BREWER_SET19.get(i));
			}
			setTransparentChartBackground(chart);
			return chart;
		case SCATTER :
			if (!columnsToDataTypes.containsValue(DataTypeSeries.SERIES)) {
				return null;
			}
			if (resultSet != null) {
                xyCollection = createSeriesCollection(
                        columnSeriesToColumnXAxis, resultSet);
            } else if (cellSet != null) {
                xyCollection = createOlapSeriesCollection(columnSeriesToColumnXAxis, cellSet);
            } else {
                return null;
            }
			if (xyCollection == null) {
				return null;
			}
			chart = ChartFactory.createScatterPlot(chartName, xaxisName, yaxisName, xyCollection, PlotOrientation.VERTICAL, showLegend, true, false);
			if (legendPosition != LegendPosition.NONE) {
				chart.getLegend().setPosition(rEdge);
				chart.getTitle().setPadding(4,4,15,4);
			}
			final XYItemRenderer xyIrenderer = chart.getXYPlot().getRenderer();
			BasicStroke circle = new BasicStroke();
			int xyScatterSeriesSize = chart.getXYPlot().getDataset().getSeriesCount();
			for (int i = 0; i < xyScatterSeriesSize; i++) {
				xyIrenderer.setSeriesShape(i, circle.createStrokedShape(new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0)));
				//XXX:AS LONG AS THERE ARE ONLY 10 SERIES!!!!
				xyIrenderer.setSeriesPaint(i, ColourScheme.BREWER_SET19.get(i));
			}
			setTransparentChartBackground(chart);
			return chart; 
		default:
			throw new IllegalStateException("Unknown graph type " + graphType);
		}
	}
	
	private static void setTransparentChartBackground(JFreeChart chart) {
		chart.setBackgroundPaint(new Color(255,255,255,0));
		chart.getPlot().setBackgroundPaint(new Color(255,255,255,0));	
		chart.getPlot().setBackgroundAlpha(0.0f);
	}

    /**
     * This will find the columns labeled as the category column in a bar chart.
     * If there is no category column an empty list will be returned. If
     * multiple columns are selected the values in each column should be
     * appended to each other to create the value's name. The column names
     * should be ordered by the columnNamesInOrder list. This list gives users
     * the ability to define the column name order.
     */
	private static List<ColumnIdentifier> findCategoryColumnNames(
	        List<ColumnIdentifier> columnNamesInOrder,
			Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes) {
		List<ColumnIdentifier> categoryColumnNames = new ArrayList<ColumnIdentifier>();
		for (ColumnIdentifier identifier : columnNamesInOrder) {
		    if (columnsToDataTypes.get(identifier) == DataTypeSeries.CATEGORY) {
		        categoryColumnNames.add(identifier);
		    }
		}
		return categoryColumnNames;
	}
	
	/**
     * This is a helper method for creating a CategoryDataset for OLAP
     * queries. This method takes in a {@link CellSet} as well as information
     * about what columns to set as categories and series to make a dataset.
     */
    private static CategoryDataset createOlapCategoryDataset(
            List<ColumnIdentifier> columnNamesInOrder,
            Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes,
            CellSet cellSet, List<ColumnIdentifier> categoryColumnIdentifiers) {
        
        if (categoryColumnIdentifiers.isEmpty()) {
            throw new IllegalStateException("There are no categories defined when trying to create a chart.");
        }
        
        List<ComparableCategoryRow> uniqueCategoryRowNames = new ArrayList<ComparableCategoryRow>();
        CellSetAxis columnsAxis = cellSet.getAxes().get(Axis.COLUMNS.axisOrdinal());
        CellSetAxis rowsAxis = cellSet.getAxes().get(Axis.ROWS.axisOrdinal());
        if (logger.isDebugEnabled()) {
            logger.debug("column axis contains positions: ");
            for (Position pos : columnsAxis.getPositions()) {
                logger.debug("Position " + columnsAxis.getPositions().indexOf(pos));
                for (Member mem : pos.getMembers()) {
                    logger.debug("Member: " + mem.getName());
                }
            }
        }

        for (int i = 0; i < rowsAxis.getPositions().size(); i++) {
            ComparableCategoryRow categoryRow = new ComparableCategoryRow();
            for (ColumnIdentifier categoryColumnIdentifier : categoryColumnIdentifiers) {
                if (categoryColumnIdentifier instanceof PositionColumnIdentifier) {
                    PositionColumnIdentifier positionColumnIdentifier = (PositionColumnIdentifier) categoryColumnIdentifier;
                    categoryRow.add(cellSet.getCell(positionColumnIdentifier.getPosition(cellSet), rowsAxis.getPositions().get(i)).getFormattedValue());
                } else if (categoryColumnIdentifier instanceof AxisColumnIdentifier) {
                    categoryRow.add(rowsAxis.getPositions().get(i));
                } else {
                    throw new IllegalStateException("Creating a dataset on an OLAP cube. A column is used as a category but has neither a position or hierarchy.");
                }
            }
            uniqueCategoryRowNames.add(categoryRow);
        }
        
        List<Integer> seriesPositions = new ArrayList<Integer>();
        List<String> seriesNames = new ArrayList<String>();
        for (int colPosition = 0; colPosition < columnNamesInOrder.size(); colPosition++) {
            ColumnIdentifier identifier = columnNamesInOrder.get(colPosition);
            if (!(identifier instanceof PositionColumnIdentifier)) continue; //Only positions can be used as series, not hierarchies, as they are numeric.
            ColumnIdentifier colToTypeIdentifier = null;
            DataTypeSeries dataType = null;
            for (Map.Entry<ColumnIdentifier, DataTypeSeries> colToTypeIdentifierEntry : columnsToDataTypes.entrySet()) {
                if (colToTypeIdentifierEntry.getKey().equals(identifier)) {
                    colToTypeIdentifier = colToTypeIdentifierEntry.getKey();
                    dataType = colToTypeIdentifierEntry.getValue();
                    break;
                }
            }
            if (dataType != DataTypeSeries.SERIES) continue;
            
            seriesPositions.add(((PositionColumnIdentifier) identifier).getPosition(cellSet).getOrdinal());
            seriesNames.add(colToTypeIdentifier.getName());
        }
        
        double[][] data = new double[seriesPositions.size()][uniqueCategoryRowNames.size()];
        try {
            for (int row = 0; row < rowsAxis.getPositions().size(); row++) {
                for (Integer colPosition : seriesPositions) {
                    logger.debug("At row " + row + " of " + rowsAxis.getPositions().size() + " and column " + colPosition);
                    final Cell cell = cellSet.getCell(Arrays.asList(new Integer[]{colPosition, row}));
                    double value;
                    if (cell.getValue() != null) {
                        value = cell.getDoubleValue();
                    } else {
                        value = 0;
                    }
                    data[seriesPositions.indexOf(colPosition)][row] += value;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        logger.debug("Series : " + seriesNames + ", Categories " + uniqueCategoryRowNames + ", data: " + Arrays.toString(data));
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset(seriesNames.toArray(new String[]{}), uniqueCategoryRowNames.toArray(new ComparableCategoryRow[]{}), data);
        
        return dataset;
    }

    /**
     * This is a helper method for creating a CategoryDataset for relational
     * queries. This method takes in a {@link ResultSet} as well as information
     * about what columns to set as categories and series to make a dataset.
     * This is done differently from the OLAP version as they each get
     * information in different ways.
     * <p>
     * This is package private for testing.
     */
	static CategoryDataset createCategoryDataset(
			List<ColumnIdentifier> columnNamesInOrder,
			Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes,
			ResultSet resultSet, List<ColumnIdentifier> categoryColumnIdentifiers) {
	    
	    //Create a list of unique category row names to label each bar with. Category rows
	    //with the same name are currently summed.
		List<String> uniqueNamesInCategory = new ArrayList<String>();
		final List<String> categoryColumnNames = new ArrayList<String>();
		for (ColumnIdentifier identifier : categoryColumnIdentifiers) {
		    categoryColumnNames.add(((ColumnNameColumnIdentifier) identifier).getColumnName());
		}
		List<Integer> columnIndicies = new ArrayList<Integer>();
        try {
            for (String categoryColumnName : categoryColumnNames) {
                columnIndicies.add(resultSet.findColumn(categoryColumnName));
            }
			resultSet.beforeFirst();
			while (resultSet.next()) {
			    List<String> categoryRowNames = new ArrayList<String>();
			    for (Integer columnIndex : columnIndicies) {
			        categoryRowNames.add(resultSet.getString(columnIndex));
			    }
			    String categoryRowName = createCategoryName(categoryRowNames);
				if (!uniqueNamesInCategory.contains(categoryRowName)) {
					uniqueNamesInCategory.add(categoryRowName);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
        List<String> seriesColumnNames = new ArrayList<String>();
        for (ColumnIdentifier identifier : columnNamesInOrder) {
        	if (columnsToDataTypes.get(identifier) == DataTypeSeries.SERIES) {
        		seriesColumnNames.add(((ColumnNameColumnIdentifier) identifier).getColumnName());
        	}
        }
        
        double[][] data = new double[seriesColumnNames.size()][uniqueNamesInCategory.size()];
        try {
        	resultSet.beforeFirst();
        	int j = 0;
        	while (resultSet.next()) {
                List<String> categoryRowNames = new ArrayList<String>();
                for (Integer columnIndex : columnIndicies) {
                    categoryRowNames.add(resultSet.getString(columnIndex));
                }
                String categoryRowName = createCategoryName(categoryRowNames);
        		for (String colName : seriesColumnNames) {
        			if (logger.isDebugEnabled() && (seriesColumnNames.indexOf(colName) == -1 || uniqueNamesInCategory.indexOf(categoryRowName) == -1)) {
        				logger.debug("Index of series " + colName + " is " + seriesColumnNames.indexOf(colName) + ", index of category " + categoryColumnIdentifiers + " is " + uniqueNamesInCategory.indexOf(categoryRowName));
        			}
        			data[seriesColumnNames.indexOf(colName)][uniqueNamesInCategory.indexOf(categoryRowName)] += resultSet.getDouble(colName); //XXX Getting numeric values as double causes problems for BigDecimal and BigInteger.
        		}
        		j++;
        	}
        } catch (SQLException e) {
        	throw new RuntimeException(e);
        }
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset(seriesColumnNames.toArray(new String[]{}), uniqueNamesInCategory.toArray(new String[]{}), data);
		
		return dataset;
	}

	/**
	 * Simple helper method that concatenates the names of a row of categories.
	 * This way all of the category names are consistent.
	 */
	static String createCategoryName(List<String> names) {
	    StringBuilder sb = new StringBuilder();
	    if (names.size() == 0) return "";
	    sb.append(names.get(0));
	    for (int i = 1; i < names.size(); i++) {
	        sb.append(CATEGORY_SEPARATOR + names.get(i));
	    }
	    return sb.toString();
	}

    /**
	 * Helper method for creating line and scatter graphs in the
	 * createJFreeChart method. This is for relational queries only.
	 * @return An XYDataset for use in a JFreeChart or null if an 
	 * XYDataset cannot be created.
	 */
	private static XYDataset createSeriesCollection(
			Map<ColumnIdentifier, ColumnIdentifier> columnSeriesToColumnXAxis, ResultSet resultSet) {
		boolean allNumeric = true;
		boolean allDate = true;
		try {
			for (Map.Entry<ColumnIdentifier, ColumnIdentifier> entry : columnSeriesToColumnXAxis.entrySet()) {
				int columnType = resultSet.getMetaData().getColumnType(resultSet.findColumn(((ColumnNameColumnIdentifier) entry.getValue()).getColumnName()));
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
			for (Map.Entry<ColumnIdentifier, ColumnIdentifier> entry : columnSeriesToColumnXAxis.entrySet()) {
			    ColumnNameColumnIdentifier seriesColIdentifier = ((ColumnNameColumnIdentifier) entry.getKey());
			    ColumnNameColumnIdentifier xAxisColIdentifier = ((ColumnNameColumnIdentifier) entry.getValue());
				XYSeries newSeries = new XYSeries(seriesColIdentifier.getColumnName());
				try {
					resultSet.beforeFirst();
					while (resultSet.next()) {
						//XXX: need to switch from double to bigDecimal if it is needed.
						newSeries.add(resultSet.getDouble(xAxisColIdentifier.getColumnName()), resultSet.getDouble(seriesColIdentifier.getColumnName()));
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				xyCollection.addSeries(newSeries);
			}
			return xyCollection;
		} else if (allDate) {
			TimePeriodValuesCollection timeCollection = new TimePeriodValuesCollection();
			for (Map.Entry<ColumnIdentifier, ColumnIdentifier> entry : columnSeriesToColumnXAxis.entrySet()) {
			    ColumnNameColumnIdentifier seriesColIdentifier = ((ColumnNameColumnIdentifier) entry.getKey());
                ColumnNameColumnIdentifier xAxisColIdentifier = ((ColumnNameColumnIdentifier) entry.getValue());
				TimePeriodValues newSeries = new TimePeriodValues(seriesColIdentifier.getColumnName());
				try {
					resultSet.beforeFirst();
					while (resultSet.next()) {
						int columnType = resultSet.getMetaData().getColumnType(resultSet.findColumn(xAxisColIdentifier.getColumnName()));
						if (columnType == Types.DATE) {
							newSeries.add(new FixedMillisecond(resultSet.getDate(xAxisColIdentifier.getColumnName())), resultSet.getDouble(seriesColIdentifier.getColumnName()));
						} else if (columnType == Types.TIMESTAMP){
							newSeries.add(new FixedMillisecond(resultSet.getTimestamp(xAxisColIdentifier.getColumnName())), resultSet.getDouble(seriesColIdentifier.getColumnName()));
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
	
	/**
     * Helper method for creating line and scatter graphs in the
     * createJFreeChart method. This is for olap queries only.
     * @return An XYDataset for use in a JFreeChart or null if an 
     * XYDataset cannot be created.
     */
    private static XYDataset createOlapSeriesCollection(
            Map<ColumnIdentifier, ColumnIdentifier> columnSeriesToColumnXAxis, CellSet cellSet) {
        XYSeriesCollection xyCollection = new XYSeriesCollection();
        for (Map.Entry<ColumnIdentifier, ColumnIdentifier> entry : columnSeriesToColumnXAxis.entrySet()) {
            PositionColumnIdentifier seriesColIdentifier = ((PositionColumnIdentifier) entry.getKey());
            PositionColumnIdentifier xAxisColIdentifier = ((PositionColumnIdentifier) entry.getValue());
            List<String> memberNames = new ArrayList<String>();
            for (Member member : seriesColIdentifier.getPosition(cellSet).getMembers()) {
                memberNames.add(member.getName());
            }
            XYSeries newSeries = new XYSeries(createCategoryName(memberNames));
            CellSetAxis rowAxis = cellSet.getAxes().get(Axis.ROWS.axisOrdinal());
            try {
                for (int rowNumber = 0; rowNumber < rowAxis.getPositionCount(); rowNumber++) {
                    Position rowPosition = rowAxis.getPositions().get(rowNumber);
                    final Cell xCell = cellSet.getCell(xAxisColIdentifier.getPosition(cellSet), rowPosition);
                    double xValue;
                    if (xCell.getValue() != null) {
                        xValue = xCell.getDoubleValue();
                    } else {
                        xValue = 0;
                    }
                    final Cell yCell = cellSet.getCell(seriesColIdentifier.getPosition(cellSet), rowPosition);
                    double yValue;
                    if (yCell.getValue() != null) {
                        yValue = yCell.getDoubleValue();
                    } else {
                        yValue = 0;
                    }
                    newSeries.add(xValue, yValue);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            xyCollection.addSeries(newSeries);
        }
        return xyCollection;
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
	
	public LegendPosition getLegendPosition() {
		return selectedLegendPosition;
	}
	
	public void setLegendPosition(LegendPosition selectedLegendPosition) {
		firePropertyChange("legendPosition", this.selectedLegendPosition, selectedLegendPosition);
		this.selectedLegendPosition = selectedLegendPosition;
	}

	public WabitObject getQuery() {
		return query;
	}

	public void defineQuery(WabitObject query) throws SQLException {
		if (this.query instanceof StatementExecutor) {
			if (this.query != null) {
				((StatementExecutor) this.query).removeRowSetChangeListener(queryListener);
			}
		} else if (this.query instanceof OlapQuery) {
		    if (this.query != null) {
		        ((OlapQuery) this.query).removePropertyChangeListener(olapQueryChangeListener);
		    }
		}
		if (query instanceof StatementExecutor) {
			((StatementExecutor) query).addRowSetChangeListener(queryListener);
		} else if (query instanceof OlapQuery) {
		    ((OlapQuery) query).addPropertyChangeListener(olapQueryChangeListener);
		}
		this.query = query;
	}
	
	public List<ColumnIdentifier> getColumnNamesInOrder() {
		return columnNamesInOrder;
	}
	
	public void setColumnNamesInOrder(List<ColumnIdentifier> newColumnOrdering) {
		firePropertyChange("columnNamesInOrder", this.columnNamesInOrder, newColumnOrdering);
		columnNamesInOrder.clear();
		columnNamesInOrder.addAll(newColumnOrdering);
	}

	public Map<ColumnIdentifier, DataTypeSeries> getColumnsToDataTypes() {
		return columnsToDataTypes;
	}
	
	public void setColumnsToDataTypes(Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes) {
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

	public Map<ColumnIdentifier, ColumnIdentifier> getColumnSeriesToColumnXAxis() {
		return columnSeriesToColumnXAxis;
	}
	
	public void setColumnSeriesToColumnXAxis(Map<ColumnIdentifier, ColumnIdentifier> newMapping) {
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
	    if (query instanceof QueryCache) {
	        try {
	            switch (graphType) {
	            case BAR:
	            case CATEGORY_LINE:
	                return GraphRenderer.createCategoryDataset(columnNamesInOrder, columnsToDataTypes, ((QueryCache) query).fetchResultSet(), GraphRenderer.findCategoryColumnNames(columnNamesInOrder, columnsToDataTypes));
	            case LINE:
	            case SCATTER:
	                return GraphRenderer.createSeriesCollection(columnSeriesToColumnXAxis, ((QueryCache) query).fetchResultSet());
	            default :
	                throw new IllegalStateException("Unknown graph type " + graphType);
	            }
	        } catch (SQLException e) {
	            throw new RuntimeException(e);
	        }
	    } else if (query instanceof OlapQuery) {
	        try {
                switch (graphType) {
                case BAR:
                case CATEGORY_LINE:
                    return GraphRenderer.createOlapCategoryDataset(columnNamesInOrder, columnsToDataTypes, ((OlapQuery) query).execute(), GraphRenderer.findCategoryColumnNames(columnNamesInOrder, columnsToDataTypes));
                case LINE:
                case SCATTER:
                    return GraphRenderer.createOlapSeriesCollection(columnSeriesToColumnXAxis, ((OlapQuery) query).execute());
                default :
                    throw new IllegalStateException("Unknown graph type " + graphType);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
	    } else {
	        throw new IllegalStateException("Unknown query type " + query.getClass() + " when creating a " + graphType + " chart dataset.");
	    }
	}

	public List<String> getSeriesColours() {
		List<String> colourList = new ArrayList<String>();
		for(WebColour wb : ColourScheme.BREWER_SET19) {
			colourList.add(wb.toString());
		}
		return colourList;
	}

    public void processEvent(PInputEvent event, int type) {
        //do nothing at current, but maybe do cool stuff later
    }

    public List<WabitObject> getDependencies() {
        if (query == null) return Collections.emptyList();
        return Collections.singletonList(query);
    }

    public List<ColumnIdentifier> getMissingIdentifiers() {
        return Collections.unmodifiableList(missingIdentifiers);
    }
    
    public void addMissingIdentifier(ColumnIdentifier identifier) {
        missingIdentifiers.add(identifier);
    }
	
}
