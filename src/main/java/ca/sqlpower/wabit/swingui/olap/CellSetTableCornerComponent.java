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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent.HierarchyComponent;

/**
 * A Component to be used as the lead corner component in the CellSetViewer.
 * Currently, it is primarily used to display the Dimensions in used by the row
 * component
 */
public class CellSetTableCornerComponent extends JComponent {
	
	/**
	 * The row component being used to determine which dimensions to display
	 */
	private List<HierarchyComponent> hierarchyComponents;

	public CellSetTableCornerComponent(List<HierarchyComponent> hierarchyComponents) {
		this.hierarchyComponents = hierarchyComponents;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
        super.paintComponent(g2);
        
        FontMetrics fm = g2.getFontMetrics();
        Font oldFont = g2.getFont();
        Font italicFont = oldFont.deriveFont(Font.ITALIC);
        Color oldColor = g2.getColor();
        
        g2.setColor(ColourScheme.HEADER_COLOURS[3]);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        try {
        	g2.setFont(italicFont);
	        int x = 0;
			ImageIcon dimensionIcon = OlapIcons.DIMENSION_ICON;
	        
			for (HierarchyComponent h: hierarchyComponents) {
				String hierarchyName = h.getHierarchy().getDimension().getName();
				g2.setColor(h.getBackground());
				g2.fillRect(x, getHeight() - Math.max(fm.getHeight(), dimensionIcon.getIconHeight()), 
						h.getWidth(), Math.max(fm.getHeight(), dimensionIcon.getIconHeight()));
				g2.setColor(oldColor);
				dimensionIcon.paintIcon(this, g2, x, getHeight() - dimensionIcon.getIconHeight());
        		g2.drawString(hierarchyName, x + dimensionIcon.getIconWidth(), getHeight() - fm.getDescent());
        		x += h.getWidth();
			}
        } finally {
        	g2.setFont(oldFont);
        	g2.setColor(oldColor);
        }
	}
}
