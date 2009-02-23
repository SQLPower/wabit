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

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;

/**
 * This DAO will load projects to a context from a given input stream.
 */
public class LoadProjectXMLDAO {

	/**
	 * This context will have new sessions added to it for each project
	 * loaded.
	 */
	private final WabitSessionContext context;
	
	/**
	 * This is the input stream we are loading projects from.
	 */
	private final InputStream in;
	
	public LoadProjectXMLDAO(WabitSessionContext context, InputStream in) {
		this.context = context;
		this.in = in;
	}

	/**
	 * Calling this method will load all of the projects from the input stream
	 * into the context that was given to this class when its constructor was called.
	 */
	public List<WabitSession> loadProjects() {
		SAXParser parser;
		ProjectSAXHandler saxHandler = new ProjectSAXHandler(context);
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(in, saxHandler);
			return saxHandler.getSessions();
		} catch (Exception e) {
			for (WabitSession session : saxHandler.getSessions()) {
				context.deregisterChildSession(session);
			}
			throw new RuntimeException(e);
		}
	}

}
