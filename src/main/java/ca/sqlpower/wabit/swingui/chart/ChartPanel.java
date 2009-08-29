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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;

import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.swingui.table.EditableJTable;
import ca.sqlpower.swingui.table.ResultSetTableModel;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.report.chart.ExistingChartTypes;
import ca.sqlpower.wabit.report.chart.LegendPosition;
import ca.sqlpower.wabit.swingui.WabitPanel;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContextImpl;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.FormLayout;

public class ChartPanel implements WabitPanel {
    
    static final Logger logger = Logger.getLogger(ChartPanel.class);

    /**
     * Key for client property stored in the chart chooser toggle buttons.
     */
    private static final String CHART_TYPE_PROP_KEY = ChartPanel.class.getName() + ".CHART_TYPE_PROP_KEY";

    private final Icon BAR_CHART_ICON = new ImageIcon(ChartPanel.class.getResource("/icons/32x32/chart-bar.png"));
    private final Icon LINE_CHART_ICON = new ImageIcon(ChartPanel.class.getResource("/icons/32x32/chart-line.png"));
    private final Icon PIE_CHART_ICON = new ImageIcon(ChartPanel.class.getResource("/icons/32x32/chart-pie.png"));
    private final Icon SCATTER_CHART_ICON = new ImageIcon(ChartPanel.class.getResource("/icons/32x32/chart-scatter.png"));
    
    /**
     * The panel that holds the chart editor UI.
     */
    private final JPanel panel = new JPanel();

    /**
     * Contains all the queries in the workspace. This will let the user
     * choose which query to chart.
     */
    private final JComboBox queryComboBox;

    /**
     * Holds the chart's Y axis label.
     */
    private final JTextField yaxisNameField = new JTextField();

    /**
     * Holds the chart's X axis label.
     */
    private final JTextField xaxisNameField = new JTextField();

    /**
     * A label for the x axis field. This is a member variable so we can make
     * the label invisible when the field is not needed.
     */
    private final JLabel xaxisNameLabel = new JLabel("X Axis");

    /**
     * The table that shows values returned from the queries. The headers
     * added to this table will allow users to define which column is the
     * category and which ones are series.
     */
    private final JTable resultTable = new EditableJTable();

    /**
     * Holds the chart's legend position.
     */
    private final JComboBox legendPositionComboBox;

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
     * This is the default renderer for the headers of the table displaying the
     * values from the query. It's wrapped by {@link #currentHeaderCellRenderer},
     * which uses it to render the standard headers below our custom
     * "column role" components.
     */
    private final TableCellRenderer defaultHeaderCellRenderer;

    /**
     * Wrapper around the regular {@link #defaultHeaderCellRenderer}. Places the
     * appropriate combo boxes above the standard table headers to allow users
     * to specify the roles of columns (series, categories, or x-axis values) in
     * a chart.
     */
    private ChartTableHeaderCellRenderer currentHeaderCellRenderer;

    /**
     * The data model for this component.
     */
    final Chart chart;

    /**
     * Flag maintained by the chart property change listener to tell if this
     * panel has any unsaved changes.
     */
    private boolean chartHasChanges = false;

    /**
     * Listens for changes to all text fields on this panel. Updates the chart
     * object and its preview on each document event.
     */
    private DocumentListener documentChangeHandler = new DocumentListener() {
        public void changedUpdate(DocumentEvent e) {
            updateChartFromGUI();
        }
        public void insertUpdate(DocumentEvent e) {
            updateChartFromGUI();
        }
        public void removeUpdate(DocumentEvent e) {
            updateChartFromGUI();
        }
    };

    /**
     * Listens to changes in the chart (this component's data model) and updates
     * the GUI when appropriate.
     */
    private final PropertyChangeListener chartListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            chartHasChanges = true; // FIXME some property changes need to be ignored
            try {
                updateGUIFromChart();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
//            if ("type".equals(evt.getPropertyName())) {
//                switch (chart.getType()) {
//                case BAR:
//                case CATEGORY_LINE:
//                    xaxisNameField.setVisible(false);
//                    xaxisNameLabel.setVisible(false);
//                    currentHeaderCellRenderer = 
//                        new CategoryChartRendererTableCellRenderer(
//                                resultTable.getTableHeader(), defaultHeaderCellRenderer);
//                    resultTable.getTableHeader().setDefaultRenderer(currentHeaderCellRenderer);
//                    break;
//                case LINE:
//                case SCATTER:
//                    currentHeaderCellRenderer =
//                        new XYChartRendererCellRenderer(
//                                resultTable.getTableHeader(), defaultHeaderCellRenderer);
//                    resultTable.getTableHeader().setDefaultRenderer(currentHeaderCellRenderer);
//                    xaxisNameField.setVisible(true);
//                    xaxisNameLabel.setVisible(true);
//                    break;
//                default:
//                    throw new IllegalStateException(
//                            "Unknown chart type " + chartTypeComboBox.getSelectedItem());
//                }
//                if(queryComboBox.getSelectedItem() != null) {
//                    final WabitObject selectedItem = (WabitObject) queryComboBox.getSelectedItem();
//                    try {
//                        updateEditor(selectedItem);
//                    } catch (SQLException e1) {
//                        throw new RuntimeException(e1);
//                    }
//                }
//            }
//        }
        
    };

    /**
     * Protects {@link #updateChartFromGUI()} and {@link #updateGUIFromChart()}
     * against mutual recursion.
     */
    private volatile boolean updating = false;
    
    private final Action refreshDataAction;

    /**
     * Listens to all the combo boxes, and updates the chart object whenever an
     * event comes in.
     */
    private final ItemListener genericItemListener = new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
            updateChartFromGUI();
        }
    };

    /**
     * Listens to anything that can produce an action event (such as a button),
     * and updates the chart object whenever an event comes in.
     */
    private final ActionListener genericActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            updateChartFromGUI();
        }
    };
    
    public ChartPanel(WabitSwingSession session, final Chart chart) {
        WabitWorkspace workspace = session.getWorkspace();
        this.chart = chart;
        
        refreshDataAction = new RefreshDataAction(chart, panel);
        
        resultTable.getTableHeader().setReorderingAllowed(false);
        defaultHeaderCellRenderer = resultTable.getTableHeader().getDefaultRenderer();
        
        List<WabitObject> queries = new ArrayList<WabitObject>();
        queries.addAll(workspace.getQueries());
        queries.addAll(workspace.getOlapQueries());
        queryComboBox = new JComboBox(queries.toArray());
        
        legendPositionComboBox = new JComboBox(LegendPosition.values());
        yaxisNameField.getDocument().addDocumentListener(documentChangeHandler);
        xaxisNameField.getDocument().addDocumentListener(documentChangeHandler);

        queryComboBox.addItemListener(genericItemListener);
        legendPositionComboBox.addItemListener(genericItemListener);
        
        chartError.setVisible(false);
        
        buildUI();

        chart.addPropertyChangeListener(chartListener);

        try {
            chart.refreshData(); // TODO check if this is necessary (it probably is)
            updateGUIFromChart();
        } catch (Exception ex) {
            chartError.setText(
                    "Execution of query \"" + chart.getQuery() + "\" failed: " + ex.getMessage());
            chartError.setVisible(true);
        }

    }

    /**
     * When any aspect of the chart changes, this method should be called to set
     * the state of the editor. This will set up the correct result set as well
     * as display the correct editor in a new edit state.
     */
    private void updateGUIFromChart() throws SQLException {
        if (updating) return;
        try {
            updating = true;
            yaxisNameField.setText(chart.getYaxisName());
            xaxisNameField.setText(chart.getXaxisName());
            queryComboBox.setSelectedItem(chart.getQuery());
            setSelectedChartType(chart.getType());
            
            if (currentHeaderCellRenderer != null) {
                currentHeaderCellRenderer.cleanup();
            }

            if (chart.getType() == ExistingChartTypes.BAR || chart.getType() == ExistingChartTypes.CATEGORY_LINE) {
                currentHeaderCellRenderer = new CategoryChartHeaderRenderer(this, resultTable.getTableHeader(), defaultHeaderCellRenderer);
                resultTable.getTableHeader().setDefaultRenderer(currentHeaderCellRenderer);
            } else if (chart.getType() == ExistingChartTypes.LINE || chart.getType() == ExistingChartTypes.SCATTER) {
                currentHeaderCellRenderer = new XYChartHeaderRenderer(this, resultTable.getTableHeader(), defaultHeaderCellRenderer);
                resultTable.getTableHeader().setDefaultRenderer(currentHeaderCellRenderer);
            }

            resultTable.getTableHeader().repaint(); // XXX probably unnecessary now
            
            if(chart.getLegendPosition() != null) {
                legendPositionComboBox.setSelectedItem(chart.getLegendPosition());
            } else {
                legendPositionComboBox.setSelectedItem(LegendPosition.BOTTOM);
            }

            CachedRowSet rs = chart.getUnfilteredResultSet();
            if (rs == null) {
                chartError.setText("The selected query \"" + chart.getQuery() + "\" returns no results.");
                chartError.setVisible(true);
                return;
            }

            final ResultSetTableModel model = new ResultSetTableModel(rs);
            resultTable.setModel(model);
            resultTable.setDefaultRenderer(Object.class, new ChartTableCellRenderer()); // TODO provide filter to renderer

            chartError.setText("");
            chartError.setVisible(false);

            updateChartPreview();

        } finally {
            updating = false;
        }
    }

    /**
     * Updates the the JFreeChart preview panel and its associated error
     * message based on the current state of the GUI components in this property
     * panel. The chart generated here is only intended as a preview for the
     * property panel.
     */
    private void updateChartPreview() {
        try {
            JFreeChart newJFreeChart = ChartSwingUtil.createChartFromQuery(chart);
            logger.debug("Created new JFree chart: " + newJFreeChart);
            chartPanel.setChart(newJFreeChart);
            chartError.setText("");
            chartError.setVisible(false);
        } catch (Exception ex) {
            chartError.setText("<html>Failed to create chart:<br>" + ex);
            chartError.setVisible(true);
            logger.warn("Failed to create a chart.", ex);
            chartPanel.setChart(null);
        }
    }

    private void buildUI() {
        DefaultFormBuilder builder = new DefaultFormBuilder(
                new FormLayout(
                        "pref, 3dlu, pref:grow, 3dlu, pref:grow",
                        "pref, 3dlu, fill:0:grow, 3dlu, fill:0:grow"),
                new FormDebugPanel());
        
        builder.append("Query", queryComboBox, 3);
        builder.nextRow();

        builder.append(new JLabel("Chart header legend goes here"));// TODO
        
        JScrollPane tableScrollPane = new JScrollPane(resultTable);
        builder.append(tableScrollPane, 3);
        builder.nextRow();
        
        JPanel chartAndErrorPanel = new JPanel(new BorderLayout());
        chartAndErrorPanel.add(chartError, BorderLayout.NORTH);
        chartAndErrorPanel.add(chartPanel, BorderLayout.CENTER);
        builder.append(chartAndErrorPanel, 3);
        
        builder.append(buildChartPrefsPanel());
        
        // XXX The following code proliferation pains me. I've opened a bug.
        JToolBar toolBar = new JToolBar();
        JButton refreshButton = new JButton(refreshDataAction);
        refreshButton.setText("Refresh");
        refreshButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        refreshButton.setHorizontalTextPosition(SwingConstants.CENTER);
        // Removes button borders on OS X 10.5
        refreshButton.putClientProperty("JButton.buttonType", "toolbar");
        toolBar.add(refreshButton);
        toolBar.setFloatable(false);

        toolBar.addSeparator();
        toolBar.add(makeChartTypeButton("Bar", ExistingChartTypes.BAR, BAR_CHART_ICON));
        toolBar.add(makeChartTypeButton("Category Line", ExistingChartTypes.CATEGORY_LINE, LINE_CHART_ICON));

        toolBar.addSeparator();
        toolBar.add(makeChartTypeButton("Line", ExistingChartTypes.LINE, LINE_CHART_ICON));
        toolBar.add(makeChartTypeButton("Scatter", ExistingChartTypes.SCATTER, SCATTER_CHART_ICON));
        
//        toolBar.addSeparator();
        // TODO pie
        
        JToolBar wabitBar = new JToolBar();
        wabitBar.setFloatable(false);
        JButton forumButton = new JButton(WabitSwingSessionContextImpl.FORUM_ACTION);
        forumButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        wabitBar.add(forumButton);
        
        JToolBar mainbar = new JToolBar();
        mainbar.setLayout(new BorderLayout());
        mainbar.add(toolBar, BorderLayout.CENTER);
        mainbar.add(wabitBar, BorderLayout.EAST);
        mainbar.setFloatable(false);

        panel.setLayout(new BorderLayout());
        panel.add(builder.getPanel(), BorderLayout.CENTER);
        panel.add(mainbar, BorderLayout.NORTH);
    }

    /**
     * Contains all the toggle buttons that are for choosing the chart type.
     * Each button in this group has a client property indicating the {@link ExistingChartTypes} constant it represents.
     * 
     */
    ButtonGroup chartTypeButtonGroup = new ButtonGroup();

    /**
     * Subroutine of {@link #buildUI()}. Makes a chart type toggle button and
     * adds it to the button group.
     * 
     * @param caption The text to appear under the button
     * @param type The type of chart the buttons should select
     * @param icon The icon for the button
     * @return A button properly configured for the new-look Wabit toolbar.
     */
    private JToggleButton makeChartTypeButton(
            String caption, ExistingChartTypes type, Icon icon) {
        JToggleButton b = new JToggleButton(caption, icon);
        b.putClientProperty(CHART_TYPE_PROP_KEY, type);
        chartTypeButtonGroup.add(b);
        
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        
        // Removes button borders on OS X 10.5
        b.putClientProperty("JButton.buttonType", "toolbar");

        b.addActionListener(genericActionListener);
        
        return b;
    }

    private ExistingChartTypes getSelectedChartType() {
        for (Enumeration<AbstractButton> buttons = chartTypeButtonGroup.getElements();
                buttons.hasMoreElements(); ) {
            AbstractButton b = buttons.nextElement();
            if (chartTypeButtonGroup.isSelected(b.getModel())) {
                ExistingChartTypes ct = (ExistingChartTypes) b.getClientProperty(CHART_TYPE_PROP_KEY);
                logger.debug("Found selected chart type " + ct);
                return ct;
            }
        }
        
        logger.debug("Didn't find any selected chart buttons. Returning null.");
        return null;
    }
    
    /**
     * Selects the appropriate button in {@link #chartTypeButtonGroup}.
     * 
     * @param type The type of chart to select the button of.
     */
    private void setSelectedChartType(ExistingChartTypes type) {
        for (Enumeration<AbstractButton> buttons = chartTypeButtonGroup.getElements();
        buttons.hasMoreElements(); ) {
            AbstractButton b = buttons.nextElement();
            ExistingChartTypes ct = (ExistingChartTypes) b.getClientProperty(CHART_TYPE_PROP_KEY);
            if (ct == type) {
                chartTypeButtonGroup.setSelected(b.getModel(), true);
                return;
            }
        }
        throw new IllegalArgumentException("I can't find a button for chart type " + type);
    }

    /**
     * Subroutine of {@link #buildUI()}. Creates the form that appears to the
     * right of the JFreeChart preview.
     */
    private Component buildChartPrefsPanel() {
        DefaultFormBuilder builder = new DefaultFormBuilder(
                new FormLayout("pref, 3dlu, pref:grow"),
                new FormDebugPanel());

        builder.append("Legend Postion", legendPositionComboBox);
        builder.nextLine();
        
        builder.append("Y Axis Label", yaxisNameField);
        builder.nextLine();
        
        builder.append(xaxisNameLabel, xaxisNameField);
        builder.nextLine();

        return builder.getPanel();
    }

    public boolean hasUnsavedChanges() {
        return chartHasChanges;
    }

    public JComponent getPanel() {
        return panel;
    }

    public void discardChanges() {
        logger.debug("Discarding changes");
        cleanup();
    }

    public boolean applyChanges() {
        logger.debug("Applying changes");
        cleanup();
        updateChartFromGUI();
        return true;
    }

    private void cleanup() {
        chart.removePropertyChangeListener(chartListener);
    }
    
    /**
     * Updates {@link #chart}'s settings to match those currently in this
     * panel's GUI components. This is the inverse of {@link #updateGUIFromChart()}.
     */
    void updateChartFromGUI() {
        if (updating) return;
        try {
            updating = true;
            try {
                chart.defineQuery((WabitObject) queryComboBox.getSelectedItem());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            chart.setType(getSelectedChartType());
            chart.setLegendPosition((LegendPosition) legendPositionComboBox.getSelectedItem());
            chart.setYaxisName(yaxisNameField.getText());
            chart.setXaxisName(xaxisNameField.getText());
            
            
        } finally {
            updating = false;
        }
        
        /*
         * Because we were purposely ignoring events from the chart while updating it,
         * we now have to do an explicit refresh of the table and chart preview. The
         * same interlock that prevented mutual recursion during the GUI->Chart update
         * prevents Chart->GUI updates from re-calling this method.
         */
        try {
            updateGUIFromChart();
        } catch (SQLException ex) {
            logger.info("Chart->GUI update failed", ex);
            chartError.setText("Update failed: " + ex);
            chartError.setVisible(true);
        }
    }

    public String getTitle() {
        return "Chart Editor";
    }

    public void maximizeEditor() {
        // no op
    }
    
    public Chart getChart() {
        return chart;
    }
    
    /**
     * Returns the leading X coordinate (within the table of data) of the given
     * column. This is a helper method for the table header cell renderers.
     */
    int getXPositionOfColumn(final TableColumnModel model, final int columnIndex) {
        int sum = 0;
        for (int i = 0; i < columnIndex; i++) {
            sum += model.getColumn(i).getWidth();
        }
        logger.debug("X position of column " + columnIndex + " is " + sum + " according to " + model);
        return sum;
    }


}
