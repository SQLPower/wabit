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

package ca.sqlpower.wabit.dao;

import java.awt.Font;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.olap4j.query.Selection.Operator;

import junit.framework.TestCase;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.query.TableContainer;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.StubWabitSession;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitColumnItem;
import ca.sqlpower.wabit.WabitConstantItem;
import ca.sqlpower.wabit.WabitConstantsContainer;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitJoin;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitTableContainer;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.dao.WabitPersister.DataType;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.WabitOlapAxis;
import ca.sqlpower.wabit.olap.WabitOlapDimension;
import ca.sqlpower.wabit.olap.WabitOlapExclusion;
import ca.sqlpower.wabit.olap.WabitOlapInclusion;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.HorizontalAlignment;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.report.Guide.Axis;
import ca.sqlpower.wabit.report.Page.PageOrientation;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.report.chart.ChartColumn;
import ca.sqlpower.wabit.report.chart.ChartType;
import ca.sqlpower.wabit.report.chart.ColumnRole;
import ca.sqlpower.wabit.report.chart.LegendPosition;

public class WabitSessionPersisterTest extends TestCase {

	private static final Logger logger = Logger
			.getLogger(WabitSessionPersisterTest.class);

	private WabitSessionPersister wsp;

	private WabitPersister targetPersister = new WabitPersister() {

		public void rollback() throws WabitPersistenceException {
			// TODO
		}

		public void removeObject(String parentUUID, String uuid) {
			// TODO
		}

		public void persistProperty(String uuid, String propertyName,
				DataType propertyType, Object newValue)
				throws WabitPersistenceException {
			// TODO
		}

		public void persistProperty(String uuid, String propertyName,
				DataType propertyType, Object oldValue, Object newValue)
				throws WabitPersistenceException {
			// TODO
		}

		public void persistObject(String parentUUID, String type, String uuid,
				int index) throws WabitPersistenceException {
			// TODO
		}

		public void commit() throws WabitPersistenceException {
			// TODO
		}

		public void begin() throws WabitPersistenceException {
			// TODO
		}
	};

	public void setUp() {
		final PlDotIni defaultPlIni = new PlDotIni();
		try {
			defaultPlIni.read(ClassLoader
							.getSystemResourceAsStream("ca/sqlpower/sql/default_database_types.ini"));
			defaultPlIni.read(ClassLoader
							.getSystemResourceAsStream("ca/sqlpower/demodata/example_database.ini"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WabitSessionContext context = new StubWabitSessionContext() {
			@Override
			public UserPrompter createUserPrompter(String question,
					UserPromptType responseType, UserPromptOptions optionType,
					UserPromptResponse defaultResponseType,
					Object defaultResponse, String... buttonNames) {
				fail("Loading the example workspace should not prompt the user, it should just work."
						+ " Prompt was: " + question);
				throw new IllegalStateException();
			}

			@Override
			public DataSourceCollection getDataSources() {
				return defaultPlIni;
			}
		};

		wsp = new WabitSessionPersister(new StubWabitSession(context),
				targetPersister);

	}

	private String generateUUID() {
		return UUID.randomUUID().toString();
	}

	public void testBegin() {
		System.out.println("Testing begin");
		wsp.begin();
	}

	public void testPersistObject() throws WabitPersistenceException {
		System.out.println("Testing persistObject");

		WabitWorkspace workspace = wsp.getWabitSession().getWorkspace();
		final String workspaceUUID = workspace.getUUID();
		final String wdsJdbcUUID = generateUUID();
		final String wdsOlapUUID = generateUUID();
		final String queryUUID = generateUUID();
		final String constantsUUID = generateUUID();
		final String constantItemUUID = generateUUID();
		final String tableUUID = generateUUID();
		final String tableItemUUID = generateUUID();
		final String tableItem2UUID = generateUUID();
		final String joinUUID = generateUUID();
		final String selectedItemUUID = generateUUID();
		final String queryOrderByItemUUID = generateUUID();
		final String olapQueryUUID = generateUUID();
		final String olapAxisUUID = generateUUID();
		final String olapDimensionUUID = generateUUID();
		final String olapInclusionUUID = generateUUID();
		final String olapExclusionUUID = generateUUID();
		final String chartUUID = generateUUID();
		final String chartColumnUUID = generateUUID();
		final String imageUUID = generateUUID();
		final String reportUUID = generateUUID();
		final String templateUUID = generateUUID();
		final String pageUUID = generateUUID();
		final String contentBoxForChartRendererUUID = generateUUID();
		final String contentBoxForCellSetRendererUUID = generateUUID();
		final String contentBoxForImageRendererUUID = generateUUID();
		final String cRendererUUID = generateUUID();
		final String csRendererUUID = generateUUID();
		final String iRendererUUID = generateUUID();
		final String guideUUID = generateUUID();

		int offset = 0;

		// Testing WabitDataSource
		System.out.println("Testing begin.");
		wsp.begin();

		System.out.println("Testing persistObject on WabitDataSource.");
		wsp.persistObject(workspaceUUID, "WabitDataSource", wdsJdbcUUID,
				offset++);
		wsp.persistProperty(wdsJdbcUUID, "name", DataType.STRING,
				"SQL Power Demo Connection");
		wsp.persistObject(workspaceUUID, "WabitDataSource", wdsOlapUUID,
				offset++);
		wsp.persistProperty(wdsOlapUUID, "name", DataType.STRING,
				"World Facts OLAP Connection");

		System.out.println("Testing commit.");
		wsp.commit();

		WabitDataSource wdsJDBC = workspace.findByUuid(wdsJdbcUUID,
				WabitDataSource.class);
		assertNotNull("WabitDataSource JDBC could not be persisted.", wdsJDBC);
		assertEquals("WabitDataSource JDBC has an invalid parent.",
				workspaceUUID, wdsJDBC.getParent().getUUID());
		assertEquals("WabitDataSource JDBC has an invalid name property.",
				"SQL Power Demo Connection", wdsJDBC.getName());

		WabitDataSource wdsOLAP = workspace.findByUuid(wdsOlapUUID,
				WabitDataSource.class);
		assertNotNull("WabitDataSource OLAP could not be persisted.", wdsOLAP);
		assertEquals("WabitDataSource OLAP has an invalid parent.",
				workspaceUUID, wdsOLAP.getParent().getUUID());
		assertEquals("WabitDataSource OLAP has an invalid name property.",
				"World Facts OLAP Connection", wdsOLAP.getName());

		// Testing QueryCache
		System.out.println("Testing begin.");
		wsp.begin();

		System.out.println("Testing persistObject on QueryCache.");
		wsp.persistObject(workspaceUUID, "QueryCache", queryUUID, offset++);

		System.out.println("Testing commit.");
		wsp.commit();

		QueryCache qc = workspace.findByUuid(queryUUID, QueryCache.class);
		assertNotNull("QueryCache could not be persisted.", qc);
		assertEquals("QueryCache has an invalid parent.", workspaceUUID, qc
				.getParent().getUUID());

		// // Testing WabitConstantsContainer
		// System.out.println("Testing begin.");
		// wsp.begin();
		//
		// System.out.println("Testing persistObject on WabitConstantsContainer.");
		// wsp.persistObject(queryUUID, "WabitConstantsContainer",
		// constantsUUID,
		// offset++);
		//
		// System.out.println("Testing commit.");
		// wsp.commit();
		//
		// WabitConstantsContainer wcc = workspace.findByUuid(constantsUUID,
		// WabitConstantsContainer.class);
		// assertNotNull("WabitConstantsContainer could not be persisted.",
		// wcc);
		// assertEquals("WabitConstantsContainer has an invalid parent.",
		// queryUUID, wcc.getParent().getUUID());
		//
		// // Testing WabitConstantItem
		// System.out.println("Testing begin.");
		// wsp.begin();
		//
		// System.out.println("Testing persistObject on WabitConstantItem.");
		// wsp.persistObject(constantsUUID, "WabitConstantItem",
		// constantItemUUID, offset++);
		// wsp.persistProperty(constantItemUUID, "name", DataType.STRING,
		// "current_date");
		//
		// System.out.println("Testing commit.");
		// wsp.commit();
		//
		// WabitConstantItem wci = workspace.findByUuid(constantItemUUID,
		// WabitConstantItem.class);
		// assertNotNull("WabitConstantItem could not be persisted.", wci);
		// assertEquals("WabitConstantItem has an invalid parent.",
		// constantsUUID,
		// wci.getParent().getUUID());
		// assertEquals("WabitConstantItem has an invalid name property.",
		// "current_date", wci.getName());

		// Testing WabitTableContainer
		System.out.println("Testing begin.");
		wsp.begin();

		System.out.println("Testing persistObject on WabitTableContainer.");
		wsp
				.persistObject(queryUUID, "WabitTableContainer", tableUUID,
						offset++);
		wsp.persistProperty(tableUUID, "name", DataType.STRING,
				"test-table-name");
		wsp.persistProperty(tableUUID, "schema", DataType.STRING,
				"test-schema-name");
		wsp.persistProperty(tableUUID, "catalog", DataType.STRING,
				"test-catalog-name");

		System.out.println("Testing commit.");
		wsp.commit();

		WabitTableContainer wtc = workspace.findByUuid(tableUUID,
				WabitTableContainer.class);
		assertNotNull("WabitTableContainer could not be persisted.", wtc);
		TableContainer tableContainer = ((TableContainer) wtc.getDelegate());
		assertEquals("WabitTableContainer has an invalid parent.", queryUUID,
				wtc.getParent().getUUID());
		assertEquals("WabitTableContainer has an invalid name property.",
				"test-table-name", wtc.getName());
		assertEquals("WabitTableContainer has an invalid schema property.",
				"test-schema-name", tableContainer.getSchema());
		assertEquals("WabitTableContainer has an invalid catalog property.",
				"test-catalog-name", tableContainer.getCatalog());

		// // Testing WabitColumnItem
		// System.out.println("Testing begin.");
		// wsp.begin();
		//
		// System.out.println("Testing persistObject on WabitColumnItem.");
		// wsp
		// .persistObject(tableUUID, "WabitColumnItem", tableItemUUID,
		// offset++);
		// wsp.persistProperty(tableItemUUID, "name", DataType.STRING,
		// "test-table-item-name");
		// wsp.persistObject(tableUUID, "WabitColumnItem", tableItem2UUID,
		// offset++);
		// wsp.persistProperty(tableItem2UUID, "name", DataType.STRING,
		// "test-table-item-2-name");
		//
		// System.out.println("Testing commit.");
		// wsp.commit();
		//
		// WabitColumnItem wci = workspace.findByUuid(tableItemUUID,
		// WabitColumnItem.class);
		// assertNotNull("WabitColumnItem could not be persisted.", wci);
		// assertEquals("WabitColumnItem has an invalid parent.", tableUUID, wci
		// .getParent().getUUID());
		// assertEquals("WabitColumnItem has an invalid name property.",
		// "test-table-item-name", wci.getName());
		//
		// WabitColumnItem wci2 = workspace.findByUuid(tableItem2UUID,
		// WabitColumnItem.class);
		// assertNotNull("WabitColumnItem could not be persisted.", wci2);
		// assertEquals("WabitColumnItem has an invalid parent.", tableUUID,
		// wci2
		// .getParent().getUUID());
		// assertEquals("WabitColumnItem has an invalid name property.",
		// "test-table-item-2-name", wci2.getName());

		// // Testing WabitJoin
		// System.out.println("Testing begin.");
		// wsp.begin();
		//
		// System.out.println("Testing persistObject on WabitJoin.");
		// wsp.persistObject(queryUUID, "WabitJoin", joinUUID, offset++);
		// wsp.persistProperty(joinUUID, "LEFT_JOIN_CHANGED",
		// DataType.REFERENCE,
		// tableItemUUID);
		// wsp.persistProperty(joinUUID, "RIGHT_JOIN_CHANGED",
		// DataType.REFERENCE,
		// tableItem2UUID);
		//
		// System.out.println("Testing commit.");
		// wsp.commit();
		//
		// WabitJoin wabitJoin = workspace.findByUuid(joinUUID,
		// WabitJoin.class);
		// SQLJoin sqlJoin = wabitJoin.getDelegate();
		// assertNotNull("WabitJoin could not be persisted.", wabitJoin);
		// assertEquals("WabitJoin has an invalid parent.", queryUUID, wabitJoin
		// .getParent().getUUID());
		// assertEquals("WabitJoin has an invalid LEFT_JOIN_CHANGED property.",
		// tableItemUUID, sqlJoin.getLeftColumn().getUUID());
		// assertEquals("WabitJoin has an invalid RIGHT_JOIN_CHANGED property.",
		// tableItem2UUID, sqlJoin.getRightColumn().getUUID());

		// Testing OlapQuery
		System.out.println("Testing begin.");
		wsp.begin();

		System.out.println("Testing persistObject on OlapQuery");
		wsp.persistObject(workspaceUUID, "OlapQuery", olapQueryUUID, offset++);
		wsp.persistProperty(olapQueryUUID, "name", DataType.STRING,
				"test-olap-name");
		wsp.persistProperty(olapQueryUUID, "queryName", DataType.STRING,
				"test-olap-query-name");
		wsp.persistProperty(olapQueryUUID, "catalogName", DataType.STRING,
				"test-olap-catalog-name");
		wsp.persistProperty(olapQueryUUID, "schemaName", DataType.STRING,
				"test-olap-schema-name");
		wsp.persistProperty(olapQueryUUID, "cubeName", DataType.STRING,
				"test-olap-cube-name");

		System.out.println("Testing commit.");
		wsp.commit();

		OlapQuery olapQuery = workspace.findByUuid(olapQueryUUID,
				OlapQuery.class);
		assertNotNull("OlapQuery could not be persisted.", olapQuery);
		assertEquals("OlapQuery has an invalid parent.", workspaceUUID,
				olapQuery.getParent().getUUID());
		assertEquals("OlapQuery has an invalid queryName property.",
				"test-olap-query-name", olapQuery.getQueryName());
		assertEquals("OlapQuery has an invalid catalogName property.",
				"test-olap-catalog-name", olapQuery.getCatalogName());
		assertEquals("OlapQuery has an invalid schemaName property.",
				"test-olap-schema-name", olapQuery.getSchemaName());
		assertEquals("OlapQuery has an invalid cubeName property.",
				"test-olap-cube-name", olapQuery.getCubeName());

		// Testing WabitOlapAxis
		System.out.println("Testing begin.");
		wsp.begin();

		System.out.println("Testing persistObject on WabitOlapAxis.");
		wsp.persistObject(olapQueryUUID, "WabitOlapAxis", olapAxisUUID,
				offset++);
		wsp.persistProperty(olapAxisUUID, "ordinal", DataType.INTEGER, 1);

		System.out.println("Testing commit.");
		wsp.commit();

		WabitOlapAxis woa = workspace.findByUuid(olapAxisUUID,
				WabitOlapAxis.class);
		assertNotNull("WabitOlapAxis could not be persisted.", woa);
		assertEquals("WabitOlapAxis has an invalid parent.", olapQueryUUID, woa
				.getParent().getUUID());
		assertEquals("WabitOlapAxis has an invalid ordinal property.", 1, woa
				.getOrdinal().axisOrdinal());

		// Testing WabitOlapDimension
		System.out.println("Testing begin.");
		wsp.begin();

		System.out.println("Testing persistObject on WabitOlapDimension.");
		wsp.persistObject(olapAxisUUID, "WabitOlapDimension",
				olapDimensionUUID, offset++);
		wsp.persistProperty(olapDimensionUUID, "name", DataType.STRING,
				"test-olap-dimension-name");

		System.out.println("Testing commit.");
		wsp.commit();

		WabitOlapDimension wod = workspace.findByUuid(olapDimensionUUID,
				WabitOlapDimension.class);
		assertNotNull("WabitOlapDimension could not be persisted.", wod);
		assertEquals("WabitOlapDimension has an invalid parent.", olapAxisUUID,
				wod.getParent().getUUID());
		assertEquals("WabitOlapDimension has an invalid name property.",
				"test-olap-dimension-name", wod.getName());

		// // Testing WabitOlapInclusion
		// System.out.println("Testing begin.");
		// wsp.begin();
		//
		// System.out.println("Testing persistObject on WabitOlapInclusion.");
		// wsp.persistObject(olapDimensionUUID, "WabitOlapInclusion",
		// olapInclusionUUID, offset++);
		// wsp.persistProperty(olapInclusionUUID, "operator", DataType.STRING,
		// Operator.MEMBER.name());
		// wsp.persistProperty(olapInclusionUUID, "uniqueMemberName",
		// DataType.STRING,
		// "[Geography].[World].[Europe].[Western Europe].[Austria]");
		//
		// System.out.println("Testing commit.");
		// wsp.commit();
		//
		// WabitOlapInclusion woi = workspace.findByUuid(olapInclusionUUID,
		// WabitOlapInclusion.class);
		// assertNotNull("WabitOlapInclusion could not be persisted.", woi);
		// assertEquals("WabitOlapInclusion has an invalid parent.",
		// olapDimensionUUID, woi.getParent().getUUID());
		// assertEquals("WabitOlapInclusion has an invalid operator property.",
		// Operator.MEMBER.name(), woi.getOperator().name());
		// assertEquals(
		// "WabitOlapInclusion has an invalid uniqueMemberName property",
		// "[Geography].[World].[Europe].[Western Europe].[Austria]", woi
		// .getUniqueMemberName());
		//
		// // Testing WabitOlapExclusion
		// System.out.println("Testing begin.");
		// wsp.begin();
		//
		// System.out.println("Testing persistObject on WabitOlapExclusion.");
		// wsp.persistObject(olapDimensionUUID, "WabitOlapExclusion",
		// olapExclusionUUID, offset++);
		// wsp.persistProperty(olapExclusionUUID, "operator", DataType.STRING,
		// Operator.MEMBER.name());
		// wsp.persistProperty(olapExclusionUUID, "uniqueMemberName",
		// DataType.STRING,
		// "[Geography].[World].[Europe].[Western Europe].[Austria]");
		//
		// System.out.println("Testing commit.");
		// wsp.commit();
		//
		// WabitOlapExclusion woe = workspace.findByUuid(olapExclusionUUID,
		// WabitOlapExclusion.class);
		// assertNotNull("WabitOlapExclusion could not be persisted.", woe);
		// assertEquals("WabitOlapExclusion has an invalid parent.",
		// olapDimensionUUID, woe.getParent().getUUID());
		// assertEquals("WabitOlapExclusion has an invalid operator property.",
		// Operator.MEMBER.name(), woi.getOperator().name());
		// assertEquals(
		// "WabitOlapExclusion has an invalid uniqueMemberName property.",
		// "[Geography].[World].[Europe].[Western Europe].[Austria]", woi
		// .getUniqueMemberName());

		// Testing Chart
		System.out.println("Testing begin.");
		wsp.begin();

		System.out.println("Testing persistObject on Chart.");
		wsp.persistObject(workspaceUUID, "Chart", chartUUID, offset++);

		System.out.println("Testing commit.");
		wsp.commit();

		Chart chart = workspace.findByUuid(chartUUID, Chart.class);
		assertNotNull("Chart could not be persisted.", chart);
		assertEquals("Chart has an invalid parent.", workspaceUUID, chart
				.getParent().getUUID());

		// Testing ChartColumn
		System.out.println("Testing begin.");
		wsp.begin();

		System.out.println("Testing persistObject on ChartColumn.");
		wsp.persistObject(chartUUID, "ChartColumn", chartColumnUUID, offset++);
		wsp.persistProperty(chartColumnUUID, "name", DataType.STRING,
				"test-chart-column-name");
		wsp.persistProperty(chartColumnUUID, "dataType", DataType.STRING,
				ca.sqlpower.wabit.report.chart.ChartColumn.DataType.TEXT);

		System.out.println("Testing commit.");
		wsp.commit();

		ChartColumn chartColumn = workspace.findByUuid(chartColumnUUID,
				ChartColumn.class);
		assertNotNull("ChartColumn could not be persisted.", chartColumn);
		assertEquals("ChartColumn has an invalid parent.", chartUUID,
				chartColumn.getParent().getUUID());

		// Testing WabitImage
		System.out.println("Testing begin.");
		wsp.begin();

		System.out.println("Testing persistObject on WabitImage.");
		wsp.persistObject(workspaceUUID, "WabitImage", imageUUID, offset++);

		System.out.println("Testing commit.");
		wsp.commit();

		WabitImage wabitImage = workspace.findByUuid(imageUUID,
				WabitImage.class);
		assertNotNull("WabitImage could not be persisted.", wabitImage);
		assertEquals("WabitImage has an invalid parent.", workspaceUUID,
				wabitImage.getParent().getUUID());

		// Testing Template
		System.out.println("Testing begin.");
		wsp.begin();

		System.out.println("Testng persistObject on Template.");
		wsp.persistObject(workspaceUUID, "Template", templateUUID, offset++);
		wsp.persistProperty(templateUUID, "name", DataType.STRING,
				"test-template-name");

		System.out.println("Testing commit.");
		wsp.commit();

		Template template = workspace.findByUuid(templateUUID, Template.class);
		assertNotNull("Template could not be persisted.", template);
		assertEquals("Template has an invalid parent.", workspaceUUID, template
				.getParent().getUUID());
		assertEquals("Template has an invalid name property.",
				"test-template-name", template.getName());

		// Testing Report
		System.out.println("Testing begin.");
		wsp.begin();

		System.out.println("Testing persistObject on Report.");
		wsp.persistObject(workspaceUUID, "Report", reportUUID, offset++);
		wsp.persistProperty(reportUUID, "name", DataType.STRING,
				"test-report-name");

		System.out.println("Testing commit.");
		wsp.commit();

		Report report = workspace.findByUuid(reportUUID, Report.class);
		assertNotNull("Report could not be persisted.", report);
		assertEquals("Report has an invalid parent.", workspaceUUID, report
				.getParent().getUUID());
		assertEquals("Report has an invalid name property.",
				"test-report-name", report.getName());

		// Testing persistProperty
		System.out.println("Testing begin.");
		wsp.begin();

		System.out.println("Testing persistProperty on Report.");
		wsp.persistProperty(reportUUID, "zoomLevel", DataType.INTEGER, 150);

		System.out.println("Testing commit.");
		wsp.commit();

		assertEquals("Report has an invalid zoomLevel property.", 150, report
				.getZoomLevel());

		// // Testing Page
		// System.out.println("Testing begin.");
		// wsp.begin();
		//
		// System.out.println("Testing persistObject on Page.");
		// wsp.persistObject(reportUUID, "Page", pageUUID, offset++);
		// wsp
		// .persistProperty(pageUUID, "name", DataType.STRING,
		// "test-page-name");
		// wsp.persistProperty(pageUUID, "width", DataType.DOUBLE, 10);
		// wsp.persistProperty(pageUUID, "height", DataType.DOUBLE, 15);
		// wsp.persistProperty(pageUUID, "orientation", DataType.STRING,
		// PageOrientation.PORTRAIT.name());
		//
		// System.out.println("Testing commit.");
		// wsp.commit();
		//
		// Page page = workspace.findByUuid(pageUUID, Page.class);
		// assertNotNull("Page could not be persisted.", page);
		// assertEquals("Page has an invalid parent.", reportUUID, page
		// .getParent().getUUID());
		// assertEquals("Page has an invalid name property.", "test-page-name",
		// page.getName());
		// assertEquals("Page has an invalid width property.", 10,
		// page.getWidth());
		// assertEquals("Page has an invalid height property.", 15, page
		// .getHeight());
		// assertEquals("Page has an invalid orientation property.",
		// PageOrientation.PORTRAIT.name(), page.getOrientation().name());
		//
		// // Testing ContentBox
		// System.out.println("Testing begin.");
		// wsp.begin();
		//
		// System.out.println("Testing persistObject on ContentBox.");
		// wsp.persistObject(pageUUID, "ContentBox",
		// contentBoxForChartRendererUUID, offset++);
		// wsp.persistObject(pageUUID, "ContentBox",
		// contentBoxForCellSetRendererUUID, offset++);
		//
		// System.out.println("Testing commit.");
		// wsp.commit();
		//
		// ContentBox contentBoxForChartRenderer = workspace.findByUuid(
		// contentBoxForChartRendererUUID, ContentBox.class);
		// assertNotNull("ContentBox could not be persisted.",
		// contentBoxForChartRenderer);
		// assertEquals("ContentBox has an invalid parent.", pageUUID,
		// contentBoxForChartRenderer.getParent().getUUID());
		//
		// ContentBox contentBoxForCellSetRenderer = workspace.findByUuid(
		// contentBoxForCellSetRendererUUID, ContentBox.class);
		// assertNotNull("ContentBox could not be persisted.",
		// contentBoxForCellSetRenderer);
		// assertEquals("ContentBox has an invalid parent.", pageUUID,
		// contentBoxForCellSetRenderer.getParent().getUUID());
		//
		// // Testing ChartRenderer
		// System.out.println("Testing begin.");
		// wsp.begin();
		//
		// System.out.println("Testing persistObject on ChartRenderer.");
		// wsp.persistObject(contentBoxForChartRendererUUID, "ChartRenderer",
		// cRendererUUID, offset++);
		// wsp.persistProperty(cRendererUUID, "chart", DataType.REFERENCE,
		// chartUUID);
		//
		// System.out.println("Testing commit.");
		// wsp.commit();
		//
		// ChartRenderer cRenderer = workspace.findByUuid(cRendererUUID,
		// ChartRenderer.class);
		// assertNotNull("ChartRenderer could not be persisted.", cRenderer);
		// assertEquals("ChartRenderer has an invalid parent.",
		// contentBoxForChartRendererUUID, cRenderer.getParent().getUUID());
		// assertEquals("ChartRenderer has an invalid chart property.",
		// chartUUID,
		// cRenderer.getChart().getUUID());
		//
		// // Testing CellSetRenderer
		// System.out.println("Testing begin.");
		// wsp.begin();
		//
		// System.out.println("Testing persistObject on CellSetRenderer.");
		// wsp.persistObject(contentBoxForCellSetRendererUUID,
		// "CellSetRenderer",
		// csRendererUUID, offset++);
		// wsp.persistProperty(csRendererUUID, "modifiedOlapQuery",
		// DataType.REFERENCE, olapQueryUUID);
		//
		// System.out.println("Testing commit.");
		// wsp.commit();
		//
		// CellSetRenderer csRenderer = workspace.findByUuid(csRendererUUID,
		// CellSetRenderer.class);
		// assertNotNull("CellSetRenderer could not be persisted.", csRenderer);
		// assertEquals("CellSetRenderer has an invalid parent.",
		// contentBoxForCellSetRendererUUID, csRenderer.getParent()
		// .getUUID());
		// assertEquals(
		// "CellSetRenderer has an invalid modifiedOlapQuery property.",
		// olapQueryUUID, csRenderer.getModifiedOlapQuery().getUUID());
		//
		// // Testing ImageRenderer
		// System.out.println("Testing begin.");
		// wsp.begin();
		//
		// System.out.println("Testing persistObject on ImageRenderer.");
		// wsp.persistObject(contentBoxForImageRendererUUID, "ImageRenderer",
		// iRendererUUID, offset++);
		//
		// System.out.println("Testing commit.");
		// wsp.commit();
		//
		// ImageRenderer iRenderer = workspace.findByUuid(iRendererUUID,
		// ImageRenderer.class);
		// assertNotNull("ImageRenderer could not be persisted.", iRenderer);
		// assertEquals("ImageRenderer has an invalid parent.",
		// contentBoxForImageRendererUUID, iRenderer.getParent().getUUID());
		//
		// // Testing Guide
		// System.out.println("Testing begin.");
		// wsp.begin();
		//
		// System.out.println("Testing persistObject on Guide.");
		// wsp.persistObject(pageUUID, "Guide", guideUUID, offset++);
		// wsp.persistProperty(guideUUID, "axis", DataType.STRING, Axis.VERTICAL
		// .name());
		// wsp.persistProperty(guideUUID, "offset", DataType.DOUBLE, 123.456);
		//
		// System.out.println("Testing commit.");
		// wsp.commit();
		//
		// Guide guide = workspace.findByUuid(guideUUID, Guide.class);
		// assertNotNull("Guide could not be persisted.", guide);
		// assertEquals("Guide has an invalid parent.", pageUUID, guide
		// .getParent().getUUID());
		// assertEquals("Guide has an invalid axis property.", Axis.VERTICAL
		// .name(), guide.getAxis().name());
		// assertEquals("Guide has an invalid offset property.", 123.456, guide
		// .getOffset());

	}

}
