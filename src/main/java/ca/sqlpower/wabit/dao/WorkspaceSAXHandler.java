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
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.query.Selection.Operator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import ca.sqlpower.query.Container;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.Query;
import ca.sqlpower.query.SQLGroupFunction;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.query.SQLObjectItem;
import ca.sqlpower.query.StringItem;
import ca.sqlpower.query.TableContainer;
import ca.sqlpower.query.QueryImpl.OrderByArgument;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompterFactory;
import ca.sqlpower.util.Version;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.WabitOlapAxis;
import ca.sqlpower.wabit.olap.WabitOlapDimension;
import ca.sqlpower.wabit.olap.WabitOlapExclusion;
import ca.sqlpower.wabit.olap.WabitOlapInclusion;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.DataType;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.HorizontalAlignment;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.report.VerticalAlignment;
import ca.sqlpower.wabit.report.ColumnInfo.GroupAndBreak;
import ca.sqlpower.wabit.report.Guide.Axis;
import ca.sqlpower.wabit.report.Page.PageOrientation;
import ca.sqlpower.wabit.report.ResultSetRenderer.BorderStyles;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.report.chart.ChartColumn;
import ca.sqlpower.wabit.report.chart.ChartType;
import ca.sqlpower.wabit.report.chart.ColumnRole;
import ca.sqlpower.wabit.report.chart.LegendPosition;

/**
 * This will be used with a parser to load a saved workspace from a file.
 */
public class WorkspaceSAXHandler extends DefaultHandler {
	
	private static final Logger logger = Logger.getLogger(WorkspaceSAXHandler.class);
	
	/**
	 * This will track the tags and depth of the XML tags.
	 */
	private final Stack<String> xmlContext = new Stack<String>();
	
	/**
	 * This is the session that the workspace will be loaded into.
	 */
	private final WabitSession session;
	
	/**
	 * This is the current query being loaded in from the file.
	 */
	private QueryCache cache;

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
	
	/**
	 * Describes if the loading of the workspace has been cancelled.
	 */
	private AtomicBoolean cancelled = new AtomicBoolean();
    
    /**
     * This message describes where the parser is in the file.
     */
    private String progressMessage = "";
    
	/**
	 * This map stores old DS names used in the loaded workspace to the new DS specified
	 * by the user. Only the data sources that have a new data source specified will
	 * appear in this map.
	 */
	private final Map<String, String> oldToNewDSNames;

	/**
	 * This is the UUID for the editorPanelModel to be set in the loading workspace.
	 * This cannot be set for the workspace until the end of the loading as the workspace's
	 * children will not be loaded at the start tag.
	 */
	private String currentEditorPanelModel;

	/**
	 * This is an {@link OlapQuery} that is currently being loaded from the file. This
	 * may be null if no OlapQuery is currently being loaded. This variable is also used
	 * when loading up the olapQuery in a report.
	 */
    private OlapQuery olapQuery;
    
    /**
     * This is an Olap4j {@link org.olap4j.query.Query} which is currently being
     * loaded. This may be null.
     */
    private CellSetRenderer cellSetRenderer;

    private final UserPrompterFactory promptFactory;
    
    /**
     * This is the current WabitImage being loaded.
     */
    private WabitImage currentWabitImage;

    /**
     * The chart currently being read from the XML stream. This will be null
     * unless we are within a &lt;chart&gt; element.
     */
    private Chart chart;

    /**
     * Gets set to true when inside a missing-columns element.
     */
    private boolean readingMissingChartCols;

    private boolean nameMandatory;

    private boolean uuidMandatory;

	private WabitOlapAxis olapAxis;

	private WabitOlapDimension olapDimension;

	private String catalogName;

	private String schemaName;

	private String cubeName;

	private Olap4jDataSource olapDataSource;

	private String olapName;

	private String olapID;
	
    /**
     * Creates a new SAX handler which is capable of reading in a series of
     * workspace descriptions from an XML stream. The list of workspaces
     * encountered in the stream become available as a Wabit Session.
     * <p>
     * This constructor must be called from the foreground thread
     * 
     * @param context The context that will create sessions for loading and
     * creates user prompters if input is required.
     */
	public WorkspaceSAXHandler(WabitSessionContext context) {
		this(context, null);
	}

	/**
	 * Creates a new SAX handler which is capable of reading in a series of
	 * workspace descriptions from an XML stream. The list of workspaces
	 * encountered in the stream become available as a Wabit Session.
	 * <p>
	 * This constructor must be called from the foreground thread
	 * 
	 * @param context
	 *            The context that will create sessions for loading and creates
	 *            user prompters if input is required.
	 * @param serverInfo
	 *            Describes a connection to a server. If this is not null, a
	 *            server session will be created that is connected to a server.
	 */
	public WorkspaceSAXHandler(WabitSessionContext context, WabitServerInfo serverInfo) {
	    this.context = context;
        this.promptFactory = context;
		oldToNewDSNames = new HashMap<String, String>();
		setCancelled(false);
	    if (serverInfo == null) {
            session = context.createSession();
        } else {
            session = context.createServerSession(serverInfo);
        }
	}

	@Override
	public void startElement(final String uri, final String localName, final String name,
			final Attributes attr) throws SAXException {
		if (isCancelled()) {
		    throw new CancellationException();
		}
		byteStream = new ByteArrayOutputStream();
		final Attributes attributes = new AttributesImpl(attr);
		Runnable runner = new Runnable() {
			public void run() {
				try {
					context.startLoading();
					startElementImpl(uri, localName, name, attributes);
				} catch (SAXException e) {
					setCancelled(true);
					throw new RuntimeException(e);
				} finally {
					context.endLoading();
				}
			}
		};
		session.runInForeground(runner);
	}
	
    /**
     * Throws a {@link CancellationException} if either the loading of the file
     * was cancelled by a method call or cancelled internally due to a problem
     * in the file. If there was a problem with the file this method will notify
     * the user and another notification does not need to be sent.
     * <p>
     * 
     * @see WorkspaceXMLDAO#FILE_VERSION
     */
	private void startElementImpl(final String uri, final String localName, final String name,
			Attributes attributes) throws SAXException {
		if (isCancelled()) {
			return;
		}

		xmlContext.push(name);

		final WabitObject createdObject;
		
		if (name.equals("wabit")) {
		    createdObject = null;
		    
		    String versionString = attributes.getValue("export-format");
		    
		    //NOTE: For correct versioning behaviour see WorkspaceXMLDAO.FILE_VERSION.
		    if (versionString == null) {
		        UserPrompter up = promptFactory.createUserPrompter(
		                "This Wabit workspace file is very old. It may not read correctly, but I will try my best.",
		                UserPromptType.MESSAGE, UserPromptOptions.OK, UserPromptResponse.OK,
		                null, "OK");
		        up.promptUser();
		    } else {
		        Version fileVersion = new Version(versionString);
		        Version fileMajorMinorVersion = new Version(fileVersion, 2);
		        Version currentMajorMinorVersion = new Version(WorkspaceXMLDAO.FILE_VERSION, 2);
		        Version fileMajorVersion = new Version(fileVersion, 1);
		        Version currentMajorVersion = new Version(WorkspaceXMLDAO.FILE_VERSION, 1);
		        
		        String message = null;
		        boolean displayMessage = true;
		        if (fileMajorVersion.compareTo(currentMajorVersion) < 0) {
		            message = "The Wabit workspace you are opening is too old to be successfully loaded.\n" +
                            "An older version of Wabit is required to view the saved workspace.";
		            setCancelled(true);
		        } else if (fileVersion.equals(new Version("1.0.0"))) {
		            message = "The Wabit workspace you are opening is an old version that does not record\n" +
		                    "information about page orientation. All pages will default to portrait orientation.";
		        } else if (fileVersion.compareTo(new Version("1.1.0")) < 0) {
		            message = "The Wabit workspace you are opening contains OLAP and/or reports from an.\n" +
		                    "old version of the wabit. These items cannot be loaded and need to be updated\n" +
		                    "to the latest version.";
		        } else if (fileVersion.compareTo(new Version("1.2.0")) < 0) {
		            message = "The Wabit workspace you are opening was created in an older version of Wabit\n" +
		                    "which stored charts within reports rather than sharing them within the Workspace.\n" +
		                    "Your charts will appear as empty boxes; you will have to re-create them.";
		        } else if (fileMajorMinorVersion.compareTo(currentMajorMinorVersion) > 0) {
		            message = "The Wabit workspace you are opening was created in a newer version of Wabit.\n" +
                            "Due to large changes in the file format this file cannot be loaded without updating " +
                            "Wabit.";
                    setCancelled(true);
		        } else if (fileVersion.compareTo(WorkspaceXMLDAO.FILE_VERSION) > 0) {
		            message = "The Wabit workspace you are opening was created in a newer version of Wabit.\n" +
                            "I will attempt to load this workspace but it is recommended to update Wabit\n" +
                            "to the latest version.";
		        } else {
		            displayMessage = false;
		        }
		        
		        if (fileVersion.compareTo(new Version("1.2.5")) >= 0) {
                    nameMandatory = true;
                    uuidMandatory = true;
                } else {
                    nameMandatory = false;
                    uuidMandatory = false;
		        }
		        
		        if (displayMessage) {
		            UserPrompter up = promptFactory.createUserPrompter(
                            message,
                            UserPromptType.MESSAGE, UserPromptOptions.OK, UserPromptResponse.OK,
                            null, "OK");
                    up.promptUser();
		        }
		        if (isCancelled()) throw new CancellationException();
		    }

        } else if (name.equals("project")) {
            createdObject = session.getWorkspace();
            for (int i = 0; i < attributes.getLength(); i++) {
                String aname = attributes.getQName(i);
                String aval = attributes.getValue(i);

                if (aname.equals("editorPanelModel")) {
                    currentEditorPanelModel = aval;
                } else {
                    logger.warn("Unexpected attribute of <project>: " + aname + "=" + aval);
                }
            }
        } else if (name.equals("data-source")) {
        	String dsName = attributes.getValue("name");
        	checkMandatory("name", dsName);
        	
        	progressMessage = session.getWorkspace().getName() + ": loading data source " + dsName;
        	
        	SPDataSource ds = context.getDataSources().getDataSource(dsName);
        	if (ds == null) {
        		List<Class<? extends SPDataSource>> dsTypes = new ArrayList<Class<? extends SPDataSource>>();
        		dsTypes.add(JDBCDataSource.class);
        		dsTypes.add(Olap4jDataSource.class);
        		//Note: the new prompt here is so that on the server side the user still has the
        		//option of creating a new datasource
        		UserPrompter prompter = promptFactory.createDatabaseUserPrompter(
        				"The data source \"" + dsName + "\" does not exist. Please select a replacement.", 
        				dsTypes, UserPromptOptions.OK_NEW_NOTOK_CANCEL,
        				UserPromptResponse.NOT_OK, null, context.getDataSources(), "Select Data Source", 
        				"New...", "Skip Data Source", "Cancel Load");
        		
        		UserPromptResponse response = prompter.promptUser();
        		if (response == UserPromptResponse.OK || response == UserPromptResponse.NEW) {
        			ds = (SPDataSource) prompter.getUserSelectedResponse();
        			createdObject = new WabitDataSource(ds);
        			if (!session.getWorkspace().dsAlreadyAdded(ds)) {
        				session.getWorkspace().addDataSource(ds);
        			}
        			oldToNewDSNames.put(dsName, ds.getName());
        		} else if (response == UserPromptResponse.NOT_OK) {
        			ds = null;
        			createdObject = null;
        		} else {
        			setCancelled(true);
        			createdObject = null;
        		}
        	} else if (!session.getWorkspace().dsAlreadyAdded(ds)) {
        		session.getWorkspace().addDataSource(ds);
        		createdObject = new WabitDataSource(ds);
        	} else {
        	    createdObject = null;
        	}
        } else if (name.equals("query")) {
        	cache = new QueryCache(session.getContext(), false);
        	createdObject = cache;
        	
        	String queryName = attributes.getValue("name");
        	cache.setName(queryName);
        	progressMessage = session.getWorkspace().getName() + " : loading query " + queryName;
        	
        	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("uuid")) {
        			// already loaded
        		} else if (aname.equals("name")) {
        			// already loaded
        		} else if (aname.equals("data-source")) { 
        			JDBCDataSource ds = session.getWorkspace().getDataSource(aval, JDBCDataSource.class);
        			if (ds == null) {
        				String newDSName = oldToNewDSNames.get(aval);
        				if (newDSName != null) {
        					ds = session.getWorkspace().getDataSource(newDSName, JDBCDataSource.class);
        					if (ds == null) {
        						logger.debug("Data source " + aval + " is not in the workspace. Attempted to replace with new data source " + newDSName + ". Query " + aname + " was connected to it previously.");
        						throw new NullPointerException("Data source " + newDSName + " was not found in the workspace.");
        					}
        				}
        				logger.debug("Workspace has data sources " + session.getWorkspace().getDataSources());
        			}
        			cache.setDataSourceWithoutReset(ds);
        		} else if (aname.equals("zoom")) {
        			cache.setZoomLevel(Integer.parseInt(aval));
        		} else if (aname.equals("streaming-row-limit")) {
        			cache.setStreamingRowLimit(Integer.parseInt(aval));
        		} else if (aname.equals("row-limit")) {
        		    cache.setRowLimit(Integer.parseInt(aval));
        		} else if (aname.equals("grouping-enabled")) {
        		    cache.setGroupingEnabled(Boolean.parseBoolean(aval));
        		} else if (aname.equals("prompt-for-cross-joins")) {
        		    cache.setPromptForCrossJoins(Boolean.parseBoolean(aval));
        		} else if (aname.equals("execute-queries-with-cross-joins")) {
        		    cache.setExecuteQueriesWithCrossJoins(Boolean.parseBoolean(aval));
        		} else if (aname.equals("automatically-executing")) {
        		    cache.setAutomaticallyExecuting(Boolean.parseBoolean(aval));
        		} else {
        			logger.warn("Unexpected attribute of <query>: " + aname + "=" + aval);
        		}
        	}
        	session.getWorkspace().addQuery(cache, session);
        } else if (name.equals("constants")) {
            createdObject = null;
        	String uuid = attributes.getValue("uuid");
        	checkMandatory("uuid", uuid);
        	cache.getConstantsContainer().setUUID(uuid);
        	Container constants = cache.getConstantsContainer();
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
            createdObject = null;
        	String tableName = attributes.getValue("name");
        	String schema = attributes.getValue("schema");
        	String catalog = attributes.getValue("catalog");
        	String uuid = attributes.getValue("uuid");
        	checkMandatory("uuid", uuid);
        	checkMandatory("name", tableName);
        	TableContainer table = new TableContainer(uuid, cache.getDatabase(), tableName, schema, catalog, new ArrayList<SQLObjectItem>());
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
            createdObject = null;
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
            		} else if (aname.equals("group-by")) {
            		    item.setGroupBy(SQLGroupFunction.valueOf(aval));
            		} else if (aname.equals("having")) {
            		    item.setHaving(aval);
            		} else if (aname.equals("order-by")) {
            		    item.setOrderBy(OrderByArgument.valueOf(aval));
            		} else {
            			logger.warn("Unexpected attribute of <constant-column>: " + aname + "=" + aval);
            		}
            	}
            	cache.getConstantsContainer().addItem(item);
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
            		} else if (aname.equals("group-by")) {
                        item.setGroupBy(SQLGroupFunction.valueOf(aval));
                    } else if (aname.equals("having")) {
                        item.setHaving(aval);
                    } else if (aname.equals("order-by")) {
                        item.setOrderBy(OrderByArgument.valueOf(aval));
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
        		cache.selectItem(uuidToItemMap.get(uuid));
        	} else {
        		throw new IllegalStateException("A column is being loaded that is not contained by any tables. Parent is " + xmlContext.get(xmlContext.size() - 2));
        	}
        } else if (name.equals("join")) {
            createdObject = null;
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
        	cache.addJoin(join);
        } else if (name.equals("select")) {
            createdObject = null;
        	// Select portion loaded in the "column" part above.
        } else if (name.equals("global-where")) {
            createdObject = null;
        	cache.setGlobalWhereClause(attributes.getValue("text"));
        } else if (name.equals("group-by-aggregate")) { // For backwards compatibility to Wabit 0.9.6 and older
            createdObject = null;
        	String uuid = attributes.getValue("column-id");
        	String aggregate = attributes.getValue("aggregate");
        	checkMandatory("column-id", uuid);
        	checkMandatory("aggregate", aggregate);
        	Item item = uuidToItemMap.get(uuid);
        	if (item == null) {
        		throw new IllegalStateException("Could not get a column for grouping. Trying to match UUID " + uuid);
        	}
        	cache.setGroupingEnabled(true);
        	item.setGroupBy(SQLGroupFunction.getGroupType(aggregate));
        } else if (name.equals("having")) { // For backwards compatibility to Wabit 0.9.6 and older
            createdObject = null;
        	String uuid = attributes.getValue("column-id");
        	String text = attributes.getValue("text");
        	checkMandatory("column-id", uuid);
        	checkMandatory("text", text);
        	Item item = uuidToItemMap.get(uuid);
        	if (item == null) {
        		throw new IllegalStateException("Could not get a column to add a having filter. Trying to match UUID " + uuid);
        	}
        	cache.setGroupingEnabled(true);
        	item.setHaving(text);
        } else if (name.equals("order-by")) {
            createdObject = null;
        	String uuid = attributes.getValue("column-id");
        	checkMandatory("column-id", uuid);
         	Item item = uuidToItemMap.get(uuid);
        	if (item == null) {
        		throw new IllegalStateException("Could not get a column to add order by to the select statement. Trying to match UUID " + uuid);
        	}
        	for (int i = 0; i < attributes.getLength(); i++) {
                String aname = attributes.getQName(i);
                String aval = attributes.getValue(i);
                if (aname.equals("column-id")) {
                    //already loaded.
                } if (aname.equals("direction")) {// For backwards compatibility to Wabit 0.9.6 and older
                    item.setOrderBy(OrderByArgument.valueOf(aval));
                } else {
                    logger.warn("Unexpected attribute of <order-by>: " + aname + " = " + aval);
                }
        	}
        	//Reinserting the items for cases where when the items were first created they defined a sort
        	//order and were placed in the query in an incorrect order to sort the columns in.
        	cache.moveOrderByItemToEnd(item);
        } else if (name.equals("query-string")) {
            createdObject = null;
        	String queryString = attributes.getValue("string");
        	checkMandatory("string", queryString);
        	cache.setUserModifiedQuery(queryString);
        } else if (name.equals("text") && parentIs("query")) {
            createdObject = null;
        } else if (name.equals("olap-query")) {
        	olapName = attributes.getValue("name");
        	olapID = attributes.getValue("uuid");
        	String dsName = attributes.getValue("data-source");
			olapDataSource = session.getWorkspace().getDataSource(dsName, Olap4jDataSource.class);
            if (olapDataSource == null) {
                String newDSName = oldToNewDSNames.get(dsName);
                if (newDSName != null) {
                    olapDataSource = session.getWorkspace().getDataSource(newDSName, Olap4jDataSource.class);
                    if (olapDataSource == null) {
                        logger.debug("Data source " + dsName + " is not in the workspace or was not of the correct type. Attempted to replace with new data source " + newDSName + ". Query " + "data-source" + " was connected to it previously.");
                        throw new NullPointerException("Data source " + newDSName + " was not found in the workspace or was not an Olap4j Datasource.");
                    }
                }
                logger.debug("Workspace has data sources " + session.getWorkspace().getDataSources());
            }
            createdObject = null;
        } else if (name.equals("olap-cube")) {
            catalogName = attributes.getValue("catalog");
            schemaName = attributes.getValue("schema");
            cubeName = attributes.getValue("cube-name");
            createdObject = null;
        } else if (name.equals("olap4j-query")) {
            olapQuery = new OlapQuery(olapID, session.getContext(), attributes.getValue("name"), attributes.getValue("name"), catalogName, schemaName, cubeName);
            olapQuery.setName(olapName);
            olapQuery.setOlapDataSource(olapDataSource);
            if (cellSetRenderer == null) {
            	session.getWorkspace().addOlapQuery(olapQuery);
            } else {
                cellSetRenderer.setModifiedOlapQuery(olapQuery);
            }
            createdObject = null;
        } else if (name.equals("olap4j-axis")) {
        	olapAxis = new WabitOlapAxis(org.olap4j.Axis.Factory.forOrdinal(Integer.parseInt(attributes.getValue("ordinal"))));
            olapQuery.addAxis(olapAxis);
            createdObject = olapAxis;
        } else if (name.equals("olap4j-dimension")) {
        	olapDimension = new WabitOlapDimension(attributes.getValue("dimension-name"));
            olapAxis.addDimension(olapDimension);
            createdObject = olapDimension;
        } else if (name.equals("olap4j-selection")) {
        	WabitOlapInclusion olapInclusion = new WabitOlapInclusion(
					Operator.valueOf(attributes.getValue("operator")), 
					attributes.getValue("unique-member-name"));
            olapDimension.addInclusion(olapInclusion);
            createdObject = olapInclusion;
        } else if (name.equals("olap4j-exclusion")) {
        	WabitOlapExclusion olapExclusion = new WabitOlapExclusion(
					Operator.valueOf(attributes.getValue("operator")), 
					attributes.getValue("unique-member-name"));
            olapDimension.addExclusion(olapExclusion);
            createdObject = olapExclusion;
        } else if (name.equals("wabit-image")) {
            currentWabitImage = new WabitImage();
            createdObject = currentWabitImage;
            session.getWorkspace().addImage(currentWabitImage);
            
            for (int i = 0; i < attributes.getLength(); i++) {
                String aname = attributes.getQName(i);
                String aval = attributes.getValue(i);
                if (aname.equals("name")) {
                    //already loaded
                } else {
                    logger.warn("Unexpected attribute of <wabit-image>: " + aname + "=" + aval);
                }
            }
            
        } else if (name.equals("chart")) {
            chart = new Chart();
            createdObject = chart;
            session.getWorkspace().addChart(chart);
            for (int i = 0; i < attributes.getLength(); i++) {
                String aname = attributes.getQName(i);
                String aval = attributes.getValue(i);
                if (aname.equals("uuid")) {
                    //already loaded
                } else if (aname.equals("y-axis-name")) {
                    chart.setYaxisName(aval);
                } else if (aname.equals("x-axis-name")) {
                    chart.setXaxisName(aval);
                } else if (aname.equals("x-axis-label-rotation")) {
                    chart.setXAxisLabelRotation(Double.parseDouble(aval));
                } else if (aname.equals("type")) {
                    chart.setType(ChartType.valueOf(aval));
                } else if (aname.equals("legend-position")) {
                    chart.setLegendPosition(LegendPosition.valueOf(aval));
                } else if (aname.equals("gratuitous-animation")) {
                    chart.setGratuitouslyAnimated(Boolean.parseBoolean(aval));
                } else if (aname.equals("query-id")) {
                    QueryCache query = null;
                    for (QueryCache q : session.getWorkspace().getQueries()) {
                        if (q.getUUID().equals(aval)) {
                            query = q;
                            break;
                        }
                    }
                    if (query != null) {
                        try {
                            chart.setQuery(query);
                        } catch (SQLException e) {
                            throw new RuntimeException("Error loading project while on chart " + chart.getName(), e);
                        }
                    }
                    OlapQuery olapQuery = null;
                    for (OlapQuery q : session.getWorkspace().getOlapQueries()) {
                        if (q.getUUID().equals(aval)) {
                            olapQuery = q;
                            break;
                        }
                    }
                    if (olapQuery != null) {
                        try {
                            chart.setQuery(olapQuery);
                        } catch (SQLException e) {
                            throw new RuntimeException("Error loading project while on chart renderer " + chart.getName(), e);
                        }
                    }
                    if (query == null && olapQuery == null) {
                        throw new IllegalArgumentException("The query with UUID " + aval + " is missing from this project.");
                    }
                } else {
                    logger.warn("Unexpected attribute of <chart>: " + aname + "=" + aval);
                }
            }

        } else if (name.equals("chart-column")) {
            ChartColumn colIdentifier = loadColumnIdentifier(attributes, "");
            createdObject = colIdentifier;
            if (colIdentifier == null) {
                throw new IllegalStateException("The chart " + chart.getName() + " with uuid " + chart.getUUID() + " has a missing column identifier when ordering columns and cannot be loaded.");
            }
            
            for (int i = 0; i < attributes.getLength(); i++) {
                String aname = attributes.getQName(i);
                String aval = attributes.getValue(i);
                if (aname.equals("name")) {
                    //already handled
                } else if (aname.equals("role")) {
                    colIdentifier.setRoleInChart(ColumnRole.valueOf(aval));
                } else if (aname.matches("x-axis-.*")) {
                    ChartColumn xAxisIdentifier = loadColumnIdentifier(attributes, "x-axis-");
                    colIdentifier.setXAxisIdentifier(xAxisIdentifier);
                }
            }
            
            if (readingMissingChartCols) {
                chart.addMissingIdentifier(colIdentifier);
            } else {
                chart.addChartColumn(colIdentifier);
            }
        
        } else if (name.equals("missing-columns")) {
            readingMissingChartCols = true;
            createdObject = null;

        } else if (name.equals("layout")) {
    		String layoutName = attributes.getValue("name");
    		checkMandatory("name", layoutName);
    		if (attributes.getValue("template") == null || !Boolean.parseBoolean(attributes.getValue("template"))) {
    			layout = new Report(layoutName);
    			session.getWorkspace().addReport((Report) layout);
    		} else {
    			layout = new Template(layoutName);
    			session.getWorkspace().addTemplate((Template) layout);
    		}
    		createdObject = layout;
    		
    		
    		
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
        	Page page = layout.getPage();
        	createdObject = page;
        	//Remove all guides from the page as they will be loaded in a later
        	//part of this handler.
        	for (WabitObject object : page.getChildren()) {
        		if (object instanceof Guide) {
        			page.removeGuide((Guide) object);
        		}
        	}
			
			//This sets the orientation before setting the width and height to prevent
			//a change in the orientation from switching the width and height. If the
			//orientation changes between portrait and landscape the width and height
			//values are swapped.
			String orientation = attributes.getValue("orientation");
			if (orientation != null) {
			    // XXX the null check is for compatibility with export-format 1.0.0
			    page.setOrientation(PageOrientation.valueOf(orientation));
			}
			
         	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("name")) {
        			// already loaded
        		} else if (aname.equals("height")) {
        			page.setHeight(Integer.parseInt(aval));
        		} else if (aname.equals("width")) {
        			page.setWidth(Integer.parseInt(aval));
        		} else if (aname.equals("orientation")) {
        		    //already loaded
        		} else {
        			logger.warn("Unexpected attribute of <layout-page>: " + aname + "=" + aval);
        		}
        	}
        } else if (name.equals("content-box")) {
        	contentBox = new ContentBox();
        	createdObject = contentBox;
        	layout.getPage().addContentBox(contentBox);
         	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("name")) {
        			// already loaded
        		} else if (aname.equals("width")) {
        			contentBox.setWidth(Double.parseDouble(aval));
        		} else if (aname.equals("height")) {
        			contentBox.setHeight(Double.parseDouble(aval));
        		} else if (aname.equals("xpos")) {
        			contentBox.setX(Double.parseDouble(aval));
        		} else if (aname.equals("ypos")) {
        			contentBox.setY(Double.parseDouble(aval));
        		} else {
        			logger.warn("Unexpected attribute of <content-box>: " + aname + "=" + aval);
        		}
         	}
        } else if (name.equals("content-label")) {
        	Label label = new Label();
        	createdObject = label;
        	contentBox.setContentRenderer(label);
         	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("name")) {
        		    //handled elsewhere
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
        } else if (name.equals("text") && parentIs("content-label")) {
            createdObject = null;
        } else if (name.equals("image-renderer")) {
        	imageRenderer = new ImageRenderer();
        	createdObject = imageRenderer;
        	//Old image renderers always had the image in the top left. If
        	//the file is new it will have the horizontal and vertical alignments
        	//set.
        	imageRenderer.setHAlign(HorizontalAlignment.LEFT);
        	imageRenderer.setVAlign(VerticalAlignment.TOP);
        	
        	contentBox.setContentRenderer(imageRenderer);
         	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("name")) {
        			//Handled elsewhere
        		} else if (aname.equals("wabit-image-uuid")) {
        		    for (WabitImage image : session.getWorkspace().getImages()) {
        		        if (image.getUUID().equals(aval)) {
        		            imageRenderer.setImage(image);
        		            break;
        		        }
        		    }
        		    if (imageRenderer.getImage() == null) {
        		        throw new IllegalStateException("Could not load the workspace as the report " + layout.getName() 
        		                + " is missing the image " + aval);
        		    }
        		} else if (aname.equals("preserving-aspect-ratio")) {
        			imageRenderer.setPreservingAspectRatio(
        					Boolean.valueOf(aval));
        		} else if (aname.equals("h-align")) {
        		    imageRenderer.setHAlign(HorizontalAlignment.valueOf(aval));
        		} else if (aname.equals("v-align")) {
        		    imageRenderer.setVAlign(VerticalAlignment.valueOf(aval));
        		} else {
        			logger.warn("Unexpected attribute of <image-renderer>: " + aname + "=" + aval);
        		}
         	}
         	
        } else if (name.equals("chart-renderer")) {
            String chartUuid = attributes.getValue("chart-uuid");
            Chart chart = session.getWorkspace().findByUuid(chartUuid, Chart.class);
            if (chart == null) {
                throw new IllegalStateException(
                        "Missing chart with UUID " + chartUuid + ", which is supposed" +
                        " to be attached to a chart renderer");
            }
            
            final ChartRenderer chartRenderer = new ChartRenderer(chart);
            createdObject = chartRenderer;
            contentBox.setContentRenderer(chartRenderer);
            for (int i = 0; i < attributes.getLength(); i++) {
                String aname = attributes.getQName(i);
                String aval = attributes.getValue(i);
                if (aname.equals("uuid")) {
                    // handled elsewhere
                } else if (aname.equals("chart-uuid")) {
                    // already handled
                } else if (aname.equals("name")) {
                    // handled elsewhere
                } else {
                    logger.warn("Unexpected attribute of <chart-renderer>: " + aname + "=" + aval);
                }
            }
            
        } else if (name.equals("content-result-set")) {
        	String queryID = attributes.getValue("query-id");
        	checkMandatory("query-id", queryID);
        	QueryCache query = session.getWorkspace().findByUuid(queryID, QueryCache.class);
        	rsRenderer = new ResultSetRenderer(query);
        	createdObject = rsRenderer;
         	for (int i = 0; i < attributes.getLength(); i++) {
        		String aname = attributes.getQName(i);
        		String aval = attributes.getValue(i);
        		if (aname.equals("query-id")) {
        			// handled elsewhere
        		} else if (aname.equals("name")) {
        			// handled elsewhere
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
			//columnInfoList.clear();
         	contentBox.setContentRenderer(rsRenderer);
        } else if (name.equals("header-font")) {
        	if (parentIs("content-result-set")) {
        		rsRenderer.setHeaderFont(loadFont(attributes));
        		createdObject = null;
        	} else {
        		throw new IllegalStateException("There are no header fonts defined for the parent " + xmlContext.get(xmlContext.size() - 2));
        	}
        } else if (name.equals("body-font")) {
        	if (parentIs("content-result-set")) {
        	    createdObject = null;
        		rsRenderer.setBodyFont(loadFont(attributes));
        	} else {
        		throw new IllegalStateException("There are no body fonts defined for the parent " + xmlContext.get(xmlContext.size() - 2));
        	}
        } else if (name.equals("column-info")) {
        	colInfo = null;
        	createdObject = null; //Not going to set name later, as this may break alias association
        	String colInfoName = attributes.getValue("name");
        	String colInfoItem = attributes.getValue("column-info-item-id");
        	
        	//For backwards compatability with 0.9.1
        	String colInfoKey = attributes.getValue("column-info-key");
        	if (colInfoKey != null && colInfoItem == null) {
        		Query q = rsRenderer.getContent();
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
        		} else if (aname.equals("uuid")){
        		    colInfo.setUUID(aval);  
        		} else if (aname.equals("width")) {
        			colInfo.setWidth(Integer.parseInt(aval));
        		} else if (aname.equals("horizontal-align")) {
        			colInfo.setHorizontalAlignment(HorizontalAlignment.valueOf(aval));
        		} else if (aname.equals("data-type")) {
        			colInfo.setDataType(DataType.valueOf(aval));
        		} else if (aname.equals("break-on-column")) {
        		    if (Boolean.parseBoolean(aval)) {
        		        colInfo.setWillGroupOrBreak(GroupAndBreak.GROUP);
        		    } else {
        		        colInfo.setWillGroupOrBreak(GroupAndBreak.NONE);
        		    }
        		} else if (aname.equals("group-or-break")) {
        		    colInfo.setWillGroupOrBreak(GroupAndBreak.valueOf(aval));
        		} else if (aname.equals("will-subtotal")) {
        			colInfo.setWillSubtotal(Boolean.parseBoolean(aval));
        		}else {
        			logger.warn("Unexpected attribute of <column-info>: " + aname + "=" + aval);
        		}
        	}
        	rsRenderer.addChild(colInfo, rsRenderer.getChildren().size());
        } else if (name.equals("date-format")) {
            createdObject = null;
        	if (parentIs("column-info")) {
        		String format = attributes.getValue("format");
        		checkMandatory("format", format);
        		colInfo.setFormat(new SimpleDateFormat(format));
        	} else {
        		throw new IllegalStateException("There is no date format defined for the parent " + xmlContext.get(xmlContext.size() - 2));
        	}
        } else if (name.equals("decimal-format")) {
            createdObject = null;
        	if (parentIs("column-info")) {
        		String format = attributes.getValue("format");
        		checkMandatory("format", format);
        		colInfo.setFormat(new DecimalFormat(format));
        	} else {
        		throw new IllegalStateException("There is no date format defined for the parent " + xmlContext.get(xmlContext.size() - 2));
        	}
        } else if (name.equals("cell-set-renderer")) {
            String uuid = attributes.getValue("uuid");
            String queryUUID = attributes.getValue("olap-query-uuid");
            OlapQuery newQuery = null;
            for (OlapQuery query : session.getWorkspace().getOlapQueries()) {
                if (query.getUUID().equals(queryUUID)) {
                    newQuery = query;
                    break;
                }
            }
            if (newQuery == null) {
                throw new NullPointerException("Cannot load workspace due to missing olap query in report.");
            }
            cellSetRenderer = new CellSetRenderer(newQuery);
            createdObject = cellSetRenderer;
            contentBox.setContentRenderer(cellSetRenderer);
            for (int i = 0; i < attributes.getLength(); i++) {
                String aname = attributes.getQName(i);
                String aval = attributes.getValue(i);
                if (aname.equals("uuid") || aname.equals("olap-query-uuid")) {
                    // handled elsewhere
                } else if (aname.equals("name")) {
                    // handled elsewhere
                } else if (aname.equals("body-alignment")) {
                    cellSetRenderer.setBodyAlignment(HorizontalAlignment.valueOf(aval));
                } else if (aname.equals("body-format-pattern")) {
                    cellSetRenderer.setBodyFormat(new DecimalFormat(aval));
                } else {
                    logger.warn("Unexpected attribute of <cell-set-renderer>: " + aname + "=" + aval);
                }
            }
            
        } else if (name.equals("olap-header-font")) {
            createdObject = null;
            cellSetRenderer.setHeaderFont(loadFont(attributes));
        } else if (name.equals("olap-body-font")) {
            createdObject = null;
            cellSetRenderer.setBodyFont(loadFont(attributes));
        } else if (name.equals("guide")) {
        	String axisName = attributes.getValue("axis");
        	String offsetAmount = attributes.getValue("offset");
        	checkMandatory("axis", axisName);
        	checkMandatory("offset", offsetAmount);
        	Guide guide = new Guide(Axis.valueOf(axisName), Double.parseDouble(offsetAmount));
        	createdObject = guide;
        	layout.getPage().addGuide(guide);
        } else if (name.equals("font")) {
            createdObject = null;
        	Font font = loadFont(attributes);
        	if (parentIs("layout-page")) {
        		layout.getPage().setDefaultFont(font);
        	} else if (parentIs("content-box")) {
        		contentBox.setFont(font);
        	} else if (parentIs("content-label")) {
        		((Label) contentBox.getContentRenderer()).setFont(font);
        	}
        } else {
            createdObject = null;
            logger.warn("Unknown object type: " + name);
        }
		
		if (createdObject != null){
		    String valName = attributes.getValue("name");
		    String valUUID = attributes.getValue("uuid");
            if (nameMandatory) {
                checkMandatory("name", valName);
            }
            if (uuidMandatory) {
                checkMandatory("uuid", valUUID);
            }
            if (valName != null) {
                createdObject.setName(valName);
		    }
            if (valUUID != null) {
                createdObject.setUUID(valUUID);
            }
		    
		    progressMessage = session.getWorkspace().getName() + ": reading " + valName;
		}
		
	}

    /**
     * This is a helper method for loading {@link ChartColumn}s introduced
     * in the save version 1.0.2 (updated in 1.2.0). Column identifiers are
     * defined by either a column name in a relational query, a row hierarchy
     * for OLAP queries, or a Position of the column axis of an OLAP query.
     * 
     * @param attributes
     *            the attributes of the start tag that introduces this column
     *            identifier
     * @param prefix
     *            the prefix string to apply to every attribute name when
     *            looking up its value. This is intended for use when more than
     *            one column identifier is defined in attributes of a single XML element.
     * @throws SAXException if any mandatory attributes are missing
     */
    private ChartColumn loadColumnIdentifier(Attributes attributes, String prefix)
    throws SAXException {
        
        String colName = attributes.getValue(prefix + "name");
        String dataTypeName = attributes.getValue(prefix + "data-type");
        checkMandatory("name", colName);
        checkMandatory("data-type", dataTypeName);
        ChartColumn.DataType dataType = ChartColumn.DataType.valueOf(dataTypeName);
        ChartColumn colIdentifier = new ChartColumn(colName, dataType);
        return colIdentifier;
    }
	
	/**
	 * This method finds a member from a cube based on given attributes.
	 */
	public Member findMember(Attributes attributes, Cube cube) {
	    String uniqueMemberName = attributes.getValue("unique-member-name");
	    if (uniqueMemberName != null) {
	        String[] uniqueMemberNameList = uniqueMemberName.split("\\]\\.\\[");
            uniqueMemberNameList[0] = uniqueMemberNameList[0].substring(1); //remove starting [ bracket
            final int lastMemberNamePosition = uniqueMemberNameList.length - 1;
            uniqueMemberNameList[lastMemberNamePosition] = uniqueMemberNameList[lastMemberNamePosition].substring(0, uniqueMemberNameList[lastMemberNamePosition].length() - 1); //remove ending ] bracket
	        try {
                return cube.lookupMember(uniqueMemberNameList);
            } catch (OlapException e) {
                throw new RuntimeException(e);
            }
	        
	    } else {
	        String dimensionName = attributes.getValue("dimension-name");
	        String hierarchyName = attributes.getValue("hierarchy-name");
	        String levelName = attributes.getValue("member-level");
	        String memberName = attributes.getValue("member-name");
	        Dimension dimension = cube.getDimensions().get(dimensionName);
	        Member actualMember = null;
	        final Hierarchy hierarchy = dimension.getHierarchies().get(hierarchyName);
	        final Level level = hierarchy.getLevels().get(levelName);
	        try {
	            for (Member member : level.getMembers()) {
	                if (member.getName().equals(memberName)) {
	                    actualMember = member;
	                    break;
	                }
	            }
	        } catch (OlapException e) {
	            throw new RuntimeException(e);
	        }
	        if (actualMember == null) {
	            throw new NullPointerException("Cannot find member " + memberName + " in hierarchy " + hierarchyName + " in dimension " + dimensionName);
	        }
	        return actualMember;
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
    public void endElement(final String uri, final String localName, final String name)
    		throws SAXException {
    	if (isCancelled()) throw new CancellationException();
    	
    	final ByteArrayOutputStream copyStream;
    	if (byteStream != null) {
    		logger.debug("Byte stream contains " + byteStream.size() + " bytes.");
    		copyStream = new ByteArrayOutputStream(byteStream.size());
    		try {
    			byteStream.writeTo(copyStream);
    		} catch (IOException ex) {
    			logger.error(ex);
    			throw new CancellationException("Could not copy the stream. See the logs for more details.");
    		}
    	} else {
    		copyStream = null;
    	}
    	Runnable runner = new Runnable() {
			public void run() {
				try {
					context.startLoading();
					endElementImpl(uri, localName, name, copyStream);
				} catch (SAXException e) {
					setCancelled(true);
					throw new RuntimeException(e);
				} finally {
					context.endLoading();
				}
		
			}
		};
		session.runInForeground(runner);
    }
    
    private void endElementImpl(final String uri, final String localName, final String name, 
    		ByteArrayOutputStream stream)
    		throws SAXException {
    	if (isCancelled()) return;
    	
    	if (name.equals("project")) {
    	    WabitObject initialView = session.getWorkspace();
    		for (WabitObject obj : session.getWorkspace().getChildren()) {
    			if (obj.getUUID().equals(currentEditorPanelModel)) {
    				initialView = obj;
    				break;
    			}
    		}
    		//XXX uncomment the code below when we are confident that Wabit runs all
    		//of its queries and other intensive operations on a separate thread.
    		//See bug 2040.
    		session.getWorkspace().setEditorPanelModel(session.getWorkspace());
//    		session.getWorkspace().setEditorPanelModel(initialView);
    		
    	} else if (name.equals("table")) {
    		TableContainer table = new TableContainer(container.getUUID(), cache.getDatabase(),
    		        container.getName(), ((TableContainer) container).getSchema(), 
    		        ((TableContainer) container).getCatalog(), containerItems);
    		table.setPosition(container.getPosition());
    		table.setAlias(container.getAlias());
    		cache.addTable(table);
    	} else if (name.equals("image-renderer")) {
    	    //This was loading an image for 1.1.2 and older.
    		byte[] byteArray = new Base64().decode(stream.toByteArray());
    		if (byteArray.length > 0) {
    		    logger.debug("Decoding byte stream: Stream has " + stream.toString().length() + " and array has " + Arrays.toString(byteArray));
    		    try {
    		        BufferedImage img = ImageIO.read(new ByteArrayInputStream(byteArray));
    		        WabitImage wabitImage = new WabitImage();
    		        wabitImage.setImage(img);
    		        wabitImage.setName(imageRenderer.getName());
    		        session.getWorkspace().addImage(wabitImage);
    		        imageRenderer.setImage(wabitImage);
    		    } catch (IOException e) {
    		        throw new RuntimeException(e);
    		    }
    		}
			imageRenderer = null;
			
    	} else if (name.equals("wabit-image")) {
            byte[] byteArray = new Base64().decode(stream.toByteArray());
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(byteArray));
                currentWabitImage.setImage(img);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            currentWabitImage = null;
            
        } else if (name.equals("missing-columns")) {
            readingMissingChartCols = false;
            
        } else if (name.equals("chart")) {
        	//XXX Fix for broken references in chart columns. The chart columns x axis references
        	//are actual new chart columns and therefore have the wrong uuid. The chart column
        	//uuids should be stored and used to reference each other instead of name.
        	for (ChartColumn column : chart.getColumns()) {
        		if (column.getXAxisIdentifier() != null) {
        			for (ChartColumn otherColumn : chart.getColumns()) {
        				if (column.getXAxisIdentifier().getColumnName().equals(otherColumn.getColumnName())) {
        					column.setXAxisIdentifier(otherColumn);
        					break;
        				}
        			}
        		}
        	}
            
        } else if (name.equals("text") && parentIs("content-label")) {
            ((Label) contentBox.getContentRenderer()).setText(stream.toString());
            
        } else if (name.equals("text") && parentIs("query")) {
            cache.setUserModifiedQuery(stream.toString());
        }
    	
    	xmlContext.pop();
    }
    
    @Override
    public void characters(char[] ch, int start, int length)
    		throws SAXException {
        if (isCancelled()) throw new CancellationException();
        
        for (int i = start; i < start+length; i++) {
        	byteStream.write((byte)ch[i]);
        }
        if (logger.isDebugEnabled()) {
        	logger.debug("Starting characters at " + start + " and ending at " + length);
        	logger.debug("Byte stream has " + byteStream.toString());
        }
    }

	public WabitSession getSession() {
		return session;
	}
	
    public String getMessage() {
        return progressMessage;
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled.set(cancelled);
    }

}
