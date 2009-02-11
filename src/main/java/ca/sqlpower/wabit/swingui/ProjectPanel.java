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

package ca.sqlpower.wabit.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.swingui.db.DefaultDataSourceDialogFactory;
import ca.sqlpower.swingui.db.DefaultDataSourceTypeDialogFactory;
import ca.sqlpower.util.BrowserUtil;
import ca.sqlpower.wabit.JDBCDataSource;
import ca.sqlpower.wabit.Query;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitVersion;
import ca.sqlpower.wabit.query.QueryCache;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This panel will display information about the project. It will
 * also allow the user to add and remove data sources.
 */
public class ProjectPanel implements WabitPanel {
	
	private static final ImageIcon SELECT_START_ICON = new ImageIcon(ProjectPanel.class.getClassLoader().getResource("icons/wunWabit_selected.png"));
	private static final ImageIcon OVER_START_ICON = new ImageIcon(ProjectPanel.class.getClassLoader().getResource("icons/wunWabit_over.png"));
	private static final ImageIcon DOWN_START_ICON = new ImageIcon(ProjectPanel.class.getClassLoader().getResource("icons/wunWabit_down.png"));
	private static final ImageIcon UP_START_ICON = new ImageIcon(ProjectPanel.class.getClassLoader().getResource("icons/wunWabit_up.png"));
	
    private static class LogoLayout implements LayoutManager {

        private int textStartY = 130;
        private int textStartX = 400;
        
        public static JPanel generateLogoPanel() {
        	JPanel panel = new JPanel(new LogoLayout());
        	
        	JLabel bgLabel = new JLabel(new ImageIcon(ProjectPanel.class.getClassLoader().getResource("icons/wabit_header_app_bkgd.png")));
        	JLabel welcomeLabel = new JLabel(new ImageIcon(ProjectPanel.class.getClassLoader().getResource("icons/wabit_header_app_welcome.png")));
        	JLabel wabitLabel = new JLabel(new ImageIcon(ProjectPanel.class.getClassLoader().getResource("icons/wabit_header_app_wabit.png")));
        	JLabel sqlpLabel = new JLabel(new ImageIcon(ProjectPanel.class.getClassLoader().getResource("icons/wabit_header_app_sqlp.png")));
        	JLabel versionLabel = new JLabel("" + WabitVersion.VERSION);
        	versionLabel.setForeground(new Color(0x999999));
        	
        	panel.add(welcomeLabel);
        	panel.add(wabitLabel);
        	panel.add(sqlpLabel);
        	panel.add(versionLabel);
        	panel.add(bgLabel);
			return panel;
        }
        
        private LogoLayout() {
        	//Do nothing for init.
        }
        
        public void layoutContainer(Container parent) {
        	JLabel bgLabel = (JLabel) parent.getComponent(4);
        	JLabel welcomeLabel = (JLabel) parent.getComponent(0);
        	JLabel wabitLabel = (JLabel) parent.getComponent(1);
        	JLabel sqlpLabel = (JLabel) parent.getComponent(2);
        	JLabel versionLabel = (JLabel) parent.getComponent(3);
        	
        	int headerStartX = (parent.getWidth() - 800) / 2;
        	
            bgLabel.setBounds(0, 0, parent.getWidth(), parent.getHeight());
            welcomeLabel.setBounds(headerStartX, 0, welcomeLabel.getPreferredSize().width, welcomeLabel.getPreferredSize().height);
            wabitLabel.setBounds(welcomeLabel.getX() + welcomeLabel.getPreferredSize().width, 0, wabitLabel.getPreferredSize().width, wabitLabel.getPreferredSize().height);
            sqlpLabel.setBounds(headerStartX + 800 - sqlpLabel.getPreferredSize().width, 0, sqlpLabel.getPreferredSize().width, sqlpLabel.getPreferredSize().height);
            versionLabel.setBounds(wabitLabel.getX() + textStartX, wabitLabel.getY() + textStartY, versionLabel.getPreferredSize().width, versionLabel.getPreferredSize().height);
        }

        public Dimension minimumLayoutSize(Container parent) {
        	JLabel welcomeLabel = (JLabel) parent.getComponent(0);
        	JLabel wabitLabel = (JLabel) parent.getComponent(1);
        	JLabel sqlpLabel = (JLabel) parent.getComponent(2);
        	
            return new Dimension(welcomeLabel.getWidth() + wabitLabel.getWidth() + sqlpLabel.getWidth(),
            		Math.max(Math.max(welcomeLabel.getHeight(), wabitLabel.getHeight()), sqlpLabel.getHeight()));
        }

        public Dimension preferredLayoutSize(Container parent) {
            return minimumLayoutSize(parent);
        }

        public void removeLayoutComponent(Component comp) {
            // no-op
        }
        
        public void addLayoutComponent(String name, Component comp) {
            // no-op
        }
    }

	/**
	 * The main panel of this project.
	 */
	private final JPanel panel;
	private final WabitSwingSession session;
	private DatabaseConnectionManager dbConnectionManager;
	
	public ProjectPanel(WabitSwingSession session) {
		this.session = session;
		panel = new JPanel(new BorderLayout());
		buildUI();
	}
	
	private void buildUI() {
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow, fill:pref, pref:grow", "pref, pref, pref"));
		CellConstraints cc = new CellConstraints();
		JPanel logoPanel = LogoLayout.generateLogoPanel();
		builder.add(logoPanel, cc.xyw(1, 1, 3));
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        final JEditorPane htmlComponent = new JEditorPane();
        htmlComponent.setEditorKit(htmlKit);
        htmlComponent.setText(
				"<html><br><br>" +
				"<p>Creating a Report in Wabit involves three steps:" +
				"<ol>" +
				" <li> Add a Data Source to your Project" +
				" <li> Formulate a Query with the help of the Query Builder" +
				" <li> Create a Page Layout for your Query" +
				"</ol>" +
				"<p>Your Page Layout can be printed directly or saved to a PDF file." +
				"<p>To add a Data Source to your project, choose one from the list below and press the <i>Start</i> button." +
				"<br>" +
				"<p>Please visit our <a href=\"" + SPSUtils.FORUM_URL + "\">support forum</a>   if you have any questions, comments, suggestions, or if you just need a friend.");
        builder.add(htmlComponent, cc.xy(2, 2));
		htmlComponent.setEditable(false);
		htmlComponent.setBackground(null);
		
        /** Jump to the URL (in the user's configured browser)
         * when a link is clicked.
         */
        htmlComponent.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    URL url = evt.getURL();
                    try {
                        BrowserUtil.launch(url.toString());
                    } catch (IOException e1) {
                        throw new RuntimeException("Unexpected error in launch", e1); //$NON-NLS-1$
                    }
                }
            }
        });

        List<JComponent> componentList = new ArrayList<JComponent>();
        DefaultFormBuilder startPanel = new DefaultFormBuilder(new FormLayout("fill:pref", "pref, pref"));
        final JLabel startImageLabel = new JLabel(UP_START_ICON);
        startImageLabel.setFocusable(true);
        startPanel.add(startImageLabel);
        startImageLabel.addMouseListener(new MouseListener() {
        	boolean inside = false;
        	boolean pressed = false;
			public void mouseReleased(MouseEvent e) {
				pressed = false;
				if (startImageLabel.isFocusOwner()) {
					startImageLabel.setIcon(SELECT_START_ICON);
					addDSToProject();
				} else if (inside) {
					startImageLabel.setIcon(OVER_START_ICON);
					addDSToProject();
				} else {
					startImageLabel.setIcon(UP_START_ICON);
				}
			}
		
			public void mousePressed(MouseEvent e) {
				startImageLabel.requestFocusInWindow();
				pressed = true;
				startImageLabel.setIcon(DOWN_START_ICON);
			}
		
			public void mouseExited(MouseEvent e) {
				inside = false;
				if (startImageLabel.isFocusOwner()) {
					startImageLabel.setIcon(SELECT_START_ICON);
				} else if (pressed) {
					startImageLabel.setIcon(DOWN_START_ICON);
				} else {
					startImageLabel.setIcon(UP_START_ICON);
				}
			}
		
			public void mouseEntered(MouseEvent e) {
				inside = true;
				if (startImageLabel.isFocusOwner()) {
					startImageLabel.setIcon(SELECT_START_ICON);
				} else if (pressed) {
					startImageLabel.setIcon(DOWN_START_ICON);
				} else {
					startImageLabel.setIcon(OVER_START_ICON);
				}
			}
		
			public void mouseClicked(MouseEvent e) {
				// do nothing
			}
		});
        startImageLabel.addFocusListener(new FocusListener() {
		
			public void focusLost(FocusEvent e) {
				startImageLabel.setIcon(UP_START_ICON);
			}
		
			public void focusGained(FocusEvent e) {
				//do nothing.
			}
		});
        
        JLabel startTextLabel = new JLabel("Start");
        startTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
        startPanel.nextLine();
		startPanel.add(startTextLabel);
        componentList.add(startPanel.getPanel());
		dbConnectionManager = new DatabaseConnectionManager(session.getContext().getDataSources(), 
				new DefaultDataSourceDialogFactory(), 
				new DefaultDataSourceTypeDialogFactory(session.getContext().getDataSources()),
				new ArrayList<Action>(), componentList, session.getFrame(), false);
		builder.add(dbConnectionManager.getPanel(), cc.xy(2, 3));
		JPanel builderPanel = builder.getPanel();
		panel.add(builderPanel, BorderLayout.CENTER);
		
		logoPanel.getLayout().layoutContainer(logoPanel);
	}
	
	/**
	 * This method is used in the DB connection manager to add the selected db
	 * to the project.
	 */
	private void addDSToProject() {
		SPDataSource ds = dbConnectionManager.getSelectedConnection();
		if (ds == null) {
			return;
		}
		try {
			ds.createConnection();
		} catch (SQLException e) {
			SPSUtils.showExceptionDialogNoReport(session.getFrame(), "Could not create a connection to " + ds.getName() + ". Please check the connection information.", e);
			return;
		}
		boolean isDSAlreadyAdded = false;
		for (WabitDataSource wds : session.getProject().getDataSources()) {
			if (wds instanceof JDBCDataSource) {
				JDBCDataSource jdbc = (JDBCDataSource) wds;
				if (jdbc.getSPDataSource() == ds) {
					isDSAlreadyAdded = true;
				}
			}
		}
		if (!isDSAlreadyAdded) {
			session.getProject().addDataSource(ds);
		}
		Query query = new QueryCache();
		query.setName("New " + ds.getName() + " query");
		session.getProject().addQuery(query);
		query.setDataSource(ds);
		session.setEditorPanel(query);
	}
	
	public boolean applyChanges() {
		return true;
	}

	public void discardChanges() {
		//no changes to discard
	}

	public JComponent getPanel() {
		return new JScrollPane(panel);
	}

	public boolean hasUnsavedChanges() {
		return false;
	}

	public void maximizeEditor() {
		//Do nothing, the project panel is always maximized.
	}

}
