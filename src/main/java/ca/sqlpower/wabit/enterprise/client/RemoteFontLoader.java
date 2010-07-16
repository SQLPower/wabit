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

/**
 * 
 */
package ca.sqlpower.wabit.enterprise.client;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import ca.sqlpower.dao.session.SPFontLoader;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.wabit.FontStyle;

/**
 * This class is able to load fonts from a Wabit server. It has a shared
 * static cache among instances so it only fetches fonts only once.
 */
public class RemoteFontLoader implements SPFontLoader {
	
	private final static Map<String, Font> fontCache = new ConcurrentHashMap<String, Font>();
	private final SPServerInfo serverInfos;
	
	private final ResponseHandler<byte[]> handler = new ResponseHandler<byte[]>() {
	    public byte[] handleResponse(
	            HttpResponse response) throws ClientProtocolException, IOException {
	        HttpEntity entity = response.getEntity();
	        if (entity != null) {
	            return EntityUtils.toByteArray(entity);
	        } else {
	            return null;
	        }
	    }
	};
	
	public RemoteFontLoader(SPServerInfo serverInfos) {
		this.serverInfos = serverInfos;
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		synchronized (fontCache) {
			for (Font currentFont : ge.getAllFonts()) {
				if (!fontCache.containsKey(currentFont.getFontName())) {
					fontCache.put(currentFont.getFontName(), currentFont);
				}
			}
		}
	}
	
	/**
	 * Loads a font from a specs String as the ones used in
	 * the persisters. Usually something like: Arial-BOLD-10.
	 * @param fontSpecs The font specs.
	 * @return A Font object corresponding to the one asked for.
	 */
	public Font loadFontFromSpecs(String fontSpecs) {
		
		Pattern pattern = Pattern.compile("^(.+?)(?:[\\-]{1}([A-Z]+))?(?:[\\-]{1}([0-9]+))?$");
		Matcher matcher = pattern.matcher(fontSpecs);
		boolean matchFound = matcher.find(); 
		if (!matchFound) { 
			throw new IllegalArgumentException("The font specs passed cannot be parsed.");
		}

		final String fontName = matcher.group(1);
		final String fontFace = matcher.group(2);
		final String fontSize = matcher.group(3);
		
		Font font = loadFontFromName(fontName);
		if (fontFace != null) {
			font = font.deriveFont(FontStyle.valueOf(fontFace).getValue());
		}
		if (fontSize != null) {
			font = font.deriveFont(Float.valueOf(fontSize));
		}
		
		return font;
	}
	
	/* (non-Javadoc)
	 * @see ca.sqlpower.wabit.dao.session.SPFontLoader#loadFontFromName(java.lang.String)
	 */
	public Font loadFontFromName(final String fontName) {
		
		// Null font names are possible. This means we return the 
		// system default.
		if (fontName == null) {
			return Font.decode("Arial");
		}
		
		// Check the cache.
		synchronized (fontCache) {
			
			if (fontCache.containsKey(fontName)) {
				return fontCache.get(fontName);
			}
			
			// We gotta load it from the server.
			
			try {
				
				// Find the server proper URL
				URL serverUrl = 
					toServerFontURL(
						serverInfos.getServerAddress(), 
						String.valueOf(serverInfos.getPort()), 
						serverInfos.getPath(), 
						URLEncoder.encode(fontName, "utf-8"));
				
				// Create a client
				HttpParams params = new BasicHttpParams();
		        HttpConnectionParams.setConnectionTimeout(params, 2000);
		        DefaultHttpClient httpClient = new DefaultHttpClient(params);
		        httpClient.setCookieStore(WabitClientSession.getCookieStore());
		        httpClient.getCredentialsProvider().setCredentials(
		            new AuthScope(serverUrl.getHost(), AuthScope.ANY_PORT), 
		            new UsernamePasswordCredentials(serverInfos.getUsername(), serverInfos.getPassword()));
		    
		        // Execute the query and parse the response.
		        HttpUriRequest request = new HttpGet(serverUrl.toURI());
		        
		        ByteArrayInputStream bais = 
		        	new ByteArrayInputStream(
	        			httpClient.execute(
        					request,
        					handler));

		        Font font = null;
		        try {	
					font = Font.createFont(
							Font.TRUETYPE_FONT, 
							bais);
				} catch (Exception e) {
					try {
						font = Font.createFont(
								Font.TYPE1_FONT, 
								bais);
					} catch (Exception e2) {
						throw new IOException(e2);
					}
				}
		        
				// Make sure we register it so the PDFs work fine.
				GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
				
				// done
	    		fontCache.put(fontName, font);
	    		return font;
	    		
			} catch (Exception e) {
				throw new RuntimeException("Failed to load a font from the server.", e);
			}
		}
	}
	
	private URL toServerFontURL(
			String host,
			String port,
			String path,
			String fontName) throws MalformedURLException
	{
		// Build the base URL
		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		sb.append(host);
		sb.append(":");
		sb.append(port);
		sb.append(path);
		sb.append(path.endsWith("/")?"fonts/":"/fonts/");
		sb.append(fontName);
		
		// Spawn a connection object
		return new URL(sb.toString());
	}
}