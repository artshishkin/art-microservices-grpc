package net.shyshkin.study.grpc.grpcintro.server.metadata;

import io.grpc.Metadata;

public class ServerConstants {

    public static final Metadata.Key<String> CLIENT_TOKEN_KEY = Metadata.Key.of("client-token", Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> USER_TOKEN_KEY = Metadata.Key.of("user-token", Metadata.ASCII_STRING_MARSHALLER);

}
