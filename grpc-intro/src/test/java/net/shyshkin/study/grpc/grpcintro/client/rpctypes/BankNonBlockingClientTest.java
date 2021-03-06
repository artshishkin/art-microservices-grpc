package net.shyshkin.study.grpc.grpcintro.client.rpctypes;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.grpc.grpcintro.models.*;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.AccountDatabase;
import net.shyshkin.study.grpc.grpcintro.server.rpctypes.BankService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class BankNonBlockingClientTest {

    private static BankServiceGrpc.BankServiceStub nonBlockingStub;
    private static Server server;
    private CountDownLatch countDownLatch;

    @BeforeAll
    static void beforeAll() throws IOException {

        AccountDatabase accountDatabase = new AccountDatabase();

        server = ServerBuilder
                .forPort(6565)
                .addService(new BankService(accountDatabase))
                .build();

        log.debug("Starting gRPC server");

        server.start();

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forAddress("localhost", server.getPort())
                .usePlaintext()
                .build();
        nonBlockingStub = BankServiceGrpc.newStub(managedChannel);
    }

    @AfterAll
    static void afterAll() {
        log.debug("Shutdown gRPC server");
        server.shutdown();
    }

    @BeforeEach
    void setUp() {
        countDownLatch = new CountDownLatch(1);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        countDownLatch.await();
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 5, 7})
    void balanceTest(int accountNumber) {
        //given
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();
        int expectedBalance = accountNumber * 111;
        StreamObserver<Balance> observer = new TestBalanceStreamObserver(accountNumber, expectedBalance);

        //when
        nonBlockingStub.getBalance(balanceCheckRequest, observer);
    }

    @Test
    void withdrawTest() {
        //given
        int accountNumber = 10;
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .setAmount(40)
                .build();
        int expectedWithdrawalCount = 4;
        StreamObserver<Money> observer = new TestMoneyStreamObserver(expectedWithdrawalCount);

        //when
        nonBlockingStub.withdraw(withdrawRequest, observer);
    }

    @Test
    void withdrawTest_notEnoughMoney() {
        //given
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(7)
                .setAmount(4000)
                .build();
        int expectedWithdrawalCount = 0;
        TestMoneyStreamObserver observer = new TestMoneyStreamObserver(expectedWithdrawalCount);

        //when
        nonBlockingStub.withdraw(withdrawRequest, observer);
    }

    private class TestBalanceStreamObserver implements StreamObserver<Balance> {
        private final int accountNumber;
        private final int expectedBalance;

        public TestBalanceStreamObserver(int accountNumber, int expectedBalance) {
            this.accountNumber = accountNumber;
            this.expectedBalance = expectedBalance;
        }

        @Override
        public void onNext(Balance value) {
            log.debug("Received balance: {} for user {}", value.getAmount(), accountNumber);
            assertThat(value.getAmount()).isEqualTo(expectedBalance);
        }

        @Override
        public void onError(Throwable t) {
            countDownLatch.countDown();
        }

        @Override
        public void onCompleted() {
            log.debug("onCompleted ");
            countDownLatch.countDown();
        }
    }

    private class TestMoneyStreamObserver implements StreamObserver<Money> {

        private final int expectedWithdrawalCount;
        private final List<Money> moneyList = new ArrayList<>();

        private TestMoneyStreamObserver(int expectedWithdrawalCount) {
            this.expectedWithdrawalCount = expectedWithdrawalCount;
        }

        @Override
        public void onNext(Money value) {
            log.debug("Withdraw money: {}", value.getValue());
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

    @Nested
    class DepositTest {

        @Test
        void depositTest() throws InterruptedException {
            //given
            int accountNumber = 9;
            int depositCount = 4;
            int chunkAmount = 10;
            int expectedBalance = accountNumber * 111 + depositCount * chunkAmount;

            TestResultWrapper testResultWrapper = new TestResultWrapper();

            StreamObserver<Balance> observer = new TestDepositBalanceStreamObserver(accountNumber, testResultWrapper);

            //when
            StreamObserver<DepositRequest> depositClient = nonBlockingStub.deposit(observer);

            for (int i = 0; i < depositCount; i++) {
                DepositRequest depositRequest = DepositRequest.newBuilder()
                        .setAccountNumber(accountNumber)
                        .setAmount(chunkAmount)
                        .build();
                depositClient.onNext(depositRequest);
            }
            depositClient.onCompleted();
//            Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
            countDownLatch.await();
            assertThat(testResultWrapper.getBalance()).isEqualTo(expectedBalance);
        }

        private class TestDepositBalanceStreamObserver implements StreamObserver<Balance> {

            private final int accountNumber;
            private final TestResultWrapper testResultWrapper;
            private int receivedBalance = 0;

            public TestDepositBalanceStreamObserver(int accountNumber, TestResultWrapper testResultWrapper) {
                this.accountNumber = accountNumber;
                this.testResultWrapper = testResultWrapper;
            }

            @Override
            public void onNext(Balance value) {
                receivedBalance = value.getAmount();
                log.debug("Received balance: {} for user {}", receivedBalance, accountNumber);
            }

            @Override
            public void onError(Throwable t) {
                log.debug("Exception occurred: " + t.getMessage());
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                log.debug("onCompleted ");
                testResultWrapper.setBalance(receivedBalance);
                countDownLatch.countDown();
            }
        }

        private class TestResultWrapper {
            int balance;

            public int getBalance() {
                return balance;
            }

            public void setBalance(int balance) {
                this.balance = balance;
            }
        }
    }
}