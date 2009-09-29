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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;

/**
 * An abstract {@link MessagePasser} implementation that specifically
 * transmits messages using the HTTP protocol.
 *
 * @param <T> An Object type that represents the content of the message
 */
public abstract class HttpMessagePasser<T> implements MessagePasser<T> {

	/**
	 * An {@link HttpClient} object that will be used to send HTTP requests
	 */
	private HttpClient httpClient;
	
	/**
	 * A {@link WabitServerInfo} instance containing information on a given
	 * server necessary to send HTTP requests to it
	 */
	private WabitServerInfo serverInfo;

	/**
	 * The UUID of the {@link WabitWorkspace} that this MessagePasser will be
	 * sending HTTP requests about
	 */
	private String workspaceUUID;

	/**
	 * Creates an HttpMessagePasser configured to send HTTP requests according
	 * to the settings contained in the {@link WabitServerInfo} parameter. All
	 * HTTP requests will be made regarding the WabitWorkspace with the given
	 * UUID.
	 * 
	 * @param serverInfo
	 *            A {@link WabitServerInfo} instance containing the connection
	 *            information for the HTTP server that the HTTP requests will be
	 *            sent to
	 * @param wabitWorkspaceUUID
	 *            The UUID that identifies the {@link WabitWorkspace} that the
	 *            HTTP requests will be referring to
	 */
	public HttpMessagePasser(WabitServerInfo serverInfo, String wabitWorkspaceUUID) {
		HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 2000);
        httpClient = new DefaultHttpClient(params);
		this.serverInfo = serverInfo;
		workspaceUUID = wabitWorkspaceUUID;
	}
	
    protected URI getServerURI() throws URISyntaxException {
        String contextPath = serverInfo.getPath();
        return new URI("http", null, serverInfo.getServerAddress(), serverInfo.getPort(),
                contextPath + "workspaces/" + workspaceUUID, null, null);
    }

    /**
     * Returns the {@link HttpClient} to be used to send HTTP requests
     */
	protected HttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * Returns a {@link WabitServerInfo} instance containing the connection
	 * information of the HTTP server that HTTP request will be sent to
	 */
	protected WabitServerInfo getServerInfo() {
		return serverInfo;
	}
}
