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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.wabit.enterprise.client.ServerInfoProvider;
import ca.sqlpower.wabit.enterprise.client.Watermarker;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;
import ca.sqlpower.wabit.swingui.WabitSwingSessionImpl;

public class PDFAction extends AbstractAction {

    public static final Icon ICON = new ImageIcon(PageFormatAction.class.getResource("/icons/32x32/pdf.png"));
    
    /**
     * Controls whether or not this action should prompt when the user attempts
     * to overwrite an existing file. The OS X file dialog does this
     * automatically, so this flag causes the overwrite prompt to be suppressed
     * on that platform.
     */
    private static final boolean PROMPT_ON_OVERWRITE = System.getProperty("mrj.version") != null;

    private final Layout layout;
    private final Component dialogOwner;

	private final WabitSwingSession session;
	
	private final JFrame parentFrame;
	
	private Watermarker watermarker = new Watermarker();

    public PDFAction(WabitSwingSession session, Component dialogOwner, Layout layout) {
        super("Create PDF...", ICON);
		this.session = session;
		parentFrame = ((WabitSwingSessionContext) session.getContext()).getFrame();
        putValue(SHORT_DESCRIPTION, "Export report as PDF");
        this.dialogOwner = dialogOwner;
        this.layout = layout;
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            FileDialog fd;
            Window owner;
            if (dialogOwner instanceof Window) {
                owner = (Window) dialogOwner;
            } else {
                owner = SwingUtilities.getWindowAncestor(dialogOwner);
            }
            if (owner instanceof Frame) {
                fd = new FileDialog((Frame) owner, "Save PDF as", FileDialog.SAVE);
            } else {
                fd = new FileDialog((Dialog) owner, "Save PDF as", FileDialog.SAVE);
            }
            File targetFile = null;
            boolean promptAgain;
            do {
                promptAgain = false;
                fd.setVisible(true);
                String dir = fd.getDirectory();
                String fileName = fd.getFile();
                if (fileName == null) return;
                if (!fileName.toLowerCase().endsWith(".pdf")) {
                    fileName += ".pdf";
                }
                targetFile = new File(dir, fileName);
                if (PROMPT_ON_OVERWRITE && targetFile.exists()) {
                    int choice = JOptionPane.showOptionDialog(
                            owner, "The file " + targetFile + " exists.\nDo you want to replace it?",
                            "File exists", -1, JOptionPane.WARNING_MESSAGE, null,
                            new String[] { "Replace", "Cancel" }, "Replace");
                    if (choice == 0) {
                        promptAgain = false;
                    } else if (choice == 1) {
                        promptAgain = true;
                    } else if (choice == -1) {
                        return;
                    } else {
                        throw new RuntimeException("Unrecognized choice: " + choice);
                    }
                }
            } while (promptAgain);

            final JPanel glassPane = new JPanel();
            parentFrame.setGlassPane(glassPane);
            glassPane.setVisible(true);
            glassPane.setFocusable(true);
            glassPane.setOpaque(false);
            glassPane.addFocusListener(new FocusListener() {
			
				public void focusLost(FocusEvent e) {
					if (glassPane.isVisible()) {
						glassPane.requestFocus();
					}
				}
				public void focusGained(FocusEvent e) {
					//Do nothing on focus gained
				}
			});
			
            glassPane.addMouseListener(new MouseListener() {
				public void mouseReleased(MouseEvent e) {
					e.consume();
				}
				public void mousePressed(MouseEvent e) {
					e.consume();			
				}
				public void mouseExited(MouseEvent e) {
					e.consume();			
				}
				public void mouseEntered(MouseEvent e) {
					e.consume();			
				}
				public void mouseClicked(MouseEvent e) {
					e.consume();			
				}
			});
            
            glassPane.addMouseMotionListener(new MouseMotionListener() {
				public void mouseMoved(MouseEvent e) {
					e.consume();
				}
				public void mouseDragged(MouseEvent e) {
					e.consume();
				}
			});
            
            Watermarker watermarkerToApply = null;
            if (this.session.isEnterpriseServerSession()) {
            	SPServerInfo infos = 
            		((WabitSwingSessionImpl)this.session).getEnterpriseServerInfos();
            	if (!ServerInfoProvider.isServerLicensed(infos)) {
            		watermarkerToApply = watermarker;
            		watermarkerToApply.setWatermarkMessage(
            			ServerInfoProvider.getWatermarkMessage(infos));
            	}
            }
            
            final LayoutToPDFWorker pdfWorker = new LayoutToPDFWorker(session, targetFile, layout, dialogOwner, watermarkerToApply);
            
            ProgressMonitor monitor = new ProgressMonitor(dialogOwner, "Exporting PDF...", "", 0, 1);
            monitor.setMillisToPopup(0);
			ProgressWatcher watcher = new ProgressWatcher(monitor, pdfWorker) {
				@Override
				public void actionPerformed(ActionEvent evt) {
					super.actionPerformed(evt);
					if (pdfWorker.isFinished() || pdfWorker.isCancelled()) {
						JPanel glassPane = new JPanel();
						glassPane.setOpaque(false);
						parentFrame.setGlassPane(glassPane);
					}
				}
			};
			watcher.start();
			new Thread(pdfWorker).start();
			
            
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
