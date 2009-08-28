package ca.sqlpower.wabit.swingui.chart;

import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import ca.sqlpower.wabit.report.chart.ChartColumn;

/**
 * This cell renderer is used to add combo boxes to the headers of tables
 * returned in the properties panel. The combo boxes allow users to define
 * the role of columns (categories or series).
 * <p>
 * This is for category type charts
 */
class CategoryChartHeaderRenderer implements ChartTableHeaderCellRenderer {

    public List<ChartColumn> getChartColumns() {
        // TODO Auto-generated method stub
        return null;
    }

    public void cleanup() {
        // TODO Auto-generated method stub
        
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        return new JLabel("Not implemented");
    }

//    /**
//     * The chart panel this header is working for.
//     */
//    private final ChartPanel chartPanel;
//
//    /**
//     * This listens to all of the combo boxes that define how the column relates
//     * to a category. This listener will update the current category combo box to NONE
//     * if a new category is selected since there is only one category allowed at a time.
//     */
//    private final ItemListener dataTypeSeriesChangeListener = new ItemListener() {
//
//        public void itemStateChanged(ItemEvent e) {
//            if (e.getStateChange() == ItemEvent.SELECTED) {
//                JComboBox sourceCombo = (JComboBox) e.getSource();
//                ColumnIdentifier identifier = CategoryChartHeaderRenderer.this.chartPanel.columnNamesInOrder.get(tableHeader.
//                        getColumnModel().getColumnIndexAtX(sourceCombo.getX()));
//                identifier.setRoleInChart((ColumnRole) e.getItem());
//                tableHeader.repaint();
//                CategoryChartHeaderRenderer.this.chartPanel.updateChartPreview();
//            }
//        }
//    };
//
//    /**
//     * The header is used to attach a mouse listener to let the combo box
//     * pop up.
//     */
//    private final JTableHeader tableHeader;
//
//    /**
//     * This map will track which combo boxes are in which position. This
//     * lets us know which combo box to use to display to a user when a
//     * header is clicked.
//     */
//    private final Map<Integer, JComboBox> columnToComboBox = new HashMap<Integer, JComboBox>();
//
//    /**
//     * This header is used as the default way to render a table's cell. This
//     * way cells will have a similar looking header to the default. 
//     */
//    private final TableCellRenderer defaultTableCellRenderer;
//
//    /**
//     * This listens for mouse clicks on the table header to show the combo box's
//     * pop-up menu. This is needed as the normal mouse listeners on the combo box
//     * are removed on the table header.
//     */
//    private final MouseListener comboBoxMouseListener = new MouseAdapter() {
//
//        private int mouseX;
//        private int mouseY;
//
//        @Override
//        public void mousePressed(MouseEvent e) {
//            mouseX = e.getX();
//            mouseY = e.getY();
//        }
//
//        @Override
//        public void mouseReleased(MouseEvent e) {
//            if (e.getX() - mouseX > 3 || e.getX() - mouseX < -3 || e.getY() - mouseY > 3 || e.getY() - mouseY < -3) {
//                return;
//            }
//            final int column = tableHeader.getColumnModel().getColumnIndexAtX(e.getX());
//            final JComboBox dataTypeComboBox = columnToComboBox.get(column);
//            tableHeader.add(dataTypeComboBox);
//            dataTypeComboBox.setBounds(CategoryChartHeaderRenderer.this.chartPanel.getXPositionOfColumn(tableHeader.getColumnModel(), column), 0, tableHeader.getColumnModel().getColumn(column).getWidth(), dataTypeComboBox.getHeight());
//            dataTypeComboBox.setPopupVisible(true);
//            dataTypeComboBox.addPopupMenuListener(new PopupMenuListener() {
//
//                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
//                    //don't care
//                }
//
//                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
//                    tableHeader.remove(dataTypeComboBox);
//                    dataTypeComboBox.removePopupMenuListener(this);
//                }
//
//                public void popupMenuCanceled(PopupMenuEvent e) {
//                    //don't care
//                }
//            });
//            ChartPanel.logger.debug("table header has components " + Arrays.toString(tableHeader.getComponents()));
//        }
//    };
//
    public CategoryChartHeaderRenderer(ChartPanel chartPanel, final JTableHeader tableHeader, TableCellRenderer defaultTableCellRenderer) {
//        this.chartPanel = chartPanel;
//        this.tableHeader = tableHeader;
//        this.defaultTableCellRenderer = defaultTableCellRenderer;
//
//        tableHeader.addMouseListener(comboBoxMouseListener);
    }
//
//    public Component getTableCellRendererComponent(JTable table,
//            Object value, boolean isSelected, boolean hasFocus, int row,
//            final int column) {
//        Component defaultComponent = defaultTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//        final JPanel newHeader = new JPanel(new BorderLayout());
//        final JComboBox dataTypeComboBox = new JComboBox(ColumnRole.values());
//        try {
//            String columnName = (String) this.chartPanel.columnNamesInOrder.get(column).getUniqueIdentifier();
//            int rsColumnIndex = 0;
//            ResultSet rs = this.chartPanel.chart.getUnfilteredResultSet();
//            for (int i = 1; i <= rs .getMetaData().getColumnCount(); i++) {
//                if (rs.getMetaData().getColumnName(i).equals(columnName)) {
//                    rsColumnIndex = i;
//                    break;
//                }
//            }
//            if (!SQL.isNumeric(rs.getMetaData().getColumnType(rsColumnIndex))) {
//                dataTypeComboBox.removeItem(ColumnRole.SERIES);
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//        columnToComboBox.put(new Integer(column), dataTypeComboBox);
//        final ColumnRole defaultDataTypeSeries = this.chartPanel.columnNamesInOrder.get(column).getRoleInChart();
//        if (defaultDataTypeSeries == null) {
//            dataTypeComboBox.setSelectedItem(ColumnRole.NONE);
//        } else {
//            dataTypeComboBox.setSelectedItem(defaultDataTypeSeries);
//        }
//        dataTypeComboBox.addItemListener(dataTypeSeriesChangeListener);
//        newHeader.add(dataTypeComboBox, BorderLayout.NORTH);
//        newHeader.add(defaultComponent, BorderLayout.SOUTH);
//
//        return newHeader;
//    }
//
//    public void cleanup() {
//        tableHeader.removeMouseListener(comboBoxMouseListener);
//    }

}