/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.wabit.swingui;

import java.awt.BorderLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;

import ca.sqlpower.swingui.MemoryMonitor;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.swingui.query.SQLQueryUIComponents;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.swingui.event.DnDTransferable;


/**
 * The Main Window for the Wabit Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class WabitSwingSession implements SwingWorkerRegistry {
    
	private final WabitSessionContext sessionContext;
	
    private SQLQueryUIComponents queryUIComponents;
	private JTree projectTree;
	private JFrame frame;

	/**
	 * Creates a new session 
	 * 
	 * @param context
	 */
	public WabitSwingSession(WabitSessionContext context) {
		sessionContext = context;
	}
	
	/**
	 *  Builds the GUI
	 */
    public void buildUI() {
        frame = new JFrame("Power*Wabit");
        
        // this will be the frame's content pane
		JPanel cp = new JPanel(new BorderLayout());

    	queryUIComponents = new SQLQueryUIComponents(this, sessionContext.getDataSources(), cp);
    	
    	JSplitPane wabitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    	JSplitPane rightViewPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    	JTabbedPane resultTabPane = queryUIComponents.getTableTabPane();
    	
    	JTabbedPane editorTabPane = new JTabbedPane();
    	JPanel playPen = QueryPen.createQueryPen(this);
    	editorTabPane.add(playPen,"PlayPen");
    	editorTabPane.add(new JScrollPane(queryUIComponents.getQueryArea()),"Query");
    	
    	// Created two JCheckBoxes for the option Panel
    	JCheckBox groupingCheckBox = new JCheckBox("Grouping");
    	JCheckBox havingCheckBox = new JCheckBox("Having");
    	Box box = new Box(BoxLayout.X_AXIS);
    	box.add(new JLabel("Database connection:"));
    	box.add(queryUIComponents.getDatabaseComboBox());
    	box.add(Box.createHorizontalGlue());
    	box.add(groupingCheckBox);
    	box.add(havingCheckBox);
    	
    	JPanel topPane = new JPanel(new BorderLayout());
    	topPane.add(box, BorderLayout.SOUTH);
    	topPane.add(editorTabPane, BorderLayout.CENTER);
    	
    	rightViewPane.add(topPane, JSplitPane.TOP);
    	rightViewPane.add(resultTabPane, JSplitPane.BOTTOM);  	
    	
    	
    	// Demo Tree 
    	projectTree = new JTree();
    	DragSource ds = new DragSource();
		ds.createDefaultDragGestureRecognizer(projectTree, DnDConstants.ACTION_COPY, new DragGestureListener() {
			
			public void dragGestureRecognized(DragGestureEvent dge) {
				dge.getDragSource().startDrag(dge, null, new DnDTransferable(projectTree.getSelectionPath().getLastPathComponent()), new DragSourceListener() {
					public void dropActionChanged(DragSourceDragEvent dsde) {
					}
					public void dragOver(DragSourceDragEvent dsde) {
					}
					public void dragExit(DragSourceEvent dse) {
					}
					public void dragEnter(DragSourceDragEvent dsde) {
					}
					public void dragDropEnd(DragSourceDropEvent dsde) {
					}
				});
			}
		});
    	
        wabitPane.add(new JScrollPane(projectTree), JSplitPane.LEFT);
        wabitPane.add(rightViewPane, JSplitPane.RIGHT);
        
        //the current status label will be replaced by the Session Messages once it is implemented
        JPanel statusPane = new JPanel(new BorderLayout());
        statusPane.add(new JLabel("Status Message here"), BorderLayout.CENTER);
		
		MemoryMonitor memoryMonitor = new MemoryMonitor();
		memoryMonitor.start();
		JLabel memoryLabel = memoryMonitor.getLabel();
		memoryLabel.setBorder(new EmptyBorder(0, 20, 0, 20));
		statusPane.add(memoryLabel, BorderLayout.EAST);
		
		cp.add(wabitPane, BorderLayout.CENTER);
        cp.add(statusPane, BorderLayout.SOUTH);
        
        JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		menuBar.add(fileMenu);
        
		frame.setJMenuBar(menuBar);
        frame.setContentPane(cp);
        frame.setSize(800, 500);
        frame.setLocation(400, 300);
        frame.setVisible(true);
        frame.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {
			}
			public void windowIconified(WindowEvent e) {
			}
			public void windowDeiconified(WindowEvent e) {
			}
			public void windowDeactivated(WindowEvent e) {
			}
			public void windowClosing(WindowEvent e) {
				frame.dispose();
			}
			public void windowClosed(WindowEvent e) {
			}
			public void windowActivated(WindowEvent e) {
			}
		});
    }
    
    public JTree getTree() {
    	return projectTree;
    }
    
    /**
     * Launches the Wabit application by loading the configuration and
     * displaying the GUI.
     * 
     * @throws Exception if startup fails
     */
    public static void  main (String[] args) throws Exception {
    	System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Wabit");
    	System.setProperty("apple.laf.useScreenMenuBar", "true");
    	WabitSessionContext context = new WabitSessionContext();
        WabitSwingSession wss = new WabitSwingSession(context);
        wss.buildUI();
    }

    /* docs inherited from interface */
	public void registerSwingWorker(SPSwingWorker worker) {
		// TODO Auto-generated method stub
		
	}

    /* docs inherited from interface */
	public void removeSwingWorker(SPSwingWorker worker) {
		// TODO Auto-generated method stub
		
	}

}
