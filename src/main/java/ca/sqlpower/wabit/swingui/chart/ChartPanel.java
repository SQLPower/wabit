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

package ca.sqlpower.wabit.swingui.chart;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.jfree.chart.JFreeChart;
import org.olap4j.CellSet;
import org.olap4j.OlapException;

import ca.sqlpower.sql.RowSetChangeEvent;
import ca.sqlpower.sql.RowSetChangeListener;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.swingui.query.StatementExecutor;
import ca.sqlpower.swingui.table.CleanupTableCellRenderer;
import ca.sqlpower.swingui.table.EditableJTable;
import ca.sqlpower.swingui.table.ResultSetTableModel;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.OlapUtils;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.report.chart.ColumnIdentifier;
import ca.sqlpower.wabit.report.chart.ColumnNameColumnIdentifier;
import ca.sqlpower.wabit.report.chart.ColumnRole;
import ca.sqlpower.wabit.report.chart.ExistingChartTypes;
import ca.sqlpower.wabit.report.chart.LegendPosition;
import ca.sqlpower.wabit.report.chart.PositionColumnIdentifier;
import ca.sqlpower.wabit.swingui.WabitPanel;
import ca.sqlpower.wabit.swingui.WabitSwingSession;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ChartPanel implements WabitPanel {
    
    private static final Logger logger = Logger.getLogger(ChartPanel.class);
    
    /**
     * This cell renderer is used to add combo boxes to the headers of tables returned in 
     * the properties panel. The combo boxes will allow users to define columns to be categories
     * or series. This is for relational queries only.
     * <p>
     * This is for category type charts
     */
    private class CategoryChartRendererTableCellRenderer implements CleanupTableCellRenderer {

        /**
         * This listens to all of the combo boxes that define how the column relates
         * to a category. This listener will update the current category combo box to NONE
         * if a new category is selected since there is only one category allowed at a time.
         */
        private final ItemListener dataTypeSeriesChangeListener = new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    JComboBox sourceCombo = (JComboBox) e.getSource();
                    ColumnIdentifier identifier = columnNamesInOrder.get(tableHeader.
                            getColumnModel().getColumnIndexAtX(sourceCombo.getX()));
                    identifier.setRoleInChart((ColumnRole) e.getItem());
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

        public CategoryChartRendererTableCellRenderer(final JTableHeader tableHeader, TableCellRenderer defaultTableCellRenderer) {
            this.tableHeader = tableHeader;
            this.defaultTableCellRenderer = defaultTableCellRenderer;

            tableHeader.addMouseListener(comboBoxMouseListener);
        }

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                final int column) {
            Component defaultComponent = defaultTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            final JPanel newHeader = new JPanel(new BorderLayout());
            final JComboBox dataTypeComboBox = new JComboBox(ColumnRole.values());
            try {
                String columnName = (String) columnNamesInOrder.get(column).getUniqueIdentifier();
                int rsColumnIndex = 0;
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if (rs.getMetaData().getColumnName(i).equals(columnName)) {
                        rsColumnIndex = i;
                        break;
                    }
                }
                if (!SQL.isNumeric(rs.getMetaData().getColumnType(rsColumnIndex))) {
                    dataTypeComboBox.removeItem(ColumnRole.SERIES);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            columnToComboBox.put(new Integer(column), dataTypeComboBox);
            final ColumnRole defaultDataTypeSeries = columnNamesInOrder.get(column).getRoleInChart();
            if (defaultDataTypeSeries == null) {
                dataTypeComboBox.setSelectedItem(ColumnRole.NONE);
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
     * Makes headers for the result set table for line and scatter charts.
     */
    private class XYChartRendererCellRenderer implements CleanupTableCellRenderer {

        /**
         * This listens to all of the combo boxes that define how the column relates
         * to a chart.
         */
        private final ItemListener dataTypeSeriesChangeListener = new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    JComboBox sourceCombo = (JComboBox) e.getSource();
                    final int columnIndexAtX = tableHeader.getColumnModel().getColumnIndexAtX(sourceCombo.getX());
                    ColumnIdentifier identifier = columnNamesInOrder.get(columnIndexAtX);
                    identifier.setRoleInChart((ColumnRole) e.getItem());
                    if (((ColumnRole) e.getItem()) == ColumnRole.NONE) {
                        identifier.setXAxisIdentifier(null);
                        columnToXAxisComboBox.remove(columnIndexAtX);
                    }
                    tableHeader.repaint();
                    updateChartPreview();
                }
            }
        };


        private final ItemListener xAxisValuesChangeListener = new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    JComboBox sourceCombo = (JComboBox) e.getSource();
                    final ColumnIdentifier changedColumn = columnNamesInOrder.get(
                            tableHeader.getColumnModel().getColumnIndexAtX(sourceCombo.getX()));
                    changedColumn.setXAxisIdentifier(new ColumnNameColumnIdentifier((String) e.getItem()));
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
        private final Map<Integer, JComboBox> columnToRoleComboBox = new HashMap<Integer, JComboBox>();

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
                    dataTypeComboBox = columnToRoleComboBox.get(column);
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

        public XYChartRendererCellRenderer(final JTableHeader tableHeader, TableCellRenderer defaultTableCellRenderer) {
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
            final JComboBox dataTypeComboBox = new JComboBox(ColumnRole.values());
            dataTypeComboBox.removeItem(ColumnRole.CATEGORY);
            try {
                String columnName = (String) columnNamesInOrder.get(column).getUniqueIdentifier();
                int rsColumnIndex = 0;
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if (rs.getMetaData().getColumnName(i).equals(columnName)) {
                        rsColumnIndex = i;
                        break;
                    }
                }
                if (!SQL.isNumeric(rs.getMetaData().getColumnType(rsColumnIndex))) {
                    JLabel emptyLabel = new JLabel();
                    emptyLabel.setPreferredSize(new Dimension(0, (int) dataTypeComboBox.getPreferredSize().getHeight() * 2));
                    newHeader.add(emptyLabel, BorderLayout.NORTH);
                    return newHeader;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            columnToRoleComboBox.put(new Integer(column), dataTypeComboBox);
            final ColumnRole currentRole = columnNamesInOrder.get(column).getRoleInChart();
            if (currentRole == null) {
                dataTypeComboBox.setSelectedItem(ColumnRole.NONE);
            } else {
                dataTypeComboBox.setSelectedItem(currentRole);
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
            if (currentRole == ColumnRole.SERIES) {
                ColumnIdentifier xAxis = columnNamesInOrder.get(column).getXAxisIdentifier();
                String columnName;
                if (xAxis != null) {
                    columnName = ((ColumnNameColumnIdentifier) xAxis).getColumnName();
                } else {
                    columnName = null;
                }
                comboBoxForXValues.setSelectedItem(columnName);
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
     * A field to change the chart's name in
     */
    private final JTextField nameField = new JTextField();

    /**
     * A combo box containing all the queries in the workspace. This
     * will let the user choose which query to chart.
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
     * This is the result set currently displayed by the resultTable. It may
     * have been a relational query in the first place, or it could have been
     * derived from an OlapQuery or other originally non-relational query type.
     */
    private ResultSet rs;

    /**
     * This is an error/warning label for the result table. If something 
     * goes wrong in a query this result table will display a nicer user.
     */
    private final JLabel resultTableLabel = new JLabel();

    /**
     * This combo box holds all of the chart types that the {@link ChartRenderer}
     * can generate and display.
     * <p>
     * This will eventually change to buttons to select the desired chart type.
     */
    private final JComboBox chartTypeComboBox;

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
     * This panel will display a JFreeChart that is a preview of what the
     * user has selected from the result table. This chart should look
     * identical to what would be shown on a report.
     */
    private final org.jfree.chart.ChartPanel chartPanel = new org.jfree.chart.ChartPanel(null);

    /**
     * The label that holds any errors encountered while attempting to create a
     * chart from the query. It should disappear when the chart has been created
     * successfully, but reappear if there's an error.
     */
    private final JLabel chartError = new JLabel();
    
    /**
     * This is the default renderer of the table displaying the values from the query.
     * This is stored to give the headers a normal look depending on user settings.
     */
    private final TableCellRenderer defaultTableCellRenderer;

    /**
     * This change listener will be added to the query that is selected in the combo box.
     * The change listener will update the chart that users can view as a preview.
     */
    private final RowSetChangeListener rowSetChangeListener = new RowSetChangeListener() {
        public void rowAdded(RowSetChangeEvent e) {
            updateChartPreview();
        }
    };

    /**
     * This {@link TableCellRenderer} is the current wrapper on the regular {@link TableCellRenderer}.
     * This wrapper will place appropriate combo boxes above the table headers to allow users
     * to specify if the columns are to be used as series, categories, or x-axis values in a chart.
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

    /**
     * This listener updates the column name list that keeps track of the
     * current order the columns have been arranged in. If the order of the
     * columns in the table are being arranged according to the order in
     * the {@link #columnNamesInOrder} list this listener should not be
     * attached to the table.
     */
    private final TableColumnModelListener columnMovingListener = new TableColumnModelListener() {

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
    };

    /**
     * The actual renderer we are editing.
     */
    private final Chart chart;

    public ChartPanel(WabitSwingSession session, final Chart chart) {
        WabitWorkspace workspace = session.getWorkspace();
        this.chart = chart;
        defaultTableCellRenderer = resultTable.getTableHeader().getDefaultRenderer();
        List<WabitObject> queries = new ArrayList<WabitObject>();
        queries.addAll(workspace.getQueries());
        queries.addAll(workspace.getOlapQueries());
        queryComboBox = new JComboBox(queries.toArray());
        chartTypeComboBox = new JComboBox(ExistingChartTypes.values());
        legendPositionComboBox = new JComboBox(LegendPosition.values());

        queryComboBox.setSelectedItem(chart.getQuery());
        chartTypeComboBox.setSelectedItem(chart.getType());
        if (chart.getType() == ExistingChartTypes.BAR || chart.getType() == ExistingChartTypes.CATEGORY_LINE) {
            currentHeaderTableCellRenderer = new CategoryChartRendererTableCellRenderer(resultTable.getTableHeader(), defaultTableCellRenderer);
            resultTable.getTableHeader().setDefaultRenderer(currentHeaderTableCellRenderer);
        } else if (chart.getType() == ExistingChartTypes.LINE || chart.getType() == ExistingChartTypes.SCATTER) {
            currentHeaderTableCellRenderer = new XYChartRendererCellRenderer(resultTable.getTableHeader(), defaultTableCellRenderer);
            resultTable.getTableHeader().setDefaultRenderer(currentHeaderTableCellRenderer);
        }
        if(chart.getLegendPosition() != null) {
            legendPositionComboBox.setSelectedItem(chart.getLegendPosition());
        } else {
            legendPositionComboBox.setSelectedItem(LegendPosition.BOTTOM);
        }

        nameField.setText(chart.getName());
        yaxisNameField.setText(chart.getYaxisName());
        xaxisNameField.setText(chart.getXaxisName());

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
        chartTypeComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (currentHeaderTableCellRenderer != null) {
                        currentHeaderTableCellRenderer.cleanup();
                    }
                    switch ((ExistingChartTypes) chartTypeComboBox.getSelectedItem()) {
                    case BAR:
                    case CATEGORY_LINE:
                        xaxisNameField.setVisible(false);
                        xaxisNameLabel.setVisible(false);
                        currentHeaderTableCellRenderer = new CategoryChartRendererTableCellRenderer(resultTable.getTableHeader(), defaultTableCellRenderer);
                        resultTable.getTableHeader().setDefaultRenderer(currentHeaderTableCellRenderer);
                        break;
                    case LINE:
                    case SCATTER:
                        currentHeaderTableCellRenderer = new XYChartRendererCellRenderer(resultTable.getTableHeader(), defaultTableCellRenderer);
                        resultTable.getTableHeader().setDefaultRenderer(currentHeaderTableCellRenderer);
                        xaxisNameField.setVisible(true);
                        xaxisNameLabel.setVisible(true);
                        break;
                    default:
                        throw new IllegalStateException("Unknown chart type " + chartTypeComboBox.getSelectedItem());
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

        if (chart.getQuery() != null) {
            if (chart.getQuery() instanceof StatementExecutor) {
                StatementExecutor executor = (StatementExecutor) chart.getQuery();
                executor.addRowSetChangeListener(rowSetChangeListener);
            }

            //This corrects the ordering of columns in case the user modified the query and new
            //columns exists or columns were removed.

            //This also removes the mapping for columns no longer in the query
            //and adds columns with a type of NONE if they were added.

            try {
                if (chart.getQuery() instanceof QueryCache) {
                    rs = ((QueryCache) chart.getQuery()).fetchResultSet();
                } else if (chart.getQuery() instanceof OlapQuery) {
                    CellSet cellSet = ((OlapQuery) chart.getQuery()).execute();
                    rs = OlapUtils.toResultSet(cellSet);
                }
                resetChartColumns();
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
            List<ColumnIdentifier> currentColumnNamesInOrder = new ArrayList<ColumnIdentifier>();
            Map<ColumnIdentifier, ColumnRole> currentNameToType = new HashMap<ColumnIdentifier, ColumnRole>();
            for (ColumnIdentifier identifier : chart.getColumnNamesInOrder()) {
                if (columnNamesInOrder.contains(identifier)) {
                    currentColumnNamesInOrder.add(identifier);
                }
            }
            for (ColumnIdentifier identifier : columnNamesInOrder) {
                if (!chart.getColumnNamesInOrder().contains(identifier)) {
                    currentColumnNamesInOrder.add(identifier);
                    currentNameToType.put(identifier, ColumnRole.NONE);
                }
            }

            //                for (ColumnIdentifier identifier : currentColumnNamesInOrder) {
                //                    final int newIndex = currentColumnNamesInOrder.indexOf(identifier);
            //                    if (columnNamesInOrder.indexOf(identifier) != newIndex) {
            //                        logger.debug("identifier is " + identifier.getName());
            //                        logger.debug("Moving column from " + columnNamesInOrder.indexOf(identifier) + " to " + newIndex);
            //                        logger.debug("Result table has " + resultTable.getColumnCount() + " columns.");
            //                        resultTable.getColumnModel().moveColumn(columnNamesInOrder.indexOf(identifier), newIndex);
            //                        logger.debug("Column at position " + newIndex + " is " + resultTable.getColumnName(newIndex));
            //                    }
            //                }

            columnNamesInOrder.clear();
            columnNamesInOrder.addAll(currentColumnNamesInOrder);
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
                if (panel.getGraphics() != null && chart.getQuery() != null) {
                    updateTableModel();
                }
            }
        });
    }

    /**
     * This will display a table for editing a chart based on the result set
     * in this class. The result set will allow users to select columns as
     * categories or series for the chart.
     */
    private void updateTableModel() {
        if (rs == null) {
            resultTableLabel.setText("The current query selected returns no result sets.");
            resultTableLabel.setVisible(true);
            return;
        }

        final ResultSetTableModel model = new ResultSetTableModel(rs);
        resultTable.setModel(model);

        //reorder the columns from the result set to the order the user specified by dragging column headers.
        resultTable.getColumnModel().removeColumnModelListener(columnMovingListener);
        for (ColumnIdentifier identifier : columnNamesInOrder) {
            final int newIndex = columnNamesInOrder.indexOf(identifier);
            int oldIndex = -1;
            for (int i = 0; i < resultTable.getColumnCount(); i++) {
                if (resultTable.getColumnName(i).equalsIgnoreCase((String) identifier.getUniqueIdentifier())) {
                    oldIndex = i;
                    break;
                }
            }
            if (oldIndex != newIndex) {
                resultTable.getColumnModel().moveColumn(oldIndex, newIndex);
            }
        }
        resultTable.getColumnModel().addColumnModelListener(columnMovingListener);

        chartEditorPanel.removeAll();
        chartEditorPanel.add(tableScrollPane, BorderLayout.CENTER);
        chartEditorPanel.revalidate();
    }

    /**
     * This resets the columns being displayed in a chart editor. If the current
     * result set is null, this method simply clears all the column names.
     */
    private void resetChartColumns() throws SQLException {
        columnNamesInOrder.clear();

        if (rs != null) {
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                final ColumnNameColumnIdentifier identifier = 
                    new ColumnNameColumnIdentifier(columnName);
                columnNamesInOrder.add(identifier);
            }
        }
    }

    /**
     * When the query object or the chart type is changed, this method should be
     * called to set the state of the editor. This will set up the correct result
     * set as well as display the correct editor in a new edit state.
     * <p>
     * TODO change method signature to require a ResultSetProducer interface
     */
    private void updateEditor(WabitObject query) throws SQLException {
        if (query instanceof QueryCache) {
            rs = ((QueryCache) query).fetchResultSet();
        } else if (query instanceof OlapQuery) {
            try {
                CellSet cellSet = ((OlapQuery) query).execute();
                rs = OlapUtils.toResultSet(cellSet);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Couldn't fetch data for this chart", e);
            }
        } else {
            throw new IllegalArgumentException("Unknown query type " + query.getClass());
        }

        resetChartColumns();
        updateTableModel();
        updateChartPreview();
    }

    /**
     * This will update the chart model, the preview panel, and its associated
     * error message based on the current state of the GUI components in this
     * property panel. The chart generated here is only intended as a preview
     * for the property panel.
     */
    private void updateChartPreview() {
        updateChartFromGUI();
        try {
            JFreeChart newJFreeChart = ChartSwingUtil.createChartFromQuery(chart);
            logger.debug("Created new JFree chart: " + newJFreeChart);
            chartPanel.setChart(newJFreeChart);
            chartError.setText("");
        } catch (Exception ex) {
            chartError.setText("<html>Failed to create chart:<br>" + ex);
            logger.warn("Failed to create a chart.", ex);
            chartPanel.setChart(null);
        }
    }

    private void buildUI() {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 5dlu, pref:grow", "pref, pref, pref, pref, fill:max(25; pref):grow, fill:max(25; pref):grow, pref"), panel);
        builder.append("Name", nameField);
        builder.nextLine();
        builder.append("Type", chartTypeComboBox);
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
        
        JPanel chartAndErrorPanel = new JPanel(new BorderLayout());
        chartAndErrorPanel.add(chartError, BorderLayout.NORTH);
        final JScrollPane chartScrollPane = new JScrollPane(chartPanel);
        chartScrollPane.setPreferredSize(new Dimension(0, 0));
        chartAndErrorPanel.add(chartScrollPane, BorderLayout.CENTER);
        builder.append(chartAndErrorPanel, 3);
        
        builder.nextLine();
        builder.append("Legend Postion", legendPositionComboBox);
        builder.nextLine();
        builder.append("Y Axis Label", yaxisNameField);
        builder.nextLine();
        builder.append(xaxisNameLabel, xaxisNameField);
        builder.nextLine();
        panel.setPreferredSize(new Dimension(800, 500));
    }

    /**
     * This is a helper method for finding a column identifier for a given
     * object.
     * 
     * @param uniqueIdentifier
     *            An object that uniquely identifies a column. This may need
     *            to be retrieved from one of the {@link ColumnIdentifier}
     *            classes like the {@link PositionColumnIdentifier}.
     * @return A {@link ColumnIdentifier} that is in the list of columns if
     *         the given object matches a column or null otherwise.
     */
    private ColumnIdentifier findColumnIdentifier(Object uniqueIdentifier) {
        for (ColumnIdentifier identifier : columnNamesInOrder) {
            if (identifier.getUniqueIdentifier().equals(uniqueIdentifier)) {
                return identifier;
            }
        }
        return null;
    }

    public boolean hasUnsavedChanges() {
        return false;
    }

    public JComponent getPanel() {
        return panel;
    }

    public void discardChanges() {
        logger.debug("Discarding changes (no-op)");
    }

    public boolean applyChanges() {
        logger.debug("Applying changes");
        updateChartFromGUI();
        return true;
    }

    /**
     * Updates {@link #chart}'s settings to match those currently in this
     * panel's GUI components. This is the inverse of {@link #updateGUIFromChart()}.
     */
    private void updateChartFromGUI() {
        chart.setName(nameField.getText());
        try {
            chart.defineQuery((WabitObject) queryComboBox.getSelectedItem());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        chart.setChartType((ExistingChartTypes) chartTypeComboBox.getSelectedItem());
        chart.setLegendPosition((LegendPosition) legendPositionComboBox.getSelectedItem());
        chart.clearColumnIdentifiers();
        for (ColumnIdentifier identifier : columnNamesInOrder) {
            chart.addColumnIdentifier(identifier);
        }
        chart.setYaxisName(yaxisNameField.getText());
        chart.setXaxisName(xaxisNameField.getText());
        chart.clearMissingIdentifiers();
    }

    public String getTitle() {
        return "Chart Editor";
    }

    public void maximizeEditor() {
        // no op
    }
}
