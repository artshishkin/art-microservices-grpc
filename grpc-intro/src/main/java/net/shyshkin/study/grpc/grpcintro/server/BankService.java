package net.shyshkin.study.grpc.grpcintro.server;

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
        if (availableBalance > amount) {

            for (int i = 0; i < amount / 10; i++) {
                Money money = Money.newBuilder()
                        .setValue(10)
                        .build();
                int deductBalance = accountDatabase.deductBalance(accountId, 10);
                responseObserver.onNext(money);
                System.out.println("withdraw " + money);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
            responseObserver.onCompleted();
        } else
            responseObserver.onError(new RuntimeException("Not enough money"));
    }
}
