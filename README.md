sohu-TV 开源cachecloud 之后，对于规模化管理REDIS提供了便利手段，解放了运维/DBA繁杂的工作，提升了排查问题便利性，在此致敬。 为了对cachecloud整体性能，高可用切换，对业务影响等方面做一个比较全面评估，特开发相应压测脚本配合各种需求性能测试。由于本人非专业开发，欢迎对代码指正修改，不喜勿喷


测试工具使用方法：
本文提供Redis Sentinel和Redis Cluster两种环境性能测试脚本：
调用命令如下：
RedisSenitel：
java -classpath /opt/cluster/canal_data_client.jar com.stone.redis.RedisSenitel "appid" "线程数"

RedisCluster：
java -classpath /opt/cluster/canal_data_client.jar com.stone.redis.RedisCluster： "appid" "线程数"

