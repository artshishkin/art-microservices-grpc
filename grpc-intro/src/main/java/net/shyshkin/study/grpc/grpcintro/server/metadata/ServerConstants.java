package net.shyshkin.study.grpc.grpcintro.server.metadata;

import io.grpc.Context;
import io.grpc.Metadata;

public class ServerConstants {

    public static final Metadata.Key<String> CLIENT_TOKEN_KEY = Metadata.Key.of("client-token", Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> USER_TOKEN_KEY = Metadata.Key.of("user-token", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<UserRole> CTX_USER_ROLE = Context.key("user-role");

    public static final Context.Key<Integer> CTX_USER_ID = Context.key("user-id");
}
