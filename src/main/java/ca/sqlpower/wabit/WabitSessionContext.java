package ca.sqlpower.wabit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	private final List<WabitSession> childSessions = new ArrayList<WabitSession>();
	
	/**
	 * If this flag is true, this session context will halt the VM when its
	 * last session closes.
	 */
	private boolean terminateWhenLastSessionCloses;
	
	/**
	 * Creates a new Wabit session context.
	 * 
	 * @param terminateWhenLastSessionCloses
	 *            If this flag is true, this session context will halt the VM
	 *            when its last session closes.
	 * @throws IOException
	 *             If the startup configuration files can't be read
	 */
	public WabitSessionContext(boolean terminateWhenLastSessionCloses) throws IOException {
		this.terminateWhenLastSessionCloses = terminateWhenLastSessionCloses;
		dataSources = new PlDotIni();
		dataSources.read(new File(System.getProperty("user.home"), "pl.ini"));
	}

	public DataSourceCollection getDataSources() {
		return dataSources;
	}
	
	/**
	 * Adds the given Wabit session to the list of child sessions for this
	 * context. This is normally done by the sessions themselves, so you
	 * shouldn't need to call this method from your own code.
	 */
	public void registerChildSession(WabitSession child) {
		childSessions.add(child);
	}

	/**
	 * Removes the given Wabit session from the list of child sessions for this
	 * context. This is normally done by the sessions themselves, so you
	 * shouldn't need to call this method from your own code.
	 */
	public void deregisterChildSession(WabitSession child) {
		childSessions.remove(child);
		
		if (terminateWhenLastSessionCloses && childSessions.isEmpty()) {
			System.exit(0);
		}
	}
}