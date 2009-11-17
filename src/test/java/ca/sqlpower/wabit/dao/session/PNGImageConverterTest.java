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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.swing.ImageIcon;

import junit.framework.TestCase;

import org.bouncycastle.util.Arrays;

import ca.sqlpower.dao.PersisterUtils;

public class PNGImageConverterTest extends TestCase {
	
	public void testConvertToAndFromImage() throws Exception {
		PNGImageConverter converter = new PNGImageConverter();
		
		//random image for testing
		Image img = new ImageIcon(PNGImageConverter.class.getClassLoader().getResource(
				"icons/execute.png")).getImage();
		ByteArrayOutputStream buffer = PersisterUtils.convertImageToStreamAsPNG(img);
		
		System.out.println("Start buffer " + buffer);
		
		InputStream inputStream = converter.convertToSimpleType(img);
		
		Image newImg = converter.convertToComplexType(inputStream);
		ByteArrayOutputStream newBuffer = PersisterUtils.convertImageToStreamAsPNG(newImg);
		
		System.out.println("End buffer " + newBuffer);
		
		assertTrue(Arrays.areEqual(buffer.toByteArray(), newBuffer.toByteArray()));
		
	}

}
