package net.shyshkin.study.grpc.grpcintro.server.metadata;

import io.grpc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor implements ServerInterceptor {

    private final String token;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        String clientToken = headers.get(ServerConstants.USER_TOKEN_KEY);
        log.debug("Client token is `{}`", clientToken);

        if (validate(clientToken))
            return next.startCall(call, headers);
        Status status = Status.UNAUTHENTICATED.withDescription("invalid/expired token");
        call.close(status, headers);
        return new ServerCall.Listener<ReqT>() {
        };
    }

    private boolean validate(String clientToken) {
        return Objects.equals(clientToken, token);
    }
}
