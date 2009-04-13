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

package ca.sqlpower.wabit.enterprise.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.jmdns.ServiceInfo;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.wabit.WabitSessionContextImpl;

/**
 * A special kind of session context that binds itself to a remote Wabit
 * Enterprise Server. Provides database connection information and file storage
 * capability based on the remote server.
 */
public class WabitServerSessionContext extends WabitSessionContextImpl {

    private HttpClient httpClient = new DefaultHttpClient();
    private final ServiceInfo serviceInfo;
    
    public WabitServerSessionContext(ServiceInfo serviceInfo, boolean terminateWhenLastSessionCloses)
            throws IOException, SQLObjectException {
        super(terminateWhenLastSessionCloses);
        this.serviceInfo = serviceInfo;
    }

    @Override
    public void close() {
        httpClient.getConnectionManager().shutdown();
        super.close();
    }
    
    /**
     * Retrieves the data source list from the server.
     * <p>
     * Future plans: In the future, the server will probably be a proxy for all
     * database operations, and we won't actually send the connection
     * information to the client. This has the advantage that it can work over
     * an HTTP firewall or proxy, where the present method would fail.
     */
    @Override
    public DataSourceCollection getDataSources() {
        ResponseHandler<DataSourceCollection> plIniHandler = new ResponseHandler<DataSourceCollection>() {
            public DataSourceCollection handleResponse(HttpResponse response)
                    throws ClientProtocolException, IOException {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException(
                            "Server error while reading data sources: " + response.getStatusLine());
                }
                PlDotIni plIni = new PlDotIni();
                plIni.read(response.getEntity().getContent());
                return plIni;
            }
        };
        try {
            return executeServerRequest("data-sources/", plIniHandler);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private <T> T executeServerRequest(String contextRelativePath, ResponseHandler<T> responseHandler)
    throws IOException, URISyntaxException {
        HttpUriRequest request = new HttpGet(getServerURI(contextRelativePath));
        return httpClient.execute(request, responseHandler);
    }
    
    private URI getServerURI(String contextRelativePath) throws URISyntaxException {
        String contextPath = serviceInfo.getPropertyString("path");
        return new URI("http", null, serviceInfo.getHostAddress(), serviceInfo.getPort(),
                contextPath + contextRelativePath, null, null);
    }
}
