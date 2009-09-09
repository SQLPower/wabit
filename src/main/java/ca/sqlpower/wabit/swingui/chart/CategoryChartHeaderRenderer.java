package ca.sqlpower.wabit.swingui.chart;

import java.awt.BorderLayout;
import java.awt.Color;
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
 * This cell renderer is used to add combo boxes to the headers of tables
 * returned in the properties panel. The combo boxes allow users to define
 * the role of columns (categories or series).
 * <p>
 * This is for category type charts
 */
class CategoryChartHeaderRenderer implements ChartTableHeaderCellRenderer {

    private static final Logger logger = Logger.getLogger(CategoryChartHeaderRenderer.class);
    
    /**
     * The chart panel this header is working for.
     */
    private final ChartPanel chartPanel;

    /**
     * Listens to all of the combo boxes that define how the column relates
     * to a category. This listener will update the current category combo box to NONE
     * if a new category is selected since there is only one category allowed at a time.
     */
    private final ItemListener roleChangeListener = new ItemListener() {

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                JComboBox sourceCombo = (JComboBox) e.getSource();
                ChartColumn identifier = chartColumns.get(
                        tableHeader.getColumnModel().getColumnIndexAtX(sourceCombo.getX()));
                identifier.setRoleInChart((ColumnRole) e.getItem());
                tableHeader.repaint();
                chartPanel.updateChartFromGUI();
            }
        }
    };

    /**
     * The header is used to attach a mouse listener to let the combo box
     * pop up.
     */
    private final JTableHeader tableHeader;

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
            if (column < 0) {
                logger.debug("Ignoring out-of-bounds click (x=" + e.getX() + " is not over a column)");
                return;
            }
            final JComboBox rolePopupBox = makeRoleBox(column);
            tableHeader.add(rolePopupBox);
            rolePopupBox.setBounds(
                    chartPanel.getXPositionOfColumn(tableHeader.getColumnModel(), column),
                    0,
                    tableHeader.getColumnModel().getColumn(column).getWidth(),
                    rolePopupBox.getPreferredSize().height);
            rolePopupBox.setPopupVisible(true);
            rolePopupBox.addPopupMenuListener(new PopupMenuListener() {

                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    //don't care
                }

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    tableHeader.remove(rolePopupBox);
                    rolePopupBox.removePopupMenuListener(this);
                }

                public void popupMenuCanceled(PopupMenuEvent e) {
                    //don't care
                }
            });
            logger.debug(e.getID() + " table header has components " + Arrays.toString(tableHeader.getComponents()));
        }
    };

    private final List<ChartColumn> chartColumns;
    
    private final Color backgroundColour;
    
    public CategoryChartHeaderRenderer(ChartPanel chartPanel, final JTableHeader tableHeader, TableCellRenderer defaultTableCellRenderer) {
        this.chartPanel = chartPanel;
        this.tableHeader = tableHeader;
        this.defaultTableCellRenderer = defaultTableCellRenderer;

        if (logger.isDebugEnabled()) {
            backgroundColour = new Color(
                    (float) Math.random(),
                    (float) Math.random(),
                    (float) Math.random());
        } else {
            backgroundColour = null;
        }
        
        tableHeader.addMouseListener(comboBoxMouseListener);
        
        chartColumns = new ArrayList<ChartColumn>(chartPanel.getChart().getColumns());
    }

    public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            final int column) {
        Component defaultComponent = defaultTableCellRenderer.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        
        JPanel newHeader = new JPanel(new BorderLayout());
        JComboBox roleBox = makeRoleBox(column);
        if (roleBox != null){
        	newHeader.add(makeRoleBox(column), BorderLayout.NORTH);
        	newHeader.add(defaultComponent, BorderLayout.SOUTH);
        }

        return newHeader;
    }

    /**
     * Subroutine of {@link #getTableCellRendererComponent()}. Returns a new
     * component that can either be used to "rubber stamp" the picture of the
     * column role chooser for the given column, or to produce a popup menu.
     * <p>
     * We always return a new combo box here because the table header actually
     * repaints while the popup is visible, so a shared instance would get used
     * for generating popups and rendering in the header at the same time. A
     * JComboBox can't do this.
     * 
     * @param chartColumn
     *            The chart column whose role should show as the selected item.
     */
    private JComboBox makeRoleBox(final int column) {
        final JComboBox roleBox = new JComboBox(ColumnRole.values());
        if (backgroundColour != null) {
            roleBox.setBackground(backgroundColour);
        }
        try {
            String columnName = chartColumns.get(column).getName();
            ResultSet rs = chartPanel.getChart().getUnfilteredResultSet();
            int rsColumnIndex = rs.findColumn(columnName);
            if (!SQL.isNumeric(rs.getMetaData().getColumnType(rsColumnIndex))) {
                roleBox.removeItem(ColumnRole.SERIES);
            }
        } catch (IndexOutOfBoundsException e){ //Thrown if query is changed to a blank query
        	return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        final ColumnRole currentRole = chartColumns.get(column).getRoleInChart();
        if (currentRole == null) {
            roleBox.setSelectedItem(ColumnRole.NONE);
        } else {
            roleBox.setSelectedItem(currentRole);
        }
        roleBox.addItemListener(roleChangeListener);
        return roleBox;
    }

    public void cleanup() {
        tableHeader.removeMouseListener(comboBoxMouseListener);
    }

    public List<ChartColumn> getChartColumns() {
        return chartColumns;
    }

    public JComponent getHeaderLegendComponent() {
        JPanel p = new JPanel(new BorderLayout());
        int comboBoxHeight = new JComboBox().getPreferredSize().height;
        
        JLabel label1 = new JLabel("Role in Chart");
        label1.setPreferredSize(new Dimension(label1.getPreferredSize().width, comboBoxHeight));

        p.add(label1, BorderLayout.NORTH);
        
        return p;
    }
}