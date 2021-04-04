package net.shyshkin.study.grpc.grpcintro.server;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.Balance;
import net.shyshkin.study.grpc.grpcintro.models.DepositRequest;

@Slf4j
class DepositStreamObserver implements StreamObserver<DepositRequest> {

    private final StreamObserver<Balance> responseObserver;
    private final AccountDatabase accountDatabase;
    private int accountId;

    public DepositStreamObserver(StreamObserver<Balance> responseObserver, AccountDatabase accountDatabase) {
        this.responseObserver = responseObserver;
        this.accountDatabase = accountDatabase;
    }

    @Override
    public void onNext(DepositRequest value) {
        log.debug("New Deposit:\n{}", value);
        accountId = value.getAccountNumber();
        int amount = value.getAmount();
        accountDatabase.addBalance(accountId, amount);
    }

    @Override
    public void onError(Throwable t) {
        log.error("Exception occurred: {}", t.getLocalizedMessage());
    }

    @Override
    public void onCompleted() {
        log.debug("Completed");
        Balance balance = Balance.newBuilder()
                .setAmount(accountDatabase.getBalance(accountId))
                .build();
        responseObserver.onNext(balance);
        responseObserver.onCompleted();
    }
}
