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

package ca.sqlpower.wabit.dao;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PersisterUtils {
	
	private PersisterUtils() {
		//cannot instantiate this class as it is just static utility methods.
	}

	/**
	 * Converts an image to an output stream to be persisted in some way.
	 * 
	 * @param img
	 *            The image to convert to an output stream for persisting.
	 * @return An output stream containing an encoding of the image as PNG.
	 */
	public static ByteArrayOutputStream convertImageToStreamAsPNG(Image img) {
		BufferedImage image;
        if (img instanceof BufferedImage) {
            image = (BufferedImage) img;
        } else {
            image = new BufferedImage(img.getWidth(null), 
            		img.getHeight(null), BufferedImage.TYPE_INT_ARGB); 
            final Graphics2D g = image.createGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
        }
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        if (image != null) {
        	try {
				ImageIO.write(image, "PNG", byteStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
        }
        return byteStream;
	}

}
