package net.shyshkin.study.grpc.grpcintro.client.loadbalancing.clientside;

import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
@RequiredArgsConstructor
public class AppNameResolverProvider extends NameResolverProvider {

    private final ServiceRegistry serviceRegistry;

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }

    @Override
    public String getDefaultScheme() {
        return "dns";
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        log.debug("Looking for service {}", targetUri);
        return new AppNameResolver(targetUri.toString(), serviceRegistry);
    }
}
