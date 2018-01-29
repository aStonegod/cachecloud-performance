package com.stone.redis;

import com.sohu.tv.builder.ClientBuilder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Stone on 2017/12/26.
 */
public class RedisSentinel {

    private static JedisSentinelPool sentinelPool;

    public static void main(String args[]) throws InterruptedException {
        long appId = Long.parseLong(args[0]);
/**
 * 自定义配置
 */
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(550);
        poolConfig.setMinIdle(50);
        poolConfig.setMaxIdle(1024);
        sentinelPool = ClientBuilder.redisSentinel(appId)
                .setConnectionTimeout(5000)
                .setSoTimeout(5000)
                .setPoolConfig(poolConfig)
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
        command.exec(loopNo, sentinelPool.getResource());
    }

    enum Command {
        GET, SET,HGET, HSET;//, SADD, SISMEMBER;

        public void exec(int loopNo, Jedis jedis) {
            switch (this) {
                case GET:
                    jedis.get("value:" + loopNo);
                    break;
                case SET:
                    jedis.set("value:" + loopNo, loopNo + "");
                    break;
                case HSET:
                    jedis.hset("uid"+ loopNo/1000 , "" + loopNo % 1000, loopNo + "");
                    break;
                case HGET:
                    jedis.hget("uid:" + loopNo / 1000, "" + loopNo % 1000);
                    break;
//                case SADD:
//                    jedis.append("uidset:" + loopNo / 1000, loopNo + "");
//                    break;
//                case SISMEMBER:
//                    jedis.sismember("uidset:" + loopNo / 1000, loopNo + "");
//                    break;
            }
            jedis.close();
        }
    }
}
