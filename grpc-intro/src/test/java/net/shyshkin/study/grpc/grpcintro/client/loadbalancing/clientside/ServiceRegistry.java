package net.shyshkin.study.grpc.grpcintro.client.loadbalancing.clientside;

import io.grpc.EquivalentAddressGroup;

import java.util.List;

public interface ServiceRegistry {

    void registerInstance(String serviceName, String instanceIP);

    List<EquivalentAddressGroup> getInstances(String serviceName);

}
