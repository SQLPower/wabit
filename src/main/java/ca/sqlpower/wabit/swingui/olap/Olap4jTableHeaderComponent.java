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

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.CellSetAxisMetaData;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

import ca.sqlpower.swingui.ColourScheme;

import com.jgoodies.forms.layout.CellConstraints;

public class Olap4jTableHeaderComponent extends JComponent {

    private static class HierarchyLabelGrid {
        List<List<String>> positionLabels;
        int maxDepth;
        int minDepth = Integer.MAX_VALUE;
        private final int ordinal;

        public HierarchyLabelGrid(int ordinal, int positionCount) {
            this.ordinal = ordinal;
            positionLabels = new ArrayList<List<String>>();
            for (int i = 0; i < positionCount; i++) {
                positionLabels.add(new LinkedList<String>());
            }
        }
        
        public void addLabelAtPosition(int position, String label) {
            List<String> labels = positionLabels.get(position);
            labels.add(0, label);
            if (labels.size() > maxDepth) {
                maxDepth = labels.size();
            }
        }

        /**
         * Returns the number of slots this label grid should take up in the
         * header. This is also the number of items returned by a call to
         * {@link #getLabelsForPosition(int)}.
         */
        public int getWidth() {
            return maxDepth - (minDepth - 1);
        }

        public int getOrdinal() {
            return ordinal;
        }
        
        /**
         * Returns a list of labels with size {@link #maxDepth} -
         * {@link #minDepth}. If the real set of labels at the given position
         * is smaller, it will be padded with trailing nulls.
         * 
         * @param position
         *            The position for which to retrieve the labels.
         * @return
         */
        public List<String> getLabelsForPosition(int position) {
            List<String> realPositionLabels = positionLabels.get(position);
            List<String> labels = new ArrayList<String>(
                    realPositionLabels.subList(minDepth - 1, realPositionLabels.size()));
            
            for (int depth = realPositionLabels.size(); depth < maxDepth; depth++) {
                labels.add(null);
            }
            
            return labels;
        }

        public void finalizePosition(int position) {
            List<String> labels = positionLabels.get(position);
            if (labels.size() < minDepth) {
                minDepth = labels.size();
            }
            
        }
    }
    
    public Olap4jTableHeaderComponent(CellSet cellSet) {
        CellSetAxis rowAxis = cellSet.getAxes().get(1);
        int positionCount = rowAxis.getPositionCount();
        
        CellSetAxisMetaData axisMetaData = rowAxis.getAxisMetaData();
        List<HierarchyLabelGrid> labelGrids =
            new ArrayList<HierarchyLabelGrid>(axisMetaData.getHierarchies().size());
        for (int i = 0; i < axisMetaData.getHierarchies().size(); i++) {
            labelGrids.add(new HierarchyLabelGrid(i, positionCount));
        }
        
        for (Position p : rowAxis.getPositions()) {
            List<Member> members = p.getMembers();
            int memberOrdinal = 0;
            for (Member member : members) {
                HierarchyLabelGrid lg = labelGrids.get(memberOrdinal);
                Member ancestor = member;
                do {
                    lg.addLabelAtPosition(p.getOrdinal(), ancestor.getName());
                    ancestor = ancestor.getParentMember();
                } while (ancestor != null);
                lg.finalizePosition(p.getOrdinal());
                memberOrdinal++;
            }
        }
        
        int columns = 0;
        for (HierarchyLabelGrid lg : labelGrids) {
            columns += lg.getWidth();
        }
        setLayout(new GridLayout(positionCount, columns));

        CellConstraints cc = new CellConstraints();
        
        String previousValue[] = new String[columns];
        for (int position = 0; position < positionCount; position++) {
            int column = 0;
            for (HierarchyLabelGrid lg : labelGrids) {
                for (String positionText : lg.getLabelsForPosition(position)) {
                    String labelText = null;
                    if (!nullSafeEqual(positionText, previousValue[column])) {
                        labelText = positionText;
                    }
                    JLabel label = new JLabel(labelText);
                    label.setBackground(ColourScheme.BACKGROUND_COLOURS[lg.getOrdinal()]);
                    label.setOpaque(true);
                    add(label, cc.xy(column + 1, position + 1));
                    
                    previousValue[column] = positionText;
                    column++;
                }
            }
        }
    }
    
    private static boolean nullSafeEqual(Object o1, Object o2) {
        if (o1 == null) return o2 == null;
        return o1.equals(o2);
    }
}
