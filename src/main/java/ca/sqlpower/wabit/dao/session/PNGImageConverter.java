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

package ca.sqlpower.wabit.dao.session;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

import javax.imageio.ImageIO;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.codec.binary.Base64;

import ca.sqlpower.wabit.dao.PersisterUtils;

/**
 * Converts between an Image and an InputStream.
 */
public class PNGImageConverter implements BidirectionalConverter<InputStream, Image> {

	public Image convertToComplexType(InputStream convertFrom)
			throws ConversionException {
		try {
			StringBuffer buffer = new StringBuffer();
			InputStreamReader reader = new InputStreamReader(convertFrom);
			for (int next = reader.read(); next != -1; next = reader.read()) {
				buffer.append((char) next);
			}
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(buffer.toString().getBytes()));
			return img;
		} catch (Exception e) {
			throw new ConversionException("Cannot convert the given image", e);
		}
	}

	public InputStream convertToSimpleType(Image convertFrom,
			Object... additionalInfo) {
		ByteArrayOutputStream byteStream = PersisterUtils.convertImageToStreamAsPNG(convertFrom);
        return new ByteArrayInputStream(byteStream.toByteArray());
	}

}
