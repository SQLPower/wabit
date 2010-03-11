/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.wabit.swingui.report;

import java.awt.Component;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.selectors.ComboBoxSelector;
import ca.sqlpower.wabit.report.selectors.Selector;
import ca.sqlpower.wabit.report.selectors.TextBoxSelector;
import ca.sqlpower.wabit.swingui.WabitIcons;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.action.DeleteReportParameterAction;
import ca.sqlpower.wabit.swingui.action.EditReportParameterAction;
import ca.sqlpower.wabit.swingui.report.selectors.SelectorFactory;

import com.lowagie.text.Font;

public class ReportPanel extends LayoutPanel {

	private final JPanel scrollerContents;
	private final JScrollPane parametersScrollPane;
	private JPanel dashboardPanel;
	final private JSplitPane splitPane;
	private final Report report;
	private SelectorFactory factory = new SelectorFactory();
	private Map<Selector, Component> selectorComponents = new HashMap<Selector, Component>();
	
	private final SPListener reportListener = new AbstractSPListener() {
		@Override
		protected void childAddedImpl(SPChildEvent e) {
			if (e.getChildType().equals(Selector.class)) {
				e.getChild().addSPListener(selectorListener);
				updateParameters();
				dashboardPanel.revalidate();				
			}
		}
		@Override
		protected void childRemovedImpl(SPChildEvent e) {
			if (e.getChildType().equals(Selector.class)) {
				e.getChild().removeSPListener(selectorListener);
				selectorComponents.remove(e.getChild());
				updateParameters();
				dashboardPanel.revalidate();				
			}
		}
	};
	
	
	private final SPListener selectorListener = new AbstractSPListener() {
		protected void propertyChangeImpl(PropertyChangeEvent evt) {
			updateParameters();
			dashboardPanel.revalidate();
		};
	};
	
	
	private final Runnable reportRefresher = new Runnable() {
		public void run() {
			refreshDataAction.actionPerformed(null);
		}
	};
	
	public ReportPanel(WabitSwingSession session, final Report report) {
		super(session, report);
		this.report = report;
		
		// build the dashboard controls
		this.dashboardPanel = new JPanel(new MigLayout("hidemode 1, fillx", "[][grow][][]"));
		this.dashboardPanel.add(new Label("Parameters"), "spanx 3");
		
		
		final JButton addButton = new JButton("+");
		addButton.setFont(addButton.getFont().deriveFont(10f));
		this.dashboardPanel.add(addButton, "wrap");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//Custom button text
				Object[] options = {"Drop down list", "Free form text box"};
				
				final int n = JOptionPane.showOptionDialog(null,
				    "What type of parameter selector would you like to create?",
				    "",
				    JOptionPane.YES_NO_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    WabitIcons.QUERY_32,
				    options,
				    options[0]);
				
				final Selector newSelector;
				if (n == JOptionPane.CLOSED_OPTION) {
					return;
				} else if (n == 0) {
					newSelector = new ComboBoxSelector();
				} else if (n == 1) {
					newSelector = new TextBoxSelector();
				} else {
					throw new AssertionError();
				}
				
				report.addChild(newSelector, report.getChildren(Selector.class).size());
				updateParameters();
				dashboardPanel.revalidate();
				
				EditReportParameterAction todo =
						new EditReportParameterAction(
								dashboardPanel, 
								newSelector);
				
				todo.actionPerformed(null);
			}
		});
		
		
		
		JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
		this.dashboardPanel.add(sep, "span, wrap, growx, gapbottom 10px");

		
		// Create a scrolling panel of the parameters
		this.scrollerContents = new JPanel(new MigLayout("hidemode 1, fillx", "[grow][][]"));
		
		this.parametersScrollPane = 
			new JScrollPane(
					scrollerContents,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		updateParameters();
		
		this.dashboardPanel.add(parametersScrollPane, "span, wrap, growx");
		
		
		
		// Fuse the dashboard panel with the source list.
		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.splitPane.setTopComponent(super.getSourceComponent());
		this.splitPane.setBottomComponent(dashboardPanel);
		this.splitPane.setOneTouchExpandable(true);
		
		// Setting a divider location needs the divider
		// to be visible, so we wrap this in a runnable.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				splitPane.setDividerLocation(0.5d);
			}
		});
		
		
		// Attach listeners
		this.report.addSPListener(reportListener);
		for (Selector selector : this.report.getSelectors()) {
			selector.addSPListener(selectorListener);
		}
	}
	
	
	private synchronized void updateParameters() {
		
		
		this.scrollerContents.removeAll();
		
		if (report.getSelectors().size() == 0) {
			this.parametersScrollPane.setVisible(false);
			return;
		} else {
			this.parametersScrollPane.setVisible(true);
		}
		
		for (Selector selector : report.getSelectors()) {
			
			JLabel label = new JLabel(selector.getName());
			label.setFont(label.getFont().deriveFont(Font.BOLD));
			
			this.scrollerContents.add(
					label,
					"span, wrap, growx");
			
			if (!selectorComponents.containsKey(selector)) {
				selectorComponents.put(
						selector, 
						factory.makeSelector(
								session.getWorkspace(),
								selector.getUUID(),
								reportRefresher));
			}
			
			this.scrollerContents.add(
					selectorComponents.get(selector),
					"growx, wmin 100px, gapleft 15px");
			
			this.scrollerContents.add(
					new JButton(
							new EditReportParameterAction(
									this.dashboardPanel, 
									selector)),
					"wmin 25px, wmax 25px");
			
			this.scrollerContents.add(
					new JButton(
							new DeleteReportParameterAction(
									dashboardPanel, 
									selector,
									reportRefresher)), 
					"wrap, wmin 25px, wmax 25px");
		}
	}
	
	
	@Override
	protected void cleanup() {
		super.cleanup();
		this.report.removeSPListener(reportListener);
		factory.cleanup();
		selectorComponents.clear();
	}

	/**
	 * We override this component because we want to add a more complex component
	 * that will display the source stuff but also the dashboard controls.
	 */
	public JComponent getSourceComponent() {
		return this.splitPane;
	}
}
