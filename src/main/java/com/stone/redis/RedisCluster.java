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
public class RedisCluster {

    private static JedisCluster redisCluster;

    public static void main(String args[]) throws InterruptedException {
        long appId = Long.parseLong(args[0]);
/**
 * 自定义配置
 */
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        redisCluster = ClientBuilder.redisCluster(appId)
                .setJedisPoolConfig(poolConfig)
                .setConnectionTimeout(1000)
                .setSoTimeout(1000)
                .setMaxRedirections(5)
                .build();
        AtomicInteger threadIndex = new AtomicInteger();
        int threadSize = Integer.parseInt(args[1]);
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
                            System.out.println(String.format("totalCount=%dw,totalSeconds=%ds,threadNo=%d,threadLoopNo=%d",
                                    (totalCount.get()/10000), (System.currentTimeMillis() - start) / 1000, threadNo, loopNo));
                        }
                    } catch (Exception e) {
                        System.out.println(String.format("loopNo=%d,e=%s", loopNo, e.getMessage()));
                        e.printStackTrace();
                    }
                }
            });
            System.out.println(String.format("threadNo=%d,start!", threadNo));
        }
        Thread.sleep(Integer.MAX_VALUE);

    }


    private static void doRandomCommand(int loopNo) {
        Command command = Command.values()[loopNo % Command.values().length];
        command.exec(loopNo,redisCluster );
    }

    enum Command {
        GET, SET,HGET, HSET;//, SADD, SISMEMBER;

        public void exec(int loopNo, JedisCluster redisCluster) {
            switch (this) {
                case GET:
                    redisCluster.get("value:" + loopNo);
                    break;
                case SET:
                    redisCluster.set("value:" + loopNo, loopNo + "");
                    break;
                case HSET:
                    redisCluster.hset("uid"+ loopNo/1000 , "" + loopNo % 1000, loopNo + "");
                    break;
                case HGET:
                    redisCluster.hget("uid:" + loopNo / 1000, "" + loopNo % 1000);
                    break;
//                case SADD:
//                    jedis.append("uidset:" + loopNo / 1000, loopNo + "");
//                    break;
//                case SISMEMBER:
//                    jedis.sismember("uidset:" + loopNo / 1000, loopNo + "");
//                    break;
            }
           // redisCluster.close();
        }
    }
}
