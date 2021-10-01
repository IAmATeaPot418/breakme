package com.polyu.rpc.client;

import com.polyu.rpc.annotation.BRpcConsumer;
import com.polyu.rpc.client.connection.ConnectionManager;
import com.polyu.rpc.client.proxy.ObjectProxy;
import com.polyu.rpc.client.proxy.RpcService;
import com.polyu.rpc.registry.ServiceDiscovery;
import com.polyu.rpc.registry.zookeeper.ZKDiscovery;
import com.polyu.rpc.route.RpcLoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcClient implements ApplicationContextAware, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private ServiceDiscovery serviceDiscovery;
    /**
     * 异步请求 线程池
     */
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

    /**
     * 注册中心地址 & 注册中心选型
     * @param serviceDiscovery 注册中心选型 nacos / zk
     */
    public RpcClient(ServiceDiscovery serviceDiscovery) {
        ConnectionManager connectionManager = ConnectionManager.getAndInitInstance(serviceDiscovery);
        this.serviceDiscovery = connectionManager.getServiceDiscovery();
        this.serviceDiscovery.discoveryService();
    }

    /**
     * todo 待删
     * just for test
     */
    public RpcClient(String discoveryAddress) {
        ConnectionManager connectionManager = ConnectionManager.getAndInitInstance(new ZKDiscovery(discoveryAddress));
        this.serviceDiscovery = connectionManager.getServiceDiscovery();
        this.serviceDiscovery.discoveryService();
    }

    @SuppressWarnings("unchecked")
    public static <T, P> T createService(Class<T> interfaceClass, String version, RpcLoadBalance loadBalance) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ObjectProxy<T, P>(interfaceClass, version, loadBalance)
        );
    }

    @SuppressWarnings("unchecked")
    public static <T, P> T createService(Class<T> interfaceClass, String version) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ObjectProxy<T, P>(interfaceClass, version)
        );
    }


    public static <T, P> RpcService createAsyncService(Class<T> interfaceClass, String version) {
        return new ObjectProxy<T, P>(interfaceClass, version);
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    private void stop() {
        threadPoolExecutor.shutdown();
        serviceDiscovery.stop();
        ConnectionManager.getInstance().stop();
    }

    @Override
    public void destroy() {
        this.stop();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Field[] fields = bean.getClass().getDeclaredFields();
            try {
                for (Field field : fields) {
                    BRpcConsumer rpcAutowired = field.getAnnotation(BRpcConsumer.class);
                    if (rpcAutowired != null) {
                        String version = rpcAutowired.version();
                        RpcLoadBalance loadBalance = (RpcLoadBalance) rpcAutowired.loadBalanceStrategy().newInstance();
                        field.setAccessible(true);
                        field.set(bean, createService(field.getType(), version, loadBalance));
                    }
                }
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
    }
}
