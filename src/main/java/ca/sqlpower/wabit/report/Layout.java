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

package ca.sqlpower.wabit.report;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPSimpleVariableResolver;
import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.object.SPVariableResolver;
import ca.sqlpower.object.SPVariableResolverProvider;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitVersion;

public abstract class Layout extends AbstractWabitObject implements Pageable, Printable, SPVariableResolverProvider {
    private static final Logger logger = Logger.getLogger(Report.class);

	public static final String PROPERTY_ZOOM = "zoomLevel";
	
	/**
	 * A Boolean property that when true defines the print method calls to be
	 * used to count the pages in the document. The PAGE_COUNT property only
	 * increases when it is not counting pages.
	 */
	private static final String COUNTING_PAGES = "counting_pages";
	
	public Layout(String uuid) {
		super(uuid);
		PageFormat pageFormat = new PageFormat();
        pageFormat.setOrientation(PageFormat.LANDSCAPE);
        page = new Page("Default Page", pageFormat);
        page.setParent(this);
	}
	
	public Layout(Page page) {
		this(null, page);
	}
	
	public Layout(String uuid, Page page) {
		super(uuid);
		this.page = page;
		page.setParent(this);
	}
	
	@Override
	public void setParent(SPObject parent) {
		super.setParent(parent);
		this.variables = new SPSimpleVariableResolver(this, this.uuid, getNameForVariables(this.getName()));
		this.updateBuiltinVariables();
	}
	
	public void setUUID(String uuid) {
		super.setUUID(uuid);
		if (this.variables != null) {
			this.variables.setNamespace(uuid);
		}
	}
	
	@Override
	public void setName(String name) {
		super.setName(name);
		if (this.variables != null) {
			this.variables.setUserFriendlyName(getNameForVariables(name));
		}
	}
	
	/**
	 * A property that defines which page is currently being printed.
	 */
	public static final String PAGE_NUMBER = "page_number";
    
    /**
     * The page size and margin info.
     * <p>
     * TODO: In future versions, a Layout can have many pages so you can accomplish
     * left and right masters, cover pages, and so on. For now, a Layout can only
     * have one arrangement of page content, and this is it.
     */
    protected Page page;
    
    private int pageCount = Integer.MAX_VALUE;
    
    /**
     * This is the zoom level for the views of this layout.
     */
    private int zoomLevel;
    
    protected SPSimpleVariableResolver variables;
    
    protected SPVariableHelper variableHelper = new SPVariableHelper(this);
    
    /**
     * This will define if the layout is currently printing, which is also done by
     * exporting it to a PDF, as the print method is not safe for two threads to
     * print the layout at the same time.
     */
    private AtomicBoolean currentlyPrinting = new AtomicBoolean(false);

    protected void updateBuiltinVariables() {
    	if (this.variables != null) {
    		// Make sure we operate under the right namespace
    		this.variables.setNamespace(this.getUUID());
    		this.variables.update("now", new Date());
    		this.variables.update("system_user", System.getProperty("user.name"));
    		this.variables.update("wabit_version", WabitVersion.VERSION);
    		this.variables.update(PAGE_NUMBER, 0);
    		this.variables.update("page_count", 0);
    	}
    }
    
    public Page getPage() {
        return page;
    }
    
    public void setPage(Page page) {
    	fireChildRemoved(Page.class, this.page, 0);
    	this.page = page;
    	fireChildAdded(Page.class, page, 0);
    	page.setParent(this);
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        if (childType == Page.class) {
            return 0;
        } else {
            throw new IllegalArgumentException("Layouts don't have children of type " + childType);
        }
    }

    public List<WabitObject> getChildren() {
        return Collections.singletonList((WabitObject)page);
    }
    
    @Override
    protected boolean removeChildImpl(SPObject child) {
        throw new IllegalStateException("Cannot currently remove the page from a layout. Need" +
        		" to implement multi-paging for this functionality.");
    }
    
    @Override
    protected void addChildImpl(SPObject child, int index) {
        throw new IllegalStateException("Cannot currently set the page from a layout. Need" +
                " to implement multi-paging for this functionality.");
    }
    
    public boolean allowsChildren() {
    	return true;
    }

    /**
     * Prints a page of this report to the given graphics context. Before printing
     * the currentlyPrinting flag should be set.
     * 
     * @param pageIndex the zero-based page number to print
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                
        if (pageIndex == 0) {
            for (ContentBox cb : page.getContentBoxes()) {
            	if (cb.getContentRenderer() != null) {
            		cb.getContentRenderer().resetToFirstPage();
            	}
            }
        }
        logger.debug("Page count is " + pageCount + " looking or page indexed " + pageIndex);
        if (pageIndex >= pageCount) {
            return Printable.NO_SUCH_PAGE;
        }
        
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setColor(Color.BLACK);
        if (this.variables != null) {
        	if (!((Boolean) this.variables.resolve(COUNTING_PAGES, false))) {
        		this.variables.update(PAGE_NUMBER, pageIndex + 1);
        	}
        }
        boolean needMorePages = false;
        for (ContentBox cb : page.getContentBoxes()) {
            logger.debug("(Page " + (pageIndex + 1) + ") rendering content box: "+ cb);
            ReportContentRenderer r = cb.getContentRenderer();
            if (r == null) {
                logger.debug("Skipping content box with no renderer: " + cb);
                continue;
            }
            Graphics2D contentGraphics = (Graphics2D) g2.create(
                    (int) cb.getX(), (int) cb.getY(),
                    (int) cb.getWidth(), (int) cb.getHeight());
            needMorePages |= r.renderReportContent(contentGraphics, (int)cb.getWidth(), (int)cb.getHeight(), 1.0, pageIndex, true, this.variableHelper);
            contentGraphics.dispose();
        }
        if (!needMorePages) {
            pageCount = pageIndex + 1;
        }
        return Printable.PAGE_EXISTS;
    }

    /**
     * Before getting the page count the currentlyPrinting flag should be set.
     */
    public int getNumberOfPages() {
    	try {
    		countPages();
    		if (this.variables != null) {
    			this.variables.update("page_count", pageCount);
    		}
    		return pageCount;
    	} catch (PrinterException ex) {
    		throw new RuntimeException("Print exception occured while counting pages", ex);
    	}
    }

    public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
        return page.getPageFormat();
    }

    public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
        return this;
    }

    private int countPages() throws PrinterException {
    	boolean done = false;
    	int pageNum = 0;
    	pageCount = Integer.MAX_VALUE;
    	BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    	Graphics g = dummyImage.getGraphics();
    	try {
    		if (this.variables != null) {
    			this.variables.update(COUNTING_PAGES, true);
    		}
	    	while (!done) {
	    		int result = print(g, getPageFormat(pageNum), pageNum);
	    		if (result == Printable.NO_SUCH_PAGE) break;
	    		pageNum++;
	    	}
    	} finally {
    		if (this.variables != null) {
    			this.variables.update(COUNTING_PAGES, false);
    		}
    		g.dispose();
    	}
    	return pageNum;
    }

	public void setZoomLevel(int zoomLevel) {
		firePropertyChange(PROPERTY_ZOOM, this.zoomLevel, zoomLevel);
		this.zoomLevel = zoomLevel;
	}

	public int getZoomLevel() {
		return zoomLevel;
	}
	
	public boolean compareAndSetCurrentlyPrinting(boolean expected, boolean updateValue) {
		return currentlyPrinting.compareAndSet(expected, updateValue);
	}

	public boolean isCurrentlyPrinting() {
		return currentlyPrinting.get();
	}

    public List<WabitObject> getDependencies() {
        return Collections.emptyList();
    }
    
    public void removeDependency(SPObject dependency) {
        page.removeDependency(dependency);
    }

    @Override
    public String toString() {
    	return getName();
    }
    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	List<Class<? extends SPObject>> types = new ArrayList<Class<? extends SPObject>>();
    	types.add(Page.class);
    	return types;
    }

    public SPVariableResolver getVariableResolver() {
    	return this.variables;
    }
    
    private String getNameForVariables(String baseName) {
    	if (this instanceof Report) {
    		return "Report - " + baseName;
    	} else if (this instanceof Template) {
    		return "Template - " + baseName;
    	} else return baseName;
    }
}
