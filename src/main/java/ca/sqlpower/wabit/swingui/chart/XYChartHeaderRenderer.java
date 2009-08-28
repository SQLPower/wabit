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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.SQL;
import ca.sqlpower.wabit.report.chart.ChartColumn;
import ca.sqlpower.wabit.report.chart.ColumnRole;

/**
 * Makes headers for the result set table for line and scatter charts.
 */
class XYChartHeaderRenderer implements ChartTableHeaderCellRenderer {

    private static final Logger logger = Logger.getLogger(XYChartHeaderRenderer.class);
    
    /**
     * The chart panel this header is working for.
     */
    private final ChartPanel chartPanel;

    /**
     * This listens to all of the combo boxes that define how the column relates
     * to a chart.
     */
    private final ItemListener columnRoleChangeListener = new ItemListener() {

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                // XXX this could be done better by storing column when showing popup
                final int columnIndexAtX =
                    tableHeader.getColumnModel().getColumnIndexAtX(columnRoleBox.getX());
                ChartColumn identifier = columnNamesInOrder.get(columnIndexAtX);
                identifier.setRoleInChart((ColumnRole) e.getItem());
                if (((ColumnRole) e.getItem()) == ColumnRole.NONE) {
                    identifier.setXAxisIdentifier(null);
                }
                chartPanel.updateChartFromGUI();
                tableHeader.repaint();
            }
        }
    };


    private final ItemListener xAxisValuesChangeListener = new ItemListener() {

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                // XXX this could be done better by storing column when showing popup
                final ChartColumn changedColumn = columnNamesInOrder.get(
                        tableHeader.getColumnModel().getColumnIndexAtX(xAxisBox.getX()));
                changedColumn.setXAxisIdentifier(new ChartColumn((String) e.getItem()));
                chartPanel.updateChartFromGUI();
                tableHeader.repaint();
            }
        }
    };

    /**
     * The header we're rendering cells for. We've got a mouse listener attached to it.
     */
    private final JTableHeader tableHeader;

    /**
     * A dummy combo box that's used by {@link #getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)}.
     * <p>
     * This box contains items of type {@link ColumnRole}.
     */
    private final JComboBox columnRoleBox;

    /**
     * A dummy combo box that's used by {@link #getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)}.
     * <p>
     * This box contains items of type String.
     */
    private final JComboBox xAxisBox;

    /**
     * This header is used as the default way to render a table's cell. This
     * way cells will have a similar looking header to the default. 
     */
    private final TableCellRenderer defaultTableCellRenderer;

    private List<ChartColumn> columnNamesInOrder;

    public XYChartHeaderRenderer(
            ChartPanel chartPanel, JTableHeader tableHeader,
            TableCellRenderer defaultTableCellRenderer) throws SQLException {
        
        this.chartPanel = chartPanel;
        this.tableHeader = tableHeader;
        this.defaultTableCellRenderer = defaultTableCellRenderer;

        columnNamesInOrder = new ArrayList<ChartColumn>();
        
        String[] colNames = new String[chartPanel.getChart().getColumnNamesInOrder().size()];
        for (int i = 0; i < chartPanel.getChart().getColumnNamesInOrder().size(); i++) {
            ChartColumn col = chartPanel.getChart().getColumnNamesInOrder().get(i);
            colNames[i] = col.getName();
        }
        
        for (ChartColumn col : chartPanel.getChart().getColumnNamesInOrder()) {
            columnNamesInOrder.add(col);
        }
        tableHeader.addMouseListener(comboBoxMouseListener);
        
        columnRoleBox = new JComboBox(ColumnRole.values());
        columnRoleBox.removeItem(ColumnRole.CATEGORY); // not legal for XY charts
        columnRoleBox.addItemListener(columnRoleChangeListener);
        
        xAxisBox = new JComboBox();
        numericAndDateCols = findNumericAndDateCols();
        for (String colName : numericAndDateCols) {
            xAxisBox.addItem(colName);
        }
        xAxisBox.addItemListener(xAxisValuesChangeListener);
    }

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
                logger.debug("Didn't pass the skill-testing question");
                return;
            }
            final int column = tableHeader.getColumnModel().getColumnIndexAtX(e.getX());
            ChartColumn chartColumn = columnNamesInOrder.get(column);
            
            final JComboBox clickedBox;
            int yPosition = 0;
            if (e.getY() < new JComboBox().getPreferredSize().getHeight()) {
                clickedBox = columnRoleBox;
                clickedBox.setSelectedItem(chartColumn);
            } else if (e.getY() < new JComboBox().getPreferredSize().getHeight() * 2) {
                if (chartColumn.getRoleInChart() == ColumnRole.SERIES) {
                    ChartColumn xAxis = chartColumn.getXAxisIdentifier();
                    clickedBox = xAxisBox;
                    if (xAxis != null) {
                        clickedBox.setSelectedItem(xAxis.getName());
                    } else {
                        clickedBox.setSelectedItem(null);
                    }
                } else {
                    clickedBox = null;
                }
            } else {
                clickedBox = null;
            }
            
            if (clickedBox == null) return;
            
            tableHeader.add(clickedBox);
            clickedBox.setBounds(
                    chartPanel.getXPositionOfColumn(tableHeader.getColumnModel(), column),
                    yPosition,
                    tableHeader.getColumnModel().getColumn(column).getWidth(),
                    clickedBox.getHeight());
            clickedBox.setPopupVisible(true);
            
            // TODO this could be set up once in the constructor (for each box)
            clickedBox.addPopupMenuListener(new PopupMenuListener() {

                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    //don't care
                }

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    tableHeader.remove(clickedBox);
                    clickedBox.removePopupMenuListener(this);
                }

                public void popupMenuCanceled(PopupMenuEvent e) {
                    //don't care
                }
            });
            logger.debug("table header has components " + Arrays.toString(tableHeader.getComponents()));
        }
    };

    /**
     * All the columns of the result set whose SQL types can be considered are
     * numeric or date.
     */
    private final List<String> numericAndDateCols;

    public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            final int column) {
        Component defaultComponent = defaultTableCellRenderer.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        final JPanel newHeader = new JPanel(new BorderLayout());
        ChartColumn chartColumn = columnNamesInOrder.get(column);
        newHeader.add(makeRoleBox(chartColumn), BorderLayout.NORTH);
        newHeader.add(makeXAxisBox(chartColumn), BorderLayout.CENTER);
        newHeader.add(defaultComponent, BorderLayout.SOUTH);
        return newHeader;
    }

    /**
     * Subroutine of {@link #getTableCellRendererComponent()}. Returns a dummy
     * component that can be used to "rubber stamp" the picture of the column
     * role chooser for the given column.
     * <p>
     * We can't return {@link #columnRoleBox} here because it's used for
     * generating popups. The table header actually repaints while the popup is
     * visible, and a JComboBox can't be used as a cell renderer while it's
     * showing its popup.
     * 
     * @param chartColumn
     *            The chart column whose role should show as the selected item.
     */
    private JComponent makeRoleBox(ChartColumn chartColumn) {
        if (numericAndDateCols.contains(chartColumn.getName())) { 
            JComboBox box = new JComboBox();
            box.addItem(chartColumn.getRoleInChart());
            box.setSelectedItem(chartColumn.getRoleInChart());
            return box;
        } else {
            return makePlaceholder();
        }
    }

    /**
     * Subroutine of {@link #getTableCellRendererComponent()}. Returns a dummy
     * component that can be used to "rubber stamp" the picture of the x-axis
     * column for the given chart column.
     * <p>
     * We can't return {@link #xAxisBox} here because it's used for generating
     * popups. The table header actually repaints while the popup is visible,
     * and a JComboBox can't be used as a cell renderer while it's showing its
     * popup.
     * 
     * @param chartColumn
     *            The chart column whose x-axis column should show as the
     *            selected item.
     */
    private JComponent makeXAxisBox(ChartColumn chartColumn) {
        if (chartColumn.getRoleInChart() == ColumnRole.SERIES) {
            JComboBox box = new JComboBox();
            if (chartColumn.getXAxisIdentifier() != null) {
                String xAxisColName = chartColumn.getXAxisIdentifier().getName();
                box.addItem(xAxisColName);
                box.setSelectedItem(xAxisColName);
            } else {
                // leave as a blank combo box
            }
            return box;
        } else {
            return makePlaceholder();
        }
    }

    /**
     * Makes a blank component with the same preferred height as a combo box.
     */
    private JComponent makePlaceholder() {
        JLabel placeholder = new JLabel();
        placeholder.setPreferredSize(
                new Dimension(10, xAxisBox.getPreferredSize().height));
        return placeholder;
    }

    private List<String> findNumericAndDateCols() throws SQLException {
        // XXX it would be better to store data type in column identifiers
        ResultSet rs = chartPanel.getChart().getUnfilteredResultSet();
        ResultSetMetaData rsmd = rs.getMetaData();
        List<String> cols = new ArrayList<String>();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            int columnType = rsmd.getColumnType(i);
            if (SQL.isNumeric(columnType) || SQL.isDate(columnType)) {
                cols.add(rsmd.getColumnName(i));
                logger.debug("Column " + i + " (" + rsmd.getColumnName(i) + ") is numeric or date. type=" + columnType);
            } else {
                logger.debug("Column " + i + " (" + rsmd.getColumnName(i) + ") is not numeric or date. type=" + columnType);
            }
        }
        return cols;
    }
    
    public void cleanup() {
        tableHeader.removeMouseListener(comboBoxMouseListener);
    }

    public List<ChartColumn> getChartColumns() {
        return columnNamesInOrder;
    }

}