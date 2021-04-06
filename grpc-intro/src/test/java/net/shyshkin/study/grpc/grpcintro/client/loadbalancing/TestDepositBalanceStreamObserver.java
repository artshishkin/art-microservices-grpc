package net.shyshkin.study.grpc.grpcintro.client.loadbalancing;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.Balance;

import java.util.concurrent.CountDownLatch;

@Slf4j
@RequiredArgsConstructor
class TestDepositBalanceStreamObserver implements StreamObserver<Balance> {

    private final int accountNumber;
    private final TestResultWrapper testResultWrapper;
    private final CountDownLatch latch;

    private Balance receivedBalance = Balance.newBuilder().build();

    @Override
    public void onNext(Balance balance) {
        receivedBalance = balance;
        log.debug("Received balance: {} for user {}", receivedBalance.getAmount(), accountNumber);
    }

    @Override
    public void onError(Throwable t) {
        log.debug("Exception occurred: " + t.getMessage());
        latch.countDown();
    }

    @Override
    public void onCompleted() {
        log.debug("onCompleted ");
        testResultWrapper.setBalance(receivedBalance);
        latch.countDown();
    }
}
