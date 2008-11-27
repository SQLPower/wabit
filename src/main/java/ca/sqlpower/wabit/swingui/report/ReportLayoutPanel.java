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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.CursorManager;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.swingui.WabitNode;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.querypen.MouseState;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;
import edu.umd.cs.piccolox.swing.PScrollPane;

public class ReportLayoutPanel implements DataEntryPanel, MouseState {

    private static final Logger logger = Logger.getLogger(ReportLayoutPanel.class);
    public static final Icon ICON = new ImageIcon(StatusComponent.class.getClassLoader().getResource("icons/shape_square_add.png"));		
    
    private final JPanel panel;
    private final PCanvas canvas;
    private final PageNode pageNode;
    private final Layout report;
    
	/**
	 * The mouse state in this LayoutPanel.
	 */
	private MouseStates mouseState = MouseStates.READY;
	
    /**
     * The cursor manager for this Query pen.
     */
	private final CursorManager cursorManager;
	
	
	private final AbstractAction addContentBoxAction = new AbstractAction("",  ReportLayoutPanel.ICON){
		public void actionPerformed(ActionEvent e) {
			setMouseState(MouseStates.CREATE_BOX);
			cursorManager.placeModeStarted();
		}
	};
	
    public ReportLayoutPanel(WabitSwingSession session, Layout report) {
        this.report = report;
		canvas = new PCanvas();
        canvas.setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        canvas.setPanEventHandler(null);
        canvas.setBackground(Color.LIGHT_GRAY);
        canvas.setPreferredSize(new Dimension(400,600));
        cursorManager = new CursorManager(canvas);
        
        pageNode = new PageNode(session, report.getPage());
        canvas.getLayer().addChild(pageNode);
        PSelectionEventHandler selectionEventHandler = new GuideAwareSelectionEventHandler(pageNode, pageNode);
        canvas.addInputEventListener(selectionEventHandler);
        pageNode.setPickable(false);
        canvas.addInputEventListener(new MouseInputHandler());
        canvas.getRoot().getDefaultInputManager().setKeyboardFocus(selectionEventHandler);
        
        
        AbstractAction cancelBoxCreateAction = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		if (mouseState == MouseStates.CREATE_BOX) {
        			setMouseState(MouseStates.READY);
        			cursorManager.placeModeFinished();
        		}
        	}
        };
		
        canvas.getActionMap().put(addContentBoxAction.getClass(), addContentBoxAction);
		InputMap inputMap = canvas.getInputMap(JComponent.WHEN_FOCUSED);
		inputMap.put(KeyStroke.getKeyStroke('b'), addContentBoxAction.getClass());
		
		canvas.addInputEventListener(new CreateBoxEventHandler(session, this));
        JToolBar toolbar = new JToolBar();
        toolbar.add(new PageFormatAction(report.getPage()));
        toolbar.add(new PrintAction(report));
        toolbar.add(new PDFAction(toolbar, report));
        toolbar.addSeparator();
        JPanel zoomPanel = new JPanel(new BorderLayout());
        zoomPanel.add(new JLabel(new ImageIcon(ReportLayoutPanel.class.getClassLoader().getResource("icons/zoom_out16.png"))), BorderLayout.WEST);
        final int defaultSliderValue = 500;
        final JSlider zoomSlider = new JSlider(JSlider.HORIZONTAL, 1, 1000, defaultSliderValue);
        zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				canvas.getCamera().setViewScale((double)zoomSlider.getValue()/defaultSliderValue);
			}
		});
        zoomSlider.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseReleased(MouseEvent e) {
        		if ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0) {
        			zoomSlider.setValue(defaultSliderValue);
        		}
        	}
		});
        zoomPanel.add(zoomSlider, BorderLayout.CENTER);
        zoomPanel.add(new JLabel(new ImageIcon(StatusComponent.class.getClassLoader().getResource("icons/zoom_in16.png"))), BorderLayout.EAST);
        zoomPanel.setMaximumSize(new Dimension((int)zoomSlider.getPreferredSize().getWidth(), 200));
        toolbar.add(zoomPanel);
        toolbar.addSeparator();
        toolbar.add(addContentBoxAction);
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(toolbar, BorderLayout.NORTH);
        PScrollPane canvasScrollPane = new PScrollPane(canvas);
		canvasScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		canvasScrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        leftPanel.add(canvasScrollPane, BorderLayout.CENTER);
        
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setResizeWeight(1);
        mainSplitPane.add(leftPanel, JSplitPane.LEFT);
        
        final JList queryList = new JList(session.getProject().getQueries().toArray());
        queryList.setCellRenderer(new ListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				return new JLabel(((WabitObject) value).getName());
			}
		});
        mainSplitPane.add(new JScrollPane(queryList), JSplitPane.RIGHT);
                
        panel = new JPanel(new BorderLayout());
        panel.add(mainSplitPane, BorderLayout.CENTER);
        
        panel.getActionMap().put(cancelBoxCreateAction.getClass(), cancelBoxCreateAction);
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelBoxCreateAction.getClass());
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

	public MouseStates getMouseState() {
		return this.mouseState;
	}

	public void setMouseState(MouseStates state) {
		this.mouseState = state;		
	}

	public Layout getReport() {
		return report;
	}

	public PageNode getPageNode() {
		return pageNode;
	}

	public CursorManager getCursorManager() {
		return cursorManager;
	}

	
}
