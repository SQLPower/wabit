/*
 * Copyright (c) 2008, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ca.sqlpower.wabit.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.SQLGroupFunction;
import ca.sqlpower.swingui.table.TableModelSortDecorator;


/**
 * A renderer that extends JPanel.This renderer will add a JComboBox 
 * and a JtextArea to the JHeader of he resultTable for
 * Group By and Having filters.  
 */

public class ComponentCellRenderer extends JPanel implements TableCellRenderer {

	private static final Logger logger = Logger.getLogger(ComponentCellRenderer.class);
	
	public static final String PROPERTY_GROUP_BY = "GROUP_BY";

	/**
	 * This property will define a change to a having field. The old value will
	 * be the value when entering the text field as it is only fired on leaving
	 * the text field.
	 */
	public static final String PROPERTY_HAVING = "HAVING";

	private final TableCellRenderer renderer;
	private final JTableHeader tableHeader;
	/**
	 * The height of the header portion that displays the column name.
	 * This height does not include the combo box or having field.
	 */
	private int labelHeight;
	private int comboBoxHeight;
	private int comboBoxWidth;
	private int havingFieldHeight;
	private ArrayList<JComboBox> comboBoxes;
	private ArrayList<JTextField> textFields;
	private boolean groupingEnabled;
	
	/**
	 * The sort decorator defined for the table this header is attached to.
	 * The sort decorator needs to know what part of the header is clickable
	 * to sort. When the column header is moved we need to update the sort
	 * decorator.
	 * 
	 * Note: This will be null if there is no sort decorator.
	 */
	private final TableModelSortDecorator sortDecorator;
	
	/**
	 * A list of listeners that fire when the group by or having clause changes.
	 */
	private final List<PropertyChangeListener> listeners;

	/**
	 * The table this renderer is on.
	 */
	private final JTable table;
	
	/**
	 * Constructs a cell renderer for a table header that allows editing of the
	 * table's group by and having clauses. The sort decorator is the sort decorator
	 * attached to the table or null if the table does not have a sort decorator.
	 */
	public ComponentCellRenderer(JTable t, TableModelSortDecorator sortDecorator) {
		this.table = t;
		this.sortDecorator = sortDecorator;
		listeners = new ArrayList<PropertyChangeListener>();
		tableHeader = t.getTableHeader();
		renderer = t.getTableHeader().getDefaultRenderer();
		tableHeader.addMouseListener(new HeaderMouseListener());
		groupingEnabled = false;
		comboBoxes = new ArrayList<JComboBox>();
		Vector<String> comboBoxItems = new Vector<String>();
		Object[] tempGroupItems =SQLGroupFunction.values();
		comboBoxItems.add(QueryCache.GROUP_BY);

		for(Object item : tempGroupItems) {
			comboBoxItems.add(item.toString());
		}

		textFields = new ArrayList<JTextField>();
		for(int i = 0 ; i < t.getColumnCount(); i++) {
			final JTextField textField = new JTextField();
			final JComboBox comboBox = new JComboBox(comboBoxItems);
			comboBox.setFont(comboBox.getFont().deriveFont(tableHeader.getFont().getSize2D()));
			comboBoxes.add(comboBox);
			textFields.add(textField);
			
			comboBox.addActionListener(new ActionListener() {
				private String oldSelectedItem;
				public void actionPerformed(ActionEvent e) {
					for (PropertyChangeListener l : listeners) {
						String selectedItem = (String)comboBox.getSelectedItem();
						l.propertyChange(new PropertyChangeEvent(comboBox, PROPERTY_GROUP_BY, oldSelectedItem, selectedItem));
						oldSelectedItem = selectedItem;
					}
				}
			});
			
			textField.addFocusListener(new FocusListener() {
				private String oldValue;
				public void focusLost(FocusEvent e) {
					for (PropertyChangeListener l : listeners) {
						l.propertyChange(new PropertyChangeEvent(textField, PROPERTY_HAVING, oldValue, textField.getText()));
					}
				}
				public void focusGained(FocusEvent e) {
					oldValue = textField.getText();
				}
			});
			
			textField.addKeyListener(new KeyListener() {
				public void keyTyped(KeyEvent e) {
					//Do nothing
				}
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						for (PropertyChangeListener l : listeners) {
							l.propertyChange(new PropertyChangeEvent(textField, PROPERTY_HAVING, null, textField.getText()));
						}
					}
				}
				public void keyPressed(KeyEvent e) {
					// Do nothing
				}
			});

			if(i == 0) {
				// takes the first ComboBoxes and TextField's height
				comboBoxHeight = comboBoxes.get(i).getPreferredSize().height;
				comboBoxWidth = comboBoxes.get(i).getPreferredSize().width;
				havingFieldHeight = textFields.get(i).getPreferredSize().height;
			}
		}
		
		setLayout(new BorderLayout());
	}

	/**
	 * Implementing the getComponent method on the renderer, this will take the current header
	 * and add a JComboBox as well as a JTextField for Group By and having filters.
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		Component c = renderer.getTableCellRendererComponent(table, value, 
				isSelected, hasFocus, row, column);
		if(c instanceof JLabel) {
			removeAll();
			int labelYPos;
			if(!groupingEnabled) {
				add((JLabel)c, BorderLayout.NORTH);
				labelHeight = c.getPreferredSize().height;
				labelYPos = 0;
			} else {
				int modelIndex = table.getColumnModel().getColumn(column).getModelIndex();
				JComboBox comboBox = new JComboBox(new Object[] { comboBoxes.get(modelIndex).getSelectedItem() });
				comboBox.setFont(comboBox.getFont().deriveFont(tableHeader.getFont().getSize2D()));
				add(comboBox, BorderLayout.NORTH);
				add(new JTextField(textFields.get(modelIndex).getText()), BorderLayout.CENTER);
				add((JLabel)c, BorderLayout.SOUTH);
				labelYPos = c.getY();

				// we need to consistently set the size of the TextField in case they resize while its focused
				textFields.get(modelIndex).setBounds(getXPositionOnColumn(table.getColumnModel(),column), comboBoxHeight, 
						table.getColumnModel().getColumn(column).getWidth(), 
						textFields.get(column).getSize().height);

				labelHeight = c.getPreferredSize().height;
				logger.debug("Provided cell renderer for viewIndex="+column+" modelIndex="+modelIndex);
			}
			logger.debug("Y position is " + labelYPos);
			sortDecorator.setTableHeaderYBounds(labelYPos, labelHeight);
		}
		return this;
	}
	
	/**
	 * This MouseListener handles clicking on the TableHeader. It will check the position of the click o determine its click
	 * as well as what component is being clicked.
	 */
	private class HeaderMouseListener extends MouseAdapter {
		
		public void mouseClicked(MouseEvent e) {
			
			if(!groupingEnabled) {
				return;
			}
			int comboBoxY = comboBoxHeight;
			int havingFieldY =  comboBoxHeight+ havingFieldHeight;
			JTableHeader h = (JTableHeader) e.getSource();
			TableColumnModel columnModel = h.getColumnModel();
			int viewIndex = columnModel.getColumnIndexAtX(e.getX());

			logger.debug("viewIndex is:" + viewIndex);

			if ( viewIndex < 0) {
				return;    			
			}

			int modelIndex = columnModel.getColumn(viewIndex).getModelIndex();
			logger.debug("modelIndex is:" + modelIndex);

			// when press anything other than TextField
			if ( e.getY() < comboBoxY || e.getY() > havingFieldY ) {
				//Disable Focus on textField if it presses anywhere else on the header.
				textFields.get(modelIndex).setFocusable(false);
			}
			
			// when click comboBox
			if (e.getY() < comboBoxY && groupingEnabled ) {

				TableColumn tc = columnModel.getColumn(viewIndex);

				// add a bufferZone So we can resize column and now have the comboBox showing
				if(e.getX()-getXPositionOnColumn(columnModel, viewIndex) < 3 || 
						(getXPositionOnColumn(columnModel, viewIndex) + tc.getWidth()) -e.getX() < 3) {
					return;
				}
				JComboBox tempCB = comboBoxes.get(modelIndex);
				h.add(tempCB);
				tempCB.setBounds(getXPositionOnColumn(columnModel, viewIndex),0, tc.getWidth(), comboBoxHeight);
				logger.debug("Temporarily placing combo box at " + tempCB.getBounds());
				tempCB.setPopupVisible(true);

				tempCB.addPopupMenuListener(new PopupMenuListener() {

					public void popupMenuCanceled(PopupMenuEvent e) {
						// don't care
					}

					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
						JComboBox cb = (JComboBox) e.getSource();
						cb.removePopupMenuListener(this);
						Container cbparent = cb.getParent();
						cbparent.remove(cb);
						cbparent.repaint();
					}

					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						JComboBox cb = (JComboBox) e.getSource();
						cb.repaint();
					}

				});

			}
		}

		public void mousePressed(MouseEvent e) {
			
			if(!groupingEnabled) {
				return;
			}
			int comboBoxY = comboBoxHeight;
			int havingFieldY =  comboBoxHeight+ havingFieldHeight;
			JTableHeader h = (JTableHeader) e.getSource();
			TableColumnModel columnModel = h.getColumnModel();
			int viewIndex = columnModel.getColumnIndexAtX(e.getX());

			logger.debug("viewIndex is:" + viewIndex);

			if ( viewIndex < 0) {
				return;    			
			}

			int modelIndex = columnModel.getColumn(viewIndex).getModelIndex();
			logger.debug("modelIndex is:" + modelIndex);

			// when press anything other than TextField
			if ( e.getY() < comboBoxY || e.getY() > havingFieldY ) {
				//Disable Focus on textField if it presses anywhere else on the header.
				textFields.get(modelIndex).setFocusable(false);
			}
			// when press text Field
			else if (e.getY() > comboBoxY && e.getY() < havingFieldY) {

				if(!textFields.get(modelIndex).isEnabled()) {
					return;
				}
				// reEnable the TextField if they clicked on the TextFieldArea
				textFields.get(modelIndex).setFocusable(true);

				JTextField tempTextField = textFields.get(modelIndex);
				h.add(tempTextField);

				if (!tempTextField.isVisible()) {
					throw new IllegalStateException("tempTextField was not visible");
				}
				TableColumn tc = columnModel.getColumn(viewIndex);
				tempTextField.setBounds(getXPositionOnColumn(columnModel, viewIndex), comboBoxY, tc.getWidth(), havingFieldHeight);
				tempTextField.requestFocus();
				logger.debug("Temporarily placing TextField at " + tempTextField.getBounds());
				tempTextField.addFocusListener(new FocusListener() {

					public void focusGained(FocusEvent e) {
						JTextField tf = (JTextField)e.getSource();
						Container tfparent = tf.getParent();
						tfparent.repaint();
					}
					public void focusLost(FocusEvent e) {
						JTextField tf = (JTextField)e.getSource();
						Container tfparent = tf.getParent();

						logger.debug("child is" + tf);
						logger.debug("parent is"+ tfparent);

						if (tfparent != null) {
							tfparent.remove(tf);
							tfparent.repaint();
						}
					}});	
			}	
		}
	}

	/**
	 * Returns the x position of the given a column index.
	 */
	public int getXPositionOnColumn(TableColumnModel model, int columnIndex) {
		int sum = 0;
		for(int i = 0; i < columnIndex; i ++) {
			sum += model.getColumn(i).getWidth();
		}
		return sum;
	}

	public ArrayList<JComboBox> getComboBoxes () {
		return comboBoxes;
	}

	public ArrayList<JTextField> getTextFields () {
		return textFields;
	}

	public void setGroupingEnabled(boolean flag) {
		
		groupingEnabled = flag;
		
		if (groupingEnabled) {
			tableHeader.getParent().setPreferredSize(new Dimension(
					tableHeader.getParent().getPreferredSize().width,
					labelHeight +comboBoxHeight+ havingFieldHeight));
			for(int i = 0; i < comboBoxes.size(); i++) {
				int tempWidth = Math.max(comboBoxWidth, tableHeader.getColumnModel().getColumn(i).getWidth());
				tableHeader.getColumnModel().getColumn(i).setPreferredWidth(tempWidth);			
			}
		} else {
			tableHeader.getParent().setPreferredSize(new Dimension(
					tableHeader.getParent().getPreferredSize().width, labelHeight));	
		}
		tableHeader.revalidate();
	}
	
	/**
	 * Gets the height of the column header label. This will
	 * not include the height of the combo box or filter field.
	 */
	public int getLabelHeight() {
		return labelHeight;
	}

	public void addTableListenerToSortDecorator(TableModelListener l) {
		sortDecorator.addTableModelListener(l);
	}
	
	public void removeTableListenerToSortDecorator(TableModelListener l) {
		sortDecorator.removeTableModelListener(l);
	}
	
	public void setSortingStatus(LinkedHashMap<Integer, Integer> columnSortMap) {
		sortDecorator.setSortingStatus(columnSortMap);
	}
	
	public void addGroupAndHavingListener(PropertyChangeListener l) {
		listeners.add(l);
	}
	
	public void removeGroupAndHavingListener(PropertyChangeListener l) {
		listeners.remove(l);
	}

	public JTable getTable() {
		return table;
	}
}
