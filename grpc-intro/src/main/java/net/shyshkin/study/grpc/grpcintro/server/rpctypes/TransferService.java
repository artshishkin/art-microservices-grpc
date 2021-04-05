package net.shyshkin.study.grpc.grpcintro.server.rpctypes;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.TransferRequest;
import net.shyshkin.study.grpc.grpcintro.models.TransferResponse;
import net.shyshkin.study.grpc.grpcintro.models.TransferServiceGrpc;

@Slf4j
public class TransferService extends TransferServiceGrpc.TransferServiceImplBase {

    private final AccountDatabase accountDatabase;

    public TransferService(AccountDatabase accountDatabase) {
        this.accountDatabase = accountDatabase;
    }

    @Override
    public StreamObserver<TransferRequest> transfer(StreamObserver<TransferResponse> responseObserver) {
        log.debug("transfer method invoked");
        return new TransferRequestStreamObserver(responseObserver, accountDatabase);
    }

}
