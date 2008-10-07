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

package ca.sqlpower.wabit;

import java.awt.BorderLayout;
import java.awt.Color;

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
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import ca.sqlpower.swingui.MemoryMonitor;


/**
 * The Main Window for the Wabit Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class WabitSwingSession {
    
    
	/**
	 *  Builds the GUI
	 */
    public void buildUI() {
    	
    	JSplitPane wabitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    	JSplitPane rightViewPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    	JTabbedPane resultTabPane = new JTabbedPane();
    	
    	JTabbedPane editorTabPane = new JTabbedPane();
    	JPanel playPen = new JPanel();
    	playPen.setBackground(new Color(255, 255, 255));
    	JTextArea queryTextArea = new JTextArea();
    	editorTabPane.add(new JScrollPane(playPen),"PlayPen");
    	editorTabPane.add(new JScrollPane(queryTextArea),"Query");
    	
    	
    	JTextArea logTextArea = new JTextArea();
    	JPanel tablePane = new JPanel();
    	
    	// this tableLabel currently is a Jlabel, and it will be replaced with a JTable
    	JLabel tableLabel = new JLabel("This is where the table is going to be :) ");
    	tableLabel.setOpaque(true);
    	tableLabel.setBackground(new Color(255, 255, 255));
    	JPanel optionPane = new JPanel();
    	optionPane.setLayout(new BorderLayout());
    	
    	
    	// Created two JCheckBoxes for the option Panel
    	JCheckBox groupingCheckBox = new JCheckBox("Grouping");
    	JCheckBox havingCheckBox = new JCheckBox("Having");
    	Box box = new Box(BoxLayout.Y_AXIS);
    	box.add(groupingCheckBox);
    	box.add(havingCheckBox);
    	optionPane.add(box);
    	
    	// Created the Result and the Log Panels and added them to the TabbedPane
    	tablePane.setLayout(new BorderLayout());
    	tablePane.add(optionPane, BorderLayout.NORTH);
    	tablePane.add(new JScrollPane(tableLabel), BorderLayout.CENTER);
    	
    	resultTabPane.add(new JScrollPane(logTextArea), "Log");
    	resultTabPane.add(tablePane, "result");
    	
    	rightViewPane.add(editorTabPane, JSplitPane.TOP);
    	rightViewPane.add(resultTabPane, JSplitPane.BOTTOM);  	
    	
    	
    	// Demo Tree 
    	DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("Project Tree");
    	JTree projectTree = new JTree(treeNode);
    	
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
		
		JPanel cp = new JPanel(new BorderLayout());
		cp.add(wabitPane, BorderLayout.CENTER);
        cp.add(statusPane, BorderLayout.SOUTH);
        
        
        JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		menuBar.add(fileMenu);
        
        JFrame wabitFrame = new JFrame("Power*Wabit");
        wabitFrame.setJMenuBar(menuBar);
        wabitFrame.getContentPane().add(cp);
        wabitFrame.setSize(800, 500);
        wabitFrame.setLocation(400, 300);
        wabitFrame.setVisible(true);
    }
    
    /**
     * Main
     * 
     */
    public static void  main (String[] args) {
    	System.setProperty("apple.laf.useScreenMenuBar", "true");
        WabitSwingSession wss = new WabitSwingSession();
        wss.buildUI();
    }

}
