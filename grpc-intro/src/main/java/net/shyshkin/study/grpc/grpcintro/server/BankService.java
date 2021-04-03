package net.shyshkin.study.grpc.grpcintro.server;

import io.grpc.stub.StreamObserver;
import net.shyshkin.study.grpc.grpcintro.models.Balance;
import net.shyshkin.study.grpc.grpcintro.models.BalanceCheckRequest;
import net.shyshkin.study.grpc.grpcintro.models.BankServiceGrpc;

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

}
