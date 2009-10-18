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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.swing.SwingUtilities;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitSessionImpl;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.dao.MessageSender;
import ca.sqlpower.wabit.dao.WabitPersistenceException;
import ca.sqlpower.wabit.dao.WabitSessionPersister;
import ca.sqlpower.wabit.dao.json.JSONHttpMessageSender;
import ca.sqlpower.wabit.dao.json.WabitJSONMessageDecoder;
import ca.sqlpower.wabit.dao.json.WabitJSONPersister;
import ca.sqlpower.wabit.dao.session.WorkspacePersisterListener;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;

/**
 * A special kind of session that binds itself to a remote Wabit Enterprise
 * Server. Provides database connection information and file storage capability
 * based on the remote server.
 */
public class WabitServerSession extends WabitSessionImpl {
    
    private static final Logger logger = Logger.getLogger(WabitServerSession.class);
    
    
    
    private final Updater updater;
    
    private final static Map<WabitServerInfo, WabitSession> systemWorkspaces = new ConcurrentHashMap<WabitServerInfo, WabitSession>();

    /**
     * This workspace's location information.
     */
	private final WorkspaceLocation workspaceLocation;

	/**
	 * They system workspace on the server this session is attached to.
	 * This system workspace is shared among all the Wabit workspaces that
	 * come from the same server.
	 */
	private final WabitWorkspace systemWorkspace;

	private final HttpClient outboundHttpClient;
	
	/**
	 * Handles output Wabit persistence calls for this WabitServerSession
	 */
	private final WabitJSONPersister jsonPersister;

	/**
	 * Applies Wabit persistence calls coming from a Wabit server to this WabitServerSession
	 */
	private final WabitSessionPersister sessionPersister;
	
    public WabitServerSession(
    		@Nonnull WorkspaceLocation workspaceLocation,
    		@Nonnull WabitWorkspace systemWorkspace,
    		@Nonnull WabitSessionContext context) {
        super(context);
		this.workspaceLocation = workspaceLocation;
		this.systemWorkspace = systemWorkspace;
        if (workspaceLocation == null) {
        	throw new NullPointerException("workspaceLocation must not be null");
        }

        outboundHttpClient = createHttpClient(workspaceLocation.getServiceInfo());
        
        getWorkspace().setUUID(workspaceLocation.getUuid());
        getWorkspace().setSession(this); // XXX leaking a reference to partially-constructed session!
        
        
        sessionPersister = new WabitSessionPersister(
        		"inbound-" + workspaceLocation.getUuid(),
        		WabitServerSession.this);
        updater = new Updater(workspaceLocation.getUuid(), new WabitJSONMessageDecoder(sessionPersister));
        
        MessageSender<JSONObject> httpSender = new JSONHttpMessageSender(outboundHttpClient, workspaceLocation.getServiceInfo(),
        		workspaceLocation.getUuid());
		jsonPersister = new WabitJSONPersister(httpSender);
    }

	public static HttpClient createHttpClient(WabitServerInfo serviceInfo) {
		HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 2000);
        DefaultHttpClient httpClient = new DefaultHttpClient(params);
        httpClient.getCredentialsProvider().setCredentials(
            new AuthScope(serviceInfo.getServerAddress(), AuthScope.ANY_PORT), 
            new UsernamePasswordCredentials(serviceInfo.getUsername(), serviceInfo.getPassword()));
        return httpClient;
	}

    @Override
    public boolean close() {
        outboundHttpClient.getConnectionManager().shutdown();
        updater.interrupt();
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
                    plIni = new PlDotIni(getServerURI(workspaceLocation.getServiceInfo(), "/"));
                    plIni.read(response.getEntity().getContent());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                return plIni;
            }
        };
        try {
            return executeServerRequest(outboundHttpClient, workspaceLocation.getServiceInfo(), "data-sources/", plIniHandler);
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
     * @throws JSONException 
     */
    public static List<WorkspaceLocation> getWorkspaceNames(WabitServerInfo serviceInfo) throws IOException, URISyntaxException, JSONException {
    	HttpClient httpClient = createHttpClient(serviceInfo);
    	try {
    		HttpUriRequest request = new HttpGet(getServerURI(serviceInfo, "workspaces"));
    		String responseBody = httpClient.execute(request, new BasicResponseHandler());
    		JSONArray response;
    		List<WorkspaceLocation> workspaces = new ArrayList<WorkspaceLocation>();
    		response = new JSONArray(responseBody);
    		logger.debug("Workspace list:\n" + responseBody);
    		for (int i = 0; i < response.length(); i++) {
    			JSONObject workspace = (JSONObject) response.get(i);
    			workspaces.add(new WorkspaceLocation(
    					workspace.getString("name"),
    					workspace.getString("UUID"),
    					serviceInfo));
    		}
    		return workspaces;
    	} finally {
    		httpClient.getConnectionManager().shutdown();
    	}
    }
    
	
	/**
	 * Finds and opens all visible Wabit workspaces on the given Wabit Enterprise Server.
	 * Calling this method essentially constitutes "logging in" to the given server.
	 * 
	 * @param context the context to add the newly-retrieved sessions to
	 * @param serverInfo The server to contact.
	 * @return the list of sessions that were opened.
	 * @throws JSONException 
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public static List<WabitSession> openServerSessions(WabitSessionContext context, WabitServerInfo serverInfo) throws IOException, URISyntaxException, JSONException {
		List<WabitSession> openedSessions = new ArrayList<WabitSession>();
		for (WorkspaceLocation workspaceLoc : WabitServerSession.getWorkspaceNames(serverInfo)) {
			final WabitServerSession session = new WabitServerSession(workspaceLoc, null, context);
			context.registerChildSession(session);
			session.startUpdaterThread();
			openedSessions.add(session);
		}
        return openedSessions;
    }
	
	/**
	 * Returns the system workspace from the given Wabit server. The system
	 * workspace for each server is cached; you will never get multiple system
	 * workspaces for the same server info.
	 * 
	 * @param serverInfo the server from which to retrieve the workspace
	 * @param context The context the system workspace will belong to
	 * @return the system Workspace for the given server
	 */
	public static WabitWorkspace getSystemWorkspace(WabitServerInfo serverInfo, WabitSessionContext context) {
		
		WabitSession session;
		if (systemWorkspaces.containsKey(serverInfo)) {
			session = systemWorkspaces.get(serverInfo);
		} else {
			WorkspaceLocation systemWorkspaceLoc = new WorkspaceLocation("System Workspace", "system", serverInfo);
			session = new WabitServerSession(systemWorkspaceLoc, null, context);
			context.registerChildSession(session);
			systemWorkspaces.put(serverInfo, session);
		}
		return session.getWorkspace();
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

	public void startUpdaterThread() {
		updater.start();
		WorkspacePersisterListener.attachListener(this, jsonPersister, sessionPersister);
	}

	public void persistWorkspaceToServer() throws WabitPersistenceException {
		WorkspacePersisterListener tempListener = new WorkspacePersisterListener(this, jsonPersister);
		tempListener.persistObject(this.getWorkspace());
	}
	
	/**
	 * Polls this session's server for updates until interrupted. There should
	 * be exactly one instance of this class per WabitServerSession.
	 */
	private class Updater extends Thread {
		
		/**
		 * How long we will pause after an update error before attempting to
		 * contact the server again.
		 */
		private long retryDelay = 1000;
		
		private final WabitJSONMessageDecoder jsonDecoder;

		/**
		 * Used by the Updater to handle inbound HTTP updates
		 */
		private final HttpClient inboundHttpClient;
		
		/**
		 * Creates, but does not start, the updater thread.
		 * 
		 * @param workspaceUUID
		 *            the ID of the workspace this updater is responsible for. This is
		 *            used in creating the thread's name.
		 */
		Updater(String workspaceUUID, WabitJSONMessageDecoder jsonDecoder) {
			super("updater-" + workspaceUUID);
			this.jsonDecoder = jsonDecoder;
			inboundHttpClient = createHttpClient(workspaceLocation.getServiceInfo());
		}
        
		@Override
		public void run() {
			logger.info("Updater thread starting");
			
			// the path to contact on the server for update events
			final String contextRelativePath = "workspaces/" + getWorkspace().getUUID();
			
			try {
				for (;;) {
					try {
						final String jsonArray = executeServerRequest(
								inboundHttpClient, workspaceLocation.getServiceInfo(),
								contextRelativePath, new BasicResponseHandler());
		                runInForeground(new Runnable() {
							public void run() {
								try {
									jsonDecoder.decode(jsonArray);
								} catch (WabitPersistenceException e) {
									logger.error("Update from server failed!", e);
									createUserPrompter(
											"Wabit failed to apply an update that was just received from the Enterprise Server.\n"
											+ "The error was:"
											+ "\n" + e.getMessage(),
											UserPromptType.MESSAGE, UserPromptOptions.OK,
											UserPromptResponse.OK, UserPromptResponse.OK, "OK");
									// TODO discard session and reload
								}
							}
						});
					} catch (Exception ex) {
						logger.error("Failed to contact server. Will retry in " + retryDelay + " ms.", ex);
						Thread.sleep(retryDelay);
					}
				}
			} catch (InterruptedException ex) {
				logger.info("Updater thread exiting normally due to interruption.");
			}
			
			inboundHttpClient.getConnectionManager().shutdown();
		}
	}

	public WabitWorkspace getSystemWorkspace() {
		return systemWorkspace;
	}
 
	@Override
	public void runInForeground(Runnable runner) {
		// If we're in a SwingContext, run on the Swing Event Dispatch thread.
		// XXX: This is a bit of a quickfix and I think a better way to possibly fix
		// this could be to have WabitServerSession implement WabitSession, and
		// use a delegate session to delegate most of the server calls (instead
		// of extending WabitSessionImpl). Then if it's in a swing context, it would
		// have a WabitSwingSession instead.
		if (getContext() instanceof WabitSwingSessionContext) {
			SwingUtilities.invokeLater(runner);
		} else {
			super.runInForeground(runner);
		}
	}
}
