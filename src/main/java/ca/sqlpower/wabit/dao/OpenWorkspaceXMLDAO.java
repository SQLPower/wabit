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

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;

/**
 * This DAO will load workspaces to a context from a given input stream.
 */
public class OpenWorkspaceXMLDAO {

    private static final Logger logger = Logger.getLogger(OpenWorkspaceXMLDAO.class);

	/**
	 * This context will have new sessions added to it for each workspace
	 * loaded.
	 */
	private final WabitSessionContext context;
	
	/**
	 * This is the input stream we are loading workspaces from.
	 */
	private final InputStream in;
	
	public OpenWorkspaceXMLDAO(WabitSessionContext context, InputStream in) {
		this.context = context;
		this.in = in;
	}

	/**
	 * Calling this method will load all of the workspaces from the input stream
	 * into the context that was given to this class when its constructor was called.
	 */
	public List<WabitSession> openWorkspaces() {
		SAXParser parser;
		WorkspaceSAXHandler saxHandler = new WorkspaceSAXHandler(context);
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(in, saxHandler);
			return saxHandler.getSessions();
		} catch (Exception e) {
		    try {
		        for (WabitSession session : saxHandler.getSessions()) {
		            context.deregisterChildSession(session);
		        }
		    } catch (Exception ex) {
		        //Logging this exception as it is hiding the underlying exception.
		        logger.error(ex);
		    }
			throw new RuntimeException(e);
		}
	}
	
	/**
     * Calling this method will import all of the workspaces from the input stream
     * into the session given.
     */
    public void importWorkspaces(WabitSession session) {
        SAXParser parser;
        WorkspaceSAXHandler saxHandler = new WorkspaceSAXHandler(context, session);
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(in, saxHandler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
