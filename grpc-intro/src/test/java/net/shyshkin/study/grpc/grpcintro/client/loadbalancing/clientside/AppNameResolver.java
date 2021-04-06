package net.shyshkin.study.grpc.grpcintro.client.loadbalancing.clientside;

import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class AppNameResolver extends NameResolver {

    private final String service;
    private final ServiceRegistry serviceRegistry;

    @Override
    public String getServiceAuthority() {
        return "no_matter";
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void refresh() {
        super.refresh();
    }

    @Override
    public void start(Listener2 listener) {

        List<EquivalentAddressGroup> addressGroups = serviceRegistry.getInstances(service);
        ResolutionResult resolutionResult = ResolutionResult.newBuilder()
                .setAddresses(addressGroups)
                .build();
        listener.onResult(resolutionResult);
    }
}
