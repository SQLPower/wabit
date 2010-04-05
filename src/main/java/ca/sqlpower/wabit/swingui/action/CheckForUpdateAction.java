/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.params.BasicHttpParams;
import org.apache.log4j.Logger;

import ca.sqlpower.util.Version;
import ca.sqlpower.wabit.WabitVersion;

public class CheckForUpdateAction extends AbstractAction {

	private final static Logger logger = 
			Logger.getLogger(CheckForUpdateAction.class);
	
	private final static String UPDATER_URL = 
			"http://wabit.googlecode.com/svn/trunk/doc/currentVersion.xml";
	
	private static final String errMsg = 
			"Error attempting to launch web browser";

	private final JFrame owner;
	
	private final static class DownloadAction extends AbstractAction {

		private final String downloadUrl;
		private final JDialog dialog;

		public DownloadAction(JDialog dialog, String downloadUrl) {
			super("Download now");
			this.dialog = dialog;
			this.downloadUrl = downloadUrl;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent event) {
			
			/*
			 * Thanks to http://sourceforge.net/projects/browserlaunch2/
			 * for the browser detection code.
			 */
			
			String osName = System.getProperty("os.name");
			
			try {
				
				if (osName.startsWith("Mac OS")) {

					Class fileMgr = Class.forName("com.apple.eio.FileManager");
					
					Method openURL = fileMgr.getDeclaredMethod(
							"openURL",
							new Class[] {String.class});
					
					openURL.invoke(
							null, 
							new Object[] {downloadUrl});
				}

				else if (osName.startsWith("Windows")) {
					
					Runtime.getRuntime().exec(
							"rundll32 url.dll,FileProtocolHandler " + downloadUrl);
					
				} else {
					
					String[] browsers = {
							"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
					
					String browser = null;
					
					for (int count = 0; count < browsers.length && browser == null; count++) {
						
						if (Runtime.getRuntime().exec(
								new String[] {"which", browsers[count]}).waitFor() == 0) {
							
							browser = browsers[count];
							
						}
					}
					
					if (browser == null) {
						
						throw new Exception("Could not find web browser");
					
					} else {
						
						Runtime.getRuntime().exec(new String[] {browser, downloadUrl});
						
					}
				}
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
			} finally {
				dialog.dispose();
			}
		}
	}
	
	public CheckForUpdateAction(JFrame owner) {
		this.owner = owner;
	}
	
	public CheckForUpdateAction(String caption, JFrame owner) {
		super(caption);
		this.owner = owner;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		checkForUpdate(owner, false);
	}
	
	public static void checkForUpdate(JFrame owner) {
		checkForUpdate(owner, true);
	}
	
	public static void checkForUpdate(JFrame owner, boolean silent) {
		
		try {
			
			GetMethod request = new GetMethod(UPDATER_URL);
			BasicHttpParams params = new BasicHttpParams();
			params.setIntParameter("http.socket.timeout", new Integer(1000));
			HttpClient connection = new HttpClient();
			connection.executeMethod(request);

			Properties results = new Properties();
			results.loadFromXML(
					new ByteArrayInputStream(
							request.getResponseBody()));
			
			Version currentVersion = new Version(
					results.getProperty("currentVersion"));
			
			if (currentVersion.compareTo(WabitVersion.VERSION) > 0) {
				
				final JDialog dialog = new JDialog(owner, "New SQL Power Wabit version available!");
				dialog.setAlwaysOnTop(true);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				
				JPanel panel = new JPanel(new MigLayout("fill", "[fill]", "[shrink][fill][shrink]"));
				dialog.setContentPane(panel);
				
				panel.add(
						new JLabel("A new version of SQL Power Wabit is available for download."),
						"span, wrap, gapbottom 10px");
				
				panel.add(new JLabel(results.getProperty("releaseNotes")), "wrap, span");
				
				Box buttons = Box.createHorizontalBox();
				JButton downloadButton = new JButton(new DownloadAction(dialog, results.getProperty("downloadUrl")));
				JButton cancelButton = new JButton(new AbstractAction("No thanks.") {
					@Override
					public void actionPerformed(ActionEvent e) {
						dialog.dispose();
					}
				});
				buttons.add(downloadButton);
				buttons.add(cancelButton);
				panel.add(buttons, "span, wrap, align right");
				
				dialog.pack();
				dialog.setVisible(true);
			} else if (!silent) {
				JOptionPane.showMessageDialog(
						owner, 
						"No updates available.", 
						"Update checker", 
						JOptionPane.INFORMATION_MESSAGE);
			}
			
		} catch (Exception ex) {
			logger.warn("Failed to check for update.", ex);
		}
		
	}

	public static void main(String[] args) {
		checkForUpdate(null);
	}
}
