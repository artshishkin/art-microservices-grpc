package net.shyshkin.study.grpc.grpcintro.server;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.TransferResponse;

import java.util.concurrent.CountDownLatch;

@Slf4j
@RequiredArgsConstructor
class TransferResponseStreamObserver implements StreamObserver<TransferResponse> {

    private final CountDownLatch latch;

    @Override
    public void onNext(TransferResponse transferResponse) {
        StringBuilder builder = new StringBuilder();
        builder.append(" [").append(transferResponse.getStatus()).append("] ");
        transferResponse.getAccountBalancesList()
                .forEach(accountBalance -> builder
                        .append("{")
                        .append(accountBalance.getAccountNumber())
                        .append(" : ")
                        .append(accountBalance.getAmount())
                        .append("}")
                );
        log.debug("Received {}", builder);
    }

    @Override
    public void onError(Throwable t) {
        log.error("Exception occurred", t);
        latch.countDown();
    }

    @Override
    public void onCompleted() {
        log.debug("onCompleted");
        latch.countDown();
    }
}
