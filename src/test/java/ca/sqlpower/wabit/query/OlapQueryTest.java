/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Wabit.
 *
 * SQL Power Wabit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Wabit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.wabit.query;

import java.io.File;

import junit.framework.TestCase;

import org.olap4j.Axis;
import org.olap4j.OlapConnection;
import org.olap4j.metadata.Cube;
import org.olap4j.query.Selection.Operator;

import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.util.DefaultUserPrompter;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.wabit.StubWabitSession;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.rs.olap.OlapConnectionPool;
import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.rs.olap.WabitOlapAxis;
import ca.sqlpower.wabit.rs.olap.WabitOlapDimension;
import ca.sqlpower.wabit.rs.olap.WabitOlapInclusion;

public class OlapQueryTest extends TestCase {

	/**
	 * A query for testing purposes. Setup with the one cube in our regression database.
	 */
	private OlapQuery query;
	
	/**
	 * The rows axis of our testing query.
	 */
	private WabitOlapAxis rowAxis;

	@Override
	protected void setUp() throws Exception {
		final PlDotIni ini = new PlDotIni();
		ini.read(new File("src/test/resources/pl.regression.ini"));
		final Olap4jDataSource source = ini.getDataSource("World Facts OLAP Connection", Olap4jDataSource.class);
		final StubWabitSessionContext context = new StubWabitSessionContext() {
			@Override
			public UserPrompter createUserPrompter(String question,
					UserPromptType responseType, UserPromptOptions optionType,
					UserPromptResponse defaultResponseType,
					Object defaultResponse, String... buttonNames) {
				return new DefaultUserPrompter(optionType,
						defaultResponseType, defaultResponse);
			}
		};
		final OlapConnectionPool olapConnectionPool = new OlapConnectionPool(source, context);
		final OlapConnection con = olapConnectionPool.getConnection();
		//At the time of writing this test there is only one cube in the regression file.
		Cube singleCube = con.getCatalogs().get(0).getSchemas().get(0).getCubes().get(0);
		assertTrue(singleCube.getName().startsWith("World Countries"));
		query = new OlapQuery("test-uuid", 
				context, 
				"test cube", 
				"query", 
				singleCube.getSchema().getCatalog().getName(), 
				singleCube.getSchema().getName(), 
				singleCube.getName(), 
				null);
		WabitSession session = new StubWabitSession(context) {
			@Override
			public WabitSessionContext getContext() {
				return context;
			}
		};
		WabitWorkspace workspace = new WabitWorkspace();
		workspace.setSession(session);
		workspace.addOlapQuery(query);
		query.setOlapDataSource(source);
		rowAxis = new WabitOlapAxis(Axis.ROWS);
		query.addAxis(rowAxis);
	}
	
	/**
	 * A simple test that ensures the dimensions and inclusions can be populated.
	 */
	public void testQueryLoadWithInclusion() throws Exception {
		WabitOlapDimension dimension = new WabitOlapDimension("Measures");
		rowAxis.addDimension(dimension);
		WabitOlapInclusion inclusion = new WabitOlapInclusion(Operator.MEMBER, "[Measures].[Population]");
		dimension.addInclusion(inclusion);
		
		
		query.init();
		
		assertEquals(1, rowAxis.getDimensions().size());
		WabitOlapDimension initDimension = rowAxis.getDimensions().get(0);
		assertTrue("measures".equalsIgnoreCase(initDimension.getName()));
		assertEquals(1, initDimension.getInclusions().size());
		WabitOlapInclusion initInclusion = initDimension.getInclusions().get(0);
		System.out.println(initInclusion.getName());
		assertTrue(initInclusion.getName().contains("Population"));
		
	}
	
	/**
	 * Test case for bug 2809. Loading a query which was saved with
	 * an inclusion that no longer exists in the cube backing the query
	 * was throwing an NPE.
	 * <p>
	 * This test adds a fake inclusion to the dimension but on initialization
	 * it should be removed and not throw exceptions.
	 */
	public void testQueryLoadWithMissingInclusion() throws Exception {
		WabitOlapDimension dimension = new WabitOlapDimension("Measures");
		rowAxis.addDimension(dimension);
		WabitOlapInclusion inclusion = new WabitOlapInclusion(Operator.MEMBER, "[Measures].[Population]");
		dimension.addInclusion(inclusion);
		WabitOlapInclusion inclusion2 = new WabitOlapInclusion(Operator.MEMBER, "[Measures].[Dummy]");
		dimension.addInclusion(inclusion2);
		
		query.init();
		
		assertEquals(1, rowAxis.getDimensions().size());
		WabitOlapDimension initDimension = rowAxis.getDimensions().get(0);
		assertTrue("measures".equalsIgnoreCase(initDimension.getName()));
		assertEquals(1, initDimension.getInclusions().size());
		WabitOlapInclusion initInclusion = initDimension.getInclusions().get(0);
		assertTrue(initInclusion.getName().contains("Population"));
	}
	
	/**
	 * Test case for bug 2809. Loading a query which was saved with
	 * an inclusion that no longer exists in the cube backing the query
	 * was throwing an NPE.
	 * <p>
	 * This test adds a fake inclusion to the dimension but on initialization
	 * it should be removed and not throw exceptions. Since the inclusion is the
	 * only thing the dimension includes the dimension should be removed as well.
	 */
	public void testQueryLoadWithMissingInclusionRemovingDimension() throws Exception {
		WabitOlapDimension dimension = new WabitOlapDimension("Measures");
		rowAxis.addDimension(dimension);
		WabitOlapInclusion inclusion = new WabitOlapInclusion(Operator.MEMBER, "[Measures].[Dummy]");
		dimension.addInclusion(inclusion);
		
		query.init();
		
		assertEquals(0, rowAxis.getDimensions().size());
	}
}
