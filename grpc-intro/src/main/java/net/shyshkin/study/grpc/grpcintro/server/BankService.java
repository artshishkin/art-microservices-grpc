package net.shyshkin.study.grpc.grpcintro.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.shyshkin.study.grpc.grpcintro.models.*;

public class BankService extends BankServiceGrpc.BankServiceImplBase {

    private final AccountDatabase accountDatabase;

    public BankService(AccountDatabase accountDatabase) {
        this.accountDatabase = accountDatabase;
    }

    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        int accountNumber = request.getAccountNumber();
        responseObserver.onNext(retrieveBalanceFromDB(accountNumber));
        responseObserver.onCompleted();
    }

    private Balance retrieveBalanceFromDB(int accountNumber) {
        return Balance.newBuilder()
                .setAmount(accountDatabase.getBalance(accountNumber))
                .build();
    }

    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
        int accountId = request.getAccountNumber();
        int amount = request.getAmount();

        int availableBalance = accountDatabase.getBalance(accountId);
        if (availableBalance < amount) {
            Status status = Status.FAILED_PRECONDITION
                    .withDescription("Not enough money. You have only " + availableBalance);
            responseObserver.onError(status.asRuntimeException());
            return;
        }

        for (int i = 0; i < amount / 10; i++) {
            Money money = Money.newBuilder()
                    .setValue(10)
                    .build();
            int deductBalance = accountDatabase.deductBalance(accountId, 10);
            responseObserver.onNext(money);
            System.out.println("withdraw " + money);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<DepositRequest> deposit(StreamObserver<Balance> responseObserver) {
        return new DepositStreamObserver(responseObserver, accountDatabase);
    }

}
