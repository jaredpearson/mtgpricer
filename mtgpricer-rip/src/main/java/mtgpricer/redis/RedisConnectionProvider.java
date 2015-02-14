package mtgpricer.redis;

import redis.clients.jedis.Jedis;

/**
 * Connection provider for Redis connections
 * @author jared.pearson
 */
public interface RedisConnectionProvider {
	/**
	 * Gets a connection from the connection pool.
	 */
	public Jedis getConnection();
	
	/**
	 * Returns the connection back to the connection pool.
	 */
	public void returnConnection(Jedis connection);
}