package net.shyshkin.study.grpc.grpcintro.server.loadbalancing;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.Balance;
import net.shyshkin.study.grpc.grpcintro.models.DepositRequest;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;

@Slf4j
@RequiredArgsConstructor
public class DepositStreamObserver implements StreamObserver<DepositRequest> {

    private final StreamObserver<Balance> responseObserver;
    private final AccountDatabase accountDatabase;
    private final int serverPort;
    private int accountId;

    @Override
    public void onNext(DepositRequest value) {
        log.debug("New Deposit: {} for {}", value.getAmount(), value.getAccountNumber());
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
                .setServerPort(serverPort)
                .build();
        responseObserver.onNext(balance);
        responseObserver.onCompleted();
    }
}
