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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartChangeEvent;

import ca.sqlpower.wabit.report.chart.ChartGradientPainter;

public class WabitJFreeChartPanel extends ChartPanel {

    public WabitJFreeChartPanel(JFreeChart chart) {
        super(chart, false, false, false, false, false);
        setBackground(new Color(0, true));
    }
    
    @Override
    public void chartChanged(ChartChangeEvent event) {
        super.chartChanged(event);
    }

    @Override
    public void paintComponent(Graphics g) {
    	if (getChart() == null){
    		return;
    	}
    	
        Graphics2D g2 = (Graphics2D) g;

        float baseline = getXaxisBaseline();
        
        ChartGradientPainter.paintChartGradient(g2, getBounds(), (int) baseline);
        
        g2.setColor(getForeground());
        super.paintComponent(g2);
        
        // this rendering has a different layout than last time;
        // have to paint again to update gradient position
        if (Math.abs(baseline - getXaxisBaseline()) > 1f) {
            repaint();
        }
    }

    private float getXaxisBaseline() {
        Rectangle2D dataArea = getScreenDataArea();
        return (float) (dataArea.getY() + dataArea.getHeight());
    }
    
    
}
