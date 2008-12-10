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

package ca.sqlpower.wabit.dao;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.wabit.Query;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.query.Container;
import ca.sqlpower.wabit.query.Item;
import ca.sqlpower.wabit.query.QueryCache;
import ca.sqlpower.wabit.query.SQLJoin;
import ca.sqlpower.wabit.query.SQLObjectItem;
import ca.sqlpower.wabit.query.StringItem;
import ca.sqlpower.wabit.query.TableContainer;
import ca.sqlpower.wabit.query.QueryCache.OrderByArgument;
import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.DataType;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.HorizontalAlignment;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.VerticalAlignment;
import ca.sqlpower.wabit.report.Guide.Axis;
import ca.sqlpower.wabit.report.ResultSetRenderer.BorderStyles;

import com.sun.mail.util.BASE64DecoderStream;

/**
 * This will be used with a parser to load a saved project from a file.
 */
public class ProjectSAXHandler extends DefaultHandler {
	
	private static final Logger logger = Logger.getLogger(ProjectSAXHandler.class);
	
	/**
	 * This list will store all of the sessions loaded by this SAX handler.
	 */
	private final List<WabitSession> sessions;

	/**
	 * This will track the tags and depth of the XML tags.
	 */
	private final Stack<String> xmlContext = new Stack<String>();
	
	/**
	 * This is the session that the project will be loaded into. This session
	 * is also the current one being loaded into by this SAX handler.
	 */
	private WabitSession session;
	
	/**
	 * This is the current query being loaded in from the file.
	 */
	private QueryCache query;
	
	/**
	 * This maps all of the currently loaded Items with their UUIDs. This
	 * will let the loaded elements of a query be able to hook up to the correct
	 * items. This map will keep the item values throughout loading to allow access
	 * to all items throughout the file.
	 */
	private final Map<String, Item> uuidToItemMap = new HashMap<String, Item>();
	
	/**
	 * This is the current container being loaded in by this SAX handler.
	 */
	private Container container;
	
	/**
	 * This list of items are the current items being loaded into the current
	 * container being loaded.
	 */
	private List<SQLObjectItem> containerItems;
	
	/**
	 * This context will store the session created by this SAX handler.
	 */
	private final WabitSessionContext context;

	/**
	 * This layout stores the current layout being loaded by this SAX handler.
	 */
	private Layout layout;

	/**
	 * This is the current content box being loaded by this SAX handler.
	 */
	private ContentBox contentBox;

	/**
	 * This is the current result set renderer being loaded by this SAX handler.
	 */
	private ResultSetRenderer rsRenderer;

	private ColumnInfo colInfo;

	/**
	 * The list of column info items we are loading for the current result set
	 * renderer we are loading.
	 */
	private List<ColumnInfo> columnInfoList = new ArrayList<ColumnInfo>();

	/**
	 * This stores the currently loading Image renderer. This will be null if no image
	 * renderer is being loaded.
	 */
	private ImageRenderer imageRenderer;

	private ByteArrayOutputStream byteStream;
	
	public ProjectSAXHandler(WabitSessionContext context) {
		this.context = context;
		sessions = new ArrayList<WabitSession>();
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {

		xmlContext.push(name);

        if (name.equals("wabit")) {
        	//TODO: check version numbers and file formats.
        } else if (name.equals("project")) {
        	session = context.createSession();
        	sessions.add(session);
        	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		
        		if (aname.equals("name")) {
        			session.getProject().setName(aval);
        		} else {
        			logger.warn("Unexpected attribute of <project>: " + aname + "=" + aval);
        		}
        	}
        } else if (name.equals("data-source")) {
        	String dsName = attributes.getValue("name");
        	checkMandatory("name", dsName);
        	SPDataSource ds = context.getDataSources().getDataSource(dsName);
        	if (ds == null) {
        		throw new NullPointerException("The data source with the name " + dsName + " was not found in this context.");
        	}
        	session.getProject().addDataSource(ds);
        } else if (name.equals("query")) {
        	String uuid = attributes.getValue("uuid");
        	checkMandatory("uuid", uuid);
        	query = new QueryCache(uuid);
        	session.getProject().addQuery(query);
        	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("uuid")) {
        			// already loaded
        		} else if (aname.equals("name")) {
        			query.setName(aval);
        		} else if (aname.equals("data-source")) { 
        			checkMandatory("data-source", aval);
        			SPDataSource ds = session.getProject().getDataSource(aval);
        			if (ds == null) {
        				logger.debug("Project has data sources " + session.getProject().getDataSources());
        				throw new NullPointerException("Could not retrieve " + aval + " from the list of data sources.");
        			}
        			logger.debug("Setting data source in query " + uuid + " to " + ds.getName());
        			query.setDataSource(ds);
        		} else if (aname.equals("zoom")) {
        			query.setZoomLevel(Integer.parseInt(aval));
        		} else {
        			logger.warn("Unexpected attribute of <query>: " + aname + "=" + aval);
        		}
        	}
        } else if (name.equals("constants")) {
        	String uuid = attributes.getValue("uuid");
        	checkMandatory("uuid", uuid);
        	Container constants = query.newConstantsContainer(uuid);
        	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("uuid")) {
        			// already loaded
        		} else if (aname.equals("xpos")) {
        			constants.setPosition(new Point2D.Double(Double.parseDouble(aval), constants.getPosition().getY()));
        			logger.debug("Constants container is at position " + constants.getPosition());
        		} else if (aname.equals("ypos")) {
        			constants.setPosition(new Point2D.Double(constants.getPosition().getX(), Double.parseDouble(aval)));
        		} else {
        			logger.warn("Unexpected attribute of <constants>: " + aname + "=" + aval);
        		}
        	}
        } else if (name.equals("table")) {
        	String tableName = attributes.getValue("name");
        	String schema = attributes.getValue("schema");
        	String catalog = attributes.getValue("catalog");
        	String uuid = attributes.getValue("uuid");
        	checkMandatory("uuid", uuid);
        	checkMandatory("name", tableName);
        	TableContainer table = new TableContainer(uuid, query, tableName, schema, catalog, new ArrayList<SQLObjectItem>());
        	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("name") || aname.equals("schema") || aname.equals("catalog") || aname.equals("uuid")) {
        			// already loaded.
        		} else if (aname.equals("xpos")) {
        			table.setPosition(new Point2D.Double(Double.parseDouble(aval), table.getPosition().getY()));
        		} else if (aname.equals("ypos")) {
        			table.setPosition(new Point2D.Double(table.getPosition().getX(), Double.parseDouble(aval)));
        		} else if (aname.equals("alias")) {
        			table.setAlias(aval);
        		} else {
        			logger.warn("Unexpected attribute of <table>: " + aname + "=" + aval);
        		}
        	}
        	container = table;
        	containerItems = new ArrayList<SQLObjectItem>();
        } else if (name.equals("column")) {
        	if (parentIs("constants")) {
        		String itemName = attributes.getValue("name");
        		String uuid = attributes.getValue("id");
        		checkMandatory("name", itemName);
        		checkMandatory("id", uuid);
        		Item item = new StringItem(itemName, uuid);
            	for (int i = 0; i < attributes.getLength(); i++) {
            		String aname = attributes.getQName(i);
            		String aval = attributes.getValue(i);
            		if (aname.equals("name") || aname.equals("id")) {
            			//already loaded.
            		} else if (aname.equals("alias")) {
            			item.setAlias(aval);
            		} else if (aname.equals("where-text")) {
            			item.setWhere(aval);
            		} else {
            			logger.warn("Unexpected attribute of <constant-column>: " + aname + "=" + aval);
            		}
            	}
            	query.getConstantsContainer().addItem(item);
            	query.addItem(item);
            	uuidToItemMap.put(uuid, item);
        	} else if (parentIs("table")) {
        		String itemName = attributes.getValue("name");
        		String uuid = attributes.getValue("id");
        		checkMandatory("name", itemName);
        		checkMandatory("id", uuid);
        		SQLObjectItem item = new SQLObjectItem(itemName, uuid);
            	for (int i = 0; i < attributes.getLength(); i++) {
            		String aname = attributes.getQName(i);
            		String aval = attributes.getValue(i);
            		if (aname.equals("name") || aname.equals("id")) {
            			//already loaded.
            		} else if (aname.equals("alias")) {
            			item.setAlias(aval);
            		} else if (aname.equals("where-text")) {
            			item.setWhere(aval);
            		} else {
            			logger.warn("Unexpected attribute of <constant-column>: " + aname + "=" + aval);
            		}
            	}
            	containerItems.add(item);
            	uuidToItemMap.put(uuid, item);
        	} else if (parentIs("select")) {
        		String uuid = attributes.getValue("id");
        		checkMandatory("id", uuid);
        		if (uuidToItemMap.get(uuid) == null) {
        			throw new IllegalStateException("Cannot find a column with id " + uuid + " to add to the select statement.");
        		}
        		uuidToItemMap.get(uuid).setSelected(true);
        	} else {
        		throw new IllegalStateException("A column is being loaded that is not contained by any tables. Parent is " + xmlContext.get(xmlContext.size() - 2));
        	}
        } else if (name.equals("join")) {
        	String leftUUID = attributes.getValue("left-item-id");
        	String rightUUID = attributes.getValue("right-item-id");
        	checkMandatory("left-item-id", leftUUID);
        	checkMandatory("right-item-id", rightUUID);
        	Item leftItem = uuidToItemMap.get(leftUUID);
        	Item rightItem = uuidToItemMap.get(rightUUID);
        	if (leftItem == null) {
        		throw new IllegalStateException("The left side of a join was not found. Trying to match UUID " + leftUUID);
        	}
        	if (rightItem == null) {
        		throw new IllegalStateException("The right side of a join was not found. Trying to match UUID " + rightUUID);
        	}
        	SQLJoin join = new SQLJoin(leftItem, rightItem);
        	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("left-item-id") || aname.equals("right-item-id")) {
        			// already loaded
        		} else if (aname.equals("left-is-outer")) {
        			join.setLeftColumnOuterJoin(Boolean.parseBoolean(aval));
        		} else if (aname.equals("right-is-outer")) {
        			join.setRightColumnOuterJoin(Boolean.parseBoolean(aval));
        		} else if (aname.equals("comparator")) {
        			join.setComparator(aval);
        		} else {
        			logger.warn("Unexpected attribute of <join>: " + aname + "=" + aval);
        		}
        	}
        	query.addJoin(join);
        } else if (name.equals("select")) {
        	// Select portion loaded in the "column" part above.
        } else if (name.equals("global-where")) {
        	query.setGlobalWhereClause(attributes.getValue("text"));
        } else if (name.equals("group-by-aggregate")) {
        	String uuid = attributes.getValue("column-id");
        	String aggregate = attributes.getValue("aggregate");
        	checkMandatory("column-id", uuid);
        	checkMandatory("aggregate", aggregate);
        	Item item = uuidToItemMap.get(uuid);
        	if (item == null) {
        		throw new IllegalStateException("Could not get a column for grouping. Trying to match UUID " + uuid);
        	}
        	query.setGroupingEnabled(true);
        	query.setGrouping(item, aggregate);
        } else if (name.equals("having")) {
        	String uuid = attributes.getValue("column-id");
        	String text = attributes.getValue("text");
        	checkMandatory("column-id", uuid);
        	checkMandatory("text", text);
        	Item item = uuidToItemMap.get(uuid);
        	if (item == null) {
        		throw new IllegalStateException("Could not get a column to add a having filter. Trying to match UUID " + uuid);
        	}
        	query.setGroupingEnabled(true);
        	query.setHavingClause(item, text);
        } else if (name.equals("order-by")) {
        	String uuid = attributes.getValue("column-id");
        	String direction = attributes.getValue("direction");
        	checkMandatory("column-id", uuid);
        	checkMandatory("direction", direction);
         	Item item = uuidToItemMap.get(uuid);
        	if (item == null) {
        		throw new IllegalStateException("Could not get a column to add order by to the select statement. Trying to match UUID " + uuid);
        	}
        	query.setSortOrder(item, OrderByArgument.valueOf(direction));
        } else if (name.equals("query-string")) {
        	String queryString = attributes.getValue("string");
        	checkMandatory("string", queryString);
        	query.defineUserModifiedQuery(queryString);
        } else if (name.equals("layout")) {
    		String layoutName = attributes.getValue("name");
    		checkMandatory("name", layoutName);
    		layout = new Layout(layoutName);
    		session.getProject().addLayout(layout);
          	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("name")) {
        			//already loaded
        		} else if (aname.equals("zoom")) {
        			layout.setZoomLevel(Integer.parseInt(aval));
        		} else {
        			logger.warn("Unexpected attribute of <layout>: " + aname + "=" + aval);
        		}
          	}
   
        } else if (name.equals("layout-page")) {
        	String pageName = attributes.getValue("name");
        	checkMandatory("name", pageName);
        	Page page = layout.getPage();
        	//Remove all guides from the page as they will be loaded in a later
        	//part of this handler.
        	for (WabitObject object : page.getChildren()) {
        		if (object instanceof Guide) {
        			page.removeGuide((Guide) object);
        		}
        	}
			page.setName(pageName);
         	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("name")) {
        			// already loaded
        		} else if (aname.equals("height")) {
        			page.setHeight(Integer.parseInt(aval));
        		} else if (aname.equals("width")) {
        			page.setWidth(Integer.parseInt(aval));
        		} else {
        			logger.warn("Unexpected attribute of <layout-page>: " + aname + "=" + aval);
        		}
        	}
        } else if (name.equals("content-box")) {
        	String boxName = attributes.getValue("name");
        	checkMandatory("name", boxName);
        	contentBox = new ContentBox();
        	layout.getPage().addContentBox(contentBox);
         	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("name")) {
        			// already loaded
        		} else if (aname.equals("width")) {
        			contentBox.setWidth(Integer.parseInt(aval));
        		} else if (aname.equals("height")) {
        			contentBox.setHeight(Integer.parseInt(aval));
        		} else if (aname.equals("xpos")) {
        			contentBox.setX(Integer.parseInt(aval));
        		} else if (aname.equals("ypos")) {
        			contentBox.setY(Integer.parseInt(aval));
        		} else {
        			logger.warn("Unexpected attribute of <content-box>: " + aname + "=" + aval);
        		}
         	}
        } else if (name.equals("content-label")) {
        	Label label = new Label(layout);
        	contentBox.setContentRenderer(label);
         	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("name")) {
        			label.setName(aval);
        		} else if (aname.equals("text")) {
        			label.setText(aval);
        		} else if (aname.equals("horizontal-align")) {
        			label.setHorizontalAlignment(HorizontalAlignment.valueOf(aval));
        		} else if (aname.equals("vertical-align")) {
        			label.setVerticalAlignment(VerticalAlignment.valueOf(aval));
        		} else if (aname.equals("bg-colour")) {
        			label.setBackgroundColour(new Color(Integer.parseInt(aval)));
        		} else {
        			logger.warn("Unexpected attribute of <content-label>: " + aname + "=" + aval);
        		}
         	}
        } else if (name.equals("image-renderer")) {
        	imageRenderer = new ImageRenderer(contentBox, null, false);
        	contentBox.setContentRenderer(imageRenderer);
         	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("name")) {
        			imageRenderer.setName(aval);
        		} else {
        			logger.warn("Unexpected attribute of <image-renderer>: " + aname + "=" + aval);
        		}
         	}
         	byteStream = new ByteArrayOutputStream();
        } else if (name.equals("content-result-set")) {
        	String queryID = attributes.getValue("query-id");
        	checkMandatory("query-id", queryID);
        	Query query = null;
        	for (Query q : session.getProject().getQueries()) {
        		if (q.getUUID().equals(UUID.fromString(queryID))) {
        			query = q;
        			break;
        		}
        	}
        	rsRenderer = new ResultSetRenderer(query);
         	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("query-id")) {
        			//already loaded
        		} else if (aname.equals("name")) {
        			rsRenderer.setName(aval);
        		} else if (aname.equals("null-string")) {
        			rsRenderer.setNullString(aval);
        		} else if (aname.equals("bg-colour")) {
        			Color color = new Color(Integer.parseInt(aval));
					logger.debug("Renderer has background " + color.getRed() + ", " + color.getBlue() + ", " + color.getGreen());
        			rsRenderer.setBackgroundColour(color);
        		} else if (aname.equals("border")) {
        			rsRenderer.setBorderType(BorderStyles.valueOf(aval));
        		} else {
        			logger.warn("Unexpected attribute of <content-result-set>: " + aname + "=" + aval);
        		}
         	}
			columnInfoList.clear();
        } else if (name.equals("header-font")) {
        	if (parentIs("content-result-set")) {
        		rsRenderer.setHeaderFont(loadFont(attributes));
        	} else {
        		throw new IllegalStateException("There are no header fonts defined for the parent " + xmlContext.get(xmlContext.size() - 2));
        	}
        } else if (name.equals("body-font")) {
        	if (parentIs("content-result-set")) {
        		rsRenderer.setBodyFont(loadFont(attributes));
        	} else {
        		throw new IllegalStateException("There are no body fonts defined for the parent " + xmlContext.get(xmlContext.size() - 2));
        	}
        } else if (name.equals("column-info")) {
        	colInfo = null;
        	String colInfoName = attributes.getValue("name");
        	String colInfoItem = attributes.getValue("column-info-item-id");
        	
        	//For backwards compatability with 0.9.1
        	String colInfoKey = attributes.getValue("column-info-key");
        	if (colInfoKey != null && colInfoItem == null) {
        		QueryCache q = (QueryCache) rsRenderer.getQuery();
        		for (Map.Entry<String, Item> entry : uuidToItemMap.entrySet()) {
        			Item item = entry.getValue();
        			if (q.getSelectedColumns().contains(item) && (item.getAlias().equals(colInfoKey) || item.getName().equals(colInfoKey))) {
        				colInfoItem = entry.getKey();
        				break;
        			}
        		}
        		if (colInfoItem == null) {
        			colInfo = new ColumnInfo(colInfoKey, colInfoName);
        		}
        	}
        	
        	String colAlias = attributes.getValue("column-alias");
        	if (colInfo == null && colAlias != null && colInfoItem == null) {
        		colInfo = new ColumnInfo(colAlias, colInfoName);
        	}
        	
        	checkMandatory("name", colInfoName);
        	if (colInfo == null) {
        		colInfo = new ColumnInfo(uuidToItemMap.get(colInfoItem), colInfoName);
        	}
        	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("column-info-key") || aname.equals("name")) {
        			//already loaded
        		} else if (aname.equals("width")) {
        			colInfo.setWidth(Integer.parseInt(aval));
        		} else if (aname.equals("horizontal-align")) {
        			colInfo.setHorizontalAlignment(HorizontalAlignment.valueOf(aval));
        		} else if (aname.equals("data-type")) {
        			colInfo.setDataType(DataType.valueOf(aval));
        		} else if (aname.equals("break-on-column")) {
        			colInfo.setWillBreak(Boolean.parseBoolean(aval));
        		} else if (aname.equals("will-subtotal")) {
        			colInfo.setWillSubtotal(Boolean.parseBoolean(aval));
        		}else {
        			logger.warn("Unexpected attribute of <column-info>: " + aname + "=" + aval);
        		}
        	}
        	columnInfoList.add(colInfo);
        } else if (name.equals("date-format")) {
        	if (parentIs("column-info")) {
        		String format = attributes.getValue("format");
        		checkMandatory("format", format);
        		colInfo.setFormat(new SimpleDateFormat(format));
        	} else {
        		throw new IllegalStateException("There is no date format defined for the parent " + xmlContext.get(xmlContext.size() - 2));
        	}
        } else if (name.equals("decimal-format")) {
        	if (parentIs("column-info")) {
        		String format = attributes.getValue("format");
        		checkMandatory("format", format);
        		colInfo.setFormat(new DecimalFormat(format));
        	} else {
        		throw new IllegalStateException("There is no date format defined for the parent " + xmlContext.get(xmlContext.size() - 2));
        	}
        } else if (name.equals("guide")) {
        	String axisName = attributes.getValue("axis");
        	String offsetAmount = attributes.getValue("offset");
        	checkMandatory("axis", axisName);
        	checkMandatory("offset", offsetAmount);
        	Guide guide = new Guide(Axis.valueOf(axisName), Integer.parseInt(offsetAmount));
        	layout.getPage().addGuide(guide);
        } else if (name.equals("font")) {
        	Font font = loadFont(attributes);
        	if (parentIs("layout-page")) {
        		layout.getPage().setDefaultFont(font);
        	} else if (parentIs("content-box")) {
        		contentBox.setFont(font);
        	} else if (parentIs("content-label")) {
        		((Label) contentBox.getContentRenderer()).setFont(font);
        	}
        }
		
	}

	/**
	 * This loads a font based on the given attributes.
	 */
	private Font loadFont(Attributes attributes) throws SAXException {
		String fontName = attributes.getValue("name");
		String fontSize = attributes.getValue("size");
		String fontStyle= attributes.getValue("style");
		checkMandatory("name", fontName);
		checkMandatory("style", fontStyle);
		checkMandatory("size", fontSize);
		Font font = new Font(fontName, Integer.parseInt(fontStyle), Integer.parseInt(fontSize));
		return font;
	}
	
    /**
     * Throws an informative exception if the given value is null.
     * 
     * @param attName Name of the attribute that's supposed to contain the value
     * @param value The value actually recovered from the attribute (if any)
     * @throws SAXException If value is null.
     */
    private void checkMandatory(String attName, Object value) throws SAXException {
        if (value == null) {
            throw new SAXException("Missing mandatory attribute \""+attName+"\" of element \""+xmlContext.peek()+"\"");
        }
    }
	
    /**
     * Returns true if the name of the parent element in the XML context
     * (the one just below the top of the stack) is the given name.
     * 
     * @param qName The name to check for equality with the parent element name.
     * @return If qName == parent element name
     */
    private boolean parentIs(String qName) {
        return xmlContext.get(xmlContext.size() - 2).equals(qName);
    }
      
    @Override
    public void endElement(String uri, String localName, String name)
    		throws SAXException {
    	if (name.equals("table")) {
    		TableContainer table = new TableContainer(container.getUUID().toString(), query, container.getName(), ((TableContainer) container).getSchema(), ((TableContainer) container).getCatalog(), containerItems);
    		table.setPosition(container.getPosition());
    		table.setAlias(container.getAlias());
    		query.addTable(table);
    	} else if (name.equals("content-result-set")) {
    		ResultSetRenderer newRSRenderer = new ResultSetRenderer(rsRenderer.getQuery(), columnInfoList);
    		newRSRenderer.setBodyFont(rsRenderer.getBodyFont());
    		newRSRenderer.setHeaderFont(rsRenderer.getHeaderFont());
    		newRSRenderer.setName(rsRenderer.getName());
    		newRSRenderer.setNullString(rsRenderer.getNullString());
    		newRSRenderer.setBackgroundColour(rsRenderer.getBackgroundColour());
    		newRSRenderer.setBorderType(rsRenderer.getBorderType());
    		contentBox.setContentRenderer(newRSRenderer);
    	} else if (name.equals("image-renderer")) {
    		byte[] byteArray = BASE64DecoderStream.decode(byteStream.toByteArray());
    		logger.debug("Decoding byte stream: Stream has " + byteStream.toString().length() + " and array has " + Arrays.toString(byteArray));
    		try {
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(byteArray));				
				imageRenderer.setImage(img);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			imageRenderer = null;
    	}
    	xmlContext.pop();
    }
    
    @Override
    public void characters(char[] ch, int start, int length)
    		throws SAXException {
    	if (imageRenderer != null) {
    		logger.debug("Starting characters at " + start + " and ending at " + length);
    		for (int i = start; i < start+length; i++) {
    			byteStream.write((byte)ch[i]);
    		}
    		logger.debug("Byte stream has " + byteStream.toString());
    	}
    }

	public List<WabitSession> getSessions() {
		return Collections.unmodifiableList(sessions);
	}
	
}
