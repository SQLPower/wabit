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

package ca.sqlpower.wabit.swingui.report.selectors;

import java.awt.Component;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.selectors.ComboBoxSelector;
import ca.sqlpower.wabit.report.selectors.Selector;
import ca.sqlpower.wabit.report.selectors.TextBoxSelector;
import ca.sqlpower.wabit.swingui.WabitIcons;
import ca.sqlpower.wabit.swingui.action.DeleteReportParameterAction;
import ca.sqlpower.wabit.swingui.action.EditReportParameterAction;

import com.lowagie.text.Font;

public class SelectorsPanel extends JPanel {

	private final JPanel scrollerContents;
	private final JScrollPane parametersScrollPane;
	private final WabitObject selectorsSource;
	private SelectorFactory factory = new SelectorFactory();
	private Map<Selector, Component> selectorComponents = new HashMap<Selector, Component>();
	private final Runnable refreshRoutine;
	
	private final SPListener reportListener = new AbstractSPListener() {
		@Override
		protected void childAddedImpl(SPChildEvent e) {
			if (e.getChildType().equals(Selector.class)) {
				e.getChild().addSPListener(selectorListener);
				updateParameters();
				revalidate();				
			}
		}
		@Override
		protected void childRemovedImpl(SPChildEvent e) {
			if (e.getChildType().equals(Selector.class)) {
				e.getChild().removeSPListener(selectorListener);
				selectorComponents.remove(e.getChild());
				updateParameters();
				revalidate();				
			}
		}
	};
	
	
	private final SPListener selectorListener = new AbstractSPListener() {
		protected void propertyChangeImpl(PropertyChangeEvent evt) {
			updateParameters();
			revalidate();
		};
	};
	

	
	
	public SelectorsPanel(final WabitObject selectorsSource, Runnable refreshRoutine) 
	{
		super(new MigLayout("hidemode 1", "[grow][]", "[shrink 99999][][grow 99999]"));
		this.selectorsSource = selectorsSource;
		this.refreshRoutine = refreshRoutine;
	
		Box labelBox = Box.createHorizontalBox();
		labelBox.add(new JLabel(WabitIcons.PARAMETERS_12));
		labelBox.add(new Label("Parameters"));
		add(labelBox, "spanx 3");
		
		 
		final JButton addButton = new JButton(WabitIcons.ADD_12);
		addButton.setFont(addButton.getFont().deriveFont(10f));
		add(addButton, "wrap");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//Custom button text
				Object[] options = {"Drop down list", "Free form text"};
				
				final int n = JOptionPane.showOptionDialog(null,
				    "What type of parameter selector would you like to create?",
				    "",
				    JOptionPane.YES_NO_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    WabitIcons.PARAMETERS_NEW_32,
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
				
				selectorsSource.addChild(newSelector, selectorsSource.getChildren(Selector.class).size());
				updateParameters();
				revalidate();
				
				EditReportParameterAction todo =
						new EditReportParameterAction(
								SelectorsPanel.this, 
								newSelector);
				
				todo.actionPerformed(null);
			}
		});
		
		
		
		JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
		add(sep, "span, wrap, growx, gapbottom 5px, gaptop 5px");

		
		// Create a scrolling panel of the parameters
		this.scrollerContents = 
				new JPanel(
						new MigLayout(
								"hidemode 1", 
								"[grow][]0[]", 
								"[shrink]"));
		
		this.parametersScrollPane = 
			new JScrollPane(
					scrollerContents,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		updateParameters();
		
		add(parametersScrollPane, "span, wrap, grow");
		
		
		
		
		
		
		// Attach listeners
		this.selectorsSource.addSPListener(reportListener);
		for (Selector selector : this.selectorsSource.getChildren(Selector.class)) {
			selector.addSPListener(selectorListener);
		}
	}
	
	
	
private synchronized void updateParameters() {
		
		
		this.scrollerContents.removeAll();
		
		if (this.selectorsSource.getChildren(Selector.class).size() == 0) {
			this.parametersScrollPane.setVisible(false);			
			return;
		} else {
			this.parametersScrollPane.setVisible(true);
		}
		
		for (Selector selector : this.selectorsSource.getChildren(Selector.class)) {
			
			JLabel label = new JLabel(selector.getName());
			label.setFont(label.getFont().deriveFont(Font.BOLD));
			
			this.scrollerContents.add(
					label,
					"span, wrap, growx");
			
			if (!selectorComponents.containsKey(selector)) {
				selectorComponents.put(
						selector, 
						factory.makeSelector(
								selector,
								refreshRoutine));
			}
			
			this.scrollerContents.add(
					selectorComponents.get(selector),
					"growx, wmin 100px, gapleft 15px, gapbottom 10px, gapright 0px");
			
			this.scrollerContents.add(
					new JButton(
							new EditReportParameterAction(
									this, 
									selector)),
					"wmin 25px, wmax 25px, gapbottom 10px, gapleft 0px, gapright 0px");
			
			this.scrollerContents.add(
					new JButton(
							new DeleteReportParameterAction(
									this, 
									selector,
									refreshRoutine)), 
					"wrap, wmin 25px, wmax 25px, gapbottom 10px, gapleft 0px, gapright 0px");
		}
	}



	public void cleanup() {
		this.selectorsSource.removeSPListener(reportListener);
		factory.cleanup();
		selectorComponents.clear();
	}
	
}
