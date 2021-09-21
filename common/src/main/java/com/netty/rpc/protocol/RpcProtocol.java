package com.netty.rpc.protocol;

import com.netty.rpc.util.JsonUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Data
public class RpcProtocol implements Serializable {
    private static final long serialVersionUID = -1102180003395190700L;

    private String host;
    private int port;
    private List<RpcServiceInfo> serviceInfoList;

    public String toJson() {
        return JsonUtil.objectToJson(this);
    }

    public static RpcProtocol fromJson(String json) {
        return JsonUtil.jsonToObject(json, RpcProtocol.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcProtocol that = (RpcProtocol) o;
        return port == that.port &&
                Objects.equals(host, that.host) &&
                isListEquals(serviceInfoList, that.getServiceInfoList());
    }

    private boolean isListEquals(List<RpcServiceInfo> thisList, List<RpcServiceInfo> thatList) {
        if (thisList == null && thatList == null) {
            return true;
        }
        if (thisList == null || thatList == null || thisList.size() != thatList.size()) {
            return false;
        }
        return thisList.containsAll(thatList) && thatList.containsAll(thisList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, serviceInfoList.hashCode());
    }

    @Override
    public String toString() {
        return toJson();
    }

    public List<RpcServiceInfo> getServiceInfoList() {
        return serviceInfoList;
    }

    public void setServiceInfoList(List<RpcServiceInfo> serviceInfoList) {
        this.serviceInfoList = serviceInfoList;
    }
}