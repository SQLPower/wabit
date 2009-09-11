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

package ca.sqlpower.wabit.report.chart;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

/**
 * Helper class for making charts look nicer.
 */
public class ChartGradientPainter {

    private static final Logger logger = Logger.getLogger(ChartGradientPainter.class);
    
    private static int TOP_BRIGHTNESS = 0xEE;
    private static int BOTTOM_BRIGHTNESS = 0xFF;

    /**
     * Paints two gradients which, together, entirely fill the given rectangle.
     * 
     * @param g
     *            The graphics to paint into
     * @param bounds
     *            The rectangle to fill with gradients
     * @param baseline
     *            The cutoff point from the top gradient to the bottom gradient.
     *            This is normally set to line up with the X-axis of the chart.
     */
    public static void paintChartGradient(Graphics2D g, Rectangle2D bounds, int baseline) {
        int x = (int) bounds.getX();
        int y = (int) bounds.getY();
        int width = (int) bounds.getWidth();
        int height = (int) bounds.getHeight();

        Object origInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // We can't use a normal GradientPaint because some interaction between
        // GradientPaint, Piccolo, and Apple's CoreGraphics on OS X 10.4 causes
        // a JVM crash. This buffered image scaling should be pretty performant
        // too, and has the added bonus of not crashing.
        BufferedImage img = makeGradientImage(g);
        g.drawImage(img, x, y, width, baseline, 0, 0, img.getWidth(), img.getHeight(), null);
        g.drawImage(img, x, baseline, width, height, 0, 0, img.getWidth(), img.getHeight(), null);

        // some systems (OS X 10.4.11 on PPC) leave this hint null by default, but
        // don't let us set it back to null!
        if (origInterpolation != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, origInterpolation);
        }
    }

    /**
     * Creates a buffered image compatible with the given graphics with a width
     * of 1 and a height exactly enough to have one pixel of each colour between
     * {@link #TOP_BRIGHTNESS} and {@link #BOTTOM_BRIGHTNESS}.
     * 
     * @param g The graphics the generated image should be compatible with.
     * @return A BufferedImage as described above.
     */
    private static BufferedImage makeGradientImage(Graphics2D g) {
        if (BOTTOM_BRIGHTNESS < TOP_BRIGHTNESS) {
            throw new IllegalStateException("This code only handles dark-to-light gradients");
        }
        BufferedImage img = g.getDeviceConfiguration().createCompatibleImage(
                1, BOTTOM_BRIGHTNESS - TOP_BRIGHTNESS + 1, Transparency.OPAQUE);
        int[] gradient = new int[img.getHeight()];
        for (int i = 0; i < img.getHeight(); i++) {
            int b = (TOP_BRIGHTNESS + i) & 0xff;
            int rgb = 0xff000000 | (b << 16) | (b << 8) | b;
            gradient[i] = rgb;
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Set pixel %d to %x", i, gradient[i]));
            }
        }
        img.setRGB(0, 0, 1, img.getHeight(), gradient, 0, 1);
        return img;
    }
}
