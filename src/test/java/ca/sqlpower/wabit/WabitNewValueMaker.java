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

package ca.sqlpower.wabit;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;

import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;
import org.olap4j.query.Selection.Operator;

import ca.sqlpower.query.Container;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.ItemContainer;
import ca.sqlpower.query.StringItem;
import ca.sqlpower.query.QueryImpl.OrderByArgument;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.sqlobject.StubSQLDatabaseMapping;
import ca.sqlpower.testutil.GenericNewValueMaker;
import ca.sqlpower.wabit.enterprise.client.Grant;
import ca.sqlpower.wabit.enterprise.client.Group;
import ca.sqlpower.wabit.enterprise.client.GroupMember;
import ca.sqlpower.wabit.enterprise.client.ReportTask;
import ca.sqlpower.wabit.enterprise.client.User;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapConnectionPool;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.WabitOlapAxis;
import ca.sqlpower.wabit.olap.WabitOlapDimension;
import ca.sqlpower.wabit.olap.WabitOlapExclusion;
import ca.sqlpower.wabit.olap.WabitOlapInclusion;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.DataType;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.HorizontalAlignment;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.ReportContentRenderer;
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
import ca.sqlpower.wabit.rs.ResultSetProducer;

public class WabitNewValueMaker extends GenericNewValueMaker {
    
    private PlDotIni plIni;
    private OlapConnectionPool connectionPool;
    private WabitWorkspace workspace;
    
    
    public WabitNewValueMaker(WabitWorkspace workspace) {
    	this.workspace = workspace;
    	
        plIni = new PlDotIni();
        try {
            plIni.read(new File("src/test/java/pl.regression.ini"));
            final Olap4jDataSource olapDS = plIni.getDataSource("World Facts OLAP Connection", 
                    Olap4jDataSource.class);
            if (olapDS == null) throw new IllegalStateException("Cannot find 'World Facts OLAP Connection'");
            connectionPool = new OlapConnectionPool(olapDS, 
                    new SQLDatabaseMapping() {
                private final SQLDatabase sqlDB = new SQLDatabase(olapDS.getDataSource());
                public SQLDatabase getDatabase(JDBCDataSource ds) {
                    return sqlDB;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object makeNewValue(Class<?> valueType, Object oldVal, String propName) {
        Object newValue;
        
        if (valueType.equals(WabitObject.class)) {
        	WabitImage image = new WabitImage();
        	workspace.addImage(image);
            newValue = image;
            
        } else if (valueType.equals(WabitDataSource.class)) {
            final JDBCDataSource ds = new JDBCDataSource(new PlDotIni());
            ds.setName("test");
            WabitDataSource wabitDataSource = new WabitDataSource(ds);
            workspace.addDataSource(wabitDataSource);
            newValue = wabitDataSource;
            
        } else if (valueType.equals(QueryCache.class)) {
            QueryCache query = new QueryCache(new SQLDatabaseMapping() {
                public SQLDatabase getDatabase(JDBCDataSource ds) {
                    return null;
                }
            });
            
            workspace.addQuery(query, workspace.getSession());
            newValue = query;
            
        } else if (valueType.equals(Report.class)) {
        	Report report = new Report("testing layout");
            workspace.addReport(report);
            newValue = report;
            
        } else if (valueType.equals(ContentBox.class)) {
        	ContentBox cb = new ContentBox();
        	Report report = (Report) makeNewValue(Report.class, null, "Made for ContentBox");
        	report.getPage().addContentBox(cb);
        	newValue = cb;
        	
        } else if (valueType.equals(ReportContentRenderer.class)) {
        	Label label = new Label();
        	ContentBox cb = (ContentBox) makeNewValue(ContentBox.class, null, "Made for Label");
        	cb.setContentRenderer(label);
        	newValue = label;
        	
        } else if (valueType.equals(Guide.class)) {
        	Guide guide;
            if (oldVal != null) {
                guide = new Guide(Axis.HORIZONTAL, (int) (((Guide) oldVal).getOffset() + 1));
            } else {
                guide = new Guide(Axis.HORIZONTAL, 123);
            }
            Report report = (Report) makeNewValue(Report.class, null, "Made for Guide");
            report.getPage().addGuide(guide);
            newValue = guide;
            
        } else if (valueType.equals(HorizontalAlignment.class)) {
            if (oldVal == HorizontalAlignment.CENTER) {
                newValue = HorizontalAlignment.LEFT;
            } else {
                newValue = HorizontalAlignment.CENTER;
            }
        } else if (valueType.equals(VerticalAlignment.class)) {
            if (oldVal == VerticalAlignment.MIDDLE) {
                newValue = VerticalAlignment.TOP;
            } else {
                newValue = VerticalAlignment.MIDDLE;
            }
        } else if (valueType.equals(PageOrientation.class)) {
            if (oldVal == PageOrientation.PORTRAIT) {
                newValue = PageOrientation.LANDSCAPE;
            } else {
                newValue = PageOrientation.PORTRAIT;
            }
        } else if (valueType.equals(DataType.class)) {
            if (oldVal == DataType.DATE) {
                newValue = DataType.NUMERIC;
            } else {
                newValue = DataType.DATE;
            }
        } else if (valueType.equals(Item.class)) {
        	newValue = new StringItem("item");
        } else if (valueType.equals(Color.class)) {
        	if (oldVal != null) {
        		newValue = new Color(0x224466);
        	} else {
        		newValue = new Color(0x001122);
        	}
        } else if (valueType.equals(BorderStyles.class)) {
        	if (oldVal != null) {
        		newValue = BorderStyles.FULL;
        	} else {
        		newValue = BorderStyles.NONE;
        	}
        } else if (valueType.equals(Container.class)) {
        	if (oldVal != null) {
        		newValue = new ItemContainer("New Item Container");
        	} else {
        		newValue = new ItemContainer("Newer Item Container");
        	}
        } else if (valueType.equals(Item.class)) {
        	if (oldVal != null) {
        		newValue = new StringItem("New String Item");
        	} else {
        		newValue = new StringItem("Newer String Item");
        	}
        } else if (valueType.equals(OlapQuery.class)) {
        	OlapQuery olapQuery = new OlapQuery(new StubWabitSessionContext());
        	workspace.addOlapQuery(olapQuery);
        	newValue = olapQuery;
        	
        } else if (valueType.equals(WabitOlapAxis.class)) { 
        	WabitOlapAxis axis = new WabitOlapAxis(org.olap4j.Axis.ROWS);
        	OlapQuery olapQuery = (OlapQuery) makeNewValue(OlapQuery.class, null, 
        			"Made for WabitOlapAxis");
        	olapQuery.addAxis(axis);
        	newValue = axis;
        	
        } else if (valueType.equals(WabitOlapDimension.class)) {
        	WabitOlapDimension dimension = new WabitOlapDimension("Geography");
        	WabitOlapAxis axis = (WabitOlapAxis) makeNewValue(WabitOlapAxis.class, null, 
        			"Made for WabitOlapDimension");
        	axis.addDimension(dimension);
        	newValue = dimension;
        	
        } else if (valueType.equals(WabitOlapInclusion.class)) {
        	WabitOlapInclusion inclusion = new WabitOlapInclusion(Operator.MEMBER, "[Geography].[World]");
        	WabitOlapDimension dimension = (WabitOlapDimension) makeNewValue(
        			WabitOlapDimension.class, null, "Made for WabitOlapInclusion");
        	dimension.addInclusion(inclusion);
        	newValue = inclusion;
        	
        } else if (valueType.equals(WabitOlapExclusion.class)) {
        	WabitOlapExclusion exclusion = new WabitOlapExclusion(Operator.MEMBER, "[Geography].[World].[Africa]");
        	WabitOlapDimension dimension = (WabitOlapDimension) makeNewValue(
        			WabitOlapDimension.class, null, "Made for WabitOlapExclusion");
        	dimension.addExclusion(exclusion);
        	newValue = exclusion;
        	
        } else if (valueType.equals(WabitImage.class)) {
            WabitImage image = new WabitImage();
            workspace.addImage(image);
            newValue = image;
            
        } else if (valueType.equals(GroupAndBreak.class)) {
            if (oldVal.equals(GroupAndBreak.GROUP)) {
                newValue = GroupAndBreak.BREAK;
            } else {
                newValue = GroupAndBreak.GROUP;
            }
        } else if (valueType.equals(WabitSession.class)) {
            newValue = new StubWabitSession(new StubWabitSessionContext());
            
        } else if (valueType.equals(Chart.class)) {
            Chart chart = new Chart();
            workspace.addChart(chart);
            newValue = chart;
            
        } else if (valueType.equals(Template.class)) {
            Template template = new Template("Some name");
            workspace.addTemplate(template);
            newValue = template;
            
        } else if (valueType.equals(Page.class)) {
        	newValue = ((Report) makeNewValue(Report.class, null, "Made for Page")).getPage();
            
        } else if (valueType.equals(ChartColumn.class)) {
            ChartColumn chartColumn = new ChartColumn("New column", 
                    ca.sqlpower.wabit.report.chart.ChartColumn.DataType.NUMERIC);
            Chart chart = (Chart) makeNewValue(Chart.class, null, "Made for ChartColumn");
            chart.addChartColumn(chartColumn);
            newValue = chartColumn;
            
        } else if (valueType.equals(LegendPosition.class)) {
            if (oldVal != null && oldVal.equals(LegendPosition.LEFT)) {
                newValue = LegendPosition.RIGHT;
            } else {
                newValue = LegendPosition.LEFT;
            }
        } else if (valueType.equals(DecimalFormat.class)) {
            if (oldVal != null && oldVal.equals(new DecimalFormat("##,##"))) {
                newValue = new DecimalFormat("##0#");
            } else {
                newValue = new DecimalFormat("##,##");
            }
        } else if (valueType.equals(ResultSetProducer.class)) {
            QueryCache query = new QueryCache(new StubSQLDatabaseMapping());
            query.setName("New query");
            workspace.addQuery(query, workspace.getSession());
            newValue = query;
            
        } else if (valueType.equals(ColumnRole.class)) {
            if (oldVal != null && oldVal.equals(ColumnRole.CATEGORY)) {
                newValue = ColumnRole.NONE;
            } else {
                newValue = ColumnRole.CATEGORY;
            }
        } else if (valueType.equals(ChartType.class)) {
            if (oldVal != null && oldVal.equals(ChartType.BAR)) {
                newValue = ChartType.PIE;
            } else {
                newValue = ChartType.BAR;
            }
        } else if (valueType.equals(Cube.class)) {
            try {
                newValue = connectionPool.getConnection().getSchema().getCubes().get("World Countries");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (valueType.equals(Member.class)) {
        	try {
        		//This train wreck gets the first member in the first dimension of a basic cube.
				newValue = connectionPool.getConnection().getSchema().getCubes().get("World Countries").
				getDimensions().get(0).getDefaultHierarchy().getDefaultMember();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
        } else if (valueType.equals(User.class)) {
        	User user = new User("New User", "");
        	workspace.addUser(user);
        	newValue = user;
        	
        } else if (valueType.equals(Group.class)) {
        	Group group = new Group("New Group");
        	workspace.addGroup(group);
        	newValue = group;
        	
        } else if (valueType.equals(Grant.class)) {
        	Grant grant = new Grant(null, WabitWorkspace.class.getCanonicalName(), 
        			false, false, false, false, false);
        	Group group = (Group) makeNewValue(Group.class, null, "Made for Grant");
        	group.addGrant(grant);
        	newValue = grant;
        	
        } else if (valueType.equals(ReportTask.class)) {
        	ReportTask reportTask = new ReportTask();
        	workspace.addReportTask(reportTask);
        	newValue = reportTask;
        	
        } else if (valueType.equals(Image.class)) {
        	if (oldVal != null) {
        		newValue = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        	} else {
        		newValue = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        	}
        } else if (valueType.equals(GroupMember.class)) {
        	GroupMember groupMember = new GroupMember(new User("username", "password"));
        	Group group = (Group) makeNewValue(Group.class, null, "Made for GroupMember");
        	group.addMember(groupMember);
        	newValue = groupMember;
        	
        } else if (valueType.equals(OrderByArgument.class)) {
        	if (oldVal.equals(OrderByArgument.ASC)) {
        		newValue = OrderByArgument.DESC;
        	} else {
        		newValue = OrderByArgument.ASC;
        	}
        } else if (valueType.equals(WabitWorkspace.class)) {
        	//The new workspace has to be in the workspace, 
        	//but the workspace cannot have a new workspace in the workspace....?
        	newValue = workspace;
        } else {
            return super.makeNewValue(valueType, oldVal, propName);
        }
        
        return newValue;
    }
}
