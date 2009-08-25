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

package ca.sqlpower.wabit.swingui.action;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.dao.WorkspaceXMLDAO;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContextImpl;

import com.rc.retroweaver.runtime.Collections;

/**
 * This action will save the active workspace in the given context to a user
 * specified file. This class also contains a static method for saving all
 * sessions to user selected files.
 */
public class SaveWorkspaceAsAction extends AbstractAction {
    
    private static final Logger logger = Logger.getLogger(SaveWorkspaceAsAction.class);
    
    private static final ImageIcon SAVE_WABIT_ICON = 
        new ImageIcon(SaveWorkspaceAsAction.class.getClassLoader().getResource("icons/wabit_save.png"));

    public static final String WABIT_FILE_EXTENSION = ".wabit";
    
    private final WabitSwingSessionContext context;

    public SaveWorkspaceAsAction(WabitSwingSessionContext context) {
        super("Save As...", SAVE_WABIT_ICON);
        this.context = context;
    }

    public void actionPerformed(ActionEvent e) {
        save(context, context.getActiveSwingSession());
    }

    /**
     * Saves the workspace in the given session to a user specified file.
     * Returns true if the file was saved. Returns false if the file was not
     * saved or cancelled. If the given session is null false will be returned.
     * 
     * @param context
     *            The context to parent dialogs to and update its recent menu.
     * @param session
     *            The session to save.
     */
    public static boolean save(WabitSwingSessionContext context, WabitSwingSession session) {
        if (session == null) return false;
        
        JFileChooser fc = new JFileChooser(session.getCurrentURIAsFile());
        fc.setDialogTitle("Select the directory to save to.");
        fc.addChoosableFileFilter(SPSUtils.WABIT_FILE_FILTER);
        
        int fcChoice = fc.showSaveDialog(context.getFrame());

        if (fcChoice != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        
        File selectedFile = fc.getSelectedFile();
        selectedFile = saveSessionToFile(context, session, selectedFile);
        context.setStatusMessage("Saved " + session.getWorkspace().getName() + " to " +
                selectedFile.getName());
        
        return true;
    }

    /**
     * This is a helper method for the saving methods in this class.
     * <p>
     * Package private for use in the SaveWorkspaceAction class.
     * 
     * @param context
     *            The context that contains the session to save. Cannot be null.
     * @param session
     *            The session to save. Cannot be null.
     * @param selectedFile
     *            The file to save the session's workspace to. Cannot be null.
     * @return The file the session was saved to. This may be different than the
     *         file given if a Wabit file extension needs to be appended to it.
     */
    @SuppressWarnings("unchecked")
    static File saveSessionToFile(WabitSwingSessionContext context,
            WabitSwingSession session, File selectedFile) {
        int lastIndexOfDecimal = selectedFile.getName().lastIndexOf(".");
        if (lastIndexOfDecimal < 0 || 
                !selectedFile.getName().substring(lastIndexOfDecimal).equals(
                        WABIT_FILE_EXTENSION)) {
            selectedFile = new File(selectedFile.getAbsoluteFile() + WABIT_FILE_EXTENSION);
        }
        try {
            final FileOutputStream out = new FileOutputStream(selectedFile);
            WorkspaceXMLDAO workspaceSaver = new WorkspaceXMLDAO(out, context);
            workspaceSaver.save(Collections.singletonList(session.getWorkspace()));
            out.flush();
            out.close();
        } catch (FileNotFoundException e1) {
            throw new RuntimeException(e1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        session.getWorkspace().setName(selectedFile.getName().replaceAll(".wabit", ""));
        session.setCurrentURI(selectedFile.toURI());
        
        context.putRecentFileName(selectedFile.getAbsolutePath());
        return selectedFile;
    }

    /**
     * This method will save all of the sessions in the context to their files.
     * Before the sessions are saved the user will be prompted with a dialog
     * that displays the workspaces and their target files and allows the user
     * to edit the target file for each session.
     * 
     * @param context
     *            The context whose sessions will be saved to user specified
     *            files. This cannot be null.
     * @return True if the sessions were saved successfully. False otherwise.
     */
    public static boolean saveAllSessions(WabitSwingSessionContext context) {
        SaveAsPrompt prompt = new SaveAsPrompt(context);
        if (prompt.isCancelled()) return false;
        StringBuffer savedStringBuffer = new StringBuffer("Saved");
        for (Map.Entry<WabitSwingSession, URI> entry : prompt.getSessionFiles().entrySet()) {
            try {
                File file = new File(entry.getValue());
                file = saveSessionToFile(context, entry.getKey(), file);
                savedStringBuffer.append(" " + entry.getKey().getWorkspace().getName() 
                        + " to " + file.getName());
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(context.getFrame(), "The workspace " +
                        entry.getKey().getWorkspace().getName() + " could not be saved" +
                        " as the file location " + entry.getValue() + " is invalid. " +
                        "This is likely a server URI and this is not currently handled"
                        , "Invalid File URI", JOptionPane.ERROR_MESSAGE);
            }
        }
        context.setStatusMessage(savedStringBuffer.toString());
        return true;
    }
    
    /**
     * This class builds the UI for prompting to choose a file for each workspace
     * in the session. The file that the user selects can be retrieved from this class
     * after the user has pressed the OK button. The prompt will appear immediately
     * after this class is created.
     * <p>
     * This class uses URIs so it can be extended to saving sessions onto a server.
     */
    private static class SaveAsPrompt {
    	
        /**
         * This map stores each session in the context and the file it will be saved
         * to.
         */
        private final Map<WabitSwingSession, URI> sessionFiles = 
            new HashMap<WabitSwingSession, URI>();
        
        /**
         * If this becomes true then the user has decided to cancel saving.
         */
        private boolean cancelled;

        /**
         * Call this constructor to prompt the user with a modal save dialog that
         * will allow the user to specify a file location for each workspace
         * being saved.
         * 
         * @param context
         *            The context that contains the workspaces that are going to
         *            be saved.
         */
        public SaveAsPrompt(final WabitSwingSessionContext context) {
            for (WabitSession session : context.getSessions()) {
            	WabitSwingSession swingSession = (WabitSwingSession) session;
            	logger.debug("Current URI is " + swingSession.getCurrentURI() + 
            			" example workspace URI is " + WabitSwingSessionContextImpl.EXAMPLE_WORKSPACE_URL);
            	if (swingSession.getCurrentURI() != null &&
            			swingSession.getCurrentURI().toString().contains(
            			WabitSwingSessionContextImpl.EXAMPLE_WORKSPACE_URL)) {
            		sessionFiles.put(swingSession, null);
            	} else {
            		sessionFiles.put(swingSession, 
            				swingSession.getCurrentURI());
            	}
            }
            
            final DataEntryPanel panel = new DataEntryPanel() {
            
                /**
                 * This maps each session to the user editable text field that will
                 * define the URI to save that session to.
                 */
                private final Map<WabitSwingSession, JTextField> sessionURIFields = 
                    new HashMap<WabitSwingSession, JTextField>();
                
                public JComponent getPanel() {
                    JPanel panel = new JPanel(new MigLayout());
                    for (final Map.Entry<WabitSwingSession, URI> entry : sessionFiles.entrySet()) {
                        panel.add(new JLabel(entry.getKey().getWorkspace().getName()));
                        final JTextField sessionURI = new JTextField();
                        sessionURIFields.put(entry.getKey(), sessionURI);
                        if (entry.getValue() != null) {
                            sessionURI.setText(entry.getValue().toString());
                        }
                        sessionURI.setColumns(30); //Arbitrary value to make this pane look nice.
                        panel.add(sessionURI);
                        panel.add(new JButton(new AbstractAction("...") {

                            public void actionPerformed(ActionEvent e) {
                                JFileChooser fc = new JFileChooser(entry.getKey().getCurrentURIAsFile());
                                fc.setDialogTitle("Select the directory to save to.");
                                fc.addChoosableFileFilter(SPSUtils.WABIT_FILE_FILTER);
                                
                                int fcChoice = fc.showSaveDialog(context.getFrame());

                                if (fcChoice == JFileChooser.APPROVE_OPTION) {
                                    sessionURI.setText(fc.getSelectedFile().toURI().toString());
                                }
                                
                            }
                            
                        }), "wrap");
                    }
                    JScrollPane mainPanelScrollPane = new JScrollPane(panel);
                    Dimension scrollPaneSize = new Dimension(
                            (int) mainPanelScrollPane.getPreferredSize().getWidth(),
                            200); //Arbitrary value just to look nice
                    mainPanelScrollPane.setPreferredSize(scrollPaneSize);
                    return mainPanelScrollPane;
                }
                
                public boolean hasUnsavedChanges() {
                    return true;
                }
            
                public void discardChanges() {
                    //do nothing
                }
            
                public boolean applyChanges() {
                    Map<WabitSwingSession, URI> newURIMap = new HashMap<WabitSwingSession, URI>();
                    for (Map.Entry<WabitSwingSession, JTextField> entry : sessionURIFields.entrySet()) {
                        if (entry.getValue().getText().trim().length() == 0) {
                            JOptionPane.showMessageDialog(context.getFrame(), "The location " +
                                    "for the workspace " + entry.getKey().getWorkspace().getName() 
                                    + " is missing.", "Invalid URI",
                                    JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                        try {
                            URI newURI = new URI(entry.getValue().getText());
                            newURIMap.put(entry.getKey(), newURI);
                        } catch (URISyntaxException ex) {
                            JOptionPane.showMessageDialog(context.getFrame(), "The location " +
                                    entry.getValue().getText() + " for the workspace " +
                                    entry.getKey().getWorkspace().getName() + " is not valid.", 
                                    "Invalid URI",
                                    JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }
                    logger.debug("Setting sessions to the following URIs: " + newURIMap);
                    sessionFiles.putAll(newURIMap);
                    return true;
                }
            };
            
            
            JDialog dep = DataEntryPanelBuilder.createDataEntryPanelDialog(panel, 
                    context.getFrame(), "Choose Workspace Files", 
                    "OK", new Callable<Boolean>() {
                    
                        public Boolean call() throws Exception {
                            boolean close = panel.applyChanges();
                            if (close) {
                                cancelled = false;
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }, new Callable<Boolean>() {
                    
                        public Boolean call() throws Exception {
                            cancelled = true;
                            return true;
                        }
                    });
            
            cancelled = true;
            dep.setModal(true);
            dep.pack();
            dep.setVisible(true);
        }
        
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * If the prompt was not cancelled this map will contain a valid URI for
         * each session. If the prompt was cancelled the URIs in this map may
         * not all be valid. There is an entry in this map for each session in
         * the context given in the constructor.
         */
        public Map<WabitSwingSession, URI> getSessionFiles() {
            return sessionFiles;
        }
    }

}
