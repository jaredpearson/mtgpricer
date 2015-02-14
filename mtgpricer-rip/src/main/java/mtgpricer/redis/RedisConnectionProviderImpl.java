package mtgpricer.redis;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Implementation of a Redis connection pool
 * @author jared.pearson
 */
class RedisConnectionProviderImpl implements RedisConnectionProvider {
	private final JedisPoolConfig poolConfig = new JedisPoolConfig();
	private final String host;
	private final int port;
	private JedisPool pool;
	
	public RedisConnectionProviderImpl(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public Jedis getConnection() {
		if (pool == null) {
			throw new IllegalStateException("Pool not initialized. Expected init to be called before this method.");
		}
		return pool.getResource();
	}
	
	public void returnConnection(Jedis connection) {
		pool.returnResource(connection);
	}
	
	public void returnBrokenConnection(Jedis connection) {
		pool.returnBrokenResource(connection);
	}
	
	@PostConstruct
	public void init() {
		this.pool = new JedisPool(poolConfig, host, port);
	}
	
	@PreDestroy
	public void destroy() {
		this.pool.close();
	}
}