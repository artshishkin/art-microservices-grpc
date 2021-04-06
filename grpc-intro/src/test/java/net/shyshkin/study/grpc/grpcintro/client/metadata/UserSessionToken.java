package net.shyshkin.study.grpc.grpcintro.client.metadata;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class UserSessionToken extends CallCredentials {

    private final String jwt;

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
        appExecutor.execute(() -> {
            Metadata metadata = new Metadata();
            metadata.put(ClientConstants.USER_TOKEN, jwt);
            applier.apply(metadata);
//            applier.fail(...); //if we want to fail execution on client side
        });
    }

    @Override
    public void thisUsesUnstableApi() {

    }
}
