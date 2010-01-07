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

package ca.sqlpower.wabit.swingui.chart.effect;

import java.util.List;

import org.jfree.data.category.DefaultCategoryDataset;

import ca.sqlpower.wabit.swingui.chart.effect.interp.Interpolator;

public class BarChartAnimator extends AbstractChartAnimator {

    private final DefaultCategoryDataset animatingData;
    private final DefaultCategoryDataset originalData;
    
    private final Interpolator interpolator;

    public BarChartAnimator(
            int frameCount, int frameDelay,
            DefaultCategoryDataset dataset,
            Interpolator interpolator) {
        super(frameCount, frameDelay);
        animatingData = dataset;
        this.interpolator = interpolator;
        try {
            originalData = (DefaultCategoryDataset) animatingData.clone();
        } catch (CloneNotSupportedException impossible) {
            throw new RuntimeException("Can't clone DefaultCategoryDataset!?", impossible);
        }
    }

    @SuppressWarnings("unchecked")
	protected void setup() {
        for (Comparable rowKey : (List<Comparable>) animatingData.getRowKeys()) {
            for (Comparable colKey : (List<Comparable>) animatingData.getColumnKeys()) {
                animatingData.setValue(0.0, rowKey, colKey);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doFrame(int frame, double pct) {
        for (Comparable rowKey : (List<Comparable>) animatingData.getRowKeys()) {
            for (Comparable colKey : (List<Comparable>) animatingData.getColumnKeys()) {
                double orig = originalData.getValue(rowKey, colKey).doubleValue();

                animatingData.setValue(
                        interpolator.value(0.0, orig, pct), rowKey, colKey);
            }
        }
    }

}
