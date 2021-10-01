package com.polyu.rpc.client.spring;

import com.polyu.rpc.client.RpcClient;
import com.polyu.rpc.registry.ServiceDiscovery;
import com.polyu.rpc.registry.nacos.NacosDiscovery;
import com.polyu.rpc.registry.zookeeper.ZKDiscovery;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

public class ClientAutoConfig {

    private static final String NACOS_CONFIG_TYPE = "nacos";
    private static final String ZK_CONFIG_TYPE = "zookeeper";

    @Value("${bRPC.client.registry.type}")
    private String registryCenter;

    @Value("${bRPC.client.registry.address}")
    private String registryAddress;

    @Bean
    public RpcClient createRpcClientBean() throws Exception {
        ServiceDiscovery serviceDiscovery = null;
        if (registryCenter != null && !"".equals(registryAddress)) {
            switch (registryCenter) {
                case NACOS_CONFIG_TYPE:
                    serviceDiscovery = new NacosDiscovery(registryAddress);
                    break;
                case ZK_CONFIG_TYPE:
                    serviceDiscovery = new ZKDiscovery(registryAddress);
                    break;
                default:
                    throw new Exception("Wrong type of registry type for " + registryCenter);
            }
        }
        return new RpcClient(serviceDiscovery);
    }

}