package com.application.test.common.server;

import com.application.test.service.HelloService;
import com.application.test.service.HelloService2;
import com.application.test.service.HelloServiceImpl;
import com.application.test.service.HelloServiceImpl2;
import com.netty.rpc.server.netty.NettyServer;

public class ServerTest {

    public static void main(String[] args) throws Exception {
//        String serverAddress = "127.0.0.1:18877";
//        String serverAddress = "127.0.0.1:18876";
//        String serverAddress = "127.0.0.1:18875";
        String serverAddress = "127.0.0.1:18874";

        // zk
        String registryAddress = "127.0.0.1:2181";
        // nacos
//        String registryAddress = "127.0.0.1:8848";
        NettyServer rpcServer = new NettyServer(serverAddress, registryAddress);
        HelloService helloService1 = new HelloServiceImpl();
        HelloServiceImpl2  helloService2 = new HelloServiceImpl2();
        rpcServer.addService(HelloService.class.getName(), "1.0", helloService1);
        rpcServer.addService(HelloService2.class.getName(), "1.0", helloService2);
        try {
            rpcServer.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}