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

package ca.sqlpower.wabit.report.chart;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class ChartGradientPainter {

    private static Color topGradientColour = new Color(0xEEEEEE);
    private static Color bottomGradientColour = new Color(0xFFFFFF);
    
    public static void paintChartGradient(Graphics2D g, Rectangle2D bounds, int baseline) {
        int x = (int) bounds.getX();
        int y = (int) bounds.getY();
        int width = (int) bounds.getWidth();
        int height = (int) bounds.getHeight();
        
        g.setPaint(new GradientPaint(
                0, y, topGradientColour,
                0, baseline, bottomGradientColour));
        g.fillRect(x, y, width, baseline);

        g.setPaint(new GradientPaint(
                0, baseline, topGradientColour,
                0, height, bottomGradientColour));
        g.fillRect(
                x, baseline,
                width, height);
        
    }

}
