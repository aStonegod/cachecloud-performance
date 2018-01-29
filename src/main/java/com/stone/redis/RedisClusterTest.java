package com.stone.redis;

import com.sohu.tv.builder.ClientBuilder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisCluster;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Stone on 2017/12/26.
 */
public class RedisClusterTest {



    public static void main(String args[]) throws InterruptedException {
        long appId = 10007;
/**
 * 使用自定义配置：
 * 1. setTimeout：redis操作的超时设置；
 * 2. setMaxRedirections：节点定位重试的次数；
 */
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        JedisCluster redisCluster = ClientBuilder.redisCluster(appId)
                .setJedisPoolConfig(poolConfig)
                .setConnectionTimeout(1000)
                .setSoTimeout(1000)
                .setMaxRedirections(5)
                .build();
//1.字符串value
        redisCluster.set("key1", "value1");
        System.out.println(redisCluster.get("key1"));
    }
}
