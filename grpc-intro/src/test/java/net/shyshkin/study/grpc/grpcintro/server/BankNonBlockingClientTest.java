package net.shyshkin.study.grpc.grpcintro.server;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import net.shyshkin.study.grpc.grpcintro.models.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        System.out.println("Starting gRPC server");

        server.start();

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forAddress("localhost", server.getPort())
                .usePlaintext()
                .build();
        nonBlockingStub = BankServiceGrpc.newStub(managedChannel);
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Shutdown gRPC server");
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
    void balanceTest(int accountNumber) throws InterruptedException {
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
            System.out.printf("Received balance: %d for user %d\n", value.getAmount(), accountNumber);
            assertThat(value.getAmount()).isEqualTo(expectedBalance);
        }

        @Override
        public void onError(Throwable t) {
            countDownLatch.countDown();
        }

        @Override
        public void onCompleted() {
            System.out.println("onCompleted ");
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
            System.out.printf("Withdraw money: %d\n", value.getValue());
            assertEquals(10, value.getValue());
            moneyList.add(value);
        }

        @Override
        public void onError(Throwable t) {
            System.out.println("Exception was thrown: " + t.getMessage());
            //then
            assertThat(t)
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessageContaining("FAILED_PRECONDITION: Not enough money. You have only ");
            countDownLatch.countDown();
        }

        @Override
        public void onCompleted() {
            System.out.println("onCompleted ");
            assertThat(moneyList)
                    .hasSize(expectedWithdrawalCount)
                    .allSatisfy(money -> assertThat(money.getValue()).isEqualTo(10));
            countDownLatch.countDown();
        }
    }
}