package ca.sqlpower.wabit.enterprise.client.security;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.log4j.Logger;

import ca.sqlpower.enterprise.client.security.SPAccessManager.Permission;

/**
 * Profiling has shown that most of the time spent reading a workspace is spent
 * answering JCR's question "is this particular permission granted?".
 */
@ThreadSafe
class GrantCache {

	private static final Logger logger = Logger.getLogger(GrantCache.class);

	@Immutable
	static class CacheKey {

		private final String uuid;
		private final String type;
		private final Set<Permission> permissions;
		
		/**
		 * Initializes all fields. No nullness or validity checks are performed
		 * on any arguments.
		 */
		CacheKey(String uuid, String type, Set<Permission> permissions) {
			this.uuid = uuid;
			this.type = type;
			this.permissions = permissions;
			
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + permissions.hashCode();
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (!permissions.equals(other.permissions))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			if (uuid == null) {
				if (other.uuid != null)
					return false;
			} else if (!uuid.equals(other.uuid))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "CacheKey [uuid=" + uuid + ", type=" + type
					+ ", permissions=" + permissions + "]";
		}

	}
	
	/**
	 * Last time (in System.currentTimeMillis) that this cache was flushed.
	 * Initially set to the creation time of this cache object.
	 */
	private final AtomicLong flushTime = new AtomicLong(System.currentTimeMillis());

	/**
	 * The maximum amount of time (milliseconds) to allow any cache entry to
	 * live before the cache is automatically flushed. The auto-flush is
	 * triggered by the first get or set operation which occurs after this
	 * interval has elapsed since the most recent flush.
	 */
	private final long flushInterval;

	/**
	 * Number of times a get() method on this cache has returned a known value
	 * since the last flush.
	 */
	private final AtomicLong hitsSinceFlush = new AtomicLong(0);

	/**
	 * Number of times a get() method on this cache has said a value is unknown
	 * since the last flush.
	 */
	private final AtomicLong missesSinceFlush = new AtomicLong(0);
	
	private final Map<CacheKey, Boolean> cache = new ConcurrentHashMap<CacheKey, Boolean>();

	/**
	 * Creates a new cache for grant information.
	 * 
	 * @param flushIntervalMillis
	 *            The maximum amount of time (milliseconds) to allow any cache
	 *            entry to live before the cache is automatically flushed. The
	 *            auto-flush is triggered by the first get or set operation
	 *            which occurs after this interval has elapsed since the most
	 *            recent flush.
	 */
	GrantCache(long flushIntervalMillis) {
		this.flushInterval = flushIntervalMillis;
	}
	
	/**
	 * Finds the item in the grant cache which corresponds with the given cache
	 * key values. The key values purposely correspond with the arguments to
	 * {@link AccessManager#isGranted()}.
	 * 
	 * @return
	 */
	public Boolean get(CacheKey key) {
		autoFlush();
		final Boolean result = cache.get(key);
		if (result != null) {
			hitsSinceFlush.incrementAndGet();
		} else {
			missesSinceFlush.incrementAndGet();
		}
		return result;
	}
	
	public void put(CacheKey key, Boolean value) {
		autoFlush();
		cache.put(key, value);
	}

	/**
	 * Flushes this cache if the flush interval has elapsed since the last
	 * flush. Otherwise, has no effect.
	 */
	private void autoFlush() {
		long age = System.currentTimeMillis() - flushTime.get();
		if (age > flushInterval) {
			flush();
		}
	}
	
	public void flush() {
		if (logger.isDebugEnabled()) {
			long age = System.currentTimeMillis() - flushTime.get();
			logger.debug("Flushing cache after its " + age + "ms lifetime. Looking back...");
			logger.debug("   Cache size: " + cache.size() + " items");
			logger.debug("   Cache hits: " + hitsSinceFlush.get());
			logger.debug("   Cache misses: " + missesSinceFlush.get());
		}
		cache.clear();
		hitsSinceFlush.set(0);
		missesSinceFlush.set(0);
		flushTime.set(System.currentTimeMillis());
	}
}
