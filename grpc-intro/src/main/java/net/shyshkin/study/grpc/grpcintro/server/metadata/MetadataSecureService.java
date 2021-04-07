package net.shyshkin.study.grpc.grpcintro.server.metadata;

import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.*;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.DepositStreamObserver;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class MetadataSecureService extends BankServiceGrpc.BankServiceImplBase {

    private final AccountDatabase accountDatabase;

    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        int accountNumber = request.getAccountNumber();

        UserRole userRole = ServerConstants.CTX_USER_ROLE.get(); //Like TreadLocal - only current thread can store and retrieve key

        if (UserRole.PRIME.equals(userRole) || ServerConstants.CTX_USER_ID.get() == accountNumber) {
            responseObserver.onNext(retrieveBalanceFromDB(accountNumber));
            responseObserver.onCompleted();
            return;
        }
        Status status = Status.PERMISSION_DENIED
                .withDescription("You have no permissions to process operation");

        responseObserver.onError(status.asRuntimeException());
    }

    private Balance retrieveBalanceFromDB(int accountNumber) {
        int amount = accountDatabase.getBalance(accountNumber);
        return Balance.newBuilder()
                .setAmount(amount)
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

            //simulate time-consuming load
            Uninterruptibles.sleepUninterruptibly(200, TimeUnit.MILLISECONDS);

            if (Context.current().isCancelled()) break;

            int deductBalance = accountDatabase.deductBalance(accountId, 10);
            responseObserver.onNext(money);
            log.debug("withdraw {}", money);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<DepositRequest> deposit(StreamObserver<Balance> responseObserver) {
        return new DepositStreamObserver(responseObserver, accountDatabase);
    }

}
