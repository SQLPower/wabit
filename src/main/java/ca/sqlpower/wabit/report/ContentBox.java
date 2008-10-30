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

package ca.sqlpower.wabit.report;

/**
 * Represents a box on the page which has an absolute position and size.
 */
public class ContentBox {

    private int x;
    private int y;
    private int width;
    private int height;

    /**
     * The Page that contains this ContentBox
     */
    private Page page;
    
    /**
     * The renderer that provides visual content for this box.
     */
    private ReportContentRenderer contentRenderer;
    
    
    public void setContentRenderer(ReportContentRenderer contentRenderer) {
        this.contentRenderer = contentRenderer;
    }
    
    public ReportContentRenderer getContentRenderer() {
        return contentRenderer;
    }
    
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    
}
