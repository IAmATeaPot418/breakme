package com.polyu.rpc.registry.zookeeper;

import com.polyu.rpc.protocol.RpcProtocol;
import com.polyu.rpc.protocol.RpcServiceInfo;
import com.polyu.rpc.registry.ServiceRegistry;
import com.polyu.rpc.util.ServiceUtil;
import com.polyu.rpc.registry.Constant;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ZKRegistry implements ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ZKRegistry.class);
    private static final int TIME_OUT_LENGTH = 5000;

    /**
     * zk客户端
     */
    private CuratorClient zkClient;
    private String zkPath;

    public ZKRegistry(String registryAddress) {
        this.zkClient = new CuratorClient(registryAddress, TIME_OUT_LENGTH);
    }

    /**
     * 服务注册
     * @param host 主机地址
     * @param port 端口
     * @param serviceKey2BeanMap 提供服务信息
     */
    @Override
    public void registerService(String host, int port, Map<String, Object> serviceKey2BeanMap) {
        List<RpcServiceInfo> serviceInfoList = ServiceUtil.beanMap2RpcServiceInfos(serviceKey2BeanMap);
        try {
            RpcProtocol rpcProtocol = new RpcProtocol();
            rpcProtocol.setHost(host);
            rpcProtocol.setPort(port);
            rpcProtocol.setServiceInfoList(serviceInfoList);
            String serviceData = rpcProtocol.toJson();
            byte[] bytes = serviceData.getBytes();
            String path = Constant.ZK_DATA_PATH + "-" + rpcProtocol.hashCode();
            path = this.zkClient.createPathData(path, bytes);
            this.zkPath = path;
            logger.info("Register {} new service, host: {}, port: {}.", serviceInfoList.size(), host, port);
        } catch (Exception e) {
            logger.error("Register service fail, exception: {}.", e.getMessage());
        }

        zkClient.addConnectionStateListener((curatorFramework, connectionState) -> {
            if (connectionState == ConnectionState.RECONNECTED) {
                logger.info("Connection state: {}, register service after reconnected.", connectionState);
                registerService(host, port, serviceKey2BeanMap);
            }
        });
    }

    /**
     * 注销服务
     */
    @Override
    public void unregisterService() {
        logger.info("Unregister service.");
        try {
            this.zkClient.deletePath(zkPath);
        } catch (Exception ex) {
            logger.error("Delete service path error: {}.", ex.getMessage());
        }
        this.zkClient.close();
    }
}