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

package ca.sqlpower.wabit.swingui.report;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.WabitLabel;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.ReportContentRenderer;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeCellRenderer;

/**
 * This is an icon which previews your template
 */
public class TemplatePreviewIcon implements Icon {
	private final Template template;
	private final static int SHRINK_MULTIPLIER = 7;
	private final static int PADDING = 11;

	public TemplatePreviewIcon(Template template) {
		this.template = template;
	}

	public int getIconHeight() {
		return ((template.getPage().getHeight() ) / SHRINK_MULTIPLIER) + (PADDING);
	}

	public int getIconWidth() {
		return  ((template.getPage().getWidth()) / SHRINK_MULTIPLIER) + (PADDING);
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D graphics = (Graphics2D) g;
		
		Page page = template.getPage();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(PADDING, PADDING, page.getWidth() / SHRINK_MULTIPLIER, page.getHeight()/ SHRINK_MULTIPLIER);
		Map<Key, Object> rendering = new HashMap<Key, Object>();
		rendering.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		rendering.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		rendering.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHints(rendering);
		for (ContentBox cb : page.getContentBoxes()) {
			int cbX = (int) (cb.getX() / SHRINK_MULTIPLIER) + PADDING;
			int cbY = (int) (cb.getY() / SHRINK_MULTIPLIER) + PADDING;
			int cbWidth = (int) cb.getWidth() / SHRINK_MULTIPLIER;
			int cbHeight = (int) cb.getHeight() / SHRINK_MULTIPLIER;
			ReportContentRenderer contentRenderer = cb.getContentRenderer();
			if (contentRenderer instanceof ImageRenderer) {
				Image image = ((ImageRenderer) contentRenderer).getImage().getImage();
				graphics.drawImage(image, cbX, cbY, cbWidth, cbHeight, null);
			} else if (contentRenderer instanceof WabitLabel) {
				graphics.setColor(Color.BLACK);
				WabitLabel label = (WabitLabel) contentRenderer;
				Font font = label.getFont();
				graphics.setFont(font.deriveFont((float) (font.getSize() / 3)));

				graphics.drawString(label.getText(), cbX, cbY + graphics.getFontMetrics().getHeight());
			} else if (contentRenderer instanceof ResultSetRenderer) {
				graphics.setColor(Color.LIGHT_GRAY);
				graphics.drawRoundRect(cbX, cbY, cbWidth, cbHeight, 7, 7);
				int dimensions = Math.min(cbWidth, cbHeight); //preserve aspect ratio
				cbX += ((cbWidth / 2) - (dimensions / 4));
				cbY += ((cbHeight / 2) - (dimensions / 4));
				graphics.drawImage(((ImageIcon) WorkspaceTreeCellRenderer.QUERY_ICON).getImage(), cbX, cbY, dimensions / 2, dimensions / 2, null);
			} else if (contentRenderer instanceof CellSetRenderer) {
				graphics.setColor(Color.LIGHT_GRAY);
				graphics.drawRoundRect(cbX, cbY, cbWidth, cbHeight, 7, 7);
				int dimensions = Math.min(cbWidth, cbHeight); //preserve aspect ratio
				cbX += ((cbWidth / 2) - (dimensions / 4));
				cbY += ((cbHeight / 2) - (dimensions / 4));
				graphics.drawImage(((ImageIcon) WorkspaceTreeCellRenderer.OLAP_QUERY_ICON).getImage(), cbX, cbY, dimensions / 2, dimensions / 2, null);
			} else if (contentRenderer == null) {
				graphics.setColor(Color.LIGHT_GRAY);
				graphics.drawRoundRect(cbX, cbY, cbWidth, cbHeight, 7, 7);
			}
		}
	}
}
