package net.shyshkin.study.grpc.grpcintro.client.deadline;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.*;
import net.shyshkin.study.grpc.grpcintro.server.deadline.DeadlineService;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.util.concurrent.Uninterruptibles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class DeadlineClientTest {

    private static BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private static Server server;
    private CountDownLatch countDownLatch;
    private static BankServiceGrpc.BankServiceStub nonBlockingStub;

    @BeforeAll
    static void beforeAll() throws IOException {

        AccountDatabase accountDatabase = new AccountDatabase();

        server = ServerBuilder
                .forPort(6363)
                .addService(new DeadlineService(accountDatabase))
                .build();

        log.debug("Starting gRPC server");

        server.start();

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forAddress("localhost", 6363)
                .intercept(new DeadlineInterceptor())
                .usePlaintext()
                .build();
        blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        nonBlockingStub = BankServiceGrpc.newStub(managedChannel);
    }

    @AfterAll
    static void afterAll() {
        log.debug("Shutdown gRPC server");
        server.shutdown();
    }

    @Test
    void balanceTest_OK() {
        //given
        int accountNumber = 33;
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        //when
        Balance balance = blockingStub
                .withDeadlineAfter(1, TimeUnit.SECONDS)
                .getBalance(balanceCheckRequest);

        //then
        log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
        assertEquals(accountNumber * 111, balance.getAmount());
    }


    @Test
    void balanceTest_withDeadLineExceeded() {
        //given
        int accountNumber = 8;
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        //when
        ThrowableAssert.ThrowingCallable exec = () -> {
            Balance balance = blockingStub
                    .withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .getBalance(balanceCheckRequest);
            log.debug("Received balance: {} for user {}", balance.getAmount(), accountNumber);
        };

        //then
        assertThatThrownBy(exec)
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("DEADLINE_EXCEEDED: deadline exceeded after");
    }

    @Test
    void withdrawTest() {
        //given
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(10)
                .setAmount(40)
                .build();

        //when
        Iterator<Money> moneyIterator = blockingStub
                .withDeadlineAfter(2, TimeUnit.SECONDS)
                .withdraw(withdrawRequest);

        //then
        List<Money> moneyList = new ArrayList<>();
        moneyIterator.forEachRemaining(moneyList::add);
        assertEquals(4, moneyList.size());
        moneyList.forEach(money -> assertEquals(10, money.getValue()));
    }

    @Test
    void withdrawTest_deadline() {
        //given
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(10)
                .setAmount(40)
                .build();

        //when
        ThrowableAssert.ThrowingCallable exec = () -> {
            Iterator<Money> moneyIterator = blockingStub
//                    .withDeadlineAfter(700, TimeUnit.MILLISECONDS)
                    .withdraw(withdrawRequest);
            List<Money> moneyList = new ArrayList<>();
            moneyIterator.forEachRemaining(moneyList::add);
            assertEquals(4, moneyList.size());
            moneyList.forEach(money -> assertEquals(10, money.getValue()));
        };

        //then
        assertThatThrownBy(exec)
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("DEADLINE_EXCEEDED: deadline exceeded after");
    }

    @Test
    @DisplayName("FIXED - If we are catching deadline exception and do not tell server about it, server continues sending stream to us")
    void withdrawTest_weiredStuff() {
        //given
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(3)
                .setAmount(200)
                .build();
        //when
        try {
            Iterator<Money> moneyIterator = blockingStub
//                    .withDeadlineAfter(700, TimeUnit.MILLISECONDS)
                    .withdraw(withdrawRequest);
            List<Money> moneyList = new ArrayList<>();
            moneyIterator.forEachRemaining(moneyList::add);
            assertEquals(4, moneyList.size());
            moneyList.forEach(money -> assertEquals(10, money.getValue()));
        } catch (StatusRuntimeException e) {
            log.debug("Exception was happened {}", e.getMessage());
        }
        //then
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
    }

    @Test
    void withdrawTest_notEnoughMoney() {
        //given
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(7)
                .setAmount(4000)
                .build();

        //when
        ThrowableAssert.ThrowingCallable exec = () -> {
            log.debug("Trying to withdraw not available amount: {}", withdrawRequest.getAmount());
            Iterator<Money> moneyIterator = blockingStub.withdraw(withdrawRequest);
            List<Money> moneyList = new ArrayList<>();
            moneyIterator.forEachRemaining(moneyList::add);
            assertEquals(400, moneyList.size());
        };

        //then
        assertThatThrownBy(exec)
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage("FAILED_PRECONDITION: Not enough money. You have only 777");
    }

    @Test
    void withdrawTest_nonBlocking_OK() throws InterruptedException {
        //given
        int accountNumber = 10;
        countDownLatch = new CountDownLatch(1);
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .setAmount(40)
                .build();
        int expectedWithdrawalCount = 4;
        StreamObserver<Money> observer = new TestMoneyStreamObserver(expectedWithdrawalCount);

        //when
        nonBlockingStub
                .withDeadlineAfter(1200, TimeUnit.MILLISECONDS)
                .withdraw(withdrawRequest, observer);
        countDownLatch.await();
    }

    private class TestMoneyStreamObserver implements StreamObserver<Money> {

        private final int expectedWithdrawalCount;
        private final List<Money> moneyList = new ArrayList<>();

        private TestMoneyStreamObserver(int expectedWithdrawalCount) {
            this.expectedWithdrawalCount = expectedWithdrawalCount;
        }

        @Override
        public void onNext(Money value) {
            log.debug("Received: {}", value.getValue());
            assertEquals(10, value.getValue());
            moneyList.add(value);
        }

        @Override
        public void onError(Throwable t) {
            log.debug("Exception was thrown: " + t.getMessage());
            //then
            assertThat(t)
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessageContaining("FAILED_PRECONDITION: Not enough money. You have only ");
            countDownLatch.countDown();
        }

        @Override
        public void onCompleted() {
            log.debug("onCompleted ");
            assertThat(moneyList)
                    .hasSize(expectedWithdrawalCount)
                    .allSatisfy(money -> assertThat(money.getValue()).isEqualTo(10));
            countDownLatch.countDown();
        }
    }

}