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

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.object.HorizontalAlignment;
import ca.sqlpower.swingui.AlignmentIcons;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ReportUtil;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;

public class CellSetSwingRenderer implements SwingContentRenderer {
    
    private final CellSetRenderer renderer;

    public CellSetSwingRenderer(CellSetRenderer renderer) {
        this.renderer = renderer;
    }

    public DataEntryPanel getPropertiesPanel() {
        final JPanel panel = new JPanel(new MigLayout("", "[][grow][]", ""));
        
        panel.add(new JLabel("Header Font"), "gap related");
        final JLabel headerFontExample = new JLabel("Header Font Example");
        headerFontExample.addPropertyChangeListener("font" , new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent arg0) {
                JDialog dialog = (JDialog) SwingUtilities.getWindowAncestor(panel);
                if (dialog != null) {
                    panel.invalidate();
                    int newWidth = (int) Math.max(dialog.getContentPane().getPreferredSize().getWidth(), dialog.getContentPane().getSize().getWidth());
                    int newHeight = (int) Math.max(dialog.getContentPane().getPreferredSize().getHeight(), dialog.getContentPane().getSize().getHeight());
                    dialog.setSize(new Dimension(newWidth, newHeight));
                }
            }
        });
        headerFontExample.setFont(renderer.getHeaderFont());
        panel.add(headerFontExample, "gap related");
        panel.add(ReportUtil.createFontButton(headerFontExample, renderer), "wrap");
        
        panel.add(new JLabel("Body Font"), "gap related");
        final JLabel bodyFontExample = new JLabel("Body Font Example");
        bodyFontExample.setFont(renderer.getBodyFont());
        panel.add(bodyFontExample, "gap related");
        panel.add(ReportUtil.createFontButton(bodyFontExample, renderer), "wrap");
        
        ButtonGroup hAlignmentGroup = new ButtonGroup();
        final JToggleButton leftAlign = new JToggleButton(
                AlignmentIcons.LEFT_ALIGN_ICON, renderer.getBodyAlignment() == HorizontalAlignment.LEFT);
        hAlignmentGroup.add(leftAlign);
        final JToggleButton centreAlign = new JToggleButton(
                AlignmentIcons.CENTRE_ALIGN_ICON, renderer.getBodyAlignment() == HorizontalAlignment.CENTER);
        hAlignmentGroup.add(centreAlign);
        final JToggleButton rightAlign = new JToggleButton(
                AlignmentIcons.RIGHT_ALIGN_ICON, renderer.getBodyAlignment() == HorizontalAlignment.RIGHT);
        hAlignmentGroup.add(rightAlign);
        Box alignmentBox = Box.createHorizontalBox();
        alignmentBox.add(leftAlign);
        alignmentBox.add(centreAlign);
        alignmentBox.add(rightAlign);
        alignmentBox.add(Box.createHorizontalGlue());
        panel.add(new JLabel("Column Alignment"), "gap related");
        panel.add(alignmentBox, "span 2, wrap");
        
        final JComboBox bodyFormatComboBox = new JComboBox();
        bodyFormatComboBox.addItem(ReportUtil.DEFAULT_FORMAT_STRING);
        for (NumberFormat item : ReportUtil.getNumberFormats()) {
            bodyFormatComboBox.addItem(((DecimalFormat) item).toPattern());
        }
        if (renderer.getBodyFormat() != null) {
            bodyFormatComboBox.setSelectedItem(renderer.getBodyFormat().toPattern());
        } else {
            bodyFormatComboBox.setSelectedItem(ReportUtil.DEFAULT_FORMAT_STRING);
        }
        panel.add(new JLabel("Body Format"), "gap related");
        panel.add(bodyFormatComboBox, "span 2, wrap");
        
        
        return new DataEntryPanel() {
            
        
            public boolean hasUnsavedChanges() {
                return true;
            }
        
            public JComponent getPanel() {
                return panel;
            }
        
            public void discardChanges() {
                //do nothing
            }
        
            public boolean applyChanges() {
                renderer.setHeaderFont(headerFontExample.getFont());
                renderer.setBodyFont(bodyFontExample.getFont());
                
                if (leftAlign.isSelected()) {
                    renderer.setBodyAlignment(HorizontalAlignment.LEFT);
                } else if (rightAlign.isSelected()) {
                    renderer.setBodyAlignment(HorizontalAlignment.RIGHT);
                } else if (centreAlign.isSelected()) {
                    renderer.setBodyAlignment(HorizontalAlignment.CENTER);
                }
                
                if (bodyFormatComboBox.getSelectedItem().equals(ReportUtil.DEFAULT_FORMAT_STRING)) {
                    renderer.setBodyFormat(null);
                } else {
                    renderer.setBodyFormat(new DecimalFormat((String) bodyFormatComboBox.getSelectedItem()));
                }
                
                return true;
            }
        };
    }

    public void processEvent(PInputEvent event, int type) {
        if (type == MouseEvent.MOUSE_MOVED) {
            final PNode pickedNode = event.getPickedNode();
            Point2D p = event.getPositionRelativeTo(pickedNode);
            p = new Point2D.Double(p.getX() - pickedNode.getX(), p.getY() - pickedNode.getY());
            renderer.setMemberSelectedAtPoint(p);

        } else if (type == MouseEvent.MOUSE_RELEASED) {
            renderer.toggleSelectedMember();
        }
    }

}
