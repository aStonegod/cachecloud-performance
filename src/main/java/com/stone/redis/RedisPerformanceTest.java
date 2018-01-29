package com.stone.redis;

        import com.sohu.tv.builder.ClientBuilder;
        import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
        import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
        import org.springframework.data.redis.core.RedisTemplate;
        import org.springframework.data.redis.serializer.StringRedisSerializer;
        import redis.clients.jedis.JedisCluster;
        import redis.clients.jedis.JedisPoolConfig;

        import java.util.concurrent.ExecutorService;
        import java.util.concurrent.Executors;
        import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by min.zhou on 2017/12/21.
 */
public class RedisPerformanceTest {
    public static void main(String[] args) throws InterruptedException {
        new RedisPerformanceTest().test();
    }

    private static JedisCluster jedisCluster;

    private RedisTemplate redisTemplate;

    private void test() throws InterruptedException {

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        long appId = 10003;
        jedisCluster = ClientBuilder.redisCluster(appId)
                .setJedisPoolConfig(poolConfig)
                .setConnectionTimeout(5000)
                .setSoTimeout(5000)
                .setMaxRedirections(5)
                .build();
//        JedisPoolConfig poolConfig = new JedisPoolConfig();
//        poolConfig.setMaxTotal(1024);
//        poolConfig.setMinIdle(8);
//        JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
//        connectionFactory.setPoolConfig(poolConfig);
//        connectionFactory.setHostName("10.2.1.123");
//        connectionFactory.setPort(6382);
//        connectionFactory.setPassword("");
//        System.out.println("######### default timeout" + connectionFactory.getTimeout());
//        connectionFactory.setTimeout(10000);
//        System.out.println("######### new timeout" + connectionFactory.getTimeout());
//        connectionFactory.afterPropertiesSet();


//
        redisTemplate = new RedisTemplate();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
     //   redisTemplate.setConnectionFactory(jedisCluster);
        redisTemplate.afterPropertiesSet();
        AtomicInteger threadIndex = new AtomicInteger();
        int threadSize = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
        AtomicInteger totalCount = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        for (int idx = 0; idx < threadSize; idx++) {
            final int threadNo = threadIndex.incrementAndGet();
            executorService.submit(() -> {
                int loopNo = 0;
                for (; loopNo < Integer.MAX_VALUE; loopNo++) {
                    try {
                        doRandomCommand(loopNo);
                        if (totalCount.incrementAndGet() % 10000 == 0) {
                            System.out.println(String.format("totalCount=%d,totalSeconds=%d,threadNo=%d,threadLoopNo=%d",
                                    totalCount.get(), (System.currentTimeMillis() - start) / 1000, threadNo, loopNo));
                        }
                    } catch (Exception e) {
                        System.out.println(String.format("loopNo=%d,e=%s", loopNo, e.getMessage()));
                    }
                }
            });
            System.out.println(String.format("threadNo=%d,start!", threadNo));
        }
        Thread.sleep(Integer.MAX_VALUE);
    }

    private void doRandomCommand(int loopNo) {
        Command command = Command.values()[loopNo % Command.values().length];
        command.exec(loopNo, redisTemplate);
    }

    enum Command {
        GET, SET, HGET, HSET, SADD, SISMEMBER;

        public void exec(int loopNo, RedisTemplate redisTemplate) {
            switch (this) {
                case GET:
                    redisTemplate.opsForValue().get("value:" + loopNo / 2);
                    break;
                case SET:
                    redisTemplate.opsForValue().set("value:" + loopNo, loopNo + "");
                    break;
                case HSET:
                    redisTemplate.opsForHash().put("map:" + loopNo / 1000, "" + loopNo % 1000, loopNo + "");
                    break;
                case HGET:
                    redisTemplate.opsForHash().get("map:" + loopNo / 1000, "" + loopNo % 1000);
                    break;
                case SADD:
                    redisTemplate.opsForSet().add("set:" + loopNo / 1000, loopNo + "");
                    break;
                case SISMEMBER:
                    redisTemplate.opsForSet().isMember("set:" + loopNo / 1000, loopNo + "");
                    break;
            }
        }
    }
}
