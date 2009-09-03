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

import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.report.HorizontalAlignment;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.VerticalAlignment;
import ca.sqlpower.wabit.swingui.Icons;
import edu.umd.cs.piccolo.event.PInputEvent;

public class ImageSwingRenderer implements SwingContentRenderer {
    
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ImageSwingRenderer.class);

    private static class ImageEntryPanel implements DataEntryPanel {
        
        private final JPanel panel;
        private JRadioButton preserveAspectButton;
        private JRadioButton scaleImageButton;
        private JToggleButton alignTop;
        private JToggleButton alignMiddle;
        private JToggleButton alignBottom;
        private JToggleButton alignLeft;
        private JToggleButton alignCentre;
        private JToggleButton alignRight;
        private final ImageRenderer renderer;
        
        public ImageEntryPanel(WabitWorkspace workspace, ImageRenderer renderer) {
            this.renderer = renderer;
            
            JLabel scalingModeLabel = new JLabel("Scaling Mode");
            
            ButtonGroup resizeModeButtonGroup = new ButtonGroup();
            preserveAspectButton = new JRadioButton("Preserve Aspect");
            scaleImageButton = new JRadioButton("Stretch");
            resizeModeButtonGroup.add(preserveAspectButton);
            resizeModeButtonGroup.add(scaleImageButton);
            preserveAspectButton.setSelected(renderer.isPreservingAspectRatio());
            scaleImageButton.setSelected(!renderer.isPreservingAspectRatio());
            
            JLabel alignmentLabel = new JLabel("Alignment");
            
            ButtonGroup vAlignButtonGroup = new ButtonGroup();
            alignTop = new JToggleButton(Icons.TOP_ALIGN_ICON);
            alignMiddle = new JToggleButton(Icons.MIDDLE_ALIGN_ICON);
            alignBottom = new JToggleButton(Icons.BOTTOM_ALIGN_ICON);
            vAlignButtonGroup.add(alignTop);
            vAlignButtonGroup.add(alignMiddle);
            vAlignButtonGroup.add(alignBottom);
            switch (renderer.getVAlign()) {
                case TOP:
                    alignTop.setSelected(true);
                    break;
                case MIDDLE:
                    alignMiddle.setSelected(true);
                    break;
                case BOTTOM:
                    alignBottom.setSelected(true);
                    break;
            }
            
            ButtonGroup hAlignButtonGroup = new ButtonGroup();
            alignLeft = new JToggleButton(Icons.LEFT_ALIGN_ICON);
            alignCentre = new JToggleButton(Icons.CENTRE_ALIGN_ICON);
            alignRight = new JToggleButton(Icons.RIGHT_ALIGN_ICON);
            hAlignButtonGroup.add(alignLeft);
            hAlignButtonGroup.add(alignCentre);
            hAlignButtonGroup.add(alignRight);
            switch (renderer.getHAlign()) {
                case LEFT:
                    alignLeft.setSelected(true);
                    break;
                case CENTER:
                    alignCentre.setSelected(true);
                    break;
                case RIGHT:
                    alignRight.setSelected(true);
                    break;
            }

            panel = new JPanel(new MigLayout());
            panel.add(scalingModeLabel, "wrap, span");
            panel.add(scaleImageButton, "gapbefore 10px, wrap, span");
            panel.add(preserveAspectButton, "gapbefore 10px, wrap, span");
            panel.add(alignmentLabel, "gapbefore 40px");
            panel.add(alignLeft);
            panel.add(alignCentre);
            panel.add(alignRight);
            panel.add(alignTop, "gapbefore unrelated");
            panel.add(alignMiddle);
            panel.add(alignBottom);
            
        }
        
        public boolean hasUnsavedChanges() {
            if (scaleImageButton.isSelected() && renderer.isPreservingAspectRatio()) return true;
            if (preserveAspectButton.isSelected() && !renderer.isPreservingAspectRatio()) return true;
            if (renderer.isPreservingAspectRatio()) {
                switch (renderer.getHAlign()) {
                    case LEFT:
                        if (!alignLeft.isSelected()) return true;
                        break;
                    case CENTER:
                        if (!alignCentre.isSelected()) return true;
                        break;
                    case RIGHT:
                        if(!alignRight.isSelected()) return true;
                        break;
                }
                switch (renderer.getVAlign()) {
                    case TOP:
                        if (!alignTop.isSelected()) return true;
                        break;
                    case MIDDLE:
                        if (!alignMiddle.isSelected()) return true;
                        break;
                    case BOTTOM:
                        if (!alignBottom.isSelected()) return true;
                        break;
                }
            }
            return false;
        }
    
        public JComponent getPanel() {
            return panel;
        }
    
        public void discardChanges() {
            //no-op
        }
    
        public boolean applyChanges() {
            if (scaleImageButton.isSelected()) {
                renderer.setPreservingAspectRatio(false);
            } else if (preserveAspectButton.isSelected()) {
                renderer.setPreservingAspectRatio(true);
                if (alignLeft.isSelected()) {
                    renderer.setHAlign(HorizontalAlignment.LEFT);
                } else if (alignCentre.isSelected()) {
                    renderer.setHAlign(HorizontalAlignment.CENTER);
                } else if (alignRight.isSelected()) {
                    renderer.setHAlign(HorizontalAlignment.RIGHT);
                }
                if (alignTop.isSelected()) {
                    renderer.setVAlign(VerticalAlignment.TOP);
                } else if (alignMiddle.isSelected()) {
                    renderer.setVAlign(VerticalAlignment.MIDDLE);
                } else if (alignBottom.isSelected()) {
                    renderer.setVAlign(VerticalAlignment.BOTTOM);
                }
            }
            return true;
        }
    };
    
    private final WabitWorkspace workspace;
    private final ImageRenderer renderer;
    
    public ImageSwingRenderer(WabitWorkspace workspace, ImageRenderer renderer) {
        this.workspace = workspace;
        this.renderer = renderer;
        
    }

    public DataEntryPanel getPropertiesPanel() {
        return new ImageEntryPanel(workspace, renderer);
    }

    public void processEvent(PInputEvent event, int type) {
        if (type == KeyEvent.KEY_PRESSED) {
        	if (event.getKeyCode() == KeyEvent.VK_CONTROL) {
        		renderer.setPreserveAspectRatioWhenResizing(false);
        	}
        } else if (type == KeyEvent.KEY_RELEASED) {
        	if (event.getKeyCode() == KeyEvent.VK_CONTROL) {
        		renderer.setPreserveAspectRatioWhenResizing(true);
        	}
        }
    }

}
