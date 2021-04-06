package net.shyshkin.study.grpc.grpcintro.client.loadbalancing.clientside;

import io.grpc.EquivalentAddressGroup;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleMapServiceRegistry implements ServiceRegistry {

    private final Map<String, List<EquivalentAddressGroup>> instances = new HashMap<>();

    @Override
    public void registerInstance(String serviceName, String instanceIP) {

        String[] ipParts = instanceIP.split(":");
        String host = ipParts[0];
        int port = Integer.parseInt(ipParts[1]);

        SocketAddress socketAddress = new InetSocketAddress(host, port);
        EquivalentAddressGroup addressGroup = new EquivalentAddressGroup(socketAddress);

        instances.putIfAbsent(serviceName,new ArrayList<>());
        instances.get(serviceName).add(addressGroup);
    }

    @Override
    public List<EquivalentAddressGroup> getInstances(String serviceName) {
        return instances.get(serviceName);
    }
}
