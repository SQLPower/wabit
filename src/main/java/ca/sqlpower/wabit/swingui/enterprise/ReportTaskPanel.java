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

package ca.sqlpower.wabit.swingui.enterprise;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.wabit.AbstractWabitListener;
import ca.sqlpower.wabit.ObjectDependentException;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitListener;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.enterprise.client.ReportTask;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.Guide.Axis;
import ca.sqlpower.wabit.swingui.WabitIcons;
import ca.sqlpower.wabit.swingui.WabitPanel;
import ca.sqlpower.wabit.swingui.WabitToolBarBuilder;

public class ReportTaskPanel implements WabitPanel {

	private boolean dirty = false;
	private final ReportTask task;
	
	private final static Logger log = Logger.getLogger(ReportTaskPanel.class);
	
	// GUI components
	private final JPanel panel = new JPanel(new MigLayout());
	private final JComboBox reportComboBox;
	private final JComboBox scheduleTypeComboBox;
	private final JTextField emailTextField;
	private final JComboBox intervalComboBox;
	private final JComboBox minutesComboBox;
	private final JComboBox hoursComboBox;
	private final JComboBox dayOfWeekComboBox;
	private final JComboBox dayOfMonthComboBox;
	private final JButton okButton;
	private final JButton cancelButton;
	
	
	// Labels
	private final JLabel reportLabel = new JLabel("Choose a report to schedule");
	private final JLabel emailLabel = new JLabel("Recipient email");
	private final JLabel scheduleTypeLabel = new JLabel("Run this report every");
	private final JLabel timeLabel = new JLabel("Run at");
	private final JLabel timeSepLabel = new JLabel(":");
	private final JLabel dowLabel = new JLabel("Every");
	private final JLabel domLabel1 = new JLabel("On the");
	private final JLabel domLabel2 = new JLabel("day of the month");
	private final JLabel repeatLabel = new JLabel("Repeat every");
	private final JLabel repeatMinuteLabel = new JLabel("minutes");
	private final JLabel repeatHourLabel = new JLabel("hours");
	
	private final WabitToolBarBuilder toolbarBuilder = new WabitToolBarBuilder();
	private final WabitWorkspace workspace;
	
	private final WabitListener taskListener = new AbstractWabitListener() {
		@Override
		protected void propertyChangeImpl(PropertyChangeEvent evt) {
			reinitGuiModel();
		}
	};
	
	public ReportTaskPanel(ReportTask baseTask) {
		this.task = baseTask;
		this.workspace = (WabitWorkspace)this.task.getParent();
		
		baseTask.addWabitListener(taskListener);
		
		this.reportComboBox = new JComboBox();
		this.reportComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dirty = true;
			}
		});
		
		this.emailTextField = new JTextField();
		this.emailTextField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				dirty = true;
			}
			public void keyReleased(KeyEvent e) {
				// no-op
			}
			public void keyPressed(KeyEvent e) {
				// no-op
			}
		});
		
		
		this.scheduleTypeComboBox = new JComboBox();
		this.scheduleTypeComboBox.addItem("minute");
		this.scheduleTypeComboBox.addItem("hour");
		this.scheduleTypeComboBox.addItem("day");
		this.scheduleTypeComboBox.addItem("week");
		this.scheduleTypeComboBox.addItem("month");
		

		this.minutesComboBox = new JComboBox();
		for (int i = 0; i < 60; i++) {
			String value;
			if (i < 10) {
				value = "0".concat(String.valueOf(i));
			} else {
				value = String.valueOf(i);
			}
			this.minutesComboBox.addItem(value);
		}
		this.minutesComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dirty = true;
			}
		});
		this.hoursComboBox = new JComboBox();
		for (int i = 0; i < 24; i++) {
			this.hoursComboBox.addItem(String.valueOf(i));
		}
		this.hoursComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dirty = true;
			}
		});
		
		
		
		
		this.dayOfWeekComboBox = new JComboBox();
		this.dayOfWeekComboBox.addItem("Sunday");
		this.dayOfWeekComboBox.addItem("Monday");
		this.dayOfWeekComboBox.addItem("Tuesday");
		this.dayOfWeekComboBox.addItem("Wednesday");
		this.dayOfWeekComboBox.addItem("Thursday");
		this.dayOfWeekComboBox.addItem("Friday");
		this.dayOfWeekComboBox.addItem("Saturday");
		this.dayOfWeekComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dirty = true;
			}
		});
		
		
		
		
		this.dayOfMonthComboBox = new JComboBox();
		for (int i = 1; i < 32; i++) {
			this.dayOfMonthComboBox.addItem(String.valueOf(i));
		}
		this.dayOfMonthComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dirty = true;
			}
		});
		
		
		
		
		this.intervalComboBox = new JComboBox();
		for (int i = 1; i < 101; i++) {
			this.intervalComboBox.addItem(String.valueOf(i));
		}
		this.intervalComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dirty = true;
			}
		});
		
		
		
		
		this.okButton = new JButton();
		this.okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyChanges();
			}
		});
		this.cancelButton = new JButton();
		this.cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				discardChanges();
				workspace.setEditorPanelModel(workspace);
			}
		});
		this.toolbarBuilder.add(this.okButton, "Update Scheduler", WabitIcons.REFRESH_ICON_32);
		this.toolbarBuilder.add(this.cancelButton, "Cancel", WabitIcons.CANCEL_ICON_32);
		
		this.scheduleTypeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dirty = true;
				updateParameters((String)scheduleTypeComboBox.getSelectedItem());
			}
		});
		
		updateReportsList();
		updateParameters((String)scheduleTypeComboBox.getSelectedItem());
		reinitGuiModel();	
		this.dirty = task.isNoob();
		
		
		this.panel.add(reportLabel, "ax right, hidemode 3");
		this.panel.add(reportComboBox, "span, wrap, growx, hidemode 3, wmin 200, wmax 200");
		
		this.panel.add(this.emailLabel, "ax right, hidemode 3");
		this.panel.add(this.emailTextField, "span, wrap, growx, hidemode 3, wmin 200, wmax 200");
		
		this.panel.add(this.scheduleTypeLabel, "ax right, hidemode 3");
		this.panel.add(this.scheduleTypeComboBox, "span, wrap, growx, hidemode 3, wmin 200, wmax 200");
		
		this.panel.add(this.timeLabel, "ax right, hidemode 3");
		this.panel.add(this.hoursComboBox, "hidemode 3");
		this.panel.add(this.timeSepLabel, "hidemode 3");
		this.panel.add(this.minutesComboBox, "wrap, hidemode 3");
		
		this.panel.add(this.dowLabel, "ax right, hidemode 3");
		this.panel.add(this.dayOfWeekComboBox, "span, wrap, growx, hidemode 3");
		
		this.panel.add(this.domLabel1, "ax right, hidemode 3");
		this.panel.add(this.dayOfMonthComboBox, "growx, hidemode 3");
		this.panel.add(this.domLabel2, "span, wrap, hidemode 3");
		
		this.panel.add(this.repeatLabel, "ax right, hidemode 3");
		this.panel.add(this.intervalComboBox, "hidemode 3");
		this.panel.add(this.repeatHourLabel, "span, wrap, hidemode 3");
		this.panel.add(this.repeatMinuteLabel, "span, wrap, hidemode 3");
	}
	
	
	private void updateParameters(String selectedItem) 
	{
		// Hide everything
		this.timeLabel.setVisible(false);
		this.hoursComboBox.setVisible(false);
		this.timeSepLabel.setVisible(false);
		this.minutesComboBox.setVisible(false);
		this.dowLabel.setVisible(false);
		this.dayOfWeekComboBox.setVisible(false);
		this.domLabel1.setVisible(false);
		this.dayOfMonthComboBox.setVisible(false);
		this.domLabel2.setVisible(false);
		this.repeatLabel.setVisible(false);
		this.intervalComboBox.setVisible(false);
		this.repeatHourLabel.setVisible(false);
		this.repeatMinuteLabel.setVisible(false);
		
		String type = (String)this.scheduleTypeComboBox.getSelectedItem();
		if (type == null || 
				type.equals("minute")) {
			this.repeatLabel.setVisible(true);
			this.intervalComboBox.setVisible(true);
			this.repeatMinuteLabel.setVisible(true);
			this.scheduleTypeComboBox.setSelectedItem("minute");
		} else if (type.equals("hour")) {
			this.repeatLabel.setVisible(true);
			this.intervalComboBox.setVisible(true);
			this.repeatHourLabel.setVisible(true);
		} else if (type.equals("day")) {
			this.timeLabel.setVisible(true);
			this.hoursComboBox.setVisible(true);
			this.timeSepLabel.setVisible(true);
			this.minutesComboBox.setVisible(true);
		} else if (type.equals("week")) {
			this.timeLabel.setVisible(true);
			this.hoursComboBox.setVisible(true);
			this.timeSepLabel.setVisible(true);
			this.minutesComboBox.setVisible(true);
			this.dowLabel.setVisible(true);
			this.dayOfWeekComboBox.setVisible(true);
		} else if (type.equals("month")) {
			this.timeLabel.setVisible(true);
			this.hoursComboBox.setVisible(true);
			this.timeSepLabel.setVisible(true);
			this.minutesComboBox.setVisible(true);
			this.domLabel1.setVisible(true);
			this.dayOfMonthComboBox.setVisible(true);
			this.domLabel2.setVisible(true);
		}
	}
	

	private void updateReportsList() {
		for (Report report : ((WabitWorkspace)task.getParent()).getReports()) {
			this.reportComboBox.addItem(report);
		}
	}
	
	public JComponent getSourceComponent() {
		return null;
	}

	public String getTitle() {
		return "Report Scheduler - " + task.getName();
	}

	public JToolBar getToolbar() {
		return this.toolbarBuilder.getToolbar();
	}

	public boolean applyChanges() {
		if (!dirty) {
			return true;
		}
		if (this.emailTextField.getText().equals("Hello.")) {
			this.showDialog("Hi.");
			return false;
		}
		if (this.reportComboBox.getSelectedIndex()==-1) {
			this.showDialog("A report must be selected.");
			return false;
		}
		if (this.emailTextField.getText()==null || 
			this.emailTextField.getText().equals("")) {
			this.showDialog("You need to provide an email adress to send the report to.");
			return false;
		}
		
		task.begin("Scheduled Report Modifications");
		task.setReport((Report)this.reportComboBox.getSelectedItem());
		task.setEmail(this.emailTextField.getText());
		task.setTriggerType((String)this.scheduleTypeComboBox.getSelectedItem());
		task.setTriggerIntervalParam(this.intervalComboBox.getSelectedIndex()+1);
		task.setTriggerHourParam(this.hoursComboBox.getSelectedIndex());
		task.setTriggerMinuteParam(this.minutesComboBox.getSelectedIndex());
		task.setTriggerDayOfWeekParam(this.dayOfWeekComboBox.getSelectedIndex());
		task.setTriggerDayOfMonthParam(this.dayOfMonthComboBox.getSelectedIndex()+1);
		task.commit();
		task.setNoob(false);
		this.dirty = false;
		JOptionPane.showMessageDialog(this.panel, "Your report schedule is now updated.", "Scheduled Report", JOptionPane.PLAIN_MESSAGE);
		return true;
	}

	private void showDialog(final String message) {
		JOptionPane.showMessageDialog(this.panel, message, "Scheduled Report Error", JOptionPane.WARNING_MESSAGE);
	}
	
	private void reinitGuiModel() {
		if (task.getReport()!=null) {
			this.reportComboBox.setSelectedItem(task.getReport());
		} else if (this.reportComboBox.getItemCount() > 0){
			this.reportComboBox.setSelectedIndex(0);
		}
		if (task.getEmail()!=null) {
			this.emailTextField.setText(task.getEmail());
		} else {
			this.emailTextField.setText("destination@example.com");
		}
		this.scheduleTypeComboBox.setSelectedItem(task.getTriggerType());
		this.intervalComboBox.setSelectedIndex(task.getTriggerIntervalParam()-1);
		this.hoursComboBox.setSelectedIndex(task.getTriggerHourParam());
		this.minutesComboBox.setSelectedIndex(task.getTriggerMinuteParam());
		this.dayOfWeekComboBox.setSelectedIndex(task.getTriggerDayOfWeekParam());
		this.dayOfMonthComboBox.setSelectedIndex(task.getTriggerDayOfMonthParam()-1);
		dirty = false;
	}
	
	public void discardChanges() {
		if (this.task.isNoob()) {
			try {
				if (this.workspace.getChildren().contains(this.task)) {
					this.workspace.removeChild(this.task);
				}
			} catch (IllegalArgumentException e) {
				log.error(e);
				throw new RuntimeException(e);
			} catch (ObjectDependentException e) {
				log.error(e);
				throw new RuntimeException(e);
			}
		}
		this.dirty = false;
	}

	public JComponent getPanel() {
		return this.panel;
	}

	public boolean hasUnsavedChanges() {
		return false;
	}
	
	
	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                	WabitWorkspace p = new WabitWorkspace();
                    
                    // Add data sources to workspace
                    DataSourceCollection<SPDataSource> plini = new PlDotIni();
                    plini.read(new File(System.getProperty("user.home"), "pl.ini"));
                    List<SPDataSource> dataSources = plini.getConnections();
                    for (int i = 0; i < 10 && i < dataSources.size(); i++) {
                        p.addDataSource(new WabitDataSource(dataSources.get(i)));
                    }
                    
                    // Add layouts to workspace
                    Report layout = new Report("Example Layout");
                    p.addReport(layout);
                    Page page = layout.getPage();
                    page.addContentBox(new ContentBox());
                    page.addGuide(new Guide(Axis.HORIZONTAL, 123));
                    page.addContentBox(new ContentBox());
                    
                    // dd a report task
                    ReportTask task = new ReportTask();
                    task.setReport(layout);
                    p.addReportTask(task);
                    
                	ReportTaskPanel panel = new ReportTaskPanel(task);
                	
                    JFrame f = new JFrame("TEST PANEL");
                    JPanel outerPanel = new JPanel(new BorderLayout());
                    outerPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                    outerPanel.add(panel.getPanel(), BorderLayout.CENTER);
                    f.setContentPane(outerPanel);
                    f.pack();
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.setVisible(true);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

}
