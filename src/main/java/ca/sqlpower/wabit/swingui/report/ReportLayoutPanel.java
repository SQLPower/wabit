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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Iterator;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.swingui.WabitNode;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.action.CanvasZoomInAction;
import ca.sqlpower.wabit.swingui.action.CanvasZoomOutAction;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;
import edu.umd.cs.piccolox.swing.PScrollPane;

public class ReportLayoutPanel implements DataEntryPanel {

    private static final Logger logger = Logger.getLogger(ReportLayoutPanel.class);
    
    private final JPanel panel;
    private final PCanvas canvas;
    private final Layout report;
    private final PageNode pageNode;
    
    public ReportLayoutPanel(WabitSwingSession session, Layout report) {
        this.report = report;
        canvas = new PCanvas();
        canvas.setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        canvas.setPanEventHandler(null);
        canvas.setBackground(Color.LIGHT_GRAY);
        canvas.setPreferredSize(new Dimension(400,600));
        
        pageNode = new PageNode(session, report.getPage());
        canvas.getLayer().addChild(pageNode);
        PSelectionEventHandler selectionEventHandler = new GuideAwareSelectionEventHandler(pageNode, pageNode);
        canvas.addInputEventListener(selectionEventHandler);
        pageNode.setPickable(false);
        canvas.addInputEventListener(new MouseInputHandler());
        canvas.getRoot().getDefaultInputManager().setKeyboardFocus(selectionEventHandler);
        
        InputMap inputMap = canvas.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke('b'), AddContentBoxAction.class);
        
        canvas.getActionMap().put(AddContentBoxAction.class, new AddContentBoxAction(session, report, pageNode));
        
        JToolBar toolbar = new JToolBar();
        toolbar.add(new PageFormatAction(report.getPage()));
        toolbar.add(new PrintAction(report));
        toolbar.add(new PDFAction(toolbar, report));
        toolbar.addSeparator();
        toolbar.add(new CanvasZoomInAction(canvas));
        toolbar.add(new CanvasZoomOutAction(canvas));
        
        panel = new JPanel(new BorderLayout());
        panel.add(toolbar, BorderLayout.NORTH);
        PScrollPane canvasScrollPane = new PScrollPane(canvas);
		canvasScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		canvasScrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        panel.add(canvasScrollPane, BorderLayout.CENTER);
    }
    
    private class MouseInputHandler implements PInputEventListener {

        public void processEvent(PInputEvent event, int type) {
            logger.debug("Processing event: " + event);
        }
        
    }
    
    /**
     * Frees any resources and references that would not have been freed otherwise (by virtue
     * of this panel being removed from the GUI).
     */
    private void cleanup() {
        recursiveCleanup(pageNode);
    }
    
    /**
     * On every PNode in the tree rooted at node which implements ReportNode, calls cleanup().
     * 
     * @param node
     */
    private void recursiveCleanup(PNode node) {
        Iterator<?> nodeChildrenIterator = node.getChildrenIterator();
        while (nodeChildrenIterator.hasNext()) {
            PNode child = (PNode) nodeChildrenIterator.next();
            recursiveCleanup(child);
        }
        
        if (node instanceof WabitNode) {
            ((WabitNode) node).cleanup();
        }
    }
    
    // ==================== DataEntryPanel implementation ==================

    public boolean applyChanges() {
        cleanup();
        return true;
    }

    public void discardChanges() {
        cleanup();
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        return false;
    }
}
