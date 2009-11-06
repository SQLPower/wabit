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

package ca.sqlpower.wabit.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

/**
 * A custom ReponseHandler that, if it receives an HTTP 500 status (Internal
 * Server Error), then it will retrieve the server stacktrace in the reponse
 * entity (Wabit Server will be implemented to take server stacktraces and place
 * them in the response). This way, it will make it easier for client users to
 * report errors to us to fix in the server.
 */
public class WabitHttpResponseHandler implements ResponseHandler<String> {

	public String handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		StatusLine statusLine = response.getStatusLine();
		HttpEntity entity = response.getEntity();
		String entityString = EntityUtils.toString(entity);
		// Attempt to get the Server stacktrace and display that
		if (statusLine.getStatusCode() == 500) {
			throw new HttpResponseException(statusLine.getStatusCode(),
					entityString);
		} else if (statusLine.getStatusCode() >= 300) {
			throw new HttpResponseException(statusLine.getStatusCode(),
					statusLine.getReasonPhrase());
		}

		return entity == null ? null : entityString;
	}

}
