//package com.stone.redis;
//import com.sohu.tv.builder.ClientBuilder;
//import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
//import org.apache.log4j.Logger;
//import redis.clients.jedis.*;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * Created by Stone on 2017/12/26.
// */
//public class RedisTest1 {
//
//    private static Logger logger = Logger.getLogger(RedisTest1.class);
//
//    private static String KEY_PREFIX = "key:";
//
//   // private static JedisCluster jedisCluster;
//   private static Jedis jedis ;
//    private static int WRITETIME = 200, READTIME = 60;
//
//    private static byte[] value = new byte[4096];
//
//    //args[0] redis集群的配置
//    public static void main(String[] args) throws Exception {
//        long startTime = System.currentTimeMillis();
//        initRedis();
//        start(args);
//        System.out.println("共用时：" + (System.currentTimeMillis() - startTime) / 1000 + "s");
//        System.exit(0);
//    }
//
//
//    public static void test() throws Exception{
//        //deleteKeys();
//        //singleWrite();
//        //singleRead();
//        //testTimer();
//        //----------------------------------------------------------
//        /*int[] multiStarts = {2000000, 3000000, 4000000};
//        multiWriteTest(multiStarts);
//        multiReadTest(getAllKeys(KEY_PREFIX + "2*"), getAllKeys(KEY_PREFIX + "3*"),
//                getAllKeys(KEY_PREFIX + "4*"));*/
//    }
//
//    public static void start(String[] args) throws Exception {
//
////        jedisCluster.set("key1", "value1");
////        System.out.println(jedisCluster.get("key1"));
//        if (args.length==0) {
//            System.out.println("please input params.");
//        }else if("write".equals(args[0])){
//            if(args.length>1&&Integer.parseInt(args[1])>1){//多线程写
//                int n = Integer.parseInt(args[1]);
//                int[] multiStarts = new int[n];
//                for (int i=0; i<n; i++)
//                    multiStarts[i] = (i + 2)*1000000;
//                multiWriteTest(multiStarts);
//            }else{//单线程写
//                singleWrite();
//            }
//        }else if("read".equals(args[0])){
//            if(args.length>1&&Integer.parseInt(args[1])>1){//多线程读
//                int n = Integer.parseInt(args[1]);
//                Set<byte[]> params = new HashSet<>();
//                for (int i=0; i<n; i++)
//                   // params.addAll(getAllKeys(KEY_PREFIX + (i+2) +"*"));
//                multiReadTest(params);
//            }else{//单线程读
//                singleRead();
//            }
//        }else if ("delete".equals(args[0])) {//删除所有测试数据
//            deleteKeys();
//        } else if ("count".equals(args[0])) {//计算测试数据的量
//            logger.info("redis集群中key..的个数：" + getKeysCount());
//        }
//
//    }
//
//
//    public static void singleWrite() throws InterruptedException {
//        logger.info("test single-Thread to write ...");
//        singleWriteTest(1000000);
//        //logger.info("single-Thread write rate is " + getKeysCount(KEY_PREFIX + "1*") + "/min");
//    }
//
//    public static void singleRead() throws InterruptedException {
//        logger.info("test single-Thread to read ...");
//       // logger.info("single-Thread read rate is " + singleReadTest(getAllKeys(KEY_PREFIX + "1*")) + "/min");
//    }
//
//    public static void initRedis() throws Exception {
////        long appId = 10003;
/////**
//// * 使用自定义配置：
//// * 1. setTimeout：redis操作的超时设置；
//// * 2. setMaxRedirections：节点定位重试的次数；
//// */
////        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
////        jedisCluster = ClientBuilder.redisCluster(appId)
////                .setJedisPoolConfig(poolConfig)
////                .setConnectionTimeout(5000)
////                .setSoTimeout(5000)
////                .setMaxRedirections(5)
////                .build();
//
//
//        long appId = 10000;
///**
// * 自定义配置
// */
//        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
//        JedisSentinelPool sentinelPool = ClientBuilder.redisSentinel(appId)
//                .setConnectionTimeout(1000)
//                .setSoTimeout(1000)
//                .setPoolConfig(poolConfig)
//                .build();
//            jedis = sentinelPool.getResource();
//        }
//
//    /**
//     * 单线程写入测试
//     * @param start 开始的底数
//     * @throws InterruptedException
//     */
//    public static void singleWriteTest(final int start) throws InterruptedException {
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            int n = start;
//            @Override
//            public void run() {
//                while (n < Integer.MAX_VALUE) {
//                    try {
//                        jedis.set((KEY_PREFIX + (n++)).getBytes(), value);
//                    } catch (Exception e) {
//                        break;
//                    }
//                }
//            }
//        },0);
//        TimeUnit.SECONDS.sleep(WRITETIME);
//        timer.cancel();
//    }
//
//
//    public static int singleReadTest(Set<byte[]> keys) throws InterruptedException {
//        Timer timer = new Timer();
//        AtomicInteger r = new AtomicInteger(0);
//        ReadTask readTask = new RedisTest1().new ReadTask(r, keys);
//        timer.schedule(readTask, 0);
//        TimeUnit.SECONDS.sleep(READTIME);
//        timer.cancel();
//        return r.intValue();
//    }
//
//    class ReadTask extends TimerTask {
//        private AtomicInteger single_inital;
//        private Set<byte[]> keys;
//
//        public ReadTask(AtomicInteger single_inital, Set<byte[]> keys) {
//            this.single_inital = single_inital;
//            this.keys = keys;
//        }
//
//        public void run() {
//            for (byte[] key : keys) {
//                try {
//                    jedis.get(key);
//                    single_inital.incrementAndGet();
//                } catch (Exception e) {
//                    break;
//                }
//            }
//        }
//    }
//
//    /**
//     * 多个写测试
//     *
//     * @param starts 写的键值起始数组
//     * @return 写入的总数
//     */
//    public static void multiWriteTest(int[] starts) {
//
//        final CountDownLatch countDownLatch = new CountDownLatch(starts.length);
//
//        int result = 0;
//        ExecutorService threadPool = Executors.newFixedThreadPool(starts.length);
//        logger.info("test multi-Thread to write ...");
//        for (final int start:starts) {
//            threadPool.execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        System.out.println(Thread.currentThread().getName()+" start...");
//                        singleWriteTest(start);
//                        countDownLatch.countDown();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
//        try {
//            countDownLatch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
////        logger.info("multi-Thread write rate is ：" + (getKeysCount(KEY_PREFIX + "2*") +
////                getKeysCount(KEY_PREFIX + "3*") + getKeysCount(KEY_PREFIX + "4*"))
////                +"/min");
//    }
//
//    /**
//     * 多个读测试
//     *
//     * @param keysSet 读的键值起始数组
//     * @return 读入的总数
//     */
//    public static void multiReadTest(Set<byte[]>... keysSet) {
//        logger.info("test multi-Thread to read ...");
//        int result = 0;
//        ExecutorService threadPool = Executors.newFixedThreadPool(keysSet.length);
//        Set<MultiRead> multiReads = new HashSet<>();
//        for (Set<byte[]> keys : keysSet) {
//            multiReads.add(new RedisTest1().new MultiRead(keys));
//        }
//        try {
//            List<Future<Integer>> futureList = threadPool.invokeAll(multiReads);
//            if (futureList != null) {
//                for (Future<Integer> future : futureList) {
//                    result += future.get();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        logger.info("multi-Thread read rate is " + result + "/min");
//    }
//
//    class MultiRead implements Callable<Integer> {
//
//        private Set<byte[]> keys;
//
//        public MultiRead(Set<byte[]> keys) {
//            this.keys = keys;
//        }
//
//        @Override
//        public Integer call() throws Exception {
//            return singleReadTest(keys);
//        }
//    }
//
//    /**
//     * 测试redis集群创建
//     *
//     * @return
//     */
//    public static Set<HostAndPort> createTestRedis() {
//        Set<HostAndPort> nodes = new HashSet<>();
//        String ip = "192.168.217.5";
//        nodes.add(new HostAndPort(ip, 7000));
//        nodes.add(new HostAndPort(ip, 7001));
//        nodes.add(new HostAndPort(ip, 7002));
//        nodes.add(new HostAndPort(ip, 7003));
//        nodes.add(new HostAndPort(ip, 7004));
//        nodes.add(new HostAndPort(ip, 7005));
//        return nodes;
//    }
//
//    /**
//     * 删除redis中测试数据
//     *
//     * @return 删除的键值对个数
//     */
//    public static int deleteKeys() {
//        Set<byte[]> allkeys = getAllKeys();
//        for (byte[] k : allkeys)
//            jedis.del(k);
//        return allkeys.size();
//    }
//
//    /**
//     * 获取redis数据库所有的key
//     *
//     * @return
//     */
//    public static Set<byte[]> getAllKeys() {
//        return getAllKeys("key:*");
//    }
//
//    /**
//     * 获取redis数据库所有的key
//     *
//     * @return
//     */
////    public static Set<byte[]> getAllKeys(String pattern) {
////        Set<byte[]> allkeys = new HashSet<>();
////        Map<String, JedisPool> nodes = jedis.getClusterNodes();
////        for (Map.Entry<String, JedisPool> node : nodes.entrySet()) {
////            Jedis jedis = node.getValue().getResource();
////            Set<byte[]> keys = jedis.keys(pattern.getBytes());
////            allkeys.addAll(keys);
////            jedis.close();
////        }
////        return allkeys;
////    }
//
//    /**
//     * 获取redis库中键值对个数
//     *
//     * @return
//     */
//    public static int getKeysCount() {
//        return getAllKeys().size();
//    }
//
//    /**
//     * 获取redis库中键值对个数
//     *@param pattern
//     * @return
//     */
////    public static int getKeysCount(String pattern) {
////        return getAllKeys(pattern).size();
////    }
//}
