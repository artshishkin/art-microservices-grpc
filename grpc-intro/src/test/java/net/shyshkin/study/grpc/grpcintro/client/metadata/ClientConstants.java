package net.shyshkin.study.grpc.grpcintro.client.metadata;

import io.grpc.Metadata;
import io.grpc.protobuf.ProtoUtils;
import net.shyshkin.study.grpc.grpcintro.models.WithdrawalError;

public class ClientConstants {

    public static final Metadata.Key<WithdrawalError> WITHDRAWAL_ERROR_KEY = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());
    public static final Metadata.Key<String> USER_TOKEN = Metadata.Key.of("user-token", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata METADATA = new Metadata();

    static {
        Metadata.Key<? super String> key = Metadata.Key.of("client-token", Metadata.ASCII_STRING_MARSHALLER);
        METADATA.put(key, "SuperSecretToken");
    }

    public static Metadata getClientToken() {
        return METADATA;
    }

}
