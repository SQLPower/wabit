/*
 * Copyright (c) 2008, SQL Power Group Inc.
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
import java.awt.Dimension;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.report.Report;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;

public class ReportLayoutPanel {

    private static final Logger logger = Logger.getLogger(ReportLayoutPanel.class);
    
    private final PCanvas canvas;
    private final Report report;
    private final PageNode pageNode;
    
    public ReportLayoutPanel(Report report) {
        this.report = report;
        canvas = new PCanvas();
        canvas.setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        canvas.setPanEventHandler(null);
        canvas.setBackground(Color.LIGHT_GRAY);
        canvas.setPreferredSize(new Dimension(400,600));
        
        pageNode = new PageNode(report.getPage());
        canvas.getLayer().addChild(pageNode);
        PSelectionEventHandler selectionEventHandler = new GuideAwareSelectionEventHandler(pageNode, pageNode);
        canvas.addInputEventListener(selectionEventHandler);
        pageNode.setPickable(false);
        canvas.addInputEventListener(new MouseInputHandler());
        canvas.getRoot().getDefaultInputManager().setKeyboardFocus(selectionEventHandler);
        
        InputMap inputMap = canvas.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke('b'), AddContentBoxAction.class);
        
        canvas.getActionMap().put(AddContentBoxAction.class, new AddContentBoxAction(report, pageNode));
    }
    
    private class MouseInputHandler implements PInputEventListener {

        public void processEvent(PInputEvent event, int type) {
            logger.debug("Processing event: " + event);
        }
        
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Report report = new Report();
                ReportLayoutPanel p = new ReportLayoutPanel(report);
                JFrame f = new JFrame("Report layout");
                f.setContentPane(p.canvas);
                f.pack();
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                
                p.canvas.getCamera().animateViewToCenterBounds(p.pageNode.getFullBounds(), true, 750);
            }
        });
    }
}
