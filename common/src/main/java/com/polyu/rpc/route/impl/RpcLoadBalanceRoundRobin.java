package com.polyu.rpc.route.impl;


import com.polyu.rpc.protocol.RpcProtocol;
import com.polyu.rpc.route.ProtocolsKeeper;
import com.polyu.rpc.route.RpcLoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询
 */
public class RpcLoadBalanceRoundRobin extends RpcLoadBalance {
    private AtomicInteger roundRobin;
    private static final Logger logger = LoggerFactory.getLogger(RpcLoadBalanceRoundRobin.class);

    public RpcLoadBalanceRoundRobin() {
        roundRobin = new AtomicInteger(0);
    }

    private RpcProtocol doRoute(List<RpcProtocol> addressList) {
        int size = addressList.size();
        nextNumUpdate();
        int index = (this.roundRobin.get() + size) % size;
        return addressList.get(index);
    }

    /**
     * 防止越界
     */
    private void nextNumUpdate() {
        this.roundRobin.updateAndGet((x) -> {
            if (x >= Integer.MAX_VALUE) {
                return 0;
            }
            return x + 1;
        });
    }

    @Override
    public RpcProtocol route(String serviceKey) throws Exception {
        logger.debug("RpcLoadBalanceRoundRobin is routing for {}.", serviceKey);
        List<RpcProtocol> addressList = ProtocolsKeeper.getProtocolsFromServiceKey(serviceKey);
        if (addressList != null && addressList.size() > 0) {
            return doRoute(addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }
}