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

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.SQLGroupFunction;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.wabit.Query;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitProject;
import ca.sqlpower.wabit.query.Container;
import ca.sqlpower.wabit.query.Item;
import ca.sqlpower.wabit.query.QueryCache;
import ca.sqlpower.wabit.query.SQLJoin;
import ca.sqlpower.wabit.query.TableContainer;
import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.ReportContentRenderer;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.xml.XMLHelper;

import com.sun.mail.util.BASE64EncoderStream;

public class ProjectXMLDAO {
	
	private static final Logger logger = Logger.getLogger(ProjectXMLDAO.class);

	/**
	 * This output stream will be used to  write the project to a file.
	 */
	private final PrintWriter out;
	
	/**
	 * This XML helper will do the formatting and outputting of the XML that
	 * creates our save file.
	 */
	private final XMLHelper xml;

	/**
	 * The project this DAO will write to a file.
	 */
	private final WabitProject project;
	
	/**
	 * This is the output stream contained by the print writer.
	 */
	private final OutputStream outputStream;
	
	/**
	 * This will construct a XML DAO to save the entire project or parts of 
	 * the project to be loaded in later.
	 */
	public ProjectXMLDAO(OutputStream out, WabitProject project) {
		this.project = project;
		this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
		outputStream = out;
		xml = new XMLHelper();
	}
	
	/**
	 * This XML DAO will save a specific query in a project as XML.
	 * The query can then be loaded as a stand-alone project or be imported
	 * into another project.
	 */
	public void save(Query query) {
		save(Collections.singletonList(query.getWabitDataSource()), Collections.singletonList(query), new ArrayList<Layout>());
	}
	
	public void save(Layout layout) {
		Set<Query> queries = new HashSet<Query>();
		for (Page page : layout.getChildren()) {
			for (ContentBox contentBox : page.getContentBoxes()) {
				ReportContentRenderer rcr = contentBox.getContentRenderer();
				if (rcr instanceof ResultSetRenderer) {
					queries.add(((ResultSetRenderer) rcr).getQuery());
				}
			}
		}
		
		Set<WabitDataSource> dataSources = new HashSet<WabitDataSource>();
		for (Query query : queries) {
			dataSources.add(query.getWabitDataSource());
		}
		
		save(new ArrayList<WabitDataSource>(dataSources), new ArrayList<Query>(queries), Collections.singletonList(layout));
	}
	
	public void save() {
		save(project.getDataSources(), project.getQueries(), project.getLayouts());
	}
	
	private void save(List<WabitDataSource> dataSources, List<Query> queries, List<Layout> layouts) {
		xml.println(out, "<?xml version='1.0' encoding='UTF-8'?>");
		xml.println(out, "");
		xml.println(out, "<wabit export-format=\"1.0.0\">");
		xml.indent++;

		xml.print(out, "<project");
		printAttribute("name", project.getName());
		printAttribute("editorPanelModel", project.getEditorPanelModel().getUUID().toString());
		xml.println(out, ">");
		xml.indent++;
		
		saveDataSources(dataSources);
		
		for (Query query : queries) {
			if (query instanceof QueryCache) {
				saveQueryCache((QueryCache)query);
			} else {
				throw new IllegalStateException("Trying to store a query of type " + query.getClass() + " which is unhandled by saving.");
			}
		}
		
		for (Layout layout : layouts) {
			saveLayout(layout);
		}
		
		xml.indent--;
		xml.println(out, "</project>");
		
		xml.indent--;
		xml.println(out, "</wabit>");
		out.flush();
		out.close();
	}

	private void saveDataSources(List<WabitDataSource> dataSources) {
		xml.println(out, "<data-sources>");
		xml.indent++;
		
		for (WabitDataSource ds : dataSources) {
			if (ds == null) {
				continue;
			}
			xml.print(out, "<data-source");
			printAttribute("name", ds.getName());
			xml.println(out, "/>");
		}
		
		xml.indent--;
		xml.println(out, "</data-sources>");
	}
	
	/**
	 * This saves a layout. This will not close the print writer passed into the constructor.
	 * If this save method is used to export the query cache somewhere then close should be 
	 * called on it to flush the print writer and close it.
	 */
	private void saveLayout(Layout layout) {
		xml.print(out, "<layout");
		printAttribute("name", layout.getName());
		printAttribute("zoom", layout.getZoomLevel());
		xml.println(out, ">");
		xml.indent++;
		
		Page page = layout.getPage();
		xml.print(out, "<layout-page");
		printAttribute("name", page.getName());
		printAttribute("height", page.getHeight());
		printAttribute("width", page.getWidth());
		xml.println(out, ">");
		xml.indent++;
		saveFont(page.getDefaultFont());
		
		for (WabitObject object : page.getChildren()) {
			if (object instanceof ContentBox) {
				ContentBox box = (ContentBox) object;
				xml.print(out, "<content-box");
				printAttribute("name", box.getName());
				printAttribute("width", box.getWidth());
				printAttribute("height", box.getHeight());
				printAttribute("xpos", box.getX());
				printAttribute("ypos", box.getY());
				xml.println(out, ">");
				xml.indent++;
				saveFont(box.getFont());
				
				if (box.getContentRenderer() != null) {
					if (box.getContentRenderer() instanceof Label) {
						Label label = (Label) box.getContentRenderer();
						xml.print(out, "<content-label");
						printAttribute("name", label.getName());
						printAttribute("text", label.getText());
						printAttribute("horizontal-align", label.getHorizontalAlignment().name());
						printAttribute("vertical-align", label.getVerticalAlignment().name());
						if (label.getBackgroundColour() != null) {
							printAttribute("bg-colour", label.getBackgroundColour().getRGB());
						}
						xml.println(out, ">");
						saveFont(label.getFont());
						xml.println(out, "</content-label>");
					} else if (box.getContentRenderer() instanceof ResultSetRenderer) {
						ResultSetRenderer rsRenderer = (ResultSetRenderer) box.getContentRenderer();
						xml.print(out, "<content-result-set");
						printAttribute("name", rsRenderer.getName());
						printAttribute("query-id", rsRenderer.getQuery().getUUID().toString());
						printAttribute("null-string", rsRenderer.getNullString());
						printAttribute("border", rsRenderer.getBorderType().name());
						if (rsRenderer.getBackgroundColour() != null) {
							printAttribute("bg-colour", rsRenderer.getBackgroundColour().getRGB());
						}
						xml.println(out, ">");
						xml.indent++;
						saveFont(rsRenderer.getHeaderFont(), "header-font");
						saveFont(rsRenderer.getBodyFont(), "body-font");
						for (WabitObject rendererChild : rsRenderer.getChildren()) {
							ColumnInfo ci = (ColumnInfo) rendererChild;
							xml.print(out, "<column-info");
							printAttribute("name", ci.getName());
							printAttribute("width", ci.getWidth());
							if (ci.getColumnInfoItem() != null) {
								printAttribute("column-info-item-id", ci.getColumnInfoItem().getUUID().toString());
							}
							printAttribute("column-alias", ci.getColumnAlias());
							printAttribute("horizontal-align", ci.getHorizontalAlignment().name());
							printAttribute("data-type", ci.getDataType().name());
							printAttribute("break-on-column", Boolean.toString(ci.getWillBreak()));
							printAttribute("will-subtotal", Boolean.toString(ci.getWillSubtotal()));
							xml.println(out, ">");
							xml.indent++;
							if (ci.getFormat() instanceof SimpleDateFormat) {
								xml.print(out, "<date-format");
								SimpleDateFormat dateFormat = (SimpleDateFormat) ci.getFormat();
								printAttribute("format", dateFormat.toPattern());
								xml.println(out, "/>");
							} else if (ci.getFormat() instanceof DecimalFormat) {
								xml.print(out, "<decimal-format");
								DecimalFormat decimalFormat = (DecimalFormat) ci.getFormat();
								printAttribute("format", decimalFormat.toPattern());
								xml.println(out, "/>");
							} else if (ci.getFormat() == null) {
								// This is a default format
							} else {
								throw new ClassCastException("Cannot cast format of type " + ci.getFormat().getClass() + " to a known format type when saving.");
							}
							xml.indent--;
							xml.println(out, "</column-info>");
						}
						xml.indent--;
						xml.println(out, "</content-result-set>");
					} else if (box.getContentRenderer() instanceof ImageRenderer) {
						ImageRenderer imgRenderer = (ImageRenderer) box.getContentRenderer();
						xml.print(out, "<image-renderer");
						printAttribute("name", imgRenderer.getName());
						xml.print(out, ">");
						BufferedImage image = imgRenderer.getImage();
						if (image != null) {
							try {
								out.flush();
								ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
								ImageIO.write(image, "PNG", byteStream);
								byte[] byteArray = BASE64EncoderStream.encode(byteStream.toByteArray());
								char[] charArray = new char[byteArray.length];
								for (int i = 0; i < byteArray.length; i++) {
									charArray[i] = (char)byteArray[i];
								}
								logger.debug("Encoded length is " + byteArray.length);
								logger.debug("Stream has byte array " + Arrays.toString(byteStream.toByteArray()));
								out.write(charArray);
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
						out.println("</image-renderer>");
					} else {
						throw new ClassCastException("Cannot save a content renderer of class " + box.getContentRenderer().getClass());
					}
				}
				xml.indent--;
				xml.println(out, "</content-box>");
			} else if (object instanceof Guide) {
				Guide guide = (Guide) object;
				xml.print(out, "<guide");
				printAttribute("axis", guide.getAxis().name());
				printAttribute("offset", guide.getOffset());
				xml.println(out, "/>");
			} else {
				throw new ClassCastException("Cannot save page element of type " + object.getClass());
			}
		}
		
		xml.indent--;
		xml.println(out, "</layout-page>");
		
		xml.indent--;
		xml.println(out, "</layout>");
	}
	
	/**
	 * This will save a font to the print writer. The font tag must be contained within tags of 
	 * the font's parent object. This allows giving a specific font name for the XML tag.
	 */
	private void saveFont(Font font, String fontName) {
		xml.print(out, "<" + fontName);
		printAttribute("name", font.getFamily());
		printAttribute("size", font.getSize());
		printAttribute("style", font.getStyle());
		xml.println(out, "/>");
	}
	
	/**
	 * This will save a font to the print writer. The font tag must be contained within tags of 
	 * the font's parent object.
	 */
	private void saveFont(Font font) {
		saveFont(font, "font");
	}
	
	/**
	 * This saves a query cache. This will not close the print writer passed into the constructor.
	 * If this save method is used to export the query cache somewhere then close should be 
	 * called on it to flush the print writer and close it.
	 */
	private void saveQueryCache(QueryCache cache) {
		xml.print(out, "<query");
		printAttribute("name", cache.getName());
		printAttribute("uuid", cache.getUUID().toString());
		printAttribute("zoom", cache.getZoomLevel());
		if (cache.getDataSource() != null) {
			printAttribute("data-source", cache.getDataSource().getName());
		}
		xml.println(out, ">");
		xml.indent++;

		Map<Item, String> itemIdMap = new HashMap<Item, String>();

		xml.print(out, "<constants");
		Container constants = cache.getConstantsContainer();
		printAttribute("uuid", constants.getUUID().toString());
		printAttribute("xpos", constants.getPosition().getX());
		printAttribute("ypos", constants.getPosition().getY());
		xml.println(out, ">");
		xml.indent++;
		for (Item item : constants.getItems()) {
			xml.print(out, "<column");
			printAttribute("id", item.getUUID().toString());
			itemIdMap.put(item, item.getUUID().toString());
			printAttribute("name", item.getName());
			printAttribute("alias", item.getAlias());
			printAttribute("where-text", item.getWhere());
			xml.println(out, "/>");
		}
		xml.indent--;
		xml.println(out, "</constants>");
		
		for (Container table : cache.getFromTableList()) {
			xml.print(out, "<table");
			printAttribute("name", table.getName());
			printAttribute("uuid", table.getUUID().toString());
			TableContainer tableContainer = (TableContainer)table;
			if (!tableContainer.getSchema().equals("")) {
				printAttribute("schema", tableContainer.getSchema());
			}
			if (!tableContainer.getCatalog().equals("")) {
				printAttribute("catalog", tableContainer.getCatalog());
			}
			printAttribute("alias", table.getAlias());
			printAttribute("xpos", table.getPosition().getX());
			printAttribute("ypos", table.getPosition().getY());
			xml.println(out, ">");
			xml.indent++;
			for (Item item : table.getItems()) {
				xml.print(out, "<column");
				printAttribute("id", item.getUUID().toString());
				itemIdMap.put(item, item.getUUID().toString());
				printAttribute("name", item.getName());
				printAttribute("alias", item.getAlias());
				printAttribute("where-text", item.getWhere());
				xml.println(out, "/>");
			}
			xml.indent--;
			xml.println(out, "</table>");
		}	
		
		for (SQLJoin join : cache.getJoins()) {
			xml.print(out, "<join");
			printAttribute("left-item-id", itemIdMap.get(join.getLeftColumn()));
			printAttribute("left-is-outer", Boolean.toString(join.isLeftColumnOuterJoin()));
			printAttribute("right-item-id", itemIdMap.get(join.getRightColumn())); 
			printAttribute("right-is-outer", Boolean.toString(join.isRightColumnOuterJoin()));
			printAttribute("comparator", join.getComparator()); 
			xml.println(out, "/>");
		}
				
		xml.println(out, "<select>");
		xml.indent++;
		for (Item col : cache.getSelectedColumns()) {
			xml.print(out, "<column");
			printAttribute("id", itemIdMap.get(col));
			xml.println(out, "/>");
		}
		xml.indent--;
		xml.println(out, "</select>");
		
		xml.print(out, "<global-where");
		printAttribute("text", cache.getGlobalWhereClause());
		xml.println(out, "/>");
		
		if (cache.isGroupingEnabled()) {
			for (Map.Entry<Item, SQLGroupFunction> entry: cache.getGroupByAggregateMap().entrySet()) {
				xml.print(out, "<group-by-aggregate");
				printAttribute("column-id", itemIdMap.get(entry.getKey()));
				printAttribute("aggregate", entry.getValue().name());
				xml.println(out, "/>");
			}
			
			for (Map.Entry<Item, String> entry : cache.getHavingMap().entrySet()) {
				xml.print(out, "<having");
				printAttribute("column-id", itemIdMap.get(entry.getKey()));
				printAttribute("text", entry.getValue());
				xml.println(out, "/>");
			}
			
		}
		
		for (Item item : cache.getOrderByList()) {
			xml.println(out, "<order-by");
			printAttribute("column-id", itemIdMap.get(item));
			printAttribute("direction", cache.getOrderByArgument(item).name());
			xml.println(out, "/>");
		}
		
		if (cache.isScriptModified()) {
			xml.print(out, "<query-string");
			printAttribute("string", cache.generateQuery());
			xml.println(out, "/>");		
		}

		xml.indent--;
		xml.println(out, "</query>");
	}
	
	/**
	 * Prints an attribute to the file. If the attribute value is null
	 * no attribute will be printed.
	 */
    private void printAttribute(String name, String value) {
        if (value == null) return;
        xml.niprint(out, " " + name + "=\"");
        xml.niprint(out, SQLPowerUtils.escapeXML(value) + "\"");
    }
    
    private void printAttribute(String name, double value) {
    	xml.niprint(out, " " + name + "=\"" + value + "\"");
    }
    
    private void printAttribute(String name, int value) {
    	xml.niprint(out, " " + name + "=\"" + value + "\"");
    }
    
    /**
     * Call this to flush and close the output stream if only part
     * of the file is being saved.
     */
    public void close() {
    	out.flush();
    	out.close();
    }
	
}
