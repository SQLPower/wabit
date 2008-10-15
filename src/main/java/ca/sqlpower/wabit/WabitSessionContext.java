package ca.sqlpower.wabit;

import java.io.File;
import java.io.IOException;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;

/**
 * A placeholder for all state and behaviour that is shared among
 * Wabit sessions. Every session belongs to a session context, and
 * there is typically one session context in each JVM. However,
 * the limit of one session context is not enforced or required. It's
 * just typical.
 */
public class WabitSessionContext {

	private final DataSourceCollection dataSources;
	
	/**
	 * Creates a new Wabit session context.
	 * 
	 * @throws IOException
	 *             If the startup configuration files can't be read
	 */
	public WabitSessionContext() throws IOException {
		dataSources = new PlDotIni();
		dataSources.read(new File(System.getProperty("user.home"), "pl.ini"));
	}

	public DataSourceCollection getDataSources() {
		return dataSources;
	}
}
