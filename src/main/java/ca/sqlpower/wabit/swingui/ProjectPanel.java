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
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanel;
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
public class ProjectPanel implements DataEntryPanel {
	
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
	
	/**
	 * This action is used in the DB connection manager to add the selected db
	 * to the project.
	 */
	private final AbstractAction addDSToProjectAction = new AbstractAction("Add To Project") {
		public void actionPerformed(ActionEvent e) {
			SPDataSource ds = dbConnectionManager.getSelectedConnection();
			if (ds == null) {
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
	}; 
	
	public ProjectPanel(WabitSwingSession session) {
		this.session = session;
		panel = new JPanel(new BorderLayout());
		buildUI();
	}
	
	private void buildUI() {
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow, fill:pref, pref:grow", "pref, pref, pref"));
		CellConstraints cc = new CellConstraints();
		builder.add(LogoLayout.generateLogoPanel(), cc.xyw(1, 1, 3));
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        final JEditorPane htmlComponent = new JEditorPane();
        htmlComponent.setEditorKit(htmlKit);
        htmlComponent.setText(
				"<html><br><br>" +
				"<p>Creating a report in Wabit involves three steps:" +
				"<ol>" +
				" <li> add a data source to your project" +
				" <li> formulate a query with the help of the query builder" +
				" <li> create a page layout for your query" +
				"</ol>" +
				"<p>Your page layout can be printed directly or saved to a PDF file." +
				"<p>To add a data source to your project, choose one from the list below and press the <i>Add To Project</i> button." +
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
		
		List<Action> actionList = new ArrayList<Action>();
		actionList.add(addDSToProjectAction);
		dbConnectionManager = new DatabaseConnectionManager(session.getContext().getDataSources(), 
				new DefaultDataSourceDialogFactory(), 
				new DefaultDataSourceTypeDialogFactory(session.getContext().getDataSources()),
				actionList, session.getFrame(), false);
		builder.add(dbConnectionManager.getPanel(), cc.xy(2, 3));
		panel.add(builder.getPanel(), BorderLayout.CENTER);
	}
	
	public boolean applyChanges() {
		return true;
	}

	public void discardChanges() {
		//no changes to discard
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return false;
	}

}
