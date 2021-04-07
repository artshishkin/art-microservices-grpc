package net.shyshkin.study.grpc.grpcintro.server.metadata;

import io.grpc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class AuthInterceptorDB implements ServerInterceptor {

    private final AccountDatabase accountDatabase;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        String clientToken = headers.get(ServerConstants.USER_TOKEN_KEY);
        log.debug("Client token is `{}`", clientToken);

        if (validate(clientToken)) {
            UserRole userRole = extractUserRole(clientToken);
            Context context = Context.current().withValue(
                    ServerConstants.CTX_USER_ROLE,
                    userRole
            );
            return Contexts.interceptCall(context, call, headers, next);
//            return next.startCall(call, headers);
        }
        Status status = Status.UNAUTHENTICATED.withDescription("invalid/expired token");
        call.close(status, headers);
        return new ServerCall.Listener<ReqT>() {
        };
    }

    private boolean validate(String clientToken) {

        if (Objects.isNull(clientToken)) return false;

        try {

            String[] tokenParts = clientToken.split(":");

            if (tokenParts.length != 2) return false;

            String role = tokenParts[1];
            UserRole userRole = UserRole.valueOf(role.toUpperCase());

            String usernameToken = tokenParts[0];

            if (!usernameToken.startsWith("user-token-")) return false;

            String accountIdString = usernameToken.replace("user-token-", "");

            int accountId = Integer.parseInt(accountIdString);

            return accountDatabase.accountPresent(accountId);
        } catch (IllegalArgumentException e) {
            log.error("Wrong Token {}", clientToken, e);
        }
        return false;
    }

    private UserRole extractUserRole(String clientToken) {

        String[] tokenParts = clientToken.split(":");
        String role = tokenParts[1];
        return UserRole.valueOf(role.toUpperCase());
    }
}
