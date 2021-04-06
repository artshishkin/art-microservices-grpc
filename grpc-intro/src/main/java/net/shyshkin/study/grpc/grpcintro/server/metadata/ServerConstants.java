package net.shyshkin.study.grpc.grpcintro.server.metadata;

import io.grpc.Metadata;

public class ServerConstants {

    public static final Metadata.Key<String> TOKEN_KEY = Metadata.Key.of("client-token", Metadata.ASCII_STRING_MARSHALLER);

}
