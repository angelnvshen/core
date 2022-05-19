#### redis 分布式锁
#### redis pipeline

pipeline顾名思义就是流水线操作，像http 1.1也开始支持pipeline来把多个HTTP请求放到一个TCP连接中一 一发送，而在发送过程中不需要等待服务器对前一个请求的响应，在redis client中使用pipeline的主要目的也与此相同，打包请求的同时减少了很多网络IO。在lettuce中的底层实现是将请求中的多个command先放到socket buffer中，然后统一flush出去。

##### **1 lettuce原生的pipeline**

关闭了auto flush选项，它会调用 writeToBuffer(command)方法，将command先flush到socket buffer中，在后面调用 commands.flushCommands()方法时才真正地执行flush操作。

##### **2 spring-data-redis包装后的lettuce的pipeline**

在使用redisTemplate的时候，从来都没有设置过autoFlushCommands，没错，这里它为true，也就是说每来set一次就会flush一次，这也就解释了为什么在第一个set操作之后断点在redis中可以查到第一条已经set成功的原因。

**也就是说，直接使用redisTemplate来操作pipeline时它还是一条条地去操作的，是一个伪批操作。** 

#### todo redis 配置pool, 单机，sentinel，集群，docker, docker file
