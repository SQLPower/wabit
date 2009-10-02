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

package ca.sqlpower.wabit.dao.json;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import ca.sqlpower.wabit.dao.HttpMessageSender;
import ca.sqlpower.wabit.dao.WabitPersistenceException;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;

/**
 * An {@link HttpMessageSender} implementation that specifically sends it's
 * message content in the JSON format. (see <a
 * href="http://www.json.org">www.json.org</a>).
 */
public class JSONHttpMessageSender extends HttpMessageSender<JSONObject> {

	public JSONHttpMessageSender(WabitServerInfo serverInfo,
			String wabitWorkspaceUUID) {
		super(serverInfo, wabitWorkspaceUUID);
	}

	public void send(JSONObject content) throws WabitPersistenceException {
		String uuid = null;
		
		try {
			uuid = content.getString("uuid");
			URI serverURI = getServerURI();
			HttpUriRequest request = null;
			HttpPost postRequest = new HttpPost(serverURI);
			postRequest.setEntity(new StringEntity(content.toString()));
			postRequest.setHeader("Content-Type", "application/json");
			request = postRequest;
	        getHttpClient().execute(request);
		} catch (URISyntaxException e) {
			throw new WabitPersistenceException(uuid, e);
		} catch (ClientProtocolException e) {
			throw new WabitPersistenceException(uuid, e);
		} catch (IOException e) {
			throw new WabitPersistenceException(uuid, e);
		} catch (JSONException e) {
			throw new WabitPersistenceException(uuid, e);
		}
	}
}
