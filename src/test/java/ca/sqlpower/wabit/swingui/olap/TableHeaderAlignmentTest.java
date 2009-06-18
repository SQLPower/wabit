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

package ca.sqlpower.wabit.swingui.olap;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class TableHeaderAlignmentTest {

	private static final Object[] ROW_HEADER = new Object[] { "Animal", "Sound" };
	
	private static final Object[][] ROW_DATA = new Object[][]
	{
		{ "Cow", "Moo" },
		{ "Sheep", "Baa" },
		{ "Cat", "Meow" },
		{ "Duck", "Quack" },
		{ "Cow", "Moo" },
		{ "Sheep", "Baa" },
		{ "Cat", "Meow" },
		{ "Duck", "Quack" },
		{ "Cow", "Moo" },
		{ "Sheep", "Baa" },
		{ "Cat", "Meow" },
		{ "Duck", "Quack" },
		{ "Cow", "Moo" },
		{ "Sheep", "Baa" },
		{ "Cat", "Meow" },
		{ "Duck", "Quack" },
	};
	
	public static void main(String[] args) {
		final JTable table = new JTable(ROW_DATA, ROW_HEADER);
		
		JScrollPane sp = new JScrollPane(table);
		sp.setRowHeaderView(new RowLabelComponent(table));
		
        table.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mousePressed(MouseEvent e) {
        		Font font = table.getFont();
				table.setFont(font.deriveFont(font.getSize() + 3f));
				table.setRowHeight(table.getFontMetrics(table.getFont()).getHeight());
        	}
        });
        
		JFrame f = new JFrame("Table alignments!");
		f.setContentPane(sp);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
	
	private static class RowLabelComponent extends JPanel {

		private final JTable table;

		RowLabelComponent(JTable table) {
			this.table = table;
			PropertyChangeListener heightListener = new PropertyChangeListener() {
	        	public void propertyChange(PropertyChangeEvent evt) {
	        		repaint();
	        	}
	        };
	        
	        table.addPropertyChangeListener("rowHeight", heightListener);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setFont(table.getFont());
			FontMetrics fm = g.getFontMetrics();
			int y = fm.getAscent();
			for (int i = 0; i < table.getRowCount(); i++) {
				g.drawString(String.valueOf(i + 1), 0, y);
				y += table.getRowHeight();
			}
		}
	}
}
