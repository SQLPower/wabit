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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitSessionImpl;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.dao.OpenWorkspaceXMLDAO;
import ca.sqlpower.wabit.dao.WorkspaceXMLDAO;

/**
 * A special kind of session that binds itself to a remote Wabit Enterprise
 * Server. Provides database connection information and file storage capability
 * based on the remote server.
 */
public class WabitServerSession extends WabitSessionImpl {
    
    private static final Logger logger = Logger.getLogger(WabitServerSession.class);
    
    private final HttpClient httpClient;
    private final WabitServerInfo serviceInfo;
    
    public WabitServerSession(WabitServerInfo serviceInfo, WabitSessionContext context) {
        super(context);
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 2000);
        httpClient = new DefaultHttpClient(params);

        this.serviceInfo = serviceInfo;
        if (serviceInfo == null) {
            logger.error("Null pointer Exception");
            throw new NullPointerException("serviceInfo is for the WabitServer is null");
        }
    }

    @Override
    public boolean close() {
        httpClient.getConnectionManager().shutdown();
        return super.close();
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
    public DataSourceCollection<SPDataSource> getDataSources() {
        ResponseHandler<DataSourceCollection<SPDataSource>> plIniHandler = 
            new ResponseHandler<DataSourceCollection<SPDataSource>>() {
            public DataSourceCollection<SPDataSource> handleResponse(HttpResponse response)
                    throws ClientProtocolException, IOException {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException(
                            "Server error while reading data sources: " + response.getStatusLine());
                }
                PlDotIni plIni;
                try {
                    plIni = new PlDotIni(getServerURI(serviceInfo, "/"));
                    plIni.read(response.getEntity().getContent());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                return plIni;
            }
        };
        try {
            return executeServerRequest(httpClient, serviceInfo, "data-sources/", plIniHandler);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * List all the workspaces on this context's server.
     * 
     * @param serviceInfo
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<String> getWorkspaceNames(HttpClient httpClient, WabitServerInfo serviceInfo) throws IOException, URISyntaxException {
        String responseBody = executeServerRequest(httpClient, serviceInfo, "workspace", new BasicResponseHandler());
        logger.debug("Workspace list:\n" + responseBody);
        List<String> workspaces = Arrays.asList(responseBody.split("\r?\n"));
        return workspaces;
    }
    
    private static <T> T executeServerRequest(HttpClient httpClient, WabitServerInfo serviceInfo, 
            String contextRelativePath, ResponseHandler<T> responseHandler)
    throws IOException, URISyntaxException {
        HttpUriRequest request = new HttpGet(getServerURI(serviceInfo, contextRelativePath));
        return httpClient.execute(request, responseHandler);
    }
    
    private static URI getServerURI(WabitServerInfo serviceInfo, String contextRelativePath) throws URISyntaxException {
        logger.debug("Getting server URI for: " + serviceInfo);
        String contextPath = serviceInfo.getPath();
        return new URI("http", null, serviceInfo.getServerAddress(), serviceInfo.getPort(),
                contextPath + contextRelativePath, null, null);
    }
    
    /**
     * Saves the given workspace on this session context's server. The name to
     * save as is determined by the workspace's name.
     * 
     * @param workspace
     *            The workspace to save. Its name determines the name of the
     *            resource saved to the server. If there is already a workspace on
     *            the server with the same name, it will be replaced.
     * @throws IOException
     *             If the upload fails
     * @throws URISyntaxException
     *             If the workspace name can't be properly encoded in a URI
     */
    public static void saveWorkspace(HttpClient httpClient, WabitServerInfo serviceInfo, WabitSessionContext context, WabitWorkspace workspace) throws IOException, URISyntaxException {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WorkspaceXMLDAO dao = new WorkspaceXMLDAO(out, context);
        dao.saveActiveWorkspace();
        out.close(); // has no effect, but feels sensible :)
        
        HttpPost request = new HttpPost(getServerURI(serviceInfo, "workspace/" + workspace.getName()));
        logger.debug("Posting workspace to " + request);
        request.setEntity(new ByteArrayEntity(out.toByteArray()));
        httpClient.execute(request);
        logger.debug("Post complete!");
    }

    /**
     * Opens a workspace based on the server information and a workspace name.
     * The workspace will be created from the given context.
     * 
     * @param serviceInfo
     *            The information that defines the wabit server.
     * @param workspaceName
     *            The name of the workspace being loaded.
     * @param context
     *            The context that will create the session and register it.
     */
    public static void openWorkspace(WabitServerInfo serviceInfo, final String workspaceName, 
            final WabitSessionContext context) throws URISyntaxException, ClientProtocolException, IOException {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 2000);
        HttpClient httpclient = new DefaultHttpClient(params);
        try {
            String contextPath = serviceInfo.getPath();
            URI uri = new URI("http", null, serviceInfo.getServerAddress(), serviceInfo.getPort(),
                    contextPath + "workspace/" + workspaceName, null, null);
            HttpGet httpget = new HttpGet(uri);
            logger.debug("executing request " + httpget.getURI());

            ResponseHandler<Void> responseHandler = new ResponseHandler<Void>() {

                public Void handleResponse(HttpResponse response)
                        throws ClientProtocolException, IOException {

                    if (response.getStatusLine().getStatusCode() != 200) {
                        context.createUserPrompter("Server reported error:\n" + response.getStatusLine() +
                                "\nWhile requesting workspace \""+workspaceName+"\"", UserPromptType.MESSAGE, 
                                UserPromptOptions.OK, UserPromptResponse.OK, true);
                        return null;
                    }
                    try {
                        OpenWorkspaceXMLDAO workspaceLoader = new OpenWorkspaceXMLDAO(
                                context, response.getEntity().getContent(), 0); //TODO set the workspace size correctly
                        workspaceLoader.openWorkspaces();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    return null;
                }
                
            };
            httpclient.execute(httpget, responseHandler);
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }
}
