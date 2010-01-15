/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.wabit.enterprise.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionImpl;

import com.kitfox.svg.app.beans.SVGIcon;
import java.awt.Rectangle;

public class Watermarker {

	private final static Logger logger = Logger.getLogger(Watermarker.class);
	private WabitSession session = null;
	private String watermarkMessage = ServerInfoProvider.defaultWatermarkMessage;

	public Watermarker() {
		session = null;
	}
	
	public Watermarker(WabitSession session) {
		this.session = session;
	}

	/**
	 * Manually sets the watermark message.
	 * @param newMessage Watermark message to display
	 */
	public void setWatermarkMessage(String newMessage) {
		this.watermarkMessage = newMessage;	
	}
	
	/**
	 * Will verify if the server is licensed before watermarking.
	 * @param g Graphics object to insert the watermark into
	 * @param size The actual size of the watermarked area
	 * @throws IllegalStateException if no session was provided at construction time
	 */
	public void maybeWatermark(Graphics g, Rectangle size) {
		if (session == null) {
			throw new IllegalStateException("Session is null. Please call constructor with session as parameter.");
		}
		if (session.isEnterpriseServerSession()) {
			try {
				boolean licensed =
					ServerInfoProvider.isServerLicensed(
						((WabitSwingSessionImpl)session).getEnterpriseServerInfos());
				if (!licensed) {
					this.watermark(g, size);
				}
			} catch (Exception e) {
				logger.warn(e);
			}
		}
	}
	
	/**
	 * Watermarks a graphics object
	 * @param g Graphics object to insert the watermark into
	 * @param size The actual size of the watermarked area
	 */
	public void watermark(Graphics g, Rectangle size) {
        
		if (session != null &&
				session.isEnterpriseServerSession()) {
			try {
				watermarkMessage =
					ServerInfoProvider.getWatermarkMessage(
						((WabitSwingSessionImpl)session).getEnterpriseServerInfos());
			} catch (Exception e) {
				logger.warn(e);
			}
		}
	
		FontMetrics fm = g.getFontMetrics();
        
        JLabel label = new JLabel();
        
        int textWidth = fm.stringWidth(watermarkMessage);
        int scaleWidth = (int) size.getWidth() - 50;
        Font font = fm.getFont();
        
        font = font.deriveFont((float) (font.getSize()*((double)scaleWidth)/textWidth));
        label.setFont(font);
        label.setForeground(Color.decode("0xFF6600"));
        label.setText(watermarkMessage);
        label.setBounds(new java.awt.Rectangle(0, 20, (int) size.getWidth(), fm.getHeight() + 20));
        label.setHorizontalAlignment(SwingConstants.HORIZONTAL);
        
        label.paint(g);
        
        URI resource = null;
        try {
        	resource = this.getClass().getClassLoader().getResource("ca/sqlpower/wabit/enterprise/client/notforproduction.svg").toURI();
        } catch (URISyntaxException e) {
        	throw new RuntimeException(e);
        }
        
        SVGIcon logo = new SVGIcon();
        logo.setSvgURI(resource);
        
        int scaleSize = (int) Math.min(size.getWidth(), size.getHeight());
        int x = (int) (size.getWidth() - scaleSize)/2;
        int y = (int) (size.getHeight() - scaleSize)/2;
        logo.setPreferredSize(new Dimension(scaleSize, scaleSize));
        logo.setScaleToFit(true);
        logo.paintIcon(null, g, x, y);
	}

}
