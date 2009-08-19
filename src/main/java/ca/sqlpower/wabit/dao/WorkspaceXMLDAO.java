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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import ca.sqlpower.graph.DepthFirstSearch;
import ca.sqlpower.graph.GraphModel;
import ca.sqlpower.query.Container;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.Query;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.query.TableContainer;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.Version;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitVersion;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.SaveOLAP4jQuery;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.chart.ColumnIdentifier;
import ca.sqlpower.wabit.report.chart.ColumnNameColumnIdentifier;
import ca.sqlpower.wabit.report.chart.PositionColumnIdentifier;
import ca.sqlpower.wabit.report.chart.RowAxisColumnIdentifier;
import ca.sqlpower.xml.XMLHelper;

public class WorkspaceXMLDAO {
	
	private static final Logger logger = Logger.getLogger(WorkspaceXMLDAO.class);

	/**
	 * The version number to put in exported files. This is not the Wabit version
	 * number; it is the version of the file format itself. It is common for the
	 * version number to change independent of Wabit releases, and this is especially
	 * important for those using the continuous integration builds to do real work.
	 * 
     * <h2>VERSION CHANGE HISTORY</h2>
     * 
     * <dl>
     *  <dt>1.0.0 <dd>initial version. lots of changes. (too many!)
     *  <dt>1.0.1 <dd>adds page orientation attribute
     *  <dt>1.0.2 <dd>update to the chart column identifier, they are now objects instead of just column names
     *  <dt>1.0.3 <dd>Added more info to saved queries inside a report definition.
     *  <dt>1.1.0 <dd>OLAP query syntax has changed, both inside the datasources definition and the report.
     *  <dt>1.1.1 <dd>OLAP query syntax has changed for reports, -report tag was removed.
     *  <dt>1.1.2 <dd>Added exclusions when saving an OLAP query
     *  <dt>1.1.3 <dd>Changed how images are being saved. There is now a wabit-image section for each {@link WabitImage}.
     *  <dt>1.1.4 <dd>Added two flags to the query cache to save if a user should be prompted each time the query
     *  is executed with a missing join, and if the user is not being prompted, if the query should be automatically
     *  executed.
     *  <dt>1.1.5 <dd>Merged the data type and the x axis identifier into the column identifier itself.
     *  This removes the graph-name-to-data-type and graph-series-col-to-x-axis-col tags.
     * </dl> 
	 */
	//                                         UPDATE HISTORY!!!!!
    static final Version FILE_VERSION = new Version(1, 1, 4); // please update version history (above) when you change this
    //                                         UPDATE HISTORY!!??!
    
    /**
     * Each edge is made up of a parent {@link WabitObject} and a child {@link WabitObject}.
     * The edge goes in the direction from the parent to the child.
     */
    private class ProjectGraphModelEdge {
        
        private final WabitObject parent;
        private final WabitObject child;

        public ProjectGraphModelEdge(WabitObject parent, WabitObject child) {
            this.parent = parent;
            this.child = child;
        }
        
        public WabitObject getParent() {
            return parent;
        }
        
        public WabitObject getChild() {
            return child;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ProjectGraphModelEdge) {
                ProjectGraphModelEdge wabitObject = (ProjectGraphModelEdge) obj;
                return getParent().equals(wabitObject.getParent()) && getChild().equals(wabitObject.getChild());
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            int result = 17;
            result = 37 * result + getParent().hashCode();
            result = 37 * result + getChild().hashCode();
            return result;
        }
    }
    
    /**
     * This graph takes a {@link WabitObject} for its root and makes a graph model that represents
     * all of the root's dependencies. The root is included in the dependencies.
     */
    private class ProjectGraphModel implements GraphModel<WabitObject, ProjectGraphModelEdge> {
        
        private final WabitObject root;

        public ProjectGraphModel(WabitObject root) {
            this.root = root;
        }

        public Collection<WabitObject> getAdjacentNodes(WabitObject node) {
            return node.getDependencies();
        }

        public Collection<ProjectGraphModelEdge> getEdges() {
            Set<ProjectGraphModelEdge> allEdges = new HashSet<ProjectGraphModelEdge>();
            allEdges.addAll(getOutboundEdges(root));
            Collection<ProjectGraphModelEdge> outboundEdges = getOutboundEdges(root);
            while (!outboundEdges.isEmpty()) {
                ProjectGraphModelEdge edge = outboundEdges.iterator().next();
                outboundEdges.remove(edge);
                if (allEdges.contains(edge)) continue;
                allEdges.add(edge);
                outboundEdges.addAll(getOutboundEdges(edge.getChild()));
            }
            return allEdges;
        }

        public Collection<ProjectGraphModelEdge> getInboundEdges(
                WabitObject node) {
            WabitObject parent = node.getParent();
            if (parent == null) return Collections.emptyList();
            return Collections.singletonList(new ProjectGraphModelEdge(parent, node));
        }

        public Collection<WabitObject> getNodes() {
            Set<WabitObject> allNodes = new HashSet<WabitObject>();
            allNodes.add(root);
            Collection<WabitObject> adjacentNodes = getAdjacentNodes(root);
            while (!adjacentNodes.isEmpty()) {
                WabitObject node = adjacentNodes.iterator().next();
                adjacentNodes.remove(node);
                if (allNodes.contains(node)) continue;
                allNodes.add(node);
                adjacentNodes.addAll(getAdjacentNodes(node));
            }
            return allNodes;
        }

        public Collection<ProjectGraphModelEdge> getOutboundEdges(
                WabitObject node) {
            List<ProjectGraphModelEdge> dependencyEdges = new ArrayList<ProjectGraphModelEdge>();
            for (WabitObject dependency : node.getDependencies()) {
                dependencyEdges.add(new ProjectGraphModelEdge(node, dependency));
            }
            return dependencyEdges;
        }
        
    }
    
	/**
	 * This output stream will be used to  write the workspace to a file.
	 */
	private final PrintWriter out;
	
	/**
	 * This XML helper will do the formatting and outputting of the XML that
	 * creates our save file.
	 */
	private final XMLHelper xml;

	/**
	 * This is the context that contains objects that require saving.
	 */
	private final WabitSessionContext context;

    private final Comparator<WabitObject> wabitObjectComparator = new WabitObjectComparator();
	
	/**
	 * This will construct a XML DAO to save the entire workspace or parts of 
	 * the workspace to be loaded in later.
	 */
	public WorkspaceXMLDAO(OutputStream out, WabitSessionContext context) {
	    this.context = context;
		try {
            this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("The UTF-8 encoding should always be supported.");
        }
		xml = new XMLHelper();
	}
	
	public void saveActiveWorkspace() {
		save(Collections.singletonList(context.getActiveSession().getWorkspace()));
	}

    /**
     * This method is used to export parts of Wabit as well as save the entire
     * set of workspaces.
     * 
     * @param objectToSave
     *            The object that is to save. If this is an object in a
     *            workspace all of the necessary data sources and other
     *            WabitObjects will be saved with it.
     */
	public void save(List<? extends WabitObject> objectToSave) {
		xml.println(out, "<?xml version='1.0' encoding='UTF-8'?>");
		xml.println(out, "");
		xml.println(out, "<wabit export-format=\"" + FILE_VERSION + "\" wabit-app-version=\"" + WabitVersion.VERSION + "\">");
		xml.indent++;
		
		Map<WabitWorkspace, List<WabitObject>> workspaceToDependencies = new HashMap<WabitWorkspace, List<WabitObject>>();
		
		for (WabitObject savingObject : objectToSave) {
		    WabitObject parentWorkspace = savingObject;
		    while (!(parentWorkspace instanceof WabitWorkspace)) {
		        parentWorkspace = parentWorkspace.getParent();
		    }
		    
		    List<WabitObject> workspaceDependencies = workspaceToDependencies.get(parentWorkspace);
		    if (workspaceDependencies == null) {
		        workspaceDependencies = new ArrayList<WabitObject>();
		        workspaceToDependencies.put((WabitWorkspace) parentWorkspace, workspaceDependencies);
		    }
		    
		    DepthFirstSearch<WabitObject, ProjectGraphModelEdge> dfs = new DepthFirstSearch<WabitObject, ProjectGraphModelEdge>();
		    dfs.performSearch(new ProjectGraphModel(savingObject));
		    List<WabitObject> dependenciesToSave = dfs.getFinishOrder();
		    for (WabitObject object : dependenciesToSave) {
		        if (!workspaceDependencies.contains(object)) {
		            workspaceDependencies.add(object);
		        }
		    }
		}
		
		for (List<WabitObject> dependenciesToSave : workspaceToDependencies.values()) {
		    Collections.sort(dependenciesToSave, wabitObjectComparator);
		}

		for (Map.Entry<WabitWorkspace, List<WabitObject>> entry : workspaceToDependencies.entrySet()) {
		    WabitWorkspace workspace = entry.getKey();
		    List<WabitObject> dependenciesToSave = entry.getValue();
		    xml.print(out, "<project");
		    printAttribute("name", workspace.getName());
		    if (workspace.getEditorPanelModel() != null) {
		        printAttribute("editorPanelModel", workspace.getEditorPanelModel().getUUID());
		    }
		    xml.niprintln(out, ">");
		    xml.indent++;


		    List<WabitDataSource> dataSources = new ArrayList<WabitDataSource>();
		    for (WabitObject wabitObject : dependenciesToSave) {
		        if (wabitObject instanceof WabitDataSource) {
		            dataSources.add((WabitDataSource) wabitObject);
		        }
		    }

		    saveDataSources(dataSources);

		    for (WabitObject wabitObject : dependenciesToSave) {
		        if (wabitObject instanceof QueryCache) {
		            saveQueryCache((QueryCache) wabitObject);
		        } else if (wabitObject instanceof OlapQuery) {
		            saveOlapQuery((OlapQuery) wabitObject);
		        } else if (wabitObject instanceof Layout) {
		            saveLayout((Layout) wabitObject);
		        } else if (wabitObject instanceof WabitImage) {
		            saveWabitImage((WabitImage) wabitObject);
		        } else {
		            logger.info("Not saving wabit object " + wabitObject.getName() + " of type " + wabitObject.getClass() + " as it should be saved elsewhere.");
		        }
		    }

		    xml.indent--;
		    xml.println(out, "</project>");
		}
		
		xml.indent--;
		xml.println(out, "</wabit>");
		out.flush();
		out.close();
		logger.debug("Saving complete");
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
			xml.niprintln(out, "/>");
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
		printAttribute("uuid", layout.getUUID());
		xml.niprintln(out, ">");
		xml.indent++;
		
		Page page = layout.getPage();
		xml.print(out, "<layout-page");
		printAttribute("name", page.getName());
		printAttribute("height", page.getHeight());
		printAttribute("width", page.getWidth());
		printAttribute("orientation", page.getOrientation().name());
		xml.niprintln(out, ">");
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
				xml.niprintln(out, ">");
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
						xml.niprintln(out, ">");
						saveFont(label.getFont());
						xml.println(out, "</content-label>");
					} else if (box.getContentRenderer() instanceof ResultSetRenderer) {
						ResultSetRenderer rsRenderer = (ResultSetRenderer) box.getContentRenderer();
						xml.print(out, "<content-result-set");
						printAttribute("name", rsRenderer.getName());
						printAttribute("uuid", rsRenderer.getUUID());
						printAttribute("query-id", rsRenderer.getQuery().getUUID());
						printAttribute("null-string", rsRenderer.getNullString());
						printAttribute("border", rsRenderer.getBorderType().name());
						if (rsRenderer.getBackgroundColour() != null) {
							printAttribute("bg-colour", rsRenderer.getBackgroundColour().getRGB());
						}
						xml.niprintln(out, ">");
						xml.indent++;
						saveFont(rsRenderer.getHeaderFont(), "header-font");
						saveFont(rsRenderer.getBodyFont(), "body-font");
						for (WabitObject rendererChild : rsRenderer.getChildren()) {
							ColumnInfo ci = (ColumnInfo) rendererChild;
							xml.print(out, "<column-info");
							printAttribute("name", ci.getName());
							printAttribute("width", ci.getWidth());
							if (ci.getColumnInfoItem() != null) {
								printAttribute("column-info-item-id", ci.getColumnInfoItem().getUUID());
							}
							printAttribute("column-alias", ci.getColumnAlias());
							printAttribute("horizontal-align", ci.getHorizontalAlignment().name());
							printAttribute("data-type", ci.getDataType().name());
							printAttribute("group-or-break", ci.getWillGroupOrBreak().name());
							printAttribute("will-subtotal", Boolean.toString(ci.getWillSubtotal()));
							xml.niprintln(out, ">");
							xml.indent++;
							if (ci.getFormat() instanceof SimpleDateFormat) {
								xml.print(out, "<date-format");
								SimpleDateFormat dateFormat = (SimpleDateFormat) ci.getFormat();
								printAttribute("format", dateFormat.toPattern());
								xml.niprintln(out, "/>");
							} else if (ci.getFormat() instanceof DecimalFormat) {
								xml.print(out, "<decimal-format");
								DecimalFormat decimalFormat = (DecimalFormat) ci.getFormat();
								printAttribute("format", decimalFormat.toPattern());
								xml.niprintln(out, "/>");
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
						if (imgRenderer.getImage() != null) {
						    printAttribute("wabit-image-uuid", imgRenderer.getImage().getUUID());
						}
						xml.niprint(out, ">");
						out.println("</image-renderer>");
						
					} else if (box.getContentRenderer() instanceof ChartRenderer) {
						ChartRenderer graphRenderer = (ChartRenderer) box.getContentRenderer();
						xml.print(out, "<graph-renderer");
						printAttribute("name", graphRenderer.getName());
						printAttribute("uuid", graphRenderer.getUUID());
						printAttribute("y-axis-name", graphRenderer.getYaxisName());
						printAttribute("x-axis-name" , graphRenderer.getXaxisName());
						if (graphRenderer.getChartType() != null) {
						    printAttribute("graph-type", graphRenderer.getChartType().name());
						}
						if (graphRenderer.getLegendPosition() != null) {
						    printAttribute("legend-position", graphRenderer.getLegendPosition().name());
						}
						if (graphRenderer.getQuery() != null) {
						    printAttribute("query-id", graphRenderer.getQuery().getUUID());
						}
						xml.niprintln(out, ">");
						xml.indent++;
						xml.println(out, "<graph-col-names-in-order>");
                        xml.indent++;
                        for (ColumnIdentifier colIdentifier : graphRenderer.getColumnNamesInOrder()) {
                            xml.print(out, "<graph-col-names");
                            saveColumnIdentifier(out, colIdentifier, "");
                            printAttribute("data-type", colIdentifier.getDataType().name());
                            saveColumnIdentifier(out, colIdentifier.getXAxisIdentifier(), "x-axis-");
                            xml.niprintln(out, "/>");
                        }
                        xml.indent--;
                        xml.println(out, "</graph-col-names-in-order>");
                        xml.println(out, "<missing-identifiers>");
                        xml.indent++;
                        for (ColumnIdentifier identifier : graphRenderer.getMissingIdentifiers()) {
                            xml.print(out, "<missing-identifier");
                            saveColumnIdentifier(out, identifier, "");
                            xml.niprintln(out, "/>");
                        }
                        xml.indent--;
                        xml.println(out, "</missing-identifiers>");
						xml.indent--;
						xml.println(out, "</graph-renderer>");
					} else if (box.getContentRenderer() instanceof CellSetRenderer) {
					    CellSetRenderer renderer = (CellSetRenderer) box.getContentRenderer();
					    xml.print(out, "<cell-set-renderer");
					    printAttribute("name", renderer.getName());
					    printAttribute("uuid", renderer.getUUID());
					    printAttribute("olap-query-uuid", renderer.getOlapQuery().getUUID());
					    printAttribute("body-alignment", renderer.getBodyAlignment().toString());
					    if (renderer.getBodyFormat() != null) {
					        printAttribute("body-format-pattern", renderer.getBodyFormat().toPattern());
					    }
					    xml.println(out, ">");
					    xml.indent++;
					    
					    saveFont(renderer.getHeaderFont(), "olap-header-font");
					    saveFont(renderer.getBodyFont(), "olap-body-font");
				        
					    this.saveOlapQuery(renderer.getModifiedOlapQuery());
					    
					    xml.indent--;
					    xml.println(out, "</cell-set-renderer>");
					    
					} else {
						throw new ClassCastException("Cannot save a content renderer of class " + box.getContentRenderer().getClass());
					}
				}
				xml.indent--;
				xml.println(out, "</content-box>");
			} else if (object instanceof Guide) {
				Guide guide = (Guide) object;
				xml.print(out, "<guide");
				printAttribute("name", guide.getName());
				printAttribute("axis", guide.getAxis().name());
				printAttribute("offset", guide.getOffset());
				xml.niprintln(out, "/>");
			} else {
				throw new ClassCastException("Cannot save page element of type " + object.getClass());
			}
		}
		
		xml.indent--;
		xml.println(out, "</layout-page>");
		
		xml.indent--;
		xml.println(out, "</layout>");
	}
	
	private void saveWabitImage(WabitImage wabitImage) {
	    xml.print(out, "<wabit-image");
        printAttribute("name", wabitImage.getName());
        printAttribute("uuid", wabitImage.getUUID());
        xml.niprint(out, ">");
        xml.indent++;
	    
	    final Image wabitInnerImage = wabitImage.getImage();
	    if (wabitInnerImage != null) {
	        BufferedImage image;
	        if (wabitInnerImage instanceof BufferedImage) {
	            image = (BufferedImage) wabitInnerImage;
	        } else {
	            image = new BufferedImage(wabitInnerImage.getWidth(null), 
	                    wabitInnerImage.getHeight(null), BufferedImage.TYPE_INT_ARGB); 
	            final Graphics2D g = image.createGraphics();
	            g.drawImage(wabitInnerImage, 0, 0, null);
	            g.dispose();
	        }
	        if (image != null) {
	            try {
	                out.flush();
	                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	                ImageIO.write(image, "PNG", byteStream);
	                byte[] byteArray = new Base64().encode(byteStream.toByteArray());
	                logger.debug("Encoded length is " + byteArray.length);
	                logger.debug("Stream has byte array " + Arrays.toString(byteStream.toByteArray()));
	                for (int i = 0; i < byteArray.length; i++) {
	                    out.write((char)byteArray[i]);
	                    if (i % 60 == 59) {
	                        out.write("\n");
	                    }
	                }
	            } catch (IOException e) {
	                throw new RuntimeException(e);
	            }
	        }
	    }
        
        xml.indent--;
        xml.println(out, "</wabit-image>");
	}
	
    /**
     * This is a helper method for saving layouts with charts. This saves
     * ColumnIdentifiers used in charts to define what column is used and how.
     * This will only save the {@link ColumnIdentifier} as an attribute of the
     * current xml element it is in, not as an entirely new element. A prefix to
     * each of the values can be given to distinguish between column identifiers
     * in cases where multiples are saved in one element.
     */
    private void saveColumnIdentifier(PrintWriter out, ColumnIdentifier identifier, String namePrefix) {
        if (identifier == null) return;
        if (identifier instanceof ColumnNameColumnIdentifier) {
            printAttribute(namePrefix + "column-name", ((ColumnNameColumnIdentifier) identifier).getColumnName());
        } else if (identifier instanceof PositionColumnIdentifier) {
            PositionColumnIdentifier positionIdentifier = ((PositionColumnIdentifier) identifier);
            for (int i = 0; i < positionIdentifier.getUniqueMemberNames().size(); i++) {
                printAttribute(namePrefix + "unique-member-name" + i, positionIdentifier.getUniqueMemberNames().get(i));
            }
        } else if (identifier instanceof RowAxisColumnIdentifier) {
            printAttribute(namePrefix + "axis-ordinal", ((RowAxisColumnIdentifier) identifier).getAxis().axisOrdinal());
        } else {
            throw new IllegalStateException("Column identifier " + identifier + " has no values defined to save.");
        }
    }
	
    /**
     * This method will save an {@link OlapQuery}
     * 
     * @param query
     *      The {@link OlapQuery} to save
     * @param name
     *      A unique name to append to the start of XML tags
     */
	private void saveOlapQuery(OlapQuery query) {
	    
	    xml.print(out, "<olap-query");
        printAttribute("name", query.getName());
        printAttribute("uuid", query.getUUID());
        if (query.getOlapDataSource() != null) {
            printAttribute("data-source", query.getOlapDataSource().getName());
        }
        xml.println(out, ">");
        xml.indent++;
        
        if (query.getCurrentCube()!=null &&
                query.getCurrentCube().getSchema()!=null &&
                query.getCurrentCube().getSchema().getCatalog()!=null) {
            xml.print(out, "<olap-cube");
            printAttribute("catalog", query.getCurrentCube().getSchema().getCatalog().getName());
            printAttribute("schema", query.getCurrentCube().getSchema().getName());
            printAttribute("cube-name", query.getCurrentCube().getName()); //XXX This does not use it's unique name to look up the cube but instead just the name, don't use unique name or it won't find the cube.
            xml.println(out, "/>");
        }
        
        SaveOLAP4jQuery.saveOlap4jQuery(query, xml, out, this);
        
        xml.indent--;
	    xml.println(out, "</olap-query>");
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
		xml.niprintln(out, "/>");
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
	    Query data = cache.getQuery();
		xml.print(out, "<query");
		printAttribute("name", data.getName());
		printAttribute("uuid", data.getUUID());
		printAttribute("zoom", data.getZoomLevel());
		printAttribute("streaming-row-limit", data.getStreamingRowLimit());
		printAttribute("row-limit", data.getRowLimit());
		printAttribute("grouping-enabled", Boolean.toString(data.isGroupingEnabled()));
		printAttribute("prompt-for-cross-joins", cache.getPromptForCrossJoins());
		if (!cache.getPromptForCrossJoins()) {
		    printAttribute("execute-queries-with-cross-joins", cache.getExecuteQueriesWithCrossJoins());
		}
		if (data.getDatabase() != null && data.getDatabase().getDataSource() != null) {
			printAttribute("data-source", data.getDatabase().getDataSource().getName());
		}
		xml.niprintln(out, ">");
		xml.indent++;

		Map<Item, String> itemIdMap = new HashMap<Item, String>();

		xml.print(out, "<constants");
		Container constants = data.getConstantsContainer();
		printAttribute("uuid", constants.getUUID());
		printAttribute("xpos", constants.getPosition().getX());
		printAttribute("ypos", constants.getPosition().getY());
		xml.niprintln(out, ">");
		xml.indent++;
		for (Item item : constants.getItems()) {
			xml.print(out, "<column");
			printAttribute("id", item.getUUID());
			itemIdMap.put(item, item.getUUID());
			printAttribute("name", item.getName());
			printAttribute("alias", item.getAlias());
			printAttribute("where-text", item.getWhere());
			printAttribute("group-by", item.getGroupBy().toString());
			printAttribute("having", item.getHaving());
			printAttribute("order-by", item.getOrderBy().toString());
			xml.niprintln(out, "/>");
		}
		xml.indent--;
		xml.println(out, "</constants>");
		
		for (Container table : data.getFromTableList()) {
			xml.print(out, "<table");
			printAttribute("name", table.getName());
			printAttribute("uuid", table.getUUID());
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
			xml.niprintln(out, ">");
			xml.indent++;
			for (Item item : table.getItems()) {
				xml.print(out, "<column");
				printAttribute("id", item.getUUID());
				itemIdMap.put(item, item.getUUID());
				printAttribute("name", item.getName());
				printAttribute("alias", item.getAlias());
				printAttribute("where-text", item.getWhere());
				printAttribute("group-by", item.getGroupBy().toString());
	            printAttribute("having", item.getHaving());
	            printAttribute("order-by", item.getOrderBy().toString());
				xml.niprintln(out, "/>");
			}
			xml.indent--;
			xml.println(out, "</table>");
		}	
		
		for (SQLJoin join : data.getJoins()) {
			xml.print(out, "<join");
			printAttribute("left-item-id", itemIdMap.get(join.getLeftColumn()));
			printAttribute("left-is-outer", Boolean.toString(join.isLeftColumnOuterJoin()));
			printAttribute("right-item-id", itemIdMap.get(join.getRightColumn())); 
			printAttribute("right-is-outer", Boolean.toString(join.isRightColumnOuterJoin()));
			printAttribute("comparator", join.getComparator()); 
			xml.niprintln(out, "/>");
		}
				
		xml.println(out, "<select>");
		xml.indent++;
		for (Item col : data.getSelectedColumns()) {
			xml.print(out, "<column");
			printAttribute("id", itemIdMap.get(col));
			xml.niprintln(out, "/>");
		}
		xml.indent--;
		xml.println(out, "</select>");
		
		xml.print(out, "<global-where");
		printAttribute("text", data.getGlobalWhereClause());
		xml.niprintln(out, "/>");
		
		for (Item item : data.getOrderByList()) {
			xml.println(out, "<order-by");
			printAttribute("column-id", itemIdMap.get(item));
			xml.niprintln(out, "/>");
		}
		
		if (data.isScriptModified()) {
			xml.print(out, "<query-string");
			printAttribute("string", data.generateQuery());
			xml.niprintln(out, "/>");		
		}

		xml.indent--;
		xml.println(out, "</query>");
	}
	
	/**
	 * Prints an attribute to the file. If the attribute value is null
	 * no attribute will be printed.
	 */
    public void printAttribute(String name, String value) {
        if (value == null) return;
        xml.niprint(out, " " + name + "=\"");
        xml.niprint(out, SQLPowerUtils.escapeXML(value) + "\"");
    }
    
    public void printAttribute(String name, double value) {
    	xml.niprint(out, " " + name + "=\"" + value + "\"");
    }
    
    public void printAttribute(String name, int value) {
        xml.niprint(out, " " + name + "=\"" + value + "\"");
    }

    public void printAttribute(String name, boolean value) {
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
