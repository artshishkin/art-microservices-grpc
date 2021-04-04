package net.shyshkin.study.grpc.grpcintro.server;

import io.grpc.stub.StreamObserver;
import net.shyshkin.study.grpc.grpcintro.models.Balance;
import net.shyshkin.study.grpc.grpcintro.models.DepositRequest;

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
        System.out.println("New Deposit: " + value);
        accountId = value.getAccountNumber();
        int amount = value.getAmount();
        accountDatabase.addBalance(accountId, amount);
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Exception occurred: " + t.getMessage());
    }

    @Override
    public void onCompleted() {
        System.out.println("Completed");
        Balance balance = Balance.newBuilder()
                .setAmount(accountDatabase.getBalance(accountId))
                .build();
        responseObserver.onNext(balance);
        responseObserver.onCompleted();
    }
}
