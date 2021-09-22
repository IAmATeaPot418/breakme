# bRPC

一个基于netty的RPC框架

1. 基于netty NIO、IO多路复用。
2. client与server端建立心跳包保活机制。此外client未知断连时，server端主动关闭，触发client channel Inactive事件并进行重连保证长连接。
3. 自定义传输包，避免TCP沾包问题。
4. 利用zookeeper做服务注册中心。
5. 整合spring注解，可通过注解便捷使用，此外可在注解中配置server端业务线程池核心线程数及最大线程数。

## Getting started

- 部署zookeeper推荐使用docker

1. 拉取zk镜像&emsp;&emsp;&emsp;指令：docker pull zookeeper:3.4.14
2. 查看镜像id&emsp;&emsp;&emsp;指令：docker images
3. 拉起容器&emsp;&emsp;&emsp;&emsp;指令：docker run -d -p 2181:2181 --name b-zookeeper --restart always {imageId}
4. 查看容器id&emsp;&emsp;&emsp;指令：docker ps -a
5. 进入容器&emsp;&emsp;&emsp;&emsp;指令：docker exec -it {containerId} /bin/bash
6. 起注册中心&emsp;&emsp;&emsp;指令：./bin/zkCli.sh

&emsp;&emsp;推荐一个zk可视化工具：https://zhuanlan.zhihu.com/p/148534430

- rpc测试 

1. com.application.test.spring.server.ServerBootstrap.java  服务端启动
2. com.application.test.spring.client.ClientTest.java       客户端启动 并rpc调用HelloService.java





