package com.polyu.rpc.registry.nacos;

import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.polyu.rpc.protocol.RpcProtocol;
import com.polyu.rpc.protocol.RpcServiceInfo;
import com.polyu.rpc.registry.ServiceRegistry;
import com.polyu.rpc.util.ServiceUtil;
import com.polyu.rpc.registry.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NacosRegistry implements ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(NacosRegistry.class);

    private NamingService namingService;
    private String applicationName;
    private String nacosNameSpace;

    private String ip;
    private int port;

    public NacosRegistry(String registryAddress, String applicationName) {
        try {
            this.namingService = NamingFactory.createNamingService(registryAddress);
        } catch (Exception e) {
            logger.error("Nacos namingService creation failed. exception: {}", e.getMessage());
        }
        this.applicationName = applicationName;
    }

    public NacosRegistry(String registryAddress) {
        try {
            this.namingService = NamingFactory.createNamingService(registryAddress);
        } catch (Exception e) {
            logger.error("Nacos namingService creation failed. exception: {}", e.getMessage());
        }
        this.applicationName = "DefaultApplication";
    }

    /**
     * 注册服务
     * @param host 主机地址
     * @param port 端口地址
     * @param serviceKey2BeanMap serviceKey -> bean
     */
    @Override
    public void registerService(String host, int port, Map<String, Object> serviceKey2BeanMap) {
        List<RpcServiceInfo> serviceInfoList = ServiceUtil.beanMap2RpcServiceInfos(serviceKey2BeanMap);
        this.ip = host;
        this.port = port;
        try {
            RpcProtocol rpcProtocol = new RpcProtocol();
            rpcProtocol.setHost(host);
            rpcProtocol.setPort(port);
            rpcProtocol.setServiceInfoList(serviceInfoList);
            String serviceData = rpcProtocol.toJson();
            this.nacosNameSpace = Constant.NACOS_NAMESPACE_PREFIX.concat(applicationName);
            Instance serviceInstance = new Instance();
            serviceInstance.setIp(host);
            serviceInstance.setPort(port);
            Map<String, String> instanceMeta = new HashMap<>();
            instanceMeta.put("rpcProtocol", serviceData);
            serviceInstance.setMetadata(instanceMeta);
            namingService.registerInstance(nacosNameSpace, serviceInstance);
            logger.info("Register {} new service, host: {}, port: {}.", serviceInfoList.size(), host, port);
        } catch (Exception e) {
            logger.error("Register service fail, exception: {}.", e.getMessage());
        }
    }

    /**
     * 注销服务
     */
    @Override
    public void unregisterService() {
        logger.info("Unregister service.");
        try {
            this.namingService.deregisterInstance(nacosNameSpace, this.ip, this.port);
        } catch (Exception e) {
            logger.error("Delete service path error: {}.", e.getMessage());
        } finally {
            try {
                this.namingService.shutDown();
            } catch (Exception ex) {
                logger.error("NamingService shutDown error: {}.", ex.getMessage());
            }
        }
    }

}